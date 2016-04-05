This folder and its content is packaged in the ipp-portal.war which will be added to the
"stardust\ide.wst\org.eclipse.stardust.ide.wst.facet.portal\lib" folder during the build.

This web archive will be unpacked if the user creates a Dynamic Web Project with enabled
IPP Portal facets except the following files:
<patternset>
  <exclude name="META-INF/MANIFEST.MF"/>
  <exclude name="WEB-INF/classes/**"/>
  <exclude name="WEB-INF/web.xml"/>
</patternset>

This patternset is written in the "stardust\ide.wst\org.eclipse.stardust.ide.wst.facet.portal\tools\templates.xml"
file which may be changed in the meantime. If some further files should be excluded during the RAD creation
then the templates.xml file must be modified.

Please note web.xml changes needs to be added manually to (if available/applicable) -
- org.eclipse.stardust.ide.wst.facet.portal.InstallDelegate
- org.eclipse.stardust.ide.wst.facet.portal.UninstallDelegate
- "components\configure-configs\ipp-portal\properties.xml"
- "components\configure-configs\stardust-portal\properties.xml"
- "stardust\deploy.jee\archetypes\tc7-stardust-portal-war\src\main\resources\archetype-resources\stardust-portal\src\main\webapp\WEB-INF"