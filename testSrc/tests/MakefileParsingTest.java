package tests;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference;

/**
 * @author maxim
 */
public class MakefileParsingTest extends BaseCppTestCase {
  protected String getTestDataPath() {
    return "testData/makefile_parsing";
  }

  public void testMakefile1() throws Throwable {
    doParseTest(getTestName(),"mk");
  }

  public void testMakefile2() throws Throwable {
    doParseTest(getTestName(),"mk");
  }

  public void testMakefile3() throws Throwable {
    doParseTest(getTestName(),"mk");
  }

  public void testCheckRefResolve() throws Throwable {
    refTest();
  }

  private void refTest() throws Throwable {
    myFixture.testHighlighting(getTestName() + ".mk");
    PsiReference psiReference = myFixture.getFile().findReferenceAt(myFixture.getEditor().getCaretModel().getOffset());
    assertNotNull(psiReference);
    PsiElement psiElement = psiReference.resolve();
    assertNotNull(psiElement);
  }

  public void testCheckRefResolve2() throws Throwable {
    refTest();
  }

  public void testCheckRefResolve3() throws Throwable {
    myFixture.testHighlighting(getTestName() + ".mk");
    PsiReference psiReference = myFixture.getFile().findReferenceAt(myFixture.getEditor().getCaretModel().getOffset());
    assertTrue(psiReference instanceof PsiMultiReference);
    ResolveResult[] resolveResults = ((PsiMultiReference) psiReference).multiResolve(false);
    assertEquals(2, resolveResults.length);
    
    assertNotNull(resolveResults[0].getElement());
    assertNotNull(resolveResults[1].getElement());

    // TODO
    //assertEquals(2, ReferencesSearch.search(resolveResults[0].getElement()).findAll().size());
    //assertEquals(2, ReferencesSearch.search(resolveResults[1].getElement()).findAll().size());
  }

  public void testCompleteTargetRef() throws Throwable {
    myFixture.testCompletion(getTestName() + ".mk", getTestName() + "_after.mk");
  }

  public void testCompleteVar() throws Throwable {
    myFixture.testCompletion(getTestName() + ".mk", getTestName() + "_after.mk");
  }
}