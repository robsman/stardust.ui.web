cd src/main/resources

del /s all-resources.js
del /s all-resources.css

move /y META-INF\webapp\html5\portal-plugin-dependencies-org.json META-INF\webapp\html5\portal-plugin-dependencies.json

cd ../../../