// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.communicator;

import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.CppSupportSettings;
import com.advancedtools.cpp.facade.EnvironmentFacade;
import com.advancedtools.cpp.settings.CppHighlightingSettings;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author maxim
 */
public class BuildingCommandHelper {
  // TODO:PathVariables support 

  static String buildPassProjectModelCommandText(@NotNull String serverBinaryDirectory, @NotNull final Project project) {
    final @NonNls StringBuilder systemSettings = new StringBuilder();
    CppSupportSettings instance = CppSupportSettings.getInstance();

    final @NonNls StringBuilder projectSettings = new StringBuilder();
    systemSettings.append("source \"").append(serverBinaryDirectory).append("lib.tcl\"\n");

    systemSettings.append("use-abs-path\n"); // Mac and Linux
    if (instance.isLaunchGdbOnFailure()) {
      systemSettings.append("set-debugger-command ");
      final String gdbOnFailureCommand = instance.getLaunchGdbOnFailureCommand();
      systemSettings.append(gdbOnFailureCommand.replace("$gdb$", quote(instance.getGdbPath()))).append("\n");
    }

    final CppSupportLoader settings = CppSupportLoader.getInstance(project);
    final Set<VirtualFile> projectAndBuildFiles = settings.getProjectAndBuildFilesSet();
    projectAndBuildFiles.clear();
    final boolean hasFilesNotUnderSourceRoots[] = new boolean[1];
    final boolean hasSomeCppFiles[] = new boolean[1];
    final Collection<String> sourceRoots = new HashSet<String>();

    final StringBuilder cppModuleList = new StringBuilder();
    final StringBuilder cModuleList = new StringBuilder();
    final @NonNls StringBuilder jniCommands = new StringBuilder();

    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        Sdk projectJdk = ProjectRootManager.getInstance(project).getProjectSdk();

        if (projectJdk != null) {
          final VirtualFile directory = EnvironmentFacade.getSdkHomeDirectory(projectJdk);
          final VirtualFile includeDir = directory != null ? directory.findChild("include"):null;
          if (includeDir != null) {
            jniCommands.append( "system-include-path \"").append(quote(fixVirtualFileName(includeDir.getPath()), false)).append("\"\n");
            VirtualFile platformSpecific = includeDir.findChild(SystemInfo.isWindows ? "win32" : System.getProperty("os.name").toLowerCase());
            if (platformSpecific != null) {
              jniCommands.append( "system-include-path \"").append(quote(fixVirtualFileName(platformSpecific.getPath()), false)).append("\"\n");
            }
          }
        }

        final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();

        fileIndex.iterateContent(
          new ContentIterator() {
            Set<String> ignoredFiles = new HashSet<String>(settings.getIgnoredFilesSet());
            private VirtualFile myparent;

            public boolean processFile(VirtualFile fileOrDir) {
              if (fileOrDir.isDirectory()) return true;

              if (fileOrDir.getName().equalsIgnoreCase(Communicator.MAKEFILE_FILE_NAME)) {
                final Module module = fileIndex.getModuleForFile(fileOrDir);
                final ModuleFileIndex index = ModuleRootManager.getInstance(module).getFileIndex();
                if (!index.isInTestSourceContent(fileOrDir)) projectAndBuildFiles.add(fileOrDir);
              } else {
                final String fileExtension = fileOrDir.getExtension();

                if (Communicator.DSP_EXTENSION.equalsIgnoreCase(fileExtension) ||
                    Communicator.VCPROJ_EXTENSION.equalsIgnoreCase(fileExtension) ||
                    //Communicator.SLN_EXTENSION.equalsIgnoreCase(fileExtension) ||
                    Communicator.MAK_EXTENSION.equalsIgnoreCase(fileExtension) //||
                    //Communicator.DSW_EXTENSION.equalsIgnoreCase(fileExtension)
                   ) {
                  final Module module = fileIndex.getModuleForFile(fileOrDir);
                  final ModuleFileIndex index = ModuleRootManager.getInstance(module).getFileIndex();
                  if (index.isInContent(fileOrDir)) projectAndBuildFiles.add(fileOrDir);
                }
                else if (fileOrDir.getFileType() == CppSupportLoader.CPP_FILETYPE) {
                  final boolean inSourceContent = CppSupportLoader.isInSource(fileOrDir, fileIndex);

                  if (!inSourceContent  ||
                    fileIndex.isInTestSourceContent(fileOrDir) ||
                      ignoredFiles.contains(fileOrDir.getPresentableUrl().replace(File.separatorChar,'/'))) {
                    if (!inSourceContent) {
                      hasFilesNotUnderSourceRoots[0] = true;
                    }
                    return true;
                  }

                  if (!Communicator.isHeaderFile(fileOrDir)) {
                    hasSomeCppFiles[0] = true;
                    Communicator.addSourceFileCommand(fileOrDir, Communicator.isCFile(fileOrDir) ? cModuleList:cppModuleList);

                    final VirtualFile parent = fileOrDir.getParent();

                    if (myparent != parent) {
                      VirtualFile sourceRootForFile = fileIndex.getSourceRootForFile(fileOrDir);
                      if (sourceRootForFile == null) sourceRootForFile = fileIndex.getContentRootForFile(fileOrDir);
                      sourceRoots.add(sourceRootForFile.getPath());
                      myparent = parent;
                    }
                  }
                }
              }
              return true;
            }
          }
        );
      }
    });

    final boolean settingsChanged = CppSupportLoader.SETTINGS_VERSION != settings.getSettingsVersion();

    if (hasFilesNotUnderSourceRoots[0] &&
       ( !settings.isWarnedAboutSourceFileOutOfSourceRoot() || settingsChanged)) {
      executeOnEdt(new Runnable() {

        public void run() {
          WarnAboutFilesOutOfSourceRootsDialog dialog = new WarnAboutFilesOutOfSourceRootsDialog(project);
          dialog.show();
          settings.setWarnedAboutSourceFileOutOfSourceRoot(true);
        }
      });
    }

    VirtualFile projectFile = null;
    String projectFilePath = settings.getProjectFile();

    if (projectFilePath != null) {
      final String projectFilePath2 = projectFilePath;
      projectFile = ApplicationManager.getApplication().runReadAction(new Computable<VirtualFile>() {
        public VirtualFile compute() {
          return LocalFileSystem.getInstance().findFileByPath(projectFilePath2);
        }
      });
    }

    if ((projectFile == null || settingsChanged) && !projectAndBuildFiles.isEmpty()) {
      final String chosenProjectFile[] = new String[1];

      executeOnEdt(new Runnable() {
        public void run() {
          String[] strings = settings.buildProjectAndBuildFilesSet().toArray(new String[projectAndBuildFiles.size() + 1]);
          LoadingCppProjectDialog dialog = new LoadingCppProjectDialog(project, strings);
          dialog.show();
          chosenProjectFile[0] = dialog.getSelectedProjectFile();
        }
      });

      if (chosenProjectFile[0] != null) {
        settings.setProjectFile(projectFilePath = chosenProjectFile[0]);
      }

      if (projectFilePath != null) {
        final String projectFilePath1 = projectFilePath;
        projectFile = ApplicationManager.getApplication().runReadAction(new Computable<VirtualFile>() {
          public VirtualFile compute() {
            return LocalFileSystem.getInstance().findFileByPath(projectFilePath1);
          }
        });
      }
    }

    CompilerProblem cproblem = addProjectSettings(project, instance, projectSettings, settings, sourceRoots, jniCommands, projectFile, true, cModuleList);
    CompilerProblem cpproblem = addProjectSettings(project, instance, projectSettings, settings, sourceRoots, jniCommands, projectFile, false, cppModuleList);

    if ((cpproblem != null || cproblem != null ) &&
        (hasSomeCppFiles[0] || hasFilesNotUnderSourceRoots[0])) {
      final boolean vcFilePathProblemFinal = cpproblem == CompilerProblem.VC_PROBLEM || cproblem == CompilerProblem.VC_PROBLEM;
      final String message =
        (vcFilePathProblemFinal ? "MS Visual Studio" : "Gcc") +
          " path is not valid, please specify it in Cpp plugin settings for std library symbols";

      if (!settings.isComplainedAboutGccOrMsVc()) {
        executeOnEdt(new Runnable() {

          public void run() {
            String title = (vcFilePathProblemFinal ? "MS VS" : "Gcc") + " Path Setting Problem";
            Notifications.Bus.notify(new Notification("C/C++ plugin", title, message, NotificationType.ERROR));
            settings.setComplainedAboutGccOrMsVc(true);
          }
        });
      } else {
        Notifications.Bus.notify(new Notification("C/C++ plugin", "Configuration problem", message, NotificationType.WARNING));
      }
    }

    systemSettings.append(projectSettings);
    systemSettings.append("unused\nstartup\n"+ Communicator.IDLE_COMMAND_NAME);
    systemSettings.append("\nhilight\nsymtab\ngc");

    return systemSettings.toString();
  }

  enum CompilerProblem {
    VC_PROBLEM, GCC_PROBLEM
  }

  private static CompilerProblem addProjectSettings(final Project project, CppSupportSettings instanceadd,
                                                    final StringBuilder projectSettings,
                                                    final CppSupportLoader settings,
                                                    Collection<String> sourceRoots,
                                                    StringBuilder jniCommands,
                                                    VirtualFile projectFile,
                                                    boolean cmode,
                                                    StringBuilder modules
                                                    ) {
    projectSettings.append("begin-config " + configNameByMode(cmode) +"\n");
    boolean usingMsVc = false;
    boolean usingGcc = false;

    final CppSupportSettings.CompilerSelectOptions compilerSelectOptions = settings.getCompilerOptions();

    CompilerProblem result = null;

    if (compilerSelectOptions == CppSupportSettings.CompilerSelectOptions.MSVC ||
        (compilerSelectOptions == CppSupportSettings.CompilerSelectOptions.AUTO && SystemInfo.isWindows)
       ) {
      usingMsVc = true;
      projectSettings.append(cmode ? "vc" : "vc++").append("\n");
      File file;

      if (instanceadd.getVsCDir() == null || !(file = new File(instanceadd.getVsCDir())).exists() || !file.isDirectory()) {
        result = CompilerProblem.VC_PROBLEM;
      }
    } else if (compilerSelectOptions == CppSupportSettings.CompilerSelectOptions.GCC ||
        compilerSelectOptions == CppSupportSettings.CompilerSelectOptions.CLANG || // TODO:
        (compilerSelectOptions == CppSupportSettings.CompilerSelectOptions.AUTO && !SystemInfo.isWindows)
       ) {
      usingGcc = true;

      final String command = cmode ? "gcc" : "g++";
      String realGccPath = instanceadd.getGccPath().replaceAll("gcc", command);
      String serverGccCommandArgument = instanceadd.getGccPath() != null ? " -c " + quote(realGccPath) : "";
      projectSettings.append(command).append(serverGccCommandArgument).append("\n");
      File gccPathFile = new File(realGccPath);

      if ((gccPathFile.isAbsolute() && (!gccPathFile.exists() || gccPathFile.isDirectory())) ||
        realGccPath.length() == 0) {
        result = CompilerProblem.GCC_PROBLEM;
      } else {
        try {
          Process process = Runtime.getRuntime().exec(realGccPath);
          process.destroy();
        } catch (IOException e) {
          result = CompilerProblem.GCC_PROBLEM;
        }
      }
    }

    final StringTokenizer autoMagicallyIncludedHeaderFiles = new StringTokenizer(settings.getAutomaticallyIncludedHeaderFiles(), CppSupportSettings.PATH_SEPARATOR);

    while (autoMagicallyIncludedHeaderFiles.hasMoreElements()) {
      projectSettings.append("include ").append(quote(autoMagicallyIncludedHeaderFiles.nextToken())).append("\n");
    }

    // TODO option
    for(String sourcePath:sourceRoots) {
      projectSettings.append("user-source-root ").append(quote(sourcePath)).append("\n");
    }

    final HashSet<String> includePathes = new LinkedHashSet<String>();
    final HashSet<String> preprocessorDefs = new HashSet<String>();
    final Set<String> usedIncludes = new HashSet<String>();

    final VirtualFile projectFile1 = projectFile;

    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        if (projectFile1 != null) {
          final FileInfo info = extractIncludesAndOtherStuffFromFile(projectFile1, project);
          includePathes.addAll(info.includes);
          preprocessorDefs.addAll(info.preprocessDefs);
        }

        final ProjectFileIndex index = ProjectRootManager.getInstance(project).getFileIndex();
        for(String includePath:includePathes) {
          final VirtualFile relativeFile = VfsUtil.findRelativeFile(includePath, null);
          if (relativeFile == null) continue;
          appendAdditionalIncludes(relativeFile.getPath().replace('/', File.separatorChar), projectSettings, index.getSourceRootForFile(relativeFile) == null, usedIncludes);
        }
        appendAdditionalIncludes(settings.getAdditionalSystemIncludeDirs(), projectSettings, true, usedIncludes);
      }
    });

    final Set<String> usedDefines = new HashSet<String>();
    for(String define:preprocessorDefs) appendDefines(define, projectSettings, usedDefines);
    final String projectDefines = settings.getAdditionalPreprocessorDefines();
    if (projectDefines != null) appendDefines(projectDefines, projectSettings, usedDefines);

    if (usingMsVc) {
      String vsDir = instanceadd.getVsCDir() + File.separatorChar;

      if (vsDir != null) {
        vsDir = quote(vsDir, false);
        projectSettings.append("system-include-path \"").append(vsDir).append("include\"\n");
        projectSettings.append("system-include-path \"").append(vsDir).append("PlatformSDK\\\\include\"\n");
        projectSettings.append("system-include-path \"").append(vsDir).append("atlmfc\\\\include\"\n");
      }
    }

    if (settings.isIncludeJavaIncludes()) {
      projectSettings.append(jniCommands.toString());
    }

    final String defines = instanceadd.getDefines();

    if (defines != null) {
      appendDefines(defines, projectSettings, usedDefines);
    }

    appendAdditionalIncludes(instanceadd.getAdditionalIncludeDirs(), projectSettings, true, usedIncludes);
    appendAdditionalIncludes(settings.getAdditionalIncludeDirs(), projectSettings, false, usedIncludes);

    projectSettings.append(createInspectionSettings(project));
    projectSettings.append("end-config\n");
    projectSettings.append(modules);
    return result;
  }

  public static String configNameByMode(boolean cmode) {
    return (cmode ? "cmode" : "cppmode");
  }

  public static void executeOnEdt(Runnable runnable) {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      ApplicationManager.getApplication().invokeAndWait(runnable, ModalityState.defaultModalityState());
    }
  }

  private static void appendDefines(String defines, StringBuilder builder, Set<String> usedDefines) {
    final StringTokenizer tokenizer = new StringTokenizer(defines, Communicator.PATH_DELIM);

    while(tokenizer.hasMoreTokens()) {
      final String s = tokenizer.nextToken();

      if (s.length() > 0 && !usedDefines.contains(s)) {
        usedDefines.add(s);
        builder.append("define ").append(s);
        if (s.indexOf(" ") == -1) {
          builder.append(" \"1\"");
        }
        builder.append("\n");
      }
    }
  }

  private static void appendAdditionalIncludes(String additionalIncludes, @NonNls StringBuilder builder, boolean system, Set<String> usedIncludes) {
    if (additionalIncludes != null) {
      final StringTokenizer tokenizer = new StringTokenizer(additionalIncludes,CppSupportSettings.PATH_SEPARATOR);

      while(tokenizer.hasMoreTokens()) {
        String s = tokenizer.nextToken();
        if (s.endsWith(File.separator)) s = s.substring(0, s.length() - 1);

        if (s.length() > 0 && !usedIncludes.contains(s)) {
          usedIncludes.add(s);
          builder.append(system ? "system-include-path ":"user-include-path ").append(quote(s)).append("\n");
        }
      }
    }
  }

  public static String quote(String str, boolean addQuotes) {
    final @NonNls StringBuilder buffer = new StringBuilder(str.length() + (addQuotes ? 2:0));
    if (addQuotes) buffer.append('"');

    for(int i = 0; i < str.length(); ++i) {
      final char ch = str.charAt(i);

      if (ch == '\\' || ch == '\"' || ch == '\'' || ch == '[' || ch == ']' || ch == '$') { buffer.append('\\'); }
      else if (ch == '\r' || ch == '\n' || ch == '\t') {
        buffer.append( ch == '\r' ? "\\r": ch == '\n' ? "\\n":"\\t");
        continue;
      }
      buffer.append(ch);
    }

    if (addQuotes) buffer.append('"');
    return buffer.toString();
  }

  public static String quote(String str) {
    return quote(str, true);
  }

  public static String unquote(String str) {
    str = StringUtil.stripQuotesAroundValue(str);
    final @NonNls StringBuilder buffer = new StringBuilder(str.length());

    for(int i = 0; i < str.length(); ++i) {
      final char ch = str.charAt(i);

      if (ch == '\\') {
        if (i + 1 < str.length()) {
          final char nextCh = str.charAt(i + 1);
          boolean recognized = false;

          if (nextCh == 'r') {
            // avoid putting \r since it causes line separator problems for IDEA
            //buffer.append('\r');
            recognized = true;
          }
          else if (nextCh == 'n') {
            buffer.append('\n');
            recognized = true;
          }
          else if (nextCh == '\\' || nextCh == '\"' || nextCh == '\'' || nextCh == '[' || nextCh == ']' || nextCh == '$') {
            buffer.append(nextCh);
            recognized = true;
          } else if (nextCh == 't') {
            buffer.append('\t');
            recognized = true;
          } else if (nextCh == Communicator.DELIMITER) {
            buffer.append(nextCh);
            recognized = true;
          }

          if (recognized) {
            ++i;
            continue;
          }
        }
      }

      buffer.append(ch);
    }

    return buffer.toString();
  }

  public static String fixVirtualFileName(String _fileName) {
    if (File.separatorChar == '\\') _fileName = _fileName.replace('/',File.separatorChar);
    return _fileName;
  }

  public static String getQuotedVirtualFileNameAsString(PsiFile file) {
    return getVirtualFileName(file.getVirtualFile());
  }

  public static String getVirtualFileName(VirtualFile vfile) {
    return BuildingCommandHelper.quote(fixVirtualFileName(vfile.getPath()));
  }

  public static void doScanFileContent(@NotNull VirtualFile file, @NotNull Processor<String> processor) {
    try {
      doScanFileContent(file.getInputStream(), processor);
    } catch(IOException e) {
      Communicator.LOG.error(e);
    }
  }

  public static void doScanFileContent(@NotNull InputStream in, @NotNull Processor<String> processor) throws IOException {
    LineNumberReader reader = null;
    try {
      reader = new LineNumberReader(new InputStreamReader(new BufferedInputStream(in)));
      String line;
      while((line = reader.readLine()) != null) {
        final boolean b = processor.process(line);
        if (!b) break;
      }
    } finally {
      if (reader != null) try { reader.close(); } catch(IOException e) { Communicator.LOG.error(e); }
    }
  }

  static String createInspectionSettings(Project project) {
    final CppSupportLoader loader = CppSupportLoader.getInstance(project);
    final StringBuilder options = new StringBuilder();

    CppHighlightingSettings highlightingSettings = loader.getHighlightingSettings();
    appendOption(options, highlightingSettings.toReportImplicitCastToBool(), "implicit_cast_to_bool");
    appendOption(options, highlightingSettings.toReportNameNeverReferenced(), "name_never_referenced");
    appendOption(options, highlightingSettings.toReportNameUsedOnce(), "name_used_only_once");
    appendOption(options, highlightingSettings.toReportRedundantCast(), "redundant_cast");
    appendOption(options, highlightingSettings.toReportRedundantQualifier(), "redundant_qualifier");
    appendOption(options, highlightingSettings.toReportStaticCallFromInstance(), "static_call_from_value");
    appendOption(options, highlightingSettings.toReportUnneededBraces(), "redundant_brackets");
    appendOption(options, highlightingSettings.toReportDuplicatedSymbols(), "report_multiple_defs");
    options.append("option +out_errorcodes\n");

    return options.toString();    // it is important to issue idle to ensure caches built
  }

  static void appendOption(StringBuilder options, boolean state, String optionId) {
    options.append("warning ").append(state ? '+':'-').append(optionId).append("\n");
  }

  public interface ProjectAndBuildFileIncludeProcessor extends Processor<String> {
    @NotNull Collection<String> getIncludes();
    @NotNull Collection<String> getPreprocessorDefinitions();
  }

  static class FileInfo {
    final Collection<String> includes;
    final Collection<String> preprocessDefs;

    FileInfo() {
      this(Collections.<String>emptyList(),Collections.<String>emptyList());
    }

    FileInfo(Collection<String> _includes, Collection<String> _preprocessDefs) {
      includes = _includes;
      preprocessDefs = _preprocessDefs;
    }
  }

  private static @NotNull FileInfo extractIncludesAndOtherStuffFromFile(final @NotNull VirtualFile file, Project project) {
    ProjectAndBuildFileIncludeProcessor processor;
    String activeConfiguration = CppSupportLoader.getInstance(project).getActiveConfiguration();

    VirtualFile sourceRootForFile = ProjectRootManager.getInstance(project).getFileIndex().getSourceRootForFile(file);
    final String basePath = sourceRootForFile != null ? sourceRootForFile.getPath():"";

    class PathVarResolver {
      @NotNull String getPathVariableValue(@NotNull String varName) {
        if (BaseProjectAndBuildFileIncludeProcessor.PROJECT_DIR_VAR_NAME.equals(varName)) return file.getParent().getPath();
        return "!"+basePath+"!";
      }
    }

    final PathVarResolver resolver = new PathVarResolver();

    if (Communicator.DSP_EXTENSION.equals(file.getExtension())) {
      processor = new DspProjectAndBuildFileIncludeProcessor(activeConfiguration) {
        public String getPathVariableValue(String varName) {
          return resolver.getPathVariableValue(varName);
        }
      };
    } else if (Communicator.VCPROJ_EXTENSION.equals(file.getExtension())) {
      processor = new VCProjProjectAndBuildFileIncludeProcessor(activeConfiguration) {
        public String getPathVariableValue(String varName) {
          return resolver.getPathVariableValue(varName);
        }
      };
    } else {
      processor = null;
    }

    if (processor == null) return new FileInfo();

    doScanFileContent(file, processor);
    return new FileInfo(processor.getIncludes(),processor.getPreprocessorDefinitions());
  }

  public static abstract class BaseProjectAndBuildFileIncludeProcessor implements ProjectAndBuildFileIncludeProcessor {
    static final Pattern pathVariablePattern = Pattern.compile("\\$\\((\\w+)\\)");
    @NonNls
    public static final String PROJECT_DIR_VAR_NAME = "ProjectDir";

    private String resolvePathVariables(String includes) {
      final Matcher matcher = pathVariablePattern.matcher(includes);
      StringBuffer buffer = new StringBuffer();

      while(matcher.find()) {
        String replacement = getPathVariableValue(matcher.group(1));
        boolean addSeparator = false;
        int end = matcher.end(1) + 1;
        if (end < includes.length() && includes.charAt(end) != File.separatorChar && !replacement.endsWith(File.separator)) {
          addSeparator = true;
        }
        matcher.appendReplacement(buffer, replacement);
        if (addSeparator) {
          buffer.append(File.separatorChar);
        }
      }
      matcher.appendTail(buffer);
      includes = buffer.toString();

      boolean startsWithTwoDots;
      if ((startsWithTwoDots = includes.startsWith("..")) || includes.startsWith(".")) {
        final StringBuilder builder = new StringBuilder(includes);
        final int index = (startsWithTwoDots ? 2 : 1);
        int remainingLen = includes.length() - index;
        final String s = startsWithTwoDots ? File.separatorChar + ".." + (remainingLen > 0 && includes.charAt(index) != File.separatorChar && includes.charAt(index) != '/' ? File.separatorChar : "") : "";

        builder.replace(
          0,
          index,
          getPathVariableValue(PROJECT_DIR_VAR_NAME) + s
        );
        includes = builder.toString();
      }

      includes = includes.replace('/', File.separatorChar).replace('\\',File.separatorChar);
      return includes;
    }

    private final HashSet<String> includePathes = new LinkedHashSet<String>();
    private final HashSet<String> preprocessorDefs = new HashSet<String>();

    protected final String activeConfiguration;
    protected boolean inActiveConfiguration = true;

    public BaseProjectAndBuildFileIncludeProcessor(String _activeConfiguration) {
      activeConfiguration = _activeConfiguration;
    }

    public Collection<String> getIncludes() {
      return includePathes;
    }

    public Collection<String> getPreprocessorDefinitions() {
      return preprocessorDefs;
    }

    public abstract @NotNull String getPathVariableValue(@NotNull String varName);

    protected void addOneInclude(@NotNull String basePath) {
      basePath = resolvePathVariables(basePath);
      if (basePath.endsWith(File.separator)) basePath = basePath.substring(0,basePath.length() - 1);
      if (basePath.length() == 0) return;

      includePathes.add(basePath);
    }

    protected void addOneDefine(@NotNull String define) {
      if (define.length() > 0) preprocessorDefs.add(define);
    }
  }

  public abstract static class VCProjProjectAndBuildFileIncludeProcessor extends BaseProjectAndBuildFileIncludeProcessor {
    final static Pattern includePattern = Pattern.compile("AdditionalIncludeDirectories=\"(.*)\"");
    final static Pattern preprocessorDefsPattern = Pattern.compile("PreprocessorDefinitions=\"(.*)\"");
    private boolean mySeenCompilerSettings;
    private String previousLine;

    public VCProjProjectAndBuildFileIncludeProcessor(String activeConfiguration) {
      super(activeConfiguration);
    }

    public boolean process(String s) {
      try {
        if (s.indexOf("Name=") != -1) {
          if (activeConfiguration != null) {
            if (previousLine != null && previousLine.indexOf("<Configuration") != -1) {
              inActiveConfiguration = s.indexOf("\"" + activeConfiguration + "|") != -1;
            }
            if (!inActiveConfiguration) return true;
          }

          if (s.indexOf("\"VCCLCompilerTool\"") != -1) {
            mySeenCompilerSettings = true;
          } else {
            mySeenCompilerSettings = false;
          }
        } else if (mySeenCompilerSettings && s.indexOf("AdditionalIncludeDirectories=") != -1) {
          final Matcher matcher = includePattern.matcher(s);
          if (matcher.find()) {
            String includes = matcher.group(1);
            final StringTokenizer t = new StringTokenizer(includes, ",");

            while(t.hasMoreElements()) {
              String include = t.nextToken();
              include = include.replaceAll("&quot;","");
              final StringTokenizer tokenizer = new StringTokenizer(include, Communicator.PATH_DELIM);
              while(tokenizer.hasMoreElements()) addOneInclude(tokenizer.nextToken());
            }

          }
        } else if (mySeenCompilerSettings && s.indexOf("PreprocessorDefinitions=") != -1) {
          final Matcher matcher = preprocessorDefsPattern.matcher(s);
          if (matcher.find()) {
            String defines = matcher.group(1);
            final StringTokenizer t = new StringTokenizer(defines, Communicator.PATH_DELIM);

            while(t.hasMoreElements()) {
              addOneDefine(t.nextToken());
            }
          }
        }
        return true;
      } finally {
        previousLine = s;
      }
    }
  }

  public abstract static class DspProjectAndBuildFileIncludeProcessor extends BaseProjectAndBuildFileIncludeProcessor {
    static final Pattern includePattern = Pattern.compile("/I (\\S*)");
    static final Pattern definePattern = Pattern.compile("/D (\\S*)");

    public DspProjectAndBuildFileIncludeProcessor(String activeConfiguration) {
      super(activeConfiguration);
    }

    public boolean process(String s) {
      if (activeConfiguration != null) {
        if (s.indexOf("IF  \"$(CFG)\"") != -1) {
          inActiveConfiguration = s.endsWith( " " + activeConfiguration + "\"");
        }
        if (!inActiveConfiguration) return true;
      }

      if (s.startsWith("# ADD CPP") || s.startsWith("# ADD BASE CPP")) {
        Matcher matcher = includePattern.matcher(s);

        while(matcher.find()) {
          String path = matcher.group(1);
          if (StringUtil.startsWithChar(path, '\"') && StringUtil.endsWithChar(path, '\"')) {
            String basePath = StringUtil.stripQuotesAroundValue(path);
            addOneInclude(basePath);
          }
        }

        matcher = definePattern.matcher(s);

        while(matcher.find()) {
          addOneDefine(StringUtil.stripQuotesAroundValue(matcher.group(1)));
        }
      }
      return true;
    }
  }
}
