define(
         [ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_i18nUtils",
                  "bpm-modeler/js/m_constants", "bpm-modeler/js/m_model","bpm-modeler/js/MailIntegrationOverlayResponseTabHandler"  ],
         function(m_utils, m_i18nUtils, m_constants, m_model,mailIntegrationOverlayResponseTabHandler)
         {
            var overlay = new MailIntegrationOverlayTestTabHandler();
            return {
               initialize : function(base)
               {
                  return overlay.initialize(base);
               },
               registerTestTabEvents : function()
               {
                  return overlay.registerTestTabEvents();
               },
               createParameterObjectString : function()
               {
                  return overlay.createParameterObjectString();
               },
               createResponseOptionString : function(responseOptionsTypeSelect,
                        responseOptionsTypeSelect, responseHttpUrlInput)
               {
                  return overlay.createResponseOptionString(responseTypeSelect,
                           responseOptionsTypeSelect, responseHttpUrlInput);
               }
            };
            function MailIntegrationOverlayTestTabHandler()
            {
               MailIntegrationOverlayTestTabHandler.prototype.initialize = function(base)
               {
                  this.base = base;
                  this.initializePropertiesTab(this.view);
                  this.loadLabels();
                  this.registerTestTabEvents();
               };
               /**
                * loads Test Tab components
                */
               MailIntegrationOverlayTestTabHandler.prototype.loadLabels = function()
               {
                  this.resetButton = m_utils.jQuerySelect("#testTab #resetButton");
                  this.runButton = m_utils.jQuerySelect("#testTab #runButton");
                  this.inputDataTextarea = m_utils.jQuerySelect("#testTab #inputDataTextarea");
                  this.outputDataTextarea = m_utils.jQuerySelect("#testTab #outputDataTextarea");
                  this.resetButton.prop("title",m_i18nUtils.getProperty("modeler.model.applicationOverlay.email.test.resetButton.title"));
                  this.runButton.prop("title",m_i18nUtils.getProperty("modeler.model.applicationOverlay.email.test.runButton.title"));
               };
               /**
                * Constructs the test tab.
                */
               MailIntegrationOverlayTestTabHandler.prototype.initializePropertiesTab = function(
                        view)
               {
                  this.base.view.insertPropertiesTab(
                                    "mailIntegrationOverlay",
                                    "test",
                                    m_i18nUtils.getProperty("modeler.model.applicationOverlay.email.test.title"),
                                    "plugins/bpm-modeler/images/icons/application-run.png");
               };
               /**
                * Register events for Test tab components
                */
               MailIntegrationOverlayTestTabHandler.prototype.registerTestTabEvents = function()
               {
                  this.runButton
                           .click(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var output = "var input = ";
                                       output += event.data.panel.inputDataTextarea.val();
                                       var inputDataTextareaObj = eval("(function(){return "
                                                + event.data.panel.inputDataTextarea
                                                         .val() + ";})()");
                                       var markup;
                                       if ((inputDataTextareaObj.mailContentAP != undefined)
                                                && (inputDataTextareaObj.mailContentAP != null)
                                                && (inputDataTextareaObj.mailContentAP != ""))
                                       {
                                          markup = "{{mailContentAP}}";
                                       }
                                       else
                                       {
                                          markup = CKEDITOR.instances[event.data.panel.base.mailTemplateEditor.id].getData();
                                          if (mailIntegrationOverlayResponseTabHandler.getResponseTypeSelect() != "none")
                                          {
                                             markup += event.data.panel
                                                      .createResponseOptionString(
                                                               mailIntegrationOverlayResponseTabHandler.getResponseTypeSelect(),
                                                               mailIntegrationOverlayResponseTabHandler.getResponseOptionsTypeSelect(),
                                                               mailIntegrationOverlayResponseTabHandler.getResponseHttpUrlInput());
                                          }
                                       }
                                       output += "; \""
                                                + markup.replace(new RegExp("\"", 'g'),
                                                         "'").replace(
                                                         new RegExp("\n", 'g'), " ")
                                                         .replace(new RegExp("{{", 'g'),
                                                                  "\" + input.").replace(
                                                                  new RegExp("}}", 'g'),
                                                                  " + \"") + "\"";
                                       event.data.panel.outputDataTextarea.empty();
                                       event.data.panel.outputDataTextarea.append(eval(output));
                                    });

                  this.resetButton.click({
                     panel : this
                  }, function(event)
                  {
                     event.data.panel.inputDataTextarea.empty();
                     event.data.panel.outputDataTextarea.empty();
                     event.data.panel.inputDataTextarea.append(event.data.panel
                              .createParameterObjectString(m_constants.IN_ACCESS_POINT,
                                       true));
                  });
               };
               /**
                * Constructs a String containing a list of available Access points that
                * can be used for testing.
                */
               MailIntegrationOverlayTestTabHandler.prototype.createParameterObjectString = function(
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
                  var accessPoints = this.base.getApplication().contexts.application.accessPoints;
                  for (var n = 0; n < accessPoints.length; ++n)
                  {
                     var parameterDefinition = accessPoints[n];
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
                        parameterObjectString += JSON.stringify(typeDeclaration.createInstance(), null, 3);
                     }
                  }
                  parameterObjectString += "}";
                  return parameterObjectString;
               };
               /**
                * Constructs a preview of the email content.
                */
               MailIntegrationOverlayTestTabHandler.prototype.createResponseOptionString = function(
                        responseOptionsTypeSelect, responseOptionsTypeSelect,
                        responseHttpUrlInput)
               {
                  if (responseOptionsTypeSelect != null
                           && responseOptionsTypeSelect != m_constants.TO_BE_DEFINED)
                  {
                     var typeDeclaration = m_model
                              .findTypeDeclaration(responseOptionsTypeSelect);
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
                           optionMarkup += responseHttpUrlInput;
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
            }
         });