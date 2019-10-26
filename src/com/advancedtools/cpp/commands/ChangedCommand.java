// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.commands;

import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.advancedtools.cpp.communicator.CommunicatorCommand;
import com.intellij.openapi.util.SystemInfo;

/**
 * @author maxim
 */
public class ChangedCommand extends CommunicatorCommand {
  private final int start;
  private final int end;
  private final String fileName;
  private final String change;
  private final long modificationStamp;

  public ChangedCommand(String _fileName, String _change, int _start, int _end, long _modificationStamp) {
    if (SystemInfo.isWindows && _change.indexOf('\n') != -1) {
      _change = _change.replaceAll("\n", "\r\n");
    }
    change = _change;
    fileName = BuildingCommandHelper.fixVirtualFileName(_fileName);
    start = _start;
    end = _end;
    modificationStamp = _modificationStamp;
  }

  public boolean isCancellable() {
    return false;
  }

  public void doExecute() {}
  public void commandOutputString(String str) {}

  public String getCommand() {
    return "changed -n " + modificationStamp + " " + BuildingCommandHelper.quote(fileName) + " " + BuildingCommandHelper.quote(change) + " " + start + " " + end;
  }
}
