<%@ include file="../common/commonTaglib.jsp" %>
<div id="topLeftLogo">
  <img src="../../${mediaMap['image.organisation-logo']}" id="topLeftLogoImage" />
</div>
<div id="topRightLogo">
  <img src="../../${mediaMap['image.grouper-logo']}" id="topRightLogoImage" />
</div>
<div id="navbar"> 
     <grouper:message key="simpleMembershipUpdate.screenWelcome"/> ${guiSettings.loggedInSubject.subject.description} 
     &nbsp; &nbsp; 
     <a href="#" onclick="if (confirm('${grouper:message('simpleMembershipUpdate.confirmLogout', true, true) }')) {location.href = 'grouper.html#operation=Misc.logout'; } return false;"
     ><img src="../public/assets/images/logout.gif" border="0" id="logoutImage" 
     alt="${grouper:message('simpleMembershipUpdate.logoutImageAlt', true, true) }" /></a>
     
     <a href="#" 
     onclick="if (confirm('${grouper:message('simpleMembershipUpdate.confirmLogout', true, true) }')) {location.href = 'grouper.html#operation=Misc.logout'; } return false;"><grouper:message key="simpleMembershipUpdate.logoutText"/></a>
</div>

