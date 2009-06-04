<%-- @annotation@
		  Form for saving debug preferences.
--%><%--
  @author Gary Brown.
  @version $Id: DebugPrefs.jsp,v 1.4 2008-04-10 19:50:25 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<div class="preferences">
<html:form styleId="DebugPrefsFormBean" action="/saveDebugPrefs">

<table border="0">
<tr>
    <td valign="top"><grouper:message bundle="${nav}" key="debug.prefs.edit.isActive"/></td>
    <td valign="top"><html:checkbox property="isActive" />
    </td>
</tr>
<tr>
    <td valign="top"><grouper:message bundle="${nav}" key="debug.prefs.edit.i2miDir"/></td>
    <td valign="top"><html:text property="i2miDir" size="75" />
    </td>
</tr>
<tr>
    <td valign="top"><grouper:message bundle="${nav}" key="debug.prefs.edit.siteDir"/></td>
    <td valign="top"><html:text property="siteDir" size="75" />
    </td>
</tr>
<tr>
    <td valign="top"><grouper:message bundle="${nav}" key="debug.prefs.edit.doShowResources"/></td>
    <td valign="top"><html:checkbox property="doShowResources" />
    </td>
</tr>
<tr>
    <td valign="top"><grouper:message bundle="${nav}" key="debug.prefs.edit.doShowResourcesInSitu"/></td>
    <td valign="top"><html:checkbox property="doShowResourcesInSitu" />
    </td>
</tr>
<tr>
    <td valign="top"><grouper:message bundle="${nav}" key="debug.prefs.edit.doShowTilesHistory"/></td>
    <td valign="top"><html:checkbox property="doShowTilesHistory" />
    </td>
</tr>
<tr>
    <td valign="top"><grouper:message bundle="${nav}" key="debug.prefs.edit.editor"/></td>
    <td valign="top"><html:text property="JSPEditor" size="75" />
    </td>
</tr>
<tr>
    <td valign="top"><grouper:message bundle="${nav}" key="debug.prefs.edit.doHideStyles"/></td>
    <td valign="top"><html:checkbox property="doHideStyles" />
    </td>
</tr>
</table>
 <html:submit styleClass="blueButton" property="submit.save" value="${navMap['debug.prefs.action.save']}"/>

</html:form>
</div>
