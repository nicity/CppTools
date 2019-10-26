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
abstract class BaseCppSymbolContributor implements ChooseByNameContributor {
  private FindSymbolsCommand.TargetTypes myType;
  static final NavigationItem[] EMPTY_NAVIGATION_ITEMS = new NavigationItem[0];

  static class PerProjectData {
    Set<String> mySymbols;
    long modificationCount;
  }

  private Map<Project,PerProjectData> myProject2Symbols = new HashMap<Project, PerProjectData>();

  protected BaseCppSymbolContributor(FindSymbolsCommand.TargetTypes type) {
    myType = type;
  }

  public String[] getNames(Project project, boolean b) {
    if (!Communicator.getInstance(project).isServerUpAndRunning()) {
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
    PerProjectData data = myProject2Symbols.get(project);

    if (data == null ||
        data.modificationCount != Communicator.getInstance(project).getModificationCount()) {
      if (data == null) {
        data = new PerProjectData();
        data.mySymbols = Collections.emptySet();
        myProject2Symbols.put(project, data);
      }
      final EnumerateSymbolsCommand symbolsCommand = new EnumerateSymbolsCommand(myType);
      symbolsCommand.post(project);
      if (!symbolsCommand.hasReadyResult()) {
        data.mySymbols = Collections.emptySet();
      } else {
        data.modificationCount = Communicator.getInstance(project).getModificationCount();
        data.mySymbols = symbolsCommand.getNames();
      }
    }

    return data.mySymbols.toArray(new String[data.mySymbols.size()]);
  }

  // IDEA8
  public NavigationItem[] getItemsByName(String s, String s1, Project project, boolean b) {
    return getItemsByName(s, project, b);
  }

  public NavigationItem[] getItemsByName(String string, final Project project, boolean b) {
    if (string.length() == 0 ||
        !Communicator.getInstance(project).isServerUpAndRunning()
       ) {
      return EMPTY_NAVIGATION_ITEMS;
    }
    
    PerProjectData data = myProject2Symbols.get(project);
    if (data == null || !(data.mySymbols.contains(string))) return EMPTY_NAVIGATION_ITEMS;
    final FindSymbolsCommand symbolsCommand = new FindSymbolsCommand(string, myType);
    symbolsCommand.post(project);

    final int usageCount = symbolsCommand.getUsageCount();
    
    if (usageCount > 0) {
      NavigationItem[] items = new NavigationItem[usageCount];
      int i = 0;

      for(final FileUsage fu:symbolsCommand.getUsagesList().files) {
        for(final OurUsage u:fu.usageList) {
          items[i++] = createNavigatationItem(u, string, fu, project);
        }
      }

      return items;
    } else {
      return EMPTY_NAVIGATION_ITEMS;
    }
  }

  protected abstract NavigationItem createNavigatationItem(OurUsage u, String string, FileUsage fu, Project project);
}
