// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.communicator;

import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.hilighting.HighlightCommand;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author maxim
*/
class ServerExecutableState {
  private static final String CPP_TOOLS = "CppTools"; // plugin name
  private String myProjectName;
  private Process myProcess;
  private OutputStream myOutputStream;
  private boolean mySomethingIsVeryWrongWithTheServer;
  private BufferedReader myInputReader;
  private BufferedReader myErrReader;
  private static boolean myMadeExecutable;
  private long myLastRestartCount = -1;
  private Communicator.ServerState serverState;
  private String myBasePath;
  private static final String C_PLUGIN_INTERNAL_ERROR = "C++ Plugin Internal Error";
  private final String lineSeparator = System.getProperty("line.separator");

  void startProcess(final Communicator communicator) {
    if (mySomethingIsVeryWrongWithTheServer ||
      (serverState != Communicator.ServerState.STARTED && myLastRestartCount == communicator.getServerRestartTracker().getModificationCount())) {
      return;
    }
    final Project project = communicator.getProject();
    myProjectName = project.getName();

    final String projectModelCommandText = BuildingCommandHelper.buildPassProjectModelCommandText(getBinaryPath(), project); // invoke outside lock
    String executableFileName = null;

    synchronized(this) {
      try {
        final List<String> commands = new LinkedList<String>();
        final String platformExeSuffix =  SystemInfo.isMac ? ".mac" : SystemInfo.isLinux ? ".linux": SystemInfo.isWindows ? ".exe":"";

        executableFileName = getBinaryPath() + "cfserver" + platformExeSuffix;
        commands.add(
          executableFileName
        );

        if (!SystemInfo.isWindows && !myMadeExecutable) {
          try {
            final Process process = Runtime.getRuntime().exec(new String[]{"chmod", "+x", commands.get(0)});
            process.waitFor();
          } catch (Exception e) {
            Communicator.LOG.debug(e);
          }
          myMadeExecutable = true;
        }

        final String logDir = getBaseLogDir();
        new File(logDir).mkdirs();
        final String inputLogName = "in";
        final String outputLogName = "out";

        int index = 1;
        while(true) {
          if (new File(logDir + inputLogName + "." + index).exists()) {
            ++index;
          } else {
            break;
          }
        }

        File infile = new File(logDir + inputLogName);
        if (infile.exists()) {
          infile.renameTo(new File(logDir + inputLogName + "." + index));
          new File(logDir + outputLogName).renameTo(new File(logDir + outputLogName + "." + index));
        }

        new File(getErrorLogName()).delete();

        commands.add("--idea");
        commands.add("--catchexceptions");
        commands.add("--inLogName");
        commands.add(logDir + inputLogName);
        commands.add("--outLogName");
        commands.add(logDir + outputLogName);

        if (!SystemInfo.isWindows) {
          commands.add(0, "nice");
        }
        myProcess = Runtime.getRuntime().exec(commands.toArray(new String[commands.size()]));
        myInputReader = new BufferedReader(new InputStreamReader(myProcess.getInputStream()));
        myErrReader = new BufferedReader(new InputStreamReader(myProcess.getErrorStream()));
        myOutputStream = myProcess.getOutputStream();
        communicator.startErrorStreamReader();

        final BlockingCommand blockingCommand = new BlockingCommand() {
          {
            setDoInfiniteBlockingWithCancelledCheck(true);
          }
          public void commandOutputString(String str) {
          }

          public String getCommand() {
            return projectModelCommandText;
          }

          @Override
          public void await(Project project) {
            HighlightCommand.initQuickFixData(project);
            super.await(project);
          }
        };
        blockingCommand.post(project);
        serverState = Communicator.ServerState.STARTING;
        myLastRestartCount = communicator.getServerRestartTracker().getModificationCount();
      } catch (IOException e) {
        final CppSupportLoader loader = CppSupportLoader.getInstance(project);
        final long reportedProblemAboutServerProblemStamp = loader.getReportedProblemAboutServerProblemStamp();
        final long lastModified = new File(executableFileName).lastModified();

        if (reportedProblemAboutServerProblemStamp != lastModified) {
          loader.setReportedProblemAboutServerProblemStamp(lastModified);
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
              Messages.showWarningDialog(
                project,
                "C++ plugin encountered unexpected problem, advanced cpp editing functionality will not be available",
                C_PLUGIN_INTERNAL_ERROR
              );
            }
          });
        }

        Communicator.LOG.error(C_PLUGIN_INTERNAL_ERROR+":can not find server executable", e);
        mySomethingIsVeryWrongWithTheServer = true;
        return;
      }
    }

    communicator.communicatorStarted();
    serverState = Communicator.ServerState.STARTED;
  }

  private String getBaseLogDir() {
    return getLogPath() + myProjectName + File.separatorChar;
  }

  void restartProcess(Communicator communicator) {
    // will not block here since deadlock can be
    ApplicationManager.getApplication().invokeLater(
      new Runnable() {
        public void run() {
          ApplicationManager.getApplication().runWriteAction(
            new Runnable() {
              public void run() {
                FileDocumentManager.getInstance().saveAllDocuments();
              }
            }
          );
        }
      },
      ModalityState.defaultModalityState()
    );

    doDestroyProcess();
    if (communicator.getProject().isDisposed()) return;
    startProcess(communicator);
  }

  void doDestroyProcess() {
    synchronized(this) {
      if (myProcess != null) {
        if(isServerProcessAlive()) {
          // write can block so do it async
          Future<?> future = ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            public void run() {
              try {
                writeStringToInputStream("quit\n");
              } catch (IOException ex) {
              }
            }
          });

          try { future.get(300, TimeUnit.MILLISECONDS); }
          catch (Exception ex) {}
        }
        myProcess.destroy();
        myProcess = null;
      }
    }
  }

  private String getBasePath() {
    if (myBasePath == null) {
      String s = PathManager.getPluginsPath() + File.separatorChar + CPP_TOOLS;

      if (!new File(s).exists()) {
        s = PathManager.getPreinstalledPluginsPath() + File.separatorChar + CPP_TOOLS;

        if (!new File(s).exists()) {
          throw new RuntimeException("Plugin home is not found");
        }
      }
      myBasePath = s;
    }
    return myBasePath;
  }

  private String getBinaryPath() {
    return BuildingCommandHelper.quote(getBasePath() + File.separatorChar + "lib" + File.separatorChar, false);
  }

  private String getLogPath() {
    return BuildingCommandHelper.quote(getBasePath() + File.separatorChar + "logs" + File.separatorChar, false);
  }

  public boolean isServerProcessStarted() {
    return myProcess != null;
  }

  public boolean isServerProcessAlive() {
    if (myProcess != null) {
      try {
        myProcess.exitValue();
      } catch(IllegalThreadStateException ex) { return true; }
    }
    return false;
  }

  final String readLineFromOutputStream() throws IOException {
    return myInputReader.readLine();
  }

  final String readLineFromErrorStream() throws IOException {
    final String line = myErrReader.readLine();
    if (line != null) {
      try {
        final FileOutputStream fileOutputStream = new FileOutputStream(getErrorLogName(), true);
        fileOutputStream.write((line + lineSeparator).getBytes());
        fileOutputStream.close();
      } catch (IOException e) {
        e.printStackTrace();   // TODO
      }
    }
    return line;
  }

  private String getErrorLogName() {
    return getBaseLogDir() + "err";
  }

  final void writeStringToInputStream(@NotNull String s) throws IOException {
    myOutputStream.write(s.getBytes());
    myOutputStream.flush();
  }
}
