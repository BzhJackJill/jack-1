/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.jack.java8.gwt.test017.jack;

import org.junit.Test;

import junit.framework.Assert;

public class Java8Test {

  interface I {
    int foo(Integer i);
  }

  @Test
  public void testSuperReferenceExpression() {
    class Y {
      int foo(Integer i) {
        return 42;
      }
    }

    class X extends Y {
      int foo(Integer i) {
        return 23;
      }

      int goo() {
        I i = super::foo;
        return i.foo(0);
      }
    }

    Assert.assertEquals(42, new X().goo());
  }
}
