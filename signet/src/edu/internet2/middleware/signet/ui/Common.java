/*--
$Id: Common.java,v 1.72 2006-12-07 02:12:40 ddonn Exp $
$Date: 2006-12-07 02:12:40 $
  
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

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessages;
import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.AssignmentHistory;
import edu.internet2.middleware.signet.Decision;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.Grantable;
import edu.internet2.middleware.signet.History;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Proxy;
import edu.internet2.middleware.signet.ProxyHistory;
import edu.internet2.middleware.signet.SelectionType;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.resource.ResLoaderUI;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

public class Common
{
  private static final String DAY_SUFFIX   = "_day";
  private static final String MONTH_SUFFIX = "_month";
  private static final String YEAR_SUFFIX  = "_year";
  
  private static final String HASDATE_SUFFIX  = "_hasDate";
  private static final String DATETEXT_SUFFIX = "_dateText";
  
  private static final String NODATE_VALUE  = "NO_DATE";
  private static final String YESDATE_VALUE = "YES_DATE";
  
  private static final String DATE_FORMAT = "MM-dd-yyyy";
  private static final String DATE_SAMPLE = "mm-dd-yyyy";

  final private static java.text.SimpleDateFormat[] DATERS =  {
    new java.text.SimpleDateFormat("MM/d/yy"),
    new java.text.SimpleDateFormat("MM-d-yy"),
    new java.text.SimpleDateFormat("MM.d.yy"),
    new java.text.SimpleDateFormat("dd-MMM-yy"),
    new java.text.SimpleDateFormat("dd-MMMM-yy"),
    new java.text.SimpleDateFormat("yyyy-MM-dd"),
    new java.text.SimpleDateFormat("MMMM d, yyyy")
    };
  
  private static final Set activeAndPendingStatus = new HashSet();
  private static final Set inactiveStatus = new HashSet();
  
  private static final Comparator grantableReportComparator
    = new GrantableReportComparator();
  
  private static final Map dateFormatMap = new HashMap();

  /**
   *  
   */
  static
  /* runs at class load time */
  {
    activeAndPendingStatus.add(Status.ACTIVE);
    activeAndPendingStatus.add(Status.PENDING);
    
    inactiveStatus.add(Status.INACTIVE);
  }
  
  /**
   * @param log
   * @param request
   */
  public static void showHttpParams
  	(String prefix, Log log, HttpServletRequest request)
  {
    Enumeration paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements())
    {
      String paramName = (String)(paramNames.nextElement());
      String[] paramValues = request.getParameterValues(paramName);
      log.warn(prefix + ": " + paramName + "=" + printArray(paramValues));
    }
  }
  
  private static SimpleDateFormat getDateFormat(String formatStr)
  {
    SimpleDateFormat formatter
      = (SimpleDateFormat)(dateFormatMap.get(formatStr));
    
    if (formatter == null)
    {
      formatter = new SimpleDateFormat(formatStr);
      dateFormatMap.put(formatStr, formatter);
    }
    
    return formatter;
  }
  
  public static String displayDatetime(String formatStr, Date datetimeVal)
  {
    SimpleDateFormat formatter = getDateFormat(formatStr);
    String dateStr = formatter.format(datetimeVal);
    
    // Here's a hack: If the dateStr includes "AM" or "PM", then make those
    // characters lower-case.
    
    dateStr = dateStr.replaceAll("AM", "am");
    dateStr = dateStr.replaceAll("PM", "pm");

    return dateStr;
  }
  
  public static String describeChange
    (History[]  historyArray,
     int        historyIndex)
  {
    StringBuffer changeStr = new StringBuffer();
    History newerHistoryRecord = historyArray[historyIndex];
    History olderHistoryRecord = null;
    
    if (historyIndex != (historyArray.length - 1))
    {
      olderHistoryRecord = historyArray[historyIndex + 1];
    }

    StringBuffer editorDescription = describeEditor(newerHistoryRecord);
    
    Difference diff = diff(newerHistoryRecord, olderHistoryRecord);
    
    if (diff.equals(Difference.GRANT))
    {
      changeStr.append(ResLoaderUI.getString("Common.describeChange.granted.txt"));
      changeStr.append(editorDescription);
    }
    else if (diff.equals(Difference.REVOKE))
    {
      changeStr.append(ResLoaderUI.getString("Common.describeChange.revoked.txt"));
      changeStr.append(editorDescription);
    }
    else
    {
      changeStr.append(ResLoaderUI.getString("Common.describeChange.modified.txt"));
      changeStr.append(editorDescription);
      changeStr.append
        (describeModifications(newerHistoryRecord, olderHistoryRecord));
    }
    
    return changeStr.toString();
  }
  
  private static StringBuffer describeChange
    (String label,
     String timeWord,
     String nullDescription,
     Date   newer,
     Date   older)
  {
    StringBuffer description = new StringBuffer();

    // Include the simple "!=" test to account for two null Date values.
    // I found an interesting bug: newer.equals(older) always works fine,
    // but older.equals(newer) sometimes returned a false negative, because
    // the "older" value was a JDBC-vendor-specific subclass of Date
    // which didn't like comparing to a plain old Date.
    if ((older != newer)
        && ((older == null) || (newer == null) || !newer.equals(older)))
    {
    	String dhtmlTemplate =
    				"<p>\n" +
    				"<span class=\"status\">\n" +
    				"    {5}\n" +
    				"  </span>\n" +
    				"  <span class=\"label\">\n" +
    				"{0}" +
    				" {6}\n" +
    				"  </span>\n" +
    				"''{1} {2}''\n" + // MessageFormat requires single quotes to be doubled (can't escape with a backslash)
    				"  <span class=\"label\">\n" +
    				"    {7}\n" +
    				"  </span>\n" +
    				"''{3} {4}''\n" + // MessageFormat requires single quotes to be doubled (can't escape with a backslash)
    				" </p>\n";
    	String[] dhtmlData = new String[] {
			label,
			timeWord,
			(older == null) ? nullDescription : displayDatetime(Constants.DATETIME_FORMAT_24_DAY, older),
			timeWord,
			(newer == null) ? nullDescription : displayDatetime(Constants.DATETIME_FORMAT_24_DAY, newer),
			ResLoaderUI.getString("Common.describeChange.changed.txt"),
			ResLoaderUI.getString("Common.describeChange.from.txt"),
			ResLoaderUI.getString("Common.describeChange.to.txt")
    	};

    	MessageFormat mf = new MessageFormat(dhtmlTemplate);
		description.append(mf.format(dhtmlData));
    }
    
    return description;
  }
  
  private static StringBuffer describeModifications
    (History newer,
     History older)
  {
    StringBuffer description = new StringBuffer();
    description.append
      (describeChange
        (ResLoaderUI.getString("Common.describeModifications.effdate.txt"),
         "",
         ResLoaderUI.getString("Common.describeModifications.immediate.txt"),
         newer.getEffectiveDate(),
         older.getEffectiveDate()));
    description.append
      (describeChange
        (ResLoaderUI.getString("Common.describeModifications.duration.txt"),
         ResLoaderUI.getString("Common.describeModifications.until.txt"),
         ResLoaderUI.getString("Common.describeModifications.revoked.txt"),
         newer.getExpirationDate(),
         older.getExpirationDate()));
    
    if (newer instanceof AssignmentHistory)
    {
      description.append
        (describeChange
          (ResLoaderUI.getString("Common.describeModifications.canuse.txt"),
           ((AssignmentHistory)newer).canUse(),
           ((AssignmentHistory)older).canUse()));
      
      description.append
        (describeChange
          (ResLoaderUI.getString("Common.describeModifications.cangrant.txt"),
           ((AssignmentHistory)newer).canGrant(),
           ((AssignmentHistory)older).canGrant()));

      description.append
        (describeLimitChanges
          (((AssignmentHistory)newer).getLimitValues(),
           ((AssignmentHistory)older).getLimitValues()));
    }
    else
    {
      description.append
        (describeChange
          (ResLoaderUI.getString("Common.describeModifications.canuse.txt"),
           ((ProxyHistory)newer).canUse(),
           ((ProxyHistory)older).canUse()));
      description.append
        (describeChange
          (ResLoaderUI.getString("Common.describeModifications.canextend.txt"),
           ((ProxyHistory)newer).canExtend(),
           ((ProxyHistory)older).canExtend()));
    }
    
    return description;
  }
  
  private static StringBuffer describeLimitChanges
    (Set newerLimitValues,
     Set olderLimitValues)
  {
    StringBuffer description = new StringBuffer();
    
    Set newerSingleChoiceLimitValues
      = getSubset(newerLimitValues, SelectionType.SINGLE);
    Set olderSingleChoiceLimitValues
      = getSubset(olderLimitValues, SelectionType.SINGLE);
    description.append
      (describeSingleChoiceLimitChanges
        (newerSingleChoiceLimitValues, olderSingleChoiceLimitValues));
    
    Set newerMultiChoiceLimitValues
      = getSubset(newerLimitValues, SelectionType.MULTIPLE);
    Set olderMultiChoiceLimitValues
      = getSubset(olderLimitValues, SelectionType.MULTIPLE);
    Set deletedLimitValues
      = subtractIntersection
          (olderMultiChoiceLimitValues, newerMultiChoiceLimitValues);
    description.append(
    	describeMultiChoiceLimitChanges(
    		ResLoaderUI.getString("Common.describeLimitChanges.deleted.txt"),
    		deletedLimitValues));
    
    Set addedLimitValues
      = subtractIntersection
          (newerMultiChoiceLimitValues, olderMultiChoiceLimitValues);
    description.append(
    	describeMultiChoiceLimitChanges(
    		ResLoaderUI.getString("Common.describeLimitChanges.added.txt"),
    		addedLimitValues));
    
    return description;
  }
  
  private static StringBuffer describeSingleChoiceLimitChanges
    (Set newerLimitValues,
     Set olderLimitValues)
  {
    StringBuffer description = new StringBuffer();
    
    Iterator olderLimitValuesIterator = olderLimitValues.iterator();
    while (olderLimitValuesIterator.hasNext())
    {
      LimitValue olderLimitValue
        = (LimitValue)(olderLimitValuesIterator.next());
      
      LimitValue newerLimitValue = null;
      Iterator newerLimitValuesIterator = newerLimitValues.iterator();
      while (newerLimitValuesIterator.hasNext())
      {
        LimitValue candidate = (LimitValue)(newerLimitValuesIterator.next());
        if (candidate.getLimit().equals(olderLimitValue.getLimit()))
        {
          newerLimitValue = candidate;
        }
      }
      
      description.append(describeChange(newerLimitValue, olderLimitValue));
    }
    
    return description;
  }
  
  private static Set getSubset
    (Set            limitValues,
     SelectionType  selectionType)
  {
    Set subset = new HashSet();
    
    Iterator limitValuesIterator = limitValues.iterator();
    while (limitValuesIterator.hasNext())
    {
      LimitValue limitValue = (LimitValue)(limitValuesIterator.next());
      if (limitValue.getLimit().getSelectionType().equals(selectionType))
      {
        subset.add(limitValue);
      }
    }
      
    return subset;
  }
  
  private static Set subtractIntersection(Set set1, Set set2)
  {
    Set difference = new HashSet(set1.size());
    difference.addAll(set1);
    difference.removeAll(set2);
    
    return difference;
  }
   
  private static StringBuffer describeMultiChoiceLimitChanges
    (String label,
     Set    limitValues)
  {
    StringBuffer description = new StringBuffer();
    
    Iterator limitValuesIterator = limitValues.iterator();
    while (limitValuesIterator.hasNext())
    {
      LimitValue limitValue = (LimitValue)(limitValuesIterator.next());
      
      description.append("<p>\n");
      description.append("  <span class=\"status\">\n");
      description.append(     label);
      description.append(     "\n");
      description.append("  </span>\n");
      description.append("  <span class=\"label\">\n");
      description.append(     limitValue.getLimit().getName());
      description.append(     "\n");
      description.append("  </span>\n");
      description.append(     limitValue.getDisplayValue());
      description.append(     "\n");
      description.append("</p>\n");
    }
    
    return description;
  }

  private static StringBuffer describeChange
    (String label,
     boolean newer,
     boolean older)
  {
    StringBuffer description = new StringBuffer();
    
    if (newer != older)
    {
    	//"<p> <span class=\"status\">{3}</span> <span class=\"label\">{0}    {4}</span>    ''{1}'' <span class=\"label\">    {5}</span>    ''{2}'' </p>"
    	String dhtmlTemplate = 
    		"<p>\n" +
    		"  <span class=\"status\">\n" +
    		"    {3}\n" +
    		"  </span>\n" +
    		"  <span class=\"label\">\n" +
    		"{0}" +
    		"    {4}\n" +
    		"  </span>\n" +
    		"    ''{1}''\n" +
    		"  <span class=\"label\">\n" +
    		"    {5}\n" +
    		"  </span>\n" +
    		"    ''{2}''\n" +
    		" </p>\n";

    	String[] dhtmlData = new String[] {
    		label,
    		Boolean.toString(older),
    		Boolean.toString(newer),
    		ResLoaderUI.getString("Common.describeChange.changed.txt"),
    		ResLoaderUI.getString("Common.describeChange.from.txt"),
    		ResLoaderUI.getString("Common.describeChange.to.txt"),
    	};

    	MessageFormat mf = new MessageFormat(dhtmlTemplate);
    	description = new StringBuffer(mf.format(dhtmlData));
    }
    
    return description;
  }

  private static StringBuffer describeChange
    (LimitValue newer,
     LimitValue older)
  {
    StringBuffer description = new StringBuffer();
    
    if (!newer.getValue().equals(older.getValue()))
    {
      description.append("<p>\n");
      description.append("  <span class=\"status\">\n");
      description.append("    changed\n");
      description.append("  </span>\n");
      description.append("  <span class=\"label\">\n");
      description.append(     older.getLimit().getName());
      description.append("    from\n");
      description.append("  </span>\n");
      description.append("    '");
      description.append(     older.getValue());
      description.append(     "'\n");
      description.append("  <span class=\"label\">\n");
      description.append("    to\n");
      description.append("  </span>\n");
      description.append("    '");
      description.append(     newer.getValue());
      description.append(     "'\n");
      description.append("</p>\n");
    }
    
    return description;
  }

  private static StringBuffer describeEditor(History history)
  {
    StringBuffer description = new StringBuffer();
    
    if (history.getProxySubject() == null)
    {
      description.append(history.getGrantor().getName());
    }
    else
    {
      description.append(history.getProxySubject().getName());
      description.append(", acting as ");
      description.append(history.getGrantor().getName());
    }
    
    return description;
  }
  
  static private Difference diff
    (History newer,
     History older)
  {
    Difference diff;
    
    if (older == null)
    {
      // If it's the first record, it must be the initial grant.
      diff = Difference.GRANT;
    }
    else if (older.getStatus().equals(Status.ACTIVE)
             && newer.getStatus().equals(Status.INACTIVE))
    {
      diff = Difference.REVOKE;
    }
    else
    {
      diff = Difference.MODIFY;
    }
    
    return diff;
  }

  /**
   * @param log
   * @param request
   */
  public static void showHttpParams
  	(String prefix, Logger logger, HttpServletRequest request)
  {
    Enumeration paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements())
    {
      String paramName = (String)(paramNames.nextElement());
      String[] paramValues = request.getParameterValues(paramName);
      logger.log
      	(Level.WARNING,
      	 prefix + ": " + paramName + "=" + printArray(paramValues));
    }
  }
  
  private static String printArray(String[] items)
  {
    StringBuffer out = new StringBuffer();
    out.append("[");
    for (int i = 0; i < items.length; i++)
    {
      if (i > 0)
      {
        out.append(", ");
      }
      
      out.append(items[i]);
    }
    
    out.append("]");
    
    return out.toString();
  }
  
  private static LimitValue[] getLimitValuesArray(Set limitValues)
  {
    LimitValue limitValuesArray[] = new LimitValue[0];
    
    return
      (LimitValue[])(limitValues.toArray(limitValuesArray));
  }
  
  public static LimitValue[] getLimitValuesInDisplayOrder(Set limitValues)
  {
    LimitValue[] limitValuesArray = getLimitValuesArray(limitValues);
    Arrays.sort(limitValuesArray);
    return limitValuesArray;
  }

  public static LimitValue[] getLimitValuesInDisplayOrder
    (Assignment assignment)
  {
    return getLimitValuesInDisplayOrder(assignment.getLimitValues());
  }
  
  public static Limit[] getLimitsInDisplayOrder(Set limits)
  {
    Limit[] limitsArray = new Limit[0];
    limitsArray = (Limit[])(limits.toArray(limitsArray));
    
    if (limitsArray.length > 0)
    {
      Arrays.sort(limitsArray);
    }
    
    return limitsArray;
  }
  
  public static Choice[] getChoicesInDisplayOrder(ChoiceSet choiceSet)
  {
    Choice[] choiceArray = new Choice[0];
    choiceArray = (Choice[])(choiceSet.getChoices().toArray(choiceArray));
    
    if (choiceArray.length > 0)
    {
      Arrays.sort(choiceArray);
    }
    return choiceArray;
  }
  
  public static String displayLimitValues
    (Set limits,
     Set limitValues)
  {
    StringBuffer strBuf = new StringBuffer();
    
    LimitValue[] limitValuesSortedArray
      = getLimitValuesInDisplayOrder(limitValues);
    Limit[] limitsSortedArray = getLimitsInDisplayOrder(limits);

    for (int limitIndex = 0;
         limitIndex < limitsSortedArray.length;
         limitIndex++)
    {
      Limit limit = limitsSortedArray[limitIndex];
      strBuf.append((limitIndex > 0) ? "\n<br />\n" : "");
      strBuf.append("<span class=\"label\">" + limit.getName() + ":</span> ");

      int limitValuesPrinted = 0;
      for (int limitValueIndex = 0;
           limitValueIndex < limitValuesSortedArray.length;
           limitValueIndex++)
      {
        LimitValue limitValue = limitValuesSortedArray[limitValueIndex];
        if (limitValue.getLimit().equals(limit))
        {
          strBuf.append((limitValuesPrinted++ > 0) ? ", " : "");
          strBuf.append(limitValue.getDisplayValue());
        }
      }
    }
    
    return strBuf.toString();
  }

  /**
   * Formats Assignment-limit-values like this:
   *     <span class="label">Approval limit:<span> $100
   * 
   * Note that the colon is inside the span. The space between colon and
   * value can be inside or outside, whichever is easier.
   * 
   * @param grantable
   * @return
   */
  public static String displayLimitValues(Grantable grantable)
  {
    String outStr;
    
    if (grantable instanceof Assignment)
    {
      Assignment assignment = (Assignment)grantable;
      Set limits = assignment.getFunction().getLimits();
      Set limitValues = assignment.getLimitValues();
    
      outStr = displayLimitValues(limits, limitValues);
    }
    else
    {
      Proxy proxy = (Proxy)grantable;
      outStr
        = "<span class=\"label\">In </span>"
          + displaySubsystem(proxy);
    }
    
    return outStr;
  }
  
  /**
   * 
   * @param currentLoggedInPrivilegedSubject
   *    The current PrivilegedSubject who's logged in as the user.
   * @param htmlSelectId
   *    The HTML ID which should be used to identify this SELECT element.
   * @param onChange
   *    The name of the JavaScript method which should be invoked when
   *    this SELECT element's "onChange" event is fired. That method will
   *    receive two parameters:
   *      1) The SELECT element's HTML ID.
   *      2) The ID of the current PrivilegedSubject who's being "acted for".
   * @return
   *   A String which represents this SELECT element in HTML.
   */
  public static String displayActingForOptions
    (Signet             signet,
     SignetSubject      currentLoggedInPrivilegedSubject,
     String             htmlSelectId,
     String             onChange)
  {
    StringBuffer outStr = new StringBuffer();
    
    Set proxiesReceived
      = currentLoggedInPrivilegedSubject.getProxiesReceived();
    proxiesReceived = filterProxies(proxiesReceived, Status.ACTIVE);
    
    if (proxiesReceived.size() > 0)
    {    
      outStr.append("<LABEL for=\"" + htmlSelectId + "\">\n");
      outStr.append(ResLoaderUI.getString("Common.actas.txt"));
      outStr.append("</LABEL>\n");
      outStr.append("<SELECT\n");
      outStr.append("  name=\"" + htmlSelectId + "\"\n");
      outStr.append("  id=\"" + htmlSelectId + "\"\n");
      outStr.append("  onchange=\"" + onChange + "('" + htmlSelectId  + "', '" + Common.buildCompoundId(currentLoggedInPrivilegedSubject.getEffectiveEditor()) + "');" + "\"\n");
      outStr.append("  class=\"long\">\n");
        
      outStr.append(Common.displayProxyOptions(signet, currentLoggedInPrivilegedSubject));

      outStr.append("</SELECT>\n");
      outStr.append("<INPUT\n");
      outStr.append("  name=\"" + Constants.ACTAS_BUTTON_NAME + "\"\n");
      outStr.append("  id=\"" + Constants.ACTAS_BUTTON_ID + "\"\n");
      outStr.append("  disabled=\"true\"\n");
      outStr.append("  type=\"submit\"\n");
      outStr.append("  class=\"button1\"\n");
      outStr.append("  value=\"" + ResLoaderUI.getString("Common.actas.bt") + "\"\n");
      outStr.append("/>\n");

    }
    
    return outStr.toString();
  }
  
  /**
   * 
   * @param pSubject
   * @param actingAs
   * @param actingAsButtonId
   *    The ID of the "acting as" button.
   * @return
   */
  private static String displayProxyOptions
    (Signet            signet,
     SignetSubject     pSubject)
  {
    StringBuffer outStr = new StringBuffer();
    
    outStr.append
      ("<OPTION\n");
    
    if (pSubject.equals(pSubject.getEffectiveEditor()))
    {
      // We're acting as no one but ourselves.
      outStr.append
        ("  SELECTED\n");
    }

    outStr.append("  value=\"" + Common.buildCompoundId(pSubject) + "\"\n");
    outStr.append(">\n");
    outStr.append("  myself (" + pSubject.getName() + ")\n");
    outStr.append("</OPTION>\n");
    
    Set proxiesReceived = pSubject.getProxiesReceived();
    proxiesReceived = filterProxies(proxiesReceived, Status.ACTIVE);
    Iterator proxiesReceivedIterator = proxiesReceived.iterator();
    
    // Get a Set of unique Proxy-grantors to choose from.
    Set proxyGrantors = new TreeSet();
    while (proxiesReceivedIterator.hasNext())
    {
      Proxy proxy = (Proxy)(proxiesReceivedIterator.next());
      proxyGrantors.add(proxy.getGrantor());
    }
    
    Iterator proxyGrantorsIterator = proxyGrantors.iterator();
    while (proxyGrantorsIterator.hasNext())
    {
      SignetSubject grantor = (SignetSubject)proxyGrantorsIterator.next();

      outStr.append("<OPTION\n");

      if (grantor.equals(pSubject.getEffectiveEditor()))
        outStr.append(" SELECTED\n");
      
      String grantorName = grantor.getName();
      if (grantor.equals(signet.getSignetSubject()))
      {
        grantorName += " (admin/owner)";
      }
      outStr.append("  value=\"" + Common.buildCompoundId(grantor) + "\"\n");
      outStr.append(">\n");
      outStr.append("  " + grantorName + "\n");
      outStr.append("</OPTION>\n");
    }
    
    return outStr.toString();
  }
  
  public static String displayStatus(Grantable grantable)
  {
    String outStr;
    
    if (grantable instanceof Assignment)
    {
      outStr = displayStatus((Assignment)grantable);
    }
    else
    {
      outStr = displayStatus((Proxy)grantable);
    }
    
    return outStr;
  }
  
  public static String displayStatus(Assignment assignment)
  {
    StringBuffer statusStr = new StringBuffer();
    
    // An Assignment with no ID has not yet been persisted. An Assignment
    // that has not yet been persisted has no Status yet. That is, it's not
    // active, it's not pending, it's not nuthin' yet.
    boolean hasStatus = !(assignment.getId() == null);
    boolean canUse = assignment.canUse();
    boolean canGrant = assignment.canGrant();
    
    if (hasStatus)
    {
      statusStr.append(assignment.getStatus().getName());
    }
    
    if (hasStatus && canUse)
    {
      statusStr.append(", ");
    }
    
    if (canUse)
    {
      statusStr.append("can use");
    }
    
    if ((hasStatus || canUse) && canGrant)
    {
      statusStr.append(", ");
    }
    
    if (canGrant)
    {
      statusStr.append("can grant");
    }

    return statusStr.toString();
  }
  
  public static String displayStatusForDetailPopup(Grantable grantable)
  {
    StringBuffer statusStr = new StringBuffer();
    
    // An Assignment or Proxy with no ID has not yet been persisted. An
    // Assignment or Proxy that has not yet been persisted has no Status yet.
    // That is, it's not active, it's not pending, it's not nuthin' yet.
    boolean hasStatus = !(grantable.getId() == null);
    
    if (hasStatus)
    {
      statusStr.append(grantable.getStatus().getName());
    }

    return statusStr.toString();
  }
  
  public static String displayStatus(Proxy proxy)
  {
    StringBuffer statusStr = new StringBuffer();
    
    // A Proxy with no ID has not yet been persisted. A Proxy
    // that has not yet been persisted has no Status yet. That is, it's not
    // active, it's not pending, it's not nuthin' yet.
    boolean hasStatus = !(proxy.getId() == null);
    boolean canUse = proxy.canUse();
    boolean canExtend = proxy.canExtend();
    
    if (hasStatus)
    {
      statusStr.append(proxy.getStatus().getName());
    }
    
    if (hasStatus && canUse)
    {
      statusStr.append(", ");
    }
    
    if (canUse)
    {
      statusStr.append("can use");
    }
    
    if ((hasStatus || canUse) && canExtend)
    {
      statusStr.append(", ");
    }
    
    if (canExtend)
    {
      statusStr.append("can extend");
    }

    return statusStr.toString();
  }
  
  

// not used
//  // This is a shameful little hack to temporarily simulate person-quicksearch
//  // until it's implemented in the upcoming new version of the Subject interface:
//  public static SortedSet filterSearchResults
//  	(Set privilegedSubjects, String searchString)
//  {
//    SortedSet resultSet = new TreeSet();
//    Iterator privilegedSubjectsIterator = privilegedSubjects.iterator();
//    while (privilegedSubjectsIterator.hasNext())
//    {
//      PrivilegedSubject pSubject
//      	= (PrivilegedSubject)(privilegedSubjectsIterator.next());
//    
//      if ((searchString == null)
//          || (searchString.equals(""))
//          || (pSubject.getName().toUpperCase().indexOf
//               (searchString.toUpperCase())
//               	 != -1))
//      {
//        resultSet.add(pSubject);
//      }
//    }
//    
//    return resultSet;
//  }
  
  public static boolean isSelected
    (Limit  limit,
     Choice choice,
     Set    assignmentLimitValues)
  {
    return isSelected(limit, choice, assignmentLimitValues, null);
  }
  
  public static boolean isSelected
    (Limit  limit,
     Choice choice,
     Set    assignmentLimitValues,
     Choice defaultChoice)
  {
    boolean limitPreSelected = false;
    
    Iterator assignmentLimitValuesIterator = assignmentLimitValues.iterator();
    while (assignmentLimitValuesIterator.hasNext())
    {
      LimitValue limitValue
        = (LimitValue)(assignmentLimitValuesIterator.next());
      if (limitValue.getLimit().equals(limit))
      {
        limitPreSelected = true;
        if (choice.getValue().equals(limitValue.getValue()))
        {
          // This particular Limit-value should appear as pre-selected,
          // because it's already part of the specified Assignment-Limit-values.
          return true;
        }
      }
    }
    
    // If we've gotten this far, then this Limit-value should not appear as
    // pre-selected, UNLESS it's the default, and this Limit does not appear
    // in the Set of pre-selected AssignmentLimitValues.
    if (choice.equals(defaultChoice) && (limitPreSelected == false))
    {
      return true;
    }
    
    // If we've gotten this far, this Limit-value should not appear as
    // pre-selected.
    return false;
  }
  
  public static String editLink(SignetSubject loggedInPrivilegedSubject, Grantable grantable)
  {
    return editLink
      (loggedInPrivilegedSubject,
       grantable,
       "Edit",
       "float: right;");
  }
  
  public static String editLink
    (SignetSubject  loggedInPrivilegedSubject,
     Grantable          grantable,
     String             prompt,
     String             style)
  {
    StringBuffer outStr = new StringBuffer();
    String editAction
      = (grantable instanceof Proxy ? "Designate.do" : "Conditions.do");
    String paramName
      = (grantable instanceof Proxy
          ? Constants.PROXYID_HTTPPARAMNAME
          : "assignmentId");
    
    Decision decision = loggedInPrivilegedSubject.canEdit(grantable);
    boolean canEdit = decision.getAnswer();
    
    // Here's a notable exception: Since the UI cannot grant non-Subsystem-
    // specific Proxies, it cannot edit them either.
    
    if ((grantable instanceof Proxy)
        && (((Proxy)grantable).getSubsystem() == null))
    {
      canEdit = false; 
    }
    
    if (canEdit == true)
    {
      outStr.append("<a\n");
      if (style != null)
      {
        outStr.append("  style=\"" + style + "\"\n");
      }
      outStr.append("  href=\"" + editAction + "?" + paramName + "=" + grantable.getId() + "\">\n");
      outStr.append("  <img\n");
      outStr.append("    src=\"images/arrow_right.gif\"\n");
      outStr.append("      alt=\"\" />\n");
      outStr.append(     prompt + "\n");
      outStr.append("</a>");
    }
    
    return outStr.toString();
  }
  
  public static String paramStr(Grantable grantableInstance)
  {
    String typeStr;
    
    if (grantableInstance instanceof Assignment)
    {
      typeStr = Constants.ASSIGNMENT_HTTPPARAMPREFIX;
    }
    else
    {
      typeStr = Constants.PROXY_HTTPPARAMPREFIX;
    }
    
    return typeStr + grantableInstance.getId();
  }
  
  public static Grantable getGrantableFromParamStr
    (Signet signet, String paramStr)
  throws NumberFormatException, ObjectNotFoundException
  {
    Grantable grantableInstance;
    
    if (paramStr.startsWith(Constants.ASSIGNMENT_HTTPPARAMPREFIX))
    {
      String idStr
        = paramStr.substring(Constants.ASSIGNMENT_HTTPPARAMPREFIX.length());
      grantableInstance = signet.getPersistentDB().getAssignment(Integer.parseInt(idStr));
    }
    else
    {

      String idStr
        = paramStr.substring(Constants.PROXY_HTTPPARAMPREFIX.length());
      grantableInstance = signet.getPersistentDB().getProxy(Integer.parseInt(idStr));
    }
    
    return grantableInstance;
  }
  
  public static String revokeBox
    (SignetSubject  loggedInPrivilegedSubject,
     Grantable          grantableInstance,
     UnusableStyle      unusableStyle)
  {
    StringBuffer outStr = new StringBuffer();
    
    Decision decision = loggedInPrivilegedSubject.canEdit(grantableInstance);
    if (decision.getAnswer() == true)
    {
      outStr.append("<td align=\"center\" >\n");
      outStr.append("  <input\n");
      outStr.append("    name=\"revoke\"\n");
      outStr.append("    type=\"checkbox\"\n");
      outStr.append("    id=\"" + paramStr(grantableInstance) + "\"\n");
      outStr.append("    value=\"" + paramStr(grantableInstance) + "\"\n");
      outStr.append("    onclick=\"selectThis(this.checked);\" />\n");
      outStr.append("</td>");
    }
    else if (unusableStyle == UnusableStyle.TEXTMSG)
    {
      outStr.append("<td align=\"center\" class=\"status\" >\n");
      outStr.append("  You are not authorized to revoke this assignment.\n");
      outStr.append("</td>");
    }
    else // show the checkbox dimmed.
    {
      outStr.append("<td align=\"center\" >\n");
      outStr.append("  <input\n");
      outStr.append("    name=\"revoke\"\n");
      outStr.append("    type=\"checkbox\"\n");
      outStr.append("    id=\"" + paramStr(grantableInstance) + "\"\n");
      outStr.append("    value=\"" + paramStr(grantableInstance) + "\"\n");
      outStr.append("    disabled=\"true\"\n");
//      outStr.append("    title=\"" + revoker.editRefusalExplanation(assignment, "logged-in user") + "\"");
      outStr.append("/>");
      outStr.append("</td>");
    }
    
    return outStr.toString();
  }
  
  public static String popupIcon(Grantable grantable)
  {
    String outStr;
    
    if (grantable instanceof Assignment)
    {
      outStr = assignmentPopupIcon((Assignment)grantable);
    }
    else
    {
      outStr = proxyPopupIcon((Proxy)grantable);
    }
    
    return outStr;
  }
  
  public static String assignmentPopupIcon(Assignment assignment)
  {
    StringBuffer outStr = new StringBuffer();
    
    outStr.append("<a\n");
    outStr.append("  style=\"float: right;\"\n");
    outStr.append("  href\n");
    outStr.append("    =\"javascript:openWindow\n");
    outStr.append("        ('Assignment.do?assignmentId=" + assignment.getId() + "',\n");
    outStr.append("         'popup',\n");
    outStr.append("         'scrollbars=yes,\n");
    outStr.append("          resizable=yes,\n");
    outStr.append("          width=500,\n");
    outStr.append("          height=450');\">\n");
    outStr.append("  <img\n");
    outStr.append("   src=\"images/maglass.gif\"\n");
    outStr.append("   alt=\"More info about this assignment...\" />\n");
    outStr.append("</a>");
  
    return outStr.toString();
  }
  
  public static String proxyPopupIcon(Proxy proxy)
  {
    StringBuffer outStr = new StringBuffer();
    
    outStr.append("<a\n");
    outStr.append("  style=\"float: right;\"\n");
    outStr.append("  href\n");
    outStr.append("    =\"javascript:openWindow\n");
    outStr.append("        ('Proxy.do?proxyId=" + proxy.getId() + "',\n");
    outStr.append("         'popup',\n");
    outStr.append("         'scrollbars=yes,\n");
    outStr.append("          resizable=yes,\n");
    outStr.append("          width=500,\n");
    outStr.append("          height=450');\">\n");
    outStr.append("  <img\n");
    outStr.append("   src=\"images/maglass.gif\"\n");
    outStr.append("   alt=\"More info about this proxy designation...\" />\n");
    outStr.append("</a>");
  
    return outStr.toString();
  }

  public static String dateSelection
  (HttpServletRequest request,
   String             nameRoot,
   String             title,
   String             noDateLabel,
   String             dateValueLabel,
   Date               defaultDateValue)
  {
    return dateSelection
      (request,
       nameRoot,
       title,
       noDateLabel,
       dateValueLabel,
       defaultDateValue,
       true);  // editable
  }
  
  /**
   * This method emits some HTML which should be placed between a <tr> and a
   * </tr> tag.
   * 
   * @param nameRoot
   * @param title
   * @param noDateLabel
   * @param dateValueLabel
   * @param defaultDateValue
   * @return
   */
  public static String dateSelection
    (HttpServletRequest request,
     String             nameRoot,
     String             title,
     String             noDateLabel,
     String             dateValueLabel,
     Date               defaultDateValue,
     boolean            editable)
  {
    StringBuffer outStr = new StringBuffer();
    String defaultDateStr;
    
    
    if (defaultDateValue == null)
    {
      defaultDateStr = DATE_SAMPLE;
    }
    else
    {
      SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
      defaultDateStr = formatter.format(defaultDateValue);
    }
    
    String radioButtonGroupName = nameRoot + HASDATE_SUFFIX;
    
    outStr.append("<td>" + title + "</td>\n");
    outStr.append("<td>\n");
    
    outStr.append(displayErrorMessage(request, nameRoot));
    
    outStr.append("  <p>\n");
    outStr.append("    <input\n");
    outStr.append("      name=\"" + radioButtonGroupName + "\"\n");
    outStr.append("      type=\"radio\"\n");
    outStr.append("      value=\"" + NODATE_VALUE + "\"\n");
    if (editable == false)
    {
      outStr.append("      disabled=\"disabled\"\n");
    }
    outStr.append(       (defaultDateValue == null ? "checked" : "") + " />\n");
    outStr.append("    " + noDateLabel + "\n");
    outStr.append("  </p>\n");
    outStr.append("  <p>\n");
    outStr.append("    <input\n");
    outStr.append("      name=\"" + radioButtonGroupName + "\"\n");
    outStr.append("      type=\"radio\"\n");
    outStr.append("      value=\"" + YESDATE_VALUE + "\"\n");
    if (editable == false)
    {
      outStr.append("      disabled=\"disabled\"\n");
    }
    outStr.append(       (defaultDateValue != null ? "checked" : "") + " />\n");
    outStr.append("    " + dateValueLabel + "\n");
    outStr.append("    <input\n");
    outStr.append("      name=\"" + nameRoot + DATETEXT_SUFFIX + "\"\n");
    outStr.append("      type=\"text\"\n");
    outStr.append("      class=\"date\"\n");
    outStr.append("      value=\"" + defaultDateStr + "\"\n");
    if (editable == false)
    {
      outStr.append("      disabled=\"disabled\"\n");
    }
    outStr.append("      onfocus=\n");
    outStr.append("        \"if (this.value == '" + DATE_SAMPLE + "') this.value='';\n");
    outStr.append("        this.style.color='black';\n");
    outStr.append("        this.form." + radioButtonGroupName + "[0].checked=false;\n");
    outStr.append("        this.form." + radioButtonGroupName + "[1].checked='checked'\"/>\n");
    outStr.append("  </p>\n");
    outStr.append("</td>\n");
    
    return outStr.toString();
  }
  
  /*
   * This code is copied from:
   * 
   *    http://javaboutique.internet.com/tutorials/excep_struts/index-3.html
   */
  private static String displayErrorMessage
    (HttpServletRequest request,
     String             nameRoot)
  {
    StringBuffer outStr = new StringBuffer();
    // Get the ActionMessages 
    Object o = request.getAttribute(Globals.MESSAGE_KEY);
    
    if (o != null)
    {
      ActionMessages actionMessages = (ActionMessages)o;

      // Get the locale and message resources bundle
// DMD - not used
//      Locale locale = 
//        (Locale)(request.getSession().getAttribute(Globals.LOCALE_KEY));
//      MessageResources messageResources
//        = (MessageResources)(request.getAttribute(Globals.MESSAGES_KEY));
        request.getSession().getAttribute(Globals.LOCALE_KEY);
        request.getAttribute(Globals.MESSAGES_KEY);

      // Loop thru all the labels in the ActionMessages  
      for (Iterator actionMessagesProperties = actionMessages.properties();
           actionMessagesProperties.hasNext();)
      {
        String property = (String)actionMessagesProperties.next();
        if (property.equals(nameRoot))
        {
          // TO DO - This code should really retrieve the text of the
          // error-message from the ActionMessage. For right now, I'll
          // just hard-code that error-text.
          outStr.append("<FONT COLOR=\"RED\">\n");
          outStr.append("  <br/>\n");
          outStr.append("  Dates must be in the format '" + DATE_SAMPLE + "'.\n");
          outStr.append("  Please try again.");
          outStr.append("  <br/>\n");
          outStr.append("</FONT>\n");

//          // Get all messages for this label
//          for (Iterator propertyValuesIterator = actionMessages.get(property);
//               propertyValuesIterator.hasNext();)
//          {
//            ActionMessage actionMessage
//              = (ActionMessage)propertyValuesIterator.next();
//            String key = actionMessage.getKey();
//            Object[] values = actionMessage.getValues();
//            String messageStr = messageResources.getMessage(locale, key, values);
//            outStr.append(messageStr);
//            outStr.append("<br/>");
//          }
        }
      }
    }
    
    return outStr.toString();
  }
  
  public static String dateSelection(String nameRoot, Date date)
  {
    Calendar calDate;
    int dayNumber   = -1;
    int monthNumber = -1;
    int yearNumber  = -1;
    Calendar calCurrentDate = Calendar.getInstance();
    calCurrentDate.setTime(new Date());
    int currentYear = calCurrentDate.get(Calendar.YEAR);
    
    if (date != null)
    {
      calDate = Calendar.getInstance();
      calDate.setTime(date);
      dayNumber = calDate.get(Calendar.DAY_OF_MONTH);
      monthNumber = calDate.get(Calendar.MONTH) + 1;
      yearNumber = calDate.get(Calendar.YEAR);
    }
    
    StringBuffer outStr = new StringBuffer();
    
    outStr.append("<select name=\"" + nameRoot + DAY_SUFFIX + "\">\n");
    outStr.append("  <option value=\"none\"></option>\n");
    
    for (int i = 1; i <= 31; i++)
    {
      outStr.append
        ("  <option" + (i == dayNumber ? " selected=\"selected\"" : "") + ">"
            + i
            + "</option>\n");
    }
    
    outStr.append("</select>\n");
    
    outStr.append("<select name=\"" + nameRoot + MONTH_SUFFIX + "\">\n");
    outStr.append("  <option value=\"none\"></option>\n");
    
    SimpleDateFormat dateFormat = new SimpleDateFormat();
    dateFormat.applyPattern("MMMMM");
    
    for (int i = 1; i <= 12; i++)
    {
      Calendar monthValue = Calendar.getInstance();
      monthValue.set(Calendar.MONTH, i-1);
      Date monthDate = monthValue.getTime();
      String monthName = dateFormat.format(monthDate).toString();
      outStr.append
        ("  <option"
            + (i == monthNumber ? " selected=\"selected\"" : "")
            + " label=\"" + monthName + "\""
            + " value=\"" + i + "\""
            + ">"
            + monthName
            + "</option>\n");
    }
    
    outStr.append("</select>\n");
    
    outStr.append("<select name=\"" + nameRoot + YEAR_SUFFIX + "\">\n");
    outStr.append("  <option value=\"none\"></option>\n");
    
    for (int i = (yearNumber == -1 ? currentYear : yearNumber);
         i < ((yearNumber == -1 ? currentYear : yearNumber) + 4);
         i++)
    {
      outStr.append
        ("  <option" + (i == yearNumber ? " selected=\"selected\"" : "") + ">"
            + i
            + "</option>\n");
    }
    
    outStr.append("</select>\n");
    
    return outStr.toString();
  }
  
  static public Date getDateParam
    (HttpServletRequest request,
     String             nameRoot)
  throws DataEntryException
  {
    return getDateParam
      (request,
       nameRoot,
       null);
  }
  
  static public Date getDateParam
    (HttpServletRequest request,
     String             nameRoot,
     Date               defaultDate)
  throws DataEntryException
  {
    Date            date = null;
    ParseException  parseException = null;
    
    // First, let's see if the date is present or not.
    String dateStringPresence = request.getParameter(nameRoot + HASDATE_SUFFIX);
    if ((dateStringPresence == null)
        || dateStringPresence.equals(NODATE_VALUE)
        || dateStringPresence.equals(""))
    {
      return defaultDate;
    }
    
    String dateStr = request.getParameter(nameRoot + DATETEXT_SUFFIX);

    // This date-parsing code was originally written by Craig Jurney for the
    // Sponsorship Manager.
    for (int i = 0; i < DATERS.length && date == null; ++i)
    {
      DATERS[i].setLenient(false);
      
      try
      {
        date = DATERS[i].parse(dateStr);
      }
      catch (java.text.ParseException pe)
      {
        parseException = pe;
        continue;
      }
    }

    if (date == null)
    {
      throw new DataEntryException
        (parseException, nameRoot, dateStr, DATE_SAMPLE);
    }
    
    return date;
  }
  
//  static public String subsystemSelectionMultiple
//    (PrivilegedSubject loggedInPrivilegedSubject,
//     PrivilegedSubject actingAs,
//     String groupName,
//     String onClickAction)
//  {
//    StringBuffer outStr = new StringBuffer();
//
//    PrivilegedSubject pSubject
//      = (actingAs == null ? loggedInPrivilegedSubject : actingAs);
//    Set grantableSubsystems = pSubject.getGrantableSubsystems();
//    
//    Iterator grantableSubsystemsIterator = grantableSubsystems.iterator();
//    while (grantableSubsystemsIterator.hasNext())
//    {
//      Subsystem subsystem = (Subsystem)(grantableSubsystemsIterator.next());
//      outStr.append("<input\n");
//      outStr.append("  name=\"" + groupName + "\"\n");
//      outStr.append("  type=\"checkbox\"\n");
//      
//      if (onClickAction != null)
//      {
//        outStr.append("  onClick=\"" + onClickAction + "\"\n");
//      }
//      
//      outStr.append("  value=\"" + subsystem.getId() + "\" />\n");
//      outStr.append(subsystem.getName() + "\n");
//      outStr.append("<br />\n");
//    }
//    
//    return outStr.toString();
//  }
  
  static public String buildCompoundId(SignetSubject pSubject)
  {
    return (new String(pSubject.getSourceId() + Constants.COMPOSITE_ID_DELIMITER + pSubject.getId()));
  }
  
  static public String[] parseCompoundId(String compoundId)
  {
	return ((null != compoundId) ?
			(compoundId.split(Constants.COMPOSITE_ID_DELIMITER)) :
			(new String[2]));
// StringTok = bad, String.split = good
//    StringTokenizer tokenizer
//      = new StringTokenizer(compoundId, Constants.COMPOSITE_ID_DELIMITER);
//    String subjectTypeId = tokenizer.nextToken();
//    String subjectId = tokenizer.nextToken();
//
//    String[] result = {subjectTypeId, subjectId};
//
//    return result;
  }

  static public boolean paramIsPresent(String param)
  {
	  return ((param != null) && (0 < param.length()));
//    if ((param != null) && (param != ""))
//    {
//      return true;
//    }
//
//    return false;
  }
  

// not used
//  static public PrivilegedSubject getSubjectFromSelectList
//    (Signet             signet,
//     HttpServletRequest request,
//     String             selectListName,
//     String             sessionAttrName)
//  throws ObjectNotFoundException
//  {
//    PrivilegedSubject pSubject = null;
//    
//    String compositeId = request.getParameter(selectListName);
//    if (compositeId != null)
//    {
//      String[] idParts = parseCompoundId(compositeId);
//      pSubject = signet.getSubjectSources().getPrivilegedSubject(idParts[0], idParts[1]);
//    }
//    
//    if (sessionAttrName != null)
//    {
//      request.getSession().setAttribute(sessionAttrName, pSubject);
//    }
//    
//    return pSubject;
//  }
  
  	/**
	 * Find the PrivilegedSubject specified by the "grantee" parameters, and stash
	 * it in the Session. If those parameters are not present, then it must already
	 * be stashed in the Session by some prior action.
	 * @param signet This instance of Signet (context)
	 * @param request The Request
	 * @return The Subject that is the Grantee
	 * @throws ObjectNotFoundException
	 */
  static public SignetSubject getGrantee(Signet signet, HttpServletRequest request)
				throws ObjectNotFoundException
  {
    SignetSubject grantee;

    String granteeSourceId = request.getParameter(Constants.SIGNET_SOURCE_ID_HTTPPARAMNAME);
    String granteeSubjectId = request.getParameter(Constants.SIGNET_SUBJECT_ID_HTTPPARAMNAME);

    if (null != granteeSubjectId)
    {
      grantee = signet.getSubject(granteeSourceId, granteeSubjectId);
      request.getSession().setAttribute(Constants.CURRENTPSUBJECT_ATTRNAME, grantee);
    }
    else
    {
      grantee = (SignetSubject)request.getSession().getAttribute(Constants.CURRENTPSUBJECT_ATTRNAME);
    }

    return grantee;
  }
  
  static public Subsystem getSubsystem
    (Signet             signet,
     HttpServletRequest request,
     String             paramName,
     String             attrName)
  throws ObjectNotFoundException
  {    
    Subsystem subsystem = null;
    
    String subsystemId = request.getParameter(paramName);
    if ((subsystemId != null)
        && (!subsystemId.equals(Constants.SUBSYSTEM_PROMPTVALUE)))
    {
      subsystem = signet.getPersistentDB().getSubsystem(subsystemId);
      request.getSession().setAttribute(attrName, subsystem);
    }
    else
    {
      subsystem
        = (Subsystem)
            (request.getSession().getAttribute(attrName));
    }
    
    return subsystem;
  }
  
  static public PrivDisplayType getAndSetPrivDisplayType
    (HttpServletRequest request,
     String             paramName,
     String             attrName,
     PrivDisplayType    defaultValue)
  {
    PrivDisplayType privDisplayType = null;
    
    String privDisplayTypeName = request.getParameter(paramName);
    if (privDisplayTypeName != null)
    {
      privDisplayType
        = (PrivDisplayType)
            (TypeSafeEnumeration.getInstanceByName(privDisplayTypeName));
    }
    else
    {
      privDisplayType
        = (PrivDisplayType)
            (request.getSession().getAttribute(attrName));
    }
    
    if (privDisplayType == null)
    {
      privDisplayType = defaultValue;
    }

    request.getSession().setAttribute(attrName, privDisplayType);
    return privDisplayType;
  }
  
  static public Subsystem getAndSetSubsystem
    (Signet             signet,
     HttpServletRequest request,
     String             paramName,
     String             attrName,
     Subsystem          defaultValue)
  throws ObjectNotFoundException
  {
    Subsystem subsystem = null;
    
    String subsystemId = request.getParameter(paramName);
    
    // If there's no subsystemID at all, it means we should look for a Subsystem
    // in the Session. If there is a subsystemID, and its value is the four-
    // character string "null", then it denotes the wildcard subsystem. This is
    // not so great, because a Signet installation could have a legitimate
    // Subsystem whose ID is that same four-character string. Also, it's
    // unnecessarily confusing.
    if (subsystemId != null)
    {
      if (subsystemId.equals("null"))
      {
        subsystem = Constants.WILDCARD_SUBSYSTEM;
      }
      else
      {
        subsystem = signet.getPersistentDB().getSubsystem(subsystemId);
      }
    }
    else
    {
      subsystem
        = (Subsystem)
            (request.getSession().getAttribute(attrName));
    }
    
    if (subsystem == null)
    {
      subsystem = defaultValue;
    }

    request.getSession().setAttribute(attrName, subsystem);
    return subsystem;
  }
  
  static public SignetSubject getAndSetPrivilegedSubject
    (Signet             signet,
     HttpServletRequest request,
     String             paramName,
     String             attrName,
     SignetSubject  defaultValue)
  throws ObjectNotFoundException
  {
    SignetSubject pSubject = null;
    
    String compoundSubjectId = request.getParameter(paramName);
    
    // If there's no compoundSubjectID at all, it means we should look for a
    // PrivilegedSubject in the Session.
    if (compoundSubjectId != null)
    {
      String subjectIdParts[] = parseCompoundId(compoundSubjectId);
      pSubject = signet.getSubject(subjectIdParts[0], subjectIdParts[1]);
    }
    else
    {
      pSubject = (SignetSubject)request.getSession().getAttribute(attrName);
    }
    
    if (pSubject == null)
    {
      pSubject = defaultValue;
    }

    request.getSession().setAttribute(attrName, pSubject);
    return pSubject;
  }
  
  static public Set getSubsystemSelections
    (Signet signet,
     HttpServletRequest request,
     String             groupName)
  throws ObjectNotFoundException
  {
    Set subsystems = new HashSet();
    String subsystemIds[] = request.getParameterValues(groupName);
    
    for (int i = 0; i < 0; i++)
    {
      Subsystem subsystem = signet.getPersistentDB().getSubsystem(subsystemIds[i]);
      subsystems.add(subsystem);
    }
    
    return subsystems;
  }
  
  static public String subsystemSelectionSingle
    (String selectName,
     String promptValue,
     String promptText,
     String onClickScript,
     Set    subsystems)
  {
    return subsystemSelectionSingle
      (selectName, promptValue, promptText, onClickScript, subsystems, null);
  }
  
  
  static public String subsystemSelectionSingle
    (String     selectName,
     String     promptValue,
     String     promptText,
     String     onChangeScript,
     Set        subsystems,
     Subsystem  selectedSubsystem)
  {
    StringBuffer outStr = new StringBuffer();
    
    outStr.append("<span style=\"white-space: nowrap;\">\n");
    outStr.append("  <!-- keep select & button together -->\n");
    outStr.append("  <select\n");
    outStr.append("    name=\"" + selectName + "\"\n");
    outStr.append("    id=\"" + selectName + "\"\n");
    outStr.append("    onchange=\"" + onChangeScript + "\">\n");

    outStr.append("    <option\n");
    
    if (selectedSubsystem == null)
    {
      outStr.append("      selected=\"selected\"\n");
    }
    
    outStr.append("      value=\"" + promptValue + "\">\n");
    outStr.append("      " + promptText + "\n");
    outStr.append("    </option>\n");

    Iterator subsystemsIterator = subsystems.iterator();
    while (subsystemsIterator.hasNext())
    {
      Subsystem subsystem = (Subsystem)(subsystemsIterator.next());

      outStr.append("    <option\n");
      
      if (subsystem.equals(selectedSubsystem))
      {
        outStr.append("      selected=\"selected\"\n");
      }
      
      outStr.append("      value=\"" + subsystem.getId() + "\">\n");
      outStr.append("      " + subsystem.getName() + "\n");
      outStr.append("    </option>\n");
    }

    outStr.append("  </select>\n");
    outStr.append("</span>\n");
    
    return outStr.toString();
  }
  
  static public String displayLogoutHref(HttpServletRequest request)
  {
    return "<a href=\"Logout.do\">" + ResLoaderUI.getString("Common.logout.href") + "</a>\n";
  }
  
  static public String displayOption
    (PrivDisplayType option,
     PrivDisplayType currentInEffect)
  {
    StringBuffer outStr = new StringBuffer();
    
    outStr.append("<option");

    if (option == currentInEffect)
    {
      outStr.append(" selected=\"selected\"");
    }

    outStr.append(" value=\"" + option.name + "\"");
    outStr.append(">");
    outStr.append(option.getDescription());
    outStr.append("</option>\n");
    
    return outStr.toString();
  }
  
  static public String subsystemLinks
    (SignetSubject  pSubject,
     PrivDisplayType    privDisplayType,
     Subsystem          currentSubsystem)
  {
    Set subsystems;
    Set proxies;
    Set assignments;
    
    if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED))
    {
      proxies = pSubject.getProxiesGranted();
      proxies = filterProxies(proxies, Status.ACTIVE);
      
      assignments = pSubject.getAssignmentsGranted();
      assignments = filterAssignments(assignments, Status.ACTIVE);
    }
    else if (privDisplayType.equals(PrivDisplayType.FORMER_GRANTED))
    {
      proxies = pSubject.getProxiesGranted();
      proxies = filterProxies(proxies, Status.INACTIVE);
      
      assignments = pSubject.getAssignmentsGranted();
      assignments = filterAssignments(assignments, Status.INACTIVE);
    }
    else if (privDisplayType.equals(PrivDisplayType.CURRENT_RECEIVED))
    {
      proxies = pSubject.getProxiesReceived();
      proxies = filterProxies(proxies, Status.ACTIVE);
      
      assignments = pSubject.getAssignmentsReceived();
      assignments = filterAssignments(assignments, Status.ACTIVE);
    }
    else if (privDisplayType.equals(PrivDisplayType.FORMER_RECEIVED))
    {
      proxies = pSubject.getProxiesReceived();
      proxies = filterProxies(proxies, Status.INACTIVE);
      
      assignments = pSubject.getAssignmentsReceived();
      assignments = filterAssignments(assignments, Status.INACTIVE);
    }
    else
    {
        throw new IllegalArgumentException
          ("This method needs to be extended to support a PrivDisplayType"
           + " value of '"
           + privDisplayType
           + "'");
    }
    
    subsystems = getSubsystems(assignments, proxies);
    
    StringBuffer outStr = new StringBuffer();
    outStr.append(subsystemLink(Constants.WILDCARD_SUBSYSTEM, currentSubsystem));
    
    Iterator subsystemsIterator = subsystems.iterator();
    while (subsystemsIterator.hasNext())
    {
      outStr.append(" | ");
      Subsystem subsystem = (Subsystem)(subsystemsIterator.next());
      outStr.append(subsystemLink(subsystem, currentSubsystem));
    }
    
    return outStr.toString();
  }
  
  private static String subsystemLink
    (Subsystem subsystem,
     Subsystem currentSubsystem)
  {
    StringBuffer outStr = new StringBuffer();
    String name;
    
    if (subsystem.equals(Constants.WILDCARD_SUBSYSTEM))
    {
      name = "All";
    }
    else
    {
      name = subsystem.getName();
    }

    if (subsystem.equals(currentSubsystem))
    {
      outStr.append("<b>");
      outStr.append(name);
      outStr.append("</b>");
    }
    else
    {
      outStr.append("<a href=\"PersonView.do?");
      outStr.append(Constants.SUBSYSTEM_HTTPPARAMNAME);
      outStr.append("=");
      outStr.append(subsystem.getId());
      outStr.append("\">");
      outStr.append(name);
      outStr.append("</a>");
    }
    
    return outStr.toString();
  }
  
  // Tosses out any NULL Subsystems.
  private static Set getSubsystems
    (Set assignments,
     Set proxies)
  {
    Set subsystems = new TreeSet(new SubsystemNameComparator());
    
    Iterator assignmentsIterator = assignments.iterator();
    while (assignmentsIterator.hasNext())
    {
      Assignment assignment = (Assignment)(assignmentsIterator.next());
      subsystems.add(assignment.getFunction().getSubsystem());
    }
    
    Iterator proxiesIterator = proxies.iterator();
    while (proxiesIterator.hasNext())
    {
      Proxy proxy = (Proxy)(proxiesIterator.next());
      
      if (proxy.getSubsystem() != null)
      {
        subsystems.add(proxy.getSubsystem());
      }
    }
    
    return subsystems;
  }
  
  public static String displaySubsystem(Proxy proxy)
  {
    String    displayStr;
    Subsystem subsystem = proxy.getSubsystem();
    
    if ((subsystem == null) || (subsystem == Constants.WILDCARD_SUBSYSTEM))
    {
      displayStr = "any subsystem";
    }
    else
    {
      displayStr = subsystem.getName();
    }
    
    return displayStr;
  }
  
  private static Set getAssignmentsGrantedForReport
    (SignetSubject  pSubject,
     Subsystem          subsystem,
     Set                statusSet)
  {
    Set assignments = pSubject.getAssignmentsGranted();
    assignments = filterAssignments(assignments, statusSet);
    assignments = filterAssignments(assignments, subsystem);
    
    return assignments;
  }
  
  private static Set getAssignmentsReceivedForReport
    (SignetSubject  pSubject,
     Subsystem          subsystem,
     Set                statusSet)
  {
    Set assignments = pSubject.getAssignmentsReceived();
    assignments = filterAssignments(assignments, statusSet);
    assignments = filterAssignments(assignments, subsystem);
    
    return assignments;
  }
  
  private static Set getProxiesGrantedForReport
    (SignetSubject  pSubject,
     Subsystem          subsystem,
     Set                statusSet)
  {
    Set proxies = pSubject.getProxiesGranted();
    proxies = filterProxies(proxies, statusSet);
    proxies = filterProxies(proxies, subsystem);
    
    return proxies;
  }
  
  public static boolean hasUsableProxies(SignetSubject pSubject)
  {
    Set usableProxies = pSubject.getProxiesReceived();
    usableProxies = filterProxies(usableProxies, Status.ACTIVE);
    usableProxies = filterProxiesByUsable(usableProxies);
    
    if (usableProxies.size() > 0)
    {
      return true;
    }
    
    return false;
  }
  
  public static boolean hasExtensibleProxies(SignetSubject pSubject)
  {
    Set extensibleProxies = pSubject.getProxiesReceived();
    extensibleProxies = filterProxies(extensibleProxies, Status.ACTIVE);
    extensibleProxies = filterProxiesByExtensible(extensibleProxies);
    
    if (extensibleProxies.size() > 0)
    {
      return true;
    }
    
    return false;
  }
  
  private static Set getProxiesReceivedForReport
    (SignetSubject  pSubject,
     Subsystem          subsystem,
     Set                statusSet)
  {
    Set proxies = pSubject.getProxiesReceived();
    proxies = filterProxies(proxies, statusSet);
    proxies = filterProxies(proxies, subsystem);
    
    return proxies;
  }
  
  public static SortedSet getGrantablesForReport
    (SignetSubject  pSubject,
     Subsystem          subsystemFilter,
     PrivDisplayType    privDisplayType)
  {
    SortedSet grantables = new TreeSet(grantableReportComparator);
    
    if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED))
    {
      grantables.addAll
        (getAssignmentsGrantedForReport
          (pSubject, subsystemFilter, activeAndPendingStatus));

      grantables.addAll
        (getProxiesGrantedForReport
            (pSubject, subsystemFilter, activeAndPendingStatus));
    }
    else if (privDisplayType.equals(PrivDisplayType.CURRENT_RECEIVED))
    {
      grantables.addAll
        (getAssignmentsReceivedForReport
          (pSubject, subsystemFilter, activeAndPendingStatus));

      grantables.addAll
        (getProxiesReceivedForReport
          (pSubject, subsystemFilter, activeAndPendingStatus));
    }
    else if (privDisplayType.equals(PrivDisplayType.FORMER_GRANTED))
    {
      grantables.addAll
        (getAssignmentsGrantedForReport
          (pSubject, subsystemFilter, inactiveStatus));

      grantables.addAll
        (getProxiesGrantedForReport
            (pSubject, subsystemFilter, inactiveStatus));
    }
    else if (privDisplayType.equals(PrivDisplayType.FORMER_RECEIVED))
    {
      grantables.addAll
        (getAssignmentsReceivedForReport
          (pSubject, subsystemFilter, inactiveStatus));

      grantables.addAll
        (getProxiesReceivedForReport
          (pSubject, subsystemFilter, inactiveStatus));
    }
    else
    {
      throw new IllegalArgumentException
        ("Unrecognized PrivDisplayTypeValue: " + privDisplayType);
    }
  
    return grantables;
  }
  
  public static Set removeGroups(Set setWithGroups)
  {
    Set setWithoutGroups = new HashSet();
    Iterator setWithGroupsIterator = setWithGroups.iterator();
    while (setWithGroupsIterator.hasNext())
    {
      SignetSubject candidate = (SignetSubject)setWithGroupsIterator.next();
      
      if (!(candidate.getType().getName().equals
             (SubjectTypeEnum.GROUP.getName())))
      {
        setWithoutGroups.add(candidate);
      }
    }
    
    return setWithoutGroups;
  }
  
  public static String proxyPrivilegeDisplayName(Signet signet, Proxy proxy)
  {
    StringBuffer displayName = new StringBuffer("Signet : ");
    
    if (proxy.getGrantor().equals(signet.getSignetSubject()))
    {
      if (proxy.getSubsystem() == null)
      {
        displayName.append("System Administrator : Act as Signet to designate subsystem owners");
      }
      else
      {
        displayName.append("Subsystem Owner : Act as Signet to grant top-level privileges");
      }
    }
    else
    {
      displayName.append("Proxy : Grant privileges as ");
      displayName.append(proxy.getGrantor().getName());
    }
    
    return displayName.toString();
  }
  /*
   * A System Administrator is a user who has an extensible, non-usable,
   * non-Subsystem-specific Proxy from the Signet subject, and is currently
   * "acting as" the Signet subject.
   * 
   * It's not good enough to be some other user who's "acting as" the
   * System Administrator.
   */
  public static boolean isSystemAdministrator
    (Signet             signet,
     SignetSubject  pSubject)
  {
    if (pSubject.getEffectiveEditor().equals(signet.getSignetSubject()))
    {
      Set proxiesReceived = pSubject.getProxiesReceived();
      proxiesReceived = filterProxies(proxiesReceived, Status.ACTIVE);
      proxiesReceived
        = filterProxiesByGrantor(proxiesReceived, signet.getSignetSubject());
      Iterator proxiesReceivedIterator = proxiesReceived.iterator();
      while (proxiesReceivedIterator.hasNext())
      {
        Proxy proxyReceived = (Proxy)(proxiesReceivedIterator.next());
        if (proxyReceived.getSubsystem() == null)
        {
          // Yes, we have an active, non-Subsystem-specific Proxy from
          // the Signet subject.
          if ((proxyReceived.canExtend() == true)
              && (proxyReceived.canUse() == false))
          {
            // Yes that active, non-Subsystem-specific Proxy is extensible,
            // and non-usable.
            return true;
          }
        }
      }
    }
    
    // If we've gotten this far, we didn't pass the test.
    return false;
  }


  public static Set filterProxiesByGrantor(Set proxies, SignetSubject grantor)
  {
    Set filteredSet = Collections.synchronizedSet(new HashSet());
    synchronized(filteredSet)
    {
	    for (Iterator iter = proxies.iterator(); iter.hasNext();)
	    {
	      Proxy candidate = (Proxy)(iter.next());
	
	      if (grantor.equals(candidate.getGrantor()))
	        filteredSet.add(candidate);
	    }
    }

    return filteredSet;
  }


	private static String timeWord(PrivDisplayType type, boolean initialCap)
	{
		StringBuffer retval = null;
	
	    if (type.equals(PrivDisplayType.CURRENT_GRANTED) || type.equals(PrivDisplayType.CURRENT_RECEIVED))
			retval = new StringBuffer(ResLoaderUI.getString("Common.timeword.current.txt"));
	
	    else if (type.equals(PrivDisplayType.FORMER_GRANTED) || type.equals(PrivDisplayType.FORMER_RECEIVED))
			retval = new StringBuffer(ResLoaderUI.getString("Common.timeword.former.txt"));
	
	    else
	    	return ("UNRECOGNIZED: PrivilegedDisplayType.getName()='" + type.getName() + "'");
	
	    if (initialCap)
	    	retval.setCharAt(0, Character.toUpperCase(retval.charAt(0)));

	    return (retval.toString());
	}


	private static String directionPhrase(PrivDisplayType type)
	{
		String retval = null;

		if (type.equals(PrivDisplayType.CURRENT_GRANTED) || type.equals(PrivDisplayType.FORMER_GRANTED))
			retval = ResLoaderUI.getString("Common.directionPhrase.by.txt");

		else if (type.equals(PrivDisplayType.CURRENT_RECEIVED) || type.equals(PrivDisplayType.FORMER_RECEIVED))
			retval = ResLoaderUI.getString("Common.directionPhrase.to.txt");

		else
			retval = new String("UNRECOGNIZED: PrivilegedDisplayType.getName()='" + type.getName() + "'");

		return (retval);
	}


	/**
	 * Creates a title string for personview-print.jsp. Note this method makes
	 * use of properties Common.titleForPrintReport.all and Common.titleForPrintReport.fmt
	 * to construct the returned string.
	 * @param subsystem The subsystem
	 * @param privDisplayType The display type
	 * @param pSubject The subject
	 * @return The formatted string
	 */
	public static String titleForPrintReport(Subsystem subsystem,
			PrivDisplayType privDisplayType,
			SignetSubject  pSubject)
	{
		Object[] fmtData = new String[4];
	
		if (Constants.WILDCARD_SUBSYSTEM == subsystem)
		{
			fmtData[0] = ResLoaderUI.getString("Common.titleForPrintReport.all");
			fmtData[1] = timeWord(privDisplayType, false);
		}
		else
		{
			fmtData[0] = timeWord(privDisplayType, true);
			fmtData[1] = subsystem.getName();
		}
		
		fmtData[2] = directionPhrase(privDisplayType);
		fmtData[3] = pSubject.getName();
		
		String outStr = MessageFormat.format(
				ResLoaderUI.getString("Common.titleForPrintReport.fmt"),
				fmtData);
	
		return (outStr);
	}


  public static Set filterProxies(Set all, Status status)
  {
    Set statusSet = new HashSet();
    statusSet.add(status);
    return filterProxies(all, statusSet);
  }
  
  public static Set filterAssignments(Set all, Status status)
  {
    Set statusSet = new HashSet();
    statusSet.add(status);
    return filterAssignments(all, statusSet);
  }


  static Set filterAssignments(Set all, Set statusSet)
  {
    if ((null == statusSet) || statusSet.isEmpty() || (null == all) || all.isEmpty())
      return all;
	  
    Set subset = Collections.synchronizedSet(new HashSet());
    synchronized (subset)
    {
	    for (Iterator iterator = all.iterator(); iterator.hasNext();)
	    {
	      Assignment candidate = (Assignment)iterator.next();
	      if (statusSet.contains(candidate.getStatus()))
	        subset.add(candidate);
	    }
    }

    return subset;
  }


  public static Set filterAssignments(Set all, Subsystem subsystem)
  {
    if ((null == subsystem) || (null == all) || all.isEmpty())
      return all;

    Set subset = Collections.synchronizedSet(new HashSet());
    synchronized (subset)
    {
	    for (Iterator iter = all.iterator(); iter.hasNext();)
	    {
	      Assignment candidate = (Assignment)iter.next();
	      if (candidate.getFunction().getSubsystem().equals(subsystem))
	        subset.add(candidate);
	    }
    }

    return subset;
  }

  public static Set filterAssignments(Set all, Function function)
  {
    if (function == null)
    {
      return all;
    }

    Set subset = new HashSet();
    Iterator iterator = all.iterator();
    while (iterator.hasNext())
    {
      Assignment candidate = (Assignment) (iterator.next());
      if (candidate.getFunction().equals(function))
      {
        subset.add(candidate);
      }
    }

    return subset;
  }

  static Set filterProxies(Set all, Set statusSet)
  {
    if ((null == statusSet) || statusSet.isEmpty() || (null == all) || all.isEmpty())
      return all;
	  
    Set subset = Collections.synchronizedSet(new HashSet());
    synchronized (subset)
    {
	    for (Iterator iterator = all.iterator(); iterator.hasNext();)
	    {
	      Proxy candidate = (Proxy)iterator.next();
	      if (statusSet.contains(candidate.getStatus()))
	        subset.add(candidate);
		}
	}

    return subset;
  }

  public static Set filterProxies(Set all, Subsystem subsystem)
  {
    if ((null == subsystem) || (null == all) || all.isEmpty())
      return all;
 
    Set subset = Collections.synchronizedSet(new HashSet());
    synchronized (subset)
    {
	    for (Iterator iterator = all.iterator(); iterator.hasNext();)
	    {
	      Proxy candidate = (Proxy) (iterator.next());
	      if ((candidate.getSubsystem() == null)
	          || candidate.getSubsystem().equals(subsystem))
	      {
	        subset.add(candidate);
	      }
	    }
    }

    return subset;
  }
  
  private static Set filterProxiesByUsable(Set all)
  {
    if ((null == all) || (all.isEmpty()))
      return (all);

    Set subset = Collections.synchronizedSet(new HashSet());
    synchronized(subset)
    {
	    for (Iterator iterator = all.iterator(); iterator.hasNext();)
	    {
	      Proxy candidate = (Proxy)(iterator.next());
	      if (candidate.canUse())
	        subset.add(candidate);
	    }
    }

    return subset;
  }
  
  private static Set filterProxiesByExtensible(Set all)
  {
    if ((null == all) || (all.isEmpty()))
      return (all);

    Set subset = Collections.synchronizedSet(new HashSet());
    synchronized(subset)
    {
	    for (Iterator iterator = all.iterator(); iterator.hasNext();)
	    {
	      Proxy candidate = (Proxy)(iterator.next());
	      if (candidate.canExtend())
	        subset.add(candidate);
	    }
    }

    return subset;
  }
  
  public static String privilegeStr
    (Signet     signet,
     Grantable  grantable)
  {
    String privilegeStr;
    
    if (grantable instanceof Assignment)
    {
      Assignment assignment = (Assignment)grantable;
      privilegeStr
        = assignment.getFunction().getSubsystem().getName()
          + " : "
          + assignment.getFunction().getCategory().getName()
          + " : "
          + assignment.getFunction().getName();
    }
    else
    {
      Proxy proxy = (Proxy)grantable;
      privilegeStr = proxyPrivilegeDisplayName(signet, proxy);
    }
    
    return privilegeStr;
  }
  
  public static String scopeStr(Grantable grantable)
  {
    String scopeStr;
    
    if (grantable instanceof Assignment)
    {
      Assignment assignment = (Assignment)grantable;
      scopeStr = assignment.getScope().getName();
    }
    else
    {
      scopeStr = "";
    }
      
    return scopeStr;
  }
  
  public static String homepageName(SignetSubject loggedInUser)
  {
    StringBuffer homePageName = new StringBuffer();
    
    homePageName.append("My View [");
    homePageName.append(loggedInUser.getName());
    homePageName.append("]");
    
    if (!loggedInUser.getEffectiveEditor().equals(loggedInUser))
    {
      homePageName.append
        ("<span class=\"actingas\" style=\"display: inline;\" id=\"ActingAs\">\n");
      homePageName.append("acting as ");
      homePageName.append(loggedInUser.getEffectiveEditor().getName());
      homePageName.append("\n</span>\n");
    }
    
    return homePageName.toString();
  }

  static Function getFunction
    (Subsystem subsystem,
     String    functionId)
  throws ObjectNotFoundException
  {
    Iterator functionsIterator = subsystem.getFunctions().iterator();
    while (functionsIterator.hasNext())
    {
      Function candidate = (Function)(functionsIterator.next());
      if (candidate.getId().equals(functionId))
      {
        return candidate;
      }
    }
    
    throw new ObjectNotFoundException
      ("Unable to find function with ID '"
       + functionId
       + "' in subsystem '"
       + subsystem + "'");
  }
  
  public static boolean isSubsystemOwner
    (Signet signet,
     Proxy  proxy)
  {
    if ((proxy.getGrantor().equals(signet.getSignetSubject()))
        && (proxy.canUse() == true)
        && (proxy.getSubsystem() != null))
    {
      return true;
    }
    
    return false;
  }
  
//  public static Set getExtensibleProxies
//    (PrivilegedSubject proxyGrantor,
//     PrivilegedSubject proxyGrantee)
//  {
//System.out.println("DEBUG: ENTERING Common.getExtensibleProxies(): proxyGrantor='" + proxyGrantor + "', proxyGrantee='" + proxyGrantee + "'");
//    Set proxies
//      = proxyGrantee.getProxiesReceived(Status.ACTIVE, null, proxyGrantor);
//    Set extensibleProxies = new HashSet();
//    
//    Iterator proxiesIterator = proxies.iterator();
//    while (proxiesIterator.hasNext())
//    {
//      Proxy proxy = (Proxy)(proxiesIterator.next());
//      if (proxy.canExtend())
//      {
//        extensibleProxies.add(proxy);
//      }
//    }
//    
//    return extensibleProxies;
//  }
  
//  public static Set getSubsystemsFromProxies(Set proxies)
//  {
//System.out.println("DEBUG: ENTERING Common.getSubsystemsFromProxies(): proxies=" + proxies);
//    Set subsystems = new HashSet();
//    
//    Iterator proxiesIterator = proxies.iterator();
//    while (proxiesIterator.hasNext())
//    {
//      Proxy proxy = (Proxy)(proxiesIterator.next());
//      
//      if (proxy.getSubsystem() == null)
//      {
//        subsystems.addAll(proxy.getGrantor().getGrantableSubsystemsForProxy());
//      }
//      subsystems.add(proxy.getSubsystem());
//    }
//System.out.println("DEBUG: EXITING Common.getSubsystemsFromProxies(): subsystems=" + subsystems);
//    return subsystems;
//  }


	public static String buildRevokeMsg(String selectionCountStr)
	{
		Object[] data = new Object[] { "", "" };
				
		MessageFormat form = new MessageFormat(ResLoaderUI.getString("Common.revokemsg.fmt"));
		int selectionCount = Integer.parseInt(selectionCountStr);
		if (selectionCount > 1)
		{
			data[0] = Integer.toString(selectionCount);
			data[1] = ResLoaderUI.getString("Common.revokemsg.plural.txt");
		}
		String retval = form.format(data);

		return (retval);
	}

}
