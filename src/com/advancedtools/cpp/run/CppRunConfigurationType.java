package com.advancedtools.cpp.run;

import com.intellij.execution.*;
import com.intellij.execution.configurations.ConfigurationFactory;
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
public class CppRunConfigurationType implements LocatableConfigurationType {
  private final ConfigurationFactory myFactory;

    public CppRunConfigurationType() {
      myFactory = new ConfigurationFactory(this) {
        public RunConfiguration createTemplateConfiguration(Project project) {
          return new CppRunConfiguration(project, this, "");
        }
      };
    }

  public RunnerAndConfigurationSettings createConfigurationByLocation(final Location location) {
      final CppRunnerParameters runnerParameters = createBuildParameters(location);
      if (runnerParameters == null) return null;

    final RunnerAndConfigurationSettings settings = RunManagerEx.getInstanceEx(location.getProject()).createConfiguration("Cpp", myFactory);
      CppRunConfiguration runConfiguration = (CppRunConfiguration)settings.getConfiguration();
      runConfiguration.setRunnerParameters(runnerParameters);
      return settings;
    }

    public boolean isConfigurationByLocation(final RunConfiguration configuration, final Location location) {
      return configuration instanceof CppRunConfiguration &&
             ((CppRunConfiguration)configuration).getRunnerParameters().equals(createBuildParameters(location));
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

    @Nullable
    private static CppRunnerParameters createBuildParameters(Location l) {
      if (!(l instanceof PsiLocation)) return null;

      final PsiFile containingFile = l.getPsiElement().getContainingFile();
      VirtualFile f = containingFile != null ? containingFile.getVirtualFile():null;
      if (f == null ||
          (f.getFileType() != CppSupportLoader.CPP_FILETYPE &&
           f.getFileType() != CppSupportLoader.MAKE_FILETYPE
          )
        ) {
        return null;
      }
      
      return new CppRunnerParameters();
    }
}
