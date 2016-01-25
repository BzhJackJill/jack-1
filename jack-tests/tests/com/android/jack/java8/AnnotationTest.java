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

package com.android.jack.java8;

import org.junit.Test;

import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

/**
 * JUnit test for compilation of annotations.
 */
public class AnnotationTest {

  private RuntimeTestInfo ANNOTATION001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.annotation.test001"),
      "com.android.jack.java8.annotation.test001.jack.Tests");

  private RuntimeTestInfo ANNOTATION002 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.annotation.test002"),
      "com.android.jack.java8.annotation.test002.jack.Tests");

  private RuntimeTestInfo ANNOTATION003 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.annotation.test003"),
      "com.android.jack.java8.annotation.test003.jack.Tests");

  @Test
  @KnownIssue
  public void testAnnotation001() throws Exception {
    new RuntimeTestHelper(ANNOTATION001)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  @KnownIssue
  public void testAnnotation002() throws Exception {
    new RuntimeTestHelper(ANNOTATION002)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  @KnownIssue
  public void testAnnotation003() throws Exception {
    new RuntimeTestHelper(ANNOTATION003)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }
}