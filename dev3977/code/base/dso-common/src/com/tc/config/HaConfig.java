/*
 * All content copyright (c) 2003-2007 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tc.config;

import com.tc.config.schema.setup.ConfigurationSetupException;
import com.tc.net.GroupID;
import com.tc.net.groups.Node;
import com.tc.net.groups.ServerGroup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public interface HaConfig {

  /**
   * Returns true if more than 1 ActiveServerGroup's are defined
   */
  boolean isActiveActive();

  /**
   * Returns true if not active-active and ha mode is set to networked-active-passive
   */
  boolean isNetworkedActivePassive();

  /**
   * Returns true if not active-active and not networked-active-passive
   */
  boolean isDiskedBasedActivePassive();

  ServerGroup getActiveCoordinatorGroup();

  ServerGroup[] getAllActiveServerGroups();

  Node getThisNode();

  ServerGroup getThisGroup();

  Node[] getThisGroupNodes();

  Node[] getAllNodes();

  boolean isActiveCoordinatorGroup();

  /**
   * @return true if nodes are removed
   * @throws ConfigurationSetupException
   */
  public void reloadConfig(List<Node> nodesAdded, List<Node> nodesRemoved) throws ConfigurationSetupException;

  public HashMap<String, GroupID> getNodeNamesToGidMap();

  public HashSet<String> getNodeNames();
}
