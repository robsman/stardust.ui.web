define(
      [ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_urlUtils",
        "bpm-modeler/js/m_i18nUtils",
        "bpm-modeler/js/m_constants",
        "bpm-modeler/js/m_commandsController",
        "bpm-modeler/js/m_command", 
        "bpm-modeler/js/m_model",
        "bpm-modeler/js/m_accessPoint",
        "bpm-modeler/js/m_typeDeclaration",
        "bpm-modeler/js/m_parameterDefinitionsPanel",
        "bpm-modeler/js/m_codeEditorAce","bpm-modeler/js/m_user" ],
      function(m_utils,m_urlUtils, m_i18nUtils, m_constants, m_commandsController,
            m_command, m_model, m_accessPoint, m_typeDeclaration,
            m_parameterDefinitionsPanel, m_codeEditorAce,m_user) {
         return {
            create : function(view) {
               var overlay = new GenericEndpointOverlay();

               overlay.initialize(view);

               return overlay;
            }
         };

         /**
          * 
          */
         function GenericEndpointOverlay() {
            /**
             * 
             */
            GenericEndpointOverlay.prototype.initialize = function(view) {
               this.view = view;
               
               this.view.insertPropertiesTab("genericEndpointOverlay","parameters", "Parameters","plugins/bpm-modeler/images/icons/database_link.png");
               this.view.insertPropertiesTab("genericEndpointOverlay","producerRoute", "Producer Route","plugins/bpm-modeler/images/icons/script_code.png");
               this.view.insertPropertiesTab("genericEndpointOverlay","consumerRoute", "Consumer Route","plugins/bpm-modeler/images/icons/script_code_red.png");
               
               m_utils.jQuerySelect("label[for='camelContextInput']").text(m_i18nUtils.getProperty("modeler.element.properties.genericCamelRouteEvent.camelContext"));
               m_utils.jQuerySelect("label[for='invocationPatternInput']").text(m_i18nUtils.getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.invocationPatternInput"));
               m_utils.jQuerySelect("label[for='invocationTypeInput']").text(m_i18nUtils.getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.invocationTypeInput"));
               m_utils.jQuerySelect("label[for='transactedRouteInput']").text(m_i18nUtils.getProperty("modeler.common.camel.transactedRouteInput"));
               m_utils.jQuerySelect("label[for='autoStartupInput']").text(m_i18nUtils.getProperty("modeler.common.camel.autoStartupInput"));
               m_utils.jQuerySelect("label[for='additionalBeanSpecificationTextarea']").text(m_i18nUtils.getProperty("modeler.element.properties.genericCamelRouteEvent.additionalBeans"));
               
               m_utils.jQuerySelect("label[for='paramDef']").text(m_i18nUtils.getProperty("modeler.element.properties.genericCamelRouteEvent.paramDef"));
               m_utils.jQuerySelect("label[for='inputBodyAccessPointInput']").text(m_i18nUtils.getProperty("modeler.element.properties.genericCamelRouteEvent.inputBodyAccessPointInput"));
               m_utils.jQuerySelect("label[for='outputBodyAccessPointInput']").text(m_i18nUtils.getProperty("modeler.element.properties.genericCamelRouteEvent.outputBodyAccessPointInput"));
               
               m_utils.jQuerySelect("label[for='processContextHeadersInput']").text(m_i18nUtils.getProperty("modeler.element.properties.genericCamelRouteEvent.processContextHeadersInput"));
               m_utils.jQuerySelect("label[for='producerBpmTypeConverter']").text(m_i18nUtils.getProperty("modeler.element.properties.genericCamelRouteEvent.producerBpmTypeConverter"));
               m_utils.jQuerySelect("label[for='producerOutboundConversion']").text(m_i18nUtils.getProperty("modeler.element.properties.genericCamelRouteEvent.producerOutboundConversion"));
               m_utils.jQuerySelect("label[for='producerInboundConversion']").text(m_i18nUtils.getProperty("modeler.element.properties.genericCamelRouteEvent.producerInboundConversion"));
               
               m_utils.jQuerySelect("label[for='producerOutboundConverterDelimiterInput']").text(m_i18nUtils.getProperty("modeler.element.properties.genericCamelRouteEvent.DelimiterInput"));
               m_utils.jQuerySelect("label[for='autogenHeadersInput']").text(m_i18nUtils.getProperty("modeler.element.properties.genericCamelRouteEvent.autogenHeadersInput"));
               m_utils.jQuerySelect("label[for='producerInboundConverterDelimiterInput']").text(m_i18nUtils.getProperty("modeler.element.properties.genericCamelRouteEvent.DelimiterInput"));
               
               m_utils.jQuerySelect("label[for='consumerBpmTypeConverter']").text(m_i18nUtils.getProperty("modeler.element.properties.genericCamelRouteEvent.producerBpmTypeConverter"));
               m_utils.jQuerySelect("label[for='consumerInboundConversion']").text(m_i18nUtils.getProperty("modeler.element.properties.genericCamelRouteEvent.producerInboundConversion"));
               m_utils.jQuerySelect("label[for='consumerInboundConverterDelimiterInput']").text(m_i18nUtils.getProperty("modeler.element.properties.genericCamelRouteEvent.DelimiterInput"));
               
               
               // configuration tab
               this.camelContextInput = m_utils.jQuerySelect("#genericEndpointOverlay #camelContextInput");
               this.additionalBeanSpecificationTextarea = m_utils.jQuerySelect("#genericEndpointOverlay #additionalBeanSpecificationTextarea");
               
               this.invocationPatternInput = m_utils.jQuerySelect("#genericEndpointOverlay #invocationPatternInput");
            // this.invocationPatternInput.append("<option value=\"" + m_constants.TO_BE_DEFINED + "\">" + m_i18nUtils
            //    .getProperty("None") + "</option>");
               this.invocationPatternInput.empty();
               this.invocationPatternInput.append("<option value=\"send\" selected>" + m_i18nUtils
                  .getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.invocationPattern.send") + "</option>");
               this.invocationPatternInput.append("<option value=\"sendReceive\">" + m_i18nUtils
                  .getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.invocationPattern.sendReceive") + "</option>");
               this.invocationPatternInput.append("<option value=\"receive\">" + m_i18nUtils
                  .getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.invocationPattern.receive") + "</option>");
               this.producerOutboundConversion = m_utils.jQuerySelect("#producerRouteTab #producerOutboundConversion");
               this.producerOutboundConversion.empty();
               this.producerOutboundConversion.append("<option value=\"" + m_constants.TO_BE_DEFINED + "\">" + m_i18nUtils.getProperty("None") + "</option>");
               this.producerOutboundConversion.append("<option value=\"toXML\" selected>" + m_i18nUtils.getProperty("modeler.common.conversion.type.xml") + "</option>");
               this.producerOutboundConversion.append("<option value=\"toJSON\">" + m_i18nUtils.getProperty("modeler.common.conversion.type.json") + "</option>");
               this.producerOutboundConversion.append("<option value=\"toCSV\">" + m_i18nUtils.getProperty("modeler.common.conversion.type.csv") + "</option>");
               
               this.producerInboundConversion = m_utils.jQuerySelect("#producerRouteTab #producerInboundConversion");
               this.producerInboundConversion.empty();
               this.producerInboundConversion.append("<option value=\"" + m_constants.TO_BE_DEFINED + "\">" + m_i18nUtils.getProperty("None") + "</option>");
               this.producerInboundConversion.append("<option value=\"fromXML\" selected>" + m_i18nUtils.getProperty("modeler.common.conversion.type.xml") + "</option>");
               this.producerInboundConversion.append("<option value=\"fromJSON\">" + m_i18nUtils.getProperty("modeler.common.conversion.type.json") + "</option>");
               this.producerInboundConversion.append("<option value=\"fromCSV\">" + m_i18nUtils.getProperty("modeler.common.conversion.type.csv") + "</option>");
                  
               this.invocationTypeInput = m_utils.jQuerySelect("#genericEndpointOverlay #invocationTypeInput");
            // this.invocationTypeInput.append("<option value=\"" + m_constants.TO_BE_DEFINED + "\">" + m_i18nUtils
               // .getProperty("None") + "</option>");
               this.invocationTypeInput.append("<option value=\"synchronous\">" + m_i18nUtils
                  .getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.invocationType.synchronous") + "</option>");
               this.invocationTypeInput.append("<option value=\"asynchronous\">" + m_i18nUtils
                  .getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.invocationType.asynchronous") + "</option>");
               
               // producer route tab
               this.processContextHeadersInput = m_utils.jQuerySelect("#producerRouteTab #processContextHeadersInput");
               this.producerBpmTypeConverter = m_utils.jQuerySelect("#producerRouteTab #producerBpmTypeConverter");
               
               
               this.producerOutboundConverterOption = m_utils.jQuerySelect("#producerRouteTab #producerOutboundConverterOptionTab");
               this.producerOutboundConverterDelimiterInput = m_utils.jQuerySelect("#producerRouteTab #producerOutboundConverterDelimiterInput");
               this.autogenHeadersInput = m_utils.jQuerySelect("#producerRouteTab #autogenHeadersInput");
               this.producerInboundConverterOption = m_utils.jQuerySelect("#producerRouteTab #producerInboundConverterOptionTab");
               this.producerInboundConverterDelimiterInput = m_utils.jQuerySelect("#producerRouteTab #producerInboundConverterDelimiterInput");
               this.producerRouteTextarea = m_utils.jQuerySelect("#producerRouteTab #producerRouteTextarea");
               this.consumerBpmTypeConverter = m_utils.jQuerySelect("#consumerRouteTab #consumerBpmTypeConverter");
               
               this.consumerInboundConversion = m_utils.jQuerySelect("#consumerRouteTab #consumerInboundConversion");
               this.consumerInboundConversion.empty();
               this.consumerInboundConversion.append("<option value=\"" + m_constants.TO_BE_DEFINED + "\">" + m_i18nUtils.getProperty("None") + "</option>");
               this.consumerInboundConversion.append("<option value=\"fromXML\" selected>" + m_i18nUtils.getProperty("modeler.common.conversion.type.xml") + "</option>");
               this.consumerInboundConversion.append("<option value=\"fromJSON\">" + m_i18nUtils.getProperty("modeler.common.conversion.type.json") + "</option>");
               this.consumerInboundConversion.append("<option value=\"fromCSV\">" + m_i18nUtils.getProperty("modeler.common.conversion.type.csv") + "</option>");
               
               this.consumerInboundConverterOption = m_utils.jQuerySelect("#consumerRouteTab #consumerInboundConverterOptionTab");
               this.consumerInboundConverterDelimiterInput = m_utils.jQuerySelect("#consumerRouteTab #consumerInboundConverterDelimiterInput");
               this.consumerRouteTextarea = m_utils.jQuerySelect("#consumerRouteTab #consumerRouteTextarea");
               this.requestDataInput = m_utils.jQuerySelect("#genericEndpointOverlay #requestDataInput");
               this.responseDataInput = m_utils.jQuerySelect("#genericEndpointOverlay #responseDataInput");
               this.transactedRouteRow = m_utils.jQuerySelect("#genericEndpointOverlay #transactedRouteRow");
               this.autoStartupRow = m_utils.jQuerySelect("#genericEndpointOverlay #autoStartupRow");
               this.transactedRouteInput = m_utils.jQuerySelect("#genericEndpointOverlay #transactedRouteInput");
               this.autoStartupInput = m_utils.jQuerySelect("#genericEndpointOverlay #autoStartupInput");
               this.inputBodyAccessPointInput = m_utils.jQuerySelect("#parametersTab #inputBodyAccessPointInput");
               this.outputBodyAccessPointInput = m_utils.jQuerySelect("#parametersTab #outputBodyAccessPointInput");

               this.parameterDefinitionsPanel = m_parameterDefinitionsPanel
                     .create({
                        scope : "parametersTab",
                        submitHandler : this,
                        supportsOrdering : false,
                        supportsDataMappings : true,
                        supportsDescriptors : false,
                        supportsDataTypeSelection : true,
                        supportsDocumentTypes : true,
                        hideEnumerations:true
                     });

               this.deleteParameterDefinitionButton = m_utils.jQuerySelect("#parametersTab #deleteParameterDefinitionButton");
               this.deleteParameterDefinitionButton.attr("src", m_urlUtils
                        .getContextName()
                        + "/plugins/bpm-modeler/images/icons/delete.png");
               
               this.deleteParameterDefinitionButton.click({
                  panel : this
               }, function(event) {
                  event.data.panel.parameterDefinitionsPanel.deleteParameterDefinition();
                  var submitElements = {};
                  var attributes = event.data.panel.getApplication().attributes;
                  
                  if(event.data.panel.parameterDefinitionsPanel.currentParameterDefinition.direction=="IN"){
                     if(attributes["carnot:engine:camel::inBodyAccessPoint"]!=null && attributes["carnot:engine:camel::inBodyAccessPoint"]==event.data.panel.parameterDefinitionsPanel.currentParameterDefinition.id)
                        {
                        attributes["carnot:engine:camel::inBodyAccessPoint"] = null;
                        }
                  }
                  else{
                     if(attributes["carnot:engine:camel::outBodyAccessPoint"]!=null && attributes["carnot:engine:camel::outBodyAccessPoint"]==event.data.panel.parameterDefinitionsPanel.currentParameterDefinition.id)
                     {
                        attributes["carnot:engine:camel::outBodyAccessPoint"] = null;
                     }
                  }
                  if (Object.keys(attributes).length > 0){
                     submitElements.attributes = attributes;
                     self.view.submitChanges(submitElements, true);
                  }
               });
               
               var self = this;
               this.parameterDefinitionNameInput = jQuery("#parametersTab #parameterDefinitionNameInput");
          
               this.camelContextInput.change(function() {
                  if (!self.view.validate()) {
                     return;
                  }
                  self.view.submitModelElementAttributeChange(
                        "carnot:engine:camel::camelContextId",
                           self.camelContextInput.val());
               });
               
               this.invocationPatternInput.change(function() 
               {           
                  if (!self.view.validate()) {
                     return;
                  }
                  
                  self.manageInvocationSettings();
               });
               
               this.invocationTypeInput.change(function() 
               {
                  if (!self.view.validate()) {
                     return;
                  }
                  
                  self.manageInvocationSettings();
               });

               this.producerRouteTextarea.change(function() {
                  if (!self.view.validate() || !self.validateProducerRoute()) {
                     return;
                  }
                  
                  self.view.submitModelElementAttributeChange(
                        "carnot:engine:camel::routeEntries",
                        self.producerRouteTextarea.val());
               });
               
               
               this.transactedRouteInput.change(function() {
                  if (!self.view.validate()) {
                     return;
                  }
                  self.view.submitModelElementAttributeChange(
                        "carnot:engine:camel::transactedRoute",
                        self.transactedRouteInput.prop("checked"));
               });
               
               this.autoStartupInput.change(function() {
                  if (!self.view.validate()) {
                     return;
                  }
                  self.view.submitModelElementAttributeChange(
                        "carnot:engine:camel::autoStartup",
                        self.autoStartupInput.prop("checked"));
               });
                              
               this.processContextHeadersInput.change(function() {
                  if (!self.view.validate()) {
                     return;
                  }
                  self.view.submitModelElementAttributeChange(
                        "carnot:engine:camel::processContextHeaders",
                        self.processContextHeadersInput.prop("checked"));
               });
               
               this.producerBpmTypeConverter.change(function() {
                  if (!self.view.validate()) {
                     return;
                  }
                  var submitElements = {};
                  var attributes = self.getApplication().attributes;
                  if (self.producerBpmTypeConverter.prop("checked")) {
                     self.producerOutboundConversion.prop('disabled', false);
                     self.producerInboundConversion.prop('disabled', false);
                     attributes["carnot:engine:camel::producerBpmTypeConverter"] = self.producerBpmTypeConverter.prop("checked");
                  }
                  else {
                     self.producerRouteTextarea.removeClass("error");
                     self.producerOutboundConversion.prop('disabled', true);
                     self.producerInboundConversion.prop('disabled', true);
                     self.hideProducerOutboundConverterOption();
                     self.hideProducerInboundConverterOption();
                     attributes["carnot:engine:camel::producerBpmTypeConverter"] = false;
                     attributes["carnot:engine:camel::producerOutboundConversion"] = null;
                     attributes["carnot:engine:camel::producerInboundConversion"] = null;
                     self.producerOutboundConversion.val(m_constants.TO_BE_DEFINED);
                     self.producerInboundConversion.val(m_constants.TO_BE_DEFINED);
                  }
                  if (Object.keys(attributes).length > 0){
                     submitElements.attributes = attributes;
                     self.view.submitChanges(submitElements, true);
                  }
               });
               
               this.producerOutboundConversion.change(function() {
                  var value=self.producerOutboundConversion.val();
                  if(value === "None" || value ===  m_constants.TO_BE_DEFINED) {
                     self.hideProducerOutboundConverterOption();
                     self.view.submitModelElementAttributeChange(
                           "carnot:engine:camel::producerOutboundConversion",
                           null);
                  }
                  if (!self.view.validate() || !self.validateProducerRoute()) {
                     return;
                  }
                  if(value != "None" && value !=  m_constants.TO_BE_DEFINED){
                     var submitElements = {};
                     var attributes = self.getApplication().attributes;
                     if (value === "toCSV")
                     {
                        self.showProducerOutboundConverterOption();
                        attributes["carnot:engine:camel::producerOutboundConversion"] = value+ self.getProducerOutboundConverterOption();
                     }else{
                        self.hideProducerOutboundConverterOption();
                        attributes["carnot:engine:camel::producerOutboundConversion"] = value;
                     }
                     if (Object.keys(attributes).length > 0){
                        submitElements.attributes = attributes;
                        self.view.submitChanges(submitElements, true);
                     }
                  }
               });
               
               this.producerOutboundConverterDelimiterInput.change(function() {
                    if (!self.view.validate() || !self.validateProducerRoute() || !self.validateCsvDelimiter(self.producerOutboundConverterDelimiterInput)) {
                        return;
                    }
                    self.view.submitModelElementAttributeChange(
                             "carnot:engine:camel::producerOutboundConversion",
                             self.producerOutboundConversion.val()
                                   + self.getProducerOutboundConverterOption());
               });
               
               this.autogenHeadersInput.change(function() {
                 if (!self.view.validate() || !self.validateProducerRoute()) {
                    return;
                 }
                 self.view.submitModelElementAttributeChange(
                           "carnot:engine:camel::producerOutboundConversion",
                            self.producerOutboundConversion.val()
                               + self.getProducerOutboundConverterOption());
                });
               
               this.producerInboundConversion.change(function() {
                  var value=self.producerInboundConversion.val();
                  if(value === "None" || value ===  m_constants.TO_BE_DEFINED) {
                     self.hideProducerInboundConverterOption();
                     self.view.submitModelElementAttributeChange(
                           "carnot:engine:camel::producerInboundConversion",
                           null);
                  }
                  
                  if (!self.view.validate() || !self.validateProducerRoute()) {
                     return;
                  }
                  if( value != "None" && value !=  m_constants.TO_BE_DEFINED ) {
                     var submitElements = {};
                     var attributes = self.getApplication().attributes;
                     if (value === "fromCSV") {
                        self.showProducerInboundConverterOption();
                        attributes["carnot:engine:camel::producerInboundConversion"] = value+ self.getProducerInboundConverterOption();
                     }else{
                    
                        self.hideProducerInboundConverterOption();
                        attributes["carnot:engine:camel::producerInboundConversion"] = value;
                     }
                     if (Object.keys(attributes).length > 0){
                        submitElements.attributes = attributes;
                        self.view.submitChanges(submitElements, true);
                     }
                  }
               });
               
               this.producerInboundConverterDelimiterInput.change(function() {
                    if (!self.view.validate() || !self.validateProducerRoute() || !self.validateCsvDelimiter(self.producerInboundConverterDelimiterInput)) {
                       return;
                    }
                   self.view.submitModelElementAttributeChange(
                            "carnot:engine:camel::producerInboundConversion",
                            self.producerInboundConversion.val()
                                 + self.getProducerInboundConverterOption());
               });
               
               this.consumerRouteTextarea.change(function() {
                  if (!self.view.validate() || !self.validateConsumerRoute()) {
                     return;
                  }
                  self.view.submitModelElementAttributeChange(
                        "carnot:engine:camel::consumerRoute",
                        self.consumerRouteTextarea.val());
               });
               
               this.consumerBpmTypeConverter.change(function() {
                  if (!self.view.validate()) {
                     return;
                  }
                  var checked=self.consumerBpmTypeConverter.prop("checked");
                  var submitElements = {};
                  var attributes = self.getApplication().attributes;
                  
                  if (checked) {
                     self.consumerInboundConversion.prop('disabled', false);
                     attributes["carnot:engine:camel::consumerBpmTypeConverter"] = checked;
                  }
                  else {
                     self.consumerRouteTextarea.removeClass("error");
                     self.consumerInboundConversion.prop('disabled', true);
                     self.hideConsumerInboundConverterOption();
                     
                     attributes["carnot:engine:camel::consumerBpmTypeConverter"] = false;
                     attributes["carnot:engine:camel::consumerInboundConversion"] = null;
                     self.consumerInboundConversion.val(m_constants.TO_BE_DEFINED);
                  }
                  
                  if (Object.keys(attributes).length > 0){
                     submitElements.attributes = attributes;
                     self.view.submitChanges(submitElements, true);
                  }
               });
               
               this.consumerInboundConversion.change(function() {
                  var value=self.consumerInboundConversion.val();
                  if (!self.view.validate() || !self.validateConsumerRoute()) {
                     return;
                  }
                  var submitElements = {};
                  var attributes = self.getApplication().attributes;
                  
                  if(value != "None" && value !=  m_constants.TO_BE_DEFINED) {
                     
                     if (value === "fromCSV") {
                        self.showConsumerInboundConverterOption();
                        attributes["carnot:engine:camel::consumerInboundConversion"] = value+ self.getConsumerInboundConverterOption();
                     }
                     else {
                        self.hideConsumerInboundConverterOption();
                        attributes["carnot:engine:camel::consumerInboundConversion"] = value;
                     }
                  }
                  else {
                     self.hideConsumerInboundConverterOption();
                     attributes["carnot:engine:camel::consumerInboundConversion"] = null;
                  }
                  
                  if (Object.keys(attributes).length > 0){
                     submitElements.attributes = attributes;
                     self.view.submitChanges(submitElements, true);
                  }
               });
               
               this.consumerInboundConverterDelimiterInput.change(function() {
                  if (!self.view.validate() || !self.validateConsumerRoute() || !self.validateCsvDelimiter(self.consumerInboundConverterDelimiterInput)) {
                     return;
                  }
                 
                 self.view.submitModelElementAttributeChange(
                          "carnot:engine:camel::consumerInboundConversion",
                          self.consumerInboundConversion.val()
                               + self.getConsumerInboundConverterOption());
             });
               
               this.additionalBeanSpecificationTextarea
                     .change(function() {
                        if (!self.view.validate()) {
                           return;
                        }
                        self.view
                              .submitModelElementAttributeChange(
                                    "carnot:engine:camel::additionalSpringBeanDefinitions",
                                    self.additionalBeanSpecificationTextarea
                                          .val());
                     });
               this.inputBodyAccessPointInput
                     .change(function() {
                        if (!self.view.validate()) {
                           return;
                        }
                        var submitElements = {};
                        var attributes = self.getApplication().attributes;
                        
                        if (self.inputBodyAccessPointInput.val() == m_constants.TO_BE_DEFINED) {
                           attributes["carnot:engine:camel::inBodyAccessPoint"] = null;
                        } else {
                           attributes["carnot:engine:camel::inBodyAccessPoint"] = self.inputBodyAccessPointInput.val();
                        }
                        if (Object.keys(attributes).length > 0){
                           submitElements.attributes = attributes;
                           self.view.submitChanges(submitElements, true);
                        }
                     });
               this.outputBodyAccessPointInput
                     .change(function() {
                        if (!self.view.validate()) {
                           return;
                        }
                        var submitElements = {};
                        var attributes = self.getApplication().attributes;
                        
                        if (self.outputBodyAccessPointInput.val() == m_constants.TO_BE_DEFINED) {
                           attributes["carnot:engine:camel::outBodyAccessPoint"] = null;
                        } else {
                           attributes["carnot:engine:camel::outBodyAccessPoint"] = self.outputBodyAccessPointInput.val();
                        }
                        if (Object.keys(attributes).length > 0){
                           submitElements.attributes = attributes;
                           self.view.submitChanges(submitElements, true);
                        }
                     });
                
               this.parameterDefinitionNameInput
                     .change(function() {
                   if (!self.view.validate()) {
                           return;
                        }
                     });
               
               
               if(!this.getApplication().attributes["carnot:engine:camel::camelContextId"])
               {
                  this.camelContextInput.val('defaultCamelContext');
                   self.view.submitModelElementAttributeChange( "carnot:engine:camel::camelContextId", self.camelContextInput.val());
               }
               else
               {
                  this.camelContextInput.val(this.getApplication().attributes["carnot:engine:camel::camelContextId"]);
               }
               
               var submitElements = {};
               var attributes = this.getApplication().attributes;
               
               if(!attributes["carnot:engine:camel::invocationPattern"])
               {
                  this.invocationPatternInput.val('send');
                  this.invocationTypeInput.val('synchronous');
                  attributes["carnot:engine:camel::invocationPattern"] = self.invocationPatternInput.val();
                  attributes["carnot:engine:camel::invocationType"] = self.invocationTypeInput.val();
                  this.invocationTypeInput.prop('disabled', true);
                  this.producerRouteTextarea.prop('disabled', false);
                  this.producerBpmTypeConverter.prop('disabled', false);
                  this.consumerRouteTextarea.prop('disabled', true);
                  this.consumerBpmTypeConverter.prop('disabled', true);
                  this.processContextHeadersInput.prop('disabled', false);
               }
               else
               {
                  this.invocationPatternInput.val(attributes["carnot:engine:camel::invocationPattern"]);      
                  this.invocationTypeInput.val(attributes["carnot:engine:camel::invocationType"]);      
               }
               
               // set default to true if absent but invocation pattern is send or sendReveive
               if (attributes["carnot:engine:camel::processContextHeaders"] && 
                     attributes["carnot:engine:camel::invocationPattern"] && (
                     attributes["carnot:engine:camel::invocationPattern"].indexOf("send") > -1 ||
                     attributes["carnot:engine:camel::invocationPattern"].indexOf("sendReceive") > -1))
               {
                  this.processContextHeadersInput.prop("checked", true);
                  attributes["carnot:engine:camel::processContextHeaders"] = true;
               }
               else
               {
                  this.processContextHeadersInput.prop("checked",
                        attributes["carnot:engine:camel::processContextHeaders"]);
               }
               if (Object.keys(attributes).length > 0){
                  submitElements.attributes = attributes;
                  this.view.submitChanges(submitElements, true);
               }
            };

            /**
             * 
             */
            GenericEndpointOverlay.prototype.getModelElement = function() {
               return this.view.getModelElement();
            };

            /**
             * 
             */
            GenericEndpointOverlay.prototype.getApplication = function() {
               return this.view.application;
            };

            /**
             * 
             */
            GenericEndpointOverlay.prototype.getScopeModel = function() {
               return this.view.getModelElement().model;
            };

            /**
             * 
             */
            GenericEndpointOverlay.prototype.activate = function() {
               this.view
                     .submitChanges({
                        attributes : {
                           "carnot:engine:camel::applicationIntegrationOverlay" : "genericEndpointOverlay",
                            "synchronous:retry:responsibility": "application"
//                         "carnot:engine:camel::camelContextId" : "defaultCamelContext"
                        }
                     });
            };

            /**
             * Overlay protocol
             */
            GenericEndpointOverlay.prototype.update = function() {
               
               this.parameterDefinitionsPanel.setScopeModel(this.getScopeModel());
               this.parameterDefinitionsPanel.setParameterDefinitions(this.getApplication().contexts.application.accessPoints);
               var accessPoints=this.getApplication().contexts.application.accessPoints;
               var attributes=this.getApplication().attributes;
               
               this.autoStartupRow.hide();
               this.transactedRouteRow.hide();
               if(this.isIntegrator()){
                 this.autoStartupRow.show();
                  this.transactedRouteRow.show();
               }
               this.inputBodyAccessPointInput.empty();
               this.inputBodyAccessPointInput.append("<option value='"
                     + m_constants.TO_BE_DEFINED + "'>"
                     + m_i18nUtils.getProperty("None")
                     + "</option>");

               for ( var n = 0; n < accessPoints.length; ++n) {
                  var accessPoint = accessPoints[n];

                  if (accessPoint.direction != m_constants.IN_ACCESS_POINT) {
                     continue;
                  }

                  this.inputBodyAccessPointInput.append("<option value='"
                        + accessPoint.id + "'>" + accessPoint.name
                        + "</option>");
               }

               this.outputBodyAccessPointInput.empty();
               this.outputBodyAccessPointInput.append("<option value='"
                     + m_constants.TO_BE_DEFINED + "' selected>"
                     + m_i18nUtils.getProperty("None")
                     + "</option>");

               
               
               for ( var n = 0; n < accessPoints.length; ++n) 
               {
                  var accessPoint = accessPoints[n];

                  if (accessPoint.direction != m_constants.OUT_ACCESS_POINT) {
                     continue;
                  }

                  this.outputBodyAccessPointInput
                        .append("<option value='" + accessPoint.id
                              + "'>" + accessPoint.name + "</option>");
               }

               this.inputBodyAccessPointInput
                     .val(attributes["carnot:engine:camel::inBodyAccessPoint"]);
               this.outputBodyAccessPointInput
                     .val(attributes["carnot:engine:camel::outBodyAccessPoint"]);
               
               // configuration tab
               this.camelContextInput
                     .val(attributes["carnot:engine:camel::camelContextId"]);
               
               this.additionalBeanSpecificationTextarea
                  .val(attributes["carnot:engine:camel::additionalSpringBeanDefinitions"]);
               
               this.invocationPatternInput
                     .val(attributes["carnot:engine:camel::invocationPattern"]);
               
               this.invocationTypeInput
                     .val(attributes["carnot:engine:camel::invocationType"]);
               
               // camel producer tab
               this.processContextHeadersInput.prop("checked",attributes["carnot:engine:camel::processContextHeaders"]);
               
               if(attributes["carnot:engine:camel::autoStartup"]==null||attributes["carnot:engine:camel::autoStartup"]===undefined){
                   this.view.submitModelElementAttributeChange("carnot:engine:camel::autoStartup", true);
               }else{
                  this.autoStartupInput.prop("checked",attributes["carnot:engine:camel::autoStartup"]);
               }
               
               if(attributes["carnot:engine:camel::transactedRoute"]==null||attributes["carnot:engine:camel::transactedRoute"]===undefined){
                  this.view.submitModelElementAttributeChange("carnot:engine:camel::transactedRoute", true);
               }else{
                  this.transactedRouteInput.prop("checked",attributes["carnot:engine:camel::transactedRoute"]);
               }
               
                              
               this.producerBpmTypeConverter.prop("checked",attributes["carnot:engine:camel::producerBpmTypeConverter"]);
               
               if (attributes["carnot:engine:camel::producerOutboundConversion"] != undefined)
               {
                  var csvOutboundIndex = attributes["carnot:engine:camel::producerOutboundConversion"]
                           .indexOf("toCSV");
                  if (csvOutboundIndex != -1)
                  {
                     var option = attributes["carnot:engine:camel::producerOutboundConversion"];
                     var options = option.split("&amp;");
                     if (options.length == 2)
                     {
                        var delimiter = options[0].substring(options[0]
                                 .indexOf("=") + 1, options[0].length);
                        this.producerOutboundConverterDelimiterInput.val(delimiter);
                        var autogenHeaders = options[1].substring(options[1]
                                 .lastIndexOf("=") + 1, options[1].length);
                        autogenHeaders = (autogenHeaders === "true") ? true : false;
                        this.autogenHeadersInput.prop("checked", autogenHeaders);
                     }
                     else
                     {
                        options = option.split("=");
                        if (options.length != 0)
                        {
                           var autogenHeaders = options[1];
                           autogenHeaders = (autogenHeaders === "true") ? true : false;
                           this.autogenHeadersInput.prop("checked", autogenHeaders);
                        }
                     }

                     this.producerOutboundConversion.val("toCSV");
                  }
                  else
                  {
                     this.producerOutboundConversion
                              .val(attributes["carnot:engine:camel::producerOutboundConversion"]);
                  }

               }
               else
               {
                  this.producerOutboundConversion
                           .val(attributes["carnot:engine:camel::producerOutboundConversion"]);
               }
               
               if (attributes["carnot:engine:camel::producerInboundConversion"] != undefined)
               {
                  var csvInboundIndex = attributes["carnot:engine:camel::producerInboundConversion"]
                           .indexOf("fromCSV");

                  if (csvInboundIndex != -1)
                  {
                     var option = attributes["carnot:engine:camel::producerInboundConversion"];
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
                              .val(attributes["carnot:engine:camel::producerInboundConversion"]);
                  }
               }
               else
               {
                  this.producerInboundConversion
                           .val(attributes["carnot:engine:camel::producerInboundConversion"]);
               }
               
               if (this.producerBpmTypeConverter.prop("checked"))
               {
                  this.producerOutboundConversion.prop('disabled', false);
                  this.producerInboundConversion.prop('disabled', false);
                  if (this.producerOutboundConversion.val() === "toCSV")
                  {
                     this.showProducerOutboundConverterOption();
                  }

                  if (this.producerInboundConversion.val() === "fromCSV")
                  {
                     this.showProducerInboundConverterOption();
                  }

               }
               else
               {
                  this.producerOutboundConversion.prop('disabled', true);
                  this.producerInboundConversion.prop('disabled', true);
               }
               
               this.producerRouteTextarea
                  .val(attributes["carnot:engine:camel::routeEntries"]);
               
               if (this.invocationPatternInput.val() == 'receive'){
                  this.invocationTypeInput.prop('disabled', true);
                  this.producerRouteTextarea.prop('disabled', true);
                  this.producerBpmTypeConverter.prop('disabled', true);
                  this.consumerRouteTextarea.prop('disabled', false);
                  this.consumerBpmTypeConverter.prop('disabled', false);
                  this.processContextHeadersInput.prop('disabled', true);
               }
               // camel consumer tab
               this.consumerRouteTextarea
                  .val(attributes["carnot:engine:camel::consumerRoute"]);
               
               this.consumerBpmTypeConverter.prop("checked", attributes["carnot:engine:camel::consumerBpmTypeConverter"]);
               
               if (attributes["carnot:engine:camel::consumerInboundConversion"] != undefined)
               {
                  var csvInboundIndex = attributes["carnot:engine:camel::consumerInboundConversion"]
                           .indexOf("fromCSV");

                  if (csvInboundIndex != -1)
                  {
                     var option = attributes["carnot:engine:camel::consumerInboundConversion"];
                     var options = option.split("delimiter=");
                     if (options.length == 2)
                     {
                        this.consumerInboundConverterDelimiterInput.val(options[1]);
                     }
                     this.consumerInboundConversion.val("fromCSV");
                  }
                  else
                  {
                     this.consumerInboundConversion
                              .val(attributes["carnot:engine:camel::consumerInboundConversion"]);
                  }
               }
               else
               {
                  this.consumerInboundConversion
                           .val(attributes["carnot:engine:camel::consumerInboundConversion"]);
               }
               
               if (this.invocationPatternInput.val() == 'send'){
                  this.invocationTypeInput.prop('disabled', true);
                  this.producerRouteTextarea.prop('disabled', false);
                  this.producerBpmTypeConverter.prop('disabled', false);
                  this.consumerRouteTextarea.prop('disabled', true);
                  this.consumerBpmTypeConverter.prop('disabled', true);
                  this.processContextHeadersInput.prop('disabled', false);
               }
               
               if ((this.invocationPatternInput.val() == 'sendReceive') && (this.invocationTypeInput.val() == "synchronous")){
                  this.invocationTypeInput.prop('disabled', false);
                  this.producerRouteTextarea.prop('disabled', false);
                  this.producerBpmTypeConverter.prop('disabled', false);
                  this.consumerRouteTextarea.prop('disabled', true);
                  this.consumerBpmTypeConverter.prop('disabled', true);
                  this.processContextHeadersInput.prop('disabled', false);
               }
               if(this.consumerBpmTypeConverter.prop("checked")) {
                  this.consumerInboundConversion.prop('disabled', false);
                  if (this.consumerInboundConversion.val() === "fromCSV")
                  {
                     this.showConsumerInboundConverterOption();
                  }
               }else{
                  this.consumerInboundConversion.prop('disabled', true);
               } 
               
               // legacy
               if (attributes["carnot:engine:camel::producerMethodName"]
                     && attributes["carnot:engine:camel::producerMethodName"]
                           .indexOf("sendBodyInOut") > -1) {
                  
                  this.sendReceiveSynchronous();
                  this.view.submitModelElementAttributeChange(
                        "carnot:engine:camel::producerMethodName", null);
                  
               }
               if (attributes["carnot:engine:camel::producerMethodName"]
                     && attributes["carnot:engine:camel::producerMethodName"]
                        .indexOf("executeMessage") > -1) {
                  
                  this.sendSynchronous();
                  this.view.submitModelElementAttributeChange(
                        "carnot:engine:camel::producerMethodName", null);
               }
            };

            /**
             * 
             */
            GenericEndpointOverlay.prototype.validate = function() {
               var valid = true;
               this.camelContextInput.removeClass("error");
               this.parameterDefinitionNameInput.removeClass("error");
               this.producerOutboundConversion.removeClass("error");
               this.producerInboundConversion.removeClass("error");
               var parameterDefinitionNameInput=this.parameterDefinitionNameInput.val();
               if(!parameterDefinitionNameInput)
                  parameterDefinitionNameInput="";
               
               var parameterDefinitionNameInputWhithoutSpaces =  parameterDefinitionNameInput.replace(/ /g, "");
               if ((parameterDefinitionNameInputWhithoutSpaces ==  "exchange")|| (parameterDefinitionNameInputWhithoutSpaces ==  "headers")){
                  this.view.errorMessages
                                .push(this.parameterDefinitionNameInput.val()+" cannot be used as an access point");
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
               if (m_utils.isEmptyString(this.camelContextInput.val())) {
                  this.view.errorMessages.push("Camel Context must not be empty.");
                  this.camelContextInput.addClass("error");
                  }
               
               if (this.view.errorMessages.length != 0){
                  valid = false;
                  }
               return valid;
            };
            
            /**
             * 
             */
            GenericEndpointOverlay.prototype.validateProducerRoute = function() {

               this.producerRouteTextarea.removeClass("error");
               this.producerOutboundConversion.removeClass("error");
               this.producerInboundConversion.removeClass("error");
               
               if(!m_utils.isEmptyString(this.producerRouteTextarea.val())) {
                  var indexFromEndpoint = this.producerRouteTextarea.val().indexOf("<from");
                  if(indexFromEndpoint != -1) {
                     this.view.errorMessages
                     .push("Producer Route must not contain From Endpoint.");
                     this.producerRouteTextarea.addClass("error");
                  }
                  
                  if(this.producerBpmTypeConverter.prop("checked")){
                     if(this.producerOutboundConversion.val()==m_constants.TO_BE_DEFINED && this.producerInboundConversion.val()==m_constants.TO_BE_DEFINED){
                        this.view.errorMessages
                        .push("Please select a conversion mode or uncheck Include BPM Type Converter checkbox.");
                        this.producerOutboundConversion.addClass("error");
                        this.producerInboundConversion.addClass("error");
                     }
                  }
               }
               
               if(this.view.errorMessages.length > 0) {
                  this.view.showErrorMessages();
                  return false;
               }
               return true;
            };
            
            /**
             * 
             */
            GenericEndpointOverlay.prototype.validateConsumerRoute = function() {
               this.consumerRouteTextarea.removeClass("error");
               if(!m_utils.isEmptyString(this.consumerRouteTextarea.val())) {
                  var indexExplicitConsumerInboundConverter = this.consumerRouteTextarea.val().indexOf("bean:bpmTypeConverter?method=from");
                  // add check for new data endpoint
                  if(indexExplicitConsumerInboundConverter == -1) {
                     indexExplicitConsumerInboundConverter = this.consumerRouteTextarea.val().indexOf("ipp:data:from");
                  }
                  
                  var indexCompleteActivity = this.consumerRouteTextarea.val().indexOf("ipp:activity:complete");
                  if(indexExplicitConsumerInboundConverter != -1 && indexCompleteActivity != -1 &&
                        this.consumerInboundConversion.val() != "None" && 
                        !m_utils.isEmptyString(this.consumerInboundConversion.val())) {
                     // check if the explicit inbound conversion is right before complete activity Endpoint
                     var beforeCompleteActivity = this.consumerRouteTextarea.val()
                        .substring(indexExplicitConsumerInboundConverter, indexCompleteActivity);
                     var toUriCount = beforeCompleteActivity.match(/<to/g);
                     if(toUriCount.length == 1) {
                        this.view.errorMessages
                        .push("More than one Consumer Inbound Conversion specified.");
                        this.consumerRouteTextarea.addClass("error");
                     }
                  }
               }
               
               if(this.view.errorMessages.length > 0) {
                  this.view.showErrorMessages();
                  return false;
               }
               return true;
            };
            
            GenericEndpointOverlay.prototype.submitApplicationTypeChanges = function(
                  applicationTypeChanges, invocationPatternChanges,
                  invocationTypeChanges, producerRoute, producerBpmTypeConverter, producerOutboundConversion,
                  producerInboundConversion, consumerRoute, consumerBpmTypeConverter, consumerInboundConversion, 
                  includeProcessContextHeaders) 
            {
               var attributes={};
               attributes["synchronous:retry:responsibility"]="application";
               attributes["carnot:engine:camel::invocationPattern"]=invocationPatternChanges;
               attributes["carnot:engine:camel::invocationType"]=invocationTypeChanges;
               attributes["carnot:engine:camel::routeEntries"]=producerRoute;
               attributes["carnot:engine:camel::producerBpmTypeConverter"]=producerBpmTypeConverter;
               if(producerOutboundConversion === "None" || producerOutboundConversion ===  m_constants.TO_BE_DEFINED)
                  attributes["carnot:engine:camel::producerOutboundConversion"]=null;
               else
                  attributes["carnot:engine:camel::producerOutboundConversion"]=producerOutboundConversion;
               if(producerInboundConversion === "None" || producerInboundConversion ===  m_constants.TO_BE_DEFINED)
                  attributes["carnot:engine:camel::producerInboundConversion"]=null;
               else
                  attributes["carnot:engine:camel::producerInboundConversion"]=producerInboundConversion;
               
               attributes["carnot:engine:camel::consumerRoute"]=consumerRoute;
               attributes["carnot:engine:camel::consumerBpmTypeConverter"]=consumerBpmTypeConverter;
               attributes["carnot:engine:camel::consumerInboundConversion"]=consumerInboundConversion;
               attributes["carnot:engine:camel::processContextHeaders"]=includeProcessContextHeaders;
               
               
               this.view
                     .submitChanges({
                        type : applicationTypeChanges,
                        attributes : attributes
                     });
            };

            /**
             * 
             */
            GenericEndpointOverlay.prototype.submitParameterDefinitionsChanges = function(
                  parameterDefinitionsChanges) {
               this.view
                     .submitChanges({
                        contexts : {
                           application : {
                              accessPoints : parameterDefinitionsChanges
                           }
                        },
                        attributes : {
                           "synchronous:retry:responsibility": "application",
                           "carnot:engine:camel::applicationIntegrationOverlay" : "genericEndpointOverlay"
                        }
                     });
            };
            
            GenericEndpointOverlay.prototype.resetInvocationSettings = function() 
            {
               
               this.view.invocationTypeInput = m_constants.TO_BE_DEFINED;
               
               this.submitApplicationTypeChanges(
                  "camelSpringProducerApplication", 
                  null, 
                  null,
                  null,
                  null,
                  null,
                  null, 
                  null,
                  null,
                  null,
                  null);
               
               this.invocationTypeInput.prop('disabled', true);
               this.producerRouteTextarea.prop('disabled', true);
               this.producerBpmTypeConverter.prop('disabled', true);
               this.consumerRouteTextarea.prop('disabled', true);
               this.consumerBpmTypeConverter.prop('disabled', true);
               this.processContextHeadersInput.prop('disabled', true);
            };
            
            GenericEndpointOverlay.prototype.sendSynchronous = function() 
            {
               this.view.invocationTypeInput = "synchronous";
               
               this.submitApplicationTypeChanges(
                  "camelSpringProducerApplication", 
                  "send", 
                  "synchronous",
                  this.producerRouteTextarea.val(),
                  this.producerBpmTypeConverter.prop("checked"),
                  this.producerOutboundConversion.val(),
                  this.producerInboundConversion.val(),
                  null,
                  false,
                  null,
                  this.processContextHeadersInput.prop("checked"));
               
               this.invocationTypeInput.prop('disabled', true);
               this.producerRouteTextarea.prop('disabled', false);
               this.producerBpmTypeConverter.prop('disabled', false);
               this.consumerRouteTextarea.prop('disabled', true);
               this.consumerBpmTypeConverter.prop('disabled', true);
               this.processContextHeadersInput.prop('disabled', false);
            };
            
            GenericEndpointOverlay.prototype.sendReceiveSynchronous = function() 
            {
               this.view.invocationTypeInput = "synchronous";
               
               this.submitApplicationTypeChanges(
                  "camelSpringProducerApplication", 
                  "sendReceive", 
                  "synchronous",
                  this.producerRouteTextarea.val(),
                  this.producerBpmTypeConverter.prop("checked"),
                  this.producerOutboundConversion.val(),
                  this.producerInboundConversion.val(),
                  null,
                  false,
                  null,
                  this.processContextHeadersInput.prop("checked"));
               
               this.invocationTypeInput.prop('disabled', false);
               this.producerRouteTextarea.prop('disabled', false);
               this.producerBpmTypeConverter.prop('disabled', false);
               this.consumerRouteTextarea.prop('disabled', true);
               this.consumerBpmTypeConverter.prop('disabled', true);
               this.processContextHeadersInput.prop('disabled', false);
            };
            
            GenericEndpointOverlay.prototype.sendReceiveAsynchronous = function() 
            {
               this.view.invocationTypeInput = "asynchronous";
               
               this.submitApplicationTypeChanges(
                  "camelConsumerApplication", 
                  "sendReceive", 
                  "asynchronous",
                  this.producerRouteTextarea.val(),
                  this.producerBpmTypeConverter.prop("checked"),
                  this.producerOutboundConversion.val(),
                  this.producerInboundConversion.val(),
                  this.consumerRouteTextarea.val(),
                  this.consumerBpmTypeConverter.prop("checked"),
                  this.consumerInboundConversion.val(),
                  this.processContextHeadersInput.prop("checked"));
               
               this.invocationTypeInput.prop('disabled', false);
               this.producerRouteTextarea.prop('disabled', false);
               this.producerBpmTypeConverter.prop('disabled', false);
               this.consumerRouteTextarea.prop('disabled', false);
               this.consumerBpmTypeConverter.prop('disabled', false);
               this.processContextHeadersInput.prop('disabled', false);
            };
            
            GenericEndpointOverlay.prototype.receiveAsynchronous = function() 
            {
               this.view.invocationTypeInput = "asynchronous";
               
               this.submitApplicationTypeChanges(
                  "camelConsumerApplication", 
                  "receive", 
                  "asynchronous",
                  null,
                  false,
                  null,
                  null,
                  this.consumerRouteTextarea.val(),
                  this.consumerBpmTypeConverter.prop("checked"),
                  this.consumerInboundConversion.val(),
                  false);
               
               this.invocationTypeInput.prop('disabled', true);
               this.producerRouteTextarea.prop('disabled', true);
               this.producerBpmTypeConverter.prop('disabled', true);
               this.consumerRouteTextarea.prop('disabled', false);
               this.consumerBpmTypeConverter.prop('disabled', false);
               this.processContextHeadersInput.prop('disabled', true);
               this.update();
            };
            
            GenericEndpointOverlay.prototype.manageInvocationSettings = function() 
            {
               if (this.invocationPatternInput.val() == m_constants.TO_BE_DEFINED)
               {
                  this.resetInvocationSettings();
               }
               else if (this.invocationPatternInput.val() == 'send') 
               {
                  this.sendSynchronous();          
               }
               else if (this.invocationPatternInput.val() == 'sendReceive') 
               {
                  if (this.invocationTypeInput.val() == 'asynchronous') 
                  {
                     this.sendReceiveAsynchronous();
                  } 
                  else if (this.invocationTypeInput.val() == 'synchronous')
                  {
                     this.sendReceiveSynchronous();
                  }
                  else
                  {
                     this.sendReceiveSynchronous();
                  }
               } 
               else if (this.invocationPatternInput.val() == 'receive') 
               {
                  this.receiveAsynchronous();
               }
            };

            GenericEndpointOverlay.prototype.disableProducerTab = function() {
               this.producerRouteTextarea.prop('disabled', true);
            };

            GenericEndpointOverlay.prototype.enableProducerTab = function() {
               this.producerRouteTextarea.prop('disabled', false);
            };

            GenericEndpointOverlay.prototype.disableConsumerTab = function() {
               this.consumerRouteTextarea.prop('disabled', true);
            };

            GenericEndpointOverlay.prototype.enableConsumerTab = function() {
               this.consumerRouteTextarea.prop('disabled', false);
            };
            
            GenericEndpointOverlay.prototype.showProducerOutboundConverterOption = function()
            {
               this.producerOutboundConverterOption.show();
            };

            GenericEndpointOverlay.prototype.hideProducerOutboundConverterOption = function()
            {
               this.producerOutboundConverterOption.hide();
            };

            GenericEndpointOverlay.prototype.showProducerInboundConverterOption = function()
            {
               this.producerInboundConverterOption.show();
            };

            GenericEndpointOverlay.prototype.hideProducerInboundConverterOption = function()
            {
               this.producerInboundConverterOption.hide();
            };
            
            GenericEndpointOverlay.prototype.showConsumerInboundConverterOption = function()
            {
               this.consumerInboundConverterOption.show();
            };

            GenericEndpointOverlay.prototype.hideConsumerInboundConverterOption = function()
            {
               this.consumerInboundConverterOption.hide();
            };
            
            GenericEndpointOverlay.prototype.getProducerOutboundConverterOption = function()
            {
               var option = "";
               var separator = "?";
               if (this.producerOutboundConverterDelimiterInput
                        .val() != null && this.producerOutboundConverterDelimiterInput
                        .val().length != 0)
               {
                  option += separator;
                  option += "delimiter="
                           + this.producerOutboundConverterDelimiterInput.val();
                  option += "&amp;";
               }
               if (this.autogenHeadersInput.val() != null)
               {
                  if (option.length === 0)
                  {
                     option += separator;
                  }
                  option += "autogenHeaders="
                           + this.autogenHeadersInput.prop("checked");
               }

               return option;
            };

            GenericEndpointOverlay.prototype.getProducerInboundConverterOption = function()
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
            
            GenericEndpointOverlay.prototype.getConsumerInboundConverterOption = function()
            {
               var option = "";
               var separator = "?";
               if (this.consumerInboundConverterDelimiterInput
                        .val() != null && this.consumerInboundConverterDelimiterInput
                        .val().length != 0)
               {
                  option += separator;
                  option += "delimiter="
                           + this.consumerInboundConverterDelimiterInput.val();
               }
               return option;
            };
            
            GenericEndpointOverlay.prototype.validateCsvDelimiter = function(csvDelimiterInput)
            {
               var delimiter = csvDelimiterInput.val();
               csvDelimiterInput.removeClass("error");
               
               if(delimiter.length == 0)
               {
                  csvDelimiterInput.addClass("error");
                  this.view.errorMessages
                  .push("No value has been specified for CSV Delimiter.");
               }
               else if(delimiter.indexOf("\"") != -1 || (delimiter.indexOf("&") != -1 && delimiter.length == 1)
                         || delimiter.indexOf("'") != -1 || delimiter.indexOf("\\n") != -1
                         || delimiter.indexOf("\\r") != -1 )
               {
                  csvDelimiterInput.addClass("error");
                  this.view.errorMessages
                  .push("CSV Delimiter is not valid.");
               }
               
               if(this.view.errorMessages.length > 0) {
                  this.view.showErrorMessages();
                  return false;
               }
               return true;
            };
            
            GenericEndpointOverlay.prototype.isIntegrator = function(){
                  return m_user.getCurrentRole() == m_constants.INTEGRATOR_ROLE;
            }

         }
      });