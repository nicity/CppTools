package com.advancedtools.cpp.debugger.commands;

import java.io.IOException;

/**
 * @author maxim
 * Date: 29.03.2009
 * Time: 17:27:21
 */
public class ReadOutputCommand extends DebuggerCommand {
  public ReadOutputCommand() {
    super("");
  }

  @Override
  public void post(CppDebuggerContext context) {
  }

  protected boolean shouldReadTillMarker() {
    return false;
  }

  @Override
  public void readResponse(CppDebuggerContext context) throws IOException {
    if (context.getInputStream().available() == 0) {
      context.scheduleOutputReading();
      return;
    }
    super.readResponse(context);
  }

  @Override
  protected boolean processResponse(String s, CppDebuggerContext context) {
    context.scheduleOutputReading();
    return super.processResponse(s, context);
  }
}
