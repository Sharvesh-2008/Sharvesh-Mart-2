<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Your cart &middot; SHARVESHMART</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body>
<%@ include file="partials/_nav.jspf" %>
<main class="content">
    <h2>Your cart</h2>
    <c:if test="${not empty param.added}">
        <p class="notice">Product added to your cart.</p>
    </c:if>
    <c:if test="${not empty param.error}">
        <p class="error"><c:out value="${param.error}"/></p>
    </c:if>

    <c:choose>
        <c:when test="${empty cart.lines}">
            <p class="muted">Your cart is empty.</p>
            <a class="btn small-btn" href="${pageContext.request.contextPath}/products">Browse products</a>
        </c:when>
        <c:otherwise>
            <table class="table">
                <thead>
                <tr>
                    <th>Product</th>
                    <th>Unit price</th>
                    <th>Quantity</th>
                    <th>Line total</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="line" items="${cart.lines}">
                    <tr>
                        <td><a href="${pageContext.request.contextPath}/products/${line.productId}"><c:out value="${line.productName}"/></a></td>
                        <td>&#8377;<c:out value="${line.unitPrice}"/></td>
                        <td>
                            <form class="inline-form" method="post" action="${pageContext.request.contextPath}/cart/update">
                                <input type="hidden" name="itemId" value="${line.cartItemId}">
                                <input class="qty-input" type="number" name="quantity" value="${line.quantity}" min="1" max="99" required>
                                <button type="submit" class="btn small-btn ghost">Update</button>
                            </form>
                        </td>
                        <td>&#8377;<c:out value="${line.lineTotal}"/></td>
                        <td>
                            <form class="inline-form" method="post" action="${pageContext.request.contextPath}/cart/remove">
                                <input type="hidden" name="itemId" value="${line.cartItemId}">
                                <button type="submit" class="btn small-btn ghost">Remove</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
            <p class="total">Total: &#8377;<c:out value="${cart.total}"/></p>
            <a class="btn small-btn" href="${pageContext.request.contextPath}/checkout">Proceed to checkout</a>
        </c:otherwise>
    </c:choose>
</main>
</body>
</html>