/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.ir.ast;


import com.android.jack.Jack;
import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.naming.TypeName;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.load.ClassOrInterfaceLoader;
import com.android.jack.load.NopClassOrInterfaceLoader;
import com.android.jack.lookup.JMethodIdLookupException;
import com.android.jack.lookup.JMethodLookupException;
import com.android.jack.lookup.JMethodWithReturnLookupException;
import com.android.jack.util.AnnotationUtils;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.util.location.HasLocation;
import com.android.sched.util.location.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Base class for any reference type.
 */
@Description("Declared type")
public abstract class JDefinedClassOrInterface extends JDefinedReferenceType
  implements JClassOrInterface, Annotable, CanBeAbstract, CanBeFinal, HasLocation, HasModifier {

  protected ArrayList<JField> fields = new ArrayList<JField>();

  protected ArrayList<JMethod> methods = new ArrayList<JMethod>();

  /**
   * The type which originally enclosed this type. Null if this class was a
   * top-level type. Note that all classes are converted to top-level types in
   * {@code JackIrBuilder}; this information is for tracking purposes.
   */
  private JClassOrInterface enclosingType;

  /**
   * List of inner types i.e. the types whose enclosed by this type.
   */
  @Nonnull
  private final List<JClassOrInterface> inners = new ArrayList<JClassOrInterface>();

  /**
   * True if the type is to be included in outputs.
   */
  private boolean isToEmit = false;

  /**
   * This type's modifier.
   */
  private int modifier;

  @Nonnull
  protected final List<JAnnotation> annotations = new ArrayList<JAnnotation>();

  @Nonnull
  private JPackage enclosingPackage;

  @Nonnull
  protected List<JMethodIdWide> phantomMethodsWide = new ArrayList<JMethodIdWide>();

  @Nonnull
  protected List<JMethodId> phantomMethods = new ArrayList<JMethodId>();

  @Nonnull
  protected List<JFieldId> phantomFields = new ArrayList<JFieldId>();

  @Nonnull
  protected ClassOrInterfaceLoader loader;

  @Nonnull
  private final Location location;

  public JDefinedClassOrInterface(@Nonnull SourceInfo info, @Nonnull String name, int modifier,
      @Nonnull JPackage enclosingPackage, @Nonnull ClassOrInterfaceLoader loader) {
    super(info, name);
    assert NamingTools.isTypeIdentifier(name);
    assert JModifier.isTypeModifier(modifier);
    assert JModifier.isValidTypeModifier(modifier);
    this.modifier = modifier;
    this.enclosingPackage = enclosingPackage;
    this.enclosingPackage.addType(this);
    this.loader = loader;
    location = loader.getLocation(this);
    updateParents(enclosingPackage);
  }

  @Override
  public void setModifier(int modifier) {
    this.modifier = modifier;
  }

  @Nonnull
  public Collection<JClassOrInterface> getHierarchy() {
    HashSet<JClassOrInterface> hierarchy = new HashSet<JClassOrInterface>();

    for (JInterface jInterface : getImplements()) {
      hierarchy.add(jInterface);
      if (jInterface instanceof JDefinedInterface) {
        hierarchy.addAll(((JDefinedInterface) jInterface).getHierarchy());
      }
    }
    JClass superClass = getSuperClass();
    if (superClass != null) {
      hierarchy.add(superClass);
      if (superClass instanceof JDefinedClass) {
        hierarchy.addAll(((JDefinedClass) superClass).getHierarchy());
      }
    }
    return hierarchy;
  }


  /**
   * Adds a field to this type.
   */
  public void addField(@Nonnull JField field) {
    assert field.getEnclosingType() == this;
    assert getPhantomField(field.getName(), field.getType(), field.getId().getKind()) == null;
    fields.add(field);
  }

  @Override
  @CheckForNull
  public <T extends Marker> T getMarker(@Nonnull Class<T> cls) {
    loader.ensureMarker(this, cls);
    return super.getMarker(cls);
  }

  @Override
  @Nonnull
  public Collection<Marker> getAllMarkers() {
    loader.ensureMarkers(this);
    return super.getAllMarkers();
  }

  @Override
  public <T extends Marker> boolean containsMarker(@Nonnull Class<T> cls) {
    loader.ensureMarker(this, cls);
    return super.containsMarker(cls);
  }

  @Override
  public <T extends Marker> T removeMarker(@Nonnull Class<T> cls) {
    loader.ensureMarker(this, cls);
    return super.removeMarker(cls);
  }

  @Nonnull
  @Override
  public <T extends Marker> T getMarkerOrDefault(@Nonnull T defaultMarker) {
    loader.ensureMarker(this, defaultMarker.getClass());
    return super.getMarkerOrDefault(defaultMarker);
  }

  @Override
  @CheckForNull
  public <T extends Marker> T addMarkerIfAbsent(@Nonnull T marker) {
    loader.ensureMarker(this, marker.getClass());
    return super.addMarkerIfAbsent(marker);
  }

  @Override
  public void addAllMarkers(@Nonnull Collection<Marker> collection) {
    loader.ensureMarkers(this);
    super.addAllMarkers(collection);
  }

  @Override
  @Nonnull
  public List<JInterface> getImplements() {
    loader.ensureHierarchy(this);
    return super.getImplements();
  }

  @Override
  public void setEnclosingPackage(@CheckForNull JPackage enclosingPackage) {
    assert enclosingPackage != null;
    this.enclosingPackage = enclosingPackage;
    updateParents(enclosingPackage);
    assert Jack.getSession().getPhantomLookup().check(this);
  }

  /**
   * Adds a method to this type.
   */
  public void addMethod(JMethod method) {
    assert method.getEnclosingType() == this;
    assert getPhantomMethodWide(method.getName(), method.getMethodIdWide().getParamTypes(),
        method.getMethodIdWide().getKind()) == null;
    methods.add(method);
  }

  /**
   * Returns the type which encloses this type.
   *
   * @return The enclosing type. May be {@code null}.
   */
  public JClassOrInterface getEnclosingType() {
    loader.ensureEnclosing(this);
    return enclosingType;
  }

  /**
   * Returns this type's fields;does not include fields defined in a super type
   * unless they are overridden by this type.
   */
  public List<JField> getFields() {
    loader.ensureFields(this);
    return fields;
  }

  @Nonnull
  public List<JField> getFields(@Nonnull String fieldName) {
    loader.ensureFields(this, fieldName);
    List<JField> fieldsFound = new ArrayList<JField>();
    for (JField field : getFields()) {
      if (field.getName().equals(fieldName)) {
        fieldsFound.add(field);
      }
    }
    return fieldsFound;
  }

  @Override
  @Nonnull
  public JPackage getEnclosingPackage() {
    return enclosingPackage;
  }

  /**
   * Returns this type's declared methods; does not include methods defined in a
   * super type unless they are overridden by this type.
   */
  public List<JMethod> getMethods() {
    loader.ensureMethods(this);
    return methods;
  }

  /**
   * Returns the {@link JMethod} with the signature {@code signature} declared for this type.
   *
   * @return Returns the matching method if any, throws a {@link JMethodLookupException} otherwise.
   */
  @Nonnull
  public JMethod getMethod(@Nonnull String name, @Nonnull JType returnType,
      @Nonnull List<? extends JType> args) throws JMethodLookupException {
    loader.ensureMethod(this, name, args, returnType);
    for (JMethod m : methods) {
      if (m.getMethodIdWide().equals(name, args) && m.getType().isSameType(returnType)) {
        // Only one method can be found due to the fact that we also use return type to filter
        return m;
      }
    }
    throw new JMethodWithReturnLookupException(this, name, args, returnType);
  }

  /**
   * Returns the {@link JMethod} with the signature {@code signature} declared for this type.
   *
   * @return Returns the matching method if any, throws a {@link JMethodLookupException} otherwise.
   */
  @Nonnull
  public JMethod getMethod(@Nonnull String name, @Nonnull JType returnType,
      @Nonnull JType... args) throws JMethodLookupException {
    return (getMethod(name, returnType, Arrays.asList(args)));
  }

  @Override
  public boolean isToEmit() {
    return isToEmit;
  }

  /**
   * Sets the type which encloses this types.
   *
   * @param enclosingType May be {@code null}.
   */
  public void setEnclosingType(JClassOrInterface enclosingType) {
    this.enclosingType = enclosingType;
  }

  public void setToEmit(boolean isToEmit) {
    this.isToEmit = isToEmit;
  }

  @Override
  public int getModifier() {
    loader.ensureModifier(this);
    return modifier;
  }

  public boolean isPublic() {
    return JModifier.isPublic(getModifier());
  }

  public boolean isProtected() {
    return JModifier.isProtected(getModifier());
  }

  public boolean isPrivate() {
    return JModifier.isPrivate(getModifier());
  }

  public boolean isStatic() {
    return JModifier.isStatic(getModifier());
  }

  public boolean isStrictfp() {
    return JModifier.isStrictfp(getModifier());
  }

  public boolean isSynthetic() {
    return JModifier.isSynthetic(getModifier());
  }

  @Override
  public boolean isAbstract() {
    return JModifier.isAbstract(getModifier());
  }

  public boolean isAnonymous() {
    return TypeName.getSimpleName(this).isEmpty();
  }

  public void setAbstract() {
    modifier = getModifier() | JModifier.ABSTRACT;
  }

  @Override
  public boolean isFinal() {
    return JModifier.isFinal(getModifier());
  }

  public void setFinal() {
    modifier = getModifier() | JModifier.FINAL;
  }

  @Override
  public void addAnnotation(@Nonnull JAnnotation annotation) {
    annotations.add(annotation);
  }

  @Override
  @Nonnull
  public List<JAnnotation> getAnnotations(@Nonnull JAnnotationType annotationType) {
    loader.ensureAnnotation(this, annotationType);
    return Jack.getUnmodifiableCollections().getUnmodifiableList(
        AnnotationUtils.getAnnotation(annotations, annotationType));
  }

  @Override
  @Nonnull
  public Collection<JAnnotation> getAnnotations() {
    loader.ensureAnnotations(this);
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(annotations);
  }

  @Override
  @Nonnull
  public Set<JAnnotationType> getAnnotationTypes() {
    loader.ensureAnnotations(this);
    return Jack.getUnmodifiableCollections().getUnmodifiableSet(
        AnnotationUtils.getAnnotationTypes(annotations));
  }

  @Nonnull
  public List<JClassOrInterface> getMemberTypes() {
    loader.ensureInners(this);
    return Jack.getUnmodifiableCollections().getUnmodifiableList(inners);
  }

  public void addMemberType(@Nonnull JClassOrInterface jDeclaredType) {
    synchronized (inners) {
      inners.add(jDeclaredType);
    }
  }

  public void removeMemberType(@Nonnull JClassOrInterface jDeclaredType) {
    synchronized (inners) {
      int index = inners.indexOf(jDeclaredType);
      if (index != -1) {
        inners.remove(index);
      }
    }
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (!transform(inners, existingNode, (JClassOrInterface) newNode, transformation)) {
      if (!transform(annotations, existingNode, (JAnnotation) newNode, transformation)) {
        super.transform(existingNode, newNode, transformation);
      }
    }
  }

  @Nonnull
  @Override
  public JMethodIdWide getMethodIdWide(
      @Nonnull String name,
      @Nonnull List<? extends JType> argsType,
      @Nonnull MethodKind kind)
      throws JMethodLookupException {
    assert !(name.contains("(") || name.contains(")"));
    loader.ensureMethods(this);
    for (JMethod method : methods) {
      JMethodIdWide id = method.getMethodIdWide();
      if (id.equals(name, argsType, kind)) {
        return id;
      }
    }

    for (JInterface jType : getImplements()) {
      try {
        return jType.getMethodIdWide(name, argsType, kind);
      } catch (JMethodLookupException e) {
        // search next
      }
    }
    JClass superClass = getSuperClass();
    if (superClass != null) {
      try {
        return superClass.getMethodIdWide(name, argsType, kind);
      } catch (JMethodLookupException e) {
        // let the following exception be thrown
      }
    }

    throw new JMethodIdLookupException(this, name, argsType);
  }

  @Nonnull
  @Override
  public JMethodIdWide getOrCreateMethodIdWide(@Nonnull String name,
      @Nonnull List<? extends JType> argsType,
      @Nonnull MethodKind kind) {
    try {
      return getMethodIdWide(name, argsType, kind);
    } catch (JMethodLookupException e) {
      synchronized (phantomMethodsWide) {
        JMethodIdWide id = getPhantomMethodWide(name, argsType, kind);

        if (id == null) {
          id = new JMethodIdWide(name, argsType, kind);
          phantomMethodsWide.add(id);
        }

        return id;
      }
    }
  }

  @Nonnull
  @Override
  public JMethodId getMethodId(
      @Nonnull String name,
      @Nonnull List<? extends JType> argsType,
      @Nonnull MethodKind kind,
      @Nonnull JType returnType)
      throws JMethodLookupException {
    assert !(name.contains("(") || name.contains(")"));
    loader.ensureMethods(this);
    for (JMethod method : methods) {
      JMethodId id = method.getMethodId();
      if (id.getType().equals(returnType) && id.getMethodIdWide().equals(name, argsType, kind)) {
        return id;
      }
    }

    for (JInterface jType : getImplements()) {
      try {
        return jType.getMethodId(name, argsType, kind, returnType);
      } catch (JMethodLookupException e) {
        // search next
      }
    }
    JClass superClass = getSuperClass();
    if (superClass != null) {
      try {
        return superClass.getMethodId(name, argsType, kind, returnType);
      } catch (JMethodLookupException e) {
        // let the following exception be thrown
      }
    }
    throw new JMethodWithReturnLookupException(this, name, argsType, returnType);
  }

  @Nonnull
  @Override
  public JMethodId getOrCreateMethodId(
      @Nonnull String name,
      @Nonnull List<? extends JType> argsType,
      @Nonnull MethodKind kind,
      @Nonnull JType returnType) {
    try {
      return getMethodId(name, argsType, kind, returnType);
    } catch (JMethodLookupException e) {
      synchronized (phantomMethods) {
        JMethodId id = getPhantomMethod(name, argsType, kind, returnType);

        if (id == null) {
          id = new JMethodId(getOrCreateMethodIdWide(name, argsType, kind), returnType);
          phantomMethods.add(id);
        }

        return id;
      }
    }
  }

  @Override
  @Nonnull
  public JFieldId getOrCreateFieldId(@Nonnull String name, @Nonnull JType type,
      @Nonnull FieldKind kind) {
    try {
      return getFieldId(name, type, kind);
    } catch (JFieldLookupException e) {
      synchronized (phantomFields) {
        JFieldId id = getPhantomField(name, type, kind);
        if (id == null) {
          id = new JFieldId(name, type, kind);
          phantomFields.add(id);
        }
        return id;
      }
    }
  }

  @Override
  @Nonnull
  public JFieldId getFieldId(
      @Nonnull String name, @Nonnull JType type,
      @Nonnull FieldKind kind) throws JFieldLookupException {
    loader.ensureFields(this);
    for (JField field : fields) {
      JFieldId id = field.getId();
      if (id.equals(name, type, kind)) {
        return id;
      }
    }

    for (JInterface jType : getImplements()) {
      try {
        return jType.getFieldId(name, type, kind);
      } catch (JFieldLookupException e) {
        // search next
      }
    }
    JClass superClass = getSuperClass();
    if (superClass != null) {
      try {
        return superClass.getFieldId(name, type, kind);
      } catch (JFieldLookupException e) {
        // let the following exception be thrown
      }
    }

    throw new JFieldLookupException(this, name, type);
  }

  @CheckForNull
  private JMethodId getPhantomMethod(
      @Nonnull String name,
      @Nonnull List<? extends JType> argsType,
      @Nonnull MethodKind kind,
      @Nonnull JType returnType) {
    synchronized (phantomMethods) {
      for (JMethodId id : phantomMethods) {
        if (id.getType().equals(returnType) && id.getMethodIdWide().equals(name, argsType, kind)) {
          return id;
        }
      }
    }
    return null;
  }

  @CheckForNull
  private JMethodIdWide getPhantomMethodWide(@Nonnull String name,
      @Nonnull List<? extends JType> argsType,
      @Nonnull MethodKind kind) {
    synchronized (phantomMethodsWide) {
      for (JMethodIdWide id : phantomMethodsWide) {
        if (id.equals(name, argsType, kind)) {
          return id;
        }
      }
    }
    return null;
  }

  @CheckForNull
  private JFieldId getPhantomField(@Nonnull String name, @Nonnull JType type,
      @Nonnull FieldKind kind) {
    synchronized (phantomFields) {
      for (JFieldId id : phantomFields) {
        if (id.equals(name, type, kind)) {
          return id;
        }
      }
    }
    return null;
  }

  @Nonnull
  public ClassOrInterfaceLoader getLoader() {
    return loader;
  }

  @Override
  @CheckForNull
  public JPrimitiveType getWrappedType() {
    return getWrappedType(this);
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return location;
  }

  public void removeLoader() {
    loader = NopClassOrInterfaceLoader.INSTANCE;
  }

  @Override
  public final boolean isSameType(@Nonnull JType type) {
    if (type instanceof HasEnclosingPackage) {
      return this.getEnclosingPackage() == ((HasEnclosingPackage) type).getEnclosingPackage()
          && name.equals(type.getName());
    } else {
      return false;
    }
  }

  @Override
  public void checkValidity() {
    if (parent == null || parent != enclosingPackage) {
      throw new JNodeInternalError(this, "Invalid parent or enclosing package");
    }

    /* For now it is a valid situation to have an enclosing type defined that is not known as inner.
     * This happens at least for Local classes defined in initializer.
     */

    for (JClassOrInterface inner : inners) {
      if (inner instanceof JDefinedClassOrInterface
          && ((JDefinedClassOrInterface) inner).getEnclosingType() != this) {
        throw new JNodeInternalError(inner, "Invalid enclosing class or interface for member type");
      }
    }
  }

  @Override
  public void setName(@Nonnull String name) {
    enclosingPackage.removeItemWithName(this);
    super.setName(name);
  }
}
