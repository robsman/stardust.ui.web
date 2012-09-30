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
		[ "m_utils", "m_constants", "m_command", "m_messageDisplay",
				"m_canvasManager", "m_symbol", "m_annotationPropertiesPanel" ],
		function(m_utils, m_constants, m_command, m_messageDisplay,
				m_canvasManager, m_symbol, m_annotationPropertiesPanel) {
			return {
				create : function(diagram) {
					var annotationSymbol = new AnnotationSymbol();

					annotationSymbol.bind(diagram);
					annotationSymbol.modelElement = {};

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
					this.propertiesPanel = m_annotationPropertiesPanel
							.getInstance();
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
					this.rect = m_canvasManager
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

					this.path = m_canvasManager
							.drawPath(
									this.getPathSvgString(),
									{
										'stroke' : m_constants.DEFAULT_STROKE_COLOR,
										'stroke-width' : m_constants.ANNOTATION_SYMBOL_DEFAULT_STROKE_WIDTH
									});
					this.addToPrimitives(this.path);

					this.text = m_canvasManager.drawTextNode(
							this.x + 0.5 * this.width,
							this.y + 0.5 * this.height, this.content).attr({
						"text-anchor" : "left",
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
							+ (this.x + m_constants.ANNOTATION_SYMBOL_DEFAULT_WIDTH)
							+ " "
							+ this.y
							+ " L"
							+ this.x
							+ " "
							+ this.y
							+ " L"
							+ this.x
							+ " "
							+ (this.y + m_constants.ANNOTATION_SYMBOL_DEFAULT_HEIGHT)
							+ " L"
							+ (this.x + m_constants.ANNOTATION_SYMBOL_DEFAULT_WIDTH)
							+ " "
							+ (this.y + m_constants.ANNOTATION_SYMBOL_DEFAULT_HEIGHT);
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
									[
											{
												imageUrl : "../../images/icons/connect.png",
												imageWidth : 16,
												imageHeight : 16,
												clickHandler : AnnotationSymbol_connectToClosure
											}],
									[ {
										imageUrl : "../../images/icons/remove.png",
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
					this.rect.attr({
						x : this.x,
						y : this.y
					});
					this.path.attr({
						path : this.getPathSvgString()
					});
					this.text.attr({
						x : this.x +  + 0.5 * this.width,
						y : this.y +  + 0.5 * this.height
					});
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