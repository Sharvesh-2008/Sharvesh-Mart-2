<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Your orders &middot; SHARVESHMART</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body>
<%@ include file="partials/_nav.jspf" %>
<main class="content">
    <h2>Your orders</h2>

    <c:choose>
        <c:when test="${empty orders}">
            <p class="muted">You have not placed any orders yet.</p>
            <a class="btn small-btn" href="${pageContext.request.contextPath}/products">Browse products</a>
        </c:when>
        <c:otherwise>
            <table class="table">
                <thead>
                <tr>
                    <th>Order</th>
                    <th>Date</th>
                    <th>Status</th>
                    <th>Total</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="order" items="${orders}">
                    <tr>
                        <td>#<c:out value="${order.id}"/></td>
                        <td><c:out value="${order.createdAt}"/></td>
                        <td><span class="status <c:out value="${order.status}"/>"><c:out value="${order.status}"/></span></td>
                        <td>&#8377;<c:out value="${order.totalAmount}"/></td>
                        <td><a class="btn small-btn ghost" href="${pageContext.request.contextPath}/orders/${order.id}">View</a></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</main>
</body>
</html>