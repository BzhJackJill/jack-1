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

package java.lang;

public final class Math {
  Math() {
    throw new RuntimeException("Stub!");
  }

  public static native double abs(double d);

  public static native float abs(float f);

  public static native int abs(int i);

  public static native long abs(long l);

  public static native double acos(double d);

  public static native double asin(double d);

  public static native double atan(double d);

  public static native double atan2(double y, double x);

  public static native double cbrt(double d);

  public static native double ceil(double d);

  public static native double cos(double d);

  public static native double cosh(double d);

  public static native double exp(double d);

  public static native double expm1(double d);

  public static native double floor(double d);

  public static native double hypot(double x, double y);

  public static native double IEEEremainder(double x, double y);

  public static native double log(double d);

  public static native double log10(double d);

  public static native double log1p(double d);

  public static double max(double d1, double d2) {
    throw new RuntimeException("Stub!");
  }

  public static float max(float f1, float f2) {
    throw new RuntimeException("Stub!");
  }

  public static native int max(int i1, int i2);

  public static long max(long l1, long l2) {
    throw new RuntimeException("Stub!");
  }

  public static double min(double d1, double d2) {
    throw new RuntimeException("Stub!");
  }

  public static float min(float f1, float f2) {
    throw new RuntimeException("Stub!");
  }

  public static native int min(int i1, int i2);

  public static long min(long l1, long l2) {
    throw new RuntimeException("Stub!");
  }

  public static native double pow(double x, double y);

  public static native double rint(double d);

  public static long round(double d) {
    throw new RuntimeException("Stub!");
  }

  public static int round(float f) {
    throw new RuntimeException("Stub!");
  }

  public static double signum(double d) {
    throw new RuntimeException("Stub!");
  }

  public static float signum(float f) {
    throw new RuntimeException("Stub!");
  }

  public static native double sin(double d);

  public static native double sinh(double d);

  public static native double sqrt(double d);

  public static native double tan(double d);

  public static native double tanh(double d);

  public static synchronized double random() {
    throw new RuntimeException("Stub!");
  }

  public static double toRadians(double angdeg) {
    throw new RuntimeException("Stub!");
  }

  public static double toDegrees(double angrad) {
    throw new RuntimeException("Stub!");
  }

  public static double ulp(double d) {
    throw new RuntimeException("Stub!");
  }

  public static float ulp(float f) {
    throw new RuntimeException("Stub!");
  }

  public static double copySign(double magnitude, double sign) {
    throw new RuntimeException("Stub!");
  }

  public static float copySign(float magnitude, float sign) {
    throw new RuntimeException("Stub!");
  }

  public static int getExponent(float f) {
    throw new RuntimeException("Stub!");
  }

  public static int getExponent(double d) {
    throw new RuntimeException("Stub!");
  }

  public static double nextAfter(double start, double direction) {
    throw new RuntimeException("Stub!");
  }

  public static float nextAfter(float start, double direction) {
    throw new RuntimeException("Stub!");
  }

  public static double nextUp(double d) {
    throw new RuntimeException("Stub!");
  }

  public static float nextUp(float f) {
    throw new RuntimeException("Stub!");
  }

  public static double scalb(double d, int scaleFactor) {
    throw new RuntimeException("Stub!");
  }

  public static float scalb(float d, int scaleFactor) {
    throw new RuntimeException("Stub!");
  }

  public static final double E = 2.718281828459045;
  public static final double PI = 3.141592653589793;
}
