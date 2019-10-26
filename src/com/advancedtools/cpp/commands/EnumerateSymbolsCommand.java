// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.commands;

import com.advancedtools.cpp.communicator.BlockingCommand;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class EnumerateSymbolsCommand extends BlockingCommand {
  private final String prefix;
  private final FindSymbolsCommand.TargetTypes myType;
  private final Set<String> symbols = new HashSet<String>(5);
  private int totalCount;

  public EnumerateSymbolsCommand(String _prefix, FindSymbolsCommand.TargetTypes type) {
    prefix = _prefix;
    myType = type;
    setDoInfiniteBlockingWithCancelledCheck(true);
  }

  public EnumerateSymbolsCommand(FindSymbolsCommand.TargetTypes type) {
    this("", type);
  }

  public void commandOutputString(String str) {
    if (myType == FindSymbolsCommand.TargetTypes.CONSTANTS) str = str.substring(1,str.length() - 1);
    else if (myType == FindSymbolsCommand.TargetTypes.FILES) str = str.substring(str.lastIndexOf(File.separatorChar) + 1);
    symbols.add(str);
    ++totalCount;
  }

  public String getCommand() {
    final String s = BuildingCommandHelper.quote(prefix);
    StringBuilder builder = new StringBuilder();
    @NonNls String commandName = null;

    switch(myType) {
      case SYMBOLS: commandName = "enumerate-symbols"; break;
      case TYPES: commandName = "enumerate-types"; break;
      case CONSTANTS: commandName = "enumerate-strings"; break;
      case MACROS: commandName = "enumerate-macros"; break;
      case FILES: commandName = "enumerate-files"; break;
    }

    assert commandName != null;

    builder.append(commandName + " " + s);

    return builder.toString();
  }

  public Set<String> getNames() {
    if (symbols.size() != this.totalCount) {
      int a = 1;
    }
    return symbols;
  }
}
