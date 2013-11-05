define([],function(){
	
	var factory={
			/*Completer which allows the user to specify a keyword list attached
			 *to a drlEditor session via session.ext_userDefined[key]
			 *@Param: extKey - Hash key where we can find our keywords.*/
			getSessionCompleter: function(extKey,options){
				var metaName="Data",score=0;
				if(options){
					metaName=options.metaName || metaName;
					score=options.score || score;
				}
				return {
				    getCompletions: function(editor, session, pos, prefix, callback) {
				        var keywords = session.ext_userDefined[extKey];
				        var t=session.getTextRange({
				        	"start":{row: pos.row,column:pos.column-1},
				        	"end":{row: pos.row,column:pos.column}
				        });
				        keywords = keywords.filter(function(w) {
				            return w.lastIndexOf(prefix, 0) == 0;
				        });
				        callback(null, keywords.map(function(word) {
				            return {
				                "name": word,
				                "value": word,
				                "score": score,
				                "meta": metaName
				            };
				        }));
				    }
				};
			}
	};
	return factory;
});