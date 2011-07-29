/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetSubjects;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsGetSubjectsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsParam;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsSubject;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsSubjectLookup;

/**
 *
 * @author mchyzer
 *
 */
public class WsSampleGetSubjects implements WsSampleGenerated {

  /**
   * @param args
   */
  public static void main(String[] args) {
    getSubjects(WsSampleGeneratedType.soap);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
   */
  public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
    getSubjects(wsSampleGeneratedType);
  }

  /**
   * @param wsSampleGeneratedType can run as soap or xml/http
   */
  public static void getSubjects(
      WsSampleGeneratedType wsSampleGeneratedType) {
    try {
      //URL, e.g. http://localhost:8091/grouper-ws/services/GrouperService
      GrouperServiceStub stub = new GrouperServiceStub(GeneratedClientSettings.URL);
      Options options = stub._getServiceClient().getOptions();
      HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
      auth.setUsername(GeneratedClientSettings.USER);
      auth.setPassword(GeneratedClientSettings.PASS);
      auth.setPreemptiveAuthentication(true);

      options.setProperty(HTTPConstants.AUTHENTICATE, auth);
      options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(3600000));
      options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,
          new Integer(3600000));

      GetSubjects getSubjects = GetSubjects.class.newInstance();

      // set the act as id
      WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
      actAsSubject.setSubjectId("GrouperSystem");
      getSubjects.setActAsSubjectLookup(actAsSubject);

      //version, e.g. v1_3_000
      getSubjects.setClientVersion(GeneratedClientSettings.VERSION);
      
      getSubjects.setFieldName("");

      WsGroupLookup wsGroupLookup = new WsGroupLookup();
      wsGroupLookup.setGroupName("aStem:aGroup");
      getSubjects.setWsGroupLookup(wsGroupLookup);

      getSubjects.setIncludeGroupDetail("");
      getSubjects.setIncludeSubjectDetail("T");
      
      getSubjects.setParams(new WsParam[]{null});
      
      getSubjects.setSearchString("test");
      
      getSubjects.setSourceIds(new String[]{null});
      
      getSubjects.setSubjectAttributeNames(new String[]{null});

      getSubjects.setWsMemberFilter("");
      
      getSubjects.setWsSubjectLookups(new WsSubjectLookup[]{new WsSubjectLookup()});

      WsGetSubjectsResults wsGetSubjectsResults = stub.getSubjects(getSubjects)
          .get_return();

      System.out.println(ToStringBuilder.reflectionToString(
          wsGetSubjectsResults));

      WsSubject[] wsSubjectsResultArray = wsGetSubjectsResults.getWsSubjects();

      for (WsSubject wsSubject : wsSubjectsResultArray) {
        System.out.println(ToStringBuilder.reflectionToString(
            wsSubject));
      }
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
