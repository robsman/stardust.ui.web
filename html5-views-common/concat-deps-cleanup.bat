cd src/main/resources

del /s html5-views-common-resources.js
del /s html5-views-common-resources.css

move /y META-INF\webapp\html5\portal-plugin-dependencies-org.json META-INF\webapp\html5\portal-plugin-dependencies.json

cd ../../../