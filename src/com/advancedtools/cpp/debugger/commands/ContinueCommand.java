package com.advancedtools.cpp.debugger.commands;

import java.io.IOException;

/**
 * @author maxim
 * Date: 29.03.2009
 * Time: 17:28:47
 */
public class ContinueCommand extends DebuggerCommand {
  public ContinueCommand() {
    super("continue");
  }

  @Override
  public void readResponse(CppDebuggerContext context) throws IOException {
  }
}
