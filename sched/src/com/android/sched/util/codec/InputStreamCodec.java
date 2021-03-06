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

package com.android.sched.util.codec;

import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.InputStreamFile;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.StandardInputLocation;

import javax.annotation.Nonnull;

/**
 * This {@link InputStreamCodec} is used to create an instance of {@link InputStreamFile}.
 */
public class InputStreamCodec extends FileCodec<InputStreamFile> {
  public InputStreamCodec() {
    super(Existence.MUST_EXIST, Permission.READ);
  }

  @Nonnull
  public InputStreamCodec allowStandardInput() {
    this.allowStandardIO = true;

    return this;
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull InputStreamFile stream) {
    if (stream.isStandard()) {
      return STANDARD_IO_NAME;
    } else {
      return stream.getPath();
    }
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull InputStreamFile stream)
      throws CheckingException {
    if (stream.isStandard() && !allowStandardIO) {
      throw new CheckingException("Standard input is not allowed");
    }
  }

  @Override
  @Nonnull
  public InputStreamFile parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Nonnull
  private static final Location STANDARD_INPUT_LOCATION = new StandardInputLocation();

  @Override
  @Nonnull
  public InputStreamFile checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    if (string.equals(STANDARD_IO_NAME)) {
      if (!allowStandardIO) {
        throw new ParsingException("Standard input can not be used");
      }

      return new InputStreamFile(context.getStandardInput(), STANDARD_INPUT_LOCATION);
    } else {
      try {
        return new InputStreamFile(context.getWorkingDirectory(), string);
      } catch (WrongPermissionException
          | NoSuchFileException | NotFileException e) {
        throw new ParsingException(e.getMessage(), e);
      }
    }
  }
}
