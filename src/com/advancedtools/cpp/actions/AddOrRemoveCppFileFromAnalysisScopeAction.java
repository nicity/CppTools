// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.actions;

import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.communicator.Communicator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.util.Set;

/**
 * @author maxim
 */
public class AddOrRemoveCppFileFromAnalysisScopeAction extends AnAction {
  public void actionPerformed(AnActionEvent anActionEvent) {
    final VirtualFile file = (VirtualFile) anActionEvent.getDataContext().getData(DataConstants.VIRTUAL_FILE);
    final Project project = (Project) anActionEvent.getDataContext().getData(DataConstants.PROJECT);
    assert file != null;
    assert project != null;

    final Set<String> strings = CppSupportLoader.getInstance(project).getIgnoredFilesSet();
    final String path = file.getPresentableUrl().replace(File.separatorChar, '/');
    if (!strings.contains(path)) strings.add(path);
    else strings.remove(path);
    Communicator.getInstance(project).restartServer();
  }

  public void update(AnActionEvent anActionEvent) {
    final VirtualFile file = (VirtualFile) anActionEvent.getDataContext().getData(DataConstants.VIRTUAL_FILE);
    final Project project = (Project) anActionEvent.getDataContext().getData(DataConstants.PROJECT);
    final Presentation presentation = anActionEvent.getPresentation();

    if (project == null ||
        file == null ||
        file.getFileType() != CppSupportLoader.CPP_FILETYPE ||
        Communicator.isHeaderFile(file)
       ) {
      presentation.setVisible(false);
    } else {
      String url = file.getPresentableUrl().replace(File.separatorChar,'/');
      final Set<String> ignoredFiles = CppSupportLoader.getInstance(project).getIgnoredFilesSet();
      presentation.setVisible(true);

      final String fileName = file.getName();
      final String term = "Cpp Support Analysis Scope";
      presentation.setText(ignoredFiles.contains(url) ? "Add " + fileName + " to "+ term :"Remove " + fileName + " from " + term);
    }
  }
}
