/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tc.admin.dso.locks;

import com.tc.admin.AdminClient;
import com.tc.admin.common.XObjectTableModel;
import com.tc.management.lock.stats.LockSpec;
import com.tc.object.lockmanager.api.LockID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class ServerLockTableModel extends XObjectTableModel {
  static protected String[]                cNames      = (String[]) AdminClient.getContext()
                                                           .getObject("dso.locks.column.headings");
  static protected String[]                cFields     = { "Name", "Requested", "Hops", "Waiters", "AcquireTime",
      "HeldTime"                                      };
  static protected String[]                cTips       = (String[]) AdminClient.getContext()
                                                           .getObject("dso.locks.column.tips");

  public static final ServerLockTableModel EMPTY_MODEL = new ServerLockTableModel();

  public ServerLockTableModel() {
    super(LockSpecWrapper.class, cFields, cNames);
  }

  public ServerLockTableModel(Collection<LockSpec> lockInfos) {
    this();
    ArrayList list = new ArrayList<LockSpecWrapper>();
    Iterator<LockSpec> iter = lockInfos.iterator();
    while (iter.hasNext()) {
      list.add(new LockSpecWrapper(iter.next()));
    }
    Collections.sort(list, new Comparator<LockSpecWrapper>() {
      public int compare(LockSpecWrapper o1, LockSpecWrapper o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    add(list);
  }

  public void notifyChanged() {
    fireTableDataChanged();
  }
  
  public static String columnTip(int column) {
    return cTips[column];
  }

  public int wrapperIndex(LockID lockID) {
    int count = getRowCount();
    
    for(int i = 0; i < count; i++) {
      LockSpecWrapper wrapper = (LockSpecWrapper)getObjectAt(i);
      if(wrapper.getLockID().equals(lockID)) {
        return i;
      }
    }
    
    return -1;
  }
  
  public static class LockSpecWrapper {
    private LockSpec fLockSpec;
    private String   fName;

    LockSpecWrapper(LockSpec lockSpec) {
      fLockSpec = lockSpec;

      fName = fLockSpec.getLockID().asString();
      String objectType = fLockSpec.getObjectType();
      if (objectType != null && objectType.length() > 0) {
        fName += " (" + objectType + ")";
      }
    }

    public LockID getLockID() {
      return fLockSpec.getLockID();
    }
    
    public String getName() {
      return fName;
    }

    public long getRequested() {
      return fLockSpec.getServerStats().getNumOfLockRequested();
    }

    public long getHops() {
      return fLockSpec.getServerStats().getNumOfLockHopRequests();
    }

    public long getWaiters() {
      return fLockSpec.getServerStats().getAvgNumberOfPendingRequests();
    }

    public long getAcquireTime() {
      return fLockSpec.getServerStats().getAvgWaitTimeToAwardInMillis();
    }

    public long getHeldTime() {
      return fLockSpec.getServerStats().getAvgHeldTimeInMillis();
    }
  }
}
