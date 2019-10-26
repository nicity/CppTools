// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.makefile;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.lexer.FlexAdapter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

/**
* @author maxim
* Date: 2/3/12
* Time: 1:34 PM
*/
public class MakefileFindUsagesProvider implements FindUsagesProvider {
  public WordsScanner getWordsScanner() {
    return new DefaultWordsScanner(
      new FlexAdapter(new _MakefileLexer(true)),
      TokenSet.create(MakefileTokenTypes.IDENTIFIER, MakefileTokenTypes.TARGET_IDENTIFIER,
        MakefileTokenTypes.VAR_DEFINITION, MakefileTokenTypes.VAR_REFERENCE),
      MakefileTokenTypes.COMMENTS,
      MakefileTokenTypes.LITERALS
    );
  }

  public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
    return MakefileIdentifierReference.isSelfReferenceType(MakefileIdentifierReference.type(psiElement));
  }

  public String getHelpId(@NotNull PsiElement psiElement) {
    return null;
  }

  @NotNull
  public String getType(@NotNull PsiElement psiElement) {
    IElementType iElementType = MakefileIdentifierReference.type(psiElement);
    if(iElementType == MakefileTokenTypes.VAR_DEFINITION) return "definition";
    if(iElementType == MakefileTokenTypes.TARGET_IDENTIFIER) return "target";
    return "should not happen type";
  }

  @NotNull
  public String getDescriptiveName(@NotNull PsiElement psiElement) {
    return psiElement.getText();
  }

  @NotNull
  public String getNodeText(@NotNull PsiElement psiElement, boolean b) {
    return getDescriptiveName(psiElement);
  }
}
