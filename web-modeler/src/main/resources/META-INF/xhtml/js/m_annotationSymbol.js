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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_command", "bpm-modeler/js/m_messageDisplay",
				"bpm-modeler/js/m_canvasManager", "bpm-modeler/js/m_symbol", "bpm-modeler/js/m_modelerUtils" ],
		function(m_utils, m_constants, m_command, m_messageDisplay,
				m_canvasManager, m_symbol, m_modelerUtils) {
			return {
				create : function(diagram) {
					var annotationSymbol = new AnnotationSymbol();

					annotationSymbol.bind(diagram);
					annotationSymbol.modelElement = null;

					return annotationSymbol;
				},
				createFromJson : function(diagram, lane, json) {
					m_utils.inheritFields(json, m_symbol.createSymbol());
					m_utils.inheritMethods(json, new AnnotationSymbol());

					json.bind(diagram);
					json.initializeFromJson(lane);

					return json;
				}
			};

			/**
			 *
			 */
			function AnnotationSymbol() {
				var symbol = m_symbol.createSymbol();

				m_utils.inheritFields(this, symbol);
				m_utils.inheritMethods(AnnotationSymbol.prototype, symbol);

				this.text = null;
				this.path = null;
				this.rect = null;
				this.width = m_constants.ANNOTATION_SYMBOL_DEFAULT_WIDTH;
				this.height = m_constants.ANNOTATION_SYMBOL_DEFAULT_HEIGHT;
				this.content = "Enter an annotation";

				/**
				 * Binds all client-side aspects to the object (graphics
				 * objects, diagram, base classes).
				 */
				AnnotationSymbol.prototype.bind = function(diagram) {
					this.type = m_constants.ANNOTATION_SYMBOL;
					this.diagram = diagram;
					this.diagram.lastSymbol = this;
					this.propertiesPanel = this.diagram.annotationPropertiesPanel;
					this.rect = null;
					this.image = null;
					this.width = m_constants.ANNOTATION_SYMBOL_DEFAULT_WIDTH;
					this.height = m_constants.ANNOTATION_SYMBOL_DEFAULT_HEIGHT;
				};

				/**
				 *
				 */
				AnnotationSymbol.prototype.toString = function() {
					return "Lightdust.AnnotationSymbol";
				};

				/**
				 *
				 */
				AnnotationSymbol.prototype.initializeFromJson = function(lane) {
					// m_utils.inheritMethods(this.modelElement.prototype,
					// m_event.prototype);

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
				AnnotationSymbol.prototype.createTransferObject = function() {
					var transferObject = {};

					m_utils.inheritFields(transferObject, this);

					transferObject = this.prepareTransferObject(transferObject);

					transferObject.rect = null;
					transferObject.path = null;
					transferObject.text = null;

					return transferObject;
				};

				/**
				 *
				 */
				AnnotationSymbol.prototype.createPrimitives = function() {
					this.rect = this.diagram.canvasManager
							.drawRectangle(
									this.x,
									this.y,
									this.width,
									this.height,
									{
										fill : "#ffffff",
										stroke : null,
										'stroke-width' : 0
									});

					this.addToPrimitives(this.rect);
					this.addToEditableTextPrimitives(this.rect);

					this.path = this.diagram.canvasManager
							.drawPath(
									this.getPathSvgString(),
									{
										'stroke' : m_constants.DEFAULT_STROKE_COLOR,
										'stroke-width' : m_constants.ANNOTATION_SYMBOL_DEFAULT_STROKE_WIDTH
									});
					this.addToPrimitives(this.path);

					this.text = this.diagram.canvasManager.drawTextNode(
							this.x,
							this.y, this.content).attr({
						"text-anchor" : "start",
						"font-family" : m_constants.DEFAULT_FONT_FAMILY,
						"font-size" : m_constants.DEFAULT_FONT_SIZE
					});


					this.addToPrimitives(this.text);
					this.addToEditableTextPrimitives(this.text);
				};

				/**
				 *
				 */
				AnnotationSymbol.prototype.getPathSvgString = function() {
					return "M"
							+ (this.x + this.width)
							+ " "
							+ this.y
							+ " L"
							+ this.x
							+ " "
							+ this.y
							+ " L"
							+ this.x
							+ " "
							+ (this.y + this.height)
							+ " L"
							+ (this.x + this.width)
							+ " "
							+ (this.y + this.height);
				};

				/**
				 * Registers symbol in specific lists in the diagram and model
				 * element in the process.
				 */
				AnnotationSymbol.prototype.register = function() {
					// this.diagram.AnnotationSymbols[this.oid] = this;
					// this.diagram.process.events[this.modelElement.id] =
					// this.modelElement;
				};

				/**
				 *
				 */
				AnnotationSymbol.prototype.initializeEventHandling = function() {
				};

				/**
				 *
				 */
				AnnotationSymbol.prototype.refreshFromModelElement = function() {
					this.text.attr({
						text : this.content
					});
				};

				/**
				 *
				 */
				AnnotationSymbol.prototype.createFlyOutMenu = function() {
					this
							.addFlyOutMenuItems(
									[],
									//This is commented temporarily
									[
											/*{
												imageUrl : "plugins/bpm-modeler/images/icons/connect.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : AnnotationSymbol_connectToClosure
											}*/],
									[ {
										imageUrl : "plugins/bpm-modeler/images/icons/delete.png",
										imageWidth : 16,
										imageHeight : 16,
										clickHandler : AnnotationSymbol_removeClosure
									} ]);
				};

				/**
				 *
				 */
				AnnotationSymbol.prototype.highlight = function() {
					this.path.attr({
						stroke : m_constants.SELECT_STROKE_COLOR
					});
				};

				/**
				 *
				 */
				AnnotationSymbol.prototype.dehighlight = function() {
					this.path.attr({
						stroke : m_constants.DEFAULT_STROKE_COLOR
					});
				};

				/**
				 *
				 */
				AnnotationSymbol.prototype.adjustPrimitives = function(dX, dY) {
					if (this.parentSymbol && this.parentSymbol.minimized) {
						return;
					}

					this.height = this.text.getBBox().height + 5;

					if (this.height < m_constants.ANNOTATION_SYMBOL_DEFAULT_HEIGHT) {
						this.height = m_constants.ANNOTATION_SYMBOL_DEFAULT_HEIGHT;
					}

					this.rect.attr({
						x : this.x,
						y : this.y,
						height : this.height
					});
					this.path.attr({
						path : this.getPathSvgString()
					});
					this.text.attr({
						x : this.x + 8,
						y : this.y + 0.5 * this.height
					});
				};

				AnnotationSymbol.prototype.showEditable = function() {
					this.text.hide();
					var editableTextArea = this.diagram.editableTextArea;
					var scrollPos = m_modelerUtils.getModelerScrollPosition();

					var name = this.content;
					var textboxWidth = this.text.getBBox().width + 20;
					var textboxHeight = this.text.getBBox().height + 20;

					editableTextArea.css("width", parseInt(textboxWidth
							.valueOf()));
					editableTextArea.css("height", parseInt(textboxHeight
							.valueOf()));

					editableTextArea.css("visibility", "visible").html(
							name).moveDiv(
							{
								"x" : this.x + this.diagram.getCanvasPosition().left
										+ this.width / 5 - 10,
								"y" : this.y + this.diagram.getCanvasPosition().top
										+ this.height / 8
							}).show().trigger("dblclick");
					return this.text;
				};

				AnnotationSymbol.prototype.postComplete = function() {
					this.select();
					this.diagram.showEditable(this.text);
				};

				AnnotationSymbol.prototype.adjustPrimitivesOnShrink = function() {
					if (this.parentSymbol && this.parentSymbol.minimized) {
						return;
					}
					if (this.text.getBBox().width > this.width) {
						var words = this.text.attr("text");
						m_utils.textWrap(this.text, this.width);
					}

					if (this.icon.getBBox().width > this.width) {
						this.icon.hide();
					} else {
						this.icon.show();
					}
				};

				AnnotationSymbol.prototype.getEditedChanges = function(text) {
					return {
						content : text
					};
				};

				/**
				 *
				 */
				AnnotationSymbol.prototype.recalculateBoundingBox = function() {
					// Noting to be done here
				};

				/**
				 *
				 */
				AnnotationSymbol.prototype.validateCreateConnection = function() {
					return false;
				};

				/**
				 *
				 */
				AnnotationSymbol.prototype.onComplete = function() {
					this.onParentSymbolChange();
				};

				/*
				 *
				 */
				AnnotationSymbol.prototype.onParentSymbolChange = function() {
				};
			}

			/**
			 *
			 */
			function AnnotationSymbol_connectToClosure() {
				this.auxiliaryProperties.callbackScope.diagram
						.connectSymbol(this.auxiliaryProperties.callbackScope);
			}

			/**
			 *
			 */
			function AnnotationSymbol_connectToGatewayClosure() {
				this.auxiliaryProperties.callbackScope.diagram
						.connectToGateway(this.auxiliaryProperties.callbackScope);
			}

			/**
			 *
			 */
			function AnnotationSymbol_connectToActivityClosure() {
				this.auxiliaryProperties.callbackScope.diagram
						.connectToActivity(this.auxiliaryProperties.callbackScope);
			}

			/**
			 *
			 */
			function AnnotationSymbol_removeClosure() {
				this.auxiliaryProperties.callbackScope
						.createAndSubmitDeleteCommand();
			}
		});