// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.actions;

import com.advancedtools.cpp.navigation.CppSymbolContributor;
import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.ide.util.gotoByName.ChooseByNamePopupComponent;
import com.intellij.ide.util.gotoByName.ContributorsBasedGotoByModel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.application.ModalityState;
import com.intellij.navigation.NavigationItem;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

/**
 * @author maxim
 */
public abstract class GotoBaseAction extends GotoActionBase {
  public abstract CppSymbolContributor getNameContributor();

  protected void gotoActionPerformed(AnActionEvent anActionEvent) {
    final Project project = (Project) anActionEvent.getDataContext().getData(DataConstants.PROJECT);

    final ChooseByNamePopup byNamePopup = ChooseByNamePopup.createPopup(
      project,
      new MyContributorsBasedGotoByModel(project, getNameContributor()),
      (PsiElement)null
    );

    byNamePopup.invoke(new ChooseByNamePopupComponent.Callback() {
      public void elementChosen(Object element) {
        ((NavigationItem)element).navigate(true);
      }
      public void onClose() {
        if (GotoBaseAction.this.getClass().equals(myInAction)) myInAction = null;
      }
    }, ModalityState.current(), false);
  }

  protected abstract String getEnterTextPrefix();
  
  protected abstract String getNoEntityText();

  private class MyContributorsBasedGotoByModel extends ContributorsBasedGotoByModel {
    public MyContributorsBasedGotoByModel(Project project, CppSymbolContributor constantContributor) {
      super(project, new ChooseByNameContributor[]{constantContributor});
    }

    public String getPromptText() {
      return getEnterTextPrefix();
    }

    public String getNotInMessage() {
      return getNoEntityText();
    }

    public String getNotFoundMessage() {
      return "";
    }

    @Nullable
    public String getCheckBoxName() {
      return "";
    }

    public char getCheckBoxMnemonic() {
      return 'q';
    }

    public boolean loadInitialCheckBoxState() {
      return false;
    }

    public void saveInitialCheckBoxState(boolean state) {
    }

    public String getFullName(Object element) { return getElementName(element); }

    public boolean willOpenEditor() { return true; }

    public String[] getSeparators() { return new String[0]; }
  }
}
