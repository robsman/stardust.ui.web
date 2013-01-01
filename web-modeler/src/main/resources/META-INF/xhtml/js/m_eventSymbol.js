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
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_messageDisplay",
				"bpm-modeler/js/m_canvasManager", "bpm-modeler/js/m_symbol",
				"bpm-modeler/js/m_gatewaySymbol",
				"bpm-modeler/js/m_eventPropertiesPanel",
				"bpm-modeler/js/m_event", "bpm-modeler/js/m_modelerUtils"],
		function(m_utils, m_constants, m_command, m_messageDisplay,
				m_canvasManager, m_symbol, m_gatewaySymbol,
				m_eventPropertiesPanel, m_event, m_modelerUtils) {

			return {
				createStartEventSymbol : function(diagram) {
					var eventSymbol = new EventSymbol();

					eventSymbol.bind(diagram);
					eventSymbol.modelElement = m_event
							.createStartEvent(diagram.process);

					eventSymbol.diagram.process.events[eventSymbol.modelElement.id] = eventSymbol.modelElement;

					return eventSymbol;
				},
				createIntermediateEventSymbol : function(diagram) {
					var eventSymbol = new EventSymbol();

					eventSymbol.bind(diagram);
					eventSymbol.modelElement = m_event
							.createIntermediateEvent(diagram.process);

					return eventSymbol;
				},
				createStopEventSymbol : function(diagram) {
					var eventSymbol = new EventSymbol();

					eventSymbol.bind(diagram);
					eventSymbol.modelElement = m_event
							.createStopEvent(diagram.process);

					return eventSymbol;
				},
				createEventSymbolFromJson : function(diagram, lane, json) {
					// TODO Ugly
					m_utils.inheritFields(json, m_symbol.createSymbol());
					m_utils.inheritMethods(json, new EventSymbol());

					json.bind(diagram);
					json.initializeFromJson(lane);

					return json;
				}
			};

			/**
			 *
			 */
			function EventSymbol() {
				var symbol = m_symbol.createSymbol();

				m_utils.inheritFields(this, symbol);
				m_utils.inheritMethods(EventSymbol.prototype, symbol);

				/**
				 * Binds all client-side aspects to the object (graphics
				 * objects, diagram, base classes).
				 */
				EventSymbol.prototype.bind = function(diagram) {
					this.type = m_constants.EVENT_SYMBOL;

					this.diagram = diagram;

					this.diagram.lastSymbol = this;
					this.bindingActivitySymbol = null;

					this.propertiesPanel = m_eventPropertiesPanel.getInstance();
					this.circle = null;
					this.innerCircle = null;
					this.image = null;
					this.text = null;
					this.timerCatchingUrl = "../../images/icons/event-timer-catching.png";
					this.messageCatchingUrl = "../../images/icons/event-message-catching.png";
					this.messageThrowingUrl = "../../images/icons/event-message-throwing.png";
					this.errorCatchingUrl = "../../images/icons/event-error-catching.png";
					this.errorThrowingUrl = "../../images/icons/event-error-throwing.png";

					this.performClientSideAdj();
				};

				/**
				 *
				 */
				EventSymbol.prototype.toString = function() {
					return "Lightdust.EventSymbol";
				};

				/**
				 *
				 */
				EventSymbol.prototype.initializeFromJson = function(lane) {
					m_event.typeObject(this.modelElement);

					this.performClientSideAdj();

					this.parentSymbol = lane;
					this.parentSymbolId = lane.id;
					this.parentSymbol.containedSymbols.push(this);
					this.prepareNoPosition();
					this.completeNoTransfer();
					this.register();
				};

				/**
				 * Client side adjustment This code is required in case the
				 * imported model is eclipse born. Force setting of these
				 * attributes cannot be done in Refresh method as
				 * m_propertiesPanel.processCommand again overwrites these
				 * attributes and then symbol.refresh does not get invoked.
				 */
				EventSymbol.prototype.performClientSideAdj = function() {
					if (this.width && this.width != (2 * m_constants.EVENT_DEFAULT_RADIUS)) {
						this.clientSideAdjX = (this.width / 2)
								- m_constants.EVENT_DEFAULT_RADIUS;
						this.x = this.x + this.clientSideAdjX;
					}
					this.width = 2 * m_constants.EVENT_DEFAULT_RADIUS;
					this.height = 2 * m_constants.EVENT_DEFAULT_RADIUS;
				};

				/**
				 *
				 */
				EventSymbol.prototype.createTransferObject = function() {
					var transferObject = {};

					m_utils.inheritFields(transferObject, this);

					transferObject = this.prepareTransferObject(transferObject);

					transferObject.circle = null;
					transferObject.innerCircle = null;
					transferObject.image = null;
					transferObject.text = null;
					transferObject.startImageUrl = null;
					transferObject.stopImageUrl = null;
					transferObject.bindingActivitySymbol = null;

					return transferObject;
				};

				/**
				 *
				 */
				EventSymbol.prototype.getPath = function(withId) {
					var path = "/models/" + this.diagram.model.id
							+ "/processes/" + this.diagram.process.id
							+ "/events";

					if (withId) {
						path += "/" + this.modelElement.id;
					}

					return path;
				};

				/**
				 *
				 */
				EventSymbol.prototype.createPrimitives = function() {
					this.circle = m_canvasManager.drawCircle(this.x
							+ m_constants.EVENT_DEFAULT_RADIUS, this.y
							+ m_constants.EVENT_DEFAULT_RADIUS,
							m_constants.EVENT_DEFAULT_RADIUS, {
								"fill" : m_constants.EVENT_DEFAULT_FILL,
								"stroke" : m_constants.DEFAULT_STROKE
							});

					this.addToPrimitives(this.circle);
					this.addToEditableTextPrimitives(this.circle);

					this.innerCircle = m_canvasManager.drawCircle(this.x
							+ m_constants.EVENT_DEFAULT_RADIUS, this.y
							+ m_constants.EVENT_DEFAULT_RADIUS,
							m_constants.EVENT_DEFAULT_RADIUS - 2, {
								"fill" : m_constants.EVENT_DEFAULT_FILL,
								"stroke" : m_constants.DEFAULT_STROKE
							});

					this.addToPrimitives(this.innerCircle);

					this.image = m_canvasManager.drawImageAt(
							this.timerCatchingUrl, this.x
									+ m_constants.EVENT_DEFAULT_RADIUS - 0.5
									* m_constants.EVENT_ICON_WIDTH, this.y
									+ m_constants.EVENT_DEFAULT_RADIUS - 0.5
									* m_constants.EVENT_ICON_WIDTH,
							m_constants.EVENT_ICON_WIDTH,
							m_constants.EVENT_ICON_WIDTH);

					this.addToPrimitives(this.image);


					this.text = m_canvasManager.drawTextNode(
							this.x + 0.5 * this.width,
							this.y + this.height + 1.2
									* m_constants.DEFAULT_FONT_SIZE, "").attr({
						"text-anchor" : "middle",
						"font-family" : m_constants.DEFAULT_FONT_FAMILY,
						"font-size" : m_constants.DEFAULT_FONT_SIZE
					});

					this.addToPrimitives(this.text);
					this.addToEditableTextPrimitives(this.text);
				};

				/**
				 * Registers symbol in specific lists in the diagram and model
				 * element in the process.
				 */
				EventSymbol.prototype.register = function() {
					this.diagram.eventSymbols[this.oid] = this;
					this.diagram.process.events[this.modelElement.id] = this.modelElement;
				};

				/**
				 *
				 */
				EventSymbol.prototype.initializeEventHandling = function() {
				};

				/**
				 *
				 */
				EventSymbol.prototype.refreshFromModelElement = function() {
					if (this.modelElement.name
							&& this.modelElement.name.trim() != "") {
						this.text.attr("text", this.modelElement.name);
						this.text.show();
					} else {
						this.text.hide();
					}

					if (this.modelElement.interrupting) {
						this.circle
								.attr(
										"stroke-dasharray",
										m_constants.EVENT_INTERRUPTING_STROKE_DASHARRAY);
						this.innerCircle
								.attr(
										"stroke-dasharray",
										m_constants.EVENT_INTERRUPTING_STROKE_DASHARRAY);
					} else {
						this.circle
								.attr(
										"stroke-dasharray",
										m_constants.EVENT_NON_INTERRUPTING_STROKE_DASHARRAY);
						this.innerCircle
								.attr(
										"stroke-dasharray",
										m_constants.EVENT_NON_INTERRUPTING_STROKE_DASHARRAY);
					}

					// Determin circle stroke and showing

					if (this.modelElement.eventType == m_constants.START_EVENT_TYPE) {
						this.circle.attr("stroke-width",
								m_constants.EVENT_START_STROKE_WIDTH);
						this.innerCircle.hide();
					} else if (this.modelElement.eventType == m_constants.INTERMEDIATE_EVENT_TYPE) {
						this.circle.attr("stroke-width",
								m_constants.EVENT_INTERMEDIATE_STROKE_WIDTH);
						this.innerCircle.attr("stroke-width",
								m_constants.EVENT_INTERMEDIATE_STROKE_WIDTH);

						this.innerCircle.show();
					} else {
						this.circle.attr("stroke-width",
								m_constants.EVENT_STOP_STROKE_WIDTH);
						this.innerCircle.hide();
					}

					// Determine icon

					if (this.modelElement.eventClass == m_constants.TIMER_EVENT_CLASS
							&& !this.modelElement.throwing) {
						this.image.attr("src", this.timerCatchingUrl);
						this.image.show();
					} else if (this.modelElement.eventClass == m_constants.MESSAGE_EVENT_CLASS) {
						if (this.modelElement.throwing) {
							this.image.attr("src", this.messageCatchingUrl);
							this.image.show();
						} else {
							this.image.attr("src", this.messageThrowingUrl);
							this.image.show();
						}
					} else if (this.modelElement.eventClass == m_constants.ERROR_EVENT_CLASS) {
						if (this.modelElement.throwing) {
							this.image.attr("src", this.errorCatchingUrl);
							this.image.show();
						} else {
							this.image.attr("src", this.errorThrowingUrl);
							this.image.show();
						}
					} else {
						this.image.hide();
					}
				};

				/**
				 *
				 */
				EventSymbol.prototype.createFlyOutMenu = function() {
					// For stop event, right menu will be empty.
					var rightMenu = [];

					// If start event
					if (this.modelElement.eventType == m_constants.START_EVENT_TYPE
							|| this.modelElement.eventType == m_constants.INTERMEDIATE_EVENT_TYPE) {
						rightMenu = [ {
							imageUrl : "../../images/icons/connect.png",
							imageWidth : 16,
							imageHeight : 16,
							clickHandler : EventSymbol_connectToClosure
						}, {
							imageUrl : "../../images/icons/activity.png",
							imageWidth : 16,
							imageHeight : 16,
							clickHandler : EventSymbol_connectToActivityClosure
						}, {
							imageUrl : "../../images/icons/gateway.png",
							imageWidth : 16,
							imageHeight : 16,
							clickHandler : EventSymbol_connectToGatewayClosure
						} ];
					}

					this.addFlyOutMenuItems([], rightMenu, [ {
						imageUrl : "../../images/icons/remove.png",
						imageWidth : 16,
						imageHeight : 16,
						clickHandler : EventSymbol_removeClosure
					} ]);
				};

				/**
				 *
				 */
				EventSymbol.prototype.highlight = function() {
					this.circle.attr({
						"stroke" : m_constants.SELECT_STROKE_COLOR
					});
					this.innerCircle.attr({
						"stroke" : m_constants.SELECT_STROKE_COLOR
					});
				};

				/**
				 *
				 */
				EventSymbol.prototype.dehighlight = function() {
					this.circle.attr({
						"stroke" : m_constants.DEFAULT_STROKE_COLOR
					});
					this.innerCircle.attr({
						"stroke" : m_constants.DEFAULT_STROKE_COLOR
					});
				};

				/**
				 *
				 */
				EventSymbol.prototype.adjustPrimitives = function(dX, dY) {

					this.performClientSideAdj();

					this.circle.animate({
						cx : this.x + m_constants.EVENT_DEFAULT_RADIUS,
						cy : this.y + m_constants.EVENT_DEFAULT_RADIUS
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
					this.innerCircle.animate({
						cx : this.x + m_constants.EVENT_DEFAULT_RADIUS,
						cy : this.y + m_constants.EVENT_DEFAULT_RADIUS
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
					this.image.animate({
						x : this.x + m_constants.EVENT_DEFAULT_RADIUS - 0.5
								* m_constants.EVENT_ICON_WIDTH,
						y : this.y + m_constants.EVENT_DEFAULT_RADIUS - 0.5
								* m_constants.EVENT_ICON_WIDTH
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
					this.text.animate({
						x : this.x + 0.5 * this.width,
						y : this.y + this.height + 1.2
								* m_constants.DEFAULT_FONT_SIZE
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);

					this.adjustPrimitivesOnShrink();

					if (this.diagram.symbolGlow
							&& this.lastModifyingUser != null) {
						if (this.glow) {
							this.glow.remove();
						}

						this.glow = this.circle.glow({
							width : m_constants.GLOW_WIDTH,
							color : window.top.modelingSession
									.getColorByUser(this.lastModifyingUser),
							opacity : m_constants.GLOW_OPACITY
						});
					}
				};

				EventSymbol.prototype.adjustPrimitivesOnShrink = function() {
					if (this.parentSymbol && this.parentSymbol.minimized) {
						return;
					}
					if (this.text) {
						if (this.text.getBBox().width > (4.0 * this.width)) {
							var words = this.text.attr("text");
							m_utils.textWrap(this.text, 4.0 * this.width);
						}
					}
				};

				/**
				 *
				 */
				EventSymbol.prototype.recalculateBoundingBox = function() {
					// Noting to be done here
				};

				/**
				 *
				 */
				EventSymbol.prototype.validateCreateConnection = function(conn) {
					if (this.connections.length > 0
							&& this.connections[0].oid > 0
							&& (this.connections[0].oid != conn.oid)) {
						m_messageDisplay
								.showMessage("No further connection allowed for this Event.");

						return false;
					}

					return true;
				};

				/**
				 *
				 */
				EventSymbol.prototype.onComplete = function() {
					this.onParentSymbolChange();

					m_utils.debug("EventSymbol.onComplete");

					if (this.modelElement.eventType == m_constants.INTERMEDIATE_EVENT_TYPE) {
						var hitSymbol = this.diagram
								.getSymbolOverlappingWithSymbol(this);

						m_utils.debug("Symbol hit");

						if (hitSymbol != null
								&& hitSymbol.type == m_constants.ACTIVITY_SYMBOL) {
							m_utils.debug("Add boundary");
							hitSymbol.addBoundaryEvent(this);
							// TODO Submit Change
						}
					}
				};

				EventSymbol.prototype.showEditable = function() {
					this.text.hide();
					var editableText = this.diagram.editableText;
					var scrollPos = m_modelerUtils.getModelerScrollPosition();

					var name = this.modelElement.name;

					var textboxWidth = this.text.getBBox().width + 20;
					var textboxHeight = this.text.getBBox().height;

					if (textboxWidth < m_constants.DEFAULT_TEXT_WIDTH
							|| textboxHeight < m_constants.DEFAULT_TEXT_HEIGHT) {
						textboxWidth = m_constants.DEFAULT_TEXT_WIDTH;
						textboxHeight = m_constants.DEFAULT_TEXT_HEIGHT;
					}

					editableText.css("width", parseInt(textboxWidth.valueOf()));
					editableText.css("height",
							parseInt(textboxHeight.valueOf()));

					editableText.css("visibility", "visible").html(name)
							.moveDiv(
									{
										"x" : this.x + this.diagram.X_OFFSET
												- scrollPos.left - 10,
										"y" : this.y + this.diagram.Y_OFFSET
												+ (2 * m_constants.EVENT_DEFAULT_RADIUS + 5) - scrollPos.top
									}).show().trigger("dblclick");

					return this.text;
				};

				EventSymbol.prototype.postComplete = function() {
					this.select();
					this.diagram.showEditable(this.text);
				};

				/*
				 *
				 */
				EventSymbol.prototype.onParentSymbolChange = function() {
					if (this.modelElement.eventType == m_constants.START_EVENT_TYPE
							&& this.parentSymbol.participantFullId != null) {
						this.modelElement.participantFullId = this.parentSymbol.participantFullId;
					}
				};

				/**
				 *
				 */
				EventSymbol.prototype.postMove = function() {
					var hitSymbol = this.diagram
							.getSymbolOverlappingWithSymbol(this);

					if (hitSymbol != null
							&& hitSymbol.type == m_constants.ACTIVITY_SYMBOL) {
						this.highlight();
					} else {
						this.dehighlight();
					}
				};

				/**
				 *
				 */
				EventSymbol.prototype.postDrag = function(dX, dY, x, y) {
					var hitSymbol = this.diagram
							.getSymbolOverlappingWithSymbol(this);

					if (hitSymbol != null
							&& hitSymbol.type == m_constants.ACTIVITY_SYMBOL) {
						this.highlight();
					} else {
						this.dehighlight();
					}
				};

				/**
				 *
				 */
				EventSymbol.prototype.postDragStop = function() {
					var hitSymbol = this.diagram
							.getSymbolOverlappingWithSymbol(this);

					if (hitSymbol != null
							&& hitSymbol.type == m_constants.ACTIVITY_SYMBOL) {
						hitSymbol.addBoundaryEvent(this);

						// TODO Submit change
					} else if (this.bindingActivitySymbol != null) {
						this.bindingActivitySymbol.removeBoundaryEvent(this);
					}
				};

				/**
				 *
				 */
				EventSymbol.prototype.resolveNonHierarchicalRelationships = function() {
					if (this.modelElement.isBoundaryEvent()) {
						this.bindingActivitySymbol = this.diagram
								.findActivitySymbolById(this.modelElement.bindingActivityUuid);
					}
				};
			}

			/**
			 *
			 */
			function EventSymbol_connectToClosure() {
				this.auxiliaryProperties.callbackScope.diagram
						.connectSymbol(this.auxiliaryProperties.callbackScope);
			}

			/**
			 *
			 */
			function EventSymbol_connectToGatewayClosure() {
				this.auxiliaryProperties.callbackScope.diagram
						.connectToGateway(this.auxiliaryProperties.callbackScope);
			}

			/**
			 *
			 */
			function EventSymbol_connectToActivityClosure() {
				this.auxiliaryProperties.callbackScope.diagram
						.connectToActivity(this.auxiliaryProperties.callbackScope);
			}

			/**
			 *
			 */
			function EventSymbol_removeClosure() {
				this.auxiliaryProperties.callbackScope
						.createAndSubmitDeleteCommand();
			}
		});