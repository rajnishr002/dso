/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tctest.statistics;

import com.tc.object.config.ConfigVisitor;
import com.tc.object.config.DSOClientConfigHelper;
import com.tc.simulator.app.ApplicationConfig;
import com.tc.simulator.listener.ListenerProvider;
import com.tctest.builtin.CyclicBarrier;
import com.tctest.runner.AbstractTransparentApp;

import java.util.concurrent.BrokenBarrierException;

public class StatisticsManagerNoActionsTestApp extends AbstractTransparentApp {

  public static final int     NODE_COUNT = 2;

  private final CyclicBarrier barrier    = new CyclicBarrier(NODE_COUNT);

  public StatisticsManagerNoActionsTestApp(final String appId, final ApplicationConfig cfg,
                                           final ListenerProvider listenerProvider) {
    super(appId, cfg, listenerProvider);
  }

  public void run() {
    barrier();
  }

  private void barrier() {
    try {
      barrier.await();
    } catch (InterruptedException ie) {
      throw new AssertionError();
    } catch (BrokenBarrierException e) {
      throw new AssertionError();
    }
  }

  public static void visitL1DSOConfig(final ConfigVisitor visitor, final DSOClientConfigHelper config) {
    String testClass = StatisticsManagerNoActionsTestApp.class.getName();

    config.getOrCreateSpec(testClass).addRoot("barrier", "barrier");

    config.addWriteAutolock("* " + testClass + "*.*(..)");
  }
}
