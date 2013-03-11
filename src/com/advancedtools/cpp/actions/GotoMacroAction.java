/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.actions;

import com.advancedtools.cpp.navigation.CppSymbolContributor;
import com.advancedtools.cpp.CppSupportLoader;

/**
 * @author maxim
 */
public class GotoMacroAction extends GotoBaseAction {
  public CppSymbolContributor getNameContributor() {
    return CppSupportLoader.getMacrosContributor();
  }

  protected String getEnterTextPrefix() {
    return "Enter macro prefix:";
  }

  protected String getNoEntityText() {
    return "No such macro in pproject";
  }
}
