cd src/main/resources

del /s benchmark-resources.js
del /s benchmark-resources.css

move /y META-INF\webapp\html5\portal-plugin-dependencies-org.json META-INF\webapp\html5\portal-plugin-dependencies.json

cd ../../../