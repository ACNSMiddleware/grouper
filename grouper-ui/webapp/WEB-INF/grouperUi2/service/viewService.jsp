<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.serviceContainer.guiService.guiAttributeDefName.attributeDefName.stem.id}" />

            <div class="bread-header-container">
              <%--
              <ul class="breadcrumb">
                <li><a href="index.html">Home </a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">Search Results</li>
              </ul>
              --%>
              ${grouperRequestContainer.serviceContainer.guiService.breadcrumbs}
              <div class="page-header blue-gradient">
                <div class="row-fluid">
                  <div class="span10">
                    <h4>${textContainer.text['viewServiceHeaderLabel'] }</h4>
                    <h1><i class="fa fa-external-link-square"></i> ${grouper:escapeHtml(grouperRequestContainer.serviceContainer.guiService.guiAttributeDefName.attributeDefName.displayExtension)}</h1>                    
                    <p>${grouper:escapeHtml(grouperRequestContainer.serviceContainer.guiService.guiAttributeDefName.attributeDefName.description)}</p>
                  </div>
                </div>
              </div>
            </div>
            <div class="row-fluid">
              <form id="viewServiceForm">
                <input type="hidden" name="idOfAttributeDefName"
                  value="${grouperRequestContainer.serviceContainer.guiService.guiAttributeDefName.attributeDefName.id}" />
              </form>
              <div class="span12">
                <div id="viewServiceResultsId">
                </div>
              </div>
            </div>


