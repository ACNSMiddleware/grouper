/*
 * @author mchyzer $Id: WsResultMeta.java,v 1.6 2008-11-18 10:05:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * result metadata (if success, result code, etc) for one result
 * (each ws call can have one or many result metadatas)
 * @see WsResultCode for all implementations of responses
 */
public class WsResultMeta {

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /** params for result */
  private WsParam[] params = null;
  
  /**
   * copy fields from another result meta.  will append warnings
   * and errors
   * @param wsResultMeta
   */
  public void copyFields(WsResultMeta wsResultMeta) {
    this.httpStatusCode = wsResultMeta.httpStatusCode;
    this.resultCode = wsResultMeta.resultCode;
    this.success = wsResultMeta.success;
    this.appendResultMessage(wsResultMeta.getResultMessage());
    this.setResultCode2(wsResultMeta.getResultCode2());
  }

  /**
   * <pre>
   * code of the result for this subject
   * SUCCESS: means everything ok
   * SUBJECT_NOT_FOUND: cant find the subject
   * SUBJECT_DUPLICATE: found multiple subjects
   *  
   * </pre>
   */
  private String resultCode;

  /**
   * <pre>
   * reserved for future purposes
   *  
   * </pre>
   */
  private String resultCode2;

  /**
   * error message if there is an error
   */
  private StringBuilder resultMessage = new StringBuilder();

  /** T or F as to whether it was a successful assignment */
  private String success;

  /** status code, if 500, then not set */
  @XStreamOmitField
  private int httpStatusCode = 500;

  /**
   * append error message to list of error messages
   * 
   * @param errorMessage
   */
  public void appendResultMessage(String errorMessage) {
    this.resultMessage.append(errorMessage);
  }

  /**
   * <pre>
   * code of the result for this subject
   * SUCCESS: means everything ok
   * SUBJECT_NOT_FOUND: cant find the subject
   * SUBJECT_DUPLICATE: found multiple subjects
   *  
   * </pre>
   * 
   * @return the resultCode
   */
  public String getResultCode() {
    return this.resultCode;
  }

  /**
   * <pre>
   * reserved for future purpose
   * </pre>
   * 
   * @return the resultCode
   */
  public String getResultCode2() {
    return this.resultCode2;
  }

  /**
   * error message if there is an error
   * 
   * @return the errorMessage
   */
  public String getResultMessage() {
    return this.resultMessage.toString();
  }

  /**
   * T or F as to whether it was a successful assignment
   * 
   * @return the success
   */
  public String getSuccess() {
    return this.success;
  }

  
  /**
   * @param resultCode1 the resultCode to set
   */
  public void setResultCode(String resultCode1) {
    this.resultCode = resultCode1;
  }

  /**
   * @param resultCode1 the resultCode2 to set
   */
  public void setResultCode2(String resultCode1) {
    this.resultCode2 = resultCode1;
  }

  /**
   * T or F as to whether it was a successful assignment
   * @param theSuccess T | F
   */
  public void setSuccess(String theSuccess) {
    this.success = theSuccess;
  }

  /**
   * <pre>
   * code of the result for this subject
   * SUCCESS: means everything ok
   * SUBJECT_NOT_FOUND: cant find the subject
   * SUBJECT_DUPLICATE: found multiple subjects
   *  
   * </pre>
   * 
   * @param resultCode1
   *            the resultCode to set
   */
  public void assignResultCode(String resultCode1) {
    this.resultCode = resultCode1;
  }

  /**
   * set result code which includes the success and http status code
   * @param wsResultCode1 bean
   */
  public void assignResultCode(WsResultCode wsResultCode1) {
    this.assignResultCode(wsResultCode1.name());
    this.assignSuccess(GrouperServiceUtils.booleanToStringOneChar(wsResultCode1
        .isSuccess()));
    this.assignHttpStatusCode(wsResultCode1.getHttpStatusCode());
  }

  /**
   * error message if there is an error
   * 
   * @param errorMessage
   *            the errorMessage to set
   */
  public void setResultMessage(String errorMessage) {
    this.resultMessage = new StringBuilder(StringUtils.defaultString(errorMessage));
  }

  /**
   * T or F as to whether it was a successful assignment
   * 
   * @param success1
   *            the success to set
   */
  public void assignSuccess(String success1) {
    this.success = success1;
  }

  /**
   * status code for http lite / rest .  not a getter so isnt in soap/lite response
   * @return the status code e.g. 200, if 500, then not initted
   */
  public int retrieveHttpStatusCode() {
    return this.httpStatusCode;
  }

  /**
   * status code for http lite / rest .  not a setter so isnt in soap/lite response
   * @param statusCode1 the status code e.g. 200, if 500, then not initted
   */
  public void assignHttpStatusCode(int statusCode1) {
    this.httpStatusCode = statusCode1;
  }

  
  /**
   * @return the params
   */
  public WsParam[] getParams() {
    return this.params;
  }

  
  /**
   * @param params1 the params to set
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

}
