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