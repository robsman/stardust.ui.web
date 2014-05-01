define([],function(){

	var directives={
		
		 "fileUpload" : function(){

		    return{
		    	"restrict" : 'EA',
		        "templateUrl" : function(element,attr){
		        	return attr.templateUrl || "templates/fileUpload.html";
		        },
		        "link" : function(scope,element,attr){
		        	var fd,		   /*Form Data we will upload*/
		            	files=[],  /*File(s) to upload, pushed onChange of our input.file*/
		            	url="",	   /*target URL to upload to*/
	            		inpt=element.find("input[type='file']"),   /*Must be singularly present in template*/
		                btn = element.find("a[name='upload']"), /*Must be singularly present in template*/
		                btnMock=element.find("a[name='mockFile']"),
		                errorHandler,    /*event handler evaluated from our attributes*/
		                loadHandler,	 /*event handler evaluated from our attributes*/
		                progressHandler; /*event handler evaluated from our attributes*/
		            
		        	/*Eval our data attributes to determine if the user has given
		             *us functions to plug into our event interface.*/
		            errorHandler = scope.$eval(attr.onError);
		            loadHandler = scope.$eval(attr.onLoad);
		            progressHandler = scope.$eval(attr.onProgress);
		        	
		            btnMock.on("click",function(){
		            	inpt.click();
		            });
		            
			        /*on change events of our input.file element we need to push into our file collection*/
		            inpt.on("change",function(){
		              for (var i = 0; i < inpt[0].files.length; i++) {
				          files.push(inpt[0].files[i]);
				        }		            
		            });
		            
		            /*On click events upload our files*/
		            btn.on("click",function(){
		              var xhr = new XMLHttpRequest();
		              
		              fd = new FormData();
		              
		              for (var i in files) {
		                  fd.append("uploadedFile", files[i]);
		              }
		              
		              xhr.upload.addEventListener("progress", function(e){
		                if(angular.isFunction(progressHandler)){
		                  progressHandler(e);
		                };        	 
		              }, false);
		              xhr.addEventListener("load", function(e){
		                if(angular.isFunction(loadHandler)){
		                  loadHandler(e);
		                };  
		              }, false);
		              xhr.addEventListener("error", function(e){
		                if(angular.isFunction(errorHandler)){
		                  progressHandler(e);
		                };  
		              }, false);
		      
		              xhr.open("POST", attr.urlTarget, true);
		              xhr.send(fd);
		              
		            });/*Click Handler Ends*/
		            
		          }/*Link function ends*/
		    
	    		};/*Return object ends*/
	    		
		    }/*fileUpload function ends*/
	};
	
	return directives;

});