<%--@elvariable id="g" type="org.apache.ignite.Ignite"--%>
<%--@elvariable id="wu" type="org.apache.ignite.webVisor.render.WU"--%>
<%@ page import="org.apache.ignite.webVisor.render.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<% ClusterPage p = new ClusterPage(request); %>
<%--@elvariable id="p" type="org.apache.ignite.webVisor.render.ClusterPage"--%>
<head>
    <title>${wu.nodeAddr(p.localNode)}: Cluster</title>

    <link rel="stylesheet" href="css/cluster.css" type="text/css">
</head>
<body>

<h2>Nodes in the cluster</h2>

<div class="row">
    <div class="col-md-6">
        <c:forEach items="${p.nodes}" var="node">
            <%--@elvariable id="node" type="org.apache.ignite.spi.discovery.tcp.internal.TcpDiscoveryNode"--%>
            <div class="cluster-node">
                <span class="glyphicon ${node.client ? 'glyphicon-phone' : 'glyphicon-hdd'}"
                      aria-hidden="true"
                      title="${node.client ? 'Client node' : 'Server node'}"></span>

                <strong>${wu.nodeAddr(node)}</strong> <br>
                <small><span class="uuid">${node.id()}</span></small>
            </div>
        </c:forEach>
    </div>

    <div class="col-md-2">

    </div>

    <div class="col-md-4">
        <h4>Filters</h4>

        <form role="form" action="cluster.jsp">
            <input type="hidden" name="filter" value="true">

            <div class="checkbox">
                <label><input type="checkbox" name="showClients" <%= p.isShowClients() ? "checked=checked" : "" %> onchange="submit()">Show clients</label>
            </div>
        </form>
    </div>
</div>


</body>
</html>