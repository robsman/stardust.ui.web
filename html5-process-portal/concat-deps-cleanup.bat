cd src/main/resources

del /s html5-process-portal-resources.js
del /s html5-process-portal-resources.css

move /y META-INF\webapp\portal-plugin-dependencies-org.json META-INF\webapp\portal-plugin-dependencies.json

cd ../../../