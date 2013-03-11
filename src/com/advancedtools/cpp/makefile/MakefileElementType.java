/* AdvancedTools, 2007, all rights reserved */
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