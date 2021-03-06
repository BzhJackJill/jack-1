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

package com.android.jack.library;

import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.config.Config;
import com.android.sched.util.config.category.Category;

import javax.annotation.Nonnull;

/**
 * Define a category to specify that a property can impact the compatibility of the prebuilts
 * contained by a library with the current compilation configuration.
 */
public interface PrebuiltCompatibility extends Category {
  boolean isCompatible(@Nonnull Config config, @Nonnull String value) throws ParsingException;
}
