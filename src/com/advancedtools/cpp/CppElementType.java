// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.intellij.psi.tree.IElementType;

/**
 * @author maxim
 * Date: 21.09.2006
 * Time: 5:29:48
 */
public class CppElementType extends IElementType {
  public CppElementType(String s) {
    super(s, CppSupportLoader.CPP_FILETYPE.getLanguage());
  }
}
