# FisPro2Knime

# Description
Every folder contains the sources of a Knime Plug-in and implements a FisPro routine.

# Prerequisites
<pre>
Eclipse IDE for Java Developers 4.6.1+ (Neon.1a)
FisPro 3.5+
Knime SDK 3.2.1+ on Eclipse
JRE v1.8+
</pre>

# Installing
1. Install from sources:
<pre>
  - Import the sources (in 'sources' folder) as projects in Eclipse IDE
  - Deploy it as a KNIME plugin through the functionality "Export deployable plugins and fragments" in the Eclipse environment
  - 'Select All' Plug-ins in the export dialog
  - Select an 'Archive file' as destination in the export dialog
  - Extract the archive file in the 'dropins' folder of Knime Analytics Platform installation directory
</pre>

2. Install from JARs:
<pre>
  - Copy the jar files (in 'jar' folder) into the 'KNIME_INSTALLATION_FOLDER/dropins/plugins/'
</pre>

# Usage
You will find the plugins in the Node Repository of Knime Analytics Platform.
To use the plugins properly, you must set a Workflow Variable:
<pre>
  1. Right Click on 'Workflow Project'
  2. Click on 'Workflow Variables'
  3. Press 'Add'
  4. Set 'Variable name' as 'FIS_BIN_PATH' (without single quote)
  5. Set 'Variable type' as 'STRING' (without single quote)
  6. Put in 'Default value' the path of FisPro 'bin' folder
  7. Click OK
</pre>
Now the plugins are ready to be used in a Workflow.

# References
* KNIME Website: <a href="www.knime.org">www.knime.org</a>
* FISPRO Website: <a href="https://www7.inra.fr/mia/M/fispro/fispro2013_en.html">https://www7.inra.fr/mia/M/fispro/fispro2013_en.html</a>
