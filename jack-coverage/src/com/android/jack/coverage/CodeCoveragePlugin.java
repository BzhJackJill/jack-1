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

package com.android.jack.coverage;

import com.android.jack.cfg.CfgBuilder;
import com.android.jack.cfg.CfgMarkerRemover;
import com.android.jack.plugin.v01.SchedAnnotationProcessorBasedPlugin;
import com.android.sched.item.Component;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.scheduler.FeatureSet;
import com.android.sched.scheduler.ProductionSet;
import com.android.sched.scheduler.Scheduler;
import com.android.sched.util.UncomparableVersion;
import com.android.sched.util.Version;
import com.android.sched.util.config.Config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Code coverage plugin for Jack.
 */
public class CodeCoveragePlugin extends SchedAnnotationProcessorBasedPlugin {  /**
   * The name of the plugin. It must NOT be changed to remain compatible with tools using it.
   */
  public static final String PLUGIN_NAME = "com.android.jack.coverage.CodeCoverage";

  /**
   * The friendly name of the plugin.
   */
  private static final String PLUGIN_FRIENDLY_NAME = "CodeCoverage";

  /**
   * A brief description of the plugin.
   */
  private static final String PLUGIN_DESCRIPTION = "Code coverage support 'a la' JaCoCo";

  @Override
  @Nonnull
  public String getCanonicalName() {
    return PLUGIN_NAME;
  }

  @Override
  @Nonnull
  public String getFriendlyName() {
    return PLUGIN_FRIENDLY_NAME;
  }

  @Override
  @Nonnull
  public String getDescription() {
    return PLUGIN_DESCRIPTION;
  }

  @Override
  @Nonnull
  public Version getVersion() {
    try {
      return new Version("jack-coverage-plugin", CodeCoveragePlugin.class.getClassLoader());
    } catch (IOException e) {
      throw new AssertionError("Failed to find the version of the coverage plugin", e);
    }
  }

  @Override
  @Nonnull
  public FeatureSet getFeatures(@Nonnull Config config, @Nonnull Scheduler scheduler) {
    FeatureSet set = scheduler.createFeatureSet();

    if (config.get(CodeCoverageFeature.CODE_COVERAGE).booleanValue()) {
      set.add(CodeCoverageFeature.class);
    }

    return set;
  }

  @Override
  @Nonnull
  public ProductionSet getProductions(@Nonnull Config config, @Nonnull Scheduler scheduler) {
    ProductionSet set = scheduler.createProductionSet();

    if (config.get(CodeCoverageFeature.CODE_COVERAGE).booleanValue()) {
      set.add(CodeCoverageMetadataFile.class);
    }

    return set;
  }

  // Plugin is compatible starting from 1.3-b4.
  private static final int JACK_COMPATIBLE_VERSION_RELEASE_CODE = 4;
  private static final int JACK_COMPATIBLE_VERSION_SUBRELEASE_CODE = 9;

  @Override
  public boolean isCompatibileWithJack(@Nonnull Version jackVersion) {
    try {
      // Avoid binary incompatibilities.
      return jackVersion.isNewerOrEqualThan(
          JACK_COMPATIBLE_VERSION_RELEASE_CODE,
          JACK_COMPATIBLE_VERSION_SUBRELEASE_CODE);
    } catch (UncomparableVersion e) {
      return true;
    }
  }

  @Override
  @Nonnull
  public List<Class<? extends RunnableSchedulable<? extends Component>>> getSortedRunners() {
    return Arrays.<Class<? extends RunnableSchedulable<? extends Component>>> asList(
        CodeCoverageSelector.class,
        CodeCoverageAnalyzer.class,
        CodeCoverageTransformer.class,
        CodeCoverageMetadataFileWriter.class,
        CfgMarkerRemover.class,
        CfgBuilder.class);
  }

  @Override
  @Nonnull
  public Collection<Class<? extends RunnableSchedulable<? extends Component>>> getCheckerRunners() {
    return Collections.emptyList();
  }
}
