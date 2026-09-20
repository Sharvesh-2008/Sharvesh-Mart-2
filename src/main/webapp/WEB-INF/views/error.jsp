<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
    if (statusCode == null) {
        statusCode = 500;
    }
    request.setAttribute("errorCode", statusCode);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error &middot; SHARVESHMART</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body>
<main class="auth-card">
    <h1 class="brand">SHARVESHMART</h1>
    <h2>Something went wrong</h2>
    <p class="error">
        <c:choose>
            <c:when test="${errorCode == 404}">Page not found (404).</c:when>
            <c:otherwise>An unexpected error occurred. Please try again.</c:otherwise>
        </c:choose>
    </p>
    <p><a class="btn" href="${pageContext.request.contextPath}/">Back to SHARVESHMART</a></p>
</main>
</body>
</html>
