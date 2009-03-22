package edu.internet2.middleware.subject.provider;

import java.util.Collection;
import java.util.Iterator;

import edu.internet2.middleware.subject.provider.BaseSourceAdapter;
import edu.internet2.middleware.subject.provider.SourceManager;

import junit.framework.*;

/**
 * Unit tests for SourceManager.
 */
public class SourceManagerTests extends TestCase {

  /** */
	private SourceManager mgr;
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(SourceManagerTests.class);
	}

	/**
	 * Default constructor.
	 */
	public SourceManagerTests() {
		super();
	}
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
  protected void setUp() throws Exception {
		super.setUp();
		this.mgr = SourceManager.getInstance();
	}

	/**
	 * Test case for loading sources from sources.xml
	 */
	public void testInitSource() {
		try {
			for (Iterator iter = this.mgr.getSources().iterator(); iter.hasNext();) {
				BaseSourceAdapter source = (BaseSourceAdapter)iter.next();
				source.init();
			}
		} catch (Exception ex) {
			fail("Failed test to init sources: " + ex.getMessage());
		}
	}

  /**
   * Test case for getting sources by SubjectType
   */
  public void testGetSourcesByType() {
    // Given that each of the current source adapters all have a fair
    // amount of overhead, external dependencies and configuration
    // requirements that might vary **widely** depending upon the
    // circumstances it seems easier to test with **no** configured
    // adapters. 
    Collection people = this.mgr.getSources(SubjectTypeEnum.valueOf("person"));
    Assert.assertNotNull("people !null", people);
    Assert.assertEquals("Person sources == 0", 0, people.size());
		
    Collection groups = this.mgr.getSources(SubjectTypeEnum.valueOf("group"));
    Assert.assertNotNull("groups !null", groups);
    Assert.assertEquals("Group sources == 0", 0, groups.size());

    Collection apps   = this.mgr.getSources(SubjectTypeEnum.valueOf("application"));
    Assert.assertNotNull("apps !null", apps);
    Assert.assertEquals("Application sources == 0", 0, apps.size());
  } // public void testGetSourcesByType()

  /**
   * Test adding multiple {@link NullSourceAdapter}s.
   */
  public void testAddNullSourceAdapterAndGetSourcesByType() {
    // Verify we have nothing
    Assert.assertEquals("sources == 0", 0, this.mgr.getSources().size());

    // Add the NullSourceAdapter 
    NullSourceAdapter nsa = new NullSourceAdapter("nsa", "null source adapter");
    Assert.assertNotNull("nsa !null", nsa);
    this.mgr.loadSource(nsa);
    Assert.assertEquals("sources == 1", 1, this.mgr.getSources().size());

    // Now verify that we have an adapter for each type
    Collection people = this.mgr.getSources(SubjectTypeEnum.valueOf("person"));
    Assert.assertNotNull("people !null", people);
    Assert.assertEquals("Person sources == 1", 1, people.size());
		
    Collection groups = this.mgr.getSources(SubjectTypeEnum.valueOf("group"));
    Assert.assertNotNull("groups !null", groups);
    Assert.assertEquals("Group sources == 1", 1, groups.size());

    Collection apps   = this.mgr.getSources(SubjectTypeEnum.valueOf("application"));
    Assert.assertNotNull("apps !null", apps);
    Assert.assertEquals("Application sources == 1", 1, apps.size());
  } // public void testAddNullSourceAdapterAndGetSourcesByType() {

}
