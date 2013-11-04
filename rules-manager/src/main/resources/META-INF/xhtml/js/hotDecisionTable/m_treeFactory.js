define(["jquery","Handsontable","jstree","./m_typeMapper",
        "bpm-modeler/js/m_utils","rules-manager/js/m_i18nUtils","bpm-modeler/js/m_urlUtils"],
		function($,Handsontable,jstree,typeMapper,m_utils,m_i18nUtils,m_urlUtils){
  
  var modelTreeFactory={
    getTree : function(category,hotInstanceSelector,treeData){
        var $root=$("<div></div>"),  			  /*root element all our frags will be appended to*/
            $well=$("<div class='well'></div>"),  /*container for the jstree*/
            $inptSearch,  	 /*input[text] for searching our tree*/
            domString,  	 /*raw html string for our input field and search label*/
            jsTreeInstance,  /*Instance of the jstree*/
            instance,        /*HandsOnTable Instance*/
            $domFrag; 		 /*domString after $()*/
        
        instance=m_utils.jQuerySelect(hotInstanceSelector).handsontable('getInstance');
        domString='<div style="margin-bottom:4px;">' +
			          '<span class="ipp-menuitem">' + 
			          	m_i18nUtils.getProperty("rules.propertyView.decisiontableview.dialog.label.search","Search") +
			          '</span>' +
			          '<input type="text" class="form-control inpt-skinny">' +
			      '</div>';
        
        $domFrag=$(domString);
        $root.append($domFrag);
        $root.append($well);
        $well.css("background-color","#FFF");/*rounded border, no color*/
        jsTreeInstance=$well.jstree({ 
    		  "json_data" : {data: treeData},
    		  "plugins" : [ "themes", "json_data", "ui", "search" ],
    		  "themes" :{"theme" : "custom","url" : "plugins/rules-manager/css/jsTreeCustom/style.css","icons":true}
  	  	}).bind("select_node.jstree", function (e, data) { 
    	   var settings=instance.getSettings(),
             model,index,
             obj={},
             refObj,
             isParamDef=false,
             model,
             path,
             $treeNode,
             rootModel,
             colType,
             $rootNode,
             $paramDefNode,
             paramDefRef,
             category;
    	   
	      colType=data.rslt.obj.data("type") || data.rslt.obj.data("jstree").type;
          path=$(this).jstree("get_path");
          category=path[0].replace("s","");
          $rootNode=data.rslt.obj.parents("li").filter(":last");
          $paramDefNode=data.rslt.obj.parents("li");
          $paramDefNode=$($paramDefNode[$paramDefNode.length-2]);
          category = $rootNode.attr("category");
        
          /*TODO:compute path from object hierarchy rather than node names*/
          model=path.slice(1).join(".");
          colType=typeMapper.ippToHoTTable(colType);
          refObj=data.rslt.obj.data("ref") || data.rslt.obj.data("jstree").ref;
          paramDefRef=$paramDefNode.data("ref") || $paramDefNode.data("jstree").ref;
          var parent=$.jstree._reference(this)._get_parent(data.rslt.obj);

          /*Autocomplete types have a source list of values we must pass along*/
          if(colType.type==="autocomplete"){
	    	  colType.source=refObj.enumeration;
	      }
          
          /*Never put a column with an unmapped/unknown type in the table.*/
	      if(colType && colType.type==="unknown"){
	    	  console.log("jsTreeNode Selected Event-[treeFactory.js]");
	    	  console.log("Unkown Type: aborting column addition to DecisionTable");
	          console.log(category + ":" + model + ":" + colType);
	          console.log(colType);
	    	  return;
	      }	      
         if(model){
           /*Any node with a class of ipp-disabled-text = exit func*/
           if(category==="Attribute" || category==="Action"){
        	   $treeNode=$("a",data.rslt.obj);
        	   if($treeNode.hasClass("ipp-disabled-text")){
        		   return;
        	   }
        	   else{
        		   $treeNode.addClass("ipp-disabled-text");
        	   }
           }
           
           /*otherwise build our col config obj and add to our table*/
           obj.hdr=model + "|EqualTo|" + category;
           obj.type=colType;
           obj.type.ref=refObj;
           obj.type.parameterDefinition=paramDefRef;
           obj.category=category;
           obj.model=model;
           
           /*Find the location where we can add our column, looking for tail end of matching categories.*/
           index=settings.helperFunctions.headerTypeLocation(instance,obj.category);
           settings.helperFunctions.addColumn(instance,index,obj,120);
         }
  	  }).bind("loaded.jstree",function(){
  		  /*on load examine our decisionTable to determine which attribute nodes to disable*/
  		  var settings=instance.getSettings(),
  		  	  $treeNode,
	  		  columnHeaders=settings.colHeaders,
	  		  hdrCount=columnHeaders.length,
	  		  path,
	  		  pathRoot,
	  		  pathModel,
	  		  category,
	  		  leafVal,
	  		  colHdrArr;
  		  
  		  while(hdrCount--){
  			colHdrArr=columnHeaders[hdrCount].split("|");
  			if(colHdrArr.length >=3){
  				category=colHdrArr[2];
  				leafVal=colHdrArr[0].split(".").pop();
  				if(category==="Attribute" || category==="Action"){
  					$treeNode=$("a:contains('" + leafVal +"')",jsTreeInstance);
  					$treeNode.each(function(){
  						path=jsTreeInstance.jstree("get_path",$(this));
  						pathRoot=path[0];
  						pathModel=path.slice(1).join(".");
  						//console.log("PM:" + pathModel + " : " + colHdrArr[0] );
  						if(pathRoot===category +"s" && (pathModel===colHdrArr[0])){
  							$(this).addClass("ipp-disabled-text");
  						}
  					});
  				}
  			}
  		  }
  	  });
      
      /*Handler for searching the tree on keypress*/
  	  $inputSearch=$("input",$domFrag);
        $inputSearch.on("keypress",function(e){
          var currentChar=String.fromCharCode(e.which);
          jsTreeInstance.jstree('clear_search');
          jsTreeInstance.jstree('close_all');
          jsTreeInstance.jstree('search',$inputSearch.val() + currentChar);
        });
      return $root;
    }
  }
  return modelTreeFactory; 
});