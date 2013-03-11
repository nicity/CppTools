package com.advancedtools.cpp.facade;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.util.Icons;
import com.intellij.xdebugger.ui.DebuggerIcons;

import javax.swing.*;

public class NikaFacade extends EnvironmentFacade {
    @Override
  public Icon getVerifiedBreakpointIcon() {
    return DebuggerIcons.VERIFIED_BREAKPOINT_ICON;
  }

  @Override
  public Object addModuleRootListener(Project project, ModuleRootListener moduleRootListener) {
    ProjectRootManager.getInstance(project).addModuleRootListener(moduleRootListener);
    return project;
  }

  @Override
  public void removeModuleRootListener(Object rootListenerConnectionData, ModuleRootListener rootListener) {
    ProjectRootManager.getInstance((Project)rootListenerConnectionData).removeModuleRootListener(rootListener);
  }

  @Override
  public Icon getStackFrameIcon() {
    return DebuggerIcons.STACK_FRAME_ICON;
  }

  @Override
  public Icon getAntMetaTargetIcon() {
    return Icons.ANT_META_TARGET_ICON;
  }

  @Override
  public boolean isSdkOfType(Sdk jdk, SdkType sdkType) {
    return jdk.getSdkType() == sdkType; // different return type in Idea 12
  }

  @Override
  public Sdk createSdk(String name, SdkType sdkType) {
    return new ProjectJdkImpl(name, sdkType);
  }
}
