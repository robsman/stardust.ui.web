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
				"bpm-modeler/js/m_canvasManager", "bpm-modeler/js/m_symbol", "bpm-modeler/js/m_gatewaySymbol",
				"bpm-modeler/js/m_testPropertiesPanel" ],
		function(m_utils, m_constants, m_command, m_messageDisplay,
				m_canvasManager, m_symbol, m_gatewaySymbol,
				m_testPropertiesPanel) {
			var TEST_WIDTH = 50;
			var TEST_HEIGHT = 20;

			return {
				createTestSymbol : function(diagram) {
					var testSymbol = new TestSymbol();

					testSymbol.bind(diagram);
					testSymbol.modelElement = {};

					// TestSymbol.diagram.process.events[TestSymbol.modelElement.id]
					// = TestSymbol.modelElement;

					return testSymbol;
				},
				createTestSymbolFromJson : function(diagram, lane, json) {
					m_utils.inheritFields(json, m_symbol.createSymbol());
					m_utils.inheritMethods(json, new TestSymbol());

					json.bind(diagram);
					json.initializeFromJson(lane);

					return json;
				}
			};

			/**
			 *
			 */
			function TestSymbol() {
				var symbol = m_symbol.createSymbol();

				m_utils.inheritFields(this, symbol);
				m_utils.inheritMethods(TestSymbol.prototype, symbol);

				this.width = TEST_WIDTH;
				this.height = TEST_HEIGHT;

				/**
				 * Binds all client-side aspects to the object (graphics
				 * objects, diagram, base classes).
				 */
				TestSymbol.prototype.bind = function(diagram) {
					this.type = "testSymbol";

					this.diagram = diagram;

					this.diagram.lastSymbol = this;

					this.propertiesPanel =
					m_testPropertiesPanel.getInstance();
					this.rect = null;
					this.image = null;

					this.width = TEST_WIDTH;
					this.height = TEST_HEIGHT;
				};

				/**
				 *
				 */
				TestSymbol.prototype.toString = function() {
					return "Lightdust.TestSymbol";
				};

				/**
				 *
				 */
				TestSymbol.prototype.initializeFromJson = function(lane) {
					// m_utils.inheritMethods(this.modelElement.prototype,
					// m_event.prototype);

					this.width = TEST_WIDTH;
					this.height = TEST_HEIGHT;

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
				TestSymbol.prototype.createTransferObject = function() {
					var transferObject = {};

					m_utils.inheritFields(transferObject, this);

					transferObject = this.prepareTransferObject(transferObject);

					transferObject.rect = null;
					transferObject.image = null;

					return transferObject;
				};

				/**
				 *
				 */
				TestSymbol.prototype.getPath = function(withId) {
					var path = "/models/" + this.diagram.model.id
							+ "/processes/" + this.diagram.process.id
							+ "/test";

					if (withId) {
						path += "/" + this.modelElement.id;
					}

					return path;
				};

				/**
				 *
				 */
				TestSymbol.prototype.createPrimitives = function() {
					this.rect = m_canvasManager
							.drawRectangle(
									this.x,
									this.y,
									this.width,
									this.height,
									{
										'fill' : m_constants.ACTIVITY_SYMBOL_DEFAULT_FILL_COLOR,
										'fill-opacity' : m_constants.ACTIVITY_SYMBOL_DEFAULT_FILL_OPACITY,
										'stroke' : m_constants.DEFAULT_STROKE_COLOR,
										'stroke-width' : m_constants.ACTIVITY_SYMBOL_DEFAULT_STROKE_WIDTH,
										'r' : 4
									});

					this.addToPrimitives(this.rect);

					this.image = m_canvasManager.drawImageAt(
							"../../images/icons/camunda.gif", this.x
									+ TEST_WIDTH - 0.5 * 16, this.y
									+ TEST_HEIGHT - 0.5 * 16, 16, 16);

					this.addToPrimitives(this.image);
				};

				/**
				 * Registers symbol in specific lists in the diagram and model
				 * element in the process.
				 */
				TestSymbol.prototype.register = function() {
					// this.diagram.TestSymbols[this.oid] = this;
					// this.diagram.process.events[this.modelElement.id] =
					// this.modelElement;
				};

				/**
				 *
				 */
				TestSymbol.prototype.initializeEventHandling = function() {
				};

				/**
				 *
				 */
				TestSymbol.prototype.refreshFromModelElement = function() {
				};

				/**
				 *
				 */
				TestSymbol.prototype.createFlyOutMenu = function() {
					this.addFlyOutMenuItems([], [ {
						imageUrl : "plugins/bpm-modeler/images/icons/connect.png",
						imageWidth : 16,
						imageHeight : 16,
						clickHandler : TestSymbol_connectToClosure
					}, {
						imageUrl : "plugins/bpm-modeler/images/icons/activity.png",
						imageWidth : 16,
						imageHeight : 16,
						clickHandler : TestSymbol_connectToActivityClosure
					}, {
						imageUrl : "plugins/bpm-modeler/images/icons/gateway.png",
						imageWidth : 16,
						imageHeight : 16,
						clickHandler : TestSymbol_connectToGatewayClosure
					} ], [ {
						imageUrl : "plugins/bpm-modeler/images/icons/delete.png",
						imageWidth : 16,
						imageHeight : 16,
						clickHandler : TestSymbol_removeClosure
					} ]);
				};

				/**
				 *
				 */
				TestSymbol.prototype.highlight = function() {
					this.rect.attr({
						"stroke" : m_constants.SELECT_STROKE_COLOR
					});
				};

				/**
				 *
				 */
				TestSymbol.prototype.dehighlight = function() {
					this.rect.attr({
						"stroke" : m_constants.DEFAULT_STROKE_COLOR
					});
				};

				/**
				 *
				 */
				TestSymbol.prototype.adjustPrimitives = function(dX, dY) {
					this.rect.attr({
						x : this.x + TEST_WIDTH,
						y : this.y + TEST_HEIGHT
					});
					this.image.attr({
						x : this.x + TEST_WIDTH - 0.5
								* 16,
						y : this.y + TEST_HEIGHT - 0.5
								* 16
					});
				};

				/**
				 *
				 */
				TestSymbol.prototype.recalculateBoundingBox = function() {
					// Noting to be done here
				};

				/**
				 *
				 */
				TestSymbol.prototype.validateCreateConnection = function() {
					m_messageDisplay
							.showMessage("No connections allowed for this Test Symbol.");

					return false;
				};

				/**
				 *
				 */
				TestSymbol.prototype.onComplete = function() {
					this.onParentSymbolChange();
				};

				/*
				 *
				 */
				TestSymbol.prototype.onParentSymbolChange = function() {
				};
			}

			/**
			 *
			 */
			function TestSymbol_connectToClosure() {
				this.auxiliaryProperties.callbackScope.diagram
						.connectSymbol(this.auxiliaryProperties.callbackScope);
			}

			/**
			 *
			 */
			function TestSymbol_connectToGatewayClosure() {
				this.auxiliaryProperties.callbackScope.diagram
						.connectToGateway(this.auxiliaryProperties.callbackScope);
			}

			/**
			 *
			 */
			function TestSymbol_connectToActivityClosure() {
				this.auxiliaryProperties.callbackScope.diagram
						.connectToActivity(this.auxiliaryProperties.callbackScope);
			}

			/**
			 *
			 */
			function TestSymbol_removeClosure() {
				this.auxiliaryProperties.callbackScope
						.createAndSubmitDeleteCommand();
			}
		});