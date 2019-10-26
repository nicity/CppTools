// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.makefile;

import com.advancedtools.cpp.CppSupportLoader;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;

/**
 * @author maxim
 * Date: 21.09.2006
 * Time: 5:29:48
 */
public class MakefileElementType extends IElementType {
  public MakefileElementType(@NonNls String s) {
    super(s, CppSupportLoader.MAKE_FILETYPE.getLanguage());
  }
}