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

package com.android.jack.transformations.ast;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.ir.ast.JAbsentArrayDimension;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Remove compound assignment operator.
 */
@Description("Remove new array with multiple dimension.")
@Name("MultiDimensionNewArrayRemover")
@Transform(remove = {MultiDimensionNewArray.class, ThreeAddressCodeForm.class}, add = {
    JNewArray.class, JMethodCall.class, JClassLiteral.class, JDynamicCastOperation.class,
    InitInNewArray.class})
@Filter(TypeWithoutPrebuiltFilter.class)
public class MultiDimensionNewArrayRemover implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  @Nonnull
  private final JClass jlo =
      Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT);

  @Nonnull
  private final JClass jlc =
      Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_CLASS);

  @Nonnull
  private final JClass reflectArrayType =
      Jack.getSession().getPhantomLookup().getClass("Ljava/lang/reflect/Array;");
  @Nonnull
  private final JArrayType intArrayType =
      Jack.getSession().getLookup().getArrayType(JPrimitiveTypeEnum.INT.getType(), 1);
  @Nonnull
  private final JMethodIdWide newInstance;

  private class Visitor extends JVisitor {

    @Nonnull
    private final TransformationRequest tr;

    public Visitor(@Nonnull TransformationRequest tr) {
      this.tr = tr;
    }

    @Override
    public boolean visit(@Nonnull JNewArray newArray) {

      List<JExpression> allDims = newArray.getDims();
      if (!allDims.isEmpty()) {
        List<JExpression> presentDimensions = getPresentDimensions(allDims);
        int nbPresentDimensions = presentDimensions.size();
        if (nbPresentDimensions > 1) {
          SourceInfo sourceInfo = newArray.getSourceInfo();
          JMethodCall call = new JMethodCall(sourceInfo,
              null,
              reflectArrayType,
              newInstance,
              jlo,
              newInstance.canBeVirtual());
          call.addArg(new JClassLiteral(sourceInfo,
              getComponentTypeForNewInstance(newArray, nbPresentDimensions), jlc));
         call.addArg(JNewArray.createWithInits(sourceInfo, intArrayType, presentDimensions));
          tr.append(new Replace(newArray, new JDynamicCastOperation(sourceInfo, call, newArray
              .getArrayType())));
        }
      }
      return super.visit(newArray);
    }

    /**
     * Return JArrayType of {@code newArray} less one dimension per declared dimension of the
     * {@link JNewArray}. For example : getComponentTypeForNewInstance(<new int[1][2][]>) returns
     * JArrayType(<int[]>).
     */
    @Nonnull
    private JType getComponentTypeForNewInstance(@Nonnull JNewArray newArray,
        @Nonnegative int nbPresentDim) {
      JType componentType = newArray.getArrayType();
      for (int i = 0; i < nbPresentDim; i++) {
        componentType = ((JArrayType) componentType).getElementType();
      }
      return componentType;
    }

    /**
     * Filter out JAbsentArrayDimension.
     */
    @Nonnull
    private List<JExpression> getPresentDimensions(@Nonnull List<JExpression> newArrayDims) {
      List<JExpression> presentDims = new ArrayList<JExpression>(newArrayDims.size());
      boolean inPresentDims = true;
      for (JExpression expression : newArrayDims) {
        if (expression instanceof JAbsentArrayDimension) {
          inPresentDims = false;
        } else {
          assert inPresentDims;
          presentDims.add(expression);
        }
      }
      return presentDims;
    }
  }

  public MultiDimensionNewArrayRemover() {
    List<JType> argsType = new ArrayList<JType>(2);
    argsType.add(jlc);
    argsType.add(intArrayType);
    newInstance = reflectArrayType.getOrCreateMethodIdWide("newInstance", argsType,
        MethodKind.STATIC);
  }

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(method);
    Visitor visitor = new Visitor(tr);
    visitor.accept(method);
    tr.commit();
  }

}
