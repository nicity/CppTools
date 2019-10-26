// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
