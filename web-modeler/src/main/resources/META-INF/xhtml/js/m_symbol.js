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
		[ "m_utils", "m_constants", "m_messageDisplay", "m_canvasManager",
				"m_drawable", "m_commandsController", "m_command",
				"m_propertiesPanel", "m_modelerUtils" ],
		function(m_utils, m_constants, m_messageDisplay, m_canvasManager,
				m_drawable, m_commandsController, m_command, m_propertiesPanel,
				m_modelerUtils) {

			return {
				createSymbol : function() {
					return new Symbol();
				},
				createAnchorPoint : function(symbol, orientation) {
					return new AnchorPoint(symbol, orientation);
				}
			};

			/**
			 *
			 */
			function Symbol() {
				var drawable = m_drawable.createDrawable();

				m_utils.inheritFields(this, drawable);
				m_utils.inheritMethods(Symbol.prototype, drawable);

				this.state = m_constants.SYMBOL_CREATED_STATE;
				this.diagram = null;
				this.parentSymbol = null;
				this.anchorPoints = new Array();
				this.connections = [];
				this.leftSelectFrame = null;
				this.topSelectFrame = null;
				this.rightSelectFrame = null;
				this.bottomSelectFrame = null;
				this.dragStartX = 0;
				this.dragStartY = 0;
				this.visible = true;
				this.serverSideCoordinates = null;

				// Method initialization

				/**
				 *
				 */
				Symbol.prototype.toString = function() {
					return "Lightdust.Symbol";
				};

				/**
				 *
				 */
				Symbol.prototype.getProperties = function() {
					this.properties.dimensions = {
						"x" : this.x,
						"y" : this.y,
						"width" : this.width,
						"height" : this.height
					};

					return this.properties;
				};

				/**
				 *
				 */
				Symbol.prototype.initialize = function(x, y) {
					this.prepare(x, y);
					this.complete();
				};

				Symbol.prototype.prepareTransferObject = function(
						transferObject) {
					transferObject.diagram = null;
					transferObject.connections = null;
					transferObject.anchorPoints = null;
					transferObject.parentSymbol = null;
					transferObject.topSelectFrame = null;
					transferObject.rightSelectFrame = null;
					transferObject.bottomSelectFrame = null;
					transferObject.leftSelectFrame = null;
					transferObject.flyOutMenuBackground = null;
					transferObject.bottomFlyOutMenuItems = null;
					transferObject.rightFlyOutMenuItems = null;
					transferObject.primitives = null;
					transferObject.editableTextPrimitives = null;
					transferObject.proximitySensor = null;
					transferObject.propertiesPanel = null;

					return transferObject;
				}

				/**
				 *
				 */
				Symbol.prototype.getXCenter = function() {
					return this.x + 0.5 * this.width;
				};

				/**
				 *
				 */
				Symbol.prototype.getYCenter = function() {
					return this.y + 0.5 * this.height;
				};

				/**
				 *
				 */
				Symbol.prototype.prepare = function(x, y) {
					this.x = x;
					this.y = y;

					this.prepareNoPosition();
				};

				/**
				 *
				 */
				Symbol.prototype.prepareNoPosition = function() {
					this.createPrimitives();
					this.createAnchorPoints();
					this.refreshFromModelElement();
					this.adjustAnchorPoints();
					this.initializePrepareEventHandling();

					this.state = m_constants.SYMBOL_PREPARED_STATE;
				};

				Symbol.prototype.isPrepared = function() {
					return this.state == m_constants.SYMBOL_PREPARED_STATE;
				};

				Symbol.prototype.isCompleted = function() {
					return this.state == m_constants.SYMBOL_COMPLETED_STATE;
				};

				/**
				 * Store the server side co-ordinates, required to move the
				 * symbols from original point when some lane is minimized
				 */
				Symbol.prototype.updateServerSideCoordinates = function() {
					this.serverSideCoordinates = {
						x : this.x,
						y : this.y
					};
				};

				/**
				 *
				 */
				Symbol.prototype.isContainerSymbol = function() {
					return false;
				};

				/**
				 *
				 */
				Symbol.prototype.requiresParentSymbol = function() {
					return true;
				};

				/**
				 *
				 */
				Symbol.prototype.completeNoTransfer = function() {
					m_messageDisplay.clear();
					if (this.requiresParentSymbol()
							&& this.parentSymbol == null) {
						this.parentSymbol = this.diagram.poolSymbol
								.findContainerSymbol(this.getXCenter(), this
										.getYCenter());
						// If tried to add symbol outside Lane or when lane is
						// minimized symbols cannot be added
						if (this.parentSymbol == null
								|| this.parentSymbol.minimized) {
							// TODO May make exception
							m_messageDisplay
									.showErrorMessage("Symbol can only be dropped inside a expanded lane.");
							this.diagram.hideSnapLines(this);
							return;
						} else {
							this.parentSymbol.containedSymbols.push(this);
							this.parentSymbolId = this.parentSymbol.id;

							this.diagram.snapSymbol(this);
							this.parentSymbol.adjustToSymbolBoundaries();
						}
					}

					this.diagram.symbols.push(this);

					// Create auxiliary graphics

					this.createProximitySensor();
					this.createSelectFrame();
					this.createFlyOutMenuBackground();
					this.createFlyOutMenu();

					this.initializeDefaultEventHandling();
					this.initializeEventHandling();
					this.primitivesToFront();
					this.createChildSymbols();

					this.state = m_constants.SYMBOL_COMPLETED_STATE;

					this.recalculateBoundingBox();
					this.adjustGeometry();
					this.hideAnchorPoints();
					this.hideFlyOutMenu();
					this.deselect();

					this.onComplete();
				};

				/**
				 * Allows subclasses to perform specific operations.
				 */
				Symbol.prototype.onComplete = function() {
				};

				/**
				 * sync : Synchronous AJAX call is made, if set(needed in
				 * scenario like creating symbol from flyout menu where symbol
				 * should be created before createConnection)
				 */
				Symbol.prototype.complete = function(sync) {
					this.completeNoTransfer(this);
					if (this.isCompleted()) {
						// If any lane is minimized, symbolXOffset is added to
						// store correct co-ord,
						// as of state when all lane will be maximized
						if (this.parentSymbol.symbolXOffset) {
							this.x += this.parentSymbol.symbolXOffset;
						}
						this.createAndSubmitCreateCommand(sync);
					}

					if (this.requiresParentSymbol()) {
						// TODO Needs to be called on create, otherwise it may
						// be aparllel calls
						// this.diagram.submitUpdate();
					}
				};

				/**
				 * generate command type based on i/p . i.e create,remove
				 */
				Symbol.prototype.getCommandIdForNode = function(str) {
					var commandType = null;
					if (this.type == m_constants.ACTIVITY_SYMBOL) {
						commandType = "activitySymbol." + str;
					} else if (this.type == m_constants.GATEWAY_SYMBOL) {
						commandType = "gateSymbol." + str;
					} else if (this.type == m_constants.EVENT_SYMBOL) {
						commandType = "eventSymbol." + str;
					} else if (this.type == m_constants.DATA_SYMBOL) {
						commandType = "dataSymbol." + str;
					} else if (m_constants.SWIMLANE_SYMBOL.match(this.type)) {
						commandType = "swimlaneSymbol." + str;
					} else if (m_constants.ANNOTATION_SYMBOL.match(this.type)) {
						commandType = "annotationSymbol." + str;
					}

					return commandType;
				}
				/**
				 *
				 * Registers symbol in specific lists in the diagram.
				 */
				Symbol.prototype.register = function() {
					// Do nothing
				};

				/**
				 *
				 */
				Symbol.prototype.createPrimitives = function() {
					// Nothing to be done
				};

				/**
				 *
				 */
				Symbol.prototype.createChildSymbols = function() {
					// Nothing to be done
				};

				/**
				 *
				 */
				Symbol.prototype.createAnchorPoints = function() {
					this.anchorPoints[0] = new AnchorPoint(this, 0);
					this.anchorPoints[1] = new AnchorPoint(this, 1);
					this.anchorPoints[2] = new AnchorPoint(this, 2);
					this.anchorPoints[3] = new AnchorPoint(this, 3);
				};

				/**
				 *
				 */
				Symbol.prototype.showAnchorPoints = function() {
					for ( var n in this.anchorPoints) {
						this.anchorPoints[n].show();
					}
				};

				/**
				 *
				 */
				Symbol.prototype.hideAnchorPoints = function() {
					for ( var n in this.anchorPoints) {
						this.anchorPoints[n].hide();
					}
				};

				/**
				 *
				 */
				Symbol.prototype.getClosestAnchorPoint = function(x, y,
						skipScrollAdjustment) {
					// Skips scroll adjustment if skipScrollAdjustment is set.
					if (!skipScrollAdjustment) {
						var scrollPos = m_modelerUtils
								.getModelerScrollPosition();
						x += scrollPos.left;
						y += scrollPos.top;
					}

					var distance = this.width + this.height;
					var resultAnchorPoint = null;

					for ( var n in this.anchorPoints) {
						var currentDistance = Math.sqrt(Math.pow(
								this.anchorPoints[n].x - x, 2)
								+ Math.pow(this.anchorPoints[n].y - y, 2));
						if (currentDistance < distance) {
							distance = currentDistance;
							resultAnchorPoint = this.anchorPoints[n];
						}
					}

					return resultAnchorPoint;
				};

				/**
				 *
				 */
				Symbol.prototype.showPrimitives = function() {
					for ( var n in this.primitives) {
						this.primitives[n].show();
					}

				};

				/**
				 *
				 */
				Symbol.prototype.hidePrimitives = function() {
					for ( var n in this.primitives) {
						this.primitives[n].hide();
					}
				};

				/**
				 *
				 */
				Symbol.prototype.hide = function() {
					this.visible = false;
					this.hideConnections();
					this.hidePrimitives();
					this.hideFlyOutMenu();
					this.hideProximitySensor();
					// this.removeAnchorPoints();

				};

				/**
				 *
				 */
				Symbol.prototype.show = function() {
					this.visible = true;
					this.refreshFromModelElement();
					this.showPrimitives();
					this.showConnections();
					this.showProximitySensor();
				};

				/**
				 *
				 */
				Symbol.prototype.hideConnections = function() {
					var n = 0;
					for ( var n in this.connections) {
						var conn = this.connections[n];
						var connectionStartLane = conn.fromAnchorPoint.symbol.parentSymbol;
						var connectionToLane = conn.toAnchorPoint.symbol.parentSymbol;
						// connections resides in current lane
						if (connectionStartLane.id == connectionToLane.id) {
							this.connections[n].hide();
						} else {
							// from-anchor point adjustment
							if (this.parentSymbol.id == connectionStartLane.id) {
								// When connection is from left to right
								if (conn.toAnchorPoint.x > conn.fromAnchorPoint.x) {
									conn.fromAnchorPoint.cacheX = conn.fromAnchorPoint.x;
									conn.fromAnchorPoint.x = this.parentSymbol.x
											+ this.parentSymbol.width;

									this.cacheAnchorPointAndAdjust(
											conn.fromAnchorPoint,
											conn.toAnchorPoint);
									conn.fromAnchorPoint.orientation = m_constants.EAST;

								} else {
									// When connection is from right to left
									conn.fromAnchorPoint.cacheX = conn.fromAnchorPoint.x;
									conn.fromAnchorPoint.x = this.parentSymbol.x;
									this.cacheAnchorPointAndAdjust(
											conn.fromAnchorPoint,
											conn.toAnchorPoint);
									conn.fromAnchorPoint.orientation = m_constants.WEST;
								}

							}
							// to-anchor point adjustment
							else {
								// When connection is from right to left
								if (conn.fromAnchorPoint.x > conn.toAnchorPoint.x) {
									conn.toAnchorPoint.cacheX = conn.toAnchorPoint.x;
									conn.toAnchorPoint.x = this.parentSymbol.x
											+ this.parentSymbol.width;

									this.cacheAnchorPointAndAdjust(
											conn.toAnchorPoint,
											conn.fromAnchorPoint);
									conn.toAnchorPoint.orientation = m_constants.EAST;

								} else {
									conn.toAnchorPoint.cacheX = conn.toAnchorPoint.x;
									conn.toAnchorPoint.x = connectionToLane.x;

									this.cacheAnchorPointAndAdjust(
											conn.toAnchorPoint,
											conn.fromAnchorPoint);
									conn.toAnchorPoint.orientation = m_constants.WEST;

								}
							}
							this.connections[n].reroute();
						}
					}
				};

				/**
				 *
				 */
				Symbol.prototype.cacheAnchorPointAndAdjust = function(
						currentAnchorPt, targetAnchorPt) {
					// Cache the current orientation for the connection
					if (currentAnchorPt.cacheOrientation == null) {
						currentAnchorPt.cacheOrientation = currentAnchorPt.orientation;
						if (!currentAnchorPt.symbol.visible
								&& !targetAnchorPt.symbol.visible) {
							currentAnchorPt.cacheY = currentAnchorPt.y;
							currentAnchorPt.y = targetAnchorPt.y;
						} else {
							// When the to orientation is south, we need to
							// move the connection down
							if (targetAnchorPt.orientation == m_constants.SOUTH) {
								currentAnchorPt.cacheY = currentAnchorPt.y;
								currentAnchorPt.y = targetAnchorPt.y
										+ m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH;
							} else if (targetAnchorPt.orientation == m_constants.NORTH) {
								currentAnchorPt.cacheY = currentAnchorPt.y;
								currentAnchorPt.y = targetAnchorPt.y
										- m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH;
							} else {
								currentAnchorPt.cacheY = currentAnchorPt.y;
								currentAnchorPt.y = targetAnchorPt.y;
							}
						}

					}
				}

				/**
				 *
				 */
				Symbol.prototype.showConnections = function() {
					var n = 0;
					for ( var n in this.connections) {
						if (this.connections[n].fromAnchorPoint.cacheOrientation != null
								&& this.connections[n].fromAnchorPoint.symbol.visible) {
							this.connections[n].fromAnchorPoint.orientation = this.connections[n].fromAnchorPoint.cacheOrientation;
							if (this.connections[n].fromAnchorPoint.cacheX)
								this.connections[n].fromAnchorPoint.x = this.connections[n].fromAnchorPoint.cacheX;
							if (this.connections[n].fromAnchorPoint.cacheY)
								this.connections[n].fromAnchorPoint.y = this.connections[n].fromAnchorPoint.cacheY;
							this.connections[n].fromAnchorPoint.cacheOrientation = null;
						}
						if (this.connections[n].toAnchorPoint.cacheOrientation != null
								&& this.connections[n].toAnchorPoint.symbol.visible) {
							this.connections[n].toAnchorPoint.orientation = this.connections[n].toAnchorPoint.cacheOrientation;
							if (this.connections[n].toAnchorPoint.cacheX)
								this.connections[n].toAnchorPoint.x = this.connections[n].toAnchorPoint.cacheX;
							if (this.connections[n].toAnchorPoint.cacheY)
								this.connections[n].toAnchorPoint.y = this.connections[n].toAnchorPoint.cacheY;
							this.connections[n].toAnchorPoint.cacheOrientation = null;
						}
						this.connections[n].reroute();
						this.connections[n].show();
					}
				};

				/**
				 *
				 */
				Symbol.prototype.primitivesToFront = function() {
					for ( var n in this.primitives) {
						this.primitives[n].toFront();
					}
				};

				/**
				 *
				 */
				Symbol.prototype.adjustAnchorPoints = function() {
					this.anchorPoints[0].adjust();
					this.anchorPoints[1].adjust();
					this.anchorPoints[2].adjust();
					this.anchorPoints[3].adjust();
				};

				/**
				 * Adjusts all graphics elements to changes in x, y, width,
				 * height.
				 */
				Symbol.prototype.adjustGeometry = function() {
					this.adjustPrimitives();
					this.adjustChildSymbols();
					this.adjustAuxiliaryElements();
				};

				/**
				 *
				 */
				Symbol.prototype.adjustAuxiliaryElements = function() {
					if (this.visible) {
						this.adjustAnchorPoints();
					}
					this.adjustSelectFrame();
					this.adjustProximitySensor(this.x, this.y, this.width,
							this.height);
					this.adjustFlyOutMenu(this.x, this.y, this.width,
							this.height);
				};

				/**
				 *
				 */
				Symbol.prototype.adjustChildSymbols = function() {
				};

				/**
				 *
				 */
				Symbol.prototype.adjustSelectFrame = function() {
					this.leftSelectFrame.attr("path", this
							.getLeftSelectFramePath());
					this.topSelectFrame.attr("path", this
							.getTopSelectFramePath());
					this.rightSelectFrame.attr("path", this
							.getRightSelectFramePath());
					this.bottomSelectFrame.attr("path", this
							.getBottomSelectFramePath());
				};

				/**
				 *
				 */
				Symbol.prototype.isInBoundingBox = function(xClick, yClick) {
					return this.x <= xClick && this.x + this.width >= xClick
							&& this.y <= yClick
							&& this.y + this.height >= yClick;
				};

				/**
				 *
				 */
				Symbol.prototype.isInRectangle = function(x, y, width, height) {
					return x <= this.x && x + width >= this.x + this.width
							&& y <= this.y
							&& y + height >= this.y + this.height;
				};

				/**
				 *
				 */
				Symbol.prototype.createProximitySensorPrimitive = function() {
					return m_canvasManager.drawRectangle(this.x
							- m_drawable.PROXIMITY_SENSOR_MARGIN, this.y
							- m_drawable.PROXIMITY_SENSOR_MARGIN, this.width
							+ 2 * m_drawable.PROXIMITY_SENSOR_MARGIN,
							this.height + 2
									* m_drawable.PROXIMITY_SENSOR_MARGIN, {
								"stroke" : "white",
								"stroke-width" : 0,
								"fill" : "white",
								"fill-opacity" : 0,
								"r" : 0
							});
				};

				/**
				 *
				 */
				Symbol.prototype.createSelectFrame = function() {
					this.leftSelectFrame = m_canvasManager.drawPath(this
							.getLeftSelectFramePath(), {
						"stroke" : m_constants.DATA_FLOW_COLOR,
						"stroke-width" : m_constants.SELECT_FRAME_STROKE_WIDTH,
						"stroke-dasharray" : m_constants.SELECT_FRAME_DASHARRAY
					});
					this.leftSelectFrame.auxiliaryProperties = {
						callbackScope : this
					};

					this.leftSelectFrame.mouseover(function() {
						this.attr("cursor", "w-resize");
					});
					this.leftSelectFrame.drag(Symbol_stretchLeftClosure,
							Symbol_stretchStartClosure,
							Symbol_stretchStopClosure);
					this.leftSelectFrame.hide();

					this.topSelectFrame = m_canvasManager.drawPath(this
							.getTopSelectFramePath(), {
						"stroke" : m_constants.DATA_FLOW_COLOR,
						"stroke-width" : m_constants.SELECT_FRAME_STROKE_WIDTH,
						"stroke-dasharray" : m_constants.SELECT_FRAME_DASHARRAY
					});
					this.topSelectFrame.auxiliaryProperties = {
						callbackScope : this
					};

					this.topSelectFrame.mouseover(function() {
						this.attr("cursor", "n-resize");
					});
					this.topSelectFrame.drag(Symbol_stretchTopClosure,
							Symbol_stretchStartClosure,
							Symbol_stretchStopClosure);
					this.topSelectFrame.hide();

					this.rightSelectFrame = m_canvasManager.drawPath(this
							.getRightSelectFramePath(), {
						"stroke" : m_constants.DATA_FLOW_COLOR,
						"stroke-width" : m_constants.SELECT_FRAME_STROKE_WIDTH,
						"stroke-dasharray" : m_constants.SELECT_FRAME_DASHARRAY
					});
					this.rightSelectFrame.auxiliaryProperties = {
						callbackScope : this
					};

					this.rightSelectFrame.mouseover(function() {
						this.attr("cursor", "e-resize");
					});
					this.rightSelectFrame.drag(Symbol_stretchRightClosure,
							Symbol_stretchStartClosure,
							Symbol_stretchStopClosure);
					this.rightSelectFrame.hide();

					this.bottomSelectFrame = m_canvasManager.drawPath(this
							.getBottomSelectFramePath(), {
						"stroke" : m_constants.DATA_FLOW_COLOR,
						"stroke-width" : m_constants.SELECT_FRAME_STROKE_WIDTH,
						"stroke-dasharray" : m_constants.SELECT_FRAME_DASHARRAY
					});
					this.bottomSelectFrame.auxiliaryProperties = {
						callbackScope : this
					};

					this.bottomSelectFrame.mouseover(function() {
						this.attr("cursor", "s-resize");
					});
					this.bottomSelectFrame.drag(Symbol_stretchBottomClosure,
							Symbol_stretchStartClosure,
							Symbol_stretchStopClosure);
					this.bottomSelectFrame.hide();
				};

				/**
				 *
				 */
				Symbol.prototype.getLeftSelectFramePath = function() {
					return "M"
							+ (this.x - m_constants.SELECT_FRAME_MARGIN)
							+ " "
							+ (this.y - m_constants.SELECT_FRAME_MARGIN)
							+ "L"
							+ (this.x - m_constants.SELECT_FRAME_MARGIN)
							+ " "
							+ (this.y + this.height + m_constants.SELECT_FRAME_MARGIN);
				};

				/**
				 *
				 */
				Symbol.prototype.getTopSelectFramePath = function() {
					return "M"
							+ (this.x - m_constants.SELECT_FRAME_MARGIN)
							+ " "
							+ (this.y - m_constants.SELECT_FRAME_MARGIN)
							+ "L"
							+ (this.x + this.width + m_constants.SELECT_FRAME_MARGIN)
							+ " " + (this.y - m_constants.SELECT_FRAME_MARGIN);
				};

				/**
				 *
				 */
				Symbol.prototype.getRightSelectFramePath = function() {
					return "M"
							+ (this.x + this.width + m_constants.SELECT_FRAME_MARGIN)
							+ " "
							+ (this.y - m_constants.SELECT_FRAME_MARGIN)
							+ "L"
							+ (this.x + this.width + m_constants.SELECT_FRAME_MARGIN)
							+ " "
							+ (this.y + this.height + m_constants.SELECT_FRAME_MARGIN);
				};

				/**
				 *
				 */
				Symbol.prototype.getBottomSelectFramePath = function() {
					return "M"
							+ (this.x - m_constants.SELECT_FRAME_MARGIN)
							+ " "
							+ (this.y + this.height + m_constants.SELECT_FRAME_MARGIN)
							+ "L"
							+ (this.x + this.width + m_constants.SELECT_FRAME_MARGIN)
							+ " "
							+ (this.y + this.height + m_constants.SELECT_FRAME_MARGIN);
				};

				/**
				 *
				 */
				Symbol.prototype.createFlyOutMenuBackground = function(x, y,
						height, width) {
					this.flyOutMenuBackground = m_canvasManager
							.drawRectangle(
									this.x,
									this.y,
									this.width,
									this.height,
									{
										"stroke" : m_constants.FLY_OUT_MENU_STROKE,
										"stroke-width" : m_constants.FLY_OUT_MENU_STROKE_WIDTH,
										"fill" : m_constants.FLY_OUT_MENU_FILL,
										"fill-opacity" : m_constants.FLY_OUT_MENU_START_OPACITY,
										"r" : 3
									});

					// Initialize return pointer for closure

					this.flyOutMenuBackground.auxiliaryProperties = {
						callbackScope : this
					};

					this.flyOutMenuBackground.hover(
							Symbol_hoverInFlyOutMenuClosure,
							Symbol_hoverOutFlyOutMenuClosure);
				};

				/**
				 *
				 */
				Symbol.prototype.showSelectFrame = function() {
					this.leftSelectFrame.show();
					this.topSelectFrame.show();
					this.rightSelectFrame.show();
					this.bottomSelectFrame.show();
				};

				/**
				 *
				 */
				Symbol.prototype.hideSelectFrame = function() {
					this.leftSelectFrame.hide();
					this.topSelectFrame.hide();
					this.rightSelectFrame.hide();
					this.bottomSelectFrame.hide();
				};

				/**
				 *
				 */
				Symbol.prototype.move = function(x, y) {
					this.moveBy(x - (this.x + (this.width / 2)), y
							- (this.y + (this.height / 2)));
				};

				/**
				 *
				 */
				Symbol.prototype.moveTo = function(x, y) {
					this.x = x;
					this.y = y;
					this.moveBy(0, 0);
				};

				/**
				 *
				 */
				Symbol.prototype.moveBy = function(dX, dY) {
					this.x = this.x + dX;
					this.y = this.y + dY;

					// Move all primitives

					this.adjustPrimitives();

					if (this.isCompleted()) {
						this.adjustGeometry();
					} else {
						// When lane is not minimized
						if (this.visible) {
							this.adjustAnchorPoints();
						}
					}

					// Reroute connections - even in prepared state
					for ( var connection in this.connections) {
						// normal mode when swimlane is not minimized
						if (this.visible) {
							this.connections[connection].reroute();
						} else {
							// If connection is visible and swimlane is minimize
							if (this.connections[connection].visible
									&& this.parentSymbol.minimized) {
								var fromAnchorPt = this.connections[connection].fromAnchorPoint;
								var toAnchorPt = this.connections[connection].toAnchorPoint;
								var fromAnchorParentLaneX = fromAnchorPt.symbol.parentSymbol.x;
								var toAnchorParentLaneX = toAnchorPt.symbol.parentSymbol.x;
								// When user minimize/maximise other lane this
								// lane connector needs to be adjusted
								if (!fromAnchorPt.symbol.visible) {
									if (fromAnchorParentLaneX < toAnchorParentLaneX) {
										this.connections[connection].fromAnchorPoint.x = fromAnchorParentLaneX
												+ fromAnchorPt.symbol.parentSymbol.width;
									} else {
										this.connections[connection].fromAnchorPoint.x = fromAnchorParentLaneX;
									}

								}
								// This check is required as movement is not
								// required when connecting Lane is visible
								if (!toAnchorPt.symbol.visible) {
									if (toAnchorParentLaneX > fromAnchorParentLaneX) {
										this.connections[connection].toAnchorPoint.x = toAnchorParentLaneX;
									} else {
										this.connections[connection].toAnchorPoint.x = toAnchorParentLaneX
												+ toAnchorPt.symbol.parentSymbol.width;
									}

								}
								this.connections[connection].reroute();
							}
						}
					}

					this.diagram.checkSnapLines(this);
				};

				/**
				 *
				 */
				Symbol.prototype.refresh = function() {
					this.refreshFromModelElement();
					this.recalculateBoundingBox();
					this.adjustGeometry();
					// Reroute connections in case the connection has moved
					// in response to server data - UNDO / collaboration etc.
					for ( var n in this.connections) {
						this.connections[n].reroute();
					}
				};

				/**
				 * Calculate new x, y, width, height based on changes e.g. on
				 * text or child symbols. Does not have to be invoked on
				 * explizit changes such as move or stretch.
				 */
				Symbol.prototype.recalculateBoundingBox = function() {
					// TODO may be default implementation
				};

				/**
				 *
				 */
				Symbol.prototype.adjustPrimitives = function() {
				};

				/**
				 *
				 */
				Symbol.prototype.dragStart = function() {
					// TODO hide for all selected
					if (this.diagram.mode == this.diagram.NORMAL_MODE) {
						this.diagram.mode = this.diagram.SYMBOL_MOVE_MODE;
						this.hideProximitySensor();

						if (!this.selected) {
							// deselect other symbols before drag
							this.diagram.deselectCurrentSelection();
							this.diagram.currentSelection = [];
							this.select();
						}

						// Remember drag start position

						this.dragStartX = this.x;
						this.dragStartY = this.y;
						this.diagram.dragEnabled = true;
					}
				};

				/**
				 *
				 */
				Symbol.prototype.dragStop = function() {
					if (this.diagram.mode == this.diagram.SYMBOL_MOVE_MODE) {
						this.diagram.mode = this.diagram.NORMAL_MODE
						this.showProximitySensor();
						// Only process if symbol has been moved at all
						if (this.x != this.dragStartX
								|| this.y != this.dragStartY) {
							if (this.requiresParentSymbol()) {
								var newParentSymbol = this.diagram.poolSymbol
										.findContainerSymbol(this.getXCenter(),
												this.getYCenter());

								if (newParentSymbol == null) {
									this.move(this.dragStartX, this.dragStartY);

									m_messageDisplay
											.showErrorMessage("Symbol is not contained in Swimlane. Reverting drag.");

									return;
								}

								if (newParentSymbol != this.parentSymbol) {
									m_utils.removeItemFromArray(
											this.parentSymbol.containedSymbols,
											this);

									this.parentSymbol = newParentSymbol;

									this.parentSymbol.containedSymbols
											.push(this);

									this.parentSymbolId = newParentSymbol.id;

									this.onParentSymbolChange();
								}

								this.diagram.snapSymbol(this);
								this.parentSymbol.adjustToSymbolBoundaries(
										this.x, this.y);

								// Other parts of the diagram may have been
								// affected
								// - update the diagram

								// this.diagram.submitUpdate();
								var oldGeometry = {
									"x" : this.dragStartX,
									"y" : this.dragStartY,
									"oid" : this.oid
								};
								var newGeometry = {
									"x" : this.x + this.parentSymbol.symbolXOffset,
									"y" : this.y,
									"parentSymbolId" : this.parentSymbol.id,
									"type" : this.type
								};

								var command = m_command
										.createMoveNodeSymbolCommand(
												this.diagram.model.id,
												this.oid, newGeometry);
								m_commandsController.submitCommand(command);
							} else {
								this.diagram.snapSymbol(this);

								// TODO Put in method

								if (this.isCompleted() != null) {
									// this.submitUpdate();

									var oldGeometry = {
										"x" : this.dragStartX,
										"y" : this.dragStartY,
										"oid" : this.oid
									};
									var newGeometry = {
										"x" : this.x + this.parentSymbol.symbolXOffset,
										"y" : this.y,
										"parentSymbolId" : this.parentSymbol.id
									};

									var command = m_command
											.createMoveNodeSymbolCommand(
													this.diagram.model.id,
													this.oid, newGeometry);
									m_commandsController.submitCommand(command);
								}
							}
						}
						this.diagram.dragEnabled = false;
					}
				};

				/**
				 *
				 */
				Symbol.prototype.proximityHoverIn = function(event) {
					if (this.diagram.mode == this.diagram.NORMAL_MODE) {
						// If this symbol hoverIn is called before other symbol
						// hoverOut, manual HoverOut is required.
						if (this.diagram.currentFlyOutSymbol
								&& this.diagram.currentFlyOutSymbol.oid != this.oid) {
							if (!this.diagram.currentFlyOutSymbol
									.validateProximity(event)) {
								this.diagram.currentFlyOutSymbol
										.hideFlyOutMenu();
							}
						}
						if (!this.selected) {
							this.showFlyOutMenu();
						}
					} else {
						if (this.validateCreateConnection()) {
							this.showAnchorPoints();
						}
					}
				};

				/**
				 *
				 */
				Symbol.prototype.proximityHoverOut = function(event) {
					if (this.diagram.mode == this.diagram.NORMAL_MODE) {
						// Hide flyout menu if mouse cursor is outside proximity
						// of symbol
						if (!this.validateProximity(event)) {
							this.hideFlyOutMenu();
						}

					} else {
						this.hideAnchorPoints();
					}
				};

				/**
				 * Checks if mouse cursor is inside proximity range of symbol
				 */
				Symbol.prototype.validateProximity = function(event) {
					try {
						// while in connection mode/or symbol is Pool/swimlane ,
						// or in Drag and Drop mode flyout menu should disappear
						if ((this.diagram.mode == this.diagram.CONNECTION_MODE
								|| this.diagram.currentConnection != null
								|| this.diagram.currentSelection.length > 0 || this.diagram.newSymbol)
								|| this.diagram.dragEnabled
								|| this.diagram.anchorDragEnabled
								|| this.type == null
								|| (this.type && (this.type.toLowerCase()
										.indexOf(
												m_constants.POOL_SYMBOL
														.toLowerCase()) > -1 || this.type == m_constants.SWIMLANE_SYMBOL))) {
							return false;
						}

						var scrollPos = m_modelerUtils
								.getModelerScrollPosition();
						var xPos = event.pageX - this.diagram.X_OFFSET
								+ scrollPos.left;
						var yPos = event.pageY - this.diagram.Y_OFFSET
								+ scrollPos.top;

						var rightProximityMargin = this.proximitySensor
								.attr('x')
								+ this.proximitySensor.attr('width');
						var leftProximityMargin = this.proximitySensor
								.attr('x');
						var topProximityMargin = this.proximitySensor.attr('y');
						var bottomProximityMargin = this.proximitySensor
								.attr('y')
								+ this.proximitySensor.attr('height');

						if ((xPos <= rightProximityMargin && xPos >= leftProximityMargin)
								&& (yPos <= bottomProximityMargin && yPos >= topProximityMargin)) {
							return true;
						}

					} catch (e) {
						return false;
					}
					return false;
				};

				/**
				 *
				 */
				Symbol.prototype.addToPrimitives = function(element) {
					this.primitives.push(element);
				};

				/**
				 * For clicks on the symbol during initial drawing.
				 */
				Symbol.prototype.initializePrepareEventHandling = function(
						element) {
					for ( var element in this.primitives) {

						// Initialize return pointer for closure

						this.primitives[element].auxiliaryProperties = {
							callbackScope : this
						};

						// Event handling

						this.primitives[element].click(Symbol_clickClosure);
					}
				};

				/**
				 *
				 */
				Symbol.prototype.initializeDefaultEventHandling = function(
						element) {
					for ( var element in this.primitives) {

						// Initialize return pointer for closure

						this.primitives[element].auxiliaryProperties = {
							callbackScope : this
						};

						// Event handling

						this.primitives[element]
								.mousemove(Symbol_mouseMoveClosure);
						this.primitives[element].hover(Symbol_hoverInClosure,
								Symbol_hoverOutClosure);
						// Drag and Drop not allowed for Pools
						if (this.type != m_constants.POOL_SYMBOL) {
							this.primitives[element].drag(Symbol_dragClosure,
									Symbol_dragStartClosure,
									Symbol_dragStopClosure);
						}
					}
				};

				/**
				 *
				 */
				Symbol.prototype.mouseMove = function(x, y) {
					if (this.diagram.isInConnectionMode()) {
						this.deselectAnchorPoints();
						var anchorPoint = this.getClosestAnchorPoint(x
								* this.diagram.zoomFactor
								- this.diagram.X_OFFSET, y
								* this.diagram.zoomFactor
								- this.diagram.Y_OFFSET);

						anchorPoint.select();

						if (null != this.diagram.currentConnection) {
							if (this.diagram.currentConnection.toAnchorPoint == anchorPoint) {
								return;
							} else if (this.diagram.currentConnection
									.isPrepared()) {
								this.diagram.currentConnection
										.setSecondAnchorPointNoComplete(anchorPoint);
							}
						}
					}
				};

				/**
				 *
				 */
				Symbol.prototype.hoverIn = function(x, y) {
					if (this.diagram.isInConnectionMode()) {
						this.showAnchorPoints();
						this.deselectAnchorPoints();
						var anchorPoint = this.getClosestAnchorPoint(x
								* this.diagram.zoomFactor
								- this.diagram.X_OFFSET, y
								* this.diagram.zoomFactor
								- this.diagram.Y_OFFSET);

						anchorPoint.select();

						if (null != this.diagram.currentConnection
								&& this.diagram.currentConnection.isPrepared()) {
							this.diagram.currentConnection
									.setSecondAnchorPointNoComplete(anchorPoint);
						}
					} else {
						this.showMoveCursor();
						this.highlight();
					}
				};

				/**
				 *
				 */
				Symbol.prototype.hoverOut = function() {
					if (this.diagram.isInConnectionMode()) {
						if (null != this.diagram.currentConnection
								&& this.diagram.currentConnection.isPrepared()) {
							this.diagram.currentConnection
									.setDummySecondAnchorPoint();
						}

						this.hideAnchorPoints();
					} else {
						this.showDefaultCursor();
						this.dehighlight();
					}
				};

				/**
				 *
				 */
				Symbol.prototype.click = function(x, y) {
					// When symbol is Draged, the edit symbol operation should
					// reset
					if (this.diagram.symbolEditMode) {
						this.diagram.editableText.resetForm();
					}
					if (!this.isCompleted()) {
						// returns 'true' if symbol was placed at new loc. else
						// false
						if (null != this.diagram.currentConnection) {
							var status = this.diagram.placeNewSymbol(x
									- this.diagram.X_OFFSET, y
									- this.diagram.Y_OFFSET, true);

							this.diagram.currentConnection.toModelElementOid = this.oid;
							this.diagram.currentConnection
									.updateAnchorPointForSymbol();
							this.diagram.currentConnection.complete();
							this.diagram.currentConnection = null;
						} else {
							var status = this.diagram.placeNewSymbol(x
									- this.diagram.X_OFFSET, y
									- this.diagram.Y_OFFSET);
						}
						// If symbol is outside the swimlane, remove it
						if (!status) {
							this.remove();
						}
					} else {
						if (this.diagram.isInConnectionMode()) {
							this.diagram.setAnchorPoint(this
									.getClosestAnchorPoint(x
											* this.diagram.zoomFactor
											- this.diagram.X_OFFSET, y
											* this.diagram.zoomFactor
											- this.diagram.Y_OFFSET));
							this.hideAnchorPoints();
						} else {
							this.select();
						}
					}
				};

				/**
				 *
				 */
				Symbol.prototype.select = function() {
					if (!this.isCompleted()) {
						return;
					}

					this.selected = true;

					this.diagram.addToCurrentSelection(this);
					this.hideFlyOutMenu();
					this.showSelectFrame();

					this.showPropertiesPanel();
				};

				/**
				 *
				 */
				Symbol.prototype.showPropertiesPanel = function() {
					if (this.propertiesPanel != null) {
						m_propertiesPanel.initializePropertiesPanel(this);
					} else {
						this.diagram.showProcessPropertiesPanel();
					}
				};

				/**
				 *
				 */
				Symbol.prototype.deselect = function() {
					if (!this.isCompleted()) {
						return;
					}

					this.selected = false;

					for ( var n in this.anchorPoints) {
						this.anchorPoints[n].hide();
					}

					this.hideSelectFrame();
				};

				/**
				 *
				 */
				Symbol.prototype.highlight = function() {
				};

				/**
				 *
				 */
				Symbol.prototype.dehighlight = function() {
				};

				/**
				 *
				 */
				Symbol.prototype.deselectAnchorPoints = function() {
					for ( var n in this.anchorPoints) {
						this.anchorPoints[n].deselect();
					}
				};

				/**
				 *
				 */
				Symbol.prototype.remove = function() {
					this.removePrimitives();
					this.removeFlyOutMenu();
					this.removeProximitySensor();
					this.removeAnchorPoints();
					if (this.parentSymbol) {
						m_utils.removeItemFromArray(
								this.parentSymbol.containedSymbols, this);
					}
					m_utils.removeItemFromArray(this.diagram.symbols, this);
				};

				/**
				 *
				 */
				Symbol.prototype.createAndSubmitDeleteCommand = function() {
					var command = m_command.createRemoveNodeCommand(this
							.getCommandIdForNode("delete"),
							this.diagram.model.id, this.parentSymbol.oid, this
									.createTransferObject());
					m_commandsController.submitCommand(command);
				}

				/**
				 * sync : Synchronous AJAX call is made, if set(needed in
				 * scenario like creating symbol from flyout menu where symbol
				 * should be created before createConnection)
				 */
				Symbol.prototype.createAndSubmitCreateCommand = function(sync) {
					var commandType = this.getCommandIdForNode("create");
					if (commandType) {
						var command = m_command.createCreateNodeCommand(this
								.getCommandIdForNode("create"),
								this.diagram.model.id, this.parentSymbol.oid,
								this.createTransferObject());
						command.sync = sync ? true : false;
						m_commandsController.submitCommand(command);
					} else {
						this.submitCreation();
					}
				}

				/**
				 *
				 */
				Symbol.prototype.removePrimitives = function() {
					var n = 0;

					while (n < this.primitives.length) {
						this.primitives[n].remove();
						++n;
					}
				};

				/**
				 *
				 */
				Symbol.prototype.removeConnections = function() {
					var n = 0;

					while (n < this.connections.length) {
						this.connections[n].createDeleteCommand();
						n++;
					}
				};

				/**
				 *
				 */
				Symbol.prototype.removeAnchorPoints = function() {
					for ( var n in this.anchorPoints) {
						this.anchorPoints[n].remove();
					}
				};

				/**
				 *
				 */
				Symbol.prototype.stretchStart = function() {
					this.preDragSymbolState = {
						x : this.x,
						y : this.y,
						height : this.height,
						width : this.width
					};
				};

				/**
				 *
				 */
				Symbol.prototype.stretchStop = function() {
					// Check if stretch is overlapping with other symbol boundry
					// or ouside swimlane margin
					if (!this.isSymbolWithinStretchLimit()) {
						this.x = this.preDragSymbolState.x;
						this.y = this.preDragSymbolState.y;
						this.width = this.preDragSymbolState.width;
						this.height = this.preDragSymbolState.height;
						this.adjustGeometry();
						return;
					}

					var newGeometry = {
						"x" : this.x,
						"y" : this.y,
						"parentSymbolId" : this.parentSymbol.id,
						"width" : this.width,
						"height" : this.height
					};

					var command = m_command.createMoveNodeSymbolCommand(
							this.diagram.model.id, this.oid, newGeometry);
					m_commandsController.submitCommand(command);
				};

				/**
				 *
				 */
				Symbol.prototype.isSymbolWithinStretchLimit = function() {
					// If stretch is outside left/right margin of swimlane
					if (this.x < this.parentSymbol.x
							|| (this.x + this.width) > (this.parentSymbol.x + this.parentSymbol.width)) {
						return false;
					}
					// If stretch is outside top/bottom margin of swimlane
					else if (this.y < this.parentSymbol.y
							|| (this.y + this.height) > (this.parentSymbol.y + this.parentSymbol.height)) {
						return false;
					} else {
						// Check if symbol co-ordinate collides with some other
						// symbol co-ordinates
						for ( var n in this.parentSymbol.containedSymbols) {
							if (this.parentSymbol.containedSymbols[n] != this
									&& this.parentSymbol.containedSymbols[n]
											.isInBoundingBox(this.x, this.y)) {
								return false;
							}
						}
					}
					return true;
				};

				/**
				 *
				 */
				Symbol.prototype.stretchLeft = function(dX, dY, x, y) {
					this.width += this.x - (x - this.diagram.X_OFFSET);
					this.x = x - this.diagram.X_OFFSET;
					if (this.width < m_constants.SYMBOL_MIN_SIZE)
						this.width = m_constants.SYMBOL_MIN_SIZE;
					this.adjustGeometry();
				};

				/**
				 *
				 */
				Symbol.prototype.stretchTop = function(dX, dY, x, y) {
					this.height += this.y - (y - this.diagram.Y_OFFSET);
					this.y = y - this.diagram.Y_OFFSET;
					if (this.height < m_constants.SYMBOL_MIN_SIZE)
						this.height = m_constants.SYMBOL_MIN_SIZE;
					this.adjustGeometry();
				};

				/**
				 *
				 */
				Symbol.prototype.stretchRight = function(dX, dY, x, y) {
					this.width = x - this.diagram.X_OFFSET - this.x;
					if (this.width < m_constants.SYMBOL_MIN_SIZE)
						this.width = m_constants.SYMBOL_MIN_SIZE;
					this.adjustGeometry();
				};

				/**
				 *
				 */
				Symbol.prototype.stretchBottom = function(dX, dY, x, y) {
					this.height += ((y - this.diagram.Y_OFFSET) - (this.y + this.height));
					if (this.height < m_constants.SYMBOL_MIN_SIZE)
						this.height = m_constants.SYMBOL_MIN_SIZE;
					this.adjustGeometry();
				};

				/**
				 *
				 */
				Symbol.prototype.validateCreateConnection = function() {
					return true;
				};

				/*
				 *
				 */
				Symbol.prototype.onParentSymbolChange = function() {
					// Do nothing
				};

				/*
				 *
				 */
				Symbol.prototype.flipFlowOrientation = function(flowOrientation) {
					var x = this.x;
					var y = this.y;

					this.move(y, x);
				};

				/*
				 *
				 */
				Symbol.prototype.getDashboardX = function() {
					return this.x + 30;
				};

				/*
				 *
				 */
				Symbol.prototype.getDashboardY = function() {
					return this.y + 30;
				};

				/*
				 *
				 */
				Symbol.prototype.hideGlow = function() {
					if (this.glow != null) {
						for ( var n = 0; n < this.glow.length; ++n) {
							this.glow[n].hide();
						}
					}
				};

				/*
				 *
				 */
				Symbol.prototype.removeGlow = function() {
					if (this.glow != null) {
						for ( var n = 0; n < this.glow.length; ++n) {
							this.glow[n].remove();
						}
					}
				};
			}

			// Callback methods for closure trick

			/**
			 *
			 */
			function Symbol_hoverInClosure(event, x, y) {
				this.auxiliaryProperties.callbackScope.hoverIn(x, y);
			}

			/**
			 *
			 */
			function Symbol_hoverOutClosure() {
				this.auxiliaryProperties.callbackScope.hoverOut();
			}

			/**
			 *
			 */
			function Symbol_clickClosure(event, x, y) {
				this.auxiliaryProperties.callbackScope.click(x, y);
				// Reset tool selection
				if (this.auxiliaryProperties.callbackScope.diagram
						.isInNormalMode()) {
					$(".selected-tool").removeClass("selected-tool");
				}
			}

			/**
			 *
			 */
			function Symbol_hoverInFlyOutMenuClosure() {
				this.auxiliaryProperties.callbackScope.showFlyOutMenu();
			}

			/**
			 *
			 */
			function Symbol_hoverOutFlyOutMenuClosure(event) {
				// Hover out(hide flyout menu) if mouse cursor is outside
				// proximity of symbol
				if (!this.auxiliaryProperties.callbackScope
						.validateProximity(event)) {
					this.auxiliaryProperties.callbackScope.hideFlyOutMenu();
				}
			}

			/**
			 *
			 */
			function Symbol_mouseMoveClosure(event, x, y) {
				this.auxiliaryProperties.callbackScope.mouseMove(x, y);
			}

			/**
			 *
			 */
			function Symbol_dragClosure(dX, dY, x, y, event) {
				this.auxiliaryProperties.callbackScope.diagram
						.moveSelectedSymbolsBy(
								x
										* this.auxiliaryProperties.callbackScope.diagram.zoomFactor
										- this.auxiliaryProperties.callbackScope.diagram.X_OFFSET
										+ this.auxiliaryProperties.callbackScope.diagram.scrollPane
												.scrollLeft()
										- (this.auxiliaryProperties.callbackScope.x + (this.auxiliaryProperties.callbackScope.width / 2)),
								y
										* this.auxiliaryProperties.callbackScope.diagram.zoomFactor
										- this.auxiliaryProperties.callbackScope.diagram.Y_OFFSET
										+ this.auxiliaryProperties.callbackScope.diagram.scrollPane
												.scrollTop()
										- (this.auxiliaryProperties.callbackScope.y + (this.auxiliaryProperties.callbackScope.height / 2)));
			}

			/**
			 *
			 */
			function Symbol_dragStartClosure() {
				this.auxiliaryProperties.callbackScope.dragStart();
			}

			/**
			 *
			 */
			function Symbol_dragStopClosure() {
				this.auxiliaryProperties.callbackScope.dragStop();
			}

			/**
			 *
			 */
			function Symbol_stretchLeftClosure(dX, dY, x, y) {
				this.auxiliaryProperties.callbackScope
						.stretchLeft(
								dX
										* this.auxiliaryProperties.callbackScope.diagram.zoomFactor,
								dY
										* this.auxiliaryProperties.callbackScope.diagram.zoomFactor,
								x
										* this.auxiliaryProperties.callbackScope.diagram.zoomFactor,
								y
										* this.auxiliaryProperties.callbackScope.diagram.zoomFactor);
			}

			/**
			 *
			 */
			function Symbol_stretchTopClosure(dX, dY, x, y) {
				this.auxiliaryProperties.callbackScope
						.stretchTop(
								dX
										* this.auxiliaryProperties.callbackScope.diagram.zoomFactor,
								dY
										* this.auxiliaryProperties.callbackScope.diagram.zoomFactor,
								x
										* this.auxiliaryProperties.callbackScope.diagram.zoomFactor,
								y
										* this.auxiliaryProperties.callbackScope.diagram.zoomFactor);
			}

			/**
			 *
			 */
			function Symbol_stretchRightClosure(dX, dY, x, y) {
				this.auxiliaryProperties.callbackScope
						.stretchRight(
								dX
										* this.auxiliaryProperties.callbackScope.diagram.zoomFactor,
								dY
										* this.auxiliaryProperties.callbackScope.diagram.zoomFactor,
								x
										* this.auxiliaryProperties.callbackScope.diagram.zoomFactor,
								y
										* this.auxiliaryProperties.callbackScope.diagram.zoomFactor);
			}

			/**
			 *
			 */
			function Symbol_stretchBottomClosure(dX, dY, x, y) {
				this.auxiliaryProperties.callbackScope
						.stretchBottom(
								dX
										* this.auxiliaryProperties.callbackScope.diagram.zoomFactor,
								dY
										* this.auxiliaryProperties.callbackScope.diagram.zoomFactor,
								x
										* this.auxiliaryProperties.callbackScope.diagram.zoomFactor,
								y
										* this.auxiliaryProperties.callbackScope.diagram.zoomFactor);
			}

			/**
			 *
			 */
			function Symbol_stretchStartClosure() {
				if (this.auxiliaryProperties.callbackScope.stretchStart) {
					this.auxiliaryProperties.callbackScope.stretchStart();
				}
			}

			/**
			 *
			 */
			function Symbol_stretchStopClosure() {
				this.auxiliaryProperties.callbackScope.stretchStop();
				/* Reset the cached state after the drag / stretch stops. */
				this.auxiliaryProperties.callbackScope.preDragSymbolState = undefined;
			}

			/**
			 *
			 */
			// TOD= Is this needed
			function Symbol_createClosure(callbackScope, data) {
				callbackScope.onCreate(data);
			}

			function AnchorPoint(symbol, orientation) {
				// Attributes

				this.orientation = orientation;
				this.symbol = symbol;
				this.x = 0;
				this.y = 0;
				this.cacheOrientation = null;
				this.cacheX = null;
				this.cacheY = null;

				this.graphics = m_canvasManager
						.drawRectangle(
								this.x,
								this.y,
								m_constants.DEFAULT_ANCHOR_WIDTH,
								m_constants.DEFAULT_ANCHOR_HEIGHT,
								{
									"stroke" : m_constants.DEFAULT_ANCHOR_STROKE_COLOR,
									"stroke-width" : m_constants.DEFAULT_ANCHOR_STROKE_WIDTH,
									"fill" : m_constants.DEFAULT_ANCHOR_FILL_COLOR
								}).hide();

				this.originalAnchorPoint = null;
				this.dragConnection = null;
				this.lastDragOverSymbol = null;

				// Initialize return pointer for closure

				this.graphics.auxiliaryProperties = {
					anchorPoint : this
				};

				// Event handling

				this.graphics.click(AnchorPoint_clickClosure);
				this.graphics.hover(AnchorPoint_hoverInClosure,
						AnchorPoint_hoverOutClosure);
				this.graphics.drag(AnchorPoint_dragClosure,
						AnchorPoint_dragStartClosure,
						AnchorPoint_dragStopClosure);

				/**
				 *
				 */
				AnchorPoint.prototype.toString = function() {
					return "Lightdust.AnchorPoint";
				};

				/**
				 *
				 */
				AnchorPoint.prototype.moveTo = function(x, y) {
					this.x = x;
					this.y = y;
					this.graphics.attr({
						'x' : this.x - 0.5 * m_constants.DEFAULT_ANCHOR_WIDTH,
						'y' : this.y - 0.5 * m_constants.DEFAULT_ANCHOR_HEIGHT
					});
				}

				/**
				 *
				 */
				AnchorPoint.prototype.adjust = function() {
					if (this.orientation == m_constants.NORTH) {
						this.moveTo(this.symbol.x + 0.5 * this.symbol.width,
								this.symbol.y);
					} else if (this.orientation == m_constants.EAST) {
						this.moveTo(this.symbol.x + this.symbol.width,
								this.symbol.y + 0.5 * this.symbol.height);
					} else if (this.orientation == m_constants.SOUTH) {
						this.moveTo(this.symbol.x + 0.5 * this.symbol.width,
								this.symbol.y + this.symbol.height);
					} else if (this.orientation == m_constants.WEST) {
						this.moveTo(this.symbol.x, this.symbol.y + 0.5
								* this.symbol.height);
					}
				};

				/**
				 *
				 */
				AnchorPoint.prototype.show = function() {
					this.graphics.show();
					this.graphics.toFront();
				};

				/**
				 *
				 */
				AnchorPoint.prototype.hide = function() {
					this.graphics.hide();
				};

				/**
				 *
				 */
				AnchorPoint.prototype.select = function() {
					this.graphics
							.attr({
								"fill" : m_constants.SELECT_ANCHOR_FILL_COLOR,
								"stroke" : m_constants.SELECT_ANCHOR_STROKE_COLOR,
								"stroke-width" : m_constants.DEFAULT_ANCHOR_STROKE_WIDTH
							});
					this.graphics.animate({
						"width" : m_constants.SELECT_ANCHOR_WIDTH,
						"height" : m_constants.SELECT_ANCHOR_HEIGHT
					}, 500, '<');
					this.graphics.animate({
						"width" : m_constants.DEFAULT_ANCHOR_WIDTH,
						"height" : m_constants.DEFAULT_ANCHOR_WIDTH
					}, 500, '>');
				};

				/**
				 *
				 */
				AnchorPoint.prototype.deselect = function() {
					this.graphics.animate({
						"width" : m_constants.DEFAULT_ANCHOR_WIDTH,
						"height" : m_constants.DEFAULT_ANCHOR_WIDTH
					}, 1000, '>');
					this.graphics
							.attr({
								"fill" : m_constants.DEFAULT_ANCHOR_FILL_COLOR,
								"stroke" : m_constants.DEFAULT_ANCHOR_STROKE_COLOR,
								"stroke-width" : m_constants.DEFAULT_ANCHOR_STROKE_WIDTH
							});
				};

				/**
				 *
				 */
				AnchorPoint.prototype.hoverIn = function() {
					this.select();
				};

				/**
				 *
				 */
				AnchorPoint.prototype.hoverOut = function() {
					this.deselect();
				};

				/**
				 *
				 */
				AnchorPoint.prototype.drag = function(dX, dY, x, y) {
					if (this.dragConnection == null) {
						return;
					}

					var scrollPos = m_modelerUtils.getModelerScrollPosition();
					// Calculate diagram coordinates

					this.moveTo((x + scrollPos.left)
							* this.symbol.diagram.zoomFactor
							- this.symbol.diagram.X_OFFSET - 0.5
							* m_constants.DEFAULT_ANCHOR_WIDTH,
							(y + scrollPos.top)
									* this.symbol.diagram.zoomFactor
									- this.symbol.diagram.Y_OFFSET - 0.5
									* m_constants.DEFAULT_ANCHOR_HEIGHT);

					// TODO Panning

					var symbol = this.symbol.diagram
							.getSymbolContainingCoordinatesExcludeContainerSymbols(
									this.x, this.y);

					if (symbol != null) {
						if (this.lastDragOverSymbol != null
								&& this.lastDragOverSymbol != symbol) {
							this.lastDragOverSymbol.hideAnchorPoints();
						}

						this.lastDragOverSymbol = symbol;

						symbol.showAnchorPoints();
						symbol.deselectAnchorPoints();

						var anchorPoint = symbol.getClosestAnchorPoint(this.x,
								this.y, true);

						if (anchorPoint != null) {
							anchorPoint.select();

							if (this.direction == m_constants.FROM_ANCHOR_POINT) {
								// Cache the original AnchorPoint for deletion
								if (!this.dragConnection.originalFromAnchorPoint) {
									this.dragConnection.originalFromAnchorPoint = this.dragConnection.fromAnchorPoint;
								}
								this.dragConnection.fromAnchorPoint = anchorPoint;
							} else {
								if (!this.dragConnection.originalToAnchorPoint) {
									this.dragConnection.originalToAnchorPoint = this.dragConnection.toAnchorPoint;
								}
								this.dragConnection.toAnchorPoint = anchorPoint;
							}
						}
					} else {
						if (this.lastDragOverSymbol != null) {
							this.lastDragOverSymbol.hideAnchorPoints();
						}

						this.lastDragOverSymbol = null;

						if (this.direction == m_constants.FROM_ANCHOR_POINT) {
							this.dragConnection.fromAnchorPoint = this;
						} else {
							this.dragConnection.toAnchorPoint = this;
						}
					}

					this.dragConnection.reroute();
				}

				/**
				 *
				 */
				AnchorPoint.prototype.dragStart = function() {
					this.symbol.diagram.mode = this.symbol.diagram.SYMBOL_MOVE_MODE;
					for ( var n in this.symbol.connections) {
						if (this.symbol.connections[n].selected) {
							this.dragConnection = this.symbol.connections[n];
						}
					}

					if (this.dragConnection == null) {
						return;
					}

					// TODO May be make direction a general property of anchor
					// point

					if (this == this.dragConnection.fromAnchorPoint) {
						this.direction = m_constants.FROM_ANCHOR_POINT;
					} else {
						this.direction = m_constants.TO_ANCHOR_POINT;
					}

					// Remember drag start position
					this.dragStartX = this.x;
					this.dragStartY = this.y;
					// Flag required to hide flyout menu's in Drag and Drop
					this.symbol.diagram.anchorDragEnabled = true;

					// Replace and anchor point and keep reference

					for ( var n in this.symbol.anchorPoints) {
						if (this == this.symbol.anchorPoints[n]) {
							this.symbol.anchorPoints[n] = new AnchorPoint(
									this.symbol, this.orientation);
							this.originalAnchorPoint = this.symbol.anchorPoints[n];

							this.symbol.anchorPoints[n].moveTo(this.x, this.y);

							break;
						}
					}
				}

				/**
				 *
				 */
				AnchorPoint.prototype.dragStop = function() {
					this.symbol.diagram.mode = this.symbol.diagram.NORMAL_MODE;
					if (this.dragConnection == null) {
						return;
					}
					// dragEnabled flag is used to hide flyout menu in Drag mode
					this.symbol.diagram.anchorDragEnabled = false;
					if (this.lastDragOverSymbol != null) {
						this.lastDragOverSymbol.hideAnchorPoints();
					}

					var symbol = this.symbol.diagram
							.getSymbolContainingCoordinatesExcludeContainerSymbols(
									this.x, this.y);

					if (symbol != null) {
						// when more than one connection from same anchorPoint,
						// prevent modification of other connection
						for ( var n in this.symbol.connections) {
							if (this.dragConnection.oid != this.symbol.connections[n].oid) {
								if (this.symbol.connections[n].fromAnchorPoint == this) {
									this.symbol.connections[n].fromAnchorPoint = this.originalAnchorPoint
								} else if (this.symbol.connections[n].toAnchorPoint == this) {
									this.symbol.connections[n].toAnchorPoint = this.originalAnchorPoint
								}
							}
						}

						var anchorPoint = symbol.getClosestAnchorPoint(this.x,
								this.y, true);
						var updateConnection = true;

						if (symbol == this.symbol) {
							if (this.direction == m_constants.FROM_ANCHOR_POINT) {
								this.dragConnection.fromAnchorPoint = anchorPoint;
							} else {
								this.dragConnection.toAnchorPoint = anchorPoint;
							}
						} else {
							var newConnection = null;
							// Store the new Anchor Points to create new
							// connection
							var fromAnchorPoint = this.dragConnection.fromAnchorPoint;
							var toAnchorPoint = this.dragConnection.toAnchorPoint;
							// Validate the new connection
							if (this.dragConnection.validateCreateConnection(
									fromAnchorPoint, toAnchorPoint)) {

								// Reset the original Anchor Point in
								// dragConnection
								// for deletion
								if (this.dragConnection.originalFromAnchorPoint) {
									this.dragConnection.fromAnchorPoint = this.dragConnection.originalFromAnchorPoint;
									this.dragConnection.originalFromAnchorPoint = null;
								} else if (this.dragConnection.originalToAnchorPoint) {
									this.dragConnection.toAnchorPoint = this.dragConnection.originalToAnchorPoint;
									this.dragConnection.originalToAnchorPoint = null;
								}
								this.dragConnection.createDeleteCommand(true);

								if (this.direction == m_constants.TO_ANCHOR_POINT) {
									newConnection = this.symbol.diagram
											.createConnection(fromAnchorPoint);
									// Create the connection
									newConnection.setSecondAnchorPoint(
											anchorPoint, true);
								} else {
									newConnection = this.symbol.diagram
											.createConnection(anchorPoint);
									// Create the connection
									newConnection.setSecondAnchorPoint(
											toAnchorPoint, true);
								}

								this.dragConnection = newConnection;

							} else {
								// Reset the original Anchor Point in
								// dragConnection to revert
								if (this.dragConnection.originalFromAnchorPoint) {
									this.dragConnection.fromAnchorPoint = this.originalAnchorPoint;
									this.dragConnection.fromAnchorPointOrientation = this.originalAnchorPoint.orientation;
									this.dragConnection.originalFromAnchorPoint = null;
								} else if (this.dragConnection.originalToAnchorPoint) {
									this.dragConnection.toAnchorPoint = this.originalAnchorPoint;
									this.dragConnection.toAnchorPointOrientation = this.originalAnchorPoint.orientation;
									this.dragConnection.originalToAnchorPoint = null;
								}
								this.moveTo(this.dragStartX, this.dragStartY);
								updateConnection = false;
							}
						}
					} else {
						if (this.direction == m_constants.FROM_ANCHOR_POINT) {
							this.dragConnection.fromAnchorPoint = this.originalAnchorPoint;
						} else {
							this.dragConnection.toAnchorPoint = this.originalAnchorPoint;
						}
					}

					this.dragConnection.reroute();
					// if reroute validation fails no update is required
					if (updateConnection) {
						var changes = {
							modelElement : {
								toAnchorPointOrientation : this.dragConnection.toAnchorPoint.orientation,
								fromAnchorPointOrientation : this.dragConnection.fromAnchorPoint.orientation
							}
						}
						this.dragConnection.createUpdateCommand(changes);
						m_messageDisplay.showMessage("Connection updated");
						this.remove();
					}

					this.dragConnection.select();
					this.dragConnection.toAnchorPoint.deselect();
				}

				/**
				 *
				 */
				AnchorPoint.prototype.remove = function() {
					this.graphics.remove();
				};

				AnchorPoint.prototype.createFlippedClone = function() {
					var clone = new AnchorPoint(null,
							(this.orientation + 2) % 4);

					clone.x = this.x;
					clone.y = this.y;

					return clone;
				};
			}

			function AnchorPoint_clickClosure() {
				if (this.auxiliaryProperties)
					this.auxiliaryProperties.anchorPoint.select();
			}

			function AnchorPoint_hoverInClosure() {
				if (this.auxiliaryProperties)
					this.auxiliaryProperties.anchorPoint.hoverIn();
			}

			function AnchorPoint_hoverOutClosure() {
				if (this.auxiliaryProperties)
					this.auxiliaryProperties.anchorPoint.hoverOut();
			}

			function AnchorPoint_dragClosure(dX, dY, x, y, event) {
				this.auxiliaryProperties.anchorPoint.drag(dX, dY, x, y);
			}

			function AnchorPoint_dragStartClosure() {
				this.auxiliaryProperties.anchorPoint.dragStart();
			}

			function AnchorPoint_dragStopClosure() {
				this.auxiliaryProperties.anchorPoint.dragStop();
			}
		});
