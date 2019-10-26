// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
