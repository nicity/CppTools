// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.sdk;

import com.advancedtools.cpp.CppBundle;
import com.advancedtools.cpp.CppSupportLoader;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectWizardStepFactory;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

/**
 * @author maxim
 */
public class CppModuleType extends ModuleType<CppModuleBuilder> implements ApplicationComponent {
  public CppModuleType() {
    super("C++");
  }

  public CppModuleBuilder createModuleBuilder() {
    return new CppModuleBuilder();
  }

  public String getName() {
    return CppBundle.message("c.cpp.module");
  }

  public ModuleWizardStep[] createWizardSteps(WizardContext wizardContext, CppModuleBuilder cppModuleBuilder, ModulesProvider modulesProvider) {
    final List<Sdk> list = CppSdkType.getInstance().getCppSdks();
    final ModuleWizardStep sourceModuleWizardStep = ProjectWizardStepFactory.getInstance().createSourcePathsStep(wizardContext, cppModuleBuilder, null, null);
    final ModuleWizardStep createSampleCode = new CreateEntryCodeStep(cppModuleBuilder);

    if (list.size() == 0) {
      return new ModuleWizardStep[] { new ChooseCppSdkStep(cppModuleBuilder, wizardContext),sourceModuleWizardStep, createSampleCode };
    }
    return new ModuleWizardStep[] { sourceModuleWizardStep, createSampleCode };
  }

  public String getDescription() {
    return CppBundle.message("c.cpp.module.description");
  }

  public Icon getBigIcon() {
    return CppSupportLoader.ourBigModuleIcon;
  }

  public Icon getNodeIcon(boolean isOpened) {
    return CppSupportLoader.ourModuleIcon;
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return "CppTools.ModuleType";
  }

  public void initComponent() {
    ModuleTypeManager.getInstance().registerModuleType(this);
  }

  public void disposeComponent() {
  }

  public static CppModuleType getInstance() {
    return ApplicationManager.getApplication().getComponent(CppModuleType.class);
  }

}
