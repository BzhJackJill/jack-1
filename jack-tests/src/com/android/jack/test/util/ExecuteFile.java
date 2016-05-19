/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.test.util;

import com.android.sched.util.findbugs.SuppressFBWarnings;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.stream.ByteStreamSucker;
import com.android.sched.util.stream.CharacterStreamSucker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class to handle the execution of an external process
 */
public class ExecuteFile {
  @Nonnull
  private final String[] cmdLine;

  @Nonnull
  private Map<String, String> env = new HashMap<String, String>();

  @CheckForNull
  private File workDir;

  @CheckForNull
  private InputStream inStream;

  @CheckForNull
  private OutputStream outStream;

  @CheckForNull
  private OutputStream errStream;
  private boolean verbose;

  @Nonnull
  private final Logger logger;

  public void setErr(@Nonnull OutputStream stream) {
    errStream = stream;
  }

  public void setOut(@Nonnull OutputStream stream) {
    outStream = stream;
  }

  public void setIn(@Nonnull InputStream stream) {
    inStream = stream;
  }

  public void setWorkingDir(@Nonnull File dir, boolean create) throws IOException {
    if (!dir.isDirectory()) {
      if (create && !dir.exists()) {
        if (!dir.mkdirs()) {
          throw new IOException("Directory creation failed");
        }
      } else {
        throw new FileNotFoundException(dir.getPath() + " is not a directory");
      }
    }

    workDir = dir;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public void addEnvVar(@Nonnull String key, @Nonnull String value) {
    env.put(key, value);
  }

  public ExecuteFile(@Nonnull File exec, @Nonnull String[] args) {
    cmdLine = new String[args.length + 1];
    System.arraycopy(args, 0, cmdLine, 1, args.length);

    cmdLine[0] = exec.getAbsolutePath();
    logger = LoggerFactory.getLogger();
  }

  public ExecuteFile(@Nonnull String exec, @Nonnull String[] args) {
    cmdLine = new String[args.length + 1];
    System.arraycopy(args, 0, cmdLine, 1, args.length);

    cmdLine[0] = exec;
    logger = LoggerFactory.getLogger();
  }

  public ExecuteFile(@Nonnull File exec) {
    cmdLine = new String[1];
    cmdLine[0] = exec.getAbsolutePath();
    logger = LoggerFactory.getLogger();
  }

  public ExecuteFile(@Nonnull String[] cmdLine) {
    this.cmdLine = cmdLine.clone();
    logger = LoggerFactory.getLogger();
  }

  public ExecuteFile(@Nonnull String cmdLine) throws IOException {
    StringReader reader = new StringReader(cmdLine);
    StreamTokenizer tokenizer = new StreamTokenizer(reader);
    tokenizer.resetSyntax();
    // Only standard spaces are recognized as whitespace chars
    tokenizer.whitespaceChars(' ', ' ');
    // Matches alphanumerical and common special symbols like '(' and ')'
    tokenizer.wordChars('!', 'z');
    // Quote chars will be ignored when parsing strings
    tokenizer.quoteChar('\'');
    tokenizer.quoteChar('\"');
    ArrayList<String> tokens = new ArrayList<String>();
    while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
      String token = tokenizer.sval;
      if (token != null) {
        tokens.add(token);
      }
    }
    this.cmdLine = tokens.toArray(new String[0]);
    logger = LoggerFactory.getLogger();
  }

  // Intended behavior, wrap any exception thrown in try block
  @SuppressFBWarnings("REC_CATCH_EXCEPTION")
  public int run() throws ExecFileException {
    int ret;
    Process proc = null;
    Thread suckOut = null;
    Thread suckErr = null;
    Thread suckIn = null;

    try {

      String[] cmdLineEnv = new String[env.size()];
      int idx = 0;
      for (Entry<String, String> envElt : env.entrySet()) {
        cmdLineEnv[idx++] = envElt.getKey() + '=' + envElt.getValue();
      }

      StringBuilder cmdLineBuilder = new StringBuilder();
      for (String envElt : cmdLineEnv) {
        cmdLineBuilder.append(envElt).append(' ');
      }
      for (String arg : cmdLine) {
        cmdLineBuilder.append(arg).append(' ');
      }
      if (verbose) {
        PrintStream printStream;
        if (outStream instanceof PrintStream) {
          printStream = (PrintStream) outStream;
        } else {
          printStream = System.out;
        }

        if (printStream != null) {
          printStream.println(cmdLineBuilder);
        }
      } else {
        logger.log(Level.INFO, "Execute: {0}", cmdLineBuilder);
      }

      proc = Runtime.getRuntime().exec(cmdLine, cmdLineEnv, workDir);

      InputStream localInStream = inStream;
      if (localInStream != null) {
        suckIn = new Thread(
            new ThreadBytesStreamSucker(localInStream, proc.getOutputStream()));
      } else {
        proc.getOutputStream().close();
      }

      OutputStream localOutStream = outStream;
      if (localOutStream != null) {
        if (localOutStream instanceof PrintStream) {
          suckOut = new Thread(new ThreadCharactersStreamSucker(proc.getInputStream(),
              (PrintStream) localOutStream));
        } else {
          suckOut = new Thread(
              new ThreadBytesStreamSucker(proc.getInputStream(), localOutStream));
        }
      }

      OutputStream localErrStream = errStream;
      if (localErrStream != null) {
        if (localErrStream instanceof PrintStream) {
          suckErr = new Thread(new ThreadCharactersStreamSucker(proc.getErrorStream(),
              (PrintStream) localErrStream));
        } else {
          suckErr = new Thread(
              new ThreadBytesStreamSucker(proc.getErrorStream(), localErrStream));
        }
      }

      if (suckIn != null) {
        suckIn.start();
      }
      if (suckOut != null) {
        suckOut.start();
      }
      if (suckErr != null) {
        suckErr.start();
      }

      proc.waitFor();
      if (suckIn != null) {
        suckIn.join();
      }
      if (suckOut != null) {
        suckOut.join();
      }
      if (suckErr != null) {
        suckErr.join();
      }

      ret = proc.exitValue();
      proc.destroy();

      return ret;
    } catch (Exception e) {
      throw new ExecFileException(cmdLine, e);
    }
  }

  private static class ThreadBytesStreamSucker extends ByteStreamSucker implements Runnable {

    public ThreadBytesStreamSucker(@Nonnull InputStream is, @Nonnull OutputStream os) {
      super(is, os);
    }

    @Override
    public void run() {
      try {
        suck();
      } catch (IOException e) {
        // Best effort
      }
    }
  }

  private static class ThreadCharactersStreamSucker extends CharacterStreamSucker implements
      Runnable {

    public ThreadCharactersStreamSucker(@Nonnull InputStream is, @Nonnull PrintStream ps) {
      super(is, ps);
    }

    @Override
    public void run() {
      try {
        suck();
      } catch (IOException e) {
        // Best effort
      }
    }
  }

  @Nonnull
  public ExecuteFile inheritEnvironment() {
    for (Entry<String, String> envVarEntry : System.getenv().entrySet()) {
      addEnvVar(envVarEntry.getKey(), envVarEntry.getValue());
    }
    return this;
  }

}