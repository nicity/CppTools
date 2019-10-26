// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.debugger;

import com.intellij.icons.AllIcons;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.util.containers.BidirectionalMap;
import com.advancedtools.cpp.debugger.commands.AddBreakpointCommand;
import com.advancedtools.cpp.debugger.commands.CppDebuggerContext;
import com.advancedtools.cpp.debugger.commands.RemoveBreakpointCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * @author maxim
 * Date: 30.03.2009
 * Time: 1:47:30
 */
public class CppBreakpointManager {
  private final BidirectionalMap<XLineBreakpoint, Integer> myBreakpointToIndexMap = new BidirectionalMap<XLineBreakpoint, Integer>();
  private final XBreakpointHandler<?>[] myBreakpointHandlers;
  public static final String BREAKPOINT_MARKER = "Breakpoint ";
  public static final String AT_MARKER = " at ";

  public CppBreakpointManager(final CppDebuggerContext context) {
    myBreakpointHandlers = new XBreakpointHandler<?>[] {
      new XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>>(CppBreakpointType.class) {
        @Override
        public void registerBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint) {
          context.sendCommand(new AddBreakpointCommand(breakpoint));
        }

        @Override
        public void unregisterBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint, boolean temporary) {
          Integer index = myBreakpointToIndexMap.get(breakpoint);
          if (index == null) {
            return;
          }
          context.sendCommand(new RemoveBreakpointCommand(breakpoint, index));
        }
      }
    };
  }

  public XBreakpointHandler<?>[] getBreakpointHandlers() {
    return myBreakpointHandlers;
  }

  public void registerBreakpoint(XLineBreakpoint<XBreakpointProperties> myBreakpoint, int breakpointId) {
    myBreakpointToIndexMap.put(myBreakpoint, breakpointId);
  }

  public boolean processResponseLine(String token, CppDebuggerContext context) {
    final String prefix = BREAKPOINT_MARKER;

    if (token.startsWith(prefix)) {
      // Breakpoint 1 at 0x401308: file src/untitled.c, line 3.
      int atPos = token.indexOf(AT_MARKER, prefix.length());
      if (atPos == -1) atPos = Integer.MAX_VALUE;

      //Breakpoint 1, main (argc=1, argv=0x3d3fd8 "y$=") at src/untitled.c:3
      int commaPos = token.indexOf(", ", prefix.length());
      if (commaPos == -1) commaPos = Integer.MAX_VALUE;
      // Breakpoint 1 (untitled2.c:3) pending.
      int spacePos = token.indexOf(' ', prefix.length());
      if (spacePos == -1) spacePos = Integer.MAX_VALUE;
      final int index = Math.min(Math.min(atPos, commaPos), spacePos);
      int breakpointNumber = index != Integer.MAX_VALUE ? Integer.parseInt(token.substring(prefix.length(), index)):-1;

      XLineBreakpoint lineBreakpoint = getBreakpointByIndex(breakpointNumber);

      if (lineBreakpoint != null) {
        if (atPos == index) {
          context.getSession().updateBreakpointPresentation(lineBreakpoint, AllIcons.Debugger.Db_verified_breakpoint, null);
        } else if (commaPos == index) {
          context.getSession().breakpointReached(
            lineBreakpoint,
            new CppSuspendContext(
              new CppStackFrame(token.substring(index + 2, atPos != Integer.MAX_VALUE ? atPos:token.length()), lineBreakpoint.getSourcePosition(), context, 0),
              context
            )
          );
        }
      }

      return true;
    }
    return false;
  }

  private @Nullable XLineBreakpoint getBreakpointByIndex(int breakpointNumber) {
    final List<XLineBreakpoint> breakpointList = myBreakpointToIndexMap.getKeysByValue(breakpointNumber);
    if (breakpointList != null && breakpointList.size() > 0) return breakpointList.get(0);
    return null;
  }

  public void unregisterBreakpoint(XLineBreakpoint<XBreakpointProperties> breakpoint) {
    myBreakpointToIndexMap.remove(breakpoint);
  }
}
