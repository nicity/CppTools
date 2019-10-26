// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.build;

import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: maxim
 * Date: Mar 23, 2009
 * Time: 12:54:43 PM
 */
public class BuildState {
  private final List<String> runCommand;
  private final File runCommandDir;
  private final Map<String, String> commandLineProperties;
  private Process process;

  public BuildState(List<String> _runCommand, File _runCommandDir, Map<String, String> _commandLineProperties) {
    runCommand = _runCommand;
    runCommandDir = _runCommandDir;
    commandLineProperties = _commandLineProperties;
  }

  public void start() throws IOException {
    ProcessBuilder processBuilder = new ProcessBuilder(runCommand);
    Map<String, String> environment = processBuilder.environment();
    if (commandLineProperties != null) {
      environment.clear();
      environment.putAll(commandLineProperties);
    }
    process = processBuilder.directory(runCommandDir).start();
  }

  public static void saveDocuments() {
    invokeOnEDTSynchroneously(new Runnable() {
      public void run() {
        FileDocumentManager.getInstance().saveAllDocuments();
      }
    });
  }

  public static void saveAll() {
    invokeOnEDTSynchroneously(new Runnable() {
      public void run() {
        ApplicationManager.getApplication().saveAll();
      }
    });
  }

  public static void invokeOnEDTSynchroneously(Runnable saveRunnable) {
    if (ApplicationManager.getApplication().isDispatchThread()) {
      saveRunnable.run();
    }
    else {
      ApplicationManager.getApplication().invokeAndWait(saveRunnable, ModalityState.defaultModalityState());
    }
  }

  public Process getProcess() {
    return process;
  }

  public String getRunCommand() {
    return BuildUtils.join(runCommand, " ");
  }
}
