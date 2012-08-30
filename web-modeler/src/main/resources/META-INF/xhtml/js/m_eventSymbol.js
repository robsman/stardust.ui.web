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
		[ "m_utils", "m_constants", "m_command", "m_messageDisplay",
				"m_canvasManager", "m_symbol", "m_gatewaySymbol",
				"m_eventPropertiesPanel", "m_event" ],
		function(m_utils, m_constants, m_command, m_messageDisplay,
				m_canvasManager, m_symbol, m_gatewaySymbol,
				m_eventPropertiesPanel, m_event) {

			return {
				createStartEventSymbol : function(diagram) {
					var eventSymbol = new EventSymbol();

					eventSymbol.bind(diagram);
					eventSymbol.modelElement = m_event
							.createStartEvent(diagram.process);

					eventSymbol.diagram.process.events[eventSymbol.modelElement.id] = eventSymbol.modelElement;

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

				this.width = 2 * m_constants.EVENT_DEFAULT_RADIUS;
				this.height = 2 * m_constants.EVENT_DEFAULT_RADIUS;

				/**
				 * Binds all client-side aspects to the object (graphics objects, diagram, base classes).
				 */
				EventSymbol.prototype.bind = function(diagram) {
					this.type = m_constants.EVENT_SYMBOL;

					this.diagram = diagram;

					this.diagram.lastSymbol = this;

					this.propertiesPanel = m_eventPropertiesPanel.getInstance();
					this.circle = null;
					this.image = null;
					this.startImageUrl = "../../images/icons/start-event.png";
					this.stopImageUrl = "../../images/icons/stop-event.png";

					// Size is not transfered from the server

					this.width = 2 * m_constants.EVENT_DEFAULT_RADIUS;
					this.height = 2 * m_constants.EVENT_DEFAULT_RADIUS;
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
					m_utils.inheritMethods(
							this.modelElement.prototype,
							m_event.prototype);

					// Overwrite width and height

					this.width = 2 * m_constants.EVENT_DEFAULT_RADIUS;
					this.height = 2 * m_constants.EVENT_DEFAULT_RADIUS;

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
				EventSymbol.prototype.createTransferObject = function() {
					var transferObject = {};

					m_utils.inheritFields(transferObject, this);

					transferObject = this.prepareTransferObject(transferObject);

					transferObject.circle = null;
					transferObject.image = null;
					transferObject.startImageUrl = null;
					transferObject.stopImageUrl = null;

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
					this.circle = m_canvasManager.drawCircle(
							this.x + m_constants.EVENT_DEFAULT_RADIUS,
							this.y + m_constants.EVENT_DEFAULT_RADIUS,
							m_constants.EVENT_DEFAULT_RADIUS, {
								"fill" : m_constants.EVENT_DEFAULT_FILL,
								"stroke" : m_constants.DEFAULT_STROKE
							});

					this.addToPrimitives(this.circle);

					this.image = m_canvasManager.drawImageAt(
							this.startImageUrl, this.x
									+ m_constants.EVENT_DEFAULT_RADIUS - 0.5 * m_constants.EVENT_ICON_WIDTH,
							this.y + m_constants.EVENT_DEFAULT_RADIUS - 0.5
									* m_constants.EVENT_ICON_WIDTH, m_constants.EVENT_ICON_WIDTH, m_constants.EVENT_ICON_WIDTH);

					this.addToPrimitives(this.image);
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
					if (this.modelElement.eventType == m_constants.START_EVENT_TYPE) {
						this.circle.attr("stroke-width", m_constants.EVENT_START_STROKE_WIDTH);
						this.image.attr("src", this.startImageUrl);
					} else {
						this.circle.attr("stroke-width", m_constants.EVENT_STOP_STROKE_WIDTH);
						this.image.attr("src", this.stopImageUrl);
					}
				};

				/**
				 *
				 */
				EventSymbol.prototype.createFlyOutMenu = function() {
					// For stop event, right menu will be empty.
					var rightMenu = [];

					//If start event
					if (this.modelElement.eventType == m_constants.START_EVENT_TYPE)
					{
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
				};

				/**
				 *
				 */
				EventSymbol.prototype.dehighlight = function() {
					this.circle.attr({
						"stroke" : m_constants.DEFAULT_STROKE_COLOR
					});
				};

				/**
				 *
				 */
				EventSymbol.prototype.adjustPrimitives = function(dX, dY) {
					this.circle.attr({
						cx : this.x + m_constants.EVENT_DEFAULT_RADIUS,
						cy : this.y + m_constants.EVENT_DEFAULT_RADIUS
					});
					this.image.attr({
						x : this.x + m_constants.EVENT_DEFAULT_RADIUS - 0.5
								* m_constants.EVENT_ICON_WIDTH,
						y : this.y + m_constants.EVENT_DEFAULT_RADIUS - 0.5
								* m_constants.EVENT_ICON_WIDTH
					});
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
				EventSymbol.prototype.validateCreateConnection = function() {
					if (this.connections.length > 0) {
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
				};

				/*
				 *
				 */
				EventSymbol.prototype.onParentSymbolChange = function() {
					if (this.modelElement.eventType == m_constants.START_EVENT_TYPE
							&& this.parentSymbol.participantFullId != null) {
						this.modelElement.participantFullId = this.parentSymbol.participantFullId;

						m_utils
								.debug("===> Event Participant ID set to "
										+ this.modelElement.participantFullId);
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
				this.auxiliaryProperties.callbackScope.createAndSubmitDeleteCommand();
			}
		});