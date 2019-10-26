// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.commands;

import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.advancedtools.cpp.communicator.CommunicatorCommand;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * User: maxim
 * Date: 06.03.2006
 * Time: 0:48:32
 */
public class ShowPreprocessedCommand extends CommunicatorCommand {
  private final String fileName;
  private final Editor editor;
  private final int start, end;
  private String myText;
  private static final String PREPROCESSED_PREFIX = "PREPROCESSED:|";

  public ShowPreprocessedCommand(String _fileName, Editor _editor) {
    _fileName = BuildingCommandHelper.fixVirtualFileName(_fileName);
    fileName = _fileName;

    editor = _editor;
    start = editor.getSelectionModel().getSelectionStart();
    end = editor.getSelectionModel().getSelectionEnd();
  }

  public boolean doInvokeInDispatchThread() {
    return true;
  }

  public void doExecute() {
    try {
      int index = fileName.lastIndexOf('.');
      File tempFile = File.createTempFile(
        fileName.substring(fileName.lastIndexOf(File.separatorChar) + 1, index != -1 ? index:fileName.length()),
        index != -1 ? fileName.substring(index):""
      );
      String s = "//  + Preprocessed Text for " + fileName + " ("+start + "," + end + "), selection follows below:\n";
      StringTokenizer tokenizer = new StringTokenizer(editor.getDocument().getCharsSequence().subSequence(start, end).toString(), "\n");
      while (tokenizer.hasMoreElements()) {
        s += "// " + tokenizer.nextToken() + "\n";
      }
      s += myText;
      FileUtil.writeToFile(tempFile, s.getBytes());
      VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(tempFile);
      if (virtualFile != null) {
        Project project = editor.getProject();
        OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile);

        FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
      } else {
        Logger.getInstance(getClass().getName()).error(
          "Unexpected problem finding virtual file for "+tempFile.getPath());
      }
    } catch (IOException ex) {
      Logger.getInstance(getClass().getName()).error(ex);
    }
  }

  public void commandOutputString(String str) {
    if (str.startsWith(PREPROCESSED_PREFIX)) {
      myText = BuildingCommandHelper.unquote(str.substring(PREPROCESSED_PREFIX.length()));
    } else {
      myText += "\n" + BuildingCommandHelper.unquote(str);
    }
  }

  public String getCommand() {
    final String quotedFileName = BuildingCommandHelper.quote(fileName);
    return "preprocess-source " + quotedFileName + " " + start + " " + end;
  }
}
