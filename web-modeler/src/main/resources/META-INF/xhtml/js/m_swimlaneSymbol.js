/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_commandsController", "m_command",
				"m_canvasManager", "m_model", "m_symbol", "m_activitySymbol",
				"m_gatewaySymbol", "m_eventSymbol", "m_dataSymbol", "m_annotationSymbol",
				"m_propertiesPanel", "m_swimlanePropertiesPanel","m_modelerUtils" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_canvasManager, m_model, m_symbol, m_activitySymbol,
				m_gatewaySymbol, m_eventSymbol, m_dataSymbol, m_annotationSymbol,
				m_propertiesPanel, m_swimlanePropertiesPanel, m_modelerUtils) {

			return {
				createSwimlaneSymbol : function(diagram, parentSymbol) {
					var laneSymbol = new SwimlaneSymbol();
					var laneIndex = parentSymbol.getLaneIndex();
					laneSymbol.name = "Lane " + laneIndex;
					laneSymbol.id = m_utils.generateIDFromName(laneSymbol.name);
					laneSymbol.bind(diagram, parentSymbol);

					return laneSymbol;
				},

				createSwimlaneSymbolFromParticipant : function(diagram,
						parentSymbol, participant) {
					var laneSymbol = new SwimlaneSymbol(diagram);

					laneSymbol.bind(diagram, parentSymbol);
					var laneIndex = parentSymbol.getLaneIndex();
					laneSymbol.name = "Lane " + laneIndex;
					laneSymbol.id = m_utils.generateIDFromName(laneSymbol.name)
					laneSymbol.participantFullId = participant.getFullId();

					return laneSymbol;
				},

				createSwimlaneSymbolFromJson : function(diagram, parentSymbol,
						json) {

					// TODO Ugly
					m_utils.inheritFields(json, m_symbol.createSymbol());
					m_utils.inheritMethods(json, new SwimlaneSymbol(diagram));

					json.bind(diagram, parentSymbol);
					json.initializeFromJson();

					return json;
				}
			};

			/**
			 *
			 */
			function SwimlaneSymbol() {
				var symbol = m_symbol.createSymbol();

				m_utils.inheritFields(this, symbol);
				m_utils.inheritMethods(SwimlaneSymbol.prototype, symbol);

				this.width = 0;
				this.height = 0;
				this.id = "DefaultLane";
				this.text ="Default Lane";
				this.description = null;
				this.participantFullId = null;
				this.participantName = null;
				this.minimizeIcon = null;
				this.cacheWidth = 0;
				this.maximizeIcon = null;
				this.minimized = false;
				this.symbolXOffset = 0;

				/**
				 * Binds all client-side aspects to the object (graphics
				 * objects, diagram, base classes).
				 */
				SwimlaneSymbol.prototype.bind = function(diagram, parentSymbol) {
					this.type = m_constants.SWIMLANE_SYMBOL;

					this.diagram = diagram;
					this.orientation = diagram.flowOrientation;
					this.parentSymbol = parentSymbol;

					var laneHeight;
					if (this.parentSymbol
							&& this.parentSymbol.laneSymbols
							&& parseInt(this.parentSymbol.laneSymbols.length) > 0) {
						laneHeight = this.parentSymbol.laneSymbols[0].height;
					} else {
						laneHeight = diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? m_constants.LANE_DEFAULT_HEIGHT
								: m_constants.LANE_DEFAULT_WIDTH;
					}
					// TODO Hack to only apply it to new symbols

					if (this.width == 0) {
						this.width = diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? m_constants.LANE_DEFAULT_WIDTH
								: m_constants.LANE_DEFAULT_HEIGHT;
						this.height = laneHeight;
					}

					this.containedSymbols = [];
					this.propertiesPanel = m_swimlanePropertiesPanel
							.getInstance();
					this.borderRectangle = null;
					this.topRectangle = null;
					this.text = null;
					this.minimizeIcon = null;
					this.maximizeIcon = null;
					this.symbolXOffset = 0;

					if (!this.comments) {
						this.comments = [];
					}
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.toString = function() {
					return "Lightdust.SwimlaneSymbol";
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.initializeFromJson = function() {
					this.prepareNoPosition();
					this.completeNoTransfer();
					this.updateServerSideCoordinates();

					for ( var n in this.activitySymbols) {
						var activitySymbol = m_activitySymbol
								.createActivitySymbolFromJson(this.diagram,
										this, this.activitySymbols[n]);
						this.activitySymbols[n].updateServerSideCoordinates();
					}

					// Create gateways

					for ( var n in this.gatewaySymbols) {
						m_gatewaySymbol.createGatewaySymbolFromJson(
								this.diagram, this, this.gatewaySymbols[n]);
						this.gatewaySymbols[n].updateServerSideCoordinates();
					}

					// Create event symbols

					for ( var n in this.eventSymbols) {
						m_eventSymbol.createEventSymbolFromJson(this.diagram,
								this, this.eventSymbols[n]);
						this.eventSymbols[n].updateServerSideCoordinates();
					}

					// Create data symbols

					for ( var n in this.dataSymbols) {
						m_dataSymbol.createDataSymbolFromJson(this.diagram,
								this, this.dataSymbols[n]);
						this.dataSymbols[n].updateServerSideCoordinates();
					}
					// Create data symbols

					for ( var n in this.annotationSymbols) {
						m_annotationSymbol.createFromJson(this.diagram,
								this, this.annotationSymbols[n]);
						this.annotationSymbols[n].updateServerSideCoordinates();
					}
				};

				/**
				 * Overridden as we do now want the create REST call to be made at this point but
				 * after the swimlane is repositioned.
				 */
				SwimlaneSymbol.prototype.complete = function() {
					this.completeNoTransfer(this);

					if (this.requiresParentSymbol())
					{
						// TODO Needs to be called on create, otherwise it may be aparllel calls
						//this.diagram.submitUpdate();
					}
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.createTransferObject = function() {
					var transferObject = {};

					m_utils.inheritFields(transferObject, this);

					transferObject = this.prepareTransferObject(transferObject);

					// TODO Recursively for children

					transferObject.containedSymbols = null;
					transferObject.borderRectangle = null;
					transferObject.topRectangle = null;
					transferObject.text = null;
					transferObject.laneSymbols = [];
					transferObject.minimizeIcon = null;
					transferObject.maximizeIcon = null;

					for ( var laneSymbol in this.laneSymbols) {
						transferObject.laneSymbols[laneSymbol] = this.laneSymbols[laneSymbol]
								.createTransferObject();
					}

					transferObject.containedSymbols = [];

					for ( var symbol in this.containedSymbols) {
						transferObject.containedSymbols[symbol] = this.containedSymbols[symbol]
								.createTransferObject();
					}

					// TODO Move to contained symbols?

					transferObject.activitySymbols = {};

					for ( var activitySymbol in this.activitySymbols) {
						transferObject.activitySymbols[activitySymbol] = this.activitySymbols[activitySymbol]
								.createTransferObject();
					}

					transferObject.gatewaySymbols = {};

					for ( var gatewaySymbol in this.gatewaySymbols) {
						transferObject.gatewaySymbols[gatewaySymbol] = this.gatewaySymbols[gatewaySymbol]
								.createTransferObject();
					}

					transferObject.eventSymbols = {};

					for ( var eventSymbol in this.eventSymbols) {
						transferObject.eventSymbols[eventSymbol] = this.eventSymbols[eventSymbol]
								.createTransferObject();
					}

					transferObject.dataSymbols = {};

					for ( var dataSymbol in this.dataSymbols) {
						transferObject.dataSymbols[dataSymbol] = this.dataSymbols[dataSymbol]
								.createTransferObject();
					}

					transferObject.annotationSymbols = {};

					for ( var annotationSymbol in this.annotationSymbols) {
						transferObject.annotationSymbols[annotationSymbol] = this.annotationSymbols[annotationSymbol]
								.createTransferObject();
					}

					return transferObject;
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.getPath = function(withId) {
					var path = "/models/" + this.diagram.model.id
							+ "/processes/" + this.diagram.process.id
							+ "/lanes";

					if (withId) {
						path += "/" + this.id;
					}

					return path;
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.isContainerSymbol = function() {
					return true;
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.createPrimitives = function() {
					this.borderRectangle = m_canvasManager
							.drawRectangle(
									this.x,
									this.y,
									this.width,
									this.height,
									{
										'stroke' : m_constants.SWIMLANE_COLOR,
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
										"fill" : m_constants.SWIMLANE_COLOR,
										"stroke" : m_constants.SWIMLANE_COLOR,
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
										"transform" : this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? "R0"
												: "R90",
										"text-anchor" : "middle",
										"fill" : "white",
										"font-family" : m_constants.DEFAULT_FONT_FAMILY,
										"font-size" : m_constants.DEFAULT_FONT_SIZE,
										"font-weight" : "bold"
									});

					this.addToPrimitives(this.text);


					this.minimizeIcon = m_canvasManager
							.drawImageAt(
									"../../images/icons/min.png",
									this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? (this.x
											+ this.width - 20)
											: (this.x + 0.5 * m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT),
									this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? (this.y + 1.2 * m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT)
											: (this.y + 0.5 * this.height), 16,
									16);

					this.addToPrimitives(this.minimizeIcon);

					this.maximizeIcon = m_canvasManager
							.drawImageAt(
									"../../images/icons/max.png",
									this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? (this.x
											+ this.width - 20)
											: (this.x + 0.5 * m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT),
									this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? (this.y + 1.2 * m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT)
											: (this.y + .35 * this.height), 16,
									16);
					this.maximizeIcon.hide();
					this.addToPrimitives(this.maximizeIcon);
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.adjustPrimitives = function(dX, dY) {
					this.borderRectangle.attr({
						"x" : this.x,
						"y" : this.y,
						"width" : this.width,
						"height" : this.height
					});

					if (this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
						this.topRectangle.attr({
							"x" : this.x,
							"y" : this.y,
							"width" : this.width,
							"height" : m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
						});
						this.text.attr({
							"transform" : "R0",
							"x" : this.x + 0.5 * this.width,
							"y" : this.y + 0.5
									* m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
						});
						this.minimizeIcon.attr({
							"x" : this.x + this.width - 20,
							"y" : this.y + 0.15
									* m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
						});
						this.maximizeIcon.attr({
							"x" : this.x + this.width - 20,
							"y" : this.y + 0.12
									* m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
						});

					} else {
						this.topRectangle.attr({
							"x" : this.x,
							"y" : this.y,
							"width" : m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT,
							"height" : this.height
						});
						this.text.attr({
							"transform" : "R90",
							"x" : this.x + 0.5
									* m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT,
							"y" : this.y + 0.5 * this.height
						});
					}
				};

				/**
				 * TODO Adjust contained symbols
				 */
				SwimlaneSymbol.prototype.flipFlowOrientation = function(
						flowOrientation) {
					this.orientation = flowOrientation;

					var buffer = this.width;

					this.width = this.height;
					this.height = buffer;

					for ( var n in this.containedSymbols) {
						this.containedSymbols[n]
								.flipFlowOrientation(flowOrientation);
					}
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.createFlyOutMenuBackground = function(
						x, y, height, width) {
					this.flyOutMenuBackground = m_canvasManager
							.drawRectangle(
									this.x,
									this.y,
									m_constants.DEFAULT_FLY_OUT_MENU_WIDTH,
									m_constants.DEFAULT_FLY_OUT_MENU_HEIGHT,
									{
										"stroke" : m_constants.FLY_OUT_MENU_STROKE,
										"stroke-width" : m_constants.FLY_OUT_MENU_STROKE_WIDTH,
										"fill" : m_constants.FLY_OUT_MENU_FILL,
										"fill-opacity" : m_constants.FLY_OUT_MENU_START_OPACITY,
										"r" : m_constants.FLY_OUT_MENU_R
									});

					// Initialize return pointer for closure

					this.flyOutMenuBackground.auxiliaryProperties = {
						callbackScope : this
					};

					this.flyOutMenuBackground.hover(
							SwimlaneSymbol_hoverInFlyOutMenuClosure,
							SwimlaneSymbol_hoverOutFlyOutMenuClosure);
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.initializeEventHandling = function() {
					this.borderRectangle.auxiliaryProperties.callbackScope = this;

					this.borderRectangle.click(SwimlaneSymbol_clickClosure);
					this.borderRectangle.hover(SwimlaneSymbol_hoverInClosure,
							SwimlaneSymbol_hoverOutClosure);
					this.topRectangle.auxiliaryProperties.callbackScope = this;

					this.topRectangle.click(SwimlaneSymbol_clickClosure);
					this.topRectangle.hover(SwimlaneSymbol_topRect_hoverInClosure,
							SwimlaneSymbol_topRect_hoverOutClosure);
					this.minimizeIcon
							.click(SwimlaneSymbol_minimizeClickClosure);
					this.maximizeIcon
							.click(SwimlaneSymbol_maximizeClickClosure);
				};


				/**
				 *
				 */
				SwimlaneSymbol.prototype.onMinimizeIconClick = function() {
					this.cacheWidth = this.width;
					this.width = m_constants.LANE_MIN_WIDTH;
					this.minimizeIcon.hide();
					this.maximizeIcon.show();
					this.parentSymbol.recalculateBoundingBox();
					this.parentSymbol.adjustPrimitives();
					this.parentSymbol.adjustAuxiliaryElements();
					for ( var n in this.containedSymbols) {
						this.containedSymbols[n].hide();
					}
					this.parentSymbol.updateLanesOffsetAndAdjustChild(this, true);
					var str = this.text.attr("text");
					if (str.length > 8) {
						this.text.attr("text", str.substring(0, 8) + " ...");
					}
					this.minimized = true;
					this.diagram.hideSnapLines();
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.onMaximizeIconClick = function() {
					this.parentSymbol.updateLanesOffsetAndAdjustChild(this, false);
					this.width = this.cacheWidth;
					this.maximizeIcon.hide();
					this.minimizeIcon.show();
					this.minimized = false;
					this.text.attr("text", this.name);
					this.parentSymbol.recalculateBoundingBox();
					this.parentSymbol.adjustPrimitives();
					this.parentSymbol.adjustAuxiliaryElements();
					for ( var n in this.containedSymbols) {
						this.containedSymbols[n].show();
					}
					this.refresh();
					this.diagram.hideSnapLines();
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.adjustFlyOutMenu = function(x, y,
						width, height) {
					this.flyOutMenuBackground.attr({
						'x' : x,
						'y' : y
					});

					this.adjustFlyOutMenuItems(x, y, width, height);
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.adjustFlyOutMenuItems = function(x, y,
						width, height) {
					var n = 0;
					while (n < this.rightFlyOutMenuItems.length) {
						this.rightFlyOutMenuItems[n].attr({
							x : x + 15,
							y : y +5
						});
						n++;
					}
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.createFlyOutMenu = function() {
					this.addFlyOutMenuItems(
							[],[{
								imageUrl : "../../images/icons/remove.png",
								imageWidth : 16,
								imageHeight : 16,
								clickHandler : SwimlaneSymbol_removeClosure
							}],[]);
				};



				/**
				 *
				 */
				SwimlaneSymbol.prototype.createProximitySensorPrimitive = function() {
					var offset = m_constants.PROXIMITY_SENSOR_MARGIN / 2;
					var pathString = "M" + (this.x + offset) + "," + (this.y + m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT + offset) +
									 "L" + (this.x + this.width - offset) + "," + (this.y + m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT + offset) +
									 "L" + (this.x + this.width - offset) + "," + (this.y + m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT - offset) +
									 "L" + (this.x + offset) + "," + (this.y + m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT - offset) +
									 "Z";
					return m_canvasManager.drawPath(pathString, {
						"stroke" : "white",
						"stroke-width" : m_constants.PROXIMITY_SENSOR_MARGIN,
						"opacity" : 0
					});
				};

				/**
				 *
				 * @param x
				 * @param y
				 * @param width
				 * @param height
				 */
				SwimlaneSymbol.prototype.adjustProximitySensor = function(x, y,
						width, height) {
					var offset = m_constants.PROXIMITY_SENSOR_MARGIN / 2;
					var pathString = "M" + (this.x + offset) + "," + (this.y + m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT + offset) +
									 "L" + (this.x + this.width - offset) + "," + (this.y + m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT + offset) +
									 "L" + (this.x + this.width - offset) + "," + (this.y + this.height - offset) +
									 "L" + (this.x + offset) + "," + (this.y + this.height - offset) +
									 "Z";

					this.proximitySensor.attr("path", pathString);
				};

				SwimlaneSymbol.prototype.click = function(x, y, event) {
					this.select();
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.proximityHoverIn = function(event) {
					if (this.diagram.isInNormalMode()) {
						var scrollPos = m_modelerUtils.getModelerScrollPosition();
						var xPos=event.pageX - this.diagram.X_OFFSET;
						var yPos=event.pageY - this.diagram.Y_OFFSET;

						var offset = m_constants.PROXIMITY_SENSOR_MARGIN;
						//the lane x co-ord , width minus proximity width will give the right proximity margin
						var rigthProximityMargin=this.x + this.width - offset - this.diagram.X_OFFSET;
						// the lane x co-ord and proximity width will give the left proximity margin
						var leftProximityMargin=this.x + offset;
						// the lane y co-ord ,TopBoxHeight and proximity width will give the top proximity margin
						var topProximityMargin=this.y + m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT + offset;
						// the lane height minus proximity width will give the bottom proximity margin
						var bottomProximityMargin = this.height - offset -this.diagram.Y_OFFSET;

						// If the mouse pointer is on edge of top header
						// the flyout menu should appear below Header and within swimlane
						if (yPos <= topProximityMargin) {
							if (xPos >= rigthProximityMargin) {
								xPos = rigthProximityMargin - offset;
							}
							yPos = topProximityMargin - offset;
							this.adjustFlyOutMenu(xPos + scrollPos.left, yPos);
						}
						else if((rigthProximityMargin - scrollPos.left) < parseInt(xPos.valueOf())){
							this.adjustFlyOutMenu(rigthProximityMargin - offset,yPos+scrollPos.top);
						}
						else if((leftProximityMargin - scrollPos.left) > parseInt(xPos.valueOf())){
							this.adjustFlyOutMenu(leftProximityMargin -  offset,yPos+scrollPos.top);
						}
						else if(bottomProximityMargin < parseInt(yPos.valueOf())){
							this.adjustFlyOutMenu(xPos + scrollPos.left, this.y
									+ bottomProximityMargin + scrollPos.top + m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT);
						}
 						else {
							return;
						}
						this.showFlyOutMenu();
					}
				};


				/**
				 *
				 */
				SwimlaneSymbol.prototype.proximityHoverOut = function(event) {
					if (this.diagram.isInNormalMode()) {
						this.hideFlyOutMenu();
					}
				};

				/**
				 *
				 */
				/*
				 * SwimlaneSymbol.prototype.deselect = function() {
				 * this.selected = false; this.borderRectangle.attr("stroke",
				 * m_constants.SWIMLANE_COLOR); };
				 */

				/**
				 *
				 */
				SwimlaneSymbol.prototype.refreshFromModelElement = function() {
					if (this.participantFullId != null) {
						var participant = m_model
								.findParticipant(this.participantFullId);

						this.text.attr("text", this.name + " ("
								+ participant.name + ")");
					} else {
						this.text.attr("text", this.name);
					}
					this.participantName = this.text.attr("text");
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.remove = function(){

					/* Remove child symbols. */
					while (this.containedSymbols.length > 0) {
						this.containedSymbols[0].remove();
					}

					this.removePrimitives();
					this.removeFlyOutMenu();
					this.removeProximitySensor();

					this.parentSymbol.removeLane(this);
				}

				/**
				 *
				 */
				SwimlaneSymbol.prototype.hoverIn = function(x,y) {
					this.showPointerCursor();
					this.borderRectangle.attr("stroke",
							m_constants.SELECT_STROKE_COLOR);
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.topRectangleHoverIn = function(event){
					if (this.diagram.isInNormalMode()) {
						this.showPointerCursor();
						this.topRectangle.attr({
							"stroke" : m_constants.SELECT_STROKE_COLOR,
							"fill" : m_constants.SELECT_STROKE_COLOR
						});

						var scrollPos = m_modelerUtils
								.getModelerScrollPosition();
						var xPos = event.pageX - this.diagram.X_OFFSET
								+ scrollPos.left;
						var yPos = event.pageY - this.diagram.Y_OFFSET
								+ scrollPos.top;
						// get the right x margin
						var xMargin = this.x + this.width;
						// if the box extends from the box, move to left
						if (xPos + m_constants.DEFAULT_FLY_OUT_MENU_WIDTH > xMargin) {
							xPos = xPos
									- m_constants.DEFAULT_FLY_OUT_MENU_WIDTH;
						}
						this.adjustFlyOutMenu(xPos, this.y
								+ m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT);
						this.showFlyOutMenu();
					}
				};


				/**
				 *
				 */
				SwimlaneSymbol.prototype.hoverOut = function() {
					this.showDefaultCursor();
					this.topRectangle.attr({
						"stroke" : m_constants.SWIMLANE_COLOR,
						"fill" : m_constants.SWIMLANE_COLOR
					});
					this.borderRectangle.attr("stroke",
							m_constants.SWIMLANE_COLOR);
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.dragStart = function() {
					// Do nothing
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.drag = function(dX, dY, x, y) {
					// Do nothing
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.dragStop = function() {
					// Do nothing
				};

				/**
				 * Temporarily commented out as left stretch is disabled
				 */
				SwimlaneSymbol.prototype.stretchLeft = function(dX, dY, x, y) {
					if (!this.diagram.isInConnectionMode()) {
						this.width = this.preDragState.width - dX;
						this.x = this.preDragState.x + dX;

						this.adjustGeometry();
					}
				};

				/**
				 * Temporarily commented out as top stretch is disabled
				 */
				SwimlaneSymbol.prototype.stretchTop = function(dX, dY, x, y) {
					if (!this.diagram.isInConnectionMode()) {
						this.height = this.preDragState.height - dY;
						this.y = this.preDragState.y + dY;

						this.adjustGeometry();
					}
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.stretchRight = function(dX, dY, x, y) {
					if (!this.diagram.isInConnectionMode()) {
						this.width = this.preDragState.width + dX;

						this.adjustGeometry();
					}
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.stretchBottom = function(dX, dY, x, y) {
					if (!this.diagram.isInConnectionMode()) {
						this.height = this.preDragState.height + dY;

						this.adjustGeometry();
					}
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.stretchStop = function() {

					// Check if the minimizing of the lane height is permissible given that
					// adjoining lanes may have content longer than this lane.
					var isStretchWithinLimitForOtherLanes = true;
					for (var i = 0; i < this.parentSymbol.laneSymbols.length; i++) {
						var childrenBindingRect = this.parentSymbol.laneSymbols[i].getChildSymbolsBindingRect();
						if (parseInt(this.y) > parseInt(childrenBindingRect.top)
								|| parseInt(this.y + this.height) < parseInt(childrenBindingRect.bottom)) {
							isStretchWithinLimitForOtherLanes = false;
						}
					}

					if (isStretchWithinLimitForOtherLanes == false
							|| parseInt(this.x) > parseInt(this.preDragState.containedSymbolsLeft)
							|| parseInt(this.x + this.width) < parseInt(this.preDragState.containedSymbolsRight)
							|| parseInt(this.y) > parseInt(this.preDragState.containedSymbolsTop)
							|| parseInt(this.y + this.height) < parseInt(this.preDragState.containedSymbolsBottom)) {

						//Reset the lane to pre-drag position
						this.x = this.preDragState.x;
						this.y = this.preDragState.y;
						this.width = this.preDragState.width;
						this.height = this.preDragState.height;
						this.adjustGeometry();
					} else {
						// If the width is decreased below min_width the
						// min-width should be set as width
						if (this.width < m_constants.LANE_MIN_WIDTH) {
							this.width = m_constants.LANE_MIN_WIDTH;
							// The displayed text needs to be trimmed
							var str = this.text.attr("text");
							if (str.length > 8) {
								this.text.attr("text", str.substring(0, 8)
										+ " ...");
							}
						} else {
							this.text.attr("text", this.name);
						}

						var moveX, moveY = 0;
						if (this.preDragState.x < this.x) {
							moveX = this.preDragState.x - this.x;
							this.x = this.preDragState.x;
						} else if (this.preDragState.x > this.x) {
							moveX = this.preDragState.x - this.x;
							this.x = this.preDragState.x;
						}
						if (this.preDragState.y < this.y) {
							moveY = this.preDragState.y - this.y;
							this.y = this.preDragState.y;
						} else if (this.preDragState.y > this.y) {
							moveY = this.preDragState.y - this.y;
							this.y = this.preDragState.y;
						}
						this.parentSymbol.recalculateBoundingBox();
						this.parentSymbol.adjustGeometry();

						var changes = {
							x : this.x,
							y : this.y,
							width : this.width,
							height : this.height,
							xOffset : moveX,
							yOffset : moveY
						};

						var command = m_command
									.createUpdateModelElementCommand(
											this.diagram.modelId, this.oid, changes);

						m_commandsController.submitCommand(command);
					}
				};

				SwimlaneSymbol.prototype.stretchStart = function() {
					var bindingRect = this.getChildSymbolsBindingRect();

					/* Capture the state of the symbol before it's dragged / stretched. */
					this.preDragState = {
							x : this.x,
							y : this.y,
							height : this.height,
							width : this.width,
							containedSymbolsLeft : bindingRect.left,
							containedSymbolsRight : bindingRect.right,
							containedSymbolsTop : bindingRect.top,
							containedSymbolsBottom : bindingRect.bottom
					};
				}

				SwimlaneSymbol.prototype.getChildSymbolsBindingRect = function () {
					var left = this.x + this.width;
					var right = this.x;
					var top = this.y + this.height;
					var bottom = this.y;

					for ( var n in this.containedSymbols) {
						left = Math.min(this.containedSymbols[n].x
								- m_constants.POOL_SWIMLANE_MARGIN, left);
						right = Math.max(this.containedSymbols[n].x
								+ this.containedSymbols[n].width
								+ m_constants.POOL_SWIMLANE_MARGIN, right);
						top = Math.min(this.containedSymbols[n].y
								- m_constants.POOL_SWIMLANE_MARGIN, top);
						bottom = Math.max(this.containedSymbols[n].y
								+ this.containedSymbols[n].height
								+ m_constants.POOL_SWIMLANE_MARGIN, bottom);
					}

					return {
						left : left,
						right : right,
						top : top,
						bottom : bottom
					};
				}

				/**
				 *
				 */
				SwimlaneSymbol.prototype.containsPosition = function(x, y) {
					// TODO Add recursion for nested swimlanes

					if (x > this.x && x < this.x + this.width && y > this.y
							&& y < this.y + this.height) {
						return true;
					}

					return false;
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.adjustToSymbolBoundaries = function(x,
						y) {
					var left = this.x + this.symbolXOffset;
					var right = this.x + this.width + this.symbolXOffset;
					var top = this.y;
					var bottom = this.y + this.height;
					var moveX=0;
					var moveY=0;
					var preAdjustmentPos = {
						x : this.x + this.symbolXOffset,
						y : this.y,
						width : this.width,
						height : this.height
					}

					for ( var n in this.containedSymbols) {
						left = Math.min(this.containedSymbols[n].x + this.symbolXOffset
								- m_constants.POOL_SWIMLANE_MARGIN, left);
						right = Math.max(this.containedSymbols[n].x + this.symbolXOffset
								+ this.containedSymbols[n].width
								+ m_constants.POOL_SWIMLANE_MARGIN, right);
						top = Math.min(this.containedSymbols[n].y
								- m_constants.POOL_SWIMLANE_MARGIN, top);
						bottom = Math.max(this.containedSymbols[n].y
								+ this.containedSymbols[n].height
								+ m_constants.POOL_SWIMLANE_MARGIN, bottom);
					}

					this.x = left;
					this.width = right - left;
					this.y = top;
					this.height = bottom - top;

					// If Symbol is moved beyond the starting Y margin and
					// Height has increased, calculate the height change to move
					// Symbol
					if (parseInt(preAdjustmentPos.y) > (parseInt(y))
							&& parseInt(this.height) > parseInt(preAdjustmentPos.height
									.valueOf())) {
						moveY = this.height - parseInt(preAdjustmentPos.height);

					}else if (this.y < preAdjustmentPos.y
							&& this.height > preAdjustmentPos.height) {
						this.y = preAdjustmentPos.y;
						moveY = this.heigth - parseInt(preAdjustmentPos.height);
					}

					// If Symbol is moved beyond the starting X margin and
					// Height has increased, calculate the width change to move
					// Symbol
					if (parseInt(preAdjustmentPos.x) > (parseInt(x))
							&& parseInt(this.width) > parseInt(preAdjustmentPos.width
									.valueOf())) {
						moveX = this.width - parseInt(preAdjustmentPos.width);

					}else if (this.x < preAdjustmentPos.x
								&& this.width > preAdjustmentPos.width) {
							this.x = preAdjustmentPos.x;
							moveX = this.width - parseInt(preAdjustmentPos.width);
						}

					if (moveX > 0 || moveY > 0) {
						for ( var n in this.containedSymbols) {
								this.containedSymbols[n].moveBy(moveX, moveY);
						}
					}

					if (preAdjustmentPos.width != this.width
							|| preAdjustmentPos.height != this.height) {
						var changes = {
							x : this.x,
							y : this.y,
							width : this.width,
							height : this.height,
							xOffset : moveX,
							yOffset : moveY
						};

						this.parentSymbol.adjustGeometry();
						var command = m_command
								.createUpdateModelElementCommand(
										this.diagram.modelId, this.oid, changes);
						command.sync = true;
						m_commandsController.submitCommand(command);
					}
					this.x -= this.symbolXOffset;
					this.parentSymbol.recalculateBoundingBox();
					this.parentSymbol.adjustGeometry();
				};

				/**
				 * TODO Adjust contained symbols
				 */
				SwimlaneSymbol.prototype.notifySymbolsOnParticipantChange = function() {
					for ( var n in this.containedSymbols) {
						this.containedSymbols[n].onParentSymbolChange();
					}
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.getAllDataSymbols = function(dataSymbols) {
					for ( var n in this.dataSymbols) {
						dataSymbols.push(this.dataSymbols[n]);
					}

					return dataSymbols;
				};
			}

			function SwimlaneSymbol_hoverInFlyOutMenuClosure() {
				this.auxiliaryProperties.callbackScope.showFlyOutMenu();
			}

			function SwimlaneSymbol_hoverOutFlyOutMenuClosure() {
				this.auxiliaryProperties.callbackScope.hideFlyOutMenu();
			}

			/**
			 *
			 */
			function SwimlaneSymbol_clickClosure() {
				this.auxiliaryProperties.callbackScope.click();
			}

			/**
			 *
			 */
			function SwimlaneSymbol_hoverInClosure() {
				this.auxiliaryProperties.callbackScope.hoverIn();
			}

			/**
			 *
			 */
			function SwimlaneSymbol_topRect_hoverInClosure(event){
				this.auxiliaryProperties.callbackScope.topRectangleHoverIn(event);
			}

			/**
			 *
			 */
			function SwimlaneSymbol_topRect_hoverOutClosure(event){
				this.auxiliaryProperties.callbackScope.proximityHoverOut(event);
			}

			/**
			 *
			 */
			function SwimlaneSymbol_hoverOutClosure() {
				this.auxiliaryProperties.callbackScope.hoverOut();
			}

			function SwimlaneSymbol_removeClosure(){
				var cbObj = this;
				if (parent.iPopupDialog) {
					parent.iPopupDialog.openPopup({
						attributes : {
							width : "400px",
							height : "200px",
							src : "../bpm-modeler/popups/confirmationPopupDialogContent.html"
						},
						payload : {
							title : "Confirm",
							message : "Are you sure you want to delete "
									 + cbObj.auxiliaryProperties.callbackScope.text.attr("text")+ " and all <BR> "
									 + "symbols in the lane?<BR><BR>",
							acceptButtonText : "Yes",
							cancelButtonText : "Cancel",
							acceptFunction : function() {
								var thisLane = cbObj.auxiliaryProperties.callbackScope;
								thisLane.createAndSubmitDeleteCommand();
							}
						}
					});
				} else {
					m_utils.debug("Error: unable to get iPopupDialog");
				}
			}

			/**
			 *
			 */
			function SwimlaneSymbol_minimizeClickClosure() {
				this.auxiliaryProperties.callbackScope.onMinimizeIconClick();
			}

			/**
			 *
			 */
			function SwimlaneSymbol_maximizeClickClosure() {
				this.auxiliaryProperties.callbackScope.onMaximizeIconClick();
			}

		});