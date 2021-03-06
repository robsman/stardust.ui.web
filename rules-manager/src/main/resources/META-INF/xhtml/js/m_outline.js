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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_urlUtils",
				"bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_extensionManager",
				"bpm-modeler/js/m_session", "bpm-modeler/js/m_user",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_process",
				"bpm-modeler/js/m_application", "bpm-modeler/js/m_participant",
				"bpm-modeler/js/m_typeDeclaration",
				"bpm-modeler/js/m_data",
				"bpm-modeler/js/m_elementConfiguration",
				"bpm-modeler/js/m_jsfViewManager",
				"bpm-modeler/js/m_messageDisplay",
				"rules-manager/js/m_i18nUtils",
				"bpm-modeler/js/m_communicationController",
				"bpm-modeler/js/m_jsfViewManagerHelper",
				"rules-manager/js/m_outlineToolbarController",
				"rules-manager/js/m_commandsDispatcher",
				"rules-manager/js/m_ruleSet",
				"rules-manager/js/m_i18nMapper",
				"rules-manager/js/m_ruleSetCommandDispatcher",
				"rules-manager/js/m_ruleSetCommand",
				"rules-manager/js/hotDecisionTable/m_utilities",
				"rules-manager/js/m_rulesManagerService",],
		function(m_utils, m_urlUtils, m_constants, m_extensionManager,
				m_session, m_user, m_model, m_process, m_application,
				m_participant, m_typeDeclaration, m_data, m_elementConfiguration, m_jsfViewManager,
				m_messageDisplay, m_i18nUtils, m_communicationController, m_jsfViewManagerHelper,
				m_outlineToolbarController, CommandsDispatcher, RuleSet,
				m_i18nMapper,m_ruleSetCommandDispatcher,m_ruleSetCommand,
				m_utilities,m_rulesManagerService) {
			
			var that = this;
			var isElementCreatedViaOutline = false;
			var hasUnsavedModifications = false;
			var displayScope = "";
			var viewManager;
			
			this.supportsRuleSetPublish = false;
			//Retrieve artifact types
			m_rulesManagerService.getSupportedArtifactTypes()
			.done(function(data){
				
				that.supportsRuleSetPublish = data.some(function(v){
					return v.id ==="drools-ruleset";
				});
				
				if(that.supportsRuleSetPublish===false){
					jQuery("#selectRuleMode").hide();
				}
			})
			.fail(function(){
				//fail
			});
			

			var readAllRuleSets = function(force,mode) {

				loadCustomTheme();
				mode = (mode === "DESIGN" || mode ==="PUBLISHED")?mode:"DESIGN";
				// Needed for types
				m_model.loadModels(false);
				
				jQuery("#lastsave").text(m_i18nUtils
										.getProperty("rules.outline.labels.lastSave"));
	
				jQuery.each(RuleSet.getRuleSets(force,mode),
					function(index, ruleSet) {

						if(ruleSet.state.isDeleted===false){
							
							createRuleSetNode(ruleSet);
						
							if (ruleSet.technicalRules) {
								jQuery.each(ruleSet.technicalRules, function(index, techRule) {
									createTechinicalRuleNode(ruleSet, techRule);
								});	
							}
							
							if (ruleSet.decisionTables) {
								jQuery.each(ruleSet.decisionTables, function(index, decTable) {
									createDecisionTableNode(ruleSet, decTable);
								});	
							}
							
							m_utils.jQuerySelect(displayScope + "#outline").jstree("close_node", "#" + ruleSet.uuid);
						} /*If ruleSet.state.isdeleted condition check end*/
					}); /*JQUERY Each->RuleSet loop end*/
				
				m_utils.debug("Tree initialized");

				hasUnsavedModifications = false;
				//jQuery("#undoChange").addClass("toolDisabled");
				//jQuery("#redoChange").addClass("toolDisabled");
			};
			
			var createRuleSetNode = function(ruleSet) {
				var node=jQuery(displayScope + "#outline").jstree('get_selected').trigger("blur");
				$("a",node).removeClass("jstree-clicked");
				jQuery(displayScope + "#outline").jstree("create", displayScope + "#outline",
						"last", {
							"attr" : {
								"id" : ruleSet.uuid,
								"rel" : ruleSet.type,
								"elementId" : ruleSet.id,
								"title": ruleSet.description
							},
							"data" : { title : ruleSet.name , icon : 'pi pi-rule-set pi-lg'}
						}, null, true);
				jQuery(displayScope + "#outline").jstree("set_type", "ruleSet",
						"#" + ruleSet.uuid);
			};
			
			var createDecisionTableNode = function(ruleSet, decTable) {
				jQuery(displayScope + "#outline").jstree("create",
						"#" + ruleSet.uuid, "last", {
							"attr" : {
								"id" : decTable.uuid,
								"title" : decTable.description,
								"ruleSetId" : ruleSet.id,
								"ruleSetUuid" : ruleSet.uuid,
								"rel" : "DecisionTable",
								"draggable" : true,
								"elementId" : decTable.id
							},
							"data" : { title : decTable.name , icon : 'pi pi-decision-table pi-lg '}
						}, null, true);
			};
			
			var createTechinicalRuleNode = function(ruleSet, techRule) {
				jQuery(displayScope + "#outline").jstree("create",
						"#" + ruleSet.uuid, "last", {
							"attr" : {
								"id" : techRule.uuid,
								"title": techRule.description,
								"ruleSetId" : ruleSet.id,
								"ruleSetUuid" : ruleSet.uuid,
								"rel" : "TechnicalRule",
								"draggable" : true,
								"elementId" : techRule.id
							},
							"data" : { title : techRule.name , icon : 'pi pi-technical-rule pi-lg '}
						}, null, true);
			};
			
			/*Publishes a design-time rule set*/
			var publishRuleSet = function(uuid){
				
				//Find our ruleSet as we will need the id from it for our url.
				var ruleSet = RuleSet.findRuleSetByUuid(uuid,"DESIGN");
				
				//Set up success callback;
				var successCallback = function(){
					
					var message = m_i18nUtils.getProperty("rules.messages.publish.success", "Ruleset, {0} was successfully published.");
					message = message.replace("{0}", ruleSet.name);
					
					var data = {
							"attributes" : {
								"width" : "400px",
								"height" : "200px",
								"src" : m_urlUtils.getPlugsInRoot() + "bpm-modeler/popups/notificationDialog.html"
							},
							"payload" : {
								"title" : m_i18nUtils.getProperty("rules.messages.publish.title.success","Success"),
								"message" : message,
								"okButtonText" :  m_i18nUtils.getProperty("rules.messages.confirm.close","Close")
							}
						};
					
					parent.iPopupDialog.openPopup(data);
				};
				
				//Set up error callback;
				var errorCallback = function(){
					
					var message = m_i18nUtils.getProperty("rules.messages.publish.error","An error occurred publishing ruleset, {0}.");
					message = message.replace("{0}",ruleSet.name);
					
					var data = {
						"attributes" : {
							"width" : "400px",
							"height" : "200px",
							"src" : m_urlUtils.getPlugsInRoot() + "bpm-modeler/popups/errorDialog.html"
						},
						"payload" : {
							"title" :  m_i18nUtils.getProperty("rules.messages.warning","Warning"),
							"message" :  message,
							"okButtonText" : m_i18nUtils.getProperty("rules.messages.confirm.close","Close")
						}
					};
					
					parent.iPopupDialog.openPopup(data);
				};
				RuleSet.publishRuleSet(ruleSet.id,successCallback,errorCallback);
			};
			
			var exportRuleSet = function(uuid,mode) {
				
				var ruleSet,
					terminalPoint,
					url;
				
				//guard mode value to ensure it is valid
				mode = (mode === "DESIGN" || mode ==="PUBLISHED")?mode:"DESIGN";
				ruleSet = RuleSet.findRuleSetByUuid(uuid,mode);
				
				terminalPoint = (mode==="DESIGN")?"design-time":"run-time";
				
				if (!areRuleSetsSaved()) {
					if (parent.iPopupDialog) {
						parent.iPopupDialog
								.openPopup({
									attributes : {
										width : "400px",
										height : "200px",
										src : m_urlUtils.getPlugsInRoot()
												+ "bpm-modeler/popups/confirmationPopupDialogContent.html"
									},
									payload : {
									   title : m_i18nUtils.getProperty("rules.messages.warning","Warning"),
                              message : m_i18nUtils.getProperty("rules.messages.info.ruleSetsNotSaved","Rule-sets have unsaved changes.<BR><BR>Please save rule-sets before continuing."),
                              acceptButtonText : m_i18nUtils.getProperty("rules.messages.confirm.close","Close"),
										acceptFunction : function() {
											// Do nothing
										}
									}
								});
					} else {
						alert(m_i18nUtils.getProperty("rules.messages.info.ruleSetsNotSaved","Rule-sets have unsaved changes.<BR><BR>Please save rule-sets before continuing."));
					}
				} else {
					if (ruleSet) {
						url =  m_urlUtils.getContextName() + "/services/rest/rules-manager/rules/";
						url += new Date().getTime() + "/rule-sets/" + terminalPoint;
						url += "/" + encodeURIComponent(ruleSet.id) + "/download";
						window.location = url;
						//window.location = m_urlUtils.getContextName() + "/services/rest/rules-manager/rules/" + new Date().getTime() + "/ruleSet/" + encodeURIComponent(ruleSet.id) + "/download"
					}	
				}
			}

			var areRuleSetsSaved = function() {
				var saved = true;
				jQuery.each(RuleSet.getRuleSets(), function(index, ruleSet) {
					if (ruleSet.state &&
							(ruleSet.state.isDirty == true
									|| ruleSet.state.isPersisted == false)) {
						saved = false;
					}
				});
				
				return saved;
			};
			
			// TODO Is this still needed? Delete after verifying
			var elementCreationHandler = function(id, name, type, parent) {
				if (type == 'activity') {
					var parentSelector = '#' + parent;
					jQuery(displayScope + "#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"id" : id,
									"rel" : "manual_activity",
									"draggable" : true
								},
								"data" : name
							}, null, true);
				} else if (type == "subProcessActivity") {
					var parentSelector = '#' + parent;
					jQuery(displayScope + "#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"id" : id,
									"rel" : "sub_process_activity"
								},
								"data" : name
							}, null, true);
				} else if (type == 'primitiveDataType') {
					var parentSelector = '#' + parent;
					jQuery(displayScope + "#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"id" : id,
									"rel" : "Primitive_Data",
									"draggable" : true
								},
								"data" : name
							}, null, true);
				} else if (type == 'role') {
					var parentSelector = '#' + parent;
					jQuery(displayScope + "#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"id" : id,
									rel : "participant_role",
									"draggable" : true
								},
								"data" : name
							}, null, true);
				}
			};

			var elementRenamingHandler = function(attrs) {
				if (attrs.action == 'Rename') {
					var rLink = jQuery("li#" + attrs.id + " a")[0];
					var textElem = jQuery(rLink.childNodes[1])[0];
					textElem.nodeValue = attrs.props.completetext;
				}
			};

			var renameNodeHandler = function(event, data) {
				var nodeType=data.rslt.obj.attr('rel');
				var newID;
				if (nodeType == 'ruleSet') {
					var ruleSet = RuleSet.findRuleSetByUuid(data.rslt.obj.attr("id"),"DESIGN");
					var oldName = ruleSet.name;
					var newName = data.rslt.name;
					ruleSet.name = newName;
					if(oldName != newName){
						ruleSet.state.isDirty=true;
					}
					var cmd=m_ruleSetCommand.ruleSetRenameCmd(
							ruleSet,newName,event);
					m_ruleSetCommandDispatcher.trigger(cmd);
				} 
				else if(nodeType=="DecisionTable"){
					var ruleSet = RuleSet.findRuleSetByUuid(data.rslt.obj.attr("ruleSetUuid"),"DESIGN");
					var decTable = ruleSet.findDecisionTableByUuid(data.rslt.obj.attr("id"));
					var oldName=decTable.name;
					var newName=data.rslt.name;
					decTable.name=newName;
					if(oldName != newName){
						ruleSet.state.isDirty=true;
					}
					var cmd=m_ruleSetCommand.decTableRenameCmd(
							ruleSet,decTable,decTable.name,{});
					m_ruleSetCommandDispatcher.trigger(cmd);
				}
				else if(nodeType=="TechnicalRule"){
					var ruleSet = RuleSet.findRuleSetByUuid(data.rslt.obj.attr("ruleSetUuid"),"DESIGN");
					var techRule = ruleSet.findTechnicalRuleByUuid(data.rslt.obj.attr("id"));
					var oldName=techRule.name;
					var newName=data.rslt.name;
					techRule.name=newName;
					if(oldName != newName){
						ruleSet.state.isDirty=true;
					}
					var cmd=m_ruleSetCommand.ruleRenameCmd(
							ruleSet,techRule,newName,event);
					m_ruleSetCommandDispatcher.trigger(cmd);
					
				}
			};

			var refresh = function() {
				if (parent.iPopupDialog) {
					parent.iPopupDialog
							.openPopup({
								attributes : {
									width : "400px",
									height : "200px",
									src : m_urlUtils.getPlugsInRoot()
											+ "rules-manager/popups/outlineRefreshConfirmationDialog.html"
								},
                              payload : {
                                 title : m_i18nUtils
                                          .getProperty(
                                                   "rules.messages.confirm",
                                                   "Confirm"),
                                 message : m_i18nUtils
                                          .getProperty(
                                                   "rules.messages.confirm.ruleSetsReload",
                                                   "All rule-sets will be reloaded from their last saved state and the session log will be cleared.<BR><BR>Continue?<BR><BR>"),
                                 acceptButtonText : m_i18nUtils
                                          .getProperty(
                                                   "rules.messages.confirm.yes",
                                                   "Yes"),
                                 cancelButtonText : m_i18nUtils
                                          .getProperty(
                                                   "rules.messages.confirm.no",
                                                   "No"),
                                 acceptFunction : reloadOutlineTreeReset,
                                 checkboxLabelText : m_i18nUtils
                                          .getProperty(
                                                   "rules.messages.confirm.saveRuleSetsPriorToReload",
                                                   "Save Rule-Sets prior to reload")
								}
							});
				}
			};
			
			var reloadOutlineTreeReset = function(saveFirst) {
				if (true == saveFirst) {
					saveRuleSets();
				}

				// close all rules views, if open
				closeAllRulesViews();

				reloadOutlineTree(true);
			};
			
			var closeAllRulesViews = function() {
				jQuery.each(RuleSet.getRuleSets(), function(index, ruleSet) {
					viewManager.closeViewsForElement(ruleSet.uuid);
				});
			};
			
			var reloadOutlineTree = function(force) {
				var mode;
				
				jQuery(displayScope + "#outline").empty();
				mode = getCurrentMode();
				readAllRuleSets(force,mode);
			};
			
			/**
			 * Retrieves the current mode as specified in select dropdown (DESIGN | PUBLISHED)
			 */
			var getCurrentMode = function(){
				var selectRuleMode,
					mode;
				
				selectRuleMode = document.getElementById('selectRuleMode');
				if(selectRuleMode && selectRuleMode.options  && selectRuleMode.options.length > 0){
					mode = selectRuleMode.options[selectRuleMode.selectedIndex].value;
				}
				else{
					mode="DESIGN";
				}
				
				return mode;
				
			};
			
			var importRuleSet = function() {
				if (!areRuleSetsSaved()) {
					if (parent.iPopupDialog) {
						parent.iPopupDialog
								.openPopup({
									attributes : {
										width : "400px",
										height : "200px",
										src : m_urlUtils.getPlugsInRoot()
												+ "bpm-modeler/popups/confirmationPopupDialogContent.html"
									},
									payload : {
										title : m_i18nUtils.getProperty("rules.messages.warning","Warning"),
										message : m_i18nUtils.getProperty("rules.messages.info.ruleSetsNotSaved","Rule-sets have unsaved changes.<BR><BR>Please save rule-sets before continuing."),
										acceptButtonText : m_i18nUtils.getProperty("rules.messages.confirm.close","Close"),
										acceptFunction : function() {
											// Do nothing
										}
									}
								});
					} else {
						alert(m_i18nUtils.getProperty("rules.messages.info.ruleSetsNotSaved","Rule-sets have unsaved changes.<BR><BR>Please save rule-sets before continuing."));
					}
				} else {
					var link = m_utils.jQuerySelect(
							"a[id $= 'open_rules_upload_dialog_link']",
							m_utils.getOutlineWindowAndDocument().doc);
					var linkId = link.attr('id');
					var form = link.parents('form:first');
					var formId = form.attr('id');
					m_jsfViewManagerHelper
							.openImportModelDialog(linkId, formId);
				}
			}

			var undoMostCurrent = function() {
				m_communicationController.postData({
					url : m_communicationController.getEndpointUrl()
							+ "/sessions/changes/mostCurrent/navigation"
				}, "undoMostCurrent", {
					success : function(data) {
						m_utils.debug("Undo");
						m_utils.debug(data);

						m_commandsController.broadcastCommandUndo(data);

						if (null != data.pendingUndo) {
							jQuery("#undoChange").removeClass("toolDisabled");
						} else {
							jQuery("#undoChange").addClass("toolDisabled");
						}

						if (null != data.pendingRedo) {
							jQuery("#redoChange").removeClass("toolDisabled");
						} else {
							jQuery("#redoChange").addClass("toolDisabled");
						}
					}
				});
			}

			var redoLastUndo = function() {
				m_communicationController.postData({
					url : m_communicationController.getEndpointUrl()
							+ "/sessions/changes/mostCurrent/navigation"
				}, "redoLastUndo", {
					success : function(data) {
						m_utils.debug("Redo");
						m_utils.debug(data);

						m_commandsController.broadcastCommand(data);

						if (null != data.pendingUndo) {
							jQuery("#undoChange").removeClass("toolDisabled");
						} else {
							jQuery("#undoChange").addClass("toolDisabled");
						}

						if (null != data.pendingRedo) {
							jQuery("#redoChange").removeClass("toolDisabled");
						} else {
							jQuery("#redoChange").addClass("toolDisabled");
						}
					}
				});
			}

			function getRuleSets() {
				var ruleSetArray = [];
				jQuery.each(RuleSet.getRuleSets(), function(index, ruleSet) {
					var ruleSetClone = {};
					jQuery.each(ruleSet, function(i, member) {
						if (typeof member != "function") {
							ruleSetClone[i] = member;	
						}						
					});
					ruleSetArray.push(ruleSetClone);
				});
				
				return ruleSetArray;
			}
			
			function saveRuleSets(deletedOnly,virginOnly) {
				var rsArray=[], /* transformed ruleSets we will post to the server*/
					refRsArray=[]; /* ref to original ruleSets we will operate on within the success callback*/
				
				console.log("SAVE=");
				console.log(this);
				deletedOnly=deletedOnly || false;
				virginOnly=virginOnly || false;
				
				//Convert each RuleSet to its transformed JSON object
				jQuery.each(RuleSet.getRuleSets(),function(){
					
					/* Detect rulesets that need to be deleted from persistant storage
					 * No matter our flags we will always try and delete ruleSets marked
					 * as such.*/
					if(this.state.isDeleted===true && 
					   this.state.isPersisted===true){
						refRsArray.push(this);
						rsArray.push({
							id: this.id,
							uuid: this.uuid,
							name: this.name,
							deleted: true
						});
					}
					
					/* Now, if we arent in a delete only mode, look for ruleSets that need to be persisted.
					 * More exactly..., if in virginOnly mode we will only look for ruleSets that have not been
					 * persisted as of yet and are not marked for deletion, else we look for all dirty ruleSets
					 * not marked for deletion.*/
					if(deletedOnly===false){
						/* Detect only those rulesets that are not persistent and not deleted (virgin).*/
						if(this.state.isPersisted===false && 
								this.state.isDeleted===false){
							refRsArray.push(this);
							rsArray.push(this.toJSON("PRE-DRL"));
						}
						/* Finally, rulesets that are persistent and have changes (are dirty)*/
						if(virginOnly===false){
							if(this.state.isPersisted===true && 
									 this.state.isDirty===true && 
									 this.state.isDeleted===false){
								refRsArray.push(this);
								rsArray.push(this.toJSON("PRE-DRL"));
							}
						}
					}
				});/*End JQUERY.Each*/

				m_communicationController
						.syncPostData(
								{
									url : m_urlUtils.getContextName() + "/services/rest/rules-manager/rules/" + new Date().getTime() + "/rule-sets/design-time/save"
								},
								JSON.stringify(rsArray),
								new function() {
									return {
										success : function(data) {
											
											var failures=[]; /*ruleSets which reported success=false from the server*/
											m_messageDisplay.markSaved();
											
											/*Purge all command history*/
											m_ruleSetCommandDispatcher.commandStack().purgeStacks();
											outline.lastSavedAt=new Date();
											hasUnsavedModifications = false;
											if(jQuery.isArray(data)){
												jQuery.each(data,function(){
													var rsRef;
													if(this.operation==="DELETE"){
														if(this.success===true){
															RuleSet.deleteRuleSet(this.uuid);
														}
														else{
															rsRef=RuleSet.getRuleSets();
															if(rsRef.hasOwnProperty(this.uuid)){
																rsRef=rsRef[this.uuid];
																rsRef.state.isDeleted=false;
																/*push our node back into the tree*/
																createRuleSetNode(this);
																failures.push(this);
															}
														}
													}
													else if(this.operation==="SAVE"){
														rsRef=RuleSet.getRuleSets();
														if(rsRef.hasOwnProperty(this.uuid)){
															rsRef=rsRef[this.uuid];
															if(this.success===true){
																rsRef.state.isPersisted=true;
																rsRef.state.isDirty=false;
															}
															else{
																failures.push(this);
															}
														}
													}
												});
											}

											window.parent.EventHub.events.publish("CONTEXT_UPDATED");

											console.log("FAILURES...");
											console.log(failures);
										},
										error : function(data) {
											JQuery.each(refRsArray,function(){
												if(this.state.isDeleted===true){
													this.state.isDeleted===false;
													createRuleSetNode(this);
												}
											});
											if (parent.iPopupDialog) {
												parent.iPopupDialog
														.openPopup(prepareErrorDialogPoupupData(
																"Error saving rule-sets.",
																"OK"));
											} else {
												alert("Error saving rule-sets.");
											}
										}
									}
								});
			}

			var setupEventHandling = function() {
				var cnstCmd; /*Shorthand accessor for our commandFactory constatns*/
				cnstCmd=m_ruleSetCommand.commands;
				/* Listen to toolbar events */
				jQuery(document).bind('TOOL_CLICKED_EVENT',
						function(event, data) {
							handleToolbarEvents(event, data);
						});

				var IE = document.all ? true : false;
				if (!IE) {
					document.captureEvents(Event.MOUSEMOVE);
				} else {
					document.ondragstart = function() {
						return false;
					};
				}
				document.onmousemove = function(e) {
					// TODO Make portable/modularize

					if (parent != null && parent.iDnD != null) {
						if (e) {
							parent.iDnD.setIframeXY(e, window.name);
						} else {
							parent.iDnD.setIframeXY(window.event, window.name);
						}
					}
				};

				document.onmouseup = function() {
					// TODO Make portable/modularize

					if (parent != null && parent.iDnD != null) {
						parent.iDnD.hideIframe();
					}
				};

				// Tree Node Selection
				var jsOutlineTree=jQuery(displayScope + "#outline");
				
				/*Set up command processing for our jsTree instance*/
				var jsTreeRegisterHooks=[
				                   cnstCmd.decTableRenameCmd,
				                   cnstCmd.decTableDescriptionCmd,
				                   cnstCmd.ruleSetRenameCmd,
				                   cnstCmd.ruleSetDescriptionCmd,
				                   cnstCmd.ruleRenameCmd,
				                   cnstCmd.ruleDescriptionCmd,
				                   cnstCmd.ruleDeleteCmd,
				                   cnstCmd.ruleCreateCmd,
				                   cnstCmd.decTableCreateCmd,
				                   cnstCmd.decTableDeleteCmd];
				m_ruleSetCommandDispatcher.register(jsOutlineTree,jsTreeRegisterHooks);
				
				var renameCmds=[cnstCmd.decTableRenameCmd,cnstCmd.ruleSetRenameCmd,cnstCmd.ruleRenameCmd];
				jsOutlineTree.on(renameCmds.join(" "), function(event,data){
					var elementID=data.elementID;
					var link = m_utils.jQuerySelect("li#" + elementID + " a")[0];
					var node = m_utils.jQuerySelect("li#" + elementID);
					var newVal=data.changes[0].value.after;
					
					if (node.attr("name") != newVal) {
						node.attr("name", newVal);
						var textElem = m_utils.jQuerySelect(link.childNodes[1])[0];
						textElem.nodeValue = newVal;
					}
				});
				
				var descriptionChangeCmds=[cnstCmd.ruleSetDescriptionCmd,
				                           cnstCmd.ruleDescriptionCmd,
				                           cnstCmd.decTableDescriptionCmd];
				
				jsOutlineTree.on(descriptionChangeCmds.join(" "),function(event,data){
					var elementID=data.elementID;
					var link = m_utils.jQuerySelect("li#" + elementID + " a")[0];
					var node = m_utils.jQuerySelect("li#" + elementID);
					var newVal=data.changes[0].value.after;
					
					if (node.attr("title") != newVal) {
						node.attr("title", newVal);
					}
				});
				
				jsOutlineTree.on(cnstCmd.ruleDeleteCmd,function(event,data){
					var elementUUID=data.elementID;
					var ruleSet = RuleSet.findRuleSetByUuid(data.ruleSetUUID,"DESIGN");
					viewManager.closeViewsForElement(data.elementID);
					if(ruleSet.technicalRules.hasOwnProperty(data.elementID)){
						ruleSet.deleteTechnicalRule(data.elementID);
						jsOutlineTree.jstree("delete_node","#"+ data.elementID);
					}
				});
				
				jsOutlineTree.on(cnstCmd.ruleCreateCmd,function(event,data){
					var elementUUID=data.elementID;
					var techRule=data.changes[0].value.after;
					var ruleSet = RuleSet.findRuleSetByUuid(data.ruleSetUUID,"DESIGN");
					
					/*filter duplicates, guranteed to occur as we receive the echo of
					 *our own techrule create events.*/
					if(ruleSet.technicalRules.hasOwnProperty(data.elementID)===false){
						createTechinicalRuleNode(ruleSet,techRule);
						ruleSet.technicalRules[data.elementID]=techRule;						
					}
				});
				
				jsOutlineTree.on(cnstCmd.decTableDeleteCmd,function(event,data){
					var elementUUID=data.elementID;
					var ruleSet = RuleSet.findRuleSetByUuid(data.ruleSetUUID,"DESIGN");
					/*Only delete if we actually have something to delete*/
					if(ruleSet.decisionTables.hasOwnProperty(data.elementID)===true){						
						viewManager.closeViewsForElement(data.elementID);
						ruleSet.deleteDecisionTable(data.elementID);
						jsOutlineTree.jstree("delete_node","#"+ data.elementID);
					}
				});
				
				jsOutlineTree.on(cnstCmd.decTableCreateCmd,function(event,data){
					var elementUUID=data.elementID;
					var decTable=data.changes[0].value.after;
					var ruleSet = RuleSet.findRuleSetByUuid(data.ruleSetUUID,"DESIGN");
					
					/*If the decision table is already present (which it will be in cases
					 * where we subscribe to our own create events, then ignore.)*/
					if(ruleSet.decisionTables.hasOwnProperty(data.elementID)===false){
						createDecisionTableNode(ruleSet, decTable);
						ruleSet.decisionTables[data.elementID]=decTable;						
					}
				});
				
				jsOutlineTree.css("display","none");
				jQuery(displayScope + "#outline")
					.bind("loaded.jstree",function(){
							/* This is a hack to work around the issue where the tree would not display after updating
							 * to jstree1.0.3 and sorting. The tree actually loaded all the nodes, and you can see it flash 
							 * momentarily but then it disappears, thus the reload and css none->block*/
							reloadOutlineTree(false);
							jsOutlineTree.css("display","block");
						})
					.bind(
							"select_node.jstree",
							function(event, data) {
								
								var mode = getCurrentMode();
								
								if (data.rslt.obj.attr('rel') == 'ruleSet') {
									var ruleSet = RuleSet.findRuleSetByUuid(data.rslt.obj.attr("id"),mode);
									viewManager.openView("ruleSetView",
											"id=" + ruleSet.id + 
											"&name=" + ruleSet.name +
											"&uuid=" + ruleSet.uuid +
											"&mode=" + mode,
											ruleSet.uuid);
								}
								else if (data.rslt.obj.attr('rel') == "TechnicalRule") {
									var ruleSet = RuleSet
											.findRuleSetByUuid(data.rslt.obj.attr("ruleSetUuid"),mode);

									var techRule = ruleSet
											.findTechnicalRuleByUuid(data.rslt.obj.attr("id"));

									viewManager.openView("technicalRuleView", 
											"id=" + techRule.id + 
											"&ruleSetId=" + ruleSet.id + 
											"&name=" + techRule.name + 
											"&uuid=" + techRule.uuid + 
											"&ruleSetUuid=" + ruleSet.uuid + 
											"&parentUUID=" + ruleSet.uuid + 
											"&mode=" + mode,
											techRule.uuid);
								} 
								else if (data.rslt.obj.attr('rel') == "DecisionTable") {
									var ruleSet = RuleSet
											.findRuleSetByUuid(data.rslt.obj
													.attr("ruleSetUuid"));

									var decTable = ruleSet
											.findDecisionTableByUuid(data.rslt.obj.attr("id"));

									viewManager.openView("decisionTableView", 
											"id=" + decTable.id + 
											"&ruleSetId=" + ruleSet.id + 
											"&name=" + decTable.name + 
											"&uuid=" + decTable.uuid + 
											"&ruleSetUuid=" + ruleSet.uuid + 
											"&parentUUID=" + ruleSet.uuid + 
											"&mode=" + mode,
											decTable.uuid);
								} 
								else {
									m_utils.debug("No View defined for "
											+ data.rslt.obj.attr('rel'));
								}

								jQuery("a")
										.mousedown(
												function(e) {

													if (jQuery(this)
															.parent()
															.attr(
																	'draggable')) {
														if (e.preventDefault) {
															e
																	.preventDefault();
														}
														var insElem = this.childNodes[0];
														var textElem = jQuery(this.childNodes[1])[0];
														var bgImage = jQuery(
																insElem)
																.css(
																		'background-image');
														bgImage = bgImage
																.substring(
																		4,
																		(bgImage.length - 1));

														// Strip double
														// quotes (for FF
														// and IE)
														bgImage = bgImage
																.replace(
																		/\"/g,
																		"");

														// parent.iDnD.drawIframeAt(e,
														// window.name);
														parent.iDnD
																.setDrag();
														var transferObj = {
															'elementType' : jQuery(
																	insElem)
																	.parent()
																	.parent()
																	.attr(
																			'rel'),
															'elementId' : jQuery(
																	insElem)
																	.parent()
																	.parent()
																	.attr(
																			"elementId"),
															'attr' : {}
														};

														if (transferObj.elementType == "Plain_Java_Application") {
															transferObj.attr.accessPoint = jQuery(
																	insElem)
																	.parent()
																	.parent()
																	.attr(
																			'accessPoint');
														}

														transferObj.attr.fullId = jQuery(
																insElem)
																.parent()
																.parent()
																.attr(
																		"fullId");

														parent.iDnD
																.setTransferObject(transferObj);
														parent.iDnD
																.setImageToDrag(
																		bgImage,
																		textElem.nodeValue);
													}
												});
							})
						.bind("rename_node.jstree", function(event, data) {
							renameNodeHandler(event, data);
						})
						.jstree(
								{
									core : {
										animation : 0
									},
									"plugins" : [ "themes", "html_data",
											"crrm", "contextmenu", "types",
											"ui","sort" ],
									contextmenu : {
										"items" : function(node) {
											var nodeType=node.attr('rel'),
												currentMode = getCurrentMode();
											if(currentMode === "DESIGN"){
												if("DecisionTable"===nodeType || "TechnicalRule"===nodeType){
													return{
														"ccp":false,
														"create":false,										
														"delete":{
															"label":m_i18nUtils.getProperty("rules.outline.ruleSet.contextMenu.delete","Delete"),
															"action": function(obj){
																var techRule,decTable;
																deleteElementAction(obj.context.lastChild.data,
																	function(){
																		var ruleSet = RuleSet.findRuleSetByUuid(obj.attr("ruleSetUuid")),
																		    id=obj.attr("id");
																		if(nodeType==="DecisionTable"){
																			decTable=ruleSet.decisionTables[id];
																			if(decTable){
																				cmd=m_ruleSetCommand.decTableDeleteCmd(ruleSet,decTable,decTable,undefined);
																				m_ruleSetCommandDispatcher.trigger(cmd);
																				ruleSet.deleteDecisionTable(id);
																			}
																		}else if(nodeType==="TechnicalRule"){
																			techRule=ruleSet.technicalRules[id];
																			if(techRule){
																				cmd=m_ruleSetCommand.ruleDeleteCmd(ruleSet,techRule,techRule,undefined);
																				m_ruleSetCommandDispatcher.trigger(cmd);
																				ruleSet.deleteTechnicalRule(id);
																			}
																		}
																		viewManager.closeViewsForElement(obj.attr("id"));
																		jQuery(displayScope + "#outline")
																			.jstree("delete_node","#"+ id);
																	});
															}
														},
														"rename":{
															"label":m_i18nUtils.getProperty("rules.outline.ruleSet.contextMenu.rename","Rename"),
															"action":function(obj){
																/*TODO:Rename Portal Label*/
																/*Rename of View is handled on rename_node event of tree*/
																jQuery(displayScope + "#outline").jstree("rename","#"+ obj.attr("id"));
															}
														}
													}
												}
												if ('ruleSet' == node.attr('rel')) {
													//TODO: Targert DESIGNTIME endpoints
													return {
														"ccp" : false,
														"create" : false,
														"createTechnicalRule" : {
															"label" : m_i18nUtils.getProperty("rules.outline.ruleSet.contextMenu.createRule","Create Rule"),
															"action" : function(obj) {
																var techRule;
																techRule=createTechnicalRule(obj.attr("id"));
																/*set new tree node for user editing*/
																jQuery(displayScope + "#outline").jstree("rename","#" + techRule.uuid);
															}
														},
														"createDecisionTable" : {
															"label" : m_i18nUtils.getProperty("rules.outline.ruleSet.contextMenu.createDecisionTable","Create Decision Table"),
															"action" : function(obj) {
																var decTable;
																decTable=createDecisionTable(obj.attr("id"));
																/*set new tree node for user editing*/
																jQuery(displayScope + "#outline").jstree("rename","#" + decTable.uuid);
																//var inst=jQuery.jstree._reference(displayScope + "#outline");
																//inst.sort(jQuery(displayScope + "#outline"));//asdads//
															}
														},
														"rename" : {
															"label" : m_i18nUtils.getProperty("rules.outline.ruleSet.contextMenu.rename","Rename"),
															"action" : function(obj) {
																jQuery(displayScope + "#outline")
																		.jstree("rename","#"+ obj.attr("id"));
															}
														},
														"deleteRuleSet" : {
															"label" : m_i18nUtils.getProperty("rules.outline.ruleSet.contextMenu.delete","Delete"),
															"action" : function(obj) {
																deleteElementAction(
																		obj.context.lastChild.data,
																		function() {
																			deleteRuleSet(obj.attr("id"));
																			viewManager.closeViewsForElement(obj.attr("id"));
																		});
															}
														},
														"export RuleSet" : {
															"label": m_i18nUtils.getProperty("rules.outline.ruleSet.contextMenu.export","Export Rule Set"),
															"action": function(obj){
																exportRuleSet(obj.attr("id"),"DESIGN")
															},
															"_class" : "ipp-text-red"
														},
														"publish RuleSet" :(function(){
															if(that.supportsRuleSetPublish){
																return {
																		"label": m_i18nUtils.getProperty("rules.outline.ruleSet.contextMenu.publish","Publish Rule Set"),
																		"action": function(obj){
																			publishRuleSet(obj.attr("id"));
																	}
																}
															}
															else{
																return false;
															}
														})()
													};
												}
											}
											else if(currentMode==="PUBLISHED"){
												//TODO: Target RUNTIME endpoints
												if ('ruleSet' == node.attr('rel')) {
													return {
														"deleteRuleSet" : {
															"label" : m_i18nUtils.getProperty("rules.outline.ruleSet.contextMenu.delete","Delete"),
															"action" : function(obj) {
																deleteElementAction(
																		obj.context.lastChild.data,
																		function() {
																			deleteRunTimeRuleSet(obj.attr("id"));
																			viewManager.closeViewsForElement(obj.attr("id"));
																		});
															}
														},
														"export RuleSet" : {
															"label": m_i18nUtils.getProperty("rules.outline.ruleSet.contextMenu.export","Export Rule Set"),
															"action": function(obj){
																exportRuleSet(obj.attr("id"),"PUBLISHED")
															},
															"_class" : "ipp-text-red"
														}
													};//return object ends
												}
											}
											return {};
											
										}//contextMenu items function end
									},
									types : {
										"types" : {
											"ruleSet" : {
												"valid_children" : [ "rule","TechnicalRule","DecisionTable" ]
											},
											"rule" : {
												
											},
											"TechnicalRule" : {
												
											},
											"DecisionTable":{
												
											}
										}
									},
									"themes" : {
										"theme" : "custom",
										"url" : m_urlUtils.getPlugsInRoot() + "rules-manager/css/jsTreeCustom/style.css"
									}
								});
				// "themes" : {
				// "theme" : "default",
				// "url" : "/xhtml/css/jstree"}}).jstree("set_theme",
				// "default");

				var handleToolbarEvents = function(event, data) {
					console.log(data);
					if ("createRuleSet" == data.id) {
						createRuleSet();
					} else if ("importRuleSet" == data.id) {
						importRuleSet();
					} else if ("undoChange" == data.id) {
						var cmd=m_ruleSetCommand.ruleSetUndoCmd();
						m_ruleSetCommandDispatcher.trigger(cmd);
					} else if ("redoChange" == data.id) {	
						var cmd=m_ruleSetCommand.ruleSetRedoCmd();
						m_ruleSetCommandDispatcher.trigger(cmd);
					} else if ("saveAllRules" == data.id) {
						saveRuleSets();
					} else if ("refreshRules" == data.id) {
						refresh();
					}
				};

				/**
				 * 
				 */
				function prepareInfoDialogPoupupData(msg, okText) {
					return {
						attributes : {
							width : "400px",
							height : "200px",
							src : m_urlUtils.getPlugsInRoot()
									+ "bpm-modeler/popups/notificationDialog.html"
						},
						payload : {
							title : "Info",
							message : msg,
							okButtonText : okText
						}
					}
				}

				/**
				 * 
				 */
				function prepareErrorDialogPoupupData(msg, okText) {
					return {
						attributes : {
							width : "400px",
							height : "200px",
							src : m_urlUtils.getPlugsInRoot()
									+ "bpm-modeler/popups/errorDialog.html"
						},
						payload : {
							title : "Error",
							message : msg,
							okButtonText : okText
						}
					}
				}
				
				/* Marks a rulesets state object as isDeleted=true.
				 * calls saveAllRules with the deleteOnly flag set. THis will cause the method to only
				 * send ruleSets we have marked for deletion. The hard delete is performed by the 
				 * success callback intenal to the method.*/
				function deleteRuleSet(ruleSetUUID) {
					//DESIGN TIME DELETE ONLY as this is actally a save (don't ask)
					RuleSet.markRuleSetForDeletion(ruleSetUUID,"DESIGN");
					jQuery(displayScope + "#outline").jstree("delete_node", "#"+ ruleSetUUID);
					saveRuleSets(true); /*deleteOnly=true*/
					window.parent.EventHub.events.publish("CONTEXT_UPDATED");
				};
				
				function deleteRunTimeRuleSet(ruleSetUUID){
					
					var url,
						ruleSet;
					
					if(getCurrentMode() ==="DESIGN"){
						return;
					}
					
					//extract actual ruleSet
					ruleSet = RuleSet.findRuleSetByUuid(ruleSetUUID,"PUBLISHED");
					
					//Delete URL
					url = m_urlUtils.getContextName();
					url += "/services/rest/rules-manager/rules/";
					url += new Date().getTime(); 
					url += "/rule-sets/run-time/" +  encodeURIComponent(ruleSet.id);
					
					jQuery.ajax({
						url: url,
						type: "DELETE",
						success : function(){
							reloadOutlineTree(true,"PUBLISHED");
						},
						error: function(){
							var message = m_i18nUtils.getProperty("rules.messages.delete.error","An error occurred deleting ruleset, {0}.");
							message = message.replace("{0}",ruleSet.name);
							
							var data = {
								"attributes" : {
									"width" : "400px",
									"height" : "200px",
									"src" : m_urlUtils.getPlugsInRoot() + "bpm-modeler/popups/errorDialog.html"
								},
								"payload" : {
									"title" :  m_i18nUtils.getProperty("rules.messages.warning","Warning"),
									"message" :  message,
									"okButtonText" : m_i18nUtils.getProperty("rules.messages.confirm.close","Close")
								}
							};
							
							parent.iPopupDialog.openPopup(data);
						}
					});
				};
				
				function prepareDeleteElementData(name, callback) {
					var popupData = {
						attributes : {
							width : "400px",
							height : "200px",
							src : m_urlUtils.getPlugsInRoot()
									+ "bpm-modeler/popups/confirmationPopupDialogContent.html"
						},
						payload : {
						   title : m_i18nUtils.getProperty("rules.messages.confirm"),
						   message : m_i18nUtils.getProperty(
						            "rules.messages.confirm.deleteElement")
						            .replace("{0}", name),
						   acceptButtonText : m_i18nUtils.getProperty("rules.messages.confirm.yes"),
						   cancelButtonText : m_i18nUtils.getProperty("rules.messages.confirm.cancel"),
						   acceptFunction : callback
						}
					};

					return popupData;
				}

				function deleteElementAction(name, callback) {
					if (parent.iPopupDialog) {
						parent.iPopupDialog.openPopup(prepareDeleteElementData(
								name, callback));
					} else {
						callback();
					}
				}
				
				function createDecisionTable(ruleSetUuid) {
					var ruleSet = RuleSet.findRuleSetByUuid(ruleSetUuid);
					var	decTableCount=1 + ruleSet.getDecisionTableCount();
					var decName =m_i18nUtils.getProperty("rules.object.decisiontable.name","Decision Table");
					var	name =decName + " " + decTableCount;
					var	id=decName.replace(/\s/g,"") + decTableCount;
					var decTable=ruleSet.addDecisionTable(id,name);
					var cmd;
					
					cmd=m_ruleSetCommand.decTableCreateCmd(ruleSet,decTable,decTable,undefined);
					m_ruleSetCommandDispatcher.trigger(cmd);
					
					createDecisionTableNode(ruleSet, decTable);
					
					viewManager.openView("decisionTableView", 
							"id=" + decTable.id + 
							"&ruleSetId=" + ruleSet.id + 
							"&name=" + decTable.name + 
							"&uuid=" + decTable.uuid + 
							"&ruleSetUuid=" + ruleSet.uuid + 
							"&parentUUID=" + ruleSet.uuid + 
							"&mode=DESIGN",
							decTable.uuid);
					return decTable;
				}
				
				function createTechnicalRule(ruleSetUuid) {
					var ruleSet = RuleSet.findRuleSetByUuid(ruleSetUuid);
					var	techRuleCount=1+ ruleSet.getTechnicalRuleCount();
					var trName=m_i18nUtils.getProperty("rules.object.technicalrule.name","Rule");
					var	name =trName + " " + techRuleCount;
					var	id=trName.replace(/\s/g,"") + techRuleCount;
					var techRule=ruleSet.addTechnicalRule(id,name);
					var cmd;
					
					cmd=m_ruleSetCommand.ruleCreateCmd(ruleSet,techRule,techRule,undefined);
					m_ruleSetCommandDispatcher.trigger(cmd);
					createTechinicalRuleNode(ruleSet, techRule);
					console.log("creating technical rule");
					viewManager.openView("technicalRuleView", 
							"id=" + techRule.id + 
							"&ruleSetId=" + ruleSet.id + 
							"&name=" + techRule.name + 
							"&uuid=" + techRule.uuid + 
							"&ruleSetUuid=" + ruleSet.uuid + 
							"&parentUUID=" + ruleSet.uuid +
							"&mode=DESIGN",
							techRule.uuid);
					
					return techRule;
				}
				
				function changeProfileHandler(profile) {
					m_user.setCurrentRole(profile);
					m_commandsController.broadcastCommand(m_command
							.createUserProfileChangeCommand(profile));
				}

				if (window.parent.EventHub != null) {
					window.parent.EventHub.events.subscribe("CHANGE_PROFILE",
							changeProfileHandler);
					window.parent.EventHub.events.subscribe("RELOAD_RULES",
							reloadOutlineTreeReset);

					window.parent.EventHub.events.subscribe("CONTEXT_UPDATED", function(releaseId) {
						if (releaseId != undefined) {
							reloadOutlineTreeReset();
						}
					});
				}

				readAllRuleSets();
			};

			/*
			 * 
			 */
			var loadCustomTheme = function () {
				jQuery.ajax({
					type : 'GET',
					url : m_urlUtils.getContextName() + "/services/rest/common/html5/api/themes/current/custom",
					async : true
				}).done(function(json){
					var body = document.getElementsByTagName('body')[0];
					
					for(var i in json.stylesheets) {
						var link = document.createElement('link');
						link.href = m_urlUtils.getContextName() + "/" + json.stylesheets[i];
						link.rel = 'stylesheet';
						link.type = 'text/css';
						body.appendChild(link);
					}
				}).fail(function(err){
					m_utils.debug("Failed in loading custom theme");
				});
			};

			var outline;

			return {
				init : function(newViewManager, newDisplayScope,options) {
					var uiElements={
							createRuleSetButton:  m_utils.jQuerySelect(options.selectors.createRuleSetButton),
							importRuleSet:  m_utils.jQuerySelect(options.selectors.importRuleSet),
							undoChange:  m_utils.jQuerySelect(options.selectors.undoChange),
							redoChange:  m_utils.jQuerySelect(options.selectors.redoChange),
							saveAllRules:  m_utils.jQuerySelect(options.selectors.saveAllRules),
							refreshRules:  m_utils.jQuerySelect(options.selectors.refreshRules),
							lastSavelLabel:  m_utils.jQuerySelect(options.selectors.lastsave),
							lastSaveDateDisplay: m_utils.jQuerySelect(options.selectors.lastSaveDateDisplay)
					};
					var defaultText, /*default text for our lastSaveDateDisplay label*/
						lessThanText,/*text to display when last save < 1 minute*/
						singularText,/*text to display when last save =1 minute*/
						pluralText;  /*text to disaply when more than 1 minute*/
					
					defaultText=m_i18nUtils.getProperty("rules.outline.labels.lastSavedAt.default");
					lessThanText=m_i18nUtils.getProperty("rules.outline.labels.lastSavedAt.suffix.lessThan");
					singularText=m_i18nUtils.getProperty("rules.outline.labels.lastSavedAt.suffix.singular");
					pluralText=m_i18nUtils.getProperty("rules.outline.labels.lastSavedAt.suffix.plural");
					
					/*map our UI elements to their values from our resource file*/
					m_i18nMapper.map(options,uiElements,true);
					
					/*Default text for last saved, exists outside mapper as this will get updated in timer*/
					uiElements.lastSaveDateDisplay.text(defaultText);
					
					/* Register redoImage button for messages from the commandStack in the sky
					 * that the pointer on that stack has moved. When it does, we need to update
					 * our tooltip text to indicate the next function we will be performing if the
					 * user clicks the redo Image button.*/
					m_ruleSetCommandDispatcher.register(uiElements.redoChange,"CommandStack.Change.Stacks");
					uiElements.redoChange.on("CommandStack.Change.Stacks",function(event,data){
						console.log("Stack change event received.");
						var title;
						var nextCmd = data.changes[0].value.after;
						var prevCmd=data.changes[0].value.before;
						if(nextCmd){
							uiElements.redoChange.removeClass("toolDisabled");
							title=nextCmd.description;
							uiElements.redoChange.attr("title", m_i18nUtils.getProperty(
							   "rules.outline.toolbar.tooltip.afterChange.redo") + ": " + title);
						}
						else{
							uiElements.redoChange.addClass("toolDisabled");
							uiElements.redoChange.attr("title", m_i18nUtils.getProperty(
							   "rules.outline.toolbar.tooltip.afterChange.redo"));
						}
						if(prevCmd){
							uiElements.undoChange.removeClass("toolDisabled");
							title=prevCmd.description;
							uiElements.undoChange.attr("title", m_i18nUtils.getProperty(
							   "rules.outline.toolbar.tooltip.afterChange.undo") + ": " + title);
						}
						else{
							uiElements.undoChange.addClass("toolDisabled");
							uiElements.undoChange.attr("title", m_i18nUtils.getProperty(
							   "rules.outline.toolbar.tooltip.afterChange.undo"));
						}
					});
					
					
					if (newDisplayScope) {
						displayScope = "#" + newDisplayScope + " ";
					}

					if (newViewManager != null) {
						viewManager = newViewManager;
					} else {
						viewManager = m_jsfViewManager.create();
					}

					setupEventHandling();

					outline = new Outline();

					outline.initialize();
					
					/*Set up a timer to check for delta between now and our last save event
					 *and update our UI accordingly. Timer runs every 60 seconds. May get a bit out
					 *of sync as save events to not reset our interval just our last saved timestamp.*/
					setInterval(function(){
						var now,dateDelta;
						/*outline var avaialble via closure*/
						if(outline.lastSavedAt===Number.NEGATIVE_INFINITY){
							uiElements.lastSaveDateDisplay.text(defaultText);
						}
						else{
							now=new Date();
							dateDelta=Math.abs(now-outline.lastSavedAt);/*diff in milliseconds*/
							dateDelta=Math.floor((dateDelta/1000)/60); /*convert to minutes*/
							if(dateDelta===0){
								uiElements.lastSaveDateDisplay.text(lessThanText);
							}
							else if(dateDelta===1){
								uiElements.lastSaveDateDisplay.text(dateDelta + " " + singularText);
							}
							else{
								uiElements.lastSaveDateDisplay.text(dateDelta + " " + pluralText);
							}	
						}
					},30000);

					m_outlineToolbarController.init("rulesOutlineToolbar");
					
					return outline;
				},
				refresh : function() {
					refresh();
				}
			};

			/**
			 * 
			 */
			function Outline() {
				/**
				 * 
				 */
				Outline.prototype.toString = function() {
					return "rules-manager.Outline";
				};

				/**
				 * 
				 */
				Outline.prototype.initialize = function() {
					var selectRuleMode;
					CommandsDispatcher.registerCommandHandler(this);
					
					/*Load options for Design/Publish mode select box*/
					selectRuleMode = document.getElementById('selectRuleMode');
					selectRuleMode.options[0]= new Option(m_i18nUtils.getProperty("rules.outline.mode.options.design","Design"), 'DESIGN');
					selectRuleMode.options[1]= new Option(m_i18nUtils.getProperty("rules.outline.mode.options.published","Published"), 'PUBLISHED');
					
					/*Add our change event listener*/
					selectRuleMode.addEventListener("change",function(e){
						var selectedOption = this.options[this.selectedIndex];
						$("#rulesOutlineToolbar").toggle();
						$("#lastSaveStatusPanel").toggle();
			            reloadOutlineTree(true,selectedOption.value);
					});
					
					/*quick initialization of ruleSet dispatcher if null.*/
					m_ruleSetCommandDispatcher.register("Bob","something");
					
					
					this.createRuleSetButton = jQuery("#createRuleSetButton");
					
					/*On saves update this value*/
					this.lastSavedAt=Number.NEGATIVE_INFINITY;
					
					this.createRuleSetButton.click({
						outline : this
					}, function(event) {
						event.data.outline.createRuleSet();
						m_ruleSetCommandDispatcher.trigger({name: "RuleSet.command"});
					});
				};
				
				
				/**
				 * 
				 */
				Outline.prototype.createRuleSet = function() {
					
					var rsName = m_i18nUtils.getProperty("rules.object.ruleset.name","Rule Set");
					var name = rsName + " " + RuleSet.getNextRuleSetNamePostfix("DESIGN");
					var id = name.replace(/\s/g,"");
					var ruleSet = RuleSet.create(id, name);

					createRuleSetNode(ruleSet);
					viewManager.openView("ruleSetView",
							"id=" + ruleSet.id + 
							"&name=" + ruleSet.name +
							"&uuid=" + ruleSet.uuid +
							"&mode=DESIGN",
							ruleSet.uuid);
					jQuery(displayScope + "#outline").jstree("rename","#" + ruleSet.uuid);
					/*save our virgin Rule Set to the server.*/
					saveRuleSets(false,true);
					
					window.parent.EventHub.events.publish("CONTEXT_UPDATED");
				}
			}
		});
