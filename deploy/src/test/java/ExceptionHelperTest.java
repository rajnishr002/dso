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
import org.eclipse.jetty.util.MultiException;

import com.tc.exception.ExceptionHelper;
import com.tc.exception.ExceptionHelperImpl;
import com.tc.exception.MortbayMultiExceptionHelper;
import com.tc.exception.RuntimeExceptionHelper;

import junit.framework.TestCase;

public class ExceptionHelperTest extends TestCase {
  public void test() {
    ExceptionHelperImpl helper = new ExceptionHelperImpl();
    helper.addHelper(new RuntimeExceptionHelper());
    helper.addHelper(new MortbayMultiExceptionHelper());

    Throwable ultimateCause = new RuntimeException();
    Exception proximateCause = new MultiException();
    ((MultiException) proximateCause).add(ultimateCause);
    Exception top = new RuntimeException(proximateCause);
    check(helper, ultimateCause, proximateCause, top);
  }

  private void check(ExceptionHelper helper, Throwable ultimateCause, Exception proximateCause, Exception top) {
    assertSame(ultimateCause, helper.getUltimateCause(top));
    assertSame(ultimateCause, helper.getUltimateCause(proximateCause));
    assertSame(ultimateCause, helper.getUltimateCause(ultimateCause));
    assertSame(proximateCause, helper.getProximateCause(top));
    assertSame(ultimateCause, helper.getProximateCause(proximateCause));
    assertSame(ultimateCause, helper.getProximateCause(ultimateCause));
  }
}
