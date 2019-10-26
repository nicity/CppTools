// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.actions;

import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.communicator.BlockingCommand;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.advancedtools.cpp.communicator.Communicator;
import com.advancedtools.cpp.hilighting.AnalyzeProcessor;
import com.advancedtools.cpp.hilighting.Fix;
import com.advancedtools.cpp.hilighting.HighlightCommand;
import com.intellij.ide.errorTreeView.NewErrorTreeViewPanel;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.content.*;
import com.intellij.ui.errorView.ContentManagerProvider;
import com.intellij.ui.errorView.ErrorViewFactory;
import com.intellij.util.ui.ErrorTreeView;
import com.intellij.util.ui.MessageCategory;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author maxim
 */
public class ShowAllProblemsInProjectAction extends AnAction {
  private static final String id = "All Project Problems";

  @Override
  public void update(AnActionEvent anActionEvent) {
    super.update(anActionEvent);
    final DataContext dataContext = anActionEvent.getDataContext();
    final Project project = (Project) dataContext.getData(DataConstants.PROJECT);
    final Presentation presentation = anActionEvent.getPresentation();

    presentation.setEnabled(
      project != null &&
        !CppSupportLoader.getInstance(project).isErrorViewIsFilling() &&
        Communicator.getInstance(project).isServerUpAndRunning()
    );
  }

  public void actionPerformed(AnActionEvent anActionEvent) {
    final DataContext dataContext = anActionEvent.getDataContext();
    final Project project = (Project) dataContext.getData(DataConstants.PROJECT);

    final CppSupportLoader loader = CppSupportLoader.getInstance(project);
    final ErrorTreeView treeView = loader.getErrorTreeView();
    if (treeView != null) treeView.dispose();

    loader.setErrorViewIsFilling(true);

    final MessageView messageView = MessageView.SERVICE.getInstance(project);
    final ContentManager contentManager = messageView.getContentManager();
    
    final ErrorTreeView view = ErrorViewFactory.SERVICE.getInstance().createErrorTreeView(
      project,
      id,
      true,
      new AnAction[0],
      new AnAction[0],
      new ContentManagerProvider() {
        public ContentManager getParentContent() {
          return contentManager;
        }
      }
    );
    
    final Content content = ContentFactory.SERVICE.getInstance().createContent(view.getComponent(), id, true);
    contentManager.addContent(content);
    contentManager.setSelectedContent(content);
    ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.MESSAGES_WINDOW).activate(null);

    contentManager.addContentManagerListener(new ContentManagerListener() {
      public void contentAdded(ContentManagerEvent event) {}

      public void contentRemoved(ContentManagerEvent event) {
        if (content == event.getContent()) {
          content.release();
          view.dispose();
          contentManager.removeContentManagerListener(this);
        }
      }

      public void contentRemoveQuery(ContentManagerEvent event) {
      }

      public void selectionChanged(ContentManagerEvent event) {
      }
    });

    loader.setErrorTreeView(view);

    ProgressManager.getInstance().runProcessWithProgressSynchronously(
      new Runnable() {
        public void run() {
          MyBlockingCommand blockingCommand = new MyBlockingCommand(project);
          ProgressIndicator pi = ProgressManager.getInstance().getProgressIndicator();
          try {
            while(true) {
              pi.setFraction(blockingCommand.getFraction());
              pi.setText2(blockingCommand.getCurrentFileName());
              blockingCommand.post(project);
              if (blockingCommand.isFailedOrCancelled()) {
                break;
              }

              pi.checkCanceled();
              blockingCommand = blockingCommand.nextCommand();
              if (blockingCommand == null) break;
            }
          } finally {
            CppSupportLoader.getInstance(project).setErrorViewIsFilling(false);
          }
        }
      },
      "analyzing project files for errors",
      true,
      project
    );
  }

  private static class MyBlockingCommand extends BlockingCommand {
    private final MyBlockingCommand.MyAnalyzeProcessor myAnalyzeProcessor;
    private final ArrayList<VirtualFile> filesToProcess;
    private final LinkedHashMap<String,VirtualFile> nameToFilesMap;
    private int currentIndex;
    private VirtualFile currentFile;

    static int filesPerRequest = 25;
    private String myText;
    private Project project;

    private final Computable<PsiFile> fileProvider = new Computable<PsiFile>() {
    public PsiFile compute() {
      return ApplicationManager.getApplication().runReadAction(new Computable<PsiFile>() {
        public PsiFile compute() {
          return PsiManager.getInstance(project).findFile(currentFile);
        }
      });
    }};
    private boolean bug373 = true; // cfserver incorrectly handl names

    MyBlockingCommand(Project project) {
      this(0, project, new ArrayList<VirtualFile>(), new LinkedHashMap<String, VirtualFile>());

      final ProjectFileIndex moduleFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
      moduleFileIndex.iterateContent(new ContentIterator() {
        public boolean processFile(VirtualFile virtualFile) {
          if (!virtualFile.isDirectory() &&
              virtualFile.getFileType() == CppSupportLoader.CPP_FILETYPE &&
              CppSupportLoader.isInSource(virtualFile, moduleFileIndex)
             ) {
            filesToProcess.add(virtualFile);
            String key = virtualFile.getPath();
            if (bug373) key = key.toLowerCase();
            nameToFilesMap.put(key, virtualFile);
          }
          return true;
        }
      });
    }

    private MyBlockingCommand(int currentIndex, Project project,
                              ArrayList<VirtualFile> virtualFiles,
                              LinkedHashMap<String,VirtualFile> nameToFilesMap) {
      this.currentIndex = currentIndex;
      this.project = project;
      myAnalyzeProcessor = new MyAnalyzeProcessor();
      filesToProcess = virtualFiles;
      this.nameToFilesMap = nameToFilesMap;
    }

    MyBlockingCommand(int currentIndex, MyBlockingCommand base) {
      this(currentIndex, base.project, base.filesToProcess, base.nameToFilesMap);
    }

    @Override
    public boolean doInvokeInDispatchThread() {
      return true;
    }

    public void commandOutputString(String str) {
      final java.util.List<String> errorListIfCached = HighlightCommand.getErrorListIfCached(
        str,
        fileProvider
      );
      if (errorListIfCached != null) {
        for(String s:errorListIfCached) HighlightCommand.processErrorInfoFromString(s, myAnalyzeProcessor);
      } else {
        HighlightCommand.processErrorInfoFromString(str, myAnalyzeProcessor);
      }
    }

    public String getCommand() {
      if (myText == null) {
        final StringBuilder commandText = new StringBuilder();
        commandText.append("analyze ");

        for(int i = 0; i < filesPerRequest; ++i) {
          final int current = currentIndex + i;
          if (current >= filesToProcess.size()) break;
          if (commandText.length() != 0) commandText.append(' ');
          final String fileName = BuildingCommandHelper.quote(BuildingCommandHelper.fixVirtualFileName(filesToProcess.get(current).getPath()));
          commandText.append(fileName).append(" 0 end");
        }
        myText = commandText.toString();
      }
      return myText;
    }

    public MyBlockingCommand nextCommand() {
      final int nextIndex = currentIndex + filesPerRequest;
      if (nextIndex < filesToProcess.size()) {
        return new MyBlockingCommand(nextIndex, this);
      }
      return null;
    }

    public double getFraction() {
      final int cnt = filesToProcess.size();
      return cnt > 0 ? ((100* (currentIndex + 1)) / cnt) / 100.0:1.0;
    }

    public String getCurrentFileName() {
      return filesToProcess.get(currentIndex).getPresentableUrl();
    }

    private class MyAnalyzeProcessor implements AnalyzeProcessor {
      final ErrorTreeView view = CppSupportLoader.getInstance(project).getErrorTreeView();

      public String getAnalizedFileName() {
        return currentFile.getPath();
      }

      public void startedAnalyzedFileName(String fileName) {
        String key = fileName.replace(File.separatorChar, '/');
        if (bug373) key = key.toLowerCase();
        currentFile = nameToFilesMap.get(key);
        assert currentFile != null:fileName + " is not known";
      }

      public void addMessage(final MessageType type, final int start, final int end, final String message, Fix... fixes) {
        if (type == MessageType.Intention || type == MessageType.Info) return;

        final VirtualFile analizedFile = currentFile;
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            view.addMessage(
              type == MessageType.Error ? MessageCategory.ERROR : type == MessageType.Warning ? MessageCategory.WARNING: MessageCategory.INFORMATION,
              new String[] { message },
              analizedFile.getPresentableUrl(),
              new OpenFileDescriptor(project, analizedFile,start),
              NewErrorTreeViewPanel.createExportPrefix(start),
              NewErrorTreeViewPanel.createRendererPrefix(start,end),
              analizedFile
            );
          }
        });
      }

    }
  }
}