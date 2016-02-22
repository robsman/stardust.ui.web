cd src/main/resources

del /s html5-admin-portal-resources.js
del /s html5-admin-portal-resources.css

move /y META-INF\webapp\portal-plugin-dependencies-org.json META-INF\webapp\portal-plugin-dependencies.json

cd ../../../