cd src/main/resources

del /s html5-business-control-center-resources.js
del /s html5-business-control-center-resources.css

move /y META-INF\webapp\portal-plugin-dependencies-org.json META-INF\webapp\portal-plugin-dependencies.json

cd ../../../