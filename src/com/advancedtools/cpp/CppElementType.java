/* AdvancedTools, 2007, all rights reserved */
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
