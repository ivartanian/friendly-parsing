<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Главная</title>
</head>
<body>

<form action="${pageContext.request.contextPath}/parse" method="POST">

    <label for="site_url" >Введите сайт:</label>
    <input type="text" id="site_url" name="site_url" value="http://"/>
    <br>
    <label for="max_deep" >Глубина парсинга:</label>
    <input type="number" id="max_deep" name="max_deep" value="2"/>
    <br>
    <input type="submit" value="Парсить"/>

</form>

</body>
</html>
