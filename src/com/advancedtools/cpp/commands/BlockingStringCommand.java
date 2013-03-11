/* AdvancedTools, 2007, all rights reserved */
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
