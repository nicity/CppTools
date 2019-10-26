// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.run;

import com.intellij.execution.*;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.advancedtools.cpp.sdk.CppSdkType;
import com.advancedtools.cpp.CppSupportLoader;

import javax.swing.*;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * @author maxim
 * Date: 28.03.2009
 * Time: 20:09:12
 */
public class CppRunConfigurationType implements ConfigurationType {
  private final ConfigurationFactory myFactory;

    public CppRunConfigurationType() {
      myFactory = new ConfigurationFactory(this) {
        public RunConfiguration createTemplateConfiguration(Project project) {
          return new CppRunConfiguration(project, this, "");
        }
      };
    }

    public String getDisplayName() {
      return "Cpp";
    }

    public String getConfigurationTypeDescription() {
      return "Cpp Run";
    }

    public Icon getIcon() {
      return CppSupportLoader.ourSdkIcon; // TODO
    }

    public ConfigurationFactory[] getConfigurationFactories() {
      return new ConfigurationFactory[] {myFactory};
    }

    @NotNull
    public String getId() {
      return "CppRunConfigurationType";
    }
}
