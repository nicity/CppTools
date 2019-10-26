// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.advancedtools.cpp.communicator.Communicator;
import com.advancedtools.cpp.facade.EnvironmentFacade;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.StringTokenizer;
import java.util.Date;
import java.util.prefs.Preferences;
import java.net.URL;
import java.net.URLConnection;
import java.io.*;

/**
 * @author maxim
 */
public class CppSupportSettings implements ApplicationComponent, JDOMExternalizable, Configurable {
  private static CppSupportSettings instance;
  private static final @NonNls String VS_DIR_KEY = "msvcdir";
  private static final @NonNls String GCC_PATH_KEY = "gccPath";
  private static final @NonNls String CLANG_PATH_KEY = "clangPath";
  private static final @NonNls String MINGW_TOOLS_DIR_KEY = "mingwToolsDir";

  private static final @NonNls String DEFINES_KEY = "defines";
  private static final @NonNls String MAIL_SERVER_KEY = "mailServer";
  private static final @NonNls String MAIL_USER_KEY = "mailUser";
  private static final @NonNls String ADDITIONAL_INCLUDES_KEY = "additionalIncludes";
  
  public static final @NonNls String PATH_SEPARATOR = ";";
  private static final String LAST_USED_TIME_PROP_KEY = "last.used.time";

  public static void setupDirectoryBrowseButton(final TextFieldWithBrowseButton browser, final FileChooserDescriptor fileChooserDescriptor) {
    browser.getButton().addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Window mostRecentFocusedWindow = WindowManagerEx.getInstanceEx().getMostRecentFocusedWindow();
          Project project = null;
          while (mostRecentFocusedWindow != null) {
            if (mostRecentFocusedWindow instanceof DataProvider) {
              project = (Project) ((DataProvider)mostRecentFocusedWindow).getData(DataConstants.PROJECT);
              if (project != null) break;
            }
            mostRecentFocusedWindow = (Window) mostRecentFocusedWindow.getParent();
          }

          FileChooserDialog fileChooser = FileChooserFactory.getInstance().createFileChooser(
            fileChooserDescriptor,
            project,
            WindowManagerEx.getInstanceEx().suggestParentWindow(project)
          );

          String lastPathToMSVS = browser.getText();
          if (fileChooserDescriptor.isChooseMultiple()) {
            StringTokenizer tokenizer = new StringTokenizer(lastPathToMSVS, PATH_SEPARATOR);
            lastPathToMSVS = tokenizer.hasMoreElements() ? tokenizer.nextToken() : null;
          }
          VirtualFile file = lastPathToMSVS != null && lastPathToMSVS.length() > 0 ? VfsUtil.findRelativeFile(lastPathToMSVS, null):null;

          final VirtualFile[] virtualFiles = fileChooser.choose(file, project);
          if (virtualFiles != null ) {
            if (!fileChooserDescriptor.isChooseMultiple()) {
              if (virtualFiles.length == 1) browser.setText(virtualFiles[0].getPresentableUrl());
            }
            else if (virtualFiles.length > 0) {
              for(VirtualFile f:virtualFiles) {
                String s = browser.getText();
                if (s.length() > 0) s += PATH_SEPARATOR;
                s += f.getPresentableUrl();
                browser.setText(s);
              }
            }
          }
        }
      });
  }

  public void setVsCDir(String vsCDir) {
    myVSCDir = vsCDir;
  }

  public void setGccPath(String gccPath) {
    myGccPath = gccPath;
  }

  public void setMingToolsDirectory(String mingToolsDirectory) {
    this.myMingToolsDirectory = mingToolsDirectory;
  }

  public void setGdbPath(String path) {
    myGdbPath = path;
  }

  public static String getPluginVersion() {
    PluginId pluginId = PluginId.getId("C/C++");;
    return pluginId != null ? PluginManager.getPlugin(pluginId).getVersion(): "Unknown version";
  }

  public enum CompilerSelectOptions {
    AUTO, MSVC, GCC, CLANG
  }

  private @NonNls String myVSCDir = "c:\\Program Files\\Microsoft Visual Studio .NET 2012\\vc10";
  private @NonNls String myGccPath = "gcc";
  private @NonNls String myClangPath = "clang";
  private @NonNls String myMailServer;
  private @NonNls String myMailUser;
  private @NonNls String myDefines = "WIN32;NDEBUG;_WINDOWS;_MBCS;_AFXDLL;_WIN32";
  private String myAdditionalIncludeDirs = null;
  private String myMingToolsDirectory = null;

  private static final @NonNls String LAUNCH_GDB_ON_FAIL_KEY = "launchGdbOnFailure";
  private static final @NonNls String LAUNCH_GDB_ON_FAIL_COMMAND_KEY = "launchGdbOnFailureCommand";
  private static final @NonNls String GDB_PATH_KEY = "gdbPath";

  private boolean myLaunchGdbOnFailure;
  private String myGdbPath = "gdb"; // C:/MinGW/bin/gdb.exe
  private String myLaunchGdbOnFailureCommand = buildDefaultGdbLaunchCommand(); // $gdb$ --directory=~/CFC/

  private static String buildDefaultGdbLaunchCommand() {
    return (SystemInfo.isWindows ? "$gdb$": "/usr/X11/bin/xterm -e $gdb$") + " --directory=" +
      BuildingCommandHelper.quote(SystemInfo.isWindows ? "c:/MingW/1.0/home/maxim/CF-C":"~/CF-C");
  }

  public CppSupportSettings() {
    if (ApplicationManager.getApplication().isUnitTestMode()) instance = this;
  }
  
  @NonNls
  public String getComponentName() {
    return "CppTools.Settings";
  }

  public void initComponent() {
    new Thread(new Runnable() {
      public void run() {
        Preferences preferences = Preferences.userNodeForPackage(CppSupportSettings.class);
        String lastUsedTime = preferences.get(LAST_USED_TIME_PROP_KEY, null);
        if (lastUsedTime != null) {
          try {
            Date date = new Date(Long.parseLong(lastUsedTime));
            long passedMillis = new Date().getTime() - date.getTime();
            if (passedMillis > (24L * 7 * 60 * 60 * 1000L)) {
              lastUsedTime = null;
            }
          } catch (IllegalArgumentException ex) {
            lastUsedTime = null;
          }
        }

        if (lastUsedTime == null) {
          PluginId pluginId = PluginId.getId("C/C++");
          if (pluginId != null) {
            IdeaPluginDescriptor ideaPluginDescriptor = PluginManager.getPlugin(pluginId);
            if (ideaPluginDescriptor != null) {
              LineNumberReader lnr = null;
              try {
                ApplicationInfo applicationInfo = ApplicationInfo.getInstance();
                String idea_version = applicationInfo.getMajorVersion() + "." + applicationInfo.getMinorVersion() + "_"+applicationInfo.getBuildNumber();
                String spec = "http://www.adv-tools.com/update_check?v=" +
                  escape(ideaPluginDescriptor.getVersion()) + "&i=" + escape(idea_version);
                URLConnection urlConnection = new URL(spec).openConnection();

                LineNumberReader reader = new LineNumberReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                String line = reader.readLine(); // TODO:more logic on having update available
              } catch (IOException ex) {
//                ex.printStackTrace(); // TODO: remove
              } finally {
                if (lnr != null) {
                  try {
                    lnr.close();
                  } catch (IOException ex) {}
                }
                preferences.put(LAST_USED_TIME_PROP_KEY, Long.toString(System.currentTimeMillis()));
              }
            }
          }
        }
      }
    }, "Cpp Plugin update check").start();

    Runnable registerIgnoredFilesRunnable = new Runnable() {
      public void run() {
        FileTypeManagerEx.getInstanceEx().setIgnoredFilesList(FileTypeManagerEx.getInstanceEx().getIgnoredFilesList() + "*.o;*.obj;");
      }
    };
    EnvironmentFacade.getInstance().runWriteActionFromComponentInstantiation(registerIgnoredFilesRunnable);
  }

  private static String escape(String s) {
    return s.replace(" ", "%20");
  }

  public boolean canDoSomething(DataContext dataContext) {
    VirtualFile file = (VirtualFile) dataContext.getData(DataConstants.VIRTUAL_FILE);
    if (file == null || file.getFileType() != CppSupportLoader.CPP_FILETYPE) return false;
    return true;
  }

  public void disposeComponent() {}

  public void readExternal(Element element) throws InvalidDataException {
    String value = element.getAttributeValue(VS_DIR_KEY);
    if (value != null) myVSCDir = value;

    value = element.getAttributeValue(DEFINES_KEY);
    if (value != null) myDefines = value;

    value = element.getAttributeValue(ADDITIONAL_INCLUDES_KEY);
    if (value != null) myAdditionalIncludeDirs = value;

    value = element.getAttributeValue(GCC_PATH_KEY);
    if (value != null) myGccPath = value;

    value = element.getAttributeValue(CLANG_PATH_KEY);
    if (value != null) myClangPath = value;

    value = element.getAttributeValue(MINGW_TOOLS_DIR_KEY);
    if (value != null) myMingToolsDirectory = value;

    value = element.getAttributeValue(MAIL_SERVER_KEY);
    if (value != null) myMailServer = value;

    value = element.getAttributeValue(MAIL_USER_KEY);
    if (value != null) myMailUser = value;

    value = element.getAttributeValue(GDB_PATH_KEY);
    if (value != null) myGdbPath = value;

    value = element.getAttributeValue(LAUNCH_GDB_ON_FAIL_COMMAND_KEY);
    if (value != null) myLaunchGdbOnFailureCommand = value;

    value = element.getAttributeValue(LAUNCH_GDB_ON_FAIL_KEY);
    if (value != null) myLaunchGdbOnFailure = Boolean.valueOf(value);
  }

  public void writeExternal(Element element) throws WriteExternalException {
    if (myGccPath != null) element.setAttribute(GCC_PATH_KEY, myGccPath);
    if (myClangPath != null) element.setAttribute(CLANG_PATH_KEY, myClangPath);
    if (myMingToolsDirectory != null) element.setAttribute(MINGW_TOOLS_DIR_KEY, myMingToolsDirectory);

    if (myVSCDir != null) element.setAttribute(VS_DIR_KEY, myVSCDir);
    if (myDefines != null) element.setAttribute(DEFINES_KEY, myDefines);
    if (myAdditionalIncludeDirs != null) element.setAttribute(ADDITIONAL_INCLUDES_KEY, myAdditionalIncludeDirs);

    if (myMailServer != null) element.setAttribute(MAIL_SERVER_KEY, myMailServer);
    if (myMailUser != null) element.setAttribute(MAIL_USER_KEY, myMailUser);
    if (myLaunchGdbOnFailure) {
      element.setAttribute(LAUNCH_GDB_ON_FAIL_KEY, Boolean.toString(myLaunchGdbOnFailure));
    }
    if (myLaunchGdbOnFailureCommand != null) {
      element.setAttribute(LAUNCH_GDB_ON_FAIL_COMMAND_KEY, myLaunchGdbOnFailureCommand);
    }

    if (myGdbPath != null) {
      element.setAttribute(GDB_PATH_KEY, myGdbPath);
    }
  }

  public static CppSupportSettings getInstance() {
    final Application application = ApplicationManager.getApplication();
    if (application.isUnitTestMode()) return instance;
    return application.getComponent(CppSupportSettings.class);
  }

  public String getVsCDir() {
    return myVSCDir;
  }

  public String getDefines() {
    return myDefines;
  }

  public String getAdditionalIncludeDirs() {
    return myAdditionalIncludeDirs;
  }

  public String getMailServer() {
    return myMailServer;
  }

  public void setMailServer(String myMailServer) {
    this.myMailServer = myMailServer;
  }

  public String getMailUser() {
    return myMailUser;
  }

  public boolean isLaunchGdbOnFailure() {
    return myLaunchGdbOnFailure;
  }

  public String getGdbPath() {
    return myGdbPath;
  }

  public String getLaunchGdbOnFailureCommand() {
    return myLaunchGdbOnFailureCommand;
  }

  public void setMailUser(String myMailUser) {
    this.myMailUser = myMailUser;
  }

  public String getGccPath() {
    return myGccPath;
  }

  public String getClangPath() {
    return myClangPath;
  }

  public String getMingwToolsDir() {
    return myMingToolsDirectory;
  }

  public String getDisplayName() {
    return "C/C++";
  }

  public Icon getIcon() {
    return null;
  }

  @Nullable
  @NonNls
  public String getHelpTopic() {
    return null;
  }

  public static boolean isMsVcDirectory(VirtualFile virtualFile) {
    return virtualFile.findChild("include") != null;
  }

  public static boolean isGccPath(VirtualFile virtualFile) {
    return !virtualFile.isDirectory() && virtualFile.getNameWithoutExtension().indexOf("gcc") != -1;
  }

  public static boolean isClangPath(VirtualFile virtualFile) {
    return !virtualFile.isDirectory() && virtualFile.getNameWithoutExtension().indexOf("clang") != -1;
  }

  class MySettings {
    private JLabel pathToVSCText;
    private JLabel definesText;
    private JLabel additionalIncludeDirsText;
    
    private TextFieldWithBrowseButton pathToVSC;
    private TextFieldWithBrowseButton defines;
    private TextFieldWithBrowseButton additionalIncludeDirs;
    private JTabbedPane myPanel;
    private TextFieldWithBrowseButton gccPath;
    private TextFieldWithBrowseButton clangPath;
    private JLabel gccIncludePathText;
    private TextFieldWithBrowseButton mingWToolsDirectory;
    private JLabel mingWToolsDirectoryText;
    private TextFieldWithBrowseButton gdbPath;
    private JTextField gdbLaunchCommand;
    private JCheckBox launchGdbOnServerCrash;
    private JButton myForceRestartServersButton;
    private JLabel pathToClangText;

    MySettings(String _pathToVsC, String _pathToGccInclude, String _pathToClang, String _mingwToolsDir,
               String _defines, String _additionalIncludeDirs, String _gdbPath, String _launchGdbOnFailurePath,
               boolean _launchGdbOnFailure) {
      pathToVSCText.setLabelFor(pathToVSC.getTextField());

      mingWToolsDirectoryText.setLabelFor(mingWToolsDirectory);
      mingWToolsDirectoryText.setDisplayedMnemonic('w');

      if (!SystemInfo.isWindows) {
        mingWToolsDirectory.setVisible(false);
        mingWToolsDirectoryText.setVisible(false);
      }

      gccIncludePathText.setLabelFor(gccPath);
      gccIncludePathText.setDisplayedMnemonic('g');
      
      definesText.setLabelFor(defines);
      definesText.setDisplayedMnemonic('i');

      pathToClangText.setLabelFor(clangPath);

      additionalIncludeDirsText.setLabelFor(additionalIncludeDirs.getTextField());
      additionalIncludeDirsText.setDisplayedMnemonic('a');

      FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(false, true, false, false, false, false) {
        public boolean isFileSelectable(VirtualFile virtualFile) {
          return isMsVcDirectory(virtualFile);
        }
      };
      fileChooserDescriptor.setTitle("Choose MS Visual C Directory");

      setupDirectoryBrowseButton(pathToVSC, fileChooserDescriptor);

      fileChooserDescriptor = new FileChooserDescriptor(false, true, false, false, false, false) {
        public boolean isFileSelectable(VirtualFile virtualFile) {
          return virtualFile.findChild("msys.bat") != null;
        }
      };
      fileChooserDescriptor.setTitle("Choose MingW Tools Directory");

      setupDirectoryBrowseButton(mingWToolsDirectory, fileChooserDescriptor);

      fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
        public boolean isFileSelectable(VirtualFile virtualFile) {
          return isGccPath(virtualFile);
        }
      };
      fileChooserDescriptor.setTitle("Choose GCC Path");
      setupDirectoryBrowseButton(gccPath, fileChooserDescriptor);

      fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
        public boolean isFileSelectable(VirtualFile virtualFile) {
          return isClangPath(virtualFile);
        }
      };
      fileChooserDescriptor.setTitle("Choose Clang Path");
      setupDirectoryBrowseButton(clangPath, fileChooserDescriptor);

      CppSupportLoader.setupEditIncludeDirectories("Edit Additional System Include Paths", additionalIncludeDirs);
      
      CppSupportLoader.setupEditPredefinesList("Edit Predefines", defines);

      fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
        public boolean isFileSelectable(VirtualFile virtualFile) {
          return super.isFileSelectable(virtualFile) && virtualFile.getName().indexOf("gdb") != -1;
        }
      };
      fileChooserDescriptor.setTitle("Choose GDB Path");

      setupDirectoryBrowseButton(gdbPath, fileChooserDescriptor);
      gdbLaunchCommand.setEnabled(false);
      launchGdbOnServerCrash.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          gdbLaunchCommand.setEnabled(launchGdbOnServerCrash.isSelected());
        }
      });

      reset(_pathToVsC, _pathToGccInclude, _pathToClang, _mingwToolsDir, _defines, _additionalIncludeDirs, _gdbPath,
        _launchGdbOnFailurePath, _launchGdbOnFailure);
      myForceRestartServersButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          doRestartServers();
        }
      });
    }

    private void reset(String _pathToVs, String _pathToGcc, String _pathToClang, String _mingwToolsDir,
                       String _defines, String _additionalIncludeDirs, String _gdbPath, String _launchGdbOnFailurePath,
               boolean _launchGdbOnFailure) {
      pathToVSC.getTextField().setText(_pathToVs);
      gccPath.getTextField().setText(_pathToGcc);
      clangPath.getTextField().setText(_pathToClang);

      defines.setText(_defines);
      additionalIncludeDirs.setText(_additionalIncludeDirs);
      mingWToolsDirectory.setText(_mingwToolsDir);
      gdbLaunchCommand.setText(_launchGdbOnFailurePath);
      gdbPath.setText(_gdbPath);
      launchGdbOnServerCrash.setSelected(_launchGdbOnFailure);
    }
  }

  private MySettings settings;

  public JComponent createComponent() {
    if (settings == null) {
      settings = new MySettings(myVSCDir, myGccPath, myClangPath, myMingToolsDirectory,
        myDefines, myAdditionalIncludeDirs, myGdbPath, myLaunchGdbOnFailureCommand, myLaunchGdbOnFailure);
    }
    return settings.myPanel;
  }

  public boolean isModified() {
    final String definesText = settings.defines.getText();
    final String additionalIncludesText = settings.additionalIncludeDirs.getText();

    return !myVSCDir.equals(settings.pathToVSC.getTextField().getText()) ||
      !settings.gccPath.getTextField().getText().equals(myGccPath) ||
      (myDefines != null && !myDefines.equals(definesText)) ||
      (myAdditionalIncludeDirs != null && !myAdditionalIncludeDirs.equals(additionalIncludesText)) ||
      (myDefines == null && definesText.length() > 0) ||
      (myAdditionalIncludeDirs == null && additionalIncludesText.length() > 0) ||
      (myMingToolsDirectory != null && !myMingToolsDirectory.equals(settings.mingWToolsDirectory.getText())) ||
      (myMingToolsDirectory == null && settings.mingWToolsDirectory.getText().length() > 0) ||
      !myGdbPath.equals(settings.gdbPath.getText()) ||
      !myClangPath.equals(settings.clangPath.getText()) ||
      myLaunchGdbOnFailure != settings.launchGdbOnServerCrash.isSelected() ||
      !myLaunchGdbOnFailureCommand.equals(settings.gdbLaunchCommand.getText())
      ;
  }

  public void apply() throws ConfigurationException {
    myGccPath = settings.gccPath.getTextField().getText();
    myClangPath = settings.clangPath.getTextField().getText();
    myMingToolsDirectory = settings.mingWToolsDirectory.getText();
    myVSCDir = settings.pathToVSC.getTextField().getText();

    myDefines = settings.defines.getText();
    myAdditionalIncludeDirs = settings.additionalIncludeDirs.getText();
    doRestartServers();

    myLaunchGdbOnFailure = settings.launchGdbOnServerCrash.isSelected();
    myGdbPath = settings.gdbPath.getText();
    myLaunchGdbOnFailureCommand = settings.gdbLaunchCommand.getText();
  }

  private static void doRestartServers() {
    final Project[] projects = ProjectManager.getInstance().getOpenProjects();
    for(Project project:projects) Communicator.getInstance(project).restartServer();
  }

  public void reset() {
    settings.reset(myVSCDir, myGccPath, myClangPath, myMingToolsDirectory, myDefines, myAdditionalIncludeDirs, myGdbPath,
      myLaunchGdbOnFailureCommand, myLaunchGdbOnFailure);
  }

  public void disposeUIResources() {
    settings = null;
  }

  // TODO: OS specific defines

}
