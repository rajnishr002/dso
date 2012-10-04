package com.tc.objectserver.persistence.gb;

import com.tc.gbapi.GBManager;
import com.tc.gbapi.GBMap;
import com.tc.gbapi.GBMapFactory;
import com.tc.gbapi.impl.GBOnHeapMapConfig;
import com.tc.object.ObjectID;

/**
 * @author tim
 */
public class GBPersistentObjectFactory {
  private final GBManager gbManager;
  private final GBMapFactory factory;

  public GBPersistentObjectFactory(final GBManager gbManager, final GBMapFactory factory) {
    this.gbManager = gbManager;
    this.factory = factory;
  }

  public GBMap<Object, Object> createMap(ObjectID objectID) {
    GBMap<Object, Object> map = factory.createMap(new GBOnHeapMapConfig<Object, Object>(Object.class, Object.class));
    gbManager.attachMap(objectID.toString(), map, Object.class, Object.class);
    return map;
  }

  public GBMap<Object, Object> getMap(final ObjectID id) {
    return gbManager.getMap(id.toString(), Object.class, Object.class);
  }

  public void destroyMap(ObjectID oid) {
    gbManager.detachMap(oid.toString());
  }
}
