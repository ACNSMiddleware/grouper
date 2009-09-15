/**
 * 
 */
package edu.internet2.middleware.grouper.ws.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.member.WsMemberFilter;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestInvalidRequest;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestServlet;
import edu.internet2.middleware.grouper.ws.rest.WsRestClassLookup;
import edu.internet2.middleware.grouper.ws.soap.WsParam;
import edu.internet2.middleware.grouper.ws.soap.WsResultMeta;
import edu.internet2.middleware.grouper.ws.soap.WsSubjectLookup;
import edu.internet2.middleware.subject.Subject;

/**
 * @author mchyzer
 * 
 */
public final class GrouperServiceUtils {

  /**
   * compute a url of a resource
   * @param resourceName
   * @param canBeNull if cant be null, throw runtime
   * @return the URL
   */
  public static URL computeUrl(String resourceName, boolean canBeNull) {
    //get the url of the navigation file
    ClassLoader cl = classLoader();

    URL url = null;

    try {
      url = cl.getResource(resourceName);
    } catch (NullPointerException npe) {
      String error = "computeUrl() Could not find resource file: " + resourceName;
      throw new RuntimeException(error, npe);
    }

    if (!canBeNull && url == null) {
      throw new RuntimeException("Cant find resource: " + resourceName);
    }

    return url;
  }


  /**
   * fast class loader
   * @return the class loader
   */
  public static ClassLoader classLoader() {
    return GrouperServiceUtils.class.getClassLoader();
  }


  /**
   * get a file name from a resource name
   * 
   * @param resourceName
   *          is the classpath location
   * 
   * @return the file path on the system
   */
  public static File fileFromResourceName(String resourceName) {
    
    URL url = computeUrl(resourceName, true);

    if (url == null) {
      return null;
    }

    File configFile = new File(url.getFile());

    return configFile;
  }

  /**
   * retrieve group type based on name
   * @param groupTypeName
   * @return group type
   */
  @SuppressWarnings("unchecked")
  public static GroupType retrieveGroupType(String groupTypeName) {
    if (StringUtils.isBlank(groupTypeName)) {
      return null;
    }
    try {
      GroupType groupType = GroupTypeFinder.find(groupTypeName);
      return groupType;
    } catch (SchemaException se) {

      //give a descriptive error, shouldnt be a security problem
      StringBuilder error = new StringBuilder("Cant find group type: '").append(
          groupTypeName).append("', valid types are: ");
      Set<GroupType> groupTypes = GroupTypeFinder.findAll();
      for (GroupType groupType : groupTypes) {
        error.append(groupType.getName()).append(", ");
      }
      throw new WsInvalidQueryException(error.toString());
    }

  }
  
  /**
   * do a case-insensitive matching
   * @param theEnumClass class of the enum
   * @param <E> generic type
   * 
   * @param string
   * @param exceptionOnNotFound true if exception should be thrown on not found
   * @return the enum or null or exception if not found
   * @throws GrouperRestInvalidRequest if there is a problem
   */
  public static <E extends Enum<?>> E enumValueOfIgnoreCase(Class<E> theEnumClass, String string, 
      boolean exceptionOnNotFound) throws GrouperRestInvalidRequest {
    
    if (!exceptionOnNotFound && StringUtils.isBlank(string)) {
      return null;
    }
    for (E e : theEnumClass.getEnumConstants()) {
      if (StringUtils.equalsIgnoreCase(string, e.name())) {
        return e;
      }
    }
    StringBuilder error = new StringBuilder(
        "Cant find " + theEnumClass.getSimpleName() + " from string: '").append(string);
    error.append("', expecting one of: ");
    for (E e : theEnumClass.getEnumConstants()) {
      error.append(e.name()).append(", ");
    }
    throw new GrouperRestInvalidRequest(error.toString());

  }
  
  /**
   * pop first url string, retrieve, and remove, or null if not there
   * @param urlStrings
   * @return the string or null if not there
   */
  public static String popUrlString(List<String> urlStrings) {
    
    int urlStringsLength = GrouperUtil.length(urlStrings);

    if (urlStringsLength > 0) {
      String firstResource = urlStrings.get(0);
      //pop off
      urlStrings.remove(0);
      //return
      return firstResource;
    }
    return null;
  }
  
  /**
   * <pre>
   * from url strings, get the subjectId
   * e.g. if the url is: /groups/aStem:aGroup/members/123412345
   * then the index should be 3 (0 for group, 1 for group name, etc) 
   * 
   * if url is: /groups/aStem:aGroup/members/sourceId/someSource/subjectId/123412345
   * then index is still 3
   * </pre>
   * @param urlStrings
   * @param startIndex
   * @param removeSubjectUrlStrings true to remove these url strings after getting data
   * @param sourceIdOrSubjectId true to get sourceId, false to get subjectId
   * @return the subjectId if it is found, null if not
   */
  public static String extractSubjectInfoFromUrlStrings(List<String> urlStrings, int startIndex, boolean sourceIdOrSubjectId,
      boolean removeSubjectUrlStrings) {
    String subjectId = null;
    String sourceId = null;
      
    //url should be: /groups/aStem:aGroup/members/sourceIds/someSource/subjectIds/123412345
    if (urlStrings.size() >= startIndex + 4 
        && StringUtils.equalsIgnoreCase("sources", urlStrings.get(startIndex))
        && StringUtils.equalsIgnoreCase("subjectId", urlStrings.get(startIndex+2))) {
      subjectId = urlStrings.get(startIndex + 3);
      sourceId = urlStrings.get(startIndex + 1);
      if (removeSubjectUrlStrings) {
        //remove 4
        for (int i=0;i<4;i++) {
          //these will collapse so different ones are removed
          urlStrings.remove(startIndex);
        }
      }
      //no sourceId in this setup
      return sourceIdOrSubjectId ? sourceId : subjectId;
    }
  
    // /groups/aStem:aGroup/members/123412345
    if (urlStrings.size() >= startIndex + 1) {
      subjectId = urlStrings.get(startIndex);
      if (removeSubjectUrlStrings) {
        urlStrings.remove(startIndex);
      }
      //no sourceId in this setup
      return sourceIdOrSubjectId ? null : subjectId;
    }
    return null;
  }
  
  /**
   * pick one of the values which is not null or empty.
   * if both sent in, make sure they are equal.
   * if neither, then see if ok.  return null if none sent in (blank)
   * @param first
   * @param second
   * @param allowBlank if ok if blank (return null)
   * @param reasonFieldName if not ok to be blank and both blank, then put this in error.
   * or if both set to different things, then put this in error
   * @return one or the other or null or exception
   * @throws WsInvalidQueryException if there is a problem with the reason text in there
   */
  public static String pickOne(String first, String second, boolean allowBlank, String reasonFieldName) {
    boolean hasFirst = !StringUtils.isBlank(first);
    boolean hasSecond = !StringUtils.isBlank(second);
    if (!hasFirst && !hasSecond) {
      //maybe this is ok
      if (allowBlank) {
        return null;
      }
      //not ok
      throw new WsInvalidQueryException("The field '" + reasonFieldName + "' is required");
    } 
    //this is the best situation
    if (!(hasFirst && hasSecond)) {
      return hasFirst ? first : second;
    }
    //has both first and second
    //if both equal, return one
    if (first.equals(second)) {
      return first;
    }
    //has both and they arent equal, that is a problem
    throw new WsInvalidQueryException("The field '" + reasonFieldName + "' is set twice to two different values: '" + first + "', '"
        + second + "', and should just be set to one value!");
  }
  
  /**
   * take http params and put into an object (type is specified in the http params object
   * if there is an object specified)
   * @param paramMap
   * @param httpServletRequest if not null, make sure no dupes.  if null, forget it
   * @param warnings is the warnings, if null, just throw exception
   * @return the object or null if nothing in query params
   */
  public static Object marshalHttpParamsToObject(Map<String,String> paramMap, 
      HttpServletRequest httpServletRequest, StringBuilder warnings) {

    String objectTypeSimpleName = GrouperServiceJ2ee.parameterValue(paramMap, httpServletRequest, WS_LITE_OBJECT_TYPE);
    Set<String> namesForWarnings = new HashSet<String>();
    //if no object, then set warnings
    boolean hasObjectType = !StringUtils.isBlank(objectTypeSimpleName);
    Object object = null;
    if (!hasObjectType) {
      namesForWarnings.addAll(paramMap.keySet());
    } else {
      
      Class<?> objectClass = WsRestClassLookup.retrieveClassBySimpleName(objectTypeSimpleName); 
      object = GrouperUtil.newInstance(objectClass);
      
      //lets assign other params
      for (String key: paramMap.keySet()) {
        if (StringUtils.equals(key, WS_LITE_OBJECT_TYPE)) {
          continue;
        }
        String value = GrouperServiceJ2ee.parameterValue(paramMap, httpServletRequest, key);
        Method method = GrouperUtil.setter(objectClass, key, true, false);
        if (method == null) {
          namesForWarnings.add(key);
        } else {
          //should we see if scalar, or try to assign?  hmmmm... hopefully the
          //scalar list is up to date
          if (!GrouperUtil.isScalar(method.getParameterTypes()[0])) {
            namesForWarnings.add(key);
          } else {
            //if null or blank, then forget it
            if (StringUtils.isBlank(value)) {
              continue;
            }
            try {
              GrouperUtil.assignSetter(object, key, value, true);
            } catch (RuntimeException re) {
              throw new WsInvalidQueryException("Problem assigning object: " + objectClass
                  + ", property: " + key + ", value: " + value, re); 
            }
          }
        }
      }
    }
    StringBuilder theWarnings = new StringBuilder();
    if (namesForWarnings.size() > 0) {
      theWarnings.append("Cant find properties to assign HTTP params: ");
      for (String property: namesForWarnings) {
        theWarnings.append(property).append(", ");
      }
      theWarnings.append("\n");
      if (warnings == null) {
        throw new RuntimeException(theWarnings.toString());
      }
      //append the warnings to the stringbuilder passed in
      warnings.append(theWarnings);
    }
    return object;

  }
  
  /**
   * convert a query string to a map (for testing purposes only, in a real system the
   * HttpServletRequest would be used.  no dupes allowed
   * @param queryString
   * @return the map (null if no query string)
   */
  public static Map<String, String> convertQueryStringToMap(String queryString) {
    
    if (StringUtils.isBlank(queryString)) {
      return null;
    }
    
    Map<String, String> paramMap = new HashMap<String, String>();
    
    String[] queryStringPairs = GrouperUtil.splitTrim(queryString, "&");
    
    for (String queryStringPair : queryStringPairs) {
      
      String key = GrouperUtil.prefixOrSuffix(queryStringPair, "=", true);
      String value = GrouperUtil.prefixOrSuffix(queryStringPair, "=", false);
      //unescape the value
      value = GrouperUtil.escapeUrlDecode(value);
      
      if (paramMap.containsKey(key)) {
        throw new RuntimeException("Query string contains two of the same key: " 
            + key + ", " + queryString);
        
      }
      paramMap.put(key, value);
    }
    return paramMap;
  }
  
  /**
   * convert a bean to a query string (including bean type)
   * @param object
   * @param convertNullToEmpty is true to convert null to empty, false to exclude nulls
   * @param includeClassName true to include the classname in the params
   * @return the query string
   */
  public static String marshalLiteBeanToQueryString(Object object, boolean convertNullToEmpty, boolean includeClassName) {
    
    //if no object, then dont worry about it
    if (object == null) {
      return "";
    }
    
    StringBuilder queryString = new StringBuilder();
    
    //first add classname 
    if (includeClassName) {
      queryString.append(WS_LITE_OBJECT_TYPE).append("=").append(
          GrouperUtil.escapeUrlEncode(object.getClass().getSimpleName()));
    }
   
    Set<Method> getters = GrouperUtil.getters(object.getClass(), Object.class, null, true);
    
    for (Method getter : getters) {
      
      String propertyName = GrouperUtil.propertyName(getter);
      //dont do getClass()
      if (StringUtils.equals(propertyName, "class")) {
        continue;
      }
      //only do scalars
      if (!GrouperUtil.isScalar(getter.getReturnType())) {
        throw new RuntimeException("Trying to convert an object " + object.getClass() + " to a query string, but a" +
        		" property is not a scalar: " + getter.getName() + ": " + getter.getReturnType());
      }
      //get the values
      Object value = GrouperUtil.invokeMethod(getter, object);
      //if null forget it
      if (value == null) {
        if (!convertNullToEmpty) {
          continue;
        }
        value = "";
      }
      //convert to string
      String valueString = GrouperUtil.stringValue(value);
      String escapedValueString = GrouperUtil.escapeUrlEncode(valueString);
      //if not doing classname, then there might not be anything before this
      if (queryString.length() != 0) {
        queryString.append('&');
      }
      queryString.append(propertyName).append("=").append(escapedValueString);
      
    }
    return queryString.toString();
  }
  
  /** cookie response */
  private static Pattern cookieResponsePattern = Pattern.compile("^Set-Cookie\\: JSESSIONID=.+; Path=/(.+)$");
  
  /**
   * take an http request and format it (assume xml or json)
   * @param http
   * @return format
   */
  public static String formatHttp(String http) {
    //is newline correct?  platform independent?
    http = http.trim();
    String[] lines = GrouperUtil.splitTrim(http, "\n", false);
    StringBuilder result = new StringBuilder();
    boolean seenBody = false;
    boolean seenBlank = false;
    boolean seenFirstSize = false;
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      if (StringUtils.isBlank(line)) {
        seenBlank = true;
        result.append(line).append("\n");
        continue;
      }
      if (seenBlank) {
        if (!seenFirstSize) {
          //if there is no chunking, and this is last line, then format
          if (i == lines.length-1 && lines[i].length() > 6) {
            String indented = GrouperUtil.indent(lines[i], false);
            result.append(indented).append("\n");
            continue;
          }
          //maybe xhtml multi-line
          if (line.startsWith("<")) {
            StringBuilder body = new StringBuilder();
            for (; i < lines.length; i++) {
              body.append(lines[i]).append("\n");
            }
            String bodyString = body.toString();
            String indented = GrouperUtil.indent(bodyString, false);
            result.append(indented).append("\n");
            i = lines.length - 1;
            seenBody = true;
            continue;
          }
          //must be first size, might need a regex here or something
          seenFirstSize = true;
          result.append(line).append("\n");
          continue;
        }
      }
      if (seenBlank && seenFirstSize && !seenBody) {
        StringBuilder body = new StringBuilder();
        for (; i < lines.length - 1; i++) {
          body.append(lines[i]).append("\n");
        }
        String bodyString = body.toString();
        String indented = GrouperUtil.indent(bodyString, false);
        result.append(indented).append("\n");
        i = lines.length - 2;
        seenBody = true;
        continue;
      }
      //format authorization
      if (line.matches("^Authorization\\: Basic (.+)$")) {
        line = "Authorization: Basic xxxxxxxxxxxxxxxxx==";
      }
      Matcher matcher = cookieResponsePattern.matcher(line);
      if (matcher.matches()) {
        line = "Set-Cookie: JSESSIONID=xxxxxxxxxxxxxxxxxxxxxxxx; Path=/" + matcher.group(1);
      }
      result.append(line).append("\n");
    }
    return result.toString();
  }

  /**
   * url param for the classname of a bean
   */
  public static final String WS_LITE_OBJECT_TYPE = "wsLiteObjectType";

  /**
   * no need to construct
   */
  private GrouperServiceUtils() {
    // no need to construct
  }

  /**
   * get null safe field name from field
   * @param field
   * @return field name
   */
  public static String fieldName(Field field) {
    return (field == null ? null : field.getName());
  }

  /**
   * take an array.  if array size is null or empty, then return null.
   * if array size if more than one, then error
   * return first
   * @param <T>
   * @param array is the array to interrogate
   * @return the first or null
   */
  public static <T> T firstInArrayOfOne(T[] array) {
    if (array == null || array.length == 0) {
      return null;
    }
    if (array.length > 1) {
      throw new RuntimeException("Expected length of 0 or 1, but was: " + array.length
          + ", " + array.getClass().getComponentType());
    }
    return array[0];
  }

  /**
   * convert a subject to a member, throw an invalid query exception if there is a problem,
   * with a descriptive cause
   * @param session
   * @param subject
   * @return the member
   */
  public static Member convertSubjectToMember(GrouperSession session, Subject subject) {

    Member member = null;

    member = MemberFinder.findBySubject(session, subject);
    if (member == null) {
      throw new WsInvalidQueryException("Member is null after findBySubject");
    }
    return member;
  }

  /**
   * convert the member filter, default to all
   * @param memberFilter
   * @return the member filter
   * @throws WsInvalidQueryException if there is a problem
   */
  public static WsMemberFilter convertMemberFilter(String memberFilter)
      throws WsInvalidQueryException {
    WsMemberFilter wsMemberFilter = null;
    try {
      wsMemberFilter = WsMemberFilter.valueOfIgnoreCase(memberFilter);
    } catch (Exception e) {
      //this exception will be descriptive
      throw new WsInvalidQueryException(e);
    }
    //default to all
    return GrouperUtil.defaultIfNull(wsMemberFilter, WsMemberFilter.All);
  }

  /**
   * convert the tx type safely with a descriptive message
   * @param txTypeString
   * @return the tx type
   * @throws WsInvalidQueryException
   */
  public static GrouperTransactionType convertTransactionType(String txTypeString)
      throws WsInvalidQueryException {
    try {
      GrouperTransactionType grouperTransactionType = GrouperTransactionType
          .valueOfIgnoreCase(txTypeString);
      grouperTransactionType = GrouperUtil.defaultIfNull(grouperTransactionType,
          GrouperTransactionType.NONE);
      return grouperTransactionType;
    } catch (Exception e) {
      throw new WsInvalidQueryException("Invalid txType: '" + txTypeString + "', "
          + e.getMessage());
    }
  }

  /**
   * convert the save mode safely with a descriptive message
   * @param theSaveMode
   * @return the save mode
   * @throws WsInvalidQueryException
   */
  public static SaveMode convertSaveMode(String theSaveMode)
      throws WsInvalidQueryException {
    try {
      SaveMode saveMode = SaveMode.valueOfIgnoreCase(theSaveMode);
      saveMode = GrouperUtil.defaultIfNull(saveMode, SaveMode.INSERT_OR_UPDATE);
      return saveMode;
    } catch (Exception e) {
      throw new WsInvalidQueryException("Invalid saveMode: '" + theSaveMode + "', "
          + e.getMessage());
    }
  }

  /**
   * convert the version safely with a descriptive message.  there is no default version
   * @param theVersion
   * @return the version
   * @throws WsInvalidQueryException
   */
  public static GrouperWsVersion convertGrouperWsVersion(String theVersion)
      throws WsInvalidQueryException {
    try {
      GrouperWsVersion version = GrouperWsVersion.valueOfIgnoreCase(theVersion, true);

      return version;
    } catch (Exception e) {
      throw new WsInvalidQueryException("Invalid version: '" + theVersion + "', "
          + e.getMessage());
    }
  }

  /** test session so we can run tests not in a container */
  public static GrouperSession testSession = null;
  
  /**
   * convert the actAsSubjectLookup (and the currently logged in user) to a grouper session
   * @param actAsSubjectLookup
   * @return the session
   */
  public static GrouperSession retrieveGrouperSession(WsSubjectLookup actAsSubjectLookup) {
    if (testSession != null) {
      return testSession;
    }
    Subject actAsSubject = null;
    actAsSubject = GrouperServiceJ2ee.retrieveSubjectActAs(actAsSubjectLookup);

    if (actAsSubject == null) {
      throw new WsInvalidQueryException("Cant find actAs user: " + actAsSubjectLookup);
    }

    // use this to be the user connected, or the user act-as
    try {
      GrouperSession session = GrouperSession.start(actAsSubject);
      return session;
    } catch (SessionException se) {
      //this is probably the callers fault, though not positive...
      throw new WsInvalidQueryException("Problem with session for subject: "
          + actAsSubject, se);
    }
  }

  /**
   * get array length, make sure at least 1 
   * @param objects to count
   * @param configNameOfMax is the config name of grouper-ws.properties where the max number of subjects is
   * @param defaultMax is the default if not in the config file
   * @param label for error
   * @return the length
   * @throws WsInvalidQueryException if array is null or size 0 or more than max
   */
  public static int arrayLengthAtLeastOne(Object[] objects,
      String configNameOfMax, int defaultMax, String label) throws WsInvalidQueryException {
    int objectsLength = objects == null ? 0 : objects.length;
    if (objectsLength == 0) {
      throw new WsInvalidQueryException(label + " length must be at least 1");
    }
    // see if greater than the max (or default)
    int maxObjects = GrouperWsConfig.getPropertyInt(configNameOfMax, 1000000);
    if (objectsLength > maxObjects) {
      throw new WsInvalidQueryException(label + " length must be less than max: "
          + maxObjects + " (sent in " + objectsLength + ")");
    }
    return objectsLength;
  }

  /**
   * if wrong size or a name is blank, then throw descriptive exception.  Assumes all params can be put into a map,
   * only one name can be sent per request, no blank names.
   * @param params
   * @return the map of names to values, will not return null
   * @throws WsInvalidQueryException if problem with inputs
   */
  public static Map<String, String> convertParamsToMap(WsParam[] params) throws WsInvalidQueryException {
    if (params == null || params.length == 0) {
      return new HashMap<String,String>();
    }
    Map<String, String> result = new LinkedHashMap<String, String>(params.length);
    int i = 0;
    for (WsParam param : params) {
      if (param!=null) {
        String paramName = param.getParamName();
        if (StringUtils.isBlank(paramName)) {
          throw new WsInvalidQueryException("Param name index " + i
              + " cannot be blank: '" + paramName + "'");
        }
        if (result.containsKey(paramName)) {
          throw new WsInvalidQueryException("Param name index " + i
              + " cannot be submitted twice: '" + paramName + "'");
        }
        result.put(paramName, param.getParamValue());
      }
      i++;
    }
    return result;

  }

  /**
   * @param requestedAttributes
   * @param requestedAttributesLength
   * @param includeSubjectDetailBoolean
   * @return the attributes calculated
   */
  public static String[] calculateSubjectAttributes(String[] requestedAttributes,
      boolean includeSubjectDetailBoolean) {

    //attribute names
    List<String> attributeNamesList = GrouperUtil.nonNull(GrouperUtil
        .toList(requestedAttributes));

    if (attributeNamesList.size() == 1 && StringUtils.isBlank(attributeNamesList.get(0))) {
      attributeNamesList.remove(0);
    }
    
    //reduce the attributes into one list
    String attributeNames = GrouperWsConfig
        .getPropertyString(GrouperWsConfig.WS_SUBJECT_RESULT_ATTRIBUTE_NAMES);

    String[] theAttributes = GrouperUtil.splitTrim(attributeNames, ",");

    GrouperUtil.addIfNotThere(attributeNamesList, GrouperUtil.toList(theAttributes));

    if (includeSubjectDetailBoolean) {

      attributeNames = GrouperWsConfig
          .getPropertyString(GrouperWsConfig.WS_SUBJECT_RESULT_DETAIL_ATTRIBUTE_NAMES);
      //default for extended is name,description
      attributeNames = GrouperUtil.defaultIfNull(attributeNames, "name, description");

      theAttributes = GrouperUtil.splitTrim(attributeNames, ",");

      GrouperUtil.addIfNotThere(attributeNamesList, GrouperUtil.toList(theAttributes));
    }

    return GrouperUtil.toArray(attributeNamesList, String.class);
  }

  /**
   * convert a boolean to a T or F
   * 
   * @param theBoolean
   * @return boolean
   */
  public static String booleanToStringOneChar(Boolean theBoolean) {
    if (theBoolean == null) {
      return null;
    }
    return theBoolean ? "T" : "F";
  }

  /**
   * parse a boolean as "T" or "F" or "TRUE" or "FALSE" case insensitive. If
   * not specified, then use default. If malformed, then exception
   * 
   * @param input
   * @param defaultValue
   * @param paramName to put in the invalid query exception
   * @return the boolean
   * @throws WsInvalidQueryException if there is a problem
   */
  public static boolean booleanValue(String input, boolean defaultValue, String paramName)
      throws WsInvalidQueryException {
    try {
      return GrouperUtil.booleanValue(input, defaultValue);
    } catch (Exception e) {
      //all info is in the message
      throw new WsInvalidQueryException("Problem with boolean '" + paramName + "', "
          + e.getMessage());
    }
  }

  /**
   * convert a fieldName into a Field
   * @param fieldName name of field
   * @return the field, or throw invalid query exception, or null if not there
   */
  public static Field retrieveField(String fieldName) {
    Field field = null;

    //get field
    try {
      field = StringUtils.isBlank(fieldName) ? null : FieldFinder.find(fieldName);
    } catch (Exception e) {
      throw new WsInvalidQueryException("Problem with fieldName: " + fieldName + ".  "
          + ExceptionUtils.getFullStackTrace(e));
    }
    return field;
  }

  /**
   * web service format string
   */
  private static final String WS_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";

  /**
   * convert a date to a string using the standard web service pattern
   * yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   * 
   * @param date
   * @return the string, or null if the date is null
   */
  public static String dateToString(Date date) {
    if (date == null) {
      return null;
    }
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(WS_DATE_FORMAT);
    return simpleDateFormat.format(date);
  }

  /**
   * convert a string to a date using the standard web service pattern Note
   * that HH is 0-23
   * 
   * @param dateString
   * @return the string, or null if the date was null
   */
  public static Date stringToDate(String dateString) {
    if (dateString == null) {
      return null;
    }
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(WS_DATE_FORMAT);
    try {
      return simpleDateFormat.parse(dateString);
    } catch (ParseException e) {
      throw new RuntimeException("Cannot convert '" + dateString
          + "' to a date based on format: " + WS_DATE_FORMAT, e);
    }
  }

  /**
   * organize params
   * 
   * @param paramName0
   * @param paramValue0
   * @param paramName1
   * @param paramValue1
   * @return the array param names
   */
  public static WsParam[] params(String paramName0, String paramValue0, String paramName1, String paramValue1) {
    
    if (!StringUtils.isBlank(paramName0)) {
      if (!StringUtils.isBlank(paramName1)) {
        WsParam[] params = new WsParam[2];
        params[0] = new WsParam(paramName0, paramValue0);
        params[1] = new WsParam(paramName1, paramValue1);
        return params;
      }
      return new WsParam[]{new WsParam(paramName0, paramValue0)};
    }
    //not sure why someone would pass second ard and not first, but oh well 
    if (!StringUtils.isBlank(paramName1)) {
      return new WsParam[]{new WsParam(paramName1, paramValue1)};
    }
    return null;
  }


  /**
   * convert a set of access privileges to privileges
   * 
   * @param accessPrivileges
   * @return the set of privileges, will never return null
   */
  public static Set<Privilege> convertAccessPrivilegesToPrivileges(
      Set<AccessPrivilege> accessPrivileges) {
    Set<Privilege> result = new HashSet<Privilege>();
    if (accessPrivileges != null) {
      for (AccessPrivilege accessPrivilege : accessPrivileges) {
        Privilege privilege = accessPrivilege.getPrivilege();
        result.add(privilege);
      }
    }
    return result;
  }

  /**
   * add response headers for a success and response code
   * @param response
   * @param success T or F
   * @param resultCode
   * @param resultCode2
   */
  public static void addResponseHeaders(HttpServletResponse response, String success,
      String resultCode, String resultCode2) {
    if (!response.containsHeader(GrouperRestServlet.X_GROUPER_RESULT_CODE)) {
      //default to NONE if not set for some reason (NONE will give clue that something wrong, why isnt it set???)
      response.addHeader(GrouperRestServlet.X_GROUPER_RESULT_CODE, StringUtils.defaultIfEmpty(resultCode, "NONE"));
    }
    if (!response.containsHeader(GrouperRestServlet.X_GROUPER_SUCCESS)) {
      //default to F if not set for some reason
      response.addHeader(GrouperRestServlet.X_GROUPER_SUCCESS, StringUtils.defaultIfEmpty(success, "F"));
    }
    if (!response.containsHeader(GrouperRestServlet.X_GROUPER_RESULT_CODE2)) {
      //default to NONE if not set for some reason (NONE will give clue that something wrong, why isnt it set???)
      response.addHeader(GrouperRestServlet.X_GROUPER_RESULT_CODE2, StringUtils.defaultIfEmpty(resultCode2, "NONE"));
    }
  }

  /**
   * add response headers for a success and response code
   * will retrieve the response object from threadlocal
   * @param wsResultMeta result metadata
   * @param isSoap if soap
   */
  public static void addResponseHeaders(WsResultMeta wsResultMeta, boolean isSoap) {
    HttpServletResponse httpServletResponse = GrouperServiceJ2ee
        .retrieveHttpServletResponse();
    addResponseHeaders(httpServletResponse, wsResultMeta.getSuccess(), wsResultMeta
        .getResultCode(), wsResultMeta.getResultCode2());
    
    //if soap, then add a content type if specified in the grouper-ws.properties
    //NOTE: this doesnt do anything since Axis sets its own headers...
//    String soapContentType = GrouperWsConfig.getPropertyString("ws.soapContentType");
//    if (!StringUtils.isBlank(soapContentType)) {
//      httpServletResponse.setContentType(soapContentType);
//    }
  }

}
