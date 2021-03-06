/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.constant.test009.dx;


import com.android.jack.constant.test009.jack.Constant009;

import org.junit.Assert;
import org.junit.Test;

public class Tests {

  @Test
  public void testConstant1() {
    Assert.assertEquals(27526103, new Constant009().getAndIncInt0());
  }
  @Test
  public void testConstant2() {
    Assert.assertEquals(0, new Constant009().getAndIncLong0());
  }

  @Test
  public void testConstant3() {
    Assert.assertEquals(67108863, new Constant009().getAndIncInt1());
  }
  @Test
  public void testConstant4() {
    Assert.assertEquals(449133, new Constant009().getAndIncLong1());
  }

  @Test
  public void testConstant5() {
    Assert.assertEquals(-9223372036854775808L, new Constant009().getAndIncInt2());
  }
  @Test
  public void testConstant6() {
    Assert.assertEquals(-9223372036854775808L, new Constant009().getAndIncLong2());
  }
}
