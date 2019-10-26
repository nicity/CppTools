// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.debugger;

import com.advancedtools.cpp.CppBundle;
import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.CppSupportSettings;
import com.advancedtools.cpp.actions.CompileCppAction;
import com.advancedtools.cpp.build.BuildUtils;
import com.advancedtools.cpp.debugger.commands.*;
import com.advancedtools.cpp.debugger.remote.CppRemoteDebugParameters;
import com.advancedtools.cpp.facade.CppCodeFragment;
import com.advancedtools.cpp.psi.ICppCodeFragment;
import com.advancedtools.cpp.run.BaseCppConfiguration;
import com.advancedtools.cpp.run.BaseCppRunnerParameters;
import com.advancedtools.cpp.run.CppRunnerParameters;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.Alarm;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * User: maxim
 * Date: 28.03.2009
 * Time: 19:44:59
 */
public class CppDebugProcess<T extends BaseCppConfiguration> extends XDebugProcess {
  private final Process process;
  private final Alarm myOutputReadingAlarm;
  private final List<Closeable> myStreamsToClose = new ArrayList<Closeable>();
  private final LinkedBlockingDeque<DebuggerCommand> commandsToWrite = new LinkedBlockingDeque<DebuggerCommand>();

  private final CppDebuggerContext context;
  private ConsoleView myConsoleView;
  private final CppBreakpointManager myBreakpointManager;
  private CppBaseDebugRunner myRunner;
  private BaseCppConfiguration myConfiguration;
  private String commandLine = "";

  public CppDebugProcess(XDebugSession session, CppBaseDebugRunner<T> debugRunner, T runConfiguration) {
    super(session);

    BaseCppRunnerParameters cppRunnerParameters = runConfiguration.getRunnerParameters();
    List<String> commands = new ArrayList<String>();
    commands.add("--interpreter=mi");
    String toolCommandLine = CompileCppAction.getEscapedPathToFile(cppRunnerParameters.getExecutableName());
    commands.add(toolCommandLine);

    List<String> command = BuildUtils.buildGccToolCall(
      CppSupportSettings.getInstance().getGdbPath(),
      Arrays.asList(toolCommandLine)
    );

    myRunner = debugRunner;
    myConfiguration = runConfiguration;

    try {
      String path = debugRunner.getWorkingDirectory(runConfiguration);
      File dir = BaseCppConfiguration.isEmpty(path) ? null : new File(path);
      ProcessBuilder processBuilder = new ProcessBuilder(command);
      processBuilder.directory(dir);
      processBuilder.redirectErrorStream(true); // important for command request / response logic
      process = processBuilder.start();
      if (dir != null) commandLine += dir.getPath() + " > ";
    } catch (IOException ex) {
      Messages.showErrorDialog(getSession().getProject(), ex.getMessage(), CppBundle.message("cpp.debugger.startup.error"));

      throw new RuntimeException(ex);
    }

    commandLine += toolCommandLine;

    if (cppRunnerParameters instanceof CppRunnerParameters) {
      commandLine += " " + ((CppRunnerParameters)cppRunnerParameters).getExecutableParameters();
    } else {
      CppRemoteDebugParameters remoteParameters = (CppRemoteDebugParameters) cppRunnerParameters;
      if (!BaseCppConfiguration.isEmpty(remoteParameters.getHost())) {
        commandLine += "@" + remoteParameters.getHost() + ":" + remoteParameters.getPort();
      }
      commandLine += "#" + remoteParameters.getPid();
    }

    final OutputStream outputStream = process.getOutputStream();
    final InputStream inputStream = process.getInputStream();
    final InputStreamReader numberReader = new InputStreamReader(inputStream);

    myStreamsToClose.add(numberReader);
    myStreamsToClose.add(outputStream);

    context = new CppDebuggerContext() {
      public OutputStream getOutputStream() {
        return outputStream;
      }

      public InputStream getInputStream() {
        return inputStream;
      }

      public InputStreamReader getInputReader() {
        return numberReader;
      }

      public CppBreakpointManager getBreakpointManager() {
        return myBreakpointManager;
      }

      public void scheduleOutputReading() {
        myOutputReadingAlarm.addRequest(new Runnable() {
          public void run() {
            myOutputReadingAlarm.cancelAllRequests();
            try {
              if (inputStream.available() > 0) {
                sendCommand(new ReadOutputCommand());
              } else {
                myOutputReadingAlarm.addRequest(this, 100);
              }
            } catch (IOException ex) {
              // exits
            }
          }
        }, 100);
      }

      public void sendCommand(DebuggerCommand command) {
        CppDebugProcess.this.sendCommand(command);
      }

      public void sendAndProcessOneCommand(final DebuggerCommand command) {
        final DebuggerCommand delegatingCommand = new DebuggerCommand(command.getCommandText()) {
          @Override
          public String getCommandText() {
            return command.getCommandText();
          }

          @Override
          public void post(CppDebuggerContext context) throws IOException {
            command.post(context);
          }

          @Override
          public void readResponse(CppDebuggerContext context) throws IOException {
            command.readResponse(context);
          }
        };

        commandsToWrite.addFirst(delegatingCommand);
        try {
          processOneCommand();
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }

      public XDebugSession getSession() {
        return CppDebugProcess.this.getSession();
      }

      public ProcessHandler getProcessHandler() {
        return CppDebugProcess.this.getProcessHandler();
      }

      private StringBuilder builder = new StringBuilder();
      private final char[] buf = new char[8192];
      private static final String GDB_MARKER = "(gdb) ";
      private String myPreviousOutput;

      public String readLine(boolean tillMarker) throws IOException {
        String res = doRead(tillMarker);

        if (res != null) {
          builder.setLength(0);
          StringTokenizer tokenizer = new StringTokenizer(res, "\r\n");
          int start = 0;
          String defaultDelim = SystemInfo.isWindows ? "\r\n" : "\n";

          while(tokenizer.hasMoreElements()) {
            String next = tokenizer.nextToken();
            String delim = builder.length() == 0 ? "":defaultDelim;

            if (start > 0) {
              if (( next.length() > 2 &&  // detecting previous line carry over
                    next.charAt(0) == ' ' &&
                    next.charAt(1) == ' ' &&
                    next.charAt(2) == ' '
                  ) ||
                  ( next.length() > 0 && next.charAt(0) == '}')
              ) {
                delim = "";
              }
              else {
                start = 0;
              }
            }

            builder.append(delim);
            builder.append(next);
            ++start;
          }

          res = builder.toString();
        }
        return res;
      }

      private String doRead(boolean tillMarker) throws IOException {
        builder.setLength(0);

        if (myPreviousOutput != null) {
          int index = myPreviousOutput.indexOf(GDB_MARKER);

          if (index != -1) {
            String ret = myPreviousOutput.substring(0, index);
            myPreviousOutput = myPreviousOutput.substring(index + GDB_MARKER.length());
            if (myPreviousOutput.length() == 0) myPreviousOutput = null;
            return ret;
          } else {
            builder.append(myPreviousOutput);
            myPreviousOutput = null;
          }
        }

        InputStreamReader reader = context.getInputReader();

        while (true) {
          int read = reader.read(buf);
          if (read == -1) break;
          int builderLength = builder.length();
          builder.append(buf, 0, read);

          if (tillMarker) {
            int index = builder.indexOf(GDB_MARKER, builderLength);
            if (index != -1) {
              myPreviousOutput = builder.substring(index + GDB_MARKER.length());
              if (myPreviousOutput.length() == 0) myPreviousOutput = null;

              return builder.substring(0, index);
            }
          } else {
            return builder.toString();
          }

        }

        return null;
      }

      public void printToConsole(String s, ConsoleViewContentType contentType) {
        CppDebugProcess.this.printToConsole(s + "\n", contentType);
      }
    };

    myBreakpointManager = new CppBreakpointManager(context);

    sendCommand(new StartupCommand(debugRunner, runConfiguration));

    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        public void run() {
          try {
            while(true) {
              if (processOneCommand()) break;
            }
          } catch (IOException ex) {
            ex.printStackTrace();
          }

          closeStreams();
        }
    });

    myOutputReadingAlarm = new Alarm(Alarm.ThreadToUse.OWN_THREAD, getSession().getProject());
  }

  private boolean processOneCommand() throws IOException {
    final DebuggerCommand command = commandsToWrite.removeFirst();
    command.post(context);
    if (command instanceof QuitCommand) return true;
    command.readResponse(context);
    return false;
  }

  private void closeStreams() {
    for(Closeable c:myStreamsToClose) {
      try {
        c.close();
      } catch (IOException ex) {}
    }
  }

  @Override
  public void sessionInitialized() {
    super.sessionInitialized();

    myConsoleView = (ConsoleView)getSession().getRunContentDescriptor().getExecutionConsole();

    sendCommand(new DebuggerCommand(myRunner.getRunCommandText(myConfiguration, this)));

    context.scheduleOutputReading();
    printToConsole(commandLine + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
//    getSession().setPauseActionSupported(true); // TODO:
  }

  void sendCommand(DebuggerCommand command) {
    commandsToWrite.addLast(command);
  }

  @Override
  public XDebuggerEditorsProvider getEditorsProvider() {
    return new XDebuggerEditorsProvider() {
      @NotNull
      @Override
      public FileType getFileType() {
        return CppSupportLoader.CPP_FILETYPE;
      }

      @NotNull
      @Override
      public Document createDocument(@NotNull Project project, @NotNull String s, @Nullable XSourcePosition xSourcePosition, @NotNull EvaluationMode evaluationMode) {
        VirtualFile virtualFile = new LightVirtualFile("dummy.cpp", s);
        ICppCodeFragment file = new CppCodeFragment(new SingleRootFileViewProvider(PsiManager.getInstance(project), virtualFile, true));
        return PsiDocumentManager.getInstance(project).getDocument(file);
      }
    };
  }

  public void startStepOver() {
    commandsToWrite.addLast(new DebuggerCommand("next"));
  }

  public void startStepInto() {
    commandsToWrite.addLast(new DebuggerCommand("step"));
  }

  public void startStepOut() {
    commandsToWrite.addLast(new DebuggerCommand("finish"));
  }

  public void stop() {
    sendCommand(new QuitCommand(myRunner, myConfiguration));
  }

  public void resume() {
    sendCommand(new ContinueCommand());
  }

  @Override
  public void startPausing() {
// BuildUtils.buildGccToolCall("sh", "kill -s SIGINT "+ sessionData.getPid())
//    sendCommand(new DebuggerCommand(""));
  }

  public void runToPosition(@NotNull XSourcePosition position) {
  }

  @Override
  public XBreakpointHandler<?>[] getBreakpointHandlers() {
    return myBreakpointManager.getBreakpointHandlers();
  }

  public void printToConsole(String s, ConsoleViewContentType contentType) {
    myConsoleView.print(s, contentType);
  }

  public void addCloseableOnDebuggingEnd(Closeable file) {
    myStreamsToClose.add(file);
  }
}
