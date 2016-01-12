define(
         [ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_i18nUtils",
                  "bpm-modeler/js/m_constants", "bpm-modeler/js/m_dialog",
                  "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_command",
                  "bpm-modeler/js/m_model", "bpm-modeler/js/m_accessPoint",
                  "bpm-modeler/js/m_typeDeclaration",
                  "bpm-modeler/js/m_parameterDefinitionsPanel",
                  "bpm-modeler/js/m_communicationController",
                  "bpm-modeler/js/m_codeEditorAce","bpm-modeler/js/m_user" ],
         function(m_utils, m_i18nUtils, m_constants, m_dialog, m_commandsController,
                  m_command, m_model, m_accessPoint, m_typeDeclaration,
                  m_parameterDefinitionsPanel,m_communicationController, m_codeEditorAce,m_user)
         {
            return {
               create : function(view)
               {
                  var overlay = new RestServiceOverlay();
                  overlay.initialize(view);
                  return overlay;
               }
            };
            /**
             * 
             */
            function RestServiceOverlay()
            {
               /**
                * 
                */
               RestServiceOverlay.prototype.initialize = function(view)
               {
                  var that = this;
                  this.view = view;

                  this.view.insertPropertiesTab("restServiceOverlay", "parameters",
                           "Parameters",
                           "plugins/bpm-modeler/images/icons/database_link.png");
                  this.view.insertPropertiesTab("restServiceOverlay", "test", "Test",
                           "plugins/bpm-modeler/images/icons/application-run.png");

                  this.view.insertPropertiesTab("restServiceOverlay", "security",
                           "Security", "plugins/bpm-modeler/images/icons/server-key.png");

                  this.uriInput = m_utils.jQuerySelect("#restServiceOverlay #uriInput");
                  this.queryStringLabel = m_utils
                           .jQuerySelect("#restServiceOverlay #queryStringLabel");
                  this.commandSelect = m_utils
                           .jQuerySelect("#restServiceOverlay #commandSelect");
                  this.requestTypeSelect = m_utils
                           .jQuerySelect("#restServiceOverlay #requestTypeSelect");
                  this.responseTypeSelect = m_utils
                           .jQuerySelect("#restServiceOverlay #responseTypeSelect");
                  this.crossDomainInput = m_utils
                           .jQuerySelect("#restServiceOverlay #crossDomainInput");
                  this.transactedRouteRow = m_utils
                           .jQuerySelect("#restServiceOverlay #transactedRouteRow");
                  this.transactedRouteInput = m_utils
                           .jQuerySelect("#restServiceOverlay #transactedRouteInput");
                  this.autoStartupRow = m_utils
                           .jQuerySelect("#restServiceOverlay #autoStartupRow");
                  this.autoStartupInput = m_utils
                           .jQuerySelect("#restServiceOverlay #autoStartupInput");
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

                  this.securityModeSelect = m_utils
                           .jQuerySelect("#securityTab #securityModeSelect");
                  this.httpBasicAuthUserInput = m_utils
                           .jQuerySelect("#securityTab #httpBasicAuthUserInput");
                  this.httpBasicAuthPwdInput = m_utils
                           .jQuerySelect("#securityTab #httpBasicAuthPwdInput");
                  this.httpBasicAuthUsingCVInput = m_utils
                           .jQuerySelect("#securityTab #httpBasicAuthUsingCVInput");

                  this.customSecurityTokenKeyInput = m_utils
                           .jQuerySelect("#securityTab #customSecurityTokenKeyInput");
                  this.customSecurityTokenValueInput = m_utils
                           .jQuerySelect("#securityTab #customSecurityTokenValueInput");
                  this.customSecurityTokenUsingCVInput = m_utils
                           .jQuerySelect("#securityTab #customSecurityTokenUsingCVInput");

                  this.authenticationPreemptiveInput = m_utils
                           .jQuerySelect("#securityTab #authenticationPreemptiveInput");
                  this.authenticationPreemptiveInput.hide();
                  this.authenticationPreemptiveLabel = m_utils
                           .jQuerySelect("#securityTab #authenticationPreemptiveLabel");
                  this.authenticationPreemptiveLabel.hide();
                  m_utils
                           .jQuerySelect("label[for='securityModeSelect']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.rest.security.securityModeSelect.label"));
                  m_utils
                           .jQuerySelect("#securityModeSelect option[value='none']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.rest.security.securityModeSelect.none.label"));
                  m_utils
                           .jQuerySelect(
                                    "#securityModeSelect option[value='httpBasicAuth']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.rest.security.securityModeSelect.httpBasicAuth.label"));
                  m_utils
                           .jQuerySelect(
                                    "#securityModeSelect option[value='customSecTok']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.rest.security.securityModeSelect.customSecTok.label"));
                  m_utils
                           .jQuerySelect("#httpBasicAuthenticationHintLabel")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.rest.security.httpBasicAuthenticationHint.label"));
                  m_utils
                           .jQuerySelect("#customSecurityTokenHintLabel")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.rest.security.customSecurityTokenHint.label"));
                  this.resetButton
                           .prop(
                                    "title",
                                    m_i18nUtils
                                             .getProperty("modeler.model.propertyView.uiMashup.test.resetButton.title"));
                  this.runButton
                           .prop(
                                    "title",
                                    m_i18nUtils
                                             .getProperty("modeler.model.propertyView.uiMashup.test.runButton.title"));
                  m_utils
                           .jQuerySelect("label[for='inputDataTextArea']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.propertyView.uiMashup.test.inputDataTextArea.label"));
                  m_utils
                           .jQuerySelect("label[for='outputDataTextarea']")
                           .text(
                                    m_i18nUtils
                                             .getProperty("modeler.model.propertyView.uiMashup.test.outputDataTextArea.label"));

                  this.parameterDefinitionsPanel = m_parameterDefinitionsPanel.create({
                     scope : "parametersTab",
                     submitHandler : this,
                     supportsOrdering : false,
                     supportsDataMappings : false,
                     supportsDescriptors : false,
                     supportsDataTypeSelection : true,
                     supportsDocumentTypes : false
                  });

                  var self = this;
                  this.parameterDefinitionNameInput = m_utils
                           .jQuerySelect("#parametersTab #parameterDefinitionNameInput");
                  var httpHeadersJson = this.getApplication().attributes["stardust:restServiceOverlay::httpHeaders"];

                  if (!httpHeadersJson)
                  {
                     httpHeadersJson = "[]";
                  }

                  this.httpHeaders = JSON.parse(httpHeadersJson);

                  this.initializeHeaderAttributesTable();

                  this.uriInput.change(function()
                  {
                     self.submitChanges();
                  });

                  this.parameterDefinitionNameInput.change(function()
                  {
                     self.submitChanges();
                  });
                  this.commandSelect.change(function()
                  {
                     self.submitChanges();
                  });
                  this.requestTypeSelect.change(function()
                  {
                     self.submitChanges();
                  });
                  this.responseTypeSelect.change(function()
                  {
                     self.submitChanges();
                  });
                  this.crossDomainInput.change(function()
                  {
                     self.submitChanges();
                  });
                  this.inputBodyAccessPointInput
                           .change(function()
                           {
                              var attributes= self.getApplication().attributes;
                              var submitElements = {};
                              if (self.inputBodyAccessPointInput.val() == m_constants.TO_BE_DEFINED)
                              {
                                 attributes["carnot:engine:camel::inBodyAccessPoint"]=null;
                              }else{
                                 attributes["carnot:engine:camel::inBodyAccessPoint"]=self.inputBodyAccessPointInput.val();
                              }
                              submitElements.attributes = attributes;
                              self.view.submitChanges(submitElements, false);
                           });
                  this.outputBodyAccessPointInput
                           .change(function()
                           {
                              var attributes= self.getApplication().attributes;
                              var submitElements = {};
                              if (self.outputBodyAccessPointInput.val() == m_constants.TO_BE_DEFINED)
                              {
                                 attributes["carnot:engine:camel::outBodyAccessPoint"]=null;
                              }else{
                                 attributes["carnot:engine:camel::outBodyAccessPoint"]=self.outputBodyAccessPointInput.val();
                              }
                              submitElements.attributes = attributes;
                              self.view.submitChanges(submitElements, false);
                           });

                  this.securityModeSelect.change(function()
                  {
                     var attributes= self.getApplication().attributes;
                     var accessPoints=self.getApplication().contexts.application.accessPoints;
                     var submitElements = {};
                     attributes["stardust:restServiceOverlay::securityMode"]=self.securityModeSelect.val();
                     if(attributes["stardust:restServiceOverlay::securityMode"]=="none"){
                        attributes["stardust:restServiceOverlay::customSecurityTokenKey"]=null;
                        attributes["stardust:restServiceOverlay::customSecurityTokenValue"]=null;
                        attributes["stardust:restServiceOverlay::securityTokenSource"]=null;
                        attributes["stardust:restServiceOverlay::customSecurityTokenCV" ]=null;
                        attributes["stardust:restServiceOverlay::httpBasicAuthUser" ]=null;
                        attributes["stardust:restServiceOverlay::httpBasicAuthPwd" ]=null;
                        attributes["stardust:restServiceOverlay::httpBasicAuthCV" ]=null;
                        attributes["stardust:restServiceOverlay::authenticationPreemptive" ]=false;
                     }
                     submitElements.attributes = attributes;
                     self.view.submitChanges(submitElements, false);
                     self.setSecurityMode(self.securityModeSelect.val());
                  });

                  this.httpBasicAuthUserInput.change(function()
                  {
                     self.submitChanges();
                  });

                  this.httpBasicAuthPwdInput.change(function()
                  {
                     self.submitChanges();
                  });

                  this.httpBasicAuthUsingCVInput.change(function()
                  {
                     self.submitChanges();
                  });
                  this.authenticationPreemptiveInput.change(function()
                  {
                     self.submitChanges();
                  });

                  this.customSecurityTokenKeyInput.change({
                     panel : this
                  }, function(event)
                  {
                     var that = event.data.panel;
                     var attributes= that.getApplication().attributes;
                     var accessPoints=that.getApplication().contexts.application.accessPoints;
                     attributes["stardust:restServiceOverlay::securityTokenSource"] = null;
                     attributes["stardust:restServiceOverlay::customSecurityTokenKey"]=that.customSecurityTokenKeyInput.val();
                     attributes["carnot:engine:camel::routeEntries"]=that.getRoute(attributes, accessPoints);
                     that.view
                     .submitChanges(
                              {
                                 attributes : attributes
                              }, false);
                     that.updateSecurityView();
                  });
                  m_utils
                           .jQuerySelect(this.customSecurityTokenKeyInput)
                           .autocomplete(
                                    {
                                       source : function(request, response)
                                       {
                                          var accessPoints = that.getApplication().contexts.application.accessPoints;
                                          var outputList = [];
                                          for (var n = 0; n < accessPoints.length; ++n)
                                          {
                                             var ap = accessPoints[n];
                                             if (ap.dataType == m_constants.PRIMITIVE_DATA_TYPE
                                                      && ap.direction == m_constants.IN_ACCESS_POINT)
                                                outputList.push(ap.id);
                                          }
                                          var matcher = new RegExp("^"
                                                   + $.ui.autocomplete
                                                            .escapeRegex(request.term),
                                                   "i");
                                          response($.grep(outputList, function(item)
                                          {
                                             return matcher.test(item);
                                          }));
                                       },
                                       select : function(event, ui)
                                       {
                                          var attributes = that.getApplication().attributes;
                                          var accessPoints = that.getApplication().contexts.application.accessPoints;
                                          var submitElements = {};
                                          attributes["stardust:restServiceOverlay::customSecurityTokenValue"]=null;
                                          attributes["stardust:restServiceOverlay::customSecurityTokenCV"]=false;
                                          attributes["stardust:restServiceOverlay::customSecurityTokenKey"] = ui.item.value;
                                          attributes["stardust:restServiceOverlay::securityTokenSource"] = "data";
                                          attributes["carnot:engine:camel::routeEntries"]=that.getRoute(attributes, accessPoints);
                                          submitElements.attributes = attributes;
                                          that.view.submitChanges(submitElements, false);
                                       }
                                    });
                  this.customSecurityTokenValueInput.change(function()
                  {
                     self.submitChanges();
                  });

                  this.customSecurityTokenUsingCVInput.change(function()
                  {
                     self.submitChanges();
                  });
                  this.transactedRouteInput.change(function()
                  {
                     var attributes = self.getApplication().attributes;
                     var accessPoints = self.getApplication().contexts.application.accessPoints;
                     var submitElements = {};
                     attributes["carnot:engine:camel::transactedRoute"]=self.transactedRouteInput.prop('checked');
                     attributes["carnot:engine:camel::routeEntries"]=self.getRoute(attributes, accessPoints);
                     submitElements.attributes = attributes;
                     self.view.submitChanges(submitElements, false);
                  });

                  this.autoStartupInput.change(function()
                  {
                     var attributes = self.getApplication().attributes;
                     var accessPoints = self.getApplication().contexts.application.accessPoints;
                     var submitElements = {};
                     attributes["carnot:engine:camel::autoStartup"]=self.autoStartupInput.prop('checked');
                     attributes["carnot:engine:camel::routeEntries"]=self.getRoute(attributes, accessPoints);
                     submitElements.attributes = attributes;
                     self.view.submitChanges(submitElements, false);
                  });

                  this.runButton.click({
                     view : this
                  }, function(event)
                  {
                     var view = event.data.view;

                     jQuery.support.cors = true;

                     var dataType = "html";

                     if (view.responseTypeSelect.val() === "application/xml")
                     {
                        dataType = "xml";
                     }
                     else if (view.responseTypeSelect.val() === "application/json")
                     {
                        dataType = "json";
                     }

                     jQuery.ajax({
                        type : view.commandSelect.val(),
                        url : view.getInvocationUri(),
                        contentType : view.requestTypeSelect.val(),
                        dataType : dataType,
                        data : view.getRequestData(),
                        crossDomain : true
                     }).done(function(data)
                     {
                        view.outputDataTextarea.val(JSON.stringify(data));
                     }).fail(
                              function()
                              {
                                 view.outputDataTextarea
                                          .val("Could not retrieve data from "
                                                   + view.getInvocationUri() + " via "
                                                   + view.commandSelect.val()
                                                   + " command.");
                              });
                  });
                  this.resetButton
                           .click(
                                    {
                                       view : this
                                    },
                                    function(event)
                                    {
                                       var view = event.data.view;

                                       view.inputDataTextarea.empty();
                                       view.outputDataTextarea.empty();

                                       var inputData = "{";

                                       for (var n = 0; n < view.getApplication().contexts.application.accessPoints.length; ++n)
                                       {
                                          var parameterDefinition = view.getApplication().contexts.application.accessPoints[n];

                                          if (parameterDefinition.direction == m_constants.OUT_ACCESS_POINT)
                                          {
                                             continue;
                                          }

                                          if (n > 0)
                                          {
                                             inputData += ", ";
                                          }

                                          if (parameterDefinition.dataType == "primitive")
                                          {
                                             inputData += parameterDefinition.id;
                                             inputData += ": \"\"";
                                          }
                                          else if (parameterDefinition.dataType == "struct")
                                          {
                                             var typeDeclaration = m_model
                                                      .findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);

                                             inputData += parameterDefinition.id;
                                             inputData += ": ";
                                             inputData += JSON.stringify(typeDeclaration
                                                      .createInstance(), null, 3);
                                          }
                                          else if (parameterDefinition.dataType == "dmsDocument")
                                          {
                                             var typeDeclaration = m_model
                                                      .findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);

                                             inputData += parameterDefinition.id;
                                             inputData += ": ";
                                             inputData += JSON.stringify(typeDeclaration
                                                      .createInstance(), null, 3);
                                          }
                                          else
                                          {
                                             // Deal with primitives
                                          }
                                       }

                                       inputData += "}";

                                       view.inputDataTextarea.append(inputData);
                                    });
               };

               RestServiceOverlay.prototype.setSecurityMode = function(securityMode)
               {
                  var attributes = this.getApplication().attributes;
                  var accessPoints = this.getApplication().contexts.application.accessPoints;
                  if (!securityMode)
                  {
                     securityMode = "none";
                  }
                  this.authenticationPreemptiveLabel.hide();
                  this.authenticationPreemptiveInput.hide();
                  this.securityModeSelect.val(securityMode);
                  m_utils.jQuerySelect("#httpBasicAuthenticationDiv").hide();
                  m_utils.jQuerySelect("#customSecurityTokenDiv").hide();

                  if (securityMode === "customSecTok")
                  {
                     m_utils.jQuerySelect("#customSecurityTokenDiv").show();
                     this.httpBasicAuthUserInput.val("");
                     this.httpBasicAuthPwdInput.val("");
                     this.httpBasicAuthUsingCVInput.prop('checked', false);
                     this.authenticationPreemptiveInput.prop('checked', false);
                     this.customSecurityTokenValueInput.prop('disabled', false);
                     this.customSecurityTokenUsingCVInput.prop('disabled', false);
                  }else{ 
                     if (securityMode === "httpBasicAuth")
                        {
                           m_utils.jQuerySelect("#httpBasicAuthenticationDiv").show();
                           this.customSecurityTokenKeyInput.val("");
                           this.customSecurityTokenValueInput.val("");
                           this.customSecurityTokenUsingCVInput.prop('checked', false);
                           this.authenticationPreemptiveInput.show();
                           this.authenticationPreemptiveLabel.show();
                        }
                        else
                        {
                           this.httpBasicAuthUserInput.val("");
                           this.httpBasicAuthPwdInput.val("");
                           this.httpBasicAuthUsingCVInput.prop('checked', false);
                           this.customSecurityTokenKeyInput.val("");
                           this.customSecurityTokenValueInput.val("");
                           this.customSecurityTokenUsingCVInput.prop('checked', false);
                        }
                  }
               };
               /**
                * 
                */
               RestServiceOverlay.prototype.initializeHeaderAttributesTable = function()
               {
                  m_utils.jQuerySelect("#configurationTab #addHeaderButton").click(
                           {
                              page : this,
                           },
                           function(event)
                           {
                              event.data.page.httpHeaders.push({
                                 "headerName" : "New"
                                          + (event.data.page.httpHeaders.length + 1),
                                 "headerSource" : "direct",
                                 "headerValue" : "New"
                                          + (event.data.page.httpHeaders.length + 1)
                              });
                              var attributes=event.data.page.getApplication().attributes;
                              var accessPoints=event.data.page.getApplication().contexts.application.accessPoints;
                              attributes["stardust:restServiceOverlay::httpHeaders"]=JSON.stringify(event.data.page.httpHeaders);
                              attributes["carnot:engine:camel::routeEntries"]=event.data.page.getRoute(attributes, accessPoints);
                              event.data.page.view.submitChanges(
                                       {
                                          attributes : attributes
                                       }, false);
                              event.data.page.refreshHeaderAttributesTable();
                           });
                  this.refreshHeaderAttributesTable();
               };
               
               
               RestServiceOverlay.prototype.refreshConfigurationVariables = function() {
                  var page = this;

                  var deferred = jQuery.Deferred();
                  jQuery.ajax({
                     type : 'GET',
                     url : m_communicationController
                           .getEndpointUrl()
                           + "/models/"
                           + encodeURIComponent(this.getScopeModel().id)
                           + "/configurationVariables",
                     async : false
                  }).done(
                        function(json) {
                           page.getScopeModel().configVariables = json;
//                           page.refreshConfigurationVariablesTable(json);

                           deferred.resolve();
                        }).fail(function(data) {
                                 m_utils.debug("Error");
                                 deferred.reject();
                              });

                  return deferred.promise();
               };
               /**
                * 
                */
               RestServiceOverlay.prototype.refreshHeaderAttributesTable = function()
               {
                  m_utils.jQuerySelect("table#headerAttributesTable tbody").empty();
                  for (var n = 0; n < this.httpHeaders.length; ++n)
                  {
                     var row = m_utils.jQuerySelect("<tr id='"+n+"'></tr>");
                     var cell = m_utils.jQuerySelect("<td></td>");
                     row.append(cell);
                     var deleteButton = m_utils.jQuerySelect("<input type='image' title='Delete' alt='Delete' class='toolbarButton' src='plugins/bpm-modeler/images/icons/delete.png'/>");
                     deleteButton.click(
                                       {
                                          page : this,
                                          headerName : this.httpHeaders[n].headerName
                                       },
                                       function(event)
                                       {
                                          newHttpHeaders = [];
                                          for (var h = 0; h < event.data.page.httpHeaders.length; ++h)
                                          {
                                             if (event.data.headerName !== event.data.page.httpHeaders[h].headerName)
                                             {
                                                newHttpHeaders
                                                         .push(event.data.page.httpHeaders[h]);
                                             }
                                          }
                                          event.data.page.httpHeaders = newHttpHeaders;
                                          
                                          var attributes= event.data.page.getApplication().attributes;
                                          var accessPoints=event.data.page.getApplication().contexts.application.accessPoints;
                                          attributes["stardust:restServiceOverlay::httpHeaders"]= JSON.stringify(newHttpHeaders);
                                          attributes["carnot:engine:camel::routeEntries"]= event.data.page.getRoute(attributes,accessPoints);
                                          event.data.page.view.submitChanges(
                                                                        {
                                                                           attributes : attributes
                                                                        }, false);
                                          event.data.page.refreshHeaderAttributesTable();
                                       });
                     
                     cell.append(deleteButton);
                     cell = m_utils.jQuerySelect("<td></td>");
                     headerNameInput = m_utils.jQuerySelect("<input  id='"+n+"' type='text' class='cellEditor' value='"+ this.httpHeaders[n].headerName + "'></input>");//
                     headerNameInput.change({
                        page : this,
                        headerName : this.httpHeaders[n].headerName
                     }, function(event)
                     {
                        var index=event.target.parentElement.parentElement.id;
                       //var oldValue = event.data.headerName;
                        var newValue = event.target.value;
                        event.data.page.httpHeaders[index].headerName = newValue;
                        var attributes= event.data.page.getApplication().attributes;
                        var accessPoints=event.data.page.getApplication().contexts.application.accessPoints;
                        attributes["stardust:restServiceOverlay::httpHeaders"]= JSON.stringify(event.data.page.httpHeaders);
                        attributes["carnot:engine:camel::routeEntries"]= event.data.page.getRoute(attributes,accessPoints);
                        event.data.page.view.submitChanges(
                                 {
                                    attributes : attributes
                                 }, false);
                        event.data.page.refreshHeaderAttributesTable();
                     });

                     cell.append(headerNameInput);
                     row.append(cell);
                     cell = m_utils.jQuerySelect("<td></td>");
                     headerSourceInput = m_utils.jQuerySelect("<select></select>");//id='"+n+"'
                     headerSourceInput.empty();
                     headerSourceInput.append("<option value='direct' selected>Direct</option>");
                     headerSourceInput.append("<option value='data'>Data</option>");
                     headerSourceInput.append("<option value='cv'>Configuration Variable</option>");
                     headerSourceInput.change({
                        page : this,
                        headerSource : this.httpHeaders[n].headerSource
                     }, function(event)
                     {
                        var attributes = currentPage.getApplication().attributes;
                        var accessPoints = currentPage.getApplication().contexts.application.accessPoints;
                        var index=event.target.parentElement.parentElement.id;
                       // var index=event.currentTarget.id;
                        var newHttpHeaders=currentPage.httpHeaders;
                        newHttpHeaders[index].headerSource=event.currentTarget.value;
                        if(newHttpHeaders[index].headerSource=="cv"){
                           newHttpHeaders[index].headerValue="${"+newHttpHeaders[index].headerName+"}";
                           currentPage.refreshConfigurationVariables();
                        }else if(newHttpHeaders[index].headerSource=="direct")
                           newHttpHeaders[index].headerValue=newHttpHeaders[index].headerName;
                        else
                           newHttpHeaders[index].headerValue="";
                        
                        attributes["stardust:restServiceOverlay::httpHeaders"]= JSON.stringify(newHttpHeaders);
                        attributes["carnot:engine:camel::routeEntries"]= currentPage.getRoute(attributes,accessPoints);
                        currentPage.view.submitChanges(
                                 {
                                    attributes : attributes
                                 }, false);
                        currentPage.refreshHeaderAttributesTable();
                     });
                     cell.append(headerSourceInput);
                     row.append(cell);
                     cell = m_utils.jQuerySelect("<td></td>");
                     headerValueInput = m_utils
                              .jQuerySelect("<input id='"+n+"' type='text' class='cellEditor' value='"
                                       + this.httpHeaders[n].headerValue + "'></input>");
                     headerValueInput.change({
                        page : this,
                        headerValue : this.httpHeaders[n].headerValue
                     }, function(event)
                     {
                        var index=event.target.parentElement.parentElement.id;
                        var oldValue = event.data.headerValue;
                        var newValue = event.target.value;
                        event.data.page.httpHeaders[index].headerValue = newValue;
                        var attributes= event.data.page.getApplication().attributes;
                        var accessPoints=event.data.page.getApplication().contexts.application.accessPoints;
                        attributes["stardust:restServiceOverlay::httpHeaders"]= JSON.stringify(event.data.page.httpHeaders);
                        attributes["carnot:engine:camel::routeEntries"]= event.data.page.getRoute(attributes,accessPoints);
                        event.data.page.view.submitChanges(
                                 {
                                    attributes : attributes
                                 }, false);
                        event.data.page.refreshHeaderAttributesTable();
                     });
                     var currentPage=this;
                     var headerValueEntry=m_utils.jQuerySelect(headerValueInput.get(0));
                     headerValueEntry.autocomplete(
                              {
                                 source : function(request, response)
                                 {
                                    var accessPoints = currentPage.getApplication().contexts.application.accessPoints;
                                    var outputList = [];
                                    for (var n = 0; n < accessPoints.length; ++n)
                                    {
                                       var ap = accessPoints[n];
                                       if (ap.dataType == m_constants.PRIMITIVE_DATA_TYPE
                                                && ap.direction == m_constants.IN_ACCESS_POINT)
                                          outputList.push(ap.id);
                                    }
                                    var matcher = new RegExp("^"
                                             + $.ui.autocomplete
                                                      .escapeRegex(request.term),
                                             "i");
                                    response($.grep(outputList, function(item)
                                    {
                                       return matcher.test(item);
                                    }));
                                 },
                                 select : function(event, ui)
                                 {
                                    var attributes = currentPage.getApplication().attributes;
                                    var accessPoints = currentPage.getApplication().contexts.application.accessPoints;
                                    var index=event.target.id;
                                    var newHttpHeaders=currentPage.httpHeaders;
                                    if(newHttpHeaders[index].headerName==null || newHttpHeaders[index].headerName=="")
                                       newHttpHeaders[index].headerName=ui.item.value;
                                    newHttpHeaders[index].headerSource="data";
                                    newHttpHeaders[index].headerValue=ui.item.value;
                                    attributes["stardust:restServiceOverlay::httpHeaders"]= JSON.stringify(newHttpHeaders);
                                    attributes["carnot:engine:camel::routeEntries"]= currentPage.getRoute(attributes,accessPoints);
                                    currentPage.view.submitChanges(
                                             {
                                                attributes : attributes
                                             }, false);
                                    currentPage.refreshHeaderAttributesTable();
                                 }
                              });
                     
                     
                     
                     if(this.httpHeaders[n].headerSource){
                        headerSourceInput.val(this.httpHeaders[n].headerSource);
                        if( this.httpHeaders[n].headerSource=="cv"){
                           headerValueInput.prop('disabled', true);
                        }
                     }
                     cell.append(headerValueInput);
                     row.append(cell);
                     m_utils.jQuerySelect("table#headerAttributesTable tbody")
                              .append(row);
                  }
               };

               /**
                * 
                */
               RestServiceOverlay.prototype.getQueryString = function(attributes)
               {
                  var httpHeadersJson = attributes["stardust:restServiceOverlay::httpHeaders"];
                  var queryString = "";
                  if (!httpHeadersJson)
                  {
                     httpHeadersJson = "[]";
                  }
                  httpHeaders = JSON.parse(httpHeadersJson);

                  for (var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n)
                  {
                     var accessPoint = this.getApplication().contexts.application.accessPoints[n];

                     if (accessPoint.direction == m_constants.OUT_ACCESS_POINT
                              || this.uriInput.val().indexOf("{" + accessPoint.id + "}") >= 0
                              || accessPoint.id == this.inputBodyAccessPointInput.val())
                     {
                        continue;
                     }
                     if(this.skipHttpHeader(httpHeaders, accessPoint))
                        continue;

                     if (n > 0)
                     {
                        queryString += "&";
                     }

                     queryString += accessPoint.id;
                     queryString += "=<i>";
                     queryString += accessPoint.id;
                     queryString += "</i>";
                  }

                  return queryString;
               };

               /**
                * 
                */
               RestServiceOverlay.prototype.getInvocationUri = function()
               {
                  var uri = this.uriInput.val();
                  if (this.inputDataTextarea.val())
                  {
                     var input = eval("(" + this.inputDataTextarea.val() + ")");
                     var start = true;
                     for (var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n)
                     {
                        var accessPoint = this.getApplication().contexts.application.accessPoints[n];
                        if (accessPoint.direction == m_constants.OUT_ACCESS_POINT
                                 || accessPoint.id == this.inputBodyAccessPointInput
                                          .val())
                        {
                           continue;
                        }
                        if (this.uriInput.val().indexOf("{" + accessPoint.id + "}") >= 0)
                        {
                           uri = uri.replace("{" + accessPoint.id + "}",
                                    input[accessPoint.id]);
                        }
                        else
                        {
                           if (start)
                           {
                              uri += "?";
                              start = false;
                           }
                           else
                           {
                              uri += "&";
                           }
                           uri += accessPoint.id;
                           uri += "=";
                           uri += input[accessPoint.id];
                        }
                     }
                  }
                  return uri;
               };
               /**
                * 
                */
               RestServiceOverlay.prototype.getRequestData = function()
               {
                  if (this.inputBodyAccessPointInput.val() != m_constants.TO_BE_DEFINED
                           && this.inputDataTextarea.val())
                  {
                     var input = eval("(" + this.inputDataTextarea.val() + ")");
                     return input[this.inputBodyAccessPointInput.val()];
                  }
                  return null;
               };
               /**
                * 
                */
               RestServiceOverlay.prototype.getModelElement = function()
               {
                  return this.view.getModelElement();
               };
               /**
                * 
                */
               RestServiceOverlay.prototype.getApplication = function()
               {
                  return this.view.application;
               };
               /**
                * 
                */
               RestServiceOverlay.prototype.getScopeModel = function()
               {
                  return this.view.getModelElement().model;
               };
               
               RestServiceOverlay.prototype.getHttpHeaderByName=function(field, key, httpHeaders){
                  var response;
                  for (var i = 0; i < httpHeaders.length; i++)
                  {
                     var httpHeader = httpHeaders[i];
                     if(httpHeader[field]==key){
                        response=httpHeader;
                        break;
                     }
                  }
                  return response;
               };
               RestServiceOverlay.prototype.getConfigurationVariable=function(name, configurationVariables){
                  var response;
                  for (var i = 0; i < configurationVariables.length; i++)
                  {
                     var cv = configurationVariables[i];
                     if(cv.name==key){
                        response=cv;
                        break;
                     }
                  }
                  return response;
               };
               RestServiceOverlay.prototype.skipHttpHeader = function(httpHeaders, accessPoint){
                  var httpHeader=this.getHttpHeaderByName("headerName",accessPoint.id,httpHeaders);
                  if(httpHeader && httpHeader.headerSource=="data")
                     return true;
                  
                  httpHeader=this.getHttpHeaderByName("headerValue",accessPoint.id,httpHeaders);
                  if(httpHeader && httpHeader.headerSource=="data")
                     return true;
                  
                  return false;
               }
               
               
               /**
                * 
                */
               RestServiceOverlay.prototype.getRoute = function(attributes, accessPoints)
               {
                  var uri = attributes["stardust:restServiceOverlay::uri"];
                  var responseTypeSelect=attributes["stardust:restServiceOverlay::responseType"];
                  var inputBodyAccessPointInput=attributes["carnot:engine:camel::inBodyAccessPoint"];
                  var customSecurityTokenKeyInput=attributes["stardust:restServiceOverlay::customSecurityTokenKey"];
                  var customSecurityTokenValueInput=attributes["stardust:restServiceOverlay::customSecurityTokenValue"];
                  var customSecurityTokenUsingCVInput=attributes["stardust:restServiceOverlay::customSecurityTokenCV"];
                  var securityModeSelect=attributes["stardust:restServiceOverlay::securityMode"];
                  var httpHeadersJson = attributes["stardust:restServiceOverlay::httpHeaders"];
                  var commandSelect=attributes["stardust:restServiceOverlay::command"];
                  var requestTypeSelect=attributes["stardust:restServiceOverlay::requestType" ];
                  var authenticationPreemptiveInput=attributes["stardust:restServiceOverlay::authenticationPreemptive"];
                  var httpBasicAuthUsingCVInput=attributes["stardust:restServiceOverlay::httpBasicAuthCV"];
                  var httpBasicAuthPwdInput=attributes["stardust:restServiceOverlay::httpBasicAuthPwd"];
                  var httpBasicAuthUserInput=attributes["stardust:restServiceOverlay::httpBasicAuthUser"];
                  if (!httpHeadersJson)
                  {
                     httpHeadersJson = "[]";
                  }
                  httpHeaders = JSON.parse(httpHeadersJson);
                  
                  var start = true;
                  var route = "";
                  var httpUri = "";
                  var httpQuery = "";
                  if (uri && uri.indexOf("?") >= 0)
                  {
                     // there is already a ? defined in URI
                     start = false;
                  }
                  
                  for (var n = 0; n < accessPoints.length; ++n)
                  {
                     var accessPoint = accessPoints[n];
                     if (accessPoint.direction == m_constants.OUT_ACCESS_POINT || accessPoint.id == inputBodyAccessPointInput)
                     {
                        continue;
                     }
                     if(attributes["stardust:restServiceOverlay::securityTokenSource"]=="data" && accessPoint.id ===attributes["stardust:restServiceOverlay::customSecurityTokenKey"] )
                     {
                        continue;
                     }
                     
                     if(this.skipHttpHeader(httpHeaders, accessPoint))
                        continue;
                     
                     if (this.uriInput.val().indexOf("{" + accessPoint.id + "}") >= 0)
                     {
                        uri = uri.replace("{" + accessPoint.id + "}", "$simple{header."+ accessPoint.id + "}");
                        route += "<setHeader headerName='" + accessPoint.id + "'>";
                        route += "<javaScript>encodeURIComponent(request.headers.get('"+ accessPoint.id + "'))</javaScript>";
                        route += "</setHeader>";
                     }
                     else
                     {
                        if (start)
                        {
                           uri += "?";
                           start = false;
                        }
                        else
                        {
                           uri += "&";
                        }
                        uri += accessPoint.id;
                        uri += "=";
                        uri += "$simple{header." + accessPoint.id + "}";
                        route += "<setHeader headerName='" + accessPoint.id + "'>";
                        route += "<javaScript>encodeURIComponent(request.headers.get('"
                                 + accessPoint.id + "'))</javaScript>";
                        route += "</setHeader>";
                     }
                  }
                  
                  // add addtional headers defined via custom security token
                  if (securityModeSelect === "customSecTok")
                  {
                     var cKey = customSecurityTokenKeyInput;
                     if(attributes["stardust:restServiceOverlay::securityTokenSource"]=="data" ){
                        if (start)
                        {
                           uri += "?";
                           start = false;
                        }
                        else
                        {
                           uri += "&";
                        }
                        uri +=cKey;
                        uri += "=";
                        uri += "$simple{header." + cKey + "}";
                     }else{
                     route += "<setHeader headerName='"
                              + m_utils.encodeXmlPredfinedCharacters(cKey) + "'>";
                     route += "<constant>";
                     
                     if (customSecurityTokenUsingCVInput)
                     {
                        route += "${";
                     }
                     var cValue = customSecurityTokenValueInput;
                     if(cValue)
                        route += m_utils.encodeXmlPredfinedCharacters(cValue);
                     if (customSecurityTokenUsingCVInput)
                     {
                        route += ":Password}";
                     }
                     route += "</constant>";
                     route += "</setHeader>";
                     }
                  }
                  
                  route += "<setHeader headerName='Content-Type'>";
                  route += "<simple>" + requestTypeSelect + "</simple>";
                  route += "</setHeader>";
                  
                  for (var h = 0; h < httpHeaders.length; h++)
                  {
                     var hName = httpHeaders[h].headerName;
                     var hValue = httpHeaders[h].headerValue;
                     var hSource = httpHeaders[h].headerSource;
                     if(hName)
                        hName = m_utils.encodeXmlPredfinedCharacters(hName);
                     if(hValue)
                        hValue = m_utils.encodeXmlPredfinedCharacters(hValue);
                     if(hSource=="direct"){
                        if(hName && hValue){
                           route += "<setHeader headerName='" + hName + "'>";
                           route += "<constant>" + hValue + "</constant>";
                           route += "</setHeader>";
                        }
                     }else if(hSource=="cv"){
                        if(hName && hValue){
                           route += "<setHeader headerName='" + hName + "'>";
                           route += "<constant>" + hValue + "</constant>";
                           route += "</setHeader>";
                        }
                     }else{
                        if(hName){
                           route += "<setHeader headerName='" + hName + "'>";
                           route += "<simple>$simple{header."+hValue+"}</simple>";
                           route += "</setHeader>";
                        }
                     }
                  }
                  uri = uri.replace(/&/g, "&amp;");
                  if (uri.indexOf("?") > 0)
                  {
                     var index = uri.indexOf("?");
                     httpUri = uri.substring(0, index);
                     httpQuery = uri.substring(index + 1);
                     route += "<setHeader headerName='CamelHttpQuery'>";
                     route += "<simple>" + httpQuery + "</simple>";
                     route += "</setHeader>";
                  }
                  else
                  {
                     httpUri = uri;
                  }
                  route += "<setHeader headerName='CamelHttpUri'>";
                  route += "<simple>" + httpUri + "</simple>";
                  route += "</setHeader>";
                  route += "<setHeader headerName='CamelHttpMethod'>";
                  route += "<constant>" + commandSelect + "</constant>";
                  route += "</setHeader>";
                  
                  if (requestTypeSelect === "application/json")
                  {
                     route += "<to uri='bean:bpmTypeConverter?method=toJSON' />";
                  }
                  else if (requestTypeSelect === "application/xml")
                  {
                     route += "<to uri='bean:bpmTypeConverter?method=toXML' />";
                  }
                  
                  route += "<to uri='http://isoverwritten";
                  
                  if (securityModeSelect === "httpBasicAuth")
                  {
                     route += "?authMethod=Basic";
                     
                     if (authenticationPreemptiveInput)
                        route += "&amp;httpClient.authenticationPreemptive=true";
                     route += "&amp;authUsername=" + httpBasicAuthUserInput;
                     route += "&amp;authPassword="+httpBasicAuthPwdInput;
                  }
                  route += "'/>";
                  route += "<setHeader headerName='Content-Type'>";
                  route += "<simple>" + responseTypeSelect + "</simple>";
                  route += "</setHeader>";
                  if (responseTypeSelect === "application/json")
                  {
                     route += "<to uri='bean:bpmTypeConverter?method=fromJSON' />";
                  }
                  else if (responseTypeSelect === "application/xml")
                  {
                     route += "<to uri='bean:bpmTypeConverter?method=fromXML' />";
                  }
                  return route;
               };

               RestServiceOverlay.prototype.getHttpBasicAuthRawPwd = function(httpBasicAuthPwdInput)
               {
                  if (!m_utils.isEmptyString(httpBasicAuthPwdInput))
                  {
                     var rawPwd = m_utils
                              .encodeXmlPredfinedCharacters(httpBasicAuthPwdInput
                                       );
                     rawPwd = this.convertPasswordToConfigVariable(rawPwd, true);
                     rawPwd = "RAW(" + rawPwd + ")";
                     return rawPwd;
                  }
                  return httpBasicAuthPwdInput;
               };
               RestServiceOverlay.prototype.getHttpBasicAuthOriginePwd = function(rawPwd)
               {
                  if (!m_utils.isEmptyString(rawPwd))
                  {
                     var firstIdex = rawPwd.indexOf("(");
                     var lastIdex = rawPwd.lastIndexOf(")");
                     var originePwd = rawPwd.substring(firstIdex + 1, lastIdex);
                     originePwd = m_utils.decodeXmlPredfinedCharacters(originePwd);
                     originePwd = this.convertConfigVariableToPassword(originePwd);
                     return originePwd;
                  }
                  return this.httpBasicAuthPwdInput.val();
               };
               
               
               RestServiceOverlay.prototype.getContentType = function(input)
               {
                  if (input === "application/json")
                  {
                     return "application/json";
                  }
                  else if (input === "application/xml")
                  {
                     return "application/xml";
                  }else if(input === "text/plain")
                  {
                     return "text/plain";
                  }
               }
               RestServiceOverlay.prototype.convertConfigVariableToPassword = function(
                        originePwd)
               {
                  if (!m_utils.isEmptyString(originePwd))
                  {
                     if (originePwd.indexOf("${") > -1)
                     {
                        var firstIdex = originePwd.indexOf("${");
                        var lastIdex = originePwd.lastIndexOf(":");
                        originePwd = originePwd.substring(firstIdex + 2, lastIdex);
                     }
                     return originePwd;
                  }
                  return originePwd;
               };
               RestServiceOverlay.prototype.convertPasswordToConfigVariable = function(
                        originePwd, basicAuthentication)
               {
                  if (basicAuthentication == true
                           && this.httpBasicAuthUsingCVInput.prop("checked"))
                  {
                     originePwd = "${" + originePwd + ":Password}";
                  }
                  else if (basicAuthentication == false
                           && this.customSecurityTokenUsingCVInput.prop("checked"))
                  {
                     originePwd = "${" + originePwd + ":Password}";
                  }
                  return originePwd;
               };
               /**
                * 
                */
               RestServiceOverlay.prototype.activate = function()
               {
                  var attributes=this.getApplication().attributes;
                  var specificAttributes={};
                     
                  if(!attributes["carnot:engine:camel::applicationIntegrationOverlay"]){
                     specificAttributes["carnot:engine:camel::applicationIntegrationOverlay"]="restServiceOverlay";
                  }
                  if (Object.keys(specificAttributes).length > 0){
                      this.view
                      .submitChanges(
                               {
                                  attributes : specificAttributes
                               }, false);
                  }
               };
               /**
                * 
                */
               RestServiceOverlay.prototype.update = function()
               {
                  this.parameterDefinitionsPanel.setScopeModel(this.getScopeModel());
                  this.parameterDefinitionsPanel.setParameterDefinitions(this
                           .getApplication().contexts.application.accessPoints);
                  this.autoStartupRow.hide();
                  this.transactedRouteRow.hide();
                  if(this.isIntegrator()){
                    this.autoStartupRow.show();
                    this.transactedRouteRow.show();
                  }
                  this.inputBodyAccessPointInput.empty();
                  this.inputBodyAccessPointInput.append("<option value='"
                           + m_constants.TO_BE_DEFINED + "'>"
                           + m_i18nUtils.getProperty("None") // TODO I18N
                           + "</option>");
                  for (var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n)
                  {
                     var accessPoint = this.getApplication().contexts.application.accessPoints[n];
                     if (accessPoint.direction != m_constants.IN_ACCESS_POINT)
                     {
                        continue;
                     }
                     this.inputBodyAccessPointInput.append("<option value='"
                              + accessPoint.id + "'>" + accessPoint.name + "</option>");
                  }
                  this.outputBodyAccessPointInput.empty();
                  this.outputBodyAccessPointInput.append("<option value='"
                           + m_constants.TO_BE_DEFINED + "' selected>"
                           + m_i18nUtils.getProperty("None") // I18N
                           + "</option>");
                  for (var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n)
                  {
                     var accessPoint = this.getApplication().contexts.application.accessPoints[n];
                     if (accessPoint.direction != m_constants.OUT_ACCESS_POINT)
                     {
                        continue;
                     }
                     this.outputBodyAccessPointInput.append("<option value='"
                              + accessPoint.id + "'>" + accessPoint.name + "</option>");
                  }
                  this.inputBodyAccessPointInput
                           .val(this.getApplication().attributes["carnot:engine:camel::inBodyAccessPoint"]);
                  this.outputBodyAccessPointInput
                           .val(this.getApplication().attributes["carnot:engine:camel::outBodyAccessPoint"]);
                  this.uriInput
                           .val(this.getApplication().attributes["stardust:restServiceOverlay::uri"]);
                  this.queryStringLabel.empty();
                  this.queryStringLabel.append(this.getQueryString(this.getApplication().attributes));
                  this.commandSelect
                           .val(this.getApplication().attributes["stardust:restServiceOverlay::command"]);
                  this.requestTypeSelect
                           .val(this.getApplication().attributes["stardust:restServiceOverlay::requestType"]);
                  this.responseTypeSelect
                           .val(this.getApplication().attributes["stardust:restServiceOverlay::responseType"]);
                  this.crossDomainInput
                           .prop(
                                    "checked",
                                    this.getApplication().attributes["stardust:restServiceOverlay::crossDomain"]);
                  this
                           .setSecurityMode(this.getApplication().attributes["stardust:restServiceOverlay::securityMode"]);
                  this.httpBasicAuthUserInput
                           .val(this.getApplication().attributes["stardust:restServiceOverlay::httpBasicAuthUser"]);
                  this.authenticationPreemptiveInput
                           .prop(
                                    "checked",
                                    this.getApplication().attributes["stardust:restServiceOverlay::authenticationPreemptive"]);
                  this.httpBasicAuthUsingCVInput
                           .prop(
                                    "checked",
                                    this.getApplication().attributes["stardust:restServiceOverlay::httpBasicAuthCV"]);
                  this.httpBasicAuthPwdInput
                           .val(this
                                    .getHttpBasicAuthOriginePwd(this.getApplication().attributes["stardust:restServiceOverlay::httpBasicAuthPwd"]));
                  this.customSecurityTokenKeyInput
                           .val(this.getApplication().attributes["stardust:restServiceOverlay::customSecurityTokenKey"]);
                  this.customSecurityTokenUsingCVInput
                           .prop(
                                    "checked",
                                    this.getApplication().attributes["stardust:restServiceOverlay::customSecurityTokenCV"]);
                  this.customSecurityTokenValueInput
                           .val(this
                                    .convertConfigVariableToPassword(this
                                             .getApplication().attributes["stardust:restServiceOverlay::customSecurityTokenValue"]));
                  this.transactedRouteInput
                           .prop(
                                    "checked",
                                    this.getApplication().attributes["carnot:engine:camel::transactedRoute"]);
                  this.autoStartupInput
                           .prop(
                                    "checked",
                                    this.getApplication().attributes["carnot:engine:camel::autoStartup"]);
                  
                  var attributes=this.getApplication().attributes;
                  if (attributes["carnot:engine:camel::transactedRoute"] == null || attributes["carnot:engine:camel::transactedRoute"] === undefined)
                  {
                     attributes["carnot:engine:camel::transactedRoute"]=false;
                  }
                  if (attributes["carnot:engine:camel::autoStartup"] == null || attributes["carnot:engine:camel::autoStartup"] === undefined)
                  {
                     attributes["carnot:engine:camel::autoStartup"]=true;
                  }
                  if (attributes["stardust:restServiceOverlay::authenticationPreemptive"] == null)
                  {
                     attributes["stardust:restServiceOverlay::authenticationPreemptive"]=true;
                  }
                  if (Object.keys(attributes).length > 0){
                   this.view.submitChanges({
                               attributes : attributes
                         }, true);
                  }
                  
                  this.updateSecurityView();
               };
               /**
                * when 
                */
               RestServiceOverlay.prototype.updateSecurityView=function(){
                  var attributes=this.getApplication().attributes;
                  var disabled=false;
                  if(attributes["stardust:restServiceOverlay::securityTokenSource"] && attributes["stardust:restServiceOverlay::securityTokenSource"]=="data"){
                     disabled=true;
                  }
                  this.customSecurityTokenValueInput.prop('disabled', disabled);
                  this.customSecurityTokenUsingCVInput.prop('disabled', disabled);

               }
               /**
                * 
                */
               RestServiceOverlay.prototype.submitChanges = function(
                        )
               {
                  var attributes=this.getApplication().attributes;
                  var accessPoints=this.getApplication().contexts.application.accessPoints;
                  attributes["carnot:engine:camel::applicationIntegrationOverlay"]="restServiceOverlay";
                  attributes["carnot:engine:camel::camelContextId"]="defaultCamelContext";
                  attributes["stardust:restServiceOverlay::uri"]=this.uriInput.val();
                  attributes["stardust:restServiceOverlay::command"]=this.commandSelect.val();
                  attributes["stardust:restServiceOverlay::requestType"]=this.requestTypeSelect.val();
                  attributes["stardust:restServiceOverlay::responseType"]=this.responseTypeSelect.val();
                  attributes["stardust:restServiceOverlay::crossDomain"]= this.crossDomainInput.prop("checked");
                  attributes["carnot:engine:camel::transactedRoute"]=this.transactedRouteInput.prop("checked");
                  attributes["carnot:engine:camel::autoStartup"]=this.autoStartupInput.prop("checked");
                  //attributes["stardust:restServiceOverlay::securityMode"]=this.securityModeSelect.val();
                  attributes["stardust:restServiceOverlay::httpBasicAuthUser"]=this.httpBasicAuthUserInput.val();
                  attributes["stardust:restServiceOverlay::httpBasicAuthPwd"]= this.getHttpBasicAuthRawPwd(this.httpBasicAuthPwdInput.val());
                  attributes["stardust:restServiceOverlay::httpBasicAuthCV"]=this.httpBasicAuthUsingCVInput.prop("checked") ? this.httpBasicAuthUsingCVInput.prop("checked"): null;
                  attributes["stardust:restServiceOverlay::authenticationPreemptive"]=this.authenticationPreemptiveInput.prop("checked") ? this.authenticationPreemptiveInput.prop("checked"): false;
                  attributes["stardust:restServiceOverlay::customSecurityTokenKey"]=this.customSecurityTokenKeyInput.val();
                  attributes["stardust:restServiceOverlay::customSecurityTokenValue"]=this.convertPasswordToConfigVariable(this.customSecurityTokenValueInput.val(), false);
                  attributes["stardust:restServiceOverlay::customSecurityTokenCV" ]=this.customSecurityTokenUsingCVInput.prop("checked") ? this.customSecurityTokenUsingCVInput.prop("checked"): null;
                  
                  attributes["carnot:engine:camel::routeEntries"]=this.getRoute(attributes, accessPoints);
                  this.view.submitChanges({
                              attributes : attributes
                           },false);
               };

               /**
                * 
                */
               RestServiceOverlay.prototype.submitParameterDefinitionsChanges = function(
                        parameterDefinitionsChanges)
               {

                  if (!this
                           .validateParameterDefinitionsChanges(parameterDefinitionsChanges))
                     return;
                  var attributes=this.getApplication().attributes;
                  attributes["carnot:engine:camel::routeEntries"]=this.getRoute(attributes, parameterDefinitionsChanges);
                  
                  this.view
                           .submitChanges(
                                    {
                                       contexts : {
                                          application : {
                                             accessPoints : parameterDefinitionsChanges
                                          }
                                       },
                                       attributes : attributes
                                    }, true);
               };
               RestServiceOverlay.prototype.submitSingleAttributeChange = function(
                        attribute, value)
               {
                  if (this.getModelElement().attributes[attribute] != value)
                  {
                     var modelElement = {
                        attributes : {}
                     };
                     modelElement.attributes[attribute] = value;
                     this.view.submitChanges(modelElement, true);
                  }
               };
               RestServiceOverlay.prototype.validateParameterDefinitionsChanges = function(
                        parameterDefinitionsChanges)
               {
                  if (parameterDefinitionsChanges)
                  {
                     this.view.errorMessages = [];
                     this.view.errorMessagesList.empty();
                     m_dialog.makeInvisible(this.view.errorMessagesList);
                     this.parameterDefinitionsPanel.parameterDefinitionNameInput
                              .removeClass("error");
                     for (var n = 0; n < parameterDefinitionsChanges.length; ++n)
                     {
                        var parameterDefinition = parameterDefinitionsChanges[n];
                        if ((parameterDefinition.name.indexOf("-") != -1)
                                 || (parameterDefinition.name.replace(/ /g, "") == "headers")
                                 || (parameterDefinition.name.replace(/ /g, "") == "exchange"))
                        {
                           this.view.errorMessages.push(parameterDefinition.name
                                    + " is not a valid name.");
                           this.parameterDefinitionsPanel.parameterDefinitionNameInput
                                    .addClass("error");
                        }
                     }
                  }
                  if (this.view.errorMessages.length != 0)
                  {
                     this.view.showErrorMessages();
                     return false;
                  }
                  else
                  {
                     return true;
                  }
               }
               /**
                * 
                */
               RestServiceOverlay.prototype.validate = function()
               {
                     this.uriInput.removeClass("error");
                     this.parameterDefinitionsPanel.parameterDefinitionNameInput.removeClass("error");
                     this.httpBasicAuthUserInput.removeClass("error");
                     this.httpBasicAuthPwdInput.removeClass("error");
                     this.httpBasicAuthPwdInput.removeClass("warn");
                     this.customSecurityTokenKeyInput.removeClass("error");
                     this.customSecurityTokenValueInput.removeClass("error");
                     this.parameterDefinitionNameInput.removeClass("error");
                     this.customSecurityTokenValueInput.removeClass("warn");
                     
                     var parameterDefinitionNameInputWhithoutSpaces =  this.parameterDefinitionNameInput.val().replace(/ /g, "");
                     if ((parameterDefinitionNameInputWhithoutSpaces.indexOf("-") != -1) || (parameterDefinitionNameInputWhithoutSpaces ==  "exchange")|| (parameterDefinitionNameInputWhithoutSpaces ==  "headers")){
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
                    
                     if (m_utils.isEmptyString(this.uriInput.val())) 
                     {
                        this.view.errorMessages.push("URI must not be empty."); // TODO I18N
                        this.uriInput.addClass("error");
                        return false;
                     }
                     
                     if ("httpBasicAuth" === this.securityModeSelect.val())
                     {
                        if (m_utils.isEmptyString(this.httpBasicAuthUserInput.val())) 
                        {
                           this.view.errorMessages.push("Username for HTTP Basic Authentication must not be empty."); // TODO I18N
                           this.httpBasicAuthUserInput.addClass("error");
                           return false;
                        }
                        
                        if (m_utils.isEmptyString(this.httpBasicAuthPwdInput.val())) 
                        {
                           this.view.errorMessages.push("Pasword for HTTP Basic Authentication must not be empty."); // TODO I18N
                           this.httpBasicAuthPwdInput.addClass("error");
                           return false;
                        }
                        else if (this.httpBasicAuthPwdInput.val().indexOf("${") == 0)
                        {
                           this.view.errorMessages.push("You should be using a configuration variable."); // TODO I18N
                           this.httpBasicAuthPwdInput.addClass("warn");
                        }
                     }
                     
                     if ("customSecTok" === this.securityModeSelect.val())
                     {
                        if(this.getApplication().attributes["stardust:restServiceOverlay::securityTokenSource"]!="data"){
                           if (m_utils.isEmptyString(this.customSecurityTokenKeyInput.val())) 
                           {
                              this.view.errorMessages.push("Token Key for Custom Security Token must not be empty.");
                              this.customSecurityTokenKeyInput.addClass("error");
                              return false;
                           }
                           
                           if (m_utils.isEmptyString(this.customSecurityTokenValueInput.val())) 
                           {
                              this.view.errorMessages.push("Token Value for Custom Security Token must not be empty.");
                              this.customSecurityTokenValueInput.addClass("error");
                              return false;
                           }
                           else if (this.customSecurityTokenValueInput.val().indexOf("${") == 0)
                           {
                              this.view.errorMessages.push("You should be using a configuration variable.");
                              this.customSecurityTokenValueInput.addClass("warn");
                           }
                        }else{
                           
                        }
                     }
                     
                     if(this.httpHeaders.length>0){
                        for (var i = 0; i < this.httpHeaders.length; i++)
                        {
                           var httpHeader = this.httpHeaders[i];
                           if(httpHeader.headerSource=="direct"){
                              for (var j = 0; j < this.getApplication().contexts.application.accessPoints.length; j++)
                              {
                                 var accessPoint = this.getApplication().contexts.application.accessPoints[j];
                                 if(httpHeader.headerName==accessPoint.id && accessPoint.direction == m_constants.IN_ACCESS_POINT && accessPoint.dataType == m_constants.PRIMITIVE_DATA_TYPE){
                                    this.view.errorMessages.push("Please change the name of HTTP Header "+httpHeader.headerName+" because an access point with the same name already exists.");
                                 }
                                 
                              }
                              if(m_utils.isEmptyString(httpHeader.headerValue)){
                                 this.view.errorMessages.push("The value for header "+httpHeader.headerName+" cannot be empty.");
                              }
                           }
                        }
                     }
                     
                  return true;
               };
               RestServiceOverlay.prototype.isIntegrator = function(){
                  return m_user.getCurrentRole() == m_constants.INTEGRATOR_ROLE;
               }
            }
         });