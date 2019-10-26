// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.advancedtools.cpp.build.BaseBuildHandler;
import com.advancedtools.cpp.build.BuildTarget;
import com.advancedtools.cpp.commands.ChangedCommand;
import com.advancedtools.cpp.commands.FindSymbolsCommand;
import com.advancedtools.cpp.commands.StringCommand;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.advancedtools.cpp.communicator.Communicator;
import com.advancedtools.cpp.facade.EnvironmentFacade;
import com.advancedtools.cpp.facade.ExtendedPlatformServices;
import com.advancedtools.cpp.makefile.MakefileLanguage;
import com.advancedtools.cpp.navigation.CppSymbolContributor;
import com.advancedtools.cpp.settings.*;
import com.advancedtools.cpp.usages.OurUsage;
import com.advancedtools.cpp.utils.StringTokenizerIterable;
import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.extensions.ExtensionsArea;
import com.intellij.openapi.extensions.LoadingOrder;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.*;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.util.Alarm;
import com.intellij.util.Processor;
import com.intellij.util.text.CharArrayUtil;
import com.intellij.util.ui.ErrorTreeView;
import gnu.trove.THashSet;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

/**
 * @author maxim
 */
public class CppSupportLoader implements ProjectComponent, JDOMExternalizable {
  public static final @NonNls String HPP_EXTENSION = "hpp";
  public static final @NonNls String HXX_EXTENSION = "hxx";
  public static final @NonNls String TCC_EXTENSION = "tcc";
  public static final @NonNls String INL_EXTENSION = "inl";
  public static final @NonNls String INC_EXTENSION = "inc";
  public static final @NonNls String HI_EXTENSION = "hi";
  public static final @NonNls String CPP_EXTENSION = "cpp";
  public static final @NonNls String CC_EXTENSION = "cc";
  public static final @NonNls String C_EXTENSION = "c";
  public static final @NonNls String H_EXTENSION = "h";
  public static final @NonNls String INO_EXTENSION = "ino";
  private static Key<DocumentListener> ourListenerKey = Key.create("cpp.document.listener");

  public static final String[] extensions = new String[]{CPP_EXTENSION, CC_EXTENSION, C_EXTENSION, INO_EXTENSION, H_EXTENSION, HPP_EXTENSION, TCC_EXTENSION, INL_EXTENSION, HI_EXTENSION, INC_EXTENSION, HXX_EXTENSION};

  private Project project;
  private DocumentListener myDocumentListener;
  private VirtualFileListener myFileListener;
  private EditorFactoryListener myEditorFactoryListener;
  private static CppSymbolContributor myConstantContributor;

  private static CppSymbolContributor myMacrosContributor;

  private static final @NonNls String JAVA_INCLUDES_KEY = "javaIncludes";
  private static final @NonNls String FILE_KEY = "ignored-file";
  private static final @NonNls String ADDITIONAL_INCLUDE_DIR_KEY = "include-dir";
  private static final @NonNls String ADDITIONAL_SYSTEM_INCLUDE_DIR_KEY = "system-include-dir";
  private static final @NonNls String WARNED_ABOUT_FILE_OUT_OF_SOURCE_ROOT_KEY = "warnedAboutFileOutOfSourceRoot";

  private static final @NonNls String SETTINGS_VERSION_KEY = "version";

  private Set<String> myIgnoredFiles = new TreeSet<String>();
  private String myAdditionalIncludeDirs = "";
  private String myAdditionalSystemIncludeDirs = "";
  private boolean myWarnedAboutSourceFileOutOfSourceRoot;

  private final CppHighlightingSettings myHighlightingSettings = new CppHighlightingSettings();

  private int mySettingsVersion = SETTINGS_VERSION;
  public static final int SETTINGS_VERSION = 3;

  private String myActiveConfiguration;
  private String myProjectFile;
  private String myAutomaticallyIncludedHeaderFiles = "";
  private String myAdditionalPreprocessorDefines;
  private String myLastBuildAction = BuildTarget.DEFAULT_BUILD_ACTION;
  private String myAdditionalCommandLineBuildParameters;

  private boolean myIncludeJavaIncludes = true;
  private String myAdditionalCompileParameters;
  private boolean myIncludeProjectSettings;
  private long myReportedProblemAboutServerProblemStamp = -1;

  private static final @NonNls String FILE_PATH_KEY = "path";

  private static final @NonNls String ACTIVE_CONFIGURATION_KEY = "activeConfiguration";
  private static final @NonNls String LAST_BUILD_ACTION_KEY = "lastBuildAction";
  private static final @NonNls String ADDITIONAL_COMMAND_LINE_BUILD_PARAMETERS_KEY = "additionalCommandLineBuildParameters";
  private static final @NonNls String ADDITIONAL_COMPILE_PARAMETERS_KEY = "additionalCompileParameters";
  private static final @NonNls String INCLUDE_PROJECT_SETTINGS_KEY = "includeProjectSettings";

  private static final @NonNls String ADDITIONAL_PREPROCESSOR_DEFINITIONS_KEY = "additionalPreprocessorDefs";
  private static final @NonNls String AUTOMATICALLY_INCLUDED_HEADER_FILES_KEY = "automaticallyIncludedHeaderFiles";
  private static final @NonNls String PROJECT_FILE_KEY = "currentProject";
  private static final @NonNls String SERVER_STAMP_ERROR_REPORTED = "serverStampWhenFatalErrorReported";
  private static final @NonNls String SDK_ERROR_REPORTED = "sdkErrorReported";

  private LinkedHashSet<VirtualFile> projectAndBuildFilesSet = new LinkedHashSet<VirtualFile>();
  private static CppSupportLoader instance;
  private ErrorTreeView errorTreeView;
  private boolean errorViewIsFilling;
  public static final Icon ourCppIcon = IconLoader.findIcon("c_file_obj.gif");
  public static final Icon ourIncludeIcon = IconLoader.findIcon("include_obj.gif");
  public static final Icon ourMakefileIcon = IconLoader.findIcon("makefile.gif");
  public static final Icon ourSdkIcon = ourCppIcon;
  public static final Icon ourModuleIcon = ourCppIcon;
  public static final Icon ourBigModuleIcon = ourCppIcon;

  private boolean complainedAboutGccOrMsVc;

  public boolean isComplainedAboutGccOrMsVc() {
    return complainedAboutGccOrMsVc;
  }

  public void setComplainedAboutGccOrMsVc(boolean complainedAboutGccOrMsVc) {
    this.complainedAboutGccOrMsVc = complainedAboutGccOrMsVc;
  }

  // Server problems
  // TODO: usage text contains new lines

  //------------
  // find parent by no should give info on class context

  public CppSupportLoader(Project _project) {
    project = _project;
  }

  public void projectOpened() {
    initFileType(project);

    if (EnvironmentFacade.isJavaIde()) {
      ExtendedPlatformServices.registerCompilerStuff(project);
    }

    myDocumentListener = new MyDocumentListener();

    EditorFactory.getInstance().addEditorFactoryListener(
      myEditorFactoryListener = new EditorFactoryListener() {
        public void editorCreated(EditorFactoryEvent event) {
          if (this != myEditorFactoryListener) return; // disposed
          final Editor editor = event.getEditor();
          if (editor.getProject() != project) return;
          final Document document = editor.getDocument();

          if (isAcceptableDocument(document) && document.getUserData(ourListenerKey) == null) {
            document.addDocumentListener(myDocumentListener);
            document.putUserData(ourListenerKey, myDocumentListener);
          }
        }

        public void editorReleased(EditorFactoryEvent event) {
          if (this != myEditorFactoryListener) return; // disposed
          final Editor editor = event.getEditor();
          if (editor.getProject() != project) return;
          final Document document = editor.getDocument();

          removeDocumentListener(document);
        }
      }
    );

    VirtualFileManager.getInstance().addVirtualFileListener(myFileListener = new VirtualFileListener() {
      public void fileCreated(VirtualFileEvent virtualFileEvent) {
        if (this != myFileListener) return; // disposed
        VirtualFile file = virtualFileEvent.getFile();

        doFileCreated(file);
      }

      public void fileDeleted(VirtualFileEvent virtualFileEvent) {}

      public void contentsChanged(VirtualFileEvent virtualFileEvent) {
        updateModificationCount(virtualFileEvent, true);
      }

      public void beforeFileDeletion(VirtualFileEvent virtualFileEvent) {
        VirtualFile file = virtualFileEvent.getFile();
        doFileRemoved(file);
        if (isCppFile(file)) {
          myIgnoredFiles.remove(file.getPath());
        }
      }

      public void beforePropertyChange(VirtualFilePropertyEvent virtualFilePropertyEvent) {
        VirtualFile file = virtualFilePropertyEvent.getFile();
        if (isCppFile(file) && VirtualFile.PROP_NAME.equals(virtualFilePropertyEvent.getPropertyName())) {
          if(myIgnoredFiles.remove(file.getPath())) {
            myIgnoredFiles.add(file.getParent().getPath() + '/' + virtualFilePropertyEvent.getNewValue());
          }
        }
      }

      public void beforeContentsChange(VirtualFileEvent virtualFileEvent) {}

      public void propertyChanged(VirtualFilePropertyEvent virtualFilePropertyEvent) {
        VirtualFile file = virtualFilePropertyEvent.getFile();

        if (isCppFile(file) && VirtualFile.PROP_NAME.equals(virtualFilePropertyEvent.getPropertyName())) {
          if (!myIgnoredFiles.contains(file.getPath())) {
            Communicator.getInstance(project).onFileCreated(file);
          }
        }

        updateModificationCount(virtualFilePropertyEvent, false);
      }

      public void beforeFileMovement(VirtualFileMoveEvent virtualFileMoveEvent) {
        VirtualFile file = virtualFileMoveEvent.getFile();
        doFileRemoved(file);
        if (isCppFile(file)) {
          if(myIgnoredFiles.remove(file.getPath())) {
            myIgnoredFiles.add(virtualFileMoveEvent.getNewParent().getPath() + '/' + file.getName());
          }
        }
      }

      public void fileMoved(VirtualFileMoveEvent event) {
        doFileCreated(event.getFile());
      }

      public void fileCopied(VirtualFileCopyEvent virtualFileCopyEvent) {
        //To change body of implemented methods use File | Settings | File Templates.
      }

      private void updateModificationCount(VirtualFileEvent event, boolean sendReload) {
        if (this != myFileListener) return; // disposed
        VirtualFile file = event.getFile();

        if (isCppFile(file)) {
          PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
          if (psiFile != null && isOurFile(psiFile)) {
            final Communicator communicator = Communicator.getInstance(project);
            communicator.incModificationCount();

            if (sendReload) {
              communicator.sendCommand(
                new StringCommand(
                  "reload -n " + communicator.getModificationCount() + " " + BuildingCommandHelper.quote(file.getPresentableUrl())
                )
              );
            }
          }
        }
      }
    });
  }

  private static boolean isCppFile(VirtualFile file) {
    return !file.isDirectory() && file.getFileType() == CPP_FILETYPE;
  }

  private void removeDocumentListener(Document document) {
    if (isAcceptableDocument(document) && document.getUserData(ourListenerKey) != null) {
      document.removeDocumentListener(myDocumentListener);
      document.putUserData(ourListenerKey, null);
    }
  }

  private void doFileCreated(VirtualFile file) {
    if (isCppFile(file)) {
      PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
      if (psiFile != null && isOurFile(psiFile)) {
        Communicator.getInstance(project).onFileCreated(file);
      }
    }
  }

  private void doFileRemoved(VirtualFile file) {
    if (isCppFile(file)) {
      PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
      if (psiFile != null && isOurFile(psiFile)) {
        Communicator.getInstance(project).onFileRemoved(file);
      }
    }
  }

  private boolean isAcceptableDocument(Document document) {
    final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
    return psiFile != null && CPP_FILETYPE == psiFile.getFileType();
  }

  private boolean isOurFile(PsiFile psiFile) {
    final ProjectFileIndex index = ProjectRootManager.getInstance(project).getFileIndex();

    return isInSource(psiFile.getVirtualFile(), index);
  }

  public static final Language CPP_LANGUAGE = new CppLanguage();
  public static final Language MAKEFILE_LANGUAGE = new MakefileLanguage();
  public static final LanguageFileType CPP_FILETYPE = new LanguageFileType(CPP_LANGUAGE) {
      @NotNull
      @NonNls
      public String getName() {
        return "C/C++";
      }

      @NotNull
      public String getDescription() {
        return "Cpp file support";
      }

      @NotNull
      @NonNls
      public String getDefaultExtension() {
        return CPP_EXTENSION;
      }

      @Nullable
      public Icon getIcon() {
        return ourCppIcon;
      }
    };

  public static final LanguageFileType MAKE_FILETYPE = new LanguageFileType(MAKEFILE_LANGUAGE) {
      @NotNull
      @NonNls
      public String getName() {
        return "Makefile";
      }

      @NotNull
      public String getDescription() {
        return "Traditional makefiles";
      }

      @NotNull
      @NonNls
      public String getDefaultExtension() {
        return "";
      }

      @Nullable
      public Icon getIcon() {
        return ourMakefileIcon;
      }
    };
  public static final IFileElementType CPP_FILE = new IFileElementType(CPP_LANGUAGE);

  private final static Set<String> filesWithEmptyExtsKnownToBeCpp = new THashSet<String>();

  static {
    final @NonNls String listOfKnownCppFiles = "algorithm\n" + "bitset\n" + "cassert\n" + "cctype\n" + "cerrno\n" +
            "cfloat\n" + "ciso646\n" + "climits\n" +"clocale\n" + "cmath\n" + "complex\n" + "csetjmp\n" + "csignal\n" +
            "cstdarg\n" + "cstddef\n" + "cstdio\n" + "cstdlib\n" + "cstring\n" + "ctime\n" + "cwchar\n" + "cwctype\n" +
            "deque\n" + "exception\n" + "fstream\n" + "functional\n" + "iomanip\n" + "ios\n" + "iosfwd\n" + "iostream\n" +
            "istream\n" + "iterator\n" + "limits\n" + "list\n" + "locale\n" + "map\n" + "memory\n" + "new\n" +
            "numeric\n" + "ostream\n" + "queue\n" + "set\n" + "sstream\n" + "stack\n" + "stdexcept\n" + "streambuf\n" +
            "string\n" + "typeinfo\n" + "utility\n" + "valarray\n" + "vector\n" + "hash_map\n" + "hash_set\n" +
            "memory\n" + "numeric\n" + "rb_tree\n" + "rope\n" + "slist\n" + "xcomplex\n" + "xdebug\n" + "xhash\n" +
            "xiosbase\n" + "xlocale\n" + "xlocinfo\n" + "xlocmes\n" + "xlocmon\n" + "xlocnum\n" + "xloctime\n" +
            "xmemory\n" + "xstddef\n" + "xstring\n" + "xtree\n" + "xutility";
    final StringTokenizer tokenizer = new StringTokenizer(listOfKnownCppFiles, "\n");

    while (tokenizer.hasMoreElements()) {
      filesWithEmptyExtsKnownToBeCpp.add(tokenizer.nextToken());
    }
  }

  public static boolean isKnownEmptyExtensionFile(@NotNull String fileName) {
    return filesWithEmptyExtsKnownToBeCpp.contains(SystemInfo.isFileSystemCaseSensitive ?fileName:fileName.toLowerCase());
  }

  public static Set<String> filesWithEmptyExtensions() {
    return filesWithEmptyExtsKnownToBeCpp;
  }

  public static final Key<OurUsage> ourUsageKey = Key.create("usage.key");
  private static boolean initialized;

  private static void initFileType(Project project) {
    if (initialized) return;
    initialized = true;

    EnvironmentFacade.getInstance().runWriteActionFromComponentInstantiation(new Runnable() {
      public void run() {
        myConstantContributor = new CppSymbolContributor(FindSymbolsCommand.TargetTypes.CONSTANTS);
        myMacrosContributor = new CppSymbolContributor(FindSymbolsCommand.TargetTypes.MACROS);
      }
    });
    Communicator.getInstance(project);
  }

  public void projectClosed() {
    EditorFactory.getInstance().removeEditorFactoryListener(myEditorFactoryListener);
    myEditorFactoryListener = null;

    VirtualFileManager.getInstance().removeVirtualFileListener(myFileListener);
    myFileListener = null;

    for(Editor editor:EditorFactory.getInstance().getAllEditors()) {
      if (editor.getProject() == project) {
        removeDocumentListener(editor.getDocument());
      }
    }
    myDocumentListener = null;
  }

  @NonNls
  public String getComponentName() {
    return "CppTools.Loader";
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  public static Editor findEditor(Project project, VirtualFile newFile) {
    final FileEditor selectedEditor = FileEditorManager.getInstance(project).getSelectedEditor(newFile);
    return selectedEditor instanceof TextEditor ? ((TextEditor)selectedEditor).getEditor() : null;
  }

  public void readExternal(Element element) throws InvalidDataException {
    myIgnoredFiles.clear();

    // old compatibility code
    boolean usedCompatibility = false;

    for (Object anIgnoredFile : element.getChildren(FILE_KEY)) {
      if (!(anIgnoredFile instanceof Element)) continue;
      Element fileElement = (Element) anIgnoredFile;
      final String name = fileElement.getAttributeValue("name");

      if (name != null && name.length() > 0) {
        myIgnoredFiles.add(name);
        usedCompatibility = true;
      }
    }

    String additionalIncludes = element.getAttributeValue(ADDITIONAL_INCLUDE_DIR_KEY);
    myAdditionalIncludeDirs = (additionalIncludes != null) ? additionalIncludes:"";
    usedCompatibility |= additionalIncludes != null;

    additionalIncludes = element.getAttributeValue(ADDITIONAL_SYSTEM_INCLUDE_DIR_KEY);
    myAdditionalSystemIncludeDirs = (additionalIncludes != null) ? additionalIncludes:"";
    usedCompatibility |= additionalIncludes != null;

    if (!usedCompatibility) {
      readChildren(element, FILE_KEY, FILE_PATH_KEY, new Processor<String>() {
        public boolean process(String s) {
          myIgnoredFiles.add(s);
          return true;
        }
      });

      final StringBuilder builder = new StringBuilder();
      Processor<String> collectingPathProcessor = new Processor<String>() {
        public boolean process(String s) {
          if (builder.length() > 0) builder.append(CppSupportSettings.PATH_SEPARATOR);
          builder.append(s);
          return true;
        }
      };

      readChildren(element, ADDITIONAL_INCLUDE_DIR_KEY, FILE_PATH_KEY, collectingPathProcessor);

      myAdditionalIncludeDirs = builder.toString();
      builder.setLength(0);

      readChildren(element, ADDITIONAL_SYSTEM_INCLUDE_DIR_KEY, FILE_PATH_KEY, collectingPathProcessor);
      myAdditionalSystemIncludeDirs = builder.toString();
    }

    myHighlightingSettings.readExternal(element);

    String s = element.getAttributeValue(WARNED_ABOUT_FILE_OUT_OF_SOURCE_ROOT_KEY);
    if (s != null) myWarnedAboutSourceFileOutOfSourceRoot = Boolean.parseBoolean(s);

    s = element.getAttributeValue(SETTINGS_VERSION_KEY);
    if (s != null) {
      try {
        mySettingsVersion = Integer.parseInt(s);
      } catch (NumberFormatException e) {}
    }

    s = element.getAttributeValue(ACTIVE_CONFIGURATION_KEY);
    if (s != null) myActiveConfiguration = s;

    s = element.getAttributeValue(LAST_BUILD_ACTION_KEY);
    if (s != null) myLastBuildAction = s;

    s = element.getAttributeValue(ADDITIONAL_COMMAND_LINE_BUILD_PARAMETERS_KEY);
    if (s != null) myAdditionalCommandLineBuildParameters = s;

    s = element.getAttributeValue(ADDITIONAL_COMPILE_PARAMETERS_KEY);
    if (s != null) myAdditionalCompileParameters = s;
    else myAdditionalCompileParameters = null;

    s = element.getAttributeValue(INCLUDE_PROJECT_SETTINGS_KEY);
    if (s != null && Boolean.parseBoolean(s)) myIncludeProjectSettings = true;
    else myIncludeProjectSettings = false;

    s = element.getAttributeValue(ADDITIONAL_PREPROCESSOR_DEFINITIONS_KEY);
    if (s != null) myAdditionalPreprocessorDefines = s;

    s = element.getAttributeValue(AUTOMATICALLY_INCLUDED_HEADER_FILES_KEY);
    if (s != null) myAutomaticallyIncludedHeaderFiles = s;

    s = element.getAttributeValue(PROJECT_FILE_KEY);
    if (s != null) myProjectFile = s;

    s = element.getAttributeValue(SERVER_STAMP_ERROR_REPORTED);
    if (s != null) {
      try {
        myReportedProblemAboutServerProblemStamp = Long.parseLong(s);
      } catch (NumberFormatException ex) { myReportedProblemAboutServerProblemStamp = -1; }
    }

    s = element.getAttributeValue(SDK_ERROR_REPORTED);
    if (s != null) {
      complainedAboutGccOrMsVc = Boolean.parseBoolean(s);
    }

    s = element.getAttributeValue(COMPILER_SELECT_KEY);
    if (s != null) myCompilerOptions = CppSupportSettings.CompilerSelectOptions.valueOf(s);

    s = element.getAttributeValue(JAVA_INCLUDES_KEY);
    if (s != null) myIncludeJavaIncludes = Boolean.parseBoolean(s);
  }

  private void readChildren(Element element, String subTagName, String attrName, Processor<String> processor) {
    for (Object anIgnoredFile : element.getChildren(subTagName)) {
      if (!(anIgnoredFile instanceof Element)) continue;
      Element fileElement = (Element) anIgnoredFile;
      final String name = fileElement.getAttributeValue(attrName);
      if (name != null && name.length() > 0) processor.process(name);
    }
  }

  public void writeExternal(Element element) throws WriteExternalException {
    serializeFileList(myIgnoredFiles, element, FILE_KEY);
    if (myAdditionalIncludeDirs.length() > 0) {
      serializeFileList(new StringTokenizerIterable(myAdditionalIncludeDirs, CppSupportSettings.PATH_SEPARATOR), element, ADDITIONAL_INCLUDE_DIR_KEY);
    }

    if (myAdditionalSystemIncludeDirs.length() > 0) {
      serializeFileList(new StringTokenizerIterable(myAdditionalSystemIncludeDirs, CppSupportSettings.PATH_SEPARATOR), element, ADDITIONAL_SYSTEM_INCLUDE_DIR_KEY);
    }

    myHighlightingSettings.writeExternal(element);

    if (myWarnedAboutSourceFileOutOfSourceRoot) element.setAttribute(WARNED_ABOUT_FILE_OUT_OF_SOURCE_ROOT_KEY, "true");
    element.setAttribute(SETTINGS_VERSION_KEY, Integer.toString(SETTINGS_VERSION));

    if (myActiveConfiguration != null) element.setAttribute(ACTIVE_CONFIGURATION_KEY, myActiveConfiguration);
    if (myLastBuildAction != null && !BuildTarget.DEFAULT_BUILD_ACTION.equals(myLastBuildAction)) element.setAttribute(LAST_BUILD_ACTION_KEY, myLastBuildAction);
    if (myAdditionalCommandLineBuildParameters != null) element.setAttribute(ADDITIONAL_COMMAND_LINE_BUILD_PARAMETERS_KEY, myAdditionalCommandLineBuildParameters);

    if (myIncludeProjectSettings) element.setAttribute(INCLUDE_PROJECT_SETTINGS_KEY, "true");
    if (myAdditionalCompileParameters != null) element.setAttribute(ADDITIONAL_COMPILE_PARAMETERS_KEY, myAdditionalCompileParameters);
    if (myProjectFile != null) element.setAttribute(PROJECT_FILE_KEY, myProjectFile);
    if (myReportedProblemAboutServerProblemStamp != -1) {
      element.setAttribute(SERVER_STAMP_ERROR_REPORTED,String.valueOf(myReportedProblemAboutServerProblemStamp));
    }
    if (complainedAboutGccOrMsVc) {
      element.setAttribute(SDK_ERROR_REPORTED, "true");
    }
    if (myAdditionalPreprocessorDefines != null) {
      element.setAttribute(ADDITIONAL_PREPROCESSOR_DEFINITIONS_KEY, myAdditionalPreprocessorDefines);
    }

    if (myAutomaticallyIncludedHeaderFiles.length() > 0) {
      element.setAttribute(AUTOMATICALLY_INCLUDED_HEADER_FILES_KEY, myAutomaticallyIncludedHeaderFiles);
    }
    element.setAttribute(COMPILER_SELECT_KEY, myCompilerOptions.toString());

    if (!myIncludeJavaIncludes) element.setAttribute(JAVA_INCLUDES_KEY, "false");
  }

  private static void serializeFileList(Iterable<String> myIgnoredFiles, Element element, String key) {
    for(String ignoredFile: myIgnoredFiles) {
      final Element fileElement = new Element(key);
      fileElement.setAttribute(FILE_PATH_KEY, ignoredFile);
      element.addContent(fileElement);
    }
  }

  public Set<String> getIgnoredFilesSet() {
    return myIgnoredFiles;
  }

  public String getAdditionalIncludeDirs() {
    return myAdditionalIncludeDirs;
  }

  public String getAdditionalSystemIncludeDirs() {
    return myAdditionalSystemIncludeDirs;
  }

  public boolean isWarnedAboutSourceFileOutOfSourceRoot() {
    return myWarnedAboutSourceFileOutOfSourceRoot;
  }

  public void setWarnedAboutSourceFileOutOfSourceRoot(boolean value) {
    myWarnedAboutSourceFileOutOfSourceRoot = value;
  }

  public static CppSupportLoader getInstance(Project project) {
    CppSupportLoader loader = project.getComponent(CppSupportLoader.class);
    if (loader == null && ApplicationManager.getApplication().isUnitTestMode() && instance != null) {
      loader = instance;
    }
    return loader;
  }

  public static void setInstance(CppSupportLoader loader) {
    assert ApplicationManager.getApplication().isUnitTestMode();
    instance = loader;
  }

  public Set<VirtualFile> getProjectAndBuildFilesSet() {
    return projectAndBuildFilesSet;
  }

  public ErrorTreeView getErrorTreeView() {
    return errorTreeView;
  }

  public void setErrorTreeView(ErrorTreeView errorTreeView) {
    this.errorTreeView = errorTreeView;
  }

  public void setErrorViewIsFilling(boolean value) {
    errorViewIsFilling = value;
  }

  public boolean isErrorViewIsFilling() {
    return errorViewIsFilling;
  }

  public boolean isIncludeJavaIncludes() {
    return myIncludeJavaIncludes;
  }

  public static void doRegisterExtensionPoint(String extensionPointName, Object extension, Project project) {
    // Dynamic registering exp point for IDEA 7
    ExtensionsArea area = project != null ? Extensions.getArea(project): Extensions.getRootArea();
    if (area != null && area.hasExtensionPoint(extensionPointName)) {
      area.getExtensionPoint(extensionPointName).registerExtension(extension, LoadingOrder.ANY);
    }
  }

  public static String getQuickDoc(PsiElement psiElement) {
    OurUsage usage = psiElement.getUserData(ourUsageKey);
    if (usage == null) return null;

    StringBuilder result = new StringBuilder();
    result.append (usage.fileUsage.getFileLocaton()).append("\n");
    if (usage.context != null) result.append(usage.context);
    else result.append(usage.getText());

    return result.toString();
  }

  public static boolean isInSource(VirtualFile virtualFile, ProjectFileIndex fileIndex) {
    if (fileIndex.isInSourceContent(virtualFile)) {
      return !fileIndex.isInTestSourceContent(virtualFile) &&
        fileIndex.getModuleForFile(virtualFile) != null;
    } else if (!EnvironmentFacade.isJavaIde()) {
      return fileIndex.getModuleForFile(virtualFile) != null;
    }
    return false;
  }

  class ProjectSettingsForm {
    private JPanel projectSettingsPanel;
    private JTable excludedFilesTable;
    private JButton removeFileButton;
    private JButton addFileButton;
    private TextFieldWithBrowseButton additionalIncludeDirectoriesTextField;
    private TextFieldWithBrowseButton additionalSystemIncludeDirectoriesTextField;

    private JComboBox activeConfiguration;
    private JComboBox projectFile;
    private TextFieldWithBrowseButton preprocessorDefines;
    private TextFieldWithBrowseButton autoIncludedHeaders;
    private JComboBox compilerSelector;

    private JCheckBox includeJavaIncludeDirs;
    private JPanel mySettingsContent;    

    private List<String> excludedFileList = new ArrayList<String>();

    ProjectSettingsForm() {
      compilerSelector.setModel(new DefaultComboBoxModel(CppSupportSettings.CompilerSelectOptions.values()));
      excludedFilesTable.setDefaultRenderer(String.class, new ValidatingFilePathCellRenderer());

      addFileButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
            public boolean isFileSelectable(VirtualFile virtualFile) {
              return virtualFile.getFileType() == CPP_FILETYPE &&
                !Communicator.isHeaderFile(virtualFile);
            }
          };

          fileChooserDescriptor.setTitle("Choose File to Remove from Analysis Scope");
          FileChooserDialog fileChooser = FileChooserFactory.getInstance().createFileChooser(
            fileChooserDescriptor,
            project,
            WindowManagerEx.getInstanceEx().suggestParentWindow(project)
          );

          final VirtualFile[] virtualFiles = fileChooser.choose(null, project);
          if (virtualFiles != null && virtualFiles.length == 1) {
            excludedFileList.add(virtualFiles[0].getPresentableUrl().replace(File.separatorChar,'/'));
            final int atEnd = excludedFileList.size() - 1;
            ((AbstractTableModel)excludedFilesTable.getModel()).fireTableRowsInserted(atEnd, atEnd);
          }
        }
      });

      removeFileButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int selectedRow = excludedFilesTable.getSelectedRow();
          if (selectedRow == -1) return;
          excludedFileList.remove(selectedRow);
          ((AbstractTableModel)excludedFilesTable.getModel()).fireTableRowsDeleted(selectedRow,selectedRow);
        }
      });

      setupEditIncludeDirectories("Edit Additional System Include Pathes",additionalSystemIncludeDirectoriesTextField);
      setupEditIncludeDirectories("Edit Additional User Include Pathes", additionalIncludeDirectoriesTextField);
      setupEditIncludeFiles("Edit Automatically Included Header Files", autoIncludedHeaders);
      setupEditPredefinesList("Edit Predefines", preprocessorDefines);

      mySettingsContent.setLayout(new BorderLayout());
      mySettingsContent.add(myHighlightingSettings.createComponent(), BorderLayout.CENTER);
    }

    public void apply() {
      myIgnoredFiles.clear();
      myIgnoredFiles.addAll(excludedFileList);
      myAdditionalIncludeDirs = additionalIncludeDirectoriesTextField.getText();
      myAdditionalSystemIncludeDirs = additionalSystemIncludeDirectoriesTextField.getText();
      myAutomaticallyIncludedHeaderFiles = autoIncludedHeaders.getText();
      myCompilerOptions = CppSupportSettings.CompilerSelectOptions.valueOf(compilerSelector.getSelectedItem().toString());

      myHighlightingSettings.apply();
      myIncludeJavaIncludes = includeJavaIncludeDirs.isSelected();

      myActiveConfiguration = (String) activeConfiguration.getSelectedItem();
      myProjectFile = (String) projectFile.getSelectedItem();
      myAdditionalPreprocessorDefines = preprocessorDefines.getText();

      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          Communicator.getInstance(project).restartServer();
        }
      });
    }

    public boolean isModified() {
      return !excludedFileList.containsAll(myIgnoredFiles) ||
        !myIgnoredFiles.containsAll(excludedFileList) ||
        !myAdditionalIncludeDirs.equals(additionalIncludeDirectoriesTextField.getText()) ||
        !myAutomaticallyIncludedHeaderFiles.equals(autoIncludedHeaders.getText()) ||
        !myAdditionalSystemIncludeDirs.equals(additionalSystemIncludeDirectoriesTextField.getText()) ||
        myHighlightingSettings.isModified() ||
        !myCompilerOptions.equals(compilerSelector.getSelectedItem()) ||
        ( myActiveConfiguration == null && activeConfiguration.getSelectedItem() != null) ||
        ( myActiveConfiguration != null && !myActiveConfiguration.equals(activeConfiguration.getSelectedItem())) ||
        (myProjectFile != null && !myProjectFile.equals(projectFile.getSelectedItem())) ||
        (myProjectFile == null && projectFile.getSelectedItem() != null) ||
        myIncludeJavaIncludes != includeJavaIncludeDirs.isSelected() ||
        (myAdditionalPreprocessorDefines != null && !myAdditionalPreprocessorDefines.equals(preprocessorDefines.getText())) ||
        (myAdditionalPreprocessorDefines == null && preprocessorDefines.getText().length() > 0)
        ;
    }

    public void init() {
      excludedFileList.clear();
      excludedFileList.addAll(myIgnoredFiles);
      compilerSelector.setSelectedItem(myCompilerOptions);

      excludedFilesTable.setModel(new AbstractTableModel() {
        public int getRowCount() {
          return excludedFileList.size();
        }

        public int getColumnCount() {
          return 1;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
          return excludedFileList.get(rowIndex);
        }

        public String getColumnName(int column) {
          return "File Name";
        }

        public Class<?> getColumnClass(int columnIndex) {
          return String.class;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
          excludedFileList.set(rowIndex, (String) aValue);
        }

      });

      additionalIncludeDirectoriesTextField.setText(myAdditionalIncludeDirs);
      additionalSystemIncludeDirectoriesTextField.setText(myAdditionalSystemIncludeDirs);

      myHighlightingSettings.init();

      final Object[] objects = new Object[]{BaseBuildHandler.DEBUG_CONFIGURATION_NAME, BaseBuildHandler.RELEASE_CONFIGURATION_NAME};
      activeConfiguration.setModel(new DefaultComboBoxModel(objects));
      activeConfiguration.setSelectedItem(myActiveConfiguration);

      LinkedHashSet<String> set = buildProjectAndBuildFilesSet();
      projectFile.setModel(new DefaultComboBoxModel(set.toArray()));
      projectFile.setSelectedItem(myProjectFile);
      preprocessorDefines.setText(myAdditionalPreprocessorDefines != null ? myAdditionalPreprocessorDefines:"");

      autoIncludedHeaders.setText(myAutomaticallyIncludedHeaderFiles);

      includeJavaIncludeDirs.setSelected(myIncludeJavaIncludes);
    }

    public JPanel getProjectSettingsPanel() {
      return projectSettingsPanel;
    }
  }

  static void setupEditIncludeDirectories(final String title, final TextFieldWithBrowseButton includeDirectoriesTextField) {
    createEditIncludeDialog(title, includeDirectoriesTextField, true);
  }

  static void setupEditIncludeFiles(final String title, final TextFieldWithBrowseButton includeDirectoriesTextField) {
    createEditIncludeDialog(title, includeDirectoriesTextField, false);
  }

  private static void createEditIncludeDialog(final String title, final TextFieldWithBrowseButton includeDirectoriesTextField, final boolean directories) {
    includeDirectoriesTextField.getButton().setToolTipText(title);
    includeDirectoriesTextField.getButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final StringListEditor pathesComponent = directories ?
          new IncludePathesListEditor(title, includeDirectoriesTextField.getText()):
          new IncludeFilesListEditor(title, includeDirectoriesTextField.getText()
        );
        pathesComponent.show();
        if (pathesComponent.isOK()) includeDirectoriesTextField.setText(pathesComponent.getText());
      }
    });
  }

  static void setupEditPredefinesList(final String title, final TextFieldWithBrowseButton editPredefinesList) {
    editPredefinesList.getButton().setToolTipText(title);
    editPredefinesList.getButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        StringListEditor pathesComponent = new MacrosListEditor(title, editPredefinesList.getText());
        pathesComponent.show();
        if (pathesComponent.isOK()) editPredefinesList.setText(pathesComponent.getText());
      }
    });
  }

  public CppHighlightingSettings getHighlightingSettings() {
    return myHighlightingSettings;
  }

  public String getActiveConfiguration() {
    return myActiveConfiguration;
  }

  public long getReportedProblemAboutServerProblemStamp() {
    return myReportedProblemAboutServerProblemStamp;
  }

  public void setReportedProblemAboutServerProblemStamp(long reportedProblemAboutServerProblemStamp) {
    this.myReportedProblemAboutServerProblemStamp = reportedProblemAboutServerProblemStamp;
  }

  public void setActiveConfiguration(String activeConfiguration) {
    myActiveConfiguration = activeConfiguration;
  }

  public String getLastBuildAction() {
    return myLastBuildAction;
  }

  public void setLastBuildAction(String lastBuildAction) {
    myLastBuildAction = lastBuildAction;
  }

  public String getAdditionalCommandLineBuildParameters() {
    return myAdditionalCommandLineBuildParameters;
  }

  public void setAdditionalCommandLineBuildParameters(String additionalBuildOptions) {
    myAdditionalCommandLineBuildParameters = additionalBuildOptions;
  }

  public static CppSymbolContributor getConstantContributor() {
    return myConstantContributor;
  }

  public static CppSymbolContributor getMacrosContributor() {
    return myMacrosContributor;
  }

  public String getAdditionalCompileParameters() {
    return myAdditionalCompileParameters;
  }

  public boolean isIncludeProjectSettings() {
    return myIncludeProjectSettings;
  }

  public void setAdditionalCompileParameters(String additionalCompileParameters) {
    myAdditionalCompileParameters = additionalCompileParameters;
  }

  public void setIncludeProjectSettings(boolean includeProjectSettings) {
    myIncludeProjectSettings = includeProjectSettings;
  }

  public String getAdditionalPreprocessorDefines() {
    return myAdditionalPreprocessorDefines;
  }

  public String getProjectFile() {
    return myProjectFile;
  }

  public void setProjectFile(String _projectFile) {
    myProjectFile = _projectFile;
  }

  public LinkedHashSet<String> buildProjectAndBuildFilesSet() {
    LinkedHashSet<String> set = new LinkedHashSet<String>();
    for(VirtualFile file:getProjectAndBuildFilesSet()) {
      set.add(file.getPath());
    }
    return set;
  }

  public int getSettingsVersion() {
    return mySettingsVersion;
  }

  private CppSupportSettings.CompilerSelectOptions myCompilerOptions = CppSupportSettings.CompilerSelectOptions.AUTO;
  private static final @NonNls String COMPILER_SELECT_KEY = "compilerSelect";

  public CppSupportSettings.CompilerSelectOptions getCompilerOptions() {
    return myCompilerOptions;
  }

  public void setCompilerOptions(CppSupportSettings.CompilerSelectOptions compilerOptions) {
    this.myCompilerOptions = compilerOptions;
  }

  public String getAutomaticallyIncludedHeaderFiles() {
    return myAutomaticallyIncludedHeaderFiles;
  }

  private class MyDocumentListener implements DocumentListener {
    private Field autoPopupControllerAlarmField;

    {
        try {
        final Field[] declaredFields = AutoPopupController.class.getDeclaredFields();
        autoPopupControllerAlarmField = declaredFields[1];
        autoPopupControllerAlarmField.setAccessible(true);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    public void beforeDocumentChange(DocumentEvent event) {
    }

    public void documentChanged(DocumentEvent event) {
      if (this != myDocumentListener) return; // disposed
      final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(event.getDocument());
      final VirtualFile virtualFile = psiFile.getVirtualFile();

      final Communicator communicator = Communicator.getInstance(project);
      final long stamp = communicator.incModificationCount();
      final CharSequence newFragment = event.getNewFragment();
      final int insertedAt = event.getOffset();
      communicator.sendCommand(
        new ChangedCommand(
          virtualFile.getPath(),
          newFragment.toString(),
          insertedAt,
          insertedAt + event.getOldLength(),
          stamp
        )
      );

      final int len = newFragment.length();

      if (len > 0) {
        char aChar = newFragment.charAt(len - 1);
        final Editor selectedEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();

        final Document document = selectedEditor.getDocument();

        if (selectedEditor != null && document == event.getDocument()) {
          final CharSequence oldFragment = event.getOldFragment();

          if ((aChar == '.' && !"->".equals(oldFragment.toString())) ||
            (aChar == ':' && previousChar(selectedEditor) == ':') ||
            ((aChar == '>' && previousChar(selectedEditor) == '-') && !".".equals(oldFragment.toString()))
           ) {
            CodeInsightSettings settings = CodeInsightSettings.getInstance();
            final int lookupDelay = settings.AUTO_POPUP_COMPLETION_LOOKUP ? 0 : -1;

            if (lookupDelay >= 0) {
              ApplicationManager.getApplication().invokeLater(new Runnable() {
                public void run() {
                  try {
                    ((Alarm)autoPopupControllerAlarmField.get(AutoPopupController.getInstance(project))).addRequest(
                    new Runnable() {
                        public void run() {
                          PsiDocumentManager.getInstance(project).commitAllDocuments();
                          new CodeCompletionHandlerBase(CompletionType.BASIC).invokeCompletion(project, selectedEditor);
                        }
                      },
                      lookupDelay
                    );
                  } catch (IllegalAccessException e) {
                    e.printStackTrace();
                  }
                }
              });
            }

            final AutoPopupController inst = AutoPopupController.getInstance(project);
            final Class[] paramClasses;
            final Object[] params;
            final int selena7_0_4_builds_start = 7800;
            int buildNumber = selena7_0_4_builds_start;

            try {
              buildNumber = Integer.parseInt(ApplicationInfo.getInstance().getBuildNumber());
            }
            catch (NumberFormatException ex) {}

            if (buildNumber >= selena7_0_4_builds_start) {
              paramClasses = new Class[] { Editor.class, Condition.class };
              params = new Object[] { selectedEditor, null };
            } else {
              paramClasses = new Class[] { Editor.class };
              params = new Object[] { selectedEditor };
            }

            try {
              inst.getClass().getMethod("autoPopupMemberLookup", paramClasses).invoke(inst, params);
            } catch (Exception ex) { ex.printStackTrace(); }
          } else if (aChar == '}' && newFragment.length() == 2 && newFragment.charAt(0) == '\n' && oldFragment.length() == 0) {
            int i = insertedAt - 1;
            final CharSequence charsSequence = document.getCharsSequence();
            i = CharArrayUtil.shiftBackwardUntil(charsSequence, i, "\n");

            if (i >= 0) {
              int j = CharArrayUtil.shiftForward(charsSequence, i +1, " \t");
              final String indent = charsSequence.subSequence(i + 1, j).toString();

              if (indent.length() > 0) {
                SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                    CommandProcessor.getInstance().executeCommand(
                      project, new Runnable() {
                      public void run() {
                        ApplicationManager.getApplication().runWriteAction(new Runnable() {
                          public void run() {
//                            document.insertString(insertedAt, "  ");
                            int lbracePos = CharArrayUtil.shiftForwardUntil(charsSequence, insertedAt, "}");
                            if (lbracePos != -1) document.insertString(lbracePos, indent);
                          }
                        });
                      }
                    }, "Indent brace", this
                    );
                  }
                });
              }
            }
          }
        }
    }
    }

    private char previousChar(Editor editor) {
      final CharSequence charsSequence = editor.getDocument().getCharsSequence();
      int offset = editor.getCaretModel().getOffset();
      if (offset > 0) return charsSequence.charAt(offset - 1);
      return 0;
    }
  }
}
