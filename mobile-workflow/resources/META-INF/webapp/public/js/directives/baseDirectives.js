define(["angularjs","jquery"],function(angular){
	
	var directives = {
			
			"speechToText" : function(){
		        
		       var getCursorPosition = function(area) {
		          var el = area;
		          var pos = 0;
		          if('selectionStart' in el) {
		              pos = el.selectionStart;
		              console.log("getCursorPosition invocation");
		          }
		          return pos;
		        };
		        
		        var setCursorPosition = function(area,pos) {
		            if (area.setSelectionRange) {
		                area.setSelectionRange(pos, pos);
		            } else if (area.createTextRange) {
		                var range = area.createTextRange();
		                range.collapse(true);
		                range.moveEnd('character', pos);
		                range.moveStart('character', pos);
		                range.select();
		            }
		        };
		        
		        var insertAtCaret = function(area,text) {
		            var txtarea = area;
		            var scrollPos = txtarea.scrollTop;
		            var strPos = 0;
		            var br = ((txtarea.selectionStart || txtarea.selectionStart == '0') ?
		                "ff" : (document.selection ? "ie" : false ) );
		            if (br == "ie") {
		                txtarea.focus();
		                var range = document.selection.createRange();
		                range.moveStart ('character', -txtarea.value.length);
		                strPos = range.text.length;
		            }
		            else if (br == "ff") strPos = txtarea.selectionStart;
		    
		            var front = (txtarea.value).substring(0,strPos);
		            var back = (txtarea.value).substring(strPos,txtarea.value.length);
		            txtarea.value=front+text+back;
		            strPos = strPos + text.length;
		            if (br == "ie") {
		                txtarea.focus();
		                range = document.selection.createRange();
		                range.moveStart ('character', -txtarea.value.length);
		                range.moveStart ('character', strPos);
		                range.moveEnd ('character', 0);
		                range.select();
		            }
		            else if (br == "ff") {
		                txtarea.selectionStart = strPos;
		                txtarea.selectionEnd = strPos;
		                txtarea.focus();
		            }
		            txtarea.scrollTop = scrollPos;
		        };
		        
		        var link=function(scope,elem,attrs){
		          
		          var recognition,
		              parentElement=elem[0].parentElement,
		              interimResult='';
		          
		          try {
		              recognition = new webkitSpeechRecognition();
		              scope.isSupported=true;
		          } catch(e) {
		        	  scope.isSupported=false;
		              recognition = Object;
		          }
		          
		          scope.$watch("isListening",function(v){
		            if(v==true){
		               elem[0].focus();
		               recognition.start();
		            }
		            else{
		              recognition.stop();
		            }
		            
		          });

		          recognition.continuous = true;
		          recognition.interimResults = true;

		          recognition.onresult = function(event){
		            var pos = getCursorPosition(elem[0]) - interimResult.length;
		            elem[0].value=elem[0].value.replace(interimResult, '');
		            interimResult = '';
		            setCursorPosition(elem[0],pos);
		            for (var i = event.resultIndex; i < event.results.length; ++i) {
		                if (event.results[i].isFinal) {
		                    insertAtCaret(elem[0], event.results[i][0].transcript);
		                    //elem[0].value = elem[0].value + event.results[i][0].transcript;
		                } 
		                else {
		                    insertAtCaret(elem[0], event.results[i][0].transcript + '\u200B');
		                    interimResult += event.results[i][0].transcript + '\u200B';
		                }
		            }
		          };
		        };
		        
		        return {
		            "link" : link,
		            "scope" :{
		              "isListening" : "=",
		              "isSupported" : "="
		          }
		        };
		        
		      },
			
			"setFocus" : function(){
		          
		          var link = function(scope,elem,attrs){
		            
		        	  /*Bind on blur for our element so we can keep the
		        	   *proper state of our 2-way bound data-attribute*/
			            angular.element(elem[0]).on('blur',function(){
			              scope.$apply(function(){
			                scope.doFocus=false;
			              });
			            });
			            
			            
			            /*Watch our doFocus scoped value for changes, this value
			             * is two bound to parentScope. On a value of true we set focus
			             * on our element.*/
			            scope.$watch("doFocus",function(v){
			              if(v==true){
			                elem[0].focus();
			              }
			            });
			            
			          };
			          
			          /*Attribute only*/
			          return {
			        	"restrict" : "A",
			            "link" : link,
			            "scope" : {doFocus : "=setFocus"}
			          };
		          
		        }
	
	};
	
	return directives;
	
});