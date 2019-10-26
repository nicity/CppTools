// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.facade;

import com.advancedtools.cpp.CppParserDefinition;
import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.communicator.Communicator;
import com.intellij.ide.IconProvider;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
* @author maxim
* Date: 2/3/12
* Time: 1:46 PM
*/
public class CppIconProvider extends IconProvider {
  private static final Key<CachedValue<Icon>> ourFileIconKey = Key.create("cppfile.icon");
  private static final Icon iconForSkippedFile = Icons.CUSTOM_FILE_ICON;

  public Icon getIcon(@NotNull PsiElement psiElement, int flags) {
    if (psiElement instanceof CppParserDefinition.CppFile) {
      final CppParserDefinition.CppFile cppFile = (CppParserDefinition.CppFile) psiElement;
      CachedValue<Icon> value = cppFile.getUserData(ourFileIconKey);

      if (value == null) {
        value = CachedValuesManager.getManager(cppFile.getManager().getProject()).createCachedValue(new CachedValueProvider<Icon>() {
          public Result<Icon> compute() {
            final VirtualFile virtualFile = cppFile.getVirtualFile();

            final boolean underSource = CppSupportLoader.isInSource(
              virtualFile, ProjectRootManager.getInstance(cppFile.getProject()).getFileIndex());

            return new Result<Icon>(
              underSource ?
                Communicator.isHeaderFile(virtualFile) ?
                  CppSupportLoader.ourIncludeIcon :
                  CppSupportLoader.ourCppIcon :
                iconForSkippedFile,
              ProjectRootManager.EVER_CHANGED
            );

//            final BlockingStringCommand stringCommand = new BlockingStringCommand(
//              "file-in-project "+ BuildingCommandHelper.quote(
//                BuildingCommandHelper.fixVirtualFileName(virtualFile.getPresentableUrl()),
//                true
//              )
//            );
//            stringCommand.post(cppFile.getProject());
//            String s1 = stringCommand.getCommandResult();
//
//            return new Result<Icon>( "false".equals(s1) ? iconForSkippedFile :null, serverRestartTracker);
          }
        }, false);
        cppFile.putUserData(ourFileIconKey, value);
      }

      return value.getValue();
    }
    return null;
  }
}
