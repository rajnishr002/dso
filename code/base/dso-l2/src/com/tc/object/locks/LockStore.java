/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package com.tc.object.locks;

import com.tc.util.Assert;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

public class LockStore {
  private static final int            DEFAULT_SEGMENTS = 32;
  private final HashMap<LockID, Lock> segments[];
  private final ReentrantLock[]       locks;
  private final int                   segmentShift;
  private final int                   segmentMask;
  private final LockFactory           lockFactory;

  public LockStore(LockFactory factory) {
    this(DEFAULT_SEGMENTS, factory);
  }

  public LockStore(int noOfSegments, LockFactory factory) {
    if (noOfSegments <= 0) throw new IllegalArgumentException();

    this.lockFactory = factory;
    // Find power-of-two sizes best matching arguments
    int sshift = 0;
    int ssize = 1;
    while (ssize < noOfSegments) {
      ++sshift;
      ssize <<= 1;
    }
    segmentShift = 32 - sshift;
    segmentMask = ssize - 1;
    noOfSegments = ssize;

    segments = new HashMap[noOfSegments];
    locks = new ReentrantLock[noOfSegments];

    for (int i = 0; i < segments.length; i++) {
      segments[i] = new HashMap();
      locks[i] = new ReentrantLock();
    }
  }

  public Lock checkOut(LockID lockID) {
    int index = indexFor(lockID);
    locks[index].lock();
    Lock lock = segments[index].get(lockID);
    if (lock == null) {
      lock = lockFactory.createLock(lockID);
      segments[index].put(lockID, lock);
    }
    return lock;
  }

  // Assumption that the lock is already held i.e. checked out
  public Lock remove(LockID lockID) {
    int index = indexFor(lockID);
    Assert.assertTrue(locks[index].isHeldByCurrentThread());
    Lock lock = segments[index].remove(lockID);
    return lock;
  }

  public void checkIn(Lock lock) {
    LockID lockID = lock.getLockID();
    int index = indexFor(lockID);
    locks[index].unlock();
  }

  private final int indexFor(Object o) {
    int hash = hash(o);
    return ((hash >>> segmentShift) & segmentMask);
  }

  /**
   * Currently from CHM
   */
  private static int hash(Object x) {
    int h = x.hashCode();
    h += ~(h << 9);
    h ^= (h >>> 14);
    h += (h << 4);
    h ^= (h >>> 10);
    return h;
  }

  public void clear() {
    for (int i = 0; i < locks.length; i++) {
      locks[i].lock();
      segments[i].clear();
      locks[i].unlock();
    }
  }

  public LockIterator iterator() {
    return new LockIterator();
  }

  public class LockIterator {
    private Iterator<Entry<LockID, Lock>> currentIter;
    private int                           currentIndex = -1;
    private Lock                          oldLock;

    /**
     * This method basically fetches the next lock by checking it out and checks back in the oldLock (that was given
     * last by this method). This method is a replacement for iterator keeping in the check out/in logic. NOTE: If you
     * do not complete the iteration then please check back in the lock. Otherwise it might result in a segment locked
     * forever.
     */
    public Lock getNextLock(Lock lock) {
      validateOldLock(lock);
      if (currentIter == null || !currentIter.hasNext()) {
        if (!tryMovingToNextSegment()) { return null; }
      }
      Assert.assertNotNull(currentIter);
      return currentIter.next().getValue();
    }
    
    public void checkIn(Lock lock) {
      Assert.assertEquals(oldLock, lock);
      LockStore.this.checkIn(lock);
    }

    private void validateOldLock(Lock lock) {
      if (oldLock != null) {
        Assert.assertEquals(oldLock, lock);
      } else {
        Assert.assertNull(lock);
      }

    }

    private boolean tryMovingToNextSegment() {
      currentIndex++;
      if (currentIndex >= segments.length) { return false; }
      if (currentIndex <= 0) {
        locks[currentIndex].unlock();
      }

      locks[currentIndex].lock();
      currentIter = segments[currentIndex].entrySet().iterator();
      return true;
    }
  }
}