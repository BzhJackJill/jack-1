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

import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.jayce.JayceClassOrInterfaceLoader;
import com.android.jack.jayce.NodeLevel;
import com.android.jack.jayce.v0004.io.ExportSession;
import com.android.jack.jayce.v0004.io.ImportHelper;
import com.android.jack.jayce.v0004.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0004.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0004.io.Token;
import com.android.jack.lookup.JMethodLookupException;
import com.android.jack.util.NamingTools;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java class type reference expression.
 */
public class NClassType extends NDeclaredType {

  @Nonnull
  public static final Token TOKEN = Token.CLASS;

  public int modifiers;

  @CheckForNull
  public String signature;

  @CheckForNull
  public String superClass;

  @CheckForNull
  public String enclosingType;

  @CheckForNull
  public String enclosingMethodClass;

  @CheckForNull
  public String enclosingMethod;

  @Nonnull
  public List<String> inners = Collections.emptyList();

  @Nonnull
  public List<String> superInterfaces = Collections.emptyList();

  @Nonnull
  public List<NMarker> markers = Collections.emptyList();

  @CheckForNull
  protected SourceInfo sourceInfo;

  @Override
  @Nonnull
  public SourceInfo getSourceInfos() {
    assert sourceInfo != null;
    return sourceInfo;
  }

  @Override
  public void setSourceInfos(@Nonnull SourceInfo sourceInfo) {
    this.sourceInfo = sourceInfo;
  }

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JDefinedClass jClassType = (JDefinedClass) node;
    modifiers = jClassType.getModifier();
    signature = ImportHelper.getSignatureName(jClassType);
    superClass = ImportHelper.getSignatureName(jClassType.getSuperClass());
    superInterfaces = ImportHelper.getSignatureNameList(jClassType.getImplements());
    enclosingType = ImportHelper.getSignatureName(jClassType.getEnclosingType());
    enclosingMethodClass = ImportHelper.getMethodClassSignature(jClassType.getEnclosingMethod());
    enclosingMethod = ImportHelper.getMethodSignature(jClassType.getEnclosingMethod());
    inners = ImportHelper.getSignatureNameList(jClassType.getMemberTypes());
    setFields(loader.load(NField.class, jClassType.getFields()));
    setMethods(loader.load(NMethod.class, jClassType.getMethods()));
    annotations = loader.load(NAnnotation.class, jClassType.getAnnotations());
    markers = loader.load(NMarker.class, jClassType.getAllMarkers());
    sourceInfo = jClassType.getSourceInfo();
  }

  @Nonnull
  @Override
  public JDefinedClass exportAsJast(@Nonnull ExportSession exportSession) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public JDefinedClass create(@Nonnull JPackage enclosingPackage,
      @Nonnull JayceClassOrInterfaceLoader loader) {
    assert signature != null;
    String binaryName = NamingTools.getClassBinaryNameFromDescriptor(signature);
    String simpleName = NamingTools.getSimpleClassNameFromBinaryName(binaryName);
    modifiers &= ~JModifier.ANONYMOUS_TYPE;
    JDefinedClass jClassType =
        new JDefinedClass(SourceInfo.UNKNOWN, simpleName, modifiers, enclosingPackage, loader);
    return jClassType;
  }

  @Override
  public void loadStructure(@Nonnull JDefinedClassOrInterface loading,
      @Nonnull JayceClassOrInterfaceLoader loader) {
    assert sourceInfo != null;
    assert signature != null;
    JDefinedClass jClassType = (JDefinedClass) loading;
    ExportSession exportSession = new ExportSession(loader.getSession(), NodeLevel.STRUCTURE);
    exportSession.setCurrentType(jClassType);
    loading.setSourceInfo(sourceInfo);
    if (superClass != null) {
      jClassType.setSuperClass(exportSession.getLookup().getClass(superClass));
    }
    for (String superInterface : superInterfaces) {
      jClassType.addImplements(exportSession.getLookup().getInterface(superInterface));
    }
    if (enclosingType != null) {
      jClassType.setEnclosingType(
          (JClassOrInterface) exportSession.getLookup().getType(enclosingType));
    }
    if (enclosingMethodClass != null) {
      assert enclosingMethod != null;
      JClass enclosingMethodJClass =
          exportSession.getLookup().getClass(enclosingMethodClass);
      if (enclosingMethodJClass instanceof JDefinedClass) {
        try {
          jClassType.setEnclosingMethod(
              exportSession.getDeclaredMethod((JDefinedClass) enclosingMethodJClass,
                  enclosingMethod));
        } catch (JMethodLookupException e) {
          // Method does not longer exists but anonymous already exists, could be trigger by build
          // tricky mechanism, skip it to go ahead
        }
      }
    }
    for (String memberType : inners) {
      jClassType.addMemberType(
          (JClassOrInterface) exportSession.getLookup().getType(memberType));
    }
    for (NField field : getFields()) {
      JField jField = field.exportAsJast(exportSession, loader);
      jField.setEnclosingType(jClassType);
      jClassType.addField(jField);
    }
    for (NMethod method : getMethods()) {
      JMethod jMethod = method.exportAsJast(exportSession, loader);
      jClassType.addMethod(jMethod);
    }
    for (NMarker marker : markers) {
      jClassType.addMarker(marker.exportAsJast(exportSession));
    }
 }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeInt(modifiers);
    out.writeId(signature);
    out.writeId(superClass);
    out.writeIds(superInterfaces);
    out.writeId(enclosingType);
    out.writeId(enclosingMethodClass);
    out.writeId(enclosingMethod);
    out.writeIds(inners);
    out.writeNodes(getFields());
    out.writeNodes(getMethods());
    out.writeNodes(annotations);
    out.writeNodes(markers);

  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    level = in.getNodeLevel();
    modifiers = in.readInt();
    signature = in.readId();
    if (level != NodeLevel.TYPES) {
      superClass = in.readId();
      superInterfaces = in.readIds();
      enclosingType = in.readId();
      enclosingMethodClass = in.readId();
      enclosingMethod = in.readId();
      inners = in.readIds();
      setFields(in.readNodes(NField.class));
      setMethods(in.readNodes(NMethod.class));
      annotations = in.readNodes(NAnnotation.class);
      markers = in.readNodes(NMarker.class);
    }
  }

  @Nonnull
  @Override
  public Token getToken() {
    return TOKEN;
  }

  @Nonnull
  @Override
  public String getSignature() {
    assert signature != null;
    return signature;
  }
}
