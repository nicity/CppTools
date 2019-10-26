// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.hilighting;

import com.advancedtools.cpp.CppSupportLoader;
import com.intellij.codeHighlighting.TextEditorHighlightingPass;
import com.intellij.codeHighlighting.TextEditorHighlightingPassFactory;
import com.intellij.codeHighlighting.TextEditorHighlightingPassRegistrar;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author maxim
 */
abstract class CppHighlightingPassFactoryBase implements ProjectComponent, TextEditorHighlightingPassFactory {
  private final Project project;

  public CppHighlightingPassFactoryBase(Project _project) {
    project = _project;
  }

  public void projectOpened() {
    register(TextEditorHighlightingPassRegistrar.getInstance(project));
  }

  protected abstract void register(TextEditorHighlightingPassRegistrar instance);

  @Nullable
  public TextEditorHighlightingPass createHighlightingPass(@Nullable PsiFile file, @NotNull final Editor editor) {
    final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    if (psiFile == null || psiFile.getFileType() != CppSupportLoader.CPP_FILETYPE) return null;

    if (HighlightUtils.debug) {
      HighlightUtils.trace(psiFile, editor, "About to highlight:");
    }

    return doCreatePass(editor, psiFile, HighlightUtils.getUpToDateHighlightCommand(psiFile, project));
  }

  protected abstract TextEditorHighlightingPass doCreatePass(Editor editor, PsiFile psiFile, HighlightCommand command);

  public void projectClosed() {
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  abstract static class HighlightingPassBase extends TextEditorHighlightingPass {
    protected final HighlightCommand command;
    protected final Editor editor;
    private final PsiFile psiFile;

    public HighlightingPassBase(Editor editor, PsiFile psiFile, HighlightCommand _command) {
      super(editor.getProject(), editor.getDocument());
      this.editor = editor;
      this.psiFile = psiFile;
      command = _command;
    }

    public void doCollectInformation(ProgressIndicator progress) {
      await();
    }

    protected abstract void await();
    protected abstract Key<Long> getUpdateMarkKey();

    public void doApplyInformationToEditor() {
      if (command != null && command.isUpToDate()) {
//        if (!command.hasReadyResult()) {
//          assert false;
//          return;
//        }
        Key<Long> updateMarkKey = getUpdateMarkKey();
        Long integer = editor.getUserData(updateMarkKey);
        long count = PsiManager.getInstance(psiFile.getProject()).getModificationTracker().getModificationCount();
        if (integer != null && integer == count) {
          return;
        }

        addMarkers();
        editor.putUserData(updateMarkKey, count);

        if (HighlightUtils.debug) {
          HighlightUtils.trace(psiFile, editor, "Applied information:");
        }
      }
    }

    protected abstract void addMarkers();
  }
}