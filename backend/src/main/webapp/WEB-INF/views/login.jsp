<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign in &middot; SHARVESHMART</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body>
<main class="auth-card">
    <h1 class="brand">SHARVESHMART</h1>
    <p class="tagline">Multi-seller marketplace</p>
    <h2>Sign in</h2>

    <c:if test="${param.loggedout != null}">
        <p class="notice">You have been signed out.</p>
    </c:if>
    <c:if test="${param.registered != null}">
        <p class="notice">Account created successfully &mdash; please sign in.</p>
    </c:if>
    <c:if test="${not empty error}">
        <p class="error"><c:out value="${error}"/></p>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/login" accept-charset="UTF-8">
        <input type="hidden" name="redirect" value="<c:out value="${param.redirect}"/>">
        <label for="email">Email</label>
        <input type="email" id="email" name="email"
               value="<c:out value="${empty email ? param.email : email}"/>" required autofocus>
        <label for="password">Password</label>
        <input type="password" id="password" name="password" required>
        <button type="submit" class="btn">Sign in</button>
    </form>

    <p class="muted">New here? <a href="${pageContext.request.contextPath}/register">Create an account</a></p>
    <p class="muted small">Demo buyer &mdash; buyer@SHARVESHMART.com / Buyer@123</p>
</main>
</body>
</html>
