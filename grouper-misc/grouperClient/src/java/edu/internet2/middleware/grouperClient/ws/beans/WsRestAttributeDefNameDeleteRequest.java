/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.ws.beans;

/**
 * request bean in body of rest request
 */
public class WsRestAttributeDefNameDeleteRequest implements WsRequestBean {

  /**
   * AttributeDefNames to delete
   */
  private WsAttributeDefNameLookup[] wsAttributeDefNameLookups;
  
  /**
   * AttributeDefNames to delete
   * @return relatedWsAttributeDefNameLookups
   */
  public WsAttributeDefNameLookup[] getWsAttributeDefNameLookups() {
    return this.wsAttributeDefNameLookups;
  }

  /**
   * AttributeDefNames to delete
   * @param relatedWsAttributeDefNameLookups1
   */
  public void setWsAttributeDefNameLookups(
      WsAttributeDefNameLookup[] relatedWsAttributeDefNameLookups1) {
    this.wsAttributeDefNameLookups = relatedWsAttributeDefNameLookups1;
  }

  /**
   * is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @return txType
   */
  public String getTxType() {
    return this.txType;
  }

  /**
   * is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param txType1
   */
  public void setTxType(String txType1) {
    this.txType = txType1;
  }

  /**
   * is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   */
  private String txType;
  
  
  /** is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000 */
  private String clientVersion;
  
  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @return version
   */
  public String getClientVersion() {
    return this.clientVersion;
  }

  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param clientVersion1
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  /** if acting as someone else */
  private WsSubjectLookup actAsSubjectLookup;
  
  /**
   * if acting as someone else
   * @return act as subject
   */
  public WsSubjectLookup getActAsSubjectLookup() {
    return this.actAsSubjectLookup;
  }

  /**
   * if acting as someone else
   * @param actAsSubjectLookup1
   */
  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup1) {
    this.actAsSubjectLookup = actAsSubjectLookup1;
  }

  /** optional: reserved for future use */
  private  WsParam[] params;

  
  
  /**
   * optional: reserved for future use
   * @return params
   */
  public WsParam[] getParams() {
    return this.params;
  }

  /**
   * optional: reserved for future use
   * @param params1
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

  


}
