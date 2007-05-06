<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: assignment.jsp,v 1.8 2007-05-06 07:13:15 ddonn Exp $
  $Date: 2007-05-06 07:13:15 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
-->

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <meta name="robots" content="noindex, nofollow" />
    <title>
      <%=ResLoaderUI.getString("signet.title") %>
    </title>
    <link href="styles/signet.css" rel="stylesheet" type="text/css" />
    <script language="JavaScript" type="text/javascript" src="scripts/signet.js">
    </script>
  </head>
  <body onload="window.focus();">

<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Arrays" %>

<%@ page import="edu.internet2.middleware.subject.Subject" %>
<%@ page import="edu.internet2.middleware.signet.subjsrc.SignetSubject" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.signet.Category" %>
<%@ page import="edu.internet2.middleware.signet.Assignment" %>
<%@ page import="edu.internet2.middleware.signet.Function" %>
<%@ page import="edu.internet2.middleware.signet.tree.TreeNode" %>
<%@ page import="edu.internet2.middleware.signet.Signet" %>
<%@ page import="edu.internet2.middleware.signet.Limit" %>
<%@ page import="edu.internet2.middleware.signet.LimitValue" %>
<%@ page import="edu.internet2.middleware.signet.AssignmentHistory" %>

<%@ page import="edu.internet2.middleware.signet.resource.ResLoaderUI" %>

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>
<%@ page import="edu.internet2.middleware.signet.ui.Constants" %>
<%@ page import="edu.internet2.middleware.signet.ui.HistoryDateComparatorDescending" %>

<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
         
  Assignment currentAssignment
    = (Assignment)
         (request.getSession().getAttribute("currentAssignment"));
         
  Subject grantee
    = signet.getSubject
        (currentAssignment.getGrantee().getSourceId(),
         currentAssignment.getGrantee().getId());
//        (currentAssignment.getGrantee().getSubjectTypeId(),
//         currentAssignment.getGrantee().getSubjectId());
// not used
//  Subject grantor
//    = signet.getSubjectSources().getSubject
//      (currentAssignment.getGrantor().getSubjectTypeId(),
//       currentAssignment.getGrantor().getSubjectId());
       
// not used
//  Subject proxy = null;
//  if (currentAssignment.getProxy() != null)
//  {
//    proxy
//      = signet.getSubjectSources().getSubject
//          (currentAssignment.getProxy().getSubjectTypeId(),
//           currentAssignment.getProxy().getSubjectId());
//  }
         
  boolean canUse = currentAssignment.canUse();
  boolean canGrant = currentAssignment.canGrant();
         
  DateFormat dateFormat = DateFormat.getDateInstance();
%>

  <div id="summary">
    <div class="section">
      <h2><%=ResLoaderUI.getString("assignment.summary.hdr") %> </h2>
    </div>
      
    <table>
    
      <tr>
        <th class="label" scope="row">
          <%=ResLoaderUI.getString("assignment.grant.lbl") %>
        </th>
        <td>
          <%=grantee.getName()%>
        </td>
      </tr>
      <tr>
        <th class="label" scope="row">
          <%=ResLoaderUI.getString("assignment.type.lbl") %>
        </th>
        <td>
          <%=currentAssignment.getFunction().getSubsystem().getName()%>
        </td>
      </tr>
      <tr>
        <th class="label" scope="row">
          <%=ResLoaderUI.getString("assignment.privilege.lbl") %>
        </th>
        <td>
          <span class="category">
            <%=currentAssignment.getFunction().getCategory().getName()%>
          </span>
          : 
          <span class="function">
            <%=currentAssignment.getFunction().getName()%>
          </span>
          <br />
          <%=currentAssignment.getFunction().getHelpText()%>
        </td>
      </tr>
      <tr>
        <th class="label" scope="row">
          <%=ResLoaderUI.getString("assignment.scope.lbl") %>
        </th>
        <td>
          <%=signet.displayAncestry
                    (currentAssignment.getScope(),
                     " : ",  // childSeparatorPrefix
                     "",     // levelPrefix
                     "",     // levelSuffix
                     "")     // childSeparatorSuffix
                 %>
        </td>
      </tr>

<%
  Limit[] limits
    = Common.getLimitsInDisplayOrder
        (currentAssignment.getFunction().getLimits());
  LimitValue[] limitValues = Common.getLimitValuesInDisplayOrder(currentAssignment);
  for (int limitIndex = 0; limitIndex < limits.length; limitIndex++)
  {
    Limit limit = limits[limitIndex];
%>
      <tr>
        <th class="label" scope="row">
          <%=limit.getName()%>:
        </th>
        <td>
<%
  StringBuffer strBuf = new StringBuffer();
  int limitValuesPrinted = 0;
  for (int limitValueIndex = 0;
       limitValueIndex < limitValues.length;
       limitValueIndex++)
  {
    LimitValue limitValue = limitValues[limitValueIndex];
    if (limitValue.getLimit().equals(limit))
    {
      strBuf.append((limitValuesPrinted++ > 0) ? ", " : "");
      strBuf.append(limitValue.getDisplayValue());
    }
  }
%>
          <%=strBuf%>
        </td>
      </tr>
<%
  }
%>

   <!-- removed to history section 
      <tr>
        <th class="label" scope="row">
          Effective:
        </th>
        <td>
          <%//=dateFormat.format(currentAssignment.getEffectiveDate())%>
        </td>
      </tr>  -->
      <tr>
        <th class="label" scope="row"><%=ResLoaderUI.getString("assignment.first_effective.lbl") %> </th>
        <td class="data"><%=dateFormat.format(currentAssignment.getEffectiveDate())%> </td>
      </tr>
      <tr> 
        <th class="label" scope="row">
          <%=ResLoaderUI.getString("assignment.duration.lbl") %>
        </th>
        <td>
          <%=ResLoaderUI.getString("assignment.until.lbl") %>
          <%=currentAssignment.getExpirationDate() == null
             ? ResLoaderUI.getString("assignment.revoked.lbl")
             : dateFormat.format(currentAssignment.getExpirationDate())%>
        </td>
      </tr>

      <tr>
        <th class="label" scope="row">
          <%=ResLoaderUI.getString("assignment.status.lbl") %>
        </th>
        <td>
          <%=Common.displayStatusForDetailPopup(currentAssignment)%><br />
          	<%=canUse ? ResLoaderUI.getString("assignment.can_use.txt") : "" %>
          	<%=(canUse && canGrant) ? ", " : ""%>
          	<%=canGrant ? ResLoaderUI.getString("assignment.can_grant.txt") : ""%>
        </td>
      </tr>
  
    </table>
    
    <div class="section">
      <h2>
        <a name="history" id="history">
        </a>
        <%=ResLoaderUI.getString("assignment.history.lbl") %>
      </h2>
    </div>

<%
  Set historySet = currentAssignment.getHistory();
  AssignmentHistory[] historyArray = new AssignmentHistory[1];
  historyArray = (AssignmentHistory[])(historySet.toArray(historyArray));
  Arrays.sort(historyArray, new HistoryDateComparatorDescending());
%>
    <table>
<%    
  for (int i = 0; i < historyArray.length; i++)
  {
    AssignmentHistory historyRecord = historyArray[i];
%>
      <tr>
        <td>
          <%=Common.displayDatetime(Constants.DATETIME_FORMAT_12_MINUTE, historyRecord.getDate())%>
        </td>
        <td>
          <%=Common.describeChange(historyArray, i)%>
        </td>
      </tr>

<%
  }
%>

    </table>
  </div>
  </body>
</html>