package com.advancedtools.cpp.debugger.commands;

/**
 * @author maxim
 * Date: Apr 5, 2009
 * Time: 12:57:16 PM
 */
public class TextDebuggerCommand extends DebuggerCommand {
  private int expectedResponseCount = 1;
  private int responseCount;

  public TextDebuggerCommand(String executableParameters) {
    super(executableParameters);

    int ptr = -1;
    while((ptr = executableParameters.indexOf('\n', ptr + 1)) != -1) {
      ++expectedResponseCount;
    }
  }

  @Override
  protected boolean processResponse(String s, CppDebuggerContext context) {
    super.processResponse(s, context);
    ++responseCount;
    return responseCount != expectedResponseCount;
  }
}
