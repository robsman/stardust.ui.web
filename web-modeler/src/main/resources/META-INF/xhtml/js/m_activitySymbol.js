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
		[ "m_utils", "m_constants", "m_command", "m_canvasManager", "m_symbol",
				"m_gatewaySymbol", "m_eventSymbol",
				"m_activityPropertiesPanel", "m_model", "m_activity" ],
		function(m_utils, m_constants, m_command, m_canvasManager, m_symbol,
				m_gatewaySymbol, m_eventSymbol, m_activityPropertiesPanel,
				m_model, m_activity) {

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
					this.icon = null;
					this.parallelMultiProcessingMarkerIcon = null;
					this.sequentialMultiProcessingMarkerIcon = null;
					this.subprocessMarkerIcon = null;
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
					transferObject.applicationIcon = null;
					transferObject.manualActivityIcon = null;
					transferObject.subprocessIcon = null;
					transferObject.parallelMultiProcessingMarkerIcon = null;
					transferObject.sequentialMultiProcessingMarkerIcon = null;
					transferObject.subprocessMarkerIcon = null;

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

//					this.glow = this.rectangle.glow({
//						width : 5.0,
//						color : m_constants.DEFAULT_STROKE_COLOR,
//						opacity : 0.7
//					});

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

//					if (this.diagram.symbolGlow
//							&& this.lastModifyingUser != null) {
//					}
				};

				/**
				 *
				 */
				ActivitySymbol.prototype.adjustPrimitives = function() {
					this.rectangle.animate({
						"x" : this.x,
						"y" : this.y,
						"width" : this.width,
						"height" : this.height
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);

//					this.glow.remove();
//					this.glow = this.rectangle.glow({
//						width : 5.0,
//						color : m_constants.DEFAULT_STROKE_COLOR,
//						opacity : 0.7
//					});

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

					this.icon = this.subprocessIcon;

					this.icon.show();
					this.icon.toFront();
				};

				/**
				 *
				 */
				ActivitySymbol.prototype.switchToApplicationActivity = function() {
					this.icon.hide();

					this.icon = this.applicationIcon;

					this.icon.show();
					this.icon.toFront();
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
					var link = jQuery("a[id $= 'modeler_view_link']",
							window.parent.frames['ippPortalMain'].document);
					var linkId = link.attr('id');
					var form = link.parents('form:first');
					var formId = form.attr('id');
					var model = m_model.findModel(m_model
							.stripModelId(this.modelElement.subprocessFullId));
					var process = m_model
							.findProcess(this.modelElement.subprocessFullId);

					window.parent.EventHub.events.publish("OPEN_VIEW", linkId,
							formId, "modelerView", "processId=" + process.id
									+ "&modelId=" + model.id + "&processName="
									+ process.name, process.id);
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
		});