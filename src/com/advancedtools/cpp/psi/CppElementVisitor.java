/* AdvancedTools, 2007, all rights reserved */
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
