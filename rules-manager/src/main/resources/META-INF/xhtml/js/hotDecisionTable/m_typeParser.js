/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
define(["require",
        "bpm-modeler/js/m_model",
        "rules-manager/js/hotDecisionTable/m_drlAttributes",
        "bpm-modeler/js/m_urlUtils",
        "bpm-modeler/js/m_typeDeclaration",
        "rules-manager/js/m_i18nUtils",
        "rules-manager/js/hotDecisionTable/m_typeMapper"],
		function(
				require,
				m_model,
				m_drlAttributes,
				m_urlUtils,
				m_typeDeclaration,
				m_i18nUtils,
				m_typeMapper){

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
  var attributeImage="pi pi-column-attribute",
  	  attributeRootImage="pi pi-column-attributes pi-lg icon-image",
	  seqImage="pi pi-column-attributes pi-lg icon-image",
	  elementImage="pi pi-primitiv pi-lg icon-image",
	  conditionImage="pi pi-column-conditions pi-lg icon-image",
	  actionImage="pi pi-column-actions pi-lg icon-image",
  	  actionTitle=m_i18nUtils.getProperty("rules.propertyView.decisiontableview.dialog.addcolumn.tree.root.actions","Actions"),
  	  conditionTitle=m_i18nUtils.getProperty("rules.propertyView.decisiontableview.dialog.addcolumn.tree.root.conditions","Conditions"),
  	  attributeTitle=m_i18nUtils.getProperty("rules.propertyView.decisiontableview.dialog.addcolumn.tree.root.attributes","Attributes");
 
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
		
		/*retrieve and flatten facets*/
		facets=obj.getFacets();
		facetsObj=facetBuilder(facets);

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
	  facetsObj.enumeration.reverse();
	  return facetsObj;
  };
  
  /*Parses a typeDecl recursively to build a JSTREE JSON structure. Only for the initial call should
   * the paramDef parameter be present. The paramDef parameter is used to construct the root node 
   * which we will attach all our iterative and recursive resutls to (ultimately).*/
  var fx2=function(typeDecl,paramDef){
	  var elements, 		/*children of our typeDecl*/
		  elementCount, 	/*number of elements*/
		  temp,				/*temp JSON object represetning a node in our JSTREE*/
		  childSchemaType,	/*child type-decl we will pass to our recursion*/
		  results=[],		/*collected tree nodes we will attach to our root*/
		  tempEnum,			/*temp object when parsing enums*/
		  obj,				/*temp element in our while loop*/
		  root;				/*root JSTREE node parsed from our paramDef, attach results to this*/
	  
	  /*Presence of truthy paramDef parameter tells us we our processing the root node in the tree.
	   *So, build our root node so that we have something to attach our results to when all is fini.*/
	  if(paramDef){
		  root={
			  data: {title:paramDef.name, icon:seqImage}, 
          	  attr: {title: ''},
          	  children:[],
          	  metadata: {
          		  ref: paramDef ,
          		  type:  'na',
          		  isParamDef:true}  
		  };
	  }
	  
	  /*Extract what will become our child nodes*/
	  elements=typeDecl.getElements();
	  elementCount=elements.length;
	  
	  /*loop through elements building nodes and recursively building child nodes where appropriate.*/
	  while(elementCount--){
		  obj=elements[elementCount];
		  
		  
		  /*Testing to see if we can resolve our temp object as a typeDeclaration itself. */
		  var childSchemaType = undefined;
		  if (typeof typeDecl.asSchemaType === "function") {
      		childSchemaType = typeDecl.asSchemaType().resolveElementType(obj.name);	
  		  } 
		  if (!childSchemaType && typeof typeDecl.resolveElementType === "function") {
  			childSchemaType = typeDecl.resolveElementType(obj.name);	
      	  }
		  
		  /*Handle enumerations-Enumerations have children but we need to flatten them for our purposes as 
		   *our tree represents enumerations as a single node with no children */
		  if(childSchemaType && obj.facets && childSchemaType.isEnumeration()){
			  tempEnum=facetBuilder(obj.facets);
			  tempEnum.name=obj.name;
			  tempEnum.type="enumeration";
			  obj=tempEnum;
		  }
		  
		  /*Build jstree node, default to elementImage for the icon*/
		  temp={
				  data: {title:obj.name, icon: elementImage}, 
	          	  attr: {title: typeDecl.description || m_typeMapper.ippToFriendlyText( obj.type)},
	          	  metadata: {
	          		  ref: obj,
	          		  type: getUnQualifiedNameIfBuiltInType(obj.type, typeDecl) || 'na',
	          		  isParamDef:false}
          };
		  
		  /*Our temp object is actually a typeDecl (and not an enumeration) that we need to 
		   * parse further.*/
		  if (childSchemaType && childSchemaType.type && temp.metadata.type !=="enumeration") {
			temp.data.icon=seqImage;
			temp.children=fx2(childSchemaType);
          } 
		  /* TODO For elements with inline types the low level api needs to be used as they don't have explicit types
		   * and no high level api exist to traverse such elements */
		  else if (obj && obj.body) {
  			temp.data.icon=seqImage;
			temp.children=fx(obj.body, paramDef, typeDecl);
          }

		  results.push(temp);
	  }
	  
	  /*if root is truthy then we are done and need to append our collected results and return
	   *else we are in a recursive call and we will just return our results so they can be used
	   *as children to treenode.*/
	  if(root){
		  root.children=results;
		  return [root];
	  }else{
		  return results;
	  }
  };
  
  var fx=function(body,paramDef,typeDecl){

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
        	  attr: {title: typeDecl.description || m_typeMapper.ippToFriendlyText( obj.type)},
        	  metadata: {
        		  ref: paramDef || obj,
        		  type: getUnQualifiedNameIfBuiltInType(obj.type, typeDecl) || 'na',
        		  isParamDef:!!paramDef}
        	  };
        /*an obj with a body prop means it has children we need to add*/
        if(obj.body){
        	console.log("Recursing");
        	console.log(obj);
          temp.children=fx(obj.body, undefined, typeDecl); //...and recurse
        } else {
        	if (typeof typeDecl.asSchemaType === "function") {
        		var childSchemaType = typeDecl.asSchemaType().resolveElementType(obj.name);	
        	} else if (typeof typeDecl.resolveElementType === "function") {
        		var childSchemaType = typeDecl.resolveElementType(obj.name);	
        	}
            
            if (childSchemaType && childSchemaType.type && childSchemaType.type.body) {
            	console.log("Recursing");
            	console.log(childSchemaType.type);
            	temp.data.icon=seqImage;
            	temp.children=fx(childSchemaType.type.body, undefined, childSchemaType);
            }
        }
        data.push(temp); 
      }
    return data;
  };
  
  /**
   * 
   */
  var getUnQualifiedNameIfBuiltInType = function(type, typeDecl) {
	  if (type && typeDecl && (typeDecl.typeDeclaration && typeDecl.typeDeclaration.schema) || typeDecl.schema) {
		  var schema = (typeDecl.typeDeclaration && typeDecl.typeDeclaration.schema) || typeDecl.schema;
		  var qName = m_typeDeclaration.parseQName(type, schema);
		  if (qName && qName.name && qName.namespace
				  && "http://www.w3.org/2001/XMLSchema" == qName.namespace) {
			  return qName.name;
		  }
	  }
	  
	  return type;
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
  
  /* Given a type declaration and a name root, parse the type declaration into an
   * array of strings representing the distinct dot-notation path to each element.
   * For Example: Person.Name,Person.Name.First,Person.Name.Last etc...*/
  var parseTypeToStringFrags=function(typeDecl,name){
	  var elements;
	  var elementCount;
	  var results=[];
	  
	  if(!typeDecl || !typeDecl.getElements ){
		  return name || "";
	  }
	  elements=typeDecl.getElements();
	  elementCount=elements.length;
	  
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
  
  /* Utility function for parsing the results of fxTypeDecl to proper DRL formatted strings.
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
	"parseTypeToStringFrags": parseTypeToStringFrags,
	"parseParamDefToStringFrags":function(paramDef){
		var typeDecl;
		var data;
		if(paramDef.dataType==="primitive"){
			data= [paramDef.name];
		}else{
			typeDecl=m_model.findTypeDeclaration(paramDef.structuredDataTypeFullId);
			data=parseTypeToStringFrags(typeDecl,paramDef.id);
		}
		return data;
	},
	"parseTypeDeclToDRL": function(typeDecls){
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
    "parsetoJsTree": function(typeBody,paramDef){
      return fx(typeBody,paramDef);
    },
    "parseParamDefinitonsToJsTree": function(paramDefs){
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
		    		//jstreeDataNode=fx(typeBody,paramDef,typeDecl); /*walk the JSON and build our tree*/	
		    		jstreeDataNode=fx2(typeDecl,paramDef);
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
	    		if(paramDef.structuredDataTypeFullId){
	    			typeDecl=m_model.findTypeDeclaration(paramDef.structuredDataTypeFullId);
				}
	    		if(typeDecl && typeDecl.getType()==="enumStructuredDataType"){
	    			typeBody=enumerationParser(typeDecl);
	    			temp=fx(typeBody,paramDef,typeDecl);
	    		}
	    		else{
		    		temp={data: {title:paramDef.name,icon:elementImage}, 
		    			  metadata: {ref: paramDef,
		    					     type: paramDef.primitiveDataType || 'na'}
		    		};
	    		}
	    		if(paramDef.direction==="IN" || paramDef.direction==="INOUT"){
	    			jsConditionNodes.push(temp);
	    		}
	    		if(paramDef.direction==="OUT" || paramDef.direction==="INOUT"){
	    			jsActionNodes.push(temp);
	    		}
	    	}
	    }
		
	    /*REF CRNT-33005: Added as only in FireFox we were somehow arriving inside our define callback function with
	     * the m_drlAttributes module not loaded*/
		if(!m_drlAttributes){
			m_drlAttributes=require("rules-manager/js/hotDecisionTable/m_drlAttributes");
		}
		
	    var treeJSON=[ {data: {title: attributeTitle,icon: attributeRootImage},attr: {category: "Attribute"}, 
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
                    {data: {title: conditionTitle,icon:conditionImage},attr: {category: "Condition"},children: jsConditionNodes},
          	        {data: {title: actionTitle, icon:actionImage},attr: {category: "Action"},children: jsActionNodes}];
	    return treeJSON;//jsonTreeData;
    }
  };
});