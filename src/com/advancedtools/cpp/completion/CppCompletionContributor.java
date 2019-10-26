// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.completion;

import com.advancedtools.cpp.utils.LM;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.DefaultCompletionContributor;
import org.jetbrains.annotations.NotNull;

/**
* @author maximm
* Date: 2/3/12
* Time: 1:23 PM
*/
public class CppCompletionContributor extends DefaultCompletionContributor {
  @Override
  public String advertise(@NotNull CompletionParameters parameters) {
    return !LM.isRegistered()? "license owners have no advertising":super.advertise(parameters);
  }
}
