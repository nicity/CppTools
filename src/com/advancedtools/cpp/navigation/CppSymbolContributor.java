// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.navigation;

import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.commands.FindSymbolsCommand;
import com.advancedtools.cpp.usages.FileUsage;
import com.advancedtools.cpp.usages.OurUsage;
import com.advancedtools.cpp.utils.NavigationUtils;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author maxim
 */
public class CppSymbolContributor extends BaseCppSymbolContributor {
  public CppSymbolContributor() {
    this(FindSymbolsCommand.TargetTypes.SYMBOLS);
  }

  public CppSymbolContributor(FindSymbolsCommand.TargetTypes type) {
    super(type);
  }

  @Override
  protected NavigationItem createNavigatationItem(OurUsage u, String string, FileUsage fu, Project project) {
    return new MyNavigationItem(u, string, fu, project);
  }

  private static class MyNavigationItem implements NavigationItem {
    private final OurUsage u;
    private final FileUsage fu;
    private final Project project;
    private final String qName;

    public MyNavigationItem(OurUsage u, String navigateTo, FileUsage fu, Project project) {
      this.u = u;
      this.fu = fu;
      this.project = project;
      qName = getT(u, navigateTo);
    }

    @Nullable
    public String getName() {
      return qName;
    }

    @Nullable
    public ItemPresentation getPresentation() {
      return new ItemPresentation() {
        public String getPresentableText() {
          return qName;
        }

        @Nullable
        public String getLocationString() {
          return fu.getFileLocaton();
        }

        @Nullable
        public Icon getIcon(boolean b) {
          return CppSupportLoader.CPP_FILETYPE.getIcon();
        }

        @Nullable
        public TextAttributesKey getTextAttributesKey() {
          return null;
        }
      };
    }

    public FileStatus getFileStatus() {
      return FileStatusManager.getInstance(project).getStatus(fu.findVirtualFile());
    }

    public void navigate(boolean b) {
      NavigationUtils.doNavigate(fu, u, project);
    }

    public boolean canNavigate() {
      return true;
    }

    public boolean canNavigateToSource() {
      return true;
    }
  }

  private static String getT(OurUsage u, String navigateTo) {
    final String s = u.getContextText();
    return s.indexOf("##") >= 0 && navigateTo != null ? navigateTo : s;
  }
}
