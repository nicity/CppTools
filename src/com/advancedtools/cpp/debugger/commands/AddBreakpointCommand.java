// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.debugger.commands;

import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.XSourcePosition;
import com.advancedtools.cpp.debugger.CppBreakpointManager;

/**
 * @author maxim
 * Date: 29.03.2009
 * Time: 17:28:14
 */
public class AddBreakpointCommand extends DebuggerCommand {
  private final XLineBreakpoint<XBreakpointProperties> myBreakpoint;

  public AddBreakpointCommand(XLineBreakpoint<XBreakpointProperties> breakpoint) {
    super("break " + fileName(breakpoint) + ":" + (breakpoint.getLine() + 1));
    myBreakpoint = breakpoint;
  }

  private static String fileName(XLineBreakpoint<XBreakpointProperties> breakpoint) {
    XSourcePosition sourcePosition = breakpoint.getSourcePosition();

    return sourcePosition != null ? sourcePosition.getFile().getName():"unknown.cpp";
  }

  protected boolean processResponse(String s, CppDebuggerContext context) {
    String marker = CppBreakpointManager.BREAKPOINT_MARKER;
    if (s.startsWith(marker)) {   // TODO: setting breakpoint can cause several ones to appear
      int breakpointId = Integer.parseInt(s.substring(marker.length(), s.indexOf(' ', marker.length())));
      context.getBreakpointManager().registerBreakpoint(myBreakpoint, breakpointId);
    }
    return super.processResponse(s, context);
  }
}
