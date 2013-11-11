/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[   "bpm-modeler/js/m_utils", 
			"rules-manager/js/m_commandsDispatcher", 
			"rules-manager/js/m_i18nUtils",
			"bpm-modeler/js/m_jsfViewManager",
			"rules-manager/js/m_ruleSet", 
			"rules-manager/js/hotDecisionTable/m_decisionTable",
			"rules-manager/js/hotDecisionTable/m_tableConfig",
			"rules-manager/js/hotDecisionTable/m_treeFactory",
			"rules-manager/js/hotDecisionTable/m_typeParser",
			"rules-manager/js/hotDecisionTable/m_operatorMenuFactory",
			"rules-manager/js/m_i18nMapper",
			"rules-manager/js/m_ruleSetCommandDispatcher",
			"rules-manager/js/m_ruleSetCommand"],
		function(m_utils,CommandsDispatcher,m_i18nUtils,m_jsfViewManager, RuleSet,
				hotDecisionTable,tableConfig,treeFactory,
				typeParser,operatorMenuFactory,m_i18nMapper,
				m_ruleSetCommandDispatcher,m_ruleSetCommand) {
			return {
				initialize : function(ruleSetUuid,decTableUuid,options) {
					var ruleSet = RuleSet.findRuleSetByUuid(ruleSetUuid);
					var decTable=ruleSet.findDecisionTableByUuid(decTableUuid);
					var view = new DecisionTableView();
					view.initialize(ruleSet,decTable,options);
				}
			};

			function DecisionTableView() {
				
				this.initialize = function(ruleSet,decTable,options) {
					var paramDefCount,    /*Number of Parameter Definitions in our ruleset*/
						paramDef,         /*instance of a parameter definition*/
						typeDecl,         /*instance of a typeDeclaration*/
						typeBody,         /*result of a typeDecl.getBody() call*/
						jstreeDataNode,   /*typeBody serialized to a jsTree jsonData structure*/ 
						decTblSelector,   /*selector for our decision table*/
						codeEditSelector, /*selector for the Ace Code editor linked to the decision table*/
						newID,            /*because I don't trust HoT, Lets make a unique ID for the instance rootElement*/
						lastCmdID,        /*ID of the last DecisionTable.data.cmd sent to the $sink*/
						cnstCMD,		  /*Constant value commands from our command factory*/
						decTblInstance;   /*instance of handsontable/dectbl with methods etc..*/
					
					
					var uiElements={
							mainView: m_utils.jQuerySelect(options.selectors.id),
							uuidOutput: m_utils.jQuerySelect(options.selectors.uuidOutput),
							idOutput: m_utils.jQuerySelect(options.selectors.idOutput),
							nameInput: m_utils.jQuerySelect(options.selectors.nameInput),
							decisionTable: m_utils.jQuerySelect(options.selectors.decisionTable),
							decisionTableInstance: undefined,
							drlEditor: undefined,
							columnTreeButton: m_utils.jQuerySelect(options.selectors.columnTreeButton),
							addRowBtn: m_utils.jQuerySelect(options.selectors.addRowBtn),
							exportBtn: m_utils.jQuerySelect(options.selectors.exportBtn),
							addIcons: m_utils.jQuerySelect(options.selectors.addIcons),
							importIcons: m_utils.jQuerySelect(options.selectors.importIcons),
							decisionTableTabs: m_utils.jQuerySelect(options.selectors.decisionTableTabs),
							decisionTableCodeTab: m_utils.jQuerySelect(options.selectors.decisionTableCodeTab),
							hideNonDataColumns: m_utils.jQuerySelect(options.selectors.hideNonDataColumns),
							decisionTableTab: m_utils.jQuerySelect(options.selectors.decisionTableTab),
							decTableNameLabel:m_utils.jQuerySelect(options.selectors.decTableNameLabel),
							decTableIdLbl: m_utils.jQuerySelect(options.selectors.decTableIdLbl),
							decTableUuidLbl: m_utils.jQuerySelect(options.selectors.decTableUuidLbl),
							decTableDescrLbl: m_utils.jQuerySelect(options.selectors.decTableDescrLbl),
							exportData: m_utils.jQuerySelect(options.selectors.exportData),
							importData: m_utils.jQuerySelect(options.selectors.importData),
							descriptionTextarea: m_utils.jQuerySelect(options.selectors.descriptionTextarea),
							addRow: m_utils.jQuerySelect(options.selectors.addRow),
							decTableNameLbl: m_utils.jQuerySelect(options.selectors.decTableNameLbl)
					};
					
					/*TODO: [ZZM] Factor these out.*/
					this.drlEditor;
					this.id = "DecisionTableView";
					this.uuidOutput = m_utils.jQuerySelect("#DecisionTableView #uuidOutput");
					this.idOutput = m_utils.jQuerySelect("#DecisionTableView #idOutput");
					this.nameInput = m_utils.jQuerySelect("#DecisionTableView #nameInput");
					this.lastModificationDateOutput = m_utils.jQuerySelect("#DecisionTableView #lastModificationDateOutput");
					
					/*For brevity , access command constants using shorthand*/
					cnstCMD=m_ruleSetCommand.commands;
					
					/*Compute a new ID for our HoT instance rootElement based off of our decisiontable uuid*/
					newID=uiElements.decisionTable.attr("id") + "_" + decTable.uuid;
					options.selectors.decisionTable="#" + newID;
					$(uiElements.decisionTable).attr("id",newID);
					
					codeEditSelector="decisionTableCodeEditor";
				    
					m_i18nMapper.map(options,uiElements,true);
					
				    /* Initialization of decision table*/
					var decTableData=decTable.getTableData();
					/*--------------IMPORTANT-----------
					 * We are merging the data from our ruleSets' decision table into our tableConfig and
					 * passing this to the handsOnTable instance. The data merged references directly into the
					 * ruleSet. Implication being that as handsOnTable modifies its tableConfig,which includes the references to our data
					 * from the ruleSet, it is actually modifying the data in the original ruleSet. 
					 * Thus, there is no need to push changes back to our ruleSet as the user modifies the 
					 * table data as the handsOnTable is operating directly on that data. It is important the decTableData is the 
					 * target of the jquery extend call and not tableConifg (target is the element returned with modifications from
					 * the tableConfig.)
					 * */
					var extData=$.extend(decTableData,tableConfig);
				    hotDecisionTable.initialize(uiElements.decisionTable,extData);
				    uiElements.decisionTableInstance= uiElements.decisionTable.handsontable('getInstance');

				    /*bind UIElements to events from our top level command processor*/
				    m_ruleSetCommandDispatcher.register(uiElements.nameInput,cnstCMD.decTableRenameCmd);
				    uiElements.nameInput.on(cnstCMD.decTableRenameCmd,function(event,data){
				    	var elementID=data.elementID;
				    	var newVal=data.changes[0].value.after;
				    	if (elementID === decTable.uuid && newVal !=uiElements.nameInput.val) {
							uiElements.nameInput.val(newVal);
						}
				    });
				    
				    /*bind UIElements to events from our top level command processor*/
				    m_ruleSetCommandDispatcher.register(uiElements.descriptionTextarea,cnstCMD.decTableDescriptionCmd);
				    uiElements.descriptionTextarea.on(cnstCMD.decTableDescriptionCmd,function(event,data){
				    	var elementID=data.elementID;
				    	var newVal=data.changes[0].value.after;
				    	if (elementID === decTable.uuid && newVal !=uiElements.descriptionTextarea.val) {
							uiElements.descriptionTextarea.val(newVal);
						}
				    });
				    
				    /*TODO:remove after closure issue addressed*/
				    var snapshotBuilder=function(obj){
				    	var snapShot={
			    			columns: obj.columns.slice(0),
					        data: obj.data.slice(0),
					        colWidths: obj.colWidths.slice(0),
					        colHeaders: obj.colHeaders.slice(0)
				    	}
				    	return function(){
				    		return snapShot;
				    	}
				    }

				    /*Hook for the change event of our table: Saved for undo redo functionality*/
				    var afterChangeFunc=function(changes,source){
				    	console.log("Decision Table Change event , native...");
						var settings=uiElements.decisionTableInstance.getSettings();
						var dataSnapshot={
							columns: settings.columns.slice(0),
					        data: settings.data.slice(0),
					        colWidths: settings.colWidths.slice(0),
					        colHeaders: settings.colHeaders.slice(0)
						};
						var cmd=m_ruleSetCommand.decTableDataCmd(
								ruleSet,decTable,snapshotBuilder(settings)(),undefined);
						lastCmdID=cmd.id;
						m_ruleSetCommandDispatcher.trigger(cmd);
						ruleSet.state.isDirty=true;
				    };
					uiElements.decisionTableInstance.addHook('afterChange',afterChangeFunc);
					uiElements.decisionTableInstance.addHook("afterColumnResize",function(col,size){
						uiElements.decisionTableInstance.setColumnWidth
					});
				    /*bind UIElements to events from our top level command processor*/
				    m_ruleSetCommandDispatcher.register(uiElements.decisionTableInstance.rootElement,cnstCMD.decTableDataCmd);
				    uiElements.decisionTableInstance.rootElement.on(cnstCMD.decTableDataCmd,function(event,data){
				    	var elementID=data.elementID;
				    	var newVal=data.changes[0].value.after;
				    	var snapShot;
				    	console.log("DecisionTable.Data.Change received from sink");
				    	if (elementID === decTable.uuid) {
							uiElements.decisionTableInstance.removeHook("afterChange",afterChangeFunc);
							snapShot=snapshotBuilder(newVal)();
							decTable.setTableData(snapShot);
							uiElements.decisionTableInstance.updateSettings(decTable.getTableData());
							uiElements.decisionTableInstance.render();
							uiElements.decisionTableInstance.addHook("afterChange",afterChangeFunc);
						}
				    });
				    
				    
				    
				    /*bind behavior to our hideNonDataCols UIElement. ON click we toggle between 
				     * hiding and showing the non data columns in our decision table. NonData columns
				     * being DRL attribute columns (salience,enabled,etc...) and the description column.*/
				    uiElements.hideNonDataColumns.on("click",function(){
				    	var colsHidden=uiElements.hideNonDataColumns.attr("colsHidden");
				    	var settings=uiElements.decisionTableInstance.getSettings();
				    	
				    	colsHidden=(colsHidden==="true")?"false":"true";
				    	uiElements.hideNonDataColumns.attr("colsHidden",colsHidden);
				    	if(colsHidden==="true"){
				    		settings.helperFunctions.hideAttributeColumns(uiElements.decisionTableInstance);
				    	}
				    	else{
				    		settings.helperFunctions.showAttributeColumns(uiElements.decisionTableInstance);
				    	}
				    });
				    
				    /* Handle the custom JQUERY event for requests for operator menu dialogs.
				     * These events bubble up from our decisiontable and indicate a user interaction with
				     * an UI element which requires an operator selection menu. This is currently specific to the
				     * operator badges in the column headers of the table (see chMenuFactoryLegacy) */
				    var myOpenDialogs=[];
				    uiElements.decisionTableInstance.rootElement.on("operatorMenu_request",function(event){
				    	var openMenuCount=myOpenDialogs.length;
				    	while(openMenuCount--){
				    		myOpenDialogs[openMenuCount].dialog("destroy");
				    	}
				    	myOpenDialogs=[];
				    	var opDialog=operatorMenuFactory.getMenu(event);
				    	opDialog.dialog("option","position",{my: "left top",at: "left bottom", of: event.ref});
				    	opDialog.dialog("option","appendTo",uiElements.decisionTableInstance.rootElement);
				    	opDialog.dialog("open");
				    	opDialog.on("column_removed",function(event){
				    		console.log("column removed from decision table");
				    	});
				    	myOpenDialogs.push(opDialog);
				    });
				    
				    /*On any click event that bubbles to our root element, close all dialogs in our array*/
				    uiElements.decisionTableInstance.rootElement.on("click",function(event){
				    	var openMenuCount=myOpenDialogs.length;
				    	while(openMenuCount--){
				    		myOpenDialogs[openMenuCount].dialog("destroy");
				    	}
				    	myOpenDialogs=[];
				    });
				    
				    /* POC for modifying any cell value that can be parsed as numeric on a scrollwheel event.
				     * Stepsize is based on the number length (powers of 10) of the current cell value, such
				     * that the wheel event will increment the value by length-1 powers of 10. In other words,
				     * values on the order of 10^3 are incremented by 10^2 etc... (with exceptions for single digits)
				     * */
				    uiElements.decisionTableInstance.rootElement.bind("DOMMouseScroll onmousewheel mousewheel wheel",function(event){
				    	var direction=(event.originalEvent.wheelDeltaY > 0)?1:-1;
				    	var selectedCellRange=uiElements.decisionTableInstance.getSelected(),
					    	cellStart,
					    	cellEnd,
					    	tempVal,
					    	stepSize,
					    	sign,
					    	cellValue;
				    	if(selectedCellRange !=undefined){
				    		cellStart={
				    			x:selectedCellRange[0],
				    			y:selectedCellRange[1]	    		
				    		};
				    		cellEnd={
					    			x:selectedCellRange[2],
					    			y:selectedCellRange[3]	    		
					    		};
				    		cellValue=uiElements.decisionTableInstance.getDataAtCell(cellStart.x,cellStart.y);
				    		if($.isNumeric(cellValue)){
				    			tempVal=1*cellValue;
				    			sign = tempVal?tempVal<0?-1:1:0;
				    			tempVal=Math.abs(tempVal);
				    			/* ref: http://mathworld.wolfram.com/NumberLength.html */
				    			stepSize=Math.pow(10,Math.ceil(Math.log(tempVal + 1) / Math.LN10)-2);
				    			if(stepSize < 1){stepSize=1;}
				    			cellValue=sign*tempVal+(direction*stepSize);
				    			uiElements.decisionTableInstance.setDataAtCell(cellStart.x,cellStart.y,cellValue);
				    		}
				    	}		
				    });
				    
				    
				    /* If we have a ruleSet then convert the body of its parameterDefinitions into a jsTree JSON structure
				     * Do this on each click as to keep the data in the tree current with any changes that the 
				     * parameterDefinitons panel has made to our ruleSet.*/ 
				    var myDialog;
				    uiElements.columnTreeButton.on("click",function(){
				    	if(myDialog){
				    		try{
				    			myDialog.dialog("destroy");
				    		}catch(err){/*DO_NOTHING_AND_CONTINUE*/}
				    	}
				    	var columnBuildertree,
				    		jstreeInstance,
				    		jsonTreeData;
				    	
				    	if(ruleSet){
						    jsonTreeData=typeParser.parseParamDefinitonsToJsTree(ruleSet.parameterDefinitions);
						    columnBuildertree=treeFactory.getTree("",options.selectors.decisionTable,jsonTreeData);
						    jstreeInstance=$(".jstree",columnBuildertree);
		                    myDialog=$(columnBuildertree).dialog({
			                    	autoOpen: false,
			                    	maxHeight: 600,
			                    	buttons: [{
			                    		text: m_i18nUtils.getProperty("rules.propertyView.decisiontableview.dialog.addcolumn.buttonset.close","Close"), 
			                    		click: function(){$(this).dialog("destroy");}
			                    	}],
			                    	dialogClass: 'ui-camino-dialog',
			                    	appendTo: uiElements.mainView,
			                    	position: {my: "center",
			                    			   at: "center",
			                    			   of: uiElements.mainView,
			                    			   within: uiElements.mainView,
			                    		       collision: "fit"},
			                    	title: m_i18nUtils.getProperty("rules.propertyView.decisiontableview.dialog.addcolumn.titleBar","Add Column")});
		                    myDialog.dialog("open");
		                    myDialog.on("column_removed",function(event){
		                    	var path,
			                    	pathRoot,
			                    	pathModel,
			                    	leafVal;
		                    	if(event.category==="Attribute" || event.category==="Action"){
		                    		leafVal=event.colValue.split(".").pop();
		          					$treeNode=$("a:contains('" + leafVal +"')",jstreeInstance);
		          					$treeNode.each(function(){
		          						path=jstreeInstance.jstree("get_path",$(this));
		          						pathRoot=path[0];
		          						pathModel=path.slice(1).join(".");
		          						if(pathRoot===event.category +"s" && (pathModel==event.colValue)){
		          							$(this).removeClass("ipp-disabled-text");
		          						}
		          					});
		          				}
		                    });
				    	}
				    });
				    uiElements.decisionTableInstance.rootElement.on("column_removed",function(event){
				    	ruleSet.state.isDirty=true;
				    	if(myDialog){
				    		myDialog.trigger(event);
				    	}
				    });
				    
				    
				    /*add the add-decision-table-row functionality to matched elements*/
				    uiElements.addRow.on("click",function(){
			        	var instance=uiElements.decisionTableInstance;
			        	var settings=instance.getSettings();
			        	settings.helperFunctions.addDefaultRow(instance);
			        	ruleSet.state.isDirty=true;
			        });
				    
				    //initialize main tabs control for the view
					uiElements.decisionTableTabs.tabs();
					
					var view = this;
					
					/* Adding a hook for column resizing as this value was not actually being saved into
					 * the HandsOnTable config settings by the HoT widget (and thus not reflected in our ruleSet data).
					 * To avoid internal problems with Handsontable, we are not messaging this event as a command.
					 * This means column resizes aren't an individual event but rather get collected into whichever 
					 * event occurs next which is registered on the command stack. A rough workaround for the present.*/
					uiElements.decisionTableInstance.addHook("afterColumnResize",function(col,size){
						var settings=uiElements.decisionTableInstance.getSettings();
						settings.helperFunctions.setColumnWidth(uiElements.decisionTableInstance,col,size);
						ruleSet.state.isDirty=true;
					});
					
					/*binding textarea to the description attribute of our decision table*/
					uiElements.descriptionTextarea.val(decTable.description);
					uiElements.descriptionTextarea.on("change",function(event){
						decTable.description=uiElements.descriptionTextarea.val();
						ruleSet.state.isDirty=true;	
						/*Communciate event to our dispatcher in the sky.*/
						var cmd=m_ruleSetCommand.decTableDescriptionCmd(
								ruleSet,decTable,decTable.description,undefined);
						m_ruleSetCommandDispatcher.trigger(cmd);
					});
					
					/*binding input element to the value of the decisiontable name referenced in our ruleSet*/
					this.nameInput.change({view : this},function(event) {
						var oldName = decTable.name;
						decTable.name = uiElements.nameInput.val();
						ruleSet.state.isDirty=true;
						/*Communciate event to our dispatcher in the sky.*/
						var cmd=m_ruleSetCommand.decTableRenameCmd(
								ruleSet,decTable,decTable.name,undefined);
						m_ruleSetCommandDispatcher.trigger(cmd);

					});
					
					this.activate(ruleSet,false,decTable,uiElements);
				};

				this.activate = function(ruleSet,decisionTableUpdate,decTable,uiElements) {
					var drlText="";
					this.ruleSet = ruleSet;

					uiElements.uuidOutput.empty();
					uiElements.uuidOutput.append(decTable.uuid);
					uiElements.idOutput.empty();
					uiElements.idOutput.append(decTable.id);
					uiElements.nameInput.val(decTable.name);

					if (decisionTableUpdate) {
						this.decisionTable.activate(this.ruleSet,false,decTable,uiElements);
					}

				};
				
			}
		});