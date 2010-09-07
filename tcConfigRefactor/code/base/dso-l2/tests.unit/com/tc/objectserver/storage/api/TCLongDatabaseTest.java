/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package com.tc.objectserver.storage.api;

import org.apache.commons.io.FileUtils;

import com.tc.object.config.schema.NewL2DSOConfig;
import com.tc.objectserver.storage.api.TCDatabaseReturnConstants.Status;
import com.tc.test.TCTestCase;
import com.tc.util.Assert;

import java.io.File;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class TCLongDatabaseTest extends TCTestCase {
  private File                           dbHome;
  private DBEnvironment                  dbenv;
  private PersistenceTransactionProvider ptp;

  private TCLongDatabase                 database;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    File dataPath = getTempDirectory();

    dbHome = new File(dataPath.getAbsolutePath(), NewL2DSOConfig.OBJECTDB_DIRNAME);
    dbHome.mkdir();

    dbenv = new DBFactoryForDBUnitTests(new Properties()).createEnvironment(true, dbHome);
    dbenv.open();

    ptp = dbenv.getPersistenceTransactionProvider();
    database = dbenv.getClientStateDatabase();
  }

  public void testPutGetAll() {
    long[] keys = new long[1000];
    for (int i = 0; i < 1000; i++) {
      keys[i] = i;
    }

    for (int i = 0; i < keys.length; i++) {
      PersistenceTransaction tx = ptp.newTransaction();
      Status status = database.put(keys[i], tx);
      tx.commit();

      Assert.assertEquals(Status.SUCCESS, status);
    }

    PersistenceTransaction tx = ptp.newTransaction();
    Set<Long> keysTemp = database.getAllKeys(tx);
    TreeSet<Long> keysFetched = new TreeSet<Long>();
    keysFetched.addAll(keysTemp);

    Assert.assertEquals(keys.length, keysFetched.size());

    int counter = 0;
    for (Long key : keysFetched) {
      Assert.assertTrue(keys[counter] == key);
      counter++;
    }
  }

  public void testContains() {
    long[] keys = new long[1000];
    for (int i = 0; i < 1000; i++) {
      keys[i] = i;
    }

    for (int i = 0; i < keys.length; i++) {
      PersistenceTransaction tx = ptp.newTransaction();
      Status status = database.put(keys[i], tx);
      tx.commit();

      Assert.assertEquals(Status.SUCCESS, status);
    }

    for (int i = 0; i < keys.length; i++) {
      PersistenceTransaction tx = ptp.newTransaction();
      Assert.assertTrue(database.contains(keys[i], tx));
      tx.commit();
    }
  }

  public void testDelete() {
    long[] keys = new long[1000];
    for (int i = 0; i < 1000; i++) {
      keys[i] = i;
    }

    for (int i = 0; i < keys.length; i++) {
      PersistenceTransaction tx = ptp.newTransaction();
      Status status = database.put(keys[i], tx);
      tx.commit();

      Assert.assertEquals(Status.SUCCESS, status);
    }

    PersistenceTransaction tx = ptp.newTransaction();
    Set<Long> keysTemp = database.getAllKeys(tx);
    Assert.assertEquals(keys.length, keysTemp.size());

    for (int i = 0; i < keys.length; i++) {
      tx = ptp.newTransaction();
      Status status = database.delete(keys[i], tx);
      tx.commit();

      Assert.assertEquals(Status.SUCCESS, status);
    }

    tx = ptp.newTransaction();
    keysTemp = database.getAllKeys(tx);
    Assert.assertEquals(0, keysTemp.size());
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    try {
      dbenv.close();
      FileUtils.cleanDirectory(dbHome);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
