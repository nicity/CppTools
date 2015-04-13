1. Plugin needs IntelliJ IDEA Plugin SDK defined and named as "IDEA", or you will need to modify the customJdkName
  field in the CppTools_Cardea.ipr with the name of your plugin SDK.
2. Ant file needs IDEA_SANDBOX_HOME_PATH variable defined in settings, that is location defined in Idea plugin sdk.
3. Ant file assumes that native analyzer binaries and its resources are located in paret directory of the project.
  When developing plugin stand-alone these binaries and resources are located in "lib" directory of existing plugin.
4. Ant will also require a "DOC" directory exists in the parent directory of the project, so you will want to create it.