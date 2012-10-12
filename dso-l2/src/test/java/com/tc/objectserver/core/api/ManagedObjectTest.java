/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tc.objectserver.core.api;

import org.terracotta.corestorage.KeyValueStorageConfig;
import org.terracotta.corestorage.StorageManager;
import org.terracotta.corestorage.heap.HeapStorageManager;

import com.tc.object.ObjectID;
import com.tc.object.SerializationUtil;
import com.tc.object.TestDNACursor;
import com.tc.object.tx.TransactionID;
import com.tc.objectserver.api.ObjectInstanceMonitor;
import com.tc.objectserver.impl.ObjectInstanceMonitorImpl;
import com.tc.objectserver.managedobject.ApplyTransactionInfo;
import com.tc.objectserver.managedobject.ManagedObjectImpl;
import com.tc.objectserver.managedobject.ManagedObjectStateFactory;
import com.tc.objectserver.managedobject.NullManagedObjectChangeListenerProvider;
import com.tc.objectserver.persistence.Persistor;
import com.tc.objectserver.persistence.StorageManagerFactory;
import com.tc.test.TCTestCase;

import java.util.Map;

public class ManagedObjectTest extends TCTestCase {

  public void testBasics() throws Exception {
    final ObjectInstanceMonitor instanceMonitor = new ObjectInstanceMonitorImpl();
    final ObjectID objectID = new ObjectID(1);
    ManagedObjectStateFactory.disableSingleton(true);
    Persistor persistor = new Persistor(new StorageManagerFactory() {
      @Override
      public StorageManager createStorageManager(final Map<String, KeyValueStorageConfig<?, ?>> configMap) {
        return new HeapStorageManager(configMap);
      }
    });
    ManagedObjectStateFactory.createInstance(new NullManagedObjectChangeListenerProvider(), persistor);

    final ManagedObjectImpl mo = new ManagedObjectImpl(objectID);

    assertTrue(mo.isDirty());
    assertTrue(mo.isNew());

    final TestDNACursor cursor = new TestDNACursor();
    cursor.addLogicalAction(SerializationUtil.PUT, new Object[] { Integer.valueOf(10), "King Kong" });
    cursor.addLogicalAction(SerializationUtil.PUT, new Object[] { Integer.valueOf(20), "Mad Max" });
    cursor.addLogicalAction(SerializationUtil.PUT, new Object[] { Integer.valueOf(25), "Mummy Returns" });
    cursor.addLogicalAction(SerializationUtil.PUT, new Object[] { Integer.valueOf(100), new ObjectID(10000) });
    final TestDNA dna = new TestDNA(cursor);

    Map instances = instanceMonitor.getInstanceCounts();
    assertEquals(0, instances.size());
    mo.apply(dna, new TransactionID(1), new ApplyTransactionInfo(), instanceMonitor, false);

    instances = instanceMonitor.getInstanceCounts();
    assertEquals(1, instances.size());
    assertEquals(Integer.valueOf(1), instances.get(dna.getTypeName()));

    assertEquals(1, mo.getObjectReferences().size());
    assertEquals(dna.typeName, mo.getClassname());
  }

  public void testIsNewToDB() throws Exception {
    final ObjectID objectID = new ObjectID(1);

    final ManagedObjectImpl mo = new ManagedObjectImpl(objectID);

    assertTrue(mo.isDirty());
    assertTrue(mo.isNew());
    assertTrue(mo.isNewInDB());

    mo.setIsDirty(true);
    assertTrue(mo.isDirty());
    assertTrue(mo.isNewInDB());

    mo.setIsDirty(false);
    assertFalse(mo.isDirty());
    assertFalse(mo.isNewInDB());

    mo.setIsDirty(true);
    assertTrue(mo.isDirty());
    assertFalse(mo.isNewInDB());

    mo.setIsDirty(false);
    assertFalse(mo.isDirty());
    assertFalse(mo.isNewInDB());

  }

  public void testApplyDNASameOrLowerVersion() throws Exception {
    final ObjectInstanceMonitor instanceMonitor = new ObjectInstanceMonitorImpl();
    final ObjectID objectID = new ObjectID(1);
    ManagedObjectStateFactory.disableSingleton(true);
    Persistor persistor = new Persistor(new StorageManagerFactory() {
      @Override
      public StorageManager createStorageManager(final Map<String, KeyValueStorageConfig<?, ?>> configMap) {
        return new HeapStorageManager(configMap);
      }
    });
    ManagedObjectStateFactory.createInstance(new NullManagedObjectChangeListenerProvider(), persistor);

    final ManagedObjectImpl mo = new ManagedObjectImpl(objectID);

    final TestDNACursor cursor = new TestDNACursor();
    cursor.addLogicalAction(SerializationUtil.PUT, new Object[] { Integer.valueOf(10), "King Kong" });
    TestDNA dna = new TestDNA(cursor);
    dna.version = 10;

    mo.apply(dna, new TransactionID(1), new ApplyTransactionInfo(), instanceMonitor, false);

    // Now reapply and see if it fails.
    boolean failed = false;
    try {
      mo.apply(dna, new TransactionID(1), new ApplyTransactionInfo(), instanceMonitor, false);
      failed = true;
    } catch (final AssertionError ae) {
      // expected.
    }
    if (failed) { throw new AssertionError("Failed to fail !!!"); }

    // Apply with lower version
    try {
      dna = new TestDNA(cursor);
      dna.version = 5;
      dna.isDelta = true;
      mo.apply(dna, new TransactionID(1), new ApplyTransactionInfo(), instanceMonitor, false);
      failed = true;
    } catch (final AssertionError ae) {
      // expected.
    }
    if (failed) { throw new AssertionError("Failed to fail !!!"); }

    // Apply with higher
    dna = new TestDNA(cursor);
    dna.version = 15;
    dna.isDelta = true;
    mo.apply(dna, new TransactionID(1), new ApplyTransactionInfo(), instanceMonitor, false);

    final long version = mo.getVersion();

    // Apply in passive, ignore as true.
    dna = new TestDNA(cursor);
    dna.version = 5;
    dna.isDelta = true;
    mo.apply(dna, new TransactionID(1), new ApplyTransactionInfo(), instanceMonitor, true);

    assertTrue(version == mo.getVersion());

    dna = new TestDNA(cursor);
    dna.version = 15;
    dna.isDelta = true;
    mo.apply(dna, new TransactionID(1), new ApplyTransactionInfo(), instanceMonitor, true);

    assertTrue(version == mo.getVersion());

    dna = new TestDNA(cursor);
    dna.version = 17;
    dna.isDelta = true;
    mo.apply(dna, new TransactionID(1), new ApplyTransactionInfo(), instanceMonitor, true);

    assertTrue(version < mo.getVersion());
  }

}
