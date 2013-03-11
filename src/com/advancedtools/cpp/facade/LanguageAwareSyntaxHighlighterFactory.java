package com.advancedtools.cpp.facade;

import com.advancedtools.cpp.LanguageFeatureAware;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
* @author maxim
* Date: 2/3/12
* Time: 1:53 PM
*/
public class LanguageAwareSyntaxHighlighterFactory extends SyntaxHighlighterFactory {

  @NotNull
  public SyntaxHighlighter getSyntaxHighlighter(Project project, VirtualFile virtualFile) {
    if (virtualFile != null) {
      FileType fileType = virtualFile.getFileType();
      if (fileType instanceof LanguageFileType) {
        Language language = ((LanguageFileType) fileType).getLanguage();
        if (language instanceof LanguageFeatureAware) {
          return ((LanguageFeatureAware) language).getSyntaxHighlighter(project, virtualFile);
        }
      }
    }

    return new PlainSyntaxHighlighter();
  }
}
