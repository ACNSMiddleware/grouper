/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouperClient.util;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl.Expression;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl.ExpressionFactory;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl.JexlContext;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl.JexlHelper;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.impl.Jdk14Logger;

/**
 * utility methods specific to grouper client
 */
public class GrouperClientUtils extends GrouperClientCommonUtils {

  /**
   * configure jdk14 logs once
   */
  private static boolean configuredLogs = false;

  /**
   * @param theClass
   * @return the log
   */
  public static Log retrieveLog(Class<?> theClass) {

    Log theLog = LogFactory.getLog(theClass);
    
    //if this isnt here, dont configure yet
    if (isBlank(GrouperClientConfig.retrieveConfig().propertyValueString("encrypt.disableExternalFileLookup"))
        || theClass.equals(GrouperClientCommonUtils.class)) {
      return new GrouperClientLog(theLog);
    }
    
    if (!configuredLogs) {
      String logLevel = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.logging.logLevel");
      String logFile = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.logging.logFile");
      String grouperClientLogLevel = GrouperClientConfig.retrieveConfig().propertyValueString(
          "grouperClient.logging.grouperClientOnly.logLevel");
      
      boolean hasLogLevel = !isBlank(logLevel);
      boolean hasLogFile = !isBlank(logFile);
      boolean hasGrouperClientLogLevel = !isBlank(grouperClientLogLevel);
      
      if (hasLogLevel || hasLogFile) {
        if (theLog instanceof Jdk14Logger) {
          Jdk14Logger jdkLogger = (Jdk14Logger) theLog;
          Logger logger = jdkLogger.getLogger();
          long timeToLive = 60;
          while (logger.getParent() != null && timeToLive-- > 0) {
            //this should be root logger
            logger = logger.getParent();
          }
  
          if (length(logger.getHandlers()) == 1) {
  
            //remove console appender if only one
            if (logger.getHandlers()[0].getClass() == ConsoleHandler.class) {
              logger.removeHandler(logger.getHandlers()[0]);
            }
          }
  
          if (length(logger.getHandlers()) == 0) {
            Handler handler = null;
            if (hasLogFile) {
              try {
                handler = new FileHandler(logFile, true);
              } catch (IOException ioe) {
                throw new RuntimeException(ioe);
              }
            } else {
              handler = new ConsoleHandler();
            }
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            logger.addHandler(handler);

            logger.setUseParentHandlers(false);
          }
          
          if (hasLogLevel) {
            Level level = Level.parse(logLevel);
            
            logger.setLevel(level);

          }
        }
      }
      
      if (hasGrouperClientLogLevel) {
        Level level = Level.parse(grouperClientLogLevel);
        Log grouperClientLog = LogFactory.getLog("edu.internet2.middleware.grouperClient");
        if (grouperClientLog instanceof Jdk14Logger) {
          Jdk14Logger jdkLogger = (Jdk14Logger) grouperClientLog;
          Logger logger = jdkLogger.getLogger();
          logger.setLevel(level);
        }
      }
      
      configuredLogs = true;
    }
    
    return new GrouperClientLog(theLog);
    
  }
  
  /**
   * override map for properties for testing
   * @return the override map
   * @deprecated use GrouperClientConfig.retrieveConfig().propertiesOverrideMap() instead
   */
  @Deprecated
  public static Map<String, String> grouperClientOverrideMap() {
    return GrouperClientConfig.retrieveConfig().propertiesOverrideMap();
  }
  
  /**
   * grouper client properties
   * @return the properties
   * @deprecated use GrouperClientConfig.retrieveConfig().properties() instead
   */
  @Deprecated
  public static Properties grouperClientProperties() {
    return GrouperClientConfig.retrieveConfig().properties();
  }

  /**
   * get a property and validate required from grouper.client.properties
   * @param key 
   * @param required 
   * @return the value
   * @deprecated use GrouperClientConfig.retrieveConfig().propertyValueString instead
   */
  @Deprecated
  public static String propertiesValue(String key, boolean required) {
    if (required) {
      return GrouperClientConfig.retrieveConfig().propertyValueStringRequired(key);
    }
    return GrouperClientConfig.retrieveConfig().propertyValueString(key);
  }


  /**
   * get a boolean and validate from grouper.client.properties
   * @param key
   * @param defaultValue
   * @param required
   * @return the string
   * @deprecated use GrouperClientConfig.retrieveConfig().propertyValueBoolean instead
   */
  @Deprecated
  public static boolean propertiesValueBoolean(String key, boolean defaultValue, boolean required ) {
    if (required) {
      return GrouperClientConfig.retrieveConfig().propertyValueBooleanRequired(key);
    }
    return GrouperClientConfig.retrieveConfig().propertyValueBoolean(key, defaultValue);
  }

  /**
   * get a boolean and validate from grouper.client.properties
   * @param key
   * @param defaultValue
   * @param required
   * @return the string
   * @deprecated GrouperClientConfig.retrieveConfig().propertyValueInt
   */
  @Deprecated
  public static int propertiesValueInt(String key, int defaultValue, boolean required ) {
    if (required) {
      return GrouperClientConfig.retrieveConfig().propertyValueIntRequired(key);
    }
    return GrouperClientConfig.retrieveConfig().propertyValueInt(key, defaultValue);
  }

  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GrouperClientUtils.class);

  /**
   * substitute an EL for objects
   * @param stringToParse
   * @param variableMap
   * @return the string
   */
  @SuppressWarnings("unchecked")
  public static String substituteExpressionLanguage(String stringToParse, Map<String, Object> variableMap) {
    if (isBlank(stringToParse)) {
      return stringToParse;
    }
    try {
      JexlContext jc = JexlHelper.createContext();

      int index = 0;
      
      for (String key: variableMap.keySet()) {
        jc.getVars().put(key, variableMap.get(key));
      }
      
      // matching ${ exp }   (non-greedy)
      Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
      Matcher matcher = pattern.matcher(stringToParse);
      
      StringBuilder result = new StringBuilder();

      //loop through and find each script
      while(matcher.find()) {
        result.append(stringToParse.substring(index, matcher.start()));
        
        //here is the script inside the curlies
        String script = matcher.group(1);
        
        Expression e = ExpressionFactory.createExpression(script);

        //this is the result of the evaluation
        Object o = e.evaluate(jc);
  
        if (o == null) {
          LOG.warn("expression returned null: " + script + ", in pattern: '" + stringToParse + "', available variables are: "
              + GrouperClientUtils.toStringForLog(variableMap.keySet()));
        }
        
        result.append(o);
        
        index = matcher.end();
      }
      
      result.append(stringToParse.substring(index, stringToParse.length()));
      return result.toString();
      
    } catch (Exception e) {
      throw new RuntimeException("Error substituting string: '" + stringToParse + "'", e);
    }
  }
  
  /**
   * get the attribute value of an attribute name of a subject
   * @param wsSubject subject
   * @param attributeNames list of attribute names in the subject
   * @param attributeName to query
   * @return the value or null
   */
  public static String subjectAttributeValue(WsSubject wsSubject, String[] attributeNames, String attributeName) {
    for (int i=0;i<GrouperClientUtils.length(attributeNames);i++) {
      
      if (GrouperClientUtils.equalsIgnoreCase(attributeName, attributeNames[i])
          && GrouperClientUtils.length(wsSubject.getAttributeValues()) > i) {
        //got it
        return wsSubject.getAttributeValue(i);
      }
    }
    return null;
  }

  /**
   * 
   * @return the encrypt key
   */
  public static String encryptKey() {
    String encryptKey = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("encrypt.key");
    
    boolean disableExternalFileLookup = GrouperClientConfig.retrieveConfig().propertyValueBooleanRequired(
        "encrypt.disableExternalFileLookup");
    
    //lets lookup if file
    encryptKey = GrouperClientUtils.readFromFileIfFile(encryptKey, disableExternalFileLookup);
    
    //the server does this, so if the key is blank, it will still have something there, so be consistent
    if (GrouperClientConfig.retrieveConfig().propertyValueBoolean("encrypt.encryptLikeServer", false)) {
      
      encryptKey += "w";
    }
    
    return encryptKey;
  }

  /**
   * name of the cache directory without trailing slash
   * @return the name of the cache directory
   */
  public static String cacheDirectoryName() {
    
    if (cacheDirectoryName == null) {
      String directoryName = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.cacheDirectory");
      if (GrouperClientCommonUtils.isBlank(directoryName)) {
        throw new RuntimeException("grouperClient.cacheDirectory is required in grouper.client.properties");
      }
  
      directoryName = GrouperClientCommonUtils.stripEnd(directoryName, "/");
      directoryName = GrouperClientCommonUtils.stripEnd(directoryName, "\\");
      
      File discoveryDir = new File(directoryName);
      directoryName = discoveryDir.getAbsolutePath();
      if (directoryName.endsWith("/.")) {
        directoryName = directoryName.substring(0, directoryName.length()-2);
      }
      if (directoryName.endsWith("\\.")) {
        directoryName = directoryName.substring(0, directoryName.length()-2);
      }
      cacheDirectoryName = directoryName;
    }    
    return cacheDirectoryName;
  }

  /** cache directory name */
  private static String cacheDirectoryName = null;
  

}
