/*--
$Id: Proxy.java,v 1.7 2006-10-25 00:08:28 ddonn Exp $
$Date: 2006-10-25 00:08:28 $

Copyright 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package edu.internet2.middleware.signet;

import edu.internet2.middleware.signet.subjsrc.SignetSubject;

/**
* 
* A Proxy represents some granting authority which has been delegated to a 
* {@link SignetSubject} (often a person). Unlike an (@link Assignment},
* which invests a <code>SignetSubject</code> with some independent authority
* of its own, a Proxy allows a SignetSubject to act "in the name of" its
* <code>Proxy</code>-grantor. For example, an administrative assistant who
* understands how to use the Signet UI may act as a Proxy for a high-ranking
* official who never actually logs into Signet.
* <p>
* Furthermore, this proxying ability applies only within Signet; That is, if
* B is a proxy for A, then B can grant any privilege that A can grant, but
* B cannot exercise any privilege that A can exercise (unless A explicitly
* grants that privilege to B). Someday, additional Proxy-types may be added
* to allow the exercise of a Proxy in enterprise information systems.
* <p>
* The granularity of a Proxy is always {@link Subsystem}; that is, a
* <code>Subsystem</code> is the smallest unit of authority which can be
* delegated.
* <p>
* Proxies are not transitive. As an illustration, consider the following case:
* 
*   Subject A grants a proxy to subject B.
*   Subject B may now "act as" subject A, when granting Assignments.
* 
*   Subject B grants a proxy to subject C.
*   Subject C may now "act as" subject B, when granting Assignments.
*   Subject C may NOT "act as" subject A
*   When "acting as" subject B, Subject C may grant only those Privileges
*   which subject B holds directly. That is, Subject C may NOT grant any
*   Assignments which subject B can grant only when subject B is "acting as"
*   subject A. 
* <p>
* An existing Proxy may be modified. To save the modified Proxy,
* call Proxy.save().
* 
* @see SignetSubject
* @see Function
* 
*/

public interface Proxy
extends Grantable
{
  
  /**
   * Gets the <code>Subsystem</code> associated with this Proxy.
   * 
   * @return the <code>Subsystem</code> associated with this Proxy.
   */
  public Subsystem getSubsystem();
  
 /**
  * Indicates whether or not this Proxy can be extended to a third party.
  * 
  * @return canExtend When <code>true</code>, means that the Proxy grantee, when
  *        acting as a Proxy for the Proxy grantor, can grant the grantor's
  *        Proxy to some third <code>SignetSubject</code>. Note, however,
  *        that that third <code>SignetSubject</code> is never allowed to
  *        further grant that second-hand Proxy.
  */
  public boolean canExtend();
  
  /**
   * Changes the extensibility of an existing Proxy. To save this change
   * to the database, call <code>Proxy.save()</code>.
   * 
   * @param editor the <code>SignetSubject</code> who is responsible for
   * this change.
   *
   * @param canExtend <code>true</code> if this Proxy should be grantable
   * to others by its current grantee, and <code>false</code> otherwise.
   * 
   * @throws SignetAuthorityException
   */
  public void setCanExtend(SignetSubject editor, boolean canExtend)
  			throws SignetAuthorityException;

  /**
   * Indicates whether or not this Proxy can be used directly
   * by its current grantee, or can only be extended to others.
   * 
   * @return <code>false</code> if this Proxy can only be extended to others
   * by its current grantee, and not used directly by its current grantee.
   */
  public boolean canUse();
  
  /**
   * Changes the direct usability of an existing Proxy. To save this change
   * to the database, call <code>Proxy.save()</code>;
   * 
   * @param editor the <code>SignetSubject</code> who is responsible for
   * this change.
   * 
   * @param canUse <code>false</code> if this Proxy should only be
   * extended to others (and not directly used) by its current grantee, and
   * <code>true</code> otherwise.
   * 
   * @throws SignetAuthorityException
   */
  public void setCanUse(SignetSubject editor, boolean canUse)
  		throws SignetAuthorityException;
}
