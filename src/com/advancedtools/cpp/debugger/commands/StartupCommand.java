package com.advancedtools.cpp.debugger.commands;

import com.advancedtools.cpp.debugger.CppBaseDebugRunner;
import com.advancedtools.cpp.run.BaseCppConfiguration;

/**
 * @author maxim
 * Date: 29.03.2009
 * Time: 17:27:21
 */
public class StartupCommand extends DebuggerCommand {
  private CppBaseDebugRunner myDebugRunner;
  private BaseCppConfiguration myRunConfiguration;

  public StartupCommand(CppBaseDebugRunner debugRunner, BaseCppConfiguration runConfiguration) {
    super("");
    myDebugRunner = debugRunner;
    myRunConfiguration = runConfiguration;
  }

  @Override
  public void post(CppDebuggerContext context) {
    String myExecutableParameters = myDebugRunner.getStartupCommandText(myRunConfiguration);
    context.sendCommand(new TextDebuggerCommand(myExecutableParameters));
    context.sendCommand(new DebuggerCommand("set breakpoint pending on"));
    context.sendCommand(new DebuggerCommand("set confirm off"));
  }

  protected boolean processResponse(String s, CppDebuggerContext context) {
    return super.processResponse(s, context);
  }
}
