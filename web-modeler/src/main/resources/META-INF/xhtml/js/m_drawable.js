/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Marc.Gille
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_command", "bpm-modeler/js/m_canvasManager", "bpm-modeler/js/m_messageDisplay"],
		function(m_utils, m_constants, m_commandsController, m_command, m_canvasManager, m_messageDisplay) {
			var PROXIMITY_SENSOR_MARGIN = 20;

			return {
				createDrawable : function() {

					return new Drawable();
				},

				PROXIMITY_SENSOR_MARGIN : PROXIMITY_SENSOR_MARGIN
			};

			/**
			 * Does not have a position and a bounding box.
			 *
			 * Supports Flyout Menu layout.
			 */
			function Drawable() {
				// TODO Put in constants
				var FLY_OUT_MENU_FILL = "#f2f2f2";
				var FLY_OUT_MENU_START_OPACITY = 0;
				var FLY_OUT_MENU_END_OPACITY = 1;
				var FLY_OUT_MENU_CONTENT_MARGIN = 30;
				var FLY_OUT_MENU_EMPTY_MARGIN = 10;
				var FLY_OUT_MENU_ITEM_MARGIN = 5;

				this.lastModifyingUser = null;
				this.properties = [];
				this.selected = false;
				this.primitives = [];
				this.editableTextPrimitives = [];
				this.proximitySensor = null;
				this.flyOutMenuBackground = null;
				this.leftFlyOutMenuItems = [];
				this.rightFlyOutMenuItems = [];
				this.bottomFlyOutMenuItems = []; //botton right aligned (RA)
				this.bottomRAFlyOutMenuItems = [];

				/**
				 *
				 */
				Drawable.prototype.applyChanges = function(changedObject) {
					m_utils.inheritFields(this, changedObject);
					this.applySymbolSpecific(changedObject);
				};

				/**
				 * Should be overridden by symbols if they need to do more
				 * than what's done in #applyChanges function.
				 */
				Drawable.prototype.applySymbolSpecific = function(changedObject) {
				};

				/**
				 * @deprecated
				 */
				Drawable.prototype.submitCreation = function() {
					m_commandsController.submitCommand(m_command.
							createCreateCommand(this.getPath(), this.createTransferObject()));
				};

				/**
				 * @deprecated
				 */
				Drawable.prototype.submitDeletion = function() {
					m_commandsController.submitCommand(m_command.createDeleteCommand(this.getPath(true),
							this.createTransferObject()));
				};

				/**
				 *
				 */
				Drawable.prototype.onCreate = function(transferObject) {
					this.oid = transferObject.oid;

					this.register();

					m_messageDisplay.markModified();
				};

				/**
				 *
				 */
				Drawable.prototype.onUpdate = function(transferObject) {
					m_messageDisplay.markModified();
				};

				/**
				 *
				 */
				Drawable.prototype.onDelete = function(transferObject) {
					m_messageDisplay.markModified();
				};

				/**
				 *
				 */
				Drawable.prototype.addToPrimitives = function(element) {
					this.primitives.push(element);
				};

				Drawable.prototype.removeFromPrimitives = function(element) {
					m_utils.removeItemFromArray(this.primitives, element);
				};
				/**
				 *
				 */
				Drawable.prototype.hoverIn = function() {
					this.showPointerCursor();
				};

				/**
				 *
				 */
				Drawable.prototype.hoverOut = function() {
					this.showDefaultCursor();
				};

				/**
				 *
				 */
				Drawable.prototype.showPointerCursor = function() {
					for ( var n in this.primitives) {
						this.primitives[n].attr("cursor", "pointer");
					}
				};

				/**
				 *
				 */
				Drawable.prototype.showDefaultCursor = function() {
					for ( var n in this.primitives) {
						this.primitives[n].attr("cursor", "default");
					}
				};

				/**
				 *
				 */
				Drawable.prototype.showMoveCursor = function() {
					for ( var n in this.primitives) {
						this.primitives[n].attr("cursor", "move");
					}
				};

				/**
				 *
				 */
				Drawable.prototype.addToEditableTextPrimitives = function(
						element) {
					this.editableTextPrimitives.push(element);

					element.auxiliaryProperties = {
						callbackScope : this
					};

					// Event handling

					if (!this.diagram.process.isReadonly()) {
						element
								.dblclick(Drawable_doubleClickEditableTextPrimitiveClosure);
					}
				};

				Drawable.prototype.createProximitySensor = function() {
					this.proximitySensor = this
							.createProximitySensorPrimitive();

					// Initialize return pointer for closure

					this.proximitySensor.auxiliaryProperties = {
						callbackScope : this
					};

					if (!this.diagram.process.isReadonly()) {
						// this.proximitySensor.mouseover(mouseOverClosure);
						this.proximitySensor.hover(
								Drawable_proximityHoverInClosure,
								Drawable_proximityHoverOutClosure);
					}
				};

				/**
				 * Adjusts all auxiliary graphics elements to changes caused by
				 * drag, textchange etc.
				 */
				Drawable.prototype.adjustGeometry = function() {
					this.refreshFromModelElement();
				};

				/**
				 * Refreshes all graphics elements e.g. text, icons against the
				 * model element properties e.g. names, types.
				 *
				 * To be overloaded for subclasses.
				 */
				Drawable.prototype.refreshFromModelElement = function() {
				};

				Drawable.prototype.adjustProximitySensor = function(x, y,
						width, height) {
					this.proximitySensor.attr({
						x : x - PROXIMITY_SENSOR_MARGIN,
						y : y - PROXIMITY_SENSOR_MARGIN,
						width : width + 2 * PROXIMITY_SENSOR_MARGIN,
						height : height + 2 * PROXIMITY_SENSOR_MARGIN
					});
				};

				/**
				 *
				 */
				Drawable.prototype.showProximitySensor = function() {
					this.proximitySensor.show();
				};

				/**
				 *
				 */
				Drawable.prototype.hideProximitySensor = function() {
					this.proximitySensor.hide();
				};

				/**
				 *
				 */
				Drawable.prototype.adjustFlyOutMenu = function(x, y, width,
						height) {
					this.flyOutMenuBackground.attr({
						'x' : x - FLY_OUT_MENU_CONTENT_MARGIN,
						'y' : y - FLY_OUT_MENU_EMPTY_MARGIN,
						'width' : width + 2 * FLY_OUT_MENU_CONTENT_MARGIN,
						'height' : height + FLY_OUT_MENU_EMPTY_MARGIN
								+ FLY_OUT_MENU_CONTENT_MARGIN
					});

					this.adjustFlyOutMenuItems(x, y, width, height);
				};

				/**
				 *
				 */
				Drawable.prototype.adjustFlyOutMenuItems = function(x, y,
						width, height) {
					var n = 0;

					while (n < this.leftFlyOutMenuItems.length) {
						this.leftFlyOutMenuItems[n].attr({
							x : x - FLY_OUT_MENU_CONTENT_MARGIN
									+ FLY_OUT_MENU_ITEM_MARGIN,
							y : y - FLY_OUT_MENU_EMPTY_MARGIN
									+ FLY_OUT_MENU_ITEM_MARGIN + n
									* (16 + FLY_OUT_MENU_ITEM_MARGIN)
						});

						++n;
					}

					n = 0;

					while (n < this.rightFlyOutMenuItems.length) {
						this.rightFlyOutMenuItems[n].attr({
							x : x + width + FLY_OUT_MENU_CONTENT_MARGIN
									- FLY_OUT_MENU_ITEM_MARGIN - 16,
							y : y - FLY_OUT_MENU_EMPTY_MARGIN
									+ FLY_OUT_MENU_ITEM_MARGIN + n
									* (16 + FLY_OUT_MENU_ITEM_MARGIN)
						});

						++n;
					}

					n = 0;

					while (n < this.bottomFlyOutMenuItems.length) {
						this.bottomFlyOutMenuItems[n].attr({
							x : x - FLY_OUT_MENU_CONTENT_MARGIN
									+ FLY_OUT_MENU_ITEM_MARGIN + n
									* (16 + FLY_OUT_MENU_ITEM_MARGIN),
							y : y + height + FLY_OUT_MENU_CONTENT_MARGIN
									- FLY_OUT_MENU_ITEM_MARGIN - 16
						});

						++n;
					}
					
					n = this.bottomRAFlyOutMenuItems.length - 1;
					while (n >= 0) {
						this.bottomRAFlyOutMenuItems[n].attr({
							x : x + width - FLY_OUT_MENU_EMPTY_MARGIN - n
									* (16 + FLY_OUT_MENU_ITEM_MARGIN),
							y : y + height
									+ FLY_OUT_MENU_CONTENT_MARGIN
									- FLY_OUT_MENU_ITEM_MARGIN - 16
						});
						--n;
					}
				};

				/**
				 *
				 */
				Drawable.prototype.showFlyOutMenu = function() {
					if (this.diagram.currentFlyOutSymbol) {
						return;
					} else if (this.type
							&& (this.isPoolSymbol() || this.type == m_constants.SWIMLANE_SYMBOL)) {
						// do nothing
					} else {
						this.diagram.currentFlyOutSymbol = this;
					}

					this.flyOutMenuBackground.show();
					this.flyOutMenuBackground.animate({
						"fill-opacity" : FLY_OUT_MENU_END_OPACITY
					}, m_constants.DRAWABLE_FLY_OUT_MENU_FADE_TIME, '<');

					var n = 0;

					while (n < this.leftFlyOutMenuItems.length) {
						this.leftFlyOutMenuItems[n].show();
						this.leftFlyOutMenuItems[n].toFront();
						this.leftFlyOutMenuItems[n].animate({
							"fill-opacity" : FLY_OUT_MENU_END_OPACITY
						}, m_constants.DRAWABLE_FLY_OUT_MENU_FADE_TIME, '<');

						++n;
					}

					n = 0;

					while (n < this.rightFlyOutMenuItems.length) {
						this.rightFlyOutMenuItems[n].show();
						this.rightFlyOutMenuItems[n].toFront();
						this.rightFlyOutMenuItems[n].animate({
							"fill-opacity" : FLY_OUT_MENU_END_OPACITY
						}, m_constants.DRAWABLE_FLY_OUT_MENU_FADE_TIME, '>');

						++n;
					}

					n = 0;

					while (n < this.bottomFlyOutMenuItems.length) {
						this.bottomFlyOutMenuItems[n].show();
						this.bottomFlyOutMenuItems[n].toFront();
						this.bottomFlyOutMenuItems[n].animate({
							"fill-opacity" : FLY_OUT_MENU_END_OPACITY
						}, m_constants.DRAWABLE_FLY_OUT_MENU_FADE_TIME, '>');

						++n;
					}
					
					n = 0;
					var length = this.bottomRAFlyOutMenuItems.length;
					while (n < length) {
						this.bottomRAFlyOutMenuItems[n].show();
						this.bottomRAFlyOutMenuItems[n].toFront();
						this.bottomRAFlyOutMenuItems[n].animate({
							"fill-opacity" : FLY_OUT_MENU_END_OPACITY
						}, m_constants.DRAWABLE_FLY_OUT_MENU_FADE_TIME, '>');

						++n;
					}
				};

				/**
				 *
				 */
				Drawable.prototype.hideFlyOutMenu = function() {
					this.flyOutMenuBackground.animate({
						"fill-opacity" : FLY_OUT_MENU_START_OPACITY
					}, m_constants.DRAWABLE_FLY_OUT_MENU_FADE_TIME, '<');
					this.flyOutMenuBackground.hide();

					var n = 0;

					while (n < this.leftFlyOutMenuItems.length) {
						this.leftFlyOutMenuItems[n].animate({
							"fill-opacity" : FLY_OUT_MENU_START_OPACITY
						}, m_constants.DRAWABLE_FLY_OUT_MENU_FADE_TIME, '>');
						this.leftFlyOutMenuItems[n].hide();

						++n;
					}

					n = 0;

					while (n < this.rightFlyOutMenuItems.length) {
						this.rightFlyOutMenuItems[n].animate({
							"fill-opacity" : FLY_OUT_MENU_START_OPACITY
						}, m_constants.DRAWABLE_FLY_OUT_MENU_FADE_TIME, '>');
						this.rightFlyOutMenuItems[n].hide();

						++n;
					}

					n = 0;

					while (n < this.bottomFlyOutMenuItems.length) {
						this.bottomFlyOutMenuItems[n].animate({
							"fill-opacity" : FLY_OUT_MENU_START_OPACITY
						}, m_constants.DRAWABLE_FLY_OUT_MENU_FADE_TIME, '>');
						this.bottomFlyOutMenuItems[n].hide();

						++n;
					}
					
					n = 0;
					var length = this.bottomRAFlyOutMenuItems.length; 
					while (n < length) {
						this.bottomRAFlyOutMenuItems[n].animate({
							"fill-opacity" : FLY_OUT_MENU_START_OPACITY
						}, m_constants.DRAWABLE_FLY_OUT_MENU_FADE_TIME, '>');
						this.bottomRAFlyOutMenuItems[n].hide();

						++n;
					}
					
					if (this.diagram.currentFlyOutSymbol
							&& this.diagram.currentFlyOutSymbol.oid == this.oid) {
						this.diagram.currentFlyOutSymbol = null;
					}

				};

				/**
				 *
				 */
				Drawable.prototype.addFlyOutMenuItems = function(left, right,
						bottom, bottomRA) {

					this.leftFlyOutMenuItems = new Array();

					var n = 0;

					while (n < left.length) {
						this.leftFlyOutMenuItems[n] = this
								.createFlyOutMenuItem(left[n].imageUrl,
										left[n].imageWidth,
										left[n].imageHeight,
										left[n].clickHandler);

						++n;
					}

					this.rightFlyOutMenuItems = new Array();

					n = 0;

					while (n < right.length) {
						this.rightFlyOutMenuItems[n] = this
								.createFlyOutMenuItem(right[n].imageUrl,
										right[n].imageWidth,
										right[n].imageHeight,
										right[n].clickHandler);

						++n;
					}

					this.bottomFlyOutMenuItems = new Array();

					n = 0;

					while (n < bottom.length) {
						this.bottomFlyOutMenuItems[n] = this
								.createFlyOutMenuItem(bottom[n].imageUrl,
										bottom[n].imageWidth,
										bottom[n].imageHeight,
										bottom[n].clickHandler);

						++n;
					}
					
					this.bottomRAFlyOutMenuItems = new Array();

					if (bottomRA) {
						n = 0;
						var length = bottomRA.length;
						while (n < length) {
							this.bottomRAFlyOutMenuItems[n] = this
									.createFlyOutMenuItem(bottomRA[n].imageUrl,
											bottomRA[n].imageWidth,
											bottomRA[n].imageHeight,
											bottomRA[n].clickHandler);

							++n;
						}
					}
				};

				Drawable.prototype.createFlyOutMenuItem = function(imageUrl,
						imageWidth, imageHeight, clickHandler) {
					var item = this.diagram.canvasManager.drawImageAt(imageUrl, 0, 0,
							imageWidth, imageHeight);

					item.attr({
						"fill-opacity" : FLY_OUT_MENU_START_OPACITY
					});

					item.auxiliaryProperties = {
						callbackScope : this
					};

					if (!this.diagram.process.isReadonly()) {
						var self = this;
						item.click(function() {
							if (self.diagram.mode === self.diagram.NORMAL_MODE) {
								clickHandler.call(this);								
							}
						});
						item.hover(Drawable_hoverInFlyMenuItemClosure,
								Drawable_hoverOutFlyMenuItemClosure);
						item.hover(Drawable_hoverInFlyMenuItemClosure,
								Drawable_hoverOutFlyMenuItemClosure);
					}

					return item;
				};

				/**
				 *
				 */
				Drawable.prototype.proximityHoverIn = function(event) {
					this.showFlyOutMenu();
				};

				/**
				 *
				 */
				Drawable.prototype.proximityHoverOut = function(event) {
					this.hideFlyOutMenu();
				};

				/**
				 *
				 */
				Drawable.prototype.removeProximitySensor = function() {
					if (this.proximitySensor) {
						this.proximitySensor.remove();
					}
				};

				/**
				 *
				 */
				Drawable.prototype.removeFlyOutMenu = function() {
					if (this.flyOutMenuBackground) {
						this.flyOutMenuBackground.remove();
					}

					var n = 0;

					while (n < this.leftFlyOutMenuItems.length) {
						this.leftFlyOutMenuItems[n].remove();

						++n;
					}

					n = 0;

					while (n < this.rightFlyOutMenuItems.length) {
						this.rightFlyOutMenuItems[n].remove();

						++n;
					}

					n = 0;

					while (n < this.bottomFlyOutMenuItems.length) {
						this.bottomFlyOutMenuItems[n].remove();

						++n;
					}
					
					n = 0;

					while (n < this.bottomRAFlyOutMenuItems.length) {
						this.bottomRAFlyOutMenuItems[n].remove();

						++n;
					}
					
					if (this.diagram.currentFlyOutSymbol
							&& this.diagram.currentFlyOutSymbol.oid == this.oid) {
						this.diagram.currentFlyOutSymbol = null;
					}
				};

				/**
				 *
				 */
				Drawable.prototype.deselect = function() {
					this.selected = false;
				};

				/**
				 *
				 */
				Drawable.prototype.doubleClickEditableTextPrimitive = function(
						element) {
					this.hideFlyOutMenu();
					this.diagram.showEditable(element);
				};

				/**
				 *
				 */
				Drawable.prototype.showDashboard = function(dashboardContent) {
					this.diagram.canvasManager.drawRectangle(
							this.getDashboardX(), this.getDashboardY(), 220,
							120, {
								"stroke" : "#bbbbbb",
								"stroke-width" : 2.0,
								"fill" : "#fcf7bf",
								"r" : 3.0,
								"fill-opacity" : 0.5
							}).show();

					var yOffset = 15;

					for ( var contentItem in dashboardContent) {
						if (dashboardContent[contentItem].type == "valueList") {
							this.diagram.canvasManager
									.drawTextNode(this.getDashboardX() + 10,
											this.getDashboardY() + yOffset,
											dashboardContent[contentItem].title)
									.attr(
											{
												"text-anchor" : "start",
												"font-family" : m_constants.DEFAULT_FONT_FAMILY,
												"font-weight" : "bold",
												"font-size" : m_constants.DEFAULT_FONT_SIZE
											}).show();

							yOffset += 25;

							var attributes = dashboardContent[contentItem].attributes;
							for ( var attribute in attributes) {
								this.diagram.canvasManager
										.drawTextNode(
												this.getDashboardX() + 10,
												this.getDashboardY() + yOffset,
												attribute + ":")
										.attr(
												{
													"text-anchor" : "start",
													"font-family" : m_constants.DEFAULT_FONT_FAMILY,
													"font-weight" : "bold",
													"font-size" : m_constants.DEFAULT_FONT_SIZE - 1
												}).show();
								this.diagram.canvasManager
										.drawTextNode(
												this.getDashboardX() + 160,
												this.getDashboardY() + yOffset,
												attributes[attribute])
										.attr(
												{
													"text-anchor" : "start",
													"font-family" : m_constants.DEFAULT_FONT_FAMILY,
													"font-size" : m_constants.DEFAULT_FONT_SIZE
												}).show();

								yOffset += 20;
							}
						} else if (dashboardContent[contentItem].type == "plot") {
							this.diagram.canvasManager
									.drawTextNode(this.getDashboardX() + 10,
											this.getDashboardY() + yOffset,
											dashboardContent[contentItem].title)
									.attr(
											{
												"text-anchor" : "start",
												"font-family" : m_constants.DEFAULT_FONT_FAMILY,
												"font-weight" : "bold",
												"font-size" : m_constants.DEFAULT_FONT_SIZE
											}).show();

							yOffset += 100;

							// Draw axis

							var pathString = "";

							pathString += "M" + (this.getDashboardX() + 10) + " " + (this.getDashboardY() + yOffset);
							pathString += "L" + (this.getDashboardX() + 10) + " " + (this.getDashboardY() + yOffset - 80);

							this.diagram.canvasManager.drawPath(pathString, {
								"arrow-end" : "block-wide-long",
								"stroke" : "#333333",
								"stroke-width" : 1.0
							}).show();

							pathString = "";

							pathString += "M" + (this.getDashboardX() + 10) + " " + (this.getDashboardY() + yOffset);
							pathString += "L" + (this.getDashboardX() + 210) + " " + (this.getDashboardY() + yOffset);

							this.diagram.canvasManager.drawPath(pathString, {
								"arrow-end" : "block-wide-long",
								"stroke" : "#333333",
								"stroke-width" : 1.0
							}).show();

							var data = dashboardContent[contentItem].data;

							pathString = "";

							for ( var n in data) {
								if (n == 0) {
									pathString += ("M" + (this.getDashboardX() + 10 + data[n][0]) + " " + (this.getDashboardY() + yOffset - data[n][1]));
								} else {
									pathString += ("L" + (this.getDashboardX() + 10 + data[n][0]) + " " + (this.getDashboardY() + yOffset - data[n][1]));
								}
							}

							this.diagram.canvasManager.drawPath(pathString, {
								"stroke" : "red",
								"stroke-width" : 1.5
							}).show();
						}

					}
				};
			}

			function Drawable_proximityHoverInClosure(event) {
				this.auxiliaryProperties.callbackScope.proximityHoverIn(event);
			}

			function Drawable_proximityHoverOutClosure(event) {
				this.auxiliaryProperties.callbackScope.proximityHoverOut(event);
			}

			function Drawable_hoverInFlyMenuItemClosure(event) {
				this.attr({
					"fill" : "white",
					"fill-opacity" : 0
				});
				this.auxiliaryProperties.callbackScope.showFlyOutMenu(event);
			}

			function Drawable_hoverOutFlyMenuItemClosure(event) {
				if (this.removed) {
					return;
				}
				this.attr({
					"fill" : "white",
					"fill-opacity" : 1
				});
				if (this.auxiliaryProperties.callbackScope.type != null) {
					// check if mouse cursor is outside proximity margin
					if (this.auxiliaryProperties.callbackScope
							.validateProximity(event)) {
						return;
					}
				}
				this.auxiliaryProperties.callbackScope.hideFlyOutMenu(event);
			}

			/**
			 *
			 */
			function Drawable_doubleClickEditableTextPrimitiveClosure() {
				this.auxiliaryProperties.callbackScope
						.doubleClickEditableTextPrimitive(this);
			}
		});