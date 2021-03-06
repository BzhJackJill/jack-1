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

package com.android.jack.preprocessor;

import com.android.jack.Jack;
import com.android.jack.ir.ast.Annotable;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JSession;
import com.android.jack.library.FileType;
import com.android.jack.library.InputLibrary;
import com.android.jack.reporting.ReportableException;
import com.android.jack.reporting.Reporter.Severity;
import com.android.sched.item.Description;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.ReaderFile;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.HasLocation;
import com.android.sched.util.location.Location;
import com.android.sched.vfs.InputVFile;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * This {@link RunnableSchedulable} applies the rules defined in the PreProcessor file.
 */
@Description("Apply the rules defined in the PreProcessor file.")
@Support(PreProcessor.class)
public class PreProcessorApplier implements RunnableSchedulable<JSession> {

  @Override
  public void run(@Nonnull JSession session) {

    Collection<Rule> rules = new ArrayList<Rule>();

    if (ThreadConfig.get(PreProcessor.ENABLE).booleanValue()) {
      ReaderFile inputFile = ThreadConfig.get(PreProcessor.FILE);
      try {
        try (Reader inputStream = inputFile.getBufferedReader()) {
          rules.addAll(parseRules(session, inputStream, inputFile.getLocation()));
        } catch (IOException e) {
          throw new CannotCloseException(inputFile, e);
        }
      } catch (CannotReadException | CannotCloseException | RecognitionException e) {
        JppParsingException reportable = new JppParsingException(inputFile, e);
        Jack.getSession().getReporter().report(Severity.FATAL, reportable);
        Jack.getSession().abortEventually();
      }
    }

    for (Iterator<InputLibrary> iter = session.getPathSources(); iter.hasNext();) {
      InputLibrary inputLibrary = iter.next();
      Iterator<InputVFile> metaFileIt = inputLibrary.iterator(FileType.META);
      while (metaFileIt.hasNext()) {
        InputVFile inputFile = metaFileIt.next();
        if (inputFile.getName().endsWith(PreProcessor.PREPROCESSOR_FILE_EXTENSION)) {
          try {
            try (InputStream inputStream = inputFile.getInputStream()) {
              rules.addAll(
                  parseRules(session, new InputStreamReader(inputStream), inputFile.getLocation()));
            } catch (IOException e) {
              throw new CannotCloseException(inputFile, e);
            }
          } catch (CannotReadException | WrongPermissionException | CannotCloseException
              | RecognitionException e) {
            JppParsingException reportable = new JppParsingException(inputFile, e);
            Jack.getSession().getReporter().report(Severity.FATAL, reportable);
            Jack.getSession().abortEventually();
          }
        }
      }
    }

    applyRules(rules, session);
  }

  @Nonnull
  private Collection<Rule> parseRules(
      @Nonnull JSession session,
      @Nonnull Reader reader,
      @Nonnull Location location) throws RecognitionException, CannotReadException {
    ANTLRReaderStream in;
    try {
      in = new ANTLRReaderStream(reader);
    } catch (IOException e) {
      throw new CannotReadException(location, e);
    }
    PreProcessorLexer lexer = new PreProcessorLexer(in);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    PreProcessorParser parser = new PreProcessorParser(tokens);

    return parser.rules(session, location);
  }

  private void applyRules(@Nonnull Collection<Rule> rules,
      @Nonnull JSession session) {
    Scope scope = new TypeToEmitScope(session);
    Collection<AddAnnotationStep> requests = new ArrayList<AddAnnotationStep>();
    for (Rule rule : rules) {
      Context context = new Context(rule);
      if (!rule.getSet().eval(scope, context).isEmpty()) {
        requests.addAll(context.getSteps());
      }
    }

    Map<Entry, Rule> map = new HashMap<Entry, Rule>();
    for (AddAnnotationStep request : requests) {
      request.apply(map);
    }
  }

  static class Entry {
    @Nonnull
    public final Annotable annotated;
    @Nonnull
    public final JAnnotationType annotationType;

    public Entry(@Nonnull Annotable annotated, @Nonnull JAnnotationType annotationType) {
      this.annotated = annotated;
      this.annotationType = annotationType;
    }

    @Override
    public final boolean equals(Object obj) {
      if (obj instanceof Entry) {
        Entry entry = (Entry) obj;

        return entry.annotated == annotated
            && entry.annotationType.equals(annotationType);
      }

      return false;
    }

    @Override
    public int hashCode() {
      return annotated.hashCode() ^ annotationType.hashCode();
    }
  }

  private static class JppParsingException extends ReportableException implements HasLocation {

    private static final long serialVersionUID = 1L;

    @Nonnull
    private final HasLocation locationProvider;

    public JppParsingException(@Nonnull HasLocation locationProvider, @Nonnull Throwable cause) {
      super(cause);
      this.locationProvider = locationProvider;
    }

    @Override
    @Nonnull
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.ERROR;
    }

    @Override
    @Nonnull
    public Location getLocation() {
      return locationProvider.getLocation();
    }

  }

}
