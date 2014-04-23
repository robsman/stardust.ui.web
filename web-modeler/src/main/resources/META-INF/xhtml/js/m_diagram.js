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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_extensionManager",
				"bpm-modeler/js/m_urlUtils",
				"bpm-modeler/js/m_communicationController",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_session",
				"bpm-modeler/js/m_canvasManager",
				"bpm-modeler/js/m_messageDisplay", "bpm-modeler/js/m_symbol",
				"bpm-modeler/js/m_poolSymbol",
				"bpm-modeler/js/m_activitySymbol",
				"bpm-modeler/js/m_dataSymbol", "bpm-modeler/js/m_eventSymbol",
				"bpm-modeler/js/m_gatewaySymbol",
				"bpm-modeler/js/m_swimlaneSymbol",
				"bpm-modeler/js/m_connection",
				"bpm-modeler/js/m_propertiesPanel",
				"bpm-modeler/js/m_processPropertiesPanel",
				"bpm-modeler/js/m_activityPropertiesPanel",
				"bpm-modeler/js/m_dataPropertiesPanel",
				"bpm-modeler/js/m_eventPropertiesPanel",
				"bpm-modeler/js/m_gatewayPropertiesPanel",
				"bpm-modeler/js/m_annotationPropertiesPanel",
				"bpm-modeler/js/m_swimlanePropertiesPanel",
				"bpm-modeler/js/m_controlFlowPropertiesPanel",
				"bpm-modeler/js/m_dataFlowPropertiesPanel",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_process",
				"bpm-modeler/js/m_data", "bpm-modeler/js/m_modelerUtils",
				"bpm-modeler/js/m_autoScrollManager",
				"bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_extensionManager, m_urlUtils,
				m_communicationController, m_commandsController, m_command,
				m_session, m_canvasManager, m_messageDisplay, m_symbol,
				m_poolSymbol, m_activitySymbol, m_dataSymbol, m_eventSymbol,
				m_gatewaySymbol, m_swimlaneSymbol, m_connection,
				m_propertiesPanel, m_processPropertiesPanel,
				m_activityPropertiesPanel, m_dataPropertiesPanel,
				m_eventPropertiesPanel, m_gatewayPropertiesPanel,
				m_annotationPropertiesPanel, m_swimlanePropertiesPanel,
				m_controlFlowPropertiesPanel, m_dataFlowPropertiesPanel,
				m_model, m_process, m_data, m_modelerUtils, m_autoScrollManager, m_i18nUtils) {

			//var X_OFFSET; // Set fpr #panningSensor
			//var Y_OFFSET; // Set for #toolbar +

			// #messageDisplay
			// Adjustments for Editable Text on Symbol

			return {
				createDiagram : function(divId, canvasManager) {
					return new Diagram(divId, canvasManager);
				}
			};

//			var currentDiagram = null;
//			var panningIntervalId = null;
//			var symbolEditMode = false;

			/**
			 *
			 */
			function Diagram(newDivId, canvasManager) {
				//currentDiagram = this;

				var canvasPos = m_utils.jQuerySelect("#" + newDivId).position();
				//X_OFFSET = canvasPos.left; // Set fpr #panningSensor
				//Y_OFFSET = canvasPos.top; // Set for #toolbar +

				// Constants

				var SNAP_LINE_THRESHOLD = 15;

				// Public constants
				this.aTempId = Math.floor(Math.random() * 1000);

				this.canvasManager = canvasManager;
				this.oid = 0;
				this.NORMAL_MODE = "NORMAL_MODE";
				this.RUBBERBAND_MODE = "RUBBERBAND_MODE";
				this.CONNECTION_MODE = "CONNECTION_MODE";
				this.SYMBOL_MOVE_MODE = "SYMBOL_MOVE_MODE";
				this.SEPARATOR_MODE = "SEPARATOR_MODE";
				this.CREATE_MODE = "CREATE_MODE";
				//this.X_OFFSET = X_OFFSET;
				//this.Y_OFFSET = Y_OFFSET;
				this.width = this.canvasManager.getCanvasWidth();
				this.height = this.canvasManager.getCanvasHeight();
				this.flowOrientation = m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL;
				this.zoomFactor = 1;
				this.divId = newDivId;
				this.modelId = null;
				this.processId = null;
				this.process = null;
				this.mode = this.NORMAL_MODE;
				this.symbols = [];
				this.dragEnabled = null;
				this.anchorDragEnabled = null;

				// Activity symbols by OIDs

				this.activitySymbols = {};

				// Gateway symbols by OIDs

				this.gatewaySymbols = {};

				// Event symbols by OIDs

				this.eventSymbols = {};

				// Data symbols by OIDs

				this.dataSymbols = {};

				// Data symbols by OIDs

				this.annotationSymbols = {};
				this.connections = [];
				this.currentSelection = [];
				this.currentConnection = null;
				this.newSymbol = null;
				this.currentFlyOutSymbol = null;
				// Caches the last newly created Symbol for server callbacks
				this.newSymbol = null;
				this.animationDelay = 0;
				this.animationEasing = null;
				this.symbolGlow = true;

				this.background = this.canvasManager.drawRectangle(0, 0,
						this.canvasManager.getCanvasWidth(), this.canvasManager
								.getCanvasHeight(), {
							"stroke-width" : 0,
							"fill" : "white"
						});

				this.background.auxiliaryProperties = {
					diagram : this
				};
				
				//All Properties pages pertaining to this instance
				this.processPropertiesPanel = null;
				this.activityPropertiesPanel = null;
				this.dataPropertiesPanel = null;
				this.eventPropertiesPanel = null;
				this.gatewayPropertiesPanel = null;
				this.annotationPropertiesPanel = null;
				this.swimlanePropertiesPanel = null;
				this.controlFlowPropertiesPanel = null;
				this.dataFlowPropertiesPanel = null;
				
				//Tooltip
				this.applicationActivityTooltip = m_utils.jQuerySelect("#applicationActivityTooltip");

				// Exclude Click from readonly check to show properties panel
				this.background.click(Diagram_clickClosure);

				// Register with Command Controller

				m_commandsController.registerCommandHandler(this);

				// Bind DOM elements

				// === Start Toolbar ===

				var toolbarPalettes = m_extensionManager
						.findExtensions("diagramToolbarPalette");

				var paletteTableRow = m_utils.jQuerySelect("#diagramToolbarTable #paletteRow");

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

					var entryRow = m_utils.jQuerySelect("#diagramToolbarTable #paletteRow #"
							+ toolbarPalettes[n].id + "EntryRow");

					if (toolbarPalettes[n].contentHtmlUrl != null) {
						var extension = toolbarPalettes[n];
						var dummy = this;

						m_utils.jQuerySelect(
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

												m_utils.jQuerySelect(
														"#"
																+ panel.id
																+ " #"
																+ extension.pageId)
														.append(msg);
												m_utils.debug(msg);
											} else {
												extension.provider
														.create(dummy);
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
									+ "\" class=\"toolbarButton"
									+ (paletteEntries[m].styleClass ? (" " + paletteEntries[m].styleClass) : "")
									+ "\" /></td>");

							m_utils.jQuerySelect(
									"#diagramToolbarTable #paletteRow #"
											+ toolbarPalettes[n].id
											+ "EntryRow #"
											+ paletteEntries[m].id)
									.click(
											{
												diagram : this,
												handler : paletteEntries[m].id,
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

				var selectModeButton = m_utils.jQuerySelect("#selectModeButton");
				
				selectModeButton.click({
					"diagram" : this
				}, function(event) {
					event.data.diagram.setSelectMode();
				});

				var separatorModeButton = m_utils.jQuerySelect("#separatorModeButton");

				separatorModeButton.click({
					"diagram" : this
				}, function(event) {
					event.data.diagram.setSeparatorMode();
				});

				// === End Toolbar

				this.canvas = m_utils.jQuerySelect('#' + this.divId);
				this.scrollPane = m_utils.jQuerySelect("#scrollpane");


				// dirty workaround - only chrome being triggerring 'blur' event on clicking scrollbars
				// resetForm and submit form conflicts in this case
				var clickedOnScrollBar = false;
				this.scrollPane.mousedown({
					"diagram" : this
				}, function(event) {
					if (m_utils.isBrowserChrome()) {
						event.data.diagram.clickedOnScrollBar = true;
					}
				});

				this.scrollPane.scroll({
					"diagram" : this
				}, function(event) {
					if (event.data.diagram.clickedOnScrollBar == false) {
						event.data.diagram.resetEditableText();
					}
				});

				// Define event handling for DOM elements

				this.canvas.mousedown({
					"diagram" : this
				},
						function(event) {
							event.data.diagram
									.onGlobalMouseDown(event.pageX
											- event.data.diagram.getCanvasPosition().left, event.pageY
											- event.data.diagram.getCanvasPosition().top);
						});

				this.canvas.mousemove({
					"diagram" : this
				},
						function(event) {
							event.data.diagram
									.onGlobalMouseMove(event.pageX
											- event.data.diagram.getCanvasPosition().left, event.pageY
											- event.data.diagram.getCanvasPosition().top);
						});

				this.canvas.mouseup({
					"diagram" : this
				}, function(event) {
					event.data.diagram
							.onGlobalMouseUp(event.pageX - event.data.diagram.getCanvasPosition().left,
									event.pageX
											- event.data.diagram.getCanvasPosition().left, event.pageY
											- event.data.diagram.getCanvasPosition().top);
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
				this.horizontalSnapLine = this.canvasManager.drawPath("", {
					"stroke" : m_constants.SNAP_LINE_COLOR,
					"stroke-width" : m_constants.SNAP_LINE_STROKE_WIDTH,
					'stroke-dasharray' : m_constants.SNAP_LINE_DASHARRAY
				});
				this.verticalSnapLinePosition = this.width * 0.5;
				this.isVerticalSnap = false;
				this.verticalSnapLine = this.canvasManager.drawPath("", {
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
				this.horizontalSeparatorLine = this.canvasManager.drawPath("", {
					"stroke" : m_constants.SEPARATOR_LINE_COLOR,
					"stroke-width" : m_constants.SEPARATOR_LINE_STROKE_WIDTH,
					'stroke-dasharray' : m_constants.SEPARATOR_LINE_DASHARRAY
				});
				this.verticalSeparatorLine = this.canvasManager.drawPath("", {
					"stroke" : m_constants.SEPARATOR_LINE_COLOR,
					"stroke-width" : m_constants.SEPARATOR_LINE_STROKE_WIDTH,
					'stroke-dasharray' : m_constants.SEPARATOR_LINE_DASHARRAY
				});

				this.rubberBand = this.canvasManager.drawRectangle(0, 0, 0, 0, {
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

				var self = this;
				this.editableText = m_utils.jQuerySelect("#editable")
						.editable(
								function(value, settings) {
									return value;
								},
								{
									type : "text",
									event : "dblclick",
									placeholder : "",
									onblur : "submit",
									select: "true",
									onreset : function(settings, value) { // On
										// Reset
										// hide
										// the
										// text box and reset
										// the value
										self.cancelEditable();
									},
									onsubmit : function(settings, value) {
										self.submitEditable(
														m_utils.jQuerySelect('input', this).val());
									}
								}).css("font-family",
								m_constants.DEFAULT_FONT_FAMILY).css(
								"font-size", m_constants.DEFAULT_FONT_SIZE);

				this.editableTextArea = m_utils.jQuerySelect("#editableArea").editable(
						function(value, settings) {
							return value;
						},
						{
							type : "textarea",
							event : "dblclick",
							placeholder : "",
							onblur : "submit",
							select: "true",
							onreset : function(settings, value) { // On
								// Reset
								// hide
								// the
								// text box and reset
								// the value
								self.cancelEditableArea();
							},
							onsubmit : function(settings, value) {
								self.submitEditableArea(
												m_utils.jQuerySelect('textarea', this).val());
							}
						}).css("font-family", m_constants.DEFAULT_FONT_FAMILY)
						.css("font-size", m_constants.DEFAULT_FONT_SIZE);

				this.currentTextPrimitive = null;
				this.poolSymbol = null;
				this.poolSymbols = {};

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
					// TODO Bind against loaded models
					var self = this;

					// Refresh properties panel on view activation
					this.onViewActivate = function(params) {
						if (params && params === self.process.uuid) {
							// Executes a timeout loop with 50ms timeout and maximum 20 repetitions
							// checks if the activated view has actually been dispalyed (display: block)
							// before re-initializing the properties panel 
							m_utils.executeTimeoutLoop(function() {
								self.clearCurrentSelection();
							}, 20, 50, function() {
								return "block" == self.canvas.parents("[ng-repeat='panel in panels']").css("display")	
							});
						} else {
							this.lastSymbol = null;
						}
					};
					
					this.onViewPinned = function(pinned) {
						require("bpm-modeler/js/m_modelerViewLayoutManager").adjustPanels();
					};
					
					this.onViewClose = function(params) {
						if (params && params === self.process.uuid) {
							EventHub.events.unsubscribe("PEPPER_VIEW_ACTIVATED", self.onViewActivate);
							EventHub.events.unsubscribe("PEPPER_VIEW_CLOSED", self.onViewClose);
							EventHub.events.unsubscribe("SIDEBAR_PINNED", self.onViewPinned);
						}						
					};
					
					EventHub.events.subscribe("PEPPER_VIEW_ACTIVATED", self.onViewActivate);
					EventHub.events.subscribe("PEPPER_VIEW_CLOSED", self.onViewClose);					
					EventHub.events.subscribe("SIDEBAR_PINNED", self.onViewPinned);

					this.modelId = BridgeUtils.View.getActiveViewParams().param(
							"modelId");
					this.processId = BridgeUtils.View.getActiveViewParams().param(
							"processId");
					this.model = m_model.findModel(this.modelId);

					// TODO - this is a temporary workaround. This will have to
					// be replaced with
					// a solution where we can refresh / reload models from
					// server side
					if (!this.model) {
						m_model.loadModels(true);
						this.model = m_model.findModel(this.modelId);
						window.parent.EventHub.events.publish("RELOAD_MODELS");
					}

					this.process = this.model.processes[this.processId];
					this.process.diagram = this;

					// Initialize Properties Panels
					this.initializePropertiesPanels();
					this.showProcessPropertiesPanel();
					
					var currentDiagram = this;
					m_autoScrollManager
							.initScrollManager(
									"scrollpane",
									function() {
										var inAutoScrollMode = false;
										if (null != currentDiagram.newSymbol
												&& (currentDiagram.newSymbol
														.isPoolSymbol() || currentDiagram.newSymbol.type == m_constants.SWIMLANE_SYMBOL)) {
											// For Default Pool drag and drop is
											// not allowed.
											inAutoScrollMode = false;
										} else if (true == currentDiagram
												.isInConnectionMode()
												|| currentDiagram.mode == currentDiagram.SYMBOL_MOVE_MODE
												|| null != currentDiagram.newSymbol
												|| true == currentDiagram
														.isDragAndDropMode()) {
											inAutoScrollMode = true;
										} else {
											inAutoScrollMode = false;
										}

										return inAutoScrollMode;
									},
									function(event) {
										if (null != currentDiagram.newSymbol
												|| currentDiagram
														.isInConnectionMode()) {
											currentDiagram
													.onGlobalMouseMove(
															event.pageX
																	- currentDiagram.getCanvasPosition().left,
															event.pageY
																	- currentDiagram.getCanvasPosition().top);
										} else if (currentDiagram.currentSelection.length > 0) {
											for ( var i in currentDiagram.currentSelection) {
												if (currentDiagram.currentSelection[i]
														.type == m_constants.CONTROL_FLOW) {
													// TODO - The connnection
													// should stick with
													// mouse pointer
													// in case of auto-scroll in
													// case of a
													// re-route
												} else {
													currentDiagram.currentSelection[i]
															.move(event.pageX, event.pageY, true);
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
							+ "/services/rest/bpm-modeler/modeler/"
							+ new Date().getTime();
				};

				/**
				 *
				 */
				Diagram.prototype.initializePropertiesPanels = function() {
					this.processPropertiesPanel = m_processPropertiesPanel.initialize(this, this.process);
					this.activityPropertiesPanel = m_activityPropertiesPanel.initialize(this);
					this.dataPropertiesPanel = m_dataPropertiesPanel.initialize(this);
					this.eventPropertiesPanel = m_eventPropertiesPanel.initialize(this);
					this.gatewayPropertiesPanel = m_gatewayPropertiesPanel.initialize(this);
					this.annotationPropertiesPanel = m_annotationPropertiesPanel.initialize(this);
					this.swimlanePropertiesPanel = m_swimlanePropertiesPanel.initialize(this);
					this.controlFlowPropertiesPanel = m_controlFlowPropertiesPanel.initialize(this);
					this.dataFlowPropertiesPanel = m_dataFlowPropertiesPanel.initialize(this);
				};

				/**
				 *
				 */
				Diagram.prototype.createTransferObject = function() {
					var transferObject = {};

					// Copy and prepare children

					transferObject.poolSymbols = {};

					for ( var id in this.poolSymbols) {
						transferObject.poolSymbols[id] = this.poolSymbols[id]
								.createTransferObject();
					}

					return transferObject;
				};

				/**
				 * Find symbol in current model by OID
				 */
				Diagram.prototype.findSymbolByGuid = function(guid, modelId) {

					if (null != guid) {
						for ( var i = 0; i < this.symbols.length; i++) {
							if (this.symbols[i].diagram.modelId == modelId
									&& (this.symbols[i].oid == guid)) {
								return this.symbols[i];
							}
						}
					}

					return null;
				};

				/**
				 * Find symbol in current model by ModelElement OID
				 */
				Diagram.prototype.findSymbolByModelElementGuid = function(guid,
						modelId) {

					if (null != guid) {
						for ( var i = 0; i < this.symbols.length; i++) {
							if (this.symbols[i].diagram.modelId == modelId
									&& (this.symbols[i].modelElement != null && this.symbols[i].modelElement.oid == guid)) {
								return this.symbols[i];
							} else if (this.symbols[i].diagram.modelId == modelId
									&& m_constants.ANNOTATION_SYMBOL == this.symbols[i].type
									&& this.symbols[i].oid == guid) {
								return this.symbols[i];
							}
						}
					}

					return null;
				};

				/**
				 * Find connection in current model by fromSymbolOID and
				 * toSymbolOID
				 */
				Diagram.prototype.findConnection = function(conn, modelId) {

					for ( var i = 0; i < this.connections.length; i++) {
						if (this.connections[i].diagram.modelId == modelId
								&& (this.connections[i].fromModelElementOid == conn.fromModelElementOid && this.connections[i].toModelElementOid == conn.toModelElementOid)) {
							// while adding a connection, to update connection
							// connection is searched using modelELementOid
							return this.connections[i];
						}
					}

					return null;
				};

				/**
				 * Find connection in current model by OID
				 */
				Diagram.prototype.findConnectionByGuid = function(guid, modelId) {

					if (null != guid) {
						for ( var i = 0; i < this.connections.length; i++) {
							if ((this.connections[i].diagram.modelId == modelId)
									&& this.connections[i].oid == guid) {
								return this.connections[i];
							}
						}
					}
					return null;
				};

				/**
				 * Find connection in current model by
				 * ModelElement(controlFlow/DataFlow) OID
				 */
				Diagram.prototype.findConnectionByModelElementGuid = function(
						guid, modelId) {

					if (null != guid) {
						for ( var i = 0; i < this.connections.length; i++) {
							if ((this.connections[i].diagram.modelId == modelId)
									&& this.connections[i].modelElement.oid == guid) {
								return this.connections[i];
							}
						}
					}
					return null;
				};

				Diagram.prototype.registerSymbol = function(symbol) {
					if (symbol && symbol.register) {
						symbol.register();
					}
				}

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

						// Delete removed elements
						for ( var i = 0; i < obj.changes.removed.length; i++) {
							var symbol = this
									.findSymbolByGuid(
											obj.changes.removed[i].oid,
											command.modelId);
							if (null == symbol) {
								symbol = this.findSymbolByModelElementGuid(
										obj.changes.removed[i].oid,
										command.modelId);
							}
							if (null == symbol) {
								if (obj.changes.removed[i].type
										&& obj.changes.removed[i].type != m_constants.CONTROL_FLOW) {
									symbol = this.findConnectionByGuid(
											obj.changes.removed[i].oid,
											command.modelId);
								}
							}
							if (null != symbol) {
								symbol.remove();
								this.resetLastSymbol(symbol.oid);
							}

						}

						// Run the added loop and create any swimlanes before
						// creating other elements
						// particularly useful in case of UNDO of swimlane
						// delete
						for ( var i = 0; i < obj.changes.added.length; i++) {
							if (obj.changes.added[i].type == m_constants.SWIMLANE_SYMBOL) {
								if ((null != this.lastSymbol && null != obj.changes.added[i].type)
										&& obj.changes.added[i].type
												.match(this.lastSymbol.type)) {
									this.lastSymbol
											.applyChanges(obj.changes.added[i]);
									this.lastSymbol.refresh();
									this.registerSymbol(this.lastSymbol);
									this.lastSymbol = null;
								} else if (command.isUndo || command.isRedo) {
									if (null == this.findSymbolByGuid(
											obj.changes.added[i].oid,
											command.modelId)) {
										this.poolSymbol.laneSymbols
												.push(m_swimlaneSymbol
														.createSwimlaneSymbolFromJson(
																this,
																this.poolSymbol,
																obj.changes.added[i]));
										this.poolSymbol.sortLanes();
										this.poolSymbol.adjustChildSymbols();
									}
								}
							}
						}

						// Run the added loop to add all data symbols except
						// connections and swimlane
						// For connections the loop is run again - to make sure
						// all connected symbols are
						// created already
						for ( var i = 0; i < obj.changes.added.length; i++) {
							if (!(obj.changes.added[i].type == m_constants.CONTROL_FLOW_CONNECTION
									|| obj.changes.added[i].type == m_constants.DATA_FLOW_CONNECTION
									|| obj.changes.added[i].type == m_constants.CONTROL_FLOW
									|| obj.changes.added[i].type == m_constants.DATA_FLOW || obj.changes.added[i].type == m_constants.SWIMLANE_SYMBOL)) {
								if ((null != this.lastSymbol && null != obj.changes.added[i].type)
										&& obj.changes.added[i].type
												.match(this.lastSymbol.type)) {
									this.lastSymbol
											.applyChanges(obj.changes.added[i]);
									this.lastSymbol.refresh();
									this.registerSymbol(this.lastSymbol);
									this.lastSymbol = null;
								}// For connections lastSymbol will be empty
								else if (command.isUndo || command.isRedo) {
									// Else block is executed in case of undo /
									// redo
									// Find swimlane from modified array or
									// added
									var swimlane;
									for ( var j = 0; j < obj.changes.modified.length; j++) {
										if (obj.changes.modified[j].type == m_constants.SWIMLANE_SYMBOL) {
											swimlane = obj.changes.modified[j];
											break;
										}
									}
									for ( var j = 0; j < obj.changes.added.length; j++) {
										if (obj.changes.added[j].type == m_constants.SWIMLANE_SYMBOL) {
											swimlane = obj.changes.added[j];
											break;
										}
									}

									if (swimlane) {
										swimlane = this.findSymbolByGuid(
												swimlane.oid, command.modelId);
									} else {
										// Swimlane delete undo scenario
										for ( var j = 0; j < obj.changes.added.length; j++) {
											if (obj.changes.added[j].type == m_constants.SWIMLANE_SYMBOL) {
												swimlane = m_swimlaneSymbol
														.createSwimlaneSymbolFromJson(
																this,
																this.poolSymbol,
																obj.changes.added[j]);
												this.poolSymbol.laneSymbols
														.push(swimlane);
												this.poolSymbol.sortLanes();
												this.poolSymbol
														.adjustChildSymbols();
											}
										}
									}

									if (swimlane) {
										// Attach prototype object
										obj.changes.added[i].prototype = {};
										if (obj.changes.added[i].type == m_constants.ACTIVITY_SYMBOL) {
											m_activitySymbol
													.createActivitySymbolFromJson(
															this,
															swimlane,
															obj.changes.added[i]);
											this.lastSymbol = null;
										} else if (obj.changes.added[i].type == m_constants.GATEWAY_SYMBOL) {
											m_gatewaySymbol
													.createGatewaySymbolFromJson(
															this,
															swimlane,
															obj.changes.added[i])
											this.lastSymbol = null;
										} else if (obj.changes.added[i].type == m_constants.EVENT_SYMBOL) {
											m_eventSymbol
													.createEventSymbolFromJson(
															this,
															swimlane,
															obj.changes.added[i])
											this.lastSymbol = null;
										} else if (obj.changes.added[i].type == m_constants.DATA_SYMBOL) {
											m_dataSymbol
													.createDataSymbolFromJson(
															this,
															swimlane,
															obj.changes.added[i]);
											this.lastSymbol = null;
										}
									}
								}
							}
						}

						// Run the added loop again to add data connections
						for ( var i = 0; i < obj.changes.added.length; i++) {
							if (obj.changes.added[i].type == m_constants.CONTROL_FLOW_CONNECTION
									|| obj.changes.added[i].type == m_constants.DATA_FLOW_CONNECTION
									|| obj.changes.added[i].type == m_constants.CONTROL_FLOW
									|| obj.changes.added[i].type == m_constants.DATA_FLOW) {
								var conn = this.findConnection(
										obj.changes.added[i], command.modelId);
								if (null != conn) {
									conn.applyChanges(obj.changes.added[i]);
									conn.refresh();
								} else if ((command.isUndo || command.isRedo)
										&& (obj.changes.added[i].type == m_constants.CONTROL_FLOW_CONNECTION || obj.changes.added[i].type == m_constants.DATA_FLOW_CONNECTION)) {
									m_connection.createConnectionFromJson(this,
											obj.changes.added[i]);
								}
							}
						}

						// Apply changes

						// this.animationDelay = 1000;
						// this.animationEasing = "<";

						for ( var i = 0; i < obj.changes.modified.length; i++) {
							var symbol = this.findSymbolByGuid(
									obj.changes.modified[i].oid,
									command.modelId);

							if (symbol != null) {
								m_utils.debug("Up to changed symbol:");
								m_utils.debug(symbol);

								symbol.lastModifyingUser = command.account; // m_session.getUserByAccount(command.account);

								symbol.applyChanges(obj.changes.modified[i]);
								m_utils.debug("Changed symbol to:");
								m_utils.debug(symbol);
								symbol.refresh();
								if (symbol.type == m_constants.SWIMLANE_SYMBOL) {
									// When swimlane co-ordinates change in
									// Undo/Redo,
									// PoolSymbol needs adjustment.
									symbol.parentSymbol
											.recalculateBoundingBox();
									symbol.parentSymbol.adjustPrimitives();
									this.poolSymbol.adjustChildSymbols();
								}
								this.resetLastSymbol(symbol.oid);
							}

							symbol = this.findSymbolByModelElementGuid(
									obj.changes.modified[i].oid,
									command.modelId);

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
								//symbol.refresh();
								// On ModelElement update, update only the model
								// of symbol
								symbol.refreshFromModelElement();
								symbol.refreshCommentPrimitives();
								this.resetLastSymbol(symbol.oid);
								// TODO - update properties panel on
								// modelElement change
							}

							// Check if connection is modified
							var conn = this.findConnectionByGuid(
									obj.changes.modified[i].oid,
									command.modelId);

							if (null != conn) {
								if ((command.isUndo || command.isRedo)
										&& conn.isDataFlow()) {
									// When dataMapping changes in Undo/Redo, only checked mapping is available in
									// change array, Update the dataMapping for unchecked dataMapping
									if (obj.changes.modified[i].modelElement.inputDataMapping == null
											&& conn.modelElement.inputDataMapping != null) {
										conn.modelElement.inputDataMapping = null;
									} else if (obj.changes.modified[i].modelElement.outputDataMapping == null
											&& conn.modelElement.outputDataMapping != null) {
										conn.modelElement.outputDataMapping = null;
									}
								}
								conn.applyChanges(obj.changes.modified[i]);
								// TODO: commented as flip orientation was not
								// working, not sure why we require this...
								// conn.initializeAnchorPoints();
								conn.reroute();
								conn.refresh();
							} else {
								conn = this.findConnectionByModelElementGuid(
										obj.changes.modified[i].oid,
										command.modelId);
								if (null != conn) {
									m_utils.inheritFields(conn.modelElement,
											obj.changes.modified[i]);
									conn.refresh();
								}
							}
						}

						this.poolSymbol.refreshDiagram();
						this.animationDelay = 0;
						this.animationEasing = null;
					}
				};

				/**
				 * Find an Activity Symbol by the Id of the corresponding Activity.
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
				Diagram.prototype.resetLastSymbol = function(oid) {
					if (oid && this.lastSymbol != null
							&& this.lastSymbol.oid == oid) {
						this.lastSymbol = null;
					}
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
							this, m_constants.TASK_ACTIVITY_TYPE);
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
							//new symbol needs to be added to currentSelection,
							//m_symbol#moveBy method is common for drag n drop and new symbol drag function
							this.clearCurrentSelection();
							this.newSymbol.move(x * this.zoomFactor, y
									* this.zoomFactor);
							// When creating symbol from flyoutMenu,
							// connection anchorPoint should be intellegently
							// changed
							if (this.currentConnection != null) {
								this.currentConnection
										.updateAnchorPointForSymbol();
							}
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

							if (this.currentConnection.toAnchorPoint.symbol == null
									|| (this.currentConnection.toAnchorPoint.symbol.type == m_constants.SWIMLANE_SYMBOL)) {
								// when connection moves with
								// toAnchorPoint.symbol as
								// swimlane , move is allowed on connection
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
				 * Returns the first symbol which is not a container symbol
				 * (pool, lane), overlapping with <code>symbol</code>.
				 */
				Diagram.prototype.getSymbolOverlappingWithSymbol = function(
						symbol) {
					for ( var n in this.symbols) {
						if (!this.symbols[n].isContainerSymbol()
								&& this.symbols[n] != symbol
								&& (this.symbols[n].isInBoundingBox(symbol.x,
										symbol.y)
										|| this.symbols[n].isInBoundingBox(
												symbol.x + symbol.width,
												symbol.y)
										|| this.symbols[n].isInBoundingBox(
												symbol.x, symbol.y
														+ symbol.height) || this.symbols[n]
										.isInBoundingBox(symbol.x
												+ symbol.width, symbol.y
												+ symbol.height))) {
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
					this.horizontalSnapLine.hide();
					this.isHorizontalSnap = false;

					if(!symbol.supportSnapping()){
						return;
					}

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
							var newSymbol = this.placeNewSymbol(x - this.getCanvasPosition().left,
									y - this.getCanvasPosition().top, true);
							if (newSymbol) {
								this.currentConnection.toModelElementOid = newSymbol.oid;
								this.currentConnection.updateAnchorPointForSymbol();
								this.currentConnection.complete();
							}
  							else {
								this.currentConnection.remove();
								this.currentConnection = null;
							}

						} else {
							this.placeNewSymbol(x * this.zoomFactor, y
									* this.zoomFactor);
							m_utils.jQuerySelect(".selected-tool").removeClass("selected-tool");
						}
					} else if (this.mode == this.NORMAL_MODE) {
						this.clearCurrentSelection();
						m_messageDisplay.clear();
						if (this.currentConnection) {
							this.currentConnection.deselect();
							this.currentConnection = null;
						}
					} else {
						this.disEngageConnection();
					}
				};

				Diagram.prototype.disEngageConnection = function() {
					if (this.mode == this.CONNECTION_MODE
							&& this.currentConnection != null && !this.currentConnection.oid) {
						this.currentConnection.remove();
						this.currentConnection = null;
						m_messageDisplay.clear();
						this.mode = this.NORMAL_MODE;
						m_utils.jQuerySelect(".selected-tool").removeClass("selected-tool");
					}
				};

				/**
				 * sync : Synchronous AJAX call is made, if set(needed in
				 * scenario like creating symbol from flyout menu where symbol
				 * should be created before createConnection)
				 */
				Diagram.prototype.placeNewSymbol = function(x, y, sync) {
					sync = true;
					this.newSymbol.complete(sync);
					// If symbol is not contained in swimlane, return
					if (!this.newSymbol.isCompleted()) {
						this.newSymbol = null;
						return false;
					}

					this.mode = this.NORMAL_MODE;

					this.snapSymbol(this.newSymbol);

					var lastSymbol = this.newSymbol;
					this.newSymbol = null;
					return lastSymbol;
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

					// TODO Not the right algorithm - kept as a reminder to
					// change
					var changeDescriptionsDiagram = [];
					var poolChangeDesc;
					for ( var id in this.poolSymbols) {
						poolChangeDesc = this.poolSymbols[id]
								.flipFlowOrientation(this.flowOrientation);
						changeDescriptionsDiagram = changeDescriptionsDiagram
								.concat(poolChangeDesc);

						this.poolSymbols[id].recalculateBoundingBox();
						this.poolSymbols[id].adjustGeometry();
					}

					// Rotate anchor points
					var connectionDesc;
					for ( var n in this.connections) {
						connectionDesc = this.connections[n]
								.flipFlowOrientation(this.flowOrientation);
						changeDescriptionsDiagram.push(connectionDesc);
					}

					var diagramChanges = {
						orientation : this.flowOrientation
					};

					changeDescriptionsDiagram.push({
						oid : this.oid,
						changes : diagramChanges
					});

					// update
					var command = m_command.createUpdateDiagramCommand(
							this.model.id, changeDescriptionsDiagram);

					m_commandsController.submitCommand(command);
				};

				/**
				 *
				 */
				Diagram.prototype.print = function(anchorPoint) {
					m_utils.jQuerySelect("#" + this.divId).jqprint();
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

					if (null != this.currentConnection) {
						// Set dummy anchor point
						this.currentConnection.setDummySecondAnchorPoint();
					} else {
						this.mode = this.NORMAL_MODE;
					}
				};

				/**
				 *
				 */
				Diagram.prototype.connectToActivity = function(symbol) {
					this.addAndConnectSymbol(symbol, m_activitySymbol
							.createActivitySymbol(this,
									m_constants.TASK_ACTIVITY_TYPE));
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
				Diagram.prototype.connectToIntermediateEvent = function(symbol) {
					this.addAndConnectSymbol(symbol, m_eventSymbol
							.createIntermediateEventSymbol(this));
				};

				/**
				 *
				 */
				Diagram.prototype.addAndConnectSymbol = function(startSymbol,
						targetSymbol) {

					this.newSymbol = targetSymbol;
					this.mode = this.CREATE_MODE;
					var x_adj = 0, y_adj = 0, fromAnchor, toAnchor;

					if (this.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
						x_adj = 0;
						y_adj = 100;
						fromAnchor = 2;
						toAnchor = 0;
						if (m_utils.isIntermediateEvent(startSymbol)) {
							fromAnchor = 2;
							toAnchor = 0;
						}
					} else {
						x_adj = 200;
						y_adj = 0;
						fromAnchor = 1;
						toAnchor = 3;
						if (m_utils.isIntermediateEvent(startSymbol)) {
							fromAnchor = 1;
							toAnchor = 0;
						}
					}

					this.newSymbol.prepare(startSymbol.x + x_adj, startSymbol.y
							+ y_adj);
					// Create connection if connectionValidation passes
					this.currentConnection = m_connection.createConnection(
							this, startSymbol.anchorPoints[fromAnchor]);

					if (null != this.currentConnection
							&& this.currentConnection
									.validateCreateConnection(
											this.currentConnection.fromAnchorPoint,
											this.newSymbol.anchorPoints[toAnchor])) {
						this.currentConnection.prepare();
						this.currentConnection
								.setSecondAnchorPointNoComplete(this.newSymbol.anchorPoints[toAnchor]);
					} else {
						// Remove the connection and symbol created, if
						// validation fails
						if (this.currentConnection) {
							this.currentConnection.remove();
							this.currentConnection = null;
						}
						
						this.newSymbol.remove();
						this.newSymbol = null;
						this.mode = this.NORMAL_MODE;
						
					}
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
						} else {
							this.currentConnection = null;
							this.mode = this.NORMAL_MODE;
						}
					} else {
						// Validate if connection is allowed on current anchor
						// point
						if (this.currentConnection.validateCreateConnection(
								this.currentConnection.fromAnchorPoint,
								anchorPoint)) {

							// When connection created from toolbar, the anchor
							// point should not change
							if (!m_utils.jQuerySelect(".selected-tool").is("#connectorButton")) {
								this.currentConnection
										.updateAnchorPointForSymbol();
							}

							this.currentConnection
									.setSecondAnchorPoint(anchorPoint, true);


							if (!this.currentConnection.isCompleted()) {
								this.currentConnection.remove();
							} else {
								this.currentConnection.select();
								m_messageDisplay
										.showMessage(m_i18nUtils.getProperty("modeler.messagedisplay.messages.info.connCreated"));
							}
							this.currentConnection = null;
							this.mode = this.NORMAL_MODE;
						} else {
							if (this.currentConnection.toAnchorPoint.symbol) {
								m_utils
										.removeItemFromArray(
												this.currentConnection.toAnchorPoint.symbol.connections,
												this.currentConnection);
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
					if(-1 == this.currentSelection.indexOf(drawable)){
						this.currentSelection.push(drawable);
					}
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
						if(!this.currentConnection.oid){
							this.currentConnection.remove();
						}
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
					m_utils.markControlsReadonly('modelerPropertiesPanelWrapper', false);
					this.processPropertiesPanel.setElement(this.process);
					m_propertiesPanel.initializeProcessPropertiesPanel(this.processPropertiesPanel);
				};

				/**
				 *
				 */
				Diagram.prototype.selectedSymbolsDragStart = function() {
					if (this.mode == this.NORMAL_MODE) {
						this.mode = this.SYMBOL_MOVE_MODE;
						for ( var n in this.currentSelection) {
							this.currentSelection[n].dragStart_();
						}
						this.dragEnabled = true;
					}
				};

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
				Diagram.prototype.selectedSymbolsDragStop = function() {
					var changeDescriptionsDiagram = [];
					var failed = false;
					
					if (this.currentSelection.length == 1) {
						if (m_utils.isIntermediateEvent(this.currentSelection[0])) {
							changeDescriptionsDiagram = this.currentSelection[0].dragStop_(false);
							if(null == changeDescriptionsDiagram){
								failed = true;
								this.revertDrag();
							}
						}
					}
					
					if (changeDescriptionsDiagram && changeDescriptionsDiagram.length == 0) {
						for ( var n in this.currentSelection) {
							var changes = this.currentSelection[n]
									.dragStop_(this.currentSelection.length > 1);
							if (null == changes) {
								failed = true;
								this.revertDrag();
								break;
							}
							changeDescriptionsDiagram.push(changes);
						}
					}

					if (!failed) {
						//Update new coordinates
						var command = m_command.createUpdateDiagramCommand(
								this.model.id, changeDescriptionsDiagram);
						command.sync = true;
						m_commandsController.submitCommand(command);

						this.adjustLanes();
					}
					this.clearCurrentSelection();
				};

				Diagram.prototype.adjustLanes = function(){
					//Adjust Lanes to fit the symbols
					var adjustedLanes = [];
					for ( var n in this.currentSelection) {
						var swimlane = this.currentSelection[n].parentSymbol;
						if (-1 == adjustedLanes.indexOf(swimlane)) {
							swimlane.adjustToSymbolBoundaries();
							adjustedLanes.push(swimlane);
						}
					}
				};
				
				Diagram.prototype.revertDrag = function() {
					for ( var n in this.currentSelection) {
						this.currentSelection[n].revertDrag_();
					}
				};

				/**
				 *
				 */
				Diagram.prototype.showEditable = function(textPrimitive) {
					// Use the Symbol's x co-ordinate to decide the width of
					// textbox
					if (!this.symbolEditMode) {
						m_utils.debug("text primitive set");
						// TODO: Can registering for this event be blocked for some Symbols
						if (textPrimitive.auxiliaryProperties.callbackScope.modelElement
								&& textPrimitive.auxiliaryProperties.callbackScope.modelElement.isReadonly()) {
							return;
						}

						// If data, check if it's external data and if external check if it's read-only
						if (textPrimitive.auxiliaryProperties.callbackScope.modelElement
								&& textPrimitive.auxiliaryProperties.callbackScope.modelElement.type === m_constants.DATA
								&& textPrimitive.auxiliaryProperties.callbackScope.modelElement.externalReference
								&& textPrimitive.auxiliaryProperties.callbackScope.modelElement.dataFullId) {
							var extData = m_model.findData(textPrimitive.auxiliaryProperties.callbackScope.modelElement.dataFullId);
							if (extData && extData.isReadonly()) {
								return;
							}
						}

						this.currentTextPrimitive = textPrimitive.auxiliaryProperties.callbackScope
								.showEditable();

						this.symbolEditMode = true;
						this.clickedOnScrollBar = false;
						m_utils.debug("editable activated");
					}
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
					var changes = this.currentTextPrimitive.auxiliaryProperties.callbackScope.getEditedChanges(content);

					m_commandsController
							.submitCommand(m_command
									.createUpdateModelElementCommand(
											this.currentTextPrimitive.auxiliaryProperties.callbackScope.diagram.modelId,
											this.currentTextPrimitive.auxiliaryProperties.callbackScope.oid,
											changes));
					this.currentTextPrimitive.show();
					this.currentTextPrimitive.auxiliaryProperties.callbackScope
							.adjustPrimitivesOnShrink();
					this.symbolEditMode = false;
					this.clickedOnScrollBar = false;
					m_utils.debug("text primitive shown");
					this.currentTextPrimitive.auxiliaryProperties.callbackScope.parentSymbol.adjustToSymbolBoundaries();
					}
				};

				Diagram.prototype.submitEditableArea = function(content) {
					if (content == '') {
						this.cancelEditableArea();
					} else {
					this.editableTextArea.css("visibility", "hidden")
							.hide().trigger("blur");
					this.currentTextPrimitive.attr("text", content);
					m_utils.debug("textarea set");
					var changes = this.currentTextPrimitive.auxiliaryProperties.callbackScope.getEditedChanges(content);

					m_commandsController
							.submitCommand(m_command
									.createUpdateModelElementCommand(
											this.currentTextPrimitive.auxiliaryProperties.callbackScope.diagram.modelId,
											this.currentTextPrimitive.auxiliaryProperties.callbackScope.oid,
											changes));
					this.currentTextPrimitive.show();
					this.currentTextPrimitive.auxiliaryProperties.callbackScope
							.adjustPrimitives();
					this.symbolEditMode = false;
					m_utils.debug("textarea primitive shown");
					this.currentTextPrimitive.auxiliaryProperties.callbackScope.parentSymbol.adjustToSymbolBoundaries();
					}
				};

				/**
				 *
				 */
				Diagram.prototype.cancelEditable = function() {
					this.editableText.css("visibility", "hidden").hide()
							.trigger("blur");

					if (!this.currentTextPrimitive.removed) {
						this.currentTextPrimitive.show();
					}

					this.symbolEditMode = false;
					m_utils.debug("text primitive hidden");
				};

				Diagram.prototype.cancelEditableArea = function() {
					this.editableTextArea.css("visibility", "hidden").hide()
							.trigger("blur");
					this.currentTextPrimitive.show();
					this.symbolEditMode = false;
					m_utils.debug("text primitive hidden");
				};

				Diagram.prototype.resetEditableText = function() {
					if (this.symbolEditMode) {
						m_utils.debug("resetting editable text");
						this.editableText.resetForm();
						this.editableTextArea.resetForm();
					}
				};

				/**
				 *
				 */
				Diagram.prototype.zoomIn = function() {
					this.zoomFactor = Math.max(this.zoomFactor
							- m_constants.ZOOM_INCREMENT, 1);

					this.canvasManager.setCanvasSize(this.width / this.zoomFactor,
							this.height / this.zoomFactor);

					this.canvasManager.setViewBox(0, 0, this.zoomFactor);
				};

				/**
				 *
				 */
				Diagram.prototype.zoomOut = function() {
					this.zoomFactor = this.zoomFactor
							+ m_constants.ZOOM_INCREMENT;

					this.canvasManager.setCanvasSize(this.width / this.zoomFactor,
							this.height / this.zoomFactor);

					this.canvasManager.setViewBox(0, 0, this.zoomFactor);
				};

				/**
				 *
				 */
				Diagram.prototype.loadProcess = function() {
					m_communicationController.syncGetData({
						url : this.getEndpointUrl() + "/models/" + encodeURIComponent(this.modelId)
								+ "/process/" + encodeURIComponent(this.processId) + "/loadModel",
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

					this.oid = json.oid;
					this.flowOrientation = json.orientation;

					// Create pools and lanes

					var poolCount = 0;

					for ( var n in json.poolSymbols) {
						this.poolSymbols[n] = m_poolSymbol
								.createPoolSymbolFromJson(this,
										json.poolSymbols[n]);

						// TODO Remove

						this.poolSymbol = this.poolSymbols[n];

						poolCount++;
					}

					if (poolCount == 0) {
						m_messageDisplay
								.showErrorMessage("Process diagram does not contain any pools. Possibly not well-formed BPMN.");
					}

					// TODO Correct algorithms

					var totalWidth = 0;
					var totalHeight = 0;

					for ( var n in this.poolSymbols) {
						totalWidth += this.poolSymbols[n].width;
						totalHeight += this.poolSymbols[n].height;
					}

					this.setSize(totalWidth, totalHeight);

					m_utils.debug("===> Diagram JSON");
					m_utils.debug(this);

					// Create connections

					for ( var n in json.connections) {
						m_connection.createConnectionFromJson(this,
								json.connections[n]);
					}

					// Resolve all further non-hierarchical relationships, e.g. boundary events

					for ( var n in this.symbols) {
						this.symbols[n].resolveNonHierarchicalRelationships();
					}

					this.processPropertiesPanel.setElement(this.process);
				};

				/**
				 *
				 */
				Diagram.prototype.setSize = function(width, height) {
					this.width = width;
					this.height = height;
					this.background.attr({
						"width" : (width / this.zoomFactor) * 1.25,
						"height" : (height / this.zoomFactor) * 1.25
					});
					this.canvasManager.setCanvasSize(width / this.zoomFactor, height / this.zoomFactor);
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


				/**
				 *
				 */
				Diagram.prototype.getCanvasPosition = function() {
					return getCanvasPosition(this.divId);
				}

				// TODO need not read position every time
				// Can cache the position on view activation and on navigation panel
				// collapse / expand event (nto sure if there exist evetns for navigation panel
				// collapse / expand???)
				function getCanvasPosition(divId) {
					var canvasPos = m_utils.jQuerySelect("#" + divId).position();

					if(canvasPos){
						return {
							left : canvasPos.left,
							top : canvasPos.top
						};
					}
					else{
						return {
							left : 0,
							top : 0
						};
					}
				}

				function Diagram_clickClosure(event) {
					this.auxiliaryProperties.diagram.onClick(event.pageX
							- this.auxiliaryProperties.diagram.getCanvasPosition().left, event.pageY
							- this.auxiliaryProperties.diagram.getCanvasPosition().top);
				}

				function Diagram_mouseDownClosure(event) {
					this.auxiliaryProperties.diagram.onMouseDown(event.pageX
							- this.auxiliaryProperties.diagram.getCanvasPosition().left, event.pageY
							- this.auxiliaryProperties.diagram.getCanvasPosition().top);
				}

				function Diagram_mouseMoveClosure(event) {
					this.auxiliaryProperties.diagram.onMouseMove(event.pageX
							- this.auxiliaryProperties.diagram.getCanvasPosition().left, event.pageY
							- this.auxiliaryProperties.diagram.getCanvasPosition().top);
				}

				function Diagram_mouseUpClosure(event) {
					this.auxiliaryProperties.diagram.onMouseUp(event.pageX
							- this.auxiliaryProperties.diagram.getCanvasPosition().left, event.pageY
							- this.auxiliaryProperties.diagram.getCanvasPosition().top);
				}
			}
		});
