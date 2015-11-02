cd src/main/resources

del /s all-resources.js
del /s all-resources.css

move /y META-INF\xhtml\html5\portal-plugin-dependencies-org.json META-INF\xhtml\html5\portal-plugin-dependencies.json

cd ../../../