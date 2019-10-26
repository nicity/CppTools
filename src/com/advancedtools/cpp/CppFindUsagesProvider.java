// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.lexer.FlexAdapter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
* @author maxim
*/
public class CppFindUsagesProvider implements FindUsagesProvider {
  @Nullable
  public WordsScanner getWordsScanner() {
    return new DefaultWordsScanner(
      new FlexAdapter(new _CppLexer(true, false, true, true, true)), // TODO: c/c++ dialects
      TokenSet.create(CppTokenTypes.IDENTIFIER),
      CppTokenTypes.COMMENTS,
      TokenSet.create(CppTokenTypes.STRING_LITERAL)
    );
  }

  public boolean canFindUsagesFor(PsiElement psiElement) {
    return true;
  }

  @Nullable
  public String getHelpId(PsiElement psiElement) {
    return null;
  }

  @NotNull
  public String getType(PsiElement psiElement) {
    return "C/C++ Symbol";
  }

  @NotNull
  public String getDescriptiveName(PsiElement psiElement) {
    if (psiElement instanceof PsiNamedElement) {
      final String name = ((PsiNamedElement) psiElement).getName();

      return name != null ? name:"";
    }
    
    return psiElement.getText();
  }

  @NotNull
  public String getNodeText(PsiElement psiElement, boolean b) {
    return getDescriptiveName(psiElement);
  }
}
