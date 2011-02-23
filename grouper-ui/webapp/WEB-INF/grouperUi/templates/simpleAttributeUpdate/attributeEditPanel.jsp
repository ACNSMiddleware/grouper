<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeUpdate/attributeEditPanel.jsp -->

<%--Attribute edit --%>

<div class="section" style="min-width: 900px">

  <grouper:subtitle key="simpleAttributeUpdate.editSectionHeader" />

  <div class="sectionBody">
    <form id="attributeEditFormId" name="attributeEditFormName" onsubmit="return false;" >
    <table class="formTable formTableSpaced" cellspacing="2">
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="folder">
            <grouper:message key="simpleAttributeUpdate.folder" />
          </label>
          <c:if test="${attributeUpdateRequestContainer.create}">
            <sup class="requiredIndicator">*</sup>
          </c:if>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          
          <c:choose>
				    <c:when test="${attributeUpdateRequestContainer.create}">
              <div class="combohint"><grouper:message key="simpleAttributeUpdate.selectFolderCombohint"/></div>
		          <grouper:combobox filterOperation="SimpleAttributeUpdateFilter.filterCreatableNamespace" id="simpleAttributeUpdatePickNamespace" 
		            width="700"/>
				    </c:when>
				    <c:otherwise>
					    <grouper:groupBreadcrumb showLeafNode="false" 
					      showCurrentLocationLabel="false" 
            	  groupName="${attributeUpdateRequestContainer.attributeDefToEdit.name}"  />
				    </c:otherwise>
				  </c:choose>
        </td>
      </tr>
      <c:if test="${!attributeUpdateRequestContainer.create}">
	      <tr class="formTableRow">
	        <td class="formTableLeft">
            <grouper:message key="simpleAttributeUpdate.editId" />
	        </td>
	        <td class="formTableRight">
	          ${attributeUpdateRequestContainer.attributeDefToEdit.id }
	          <input type="hidden" name="attributeDefToEditId" 
	            value="${attributeUpdateRequestContainer.attributeDefToEdit.id }" />
	        </td>
	      </tr>
      </c:if>	      
       <tr class="formTableRow">
         <td class="formTableLeft">
           <c:if test="${attributeUpdateRequestContainer.create}">
             <label for="attributeDefToEditExtension">
           </c:if>
           <grouper:message key="simpleAttributeUpdate.extension" />
           <c:if test="${attributeUpdateRequestContainer.create}">
             </label>
             <sup class="requiredIndicator">*</sup>
           </c:if>
         </td>
         <td class="formTableRight">
           <c:choose>
             <c:when test="${attributeUpdateRequestContainer.create}">
	             <input type="text" name="attributeDefToEditExtension" 
	               value="${attributeUpdateRequestContainer.attributeDefToEdit.extension}" />
             </c:when>
             <c:otherwise>
               <%-- TODO escape html in whole file including this element after merging with escape fucntions in 2.7 --%>
               ${attributeUpdateRequestContainer.attributeDefToEdit.extension}
             </c:otherwise>
           </c:choose>
         </td>
       </tr>
       <tr class="formTableRow">
         <td class="formTableLeft">

           <c:choose>
             <c:when test="${attributeUpdateRequestContainer.create}">
		           <label for="attributeDefToEditType">
		             <grouper:message key="simpleAttributeUpdate.type" />
		           </label>
		           <sup class="requiredIndicator">*</sup>
             </c:when>
             <c:otherwise>
               <grouper:message key="simpleAttributeUpdate.type" />
             </c:otherwise>
           </c:choose>
           
         </td>
         <td class="formTableRight">
           <c:choose>
             <c:when test="${attributeUpdateRequestContainer.create}">
		           <select name="attributeDefToEditType">
                 <option value="" ></option>
		             <option value="attr" ${attributeUpdateRequestContainer.attributeDefToEdit.attributeDefTypeDb == 'attr' ? 'selected="selected"' : '' } ><grouper:message key="simpleAttributeUpdate.type.attr" /></option>
		             <option value="domain" ${attributeUpdateRequestContainer.attributeDefToEdit.attributeDefTypeDb == 'domain' ? 'selected="selected"' : '' }><grouper:message key="simpleAttributeUpdate.type.domain" /></option>
		             <option value="limit" ${attributeUpdateRequestContainer.attributeDefToEdit.attributeDefTypeDb == 'limit' ? 'selected="selected"' : '' }><grouper:message key="simpleAttributeUpdate.type.limit" /></option>
		             <option value="perm" ${attributeUpdateRequestContainer.attributeDefToEdit.attributeDefTypeDb == 'perm' ? 'selected="selected"' : '' }><grouper:message key="simpleAttributeUpdate.type.perm" /></option>
		             <option value="type" ${attributeUpdateRequestContainer.attributeDefToEdit.attributeDefTypeDb == 'type' ? 'selected="selected"' : '' }><grouper:message key="simpleAttributeUpdate.type.type" /></option>
		           </select>
             </c:when>
             <c:otherwise>
               <grouper:message key="simpleAttributeUpdate.type.${attributeUpdateRequestContainer.attributeDefToEdit.attributeDefTypeDb}" />
             </c:otherwise>
           </c:choose>
         </td>
       </tr>
       
       <tr class="formTableRow">
         <td class="formTableLeft">
           <label for="attributeDefToEditDescription">
             <grouper:message key="simpleAttributeUpdate.description" />
           </label>
         </td>
         <td class="formTableRight">
            <%-- TODO escape the description for HTML --%>
            <textarea name="attributeDefToEditDescription" rows="3" cols="40">${attributeUpdateRequestContainer.attributeDefToEdit.description}</textarea> 
         </td>
       </tr>
       <tr class="formTableRow">
         <td class="formTableLeft">
           <label for="attributeDefToEditMultiAssignable">
             <grouper:message key="simpleAttributeUpdate.multiAssignable" />
           </label>
         </td>
         <td class="formTableRight">
            <input type="checkbox" name="attributeDefToEditMultiAssignable" value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.multiAssignableDb == 'T' ? 'checked="checked"' : '' } />
         </td>
       </tr>

       <tr class="formTableRow">
         <td class="formTableLeft">

           <label for="attributeDefToEditValueType">
             <grouper:message key="simpleAttributeUpdate.valueType" />
           </label>
         </td>
         <td class="formTableRight">
           <select name="attributeDefToEditValueType">
             <option value="marker" ${attributeUpdateRequestContainer.attributeDefToEdit.valueTypeDb == 'marker' ? 'selected="selected"' : '' }><grouper:message key="simpleAttributeUpdate.valueType.marker" /></option>
             <option value="floating" ${attributeUpdateRequestContainer.attributeDefToEdit.valueTypeDb == 'floating' ? 'selected="selected"' : '' } ><grouper:message key="simpleAttributeUpdate.valueType.floating" /></option>
             <option value="integer" ${attributeUpdateRequestContainer.attributeDefToEdit.valueTypeDb == 'integer' ? 'selected="selected"' : '' }><grouper:message key="simpleAttributeUpdate.valueType.integer" /></option>
             <option value="memberId" ${attributeUpdateRequestContainer.attributeDefToEdit.valueTypeDb == 'memberId' ? 'selected="selected"' : '' }><grouper:message key="simpleAttributeUpdate.valueType.memberId" /></option>
             <option value="string" ${attributeUpdateRequestContainer.attributeDefToEdit.valueTypeDb == 'string' ? 'selected="selected"' : '' }><grouper:message key="simpleAttributeUpdate.valueType.string" /></option>
             <option value="timestamp" ${attributeUpdateRequestContainer.attributeDefToEdit.valueTypeDb == 'timestamp' ? 'selected="selected"' : '' }><grouper:message key="simpleAttributeUpdate.valueType.timestamp" /></option>

           </select>
         </td>
       </tr>

       <tr class="formTableRow">
         <td class="formTableLeft">
           <label for="attributeDefToEditMultiValued">
             <grouper:message key="simpleAttributeUpdate.multiValued" />
           </label>
         </td>
         <td class="formTableRight">
            <input type="checkbox" name="attributeDefToEditMultiValued" value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.multiValuedDb == 'T' ? 'checked="checked"' : '' } />
         </td>
       </tr>
       
       <tr class="formTableRow">
         <td class="formTableLeft">

           <grouper:message key="simpleAttributeUpdate.assignTo" />
           <sup class="requiredIndicator">*</sup>
           
         </td>
         <td class="formTableRight">
           <table cellpadding="0" cellspacing="0" border="0">
             <tr>
               <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                 <input type="checkbox" name="attributeDefToEditAssignToAttributeDef" value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToAttributeDefDb == 'T' ? 'checked="checked"' : '' } />
                 <grouper:message key="simpleAttributeUpdate.assignTo.attributeDef" />
               </td>
               <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                 <input type="checkbox" name="attributeDefToEditAssignToAttributeDefAssign" value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToAttributeDefAssnDb == 'T' ? 'checked="checked"' : '' } />
                 <grouper:message key="simpleAttributeUpdate.assignTo.attributeDefAssign" />
               </td>
             </tr>
             <tr>
               <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                 <input type="checkbox" name="attributeDefToEditAssignToStem" value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToStemDb == 'T' ? 'checked="checked"' : '' } />
                 <grouper:message key="simpleAttributeUpdate.assignTo.stem" />
               </td>
               <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                 <input type="checkbox" name="attributeDefToEditAssignToStemAssign" value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToStemAssnDb == 'T' ? 'checked="checked"' : '' } />
                 <grouper:message key="simpleAttributeUpdate.assignTo.stemAssign" />
               </td>
             </tr>
             <tr>
               <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                 <input type="checkbox" name="attributeDefToEditAssignToGroup" value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToGroupDb == 'T' ? 'checked="checked"' : '' } />
                 <grouper:message key="simpleAttributeUpdate.assignTo.group" />
               </td>
               <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                 <input type="checkbox" name="attributeDefToEditAssignToGroupAssign" value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToGroupAssnDb == 'T' ? 'checked="checked"' : '' } />
                 <grouper:message key="simpleAttributeUpdate.assignTo.groupAssign" />
               </td>
             </tr>
             <tr>
               <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                 <input type="checkbox" name="attributeDefToEditAssignToMember" value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToMemberDb == 'T' ? 'checked="checked"' : '' } />
                 <grouper:message key="simpleAttributeUpdate.assignTo.member" />
               </td>
               <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                 <input type="checkbox" name="attributeDefToEditAssignToMemberAssign" value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToMemberAssnDb == 'T' ? 'checked="checked"' : '' } />
                 <grouper:message key="simpleAttributeUpdate.assignTo.memberAssign" />
               </td>
             </tr>
             <tr>
               <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                 <input type="checkbox" name="attributeDefToEditAssignToMembership" value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToEffMembershipDb == 'T' ? 'checked="checked"' : '' } />
                 <grouper:message key="simpleAttributeUpdate.assignTo.membership" />
               </td>
               <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                 <input type="checkbox" name="attributeDefToEditAssignToMembershipAssign" value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToEffMembershipAssnDb == 'T' ? 'checked="checked"' : '' } />
                 <grouper:message key="simpleAttributeUpdate.assignTo.membershipAssign" />
               </td>
             </tr>
             <tr>
               <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                 <input type="checkbox" name="attributeDefToEditAssignToImmediateMembership" value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToImmMembershipDb == 'T' ? 'checked="checked"' : '' } />
                 <grouper:message key="simpleAttributeUpdate.assignTo.immediateMembership" />
               </td>
               <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                 <input type="checkbox" name="attributeDefToEditAssignToImmediateMembershipAssign" value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToImmMembershipAssnDb == 'T' ? 'checked="checked"' : '' } />
                 <grouper:message key="simpleAttributeUpdate.assignTo.immediateMembershipAssign" />
               </td>
             </tr>
           </table>
         </td>
       </tr>
       <tr>
       <td colspan="2">

         <input class="redButton" type="submit" 
          onclick="if (confirm('${grouper:message('simpleAttributeUpdate.editPanelDeleteConfirm', true, true) }')) {ajax('../app/SimpleAttributeUpdate.attributeEditPanelDelete', {formIds: 'attributeEditFormId'}); } return false;" 
          value="${simpleAttributeUpdateContainer.text.editPanelDelete}" style="margin-top: 2px" />
       
         <input class="redButton" type="submit" 
          onclick="window.location = 'grouper.html?operation=SimpleAttributeUpdate.createEdit'; return false;" 
          value="${simpleAttributeUpdateContainer.text.editPanelCancel}" style="margin-top: 2px" />
         
         <input class="blueButton" type="submit" 
          onclick="ajax('../app/SimpleAttributeUpdate.attributeEditPanelActions', {formIds: 'attributeEditFormId'}); return false;" 
          value="${simpleAttributeUpdateContainer.text.editPanelActions}" style="margin-top: 2px" />
       
         <input class="blueButton" type="submit" 
          onclick="ajax('../app/SimpleAttributeUpdate.attributeEditPanelSubmit', {formIds: 'attributeEditFormId'}); return false;" 
          value="${simpleAttributeUpdateContainer.text.editPanelSubmit}" style="margin-top: 2px" />
       
       </td>
       </tr>
    </table>
    </form>
    <br />
  </div>
</div>


<div id="attributeActionsPanel">
</div>


<!-- End: simpleAttributeUpdate/attributeEditPanel.jsp -->
