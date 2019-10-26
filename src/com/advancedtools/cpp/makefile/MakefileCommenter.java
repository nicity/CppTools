// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.makefile;

import com.intellij.lang.Commenter;
import org.jetbrains.annotations.Nullable;

/**
* @author maxim
* Date: 2/3/12
* Time: 1:30 PM
*/
public class MakefileCommenter implements Commenter {
  @Nullable
  public String getLineCommentPrefix() {
    return "#";
  }

  @Nullable
  public String getBlockCommentPrefix() {
    return null;
  }

  @Nullable
    public String getBlockCommentSuffix() {
    return null;
  }

  public String getCommentedBlockCommentPrefix() {
    return null;
  }

  public String getCommentedBlockCommentSuffix() {
    return null;
  }
}
