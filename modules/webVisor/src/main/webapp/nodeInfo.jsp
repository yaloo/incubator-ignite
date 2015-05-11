<%--@elvariable id="g" type="org.apache.ignite.Ignite"--%>
<%--@elvariable id="wu" type="org.apache.ignite.webVisor.render.WU"--%>
<%@ page import="org.apache.ignite.webVisor.render.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<% NodeInfoPage p = new NodeInfoPage(request); %>
<%--@elvariable id="p" type="org.apache.ignite.webVisor.render.NodeInfoPage"--%>
<head>
    <title>${wu.nodeAddr(p.localNode)}: Cluster</title>

    <%--<link rel="stylesheet" href="css/cluster.css" type="text/css">--%>
</head>
<body>

In development

</body>
</html>