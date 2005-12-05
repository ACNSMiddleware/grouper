/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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

package edu.internet2.middleware.grouper;


import  java.io.Serializable;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.logging.*;


/** 
 * Perform <i>member of</i> calculation.
 * <p />
 * @author  blair christensen.
 * @version $Id: MemberOf.java,v 1.9 2005-12-05 21:40:02 blair Exp $
 */
class MemberOf implements Serializable {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(MemberOf.class);


  // Protected Class Methods
  
  // Find effective memberships, whether for addition or deletion
  protected static Set doMemberOf(GrouperSession s, Group g, Member m, Field f) 
    throws  GroupNotFoundException
  {
    // In order to bypass privilege constraints since a subject may be
    // privileged to adjust memberships but not be privileged to see,
    // let alone alter, all of the effective memberships that change as
    // a result.
    GrouperSession  root    = GrouperSessionFinder.getRootSession();
    Set             mships  = new LinkedHashSet();
    String          msg     = " for '" + g.getName() + "'/'" + m + "'";
    GrouperLog.debug(LOG, s, "doMemberOf: membership calculation" + msg);
    msg += ": ";

    // Proxy as root for a short period of time
    g.setSession(root);  

    // Find where g is a member - but do it as root
    Set isMember = new LinkedHashSet();
    if (f.equals(Group.getDefaultList())) {
      // We only propagate effective membership changes further up when
      // adding to the "members" list.
      isMember = g.toMember().getMemberships();
    }

    // Add m to where g is a member - as root
    Set temp0 = _findMembershipsWhereGroupIsMember(root, g, m, isMember);
    mships.addAll(temp0);
    GrouperLog.debug(LOG, s, "group is member" + msg + temp0.size());

    // Add members of m to g - as root
    // FIXME I need to pass along field here
    // Add members of m to where g is a member - as root
    mships.addAll(_findGroupAsMember(root, g.getUuid(), m, f, isMember));

    // Now reset everything to the proper session
    g.setSession(s);
    Set resetMships = new LinkedHashSet();
    // TODO Don't I already have a method to do this in bulk?
    Iterator iter = mships.iterator();
    while (iter.hasNext()) {
      Membership ms = (Membership) iter.next();    
      ms.setSession(s);
      resetMships.add(ms);
    }

    GrouperLog.debug(LOG, s, "memberOf total" + msg + resetMships.size());
    return resetMships;
  } // protected static Set doMemberOf(s, g, m, f)

  // Find effective memberships, whether for addition or deletion
  protected static Set doMemberOf(GrouperSession s, Stem ns, Member m, Field f) 
    throws  StemNotFoundException
  {
    // TODO Add logging as above
    Set mships    = new LinkedHashSet();

    // Stems can't be members
    Set isMember  = new LinkedHashSet();

    // Add members of m to ns
    // Add members of m to where ns is a member
    try {
      mships.addAll(
        _findGroupAsMember(s, ns.getUuid(), m, f, isMember)
      );
    }
    catch (GroupNotFoundException eGNF) {
      throw new StemNotFoundException(eGNF.getMessage());
    }

    return mships;
  } // protected static Set doMemberOf(s, ns, m, f)


  // Private Class Methods

  // If m is a group, find its members and add them to g and where g is
  // a member
  private static Set _findGroupAsMember(
    GrouperSession s, String oid, Member m, Field f, Set isMember
  )
    throws  GroupNotFoundException
  {
    Set     mships      = new LinkedHashSet();
    Set     hasMembers  = new LinkedHashSet();
    String  msg         = null;

    if (m.getSubjectTypeId().equals("group")) {
      // Convert member back to a group
      Group gm = m.toGroup();
      msg = "member '" + m + "' is group";
      GrouperLog.debug(LOG, s, msg);
      // And attach root session for better looking up of memberships
      gm.setSession(GrouperSessionFinder.getRootSession());

      // Find members of m
      Set hasMships = gm.getMemberships();
      GrouperLog.debug(LOG, s, msg + " has members: " + hasMships.size());

      // Add members of m to g
      // Add members of m to where g is a member
      mships.addAll(
        _findMembershipsOfMember(
          s, oid, gm.getUuid(), f, isMember, hasMships
        )
      );
    }
    if (msg != null) {
      GrouperLog.debug(LOG, s, msg + " total: " + mships.size());
    }
    return mships; 
  } // private static Set _findGroupAsMember(s, oid, m, f, isMember)

  // Member is a group.  Look for its memberships and add them to where
  // the containing object is a member.
  private static Set _findMembershipsOfMember(
    GrouperSession s, String oid, String gmid, Field f, Set isMember, Set hasMembers
  ) 
  {
    // TODO Refactor into smaller components
    Set mships = new LinkedHashSet();

    // Add members of m to where this group is a member

    // For every member of m...
    Iterator iterMofM = hasMembers.iterator();
    while (iterMofM.hasNext()) {
      Membership  mofm  = (Membership) iterMofM.next();
      // ... add to this group
      int         depth = mofm.getDepth() + 1;
      String      vid   = mofm.getVia_id();
      if (vid == null) {
        vid = gmid;
      }
      try {
        Membership msMofM = new Membership(
          s, oid, mofm.getMember(), f, vid, depth
        );
        mships.add(msMofM);
      }
      catch (MemberNotFoundException eMNF0) {
        // TODO
        GrouperLog.warn(LOG, s, eMNF0.getMessage());
      }
      // ... and add to wherever this group is a member
      Iterator iterGisM = isMember.iterator();
      while (iterGisM.hasNext()) {
        Membership gism = (Membership) iterGisM.next();
        try {
          Membership msGisM = new Membership(
            s, gism.getOwner_id(), mofm.getMember(), Group.getDefaultList(), 
            vid, depth + gism.getDepth() 
          );
          mships.add(msGisM);
        }
        catch (MemberNotFoundException eMNF1) {
          // TODO
          GrouperLog.warn(LOG, s, eMNF1.getMessage());
        }
      }
    }

    return mships;
  } // private static Set _findMembershipsOfMember(s, oid, gmid, f, isMember, hasMembers)

  // More effective membership voodoo
  private static Set _findMembershipsWhereGroupIsMember(
    GrouperSession s, Group g, Member m, Set isMember
  ) 
  {
    Set mships = new LinkedHashSet();
    // Add m to where g is a member
    Iterator iter = isMember.iterator();
    while (iter.hasNext()) {
      Membership  ms    = (Membership) iter.next();
      int         depth = ms.getDepth() + 1;
      String      vid   = ms.getVia_id();
      if (vid == null) {
        vid = g.getUuid();
      }
      Membership msGisM = new Membership(
        s, ms.getOwner_id(), m, Group.getDefaultList(), vid, depth
      );
      mships.add(msGisM);
    }
    return mships;
  } // private static Set _findMembershipsWhereGroupIsMember(s, g, m, isMember)

}

