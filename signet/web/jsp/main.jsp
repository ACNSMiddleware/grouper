<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: main.jsp,v 1.8 2006-10-25 00:13:31 ddonn Exp $
  $Date: 2006-10-25 00:13:31 $
  
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
  <script language="JavaScript" type="text/javascript" src="scripts/signet.js"></script>
</head>

<body onload="javascript:initControls();">

  <script type="text/javascript">


	function confirmRevokeMsg()
	{
		var ResLoaderUI = ResLoaderUI;
		var s_msg = ResLoaderUI.getString("privilegesGrantedReport.warning.txt");
var System = java.lang.System;
System.err.println("javascript.confirmRevokeMsg: msg = " + s_msg)
		var torf = confirm(s_msg);
if (true == torf) {
 System.err.println("confirmRevokeMsg returned true");
} else {
 System.err.println("confirmRevokeMsg returned false");
}
//		return torf;
return false;
	}

    function initControls()
    {
var System = java.lang.System;
System.err.println("RUNNING INITCONTROLS");
      initRevokeAllCheckbox();
      setStartButtonStatus();
    }
    
    function setStartButtonStatus()
    {
      // The first entry in the list of grantable Subsystems is always
      // a place-holder that reads "select a privilege type...", to prompt the
      // user to go ahead and pick a Subsystem.
      //
      // As long as that first entry is selected, the "start" button, which
      // initiates the privilege-granting process, should be dimmed.
      
      var theGrantableSubsystemList = document.grantForm.grantableSubsystems;
      var theGrantButton = document.grantForm.grantButton;
      
      if (theGrantableSubsystemList.selectedIndex < 1)
      {
        theGrantButton.disabled = true;
      }
      else
      {
        theGrantButton.disabled = false;
      }
    }
    
	function setShowButtonStatus()
    {
      var theDisplayList = document.personViewForm.privDisplayType;
      var theShowButton = document.personViewForm.showButton;
      
      if (theDisplayList.selectedIndex < 1)
      {
        theShowButton.disabled = true;
      }
      else
      {
        theShowButton.disabled = false;
      }
    }

	
    function initRevokeAllCheckbox()
    {
      // If there are no revocable Assignments on this page, then it makes
      // no sense to have the "revoke all" checkbox enabled.
      var theCheckAllBox = document.checkform.checkAll;
      
      if (selectableCount() > 0)
      {
        theCheckAllBox.disabled = false;
      }
      else
      {
        theCheckAllBox.disabled = true;
      }
    }
    
    function selectThis(isChecked)
    {
      var theCheckAllBox = document.checkform.checkAll;
      if (!isChecked)
      {
        theCheckAllBox.checked = false;
      }
      
      if (selectCount() > 0)
      {
        document.checkform.revokeButton.disabled = false;
      }
      else
      {
        document.checkform.revokeButton.disabled = true;
      }
    }
    
    function selectAll(isChecked)
    {
      var theForm = document.checkform;

      for (var i = 0; i < theForm.elements.length; i++)
      {
        if (theForm.elements[i].name != 'checkAll'
            && theForm.elements[i].type == 'checkbox'
            && theForm.elements[i].disabled == false)
        {
          theForm.elements[i].checked = isChecked;
        }
      }
      
      if (selectCount() > 0)
      {
        document.checkform.revokeButton.disabled = false;
      }
      else
      {
        document.checkform.revokeButton.disabled = true;
      }
    }
    
    function selectCount()
    {
      var theForm = document.checkform;
      var count = 0;
      
      for (var i = 0; i < theForm.elements.length; i++)
      {
        if ((theForm.elements[i].name != 'checkAll')
            && (theForm.elements[i].type == 'checkbox')
            && (theForm.elements[i].checked == true))
        {
          count++;
        }
      }
      
      return count;
    }
    
    function selectableCount()
    {
      var theForm = document.checkform;
      var count = 0;
      
      for (var i = 0; i < theForm.elements.length; i++)
      {
        if ((theForm.elements[i].name != 'checkAll')
            && (theForm.elements[i].type == 'checkbox')
            && (theForm.elements[i].disabled == false))
        {
          count++;
        }
      }
      
      return count;
    }
    
    function actAsChange(selectElementID, currentlyActingAs)
    {
      selectElement = document.getElementById(selectElementID);
      selectValue = selectElement.value;
      
      if (selectValue == currentlyActingAs)
      {
        document.getElementById('<%=Constants.ACTAS_BUTTON_ID%>').disabled=true;
      }
      else
      {
        document.getElementById('<%=Constants.ACTAS_BUTTON_ID%>').disabled=false;
      }
    }    
  </script>


<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.SortedSet" %>
<%@ page import="java.util.TreeSet" %>

<%@ page import="edu.internet2.middleware.signet.Signet" %>
<%@ page import="edu.internet2.middleware.signet.subjsrc.SignetSubject" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.signet.Function" %>
<%@ page import="edu.internet2.middleware.signet.Category" %>
<%@ page import="edu.internet2.middleware.signet.Assignment" %>
<%@ page import="edu.internet2.middleware.signet.Status" %>
<%@ page import="edu.internet2.middleware.signet.Proxy" %>

<%@ page import="edu.internet2.middleware.signet.resource.ResLoaderUI" %>

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>
<%@ page import="edu.internet2.middleware.signet.ui.Constants" %>

<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
         
   SignetSubject loggedInPrivilegedSubject
     = (SignetSubject)
         (request.getSession().getAttribute(Constants.LOGGEDINUSER_ATTRNAME));
         
   SignetSubject currentPSubject
     = (SignetSubject)
         (request.getSession().getAttribute(Constants.CURRENTPSUBJECT_ATTRNAME));
         
   DateFormat dateFormat = DateFormat.getDateInstance();
   
   boolean displayedFirstDesignationPrompt = false;
%>

    <tiles:insert page="/tiles/header.jsp" flush="true" />
    <div id="Navbar">
      <span class="logout">
        <%=Common.displayLogoutHref(request)%>
      </span> <!-- logout -->

      <span class="select">
<%
  if (currentPSubject.equals(loggedInPrivilegedSubject.getEffectiveEditor()))
  { 
%>
        <%=Common.homepageName(loggedInPrivilegedSubject)%>
<%
  }
  else
  {
%>
      <a href="Start.do?<%=Constants.CURRENTPSUBJECT_HTTPPARAMNAME%>=<%=Common.buildCompoundId(loggedInPrivilegedSubject.getEffectiveEditor())%>">
        <%=Common.homepageName(loggedInPrivilegedSubject)%>
      </a>
      <%=ResLoaderUI.getString("main.hdr.subjview.txt") %>[<%=currentPSubject.getName()%>]
<%
  }
%>
      </span> <!-- select -->

    </div> <!-- Navbar -->
  
  <div id="Layout">
  
    <tiles:insert page="/tiles/privilegesGrantedReport.jsp" flush="true" >
      <tiles:put name="signet"                    beanName="signet" />
      <tiles:put name="loggedInPrivilegedSubject" beanName="loggedInPrivilegedSubject" />
      <tiles:put name="pSubject"                  beanName="currentPrivilegedSubject" />
      <tiles:put name="privDisplayType"           beanName="privDisplayTypeAttr" />
      <tiles:put name="currentSubsystem"          beanName="currentSubsystemAttr" />
    </tiles:insert>
    
    <tiles:insert page="/tiles/footer.jsp" flush="true" />
    <div id="Sidebar">
    
        
<% 
  if (!(currentPSubject.equals(loggedInPrivilegedSubject.getEffectiveEditor())))
  {
    Set grantableSubsystems
      = loggedInPrivilegedSubject.getGrantableSubsystemsForAssignment();
%>


        <form name="grantForm" id="grantForm" action="Functions.do">
      <div class="grant" >
        <h2>
        	<%=ResLoaderUI.getString("main.grant.h2.lb")%> <!-- Grant a privilege  -->
        </h2>
        <p>to <%=currentPSubject.getName()%>:<br />
            <select
              id="grantableSubsystems"
              name="grantableSubsystems"
              class="long" 
              onchange="setStartButtonStatus()">
                
              <option value="prompt">
                <%=ResLoaderUI.getString("main.grant.prompt.cb")%> <!-- select a privilege type...  -->
              </option>

<%
    Iterator grantableSubsystemsIterator = grantableSubsystems.iterator();
    while (grantableSubsystemsIterator.hasNext())
    {
      Subsystem subsystem = (Subsystem)(grantableSubsystemsIterator.next());
%>
              <option value="<%=subsystem.getId()%>">
                <%=subsystem.getName()%>
              </option>
<%
    }
%>
            </select>
       
            <input 
              type="submit"
              name="grantButton"
              id="grantButton"
              class="button1"
              disabled="disabled"
              value=<%=ResLoaderUI.getString("main.grant.bt")%> />
            </p>
            <p class="dropback"><label for="grantableSubsystems">
            <%=ResLoaderUI.getString("main.grant.txt")%>
            </label>
            </p>
          
        </form> <!-- grantForm -->
      </div> <!-- grant -->
      
      <div class="actionbox"> 
        <h2>
          <%=ResLoaderUI.getString("main.designate.h2.lb")%>
        </h2>
<%
  if (!Common.isSystemAdministrator(signet, loggedInPrivilegedSubject))
  {
    displayedFirstDesignationPrompt = true;
%>
        <!-- Display this if the logged-in user is anyone other than a
             System Administrator.
         -->
        <p>
          <a href="Designate.do?<%=Constants.NEW_PROXY_HTTPPARAMNAME%>=true">
            <img src="images/arrow_right.gif" />
            <%=ResLoaderUI.getString("main.designate_1.href") %>
            <%=currentPSubject.getName()%></a>	<!-- keep closing 'a' tag on same line -->
            <%=ResLoaderUI.getString("main.designate_2.href") %>
            <%=loggedInPrivilegedSubject.getEffectiveEditor().getName()%>       
        </p>
<%
  }
  else
  {
    if (displayedFirstDesignationPrompt)
    {
%>
        <p>
          <%=ResLoaderUI.getString("main.designate_3.href") %>
        </p>
<%
    }
%>
	    <!-- Display this if the logged-in user is a System Administrator.
	         That's a user who has a non-Subsystem-specific Proxy from the
	         Signet subject, and is currently "acting as" the Signet subject.
	         It's not good enough to be some other user who's "acting as" the
	         System Administrator.
	     -->
        <p>
          <a href="Designate.do?<%=Constants.SUBSYSTEM_OWNER_HTTPPARAMNAME%>=true&<%=Constants.NEW_PROXY_HTTPPARAMNAME%>=true">
            <img src="images/arrow_right.gif" />
            <%=ResLoaderUI.getString("main.designate_4.href") %>
			<%=currentPSubject.getName()%></a>	<!-- keep closing 'a' tag on same line -->
            <%=ResLoaderUI.getString("main.designate_5.href") %>
			
        </p>
<%
  }
%>
      </div> <!-- actionbox -->
	  <br />
          
<%
  }
%>

    <form
      name="personSearchForm"
      method="post"
      action="" 
      onsubmit ="return checkForCursorInPersonSearch('personQuickSearch.jsp', 'words', 'PersonSearchResults')">

      <div class="findperson">
        <h2>
          <%=ResLoaderUI.getString("main.findperson.h2.lb")%>
        </h2> 
		<p>
        <input
          name="words"
          type="text"
          id="words"
          maxlength="500"
          onfocus="personSearchFieldHasFocus=true;"
          onblur="personSearchFieldHasFocus=false;" />
        <input
          name="searchbutton"
          type="submit"
          class="button1"
          value=<%=ResLoaderUI.getString("main.findperson.bt")%>
          onclick="personSearchButtonHasFocus=true;"
          onfocus="personSearchButtonHasFocus=true;"
          onblur="personSearchButtonHasFocus=false;" />
        </p>
		<p class="dropback">
          <label for="words"><%=ResLoaderUI.getString("main.findperson.txt")%></label>
        </p>
        <div id="PersonSearchResults" style="display:none">
        </div> <!-- PersonSearchResults -->
      </div><!-- findperson -->    
    </form> <!-- personSearchForm -->   

      
<% 
  if (Common.hasUsableProxies(loggedInPrivilegedSubject)
      || Common.hasExtensibleProxies(loggedInPrivilegedSubject))
  {
%>
    <div class="actionbox"> 
      <form
        name="actAsForm"
        method="post"
        action="ActAs.do">
        <h2><%=ResLoaderUI.getString("main.actas.h2.lb")%></h2>
        <p>
          <%=Common.displayActingForOptions
               (signet,
                loggedInPrivilegedSubject,
                Constants.ACTING_FOR_SELECT_ID,
                "actAsChange")%>
        </p>

      </form>
    </div> <!-- actionbox -->        
<%
  }
%>


  </div> <!-- Sidebar -->
</div>  <!-- Layout -->
</body>
</html>
