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
				"bpm-modeler/js/m_canvasManager", "bpm-modeler/js/m_model", "bpm-modeler/js/m_symbol", "bpm-modeler/js/m_activitySymbol",
				"bpm-modeler/js/m_gatewaySymbol", "bpm-modeler/js/m_eventSymbol", "bpm-modeler/js/m_dataSymbol", "bpm-modeler/js/m_annotationSymbol",
				"bpm-modeler/js/m_propertiesPanel", "bpm-modeler/js/m_swimlanePropertiesPanel","bpm-modeler/js/m_modelerUtils","bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_canvasManager, m_model, m_symbol, m_activitySymbol,
				m_gatewaySymbol, m_eventSymbol, m_dataSymbol, m_annotationSymbol,
				m_propertiesPanel, m_swimlanePropertiesPanel, m_modelerUtils,m_i18nUtils) {

			return {
				createSwimlaneSymbol : function(diagram, parentSymbol) {
					var laneSymbol = new SwimlaneSymbol();
					var laneIndex = parentSymbol.getLaneIndex();
					var laneNamePrefix = m_i18nUtils.getProperty("modeler.diagram.newLane.namePrefix");
					laneSymbol.name = laneNamePrefix + " " + laneIndex;
					laneSymbol.bind(diagram, parentSymbol);

					return laneSymbol;
				},

				createSwimlaneSymbolFromParticipant : function(diagram,
						parentSymbol, participant) {
					var laneSymbol = new SwimlaneSymbol(diagram);

					laneSymbol.bind(diagram, parentSymbol);
					var laneIndex = parentSymbol.getLaneIndex();
					laneSymbol.name = "Lane " + laneIndex;
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
				this.text ="Default Lane";
				this.description = null;
				this.participantFullId = null;
				this.participantName = null;
				this.minimizeIcon = null;
				this.shrinkToFitIcon = null;
				this.cacheWidth = 0;
				this.cacheHeight = 0;
				this.maximizeIcon = null;
				this.minimized = false;
				this.symbolXOffset = 0;
				this.symbolYOffset = 0;

				/**
				 * Binds all client-side aspects to the object (graphics
				 * objects, diagram, base classes).
				 */
				SwimlaneSymbol.prototype.bind = function(diagram, parentSymbol) {
					this.type = m_constants.SWIMLANE_SYMBOL;

					this.diagram = diagram;
					this.orientation = parentSymbol.orientation;
					this.parentSymbol = parentSymbol;

					var laneHeight;
					if (this.parentSymbol
							&& this.parentSymbol.laneSymbols
							&& parseInt(this.parentSymbol.laneSymbols.length) > 0) {
						laneHeight = this.parentSymbol.laneSymbols[0].height;
					} else {
						laneHeight = this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? m_constants.LANE_DEFAULT_HEIGHT
								: m_constants.LANE_DEFAULT_WIDTH;
					}
					// TODO Hack to only apply it to new symbols

					if (this.width == 0) {
						this.width = this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? m_constants.LANE_DEFAULT_WIDTH
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
					this.shrinkToFitIcon = null;
					this.symbolXOffset = 0;
					this.symbolYOffset = 0;

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
				 * Overrides function in Drawable to to remove assigned participant if
				 * participant is deleted.
				 */
				SwimlaneSymbol.prototype.applySymbolSpecific = function(changedObject) {
					if (!changedObject.participantFullId) {
						this.participantFullId = null;
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
					transferObject.shrinkToFitIcon = null;

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
									this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? this.width
											: m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT,
											this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
											: this.height,
									{
										"fill" : m_constants.SWIMLANE_COLOR,
										"stroke" : m_constants.SWIMLANE_COLOR,
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


					this.minimizeIcon = m_canvasManager
							.drawImageAt(
									"../../images/icons/min.png",
									this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? (this.x
											+ this.width - 20)
											: (this.x + 0.5 * m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT),
											this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? (this.y + 1.2 * m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT)
											: (this.y + 0.5 * this.height), 16,
									16)
									.attr(
											{
												"transform" : this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? "R0"
														: "R270", "title" : m_i18nUtils.getProperty("modeler.diagram.minimizeLane")
											});

					this.addToPrimitives(this.minimizeIcon);

					this.maximizeIcon = m_canvasManager
					.drawImageAt(
							"../../images/icons/max.png",
							this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? (this.x
									+ this.width - 20)
									: (this.x + 0.5 * m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT),
									this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? (this.y + 1.2 * m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT)
									: (this.y + .35 * this.height), 16,
							16)
							.attr(
									{
										"transform" : this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? "R0"
												: "R270", "title" : m_i18nUtils.getProperty("modeler.diagram.maximizeLane")
									});

					this.maximizeIcon.hide();
					this.addToPrimitives(this.maximizeIcon);

					this.shrinkToFitIcon = m_canvasManager
					.drawImageAt(
							"../../images/icons/shrinktofit.png",
							this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? (this.x
									+ this.width - 20)
									: (this.x + 0.5 * m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT),
									this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? (this.y + 1.2 * m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT)
									: (this.y + 0.5 * this.height), 16,
							16)
							.attr(
									{
										"transform" : this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL ? "R0"
												: "R270", "title" : m_i18nUtils.getProperty("modeler.diagram.shrinkToFit")
									});
					this.addToPrimitives(this.shrinkToFitIcon);
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

					if (this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
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
							"transform" : "R0",
							"x" : this.x + this.width - 20,
							"y" : this.y + 0.15
									* m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
						});
						this.maximizeIcon.attr({
							"transform" : "R0",
							"x" : this.x + this.width - 20,
							"y" : this.y + 0.12
									* m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
						});
						this.shrinkToFitIcon.attr({
							"transform" : "R0",
							"x" : this.x + this.width - 40,
							"y" : this.y + 0.15
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
							"transform" : "R270",
							"x" : this.x + 0.5
									* m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT,
							"y" : this.y + 0.5 * this.height
						});
						this.minimizeIcon.attr({
							"transform" : "R270",
							"x" : this.x + 0.2
									* m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT,
							"y" : this.y + 0.5
									* m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
						});
						this.maximizeIcon.attr({
							"transform" : "R270",
							"x" : this.x + 0.2
									* m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT,
							"y" : this.y + 0.5
									* m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT
						});
						this.shrinkToFitIcon.attr({
							"transform" : "R270",
							"x" : this.x + 0.2
									* m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT,
							"y" : this.y + 0.5
									* m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT + 20
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

					var temp = this.x;
					this.x = this.y;
					this.y = temp;

					var changeDescriptionsLane = [];

					var changesLane = {
						x : this.x,
						y : this.y,
						width : this.width,
						height : this.height,
						orientation : this.orientation
					};
					changeDescriptionsLane.push({
						oid : this.oid,
						changes : changesLane
					});

					var symbolchangeDesc;
					for ( var n in this.containedSymbols) {
						symbolchangeDesc = this.containedSymbols[n]
								.flipFlowOrientation(flowOrientation);
						changeDescriptionsLane.push(symbolchangeDesc);
					}

					return changeDescriptionsLane;
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
					this.shrinkToFitIcon
							.click(SwimlaneSymbol_shrinkToFitClickClosure);
				};


				/**
				 *
				 */
				SwimlaneSymbol.prototype.onMinimizeIconClick = function() {
					if (this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
						this.cacheWidth = this.width;
						this.width = m_constants.LANE_MIN_WIDTH;
					} else {
						this.cacheHeight = this.height;
						this.height = m_constants.LANE_MIN_WIDTH;
					}
					this.minimizeIcon.hide();

					//hide all Shrink to fit symbols
					for ( var n in this.parentSymbol.laneSymbols) {
						this.parentSymbol.laneSymbols[n].shrinkToFitIcon.hide();
					}
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
					if (this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
						this.width = this.cacheWidth;
					} else {
						this.height = this.cacheHeight;
					}
					this.maximizeIcon.hide();
					this.minimizeIcon.show();
					//show all Shrink to fit symbols
					for ( var n in this.parentSymbol.laneSymbols) {
						this.parentSymbol.laneSymbols[n].shrinkToFitIcon.show();
					}

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
					if (this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL){
						// Check if the minimizing of the lane height is permissible given that
						// adjoining lanes may have content longer than this lane.
						var isStretchWithinLimitForOtherLanes = true;
						var updateChanges = false;
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

							updateChanges = true;
						}
					}
					else{
						// Check if the minimizing of the lane height is permissible given that
						// adjoining lanes may have content longer than this lane.
						var isStretchWithinLimitForOtherLanes = true;
						for (var i = 0; i < this.parentSymbol.laneSymbols.length; i++) {
							var childrenBindingRect = this.parentSymbol.laneSymbols[i].getChildSymbolsBindingRect();

							if (parseInt(this.x) > parseInt(childrenBindingRect.left)
									|| parseInt(this.x + this.width) < parseInt(childrenBindingRect.right)) {
								isStretchWithinLimitForOtherLanes = false;
							}
						}

						if (isStretchWithinLimitForOtherLanes == false
								|| parseInt(this.y) > parseInt(this.preDragState.containedSymbolsTop)
								|| parseInt(this.y + this.height) < parseInt(this.preDragState.containedSymbolsBottom)
								|| parseInt(this.x) > parseInt(this.preDragState.containedSymbolsLeft)
								|| parseInt(this.x + this.width) < parseInt(this.preDragState.containedSymbolsRight)) {

							//Reset the lane to pre-drag position
							this.x = this.preDragState.x;
							this.y = this.preDragState.y;
							this.width = this.preDragState.width;
							this.height = this.preDragState.height;
							this.adjustGeometry();
						} else {
							// If the width is decreased below min_width the
							// min-width should be set as width
							if (this.height < m_constants.LANE_MIN_WIDTH) {
								this.height = m_constants.LANE_MIN_WIDTH;
								// The displayed text needs to be trimmed

								var str = this.text.attr("text");
								if (str.length > 8) {
									this.text.attr("text", str.substring(0, 8)
											+ " ...");
								}
							} else {
								this.text.attr("text", this.name);
							}

							updateChanges = true;
						}
					}

					if (updateChanges == true) {
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
							yOffset : moveY,
							orientation : this.orientation
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

				/**
				 *
				 */
				SwimlaneSymbol.prototype.getChildSymbolsBindingRect = function () {
					var left = this.x + this.symbolXOffset + this.width;
					var right = this.x + this.symbolXOffset;
					var top = this.y + this.symbolYOffset + this.height;
					var bottom = this.y + this.symbolYOffset;

					for ( var n in this.containedSymbols) {
						left = Math.min(this.containedSymbols[n].x + this.symbolXOffset
								- m_constants.SWIMLANE_SYMBOL_MARGIN, left);
						right = Math.max(this.containedSymbols[n].x + this.symbolXOffset
								+ this.containedSymbols[n].width
								+ m_constants.SWIMLANE_SYMBOL_MARGIN, right);
						top = Math.min(this.containedSymbols[n].y + this.symbolYOffset
								- m_constants.SWIMLANE_SYMBOL_MARGIN, top);
						bottom = Math.max(this.containedSymbols[n].y + this.symbolYOffset
								+ this.containedSymbols[n].height
								+ m_constants.SWIMLANE_SYMBOL_MARGIN, bottom);
					}

					return {
						left : left,
						right : right,
						top : top,
						bottom : bottom
					};
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.containsPosition = function(x, y) {
					// TODO Add recursion for nested swimlanes

					if (x > this.x
							&& x < this.x + this.width
							&& y > (this.y + m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT)
							&& y < this.y + this.height) {
						return true;
					}

					return false;
				};


				SwimlaneSymbol.prototype.recalculateBoundingBox = function() {

					for ( var c in this.containedSymbols) {
						containedSymbol = this.containedSymbols[c];

						var sX2 = containedSymbol.x + containedSymbol.width
								+ m_constants.SWIMLANE_SYMBOL_MARGIN;

						var lX2 = this.x + this.width;
						if (sX2 > lX2) {
							this.width += (sX2 - lX2);
						}

						var sY2 = containedSymbol.y + containedSymbol.height
								+ m_constants.SWIMLANE_SYMBOL_MARGIN;

						var lY2 = this.y + this.height;
						if (sY2 > lY2) {
							this.height += (sY2 - lY2);
						}
					}
				};

				/**
				 *
				 */
				SwimlaneSymbol.prototype.adjustToSymbolBoundaries = function(x,
						y) {

					var left;
					var right;
					var top;
					var bottom;
					var moveX=0;
					var moveY=0;
					var preAdjustmentPos;

					if (this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
						left = this.x + this.symbolXOffset;
						right = this.x + this.width + this.symbolXOffset;
						top = this.y;
						bottom = this.y + this.height;
						moveX=0;
						moveY=0;
						preAdjustmentPos = {
							x : this.x + this.symbolXOffset,
							y : this.y,
							width : this.width,
							height : this.height
						}

						for ( var n in this.containedSymbols) {
							left = Math.min(this.containedSymbols[n].x + this.symbolXOffset
									- m_constants.SWIMLANE_SYMBOL_MARGIN, left);
							right = Math.max(this.containedSymbols[n].x + this.symbolXOffset
									+ this.containedSymbols[n].width
									+ m_constants.SWIMLANE_SYMBOL_MARGIN, right);
							top = Math.min(this.containedSymbols[n].y
									- m_constants.SWIMLANE_SYMBOL_MARGIN, top);
							bottom = Math.max(this.containedSymbols[n].y
									+ this.containedSymbols[n].height
									+ m_constants.SWIMLANE_SYMBOL_MARGIN, bottom);
						}
					}else{
						left = this.x;
						right = this.x + this.width;
						top = this.y + this.symbolYOffset;
						bottom = this.y + this.height + this.symbolYOffset;
						moveX=0;
						moveY=0;
						preAdjustmentPos = {
							x : this.x,
							y : this.y + this.symbolYOffset,
							width : this.width,
							height : this.height
						};

						for ( var n in this.containedSymbols) {
							top = Math.min(this.containedSymbols[n].y + this.symbolYOffset
									- m_constants.SWIMLANE_SYMBOL_MARGIN, top);
							bottom = Math.max(this.containedSymbols[n].y + this.symbolYOffset
									+ this.containedSymbols[n].height
									+ m_constants.SWIMLANE_SYMBOL_MARGIN, bottom);
							left = Math.min(this.containedSymbols[n].x
									- m_constants.SWIMLANE_SYMBOL_MARGIN, left);
							right = Math.max(this.containedSymbols[n].x
									+ this.containedSymbols[n].width
									+ m_constants.SWIMLANE_SYMBOL_MARGIN, right);
						}
					}
					this.x = left;
					this.width = right - left;
					this.y = top;
					this.height = bottom - top;

					// If Symbol is moved beyond the starting Y margin and
					// Height has increased, calculate the height change to move
					// Symbol
					if (this.height > preAdjustmentPos.height) {
						if ((parseInt(preAdjustmentPos.y) > parseInt(y))
								|| this.y < preAdjustmentPos.y) {
							moveY = this.height
									- parseInt(preAdjustmentPos.height);
						}
						if (this.height > preAdjustmentPos.height) {
							this.y = preAdjustmentPos.y;
						}
					}

					// If Symbol is moved beyond the starting X margin and
					// width has increased, calculate the width change to move
					// Symbol
					if (this.width > preAdjustmentPos.width) {
						if ((preAdjustmentPos.x > parseInt(x))
								|| (this.x < preAdjustmentPos.x)) {
							moveX = this.width
									- parseInt(preAdjustmentPos.width);
						}
						if (this.x < preAdjustmentPos.x) {
							this.x = preAdjustmentPos.x;
						}
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
					if (this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
						this.x -= this.symbolXOffset;
					} else {
						this.y -= this.symbolYOffset;
					}

					this.parentSymbol.recalculateBoundingBox();
					this.parentSymbol.adjustGeometry();
				};


				/**
				 *
				 * @author Yogesh.Manware
				 */
				SwimlaneSymbol.prototype.shrinkToFit = function() {
					if(this.containedSymbols.length < 1){
						return;
					}

					// check if any lane is already minimized
//					for ( var n in this.parentSymbol.laneSymbols) {
//						if (this.parentSymbol.laneSymbols[1].minimizeIcon.node.style.display == "none") {
//							return;
//						}
//					}

					var moveX=0;
					var moveY=0;
					var childSymbolsBindingRect, otherChildSymbolsBindingRect;
					//evaluate binding rectangle for current lane
					childSymbolsBindingRect = this
							.getChildSymbolsBindingRect();

					if (this.orientation === m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {

						//evaluate binding rectangle for other lanes
						for ( var n in this.parentSymbol.laneSymbols) {
							otherChildSymbolsBindingRect = this.parentSymbol.laneSymbols[n]
									.getChildSymbolsBindingRect();
							//determine the minimum top margin that can be reduced by
							if (childSymbolsBindingRect.top > otherChildSymbolsBindingRect.top) {
								childSymbolsBindingRect.top = otherChildSymbolsBindingRect.top;
							}
							//determine the minimum bottom margin that can be reduced by
							if (childSymbolsBindingRect.bottom < otherChildSymbolsBindingRect.bottom) {
								childSymbolsBindingRect.bottom = otherChildSymbolsBindingRect.bottom;
							}
						}
						// move symbols towards swimlane header
						moveY = (this.y + this.symbolYOffset + m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT)
								- (childSymbolsBindingRect.top - m_constants.SWIMLANE_SYMBOL_MARGIN);
						// move symbols towards swimlane's left border
						moveX = this.x + this.symbolXOffset
								- (childSymbolsBindingRect.left - m_constants.SWIMLANE_SYMBOL_MARGIN);

						if (moveX != 0 || moveY != 0) {
							// move current lane's symbols towards left border and towards header
							for ( var n in this.containedSymbols) {
								this.containedSymbols[n].moveBy(moveX, moveY);
							}
							// move current lane's symbols towards header
							for ( var n in this.parentSymbol.laneSymbols) {
								for ( var c in this.parentSymbol.laneSymbols[n].containedSymbols) {
									if (this.parentSymbol.laneSymbols[n].oid != this.oid) {
										this.parentSymbol.laneSymbols[n].containedSymbols[c]
												.moveBy(0, moveY);
									}
								}
							}
						}

						// force set height of all lanes
						for ( var n in this.parentSymbol.laneSymbols) {
							this.parentSymbol.laneSymbols[n].height = childSymbolsBindingRect.bottom
									- childSymbolsBindingRect.top;
						}

						this.parentSymbol.adjustGeometry();
						// Adjust current lane width
						this.width = childSymbolsBindingRect.right
								- childSymbolsBindingRect.left;
					} else {
						//evaluate binding rectangle for other lanes
						for ( var n in this.parentSymbol.laneSymbols) {
							otherChildSymbolsBindingRect = this.parentSymbol.laneSymbols[n]
									.getChildSymbolsBindingRect();
							//determine the minimum left margin that can be reduced by
							if (childSymbolsBindingRect.left > otherChildSymbolsBindingRect.left) {
								childSymbolsBindingRect.left = otherChildSymbolsBindingRect.left;
							}
							//determine the minimum right margin that can be reduced by
							if (childSymbolsBindingRect.right < otherChildSymbolsBindingRect.right) {
								childSymbolsBindingRect.right = otherChildSymbolsBindingRect.right;
							}
						}
						// move symbols towards swimlane header
						moveX = (this.x + this.symbolXOffset + m_constants.POOL_SWIMLANE_TOP_BOX_HEIGHT)
								- (childSymbolsBindingRect.left - m_constants.SWIMLANE_SYMBOL_MARGIN);
						// move symbols towards swimlane's top border
						moveY = this.y + this.symbolYOffset
								- (childSymbolsBindingRect.top - m_constants.SWIMLANE_SYMBOL_MARGIN);

						if (moveX != 0 || moveY != 0) {
							// move current lane's symbols towards left and towards header
							for ( var n in this.containedSymbols) {
								this.containedSymbols[n].moveBy(moveX, moveY);
							}
							// move current lane's symbols towards header
							for ( var n in this.parentSymbol.laneSymbols) {
								for ( var c in this.parentSymbol.laneSymbols[n].containedSymbols) {
									if (this.parentSymbol.laneSymbols[n].oid != this.oid) {
										this.parentSymbol.laneSymbols[n].containedSymbols[c]
												.moveBy(moveX, 0);
									}
								}
							}
						}

						// force set width of all lanes
						for ( var n in this.parentSymbol.laneSymbols) {
							this.parentSymbol.laneSymbols[n].width = childSymbolsBindingRect.right
									- childSymbolsBindingRect.left;
						}

						this.parentSymbol.adjustGeometry();
						// Adjust current lane height
						this.height = childSymbolsBindingRect.bottom
								- childSymbolsBindingRect.top;
					}

					var changes = {
						x : this.x,
						y : this.y,
						width : this.width,
						height : this.height,
						xOffset : moveX,
						yOffset : moveY
					};

					var command = m_command.createUpdateModelElementCommand(
							this.diagram.modelId, this.oid, changes);
					command.sync = true;
					m_commandsController.submitCommand(command);

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
							title : m_i18nUtils.getProperty("modeler.messages.confirm"),
							message : m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.confirmMsg.delete").replace("{0}", cbObj.auxiliaryProperties.callbackScope.text.attr("text")),
							acceptButtonText : m_i18nUtils.getProperty("modeler.messages.confirm.yes"),
							cancelButtonText : m_i18nUtils.getProperty("modeler.messages.confirm.cancel"),
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
			/**
			 *
			 */
			function SwimlaneSymbol_shrinkToFitClickClosure() {
				this.auxiliaryProperties.callbackScope.shrinkToFit();
			}

		});