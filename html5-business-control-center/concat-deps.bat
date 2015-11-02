cd src/main/resources

del /s html5-business-control-center-resources.js
del /s html5-business-control-center-resources.css

move /y META-INF\webapp\portal-plugin-dependencies-org.json META-INF\webapp\portal-plugin-dependencies.json

cd ../../../
java -classpath c:/deps/*;../portal-common/target/classes;./src/main/resources org.eclipse.stardust.ui.web.html5.utils.ConcatDependencies src/main/resources

set /p pathName=Press Enter To Stop...