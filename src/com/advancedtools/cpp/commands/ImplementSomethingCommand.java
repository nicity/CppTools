// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.commands;

import com.advancedtools.cpp.communicator.CommunicatorCommand;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.intellij.openapi.editor.Editor;

/**
 * @author maxim
 * Date: 06.03.2006
 * Time: 0:48:32
 */
public class ImplementSomethingCommand extends CommunicatorCommand {
  private final String fileName;
  private final Editor editor;
  private final int start;
  private String myText;

  public ImplementSomethingCommand(String _fileName, Editor _editor) {
    _fileName = BuildingCommandHelper.fixVirtualFileName(_fileName);
    fileName = _fileName;

    editor = _editor;
    start = editor.getCaretModel().getOffset();
  }

  public boolean doInvokeInDispatchThread() {
    return true;
  }

  public void doExecute() {

  }

  public void commandOutputString(String str) {
    myText = str;
  }

  public String getCommand() {
    final String quotedFileName = BuildingCommandHelper.quote(fileName);
    return "implements " + quotedFileName + " " + start;
  }
}
