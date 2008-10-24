/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import java.io.File;
import java.util.Properties;

import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.morphString.Morph;

import bsh.CallStack;
import bsh.Interpreter;

/**
 * Run a sql script against DB (like ant does it)
 * <p/>
 * @author  Chris Hyzer
 * @version $Id: sqlRun.java,v 1.1 2008-10-24 05:51:47 mchyzer Exp $
 * @since   0.0.1
 */
public class sqlRun {

  /**
   * Create tables and init schema (depending on configuration in grouper.properties)
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @return  a string
   * @since   0.0.1
   */
  public static void invoke(Interpreter interpreter, CallStack stack, File scriptFile) {
    GrouperShell.setOurCommand(interpreter, true);
    
    Properties properties = GrouperUtil.propertiesFromResourceName(
        "grouper.hibernate.properties");
      
    String user = properties.getProperty("hibernate.connection.username");
    String pass = properties.getProperty("hibernate.connection.password");
    String url = properties.getProperty("hibernate.connection.url");
    String driver = properties.getProperty("hibernate.connection.driver_class");
    pass = Morph.decryptIfFile(pass);

    GrouperDdlUtils.sqlRun(scriptFile, driver, url, user, pass, false, true);
    
  }

}

