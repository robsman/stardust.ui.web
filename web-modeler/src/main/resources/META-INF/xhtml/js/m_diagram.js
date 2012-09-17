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
		[ "m_utils", "m_constants", "m_extensionManager", "m_urlUtils",
			"m_communicationController", "m_commandsController",
			"m_command", "m_session", "m_canvasManager", "m_messageDisplay", "m_symbol",
			"m_poolSymbol", "m_activitySymbol", "m_dataSymbol",
			"m_eventSymbol", "m_gatewaySymbol", "m_connection",
			"m_propertiesPanel", "m_processPropertiesPanel",
			"m_activityPropertiesPanel", "m_dataPropertiesPanel",
			"m_eventPropertiesPanel", "m_gatewayPropertiesPanel",
			"m_swimlanePropertiesPanel", "m_controlFlowPropertiesPanel",
			"m_dataFlowPropertiesPanel", "m_model", "m_process", "m_data",
			"m_modelerUtils", "m_autoScrollManager" ],
	function(m_utils, m_constants, m_extensionManager, m_urlUtils,
			m_communicationController, m_commandsController, m_command,
			m_session, m_canvasManager, m_messageDisplay, m_symbol, m_poolSymbol,
			m_activitySymbol, m_dataSymbol, m_eventSymbol, m_gatewaySymbol,
			m_connection, m_propertiesPanel, m_processPropertiesPanel,
			m_activityPropertiesPanel, m_dataPropertiesPanel,
			m_eventPropertiesPanel, m_gatewayPropertiesPanel,
			m_swimlanePropertiesPanel, m_controlFlowPropertiesPanel,
			m_dataFlowPropertiesPanel, m_model, m_process, m_data,
			m_modelerUtils, m_autoScrollManager) {

			var canvasPos = $("#canvas").position();
			var X_OFFSET = canvasPos.left; // Set fpr #panningSensor
			var Y_OFFSET = canvasPos.top; // Set for #toolbar + #messageDisplay
			// Adjustments for Editable Text on Symbol
			var WIDTH_ADJUSTMENT = 60;

			return {
				createDiagram : function(divId) {
					return new Diagram("canvas");
				}
			};

			var currentDiagram = null;
			var panningIntervalId = null;
			var symbolEditMode = false;

			/**
			 *
			 */
			function Diagram(newDivId) {
				currentDiagram = this;
				// Constants

				var SNAP_LINE_THRESHOLD = 15;

				// Public constants

				this.NORMAL_MODE = "NORMAL_MODE";
				this.RUBBERBAND_MODE = "RUBBERBAND_MODE";
				this.CONNECTION_MODE = "CONNECTION_MODE";
				this.SEPARATOR_MODE = "SEPARATOR_MODE";
				this.X_OFFSET = X_OFFSET;
				this.Y_OFFSET = Y_OFFSET;
				this.width = m_canvasManager.getCanvasWidth();
				this.height = m_canvasManager.getCanvasHeight();
				this.flowOrientation = m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL;
				this.zoomFactor = 1;
				this.divId = newDivId;
				this.modelId = null;
				this.processId = null;
				this.process = null;
				this.mode = this.NORMAL_MODE;
				this.symbols = [];

				// Activity symbols by OIDs

				this.activitySymbols = {};

				// Gateway symbols by OIDs

				this.gatewaySymbols = {};

				// Event symbols by OIDs

				this.eventSymbols = {};

				// Data symbols by OIDs

				this.dataSymbols = {};
				this.connections = [];
				this.currentSelection = [];
				this.currentConnection = null;
				this.newSymbol = null;
				// Caches the last newly created Symbol for server callbacks
				this.newSymbol = null;
				this.animationDelay = 0;
				this.animationEasing = null;
				this.symbolGlow = true;

				this.background = m_canvasManager.drawRectangle(0, 0,
						m_canvasManager.getCanvasWidth(), m_canvasManager
								.getCanvasHeight(), {
							"stroke-width" : 0,
							"fill" : "white"
						});

				this.background.auxiliaryProperties = {
					diagram : this
				};

				this.background.click(Diagram_clickClosure);

				// Register with Command Controller

				m_commandsController.registerCommandHandler(this);

				// Bind DOM elements

				// === Start Toolbar ===

				var toolbarPalettes = m_extensionManager
						.findExtensions("diagramToolbarPalette");

				var paletteTableRow = jQuery("#diagramToolbarTable #paletteRow");

				for ( var n = 0; n < toolbarPalettes.length; ++n) {
					if (!m_session.initialize().technologyPreview
							&& toolbarPalettes[n].visibility == "preview") {
						continue;
					}

					m_utils.debug("Adding " + toolbarPalettes[n].id);

					paletteTableRow
							.append("<td><div class=\"toolbar-section\"><div class=\"toolbar-section-content\"><table><tr id=\""
									+ toolbarPalettes[n].id
									+ "EntryRow\"></tr></table></div><div class=\"toolbar-section-footer\">"
									+ toolbarPalettes[n].title
									+ "</div></div></td>");

					var entryRow = jQuery("#diagramToolbarTable #paletteRow #"
							+ toolbarPalettes[n].id + "EntryRow");

					if (toolbarPalettes[n].contentHtmlUrl != null) {
						var extension = toolbarPalettes[n];

						jQuery(
								"#diagramToolbarTable #paletteRow #"
										+ toolbarPalettes[n].id + "EntryRow")
								.load(
										extension.contentHtmlUrl,
										function(response, status, xhr) {
											if (status == "error") {
												var msg = "Properties Page Load Error: "
														+ xhr.status
														+ " "
														+ xhr.statusText;

												jQuery(
														"#"
																+ panel.id
																+ " #"
																+ extension.pageId)
														.append(msg);
												m_utils.debug(msg);
											} else {
												m_utils
														.debug("Palette loaded: "
																+ extension.id);
												extension.controller.create();
											}
										});
					} else {
						var paletteEntries = m_extensionManager.findExtensions(
								"diagramToolbarPaletteEntry", "paletteId",
								toolbarPalettes[n].id);

						for ( var m = 0; m < paletteEntries.length; ++m) {
							if (!m_session.initialize().technologyPreview
									&& paletteEntries[m].visibility == "preview") {
								continue;
							}

							entryRow.append("<td><input id=\""
									+ paletteEntries[m].id
									+ "\" type=\"image\" src=\""
									+ paletteEntries[m].iconUrl + "\" "
									+ "title=\"" + paletteEntries[m].title
									+ "\" height=\"16\" width=\"16\" alt=\""
									+ paletteEntries[m].title
									+ "\" class=\"toolbarButton\" /></td>");

							jQuery(
									"#diagramToolbarTable #paletteRow #"
											+ toolbarPalettes[n].id
											+ "EntryRow #"
											+ paletteEntries[m].id)
									.click(
											{
												diagram : this,
												handler : paletteEntries[m].handler,
												provider : paletteEntries[m].provider,
												handlerMethod : paletteEntries[m].handlerMethod
											},
											function(event) {
												m_utils
														.debug("Clicked "
																+ event.data.handler
																+ " "
																+ event.data.handlerMethod);
												event.data.provider[event.data.handlerMethod]
														(event.data.diagram);
											});
						}
					}

				}

				var selectModeButton = jQuery("#selectModeButton");

				selectModeButton.click({
					"diagram" : this
				}, function(event) {
					event.data.diagram.setSelectMode();
				});

				var separatorModeButton = jQuery("#separatorModeButton");

				separatorModeButton.click({
					"diagram" : this
				}, function(event) {
					event.data.diagram.setSeparatorMode();
				});

				// === End Toolbar

				this.canvas = jQuery('#' + this.divId);
				this.scrollPane = jQuery("#scrollpane");

				// Define event handling for DOM elements

				this.canvas.mousedown({
					"diagram" : this
				},
						function(event) {
							event.data.diagram
									.onGlobalMouseDown(event.pageX
											- X_OFFSET
											+ event.data.diagram.scrollPane
													.scrollLeft(), event.pageY
											- Y_OFFSET
											+ event.data.diagram.scrollPane
													.scrollTop());
						});

				this.canvas.mousemove({
					"diagram" : this
				},
						function(event) {
							event.data.diagram
									.onGlobalMouseMove(event.pageX
											- X_OFFSET
											+ event.data.diagram.scrollPane
													.scrollLeft(), event.pageY
											- Y_OFFSET
											+ event.data.diagram.scrollPane
													.scrollTop());
						});

				this.canvas.mouseup({
					"diagram" : this
				}, function(event) {
					event.data.diagram
							.onGlobalMouseUp(event.pageX - X_OFFSET,
									event.pageX
											- X_OFFSET
											+ event.data.diagram.scrollPane
													.scrollLeft(), event.pageY
											- Y_OFFSET
											+ event.data.diagram.scrollPane
													.scrollTop());
				});

				this.panningSensorNorthWest = {};
				this.panningSensorNorth = {};
				this.panningSensorNorthEast = {};
				this.panningSensorEast = {};
				this.panningSensorSouthEast = {};
				this.panningSensorSouth = {};
				this.panningSensorSouthWest = {};
				this.panningSensorWest = {};

				this.panningSensorNorthWest.x = 0;
				this.panningSensorNorthWest.y = 0;
				this.panningSensorNorthWest.width = m_constants.PANNING_SENSOR_WIDTH;
				this.panningSensorNorthWest.height = m_constants.PANNING_SENSOR_WIDTH;

				this.panningSensorNorth.x = m_constants.PANNING_SENSOR_WIDTH;
				this.panningSensorNorth.y = 0;
				this.panningSensorNorth.width = m_constants.VIEWPORT_WIDTH - 2
						* m_constants.PANNING_SENSOR_WIDTH;
				this.panningSensorNorth.height = m_constants.PANNING_SENSOR_WIDTH;

				this.panningSensorNorthEast.x = m_constants.VIEWPORT_WIDTH
						- m_constants.PANNING_SENSOR_WIDTH;
				this.panningSensorNorthEast.y = 0;
				this.panningSensorNorthEast.width = m_constants.PANNING_SENSOR_WIDTH;
				this.panningSensorNorthEast.height = m_constants.PANNING_SENSOR_WIDTH;

				this.panningSensorEast.x = m_constants.VIEWPORT_WIDTH
						- m_constants.PANNING_SENSOR_WIDTH;
				this.panningSensorEast.y = m_constants.PANNING_SENSOR_WIDTH;
				this.panningSensorEast.width = m_constants.PANNING_SENSOR_WIDTH;
				this.panningSensorEast.height = m_constants.VIEWPORT_HEIGHT - 2
						* m_constants.PANNING_SENSOR_WIDTH;

				this.panningSensorSouthEast.x = m_constants.VIEWPORT_WIDTH
						- m_constants.PANNING_SENSOR_WIDTH;
				this.panningSensorSouthEast.y = m_constants.VIEWPORT_HEIGHT
						- m_constants.PANNING_SENSOR_WIDTH;
				this.panningSensorSouthEast.width = m_constants.PANNING_SENSOR_WIDTH;
				this.panningSensorSouthEast.height = m_constants.PANNING_SENSOR_WIDTH;

				this.panningSensorSouth.x = m_constants.PANNING_SENSOR_WIDTH;
				this.panningSensorSouth.y = m_constants.VIEWPORT_HEIGHT
						- m_constants.PANNING_SENSOR_WIDTH;
				this.panningSensorSouth.width = m_constants.VIEWPORT_WIDTH - 2
						* m_constants.PANNING_SENSOR_WIDTH;
				this.panningSensorSouth.height = m_constants.PANNING_SENSOR_WIDTH;

				this.panningSensorSouthWest.x = 0;
				this.panningSensorSouthWest.y = m_constants.VIEWPORT_HEIGHT
						- m_constants.PANNING_SENSOR_WIDTH;
				this.panningSensorSouthWest.width = m_constants.PANNING_SENSOR_WIDTH;
				this.panningSensorSouthWest.height = m_constants.PANNING_SENSOR_WIDTH;

				this.panningSensorWest.x = 0;
				this.panningSensorWest.y = m_constants.PANNING_SENSOR_WIDTH;
				this.panningSensorWest.width = m_constants.PANNING_SENSOR_WIDTH;
				this.panningSensorWest.height = m_constants.VIEWPORT_HEIGHT - 2
						* m_constants.PANNING_SENSOR_WIDTH;

				this.horizontalSnapLinePosition = this.height * 0.5;
				this.isHorizontalSnap = false;
				this.horizontalSnapLine = m_canvasManager.drawPath("", {
					"stroke" : m_constants.SNAP_LINE_COLOR,
					"stroke-width" : m_constants.SNAP_LINE_STROKE_WIDTH,
					'stroke-dasharray' : m_constants.SNAP_LINE_DASHARRAY
				});
				this.verticalSnapLinePosition = this.width * 0.5;
				this.isVerticalSnap = false;
				this.verticalSnapLine = m_canvasManager.drawPath("", {
					"stroke" : m_constants.SNAP_LINE_COLOR,
					"stroke-width" : m_constants.SNAP_LINE_STROKE_WIDTH,
					'stroke-dasharray' : m_constants.SNAP_LINE_DASHARRAY
				});

				this.separationActive = false;
				this.separationStarted = false;
				this.separatorX = 0;
				this.separatorY = 0;
				this.separatorDX = 0;
				this.separatorDY = 0;
				this.separatorList = [];
				this.horizontalSeparatorLine = m_canvasManager.drawPath("", {
					"stroke" : m_constants.SEPARATOR_LINE_COLOR,
					"stroke-width" : m_constants.SEPARATOR_LINE_STROKE_WIDTH,
					'stroke-dasharray' : m_constants.SEPARATOR_LINE_DASHARRAY
				});
				this.verticalSeparatorLine = m_canvasManager.drawPath("", {
					"stroke" : m_constants.SEPARATOR_LINE_COLOR,
					"stroke-width" : m_constants.SEPARATOR_LINE_STROKE_WIDTH,
					'stroke-dasharray' : m_constants.SEPARATOR_LINE_DASHARRAY
				});

				this.rubberBand = m_canvasManager.drawRectangle(0, 0, 0, 0, {
					'stroke' : m_constants.RUBBERBAND_COLOR,
					'fill' : m_constants.RUBBERBAND_COLOR,
					'fill-opacity' : 0.1,
					'stroke-width' : m_constants.RUBBERBAND_STROKE_WIDTH,
					'stroke-dasharray' : m_constants.RUBBERBAND_DASHARRAY
				});

				this.rubberBand.hide();

				this.rubberBandX = 0;
				this.rubberBandY = 0;
				this.rubberBandWidth = 0;
				this.rubberBandHeight = 0;

				this.editableText = jQuery("#editable")
						.editable(
								function(value, settings) {
									return value;
								},
								{
									type : "text",
									event : "dblclick",
									onblur : "submit",
									onreset : function(settings, value) { // On
										// Reset
										// hide
										// the
										// text box and reset
										// the value
										jQuery.data(document, "diagram")
												.cancelEditable();
									},
									onsubmit : function(settings, value) {
										jQuery.data(document, "diagram")
												.submitEditable(
														$('input', this).val());
									}
								}).css("font-family",
								m_constants.DEFAULT_FONT_FAMILY).css(
								"font-size", m_constants.DEFAULT_FONT_SIZE);

				jQuery.data(document, "diagram", this);

				this.currentTextPrimitive = null;
				this.poolSymbol = null;

				/**
				 *
				 */
				Diagram.prototype.toString = function() {
					return "Lightdust.Diagram";
				};

				/**
				 *
				 */
				Diagram.prototype.initialize = function() {
					// Load all models to populate Properties Panels

					m_model.loadModels();

					m_utils.debug("===> Loaded Models");
					m_utils.debug(m_model.getModels());

					// TODO Bind against loaded models

					this.modelId = jQuery.url.setUrl(window.location.search)
							.param("modelId");
					this.processId = jQuery.url.setUrl(window.location.search)
							.param("processId");
					this.model = m_model.findModel(this.modelId);

					//TODO - this is a temporary workaround. This will have to be replaced with
					// a solution where we can refresh / reload models from server side
					if (!this.model) {
						m_model.loadModels(true);
						this.model = m_model.findModel(this.modelId);
						window.parent.EventHub.events.publish("RELOAD_MODELS");
					}

					this.process = this.model.processes[this.processId];

					// Initialize Properties Panels

					m_processPropertiesPanel.initialize(this);
					m_activityPropertiesPanel.initialize(this);
					m_dataPropertiesPanel.initialize(this);
					m_eventPropertiesPanel.initialize(this);
					m_gatewayPropertiesPanel.initialize(this);
					m_swimlanePropertiesPanel.initialize(this);
					m_controlFlowPropertiesPanel
							.initialize(this);
					m_dataFlowPropertiesPanel.initialize(this);
					m_autoScrollManager.initScrollManager("scrollpane", function() {
						var inAutoScrollMode = false;
						if (true == currentDiagram.isInConnectionMode()
								|| currentDiagram.mode == currentDiagram.RUBBERBAND_MODE
								|| null != currentDiagram.newSymbol
								|| true == currentDiagram.isDragAndDropMode()) {
							inAutoScrollMode = true;
						} else {
							inAutoScrollMode = false;
						}

						return inAutoScrollMode;
					}, function(event) {
						if (null != currentDiagram.newSymbol
								|| currentDiagram.isInConnectionMode()) {
							currentDiagram.onGlobalMouseMove(event.pageX
									- X_OFFSET
									+ currentDiagram.scrollPane
											.scrollLeft(), event.pageY
									- Y_OFFSET
									+ currentDiagram.scrollPane
											.scrollTop());
						} else if (currentDiagram.currentSelection.length > 0) {
							for (var i in currentDiagram.currentSelection) {
								if (currentDiagram.currentSelection[i].toString() == "Lightdust.Connection") {
									//TODO - The connnection should stick with mouse pointer
									// in case of auto-scroll in case of a re-route
								} else {
									currentDiagram.currentSelection[i].move(event.pageX
											- X_OFFSET
											+ currentDiagram.scrollPane
													.scrollLeft(), event.pageY
											- Y_OFFSET
											+ currentDiagram.scrollPane
													.scrollTop())
								}
							}
						}
					});
				};

				/**
				 *
				 */
				Diagram.prototype.getEndpointUrl = function() {
					return m_urlUtils.getContextName()
							+ "/services/rest/modeler/" + new Date().getTime();
				};

				/**
				 *
				 */
				Diagram.prototype.createTransferObject = function() {
					var transferObject = {};

					// Copy and prepare children

					transferObject.poolSymbols = {};

					transferObject.poolSymbols[this.poolSymbol.id] = this.poolSymbol
							.createTransferObject();

					return transferObject;
				};

				/**
				 *
				 */
				Diagram.prototype.findSymbolByGuid = function(guid) {

					if (null != guid) {
						for ( var i = 0; i < this.symbols.length; i++) {
							if (this.symbols[i].oid == guid) {
								return this.symbols[i];
							}
						}
					}

					return null;
				};

				/**
				 *
				 */
				Diagram.prototype.findSymbolByModelElementGuid = function(guid) {

					if (null != guid) {
						for ( var i = 0; i < this.symbols.length; i++) {
							if (this.symbols[i].modelElement != null
									&& this.symbols[i].modelElement.oid == guid) {
								return this.symbols[i];
							}
						}
					}

					return null;
				};

				/**
				 *
				 */
				Diagram.prototype.findConnection = function(conn) {

					for ( var i = 0; i < this.connections.length; i++) {
						if (this.connections[i].fromModelElementOid == conn.fromModelElementOid
								&& this.connections[i].toModelElementOid == conn.toModelElementOid) {
							// while adding a connection, to update connection
							// connection is searched using modelELementOid
							return this.connections[i];
						}
					}

					return null;
				};

				/**
				 *
				 */
				Diagram.prototype.findConnectionByGuid = function(guid) {

					if (null != guid) {
						for ( var i = 0; i < this.connections.length; i++) {
							if (this.connections[i].oid == guid) {
								return this.connections[i];
							}
						}
					}
					return null;
				};

				/**
				 *
				 */
				Diagram.prototype.findConnectionByModelElementGuid = function(
						guid) {

					if (null != guid) {
						for ( var i = 0; i < this.connections.length; i++) {
							if (this.connections[i].modelElement.oid == guid) {
								return this.connections[i];
							}
						}
					}
					return null;
				};

				/**
				 * The diagram serves as a dispatcher for all changes on model
				 * elements underneath the diagram process.
				 */
				Diagram.prototype.processCommand = function(command) {
					m_utils.debug("===> Diagram Process Command");
					m_utils.debug(command.type);

					// Parse the response JSON from command pattern

					var obj = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != obj && null != obj.changes) {

						// TODO is lastSymbol still needed

						for ( var i = 0; i < obj.changes.added.length; i++) {
							if ((null != this.lastSymbol && null != obj.changes.added[i].type)
									&& obj.changes.added[i].type
											.match(this.lastSymbol.type)) {
								this.lastSymbol
										.applyChanges(obj.changes.added[i]);
								this.lastSymbol = null;
							}// For connections lastSymbol will be empty
							else if (null != obj.changes.added[i].modelElement) {
								// for connections , search by connectionId to
								// set OID
								var conn = this
										.findConnection(obj.changes.added[i]);
								if (null != conn) {
									conn.applyChanges(obj.changes.added[i]);
									conn.refresh();
								}
//								else {
//									//Find swimlane from modified array
//									var swimlane;
//									for ( var j = 0; j < obj.changes.modified.length; j++) {
//										if (obj.changes.modified[j].type == m_constants.SWIMLANE_SYMBOL) {
//											swimlane = obj.changes.modified[j];
//										}
//									}
//									if (swimlane) {
//										if (obj.changes.added[i].type == m_constants.ACTIVITY_SYMBOL) {
//											m_activitySymbol.createActivitySymbolFromJson(this, swimlane, obj.changes.added[i]);
//										}
//									}
//								}
							}
						}

						// Apply changes

						this.animationDelay = 1000;
						this.animationEasing = "<";

						for ( var i = 0; i < obj.changes.modified.length; i++) {
							var symbol = this
									.findSymbolByGuid(obj.changes.modified[i].oid);

							if (symbol != null) {
								m_utils.debug("Up to changed symbol:");
								m_utils.debug(symbol);

								symbol.lastModifyingUser = command.account; //m_session.getUserByAccount(command.account);

								symbol.applyChanges(obj.changes.modified[i]);
								m_utils.debug("Changed symbol to:");
								m_utils.debug(symbol);
								symbol.refresh();
							}

							symbol = this
									.findSymbolByModelElementGuid(obj.changes.modified[i].oid);

							if (symbol != null) {
								m_utils.debug("Up to changed symbol:");
								m_utils.debug(symbol);
								// Modifies only modelElement,rather than whole
								// symbol
								m_utils.inheritFields(symbol.modelElement,
										obj.changes.modified[i]);
								/* symbol.applyChanges(obj.changes.modified[i]); */
								m_utils.debug("Changed symbol to:");
								m_utils.debug(symbol);
								symbol.refresh();
								// TODO - update properties panel on
								// modelElement change
							}

							// Check if connection is modified
							var conn = this
									.findConnectionByGuid(obj.changes.modified[i].oid);

							if (null != conn) {
								conn.applyChanges(obj.changes.modified[i]);
								conn.refresh();
							} else {
								conn = this
										.findConnectionByModelElementGuid(obj.changes.modified[i].oid);
								if (null != conn) {
									m_utils.inheritFields(conn.modelElement,
											obj.changes.modified[i]);
									conn.refresh();
								}
							}
						}

						this.animationDelay = 0;
						this.animationEasing = null;

						// Delete removed elements
						for ( var i = 0; i < obj.changes.removed.length; i++) {
							var symbol = this
									.findSymbolByGuid(obj.changes.removed[i].oid);
							if (null == symbol) {
								symbol = this
										.findSymbolByModelElementGuid(obj.changes.removed[i].oid);
							}
							if (null == symbol) {
								symbol = this
										.findConnectionByGuid(obj.changes.removed[i].oid);
							}
							if (null != symbol) {
								symbol.remove();
							}

						}
					}
				};

				/**
				 *
				 */
				Diagram.prototype.findActivitySymbolById = function(id) {
					for ( var n in this.activitySymbols) {
						var activitySymbol = this.activitySymbols[n];

						if (activitySymbol.modelElement.id == id) {
							return activitySymbol;
						}
					}

					return null;
				};

				/**
				 *
				 */
				Diagram.prototype.findGatewaySymbolById = function(id) {
					for ( var n in this.gatewaySymbols) {
						var gatewaySymbol = this.gatewaySymbols[n];

						if (gatewaySymbol.modelElement.id == id) {
							return gatewaySymbol;
						}
					}

					return null;
				};

				/**
				 *
				 */
				Diagram.prototype.findEventSymbolById = function(id) {
					for ( var n in this.eventSymbols) {
						var eventSymbol = this.eventSymbols[n];

						if (eventSymbol.modelElement.id == id) {
							return eventSymbol;
						}
					}

					return null;
				};

				/**
				 *
				 */
				Diagram.prototype.findDataSymbolById = function(id) {
					for ( var n in this.dataSymbols) {
						var dataSymbol = this.dataSymbols[n];

						if (dataSymbol.dataId == id) {
							return dataSymbol;
						}
					}

					return null;
				};

				/**
				 *
				 */
				Diagram.prototype.submitUpdate = function() {
					// TODO Incomplete
					m_commandsController.submitCommand(m_command
							.createUpdateCommand("/models/" + this.model.id
									+ "/processes/" + this.process.id
									+ "/diagrams/4711", null, this
									.createTransferObject()));
				};

				/**
				 *
				 */
				Diagram.prototype.onUpdate = function() {
				};

				/**
				 *
				 */
				Diagram.prototype.addActivitySymbol = function() {
					this.newSymbol = m_activitySymbol.createActivitySymbol(
							this, m_constants.MANUAL_ACTIVITY_TYPE);
				};

				/**
				 *
				 */
				Diagram.prototype.isDragAndDropMode = function() {
					if (parent.iDnD.getTransferObject()) {
						return true;
					}

					return false;
				};

				/**
				 *
				 */
				Diagram.prototype.isInConnectionMode = function() {
					return this.mode == this.CONNECTION_MODE;
				};

				/**
				 *
				 */
				Diagram.prototype.setSelectMode = function() {
					this.clearCurrentSelection();
					this.mode = this.NORMAL_MODE;
					this.newSymbol = null;

					if (this.currentConnection != null) {
						this.currentConnection.remove();

						this.currentConnection = null;
					}

					this.verticalSeparatorLine.hide();
					this.horizontalSeparatorLine.hide();
				};

				/**
				 *
				 */
				Diagram.prototype.setSeparatorMode = function() {
					this.clearCurrentSelection();
					this.mode = this.SEPARATOR_MODE;
					this.newSymbol = null;

					if (this.currentConnection != null) {
						this.currentConnection.remove();

						this.currentConnection = null;
					}

					this.verticalSeparatorLine.show();
					this.horizontalSeparatorLine.show();
				};

				/**
				 *
				 */
				Diagram.prototype.isInNormalMode = function() {
					return this.mode == this.NORMAL_MODE;
				};

				/**
				 * Used for initial symbol drawing and DnD
				 */
				Diagram.prototype.onGlobalMouseDown = function(x, y) {
					if (this.mode == this.NORMAL_MODE) {
						this.mode = this.RUBBERBAND_MODE;

						this.rubberBandX = x * this.zoomFactor;
						this.rubberBandY = y * this.zoomFactor;
						this.rubberBand.attr({
							"x" : this.rubberBandX,
							"y" : this.rubberBandY,
							"width" : 0,
							"height" : 0
						});
						this.rubberBand.show();
						this.rubberBand.toFront();
					} else if (this.mode == this.SEPARATOR_MODE) {
						this.separationStarted = true;
						this.separationActive = true;
						this.separatorX = x * this.zoomFactor;
						this.separatorY = y * this.zoomFactor;
					}
				};

				/**
				 *
				 */
				Diagram.prototype.onGlobalMouseMove = function(x, y) {
					if (this.newSymbol != null) {
						if (this.newSymbol.isPrepared()) {
							this.newSymbol.move(x * this.zoomFactor, y
									* this.zoomFactor);
						} else {
							this.newSymbol.prepare(x * this.zoomFactor, y
									* this.zoomFactor);
						}
					} else if (this.mode == this.RUBBERBAND_MODE) {
						this.rubberBandWidth = x * this.zoomFactor
								- this.rubberBandX;
						this.rubberBandHeight = y * this.zoomFactor
								- this.rubberBandY;

						this.rubberBand.attr({
							"width" : this.rubberBandWidth,
							"height" : this.rubberBandHeight
						});
					} else if (this.mode == this.SEPARATOR_MODE) {
						if (this.separationActive) {
							var dX = x * this.zoomFactor - this.separatorX;
							var dY = y * this.zoomFactor - this.separatorY;

							if (this.separationStarted) {
								this.separationStarted = false;
								this.separatorList = [];

								if (Math.abs(dX) > Math.abs(dY)) {
									if (dX > 0) {
										this.separatorDX = 1;
										this.separatorDY = 0;

										for ( var n in this.symbols) {
											if (this.symbols[n].x > this.separatorX
													&& this.symbols[n].type != m_constants.SWIMLANE_SYMBOL) {
												this.separatorList
														.push(this.symbols[n]);
											}
										}

									} else {
										this.separatorDX = -1;
										this.separatorDY = 0;

										for ( var n in this.symbols) {
											if (this.symbols[n].x < this.separatorX
													&& this.symbols[n].type != m_constants.SWIMLANE_SYMBOL) {
												this.separatorList
														.push(this.symbols[n]);
											}
										}
									}
								} else {
									if (dY < 0) {
										this.separatorDX = 0;
										this.separatorDY = -1;

										for ( var n in this.symbols) {
											if (this.symbols[n].y > this.separatorY
													&& this.symbols[n].type != m_constants.SWIMLANE_SYMBOL) {
												this.separatorList
														.push(this.symbols[n]);
											}
										}
									} else {
										this.separatorDX = 0;
										this.separatorDY = 1;

										for ( var n in this.symbols) {
											if (this.symbols[n].y > this.separatorY
													&& this.symbols[n].type != m_constants.SWIMLANE_SYMBOL) {
												this.separatorList
														.push(this.symbols[n]);
											}
										}
									}
								}
							} else {
								for ( var n in this.separatorList) {
									this.separatorList[n].moveBy(dX
											* Math.abs(this.separatorDX), dY
											* Math.abs(this.separatorDY));
									this.separatorList[n].parentSymbol
											.adjustToSymbolBoundaries();
								}
							}

							if (Math.abs(this.separatorDX) > 0) {
								this.verticalSeparatorLine.attr({
									"path" : "M" + x + " 0L" + x + " "
											+ this.height,
									"width" : this.width,
									"height" : this.height
								});
							} else {
								this.horizontalSeparatorLine.attr({
									"path" : "M0" + " " + y + "L" + this.width
											+ " " + y,
									"width" : this.width,
									"height" : this.height
								});
							}

							// Remember position

							this.separatorX = x * this.zoomFactor;
							this.separatorY = y * this.zoomFactor;
						} else {
							this.verticalSeparatorLine.attr({
								"path" : "M" + x + " 0L" + x + " "
										+ this.height,
								"width" : this.width,
								"height" : this.height
							});
							this.horizontalSeparatorLine.attr({
								"path" : "M0" + " " + y + "L" + this.width
										+ " " + y,
								"width" : this.width,
								"height" : this.height
							});
						}

						// TODO Workaround

						this.verticalSnapLine.hide();
						this.horizontalSnapLine.hide();
					} else if (this.isInConnectionMode()) {
						if (this.currentConnection != null) {

							// TODO Can we guarantee that Raphael objects always
							// get their events first

							if (this.currentConnection.toAnchorPoint.symbol == null) {
								/*m_utils
										.debug("this.currentConnection.toAnchorPoint.symbol == null");*/

								this.currentConnection.toAnchorPoint.moveTo(x
										* this.zoomFactor, y * this.zoomFactor);
								this.currentConnection.reroute();
							}
						}
					}
				};

				/**
				 *
				 */
				Diagram.prototype.onGlobalMouseUp = function(x, y) {
					if (this.mode == this.RUBBERBAND_MODE) {
						this.clearCurrentSelection();

						var n = 0;

						while (n < this.symbols.length) {
							if (this.symbols[n].isInRectangle(this.rubberBandX
									* this.zoomFactor, this.rubberBandY,
									this.rubberBandWidth * this.zoomFactor,
									this.rubberBandHeight)) {
								this.symbols[n].select();
							}

							++n;
						}

						this.mode = this.NORMAL_MODE;

						this.rubberBand.hide();
					} else if (this.mode == this.SEPARATOR_MODE) {
						this.separationActive = false;
					}
				};

				/**
				 *
				 */
				Diagram.prototype.getSymbolContainingCoordinates = function(x,
						y) {
					for ( var n in this.symbols) {
						if (this.symbols[n].isInBoundingBox(x, y)) {
							return this.symbols[n];
						}
					}

					return null;
				};

				/**
				 *
				 */
				Diagram.prototype.moveLeftOfBy = function(x, dX) {
					for ( var n in this.symbols) {
						if (this.symbols[n].x < x) {
							this.symbols[n].moveBy(dX, 0);
						}
					}
				};

				/**
				 *
				 */
				Diagram.prototype.moveRightOfBy = function(x, dX) {
					for ( var n in this.symbols) {
						if (this.symbols[n].x > x) {
							this.symbols[n].moveBy(dX, 0);
						}
					}
				};

				/**
				 *
				 */
				Diagram.prototype.moveAboveBy = function(y, dY) {
					for ( var n in this.symbols) {
						if (this.symbols[n].y < y) {
							this.symbols[n].moveBy(0, dY);
						}
					}
				};

				/**
				 *
				 */
				Diagram.prototype.moveBelowBy = function(y, dY) {
					for ( var n in this.symbols) {
						if (this.symbols[n].y > y) {
							this.symbols[n].moveBy(0, dY);
						}
					}
				};

				/**
				 *
				 */
				Diagram.prototype.getSymbolContainingCoordinatesExcludeContainerSymbols = function(
						x, y) {
					for ( var n in this.symbols) {
						if (!this.symbols[n].isContainerSymbol()
								&& this.symbols[n].isInBoundingBox(x, y)) {
							return this.symbols[n];
						}
					}

					return null;
				};

				/**
				 *
				 */
				Diagram.prototype.checkSnapLines = function(symbol) {
					this.verticalSnapLine.hide();

					this.isVerticalSnap = false;

					for ( var n in this.symbols) {
						if (symbol == this.symbols[n]) {
							continue;
						}

						if (Math.abs(symbol.getXCenter()
								- this.symbols[n].getXCenter()) < SNAP_LINE_THRESHOLD) {
							this.adjustVerticalSnapLine(this.symbols[n]
									.getXCenter());
							this.verticalSnapLine.show();
							this.verticalSnapLine.toFront();

							this.isVerticalSnap = true;

							break;
						}
					}

					this.horizontalSnapLine.hide();

					this.isHorizontalSnap = false;

					for ( var n in this.symbols) {
						if (symbol == this.symbols[n]) {
							continue;
						}

						if (Math.abs(symbol.getYCenter()
								- this.symbols[n].getYCenter()) < SNAP_LINE_THRESHOLD) {
							this.adjustHorizontalSnapLine(this.symbols[n]
									.getYCenter());
							this.horizontalSnapLine.show();
							this.horizontalSnapLine.toFront();

							this.isHorizontalSnap = true;

							break;
						}
					}
				};

				/**
				 *
				 */
				Diagram.prototype.hideSnapLines = function(symbol) {
					this.verticalSnapLine.hide();
					this.isVerticalSnap = false;

					this.horizontalSnapLine.hide();
					this.isHorizontalSnap = false;
				};

				/**
				 *
				 */
				Diagram.prototype.adjustVerticalSnapLine = function(newPosition) {
					this.verticalSnapLinePosition = newPosition;
					this.verticalSnapLine
							.attr("path", "M" + this.verticalSnapLinePosition
									+ " 0L" + this.verticalSnapLinePosition
									+ " " + this.height);
				};

				/**
				 *
				 */
				Diagram.prototype.adjustHorizontalSnapLine = function(
						newPosition) {
					this.horizontalSnapLinePosition = newPosition;
					this.horizontalSnapLine.attr("path", "M0 "
							+ this.horizontalSnapLinePosition + "L"
							+ this.width + " "
							+ this.horizontalSnapLinePosition);
				};

				/**
				 *
				 */
				Diagram.prototype.snapSymbol = function(symbol) {
					if (this.isVerticalSnap) {
						symbol.moveBy(this.verticalSnapLinePosition
								- symbol.getXCenter(), 0);

					}

					if (this.isHorizontalSnap) {
						symbol.moveBy(0, this.horizontalSnapLinePosition
								- symbol.getYCenter());

					}

					this.isVerticalSnap = false;
					this.isHorizontalSnap = false;
					this.verticalSnapLine.hide();
					this.horizontalSnapLine.hide();
				};

				/**
				 *
				 */
				Diagram.prototype.onClick = function(x, y) {
					if (this.newSymbol != null) {
						// If the symbol was created with a connection traversal
						// the connection needs to be completed, too
						if (null != this.currentConnection) {
							var status = this.placeNewSymbol(x - this.X_OFFSET,
									y - this.Y_OFFSET, true);
							this.currentConnection.toModelElementOid = this.lastSymbol.oid;
							this.currentConnection.updateAnchorPointForSymbol();
							this.currentConnection.complete();
							this.currentConnection = null;
						} else {
							this.placeNewSymbol(x * this.zoomFactor, y
									* this.zoomFactor);
						}
					} else if (this.mode == this.NORMAL_MODE) {
						this.clearCurrentSelection();
						m_messageDisplay.clear();
					} else if (this.mode == this.CONNECTION_MODE
							&& this.currentConnection != null) {
						this.currentConnection.remove();
						this.currentConnection = null;
						m_messageDisplay.clear();
						this.mode = this.NORMAL_MODE;
					}
				};

				/**
				 * sync : Synchronous AJAX call is made, if set(needed in
				 * scenario like creating symbol from flyout menu where symbol
				 * should be created before createConnection)
				 */
				Diagram.prototype.placeNewSymbol = function(x, y, sync) {
					this.newSymbol.complete(sync);
					// If symbol is not contained in swimlane, return
					if (!this.newSymbol.isCompleted()) {
						this.newSymbol = null;
						return false;
					}

					this.snapSymbol(this.newSymbol);

					this.lastSymbol = this.newSymbol;
					this.newSymbol = null;
					return true;
				};

				/**
				 *
				 */
				Diagram.prototype.flipFlowOrientation = function(anchorPoint) {
					if (this.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
						this.flowOrientation = m_constants.DIAGRAM_FLOW_ORIENTATION_HORIZONTAL;
					} else {
						this.flowOrientation = m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL;
					}

					this.poolSymbol.flipFlowOrientation(this.flowOrientation);
					this.poolSymbol.recalculateBoundingBox();
					this.poolSymbol.adjustGeometry();

					// Rotate anchor points

					for ( var n in this.connections) {
						this.connections[n]
								.flipFlowOrientation(this.flowOrientation);
					}
				};

				/**
				 *
				 */
				Diagram.prototype.print = function(anchorPoint) {
					jQuery("#scrollpane").print();
				};

				/**
				 *
				 */
				Diagram.prototype.connectSymbol = function(symbol) {
					this.mode = this.CONNECTION_MODE;

					if (this.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
						this.currentConnection = m_connection.createConnection(
								this, symbol.anchorPoints[2]);
					} else {
						this.currentConnection = m_connection.createConnection(
								this, symbol.anchorPoints[1]);
					}
					if(null!=this.currentConnection){
						m_messageDisplay
						.showMessage("Select second anchor point for connection.");

						// Set dummy anchor point
						this.currentConnection.setDummySecondAnchorPoint();
					}else{
						this.mode = this.NORMAL_MODE;
					}
				};

				/**
				 *
				 */
				Diagram.prototype.connectToActivity = function(symbol) {
					this.addAndConnectSymbol(symbol, m_activitySymbol
							.createActivitySymbol(this));
				};

				/**
				 *
				 */
				Diagram.prototype.connectToGateway = function(symbol) {
					this.addAndConnectSymbol(symbol, m_gatewaySymbol
							.createGatewaySymbol(this));
				};

				/**
				 *
				 */
				Diagram.prototype.connectToStopEvent = function(symbol) {
					this.addAndConnectSymbol(symbol, m_eventSymbol
							.createStopEventSymbol(this));
				};

				/**
				 *
				 */
				Diagram.prototype.addAndConnectSymbol = function(startSymbol,
						targetSymbol) {
					this.newSymbol = targetSymbol;

					if (this.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
						this.newSymbol.prepare(startSymbol.x,
								startSymbol.y + 200);
						// Create connection if connectionValidation passes
						this.currentConnection = m_connection.createConnection(
								this, startSymbol.anchorPoints[2]);
						if (null != this.currentConnection
								&& this.currentConnection.validateCreateConnection(
										this.currentConnection.fromAnchorPoint,
										this.newSymbol.anchorPoints[0])) {
							this.currentConnection.prepare();
							this.currentConnection
									.setSecondAnchorPointNoComplete(this.newSymbol.anchorPoints[0]);
						} else {
							// Remove the connection and symbol created, if
							// validation fails
							if (this.currentConnection) {
								this.currentConnection.remove();
								this.currentConnection
							}
							this.newSymbol.remove();
							this.newSymbol = null;
						}
					} else {
						this.newSymbol.prepare(startSymbol.x + 200,
								startSymbol.y);
						this.currentConnection = m_connection.createConnection(
								this, startSymbol.anchorPoints[1]);
						this.currentConnection.prepare();
						this.currentConnection
								.setSecondAnchorPointNoComplete(this.newSymbol.anchorPoints[3]);
					}

					// TODO Is this needed
					this.mode = this.NORMAL_MODE;
				};

				/**
				 *
				 */
				Diagram.prototype.setAnchorPoint = function(anchorPoint) {
					if (this.currentConnection == null) {
						// createConnection returns null, if anchorPoint is not
						// valid.Ex. trying to create a connection from End
						// Event
						this.currentConnection = this
								.createConnection(anchorPoint);

						// Set dummy anchor point
						if (this.currentConnection) {
							this.currentConnection.setDummySecondAnchorPoint();
						}else{
							this.currentConnection = null;
							this.mode = this.NORMAL_MODE;
						}
					} else {
						// Validate if connection is allowed on current anchor
						// point
						if (this.currentConnection.validateCreateConnection(
								this.currentConnection.fromAnchorPoint,
								anchorPoint)) {
							this.currentConnection.updateAnchorPointForSymbol();
							this.currentConnection
									.setSecondAnchorPoint(anchorPoint);
							if (!this.currentConnection.isCompleted()) {
								this.currentConnection.remove();
							} else {
								this.currentConnection.select();
								m_messageDisplay
								.showMessage("Connection created");
							}
							this.currentConnection = null;
							this.mode = this.NORMAL_MODE;
						}else{
							if(this.currentConnection.toAnchorPoint.symbol){
								m_utils.removeItemFromArray(
										this.currentConnection.toAnchorPoint.symbol.connections, this.currentConnection);
							}

						}
					}
				};

				/**
				 * TODO Review
				 */
				Diagram.prototype.createConnection = function(anchorPoint) {
					return m_connection.createConnection(this, anchorPoint);
				};
				/**
				 *
				 */
				Diagram.prototype.addToCurrentSelection = function(drawable) {
					this.currentSelection.push(drawable);
				};

				/**
				 *
				 */
				Diagram.prototype.deselectCurrentSelection = function() {
					for ( var item in this.currentSelection) {
						this.currentSelection[item].deselect();
					}
				};

				/**
				 *
				 */
				Diagram.prototype.clearCurrentSelection = function() {
					this.deselectCurrentSelection();
					this.currentSelection = [];

					this.showProcessPropertiesPanel();
				};

				/**
				 * Clears the current selected symbol/connection. New new action
				 * is made from toolbar(e.x Create Activity), old action(e.x
				 * create connecion) if in progess in removed
				 */
				Diagram.prototype.clearCurrentToolSelection = function() {
					this.clearCurrentSelection();
					if (this.currentConnection != null) {
						this.currentConnection.remove();

						this.currentConnection = null;
					}
					if (this.newSymbol != null) {
						this.newSymbol.remove();
						this.newSymbol = null;
					}
					this.mode = this.NORMAL_MODE;
				};

				/**
				 *
				 */
				Diagram.prototype.showProcessPropertiesPanel = function() {
					m_processPropertiesPanel.getInstance().setElement(
							this.process);

					m_propertiesPanel
							.initializeProcessPropertiesPanel(m_processPropertiesPanel
									.getInstance());
				}

				/**
				 *
				 */
				Diagram.prototype.moveSelectedSymbolsBy = function(dX, dY) {
					for ( var n in this.currentSelection) {
						this.currentSelection[n].moveBy(dX, dY);
					}
				};

				/**
				 *
				 */
				Diagram.prototype.showEditable = function(textPrimitive) {
					this.currentTextPrimitive = textPrimitive;
					var scrollPos = m_modelerUtils.getModelerScrollPosition();
					// Use the Symbol's x co-ordinate to decide the width of
					// textbox
					var textboxWidth = textPrimitive.auxiliaryProperties.callbackScope.width
							- WIDTH_ADJUSTMENT;
					m_utils.debug("text primitive set");
					this.editableText.css("width", parseInt(textboxWidth
							.valueOf()));
					this.editableText
							.css("visibility", "visible")
							.html(textPrimitive.attr("text"))
							.moveDiv(
									{
										"x" : textPrimitive.auxiliaryProperties.callbackScope.x
												+ X_OFFSET + textPrimitive.auxiliaryProperties.callbackScope.width/5 - scrollPos.left,
										"y" : textPrimitive.auxiliaryProperties.callbackScope.y
												+ Y_OFFSET + textPrimitive.auxiliaryProperties.callbackScope.height/3 -scrollPos.top
									}).show().trigger("dblclick");
					this.symbolEditMode = true;
					m_utils.debug("editable activated");
				};

				/**
				 *
				 */
				Diagram.prototype.submitEditable = function(content) {
					if (content == '') {
						this.cancelEditable();
					} else {
						this.editableText.css("visibility", "hidden").hide()
								.trigger("blur");
						this.currentTextPrimitive.attr("text", content);
						m_utils.debug("text set");
						var changes = {
							modelElement : {
								name : this.currentTextPrimitive.attr("text")
							}
						};

						m_commandsController
								.submitCommand(m_command
										.createUpdateModelElementCommand(
												this.currentTextPrimitive.auxiliaryProperties.callbackScope.diagram.modelId,
												this.currentTextPrimitive.auxiliaryProperties.callbackScope.modelElement.oid,
												changes));
						this.currentTextPrimitive.show();
						this.symbolEditMode = false;
						m_utils.debug("text primitive shown");
					}
				};

				/**
				 *
				 */
				Diagram.prototype.cancelEditable = function() {
					this.editableText.css("visibility", "hidden").hide()
							.trigger("blur");
					this.currentTextPrimitive.show();
					this.symbolEditMode = false;
					m_utils.debug("text primitive hidden");
				};

				/**
				 *
				 */
				Diagram.prototype.zoomIn = function() {
					this.zoomFactor = Math.max(this.zoomFactor
							- m_constants.ZOOM_INCREMENT, 1);

					m_canvasManager.setViewBox(this.scrollPane.scrollLeft(),
							this.scrollPane.scrollTop(), this.zoomFactor);

					// Only zoom viewbox

					m_canvasManager.setCanvasSize(this.width / this.zoomFactor,
							this.height / this.zoomFactor);
				};

				/**
				 *
				 */
				Diagram.prototype.zoomOut = function() {
					this.zoomFactor = this.zoomFactor
							+ m_constants.ZOOM_INCREMENT;

					m_canvasManager.setViewBox(this.scrollPane.scrollLeft(),
							this.scrollPane.scrollTop(), this.zoomFactor);

					// Only zoom viewbox

					m_canvasManager.setCanvasSize(this.width / this.zoomFactor,
							this.height / this.zoomFactor);
				};

				/**
				 *
				 */
				Diagram.prototype.loadProcess = function() {
					m_communicationController.syncGetData({
						url : this.getEndpointUrl() + "/models/" + this.modelId
								+ "/process/" + this.processId + "/loadModel",
						callbackScope : this
					}, new function() {
						return {
							success : function(json) {
								this.callbackScope.loadFromJson(json);

								// resetState();
							},
							failure : function() {
								alert('Hey');
							}
						};
					});
				};

				/**
				 *
				 */
				Diagram.prototype.loadFromJson = function(json) {
					m_utils.debug("===> Process/Diagram JSON");
					m_utils.debug(json);

					// Create pools and lanes

					// TODO Multiple pool symbols

					for ( var n in json.poolSymbols) {
						m_poolSymbol.createPoolSymbolFromJson(this,
								json.poolSymbols[n]);
					}

					if (this.poolSymbol == null) {
						m_messageDisplay
								.showErrorMessage("Process diagram does not contain any pools. Possibly not well-formed BPMN.");
					}

					this.setSize(this.poolSymbol.width, this.poolSymbol.height);
					this.flowOrientation = this.poolSymbol.orientation;

					m_utils.debug("===> Diagram JSON");
					m_utils.debug(this);

					// Create connections

					for ( var n in json.connections) {
						m_connection.createConnectionFromJson(this,
								json.connections[n]);
					}

					m_processPropertiesPanel.getInstance().setElement(
							this.process);
				};

				/**
				 *
				 */
				Diagram.prototype.setSize = function(width, height) {
					this.width = width;
					this.height = height;
					this.background.attr({
						"width" : width,
						"height" : height
					});
					m_canvasManager.setCanvasSize(width, height);
				};

				/**
				 *
				 */
				Diagram.prototype.applyDecoration = function(decoration) {
					for ( var decorationElement in decoration.elements) {
						for ( var symbol in this.symbols) {
							if ((decoration.elements[decorationElement].type != null && decoration.elements[decorationElement].type == this.symbols[symbol].type)
									|| ((this.symbols[symbol].id != null && this.symbols[symbol].id == decoration.elements[decorationElement].id) || (this.symbols[symbol].modelElement != null
											&& this.symbols[symbol].modelElement.id != null && this.symbols[symbol].modelElement.id == decoration.elements[decorationElement].id))
									|| (decoration.elements[decorationElement].oid != null && decoration.elements[decorationElement].oid == this.symbols[symbol].oid)) {
								if (decoration.elements[decorationElement]["graphicsDecoration"] != null) {
									for ( var primitive in decoration.elements[decorationElement]["graphicsDecoration"]) {
										this.symbols[symbol][primitive]
												.attr(decoration.elements[decorationElement]["graphicsDecoration"][primitive]);
									}
								}
								if (decoration.elements[decorationElement]["dashboardContent"] != null) {
									this.symbols[symbol]
											.showDashboard(decoration.elements[decorationElement]["dashboardContent"]);
								}
							}
						}

						for ( var connection in this.connections) {
							if ((decoration.elements[decorationElement].oid != null && decoration.elements[decorationElement].oid == this.connections[connection].oid)) {
								if (decoration.elements[decorationElement]["graphicsDecoration"] != null) {
									for ( var primitive in decoration.elements[decorationElement]["graphicsDecoration"]) {
										this.connections[connection][primitive]
												.attr(decoration.elements[decorationElement]["graphicsDecoration"][primitive]);
									}
								}
								if (decoration.elements[decorationElement]["dashboardContent"] != null) {
									this.connections[connection]
											.showDashboard(decoration.elements[decorationElement]["dashboardContent"]);
								}
							}
						}

					}
				};

				/**
				 * TODO Is this really needed?
				 */
				Diagram.prototype.getAllDataSymbols = function() {
					var dataSymbols = [];

					return this.poolSymbol.getAllDataSymbols(dataSymbols);
				};

				/**
				 *
				 */
				Diagram.prototype.findLane = function(id) {
					return this.poolSymbol.findLane(id);
				};
			}

			function Diagram_clickClosure(event) {
				this.auxiliaryProperties.diagram.onClick(event.pageX
						- X_OFFSET
						+ this.auxiliaryProperties.diagram.scrollPane
								.scrollLeft(), event.pageY
						- Y_OFFSET
						+ this.auxiliaryProperties.diagram.scrollPane
								.scrollTop());
			}

			function Diagram_mouseDownClosure(event) {
				this.auxiliaryProperties.diagram.onMouseDown(event.pageX
						- X_OFFSET
						+ this.auxiliaryProperties.diagram.scrollPane
								.scrollLeft(), event.pageY
						- Y_OFFSET
						+ this.auxiliaryProperties.diagram.scrollPane
								.scrollTop());
			}

			function Diagram_mouseMoveClosure(event) {
				this.auxiliaryProperties.diagram.onMouseMove(event.pageX
						- X_OFFSET
						+ this.auxiliaryProperties.diagram.scrollPane
								.scrollLeft(), event.pageY
						- Y_OFFSET
						+ this.auxiliaryProperties.diagram.scrollPane
								.scrollTop());
			}

			function Diagram_mouseUpClosure(event) {
				this.auxiliaryProperties.diagram.onMouseUp(event.pageX
						- X_OFFSET
						+ this.auxiliaryProperties.diagram.scrollPane
								.scrollLeft(), event.pageY
						- Y_OFFSET
						+ this.auxiliaryProperties.diagram.scrollPane
								.scrollTop());
			}
		});
