// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;
import java.lang.ref.Reference;

import com.intellij.CommonBundle;
import com.intellij.reference.SoftReference;

/**
 * @author maxim
 * Date: 21.09.2006
 * Time: 5:05:57
 */
public class CppBundle {
  private static Reference<ResourceBundle> ourBundle;

  @NonNls private static final String BUNDLE = "com.advancedtools.cpp.CppBundle";

  private CppBundle() {}

  public static String message(@NonNls @PropertyKey(resourceBundle = BUNDLE)String key, Object... params) {
    return CommonBundle.message(getBundle(), key, params);
  }

  private static ResourceBundle getBundle() {
    ResourceBundle bundle = null;
    if (ourBundle != null) bundle = ourBundle.get();
    if (bundle == null) {
      bundle = ResourceBundle.getBundle(BUNDLE);
      ourBundle = new SoftReference<ResourceBundle>(bundle);
    }
    return bundle;
  }
}
