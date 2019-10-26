// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.hilighting;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.advancedtools.cpp.actions.refactoring.ChangesSupport;
import com.advancedtools.cpp.commands.StringCommand;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import org.jetbrains.annotations.NotNull;

/**
* User: maxim
* Date: 17.06.2008
* Time: 21:26:03
*/
public class Fix implements IntentionAction, LocalQuickFix {
  private String myName;
  private int myPos;
  private String myId;

  Fix(String _name, String _id, int pos) {
    myName = _name;
    myId = _id;
    myPos = pos;
  }

  @NotNull
  public String getText() {
    return getName();
  }

  @NotNull
  public String getFamilyName() {
    return getText();
  }

  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
    return true;
  }

  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile psiFile) throws IncorrectOperationException {
    CommandProcessor.getInstance().executeCommand(
      project,
      new Runnable() {
        public void run() {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
              doRun(project, editor, psiFile);
            }
          });
        }
      }, myName, this
    );
  }

  private void doRun(final Project project, Editor editor, PsiFile psiFile) {
    final ChangesSupport changesSupport = new ChangesSupport();
    final StringCommand stringCommand = new StringCommand(
      "quickfix " + BuildingCommandHelper.quote(myId) +
        " " + BuildingCommandHelper.quote(psiFile.getVirtualFile().getPath()) + " " + myPos) {
      public void commandOutputString(String str) {
        changesSupport.appendChangesFromString(str);
      }

      @Override
      public boolean doInvokeInDispatchThread() {
        return true;
      }

      @Override
      public void doExecute() {
        changesSupport.applyChanges(myName, project);
      }
    };
    stringCommand.post(project);
  }

  public boolean startInWriteAction() {
    return true;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
    final PsiElement psiElement = problemDescriptor.getPsiElement();
    final PsiFile psiFile = psiElement.getContainingFile();
    final Editor editor = FileEditorManager.getInstance(project).openTextEditor(
      new OpenFileDescriptor(project, psiFile.getVirtualFile(), psiElement.getTextOffset()), false
    );
    try {
      invoke(project, editor, psiFile);
    } catch (IncorrectOperationException e) {
      Logger.getInstance(getClass().getName()).error(e);
    }
  }
}
