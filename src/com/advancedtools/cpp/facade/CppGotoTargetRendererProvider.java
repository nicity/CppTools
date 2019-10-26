// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.facade;

import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.usages.OurUsage;
import com.intellij.codeInsight.navigation.GotoTargetHandler;
import com.intellij.codeInsight.navigation.GotoTargetRendererProvider;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

/**
* @author maxim
* Date: 2/3/12
* Time: 1:56 PM
*/
public class CppGotoTargetRendererProvider implements GotoTargetRendererProvider {
  protected PsiElementListCellRenderer getRendererImpl(PsiElement psiElement) {
    if (psiElement.getLanguage() != CppSupportLoader.CPP_LANGUAGE) return null;
    return new DefaultPsiElementCellRenderer() {
      @Override
      public String getElementText(PsiElement psiElement) {
        OurUsage usage = psiElement.getUserData(CppSupportLoader.ourUsageKey);
        if (usage != null) {
          String text = usage.getContextText();
          if (text != null) return text;
        }
        return super.getElementText(psiElement);
      }

      @Override
      public String getContainerText(PsiElement psiElement, String s) {
        OurUsage usage = psiElement.getUserData(CppSupportLoader.ourUsageKey);
        if (usage != null) {
          return " in " + usage.fileUsage.getFileLocaton();
        }
        return super.getContainerText(psiElement, s);
      }
    };
  }

  public PsiElementListCellRenderer getRenderer(PsiElement psiElement) {
    return getRendererImpl(psiElement);
  }

  @Nullable
  public PsiElementListCellRenderer getRenderer(PsiElement psiElement, GotoTargetHandler.GotoData gotoData) {
    return getRenderer(psiElement);
  }
}
