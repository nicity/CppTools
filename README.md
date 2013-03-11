1. Plugin needs IntelliJ IDEA Plugin SDK defined.
2. Ant file needs IDEA_SANDBOX_HOME_PATH variable defined in settings, that is location defined in Idea plugin sdk.
3. Ant file assumes that native analyzer binaries and its resources are located in paret directory of the project.
  When developing plugin stand-alone these binaries and resources are located in "lib" directory of existing plugin.