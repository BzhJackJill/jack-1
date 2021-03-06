/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.java8.defaultmethod.test003.jack;

interface I1 {
  default int add(int v1, int v2) {
    return v1 + v2;
  }
}

interface I extends I1{
}

/**
 * Inherits default method.
 */
public class DefaultMethod implements I {

  public int addValue(int v1, int v2) {
    return add(v1,v2);
  }

  public int addValueWithCastToI(int v1, int v2) {
    return ((I) this).add(v1,v2);
  }

  public int addValueWithCastToI1(int v1, int v2) {
    return ((I1) this).add(v1,v2);
  }
}
