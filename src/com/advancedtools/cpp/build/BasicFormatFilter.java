// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.build;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author maxim
 * Date: Mar 23, 2009
 * Time: 10:47:14 PM
 */
public class BasicFormatFilter implements Filter {
  private final Pattern fileNameAndSizeExractor;
  protected VirtualFile currentContext;
  protected final Project project;

  public BasicFormatFilter(VirtualFile file, Project project, String pattern) {
    this.project = project;
    currentContext = file.getParent();
    fileNameAndSizeExractor = pattern != null ? Pattern.compile(pattern):null;
  }

  public Result applyFilter(String line, int entireLength) {
    if (fileNameAndSizeExractor == null) return null;
    final Matcher matcher = fileNameAndSizeExractor.matcher(line);

    if (matcher.find()) {
      String fileName = matcher.group(1).trim();
      if (fileName.length() == 0) return null;
      final boolean hasLineNumber = matcher.group(2) != null;
      final boolean hasColumnNumber = matcher.groupCount() > 2 && matcher.group(3) != null;
      final int lineNumber = hasLineNumber ? Integer.parseInt(matcher.group(2)):0;
      final int columnNumber = hasColumnNumber ? Integer.parseInt(matcher.group(3)):0;

      VirtualFile child = resolveFilename(fileName);
      if (child == null) child = VfsUtil.findRelativeFile(fileName, null);
      if (child == null) return null;

      return new BasicFormatResult(
        child,
        entireLength + matcher.start(1) - line.length(),
        entireLength + matcher.end(hasLineNumber ? (hasColumnNumber ? 3:2):1) - line.length(),
        lineNumber,
        columnNumber,
        line.indexOf("error") != -1
      );
    }
    return null;
  }

  public static class BasicFormatResult extends Result {
    public final int line;
    public final int column;
    public final VirtualFile file;
    private final boolean isError;

    public BasicFormatResult(VirtualFile _file, int start, int end, int _line, int _column, boolean error) {
      super(start, end, createHyperLink(_file, _line, _column));
      file = _file;
      line = _line;
      column = _column;
      isError = error;
    }

    private static HyperlinkInfo createHyperLink(final VirtualFile child1, final int lineNumber, final int columnNumber) {
      return new HyperlinkInfo() {
        public void navigate(Project project) {
          new OpenFileDescriptor(project, child1).navigate(lineNumber == 0);

          if (lineNumber != 0) {
            final FileEditor[] fileEditors = FileEditorManager.getInstance(project).getEditors(child1);

            Editor editor = null;

            for(FileEditor fe:fileEditors) {
              if (fe instanceof TextEditor) {
                editor = ((TextEditor)fe).getEditor();
                break;
              }
            }

            if (editor != null) {
              int offset = editor.getDocument().getLineStartOffset(lineNumber - 1) + (columnNumber != 0?columnNumber - 1:0);
              new OpenFileDescriptor(project, child1,offset).navigate(true);
            }
          }
        }
      };

    }

    public boolean isError() {
      return isError;
    }
  }

  protected VirtualFile resolveFilename(String fileName) {
    return VfsUtil.findRelativeFile(fileName, currentContext);
  }
}
