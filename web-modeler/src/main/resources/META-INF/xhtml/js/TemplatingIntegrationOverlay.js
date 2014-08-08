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
         [ "bpm-modeler/js/m_utils","bpm-modeler/js/m_urlUtils", "bpm-modeler/js/m_i18nUtils",
                  "bpm-modeler/js/m_constants", "bpm-modeler/js/m_commandsController",
                  "bpm-modeler/js/m_command", "bpm-modeler/js/m_model",
                  "bpm-modeler/js/m_accessPoint", "bpm-modeler/js/m_typeDeclaration",
                  "bpm-modeler/js/m_parameterDefinitionsPanel",
                  "bpm-modeler/js/m_codeEditorAce" ],
         function(m_utils, m_urlUtils, m_i18nUtils, m_constants, m_commandsController, m_command,
                  m_model, m_accessPoint, m_typeDeclaration, m_parameterDefinitionsPanel,
                  m_codeEditorAce)
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
                  this.textAreaConfigurationDiv = m_utils
                           .jQuerySelect("#templatingIntegrationOverlay #configurationTab #textAreaConfiguration");
                  this.classpathFileConfigurationDiv = m_utils
                           .jQuerySelect("#templatingIntegrationOverlay #configurationTab #classpathFileConfiguration");
                  this.textAreaConfigurationDiv.hide();
                  this.classpathFileConfigurationDiv.hide();
                  this.templateLocationInput = m_utils
                           .jQuerySelect("#templatingIntegrationOverlay #configurationTab #templateLocationInput");
                  this.templateLocationInput
                           .append("<option value=\"textBox\">"
                                    + m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.templating.templateLocationInput.textBox.label")
                                    + "</option>");
                  this.templateLocationInput
                           .append("<option value=\"classpath\">"
                                    + m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.templating.templateLocationInput.classPath.label")
                                    + "</option>");
                  this.classPathLocationInput=m_utils.jQuerySelect("#templatingIntegrationOverlay #configurationTab #classPathLocationInput");
                  this.templateLocationInput.change(
                           {
                              panel : this
                           },
                           function(event)
                           {
                  
                     if (event.data.panel.templateLocationInput.val() == "textBox")
                     {
                        event.data.panel.textAreaConfigurationDiv.show();
                        event.data.panel.classpathFileConfigurationDiv.hide();

                     }
                     else if (event.data.panel.templateLocationInput.val() == "classpath")
                     {
                        event.data.panel.textAreaConfigurationDiv.hide();
                        event.data.panel.classpathFileConfigurationDiv.show();
                     }
                     else
                     {
                        event.data.panel.textAreaConfigurationDiv.hide();
                        event.data.panel.classpathFileConfigurationDiv.hide();
                     }
                     event.data.panel.view.submitModelElementAttributeChange(
                              "stardust:templatingIntegrationOverlay::templateLocation",
                              event.data.panel.templateLocationInput.val());
                  });
                  this.editorAnchor = m_utils.jQuerySelect("#codeEditorDiv").get(0);
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
                     supportsDocumentTypes : false,
                     supportsOtherData : false,
                     hideEnumerations : true
                  });
                  
                  this.parameterDefinitionDirectionSelect = m_utils.jQuerySelect("#parametersTab #parameterDefinitionDirectionSelect");
                  this.parameterDefinitionDirectionSelect.empty();
                  this.parameterDefinitionDirectionSelect.append("<option value=\"IN\">" + m_i18nUtils.getProperty("modeler.element.properties.commonProperties.in") + "</option>");
                  this.parameterDefinitionDirectionSelect.append("<option value=\"OUT\">" + m_i18nUtils.getProperty("modeler.element.properties.commonProperties.out") + "</option>");
                  this.parameterDefinitionDirectionSelect.append("<option value=\"INOUT\">" + m_i18nUtils.getProperty("modeler.element.properties.commonProperties.inout") + "</option>");
                  
                  this.outputBodyAccessPointInput = m_utils.jQuerySelect("#parametersTab #outputBodyAccessPointInput");
                  this.deleteParameterDefinitionButton = m_utils.jQuerySelect("#parametersTab #deleteParameterDefinitionButton");
                  this.deleteParameterDefinitionButton.attr("src", m_urlUtils
                           .getContextName()
                           + "/plugins/bpm-modeler/images/icons/delete.png");
                  
                  this.deleteParameterDefinitionButton.click({
                     panel : this
                  }, function(event) {
                     event.data.panel.parameterDefinitionsPanel.deleteParameterDefinition();
                        if(event.data.panel.getApplication().attributes["carnot:engine:camel::outBodyAccessPoint"]!=null && event.data.panel.getApplication().attributes["carnot:engine:camel::outBodyAccessPoint"]==event.data.panel.parameterDefinitionsPanel.currentParameterDefinition.id)
                        {
                        event.data.panel.view
                        .submitModelElementAttributeChange(
                              "carnot:engine:camel::outBodyAccessPoint",
                              null);
                        }
                  });
                  this.outputBodyAccessPointInput
                  .change(function() {
                     if (!self.view.validate()) {
                        return;
                     }

                     if (self.outputBodyAccessPointInput.val() == m_constants.TO_BE_DEFINED) {
                        self.view
                              .submitModelElementAttributeChange(
                                    "carnot:engine:camel::outBodyAccessPoint",
                                    null);
                     } else {
                        self.view
                              .submitModelElementAttributeChange(
                                    "carnot:engine:camel::outBodyAccessPoint",
                                    self.outputBodyAccessPointInput
                                          .val());
                     }
                  });
                  this.classPathLocationInput
                  .change(
                        {
                           panel : this
                        },
                        function(event) {
                           if (!event.data.panel.validate()) {
                              return;
                           }
                           event.data.panel.submitChanges();
                        });
                  
                  this.update();
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
                          // "stardust:predefined" : true
                           "carnot:engine:data:bidirectional": true,
                           "carnot:engine:path:separator": "/"
                        }
                     });
                     
                     
                     this.submitChanges({
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
                                    "carnot:engine:camel::outBodyAccessPoint":"returnValue"
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
                  this.outputBodyAccessPointInput.append("<option value='"
                        + m_constants.TO_BE_DEFINED + "' selected>"
                        + m_i18nUtils.getProperty("None") // TODO I18N
                        + "</option>");

                  
                  
                  for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) 
                  {
                     var accessPoint = this.getApplication().contexts.application.accessPoints[n];

                     if (accessPoint.direction != m_constants.OUT_ACCESS_POINT) {
                        continue;
                     }

                     this.outputBodyAccessPointInput
                           .append("<option value='" + accessPoint.id
                                 + "'>" + accessPoint.name + "</option>");
                  }
                  
                  this.outputBodyAccessPointInput
                  .val(this.getApplication().attributes["carnot:engine:camel::outBodyAccessPoint"]);
                  
                  if (this.getApplication().attributes["stardust:templatingIntegrationOverlay::templateLocation"] == null)
                  {
                     // show TextBox view as default
                     this.templateLocationInput.val("textBox");
                     this.textAreaConfigurationDiv.show();
                  }
                  else
                  {
                     var templateLocationValue = this.getApplication().attributes["stardust:templatingIntegrationOverlay::templateLocation"];
                     this.templateLocationInput.val(templateLocationValue);
                     if (templateLocationValue == "textBox")
                     {
                        this.textAreaConfigurationDiv.show();
                        this.classpathFileConfigurationDiv.hide();
                        var templateContent=this.getApplication().attributes["stardust:templatingIntegrationOverlay::templateContent"];
                        if(templateContent!=null){
                           this.codeEditor.getEditor().getSession().setValue(templateContent);
                        }
                        
                     }
                     else if (templateLocationValue == "classpath")
                     {
                        this.textAreaConfigurationDiv.hide();
                        this.classpathFileConfigurationDiv.show();
                        this.classPathLocationInput.val(this.getApplication().attributes["stardust:templatingIntegrationOverlay::fileLocation"]);
                     }
                     else
                     {
                        // handle in document
                     }
                  }

               };
               /**
                * returns camel route definition
                */
               TemplatingIntegrationOverlay.prototype.getRoute = function()
               {
                  var route = "";
                  var templateLocation=this.templateLocationInput.val();
                  if(templateLocation=="textBox"){
                     route += "<process ref=\"customVelocityContextAppender\"/>";
                     route += "<setHeader headerName=\"CamelVelocityTemplate\">\n";
                     route += "   <constant>\n";
                     route+="<![CDATA[";
                     route+="#parse(\"commons.vm\")"; 
                     route+="#getInputs()";
                     var templateContent = this.codeEditor.getEditor().getSession().getValue();
                     route+=templateContent;
                     route += "#setOutputs()";
                     route+="]]>\n";
                     route += "   </constant>\n";
                     route += "</setHeader>\n";
                     route += "<to uri=\"velocity:dummy\" />";
                     
                  }else if(templateLocation=="classpath"){
                     route += "<process ref=\"customVelocityContextAppender\"/>";
                     route += "<setHeader headerName=\"CamelVelocityResourceUri\">\n";
                     route += "   <constant>"+this.classPathLocationInput.val()+"</constant>\n";
                     route += "</setHeader>\n";
                     route += "<to uri=\"velocity:dummy?encoding=UTF-8\" />";
                  }else{
                     
                  }
                  m_utils.debug(route);
                  
                  return route;
               };
               /**
                * 
                */
               TemplatingIntegrationOverlay.prototype.submitChanges = function(changes)
               {
                  if(changes){
                     this.view
                     .submitChanges(changes);
                  }else{
                  this.view
                           .submitChanges({
                              attributes : {
                                 "carnot:engine:camel::applicationIntegrationOverlay" : "templatingIntegrationOverlay",
                                 "carnot:engine:camel::camelContextId" : "defaultCamelContext",
                                 "carnot:engine:camel::invocationPattern" : "sendReceive",
                                 "carnot:engine:camel::invocationType" : "synchronous",
                                 "carnot:engine:camel::transactedRoute":"false",
                                 "stardust:templatingIntegrationOverlay::templateLocation" : this.templateLocationInput.val(),
                                 "stardust:templatingIntegrationOverlay::templateContent" : (this.templateLocationInput.val()=="textBox")?this.codeEditor.getEditor().getSession().getValue():null,
                                 "stardust:templatingIntegrationOverlay::fileLocation":(this.templateLocationInput.val()=="classpath")?this.classPathLocationInput.val():null,
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
                  return true;
               };
            }
         });