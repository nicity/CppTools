/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.actions;

import com.advancedtools.cpp.navigation.CppSymbolContributor;
import com.advancedtools.cpp.CppSupportLoader;

/**
 * @author maxim
 */
public class GotoConstAction extends GotoBaseAction {
  public CppSymbolContributor getNameContributor() {
    return CppSupportLoader.getConstantContributor();
  }

  protected String getEnterTextPrefix() {
    return "Enter constant part:";
  }

  protected String getNoEntityText() {
    return "No such constant in project";
  }
}
