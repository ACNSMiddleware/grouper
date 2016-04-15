package edu.internet2.middleware.grouper.pspng;

/*******************************************************************************
 * Copyright 2015 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


/** 
 * This class manages a full-sync thread for a provisioner. This is expected to be instantiated
 * within the Grouper Loader/Daemon jvm and it reads from the grouper-loader settings, but it is not
 * triggered by any changelog or message harness. 
 * 
 * Instead, full-refreshes are triggered via Quartz and the processing interacts directly with the 
 * Group registry.
 * 
 * @author Bert Bee-Lindgren
 *
 */
public class FullSyncProvisioner  {
  protected static class FullSyncQueueItem {
	  Date queuedTime = new Date();
	  
	  // Either a group or 'null' to indicate that a sweep of extra groups
	  // should be performed
	  GrouperGroupInfo groupToProcess;
	  
	  public FullSyncQueueItem(GrouperGroupInfo grouperGroupInfo) {
		  this.groupToProcess = grouperGroupInfo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((groupToProcess == null) ? 0 : groupToProcess.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FullSyncQueueItem other = (FullSyncQueueItem) obj;
		if (groupToProcess == null) {
			if (other.groupToProcess != null)
				return false;
		} else if (!groupToProcess.equals(other.groupToProcess))
			return false;
		return true;
	}
  }
  final private FullSyncQueueItem GROUP_CLEANUP_MARKER = new FullSyncQueueItem(null);
  
  final private Logger LOG;
  
  final protected Provisioner provisioner;
  
  Lock groupListLock = new ReentrantLock();
  // This is used to signal the full-syncing thread that one of the 
  // collections of groups has changed
  Condition notEmptyCondition = groupListLock.newCondition();
  
  // What groups need to be Full-Synced?
  List<FullSyncQueueItem> groupsToSync = new LinkedList<FullSyncQueueItem>();
  
  // What groups need to be Full-Synced as soon as possible?
  // This is a Set (instead of a List or a PriorityQueue) to avoid duplicates. 
  Set<FullSyncQueueItem> groupsToSyncAsap = new HashSet<FullSyncQueueItem>();
  
  // What groups failed in their full-sync at least once and need to be Full-Synced again?
  // This is a Set (instead of a List or a PriorityQueue) to avoid duplicates. 
  Set<FullSyncQueueItem> groupsToSyncRetry = new HashSet<FullSyncQueueItem>();
  
  /**
   * Constructor used by the getfullSyncer() factory method to construct a full-sync wrapper
   * around a provisioner. In other words, do not call this constructor directly.
   * 
   * @param provisioner
   */
  protected FullSyncProvisioner(Provisioner provisioner) {
    this.provisioner = provisioner;
    provisioner.setFullSyncMode(true);
    LOG = LoggerFactory.getLogger(String.format("%s.%s", getClass().getName(), provisioner.getName()));
    
    LOG.debug("Constructing PspngFullSyncer-{}", provisioner.getName());
  }
  
  
  /**
   * Get the FullSync thread_manageFullSyncProcessing() thread running
   */
  protected void start() {
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        thread_manageFullSyncProcessing();
      }
    }, provisioner.getName() + "-FullSync");
    
    t.setDaemon(true);
    t.start();
  }
  
  
  /**
   * method that manages full-sync processing. Most of the time you think of FullSync as 
   * what happens on a schedule (weekly full-sync, for example). However, sometimes
   * incremental provisioning can need a specific group to be sanity checked. For example,
   * if a +member change and a -member change are batched together, the desired order 
   * can be lost. Therefore, a full-sync of the group will make sure the result is accurate.
   * 
   * This method will wait for any full-refreshes to be requested, either from incremental
   * provisioning or from a quartz-scheduled thread that triggers all the groups to be 
   * sync'ed. 
   */
  protected void thread_manageFullSyncProcessing() {
    MDC.put("why", "full-sync/");
    MDC.put("who", provisioner.getName()+"/");
    
    GrouperSession.startRootSession();
    
    while (true) {
  	  FullSyncQueueItem queueItem = getNextGroupToFullSync();
      
      // We pulled a work element from a queue. Time to get busy.
      if ( queueItem == null )
    	  throw new IllegalStateException("Should always have pulled a queue item or gone back to top of loop");
      
      
      try {
        if ( queueItem != GROUP_CLEANUP_MARKER ) {
          GrouperGroupInfo grouperGroupInfo = queueItem.groupToProcess;
  		  MDC.put("what", grouperGroupInfo+"/");
  		  processGroup(grouperGroupInfo);
   	    }
    	else {
  		  // Time to look for extra groups
  		  if ( provisioner.config.isGrouperAuthoritative() ) {
  			MDC.put("what", "group_cleanup/");
  			processGroupCleanup();
  		  }
    	}
      }
      finally {
	    MDC.remove("what");
	  }
    }
  }
  
  
  /**
   * Go through the various full-sync queues and get the next GroupInfo object.
   * 
   * This method blocks until a group is ready for full-syncing
   */
  protected FullSyncQueueItem getNextGroupToFullSync() {
    while (true) {
      try {
        groupListLock.lock();
  
        // Grab a group from the first collection that has one or wait until
        // a group is added to one of them. Groups are grabbed from queues
        // in the following priority order:
        //   groupsToSyncAsap, groupsToSync, groupsToSyncRetry
        
        // NOTE: If a group is found in RETRY, then we sleep to prevent 
        //       hammering away at retry after retry
        
        if ( groupsToSyncAsap.size() > 0 ) {
          FullSyncQueueItem queueItem = groupsToSyncAsap.iterator().next();
          groupsToSyncAsap.remove(queueItem);
          return queueItem;
        } else if ( groupsToSync.size() > 0 )
          return groupsToSync.remove(0);
        else if ( groupsToSyncRetry.size() > 0 ) {
          FullSyncQueueItem queueItem = groupsToSyncRetry.iterator().next();
          
          // This is a group retry, Sleep to prevent hammering away
          try {
            Thread.sleep(provisioner.config.getSleepTimeAfterError_ms());
          } catch (InterruptedException e1) {
            // Nothing
          }
          return queueItem;
        }
        else {
          LOG.debug("No groups ready for FullSync. Waiting....");
          notEmptyCondition.awaitUninterruptibly();
        }
      }
      finally {
        groupListLock.unlock();
      }
    }    
  }
  
  
  /**
   * Go through the Grouper Groups and queue up the ones that match the provisioner's 
   * ShouldBeProvisioned filter.
   */
  protected void queueAllGroupsForFullSync() {
    Collection<Group> allGroups = GrouperDAOFactory.getFactory().getGroup().getAllGroups();
    
    for ( Group group : allGroups ) {
      GrouperGroupInfo grouperGroupInfo = new GrouperGroupInfo(group);
      if ( provisioner.shouldGroupBeProvisioned(grouperGroupInfo) )
        scheduleGroupForSync(grouperGroupInfo, false);
    }
    
    if ( provisioner.config.isGrouperAuthoritative())
      scheduleGroupCleanup();
  }
  
  
  /**
   * Put the given group in a queue for full syncing
   * @param asap: Should this group be done before others that were queued with !asap?
   * @param group
   */
  public void scheduleGroupForSync(GrouperGroupInfo grouperGroupInfo, boolean asap) {
    LOG.debug("Scheduling group for {} full-sync: {}", asap ? "asap" : "eventual", 
        grouperGroupInfo != null ? grouperGroupInfo : "<remove extra groups>");
    queueGroupForSync(grouperGroupInfo, asap ? groupsToSyncAsap : groupsToSync);
  }
  
  /**
   * Put a GROUP_CLEANUP_MARKER into the full-sync schedule. This means that
   * the target system will be checked for information about groups that either
   * no longer exist or that are no longer selected to be provisioned to the system.
   * @param group
   */
  public void scheduleGroupCleanup() {
    if ( provisioner.config.isGrouperAuthoritative() ) {
      LOG.debug("Scheduling group cleanup");
      queueGroupForSync(null, groupsToSync);
    } else {
      LOG.warn("Ignoring group-cleanup request because grouper is not authoritative within the target system");
    }
  }
  
  /**
   * Put the given group in the given full-sync queue
   * @param grouperGroupInfo Not surprisingly, this normally points to the group that you wish to fully sync.
   * However, this can also be null in which case a GROUP_CLEANUP_MARKER will be put on the queue.
   */
  private void queueGroupForSync(GrouperGroupInfo grouperGroupInfo, Collection<FullSyncQueueItem> queue) {
    try {
      groupListLock.lock();
      
      if ( grouperGroupInfo != null )
        queue.add(new FullSyncQueueItem(grouperGroupInfo));
      else
        queue.add(GROUP_CLEANUP_MARKER);
      
      notEmptyCondition.signal();
    }
    finally {
      groupListLock.unlock();
    }
  }
  
  /**
   * Workhorse method that handles the FullSync of a specific group.
   * @param grouperGroupInfo Group on which to do a Full Sync
   * @param asap Used to requeue the group in the case of an error
   */
  protected void processGroup(GrouperGroupInfo grouperGroupInfo) {
    try {
      LOG.debug("{}: Starting Full-Sync: {}", provisioner.getName(), grouperGroupInfo);
      provisioner.doFullSync(grouperGroupInfo);
    } catch (PspException e) {
      LOG.error("{}: Problem doing full sync. Requeuing {}: {}",
          provisioner.getName(), grouperGroupInfo, e.getMessage() );
      
      // Put the group into the error queue
      queueGroupForSync(grouperGroupInfo, groupsToSyncRetry);
    }
    catch (Throwable e) {
      LOG.error("{}: Problem doing full sync. Requeuing {}",
          provisioner.getName(), grouperGroupInfo, e );
      
      // Put the group into the error queue
      queueGroupForSync(grouperGroupInfo, groupsToSyncRetry);
    }
  }
  
  protected void processGroupCleanup() {
    try {
      LOG.debug("{}: Starting Group Cleanup", provisioner.getName());
      provisioner.doFullSync_cleanupExtraGroups();
    } catch (PspException e) {
      LOG.error("{}: Problem doing group cleanup: {}",
          provisioner.getName(), e.getMessage() );
    }
    catch (Throwable e) {
      LOG.error("{}: Problem doing group cleanup",
          provisioner.getName(), e );
    }
  
  }
  
  
}
