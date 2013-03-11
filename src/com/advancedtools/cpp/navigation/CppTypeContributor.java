package com.advancedtools.cpp.navigation;

import com.advancedtools.cpp.commands.FindSymbolsCommand;

/**
 * @author maxim
 */
public class CppTypeContributor extends CppSymbolContributor {
  public CppTypeContributor() {
    super(FindSymbolsCommand.TargetTypes.TYPES);
  }
}
