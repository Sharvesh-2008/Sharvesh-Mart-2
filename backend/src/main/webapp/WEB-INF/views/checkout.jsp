<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Checkout &middot; SHARVESHMART</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body>
<%@ include file="partials/_nav.jspf" %>
<main class="content">
    <h2>Checkout</h2>

    <table class="table">
        <thead>
        <tr>
            <th>Product</th>
            <th>Qty</th>
            <th>Unit price</th>
            <th>Line total</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="line" items="${cart.lines}">
            <tr>
                <td><c:out value="${line.productName}"/></td>
                <td><c:out value="${line.quantity}"/></td>
                <td>&#8377;<c:out value="${line.unitPrice}"/></td>
                <td>&#8377;<c:out value="${line.lineTotal}"/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <p class="total">Total payable: &#8377;<c:out value="${cart.total}"/></p>

    <p class="notice">Payment confirmation is mocked for this project &mdash; no real money is charged.</p>
    <form method="post" action="${pageContext.request.contextPath}/checkout">
        <label class="confirm-label">
            <input type="checkbox" name="confirm" value="1" required>
            I confirm this mock payment of &#8377;<c:out value="${cart.total}"/>.
        </label>
        <button type="submit" class="btn">Confirm &amp; place order</button>
    </form>
</main>
</body>
</html>