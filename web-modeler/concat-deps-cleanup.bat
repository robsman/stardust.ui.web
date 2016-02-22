cd src/main/resources

del /s bpm-modeler-resources.js
del /s bpm-modeler-resources.css

move /y META-INF\xhtml\html5\portal-plugin-dependencies-org.json META-INF\xhtml\html5\portal-plugin-dependencies.json

cd ../../../