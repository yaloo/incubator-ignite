/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.mesos;

import org.apache.ignite.mesos.resource.*;
import org.apache.mesos.*;
import org.slf4j.*;

import java.util.*;
import java.util.concurrent.atomic.*;

/**
 * Ignite scheduler receives offers from Mesos and decides how many resources will be occupied.
 */
public class IgniteScheduler implements Scheduler {
    /** Cpus. */
    public static final String CPUS = "cpus";

    /** Mem. */
    public static final String MEM = "mem";

    /** Disk. */
    public static final String DISK = "disk";

    /** Default port range. */
    public static final String DEFAULT_PORT = ":47500..47510";

    /** Delimiter to use in IP names. */
    public static final String DELIM = ",";

    /** Logger. */
    private static final Logger log = LoggerFactory.getLogger(IgniteScheduler.class);

    /** Mutex. */
    private static final Object mux = new Object();

    /** ID generator. */
    private AtomicInteger taskIdGenerator = new AtomicInteger();

    /** Task on host. */
    private Map<String, IgniteTask> tasks = new HashMap<>();

    /** Cluster resources. */
    private ClusterProperties clusterProps;

    /** Resource provider. */
    private ResourceProvider resourceProvider;

    /**
     * @param clusterProps Cluster limit.
     * @param resourceProvider Resource provider.
     */
    public IgniteScheduler(ClusterProperties clusterProps, ResourceProvider resourceProvider) {
        this.clusterProps = clusterProps;
        this.resourceProvider = resourceProvider;
    }

    /** {@inheritDoc} */
    @Override public void resourceOffers(SchedulerDriver schedulerDriver, List<Protos.Offer> offers) {
        synchronized (mux) {
            log.debug("Offers resources: {} ", offers.size());

            for (Protos.Offer offer : offers) {
                IgniteTask igniteTask = checkOffer(offer);

                // Decline offer which doesn't match by mem or cpu.
                if (igniteTask == null) {
                    schedulerDriver.declineOffer(offer.getId());

                    continue;
                }

                // Generate a unique task ID.
                Protos.TaskID taskId = Protos.TaskID.newBuilder()
                    .setValue(Integer.toString(taskIdGenerator.incrementAndGet())).build();

                log.info("Launching task: [{}]", igniteTask);

                // Create task to run.
                Protos.TaskInfo task = createTask(offer, igniteTask, taskId);

                schedulerDriver.launchTasks(Collections.singletonList(offer.getId()),
                    Collections.singletonList(task),
                    Protos.Filters.newBuilder().setRefuseSeconds(1).build());

                tasks.put(taskId.getValue(), igniteTask);
            }
        }
    }

    /**
     * Create Task.
     *
     * @param offer Offer.
     * @param igniteTask Task description.
     * @param taskId Task id.
     * @return Task.
     */
    protected Protos.TaskInfo createTask(Protos.Offer offer, IgniteTask igniteTask, Protos.TaskID taskId) {
        Protos.CommandInfo.Builder builder = Protos.CommandInfo.newBuilder()
            .setEnvironment(Protos.Environment.newBuilder().addVariables(Protos.Environment.Variable.newBuilder()
                .setName("IGNITE_TCP_DISCOVERY_ADDRESSES")
                .setValue(getAddress(offer.getHostname()))))
            .addUris(Protos.CommandInfo.URI.newBuilder()
                .setValue(resourceProvider.igniteUrl())
                .setExtract(true))
            .addUris(Protos.CommandInfo.URI.newBuilder().setValue(resourceProvider.igniteConfigUrl()));

        if (resourceProvider.resourceUrl() != null) {
            for (String url : resourceProvider.resourceUrl())
                builder.addUris(Protos.CommandInfo.URI.newBuilder().setValue(url));

            builder.setValue("cp *.jar ./gridgain-community-*/libs/ "
                + "&& ./gridgain-community-*/bin/ignite.sh "
                + resourceProvider.configName()
                + " -J-Xmx" + String.valueOf((int)igniteTask.mem() + "m")
                + " -J-Xms" + String.valueOf((int)igniteTask.mem()) + "m");
        }
        else
            builder.setValue("./gridgain-community-*/bin/ignite.sh "
                + resourceProvider.configName()
                + " -J-Xmx" + String.valueOf((int)igniteTask.mem() + "m")
                + " -J-Xms" + String.valueOf((int)igniteTask.mem()) + "m");

        return Protos.TaskInfo.newBuilder()
            .setName("Ignite node " + taskId.getValue())
            .setTaskId(taskId)
            .setSlaveId(offer.getSlaveId())
            .setCommand(builder)
            .addResources(Protos.Resource.newBuilder()
                .setName(CPUS)
                .setType(Protos.Value.Type.SCALAR)
                .setScalar(Protos.Value.Scalar.newBuilder().setValue(igniteTask.cpuCores())))
            .addResources(Protos.Resource.newBuilder()
                .setName(MEM)
                .setType(Protos.Value.Type.SCALAR)
                .setScalar(Protos.Value.Scalar.newBuilder().setValue(igniteTask.mem())))
            .addResources(Protos.Resource.newBuilder()
                .setName(DISK)
                .setType(Protos.Value.Type.SCALAR)
                .setScalar(Protos.Value.Scalar.newBuilder().setValue(igniteTask.disk())))
                .build();
    }

    /**
     * @return Address running nodes.
     */
    protected String getAddress(String address) {
        if (tasks.isEmpty()) {
            if (address != null && !address.isEmpty())
                return address + DEFAULT_PORT;

            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (IgniteTask task : tasks.values())
            sb.append(task.host()).append(DEFAULT_PORT).append(DELIM);

        return sb.substring(0, sb.length() - 1);
    }

    /**
     * Check slave resources and return resources infos.
     *
     * @param offer Offer request.
     * @return Ignite task description.
     */
    private IgniteTask checkOffer(Protos.Offer offer) {
        // Check limit on running nodes.
        if (clusterProps.instances() <= tasks.size())
            return null;

        double cpus = -1;
        double mem = -1;
        double disk = -1;

        // Collect resource on slave.
        for (Protos.Resource resource : offer.getResourcesList()) {
            if (resource.getName().equals(CPUS)) {
                if (resource.getType().equals(Protos.Value.Type.SCALAR))
                    cpus = resource.getScalar().getValue();
                else
                    log.debug("Cpus resource was not a scalar: {}" + resource.getType());
            }
            else if (resource.getName().equals(MEM)) {
                if (resource.getType().equals(Protos.Value.Type.SCALAR))
                    mem = resource.getScalar().getValue();
                else
                    log.debug("Mem resource was not a scalar: {}", resource.getType());
            }
            else if (resource.getName().equals(DISK))
                if (resource.getType().equals(Protos.Value.Type.SCALAR))
                    disk = resource.getScalar().getValue();
                else
                    log.debug("Disk resource was not a scalar: {}", resource.getType());
        }

        // Check that slave satisfies min requirements.
        if (cpus < clusterProps.minCpuPerNode() || mem < clusterProps.minMemoryPerNode() ) {
            log.debug("Offer not sufficient for slave request: {}", offer.getResourcesList());

            return null;
        }

        double totalCpus = 0;
        double totalMem = 0;
        double totalDisk = 0;

        // Collect occupied resources.
        for (IgniteTask task : tasks.values()) {
            totalCpus += task.cpuCores();
            totalMem += task.mem();
            totalDisk += task.disk();
        }

        cpus = Math.min(clusterProps.cpus() - totalCpus, Math.min(cpus, clusterProps.cpusPerNode()));
        mem = Math.min(clusterProps.memory() - totalMem, Math.min(mem, clusterProps.memoryPerNode()));
        disk = Math.min(clusterProps.disk() - totalDisk, Math.min(disk, clusterProps.diskPerNode()));

        if (cpus > 0 && mem > 0)
            return new IgniteTask(offer.getHostname(), cpus, mem, disk);
        else {
            log.debug("Offer not sufficient for slave request: {}", offer.getResourcesList());

            return null;
        }
    }

    /** {@inheritDoc} */
    @Override public void statusUpdate(SchedulerDriver schedulerDriver, Protos.TaskStatus taskStatus) {
        final String taskId = taskStatus.getTaskId().getValue();

        log.info("Received update event task: [{}] is in state: [{}]", taskId, taskStatus.getState());

        if (taskStatus.getState().equals(Protos.TaskState.TASK_FAILED)
            || taskStatus.getState().equals(Protos.TaskState.TASK_ERROR)
            || taskStatus.getState().equals(Protos.TaskState.TASK_FINISHED)
            || taskStatus.getState().equals(Protos.TaskState.TASK_KILLED)
            || taskStatus.getState().equals(Protos.TaskState.TASK_LOST)) {
            synchronized (mux) {
                IgniteTask failedTask = tasks.remove(taskId);

                if (failedTask != null) {
                    List<Protos.Request> requests = new ArrayList<>();

                    Protos.Request request = Protos.Request.newBuilder()
                        .addResources(Protos.Resource.newBuilder()
                            .setType(Protos.Value.Type.SCALAR)
                            .setName(MEM)
                            .setScalar(Protos.Value.Scalar.newBuilder().setValue(failedTask.mem())))
                        .addResources(Protos.Resource.newBuilder()
                            .setType(Protos.Value.Type.SCALAR)
                            .setName(CPUS)
                            .setScalar(Protos.Value.Scalar.newBuilder().setValue(failedTask.cpuCores())))
                        .build();

                    requests.add(request);

                    schedulerDriver.requestResources(requests);
                }
            }
        }
    }

    /**
     * @param clusterProps Cluster properties.
     */
    public void setClusterProps(ClusterProperties clusterProps) {
        this.clusterProps = clusterProps;
    }

    /** {@inheritDoc} */
    @Override public void registered(SchedulerDriver schedulerDriver, Protos.FrameworkID frameworkID,
        Protos.MasterInfo masterInfo) {
        log.info("Scheduler registered. Master: [{}:{}], framework=[{}]", masterInfo.getIp(), masterInfo.getPort(),
            frameworkID);
    }

    /** {@inheritDoc} */
    @Override public void disconnected(SchedulerDriver schedulerDriver) {
        log.info("Scheduler disconnected.");
    }

    /** {@inheritDoc} */
    @Override public void error(SchedulerDriver schedulerDriver, String s) {
        log.error("Failed. Error message: {}", s);
    }

    /** {@inheritDoc} */
    @Override public void frameworkMessage(SchedulerDriver schedulerDriver, Protos.ExecutorID executorID,
        Protos.SlaveID slaveID, byte[] bytes) {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public void slaveLost(SchedulerDriver schedulerDriver, Protos.SlaveID slaveID) {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public void executorLost(SchedulerDriver schedulerDriver, Protos.ExecutorID executorID,
        Protos.SlaveID slaveID, int i) {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public void offerRescinded(SchedulerDriver schedulerDriver, Protos.OfferID offerID) {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public void reregistered(SchedulerDriver schedulerDriver, Protos.MasterInfo masterInfo) {
        // No-op.
    }
}