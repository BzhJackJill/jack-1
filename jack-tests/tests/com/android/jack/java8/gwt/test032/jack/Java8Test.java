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

package com.android.jack.java8.gwt.test032.jack;

import org.junit.Test;

import junit.framework.Assert;

public class Java8Test {

  interface Lambda<T> {
    T run(int a, int b);
  }


  class AcceptsLambda<T> {
    public T accept(Lambda<T> foo) {
      return foo.run(10, 20);
    }
  }

  interface DefaultInterface {
    void method1();

    default int method2() {
      return 42;
    }

    default int redeclaredAsAbstract() {
      return 88;
    }

    default Integer addInts(int x, int y) {
      return x + y;
    }

    default String print() {
      return "DefaultInterface";
    }
  }

  static class VirtualUpRef {
    public int method2() {
      return 99;
    }

    public int redeclaredAsAbstract() {
      return 44;
    }
  }

  static class DefaultInterfaceImplVirtualUpRef extends VirtualUpRef implements DefaultInterface {
    public void method1() {}
  }

  @Test
  public void testDefaultMethodReference() {
    DefaultInterfaceImplVirtualUpRef x = new DefaultInterfaceImplVirtualUpRef();
    Assert.assertEquals(30, (int) new AcceptsLambda<Integer>().accept(x::addInts));
  }
}
