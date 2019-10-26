// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.actions;

import com.advancedtools.cpp.CppBundle;
import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.CppSupportSettings;
import com.advancedtools.cpp.build.*;
import com.advancedtools.cpp.communicator.Communicator;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.intellij.execution.filters.Filter;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author maxim
 */
public class CompileCppAction extends AnAction {
  private static CppSupportSettings.CompilerSelectOptions lastOptions;

  public void actionPerformed(AnActionEvent anActionEvent) {
    invoke(anActionEvent.getData(LangDataKeys.PROJECT), anActionEvent.getData(LangDataKeys.VIRTUAL_FILE), null);
  }

  interface CompileCppOptions {
    String getCompilerOptions();
    String getProjectCompilerOptions();
    boolean doRun();
    String getOutputFileName();
    Project getProject();

    boolean isCppFile();
  }

  public static abstract class CompileHandler {
    private static final String MARKER = "$filename$";

    abstract @Nullable List<String> buildCommand(VirtualFile file, CompileCppOptions options);
    abstract @Nullable Map<String, String> getCommandLineProperties();
    abstract @Nullable Filter getCompileLogFilter(VirtualFile file, CompileCppOptions options);

    protected List<String> defaultAppendOptions(CompileCppOptions options, List<String> command, VirtualFile file) {
      command = BuildUtils.appendAllOptions(command, options.getProjectCompilerOptions());
      command = BuildUtils.appendAllOptions(
        command,
        options.getCompilerOptions().replace(
          MARKER,
          getEscapedPathToFile(file.getPath())
        )
      );
      return command;
    }

    abstract String buildProjectCompileOptions(Project project);

    abstract String getOutputFileName(VirtualFile file, CompileCppOptions compileOptions);
  }

  public static class ClangCompileHandler extends CompileHandler {
    private @NonNls Map<String, String> myItems;

    @Nullable List<String> buildCommand(VirtualFile file, CompileCppOptions options) {
      List<String> commandLine = new ArrayList<String>(4);
      if (!options.doRun()) {
        commandLine = BuildUtils.appendOptions(commandLine, "-S");
      }
      else {
        final String fileName = options.getOutputFileName();
        if (fileName != null) {
          commandLine = BuildUtils.appendOptions(commandLine, "-o");
          commandLine = BuildUtils.appendOptions(commandLine, fileName);
        }
      }
      commandLine = defaultAppendOptions(options, commandLine, file);
      myItems = BuildUtils.buildEnvironmentMap(options.getProject(), file);
      return BuildUtils.buildClangToolCall(CppSupportSettings.getInstance().getClangPath(), commandLine);
    }

    @Nullable
    Map<String, String> getCommandLineProperties() {
      return myItems;
    }

    public static String getMatchingPattern() {
      return "^((?:\\w\\:)?[^\\:]+)(?:\\:([0-9]+)\\:(?:([0-9])+\\:))";
    }

    @Nullable
    Filter getCompileLogFilter(VirtualFile file, CompileCppOptions options) {
      return new BasicFormatFilter(file, options.getProject(), getMatchingPattern());
    }

    String buildProjectCompileOptions(Project project) {
      return null;
    }

    String getOutputFileName(VirtualFile file, CompileCppOptions compileOptions) {
      final String fileName = compileOptions.getOutputFileName();
      return fileName != null ? fileName: SystemInfo.isWindows ? "a.exe":"a.out";
    }
  }

  static class GccCompileHandler extends CompileHandler {
    private @NonNls Map<String, String> myItems;

    @Nullable List<String> buildCommand(VirtualFile file, CompileCppOptions options) {
      List<String> command = new ArrayList<String>(4);
      if (!options.doRun()) command = BuildUtils.appendOptions(command, "-c");
      else {
        final String fileName = options.getOutputFileName();
        if (fileName != null) {
          command = BuildUtils.appendOptions(command, "-o");
          command = BuildUtils.appendOptions(command, fileName);
        }
      }
      command = defaultAppendOptions(options, command, file);
      myItems = BuildUtils.buildEnvironmentMap(options.getProject(), file);
      String gccPath = CppSupportSettings.getInstance().getGccPath().replaceAll("gcc", options.isCppFile() ? "g++":"gcc");

      return BuildUtils.buildGccToolCall(gccPath, command);
    }

    @Nullable
    Map<String, String> getCommandLineProperties() {
      return myItems;
    }

    @Nullable
    Filter getCompileLogFilter(VirtualFile file, CompileCppOptions options) {
      return new MakeBuildHandler.MakeFormatFilter(file, options.getProject());
    }

    String buildProjectCompileOptions(Project project) {
      return null;
    }

    String getOutputFileName(VirtualFile file, CompileCppOptions compileOptions) {
      final String fileName = compileOptions.getOutputFileName();
      return fileName != null ? fileName: SystemInfo.isWindows ? "a.exe":"a.out";
    }
  }

  static class VcCompileHandler extends CompileHandler {
    @Nullable List<String> buildCommand(VirtualFile file, CompileCppOptions options) {
      try {
        List<String> commandLine = Arrays.asList("cl.exe");
        if (!options.doRun()) commandLine = BuildUtils.appendOptions(commandLine, "/c");
        else {
          final String fileName = options.getOutputFileName();
          if (fileName != null) {
            commandLine = BuildUtils.appendOptions(commandLine, "/Fe"+ fileName);
          }
        }

        commandLine = defaultAppendOptions(options, commandLine, file);
        return BuildUtils.buildVCToolInvokation(commandLine);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Nullable
    Map<String, String> getCommandLineProperties() {
      return null;
    }

    @Nullable
    Filter getCompileLogFilter(VirtualFile file, CompileCppOptions options) {
      return new VisualStudioBuildHandler.VCFormatFilter(file, options.getProject());
    }

    String buildProjectCompileOptions(Project project) {
      return null;
    }

    String getOutputFileName(VirtualFile file, CompileCppOptions compileOptions) {
      final String fileName = compileOptions.getOutputFileName();
      return fileName != null ? fileName:file.getNameWithoutExtension() + ".exe";
    }
  }

  private void invoke(final Project project, final VirtualFile file, CompileCppOptions _options) {
    CompileHandler handler = getCompileHandler(CompileCppDialog.getCurrentCompilerOption(project));

    final CompileCppDialog dialog = new CompileCppDialog(project, handler, _options);
    dialog.show();
    if(dialog.getExitCode() != DialogWrapper.OK_EXIT_CODE) return;

    _options = new CompileCppOptions() {
      public String getCompilerOptions() {
        return dialog.getCompilerOptions();
      }

      public String getProjectCompilerOptions() {
        return dialog.getProjectCompilerOptions();
      }

      public boolean doRun() {
        return dialog.doRun();
      }

      public String getOutputFileName() {
        final String fileName = dialog.getOutputFileName();
        return fileName.length() > 0? fileName:null;
      }

      public Project getProject() {
        return project;
      }

      public boolean isCppFile() {
        return "cpp".endsWith(file.getExtension());
      }
    };

    final CompileCppOptions options = _options;
    handler = getCompileHandler(CompileCppDialog.getCurrentCompilerOption(project));
    List<String> runCommand = handler.buildCommand(
      file,
      options
    );

    if (runCommand == null) throw new RuntimeException("Cannot invoke compilation");

    final Map<String, String> commandLineProperties = handler.getCommandLineProperties();

    final String baseForExecutableFile = file.getParent().getPath() + File.separatorChar;
    final String fileToExecute = baseForExecutableFile + handler.getOutputFileName(file, options);
    new File(fileToExecute).delete();

    final ConsoleBuilder consoleBuilderRef[] = new ConsoleBuilder[1];
    Runnable action = new Runnable() {
      public void run() {
        if (options.doRun() && new File(fileToExecute).exists()) {
          new ConsoleBuilder(
            "Run " + file.getName(),
            new BuildState(Arrays.asList(fileToExecute),new File(file.getParent().getPath()),commandLineProperties),
            project,
            null,
            new Runnable() {
              public void run() {
                invoke(project, file, options);
              }
            },
            new Runnable() {
              public void run() {
                consoleBuilderRef[0].start();
              }
            },
            null
          ).start();
        }
      }
    };

    final ConsoleBuilder consoleBuilder = new ConsoleBuilder(
      "Compile File " + file.getName(),
      new BuildState(runCommand,new File(file.getParent().getPath()), commandLineProperties),
      project,
      handler.getCompileLogFilter(file, options),
      new Runnable() {
        public void run() {
          invoke(project, file, options);
        }
      },
      null,
      action
    );
    consoleBuilderRef[0] = consoleBuilder;
    consoleBuilder.start();
  }

  private static CompileHandler getCompileHandler(CppSupportSettings.CompilerSelectOptions compilerOption) {
    CompileHandler handler;

    if (compilerOption == CppSupportSettings.CompilerSelectOptions.GCC ||
        (!SystemInfo.isWindows && compilerOption == CppSupportSettings.CompilerSelectOptions.AUTO)
      ) {
      handler = new GccCompileHandler();
    } else if (compilerOption == CppSupportSettings.CompilerSelectOptions.CLANG) {
      handler = new ClangCompileHandler();
    } else {
      handler = new VcCompileHandler();
    }
    return handler;
  }

  public void update(AnActionEvent e) {
    super.update(e);

    final Project project = e.getData(LangDataKeys.PROJECT);
    final VirtualFile file = e.getData(LangDataKeys.VIRTUAL_FILE);

    boolean visible = project != null &&
      file != null &&
      !file.isDirectory() &&
      file.getFileType() == CppSupportLoader.CPP_FILETYPE &&
      !Communicator.isHeaderFile(file);
    boolean enabled = visible;

    if (!visible) {
      visible = ActionPlaces.MAIN_MENU.equals(e.getPlace());
    }

    e.getPresentation().setEnabled(enabled);
    e.getPresentation().setVisible(visible);

    if (visible) {
      final String s = "Do c&ompile for " + (file != null ? file.getName():"selected c/c++ fileToCompile");
      e.getPresentation().setText(s);
      e.getPresentation().setDescription(s);
    }
  }

  static class CompileCppDialog extends DialogWrapper {
    private JPanel myPanel;
    private JTextField compileProperties;
    private JComboBox compilerSelector;
    private JCheckBox includeProjectCompileParametersCheckBox;
    private JTextField projectCompileParameters;
    private JCheckBox doRun;
    private JComboBox executableFileName;
    private Project project;

    static boolean lastRunStatus;
    
    protected CompileCppDialog(Project _project, CompileHandler compilerHandler, CompileCppOptions options) {
      super(_project, false);

      project = _project;
      setModal(true);

      doRun.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          final boolean b = doRun.isSelected();
          executableFileName.setEnabled(b);
          executableFileName.setEditable(b);
        }
      });

      executableFileName.getEditor().setItem(options != null ? options.getOutputFileName() : "");
      doRun.setSelected(lastRunStatus);

      final CppSupportLoader loader = CppSupportLoader.getInstance(project);
      final String compileParameters = loader.getAdditionalCompileParameters();

      compileProperties.setText(
        (compileParameters != null && compileParameters.length() > 0 ?compileParameters + " ":"") + CompileHandler.MARKER
      );

      setTitle(CppBundle.message("compile.cpp.file.dialog.title"));

      compilerSelector.setModel(new DefaultComboBoxModel(CppSupportSettings.CompilerSelectOptions.values()));
      compilerSelector.setSelectedItem(getCurrentCompilerOption(project));

      setSelectedProjectCompile();

      includeProjectCompileParametersCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          setSelectedProjectCompile();
        }
      });

      includeProjectCompileParametersCheckBox.setSelected(loader.isIncludeProjectSettings());
      final String compileParametersText = compilerHandler.buildProjectCompileOptions(project);
      projectCompileParameters.setText(compileParametersText != null ? compileParametersText:"");

      init();
    }

    private void setSelectedProjectCompile() {
      boolean b = includeProjectCompileParametersCheckBox.isSelected();
      projectCompileParameters.setEditable(b);
      projectCompileParameters.setEnabled(b);
    }

    static CppSupportSettings.CompilerSelectOptions getCurrentCompilerOption(Project project) {
      return lastOptions != null ? lastOptions: CppSupportLoader.getInstance(project).getCompilerOptions();
    }

    String getCompilerOptions() {
      return compileProperties.getText();
    }

    String getProjectCompilerOptions() {
      return includeProjectCompileParametersCheckBox.isSelected() ? projectCompileParameters.getText():"";
    }

    boolean doRun() {
      return doRun.isSelected();
    }

    CppSupportSettings.CompilerSelectOptions getCompiler() {
      return (CppSupportSettings.CompilerSelectOptions) compilerSelector.getSelectedItem();
    }

    protected void doOKAction() {
      lastRunStatus = doRun.isSelected();
      
      final CppSupportLoader loader = CppSupportLoader.getInstance(project);
      loader.setIncludeProjectSettings(includeProjectCompileParametersCheckBox.isSelected());
      String s = compileProperties.getText();
      int index = s.lastIndexOf(CompileHandler.MARKER);
      if (index != -1) s = s.substring(0, index).trim();
      loader.setAdditionalCompileParameters(s.length() > 0 ? s:null);
      lastOptions = getCompiler();

      super.doOKAction();
    }

    @Nullable
    protected JComponent createCenterPanel() {
      return myPanel;
    }

    public String getOutputFileName() {
      return (String) executableFileName.getEditor().getItem();
    }
  }

  public static String getEscapedPathToFile(String path) {
    return BuildingCommandHelper.quote(path, SystemInfo.isWindows);
  }
}
