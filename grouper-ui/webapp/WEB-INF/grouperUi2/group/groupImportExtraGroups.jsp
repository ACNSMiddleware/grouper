<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <c:forEach items="${grouperRequestContainer.groupContainer.groupImportExtraGuiGroups}" 
                  var="guiGroup" varStatus="status">
                
                  <%-- c:if test="${status.count>1}" --%>
                  <br />

                  <input type="hidden" name="extraGroupId_${status.count-1}" value="${grouper:escapeHtml(guiGroup.group.id)}" />            
                  ${guiGroup.linkWithIcon} <a href="#" onclick="ajax('../app/UiV2Group.groupImportRemoveGroup?removeGroupId=${grouper:escapeUrl(guiGroup.group.id)}', {formIds: 'importGroupFormId'}); return false;"><i class="fa fa-times" style="color: #990000"></i></a>
                
                </c:forEach>
