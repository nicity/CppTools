package tests;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference;

/**
 * @author maxim
 */
public class CppParsingTest extends BaseCppTestCase {

  protected String getTestDataPath() {
    return "testData/parsing";
  }

  public void test() throws Throwable {
    doParseTest("SimpleParse");
    String docContent = myFixture.getEditor().getDocument().getCharsSequence().toString();
    String marker = "warn";

    PsiReference psiReference = myFixture.getFile().findReferenceAt(docContent.indexOf(marker) + marker.length());
    assertNotNull(psiReference);
    assertTrue(!(psiReference instanceof PsiMultiReference));

    marker = "operator ==";
    int offset = docContent.indexOf(marker) + marker.length() - 1;
    psiReference = myFixture.getFile().findReferenceAt(offset);
    assertNotNull(psiReference);
    assertTrue(!(psiReference instanceof PsiMultiReference));

    PsiElement psiElement = TargetElementUtil.getInstance().findTargetElement(myFixture.getEditor(), TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED |
      TargetElementUtil.ELEMENT_NAME_ACCEPTED, offset);

    assertNotNull(psiElement);

    marker = "operator=";
    offset = docContent.indexOf(marker) + marker.length() - 1;
    psiReference = myFixture.getFile().findReferenceAt(offset);
    assertNotNull(psiReference);
    assertTrue(!(psiReference instanceof PsiMultiReference));
    psiElement = TargetElementUtil.getInstance().findTargetElement(myFixture.getEditor(), TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED |
      TargetElementUtil.ELEMENT_NAME_ACCEPTED, offset);
    assertNotNull(psiElement);
  }

  public void testParseWithMacrosDef() throws Throwable {
    doParseTest(getTestName());
  }

  public void testParseWithNs() throws Throwable {
    doParseTest(getTestName());
  }

  private void doParseTest(String fileName) throws Throwable {
    super.doParseTest(fileName, "cpp");
  }
}
