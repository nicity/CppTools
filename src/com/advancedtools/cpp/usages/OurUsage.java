// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.usages;

/**
 * @author maxim
 * Date: 02.06.2006
 * Time: 18:38:16
 */
public class OurUsage {
  public final int start, end;
  public String text;
  public String context;
  public int line;
  public int endColumn;
  public int startColumn;
  public final FileUsage fileUsage;

  public OurUsage(int _start, int _end, FileUsage _fileUsage) {
    start = _start;
    end = _end;
    fileUsage = _fileUsage;
  }

  public int getStart() {
    return start;
  }

  public String getText() {
    if (text != null) {
      return text.substring(startColumn, endColumn);
    }
    return "!null!";
  }

  public String getContextText() {
    return context != null ? context:getText();
  }

  public int getEnd() {
    return end;
  }
}
