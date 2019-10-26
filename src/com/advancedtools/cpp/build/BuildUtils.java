// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.build;

import com.advancedtools.cpp.CppSupportSettings;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.intellij.CommonBundle;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author maxim
 */
public class BuildUtils {
  @NonNls
  public static final String MAKE_TOOL_NAME = "make";

  static File createTempFileWithContent(@NotNull String namePrefix, @NotNull String nameSuffix, @NotNull String content) throws IOException {
    namePrefix = namePrefix.substring(namePrefix.lastIndexOf('\\') + 1);
    namePrefix = namePrefix.substring(namePrefix.lastIndexOf('/') + 1);
    namePrefix = namePrefix.replace('.', '_');
    File tempFile = File.createTempFile(namePrefix, nameSuffix);
    tempFile.deleteOnExit();
    final BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(tempFile));
    stream.write(content.getBytes());
    stream.close();
    return tempFile;
  }

  private static final Map<String, File> createdFiles = new HashMap<String, File>();

  static List<String> createShellCommand(@NotNull String namePrefix, @NotNull String content) throws IOException {
    final String key = namePrefix + content;

    synchronized(createdFiles) {
      File tempFile = createdFiles.get(key);

      if (tempFile == null) {
        tempFile = createTempFileWithContent(namePrefix, ".bat", content);
        createdFiles.put(key, tempFile);
      }
      return Arrays.asList("cmd.exe", "/c", tempFile.getPath());
    }
  }

  static String[] getTargetsFromMakeFile(@NotNull VirtualFile file, final @NotNull Pattern targetPattern) {
    final Set<String> availableTargets = new HashSet<String>();

    BuildingCommandHelper.doScanFileContent(file, new Processor<String>() {
      public boolean process(String s) {
        if (s.length() > 0 && Character.isJavaIdentifierPart(s.charAt(0)) && s.indexOf(':') > 0) {
          final Matcher matcher = targetPattern.matcher(s);
          if (matcher.find()) {
            availableTargets.add(matcher.group(1));
          }
        }
        return true;
      }
    });

    availableTargets.add(BuildTarget.DEFAULT_BUILD_ACTION);
    final String[] strings = availableTargets.toArray(new String[availableTargets.size()]);
    Arrays.sort(strings);
    return strings;
  }

  public static List<String> buildVCToolInvokation(List<String> nmakeInvokation) throws IOException {
    final String vsCDir = CppSupportSettings.getInstance().getVsCDir();
    return vsCDir != null ? createShellCommand("nmake",
      "call \"" + vsCDir + File.separatorChar + "bin" + File.separatorChar + "vcvars32.bat\"\n" + join(nmakeInvokation, " ")
    ):null;
  }

  public static @NotNull List<String> appendOptions(@NotNull List<String> command, String opts) {
    try {
      if (!StringUtil.isEmpty(opts)) command.add(opts);
    } catch (Exception e) {
      command = new ArrayList<String>(command);
      command.add(opts);
    }
    return command;
  }

  public static @NotNull List<String> buildGccToolCall(@NotNull String toolExeName,@NotNull List<String> toolCommandLine) {
    List<String> shellCommand;

    if (SystemInfo.isWindows) {
      try {
        final CppSupportSettings cppSupportSettings = CppSupportSettings.getInstance();
        String cygBin = cppSupportSettings.getGccPath();

        if (cygBin == null || cygBin.length() == 0) {
          cygBin = "c:\\CygWin\\bin";
        } else {
          int index = cygBin.lastIndexOf(File.separatorChar);

          cygBin = index != -1 ? cygBin.substring(0, index):"";
          if (cygBin.length() == 0) cygBin = "c:\\CygWin\\bin";
        }

        String path = "set PATH=$PATH$;" + cygBin;

        if (MAKE_TOOL_NAME.equals(toolExeName) &&
          !new File(cygBin, toolExeName + ".exe").exists() &&
          cppSupportSettings.getMingwToolsDir() != null) {
          String prevPath = path;
          path = "set OSTYPE=msys\n";
          path = path + prevPath + ";" + cppSupportSettings.getMingwToolsDir() + File.separatorChar + "bin";
        }

        shellCommand = createShellCommand(toolExeName, path + "\n" + toolExeName + " %*");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      shellCommand = Arrays.asList(toolExeName);
    }

    shellCommand = BuildUtils.join(shellCommand, toolCommandLine);
    return shellCommand;
  }

  static final @NonNls Map<String, String> myItems;
  static {
    if (SystemInfo.isMac) {
      myItems = new HashMap<String, String>(System.getenv());
      myItems.put("OSTYPE", "darwin");
    } else {
      myItems = null;
    }
  }
  public static Map<String, String> buildEnvironmentMap(Project project, VirtualFile file) {
    return myItems;
  }

  public static List<String> buildClangToolCall(String clang, List<String> args) {
    return prepend(clang, args);
  }

  private static List<String> prepend(String clang, List<String> args) {
    try {
      args.add(0, clang);
      return args;
    } catch (Exception e) {
      ArrayList<String> commands = new ArrayList<String>(args.size() + 1);
      commands.add(clang);
      commands.addAll(args);
      return commands;
    }
  }

  private static List<String> join(List<String> command, List<String> args) {
    try {
      args.addAll(0, command);
      return args;
    } catch (Exception e) {
      ArrayList<String> commands = new ArrayList<String>(args.size() + command.size());
      commands.addAll(command);
      commands.addAll(args);
      return commands;
    }
  }

  public static List<String> appendAllOptions(List<String> runCommand, String additionalCommandLineParameters) {
    if (StringUtil.isEmpty(additionalCommandLineParameters)) return runCommand;
    StringTokenizer tokenizer = new StringTokenizer(additionalCommandLineParameters, " ");
    while(tokenizer.hasMoreElements()) runCommand = appendOptions(runCommand, tokenizer.nextToken());
    return runCommand;
  }

  static String join(List<String> runCommand, String del) {
    if (runCommand.size() == 0) return "";
    StringBuilder b = new StringBuilder(runCommand.get(0));
    for(int i = 1; i < runCommand.size(); ++i) {
      b.append(del).append(runCommand.get(i));
    }
    return b.toString();
  }

  static class BaseRerunAction extends AnAction {
    private final OSProcessHandler myProcessHandler;
    private Runnable myRerunTask;

    public BaseRerunAction(String text, String description, Icon icon, final ConsoleView consoleView, OSProcessHandler processHandler, Runnable rerun) {
      super(text,description,icon);
      myProcessHandler = processHandler;
      myRerunTask = rerun;
    }

    public void update(AnActionEvent e) {
      e.getPresentation().setEnabled(myProcessHandler.isProcessTerminated());
    }

    public void actionPerformed(AnActionEvent e) {
      myRerunTask.run();
    }
  }

  static class RerunAction extends BaseRerunAction {
    public RerunAction(final ConsoleView consoleView, OSProcessHandler processHandler, Runnable rerun) {
      super(CommonBundle.message("action.rerun"),CommonBundle.message("action.rerun"),
            IconLoader.getIcon("/actions/refreshUsages.png"), consoleView, processHandler, rerun);
      registerCustomShortcutSet(CommonShortcuts.getRerun(),consoleView.getComponent());
    }
  }

  static class RefreshAction extends BaseRerunAction {
    @NonNls
    private static final String CHOOSE_SETTINGS_AND_RERUN = "Choose settings and rerun";

    public RefreshAction(final ConsoleView consoleView, OSProcessHandler processHandler, Runnable rerun) {
      super(CHOOSE_SETTINGS_AND_RERUN, CHOOSE_SETTINGS_AND_RERUN,
            IconLoader.getIcon("/actions/refresh.png"), consoleView, processHandler, rerun);
    }
  }
}
