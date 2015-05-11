<%--@elvariable id="g" type="org.apache.ignite.Ignite"--%>
<%--@elvariable id="wu" type="org.apache.ignite.webVisor.render.WU"--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <title><sitemesh:write property='title'/></title>

    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->

    <link rel="stylesheet" href="css/main.css" type="text/css">
    
    <sitemesh:write property='head'/>
</head>
<body>
<nav class="navbar navbar-default">
    <div class="container">
        <div class="navbar-header">
            <a class="navbar-brand" href="${pageContext.request.contextPath}/">Ignite</a>
        </div>
        <div>
            <ul class="nav navbar-nav">
                <li><a href="nodeInfo.jsp">Node</a></li>
                <li><a href="cluster.jsp">Cluster</a></li>
                <li><a href="caches.jsp">Caches</a></li>
            </ul>
            <ul class="nav navbar-nav navbar-right">
                <li style="color: gray">
                    <small>Local node: ${wu.nodeAddr(wu.localNode())}</small>

                    <c:if test="${g.name() != null}">
                        <br> <small>Name: ${g.name()}</small>
                    </c:if>
                </li>
            </ul>
        </div>
    </div>
</nav>

<div class="container">
    <sitemesh:write property='body'/>
</div>

<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
</body>
</html>