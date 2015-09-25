<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Главная</title>
</head>
<body>
<div>
    <a href="/friendly-parsing">На главную</a>
</div>
<div>
    <c:forEach var="varItem" items="${resultItems}">
        <div>
            - <a href=${varItem.link}>${varItem.link}</a> <c:out value=" : score - ${varItem.score}, pass - ${varItem.pass}"/>
    </c:forEach>
</div>

</body>
</html>
