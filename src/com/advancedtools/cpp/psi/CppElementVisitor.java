// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.psi;

import com.intellij.psi.PsiElementVisitor;

/**
 * @author maxim
 */
public abstract class CppElementVisitor extends PsiElementVisitor {
  public void visitCppElement(CppElement element) {
    visitElement(element);
  }

  public void visitCppKeyword(CppKeyword keyword) {
    visitElement(keyword);
  }
}
