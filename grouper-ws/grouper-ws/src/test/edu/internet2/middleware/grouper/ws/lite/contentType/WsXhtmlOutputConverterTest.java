/*
 * @author mchyzer
 * $Id: WsXhtmlOutputConverterTest.java,v 1.1 2008-03-24 20:19:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.lite.contentType;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ws.lite.contentType.WsXhtmlInputConverter;
import edu.internet2.middleware.grouper.ws.lite.contentType.WsXhtmlOutputConverter;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * test the output converter
 */
public class WsXhtmlOutputConverterTest extends TestCase {

  /**
   * @param name
   */
  public WsXhtmlOutputConverterTest(String name) {
    super(name);
  }

  /**
   * run a test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new WsXhtmlOutputConverterTest("testMarshal"));
  }
  
  /**
   * test convert to xml
   */
  public void testMarshal() {
    BeanChild beanChild = new BeanChild("va<l1", "val2", new String[]{"a"}, new int[]{1, 2});
    WsXhtmlOutputConverter wsXhtmlOutputConverter = new WsXhtmlOutputConverter(false, null);
    String xhtml = wsXhtmlOutputConverter.writeBean(beanChild);

    System.out.println(xhtml);
    assertEquals("<div title=\"BeanChild\"><p class=\"childField1\">va&lt;l1</p><p class=\"childField2\">val2</p><ul class=\"childStringArray\"><li>a</li></ul><ul class=\"childIntegerArray\"><li>1</li><li>2</li></ul></div>", xhtml);
    
    //test warnings
    xhtml = "<div title=\"BeanChild\" id=\"something\"><span>something</span><p class=\"childField1\">va&lt;l1</p><p class=\"childField2\">val2</p><ul class=\"childStringArray\"><li>a</li></ul><ul class=\"childIntegerArray\"><li>1</li><li>2</li></ul></div>";
    WsXhtmlInputConverter wsXhtmlInputConverter = new WsXhtmlInputConverter();
    
    beanChild = (BeanChild)wsXhtmlInputConverter.parseXhtmlString(xhtml);
    
    String warnings = wsXhtmlInputConverter.getWarnings();
    System.out.println(warnings);
    assertFalse(StringUtils.isBlank(warnings));
    assertTrue("should detect extraneous attribute", warnings.contains("not expecting attribute"));
    assertTrue("should detect extraneous element", warnings.contains("not expecting child element"));
  }
  
  /**
   * test convert object map to xhtml
   * @param includeHeader
   */
  public void testMarshal2() {
    marshal2helper(false);
    marshal2helper(true);
  }

  /**
   * test convert object map to xhtml
   * @param includeHeader
   */
  public void marshal2helper(boolean includeHeader) {
    BeanGrandparent beanGrandparent = generateGrandParent();
    
    WsXhtmlOutputConverter wsXhtmlOutputConverter = new WsXhtmlOutputConverter(includeHeader, "the title");
    String xhtml = wsXhtmlOutputConverter.writeBean(beanGrandparent);

    System.out.println(xhtml);
    
    WsXhtmlInputConverter wsXhtmlInputConverter = new WsXhtmlInputConverter();
    beanGrandparent = (BeanGrandparent)wsXhtmlInputConverter.parseXhtmlString(xhtml);
  
    //shouldnt be any warnings
    assertTrue(wsXhtmlInputConverter.getWarnings(), StringUtils.isBlank(wsXhtmlInputConverter.getWarnings()));

    WsXhtmlOutputConverter wsXhtmlOutputConverter2 = new WsXhtmlOutputConverter(includeHeader, "the title");
    String xhtml2 = wsXhtmlOutputConverter2.writeBean(beanGrandparent);
    
    assertEquals(xhtml, xhtml2);
  }

  /**
   * @return
   */
  public static BeanGrandparent generateGrandParent() {
    //note, xhtml doesnt do nulls vs empty strings, so dont worry about empty string in test
    BeanChild beanChild = new BeanChild("v\"a<l{1}", "val2", new String[]{"a"}, new int[]{1, 2});
    BeanParent beanParent = new BeanParent("qwe", "rtyu", new String[]{ "uio", "cv"}, 45, 
        null, beanChild, null, new BeanChild[]{beanChild});
    
    BeanGrandparent beanGrandparent = new BeanGrandparent("xv", null, 
        new String[]{null},beanParent, new BeanParent[]{beanParent, beanParent} );

    return beanGrandparent;
  }
  
}
