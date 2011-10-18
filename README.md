# CytoSPADE Cytoscape Plugin for SPADE

The CytoSPADE Cytoscape plugin provides a GUI for setting-up and interactively visualizing the results of SPADE analyses. It is designed to work with the SPADE R-package. Please see the documentation for that project on how to use the plugin (and SPADE in general). This README and related documentation are primarily targeted at developers working on the plugin itself.

## Prerequisites
1. [Netbeans and the Java 1.6 SDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
1. [git](http://git-scm.com)
1. Latest version of [Cytoscape](http://www.cytoscape.org/download.html)

## Setup and Build Process
The CytoSPADE repository is itself a Netbeans project and can be opened directly with *Open a Project*. Note that you will likely get complaints about unresolved library paths. You can resolve these paths by setting the path to your Cytoscape installation via an "IDE Variable", specificallly via *Tools -> Variables* add a variable `CYTOSCAPE_PATH` that points to your top level Cytoscape installation directory. The project library dependencies are set relative to this variable and should be resolved at this point.

For direct integration of NetBeans and the github repository, download and install the [nbgit plugin](http://code.google.com/p/nbgit/downloads/list). Then, from the menu bar, click Team>Git>Clone other... and copy-and-paste the full HTTP URL for the github repository (above, https://username@github.com/nolanlab/cytospade.git). Note that this does not work with branches yet, and will pull from the master.

After you build the project (the hammer in the toolbar), there will be two jars created in the `dist/` directory, `CytoSPADE.jar` and `CytoSPADE.dist.jar`. The latter has all the non-Cytoscape dependencies compiled in, and is the file you should copy to the CytoScape plugins folder. You can set your build script to automatically copy the dist.jar to your plugins folder by modifying `build.xml` with the following:
To the "-post-jar" target at the end of the file, add the following two lines between `</jar>` and `</target>`:

```
<echo message="Copying ${application.title} to Cytoscape plugins folder"/>
<copy file="dist/${dist.jar.name}" todir="path\to\Cytoscape_v2.8.x\plugins\"/>
```

On Windows, you will need to change the permissions on the plugins folder to allow write access without UAC.

## Tips and Resources
* [Cytoscape plugin developer's cookbook](http://cytoscape.wodaklab.org/wiki/plugin_developer_tutorial)
* [Cytoscape 2.8.1 API](http://chianti.ucsd.edu/Cyto-2_8_1/javadoc/overview-summary.html)
