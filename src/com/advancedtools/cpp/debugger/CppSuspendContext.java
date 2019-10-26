// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.debugger;

import com.advancedtools.cpp.debugger.commands.CppDebuggerContext;
import com.advancedtools.cpp.debugger.commands.DebuggerCommand;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;

import java.util.ArrayList;
import java.util.List;

/**
 * User: maxim
 * Date: 30.03.2009
 * Time: 0:53:59
 */
public class CppSuspendContext extends XSuspendContext {
  private CppThreadStackInfo myActiveThreadStack;
  private CppThreadStackInfo[] myThreadStacks;
  private final CppDebuggerContext myContext;

  public CppSuspendContext(final CppStackFrame stackFrame, final CppDebuggerContext context) {
    myContext = context;

    final List<CppThreadStackInfo> threads = new ArrayList<CppThreadStackInfo>();

    myContext.sendAndProcessOneCommand(new DebuggerCommand("info threads") {
      @Override
      protected void processToken(String token, CppDebuggerContext context) {
        if (token.indexOf("thread") != -1) {
          CppThreadStackInfo info;
          // "* "?6 thread 3020.0x890  0x7c90e514 in ntdll!LdrAccessResource ()   from C:\WINDOWS\system32\ntdll.dll
          boolean activeThread = token.charAt(0) == '*';
          int startOffset = activeThread ? 2 : 0;
          int threadIdEnd = token.indexOf(' ', startOffset);
          int startOfFunPos = token.indexOf(' ', token.indexOf(' ', threadIdEnd + 1) + 1);
          int threadNo = Integer.parseInt(token.substring(startOffset, threadIdEnd));
          String displayName = token.substring(startOffset, startOfFunPos);

          if (activeThread) {
            info = new CppThreadStackInfo(CppSuspendContext.this, stackFrame, context, displayName, threadNo);
            myActiveThreadStack = info;
          } else {
            CppStackFrame cppStackFrame = CppStackFrame.parseStackFrame("#0"+token.substring(startOfFunPos + 1), null, context);
            info = new CppThreadStackInfo(CppSuspendContext.this, cppStackFrame, context, displayName, threadNo);
          }
          threads.add(info);
          return;
        }
        super.processToken(token, context);
      }
    });

    myThreadStacks = threads.toArray(new CppThreadStackInfo[threads.size()]);
  }

  @Override
  public XExecutionStack getActiveExecutionStack() {
    return myActiveThreadStack;
  }

  @Override
  public XExecutionStack[] getExecutionStacks() {
    return myThreadStacks;
  }

  public void setActiveExecutionStack(CppThreadStackInfo threadStackInfo) {
    myActiveThreadStack = threadStackInfo;
  }
}
