/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright notice.  All rights reserved.
 */
package com.tc.util;

public interface TickerTokenHandler {
  
  public static final TickerTokenHandler NULL_TICKER_TOKEN_HANDLER = new TickerTokenHandler() {

    public TickerToken processToken(TickerToken token) {
      return token;
    }
    
  };
  
  public TickerToken processToken( TickerToken token );
  
}
