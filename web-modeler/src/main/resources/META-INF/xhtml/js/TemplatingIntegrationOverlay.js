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
                  "bpm-modeler/js/m_codeEditorAce" ],
         function(m_utils, m_urlUtils, m_i18nUtils, m_constants, m_commandsController,
                  m_command, m_model, m_accessPoint, m_typeDeclaration,
                  m_parameterDefinitionsPanel, m_codeEditorAce)
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
                  this.deleteParameterDefinitionButton = m_utils
                           .jQuerySelect("#parametersTab #deleteParameterDefinitionButton");
                  this.outputAccessPointRow = m_utils
                           .jQuerySelect("#templatingIntegrationOverlay #configurationTab #outputAccessPointRow");
                  this.outputAccessPointInput = m_utils
                           .jQuerySelect("#templatingIntegrationOverlay #configurationTab #outputAccessPointInput");
                  this.outputAccessPointInput = m_utils
                           .jQuerySelect("#templatingIntegrationOverlay #configurationTab #outputAccessPointInput");

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
                     event.data.panel.submitChanges(true);
                  });

                  this.formatInput.change({
                     panel : this
                  }, function(event)
                  {
                     event.data.panel.submitChanges(true);
                  });

                  this.templateInput.change({
                     panel : this
                  }, function(event)
                  {
                     event.data.panel.submitChanges(true);
                  });

                  this.outputNameInput.change({
                     panel : this
                  }, function(event)
                  {
                     event.data.panel.submitChanges(true);
                  });

                  this.convertToPdfInput.change({
                     panel : this
                  }, function(event)
                  {
                     event.data.panel.submitChanges(true);
                  });

                  this.editorAnchor.id = "codeEditorDiv"
                           + Math.floor((Math.random() * 100000) + 1);
                  this.codeEditor = m_codeEditorAce
                           .getSQLCodeEditor(this.editorAnchor.id);
                  this.codeEditor.loadLanguageTools();
                  this.codeEditor.getEditor().on('blur', function(e)
                  {
                     self.submitChanges();
                  });

                  var self = this;
                  this.parameterDefinitionsPanel = m_parameterDefinitionsPanel.create({
                     scope : "parametersTab",
                     submitHandler : this,
                     supportsOrdering : false,
                     supportsDataMappings : false,
                     supportsDescriptors : false,
                     supportsDataTypeSelection : true,
                     supportsDocumentTypes : true,
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
                  this.deleteParameterDefinitionButton
                           .click(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       event.data.panel.parameterDefinitionsPanel.deleteParameterDefinition();
                                    });

                  this.outputAccessPointInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var accessPoints = self.getApplication().contexts.application.accessPoints;
                                       if (self.outputAccessPointInput.val() == m_constants.TO_BE_DEFINED)
                                       {
                                          // set output AP type to default(String)
                                          var defaultAccessPoints= event.data.panel.createOrReplaceOutAccessPoint();
                                          event.data.panel
                                                   .submitParameterDefinitionsChanges(defaultAccessPoints);
                                       }
                                       else
                                       {
                                          var filteredAccessPoints = event.data.panel
                                                   .filterAccessPoint(accessPoints,
                                                            "output");
                                          var selectedDataItem = event.data.panel
                                                   .findTypeDeclaration(
                                                            event.data.panel
                                                                     .getScopeModel().typeDeclarations,
                                                            event.data.panel.outputAccessPointInput
                                                                     .val());
                                          var outputAccessPoint = event.data.panel
                                                   .findAccessPoint(accessPoints,
                                                            "output");
                                          outputAccessPoint = {
                                             id : "output",
                                             name : "Output",
                                             dataType : "dmsDocument",
                                             direction : "OUT",
                                             structuredDataTypeFullId : selectedDataItem
                                                      .getFullId(),
                                             attributes : {
                                                "stardust:predefined" : true,
                                                "carnot:engine:dataType" : m_model
                                                         .stripElementId(selectedDataItem
                                                                  .getFullId())
                                             }
                                          };
                                       }
                                       filteredAccessPoints.push(outputAccessPoint);
                                       event.data.panel
                                                .submitParameterDefinitionsChanges(filteredAccessPoints);
                                    });
                  this.update();
               };

               TemplatingIntegrationOverlay.prototype.showHideOutAccessPointRow = function(convertToPdfInput, formatInput){
                     if (convertToPdfInput
                              || formatInput == "docx")
                     {
                        this.outputAccessPointRow.show();
                     }else{
                        this.outputAccessPointRow.hide();
                     }
               }

               /**
                * exclude accessPointId from the accessPoints List
                */
               TemplatingIntegrationOverlay.prototype.findAccessPoint = function(
                        accessPoints, accessPointId)
               {
                  var accessPopint = null;
                  for (var n = 0; n < accessPoints.length; n++)
                  {
                     var ap = accessPoints[n];
                     if (ap.id == accessPointId)
                     {
                        accessPopint = ap;
                        break;
                     }
                  }
                  return accessPopint;
               }

               /**
                * exclude accessPointId from the accessPoints List
                */
               TemplatingIntegrationOverlay.prototype.filterAccessPoint = function(
                        accessPoints, accessPointId)
               {
                  var filteredAccessPoints = [];
                  for (var n = 0; n < accessPoints.length; n++)
                  {
                     var ap = accessPoints[n];
                     if (ap.id != accessPointId)
                     {
                        filteredAccessPoints.push(ap);
                     }
                  }
                  return filteredAccessPoints;
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
                     var defaultAccessPoints= this.createOrReplaceOutAccessPoint();
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

               TemplatingIntegrationOverlay.prototype.createOrReplaceOutAccessPoint = function(){
                  var accessPoints = this.getApplication().contexts.application.accessPoints;
                  var outAccessPoint=this.findAccessPoint(accessPoints,"output");
                  var accessPointList=[];

                  if(outAccessPoint){
                     accessPointList=this.filterAccessPoint(accessPoints,"output");
                  }
                  accessPointList.push({
                     id : "output",
                     name : "Output",
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
                  this.parameterDefinitionsPanel.setParameterDefinitions(this.getApplication().contexts.application.accessPoints);

                  //intiailize dropdown list with typeDeclarations
                  this.outputAccessPointInput.empty();
                  this.outputAccessPointInput.append("<option value='" + m_constants.TO_BE_DEFINED + "'>"+ m_i18nUtils.getProperty("None") + "</option>");
                  var typeDeclarations = this.getScopeModel().typeDeclarations;
                  for ( var i in typeDeclarations)
                  {
                     var typeDeclaration = typeDeclarations[i];
                     this.outputAccessPointInput.append("<option value='"+ typeDeclaration.getFullId() + "'>" + typeDeclaration.name+ "</option>");
                  }
                  //////////////////////////////////////////////////////////////

                  this.populateFormatInputField();
                  if (this.getApplication().attributes["stardust:templatingIntegrationOverlay::location"]){
                     this.locationInput.val(this.getApplication().attributes["stardust:templatingIntegrationOverlay::location"]);

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

                  var accessPoints = this.getApplication().contexts.application.accessPoints;

               /*   if(!this.getApplication().attributes["stardust:templatingIntegrationOverlay::convertToPdf"]){
                     this.outputAccessPointRow.hide();
                     var outputAccessPoint = this.findAccessPoint(accessPoints,"output");
                     if(outputAccessPoint && (outputAccessPoint.dataType!="primitive" && outputAccessPoint.primitiveDataType!="String")){
                     var filteredAccessPoints = this.createOrReplaceOutAccessPoint();
                     this.submitParameterDefinitionsChanges(filteredAccessPoints);
                     }
                  }*/



                  if (accessPoints.length > 0)
                  {
                     var outputAccessPoint = this.findAccessPoint(accessPoints, "output");
                     if (outputAccessPoint)
                     {
                        if (outputAccessPoint.dataType == "dmsDocument")
                           this.outputAccessPointInput
                                    .val(outputAccessPoint.structuredDataTypeFullId);
                     }
                  }
                  //update the view
                  this.updateView(this.locationInput.val());
               };

               /**
               *
               */
              TemplatingIntegrationOverlay.prototype.updateView = function(location)
              {
                 this.textAreaConfigurationDiv.hide();
                 this.outputAccessPointRow.show();
                 //this.outputAccessPointRow.hide();
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
                 }
                 if (this.convertToPdfInput.prop("checked")
                          || this.formatInput.val() == "docx"){
                    this.outputNameRow.show();
                  //  this.outputAccessPointRow.show();
                 }
                 var accessPoints = this.getApplication().contexts.application.accessPoints;
                 if (accessPoints.length > 0)
                 {
                    var outputAccessPoint = this.findAccessPoint(accessPoints, "output");
                    if (outputAccessPoint)
                    {
                       if (outputAccessPoint.dataType == "dmsDocument")
                          this.outputNameRow.show();
                    }
                 }
              };
               /**
                * returns camel route definition
                */
               TemplatingIntegrationOverlay.prototype.getRoute = function()
               {
                  var route = "";
                  var accessPoints = this.getApplication().contexts.application.accessPoints;
                  var includeDocumentPostProcessor = false;
                  var outAccessPoint;
                  if (accessPoints.length > 0)
                  {
                     for (var n = 0; n < accessPoints.length; ++n)
                     {
                        var accessPoint = accessPoints[n];
                        if (accessPoint.direction == m_constants.OUT_ACCESS_POINT
                                 || accessPoint.direction == m_constants.IN_OUT_ACCESS_POINT)
                        {
                           outAccessPoint = accessPoint;
                           includeDocumentPostProcessor = true;
                        }
                     }
                  }
                  var templateLocation = this.locationInput.val();
                  if (this.formatInput.val() != "docx")
                  {
                     route += "<process ref=\"customVelocityContextAppender\"/>";

                     if (templateLocation == "embedded")
                     {
                        route += "<setHeader headerName=\"CamelVelocityTemplate\">\n";
                        route += "   <constant>\n";
                        route += "<![CDATA[";
                        route += "#parse(\"commons.vm\")\n";
                        route += "#getInputs()\n";
                        route += this.codeEditor.getEditor().getSession().getValue();
                        route += "\n";
                        route += "#setOutputs()\n";
                        route += "]]>\n";
                        route += "   </constant>\n";
                        route += "</setHeader>\n";
                     }
                     else if (templateLocation == "classpath")
                     {
                        route += "<setHeader headerName=\"CamelVelocityResourceUri\">\n";
                        route += "   <constant>" + this.templateInput.val()
                                 + "</constant>\n";
                        route += "</setHeader>\n";
                     }
                     else if (templateLocation == "repository"
                              || templateLocation == "data")
                     {
                        if (templateLocation == "repository")
                        {
                           route += "<setHeader headerName=\"ippDmsTargetPath\">\n";
                           route += "   <constant>" + this.templateInput.val()
                                    + "</constant>\n";
                           route += "</setHeader>\n";
                        }
                        route += "<to uri=\"bean:documentHandler?method=retrieveContent\"/>";
                        route += "<setHeader headerName=\"CamelVelocityTemplate\">\n";
                        route += "   <simple>$simple{header.ippDmsDocumentContent}</simple>\n";
                        route += "</setHeader>\n";
                     }
                     var uri = "templating:" + templateLocation + "?format="
                              + this.formatInput.val();
                     if (this.templateInput.val() != null && this.templateInput.val() != "")
                        uri += "&amp;template=" + this.templateInput.val()
                     if (this.outputNameInput.val() != null && this.outputNameInput.val() != "")
                        uri += "&amp;outputName=" + this.outputNameInput.val();
                     if (this.convertToPdfInput.prop("checked"))
                     {
                        uri += "&amp;convertToPdf="
                                 + this.convertToPdfInput.prop("checked");
                     }
                     route += "<to uri=\"" + uri + "\" />";

                     if (this.convertToPdfInput.prop("checked"))
                     {
                        route += "<to uri=\"bean:pdfConverterProcessor?method=process\"/>";
                     }
                     if (outAccessPoint.dataType == "dmsDocument")
                     {
                        route += "<setHeader headerName=\"ippDmsDocumentName\">\n";
                        route += "   <simple>$simple{header.CamelTemplatingOutputName}</simple>\n";
                        route += "</setHeader>\n";

                        route += "<to uri=\"bean:documentHandler?method=toDocument\"/>";
                        route += "<setHeader headerName=\"output\">\n";
                        route += "<simple>$simple{body}</simple>\n";
                        route += "</setHeader>\n";
                     }else{
                        route += "<setHeader headerName=\"output\">\n";
                        route += "<simple>$simple{body}</simple>\n";
                        route += "</setHeader>\n";
                     }

                  }
                  else
                  {
                     route += "<process ref=\"customVelocityContextAppender\"/>";
                     if (templateLocation == "repository" || templateLocation == "data")
                     {
                        route += "<process ref=\"customVelocityContextAppender\"/>";
                        route += "<setHeader headerName=\"ippDmsTargetPath\">\n";
                        route += "   <constant>" + this.templateInput.val()
                                 + "</constant>\n";
                        route += "</setHeader>\n";
                        route += "<to uri=\"bean:documentHandler?method=retrieveContent\"/>";
                        route += "<setHeader headerName=\"CamelTemplatingTemplateContent\">\n";
                        route += "   <simple>$simple{header.ippDmsDocumentContent}</simple>\n";
                        route += "</setHeader>\n";
                     }
                     var uri = "templating:" + templateLocation + "?";
                     uri += "format=" + this.formatInput.val();

                     if (this.templateInput.val() != null && this.templateInput.val() != "")
                        uri += "&amp;template=" + this.templateInput.val()
                     if (this.outputNameInput.val() != null && this.outputNameInput.val() != "")
                        uri += "&amp;outputName=" + this.outputNameInput.val();
                     if (this.convertToPdfInput.prop("checked"))
                     {
                        uri += "&amp;convertToPdf="
                                 + this.convertToPdfInput.prop("checked");
                     }
                     route += "<to uri=\"" + uri + "\" />";
                     route += "<setHeader headerName=\"ippDmsDocumentName\">\n";
                     route += "   <simple>$simple{header.CamelTemplatingOutputName}</simple>\n";
                     route += "</setHeader>\n";

                     route += "<to uri=\"bean:documentHandler?method=toDocument\"/>";
                     route += "<setHeader headerName=\"output\">\n";
                     route += "<simple>$simple{body}</simple>\n";
                     route += "</setHeader>\n";
                  }
                  m_utils.debug(route);

                  return route;
               };
               /**
                *
                */
               TemplatingIntegrationOverlay.prototype.submitChanges = function(skipValidation)
               {
                  if (!skipValidation&& !this.view.validate())
                  {
                     this.updateView(this.locationInput.val());
                     return;
                  }
                 /* if (changes)
                  {
                     this.view.submitChanges(changes);
                  }
                  else
                  {*/
              else{
                     this.view
                              .submitChanges({
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
                                             .getEditor().getSession().getValue() : null,
                                    "stardust:templatingIntegrationOverlay::template" : (this.templateInput
                                             .val() != "") ? this.templateInput.val()
                                             : null,
                                    "stardust:templatingIntegrationOverlay::outputName" : (this.outputNameInput
                                             .val() != "") ? this.outputNameInput.val()
                                             : null,
                                    "stardust:templatingIntegrationOverlay::convertToPdf" : this.convertToPdfInput
                                             .prop("checked") ? this.convertToPdfInput
                                             .prop("checked") : null,
                                    "carnot:engine:camel::routeEntries" : this.getRoute()
                                 }
                              },skipValidation);
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
                  },true);
               }
               /**
                *
                */
               TemplatingIntegrationOverlay.prototype.validate = function()
               {
                  var valid = true;
                          var outAccessPointList = [];
                          var accessPoints = this.getApplication().contexts.application.accessPoints;

                          var outAccessPoint;

                          if (accessPoints.length > 0)
                          {
                             outAccessPoint=this.findAccessPoint(accessPoints,"output");


                          if(!outAccessPoint){
                             this.view.errorMessages.push("Please select one Out Access Point.");
                             valid = false;
                          }else{
                             if(this.convertToPdfInput.prop("checked")){
                                if(outAccessPoint.dataType!="dmsDocument"){
                                   this.view.errorMessages.push("Out Access Point "+outAccessPoint.name+" should be of document Type.");
                                   valid = false;
                                }
                                if(m_utils.isEmptyString(this.outputNameInput.val())){
                                   this.view.errorMessages.push("Output Name cannot be empty.");
                                   valid = false;
                                }
                             }
                             if(this.formatInput.val()!="docx"){
                               if(this.locationInput.val()=="embedded"){
                                     if(m_utils.isEmptyString(this.codeEditor.getEditor().getSession().getValue())){
                                        this.view.errorMessages.push("Template content cannot be empty");
                                        valid = false;
                                     }
                               }else if(this.locationInput.val()=="classpath" || this.locationInput.val()=="repository"){
                                     if(m_utils.isEmptyString(this.templateInput.val())){
                                     this.view.errorMessages.push("Template field cannot be empty.");
                                     valid = true;
                                  }
                                }
                             }else {
                                if(outAccessPoint.dataType!="dmsDocument"){
                                   this.view.errorMessages.push("The Out AccessPoint should be of document Type.");
                                   valid = false;
                                }
                             }


                          }

                          if(outAccessPoint.dataType!="dmsDocument" && !(outAccessPoint.dataType=="primitive" && outAccessPoint.primitiveDataType=="String") ){
                             this.view.errorMessages.push("Out AccessPoint "+outAccessPoint.name +" is not valid.");
                             valid = false;
                          }

                          }
                  return valid;

               };
            }
         });