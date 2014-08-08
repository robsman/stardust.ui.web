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
 * SMS Overlay
 * 
 * @author
 */
define(
         [ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_i18nUtils",
                  "bpm-modeler/js/m_constants", "bpm-modeler/js/m_commandsController",
                  "bpm-modeler/js/m_command", "bpm-modeler/js/m_model",
                  "bpm-modeler/js/m_accessPoint", "bpm-modeler/js/m_typeDeclaration",
                  "bpm-modeler/js/m_parameterDefinitionsPanel",
                  "bpm-modeler/js/m_codeEditorAce" ],
         function(m_utils, m_i18nUtils, m_constants, m_commandsController, m_command,
                  m_model, m_accessPoint, m_typeDeclaration, m_parameterDefinitionsPanel,
                  m_codeEditorAce)
         {
            return {
               create : function(view)
               {
                  var overlay = new SmsIntegrationOverlay();

                  overlay.initialize(view);

                  return overlay;
               }
            };

            /**
             * 
             */
            function SmsIntegrationOverlay()
            {
               /**
                * 
                */
               SmsIntegrationOverlay.prototype.initialize = function(view)
               {
                  this.view = view;
                  this.view
                           .insertPropertiesTab(
                                    "smsIntegrationOverlay",
                                    "parameters",
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.sms.parameters.title"),
                                    "plugins/bpm-modeler/images/icons/database_link.png");
                  this.view
                           .insertPropertiesTab(
                                    "smsIntegrationOverlay",
                                    "dataSource",
                                    m_i18nUtils
                                             .getProperty("modeler.model.applicationOverlay.sms.datasource.title"),
                                    "plugins/bpm-modeler/images/icons/database_link.png");
                  //this.sqlQueryHeading = m_utils.jQuerySelect("#smsIntegrationOverlay #sqlQueryHeading");
                  this.hostNameInput = m_utils.jQuerySelect("#dataSourceTab #hostNameInput");
                  this.portInput = m_utils.jQuerySelect("#dataSourceTab #portInput");
                  this.userNameInput = m_utils.jQuerySelect("#dataSourceTab #userNameInput");
                  this.passowrdInput = m_utils.jQuerySelect("#dataSourceTab #passowrdInput");
                  this.useCVforPassowrdInput = m_utils.jQuerySelect("#dataSourceTab #useCVforPassowrdInput");
                  this.sourceAddressInput = m_utils.jQuerySelect("#dataSourceTab #sourceAddressInput");
                  this.destinationAddressInput = m_utils.jQuerySelect("#dataSourceTab #destinationAddressInput");
                  this.useSSLInput = m_utils.jQuerySelect("#dataSourceTab #useSSLInput");
                  this.inputBodyAccessPointInput = m_utils.jQuerySelect("#parametersTab #inputBodyAccessPointInput");
                  
                  this.editorAnchor = m_utils.jQuerySelect("#codeEditorDiv").get(0);
                  this.editorAnchor.id = "codeEditorDiv"
                           + Math.floor((Math.random() * 100000) + 1);

                  this.codeEditor = m_codeEditorAce.getSQLCodeEditor(this.editorAnchor.id);
                  this.codeEditor.loadLanguageTools();

                  var self = this;
                  this.inputBodyAccessPointInput
                           .change(function()
                           {
                              if (!self.view.validate())
                              {
                                 return;
                              }

                              if (self.inputBodyAccessPointInput.val() == m_constants.TO_BE_DEFINED)
                              {
                                 self.submitChanges({
                                    contexts : {
                                    attributes : {
                                       "carnot:engine:camel::inBodyAccessPoint":
                                       null}
                                    }
                                 });
                              }
                              else
                              {
                                 self
                                 .submitChanges({
                                    contexts : {
                                    attributes : {
                                       "carnot:engine:camel::inBodyAccessPoint":
                                       self.inputBodyAccessPointInput.val()}
                                    }
                                 });
                              }
                           });
                  this.codeEditor.getEditor().on('blur', function(e)
                  {
                     self.submitChanges();
                  });

                  this.hostNameInput
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
                  this.portInput
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
                  this.userNameInput
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
                  this.passowrdInput
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
                  this.sourceAddressInput
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
                  this.useCVforPassowrdInput
                  .change(
                           {
                              panel : this
                           },
                           function(event)
                           {
                              event.data.panel
                                       .submitChanges({
                                          modelElement : {
                                             attributes : {
                                                "stardust:smsIntegrationOverlay::useCVforPassowrd" : event.data.panel.useCVforPassowrdInput
                                                         .prop("checked") ? event.data.panel.useCVforPassowrdInput
                                                         .prop("checked")
                                                         : null

                                             }
                                          }
                                       });
                           });
                  this.destinationAddressInput
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
                  
                  this.useSSLInput
                  .change(
                        {
                           panel : this
                        },
                        function(event) {
                           event.data.panel.submitChanges();
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
                  this.deleteParameterDefinitionButton = m_utils
                           .jQuerySelect("#propertiesTabs #parametersTab #deleteParameterDefinitionButton");
             //     this.update();
               };
               /**
                * 
                */
               SmsIntegrationOverlay.prototype.getModelElement = function()
               {
                  return this.view.getModelElement();
               };

               /**
                * 
                */
               SmsIntegrationOverlay.prototype.getApplication = function()
               {
                  return this.view.application;
               };

               /**
                * 
                */
               SmsIntegrationOverlay.prototype.getScopeModel = function()
               {
                  return this.view.getModelElement().model;
               };
               /**
                * 
                */
               SmsIntegrationOverlay.prototype.activate = function()
               {
                  if (this.view.getApplication().contexts.application.accessPoints.length == 0)
                  {
                     this.view
                              .submitChanges({
                                 /*
                                  * contexts : { application : { accessPoints :
                                  * this.createIntrinsicAccessPoints() } },
                                  */
                                 attributes : {
                                    "carnot:engine:camel::camelContextId" : "defaultCamelContext",
                                    "carnot:engine:camel::invocationPattern" : "sendReceive",
                                    "carnot:engine:camel::invocationType" : "synchronous",
                                    "carnot:engine:camel::applicationIntegrationOverlay" : "smsIntegrationOverlay"
                                 }
                              });
                  }
               };
               /**
                * 
                */
               SmsIntegrationOverlay.prototype.update = function()
               {
                  this.parameterDefinitionsPanel.setScopeModel(this.getScopeModel());
                  this.parameterDefinitionsPanel.setParameterDefinitions(this
                           .getApplication().contexts.application.accessPoints);
                  this.codeEditor
                           .getEditor()
                           .getSession()
                           .setValue(
                                    this.getApplication().attributes["stardust:smsIntegrationOverlay::messagecontent"]);

                  // Initialize the UI to show only primitives IN only
                  this.parameterDefinitionDirectionSelect = m_utils
                           .jQuerySelect("#parametersTab #parameterDefinitionDirectionSelect");
                  this.parameterDefinitionDirectionSelect.empty();
                  var direction = m_i18nUtils
                           .getProperty("modeler.element.properties.commonProperties.in");
                  this.parameterDefinitionDirectionSelect.append("<option value=\"IN\">"
                           + direction + "</option>");
                  this.inputBodyAccessPointInput.empty();
                  this.inputBodyAccessPointInput.append("<option value='"
                           + m_constants.TO_BE_DEFINED + "'>"
                           + m_i18nUtils.getProperty("None")
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

                  this.inputBodyAccessPointInput
                           .val(this.getApplication().attributes["carnot:engine:camel::inBodyAccessPoint"]);
                  this.useCVforPassowrdInput
                  .prop(
                           "checked",
                           this.getApplication().attributes["stardust:smsIntegrationOverlay::useCVforPassowrd"]);
                  this.hostNameInput
                  .val(this.getApplication().attributes["stardust:smsIntegrationOverlay::messagecontent"]);
                  this.hostNameInput
                  .val(this.getApplication().attributes["stardust:smsIntegrationOverlay::hostname"]);
                  this.portInput
                  .val(this.getApplication().attributes["stardust:smsIntegrationOverlay::port"]);
                  this.userNameInput
                  .val(this.getApplication().attributes["stardust:smsIntegrationOverlay::username"]);
                  this.passowrdInput
                  .val(this.getApplication().attributes["stardust:smsIntegrationOverlay::password"]);
                  this.sourceAddressInput
                  .val(this.getApplication().attributes["stardust:smsIntegrationOverlay::sourceaddress"]);
                  this.destinationAddressInput
                  .val(this.getApplication().attributes["stardust:smsIntegrationOverlay::destinationaddress"]);
                  this.inputBodyAccessPointInput
                  .val(this.getApplication().attributes["stardust:smsIntegrationOverlay::usessl"]);
               };
               /**
                * returns camel route definition
                */
               SmsIntegrationOverlay.prototype.getRoute = function()
               {
                  var route = "<to uri=\"ipp:data:toNativeObject\"/>\n";
                  route += "<setHeader headerName=\"CamelLanguageScript\">\n";
                  route += "   <constant>\n";
                  route += "function setOutHeader(key, output){\nexchange.out.headers.put(key,output);}\n";
                  route += "function formatDate(format,value){\n  return new java.text.SimpleDateFormat(format).format(value);}\n";
                  route += "function isArray(obj) {\n\tif (Array.isArray) {\n\t\treturn Array.isArray(obj);\n\t} else {\n\treturn Object.prototype.toString.call(obj) === '[object Array]';\n\t}\n}\n";
                  
                  route += "function visitMembers(obj, callback) {\n\tvar i = 0, length = obj.length;\n\tif (isArray(obj)) {\n\t\t";
                  route += "for(; i &lt; length; i++) {\n\t\tobj[i]= callback(i, obj[i]);\n\t\t}\n";
                  route += "} else {\n\t\tfor (i in obj) {\n\t\tobj[i]=  callback(i, obj[i]);}\n\t}\n\treturn obj;\n}\n";
                  
                  route += "function recursiveFunction(key, val) {\n";
                  route += "\tif (val instanceof Object || isArray(val)) {\n";
                  route += "\t\treturn visitMembers(val, recursiveFunction);\n";
                  route += "\t} else {\n";
                  route += "\t\treturn actualFunction(val, typeof val);\n";
                  route += "\t}\n";
                  route += "}\n";
                  
                  route += "function actualFunction(value, type) {\n";
                  route += "\tvar dataAsLong;\n";
                  route += "\tif (type === 'string') {\n";
                  route += "\t\tdataAsLong =new RegExp(/\\/Date\\((-?\\d*)\\)\\//).exec(value);\n";
                  route += "\tif (dataAsLong) {\n";
                  route += "\t\treturn new java.util.Date(+dataAsLong[1]);\n";
                  route += "\t}\n";
                  route += "}\n";
                  route += "return value;\n";
                  route += "}\n";

                  route += "     String.prototype.hashCode = function() {";
                  route += "        var hash = 0;\n";
                  route += "        if (this == 0) return hash;\n";
                  route += "        for (var i = 0; i &lt; this.length; i++) {\n";
                  route += "           var character = this.charCodeAt(i);\n";
                  route += "           hash = ((hash&lt;&lt;5)-hash)+character;\n";
                  route += "           hash = hash &amp; hash;\n";
                  route += "        }\n";
                  route += "        return hash;\n";
                  route += "     }\n";
                  for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {

                     var accessPoint = this.getApplication().contexts.application.accessPoints[n];

                     if (accessPoint.direction == m_constants.OUT_ACCESS_POINT) {
                        continue;
                     }

                     if (accessPoint.dataType == "primitive") {
                        
                        
                        route += "var " + accessPoint.id + ";\n";
                        route += "if(request.headers.get('"
                              + accessPoint.id + "')!=null){\n";
                        route += accessPoint.id
                              + " =  request.headers.get('"
                              + accessPoint.id + "');\n";
                        route += "}\n";

                     } else if (accessPoint.dataType == "struct") {
                        
                        route += "var " + accessPoint.id + ";\n";
                        route += "if(request.headers.get('"
                              + accessPoint.id + "')!=null){\n";
                        route += accessPoint.id
                              + " =  eval('(' + request.headers.get('"
                              + accessPoint.id + "')+ ')');\n";
                        route +=  accessPoint.id+"=visitMembers("+accessPoint.id+", recursiveFunction);\n";
                        route += "}\n";
                        
                     }
                     }
                  route += "\n";
                  
                  var messageContent = this.codeEditor.getEditor().getSession().getValue();
                  if (messageContent != null && messageContent != "")
                  {
                     messageContent = m_utils.encodeXmlPredfinedCharacters(messageContent);
                  }
                  route+="<![CDATA[";
                  route += "      response = '"
                        + messageContent.replace(new RegExp("\n", 'g'), " ")
                              .replace(new RegExp("toDate", 'g'), "formatDate")
                              .replace(new RegExp("{{", 'g'), "' + ")
                              .replace(new RegExp("}}", 'g'), " + '")
                        + "';\n";
                  route+="]]>";
                  route += "      setOutHeader('response', response);\n";
                  route += "   </constant>\n";
                  route += "</setHeader>\n";

                  // execute java sript
                  route += "<to uri=\"language:javascript\"/>\n";
                  route += "<setBody>\n";
                  route += "   <simple>$simple{in.header.response}</simple>\n";
                  route += "</setBody>\n";
                  
                  //route +="<to uri=\"smpp://"+this.userNameInput.val()+"@"+this.hostNameInput.val()+":"+this.portInput.val()+"?password="+this.passowrdInput.val()+"&amp;CamelSmppDestAddr="+this.destinationAddressInput.val()+"&amp;CamelSmppSourceAddr="+this.sourceAddressInput.val()+"\"/>"
                  route +="<to uri=\"smpp://"+this.userNameInput.val()+"@"+this.hostNameInput.val()+":"+this.portInput.val()+"?password="+this.passowrdInput.val()+"&amp;destAddr="+this.destinationAddressInput.val()+"&amp;sourceAddr="+this.sourceAddressInput.val()+"\"/>"
                  m_utils.debug(route);
              //    route = route.replace(/&/g, "&amp;");
                  return route;
               };

               SmsIntegrationOverlay.prototype.populateDataStructuresSelectInput = function(
                        structuredDataTypeSelect, scopeModel, restrictToCurrentModel,
                        direction)
               {
                  structuredDataTypeSelect.empty();
                  structuredDataTypeSelect.append("<option value='"
                           + m_constants.TO_BE_DEFINED + "'>"
                           + m_i18nUtils.getProperty("modeler.general.toBeDefined")
                           + "</option>");

                  if (scopeModel)
                  {
                     structuredDataTypeSelect.append("<optgroup label='"
                              + m_i18nUtils.getProperty("modeler.general.thisModel")
                              + "'>");
                     if (direction == "IN")
                     {
                        for ( var i in scopeModel.typeDeclarations)
                        {
                           if (!scopeModel.typeDeclarations[i].isSequence())
                              continue;
                           structuredDataTypeSelect.append("<option value='"
                                    + scopeModel.typeDeclarations[i].id.toLowerCase()
                                    + "'>" + scopeModel.typeDeclarations[i].name
                                    + "</option>");
                        }
                     }
                     if (direction == "OUT")
                     {
                        for ( var i in scopeModel.dataItems)
                        {
                           if (scopeModel.dataItems[i].dataType == "struct")
                           {
                              structuredDataTypeSelect.append("<option value='"
                                       + scopeModel.dataItems[i].id + "'>"
                                       + scopeModel.dataItems[i].name + "</option>");
                           }
                           else
                           {
                              structuredDataTypeSelect.append("<option value='"
                                       + scopeModel.dataItems[i].id + "'>"
                                       + scopeModel.dataItems[i].name + "</option>");
                           }
                        }
                     }
                  }
                  return structuredDataTypeSelect;
               };

               /**
                * 
                */
               SmsIntegrationOverlay.prototype.submitChanges = function()
               {
                  this.view
                           .submitChanges({
                              attributes : {
                                 "carnot:engine:camel::applicationIntegrationOverlay" : "smsIntegrationOverlay",
                                 "carnot:engine:camel::camelContextId" : "defaultCamelContext",
                                 "carnot:engine:camel::invocationPattern" : "sendReceive",
                                 "carnot:engine:camel::invocationType" : "synchronous",
                                 "carnot:engine:camel::inBodyAccessPoint" : (this.inputBodyAccessPointInput
                                          .val() != null && this.inputBodyAccessPointInput
                                          .val() != m_constants.TO_BE_DEFINED) ? this.inputBodyAccessPointInput
                                          .val()
                                          : null,
                                 "stardust:smsIntegrationOverlay::messagecontent" : this.codeEditor.getEditor().getSession().getValue(),
                                 "stardust:smsIntegrationOverlay::hostname":this.hostNameInput.val(),
                                 "stardust:smsIntegrationOverlay::port":this.portInput.val(),
                                 "stardust:smsIntegrationOverlay::username":this.userNameInput.val(),
                                 "stardust:smsIntegrationOverlay::password":this.passowrdInput.val(),
                                 "stardust:smsIntegrationOverlay::useCVforPassowrd" : this.useCVforPassowrdInput
                                 .prop("checked") ? "${"+this.passowrdInput.val()+":Password}" : null,
                                 "stardust:smsIntegrationOverlay::sourceaddress":this.sourceAddressInput.val(),
                                 "stardust:smsIntegrationOverlay::destinationaddress":this.destinationAddressInput.val(),
                                 "stardust:smsOverlay::usessl":this.useSSLInput
                                 .prop("checked") ? this.useSSLInput
                                          .prop("checked") : null,
                                 "carnot:engine:camel::routeEntries" : this.getRoute()
                                 
                              }
                           });
               };
               /**
                * 
                */
               SmsIntegrationOverlay.prototype.submitParameterDefinitionsChanges = function(
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
               SmsIntegrationOverlay.prototype.validate = function()
               {
                  return true;
               };
            }
         });