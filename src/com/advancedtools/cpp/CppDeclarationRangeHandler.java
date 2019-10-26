// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.intellij.codeInsight.hint.DeclarationRangeHandler;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 * User: maxim
 * Date: 10.01.2009
 * Time: 19:28:37
 */
public class CppDeclarationRangeHandler implements DeclarationRangeHandler<PsiElement> {
  @NotNull
  public TextRange getDeclarationRange(@NotNull PsiElement container) {
    PsiElement e = container;
    while(e.getNode().getElementType() != CppTokenTypes.BLOCK) {
      e = e.getParent();
      if (e instanceof PsiFile) break;
    }
    return e.getTextRange();
  }
}
