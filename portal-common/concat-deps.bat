cd src/main/resources

del /s all-resources.js
del /s all-resources.css

move /y META-INF\xhtml\html5\portal-plugin-dependencies-org.json META-INF\xhtml\html5\portal-plugin-dependencies.json

cd ../../../
java -classpath c:/deps/*;../portal-common/target/classes;./src/main/resources org.eclipse.stardust.ui.web.html5.utils.ConcatDependencies src/main/resources

set /p pathName=Press Enter To Stop...