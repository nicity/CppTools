// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.psi;

import com.advancedtools.cpp.CppTokenTypes;
import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.facade.EnvironmentFacade;
import com.advancedtools.cpp.commands.CompletionCommand;
import com.advancedtools.cpp.commands.NavigationCommand;
import com.advancedtools.cpp.usages.FileUsage;
import com.advancedtools.cpp.usages.OurUsage;
import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author maxim
*/
class MyPsiPolyVariantReference implements PsiPolyVariantReference {
  private final PsiElement psiElement;
  private final TextRange range;

  MyPsiPolyVariantReference(PsiElement _element) {
    this(_element, null);
  }

  MyPsiPolyVariantReference(PsiElement _element, TextRange _range) {
    psiElement = _element;
    range = _range;
  }

  public PsiElement getElement() {
    return psiElement;
  }

  public TextRange getRangeInElement() {
    return range != null ? range:new TextRange(0, psiElement.getTextLength());
  }

  @Nullable
  public PsiElement resolve() {
    final ResolveResult[] resolveResults = multiResolve(true);
    return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
  }

  public String getCanonicalText() {
    String s = psiElement.getText();
    if (range != null) s = s.substring(range.getStartOffset(), range.getEndOffset());
    return s;
  }

  public PsiElement handleElementRename(String string) throws IncorrectOperationException {
    if (range != null) {
      final String s = psiElement.getText();
      string = s.substring(0, range.getStartOffset()) + string + s.substring(range.getEndOffset());
    }
    psiElement.getNode().replaceChild(psiElement.getFirstChild().getNode(), CppElement.createNameIdentifier(psiElement.getProject(), string));
    return psiElement;
  }

  public PsiElement bindToElement(PsiElement psiElement) throws IncorrectOperationException {
    return null;
  }

  public boolean isReferenceTo(PsiElement _psiElement) {
    return psiElement == _psiElement;
  }

  public Object[] getVariants() {
    final PsiFile psiFile = psiElement.getContainingFile().getOriginalFile();
    int offset = psiElement.getText().indexOf(CompletionUtil.DUMMY_IDENTIFIER_TRIMMED);
    CompletionCommand completion = new CompletionCommand(psiFile.getVirtualFile().getPath(), psiElement.getTextOffset() + offset);
    completion.post(psiFile.getProject());

    if (!completion.hasReadyResult()) return ArrayUtil.EMPTY_OBJECT_ARRAY;

    final List<String> list = completion.getVariants();
    if (list != null) {
      Object[] result = new Object[list.size()];
      int i = 0;

      EnvironmentFacade facade = EnvironmentFacade.getInstance();
      for (String s : list) {
        result[i++] = facade.createLookupElement(s);
      }
      return result;
    }
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  public boolean isSoft() {
    return true;
  }

  @NotNull
  public ResolveResult[] multiResolve(boolean b) {
    return ResolveCache.getInstance(psiElement.getProject()).resolveWithCaching(
      this,
      CppElement.MyResolver.INSTANCE,
      false,
      false
    );
  }

  public ResolveResult[] resolveInner() {
    if (psiElement.getNode().getElementType() == CppTokenTypes.OPERATOR_KEYWORD) {
      return new ResolveResult[] {new PsiElementResolveResult(psiElement)};
    }
    final PsiFile psiFile = psiElement.getContainingFile();
    final Project project = psiFile.getProject();

    final NavigationCommand command = new NavigationCommand(psiFile, psiElement.getTextOffset() + (range != null ? range.getStartOffset():0));

    command.post(project);

    if (!command.hasReadyResult()) {
      WindowManager.getInstance().getStatusBar(project).setInfo("Command was cancelled");
      return ResolveResult.EMPTY_ARRAY;
    }

    final int count = command.getUsageCount();
    if (count == 0) return ResolveResult.EMPTY_ARRAY;
    ResolveResult[] result = new ResolveResult[count];
    int i = 0;
    for(FileUsage fu:command.getUsagesList().files) {
      final VirtualFile file = fu.findVirtualFile();
      if (file == null) {
        return ResolveResult.EMPTY_ARRAY;
      }
      final PsiFile usageFile = PsiManager.getInstance(project).findFile( file );
      assert usageFile != null : "Unexpected null psi file:" + file.getPath();

      for(final OurUsage u:fu.usageList) {
        result[i++] = new ResolveResult() {
          PsiElement element;
          @Nullable
          public PsiElement getElement() {
            if (element == null && (u.getStart() != 0 || u.getEnd() != 0)) {
              element = usageFile.findElementAt(u.getStart());
            }
            if (element != null) {
              PsiElement result = element.getParent();
              result.putUserData(CppSupportLoader.ourUsageKey, u);
              return result;
            }
            return usageFile;
          }

          public boolean isValidResult() {
            return true;
          }
        };
      }
    }
    return result;
  }
}
