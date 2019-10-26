// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.hilighting;

import com.advancedtools.cpp.CppSupportLoader;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author maxim
 */
public class CppErrorHighlightingHandler implements ProjectComponent, HighlightVisitor, Cloneable {
  protected final Project project;
  private boolean myReadyToHighlight;
  private HighlightInfoHolder myHighlightInfoHolder;

  public CppErrorHighlightingHandler(Project _project) {
    project = _project;
  }

  public void projectOpened() {
  }

  public void projectClosed() {
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  public boolean suitableForFile(PsiFile psiFile) {
    return psiFile.getFileType() == CppSupportLoader.CPP_FILETYPE;
  }

  public void visit(@NotNull PsiElement psiElement) {
    if (myReadyToHighlight) {
      final PsiFile psiFile = psiElement.getContainingFile();
      if (HighlightUtils.debug) {
        HighlightUtils.trace(psiFile, null, "Daemon about to start:");
      }

      final HighlightCommand command = HighlightUtils.getUpToDateHighlightCommand(psiFile, psiFile.getProject());
      command.awaitErrors(project);

      if (HighlightUtils.debug) {
        HighlightUtils.trace(psiFile, null, "Adding errors:");
      }
      command.addErrors(myHighlightInfoHolder);
//      System.out.println("Finished--:"+getClass()+"," + (System.currentTimeMillis() - command.started));
      myReadyToHighlight = false;
    }
  }

  // not null removed for reason of usage
  public boolean analyze(@NotNull PsiFile psiFile, boolean wholeFile, HighlightInfoHolder highlightInfoHolder, @NotNull Runnable runnable) {
    if (!wholeFile) {
      runnable.run();
      return true;
    }
    myReadyToHighlight = true;
    myHighlightInfoHolder = highlightInfoHolder;
    runnable.run();
    return true;
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return "Cpp.ErrorHighlightingHandler";
  }

  public void init() {
    myReadyToHighlight = true;
  }

  public HighlightVisitor clone() {
    try {
      final CppErrorHighlightingHandler visitor = (CppErrorHighlightingHandler) super.clone();
      visitor.myReadyToHighlight = true;
      visitor.myHighlightInfoHolder = null;
      return visitor;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return null;
    }
  }

  // IDEA8
  public int order() {
    return 1;
  }
}