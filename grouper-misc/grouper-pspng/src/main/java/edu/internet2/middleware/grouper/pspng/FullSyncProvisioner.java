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

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.*;
import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;


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
    // How often to print the progress of full-sync tasks
    private static final int FULL_SYNC_PROGRESS_INTERVAL_SECS = 5;

    // This string is chosen to not match any group name
    private static final String FULL_SYNC_ALL_GROUPS = "::full-sync-all-groups::";
    private static final InheritableThreadLocal<FullSyncQueueItem> currentFullSyncItem
                = new InheritableThreadLocal<FullSyncQueueItem>();

    protected class FullSyncQueueItem {
      final String reason;
	  Date queuedTime = new Date();
	  JobStatistics stats = new JobStatistics();

	  // Either a group or 'null' to indicate that a sweep of extra groups
	  // should be performed
	  GrouperGroupInfo groupToProcess;
	  
	  public FullSyncQueueItem(GrouperGroupInfo grouperGroupInfo, String reason) {
		  this.groupToProcess = grouperGroupInfo;
		  this.reason = reason;
	}

	public boolean isCleanupRequest() {
	      return groupToProcess == null;
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

	private void processingStarting() {
	      stats.processingStartTime = new Date();
    }

    private void processingCompletedSuccessfully() {
	      // Only act the first time we're told this was completed
	      if ( stats.processingCompletedTime != null ) {
	          return;
          }

	      stats.processingCompletedTime = new Date();
	      LOG.info("{}: Full-sync processing finished. Stats: {}|{}",
                  new Object[]{ getName(), stats, this.getName()});
    }

    private void processingCompletedUnsuccessfully() {
        // Only act the first time we're told this was completed
        if ( stats.processingCompletedTime != null ) {
            return;
        }

        stats.processingCompletedTime = new Date();
        LOG.info("{}: Full-sync processing failed. {}",
                new Object[]{ getName(), this.getName()});
    }

        public boolean hasBeenProcessed() {
	  return stats.processingCompletedTime != null;
    }

    public String getName() {
	      if ( isCleanupRequest() ) {
	          return "Extra-Group Cleanup";
          }
          else {
	          return groupToProcess.toString();
          }
    }

    public String toString() {
	      return String.format("%s (%s)", getName(), reason);
    }
  }

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
          PspUtils.setupNewThread();
          try {
              thread_manageFullSyncProcessing();
          } catch (Throwable t) {
              LOG.error("{}: FullSync failed", getName(), t);
          }
          finally {
              LOG.warn("{}: FullSync thread has exitted", getName());
          }
      }
    }, getName() + "-Thread");
    
    t.setDaemon(true);
    t.start();

      Thread t2 = new Thread(new Runnable() {
          @Override
          public void run() {
              PspUtils.setupNewThread();
              try {
                  thread_fullSyncMessageQueueReader();
              } catch (Throwable t) {
                  LOG.error("{}: Full-sync queue reader failed", getName(), t);
              }
              finally {
                  LOG.error("{}: Full-sync queue reader has exitted", getName());
              }
          }
      }, getName() + "-MessageReaderThread");

      t2.setDaemon(true);
      t2.start();
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
    MDC.put("who", getName()+"/");
    
    GrouperSession.startRootSession();
    
    while (true) {
  	  FullSyncQueueItem queueItem = getNextGroupToFullSync();

  	  queueItem.processingStarting();

      if ( !provisioner.config.isEnabled() ) {
          LOG.warn("{} is disabled. Full-sync not being done.", provisioner.getName());
          continue;
      }

      // We pulled a work element from a queue. Time to get busy.
      if ( queueItem == null )
    	  throw new IllegalStateException("Should always have pulled a queue item or gone back to top of loop");
      
      try {
        if ( !queueItem.isCleanupRequest() ) {
          GrouperGroupInfo grouperGroupInfo = queueItem.groupToProcess;
          MDC.put("what", grouperGroupInfo+"/");
          MDC.put("why", queueItem.reason+"/");
  		  if (processGroup(grouperGroupInfo, queueItem)) {
  		      queueItem.processingCompletedSuccessfully();
          }
          else {
  		      queueItem.processingCompletedUnsuccessfully();
          }
   	    }
    	else {
  		  // Time to look for extra groups
  		  if ( provisioner.config.isGrouperAuthoritative() ) {
  			MDC.put("what", "group_cleanup/");
  			MDC.put("why", queueItem.reason + "/");
  			if (processGroupCleanup(queueItem)) {
  			    queueItem.processingCompletedSuccessfully();
            }
            else {
  			    queueItem.processingCompletedUnsuccessfully();
            }
  		  }
  		  else {
  		      LOG.warn("{}: Ignoring group-cleanup because grouper is not authoritative", getName());
  		      queueItem.processingCompletedUnsuccessfully();
          }
    	}
      }
      catch (Throwable t) {
          LOG.error("{}: Full-Sync processing failed: {}", new Object[]{getName(), queueItem, t});
          queueItem.processingCompletedUnsuccessfully();
      }
      finally {
	    MDC.remove("what");
	    MDC.remove("why");
	  }
    }
  }


    /**
     * Method that watches the full-sync queue (See Grouper Messaging) and copies
     * messages into the queues within this class for the full-sync threads to
     * process
     */
    protected void thread_fullSyncMessageQueueReader() {
        MDC.put("why", "full-sync-message-reader/");
        MDC.put("who", getName()+"/");

        LOG.info("{} message reader: Starting", getName());

        GrouperSession gs = GrouperSession.startRootSession();

        String queueName = "pspng_full_sync_" + provisioner.getName();
        GrouperBuiltinMessagingSystem.createQueue(queueName);
        GrouperBuiltinMessagingSystem.allowSendToQueue(queueName, gs.getSubject());
        GrouperBuiltinMessagingSystem.allowReceiveFromQueue(queueName, gs.getSubject());

        // In order to remain gentle in the face of repeating errors or empty queue-pull results,
        // we want to sleep in our loop. This variable is set to different values depending on
        // what happened in our last pull-and-process loop
        int nextSleepTime_secs = 0;

        LOG.info("{} message reader: created queue {} and granted send/receive permission to {}",
                new Object[]{getName(), queueName, gs.getSubject()});

        while (true) {
            if ( nextSleepTime_secs > 0 ) {
                GrouperUtil.sleep(1000L * nextSleepTime_secs);
            }

            // Normally, we don't want to sleep, so start each loop like nothing went wrong
            nextSleepTime_secs = 0;

            GrouperMessageReceiveParam receive = new GrouperMessageReceiveParam();
            receive.assignGrouperMessageSystemName(GrouperBuiltinMessagingSystem.BUILTIN_NAME);
            receive.assignQueueName(queueName);
            GrouperMessageReceiveResult grouperMessageReceiveResult = GrouperMessagingEngine.receive(receive);

            Collection<GrouperMessage> grouperMessages;
            try {
                LOG.debug("{} message reader: requesting messages from queue {}",
                        getName(), queueName);

                grouperMessages = grouperMessageReceiveResult.getGrouperMessages();
            }
            catch (Exception e) {
                nextSleepTime_secs = 15;
                LOG.error("{} message reader: Problem pulling messages from grouper message queue",
                        getName(), e);
                continue;
            }

            if ( grouperMessages.size() == 0 ) {
                nextSleepTime_secs = 5;
                LOG.info("{} message reader: no messages received", getName());
                continue;
            }

            LOG.info("{} message reader: received and processing {} messages",
                    getName(), grouperMessages.size());

            try {
                for (GrouperMessage message : grouperMessages) {
                    String body = message.getMessageBody();
                    LOG.info("{} message reader: Processing grouper message {} = {}",
                            new Object[]{getName(), message.getId(), body});

                    if ( body.equals(FULL_SYNC_ALL_GROUPS) ) {
                        queueAllGroupsForFullSync("Requested by message");
                    }
                    else {
                        Group group = GroupFinder.findByName(gs, body, false);

                        if (group == null) {
                            LOG.warn("{} message reader: Group was not found: {}", getName(), body);
                            continue;
                        }

                        scheduleGroupForSync(new GrouperGroupInfo(group), "from-message-system", true);

                        LOG.info("{} message reader: Group is queued for full sync: {}",
                                getName(), group);
                    }

                    // ACK that the message is done
                    GrouperMessageAcknowledgeParam ack = new GrouperMessageAcknowledgeParam();
                    ack.assignAcknowledgeType(GrouperMessageAcknowledgeType.mark_as_processed);
                    ack.assignQueueName(queueName);
                    ack.addGrouperMessage(message);
                    ack.assignGrouperMessageSystemName(GrouperBuiltinMessagingSystem.BUILTIN_NAME);

                    GrouperMessagingEngine.acknowledge(ack);
                }
            }
            catch (Exception e) {
                nextSleepTime_secs = 15;
                LOG.error("{} message reader: Problem while processing and acknowledging message",
                        getName(), e);
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
        LOG.debug("{}: Looking for full-sync tasks: Locking.", getName());
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
          LOG.debug("{}: Found asap full-sync task: {}", getName(), queueItem);
          return queueItem;
        } else if ( groupsToSync.size() > 0 ) {
            FullSyncQueueItem queueItem = groupsToSync.remove(0);
            LOG.debug("{}: Found full-sync task: {}", getName(), queueItem);
            return queueItem;
        }
        else if ( groupsToSyncRetry.size() > 0 ) {
          FullSyncQueueItem queueItem = groupsToSyncRetry.iterator().next();

          LOG.debug("{}: Found full-sync retry task: {}", getName(), queueItem);

          // This is a group retry, Sleep to prevent hammering away
          GrouperUtil.sleep(provisioner.config.getSleepTimeAfterError_ms());
          return queueItem;
        }
        else {
          LOG.debug("{}: No groups ready for FullSync. Waiting....", getName());
          notEmptyCondition.awaitUninterruptibly();
        }
      }
      finally {
        groupListLock.unlock();
      }
    }    
  }


    public JobStatistics startFullSyncOfAllGroupsAndWaitForCompletion() {
        JobStatistics overallStats = new JobStatistics();

        List<FullSyncProvisioner.FullSyncQueueItem> queuedGroupSyncs
                = queueAllGroupsForFullSync("Scheduled full sync");

        boolean everythingHasBeenCompleted = false;
        Date statusLastLoggedDate = null;

        // Monitor the progress of our full-sync task items
        int doneCount=0;
        while ( !everythingHasBeenCompleted ) {
            doneCount = 0;
            everythingHasBeenCompleted=true;
            for (FullSyncProvisioner.FullSyncQueueItem item : queuedGroupSyncs) {
                if ( item.hasBeenProcessed() ) {
                    doneCount++;
                }
                else {
                    everythingHasBeenCompleted=false;
                }
            }

            // log the progress every FULL_SYNC_PROGRESS_INTERVAL_SECS
            if ( statusLastLoggedDate==null ||
                    (System.currentTimeMillis()-statusLastLoggedDate.getTime())/1000L > FULL_SYNC_PROGRESS_INTERVAL_SECS ) {
                LOG.info("{}: Full Sync of all groups: {} steps are done out of {} ({}%)",
                        new Object[]{getName(),
                                doneCount, queuedGroupSyncs.size(),
                                100.0 * doneCount / queuedGroupSyncs.size()});
                statusLastLoggedDate = new Date();
            }

            // Sleep for a bit to let the full-syncs progress
            if ( !everythingHasBeenCompleted ) {
                GrouperUtil.sleep(250);
            }
        }

        LOG.info("{}: Full Sync of all groups: {} steps are done out of {} ({}%)",
                new Object[]{getName(),
                        doneCount, queuedGroupSyncs.size(),
                        100.0 * doneCount / queuedGroupSyncs.size()});

        for (FullSyncProvisioner.FullSyncQueueItem item : queuedGroupSyncs) {
            overallStats.add(item.stats);
        }
        overallStats.done();

        LOG.info("{}: Full Sync of all groups: Finished. Stats: {}", getName(), overallStats);
        return overallStats;
    }

    /**
   * Go through the Grouper Groups and queue up the ones that match the provisioner's 
   * ShouldBeProvisioned filter.
   */
  protected List<FullSyncQueueItem> queueAllGroupsForFullSync(String reason) {
    LOG.info("{}: Queuing all groups for full sync. ({})", getName(), reason);
    List<FullSyncQueueItem> result = new ArrayList<FullSyncQueueItem>();

    Collection<Group> allGroups = provisioner.getAllGroupsForProvisioner();
    for ( Group group : allGroups ) {
      GrouperGroupInfo grouperGroupInfo = new GrouperGroupInfo(group);
      result.add(scheduleGroupForSync(grouperGroupInfo, reason, false));
    }
    
    if ( provisioner.config.isGrouperAuthoritative()) {
        FullSyncQueueItem cleanupItem = scheduleGroupCleanup();
        if ( cleanupItem != null ) {
            result.add(cleanupItem);
        }
    }

    return result;
  }
  
  
  /**
   * Put the given group in a queue for full syncing
   * @param asap: Should this group be done before others that were queued with !asap?
   * @param grouperGroupInfo
   */
  public FullSyncQueueItem scheduleGroupForSync(GrouperGroupInfo grouperGroupInfo, String reason, boolean asap) {
    LOG.debug("Scheduling group for {} full-sync: {}: {}",
            new Object[] {
                asap ? "asap" : "eventual",
                grouperGroupInfo != null ? grouperGroupInfo : "<remove extra groups>",
                reason
            }
        );
    return queueGroupForSync(grouperGroupInfo, reason, asap ? groupsToSyncAsap : groupsToSync);
  }
  
  /**
   * Put a GROUP_CLEANUP_MARKER into the full-sync schedule. This means that
   * the target system will be checked for information about groups that either
   * no longer exist or that are no longer selected to be provisioned to the system.
   */
  public FullSyncQueueItem scheduleGroupCleanup() {
    if ( provisioner.config.isGrouperAuthoritative() ) {
      LOG.debug("Scheduling group cleanup");
      return queueGroupForSync(null,
              "Cleanup as part of scheduled full sync", groupsToSync);
    } else {
      LOG.warn("Ignoring group-cleanup request because grouper is not authoritative within the target system");
      return null;
    }
  }
  
  /**
   * Put the given group in the given full-sync queue
   * @param grouperGroupInfo Not surprisingly, this normally points to the group that you wish to fully sync.
   * However, this can also be null in which case a group-cleanup entry will be put on the queue.
   */
  private FullSyncQueueItem
  queueGroupForSync(GrouperGroupInfo grouperGroupInfo, String reason, Collection<FullSyncQueueItem> queue) {
    FullSyncQueueItem result;
    try {
      groupListLock.lock();
      
      if ( grouperGroupInfo != null ) {
          result = new FullSyncQueueItem(grouperGroupInfo, reason);
      }
      else {
          result = new FullSyncQueueItem(null, reason);
      }

      queue.add(result);
      
      notEmptyCondition.signal();
      return result;
    }
    finally {
      groupListLock.unlock();
    }
  }
  
  /**
   * Workhorse method that handles the FullSync of a specific group.
   * @param grouperGroupInfo Group on which to do a Full Sync
   * @param fullSyncQueueItem What is driving this sync
   * @return true if successful
   */
  protected boolean processGroup(GrouperGroupInfo grouperGroupInfo, FullSyncQueueItem fullSyncQueueItem) {
    ProvisioningWorkItem workItem = new ProvisioningWorkItem("FullSync", grouperGroupInfo);
    try {
      MDC.put("step", "start/");
      LOG.info("{}: Starting Full-Sync ({}) of group {}",
              new Object[]{getName(), fullSyncQueueItem.reason, grouperGroupInfo});

      provisioner.startProvisioningBatch(Arrays.asList(workItem));
      
      MDC.put("step",  "doit/");

      provisioner.setCurrentWorkItem(workItem);
      provisioner.doFullSync(grouperGroupInfo, fullSyncQueueItem.stats);
      
      MDC.put("step", "fin/");
      provisioner.finishProvisioningBatch(Arrays.asList(workItem));
      return true;
    } catch (PspException e) {
      LOG.error("{}: Problem doing full sync. Requeuing group {}",
          new Object[]{ getName(), grouperGroupInfo, e} );
      
      // Put the group into the error queue
      queueGroupForSync(grouperGroupInfo, fullSyncQueueItem.reason, groupsToSyncRetry);
      return false;
    }
    catch (Throwable e) {
      LOG.error("{}: Problem doing full sync. Requeuing group {}",
          new Object[] {getName(), grouperGroupInfo, e });
      
      // Put the group into the error queue
      queueGroupForSync(grouperGroupInfo, fullSyncQueueItem.reason, groupsToSyncRetry);
      return false;
    }
    finally {
      MDC.remove("step");
    }
  }
  
  protected boolean processGroupCleanup(FullSyncQueueItem queueItem) {
    try {
      LOG.info("{}: Starting Group Cleanup ({})", getName(), queueItem.reason);
      ProvisioningWorkItem workItem = new ProvisioningWorkItem("RemoveExtraGroups", null);
      MDC.put("step", "start/");
      provisioner.startProvisioningBatch(Arrays.asList(workItem));

      MDC.put("step", "doit/");
      provisioner.setCurrentWorkItem(workItem);
      provisioner.doFullSync_cleanupExtraGroups(queueItem.stats);
      
      MDC.put("step",  "fin/");
      provisioner.finishProvisioningBatch(Arrays.asList(workItem));
      LOG.info("{}: Group-cleanup done. Stats: {}", getName(), queueItem.stats);
      return true;
    } catch (PspException e) {
      LOG.error("{}: Problem doing group cleanup",
          getName(), e );
      return false;
    }
    catch (Throwable e) {
      LOG.error("{}: Problem doing group cleanup",
          getName(), e );
      return false;
    }
    finally {
      MDC.remove("step");
    }
  
  }

  public String getName() {
      return provisioner.getName() + "-FullSync";
  }
  
  
}
