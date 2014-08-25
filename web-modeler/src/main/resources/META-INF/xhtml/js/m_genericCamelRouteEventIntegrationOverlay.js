/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
      [ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
            "bpm-modeler/js/m_commandsController",
            "bpm-modeler/js/m_command", "bpm-modeler/js/m_model",
            "bpm-modeler/js/m_accessPoint",
            "bpm-modeler/js/m_parameterDefinitionsPanel",
            "bpm-modeler/js/m_eventIntegrationOverlay",
            "bpm-modeler/js/m_i18nUtils" ],
      function(m_utils, m_constants, m_commandsController, m_command,
            m_model, m_accessPoint, m_parameterDefinitionsPanel,
            m_eventIntegrationOverlay, m_i18nUtils) {

         return {
            create : function(page, id) {
               var overlay = new GenericCamelRouteEventIntegrationOverlay();

               overlay.initialize(page, id);

               return overlay;
            }
         };
 
         /**
          * 
          */
         function GenericCamelRouteEventIntegrationOverlay() {
            var eventIntegrationOverlay = m_eventIntegrationOverlay
                  .create();

            m_utils.inheritFields(this, eventIntegrationOverlay);
            m_utils.inheritMethods(
                  GenericCamelRouteEventIntegrationOverlay.prototype,
                  eventIntegrationOverlay);

            /**
             * 
             */
            GenericCamelRouteEventIntegrationOverlay.prototype.initialize = function(
                  page, id) {
               this.initializeEventIntegrationOverlay(page, id);
               m_utils.jQuerySelect("label[for='camelContextInput']")
                     .text(
                           m_i18nUtils
                                 .getProperty("modeler.element.properties.genericCamelRouteEvent.camelContext"));
               m_utils.jQuerySelect("label[for='routeTextarea']")
                     .text(
                           m_i18nUtils
                                 .getProperty("modeler.element.properties.genericCamelRouteEvent.routeDefinition"));
               m_utils.jQuerySelect("label[for='beanTextarea']")
                     .text(
                           m_i18nUtils
                                 .getProperty("modeler.element.properties.genericCamelRouteEvent.additionalBeans"));

               this.camelContextInput = this
                     .mapInputId("camelContextInput");
               
               this.transactedRouteInput = this.mapInputId("transactedRouteInput");
               
               this.configurationSpan = this.mapInputId("configuration");
               this.configurationSpan.text(m_i18nUtils.getProperty("modeler.element.properties.event.configuration"));
               this.parametersSpan = this.mapInputId("parameters");
               this.parametersSpan.text(m_i18nUtils.getProperty("modeler.element.properties.event.parameters"));
               
               this.converterSettingsSpan = this.mapInputId("converterSettings");
               this.converterSettingsSpan.text(m_i18nUtils.getProperty("modeler.element.properties.event.converter"));
               this.producerBpmTypeConverter =jQuery("#genericCamelRouteEvent #propertiesTabs #converterSettingsTab #producerBpmTypeConverter");
               this.producerInboundConversion = jQuery("#genericCamelRouteEvent #propertiesTabs #converterSettingsTab #producerInboundConversion");
               this.producerInboundConverterDelimiterInput = jQuery("#genericCamelRouteEvent #propertiesTabs #converterSettingsTab #producerInboundConverterDelimiterInput");
               
               this.fromXmlParameters=jQuery("#genericCamelRouteEvent #propertiesTabs #converterSettingsTab #fromXmlParameters");
               this.fromXmlParameters.hide();
               this.fromJsonParameters=jQuery("#genericCamelRouteEvent #propertiesTabs #converterSettingsTab #fromJsonParameters");
               this.fromJsonParameters.hide();
               this.fromCsvParameters=jQuery("#genericCamelRouteEvent #propertiesTabs #converterSettingsTab #fromCsvParameters");
               this.fromCsvParameters.hide();
               
               this.producerInboundConverterDelimiterInput.change({
                  overlay : this
               }, function(event) {
                  var overlay = event.data.overlay;

                  if (!overlay.validate()) {
                     return;
                  }
                  
                  var uri=  overlay.producerInboundConversion.val()+"?delimiter="+overlay.producerInboundConverterDelimiterInput.val()
                  
                  overlay.submitChanges({
                     modelElement : {
                        attributes : {
                           "carnot:engine:camel::producerInboundConversion" :  uri
                        }
                     }
                  });
             });
               
               this.producerBpmTypeConverter.change({
                  overlay : this
               }, function(event) {
                  var overlay = event.data.overlay;
                  overlay.submitChanges({
                     modelElement : {
                        attributes : {
                           "carnot:engine:camel::producerBpmTypeConverter" : overlay.producerBpmTypeConverter
                                          .prop("checked")
                        }
                     }
                  });
                  
                  if(!overlay.producerBpmTypeConverter.prop("checked")){
                     overlay.producerInboundConversion.prop('disabled',true);
                     overlay.submitChanges({
                        modelElement : {
                           attributes : {
                              "carnot:engine:camel::producerInboundConversion" : null
                           }
                        }
                     });
                     overlay.producerInboundConversion.val(null);
                     }
                  
                 
               });
               
               this.producerInboundConversion.change(
                        {
                           panel : this
                        },
                        function(event) {
                           event.data.panel.showConverterOptions(event.data.panel);
                        
                     if (!event.data.panel.validate()) {
                        return;
                     }

                     if (event.data.panel.producerInboundConversion.val() == m_constants.TO_BE_DEFINED) {
                        event.data.panel.hideConverterOptions(event.data.panel);            
                        event.data.panel.submitChanges({
                     modelElement : {
                        attributes : {
                           "carnot:engine:camel::producerInboundConversion" : null
                        }
                     }
                  });
                     } else {
                     var uri=event.data.panel.producerInboundConversion.val();
                     if(uri=="fromCSV")
                        uri+="?delimiter=,"
                  event.data.panel.submitChanges({
                     modelElement : {
                        attributes : {
                           "carnot:engine:camel::producerInboundConversion" :uri
                        }
                     }
                  });
                 
                     }
                  });
               
               
               this.parameterDefinitionsPanel = this.mapInputId("parameterDefinitionsTable");
               this.outputBodyAccessPointInput = jQuery("#genericCamelRouteEvent #parametersTab #outputBodyAccessPointInput");
               this.parameterDefinitionsPanel = m_parameterDefinitionsPanel
                     .create({
                        scope : "genericCamelRouteEvent",
                        submitHandler : this,
                        supportsOrdering : true,
                        supportsDataMappings : true,
                        supportsDescriptors : false,
                        supportsDataTypeSelection : true,
                        supportsDocumentTypes : true,
                        hideEnumerations:true,
                        supportsDataPathes:false
                     });

               if (this.propertiesTabs != null) {
                  this.propertiesTabs.tabs();
               }
               this.routeTextarea = this
                     .mapInputId("routeTextarea");
               this.additionalBeanTextarea = this
                     .mapInputId("beanTextarea");

               this.routeTextarea.change({
                  overlay : this
               }, function(event) {
                  var overlay = event.data.overlay;
                  overlay.submitRouteChanges();
               });
               
               this.additionalBeanTextarea.change({
                  overlay : this
               }, function(event) {
                  var overlay = event.data.overlay;
                  overlay.submitRouteChanges();
               });
               
               this.camelContextInput.change({
                  overlay : this
               }, function(event) {
                  var overlay = event.data.overlay;
                  overlay.submitRouteChanges();
               });

               this.transactedRouteInput.change({
                  overlay : this
               }, function(event) {
                  var overlay = event.data.overlay;
                  overlay.submitChanges({
                     modelElement : {
                        attributes : {
                           "carnot:engine:camel::transactedRoute" : overlay.transactedRouteInput
                                          .prop("checked")
                        }
                     }
                  });
               });
               
               
               this.parameterDefinitionNameInput = jQuery("#parametersTab #parameterDefinitionNameInput");
            
               this.outputBodyAccessPointInput.change(
                           {
                              panel : this
                           },
                           function(event) {
                        if (!event.data.panel.validate()) {
                           return;
                        }

                        if (event.data.panel.outputBodyAccessPointInput.val() == m_constants.TO_BE_DEFINED) {
                                       event.data.panel.submitChanges({
                        modelElement : {
                           attributes : {
                              "carnot:engine:camel::outBodyAccessPoint" : null
                           }
                        }
                     });
                        } else {
                     event.data.panel.submitChanges({
                        modelElement : {
                           attributes : {
                              "carnot:engine:camel::outBodyAccessPoint" : event.data.panel.outputBodyAccessPointInput
                                             .val()
                           }
                        }
                     });
                        }
                     });
               
               this.registerForRouteChanges(this.camelContextInput);
               this.registerForRouteChanges(this.routeTextarea);
               this.registerForRouteChanges(this.additionalBeanTextarea);
               this.camelContextInput.val("defaultCamelContext");
            };

           
            
            GenericCamelRouteEventIntegrationOverlay.prototype.showConverterOptions = function(panel) {
               if(panel.producerInboundConversion.val()=="fromXML"){
                  panel.fromXmlParameters.show();
               }else if (panel.producerInboundConversion.val()=="fromJSON"){
                  panel.fromJsonParameters.show();
               }else if (panel.producerInboundConversion.val()=="fromCSV"){
                  panel.fromCsvParameters.show();
               }
               
            }
            GenericCamelRouteEventIntegrationOverlay.prototype.hideConverterOptions = function(panel) {
               panel.fromXmlParameters.hide();
               panel.fromJsonParameters.hide();
               panel.fromCsvParameters.hide();
               
            }
            
            
            GenericCamelRouteEventIntegrationOverlay.prototype.getProducerInboundConverterOption = function()
            {
               var option = "";
               var separator = "?";
               if (this.producerInboundConverterDelimiterInput
                        .val() != null && this.producerInboundConverterDelimiterInput
                        .val().length != 0)
               {
                  option += separator;
                  option += "delimiter="
                           + this.producerInboundConverterDelimiterInput.val();
               }
               return option;
            };

            /**
             *
             */
            GenericCamelRouteEventIntegrationOverlay.prototype.populateParameterDefinitionFields = function() {
               this.parameterDefinitionNameInput
                     .val(this.currentParameterDefinition.name);
               this.parameterDefinitionDirectionSelect
                     .val(this.currentParameterDefinition.direction);
               this.parameterDefinitionDataSelect
                     .val(this.currentParameterDefinition.dataFullId);
               this.parameterDefinitionPathInput
                     .val(this.currentParameterDefinition.path);
               this.dataTypeSelector
                     .setDataType(this.currentParameterDefinition);
            };
            /**
             * 
             */
            GenericCamelRouteEventIntegrationOverlay.prototype.getCamelContext = function() {
               return this.camelContextInput.val();
            };

            /**
             * 
             */
            GenericCamelRouteEventIntegrationOverlay.prototype.activate = function() {
               this.routeTextarea.val(m_i18nUtils
               .getProperty("modeler.general.toBeDefined"));
               var parameterMappings = [];
               this.submitOverlayChanges(parameterMappings);
            };

            function HTMLEncode(str){
                 var i = str.length,
                     aRet = [];

                 while (i--) {
                   var iC = str[i];
                  if(str[i-4]=='&' &&str[i-3]=='a' &&str[i-2]=='m'&&str[i-1]=='p'&&str[i]==';')
                  {
                     i=i-4;
                     aRet[i]='&amp;';
                  }else{
                     if(str[i]=='&'){
                     aRet[i]='&amp;';
                     }else{
                      aRet[i] = str[i]; 
                     }
                  }                 
               }
                 return aRet.join('');    
               }
               
            
            /**
             * 
             */
            GenericCamelRouteEventIntegrationOverlay.prototype.update = function() {
               this.hideConverterOptions(this);
               this.producerInboundConversion.prop('disabled',true);
               if(this.page.getEvent().attributes["carnot:engine:camel::transactedRoute"]==null || this.page.getEvent().attributes["carnot:engine:camel::transactedRoute"]===undefined){
                  this.submitChanges({
                     modelElement : {
                        attributes : {
                           "carnot:engine:camel::transactedRoute" : true
                        }
                     }
                  });
               }
               
               if (this.page.getEvent().attributes["carnot:engine:camel::producerBpmTypeConverter"]!=null && this.page.getEvent().attributes["carnot:engine:camel::producerBpmTypeConverter"]!==undefined &&this.page.getEvent().attributes["carnot:engine:camel::producerBpmTypeConverter"]==true) {
                  this.producerInboundConversion.prop('disabled',false);
                  this.producerBpmTypeConverter.prop("checked",this.page.getEvent().attributes["carnot:engine:camel::producerBpmTypeConverter"]);
               }
               
               this.showConverterOptions(this);
               
               if (this.page.getEvent().attributes["carnot:engine:camel::producerInboundConversion"] != undefined)
               {
                  var csvInboundIndex = this.page.getEvent().attributes["carnot:engine:camel::producerInboundConversion"]
                           .indexOf("fromCSV");

                  if (csvInboundIndex != -1)
                  {
                     var option = this.page.getEvent().attributes["carnot:engine:camel::producerInboundConversion"];
                     var options = option.split("delimiter=");
                     if (options.length == 2)
                     {
                        this.producerInboundConverterDelimiterInput.val(options[1]);
                     }
                     this.producerInboundConversion.val("fromCSV");
                  }
                  else
                  {
                     this.producerInboundConversion
                              .val(this.page.getEvent().attributes["carnot:engine:camel::producerInboundConversion"]);
                  }
               }
               else
               {
                  this.producerInboundConversion
                           .val(this.page.getEvent().attributes["carnot:engine:camel::producerInboundConversion"]);
               }

               
               this.transactedRouteInput.prop("checked",this.page.getEvent().attributes["carnot:engine:camel::transactedRoute"]);
               var route = this.page.getEvent().attributes["carnot:engine:camel::camelRouteExt"];
               this.camelContextInput
                     .val(this.page.getEvent().attributes["carnot:engine:camel::camelContextId"]);
            
               this.outputBodyAccessPointInput.empty();
               this.outputBodyAccessPointInput.append("<option value='"
                     + m_constants.TO_BE_DEFINED + "' selected>"
                     + m_i18nUtils.getProperty("None") // TODO I18N
                     + "</option>");

               
               
               for ( var n = 0; n < this.page.getEvent().parameterMappings.length; ++n) 
               {
                  var accessPoint = this.page.getEvent().parameterMappings[n];
                  accessPoint.direction = m_constants.OUT_ACCESS_POINT
                  this.outputBodyAccessPointInput
                        .append("<option value='" + accessPoint.id
                              + "'>" + accessPoint.name + "</option>");
               }
            
               this.routeTextarea.val(route);
               
               this.additionalBeanTextarea
                     .val(this.page.getEvent().attributes["carnot:engine:camel::additionalSpringBeanDefinitions"]);
               
               this.outputBodyAccessPointInput
                     .val(this.page.getEvent().attributes["carnot:engine:camel::outBodyAccessPoint"]);
               
               this.parameterDefinitionsPanel.setScopeModel(this.page
                     .getModel());
               
               this.parameterDefinitionsPanel
                     .setParameterDefinitions(this.page.getEvent().parameterMappings);
            }; 

            /**
             * 
             */
            GenericCamelRouteEventIntegrationOverlay.prototype.getRouteDefinitions = function() {
            
                    return HTMLEncode(this.routeTextarea.val());
            };

            /**
             * 
             */
            GenericCamelRouteEventIntegrationOverlay.prototype.getAdditionalBeanSpecifications = function() {
               return this.additionalBeanTextarea.val();
            };

            /**
             * 
             */
            GenericCamelRouteEventIntegrationOverlay.prototype.validate = function() {
               
               this.camelContextInput.removeClass("error");
               this.routeTextarea.removeClass("error");
               this.page.propertiesPanel.errorMessages=[];
               this.page.propertiesPanel.warningMessages=[];
               this.page.propertiesPanel.clearWarningMessages();
               
               if(this.producerInboundConversion != null 
                        && this.producerInboundConversion.val() != m_constants.TO_BE_DEFINED 
                        && this.producerInboundConversion.val().indexOf("fromCSV") != -1)
               {
                  this.validateCsvDelimiter(this.producerInboundConverterDelimiterInput);
               }
               
               if (m_utils.isEmptyString(this.camelContextInput.val()) ||
                     this.camelContextInput.val() == m_i18nUtils
                     .getProperty("modeler.general.toBeDefined")) {
                  this.getPropertiesPanel().errorMessages
                        .push(m_i18nUtils
                              .getProperty("modeler.general.fieldMustNotBeEmpty"));
                  this.camelContextInput.addClass("error");
               // this.camelContextInput.focus();

               }

               if (m_utils.isEmptyString(this.routeTextarea.val())) {
                  this.getPropertiesPanel().errorMessages
                        .push(m_i18nUtils
                              .getProperty("modeler.general.fieldMustNotBeEmpty"));
                  this.routeTextarea.addClass("error");
                  //this.routeTextarea.focus();

               }
               
               if(this.page.overlay.parameterDefinitionsPanel.parameterDefinitions.length == 0) {
                  this.page.propertiesPanel.warningMessages
                  .push("No parameters defined for Start Event.");
                  this.page.propertiesPanel.showWarningMessages();
               }
               
               if (this.page.propertiesPanel.errorMessages.length != 0){
                  this.page.propertiesPanel.showErrorMessages();
                  return false;
               }

               return true;
            };
         }
      });