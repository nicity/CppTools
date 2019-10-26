// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.debugger;

import com.advancedtools.cpp.run.BaseCppConfiguration;
import com.advancedtools.cpp.run.CppRunConfiguration;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * User: maxim
 * Date: 28.03.2009
 * Time: 19:35:05
 */
public class CppDebugRunner extends CppBaseDebugRunner<CppRunConfiguration> {
  @NotNull
  public String getRunnerId() {
    return "CppDebugRunner";
  }

  protected boolean isSuitableConfiguration(BaseCppConfiguration configuration) {
    return configuration instanceof CppRunConfiguration;
  }

  public String getWorkingDirectory(CppRunConfiguration runConfiguration) {
    return runConfiguration.getRunnerParameters().getWorkingDir();
  }

  @Override
  public String getStartupCommandText(CppRunConfiguration runConfiguration) {
    String executableParameters = runConfiguration.getRunnerParameters().getExecutableParameters();
    String result = "";

    if (!BaseCppConfiguration.isEmpty(executableParameters)) {
      result += "set args "+executableParameters;
    }

    if (CppDebuggerConstants.gdbCanNotRedirectOutputOfDebuggedCommand) {
      if (result.length() > 0) result += "\n";
      result += "set new-console";
    }

    return result;
  }

  @Override
  public String getQuitCommandText(CppRunConfiguration runConfiguration) {
    return "quit\ny";
  }

  @Override
  public String getRunCommandText(CppRunConfiguration configuration, CppDebugProcess<CppRunConfiguration> cppDebugProcess) {
    String runCommand = "run";

    if (!CppDebuggerConstants.gdbCanNotRedirectOutputOfDebuggedCommand) {
      File outFile;
      File errFile;

      try {
        // TODO: output redirecting from parameters!
        outFile = File.createTempFile("out", "");
        errFile = File.createTempFile("err", "");

        ReadFileTask task = new ReadFileTask(outFile, ConsoleViewContentType.NORMAL_OUTPUT, cppDebugProcess);

        ApplicationManager.getApplication().executeOnPooledThread(task);
        ReadFileTask task2 = new ReadFileTask(errFile, ConsoleViewContentType.ERROR_OUTPUT, cppDebugProcess);
        ApplicationManager.getApplication().executeOnPooledThread(task2);

        String executableParameters = configuration.getRunnerParameters().getExecutableParameters();
        if (!CppRunConfiguration.isEmpty(executableParameters)) runCommand += " " + executableParameters;
        
        runCommand += " >" + outFile.getPath() + " 2>" + errFile.getPath();
      } catch (IOException ex) {
        // TODO:
        ex.printStackTrace();
      }
    }

    return runCommand;
  }

  private static class ReadFileTask implements Runnable {
    private final ConsoleViewContentType contentType;
    private final RandomAccessFile reader;
    private final byte[] buffer = new byte[8192];
    private int position;
    private boolean closed;
    private final CppDebugProcess<CppRunConfiguration> cppDebugProcess;

    public ReadFileTask(File _file, ConsoleViewContentType _contentType,
                        CppDebugProcess<CppRunConfiguration> _cppDebugProcess) throws IOException {
      contentType = _contentType;
      reader = new RandomAccessFile(_file, "r") {
        @Override
        public void close() throws IOException {
          super.close();
          closed = true;
        }
      };

      cppDebugProcess = _cppDebugProcess;
      cppDebugProcess.addCloseableOnDebuggingEnd(reader);
    }

    public void run() {
      try {
        while(true) {
          while (reader.length() == position) {
            synchronized (this) {
              wait(300);
            }
            if (closed) {
              return;
            }
          }

          int read = reader.read(buffer);
          if (read == -1) break;

          cppDebugProcess.printToConsole(new String(buffer, 0, read), contentType); // TODO: byte to char conversion
          position += read;
        }
      } catch (IOException ex) {
        ex.printStackTrace();
      } catch (InterruptedException ex) {}
    }

    public void close() throws IOException {
      reader.close();
    }
  }

}
