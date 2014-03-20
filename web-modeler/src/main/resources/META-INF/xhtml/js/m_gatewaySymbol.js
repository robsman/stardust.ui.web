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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_canvasManager", "bpm-modeler/js/m_symbol",
				"bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_command", "bpm-modeler/js/m_activity",
				"bpm-modeler/js/m_modelerUtils"],
		function(m_utils, m_constants, m_canvasManager, m_symbol,
				m_commandsController, m_command, m_activity, m_modelerUtils) {
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
				var _super = m_utils.inheritMethods(GatewaySymbol.prototype, symbol, {selected: ['createTransferObject']});
				

				/**
				 * Binds all client-side aspects to the object (graphics
				 * objects, diagram, base classes).
				 */
				GatewaySymbol.prototype.bind = function(diagram) {
					this.type = m_constants.GATEWAY_SYMBOL;

					this.diagram = diagram;

					this.diagram.lastSymbol = this;

					this.propertiesPanel = this.diagram.gatewayPropertiesPanel;
					
					this.path = null;
					this.andPath = null;
					this.xorPath = null;
					this.orCircle = null;
					this.text = null;
					this.performClientSideAdj();
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
					if (!this.modelElement.prototype) {
						this.modelElement.prototype = {};
					}
					m_utils.inheritMethods(this.modelElement.prototype,
							m_activity.prototype);

					// Overwrite width as Eclipse modeler only stores activity
					// bounding box

					this.parentSymbol = lane;
					this.parentSymbolId = lane.id;

					this.performClientSideAdj();

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
				GatewaySymbol.prototype.performClientSideAdj = function() {
					if (this.width &&  this.width != m_constants.GATEWAY_SYMBOL_DEFAULT_WIDTH) {
						this.clientSideAdjX = (this.width / 2)
								- (m_constants.GATEWAY_SYMBOL_DEFAULT_WIDTH / 2);
						this.x = this.x + this.clientSideAdjX;
					}
					this.width = m_constants.GATEWAY_SYMBOL_DEFAULT_WIDTH;
					this.height = m_constants.GATEWAY_SYMBOL_DEFAULT_HEIGHT;
				};

				/**
				 *
				 */
				GatewaySymbol.prototype.createTransferObject = function() {
					var transferObject = {};

					m_utils.inheritFields(transferObject, this);

					transferObject = _super.createTransferObject(this, transferObject);

					transferObject.path = null;
					transferObject.andPath = null;
					transferObject.xorPath = null;
					transferObject.orCircle = null;
					transferObject.text = null;
					transferObject.width = m_constants.GATEWAY_SYMBOL_DEFAULT_WIDTH_EC;
					transferObject.height = m_constants.GATEWAY_SYMBOL_DEFAULT_HEIGHT_EC;
					var clientSideAdjX = (m_constants.GATEWAY_SYMBOL_DEFAULT_WIDTH_EC / 2)
					- (m_constants.GATEWAY_SYMBOL_DEFAULT_WIDTH / 2);
					transferObject.x = transferObject.x - clientSideAdjX;

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
					this.path = this.diagram.canvasManager
							.drawPath(
									this.getPathSvgString(),
									{
										'fill' : m_constants.GATEWAY_SYMBOL_DEFAULT_FILL_COLOR,
										'fill-opacity' : m_constants.GATEWAY_SYMBOL_DEFAULT_FILL_OPACITY,
										'stroke' : m_constants.DEFAULT_STROKE_COLOR,
										'stroke-width' : m_constants.GATEWAY_SYMBOL_DEFAULT_STROKE_WIDTH
									});
					this.addToPrimitives(this.path);
					this.addToEditableTextPrimitives(this.path);

					this.andPath = this.diagram.canvasManager
							.drawPath(
									this.getPlusPathSvgString(),
									{
										'stroke' : m_constants.DEFAULT_STROKE_COLOR,
										'stroke-width' : m_constants.GATEWAY_SYMBOL_PLUS_STROKE_WIDTH
									});

					this.andPath.hide();
					this.addToPrimitives(this.andPath);
					this.addToEditableTextPrimitives(this.andPath);

					this.xorPath = this.diagram.canvasManager
							.drawPath(
									this.getCrossPathSvgString(),
									{
										'stroke' : m_constants.DEFAULT_STROKE_COLOR,
										'stroke-width' : m_constants.GATEWAY_SYMBOL_CROSS_STROKE_WIDTH
									});

					this.xorPath.hide();
					this.addToPrimitives(this.xorPath);
					this.addToEditableTextPrimitives(this.xorPath);

					this.orCircle = this.diagram.canvasManager
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
					this.addToEditableTextPrimitives(this.orCircle);
			
					if (this.modelElement.gatewayType == "and") {
						this.andPath.show();
					} else if(this.modelElement.gatewayType == "xor"){
						this.xorPath.show();
					} else{
						this.orCircle.show();
					}

					this.text = this.diagram.canvasManager.drawTextNode(
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

					this.performClientSideAdj();

					this.text.animate({
						x : this.x + 0.5 * this.width,
						y : this.y + this.height + 1.2
								* m_constants.DEFAULT_FONT_SIZE
					}, this.diagram.animationDelay,
							this.diagram.animationEasing);
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
				 * Returns max height for flyout menu based on number of
				 * vertical menu items and symbol height
				 */
				GatewaySymbol.prototype.getFlyoutMenuHeight = function(height) {
					var defaultheight = height
							+ m_constants.FLY_OUT_MENU_EMPTY_MARGIN
							+ m_constants.FLY_OUT_MENU_CONTENT_MARGIN;
					var rightVertMenuHeight = m_constants.FLY_OUT_MENU_ITEM_MARGIN
							+ this.rightFlyOutMenuItems.length
							* (16 + m_constants.FLY_OUT_MENU_ITEM_MARGIN);
					var leftVertMenuHeight = m_constants.FLY_OUT_MENU_ITEM_MARGIN
							+ this.leftFlyOutMenuItems.length
							* (16 + m_constants.FLY_OUT_MENU_ITEM_MARGIN);

					return Math.max(defaultheight, rightVertMenuHeight,
							leftVertMenuHeight);
				}

				/**
				 * Overrides Drawable.prototype.adjustFlyOutMenu
				 *
				 * TODO - this can be the default implementation as it
				 * caclulates the height dynamically. Will also need to
				 * determine width dynamically if moved to Diagram as default
				 * implementation.
				 */
				GatewaySymbol.prototype.adjustFlyOutMenu = function(x, y,
						width, height) {

					this.flyOutMenuBackground.attr({
						'x' : x - m_constants.FLY_OUT_MENU_CONTENT_MARGIN,
						'y' : y - m_constants.FLY_OUT_MENU_EMPTY_MARGIN,
						'width' : width + 2
								* m_constants.FLY_OUT_MENU_CONTENT_MARGIN + 5,
						'height' : this.getFlyoutMenuHeight(height)
					});

					this.adjustFlyOutMenuItems(x, y, width, height);
				};

				/**
				 * Overrides Drawable.prototype.adjustFlyOutMenuItems
				 */
				GatewaySymbol.prototype.adjustFlyOutMenuItems = function(x, y,
						width, height) {
					var n = 0;

					while (n < this.leftFlyOutMenuItems.length) {
						this.leftFlyOutMenuItems[n]
								.attr({
									x : x
											- m_constants.FLY_OUT_MENU_CONTENT_MARGIN
											+ m_constants.FLY_OUT_MENU_ITEM_MARGIN,
									y : y
											- m_constants.FLY_OUT_MENU_EMPTY_MARGIN
											+ m_constants.FLY_OUT_MENU_ITEM_MARGIN
											+ n
											* (16 + m_constants.FLY_OUT_MENU_ITEM_MARGIN)
								});

						++n;
					}

					n = 0;

					while (n < this.rightFlyOutMenuItems.length) {
						this.rightFlyOutMenuItems[n]
								.attr({
									x : x
											+ width
											+ m_constants.FLY_OUT_MENU_CONTENT_MARGIN
											- m_constants.FLY_OUT_MENU_ITEM_MARGIN
											-5,
									y : y
											- m_constants.FLY_OUT_MENU_EMPTY_MARGIN
											+ m_constants.FLY_OUT_MENU_ITEM_MARGIN
											+ n
											* (16 + m_constants.FLY_OUT_MENU_ITEM_MARGIN)
								});

						++n;
					}

					n = 0;

					while (n < this.bottomFlyOutMenuItems.length) {
						this.bottomFlyOutMenuItems[n]
								.attr({
									x : x
											- m_constants.FLY_OUT_MENU_CONTENT_MARGIN
											+ m_constants.FLY_OUT_MENU_ITEM_MARGIN
											+ n
											* (16 + m_constants.FLY_OUT_MENU_ITEM_MARGIN),
									y : y
											- m_constants.FLY_OUT_MENU_EMPTY_MARGIN
											+ this.getFlyoutMenuHeight(height)
											- (16 + m_constants.FLY_OUT_MENU_ITEM_MARGIN)

								});

						++n;
					}
				}

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
				GatewaySymbol.prototype.showPrimitives = function() {
					this.path.show();
					this.refreshFromModelElement();
				};

				/**
				 *
				 */
				GatewaySymbol.prototype.refreshFromModelElement = function() {
					if (this.modelElement.name
							&& this.modelElement.name.trim() != "") {
						this.text.attr("text", this.modelElement.name);
						this.text.show();
					} else {
						this.text.hide();
					}

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
												imageUrl : "plugins/bpm-modeler/images/icons/connect.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : GatewaySymbol_connectToClosure
											},
											{
												imageUrl : "plugins/bpm-modeler/images/icons/activity.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : GatewaySymbol_connectToActivityClosure
											},
											{
												imageUrl : "plugins/bpm-modeler/images/icons/gateway.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : GatewaySymbol_connectToGatewayClosure
											},
											{
												imageUrl : "plugins/bpm-modeler/images/icons/end-event-toolbar.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : GatewaySymbol_connectToEndEventClosure
											} ],
									[
											{
												imageUrl : "plugins/bpm-modeler/images/icons/delete.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : GatewaySymbol_removeClosure
											},
											{
												imageUrl : "plugins/bpm-modeler/images/icons/gateway-xor.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : GatewaySymbol_switchToXorGatewayClosure
											},
											{
												imageUrl : "plugins/bpm-modeler/images/icons/gateway-and.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : GatewaySymbol_switchToAndGatewayClosure
											},
											{
												imageUrl : "plugins/bpm-modeler/images/icons/gateway-or.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : GatewaySymbol_switchToOrGatewayClosure
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
					this.orCircle.hide();
					this.andPath.show();
					this.submitChanges();
				};

				GatewaySymbol.prototype.switchToXorGateway = function() {
					this.modelElement.gatewayType = m_constants.XOR_GATEWAY_TYPE;
					this.andPath.hide();
					this.orCircle.hide();
					this.xorPath.show();
					this.submitChanges();
				};
				
				GatewaySymbol.prototype.switchToOrGateway = function() {
					this.modelElement.gatewayType = m_constants.OR_GATEWAY_TYPE;
					this.andPath.hide();
					this.xorPath.hide();
					this.orCircle.show();
					this.submitChanges();
				};

				/**
				 * Update the modelElement
				 */
				GatewaySymbol.prototype.submitChanges = function() {
					var changes = {
						gatewayType : this.modelElement.gatewayType
					};
					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(
									this.diagram.modelId,
									this.modelElement.oid, changes));
				};

				GatewaySymbol.prototype.showEditable = function() {
					this.performClientSideAdj();
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
										"x" : this.x + this.diagram.getCanvasPosition().left
												- 10,
										"y" : this.y
												+ this.diagram.getCanvasPosition().top
												+ m_constants.GATEWAY_SYMBOL_DEFAULT_HEIGHT
												+ 5
									}).show().trigger("dblclick");

					return this.text;
				};

				GatewaySymbol.prototype.postComplete = function() {
					this.select();
					this.diagram.showEditable(this.text);
				};

				GatewaySymbol.prototype.adjustPrimitivesOnShrink = function() {
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
				this.auxiliaryProperties.callbackScope
						.createAndSubmitDeleteCommand();
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
			
			/**
			 *
			 */
			function GatewaySymbol_switchToOrGatewayClosure() {
				this.auxiliaryProperties.callbackScope.switchToOrGateway();
			}
		});