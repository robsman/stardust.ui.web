define(
      [ "bpm-modeler/js/m_utils", 
        "bpm-modeler/js/m_i18nUtils",
        "bpm-modeler/js/m_constants",
        "bpm-modeler/js/m_commandsController",
        "bpm-modeler/js/m_command", 
        "bpm-modeler/js/m_model",
        "bpm-modeler/js/m_accessPoint",
        "bpm-modeler/js/m_typeDeclaration",
        "bpm-modeler/js/m_parameterDefinitionsPanel",
        "bpm-modeler/js/m_codeEditorAce" ],
      function(m_utils, m_i18nUtils, m_constants, m_commandsController,
            m_command, m_model, m_accessPoint, m_typeDeclaration,
            m_parameterDefinitionsPanel, m_codeEditorAce) {
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
               
               this.view.insertPropertiesTab("genericEndpointOverlay",
                     "parameters", "Parameters",
                     "plugins/bpm-modeler/images/icons/database_link.png");
               
               this.view.insertPropertiesTab("genericEndpointOverlay",
                     "producerRoute", "Producer Route",
                     "plugins/bpm-modeler/images/icons/script_code.png");

               this.view.insertPropertiesTab("genericEndpointOverlay",
                     "consumerRoute", "Consumer Route",
                     "plugins/bpm-modeler/images/icons/script_code_red.png");
               
                     

               // configuration tab
               this.camelContextInput = m_utils.jQuerySelect("#genericEndpointOverlay #camelContextInput");
               this.additionalBeanSpecificationTextarea = m_utils.jQuerySelect("#genericEndpointOverlay #additionalBeanSpecificationTextarea");
               
               this.invocationPatternInput = m_utils.jQuerySelect("#genericEndpointOverlay #invocationPatternInput");
            // this.invocationPatternInput.append("<option value=\"" + m_constants.TO_BE_DEFINED + "\">" + m_i18nUtils
            //    .getProperty("None") + "</option>");
               this.invocationPatternInput.append("<option value=\"send\" selected>" + m_i18nUtils
                  .getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.invocationPattern.send") + "</option>");
               this.invocationPatternInput.append("<option value=\"sendReceive\">" + m_i18nUtils
                  .getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.invocationPattern.sendReceive") + "</option>");
               this.invocationPatternInput.append("<option value=\"receive\">" + m_i18nUtils
                  .getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.invocationPattern.receive") + "</option>");
                           
                  
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
               this.producerOutboundConversion = m_utils.jQuerySelect("#producerRouteTab #producerOutboundConversion");
               this.producerInboundConversion = m_utils.jQuerySelect("#producerRouteTab #producerInboundConversion");
               this.producerRouteTextarea = m_utils.jQuerySelect("#producerRouteTab #producerRouteTextarea");
               this.consumerBpmTypeConverter = m_utils.jQuerySelect("#consumerRouteTab #consumerBpmTypeConverter");
               this.consumerInboundConversion = m_utils.jQuerySelect("#consumerRouteTab #consumerInboundConversion");
               this.consumerRouteTextarea = m_utils.jQuerySelect("#consumerRouteTab #consumerRouteTextarea");
               this.requestDataInput = m_utils.jQuerySelect("#genericEndpointOverlay #requestDataInput");
               this.responseDataInput = m_utils.jQuerySelect("#genericEndpointOverlay #responseDataInput");
               this.transactedRouteInput = m_utils.jQuerySelect("#genericEndpointOverlay #transactedRouteInput");
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
                        supportsDocumentTypes : false,
                        hideEnumerations:true
                     });

               var self = this;
                           
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
                  if (!self.view.validate()) {
                     return;
                  }
                  
                  if (!self.validateProducerRoute()) {
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
                  
                  if (self.producerBpmTypeConverter.prop("checked")) {
                     self.producerOutboundConversion.prop('disabled', false);
                     self.producerInboundConversion.prop('disabled', false);
                     self.view.submitModelElementAttributeChange(
                           "carnot:engine:camel::producerBpmTypeConverter",
                           self.producerBpmTypeConverter.prop("checked"));
                  }
                  else {
                     self.producerRouteTextarea.removeClass("error");
                     self.producerOutboundConversion.prop('disabled', true);
                     self.producerInboundConversion.prop('disabled', true);
                     
                     self.view.submitModelElementAttributeChange(
                           "carnot:engine:camel::producerBpmTypeConverter",
                           false);
                     self.view.submitModelElementAttributeChange(
                           "carnot:engine:camel::producerOutboundConversion",
                           null);
                     self.view.submitModelElementAttributeChange(
                           "carnot:engine:camel::producerInboundConversion",
                           null);
                  }
               });
               
               this.producerOutboundConversion.change(function() {
                  if (!self.view.validate()) {
                     return;
                  }
                  
                  if (!self.validateProducerRoute()) {
                     return;
                  }
                  
                  if(self.producerOutboundConversion.val() != "None") {
                     self.view.submitModelElementAttributeChange(
                           "carnot:engine:camel::producerOutboundConversion",
                           self.producerOutboundConversion.val());
                  }
                  else {
                     self.view.submitModelElementAttributeChange(
                           "carnot:engine:camel::producerOutboundConversion",
                           null);
                  }
               });
               
               this.producerInboundConversion.change(function() {
                  if (!self.view.validate()) {
                     return;
                  }
                  
                  if (!self.validateProducerRoute()) {
                     return;
                  }
                  
                  if(self.producerInboundConversion.val() != "None") {
                     self.view.submitModelElementAttributeChange(
                           "carnot:engine:camel::producerInboundConversion",
                           self.producerInboundConversion.val());
                  }
                  else {
                     self.view.submitModelElementAttributeChange(
                           "carnot:engine:camel::producerInboundConversion",
                           null);
                  }
               });
               
               this.consumerRouteTextarea.change(function() {
                  if (!self.view.validate()) {
                     return;
                  }
                  
                  if (!self.validateConsumerRoute()) {
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
                  
                  if (self.consumerBpmTypeConverter.prop("checked")) {
                     self.consumerInboundConversion.prop('disabled', false);
                     self.view.submitModelElementAttributeChange(
                           "carnot:engine:camel::consumerBpmTypeConverter",
                           self.consumerBpmTypeConverter.prop("checked"));
                  }
                  else {
                     self.consumerRouteTextarea.removeClass("error");
                     self.consumerInboundConversion.prop('disabled', true);
                     
                     self.view.submitModelElementAttributeChange(
                           "carnot:engine:camel::consumerBpmTypeConverter",
                           false);
                     self.view.submitModelElementAttributeChange(
                           "carnot:engine:camel::consumerInboundConversion",
                           null);
                  }
               });
               
               this.consumerInboundConversion.change(function() {
                  if (!self.view.validate()) {
                     return;
                  }
                  
                  if (!self.validateConsumerRoute()) {
                     return;
                  }
                  
                  if(self.consumerInboundConversion.val() != "None") {
                     self.view.submitModelElementAttributeChange(
                           "carnot:engine:camel::consumerInboundConversion",
                           self.consumerInboundConversion.val());
                  }
                  else {
                     self.view.submitModelElementAttributeChange(
                           "carnot:engine:camel::consumerInboundConversion",
                           null);
                  }
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

                        if (self.inputBodyAccessPointInput.val() == m_constants.TO_BE_DEFINED) {
                           self.view
                                 .submitModelElementAttributeChange(
                                       "carnot:engine:camel::inBodyAccessPoint",
                                       null);
                        } else {
                           self.view
                                 .submitModelElementAttributeChange(
                                       "carnot:engine:camel::inBodyAccessPoint",
                                       self.inputBodyAccessPointInput
                                             .val());
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
               
               
               if(!this.getApplication().attributes["carnot:engine:camel::camelContextId"])
               {
                  this.camelContextInput.val('defaultCamelContext');
                   self.view.submitModelElementAttributeChange( "carnot:engine:camel::camelContextId", self.camelContextInput.val());
               }
               else
               {
                  this.camelContextInput.val(this.getApplication().attributes["carnot:engine:camel::camelContextId"]);                             
               }
               
               if(!this.getApplication().attributes["carnot:engine:camel::invocationPattern"])
               {
                  this.invocationPatternInput.val('send');
                  this.invocationTypeInput.val('synchronous');
                   
                  self.view.submitModelElementAttributeChange("carnot:engine:camel::invocationPattern", self.invocationPatternInput.val());
                   self.view.submitModelElementAttributeChange("carnot:engine:camel::invocationType", self.invocationTypeInput.val());
                   
                   this.invocationTypeInput.prop('disabled', true);
                  this.producerRouteTextarea.prop('disabled', false);
                  this.producerBpmTypeConverter.prop('disabled', false);
                  this.consumerRouteTextarea.prop('disabled', true);
                  this.consumerBpmTypeConverter.prop('disabled', true);
                  this.processContextHeadersInput.prop('disabled', false);
               }
               else
               {
                  this.invocationPatternInput.val(this.getApplication().attributes["carnot:engine:camel::invocationPattern"]);      
                  this.invocationTypeInput.val(this.getApplication().attributes["carnot:engine:camel::invocationType"]);      
                   
               }
               
               // set default to true if absent but invocation pattern is send or sendReveive
               if (this.getApplication().attributes["carnot:engine:camel::processContextHeaders"] && 
                     this.getApplication().attributes["carnot:engine:camel::invocationPattern"] && (
                     this.getApplication().attributes["carnot:engine:camel::invocationPattern"].indexOf("send") > -1 ||
                     this.getApplication().attributes["carnot:engine:camel::invocationPattern"].indexOf("sendReceive") > -1))
               {
                  this.processContextHeadersInput.prop("checked", true);
                  self.view.submitModelElementAttributeChange("carnot:engine:camel::processContextHeaders", true);
               }
               else
               {
                  this.processContextHeadersInput.prop("checked",
                        this.getApplication().attributes["carnot:engine:camel::processContextHeaders"]);
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
                           "carnot:engine:camel::applicationIntegrationOverlay" : "genericEndpointOverlay"//,
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

               this.inputBodyAccessPointInput.empty();
               this.inputBodyAccessPointInput.append("<option value='"
                     + m_constants.TO_BE_DEFINED + "'>"
                     + m_i18nUtils.getProperty("None") // TODO I18N
                     + "</option>");

               for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
                  var accessPoint = this.getApplication().contexts.application.accessPoints[n];

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

               this.inputBodyAccessPointInput
                     .val(this.getApplication().attributes["carnot:engine:camel::inBodyAccessPoint"]);
               this.outputBodyAccessPointInput
                     .val(this.getApplication().attributes["carnot:engine:camel::outBodyAccessPoint"]);
               
               // configuration tab
               this.camelContextInput
                     .val(this.getApplication().attributes["carnot:engine:camel::camelContextId"]);
               
               this.additionalBeanSpecificationTextarea
                  .val(this.getApplication().attributes["carnot:engine:camel::additionalSpringBeanDefinitions"]);
               
               this.invocationPatternInput
                     .val(this.getApplication().attributes["carnot:engine:camel::invocationPattern"]);
               
               this.invocationTypeInput
                     .val(this.getApplication().attributes["carnot:engine:camel::invocationType"]);
               
               // camel producer tab
               this.processContextHeadersInput.prop("checked",
                     this.getApplication().attributes["carnot:engine:camel::processContextHeaders"]);
               
               
                if(this.getApplication().attributes["carnot:engine:camel::transactedRoute"]==null||this.getApplication().attributes["carnot:engine:camel::transactedRoute"]===undefined){
                     this.view.submitModelElementAttributeChange("carnot:engine:camel::transactedRoute", true);
                  }
               
               this.transactedRouteInput.prop("checked",
                        this.getApplication().attributes["carnot:engine:camel::transactedRoute"]);
               
               this.producerBpmTypeConverter.prop("checked",
                     this.getApplication().attributes["carnot:engine:camel::producerBpmTypeConverter"]);
               
               this.producerOutboundConversion
                  .val(this.getApplication().attributes["carnot:engine:camel::producerOutboundConversion"]);
               
               this.producerInboundConversion
                  .val(this.getApplication().attributes["carnot:engine:camel::producerInboundConversion"]);
               
               if(this.producerBpmTypeConverter.prop("checked")) {
                  this.producerOutboundConversion.prop('disabled', false);
                  this.producerInboundConversion.prop('disabled', false);
               }else{
                  this.producerOutboundConversion.prop('disabled', true);
                  this.producerInboundConversion.prop('disabled', true);
               }
               
               this.producerRouteTextarea
                  .val(this.getApplication().attributes["carnot:engine:camel::routeEntries"]);
               
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
                  .val(this.getApplication().attributes["carnot:engine:camel::consumerRoute"]);
               
               this.consumerBpmTypeConverter.prop("checked",
                     this.getApplication().attributes["carnot:engine:camel::consumerBpmTypeConverter"]);
               
               this.consumerInboundConversion
                  .val(this.getApplication().attributes["carnot:engine:camel::consumerInboundConversion"]);
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
               }else{
                  this.consumerInboundConversion.prop('disabled', true);
               } 
               
               // legacy
               if (this.getApplication().attributes["carnot:engine:camel::producerMethodName"]
                     && this.getApplication().attributes["carnot:engine:camel::producerMethodName"]
                           .indexOf("sendBodyInOut") > -1) {
                  
                  this.sendReceiveSynchronous();
                  this.view.submitModelElementAttributeChange(
                        "carnot:engine:camel::producerMethodName", null);
                  
               } if (this.getApplication().attributes["carnot:engine:camel::producerMethodName"]
                     && this.getApplication().attributes["carnot:engine:camel::producerMethodName"]
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
               this.camelContextInput.removeClass("error");

               if (m_utils.isEmptyString(this.camelContextInput.val())) {
                  this.view.errorMessages
                        .push("Camel Context must not be empty."); // TODO
                  // I18N
                  this.camelContextInput.addClass("error");

                  return false;
               }

               return true;
            };
            
            /**
             * 
             */
            GenericEndpointOverlay.prototype.validateProducerRoute = function() {

               this.producerRouteTextarea.removeClass("error");
               if(!m_utils.isEmptyString(this.producerRouteTextarea.val())) {
                  var indexFromEndpoint = this.producerRouteTextarea.val().indexOf("<from");
                  if(indexFromEndpoint != -1) {
                     this.view.errorMessages
                     .push("Producer Route must not contain From Endpoint.");
                     this.producerRouteTextarea.addClass("error");
                  }
                  var indexExplicitProuducerOutboundConverter = this.producerRouteTextarea.val().indexOf("bean:bpmTypeConverter?method=to");
                  
                  // add check for new data endpoint
                  if(indexExplicitProuducerOutboundConverter == -1) {
                     indexExplicitProuducerOutboundConverter = this.producerRouteTextarea.val().indexOf("ipp:data:to");
                  }
                  
                  if(indexExplicitProuducerOutboundConverter != -1 && this.producerOutboundConversion.val() != "None" && 
                        !m_utils.isEmptyString(this.producerOutboundConversion.val())) {
                     // check if this converter is in the same place as the injected Inbound converter
                     // check if the explicit outbound conversion is right after setHeader Endpoint
                     var setHeaderIndex = this.producerRouteTextarea.val().indexOf("</setHeader>");
                     if(setHeaderIndex == -1) {
                        this.view.errorMessages
                        .push("More than one Producer Outbound Conversion specified.");
                        this.producerRouteTextarea.addClass("error");
                     }
                     else  {
                        var beforeExplicitProuducerOutboundConverter = this.producerRouteTextarea.val().substring(setHeaderIndex, indexExplicitProuducerOutboundConverter);
                        var toUriCount = beforeExplicitProuducerOutboundConverter.match(/<to/g);
                        if(toUriCount.length == 1) {
                           this.view.errorMessages
                           .push("More than one Producer Outbound Conversion specified.");
                           this.producerRouteTextarea.addClass("error");
                        }
                     }
                  }
                  
                  var indexExplicitProuducerInboundConverter = this.producerRouteTextarea.val().indexOf("bean:bpmTypeConverter?method=from");
                  
                  // add check for new data endpoint
                  if(indexExplicitProuducerInboundConverter == -1) {
                     indexExplicitProuducerInboundConverter = this.producerRouteTextarea.val().indexOf("ipp:data:from");
                  }
                  
                  if(indexExplicitProuducerInboundConverter != -1 && this.producerInboundConversion.val() != "None" && 
                        !m_utils.isEmptyString(this.producerInboundConversion.val())) {
                     // check if the explicit inbound conversion is right after last To Endpoint
                     var lastProducerRoutePart = this.producerRouteTextarea.val()
                        .substring(indexExplicitProuducerInboundConverter, this.producerRouteTextarea.val().length);
                     if(lastProducerRoutePart.indexOf("<to") == -1) {
                        this.view.errorMessages
                        .push("More than one Producer Inbound Conversion specified.");
                        this.producerRouteTextarea.addClass("error");
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
               this.view
                     .submitChanges({
                        type : applicationTypeChanges,
                        attributes : {
                           "carnot:engine:camel::invocationPattern" : invocationPatternChanges,
                           "carnot:engine:camel::invocationType" : invocationTypeChanges,
                           "carnot:engine:camel::routeEntries" : producerRoute,
                           "carnot:engine:camel::producerBpmTypeConverter" : producerBpmTypeConverter,
                           "carnot:engine:camel::producerOutboundConversion" : producerOutboundConversion,
                           "carnot:engine:camel::producerInboundConversion" : producerInboundConversion,
                           "carnot:engine:camel::consumerRoute" : consumerRoute,
                           "carnot:engine:camel::consumerBpmTypeConverter" : consumerBpmTypeConverter,
                           "carnot:engine:camel::consumerInboundConversion" : consumerInboundConversion,
                           "carnot:engine:camel::processContextHeaders" : includeProcessContextHeaders
                        }
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

         }
      });