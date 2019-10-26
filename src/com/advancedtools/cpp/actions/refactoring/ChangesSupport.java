// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.actions.refactoring;

import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.advancedtools.cpp.communicator.Communicator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author maxim
 */
public class ChangesSupport {
  private final List<FileChanges> changes = new ArrayList<FileChanges>();

  private static class Change {
    int start,end;
    String text;
  }

  private static class FileChanges {
    String fileName;
    List<Change> elementaries = new ArrayList<Change>();
  }

  private FileChanges lastChanges;

  public void appendChangesFromString(String str) {
    final String delim = "changes for ";
    if (str.startsWith(delim)) {
      String fileName = str.substring(delim.length());
      lastChanges = new FileChanges();
      lastChanges.fileName = fileName;
      changes.add(lastChanges);
    } else {
      final String delim2 = "replace ";

      if (str.startsWith(delim2)) {
        final Change change = new Change();
        final int index = str.indexOf(Communicator.DELIMITER, delim2.length());
        change.start = Integer.parseInt(str.substring(delim2.length(), index));
        final int index2 = str.indexOf(' ', index);
        change.end = Integer.parseInt(str.substring(index + 1, index2));
        change.text = BuildingCommandHelper.unquote(str.substring(index2 + 1));
        lastChanges.elementaries.add(change);
      }
    }
  }

  public void applyChanges(String commandId, final Project project) {
    CommandProcessor.getInstance().executeCommand(
      project,
      new Runnable() {

        public void run() {
          ApplicationManager.getApplication().runWriteAction(
            new Runnable() {
              public void run() {
                for (FileChanges c : changes) {
                  VirtualFile file = LocalFileSystem.getInstance().findFileByIoFile(new File(c.fileName));
                  assert file != null;
                  final Document document = PsiDocumentManager.getInstance(project).getDocument(PsiManager.getInstance(project).findFile(file));
                  assert document != null;
                  int diff = 0;

                  for (Change ch : c.elementaries) {
                    document.replaceString(diff + ch.start, diff + ch.end, ch.text);
                    diff += ch.text.length() - (ch.end - ch.start);
                  }
                }
              }
            }
          );
        }
      },
      commandId,
      null
    );
        }

}
