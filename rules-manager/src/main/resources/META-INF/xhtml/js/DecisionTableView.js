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
			"rules-manager/js/CommandsDispatcher", 
			"rules-manager/js/m_i18nUtils",
			"bpm-modeler/js/m_jsfViewManager",
			"rules-manager/js/RuleSet", 
			"rules-manager/js/hotDecisionTable/m_decisionTable",
			"rules-manager/js/hotDecisionTable/m_tableConfig",
			"rules-manager/js/hotDecisionTable/m_images",
			"rules-manager/js/hotDecisionTable/m_treeFactory",
			"rules-manager/js/hotDecisionTable/m_typeParser",
			"rules-manager/js/hotDecisionTable/m_operatorMenuFactory",
			"rules-manager/js/m_i18nMapper"],
		function(m_utils,CommandsDispatcher,m_i18nUtils,m_jsfViewManager, RuleSet,
				hotDecisionTable,tableConfig,images,treeFactory,typeParser,operatorMenuFactory,m_i18nMapper) {
			return {
				initialize : function(ruleSetUuid,decTableUuid,options) {
					var ruleSet = RuleSet.findRuleSetByUuid(ruleSetUuid);
					var decTable=ruleSet.findDecisionTableByUuid(decTableUuid);
					var view = new DecisionTableView();
					CommandsDispatcher.registerCommandHandler(view);
					view.initialize(ruleSet,decTable,options);
				}
			};

			/**
			 * 
			 */
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
							importData:m_utils.jQuerySelect(options.selectors.importData)
					};
					
					/*By Convention name and CommandsDispatcher.registerCommandHandler we link to windows.top
					 * command dispatches.*/
					this.processCommand=function(cmd){
						switch(cmd.name){
						case "DecisionTable.Rename":
							uiElements.nameInput.val(cmd.changes[1]);
							this.renameView(cmd.decTable);
							break;
						}
					};
					
					this.drlEditor;
					this.id = "DecisionTableView";
					this.uuidOutput = m_utils.jQuerySelect("#DecisionTableView #uuidOutput");
					this.idOutput = m_utils.jQuerySelect("#DecisionTableView #idOutput");
					this.nameInput = m_utils.jQuerySelect("#DecisionTableView #nameInput");
					this.lastModificationDateOutput = m_utils.jQuerySelect("#DecisionTableView #lastModificationDateOutput");
					
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
				    
				    
				    /*bind behavior to our hideNonDataCols UIElement. ON click we toggle between 
				     * hiding and showing the non data columns in our decision table. NonData columns
				     * being DRL attribute columns (salience,enabled,etc...) and the description column.*/
				    uiElements.hideNonDataColumns.on("click",function(){
				    	var settings=uiElements.decisionTableInstance.getSettings();
				    	settings.helperFunctions.toggleNonDataColumns(uiElements.decisionTableInstance);
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
				    
				    uiElements.decisionTableInstance.rootElement.on("contextmenu",function(event){
				    	console.log("mousedown");
				    	console.log(event);
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
				    	var columnBuildertree,
				    		jstreeInstance,
				    		jsonTreeData;
				    	
				    	if(ruleSet){
						    jsonTreeData=typeParser.parseParamDefinitonsToJsTree(ruleSet.parameterDefinitions);
						    columnBuildertree=treeFactory.getTree("",options.selectors.decisionTable,jsonTreeData);
						    jstreeInstance=$(".jstree",columnBuildertree);
		                    myDialog=$(columnBuildertree).dialog({
			                    	autoOpen: false,
			                    	buttons: [{
			                    		text: "Close", click: function(){$(this).dialog("destroy");}
			                    	}],
			                    	open: function(){
			                    		$("button.ui-dialog-titlebar-close",myDialog.prev()).text("X");
			                    	},
			                    	dialogClass: 'ui-camino-dialog',
			                    	appendTo: uiElements.mainView,
			                    	position: {my: "center",
			                    			   at: "center",
			                    			   of: uiElements.mainView,
			                    			   within: uiElements.mainView,
			                    		       collision: "fit"},
			                    	title: "Add Column"});
		                    myDialog.dialog("open");
		                    myDialog.on("column_removed",function(event){
		                    	if(event.category==="Attribute"){
		                    		$("a:contains('" + event.colValue +"')",columnBuildertree).removeClass("ipp-disabled-text");
		                    	}
		                    });
				    	}
				    });
				    uiElements.decisionTableInstance.rootElement.on("column_removed",function(event){
				    	if(myDialog){
				    		myDialog.trigger(event);
				    	}
				    });
				    
				    
				    /*add the add-decision-table-row functionality to matched elements*/
				    uiElements.addRowBtn.each(function(){
				        $(this).on("click",function(){
				        	var instance=uiElements.decisionTableInstance;
				        	var settings=instance.getSettings();
				        	settings.helperFunctions.addDefaultRow(instance);
				        	//uiElements.decisionTableInstance.alter('insert_row');
				        });
				      });
				    
				    //add source image for all matched elements
				    uiElements.exportBtn.each(function(){
				      $(this).attr("src",images.export);
				    });

				    //add source image for all matched elements
				    uiElements.addIcons.each(function(){
				      $(this).attr("src",images.addsrc);
				    });
				    
				    //add source image for all images with a class of import-icon
				    uiElements.importIcons.each(function(){
				      $(this).attr("src",images.import);
				    });
					
				    //initialize main tabs control for the view
					uiElements.decisionTableTabs.tabs();
					
					var view = this;
					
					/*Hook for the change event of our table: Saved for undo redo functionality*/
					uiElements.decisionTableInstance.addHook('afterChange', function(changes,source) {
						console.log("Decision Table Change event...");
						//console.log(ruleSet.toJSON("PRE-DRL"));
					});
					
					/* Adding a hook for column resizing as this value was not actually being saved into
					 * the HandsOnTable config settings by the HoT widget (and thus not reflected in our ruleSet data).
					 */
					uiElements.decisionTableInstance.addHook("afterColumnResize",function(col,size){
						decTableData.colWidths[col]=size;
					});
					
					/*binding input element to the value of the decisiontable name referenced in our ruleSet*/
					this.nameInput.change({view : this},function(event) {
						decTable.name = uiElements.nameInput.val();
						view.renameView(decTable);
						CommandsDispatcher.submitCommand();
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
				
				this.renameView = function(decTable) {
					m_jsfViewManager.create().updateView("decisionTableView", "name" + "=" + decTable.name,
							decTable.uuid);	
				};
			}
		});