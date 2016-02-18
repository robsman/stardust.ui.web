echo off

cd src/main/resources

del /s benchmark-resources.js
del /s benchmark-resources.css

move /y META-INF\webapp\html5\portal-plugin-dependencies-org.json META-INF\webapp\html5\portal-plugin-dependencies.json

cd ../../../
java -classpath c:/deps/*;../portal-common/target/classes;./src/main/resources org.eclipse.stardust.ui.web.html5.utils.ConcatDependencies src/main/resources