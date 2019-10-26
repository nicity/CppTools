// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.commands;

import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NonNls;

public class NavigationCommand extends FindUsagesCommand {
  protected final Project project;

  protected NavigationCommand(Project _project, String _filePath, int _offset) {
    super(_filePath, _offset);
    project = _project;
  }

  public NavigationCommand(PsiFile file, int _offset) {
    this(file.getProject(), getPath(file), _offset);
  }

  private static String getPath(PsiFile file) {
    VirtualFile virtualFile = file.getVirtualFile();

    if (virtualFile == null && file.getOriginalFile() != null) {
      virtualFile = file.getOriginalFile().getVirtualFile();
    }
    return virtualFile != null ? virtualFile.getPath():"";
  }

  public String getCommand() {
    return getCommandText() + " " + BuildingCommandHelper.quote(filePath) + " " + offset;
  }

  protected @NonNls String getCommandText() {
    return "goto-def";
  }
}
