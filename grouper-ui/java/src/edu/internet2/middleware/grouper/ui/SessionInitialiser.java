/*
Copyright 2004-2008 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2008 The University Of Bristol

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

package edu.internet2.middleware.grouper.ui;

import java.util.*;

import javax.servlet.http.*;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ui.util.*;

/**
 * Initialises HttpSession after login. <p/>Should probably make an interface
 * and allow site specific initialisation
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: SessionInitialiser.java,v 1.16 2008-11-04 07:17:59 mchyzer Exp $
 */

public class SessionInitialiser {
	protected static Log LOG = LogFactory.getLog(SessionInitialiser.class);
	/**
	 * Sets locale and calls any module specific initialisation
	 * 
	 * @param request
	 *            current HttpServletRequest
	 * @throws Exception
	 */
	public static void init(HttpServletRequest request) throws Exception {

		String localeStr = request.getParameter("lang");
		HttpSession session = request.getSession();
		Locale locale = createLocale(localeStr);
		
		session.setAttribute("org.apache.struts.action.LOCALE", locale);

		org.apache.struts.config.ModuleConfig configx = (org.apache.struts.config.ModuleConfig) request
				.getAttribute("org.apache.struts.action.MODULE");
		String module = "";
		if (configx != null)
			module = configx.getPrefix();
		SessionInitialiser.init(module, locale.toString(), session);
		session.setAttribute("javax.servlet.jsp.jstl.fmt.locale", locale);

		//session.setAttribute("sessionInited", localeStr);

	}

	/**
	 * Module specific initialisation with no locale specified
	 * 
	 * @param module
	 *            Struts's module
	 * @param session
	 *            current HttpSession
	 * @throws Exception
	 */
	public static void init(String module, HttpSession session)
			throws Exception {
		init(module, null, session);
	}

	/**
	 * Module and locale specific initialisation
	 * 
	 * @param module
	 *            Strut's module
	 * @param locale
	 *            selected locale
	 * @param session
	 *            current HttpSession
	 * @throws Exception
	 */
	public static void init(String module, String locale, HttpSession session)
			throws Exception {
		if(Boolean.TRUE.equals(session.getAttribute("sessionInited"))) return;
		if (module != null)
			module = module.replaceAll("^/", "");
		ResourceBundle defaultInit = ResourceBundle
				.getBundle("/resources/init");
		if (module == null || module.equals("")) {
			module = defaultInit.getString("default.module");
		}
		ResourceBundle moduleInit = ResourceBundle.getBundle("/resources/"
				+ module + "/init");
		if (locale == null || locale.equals("")) {
			locale = moduleInit.getString("default.locale");
		}
		Locale localeObj = createLocale(locale);
		ResourceBundle grouperBundle = ResourceBundle.getBundle(
				"resources.grouper.nav", localeObj);
		ResourceBundle grouperMediaBundle = ResourceBundle.getBundle(
				"resources.grouper.media", localeObj);

		ChainedResourceBundle chainedBundle = null;
		ChainedResourceBundle chainedMediaBundle = null;

		if (module.equals("i2mi") || module.equals("grouper")) {
			chainedBundle = new ChainedResourceBundle(grouperBundle,
					"navResource");
			chainedMediaBundle = new ChainedResourceBundle(grouperMediaBundle,
					"mediaResource");
		} else {
			ResourceBundle moduleBundle = ResourceBundle.getBundle("resources."
					+ module + ".nav", localeObj);
			ResourceBundle moduleMediaBundle = ResourceBundle.getBundle(
					"resources." + module + ".media", localeObj);
			chainedBundle = new ChainedResourceBundle(moduleBundle,
					"navResource");
			chainedBundle.addBundle(grouperBundle);
			session.setAttribute("navExceptionHelper",new NavExceptionHelper(chainedBundle));
			chainedMediaBundle = new ChainedResourceBundle(moduleMediaBundle,
					"mediaResource");
			chainedMediaBundle.addBundle(grouperMediaBundle);
		}
		
		//add in properties from grouper.config
    MapBundleWrapper navBundleWrapperNull = new MapBundleWrapper(chainedBundle, true);

    addIncludeExcludeDefaults(chainedBundle, navBundleWrapperNull);
    
		session.setAttribute("nav",
				new javax.servlet.jsp.jstl.fmt.LocalizationContext(
						chainedBundle));
		session.setAttribute("navMap", new MapBundleWrapper(chainedBundle, false));
    session.setAttribute("navNullMap", navBundleWrapperNull);

		session.setAttribute("media",
				new javax.servlet.jsp.jstl.fmt.LocalizationContext(
						chainedMediaBundle));
		session.setAttribute("mediaMap", new MapBundleWrapper(
				chainedMediaBundle, false));
		//returns null if not there, not question marks
    session.setAttribute("mediaNullMap", new MapBundleWrapper(
        chainedMediaBundle, true));
		String pageSizes = chainedMediaBundle
				.getString("pager.pagesize.selection");

		String[] pageSizeSelections = pageSizes.split(" ");
		session.setAttribute("pageSizeSelections", pageSizeSelections);
		session.setAttribute("stemSeparator", GrouperHelper.HIER_DELIM);
		try {
			String initialStems = chainedMediaBundle
					.getString("plugin.initialstems");
			if (initialStems != null && !"".equals(initialStems))
				session.setAttribute("isQuickLinks", Boolean.TRUE);
		} catch (Exception e) {

		}
		GrouperSession s = getGrouperSession(session);
		//@TODO: should we split the personalStemRoot and create
		//any stems which are missing

		try {
			String personalStem = chainedMediaBundle
					.getString("plugin.personalstem");
			if (personalStem != null && !"".equals(personalStem)) {
				PersonalStem personalStemInstance = (PersonalStem) Class
						.forName(personalStem).newInstance();
				GrouperHelper.createIfAbsentPersonalStem(s,personalStemInstance);
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		initThread(session);
		if(getAuthUser(session)!=null) session.setAttribute("sessionInited", Boolean.TRUE);
		try{
			session.setAttribute("fieldList",GrouperHelper.getFieldsAsMap(chainedBundle));
		}catch(Exception e) {
			LOG.error("Error retrieving Fields: " + e.getMessage());
		}
		session.setAttribute("MembershipExporter",new MembershipExporter(chainedMediaBundle));
		session.setAttribute("MembershipImportManager",new MembershipImportManager(chainedMediaBundle,chainedBundle));
		Document doc = null;
		try {
			doc = DOMHelper.getDomFromResourceOnClassPath("resources/grouper/ui-permissions.xml");
		}catch(Exception e){
		  LOG.info("resources/grouper/ui-permissions.xml not found. Default permissions apply.");
		}
		if(doc==null) {
			doc=DOMHelper.newDocument();
		}
		if(s==null) return;
		UiPermissions uip = new UiPermissions(s,doc);
		session.setAttribute("uiPermissions", uip);
		Set menuFilters = new LinkedHashSet();
		session.setAttribute("menuFilters",menuFilters);
		String mFilters = null;
		try {
			mFilters=chainedMediaBundle.getString("menu.filters");
			String[] parts = mFilters.split(" ");
			Class claz;
			MenuFilter filter;
			for(int i=0;i<parts.length;i++) {
				try {
					claz=Class.forName(parts[i]);
					filter=(MenuFilter)claz.newInstance();
					menuFilters.add(filter);
				}catch(Exception e){
					LOG.error("Unable to add menu filter [" + parts[i] + "]. " + e.getMessage());
				}
			}
		}catch(MissingResourceException mre){
			LOG.info("No menu.filters set in media.properties");
		}
		
	}

  /**
   * add in exclude and include tooltips if not already in there (from grouper.properties)
   * @param chainedBundle
   * @param navBundleWrapperNull
   */
  private static void addIncludeExcludeDefaults(ChainedResourceBundle chainedBundle,
      MapBundleWrapper navBundleWrapperNull) {
    String typeName = "tooltipTargetted.groupTypes." + GrouperConfig.getProperty("grouperIncludeExclude.type.name");
    if (navBundleWrapperNull.get(typeName) == null) {
      chainedBundle.addToCache(typeName, GrouperConfig.getProperty("grouperIncludeExclude.tooltip"));
    }
    
    typeName = "tooltipTargetted.groupTypes." + GrouperConfig.getProperty("grouperIncludeExclude.requireGroups.type.name");
    if (navBundleWrapperNull.get(typeName) == null) {
      chainedBundle.addToCache(typeName, GrouperConfig.getProperty("grouperIncludeExclude.requireGroups.tooltip"));
    }
    
    //built in attribute
    typeName = "tooltipTargetted.groupFields." + GrouperConfig.getProperty("grouperIncludeExclude.requireGroups.attributeName");
    if (navBundleWrapperNull.get(typeName) == null) {
      chainedBundle.addToCache(typeName, GrouperConfig.getProperty("grouperIncludeExclude.requireGroups.tooltip"));
    }
    
    //loop through custom types and attributes
    int i=0;
    while(true) {
      
      //#grouperIncludeExclude.requireGroup.name.0 = requireActiveEmployee
      //#grouperIncludeExclude.requireGroup.attributeOrType.0 = type
      //#grouperIncludeExclude.requireGroup.description.0 = If value is true, members of the overall group must be an active employee (in the school:community:activeEmployee group).  Otherwise, leave this value not filled in.
      String name = GrouperConfig.getProperty("grouperIncludeExclude.requireGroup.name." + i);
      if (StringUtils.isBlank(name)) {
        break;
      }
      String attributeOrTypeName = "grouperIncludeExclude.requireGroup.attributeOrType." + i;
      String type = GrouperConfig.getProperty(attributeOrTypeName);
      boolean isAttribute = false;
      if (StringUtils.equalsIgnoreCase("attribute", type)) {
        isAttribute = true;
      } else if (!StringUtils.equalsIgnoreCase("type", type)) {
        throw new RuntimeException("Invalid type: '" + type + "' for grouper.properties entry: " + attributeOrTypeName);
      }
      String description = "grouperIncludeExclude.requireGroup.description." + i;
      
      String key = "tooltipTargetted.group" + (isAttribute ? "Field" : "Type") + "s." + name;
      if (navBundleWrapperNull.get(key) == null) {
        chainedBundle.addToCache(key, description);
      }
      i++;
    }
    
  }

	/**
	 * Proper way to get GrouperSession from HttpSession
	 * 
	 * @param session
	 *            current HttpSession
	 * @return the current GrouperSession
	 */
	public static GrouperSession getGrouperSession(HttpSession session) {

		GrouperSession s = (GrouperSession) session
				.getAttribute("edu.intenet2.middleware.grouper.ui.GrouperSession");
		return s;
	}
	
	/**
	 * Proper way to get UiPermissions from HttpSession
	 * 
	 * @param session
	 *            current HttpSession
	 * @return the current UiPermissions
	 */
	public static UiPermissions getUiPermissions(HttpSession session) {
		UiPermissions uip = (UiPermissions)session.getAttribute("uiPermissions");
		return uip;
	}
	
	/**
	 * Proper way to get MenuFilters from HttpSession
	 * 
	 * @param session
	 *            current HttpSession
	 * @return the current MenuFilters
	 */
	public static Set getMenuFilters(HttpSession session) {
		Set mf = (Set)session.getAttribute("menuFilters");
		return mf;
	}

	/**
	 * Proper way of getting the underlying HttpSession attribute value for the
	 * currently logged in user.
	 * 
	 * @param session
	 * @return the id of the currently authenticated user
	 */
	public static String getAuthUser(HttpSession session) {
		String authUser = (String) session.getAttribute("authUser");
		return authUser;
	}
	
	public static Locale createLocale(String localeStr) {
		if(localeStr==null || localeStr.equals("")) return Locale.getDefault();
		String[] parts = localeStr.split("_");
		Locale locale=null;
		switch (parts.length) {
		case 1:
			locale = new Locale(parts[0]);
			break;
		case 2:
			locale = new Locale(parts[0],parts[1]);
			break;
		case 3:
			locale = new Locale(parts[0],parts[1],parts[2]);
			break;

		default:
			throw new IllegalArgumentException("Wrong number of parts for locale: " + localeStr);
			
		}
		return locale;
	}
	
	public static void initThread(HttpSession session) {
		LocalizationContext lc = (LocalizationContext) session.getAttribute("media");
		
		if (lc == null) return;
		
		ResourceBundle mediaBundle = lc.getResourceBundle();
		UIThreadLocal.put("mediaBundle", mediaBundle);
	}

}