define(["./m_operators","./m_utilities"],function(operators,utils){
	var factory={
			getMenu: function(options){
				var category=options.columnCategory,
					colType=options.columnType,
					$opBadge=options.ref,
					instance=options.instance,
					col=options.columnIndex,
					$dialog;
				 /*attribute columns are only involved in assignment operations,not comparison operations.*/
	              if(category==="Attribute"){
	            	  colType=operators.MenuCategory.CAT_BOOLEAN;
	              }

				  validOps=operators.getOperators(colType);
				  validOpCount=validOps.length;
				  menuRows=[];
				  
				  /*build our html string representing our menu items*/
				  tempRowString="<div style='box-shadow:5px 5px 5px #CCC;'>";			  
				  while(validOpCount--){
					  tempOp=validOps[validOpCount];
					  tempRowString +="<div style='display:block;' value='" + tempOp.symbol + 
					                  "' class='ipp-menuitem' data-text='"  +  tempOp.text + 
					                  "' data-operator='" + tempOp.operator + 
					                  "'>" +
					                  "<span class='ipp-badge black operator pull-left'>" + 
					                  	tempOp.symbol + 
					                  "</span>" + 
					                  tempOp.text + 
					                  "</div>";
				  }
				  tempRowString +="</div>";
				  
				  /*Build a dialog based on our temprowstring. This is the menu oeprator dialog
				   * the users will select from when they click on the $opBadge
				   * */
	              $dialog=$(tempRowString).dialog({
	            	  autoOpen:false,
	            	  width:220,
	            	  dialogClass: "camino-list-hdr __opMenu"});
	              
	              /*As we want this to behave as a menu, remove the default titlebar.*/
	              $dialog.siblings('div.ui-dialog-titlebar').remove();

	              /*Attach the click handler for the menu-dialog. We must update the 
	               * meta information for the column header, with the operator they selected.
	               **/
	              $("div",$dialog).on("click",function(event){
	            	    var mySymbol=$(this).attr("value"),
		            	    myOperator=$(this).attr("data-operator"),
		            	    meta=$opBadge.attr("data-meta-head"),
		            	    opText=$(this).attr("data-text"),
		            	    colHeaders=instance.getSettings().colHeaders,      	    
		            	    metaArray=meta.split("|");
	            	    
	            	    if(metaArray.length > 2){
	            	    	metaArray.splice(1,1,myOperator);
	            	    	meta=metaArray.join("|");
	            	    }
	            	    console.log("Hash for: " + mySymbol + " " + utils.hashString(mySymbol));
	                    colHeaders.splice(col,1,meta);
	                    instance.render();
	                    $opBadge.attr("data-meta-head",meta); 
	                    $opBadge.attr("data-operator", myOperator);
	                    $opBadge.attr("title",opText);
	                    $opBadge.text(mySymbol);
	                    event.stopPropagation();
	            	    $dialog.dialog("close"); 
	            	    instance.runHooks('afterChange');
	            	    console.log(myOperator);
	            	    //$dialog.dialog("destroy");
	            	  });
	              return $dialog;
			}
	}
	return factory;
});