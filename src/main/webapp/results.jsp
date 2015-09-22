<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Главная</title>
</head>
<body>
<div>
    <a href="/testing">На главную</a>
</div>
<div>
    <c:forEach var="varItem" items="${resultItems}">
        <c:out value=" - ${varItem.url}: score - ${varItem.score}, pass - ${varItem.pass}"/><br/>
    </c:forEach>
</div>

</body>
</html>
