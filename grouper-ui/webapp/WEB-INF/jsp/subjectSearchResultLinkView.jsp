<%-- @annotation@
			Link to subject summary page
--%><%--
  @author Gary Brown.
  @version $Id: subjectSearchResultLinkView.jsp,v 1.2.4.1 2008-08-28 08:30:57 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute name="viewObject"/>
<c:set var="linkTitle"><fmt:message bundle="${nav}" key="browse.to.subject.summary">
		 		<fmt:param value="${viewObject.description}"/>
		</fmt:message></c:set>
<html:link page="/populateSubjectSummary.do" name="viewObject" title="${linkTitle}">
  <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject"/>
	  <tiles:put name="view" value="subjectSearchResult"/>
  </tiles:insert>
</html:link>