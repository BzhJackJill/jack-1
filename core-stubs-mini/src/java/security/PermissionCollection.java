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

package java.security;

public abstract class PermissionCollection implements java.io.Serializable {
  public PermissionCollection() {
    throw new RuntimeException("Stub!");
  }

  public abstract void add(java.security.Permission permission);

  public abstract java.util.Enumeration<java.security.Permission> elements();

  public abstract boolean implies(java.security.Permission permission);

  public boolean isReadOnly() {
    throw new RuntimeException("Stub!");
  }

  public void setReadOnly() {
    throw new RuntimeException("Stub!");
  }
}
