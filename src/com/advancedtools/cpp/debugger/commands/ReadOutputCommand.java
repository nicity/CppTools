// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
