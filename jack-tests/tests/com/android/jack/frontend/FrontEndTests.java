/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.frontend;

import com.android.jack.Options;
import com.android.jack.test.category.ExtraTests;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.IncrementalToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FrontEndTests {

  @Test
  @KnownIssue
  public void testMissingClass001() throws Exception {
    File outJackTmpMissing = AbstractTestTools.createTempDir();
    File outJackTmpSuper = AbstractTestTools.createTempDir();
    File outJackTmpTest = AbstractTestTools.createTempDir();

    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain();

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        outJackTmpMissing,
        /* zipFiles= */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.frontend.test001.jack.missing"));

    toolchain =  AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(outJackTmpMissing)
    .srcToLib(outJackTmpSuper,
        /* zipFiles= */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.frontend.test001.jack.sub2"));

    toolchain =  AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(outJackTmpSuper)
    .srcToLib(outJackTmpTest,
        /* zipFiles= */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.frontend.test001.jack.test"));

  }

  /**
   * Test that we do not crash and that we report the error.
   */
  @Test
  public void testConflictingPackage001() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setOutputStream(out);
    toolchain.setErrorStream(err);
    toolchain.addProperty(Options.INPUT_FILTER.getName(), "ordered-filter");

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          outDir,
          /* zipFiles= */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test002.jack"));
      Assert.fail();
    } catch (FrontendCompilationException e) {
      Assert.assertEquals(0, out.size());
      String errString = err.toString();
      Assert.assertTrue(errString.contains("ERROR:"));
      Assert.assertTrue(errString.contains(
          "com.android.jack.frontend.test002.jack.PackageName"));
      Assert.assertTrue(errString.contains("collides"));
   }
  }

  /**
   * Test that we do not crash.
   */
  @Test
  @Category(ExtraTests.class)
  public void testConflictingPackage002() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    IToolchain toolchain =  AbstractTestTools.getCandidateToolchain();

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        outDir,
        /* zipFiles= */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.frontend.test003.jack"));
  }

  /**
   * Test that we do not crash and that we report the error.
   */
  @Test
  public void testDuplicated001() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setOutputStream(out);
    toolchain.setErrorStream(err);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          outDir,
          /* zipFiles= */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test016.jack"));
      Assert.fail();
    } catch (FrontendCompilationException e) {
      Assert.assertEquals(0, out.size());
      String errString = err.toString();
      Assert.assertTrue(errString.contains("ERROR:"));
      Assert.assertTrue(errString.contains("Duplicated"));
   }
  }


  /**
   * Test that Jack is neither failing nor dropping warnings while ecj frontend is subject to skip
   * the local classes.
   */
  @Test
  @Category(ExtraTests.class)
  public void testUninstanciableLocalClass001() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    List<Class<? extends IToolchain>> excludeList = new ArrayList<Class<? extends IToolchain>>(1);
    excludeList.add(JillBasedToolchain.class);
    excludeList.add(IncrementalToolchain.class);
    IToolchain toolchain =  AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeList);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setOutputStream(out);
    toolchain.setErrorStream(err);

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        outDir,
        /* zipFiles= */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.frontend.test004.jack"));
    Assert.assertEquals(0, out.size());
    String errString = err.toString();
    int warnIndex = errString.indexOf("WARNING:");
    Assert.assertTrue(warnIndex != -1);
    warnIndex = errString.indexOf("WARNING:", warnIndex + 1);
    Assert.assertTrue(warnIndex != -1);
    Assert.assertTrue(errString.indexOf("WARNING:", warnIndex + 1) == -1);

  }

  /**
   * Test that Jack is neither failing nor dropping the error in this case.
   */
  @Test
  public void testInnerError001() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setOutputStream(out);
    toolchain.setErrorStream(err);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          outDir,
          /* zipFiles= */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test013.jack"));
      Assert.fail();
    } catch (FrontendCompilationException e) {
      Assert.assertEquals(0, out.size());
      String errString = err.toString();
      Assert.assertTrue(errString.contains("ERROR:"));
      Assert.assertTrue(errString.contains("ExtendingInnerOnly"));
      Assert.assertTrue(errString.contains("Inner"));
   }
  }

  /**
   * Test that Jack is neither failing nor dropping the error in this case.
   */
  @Test
  @Category(ExtraTests.class)
  public void testInnerError002() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setOutputStream(out);
    toolchain.setErrorStream(err);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          outDir,
          /* zipFiles= */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test014.jack"));
      Assert.fail();
    } catch (FrontendCompilationException e) {
      Assert.assertEquals(0, out.size());
      String errString = err.toString();
      Assert.assertTrue(errString.contains("ERROR:"));
      Assert.assertTrue(errString.contains("ExtendingInnerInStaticContext"));
      Assert.assertTrue(errString.contains("Inner"));
   }
  }

  /**
   * Test that Jack is neither failing nor dropping the error in this case.
   */
  @Test
  public void testInnerError003() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setOutputStream(out);
    toolchain.setErrorStream(err);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          outDir,
          /* zipFiles= */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test015.jack"));
      Assert.fail();
    } catch (FrontendCompilationException e) {
      Assert.assertEquals(0, out.size());
      String errString = err.toString();
      Assert.assertTrue(errString.contains("ERROR:"));
      Assert.assertTrue(errString.contains("WithOuterContextButStatic"));
      Assert.assertTrue(errString.contains("Inner"));
   }
  }

  /**
   * Test that Jack is neither failing nor dropping the error in this case.
   */
  @Test
  public void testInnerError004() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setOutputStream(out);
    toolchain.setErrorStream(err);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          outDir,
          /* zipFiles= */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test008.jack"));
      Assert.fail();
    } catch (FrontendCompilationException e) {
      Assert.assertEquals(0, out.size());
      String errString = err.toString();
      Assert.assertTrue(errString.contains("ERROR:"));
      Assert.assertTrue(errString.contains("NoOuterContext"));
      Assert.assertTrue(errString.contains("Inner"));
   }
  }

  /**
   * Test that Jack is neither failing nor dropping the error in this case.
   */
  @Test
  public void testUnusedLocalVar001() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setOutputStream(out);
    toolchain.setErrorStream(err);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          outDir,
          /* zipFiles= */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test010.jack"));
      Assert.fail();
    } catch (FrontendCompilationException e) {
      Assert.assertEquals(0, out.size());
      String errString = err.toString();
      Assert.assertTrue(errString.contains("ERROR:"));
      Assert.assertTrue(errString.contains("UnusedLocalVar"));
      Assert.assertTrue(errString.contains("Inner"));
   }
  }

  /**
   * Test that Jack is not failing.
   */
  @Test
  @Category(ExtraTests.class)
  public void testQualifedNew001() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          outDir,
          /* zipFiles= */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test011.jack"));
  }

  /**
   * Test that Jack is not failing.
   */
  @Test
  @Category(ExtraTests.class)
  public void testUnusedLocalVar003() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();

      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          outDir,
          /* zipFiles= */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test012.jack"));
  }

  /**
   * Test that Jack is neither failing nor dropping the error in this case.
   */
  @Test
  @Category(ExtraTests.class)
  public void testUnusedLocalVar004() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setOutputStream(out);
    toolchain.setErrorStream(err);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          outDir,
          /* zipFiles= */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test017.jack"));
      Assert.fail();
    } catch (FrontendCompilationException e) {
      Assert.assertEquals(0, out.size());
      String errString = err.toString();
      Assert.assertTrue(errString.contains("ERROR:"));
      Assert.assertTrue(errString.contains("InvalidQualification"));
   }
  }
}
