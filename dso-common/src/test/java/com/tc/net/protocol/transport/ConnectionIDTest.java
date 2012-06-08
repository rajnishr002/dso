/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tc.net.protocol.transport;

import junit.framework.TestCase;

public class ConnectionIDTest extends TestCase {

  private static final String VALID_SERVER_ID    = "aaBBccddeeff11223344556677889900";
  private static final String INVALID_SERVER_ID1 = "Gabbccddeeff11223344556677889900"; // bad char
  private static final String INVALID_SERVER_ID2 = "abbccddeeff11223344556677889900"; // bad length

  public void test() {
    try {
      ConnectionID connectionID = ConnectionID.parse("12." + VALID_SERVER_ID + ".jvm1..");
      assertEquals(12, connectionID.getChannelID());
      assertEquals(VALID_SERVER_ID, connectionID.getServerID());
      assertEquals("jvm1", connectionID.getJvmID());
      assertEquals(false, connectionID.isJvmIDNull());
      assertEquals(false, connectionID.isSecured());

      connectionID = ConnectionID.parse("12." + VALID_SERVER_ID + ".jvm1.alex.secret..!");
      assertEquals(12, connectionID.getChannelID());
      assertEquals(VALID_SERVER_ID, connectionID.getServerID());
      assertEquals("jvm1", connectionID.getJvmID());
      assertEquals(false, connectionID.isJvmIDNull());
      assertEquals(true, connectionID.isSecured());
      assertEquals("alex", connectionID.getUsername());
      assertEquals("secret..!", new String(connectionID.getPassword()));
    } catch (InvalidConnectionIDException e) {
      fail(e.getMessage());
    }

    try {
      ConnectionID.parse("");
      fail();
    } catch (InvalidConnectionIDException e) {
      // expected
    }

    try {
      ConnectionID.parse(null);
      fail();
    } catch (InvalidConnectionIDException e) {
      // expected
    }

    try {
      ConnectionID.parse("sdljksdf");
      fail();
    } catch (InvalidConnectionIDException e) {
      // expected
    }

    try {
      ConnectionID.parse("." + VALID_SERVER_ID);
      fail();
    } catch (InvalidConnectionIDException e) {
      // expected
    }

    try {
      ConnectionID.parse(VALID_SERVER_ID + ".");
      fail();
    } catch (InvalidConnectionIDException e) {
      // expected
    }

    try {
      ConnectionID.parse(VALID_SERVER_ID + ".42");
      fail();
    } catch (InvalidConnectionIDException e) {
      // expected
    }

    try {
      ConnectionID.parse("212." + INVALID_SERVER_ID1);
      fail();
    } catch (InvalidConnectionIDException e) {
      // expected
    }

    try {
      ConnectionID.parse("144." + INVALID_SERVER_ID2);
      fail();
    } catch (InvalidConnectionIDException e) {
      // expected
    }

  }

}
