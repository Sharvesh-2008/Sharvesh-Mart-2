<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${product.name}"/> &middot; SHARVESHMART</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body>
<%@ include file="partials/_nav.jspf" %>
<main class="content">
    <p><a class="muted small" href="${pageContext.request.contextPath}/products">&larr; Back to catalog</a></p>
    <c:if test="${not empty param.error}">
        <p class="error"><c:out value="${param.error}"/></p>
    </c:if>

    <h2><c:out value="${product.name}"/></h2>
    <p class="muted"><c:out value="${product.category}"/></p>
    <p class="price">&#8377;<c:out value="${product.price}"/></p>
    <p><c:out value="${product.description}"/></p>
    <p class="muted small">
        <c:choose>
            <c:when test="${product.stockQty > 0}">In stock: <c:out value="${product.stockQty}"/></c:when>
            <c:otherwise>Out of stock</c:otherwise>
        </c:choose>
    </p>

    <c:if test="${product.stockQty > 0}">
        <form class="qty-form" method="post" action="${pageContext.request.contextPath}/cart/add">
            <input type="hidden" name="productId" value="${product.id}">
            <label for="qty">Quantity</label>
            <div class="qty-row">
                <input type="number" id="qty" name="quantity" value="1" min="1" max="${product.stockQty}" required>
                <button type="submit" class="btn small-btn">Add to cart</button>
            </div>
        </form>
    </c:if>
</main>
</body>
</html>