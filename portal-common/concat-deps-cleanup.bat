cd src/main/resources

del /s common-resources.js
del /s common-resources.css

move /y META-INF\xhtml\html5\portal-plugin-dependencies-org.json META-INF\xhtml\html5\portal-plugin-dependencies.json

cd ../../../