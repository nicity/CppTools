package com.advancedtools.cpp.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.Nullable;

/**
* User: maxim
* Date: 31.03.2010
* Time: 16:09:57
*/
public class PsiElementResolveResult implements ResolveResult {
  private final PsiElement psiElement;

  public PsiElementResolveResult(PsiElement psiElement) {
    this.psiElement = psiElement;
  }

  @Nullable
  public PsiElement getElement() {
    return psiElement;
  }

  public boolean isValidResult() {
    return true;
  }
}
