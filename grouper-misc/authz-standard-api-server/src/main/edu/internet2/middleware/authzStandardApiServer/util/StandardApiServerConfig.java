package edu.internet2.middleware.authzStandardApiServer.util;

import edu.internet2.middleware.authzStandardApiServer.config.AsasConfigPropertiesCascadeBase;


/**
 * hierarchical config class for authzStandardApi.server.properties
 * @author mchyzer
 *
 */
public class StandardApiServerConfig extends AsasConfigPropertiesCascadeBase {

  /**
   * use the factory
   */
  private StandardApiServerConfig() {
    
  }
  
  /**
   * path separator char
   * @return path separator
   */
  public String configItemPathSeparatorChar() {
    return StandardApiServerConfig.retrieveConfig().propertyValueStringRequired(
        "authzStandardApiServer.pathSeparatorChar");
  }
  
  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static StandardApiServerConfig retrieveConfig() {
    return retrieveConfig(StandardApiServerConfig.class);
  }

  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  @Override
  public void clearCachedCalculatedValues() {
    
  }

  /**
   * @see AsasConfigPropertiesCascadeBase#getHierarchyConfigKey
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "authzStandardApiServer.config.hierarchy";
  }

  /**
   * @see AsasConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "authzStandardApi.server.properties";
  }
  
  /**
   * @see AsasConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "authzStandardApi.server.base.properties";
  }

  /**
   * @see AsasConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "authzStandardApiServer.config.secondsBetweenUpdateChecks";
  }

  
  
}
