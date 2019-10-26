// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.commands;

import com.advancedtools.cpp.communicator.BlockingCommand;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.advancedtools.cpp.communicator.Communicator;
import com.intellij.openapi.editor.Editor;

/**
 * @author maxim
 * Date: 06.03.2006
 * Time: 0:48:32
 */
public class SelectWordCommand extends BlockingCommand {
  private final String fileName;
  private final int start, end;
  private int mySelectionStart = -1;
  private int mySelectionEnd = -1;

  public SelectWordCommand(String _fileName, Editor _editor) {
    _fileName = BuildingCommandHelper.fixVirtualFileName(_fileName);
    fileName = _fileName;

    start = _editor.getSelectionModel().getSelectionStart();
    end = _editor.getSelectionModel().getSelectionEnd();
  }

  public void commandOutputString(String str) {
    if (str.startsWith("AT:|")) {
      int offset = str.indexOf(Communicator.DELIMITER, 4) + 1;

      mySelectionStart = Integer.parseInt(str.substring(4, offset - 1));
      mySelectionEnd = Integer.parseInt(str.substring(offset));
    }
  }

  public String getCommand() {
    final String quotedFileName = BuildingCommandHelper.quote(fileName);
    return "select " + quotedFileName + " " + start + " " + end;
  }


  public boolean hasReadyResult() {
    return super.hasReadyResult() && mySelectionEnd != -1;
  }

  public int getSelectionStart() {
    return mySelectionStart;
  }

  public int getSelectionEnd() {
    return mySelectionEnd;
  }
}
