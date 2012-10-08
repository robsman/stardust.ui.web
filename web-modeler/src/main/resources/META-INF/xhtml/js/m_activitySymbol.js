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
		[ "m_utils", "m_constants", "m_extensionManager", "m_command",
				"m_canvasManager", "m_symbol", "m_gatewaySymbol", "m_session",
				"m_eventSymbol", "m_activityPropertiesPanel", "m_model",
				"m_activity", "m_commandsController", "m_command" ],
		function(m_utils, m_constants, m_extensionManager, m_command,
				m_canvasManager, m_symbol, m_gatewaySymbol, m_eventSymbol,
				m_session, m_activityPropertiesPanel, m_model, m_activity,
				m_commandsController, m_command) {

			return {
				createActivitySymbol : function(diagram, type) {
					var activitySymbol = new ActivitySymbol();

					activitySymbol.bind(diagram);

					activitySymbol.modelElement = m_activity.createActivity(
							diagram.process, type);

					activitySymbol.diagram.process.activities[activitySymbol.modelElement.id] = activitySymbol.modelElement;

					return activitySymbol;
				},

				createActivitySymbolFromProcess : function(diagram, process) {
					var activitySymbol = new ActivitySymbol();

					activitySymbol.bind(diagram);

					activitySymbol.modelElement = m_activity
							.createActivityFromProcess(diagram.process, process);

					return activitySymbol;
				},

				createActivitySymbolFromApplication : function(diagram,
						application) {
					var activitySymbol = new ActivitySymbol();

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
					this.icon = null;
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
					if (!this.modelElement.prototype) {
						this.modelElement.prototype = {};
					}
					m_utils.inheritMethods(this.modelElement.prototype,
							m_activity.prototype);

					this.parentSymbol = lane;
					this.parentSymbolId = lane.id;
					this.parentSymbol.containedSymbols.push(this);
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
					transferObject.icon = null;
					transferObject.text = null;
					transferObject.commentCountText = null;
					transferObject.manualActivityIcon = null;
					transferObject.subprocessIcon = null;
					transferObject.parallelMultiProcessingMarkerIcon = null;
					transferObject.sequentialMultiProcessingMarkerIcon = null;
					transferObject.subprocessMarkerIcon = null;
					transferObject.commentCountIcon = null;

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

					this.text = m_canvasManager.drawTextNode(
							this.x + 0.5 * this.width,
							this.y + 0.5 * this.height, "").attr({
						"text-anchor" : "middle",
						"font-family" : m_constants.DEFAULT_FONT_FAMILY,
						"font-size" : m_constants.DEFAULT_FONT_SIZE
					});

					this.addToPrimitives(this.text);
					this.addToEditableTextPrimitives(this.text);

					this.manualActivityIcon = m_canvasManager.drawImageAt(
							"../../images/icons/activity-manual.png",
							this.x + 5, this.y + 5, 16, 16).hide();

					this.addToPrimitives(this.manualActivityIcon);

					this.subprocessIcon = m_canvasManager.drawImageAt(
							"../../images/icons/activity-subprocess.png",
							this.x + 5, this.y + 5, 16, 16).hide();

					this.addToPrimitives(this.subprocessIcon);

					this.applicationIcon = m_canvasManager.drawImageAt(
							"../../images/icons/activity-application.png",
							this.x + 5, this.y + 5, 16, 16).hide();

					this.addToPrimitives(this.applicationIcon);

					this.icon = this.manualActivityIcon;

					this.icon.show();

					this.parallelMultiProcessingMarkerIcon = m_canvasManager
							.drawImageAt(
									"../../images/icons/parallel-marker.gif",
									this.x + 0.5 * this.width - 4, this.y + 2,
									16, 16).hide();

					this
							.addToPrimitives(this.parallelMultiProcessingMarkerIcon);

					this.sequentialMultiProcessingMarkerIcon = m_canvasManager
							.drawImageAt(
									"../../images/icons/sequential-marker.gif",
									this.x + 0.5 * this.width - 4, this.y + 2,
									16, 16).hide();

					this
							.addToPrimitives(this.sequentialMultiProcessingMarkerIcon);

					this.subprocessMarkerIcon = m_canvasManager.drawImageAt(
							"../../images/icons/subprocess-marker.gif",
							this.x + 0.5 * this.width - 4,
							this.y + this.height - 16, 16, 16).hide();

					this.addToPrimitives(this.subprocessMarkerIcon);

					this.commentCountText = m_canvasManager
							.drawTextNode(this.x + this.width - 20,
									this.y, "")
							.attr(
									{
										'fill' : m_constants.ACTIVITY_SYMBOL_DEFAULT_FILL_COLOR,
										'stroke' : m_constants.DEFAULT_STROKE_COLOR,
										"text-anchor" : "middle",
										"font-family" : m_constants.DEFAULT_FONT_FAMILY,
										"font-size" : m_constants.DEFAULT_FONT_SIZE
									});

					this.addToPrimitives(this.commentCountText);

					this.commentCountIcon = m_canvasManager.drawImageAt(
							"../../images/icons/comments-count.png",
							this.x + this.width - 30,
							this.y - 8, 16, 16).hide();

					this.addToPrimitives(this.commentCountIcon);
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
				ActivitySymbol.prototype.showPrimitives = function() {
					this.rectangle.show();
					this.text.show();
					this.refreshFromModelElement();
				};

				/**
				 *
				 */
				ActivitySymbol.prototype.refreshFromModelElement = function() {
					this.text.attr("text", this.modelElement.name);
					this.icon.hide();

					if (this.modelElement.activityType == m_constants.MANUAL_ACTIVITY_TYPE) {
						this.icon = this.manualActivityIcon;

						this.subprocessMarkerIcon.hide();
					} else if (this.modelElement.activityType == m_constants.SUBPROCESS_ACTIVITY_TYPE) {
						this.icon = this.subprocessIcon;

						this.subprocessMarkerIcon.show();
					} else if (this.modelElement.activityType == m_constants.APPLICATION_ACTIVITY_TYPE) {
						this.icon = this.applicationIcon;

						this.subprocessMarkerIcon.hide();
					}

					this.icon.show();

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

					if (this.modelElement.comments
							&& this.modelElement.comments.length > 0) {
						this.commentCountText.attr("text",
								this.modelElement.comments.length);
						this.commentCountText.show();
						this.commentCountIcon.show();
					} else {
						this.commentCountText.hide();
						this.commentCountIcon.hide();
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

					this.manualActivityIcon.animate({
						"x" : this.x + 5,
						"y" : this.y + 5
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
					this.subprocessIcon.animate({
						"x" : this.x + 5,
						"y" : this.y + 5
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
					this.applicationIcon.animate({
						"x" : this.x + 5,
						"y" : this.y + 5
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
					this.applicationIcon.animate({
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
						"x" : this.x + 0.5 * this.width - 4,
						"y" : this.y + this.height - 16
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
					this.commentCountText.animate({
						"x" : this.x + this.width - 20,
						"y" : this.y
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
					this.commentCountIcon.animate({
						"x" : this.x + this.width - 30,
						"y" : this.y - 8
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);

					this.adjustPrimitivesOnShrink();
					// this.rectangle.attr({
					// "x" : this.x,
					// "y" : this.y,
					// "width" : this.width,
					// "height" : this.height
					// });
					// this.manualActivityIcon.attr({
					// "x" : this.x + 5,
					// "y" : this.y + 5
					// });
					// this.subprocessIcon.attr({
					// "x" : this.x + 5,
					// "y" : this.y + 5
					// });
					// this.applicationIcon.attr({
					// "x" : this.x + 5,
					// "y" : this.y + 5
					// });
					// this.applicationIcon.attr({
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
					if (this.text.getBBox().width > this.width) {
						var words = this.text.attr("text");
						m_utils.textWrap(this.text, this.width);
					}

					if (this.icon.getBBox().width > this.width) {
						this.icon.hide();
					} else
						this.icon.show();
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
												imageUrl : "../../images/icons/connect.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : ActivitySymbol_connectToClosure
											},
											{
												imageUrl : "../../images/icons/activity.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : ActivitySymbol_connectToActivityClosure
											},
											{
												imageUrl : "../../images/icons/gateway.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : ActivitySymbol_connectToGatewayClosure
											},
											{
												imageUrl : "../../images/icons/end_event_with_border.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : ActivitySymbol_connectToEndEventClosure
											} ],
									[
											{
												imageUrl : "../../images/icons/remove.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : ActivitySymbol_removeClosure
											},
											{
												imageUrl : "../../images/icons/activity-subprocess.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : ActivitySymbol_switchToSubprocessActivityClosure
											},
											{
												imageUrl : "../../images/icons/activity-application.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : ActivitySymbol_switchToApplicationActivityClosure
											} ]);
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
					this.icon.hide();

					this.modelElement.activityType = m_constants.SUBPROCESS_ACTIVITY_TYPE;
					this.icon = this.subprocessIcon;

					this.icon.show();
					this.icon.toFront();

					this.submitChanges();
				};

				/**
				 *
				 */
				ActivitySymbol.prototype.switchToApplicationActivity = function() {
					this.icon.hide();

					this.modelElement.activityType = m_constants.APPLICATION_ACTIVITY_TYPE;
					this.icon = this.applicationIcon;

					this.icon.show();
					this.icon.toFront();

					this.submitChanges();
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
					if ((this.modelElement.activityType == m_constants.MANUAL_ACTIVITY_TYPE || this.modelElement.activityType == m_constants.APPLICATION_ACTIVITY_TYPE)
							&& this.parentSymbol.participantFullId != null) {
						this.modelElement.participantFullId = this.parentSymbol.participantFullId;
					}
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
					for ( var n in this.connections) {
						var connection = this.connections[n];
						if (connection.fromAnchorPoint.symbol.type == m_constants.ACTIVITY_SYMBOL
								&& connection.fromAnchorPoint.symbol.oid == this.oid) {
							if ((null != connection.toAnchorPoint && null != connection.toAnchorPoint.symbol)
									&& connection.toAnchorPoint.symbol.type == m_constants.DATA_SYMBOL) {
								// do nothing
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
								// do nothing
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