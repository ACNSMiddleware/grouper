/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;

/**
 * Result of assigning or removing a privilege
 * 
 * @author mchyzer
 */
public class WsAssignGrouperPrivilegesResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * empty
   */
  public WsAssignGrouperPrivilegesResults() {
    //empty
  }

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(WsAssignGrouperPrivilegesResults.class);


  /**
   * prcess an exception, log, etc
   * @param wsMemberChangeSubjectLiteResultCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsAssignGrouperPrivilegesResultsCode wsMemberChangeSubjectLiteResultCodeOverride, 
      String theError, Exception e) {

    if (e instanceof WsInvalidQueryException || e instanceof StemNotFoundException 
        || e instanceof GroupNotFoundException || e instanceof InsufficientPrivilegeException) {

      wsMemberChangeSubjectLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsMemberChangeSubjectLiteResultCodeOverride, WsAssignGrouperPrivilegesResultsCode.INVALID_QUERY);
      if (e instanceof StemNotFoundException || e.getCause() instanceof StemNotFoundException) {
        wsMemberChangeSubjectLiteResultCodeOverride = WsAssignGrouperPrivilegesResultsCode.STEM_NOT_FOUND;
      }
      if (e instanceof GroupNotFoundException || e.getCause() instanceof GroupNotFoundException) {
        wsMemberChangeSubjectLiteResultCodeOverride = WsAssignGrouperPrivilegesResultsCode.GROUP_NOT_FOUND;
      }
      if (e instanceof InsufficientPrivilegeException || e.getCause() instanceof InsufficientPrivilegeException) {
        wsMemberChangeSubjectLiteResultCodeOverride = WsAssignGrouperPrivilegesResultsCode.INSUFFICIENT_PRIVILEGES;
      }

      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsMemberChangeSubjectLiteResultCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsMemberChangeSubjectLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsMemberChangeSubjectLiteResultCodeOverride, WsAssignGrouperPrivilegesResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsMemberChangeSubjectLiteResultCodeOverride);

    }
  }

  /**
   * assign the code from the enum
   * @param wsAssignGrouperPrivilegesResultsCode
   */
  public void assignResultCode(WsAssignGrouperPrivilegesResultsCode wsAssignGrouperPrivilegesResultsCode) {
    this.getResultMetadata().assignResultCode(wsAssignGrouperPrivilegesResultsCode);
  }

  /**
    * metadata about the result
    */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * result code of a request
   */
  public static enum WsAssignGrouperPrivilegesResultsCode implements WsResultCode {

    /** problem deleting members e.g. in replaceExisting (rest http status code 500) (success: F) */
    PROBLEM_DELETING_MEMBERS(500),

    /** assignments successful (rest http status code 200) (success: T) */
    SUCCESS(200),

    /** some exception occurred (rest http status code 500) (success: F) */
    EXCEPTION(500),

    /** cant find group (rest http status code 404) (success: F) */
    GROUP_NOT_FOUND(404),

    /** cant find stem (rest http status code 404) (success: F) */
    STEM_NOT_FOUND(404),

    /** cant find type (rest http status code 404) (success: F) */
    TYPE_NOT_FOUND(404),

    /** cant find name (rest http status code 404) (success: F) */
    NAME_NOT_FOUND(404),

    /** if one request, and that is a insufficient privileges (rest http status code 403) (success: F) */
    INSUFFICIENT_PRIVILEGES(403),

    /** invalid query (e.g. if everything blank) (rest http status code 400) (success: F) */
    INVALID_QUERY(400);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name */
    public String nameForVersion(GrouperWsVersion clientVersion) {
      return this.name();
    }

    /**
     * if this is a successful result
     * 
     * @return true if success
     */
    public boolean isSuccess() {
      return this.name().startsWith("SUCCESS");
    }

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsAssignGrouperPrivilegesResultsCode(int statusCode) {
      this.httpStatusCode = statusCode;
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.WsResultCode#getHttpStatusCode()
     */
    public int getHttpStatusCode() {
      return this.httpStatusCode;
    }
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * attributes of subjects returned, in same order as the data
   * @return the attributeNames
   */
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  /**
   * attributes of subjects returned, in same order as the data
   * @param attributeNamesa the attributeNames to set
   */
  public void setSubjectAttributeNames(String[] attributeNamesa) {
    this.subjectAttributeNames = attributeNamesa;
  }

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  
  /**
   * @param responseMetadata1 the responseMetadata to set
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }

  /**
   * group querying 
   */
  private WsGroup wsGroup;

  /**
   * stem querying 
   */
  private WsStem wsStem;

  /**
   * results for each assignment sent in
   */
  private WsAssignGrouperPrivilegesResult[] results;

  /**
   * results for each assignment sent in
   * @return the results
   */
  public WsAssignGrouperPrivilegesResult[] getResults() {
    return this.results;
  }

  /**
   * results for each assignment sent in
   * @param results1
   */
  public void setResults(WsAssignGrouperPrivilegesResult[] results1) {
    this.results = results1;
  }

  /**
   * group querying
   * @return the group
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * stem querying
   * @return the stem
   */
  public WsStem getWsStem() {
    return this.wsStem;
  }

  /**
   * group querying
   * @param wsGroup1
   */
  public void setWsGroup(WsGroup wsGroup1) {
    this.wsGroup = wsGroup1;
  }

  /**
   * stem querying
   * @param wsStem1
   */
  public void setWsStem(WsStem wsStem1) {
    this.wsStem = wsStem1;
  }
}
