/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.load;

import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JParameter;
import com.android.sched.marker.Marker;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.NoLocation;

import javax.annotation.Nonnull;

/**
 * A {@link ParameterLoader} doing nothing.
 */
public class NopParameterLoader implements ParameterLoader {

  @Nonnull
  public static final ParameterLoader INSTANCE = new NopParameterLoader();
  @Nonnull
  private static final NoLocation NO_LOCATION = new NoLocation();

  private NopParameterLoader() {
    // Nothing to do
  }

  @Override
  @Nonnull
  public Location getLocation(@Nonnull JParameter loaded) {
    return NO_LOCATION;
  }

  @Override
  public void ensureMarkers(@Nonnull JParameter loaded) {
    // Nothing to do
  }

  @Override
  public void ensureMarker(@Nonnull JParameter loaded, @Nonnull Class<? extends Marker> cls) {
    // Nothing to do
  }

  @Override
  public void ensureAnnotations(@Nonnull JParameter loaded) {
    // Nothing to do
  }

  @Override
  public void ensureAnnotation(@Nonnull JParameter loaded, @Nonnull JAnnotationType annotation) {
    // Nothing to do
  }

}