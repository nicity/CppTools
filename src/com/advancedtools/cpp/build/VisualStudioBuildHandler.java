// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.build;

import com.advancedtools.cpp.CppSupportSettings;
import com.advancedtools.cpp.communicator.Communicator;
import com.intellij.execution.filters.Filter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * @author maxim
*/
public class VisualStudioBuildHandler extends BaseBuildHandler {
  private File tempOutFile;
  private volatile boolean stopReadThread;

  @NonNls
  private static final String REBUILD_BUILD_ACTION = "Rebuild";
  @NonNls
  private static final String CLEAN_BUILD_ACTION = "Clean";
  @NonNls
  private static final String BUILD_THIS_PROJECT_ONLY_BUILD_ACTION = "Build This Project Only";

  VisualStudioBuildHandler(Project _project, VirtualFile _file) {
    super(_project, _file);
  }

  public List<String> getCommandLine(@NotNull BuildTarget buildTarget) {
    stopReadThread = false;
    List<String> result = new ArrayList<String>();

    final String vsCDir = CppSupportSettings.getInstance().getVsCDir();
    if (vsCDir == null) return null;

    final StringBuilder command = new StringBuilder(vsCDir);
    command.append(File.separatorChar).append("..");
    
    File[] files = new File(command.toString()).listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.startsWith("Common");
      }
    });

    if (files.length == 0) return null;

    command.append(File.separatorChar).append(files[0].getName());

    boolean devEnv7AndUp = false;

    files = new File(command.toString()).listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.startsWith("MSDev98");
      }
    });

    if (files.length == 0) {
      devEnv7AndUp = true;
      files = new File(command.toString()).listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.startsWith("IDE");
        }
      });
    }

    if (files.length == 0) return null;

    command.append(File.separatorChar).append(files[0].getName()).append(File.separatorChar);

    if (!devEnv7AndUp) {
      command.append("Bin").append(File.separatorChar);
    }
    
    if (!devEnv7AndUp && new File(command.toString() + "msdev.exe").exists()) {
      command.append("msdev.exe");
    } else if (new File(command.toString() + "devenv.exe").exists()) {
      command.append("devenv.exe");
    } else {
      return null;
    }

    result.add(command.toString());

    if (devEnv7AndUp) {
      boolean buildingProject = false;

      if (Communicator.VCPROJ_EXTENSION.equals(file.getExtension()) ||
          Communicator.DSP_EXTENSION.equals(file.getExtension())
          ) {
        final String searchedExtension = "." + (Communicator.DSP_EXTENSION.equals(file.getExtension()) ?
          Communicator.DSW_EXTENSION:Communicator.SLN_EXTENSION);

        FilenameFilter filter = new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return name.endsWith(searchedExtension);
          }
        };
        files = new File(file.getParent().getPath()).listFiles(filter);
        if (files.length == 0) files = new File(file.getParent().getParent().getPath()).listFiles(filter);
        if (files.length == 0) return null;
        BuildUtils.appendOptions(result, files[0].getName());
        buildingProject = true;
      } else {
        BuildUtils.appendOptions(result, file.getName());
      }

      if (CLEAN_BUILD_ACTION.equals(buildTarget.buildAction)) {
        BuildUtils.appendOptions(result, "/clean");
      } else if (REBUILD_BUILD_ACTION.equals(buildTarget.buildAction)) {
        BuildUtils.appendOptions(result, "/rebuild");
      } else {
        BuildUtils.appendOptions(result, "/build");
      }

      BuildUtils.appendOptions(result, buildTarget.buildConfiguration);

      if (buildingProject) {
        BuildUtils.appendOptions(result, "/project");
        BuildUtils.appendOptions(result, file.getName());
      }
    } else {
      BuildUtils.appendOptions(result, file.getName());
      BuildUtils.appendOptions(result, "/make");
      command.setLength(0);

      command.append("\"").append(file.getNameWithoutExtension()).append(" - Win32 ")
        .append(buildTarget.buildConfiguration).append("\"");
      BuildUtils.appendOptions(result, command.toString());

      if (CLEAN_BUILD_ACTION.equals(buildTarget.buildAction)) {
        BuildUtils.appendOptions(result, "/clean");
      } else if (BUILD_THIS_PROJECT_ONLY_BUILD_ACTION.equals(buildTarget.buildAction)) {
        BuildUtils.appendOptions(result, "/norecurse");
      } else if (REBUILD_BUILD_ACTION.equals(buildTarget.buildAction)) {
        BuildUtils.appendOptions(result, "/rebuild");
      }
    }

    try {
      tempOutFile = File.createTempFile("out", ".txt");
      tempOutFile.deleteOnExit();
      command.append(" /out ").append(tempOutFile.getPath());
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

    return result;
  }

  public @NotNull @Nls String getBuildTitle(BuildTarget target) {
    return "MsDev Command Line Build";
  }

  public void afterProcessStarted() {
    new Thread(new Runnable() {
      public void run() {
        while(!tempOutFile.exists()) {
          try {
            Thread.sleep(300);
          } catch (InterruptedException e) {
            break;
          }
        }


        int lastLength = 0;

        while(!stopReadThread || lastLength == 0) {
          try {
            RandomAccessFile file = new RandomAccessFile(tempOutFile, "r");
            file.skipBytes(lastLength);
            String s;

            while((s = file.readLine())!= null) {
              addLineToOutput(s, false);
            }
            lastLength = (int)tempOutFile.length();
            file.close();
          } catch (IOException e) {
            // we can get io exception because the file is being used by other process
          }

          if (stopReadThread) break;
          try {
            Thread.sleep(300);
          } catch(InterruptedException ex) {}
        }
      }
    }).start();
  }

  public void afterProcessFinished() {
    stopReadThread = true;
  }

  public @Nullable Filter getOutputFormatFilter() {
    return new VCFormatFilter(file, project);
  }

  public String[] getAvailableConfigurations() {
    return new String[] {
      DEBUG_CONFIGURATION_NAME, RELEASE_CONFIGURATION_NAME
    };
  }

  public String[] getAvailableBuildActions() {
    return new String[] {
      BuildTarget.DEFAULT_BUILD_ACTION, REBUILD_BUILD_ACTION, CLEAN_BUILD_ACTION, BUILD_THIS_PROJECT_ONLY_BUILD_ACTION
    };
  }

  public static class VCFormatFilter extends BasicFormatFilter {
    public VCFormatFilter(VirtualFile file, Project project) {
      super(file, project, "^(?:[0-9]>)?([^\\(]+)\\(([0-9]+)\\) \\: (?:warning|error|fatal error)");
    }
  }
}
