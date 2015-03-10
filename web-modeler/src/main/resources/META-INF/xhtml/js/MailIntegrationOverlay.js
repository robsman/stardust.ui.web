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
                  "bpm-modeler/js/m_angularContextUtils"],
         function(m_utils, m_i18nUtils, m_constants, m_urlUtils, m_commandsController,
                  m_command, m_model, m_accessPoint, m_typeDeclaration,
                  m_parameterDefinitionsPanel, m_codeEditorAce, m_modelElementUtils,
                  m_routeDefinitionUtils, m_mailRouteDefinitionHandler, m_angularContextUtils)
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
               }
               MailIntegrationOverlay.prototype.initialize = function(view)
               {
                  this.view = view;
                  this.view
                           .insertPropertiesTab(
                                    "mailIntegrationOverlay",
                                    "parameters",
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.parameters.title"),
                                    "plugins/bpm-modeler/images/icons/database_link.png");
                  this.view
                           .insertPropertiesTab(
                                    "mailIntegrationOverlay",
                                    "response",
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.response.title"),
                                    "plugins/bpm-modeler/images/icons/email.png");
                  this.view
                           .insertPropertiesTab(
                                    "mailIntegrationOverlay",
                                    "test",
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.test.title"),
                                    "plugins/bpm-modeler/images/icons/application-run.png");
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
                  this.transactedRouteInput = m_utils
                           .jQuerySelect("#mailIntegrationOverlay #transactedRouteInput");
                  this.autoStartupInput = m_utils
                           .jQuerySelect("#mailIntegrationOverlay #autoStartupInput");
                  this.templateSourceSelect = m_utils
                           .jQuerySelect("#mailIntegrationOverlay #templateSourceSelect");
                  this.templatePathInput = m_utils
                           .jQuerySelect("#mailIntegrationOverlay #templatePathInput");
                  this.mailTemplateEditor = m_utils.jQuerySelect(
                           "#mailIntegrationOverlay #mailTemplateEditor").get(0);
                  var rdmNo = Math.floor((Math.random() * 100000) + 1);
                  this.mailTemplateEditor.id = "mailTemplateEditor" + rdmNo;
                  this.responseTypeSelect = m_utils
                           .jQuerySelect("#responseTab #responseTypeSelect");
                  this.responseOptionsTypeSelect = m_utils
                           .jQuerySelect("#responseTab #responseOptionsTypeSelect");
                  this.responseHttpUrlInput = m_utils
                           .jQuerySelect("#responseTab #responseHttpUrlInput");
                  CKEDITOR.replace(this.mailTemplateEditor.id, {
                     toolbarGroups : this.getToolbarConfiguration()
                  });
                  this.resetButton = m_utils.jQuerySelect("#testTab #resetButton");
                  this.runButton = m_utils.jQuerySelect("#testTab #runButton");
                  this.inputDataTextarea = m_utils
                           .jQuerySelect("#testTab #inputDataTextarea");
                  this.outputDataTextarea = m_utils
                           .jQuerySelect("#testTab #outputDataTextarea");
                  this.inputBodyAccessPointInput = m_utils
                           .jQuerySelect("#parametersTab #inputBodyAccessPointInput");
                  this.outputBodyAccessPointInput = m_utils
                           .jQuerySelect("#parametersTab #outputBodyAccessPointInput");
                  this.scriptCodeHeading.empty();
                  this.scriptCodeHeading
                           .append(m_i18nUtils
                                    .getProperty("modeler.model.applicationOverlay.email.template.heading"));
                  this.resetButton
                           .prop(
                                    "title",
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.test.resetButton.title"));
                  this.runButton
                           .prop(
                                    "title",
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.email.test.runButton.title"));
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
                  this.deleteParameterDefinitionButton = m_utils
                           .jQuerySelect("#parametersTab #deleteParameterDefinitionButton");
                  this.deleteParameterDefinitionButton.attr("src", m_urlUtils
                           .getContextName()
                           + "/plugins/bpm-modeler/images/icons/delete.png");
                  this.parameterDefinitionDirectionSelect = m_utils.jQuerySelect("#parametersTab #parameterDefinitionDirectionSelect");
                  this.parameterDefinitionNameInput = m_utils.jQuerySelect("#parametersTab #parameterDefinitionNameInput");
                  this.dataTypeSelect = m_utils.jQuerySelect("#parametersTab #dataTypeSelect");
                  this.parameterDefinitionDirectionSelect
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event) 
                                    {
                                       var ap = event.data.panel.parameterDefinitionsPanel.currentParameterDefinition;
                                       if (event.data.panel.parameterDefinitionDirectionSelect.val() == m_constants.IN_ACCESS_POINT
                                                && ap.dataType == m_constants.DOCUMENT_DATA_TYPE)
                                       {
                                          // add template conf
                                          event.data.panel.addTemplateConfigurationForDocumentAp(ap);
                                          
                                       } else if (event.data.panel.parameterDefinitionDirectionSelect.val() == m_constants.OUT_ACCESS_POINT
                                                      && ap.dataType == m_constants.DOCUMENT_DATA_TYPE)
                                       {
                                          // delete template conf
                                          event.data.panel.deleteTemplateConfigurationForDocumentAp(ap);
                                       }
                                    });
                  this.parameterDefinitionNameInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event) 
                                    {
                                       var ap = event.data.panel.parameterDefinitionsPanel.currentParameterDefinition;
                                       if (ap.direction == m_constants.IN_ACCESS_POINT
                                                && ap.dataType == m_constants.DOCUMENT_DATA_TYPE)
                                       {
                                          // update element name in Template configuration
                                          event.data.panel.updateTemplateConfigurationForDocumentAp(ap, 
                                                   event.data.panel.parameterDefinitionNameInput.val());
                                       }
                                    });
                  this.dataTypeSelect
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event) 
                                    {
                                       var ap = event.data.panel.parameterDefinitionsPanel.currentParameterDefinition;
                                       if (ap.direction == m_constants.IN_ACCESS_POINT)
                                       {
                                          if(event.data.panel.dataTypeSelect.val() == m_constants.DOCUMENT_DATA_TYPE)
                                          {
                                             // add template conf
                                             event.data.panel.addTemplateConfigurationForDocumentAp(ap);
                                             
                                          } else if(ap.dataType == m_constants.DOCUMENT_DATA_TYPE)
                                          {
                                             // delete template conf
                                             event.data.panel.deleteTemplateConfigurationForDocumentAp(ap);
                                          }
                                       }
                                    });
                  
                  this.deleteParameterDefinitionButton
                           .click(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var ap = event.data.panel.parameterDefinitionsPanel.currentParameterDefinition;
                                       if (ap.direction == m_constants.IN_ACCESS_POINT
                                                && ap.dataType == m_constants.DOCUMENT_DATA_TYPE)
                                       {
                                          // delete template conf
                                          event.data.panel.deleteTemplateConfigurationForDocumentAp(ap);
                                       }
                                       event.data.panel.parameterDefinitionsPanel
                                                .deleteParameterDefinition();
                                       event.data.panel.getApplication().contexts.application.accessPoints = event.data.panel.parameterDefinitionsPanel.parameterDefinitions;
                                       event.data.panel.submitChanges();
                                    });
                  var self = this;
                  this.parameterDefinitionNameInput = m_utils.jQuerySelect("#parametersTab #parameterDefinitionNameInput");
				  
				  this.parameterDefinitionNameInput.change(function()
                  {
                     self.submitChanges();
                  });
                  this.transactedRouteInput.change(function()
                  {
                     self.submitChanges();
                  });
                  this.autoStartupInput.change(function()
                           {
                              self.submitChanges();
                           });
                  this.serverInput.change(function()
                  {
                     self.submitChanges();
                  });
                  this.userInput.change(function()
                  {
                     self.submitChanges();
                  });
                  this.passwordInput.change(function()
                  {
                     self.submitChanges();
                  });
                  this.mailFormatSelect.change(function()
                  {
                     self.submitChanges();
                  });
                  this.protocolSelect.change(function()
                  {
                     self.submitChanges();
                  });
                  this.subjectInput.change(function()
                  {
                     self.submitChanges();
                  });
                  this.identifierInSubjectInput.change(function()
                  {
                     self.submitChanges();
                  });
                  this.storeAttachmentsInput.change(function()
                  {
                     self.submitChanges();
                  });
                  this.storeEmailInput.change(function()
                  {
                     self.submitChanges();
                  });
                  this.templateSourceSelect
                           .change(function()
                           {
                              self.setTemplateSource(self.templateSourceSelect.val());
                              self.submitChanges(true);
                              if (self.templateSourceSelect.val() == "data")
                              {// create MailContent AP
                                 var accessPoints = self.getApplication().contexts.application.accessPoints;
                                 var mailContentAp = m_routeDefinitionUtils
                                          .findAccessPoint(accessPoints, "mailContentAP");
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
                                    self.submitParameterDefinitionsChanges(accessPoints);
                                 }
                                 ;
                              }
                              else
                              {
                                 var accessPoints = self.getApplication().contexts.application.accessPoints;
                                 var filteredAccessPoints = m_routeDefinitionUtils
                                          .filterAccessPoint(accessPoints,
                                                   "mailContentAP");
                                 self
                                          .submitParameterDefinitionsChanges(filteredAccessPoints);
                              }
                           });
                  this.templatePathInput.change(function()
                  {
                     self.submitChanges();
                  });
                  this.fromInput.change(function()
                  {
                     self.submitChanges();
                  });
                  this.toInput.change(function()
                  {
                     self.submitChanges();
                  });
                  this.ccInput.change(function()
                  {
                     self.submitChanges();
                  });
                  this.bccInput.change(function()
                  {
                     self.submitChanges();
                  });
                  this.responseOptionsTypeSelect.change(function()
                  {
                     self.submitChanges();
                  });
                  this.responseTypeSelect.change(function()
                  {
                     self.setResponseType(self.responseTypeSelect.val());
                     self.submitChanges();
                  });
                  this.responseHttpUrlInput.change(function()
                  {
                     self.submitChanges();
                  });
                  CKEDITOR.instances[this.mailTemplateEditor.id].on('blur', function(e)
                  {
                     self.submitChanges();
                  });
                  this.parameterDefinitionsPanel = m_parameterDefinitionsPanel.create({
                     scope : "parametersTab",
                     submitHandler : this,
                     supportsOrdering : false,
                     supportsDataMappings : false,
                     supportsDescriptors : false,
                     supportsDataTypeSelection : true,
                     supportsDocumentTypes : true
                  });
                  this.populateResponseOptionsTypeSelect();
                  this.runButton
                           .click(function()
                           {
                              var output = "var input = ";
                              output += self.inputDataTextarea.val();
                              var inputDataTextareaObj = eval("(function(){return "
                                       + self.inputDataTextarea.val() + ";})()");
                              var markup;
                              if ((inputDataTextareaObj.mailContentAP != undefined)
                                       && (inputDataTextareaObj.mailContentAP != null)
                                       && (inputDataTextareaObj.mailContentAP != ""))
                              {
                                 markup = "{{mailContentAP}}";
                              }
                              else
                              {
                                 markup = CKEDITOR.instances[self.mailTemplateEditor.id]
                                          .getData();
                                 if (self.responseTypeSelect != "none")
                                 {
                                    markup += self.createResponseOptionString();
                                 }
                              }
                              output += "; \""
                                       + markup.replace(new RegExp("\"", 'g'), "'")
                                                .replace(new RegExp("\n", 'g'), " ")
                                                .replace(new RegExp("{{", 'g'),
                                                         "\" + input.").replace(
                                                         new RegExp("}}", 'g'), " + \"")
                                       + "\"";
                              self.outputDataTextarea.empty();
                              self.outputDataTextarea.append(eval(output));
                           });
                  this.resetButton.click(function()
                  {
                     self.inputDataTextarea.empty();
                     self.outputDataTextarea.empty();
                     self.inputDataTextarea.append(self.createParameterObjectString(
                              m_constants.IN_ACCESS_POINT, true));
                  });
                  if (this.getModelElement() && this.getModelElement().isReadonly())
                  {
                     CKEDITOR.instances[this.mailTemplateEditor.id].config.readOnly = true;
                  }
                  CKEDITOR.instances[this.mailTemplateEditor.id].setData(this.getApplication().attributes["stardust:emailOverlay::mailTemplate"]);
                  if(this.getApplication().attributes["carnot:engine:camel::autoStartup"]==null||this.getApplication().attributes["carnot:engine:camel::autoStartup"]===undefined){
                     this.view.submitModelElementAttributeChange("carnot:engine:camel::autoStartup", true);
                   }
               };
               MailIntegrationOverlay.prototype.populateResponseOptionsTypeSelect = function()
               {
                  this.responseOptionsTypeSelect.empty();
                  this.responseOptionsTypeSelect.append("<option value='"
                           + m_constants.TO_BE_DEFINED + "'>"
                           + m_i18nUtils.getProperty("modeler.general.toBeDefined")
                           + "</option>");
                  if (this.getScopeModel())
                  {
                     this.responseOptionsTypeSelect.append("<optgroup label='"
                              + m_i18nUtils.getProperty("modeler.general.thisModel")
                              + "'>");
                     for ( var i in this.getScopeModel().typeDeclarations)
                     {
                        if (!this.getScopeModel().typeDeclarations[i].isSequence())
                        {
                           this.responseOptionsTypeSelect.append("<option value='"
                                    + this.getScopeModel().typeDeclarations[i]
                                             .getFullId() + "'>"
                                    + this.getScopeModel().typeDeclarations[i].name
                                    + "</option>");
                        }
                     }
                     this.responseOptionsTypeSelect.append("</optgroup><optgroup label='"
                              + m_i18nUtils.getProperty("modeler.general.otherModels")
                              + "'>");
                     for ( var n in m_model.getModels())
                     {
                        if (this.getScopeModel()
                                 && m_model.getModels()[n] == this.getScopeModel())
                        {
                           continue;
                        }
                        for ( var m in m_model.getModels()[n].typeDeclarations)
                        {
                           if (m_modelElementUtils.hasPublicVisibility(m_model
                                    .getModels()[n].typeDeclarations[m]))
                           {
                              if (!m_model.getModels()[n].typeDeclarations[m]
                                       .isSequence())
                              {
                                 this.responseOptionsTypeSelect
                                          .append("<option value='"
                                                   + m_model.getModels()[n].typeDeclarations[m]
                                                            .getFullId()
                                                   + "'>"
                                                   + m_model.getModels()[n].name
                                                   + "/"
                                                   + m_model.getModels()[n].typeDeclarations[m].name
                                                   + "</option>");
                              }
                           }
                        }
                     }
                     this.responseOptionsTypeSelect.append("</optgroup>");
                  }
               };
               MailIntegrationOverlay.prototype.setResponseType = function(responseType)
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
                  this.responseTypeSelect.prop('disabled', true);

                  if (templateSource === "embedded")
                  {
                     m_utils.jQuerySelect("#embeddedTemplateSource").show();
                     this.templatePathInput.val("");
                     this.identifierInSubjectInput.prop('disabled', false);
                     this.responseTypeSelect.prop('disabled', false);
                  }
                  else if (templateSource === "data")
                  {
                     if (this.identifierInSubjectInput.prop('checked'))
                        this.identifierInSubjectInput.prop('checked', false);
                     this.setResponseType("none");
                  }
                  else
                  {
                     if (this.identifierInSubjectInput.prop('checked'))
                        this.identifierInSubjectInput.prop('checked', false);
                     this.setResponseType("none");
                     m_utils.jQuerySelect("#externalTemplateSource").show();
                     CKEDITOR.instances[this.mailTemplateEditor.id].setData("");
                  }
               };
               /**
                *
                */
               MailIntegrationOverlay.prototype.createParameterObjectString = function(
                        direction, initializePrimitives)
               {
                  var otherDirection;
                  if (direction === m_constants.IN_ACCESS_POINT)
                  {
                     otherDirection = m_constants.OUT_ACCESS_POINT;
                  }
                  else
                  {
                     otherDirection = m_constants.IN_ACCESS_POINT;
                  }
                  var parameterObjectString = "{";
                  var index = 0;
                  for (var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n)
                  {
                     var parameterDefinition = this.getApplication().contexts.application.accessPoints[n];
                     if (parameterDefinition.direction == otherDirection)
                     {
                        continue;
                     }
                     if (index > 0)
                     {
                        if (parameterDefinition.dataType != "dmsDocument")
                        {
                           parameterObjectString += ", ";
                        }
                     }
                     ++index;
                     if (parameterDefinition.dataType == "primitive")
                     {
                        if (initializePrimitives)
                        {
                           parameterObjectString += parameterDefinition.id;
                           if (parameterDefinition.primitiveDataType === "String")
                           {
                              parameterObjectString += ": \"\"";
                           }
                           else if (parameterDefinition.primitiveDataType === "Boolean")
                           {
                              parameterObjectString += ": false";
                           }
                           else
                           {
                              parameterObjectString += ": 0";
                           }
                        }
                     }
                     else if (parameterDefinition.dataType == "struct")
                     {
                        var typeDeclaration = m_model
                                 .findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);
                        parameterObjectString += parameterDefinition.id;
                        parameterObjectString += ": ";
                        parameterObjectString += JSON.stringify(typeDeclaration
                                 .createInstance(), null, 3);
                     }
                  }
                  parameterObjectString += "}";
                  return parameterObjectString;
               };
               /**
                *
                */
               MailIntegrationOverlay.prototype.createResponseOptionString = function()
               {
                  if (this.responseOptionsTypeSelect.val() != null
                           && this.responseOptionsTypeSelect.val() != m_constants.TO_BE_DEFINED)
                  {
                     var typeDeclaration = m_model
                              .findTypeDeclaration(this.responseOptionsTypeSelect.val());
                     var optionMarkup = "<hr><p>Select one of the following options:</p><ul>";
                     for (var i = 0; i < typeDeclaration.getFacets().length; ++i)
                     {
                        if ((typeDeclaration.getFacets()[i].classifier != "maxLength")
                                 && (typeDeclaration.getFacets()[i].classifier != "minLength"))
                        {
                           var option = typeDeclaration.getFacets()[i];
                           var hashCodeJS = "(";
                           hashCodeJS += "processInstanceOid + '|' + ";
                           hashCodeJS += "activityInstanceOid + '|' + ";
                           hashCodeJS += "partition + '|false|";
                           hashCodeJS += option.name;
                           hashCodeJS += "').hashCode()";
                           optionMarkup += "<li><a href=&quot;";
                           optionMarkup += this.responseHttpUrlInput.val();
                           optionMarkup += "/mail-confirmation";
                           optionMarkup += "?activityInstanceOID=' + activityInstanceOid + '";
                           optionMarkup += "&amp;processInstanceOID=' + processInstanceOid + '";
                           optionMarkup += "&amp;partition=' + partition + '";
                           optionMarkup += "&amp;investigate=false";
                           optionMarkup += "&amp;outputValue=";
                           optionMarkup += option.name;
                           optionMarkup += "&amp;hashCode=' + ";
                           optionMarkup += hashCodeJS;
                           optionMarkup += "+ '";
                           optionMarkup += "&quot;>";
                           optionMarkup += option.name;
                           optionMarkup += "</a></li>";
                        }
                     }
                     optionMarkup += "</ul>";
                     return optionMarkup;
                  }
                  return "";
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
                *
                */
               MailIntegrationOverlay.prototype.activate = function()
               {
                  this
                           .setResponseType(this.getApplication().attributes["stardust:emailOverlay::responseType"]);
                  var accessPoints = this.createIntrinsicAccessPoints();
                  this.submitParameterDefinitionsChanges(accessPoints);
               };
               MailIntegrationOverlay.prototype.createIntrinsicAccessPoints = function()
               {
                  var accessPoints = {};
                  var defaultAccessPoints = [];
                  for (var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n)
                  {
                     var parameterDefinition = this.getApplication().contexts.application.accessPoints[n];
                     if (parameterDefinition.direction == m_constants.IN_ACCESS_POINT)
                     {
                        accessPoints[parameterDefinition.id] = parameterDefinition;
                        defaultAccessPoints.push(parameterDefinition);
                     }
                  }
                  if (this.responseTypeSelect.val() != "none")
                  {
                     defaultAccessPoints.push({
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
                *
                */
               MailIntegrationOverlay.prototype.update = function()
               {
                  this.parameterDefinitionsPanel.setScopeModel(this.getScopeModel());
                  this.parameterDefinitionsPanel.setParameterDefinitions(this
                           .getApplication().contexts.application.accessPoints);
                  this
                           .setResponseType(this.getApplication().attributes["stardust:emailOverlay::responseType"]);
                  this.serverInput
                           .val(this.getApplication().attributes["stardust:emailOverlay::server"]);
                  this.mailFormatSelect
                           .val(this.getApplication().attributes["stardust:emailOverlay::mailFormat"]);
                  this.protocolSelect
                           .val(this.getApplication().attributes["stardust:emailOverlay::protocol"]);
                  this.subjectInput
                           .val(this.getApplication().attributes["stardust:emailOverlay::subject"]);
                  this.identifierInSubjectInput
                           .prop(
                                    "checked",
                                    this.getApplication().attributes["stardust:emailOverlay::includeUniqueIdentifierInSubject"]);
                  this.toInput
                           .val(this.getApplication().attributes["stardust:emailOverlay::to"]);
                  this.fromInput
                           .val(this.getApplication().attributes["stardust:emailOverlay::from"]);
                  this.ccInput
                           .val(this.getApplication().attributes["stardust:emailOverlay::cc"]);
                  this.bccInput
                           .val(this.getApplication().attributes["stardust:emailOverlay::bcc"]);
                  this.storeEmailInput
                           .prop(
                                    "checked",
                                    this.getApplication().attributes["stardust:emailOverlay::storeEmail"]);
                  this.storeAttachmentsInput
                           .prop(
                                    "checked",
                                    this.getApplication().attributes["stardust:emailOverlay::storeAttachments"]);
                  this
                           .setTemplateSource(this.getApplication().attributes["stardust:emailOverlay::templateSource"]);
                  this.templatePathInput
                           .val(this.getApplication().attributes["stardust:emailOverlay::templatePath"]);
                  this.responseOptionsTypeSelect
                           .val(this.getApplication().attributes["stardust:emailOverlay::responseOptionType"]);
                  this.responseHttpUrlInput
                           .val(this.getApplication().attributes["stardust:emailOverlay::responseHttpUrl"]);
                  this.userInput
                           .val(this.getApplication().attributes["stardust:emailOverlay::user"]);
                  this.passwordInput
                           .val(this.getApplication().attributes["stardust:emailOverlay::pwd"]);
                  this.transactedRouteInput
                           .prop(
                                    "checked",
                                    this.getApplication().attributes["carnot:engine:camel::transactedRoute"]);
                  
                  this.autoStartupInput
                           .prop(
                                    "checked",
                                    this.getApplication().attributes["carnot:engine:camel::autoStartup"]);
                  
                  var templateConfigurationsJson = this.getApplication().attributes["stardust:emailOverlay::templateConfigurations"];
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
                  this.attachmentsTemplateSource = this.getApplication().attributes["stardust:emailOverlay::attachmentsTemplateSource"];
                  this.attachmentsTemplateSourceType = this.getApplication().attributes["stardust:emailOverlay::attachmentsTemplateSourceType"];
                  if(this.attachmentsTemplateSource == "data")
                  {
                     m_angularContextUtils.runInAngularContext(function($scope) {
                        $scope.temSrcOptInit = $scope.overlayPanel.templateSourceOptions[1].value;
                     }, m_utils.jQuerySelect("#attachmentsTemplateSourceSelect"));
                     
                     var typeDeclaration = this.attachmentsTemplateSourceType;
                     m_angularContextUtils.runInAngularContext(function($scope) {
                        $scope.typeDeclaration = typeDeclaration;
                     }, m_utils.jQuerySelect("#attachmentsTemplateSourceTypeTab #attachmentsTemplateSourceTypeSelect"));
                       
                     m_utils.jQuerySelect("#attachmentsTemplateSourceTypeTab").show();
                     m_utils.jQuerySelect("#templateConfigurationTab").hide();
                  
                  } else if(this.attachmentsTemplateSource == "embedded" || this.attachmentsTemplateSource == undefined)
                  {
                     m_angularContextUtils.runInAngularContext(function($scope) {
                        $scope.temSrcOptInit = $scope.overlayPanel.templateSourceOptions[0].value;
                     }, m_utils.jQuerySelect("#attachmentsTemplateSourceSelect"));
                     
                     m_utils.jQuerySelect("#attachmentsTemplateSourceTypeTab").hide();
                     m_utils.jQuerySelect("#templateConfigurationTab").show();
                     
                  } else
                  {
                     m_angularContextUtils.runInAngularContext(function($scope) {
                        $scope.temSrcOptInit = $scope.overlayPanel.templateSourceOptions[2].value;
                     }, m_utils.jQuerySelect("#attachmentsTemplateSourceSelect"));
                     
                     m_utils.jQuerySelect("#attachmentsTemplateSourceTypeTab").hide();
                     m_utils.jQuerySelect("#templateConfigurationTab").hide();
                  }
                  this.view.validate();
               };

               /**
                *
                */
               MailIntegrationOverlay.prototype.getRoute = function()
               {
                  var route = m_mailRouteDefinitionHandler.createRouteForEmail(this);
                  m_utils.debug(route);
                  return route;
               };
               MailIntegrationOverlay.prototype.submitChanges = function(skipValidation)
               {
                  var applicationTypeChanges = null;
                  var invocationPatternChanges = null;
                  var invocationTypeChanges = null;
                  var responseTypeChanges = null;
                  var responseHttpUrlChanges = null;
                  var responseOptionsTypeChanges = null;
                  if (this.responseTypeSelect.val() === "none")
                  {
                     applicationTypeChanges = "camelSpringProducerApplication";
                     invocationPatternChanges = "send";
                     invocationTypeChanges = "synchronous";
                  }
                  else
                  {
                     applicationTypeChanges = "camelConsumerApplication";
                     invocationPatternChanges = "sendReceive";
                     invocationTypeChanges = "asynchronous";
                     responseTypeChanges = this.responseTypeSelect.val();
                     responseHttpUrlChanges = this.responseHttpUrlInput.val();
                     responseOptionsTypeChanges = this.responseOptionsTypeSelect.val();
                  }
                  var accessPointsChanges = this.getApplication().contexts.application.accessPoints;
                  accessPointsChanges = this.createIntrinsicAccessPoints();
                  this.submitParameterDefinitionsChanges(accessPointsChanges);
                  this.view
                           .submitChanges(
                                    {
                                       type : applicationTypeChanges,
                                       attributes : {
                                          "carnot:engine:camel::applicationIntegrationOverlay" : "mailIntegrationOverlay",
                                          "carnot:engine:camel::transactedRoute" : this.transactedRouteInput
                                                   .prop("checked"),
                                          "carnot:engine:camel::autoStartup" : this.autoStartupInput
                                                   .prop("checked"),
                                          "carnot:engine:camel::camelContextId" : "defaultCamelContext",
                                          "carnot:engine:camel::invocationPattern" : invocationPatternChanges,
                                          "carnot:engine:camel::invocationType" : invocationTypeChanges,
                                          "carnot:engine:camel::routeEntries" : this
                                                   .getRoute(),
                                          "carnot:engine:camel::consumerRoute" : "",
                                          "carnot:engine:camel::includeAttributesAsHeaders" : "false",
                                          "carnot:engine:camel::processContextHeaders" : "true",
                                          "stardust:emailOverlay::responseType" : responseTypeChanges,
                                          "stardust:emailOverlay::responseOptionType" : responseOptionsTypeChanges,
                                          "stardust:emailOverlay::responseHttpUrl" : responseHttpUrlChanges,
                                          "stardust:emailOverlay::server" : this.serverInput
                                                   .val(),
                                          "stardust:emailOverlay::user" : this.userInput
                                                   .val(),
                                          "stardust:emailOverlay::pwd" : this.passwordInput
                                                   .val(),
                                          "stardust:emailOverlay::mailFormat" : this.mailFormatSelect
                                                   .val(),
                                          "stardust:emailOverlay::protocol" : this.protocolSelect
                                                   .val(),
                                          "stardust:emailOverlay::subject" : this.subjectInput
                                                   .val(),
                                          "stardust:emailOverlay::includeUniqueIdentifierInSubject" : this.identifierInSubjectInput
                                                   .prop("checked"),
                                          "stardust:emailOverlay::from" : this.fromInput
                                                   .val(),
                                          "stardust:emailOverlay::to" : this.toInput
                                                   .val(),
                                          "stardust:emailOverlay::cc" : this.ccInput
                                                   .val(),
                                          "stardust:emailOverlay::bcc" : this.bccInput
                                                   .val(),
                                          "stardust:emailOverlay::storeEmail" : this.storeEmailInput
                                                   .prop("checked"),
                                          "stardust:emailOverlay::storeAttachments" : this.storeAttachmentsInput
                                                   .prop("checked"),
                                          "stardust:emailOverlay::templateSource" : this.templateSourceSelect
                                                   .val(),
                                          "stardust:emailOverlay::templatePath" : this.templatePathInput
                                                   .val(),
                                          "stardust:emailOverlay::mailTemplate" : CKEDITOR.instances[this.mailTemplateEditor.id]
                                                   .getData(),
                                          "stardust:emailOverlay::templateConfigurations" : angular
                                                   .toJson(this.templateConfigurations),
                                          "stardust:emailOverlay::attachmentsTemplateSource" : this.attachmentsTemplateSource
                                                   ,
                                          "stardust:emailOverlay::attachmentsTemplateSourceType" : this.attachmentsTemplateSourceType
                                       }
                                    }, skipValidation);
               };
               MailIntegrationOverlay.prototype.submitParameterDefinitionsChanges = function(
                        parameterDefinitionsChanges)
               {
                  var applicationTypeChanges = null;
                  if (this.responseTypeSelect.val() === "none")
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
               MailIntegrationOverlay.prototype.validate = function()
               {
                  var valid = true;
                  this.serverInput.removeClass("error");
                  this.userInput.removeClass("error");
                  this.passwordInput.removeClass("error");
                  this.templatePathInput.removeClass("error");
                  this.parameterDefinitionNameInput.removeClass("error");
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
                  if (m_utils.isEmptyString(this.serverInput.val()))
                  {
                     this.view.errorMessages.push("Mail server must be defined."); // TODO
                     // I18N
                     this.serverInput.addClass("error");
                     valid = false;
                  }
                  if (this.templateSourceSelect.val() == "repository"
                           || this.templateSourceSelect.val() == "classpath")
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
                  return valid;
               };
               this.sourceOptions = [ {
                  value : "repository",
                  title : "Document Repository"
               }, {
                  value : "classpath",
                  title : "Classpath"
               } , {
                  value : "data",
                  title : "Data"
               } ];
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
               this.i18nValues = initI18nLabels();
               /**
                *
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
                  this.submitChanges();
               };
               /**
                *
                */
               MailIntegrationOverlay.prototype.deleteConfiguration = function(index)
               {
                  this.templateConfigurations.splice(index, 1);
                  this.submitChanges();
               };
               /**
                *
                */
               MailIntegrationOverlay.prototype.i18nLabels = function(key)
               {
                  return this.i18nValues[key];
               };
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
               
               MailIntegrationOverlay.prototype.addApTemplateConfiguration = function(typeDeclarationFullId)
               {
                  var accessPoints = this.getApplication().contexts.application.accessPoints;
                  if(typeDeclarationFullId && typeDeclarationFullId != m_constants.TO_BE_DEFINED)
                  {
                     var requiredTemplatingType = this.getTemplateConfigurationType(typeDeclarationFullId);
                     if(requiredTemplatingType && requiredTemplatingType != undefined)
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
                           this.submitParameterDefinitionsChanges(accessPoints);
                           this.view.submitModelElementAttributeChange("stardust:emailOverlay::attachmentsTemplateSourceType", requiredTemplatingType
                                    .getFullId());
                           this.hideTemplateErroType();
                        }
                     } else
                     {
                        var filteredAccessPoints = m_routeDefinitionUtils.filterAccessPoint(accessPoints,
                              "mailAttachmentsAP");
                        this.submitParameterDefinitionsChanges(filteredAccessPoints);
                        this.view.submitModelElementAttributeChange("stardust:emailOverlay::attachmentsTemplateSourceType", null);
                        this.showTemplateErroType();
                     }
                     
                  } else
                  {
                     var filteredAccessPoints = m_routeDefinitionUtils.filterAccessPoint(accessPoints,
                              "mailAttachmentsAP");
                     this.submitParameterDefinitionsChanges(filteredAccessPoints);
                     this.view.submitModelElementAttributeChange("stardust:emailOverlay::attachmentsTemplateSourceType", null);
                     this.showTemplateErroType();
                  }
              
               };
               
               MailIntegrationOverlay.prototype.getTemplateConfigurationType = function(typeDeclarationFullId)
               {
                  var typeDeclaration = this.findTypeDeclaration(this.getScopeModel().typeDeclarations, typeDeclarationFullId);
                  
                  var elements = typeDeclaration.getElements();
                  if(elements.length == 1)
                  {
                     var elt = elements[0];
                     if(elt.type.indexOf(":") != -1)
                     {
                        var childType = elt.type.split(":")[1];
                        var childTypeDeclaration = typeDeclaration.model.findTypeDeclarationBySchemaName(childType);
                        if(childTypeDeclaration)
                        {
                           if(this.isTemplateConfigurationStructure(childTypeDeclaration))
                           {
                              return typeDeclaration;
                           } 
                        }
                     }
                  }
               };
               
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
               
               MailIntegrationOverlay.prototype.isTemplateConfigurationStructure = function(childTypeDeclaration)
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
               
               
               MailIntegrationOverlay.prototype.checkTemplatingStructure = function(element, name)
               {
                  var type = element.name == "tTemplate" ? "xsd:boolean" :"xsd:string";
                  return (element != undefined) && (element.name == name)
                           && (element.classifier == "element")
                           && (element.cardinality == "required")
                           && (element.type == type) ? true : false;
               };
               
               MailIntegrationOverlay.prototype.updateTemplateConfTab = function(item)
               {
                  var accessPoints = this.getApplication().contexts.application.accessPoints;
                  if(item == "data")
                  {
                     // filter Document Request AP
                     var filteredAccessPoints = m_routeDefinitionUtils.filterAccessPoint(accessPoints,
                           "DOCUMENT_REQUEST");
                     this.submitParameterDefinitionsChanges(filteredAccessPoints);
                     this.view.submitModelElementAttributeChange("stardust:emailOverlay::attachmentsTemplateSource", "data");
                     m_utils.jQuerySelect("#attachmentsTemplateSourceTypeTab").show();
                     m_utils.jQuerySelect("#templateConfigurationTab").hide();
                     
                  } else if(item == "embedded")
                  {
                     // filter Mail Attachments AP
                     var filteredAccessPoints = m_routeDefinitionUtils.filterAccessPoint(accessPoints,
                           "mailAttachmentsAP");
                     // filter Document Request AP
                     filteredAccessPoints =  m_routeDefinitionUtils.filterAccessPoint(filteredAccessPoints,
                           "DOCUMENT_REQUEST");
                     this.submitParameterDefinitionsChanges(filteredAccessPoints);
                     this.view.submitModelElementAttributeChange("stardust:emailOverlay::attachmentsTemplateSource", "embedded");
                     this.view.submitModelElementAttributeChange("stardust:emailOverlay::attachmentsTemplateSourceType", null);
                     this.hideTemplateErroType();
                     m_utils.jQuerySelect("#templateConfigurationTab").show();
                     m_utils.jQuerySelect("#attachmentsTemplateSourceTypeTab").hide();
                     
                  } else
                  {
                     // filter Mail Attachments AP
                     var filteredAccessPoints = m_routeDefinitionUtils.filterAccessPoint(accessPoints,
                           "mailAttachmentsAP");
                     // add Document Request AP
                     var documentRequestAp = m_routeDefinitionUtils.findAccessPoint(filteredAccessPoints, 
                           "DOCUMENT_REQUEST");
                     
                     if (!documentRequestAp)
                     {
                        filteredAccessPoints.push({
                              id : "DOCUMENT_REQUEST",
                              name : "Document Request",
                              dataType : "struct",
                              direction : "IN",
                              structuredDataTypeFullId : this.getScopeModel().id + ":" + "DOCUMENT_REQUEST",
                              attributes : {
                                 "stardust:predefined" : true
                  }
                           });
                     }
                     this.submitParameterDefinitionsChanges(filteredAccessPoints);
                     this.view.submitModelElementAttributeChange("stardust:emailOverlay::attachmentsTemplateSource", "DOCUMENT_REQUEST");
                     this.view.submitModelElementAttributeChange("stardust:emailOverlay::attachmentsTemplateSourceType", null);
                     m_utils.jQuerySelect("#attachmentsTemplateSourceTypeTab").hide();
                     m_utils.jQuerySelect("#templateConfigurationTab").hide();
                 
                  }
                 
               };
               
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
                                    + "Exactly One" + "</td>" + "</tr>" + "</table>");
                  m_utils.jQuerySelect("#typeErrorMessagesTab").show();
               };
               
               MailIntegrationOverlay.prototype.hideTemplateErroType = function()
               {
                  m_utils.jQuerySelect("#typeErrorMessagesTab").hide();
               };
               
               MailIntegrationOverlay.prototype.addTemplateConfigurationForDocumentAp = function(accessPoint)
               {
                  this.templateConfigurations.push({
                     "tTemplate" : false, 
                     "tName" : accessPoint.name,
                     "tPath" : "" ,
                     "tSource" : "data",
                     "tFormat" : "plain"
                  });
                  this.view.submitModelElementAttributeChange("stardust:emailOverlay::templateConfigurations", angular
                           .toJson(this.templateConfigurations));
               };
               
               MailIntegrationOverlay.prototype.deleteTemplateConfigurationForDocumentAp = function(accessPoint)
               {
                  var templateConf = this.templateConfigurations;
                  for(var i in templateConf)
                  {
                     if (templateConf[i].tName == accessPoint.name)
                     {
                        this.templateConfigurations.splice(i, 1);
                        this.view.submitModelElementAttributeChange("stardust:emailOverlay::templateConfigurations", angular
                                 .toJson(this.templateConfigurations));
                        break;
                     }
                  }
               };
               
               MailIntegrationOverlay.prototype.updateTemplateConfigurationForDocumentAp = function(accessPoint, newName)
               {
                  // add Template configuration related to Document AP if not exist
                  if(this.isAlreadyInTemplateConfiguration(accessPoint))
                  {
                     // update Template element Name
                     for(var i in this.templateConfigurations)
                     {
                        if (this.templateConfigurations[i].tSource == "data" && this.templateConfigurations[i].tName == accessPoint.name)
                        {
                           this.templateConfigurations[i].tName = newName;
                           this.view.submitModelElementAttributeChange("stardust:emailOverlay::templateConfigurations", angular
                                             .toJson(this.templateConfigurations));
                           break;
                        }
                     }
                     
                  } else
                  {
                     // add tempate conf
                     accessPoint.name = newName;
                     this.addTemplateConfigurationForDocumentAp(accessPoint);
                  }
               };
               
               MailIntegrationOverlay.prototype.isAlreadyInTemplateConfiguration = function(accessPoint)
               {
                  for(var i in this.templateConfigurations)
                  {
                     if(this.templateConfigurations[i].tName == accessPoint.name)
                     {
                        return true;
                     }
                  }
                  return false;
               };
               
               MailIntegrationOverlay.prototype.submitTemplateChanges = function(source, index)
               {
                  if(source == "data")
                  {
                     //TODO use new directive options-disabled  
                     this.templateConfigurations[index].tSource="repository";
                  }
                  this.view.submitModelElementAttributeChange("stardust:emailOverlay::templateConfigurations", angular
                           .toJson(this.templateConfigurations));
               };
            }
         });