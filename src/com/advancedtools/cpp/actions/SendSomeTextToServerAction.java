// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.actions;

import com.advancedtools.cpp.commands.StringCommand;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author maxim
 */
public class SendSomeTextToServerAction extends BaseEditorAction {
  @Override
  public void update(AnActionEvent anActionEvent) {
    super.update(anActionEvent);
    final Presentation presentation = anActionEvent.getPresentation();

    presentation.setEnabled(true);
  }

  public void actionPerformed(AnActionEvent anActionEvent) {
    final DataContext dataContext = anActionEvent.getDataContext();
    final Project project = (Project) dataContext.getData(DataConstants.PROJECT);

    new SendDialog(project).show();
  }

  protected boolean acceptableState(Editor editor, PsiFile file) {
    return true;
  }

  static class SendDialog extends DialogWrapper {
    private JTextArea textArea1;
    private JTextArea textArea2;
    private JPanel myPanel;
    private Project myProject;

    protected SendDialog(Project project) {
      super(project, false);
      myProject = project;
      setModal(false);
      setTitle("Send Command to C++ Analyzer");
      textArea1.setText("gc-stat");
      setOKButtonText("&Send");
      init();
    }

    @Nullable
    protected JComponent createCenterPanel() {
      return myPanel;
    }

    @Override
    protected void doOKAction() {
      getOKAction().setEnabled(false);
      new StringCommand(textArea1.getText()) {
        @Override
        public void doExecute() {
          getOKAction().setEnabled(true);
        }

        @Override
        public boolean doInvokeInDispatchThread() {
          return true;
        }

        @Override
        public void doExecuteOnCancel() {
          getOKAction().setEnabled(true);
        }

        @Override
        public void commandOutputString(final String str) {
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
              textArea2.append(str + "\n");
            }
          });
        }
      }.post(myProject);
    }
  }
}