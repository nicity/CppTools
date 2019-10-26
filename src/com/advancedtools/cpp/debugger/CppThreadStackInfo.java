// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.debugger;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.advancedtools.cpp.debugger.commands.CppDebuggerContext;
import com.advancedtools.cpp.debugger.commands.DebuggerCommand;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
* User: maxim
* Date: 22.08.2009
* Time: 19:08:11
*/
class CppThreadStackInfo extends XExecutionStack {
  private final CppSuspendContext suspendContext;
  private final XStackFrame stackFrame;
  private final CppDebuggerContext context;
  private final int threadNo;

  CppThreadStackInfo(CppSuspendContext _suspendContext, CppStackFrame stackFrame,
                             CppDebuggerContext context, String displayName, int _threadNo) {
    super(displayName);

    suspendContext = _suspendContext;
    stackFrame.setThreadStackInfo(this);
    this.stackFrame = stackFrame;
    this.context = context;
    threadNo = _threadNo;
  }

  @Override
  public XStackFrame getTopFrame() {
    return stackFrame;
  }

  @Override
  public void computeStackFrames(final int i, final XStackFrameContainer xStackFrameContainer) {
    switchToTargetThread(this);
    context.sendCommand(new DebuggerCommand("bt") {
      final List<XStackFrame> frames = new ArrayList<XStackFrame>();
      int count = 0;
      @Override
      protected void processToken(String token, CppDebuggerContext context) {
        ++count;
        if (count == i) return;
        CppStackFrame stackFrame = CppStackFrame.parseStackFrame(token, CppThreadStackInfo.this, context);
        if (stackFrame != null) frames.add(stackFrame);
      }

      @Override
      public void readResponse(CppDebuggerContext context) throws IOException {
        super.readResponse(context);
        xStackFrameContainer.addStackFrames(frames, true);
      }
    });
  }

  public static void switchToTargetThread(CppThreadStackInfo threadStackInfo) {
    if (!threadStackInfo.isActive()) {
      threadStackInfo.context.sendCommand(new DebuggerCommand("thread " + threadStackInfo.threadNo));
      threadStackInfo.suspendContext.setActiveExecutionStack(threadStackInfo);
    }
  }

  boolean isActive() {
    return suspendContext.getActiveExecutionStack() == this;
  }
}
