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
      [ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
            "bpm-modeler/js/m_canvasManager", "bpm-modeler/js/m_drawable",
            "bpm-modeler/js/m_commandsController",
            "bpm-modeler/js/m_command", "bpm-modeler/js/m_controlFlow",
            "bpm-modeler/js/m_propertiesPanel",
            "bpm-modeler/js/m_activitySymbol",
            "bpm-modeler/js/m_gatewaySymbol",
            "bpm-modeler/js/m_eventSymbol", "bpm-modeler/js/m_controlFlow",
            "bpm-modeler/js/m_dataFlow", "bpm-modeler/js/m_modelerUtils",
            "bpm-modeler/js/m_messageDisplay",
            "bpm-modeler/js/libs/jsPlumb/jquery.jsPlumb-1.4.1-all-min",
            "bpm-modeler/js/m_i18nUtils"],
      function(m_utils, m_constants, m_canvasManager, m_drawable,
            m_commandsController, m_command, m_controlFlow,
            m_propertiesPanel, m_activitySymbol,
            m_gatewaySymbol, m_eventSymbol, m_controlFlow, m_dataFlow,
            m_modelerUtils, m_messageDisplay, jquery_jsPlumb,m_i18nUtils) {

         return {
            createConnection : function(diagram, fromAnchorPoint) {
               var connection = new Connection();

               connection.bind(diagram);
               // Validate the connection rules for anchor Point
               if (connection.validateCreateConnection(fromAnchorPoint)) {
                  connection.setFirstAnchorPoint(fromAnchorPoint);
               } else {
                  // reset the connection
                  connection = null;
               }

               return connection;
            },

            createConnectionFromJson : function(diagram, json) {
               // TODO Ugly
               m_utils.inheritFields(json, m_drawable.createDrawable());
               m_utils.inheritMethods(json, new Connection());

               json.bind(diagram);

               json.initializeFromJson(diagram);

               return json;
            }
         };

         /**
         *
         */
         /**
         * @returns {Connection}
         */
         function Connection() {
            var drawable = m_drawable.createDrawable();
            var FLYOUT_MENU_LOC_OFFSET = 20;

            m_utils.inheritFields(this, drawable);
            m_utils.inheritMethods(Connection.prototype, drawable);

            this.description = null;
            this.fromAnchorPointOrientation = 1;
            this.fromModelElementOid = null;
            this.fromModelElementType = null;
            this.toAnchorPointOrientation = 3;
            this.toModelElementOid = null;
            this.toModelElementType = null;
            this.conditionExpressionTextXOffset;
            this.conditionExpressionTextYOffset;

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
            Connection.prototype.initializeFromJson = function(diagram) {
               // Adjust anchor orientation
               var orientation = this.determineOrientation();
               if (orientation) {
                  this.fromAnchorPointOrientation = orientation["from"];
                  this.toAnchorPointOrientation = orientation["to"];

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
                     m_dataFlow.initializeFromJson(this.diagram.process,
                           this.modelElement);
                     this.propertiesPanel = this.diagram.dataFlowPropertiesPanel;
                  } else {
                     if (!this.modelElement.prototype) {
                        this.modelElement.prototype = {};
                     }
                     m_utils.inheritMethods(this.modelElement.prototype,
                           m_controlFlow.prototype);
                     this.propertiesPanel = diagram.controlFlowPropertiesPanel;

                     this.adjustConditionExpressionText();

                  }

                  this.completeNoTransfer();
                  this.reroute();
            }
            };

            /*
            *
            */
            Connection.prototype.initializeAnchorPoints = function() {
               if (this.toModelElementType == m_constants.ACTIVITY) {
                  this.toAnchorPoint = this.diagram.activitySymbols[this.toModelElementOid].anchorPoints[this.toAnchorPointOrientation];
               } else if (this.toModelElementType == m_constants.EVENT) {
                  this.toAnchorPoint = this.diagram.eventSymbols[this.toModelElementOid].anchorPoints[this.toAnchorPointOrientation];
               } else if (this.toModelElementType == m_constants.DATA) {
                  this.toAnchorPoint = this.diagram.dataSymbols[this.toModelElementOid].anchorPoints[this.toAnchorPointOrientation];
               } else if (this.toModelElementType == m_constants.GATEWAY) {
                  this.toAnchorPoint = this.diagram.gatewaySymbols[this.toModelElementOid].anchorPoints[this.toAnchorPointOrientation];
               }

               if (this.fromModelElementType == m_constants.ACTIVITY) {
                  this.fromAnchorPoint = this.diagram.activitySymbols[this.fromModelElementOid].anchorPoints[this.fromAnchorPointOrientation];
               } else if (this.fromModelElementType == m_constants.EVENT) {
                  this.fromAnchorPoint = this.diagram.eventSymbols[this.fromModelElementOid].anchorPoints[this.fromAnchorPointOrientation];
               } else if (this.fromModelElementType == m_constants.DATA) {
                  this.fromAnchorPoint = this.diagram.dataSymbols[this.fromModelElementOid].anchorPoints[this.fromAnchorPointOrientation];
               } else if (this.fromModelElementType == m_constants.GATEWAY) {
                  this.fromAnchorPoint = this.diagram.gatewaySymbols[this.fromModelElementOid].anchorPoints[this.fromAnchorPointOrientation];
               }
            };

            /*
            * determines orientation only if it is undefined, e.g. in Eclipse born models, gateways don't contain
            * orientation related information, all is center.
            */
            Connection.prototype.determineOrientation = function() {
               var frmSmbl;
               var toSmbl;

               if (this.fromModelElementType == m_constants.ACTIVITY) {
                  frmSmbl = this.diagram.activitySymbols[this.fromModelElementOid
                        .toString()];
               } else if (this.fromModelElementType == m_constants.EVENT) {
                  frmSmbl = this.diagram.eventSymbols[this.fromModelElementOid];
               } else if (this.fromModelElementType == m_constants.DATA) {
                  frmSmbl = this.diagram.dataSymbols[this.fromModelElementOid];
               } else if (this.fromModelElementType == m_constants.GATEWAY) {
                  frmSmbl = this.diagram.gatewaySymbols[this.fromModelElementOid];
               }

               if (this.toModelElementType == m_constants.ACTIVITY) {
                  toSmbl = this.diagram.activitySymbols[this.toModelElementOid
                        .toString()];
               } else if (this.toModelElementType == m_constants.EVENT) {
                  toSmbl = this.diagram.eventSymbols[this.toModelElementOid];
               } else if (this.toModelElementType == m_constants.DATA) {
                  toSmbl = this.diagram.dataSymbols[this.toModelElementOid];
               } else if (this.toModelElementType == m_constants.GATEWAY) {
                  toSmbl = this.diagram.gatewaySymbols[this.toModelElementOid];
               }

               if (!frmSmbl || !toSmbl) {
                  return;
               }

               var fromAnchorPOFixed = this.fromAnchorPointOrientation;
               var toAnchorPOFixed = this.toAnchorPointOrientation;
               var orientation = {};


               // for Vertical orientation
               // If the connection is from Activity to Gateway, - use
               // single target anchor point of Gateway
               // it will keep other Anchor points of Gateway free for
               // out-flowing controls
               if (frmSmbl.type == m_constants.ACTIVITY_SYMBOL
                     && toSmbl.type == m_constants.GATEWAY_SYMBOL) {
                  if (fromAnchorPOFixed == m_constants.UNDEFINED_ORIENTATION) {
                     if (this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
                        fromAnchorPOFixed = 2;
                     } else {
                        fromAnchorPOFixed = 1;
                     }
                  }
                  if (toAnchorPOFixed == m_constants.UNDEFINED_ORIENTATION) {
                     if (this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
                        toAnchorPOFixed = 0;
                     } else {
                        toAnchorPOFixed = 3;
                     }
                  }
               }

               if (frmSmbl.type == m_constants.GATEWAY_SYMBOL
                     && toSmbl.type == m_constants.ACTIVITY_SYMBOL) {
                  if (toAnchorPOFixed == m_constants.UNDEFINED_ORIENTATION) {
                     if (this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
                        toAnchorPOFixed = 0;
                     }
                     else{
                        toAnchorPOFixed = 3;
                     }
                  }
                  if (fromAnchorPOFixed == m_constants.UNDEFINED_ORIENTATION) {
                     if (this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_VERTICAL) {
                        fromAnchorPOFixed = 2;
                        if (toSmbl.anchorPoints[0].x < frmSmbl.anchorPoints[3].x) {
                           fromAnchorPOFixed = 3;
                        } else if (toSmbl.anchorPoints[0].x > frmSmbl.anchorPoints[1].x) {
                           fromAnchorPOFixed = 1;
                        }
                     } else {
                        fromAnchorPOFixed = 1;
                        if (toSmbl.anchorPoints[3].y < frmSmbl.anchorPoints[0].y) {
                           fromAnchorPOFixed = 0;
                        } else if (toSmbl.anchorPoints[3].y > frmSmbl.anchorPoints[2].y) {
                           fromAnchorPOFixed = 2;
                        }
                     }
                  }
               }

               if (fromAnchorPOFixed != m_constants.UNDEFINED_ORIENTATION
                     && toAnchorPOFixed != m_constants.UNDEFINED_ORIENTATION) {
                  orientation["from"] = fromAnchorPOFixed;
                  orientation["to"] = toAnchorPOFixed;
               }
               // if from anchor point is defined and to anchor point is
               // not
               // defined
               else if (fromAnchorPOFixed != m_constants.UNDEFINED_ORIENTATION
                     && toAnchorPOFixed == m_constants.UNDEFINED_ORIENTATION) {
                  orientation["from"] = fromAnchorPOFixed;
                  orientation["to"] = this.getClosestAnchorPointFor(
                        frmSmbl.anchorPoints[fromAnchorPOFixed],
                        toSmbl.anchorPoints)["orient"];
               } else if (fromAnchorPOFixed == m_constants.UNDEFINED_ORIENTATION
                     && toAnchorPOFixed != m_constants.UNDEFINED_ORIENTATION) {
                  orientation["to"] = toAnchorPOFixed;
                  orientation["from"] = this.getClosestAnchorPointFor(
                        toSmbl.anchorPoints[toAnchorPOFixed],
                        frmSmbl.anchorPoints)["orient"];
               } else if (fromAnchorPOFixed == m_constants.UNDEFINED_ORIENTATION
                     && toAnchorPOFixed == m_constants.UNDEFINED_ORIENTATION) {
                  var distance;
                  var orientationDist;
                  for ( var i = 0; i < 4; i++) {
                     orientationDist = this.getClosestAnchorPointFor(
                           frmSmbl.anchorPoints[i],
                           toSmbl.anchorPoints);

                     if (!distance
                           || (orientationDist["dist"] < distance)) {
                        distance = orientationDist["dist"];
                        orientation["from"] = i;
                        orientation["to"] = orientationDist["orient"];
                     }
                  }
               }

               return orientation;
            };

            /**
            * finds the closest anchor points of four anchor points
            *
            */
            Connection.prototype.getClosestAnchorPointFor = function(
                  fromAnchorPoint, toAnchorPoints) {
               var orientationDist = {};
               var distance;
               for ( var i = 0; i < 4; i++) {
                  var dX = fromAnchorPoint.x - toAnchorPoints[i].x;
                  var dY = fromAnchorPoint.y - toAnchorPoints[i].y;
                  var dX2 = dX * dX;
                  var dY2 = dY * dY;
                  var distance1 = Math.sqrt((dX * dX) + (dY * dY));
                  if (i == (fromAnchorPoint.orientation + 2) % 4) {
                     distance1 = distance1
                           - m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH;
                  }

                  if (!distance || distance1 < distance) {
                     distance = distance1;
                     orientationDist["orient"] = i;
                     orientationDist["dist"] = distance1;
                  }
               }
               return orientationDist;
            };

            /**
            * when connection is created from Flyout Menu, anchor points
            * needs to be moved to 6 O'clock or 3 O'clock
            */
            Connection.prototype.updateAnchorPointForSymbol = function() {
               var sourceOrientation = null;
               var targetOrientation = 0;
               if (this.diagram.flowOrientation == m_constants.DIAGRAM_FLOW_ORIENTATION_HORIZONTAL) {
                  targetOrientation = 3;
               }

               if (this.fromAnchorPoint.symbol.type == m_constants.GATEWAY_SYMBOL
                     || this.fromAnchorPoint.symbol.type == m_constants.ACTIVITY_SYMBOL) {
                  var startSymbol = this.fromAnchorPoint.symbol;
                  var targetSymbol = this.toAnchorPoint.symbol;
                  if (startSymbol.x > targetSymbol.x + targetSymbol.width) {
                     // Start Symbol is at right, show arrow at left
                     sourceOrientation = 3;
                     if (startSymbol.y > targetSymbol.y) {
                        targetOrientation = 1;
                     }
                  } else if (startSymbol.x + startSymbol.width < targetSymbol.x) {
                     // Start Symbol is at left, show arrow at right
                     sourceOrientation = 1;
                     if (startSymbol.y > targetSymbol.y) {
                        targetOrientation = 3;
                     }
                  } else {
                     // default orientation is SOUTH for gateway
                     sourceOrientation = 2;
                  }
                  this.fromAnchorPoint = startSymbol.anchorPoints[sourceOrientation];
                  this.toAnchorPoint = targetSymbol.anchorPoints[targetOrientation];
               }
            };

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

               if (this.toAnchorPoint.symbol != null
                     && this.validateCreateConnection(
                           this.fromAnchorPoint, this.toAnchorPoint)) {
                  // On Mouse move , the same connection is added again,
                  // so remove if present then add(update)
                  m_utils.removeItemFromArray(
                        this.toAnchorPoint.symbol.connections, this);
                  this.toAnchorPoint.symbol.connections.push(this);

                  if (this.isDataFlow()) {
                     var data;
                     var activity;

                     // TODO need better ways to identify data symbol
                     if (this.fromAnchorPoint.symbol.dataFullId != null) {
                        this.fromModelElementOid = this.fromAnchorPoint.symbol.oid;
                        this.fromModelElementType = m_constants.DATA;
                        this.toModelElementOid = this.toAnchorPoint.symbol.oid;
                        if (this.toAnchorPoint.symbol.modelElement) {
                           this.toModelElementType = this.toAnchorPoint.symbol.modelElement.type;
                        }
                        data = this.fromAnchorPoint.symbol.modelElement;
                        activity = this.toAnchorPoint.symbol.modelElement;
                     } else {
                        this.fromModelElementOid = this.fromAnchorPoint.symbol.oid;
                        if (this.fromAnchorPoint.symbol.modelElement) {
                           this.fromModelElementType = this.fromAnchorPoint.symbol.modelElement.type;
                        }
                        this.toModelElementOid = this.toAnchorPoint.symbol.oid;
                        this.toModelElementType = m_constants.DATA;
                        data = this.toAnchorPoint.symbol.modelElement;
                        activity = this.fromAnchorPoint.symbol.modelElement;
                     }

                     this.modelElement = m_dataFlow.createDataFlow(
                           this.diagram.process, data, activity);

                     if (this.fromModelElementType == m_constants.DATA) {
                       this.modelElement.dataMappings = [{
                           id : this.modelElement.id,
                           name : this.modelElement.name,
                           direction : "IN"
                         }
                       ];
                     } else {
                       this.modelElement.dataMappings = [{
                           id : this.modelElement.id,
                           name : this.modelElement.name,
                           direction : "OUT"
                         }
                       ];
                     }

                    this.propertiesPanel = this.diagram.dataFlowPropertiesPanel;
                     
                  } else {
                     this.fromModelElementOid = this.fromAnchorPoint.symbol.oid;

                     if (null != this.fromAnchorPoint.symbol.modelElement) {
                        if (this.fromAnchorPoint.symbol.modelElement.type === m_constants.ACTIVITY
                              && this.fromAnchorPoint.symbol.modelElement.activityType === m_constants.GATEWAY_ACTIVITY_TYPE) {
                           this.fromModelElementType = m_constants.GATEWAY;
                        } else {
                           this.fromModelElementType = this.fromAnchorPoint.symbol.modelElement.type;
                        }
                     } else {
                        this.fromModelElementType = this.fromAnchorPoint.symbol.type;
                     }
                     this.toModelElementOid = this.toAnchorPoint.symbol.oid;
                     if (this.toAnchorPoint.symbol.modelElement) {
                        this.toModelElementType = this.toAnchorPoint.symbol.modelElement.type;
                     }

                     this.modelElement = m_controlFlow
                           .createControlFlow(this.diagram.process);
                     this.propertiesPanel = this.diagram.controlFlowPropertiesPanel;
                  }

                  this.refreshFromModelElement();
               }

               this.reroute();
            };

            /**
            * sync : Synchronous AJAX call is made, if set(needed in
            * scenario like rerouting a connection(Create new connection
            * and remove original))
            */
            Connection.prototype.setSecondAnchorPoint = function(
                  anchorPoint, sync, reRoutedConnectionModelElement) {

               this.setSecondAnchorPointNoComplete(anchorPoint);
               
               if(reRoutedConnectionModelElement){
            	   this.modelElement.conditionExpression = reRoutedConnectionModelElement.conditionExpression;
            	   this.modelElement.otherwise = reRoutedConnectionModelElement.otherwise;
            	   this.modelElement.name = reRoutedConnectionModelElement.name;
            	   this.modelElement.description = reRoutedConnectionModelElement.description;
            	   this.modelElement.forkOnTraversal = reRoutedConnectionModelElement.forkOnTraversal;
            	   this.modelElement.comments = reRoutedConnectionModelElement.comments;
               }
               
               var updateConnection = null;

               if (this.toAnchorPoint.symbol != null) {

                  // When IN mapping present and OUT mapping is added,
                  // same connection is modified viceversa
                  if (this.isDataFlow()) {
                     if (this.fromAnchorPoint.symbol.type == m_constants.DATA_SYMBOL) {
                        var dataSymbol = this.fromAnchorPoint.symbol;
                        var activity = this.toAnchorPoint.symbol;
                     } else {
                        var dataSymbol = this.toAnchorPoint.symbol;
                        var activity = this.fromAnchorPoint.symbol;
                     }
                     for ( var n in dataSymbol.connections) {
                        // Identify if connection exist between same
                        // Data and Activity symbol
                        if (dataSymbol.connections[n].oid
                              && (dataSymbol.connections[n].fromAnchorPoint.symbol.oid == activity.oid || dataSymbol.connections[n].toAnchorPoint.symbol.oid == activity.oid)) {
                           // Use the existing connection
                           updateConnection = dataSymbol.connections[n];
                           var existingDataMapping = updateConnection.modelElement.dataMappings[0]
                           var changes = {'id': existingDataMapping.id, 'name': existingDataMapping.name};
                           changes.direction = "OUT";
                           if(existingDataMapping.direction == "OUT"){
                             changes.direction = "IN"; 
                           }
                           updateConnection.createDataMapping(changes);
                           m_messageDisplay.showMessage("Connection updated");
                           break;
                        }
                     }
                  }

                  // If update is not called, new connection is created
                  if (updateConnection == null) {
                     this.complete(sync);
                  }
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
                  if (!this.conditionExpressionTextXOffset)
                     transferObject.modelElement.attributes["carnot:engine:conditionExpressionTextXOffset"] = this.conditionExpressionTextXOffset;
                  if (!this.conditionExpressionTextYOffset)
                     transferObject.modelElement.attributes["carnot:engine:conditionExpressionTextYOffset"] = this.conditionExpressionTextYOffset;
                  // TODO Add later
                  transferObject.segments = null;
               } else {
                  transferObject.modelElement = transferObject.modelElement
                        .createTransferObject();
                  if (this.modelElement)
                     transferObject.modelElement.oid = this.modelElement.oid;
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
            Connection.prototype.isPoolSymbol = function() {
               return false;
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
                     .createFlippedClone(this.diagram));
               m_messageDisplay
                     .showMessage(m_i18nUtils.getProperty("modeler.messagedisplay.messages.info.connSelAchorPoint"));
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
            * sync : Synchronous AJAX call is made, if set(needed in
            * scenario like rerouting a connection(Create new connection
            * and remove original))
            */
            Connection.prototype.complete = function(sync) {
               this.completeNoTransfer();
               var command = m_command.createCreateNodeCommand(
                     "connection.create", this.diagram.model.id,
                     this.diagram.process.oid, this
                           .createTransferObject());
               command.sync = sync ? true : false;
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

               this.auxiliaryPickPath = this.diagram.canvasManager.drawPath("", {
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
               this.path = this.diagram.canvasManager.drawPath("", {
                  "arrow-end" : "block-wide-long",
                  "arrow-start" : "none",
                  "stroke" : m_constants.UNKNOWN_FLOW_COLOR,
                  "stroke-width" : m_constants.CONNECTION_STROKE_WIDTH,
                  "stroke-dasharray" : "-",
                  "r" : 3
               });

               this.addToPrimitives(this.path);
               //this.addToEditableTextPrimitives(this.path);

               this.path.auxiliaryProperties = {
                  callbackScope : this
               };

               this.conditionExpressionText = this.diagram.canvasManager
                     .drawTextNode(
                           this.fromAnchorPoint.x,
                           this.fromAnchorPoint.y,
                           "").attr({
                        "text-anchor" : "start",
                        "fill" : m_constants.CONTROL_FLOW_COLOR,
                        "font-size" : m_constants.DEFAULT_FONT_SIZE
                     });

               this.addToPrimitives(this.conditionExpressionText);

               this.conditionExpressionText.auxiliaryProperties = {
                  callbackScope : this
               };

               //this.addToEditableTextPrimitives(this.conditionExpressionText);
               this.conditionExpressionText.hide();

               this.defaultIndicatorPath = this.diagram.canvasManager.drawPath("", {
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
               // Exclude Click from readonly check to show properties panel
               this.auxiliaryPickPath.click(Connection_clickClosure);
               if (!this.diagram.process.isReadonly()) {
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
               }
            };

            Connection.prototype.createProximitySensorPrimitive = function() {
               return this.diagram.canvasManager.drawPath("", {
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
                     if (this.modelElement.conditionExpression
                           && this.modelElement.conditionExpression
                                 .trim() != ""
                           && this.modelElement.conditionExpression
                                 .trim() != "true") {
                        if (!this.conditionExpressionTextXOffset
                              || !this.conditionExpressionTextYOffset) {
                           this.adjustConditionExpressionText();
                        }
                        this.conditionExpressionText.attr("text",
                              this.modelElement.conditionExpression);

                        this.conditionExpressionText.attr({
                           x : this.toAnchorPoint.x
                                 + this.conditionExpressionTextXOffset,
                           y : this.toAnchorPoint.y
                                 + this.conditionExpressionTextYOffset
                        });

                        this.conditionExpressionText.show();
                     } else if (this.fromModelElementType == m_constants.GATEWAY
                              && this.modelElement.name) {
                        this.conditionExpressionText.attr("text",
                              this.modelElement.name);
                        this.conditionExpressionText.show();
                     } else {
                        this.conditionExpressionText.hide();
                     }
                  }
               } else if (this.isDataFlow()) {

                  this.path.attr({
                     "stroke" : m_constants.DATA_FLOW_COLOR,
                     "stroke-dasharray" : "-",
                     "arrow-start" : "none",
                     "arrow-end" : "none"
                  });

                  // For In-Mapping path will be from Data to
                  // Activity
                  // vice-versa for Out mapping
                  var mappingExist = this.modelElement.inputOutputMappingExists();
                  
                  if (mappingExist.input
                        && mappingExist.output) {
                     this.path.attr("arrow-start",
                           "block-wide-long");
                     this.path.attr("arrow-end",
                           "block-wide-long");
                  } else if (mappingExist.input) {
                     // When dataFlow modified from properties
                     // panel
                     // the From,To anchor point symbols to not
                     // change
                     if (this.fromAnchorPoint.symbol.type == m_constants.ACTIVITY_SYMBOL) {
                        this.path.attr("arrow-start",
                              "block-wide-long");
                        this.path.attr("arrow-end", "none");
                     } else {
                        this.path.attr("arrow-start",
                              "none");
                        this.path.attr("arrow-end",
                              "block-wide-long");
                     }
                  } else if (mappingExist.output) {
                     if (this.fromAnchorPoint.symbol.type == m_constants.DATA_SYMBOL) {
                        this.path.attr("arrow-start",
                              "block-wide-long");
                        this.path.attr("arrow-end", "none");
                     } else {
                        this.path.attr("arrow-start",
                              "none");
                        this.path.attr("arrow-end",
                              "block-wide-long");
                     }
                  }
               }
            };

            /**
            *
            */
            Connection.prototype.adjustConditionExpressionText = function() {
               if (this.modelElement && this.modelElement.attributes) {
                  this.conditionExpressionTextXOffset = parseInt(this.modelElement.attributes["carnot:engine:conditionExpressionTextXOffset"]);
                  this.conditionExpressionTextYOffset = parseInt(this.modelElement.attributes["carnot:engine:conditionExpressionTextYOffset"]);
               }
               if (!this.conditionExpressionTextXOffset
                     || !this.conditionExpressionTextYOffset) {
                  if (this.toAnchorPoint.orientation == m_constants.NORTH) {
                     this.conditionExpressionTextXOffset = m_constants.CONNECTION_EXPRESSION_OFFSET;
                     this.conditionExpressionTextYOffset = m_constants.CONNECTION_EXPRESSION_OFFSET * -2;
                  } else if (this.toAnchorPoint.orientation == m_constants.EAST) {
                     this.conditionExpressionTextXOffset = m_constants.CONNECTION_EXPRESSION_OFFSET * 2;
                     this.conditionExpressionTextYOffset = m_constants.CONNECTION_EXPRESSION_OFFSET * -1;
                  } else if (this.toAnchorPoint.orientation == m_constants.SOUTH) {
                     this.conditionExpressionTextXOffset = m_constants.CONNECTION_EXPRESSION_OFFSET;
                     this.conditionExpressionTextYOffset = m_constants.CONNECTION_EXPRESSION_OFFSET * 2;
                  } else if (this.toAnchorPoint.orientation == m_constants.WEST) {
                     this.conditionExpressionTextXOffset = m_constants.CONNECTION_EXPRESSION_OFFSET * -4;
                     this.conditionExpressionTextYOffset = m_constants.CONNECTION_EXPRESSION_OFFSET * -1;
                  }
               }
            };


            /**
            *
            */
            Connection.prototype.reroute = function() {
               var t = null, p = null;
               if (this.isControlFlow()) {
                  this.segments = new Array();

                  // if (this.isPrepared()) {
                  // this.toAnchorPoint.x += 1;
                  // this.toAnchorPoint.y += 1;
                  // }

                  var sourceBox, targetBox;
                  if (this.fromAnchorPoint.symbol) {
                     sourceBox = {
                        left : this.fromAnchorPoint.symbol.x
                              - m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH,
                        top : this.fromAnchorPoint.symbol.y
                              - m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH,
                        right : this.fromAnchorPoint.symbol.x
                              + this.fromAnchorPoint.symbol.width
                              + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH,
                        bottom : this.fromAnchorPoint.symbol.y
                              + this.fromAnchorPoint.symbol.height
                              + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH
                     };
                  }
                  if (this.toAnchorPoint.symbol) {
                     targetBox = {
                        left : this.toAnchorPoint.symbol.x
                              - m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH,
                        top : this.toAnchorPoint.symbol.y
                              - m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH,
                        right : this.toAnchorPoint.symbol.x
                              + this.toAnchorPoint.symbol.width
                              + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH,
                        bottom : this.toAnchorPoint.symbol.y
                              + this.toAnchorPoint.symbol.height
                              + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH
                     };
                  }

                  var offset;
                  var sourceX;
                  var sourceY;
                  var targetX;
                  var targetY;
                  if (this.fromAnchorPoint.orientation === this.toAnchorPoint.orientation) {
                     // definitely not a straight line ....
                     var fromOffset = {
                        dx : (this.fromAnchorPoint.symbol.width / 2),
                        dy : this.fromAnchorPoint.symbol.height / 2
                     };
                     var toOffset = {
                        dx : (this.toAnchorPoint.symbol.width / 2),
                        dy : this.toAnchorPoint.symbol.height / 2
                     };
                     // center of source/target symbol
                     var sourceX = this.fromAnchorPoint.symbol.x
                           + fromOffset.dx;
                     var sourceY = this.fromAnchorPoint.symbol.y
                           + fromOffset.dy;
                     var targetX = this.toAnchorPoint.symbol.x
                           + toOffset.dx;
                     var targetY = this.toAnchorPoint.symbol.y
                           + toOffset.dy;
                     // ensure first/last segment extends beyond both
                     // source and target symbol
                     offset = {
                        dx : fromOffset.dx >= toOffset.dx ? fromOffset.dx
                              : toOffset.dx,
                        dy : fromOffset.dy >= toOffset.dy ? fromOffset.dy
                              : toOffset.dy
                     };
                  } else {
                     offset = {
                        dx : 0,
                        dy : 0
                     };
                     var sourceX = this.fromAnchorPoint.x;
                     var sourceY = this.fromAnchorPoint.y;
                     var targetX = this.toAnchorPoint.x;
                     var targetY = this.toAnchorPoint.y;
                  }

                  // Adjust target

                  if (this.toAnchorPoint.orientation == m_constants.NORTH) {
                     targetY -= (offset.dy + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH);
                  } else if (this.toAnchorPoint.orientation == m_constants.EAST) {
                     targetX += (offset.dx + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH);
                  } else if (this.toAnchorPoint.orientation == m_constants.SOUTH) {
                     targetY += (offset.dy + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH);
                  } else if (this.toAnchorPoint.orientation == m_constants.WEST) {
                     targetX -= (offset.dx + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH);
                  }

                  // Add first segment

                  var currentSegment = null;

                  if (this.fromAnchorPoint.orientation == m_constants.NORTH) {
                     this.segments
                           .push(currentSegment = new Segment(
                                 this.fromAnchorPoint.x,
                                 this.fromAnchorPoint.y,
                                 sourceX,
                                 sourceY
                                       - (offset.dy + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH),
                                 currentSegment));

                     // avoid route crossing source element
                     if ((sourceY < targetY)
                           && (sourceBox && (sourceBox.left < targetX) && (targetX < sourceBox.right))) {
                        this.segments
                              .push(currentSegment = new Segment(
                                    currentSegment.toX,
                                    currentSegment.toY,
                                    sourceBox.right,
                                    currentSegment.toY,
                                    currentSegment));
                     }
                  } else if (this.fromAnchorPoint.orientation == m_constants.EAST) {
                     this.segments
                           .push(currentSegment = new Segment(
                                 this.fromAnchorPoint.x,
                                 this.fromAnchorPoint.y,
                                 sourceX
                                       + (offset.dx + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH),
                                 sourceY, currentSegment));
                  } else if (this.fromAnchorPoint.orientation == m_constants.SOUTH) {
                     this.segments
                           .push(currentSegment = new Segment(
                                 this.fromAnchorPoint.x,
                                 this.fromAnchorPoint.y,
                                 sourceX,
                                 sourceY
                                       + (offset.dy + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH),
                                 currentSegment));

                     // avoid route crossing source element
                     if ((sourceY > targetY)
                           && (sourceBox && (sourceBox.left < targetX) && (targetX < sourceBox.right))) {
                        this.segments
                              .push(currentSegment = new Segment(
                                    currentSegment.toX,
                                    currentSegment.toY,
                                    sourceBox.right,
                                    currentSegment.toY,
                                    currentSegment));
                     }
                  } else if (this.fromAnchorPoint.orientation == m_constants.WEST) {
                     this.segments
                           .push(currentSegment = new Segment(
                                 this.fromAnchorPoint.x,
                                 this.fromAnchorPoint.y,
                                 sourceX
                                       - (offset.dx + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH),
                                 sourceY, currentSegment));
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

                  if (this.fromAnchorPoint.orientation == m_constants.NORTH) {
                     this.defaultIndicatorPath
                           .attr({
                              'path' : "M"
                                    + (this.fromAnchorPoint.x - m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
                                    + " "
                                    + (this.fromAnchorPoint.y
                                          - m_constants.CONNECTION_DEFAULT_PATH_OFFSET - m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
                                    + "L"
                                    + (this.fromAnchorPoint.x + m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
                                    + " "
                                    + (this.fromAnchorPoint.y
                                          - m_constants.CONNECTION_DEFAULT_PATH_OFFSET + m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
                           });
                  } else if (this.fromAnchorPoint.orientation == m_constants.EAST) {
                     this.defaultIndicatorPath
                           .attr({
                              'path' : "M"
                                    + (this.fromAnchorPoint.x
                                          + m_constants.CONNECTION_DEFAULT_PATH_OFFSET - m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
                                    + " "
                                    + (this.fromAnchorPoint.y - m_constants.CONNECTION_DEFAULT_PATH_OFFSET / 2)
                                    + "L"
                                    + (this.fromAnchorPoint.x
                                          + m_constants.CONNECTION_DEFAULT_PATH_OFFSET + m_constants.CONNECTION_DEFAULT_PATH_LENGTH)
                                    + " "
                                    + (this.fromAnchorPoint.y + m_constants.CONNECTION_DEFAULT_PATH_OFFSET / 2)
                           });
                  } else if (this.fromAnchorPoint.orientation == m_constants.SOUTH) {
                     this.defaultIndicatorPath
                           .attr({
                              'path' : "M"
                                    + (this.fromAnchorPoint.x
                                          - m_constants.CONNECTION_DEFAULT_PATH_OFFSET + m_constants.CONNECTION_DEFAULT_PATH_LENGTH)
                                    + " "
                                    + (this.fromAnchorPoint.y + m_constants.CONNECTION_DEFAULT_PATH_LENGTH)
                                    + "L"
                                    + (this.fromAnchorPoint.x + m_constants.CONNECTION_DEFAULT_PATH_OFFSET / 2)
                                    + " "
                                    + (this.fromAnchorPoint.y
                                          + m_constants.CONNECTION_DEFAULT_PATH_OFFSET + m_constants.CONNECTION_DEFAULT_PATH_LENGTH)
                           });
                  } else if (this.fromAnchorPoint.orientation == m_constants.WEST) {
                     this.defaultIndicatorPath
                           .attr({
                              'path' : "M"
                                    + (this.fromAnchorPoint.x
                                          - m_constants.CONNECTION_DEFAULT_PATH_OFFSET - m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
                                    + " "
                                    + (this.fromAnchorPoint.y - m_constants.CONNECTION_DEFAULT_PATH_LENGTH)
                                    + "L"
                                    + (this.fromAnchorPoint.x
                                          - m_constants.CONNECTION_DEFAULT_PATH_OFFSET + m_constants.CONNECTION_DEFAULT_PATH_LENGTH / 2)
                                    + " "
                                    + (this.fromAnchorPoint.y + m_constants.CONNECTION_DEFAULT_PATH_OFFSET / 2)
                           });

                  }

                  if (!this.conditionExpressionTextXOffset
                        || !this.conditionExpressionTextYOffset) {
                     this.adjustConditionExpressionText();
                  }

                  this.conditionExpressionText.attr({
                     x : this.toAnchorPoint.x
                           + this.conditionExpressionTextXOffset,
                     y : this.toAnchorPoint.y
                           + this.conditionExpressionTextYOffset
                  });

                  var fromAnchor = { x: this.fromAnchorPoint.x,
                        y: this.fromAnchorPoint.y,
                        orientation: this.fromAnchorPoint.orientation
                  };
                  var toAnchor = { x: this.toAnchorPoint.x,
                     y: this.toAnchorPoint.y,
                     orientation: this.toAnchorPoint.orientation
                  };

                  var swapX = this.toAnchorPoint.x < this.fromAnchorPoint.x;
                  var swapY = this.toAnchorPoint.y < this.fromAnchorPoint.y;
                  var xShift = swapX ? this.toAnchorPoint.x : this.fromAnchorPoint.x;
                  var yShift = swapY ? this.toAnchorPoint.y : this.fromAnchorPoint.y;

                  t = "T" + xShift + " " + yShift;
                  p = this.getPathSvgString(fromAnchor, toAnchor);
               }
               else {
                  t = "T" + (xShift * -1) + " " + (yShift * -1);
                  p = this.getSvgString();
               }

               if (this.isCompleted()) {
                  this.auxiliaryPickPath.attr({
                     'path' : p
                  });
                  this.auxiliaryPickPath.attr({
                     'transform' : t
                  });
                  this.proximitySensor.attr({
                     'path' : p
                  });
                  this.proximitySensor.attr({
                     'transform' : t
                  });
               }

               this.path.attr({
                  'path' : p
               });
               this.path.attr({
                  'transform' : t
               });
            };

            Connection.prototype.getOrientation = function(orientation) {
               switch (orientation) {
                  case 0: // m_constants.NORTH:
                     return [0, -1];
                     break;

                  case 1: // m_constants.EAST:
                     return [1, 0];
                     break;

                  case 2: // m_constants.SOUTH:
                     return [0, 1];
                     break;

                  case 3: //m_constants.WEST:
                     return [-1, 0];
                     break;

                  default:
                     return [0, 0];
               }
            };

            Connection.prototype.getPathSvgString = function(fromAnchor, toAnchor) {
               var connectorParams = { lineWidth: 1,
                  sourcePos: [fromAnchor.x, fromAnchor.y],
                  targetPos: [toAnchor.x, toAnchor.y],
                  sourceEndpoint: {anchor: {orientation: this.getOrientation(fromAnchor.orientation), elementId: "a"}},
                  targetEndpoint: {anchor: {orientation: this.getOrientation(toAnchor.orientation)}, elementId: "b"}
               };

               // From jQuery.jsPlumb.js > jsPlumb.Connection
               var makeConnector = function(renderMode, connectorName, connectorArgs) {
                  var c = new Object();
                  if (!jsPlumb.Defaults.DoNotThrowErrors && jsPlumb.Connectors[connectorName] == null)
                        throw { msg:"jsPlumb: unknown connector type '" + connectorName + "'" };

                  jsPlumb.Connectors[connectorName].apply(c, [connectorArgs]);
                  // jsPlumb.ConnectorRenderers[renderMode].apply(c, [connectorArgs]);
                  return c;
               };

               var renderMode = "svg";
               var connectorName = "Flowchart", connectorArgs = {stub: m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH,
                     cornerRadius: m_constants.CONNECTION_DEFAULT_EDGE_RADIUS };
               var connector = makeConnector(renderMode, connectorName, connectorArgs);

               connector.compute(connectorParams);

               // From jQuery.jsPlumb.js > SvgConnector
               var segments = connector.getSegments(), p = "";
               // create path from segments.
               for (var i = 0; i < segments.length; i++) {
                  p += jsPlumb.Segments.svg.SegmentRenderer.getPath(segments[i]);
                  p += " ";
               }

               return p;
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
                  // Horizontal segments
                  if (currentSegment.isHorizontal()) {
                     if ((currentSegment.fromX < currentSegment.toX && currentSegment.toX < targetX)
                           || (currentSegment.fromX > currentSegment.toX && currentSegment.toX > targetX)) {

                        if (this.toAnchorPoint.symbol != null
                              && this.toAnchorPoint.symbol.type != m_constants.SWIMLANE_SYMBOL
                              && !this.diagram.anchorDragEnabled) {
                           // the Anchor point bend location should be
                           // midway between symbol's
                           if (n == 0) {
                              if (currentSegment.toX < targetX) {
                                 var anchorPointMargin = (this.toAnchorPoint.symbol.x - currentSegment.toX) / 2;
                                 currentSegment.toX = currentSegment.toX
                                       + anchorPointMargin;
                              } else {
                                 var anchorPointMargin = (currentSegment.toX - (this.toAnchorPoint.symbol.x + this.toAnchorPoint.symbol.width)) / 2
                                 currentSegment.toX = currentSegment.toX
                                       - anchorPointMargin;
                              }
                           }
                           // Horizontal segment moving down from 3
                           // o'clk (Source Symbol)
                           else if (currentSegment.toY < targetY) {
                              if (targetY != this.toAnchorPoint.y) {
                                 // following scenario bend not
                                 // required
                                 if ((this.fromAnchorPoint.orientation == m_constants.WEST
                                       && this.toAnchorPoint.orientation == m_constants.NORTH && currentSegment.toX > targetX)
                                       || (this.fromAnchorPoint.orientation == m_constants.EAST
                                             && this.toAnchorPoint.orientation == m_constants.NORTH && currentSegment.toX < targetX)) {
                                    currentSegment.toX = targetX;
                                 } else
                                    this.segments
                                          .push(currentSegment = new Segment(
                                                currentSegment.toX,
                                                currentSegment.toY,
                                                currentSegment.toX,
                                                targetY,
                                                currentSegment));
                              } else {
                                 // Symbol connects to 9'clk(move
                                 // right) of
                                 // Symbol 2 or 3'clk (move left) of
                                 // symbol2
                                 this.segments
                                       .push(currentSegment = new Segment(
                                             currentSegment.toX,
                                             currentSegment.toY,
                                             currentSegment.toX,
                                             targetY,
                                             currentSegment));
                              }

                           }
                           // Horizontal segment moving Upward from 3
                           // o'clk
                           else if (currentSegment.toY > targetY) {
                              if (targetY != this.toAnchorPoint.y) {
                                 // following scenario bend not
                                 // required
                                 if ((this.fromAnchorPoint.orientation == m_constants.WEST
                                       && this.toAnchorPoint.orientation == m_constants.SOUTH && currentSegment.toX > targetX)
                                       || (this.fromAnchorPoint.orientation == m_constants.EAST
                                             && this.toAnchorPoint.orientation == m_constants.SOUTH && currentSegment.toX < targetX)) {
                                    currentSegment.toX = targetX;
                                 } else
                                    // Horizontal segment from 3
                                    // o'clk to 3 o'clk
                                    this.segments
                                          .push(currentSegment = new Segment(
                                                currentSegment.toX,
                                                currentSegment.toY,
                                                currentSegment.toX,
                                                targetY
                                                      - m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH,
                                                currentSegment));
                              } else {
                                 // Symbol connects to 9'clk(move
                                 // right) of
                                 // Symbol 2 or 3'clk (move left) of
                                 // symbol2
                                 this.segments
                                       .push(currentSegment = new Segment(
                                             currentSegment.toX,
                                             currentSegment.toY,
                                             currentSegment.toX,
                                             targetY,
                                             currentSegment));
                              }
                           }
                           // Intermediate Horizontal segment from 9
                           // o'clk to 9 o'clk towards right
                           else if (currentSegment.toX < targetX
                                 && targetX > this.toAnchorPoint.x) {
                              this.segments
                                    .push(currentSegment = new Segment(
                                          currentSegment.toX,
                                          currentSegment.toY,
                                          currentSegment.toX,
                                          this.toAnchorPoint.symbol.y
                                                - m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH,
                                          currentSegment));
                           }
                           // Horizontal segment from 9 o'clk to 9
                           // o'clk towards left
                           else if (currentSegment.toX > targetX
                                 && targetX < this.toAnchorPoint.x) {
                              this.segments
                                    .push(currentSegment = new Segment(
                                          currentSegment.toX,
                                          currentSegment.toY,
                                          currentSegment.toX,
                                          this.toAnchorPoint.symbol.y
                                                - m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH,
                                          currentSegment));
                           } else {
                              currentSegment.toX = targetX;
                           }
                        } else
                           currentSegment.toX = targetX;
                     } else {
                        // connect from 6 O'clk to 12 O'clk, when toX
                        // and TargetX match,
                        // the vertical segment needs modification
                        if (this.toAnchorPoint.symbol != null
                              && !this.diagram.anchorDragEnabled
                              && (currentSegment.toY > targetY && this.toAnchorPoint.y > targetY)) {

                           this.segments
                                 .push(currentSegment = new Segment(
                                       currentSegment.toX,
                                       currentSegment.toY,
                                       currentSegment.toX,
                                       (this.toAnchorPoint.symbol.y - m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH),
                                       currentSegment));
                        } else {
                           this.segments
                                 .push(currentSegment = new Segment(
                                       currentSegment.toX,
                                       currentSegment.toY,
                                       currentSegment.toX,
                                       targetY, currentSegment));
                        }
                     }
                  } else {
                     // Vertical segments
                     if ((currentSegment.fromY < currentSegment.toY && currentSegment.toY < targetY)
                           || (currentSegment.fromY > currentSegment.toY && currentSegment.toY > targetY)) {
                        if (this.toAnchorPoint.symbol != null
                              && this.toAnchorPoint.symbol.type != m_constants.SWIMLANE_SYMBOL
                              && !this.diagram.anchorDragEnabled) {

                           if (n == 0) {
                              // the bend location should be midway
                              // between symbols,
                              // update current segment
                              if (currentSegment.toY < targetY) {
                                 var anchorPointMargin = (this.toAnchorPoint.symbol.y - currentSegment.toY) / 2;
                                 currentSegment.toY = currentSegment.toY
                                       + anchorPointMargin;
                              } else {
                                 var anchorPointMargin = (currentSegment.toY - (this.toAnchorPoint.symbol.y + this.toAnchorPoint.symbol.height)) / 2
                                 currentSegment.toY = currentSegment.toY
                                       - anchorPointMargin;
                              }
                           }
                           // Vertical segment from 6'clk moving
                           // downwards
                           else if (currentSegment.toX > targetX) {
                              if (targetX != this.toAnchorPoint.x) {
                                 if ((this.fromAnchorPoint.orientation == m_constants.SOUTH
                                       && this.toAnchorPoint.orientation == m_constants.EAST && currentSegment.toY < targetY)) {
                                    currentSegment.toY = targetY;
                                 } else
                                    this.segments
                                          .push(currentSegment = new Segment(
                                                currentSegment.toX,
                                                currentSegment.toY,
                                                targetX,
                                                currentSegment.toY,
                                                currentSegment));
                              } else {
                                 // Vertical segment from 6'clk to
                                 // 12'clk vice versa
                                 this.segments
                                       .push(currentSegment = new Segment(
                                             currentSegment.toX,
                                             currentSegment.toY,
                                             targetX,
                                             currentSegment.toY,
                                             currentSegment));
                              }
                           }
                           // Vertical segment from 6'clk moving
                           // upwards
                           else if (currentSegment.toX < targetX) {
                              if (targetX != this.toAnchorPoint.x) {
                                 if ((this.fromAnchorPoint.orientation == m_constants.SOUTH
                                       && this.toAnchorPoint.orientation == m_constants.WEST && currentSegment.toY < targetY)
                                       || (this.fromAnchorPoint.orientation == m_constants.NORTH
                                             && this.toAnchorPoint.orientation == m_constants.WEST && currentSegment.toY > targetY)) {
                                    currentSegment.toY = targetY;
                                 } else
                                    // Vertical - From 6 o'clk to 9
                                    // o'clk
                                    // left to right, no bend
                                    // required
                                    this.segments
                                          .push(currentSegment = new Segment(
                                                currentSegment.toX,
                                                currentSegment.toY,
                                                targetX,
                                                currentSegment.toY,
                                                currentSegment));
                              } else {
                                 this.segments
                                       .push(currentSegment = new Segment(
                                             currentSegment.toX,
                                             currentSegment.toY,
                                             targetX,
                                             currentSegment.toY,
                                             currentSegment));
                              }

                           } else if (currentSegment.toY < targetY
                                 && targetY > this.toAnchorPoint.y) {
                              // Intermediate vertical lines,
                              // connecting to 9 O'clk segment
                              this.segments
                                    .push(currentSegment = new Segment(
                                          currentSegment.toX,
                                          currentSegment.toY,
                                          this.toAnchorPoint.symbol.x
                                                - m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH,
                                          currentSegment.toY,
                                          currentSegment));
                           } else if (currentSegment.toY > targetY
                                 && targetY < this.toAnchorPoint.y) {
                              // Intermediate vertical lines,
                              // connecting to 12 O'clk segment
                              this.segments
                                    .push(currentSegment = new Segment(
                                          currentSegment.toX,
                                          currentSegment.toY,
                                          this.toAnchorPoint.symbol.x
                                                + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH,
                                          currentSegment.toY,
                                          currentSegment));
                           } else {
                              currentSegment.toY = targetY;
                           }
                        } else
                           currentSegment.toY = targetY;
                     } else {
                        if (this.toAnchorPoint.symbol != null
                              && !this.diagram.anchorDragEnabled
                              && ((currentSegment.toX < this.fromAnchorPoint.symbol.x) && this.toAnchorPoint.symbol.x
                                    + this.toAnchorPoint.symbol.width < targetX)) {
                           // For scenario connecting from 9
                           // o'clk(Symbol 1) to 3 o'clk(symbol2)
                           if (this.fromAnchorPoint.orientation == m_constants.WEST
                                 && this.toAnchorPoint.orientation == m_constants.EAST) {
                              this.segments
                                    .push(currentSegment = new Segment(
                                          currentSegment.toX,
                                          currentSegment.toY,
                                          targetX,
                                          currentSegment.toY,
                                          currentSegment));
                           } else
                              this.segments
                                    .push(currentSegment = new Segment(
                                          currentSegment.toX,
                                          currentSegment.toY,
                                          this.toAnchorPoint.symbol.x
                                                - m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH,
                                          currentSegment.toY,
                                          currentSegment));
                        } else if (this.toAnchorPoint.symbol != null
                              && !this.diagram.anchorDragEnabled
                              && ((currentSegment.toX >= (this.fromAnchorPoint.symbol.x + this.fromAnchorPoint.symbol.width)) && this.toAnchorPoint.symbol.x > targetX)) {
                           // For scenario connecting from 3
                           // o'clk(Symbol 1) to 9 o'clk(symbol2)
                           if (this.fromAnchorPoint.orientation == m_constants.EAST
                                 && this.toAnchorPoint.orientation == m_constants.WEST) {
                              this.segments
                                    .push(currentSegment = new Segment(
                                          currentSegment.toX,
                                          currentSegment.toY,
                                          targetX,
                                          currentSegment.toY,
                                          currentSegment));
                           } else
                              this.segments
                                    .push(currentSegment = new Segment(
                                          currentSegment.toX,
                                          currentSegment.toY,
                                          (this.toAnchorPoint.symbol.x + this.toAnchorPoint.symbol.width)
                                                + m_constants.CONNECTION_MINIMAL_SEGMENT_LENGTH,
                                          currentSegment.toY,
                                          currentSegment));
                        } else {
                           this.segments
                                 .push(currentSegment = new Segment(
                                       currentSegment.toX,
                                       currentSegment.toY,
                                       targetX,
                                       currentSegment.toY,
                                       currentSegment));
                        }
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
               if (this.diagram.currentConnection) {
                  this.diagram.currentConnection.deselect();
               };

               this.selected = true;

               this.path
                     .attr({
                        "stroke-width" : m_constants.CONNECTION_SELECT_STROKE_WIDTH
                     });

               this.diagram.currentConnection = this;
               this.fromAnchorPoint.show();
               this.toAnchorPoint.show();
               m_propertiesPanel.initializePropertiesPanel(this);
            };

            /**
            *
            */
            Connection.prototype.deselect = function() {
               this.selected = false;

               if(!this.path.removed){
                  this.path
                  .attr({
                     "stroke-width" : m_constants.CONNECTION_STROKE_WIDTH
                  });
               }

               this.fromAnchorPoint.hide();
               this.toAnchorPoint.hide();

            };

            /**
            *
            */
            Connection.prototype.dragMove = function(dX, dY, x, y, event) {
                return false;
              
               /*if (this.clickedSegmentIndex > 0
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
               }*/
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
                                    + this.diagram.getCanvasPosition().left, y
                                    / this.diagram.zoomFactor
                                    + this.diagram.getCanvasPosition().top);

                  if (symbol != null) {
                     m_utils.debug("Symbol found");
                     var anchorPoint = symbol.getClosestAnchorPoint(x
                           / this.diagram.zoomFactor
                           + this.diagram.getCanvasPosition().left, y
                           / this.diagram.zoomFactor
                           + this.diagram.getCanvasPosition().top);
                     this.diagram.setAnchorPoint(anchorPoint);
                  }
                  else {
                     this.diagram.disEngageConnection();
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
               this.diagram.mode = this.diagram.SYMBOL_MOVE_MODE;
            };

            /**
            *
            */
            Connection.prototype.dragConditionExpressionText = function(dX,
                  dY, x, y, event) {
               this.conditionExpressionTextXOffset = x
                     * this.diagram.zoomFactor - this.diagram.getCanvasPosition().left
                     - this.toAnchorPoint.x;
               this.conditionExpressionTextYOffset = y
                     * this.diagram.zoomFactor - this.diagram.getCanvasPosition().top
                     - this.toAnchorPoint.y;

               this.conditionExpressionText.attr({
                  "x" : this.conditionExpressionTextXOffset + this.toAnchorPoint.x,
                  "y" : this.conditionExpressionTextYOffset + this.toAnchorPoint.y
               });
            };

            /**
            *
            */
            Connection.prototype.dragStopConditionExpressionText = function(
                  x, y, event) {

               var changes = {
                  modelElement : {
                     attributes : {
                     "carnot:engine:conditionExpressionTextXOffset" : this.conditionExpressionTextXOffset,
                     "carnot:engine:conditionExpressionTextYOffset" : this.conditionExpressionTextYOffset
                     }
                  }
               };

               this.createUpdateCommand(changes);
               this.diagram.mode = this.diagram.NORMAL_MODE;
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
                  imageUrl : "plugins/bpm-modeler/images/icons/delete.png",
                  imageWidth : 16,
                  imageHeight : 16,
                  clickHandler : Connection_removeClosure
               }, {
                  imageUrl : "plugins/bpm-modeler/images/icons/connect.png",
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
                  var scrollPos = m_modelerUtils
                        .getModelerScrollPosition();

                  this.adjustFlyOutMenu(event.pageX
                        - this.diagram.getCanvasPosition().left
                        - m_constants.FLY_OUT_MENU_ITEM_MARGIN,
                        event.pageY - this.diagram.getCanvasPosition().top
                              - FLYOUT_MENU_LOC_OFFSET, 60, 30);
                  // If connection hoverIn is called before other symbol
                  // hoverOut, manual HoverOut is required.
                  if (this.diagram.currentFlyOutSymbol
                        && this.diagram.currentFlyOutSymbol.oid != this.oid) {
                     if (!this.diagram.currentFlyOutSymbol
                           .validateProximity(event)) {
                        this.diagram.currentFlyOutSymbol
                              .hideFlyOutMenu();
                     }
                  }
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

            /**
            *
            */
            Connection.prototype.createFlyOutMenuBackground = function(x,
                  y, height, width) {
               this.flyOutMenuBackground = this.diagram.canvasManager
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
            Connection.prototype.adjustFlyOutMenu = function(x, y, width,
                  height) {
               this.flyOutMenuBackground.attr({
                  'x' : x,
                  'y' : y,
                  width : width,
                  height : height
               });

               this.adjustFlyOutMenuItems(x, y, width, height);
            };

            /**
            *
            */
            Connection.prototype.adjustFlyOutMenuItems = function(x, y,
                  width, height) {
               var n = 0;
               while (n < this.bottomFlyOutMenuItems.length) {
                  this.bottomFlyOutMenuItems[n].attr({
                     'x' : x + n
                           * (m_constants.FLY_OUT_MENU_CONTENT_MARGIN)
                           + 10,
                     'y' : y + 2

                  });
                  ++n;
               }
            };

            /**
            *
            */
            Connection.prototype.remove = function() {
               this.removePrimitives();
               this.removeFlyOutMenu();
               this.removeProximitySensor();
               m_utils.removeItemFromArray(this.diagram.currentSelection,
                     this);
               m_utils.removeItemFromArray(this.diagram.connections, this);
               // Remove this connection from FROM and TO Symbol's
               // connection array
               m_utils.removeItemFromArray(
                     this.fromAnchorPoint.symbol.connections, this);
               if (this.toAnchorPoint && this.toAnchorPoint.symbol) {
                  m_utils.removeItemFromArray(
                        this.toAnchorPoint.symbol.connections, this);

               } else if (this.toModelElementOid != null) {
                  // On hover over symbol, connection gets added to symbol
                  // It may not be removed when connection is
                  // disengaged(canvas click)
                  // if connection is removed , but symbol contains the
                  // connection remove It.
                  var symbol = this.diagram.findSymbolByGuid(
                        this.toModelElementOid, this.diagram.model.id)
                  if (null != symbol) {
                     m_utils.removeItemFromArray(symbol.connections,
                           this);
                  }
               }
            };

            /**
            *
            */
            Connection.prototype.removePrimitives = function() {
               var n = 0;

               while (n < this.primitives.length) {
                  if (this.primitives[n].node) {
                     this.primitives[n].remove();
                  }
                  n++;
               }
            };
            /**
            * sync : Synchronous AJAX call is made, if set(needed in
            * scenario like rerouting a connection(Create new connection
            * and remove original))
            */
            Connection.prototype.createDeleteCommand = function(sync) {
               var command = m_command.createRemoveNodeCommand(
                     "connection.delete", this.diagram.model.id,
                     this.diagram.process.oid, {
                        "oid" : this.oid
                     });
               command.sync = sync ? true : false;
               m_commandsController.submitCommand(command);
            };

            /**
            *
            */
            Connection.prototype.createUpdateCommand = function(changes) {
               var command = m_command.createUpdateModelElementCommand(
                     this.diagram.model.id, this.oid, changes);
               m_commandsController.submitCommand(command);
            };

            /**
             * 
             */
            Connection.prototype.createDataMapping = function(changes) {
              var command = m_command.createCommand("datamapping.create",
                      this.diagram.model.id, this.uuid, changes);
              m_commandsController.submitCommand(command);
            };
            
            /**
            * Nothing required here
            */
            Connection.prototype.validateProximity = function(event) {
               return false;
            }

            /**
            *
            */
            Connection.prototype.hide = function() {
               this.path.hide();
               this.conditionExpressionText.hide();
               this.visible = false;
               this.hideFlyOutMenu();
            }

            /**
            *
            */
            Connection.prototype.show = function() {
               this.path.show();
               this.conditionExpressionText.show();
               this.visible = true;
            }

            /**
            * Validate connection rules for symbols
            */
            Connection.prototype.validateCreateConnection = function(
                  fromAnchorPoint, toAnchorPoint) {
               m_messageDisplay.clearErrorMessages();
               if (fromAnchorPoint.symbol.type == m_constants.EVENT_SYMBOL) {
                  // Check for OUT connections on End Event
                  if (fromAnchorPoint.symbol.modelElement.eventType == m_constants.STOP_EVENT_TYPE) {
                     m_messageDisplay
                           .showErrorMessage( m_i18nUtils.getProperty("modeler.messagedisplay.messages.error.onlyIncomingSeq"));
                     return false;
                  }// Connection between Event and Data not supported
                  else if (null != toAnchorPoint
                        && toAnchorPoint.symbol.type == m_constants.DATA_SYMBOL) {
                     m_messageDisplay
                           .showErrorMessage(m_i18nUtils.getProperty("modeler.messagedisplay.messages.error.dataConnNotSup"));
                     return false;
                  } else if (!fromAnchorPoint.symbol
                        .validateCreateConnection(this)) {
                     // Start Event can have only one OUT connection
                     m_messageDisplay
                           .showErrorMessage(m_i18nUtils.getProperty("modeler.messagedisplay.messages.error.noFurtherConnEvt"));
                     return false;
                  }
               } else if (fromAnchorPoint.symbol.type == m_constants.DATA_SYMBOL) {
                  if (null != toAnchorPoint
                        && (toAnchorPoint.symbol.type == m_constants.GATEWAY_SYMBOL || toAnchorPoint.symbol.type == m_constants.EVENT_SYMBOL)) {
                     m_messageDisplay
                           .showErrorMessage(m_i18nUtils.getProperty("modeler.messagedisplay.messages.error.dataAssocNotSup"));
                     return false;
                  } else if (null != toAnchorPoint
                        && (toAnchorPoint.symbol.type == m_constants.DATA_SYMBOL)) {
                     m_messageDisplay
                           .showErrorMessage(m_i18nUtils.getProperty("modeler.messagedisplay.messages.error.DataSymbolsCan"));
                     return false;
                  } else if (null != toAnchorPoint
                        && (toAnchorPoint.symbol.type == m_constants.ACTIVITY_SYMBOL)) {
                     if (!toAnchorPoint.symbol
                           .validateCreateConnection(this)) {
                        m_messageDisplay
                              .showErrorMessage(m_i18nUtils.getProperty("modeler.messagedisplay.messages.error.dataEleAlready"));
                        return false;
                     }
                  }

                  if (!fromAnchorPoint.symbol
                        .validateCreateConnection()) {
                     m_messageDisplay
                           .showErrorMessage(m_i18nUtils.getProperty("modeler.messagedisplay.messages.error.noFurtherConnDS"));
                     return false;
                  }
               } else if (fromAnchorPoint.symbol.type == m_constants.ACTIVITY_SYMBOL) {
                  // Data symbol validation called
                  if (null != toAnchorPoint
                        && (toAnchorPoint.symbol.type == m_constants.DATA_SYMBOL)) {
                     // Validation handled on Data Symbol for data
                     // connections.
                     if (!toAnchorPoint.symbol
                           .validateCreateConnection()) {
                        m_messageDisplay
                              .showErrorMessage(m_i18nUtils.getProperty("modeler.messagedisplay.messages.error.noFutherOutConnAct"));
                        return false;
                     }

                     if (!fromAnchorPoint.symbol
                           .validateCreateConnection(this)) {
                        m_messageDisplay
                              .showErrorMessage(m_i18nUtils.getProperty("modeler.messagedisplay.messages.error.dataEleAlready"));
                        return false;
                     }

                  } else if (!fromAnchorPoint.symbol
                        .validateCreateConnection(this)) {
                     m_messageDisplay
                           .showErrorMessage(m_i18nUtils.getProperty("modeler.messagedisplay.messages.error.noFurtherConnAct"));
                     return false;
                  }
               } else if (fromAnchorPoint.symbol.type == m_constants.GATEWAY_SYMBOL) {
                  if (null != toAnchorPoint
                        && toAnchorPoint.symbol.type == m_constants.DATA_SYMBOL) {
                     m_messageDisplay
                           .showErrorMessage(m_i18nUtils.getProperty("modeler.messagedisplay.messages.error.dataConnNotSup"));
                     return false;
                  }
               }

               if (toAnchorPoint != null) {
                  if (toAnchorPoint.symbol.type == m_constants.SWIMLANE_SYMBOL
                        || toAnchorPoint.symbol.type == m_constants.POOL_SYMBOL) {
                     return false;
                  } else if (toAnchorPoint.symbol.type == m_constants.EVENT_SYMBOL) {
                     // Check for IN connections on Start Event
                     if (fromAnchorPoint.symbol.modelElement.eventType == m_constants.STOP_EVENT_TYPE) {
                        m_messageDisplay
                              .showErrorMessage(m_i18nUtils.getProperty("modeler.messagedisplay.messages.error.onlyOutSeqFlow"));
                        return false;
                     } else if (!toAnchorPoint.symbol
                           .validateCreateConnection(this)) {
                        return false;
                     }
                  } else if (toAnchorPoint.symbol.type == m_constants.DATA_SYMBOL) {
                     if (!toAnchorPoint.symbol
                           .validateCreateConnection()) {
                        m_messageDisplay
                              .showErrorMessage(m_i18nUtils.getProperty("modeler.messagedisplay.messages.error.noFutherOutConnAct"));
                        return false;
                     }
                  } else if (toAnchorPoint.symbol.type == m_constants.ACTIVITY_SYMBOL) {
                     if ((fromAnchorPoint.symbol.type != m_constants.DATA_SYMBOL)
                           && !toAnchorPoint.symbol
                                 .validateCreateConnection(this)) {
                        m_messageDisplay
                              .showErrorMessage(m_i18nUtils.getProperty("modeler.messagedisplay.messages.error.noFurtherConnActTo"));
                        return false;
                     }
                  } else if (toAnchorPoint.symbol.isPoolSymbol()
                        || toAnchorPoint.symbol.type == m_constants.SWIMLANE_SYMBOL) {
                     return false;
                  }

                  // If Start and End symbol are same, show error
                  if (fromAnchorPoint.symbol.oid != null
                        && fromAnchorPoint.symbol.oid == toAnchorPoint.symbol.oid) {
                     m_messageDisplay
                           .showErrorMessage(m_i18nUtils.getProperty("modeler.messagedisplay.messages.error.connMustContain2"));
                     return false;
                  }

                  //to be removed in future
                  if (m_constants.ANNOTATION_SYMBOL == toAnchorPoint.symbol.type
                        || m_constants.ANNOTATION_SYMBOL == fromAnchorPoint.symbol.type) {
                     m_messageDisplay
                     .showErrorMessage(m_i18nUtils.getProperty("modeler.messagedisplay.messages.error.connToFromAnnot"));
                     return false;
                  }

               }

               return true;
            };

            /**
            *
            */
            Connection.prototype.flipFlowOrientation = function(
                  flowOrientation) {
               // set fromAnchorPoint
               var index = this
                     .flipAnchorPoint(this.fromAnchorPoint.orientation);
               this.fromAnchorPoint.orientation = index;

               // set toAnchorPoint
               index = this
                     .flipAnchorPoint(this.toAnchorPoint.orientation);
               this.toAnchorPoint.orientation = index;

               var changesConn = {
                  modelElement : {
                     toAnchorPointOrientation : this.toAnchorPoint.orientation,
                     fromAnchorPointOrientation : this.fromAnchorPoint.orientation
                  }
               };

               this.reroute();

               return {
                  oid : this.oid,
                  changes : changesConn
               };
            };

            // This method is written to handle Gateway anchor points
            Connection.prototype.flipAnchorPoint = function(orientation) {
               var newOrientation = 0;
               if (orientation == m_constants.NORTH) {
                  newOrientation = m_constants.WEST;
               } else if (orientation == m_constants.EAST) {
                  newOrientation = m_constants.SOUTH;
               } else if (orientation == m_constants.SOUTH) {
                  newOrientation = m_constants.EAST;
               } else if (orientation == m_constants.WEST) {
                  newOrientation = m_constants.NORTH;
               }
               return newOrientation;
            };

            Connection.prototype.showEditable = function() {
               this.conditionExpressionText.hide();
               var editableText = this.diagram.editableText;
               var scrollPos = m_modelerUtils.getModelerScrollPosition();

               var name = this.conditionExpressionText.attr("text");

               var textboxWidth = this.conditionExpressionText.getBBox().width + 20;
               var textboxHeight = this.conditionExpressionText.getBBox().height;

               if (textboxWidth < m_constants.DEFAULT_TEXT_WIDTH
                     || textboxHeight < m_constants.DEFAULT_TEXT_HEIGHT) {
                  textboxWidth = m_constants.DEFAULT_TEXT_WIDTH;
                  textboxHeight = m_constants.DEFAULT_TEXT_HEIGHT;
               }

               editableText.css("width", parseInt(textboxWidth.valueOf()));
               editableText.css("height",
                     parseInt(textboxHeight.valueOf()));

               editableText.css("visibility", "visible").html(name)
                     .moveDiv(
                           {
                              "x" : this.conditionExpressionText
                                    .getBBox().x
                                    + this.diagram.getCanvasPosition().left,
                              "y" : this.conditionExpressionText
                                    .getBBox().y
                                    + this.diagram.getCanvasPosition().top
                     }).show().trigger("dblclick");

               return this.conditionExpressionText;
            };

            Connection.prototype.postComplete = function() {
               this.select();
               this.diagram.showEditable(this.text);
            };

            Connection.prototype.adjustPrimitivesOnShrink = function() {
               if (this.parentSymbol && this.parentSymbol.minimized) {
                  return;
               }
               if (this.conditionExpressionText) {
                  if (this.conditionExpressionText.getBBox().width > (4.0 * this.width)) {
                     var words = this.conditionExpressionText
                           .attr("text");
                     m_utils.textWrap(this.conditionExpressionText,
                           4.0 * this.width);
                  }
               }
            };

            Connection.prototype.getEditedChanges = function(content) {
               return {
                  modelElement : {
                     conditionExpression : content
                  }
               };
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
            this.auxiliaryProperties.callbackScope.createDeleteCommand();
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