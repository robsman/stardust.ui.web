/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

define(
		[ "m_utils", "m_constants", "m_canvasManager", "m_symbol",
				"m_commandsController", "m_command", "m_activity",
				"m_gatewayPropertiesPanel" ],
		function(m_utils, m_constants, m_canvasManager, m_symbol,
				m_commandsController, m_command, m_activity,
				m_gatewayPropertiesPanel) {

			return {
				createGatewaySymbol : function(diagram) {
					var gatewaySymbol = new GatewaySymbol();

					gatewaySymbol.bind(diagram);

					gatewaySymbol.modelElement = m_activity
							.createGatewayActivity(diagram.process);
					// Register the symbol
					gatewaySymbol.diagram.process.gateways[gatewaySymbol.modelElement.id] = gatewaySymbol.modelElement;

					return gatewaySymbol;
				},

				createGatewaySymbolFromJson : function(diagram, lane, json) {
					// TODO Ugly
					m_utils.inheritFields(json, m_symbol.createSymbol());
					m_utils.inheritMethods(json, new GatewaySymbol());

					json.bind(diagram);
					json.initializeFromJson(lane);

					return json;
				}
			};

			function GatewaySymbol() {
				var symbol = m_symbol.createSymbol();

				m_utils.inheritFields(this, symbol);
				m_utils.inheritMethods(GatewaySymbol.prototype, symbol);

				this.width = m_constants.GATEWAY_SYMBOL_DEFAULT_WIDTH;
				this.height = m_constants.GATEWAY_SYMBOL_DEFAULT_HEIGHT;

				/**
				 * Binds all client-side aspects to the object (graphics
				 * objects, diagram, base classes).
				 */
				GatewaySymbol.prototype.bind = function(diagram) {
					this.type = m_constants.GATEWAY_SYMBOL;

					this.diagram = diagram;

					this.diagram.lastSymbol = this;

					this.propertiesPanel = m_gatewayPropertiesPanel
							.getInstance();
					this.path = null;
					this.andPath = null;
					this.xorPath = null;
					this.orCircle = null;
				};

				/**
				 *
				 */
				GatewaySymbol.prototype.toString = function() {
					return "Lightdust.GatewaySymbol";
				};

				/**
				 *
				 */
				GatewaySymbol.prototype.initializeFromJson = function(lane) {
					m_utils.inheritMethods(this.modelElement.prototype,
							m_activity.prototype);

					// Overwrite width as Eclipse modeler only stores activity
					// bounding box

					this.parentSymbol = lane;
					this.parentSymbolId = lane.id;

					// Patch width and height

					this.width = m_constants.GATEWAY_SYMBOL_DEFAULT_WIDTH;
					this.height = m_constants.GATEWAY_SYMBOL_DEFAULT_HEIGHT;
					this.parentSymbol.containedSymbols.push(this);
					this.prepareNoPosition();
					this.completeNoTransfer();
					this.register();
				};

				/**
				 *
				 */
				GatewaySymbol.prototype.createTransferObject = function() {
					var transferObject = {};

					m_utils.inheritFields(transferObject, this);

					transferObject = this.prepareTransferObject(transferObject);

					transferObject.path = null;
					transferObject.andPath = null;
					transferObject.xorPath = null;
					transferObject.orCircle = null;

					return transferObject;
				};

				/**
				 *
				 */
				GatewaySymbol.prototype.getPath = function(withId) {
					var path = "/models/" + this.diagram.model.id
							+ "/processes/" + this.diagram.process.id
							+ "/gateways";

					if (withId) {
						path += "/" + this.modelElement.id;
					}

					return path;
				};

				/**
				 *
				 */
				GatewaySymbol.prototype.createPrimitives = function() {
					this.path = m_canvasManager
							.drawPath(
									this.getPathSvgString(),
									{
										'fill' : m_constants.GATEWAY_SYMBOL_DEFAULT_FILL_COLOR,
										'fill-opacity' : m_constants.GATEWAY_SYMBOL_DEFAULT_FILL_OPACITY,
										'stroke' : m_constants.DEFAULT_STROKE_COLOR,
										'stroke-width' : m_constants.GATEWAY_SYMBOL_DEFAULT_STROKE_WIDTH
									});
					this.addToPrimitives(this.path);

					this.andPath = m_canvasManager
							.drawPath(
									this.getPlusPathSvgString(),
									{
										'stroke' : m_constants.DEFAULT_STROKE_COLOR,
										'stroke-width' : m_constants.GATEWAY_SYMBOL_PLUS_STROKE_WIDTH
									});

					this.andPath.hide();
					this.addToPrimitives(this.andPath);

					this.xorPath = m_canvasManager
							.drawPath(
									this.getCrossPathSvgString(),
									{
										'stroke' : m_constants.DEFAULT_STROKE_COLOR,
										'stroke-width' : m_constants.GATEWAY_SYMBOL_CROSS_STROKE_WIDTH
									});

					this.xorPath.hide();
					this.addToPrimitives(this.xorPath);

					if (this.modelElement.gatewayType == "and") {
						this.andPath.show();
					} else {
						this.xorPath.show();
					}

					this.orCircle = m_canvasManager
							.drawCircle(
									this.x
											+ m_constants.GATEWAY_SYMBOL_OR_RADIUS,
									this.y
											+ m_constants.GATEWAY_SYMBOL_OR_RADIUS,
									m_constants.GATEWAY_SYMBOL_OR_RADIUS,
									{
										"stroke" : m_constants.GATEWAY_SYMBOL_DEFAULTSTROKE_COLOR,
										'stroke-width' : m_constants.GATEWAY_SYMBOL_OR_STROKE_WIDTH
									});

					this.orCircle.hide();
					this.addToPrimitives(this.orCircle);
				};

				/**
				 *
				 */
				GatewaySymbol.prototype.initializeEventHandling = function() {
				};

				/**
				 *
				 */
				GatewaySymbol.prototype.getPathSvgString = function() {
					return "M "
							+ this.x
							+ " "
							+ (this.y + 0.5 * m_constants.GATEWAY_SYMBOL_DEFAULT_HEIGHT)
							+ " L "
							+ (this.x + 0.5 * m_constants.GATEWAY_SYMBOL_DEFAULT_WIDTH)
							+ " "
							+ this.y
							+ " L "
							+ (this.x + m_constants.GATEWAY_SYMBOL_DEFAULT_WIDTH)
							+ " "
							+ (this.y + 0.5 * m_constants.GATEWAY_SYMBOL_DEFAULT_HEIGHT)
							+ " L "
							+ (this.x + 0.5 * m_constants.GATEWAY_SYMBOL_DEFAULT_WIDTH)
							+ " "
							+ (this.y + m_constants.GATEWAY_SYMBOL_DEFAULT_HEIGHT)
							+ " L "
							+ this.x
							+ " "
							+ (this.y + 0.5 * m_constants.GATEWAY_SYMBOL_DEFAULT_HEIGHT);
				};

				/**
				 *
				 */
				GatewaySymbol.prototype.getPlusPathSvgString = function() {
					return "M "
							+ (this.x + m_constants.GATEWAY_SYMBOL_PLUS_OFFSET)
							+ " "
							+ (this.y + 0.5 * m_constants.GATEWAY_SYMBOL_DEFAULT_HEIGHT)
							+ " L "
							+ (this.x
									+ m_constants.GATEWAY_SYMBOL_DEFAULT_WIDTH - m_constants.GATEWAY_SYMBOL_PLUS_OFFSET)
							+ " "
							+ (this.y + 0.5 * m_constants.GATEWAY_SYMBOL_DEFAULT_HEIGHT)
							+ " M "
							+ (this.x + 0.5 * m_constants.GATEWAY_SYMBOL_DEFAULT_WIDTH)
							+ " "
							+ (this.y + m_constants.GATEWAY_SYMBOL_PLUS_OFFSET)
							+ " L "
							+ (this.x + 0.5 * m_constants.GATEWAY_SYMBOL_DEFAULT_WIDTH)
							+ " "
							+ (this.y
									+ m_constants.GATEWAY_SYMBOL_DEFAULT_HEIGHT - m_constants.GATEWAY_SYMBOL_PLUS_OFFSET);
				};

				/**
				 *
				 */
				GatewaySymbol.prototype.getCrossPathSvgString = function() {
					return "M "
							+ (this.x + m_constants.GATEWAY_SYMBOL_CROSS_OFFSET)
							+ " "
							+ (this.y + m_constants.GATEWAY_SYMBOL_CROSS_OFFSET)
							+ " L "
							+ (this.x
									+ m_constants.GATEWAY_SYMBOL_DEFAULT_WIDTH - m_constants.GATEWAY_SYMBOL_CROSS_OFFSET)
							+ " "
							+ (this.y
									+ m_constants.GATEWAY_SYMBOL_DEFAULT_HEIGHT - m_constants.GATEWAY_SYMBOL_CROSS_OFFSET)
							+ " M "
							+ (this.x + m_constants.GATEWAY_SYMBOL_CROSS_OFFSET)
							+ " "
							+ (this.y
									+ m_constants.GATEWAY_SYMBOL_DEFAULT_HEIGHT - m_constants.GATEWAY_SYMBOL_CROSS_OFFSET)
							+ " L "
							+ (this.x
									+ m_constants.GATEWAY_SYMBOL_DEFAULT_WIDTH - m_constants.GATEWAY_SYMBOL_CROSS_OFFSET)
							+ " "
							+ (this.y + m_constants.GATEWAY_SYMBOL_CROSS_OFFSET);
				};

				/**
				 *
				 */
				GatewaySymbol.prototype.adjustPrimitives = function(dX, dY) {
					this.path.attr({
						"path" : this.getPathSvgString()
					});
					this.andPath.attr({
						"path" : this.getPlusPathSvgString()
					});
					this.xorPath.attr({
						"path" : this.getCrossPathSvgString()
					});
					this.orCircle.attr({
						cx : this.x + 0.5
								* m_constants.GATEWAY_SYMBOL_DEFAULT_WIDTH,
						cy : this.y + 0.5
								* m_constants.GATEWAY_SYMBOL_DEFAULT_HEIGHT
					});

				};

				/**
				 * Registers symbol in specific lists in the diagram and model
				 * element in the process.
				 */
				GatewaySymbol.prototype.register = function() {
					this.diagram.gatewaySymbols[this.oid] = this;
					this.diagram.process.gateways[this.modelElement.id] = this.modelElement;
				};

				/**
				 *
				 */
				GatewaySymbol.prototype.refreshFromModelElement = function() {
					if (this.modelElement.gatewayType == m_constants.AND_GATEWAY_TYPE) {
						this.andPath.show();
						this.xorPath.hide();
						this.orCircle.hide();
					} else if (this.modelElement.gatewayType == m_constants.XOR_GATEWAY_TYPE) {
						this.andPath.hide();
						this.xorPath.show();
						this.orCircle.hide();
					} else if (this.modelElement.gatewayType == m_constants.OR_GATEWAY_TYPE) {
						this.andPath.hide();
						this.xorPath.hide();
						this.orCircle.show();
					}
				};

				GatewaySymbol.prototype.recalculateBoundingBox = function() {
					// Noting to be done here
				};

				/**
				 *
				 */
				GatewaySymbol.prototype.createFlyOutMenu = function() {
					this
							.addFlyOutMenuItems(
									[],
									[
											{
												imageUrl : "../../images/icons/connect.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : GatewaySymbol_connectToClosure
											},
											{
												imageUrl : "../../images/icons/activity.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : GatewaySymbol_connectToActivityClosure
											},
											{
												imageUrl : "../../images/icons/gateway.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : GatewaySymbol_connectToGatewayClosure
											},
											{
												imageUrl : "../../images/icons/end_event_with_border.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : GatewaySymbol_connectToEndEventClosure
											} ],
									[
											{
												imageUrl : "../../images/icons/remove.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : GatewaySymbol_removeClosure
											},
											{
												imageUrl : "../../images/icons/xor-gateway-menu-icon.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : GatewaySymbol_switchToXorGatewayClosure
											},
											{
												imageUrl : "../../images/icons/and-gateway-menu-icon.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : GatewaySymbol_switchToAndGatewayClosure
											}

									]);
				};

				/**
				 *
				 */
				GatewaySymbol.prototype.highlight = function() {
					this.path.attr({
						"stroke" : m_constants.SELECT_STROKE_COLOR
					});
					this.andPath.attr({
						"stroke" : m_constants.SELECT_STROKE_COLOR
					});
					this.xorPath.attr({
						"stroke" : m_constants.SELECT_STROKE_COLOR
					});
					this.orCircle.attr({
						"stroke" : m_constants.SELECT_STROKE_COLOR
					});
				};

				/**
				 *
				 */
				GatewaySymbol.prototype.dehighlight = function() {
					this.path.attr({
						"stroke" : m_constants.DEFAULT_STROKE_COLOR
					});
					this.andPath.attr({
						"stroke" : m_constants.DEFAULT_STROKE_COLOR
					});
					this.xorPath.attr({
						"stroke" : m_constants.DEFAULT_STROKE_COLOR
					});
					this.orCircle.attr({
						"stroke" : m_constants.DEFAULT_STROKE_COLOR
					});
				};

				GatewaySymbol.prototype.switchToAndGateway = function() {
					this.modelElement.gatewayType = m_constants.AND_GATEWAY_TYPE;
					this.xorPath.hide();
					this.andPath.show();
					m_commandsController.submitImmediately(m_command
							.createUpdateCommand(this));
				};

				GatewaySymbol.prototype.switchToXorGateway = function() {
					this.modelElement.gatewayType = m_constants.XOR_GATEWAY_TYPE;
					this.andPath.hide();
					this.xorPath.show();
					m_commandsController.submitImmediately(m_command
							.createUpdateCommand(this));
				};
			}

			/**
			 *
			 */
			function GatewaySymbol_connectToClosure() {
				this.auxiliaryProperties.callbackScope.diagram
						.connectSymbol(this.auxiliaryProperties.callbackScope);
			}

			/**
			 *
			 */
			function GatewaySymbol_connectToActivityClosure() {
				this.auxiliaryProperties.callbackScope.diagram
						.connectToActivity(this.auxiliaryProperties.callbackScope);
			}

			/**
			 *
			 */
			function GatewaySymbol_connectToGatewayClosure() {
				this.auxiliaryProperties.callbackScope.diagram
						.connectToGateway(this.auxiliaryProperties.callbackScope);
			}

			/**
			 *
			 */
			function GatewaySymbol_connectToEndEventClosure() {
				this.auxiliaryProperties.callbackScope.diagram
						.connectToStopEvent(this.auxiliaryProperties.callbackScope);
			}

			/**
			 *
			 */
			function GatewaySymbol_removeClosure() {
				this.auxiliaryProperties.callbackScope.createAndSubmitDeleteCommand();
			}

			/**
			 *
			 */
			function GatewaySymbol_switchToXorGatewayClosure() {
				this.auxiliaryProperties.callbackScope.switchToXorGateway();
			}

			/**
			 *
			 */
			function GatewaySymbol_switchToAndGatewayClosure() {
				this.auxiliaryProperties.callbackScope.switchToAndGateway();
			}
		});