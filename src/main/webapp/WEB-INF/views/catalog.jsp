<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Catalog &middot; SHARVESHMART</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body>
<%@ include file="partials/_nav.jspf" %>
<main class="content">
    <h2>Browse products</h2>
    <c:if test="${not empty param.error}">
        <p class="error"><c:out value="${param.error}"/></p>
    </c:if>

    <form class="filters" method="get" action="${pageContext.request.contextPath}/products">
        <input type="text" name="q" value="<c:out value="${q}"/>" placeholder="Search products..." aria-label="Search products">
        <select name="category" aria-label="Category">
            <option value="">All categories</option>
            <c:forEach var="cat" items="${categories}">
                <option value="<c:out value="${cat}"/>" ${cat == category ? 'selected' : ''}><c:out value="${cat}"/></option>
            </c:forEach>
        </select>
        <button type="submit" class="btn small-btn">Search</button>
    </form>

    <c:choose>
        <c:when test="${empty products}">
            <p class="muted">No products found. Try a different keyword or category.</p>
        </c:when>
        <c:otherwise>
            <div class="product-grid">
                <c:forEach var="p" items="${products}">
                    <div class="product-card">
                        <h3><a href="${pageContext.request.contextPath}/products/${p.id}"><c:out value="${p.name}"/></a></h3>
                        <p class="muted small"><c:out value="${p.category}"/></p>
                        <p class="price">&#8377;<c:out value="${p.price}"/></p>
                        <p class="muted small">
                            <c:choose>
                                <c:when test="${p.stockQty > 0}">In stock: <c:out value="${p.stockQty}"/></c:when>
                                <c:otherwise>Out of stock</c:otherwise>
                            </c:choose>
                        </p>
                        <a class="btn small-btn" href="${pageContext.request.contextPath}/products/${p.id}">View</a>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</main>
</body>
</html>
