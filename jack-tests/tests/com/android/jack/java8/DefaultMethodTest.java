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

package com.android.jack.java8;

import com.android.jack.JackAbortException;
import com.android.jack.Options;
import com.android.jack.backend.dex.compatibility.AndroidCompatibilityChecker;
import com.android.jack.test.helper.FileChecker;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.junit.RuntimeVersion;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.IncrementalToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackApiV01;
import com.android.jack.test.toolchain.JackApiV02;
import com.android.jack.test.toolchain.JackApiV03;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;
import com.android.jack.util.AndroidApiLevel;

import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.DexFile;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;


/**
 * JUnit test for compilation of default method.
 */
public class DefaultMethodTest {

  private RuntimeTestInfo DEFAULTMETHOD001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.defaultmethod.test001"),
      "com.android.jack.java8.defaultmethod.test001.jack.Tests");

  private RuntimeTestInfo DEFAULTMETHOD002 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.defaultmethod.test002"),
      "com.android.jack.java8.defaultmethod.test002.jack.Tests");

  private RuntimeTestInfo DEFAULTMETHOD003 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.defaultmethod.test003"),
      "com.android.jack.java8.defaultmethod.test003.jack.Tests");

  private RuntimeTestInfo DEFAULTMETHOD004 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.defaultmethod.test004"),
      "com.android.jack.java8.defaultmethod.test004.jack.Tests");

  private RuntimeTestInfo DEFAULTMETHOD005 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.defaultmethod.test005"),
      "com.android.jack.java8.defaultmethod.test005.jack.Tests");

  private RuntimeTestInfo DEFAULTMETHOD006 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.defaultmethod.test006"),
      "com.android.jack.java8.defaultmethod.test006.jack.Tests");

  private RuntimeTestInfo DEFAULTMETHOD007 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.defaultmethod.test007"),
      "com.android.jack.java8.defaultmethod.test007.jack.Tests");

  private RuntimeTestInfo DEFAULTMETHOD008 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.defaultmethod.test008"),
          "com.android.jack.java8.defaultmethod.test008.jack.Tests")
          .addProguardFlagsFileName("proguard.flags");

  private RuntimeTestInfo DEFAULTMETHOD009 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.defaultmethod.test009"),
      "com.android.jack.java8.defaultmethod.test009.jack.Tests")
          .addProguardFlagsFileName("proguard.flags").addFileChecker(new FileChecker() {
            @Override
            public void check(@Nonnull File file) throws Exception {
              DexFile dexFile = new DexFile(file);
              Set<String> sourceFileInDex = new HashSet<String>();
              boolean isIKept = false;
              for (ClassDefItem classDef : dexFile.ClassDefsSection.getItems()) {
                if (classDef.toString().equals(
                    "class_def_item: Lcom/android/jack/java8/defaultmethod/test009/jack/I;")) {
                  isIKept = true;
                  List<EncodedMethod> methods = classDef.getClassData().getVirtualMethods();
                  Assert.assertEquals(methods.size(), 1);
                  Assert.assertEquals(methods.get(0).method.getMethodName().getStringValue(),
                      "add");
                }
              }
              Assert.assertTrue(isIKept);
            }
          });

  private RuntimeTestInfo DEFAULTMETHOD010 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.defaultmethod.test010"),
      "com.android.jack.java8.defaultmethod.test010.jack.Tests")
          .addProguardFlagsFileName("proguard.flags").addFileChecker(new FileChecker() {
            @Override
            public void check(@Nonnull File file) throws Exception {
              DexFile dexFile = new DexFile(file);
              Set<String> sourceFileInDex = new HashSet<String>();
              boolean isIRenamed = false;
              for (ClassDefItem classDef : dexFile.ClassDefsSection.getItems()) {
                System.out.println(classDef.toString());
                if (classDef.toString().equals(
                    "class_def_item: Lcom/android/jack/java8/defaultmethod/test010/jack/Z;")) {
                  isIRenamed = true;
                  List<EncodedMethod> methods = classDef.getClassData().getVirtualMethods();
                  Assert.assertEquals(methods.size(), 1);
                  Assert.assertEquals(methods.get(0).method.getMethodName().getStringValue(),
                      "renamedAdd");
                }
              }
              Assert.assertTrue(isIRenamed);
            }
          });

  private RuntimeTestInfo DEFAULTMETHOD011 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.defaultmethod.test011"),
          "com.android.jack.java8.defaultmethod.test011.jack.Tests");

  private RuntimeTestInfo DEFAULTMETHOD012 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.defaultmethod.test012"),
          "com.android.jack.java8.defaultmethod.test012.jack.Tests");

  private RuntimeTestInfo DEFAULTMETHOD013 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.defaultmethod.test013"),
          "com.android.jack.java8.defaultmethod.test013.jack.Tests");

  private RuntimeTestInfo DEFAULTMETHOD014 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.defaultmethod.test014"),
          "com.android.jack.java8.defaultmethod.test014.jack.Tests");

  private RuntimeTestInfo DEFAULTMETHOD015 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.defaultmethod.test015"),
          "com.android.jack.java8.defaultmethod.test015.jack.Tests");

  private RuntimeTestInfo DEFAULTMETHOD016 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.defaultmethod.test016"),
          "com.android.jack.java8.defaultmethod.test016.jack.Tests");

  private RuntimeTestInfo DEFAULTMETHOD017 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.defaultmethod.test017"),
          "com.android.jack.java8.defaultmethod.test017.jack.Tests");

  private RuntimeTestInfo DEFAULTMETHOD018 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.defaultmethod.test018"),
          "com.android.jack.java8.defaultmethod.test018.jack.Tests");

  private RuntimeTestInfo DEFAULTMETHOD019 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.defaultmethod.test019"),
          "com.android.jack.java8.defaultmethod.test019.jack.Tests");

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultMethod001() throws Exception {
    run(DEFAULTMETHOD001);
  }

  /**
   * Ensure that we refuse to import a default method library in an api 23 dex.
   */
  @Test
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void testDefaultMethod001_1() throws Exception {
    List<Class<? extends IToolchain>> excludeClazz = new ArrayList<Class<? extends IToolchain>>(1);
    excludeClazz.add(JackApiV01.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class, excludeClazz);

    File lib24 =
        AbstractTestTools.createTempFile("lib24", toolchain.getLibraryExtension());
    toolchain.addProperty(
        Options.ANDROID_MIN_API_LEVEL.getName(),
        String.valueOf(AndroidApiLevel.ReleasedLevel.N.getLevel()))
    .setSourceLevel(SourceLevel.JAVA_8)
    .addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(lib24,
        /* zipFiles = */ true, new File(DEFAULTMETHOD001.directory, DEFAULTMETHOD001.srcDirName));

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    File dex23 = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class, excludeClazz);
    toolchain.addProperty(
        Options.ANDROID_MIN_API_LEVEL.getName(),
        String.valueOf(23))
    .setErrorStream(errOut);
    try {
      toolchain.libToExe(lib24, dex23, /* zipFiles = */ false);
      Assert.fail();
    } catch (JackAbortException e) {
      Assert.assertTrue(
          errOut.toString().contains("not supported in Android API level less than 24"));
    }
  }

  /**
   * Ensure that we CANNOT compile a library WITH predexing in min api 23 because it contains
   * default methods (because they are only allowed starting from min api 24).
   */
  @Test
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void testDefaultMethod001_2_WithPredexing() throws Exception {
    runTestDefaultMethod001_2(true);
  }

  /**
   * Ensure that we can compile a library WITHOUT predexing in min api 23 even if it contains
   * default methods.
   * Then ensure that we refuse to import this library into a dex compiled in min api 23
   * (because default methods are only allowed starting from min api 24).
   */
  @Test
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void testDefaultMethod001_2_WithoutPredexing() throws Exception {
    runTestDefaultMethod001_2(false);
  }

  private void runTestDefaultMethod001_2(boolean enablePredexing) throws Exception {
    List<Class<? extends IToolchain>> excludeClazz = new ArrayList<Class<? extends IToolchain>>(1);
    excludeClazz.add(JackApiV01.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class, excludeClazz);
    boolean pre04 = (toolchain instanceof JackApiV01 ||
                     toolchain instanceof JackApiV02 ||
                     toolchain instanceof JackApiV03);
    File lib23 =
        AbstractTestTools.createTempFile("lib23", toolchain.getLibraryExtension());
    toolchain.addProperty(
        Options.ANDROID_MIN_API_LEVEL.getName(),
        String.valueOf(23))
    .addProperty(Options.GENERATE_DEX_IN_LIBRARY.getName(), Boolean.toString(enablePredexing))
    .setSourceLevel(SourceLevel.JAVA_8)
    .addToClasspath(toolchain.getDefaultBootClasspath());

    if (enablePredexing) {
      // When enabling predexing, we should have run the checker and fail due to the presence
      // of default methods.
      ByteArrayOutputStream errOut = new ByteArrayOutputStream();
      toolchain.setErrorStream(errOut);
      try {
        toolchain.srcToLib(lib23, /* zipFiles = */ true,
            new File(DEFAULTMETHOD001.directory, DEFAULTMETHOD001.srcDirName));
        if (!pre04) {
          Assert.fail();
        }
      } catch (JackAbortException e) {
        if (!pre04) {
          Assert.assertTrue(
              errOut.toString().contains("not supported in Android API level less than 24"));
        } else {
          Assert.fail();
        }
      }
    } else {
      // We do not expect any error when producing the library ...
      toolchain.srcToLib(lib23, /* zipFiles = */ true,
          new File(DEFAULTMETHOD001.directory, DEFAULTMETHOD001.srcDirName));

      // ... but we do expect one when using the library.
      ByteArrayOutputStream errOut = new ByteArrayOutputStream();
      File dex23 = AbstractTestTools.createTempDir();
      toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class, excludeClazz);
      toolchain.addProperty(
          Options.ANDROID_MIN_API_LEVEL.getName(),
          String.valueOf(23))
      .setErrorStream(errOut);
      try {
        toolchain.libToExe(lib23, dex23, /* zipFiles = */ false);
        Assert.fail();
      } catch (JackAbortException e) {
        Assert.assertTrue(
            errOut.toString().contains("not supported in Android API level less than 24"));
      }
    }
  }

  /**
   * Ensure that we CANNOT compile a predexed lib including a default method with min api 23.
   */
  @Test
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void testDefaultMethod001_3_WithPredexing() throws Exception {
    runTestDefaultMethod001_3(true);
  }

  /**
   * Ensure that we can compile a non-predexed lib including a default method with min api 23 and
   * then import it into a dex with min api 24.
   */
  @Test
  @Runtime(from=RuntimeVersion.N)
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void testDefaultMethod001_3_WithoutPredexing() throws Exception {
    runTestDefaultMethod001_3(false);
  }

  private void runTestDefaultMethod001_3(boolean enablePredexing) throws Exception {
    List<Class<? extends IToolchain>> excludeClazz = new ArrayList<Class<? extends IToolchain>>(2);
    excludeClazz.add(JackApiV01.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class, excludeClazz);
    File lib23 =
        AbstractTestTools.createTempFile("lib23", toolchain.getLibraryExtension());
    toolchain.addProperty(
        Options.ANDROID_MIN_API_LEVEL.getName(),
        String.valueOf(23))
    .addProperty(Options.GENERATE_DEX_IN_LIBRARY.getName(), Boolean.toString(enablePredexing))
    .setSourceLevel(SourceLevel.JAVA_8)
    .addToClasspath(toolchain.getDefaultBootClasspath());

    if (enablePredexing) {
      boolean pre04 = (toolchain instanceof JackApiV01 ||
                       toolchain instanceof JackApiV02 ||
                       toolchain instanceof JackApiV03);

      ByteArrayOutputStream errOut = new ByteArrayOutputStream();
      toolchain.setErrorStream(errOut);
      try {
        toolchain.srcToLib(lib23, /* zipFiles = */ true,
            new File(DEFAULTMETHOD001.directory, DEFAULTMETHOD001.srcDirName));
        if (!pre04) {
          Assert.fail();
        }
      } catch (JackAbortException e) {
        if (!pre04) {
          Assert.assertTrue(
              errOut.toString().contains("not supported in Android API level less than 24"));
        } else {
          Assert.fail();
        }
      }
    } else {
      toolchain.srcToLib(lib23,
          /* zipFiles = */ true, new File(DEFAULTMETHOD001.directory, DEFAULTMETHOD001.srcDirName));

      File dex24 = AbstractTestTools.createTempDir();
      toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class, excludeClazz);
      toolchain.addProperty(
          Options.ANDROID_MIN_API_LEVEL.getName(),
          String.valueOf(AndroidApiLevel.ReleasedLevel.N.getLevel()))
      .libToExe(lib23, dex24, /* zipFiles = */ false);

      // Run to check everything went as expected
      RuntimeTestHelper.runOnRuntimeEnvironments(
          Collections.singletonList(DEFAULTMETHOD001.jUnit),
          RuntimeTestHelper.getJunitDex(), new File(dex24, "classes.dex"));
    }

  }

  /**
   * Ensure that can compile a lib including a default method with min api 24 and then import it to
   * a dex with min api 24.
   */
  @Test
  @Runtime(from=RuntimeVersion.N)
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void testDefaultMethod001_4() throws Exception {
    List<Class<? extends IToolchain>> excludeClazz = new ArrayList<Class<? extends IToolchain>>(2);
    excludeClazz.add(JackApiV01.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeClazz);
    File lib24 =
        AbstractTestTools.createTempFile("lib24", toolchain.getLibraryExtension());
    toolchain.addProperty(
        Options.ANDROID_MIN_API_LEVEL.getName(),
        String.valueOf(AndroidApiLevel.ReleasedLevel.N.getLevel()))
    .setSourceLevel(SourceLevel.JAVA_8)
    .addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(lib24,
        /* zipFiles = */ true, new File(DEFAULTMETHOD001.directory, DEFAULTMETHOD001.srcDirName));

    File dex24 = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeClazz);
    toolchain.addProperty(
        Options.ANDROID_MIN_API_LEVEL.getName(),
        String.valueOf(AndroidApiLevel.ReleasedLevel.N.getLevel()))
    .libToExe(lib24, dex24, /* zipFiles = */ false);

    // Run to check everything went as expected
    RuntimeTestHelper.runOnRuntimeEnvironments(
        Collections.singletonList(DEFAULTMETHOD001.jUnit),
        RuntimeTestHelper.getJunitDex(), new File(dex24, "classes.dex"));
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultMethod002() throws Exception {
    run(DEFAULTMETHOD002);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultMethod003() throws Exception {
    run(DEFAULTMETHOD003);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultMethod004() throws Exception {
    run(DEFAULTMETHOD004);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultMethod005() throws Exception {
    run(DEFAULTMETHOD005);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  @KnownIssue
  public void testDefaultMethod006() throws Exception {
    run(DEFAULTMETHOD006);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultMethod007() throws Exception {
    run(DEFAULTMETHOD007);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultMethod008() throws Exception {
    run(DEFAULTMETHOD008);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultMethod009() throws Exception {
    run(DEFAULTMETHOD009);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultMethod010() throws Exception {
    run(DEFAULTMETHOD010);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultMethod011() throws Exception {
    run(DEFAULTMETHOD011);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultMethod012() throws Exception {
    run(DEFAULTMETHOD012);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultMethod013() throws Exception {
    run(DEFAULTMETHOD013);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultMethod014() throws Exception {
    run(DEFAULTMETHOD014);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultMethod015() throws Exception {
    run(DEFAULTMETHOD015);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultMethod016() throws Exception {
    run(DEFAULTMETHOD016);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultMethod017() throws Exception {
    run(DEFAULTMETHOD017);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultMethod018() throws Exception {
    new RuntimeTestHelper(DEFAULTMETHOD018)
    .addProperty(
        Options.ANDROID_MIN_API_LEVEL.getName(),
        String.valueOf(AndroidApiLevel.ReleasedLevel.N.getLevel()))
    .setSourceLevel(SourceLevel.JAVA_8)
    .addIgnoredCandidateToolchain(JackApiV01.class)
    // This test must be exclude from the Jill tool-chain because, there is a different behavior than with Jack
    .addIgnoredCandidateToolchain(JillBasedToolchain.class)
    .compileAndRunTest();
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultMethod019() throws Exception {
    run(DEFAULTMETHOD019);
  }

  private void run(@Nonnull RuntimeTestInfo rti) throws Exception {
    new RuntimeTestHelper(rti)
        .addProperty(
            Options.ANDROID_MIN_API_LEVEL.getName(),
            String.valueOf(AndroidApiLevel.ReleasedLevel.N.getLevel()))
        .setSourceLevel(SourceLevel.JAVA_8)
        .addIgnoredCandidateToolchain(JackApiV01.class)
        .compileAndRunTest();
  }
}
