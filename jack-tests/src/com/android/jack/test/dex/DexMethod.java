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

package com.android.jack.test.dex;

import com.google.common.collect.ImmutableList;

import com.android.dx.rop.code.AccessFlags;

import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.Code.Instruction;

import javax.annotation.Nonnull;

/** Represents DEX method */
public class DexMethod {
  @Nonnull
  private final ClassDataItem.EncodedMethod item;

  public DexMethod(@Nonnull ClassDataItem.EncodedMethod item) {
    this.item = item;
  }

  public boolean isFinal() {
    return ((item.accessFlags & AccessFlags.ACC_FINAL) != 0);
  }

  @Nonnull
  public String getId() {
    return item.method.getShortMethodString();
  }

  @Nonnull
  String getSource() {
    return DexCodeItemPrinter.print(item.method, item.codeItem);
  }

  @Nonnull
  public ImmutableList<Instruction> getInstructions() {
    return ImmutableList.copyOf(item.codeItem.getInstructions());
  }
}
