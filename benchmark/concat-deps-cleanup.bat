echo off

cd src/main/resources

if "%1" == "true" (
	echo "Not deleting generated resources"
) else (
	del /s benchmark-resources.js
	del /s benchmark-resources.css
)

move /y META-INF\webapp\html5\portal-plugin-dependencies-org.json META-INF\webapp\html5\portal-plugin-dependencies.json

cd ../../../