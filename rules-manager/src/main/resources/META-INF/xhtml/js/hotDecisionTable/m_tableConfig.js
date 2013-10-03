define(["jquery","./m_renderEngines","./m_dataFactory","./m_chFactory","./m_images","./m_chMenuFactoryLegacy","./m_utilities","./m_operators"],
      function($,renderEngines,dataFactory,colHdrFactory,images,chMenuFactory,utils,operators){
      var tableConfig={
        cells:function(row, col, prop){
          /*Column zero needs a special renderer to add DOM elements for the
          add/remove row buttons*/
          if(col===0){
            return{
              type:{renderer:renderEngines.rowHeader},
              readOnly: true
            };
          }
        },
        beforeKeyDown: function(event){
        	console.log("Before KeyDown");
        	console.log(event);
        },
        beforeChange: function(changes,source){
        	console.log("BeforeChange- THIS");
        	console.log(source);
        },
        afterChange: function (changes, source){
        	console.log("AfterChange- THIS");
        	console.log(source);
        },
        afterColumnMove: function(oldIndex,newIndex){
        	/*HoT does not actually move the underlying config and data, so we have to.
        	 *HoT will only modify its manualColumnPositions array which is pure UI sugar
        	 *and does not reflect the underlying data. SO, we are left to implement the deep changes.*/
        	var settings=this.getSettings();
        		headers=settings.colHeaders,
        		hdrCheck=headers[newIndex].split("|")[2],
        		myHdr=headers[oldIndex].split("|")[2];
        		
        	/*only allow a manual column move if we are swapping the same categories
        	 * and our move type is not Header*/
        	if(hdrCheck===myHdr && myHdr !=="Header"){
        		settings.helperFunctions.moveColumn(this,oldIndex,newIndex);
        	}
        	else{
        		/*Reset the manualColumnPositions so they reflect that we did not actually move the data
        		 * otherwise our UI representation is out of sync with our settings data and config*/
        		this.manualColumnPositions.splice(oldIndex, 0, this.manualColumnPositions.splice(newIndex, 1)[0]);
            	this.forceFullRender=true;
            	this.view.render();
            	this.PluginHooks.run('persistentStateSave', 'manualColumnPositions', this.manualColumnPositions);
        	}
        	
        },
        afterRemoveRow: function(index,amount){
          //console.log("afterRemoveRow");
        },
        afterCreateRow: function(index,amount){
          //console.log("afterCreateRow");
        },
        afterGetColHeader: function(col,TH){
          /*check if we have a plain text meta header that needs to be converted 
          into a DOM header by our chFactory, otherwise ignore...*/
          var $hdrObj,
              $colObj,
              $img,
              $colHeader,
              ch = this.getColHeader(col),
              patt=/^([^|]*\|){2,2}[^|]*$/,
              $categoryLabel,
              instance,
              $TH,
              category;
          instance=this;
          $colHeader= $(".colHeader",TH).text();
          if(patt.test($colHeader)===true){
        	$(TH).attr("data-initialized",true);
            category= $colHeader.split("|")[2];
            $hdrObj=chMenuFactory.AttributeHeader($colHeader,col,this,TH);
            $colObj=$(".colHeader",TH);
            $colObj.empty().append($hdrObj);
            if(category.toLowerCase() !="header"){
              $categoryLabel=$("<div class='hot-splithdr-label'>" + category +"</div>");
              $colObj.prepend( $categoryLabel);
               $img=$(images.remove).on("click",function(event){
                var settings=instance.getSettings();
                settings.helperFunctions.removeColumn(instance,col);
                instance.rootElement.trigger({
                	type:"column_removed",
                	"category":category,
                	colValue: $colHeader.split("|")[0]});
              }
            );
            $img.addClass("pull-right");
            $img.css("margin-right","2px");
            $img.css("margin-top","4px");
            $categoryLabel.append( $img);
            }
          }
         
        },
        minRows: 1,
        fillHandle : true,
        minCols: 1,
        minSpareCols: 0,
        minSpareRows: 0,
        manualColumnMove: true,
        manualColumnResize: true,
        rowHeaders: false,
        contextMenu:false,
        helperFunctions:{
          addDefaultRow: function(instance){
        	  var settings=instance.getSettings(),
        	  	  columns,
        	      rowCount,
        	      colCount,
        	      newRow;
        	  
        	  columns=settings.columns;
        	  colCount=columns.length;
        	  rowCount=settings.data.length;
        	  newRow=settings.data[0].slice();
        	  
        	  while(colCount--){
        		  switch(columns[colCount].type){
	        		  case "autocomplete":
	        		  case "text":
	        			  newRow[colCount]="";
	        			  break;
	        		  case "numeric":
	        			  newRow[colCount]=columns[colCount].default;
	        			  break;
	        		  case "checkbox":
	        			  newRow[colCount]=columns[colCount].default;
	        			  break;
	        		  case "date":
	        		  	newRow[colCount]=columns[colCount].default;
	        		  	break;
	        		  default:
	        			newRow[colCount]="";
        		  }
        	  }
        	  settings.data.push(newRow);
        	  instance.updateSettings({data: settings.data});
          },
          parseRowToDRL: function(index,instance){
        	  var hdrCells=instance.getColHeader(),
        	  	  rowData=instance.getDataAtRow(index),
	        	  cellCount=rowData.length,
	        	  hdrData,hdrOp,hdrCategory,hdrModel,
	        	  opObject,
	        	  rowCellValue,
	        	  metaInstance={},
	        	  metaData={attributes:[],conditions:[],actions:[],description:""},
	        	  drlString="",
	        	  attrs,
        	  	  lhs,
        	  	  rhs;
        	  
        	  /*Collect our row and header data into a metaobject we can stringify to DRL*/
        	  while(cellCount--){
        		  hdrData=hdrCells[cellCount].split("|");
        		  if(hdrData.length !=3){
        			  console.log('Error: tableConfig.js - parseRowToDRL: ' +
        					      'Invalid column header data structure.');
        			  return "Invalid Column Structure.";
        		  }
        		  hdrModel=hdrData[0];
        		  hdrOp=hdrData[1].trim();
        		  opObject=operators.getOperatorByHashCode(utils.hashString(hdrOp));
        		  opObject=opObject || {DRLoperator: hdrOp};
        		  hdrCategory=hdrData[2];
        		  rowCellValue=rowData[cellCount];
        		  metaInstance={
    				  model:hdrModel,
    				  op: opObject.DRLoperator,
    				  category: hdrCategory,
    				  val: rowCellValue
    			  };
        		  switch(hdrCategory.toUpperCase()){
	        		  case "ATTRIBUTE":
	        			  metaData.attributes.push(metaInstance);
	        			  break;
	        		  case "ACTION":
	        			  metaData.actions.push(metaInstance);
	        			  break;
	        		  case "CONDITION":
	        			  metaData.conditions.push(metaInstance);
	        			  break;
	        		  case "HEADER":
	        			  if(hdrModel.toUpperCase()==="DESCRIPTION"){
	        				  metaData.description=rowCellValue;
	        			  }
	        			  break;
	        		  default:
	        			  console.log("Error: RulesManager.tableConfig.js - " +
	        					      "ColHdr category unrecognized." + hdrCategory);
        		  }
        	  }	  
        	  /***R2**************/
        	  var condDict={};
        	  var condCount=metaData.conditions.length;
        	  var tempCond;
        	  var keyCond;
        	  var condClause="";
        	  var clasueCount;
        	  var tempObj;
        	  var factType="";
        	  var allConditions="";
        	  var condKey;
        	  /*Collect all conditions by their Fact-type (ParamDef)*/
        	  while(condCount--){
        		  tempCond=metaData.conditions[condCount];
        		  factType=tempCond.model.split(".")[0];
        		  if(!condDict.hasOwnProperty(factType)){
        			  condDict[factType]=[];
        		  }
        		  condDict[factType].push(tempCond);
        	  }
        	  console.log(condDict);
        	  factType=factType.replace(" ","_");
        	  for(condKey in condDict){
        		  condClause="";
        		  if(condDict.hasOwnProperty(condKey)){	  
        			  
	        		   tempCond=condDict[condKey];
	        		   clauseCount=tempCond.length;
	        		   
	        		   while(clauseCount--){
	        			   tempObj=tempCond[clauseCount];
	        			   condClause += tempObj.model.substring(tempObj.model.lastIndexOf(".")+1) + " " + tempObj.op + " " + tempObj.val;
	        			   if(clauseCount >0){
	        				   condClause += "  && ";
	        			   }
	        			   else{
	        				   condClause="\t$" +condKey + " : " + condKey + " (" + condClause + ")\n";
	        			   }
	        		   }
	        		   allConditions += condClause;
        		  }
        	  }
        	  console.log(allConditions);
        	  /*******************/
        	  /*now we have our row prepped as a JSON object we can examine in order
        	   * to generate our DRL.*/
        	    attrs=$.map(metaData.attributes,function(val,i){
        	        return "\t"+val.model + val.op + val.val;
        	      }).join("\r\n")+"\r\n";
        	      
        	    /*Build left hand side: Conditions*/
        	   lhs=$.map(metaData.conditions,function(val,i){
        	      var rootModel,
        	          leafmodel,
        	           dotLocation=val.model.indexOf(".");
        	      if(dotLocation>=0){
        	        rootModel=val.model.substring(0,dotLocation);
        	        leafModel=val.model.substring(val.model.lastIndexOf(".")+1);
        	        return "\t$" + rootModel + " : " +  rootModel + 
        	               "(" + leafModel + " " + val.op + " " + val.val + ")";
        	      }
        	      else{
        	        return "\t$" + val.model + " : " + val.model + " " + val.op + " " + val.val;
        	      }
        	    }).join("\r\n")+"\r\n";
        	      
        	    /*Build right hand side: Actions*/
        	    rhs=$.map(metaData.actions,function(val,i){
        	      return "\t" +val.model + " " + val.op + " " + val.val;
        	    }).join("\r\n");
        	    
        	    /*concat all sections into our final drl rule block*/
        	    drlString="rule \"" + metaData.description + "\"\r\n" +
        	              attrs + 
        	              "when\r\n" + allConditions + 
        	              "then\r\n" + rhs + 
        	              "\r\nend\r\n";
        	  return drlString;
          },
          headerTypeLocation: function(instance,hdrType){
            var settings=instance.getSettings(),
                hdrCounter=settings.colHeaders.length,
                metaHdr,
                precedence={
                    Header:0,Attribute:1,Condition:2,Action:3
                };
                
            while(hdrCounter--){
              metaHdr=settings.colHeaders[hdrCounter].split("|")[2];
              if(precedence[metaHdr] <= precedence[hdrType]){
                return hdrCounter + 1;
              }
            }
          },
          addColumn: function(instance,index,obj,width){
            var settings=instance.getSettings(),
                dataCounter=settings.data.length;
            
            while(dataCounter--){
              settings.data[dataCounter].splice(index,0,obj.type.default);
            }
            settings.columns.splice(index,0,obj.type);
            settings.colHeaders.splice(index,0,obj.hdr);
            settings.colWidths.splice(index,0,width);
            instance.updateSettings({
            	data: settings.data,
            	columns: settings.columns,
            	colHeaders: settings.colHeaders,
            	colWidths: settings.colWidths
            });
          },
          removeColumn: function(instance,index){
            var settings=instance.getSettings(),
                dataCounter=settings.data.length;
            //need cell editors to disengage before we remove the column. 
            instance.deselectCell();  
            while(dataCounter--){
              settings.data[dataCounter].splice(index,1);
            }
            settings.columns.splice(index,1);
            settings.colHeaders.splice(index,1);
            settings.colWidths.splice(index,1);
            instance.updateSettings({
            	data: settings.data,
            	columns: settings.columns,
            	colHeaders: settings.colHeaders,
            	colWidths: settings.colWidths
            });
          },
          moveColumn: function(instance,oldIndex,newIndex){
    	    var settings=instance.getSettings();
    	    var dataCounter=settings.data.length;
    	    while(dataCounter--){
    	      settings.data[dataCounter].splice(newIndex,0,settings.data[dataCounter].splice(oldIndex,1)[0]);
    	    }
    	    settings.columns.splice(newIndex, 0, settings.columns.splice(oldIndex, 1)[0]);
    	    settings.colHeaders.splice(newIndex, 0, settings.colHeaders.splice(oldIndex, 1)[0]);
    	    settings.colWidths.splice(newIndex, 0, settings.colWidths.splice(oldIndex, 1)[0]);
    	    instance.updateSettings({
            	data: settings.data,
            	columns: settings.columns,
            	colHeaders: settings.colHeaders,
            	colWidths: settings.colWidths
            });
    	    console.log("getting column data");
    	    console.log(settings.columns[newIndex]);
          },
          setColumnWidth: function(index,width){
            //TODO: implement, need ref to instance
          }
        }
      };
    return tableConfig;
});