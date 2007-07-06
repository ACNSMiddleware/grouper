/*--
$Id: Constants.java,v 1.28 2007-07-06 21:59:20 ddonn Exp $
$Date: 2007-07-06 21:59:20 $

Copyright 2006 Internet2, Stanford University

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
package edu.internet2.middleware.signet.ui;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.signet.Category;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.Permission;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.tree.Tree;

public final class Constants
{
  /**
   * The package name for this application.
   */
  public static final String PACKAGE = "edu.internet2.middleware.signet.ui";

  /**
   * The token representing "failure" for this application.
   */
  public static final String FAILURE = "failure";

  /**
   * The token representing "success" for this application.
   */
  public static final String SUCCESS = "success";

  /**
   * The token representing the discovery of data-entry errors for this
   * application.
   */
  public static final String DATA_ENTRY_ERRORS = "dataEntryErrors";
  
  /**
   * This token exists only for Signet demo installations.
   * In the case of a normal production system, user authentication would
   * occur before any Signet action is accessed.
   */
  public static final String DEMO_LOGIN = "demoLogin";

  /**
   * The application scope attribute under which our user database
   * is stored.
   */
  public static final String DATABASE_KEY = "database";

  /**
   * The session scope attribute under which the Subscription object
   * currently selected by our logged-in User is stored.
   */
  public static final String SUBSCRIPTION_KEY = "subscription";

  /**
   * The session scope attribute under which the User object
   * for the currently logged in user is stored.
   */
  public static final String USER_KEY = "user";

  /**
   * A static message in case database resource is not loaded.
   */
  public static final String ERROR_DATABASE_NOT_LOADED =
      "ERROR:  User database not loaded -- check servlet container logs for error messages.";

  /**
   * A static message in case message resource is not loaded.
   */
  public static final String ERROR_MESSAGES_NOT_LOADED =
      "ERROR:  Message resources not loaded -- check servlet container logs for error messages.";

  /**
   * The request attributes key under the WelcomeAction stores an ArrayList
   * of error messages, if required resources are missing.
   */
  public static final String ERROR_KEY = "ERROR";

  public static final String SIGNET_ATTRNAME = "signet";

  public static final String EFFECTIVE_DATE_PREFIX = "effectiveDate";
  public static final String EXPIRATION_DATE_PREFIX = "expirationDate";
  
  public static final String ASSIGNMENT_HTTPPARAMPREFIX = "assignment:";
  public static final String PROXY_HTTPPARAMPREFIX = "proxy:";

  public static final String SUBSYSTEM_HTTPPARAMNAME = "subsystem";
  public static final String SUBSYSTEM_PROMPTVALUE = "__subsystem_prompt_value";
  public static final String SUBSYSTEM_ATTRNAME = "currentSubsystemAttr";
  
  public static final String LOGGEDINUSER_ATTRNAME = "loggedInPrivilegedSubject";
  
  public static final String CURRENTPSUBJECT_HTTPPARAMNAME = "currentPrivilegedSubject";
  public static final String CURRENTPSUBJECT_ATTRNAME = "currentPrivilegedSubject";
  
  public static final String PROXYID_HTTPPARAMNAME = "proxyId";
  public static final String PROXY_ATTRNAME = "currentProxy";
  public static final String DUP_PROXIES_ATTRNAME = "duplicateProxies";

  public static final String ASSIGNMENT_HTTPPARAMNAME = "assignmentId";
  public static final String ASSIGNMENT_ATTRNAME = "currentAssignment";
  public static final String DUP_ASSIGNMENTS_ATTRNAME = "duplicateAssignments";

  public static final String LIMITVALUE_ATTRNAME = "currentLimitValues";

  public static final String SCOPE_HTTPPARAMNAME = "scope";
  public static final String SCOPE_ATTRNAME = "currentScope";

  public static final String FUNCTION_ATTRNAME = "currentFunction";

  public static final String CATEGORY_ATTRNAME = "currentCategory";

  public static final String PRIVDISPLAYTYPE_ATTRNAME = "privDisplayTypeAttr";
  public static final String PRIVDISPLAYTYPE_HTTPPARAMNAME="privDisplayType";
  
  public static final String NEW_PROXY_HTTPPARAMNAME = "newProxy";
  
  public static final String SUBSYSTEM_OWNER_HTTPPARAMNAME = "isSubsystemOwner";
  public static final String SUBSYSTEM_OWNER_ATTRNAME = "isSubsystemOwner";
  
  public static final String SUBJECT_SELECTLIST_ID = "subjectSelectList";
  public static final String ACTING_FOR_SELECT_ID = "selectActingFor";
  
  public static final String COMPOSITE_ID_DELIMITER = ":";
  
  public static final String ACTAS_BUTTON_NAME = "actAsButton";
  public static final String ACTAS_BUTTON_ID = "actAsButton";
  
  public static final String DATETIME_FORMAT_24_SECOND = "dd-MMM-yyyy kk:mm:ss";
  public static final String DATETIME_FORMAT_12_MINUTE = "MMM d, yyyy hh:mmaa";
  public static final String DATETIME_FORMAT_24_DAY = "MMM d, yyyy";
  
  public static final String DEMO_USERNAME_HTTPPARAMNAME = "username";
  public static final String DEMO_PASSWORD_HTTPPARAMNAME = "password";
  public static final String DEMO_PASSWORD = "signet";

  public static final String SIGNET_SOURCE_ID_HTTPPARAMNAME = "SignetSourceId_param";
  public static final String SIGNET_SUBJECT_ID_HTTPPARAMNAME = "SignetSubjectId_param";

  public static final String CAN_USE_HTTPPARAMNAME = "can_use";

  public static final String CAN_GRANT_HTTPPARAMNAME = "can_grant";


  public static final Subsystem WILDCARD_SUBSYSTEM
    = new Subsystem()
      {
        public String getId() {return null;}
        public Set getCategories() {return null;}
        public Category getCategory(String categoryId){return null;}
        public Set getFunctions() {return null;}
//        public void setFunctionsArray(Function[] categories) {}
//        public Function getFunction(String functionId){return null;}
        public Tree getTree() {return null;}
        public void setTree(Tree tree) {}
        public void add(Category category) {}
        public void add(Limit limit) {}
        public Set getChoiceSets() {return null;}
        public ChoiceSet getChoiceSet(String id){return null;}
        public Map getLimits() {return null;}
        public Limit getLimit(String id){return null;}
        public Map getPermissions() {return null;}
        public Permission getPermission(String id){return null;}
//        public void setHelpText(String helpText) {}
        public String getHelpText() {return null;}
        public void inactivate() {}
        public Status getStatus() {return null;}
        public Date getCreateDatetime() {return null;}
        public String getName() {return null;}
        public int compareTo(Object o) {return 0;}
  };
}
