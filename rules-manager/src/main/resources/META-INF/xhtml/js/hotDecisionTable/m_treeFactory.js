define(["jquery","Handsontable","jstree","./m_typeMapper","bpm-modeler/js/m_utils"],
		function($,Handsontable,jstree,typeMapper,m_utils){
  
  var modelTreeFactory={
    getTree : function(category,hotInstanceSelector,treeData){
        var $root=$("<div></div>"),  			  /*root element all our frags will be appended to*/
            $well=$("<div class='well'></div>"),  /*container for the jstree*/
            $inptSearch,  	 /*input[text] for searching our tree*/
            domString,  	 /*raw html string for our input field and search label*/
            jsTreeInstance,  /*Instance of the jstree*/
            instance,        /*HandsOnTable Instance*/
            $domFrag; 		 /*domString after $()*/
        
        instance=m_utils.jQuerySelect(hotInstanceSelector).handsontable('getInstance')
        domString='<div style="margin-bottom:4px;">' +
			          '<span class="ipp-menuitem">Search</span>' +
			          '<input type="text" class="form-control inpt-skinny">' +
			      '</div>';
        
        $domFrag=$(domString);
        $root.append($domFrag);
        $root.append($well);
        $well.css("background-color","#FFF");/*rounded border, no color*/
        jsTreeInstance=$well.jstree({ 
    		  "json_data" : {data: treeData},
    		  "plugins" : [ "themes", "json_data", "ui", "search" ],
    		  "themes" :{"theme" : "default",icons:true}
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
             colType;
    	   
	      colType=data.rslt.obj.data("type") || data.rslt.obj.data("jstree").type;
          path=$(this).jstree("get_path");
          category=path[0].replace("s","");
          
          /*TODO:compute path from object hierarchy rather than node names*/
          model=path.slice(1).join(".");
          colType=typeMapper.ippToHoTTable(colType);
          refObj=data.rslt.obj.data("ref") || data.rslt.obj.data("jstree").ref;
          console.log(refObj);
          var parent=$.jstree._reference(this)._get_parent(data.rslt.obj);
          
          /*Autocomplete types have a source list of values we must pass along*/
          if(colType.type==="autocomplete"){
	    	  colType.source=refObj.enumeration.split(",");
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
           if(category==="Attribute"){
        	   $treeNode=$("a:contains('" + model +"')",jsTreeInstance);
        	   if($treeNode.hasClass("ipp-disabled-text")){
        		   return;
        	   }
        	   else{
        		   $treeNode.addClass("ipp-disabled-text");
        	   }
           }
           
           /*otherwise build our col config obj and add to our table*/
           obj.hdr=model + "|=|" + category;
           obj.type=colType;
           obj.type.ref=refObj;
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
	  		  colHdrArr;
	  		  
  		  while(hdrCount--){
  			colHdrArr=columnHeaders[hdrCount].split("|");
  			if(colHdrArr.length >=3){
  				if(colHdrArr[2]==="Attribute"){
  					$treeNode=$("a:contains('" + colHdrArr[0] +"')",jsTreeInstance);
  					$treeNode.addClass("ipp-disabled-text");
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