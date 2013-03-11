/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.hilighting;

import com.intellij.codeHighlighting.TextEditorHighlightingPass;
import com.intellij.codeHighlighting.TextEditorHighlightingPassRegistrar;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author maxim
 */
public class CppSimpleEditorHighlightingPassFactory extends CppHighlightingPassFactoryBase {
  public CppSimpleEditorHighlightingPassFactory(Project _project) {
    super(_project);
  }

  protected void register(TextEditorHighlightingPassRegistrar instance) {
    instance.registerTextEditorHighlightingPass(
      this,
      TextEditorHighlightingPassRegistrar.FIRST,
      0xff
    );
  }

  protected TextEditorHighlightingPass doCreatePass(Editor editor, PsiFile psiFile, HighlightCommand command) {
    return new SimpleHighlightingPass(editor, psiFile, command);
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return "Cpp.HighlightingHandler";
  }

  static class SimpleHighlightingPass extends HighlightingPassBase {
    private static Key<Long> updateMarkKey = Key.create("simple.update.mark");

    public SimpleHighlightingPass(Editor editor, PsiFile psiFile, HighlightCommand command) {
      super(editor, psiFile, command);
    }

    protected void await() {
      command.awaitHighlighting(editor.getProject());
    }

    protected Key<Long> getUpdateMarkKey() {
      return updateMarkKey;
    }

    protected void addMarkers() {
      command.addRangeMarkers(editor);
    }

    public int getPassId() {
      return 13101977;
    }
  }
}