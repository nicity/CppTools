// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.refactoring;

import com.advancedtools.cpp.actions.refactoring.ChangesSupport;
import com.advancedtools.cpp.communicator.BlockingCommand;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.advancedtools.cpp.communicator.Communicator;
import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
* @author maxim
* Date: 2/3/12
* Time: 1:13 PM
*/
public class CppRefactoringSupportProvider extends RefactoringSupportProvider {
  private static final String NAME_PLACEHOLDER = "%s";

  public boolean isSafeDeleteAvailable(PsiElement psiElement) {
    return false;
  }

  @Nullable
  public RefactoringActionHandler getIntroduceVariableHandler() {
    return new RefactoringActionHandler() {
      public void invoke(final Project project, final Editor editor, final PsiFile psiFile, DataContext dataContext) {
        final SelectionModel selectionModel = editor.getSelectionModel();
        if (!selectionModel.hasSelection()) selectionModel.selectLineAtCaret();
        if (!CommonRefactoringUtil.checkReadOnlyStatus(project, psiFile)) return;

        final int start = selectionModel.getSelectionStart();
        final int end = selectionModel.getSelectionEnd();

        final IntroduceVariableCommand introCommand = new IntroduceVariableCommand(start, end, psiFile);
        introCommand.post(project);
        if (introCommand.hasReadyResult() && introCommand.allowsValidIntroduce()) {
          final IntroduceVariableDialog dialog = new IntroduceVariableDialog(project, introCommand.myType.substring(0, introCommand.myType.indexOf(NAME_PLACEHOLDER)));
          dialog.show();

          if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            selectionModel.removeSelection();
            com.intellij.openapi.command.CommandProcessor.getInstance().executeCommand(
              project,
              new Runnable() {

                public void run() {
                  ApplicationManager.getApplication().runWriteAction(
                    new Runnable() {
                      public void run() {
                        String name = dialog.getVarName();
                        editor.getDocument().replaceString(introCommand.suggestedOffset, introCommand.suggestedOffset2, name);
                        int i = CharArrayUtil.shiftBackward(editor.getDocument().getCharsSequence(), introCommand.at - 1, " \t");
                        String prefix = editor.getDocument().getCharsSequence().subSequence(i + 1, introCommand.at).toString();
                        editor.getDocument().insertString(
                          introCommand.at,
                          introCommand.myType.replaceFirst(NAME_PLACEHOLDER, " " + name) + " = " + introCommand.myInitial + ";\n" + prefix);
                      }
                    }
                  );
                }
              },
              "Cpp.IntroduceVar",
              null
            );
          }
        } else {
          Messages.showErrorDialog(project, "Could not introduce variable", "Problem with Introduce Variable");
        }

      }

      public void invoke(Project project, PsiElement[] psiElements, DataContext dataContext) {
        throw new UnsupportedOperationException("not implemented yet");
      }
    };
  }

  @Nullable
  public RefactoringActionHandler getExtractMethodHandler() {
    return new RefactoringActionHandler() {
      public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile, DataContext dataContext) {
        final ExtractFunctionDialog dialog = new ExtractFunctionDialog(project);

        dialog.show();

        if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
          final ExtractFunctionCommand functionCommand = new ExtractFunctionCommand(
            editor.getSelectionModel().getSelectionStart(),
            editor.getSelectionModel().getSelectionEnd(),
            psiFile,
            dialog.getFunctionName()
          );
          functionCommand.post(project);
          if (functionCommand.isFailedOrCancelled()) return;

          functionCommand.changesSupport.applyChanges("Extract Function", project);
        }
      }

      public void invoke(@NotNull Project project, @NotNull PsiElement[] psiElements, DataContext dataContext) {
        throw new UnsupportedOperationException();
      }
    };
  }

  public RefactoringActionHandler getIntroduceConstantHandler() {
    return null;
  }

  public RefactoringActionHandler getIntroduceFieldHandler() {
    return null;
  }

  public boolean doInplaceRenameFor(PsiElement psiElement, PsiElement psiElement1) {
    return false;
  }

  public static class InlineCommand extends BlockingCommand {
    private int start;
    private PsiFile psiFile;
    final ChangesSupport changesSupport = new ChangesSupport();

    public InlineCommand(int start, PsiFile psiFile) {
      this.start = start;
      this.psiFile = psiFile;

      setDoInfiniteBlockingWithCancelledCheck(true);
    }

    public void commandOutputString(String str) {
      changesSupport.appendChangesFromString(str);
    }

    public String getCommand() {
      return "inline-variable " +
        BuildingCommandHelper.quote(BuildingCommandHelper.fixVirtualFileName(psiFile.getVirtualFile().getPath())) + " " +
        start;
    }

    public void execute() {
      changesSupport.applyChanges(getClass().getName(), psiFile.getProject());
    }
  }

  private static class ExtractFunctionCommand extends BlockingCommand {
    private int start;
    private int end;
    private PsiFile psiFile;
    private String functionName;
    final ChangesSupport changesSupport = new ChangesSupport();

    public ExtractFunctionCommand(int start, int end, PsiFile psiFile, String _functionName) {
      this.start = start;
      this.end = end;
      this.psiFile = psiFile;
      functionName = _functionName;
      setDoInfiniteBlockingWithCancelledCheck(true);
    }

    public void commandOutputString(String str) {
      changesSupport.appendChangesFromString(str);
    }

    public String getCommand() {
      return "extract-fn " +
        BuildingCommandHelper.quote(BuildingCommandHelper.fixVirtualFileName(psiFile.getVirtualFile().getPath())) + " " +
        start + " " + end + " " + functionName;
    }
  }

  private static class IntroduceVariableCommand extends BlockingCommand {
    private String myType;
    private String myInitial;
    private int at;
    private int suggestedOffset;
    private int suggestedOffset2;

    public static final String INTRO_PREFIX = "INTRO:|";
    private final int start;
    private final int end;
    private final PsiFile psiFile;
    private String message;

    public IntroduceVariableCommand(int start, int end, PsiFile psiFile) {
      this.start = start;
      this.end = end;
      this.psiFile = psiFile;
      setDoInfiniteBlockingWithCancelledCheck(true);
    }

    boolean allowsValidIntroduce() {
      return myInitial != null && myType != null && at != 0 && myType.indexOf("*error") == -1;
    }

    public void commandOutputString(String str) {
      if (str.startsWith(INTRO_PREFIX)) {
        int i = str.indexOf(Communicator.DELIMITER, INTRO_PREFIX.length());
        int i2 = str.indexOf(Communicator.DELIMITER, i + 1);
        int i3 = str.indexOf(Communicator.DELIMITER, i2 + 1);
        int i4 = str.indexOf(Communicator.DELIMITER, i3 + 1);
        int i5 = str.indexOf(Communicator.DELIMITER, i4 + 1);
        int i6 = str.indexOf(Communicator.DELIMITER, i5 + 1);

        at = Integer.parseInt(str.substring(i5 + 1, i6));
        suggestedOffset = Integer.parseInt(str.substring(i3 + 1, i4));
        suggestedOffset2 = Integer.parseInt(str.substring(i4 + 1, i5));

        myType = str.substring(INTRO_PREFIX.length(), i);
        myInitial = BuildingCommandHelper.unquote(str.substring(i + 1, i2));
      } else {
        message = str;
      }
    }

    public String getCommand() {
      return "introduce-variable " +
        BuildingCommandHelper.quote(BuildingCommandHelper.fixVirtualFileName(psiFile.getVirtualFile().getPath())) + " " +
        start + " " + end;
    }
  }
}
