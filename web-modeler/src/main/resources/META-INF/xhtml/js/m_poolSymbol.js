/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_commandsController", "m_command",
				"m_canvasManager", "m_symbol", "m_swimlaneSymbol" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_canvasManager, m_symbol, m_swimlaneSymbol) {

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

					// TODO Hack; multiple pool Symbols

					diagram.poolSymbol = json;

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

				this.type = m_constants.POOL_SYMBOL;
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
					this.diagram = diagram;
					this.orientation = diagram.flowOrientation;
					this.borderRectangle = null;
					this.topRectangle = null;
					this.text = null;
					this.name = "Default Pool";
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

					// TODO Verify

					if (!hasLanes) {
						this.createDefaultLane();
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
									this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? this.width
											: m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT,
									this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
											: this.height,
									{
										"fill" : m_constants.POOL_COLOR,
										"stroke" : m_constants.POOL_COLOR,
										"stroke-width" : m_constants.POOL_SWIMLANE_STROKE_WIDTH
									});

					this.addToPrimitives(this.topRectangle);

					this.text = m_canvasManager
							.drawTextNode(
									this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? (this.x + 0.5 * this.width)
											: (this.x + 0.5 * m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT),
									this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? (this.y + 0.5 * m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT)
											: (this.y + 0.5 * this.height),
									this.name)
							.attr(
									{
										"text-anchor" : "middle",
										"fill" : "white",
										"font-family" : m_constants.DEFAULT_FONT_FAMILY,
										"font-size" : m_constants.DEFAULT_FONT_SIZE,
										"font-weight" : "bold"
									});

					this.text
							.rotate(this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? 0
									: -90);
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
											+ (this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? 0
													: m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT),
									this.y
											+ m_constants.POOL_SWIMLANE_MARGIN
											+ (this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
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
				PoolSymbol.prototype.createSwimlaneSymbolFromParticipant = function(
						participant) {
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
											+ (this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? 0
													: m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT),
									this.y
											+ m_constants.POOL_SWIMLANE_MARGIN
											+ (this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
													: 0));

					if (participant != null) {
						swimlaneSymbol.refreshFromModelElement();
					}

					this.recalculateBoundingBox();
					this.adjustGeometry();

					//The create REST call for swimlanes is made after the swimlabe is created and re-positioned.
					swimlaneSymbol.createAndSubmitCreateCommand();
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
					if (this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
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
					if (this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
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
				 *
				 */
				PoolSymbol.prototype.adjustPrimitives = function(dX, dY) {
					if (this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
						this.borderRectangle.attr({
							"width" : this.width,
							"height" : this.height
						});
						this.topRectangle.attr({
							"width" : this.width,
							"height" : m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
						});
						this.text.attr({
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
							"y" : this.y + 0.5 * this.height
						});
					}
				};

				/**
				 *
				 */
				PoolSymbol.prototype.adjustChildSymbols = function() {
					if (this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
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

						for ( var n in this.laneSymbols) {
							var dX = currentX - this.laneSymbols[n].x;
							this.laneSymbols[n].x = currentX;
							this.laneSymbols[n].y = this.y
									+ m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
									+ m_constants.POOL_SWIMLANE_MARGIN;
							currentX += this.laneSymbols[n].width;
							currentX += m_constants.POOL_SWIMLANE_MARGIN;

							for (var c in this.laneSymbols[n].containedSymbols) {
								this.laneSymbols[n].containedSymbols[c].moveBy(dX, dY);
							}

							this.laneSymbols[n].adjustGeometry();
						}
					} else {
						var currentY = this.y
								+ m_constants.POOL_SWIMLANE_MARGIN;

						for ( var n in this.laneSymbols) {
							var dY = currentY - this.laneSymbols[n].y;

							this.laneSymbols[n].x = this.x
									+ m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
									+ m_constants.POOL_SWIMLANE_MARGIN;
							this.laneSymbols[n].y = currentY;
							currentY += this.laneSymbols[n].height;
							currentY += m_constants.POOL_SWIMLANE_MARGIN;

							for (var c in this.laneSymbols[n].containedSymbols) {
								this.laneSymbols[n].containedSymbols[c].moveBy(0, dY);
							}

							this.laneSymbols[n].adjustGeometry();
						}
					}

					/* Call hideSnapLines, as the moveBy function invokes checkSnaplines causing the
					 * snap lines to be created. */
					this.diagram.hideSnapLines();
				};

				/**
				 *
				 */
				PoolSymbol.prototype.flipFlowOrientation = function(
						flowOrientation) {
					this.orientation = flowOrientation;

					for ( var n in this.laneSymbols) {
						this.laneSymbols[n]
								.flipFlowOrientation(flowOrientation);
					}
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

					if (laneIndex == 1) {
						for ( var n in this.laneSymbols) {
							++laneIndex;
						}
					} else {
						++laneIndex;
					}

					return laneIndex;
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

					/* Create a default lane if the very last lane was deleted. */
					if(this.laneSymbols.length == 0) {
						this.createDefaultLane();
					}

					this.recalculateBoundingBox();
					this.adjustGeometry();
				};
			}

			function PoolSymbol_hoverInFlyOutMenuClosure() {
				this.auxiliaryProperties.callbackScope.showFlyOutMenu();
			}

			function PoolSymbol_hoverOutFlyOutMenuClosure() {
				this.auxiliaryProperties.callbackScope.hideFlyOutMenu();
			}
		});