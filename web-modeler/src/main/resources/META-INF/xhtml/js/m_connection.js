/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_canvasManager", "m_drawable",
				"m_commandsController", "m_command", "m_controlFlow",
				"m_propertiesPanel", "m_dataFlowPropertiesPanel",
				"m_controlFlowPropertiesPanel", "m_activitySymbol",
				"m_gatewaySymbol", "m_eventSymbol", "m_controlFlow",
				"m_dataFlow" ],
		function(m_utils, m_constants, m_canvasManager, m_drawable,
				m_commandsController, m_command, m_controlFlow,
				m_propertiesPanel, m_dataFlowPropertiesPanel,
				m_controlFlowPropertiesPanel, m_activitySymbol,
				m_gatewaySymbol, m_eventSymbol, m_controlFlow, m_dataFlow) {

			return {
				createConnection : function(diagram, fromAnchorPoint) {
					var connection = new Connection();

					connection.bind(diagram);

					connection.setFirstAnchorPoint(fromAnchorPoint);

					return connection;
				},

				createConnectionFromJson : function(diagram, json) {
					// TODO Ugly
					m_utils.inheritFields(json, m_drawable.createDrawable());
					m_utils.inheritMethods(json, new Connection());

					json.bind(diagram);
					json.initializeFromJson();
					
					return json;
				}
			};

			/**
			 * 
			 */
			function Connection() {
				var drawable = m_drawable.createDrawable();

				m_utils.inheritFields(this, drawable);
				m_utils.inheritMethods(Connection.prototype, drawable);

				this.description = null;
				this.fromAnchorPointOrientation = 1;
				this.fromModelElementOid = null;
				this.fromModelElementType = null;
				this.toAnchorPointOrientation = 3;
				this.toModelElementOid = null;
				this.toModelElementType = null;
				this.conditionExpressionTextXOffset = m_constants.CONNECTION_EXPRESSION_OFFSET;
				this.conditionExpressionTextYOffset = m_constants.CONNECTION_EXPRESSION_OFFSET;

				/**
				 * Binds all client-side aspects to the object (graphics
				 * objects, diagram, base classes).
				 */
				Connection.prototype.bind = function(diagram) {
					this.state = m_constants.SYMBOL_CREATED_STATE;
					this.diagram = diagram;
					this.selected = false;
					this.fromAnchorPoint = null;
					this.toAnchorPoint = null;
					this.conditionExpressionText = null;
					this.path = null;
					this.visible = true;
					this.auxiliaryPickPath = null;
					this.segments = new Array();
					this.clickedSegmentIndex = -1;
				};

				/**
				 * 
				 */
				Connection.prototype.toString = function() {
					return "Lightdust.Connection";
				};

				/**
				 * TODO Check whether this method can be implemented with more
				 * reuse.
				 */
				Connection.prototype.initializeFromJson = function() {
					// Adjust anchor orientation

					var orientation = this.determineOrientation();
					if (this.fromAnchorPointOrientation == m_constants.UNDEFINED_ORIENTATION) {
						if (this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
							if (orientation.indexOf("S") == 0) {
								this.fromAnchorPointOrientation = 2;								
							} else if (orientation.indexOf("W") == 0) {
								this.fromAnchorPointOrientation = 3;
							} else if (orientation.indexOf("E") == 0) {
								this.fromAnchorPointOrientation = 1;
							} else {
								this.fromAnchorPointOrientation = 0;
							}
						} else {
							this.fromAnchorPointOrientation = 1;
						}
					}

					if (this.toAnchorPointOrientation == m_constants.UNDEFINED_ORIENTATION) {
						if (this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
							if (orientation.indexOf("S") == 1) {
								this.toAnchorPointOrientation = 2;								
							} else if (orientation.indexOf("W") == 1) {
								this.toAnchorPointOrientation = 3;
							} else if (orientation.indexOf("E") == 1) {
								this.toAnchorPointOrientation = 1;
							} else {
								this.toAnchorPointOrientation = 0;
							}							
						} else {
							this.toAnchorPointOrientation = 3;
						}
					}

					if (this.fromModelElementType == m_constants.ACTIVITY) {
						this
								.setFirstAnchorPoint(this.diagram.activitySymbols[this.fromModelElementOid
										.toString()].anchorPoints[this.fromAnchorPointOrientation]);
					} else if (this.fromModelElementType == m_constants.EVENT) {
						this
								.setFirstAnchorPoint(this.diagram.eventSymbols[this.fromModelElementOid].anchorPoints[this.fromAnchorPointOrientation]);
					} else if (this.fromModelElementType == m_constants.DATA) {
						this
								.setFirstAnchorPoint(this.diagram.dataSymbols[this.fromModelElementOid].anchorPoints[this.fromAnchorPointOrientation]);
					} else if (this.fromModelElementType == m_constants.GATEWAY) {
						this
								.setFirstAnchorPoint(this.diagram.gatewaySymbols[this.fromModelElementOid].anchorPoints[this.fromAnchorPointOrientation]);
					}

					this.prepare();

					if (this.toModelElementType == m_constants.ACTIVITY) {
						this.toAnchorPoint = this.diagram.activitySymbols[this.toModelElementOid].anchorPoints[this.toAnchorPointOrientation];
					} else if (this.toModelElementType == m_constants.EVENT) {
						this.toAnchorPoint = this.diagram.eventSymbols[this.toModelElementOid].anchorPoints[this.toAnchorPointOrientation];
					} else if (this.toModelElementType == m_constants.DATA) {
						this.toAnchorPoint = this.diagram.dataSymbols[this.toModelElementOid].anchorPoints[this.toAnchorPointOrientation];
					} else if (this.toModelElementType == m_constants.GATEWAY) {						
						this.toAnchorPoint = this.diagram.gatewaySymbols[this.toModelElementOid].anchorPoints[this.toAnchorPointOrientation];
					}

					this.toAnchorPoint.symbol.connections.push(this);

					if (this.isDataFlow()) {
						m_dataFlow.initializeFromJson(this.diagram.process);
						this.propertiesPanel = m_dataFlowPropertiesPanel
								.getInstance();
					} else {
						m_utils.inheritMethods(this.modelElement.prototype,
								m_controlFlow.prototype);
						this.propertiesPanel = m_controlFlowPropertiesPanel
								.getInstance();

						if (this.modelElement.attributes != null) {
							this.conditionExpressionTextXOffset = this.modelElement.attributes["carnot:engine:conditionExpressionTextXOffset"];
							this.conditionExpressionTextYOffset = this.modelElement.attributes["carnot:engine:conditionExpressionTextYOffset"];

							if (this.conditionExpressionTextXOffset == null) {
								this.conditionExpressionTextXOffset = m_constants.CONNECTION_EXPRESSION_OFFSET;
							}

							if (this.conditionExpressionTextYOffset == null) {
								this.conditionExpressionTextYOffset = m_constants.CONNECTION_EXPRESSION_OFFSET;
							}
						}
					}

					this.completeNoTransfer();
					this.reroute();					
				};

				/* Determinies orientation of anchorpoints for connections with undefined orientation.
				 * Orientation string returned follows syntax - FromAnchopointOrientation(N/E/W/S)
				 * followed by ToAnchopointOrientation(N/E/W/S)
				 * 
				 * e.g. Orientation NE means From anchorpoint orientation is North and to-anchorpoint orientation is East
				 *  */
				Connection.prototype.determineOrientation = function() {
					var frmSmbl;
					var toSmbl;

					if (this.fromModelElementType == m_constants.ACTIVITY) {
						frmSmbl = this.diagram.activitySymbols[this.fromModelElementOid.toString()];
					} else if (this.fromModelElementType == m_constants.EVENT) {
						frmSmbl = this.diagram.eventSymbols[this.fromModelElementOid];
					} else if (this.fromModelElementType == m_constants.DATA) {
						frmSmbl = this.diagram.dataSymbols[this.fromModelElementOid];
					} else if (this.fromModelElementType == m_constants.GATEWAY) {
						frmSmbl = this.diagram.gatewaySymbols[this.fromModelElementOid];
					}

					if (this.toModelElementType == m_constants.ACTIVITY) {
						toSmbl = this.diagram.activitySymbols[this.toModelElementOid.toString()];
					} else if (this.toModelElementType == m_constants.EVENT) {
						toSmbl = this.diagram.eventSymbols[this.toModelElementOid];
					} else if (this.toModelElementType == m_constants.DATA) {
						toSmbl = this.diagram.dataSymbols[this.toModelElementOid];
					} else if (this.toModelElementType == m_constants.GATEWAY) {
						toSmbl = this.diagram.gatewaySymbols[this.toModelElementOid];
					}

					var orientation;
					if (Math.abs(frmSmbl.anchorPoints[0].x - toSmbl.anchorPoints[0].x) < parseInt(toSmbl.width / 2)) {
						if (frmSmbl.anchorPoints[1].y < toSmbl.anchorPoints[1].y) {
							orientation = "SN";
						} else {
							orientation = "NS";
						}						
					} else if (Math.abs(frmSmbl.anchorPoints[1].y - toSmbl.anchorPoints[1].y) < parseInt(toSmbl.height / 2)) {
						if (frmSmbl.anchorPoints[1].x < toSmbl.anchorPoints[1].x) {
							orientation = "EW";
						} else {
							orientation = "WE";
						}						
					} else if (frmSmbl.anchorPoints[0].x < toSmbl.anchorPoints[0].x
							&& frmSmbl.anchorPoints[1].y < toSmbl.anchorPoints[1].y) {
						if (toSmbl.anchorPoints[0].x > parseInt(frmSmbl.anchorPoints[1].x + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH)) {
							orientation = "EN";
						} else if (toSmbl.anchorPoints[3].y > parseInt(frmSmbl.anchorPoints[2].y + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH)) {
							orientation = "SW";
						} else {
							orientation = "SN";
						}
					} else if (frmSmbl.anchorPoints[0].x > toSmbl.anchorPoints[0].x
							&& frmSmbl.anchorPoints[1].y > toSmbl.anchorPoints[1].y) {
						if (frmSmbl.anchorPoints[0].x > parseInt(toSmbl.anchorPoints[1].x + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH)) {
							orientation = "NE";
						} else if (frmSmbl.anchorPoints[3].y > parseInt(toSmbl.anchorPoints[2].y + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH)) {
							orientation = "WS";
						} else {
							orientation = "NS";
						}
					} else if (frmSmbl.anchorPoints[0].x < toSmbl.anchorPoints[0].x
							&& frmSmbl.anchorPoints[1].y > toSmbl.anchorPoints[1].y) {
						if (toSmbl.anchorPoints[3].x > parseInt(frmSmbl.anchorPoints[0].x + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH)) {
							orientation = "NW";
						} else if (frmSmbl.anchorPoints[1].y > parseInt(toSmbl.anchorPoints[2].y + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH)) {
							orientation = "ES";
						} else {
							orientation = "NS";
						}
					} else if (frmSmbl.anchorPoints[0].x > toSmbl.anchorPoints[0].x
							&& frmSmbl.anchorPoints[1].y < toSmbl.anchorPoints[1].y) {
						if (frmSmbl.anchorPoints[3].x > parseInt(toSmbl.anchorPoints[0].x + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH)) {
							orientation = "WN";
						} else if (toSmbl.anchorPoints[1].y > parseInt(frmSmbl.anchorPoints[3].y + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH)) {
							orientation = "SE";
						} else {
							orientation = "NS";
						}						
					}
					
					return orientation;
				}

				/**
				 * 
				 */
				Connection.prototype.setFirstAnchorPoint = function(anchorPoint) {
					this.fromAnchorPoint = anchorPoint;

					this.fromAnchorPoint.symbol.connections.push(this);
				};

				/**
				 * 
				 */
				Connection.prototype.setSecondAnchorPointNoComplete = function(
						anchorPoint) {
					this.toAnchorPoint = anchorPoint;

					if (this.path == null) {
						this.prepare();
					}

					if (this.toAnchorPoint.symbol != null) {
						this.toAnchorPoint.symbol.connections.push(this);

						if (this.isDataFlow()) {
							var data;
							var activity;

							// TODO need better ways to identify data symbol
							if (this.fromAnchorPoint.symbol.dataFullId != null) {
								this.fromModelElementOid = this.fromAnchorPoint.symbol.oid;
								this.fromModelElementType = m_constants.DATA;
								this.toModelElementOid = this.toAnchorPoint.symbol.oid;
								this.toModelElementType = this.toAnchorPoint.symbol.modelElement.type;
								data = this.fromAnchorPoint.symbol.modelElement;
								activity = this.toAnchorPoint.symbol.modelElement;
							} else {
								this.fromModelElementOid = this.fromAnchorPoint.symbol.oid;
								this.fromModelElementType = this.fromAnchorPoint.symbol.modelElement.type;
								this.toModelElementOid = this.toAnchorPoint.symbol.oid;
								this.toModelElementType = m_constants.DATA;
								data = this.toAnchorPoint.symbol.modelElement;
								activity = this.fromAnchorPoint.symbol.modelElement;
							}

							this.modelElement = m_dataFlow.createDataFlow(
									this.diagram.process, data, activity);
							
							this.propertiesPanel = m_dataFlowPropertiesPanel
									.getInstance();
						} else {
							this.fromModelElementOid = this.fromAnchorPoint.symbol.oid;
							this.fromModelElementType = this.fromAnchorPoint.symbol.modelElement.type;
							this.toModelElementOid = this.toAnchorPoint.symbol.oid;
							this.toModelElementType = this.toAnchorPoint.symbol.modelElement.type;

							this.modelElement = m_controlFlow
									.createControlFlow(this.diagram.process);
							this.propertiesPanel = m_controlFlowPropertiesPanel
									.getInstance();
						}

						this.refreshFromModelElement();
					}

					this.reroute();
				};

				/**
				 * 
				 */
				Connection.prototype.setSecondAnchorPoint = function(
						anchorPoint) {
					this.setSecondAnchorPointNoComplete(anchorPoint);

					if (this.toAnchorPoint.symbol != null) {
						this.complete();
					}
				};

				/**
				 * 
				 */
				Connection.prototype.createTransferObject = function() {
					var transferObject = {};

					m_utils.inheritFields(transferObject, this);

					transferObject.diagram = null;
					transferObject.path = null;
					transferObject.flyOutMenuBackground = null;
					transferObject.bottomFlyOutMenuItems = null;
					transferObject.rightFlyOutMenuItems = null;
					transferObject.primitives = null;
					transferObject.editableTextPrimitives = null;
					transferObject.proximitySensor = null;
					transferObject.propertiesPanel = null;
					transferObject.toAnchorPointOrientation = this.toAnchorPoint.orientation;
					transferObject.fromAnchorPointOrientation = this.fromAnchorPoint.orientation;
					transferObject.toAnchorPoint = null;
					transferObject.fromAnchorPoint = null;
					transferObject.toAnchorPoint = null;
					transferObject.defaultIndicatorPath = null;
					transferObject.conditionExpressionText = null;
					transferObject.auxiliaryPickPath = null;

					if (this.isControlFlow()) {
						// TODO Can we store in graphical element?
						transferObject.modelElement.attributes["carnot:engine:conditionExpressionTextXOffset"] = this.conditionExpressionTextXOffset;
						transferObject.modelElement.attributes["carnot:engine:conditionExpressionTextYOffset"] = this.conditionExpressionTextYOffset;
						// TODO Add later
						transferObject.segments = null;
					} else {
						transferObject.modelElement = transferObject.modelElement.createTransferObject();
					}

					return transferObject;
				};

				/**
				 * 
				 */
				Connection.prototype.getPath = function(withId) {
					var path = "/models/" + this.diagram.model.id
							+ "/processes/" + this.diagram.process.id
							+ "/connections";

					if (withId) {
						path += "/" + this.oid;
					}
					
					return path;
				};

				/**
				 * 
				 */
				Connection.prototype.refresh = function() {
					this.adjustGeometry();

					// TODO Why here? Replace by command
//					m_commandsController.submitImmediately(this
//							.createUpdateCommand());
				};

				/**
				 * 
				 */
				Connection.prototype.isUnknownFlow = function() {
					return this.getToSymbol() == null;
				};

				/**
				 * 
				 */
				Connection.prototype.isDataFlow = function() {
					// TODO Need better type indication
					return this.getFromSymbol().dataFullId != null
							|| (this.getToSymbol() != null && this
									.getToSymbol().dataFullId != null);
				};

				/**
				 * 
				 */
				Connection.prototype.allowsCondition = function() {
					return this.fromModelElementType == m_constants.GATEWAY;
				};

				/**
				 * 
				 */
				Connection.prototype.isControlFlow = function() {
					return !this.isDataFlow();
				};

				/**
				 * 
				 */
				Connection.prototype.getFromSymbol = function() {
					return this.fromAnchorPoint.symbol;
				};

				/**
				 * 
				 */
				Connection.prototype.getToSymbol = function() {
					return this.toAnchorPoint == null ? null
							: this.toAnchorPoint.symbol;
				};

				/**
				 * 
				 */
				Connection.prototype.setDummySecondAnchorPoint = function() {
					this.setSecondAnchorPoint(this.fromAnchorPoint
							.createFlippedClone());
				};

				/**
				 * 
				 */
				Connection.prototype.prepare = function() {
					this.createPathPrimitives();
					this.initializePrepareEventHandling();

					this.state = m_constants.SYMBOL_PREPARED_STATE;
				};

				/**
				 * 
				 */
				Connection.prototype.complete = function() {
					this.completeNoTransfer();
					/*this.submitCreation();*/
					var command = m_command.createCreateNodeCommand("connection.create",
							this.diagram.model.id, this.diagram.process.oid,
							this.createTransferObject());
					m_commandsController.submitCommand(command);
				};

				// TODO Move to drawable

				/**
				 * 
				 */
				Connection.prototype.isPrepared = function() {
					return this.state == m_constants.SYMBOL_PREPARED_STATE;
				};

				/**
				 * 
				 */
				Connection.prototype.isCompleted = function() {
					return this.state == m_constants.SYMBOL_COMPLETED_STATE;
				};

				/**
				 * 
				 */
				Connection.prototype.completeNoTransfer = function() {
					this.register();

					this.auxiliaryPickPath = m_canvasManager.drawPath("", {
						"stroke" : "white",
						"stroke-width" : 4,
						"opacity" : 0
					});

					this.addToPrimitives(this.auxiliaryPickPath);

					// Initialize return pointer for closure

					this.auxiliaryPickPath.auxiliaryProperties = {
						callbackScope : this
					};

					this.createProximitySensor();
					this.createFlyOutMenuBackground();
					this.createFlyOutMenu();
					this.hideFlyOutMenu();
					this.initializeEventHandling();

					this.state = m_constants.SYMBOL_COMPLETED_STATE;

					// TODO Should this be called after?

					this.refreshFromModelElement();
				};

				/**
				 * 
				 */
				Connection.prototype.register = function() {
					this.diagram.connections.push(this);

					if (this.isControlFlow()) {
						this.diagram.process.controlFlows[this.modelElement.id] = this.modelElement;
					} else {
						this.diagram.process.dataFlows[this.modelElement.id] = this.modelElement;
					}
				};

				/**
				 * 
				 */
				Connection.prototype.createPathPrimitives = function() {
					this.path = m_canvasManager.drawPath("", {
						"arrow-end" : "block-wide-long",
						"arrow-start" : "none",
						"stroke" : m_constants.UNKNOWN_FLOW_COLOR,
						"stroke-width" : m_constants.CONNECTION_STROKE_WIDTH,
						"stroke-dasharray" : "-",
						"r" : 3
					});

					this.addToPrimitives(this.path);

					this.path.auxiliaryProperties = {
						callbackScope : this
					};

					this.conditionExpressionText = m_canvasManager
							.drawTextNode(
									this.fromAnchorPoint.x
											+ this.conditionExpressionTextXOffset,
									this.fromAnchorPoint.y
											+ this.conditionExpressionTextYOffset,
									"").attr({
								"text-anchor" : "start",
								"fill" : m_constants.CONTROL_FLOW_COLOR,
								"font-size" : m_constants.DEFAULT_FONT_SIZE
							});

					this.addToPrimitives(this.conditionExpressionText);
					this
							.addToEditableTextPrimitives(this.conditionExpressionText);
					this.conditionExpressionText.hide();

					this.defaultIndicatorPath = m_canvasManager.drawPath("", {
						"stroke" : m_constants.CONTROL_FLOW_COLOR,
						"stroke-width" : m_constants.CONNECTION_STROKE_WIDTH
					});

					this.addToPrimitives(this.defaultIndicatorPath);
					this.defaultIndicatorPath.hide();
				};

				/**
				 * 
				 */
				Connection.prototype.initializePrepareEventHandling = function() {
					this.path.click(Connection_clickClosure);
				};

				/**
				 * 
				 */
				Connection.prototype.initializeEventHandling = function() {
					this.auxiliaryPickPath.click(Connection_clickClosure);
					this.auxiliaryPickPath.hover(Connection_hoverInClosure,
							Connection_hoverOutClosure);
					this.auxiliaryPickPath.drag(Connection_dragMoveClosure,
							Connection_dragStartClosure,
							Connection_dragStopClosure);
					this.path.drag(Connection_dragMoveClosure,
							Connection_dragStartClosure,
							Connection_dragStopClosure);
					this.path.hover(Connection_hoverInClosure,
							Connection_hoverOutClosure);
					this.conditionExpressionText.hover(
							Connection_hoverInConditionExpressionTextClosure,
							Connection_hoverOutConditionExpressionTextClosure);
					this.conditionExpressionText.drag(
							Connection_dragConditionExpressionTextClosure,
							Connection_dragStartConditionExpressionTextClosure,
							Connection_dragStopConditionExpressionTextClosure);
				};

				Connection.prototype.createProximitySensorPrimitive = function() {
					return m_canvasManager.drawPath("", {
						"stroke" : "white",
						"stroke-width" : m_constants.PROXIMITY_SENSOR_MARGIN,
						"opacity" : 0
					});
				};

				/**
				 * 
				 */
				Connection.prototype.refreshFromModelElement = function() {
					this.conditionExpressionText.hide();
					this.defaultIndicatorPath.hide();

					if (!this.isCompleted()) {
						return;
					}

					if (this.isControlFlow()) {
						this.path.attr({
							"arrow-start" : "none",
							"stroke" : m_constants.CONTROL_FLOW_COLOR,
							"stroke-dasharray" : ""
						});

						if (this.modelElement.otherwise) {
							this.defaultIndicatorPath.show();
						} else {
							if (this.modelElement.conditionExpression != "true") {
								this.conditionExpressionText.attr("text",
										this.modelElement.conditionExpression);
								this.conditionExpressionText.show();
							}
						}
					} else if (this.isDataFlow()) {
						this.path.attr({
							"stroke" : m_constants.DATA_FLOW_COLOR,
							"stroke-dasharray" : ""
						});

						if (this.modelElement.inDataMapping) {
							this.path.attr("arrow-start", "block-wide-long");
						} else {
							this.path.attr("arrow-start", "none");
						}

						if (this.modelElement.outDataMapping) {
							this.path.attr("arrow-end", "block-wide-long");
						} else {
							this.path.attr("arrow-end", "none");
						}
					}
				};

				/**
				 * 
				 */
				Connection.prototype.reroute = function() {
					if (this.isControlFlow()) {
						this.segments = new Array();

						// if (this.isPrepared()) {
						// this.toAnchorPoint.x += 1;
						// this.toAnchorPoint.y += 1;
						// }

						var targetX = this.toAnchorPoint.x;
						var targetY = this.toAnchorPoint.y;

						// Adjust target

						if (this.toAnchorPoint.orientation == 0) {
							targetY = targetY
									- m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH;
						} else if (this.toAnchorPoint.orientation == 1) {
							targetX = targetX
									+ m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH;
						} else if (this.toAnchorPoint.orientation == 2) {
							targetY = targetY
									+ m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH;
						} else if (this.toAnchorPoint.orientation == 3) {
							targetX = targetX
									- m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH;
						}

						// Add first segment

						var currentSegment = null;

						if (this.fromAnchorPoint.orientation == 0) {
							this.segments
									.push(currentSegment = new Segment(
											this.fromAnchorPoint.x,
											this.fromAnchorPoint.y,
											this.fromAnchorPoint.x,
											this.fromAnchorPoint.y
													- m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH,
											currentSegment));
						} else if (this.fromAnchorPoint.orientation == 1) {
							this.segments
									.push(currentSegment = new Segment(
											this.fromAnchorPoint.x,
											this.fromAnchorPoint.y,
											this.fromAnchorPoint.x
													+ m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH,
											this.fromAnchorPoint.y,
											currentSegment));
						} else if (this.fromAnchorPoint.orientation == 2) {
							this.segments
									.push(currentSegment = new Segment(
											this.fromAnchorPoint.x,
											this.fromAnchorPoint.y,
											this.fromAnchorPoint.x,
											this.fromAnchorPoint.y
													+ m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH,
											currentSegment));
						} else if (this.fromAnchorPoint.orientation == 3) {
							this.segments
									.push(currentSegment = new Segment(
											this.fromAnchorPoint.x,
											this.fromAnchorPoint.y,
											this.fromAnchorPoint.x
													- m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH,
											this.fromAnchorPoint.y,
											currentSegment));
						}

						currentSegment = this.findPath(currentSegment, targetX,
								targetY);

						// Add last segment

						var lastSegment = new Segment(currentSegment.toX,
								currentSegment.toY, this.toAnchorPoint.x,
								this.toAnchorPoint.y, currentSegment);

						if (currentSegment.hasSameOrientation(lastSegment)) {
							currentSegment.toX = this.toAnchorPoint.x;
							currentSegment.toY = this.toAnchorPoint.y;
							currentSegment.nextSegment = null
						} else {
							this.segments.push(lastSegment);
						}

						if (this.toAnchorPoint.orientation == m_constants.NORTH) {
							this.defaultIndicatorPath
									.attr({
										'path' : "M"
												+ (this.toAnchorPoint.x - m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
												+ " "
												+ (this.toAnchorPoint.y
														- m_constants.CONNECTION_DEFAULT_PATH_OFFSET - m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
												+ "L"
												+ (this.toAnchorPoint.x + m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
												+ " "
												+ (this.toAnchorPoint.y
														- m_constants.CONNECTION_DEFAULT_PATH_OFFSET + m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
									});
						} else if (this.toAnchorPoint.orientation == m_constants.EAST) {
							this.defaultIndicatorPath
									.attr({
										'path' : "M"
												+ (this.toAnchorPoint.x
														+ m_constants.CONNECTION_DEFAULT_PATH_OFFSET - m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
												+ " "
												+ (this.toAnchorPoint.y - m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
												+ "L"
												+ (this.toAnchorPoint.x
														+ m_constants.CONNECTION_DEFAULT_PATH_OFFSET + m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
												+ " "
												+ (this.toAnchorPoint.y
														- m_constants.CONNECTION_DEFAULT_PATH_OFFSET + m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
									});
						} else if (this.toAnchorPoint.orientation == m_constants.SOUTH) {
							this.defaultIndicatorPath
									.attr({
										'path' : "M"
												+ (this.toAnchorPoint.x - m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
												+ " "
												+ (this.toAnchorPoint.y
														+ m_constants.CONNECTION_DEFAULT_PATH_OFFSET - m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
												+ "L"
												+ (this.toAnchorPoint.x + m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
												+ " "
												+ (this.toAnchorPoint.y
														+ m_constants.CONNECTION_DEFAULT_PATH_OFFSET + m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
									});
						} else if (this.toAnchorPoint.orientation == m_constants.WEST) {
							this.defaultIndicatorPath
									.attr({
										'path' : "M"
												+ (this.toAnchorPoint.x
														- m_constants.CONNECTION_DEFAULT_PATH_OFFSET - m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
												+ " "
												+ (this.toAnchorPoint.y - m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
												+ "L"
												+ (this.toAnchorPoint.x
														- m_constants.CONNECTION_DEFAULT_PATH_OFFSET + m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
												+ " "
												+ (this.toAnchorPoint.y
														- m_constants.CONNECTION_DEFAULT_PATH_OFFSET + m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
									});

						}

						this.conditionExpressionText.attr({
							x : this.fromAnchorPoint.x
									+ this.conditionExpressionTextXOffset,
							y : this.fromAnchorPoint.y
									+ this.conditionExpressionTextYOffset
						});
					}

					if (this.isCompleted()) {
						this.auxiliaryPickPath.attr({
							'path' : this.getSvgString()
						});
						this.proximitySensor.attr({
							'path' : this.getSvgString()
						});
					}

					this.path.attr({
						'path' : this.getSvgString()
					});
				};

				/**
				 * 
				 */
				Connection.prototype.findPath = function(startSegment, targetX,
						targetY) {
					var currentSegment = startSegment;
					var n = 0;

					// n used as a insurance flag to terminate routing

					while ((currentSegment.toX != targetX || currentSegment.toY != targetY)
							&& n < 6) {
						if (currentSegment.isHorizontal()) {
							if ((currentSegment.fromX < currentSegment.toX && currentSegment.toX < targetX)
									|| (currentSegment.fromX > currentSegment.toX && currentSegment.toX > targetX)) {
								currentSegment.toX = targetX;
							} else {
								this.segments
										.push(currentSegment = new Segment(
												currentSegment.toX,
												currentSegment.toY,
												currentSegment.toX, targetY,
												currentSegment));
							}
						} else {
							if ((currentSegment.fromY < currentSegment.toY && currentSegment.toY < targetY)
									|| (currentSegment.fromY > currentSegment.toY && currentSegment.toY > targetY)) {
								currentSegment.toY = targetY;
							} else {
								this.segments
										.push(currentSegment = new Segment(
												currentSegment.toX,
												currentSegment.toY, targetX,
												currentSegment.toY,
												currentSegment));
							}
						}

						++n;
					}

					return currentSegment;
				};

				/**
				 * 
				 */
				Connection.prototype.getSvgString = function() {
					var svgString = "M " + this.fromAnchorPoint.x + " "
							+ this.fromAnchorPoint.y;

					if (this.isControlFlow()) {
						for ( var n in this.segments) {
							svgString += this.segments[n].getSvgString();
						}
					} else {
						svgString += "L " + this.toAnchorPoint.x + " "
								+ this.toAnchorPoint.y;
					}

					return svgString;
				};

				/**
				 * 
				 */
				Connection.prototype.select = function() {
					this.selected = true;

					this.path
							.attr({
								"stroke-width" : m_constants.CONNECTION_SELECT_STROKE_WIDTH
							});
					this.diagram.addToCurrentSelection(this);
					this.fromAnchorPoint.show();
					this.toAnchorPoint.show();
					m_propertiesPanel.initializePropertiesPanel(this);
				};

				/**
				 * 
				 */
				Connection.prototype.deselect = function() {
					this.selected = false;

					this.path.attr({
						"stroke-width" : m_constants.CONNECTION_STROKE_WIDTH
					});
					this.fromAnchorPoint.hide();
					this.toAnchorPoint.hide();
				};

				/**
				 * 
				 */
				Connection.prototype.dragMove = function(dX, dY, x, y, event) {
					if (this.clickedSegmentIndex > 0
							&& this.clickedSegmentIndex < this.segments.length - 1) {

						if (this.segments[this.clickedSegmentIndex]) {
							this.segments[this.clickedSegmentIndex - 1].toX = x;
							this.segments[this.clickedSegmentIndex].fromX = x;
							this.segments[this.clickedSegmentIndex].toX = x;
							this.segments[this.clickedSegmentIndex + 1].fromX = x;
						} else {
							this.segments[this.clickedSegmentIndex - 1].toY = y;
							this.segments[this.clickedSegmentIndex].fromY = y;
							this.segments[this.clickedSegmentIndex].toY = y;
							this.segments[this.clickedSegmentIndex + 1].fromY = y;
						}

						this.path.attr({
							'path' : this.getSvgString()
						});
					}
				};

				/**
				 * May require "segment eater" up front.
				 */
				Connection.prototype.findClickedSegment = function(x, y) {
					var delta = 3;
					var n = 0;

					while (n < this.segments.length) {
						if (this.segments[n].isHorizontal()) {
							// TODO Need to check other coordinate as well

							if (Math.abs(this.segments[n].toY - y) < delta) {
								return n;
							}
						} else {
							if (Math.abs(this.segments[n].toX - x) < delta) {
								return n;
							}
						}

						++n;
					}

					return -1;
				};

				/**
				 * 
				 */
				Connection.prototype.click = function(x, y, event) {
					m_utils.debug("Connection.prototype.click");
					if (!this.isCompleted()) {
						m_utils.debug("Prorietary handling");
						var symbol = this.diagram
								.getSymbolContainingCoordinatesExcludeContainerSymbols(
										x / this.diagram.zoomFactor
												+ this.diagram.X_OFFSET, y
												/ this.diagram.zoomFactor
												+ this.diagram.Y_OFFSET);

						if (symbol != null) {
							m_utils.debug("Symbol found");
							var anchorPoint = symbol.getClosestAnchorPoint(x
									/ this.diagram.zoomFactor
									+ this.diagram.X_OFFSET, y
									/ this.diagram.zoomFactor
									+ this.diagram.Y_OFFSET);
							this.diagram.setAnchorPoint(anchorPoint);
						}
					} else {
						this.select();
					}
				};

				/**
				 * 
				 */
				Connection.prototype.dragStart = function(x, y, event) {
					this.clickedSegmentIndex = this.findClickedSegment(x, y);
				};

				/**
				 * 
				 */
				Connection.prototype.dragStop = function(x, y, event) {
				};

				/**
				 * 
				 */
				Connection.prototype.dragStartConditionExpressionText = function(
						x, y, event) {
				};

				/**
				 * 
				 */
				Connection.prototype.dragConditionExpressionText = function(dX,
						dY, x, y, event) {
					this.conditionExpressionTextXOffset = x
							* this.diagram.zoomFactor - this.diagram.X_OFFSET
							- this.fromAnchorPoint.x;
					this.conditionExpressionTextYOffset = y
							* this.diagram.zoomFactor - this.diagram.Y_OFFSET
							- this.fromAnchorPoint.y;

					this.conditionExpressionText.attr({
						"x" : x * this.diagram.zoomFactor
								- this.diagram.X_OFFSET,
						"y" : y * this.diagram.zoomFactor
								- this.diagram.Y_OFFSET
					});
				};

				/**
				 * 
				 */
				Connection.prototype.dragStopConditionExpressionText = function(
						x, y, event) {
				};

				/**
				 * 
				 */
				Connection.prototype.hoverInConditionExpressionText = function() {
					this.conditionExpressionText.attr({
						fill : m_constants.SELECT_STROKE_COLOR,
						cursor : "move"
					});
				};

				/**
				 * 
				 */
				Connection.prototype.hoverOutConditionExpressionText = function() {
					this.conditionExpressionText.attr({
						fill : this.isDataFlow() ? m_constants.DATA_FLOW_COLOR
								: m_constants.CONTROL_FLOW_COLOR,
						cursor : "default"
					});
				};

				/**
				 * 
				 */
				Connection.prototype.hoverIn = function() {
					this.path.attr({
						stroke : m_constants.SELECT_STROKE_COLOR,
						cursor : "move"
					});
				};

				/**
				 * 
				 */
				Connection.prototype.hoverOut = function() {
					this.path
							.attr({
								stroke : this.isDataFlow() ? m_constants.DATA_FLOW_COLOR
										: m_constants.CONTROL_FLOW_COLOR,
								cursor : "move"
							});
				};

				/**
				 * 
				 */
				Connection.prototype.createFlyOutMenu = function() {
					this.addFlyOutMenuItems([], [], [ {
						imageUrl : "../../images/icons/remove.png",
						imageWidth : 16,
						imageHeight : 16,
						clickHandler : Connection_removeClosure
					}, {
						imageUrl : "../../images/icons/connect.png",
						imageWidth : 16,
						imageHeight : 16,
						clickHandler : Connection_toggleConnectionType
					} ]);

					this.auxiliaryPickPath.toFront();
					this.path.toFront();
				};

				/**
				 * 
				 */
				Connection.prototype.proximityHoverIn = function(event) {
					if (this.diagram.isInNormalMode()) {
						this.adjustFlyOutMenu(event.pageX
								- this.diagram.X_OFFSET - 5, event.pageY
								- this.diagram.Y_OFFSET - 5, 40, 20);
						this.showFlyOutMenu();
					}
				};

				/**
				 * 
				 */
				Connection.prototype.proximityHoverOut = function(event) {
					if (this.diagram.isInNormalMode()) {
						this.hideFlyOutMenu();
					}
				};

				Connection.prototype.createFlyOutMenuBackground = function(x,
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
										"fill" : m_constants.FLY_OUT_MENU_FILL,
										"fill-opacity" : m_constants.FLY_OUT_MENU_START_OPACITY,
										"r" : m_constants.FLY_OUT_MENU_R
									});

					// Initialize return pointer for closure

					this.flyOutMenuBackground.auxiliaryProperties = {
						callbackScope : this
					};

					this.flyOutMenuBackground.hover(
							Connection_hoverInFlyOutMenuClosure,
							Connection_hoverOutFlyOutMenuClosure);
				};

				/**
				 * 
				 */
				Connection.prototype.remove = function() {
					// TODO add symbol/diagram cleanup
					this.path.remove();
					// Remove this connection from FROM and TO Symbol's
					// connection array
					m_utils.removeItemFromArray(
							this.toAnchorPoint.symbol.connections, this);
					m_utils.removeItemFromArray(
							this.fromAnchorPoint.symbol.connections, this);
					
					var command = m_command.createRemoveNodeCommand("connection.delete", this.diagram.model.id,
							this.oid, this.createTransferObject());
					m_commandsController.submitCommand(command);
				};
				
				/**
				 * 
				 */
				Connection.prototype.hide = function() {
					this.path.hide();
					this.visible = false;
					this.hideFlyOutMenu();
				}
				
				/**
				 * 
				 */
				Connection.prototype.show = function() {
					this.path.show();
					this.visible = true;
				}
				
				/**
				 * 
				 */
				Connection.prototype.flipFlowOrientation = function(
						flowOrientation) {
					// if (this.isDataFlow()) {
					// return;
					// }

					if (flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
						this.fromAnchorPoint = this.getFromSymbol().anchorPoints[(this.fromAnchorPoint.orientation + 1) % 4];
						this.toAnchorPoint = this.getToSymbol().anchorPoints[(this.toAnchorPoint.orientation + 1) % 4];
					} else {
						this.fromAnchorPoint = this.getFromSymbol().anchorPoints[(this.fromAnchorPoint.orientation + 3) % 4];
						this.toAnchorPoint = this.getToSymbol().anchorPoints[(this.toAnchorPoint.orientation + 3) % 4];
					}

					this.reroute();
				};
			}

			function Connection_dragMoveClosure(dX, dY, x, y, event) {
				this.auxiliaryProperties.callbackScope.dragMove(dX, dY, x, y,
						event);
			}

			function Connection_dragStartClosure(x, y, event) {
				this.auxiliaryProperties.callbackScope.dragStart(x, y, event);
			}

			function Connection_dragStopClosure(x, y, event) {
				this.auxiliaryProperties.callbackScope.dragStop(x, y, event);
			}

			function Connection_dragConditionExpressionTextClosure(dX, dY, x,
					y, event) {
				this.auxiliaryProperties.callbackScope
						.dragConditionExpressionText(dX, dY, x, y, event);
			}

			function Connection_dragStartConditionExpressionTextClosure(x, y,
					event) {
				this.auxiliaryProperties.callbackScope
						.dragStartConditionExpressionText(x, y, event);
			}

			function Connection_dragStopConditionExpressionTextClosure(x, y,
					event) {
				this.auxiliaryProperties.callbackScope
						.dragStopConditionExpressionText(x, y, event);
			}

			function Connection_hoverInConditionExpressionTextClosure() {
				this.auxiliaryProperties.callbackScope
						.hoverInConditionExpressionText();
			}

			function Connection_hoverOutConditionExpressionTextClosure() {
				this.auxiliaryProperties.callbackScope
						.hoverOutConditionExpressionText();
			}

			function Connection_clickClosure() {
				this.auxiliaryProperties.callbackScope.click();
			}

			function Connection_hoverInClosure() {
				this.auxiliaryProperties.callbackScope.hoverIn();
			}

			function Connection_hoverOutClosure() {
				this.auxiliaryProperties.callbackScope.hoverOut();
			}

			function Connection_hoverInFlyOutMenuClosure() {
				this.auxiliaryProperties.callbackScope.showFlyOutMenu();
			}

			function Connection_hoverOutFlyOutMenuClosure() {
				this.auxiliaryProperties.callbackScope.hideFlyOutMenu();
			}

			function Connection_removeClosure() {
				this.auxiliaryProperties.callbackScope.remove();
			}

			function Connection_toggleConnectionType() {
				this.auxiliaryProperties.callbackScope.toggleConnectionType();
			}

			/**
			 * From and to x, y values are stored redundantly to allow easy
			 * computation later
			 */
			function Segment(fromX, fromY, toX, toY, previousSegment) {
				this.fromX = fromX;
				this.fromY = fromY;
				this.toX = toX;
				this.toY = toY;
				this.previousSegment = previousSegment;
				this.nextSegment = null;

				if (this.previousSegment) {
					this.previousSegment.nextSegment = this;
				}

				/**
				 * 
				 */
				Segment.prototype.toString = function() {
					return "[object Lightdust.Segment()]";
				};

				Segment.prototype.isVertical = function() {
					return this.toX == this.fromX;
				};

				Segment.prototype.isHorizontal = function() {
					return this.toY == this.fromY;
				};

				Segment.prototype.isNorth = function() {
					return this.isVertical() && this.toY <= this.fromY;
				};

				Segment.prototype.isSouth = function() {
					return this.isVertical() && this.toY > this.fromY;
				};

				Segment.prototype.isEast = function() {
					return this.isHorizontal() && this.toX > this.fromX;
				};

				Segment.prototype.isWest = function() {
					return this.isHorizontal() && this.toX <= this.fromX;
				};

				Segment.prototype.hasSameOrientation = function(segment) {
					return (this.isNorth() && segment.isNorth())
							|| (this.isEast() && segment.isEast())
							|| (this.isSouth() && segment.isSouth())
							|| (this.isWest() && segment.isWest());
				};

				Segment.prototype.length = function() {
					return this.isHorizontal() ? Math
							.abs(this.toX - this.fromX) : Math.abs(this.toY
							- this.fromY);
				};

				/**
				 * 
				 */
				Segment.prototype.getSvgString = function() {
					var previousXOffset = 0;
					var previousYOffset = 0;
					var currentXOffset = 0;
					var currentYOffset = 0;
					var rotation = 0;
					var radius = Math.min(2 * this.length(),
							m_constants.CONNECTION_DEFAULT_EDGE_RADIUS);

					if (this.previousSegment != null) {
						radius = Math.min(2 * this.previousSegment.length(),
								radius);

						if (this.previousSegment.isNorth()) {
							previousYOffset = -radius;
						} else if (this.previousSegment.isEast()) {
							previousXOffset = radius;
						} else if (this.previousSegment.isSouth()) {
							previousYOffset = radius;
						} else if (this.previousSegment.isWest()) {
							previousXOffset = -radius;
						}

						if ((this.previousSegment.isNorth() && this.isEast())
								|| (this.previousSegment.isEast() && this
										.isSouth())
								|| (this.previousSegment.isSouth() && this
										.isWest())
								|| (this.previousSegment.isWest() && this
										.isNorth())) {
							rotation = 1;
						}
					}

					if (this.isNorth()) {
						currentYOffset = -radius;
					} else if (this.isEast()) {
						currentXOffset = radius;
					} else if (this.isSouth()) {
						currentYOffset = radius;
					} else if (this.isWest()) {
						currentXOffset = -radius;
					}

					var path = "M" + (this.fromX - previousXOffset) + " "
							+ (this.fromY - previousYOffset);

					if (this.previousSegment != null) {
						path += "A" + radius + " " + radius + " 0 0 "
								+ rotation + " "
								+ (this.fromX + currentXOffset) + " "
								+ (this.fromY + currentYOffset);
					}

					if (this.nextSegment != null) {
						path += "L" + (this.toX - currentXOffset) + " "
								+ (this.toY - currentYOffset);
					} else {
						path += "L" + this.toX + " " + this.toY;
					}

					return path;
				};
			}
		});