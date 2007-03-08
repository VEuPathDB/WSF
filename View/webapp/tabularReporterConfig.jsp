<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>

<!-- get wdkAnswer from requestScope -->
<jsp:useBean id="wdkUser" scope="session" type="org.gusdb.wdk.model.jspwrap.UserBean"/>
<c:set value="${requestScope.wdkAnswer}" var="wdkAnswer"/>
<c:set var="history_id" value="${requestScope.wdk_history_id}"/>
<c:set var="format" value="${requestScope.wdkReportFormat}"/>


<!-- display page header -->
<site:header banner="Create and download a Report in Tabular Format" />

<!-- display description for page -->
<p><b>Generate a tab delimited report of your query result.  Select columns to include in the report.  Optionally include a first line with column names</b></p>

<!-- display the parameters of the question, and the format selection form -->
<wdk:reporter/>

<!-- handle empty result set situation -->
<c:choose>
  <c:when test='${wdkAnswer.resultSize == 0}'>
    No results for your query
  </c:when>
  <c:otherwise>

<!-- content of current page -->
<form name="downloadConfigForm" method="get" action="<c:url value='/getDownloadResult.do' />">
  <table>
  <tr><td valign="top"><b>Columns:</b></td>
      <td>
        <input type="hidden" name="wdk_history_id" value="${history_id}"/>
        <input type="hidden" name="wdkReportFormat" value="${format}"/>
          <table>
          <c:set var="numPerLine" value="2"/>
          <c:set var="i" value="0"/>

          <tr><td colspan="${numPerLine}">
          <input type="checkbox" name="selectedFields" value="default" onclick="uncheck(1);" checked>
          Default (same as in <a href="showSummary.do?wdk_history_id=${history_id}">result</a>), or...
          </td></tr>
          <tr><td colspan="${numPerLine}">&nbsp;</td></tr>

          <tr>
          <c:forEach items="${wdkAnswer.allReportMakerAttributes}" var="rmAttr">
            <%-- this is a hack, why some reportMakerAttributes have no name? --%>
            <c:choose>
            <c:when test="${rmAttr.name != null && rmAttr.name != ''}">
            <c:set var="i" value="${i+1}"/>
            <c:set var="br" value=""/>
            <c:if test="${i % numPerLine == 0}"><c:set var="br" value="</tr><tr>"/></c:if>
            <td>
              <input type="checkbox" name="selectedFields" value="${rmAttr.name}" onclick="uncheck(0);">
                  <c:choose>
                    <c:when test="${rmAttr.displayName == null || rmAttr.displayName == ''}">
                      ${rmAttr.name}
                    </c:when>
                    <c:otherwise>
                      ${rmAttr.displayName}
                    </c:otherwise>
                  </c:choose>
                  <c:if test="${rmAttr.name == 'primaryKey'}">ID</c:if>
            </td>${br}
            </c:when>
            <c:otherwise>
              <!-- <td><html:multibox property="selectedFields">junk</html:multibox>junk</td>${br} -->
            </c:otherwise>
            </c:choose>
          </c:forEach>

          <c:if test="${i % numPerLine != 0 }">
              <c:set var="j" value="${i}"/>
              <c:forEach begin="${i+1}" end="${i+numPerLine}" step="1">
                  <c:set var="j" value="${j+1}"/>
                  <c:if test="${j % numPerLine != 0}"><td></td></c:if>
              </c:forEach>
              </tr>
          </c:if>
          </table>
        </td></tr>

  <tr><td valign="top">&nbsp;</td>
      <td align="center"><input type="button" value="select all" onclick="check(1)">
          <input type="button" value="clear all" selected="yes" onclick="check(0)">
        </td></tr>

  <tr><td valign="top"><b>Column names: </b></td>
      <td><input type="radio" name="includeHeader" value="yes" checked>include
          <input type="radio" name="includeHeader" value="no">exclude
        </td></tr>
  <tr><td colspan="2">&nbsp;</td></tr>
  <tr><td></td>
      <td><html:submit property="downloadConfigSubmit" value="Get Report"/>
      </td></tr></table>
</form>

  </c:otherwise>
</c:choose>

<site:footer/>