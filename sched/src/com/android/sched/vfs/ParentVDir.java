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

package com.android.sched.vfs;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@link BaseVDir} implementation based on a {@link VDir} parent.
 */
public class ParentVDir extends BaseVDir {
  @CheckForNull
  protected final VDir parent;

  ParentVDir(@Nonnull BaseVFS<? extends BaseVDir, ? extends BaseVFile> vfs, @Nonnull String name) {
    super(vfs, name);
    this.parent = null;
  }

  ParentVDir(@Nonnull BaseVFS<? extends BaseVDir, ? extends BaseVFile> vfs, @Nonnull VDir parent,
      @Nonnull String name) {
    super(vfs, name);
    this.parent = parent;
  }

  @Override
  @Nonnull
  public VPath getPath() {
    if (parent != null) {
      return parent.getPath().clone().appendPath(new VPath(name, '/'));
    } else {
      return VPath.ROOT;
    }
  }

  @CheckForNull
  VDir getParent() {
    return parent;
  }
}