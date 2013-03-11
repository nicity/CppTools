/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.sdk;

import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.facade.EnvironmentFacade;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.util.Computable;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author maxim
 */
public class CppSdkType extends SdkType implements ApplicationComponent {
  public CppSdkType() {
    super("CppSdk");
  }

  @Nullable
  public String suggestHomePath() {
    return null;
  }

  public boolean isValidSdkHome(String path) {
//    VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
    return true; //TODO: file != null && (CppSupportSettings.isGccPath(file) || CppSupportSettings.isMsVcDirectory(file));
  }

  @Nullable
  public String getVersionString(String sdkHome) {
    return null;
  }

  public String suggestSdkName(String currentSdkName, String sdkHome) {
    return sdkHome.substring(sdkHome.lastIndexOf('/') + 1);
  }

  public void setupSdkPaths(Sdk sdk) {
  }

  public AdditionalDataConfigurable createAdditionalDataConfigurable(SdkModel sdkModel, SdkModificator sdkModificator) {
    return null;
  }

  public void saveAdditionalData(SdkAdditionalData additionalData, Element additional) {
  }

  public SdkAdditionalData loadAdditionalData(Element additional) {
    return null;
  }

  public String getPresentableName() {
    return "Cpp Sdk";
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return "CppTools.CppSdk";
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  @NotNull List<Sdk> getCppSdks() {
    List<Sdk> sdks = null;
    EnvironmentFacade facade = EnvironmentFacade.getInstance();
    for(Sdk jdk:ProjectJdkTable.getInstance().getAllJdks()) {
      if (facade.isSdkOfType(jdk, this)) {
        if (sdks == null) sdks = new ArrayList<Sdk>(1);
        sdks.add(jdk);
      }
    }
    return sdks != null ? sdks: Collections.<Sdk>emptyList();
  }

  public static CppSdkType getInstance() {
    return ApplicationManager.getApplication().getComponent(CppSdkType.class);
  }

  public Sdk createOrGetSdkByPath(final String s) {
    for(Sdk jdk:getCppSdks()) {
      if (jdk.getHomePath().equals(s)) return jdk;
    }
    return ApplicationManager.getApplication().runWriteAction(new Computable<Sdk>() {
      public Sdk compute() {
        try {
          Sdk jdk = EnvironmentFacade.getInstance().createSdk("Cpp SDK", getInstance());
          SdkModificator sdkModificator = addSdkVersion(jdk);
          sdkModificator.setHomePath(s);
          sdkModificator.commitChanges();
          ProjectJdkTable.getInstance().addJdk(jdk);
          return jdk;
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  private static SdkModificator addSdkVersion(Sdk jdk) {
    SdkModificator sdkModificator = jdk.getSdkModificator();
    sdkModificator.setVersionString("xxx");
    return sdkModificator;
  }

  @Override
  public Icon getIcon() {
    return CppSupportLoader.ourSdkIcon;
  }

  @Override
  public Icon getIconForAddAction() {
    return CppSupportLoader.ourSdkIcon;
  }

  public boolean setupSdkPaths(Sdk sdk, SdkModel sdkModel) {
    SdkModificator sdkModificator = addSdkVersion(sdk);
    sdkModificator.commitChanges();
    return true;
  }
}
