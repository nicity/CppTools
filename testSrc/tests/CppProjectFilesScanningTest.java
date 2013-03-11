package tests;

import junit.framework.TestCase;
import com.advancedtools.cpp.build.BaseBuildHandler;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author maxim
 */
public class CppProjectFilesScanningTest extends TestCase {
  public void testDspInfoRetrieval() throws IOException {
    BuildingCommandHelper.ProjectAndBuildFileIncludeProcessor buildFileProcessor = new
      BuildingCommandHelper.DspProjectAndBuildFileIncludeProcessor(BaseBuildHandler.DEBUG_CONFIGURATION_NAME) {

        public String getPathVariableValue(String varName) {
          return defaultGetPathVariables(varName);
        }
      };

    doTest(new String[]{"P_r_o_j_e_c_t_P_a_t_h\\..\\stdafx.h"}, buildFileProcessor, new String[]{"WIN32","_MBCS","ICONTEXTMENU_JNI_EXPORTS","_USRDLL", "_WINDOWS", "_DEBUG"}, "A.dsp");
  }

  private static String defaultGetPathVariables(String varName) {
    if (BuildingCommandHelper.BaseProjectAndBuildFileIncludeProcessor.PROJECT_DIR_VAR_NAME.equals(varName)) return "P_r_o_j_e_c_t_P_a_t_h";
    return "!" + varName + "!";
  }

  public void testVCProjectInfoRetrieval() throws IOException {
    BuildingCommandHelper.ProjectAndBuildFileIncludeProcessor buildFileProcessor = new
      BuildingCommandHelper.VCProjProjectAndBuildFileIncludeProcessor(BaseBuildHandler.DEBUG_CONFIGURATION_NAME) {

        public String getPathVariableValue(String varName) {
          return defaultGetPathVariables(varName);
        }
      };

    doTest(
      new String[]{"P_r_o_j_e_c_t_P_a_t_h\\..", "P_r_o_j_e_c_t_P_a_t_h\\..\\css", "P_r_o_j_e_c_t_P_a_t_h\\..\\editing", "P_r_o_j_e_c_t_P_a_t_h\\..\\rendering",
      "P_r_o_j_e_c_t_P_a_t_h\\..\\bindings\\js", "P_r_o_j_e_c_t_P_a_t_h\\..\\dom", "P_r_o_j_e_c_t_P_a_t_h\\..\\history", "P_r_o_j_e_c_t_P_a_t_h\\..\\html",
      "P_r_o_j_e_c_t_P_a_t_h\\..\\bridge", "P_r_o_j_e_c_t_P_a_t_h\\..\\bridge\\win", "P_r_o_j_e_c_t_P_a_t_h\\..\\loader", "P_r_o_j_e_c_t_P_a_t_h\\..\\loader\\icon",
      "P_r_o_j_e_c_t_P_a_t_h\\..\\page", "P_r_o_j_e_c_t_P_a_t_h\\..\\platform", "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\win", "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\network",
      "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\network\\win", "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\cf", "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\network\\cf",
      "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\graphics", "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\graphics\\cairo", "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\graphics\\cg",
      "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\graphics\\win", "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\image-decoders", "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\image-decoders\\bmp",
      "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\image-decoders\\gif", "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\image-decoders\\ico", "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\image-decoders\\jpeg",
      "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\image-decoders\\png", "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\image-decoders\\xbm", "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\image-decoders\\zlib",
      "P_r_o_j_e_c_t_P_a_t_h\\..\\xml","!WebKitOutputDir!\\obj\\WebCore\\DerivedSources","P_r_o_j_e_c_t_P_a_t_h\\..\\plugins\\win",
      "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\cairo\\pixman\\src", "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\cairo\\cairo\\src","P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\graphics\\svg",
      "P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\graphics\\svg\\cg","P_r_o_j_e_c_t_P_a_t_h\\..\\platform\\graphics\\svg\\filters","P_r_o_j_e_c_t_P_a_t_h\\..\\kcanvas",
      "P_r_o_j_e_c_t_P_a_t_h\\..\\kcanvas\\device","P_r_o_j_e_c_t_P_a_t_h\\..\\kcanvas\\device\\quartz","P_r_o_j_e_c_t_P_a_t_h\\..\\ksvg2","P_r_o_j_e_c_t_P_a_t_h\\..\\ksvg2\\css",
      "P_r_o_j_e_c_t_P_a_t_h\\..\\ksvg2\\events","P_r_o_j_e_c_t_P_a_t_h\\..\\ksvg2\\misc","P_r_o_j_e_c_t_P_a_t_h\\..\\ksvg2\\svg", "!WebKitOutputDir!\\include",
      "!WebKitOutputDir!\\include\\JavaScriptCore","P_r_o_j_e_c_t_P_a_t_h\\..\\ForwardingHeaders", "!WebKitLibrariesDir!\\include","!WebKitLibrariesDir!\\include\\icu",
      "!WebKitLibrariesDir!\\include\\iconv", "!WebKitLibrariesDir!\\include\\pthreads", "!WebKitLibrariesDir!\\include\\sqlite", "!WebKitLibrariesDir!\\include\\JavaScriptCore",
      "!WebKitLibrariesDir!\\Include\\CoreFoundation\\OSXCompatibilityHeaders", "!WebKitLibrariesDir!\\Include\\CoreFoundation\\OSXCompatibilityHeaders\\GNUCompatibility"
        
      },
      buildFileProcessor,
      new String[]{"WIN32","__WIN32__","_SCL_SECURE_NO_DEPRECATE","_CRT_SECURE_NO_DEPRECATE", "ENABLE_XSLT", "ENABLE_XPATH",
      "ENABLE_SVG","WEBCORE_CONTEXT_MENUS", "USE_SAFARI_THEME"}, 
      "A.vcproj"
    );
  }

  // ToDo let the user choose what type of the dsp/vcproj and path variables to use
  
  private static void doTest(String[] includeDefs, BuildingCommandHelper.ProjectAndBuildFileIncludeProcessor buildFileProcessor, String[] ppdefs, String projectFileName) throws IOException {
    BuildingCommandHelper.doScanFileContent(
      new FileInputStream(new File(".","testData/project_files/" + projectFileName)),
      buildFileProcessor
    );

    Set<String> expectedIncludePathes = new LinkedHashSet<String>(Arrays.asList(includeDefs));
    Collection<String> actualIncludePathes = buildFileProcessor.getIncludes();

    for(String actualIncludePath:actualIncludePathes) {
      assertTrue("Actual path is not present:"+actualIncludePath,expectedIncludePathes.contains(actualIncludePath));
    }
    assertEquals(expectedIncludePathes.size(), actualIncludePathes.size());

    final String[] expectedIncludesArray = expectedIncludePathes.toArray(new String[expectedIncludePathes.size()]);
    final String[] actualIncludesArray = actualIncludePathes.toArray(new String[actualIncludePathes.size()]);

    for(int i = 0; i < actualIncludesArray.length;++i) {
      assertEquals(
        "Should be in the same order, expected: "+expectedIncludesArray[i] + ", actual:"+actualIncludesArray[i],
        expectedIncludesArray[i],
        actualIncludesArray[i]
      );
    }

    final Set<String> expectedPreprocessorSymbols = new HashSet<String>(
      Arrays.asList(ppdefs)
    );

    final Collection<String> actualPreprocessorSymbols = buildFileProcessor.getPreprocessorDefinitions();

    for(String actualPreprocessorSym:actualPreprocessorSymbols) {
      assertTrue(
        "Should contain " + actualPreprocessorSym + " in expected pp defs",
        expectedPreprocessorSymbols.contains(actualPreprocessorSym)
      );
    }

    assertEquals("Not all preprocessor defs was found",expectedPreprocessorSymbols.size(), actualPreprocessorSymbols.size());
  }
}
