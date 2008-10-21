package edu.internet2.middleware.grouper.ws.samples.rest.stem;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ws.rest.WsRestResultProblem;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemSaveRequest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType;
import edu.internet2.middleware.grouper.ws.soap.WsStem;
import edu.internet2.middleware.grouper.ws.soap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.soap.WsStemSaveResults;
import edu.internet2.middleware.grouper.ws.soap.WsStemToSave;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;

/**
 * @author mchyzer
 */
public class WsSampleStemSaveRest implements WsSampleRest {

  /**
   * stem save lite web service with REST
   * @param wsSampleRestType is the type of rest (xml, xhtml, etc)
   */
  @SuppressWarnings("deprecation")
  public static void stemSaveLite(WsSampleRestType wsSampleRestType) {

    try {
      HttpClient httpClient = new HttpClient();
      
      org.apache.commons.httpclient.DefaultMethodRetryHandler retryhandler = new org.apache.commons.httpclient.DefaultMethodRetryHandler();
      retryhandler.setRequestSentRetryEnabled(false);
      httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);

      //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
      PostMethod method = new PostMethod(
          RestClientSettings.URL + "/" + RestClientSettings.VERSION  
            + "/stems");

      httpClient.getParams().setAuthenticationPreemptive(true);
      Credentials defaultcreds = new UsernamePasswordCredentials(RestClientSettings.USER, 
          RestClientSettings.PASS);

      //no keep alive so response if easier to indent for tests
      method.setRequestHeader("Connection", "close");
      
      //e.g. localhost and 8093
      httpClient.getState()
          .setCredentials(new AuthScope(RestClientSettings.HOST, RestClientSettings.PORT), defaultcreds);

      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestStemSaveRequest stemSave = new WsRestStemSaveRequest();
      
      WsStemToSave wsStemToSave = new WsStemToSave();
      wsStemToSave.setWsStemLookup(new WsStemLookup("aStem:whateverStem", null));
      WsStem wsStem = new WsStem();
      wsStem.setDescription("desc1");
      wsStem.setDisplayExtension("disp1");
      wsStem.setExtension("whateverStem");
      wsStem.setName("aStem:whateverStem");
      wsStemToSave.setWsStem(wsStem);
      
      WsStemToSave wsStemToSave2 = new WsStemToSave();
      wsStemToSave2.setWsStemLookup(new WsStemLookup("aStem:whateverStem2", null));
      WsStem wsStems = new WsStem();
      wsStems.setDescription("descs");
      wsStems.setDisplayExtension("disp2");
      wsStems.setExtension("whateverStem2");
      wsStems.setName("aStem:whateverStem2");
      wsStemToSave2.setWsStem(wsStems);

      WsStemToSave[] wsStemToSaves = new WsStemToSave[] {wsStemToSave, wsStemToSave2};
      
      stemSave.setWsStemToSaves(wsStemToSaves);
      
      //get the xml / json / xhtml / paramString
      String requestDocument = wsSampleRestType.getWsLiteRequestContentType().writeString(stemSave);
      
      //make sure right content type is in request (e.g. application/xhtml+xml
      String contentType = wsSampleRestType.getWsLiteRequestContentType().getContentType();
      
      method.setRequestEntity(new StringRequestEntity(requestDocument, contentType, "UTF-8"));
      
      httpClient.executeMethod(method);

      //make sure a request came back
      Header successHeader = method.getResponseHeader("X-Grouper-success");
      String successString = successHeader == null ? null : successHeader.getValue();
      if (StringUtils.isBlank(successString)) {
        throw new RuntimeException("Web service did not even respond!");
      }
      boolean success = "T".equals(successString);
      String resultCode = method.getResponseHeader("X-Grouper-resultCode").getValue();
      
      String response = RestClientSettings.responseBodyAsString(method);

      Object result = wsSampleRestType
        .getWsLiteResponseContentType().parseString(response);
      
      //see if problem
      if (result instanceof WsRestResultProblem) {
        throw new RuntimeException(((WsRestResultProblem)result).getResultMetadata().getResultMessage());
      }
      
      //convert to object (from xhtml, xml, json, etc)
      WsStemSaveResults wsStemSaveResults = (WsStemSaveResults)result;
      
      String resultMessage = wsStemSaveResults.getResultMetadata().getResultMessage();

      // see if request worked or not
      if (!success) {
        throw new RuntimeException("Bad response from web service: successString: " + successString 
            + ", resultCode: " + resultCode
            + ", " + resultMessage);
      }
      
      System.out.println("Server version: " + wsStemSaveResults.getResponseMetadata().getServerVersion()
          + ", result code: " + resultCode
          + ", result message: " + resultMessage );

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * @param args
   */
  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    stemSaveLite(WsSampleRestType.xhtml);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType)
   */
  public void executeSample(WsSampleRestType wsSampleRestType) {
    stemSaveLite(wsSampleRestType);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest#validType(edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType)
   */
  public boolean validType(WsSampleRestType wsSampleRestType) {
    //dont allow http params
    return !WsSampleRestType.http_xhtml.equals(wsSampleRestType);
  }
}
