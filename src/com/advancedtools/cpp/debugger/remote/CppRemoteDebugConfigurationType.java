package com.advancedtools.cpp.debugger.remote;

import com.advancedtools.cpp.CppSupportLoader;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.project.Project;
import com.advancedtools.cpp.sdk.CppSdkType;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;

/**
 * @author maxim
 * Date: 28.03.2009
 * Time: 20:09:12
 */
public class CppRemoteDebugConfigurationType implements ConfigurationType {
  private final ConfigurationFactory myFactory;

    public CppRemoteDebugConfigurationType() {
      myFactory = new ConfigurationFactory(this) {
        public RunConfiguration createTemplateConfiguration(Project project) {
          return new CppRemoteDebugConfiguration(project, this, "");
        }
      };
    }

    public String getDisplayName() {
      return "Cpp Remote Debug";
    }

    public String getConfigurationTypeDescription() {
      return "Cpp Remote Debug";
    }

    public Icon getIcon() {
      return CppSupportLoader.ourSdkIcon;
    }

    public ConfigurationFactory[] getConfigurationFactories() {
      return new ConfigurationFactory[] {myFactory};
    }

    @NotNull
    public String getId() {
      return "CppRemoteDebugConfigurationType";
    }
}