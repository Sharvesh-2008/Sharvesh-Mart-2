<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create an account &middot; SHARVESHMART</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body>
<main class="auth-card">
    <h1 class="brand">SHARVESHMART</h1>
    <p class="tagline">Join as a buyer or a seller</p>
    <h2>Create an account</h2>

    <c:if test="${not empty error}">
        <p class="error"><c:out value="${error}"/></p>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/register" accept-charset="UTF-8">
        <label for="name">Full name</label>
        <input type="text" id="name" name="name" maxlength="100"
               value="<c:out value="${name}"/>" required autofocus>

        <label for="email">Email</label>
        <input type="email" id="email" name="email"
               value="<c:out value="${email}"/>" required>

        <label for="password">Password <span class="muted small">(min 8 chars, letter + digit)</span></label>
        <input type="password" id="password" name="password" minlength="8" required>

        <label for="role">I want to</label>
        <select id="role" name="role" required>
            <option value="" disabled <c:if test="${empty role}">selected</c:if>>Select a role&hellip;</option>
            <option value="BUYER" <c:if test="${role == 'BUYER'}">selected</c:if>>Buy products</option>
            <option value="SELLER" <c:if test="${role == 'SELLER'}">selected</c:if>>Sell products</option>
        </select>

        <button type="submit" class="btn">Create account</button>
    </form>

    <p class="muted">Already have an account? <a href="${pageContext.request.contextPath}/login">Sign in</a></p>
</main>
</body>
</html>
