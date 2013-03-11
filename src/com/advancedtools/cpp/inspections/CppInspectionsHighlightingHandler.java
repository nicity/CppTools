/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.inspections;

import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.facade.EnvironmentFacade;
import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author maxim
 */
public class CppInspectionsHighlightingHandler implements InspectionToolProvider, ApplicationComponent {
  @NonNls
  @NotNull
  public String getComponentName() {
    return "Cpp.InspectionsHighlightingHandler";
  }

  public void initComponent() {
    CppSupportLoader.doRegisterExtensionPoint("com.intellij.inspectionToolProvider", this, null);
  }

  public void disposeComponent() {
  }

  public Class[] getInspectionClasses() {
    if (EnvironmentFacade.isJavaIde()) {
      return new Class[] {
        UnusedCppSymbolInspection.class,
        NativeJavaMethodsInspection.class,
        JNIImplementationsInspection.class
      };
    } else {
      return new Class[] { UnusedCppSymbolInspection.class };
    }
  }
}