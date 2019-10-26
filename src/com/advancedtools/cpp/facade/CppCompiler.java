// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.facade;

import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.build.BaseBuildHandler;
import com.advancedtools.cpp.build.BasicFormatFilter;
import com.advancedtools.cpp.build.BuildState;
import com.advancedtools.cpp.build.BuildTarget;
import com.advancedtools.cpp.sdk.CppModuleType;
import com.advancedtools.cpp.sdk.CppSdkType;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class CppCompiler implements Validator {
  @NotNull
  public ProcessingItem[] getProcessingItems(final CompileContext compileContext) {
    final List<ProcessingItem> processingItems = new ArrayList<ProcessingItem>();
    boolean doneSave = false;

    Module[] affectedModules = ApplicationManager.getApplication().runReadAction(new Computable<Module[]>() {
      public Module[] compute() {
        return compileContext.getCompileScope().getAffectedModules();
      }
    });

    for(Module module: affectedModules) {
      Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
      if (ModuleType.get(module) == CppModuleType.getInstance() ||
         (sdk != null && sdk.getSdkType() == CppSdkType.getInstance())) {
        processingItems.add(new MyProcessingItem(module));

        if (!doneSave) {
          BuildState.saveDocuments();
          doneSave = true;
        }

        VirtualFile moduleFile = module.getModuleFile();
        if (moduleFile == null) {
          BuildState.saveAll();
        }
      }
    }
    return processingItems.toArray(new ProcessingItem[processingItems.size()]);
  }

  public ProcessingItem[] process(final CompileContext compileContext, ProcessingItem[] processingItems) {
    if (processingItems.length == 0) return processingItems;
    Project project = ((MyProcessingItem)processingItems[0]).myModule.getProject();
    CppSupportLoader loader = CppSupportLoader.getInstance(project);
    String projectFilePath = loader.getProjectFile();
    VirtualFile projectFile = projectFilePath != null ? LocalFileSystem.getInstance().findFileByPath(projectFilePath):null;

    if (projectFile != null) {
      final BaseBuildHandler buildHandler = BaseBuildHandler.getBuildHandler(project, projectFile);

      if (buildHandler != null) {
        String buildAction = compileContext.isMake() ? loader.getLastBuildAction() : "rebuild";

        BuildTarget buildTarget = new BuildTarget(loader.getActiveConfiguration(), buildAction, loader.getAdditionalCommandLineBuildParameters());
        final BuildState buildState = buildHandler.createBuildState(buildTarget);

        if (buildState != null) {
          try {
            buildState.start();
            final BasicFormatFilter filter = (BasicFormatFilter) buildHandler.getOutputFormatFilter();

            Future<?> future = ApplicationManager.getApplication().executeOnPooledThread((Runnable) new CompilerStreamReader(
              buildState.getProcess().getErrorStream(), compileContext
            ) {
              protected void onInputLine(String s) {
                if (filter != null) {
                  BasicFormatFilter.BasicFormatResult result = (BasicFormatFilter.BasicFormatResult) filter.applyFilter(s, s.length());

                  if (result != null) {
                    CompilerMessageCategory category = result.isError() ? CompilerMessageCategory.ERROR : CompilerMessageCategory.WARNING;
                    String message = s.substring(result.highlightEndOffset + 1);
                    compileContext.addMessage(category, message, result.file.getUrl(), result.line, result.column);
                    return;
                  }
                }
                compileContext.addMessage(CompilerMessageCategory.WARNING, s, null, 0, 0);
              }
            });
            Future<?> future2 = ApplicationManager.getApplication().executeOnPooledThread((Runnable) new CompilerStreamReader(
              buildState.getProcess().getInputStream(), compileContext
            ) {
              final ProgressIndicator pi = ProgressManager.getInstance().getProgressIndicator();

              protected void onInputLine(String s) {
                if (pi != null) pi.setText2(s);
                compileContext.addMessage(CompilerMessageCategory.INFORMATION, s, null, 0, 0);
              }
            });
            future.get();
            future2.get();
          } catch (IOException e) {
            compileContext.addMessage(CompilerMessageCategory.ERROR, e.getLocalizedMessage(), null, 0, 0);
          } catch (InterruptedException ex) {}
          catch (ExecutionException ex) {}
        }
      }
    }
    
    return processingItems;
  }

  @NotNull
  public String getDescription() {
    return "C/C++ compiler";
  }

  public boolean validateConfiguration(CompileScope compileScope) {
    EnvironmentFacade facade = EnvironmentFacade.getInstance();

    for(Module module:compileScope.getAffectedModules()) {
      if (ModuleType.get(module) == CppModuleType.getInstance()) {
        Sdk sdk = ModuleRootManager.getInstance(module).getSdk();

        if (!(sdk.getSdkType() == CppSdkType.getInstance())) {
          Messages.showMessageDialog(module.getProject(), "C/Cpp module type is not configured", "C/C++ compiler problem", Messages.getErrorIcon());
          return false;
        }
      }
    }
    return true;
  }

  // IDEA 8
  public ValidityState createValidityState(DataInput dataInput) throws IOException {
    return new MyValidityState();
  }

  public ValidityState createValidityState(DataInputStream dataInputStream) throws IOException {
    return new MyValidityState();
  }

  private static class MyProcessingItem implements ProcessingItem {
    private Module myModule;

    public MyProcessingItem(Module module) {
      myModule = module;
    }

    @NotNull
    public VirtualFile getFile() {
      return myModule.getModuleFile();
    }

    public ValidityState getValidityState() {
      return new MyValidityState();
    }
  }

  private static class MyValidityState implements ValidityState {
    public boolean equalsTo(ValidityState validityState) {
      return validityState == this;
    }

    public void save(DataOutputStream dataOutputStream) throws IOException {
    }

    // IDEA 8
    public void save(DataOutput dataOutput) throws IOException {
    }
  }

  private static abstract class CompilerStreamReader implements Runnable {
    private final InputStream stream;
    protected final CompileContext compileContext;

    public CompilerStreamReader(InputStream _stream, CompileContext _compileContext) {
      stream = _stream;
      compileContext = _compileContext;
    }

    public void run() {
      try {
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(new BufferedInputStream(stream)));
        while (true) {
          String s = reader.readLine();
          if (s == null) break;
          onInputLine(s);
        }
      } catch (IOException ex) {
        compileContext.addMessage(CompilerMessageCategory.ERROR, ex.getLocalizedMessage(), null, 0, 0);
      } finally {
        try {
          stream.close();
        } catch (IOException ex) {}
      }
    }

    protected abstract void onInputLine(String s);
  }
}
