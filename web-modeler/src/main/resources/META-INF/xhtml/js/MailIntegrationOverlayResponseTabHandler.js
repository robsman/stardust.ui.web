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
                  "bpm-modeler/js/MailIntegrationOverlayTestTabHandler" ],
         function(m_utils, m_i18nUtils, m_constants, m_urlUtils, m_commandsController,
                  m_command, m_model, m_accessPoint, m_typeDeclaration,
                  m_parameterDefinitionsPanel, m_codeEditorAce, m_modelElementUtils,
                  m_routeDefinitionUtils, m_mailRouteDefinitionHandler,
                  m_angularContextUtils, mailIntegrationOverlayTestTabHandler)
         {
            var overlay = new MailIntegrationOverlayResponseTabHandler();
            return {
               initialize : function(base)
               {
                  return overlay.initialize(base);
               },
               update : function()
               {
                  return overlay.update();
               },
               setResponseType : function(responseType)
               {
                  return overlay.setResponseType(responseType);
               },
               disableResponseTypeSelect : function()
               {
                  return overlay.disableResponseTypeSelect();
               },
               enableResponseTypeSelect : function()
               {
                  return overlay.enableResponseTypeSelect();
               },
               getResponseTypeSelect : function()
               {
                  return overlay.getResponseTypeSelect();
               },
               getResponseOptionsTypeSelect : function()
               {
                  return overlay.getResponseOptionsTypeSelect();
               },
               getResponseHttpUrlInput : function()
               {
                  return overlay.getResponseHttpUrlInput();
               }
            };
            function MailIntegrationOverlayResponseTabHandler()
            {
               MailIntegrationOverlayResponseTabHandler.prototype.initialize = function(
                        base)
               {
                  this.base = base;
                  this.initializePropertiesTab(this.view);
                  this.loadLabels();
                  this.registerResponseTabEvents();
               };
               /**
                * Constructs the test tab.
                */
               MailIntegrationOverlayResponseTabHandler.prototype.initializePropertiesTab = function(
                        view)
               {
                  this.base.view
                           .insertPropertiesTab(
                                    "mailIntegrationOverlay",
                                    "response",
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.response.title"),
                                    "plugins/bpm-modeler/images/icons/email.png");
               };
               /**
                * loads Test Tab components
                */
               MailIntegrationOverlayResponseTabHandler.prototype.loadLabels = function()
               {
                  this.responseTypeSelect = m_utils.jQuerySelect("#responseTab #responseTypeSelect");
                  this.responseOptionsTypeSelect = m_utils.jQuerySelect("#responseTab #responseOptionsTypeSelect");
                  this.responseHttpUrlInput = m_utils.jQuerySelect("#responseTab #responseHttpUrlInput");
               };
               /**
                * Register events for Test tab components
                */
               MailIntegrationOverlayResponseTabHandler.prototype.update = function()
               {
                  this.populateResponseOptionsTypeSelect();
                  this.setResponseType(this.base.getApplication().attributes["stardust:emailOverlay::responseType"]);
                  this.responseOptionsTypeSelect.val(this.base.getApplication().attributes["stardust:emailOverlay::responseOptionType"]);
                  this.responseHttpUrlInput.val(this.base.getApplication().attributes["stardust:emailOverlay::responseHttpUrl"]);
               };
               /**
                * Register events for Test tab components
                */
               MailIntegrationOverlayResponseTabHandler.prototype.registerResponseTabEvents = function()
               {
                  this.responseTypeSelect
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var submitElements={};
                                       var attributes=event.data.panel.base.getApplication().attributes;
                                       var accessPoints = event.data.panel.base.getApplication().contexts.application.accessPoints;
                                       var applicationTypeChanges = null;
                                       var invocationPatternChanges = null;
                                       var invocationTypeChanges = null;
                                       var responseTypeChanges = null;
                                       if (event.data.panel.responseTypeSelect.val() === "none")
                                       {
                                          applicationTypeChanges = "camelSpringProducerApplication";
                                          invocationPatternChanges = "send";
                                          invocationTypeChanges = "synchronous";
                                          accessPoints = m_routeDefinitionUtils.filterAccessPoint(accessPoints,"returnValue");
                                          attributes["stardust:emailOverlay::responseOptionType"]=null;
                                          attributes["stardust:emailOverlay::responseHttpUrl"]=null;
                                       }
                                       else
                                       {
                                          applicationTypeChanges = "camelConsumerApplication";
                                          invocationPatternChanges = "sendReceive";
                                          invocationTypeChanges = "asynchronous";
                                          responseTypeChanges = event.data.panel.responseTypeSelect.val();
                                          var ap = m_routeDefinitionUtils.findAccessPoint(accessPoints,"returnValue");
                                          if (!ap)
                                          {
                                             accessPoints.push({
                                                id : "returnValue",
                                                name : "returnValue",
                                                dataType : "primitive",
                                                primitiveDataType : "String",
                                                direction : "OUT",
                                                attributes : {
                                                   "stardust:predefined" : true
                                                }
                                             });
                                          }
                                       }
                                       
                                       submitElements.type = applicationTypeChanges;
                                       if(!attributes["carnot:engine:camel::invocationPattern"])
                                          attributes["carnot:engine:camel::invocationPattern"]=invocationPatternChanges;
                                       if(!attributes["carnot:engine:camel::invocationType"])
                                          attributes["carnot:engine:camel::invocationType"]=invocationTypeChanges;
                                       attributes["stardust:emailOverlay::responseType"]=responseTypeChanges;
                                       attributes["carnot:engine:camel::routeEntries"]= event.data.panel.base.getRoute(attributes,event.data.panel.base.getApplication().contexts.application.accessPoints);
                                       
                                       submitElements.attributes=attributes;
                                       submitElements.contexts={
                                          application : {
                                            accessPoints : accessPoints
                                          }
                                       };
                                       
                                       event.data.panel.base.view.submitChanges(submitElements, false);
                                       event.data.panel.setResponseType(event.data.panel.responseTypeSelect.val());
                                    });
                  this.responseOptionsTypeSelect
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var attributes=event.data.panel.base.getApplication().attributes;
                                       attributes["stardust:emailOverlay::responseOptionType"]=event.data.panel.responseOptionsTypeSelect.val();
                                       attributes["carnot:engine:camel::routeEntries"]= event.data.panel.base.getRoute(attributes,event.data.panel.base.getApplication().contexts.application.accessPoints);
                                       event.data.panel.base.view
                                                .submitChanges(
                                                         {
                                                            attributes : attributes
                                                         }, false);
                                    });
                  this.responseHttpUrlInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var attributes=event.data.panel.base.getApplication().attributes;
                                       attributes["stardust:emailOverlay::responseHttpUrl"]=event.data.panel.responseHttpUrlInput.val();
                                       attributes["carnot:engine:camel::routeEntries"]= event.data.panel.base.getRoute(attributes,event.data.panel.base.getApplication().contexts.application.accessPoints);
                                       event.data.panel.base.view
                                                .submitChanges(
                                                         {
                                                            attributes : attributes
                                                         }, false);
                                    });
               };
               /**
                * Popolation ResponseOptionsTypeSelect with the available type declaration.
                */
               MailIntegrationOverlayResponseTabHandler.prototype.populateResponseOptionsTypeSelect = function()
               {
                  this.responseOptionsTypeSelect.empty();
                  this.responseOptionsTypeSelect.append("<option value='"+ m_constants.TO_BE_DEFINED + "'>"+ m_i18nUtils.getProperty("modeler.general.toBeDefined")+ "</option>");
                  if (this.base.getScopeModel())
                  {
                     this.responseOptionsTypeSelect.append("<optgroup label='"+ m_i18nUtils.getProperty("modeler.general.thisModel")+ "'>");
                     for ( var i in this.base.getScopeModel().typeDeclarations)
                     {
                        var currentTypeDeclaration=this.base.getScopeModel().typeDeclarations[i];
                        if (!currentTypeDeclaration.isSequence())
                        {
                           this.responseOptionsTypeSelect.append("<option value='"
                                    + currentTypeDeclaration.getFullId() + "'>"
                                    + currentTypeDeclaration.name
                                    + "</option>");
                        }
                     }
                     
                     this.responseOptionsTypeSelect.append("</optgroup><optgroup label='"+ m_i18nUtils.getProperty("modeler.general.otherModels")+ "'>");
                     
                     for ( var n in m_model.getModels())
                     {
                        var currentModel=m_model.getModels()[n];
                        if (this.base.getScopeModel()&& currentModel == this.base.getScopeModel())
                        {
                           continue;
                        }
                        for ( var m in currentModel.typeDeclarations)
                        {
                           var currentTypeDeclaration=currentModel.typeDeclarations[m];
                           
                           if (m_modelElementUtils.hasPublicVisibility(currentTypeDeclaration))
                           {
                              if (!currentTypeDeclaration.isSequence())
                              {
                                 this.responseOptionsTypeSelect.append("<option value='"+ currentTypeDeclaration.getFullId()+ "'>");
                                 this.responseOptionsTypeSelect.append(currentModel.name+ "/"+ currentTypeDeclaration.name+ "</option>");
                              }
                           }
                        }
                     }
                     this.responseOptionsTypeSelect.append("</optgroup>");
                  }
               };
               /**
                * 
                */
               MailIntegrationOverlayResponseTabHandler.prototype.setResponseType = function(
                        responseType)
               {
                  if (!responseType)
                  {
                     responseType = "none";
                  }
                  this.responseTypeSelect.val(responseType);
                  m_utils.jQuerySelect("#emailResponseDiv").hide();
                  m_utils.jQuerySelect("#httpResponseDiv").hide();
                  m_utils.jQuerySelect("#parameterDefinitionDirectionOutOption").hide();
                  if (responseType === "http")
                  {
                     m_utils.jQuerySelect("#httpResponseDiv").show();
                  }
                  else if (responseType === "eMail")
                  {
                     m_utils.jQuerySelect("#emailResponseDiv").show();
                  }
               };
               /**
                * 
                */
               MailIntegrationOverlayResponseTabHandler.prototype.disableResponseTypeSelect = function()
               {
                  this.responseTypeSelect.prop('disabled', true);
               };
               /**
                * 
                */
               MailIntegrationOverlayResponseTabHandler.prototype.enableResponseTypeSelect = function(
                        responseType)
               {
                  this.responseTypeSelect.prop('disabled', false);
               };
               /**
                * 
                */
               MailIntegrationOverlayResponseTabHandler.prototype.getResponseTypeSelect = function()
               {
                  return this.responseTypeSelect.val();
               };
               /**
                * 
                */
               MailIntegrationOverlayResponseTabHandler.prototype.getResponseOptionsTypeSelect = function()
               {
                  return this.responseOptionsTypeSelect.val();
               };
               /**
                * 
                */
               MailIntegrationOverlayResponseTabHandler.prototype.getResponseHttpUrlInput = function()
               {
                  return this.responseHttpUrlInput.val();
               };
            }
         });