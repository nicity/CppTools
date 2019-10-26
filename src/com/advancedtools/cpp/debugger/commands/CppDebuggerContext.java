// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.debugger.commands;

import com.advancedtools.cpp.debugger.CppBreakpointManager;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleViewContentType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;

/**
 * User: maxim
 * Date: 29.03.2009
 * Time: 17:24:00
 */
public interface CppDebuggerContext {
  OutputStream getOutputStream();

  InputStream getInputStream();
  
  InputStreamReader getInputReader();

  CppBreakpointManager getBreakpointManager();

  void scheduleOutputReading();

  void sendCommand(DebuggerCommand command);
  void sendAndProcessOneCommand(DebuggerCommand command);

  XDebugSession getSession();

  ProcessHandler getProcessHandler();

  String readLine(boolean b) throws IOException;

  void printToConsole(String s, ConsoleViewContentType contentType);
}
