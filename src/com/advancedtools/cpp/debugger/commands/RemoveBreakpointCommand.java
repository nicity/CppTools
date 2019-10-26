// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.debugger.commands;

import com.advancedtools.cpp.debugger.commands.DebuggerCommand;
import com.advancedtools.cpp.debugger.commands.CppDebuggerContext;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;

import java.io.IOException;

/**
 * @author maxim
 * Date: 30.03.2009
 * Time: 1:23:02
 */
public class RemoveBreakpointCommand extends DebuggerCommand {
  private XLineBreakpoint<XBreakpointProperties> myBreakpoint;

  public RemoveBreakpointCommand(XLineBreakpoint<XBreakpointProperties> breakpoint, int index) {
    super("delete "+index);
    myBreakpoint = breakpoint;
  }

  @Override
  public void readResponse(CppDebuggerContext context) throws IOException {
    super.readResponse(context);
    context.getBreakpointManager().unregisterBreakpoint(myBreakpoint);
  }
}
