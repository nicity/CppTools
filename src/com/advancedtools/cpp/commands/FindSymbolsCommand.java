/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.commands;

import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import org.jetbrains.annotations.NonNls;

public class FindSymbolsCommand extends FindUsagesCommand {
  public enum TargetTypes {
    SYMBOLS, TYPES, MACROS, CONSTANTS, FILES
  }

  private final String prefix;
  private final TargetTypes myTargetType;

  public FindSymbolsCommand(String _prefix, TargetTypes targetType) {
    super("",0);

    prefix = _prefix;
    myTargetType = targetType;
    setDoInfiniteBlockingWithCancelledCheck(true);
  }

  public String getCommand() {
    final String s = BuildingCommandHelper.quote(prefix);
    StringBuilder builder = new StringBuilder();
    @NonNls String command = null;

    switch(myTargetType) {
      case MACROS: command = "exact-find-macros"; break;
      case TYPES: command = "exact-find-types"; break;
      case SYMBOLS: command = "exact-find-names"; break;
      case CONSTANTS: command = "find-strings"; break;
      case FILES: command = "find-files"; break;
    }

    assert command != null;
    builder.append(command + " " + s);

    return builder.toString();
  }
}
