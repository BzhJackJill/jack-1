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

package com.android.jack.backend.dex;

import com.android.jack.JackAbortException;
import com.android.jack.Options;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JSession;
import com.android.jack.reporting.Reporter.Severity;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.vfs.Container;
import com.android.sched.vfs.OutputVFS;

import javax.annotation.Nonnull;

/**
 * Finalize dex and write it to a file.
 */
@Description("Finalize dex and write it to a file.")
@Name("DexFileWriterFinalizer")
@Produce(DexFileProduct.class)
@Constraint(no = {DexFileWriterSeparator.SeparatorTag.class})
public class DexFileWriterFinalizer implements RunnableSchedulable<JSession> {

  @Nonnull
  private final OutputVFS outputVDir;

  {
    assert ThreadConfig.get(Options.GENERATE_DEX_FILE).booleanValue();
    Container container = ThreadConfig.get(Options.DEX_OUTPUT_CONTAINER_TYPE);
    if (container == Container.DIR) {
      outputVDir = ThreadConfig.get(Options.DEX_OUTPUT_DIR);
    } else {
      outputVDir = ThreadConfig.get(Options.DEX_OUTPUT_ZIP);
    }
  }

  @Override
  public void run(@Nonnull JSession session) throws JackAbortException {
    DexWritingTool writingTool = ThreadConfig.get(DexFileWriter.DEX_WRITING_POLICY);
    try {
      writingTool.finishMerge(outputVDir);
    } catch (DexWritingException e) {
      session.getReporter().report(Severity.FATAL, e);
      throw new JackAbortException(e);
    }

    if (ThreadConfig.get(Options.DETERMINISTIC_MULTIDEX_MODE).booleanValue()) {
      for (JDefinedClassOrInterface type : session.getTypesToEmit()) {
        type.removeMarker(NumberMarker.class);
      }
    }
  }

}
