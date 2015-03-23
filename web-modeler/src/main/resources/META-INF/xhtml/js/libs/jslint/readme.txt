Core Modifications required to *.js files included in this folder.
-------------------------------------------------------------------
jslint.js

Modification:
CRNT-34956
line 5572 is changed 
from -> s.string = s;
to -> x.string = s;
JSLint itself git fixed with commit  
https://github.com/douglascrockford/JSLint/commit/656150a46106206b915e1e881ce95e2ce0c12cd2