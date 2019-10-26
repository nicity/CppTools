// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
