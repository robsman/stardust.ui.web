/*
 * 
 */
(function () {
	'use strict';
	angular.module('shell').filter('interpolate', function () {
		'use strict';

		return function (text, val) {
			if (text) {
				var str = String(text);

				if ( typeof (val) !== 'object' && typeof (val) !== 'array') {
					val = [val];
				}
				for (var itm in val) {
					if (itm) {
						var re = new RegExp('\\{' + itm + '\\}', 'mg');
						str = str.replace(re, val[itm]);
					}
				}
				return str;
			} else {
				return '';
			}
		};
	});
})();

