// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.debugger.commands;

import com.advancedtools.cpp.debugger.CppBaseDebugRunner;
import com.advancedtools.cpp.run.BaseCppConfiguration;

/**
 * @author maxim
 * Date: 29.03.2009
 * Time: 20:26:29
 */
public class QuitCommand extends DebuggerCommand {
  private CppBaseDebugRunner myDebugRunner;
  private BaseCppConfiguration myRunConfiguration;

  public QuitCommand(CppBaseDebugRunner debugRunner, BaseCppConfiguration runConfiguration) {
    super("");
    myDebugRunner = debugRunner;
    myRunConfiguration = runConfiguration;
  }

  @Override
  public String getCommandText() {
    return myDebugRunner.getQuitCommandText(myRunConfiguration);
  }
}
