package tests;

import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.CppSupportSettings;
import com.advancedtools.cpp.build.BuildState;
import com.advancedtools.cpp.communicator.Communicator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import junit.framework.TestCase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author maxim
 *         Date: Jan 3, 2007
 *         Time: 11:28:20 PM
 *         To change this template use File | Settings | File Templates.
 */
abstract class BaseCppTestCase extends TestCase {
  private CppSupportLoader loader;
  private CppSupportSettings settings;
  protected CodeInsightTestFixture myFixture;
  private Communicator communicator;

  // -Didea.load.plugins=false -Didea.plugins.path="-Didea.config.path=C:\Program Files\JetBrains\IntelliJ IDEA 6.0\debug\config"
  protected void setUp() throws Exception {
    super.setUp();
    final IdeaTestFixtureFactory fixtureFactory = IdeaTestFixtureFactory.getFixtureFactory();
    final TestFixtureBuilder<IdeaProjectTestFixture> testFixtureBuilder = fixtureFactory.createFixtureBuilder();

    myFixture = fixtureFactory.createCodeInsightFixture(testFixtureBuilder.getFixture());
    myFixture.setTestDataPath(new File("").getAbsolutePath() + File.separatorChar + getTestDataPath());
    myFixture.setUp();

    BuildState.invokeOnEDTSynchroneously(new Runnable() {
      public void run() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {

          public void run() {
            settings = new CppSupportSettings();
            settings.initComponent();
            loader = new CppSupportLoader(myFixture.getProject());
            loader.projectOpened();
            communicator = new Communicator(myFixture.getProject());
            communicator.projectOpened();
          }
        });
      }
    });
  }

  protected abstract
  @NonNls
  String getTestDataPath();

  protected void tearDown() throws Exception {
    communicator.projectClosed();
    loader.projectClosed();
    settings.disposeComponent();

    Runnable action = new Runnable() {
      public void run() {
        try {
          myFixture.tearDown();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
    BuildState.invokeOnEDTSynchroneously(action);
    myFixture = null;
    super.tearDown();
  }

  protected void doParseTest(final String fileName, @NotNull @NonNls final String ext) throws Throwable {
    Runnable action = new Runnable() {
      public void run() {
        try {
          myFixture.testHighlighting(fileName + (ext.length() > 0 ? "." + ext : ""));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        String s = DebugUtil.psiToString(myFixture.getFile(), true);
        final String expected = LoadTextUtil.loadText(
          LocalFileSystem.getInstance().findFileByIoFile(new File(getTestDataPath() + File.separator + fileName + ".txt"))
        ).toString();

        assertEquals(
          expected,
          s
        );
      }
    };
    BuildState.invokeOnEDTSynchroneously(action);
  }

  protected String getTestName() {
    return getName().substring(4);
  }
}