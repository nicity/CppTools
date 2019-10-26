// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurableContributor;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigurableContext;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
* Created by maximmossienko on 30/11/14.
*/
public class CppProjectConfigurableProvider extends ProjectStructureConfigurableContributor {
  @NotNull
  @Override
  public List<? extends Configurable> getExtraProjectConfigurables(@NotNull Project project, @NotNull StructureConfigurableContext context) {
    return new SmartList<Configurable>(new ProjectConfigurable(project));
  }

  private static class ProjectConfigurable implements Configurable {
    private final Project myProject;

    private ProjectConfigurable(Project project) {
      this.myProject = project;
    }

    @Nls
    public String getDisplayName() {
      return "C/C++";
    }

    @Nullable
    @NonNls
    public String getHelpTopic() {
      return null;
    }


    private CppSupportLoader.ProjectSettingsForm form;

    public JComponent createComponent() {
      CppSupportLoader instance = CppSupportLoader.getInstance(myProject);
      form = instance.new ProjectSettingsForm();
      reset();
      return form.getProjectSettingsPanel();
    }

    public boolean isModified() {
      return form != null && form.isModified();
    }

    public void apply() throws ConfigurationException {
      if(form != null) form.apply();
    }

    public void reset() {
      if (form != null) form.init();
    }

    public void disposeUIResources() {
      form = null;
    }

  }
}
