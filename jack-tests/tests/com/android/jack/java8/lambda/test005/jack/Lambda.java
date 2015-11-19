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

package com.android.jack.java8.lambda.test005.jack;

/**
 * Lambda expression with generic.
 */
public class Lambda {

  public int testAddInt(int i1, int i2) {
    I<Number> i = (a, b) -> a.intValue() + b.intValue();
    return i.add(new Integer(i1), new Integer(i2));
  }

  public int testAddDouble(double d1, double d2) {
    I<Number> i = (a, b) -> a.intValue() + b.intValue();
    return i.add(new Double(d1), new Double(d2));
  }
}