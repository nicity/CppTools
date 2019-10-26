// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.utils;

import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.project.Project;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author maxim
*/
abstract public class IconableGutterNavigator extends GutterIconRenderer {
  private AnAction myAction;
  private final Icon myIcon;
  private final String myTooltip;

  public IconableGutterNavigator(Icon icon, String tooltip) {
    myIcon = icon;
    myTooltip = tooltip != null ? tooltip:null;
  }

  @NotNull
  public Icon getIcon() {
    return myIcon;
  }

  public boolean isNavigateAction() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof IconableGutterNavigator)) return false;
    return myTooltip.equals(((IconableGutterNavigator)o).myTooltip);
  }

  @Override
  public int hashCode() {
    return myTooltip.hashCode();
  }

  @Nullable
  public String getTooltipText() {
    return myTooltip;
  }

  @Nullable
  public AnAction getClickAction() {
    if (myAction == null) {
      myAction = new AnAction() {
        public void actionPerformed(AnActionEvent anActionEvent) {
          final Project project = (Project) anActionEvent.getDataContext().getData(DataConstants.PROJECT);
          doNavigate(project);
        }
      };
    }
    return myAction;
  }

  protected abstract void doNavigate(Project project);
}
