/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company 2024
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tc.net.protocol.transport;

public class TransportHandshakeErrorContext implements TransportHandshakeError {
  private String    message;
  private short     errorType;
  private Throwable throwable;

  public TransportHandshakeErrorContext(String message) {
    this.message = message;
    this.errorType = ERROR_GENERIC;
  }

  public TransportHandshakeErrorContext(String message, Throwable throwable) {
    this(message);
    this.errorType = ERROR_GENERIC;
    this.throwable = throwable;
  }

  public TransportHandshakeErrorContext(String message, short errorType) {
    this(message);
    this.errorType = errorType;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public short getErrorType() {
    return errorType;
  }

  @Override
  public String toString() {
    StringBuffer rv = new StringBuffer(getClass().getName() + ": " + this.message);
    if (this.throwable != null) {
      rv.append(", throwable=" + throwable.getMessage());
    }
    return rv.toString();
  }
}
