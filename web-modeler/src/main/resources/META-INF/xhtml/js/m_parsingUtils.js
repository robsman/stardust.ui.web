/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/
/*Parsing functions that may well need a better home but for now will live here.*/
define(["bpm-modeler/js/m_model"],function(m_model){
	
	/*Parse a javascript object to an array of its dot-delimited elements
	 *where each string is a unique path in the object hierarchy, and all 
	 *possible paths are accounted for.*/
	var parseJSObjToStringFrags=function(obj,name){
	  
	  var results=[], /*accumulate our strings*/
	      key,        /*key of object we are testing*/
	      temp;       /*temp var for entries we will push to our results, and pass along in our recursion*/
	      
	  for(key in obj){
	    if(obj.hasOwnProperty(key)){
	      temp=name + "." + key;
	  	  results.push(temp);
	  	  if (typeof(obj[key])==="object"){
	  	    results=results.concat(parseJSObjToStringFrags(obj[key],temp));
	  	  }
	    }
	  }
	  return results;
	};
	  
	/*Given a typeDeclaration,parse it to an array of its dot-delimited elements
	 *where each string is a unique path in the object hierarchy, and all 
	 *possible paths are accounted for.*/
	  var parseTypeToStringFrags=function(typeDecl,name){
		  var elements=typeDecl.getElements();
		  var elementCount=elements.length;
		  var results=[];
		  while(elementCount--){
				temp=elements[elementCount];
				results.push(name + "." + temp.name);
				if (typeof typeDecl.asSchemaType === "function") {
	        		var childSchemaType = typeDecl.asSchemaType().resolveElementType(temp.name);	
	        	} else if (typeof typeDecl.resolveElementType === "function") {
	        		var childSchemaType = typeDecl.resolveElementType(temp.name);	
	        	}
	            
	            if (childSchemaType && childSchemaType.type) {
	            	results=results.concat(parseTypeToStringFrags(childSchemaType, name + "." + temp.name));
	            }
		  }
		  return results;
	  };
	  
	  return {
		  "parseTypeToStringFrags" : parseTypeToStringFrags,
		  "parseJSObjToStringFrags" :parseJSObjToStringFrags,
		  "parseParamDefToStringFrags":function(paramDef){
				var typeDecl;
				var data;
				if(paramDef.dataType==="primitive"){
					data= [paramDef.name];
				}else{
					typeDecl=m_model.findTypeDeclaration(paramDef.structuredDataTypeFullId || paramDef.dataFullId);
					if(typeDecl){
						data=parseTypeToStringFrags(typeDecl,paramDef.name);
					}
				}
				return data;
			}
	  };
});