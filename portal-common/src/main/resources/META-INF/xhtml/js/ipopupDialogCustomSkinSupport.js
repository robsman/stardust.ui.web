$(function() {
	function getContextName() {
		return location.pathname
				.substring(0, location.pathname.indexOf('/', 1));
	}
	
	var loadCustomTheme = function() {
		jQuery.ajax({
			type : 'GET',
			url : getContextName() + "/services/rest/common/html5/api/themes/current/custom",
			async : true
		}).done(function(json){
			var head = document.getElementsByTagName('head')[0];
			
			for(var i in json.stylesheets) {
				var link = document.createElement('link');
				link.href = getContextName() + "/" + json.stylesheets[i];
				link.rel = 'stylesheet';
				link.type = 'text/css';
				head.appendChild(link);
			}
		}).fail(function(err){
			console.log("Failed in loading custom theme");
		});
	};
	
	loadCustomTheme();
});
