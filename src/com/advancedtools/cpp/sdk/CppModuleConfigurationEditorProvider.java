/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.sdk;

import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import com.intellij.openapi.roots.ui.configuration.DefaultModuleConfigurationEditorFactory;
import com.intellij.peer.PeerFactory;
import com.advancedtools.cpp.facade.EnvironmentFacade;
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

    return new ModuleConfigurationEditor[] {
      PeerFactory.getInstance().createModuleConfigurationEditor(
        rootModel.getModule().getName(), state
      ),
      DefaultModuleConfigurationEditorFactory.getInstance().createClasspathEditor(state),
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
