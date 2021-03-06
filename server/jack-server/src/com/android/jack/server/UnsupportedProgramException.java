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

package com.android.jack.server;

import javax.annotation.Nonnull;

/**
 * Thrown when trying to install a jar that can not be handled by the server.
 */
public class UnsupportedProgramException extends Exception {

  private static final long serialVersionUID = 1L;

  @Nonnull
  private final String expectedProgram;

  public UnsupportedProgramException(@Nonnull String expectedProgram) {
    this.expectedProgram = expectedProgram;
  }

  @Override
  public String getMessage() {
    return "Jar is not a supported " + expectedProgram + " archive";
  }
}
