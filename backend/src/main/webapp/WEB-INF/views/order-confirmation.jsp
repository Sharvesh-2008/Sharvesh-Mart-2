<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Order &middot; SHARVESHMART</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body>
<%@ include file="partials/_nav.jspf" %>
<main class="content">
    <div class="confirmation">
        <h2>Order confirmed</h2>
        <p class="notice">Thank you! Your mock payment was successful and your order has been placed.</p>
    </div>

    <p>Order #<c:out value="${order.id}"/>
        &middot; <c:out value="${order.createdAt}"/>
        &middot; <span class="status <c:out value="${order.status}"/>"><c:out value="${order.status}"/></span></p>

    <table class="table">
        <thead>
        <tr>
            <th>Product</th>
            <th>Qty</th>
            <th>Unit price</th>
            <th>Total</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="item" items="${items}">
            <tr>
                <td><c:out value="${item.productName}"/></td>
                <td><c:out value="${item.quantity}"/></td>
                <td>&#8377;<c:out value="${item.unitPrice}"/></td>
                <td>&#8377;<c:out value="${item.quantity * item.unitPrice}"/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <p class="total">Total: &#8377;<c:out value="${order.totalAmount}"/></p>

    <p><a class="btn small-btn" href="${pageContext.request.contextPath}/orders">View all orders</a></p>
</main>
</body>
</html>