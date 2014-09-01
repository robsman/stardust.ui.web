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
                                    m_i18nUtils.getProperty("modeler.model.applicationOverlay.templating.parameters.title"),
                                    "plugins/bpm-modeler/images/icons/database_link.png");
                  
                  this.locationInput = m_utils.jQuerySelect("#templatingIntegrationOverlay #configurationTab #locationInput");
                  this.formatInput= m_utils.jQuerySelect("#templatingIntegrationOverlay #configurationTab #formatInput");
                  this.textAreaConfigurationDiv = m_utils.jQuerySelect("#templatingIntegrationOverlay #configurationTab #textAreaConfiguration");
                  this.templateRow= m_utils.jQuerySelect("#templatingIntegrationOverlay #configurationTab #templateRow");
                  this.templateInput=m_utils.jQuerySelect("#templatingIntegrationOverlay #configurationTab #templateInput");
                  this.outputNameRow= m_utils.jQuerySelect("#templatingIntegrationOverlay #configurationTab #outputNameRow");
                  this.outputNameInput=m_utils.jQuerySelect("#templatingIntegrationOverlay #configurationTab #outputNameInput");
                  this.convertToPdfRow= m_utils.jQuerySelect("#templatingIntegrationOverlay #configurationTab #convertToPdfRow");
                  this.convertToPdfInput= m_utils.jQuerySelect("#templatingIntegrationOverlay #configurationTab #convertToPdfInput");
                  this.outputBodyAccessPointInput = m_utils.jQuerySelect("#parametersTab #outputBodyAccessPointInput");
                  this.deleteParameterDefinitionButton = m_utils.jQuerySelect("#parametersTab #deleteParameterDefinitionButton");
                  this.editorAnchor = m_utils.jQuerySelect("#codeEditorDiv").get(0);
                  this.parameterDefinitionDirectionSelect = m_utils.jQuerySelect("#parametersTab #parameterDefinitionDirectionSelect");
                  
                  this.locationInput.append("<option value=\"embedded\" selected>"+ m_i18nUtils.getProperty("modeler.model.applicationOverlay.templating.locationInput.embedded.label")+ "</option>");
                  this.locationInput.append("<option value=\"classpath\">"+ m_i18nUtils.getProperty("modeler.model.applicationOverlay.templating.locationInput.classpath.label")+ "</option>");
                  this.locationInput.append("<option value=\"repository\">"+ m_i18nUtils.getProperty("modeler.model.applicationOverlay.templating.locationInput.repository.label")+ "</option>");
                  this.locationInput.append("<option value=\"data\">"+ m_i18nUtils.getProperty("modeler.model.applicationOverlay.templating.locationInput.data.label")+ "</option>");
                  this.locationInput
                  .change(
                           {
                              panel : this
                           },
                           function(event)
                           {
                              event.data.panel.submitChanges();
                           });
                  
                 
                  this.formatInput
                  .change(
                           {
                              panel : this
                           },
                           function(event)
                           { 
                              event.data.panel.submitChanges();
                   });
                  
                  this.templateInput.change({
                     panel : this
                  }, function(event)
                  {
                     event.data.panel.submitChanges();
                  });

                  this.outputNameInput.change({
                     panel : this
                  }, function(event)
                  {
                     event.data.panel.submitChanges();
                  });
                  
                  this.convertToPdfInput.change({
                           panel : this
                        },function(event) {
                           if(event.data.panel.convertToPdfInput.prop("checked")){
                              event.data.panel.outputNameRow.show();
                           }else{
                              event.data.panel.outputNameRow.hide();
                           }
                           event.data.panel.submitChanges();
                           
                        });
                 
                  this.editorAnchor.id = "codeEditorDiv" + Math.floor((Math.random() * 100000) + 1);
                  this.codeEditor = m_codeEditorAce.getSQLCodeEditor(this.editorAnchor.id);
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
                  this.parameterDefinitionDirectionSelect
                           .append("<option value=\"OUT\">"
                                    + m_i18nUtils
                                             .getProperty("modeler.element.properties.commonProperties.out")
                                    + "</option>");
                  this.parameterDefinitionDirectionSelect
                           .append("<option value=\"INOUT\">"
                                    + m_i18nUtils
                                             .getProperty("modeler.element.properties.commonProperties.inout")
                                    + "</option>");
                  
                  this.deleteParameterDefinitionButton.attr("src", m_urlUtils.getContextName()+ "/plugins/bpm-modeler/images/icons/delete.png");
                  this.deleteParameterDefinitionButton
                           .click(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       event.data.panel.parameterDefinitionsPanel.deleteParameterDefinition();
                                       if(event.data.panel.getApplication().attributes["carnot:engine:camel::outBodyAccessPoint"] != null){
                                          if (event.data.panel.getApplication().attributes["carnot:engine:camel::outBodyAccessPoint"] == event.data.panel.parameterDefinitionsPanel.currentParameterDefinition.id)
                                          {
                                             event.data.panel.view
                                                      .submitModelElementAttributeChange(
                                                               "carnot:engine:camel::outBodyAccessPoint",
                                                               null);
                                          }else{
                                             event.data.panel.outputBodyAccessPointInput.val(event.data.panel.getApplication().attributes["carnot:engine:camel::outBodyAccessPoint"]);
                                          }
                                       }
                                    });
                  this.outputBodyAccessPointInput
                           .change(function()
                           {
                              if (!self.view.validate())
                              {
                                 self.outputBodyAccessPointInput.val(self.getApplication().attributes["carnot:engine:camel::outBodyAccessPoint"])
                                 return;
                              }
                              self.submitChanges();
                           });
                  this.update();
               };
               /**
                * 
                */
               TemplatingIntegrationOverlay.prototype.updateView = function(location)
               {
                  this.textAreaConfigurationDiv.hide();
                  this.outputNameRow.hide();
                  this.formatInput.empty();
                  this.formatInput.append("<option value=\"text\" selected>"+ m_i18nUtils.getProperty("modeler.model.applicationOverlay.templating.formatInput.text.label")+ "</option>");
                  this.formatInput.append("<option value=\"html\">"+ m_i18nUtils.getProperty("modeler.model.applicationOverlay.templating.formatInput.html.label")+ "</option>");
                  this.formatInput.append("<option value=\"xml\">"+ m_i18nUtils.getProperty("modeler.model.applicationOverlay.templating.formatInput.xml.label")+ "</option>");
                  if(location!="embedded"){
                     this.formatInput.append("<option value=\"docx\">"+ m_i18nUtils.getProperty("modeler.model.applicationOverlay.templating.formatInput.docx.label")+ "</option>");
                  }
                  if(location=="embedded"){
                     this.textAreaConfigurationDiv.show();
                     this.templateRow.hide();
                  }
                  
                  if(location =="classpath" ||location =="repository"){
                     this.templateRow.show();
                     
                  }
                  
                  if(location =="data"){
                     this.templateRow.hide();
                     this.outputNameRow.hide();
                  }
                  if(this.convertToPdfInput.prop("checked") || this.formatInput.val()=="docx")
                     this.outputNameRow.show();
               };
               
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
                     var defaultAccessPoints = [];
                     defaultAccessPoints.push({
                        id : "returnValue",
                        name : "returnValue",
                        dataType : "primitive",
                        primitiveDataType : "String",
                        direction : "OUT",
                        attributes : {
                           "carnot:engine:data:bidirectional" : true,
                           "carnot:engine:path:separator" : "/"
                        }
                     });
                     this
                              .submitChanges({
                                 contexts : {
                                    application : {
                                       accessPoints : defaultAccessPoints
                                    }
                                 },
                                 attributes : {
                                    "carnot:engine:camel::camelContextId" : "defaultCamelContext",
                                    "carnot:engine:camel::invocationPattern" : "sendReceive",
                                    "carnot:engine:camel::invocationType" : "synchronous",
                                    "carnot:engine:camel::applicationIntegrationOverlay" : "templatingIntegrationOverlay",
                                    "carnot:engine:camel::outBodyAccessPoint" : "returnValue"
                                 }
                              });
                  }
               };
               /**
                * 
                */
               TemplatingIntegrationOverlay.prototype.update = function()
               {
                  this.parameterDefinitionsPanel.setScopeModel(this.getScopeModel());
                  this.parameterDefinitionsPanel.setParameterDefinitions(this.getApplication().contexts.application.accessPoints);
                  this.outputBodyAccessPointInput.empty();
                  this.outputBodyAccessPointInput.append("<option value='"+ m_constants.TO_BE_DEFINED + "' selected>"+ m_i18nUtils.getProperty("None")+ "</option>");
                  
                  this.updateView(this.locationInput.val());
                  if(this.getApplication().attributes["stardust:templatingIntegrationOverlay::location"])
                     this.locationInput.val(this.getApplication().attributes["stardust:templatingIntegrationOverlay::location"]);
                  
                  if(this.getApplication().attributes["stardust:templatingIntegrationOverlay::format"])
                     this.formatInput.val(this.getApplication().attributes["stardust:templatingIntegrationOverlay::format"]);
                  
                  if(this.getApplication().attributes["stardust:templatingIntegrationOverlay::location"]=="embedded"){
                     this.codeEditor.getEditor().getSession().setValue(this.getApplication().attributes["stardust:templatingIntegrationOverlay::content"]);
                  }
                  
                  if(this.getApplication().attributes["stardust:templatingIntegrationOverlay::template"])
                     this.templateInput.val(this.getApplication().attributes["stardust:templatingIntegrationOverlay::template"]);
                  
                  if(this.getApplication().attributes["stardust:templatingIntegrationOverlay::outputName"])
                     this.outputNameInput.val(this.getApplication().attributes["stardust:templatingIntegrationOverlay::outputName"]);
                  
                  this.convertToPdfInput.prop("checked",this.getApplication().attributes["stardust:templatingIntegrationOverlay::convertToPdf"]);
                  
                  var outAccessPoints=[];
                  for (var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n)
                  {
                     var accessPoint = this.getApplication().contexts.application.accessPoints[n];

                     if (accessPoint.direction != m_constants.OUT_ACCESS_POINT && accessPoint.direction != m_constants.IN_OUT_ACCESS_POINT)
                     {
                        continue;
                     }
                     outAccessPoints.push(accessPoint);
                     this.outputBodyAccessPointInput.append("<option value='"+ accessPoint.id + "'>" + accessPoint.name + "</option>");
                  }

                  this.outputBodyAccessPointInput.val(this.getApplication().attributes["carnot:engine:camel::outBodyAccessPoint"]);
                  
             /*     if(this.convertToPdfInput.prop("checked") || this.formatInput.val()=="docx")
                     this.outputNameRow.show();
               */   
                  if(outAccessPoints.length>0){
                     var outAccessPoint=outAccessPoints[0];
                     if(outAccessPoint.dataType=="dmsDocument")
                        this.outputNameRow.show();
                  }
               };
               /**
                * returns camel route definition
                */
              TemplatingIntegrationOverlay.prototype.getRoute = function()
               {
                  var route = "";
                  var accessPoints = this.getApplication().contexts.application.accessPoints;
                  var includeDocumentPostProcessor=false;
                  var outAccessPoint;
                  if (accessPoints.length > 0)
                  {
                     for (var n = 0; n < accessPoints.length; ++n)
                     {
                        var accessPoint = accessPoints[n];
                        if (accessPoint.direction == m_constants.OUT_ACCESS_POINT
                                 || accessPoint.direction == m_constants.IN_OUT_ACCESS_POINT)
                        {
                           outAccessPoint=accessPoint;
                           includeDocumentPostProcessor=true;
                        }
                     }
                  }
                  var templateLocation = this.locationInput.val();
                  if(this.formatInput.val()!="docx"){
                     route += "<process ref=\"customVelocityContextAppender\"/>";
                     
                     if (templateLocation == "embedded")
                     {
                        route += "<setHeader headerName=\"CamelVelocityTemplate\">\n";
                        route += "   <constant>\n";
                        route += "<![CDATA[";
                        route += "#parse(\"commons.vm\")\n";
                        route += "#getInputs()\n";
                        route += this.codeEditor.getEditor().getSession().getValue();
                        route += "#setOutputs()\n";
                        route += "]]>\n";
                        route += "   </constant>\n";
                        route += "</setHeader>\n";
                     }
                     else if (templateLocation == "classpath")
                     {
                        route += "<setHeader headerName=\"CamelVelocityResourceUri\">\n";
                        route += "   <constant>" + this.templateInput.val() + "</constant>\n";
                        route += "</setHeader>\n";
                     }
                     else if (templateLocation == "repository" ||templateLocation == "data"  )
                     {
                        if(templateLocation == "repository"){
                           route += "<setHeader headerName=\"ippDmsTargetPath\">\n";
                           route += "   <constant>" + this.templateInput.val() + "</constant>\n";
                           route += "</setHeader>\n";
                        }
                        route += "<to uri=\"bean:documentHandler?method=retrieveContent\"/>";
                        route += "<setHeader headerName=\"CamelVelocityTemplate\">\n";
                        route += "   <simple>$simple{header.ippDmsDocumentContent}</simple>\n";
                        route += "</setHeader>\n";
                     }
                     
                     var uri="templating:"+templateLocation+"?format="+this.formatInput.val();
                     if( this.templateInput.val()!=null)
                        uri+="&amp;template="+this.templateInput.val()
                     if( this.outputNameInput.val()!=null)
                        uri+="&amp;outputName="+this.outputNameInput.val();
                     if(this.convertToPdfInput.prop("checked")){
                        uri+="&amp;convertToPdf="+this.convertToPdfInput.prop("checked");
                     }
                     route += "<to uri=\""+uri+"\" />";
                     
                     if(this.convertToPdfInput.prop("checked")){
                        route += "<to uri=\"bean:pdfConverterProcessor?method=process\"/>";
                     }
                     if(outAccessPoint.dataType=="dmsDocument")
                     {
                        route += "<setHeader headerName=\"ippDmsDocumentName\">\n";
                        route += "   <simple>$simple{header.CamelTemplatingOutputName}</simple>\n";
                        route += "</setHeader>\n";
                        
                        route += "<to uri=\"bean:documentHandler?method=toDocument\"/>"; 
                     }
                  }else{
                     route += "<process ref=\"customVelocityContextAppender\"/>";
                     if(templateLocation == "repository"||templateLocation == "data")
                      {
                        route += "<process ref=\"customVelocityContextAppender\"/>";
                        route += "<setHeader headerName=\"ippDmsTargetPath\">\n";
                        route += "   <constant>" + this.templateInput.val() + "</constant>\n";
                        route += "</setHeader>\n";
                        route += "<to uri=\"bean:documentHandler?method=retrieveContent\"/>";
                        route += "<setHeader headerName=\"CamelTemplatingTemplateContent\">\n";
                        route += "   <simple>$simple{header.ippDmsDocumentContent}</simple>\n";
                        route += "</setHeader>\n";
                      }
                     var uri="templating:"+templateLocation+"?";
                     uri+="format="+this.formatInput.val();
                     
                     if( this.templateInput.val()!=null)
                        uri+="&amp;template="+this.templateInput.val()
                     if( this.outputNameInput.val()!=null)
                       uri+="&amp;outputName="+this.outputNameInput.val();   
                     if(this.convertToPdfInput.prop("checked")){
                           uri+="&amp;convertToPdf="+this.convertToPdfInput.prop("checked");
                     }
                     route += "<to uri=\""+uri+"\" />";
                     route += "<setHeader headerName=\"ippDmsDocumentName\">\n";
                     route += "   <simple>$simple{header.CamelTemplatingOutputName}</simple>\n";
                     route += "</setHeader>\n";
                     
                     route += "<to uri=\"bean:documentHandler?method=toDocument\"/>"; 
                  }
                  m_utils.debug(route);

                  return route;
               };
               /**
                * 
                */
               TemplatingIntegrationOverlay.prototype.submitChanges = function(changes)
               {
                  if (!this.view.validate()) {
                     this.updateView(this.locationInput.val());
                     return;
                  }
                  if (changes)
                  {
                     this.view.submitChanges(changes);
                  }
                  else
                  {
                     this.view
                              .submitChanges({
                                 attributes : {
                                    "carnot:engine:camel::applicationIntegrationOverlay" : "templatingIntegrationOverlay",
                                    "carnot:engine:camel::camelContextId" : "defaultCamelContext",
                                    "carnot:engine:camel::invocationPattern" : "sendReceive",
                                    "carnot:engine:camel::invocationType" : "synchronous",
                                    "carnot:engine:camel::transactedRoute" : "false",
                                    "carnot:engine:camel::outBodyAccessPoint":(this.outputBodyAccessPointInput.val()== m_constants.TO_BE_DEFINED)?null:this.outputBodyAccessPointInput.val(),
                                    "stardust:templatingIntegrationOverlay::location":this.locationInput.val(),
                                    "stardust:templatingIntegrationOverlay::format":this.formatInput.val(),
                                    "stardust:templatingIntegrationOverlay::content" : (this.locationInput.val() == "embedded") ? this.codeEditor.getEditor().getSession().getValue() : null,
                                    "stardust:templatingIntegrationOverlay::template":(this.templateInput.val()!= "")?this.templateInput.val():null,
                                    "stardust:templatingIntegrationOverlay::outputName":(this.outputNameInput.val()!="")?this.outputNameInput.val():null,
                                    "stardust:templatingIntegrationOverlay::convertToPdf":this.convertToPdfInput.prop("checked") ? this.convertToPdfInput.prop("checked"): null,
                                    "carnot:engine:camel::routeEntries" : this.getRoute()
                                 }
                              });
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
                  });
               }
               /**
                * 
                */
               TemplatingIntegrationOverlay.prototype.validate = function()
               {
                  var valid = true;
                  var outAccessPointList = [];
                  var accessPoints = this.getApplication().contexts.application.accessPoints;
                  var outBodyAccessPointId = this.outputBodyAccessPointInput.val();
                  var outAccessPoint;

                  if (accessPoints.length > 0)
                  {
                     for (var n = 0; n < accessPoints.length; ++n)
                     {
                        var accessPoint = accessPoints[n];
                        if (accessPoint.direction == m_constants.OUT_ACCESS_POINT
                                 || accessPoint.direction == m_constants.IN_OUT_ACCESS_POINT)
                        {
                           outAccessPointList.push(accessPoint);
                        }
                        if (outBodyAccessPointId == accessPoint.id)
                        {
                           outAccessPoint = accessPoint;
                        }
                     }
                  
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