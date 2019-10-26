// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.completion;

import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ThreeState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CppCompletionConfidence extends CompletionConfidence {
  @NotNull
  @Override
  public ThreeState shouldFocusLookup(@NotNull CompletionParameters completionParameters) {
    return ThreeState.NO;
  }

  @NotNull
  @Override
  public ThreeState shouldSkipAutopopup(@Nullable PsiElement contextElement, @NotNull PsiFile psiFile, int offset) {
    return ThreeState.YES;
  }
}
