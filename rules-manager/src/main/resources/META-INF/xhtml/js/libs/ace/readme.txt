Core Modifications required to *.js files included in this folder.
-------------------------------------------------------------------

1. ext-language_tools.js

Modification:
Required By :m_drlAceEditor.js
Change:
	line#: 1350
	From: var ID_REGEX = /[a-zA-Z_0-9\$-]/;
	To:   var ID_REGEX = /[a-zA-Z_0-9\$-\.]/;
	Error: AutoCompletion matches will not match on '.', which will cause prefix matching of 
		   strings containing a dot to fail when the matcher encounters a '.' Specifically, this
		   will affect autocompletion of parameterDefinitions/Models.
		   
2. worker-javascript.js

Modification:

File has been modified such that its JSHINT module has been
replaced by Douglas Crockford's JSLINT module. The body of the replacement
occurs between lines 3148 and 9653. THe module itself still exports a varaible
named JSHINT. THis has not been renamed as to keep the internal replacement 
transparent to ACE. THis is accomplished at line no. 9649 ...

exports.JSHINT = JSLINT;

Side-effects: ACE panels running in Javascript mode will now complain just like
Douglas Crockford.
