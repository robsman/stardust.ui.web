define(["./m_operators"],function(operators){
    var chFactory={
          AttributeHeader: function(meta,col,instance,th){
              var metaData=meta.split("|"),
                  $span,
                  categoryPrefix,
                  $opBadge=$("<div class='ipp-badge pointy nudgeLeft pop' title=''></div>"),
                  category=metaData[2],
                  currentOp=metaData[1],
                  modelText=metaData[0],
                  leafModel=modelText.split(".").slice(-1)[0],
                  labelColor,
                  needTooltip=false,
                  $prefix="",
                  popover,
                  tempOp,
                  $img,
                  settings,
                  colType,
                  validOps,
                  tempOp,
                  validOpCount,
                  menuRows,
                  tempRowString,
                  $dialog;
              
              /*Populate the correct symbol for our operator name*/
              tempOp=operators.getOperatorByOpName(metaData[1]);
              $opBadge.append(tempOp.symbol || "NOOP");
              
              /*convert our meta text header to a data attribute to attach to the
              DOM conversion of the original text header.*/
              $opBadge.attr("data-meta-head",meta);
              
              /*Header columns need no additions other than a span tag*/
              if(metaData[2]==="Header"){
                $span="<span class=''>" + metaData[0] + "</span>";
                return $span;
              }
              console.log("adding headers......................");
              labelColor="#AAAAAA";
              $opBadge.css({"background-color":labelColor,"height":"10px","border-radius":"4px"});

              
              /*quick test to determine if we have a model with structure*/
              if(leafModel!==modelText){
                needTooltip=true;
              }
              
              /*if model has a hierarchy then add a prefix with a tooltip to
              *communicate the complete model structure to the user.
              *Prefix is no longer appended, TODO: remove after final decision.
              */
              if(needTooltip){
                $prefix=$("<span></span>").append(modelText.charAt(0) + ".")
                .addClass("cursor-default")
                .tooltip({title: modelText,container:"body"});
              }
              
              /*Now build our span, this is what we return to callers, it contains all the dynamically built
               * DOM elements for our column header.
               * */
              $span=$("<span></span>").append(leafModel).addClass("cursor-default").attr({title:modelText})
                
              if(metaData[2]==="Condition"){
            	  $span.append($opBadge);
              }
              
              /*Determine the handsontable datatype for this column.
               * We need this info so we can attach the correct operator menu to the columnheaders $opbadge
               * */
              settings=instance.getSettings();
              colType=settings.columns[col].type;
              
              /*attribute columns are only involved in assignment operations,not comparison operations.*/
              if(category==="Attribute"){
            	  colType="checkbox";
              }

			  switch(colType){
				  case "numeric":
					  colType=operators.MenuCategory.CAT_NUMERIC;
					  break;
				  case "checkbox":
					  colType=operators.MenuCategory.CAT_BOOLEAN;
					  break;
				  case "dateTime":
				  case "date":
					  colType=operators.MenuCategory.CAT_DATE;
					  break;
				  case "autocomplete":
				  case "text":
					  colType=operators.MenuCategory.CAT_STRING;
					  break;
				  default:
					  colType=0;
			  }
			  validOps=operators.getOperators(colType);
			  validOpCount=validOps.length;
			  menuRows=[];
			  
              /*Lastly, add the click handler to the $opBadge to launch the operator-dialog-menu
               * !Be sure to appendTo the HoT instance root element!
               */
              $opBadge.on("click",function(event){
            	  instance.rootElement.trigger({
            		  type:"operatorMenu_request",
            		  columnType: colType,
            		  columnIndex: col,
            		  ref: $opBadge,
            		  columnCategory: category,
            		  instance: instance 
            		  });
            	  event.stopPropagation();
            	  return;
              });

            return $span;
          }
    };
    return chFactory;
  }
);