define(
         [ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_i18nUtils",
                  "bpm-modeler/js/m_constants", "bpm-modeler/js/m_urlUtils",
                  "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_command",
                  "bpm-modeler/js/m_model", "bpm-modeler/js/m_accessPoint",
                  "bpm-modeler/js/m_typeDeclaration",
                  "bpm-modeler/js/m_parameterDefinitionsPanel",
                  "bpm-modeler/js/m_codeEditorAce", "bpm-modeler/js/m_modelElementUtils",
                  "bpm-modeler/js/m_routeDefinitionUtils",
                  "bpm-modeler/js/m_mailRouteDefinitionHandler",
                  "bpm-modeler/js/m_angularContextUtils",
                  "bpm-modeler/js/MailIntegrationOverlayTestTabHandler",
                  "bpm-modeler/js/MailIntegrationOverlayResponseTabHandler","bpm-modeler/js/m_user" ],
         function(m_utils, m_i18nUtils, m_constants, m_urlUtils, m_commandsController,
                  m_command, m_model, m_accessPoint, m_typeDeclaration,
                  m_parameterDefinitionsPanel, m_codeEditorAce, m_modelElementUtils,
                  m_routeDefinitionUtils, m_mailRouteDefinitionHandler,
                  m_angularContextUtils, mailIntegrationOverlayTestTabHandler,
                  mailIntegrationOverlayResponseTabHandler,m_user)
         {
            return {
               create : function(view)
               {
                  var overlay = new MailIntegrationOverlay();
                  overlay.initialize(view);
                  return overlay;
               }
            };
            function MailIntegrationOverlay()
            {
               MailIntegrationOverlay.prototype.initialize = function(view)
               {
                  this.view = view;
                  this.initializePropertiesTab(this.view);
                  mailIntegrationOverlayResponseTabHandler.initialize(this);
                  mailIntegrationOverlayTestTabHandler.initialize(this);
                  this.loadLabels();

                  var rdmNo = Math.floor((Math.random() * 100000) + 1);
                  this.mailTemplateEditor.id = "mailTemplateEditor" + rdmNo;

                  CKEDITOR.replace(this.mailTemplateEditor.id, {
                     toolbarGroups : this.getToolbarConfiguration()
                  });

                  this.registerConfigurationTabEvents();
                  this.registerParametersTabEvents();
               };
               /**
                * update the UI according the the template source being
                * used(embedded/repository/classpath/data)
                * 
                */
               MailIntegrationOverlay.prototype.setTemplateSource = function(
                        templateSource)
               {
                  if (!templateSource)
                  {
                     templateSource = "embedded";
                  }
                  this.templateSourceSelect.val(templateSource);
                  this.identifierInSubjectInput.prop('disabled', true);
                  m_utils.jQuerySelect("#embeddedTemplateSource").hide();
                  m_utils.jQuerySelect("#externalTemplateSource").hide();
                  mailIntegrationOverlayResponseTabHandler.disableResponseTypeSelect();
                  if (templateSource === "embedded")
                  {
                     m_utils.jQuerySelect("#embeddedTemplateSource").show();
                     this.templatePathInput.val("");
                     this.identifierInSubjectInput.prop('disabled', false);
                     mailIntegrationOverlayResponseTabHandler.enableResponseTypeSelect();
                  }
                  else if (templateSource === "data")
                  {
                     if (this.identifierInSubjectInput.prop('checked'))
                        this.identifierInSubjectInput.prop('checked', false);
                     mailIntegrationOverlayResponseTabHandler.setResponseType("none");
                  }
                  else
                  {
                     if (this.identifierInSubjectInput.prop('checked'))
                        this.identifierInSubjectInput.prop('checked', false);
                     mailIntegrationOverlayResponseTabHandler.setResponseType("none");
                     m_utils.jQuerySelect("#externalTemplateSource").show();
                     CKEDITOR.instances[this.mailTemplateEditor.id].setData("");
                  }
               };
               /**
                * 
                */
               MailIntegrationOverlay.prototype.getModelElement = function()
               {
                  return this.view.getModelElement();
               };
               /**
                * 
                */
               MailIntegrationOverlay.prototype.getApplication = function()
               {
                  return this.view.application;
               };
               /**
                * 
                */
               MailIntegrationOverlay.prototype.getScopeModel = function()
               {
                  return this.view.getModelElement().model;
               };
               /**
                * Create default application's Acces Points and populate default
                * application extended attributes.
                */
               MailIntegrationOverlay.prototype.activate = function()
               {
                  var submitElements = {};
                  var accessPoints = this.getApplication().contexts.application.accessPoints;
                  if (accessPoints.length < 5)
                  {
                     accessPoints = this.createIntrinsicAccessPoints(accessPoints);
                     submitElements.contexts = {
                        application : {
                           accessPoints : accessPoints
                        }
                     };
                  }
                  var attributes = this.getApplication().attributes;
                  var specificAttributes = {};
                  if (!attributes["carnot:engine:camel::applicationIntegrationOverlay"])
                     specificAttributes["carnot:engine:camel::applicationIntegrationOverlay"] = "mailIntegrationOverlay";
                  if (!attributes["carnot:engine:camel::camelContextId"])
                     specificAttributes["carnot:engine:camel::camelContextId"] = "defaultCamelContext";
                  if (!attributes["carnot:engine:camel::includeAttributesAsHeaders"])
                     specificAttributes["carnot:engine:camel::includeAttributesAsHeaders"] = "false";
                  if (!attributes["carnot:engine:camel::processContextHeaders"])
                     specificAttributes["carnot:engine:camel::processContextHeaders"] = "true";
                  if (attributes["carnot:engine:camel::autoStartup"] == null
                           || attributes["carnot:engine:camel::autoStartup"] === undefined)
                     specificAttributes["carnot:engine:camel::autoStartup"] = true;
                  if (Object.keys(specificAttributes).length > 0)
                     submitElements.attributes = specificAttributes;
                  if (submitElements.attributes || submitElements.contexts)
                     this.view.submitChanges(submitElements, true);
                  this.validate();
               };
               /**
                * Creates the default application Access Points
                */
               MailIntegrationOverlay.prototype.createIntrinsicAccessPoints = function(
                        accessPoints)
               {
                  var defaultAccessPoints = [];
                  if (!accessPoints["to"])
                  {
                     defaultAccessPoints.push({
                        id : "to",
                        name : "to",
                        dataType : "primitive",
                        primitiveDataType : "String",
                        direction : "IN",
                        attributes : {
                           "stardust:predefined" : true
                        }
                     });
                  }
                  if (!accessPoints["from"])
                  {
                     defaultAccessPoints.push({
                        id : "from",
                        name : "from",
                        dataType : "primitive",
                        primitiveDataType : "String",
                        direction : "IN",
                        attributes : {
                           "stardust:predefined" : true
                        }
                     });
                  }
                  if (!accessPoints["cc"])
                  {
                     defaultAccessPoints.push({
                        id : "cc",
                        name : "cc",
                        dataType : "primitive",
                        primitiveDataType : "String",
                        direction : "IN",
                        attributes : {
                           "stardust:predefined" : true
                        }
                     });
                  }
                  if (!accessPoints["bcc"])
                  {
                     defaultAccessPoints.push({
                        id : "bcc",
                        name : "bcc",
                        dataType : "primitive",
                        primitiveDataType : "String",
                        direction : "IN",
                        attributes : {
                           "stardust:predefined" : true
                        }
                     });
                  }
                  if (!accessPoints["subject"])
                  {
                     defaultAccessPoints.push({
                        id : "subject",
                        name : "subject",
                        dataType : "primitive",
                        primitiveDataType : "String",
                        direction : "IN",
                        attributes : {
                           "stardust:predefined" : true
                        }
                     });
                  }
                  return defaultAccessPoints;
               }
               /**
                * Returns the Extended attribute value
                */
               MailIntegrationOverlay.prototype.getExtendedAttributeValue = function(key)
               {
                  return this.getApplication().attributes[key];
               };
               /**
                * Updates the view using extended attributes values.
                */
               MailIntegrationOverlay.prototype.update = function()
               {
                  this.parameterDefinitionsPanel.setScopeModel(this.getScopeModel());
                  this.parameterDefinitionsPanel.setParameterDefinitions(this.getApplication().contexts.application.accessPoints);
                  
                  this.autoStartupRow.hide();
                  if(this.isIntegrator()){
                    this.autoStartupRow.show();
                  }
                  this.serverInput.val(this
                           .getExtendedAttributeValue("stardust:emailOverlay::server"));
                  this.mailFormatSelect
                           .val(this
                                    .getExtendedAttributeValue("stardust:emailOverlay::mailFormat"));
                  
                  if(this.mailFormatSelect.val()=="text/plain"){
                     CKEDITOR.config.startupMode = 'source';
                  }else{
                     CKEDITOR.config.startupMode = 'wysiwyg';
                  }
                  
                  this.protocolSelect.val(this
                           .getExtendedAttributeValue("stardust:emailOverlay::protocol"));
                  this.subjectInput.val(this
                           .getExtendedAttributeValue("stardust:emailOverlay::subject"));
                  this.identifierInSubjectInput
                           .prop(
                                    "checked",
                                    this
                                             .getExtendedAttributeValue("stardust:emailOverlay::includeUniqueIdentifierInSubject"));
                  this.toInput.val(this
                           .getExtendedAttributeValue("stardust:emailOverlay::to"));
                  this.fromInput.val(this
                           .getExtendedAttributeValue("stardust:emailOverlay::from"));
                  this.ccInput.val(this
                           .getExtendedAttributeValue("stardust:emailOverlay::cc"));
                  this.bccInput.val(this
                           .getExtendedAttributeValue("stardust:emailOverlay::bcc"));
                  this.storeEmailInput
                           .prop(
                                    "checked",
                                    this
                                             .getExtendedAttributeValue("stardust:emailOverlay::storeEmail"));
                  this.storeAttachmentsInput
                           .prop(
                                    "checked",
                                    this
                                             .getExtendedAttributeValue("stardust:emailOverlay::storeAttachments"));
                  this.templatePathInput
                           .val(this
                                    .getExtendedAttributeValue("stardust:emailOverlay::templatePath"));
                  this.userInput.val(this
                           .getExtendedAttributeValue("stardust:emailOverlay::user"));
                  this.passwordInput.val(this
                           .getExtendedAttributeValue("stardust:emailOverlay::pwd"));
                  this.autoStartupInput
                           .prop(
                                    "checked",
                                    this
                                             .getExtendedAttributeValue("carnot:engine:camel::autoStartup"));
                  this
                           .setTemplateSource(this
                                    .getExtendedAttributeValue("stardust:emailOverlay::templateSource"));
                  mailIntegrationOverlayResponseTabHandler.update();
                  mailIntegrationOverlayResponseTabHandler
                           .setResponseType(this
                                    .getExtendedAttributeValue("stardust:emailOverlay::responseType"));

                  var templateConfigurationsJson = this
                           .getExtendedAttributeValue("stardust:emailOverlay::templateConfigurations");

                  if (!templateConfigurationsJson)
                  {
                     templateConfigurationsJson = "[]";
                  }
                  this.templateConfigurations = JSON.parse(templateConfigurationsJson);
                  this.typeDeclarationsTab = [];
                  var typeDeclarations = this.getScopeModel().typeDeclarations;
                  for ( var i in typeDeclarations)
                  {
                     this.typeDeclarationsTab.push(typeDeclarations[i]);
                  }
                  this.attachmentsTemplateSource = this
                           .getExtendedAttributeValue("stardust:emailOverlay::attachmentsTemplateSource");
                  this.attachmentsTemplateSourceType = this
                           .getExtendedAttributeValue("stardust:emailOverlay::attachmentsTemplateSourceType");
                  if (this.attachmentsTemplateSource == "data")
                  {
                     m_angularContextUtils
                              .runInAngularContext(
                                       function($scope)
                                       {
                                          $scope.temSrcOptInit = $scope.overlayPanel.templateSourceOptions[1].value;
                                       },
                                       m_utils
                                                .jQuerySelect("#attachmentsTemplateSourceSelect"));
                     var typeDeclaration = this.attachmentsTemplateSourceType;
                     m_angularContextUtils
                              .runInAngularContext(
                                       function($scope)
                                       {
                                          $scope.typeDeclaration = typeDeclaration;
                                       },
                                       m_utils
                                                .jQuerySelect("#attachmentsTemplateSourceTypeTab #attachmentsTemplateSourceTypeSelect"));
                     m_utils.jQuerySelect("#attachmentsTemplateSourceTypeTab").show();
                     m_utils.jQuerySelect("#templateConfigurationTab").hide();
                  }
                  else if (this.attachmentsTemplateSource == "embedded"
                           || this.attachmentsTemplateSource == undefined)
                  {
                     m_angularContextUtils
                              .runInAngularContext(
                                       function($scope)
                                       {
                                          $scope.temSrcOptInit = $scope.overlayPanel.templateSourceOptions[0].value;
                                       },
                                       m_utils
                                                .jQuerySelect("#attachmentsTemplateSourceSelect"));
                     m_utils.jQuerySelect("#attachmentsTemplateSourceTypeTab").hide();
                     m_utils.jQuerySelect("#templateConfigurationTab").show();
                  }
                  else
                  {
                     m_angularContextUtils
                              .runInAngularContext(
                                       function($scope)
                                       {
                                          $scope.temSrcOptInit = $scope.overlayPanel.templateSourceOptions[2].value;
                                       },
                                       m_utils
                                                .jQuerySelect("#attachmentsTemplateSourceSelect"));
                     m_utils.jQuerySelect("#attachmentsTemplateSourceTypeTab").hide();
                     m_utils.jQuerySelect("#templateConfigurationTab").hide();
                  }
               };
               /**
                * generates the xml definiton of the route using the provided values.
                */
               MailIntegrationOverlay.prototype.getRoute = function(attributes,accessPoints)
               {
                  var route = m_mailRouteDefinitionHandler.createRouteForEmail(attributes,accessPoints);
                  m_utils.debug(route);
                  return route;
               };
               /**
                * 
                */
               MailIntegrationOverlay.prototype.submitChanges = function(skipValidation)
               {
                  // nothing to be done here.
               };
               /**
                * Submit changes relates to parameters definition
                */
               MailIntegrationOverlay.prototype.submitParameterDefinitionsChanges = function(
                        parameterDefinitionsChanges)
               {
                  var applicationTypeChanges = null;
                  if (mailIntegrationOverlayResponseTabHandler.getResponseTypeSelect() === "none")
                  {
                     applicationTypeChanges = "camelSpringProducerApplication";
                  }
                  else
                  {
                     applicationTypeChanges = "camelConsumerApplication";
                  }
                  this.view.submitChanges({
                     type : applicationTypeChanges,
                     contexts : {
                        application : {
                           accessPoints : parameterDefinitionsChanges
                        }
                     }
                  }, false);
               };
               MailIntegrationOverlay.prototype.validateRecipientsFields = function(inputField){
            	   if(!m_utils.isEmptyString(inputField.val())){
                 	  if(inputField.val().indexOf(';') != -1){
                 		  this.view.errorMessages.push("To configure multiple recipients please use comma instead of semicolon.");
                 		  inputField.addClass("error");
                          valid = false;
                 	  }
                   }
               }
               
               /**
                * Validates the provided values.
                */
               MailIntegrationOverlay.prototype.validate = function()
               {
                  var valid = true;
                  this.serverInput.removeClass("error");
                  this.userInput.removeClass("error");
                  this.passwordInput.removeClass("error");
                  this.templatePathInput.removeClass("error");
                  this.parameterDefinitionNameInput.removeClass("error");
                  this.fromInput.removeClass("error");
                  this.validateRecipientsFields(this.fromInput);
                  this.toInput.removeClass("error");
                  this.validateRecipientsFields(this.toInput);
                  this.ccInput.removeClass("error");
                  this.validateRecipientsFields(this.ccInput);
                  this.bccInput.removeClass("error");
                  this.validateRecipientsFields(this.bccInput);
                 
                  
                  var parameterDefinitionNameInputWhithoutSpaces = this.parameterDefinitionNameInput
                           .val().replace(/ /g, "");
                  if ((parameterDefinitionNameInputWhithoutSpaces == "exchange")
                           || (parameterDefinitionNameInputWhithoutSpaces == "headers"))
                  {
                     this.view.errorMessages.push(this.parameterDefinitionNameInput.val()
                              + " cannot be used as an access point");
                     this.parameterDefinitionNameInput.addClass("error");
                     valid = false;
                  }
                  for (var n = 0; n < this.getApplication().contexts.application.accessPoints.length; n++)
                  {
                     var ap = this.getApplication().contexts.application.accessPoints[n];
                     if ((ap.name.replace(/ /g, "") == "headers")
                              || (ap.name.replace(/ /g, "") == "exchange"))
                     {
                        if (this.view.errorMessages.indexOf(ap.name.replace(/ /g, "")
                                 + " cannot be used as an access point") < 0)
                        {
                           this.view.errorMessages.push(ap.name.replace(/ /g, "")
                                    + " cannot be used as an access point");
                        }
                        this.parameterDefinitionNameInput.addClass("error");
                        valid = false;
                     }
                  }
                  // if ( )
                  if (m_utils.isEmptyString(this.serverInput.val())
                           && m_utils
                                    .isEmptyString(this
                                             .getExtendedAttributeValue("stardust:emailOverlay::server")))
                  {//
                     this.view.errorMessages.push("Mail server must be defined."); // TODO
                     // I18N
                     this.serverInput.addClass("error");
                     valid = false;
                  }
                  var templateSourceEA = this.templateSourceSelect.val();
                  if ((!m_utils.isEmptyString(templateSourceEA))
                           && (templateSourceEA == "repository" || templateSourceEA == "classpath"))
                  {
                     if (m_utils.isEmptyString(this.templatePathInput.val()))
                     {
                        this.view.errorMessages
                                 .push("Please provide a template location"); // TODO
                        // I18N
                        this.templatePathInput.addClass("error");
                        valid = false;
                     }
                  }
                  if (this.getApplication().contexts.application.accessPoints.length > 0)
                  {
                     for (var n = 0; n < this.getApplication().contexts.application.accessPoints.length; n++)
                     {
                        var ap = this.getApplication().contexts.application.accessPoints[n];
                        if (ap.direction == m_constants.IN_ACCESS_POINT
                                 && ap.dataType == m_constants.DOCUMENT_DATA_TYPE)
                        {
                           var occurence = this.getTemplateConfigurationDetails(
                                    this.templateConfigurations, ap.name);
                           if (this.countOccurences(this.templateConfigurations, ap.name) > 1
                                    && occurence.tSource == "data"
                                    && ap.direction == m_constants.IN_ACCESS_POINT
                                    && ap.dataType == m_constants.DOCUMENT_DATA_TYPE)
                           {
                              this.view.errorMessages
                                       .push("Change cannot be performed because an attachment already exists with the name ["
                                                + ap.name + "]");
                              this.parameterDefinitionNameInput
                                       .val(this.parameterDefinitionsPanel.currentParameterDefinition.name);
                              this.parameterDefinitionsPanel
                                       .populateParameterDefinitionFields();
                              this.parameterDefinitionNameInput.addClass("error");
                              valid = false;
                           }
                        }
                     }
                  }
                  return valid;
               };
               /**
                * Adds a template configuration.
                */
               MailIntegrationOverlay.prototype.addTemplateConfigurationForDocumentAp = function(
                        accessPoint)
               {
                  this.templateConfigurations.push({
                     "tTemplate" : false,
                     "tName" : accessPoint.name,
                     "tPath" : "",
                     "tSource" : "data",
                     "tFormat" : "plain"
                  });
                  this.view.submitModelElementAttributeChange(
                           "stardust:emailOverlay::templateConfigurations", angular
                                    .toJson(this.templateConfigurations));
               };

               MailIntegrationOverlay.prototype.deleteTemplateConfigurationForDocumentAp = function(
                        accessPoint)
               {
                  var templateConf = this.templateConfigurations;
                  for ( var i in templateConf)
                  {
                     if (templateConf[i].tName == accessPoint.name)
                     {
                        this.templateConfigurations.splice(i, 1);
                        this.view.submitModelElementAttributeChange(
                                 "stardust:emailOverlay::templateConfigurations", angular
                                          .toJson(this.templateConfigurations));
                        break;
                     }
                  }
               };

               MailIntegrationOverlay.prototype.updateTemplateConfigurationForDocumentAp = function(
                        accessPoint, newName)
               {
                  if (this.getTemplateConfigurationDetails(this.templateConfigurations,
                           accessPoint.name))
                  {
                     // update Template element Name
                     for ( var i in this.templateConfigurations)
                     {
                        if (this.templateConfigurations[i].tSource == "data"
                                 && this.templateConfigurations[i].tName == accessPoint.name)
                        {
                           this.templateConfigurations[i].tName = newName;
                           this.view.submitModelElementAttributeChange(
                                    "stardust:emailOverlay::templateConfigurations",
                                    angular.toJson(this.templateConfigurations));
                           break;
                        }
                     }

                  }
                  else
                  {
                     // add tempate conf
                     accessPoint.name = newName;
                     this.addTemplateConfigurationForDocumentAp(accessPoint);
                  }
               };
               /**
                * 
                */
               MailIntegrationOverlay.prototype.getTemplateConfigurationDetails = function(
                        templateConfigurations, accessPointName)
               {
                  var count = 0;
                  for ( var i in templateConfigurations)
                  {
                     if (templateConfigurations[i].tName == accessPointName
                              && templateConfigurations[i].tSource == "data")
                     {
                        return templateConfigurations[i];
                     }
                  }
                  return null;
               };
               /**
                * 
                */
               MailIntegrationOverlay.prototype.countOccurences = function(
                        templateConfigurations, accessPointName)
               {
                  var count = 0;
                  for ( var i in templateConfigurations)
                  {
                     if (templateConfigurations[i].tName == accessPointName)
                     {
                        count++;
                     }
                  }
                  return count;
               };
               /**
                * 
                */
               MailIntegrationOverlay.prototype.notify = function(occurence)
               {
                  this.view.errorMessages
                           .push("Change cannot be performed because an attachment already exists with the name ["
                                    + occurence.name + "]");
                  this.view.showErrorMessages();
                  this.parameterDefinitionsPanel.populateParameterDefinitionFields();
               };
               /**
                * Register events of components located iin Parameters Tab
                */
               MailIntegrationOverlay.prototype.registerParametersTabEvents = function()
               {
                  this.parameterDefinitionsPanel = m_parameterDefinitionsPanel.create({
                     scope : "parametersTab",
                     submitHandler : this,
                     supportsOrdering : false,
                     supportsDataMappings : false,
                     supportsDescriptors : false,
                     supportsDataTypeSelection : true,
                     supportsDocumentTypes : true
                  });

                  this.parameterDefinitionDirectionSelect.unbind("change");
                  this.parameterDefinitionDirectionSelect
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var accessPoints = event.data.panel
                                                .getApplication().contexts.application.accessPoints;
                                       var ap = event.data.panel.parameterDefinitionsPanel.currentParameterDefinition;
                                       var accessPoint = m_routeDefinitionUtils
                                                .findAccessPoint(accessPoints, ap.id);
                                       var occurence = event.data.panel
                                                .getTemplateConfigurationDetails(
                                                         event.data.panel.templateConfigurations,
                                                         ap.name);
                                       if (occurence
                                                && occurence.tSource == "data"
                                                && ap.dataType == m_constants.DOCUMENT_DATA_TYPE)
                                       {
                                          event.data.panel.templateConfigurations = event.data.panel
                                                   .removeTemplateConfiguration(
                                                            event.data.panel.templateConfigurations,
                                                            ap);
                                       }

                                       ap.direction = event.data.panel.parameterDefinitionDirectionSelect
                                                .val();
                                       if (ap.dataType == m_constants.DOCUMENT_DATA_TYPE
                                                && ap.direction == m_constants.IN_ACCESS_POINT
                                                && !occurence)
                                       {
                                          event.data.panel.templateConfigurations.push({
                                             "tTemplate" : false,
                                             "tName" : ap.name,
                                             "tPath" : "",
                                             "tSource" : "data",
                                             "tFormat" : "plain"
                                          });
                                       }
                                       if (!accessPoint)
                                       {
                                          accessPoints.push(ap);
                                       }
                                       event.data.panel.view
                                                .submitChanges(
                                                         {
                                                            attributes : {
                                                               "stardust:emailOverlay::templateConfigurations" : angular
                                                                        .toJson(event.data.panel.templateConfigurations)
                                                            },
                                                            contexts : {
                                                               application : {
                                                                  accessPoints : accessPoints
                                                               }
                                                            }
                                                         }, false);
                                    });
                  this.parameterDefinitionNameInput.unbind("change");
                  this.parameterDefinitionNameInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var accessPoints = event.data.panel.parameterDefinitionsPanel.parameterDefinitions;
                                       var ap = event.data.panel.parameterDefinitionsPanel.currentParameterDefinition;
                                       var accessPoint = m_routeDefinitionUtils
                                                .findAccessPoint(accessPoints, ap.id);
                                       var occurence = event.data.panel
                                                .getTemplateConfigurationDetails(
                                                         event.data.panel.templateConfigurations,
                                                         ap.name);
                                       if (occurence
                                                && occurence.tSource == "data"
                                                && ap.dataType == m_constants.DOCUMENT_DATA_TYPE)
                                       {
                                          event.data.panel.templateConfigurations = event.data.panel
                                                   .removeTemplateConfiguration(
                                                            event.data.panel.templateConfigurations,
                                                            ap);
                                       }
                                       if (occurence
                                                && occurence.tSource != "data"
                                                && ap.direction == m_constants.IN_ACCESS_POINT
                                                && ap.dataType == m_constants.DOCUMENT_DATA_TYPE)
                                       {
                                          event.data.panel.notify(ap);
                                          return;
                                       }
                                       ap.name = event.data.panel.parameterDefinitionNameInput
                                                .val();
                                       if (ap.direction == m_constants.IN_ACCESS_POINT
                                                && ap.dataType == m_constants.DOCUMENT_DATA_TYPE)
                                       {
                                          event.data.panel.add(ap);
                                       }else{
                                         // event.data.panel.parameterDefinitionsPanel.submitChanges();
                                          var attributes= event.data.panel.getApplication().attributes;
                                          attributes["carnot:engine:camel::routeEntries"]=  event.data.panel.getRoute(event.data.panel.getApplication().attributes,accessPoints);
                                          event.data.panel.view
                                          .submitChanges(
                                                   {
                                                      attributes : attributes,
                                                      contexts : {
                                                         application : {
                                                            accessPoints : event.data.panel.parameterDefinitionsPanel.parameterDefinitions
                                                         }
                                                      }
                                                   }, false);
                                          
                                       }
                                    });
                  this.dataTypeSelect.unbind("change");
                  this.dataTypeSelect
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var accessPoints = event.data.panel.parameterDefinitionsPanel.parameterDefinitions;
                                       var ap = event.data.panel.parameterDefinitionsPanel.currentParameterDefinition;
                                       var accessPoint = m_routeDefinitionUtils
                                                .findAccessPoint(accessPoints, ap.id);
                                       var occurence = event.data.panel
                                                .getTemplateConfigurationDetails(
                                                         event.data.panel.templateConfigurations,
                                                         ap.name);
                                       if (occurence
                                                && occurence.tSource == "data"
                                                && ap.dataType == m_constants.DOCUMENT_DATA_TYPE)
                                       {
                                          event.data.panel.templateConfigurations = event.data.panel
                                                   .removeTemplateConfiguration(
                                                            event.data.panel.templateConfigurations,
                                                            ap);
                                       }
                                       if (occurence
                                                && occurence.tSource != "data"
                                                && ap.direction == m_constants.IN_ACCESS_POINT
                                                && ap.dataType == m_constants.DOCUMENT_DATA_TYPE)
                                       {
                                          event.data.panel.notify(ap);
                                          return;
                                       }
                                       ap.dataType = event.data.panel.dataTypeSelect
                                                .val();

                                       event.data.panel.parameterDefinitionsPanel.dataTypeSelector
                                                .setDataTypeSelectVal({
                                                   dataType : event.data.panel.dataTypeSelect
                                                            .val()
                                                });

                                       if (ap.direction == m_constants.IN_ACCESS_POINT
                                                && ap.dataType == m_constants.DOCUMENT_DATA_TYPE)
                                       {
                                          event.data.panel.add(ap);
                                       }
                                       else
                                       {
                                          var structTypeFullId;
                                          var primitiveDataType;
                                          if (m_constants.PRIMITIVE_DATA_TYPE == event.data.panel.dataTypeSelect
                                                   .val())
                                          {
                                             primitiveDataType = event.data.panel.parameterDefinitionsPanel.dataTypeSelector.primitiveDataTypeSelect
                                                      .val();
                                             if (event.data.panel.parameterDefinitionsPanel.dataTypeSelector
                                                      .isEnumTypeDeclaration(primitiveDataType))
                                             {
                                                structTypeFullId = event.data.panel.parameterDefinitionsPanel.dataTypeSelector.primitiveDataTypeSelect
                                                         .val();
                                             }
                                          }
                                          else if (m_constants.STRUCTURED_DATA_TYPE == event.data.panel.dataTypeSelect
                                                   .val())
                                          {
                                             structTypeFullId = event.data.panel.parameterDefinitionsPanel.dataTypeSelector.structuredDataTypeSelect
                                                      .val();
                                          }
                                          else if (m_constants.DOCUMENT_DATA_TYPE == event.data.panel.dataTypeSelect
                                                   .val())
                                          {
                                             structTypeFullId = event.data.panel.documentTypeSelect
                                                      .val();
                                          }
                                          event.data.panel.parameterDefinitionsPanel.currentParameterDefinition.dataType = event.data.panel.dataTypeSelect
                                                   .val();
                                          event.data.panel.parameterDefinitionsPanel.currentParameterDefinition.primitiveDataType = primitiveDataType;
                                          event.data.panel.parameterDefinitionsPanel.currentParameterDefinition.structuredDataTypeFullId = structTypeFullId;
                                          var attributes=event.data.panel.getApplication().attributes;
                                          attributes["stardust:emailOverlay::templateConfigurations"]= angular.toJson(event.data.panel.templateConfigurations);
                                          attributes["carnot:engine:camel::routeEntries"] = event.data.panel.getRoute(attributes,event.data.panel.parameterDefinitionsPanel.parameterDefinitions);
                                          
                                          event.data.panel.view
                                                   .submitChanges(
                                                            {
                                                               attributes : attributes,
                                                               contexts : {
                                                                  application : {
                                                                     accessPoints : event.data.panel.parameterDefinitionsPanel.parameterDefinitions
                                                                  }
                                                               }
                                                            }, false);
                                       }
                                    });
                  this.documentTypeSelect.unbind("change");
                  this.documentTypeSelect
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var ap = event.data.panel.parameterDefinitionsPanel.currentParameterDefinition;
                                       if (ap.direction == m_constants.IN_ACCESS_POINT)
                                       {
                                          var occurence = event.data.panel
                                                   .getTemplateConfigurationDetails(
                                                            event.data.panel.templateConfigurations,
                                                            event.data.panel.parameterDefinitionNameInput
                                                                     .val());
                                          if (!occurence
                                                   && event.data.panel.dataTypeSelect
                                                            .val() == m_constants.DOCUMENT_DATA_TYPE)
                                          {
                                             event.data.panel
                                                      .addTemplateConfigurationForDocumentAp(ap);
                                          }
                                       }
                                       event.data.panel.parameterDefinitionsPanel.dataTypeSelector
                                                .setDocumentDataType(event.data.panel.documentTypeSelect
                                                         .val());
                                       event.data.panel.parameterDefinitionsPanel.dataTypeSelector
                                                .submitChanges();
                                    });
                  this.parameterDefinitionsPanel.deleteParameterDefinitionButton
                           .unbind("click");
                  this.deleteParameterDefinitionButton
                           .click(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var accessPoints = event.data.panel
                                                .getApplication().contexts.application.accessPoints;
                                       var ap = event.data.panel.parameterDefinitionsPanel.currentParameterDefinition;
                                       var filteredAccessPoints = m_routeDefinitionUtils
                                                .filterAccessPoint(accessPoints, ap.id);
                                       if (ap.direction == m_constants.IN_ACCESS_POINT
                                                && ap.dataType == m_constants.DOCUMENT_DATA_TYPE)
                                       {
                                          event.data.panel.templateConfigurations = event.data.panel
                                                   .removeTemplateConfiguration(
                                                            event.data.panel.templateConfigurations,
                                                            ap);
                                       }
                                       var attributes=event.data.panel.getApplication().attributes;
                                       attributes["stardust:emailOverlay::templateConfigurations"]=angular.toJson(event.data.panel.templateConfigurations);
                                       attributes["carnot:engine:camel::routeEntries"]= event.data.panel.getRoute(attributes, filteredAccessPoints);
                                       
                                       event.data.panel.view
                                                .submitChanges(
                                                         {
                                                            attributes : attributes,
                                                            contexts : {
                                                               application : {
                                                                  accessPoints : filteredAccessPoints
                                                               }
                                                            }
                                                         }, false);
                                       event.data.panel.parameterDefinitionsPanel.selectedRowIndex = event.data.panel.parameterDefinitionsPanel.parameterDefinitions.length - 1;
                                       event.data.panel.parameterDefinitionsPanel.currentFocusInput = event.data.panel.parameterDefinitionsPanel.parameterDefinitionNameInput;
                                    });

                  this.parameterDefinitionsPanel.addParameterDefinitionButton
                           .unbind("click");
                  this.addParameterDefinitionButton
                           .click(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var n = event.data.panel.parameterDefinitionsPanel
                                                .getNextIdIndex(), generatedID, fx;
                                       event.data.panel.parameterDefinitionsPanel.currentParameterDefinition = {
                                          id : "New_" + n, // TODO: Anticipates renaming
                                          // of ID
                                          // on server
                                          name : "New " + n, // TODO - i18n
                                          dataType : event.data.panel.dataTypeSelect
                                                   .val(),
                                          direction : "IN",
                                          dataFullId : null,
                                          dataPath : null
                                       };
                                       if (event.data.panel.parameterDefinitionsPanel.options.supportsDataTypeSelection)
                                       {
                                          event.data.panel.parameterDefinitionsPanel.dataTypeSelector
                                                   .getDataType(event.data.panel.parameterDefinitionsPanel.currentParameterDefinition);
                                       }
                                       var occurence = event.data.panel
                                                .getTemplateConfigurationDetails(
                                                         event.data.panel.templateConfigurations,
                                                         event.data.panel.parameterDefinitionsPanel.currentParameterDefinition.name);
                                       if (occurence
                                                && event.data.panel.parameterDefinitionsPanel.currentParameterDefinition.direction == m_constants.IN_ACCESS_POINT
                                                && event.data.panel.parameterDefinitionsPanel.currentParameterDefinition.dataType == m_constants.DOCUMENT_DATA_TYPE)
                                       {
                                          event.data.panel
                                                   .notify(event.data.panel.parameterDefinitionsPanel.currentParameterDefinition);
                                          return;
                                       }
                                       event.data.panel
                                                .add(event.data.panel.parameterDefinitionsPanel.currentParameterDefinition);
                                    });
               };

               MailIntegrationOverlay.prototype.removeTemplateConfiguration = function(
                        templateConfigurations, ap)
               {
                  var templateConf = templateConfigurations;
                  for ( var i in templateConf)
                  {
                     if (templateConf[i].tName == ap.name
                              && templateConfigurations[i].tSource == "data")
                     {
                        templateConfigurations.splice(i, 1);
                        break;
                     }
                  }
                  return templateConfigurations;
               }
               /**
                * 
                */
               MailIntegrationOverlay.prototype.add = function(ap)
               {
                  var accessPoints = this.getApplication().contexts.application.accessPoints;
                  var accessPoint = m_routeDefinitionUtils.findAccessPoint(accessPoints,
                           ap.id);
                  if (!accessPoint)
                  {
                     accessPoints.push(ap);
                  }
                  if (ap.direction == m_constants.IN_ACCESS_POINT
                           && ap.dataType == m_constants.DOCUMENT_DATA_TYPE)
                  {
                     this.templateConfigurations.push({
                        "tTemplate" : false,
                        "tName" : ap.name,
                        "tPath" : "",
                        "tSource" : "data",
                        "tFormat" : "plain"
                     });
                  }
                  var attributes=this.getApplication().attributes;
                  attributes["stardust:emailOverlay::templateConfigurations"]=angular.toJson(this.templateConfigurations);
                  attributes["carnot:engine:camel::routeEntries"]=this.getRoute(attributes,accessPoints);
                  this.view.submitChanges({
                     attributes : attributes,
                     contexts : {
                        application : {
                           accessPoints : accessPoints
                        }
                     }
                  }, false);
                  this.parameterDefinitionsPanel.setParameterDefinitions(accessPoints);
                  this.parameterDefinitionsPanel.selectedRowIndex = this.parameterDefinitionsPanel.parameterDefinitions.length - 1;
                  this.parameterDefinitionsPanel.currentFocusInput = this.parameterDefinitionsPanel.parameterDefinitionNameInput;
                  this.parameterDefinitionsPanel.populateParameterDefinitionFields();
               };
               /**
                * Contains the logic related to a template Source change event.
                * 
                */
               MailIntegrationOverlay.prototype.handleTemplateSourceChangeEvent = function()
               {
                  var attributes = {};
                  var aps;
                  var accessPoints = this.getApplication().contexts.application.accessPoints;
                  if (this.templateSourceSelect.val() != "data")
                  {
                     aps = m_routeDefinitionUtils.filterAccessPoint(accessPoints,
                              "mailContentAP");
                  }
                  else
                  {// if mailContentAP exists keep it otherwise create it
                     var mailContentAp = m_routeDefinitionUtils.findAccessPoint(
                              accessPoints, "mailContentAP");
                     if (!mailContentAp)
                     {
                        accessPoints.push({
                           id : "mailContentAP",
                           name : "Mail Content",
                           dataType : "primitive",
                           primitiveDataType : "String",
                           direction : "IN",
                           attributes : {
                              "stardust:predefined" : true
                           }
                        });
                        aps = accessPoints;
                     }
                  }
                  if (this.templateSourceSelect.val() == "embedded")
                  {
                     attributes["stardust:emailOverlay::templatePath"] = null;
                  }
                  else
                  {
                     attributes["stardust:emailOverlay::templatePath"] = this.templatePathInput
                              .val();
                  }
                  attributes["stardust:emailOverlay::templateSource"] = this.templateSourceSelect.val();
                  attributes["carnot:engine:camel::routeEntries"] = this.getRoute(attributes, aps);

                  this.view.submitChanges({
                     attributes : attributes,
                     contexts : {
                        application : {
                           accessPoints : aps
                        }
                     }
                  }, false);
                  this.setTemplateSource(this.templateSourceSelect.val());
               };
               /**
                * register events related to components location in the Configuration Tab
                * 
                */
               MailIntegrationOverlay.prototype.registerConfigurationTabEvents = function()
               {
                  var self = this;
                  this.autoStartupInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var attributes=event.data.panel.getApplication().attributes;
                                       attributes["carnot:engine:camel::autoStartup"]=event.data.panel.autoStartupInput.prop("checked");
                                       attributes["carnot:engine:camel::routeEntries"]= event.data.panel.getRoute(attributes,event.data.panel.getApplication().contexts.application.accessPoints);
                                       event.data.panel.view
                                                .submitChanges(
                                                         {
                                                            attributes : attributes
                                                         }, false);
                                    });
                  this.serverInput.change({
                     panel : this
                  }, function(event)
                  {
                     var attributes=event.data.panel.getApplication().attributes;
                     attributes["stardust:emailOverlay::server"]=event.data.panel.serverInput.val();
                     attributes["carnot:engine:camel::routeEntries"]= event.data.panel.getRoute(attributes,event.data.panel.getApplication().contexts.application.accessPoints);
                     event.data.panel.view.submitChanges({
                        attributes : attributes
                     }, false);
                  });
                  this.userInput.change({
                     panel : this
                  }, function(event)
                  {
                     var attributes=event.data.panel.getApplication().attributes;
                     attributes["stardust:emailOverlay::user"]=event.data.panel.userInput.val();
                     attributes["carnot:engine:camel::routeEntries"]= event.data.panel.getRoute(attributes,event.data.panel.getApplication().contexts.application.accessPoints);
                     
                     event.data.panel.view.submitChanges({
                        attributes : attributes
                     }, false);
                  });
                  this.passwordInput.change({
                     panel : this
                  }, function(event)
                  {
                     var attributes=event.data.panel.getApplication().attributes;
                     attributes["stardust:emailOverlay::pwd"]=event.data.panel.passwordInput.val();
                     attributes["carnot:engine:camel::routeEntries"]= event.data.panel.getRoute(attributes,event.data.panel.getApplication().contexts.application.accessPoints);
                     event.data.panel.view.submitChanges({
                        attributes : attributes
                     }, false);
                  });
                  this.mailFormatSelect
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var attributes=event.data.panel.getApplication().attributes;
                                       attributes["stardust:emailOverlay::mailFormat"]=event.data.panel.mailFormatSelect.val();
                                       attributes["carnot:engine:camel::routeEntries"]= event.data.panel.getRoute(attributes,event.data.panel.getApplication().contexts.application.accessPoints);
                                       event.data.panel.view
                                                .submitChanges(
                                                         {
                                                            attributes : attributes
                                                         }, false);
                                       if(event.data.panel.mailFormatSelect.val()=="text/plain"){
                                          CKEDITOR.instances[event.data.panel.mailTemplateEditor.id].setMode('source');
                                       }else{
                                          CKEDITOR.instances[event.data.panel.mailTemplateEditor.id].setMode('wysiwyg');
                                       }
                                    });
                  this.protocolSelect
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var attributes=event.data.panel.getApplication().attributes;
                                       attributes["stardust:emailOverlay::protocol"]=event.data.panel.protocolSelect.val();
                                       attributes["carnot:engine:camel::routeEntries"]= event.data.panel.getRoute(attributes,event.data.panel.getApplication().contexts.application.accessPoints);
                                       event.data.panel.view
                                                .submitChanges(
                                                         {
                                                            attributes : attributes
                                                         }, false);
                                    });
                  this.subjectInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var attributes=event.data.panel.getApplication().attributes;
                                       attributes["stardust:emailOverlay::subject"]=event.data.panel.subjectInput.val();
                                       attributes["carnot:engine:camel::routeEntries"]= event.data.panel.getRoute(attributes,event.data.panel.getApplication().contexts.application.accessPoints);
                                       event.data.panel.view
                                                .submitChanges(
                                                         {
                                                            attributes : attributes
                                                         }, false);
                                    });
                  this.identifierInSubjectInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var attributes=event.data.panel.getApplication().attributes;
                                       attributes["stardust:emailOverlay::includeUniqueIdentifierInSubject"]=event.data.panel.identifierInSubjectInput.prop("checked");
                                       attributes["carnot:engine:camel::routeEntries"]= event.data.panel.getRoute(attributes,event.data.panel.getApplication().contexts.application.accessPoints);
                                       event.data.panel.view
                                                .submitChanges(
                                                         {
                                                            attributes : attributes
                                                         }, false);
                                    });
                  this.storeAttachmentsInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var attributes=event.data.panel.getApplication().attributes;
                                       attributes["stardust:emailOverlay::storeAttachments"]=event.data.panel.storeAttachmentsInput.prop("checked");
                                       attributes["carnot:engine:camel::routeEntries"]= event.data.panel.getRoute(attributes,event.data.panel.getApplication().contexts.application.accessPoints);
                                       event.data.panel.view
                                                .submitChanges(
                                                         {
                                                            attributes : attributes
                                                         }, false);
                                    });
                  this.storeEmailInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var attributes=event.data.panel.getApplication().attributes;
                                       attributes["stardust:emailOverlay::storeEmail"]=event.data.panel.storeEmailInput.prop("checked");
                                       attributes["carnot:engine:camel::routeEntries"]= event.data.panel.getRoute(attributes,event.data.panel.getApplication().contexts.application.accessPoints);
                                       event.data.panel.view
                                                .submitChanges(
                                                         {
                                                            attributes : attributes
                                                         }, false);
                                    });
                  this.templateSourceSelect.change({
                     panel : this
                  }, function(event)
                  {
                     event.data.panel.handleTemplateSourceChangeEvent();
                  });
                  this.templatePathInput.change({
                     panel : this
                  }, function(event)
                  {
                     event.data.panel.handleTemplateSourceChangeEvent();
                  });
                  this.fromInput.change({
                     panel : this
                  }, function(event)
                  {
                     var attributes=event.data.panel.getApplication().attributes;
                     attributes["stardust:emailOverlay::from"]=event.data.panel.fromInput.val();
                     attributes["carnot:engine:camel::routeEntries"]= event.data.panel.getRoute(attributes,event.data.panel.getApplication().contexts.application.accessPoints);
                     event.data.panel.view.submitChanges({
                        attributes : attributes
                     }, false);
                  });
                  this.toInput.change({
                     panel : this
                  }, function(event)
                  {
                     var attributes=event.data.panel.getApplication().attributes;
                     attributes["stardust:emailOverlay::to"]=event.data.panel.toInput.val();
                     attributes["carnot:engine:camel::routeEntries"]= event.data.panel.getRoute(attributes,event.data.panel.getApplication().contexts.application.accessPoints);
                     event.data.panel.view.submitChanges({
                        attributes : attributes
                     }, false);
                  });
                  this.ccInput.change({
                     panel : this
                  }, function(event)
                  {
                     var attributes=event.data.panel.getApplication().attributes;
                     attributes["stardust:emailOverlay::cc"]=event.data.panel.ccInput.val();
                     attributes["carnot:engine:camel::routeEntries"]= event.data.panel.getRoute(attributes,event.data.panel.getApplication().contexts.application.accessPoints);
                     event.data.panel.view.submitChanges({
                        attributes : attributes
                     }, false);
                  });
                  this.bccInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var attributes=event.data.panel.getApplication().attributes;
                                       attributes["stardust:emailOverlay::bcc"]=event.data.panel.bccInput.val();
                                       attributes["carnot:engine:camel::routeEntries"]= event.data.panel.getRoute(attributes,event.data.panel.getApplication().contexts.application.accessPoints);
                                       event.data.panel.view
                                                .submitChanges(
                                                         {
                                                            attributes : attributes
                                                         }, false);
                                    });
                  
                  CKEDITOR.instances[this.mailTemplateEditor.id].on('mode', function(e) {
                     var format="text/plain";
                     if(e.editor.mode=='wysiwyg')
                        format="text/html";
                     var attributes=self.getApplication().attributes;
                     attributes["stardust:emailOverlay::mailFormat"]=format;
                     self.view.submitChanges(
                                {
                                   attributes : attributes
                                }, false);
                 }); 
                  
                  CKEDITOR.instances[this.mailTemplateEditor.id]
                           .on(
                                    'blur',
                                    function(e)
                                    {
                                       var attributes=self.getApplication().attributes;
                                       attributes["stardust:emailOverlay::mailTemplate"]=CKEDITOR.instances[self.mailTemplateEditor.id].getData();
                                       attributes["carnot:engine:camel::routeEntries"]= self.getRoute(attributes,self.getApplication().contexts.application.accessPoints);
                                       self.view
                                                .submitChanges(
                                                         {
                                                            attributes : attributes
                                                         }, false);
                                    });

                  CKEDITOR.instances[this.mailTemplateEditor.id]
                           .on(
                                    'instanceReady',
                                    function(e)
                                    {
                                       e.editor
                                                .setData(self.getApplication().attributes["stardust:emailOverlay::mailTemplate"]);
                                       // diasble the content editor when the model is
                                       // read only
                                       if (self.getModelElement()
                                                && self.getModelElement().isReadonly())
                                       {
                                          e.editor.setReadOnly(true);
                                       }
                                       else
                                       {
                                          e.editor.setReadOnly(false);
                                       }
                                    });
               }
               /**
                * 
                */
               MailIntegrationOverlay.prototype.initializePropertiesTab = function(view)
               {
                  view
                           .insertPropertiesTab(
                                    "mailIntegrationOverlay",
                                    "parameters",
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.parameters.title"),
                                    "plugins/bpm-modeler/images/icons/database_link.png");
               };
               /**
                * 
                */
               MailIntegrationOverlay.prototype.loadLabels = function()
               {
                  this.mailTemplateEditor = m_utils.jQuerySelect(
                           "#mailIntegrationOverlay #mailTemplateEditor").get(0);
                  this.scriptCodeHeading = m_utils
                           .jQuerySelect("#mailIntegrationOverlay #scriptCodeHeading");
                  this.serverInput = m_utils
                           .jQuerySelect("#mailIntegrationOverlay #serverInput");
                  this.userInput = m_utils
                           .jQuerySelect("#mailIntegrationOverlay #userInput");
                  this.passwordInput = m_utils
                           .jQuerySelect("#mailIntegrationOverlay #passwordInput");
                  this.mailFormatSelect = m_utils
                           .jQuerySelect("#mailIntegrationOverlay #mailFormatSelect");
                  this.protocolSelect = m_utils
                           .jQuerySelect("#mailIntegrationOverlay #protocolSelect");
                  this.subjectInput = m_utils
                           .jQuerySelect("#mailIntegrationOverlay #subjectInput");
                  this.toInput = m_utils.jQuerySelect("#mailIntegrationOverlay #toInput");
                  this.fromInput = m_utils
                           .jQuerySelect("#mailIntegrationOverlay #fromInput");
                  this.ccInput = m_utils.jQuerySelect("#mailIntegrationOverlay #ccInput");
                  this.bccInput = m_utils
                           .jQuerySelect("#mailIntegrationOverlay #bccInput");
                  this.identifierInSubjectInput = m_utils
                           .jQuerySelect("#mailIntegrationOverlay #identifierInSubjectInput");
                  this.storeEmailInput = m_utils
                           .jQuerySelect("#mailIntegrationOverlay #storeEmailInput");
                  this.storeAttachmentsInput = m_utils
                           .jQuerySelect("#mailIntegrationOverlay #storeAttachmentsInput");
                  this.autoStartupRow = m_utils
                  .jQuerySelect("#mailIntegrationOverlay #autoStartupRow");
                  this.autoStartupInput = m_utils
                           .jQuerySelect("#mailIntegrationOverlay #autoStartupInput");
                  this.templateSourceSelect = m_utils
                           .jQuerySelect("#mailIntegrationOverlay #templateSourceSelect");
                  this.templatePathInput = m_utils
                           .jQuerySelect("#mailIntegrationOverlay #templatePathInput");
                  this.inputBodyAccessPointInput = m_utils
                           .jQuerySelect("#parametersTab #inputBodyAccessPointInput");
                  this.outputBodyAccessPointInput = m_utils
                           .jQuerySelect("#parametersTab #outputBodyAccessPointInput");
                  this.scriptCodeHeading.empty();
                  this.scriptCodeHeading
                           .append(m_i18nUtils
                                    .getProperty("modeler.model.applicationOverlay.email.template.heading"));
                  this.deleteParameterDefinitionButton = m_utils
                           .jQuerySelect("#parametersTab #deleteParameterDefinitionButton");
                  this.deleteParameterDefinitionButton.attr("src", m_urlUtils
                           .getContextName()
                           + "/plugins/bpm-modeler/images/icons/delete.png");
                  this.addParameterDefinitionButton = m_utils
                           .jQuerySelect("#parametersTab #addParameterDefinitionButton");
                  this.addParameterDefinitionButton.attr("src", m_urlUtils
                           .getContextName()
                           + "/plugins/bpm-modeler/images/icons/add.png");
                  this.parameterDefinitionDirectionSelect = m_utils
                           .jQuerySelect("#parametersTab #parameterDefinitionDirectionSelect");
                  this.parameterDefinitionNameInput = m_utils
                           .jQuerySelect("#parametersTab #parameterDefinitionNameInput");
                  this.dataTypeSelect = m_utils
                           .jQuerySelect("#parametersTab #dataTypeSelect");
                  this.documentTypeSelect = m_utils
                           .jQuerySelect("#parametersTab #documentTypeSelect");
                  m_utils
                           .jQuerySelect("label[for='inputDataTextArea']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.test.inputDataTextArea.label"));
                  m_utils
                           .jQuerySelect("label[for='outputDataTextarea']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.test.outputDataTextArea.label"));
                  m_utils
                           .jQuerySelect("#defaultValueHintLabel")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.defaultValueHint.label"));
                  m_utils
                           .jQuerySelect("label[for='serverInput']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.serverInput.label"));
                  m_utils
                           .jQuerySelect("label[for='userInput']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.userInput.label"));
                  m_utils
                           .jQuerySelect("label[for='passwordInput']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.passwordInput.label"));
                  m_utils
                           .jQuerySelect("label[for='mailFormatSelect']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.mailFormatSelect.label"));
                  m_utils
                           .jQuerySelect("#mailFormatSelect option[value='text/plain']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.mailFormatSelect.text.label"));
                  m_utils
                           .jQuerySelect("#mailFormatSelect option[value='text/html']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.mailFormatSelect.html.label"));
                  m_utils
                           .jQuerySelect("label[for='protocolSelect']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.protocolSelect.label"));
                  m_utils
                           .jQuerySelect("#protocolSelect option[value='smtp']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.protocolSelect.smtp.label"));
                  m_utils
                           .jQuerySelect("#protocolSelect option[value='smtps']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.protocolSelect.smtps.label"));
                  m_utils
                           .jQuerySelect("label[for='subjectInput']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.subjectInput.label"));
                  m_utils
                           .jQuerySelect("label[for='toInput']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.toInput.label"));
                  m_utils
                           .jQuerySelect("label[for='fromInput']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.fromInput.label"));
                  m_utils
                           .jQuerySelect("label[for='ccInput']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.ccInput.label"));
                  m_utils
                           .jQuerySelect("label[for='bccInput']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.bccInput.label"));
                  m_utils
                           .jQuerySelect("label[for='storeEmailInput']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.storeEmailInput.label"));
                  m_utils
                           .jQuerySelect("label[for='storeAttachmentsInput']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.storeAttachmentsInput.label"));
                  m_utils
                           .jQuerySelect("label[for='identifierInSubjectInput']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.identifierInSubjectInput.label"));
                  m_utils
                           .jQuerySelect("label[for='templateSourceSelect']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.templateSourceSelect.label"));
                  m_utils
                           .jQuerySelect("#templateSourceSelect option[value='embedded']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.templateSourceSelect.embedded.label"));
                  m_utils
                           .jQuerySelect(
                                    "#templateSourceSelect option[value='repository']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.templateSourceSelect.repository.label"));
                  m_utils
                           .jQuerySelect(
                                    "#templateSourceSelect option[value='classpath']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.templateSourceSelect.classpath.label"));
                  m_utils
                           .jQuerySelect("#templateSourceSelect option[value='data']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.templateSourceSelect.data.label"));
                  m_utils
                           .jQuerySelect("#mailTemplateHeading")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.template.heading"));
                  m_utils
                           .jQuerySelect("label[for='responseTypeSelect']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.responseTypeSelect.label"));
                  m_utils
                           .jQuerySelect("#responseTypeSelect option[value='none']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.responseTypeSelect.none.label"));
                  m_utils
                           .jQuerySelect("#responseTypeSelect option[value='http']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.responseTypeSelect.http.label"));
                  m_utils
                           .jQuerySelect("#responseOptionsTypeHintLabel")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.responseTypeSelect.http.typeHint.label"));
                  m_utils
                           .jQuerySelect("#responseHttpUrlIHintLabel")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.responseTypeSelect.http.urlHint.label"));
               };
               // Toolbar groups configuration.
               MailIntegrationOverlay.prototype.getToolbarConfiguration = function(view)
               {
                  return [ {
                     name : 'clipboard',
                     groups : [ 'clipboard', 'undo' ]
                  }, {
                     name : 'editing',
                     groups : [ 'find', 'selection', 'spellchecker' ]
                  }, {
                     name : 'links'
                  }, {
                     name : 'insert'
                  }, {
                     name : 'forms'
                  }, {
                     name : 'tools'
                  }, {
                     name : 'document',
                     groups : [ 'mode', 'document', 'doctools' ]
                  }, {
                     name : 'others'
                  }, '/', {
                     name : 'basicstyles',
                     groups : [ 'basicstyles', 'cleanup' ]
                  }, {
                     name : 'paragraph',
                     groups : [ 'list', 'indent', 'blocks', 'align', 'bidi' ]
                  }, {
                     name : 'styles'
                  }, {
                     name : 'colors'
                  } ];
               };
               /**************************************************************************************************************/
               MailIntegrationOverlay.prototype.i18nLabels = function(key)
               {
                  return this.i18nValues[key];
               };
               this.i18nValues = initI18nLabels();
               /**
                * 
                */
               function initI18nLabels()
               {
                  var labels = {};
                  labels['templateConfigurations.title'] = m_i18nUtils
                           .getProperty("modeler.model.applicationOverlay.email.attachments.templateConfigurations.title");
                  return labels;
               };
               this.formatOptions = [ {
                  value : "plain",
                  title : "Plain"
               }, {
                  value : "pdf",
                  title : "PDF"
               } ];
               this.templateSourceOptions = [ {
                  value : "embedded",
                  title : "Embedded"
               }, {
                  value : "data",
                  title : "Data"
               }, {
                  value : "DOCUMENT_REQUEST",
                  title : "Document Request"
               } ];
               MailIntegrationOverlay.prototype.getSourceOptions = function(item){
                   var sourceOptions = [ {
                          value : "repository",
                          title : "Document Repository"
                       }, {
                          value : "classpath",
                          title : "Classpath"
                       }];
                   if(item.tSource=="data"){
                   sourceOptions.push({
                          value : "data",
                          title : "Data"
                    });
                   }
                   return sourceOptions;
               };
                
               /**
                * invoked when the user click on add button in attchment tab (add template
                * configuration)
                */
               MailIntegrationOverlay.prototype.addConfiguration = function()
               {
                  this.templateConfigurations.push({
                     "tTemplate" : "true",
                     "tName" : "New" + (this.templateConfigurations.length + 1),
                     "tPath" : "New" + (this.templateConfigurations.length + 1),
                     "tSource" : "repository",
                     "tFormat" : "plain"
                  });
                  var attributes=this.getApplication().attributes;
                  attributes["stardust:emailOverlay::templateConfigurations"]=angular.toJson(this.templateConfigurations);
                  attributes["carnot:engine:camel::routeEntries"]= this.getRoute(attributes,this.getApplication().contexts.application.accessPoints);
                  
                  this.view
                  .submitChanges(
                           {
                              attributes : attributes
                           }, false);
               };
               /**
                * invoked when the user click on delete button in attchment tab (add
                * template configuration)
                */
               MailIntegrationOverlay.prototype.deleteConfiguration = function(index)
               {
                  this.templateConfigurations.splice(index, 1);
                  var attributes=this.getApplication().attributes;
                  attributes["stardust:emailOverlay::templateConfigurations"]=angular.toJson(this.templateConfigurations);
                  attributes["carnot:engine:camel::routeEntries"]= this.getRoute(attributes,this.getApplication().contexts.application.accessPoints);
                  this.view
                  .submitChanges(
                           {
                              attributes : attributes
                           }, false);
               };
               /**
                * saves the changes
                */
               MailIntegrationOverlay.prototype.submitTemplateChanges = function(source,
                        index)
               {
                  var attributes=this.getApplication().attributes;
                  attributes["stardust:emailOverlay::templateConfigurations"]=angular.toJson(this.templateConfigurations);
                  attributes["carnot:engine:camel::routeEntries"]= this.getRoute(attributes,this.getApplication().contexts.application.accessPoints);
                  this.view
                  .submitChanges(
                           {
                              attributes :attributes
                           }, false);
               };
               /**
                * Contains the logic related to Template configuration source change
                * event.
                * 
                */
               MailIntegrationOverlay.prototype.updateTemplateConfTab = function(item)
               {
                  this.hideTemplateErroType();
                  var submitElements = {};
                  var specificAttributes = this.getApplication().attributes;
                  var filteredAccessPoints;
                  var accessPoints = this.getApplication().contexts.application.accessPoints;
                  if (item == "data")
                  {
                     // filter Document Request AP
                     filteredAccessPoints = m_routeDefinitionUtils.filterAccessPoint(
                              accessPoints, "DOCUMENT_REQUEST");
                     specificAttributes["stardust:emailOverlay::attachmentsTemplateSource"] = "data";
                     specificAttributes["stardust:emailOverlay::templateConfigurations"] = null;

                     m_utils.jQuerySelect("#attachmentsTemplateSourceTypeTab").show();
                     m_utils.jQuerySelect("#templateConfigurationTab").hide();
                  }
                  else if (item == "embedded")
                  {
                     // filter Mail Attachments AP
                     filteredAccessPoints = m_routeDefinitionUtils.filterAccessPoint(
                              accessPoints, "mailAttachmentsAP");
                     // filter Document Request AP
                     filteredAccessPoints = m_routeDefinitionUtils.filterAccessPoint(
                              filteredAccessPoints, "DOCUMENT_REQUEST");
                     var templateConfigurations=[];
                     for (var n = 0; n < filteredAccessPoints.length; n++)
                     {
                        var ap = filteredAccessPoints[n];
                        if (ap.direction == m_constants.IN_ACCESS_POINT&& ap.dataType == m_constants.DOCUMENT_DATA_TYPE)
                          {
                            templateConfigurations.push({
                              "tTemplate" : false,
                              "tName" : ap.name,
                              "tPath" : "",
                              "tSource" : "data",
                              "tFormat" : "plain"
                            });
                          }
                     }
                     
                     specificAttributes["stardust:emailOverlay::attachmentsTemplateSource"] = "embedded";
                     specificAttributes["stardust:emailOverlay::attachmentsTemplateSourceType"] = null;
                     specificAttributes["stardust:emailOverlay::templateConfigurations"]=angular.toJson(templateConfigurations);
                     this.hideTemplateErroType();
                     m_utils.jQuerySelect("#templateConfigurationTab").show();
                     m_utils.jQuerySelect("#attachmentsTemplateSourceTypeTab").hide();
                  }
                  else
                  {
                     // filter Mail Attachments AP
                     filteredAccessPoints = m_routeDefinitionUtils.filterAccessPoint(
                              accessPoints, "mailAttachmentsAP");
                     // add Document Request AP
                     var documentRequestAp = m_routeDefinitionUtils.findAccessPoint(
                              filteredAccessPoints, "DOCUMENT_REQUEST");
                     var documentRequestType=m_model.findTypeDeclaration(this.getScopeModel().id + ":"+ "DOCUMENT_REQUEST");
                     if(!documentRequestType){
                        this.view
                        .submitChanges(
                                 {
                                    attributes :{
                                       "stardust:emailOverlay::attachmentsTemplateSource" :  "embedded"
                                       }
                                 }, true);
                        this.view.errorMessagesList.empty();
                        this.view.errorMessages
                        .push("DOCUMENT_REQUEST structure is not available in the current model, please create it.");
                        this.view.showErrorMessages();
                        return;
                     }
                     if (!documentRequestAp)
                     {
                        filteredAccessPoints.push({
                           id : "DOCUMENT_REQUEST",
                           name : "Document Request",
                           dataType : "struct",
                           direction : "IN",
                           structuredDataTypeFullId : this.getScopeModel().id + ":"
                                    + "DOCUMENT_REQUEST",
                           attributes : {
                              "stardust:predefined" : true
                           }
                        });
                     }
                     specificAttributes["stardust:emailOverlay::attachmentsTemplateSource"] = "DOCUMENT_REQUEST";
                     specificAttributes["stardust:emailOverlay::attachmentsTemplateSourceType"] = null;
                     specificAttributes["stardust:emailOverlay::templateConfigurations"] = null;
                     m_utils.jQuerySelect("#attachmentsTemplateSourceTypeTab").hide();
                     m_utils.jQuerySelect("#templateConfigurationTab").hide();
                  }
                  submitElements.attributes = specificAttributes;
                  submitElements.attributes["carnot:engine:camel::routeEntries"] = this.getRoute(submitElements.attributes,filteredAccessPoints);
                  submitElements.contexts = {
                     application : {
                        accessPoints : filteredAccessPoints
                     }
                  };
                  this.view.submitChanges(submitElements, false);
               };
               /**
                * will create a new AP of type DOCUMENT Request
                */
               MailIntegrationOverlay.prototype.addApTemplateConfiguration = function(
                        typeDeclarationFullId)
               {
                  var submitElements = {};
                  var specificAttributes = this.getApplication().attributes;
                  var accessPoints = this.getApplication().contexts.application.accessPoints;
                  var filteredAccessPoints = accessPoints;

                  if (typeDeclarationFullId
                           && typeDeclarationFullId != m_constants.TO_BE_DEFINED)
                  {
                     var requiredTemplatingType = this
                              .getTemplateConfigurationType(typeDeclarationFullId);
                     if (requiredTemplatingType && requiredTemplatingType != undefined)
                     {
                        var templateConfigurationAp = m_routeDefinitionUtils
                                 .findAccessPoint(accessPoints, "mailAttachmentsAP");
                        if (!templateConfigurationAp)
                        {
                           accessPoints.push({
                              id : "mailAttachmentsAP",
                              name : "Mail Attachments",
                              dataType : "struct",
                              direction : "IN",
                              structuredDataTypeFullId : requiredTemplatingType
                                       .getFullId(),
                              attributes : {
                                 "stardust:predefined" : true
                              }
                           });
                           specificAttributes["stardust:emailOverlay::attachmentsTemplateSourceType"] = requiredTemplatingType
                                    .getFullId();
                           this.hideTemplateErroType();
                        }
                     }
                     else
                     {
                        filteredAccessPoints = m_routeDefinitionUtils.filterAccessPoint(
                                 accessPoints, "mailAttachmentsAP");
                        specificAttributes["stardust:emailOverlay::attachmentsTemplateSourceType"] = null;
                        this.showTemplateErroType();
                     }
                  }
                  else
                  {
                     var filteredAccessPoints = m_routeDefinitionUtils.filterAccessPoint(
                              accessPoints, "mailAttachmentsAP");
                     specificAttributes["stardust:emailOverlay::attachmentsTemplateSourceType"] = null;
                     this.showTemplateErroType();
                  }
                  submitElements.attributes = specificAttributes;
                  submitElements.attributes["carnot:engine:camel::routeEntries"] = this.getRoute(submitElements.attributes,filteredAccessPoints);
                  submitElements.contexts = {
                     application : {
                        accessPoints : filteredAccessPoints
                     }
                  };
                  this.view.submitChanges(submitElements, false);
               };
               /**
                * 
                */
               MailIntegrationOverlay.prototype.getTemplateConfigurationType = function(
                        typeDeclarationFullId)
               {
                  var typeDeclaration = this.findTypeDeclaration(
                           this.getScopeModel().typeDeclarations, typeDeclarationFullId);
                  var elements = typeDeclaration.getElements();
                  if (elements.length == 1)
                  {
                     var elt = elements[0];
                     if (elt.type.indexOf(":") != -1)
                     {
                        var childType = elt.type.split(":")[1];
                        var childTypeDeclaration = typeDeclaration.model
                                 .findTypeDeclarationBySchemaName(childType);
                        if (childTypeDeclaration)
                        {
                           if (this
                                    .isTemplateConfigurationStructure(childTypeDeclaration))
                           {
                              return typeDeclaration;
                           }
                        }
                     }
                  }
               };
               /**
                * 
                */
               MailIntegrationOverlay.prototype.isTemplateConfigurationStructure = function(
                        childTypeDeclaration)
               {
                  return this.checkTemplatingStructure(childTypeDeclaration
                           .getElement("tTemplate"), "tTemplate")
                           && this.checkTemplatingStructure(childTypeDeclaration
                                    .getElement("tName"), "tName")
                           && this.checkTemplatingStructure(childTypeDeclaration
                                    .getElement("tPath"), "tPath")
                           && this.checkTemplatingStructure(childTypeDeclaration
                                    .getElement("tFormat"), "tFormat")
                           && this.checkTemplatingStructure(childTypeDeclaration
                                    .getElement("tSource"), "tSource") ? true : false;
               };
               /**
                * 
                */
               MailIntegrationOverlay.prototype.checkTemplatingStructure = function(
                        element, name)
               {
                  var type = element.name == "tTemplate" ? "xsd:boolean" : "xsd:string";
                  return (element != undefined) && (element.name == name)
                           && (element.classifier == "element")
                           && (element.cardinality == "required")
                           && (element.type == type) ? true : false;
               };
               /**
                * 
                */
               MailIntegrationOverlay.prototype.findTypeDeclaration = function(
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
               };
               /**
                * Shows error message when the selected data type doesn't fit into the document request structure.
                */
               MailIntegrationOverlay.prototype.showTemplateErroType = function()
               {
                  var typeErrorMessages = m_utils
                           .jQuerySelect("#typeErrorMessagesTab #typeErrorMessages");
                  typeErrorMessages.empty();
                  typeErrorMessages
                           .append("<table cellpadding=\"0\" border=\"1\" cellspacing=\"0\" class=\"layoutTable\" style=\"width: 100%;color: #708090;background: #F2F2F2;\">"
                                    + "<tr>"
                                    + "<td style=\"font-weight: bold; color: #000000 \">"
                                    + "Name"
                                    + "</td>"
                                    + "<td style=\"font-weight: bold; color: #000000 \">"
                                    + "Type"
                                    + "</td>"
                                    + "<td style=\"font-weight: bold; color: #000000 \">"
                                    + "Cardinality"
                                    + "</td>"
                                    + "</tr>"
                                    + "<tr>"
                                    + "<td>"
                                    + "tTemplate"
                                    + "</td>"
                                    + "<td>"
                                    + "Boolean"
                                    + "</td>"
                                    + "<td>"
                                    + "Exactly One"
                                    + "</td>"
                                    + "</tr>"
                                    + "<tr>"
                                    + "<td>"
                                    + "tName"
                                    + "</td>"
                                    + "<td>"
                                    + "Text"
                                    + "</td>"
                                    + "<td>"
                                    + "Exactly One"
                                    + "</td>"
                                    + "</tr>"
                                    + "<tr>"
                                    + "<td>"
                                    + "tPath"
                                    + "</td>"
                                    + "<td>"
                                    + "Text"
                                    + "</td>"
                                    + "<td>"
                                    + "Exactly One"
                                    + "</td>"
                                    + "</tr>"
                                    + "<tr>"
                                    + "<td>"
                                    + "tFormat"
                                    + "</td>"
                                    + "<td>"
                                    + "Text"
                                    + "</td>"
                                    + "<td>"
                                    + "Exactly One"
                                    + "</td>"
                                    + "</tr>"
                                    + "<tr>"
                                    + "<td>"
                                    + "tSource"
                                    + "</td>"
                                    + "<td>"
                                    + "Text"
                                    + "</td>"
                                    + "<td>"
                                    + "Exactly One"
                                    + "</td>"
                                    + "</tr>" + "</table>");
                  m_utils.jQuerySelect("#typeErrorMessagesTab").show();
               };
               /**
                * 
                */
               MailIntegrationOverlay.prototype.hideTemplateErroType = function()
               {
                  m_utils.jQuerySelect("#typeErrorMessagesTab").hide();
               };
               MailIntegrationOverlay.prototype.isIntegrator = function(){
                  return m_user.getCurrentRole() == m_constants.INTEGRATOR_ROLE;
               }
            }
         });