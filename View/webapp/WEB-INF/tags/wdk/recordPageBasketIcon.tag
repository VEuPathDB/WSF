<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="w" uri="http://www.servletsuite.com/servlets/wraptag" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set value="${requestScope.wdkRecord}" var="wdkRecord"/>
<c:set value="${wdkRecord.primaryKey.values}" var="vals"/>
<c:set value="${vals['source_id']}" var="id"/>
<c:set value="${vals['project_id']}" var="pid"/>
        
<c:choose>
	<c:when test="${wdkUser.guest}">
	  <a class="basket" href="javascript:void(0)" onClick="popLogin()">
		<img src="<c:url value='/wdk/images/basket_gray.png'/>" value="0" title="Please log in to use basket."/>
	  </a>
    </c:when>
	<c:otherwise>
		<c:set var="image" value="${wdkRecord.inBasket ? 'color' : 'gray'}" />
		<c:set var="imagevalue" value="${wdkRecord.inBasket ? '1' : '0'}"/>
		<c:set var="imagetitle" value="${wdkRecord.inBasket ? 'Click to remove this item from the basket.' : 'Click to add this item to the basket.'}"/>
        <a class="basket" href="javascript:void(0)" onClick="updateBasket(this, 'single', '${id}', '${pid}', '${wdkRecord.recordClass.fullName}')">
			<img src="<c:url value='/wdk/images/basket_${image}.png'/>" value="${imagevalue}" title="${imagetitle}"/>
		</a>
	</c:otherwise>
</c:choose>
		