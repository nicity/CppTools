// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.commands;

import com.advancedtools.cpp.communicator.BlockingCommand;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.advancedtools.cpp.communicator.Communicator;

import java.util.LinkedList;
import java.util.List;

/**
 * @author maxim
 * Date: 02.06.2006
 * Time: 19:01:11
 */
public class CompletionCommand extends BlockingCommand {
  protected final String filePath;
  protected final long offset;
  private List<String> variants;

  public CompletionCommand(String _filePath, int _offset) {
    filePath = BuildingCommandHelper.fixVirtualFileName(_filePath);
    offset = _offset;
  }

  public void commandOutputString(String str) {
    if (!str.startsWith("NUM")) {
      final int i = str.indexOf(Communicator.DELIMITER);
      final int i2 = str.indexOf(Communicator.DELIMITER, i + 2);

      if (i2 == -1) return;

      if (variants == null) variants = new LinkedList<String>();
      variants.add(str);
    }
  }

  public String getCommand() {
    return "complete " + BuildingCommandHelper.quote(filePath) + " " + offset;
  }

  public List<String> getVariants() {
    return variants;
  }
}
