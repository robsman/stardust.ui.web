define(["bpm-modeler/js/m_model","./m_drlAttributes","bpm-modeler/js/m_urlUtils"],
		function(m_model,m_drlAttributes,m_urlUtils){
  
  /*************************************************
   * Parse the result of a typeDeclarations.getBody() 
   * method into a jsTree JSON object.
   * @param: body = typeDeclarations.getBody() 
   * @param: paramDef = {name: "foo"}. Only passed in
   * 		 on the root invocation. Signifies that the
   * 		 sequence/metadata node for that call should 
   * 		 be titled with the parameter definitions name.
   *************************************************/
  var contextName=m_urlUtils.getContextName();
  var attributeImage="plugins/rules-manager/images/icons/tag_blue.png",
  	  attributeRootImage="plugins/rules-manager/images/icons/table-insert-column-gray.png",
	  seqImage="plugins/rules-manager/images/icons/bricks.png",
	  elementImage="plugins/rules-manager/images/icons/data-primitive.png",
	  conditionImage="plugins/rules-manager/images/icons/table-insert-column-blue.png",
	  actionImage="plugins/rules-manager/images/icons/table-insert-column-green.png";
  
  /*@Param:obj=typeDecl object
   * Converts a typeDecl for an enumeration into a typeBody object which can be parsed
   * by our recursive function->fx. 
   * */
  var enumerationParser=function(obj){
		var typeBody=[{}],     /*object we will return to be parsed by fx*/
			facetsObj={}, /*flat object built from our facets[]*/
			facets,       /*result of the typeDecl.getFacets call*/
			facetsCount,  /*number of facets, loop counter*/
			facetsEnum=[],   /*our collection of enumerations in our facet obj*/
			facetTemp;    /*temp var for our loop*/
		
		facets=obj.getFacets();
		
		console.log("-----Facets-----");
		console.log(facets);
		facetsObj=facetBuilder(facets);
		
		//facetsCount=facets.length;
		//facetsObj.enumeration=[];
		/*convert facets to flattened object*/
		/*
		while(facetsCount--){
			facetTemp=facets[facetsCount];
			if(facetTemp.classifier==="enumeration"){
				facetsObj.enumeration.push(facetTemp.name);
			}
			else{
				facetsObj[facetTemp.classifier]=facetTemp.name;
			}
		}*/
		//facetsObj=facetBuilder(facets);
		/*create our typeBody object which fx can process*/
		typeBody[0]=facetsObj;
		typeBody[0].type="enumeration";
		typeBody[0].name=obj.name;
		typeBody.typeDecl=obj;	  
		return typeBody;
  };
  
  /*convert facets to flattened object*/
  var facetBuilder=function(facets){
	  var facetsCount=facets.length,
	  	  facetTemp,
	      facetsObj={};
	  
	  facetsObj.enumeration=[];
	  /*convert facets to flattened object*/
	  while(facetsCount--){
		  facetTemp=facets[facetsCount];
		  if(facetTemp.classifier==="enumeration"){
		    facetsObj.enumeration.push(facetTemp.name);
		  }
		  else{
			facetsObj[facetTemp.classifier]=facetTemp.name;
		  }
	}
	return facetsObj;
  };
  
  var fx=function(body,paramDef){
    var data=[], /*hold our result and our recursively returned children*/
	    temp,    /*an item to be pushed onto data[]*/
	    i,       /*loop counter*/	    
	    img,     /*url of the image to apply to our icon attribute*/
	    bodyRef, /*snapshot of our original body for constructing facet based objects*/
	    obj;     /*item in our body array (see parameters)*/
    	
    /* Skip nodes that only define metadata. Pass children to their grandparent*/
    if(body[0].name==="<sequence>" && !paramDef){
        body=body[0].body; //skip and pass children onwards
    }
    /*test for nodes which are enumerations*/
    
    for(i=0;i<body.length;i++){
        obj=body[i];
        if(obj.facets){
        	bodyRef=obj;
        	obj=facetBuilder(obj.facets);
        	obj.name=bodyRef.name;
        	obj.type="enumeration";
        }
        img=(obj.body)?seqImage:elementImage;
        if (paramDef){
        	obj.name=paramDef.name;
        }
        if(paramDef && obj.enumeration){
        	paramDef.enumeration=obj.enumeration;
        }
        temp={data: {title:obj.name, icon:img}, 
        	  attr: {title: obj.type},
        	  metadata: {
        		  ref: paramDef || obj,
        		  type: obj.type || 'na',
        		  isParamDef:!!paramDef}
        	  };
        /*an obj with a body prop means it has children we need to add*/
        if(obj.body){
          temp.children=fx(obj.body); //...and recurse
        }
        data.push(temp); 
      }
    return data;
  };
  
  /*util function for returning a java type for a xsd type*/
  var mapXSDTypesToJava=function(xsdType){
	  var result;
	  switch(xsdType){
	  case "xsd:decimal":
		  result="java.math.BigDecimal";
		  break;
	  case "xsd:byte":
		  result="byte";
		  break;
	  case "xsd:short":
		  result="short";
		  break;
	  case "xsd:int":
		  result="integer";
		  break;
	  case "xsd:long":
		  result="Long";
		  break;
	  case "xsd:float":
		  result="float";
		  break;
	  case "xsd:double":
		  result="double";
		  break;	
	  case "xsd:date":
	  case "xsd:time":
	  case "xsd:dateTime":
		  result="java.util.Date";
		  break;
	  case "xsd:boolean":
		  result="boolean";
		  break;	
	  default:
		  result="String";
	  }
	  return result;
  };
  
  /*Recursive function to flatten Type Declarations and put them in a format primed for conversion into a DRL type...
   DESCRIPTION: in order to represent the typeDeclaration in DRL we must have each nested type in the declaration extracted
   and flattened our to the same level as our parent object. These nested types must be declared as their own types
   and then referenced by the parent type DRL declaration. This function will collect all the type names and properties
   into a one dimensional array for ease of parsing. In order to support types which serve as the root type for a
   parameter definition, the caller may add a paramDefOptions object to the typeDecl. When doing this be sure to
   extend the typeDecl to a new object as the XPDL based functions that return typeDecls do so as references.
   */
  var fxTypeDecl=function(typeDecl){
	  	var typeArray=[], /*our 'name : type' properties of our type*/
	  		elements = typeDecl.getElements(),
	  		paramDefKey={},
	  		paramDefName, /*name of our param def, when present*/
	  		results=[],   /*array we concat(for recursive calls) and push results into*/
	  		childTypeDecl,/*temp object for nested types we need to pass recursively*/
		    elementCount=elements.length,
		    temp,        /*temp obj for our element loop*/
		    tempType,    /*type of our nested typeDecl to search for*/
		    directionKey,/*@key to indicate direction of our paramDef*/
		    formattedID, /*FQID standard notation for IDs*/
		    fIDpieces,    /*array for holding our formattedID pieces*/
		    paramDirection,/* In,Out,InOut - Parameter Definition Direction*/
		    metaKeys;    /*array of name value pairs representing metakeys*/
	  	
	  	/*builidng up our properties for our type*/
		while(elementCount--){
			temp=elements[elementCount];
			tempType=temp.type;
			if(temp.type.indexOf("xsd:")> -1){
				tempType=mapXSDTypesToJava(temp.type);
			}
			else{
				tempType=temp.type.substring(temp.type.indexOf(":")+1);
			}
			typeArray.push({attr_type: tempType,name:temp.name});
			if(temp.body){;
				childTypeDecl=typeDecl.model.findTypeDeclarationBySchemaName(tempType);
				results=results.concat(fxTypeDecl(childTypeDecl,elements));
			}
		}
		
		/*build metakeys for our type*/
		if(typeDecl.type==="typeDeclaration"){
			metaKeys=[];
			if(typeDecl.paramDefOptions){
				paramDirection=typeDecl.paramDefOptions.direction.toUpperCase();
				if(paramDirection==="IN"){
					directionKey="@input";
				}
				else if(paramDirection==="OUT"){
					directionKey="@output";
				}
				else{
					directionKey="@inout";
				}
					
				formattedID=typeDecl.paramDefOptions.dataTypeID;
				fIDpieces=formattedID.split(":");
				formattedID="{" + fIDpieces[0] + "}" + fIDpieces[1];
				paramDefName=typeDecl.paramDefOptions.name
				paramDefKey={name: directionKey,
							 value: "dataId=\"" + formattedID +"\"," +
							        "ParamDefName=\"" + paramDefName + "\"," +
							        "ParamDefID=\"" + typeDecl.paramDefOptions.id + "\""};
				metaKeys.push(paramDefKey);
			}
		}
		results.push({
			name: paramDefName || typeDecl.name,
			metaKeys:metaKeys,
			objType:typeDecl.type.substring(typeDecl.type.indexOf(":")+1),
			properties:typeArray});
		return results;
  };
  
  /*Utility function for parsing the results of fxTypeDecl to proper DRL formatted strings.
   * The input should only be the returned data from a call to fxTypeDecl.
   * */
  var parseToDRLStrings=function(val){
	  var valCounter=val.length,
	      drlStrings=[],
	      temp="",
	      tempObj,
	      valDict={},
	      dictEntry,
	      tempProp,
	      propCounter,
	      keyCounter,
	      tempDict={},
	      mkeyTemp="";
	  
	  /*task: remove duplicate type declarations but merge their metakeys*/
	  while(valCounter--){
		  tempObj=val[valCounter];
		  dictEntry=valDict[tempObj.name];
		  if(dictEntry && dictEntry.metaKeys.length>0){
			  tempObj.metaKeys=tempObj.metaKeys.concat(dictEntry.metaKeys);
		  }
		  valDict[tempObj.name]=tempObj;
	  }
	  
	  /*now convert back to array*/
	  val=[];
	  for(var k in valDict){
		  if(valDict.hasOwnProperty(k)){
			  val.push(valDict[k]);
		  }
	  }
	  valCounter=val.length;
	  
	  while(valCounter--){
	    tempObj=val[valCounter];
	    temp="declare " +  tempObj.name + "\n";
	    keyCounter=tempObj.metaKeys.length;
	    while(keyCounter--){
	      mkeyTemp=tempObj.metaKeys[keyCounter];
    	  temp+="\t"+ mkeyTemp.name + "(" + mkeyTemp.value + ")" + "\n";
	    }
	    propCounter=tempObj.properties.length;
	    while(propCounter--){
	      tempProp=tempObj.properties[propCounter];
	      temp+="\t" +tempProp.name + " : " + (tempProp.objType || tempProp.attr_type) + "\n";
	    }
	    temp+="end\n";
	    drlStrings.push(temp);
	  }
	  return drlStrings;
	};
  
  return {
	parse:function(typeDecl){
		var parsedTypes;
		parsedTypes=fxTypeDecl(typeDecl,typeDecl.getElements());
		return parsedTypes;
	},
	parseTypeDeclToDRL: function(typeDecls){
		var parsedTypes=[],   /*stage 1 of our parsing*/
			typeCounter,
			temp,
		    drlStrings=[]; /*string array of DRL types*/
		
		if($.isArray(typeDecls)===false){typeDecls=[typeDecls];}
		typeCounter=typeDecls.length;
		while(typeCounter--){
			temp=typeDecls[typeCounter];
			parsedTypes=parsedTypes.concat(fxTypeDecl(temp));
		}
		drlStrings=parseToDRLStrings(parsedTypes);
		return drlStrings;
	},
    parsetoJsTree: function(typeBody,paramDef){
      return fx(typeBody,paramDef);
    },
    parseParamDefinitonsToJsTree: function(paramDefs){
    	var paramDefCount=paramDefs.length,
	        jsonTreeData=[],
	        paramDef,
	        typeDecl,
	        typeBody,
	        jstreeDataNode,
	        jsConditionNodes=[],
	        jsActionNodes=[],
	        facets,
	        facetsObj={},
	        facetTemp,
	        facetsCount,
	        temp;
    	
	    while(paramDefCount--){
	    	paramDef=paramDefs[paramDefCount];
	    	//structs can be either objects or enumerated types
	    	if(paramDef.dataType==="struct"){
	    		try{
	    			/*extract type declaration from parameter definition and fish for a typeBody*/
		    		typeDecl=m_model.findTypeDeclaration(paramDef.structuredDataTypeFullId);
		    		typeBody=typeDecl.getBody();
		    		/*enumerated types do not have a typeBody*/
		    		if(!typeBody){
		    			/*parse typeDecl into a typeBody we can process*/
		    			typeBody=enumerationParser(typeDecl);
		    		}
		    		else{
		    			typeBody[0].name=paramDef.name; /*Give the agnostic typeBody context from our paramDef*/
		    		}
		    		jstreeDataNode=fx(typeBody,paramDef); /*walk the JSON and build our tree*/	
		    		
		    		if(paramDef.direction==="IN" || paramDef.direction==="INOUT"){
		    			jsConditionNodes.push(jstreeDataNode);
		    		}
		    		
		    		if(paramDef.direction==="OUT" || paramDef.direction==="INOUT"){
		    			jsActionNodes.push(jstreeDataNode);
		    		}
	    		}
	    		catch(err){
	    			console.log("Error on m_model.findTypeDeclaration" + err);
	    			console.log("structuredDataTypeFullId: " + paramDef.structuredDataTypeFullId);
	    		}
	    	}
	    	else{
	    		temp={data: {title:paramDef.name,icon:elementImage}, 
	    			  metadata: {ref: paramDef,
	    					     type: paramDef.primitiveDataType || 'na'}
	    		};
	    		if(paramDef.direction==="IN"){
	    			jsConditionNodes.push(temp);
	    		}
	    		else{
	    			jsActionNodes.push(temp);
	    		}
	    	}
	    }
	    var treeJSON=[ {data: {title:"Attributes",icon: attributeRootImage}, 
	    		    children:[m_drlAttributes.getAttributeAsJSTreeData("salience",attributeImage),
	    		              m_drlAttributes.getAttributeAsJSTreeData("enabled",attributeImage),
	    		              m_drlAttributes.getAttributeAsJSTreeData("date-effective",attributeImage),
	    		              m_drlAttributes.getAttributeAsJSTreeData("date-expires",attributeImage),
	    		              m_drlAttributes.getAttributeAsJSTreeData("no-loop",attributeImage),
	    		              m_drlAttributes.getAttributeAsJSTreeData("agenda-group",attributeImage),
	    		              m_drlAttributes.getAttributeAsJSTreeData("activation-group",attributeImage),
	    		              m_drlAttributes.getAttributeAsJSTreeData("duration",attributeImage),
	    		              m_drlAttributes.getAttributeAsJSTreeData("auto-focus",attributeImage),
	    		              m_drlAttributes.getAttributeAsJSTreeData("lock-on-active",attributeImage),
	    		              m_drlAttributes.getAttributeAsJSTreeData("rule-flow-group",attributeImage),
	    		              m_drlAttributes.getAttributeAsJSTreeData("dialect",attributeImage)
                            ]},
                    {data: {title:"Conditions",icon:conditionImage},children: jsConditionNodes},
          	        {data: {title:"Actions", icon:actionImage},children: jsActionNodes}];
	    return treeJSON;//jsonTreeData;
    }
  };
});