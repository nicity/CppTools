// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.sdk;

import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import com.intellij.openapi.roots.ui.configuration.DefaultModuleConfigurationEditorFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author maxim
 */
public class CppModuleConfigurationEditorProvider implements ModuleConfigurationEditorProvider, ModuleComponent {
  public ModuleConfigurationEditor[] createEditors(ModuleConfigurationState state) {
    final Module module = state.getRootModel().getModule();
    if (ModuleType.get(module) != CppModuleType.getInstance()) return new ModuleConfigurationEditor[0];

    ModifiableRootModel rootModel = state.getRootModel();

    DefaultModuleConfigurationEditorFactory defaultModuleConfigurationEditorFactory = DefaultModuleConfigurationEditorFactory.getInstance();
    return new ModuleConfigurationEditor[] {
      defaultModuleConfigurationEditorFactory.createModuleContentRootsEditor(state),
      defaultModuleConfigurationEditorFactory.createClasspathEditor(state),
    };
  }

  public void projectOpened() {
  }

  public void projectClosed() {
  }

  public void moduleAdded() {
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return "CppSupport.ModuleEditorProvider";
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }
}
