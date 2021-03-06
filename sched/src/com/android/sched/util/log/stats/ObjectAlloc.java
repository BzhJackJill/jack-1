/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.sched.util.log.stats;

import com.google.common.collect.Iterators;

import com.android.sched.util.print.DataType;
import com.android.sched.util.print.DataView;
import com.android.sched.util.print.DataViewBuilder;

import java.util.Iterator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Represents a statistic on object allocation when statistic is not enabled.
 */
public class ObjectAlloc extends Statistic {
  protected ObjectAlloc(@Nonnull StatisticId<? extends Statistic> id) {
    super(id);
  }

  /**
   * Record an object allocation.
   *
   * @param size size in bytes of object in memory.
   */
  public void recordAllocation(@Nonnegative long size) {
  }


  public long getNumber() {
    return 0;
  }

  public long getSize() {
    return 0;
  }

  @Override
  public void merge(@Nonnull Statistic statistic) {
    throw new AssertionError();
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "Object allocation";
  }

  @Override
  @Nonnull
  public synchronized Iterator<Object> iterator() {
    return Iterators.<Object> forArray(
        Long.valueOf(getNumber()),
        Long.valueOf(getSize()));
  }

  @Nonnull
  private static final DataView DATA_VIEW = DataViewBuilder.getStructure()
      .addField("objectCount", DataType.NUMBER)
      .addField("objectSize", DataType.QUANTITY)
      .build();

  @Override
  @Nonnull
  public DataView getDataView() {
    return DATA_VIEW;
  }

}
