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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_messageDisplay", "bpm-modeler/js/m_command", "bpm-modeler/js/m_canvasManager", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_symbol", "bpm-modeler/js/m_connection", "bpm-modeler/js/m_data", "bpm-modeler/js/m_modelerUtils", "bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_messageDisplay, m_command, m_canvasManager, m_model,
				m_symbol, m_connection, m_data, m_modelerUtils, m_i18nUtils) {

			return {
				/**
				 * Invoked to create a data symbol and (during completion) a
				 * corresponding primitive data.
				 *
				 * @param diagram
				 * @returns
				 */
				createDataSymbol : function(diagram) {
					var dataSymbol = new DataSymbol();

					dataSymbol.bind(diagram);

					var index = diagram.model.getNewDataIndex();

					// TODO Need to create data before!

					var dataNamePrefix = m_i18nUtils.getProperty("modeler.diagram.newData.namePrefix");
					dataSymbol.dataId = dataNamePrefix + "_" + index;
					dataSymbol.dataName = dataNamePrefix + " " + index;
					// Data is not present at server side, using DataIndex and
					// modelId to create dataId
					dataSymbol.dataFullId = m_model.getFullId(diagram.model,
							dataSymbol.dataId);
					return dataSymbol;
				},

				createDataSymbolFromData : function(diagram, data) {
					var dataSymbol = new DataSymbol();

					dataSymbol.activateEditOnCreation = false;

					dataSymbol.bind(diagram);

					dataSymbol.modelElement = data;
					dataSymbol.dataId = data.id;
					dataSymbol.dataName = data.name;
					dataSymbol.dataFullId = data.getFullId();

					return dataSymbol;
				},

				createDataSymbolFromJson : function(diagram, lane, json) {
					// TODO Ugly
					m_utils.inheritFields(json, m_symbol.createSymbol());
					m_utils.inheritMethods(json, new DataSymbol());

					json.bind(diagram);
					json.initializeFromJson(lane);

					return json;
				}
			};

			/**
			 *
			 */
			function DataSymbol() {
				var symbol = m_symbol.createSymbol();

				m_utils.inheritFields(this, symbol);
				m_utils.inheritMethods(DataSymbol.prototype, symbol);

				this.width = m_constants.DATA_SYMBOL_DEFAULT_WIDTH;
				this.height = m_constants.DATA_SYMBOL_DEFAULT_HEIGHT;
				this.dataFullId = null;

				/**
				 * Binds all client-side aspects to the object (graphics
				 * objects, diagram, base classes).
				 */
				DataSymbol.prototype.bind = function(diagram) {
					this.type = m_constants.DATA_SYMBOL;
					this.diagram = diagram;

					this.diagram.lastSymbol = this;

					this.propertiesPanel = this.diagram.dataPropertiesPanel;
					this.path = null;
					this.text = null;
				};

				/**
				 *
				 */
				DataSymbol.prototype.toString = function() {
					return "Lightdust.DataSymbol";
				};

				/**
				 *
				 */
				DataSymbol.prototype.initializeFromJson = function(lane) {
					// TODO Should come from server
					this.width = m_constants.DATA_SYMBOL_DEFAULT_WIDTH;
					this.height = m_constants.DATA_SYMBOL_DEFAULT_HEIGHT;

					this.parentSymbol = lane;
					this.parentSymbolId = lane.id;
					this.parentSymbol.containedSymbols.push(this);
					this.prepareNoPosition();
					this.completeNoTransfer();
					this.register();
				};

				/**
				 *
				 */
				DataSymbol.prototype.createTransferObject = function() {
					var transferObject = {};

					m_utils.inheritFields(transferObject, this);

					transferObject = this.prepareTransferObject(transferObject);

					transferObject.path = null;
					transferObject.text = null;
					transferObject.width = m_constants.DATA_SYMBOL_DEFAULT_WIDTH_EC;
					transferObject.height = m_constants.DATA_SYMBOL_DEFAULT_HEIGHT_EC;

					// Data are not transfered with the symbol

					transferObject.modelElement = null;

					return transferObject;
				};

				/**
				 *
				 */
				DataSymbol.prototype.getPath = function(withId) {
					var path = "/models/" + this.diagram.model.id
							+ "/processes/" + this.diagram.process.id
							+ "/dataSymbols";

					if (withId) {
						path += "/" + this.dataFullId;
					}

					return path;
				};

				/**
				 *
				 */
				DataSymbol.prototype.createPrimitives = function() {
					this.path = this.diagram.canvasManager
							.drawPath(
									this.getPathSvgString(),
									{
										'fill' : m_constants.DATA_SYMBOL_DEFAULT_FILL_COLOR,
										'fill-opacity' : m_constants.DATA_SYMBOL_DEFAULT_FILL_OPACITY,
										'stroke' : m_constants.DEFAULT_STROKE_COLOR,
										'stroke-width' : m_constants.DATA_SYMBOL_DEFAULT_STROKE_WIDTH
									});
					this.addToPrimitives(this.path);
					this.addToEditableTextPrimitives(this.path);

					this.text = this.diagram.canvasManager.drawTextNode(this.x + 15,
							this.y + 50, this.dataName).attr({
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
				DataSymbol.prototype.initializeEventHandling = function() {
				};

				/**
				 *  Overwrites standard behavior from Drawable.
				 */
				DataSymbol.prototype.onCreate = function(transferObject) {
					this.oid = transferObject.oid;

					this.register();

					if (transferObject.data != null) {
						// Bind data to model if newly created

						this.diagram.model.dataItems[transferObject.data.id] = transferObject.data;
					}

					m_messageDisplay.markModified();
				};

				/**
				 * Registers symbol in specific lists in the diagram and model
				 * element in the process.
				 */
				DataSymbol.prototype.register = function() {
					this.diagram.dataSymbols[this.oid] = this;
				};

				/**
				 *
				 */
				DataSymbol.prototype.getPathSvgString = function() {
					return "M "
							+ (this.x + this.width - m_constants.DATA_SYMBOL_DOG_EAR_OFFSET)
							+ " "
							+ this.y
							+ " L "
							+ this.x
							+ " "
							+ this.y
							+ " L "
							+ this.x
							+ " "
							+ (this.y + this.height)
							+ " L "
							+ (this.x + this.width)
							+ " "
							+ (this.y + this.height)
							+ " L "
							+ (this.x + this.width)
							+ " "
							+ (this.y + m_constants.DATA_SYMBOL_DOG_EAR_OFFSET)
							+ " L "
							+ (this.x + this.width - m_constants.DATA_SYMBOL_DOG_EAR_OFFSET)
							+ " "
							+ this.y
							+ " L "
							+ (this.x + this.width - m_constants.DATA_SYMBOL_DOG_EAR_OFFSET)
							+ " "
							+ (this.y + m_constants.DATA_SYMBOL_DOG_EAR_OFFSET)
							+ " L " + (this.x + this.width) + " "
							+ (this.y + m_constants.DATA_SYMBOL_DOG_EAR_OFFSET);
				};

				/**
				 *
				 */
				DataSymbol.prototype.adjustPrimitives = function() {
					this.path.attr({
						"path" : this.getPathSvgString()
					});
					this.text.attr({
						"x" : this.x + 15,
						"y" : this.y + 50
					});
				};

				DataSymbol.prototype.recalculateBoundingBox = function() {
					// Noting to be done here
				};

				/**
				 *
				 */
				DataSymbol.prototype.refreshFromModelElement = function() {
					if (this.modelElement) {
						var data = m_model.findData(this.modelElement
								.getFullId());
					} else {
						var data = m_model.findData(this.dataFullId);
					}
					// Data may not have been created yet
					if (data != null) {
						this.text.attr("text", data.name);
						this.modelElement = data;
					}
				};

				/**
				 * Overrides function in Drawable to to to check if modelEleemnt
				 * has changed and if yes assigns it
				 */
				DataSymbol.prototype.applySymbolSpecific = function(
						changedObject) {
					if (this.modelElement
							&& this.modelElement.getFullId() != changedObject.dataFullId) {
						var data = m_model.findData(this.dataFullId);
						if (data != null) {
							this.modelElement = data;
						}
					}
				};

				/**
				 *
				 */
				DataSymbol.prototype.createFlyOutMenu = function() {
					this.addFlyOutMenuItems([], [ {
						imageUrl : "plugins/bpm-modeler/images/icons/connect.png",
						imageWidth : 16,
						imageHeight : 16,
						clickHandler : DataSymbol_connectToClosure
					} ], [ {
						imageUrl : "plugins/bpm-modeler/images/icons/delete.png",
						imageWidth : 16,
						imageHeight : 16,
						clickHandler : DataSymbol_removeClosure
					} ]);
				};

				/**
				 *
				 */
				DataSymbol.prototype.highlight = function() {
					this.path.attr({
						stroke : m_constants.SELECT_STROKE_COLOR
					});
				};

				/**
				 *
				 */
				DataSymbol.prototype.dehighlight = function() {
					this.path.attr({
						stroke : m_constants.DEFAULT_STROKE_COLOR
					});
				};

				/**
				 *
				 */
				DataSymbol.prototype.validateCreateConnection = function() {
					var inMapping = new Array();
					var outMapping = new Array();
					var inOutMapping = new Array();
					for ( var n in this.connections) {
						var connection = this.connections[n];
						if (null != connection.modelElement
								&& connection.modelElement.inputDataMapping
								&& connection.modelElement.outputDataMapping) {
							if (connection.fromAnchorPoint.symbol.type == m_constants.ACTIVITY_SYMBOL) {
								outMapping
										.push(connection.fromAnchorPoint.symbol.oid);
								inMapping
										.push(connection.fromAnchorPoint.symbol.oid);
							} else {
								outMapping
										.push(connection.toAnchorPoint.symbol.oid);
								inMapping
										.push(connection.toAnchorPoint.symbol.oid);
							}
						} else if (connection.fromAnchorPoint.symbol.type == m_constants.ACTIVITY_SYMBOL) {
							if (-1 != jQuery.inArray(
									connection.fromAnchorPoint.symbol.oid,
									outMapping)) {
								return false;
							} else {
								outMapping
										.push(connection.fromAnchorPoint.symbol.oid);
							}
						} else if (null != connection.toAnchorPoint
								&& null != connection.toAnchorPoint.symbol) {
							if (connection.toAnchorPoint.symbol.type == m_constants.ACTIVITY_SYMBOL) {
								if (-1 != jQuery.inArray(
										connection.toAnchorPoint.symbol.oid,
										inMapping)) {
									return false;
								} else {
									inMapping
											.push(connection.toAnchorPoint.symbol.oid);
								}
							}
						}
					}

					return true;
				};

				DataSymbol.prototype.showEditable = function() {
					this.text.hide();
					var editableText = this.diagram.editableText;
					var scrollPos = m_modelerUtils.getModelerScrollPosition();

					var name = this.modelElement.name;

					var textboxWidth = this.text.getBBox().width + 20;
					var textboxHeight = this.text.getBBox().height;

					editableText.css("width", parseInt(textboxWidth.valueOf()));
					editableText.css("height",
							parseInt(textboxHeight.valueOf()));

					editableText.css("visibility", "visible").html(name)
							.moveDiv(
									{
										"x" : this.x + this.diagram.getCanvasPosition().left
												+ this.width / 5 - 10,
										"y" : this.y + this.diagram.getCanvasPosition().top
												+ (this.height) + 5
									}).show().trigger("dblclick");
					return this.text;
				};

				DataSymbol.prototype.postComplete = function() {
					this.select();
					this.diagram.showEditable(this.text);
				};

				DataSymbol.prototype.adjustPrimitivesOnShrink = function() {
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
			function DataSymbol_connectToClosure() {
				this.auxiliaryProperties.callbackScope.diagram
						.connectSymbol(this.auxiliaryProperties.callbackScope);
			}

			/**
			 *
			 */
			function DataSymbol_removeClosure() {
				this.auxiliaryProperties.callbackScope.createAndSubmitDeleteCommand();
			}
		});