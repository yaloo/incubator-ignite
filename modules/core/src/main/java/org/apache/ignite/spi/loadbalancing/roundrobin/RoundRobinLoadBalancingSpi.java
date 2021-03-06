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

package org.apache.ignite.spi.loadbalancing.roundrobin;

import org.apache.ignite.*;
import org.apache.ignite.cluster.*;
import org.apache.ignite.compute.*;
import org.apache.ignite.events.*;
import org.apache.ignite.internal.managers.eventstorage.*;
import org.apache.ignite.internal.util.typedef.internal.*;
import org.apache.ignite.lang.*;
import org.apache.ignite.resources.*;
import org.apache.ignite.spi.*;
import org.apache.ignite.spi.loadbalancing.*;
import org.jetbrains.annotations.*;
import org.jsr166.*;

import java.util.*;

import static org.apache.ignite.events.EventType.*;

/**
 * This SPI iterates through nodes in round-robin fashion and pick the next
 * sequential node. Two modes of operation are supported: per-task and global
 * (see {@link #setPerTask(boolean)} configuration).
 * <p>
 * When configured in per-task mode, implementation will pick a random starting
 * node at the beginning of every task execution and then sequentially iterate through all
 * nodes in topology starting from the picked node. This is the default configuration
 * and should fit most of the use cases as it provides a fairly well-distributed
 * split and also ensures that jobs within a single task are spread out across
 * nodes to the maximum. For cases when split size is equal to the number of nodes,
 * this mode guarantees that all nodes will participate in the split.
 * <p>
 * When configured in global mode, a single sequential queue of nodes is maintained for
 * all tasks and the next node in the queue is picked every time. In this mode (unlike in
 * {@code per-task} mode) it is possible that even if split size may be equal to the
 * number of nodes, some jobs within the same task will be assigned to the same node if
 * multiple tasks are executing concurrently.
 * <h1 class="header">Coding Example</h1>
 * If you are using {@link org.apache.ignite.compute.ComputeTaskSplitAdapter} then load balancing logic
 * is transparent to your code and is handled automatically by the adapter.
 * Here is an example of how your task will look:
 * <pre name="code" class="java">
 * public class MyFooBarTask extends GridComputeTaskSplitAdapter&lt;Object, Object&gt; {
 *    &#64;Override
 *    protected Collection&lt;? extends ComputeJob&gt; split(int gridSize, Object arg) throws IgniteCheckedException {
 *        List&lt;MyFooBarJob&gt; jobs = new ArrayList&lt;MyFooBarJob&gt;(gridSize);
 *
 *        for (int i = 0; i &lt; gridSize; i++) {
 *            jobs.add(new MyFooBarJob(arg));
 *        }
 *
 *        // Node assignment via load balancer
 *        // happens automatically.
 *        return jobs;
 *    }
 *    ...
 * }
 * </pre>
 * If you need more fine-grained control over how some jobs within task get mapped to a node
 * and use affinity load balancing for some other jobs within task, then you should use
 * {@link org.apache.ignite.compute.ComputeTaskAdapter}. Here is an example of how your task will look. Note that in this
 * case we manually inject load balancer and use it to pick the best node. Doing it in
 * such way would allow user to map some jobs manually and for others use load balancer.
 * <pre name="code" class="java">
 * public class MyFooBarTask extends ComputeTaskAdapter&lt;String, String&gt; {
 *    // Inject load balancer.
 *    &#64;LoadBalancerResource
 *    ComputeLoadBalancer balancer;
 *
 *    // Map jobs to grid nodes.
 *    public Map&lt;? extends ComputeJob, GridNode&gt; map(List&lt;GridNode&gt; subgrid, String arg) throws IgniteCheckedException {
 *        Map&lt;MyFooBarJob, GridNode&gt; jobs = new HashMap&lt;MyFooBarJob, GridNode&gt;(subgrid.size());
 *
 *        // In more complex cases, you can actually do
 *        // more complicated assignments of jobs to nodes.
 *        for (int i = 0; i &lt; subgrid.size(); i++) {
 *            // Pick the next best balanced node for the job.
 *            jobs.put(new MyFooBarJob(arg), balancer.getBalancedNode())
 *        }
 *
 *        return jobs;
 *    }
 *
 *    // Aggregate results into one compound result.
 *    public String reduce(List&lt;ComputeJobResult&gt; results) throws IgniteCheckedException {
 *        // For the purpose of this example we simply
 *        // concatenate string representation of every
 *        // job result
 *        StringBuilder buf = new StringBuilder();
 *
 *        for (ComputeJobResult res : results) {
 *            // Append string representation of result
 *            // returned by every job.
 *            buf.append(res.getData().string());
 *        }
 *
 *        return buf.string();
 *    }
 * }
 * </pre>
 * <p>
 * <h1 class="header">Configuration</h1>
 * In order to use this load balancer, you should configure your grid instance
 * to use {@code GridRoundRobinLoadBalancingSpi} either from Spring XML file or
 * directly. The following configuration parameters are supported:
 * <h2 class="header">Mandatory</h2>
 * This SPI has no mandatory configuration parameters.
 * <h2 class="header">Optional</h2>
 * The following configuration parameters are optional:
 * <ul>
 * <li>
 *      Flag that indicates whether to use {@code per-task} or global
 *      round-robin modes described above (see {@link #setPerTask(boolean)}).
 * </li>
 * </ul>
 * Below is Java configuration example:
 * <pre name="code" class="java">
 * GridRandomLoadBalancingSpi = new GridRandomLoadBalancingSpi();
 *
 * // Configure SPI to use global round-robin mode.
 * spi.setPerTask(false);
 *
 * GridConfiguration cfg = new GridConfiguration();
 *
 * // Override default load balancing SPI.
 * cfg.setLoadBalancingSpi(spi);
 *
 * // Starts grid.
 * G.start(cfg);
 * </pre>
 * Here is how you can configure {@code GridRandomLoadBalancingSpi} using Spring XML configuration:
 * <pre name="code" class="xml">
 * &lt;property name="loadBalancingSpi"&gt;
 *     &lt;bean class="org.apache.ignite.spi.loadBalancing.roundrobin.GridRoundRobinLoadBalancingSpi"&gt;
 *         &lt;!-- Set to global round-robin mode. --&gt;
 *         &lt;property name="perTask" value="false"/&gt;
 *     &lt;/bean&gt;
 * &lt;/property&gt;
 * </pre>
 * <p>
 * <img src="http://ignite.incubator.apache.org/images/spring-small.png">
 * <br>
 * For information about Spring framework visit <a href="http://www.springframework.org/">www.springframework.org</a>
 */
@IgniteSpiMultipleInstancesSupport(true)
public class RoundRobinLoadBalancingSpi extends IgniteSpiAdapter implements LoadBalancingSpi,
    RoundRobinLoadBalancingSpiMBean {
    /** Grid logger. */
    @LoggerResource
    private IgniteLogger log;

    /** */
    private RoundRobinGlobalLoadBalancer balancer;

    /** */
    private boolean isPerTask;

    /** */
    private final Map<IgniteUuid, RoundRobinPerTaskLoadBalancer> perTaskBalancers =
        new ConcurrentHashMap8<>();

    /** Event listener. */
    private final GridLocalEventListener lsnr = new GridLocalEventListener() {
        @Override public void onEvent(Event evt) {
            if (evt.type() == EVT_TASK_FAILED ||
                evt.type() == EVT_TASK_FINISHED)
                perTaskBalancers.remove(((TaskEvent)evt).taskSessionId());
            else if (evt.type() == EVT_JOB_MAPPED) {
                RoundRobinPerTaskLoadBalancer balancer =
                    perTaskBalancers.get(((JobEvent)evt).taskSessionId());

                if (balancer != null)
                    balancer.onMapped();
            }
        }
    };

    /** {@inheritDoc} */
    @Override public boolean isPerTask() {
        return isPerTask;
    }

    /**
     * Configuration parameter indicating whether a new round robin order should be
     * created for every task. If {@code true} then load balancer is guaranteed
     * to iterate through nodes sequentially for every task - so as long as number
     * of jobs is less than or equal to the number of nodes, jobs are guaranteed to
     * be assigned to unique nodes. If {@code false} then one round-robin order
     * will be maintained for all tasks, so when tasks execute concurrently, it
     * is possible for more than one job within task to be assigned to the same
     * node.
     * <p>
     * Default is {@code false}.
     *
     * @param isPerTask Configuration parameter indicating whether a new round robin order should
     *      be created for every task. Default is {@code false}.
     */
    @IgniteSpiConfiguration(optional = true)
    public void setPerTask(boolean isPerTask) {
        this.isPerTask = isPerTask;
    }

    /** {@inheritDoc} */
    @Override public void spiStart(@Nullable String gridName) throws IgniteSpiException {
        startStopwatch();

        if (log.isDebugEnabled())
            log.debug(configInfo("isPerTask", isPerTask));

        registerMBean(gridName, this, RoundRobinLoadBalancingSpiMBean.class);

        balancer = new RoundRobinGlobalLoadBalancer(log);

        // Ack ok start.
        if (log.isDebugEnabled())
            log.debug(startInfo());
    }

    /** {@inheritDoc} */
    @Override public void spiStop() throws IgniteSpiException {
        balancer = null;

        perTaskBalancers.clear();

        unregisterMBean();

        // Ack ok stop.
        if (log.isDebugEnabled())
            log.debug(stopInfo());
    }

    /** {@inheritDoc} */
    @Override protected void onContextInitialized0(IgniteSpiContext spiCtx) throws IgniteSpiException {
        if (!isPerTask)
            balancer.onContextInitialized(spiCtx);
        else {
            if (!getSpiContext().isEventRecordable(EVT_TASK_FAILED, EVT_TASK_FINISHED, EVT_JOB_MAPPED))
                throw new IgniteSpiException("Required event types are disabled: " +
                    U.gridEventName(EVT_TASK_FAILED) + ", " +
                    U.gridEventName(EVT_TASK_FINISHED) + ", " +
                    U.gridEventName(EVT_JOB_MAPPED));

            getSpiContext().addLocalEventListener(lsnr, EVT_TASK_FAILED, EVT_TASK_FINISHED, EVT_JOB_MAPPED);
        }
    }

    /** {@inheritDoc} */
    @Override protected void onContextDestroyed0() {
        if (!isPerTask) {
            if (balancer != null)
                balancer.onContextDestroyed();
        }
        else {
            IgniteSpiContext spiCtx = getSpiContext();

            if (spiCtx != null)
                spiCtx.removeLocalEventListener(lsnr);
        }
    }

    /** {@inheritDoc} */
    @Override public ClusterNode getBalancedNode(ComputeTaskSession ses, List<ClusterNode> top, ComputeJob job) {
        A.notNull(ses, "ses", top, "top");

        if (isPerTask) {
            // Note that every session operates from single thread which
            // allows us to use concurrent map and avoid synchronization.
            RoundRobinPerTaskLoadBalancer taskBalancer = perTaskBalancers.get(ses.getId());

            if (taskBalancer == null)
                perTaskBalancers.put(ses.getId(), taskBalancer = new RoundRobinPerTaskLoadBalancer());

            return taskBalancer.getBalancedNode(top);
        }

        return balancer.getBalancedNode(top);
    }

    /**
     * THIS METHOD IS USED ONLY FOR TESTING.
     *
     * @param ses Task session.
     * @return Internal list of nodes.
     */
    List<UUID> getNodeIds(ComputeTaskSession ses) {
        if (isPerTask) {
            RoundRobinPerTaskLoadBalancer balancer = perTaskBalancers.get(ses.getId());

            if (balancer == null)
                return Collections.emptyList();

            List<UUID> ids = new ArrayList<>();

            for (ClusterNode node : balancer.getNodes()) {
                ids.add(node.id());
            }

            return ids;
        }

        return balancer.getNodeIds();
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(RoundRobinLoadBalancingSpi.class, this);
    }
}
