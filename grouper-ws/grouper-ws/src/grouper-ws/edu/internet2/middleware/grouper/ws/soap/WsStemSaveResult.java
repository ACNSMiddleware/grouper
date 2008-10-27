/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.soap.WsStemSaveLiteResult.WsStemSaveLiteResultCode;

/**
 * Result of one save being saved.  The number of
 * these result objects will equal the number of saves sent in to the method
 * to be saved
 * 
 * @author mchyzer
 */
public class WsStemSaveResult {

  /**
   * empty
   */
  public WsStemSaveResult() {
    //empty
  }
  
  /**
   * construct initially with lookup
   * @param wsStemLookup
   */
  public WsStemSaveResult(WsStemLookup wsStemLookup) {
    this.wsStem = new WsStem(wsStemLookup);
  }
  
  /** stem that is saved */
  private WsStem wsStem = null;

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsStemSaveResult.class);

  /**
   * convert string to result code
   * @return the result code
   */
  public WsStemSaveResultCode resultCode() {
    return WsStemSaveResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * result code of a request
   */
  public static enum WsStemSaveResultCode {

    /** successful addition */
    SUCCESS {

      /** 
       * if there is one result, convert to the results code
       * @return WsStemSaveLiteResultCode
       */
      @Override
      public WsStemSaveLiteResultCode convertToLiteCode() {
        return WsStemSaveLiteResultCode.SUCCESS;
      }

    },

    /** invalid query, can only happen if lite query */
    INVALID_QUERY {

      /** 
       * if there is one result, convert to the results code
       * @return WsStemSaveLiteResultCode
       */
      @Override
      public WsStemSaveLiteResultCode convertToLiteCode() {
        return WsStemSaveLiteResultCode.INVALID_QUERY;
      }

    },

    /** the save was not found */
    STEM_NOT_FOUND {

      /** 
       * if there is one result, convert to the results code
       * @return WsStemSaveLiteResultCode
       */
      @Override
      public WsStemSaveLiteResultCode convertToLiteCode() {
        return WsStemSaveLiteResultCode.STEM_NOT_FOUND;
      }

    },

    /** problem with saving */
    EXCEPTION {

      /** 
       * if there is one result, convert to the results code
       * @return WsStemSaveLiteResultCode
       */
      @Override
      public WsStemSaveLiteResultCode convertToLiteCode() {
        return WsStemSaveLiteResultCode.EXCEPTION;
      }

    },

    /** was a success but rolled back */
    TRANSACTION_ROLLED_BACK {

      /** 
       * if there is one result, convert to the results code
       * @return WsStemSaveLiteResultCode
       */
      @Override
      public WsStemSaveLiteResultCode convertToLiteCode() {
        return WsStemSaveLiteResultCode.EXCEPTION;
      }

    },

    /** user not allowed */
    INSUFFICIENT_PRIVILEGES {

      /** 
       * if there is one result, convert to the results code
       * @return WsStemSaveLiteResultCode
       */
      @Override
      public WsStemSaveLiteResultCode convertToLiteCode() {
        return WsStemSaveLiteResultCode.INSUFFICIENT_PRIVILEGES;
      }

    };

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }

    /** 
     * if there is one result, convert to the results code
     * @return result code
     */
    public abstract WsStemSaveLiteResultCode convertToLiteCode();
  }

  /**
   * assign the code from the enum
   * @param saveSaveResultCode
   */
  public void assignResultCode(WsStemSaveResultCode saveSaveResultCode) {
    this.getResultMetadata().assignResultCode(
        saveSaveResultCode == null ? null : saveSaveResultCode.name());
    this.getResultMetadata().assignSuccess(saveSaveResultCode.isSuccess() ? "T" : "F");
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * assign a resultcode of exception, and process/log the exception
   * @param e
   * @param wsStemToSave
   */
  public void assignResultCodeException(Exception e, WsStemToSave wsStemToSave) {
    //get root exception (might be wrapped in wsInvalidQuery)
    Throwable mainThrowable = (e instanceof WsInvalidQueryException 
        && e.getCause() != null) ? e.getCause() : e;
    
    if (mainThrowable instanceof InsufficientPrivilegeException) {
      this.getResultMetadata().setResultMessage(mainThrowable.getMessage());
      this.assignResultCode(WsStemSaveResultCode.INSUFFICIENT_PRIVILEGES);
      
    } else if (mainThrowable  instanceof StemNotFoundException) {
      this.getResultMetadata().setResultMessage(mainThrowable.getMessage());
      this.assignResultCode(WsStemSaveResultCode.STEM_NOT_FOUND);
    } else if (e  instanceof WsInvalidQueryException) {
      this.getResultMetadata().setResultMessage(mainThrowable.getMessage());
      this.assignResultCode(WsStemSaveResultCode.INVALID_QUERY);
    } else {
      this.getResultMetadata().setResultMessage(ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(WsStemSaveResultCode.EXCEPTION);
    }
    LOG.error(wsStemToSave + ", " + e, e);
  }

  /**
   * @return the wsStem
   */
  public WsStem getWsStem() {
    return this.wsStem;
  }

  /**
   * @param wsStem1 the wsStem to set
   */
  public void setWsStem(WsStem wsStem1) {
    this.wsStem = wsStem1;
  }

  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }
}
