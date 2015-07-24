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

package com.android.jack.transformations.enums.opt;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JEnumField;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.library.TypeInInputLibraryLocation;
import com.android.jack.load.NopClassOrInterfaceLoader;
import com.android.jack.transformations.enums.OptimizationUtil;
import com.android.jack.transformations.enums.SwitchEnumSupport;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
/**
 * This class is used to manage the synthetic class, as multiple synthetic switch map
 * classes are created. The idea behind the scene is that if it is decided that creating
 * synthetic class is worth of, then create it. Otherwise follow {@link SwitchEnumSupport}'s
 * solution.
 */
public class SyntheticClassManager {
  // the prefix of synthetic switch map class
  public static final String SyntheticSwitchmapClassNamePrefix = "SyntheticSwitchmapClass-";

  // the package name of synthetic switch map class
  @Nonnull
  public static final String PublicSyntheticSwitchmapClassPkgName =
      "com/android/jack/enums/synthetic";

  // the statistic counting the number of synthetic switch map class created during current
  // compilation.
  @Nonnull
  public static final StatisticId<Counter> SYNTHETIC_SWITCHMAP_CLASS = new StatisticId<Counter>(
      "jack.optimization.enum.switch.synthetic.class.increase",
      "Total number of synthetic class created",
      CounterImpl.class, Counter.class);

  //this map represents relationship from the package to synthetic class. There should only be
  // one synthetic class at most per package. Synthetic class cannot be located anywhere because
  // the enum field may not be visible to it
  @Nonnull
  private final Map<JPackage, JDefinedClass> syntheticClassMap = Maps.newHashMap();

  // statistic tracer. It will be used to collect statistic measurement
  @Nonnull
  private final Tracer statisticTracer = TracerFactory.getTracer();

  // global session, which will be used later
  @Nonnull
  private final JSession session = Jack.getSession();

  // support utility provides basic APIs to use
  @Nonnull
  private final OptimizationUtil supportUtil;

  // if the type cache is allowed to reload. True means it can be reloaded
  private boolean reloadTypeCache = true;

  // Minimal number of user classes to enable enum optimization. If it is less or equals than 0,
  // use the default algorithm {@link #isOptimizationWorthwhile(JDefinedEnum)}
  private final int optimizeThreshold;

  /**
   * Constructor.
   * @param supportUtil The instance of {@link OptimizationUtil}
   * @param optimizeThreshold Number of classes triggering strategies, details is given at
   * {@link #isOptimizationWorthwhile(JDefinedEnum)}
   */
  public SyntheticClassManager(@Nonnull OptimizationUtil supportUtil, int optimizeThreshold) {
    this.supportUtil = supportUtil;
    this.optimizeThreshold = optimizeThreshold;
  }

  /**
   * The logic behind the scene is that different strategies are needed based on
   * the profiling of given enum:
   * <li> enumType is public </li>
   * In this case, only a synthetic class is created at SyntheticSwitchmapClassPkgName if
   * the optimizing condition is met {@link #isOptimizationWorthwhile(JDefinedEnum)}.
   *
   * <li> enumType is private </li>
   * In this case, synthetic class is created at the same level of user class only when
   * it detects it is worth to do that. Otherwise switch map initializer will be created
   * inside of user class like what is done in {@link SwitchEnumSupport}.
   *
   * <li> enumType is protected </li>
   * In this case, synthetic class is created at the same level of user class only when
   * it detects it is worth to do that. Otherwise switch map initializer will be created
   * inside of user class like what is done in {@link SwitchEnumSupport}.
   *
   * <li> enumType is package private </li>
   * In this case, synthetic class is created at the same level of user class only when
   * it detects it is worth to do that. Otherwise switch map initializer will be created
   * inside of user class like what is done in {@link SwitchEnumSupport}.
   *
   * @param enumType The related enum for which synthetic class will be generated
   * @param createIfNotExist True means create class is not found, otherwise throw exception
   *
   * @return The synthetic class to create
   * @throws AssertionError
   */
  @CheckForNull
  public JDefinedClass getOrCreateRelatedSyntheticClass(@Nonnull JDefinedEnum enumType,
      boolean createIfNotExist) {
    JPackage syntheticClassPackage;
    if (enumType.isPublic()) {
      // create synthetic class at a specific package if the enum is public. We can access
      // it from anywhere
      syntheticClassPackage = supportUtil.getLookup().getOrCreatePackage(
          PublicSyntheticSwitchmapClassPkgName);
    } else {
      syntheticClassPackage = enumType.getEnclosingPackage();
    }
    SwitchEnumUsageMarker enumUsageMarker = syntheticClassPackage.getMarker(
        SwitchEnumUsageMarker.class);
    assert enumUsageMarker != null;
    Set<JDefinedEnum> usedEnumsType = Sets.newHashSet(enumUsageMarker.getUsedEnumsType());
    // init the markers correspondingly
    initEnumOptimizationMarkers(usedEnumsType);
    boolean isOptWorth = isOptimizationWorthwhile(enumUsageMarker.getUses(), usedEnumsType);
    if (!isOptWorth) {
      // if optimization is not worth, don't optimize it because of class overhead
      return null;
    }
    // make sure only one synthetic class is created every prefix
    JDefinedClass syntheticClass = syntheticClassMap.get(syntheticClassPackage);
    if (syntheticClass != null) {
      // do nothing
      return syntheticClass;
    } else if (!createIfNotExist) {
      // if synthetic class is not found and createIfNotExist is set to false
      throw new AssertionError("Not found synthetic class under prefix: " + syntheticClassPackage);

    }
    // if there is no synthetic class existed, create one. Note each synthetic switch map
    // class will have a unique name base on the related enums inside it
    String uuid = getSyntheticClassUUID(usedEnumsType);
    // if something new, make sure all the references to the old class must be changed
    // correspondingly
    String syntheticClassFullName = "L" + Jack.getLookupFormatter().getName(syntheticClassPackage)
        + "/" + SyntheticSwitchmapClassNamePrefix + uuid + ";";
    syntheticClass = checkAndInitializeSyntheticClass(syntheticClassFullName);

    syntheticClassMap.put(syntheticClassPackage, syntheticClass);
    // increase the statistic measurement for synthetic class created
    statisticTracer.getStatistic(SYNTHETIC_SWITCHMAP_CLASS).incValue();
    return syntheticClass;
  }

  /**
   * Determine if the given class is synthetic switch map class.
   * @param cls The class to check
   *
   * @return true if given class is synthetic class
   */
  public static boolean isSyntheticSwitchMapClass(@Nonnull JDefinedClass cls) {
    String shortName = cls.getName();
    return shortName.startsWith(SyntheticSwitchmapClassNamePrefix);
  }

  /**
   * Check and create synthetic switch map class which never exists before. The class is
   * added to the session.
   * @param syntheticClassFullName The full class name of synthetic class
   *
   * @return The created synthetic switch map class
   */
  @Nonnull
  private JDefinedClass checkAndInitializeSyntheticClass(@Nonnull String syntheticClassFullName) {
    JDefinedClass sourceSyntheticClass = null;
    boolean needCreateSyntheticClass = false;
    try {
      sourceSyntheticClass = supportUtil.getLookup().getClass(syntheticClassFullName);
      if (sourceSyntheticClass.getLocation() instanceof TypeInInputLibraryLocation) {
        // if the class exists inside of library, remove it first. Because we want to
        // avoid the dependencies between libraries and source. For each source, if
        // the synthetic class is required, then we generate it. We cannot rely on
        // library even it may have the same synthetic class
        JPackage enclosingPackage = sourceSyntheticClass.getEnclosingPackage();
        enclosingPackage.getLoadedTypes().remove(sourceSyntheticClass);
        // because we don't have API to remove type cache, we have to clear all the
        // types inside of lookup node once per compilation
        if (reloadTypeCache) {
          supportUtil.getLookup().clear();
          reloadTypeCache = false;
        }
        needCreateSyntheticClass = true;
      }
    } catch (JTypeLookupException e){
      // if the synthetic switch map class cannot be found, create it
      needCreateSyntheticClass = true;
    }
    if (needCreateSyntheticClass) {
      sourceSyntheticClass = initializeSyntheticClass(syntheticClassFullName);
    }
    assert sourceSyntheticClass != null;
    return sourceSyntheticClass;
  }

  /**
   * Initialize synthetic class and corresponding package. Both synthetic class as well as
   * Corresponding package are added into session.
   * @param syntheticClassFullName Synthetic switch map class full name which includes package
   *
   * @return The created synthetic switch map class
   */
  private JDefinedClass initializeSyntheticClass(@Nonnull String syntheticClassFullName) {
    JDefinedClass syntheticSwitchmapClass = createSyntheticClass(syntheticClassFullName);
    // set super class since it is required by {@link ClassDefItemBuilder}
    syntheticSwitchmapClass.setSuperClass(supportUtil.getObjectType());
    // it is not external class
    syntheticSwitchmapClass.setExternal(false);
    // make sure the future instrumentation knows this class
    Jack.getSession().addTypeToEmit(syntheticSwitchmapClass);

    // make sure this class exists
    if (supportUtil.getLookup().getClass(syntheticClassFullName) != syntheticSwitchmapClass) {
      throw new AssertionError("Existed synthetic switch map class doesn't match with: "
          + syntheticSwitchmapClass);
    }
    return syntheticSwitchmapClass;
  }

  /**
   * Create the class and fill its content later.
   * @param syntheticClassFullName The full class name for synthetic switch map class
   *
   * @return The created synthetic switch map class
   */
  private JDefinedClass createSyntheticClass(@Nonnull String syntheticClassFullName) {
    // check if it is illegal class name
    if (!syntheticClassFullName.startsWith("L") || !syntheticClassFullName.endsWith(";")) {
      throw new AssertionError("Invalid full class name: " + syntheticClassFullName);
    }

    // get the package name of full class name
    String classShortName;
    JPackage currentPackage;
    int lastIndex = syntheticClassFullName.lastIndexOf('/');
    if (lastIndex >= 0) {
      String pkgName = syntheticClassFullName.substring(1, lastIndex);
      classShortName = syntheticClassFullName.substring(lastIndex + 1,
          syntheticClassFullName.length() - 1);

      // create the package object
      currentPackage = supportUtil.getLookup().getOrCreatePackage(pkgName);
    } else {
      // if there is no "/", then classShortName is classFullName
      classShortName = syntheticClassFullName.substring(1, syntheticClassFullName.length()
          - 1);
      currentPackage = supportUtil.getLookup().getTopLevelPackage();
    }

    // create the class under current package
    JDefinedClass syntheticSwitchmapClass = new JDefinedClass(SourceInfo.UNKNOWN,
        classShortName, JModifier.PUBLIC | JModifier.FINAL | JModifier.SYNTHETIC,
        currentPackage, NopClassOrInterfaceLoader.INSTANCE);

    return syntheticSwitchmapClass;
  }

  /**
   * This function to determine if optimization is worth is based on experiment. The metric
   * may vary slightly across different versions of Jack.
   * @param uses the number of uses under the related package
   * @param enumsType the set of enum inside of synthetic class under a specific package
   *
   * @return true if it is worth optimization
   */
  private boolean isOptimizationWorthwhile(int uses, @Nonnull Set<JDefinedEnum> enumsType) {
    if (optimizeThreshold > 0) {
      // simply metric could be used here like number of user classes is more than 2.
      return uses >= optimizeThreshold;
    } else {
      // calculate the average of enum fields
      int enumFields = 0;
      for (JDefinedEnum enumType : enumsType) {
        EnumOptimizationMarker enumOptMarker = enumType.getMarker(EnumOptimizationMarker.class);
        assert enumOptMarker != null;
        enumFields += enumOptMarker.getEnumFields().size();
      }
      enumFields = enumFields / enumsType.size();
      // try a little complicated algorithm
      // the observation is that if an enum has
      // 1 constant enum fields, 3 user classes is enough to observe space reduction
      // 4 constant enum fields, 2 user classes is enough to observe space reduction
      if (enumFields >= 1 && enumFields < 4 && uses >= 3) {
        return true;
      } else if (enumFields >= 4 && uses >= 2) {
        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * Generate the UUID based on the given set of enum types. The basic idea is
   * to concatenate all the enum's full class name and enum field together,
   * e.g., $FULL_CLASS_OF_ENUM1$:Field1,Field2,Field3.$FULL_CLASS_OF_ENUM2$:Field1,
   * Field2,Field3.
   *
   * Please keep in mind that both the enums type and enum fields are first sorted in
   * alphabetic order, then concatenate them together. Then based on this string,
   * generate SHA-256 and use it as UUID.
   *
   * @param enumsType the set of enums type
   *
   * @return UUID
   */
  @Nonnull
  private String getSyntheticClassUUID(@Nonnull Set<JDefinedEnum> enumsType) {
    List<JDefinedEnum> sortedEnumsType = Lists.newArrayList(enumsType);
    Collections.sort(sortedEnumsType, new Comparator<JDefinedEnum>() {
      @Override
      public int compare(JDefinedEnum enum1, JDefinedEnum enum2) {
        return enum1.getName().compareTo(enum2.getName());
      }
    });

    StringBuffer sb = new StringBuffer();
    for (JDefinedEnum sortedEnumType : sortedEnumsType) {
      EnumOptimizationMarker enumOptMarker = sortedEnumType.getMarker(EnumOptimizationMarker.class);
      assert enumOptMarker != null;
      sb.append(Jack.getLookupFormatter().getName(sortedEnumType));
      sb.append(":");
      List<JEnumField> sortedEnumFields = enumOptMarker.sortEnumFields();
      for (JEnumField enumField : sortedEnumFields) {
        sb.append(enumField.getName());
        sb.append(",");
      }
      sb.append(".");
    }
    String sig = sb.toString();
    sb.delete(0, sb.length());
    MessageDigest md;
    try {
      // can use either MD5, SHA-256, SHA-512, unless it is deterministic
      md = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new AssertionError();
    }
    byte[] uuid = md.digest(sig.getBytes());
    for (int i = 0; i < uuid.length; i++) {
      String hexString = Integer.toHexString(0xff & uuid[i]);
      if (hexString.length() == 1) {
        sb.append('0');
      }
      sb.append(hexString);
    }
    return sb.toString();
  }

  /**
   * Initialize the EnumOptimizationMarker for all the enum types.
   * @param enumsType the set of enums
   */
  private void initEnumOptimizationMarkers(@Nonnull Set<JDefinedEnum> enumsType) {
    for (JDefinedEnum enumType : enumsType) {
      if (enumType.containsMarker(EnumOptimizationMarker.class)) {
        continue;
      }
      // count the total number of enum literals defined inside
      EnumOptimizationMarker enumOptMarker = new EnumOptimizationMarker();
      enumType.addMarker(enumOptMarker);
      for (JField enumField : enumType.getFields()) {
        if (!(enumField instanceof JEnumField)) {
          continue;
        }
        enumOptMarker.addEnumField((JEnumField) enumField);
      }
    }
  }
}
