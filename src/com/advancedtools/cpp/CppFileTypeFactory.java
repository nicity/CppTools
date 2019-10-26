// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author maxim
 */
public class CppFileTypeFactory extends FileTypeFactory {
  @Override
  public void createFileTypes(@NotNull FileTypeConsumer fileTypeConsumer) {
    List<FileNameMatcher> matcherList = new ArrayList<FileNameMatcher>();
    for(String s:CppSupportLoader.extensions) {
      matcherList.add(new ExtensionFileNameMatcher(s));
    }

    for(String s:CppSupportLoader.filesWithEmptyExtensions()) {
      matcherList.add(new ExactFileNameMatcher(s));
    }

    fileTypeConsumer.consume(CppSupportLoader.CPP_FILETYPE, matcherList.toArray(new FileNameMatcher[matcherList.size()]));
    matcherList.clear();
    matcherList.add(new ExactFileNameMatcher("Makefile"));
    if (ApplicationManager.getApplication().isUnitTestMode()) matcherList.add(new ExtensionFileNameMatcher("mk"));
    fileTypeConsumer.consume(CppSupportLoader.MAKE_FILETYPE, matcherList.toArray(new FileNameMatcher[matcherList.size()]));
  }
}
