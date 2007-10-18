/*
Copyright 2004-2007 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2007 The University Of Bristol

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package edu.internet2.middleware.grouper.ui.actions;


import java.util.HashMap;
import java.util.HashSet;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.CompositeFinder;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.util.CollectionPager;


/**
 * Top level Strut's action which retrieves and makes available group members.  
 
 * <p/>
<table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">groupId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies group we want to 
      see members for</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">asMemberOf</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">if groupId and findForNode are 
      empty, asMemberOf identifies group</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">membershipListScope=all or 
        imm or eff</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates whether to show only 
      immediate or effective members, or both. If not set, set to imm</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">start</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used by CollectionPager</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">submit.addMembers</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates user really wants 
      to add members rather than display members</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">contextSubject</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates we got here from SubjectSummary</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">listField</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Custom list field we should 
      display 'members' for</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">submit.import</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates that user has clicked 
      'Import members' button</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">submit.export</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates that user has clicked 
      'Export members' button</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">selectedSource</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Filters members by source. Retrieved from session if not present</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;thisPageId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;Allows callerPageId to 
      be added to links/forms so this page can be returned to</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;isCompositeGroup</font></td>
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;Indicates whether group 
      is composite</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">browseParent</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map for stem of current stem</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">pager</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">CollectionPager instance</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">pagerParams</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map of params set on pager</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">groupPrivs</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map of privileges the Subject 
      identified by request parameters has for this group</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">membership</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map used by Strut's &lt;html:link&gt; 
      tags when generating parameters for &lt;a&gt; tags</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">contextSubject</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Makes request parameter available 
      to JSTL</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">listField</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Current list field in scope 
      (if any). Default is membership list</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">listFields</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">List of available list fields 
      for group - to enable user to change view</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">listFieldsSize</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Number of list fields available</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">canWriteField</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Can the current user add members 
      to the current list</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">removableMembers</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Can the current user remove 
      members from the current list. Only true if there are members to be removed 
      and immediate members are being viewed</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">exportMembers</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Collection of memberships to 
      be exported</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">sources</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map of source ids - display 
      names. If &gt;1 then let user filter</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">sourcesSize</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Number of sources represented 
      in result set</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">membershipListScope</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif"> SET if present as request parameter 
      or does not exist, otherwise READ to use as default</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subtitle=groups.action.show-members</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Key resolved in nav ResourceBundle 
      </font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">findForNode</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Use if groupId not set</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">selectedSource</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">See Request parameter of same name</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Strut's Action Parameter</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
</table>
 * 
 * @author Gary Brown.
 * @version $Id: PopulateGroupMembersAction.java,v 1.19 2007-10-18 08:40:06 isgwb Exp $
 */
public class PopulateGroupMembersAction extends GrouperCapableAction {

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_GroupMembers = "GroupMembers";
	static final private String FORWARD_AddGroupMembers = "AddGroupMembers"; 
	static final private String FORWARD_ExportMembers = "ExportMembers";
	static final private String FORWARD_ImportMembers = "ImportMembers";
	static final private String FORWARD_StemMembers = "StemMembers";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		
		if(!isEmpty(request.getParameter("submit.addMembers"))) return mapping.findForward(FORWARD_AddGroupMembers);
		if(!isEmpty(request.getParameter("submit.import"))) {
			return mapping.findForward(FORWARD_ImportMembers);
		}
		String selectedSource=null;
		session.setAttribute("subtitle","groups.action.show-members");
		String noResultsKey="groups.list-members.none";
		DynaActionForm groupForm = (DynaActionForm) form;
		if(isEmpty(request.getParameter("submit.export"))) {
			saveAsCallerPage(request,groupForm,"findForNode membershipListScope");
		}
		request.setAttribute("contextSubject",groupForm.get("contextSubject"));
		//Identify the group whose membership we are showing
		String groupId = (String)groupForm.get("groupId");
		
		//TODO: check following - shouldn't I always pass parameter
		if (groupId == null || groupId.length() == 0)
			groupId = (String) session.getAttribute("findForNode");
		if (groupId == null)
			groupId = request.getParameter("asMemberOf");
		Group group = null;
		String listField = request.getParameter("listField");
		String membershipField = "members";
		
		selectedSource = (String)groupForm.get("selectedSource");
		
		if(isEmpty(selectedSource)) {
			selectedSource=(String)session.getAttribute("selectedSource");
			groupForm.set("selectedSource",selectedSource);
		}else{
			session.setAttribute("selectedSource",selectedSource);
		}
		
		if(!isEmpty(listField)) membershipField=listField;
		Field mField = FieldFinder.find(membershipField);
		//Determine whether to show immediate, effective only, or all memberships
		String membershipListScope = (String) groupForm
				.get("membershipListScope");
		if (":all:imm:eff:".indexOf(":" + membershipListScope + ":") == -1) {
			membershipListScope = (String) session
					.getAttribute("membershipListScope");
		}
		if (membershipListScope == null)
			membershipListScope = "imm";
		session.setAttribute("membershipListScope", membershipListScope);
		groupForm.set("membershipListScope", membershipListScope);
		
	
		//Retrieve the membership according to scope selected by user
		group = GroupFinder.findByUuid(grouperSession, groupId);
		if(group.canWriteField(mField)) request.setAttribute("canWriteField",Boolean.TRUE);
		
		List listFields = GrouperHelper.getListFieldsForGroup(grouperSession,group);
		request.setAttribute("listFields",listFields);
		request.setAttribute("listFieldsSize",new Integer(listFields.size()));
		
		Set members = null;
		if ("imm".equals(membershipListScope)) {
			if(group.hasComposite()&& "members".equals(membershipField)) {
				members=new HashSet();
			}else{
				members = group.getImmediateMemberships(mField);
			}
			if("members".equals(membershipField)) {
				noResultsKey="groups.list-members.imm.none";
			}else{
				noResultsKey="groups.list-members.custom.imm.none";
			}
		} else if ("eff".equals(membershipListScope)) {
			if(group.hasComposite()&& membershipField.equals("members")) {
				members = group.getCompositeMemberships();
			}else{
				members = group.getEffectiveMemberships(mField);
			}
			if("members".equals(membershipField)) {
				noResultsKey="groups.list-members.eff.none";
			}else{
				noResultsKey="groups.list-members.custom.eff.none";
			}
		} else {
			members = group.getMemberships(mField);
			if("members".equals(membershipField)) {
				noResultsKey="groups.list-members.all.none";
			}else{
				noResultsKey="groups.list-members.custom.all.none";
			}
		}
		Map compMap = null;
		if(membershipField.equals("members")) {
			
			if(group.hasComposite()) {
				if(!"eff".equals(membershipListScope)) {
					Composite comp = CompositeFinder.findAsOwner(group);
					compMap = GrouperHelper.getCompositeMap(grouperSession,comp);
				}
				request.setAttribute("isCompositeGroup",Boolean.TRUE);
			}
			
		}
		
		Map countMap = new HashMap();
		Map sources = new HashMap();
		int membersFilterLimit=500;
		String mfl = getMediaResources(request).getString("members.filter.limit");
		try {
			membersFilterLimit = Integer.parseInt(mfl);
		}catch(Exception e){}
		
		List uniqueMemberships = GrouperHelper.getOneMembershipPerSubjectOrGroup(members,"group",countMap,sources,membersFilterLimit);
		uniqueMemberships=sort(uniqueMemberships,request,"members");
		Map.Entry entry = null;
		Iterator sIterator = sources.entrySet().iterator();
		String lookupKey=null;
		while(sIterator.hasNext()) {
			entry=(Map.Entry) sIterator.next();
			try {
				lookupKey="subject-source."+ entry.getKey()+".display-name";
				entry.setValue(getNavResources(request).getString(lookupKey));
			}catch(Exception e){}
		}
		if("_void_".equals(selectedSource)) selectedSource=null;
		if(!isEmpty(selectedSource) && sources.get(selectedSource)!=null && members.size()<membersFilterLimit) {
			Iterator it = uniqueMemberships.iterator();
			Membership mShip=null;
			while(it.hasNext()) {
				mShip=(Membership)it.next();
				if(!mShip.getMember().getSubjectSourceId().equals(selectedSource)) {
					it.remove();
				}
			}
		}
		request.setAttribute("sources",sources);
		request.setAttribute("sourcesSize", sources.size());
		request.setAttribute("browseParent", GrouperHelper.group2Map(
				grouperSession, group));
		if(!isEmpty(request.getParameter("submit.export"))) {
			request.setAttribute("exportMembers",uniqueMemberships);
			
			return mapping.findForward(FORWARD_ExportMembers);
		}
		
		
		//Set up CollectionPager for view
		String startStr = request.getParameter("start");
		if (startStr == null || "".equals(startStr))
			startStr = "0";

		int start = Integer.parseInt(startStr);
		int pageSize = getPageSize(session);
		int end = start + pageSize;
		if (end > uniqueMemberships.size())
			end = uniqueMemberships.size();
		
		List membershipMaps = GrouperHelper.memberships2Maps(
				grouperSession, uniqueMemberships.subList(start, end));
		int uniqueMembershipCount = uniqueMemberships.size();
		GrouperHelper.setMembershipCountPerSubjectOrGroup(membershipMaps,"group",countMap);
		if(compMap!=null) {
			membershipMaps.add(0,compMap);
			uniqueMembershipCount++;
		}
		CollectionPager pager = new CollectionPager(membershipMaps,uniqueMembershipCount,
				null, start, null, pageSize);
		pager.setParam("groupId", groupId);
		pager.setTarget(mapping.getPath());
		if(!isEmpty(listField))pager.setParam("listField", listField);
		request.setAttribute("pager", pager);
		request.setAttribute("listField",listField);
		request.setAttribute("linkParams", pager.getParams().clone());
		Map membership = new HashMap();
		
		membership.put("groupId", groupId);
		membership.put("callerPageId",request.getAttribute("thisPageId"));
		if(!isEmpty(listField)) membership.put("listField",listField);
		//TODO: some of this looks familar  - look at refactoring
		Map privs = GrouperHelper.hasAsMap(grouperSession, GroupOrStem.findByGroup(grouperSession, group));
		request.setAttribute("groupPrivs", privs);

		
		request.setAttribute("groupMembership", membership);
		request.setAttribute("noResultsKey", noResultsKey);
		request.setAttribute("removableMembers",new Boolean("imm".equals(membershipListScope) && group.canWriteField(mField) && !group.isComposite() && members.size()>0));
		
		return mapping.findForward(FORWARD_GroupMembers);

	}

}