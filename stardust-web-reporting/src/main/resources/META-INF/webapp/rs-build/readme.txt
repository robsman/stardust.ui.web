pre-requisite: install nodeJs
place rs-build folder in parallel with js folder (js contains libs and report folders)
-------------
ReportDefinitionController.js
ReportingService.js
ReportRenderingController.js

Replace all imports from report folder js
bpm-reporting/js -> ..

I18NUtils.js
remove references of InfinityBPMI18N and remove initI18N() call
(internationalization is not supported)

ReportViewMain.js (also refer ReportDefinitionViewMain.js in there are any new additions)
remove require.config block
Change paths  bpm-reporting/js -> ..
remove references of 
jquery.url
jquery.base64
TableTools (included in reportTemplate.html)
ckeditor
ace
Note: this file contains all js which are required to render the report

r.js build
Open Command Prompt
Goto stardust-web-reporting\src\main\resources\META-INF\webapp\public\rs-build directory 
#node r.js -o build.js optimize=none
Note: committing the working set - report_adjusted_for_reference dir
------------
inlined css 
check reportDefinitionView.html file for all css imports
accordingly modify style.css
#node r.js -o build-css.js

-------------
properties file is prepared manually
var message_bundle = {*****};
Use UiHelper.java-> main method to create this string.

That's it!
