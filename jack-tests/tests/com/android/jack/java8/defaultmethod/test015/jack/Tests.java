/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.java8.defaultmethod.test015.jack;

import org.junit.Assert;
import org.junit.Test;

/**
 * Check that when several methods are inherited from super interfaces, the method from a subtype is
 * chosen rather than from a super type.
 */
public class Tests {

  @Test
  public void test001() {
    DefaultMethod l = new DefaultMethod();
    Assert.assertEquals(2, l.test());
  }
}
