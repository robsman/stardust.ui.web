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
 * Templating Overlay
 *
 * @author
 */
define(
         [ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_urlUtils",
                  "bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_constants",
                  "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_command",
                  "bpm-modeler/js/m_model", "bpm-modeler/js/m_accessPoint",
                  "bpm-modeler/js/m_typeDeclaration",
                  "bpm-modeler/js/m_parameterDefinitionsPanel",
                  "bpm-modeler/js/m_codeEditorAce",
                  "bpm-modeler/js/m_routeDefinitionUtils","bpm-modeler/js/m_user" ],
         function(m_utils, m_urlUtils, m_i18nUtils, m_constants, m_commandsController,
                  m_command, m_model, m_accessPoint, m_typeDeclaration,
                  m_parameterDefinitionsPanel, m_codeEditorAce, m_routeDefinitionUtils,m_user)
         {
            return {
               create : function(view)
               {
                  var overlay = new TemplatingIntegrationOverlay();
                  overlay.initialize(view);
                  return overlay;
               }
            };
            /**
             *
             */
            function TemplatingIntegrationOverlay()
            {
               /**
                *
                */
               TemplatingIntegrationOverlay.prototype.initialize = function(view)
               {
                  this.view = view;
                  this.view
                           .insertPropertiesTab(
                                    "templatingIntegrationOverlay",
                                    "parameters",
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.templating.parameters.title"),
                                    "plugins/bpm-modeler/images/icons/database_link.png");

                  this.locationInput = m_utils
                           .jQuerySelect("#templatingIntegrationOverlay #configurationTab #locationInput");
                  this.formatInput = m_utils
                           .jQuerySelect("#templatingIntegrationOverlay #configurationTab #formatInput");
                  this.textAreaConfigurationDiv = m_utils
                           .jQuerySelect("#templatingIntegrationOverlay #configurationTab #textAreaConfiguration");
                  this.templateRow = m_utils
                           .jQuerySelect("#templatingIntegrationOverlay #configurationTab #templateRow");
                  this.templateInput = m_utils
                           .jQuerySelect("#templatingIntegrationOverlay #configurationTab #templateInput");
                  this.outputNameRow = m_utils
                           .jQuerySelect("#templatingIntegrationOverlay #configurationTab #outputNameRow");
                  this.outputNameInput = m_utils
                           .jQuerySelect("#templatingIntegrationOverlay #configurationTab #outputNameInput");
                  this.convertToPdfRow = m_utils
                           .jQuerySelect("#templatingIntegrationOverlay #configurationTab #convertToPdfRow");
                  this.convertToPdfInput = m_utils
                           .jQuerySelect("#templatingIntegrationOverlay #configurationTab #convertToPdfInput");
                  this.autoStartupRow = m_utils
                  .jQuerySelect("#templatingIntegrationOverlay #configurationTab #autoStartupRow");
                  this.autoStartupInput = m_utils
                           .jQuerySelect("#templatingIntegrationOverlay #configurationTab #autoStartupInput");
                  this.deleteParameterDefinitionButton = m_utils
                           .jQuerySelect("#parametersTab #deleteParameterDefinitionButton");
                  this.outputAccessPointRow = m_utils
                           .jQuerySelect("#templatingIntegrationOverlay #configurationTab #outputAccessPointRow");
                  this.outputAccessPointInput = m_utils
                           .jQuerySelect("#templatingIntegrationOverlay #configurationTab #outputAccessPointInput");
                  this.sourceTypeInput = m_utils
                           .jQuerySelect("#templatingIntegrationOverlay #configurationTab #sourceTypeInput");
                  this.sourceTypeInput.hide();


                  this.editorAnchor = m_utils.jQuerySelect("#codeEditorDiv").get(0);
                  this.parameterDefinitionDirectionSelect = m_utils
                           .jQuerySelect("#parametersTab #parameterDefinitionDirectionSelect");

                  this.locationInput
                           .append("<option value=\"embedded\" selected>"
                                    + m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.templating.locationInput.embedded.label")
                                    + "</option>");
                  this.locationInput
                           .append("<option value=\"classpath\">"
                                    + m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.templating.locationInput.classpath.label")
                                    + "</option>");
                  this.locationInput
                           .append("<option value=\"repository\">"
                                    + m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.templating.locationInput.repository.label")
                                    + "</option>");
                  this.locationInput
                           .append("<option value=\"data\">"
                                    + m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.templating.locationInput.data.label")
                                    + "</option>");

                  this.locationInput.change({
                      panel : this
                   }, function(event)
                   {
                      event.data.panel.view.clearErrorMessages();
                      var accessPointList=event.data.panel.getApplication().contexts.application.accessPoints;
                      var defaultInputAp=m_routeDefinitionUtils.findAccessPoint(accessPointList, "defaultInputAp");
                      if(event.data.panel.locationInput.val()!="data" && defaultInputAp!=null){
                         accessPointList=m_routeDefinitionUtils.filterAccessPoint(accessPointList, "defaultInputAp");
                         event.data.panel.submitParameterDefinitionsChanges(accessPointList);
                      }
                      if(event.data.panel.locationInput.val()=="data"){
                            if(defaultInputAp!=null){
                               accessPointList=m_routeDefinitionUtils.filterAccessPoint(accessPointList, "defaultInputAp");
                            }
                            accessPointList.push({
                               id : "defaultInputAp",
                               name : "template",
                               dataType : "primitive",
                               primitiveDataType : "String",
                               direction : "IN",
                               attributes : {
                                  "stardust:predefined" : true
                               }
                            });

                         event.data.panel.view.submitChanges({
                            contexts : {
                               application : {
                                  accessPoints : accessPointList
                               }
                            }
                         }, true);
                      }
                      event.data.panel.parameterDefinitionsPanel.setParameterDefinitions(accessPointList);
                      event.data.panel.submitChanges(true);
                      event.data.panel.updateView(event.data.panel.locationInput.val());
                   });

                   this.parameterDefinitionNameInput = m_utils.jQuerySelect("#parametersTab #parameterDefinitionNameInput");
                   this.parameterDefinitionNameInput.change({
                       panel : this
                    }, function(event)
                    {
                 	   event.data.panel.submitChanges(false);
                    });
                   this.formatInput.change({
                      panel : this
                   }, function(event)
                   {
                      event.data.panel.view.clearErrorMessages();
                      event.data.panel.submitChanges(true);
                      event.data.panel.updateView(event.data.panel.locationInput.val());
                      event.data.panel.view.validate();
                   });

                   this.templateInput.change({
                      panel : this
                   }, function(event)
                   {
                      event.data.panel.view.clearErrorMessages();
                      event.data.panel.submitChanges();
                   });

                   this.outputNameInput.change({
                      panel : this
                   }, function(event)
                   {
                         event.data.panel.submitChanges(false);
                   });

                   this.convertToPdfInput.change({
                      panel : this
                   }, function(event)
                   {
                      event.data.panel.submitChanges(false);
                      event.data.panel.updateView(event.data.panel.locationInput.val());
                   });

                   this.autoStartupInput.change({
                      panel : this
                   }, function(event)
                   {
                      event.data.panel.submitChanges(false);
                   });
                  
                  this.editorAnchor.id = "codeEditorDiv"
                           + Math.floor((Math.random() * 100000) + 1);
                  this.codeEditor = m_codeEditorAce
                           .getSQLCodeEditor(this.editorAnchor.id);
                  this.codeEditor.loadLanguageTools();
                  this.codeEditor.getEditor().on('blur', function(e)
                  {
                     self.submitChanges(false);
                  });

                  var self = this;
                  this.parameterDefinitionsPanel = m_parameterDefinitionsPanel.create({
                     scope : "parametersTab",
                     submitHandler : this,
                     supportsOrdering : false,
                     supportsDataMappings : false,
                     supportsDescriptors : false,
                     supportsDataTypeSelection : true,
                     supportsDocumentTypes : false,
                     supportsOtherData : false,
                     hideEnumerations : true
                  });

                  this.parameterDefinitionDirectionSelect.empty();
                  this.parameterDefinitionDirectionSelect
                           .append("<option value=\"IN\">"
                                    + m_i18nUtils
                                             .getProperty("modeler.element.properties.commonProperties.in")
                                    + "</option>");

                  this.deleteParameterDefinitionButton.attr("src", m_urlUtils
                           .getContextName()
                           + "/plugins/bpm-modeler/images/icons/delete.png");
                  this.deleteParameterDefinitionButton.click({
                     panel : this
                  }, function(event)
                  {
                     event.data.panel.parameterDefinitionsPanel
                              .deleteParameterDefinition();
                  });

                  this.outputAccessPointInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       event.data.panel.view.clearErrorMessages();
                                       var accessPoints = self.getApplication().contexts.application.accessPoints;
                                       if (self.outputAccessPointInput.val() == m_constants.TO_BE_DEFINED)
                                       {
                                          // set output AP type to default(String)
                                          var defaultAccessPoints = event.data.panel
                                                   .createOrReplaceOutAccessPoint();
                                          event.data.panel
                                                   .submitParameterDefinitionsChanges(defaultAccessPoints);
                                          event.data.panel.view.submitModelElementAttributeChange("stardust:templatingIntegrationOverlay::outputName", null);
                                          event.data.panel.view.submitModelElementAttributeChange("stardust:templatingIntegrationOverlay::template",null);
                                       }
                                       else
                                       {
                                          var filteredAccessPoints = m_routeDefinitionUtils.filterAccessPoint(accessPoints,"defaultOutputAp");

                                          var selectedDataItem = event.data.panel
                                                   .findTypeDeclaration(
                                                            event.data.panel
                                                                     .getScopeModel().typeDeclarations,
                                                            event.data.panel.outputAccessPointInput
                                                                     .val());
                                          var outputAccessPoint = m_routeDefinitionUtils.findAccessPoint(accessPoints,
                                                            "defaultOutputAp");
                                          outputAccessPoint = {
                                                   id : "defaultOutputAp",
                                                   name : "output",
                                                   dataType : "dmsDocument",
                                                   direction : "OUT",
                                                   attributes : {
                                                      "stardust:predefined" : true
                                                   }
                                                };
                                          filteredAccessPoints.push(outputAccessPoint);
                                          event.data.panel.view.submitChanges({
                                             contexts : {
                                                application : {
                                                   accessPoints : filteredAccessPoints
                                                }
                                             }
                                          }, true);
                                          event.data.panel.parameterDefinitionsPanel.setParameterDefinitions(filteredAccessPoints)
                                          event.data.panel.view.submitModelElementAttributeChange("carnot:engine:camel::routeEntries",event.data.panel.getRoute());
                                          event.data.panel.view.validate();
                                     }
                                    });

                  this.sourceTypeInput.change(
                           {
                              panel : this
                           },
                           function(event)
                           {//shown only when location==data
                              event.data.panel.view.clearErrorMessages();
                              var accessPointList=event.data.panel.getApplication().contexts.application.accessPoints;
                              var defaultInputAp=m_routeDefinitionUtils.findAccessPoint(accessPointList, "defaultInputAp");
                              if (self.sourceTypeInput.val() == m_constants.TO_BE_DEFINED)
                              {
                                 if(defaultInputAp!=null){
                                    accessPointList=m_routeDefinitionUtils.filterAccessPoint(accessPointList, "defaultInputAp");
                                 }
                                 accessPointList.push({
                                       id : "defaultInputAp",
                                       name : "template",
                                       dataType : "primitive",
                                       primitiveDataType : "String",
                                       direction : "IN",
                                       attributes : {
                                          "stardust:predefined" : true
                                       }
                                    });
                              }else{
                                 if(defaultInputAp!=null){
                                    accessPointList=m_routeDefinitionUtils.filterAccessPoint(accessPointList, "defaultInputAp");
                                 }
                                 accessPointList.push(
                                          {
                                             id : "defaultInputAp",
                                             name : "template",
                                             dataType : "dmsDocument",
                                             direction : "IN",
                                             attributes : {
                                                "stardust:predefined" : true
                                             }
                                          });
                              }
                              event.data.panel.view.submitChanges({
                                 contexts : {
                                    application : {
                                       accessPoints : accessPointList
                                    }
                                 }
                              }, true);
                              event.data.panel.parameterDefinitionsPanel.setParameterDefinitions(accessPointList);
                              event.data.panel.view.submitModelElementAttributeChange("carnot:engine:camel::routeEntries",event.data.panel.getRoute());
                           });
                  this.update();
               };

               TemplatingIntegrationOverlay.prototype.showHideOutAccessPointRow = function(
                        convertToPdfInput, formatInput)
               {
                  if (convertToPdfInput || formatInput == "docx")
                  {
                     this.outputAccessPointRow.show();
                  }
                  else
                  {
                     this.outputAccessPointRow.hide();
                  }
               }
               TemplatingIntegrationOverlay.prototype.findTypeDeclaration = function(
                        typeDeclarations, fullDataId)
               {
                  var typeDeclaration = {};
                  for ( var i in typeDeclarations)
                  {
                     var item = typeDeclarations[i];
                     if (item.getFullId() == fullDataId)
                     {
                        typeDeclarations = item;
                        break;
                     }
                  }
                  return typeDeclarations;
               }

               TemplatingIntegrationOverlay.prototype.populateFormatInputField = function()
               {
                  this.formatInput.empty();
                  this.formatInput
                           .append("<option value=\"text\">"
                                    + m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.templating.formatInput.text.label")
                                    + "</option>");
                  this.formatInput
                           .append("<option value=\"html\">"
                                    + m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.templating.formatInput.html.label")
                                    + "</option>");
                  this.formatInput
                           .append("<option value=\"xml\">"
                                    + m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.templating.formatInput.xml.label")
                                    + "</option>");
                  if (this.locationInput.val() != "embedded")
                  {
                     this.formatInput
                              .append("<option value=\"docx\">"
                                       + m_i18nUtils
                                                .getProperty("modeler.model.applicationOverlay.templating.formatInput.docx.label")
                                       + "</option>");
                  }
                  if (this.getApplication().attributes["stardust:templatingIntegrationOverlay::format"])
                  {
                	  this.formatInput.val(this.getApplication().attributes["stardust:templatingIntegrationOverlay::format"]);  
                  }
               }
               /**
                *
                */
               TemplatingIntegrationOverlay.prototype.getModelElement = function()
               {
                  return this.view.getModelElement();
               };

               /**
                *
                */
               TemplatingIntegrationOverlay.prototype.getApplication = function()
               {
                  return this.view.application;
               };

               /**
                *
                */
               TemplatingIntegrationOverlay.prototype.getScopeModel = function()
               {
                  return this.view.getModelElement().model;
               };
               /**
                *
                */
               TemplatingIntegrationOverlay.prototype.activate = function()
               {
                  if (this.view.getApplication().contexts.application.accessPoints.length == 0)
                  {
                     var defaultAccessPoints = this.createOrReplaceOutAccessPoint();
                     this.view
                              .submitChanges(
                                       {
                                          contexts : {
                                             application : {
                                                accessPoints : defaultAccessPoints
                                             }
                                          },
                                          attributes : {
                                             "carnot:engine:camel::camelContextId" : "defaultCamelContext",
                                             "carnot:engine:camel::invocationPattern" : "sendReceive",
                                             "carnot:engine:camel::invocationType" : "synchronous",
                                             "carnot:engine:camel::applicationIntegrationOverlay" : "templatingIntegrationOverlay"
                                          }
                                       }, true);
                  }
               };

               TemplatingIntegrationOverlay.prototype.createOrReplaceOutAccessPoint = function()
               {
                  var accessPoints = this.getApplication().contexts.application.accessPoints;
                  var outAccessPoint = m_routeDefinitionUtils.findAccessPoint(accessPoints, "defaultOutputAp");
                  var accessPointList = [];

                  if (outAccessPoint)
                  {
                     accessPointList = m_routeDefinitionUtils.filterAccessPoint(accessPoints, "defaultOutputAp");
                  }
                  accessPointList.push({
                     id : "defaultOutputAp",
                     name : "output",
                     dataType : "primitive",
                     primitiveDataType : "String",
                     direction : "OUT",
                     attributes : {
                        "stardust:predefined" : true
                     }
                  });

                  return accessPointList;
               }

               /**
                *
                */
               TemplatingIntegrationOverlay.prototype.update = function()
               {
                  this.parameterDefinitionsPanel.setScopeModel(this.getScopeModel());
                  this.parameterDefinitionsPanel.setParameterDefinitions(this
                           .getApplication().contexts.application.accessPoints);
                  
                  this.autoStartupRow.hide();
                  if(this.isIntegrator())
                    this.autoStartupRow.show();

                  // intiailize dropdown list with typeDeclarations
                  this.outputAccessPointInput.empty();
                  this.outputAccessPointInput.append("<option value='" + m_constants.TO_BE_DEFINED + "'>" + m_i18nUtils
                           .getProperty("modeler.model.applicationOverlay.templating.formatInput.text.label") + "</option>");
                  this.outputAccessPointInput.append("<option value='dmsDocument'>" + m_i18nUtils
                           .getProperty("modeler.model.applicationOverlay.templating.formatInput.document.label") + "</option>");
                  // ////////////////////////////////////////////////////////////

                  this.sourceTypeInput.empty();
                  this.sourceTypeInput.append("<option value='" + m_constants.TO_BE_DEFINED + "'>" + m_i18nUtils
                           .getProperty("modeler.model.applicationOverlay.templating.formatInput.text.label") + "</option>");
                  this.sourceTypeInput.append("<option value='dmsDocument'>" + m_i18nUtils
                           .getProperty("modeler.model.applicationOverlay.templating.formatInput.document.label") + "</option>");

                  /////////////////////////////////////
                  this.populateFormatInputField();
                  if (this.getApplication().attributes["stardust:templatingIntegrationOverlay::location"])
                  {
                     this.locationInput
                              .val(this.getApplication().attributes["stardust:templatingIntegrationOverlay::location"]);

                  }
                  if (this.getApplication().attributes["stardust:templatingIntegrationOverlay::format"])
                     this.formatInput
                              .val(this.getApplication().attributes["stardust:templatingIntegrationOverlay::format"]);

                  if (this.getApplication().attributes["stardust:templatingIntegrationOverlay::location"] == "embedded")
                  {
                     this.codeEditor
                              .getEditor()
                              .getSession()
                              .setValue(
                                       this.getApplication().attributes["stardust:templatingIntegrationOverlay::content"]);
                  }

                  if (this.getApplication().attributes["stardust:templatingIntegrationOverlay::template"])
                     this.templateInput
                              .val(this.getApplication().attributes["stardust:templatingIntegrationOverlay::template"]);

                  if (this.getApplication().attributes["stardust:templatingIntegrationOverlay::outputName"])
                     this.outputNameInput
                              .val(this.getApplication().attributes["stardust:templatingIntegrationOverlay::outputName"]);

                  this.convertToPdfInput
                           .prop(
                                    "checked",
                                    this.getApplication().attributes["stardust:templatingIntegrationOverlay::convertToPdf"]);
                  
                  if(this.getApplication().attributes["carnot:engine:camel::autoStartup"]==null||this.getApplication().attributes["carnot:engine:camel::autoStartup"]===undefined){
                     this.view.submitModelElementAttributeChange("carnot:engine:camel::autoStartup", true);
                  }
                  this.autoStartupInput
                           .prop(
                                    "checked",
                                    this.getApplication().attributes["carnot:engine:camel::autoStartup"]);

                  var accessPoints = this.getApplication().contexts.application.accessPoints;
                  if (accessPoints.length > 0)
                  {
                     var outputAccessPoint = m_routeDefinitionUtils.findAccessPoint(accessPoints, "defaultOutputAp");
                     var inAccessPoint = m_routeDefinitionUtils.findAccessPoint(accessPoints, "defaultInputAp");
                     if (outputAccessPoint)
                     {
                        if (outputAccessPoint.dataType == "dmsDocument")
                           this.outputAccessPointInput
                                    .val("dmsDocument");
                     }
                     if (inAccessPoint)
                     {
                        if (inAccessPoint.dataType == "dmsDocument")
                           this.sourceTypeInput
                                    .val("dmsDocument");
                     }
                  }
                  // update the view
                  this.updateView(this.locationInput.val());
                  this.view.validate();
               };

               /**
                *
                */
               TemplatingIntegrationOverlay.prototype.updateView = function(location)
               {
                  this.sourceTypeInput.hide();
                  this.textAreaConfigurationDiv.hide();
                  this.outputAccessPointRow.show();
                  this.outputNameRow.hide();
                 
                  if (location == "embedded")
                  {
                     this.textAreaConfigurationDiv.show();
                     this.templateRow.hide();
                  }
                  if (location == "classpath" || location == "repository")
                  {
                     this.templateRow.show();
                  }
                  if (location == "data")
                  {
                     this.templateRow.hide();
                     this.outputNameRow.hide();
                     this.sourceTypeInput.show();
                  }
                  if (this.convertToPdfInput.prop("checked")
                           || this.formatInput.val() == "docx")
                  {
                     this.outputNameRow.show();
                  }
                  var accessPoints = this.getApplication().contexts.application.accessPoints;
                  if (accessPoints.length > 0)
                  {
                     var outputAccessPoint = m_routeDefinitionUtils.findAccessPoint(accessPoints, "defaultOutputAp");
                     if (outputAccessPoint)
                     {
                        if (outputAccessPoint.dataType == "dmsDocument")
                           this.outputNameRow.show();
                     }
                  }
                  this.populateFormatInputField();
               };
               /**
                * returns camel route definition
                */
               TemplatingIntegrationOverlay.prototype.getRoute = function()
               {

                  var accessPoints =this.parameterDefinitionsPanel.parameterDefinitions;
                  var outAccessPoint = m_routeDefinitionUtils.findAccessPoint(accessPoints, "defaultOutputAp");
                  var defaultInputAp = m_routeDefinitionUtils.findAccessPoint(accessPoints, "defaultInputAp");
                  var route = m_routeDefinitionUtils.createTemplatingHandlerRouteDefinition(
                           this.formatInput.val(), this.locationInput.val(),
                           this.codeEditor.getEditor().getSession().getValue(),
                           this.templateInput.val(), this.outputNameInput.val(),
                           this.convertToPdfInput.prop("checked"),defaultInputAp);
                  if (this.formatInput.val() != "docx")
                  {
                     if (outAccessPoint!=null && outAccessPoint.dataType == "dmsDocument")
                     {
                        route += "<setHeader headerName=\"ippDmsDocumentName\">\n";
                        route += "   <simple>$simple{header.CamelTemplatingOutputName}</simple>\n";
                        route += "</setHeader>\n";
                        route += "<to uri=\"bean:documentHandler?method=toDocument\"/>";
                        route += "<setHeader headerName=\"defaultOutputAp\">\n";
                        route += "<simple>$simple{body}</simple>\n";
                        route += "</setHeader>\n";
                     }
                     else if(outAccessPoint.dataType == "primitive" && outAccessPoint.primitiveDataType=="String")
                     {
                        route += "<setHeader headerName=\"defaultOutputAp\">\n";
                        route += "<simple>$simple{bodyAs(String)}</simple>\n";
                        route += "</setHeader>\n";
                     }else{
                        route += "<setHeader headerName=\"defaultOutputAp\">\n";
                        route += "<simple>$simple{body}</simple>\n";
                        route += "</setHeader>\n";
                     }
                  }
                  m_utils.debug(route);
                  return route;
               };
               /**
                *
                */
               TemplatingIntegrationOverlay.prototype.submitChanges = function(
                        skipValidation)
               {
                  if (!skipValidation && !this.view.validate())
                  {
                     this.updateView(this.locationInput.val());
                     return;
                  }
                  else
                  {
                     this.view
                              .submitChanges(
                                       {
                                          attributes : {
                                             "carnot:engine:camel::applicationIntegrationOverlay" : "templatingIntegrationOverlay",
                                             "carnot:engine:camel::camelContextId" : "defaultCamelContext",
                                             "carnot:engine:camel::invocationPattern" : "sendReceive",
                                             "carnot:engine:camel::invocationType" : "synchronous",
                                             "carnot:engine:camel::transactedRoute" : "false",
                                             "stardust:templatingIntegrationOverlay::location" : this.locationInput
                                                      .val(),
                                             "stardust:templatingIntegrationOverlay::format" : this.formatInput
                                                      .val(),
                                             "stardust:templatingIntegrationOverlay::content" : (this.locationInput
                                                      .val() == "embedded") ? this.codeEditor
                                                      .getEditor().getSession()
                                                      .getValue()
                                                      : null,
                                             "stardust:templatingIntegrationOverlay::template" : (this.templateInput
                                                      .val() != "") ? this.templateInput
                                                      .val() : null,
                                             "stardust:templatingIntegrationOverlay::outputName" : (this.outputNameInput
                                                      .val() != "") ? this.outputNameInput
                                                      .val()
                                                      : null,
                                             "stardust:templatingIntegrationOverlay::convertToPdf" : this.convertToPdfInput
                                                      .prop("checked") ? this.convertToPdfInput
                                                      .prop("checked")
                                                      : null,
                                             "carnot:engine:camel::autoStartup" : this.autoStartupInput.prop("checked"),
                                             "carnot:engine:camel::routeEntries" : this
                                                      .getRoute()
                                          }
                                       }, skipValidation);
                  }
               };
               /**
                *
                */
               TemplatingIntegrationOverlay.prototype.submitParameterDefinitionsChanges = function(
                        parameterDefinitionsChanges)
               {
                  this.view.submitChanges({
                     contexts : {
                        application : {
                           accessPoints : parameterDefinitionsChanges
                        }
                     }
                  }, true);
               }
               /**
                *
                */
               TemplatingIntegrationOverlay.prototype.validate = function()
               {
                  var valid = true;
                  this.parameterDefinitionNameInput.removeClass("error"); //CRNT-34509
  					var parameterDefinitionNameInputWhithoutSpaces =  this.parameterDefinitionNameInput.val().replace(/ /g, "");
			
  					if ((parameterDefinitionNameInputWhithoutSpaces ==  "exchange")|| (parameterDefinitionNameInputWhithoutSpaces ==  "headers")){
  						this.view.errorMessages.push(this.parameterDefinitionNameInput.val()+" cannot be used as an access point");
  						this.parameterDefinitionNameInput.addClass("error");
  						valid = false;
  					}
  					
  				  	for (var n = 0; n < this.getApplication().contexts.application.accessPoints.length; n++)
                    {
                       var ap = this.getApplication().contexts.application.accessPoints[n];
                       if ((ap.name.replace(/ /g, "") == "headers")||(ap.name.replace(/ /g, "") == "exchange"))
                       {
                    	   if(this.view.errorMessages.indexOf(ap.name.replace(/ /g, "")+" cannot be used as an access point")<0){
                    		   this.view.errorMessages.push(ap.name.replace(/ /g, "")+" cannot be used as an access point");
                    	   }
  						this.parameterDefinitionNameInput.addClass("error");
  						valid = false;
                       }
                    }
                  if (this.locationInput.val() == "embedded")
                  {
                     if (m_utils.isEmptyString(this.codeEditor.getEditor().getSession()
                              .getValue()))
                     {
                        this.view.errorMessages.push("Template content cannot be empty");
                        valid = false;
                     }
                  }
                  if (this.convertToPdfInput.prop("checked") && m_utils.isEmptyString(this.outputNameInput.val()))
                  {
                     this.view.errorMessages.push("Output Name cannot be empty.");
                     valid = false;
                  }
                  if (this.convertToPdfInput.prop("checked") && !m_utils.isEmptyString(this.outputNameInput.val()) && this.outputNameInput.val().search(/.pdf$/) == -1)
                  {
                     this.view.errorMessages.push("Output Name should end with .pdf");
                     valid = false;
                  }

                  if (this.locationInput.val() == "classpath" || this.locationInput.val() == "repository")
                  {
                     if (m_utils.isEmptyString(this.templateInput.val()))
                     {
                        this.view.errorMessages.push("Template field cannot be empty.");
                        valid = true;
                     }
                  }
                  if(this.formatInput.val()=="docx" && this.templateInput.val().search(/.docx$/) == -1 && this.locationInput.val() != "data"){
                     this.view.errorMessages.push("Template Name should end with .docx");
                     valid = false;
                  }
                  if(this.locationInput.val() == "data" && this.sourceTypeInput.val()=="text" &&this.formatInput.val()=="docx"){
                     this.view.errorMessages.push("The provided configuration is not valid.");
                     valid = false;
                  }
                  if((this.formatInput.val()=="docx" && this.outputAccessPointInput.val()==m_constants.TO_BE_DEFINED) || (this.convertToPdfInput.prop("checked") && this.outputAccessPointInput.val()==m_constants.TO_BE_DEFINED)){
                     this.view.errorMessages.push("The Out AccessPoint should be of document Type.");
                     valid = false;
                  }

                  if(this.outputAccessPointInput.val()!=m_constants.TO_BE_DEFINED && m_utils.isEmptyString(this.outputNameInput.val())){
                     this.view.errorMessages.push("Output Name cannot be empty.");
                     valid = false;
                  }

                  return valid;
               };
               TemplatingIntegrationOverlay.prototype.isIntegrator = function(){
            	   return m_user.getCurrentRole() == m_constants.INTEGRATOR_ROLE;
            	}
            }
         });