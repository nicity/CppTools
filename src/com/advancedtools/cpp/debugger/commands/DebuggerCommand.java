// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.debugger.commands;

import com.advancedtools.cpp.debugger.CppStackFrame;
import com.advancedtools.cpp.debugger.CppSuspendContext;
import com.intellij.execution.ui.ConsoleViewContentType;

import java.io.IOException;
import java.io.OutputStream;
import java.util.StringTokenizer;

/**
* User: maxim
* Date: 29.03.2009
* Time: 17:23:22
*/
public class DebuggerCommand {
  private String command;  

  public DebuggerCommand(String _command) {
    command = _command;
  }

  public String getCommandText() {
    return command;
  }

  public void post(CppDebuggerContext context) throws IOException {
    OutputStream outputStream = context.getOutputStream();
    String text = getCommandText();
    System.out.println("in:"+text);
    outputStream.write((text + "\n").getBytes());
    outputStream.flush();
  }

  public void readResponse(CppDebuggerContext context) throws IOException {
    while(true) {
      String s = context.readLine(shouldReadTillMarker());
      System.out.println("out:"+s);
      if (!processResponse(s, context)) return;
    }
  }

  protected boolean shouldReadTillMarker() {
    return true;
  }

  protected boolean processResponse(String s, CppDebuggerContext context) {
    if (s == null) return false;
    StringTokenizer tokenizer = new StringTokenizer(s, "\r\n");
    while(tokenizer.hasMoreElements()) {
      final String token = tokenizer.nextToken().trim();
      if (token.length() == 0) continue;

      processToken(token, context);
    }
    return false;
  }

  protected void processToken(String token, CppDebuggerContext context) {
    if(context.getBreakpointManager().processResponseLine(token, context)) return;

    final char c = token.charAt(0);
    if (Character.isDigit(c) && !context.getSession().isPaused()) {
      context.sendCommand(new DebuggerCommand("bt 1"));
    } else if (c == '#' && token.startsWith("#0")) {
      final CppStackFrame stackFrame = CppStackFrame.parseStackFrame(token, null, context);

      context.getSession().positionReached(
        new CppSuspendContext(stackFrame, context)
      );
    } else if (token.indexOf("Program exited") != -1) {
      context.printToConsole(token, ConsoleViewContentType.SYSTEM_OUTPUT);
      if (!context.getSession().isStopped()) {
        context.getProcessHandler().detachProcess();
      }
    } else if (token.indexOf("Program received signal") != -1) {
      context.printToConsole(token, ConsoleViewContentType.SYSTEM_OUTPUT);
    }
  }

}
