/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.commands;

import com.advancedtools.cpp.communicator.CommunicatorCommand;

/**
 * @author maxim
 * Date: 05.03.2006
 * Time: 13:32:58
 */
public class StringCommand extends CommunicatorCommand {
  private String myCommandText;

  public StringCommand(String commandText) { myCommandText = commandText; }

  public void doExecute() {}
  public void commandOutputString(String str) {}
  public String getCommand() { return myCommandText; }
}
