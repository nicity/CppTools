package com.advancedtools.cpp.actions;

import com.advancedtools.cpp.utils.LM;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: maxim
 * Date: 20.03.2010
 * Time: 20:11:29
 */
public class AboutAction extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent anActionEvent) {
    LM.showAboutDialog(anActionEvent.getDataContext());
  }
}
