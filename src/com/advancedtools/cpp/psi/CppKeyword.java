// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.CppTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;

/**
 * @author maxim
*/
public class CppKeyword extends ASTWrapperPsiElement implements ICppElement {
  public CppKeyword(ASTNode node) {
    super(node);
  }

  @NotNull
  public PsiReference[] getReferences() {
    IElementType elementType = getNode().getElementType();
    if (elementType == CppTokenTypes.OPERATOR_KEYWORD) {
      return new PsiReference[] {new MyPsiPolyVariantReference(this)};
    }
    return PsiReference.EMPTY_ARRAY;
  }


  public PsiReference getReference() {
    final PsiReference[] references = getReferences();
    if (references.length > 0) return references[0];
    return null;
  }

  @NotNull
  public Language getLanguage() {
    return CppSupportLoader.CPP_FILETYPE.getLanguage();
  }

  public String toString() {
    return "CppKeyword:"+getNode().getElementType();
  }

  public void accept(@NotNull PsiElementVisitor psiElementVisitor) {
    if (psiElementVisitor instanceof CppElementVisitor) {
      ((CppElementVisitor)psiElementVisitor).visitCppKeyword(this);
    } else {
      psiElementVisitor.visitElement(this);
    }
  }

  public PsiElement setName(@NonNls String s) throws IncorrectOperationException {
    throw new IncorrectOperationException();
  }
}
