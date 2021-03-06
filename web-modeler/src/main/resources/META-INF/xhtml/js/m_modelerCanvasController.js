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
 * @author Omkar.Patil
 */

define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_globalVariables", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_messageDisplay",
				"bpm-modeler/js/m_canvasManager",
				"bpm-modeler/js/m_communicationController", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_logger",
				"bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_diagram", "bpm-modeler/js/m_activitySymbol",
				"bpm-modeler/js/m_eventSymbol", "bpm-modeler/js/m_gatewaySymbol", "bpm-modeler/js/m_dataSymbol", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_process", "bpm-modeler/js/m_activity", "bpm-modeler/js/m_data", "bpm-modeler/js/m_elementConfiguration",
				"bpm-modeler/js/m_modelerUtils", "bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_modelElementUtils" ],
		function(m_utils, m_globalVariables, m_constants, m_messageDisplay,
				m_canvasManager,
				m_communicationController, m_constants, m_logger,
				m_commandsController, m_diagram, m_activitySymbol,
				m_eventSymbol, m_gatewaySymbol, m_dataSymbol, m_model,
				m_process, m_activity, m_data, m_elementConfiguration,
				m_modelerUtils, m_i18nUtils, m_modelElementUtils) {
			return {
				initialize : function(fullId, divId, width, height, toolbarDiv) {
					new ModelerCanvasController(fullId, divId, width, height, toolbarDiv);
				}
			};
			
			function ModelerCanvasController(fullId, divId, width, height, toolbarDiv) {
				// TODO delete
				this.aTempId = Math.random(1000);
				
				var diagram = null;

				var canvasManager = m_canvasManager.initialize(divId, width, height);

				diagram = m_diagram.createDiagram(divId, canvasManager);

				diagram.initialize();

				m_commandsController.init(true, false);

				var process = m_model.findProcess(fullId);

				diagram.loadProcess();
				initDnD(divId);
				// TODO Used temporarily to indicate the VIEW_LOADED event for the Process Definition View
				m_globalVariables.findMainWindowBottomUp().EventHub.events.publish("VIEW_LOADED", "");
				m_utils.jQuerySelect("#processDefinitionView").css("visibility", "visible");
				
				function initDnD(divId) {
					var IE = document.all ? true : false;
					var canvasDiv = document.getElementById(divId);
					canvasDiv.onmousemove = function(e) {
						var parentWindow = m_globalVariables.findMainWindowBottomUp();
						// De-select any selected elements in the canvas
						// to avoid they getting dragged inadvertently
						if (parentWindow.iDnD.dragMode) {
							diagram.clearCurrentSelection();
						}

						if (e) {
							parentWindow.iDnD.setIframeXY(e, window.name);
						} else {
							parentWindow.iDnD.setIframeXY(window.event, window.name);
						}
					};

					canvasDiv.onmouseup = dropElement;

					/**
					 * All Drag & Drop logic.
					 */
					function dropElement(e) {
						var eve = e;
						if (!eve) {
							eve = window.event;
						}
						var parentWindow = m_globalVariables.findMainWindowBottomUp();
						parentWindow.iDnD.dragMode = false;

						if (diagram.process.isReadonly()) {
							m_messageDisplay.clear();
							m_messageDisplay.showErrorMessage("Process is marked as Read-only.");
						}

						if (!diagram.process.isReadonly() && parentWindow.iDnD.getTransferObject()) {
							var clickCoordinates = parentWindow.iDnD
									.getMouseCoordinates(eve);
							var scrollPos = m_modelerUtils
									.getModelerScrollPosition();
//							clickCoordinates.x += scrollPos.left;
//							clickCoordinates.y += scrollPos.top;

							var otherModelId = m_model.stripModelId(parentWindow.iDnD.getTransferObject().attr.fullId);
							if (otherModelId != diagram.modelId
									&& m_model.isModelReferencedIn(diagram.modelId, otherModelId)) {
								m_messageDisplay.clear();
								m_messageDisplay.showMessage(m_i18nUtils
										.getProperty("modeler.propertyPages.commonProperties.errorMessage.modelCircularReferenceNotAllowed"));
								parentWindow.iDnD.hideIframe();
								return;
							}

							// Check if there is any element in the model that
							// has same id and type
							// as that of the dragged element, if dragged
							// element belongs to other model.
							if (diagram.model.uuid != parentWindow.iDnD
									.getTransferObject().modelUUID) {
								var matchingElems = diagram.model
										.findModelElementsById(parentWindow.iDnD
												.getTransferObject().elementId);
								if (matchingElems && matchingElems.length > 0) {
									for ( var i in matchingElems) {
										if (!matchingElems[i].externalReference
												&& matchingElems[i].type === parentWindow.iDnD
														.getTransferObject().type) {
											m_messageDisplay.clear();
											m_messageDisplay
													.showMessage(m_i18nUtils
															.getProperty("modeler.propertyPages.commonProperties.errorMessage.elementWithSameIdExists"));
											parentWindow.iDnD.hideIframe();
											return;
										}
									}
								}
							}

							if (m_elementConfiguration
									.isValidDataType(parentWindow.iDnD
											.getTransferObject().elementType)) {
								// TODO other check required
								var data = m_model.findData(parentWindow.iDnD
										.getTransferObject().attr.fullId);

								if (isElementExternalAndPrivate(data)) {
									diaplayPrivateElementMessage();
									return;
								}

								var dataSymbol = m_dataSymbol
										.createDataSymbolFromData(diagram,
												data);

								dataSymbol.initialize(clickCoordinates.x
										- diagram.getCanvasPosition().left,
										clickCoordinates.y
												- diagram.getCanvasPosition().top);
							} else if (m_constants.ROLE_PARTICIPANT_TYPE == parentWindow.iDnD
									.getTransferObject().elementType
									|| m_constants.TEAM_LEADER_TYPE == parentWindow.iDnD
									.getTransferObject().elementType
									|| m_constants.CONDITIONAL_PERFORMER_PARTICIPANT_TYPE == parentWindow.iDnD
									.getTransferObject().elementType
									|| m_constants.ORGANIZATION_PARTICIPANT_TYPE == parentWindow.iDnD
									.getTransferObject().elementType) {
								var participant = m_model
										.findParticipant(parentWindow.iDnD
												.getTransferObject().attr.fullId);
								if (isElementExternalAndPrivate(participant)) {
									diaplayPrivateElementMessage();
									return;
								}
								diagram.poolSymbol
										.createSwimlaneSymbolFromParticipant(participant);
							} else if ('structuredDataType' == parentWindow.iDnD.getTransferObject().elementType
										|| 'compositeStructuredDataType' == parentWindow.iDnD.getTransferObject().elementType
										|| 'enumStructuredDataType' == parentWindow.iDnD.getTransferObject().elementType
										|| 'importedStructuredDataType' == parentWindow.iDnD.getTransferObject().elementType) {
								var dataStructure = m_model.findTypeDeclaration(parentWindow.iDnD
												.getTransferObject().attr.fullId);
								if (isElementExternalAndPrivate(dataStructure)) {
									diaplayPrivateElementMessage();
									return;
								}

								var data = m_data.createDataFromDataStructure(
										diagram.model, dataStructure);

								// Submit creation

								data.submitCreation();

								// TODO: this needs to be changed.
								// we need some mechanism to associate newly create
								// data with the data symbol to be created as now
								// we don't generate ID on client side.
								data = data.model.findModelElementByName(data.name);

								if (data) {
									var dataSymbol = m_dataSymbol
									.createDataSymbolFromData(diagram, data);

									dataSymbol.initialize(clickCoordinates.x
											- diagram.getCanvasPosition().left, clickCoordinates.y
											- diagram.getCanvasPosition().top);
									dataSymbol.refreshFromModelElement();
								}
							} else if ('process' == parentWindow.iDnD
									.getTransferObject().elementType) {
								var process = m_model.findProcess(parentWindow.iDnD
										.getTransferObject().attr.fullId);

								if (diagram.model.uuid != parentWindow.iDnD
										.getTransferObject().modelUUID
										&& !(process.processInterfaceType === m_constants.PROVIDES_PROCESS_INTERFACE_KEY)) {
									parentWindow.iDnD.hideIframe();
									m_messageDisplay.clear();
									m_messageDisplay
											.showMessage(m_i18nUtils
													.getProperty("modeler.diagram.message.noProcessInterfaceDnDNotAllowed"));
									return;
								}

								var activitySymbol = m_activitySymbol
										.createActivitySymbolFromProcess(
												diagram, process);

								activitySymbol.initialize(clickCoordinates.x
										- diagram.getCanvasPosition().left, clickCoordinates.y
										- diagram.getCanvasPosition().top);
								activitySymbol.refreshFromModelElement();
							} else if (m_elementConfiguration
									.isValidAppType(parentWindow.iDnD
											.getTransferObject().elementType)) {
								m_utils.debug("Dragged Application");
								m_utils
										.debug(parentWindow.iDnD.getTransferObject().attr.fullId);
								var application = m_model
										.findApplication(parentWindow.iDnD
												.getTransferObject().attr.fullId);

								m_utils.debug("Retrieved Application");
								m_utils.debug(application);

								if (isElementExternalAndPrivate(application)) {
									diaplayPrivateElementMessage();
									return;
								}

								var activitySymbol = m_activitySymbol
										.createActivitySymbolFromApplication(
												diagram, application);

								activitySymbol.initialize(clickCoordinates.x
										- diagram.getCanvasPosition().left, clickCoordinates.y
										- diagram.getCanvasPosition().top);
								activitySymbol.refreshFromModelElement();
							} else {
								m_messageDisplay
										.showErrorMessage("Unsupported element type: "
												+ parentWindow.iDnD
														.getTransferObject().elementType);
							}
						}

						parentWindow.iDnD.hideIframe();

						function isElementExternalAndPrivate(modelElement) {
							if (diagram.model.uuid != parentWindow.iDnD
									.getTransferObject().modelUUID
									&& !m_modelElementUtils.hasPublicVisibility(modelElement)) {
								return true;
							}

							return false;
						}

						function diaplayPrivateElementMessage() {
							parentWindow.iDnD.hideIframe();
							m_messageDisplay.clear();
							m_messageDisplay
									.showMessage(m_i18nUtils
											.getProperty("modeler.diagram.message.privateElementDnDNotAllowed"));

						}
					}			
				}
			}

			// === LOAD PROCESS ===>

//			function loadProcess() {
//				m_communicationController
//						.syncGetData(
//								{
//									url : getEndpointUrl() + "/models/"
//											+ modelId + "/process/" + processId
//											+ "/loadModel"
//								},
//								new function() {
//									return {
//										success : function(json) {
//											m_utils
//													.debug("=== Loading process definition from JSON ===>");
//											m_utils.debug(json)
//
//											// diagram.loadFromJson(json);
//
//											resetState();
//
//											m_utils
//													.debug("=== Process loaded ===>");
//										},
//										failure : function() {
//											alert('Hey');
//										}
//									}
//								});
//			}

			// <=== LOAD PROCESS ===

//			function resetState() {
//				allAnnotationsList.length = 0;
//				connections.length = 0;
//				connectionEnds.length = 0;
//				activityCounter = 0;
//			}
//
			function getEndpointUrl() {
				return require('bpm-modeler/js/m_urlUtils').getContextName()
						+ "/services/rest/bpm-modeler/modeler/" + new Date().getTime();
			};

//			function setupEventHandling(self) {
//				m_utils.jQuerySelect("#dialog-form").dialog({
//					autoOpen : false,
//					height : 70,
//					width : 350,
//					modal : true,
//					buttons : {
//						"Submit" : function() {
//							alert("Thanks");
//							m_utils.jQuerySelect(this).dialog("close");
//						}
//					}
//				});
//
//				m_utils.jQuerySelect(document).bind('ACTIVITY_NAMECHANGE',
//						function(event, data) {
//							m_communicationController.postData({
//								url : data.url
//							}, data.jsonData, new function() {
//								return {
//									success : function() {
//									},
//									failure : function() {
//									}
//								}
//							});
//						});
//
//				m_utils.jQuerySelect(document).bind(
//						"ELEMENT_ADDED",
//						function(event, data) {
//							if (typeof data.url != 'undefined') {
//								m_communicationController.postData({
//									url : data.url
//								}, data.jsonData, new function() {
//									return {
//										success : function(serverData) {
//											window.parent.EventHub.events
//													.publish("ELEMENT_CREATED",
//															serverData.id,
//															serverData.name,
//															serverData.type,
//															serverData.parent);
//										},
//										failure : function() {
//										}
//									}
//								});
//							}
//						});
//
//				m_utils.jQuerySelect(document).bind(
//						'ELEMENT_MODIFIED',
//						function(event, data) {
//							m_communicationController.postData({
//								url : data.url
//							}, data.jsonData, new function() {
//								return {
//									success : function(serverData) {
//										if ("Rename" == serverData.action) {
//											window.parent.EventHub.events
//													.publish("ELEMENT_RENAMED",
//															serverData);
//										}
//									},
//									failure : function() {
//									}
//								}
//							});
//						});
//
//				m_utils.jQuerySelect(document)
//						.bind(
//								'SHOW_PROPERTIES',
//								function(e) {
//									m_utils.jQuerySelect("#propertiesDialog")
//											.dialog(
//													"option",
//													"title",
//													currentlySelectedAnnotation.text.customProps.completetext);
//									m_utils.jQuerySelect('#propsForm input[name=elementId]')
//											.val(
//													currentlySelectedAnnotation.customProps.id);
//									m_utils.jQuerySelect('#propsForm input[name=elementName]')
//											.val(
//													currentlySelectedAnnotation.text.customProps.completetext);
//									m_utils.jQuerySelect('#propsForm input[name=propDesc]')
//											.val(
//													currentlySelectedAnnotation.customProps.description);
//									// m_utils.jQuerySelect('#propsForm
//									// input[name=propId]').val(currentlySelectedAnnotation.customProps.prop1);
//									// m_utils.jQuerySelect('#propsForm
//									// input[name=propName]').val(currentlySelectedAnnotation.customProps.prop2);
//									m_utils.jQuerySelect("#propertiesDialog").dialog("open");
//								});
//			}
//			;
//
//			function highlightAnnotation(element) {
//				deHighlightAnnotation();
//				annotationHighlighter = m_canvasManager.drawRectangle(element
//						.attr("x") - 4, element.attr("y") - 4, element
//						.attr("width") + 8, element.attr("height") + 8, {
//					'stroke' : '#DDD7D7',
//					'stroke-dasharray' : '- ',
//					'stroke-width' : 2
//				});
//			}
//
//			function deHighlightAnnotation(element) {
//				if (annotationHighlighter != undefined) {
//					annotationHighlighter.remove();
//					annotationHighlighter = null;
//				}
//			}
//
//			function getCurrentEditablesWidth() {
//				if (currentEditableAnnotation) {
//					return currentEditableAnnotation.attr('width');
//				} else {
//					return 50;
//				}
//			}
//
//			function getCurrentEditablesHeight() {
//				if (currentEditableAnnotation) {
//					return currentEditableAnnotation.attr('height');
//				} else {
//					return 50;
//				}
//			}

//			function drawApplicationActivity(x, y, width, height, text,
//					completeText, appId, action, attrs, id) {
//				var rect = m_canvasManager.drawRectangle(x, y, width, height, {
//					'fill' : activityDefaultColour,
//					'fill-opacity' : activityDefaultOpacity,
//					'stroke' : '#8E8989',
//					'stroke-width' : '1',
//					'r' : 4
//				});
//				rect.initZoom();
//				setOnclickSelectHandling(rect);
//				if (id == null) {
//					setUniqueId(rect);
//				} else {
//					rect.customProps = {
//						'id' : id
//					};
//				}
//				rect.customProps.appId = appId;
//
//				// Place the text node at the start of the rectangle.
//				var txtX = parseInt(x) + 60;
//				var txtY = parseInt(y) + (parseInt(height) / 2);
//				var txt = m_canvasManager.drawTextNode(txtX, txtY, text).attr({
//					'text-anchor' : 'start'
//				}).attr('font-size', 11);
//
//				if ('Create' == action) {
//					txt.zoom_memory["x"] = ((txtX - currentImage.attr('x')) / currentZoomLevel)
//							+ pannedByDXAggregated;
//					txt.zoom_memory["y"] = ((txtY - currentImage.attr('y')) / currentZoomLevel)
//							+ pannedByDYAggregated;
//				} else {
//					txt.initZoom();
//				}
//				txt.customProps = {
//					'completetext' : completeText
//				};
//
//				var icon = m_canvasManager.drawImageAt(
//						"plugins/bpm-modeler/images/icons/activity_application.gif",
//						parseInt(x) + 5, parseInt(y) + 5, 20, 16);
//
//				setId(txt, rect.customProps.id);
//				rect.text = txt;
//				setOnclickHandlingForText(txt, rect);
//
//				rect.icon = icon;
//				// for property pages
//				icon.parent = rect;
//				setId(icon, rect.customProps.id);
//				makeAnnotationEditable(rect);
//
//				/*
//				 * m_utils.jQuerySelect(icon.node).click(function(e) { //console.log("Id from
//				 * Icon = ", icon.customProps.id);
//				 * m_utils.jQuerySelect(document).trigger('ACTIVITY_SELECTED', {element:
//				 * icon.parent}); });
//				 */
//
//				rect.customProps.annotSet = m_canvasManager.getNewSet();
//				rect.customProps.annotSet.push(rect, txt, icon);
//				rect.customProps.type = "applicationActivity";
//				rect.customProps.accessPoint = attrs.accessPoint;
//				// allObjects.push(rect, txt, circ);
//				var activityCreationCmd = createCommandObject(rect,
//						"applicationActivity", action, txt);
//
//				makeAnnotationMovableStretchable(rect);
//
//				m_commandsController.handleCommand(activityCreationCmd);
//
//				rect.customProps.hideAnnotation = function() {
//					// rect.stretchHandle.hide();
//					rect.customProps.annotSet.hide();
//				}
//
//				rect.customProps.showAnnotation = function() {
//					// rect.stretchHandle.show();
//					rect.customProps.annotSet.show();
//				}
//
//				rect.customProps.moveStart = function() {
//					rect.startX = rect.attr('x');
//					rect.startY = rect.attr('y');
//					// rect.stretchHandle.startX =
//					// rect.stretchHandle.attr('cx');
//					// rect.stretchHandle.startY =
//					// rect.stretchHandle.attr('cy');
//					rect.text.startX = rect.text.attr('x');
//					rect.text.startY = rect.text.attr('y');
//
//					rect.icon.startX = rect.icon.attr('x');
//					rect.icon.startY = rect.icon.attr('y');
//				}
//
//				rect.customProps.moveAnnotation = function(dx, dy) {
//					rect.customProps.isMoved = true;
//					var delta = getRotationQualifiedDeltas(dx, dy);
//					dx = delta.x;
//					dy = delta.y;
//
//					if (dx != 0) {
//						rect.customProps.moveAnnotationX(dx);
//					}
//					if (dy != 0) {
//						rect.customProps.moveAnnotationY(dy);
//					}
//				}
//
//				rect.customProps.moveAnnotationX = function(dx) {
//					rect.attr({
//						x : rect.startX + dx
//					});
//					/*
//					 * rect.stretchHandle.attr({ cx : rect.stretchHandle.startX +
//					 * dx });
//					 */
//					rect.text.attr({
//						x : rect.text.startX + dx
//					});
//
//					rect.icon.attr({
//						x : rect.icon.startX + dx
//					});
//				}
//
//				rect.customProps.moveAnnotationY = function(dy) {
//					rect.attr({
//						y : rect.startY + dy
//					});
//					/*
//					 * rect.stretchHandle.attr({ cy : rect.stretchHandle.startY +
//					 * dy });
//					 */
//					rect.text.attr({
//						y : rect.text.startY + dy
//					});
//					rect.icon.attr({
//						y : rect.icon.startY + dy
//					});
//				};
//
//				rect.customProps.moveStop = function() {
//					if (rect.customProps.isMoved == true) {
//						rect.customProps.isMoved = false;
//						executeMoveStop(rect);
//
//						rect.text.zoom_memory["x"] = ((rect.text.attr('x') - currentImage
//								.attr('x')) / currentZoomLevel)
//								+ pannedByDXAggregated;
//						rect.text.zoom_memory["y"] = ((rect.text.attr('y') - currentImage
//								.attr('y')) / currentZoomLevel)
//								+ pannedByDYAggregated;
//					}
//				};
//
//				rect.customProps.remove = function() {
//					deHighlightAnnotation();
//					removeFromCollection(allAnnotationsList, rect);
//
//					if (rect.text) {
//						rect.text.remove();
//					}
//					if (rect.icon) {
//						rect.icon.remove();
//					}
//
//					rect.remove();
//				};
//
//				/*
//				 * Make adjustments to the memory attributes (x, y) of text as
//				 * mentioned above. This is done to adjust the dx, dy caused by
//				 * panning.
//				 */
//				rect.customProps.panStop = function(pannedByDX, pannedByDY) {
//					rect.text.zoom_memory["x"] = rect.text.zoom_memory["x"]
//							+ pannedByDX;
//					rect.text.zoom_memory["y"] = rect.text.zoom_memory["y"]
//							+ pannedByDY;
//				}
//
//				allAnnotationsList.push(rect);
//				return rect;
//			}
//
//			function drawSubProcessActivity(x, y, width, height, text,
//					completeText, procId, action, id) {
//				var rect = m_canvasManager.drawRectangle(x, y, width, height, {
//					'fill' : activityDefaultColour,
//					'fill-opacity' : activityDefaultOpacity,
//					'stroke' : '#8E8989',
//					'stroke-width' : '1',
//					'r' : 4
//				});
//				rect.initZoom();
//				setOnclickSelectHandling(rect);
//				if (id == null) {
//					setUniqueId(rect);
//				} else {
//					rect.customProps = {
//						'id' : id
//					};
//				}
//				rect.customProps.subProcId = procId;
//
//				// Place the text node at the start of the rectangle.
//				var txtX = parseInt(x) + 60;
//				var txtY = parseInt(y) + (parseInt(height) / 2);
//				var txt = m_canvasManager.drawTextNode(txtX, txtY, text).attr({
//					'text-anchor' : 'start'
//				}).attr('font-size', 11);
//
//				if ('Create' == action) {
//					txt.zoom_memory["x"] = ((txtX - currentImage.attr('x')) / currentZoomLevel)
//							+ pannedByDXAggregated;
//					txt.zoom_memory["y"] = ((txtY - currentImage.attr('y')) / currentZoomLevel)
//							+ pannedByDYAggregated;
//				} else {
//					txt.initZoom();
//				}
//				txt.customProps = {
//					'completetext' : completeText
//				};
//
//				var icon = m_canvasManager.drawImageAt(
//						"plugins/bpm-modeler/images/icons/activity-subprocess.png",
//						parseInt(x) + 5, parseInt(y) + 5, 20, 16);
//
//				setId(txt, rect.customProps.id);
//				rect.text = txt;
//				setOnclickHandlingForText(txt, rect);
//
//				rect.icon = icon;
//				// for property pages
//				icon.parent = rect;
//				setId(icon, rect.customProps.id);
//				makeAnnotationEditable(rect);
//
//				/*
//				 * m_utils.jQuerySelect(icon.node).click(function(e) { //console.log("Id from
//				 * Icon = ", icon.customProps.id);
//				 * m_utils.jQuerySelect(document).trigger('ACTIVITY_SELECTED', {element:
//				 * icon.parent}); });
//				 */
//
//				rect.customProps.annotSet = m_canvasManager.getNewSet();
//				rect.customProps.annotSet.push(rect, txt, icon);
//				rect.customProps.type = "activity";
//
//				// allObjects.push(rect, txt, circ);
//				var activityCreationCmd = createCommandObject(rect,
//						'subProcessActivity', action, txt);
//
//				makeAnnotationMovableStretchable(rect);
//
//				m_commandsController.handleCommand(activityCreationCmd);
//
//				rect.customProps.hideAnnotation = function() {
//					// rect.stretchHandle.hide();
//					rect.customProps.annotSet.hide();
//				}
//
//				rect.customProps.showAnnotation = function() {
//					// rect.stretchHandle.show();
//					rect.customProps.annotSet.show();
//				}
//
//				rect.customProps.moveStart = function() {
//					rect.startX = rect.attr('x');
//					rect.startY = rect.attr('y');
//					// rect.stretchHandle.startX =
//					// rect.stretchHandle.attr('cx');
//					// rect.stretchHandle.startY =
//					// rect.stretchHandle.attr('cy');
//					rect.text.startX = rect.text.attr('x');
//					rect.text.startY = rect.text.attr('y');
//
//					rect.icon.startX = rect.icon.attr('x');
//					rect.icon.startY = rect.icon.attr('y');
//				}
//
//				rect.customProps.moveAnnotation = function(dx, dy) {
//					rect.customProps.isMoved = true;
//					var delta = getRotationQualifiedDeltas(dx, dy);
//					dx = delta.x;
//					dy = delta.y;
//
//					if (dx != 0) {
//						rect.customProps.moveAnnotationX(dx);
//					}
//					if (dy != 0) {
//						rect.customProps.moveAnnotationY(dy);
//					}
//				}
//
//				rect.customProps.moveAnnotationX = function(dx) {
//					rect.attr({
//						x : rect.startX + dx
//					});
//					/*
//					 * rect.stretchHandle.attr({ cx : rect.stretchHandle.startX +
//					 * dx });
//					 */
//					rect.text.attr({
//						x : rect.text.startX + dx
//					});
//
//					rect.icon.attr({
//						x : rect.icon.startX + dx
//					});
//				}
//
//				rect.customProps.moveAnnotationY = function(dy) {
//					rect.attr({
//						y : rect.startY + dy
//					});
//					/*
//					 * rect.stretchHandle.attr({ cy : rect.stretchHandle.startY +
//					 * dy });
//					 */
//					rect.text.attr({
//						y : rect.text.startY + dy
//					});
//					rect.icon.attr({
//						y : rect.icon.startY + dy
//					});
//				};
//
//				rect.customProps.moveStop = function() {
//					if (rect.customProps.isMoved == true) {
//						rect.customProps.isMoved = false;
//						executeMoveStop(rect);
//
//						rect.text.zoom_memory["x"] = ((rect.text.attr('x') - currentImage
//								.attr('x')) / currentZoomLevel)
//								+ pannedByDXAggregated;
//						rect.text.zoom_memory["y"] = ((rect.text.attr('y') - currentImage
//								.attr('y')) / currentZoomLevel)
//								+ pannedByDYAggregated;
//					}
//				};
//
//				rect.customProps.remove = function() {
//					deHighlightAnnotation();
//					removeFromCollection(allAnnotationsList, rect);
//
//					if (rect.text) {
//						rect.text.remove();
//					}
//					if (rect.icon) {
//						rect.icon.remove();
//					}
//
//					rect.remove();
//				};
//
//				/*
//				 * Make adjustments to the memory attributes (x, y) of text as
//				 * mentioned above. This is done to adjust the dx, dy caused by
//				 * panning.
//				 */
//				rect.customProps.panStop = function(pannedByDX, pannedByDY) {
//					rect.text.zoom_memory["x"] = rect.text.zoom_memory["x"]
//							+ pannedByDX;
//					rect.text.zoom_memory["y"] = rect.text.zoom_memory["y"]
//							+ pannedByDY;
//				}
//
//				allAnnotationsList.push(rect);
//				return rect;
//			}
//
//			function drawActivity(x, y, width, height, text, completeText,
//					action, attrs) {
//				var rect = m_canvasManager.drawRectangle(x, y, width, height, {
//					'fill' : activityDefaultColour,
//					'fill-opacity' : activityDefaultOpacity,
//					'stroke' : '#8E8989',
//					'stroke-width' : '1',
//					'r' : 4
//				});
//
//				rect.initZoom();
//
//				// Set event handling
//
//				setOnclickSelectHandling(rect);
//
//				if (attrs == null || attrs.id == null) {
//					setUniqueId(rect);
//				} else {
//					rect.customProps = {
//						'id' : attrs.id
//					};
//				}
//
//				setDescription(rect, attrs);
//
//				// Place the text node at the start of the rectangle.
//
//				var txtX = parseInt(x) + 60;
//				var txtY = parseInt(y) + (parseInt(height) / 2);
//				var txt = m_canvasManager.drawTextNode(txtX, txtY, text).attr({
//					'text-anchor' : 'start'
//				}).attr('font-size', 11);
//
//				if ('Create' == action) {
//					txt.zoom_memory["x"] = ((txtX - currentImage.attr('x')) / currentZoomLevel)
//							+ pannedByDXAggregated;
//					txt.zoom_memory["y"] = ((txtY - currentImage.attr('y')) / currentZoomLevel)
//							+ pannedByDYAggregated;
//				} else {
//					txt.initZoom();
//				}
//
//				txt.customProps = {
//					'completetext' : completeText
//				};
//
//				var icon = m_canvasManager.drawImageAt(
//						"plugins/bpm-modeler/images/icons/activity_manual.png",
//						parseInt(x) + 5, parseInt(y) + 5, 20, 16);
//
//				setId(txt, rect.customProps.id);
//				rect.text = txt;
//				setOnclickHandlingForText(txt, rect);
//
//				rect.icon = icon;
//
//				// for property pages
//
//				icon.parent = rect;
//				setId(icon, rect.customProps.id);
//				makeAnnotationEditable(rect);
//
//				/*
//				 * m_utils.jQuerySelect(icon.node).click(function(e) { //console.log("Id from
//				 * Icon = ", icon.customProps.id);
//				 * m_utils.jQuerySelect(document).trigger('ACTIVITY_SELECTED', {element:
//				 * icon.parent}); });
//				 */
//
//				rect.customProps.annotSet = m_canvasManager.getNewSet();
//				rect.customProps.annotSet.push(rect, txt, icon);
//				rect.customProps.type = "activity";
//
//				// allObjects.push(rect, txt, circ);
//				var activityCreationCmd = createCommandObject(rect, 'activity',
//						action, txt);
//
//				makeAnnotationMovableStretchable(rect);
//
//				m_commandsController.handleCommand(activityCreationCmd);
//
//				rect.customProps.hideAnnotation = function() {
//					// rect.stretchHandle.hide();
//					rect.customProps.annotSet.hide();
//				}
//
//				rect.customProps.showAnnotation = function() {
//					// rect.stretchHandle.show();
//					rect.customProps.annotSet.show();
//				}
//
//				rect.customProps.moveStart = function() {
//					rect.startX = rect.attr('x');
//					rect.startY = rect.attr('y');
//					// rect.stretchHandle.startX =
//					// rect.stretchHandle.attr('cx');
//					// rect.stretchHandle.startY =
//					// rect.stretchHandle.attr('cy');
//					rect.text.startX = rect.text.attr('x');
//					rect.text.startY = rect.text.attr('y');
//
//					rect.icon.startX = rect.icon.attr('x');
//					rect.icon.startY = rect.icon.attr('y');
//				}
//
//				rect.customProps.moveAnnotation = function(dx, dy) {
//					rect.customProps.isMoved = true;
//					var delta = getRotationQualifiedDeltas(dx, dy);
//					dx = delta.x;
//					dy = delta.y;
//
//					if (dx != 0) {
//						rect.customProps.moveAnnotationX(dx);
//					}
//					if (dy != 0) {
//						rect.customProps.moveAnnotationY(dy);
//					}
//				}
//
//				rect.customProps.moveAnnotationX = function(dx) {
//					rect.attr({
//						x : rect.startX + dx
//					});
//					/*
//					 * rect.stretchHandle.attr({ cx : rect.stretchHandle.startX +
//					 * dx });
//					 */
//					rect.text.attr({
//						x : rect.text.startX + dx
//					});
//
//					rect.icon.attr({
//						x : rect.icon.startX + dx
//					});
//				}
//
//				rect.customProps.moveAnnotationY = function(dy) {
//					rect.attr({
//						y : rect.startY + dy
//					});
//					/*
//					 * rect.stretchHandle.attr({ cy : rect.stretchHandle.startY +
//					 * dy });
//					 */
//					rect.text.attr({
//						y : rect.text.startY + dy
//					});
//					rect.icon.attr({
//						y : rect.icon.startY + dy
//					});
//				};
//
//				rect.customProps.moveStop = function() {
//					if (rect.customProps.isMoved == true) {
//						rect.customProps.isMoved = false;
//						executeMoveStop(rect);
//
//						rect.text.zoom_memory["x"] = ((rect.text.attr('x') - currentImage
//								.attr('x')) / currentZoomLevel)
//								+ pannedByDXAggregated;
//						rect.text.zoom_memory["y"] = ((rect.text.attr('y') - currentImage
//								.attr('y')) / currentZoomLevel)
//								+ pannedByDYAggregated;
//					}
//				};
//
//				rect.customProps.remove = function() {
//					deHighlightAnnotation();
//					removeFromCollection(allAnnotationsList, rect);
//
//					if (rect.text) {
//						rect.text.remove();
//					}
//					if (rect.icon) {
//						rect.icon.remove();
//					}
//
//					rect.remove();
//				};
//
//				/*
//				 * Make adjustments to the memory attributes (x, y) of text as
//				 * mentioned above. This is done to adjust the dx, dy caused by
//				 * panning.
//				 */
//				rect.customProps.panStop = function(pannedByDX, pannedByDY) {
//					rect.text.zoom_memory["x"] = rect.text.zoom_memory["x"]
//							+ pannedByDX;
//					rect.text.zoom_memory["y"] = rect.text.zoom_memory["y"]
//							+ pannedByDY;
//				}
//
//				allAnnotationsList.push(rect);
//				return rect;
//			}
//
//			function removeFromCollection(collection, element) {
//				jQuery.each(collection, function(id, value) {
//					if (value != null
//							&& value.customProps.id == element.customProps.id) {
//						collection.splice(id, 1);
//					}
//				});
//			}

//			function drawRoleSymbol(x, y, width, height, text, action, attrs,
//					id) {
//				var role = m_canvasManager.drawRectangle(x, y, width, height, {
//					'fill' : 'silver',
//					'fill-opacity' : '0.5',
//					'stroke' : 'silver',
//					'stroke-width' : '1'
//				});
//				role.initZoom();
//				setOnclickSelectHandling(role);
//				if (id == null) {
//					setUniqueId(role);
//				} else {
//					role.customProps = {
//						'id' : id
//					};
//				}
//				// Place the text node at the middle.
//				var txtX = parseInt(x) + 12;
//				var txtY = parseInt(y) + 30;
//				var txt = m_canvasManager.drawTextNode(txtX, txtY, text).attr({
//					'text-anchor' : 'start'
//				}).attr('font-size', 11);
//
//				if ('Create' == action) {
//					txt.zoom_memory["x"] = ((txtX - currentImage.attr('x')) / currentZoomLevel)
//							+ pannedByDXAggregated;
//					txt.zoom_memory["y"] = ((txtY - currentImage.attr('y')) / currentZoomLevel)
//							+ pannedByDYAggregated;
//				} else {
//					txt.initZoom();
//				}
//				txt.customProps = {
//					'completetext' : text
//				};
//
//				var icon = m_canvasManager.drawImageAt(
//						"plugins/bpm-modeler/images/icons/role.png", x + 17, y + 1, 20, 16);
//
//				setId(txt, role.customProps.id);
//				role.text = txt;
//				setOnclickHandlingForText(txt, role);
//
//				role.icon = icon;
//				makeAnnotationEditable(role);
//
//				/*
//				 * var circ = m_canvasManager.drawCircle(x + width, y + height,
//				 * 3, {fill : 'black'}); circ.customProps = {}; circ.initZoom();
//				 * makeElementEventOpaque(circ); circ.stretchable = rect;
//				 * rect.stretchHandle = circ;
//				 */
//
//				role.customProps.annotSet = m_canvasManager.getNewSet();
//				role.customProps.annotSet.push(role, txt, icon);
//
//				role.customProps.type = "roleSymbol";
//
//				if (attrs.roleId) {
//					role.customProps.roleId = attrs.roleId;
//				}
//
//				// allObjects.push(rect, txt, circ);
//				var roleCreationCmd = createCommandObject(role,
//						role.customProps.type, action, txt);
//
//				makeAnnotationMovableStretchable(role);
//
//				m_commandsController.handleCommand(roleCreationCmd);
//
//				role.customProps.hideAnnotation = function() {
//					role.customProps.annotSet.hide();
//				}
//
//				role.customProps.showAnnotation = function() {
//					role.customProps.annotSet.show();
//				}
//
//				role.customProps.moveStart = function() {
//					role.startX = role.attr('x');
//					role.startY = role.attr('y');
//					role.text.startX = role.text.attr('x');
//					role.text.startY = role.text.attr('y');
//
//					role.icon.startX = role.icon.attr('x');
//					role.icon.startY = role.icon.attr('y');
//				}
//
//				role.customProps.moveAnnotation = function(dx, dy) {
//					role.customProps.isMoved = true;
//					var delta = getRotationQualifiedDeltas(dx, dy);
//					dx = delta.x;
//					dy = delta.y;
//
//					if (dx != 0) {
//						role.customProps.moveAnnotationX(dx);
//					}
//					if (dy != 0) {
//						role.customProps.moveAnnotationY(dy);
//					}
//				}
//
//				role.customProps.moveAnnotationX = function(dx) {
//					role.attr({
//						x : role.startX + dx
//					});
//					role.text.attr({
//						x : role.text.startX + dx
//					});
//					role.icon.attr({
//						x : role.icon.startX + dx
//					});
//				}
//
//				role.customProps.moveAnnotationY = function(dy) {
//					role.attr({
//						y : role.startY + dy
//					});
//					role.text.attr({
//						y : role.text.startY + dy
//					});
//					role.icon.attr({
//						y : role.icon.startY + dy
//					});
//				}
//
//				role.customProps.moveStop = function() {
//					if (role.customProps.isMoved == true) {
//						role.customProps.isMoved = false;
//						executeMoveStop(role);
//
//						role.text.zoom_memory["x"] = ((role.text.attr('x') - currentImage
//								.attr('x')) / currentZoomLevel)
//								+ pannedByDXAggregated;
//						role.text.zoom_memory["y"] = ((role.text.attr('y') - currentImage
//								.attr('y')) / currentZoomLevel)
//								+ pannedByDYAggregated;
//					}
//				}
//
//				/*
//				 * Make adjustments to the memory attributes (x, y) of text as
//				 * mentioned above. This is done to adjust the dx, dy caused by
//				 * panning.
//				 */
//				role.customProps.panStop = function(pannedByDX, pannedByDY) {
//					role.text.zoom_memory["x"] = role.text.zoom_memory["x"]
//							+ pannedByDX;
//					role.text.zoom_memory["y"] = role.text.zoom_memory["y"]
//							+ pannedByDY;
//				}
//
//				allAnnotationsList.push(role);
//				return role;
//			}
//
//			function drawRole(x, y, width, height, text, action, attrs) {
//				var role = m_canvasManager.drawRectangle(x, y, width, height, {
//					'fill' : 'silver',
//					'fill-opacity' : '0.5',
//					'stroke' : 'silver',
//					'stroke-width' : '1'
//				});
//				role.initZoom();
//				setOnclickSelectHandling(role);
//				if (attrs == null || attrs.id == null) {
//					setUniqueId(role);
//				} else {
//					role.customProps = {
//						'id' : attrs.id
//					};
//				}
//
//				setDescription(role, attrs);
//
//				// Place the text node at the middle.
//				var txtX = parseInt(x) + 12;
//				var txtY = parseInt(y) + 30;
//				var txt = m_canvasManager.drawTextNode(txtX, txtY, text).attr({
//					'text-anchor' : 'start'
//				}).attr('font-size', 11);
//
//				if ('Create' == action) {
//					txt.zoom_memory["x"] = ((txtX - currentImage.attr('x')) / currentZoomLevel)
//							+ pannedByDXAggregated;
//					txt.zoom_memory["y"] = ((txtY - currentImage.attr('y')) / currentZoomLevel)
//							+ pannedByDYAggregated;
//				} else {
//					txt.initZoom();
//				}
//				txt.customProps = {
//					'completetext' : text
//				};
//
//				var icon = m_canvasManager.drawImageAt(
//						"plugins/bpm-modeler/images/icons/role.png", x + 17, y + 1, 20, 16);
//
//				setId(txt, role.customProps.id);
//				role.text = txt;
//				setOnclickHandlingForText(txt, role);
//
//				role.icon = icon;
//				makeAnnotationEditable(role);
//
//				/*
//				 * var circ = m_canvasManager.drawCircle(x + width, y + height,
//				 * 3, {fill : 'black'}); circ.customProps = {}; circ.initZoom();
//				 * makeElementEventOpaque(circ); circ.stretchable = rect;
//				 * rect.stretchHandle = circ;
//				 */
//
//				role.customProps.annotSet = m_canvasManager.getNewSet();
//				role.customProps.annotSet.push(role, txt, icon);
//
//				role.customProps.type = "role";
//
//				// allObjects.push(rect, txt, circ);
//				var roleCreationCmd = createCommandObject(role, 'role', action,
//						txt);
//
//				makeAnnotationMovableStretchable(role);
//
//				m_commandsController.handleCommand(roleCreationCmd);
//
//				role.customProps.hideAnnotation = function() {
//					role.customProps.annotSet.hide();
//				}
//
//				role.customProps.showAnnotation = function() {
//					role.customProps.annotSet.show();
//				}
//
//				role.customProps.moveStart = function() {
//					role.startX = role.attr('x');
//					role.startY = role.attr('y');
//					role.text.startX = role.text.attr('x');
//					role.text.startY = role.text.attr('y');
//
//					role.icon.startX = role.icon.attr('x');
//					role.icon.startY = role.icon.attr('y');
//				}
//
//				role.customProps.moveAnnotation = function(dx, dy) {
//					role.customProps.isMoved = true;
//					var delta = getRotationQualifiedDeltas(dx, dy);
//					dx = delta.x;
//					dy = delta.y;
//
//					if (dx != 0) {
//						role.customProps.moveAnnotationX(dx);
//					}
//					if (dy != 0) {
//						role.customProps.moveAnnotationY(dy);
//					}
//				}
//
//				role.customProps.moveAnnotationX = function(dx) {
//					role.attr({
//						x : role.startX + dx
//					});
//					role.text.attr({
//						x : role.text.startX + dx
//					});
//					role.icon.attr({
//						x : role.icon.startX + dx
//					});
//				}
//
//				role.customProps.moveAnnotationY = function(dy) {
//					role.attr({
//						y : role.startY + dy
//					});
//					role.text.attr({
//						y : role.text.startY + dy
//					});
//					role.icon.attr({
//						y : role.icon.startY + dy
//					});
//				}
//
//				role.customProps.moveStop = function() {
//					if (role.customProps.isMoved == true) {
//						role.customProps.isMoved = false;
//						executeMoveStop(role);
//
//						role.text.zoom_memory["x"] = ((role.text.attr('x') - currentImage
//								.attr('x')) / currentZoomLevel)
//								+ pannedByDXAggregated;
//						role.text.zoom_memory["y"] = ((role.text.attr('y') - currentImage
//								.attr('y')) / currentZoomLevel)
//								+ pannedByDYAggregated;
//					}
//				}
//
//				/*
//				 * Make adjustments to the memory attributes (x, y) of text as
//				 * mentioned above. This is done to adjust the dx, dy caused by
//				 * panning.
//				 */
//				role.customProps.panStop = function(pannedByDX, pannedByDY) {
//					role.text.zoom_memory["x"] = role.text.zoom_memory["x"]
//							+ pannedByDX;
//					role.text.zoom_memory["y"] = role.text.zoom_memory["y"]
//							+ pannedByDY;
//				}
//
//				allAnnotationsList.push(role);
//				return role;
//			}
//
//			function drawDataSymbol(x, y, width, height, text, action, attrs,
//					id) {
//				var datatype = m_canvasManager.drawRectangle(x, y, width,
//						height, {
//							'fill' : 'silver',
//							'fill-opacity' : '0.5',
//							'stroke' : 'silver',
//							'stroke-width' : '1'
//						});
//				datatype.initZoom();
//				setOnclickSelectHandling(datatype);
//				if (id == null) {
//					setUniqueId(datatype);
//				} else {
//					datatype.customProps = {
//						'id' : id
//					};
//				}
//				datatype.customProps.elementId = datatype.customProps.id;
//
//				// Place the text node at the middle.
//				var txtX = parseInt(x) + 12;
//				var txtY = parseInt(y) + 30;
//				var txt = m_canvasManager.drawTextNode(txtX, txtY, text).attr({
//					'text-anchor' : 'start'
//				}).attr('font-size', 11);
//
//				if ('Create' == action) {
//					txt.zoom_memory["x"] = ((txtX - currentImage.attr('x')) / currentZoomLevel)
//							+ pannedByDXAggregated;
//					txt.zoom_memory["y"] = ((txtY - currentImage.attr('y')) / currentZoomLevel)
//							+ pannedByDYAggregated;
//				} else {
//					txt.initZoom();
//				}
//				txt.customProps = {
//					'completetext' : text
//				};
//				// Default data icon
//				var icon = m_canvasManager.drawImageAt(
//						"plugins/bpm-modeler/images/icons/data.png", x + 17, y + 1, 20,
//						16);
//
//				datatype.customProps.type = "dataSymbol";
//				// Overwrite the icon for specific data types
//				if (attrs.dataTypeType) {
//					if ('Primitive_Data' == attrs.dataTypeType) {
//						var icon = m_canvasManager.drawImageAt(
//								"plugins/bpm-modeler/images/icons/data.png", x + 17,
//								y + 1, 20, 16);
//					} else if ('Structured_Data' == attrs.dataTypeType) {
//						var icon = m_canvasManager.drawImageAt(
//								"plugins/bpm-modeler/images/icons/data-structured.png", x + 17,
//								y + 1, 20, 16);
//					}
//				}
//
//				if (attrs.dataTypeId) {
//					datatype.customProps.dataId = attrs.dataTypeId;
//				}
//
//				setId(txt, datatype.customProps.id);
//				datatype.text = txt;
//				setOnclickHandlingForText(txt, datatype);
//				datatype.icon = icon;
//				makeAnnotationEditable(datatype);
//				datatype.customProps.annotSet = m_canvasManager.getNewSet();
//				datatype.customProps.annotSet.push(datatype, txt, icon);
//				// datatype.customProps.type = "primitiveDataType";
//				var datatypeCreationCmd = createCommandObject(datatype,
//						datatype.customProps.type, action, txt);
//				makeAnnotationMovableStretchable(datatype);
//				m_commandsController.handleCommand(datatypeCreationCmd);
//
//				datatype.customProps.hideAnnotation = function() {
//					datatype.customProps.annotSet.hide();
//				}
//
//				datatype.customProps.showAnnotation = function() {
//					datatype.customProps.annotSet.show();
//				}
//
//				datatype.customProps.moveStart = function() {
//					datatype.startX = datatype.attr('x');
//					datatype.startY = datatype.attr('y');
//					datatype.text.startX = datatype.text.attr('x');
//					datatype.text.startY = datatype.text.attr('y');
//
//					datatype.icon.startX = datatype.icon.attr('x');
//					datatype.icon.startY = datatype.icon.attr('y');
//				}
//
//				datatype.customProps.moveAnnotation = function(dx, dy) {
//					datatype.customProps.isMoved = true;
//					var delta = getRotationQualifiedDeltas(dx, dy);
//					dx = delta.x;
//					dy = delta.y;
//
//					if (dx != 0) {
//						datatype.customProps.moveAnnotationX(dx);
//					}
//					if (dy != 0) {
//						datatype.customProps.moveAnnotationY(dy);
//					}
//				}
//
//				datatype.customProps.moveAnnotationX = function(dx) {
//					datatype.attr({
//						x : datatype.startX + dx
//					});
//					datatype.text.attr({
//						x : datatype.text.startX + dx
//					});
//					datatype.icon.attr({
//						x : datatype.icon.startX + dx
//					});
//				}
//
//				datatype.customProps.moveAnnotationY = function(dy) {
//					datatype.attr({
//						y : datatype.startY + dy
//					});
//					datatype.text.attr({
//						y : datatype.text.startY + dy
//					});
//					datatype.icon.attr({
//						y : datatype.icon.startY + dy
//					});
//				}
//
//				datatype.customProps.moveStop = function() {
//					if (datatype.customProps.isMoved == true) {
//						datatype.customProps.isMoved = false;
//						executeMoveStop(datatype);
//
//						datatype.text.zoom_memory["x"] = ((datatype.text
//								.attr('x') - currentImage.attr('x')) / currentZoomLevel)
//								+ pannedByDXAggregated;
//						datatype.text.zoom_memory["y"] = ((datatype.text
//								.attr('y') - currentImage.attr('y')) / currentZoomLevel)
//								+ pannedByDYAggregated;
//					}
//				}
//
//				/*
//				 * Make adjustments to the memory attributes (x, y) of text as
//				 * mentioned above. This is done to adjust the dx, dy caused by
//				 * panning.
//				 */
//				datatype.customProps.panStop = function(pannedByDX, pannedByDY) {
//					datatype.text.zoom_memory["x"] = datatype.text.zoom_memory["x"]
//							+ pannedByDX;
//					datatype.text.zoom_memory["y"] = datatype.text.zoom_memory["y"]
//							+ pannedByDY;
//				}
//
//				allAnnotationsList.push(datatype);
//				return datatype;
//			}
//
//			function drawDatatype(x, y, width, height, text, action, attrs) {
//				var datatype = m_canvasManager.drawRectangle(x, y, width,
//						height, {
//							'fill' : 'silver',
//							'fill-opacity' : '0.5',
//							'stroke' : 'silver',
//							'stroke-width' : '1'
//						});
//				datatype.initZoom();
//				setOnclickSelectHandling(datatype);
//				if (attrs == null || attrs.id == null) {
//					setUniqueId(datatype);
//				} else {
//					datatype.customProps = {
//						'id' : attrs.id
//					};
//				}
//				datatype.customProps.elementId = datatype.customProps.id;
//
//				setDescription(datatype, attrs);
//
//				// Place the text node at the middle.
//				var txtX = parseInt(x) + 12;
//				var txtY = parseInt(y) + 30;
//				var txt = m_canvasManager.drawTextNode(txtX, txtY, text).attr({
//					'text-anchor' : 'start'
//				}).attr('font-size', 11);
//
//				if ('Create' == action) {
//					txt.zoom_memory["x"] = ((txtX - currentImage.attr('x')) / currentZoomLevel)
//							+ pannedByDXAggregated;
//					txt.zoom_memory["y"] = ((txtY - currentImage.attr('y')) / currentZoomLevel)
//							+ pannedByDYAggregated;
//				} else {
//					txt.initZoom();
//				}
//				txt.customProps = {
//					'completetext' : text
//				};
//				// Default data icon
//				var icon = m_canvasManager.drawImageAt(
//						"plugins/bpm-modeler/images/icons/data.png", x + 17, y + 1, 20,
//						16);
//				// Overwrite the icon for specific data types
//				if (attrs.dataTypeType) {
//					if ('Primitive_Data' == attrs.dataTypeType
//							|| 'primitiveDataType' == attrs.dataTypeType) {
//						var icon = m_canvasManager.drawImageAt(
//								"plugins/bpm-modeler/images/icons/data.png", x + 17,
//								y + 1, 20, 16);
//						datatype.customProps.type = "primitiveDataType";
//					} else if ('Structured_Data' == attrs.dataTypeType
//							|| 'structuredDataType' == attrs.dataTypeType) {
//						var icon = m_canvasManager.drawImageAt(
//								"plugins/bpm-modeler/images/icons/data-structured.png", x + 17,
//								y + 1, 20, 16);
//						datatype.customProps.type = "structuredDataType";
//					}
//				} else {
//					// TODO Default value - get rid of this
//					datatype.customProps.type = "primitiveDataType";
//				}
//				if (attrs.dataTypeSubType) {
//					datatype.customProps.dataSubType = attrs.dataTypeSubType;
//				}
//
//				setId(txt, datatype.customProps.id);
//				datatype.text = txt;
//				setOnclickHandlingForText(txt, datatype);
//				datatype.icon = icon;
//				makeAnnotationEditable(datatype);
//				datatype.customProps.annotSet = m_canvasManager.getNewSet();
//				datatype.customProps.annotSet.push(datatype, txt, icon);
//				// datatype.customProps.type = "primitiveDataType";
//				var datatypeCreationCmd = createCommandObject(datatype,
//						datatype.customProps.type, action, txt);
//				makeAnnotationMovableStretchable(datatype);
//				m_commandsController.handleCommand(datatypeCreationCmd);
//
//				datatype.customProps.hideAnnotation = function() {
//					datatype.customProps.annotSet.hide();
//				}
//
//				datatype.customProps.showAnnotation = function() {
//					datatype.customProps.annotSet.show();
//				}
//
//				datatype.customProps.moveStart = function() {
//					datatype.startX = datatype.attr('x');
//					datatype.startY = datatype.attr('y');
//					datatype.text.startX = datatype.text.attr('x');
//					datatype.text.startY = datatype.text.attr('y');
//
//					datatype.icon.startX = datatype.icon.attr('x');
//					datatype.icon.startY = datatype.icon.attr('y');
//				}
//
//				datatype.customProps.moveAnnotation = function(dx, dy) {
//					datatype.customProps.isMoved = true;
//					var delta = getRotationQualifiedDeltas(dx, dy);
//					dx = delta.x;
//					dy = delta.y;
//
//					if (dx != 0) {
//						datatype.customProps.moveAnnotationX(dx);
//					}
//					if (dy != 0) {
//						datatype.customProps.moveAnnotationY(dy);
//					}
//				}
//
//				datatype.customProps.moveAnnotationX = function(dx) {
//					datatype.attr({
//						x : datatype.startX + dx
//					});
//					datatype.text.attr({
//						x : datatype.text.startX + dx
//					});
//					datatype.icon.attr({
//						x : datatype.icon.startX + dx
//					});
//				}
//
//				datatype.customProps.moveAnnotationY = function(dy) {
//					datatype.attr({
//						y : datatype.startY + dy
//					});
//					datatype.text.attr({
//						y : datatype.text.startY + dy
//					});
//					datatype.icon.attr({
//						y : datatype.icon.startY + dy
//					});
//				}
//
//				datatype.customProps.moveStop = function() {
//					if (datatype.customProps.isMoved == true) {
//						datatype.customProps.isMoved = false;
//						executeMoveStop(datatype);
//
//						datatype.text.zoom_memory["x"] = ((datatype.text
//								.attr('x') - currentImage.attr('x')) / currentZoomLevel)
//								+ pannedByDXAggregated;
//						datatype.text.zoom_memory["y"] = ((datatype.text
//								.attr('y') - currentImage.attr('y')) / currentZoomLevel)
//								+ pannedByDYAggregated;
//					}
//				}
//
//				/*
//				 * Make adjustments to the memory attributes (x, y) of text as
//				 * mentioned above. This is done to adjust the dx, dy caused by
//				 * panning.
//				 */
//				datatype.customProps.panStop = function(pannedByDX, pannedByDY) {
//					datatype.text.zoom_memory["x"] = datatype.text.zoom_memory["x"]
//							+ pannedByDX;
//					datatype.text.zoom_memory["y"] = datatype.text.zoom_memory["y"]
//							+ pannedByDY;
//				}
//
//				allAnnotationsList.push(datatype);
//				return datatype;
//			}
//
//			function drawEvent(x, y, type, action, id) {
//				var imageUrl = null;
//				var eventType = null;
//
//				if (type == 'start') {
//					imageUrl = "plugins/bpm-modeler/images/icons/start_event_with_border.png";
//					eventType = "startEvent";
//				} else {
//					imageUrl = "plugins/bpm-modeler/images/icons/end-event-toolbar.png";
//					eventType = "stopEvent";
//				}
//
//				var eventSymbol = m_canvasManager.drawImageAt(imageUrl, x, y,
//						30, 30);
//
//				setOnclickSelectHandling(eventSymbol);
//
//				if (id == null) {
//					setUniqueId(eventSymbol);
//				} else {
//					eventSymbol.customProps = {
//						'id' : id
//					};
//				}
//
//				var createEventCmd = createCommandObject(eventSymbol,
//						eventType, action);
//
//				makeAnnotationMovableStretchable(eventSymbol);
//
//				m_commandsController.handleCommand(createEventCmd);
//
//				eventSymbol.customProps.moveStart = function() {
//					eventSymbol.startX = eventSymbol.attr('x');
//					eventSymbol.startY = eventSymbol.attr('y');
//				}
//
//				eventSymbol.customProps.moveAnnotation = function(dx, dy) {
//					eventSymbol.customProps.isMoved = true;
//					var delta = getRotationQualifiedDeltas(dx, dy);
//					dx = delta.x;
//					dy = delta.y;
//
//					if (dx != 0) {
//						eventSymbol.customProps.moveAnnotationX(dx);
//					}
//					if (dy != 0) {
//						eventSymbol.customProps.moveAnnotationY(dy);
//					}
//				}
//
//				eventSymbol.customProps.moveAnnotationX = function(dx) {
//					eventSymbol.attr({
//						x : eventSymbol.startX + dx
//					});
//					/*
//					 * rect.stretchHandle.attr({ cx : rect.stretchHandle.startX +
//					 * dx });
//					 */
//				}
//
//				eventSymbol.customProps.moveAnnotationY = function(dy) {
//					eventSymbol.attr({
//						y : eventSymbol.startY + dy
//					});
//				}
//
//				eventSymbol.customProps.moveStop = function() {
//					if (eventSymbol.customProps.isMoved == true) {
//						eventSymbol.customProps.isMoved = false;
//						executeMoveStop(eventSymbol);
//					}
//				}
//
//				allAnnotationsList.push(eventSymbol);
//				return eventSymbol;
//			}
//
//			function setUniqueId(object) {
//				m_communicationController.syncGetData({
//					url : getEndpointUrl() + "/uniqueid"
//				}, new function() {
//					return {
//						success : function(data) {
//							if (object.customProps) {
//								object.customProps.id = data;
//							} else {
//								object.customProps = {
//									id : data
//								};
//							}
//						},
//
//						failure : function(data) {
//						}
//					}
//				});
//			}
//			function setId(element, id) {
//
//				if (element.customProps) {
//					element.customProps.id = id;
//				} else {
//					element.customProps = {
//						'id' : id
//					};
//				}
//			}

//			function makeElementEventOpaque(element) {
//				m_utils.jQuerySelect(element.node).click(function(event) {
//					event.stopPropagation();
//				});
//			}

			/*
			 * Creates a command object. This is used for persisting the data in
			 * case of creation, deletion, change to an annotation
			 */
			/*
			 * NOTE: The current x, y coordinates of the annotation are not
			 * stored as is because they are relative to the canvas and not
			 * relative to the image. Here we calculate the (x, y) of an
			 * annotation in relation to the top left corner of the image when
			 * as 100% zoom. This enables us to lay the annotations at correct
			 * location whenever it's accessed again or in the event of image at
			 * zoom etc.
			 *
			 * Calculation:: Subtract the current image coordinates from those
			 * of the annotation to get dx, dy. Divide dx, dy by current zoom to
			 * get the actual dx, dy at 100% zoom.
			 */
//			function createCommandObject(element, type, action, txt) {
//				var commandObject = {};
//				commandObject.id = element.customProps.id;
//				commandObject.type = type;
//				commandObject.action = action;
//				commandObject.props = {};
//				if (type == "activity" || type == "startEvent"
//						|| type == "stopEvent" || type == "role"
//						|| type == "primitiveDataType"
//						|| type == "structuredDataType" || type == "gateway"
//						|| type == "subProcessActivity"
//						|| type == "applicationActivity"
//						|| type == "dataSymbol" || type == "roleSymbol") {
//					commandObject.props.dimensions = {
//						x : (element.attr("x") - currentImage.attr('x'))
//								/ currentZoomLevel,
//						y : (element.attr("y") - currentImage.attr('y'))
//								/ currentZoomLevel,
//						width : element.attr("width"),
//						height : element.attr("height")
//					};
//					commandObject.props.attributes = {
//						'fill' : element.attr("fill"),
//						'fill-opacity' : element.attr("fill-opacity")
//					};
//				}
//
//				if (type == "connector" || type == "roleassoc"
//						|| type == "dataassoc") {
//					commandObject.props.ends = {
//						source : element.customProps.source,
//						target : element.customProps.target
//					};
//					if (element.customProps.dataId) {
//						commandObject.props.ends.dataid = element.customProps.dataId;
//					}
//					if (connectionEnds[0].customProps.accessPoint) {
//						commandObject.props.ends.accesspoint = connectionEnds[0].customProps.accessPoint;
//					}
//					if (element.customProps.roleId) {
//						commandObject.props.ends.roleid = element.customProps.roleId;
//					}
//				}
//
//				if (txt) {
//					commandObject.props.text = txt.attr('text');
//					commandObject.props.completetext = txt.customProps.completetext;
//
//					if (element.customProps.type == "primitiveDataType"
//							|| element.customProps.type == "structuredDataType") {
//						commandObject.props.elementid = txt.customProps.completetext;
//						element.customProps.elementId = txt.customProps.completetext;
//					}
//				}
//				if (element.customProps.dataSubType) {
//					commandObject.dataelementproperties = {};
//					commandObject.dataelementproperties.type = element.customProps.dataSubType;
//				}
//
//				if (element.customProps.url != undefined) {
//					commandObject.props.url = element.customProps.url;
//				}
//				if (undefined != element.customProps.subProcId) {
//					commandObject.subprocid = element.customProps.subProcId;
//				}
//				if (undefined != element.customProps.appId) {
//					commandObject.applicationid = element.customProps.appId;
//				}
//				if (element.customProps.description) {
//					commandObject.description = element.customProps.description;
//				}
//
//				commandObject.execute = function() {
//					var endpointUrl = getEndpointUrl() + "/models/" + modelId
//							+ "/processes/" + processId + ElementUrlMap[type];
//					if (commandObject.action == 'Create') {
//						m_utils.jQuerySelect(document).trigger("ELEMENT_ADDED", {
//							url : endpointUrl,
//							jsonData : JSON.stringify(commandObject)
//						});
//					} else if (commandObject.action == 'Recreate') {
//						m_utils.jQuerySelect(document).trigger("ELEMENT_ADDED", {
//							jsonData : JSON.stringify(commandObject)
//						});
//					} else if (commandObject.action == 'Modify'
//							|| commandObject.action == "Rename") {
//						m_utils.jQuerySelect(document).trigger("ELEMENT_MODIFIED", {
//							url : endpointUrl + "/" + commandObject.id,
//							jsonData : JSON.stringify(commandObject)
//						});
//					}
//				}
//
//				element.customProps.commandObj = commandObject;
//
//				return commandObject;
//			}

			/*
			 * Called after an annotation is dragged around. NOTE: The current
			 * x, y coordinates of the annotation are not stored as is because
			 * they are relative to the canvas and not relative to the image.
			 * For details please see the comment at createCommandObject()
			 * function where a similar thing is done.
			 */
//			function executeMoveStop(annotToStop) {
//				var cmdObj = annotToStop.customProps.commandObj;
//				cmdObj.action = 'Modify';
//				cmdObj.props.dimensions.x = (annotToStop.attr("x") - currentImage
//						.attr('x'))
//						/ currentZoomLevel;
//				cmdObj.props.dimensions.y = (annotToStop.attr("y") - currentImage
//						.attr('y'))
//						/ currentZoomLevel;
//				m_commandsController.handleCommand(cmdObj);
//			}

//			function getRotationQualifiedCoordinates(x, y) {
//				if (((currentRotationFactor / 90) % 4) == 3) {
//					tx = x;
//					x = -y;
//					y = tx;
//				}
//				if (((currentRotationFactor / 90) % 4) == 2) {
//					x = -x;
//					y = -y;
//				}
//				if (((currentRotationFactor / 90) % 4) == 1) {
//					tx = x;
//					x = y;
//					y = -tx;
//				}
//
//				return [ x, y ];
//			}

//			function makeAnnotationEditable(rect) {
//				m_utils.jQuerySelect(rect.text.node).dblclick(function(e) {
//					makeEditableCallback(rect);
//				});
//				m_utils.jQuerySelect(rect.node).dblclick(function(e) {
//					m_utils.jQuerySelect(document).trigger("SHOW_PROPERTIES");
//				});
//				m_utils.jQuerySelect(rect.icon.node).dblclick(function(e) {
//					m_utils.jQuerySelect(document).trigger("SHOW_PROPERTIES");
//				});
//			}

//			function makeEditableCallback(annot, xOffset, yOffset) {
//				currentEditableAnnotation = annot;
//				currentlySelectedAnnotation = null;
//				deHighlightAnnotation();
//
//				/*
//				 * m_utils.jQuerySelect("#editable").moveDiv({ x :
//				 * getEditableAnnotationsXCoordinate(), y :
//				 * getEditableAnnotationsYCoordinate() });
//				 * m_utils.jQuerySelect("#editable").css('visibility', 'visible');
//				 * m_utils.jQuerySelect("#editable") .html(
//				 * currentEditableAnnotation.text.customProps.completetext)
//				 * .show().trigger('dblclick');
//				 */
//				currentEditableAnnotation.customProps.hideAnnotation();
//			}

//			function getEditableAnnotationsXCoordinate() {
//				var canvasCentreX = canvasWidth / 2;
//				var canvasCentreY = canvasHeight / 2;
//
//				switch (getCurrentImageOrientation()) {
//				case 1:
//					return canvasCentreX
//							+ (canvasCentreY - currentEditableAnnotation.attrs.y)
//							- currentEditableAnnotation.attrs.height;
//					break;
//				case 2:
//					return canvasCentreX
//							+ (canvasCentreX - currentEditableAnnotation.attrs.x)
//							- currentEditableAnnotation.attrs.width;
//					break;
//				case 3:
//					return canvasCentreX
//							- (canvasCentreY - currentEditableAnnotation.attrs.y);
//					break;
//				default:
//					return currentEditableAnnotation.attrs.x;
//				}
//			}

//			function getEditableAnnotationsYCoordinate() {
//				var editableBoxYOffset = 35;
//				var canvasCentreX = canvasWidth / 2;
//				var canvasCentreY = canvasHeight / 2;
//
//				switch (getCurrentImageOrientation()) {
//				case 1:
//					return canvasCentreY
//							- (canvasCentreX - currentEditableAnnotation.attrs.x)
//							+ editableBoxYOffset;
//					break;
//				case 2:
//					return canvasCentreY
//							+ (canvasCentreY - currentEditableAnnotation.attrs.y)
//							- currentEditableAnnotation.attrs.height
//							+ editableBoxYOffset;
//					break;
//				case 3:
//					return canvasCentreY
//							+ (canvasCentreX - currentEditableAnnotation.attrs.x)
//							- currentEditableAnnotation.attrs.width
//							+ editableBoxYOffset;
//					break;
//				default:
//					return currentEditableAnnotation.attrs.y
//							+ editableBoxYOffset;
//				}
//			}

			/*
			 * Closure used here. The parent element's (rectangles) reference is
			 * used to call move start / stop etc even in cases where thenclosed
			 * image / text is dragged.
			 */
//			function makeAnnotationMovableStretchable(element) {
//				var start = function() {
//					element.customProps.moveStart();
//				}, up = function() {
//					element.customProps.moveStop();
//				}, move = function(dx, dy) {
//					// move will be called with dx and dy.
//					element.customProps.moveAnnotation(dx, dy);
//					/* Connector move logic START */
//					for ( var i = connections.length; i--;) {
//						m_canvasManager.getCanvas().connection(connections[i]);
//					}
//					m_canvasManager.getCanvas().safari();
//					/* Connector move logic END */
//				}
//
//				element.drag(move, start, up);
//				element.text.drag(move, start, up);
//				element.icon.drag(move, start, up);
//			}

			/*
			 * Breaks the text into number of rows and number of characters per
			 * row. Existing linefeeds are preserved.
			 */
//			function formatText(value, rows, charsPerRow) {
//				var strs = value.split(/\r\n|\r|\n/);
//				var newValue = '';
//				for ( var i = 0; i < strs.length; i++) {
//					newValue += strs[i] + '\r\n';
//				}
//				return breakIntoRows(newValue, rows, charsPerRow);
//			}

			/*
			 * Breaks the text into number of rows and number of characters per
			 * row. Existing linefeeds may not be preserved. Use formatText for
			 * preserving existing linefeeds.
			 */
//			function breakIntoRows(value, rows, charsPerRow) {
//				rows = parseInt(rows);
//				charsPerRow = parseInt(charsPerRow);
//				var newVal = "";
//				for ( var i = 0; i < rows; i++) {
//					var newLineIndex = value.substring(0, charsPerRow).indexOf(
//							'\r\n');
//					if (newLineIndex == 0) {
//						newVal += value.substring(0, newLineIndex + 1);
//						value = value.substring(newLineIndex + 1);
//					} else if (newLineIndex != -1) {
//						newVal += value.substring(0, newLineIndex + 1);
//						value = value.substring(newLineIndex + 1);
//					} else if (value.length > charsPerRow) {
//						var spIndex = value.substring(0, charsPerRow)
//								.lastIndexOf(' ');
//						if (spIndex != -1) {
//							newVal += value.substring(0, spIndex + 1) + '\r\n';
//							value = value.substring(spIndex + 1);
//						} else {
//							newVal += value.substring(0, charsPerRow) + '\r\n';
//							value = value.substring(charsPerRow);
//						}
//
//					} else {
//						newVal += value;
//						break;
//					}
//				}
//
//				return newVal;
//			}
//
//			function getNumberOfRows(rect) {
//				return parseInt(rect.attr('height') / 15);
//			}
//
//			function getNumberOfCharactersPerRow(rect) {
//				return parseInt(rect.attr('width') / 6);
//			}
//
//			function executeStretchStop(annotToStop) {
//				var cmdObj = annotToStop.customProps.commandObj;
//				cmdObj.action = 'Modify';
//				cmdObj.props.dimensions.width = annotToStop.attr("width")
//						/ currentZoomLevel;
//				cmdObj.props.dimensions.height = annotToStop.attr("height")
//						/ currentZoomLevel;
//				if (cmdObj.props.completetext) {
//					cmdObj.props.text = formatText(cmdObj.props.completetext,
//							getNumberOfRows(annotToStop),
//							getNumberOfCharactersPerRow(annotToStop));
//				}
//				m_commandsController.handleCommand(cmdObj);
//				highlightAnnotation(annotToStop);
//			}
//
//			function getRotationQualifiedDeltas(x, y) {
//				if (((currentRotationFactor / 90) % 4) == 3) {
//					tx = x;
//					x = -y;
//					y = tx;
//				}
//				if (((currentRotationFactor / 90) % 4) == 2) {
//					x = -x;
//					y = -y;
//				}
//				if (((currentRotationFactor / 90) % 4) == 1) {
//					tx = x;
//					x = y;
//					y = -tx;
//				}
//
//				return {
//					'x' : x,
//					'y' : y
//				};
//			}
//
//			function getCurrentImageOrientation() {
//				return ((currentRotationFactor / 90) % 4);
//			}
//
//			function addConnectionEnd(obj, isRecreate) {
//				if (connectionEnds.length < 1) {
//					connectionEnds.push(obj);
//				} else {
//					// push second object
//					if (connectionEnds[0].customProps.id != obj.customProps.id) {
//						connectionEnds.push(obj);
//						var connectorType = "connector";
//						if (connectionEnds[0].customProps.type == 'role'
//								|| connectionEnds[0].customProps.type == "roleSymbol") {
//							connectorType = "roleassoc";
//						} else if ((connectionEnds[0].customProps.type == "primitiveDataType")
//								|| (connectionEnds[1].customProps.type == "primitiveDataType")
//								|| (connectionEnds[0].customProps.type == "structuredDataType")
//								|| (connectionEnds[1].customProps.type == "structuredDataType")
//								|| (connectionEnds[0].customProps.type == "dataSymbol")
//								|| (connectionEnds[1].customProps.type == "dataSymbol")) {
//							connectorType = "dataassoc";
//							if (connectionEnds[0].customProps.type == "applicationActivity") {
//								connectionEnds[0].customProps.accessPoint = connectionEnds[0].customProps.accessPoint;
//							}
//						}
//						// gateway logic -
//						// 1. if connectionEnds[1] is gateway - set inbound to
//						// connectionEnds[0]
//						if (connectionEnds[1].customProps.type == 'gateway') {
//							connectionEnds[1].customProps.inbound = connectionEnds[0];
//						}
//						// 2. if connectionEnds[0] is gateway - set outbound 1
//						// or 2 to connectionEnds[1]
//						if (connectionEnds[0].customProps.type == 'gateway') {
//							if (connectionEnds[0].customProps.outbound1 == null) {
//								connectionEnds[0].customProps.outbound1 = connectionEnds[1];
//							} else {
//								connectionEnds[0].customProps.outbound2 = connectionEnds[1];
//							}
//						}
//						createConnection(connectionEnds[0], connectionEnds[1],
//								isRecreate, connectorType);
//						// clear the ends array
//						connectionEnds.length = 0;
//					}
//				}
//			}
//			;

//			function createConnection(source, target, isRecreate, connectorType) {
//				var connection = m_canvasManager.getCanvas().connection(source,
//						target, null, "gray|1", connectorType);
//				setUniqueId(connection);
//				if (source.customProps.type == "primitiveDataType"
//						|| source.customProps.type == "structuredDataType") {
//					connection.customProps.source = source.customProps.elementId;
//				} else {
//					connection.customProps.source = source.customProps.id;
//				}
//				if (target.customProps.type == "primitiveDataType"
//						|| target.customProps.type == "structuredDataType") {
//					connection.customProps.target = target.customProps.elementId;
//				} else {
//					connection.customProps.target = target.customProps.id;
//				}
//				if (source.customProps.dataId) {
//					connection.customProps.dataId = source.customProps.dataId;
//				}
//				if (target.customProps.dataId) {
//					connection.customProps.dataId = target.customProps.dataId;
//				}
//				if (source.customProps.roleId) {
//					connection.customProps.roleId = source.customProps.roleId;
//				}
//
//				// Check if first object is a role - Need to communicate that to
//				// server
//
//				addConnection(connection, isRecreate, connectorType);
//			}
//			;
//
//			function addConnection(connection, isRecreate, connectorType) {
//				connections.push(connection);
//				var action = (isRecreate == true) ? "Recreate" : "Create";
//				// var connectorType = (isRole == true) ? "roleassoc" :
//				// "connector";
//				var createConnectionCmd = createCommandObject(connection,
//						connectorType, action);
//				m_commandsController.handleCommand(createConnectionCmd);
//			}
//			;
//
//			// Candidate utility method
//			function setDescription(element, attrs) {
//				if (attrs != null && attrs.description != null) {
//					element.customProps.description = attrs.description;
//				} else {
//					element.customProps.description = "";
//				}
//			}
//
//			// Candidate utility function
//			function isElementPresent(elementList, compareProperty, property) {
//				var isPresent = false;
//				jQuery.each(elementList, function(i, val) {
//					if (getPropertyValue(val, compareProperty) == property) {
//						isPresent = true;
//					}
//				});
//
//				return isPresent;
//			}
//
//			// Candidate utility function
//			function getPropertyValue(obj, prop) {
//				var val = obj;
//				var props = prop.split(".");
//				for ( var p in props) {
//					if (val.hasOwnProperty(props[p])) {
//						val = val[props[p]];
//					} else {
//						val = undefined;
//						break;
//					}
//				}
//
//				return val;
//			}
		});