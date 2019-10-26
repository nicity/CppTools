// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.utils;

import com.advancedtools.cpp.sdk.CppModuleBuilder;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.util.Properties;

/**
 * @author maxim
 */
public class TemplateUtils {
  public static final String MAKEFILE_TEMPLATE_NAME = "Makefile";
  public static final String C_MAIN_TEMPLATE_NAME = "C_Main";
  public static final String CPP_MAIN_TEMPLATE_NAME = "Cpp_Main";

  public static String getTemplateText(String fileName, String ... additionalParameters) throws IOException {
    Properties properties = FileTemplateManager.getInstance().getDefaultProperties();

    if (additionalParameters != null) {
      for(int i = 0; i < additionalParameters.length; ++i) {
        String paramName = additionalParameters[i];
        if (i + 1 < additionalParameters.length) {
          ++i;
          properties.put(paramName, additionalParameters[i]);
        }
      }
    }
    final FileTemplate fileTemplate = FileTemplateManager.getInstance().getTemplate(fileName);
    assert fileTemplate != null;
    String text = fileTemplate.getText(properties);
    return text;
  }

  public static void createOrResetFileContent(VirtualFile sourcePathDir, String fileName, StringBufferInputStream inputStream) throws IOException {
    VirtualFile child = sourcePathDir.findChild(fileName);
    if (child == null) child = sourcePathDir.createChildData(CppModuleBuilder.class, fileName);
    OutputStream outputStream = child.getOutputStream(CppModuleBuilder.class);

    FileUtil.copy(inputStream, outputStream);
    outputStream.flush();
    outputStream.close();
  }

  public static void createOrResetFileContentFromTemplate(VirtualFile sourcePathDir, String fileName, 
                                                          String templateFileName, String ... additionalParameters) {
    try {
      createOrResetFileContent(sourcePathDir, fileName, new StringBufferInputStream(getTemplateText(templateFileName, additionalParameters)));
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
