// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
