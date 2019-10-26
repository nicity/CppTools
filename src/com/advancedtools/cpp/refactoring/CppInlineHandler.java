// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.refactoring;

import com.intellij.lang.refactoring.InlineHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.Nullable;

/**
* @author maxim
* Date: 2/3/12
* Time: 1:20 PM
*/
public class CppInlineHandler implements InlineHandler {

  @Nullable
  public Settings prepareInlineElement(PsiElement psiElement, Editor editor, boolean b) {
    return new Settings() {
      public boolean isOnlyOneReferenceToInline() {
        return true;
      }
    };
  }

  public void removeDefinition(PsiElement psiElement, Settings settings) {
    // non relevant
  }

  public void removeDefinition(PsiElement psiElement) {
    // non relevant
  }

  public Inliner createInliner(PsiElement psiElement, Settings settings) {
    return new Inliner() {

      public MultiMap<PsiElement, String> getConflicts(PsiReference psiReference, PsiElement psiElement) {
        return null;
      }

      public void inlineUsage(UsageInfo usageInfo, PsiElement psiElement) {
        final CppRefactoringSupportProvider.InlineCommand command = new CppRefactoringSupportProvider.InlineCommand(psiElement.getTextOffset(), psiElement.getContainingFile());
        command.post(psiElement.getProject());
        if (command.isFailedOrCancelled()) return;
        command.execute();
      }
    };
  }
}
