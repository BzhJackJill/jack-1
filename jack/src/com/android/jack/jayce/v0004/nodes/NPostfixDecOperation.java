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

package com.android.jack.jayce.v0004.nodes;

import com.android.jack.ir.ast.JPostfixDecOperation;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.jayce.v0004.io.ExportSession;
import com.android.jack.jayce.v0004.io.ImportHelper;
import com.android.jack.jayce.v0004.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0004.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0004.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Unary operator expression for {@code postfix dec}.
 */
public class NPostfixDecOperation extends NExpression {

  @Nonnull
  public static final Token TOKEN = Token.POSTFIX_DEC_OPERATION;

  @CheckForNull
  public NExpression arg;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JPostfixDecOperation operation = (JPostfixDecOperation) node;
    arg = (NExpression) loader.load(operation.getArg());
    sourceInfo = operation.getSourceInfo();
  }

  @Override
  @Nonnull
  public JPostfixDecOperation exportAsJast(@Nonnull ExportSession exportSession)
      throws JMethodLookupException, JTypeLookupException {
    assert sourceInfo != null;
    assert arg != null;
    return new JPostfixDecOperation(sourceInfo, arg.exportAsJast(exportSession));
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeNode(arg);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    arg = in.readNode(NExpression.class);

  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}
