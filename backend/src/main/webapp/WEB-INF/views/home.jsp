<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Home &middot; SHARVESHMART</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body>
<nav class="topbar">
    <span class="brand small">SHARVESHMART</span>
    <span class="spacer"></span>
    <span class="muted small">Signed in as <c:out value="${sessionScope.user.email}"/></span>
    <a class="role-badge" href="${pageContext.request.contextPath}/home"><c:out value="${sessionScope.user.role}"/></a>
    <a class="btn ghost" href="${pageContext.request.contextPath}/logout">Sign out</a>
</nav>

<main class="content">
    <h2>Welcome, <c:out value="${sessionScope.user.name}"/>!</h2>
    <p class="muted">Week 2 milestone &mdash; browse the catalog, shop your cart, and track your orders.</p>

    <section class="placeholder-grid">
        <div class="placeholder-card">
            <h3><a href="${pageContext.request.contextPath}/products">Browse products</a></h3>
            <p class="muted small">Search and filter the catalog by keyword and category.</p>
        </div>
        <div class="placeholder-card">
            <h3><a href="${pageContext.request.contextPath}/cart">Cart</a></h3>
            <p class="muted small">Review items, update quantities, and see your running total.</p>
        </div>
        <div class="placeholder-card">
            <h3><a href="${pageContext.request.contextPath}/orders">Orders</a></h3>
            <p class="muted small">Checkout with a mock payment and track your order history.</p>
        </div>
        <c:if test="${sessionScope.user.role == 'SELLER'}">
            <div class="placeholder-card">
                <h3><a href="${pageContext.request.contextPath}/seller/orders">Seller orders</a></h3>
                <p class="muted small">See orders that contain your products.</p>
            </div>
        </c:if>
        <c:if test="${sessionScope.user.role == 'ADMIN'}">
            <div class="placeholder-card">
                <h3>Admin panel</h3>
                <p class="muted small">Coming in Week 4</p>
            </div>
        </c:if>
    </section>
</main>
</body>
</html>
