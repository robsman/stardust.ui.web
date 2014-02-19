define(function(require){
	var underscoreService = require('js/libs/misc/underscore');
		underscoreRoot={},
	    underscore=angular.module('underscore',[]);
		
	underscoreService.init(underscoreRoot);
	underscore.factory( '_' , function(){
		return underscoreRoot._;
	});
	
	return {underscore : underscore};
});