// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.actions.generate;

/**
 * Created by IntelliJ IDEA.
 * @author maxim
 * Date: Sep 24, 2006
 * Time: 4:08:13 AM
 */
public class GenerateContructorWithParametersAction extends BaseGenerateAction {
  protected GenerateType getGenerationType() {
    return GenerateType.CONSTRUCTOR_WITH_PARAMETERS;
  }
}