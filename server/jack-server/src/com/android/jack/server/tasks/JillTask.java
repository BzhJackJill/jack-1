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

package com.android.jack.server.tasks;

import com.android.jack.server.JackHttpServer;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

import javax.annotation.Nonnull;

/**
 * Service task: Execute one Jack command.
 */
public class JillTask extends SynchronousServiceTask {

  public JillTask(@Nonnull JackHttpServer jackServer) {
    super(jackServer);
  }

  @Override
  protected void handle(long taskId, @Nonnull Request request, @Nonnull Response response) {
    response.setContentLength(0);
    response.setStatus(Status.NOT_IMPLEMENTED);
  }
}