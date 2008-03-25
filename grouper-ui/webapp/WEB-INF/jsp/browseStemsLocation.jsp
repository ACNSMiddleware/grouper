<%-- @annotation@
		  Tile which shows parent nodes of the current node as links
		  which can be used to navigate to the parent node allowing 
		  the user to navigate the hierarchy
--%><%--
  @author Gary Brown.
  @version $Id: browseStemsLocation.jsp,v 1.5 2008-03-25 14:59:51 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div class="browseStemsLocation">
<c:choose>
	<c:when test="${! isFlat}">
<a href="<c:out value="${pageUrl}"/>#skipCurrentLocation" class="noCSSOnly"><grouper:message bundle="${nav}" key="page.skip.current-location"/><br/></a>
<strong><grouper:message bundle="${nav}" key="find.browse.here"/></strong>
<%-- CH 20080324 change spacing:  <br />&nbsp;&nbsp;&nbsp; --%>

<%
	int browsePathSize = ((List)request.getAttribute("browsePath")).size();
	pageContext.setAttribute("browsePathSize",new Integer(browsePathSize));
%>	
	<c:forEach var="stem" items="${browsePath}">
	<span class="browseStemsLocationPart">
				<html:link 
					page="/browseStems${browseMode}.do" 
					paramId="currentNode" 
					paramName="stem" 
					paramProperty="id"
					title="${navMap['browse.to.parent-stem']} ${stem.displayExtension}">
						<c:out value="${stem.displayExtension}"/><c:out value="${stemSeparator}"/>
				</html:link>

		</span>
	</c:forEach>
		<c:if test="${browseParent.isGroup}"><span class="browseStemsLocationHere">[</c:if><c:out value="${browseParent.displayExtension}"/><c:if test="${browseParent.isGroup}">]</span></c:if>
<a id="skipCurrentLocation" name="skipCurrentLocation"></a>
</c:when>
<c:otherwise>

</c:otherwise>
</c:choose>
</div>
</grouper:recordTile>