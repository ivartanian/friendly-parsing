<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Результат</title>

    <script src="<c:url value="/webjars/jquery/2.1.3/jquery.js"/>"></script>

    <script src="<c:url value="/resources/dataTables_1_10_7/js/jquery.dataTables.js"/>"></script>

    <link href="<c:url value="/resources/dataTables_1_10_7/css/jquery.dataTables.css"/>" rel="stylesheet" type="text/css"/>

    <script>
        $(document).ready(function() {
            var table = $('#items').DataTable();
        } );
    </script>

</head>
<body>
<div>
    <a href="${pageContext.request.contextPath}">На главную</a>
</div>
<div>
    <h2 style="text-align:center">Results: ${site}</h2>
    <table id="items" class="display" cellspacing="0" border="1" width="100%">
        <thead>
        <tr>
            <th >URL</th>
            <th>Score</th>
            <th>Pass</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="varItem" items="${resultItems}">
            <tr>
                <td style="text-align:left">
                    <a href=${varItem.url}>${varItem.url}</a>
                </td>
                <td>
                    <c:out value="${varItem.score}"/>
                </td>
                <td>
                    <c:out value="${varItem.pass}"/>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

</body>
</html>
