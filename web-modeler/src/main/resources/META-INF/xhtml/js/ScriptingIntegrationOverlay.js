define(
      [ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_i18nUtils",
            "bpm-modeler/js/m_constants",
            "bpm-modeler/js/m_commandsController",
            "bpm-modeler/js/m_command", "bpm-modeler/js/m_model",
            "bpm-modeler/js/m_accessPoint",
            "bpm-modeler/js/m_typeDeclaration",
            "bpm-modeler/js/m_parameterDefinitionsPanel",
            "bpm-modeler/js/m_codeEditorAce",
            "bpm-modeler/js/m_parsingUtils",
            "bpm-modeler/js/m_autoCompleters","bpm-modeler/js/m_user"],
      function(m_utils, m_i18nUtils, m_constants, m_commandsController,
            m_command, m_model, m_accessPoint, m_typeDeclaration,
            m_parameterDefinitionsPanel, m_codeEditorAce,
            m_parsingUtils,m_autoCompleters,m_user) {
         return {
            create : function(view) {
               var overlay = new ScriptingIntegrationOverlay();

               overlay.initialize(view);

               return overlay;
            }
         };

         /**
          * 
          */
         function ScriptingIntegrationOverlay() {
            /**
             * 
             */
            ScriptingIntegrationOverlay.prototype.initialize = function(
                  view) {
               this.view = view;

               this.view
                     .insertPropertiesTab(
                           "scriptingIntegrationOverlay",
                           "parameters",
                           m_i18nUtils
                                 .getProperty("modeler.model.applicationOverlay.scripting.parameters.title"),
                           "plugins/bpm-modeler/images/icons/database_link.png");
               this.view
                     .insertPropertiesTab(
                           "scriptingIntegrationOverlay",
                           "test",
                           m_i18nUtils
                                 .getProperty("modeler.model.applicationOverlay.scripting.test.title"),
                           "plugins/bpm-modeler/images/icons/application-run.png");
               this.testTab = m_utils.jQuerySelect("#test");
               this.scriptCodeHeading = m_utils.jQuerySelect("#scriptingIntegrationOverlay #scriptCodeHeading");
               this.languageSelect = m_utils.jQuerySelect("#scriptingIntegrationOverlay #languageSelect");
               this.transactedRouteRow = m_utils.jQuerySelect("#scriptingIntegrationOverlay #transactedRouteRow");
               this.transactedRouteInput = m_utils.jQuerySelect("#scriptingIntegrationOverlay #transactedRouteInput");
               this.autoStartupRow = m_utils.jQuerySelect("#scriptingIntegrationOverlay #autoStartupRow");
               this.autoStartupInput = m_utils.jQuerySelect("#scriptingIntegrationOverlay #autoStartupInput");
               this.editorAnchor = m_utils.jQuerySelect("#codeEditorDiv").get(0);
               this.editorAnchor.id = "codeEditorDiv" + Math.floor((Math.random()*100000) + 1);
               
               this.codeEditor = m_codeEditorAce
                     .getJSCodeEditor(this.editorAnchor.id);
               
               /*Listen for our module loaded events. Specifically, for our
                *language tools being loaded. When we receive it load our sessionCompleter*/
               var that=this; /*for reference in our callback*/
               $(this.codeEditor).on("moduleLoaded",function(event,module){
                  var sessionCompleter;
                  if(module.name==="ace/ext/language_tools"){
                     sessionCompleter=m_autoCompleters.getSessionCompleter({metaName:"Data",score:9999});
                     that.codeEditor.addCompleter(sessionCompleter);
                  }
               });
               
               /*Load our languageTools extension into ACE, this will fire a 
                * moduleLoaded event on completion.*/
               this.codeEditor.loadLanguageTools();
               
               this.resetButton = m_utils.jQuerySelect("#testTab #resetButton");
               this.runButton = m_utils.jQuerySelect("#testTab #runButton");
               this.inputDataTextarea = m_utils.jQuerySelect("#testTab #inputDataTextarea");
               this.outputDataTextarea = m_utils.jQuerySelect("#testTab #outputDataTextarea");
               this.inputBodyAccessPointInput = m_utils.jQuerySelect("#parametersTab #inputBodyAccessPointInput");
               this.outputBodyAccessPointInput = m_utils.jQuerySelect("#parametersTab #outputBodyAccessPointInput");
               
               this.scriptCodeHeading.empty();
               this.scriptCodeHeading
                     .append(m_i18nUtils
                           .getProperty("modeler.model.applicationOverlay.scripting.code.heading"));

               var self = this;
               this.parameterDefinitionNameInput = m_utils.jQuerySelect("#parametersTab #parameterDefinitionNameInput");

               m_utils.jQuerySelect("a[href='#configurationTab']").click(function() {
                  self.setGlobalVariables();
               });

               this.languageSelect
                     .change(function() {
                        var code = self.codeEditor.getEditor()
                              .getSession().getValue();

                        if (self.languageSelect.val() == "JavaScript") {
                           self.testTab.parent().parent().show();
                           self.codeEditor = m_codeEditorAce
                                 .getJSCodeEditor(self.editorAnchor.id);
                        } else if (self.languageSelect.val() == "Python") {
                           self.testTab.parent().parent().hide();
                           self.codeEditor = m_codeEditorAce
                                 .getPythonCodeEditor(self.editorAnchor.id);
                        } else if (self.languageSelect.val() == "Groovy") {
                           self.testTab.parent().parent().hide();
                           self.codeEditor = m_codeEditorAce
                                 .getGroovyCodeEditor(self.editorAnchor.id);
                        }
                        
                        self.codeEditor.getEditor().getSession()
                              .setValue(code);
                        self.submitChanges();
                     });
               this.codeEditor.getEditor().on('blur', function(e) {
                  self.submitChanges();
               });
               this.resetButton
                     .prop(
                           "title",
                           m_i18nUtils
                                 .getProperty("modeler.model.applicationOverlay.scripting.test.resetButton.title"));
               this.runButton
                     .prop(
                           "title",
                           m_i18nUtils
                                 .getProperty("modeler.model.applicationOverlay.scripting.test.runButton.title"));
               m_utils.jQuerySelect("label[for='inputDataTextArea']")
                     .text(
                           m_i18nUtils
                                 .getProperty("modeler.model.applicationOverlay.scripting.test.inputDataTextArea.label"));
               m_utils.jQuerySelect("label[for='outputDataTextarea']")
                     .text(
                           m_i18nUtils
                                 .getProperty("modeler.model.applicationOverlay.scripting.test.outputDataTextArea.label"));

               this.parameterDefinitionsPanel = m_parameterDefinitionsPanel
                     .create({
                        scope : "parametersTab",
                        submitHandler : this,
                        supportsOrdering : false,
                        supportsDataMappings : false,
                        supportsDescriptors : false,
                        supportsDataTypeSelection : true,
                        supportsDocumentTypes : false,
                        hideEnumerations:true
                     });

               this.runButton
                     .click(function() {
                        var functionBody = "";
                        if (self.inputDataTextarea.val()
                              && self.inputDataTextarea.val().trim() != "") {
                           functionBody = "var input = "
                              + self.inputDataTextarea.val() + ";\n";   
                        }

                        functionBody += "var output = "
                              + self.createParameterObjectString(
                                    m_constants.OUT_ACCESS_POINT,
                                    true) + ";\n";

                        var code = self.codeEditor.getEditor()
                              .getSession().getValue();

                        // Convert Input and Output Access Points

                        for ( var n = 0; n < self.getApplication().contexts.application.accessPoints.length; ++n) {
                           var accessPoint = self.getApplication().contexts.application.accessPoints[n];

                           // \b is to demarcate whole words only

                           if (accessPoint.direction === m_constants.IN_ACCESS_POINT) {
                              code = code.replace(new RegExp("\\b"
                                    + accessPoint.id + "\\b", "g"),
                                    "input." + accessPoint.id);
                           }

                           if (accessPoint.direction === m_constants.OUT_ACCESS_POINT) {
                              code = code.replace(new RegExp("\\b"
                                    + accessPoint.id + "\\b", "g"),
                                    "output." + accessPoint.id);
                           }
                        }

                        functionBody += code + "\n";

                        functionBody += "return output;";

                        m_utils.debug(functionBody);

                        var mappingFunction = new Function(functionBody);

                        var result = "";
                        
                        try {
                           result = mappingFunction();
                        } catch(e) {
                           result = "Error: " + e.message;
                        }

                        self.outputDataTextarea.val(JSON
                              .stringify(result));
                     });
               this.resetButton.click(function() {
                  self.inputDataTextarea.val("");
                  self.outputDataTextarea.val("");
                  self.inputDataTextarea.val(self
                        .createParameterObjectString(
                              m_constants.IN_ACCESS_POINT, true));
               });
               this.transactedRouteInput.change(function() {
                   if (!self.view.validate()) {
                      return;
                   }
                   self.view.submitModelElementAttributeChange(
                         "carnot:engine:camel::transactedRoute",
                         self.transactedRouteInput.prop('checked'));
                   self.submitChanges();
                });
               this.autoStartupInput.change(function() {
                   if (!self.view.validate()) {
                      return;
                   }
                   self.view.submitModelElementAttributeChange(
                         "carnot:engine:camel::autoStartup",
                         self.autoStartupInput.prop('checked'));
                   self.submitChanges();
                });
                
            this.update();
            };

            /**
             * 
             */
            ScriptingIntegrationOverlay.prototype.createParameterObjectString = function(
                  direction, initializePrimitives, singleVariables, identifier) {
               var otherDirection;
               if (direction === m_constants.IN_ACCESS_POINT) {
                  otherDirection = m_constants.OUT_ACCESS_POINT;
               } else {
                  otherDirection = m_constants.IN_ACCESS_POINT;
               }

               var parameterObjectString = "";

               if (!singleVariables) {
                  parameterObjectString += "{";
               }

               var index = 0;

               for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
                  var parameterDefinition = this.getApplication().contexts.application.accessPoints[n];

                  if (parameterDefinition.direction === otherDirection) {
                     continue;
                  }

                  if (index > 0 && !singleVariables) {
                     parameterObjectString += ", ";
                  }

                  ++index;

                  if (parameterDefinition.dataType == "primitive") {
                     if (initializePrimitives || singleVariables) {
                        if (singleVariables) {
                           parameterObjectString += identifier;
                        }

                        parameterObjectString += parameterDefinition.id;
                        
                        if (singleVariables) {
                           parameterObjectString += " = ";
                        } else {
                           parameterObjectString += ": ";
                        }

                        if (parameterDefinition.primitiveDataType === "String") {
                           parameterObjectString += "\"\"";
                        } else if (parameterDefinition.primitiveDataType === "Boolean"||parameterDefinition.primitiveDataType === "boolean") {
                           parameterObjectString += "false";
                        }else if(parameterDefinition.primitiveDataType === "Timestamp"){
                           parameterObjectString += "null";
                        }else {
                           parameterObjectString += "0";
                        }

                        if (singleVariables) {
                           parameterObjectString += ";\n";
                        }
                     }
                  } else if (parameterDefinition.dataType == "struct") {
                     var typeDeclaration = m_model
                           .findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);
                     if (singleVariables) {
                        parameterObjectString += identifier;
                     }

                     parameterObjectString += parameterDefinition.id;
                     if (singleVariables) {
                        parameterObjectString += " = ";
                     } else {
                        parameterObjectString += ": ";
                     }

                     parameterObjectString += JSON
                           .stringify(
                                 typeDeclaration
                                       .createInstance({
                                          initializePrimitives : initializePrimitives
                                       }), null, 3);

                     if (singleVariables) {
                        parameterObjectString += ";\n";
                     }
                  } else if (parameterDefinition.dataType == "dmsDocument") {
                     var typeDeclaration = m_model
                           .findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);

                     if (singleVariables) {
                        parameterObjectString += identifier;
                     }

                     parameterObjectString += parameterDefinition.id;

                     if (singleVariables) {
                        parameterObjectString += " = ";
                     } else {
                        parameterObjectString += ": ";
                     }

                     parameterObjectString += JSON
                           .stringify(
                                 typeDeclaration
                                       .createInstance({
                                          initializePrimitives : initializePrimitives
                                       }), null, 3);

                     if (singleVariables) {
                        parameterObjectString += ";\n";
                     }
                  }
               }

               if (!singleVariables) {
                  parameterObjectString += "}";
               }

               return parameterObjectString;
            };

            /**
             * 
             */
            ScriptingIntegrationOverlay.prototype.getModelElement = function() {
               return this.view.getModelElement();
            };

            /**
             * 
             */
            ScriptingIntegrationOverlay.prototype.getApplication = function() {
               return this.view.application;
            };

            /**
             * 
             */
            ScriptingIntegrationOverlay.prototype.getScopeModel = function() {
               return this.view.getModelElement().model;
            };

            ScriptingIntegrationOverlay.prototype.toArray = function(attributes){
               var arr=new Array();
               for( var i in attributes ) {
                   if (attributes.hasOwnProperty(i)){
                       arr.push(attributes[i]);
                   }
               }
               return arr;
           };
            /**
             * 
             */
            ScriptingIntegrationOverlay.prototype.activate = function() {
             var attributes={};
             attributes["carnot:engine:camel::routeEntries"]=null;
               if (this.toArray(this.view.getApplication().attributes).length==2)
               {
                  attributes["carnot:engine:camel::camelContextId"]  = "defaultCamelContext";
                  attributes["carnot:engine:camel::invocationPattern"]  = "sendReceive";
                  attributes["carnot:engine:camel::invocationType"]  = "synchronous";
                  attributes["carnot:engine:camel::applicationIntegrationOverlay"]  = "scriptingIntegrationOverlay";
               }
               this.view
               .submitChanges({
                  attributes :attributes
               });
            };

            /**
             * 
             */
            ScriptingIntegrationOverlay.prototype.update = function() {
               var completerStrings=[],pDef,key;
               this.parameterDefinitionsPanel.setScopeModel(this
                     .getScopeModel());
               this.parameterDefinitionsPanel
                     .setParameterDefinitions(this.getApplication().contexts.application.accessPoints);
               this.autoStartupRow.hide();
               this.transactedRouteRow.hide();
               if(this.isIntegrator()){
                 this.autoStartupRow.show();
                 this.transactedRouteRow.show();
               }
               this.languageSelect
                     .val(this.getApplication().attributes["stardust:scriptingOverlay::language"]);         
               this.codeEditor
                     .getEditor()
                     .getSession()
                     .setValue(
                           this.getApplication().attributes["stardust:scriptingOverlay::scriptCode"]);
               
               for(key in this.parameterDefinitionsPanel.parameterDefinitions){
                  if(this.parameterDefinitionsPanel.parameterDefinitions.hasOwnProperty(key)){
                     pDef=this.parameterDefinitionsPanel.parameterDefinitions[key];
                     completerStrings=completerStrings.concat(m_parsingUtils.parseParamDefToStringFrags(pDef));
                     this.codeEditor.setSessionData("$keywordList",completerStrings);
                  }
               }        
               if(this.getApplication().attributes["carnot:engine:camel::transactedRoute"]==null||this.getApplication().attributes["carnot:engine:camel::transactedRoute"]===undefined){
                   this.view.submitModelElementAttributeChange("carnot:engine:camel::transactedRoute", false);
                }
               this.transactedRouteInput.prop("checked",
                       this.getApplication().attributes["carnot:engine:camel::transactedRoute"]);
               if(this.getApplication().attributes["carnot:engine:camel::autoStartup"]==null||this.getApplication().attributes["carnot:engine:camel::autoStartup"]===undefined){
                   this.view.submitModelElementAttributeChange("carnot:engine:camel::autoStartup", true);
                }
               this.autoStartupInput.prop("checked",
                       this.getApplication().attributes["carnot:engine:camel::autoStartup"]);
            };
            
             /**
             * 
             */
            ScriptingIntegrationOverlay.prototype.submitChanges = function(
                  parameterDefinitionsChanges) {
               this.view
                     .submitChanges({
                        attributes : {
                           "carnot:engine:camel::applicationIntegrationOverlay" : "scriptingIntegrationOverlay",
                           "carnot:engine:camel::camelContextId" : "defaultCamelContext",
                           "carnot:engine:camel::invocationPattern" : "sendReceive",
                           "carnot:engine:camel::invocationType" : "synchronous",
                           "stardust:scriptingOverlay::language" : this.languageSelect
                                 .val(),
                           "stardust:scriptingOverlay::scriptCode" : this.codeEditor
                                 .getEditor().getSession()
                                 .getValue()
                        }
                     });
            };

            /**
             * 
             */
            ScriptingIntegrationOverlay.prototype.submitParameterDefinitionsChanges = function(
                  parameterDefinitionsChanges) {
               this.getApplication().contexts.application.accessPoints = parameterDefinitionsChanges;
               this.view
                     .submitChanges({
                        contexts : {
                           application : {
                              accessPoints : parameterDefinitionsChanges
                           }
                        },
                        attributes : {
                           "carnot:engine:camel::applicationIntegrationOverlay" : "scriptingIntegrationOverlay",
                           "carnot:engine:camel::camelContextId" : "defaultCamelContext",
                           "stardust:scriptingOverlay::scriptCode" : this.codeEditor
                                 .getEditor().getSession()
                                 .getValue()
                        }
                     });
            };

            /**
             * 
             */
            ScriptingIntegrationOverlay.prototype.setGlobalVariables = function() {
               // Global variables for Code Editor auto-complete / validation
               var globalVariables = {};
               
               for (var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
                  var parameterDefinition = this.getApplication().contexts.application.accessPoints[n];

                  var typeDeclaration = null;
                  if (parameterDefinition.dataType == "struct") {
                     typeDeclaration = m_model.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);
                  }
                  
                  if (typeDeclaration != null) {
                     globalVariables[parameterDefinition.id] = typeDeclaration.createInstance();
                  }
                  else {
                     globalVariables[parameterDefinition.id] = "";
                  }
               }

               this.codeEditor.setGlobalVariables(globalVariables);
            };
            
            /**
             * 
             */
            ScriptingIntegrationOverlay.prototype.validate = function() {
               
               var valid = true;
               this.view.clearErrorMessages();
               m_utils.jQuerySelect("#codeEditorElmt").removeClass("error");
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
             
               this.view.warningMessages = [];
               this.view.clearWarningMessages();
               if(m_utils.isEmptyString(this.codeEditor.getEditor().getSession().getValue())){
                  if (this.languageSelect.val() === "JavaScript") {
                     this.view.warningMessages.push("No JavaScript expression provided.");
                  }else if(this.languageSelect.val() === "Groovy"){
                     this.view.warningMessages.push("No Groovy script provided.");   
                  }else{
                     this.view.warningMessages.push("No Python script provided.");
                  }
                  m_utils.jQuerySelect("#codeEditorElmt").addClass("error");
                  this.view.showWarningMessages();
               }
               return true;
            };
            ScriptingIntegrationOverlay.prototype.isIntegrator = function(){
               return m_user.getCurrentRole() == m_constants.INTEGRATOR_ROLE;
            }
         }
      });