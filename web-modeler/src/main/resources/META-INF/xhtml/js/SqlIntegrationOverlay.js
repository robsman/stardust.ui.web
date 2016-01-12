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
 * Utility functions for dialog programming.
 * 
 * @author
 */
define(
         [ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_i18nUtils",
                  "bpm-modeler/js/m_constants", "bpm-modeler/js/m_commandsController",
                  "bpm-modeler/js/m_command", "bpm-modeler/js/m_model",
                  "bpm-modeler/js/m_accessPoint", "bpm-modeler/js/m_typeDeclaration",
                  "bpm-modeler/js/m_parameterDefinitionsPanel",
                  "bpm-modeler/js/m_codeEditorAce", "bpm-modeler/js/m_user",
                  "bpm-modeler/js/m_routeDefinitionUtils" ],
         function(m_utils, m_i18nUtils, m_constants, m_commandsController, m_command,
                  m_model, m_accessPoint, m_typeDeclaration, m_parameterDefinitionsPanel,
                  m_codeEditorAce, m_user, m_routeDefinitionUtils)
         {
            return {
               create : function(view)
               {
                  var overlay = new SqlIntegrationOverlay();
                  overlay.initialize(view);
                  return overlay;
               }
            };

            /**
             * 
             */
            function SqlIntegrationOverlay()
            {
               /**
                * 
                */
               SqlIntegrationOverlay.prototype.initialize = function(view)
               {
                  this.view = view;

                  this.view
                           .insertPropertiesTab(
                                    "sqlIntegrationOverlay",
                                    "parameters",
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.scripting.parameters.title"),
                                    "plugins/bpm-modeler/images/icons/database_link.png");

                  this.view.insertPropertiesTab("sqlIntegrationOverlay", "dataSource",
                           "Data Source",
                           "plugins/bpm-modeler/images/icons/database_link.png");

                  this.sqlQueryHeading = m_utils
                           .jQuerySelect("#sqlIntegrationOverlay #sqlQueryHeading");
                  this.integratorSetupRow = m_utils
                           .jQuerySelect("#sqlIntegrationOverlay #integratorSetupRow");
                  this.transactedRouteInput = m_utils
                           .jQuerySelect("#sqlIntegrationOverlay #transactedRouteInput");
                  this.autoStartupInput = m_utils
                           .jQuerySelect("#sqlIntegrationOverlay #autoStartupInput");
                  this.inputBodyAccessPointInput = m_utils
                           .jQuerySelect("#parametersTab #inputBodyAccessPointInput");
                  this.outputBodyAccessPointInput = m_utils
                           .jQuerySelect("#parametersTab #outputBodyAccessPointInput");
                  this.expectedResultSetInput = m_utils
                           .jQuerySelect("#parametersTab #expectedResultSetInput");
                  this.expectedResultSetInput.empty();
                  this.expectedResultSetInput.append("<option value='"
                           + m_constants.TO_BE_DEFINED + "'>"
                           + m_i18nUtils.getProperty("None") + "</option>");
                  this.expectedResultSetInput
                           .append("<option value='SelectList'>List</option>");
                  this.expectedResultSetInput
                           .append("<option value='SelectOne'>One</option>");

                  this.expectedResultSetInput
                           .change(function()
                           {
                              var submitElements = {};
                              var attributes = self.getApplication().attributes;
                              submitElements.contexts = self.getApplication().contexts;
                              if (self.expectedResultSetInput.val() == m_constants.TO_BE_DEFINED)
                              {
                                 attributes["stardust:sqlScriptingOverlay::outputType"] = null;
                              }
                              else if (self.expectedResultSetInput.val() == "SelectList")
                              {
                                 attributes["stardust:sqlScriptingOverlay::outputType"] = "SelectList";
                              }
                              else if (self.expectedResultSetInput.val() == "SelectOne")
                              {
                                 attributes["stardust:sqlScriptingOverlay::outputType"] = "SelectOne";
                              }
                              attributes["carnot:engine:camel::routeEntries"] = self
                                       .getRoute(attributes),
                                       submitElements.attributes = attributes;
                              self.view.submitChanges(submitElements, true);
                           });

                  this.editorAnchor = m_utils.jQuerySelect("#codeEditorDiv").get(0);
                  this.editorAnchor.id = "codeEditorDiv"
                           + Math.floor((Math.random() * 100000) + 1);
                  this.codeEditor = m_codeEditorAce
                           .getSQLCodeEditor(this.editorAnchor.id);
                  this.codeEditor.loadLanguageTools();

                  this.connectionTypeSelect = m_utils
                           .jQuerySelect("#dataSourceTab #connectionTypeSelect");
                  this.databaseTypeSelect = m_utils
                           .jQuerySelect("#dataSourceTab #databaseTypeSelect");

                  this.urlInput = m_utils.jQuerySelect("#dataSourceTab #urlInput");
                  this.driverInput = m_utils.jQuerySelect("#dataSourceTab #driverInput");

                  this.hostInput = m_utils.jQuerySelect("#dataSourceTab #hostInput");
                  this.portInput = m_utils.jQuerySelect("#dataSourceTab #portInput");
                  this.dataBaseNameInput = m_utils
                           .jQuerySelect("#dataSourceTab #dataBaseNameInput");
                  this.userNameInput = m_utils
                           .jQuerySelect("#dataSourceTab #userNameInput");
                  this.passwordInput = m_utils
                           .jQuerySelect("#dataSourceTab #passwordInput");
                  this.useCVforPassowrdInput = m_utils
                           .jQuerySelect("#dataSourceTab #useCVforPassowrdInput");

                  this.connectionTypeSelect.empty();
                  this.connectionTypeSelect
                           .append("<option value='direct' selected>Direct</option>");
                  /*
                   * this.connectionTypeSelect .append("<option value='jndi'>JNDI</option>");
                   */

                  this.directConfigTab = m_utils
                           .jQuerySelect("#dataSourceTab #directConfigTab");
                  this.jndiConfigTab = m_utils.jQuerySelect("#jndiConfigTab");

                  this.dbUrlConfig = m_utils.jQuerySelect("#dbUrlConfig");
                  this.dbDriverConfig = m_utils.jQuerySelect("#dbDriverConfig");
                  this.showHideOthersDbConfig(true);// hide by default; show
                  // only when others is
                  // selected
                  this.hostDbConfig = m_utils.jQuerySelect("#hostDbConfig");
                  this.portConfig = m_utils.jQuerySelect("#portConfig");
                  this.dbNameConfig = m_utils.jQuerySelect("#dbNameConfig");

                  this.databaseTypeSelect.empty();
                  this.databaseTypeSelect
                           .append("<option value='oracle'>Oracle</option>");
                  this.databaseTypeSelect.append("<option value='mysql'>Mysql</option>");
                  this.databaseTypeSelect
                           .append("<option value='postgres'>PostgreSQL</option>");
                  this.databaseTypeSelect
                           .append("<option value='others'>Others...</option>");

                  this.connectionTypeSelect.change(function()
                  {
                     if (self.connectionTypeSelect.val() == "direct")
                     {
                        self.jndiConfigTab.hide();
                        self.directConfigTab.show();
                     }
                     else if (self.connectionTypeSelect.val() == "jndi")
                     {
                        self.directConfigTab.hide();
                        self.jndiConfigTab.show();
                     }
                     self.view.submitModelElementAttributeChange(
                              "stardust:sqlScriptingOverlay::connectionType",
                              self.connectionTypeSelect.val());
                  });

                  this.databaseTypeSelect
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var submitElements = {};
                                       var attributes = event.data.panel.getApplication().attributes;
                                       attributes["stardust:sqlScriptingOverlay::databasetype"] = self.databaseTypeSelect
                                                .val();

                                       if (event.data.panel.databaseTypeSelect.val() == "others")
                                       {
                                          attributes["stardust:sqlScriptingOverlay::hostname"] = null;
                                          attributes["stardust:sqlScriptingOverlay::port"] = null;
                                          attributes["stardust:sqlScriptingOverlay::dbname"] = null;
                                          event.data.panel.showHideCommonDbConfig(true);
                                          event.data.panel.showHideOthersDbConfig();
                                       }
                                       else
                                       {
                                          attributes["stardust:sqlScriptingOverlay::url"] = null;
                                          attributes["stardust:sqlScriptingOverlay::driverClassName"] = null;
                                          event.data.panel.showHideCommonDbConfig();
                                          event.data.panel.showHideOthersDbConfig(true);
                                       }

                                       attributes["carnot:engine:camel::additionalSpringBeanDefinitions"] = event.data.panel
                                                .populateDataSourceBeanDefinition();
                                       submitElements.attributes = attributes;
                                       event.data.panel.view.submitChanges(
                                                submitElements, true);
                                       if (!event.data.panel.view.validate())
                                       {
                                          return;
                                       }
                                    });

                  this.urlInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var submitElements = {};
                                       var attributes = event.data.panel.getApplication().attributes;
                                       attributes["stardust:sqlScriptingOverlay::url"] = event.data.panel.urlInput
                                                .val();
                                       attributes["stardust:sqlScriptingOverlay::driverClassName"] = event.data.panel.driverInput
                                                .val();
                                       attributes["carnot:engine:camel::additionalSpringBeanDefinitions"] = event.data.panel
                                                .populateDataSourceBeanDefinition();
                                       attributes["carnot:engine:camel::routeEntries"] = self
                                                .getRoute(attributes);
                                       submitElements.attributes = attributes;
                                       event.data.panel.view.submitChanges(
                                                submitElements, true);
                                       if (!event.data.panel.view.validate())
                                       {
                                          return;
                                       }
                                    });

                  this.driverInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var submitElements = {};
                                       var attributes = event.data.panel.getApplication().attributes;
                                       attributes["stardust:sqlScriptingOverlay::url"] = event.data.panel.urlInput
                                                .val();
                                       attributes["stardust:sqlScriptingOverlay::driverClassName"] = event.data.panel.driverInput
                                                .val();
                                       attributes["carnot:engine:camel::additionalSpringBeanDefinitions"] = event.data.panel
                                                .populateDataSourceBeanDefinition();
                                       attributes["carnot:engine:camel::routeEntries"] = self
                                                .getRoute(attributes);
                                       submitElements.attributes = attributes;
                                       event.data.panel.view.submitChanges(
                                                submitElements, true);
                                       if (!event.data.panel.view.validate())
                                       {
                                          return;
                                       }
                                    });

                  this.hostInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var submitElements = {};
                                       var attributes = event.data.panel.getApplication().attributes;
                                       attributes["stardust:sqlScriptingOverlay::hostname"] = event.data.panel.hostInput
                                                .val();
                                       attributes["carnot:engine:camel::additionalSpringBeanDefinitions"] = event.data.panel
                                                .populateDataSourceBeanDefinition();
                                       attributes["carnot:engine:camel::routeEntries"] = self
                                                .getRoute(attributes);
                                       submitElements.attributes = attributes;
                                       event.data.panel.view.submitChanges(
                                                submitElements, true);
                                       if (!event.data.panel.view.validate())
                                       {
                                          return;
                                       }
                                    });

                  this.portInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var submitElements = {};
                                       var attributes = event.data.panel.getApplication().attributes;
                                       attributes["stardust:sqlScriptingOverlay::port"] = event.data.panel.portInput
                                                .val();
                                       attributes["carnot:engine:camel::additionalSpringBeanDefinitions"] = event.data.panel
                                                .populateDataSourceBeanDefinition();
                                       attributes["carnot:engine:camel::routeEntries"] = self
                                                .getRoute(attributes);
                                       submitElements.attributes = attributes;
                                       event.data.panel.view.submitChanges(
                                                submitElements, true);
                                       if (!event.data.panel.view.validate())
                                       {
                                          return;
                                       }
                                    });

                  this.dataBaseNameInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var submitElements = {};
                                       var attributes = event.data.panel.getApplication().attributes;
                                       attributes["stardust:sqlScriptingOverlay::dbname"] = event.data.panel.dataBaseNameInput
                                                .val();
                                       attributes["carnot:engine:camel::additionalSpringBeanDefinitions"] = event.data.panel
                                                .populateDataSourceBeanDefinition();
                                       attributes["carnot:engine:camel::routeEntries"] = self
                                                .getRoute(attributes);
                                       submitElements.attributes = attributes;
                                       event.data.panel.view.submitChanges(
                                                submitElements, true);
                                       if (!event.data.panel.view.validate())
                                       {
                                          return;
                                       }
                                    });

                  this.userNameInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var submitElements = {};
                                       var attributes = event.data.panel.getApplication().attributes;
                                       attributes["stardust:sqlScriptingOverlay::username"] = event.data.panel.userNameInput
                                                .val();
                                       attributes["carnot:engine:camel::additionalSpringBeanDefinitions"] = event.data.panel
                                                .populateDataSourceBeanDefinition();
                                       attributes["carnot:engine:camel::routeEntries"] = self
                                                .getRoute(attributes);
                                       submitElements.attributes = attributes;
                                       event.data.panel.view.submitChanges(
                                                submitElements, true);
                                       if (!event.data.panel.view.validate())
                                       {
                                          return;
                                       }
                                    });

                  this.passwordInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var submitElements = {};
                                       var attributes = event.data.panel.getApplication().attributes;
                                       attributes["stardust:sqlScriptingOverlay::password"] = event.data.panel.passwordInput
                                                .val();
                                       attributes["carnot:engine:camel::additionalSpringBeanDefinitions"] = event.data.panel
                                                .populateDataSourceBeanDefinition();
                                       attributes["carnot:engine:camel::routeEntries"] = self
                                                .getRoute(attributes);
                                       submitElements.attributes = attributes;
                                       event.data.panel.view.submitChanges(
                                                submitElements, true);
                                       if (!event.data.panel.view.validate())
                                       {
                                          return;
                                       }
                                    });

                  this.useCVforPassowrdInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       var submitElements = {};
                                       var attributes = event.data.panel.getApplication().attributes;
                                       attributes["stardust:sqlScriptingOverlay::useCVforPassowrd"] = event.data.panel.useCVforPassowrdInput
                                                .prop("checked") ? event.data.panel.useCVforPassowrdInput
                                                .prop("checked")
                                                : null;
                                       attributes["carnot:engine:camel::additionalSpringBeanDefinitions"] = event.data.panel
                                                .populateDataSourceBeanDefinition();
                                       attributes["carnot:engine:camel::routeEntries"] = self
                                                .getRoute(attributes);
                                       submitElements.attributes = attributes;
                                       event.data.panel.view.submitChanges(
                                                submitElements, true);
                                    });

                  var self = this;
                  this.parameterDefinitionNameInput = m_utils
                           .jQuerySelect("#parametersTab #parameterDefinitionNameInput");
                  this.codeEditor
                           .getEditor()
                           .on(
                                    'blur',
                                    function(e)
                                    {
                                       var submitElements = {};
                                       var attributes = self.getApplication().attributes;
                                       attributes["stardust:sqlScriptingOverlay::sqlQuery"] = self.codeEditor
                                                .getEditor().getSession().getValue();
                                       attributes["carnot:engine:camel::additionalSpringBeanDefinitions"] = self
                                                .populateDataSourceBeanDefinition();
                                       attributes["carnot:engine:camel::routeEntries"] = self
                                                .getRoute(attributes);
                                       submitElements.attributes = attributes;
                                       self.view.submitChanges(submitElements, false);
                                    });

                  this.inputBodyAccessPointInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       if (!event.data.panel.view.validate())
                                       {
                                          return;
                                       }
                                       var oldInBodyAccess;
                                       var filteredAccessPoints, inAccessPoints, outAccessPoints, accessPoints, inOutAccessPoints;
                                       var submitElements = {};
                                       var attributes = self.getApplication().attributes;
                                       var contexts = self.getApplication().contexts;

                                       accessPoints = self.getApplication().contexts.application.accessPoints;
                                       outAccessPoints = m_routeDefinitionUtils
                                                .getOutAccessPoints(self.getApplication().contexts.application.accessPoints);
                                       inAccessPoints = m_routeDefinitionUtils
                                                .getInAccessPoints(self.getApplication().contexts.application.accessPoints);

                                       // reset the value of
                                       // carnot:engine:camel::inBodyAccessPoint EA
                                       if (attributes["carnot:engine:camel::inBodyAccessPoint"])
                                       {
                                          oldInBodyAccess = attributes["carnot:engine:camel::inBodyAccessPoint"];
                                          attributes["carnot:engine:camel::inBodyAccessPoint"] = null;
                                       }

                                       if (self.inputBodyAccessPointInput.val() == m_constants.TO_BE_DEFINED)
                                       {
                                          filteredAccessPoints = m_routeDefinitionUtils
                                                   .filterAccessPoint(inAccessPoints,
                                                            oldInBodyAccess);
                                          attributes["carnot:engine:camel::inBodyAccessPoint"] = null;
                                       }
                                       else
                                       {
                                          structuredData = m_model
                                                   .findTypeDeclaration(self.inputBodyAccessPointInput
                                                            .val());
                                          filteredAccessPoints = m_routeDefinitionUtils
                                                   .filterAccessPoint(inAccessPoints,
                                                            oldInBodyAccess);

                                          if (structuredData)
                                          {
                                             var ap_id = structuredData.getFullId();
                                             var ap = m_routeDefinitionUtils
                                                      .findAccessPoint(
                                                               filteredAccessPoints,
                                                               ap_id);
                                             if (ap
                                                      && ap.direction == m_constants.IN_ACCESS_POINT)
                                             {
                                                // alreadyExists = true;
                                                m_utils.debug("Access Point " + ap.name
                                                         + " already exists")
                                             }
                                             else
                                             {
                                                filteredAccessPoints
                                                         .push({
                                                            id : ap_id,
                                                            name : structuredData.name,
                                                            dataType : "struct",
                                                            direction : "IN",
                                                            structuredDataTypeFullId : structuredData
                                                                     .getFullId(),
                                                            attributes : {
                                                               "stardust:predefined" : true
                                                            }
                                                         });
                                                attributes["carnot:engine:camel::inBodyAccessPoint"] = ap_id;
                                             }
                                          }
                                       }
                                       contexts.application.accessPoints = m_routeDefinitionUtils
                                                .addAll(filteredAccessPoints,
                                                         outAccessPoints);

                                       submitElements.attributes = attributes;
                                       submitElements.contexts = contexts;
                                       self.view.submitChanges(submitElements);
                                    });

                  this.outputBodyAccessPointInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       if (!event.data.panel.view.validate())
                                       {
                                          return;
                                       }

                                       var oldOutBodyAccess;
                                       var filteredAccessPoints, inAccessPoints, outAccessPoints, accessPoints, inOutAccessPoints;
                                       var submitElements = {};
                                       var attributes = self.getApplication().attributes;
                                       var contexts = self.getApplication().contexts;

                                       accessPoints = self.getApplication().contexts.application.accessPoints;
                                       outAccessPoints = m_routeDefinitionUtils
                                                .getOutAccessPoints(accessPoints);
                                       inAccessPoints = m_routeDefinitionUtils
                                                .getInAccessPoints(accessPoints);
                                       // reset the value of
                                       // carnot:engine:camel::outBodyAccessPoint EA
                                       if (attributes["carnot:engine:camel::outBodyAccessPoint"])
                                       {
                                          oldOutBodyAccess = attributes["carnot:engine:camel::outBodyAccessPoint"];
                                          attributes["carnot:engine:camel::outBodyAccessPoint"] = null;
                                       }

                                       if (self.outputBodyAccessPointInput.val() == m_constants.TO_BE_DEFINED)
                                       {
                                          var data = m_model.findData(oldOutBodyAccess);
                                          if (data && data.dataType == "primitive")
                                          {
                                             filteredAccessPoints = m_routeDefinitionUtils
                                                      .filterAccessPoint(outAccessPoints,
                                                               data.getFullId());
                                          }
                                          else
                                          {
                                             filteredAccessPoints = m_routeDefinitionUtils
                                                      .filterAccessPoint(outAccessPoints,
                                                               oldOutBodyAccess);
                                          }
                                          attributes["carnot:engine:camel::outBodyAccessPoint"] = null;
                                       }
                                       else
                                       {
                                          structuredData = m_model
                                                   .findData(self.outputBodyAccessPointInput
                                                            .val());
                                          filteredAccessPoints = m_routeDefinitionUtils
                                                   .filterAccessPoint(outAccessPoints,
                                                            oldOutBodyAccess);
                                          if (structuredData)
                                          {
                                             if (structuredData.dataType == "struct")
                                             {
                                                var ap_id = structuredData.structuredDataTypeFullId;
                                                var ap = m_routeDefinitionUtils
                                                         .findAccessPoint(
                                                                  filteredAccessPoints,
                                                                  ap_id);
                                                filteredAccessPoints.push({
                                                   id : structuredData.getFullId(),
                                                   name : structuredData.name,
                                                   dataType : "struct",
                                                   direction : "OUT",
                                                   structuredDataTypeFullId : ap_id,
                                                   attributes : {
                                                      "stardust:predefined" : true
                                                   }
                                                });
                                                attributes["carnot:engine:camel::outBodyAccessPoint"] = structuredData
                                                         .getFullId();
                                             }
                                             else if (structuredData.dataType == "primitive")
                                             {
                                                var ap_id = structuredData.getFullId();
                                                filteredAccessPoints
                                                         .push({
                                                            id : ap_id,
                                                            name : structuredData.name,
                                                            dataType : "primitive",
                                                            primitiveDataType : structuredData.primitiveDataType,
                                                            direction : "OUT",
                                                            attributes : {
                                                               "stardust:predefined" : true
                                                            }
                                                         });
                                                attributes["carnot:engine:camel::outBodyAccessPoint"] = structuredData
                                                         .getFullId();
                                             }
                                          }
                                       }
                                       contexts.application.accessPoints = m_routeDefinitionUtils
                                                .addAll(filteredAccessPoints,
                                                         inAccessPoints);
                                       submitElements.attributes = attributes;
                                       submitElements.contexts = contexts;
                                       self.view.submitChanges(submitElements);
                                    });

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

                  this.transactedRouteInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       if (!event.data.panel.view.validate())
                                       {
                                          return;
                                       }
                                       var submitElements = {};
                                       var attributes = event.data.panel.getApplication().attributes;
                                       attributes["carnot:engine:camel::transactedRoute"] = self.transactedRouteInput
                                                .prop('checked');
                                       submitElements.attributes = attributes;
                                       event.data.panel.view.submitChanges(
                                                submitElements, true);
                                    });
                  this.autoStartupInput
                           .change(
                                    {
                                       panel : this
                                    },
                                    function(event)
                                    {
                                       if (!event.data.panel.view.validate())
                                       {
                                          return;
                                       }
                                       var submitElements = {};
                                       var attributes = event.data.panel.getApplication().attributes;
                                       attributes["carnot:engine:camel::autoStartup"] = self.autoStartupInput
                                                .prop('checked');
                                       submitElements.attributes = attributes;
                                       event.data.panel.view.submitChanges(
                                                submitElements, true);
                                    });

                  this.update();
               };

               /**
                * 
                */
               SqlIntegrationOverlay.prototype.showHideOthersDbConfig = function(hide)
               {
                  if (hide)
                  {
                     this.dbUrlConfig.hide();
                     this.dbDriverConfig.hide();
                  }
                  else
                  {
                     this.dbUrlConfig.show();
                     this.dbDriverConfig.show();
                  }
               }
               SqlIntegrationOverlay.prototype.showHideCommonDbConfig = function(hide)
               {
                  if (hide)
                  {
                     this.hostDbConfig.hide();
                     this.portConfig.hide();
                     this.dbNameConfig.hide();
                  }
                  else
                  {
                     this.hostDbConfig.show();
                     this.portConfig.show();
                     this.dbNameConfig.show();
                  }
               }

               /**
                * 
                */
               SqlIntegrationOverlay.prototype.getModelElement = function()
               {
                  return this.view.getModelElement();
               };

               /**
                * 
                */
               SqlIntegrationOverlay.prototype.getApplication = function()
               {
                  return this.view.application;
               };

               /**
                * 
                */
               SqlIntegrationOverlay.prototype.getScopeModel = function()
               {
                  return this.view.getModelElement().model;
               };

               /**
                * 
                */
               SqlIntegrationOverlay.prototype.activate = function()
               {
                  if (this.view.getApplication().contexts.application.accessPoints.length == 0)
                  {
                     this.view
                              .submitChanges(
                                       {
                                          contexts : {
                                             application : {
                                                accessPoints : this
                                                         .createIntrinsicAccessPoints()
                                             }
                                          },
                                          attributes : {
                                             "carnot:engine:camel::camelContextId" : "defaultCamelContext",
                                             "carnot:engine:camel::invocationPattern" : "sendReceive",
                                             "carnot:engine:camel::invocationType" : "synchronous",
                                             "carnot:engine:camel::applicationIntegrationOverlay" : "sqlIntegrationOverlay"
                                          }
                                       }, true);
                  }
               };

               /**
                * Updates the application's view
                */
               SqlIntegrationOverlay.prototype.update = function()
               {
                  this.parameterDefinitionsPanel.setScopeModel(this.getScopeModel());
                  this.parameterDefinitionsPanel.setParameterDefinitions(this
                           .getApplication().contexts.application.accessPoints);
                  this.checkForDeprecatedEA("stardust:sqlScriptingOverlay::driver",
                           "stardust:sqlScriptingOverlay::driverClassName");
                  this.checkDeprecatedFormatForInBodyAccessPointEA();
                  this.checkDeprecatedFormatForOutBodyAccessPointEA();

                  this.integratorSetupRow.hide();
                  if (this.isIntegrator())
                     this.integratorSetupRow.show();
                  // Populate inputBodyAccessPointInput
                  this.inputBodyAccessPointInput.empty();
                  this.inputBodyAccessPointInput.append("<option value='"
                           + m_constants.TO_BE_DEFINED + "'>"
                           + m_i18nUtils.getProperty("None") + "</option>");
                  this.inputBodyAccessPointInput = this
                           .populateDataStructuresSelectInput(
                                    this.inputBodyAccessPointInput, "IN");
                  this.setInputTextFieldValue(this.inputBodyAccessPointInput,
                           "carnot:engine:camel::inBodyAccessPoint");
                  // Populate outputBodyAccessPointInput
                  this.outputBodyAccessPointInput.empty();
                  this.outputBodyAccessPointInput.append("<option value='"
                           + m_constants.TO_BE_DEFINED + "' selected>"
                           + m_i18nUtils.getProperty("None") + "</option>");
                  this.outputBodyAccessPointInput = this
                           .populateDataStructuresSelectInput(
                                    this.outputBodyAccessPointInput, "OUT");
                  this.setInputTextFieldValue(this.outputBodyAccessPointInput,
                           "carnot:engine:camel::outBodyAccessPoint");

                  this.setInputTextFieldValue(this.expectedResultSetInput,
                           "stardust:sqlScriptingOverlay::outputType",
                           m_constants.TO_BE_DEFINED);
                  this.codeEditor
                           .getEditor()
                           .getSession()
                           .setValue(
                                    this.getApplication().attributes["stardust:sqlScriptingOverlay::sqlQuery"]);

                  // Initialize the UI to show only primitives IN only
                  this.parameterDefinitionDirectionSelect = m_utils
                           .jQuerySelect("#parametersTab #parameterDefinitionDirectionSelect");
                  this.parameterDefinitionDirectionSelect.empty();
                  var direction = m_i18nUtils
                           .getProperty("modeler.element.properties.commonProperties.in");
                  this.parameterDefinitionDirectionSelect.append("<option value=\"IN\">"
                           + direction + "</option>");

                  this.dataTypeSelect = m_utils
                           .jQuerySelect("#parametersTab #dataTypeSelect");
                  this.dataTypeSelect.empty();
                  this.dataTypeSelect
                           .append("<option value='primitive' selected>"
                                    + m_i18nUtils
                                             .getProperty("modeler.element.properties.commonProperties.primitive")
                                    + "</option>");

                  this.setInputTextFieldValue(this.connectionTypeSelect,
                           "stardust:sqlScriptingOverlay::connectionType");
                  this.setInputTextFieldValue(this.databaseTypeSelect,
                           "stardust:sqlScriptingOverlay::databasetype");

                  if (this.getApplication().attributes["stardust:sqlScriptingOverlay::databasetype"] == "others")
                  {
                     this.setInputTextFieldValue(this.urlInput,
                              "stardust:sqlScriptingOverlay::url");
                     this.setInputTextFieldValue(this.driverInput,
                              "stardust:sqlScriptingOverlay::driverClassName");
                     this.showHideCommonDbConfig(true);
                     this.showHideOthersDbConfig(false);
                  }
                  else
                  {
                     this.showHideCommonDbConfig(false);
                     this.showHideOthersDbConfig(true);
                     this.setInputTextFieldValue(this.hostInput,
                              "stardust:sqlScriptingOverlay::hostname");
                     this.setInputTextFieldValue(this.portInput,
                              "stardust:sqlScriptingOverlay::port");
                     this.setInputTextFieldValue(this.dataBaseNameInput,
                              "stardust:sqlScriptingOverlay::dbname");

                  }

                  this.setInputTextFieldValue(this.userNameInput,
                           "stardust:sqlScriptingOverlay::username");
                  this.setInputTextFieldValue(this.passwordInput,
                           "stardust:sqlScriptingOverlay::password");

                  this.setCheckbox(this.useCVforPassowrdInput,
                           "stardust:sqlScriptingOverlay::useCVforPassowrd", false);
                  this.setCheckbox(this.transactedRouteInput,
                           "carnot:engine:camel::transactedRoute", true);
                  this.setCheckbox(this.autoStartupInput,
                           "carnot:engine:camel::autoStartup", true);

                  this.parameterDefinitionsTableBody = this.parameterDefinitionsPanel.parameterDefinitionsTableBody;
                  this.initializeParameterDefinitionsTable();
                  this.parameterDefinitionsPanel.selectCurrentParameterDefinition();

               };
               /**
                * This method is used to check old Extended Attributes. if one is found
                * its value will be copied to the new EA.
                * 
                */
               SqlIntegrationOverlay.prototype.checkDeprecatedFormatForInBodyAccessPointEA = function()
               {
                  var submitElements = {};
                  var attributes = this.getApplication().attributes;
                  var value = this.getApplication().attributes["carnot:engine:camel::inBodyAccessPoint"];
                  if (value && value.indexOf(":") == -1)
                  {
                     var submitElements = {};
                     attributes["carnot:engine:camel::inBodyAccessPoint"] = this
                              .getScopeModel().id
                              + ":" + value;
                     submitElements.attributes = attributes;
                     this.submitChanges(submitElements, true);
                  }

               }
               /**
                * This method is used to check old Extended Attributes. if one is found
                * its value will be copied to the new EA.
                * 
                */
               SqlIntegrationOverlay.prototype.checkDeprecatedFormatForOutBodyAccessPointEA = function()
               {
                  var submitElements = {};
                  var attributes = this.getApplication().attributes;
                  var value = this.getApplication().attributes["carnot:engine:camel::outBodyAccessPoint"];
                  if (value)
                  {
                     var fullDataId=value;
                     if(m_model.stripModelId(value)&& !m_model.stripElementId(value))
                        fullDataId=this.getScopeModel().id+":"+value;
                     var data = m_model.findData(fullDataId);
                     if (data)
                     {
                        var submitElements = {};
                        attributes["carnot:engine:camel::outBodyAccessPoint"] = data
                                 .getFullId();
                        submitElements.attributes = attributes;
                        this.submitChanges(submitElements, true);
                     }
                  }
               }

               /**
                * This method is used to check old Extended Attributes. if one is found
                * its value will be copied to the new EA.
                * 
                */
               SqlIntegrationOverlay.prototype.checkForDeprecatedEA = function(
                        deprecatedEA, newExtendedAttribute)
               {
                  var submitElements = {};
                  var attributes = this.getApplication().attributes;
                  var value = this.getApplication().attributes[deprecatedEA];
                  if (value)
                  {
                     var submitElements = {};
                     attributes[newExtendedAttribute] = value;
                     attributes[deprecatedEA] = null;
                     submitElements.attributes = attributes;
                     this.submitChanges(submitElements, true);
                  }
               }

               SqlIntegrationOverlay.prototype.setInputTextFieldValue = function(
                        inputText, extendedAttribute, defaultValue)
               {
                  var value = this.getApplication().attributes[extendedAttribute];
                  if (value != null)
                     this.setInputTextFieldValue(inputText, extendedAttribute);
                  else
                     inputText.val(defaultValue);
               }

               SqlIntegrationOverlay.prototype.setInputTextFieldValue = function(
                        inputText, extendedAttribute)
               {
                  var value = this.getApplication().attributes[extendedAttribute];
                  inputText.val(value);
               }

               SqlIntegrationOverlay.prototype.setCheckbox = function(inputField,
                        extendedAttribute, defaultValue)
               {
                  var extendedAttributeValue = this.getApplication().attributes[extendedAttribute];
                  if (extendedAttributeValue == null
                           || extendedAttributeValue === undefined)
                  {
                     this.view.submitModelElementAttributeChange(extendedAttribute,
                              defaultValue);
                  }
                  inputField.prop("checked", extendedAttributeValue);
               }

               SqlIntegrationOverlay.prototype.initializeParameterDefinitionsTable = function()
               {
                  this.parameterDefinitionsTableBody.empty();

                  for (var m = 0; m < this.parameterDefinitionsPanel.parameterDefinitions.length; ++m)
                  {
                     var parameterDefinition = this.parameterDefinitionsPanel.parameterDefinitions[m];

                     var fullParameterDefinitionId=parameterDefinition.id;
                     if(m_model.stripModelId(parameterDefinition.id)&& !m_model.stripElementId(parameterDefinition.id))
                        fullParameterDefinitionId=this.getScopeModel().id+":"+parameterDefinition.id;
                     
                     if (fullParameterDefinitionId == this.inputBodyAccessPointInput.val()
                              || fullParameterDefinitionId == this.outputBodyAccessPointInput
                                       .val())
                     {
                        continue;
                     }
                     else
                     {
                        var content = "<tr id=\"" + m + "\">";

                        content += "<td class=\"";

                        if (parameterDefinition.direction == "IN")
                        {
                           if (this.parameterDefinitionsPanel.options.supportsDescriptors)
                           {
                              if (parameterDefinition.descriptor)
                              {
                                 content += "descriptorDataPathListItem";
                              }
                              else if (parameterDefinition.keyDescriptor)
                              {
                                 content += "keyDescriptorDataPathListItem";
                              }
                              else
                              {
                                 content += "inDataPathListItem";
                              }
                           }
                           else
                           {
                              content += "inDataPathListItem";
                           }
                        }
                        else if (parameterDefinition.direction == "INOUT")
                        {
                           content += "inoutDataPathListItem";
                        }
                        else
                        {
                           content += "outDataPathListItem";
                        }

                        content += "\" style=\"width: "
                                 + this.parameterDefinitionsPanel.options.directionColumnWidth
                                 + "\"></td>";
                        content += "<td style=\"width: "
                                 + this.parameterDefinitionsPanel.options.nameColumnWidth
                                 + "\">" + parameterDefinition.name;
                        content += "</td>";

                        if (this.parameterDefinitionsPanel.options.supportsDataTypeSelection)
                        {
                           content += "<td style=\"width: "
                                    + this.parameterDefinitionsPanel.options.typeColumnWidth
                                    + "\">";
                           if (parameterDefinition.dataType == m_constants.PRIMITIVE_DATA_TYPE)
                           {
                              content += m_typeDeclaration
                                       .getPrimitiveTypeLabel(parameterDefinition.primitiveDataType);
                           }
                           else
                           {
                              if (parameterDefinition.structuredDataTypeFullId)
                              {
                                 content += m_model
                                          .stripElementId(parameterDefinition.structuredDataTypeFullId);
                              }
                           }
                           content += "</td>";
                        }
                        if (this.parameterDefinitionsPanel.options.supportsDataMappings)
                        {
                           content += "<td style=\"width: "
                                    + this.parameterDefinitionsPanel.options.mappingColumnWidth
                                    + "\">";

                           if (parameterDefinition.dataFullId != null
                                    && m_model.findData(parameterDefinition.dataFullId))
                           {
                              var data = m_model.findData(parameterDefinition.dataFullId);
                              content += data.name;
                              if (this.options.supportsDataPathes)
                              {
                                 if (parameterDefinition.dataPath != null)
                                 {
                                    content += ".";
                                    content += parameterDefinition.dataPath;
                                 }
                              }
                           }
                           content += "</td>";
                        }
                        var newValue = m_i18nUtils
                                 .getProperty("modeler.element.properties.commonProperties.inputText.new");
                        content = content.replace(">New", ">" + newValue);
                        newValue = m_i18nUtils
                                 .getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.element.selectType.string");
                        content = content.replace("String", newValue);
                        this.parameterDefinitionsTableBody.append(content);
                        m_utils
                                 .jQuerySelect(
                                          this.parameterDefinitionsPanel.options.scope
                                                   + "table#parameterDefinitionsTable tr")
                                 .mousedown(
                                          {
                                             panel : this
                                          },
                                          function(event)
                                          {
                                             event.data.panel.parameterDefinitionsPanel
                                                      .deselectParameterDefinitions()
                                             m_utils.jQuerySelect(this).addClass(
                                                      "selected");

                                             var index = m_utils.jQuerySelect(this).attr(
                                                      "id");
                                             event.data.panel.parameterDefinitionsPanel.currentParameterDefinition = event.data.panel.parameterDefinitionsPanel.parameterDefinitions[index];
                                             event.data.panel.parameterDefinitionsPanel.selectedRowIndex = index;
                                             event.data.panel.parameterDefinitionsPanel
                                                      .populateParameterDefinitionFields();
                                          });
                     }
                  }
                  return parameterDefinitionsTable;
               }

               SqlIntegrationOverlay.prototype.createIntrinsicAccessPoints = function()
               {
                  var accessPoints = this.getApplication().contexts.application.accessPoints;
                  var defaultAccessPoints = this.getApplication().contexts.application.accessPoints;
                  var addCamelSqlQueryVar = true, addCamelSqlUpdateCountVar = true, addCamelSqlRowCountVar = true;

                  for (var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n)
                  {
                     var parameterDefinition = this.getApplication().contexts.application.accessPoints[n];
                     if (parameterDefinition.id == "CamelSqlQuery")
                     {
                        addCamelSqlQueryVar = false;
                        continue;
                     }
                     if (parameterDefinition.id == "CamelSqlUpdateCount")
                     {
                        addCamelSqlUpdateCountVar = false;
                        continue;
                     }
                     if (parameterDefinition.id == "CamelSqlRowCount")
                     {
                        addCamelSqlRowCountVar = false;
                        continue;
                     }
                  }

                  if (!accessPoints["CamelSqlQuery"] && addCamelSqlQueryVar)
                  {
                     defaultAccessPoints.push({
                        id : "CamelSqlQuery",
                        name : "SqlQuery",
                        dataType : "primitive",
                        primitiveDataType : "String",
                        direction : "IN",
                        attributes : {
                           "stardust:predefined" : true
                        }
                     });
                  }
                  if (!accessPoints["CamelSqlUpdateCount"] && addCamelSqlQueryVar)
                  {
                     defaultAccessPoints.push({
                        id : "CamelSqlUpdateCount",
                        name : "SqlUpdateCount",
                        dataType : "primitive",
                        primitiveDataType : "int",
                        direction : "OUT",
                        attributes : {
                           "stardust:predefined" : true
                        }
                     });
                  }
                  if (!accessPoints["CamelSqlRowCount"] && addCamelSqlQueryVar)
                  {
                     defaultAccessPoints.push({
                        id : "CamelSqlRowCount",
                        name : "SqlRowCount",
                        dataType : "primitive",
                        primitiveDataType : "int",
                        direction : "OUT",
                        attributes : {
                           "stardust:predefined" : true
                        }
                     });
                  }

                  return defaultAccessPoints;
               }
               /**
                * will populate structuredDataTypeSelect (IN/OUT) for OUT direction we
                * need to get all data items listed because we can map the reponse to a
                * primitive data for IN only structred datatypes will be provided, because
                * they will be mapped to an exchange header an used later in the query.
                */
               SqlIntegrationOverlay.prototype.populateDataStructuresSelectInput = function(
                        structuredDataTypeSelect, direction)
               {
                  var scopeModel = this.getScopeModel();
                  structuredDataTypeSelect.empty();
                  structuredDataTypeSelect.append("<option value='"
                           + m_constants.TO_BE_DEFINED + "'>"
                           + m_i18nUtils.getProperty("modeler.general.toBeDefined")
                           + "</option>");
                  structuredDataTypeSelect.append("<optgroup label='"
                           + m_i18nUtils.getProperty("modeler.general.thisModel") + "'>");
                  if (direction == "IN")
                  {
                     for ( var i in scopeModel.typeDeclarations)
                     {
                        if (!scopeModel.typeDeclarations[i].isSequence())
                           continue;
                        structuredDataTypeSelect.append("<option value='"
                                 + scopeModel.typeDeclarations[i].getFullId() + "'>"
                                 + scopeModel.typeDeclarations[i].name + "</option>");
                     }
                     var othermodel = m_i18nUtils
                              .getProperty("modeler.element.properties.commonProperties.otherModel")
                     structuredDataTypeSelect.append("</optgroup><optgroup label=\""
                              + othermodel + "\">");

                     for ( var n in m_model.getModels())
                     {
                        var model = m_model.getModels()[n]
                        if (scopeModel && model.id == scopeModel.id)
                        {
                           continue;
                        }
                        for ( var m in model.typeDeclarations)
                        {
                           var typeDeclaration = model.typeDeclarations[m];
                           if (!typeDeclaration.isSequence())
                              continue;
                           structuredDataTypeSelect.append("<option value='"
                                    + typeDeclaration.getFullId() + "'>" + model.name
                                    + "/" + typeDeclaration.name + "</option>");
                        }
                     }
                  }
                  else if (direction == "OUT")
                  {
                     for ( var i in scopeModel.dataItems)
                     {
                        if (scopeModel.dataItems[i].dataType == "struct")
                        {
                           structuredDataTypeSelect.append("<option value='"
                                    + scopeModel.dataItems[i].getFullId() + "'>"
                                    + scopeModel.dataItems[i].name + "</option>");
                        }
                        else
                        {
                           structuredDataTypeSelect.append("<option value='"
                                    + scopeModel.dataItems[i].getFullId() + "'>"
                                    + scopeModel.dataItems[i].name + "</option>");
                        }
                     }

                     var othermodel = m_i18nUtils
                              .getProperty("modeler.element.properties.commonProperties.otherModel")
                     structuredDataTypeSelect.append("</optgroup><optgroup label=\""
                              + othermodel + "\">");

                     for ( var n in m_model.getModels())
                     {
                        if (scopeModel && m_model.getModels()[n].id == scopeModel.id)
                        {
                           continue;
                        }
                        for ( var m in m_model.getModels()[n].dataItems)
                        {
                           var dataItem = m_model.getModels()[n].dataItems[m];
                           structuredDataTypeSelect.append("<option value='"
                                    + dataItem.getFullId() + "'>"
                                    + m_model.getModels()[n].name + "/" + dataItem.name
                                    + "</option>");
                        }
                     }
                  }

                  structuredDataTypeSelect.append("</optgroup>");
                  return structuredDataTypeSelect;
               };

               SqlIntegrationOverlay.prototype.populateDataSourceBeanDefinition = function()
               {
                  if (!this.view.validate())
                  {
                     return;
                  }

                  var beanDefinition = "";
                  var driverClassName = "";
                  var url = "";
                  if (this.databaseTypeSelect.val() == "oracle")
                  {
                     driverClassName = "oracle.jdbc.driver.OracleDriver";
                     url = "jdbc:oracle:thin:@" + this.hostInput.val() + ":"
                              + this.portInput.val() + ":" + this.dataBaseNameInput.val();
                  }
                  else if (this.databaseTypeSelect.val() == "mysql")
                  {
                     driverClassName = "com.mysql.jdbc.Driver";
                     url = "jdbc:mysql://" + this.hostInput.val() + ":"
                              + this.portInput.val() + "/" + this.dataBaseNameInput.val();
                  }
                  else if (this.databaseTypeSelect.val() == "postgres")
                  {
                     driverClassName = "org.postgresql.Driver";
                     url = "jdbc:postgresql://" + this.hostInput.val() + ":"
                              + this.portInput.val() + "/" + this.dataBaseNameInput.val();
                  }
                  else if (this.databaseTypeSelect.val() == "others")
                  {
                     driverClassName = this.driverInput.val();
                     url = this.urlInput.val();
                  }

                  beanDefinition += "<bean id=\""
                           + this.getDataSourceName()
                           + "\" class=\"org.apache.commons.dbcp.BasicDataSource\" destroy-method=\"close\">";
                  beanDefinition += "<property name=\"url\" value=\"" + url + "\" />";
                  beanDefinition += "<property name=\"driverClassName\" value=\""
                           + driverClassName + "\" />";
                  if (this.userNameInput.val() != "")
                  {
                     beanDefinition += "<property name=\"username\" value=\""
                              + this.userNameInput.val() + "\" />";
                  }
                  if (this.useCVforPassowrdInput.prop("checked")
                           && this.passwordInput.val() != "")
                  {
                     beanDefinition += "<property name=\"password\" value=\"${"
                              + this.passwordInput.val() + ":Password}\" />";
                  }
                  else
                  {
                     if (this.passwordInput.val() != "")
                     {
                        beanDefinition += "<property name=\"password\" value=\""
                                 + this.passwordInput.val() + "\" />";
                     }else{
                        beanDefinition += "<property name=\"password\" value=\"\" />";
                     }
                  }
                  beanDefinition += "</bean>";
                  return beanDefinition;
               }
               SqlIntegrationOverlay.prototype.getDataSourceName = function()
               {
                  return this.getApplication().id + "Ds";
               }
               /**
                * 
                */
               SqlIntegrationOverlay.prototype.getRoute = function(attributes)
               {
                  if (!attributes)
                     attributes = this.getApplication().attributes;
                  var route = "";
                  var sqlQuery = attributes["stardust:sqlScriptingOverlay::sqlQuery"];// this.codeEditor.getEditor().getSession().getValue();
                  var dataSourceName = "";
                  if (sqlQuery != null && sqlQuery != "")
                  {

                     sqlQuery = m_utils.encodeXmlPredfinedCharacters(sqlQuery);
                  }
                  var questionMarkExists = false;

                  if (attributes["stardust:sqlScriptingOverlay::outputType"] != null
                           && attributes["stardust:sqlScriptingOverlay::outputType"] != ""
                           && attributes["stardust:sqlScriptingOverlay::outputType"] == "SelectOne")
                  {
                     sqlQuery += "?outputType=SelectOne";
                  }
                  else if (attributes["stardust:sqlScriptingOverlay::outputType"] != null
                           && attributes["stardust:sqlScriptingOverlay::outputType"] != ""
                           && attributes["stardust:sqlScriptingOverlay::outputType"] == "SelectList")
                  {
                     sqlQuery += "?outputType=SelectList";
                  }

                  if (sqlQuery != null && sqlQuery != "" && sqlQuery.indexOf('?') != -1)
                  {
                     questionMarkExists = true;
                  }
                  if (questionMarkExists)
                     route += "<to uri=\"sql:"
                              + sqlQuery
                              + "&dataSource=#"
                              + this.getDataSourceName()
                              + "&alwaysPopulateStatement=true&prepareStatementStrategy=#sqlPrepareStatementStrategy\" />"
                  else
                     route += "<to uri=\"sql:"
                              + sqlQuery
                              + "?dataSource=#"
                              + this.getDataSourceName()
                              + "&alwaysPopulateStatement=true&prepareStatementStrategy=#sqlPrepareStatementStrategy\" />";

                  route += "<to uri=\"bean:bpmTypeConverter?method=fromList\"/>"

                  m_utils.debug(route);
                  route = route.replace(/&/g, "&amp;");
                  return route;
               };

               /**
                * 
                */
               SqlIntegrationOverlay.prototype.submitChanges = function()
               {
                  // this.view
                  // .submitChanges({
                  // attributes : {
                  // "carnot:engine:camel::applicationIntegrationOverlay" :
                  // "sqlIntegrationOverlay",
                  // "carnot:engine:camel::camelContextId" : "defaultCamelContext",
                  // "carnot:engine:camel::invocationPattern" : "sendReceive",
                  // "carnot:engine:camel::invocationType" : "synchronous",
                  // "carnot:engine:camel::inBodyAccessPoint" :
                  // (this.inputBodyAccessPointInput
                  // .val() != null && this.inputBodyAccessPointInput
                  // .val() != m_constants.TO_BE_DEFINED) ? this.inputBodyAccessPointInput
                  // .val()
                  // : null,
                  // "carnot:engine:camel::outBodyAccessPoint" :
                  // (this.outputBodyAccessPointInput
                  // .val() != null && this.outputBodyAccessPointInput
                  // .val() != m_constants.TO_BE_DEFINED) ?
                  // this.outputBodyAccessPointInput
                  // .val()
                  // : null,
                  // "stardust:sqlScriptingOverlay::sqlQuery" : this.codeEditor
                  // .getEditor().getSession().getValue(),
                  // "stardust:sqlScriptingOverlay::connectionType" :
                  // this.connectionTypeSelect
                  // .val(),
                  // "stardust:sqlScriptingOverlay::databasetype" :
                  // this.databaseTypeSelect
                  // .val(),
                  // "stardust:sqlScriptingOverlay::hostname" : this.hostInput
                  // .val(),
                  // "stardust:sqlScriptingOverlay::port" : this.portInput
                  // .val(),
                  // "stardust:sqlScriptingOverlay::dbname" : this.dataBaseNameInput
                  // .val(),
                  // "stardust:sqlScriptingOverlay::username" : this.userNameInput
                  // .val(),
                  // "stardust:sqlScriptingOverlay::useCVforPassowrd" :
                  // this.useCVforPassowrdInput
                  // .prop("checked") ? this.useCVforPassowrdInput
                  // .prop("checked") : null,
                  // "stardust:sqlScriptingOverlay::password" : this.passwordInput
                  // .val(),
                  //
                  // "carnot:engine:camel::additionalSpringBeanDefinitions" : this
                  // .populateDataSourceBeanDefinition(),
                  // "carnot:engine:camel::routeEntries" : this.getRoute(),
                  // "stardust:sqlScriptingOverlay::url" : this.urlInput
                  // .val(),
                  // "stardust:sqlScriptingOverlay::driverClassName" : this.driverInput
                  // .val()
                  // }
                  // });
               };
               /**
                * 
                */
               SqlIntegrationOverlay.prototype.submitParameterDefinitionsChanges = function(
                        parameterDefinitionsChanges)
               {
                  var submitElements = {};
                  submitElements.contexts = {
                     application : {
                        accessPoints : parameterDefinitionsChanges
                     }
                  };
                  this.view.submitChanges(submitElements, true);
               };
               /**
                * 
                */
               SqlIntegrationOverlay.prototype.validate = function()
               {
                  var valid = true;
                  if (m_utils.isEmptyString(this.codeEditor.getEditor().getSession()
                           .getValue()))
                  {
                     this.view.errorMessages.push("No SQL Query provided.");
                     valid = false;
                  }

                  if (this.connectionTypeSelect.val() == "direct"
                           && (this.databaseTypeSelect.val() != "others" && this.databaseTypeSelect
                                    .val() != m_constants.TO_BE_DEFINED))
                  {
                     // when using direct connection verify host,port,databasename
                     this.showHideCommonDbConfig();
                     this.showHideOthersDbConfig(true);
                     this.hostInput.removeClass("error");
                     this.portInput.removeClass("error");
                     this.dataBaseNameInput.removeClass("error");
                     if (m_utils.isEmptyString(this.hostInput.val()))
                     {
                        this.view.errorMessages.push("No Data Source Host provided.");
                        this.hostInput.addClass("error");
                        valid = false;
                     }
                     var numRegexp = new RegExp("[^0-9]");
                     if (numRegexp.test(this.portInput.val())
                              || (m_utils.isEmptyString(this.portInput.val()))
                              || (Number(this.portInput.val()) < 1 || Number(this.portInput
                                       .val()) > 65535))
                     {
                        this.view.errorMessages
                                 .push("Port number should be from 1-65535.");
                        this.portInput.addClass("error");
                        valid = false;
                     }
                     if (m_utils.isEmptyString(this.dataBaseNameInput.val()))
                     {
                        this.view.errorMessages.push("No Data Source Name provided.");
                        this.dataBaseNameInput.addClass("error");
                        valid = false;
                     }
                  }
                  if (this.connectionTypeSelect.val() == "direct"
                           && (this.databaseTypeSelect.val() == "others"))
                  {
                     // when using others connection verify url/driver
                     this.showHideCommonDbConfig(true);
                     this.showHideOthersDbConfig();
                     this.urlInput.removeClass("error");
                     this.driverInput.removeClass("error");
                     if (m_utils.isEmptyString(this.urlInput.val()))
                     {
                        this.view.errorMessages.push("No URL provided.");
                        this.urlInput.addClass("error");
                        valid = false;
                     }
                     if (m_utils.isEmptyString(this.driverInput.val()))
                     {
                        this.view.errorMessages.push("No Driver provided.");
                        this.driverInput.addClass("error");
                        valid = false;
                     }
                  }
                  this.parameterDefinitionNameInput.removeClass("error");
                  var parameterDefinitionNameInputWhithoutSpaces = this.parameterDefinitionNameInput
                           .val().replace(/ /g, "");
                  if ((parameterDefinitionNameInputWhithoutSpaces.indexOf("-") != -1)
                           || (parameterDefinitionNameInputWhithoutSpaces == "exchange")
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
                  return valid;
               };

               SqlIntegrationOverlay.prototype.isIntegrator = function()
               {
                  return m_user.getCurrentRole() == m_constants.INTEGRATOR_ROLE;
               }
            }
         });