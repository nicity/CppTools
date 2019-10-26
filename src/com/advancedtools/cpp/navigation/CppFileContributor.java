// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.navigation;

import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.commands.EnumerateSymbolsCommand;
import com.advancedtools.cpp.commands.FindSymbolsCommand;
import com.advancedtools.cpp.communicator.Communicator;
import com.advancedtools.cpp.usages.FileUsage;
import com.advancedtools.cpp.usages.OurUsage;
import com.advancedtools.cpp.utils.NavigationUtils;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author maxim
 */
public class CppFileContributor extends BaseCppSymbolContributor {
  public CppFileContributor() {
    super(FindSymbolsCommand.TargetTypes.FILES);
  }

  @Override
  protected NavigationItem createNavigatationItem(OurUsage u, String string, FileUsage fu, Project project) {
    VirtualFile virtualFile = fu.findVirtualFile();
    if (virtualFile == null) {
      new Throwable("Unsupported file path:" + fu.fileName).printStackTrace();
      return null;
    }
    return PsiManager.getInstance(project).findFile(virtualFile);
  }
}
