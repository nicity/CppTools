/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.hilighting;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeHighlighting.TextEditorHighlightingPass;
import com.intellij.codeHighlighting.TextEditorHighlightingPassRegistrar;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author maxim
 */
public class CppOverridenHighlightingPassFactory extends CppHighlightingPassFactoryBase {
  public CppOverridenHighlightingPassFactory(Project _project) {
    super(_project);
  }

  protected void register(TextEditorHighlightingPassRegistrar instance) {
    instance.registerTextEditorHighlightingPass(
      this,
      TextEditorHighlightingPassRegistrar.BEFORE,
      Pass.LOCAL_INSPECTIONS
    );
  }

  protected TextEditorHighlightingPass doCreatePass(Editor editor, PsiFile psiFile, HighlightCommand command) {
    return new OverridenHighlightingPass(editor, psiFile,command);
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return "Cpp.OverridenHighlightingHandler";
  }

  static class OverridenHighlightingPass extends HighlightingPassBase {
    private static Key<Long> updateMarkKey = Key.create("override.update.mark");

    public OverridenHighlightingPass(Editor editor, PsiFile psiFile, HighlightCommand command) {
      super(editor, psiFile, command);
    }

    protected void await() {
      command.awaitOverriden(editor.getProject());
    }

    protected Key<Long> getUpdateMarkKey() {
      return updateMarkKey;
    }

    protected void addMarkers() {
      command.addOverridenRangeMarkers(editor);
    }

    public int getPassId() {
      return 2082001;
    }
  }

}