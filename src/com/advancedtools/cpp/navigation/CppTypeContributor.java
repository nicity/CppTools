// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
