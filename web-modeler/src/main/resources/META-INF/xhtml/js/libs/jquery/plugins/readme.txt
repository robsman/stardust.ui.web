Core Modifications required to *.js files included in this folder.
-------------------------------------------------------------------

1. jquery.jstree.js

Modification:
Required By :m_outline.js function addCamelOverlayMenuOptions
Change:
	line#: 3616
	From: $.vakata.context.func[i].call($.vakata.context.data, $.vakata.context.par);
	To:   $.vakata.context.func[i].call($.vakata.context.data, $.vakata.context.par,i);
	Error: Camel applications will not be added to the outline tree if this change is not made.