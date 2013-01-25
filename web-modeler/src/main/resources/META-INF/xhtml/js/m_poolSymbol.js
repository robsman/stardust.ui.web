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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_command",
				"bpm-modeler/js/m_canvasManager", "bpm-modeler/js/m_symbol", "bpm-modeler/js/m_swimlaneSymbol", "bpm-modeler/js/m_messageDisplay","bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_canvasManager, m_symbol, m_swimlaneSymbol, m_messageDisplay, m_i18nUtils) {

			return {
				createPoolSymbol : function(diagram) {
					var poolSymbol = new PoolSymbol();

					poolSymbol.bind(diagram);

					return poolSymbol;
				},

				createPoolSymbolFromJson : function(diagram, json) {
					// TODO Ugly
					m_utils.inheritFields(json, m_symbol.createSymbol());
					m_utils.inheritMethods(json, new PoolSymbol());

					json.bind(diagram);

					json.initializeFromJson();

					return json;
				}
			};

			/**
			 *
			 */
			function PoolSymbol() {
				var symbol = m_symbol.createSymbol();

				m_utils.inheritFields(this, symbol);
				m_utils.inheritMethods(PoolSymbol.prototype, symbol);

				this.laneSymbols = [];
				this.x = 0;
				this.y = 0;
				this.width = 0;
				this.height = 0;
				this.orientation = null;
				var laneIndex = 1;


				/**
				 * Binds all client-side aspects to the object (graphics
				 * objects, diagram, base classes).
				 */
				PoolSymbol.prototype.bind = function(diagram) {
					this.type = m_constants.POOL_SYMBOL;
					this.diagram = diagram;
					this.orientation = diagram.flowOrientation;
					this.borderRectangle = null;
					this.topRectangle = null;
					this.text = null;
				};

				/**
				 *
				 */
				PoolSymbol.prototype.toString = function() {
					return "[object Lightdust.PoolSymbol()]";
				};

				/**
				 *
				 */
				PoolSymbol.prototype.initializeFromJson = function() {
					var hasLanes = false;
					for ( var m in this.laneSymbols) {
						m_swimlaneSymbol.createSwimlaneSymbolFromJson(
								this.diagram, this, this.laneSymbols[m]);

						hasLanes = true;
					}

					if (!hasLanes) {
						m_messageDisplay
								.showErrorMessage(m_i18nUtils
										.getProperty("modeler.diagram.messages.processNotUsingBPMNMode"));
						return;
					}

					this.prepareNoPosition();
					this.completeNoTransfer();
					this.recalculateBoundingBox();
					this.adjustGeometry();
				};

				/**
				 *
				 */
				PoolSymbol.prototype.createTransferObject = function() {
					var transferObject = {};

					m_utils.inheritFields(transferObject, this);

					transferObject = this.prepareTransferObject(transferObject);

					transferObject.borderRectangle = null;
					transferObject.topRectangle = null;
					transferObject.text = null;
					transferObject.laneSymbols = [];

					for ( var laneSymbol in this.laneSymbols) {
						transferObject.laneSymbols[laneSymbol] = this.laneSymbols[laneSymbol]
								.createTransferObject();
					}

					transferObject.containedSymbols = [];

					for ( var symbol in this.containedSymbols) {
						transferObject.containedSymbols[symbol] = this.containedSymbols[symbol]
								.createTransferObject();
					}

					return transferObject;
				};

				/**
				 *
				 */
				PoolSymbol.prototype.createCreateCommand = function() {
					return m_command.createCommand("/models/"
							+ this.diagram.model.id + "/processes/"
							+ this.diagram.process.id + "/pools", this
							.createTransferObject());
				};

				/**
				 *
				 */
				PoolSymbol.prototype.createUpdateCommand = function() {
					return m_command.createCommand("/models/"
							+ this.diagram.model.id + "/processes/"
							+ this.diagram.process.id + "/pools/" +
					this.oid, this.createTransferObject());
				};

				/**
				 *
				 */
				PoolSymbol.prototype.createPrimitives = function() {
					this.borderRectangle = m_canvasManager
							.drawRectangle(
									this.x,
									this.y,
									this.width,
									this.height,
									{
										'stroke' : m_constants.POOL_COLOR,
										'stroke-width' : m_constants.POOL_SWIMLANE_STROKE_WIDTH
									});

					this.addToPrimitives(this.borderRectangle);

					this.topRectangle = m_canvasManager
							.drawRectangle(
									this.x,
									this.y,
									this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? this.width
											: m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT,
											this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
											: this.height,
									{
										"fill" : m_constants.POOL_COLOR,
										"stroke" : m_constants.POOL_COLOR,
										"stroke-width" : m_constants.POOL_SWIMLANE_STROKE_WIDTH
									});

					this.addToPrimitives(this.topRectangle);

					this.text = m_canvasManager
							.drawTextNode(
									this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? (this.x + 0.5 * this.width)
											: (this.x + 0.5 * m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT),
									this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? (this.y + 0.5 * m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT)
											: (this.y + 0.5 * this.height),
									this.name)
							.attr(
									{
										"transform" : this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? "R0"
												: "R270",
										"text-anchor" : "middle",
										"fill" : "white",
										"font-family" : m_constants.DEFAULT_FONT_FAMILY,
										"font-size" : m_constants.DEFAULT_FONT_SIZE,
										"font-weight" : "bold"
									});

					this.addToPrimitives(this.text);
				};

				/**
				 *
				 */
				PoolSymbol.prototype.createChildSymbols = function() {
					// if (this.laneSymbols.size() == 0) {
					// this.createDefaultLane();
					// }
				};

				/**
				 *
				 */
				PoolSymbol.prototype.isContainerSymbol = function() {
					return true;
				};

				/**
				 *
				 */
				PoolSymbol.prototype.createDefaultLane = function() {
					laneIndex = 0;
					var defaultSwimlaneSymbol = m_swimlaneSymbol
							.createSwimlaneSymbol(this.diagram, this);

					this.laneSymbols.push(defaultSwimlaneSymbol);
					defaultSwimlaneSymbol
							.initialize(
									this.x
											+ m_constants.POOL_SWIMLANE_MARGIN
											+ (this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? 0
													: m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT),
									this.y
											+ m_constants.POOL_SWIMLANE_MARGIN
											+ (this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
													: 0));
				};

				/**
				 *
				 */
				PoolSymbol.prototype.createSwimlaneSymbol = function() {
					this.createSwimlaneSymbolFromParticipant(null, this);
				};

				/**
				 *
				 */
				PoolSymbol.prototype.getSwimlaneSymbolForParticipant = function(
						participant) {
					var ln;
					if (participant && this.laneSymbols) {
						jQuery.each(this.laneSymbols, function(index, element) {
							if (element.participantFullId == participant
									.getFullId()) {
								ln = element;
							}
						});
					}

					return ln;
				};

				/**
				 *
				 */
				PoolSymbol.prototype.createSwimlaneSymbolFromParticipant = function(
						participant) {
					m_messageDisplay.clear();
					if (!this.getSwimlaneSymbolForParticipant(participant)) {
						var swimlaneSymbol = null;

						if (participant == null) {
							swimlaneSymbol = m_swimlaneSymbol.createSwimlaneSymbol(
									this.diagram, this);
						} else {
							swimlaneSymbol = m_swimlaneSymbol
									.createSwimlaneSymbolFromParticipant(
											this.diagram, this, participant);
						}

						this.laneSymbols.push(swimlaneSymbol);

						// Required to receive command callbacks

						this.diagram.lastSymbol = swimlaneSymbol;

						swimlaneSymbol
								.initialize(
										this.x
												+ m_constants.POOL_SWIMLANE_MARGIN
												+ (this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? 0
														: m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT),
										this.y
												+ m_constants.POOL_SWIMLANE_MARGIN
												+ (this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
														: 0));

						if (participant != null) {
							swimlaneSymbol.refreshFromModelElement();
						}

						this.recalculateBoundingBox();
						this.adjustGeometry();
						// If any lane is minimized , x co-ord needs to be
						// adjusted
						this.adjustCurrentLaneCoordinates(swimlaneSymbol);
						//The create REST call for swimlanes is made after the swimlabe is created and re-positioned.
						swimlaneSymbol.createAndSubmitCreateCommand();
					} else {
						m_messageDisplay.showMessage("Swimlane for participant (" + participant.name + ") exists already");
					}
				};

				/**
				 * Adjust the co-ordinates for current swimalane, and set offset
				 * required for calcualating x/y for symbols contained in lane
				 */
				PoolSymbol.prototype.adjustCurrentLaneCoordinates = function(
						swimlaneSymbol) {
					var xOffset = 0;
					var yOffset = 0;
					for ( var n in this.laneSymbols) {
						if (this.laneSymbols[n].minimized) {
							if (this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
								xOffset = this.laneSymbols[n].cacheWidth
										- this.laneSymbols[n].width;
								swimlaneSymbol.x += xOffset;
								swimlaneSymbol.symbolXOffset = xOffset;
							} else {
								yOffset = this.laneSymbols[n].cacheHeight
										- this.laneSymbols[n].height;
								swimlaneSymbol.y += yOffset;
								swimlaneSymbol.symbolYOffset = yOffset;
							}
							break;
						}
					}
				};

				/**
				 *
				 */
				PoolSymbol.prototype.recalculateBoundingBox = function() {
					this.width = this.calculateWidth();
					this.height = this.calculateHeight();

					// TODO Probably belongs to adjustGeometry

					this.diagram.setSize(this.width, this.height);
				};

				/**
				 *
				 */
				PoolSymbol.prototype.calculateWidth = function() {
					if (this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
						var width = m_constants.POOL_SWIMLANE_MARGIN;

						for ( var n in this.laneSymbols) {
							width += this.laneSymbols[n].width;
							width += m_constants.POOL_SWIMLANE_MARGIN;
						}

						return width;
					} else {
						var width = 0;

						for ( var n in this.laneSymbols) {
							width = Math.max(this.laneSymbols[n].width, width);
						}

						return width + 2 * m_constants.POOL_SWIMLANE_MARGIN
								+ m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT;
					}
				};

				/**
				 *
				 */
				PoolSymbol.prototype.calculateHeight = function() {
					if (this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
						var height = 0;

						for ( var n in this.laneSymbols) {
							height = Math.max(this.laneSymbols[n].height,
									height);
						}

						return height + 2 * m_constants.POOL_SWIMLANE_MARGIN
								+ m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT;
					} else {
						var height = m_constants.POOL_SWIMLANE_MARGIN;

						for ( var n in this.laneSymbols) {
							height += this.laneSymbols[n].height;
							height += m_constants.POOL_SWIMLANE_MARGIN;
						}

						return height;
					}
				};

				/**
				 * Sort the lanes based on oid, required on Undo of delete lanes
				 */
				PoolSymbol.prototype.sortLanes = function() {
					this.laneSymbols.sort(function(a, b) {
						return $(a)[0].oid > $(b)[0].oid;
					});
				};

				/**
				 * Calculate Lane Offset and adjust symbols for all lanes
				 */
				PoolSymbol.prototype.updateLanesOffsetAndAdjustChild = function(
						currentLane, minimize) {
					for ( var n in this.laneSymbols) {
						//Vertical Orientation
						if (this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
							// For all lanes right to current lane, set the XOffset
							// for width adjustment
							if (this.laneSymbols[n] != currentLane
									&& this.laneSymbols[n].x > currentLane.x) {
								if (minimize) {
									this.laneSymbols[n].symbolXOffset += currentLane.cacheWidth
											- currentLane.width;
									// Move the lane to left when adjacent lane is
									// minimized
									this.laneSymbols[n].moveBy(
											-(currentLane.cacheWidth - currentLane.width), 0);
									// Move the contained symbols
									for ( var c in this.laneSymbols[n].containedSymbols) {
										this.laneSymbols[n].containedSymbols[c]
												.moveBy(
														-(currentLane.cacheWidth - currentLane.width),
														0);
									}
								} else {
									if (this.laneSymbols[n].symbolXOffset > 0) {
										// Reset the offset, when adjacant lane is
										// maximized
										this.laneSymbols[n].symbolXOffset -= (currentLane.cacheWidth - currentLane.width);
										// Move the lane to right
										this.laneSymbols[n].moveBy(
												currentLane.cacheWidth
														- currentLane.width, 0);
										// Move the contained symbols to saved
										// location
										for ( var c in this.laneSymbols[n].containedSymbols) {
											this.laneSymbols[n].containedSymbols[c]
													.moveTo(
															this.laneSymbols[n].containedSymbols[c].serverSideCoordinates.x
																	- this.laneSymbols[n].symbolXOffset,
															this.laneSymbols[n].containedSymbols[c].serverSideCoordinates.y);

											// Cache Anchor Points stored when lane is minimized, needs to be
											// moved when adj lane is maximized and current lane is in minimized state
											for ( var m in this.laneSymbols[n].containedSymbols[c].anchorPoints) {
												if (this.laneSymbols[n].containedSymbols[c].anchorPoints[m].cacheX) {
													var symbolNewAnchorPointLocation = this.laneSymbols[n].containedSymbols[c].x
															+ this.laneSymbols[n].containedSymbols[c].width
															/ 2;
													this.laneSymbols[n].containedSymbols[c].anchorPoints[m].cacheX += (symbolNewAnchorPointLocation - this.laneSymbols[n].containedSymbols[c].anchorPoints[m].cacheX);
												}else if (this.laneSymbols[n].containedSymbols[c].anchorPoints[m].cacheY) {
													var symbolNewAnchorPointLocation = this.laneSymbols[n].containedSymbols[c].y
															+ this.laneSymbols[n].containedSymbols[c].height
															/ 2;
													this.laneSymbols[n].containedSymbols[c].anchorPoints[m].cacheY += (symbolNewAnchorPointLocation - this.laneSymbols[n].containedSymbols[c].anchorPoints[m].cacheY);
												}
											}
										}
									}
								}
							}
						} else { // Horizontal Orientation
							// For all lanes right to current lane, set the
							// YOffset
							// for hieght adjustment
							if (this.laneSymbols[n] != currentLane
									&& this.laneSymbols[n].y > currentLane.y) {
								if (minimize) {
									this.laneSymbols[n].symbolYOffset += currentLane.cacheHeight
											- currentLane.height;
									// Move the lane to Top when adjacent lane
									// is
									// minimized
									this.laneSymbols[n]
											.moveBy(
													0,
													-(currentLane.cacheHeight - currentLane.height));
									// Move the contained symbols
									for ( var c in this.laneSymbols[n].containedSymbols) {
										this.laneSymbols[n].containedSymbols[c]
												.moveBy(
														0,
														-(currentLane.cacheHeight - currentLane.height));
									}
								} else {
									if (this.laneSymbols[n].symbolYOffset > 0) {
										// Reset the offset, when adjacent lane
										// is maximized
										this.laneSymbols[n].symbolYOffset -= (currentLane.cacheHeight - currentLane.height);
										// Move the lane to bottom
										this.laneSymbols[n].moveBy(0,
												currentLane.cacheHeight
														- currentLane.height);
										// Move the contained symbols to saved
										// location
										for ( var c in this.laneSymbols[n].containedSymbols) {
											this.laneSymbols[n].containedSymbols[c]
													.moveTo(
															this.laneSymbols[n].containedSymbols[c].serverSideCoordinates.x,
															this.laneSymbols[n].containedSymbols[c].serverSideCoordinates.y
																	- this.laneSymbols[n].symbolYOffset);

											// Cache Anchor Points stored when lane is minimized, needs to be
											// moved when adj lane is maximized and current lane is in minimized state
											for ( var m in this.laneSymbols[n].containedSymbols[c].anchorPoints) {
												if (this.laneSymbols[n].containedSymbols[c].anchorPoints[m].cacheY) {
													var symbolNewAnchorPointLocation = this.laneSymbols[n].containedSymbols[c].y
															+ this.laneSymbols[n].containedSymbols[c].height
															/ 2;
													this.laneSymbols[n].containedSymbols[c].anchorPoints[m].cacheY += (symbolNewAnchorPointLocation - this.laneSymbols[n].containedSymbols[c].anchorPoints[m].cacheY);
												} else if (this.laneSymbols[n].containedSymbols[c].anchorPoints[m].cacheX) {
													var symbolNewAnchorPointLocation = this.laneSymbols[n].containedSymbols[c].x
															+ this.laneSymbols[n].containedSymbols[c].width
															/ 2;
													this.laneSymbols[n].containedSymbols[c].anchorPoints[m].cacheX += (symbolNewAnchorPointLocation - this.laneSymbols[n].containedSymbols[c].anchorPoints[m].cacheX);
												}
											}
										}
									}
								}
							}
						}
						this.laneSymbols[n].adjustGeometry();
					}
				};


				/**
				 *
				 */
				PoolSymbol.prototype.adjustPrimitives = function(dX, dY) {
					if (this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
						this.borderRectangle.attr({
							"width" : this.width,
							"height" : this.height
						});
						this.topRectangle.attr({
							"width" : this.width,
							"height" : m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
						});
						this.text.attr({
							"transform" : "R0",
							"y" : 0.5 * m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT,
							"x" : this.x + 0.5 * this.width
						});
					} else {
						this.borderRectangle.attr({
							"width" : this.width,
							"height" : this.height
						});
						this.topRectangle.attr({
							"width" : m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT,
							"height" : this.height
						});
						this.text.attr({
							"transform" : "R270",
							"x" : 0.5 * m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT,
							"y" : this.y + 0.5 * this.height
						});
					}
				};

				/**
				 *
				 */
				PoolSymbol.prototype.refreshDiagram = function() {
					var laneMinimized = false;
					for ( var n in this.laneSymbols) {
						if (this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
							if (this.laneSymbols[n].symbolXOffset) {
								laneMinimized = true;
								this.laneSymbols[n].x = this.laneSymbols[n].serverSideCoordinates.x;
								for ( var m in this.laneSymbols[n].containedSymbols) {
									this.laneSymbols[n].containedSymbols[m].x = this.laneSymbols[n].containedSymbols[m].serverSideCoordinates.x;
									this.laneSymbols[n].containedSymbols[m].adjustGeometry();
								}
							}
						}
						else{
							if (this.laneSymbols[n].symbolYOffset) {
								laneMinimized = true;
								this.laneSymbols[n].y = this.laneSymbols[n].serverSideCoordinates.y;
								for ( var m in this.laneSymbols[n].containedSymbols) {
									this.laneSymbols[n].containedSymbols[m].y = this.laneSymbols[n].containedSymbols[m].serverSideCoordinates.y;
									this.laneSymbols[n].containedSymbols[m].adjustGeometry();
								}
							}
						}
					}
					if(laneMinimized){
						this.adjustChildSymbols();
					}
				};

				/**
				 *
				 */
				PoolSymbol.prototype.adjustChildSymbols = function() {
					var topMargin = m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
							+ m_constants.POOL_SWIMLANE_MARGIN;
					if (this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
						var currentX = this.x
								+ m_constants.POOL_SWIMLANE_MARGIN;

						//Check if there has been a vertical shift
						//(due to a lane being shrunk from top)
						var dY = 0;
						for ( var n in this.laneSymbols) {
							var dYNew = (this.y + 2
									* m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT + m_constants.POOL_SWIMLANE_MARGIN)
									- this.laneSymbols[n].y;
							// If child symbols are on lane header, dY is set to
							// move the child symbols
							for ( var c in this.laneSymbols[n].containedSymbols) {
								if (this.laneSymbols[n].containedSymbols[c].y <= dYNew) {
									if (dY < dYNew) {
										dY = dYNew;
									}
								}
							}
						}

						var swimLaneHeight = 0;
						for ( var n in this.laneSymbols) {
							var dX = currentX - this.laneSymbols[n].x;
							var laneYMargin = topMargin - this.laneSymbols[n].y;
							if (dX != 0) {
								this.laneSymbols[n].moveBy(dX, 0);
							}
							if (laneYMargin != 0) {
								this.laneSymbols[n].moveBy(0, laneYMargin);
							}

							//adjust left side swim-lane boundaries
							var symbolX = (this.laneSymbols[n].x - dX)
							+ m_constants.SWIMLANE_SYMBOL_MARGIN;

							var symbolDx = this.evaluateSymbolDx(symbolX, this.laneSymbols[n].containedSymbols);

							for ( var c in this.laneSymbols[n].containedSymbols) {
								this.laneSymbols[n].containedSymbols[c].moveBy(
										dX + symbolDx, dY);
							}

							this.laneSymbols[n].recalculateBoundingBox();

							if (swimLaneHeight < this.laneSymbols[n].height) {
								swimLaneHeight = this.laneSymbols[n].height;
							}

							currentX += this.laneSymbols[n].width;
							currentX += m_constants.POOL_SWIMLANE_MARGIN;

							this.laneSymbols[n].adjustGeometry();
						}
						//Adjust height of lanes if required
						for ( var n in this.laneSymbols) {
							this.laneSymbols[n].height = swimLaneHeight;
							this.laneSymbols[n].adjustGeometry();
						}
					} else {
						var dX = 0;
						var symbolX = (this.x + 2
								* m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT + m_constants.POOL_SWIMLANE_MARGIN) + m_constants.SWIMLANE_SYMBOL_MARGIN;
						for ( var n in this.laneSymbols) {
							this.laneSymbols[n].x = this.x + topMargin;
							// If child symbols are on lane header, dX is set to
							// move the child symbols
							var symbolDx = this.evaluateSymbolDx(symbolX, this.laneSymbols[n].containedSymbols);
							if (dX < symbolDx) {
								dX = symbolDx;
							}
						}

						var currentY = this.y
								+ m_constants.POOL_SWIMLANE_MARGIN;

						var swimLaneWidth = 0;
						for ( var n in this.laneSymbols) {
							var dY = currentY - this.laneSymbols[n].y;

							if (dY != 0) {
								this.laneSymbols[n].moveBy(0, dY);
							}

						 	for ( var c in this.laneSymbols[n].containedSymbols) {
								this.laneSymbols[n].containedSymbols[c].moveBy(
										dX, dY);
							}

							this.laneSymbols[n].recalculateBoundingBox();
							if (swimLaneWidth < this.laneSymbols[n].width) {
								swimLaneWidth = this.laneSymbols[n].width;
							}

							currentY += this.laneSymbols[n].height;
							currentY += m_constants.POOL_SWIMLANE_MARGIN;

							this.laneSymbols[n].adjustGeometry();
						}

						//Adjust width
						for ( var n in this.laneSymbols) {
							this.laneSymbols[n].width = swimLaneWidth;
							this.laneSymbols[n].adjustGeometry();
						}
					}

					this.recalculateBoundingBox();
					this.adjustPrimitives();

					/* Call hideSnapLines, as the moveBy function invokes checkSnaplines causing the
					 * snap lines to be created. */
					this.diagram.hideSnapLines();
				};

				/**
				 * internal method to evaluate effective dX
				 */
				PoolSymbol.prototype.evaluateSymbolDx = function(x, containedSymbols) {
					var dX = 0;
						// If child symbols are on lane header, dX is set to
						// move the child symbols
					for ( var c in containedSymbols) {
						if (containedSymbols[c].x < x) {
							var moveBy = x - containedSymbols[c].x;
							if (dX < moveBy) {
								dX = moveBy;
							}
						}
					}

					return dX;
				};

				/**
				 * internal method to evaluate effective dY
				 */

				PoolSymbol.prototype.evaluateSymbolDy = function(y, containedSymbols) {
					var dY = 0;
						// If child symbols are on lane header, dY is set to
						// move the child symbols
					for ( var c in containedSymbols) {
						if (containedSymbols[c].y < y) {
							var moveBy = y - containedSymbols[c].y;
							if (dY < moveBy) {
								dY = moveBy;
							}
						}
					}

					return dY;
				};

				/**
				 *
				 */
				PoolSymbol.prototype.flipFlowOrientation = function(
						flowOrientation) {
					this.orientation = flowOrientation;

					var changeDescriptionsPool = [];

					var laneChangeDescs;
					for ( var n in this.laneSymbols) {
						laneChangeDescs = this.laneSymbols[n]
								.flipFlowOrientation(flowOrientation);

						changeDescriptionsPool = changeDescriptionsPool
								.concat(laneChangeDescs);
					}

					return changeDescriptionsPool;
				};

				/**
				 *
				 */
				PoolSymbol.prototype.dragStart = function() {
					// Do nothing
				};

				/**
				 *
				 */
				PoolSymbol.prototype.drag = function(dX, dY, x, y) {
					// Do nothing
				};

				/**
				 *
				 */
				PoolSymbol.prototype.dragStop = function() {
					// Do nothing
				};

				/**
				 *
				 */
				PoolSymbol.prototype.createFlyOutMenuBackground = function(x,
						y, height, width) {
					this.flyOutMenuBackground = m_canvasManager
							.drawRectangle(
									this.x,
									this.y,
									m_constants.DEFAULT_FLY_OUT_MENU_WIDTH,
									m_constants.DEFAULT_FLY_OUT_MENU_HEIGHT,
									{
										"stroke" : m_constants.FLY_OUT_MENU_STROKE,
										"stroke-width" : m_constants.FLY_OUT_MENU_STROKE_WIDTH,
										'fill' : m_constants.FLY_OUT_MENU_FILL,
										'fill-opacity' : m_constants.FLY_OUT_MENU_START_OPACITY,
										'r' : m_constants.FLY_OUT_MENU_R
									});

					// Initialize return pointer for closure

					this.flyOutMenuBackground.auxiliaryProperties = {
						callbackScope : this
					};

					this.flyOutMenuBackground.hover(
							PoolSymbol_hoverInFlyOutMenuClosure,
							PoolSymbol_hoverOutFlyOutMenuClosure);
				};

				/**
				 *
				 */
				PoolSymbol.prototype.initializeEventHandling = function() {
					// this.borderRectangle.auxiliaryProperties.callbackScope =
					// this;

				};

				/**
				 *
				 */
				PoolSymbol.prototype.adjustFlyOutMenu = function(x, y, width,
						height) {
					this.flyOutMenuBackground.attr({
						'x' : x,
						'y' : y
					});

					this.adjustFlyOutMenuItems(x, y, width, height);
				};

				/**
				 *
				 */
				PoolSymbol.prototype.createFlyOutMenu = function() {
				};

				/**
				 *
				 */
				PoolSymbol.prototype.createProximitySensorPrimitive = function() {
					var POOL_PROXIMITY_SENSOR_WIDTH = 3;
					return m_canvasManager.drawRectangle(this.x, this.y
							+ m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT,
							this.width, POOL_PROXIMITY_SENSOR_WIDTH, {
								"stroke" : "white",
								"stroke-width" : 0,
								"fill" : "white",
								"fill-opacity" : 0,
								"r" : 0
							});
				};

				/**
				 *
				 * @param x
				 * @param y
				 * @param width
				 * @param height
				 */
				PoolSymbol.prototype.adjustProximitySensor = function(x, y,
						width, height) {
					// Do nothing
				};

				/**
				 *
				 */
				PoolSymbol.prototype.findContainerSymbol = function(x, y) {
					// TODO Add recursion for nested swimlanes
					for ( var n in this.laneSymbols) {
						if (this.laneSymbols[n].containsPosition(x, y)) {
							return this.laneSymbols[n];
						}
					}

					return null;
				};

				/**
				 *
				 */
				PoolSymbol.prototype.findLane = function(id) {
					// TODO Add recursion for nested swimlanes
					for ( var n in this.laneSymbols) {
						if (this.laneSymbols[n].id == id) {
							return this.laneSymbols[n];
						}
					}

					return null;
				};

				/**
				 *
				 */
				PoolSymbol.prototype.requiresParentSymbol = function() {
					return false;
				};

				/**
				 *
				 */
				PoolSymbol.prototype.getLaneIndex = function() {
					if (laneIndex <= this.laneSymbols.length) {
						laneIndex = this.laneSymbols.length + 1;
					}

					while (true == this.hasLaneWithName("Lane " + laneIndex)) {
						laneIndex++;
					}

					return laneIndex;
				};

				/**
				 *
				 */
				PoolSymbol.prototype.hasLaneWithName = function(name) {
					for ( var n in this.laneSymbols) {
						if (this.laneSymbols[n].name == name) {
							return true;
						}
					}

					return false;
				};

				PoolSymbol.prototype.getAllDataSymbols = function(dataSymbols) {
					for ( var n in this.laneSymbols) {
						this.laneSymbols[n].getAllDataSymbols(dataSymbols);
					}

					return dataSymbols;
				};

				PoolSymbol.prototype.removeLane = function(laneSymbol) {
					/* remove lane from lane symbols array. */
					m_utils.removeItemFromArray(this.laneSymbols, laneSymbol);
					m_utils.removeItemFromArray(this.diagram.symbols,
							laneSymbol);
					/* Create a default lane if the very last lane was deleted. */
					if(this.laneSymbols.length == 0) {
						// Lane Index is reinitialized when all lanes are
						// deleted
						laneIndex = 0;
						this.createSwimlaneSymbol();
					}

					this.recalculateBoundingBox();
					this.adjustGeometry();
				};
			}

			function PoolSymbol_hoverInFlyOutMenuClosure() {
				//this.auxiliaryProperties.callbackScope.showFlyOutMenu();
			}

			function PoolSymbol_hoverOutFlyOutMenuClosure() {
				this.auxiliaryProperties.callbackScope.hideFlyOutMenu();
			}

		});