// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.communicator;

import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.facade.EnvironmentFacade;
import com.advancedtools.cpp.commands.StringCommand;
import com.intellij.ProjectTopics;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author maxim
 */
public class Communicator implements ProjectComponent {
  // In log4.xml add
  // <category name="cppsupport.communicator">
  //   <priority value="DEBUG"/>
  //   <appender-ref ref="CONSOLE-ALL"/>
  // </category>
  static final Logger LOG = Logger.getInstance("cppsupport.communicator");
  public static final boolean isDebugEnabled = LOG.isDebugEnabled();

  private static Communicator instance;

  private Project myProject;
  private Runnable myIdleListener;

  private ModuleRootListener myRootListener;
  private MessageBusConnection myConnection;
  private long myServerRestartCount;
  private volatile int myServerInternalRestartCount;

  @NonNls
  public static final String DSP_EXTENSION = "dsp";
  @NonNls
  public static final String MAK_EXTENSION = "mak";
  @NonNls
  public static final String DSW_EXTENSION = "dsw";
  @NonNls
  public static final String VCPROJ_EXTENSION = "vcproj";
  @NonNls
  public static final String SLN_EXTENSION = "sln";
  @NonNls
  public static final String MAKEFILE_FILE_NAME = "makefile";
  @NonNls
  static final String PATH_DELIM = ";";

  private ModificationTracker myServerRestartTracker = new ModificationTracker() {
    public long getModificationCount() {
      return myServerRestartCount;
    }
  };

  private ModificationTracker myModificationTracker = new ModificationTracker() {
    public long getModificationCount() {
      return modificationCount;
    }
  };

  private long modificationCount;
  @NonNls
  private static final String QUIT_COMMAND_NAME = "quit";
  @NonNls
  static final String IDLE_COMMAND_NAME = "idle";
  private boolean myListenerAdded;
  public static final char DELIMITER = '|';
  public static final String DELIMITER_STRING = "|";
  private static final int CAPACITY = 25;

  static enum ServerState
  {
    FAILED_OR_NOT_STARTED, STARTING, STARTED
  }

  private volatile ServerState serverState;

  public Communicator(Project project) {
    myProject = project;
    if (ApplicationManager.getApplication().isUnitTestMode()) instance = this;
  }

  public synchronized long incModificationCount() {
    return ++modificationCount;
  }

  public synchronized long getModificationCount() {
    return modificationCount;
  }

  void cancelCommand(CommunicatorCommand command) {
    if (command != null && command.isCancellable()) {
      sendCommand(new CancelCommand());
      if (isDebugEnabled) {
        LOG.debug("cancelled:" + command.getCommand());
      }
    }
  }

  public static void debug(String s) {
    if (isDebugEnabled) LOG.debug(s);
  }

  public Project getProject() {
    return myProject;
  }

  public static int findDelimiter(String str, int startFrom) {
    boolean escaped = false;

    for(int i = startFrom;i < str.length(); ++i) {
      char ch = str.charAt(i);
      if (escaped) {
        escaped = false;
        continue;
      }
      if (ch == '\\') escaped = true;
      else if (ch == Communicator.DELIMITER) {
        return i + 1;
      }
    }

    return str.length();
  }

  public void communicatorStarted() {
    serverState = ServerState.STARTED;
  }

  void startErrorStreamReader() {
    new Thread(new ServerErrorThread(), "Cpp Communicator Error Stream Read Thread").start();
  }

  public boolean isServerUpAndRunning() {
    return serverState == ServerState.STARTED;
  }

  static class RestartCommand extends CommunicatorCommand {
    private final String unexpected;

    public RestartCommand(String unexpected) {
      this.unexpected = unexpected;
    }

    public void doExecute() {
    }

    public void commandOutputString(String str) {
    }

    public String getCommand() {
      return null;
    }
  }
  
  public void projectOpened() {
    if (!myProject.isDefault()) {
      final Runnable initRunnable = new Runnable() {
        public void run() {
          new CommunicatorThread();
          myIdleListener = new Runnable() {
            long lastUpdate = -1;

            public void run() {
              final long currentModificationCount = getModificationCount();

              if (lastUpdate != currentModificationCount) {
                lastUpdate = currentModificationCount;

                sendCommand(new StringCommand(IDLE_COMMAND_NAME + " -n "+lastUpdate) {
                  public void doExecuteOnCancel() {
                    super.doExecuteOnCancel();
                    lastUpdate = -1;
                  }
                });
              }
            }
          };

          SwingUtilities.invokeLater(new Runnable() {

            public void run() {
              IdeEventQueue.getInstance().addIdleListener(
                myIdleListener, 2000
              );

              myListenerAdded = true;

              myRootListener = new ModuleRootListener() {
                public void beforeRootsChange(ModuleRootEvent moduleRootEvent) {
                }

                public void rootsChanged(ModuleRootEvent moduleRootEvent) {
                  if (EnvironmentFacade.getInstance().isRealRootChangedEvent(moduleRootEvent)) {
                    restartServer();
                  }
                }
              };

              myConnection = myProject.getMessageBus().connect();
              myConnection.subscribe(ProjectTopics.PROJECT_ROOTS, myRootListener);
            }
          });

          sendCommand(new StringCommand("gc"));
        }
      };
      if (ApplicationManager.getApplication().isUnitTestMode()) {
        initRunnable.run();
        try {
          Thread.sleep(500); // we need to give some time for starting thread and get write lock
        } catch (InterruptedException e) {}
      }
      else {
        StartupManager.getInstance(myProject).registerStartupActivity(initRunnable);
      }
    }
  }

  static class QuitCommand extends StringCommand {
    public QuitCommand() {
      super(QUIT_COMMAND_NAME);
    }
  }
  
  public void sendCommand(final CommunicatorCommand command) {
    if (command instanceof QuitCommand) commandsToRestart.offer(command);
    command.setRestartTimestamp(myServerInternalRestartCount);
    if (meaningfulCommand(command)) ++nonCancellableCommandsCount;

    int startingFactor = serverState == ServerState.STARTING ? 2 : 1;
    if (nonCancellableCommandsCount < startingFactor * CAPACITY || !command.isCancellable()) {
      commandsToWrite.offer(command);
    } else {
      // do restart the server because command length is big
      commandsToRestart.offer(new RestartCommand("Write command limit exceeded"));
      command.doExecuteOnCancel();
    }
  }

  public void projectClosed() {
    if (!myProject.isDefault()) {
      if (myListenerAdded) IdeEventQueue.getInstance().removeIdleListener(myIdleListener);
      myConnection.disconnect();
      myRootListener = null;
      myConnection = null;
      sendCommand(new QuitCommand());

      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          myExecutableState.doDestroyProcess();
        }
      });
    }
  }

  @NonNls
  public String getComponentName() {
    return "CppSupport.Communicator";
  }

  public void initComponent() {}
  public void disposeComponent() {}

  public void restartServer() {
    myServerRestartCount++;
    incModificationCount();

    sendCommand(new StringCommand(QUIT_COMMAND_NAME));

    serverState = ServerState.FAILED_OR_NOT_STARTED;
    commandsToRestart.add(new RestartCommand(null) {
      public void doExecute() {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if (!myProject.isDisposed()) DaemonCodeAnalyzer.getInstance(myProject).restart();
          }
        });
      }
    });
  }

  public void onFileCreated(VirtualFile file) {
    StringBuilder builder = new StringBuilder();

    if (isHeaderFile(file)) {
      addHeaderFileCommand(buildContainingDirPath(file), builder);
    } else {
      addSourceFileCommand(file, builder);
    } 

    if (builder.length() > 0) {
      sendCommand(new StringCommand(builder.toString()));
    }
    incModificationCount();
  }

  public ModificationTracker getServerRestartTracker() {
    return myServerRestartTracker;
  }

  public ModificationTracker getModificationTracker() {
    return myModificationTracker;
  }

  private static String buildContainingDirPath(VirtualFile fileOrDir) {
    return fileOrDir.getParent().getPath().replace('/', File.separatorChar);
  }

  public static boolean isCFile(VirtualFile file) {
    final String extension = file.getExtension();
    return "c".equals(extension);
  }
  
  public static boolean isHeaderFile(VirtualFile file) {
    final String extension = file.getExtension();
    return CppSupportLoader.H_EXTENSION.equals(extension) ||
           CppSupportLoader.INC_EXTENSION.equals(extension) ||
           CppSupportLoader.isKnownEmptyExtensionFile(file.getName()) ||
               ( CppSupportLoader.HPP_EXTENSION.equals(extension) ||
                 CppSupportLoader.INL_EXTENSION.equals(extension) ||
                 CppSupportLoader.HXX_EXTENSION.equals(extension) ||
                 CppSupportLoader.HI_EXTENSION.equals(extension) ||
                 CppSupportLoader.TCC_EXTENSION.equals(extension)
               );
  }

  private static void addHeaderFileCommand(String path, StringBuilder builder) {
    builder.append("user-include-path ").append(BuildingCommandHelper.quote(path)).append("\n");
  }

  public static void addSourceFileCommand(VirtualFile fileOrDir, StringBuilder builder) {
    String path = fileOrDir.getPath().replace('/', File.separatorChar);
    builder.append("module ").append(BuildingCommandHelper.quote(path)).append(" " + configName(fileOrDir)).append("\n");
  }

  private static String configName(VirtualFile fileOrDir) {
    return BuildingCommandHelper.configNameByMode(isCFile(fileOrDir));
  }

  public void onFileRemoved(VirtualFile file) {
    String path = file.getPath().replace('/', File.separatorChar);
    StringBuilder builder = new StringBuilder();

    if (!isHeaderFile(file)) {
      builder.append("module-remove ").append(BuildingCommandHelper.quote(path)).append(" " + configName(file));
    }

    if (builder.length() > 0) {
      sendCommand(new StringCommand(builder.toString()));
    }
    incModificationCount();
  }

  public static Communicator getInstance(Project project) {
    if (instance != null) return instance;
    return project.getComponent(Communicator.class);
  }
  
  private final BlockingQueue<CommunicatorCommand> commandsToRestart = new ArrayBlockingQueue<CommunicatorCommand>(CAPACITY);
  private final BlockingQueue<CommunicatorCommand> commandsToRead = new LinkedBlockingQueue<CommunicatorCommand>();
  private final BlockingQueue<CommunicatorCommand> commandsToWrite = new LinkedBlockingQueue<CommunicatorCommand>();
  private volatile int nonCancellableCommandsCount;

  private ServerExecutableState myExecutableState = new ServerExecutableState();
    
  class CommunicatorThread implements Runnable {
    CommunicatorThread() {
      new Thread(new ServerAliveThread(), "Cpp Communicator Support Thread").start();
      new Thread(null, this, "Cpp Communicator Thread").start();
      new Thread(new ServerReadThread(), "Cpp Communicator Read Thread").start();
    }

    public void run() {
      try {
        while(true) {
          if (myProject.isDisposed()) {
            return;
          }

          final CommunicatorCommand pendingCommand = commandsToWrite.poll(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
          if (meaningfulCommand(pendingCommand)) --nonCancellableCommandsCount;

          if (pendingCommand.getRestartTimestamp() != myServerInternalRestartCount) {
            pendingCommand.doExecuteOnCancel();
            continue;
          }

          if (!myExecutableState.isServerProcessAlive()) {
            pendingCommand.doExecuteOnCancel();
            if (myExecutableState.isServerProcessStarted()) {
              commandsToRestart.clear();
              commandsToRestart.add(new RestartCommand("Unexpected server exit"));
            }
            continue;
          }

          if (!commandsToRead.offer(pendingCommand)) {
            // we offer command to read first because next write should not
            // block during flush due to limited i / o buffer!
            if (pendingCommand.isCancellable()) {
              commandsToRestart.add(new RestartCommand("Unexpected read buffer overflow"));
              pendingCommand.doExecuteOnCancel();
            } else {
              commandsToRead.put(pendingCommand);
            }
          }

          try {
            String command = pendingCommand.getCommand();
            if (isDebugEnabled) LOG.debug("Sending to server: " + command);            
            myExecutableState.writeStringToInputStream(command +"\n");
            if (pendingCommand instanceof QuitCommand) return;
          } catch (IOException e) {
            pendingCommand.doExecuteOnCancel();
            LOG.debug(e);
            commandsToRestart.add(new RestartCommand(e.getMessage()));
          }
        }
      } catch (InterruptedException e) {
        return;
      }
    }
  }

  class ServerErrorThread implements Runnable {
    public void run() {
      try {
        while(true) {
          if (myProject.isDisposed()) {
            return;
          }

          final String str = myExecutableState.readLineFromErrorStream();
          if (str == null) break;
          LOG.error(str);
        }
      } catch (IOException ex) {
        serverState = ServerState.FAILED_OR_NOT_STARTED;
        if(filterExceptionDueToServerExit(ex)) return;
        ex.printStackTrace();
        LOG.error(ex);
      }
    }
  }

  class ServerReadThread implements Runnable {
    @NonNls
    private static final String OK_COMMAND_PREFIX = "<OK:";
    @NonNls
    private static final String CANCELLED_COMMAND_PREFIX = "<Cancelled:";
    @NonNls
    private static final String OK_CANCEL_COMMAND_PREFIX = "<OK:cancel";

    public void run() {
      try {
        Out:
        while(true) {
          if (myProject.isDisposed()) {
            return;
          }

          CommunicatorCommand communicatorCommand = commandsToRead.poll(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
          if (communicatorCommand instanceof QuitCommand) return;
          if (communicatorCommand.getRestartTimestamp() != myServerInternalRestartCount) {
            communicatorCommand.doExecuteOnCancel();
            continue;
          }
          String commandText = communicatorCommand.getCommand();
          int i = commandText.lastIndexOf('\n') + 1;
          int i2 = commandText.indexOf(' ',i);
          String commandId = null;

          if (i2 == -1) i2 = commandText.length();
          else {
            final String str = "-n ";
            int i3 = commandText.indexOf(str, i2);
            if (i3 != -1) {
              i3 += str.length();
              int index = commandText.indexOf(' ', i3);
              commandId = ":" + commandText.substring(i3, index == -1 ? commandText.length():index);
            }
          }

          String lastCommandName = commandText.substring(i, i2);
          String confirmationText = OK_COMMAND_PREFIX + lastCommandName;
          if (commandId != null) {
            confirmationText += commandId;
            lastCommandName += commandId;
          } else {
            confirmationText += ":";
            lastCommandName += ":";
          }

          if (isDebugEnabled) LOG.debug("waiting for confirmation string:"+confirmationText);

          boolean wasOutputToStatus = false;

          try {
            final long started = System.currentTimeMillis();

            while(true) {
              String read = myExecutableState.readLineFromOutputStream();
              if (isDebugEnabled) LOG.debug("Read from server:" + read);
              if (read == null) {
                if (!(communicatorCommand instanceof QuitCommand)) {
                  communicatorCommand.doExecuteOnCancel();
                }
                break;
              }

              if (read.indexOf(CANCELLED_COMMAND_PREFIX) != -1) {
                if (read.indexOf(lastCommandName) != -1) {
                  communicatorCommand.doExecuteOnCancel();
                  break;
                }
              } else
              if (read.indexOf(confirmationText) != -1) {
                if (isDebugEnabled) {
                  LOG.debug("Done:" + confirmationText + " " + (System.currentTimeMillis() - started));
                }
                notifySink(communicatorCommand, read);
                break;
              } else if (read.startsWith(OK_COMMAND_PREFIX, 0)) {
                if (read.startsWith(OK_CANCEL_COMMAND_PREFIX)) {
                  break;
                }
                if (isDebugEnabled) {
                  LOG.debug("1:" + read + " " + (System.currentTimeMillis() - started));
                }
                communicatorCommand.commandFinishedString( read );
              } else {
                wasOutputToStatus |= doDispatch(communicatorCommand, read);
              }
            }
          } catch(IOException ex) {
            communicatorCommand.doExecuteOnCancel();
            serverState = ServerState.FAILED_OR_NOT_STARTED;
            if (filterExceptionDueToServerExit(ex)) {
              continue;
            }
            ex.printStackTrace();
            LOG.error(ex);
            continue;
          }
          finally {
            if (wasOutputToStatus) {
              setMessage("");
            }
          }
        }
      } catch (InterruptedException e) {
        return;
      }
    }

    void notifySink(final CommunicatorCommand myCommand, final String confirmationText) {
      myCommand.commandFinishedString( confirmationText );

      if (myCommand.doInvokeInDispatchThread()) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if (!myProject.isDisposed()) myCommand.doExecute();
          }
        });
      } else {

        myCommand.doExecute();
      }
    }

    private boolean doDispatch(final CommunicatorCommand myCommand, final String str) {
      if (str == null) return false; // ?

      final String prefix = "Status:";
      final String prefix2 = "MESSAGE:|";
      boolean shouldClearMessage;

      if ((shouldClearMessage = str.startsWith(prefix)) ||
          str.startsWith(prefix2)
         ) {
        final String message = str.substring(
          str.startsWith(prefix) ? prefix.length() : prefix2.length()
        );

        setMessage(message);
        return shouldClearMessage;
      }

      if (str.length() == 0 && !myCommand.acceptsEmptyResult()) return false;
      try {
        myCommand.commandOutputString( str );
      } catch(Exception e) {
        e.printStackTrace();
        LOG.debug(e);
      }
      return false;
    }

    private void setMessage(final String message) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          if (myProject.isDisposed()) return;
          WindowManager.getInstance().getStatusBar(myProject).setInfo(message);
          if (message.indexOf("Failure:") != -1) {
            serverState = ServerState.FAILED_OR_NOT_STARTED;
            LOG.error(new ServerExecutionException(BuildingCommandHelper.unquote(message)));
          }
        }
      });
    }
  }

  private static boolean filterExceptionDueToServerExit(IOException ex) {
    final String localizedMessage = ex.getLocalizedMessage();
    return localizedMessage != null && localizedMessage.indexOf("Stream closed") >= 0;
  }

  static final long MIN_TIME = 5 * 60 * 1000;
  class ServerAliveThread implements Runnable {
    final Runnable startServerAction = new Runnable() {
      public void run() {
        myExecutableState.startProcess(Communicator.this);
      }
    };

    final Runnable restartServerAction = new Runnable() {
      public void run() {
        myExecutableState.restartProcess(Communicator.this);
      }
    };

    public void run() {
      try {
        int restartCount = 0;
        long startedTime = System.currentTimeMillis();
        doServerStartup(startServerAction);
        
        while(true) {
          if (myProject.isDisposed()) {
            return;
          }

          final CommunicatorCommand pendingCommand = commandsToRestart.poll(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
          if (pendingCommand instanceof QuitCommand) return;
          if (serverState == ServerState.STARTING) {
            continue;
          }

          ++myServerInternalRestartCount;
          nonCancellableCommandsCount = 0;
          if (isDebugEnabled) LOG.debug("restarting server");
          if (pendingCommand instanceof RestartCommand &&
              ((RestartCommand)pendingCommand).unexpected != null
             ) {
            LOG.error(new ServerExecutionException(((RestartCommand)pendingCommand).unexpected));
          }

          final long now = System.currentTimeMillis();
          if (now - startedTime < MIN_TIME) ++restartCount;
          else restartCount = 0;

          startedTime = now;
          doServerStartup(restartServerAction);

          commandsToRestart.clear();

          pendingCommand.doExecute();
          Thread.sleep(5000 * restartCount);
        }
      } catch(InterruptedException ex) {}
    }

    private void doServerStartup(final Runnable startServerAction) {
      if (ApplicationManager.getApplication().isUnitTestMode()) return;
      serverState = ServerState.STARTING;

      ApplicationManager.getApplication().invokeAndWait(new Runnable() {
        public void run() {
          ProgressManager.getInstance().runProcessWithProgressAsynchronously(
            myProject,
            "analyzing c / c++ sources",
            startServerAction,
            null,
            null,
            new PerformInBackgroundOption() {
              public boolean shouldStartInBackground() {
                return true;
              }

              public void processSentToBackground() {
              }
            }
          );
        }
      }, ModalityState.NON_MODAL);
    }
  }

  static class ServerExecutionException extends RuntimeException {

    public ServerExecutionException(String message) {
      super(message);
    }

    public void printStackTrace() {
      printStackTrace(System.out);
    }

    public void printStackTrace(PrintStream s) {
      s.println(getMessage());
    }

    public void printStackTrace(PrintWriter s) {
      s.println(getMessage());
    }
  }

  private class CancelCommand extends StringCommand {
    public CancelCommand() {
      super("cancel");
    }
  }

  private boolean meaningfulCommand(CommunicatorCommand pendingCommand) {
    return pendingCommand.isCancellable() && !(pendingCommand instanceof CancelCommand);
  }
}
