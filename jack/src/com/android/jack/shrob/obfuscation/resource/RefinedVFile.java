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

package com.android.jack.shrob.obfuscation.resource;

import com.android.sched.util.location.Location;
import com.android.sched.vfs.AbstractVElement;
import com.android.sched.vfs.InputVFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A {@code VFile} wrapping another {@code VFile} with refining data.
 */
public class RefinedVFile extends AbstractVElement implements InputVFile {

  @Nonnull
  private final InputVFile file;

  @Nonnull
  private final TreeSet<RefinedEntry> refinedEntries =
      new TreeSet<RefinedVFile.RefinedEntry>(new Comparator<RefinedEntry>() {

        @Override
        public int compare(@Nonnull RefinedEntry e1, @Nonnull RefinedEntry e2) {
          return e1.startPosition > e2.startPosition ? 1
              : (e1.endPosition < e2.startPosition ? -1 : 0);
        }

      });

  public RefinedVFile(@Nonnull InputVFile file) {
    this.file = file;
  }

  public void addRefinedEntry(@Nonnegative int startPosition, @Nonnegative int endPosition,
      @Nonnull CharSequence content) {
    refinedEntries.add(new RefinedEntry(startPosition, endPosition, content));
  }

  @Nonnull
  @Override
  public InputStream openRead() throws IOException {
    InputStream inputStream = file.openRead();
    if (refinedEntries.isEmpty()) {
      return inputStream;
    }
    return new RefinedInputStream(inputStream);
  }

  @Nonnull
  @Override
  public String getName() {
    return file.getName();
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return file.getLocation();
  }

  private static class RefinedEntry {
    @Nonnegative
    private final int startPosition;

    @Nonnegative
    private final int endPosition;

    @Nonnull
    private final CharSequence content;

    private RefinedEntry(@Nonnegative int startPosition, @Nonnegative int endPosition,
        @Nonnull CharSequence content) {
      this.startPosition = startPosition;
      this.endPosition = endPosition;
      this.content = content;
    }

    @Nonnull
    public InputStream openRead() {
      return new ByteArrayInputStream(content.toString().getBytes());
    }

    @Override
    public String toString() {
      return startPosition + ":" + endPosition;
    }
  }

  private class RefinedInputStream extends InputStream {

    /**
     * The unrefined inputstream
     */
    @Nonnull
    private final InputStream baseInputStream;

    /**
     * The position in the baseInputStream
     */
    private int position = -1;

    /**
     * The current refined part of the stream
     */
    @CheckForNull
    private InputStream currentStream;

    @CheckForNull
    private RefinedEntry currentRefinedEntry;

    /**
     * The set of refined entries. Entries already read are removed.
     */
    @Nonnull
    private final TreeSet<RefinedEntry> currentRefinedEntries;

    @SuppressWarnings("unchecked")
    public RefinedInputStream(@Nonnull InputStream baseInputStream) {
      this.baseInputStream = baseInputStream;
      currentRefinedEntries = (TreeSet<RefinedEntry>) RefinedVFile.this.refinedEntries.clone();
    }

    @Override
    public int read() throws IOException {
      int inputData;
      if (currentStream != null) {
        // We are reading a refined entry
        inputData = currentStream.read();
        if (inputData == -1) {
          // The entry has already been completely read
          assert currentRefinedEntry != null;
          // Set the base inputstream position to the refined entry end position
          int toSkip = currentRefinedEntry.endPosition - position;
          while (toSkip > 0) {
            assert baseInputStream.available() != 0;
            toSkip -= baseInputStream.skip(toSkip);
          }
          position = currentRefinedEntry.endPosition;
          currentRefinedEntries.remove(currentRefinedEntry);
          currentRefinedEntry = null;
          currentStream = null;
        } else {
          return inputData;
        }
      }
      position++;
      inputData = baseInputStream.read();
      for (RefinedEntry entry : refinedEntries) {
        if (entry.startPosition <= position) {
          if (entry.endPosition >= position) {
            // A refined entry should be read
            currentRefinedEntry = entry;
            currentStream = entry.openRead();
            return currentStream.read();
          }
        } else {
          break;
        }
      }
      // No refined entry was found, read the base stream normally.
      return inputData;
    }

    @Override
    public int available() throws IOException {
      int available = 0;
      if (currentStream != null) {
        available += currentStream.available();
      }
      if (baseInputStream.available() > 0) {
        available += 1;
      }
      return available;
    }

    @Override
    public void close() throws IOException {
      super.close();
      baseInputStream.close();
    }
  }
}
