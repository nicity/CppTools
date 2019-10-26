// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.commands;

import com.advancedtools.cpp.communicator.BlockingCommand;

/**
 * @author maxim
 */
public class BlockingStringCommand extends BlockingCommand {
  private String myCommandText;
  private String myCommandResult;

  public BlockingStringCommand(String commandText) { myCommandText = commandText; }

  public void commandOutputString(String str) {
    if (myCommandResult == null) myCommandResult = str;
    else myCommandResult += str;
  }

  public String getCommand() { return myCommandText; }

  public String getCommandResult() {
    return myCommandResult;
  }
}
