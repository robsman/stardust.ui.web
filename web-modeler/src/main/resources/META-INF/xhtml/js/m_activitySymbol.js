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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_extensionManager",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_canvasManager",
				"bpm-modeler/js/m_symbol", "bpm-modeler/js/m_gatewaySymbol",
				"bpm-modeler/js/m_session", "bpm-modeler/js/m_eventSymbol",
				"bpm-modeler/js/m_activityPropertiesPanel",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_activity",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_modelerUtils" ],
		function(m_utils, m_constants, m_extensionManager, m_command,
				m_canvasManager, m_symbol, m_gatewaySymbol, m_eventSymbol,
				m_session, m_activityPropertiesPanel, m_model, m_activity,
				m_commandsController, m_command, m_modelerUtils) {

			return {
				createActivitySymbol : function(diagram, type) {
					var activitySymbol = new ActivitySymbol();

					activitySymbol.bind(diagram);

					activitySymbol.modelElement = m_activity.createActivity(
							diagram.process, type);

					return activitySymbol;
				},

				createActivitySymbolFromProcess : function(diagram, process) {
					var activitySymbol = new ActivitySymbol();

					activitySymbol.activateEditOnCreation = false;

					activitySymbol.bind(diagram);

					activitySymbol.modelElement = m_activity
							.createActivityFromProcess(diagram.process, process);

					return activitySymbol;
				},

				createActivitySymbolFromApplication : function(diagram,
						application) {
					var activitySymbol = new ActivitySymbol();

					activitySymbol.activateEditOnCreation = false;

					activitySymbol.bind(diagram);

					activitySymbol.modelElement = m_activity
							.createActivityFromApplication(diagram.process,
									application);

					return activitySymbol;
				},

				createActivitySymbolFromJson : function(diagram, lane, json) {
					// TODO Ugly
					m_utils.inheritFields(json, m_symbol.createSymbol());
					m_utils.inheritMethods(json, new ActivitySymbol());

					json.bind(diagram);
					json.initializeFromJson(lane);

					return json;
				}
			};

			/**
			 *
			 */
			function ActivitySymbol() {
				var symbol = m_symbol.createSymbol();

				m_utils.inheritFields(this, symbol);
				m_utils.inheritMethods(ActivitySymbol.prototype, symbol);

				this.x = 0;
				this.y = 0;
				this.width = m_constants.ACTIVITY_SYMBOL_DEFAULT_WIDTH;
				this.height = m_constants.ACTIVITY_SYMBOL_DEFAULT_HEIGHT;
				this.modelElement = null;
				this.boundaryEventSymbols = [];

				/**
				 * Binds all client-side aspects to the object (graphics
				 * objects, diagram, base classes).
				 */
				ActivitySymbol.prototype.bind = function(diagram) {
					this.type = m_constants.ACTIVITY_SYMBOL;

					this.diagram = diagram;

					this.diagram.lastSymbol = this;

					this.propertiesPanel = m_activityPropertiesPanel
							.getInstance();

					this.rectangle = null;
					this.text = null;
					this.commentCountText = null;
					this.parallelMultiProcessingMarkerIcon = null;
					this.sequentialMultiProcessingMarkerIcon = null;
					this.subprocessMarkerIcon = null;
					this.commentCountIcon = null;

					var viewManagerExtension = m_extensionManager
							.findExtension("viewManager");

					this.viewManager = viewManagerExtension.provider.create();
				};

				/**
				 *
				 */
				ActivitySymbol.prototype.toString = function() {
					return "Lightdust.ActivitySymbol";
				};

				/**
				 *
				 */
				ActivitySymbol.prototype.initializeFromJson = function(lane) {
					m_activity.typeObject(this.modelElement);
					this.parentSymbol = lane;
					this.parentSymbolId = lane.id;
					this.parentSymbol.containedSymbols.push(this);
					this.boundaryEventSymbols = [];
					this.prepareNoPosition();
					this.completeNoTransfer();
					this.register();
				};

				/**
				 *
				 */
				ActivitySymbol.prototype.createTransferObject = function() {
					var transferObject = {};

					m_utils.inheritFields(transferObject, this);

					transferObject = this.prepareTransferObject(transferObject);
					transferObject.rectangle = null;
					transferObject.text = null;
					transferObject.manualTaskIcon = null;
					transferObject.receiveTaskIcon = null;
					transferObject.ruleTaskIcon = null;
					transferObject.scriptTaskIcon = null;
					transferObject.sendTaskIcon = null;
					transferObject.serviceTaskIcon = null;
					transferObject.userTaskIcon = null;
					transferObject.parallelMultiProcessingMarkerIcon = null;
					transferObject.sequentialMultiProcessingMarkerIcon = null;
					transferObject.subprocessMarkerIcon = null;
					transferObject.boundaryEvents = null;
					transferObject.viewManager = null;

					return transferObject;
				};

				/**
				 *
				 */
				ActivitySymbol.prototype.getPath = function(withId) {
					var path = "/models/" + this.diagram.model.id
							+ "/processes/" + this.diagram.process.id
							+ "/activities";

					if (withId) {
						path += "/" + this.modelElement.id;
					}

					return path;
				};

				/**
				 *
				 */
				ActivitySymbol.prototype.createPrimitives = function() {
					this.rectangle = m_canvasManager
							.drawRectangle(
									this.x,
									this.y,
									this.width,
									this.height,
									{
										'fill' : m_constants.ACTIVITY_SYMBOL_DEFAULT_FILL_COLOR,
										'fill-opacity' : m_constants.ACTIVITY_SYMBOL_DEFAULT_FILL_OPACITY,
										'stroke' : m_constants.DEFAULT_STROKE_COLOR,
										'stroke-width' : m_constants.ACTIVITY_SYMBOL_DEFAULT_STROKE_WIDTH,
										'r' : 4
									});

					this.addToPrimitives(this.rectangle);
					this.addToEditableTextPrimitives(this.rectangle);

					this.text = m_canvasManager.drawTextNode(
							this.x + 0.5 * this.width,
							this.y + 0.5 * this.height, "").attr({
						"text-anchor" : "middle",
						"font-family" : m_constants.DEFAULT_FONT_FAMILY,
						"font-size" : m_constants.DEFAULT_FONT_SIZE
					});

					this.addToPrimitives(this.text);
					this.addToEditableTextPrimitives(this.text);

					this.manualTaskIcon = m_canvasManager.drawImageAt(
							"plugins/bpm-modeler/images/icons/manual-task.png", this.x + 5,
							this.y + 5, 18, 13).hide();

					this.addToPrimitives(this.manualTaskIcon);

					this.receiveTaskIcon = m_canvasManager.drawImageAt(
							"plugins/bpm-modeler/images/icons/receive-task.png", this.x + 5,
							this.y + 5, 18, 14).hide();

					this.addToPrimitives(this.receiveTaskIcon);

					this.ruleTaskIcon = m_canvasManager.drawImageAt(
							"plugins/bpm-modeler/images/icons/rule-task.png", this.x + 5,
							this.y + 5, 18, 14).hide();

					this.addToPrimitives(this.ruleTaskIcon);

					this.scriptTaskIcon = m_canvasManager.drawImageAt(
							"plugins/bpm-modeler/images/icons/script-task.png", this.x + 5,
							this.y + 5, 18, 18).hide();

					this.addToPrimitives(this.scriptTaskIcon);

					this.sendTaskIcon = m_canvasManager.drawImageAt(
							"plugins/bpm-modeler/images/icons/send-task.png", this.x + 5,
							this.y + 5, 18, 14).hide();

					this.addToPrimitives(this.sendTaskIcon);

					this.serviceTaskIcon = m_canvasManager.drawImageAt(
							"plugins/bpm-modeler/images/icons/service-task.png", this.x + 5,
							this.y + 5, 18, 18).hide();

					this.addToPrimitives(this.serviceTaskIcon);

					this.userTaskIcon = m_canvasManager.drawImageAt(
							"plugins/bpm-modeler/images/icons/user-task.png", this.x + 5,
							this.y + 5, 16, 18).hide();

					this.addToPrimitives(this.userTaskIcon);

					this.parallelMultiProcessingMarkerIcon = m_canvasManager
							.drawImageAt(
									"plugins/bpm-modeler/images/icons/parallel-marker.gif",
									this.x + 0.5 * this.width - 4, this.y + 2,
									16, 16).hide();

					this
							.addToPrimitives(this.parallelMultiProcessingMarkerIcon);

					this.sequentialMultiProcessingMarkerIcon = m_canvasManager
							.drawImageAt(
									"plugins/bpm-modeler/images/icons/sequential-marker.gif",
									this.x + 0.5 * this.width - 4, this.y + 2,
									16, 16).hide();

					this
							.addToPrimitives(this.sequentialMultiProcessingMarkerIcon);

					this.subprocessMarkerIcon = m_canvasManager.drawImageAt(
							"plugins/bpm-modeler/images/icons/subprocess-marker.gif",
							this.x + 0.5 * this.width - 8,
							this.y + this.height - 16, 16, 16).hide();

					this.addToPrimitives(this.subprocessMarkerIcon);
				};

				/**
				 *
				 */
				ActivitySymbol.prototype.initializeEventHandling = function() {
					this.subprocessMarkerIcon
							.mousemove(ActivitySymbol_subprocessMarkerIconMouseMoveClosure);
					this.subprocessMarkerIcon
							.click(ActivitySymbol_subprocessMarkerIconClickClosure);
				};

				/**
				 * Registers symbol in specific lists in the diagram and model
				 * element in the process.
				 */
				ActivitySymbol.prototype.register = function() {
					this.diagram.activitySymbols[this.oid] = this;
					this.diagram.process.activities[this.modelElement.id] = this.modelElement;
				};

				/**
				 *
				 */
				// ActivitySymbol.prototype.showPrimitives = function() {
				// this.rectangle.show();
				// this.text.show();
				// this.refreshFromModelElement();
				// };
				/**
				 *
				 */
				ActivitySymbol.prototype.refreshFromModelElement = function() {
					this.text.attr("text", this.modelElement.name);
					this.subprocessMarkerIcon.hide();
					this.manualTaskIcon.hide();
					this.receiveTaskIcon.hide();
					this.ruleTaskIcon.hide();
					this.scriptTaskIcon.hide();
					this.sendTaskIcon.hide();
					this.serviceTaskIcon.hide();
					this.userTaskIcon.hide();
					this.sequentialMultiProcessingMarkerIcon.hide();
					this.parallelMultiProcessingMarkerIcon.hide();

					if (this.modelElement.activityType == m_constants.SUBPROCESS_ACTIVITY_TYPE) {
						this.subprocessMarkerIcon.show();
						this.subprocessMarkerIcon.toFront();
					}else if (this.modelElement.taskType == m_constants.MANUAL_TASK_TYPE) {
						this.manualTaskIcon.show();
						this.manualTaskIcon.toFront();
					} else if (this.modelElement.taskType == m_constants.RECEIVE_TASK_TYPE) {
						this.receiveTaskIcon.show();
						this.receiveTaskIcon.toFront();
					} else if (this.modelElement.taskType == m_constants.RULE_TASK_TYPE) {
						this.ruleTaskIcon.show();
						this.ruleTaskIcon.toFront();
					} else if (this.modelElement.taskType == m_constants.SCRIPT_TASK_TYPE) {
						this.scriptTaskIcon.show();
						this.scriptTaskIcon.toFront();
					} else if (this.modelElement.taskType == m_constants.SEND_TASK_TYPE) {
						this.sendTaskIcon.show();
						this.sendTaskIcon.toFront();
					} else if (this.modelElement.taskType == m_constants.SERVICE_TASK_TYPE) {
						this.serviceTaskIcon.show();
						this.serviceTaskIcon.toFront();
					} else if (this.modelElement.taskType == m_constants.USER_TASK_TYPE) {
						this.userTaskIcon.show();
						this.userTaskIcon.toFront();
					}

					if (this.modelElement.processingType == m_constants.SINGLE_PROCESSING_TYPE) {
						this.parallelMultiProcessingMarkerIcon.hide();
						this.sequentialMultiProcessingMarkerIcon.hide();
					} else if (this.modelElement.processingType == m_constants.PARALLEL_MULTI_PROCESSING_TYPE) {
						this.parallelMultiProcessingMarkerIcon.show();
						this.sequentialMultiProcessingMarkerIcon.hide();
					} else if (this.modelElement.processingType == m_constants.SEQUENTIAL_MULTI_PROCESSING_TYPE) {
						this.parallelMultiProcessingMarkerIcon.hide();
						this.sequentialMultiProcessingMarkerIcon.show();
					}

				};

				/**
				 *
				 */
				ActivitySymbol.prototype.adjustPrimitives = function() {
					this.hideGlow();
					this.rectangle.animate({
						"x" : this.x,
						"y" : this.y,
						"width" : this.width,
						"height" : this.height,
						"callback" : ActivitySymbol_updateGlow
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);

					this.manualTaskIcon.animate({
						"x" : this.x + 5,
						"y" : this.y + 5
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
					this.manualTaskIcon.animate({
						"x" : this.x + 5,
						"y" : this.y + 5
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
					this.receiveTaskIcon.animate({
						"x" : this.x + 5,
						"y" : this.y + 5
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
					this.ruleTaskIcon.animate({
						"x" : this.x + 5,
						"y" : this.y + 5
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
					this.scriptTaskIcon.animate({
						"x" : this.x + 5,
						"y" : this.y + 5
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
					this.sendTaskIcon.animate({
						"x" : this.x + 5,
						"y" : this.y + 5
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
					this.serviceTaskIcon.animate({
						"x" : this.x + 5,
						"y" : this.y + 5
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
					this.userTaskIcon.animate({
						"x" : this.x + 5,
						"y" : this.y + 5
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
					this.text.animate({
						"x" : this.x + 0.5 * this.width,
						"y" : this.y + 0.5 * this.height
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
					this.parallelMultiProcessingMarkerIcon.animate({
						"x" : this.x + 0.5 * this.width - 4,
						"y" : this.y + 2
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
					this.sequentialMultiProcessingMarkerIcon.animate({
						"x" : this.x + 0.5 * this.width - 4,
						"y" : this.y + 2
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
					this.subprocessMarkerIcon.animate({
						"x" : this.x + 0.5 * this.width - 8,
						"y" : this.y + this.height - 16
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);

					this.adjustPrimitivesOnShrink();
					// this.rectangle.attr({
					// "x" : this.x,
					// "y" : this.y,
					// "width" : this.width,
					// "height" : this.height
					// });
					// this.manualTaskIcon.attr({
					// "x" : this.x + 5,
					// "y" : this.y + 5
					// });
					// this.text.attr({
					// "x" : this.x + 0.5 * this.width,
					// "y" : this.y + 0.5 * this.height
					// });
					//
					// this.parallelMultiProcessingMarkerIcon.attr({
					// "x" : this.x + 0.5 * this.width - 4,
					// "y" : this.y + 2
					// });
					//
					// this.sequentialMultiProcessingMarkerIcon.attr({
					// "x" : this.x + 0.5 * this.width - 4,
					// "y" : this.y + 2
					// });
					//
					// this.subprocessMarkerIcon.attr({
					// "x" : this.x + 0.5 * this.width - 4,
					// "y" : this.y + this.height - 16
					// });

				};

				/**
				 * Hides the icon and shrinks the activity label when activity
				 * size decreases
				 */
				ActivitySymbol.prototype.adjustPrimitivesOnShrink = function() {
					if (this.parentSymbol && this.parentSymbol.minimized) {
						return;
					}
					if (this.text.getBBox().width > this.width) {
						var words = this.text.attr("text");
						m_utils.textWrap(this.text, this.width);
					}

					// TODO: Add back once shrink is supported again
					// if (this.icon.getBBox().width > this.width) {
					// this.icon.hide();
					// } else {
					// this.icon.show();
					// }
				}

				/**
				 *
				 */
				ActivitySymbol.prototype.createFlyOutMenu = function() {
					this
							.addFlyOutMenuItems(
									[],
									[
											{
												imageUrl : "plugins/bpm-modeler/images/icons/connect.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : ActivitySymbol_connectToClosure
											},
											{
												imageUrl : "plugins/bpm-modeler/images/icons/activity.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : ActivitySymbol_connectToActivityClosure
											},
											{
												imageUrl : "plugins/bpm-modeler/images/icons/gateway.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : ActivitySymbol_connectToGatewayClosure
											},
											{
												imageUrl : "plugins/bpm-modeler/images/icons/end-event-toolbar.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : ActivitySymbol_connectToEndEventClosure
											} ],
									[
											{
												imageUrl : "plugins/bpm-modeler/images/icons/delete.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : ActivitySymbol_removeClosure
											},
											/*{
												imageUrl : "plugins/bpm-modeler/images/icons/activity-subprocess.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : ActivitySymbol_switchToSubprocessActivityClosure
											},
											{
												imageUrl : "plugins/bpm-modeler/images/icons/activity-manual.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : ActivitySymbol_switchToManualActivityClosure
											}*/ ]);
				};

				/**
				 *
				 */
				ActivitySymbol.prototype.highlight = function() {
					this.rectangle.attr({
						"stroke" : m_constants.SELECT_STROKE_COLOR
					});
				};

				/**
				 *
				 */
				ActivitySymbol.prototype.dehighlight = function() {
					this.rectangle.attr({
						"stroke" : m_constants.DEFAULT_STROKE_COLOR
					});
				};

				/**
				 *
				 */
				ActivitySymbol.prototype.switchToSubprocessActivity = function() {
					if (this.modelElement.activityType != m_constants.SUBPROCESS_ACTIVITY_TYPE) {
						this.modelElement.activityType = m_constants.SUBPROCESS_ACTIVITY_TYPE;
						this.modelElement.taskType = null;

						this.submitChanges();
					}
				};

				/**
				 *
				 */
				ActivitySymbol.prototype.switchToManualActivity = function() {
					if (this.modelElement.taskType != m_constants.MANUAL_TASK_TYPE) {
						this.modelElement.activityType = m_constants.TASK_ACTIVITY_TYPE;
						this.modelElement.taskType = m_constants.MANUAL_TASK_TYPE;

						this.submitChanges();
					}
				};

				/**
				 *
				 */
				ActivitySymbol.prototype.onComplete = function() {
					this.onParentSymbolChange();
				};

				/*
				 *
				 */
				ActivitySymbol.prototype.onParentSymbolChange = function() {
					this.modelElement.participantFullId = this.parentSymbol.participantFullId;
				};

				/**
				 *
				 */
				ActivitySymbol.prototype.onSubprocessMarkerIconMouseMove = function() {
					this.showPointerCursor();
				};

				/**
				 *
				 */
				ActivitySymbol.prototype.onSubprocessMarkerIconClick = function() {
					var model = m_model.findModel(m_model
							.stripModelId(this.modelElement.subprocessFullId));
					var process = m_model
							.findProcess(this.modelElement.subprocessFullId);

					this.viewManager.openView("processDefinitionView",
							"processId=" + process.id + "&modelId=" + model.id
									+ "&processName=" + process.name
									+ "&fullId=" + process.getFullId(), process
									.getFullId());
				};

				/**
				 * Update the modelElement
				 */
				ActivitySymbol.prototype.submitChanges = function() {
					var changes = {
						activityType : this.modelElement.activityType
					};
					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(
									this.diagram.modelId,
									this.modelElement.oid, changes));
				};
				/**
				 *
				 */
				ActivitySymbol.prototype.validateCreateConnection = function(
						conn) {
					var outMappingActivity = new Array();
					var inMappingActivity = new Array();
					var dataMapping = {};
					for ( var n in this.connections) {
						var connection = this.connections[n];
						if (connection.fromAnchorPoint.symbol.type == m_constants.ACTIVITY_SYMBOL
								&& connection.fromAnchorPoint.symbol.oid == this.oid) {
							if ((null != connection.toAnchorPoint && null != connection.toAnchorPoint.symbol)
									&& connection.toAnchorPoint.symbol.type == m_constants.DATA_SYMBOL) {
								// verify duplicate Data mapping
								if (connection.toAnchorPoint.symbol.modelElement
										&& connection.toAnchorPoint.symbol.modelElement.id in dataMapping) {
									if (dataMapping[conn.toAnchorPoint.symbol.modelElement.id] != connection.toAnchorPoint.symbol.oid) {
										return false;
									}
								} else {
									dataMapping[connection.toAnchorPoint.symbol.modelElement.id] = connection.toAnchorPoint.symbol.oid;
								}
							} else {
								if (-1 != jQuery.inArray(
										connection.fromAnchorPoint.symbol.oid,
										outMappingActivity)) {
									return false;
								} else
									outMappingActivity
											.push(connection.fromAnchorPoint.symbol.oid);
							}
						} else if (null != connection.toAnchorPoint
								&& null != connection.toAnchorPoint.symbol) {
							if (connection.fromAnchorPoint.symbol.type == m_constants.EVENT_SYMBOL) {
								// do nothing
							} else if (connection.fromAnchorPoint.symbol.type == m_constants.DATA_SYMBOL) {
								// verify duplicate Data mapping
								if (connection.toAnchorPoint.symbol.modelElement
										&& connection.fromAnchorPoint.symbol.modelElement.id in dataMapping) {
									if (dataMapping[connection.fromAnchorPoint.symbol.modelElement.id] != connection.fromAnchorPoint.symbol.oid) {
										return false;
									}
								} else {
									dataMapping[connection.fromAnchorPoint.symbol.modelElement.id] = connection.fromAnchorPoint.symbol.oid;
								}
							} else if (connection.toAnchorPoint.symbol.oid == this.oid) {
								if (-1 != jQuery.inArray(
										connection.toAnchorPoint.symbol.oid,
										inMappingActivity)) {
									return false;
								} else
									inMappingActivity
											.push(connection.toAnchorPoint.symbol.oid);
							}
						}
					}
					// When rerouting happens, connection is not present in
					// this.connections, check the validation rules with symbol
					// connections list
					if (conn != null && conn.oid > 0) {
						if (-1 == jQuery.inArray(conn, this.connections)) {
							if (conn.fromAnchorPoint
									&& conn.fromAnchorPoint.symbol) {
								if (this.oid == conn.fromAnchorPoint.symbol.oid) {
									return (-1 == jQuery.inArray(
											conn.fromAnchorPoint.symbol.oid,
											outMappingActivity))
								}
							}
							if (conn.toAnchorPoint && conn.toAnchorPoint.symbol) {
								if (this.oid == conn.toAnchorPoint.symbol.oid) {
									return (-1 == jQuery.inArray(
											conn.toAnchorPoint.symbol.oid,
											inMappingActivity))
								}
							}
						}
					}
					return true;
				};

				/**
				 *
				 */
				ActivitySymbol.prototype.postMove = function(x, y, originalX,
						originalY) {
					for ( var n = 0; n < this.boundaryEventSymbols.length; ++n) {
						this.boundaryEventSymbols[n].moveBy(x - originalX, y
								- originalY);
						this.boundaryEventSymbols[n].toFront();
					}
				};

				/**
				 *
				 */
				ActivitySymbol.prototype.addBoundaryEvent = function(
						eventSymbol) {

					if (!m_utils.isItemInArray(this.boundaryEventSymbols,
							eventSymbol)) {
						this.boundaryEventSymbols.push(eventSymbol);

						eventSymbol.bindingActivitySymbol = this;

						eventSymbol.modelElement
								.bindWithActivity(this.modelElement);
					}

					// Align all boundary events on the symbol boundary
					this.realignBoundaryEvent();

				};

				ActivitySymbol.prototype.realignBoundaryEvent = function(){
					var x = this.x + this.width;

					var eventSymbol;
					for ( var i = 0; i < this.boundaryEventSymbols.length; ++i) {
						x -= m_constants.ACTIVITY_BOUNDARY_EVENT_OFFSET;
						eventSymbol = this.boundaryEventSymbols[i];

						eventSymbol.moveTo(x - 0.5 * eventSymbol.width, this.y
								+ this.height - 0.5 * eventSymbol.height);

						x -= eventSymbol.width;
					}
				};


				/**
				 *
				 */
				ActivitySymbol.prototype.removeBoundaryEvent = function(
						eventSymbol) {
					m_utils.removeItemFromArray(this.boundaryEventSymbols,
							eventSymbol);

					this.realignBoundaryEvent();

					eventSymbol.bindingActivitySymbol = null;

					eventSymbol.modelElement
							.unbindFromActivity(this.modelElement);
				};

				ActivitySymbol.prototype.showEditable = function() {
					this.text.hide();
					var editableText = this.diagram.editableText;
					var scrollPos = m_modelerUtils.getModelerScrollPosition();

					var name = this.modelElement.name;
					var textboxWidth, textboxHeight, textBoxX, textBoxY;
					if (this.text.getBBox() != null) {
						textboxWidth = this.text.getBBox().width + 20;
						textboxHeight = this.text.getBBox().height;
						textBoxX = this.text.getBBox().x;
						textBoxY = this.text.getBBox().y;
					} else {
						textboxWidth = m_constants.DEFAULT_TEXT_WIDTH;
						textboxHeight = m_constants.DEFAULT_TEXT_HEIGHT;
						textBoxX = this.x + this.width / 3;
						textBoxY = this.y + this.height / 3;
					}

					textBoxX = textBoxX + this.diagram.X_OFFSET
							- scrollPos.left;
					textBoxY = textBoxY + this.diagram.Y_OFFSET - scrollPos.top;

					editableText.css("width", parseInt(textboxWidth.valueOf()));
					editableText.css("height",
							parseInt(textboxHeight.valueOf()));

					editableText.css("visibility", "visible").html(name)
							.moveDiv({
								"x" : textBoxX,
								"y" : textBoxY
							}).show().trigger("dblclick");
					return this.text;
				};

				ActivitySymbol.prototype.postComplete = function() {
					this.select();
					this.diagram.showEditable(this.text);
				};

				ActivitySymbol.prototype.click = function(x, y) {
					if (this.diagram.CREATE_MODE == this.diagram.mode
							&& m_constants.INTERMEDIATE_EVENT_TYPE == this.diagram.newSymbol.modelElement.eventType) {
						this.diagram.onClick(x, y);
					} else {
						this.click_(x, y);
					}
				};
			}

			/**
			 *
			 */
			function ActivitySymbol_connectToClosure() {
				this.auxiliaryProperties.callbackScope.diagram
						.connectSymbol(this.auxiliaryProperties.callbackScope);
			}

			/**
			 *
			 */
			function ActivitySymbol_connectToActivityClosure() {
				this.auxiliaryProperties.callbackScope.diagram
						.connectToActivity(this.auxiliaryProperties.callbackScope);
			}

			/**
			 *
			 */
			function ActivitySymbol_connectToGatewayClosure() {
				this.auxiliaryProperties.callbackScope.diagram
						.connectToGateway(this.auxiliaryProperties.callbackScope);
			}

			/**
			 *
			 */
			function ActivitySymbol_connectToEndEventClosure() {
				this.auxiliaryProperties.callbackScope.diagram
						.connectToStopEvent(this.auxiliaryProperties.callbackScope);
			}

			/**
			 *
			 */
			function ActivitySymbol_removeClosure() {
				this.auxiliaryProperties.callbackScope
						.createAndSubmitDeleteCommand();
			}

			/**
			 *
			 */
			function ActivitySymbol_switchToSubprocessActivityClosure() {
				this.auxiliaryProperties.callbackScope
						.switchToSubprocessActivity();
			}

			/**
			 *
			 */
			function ActivitySymbol_switchToApplicationActivityClosure() {
				this.auxiliaryProperties.callbackScope
						.switchToApplicationActivity();
			}

			/**
			 *
			 */
			function ActivitySymbol_switchToManualActivityClosure() {
				this.auxiliaryProperties.callbackScope.switchToManualActivity();
			}

			/**
			 *
			 */
			function ActivitySymbol_subprocessMarkerIconMouseMoveClosure() {
				this.auxiliaryProperties.callbackScope
						.onSubprocessMarkerIconMouseMove();
			}

			/**
			 *
			 */
			function ActivitySymbol_subprocessMarkerIconClickClosure() {
				this.auxiliaryProperties.callbackScope
						.onSubprocessMarkerIconClick();
			}

			/**
			 *
			 */
			function ActivitySymbol_updateGlow() {
				this.auxiliaryProperties.callbackScope.removeGlow();

				if (this.auxiliaryProperties.callbackScope.diagram.symbolGlow
						&& this.auxiliaryProperties.callbackScope.lastModifyingUser != null) {
					this.auxiliaryProperties.callbackScope.glow = this.auxiliaryProperties.callbackScope.rectangle
							.glow({
								width : m_constants.GLOW_WIDTH,
								color : window.top.modelingSession
										.getColorByUser(this.auxiliaryProperties.callbackScope.lastModifyingUser),
								opacity : m_constants.GLOW_OPACITY
							});
				} else {
					this.auxiliaryProperties.callbackScope.glow = null;
				}
			}
		});