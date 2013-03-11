/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.actions.refactoring;

import com.advancedtools.cpp.actions.BaseEditorAction;
import com.advancedtools.cpp.commands.StringCommand;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

/**
 * @author maxim
 */
public class MoveMethodDefinitionToDeclarationAction extends BaseEditorAction {
  @Override
  protected void execute(Editor editor, PsiFile file, final Project project) {
    String filePath = file.getVirtualFile().getPath();

    StringCommand command = new StringCommand("function-to-inline " + BuildingCommandHelper.quote(filePath) + " " +
      editor.getCaretModel().getOffset()) {
      private final ChangesSupport changesSupport = new ChangesSupport();

      @Override
      public void commandOutputString(String str) {
        changesSupport.appendChangesFromString(str);
      }

      @Override
      public void doExecute() {
//        UsageViewManager.getInstance(project).showUsages(
//          new UsageTarget[] {}, new Usage[] {}, new UsageViewPresentation(), new Factory<UsageSearcher>() {
//          public UsageSearcher create() {
//            return new UsageSearcher() {
//              public void generate(Processor<Usage> usageProcessor) {
//              }
//            };
//          }
//        });
        changesSupport.applyChanges("CppSupport."+getClass().getName(), project);
      }

      @Override
      public boolean doInvokeInDispatchThread() {
        return true;
      }
    };
    command.post(project);
  }

  protected boolean acceptableState(Editor editor, PsiFile file) {
    return true;
  }
}
