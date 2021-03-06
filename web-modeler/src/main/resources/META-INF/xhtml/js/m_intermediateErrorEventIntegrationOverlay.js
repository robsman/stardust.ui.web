/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * Timer Event overlay for Intermediate events
 *
 * @author Yogesh.Manware
 *
 * Note: don't remove the code around following elements - this is for future development
 *  - Automatic Binding
 *  - Consume On Match
 *  - Event Action
 *
 */

define(
      [ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
            "bpm-modeler/js/m_commandsController",
            "bpm-modeler/js/m_command", "bpm-modeler/js/m_model",
            "bpm-modeler/js/m_accessPoint",
            "bpm-modeler/js/m_parameterDefinitionsPanel",
            "bpm-modeler/js/m_eventIntegrationOverlay", 
            "bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_dialog",
            "bpm-modeler/js/m_parsingUtils"],
      function(m_utils, m_constants, m_commandsController, m_command,
            m_model, m_accessPoint, m_parameterDefinitionsPanel,
            m_eventIntegrationOverlay, m_i18nUtils, m_dialog, m_parsingUtils) {

         return {
            create : function(page, id) {
               var overlay = new IntermediateErrorEventIntegrationOverlay();
               overlay.initialize(page, id);
               return overlay;
            }
         };

         /**
         *
         */
         /**
         * @returns {IntermediateErrorEventIntegrationOverlay}
         */
         function IntermediateErrorEventIntegrationOverlay() {
            var eventIntegrationOverlay = m_eventIntegrationOverlay
                  .create();

            m_utils.inheritFields(this, eventIntegrationOverlay);
            m_utils.inheritMethods(
                  IntermediateErrorEventIntegrationOverlay.prototype,
                  eventIntegrationOverlay);

            /**
            *
            */
            IntermediateErrorEventIntegrationOverlay.prototype.initialize = function(
                  page, id) {
               var thisOverlay = this;
              
               this.initializeEventIntegrationOverlay(page, id);

               /*jQuery("label[for='autoBindingInput']")
                     .text(
                           m_i18nUtils
                                 .getProperty("modeler.element.properties.timerEvent_intermediate.autoBinding"));*/
               jQuery("label[for='logHandlerInput']")
                     .text(
                           m_i18nUtils
                                 .getProperty("modeler.element.properties.timerEvent_intermediate.logHandler"));

               jQuery("label[for='consumeOnMatchInput']")
                     .text(
                           m_i18nUtils
                                 .getProperty("modeler.element.properties.timerEvent_intermediate.consumeOnMatch"));

               jQuery("label[for='interruptingInput']")
                     .text(
                           m_i18nUtils
                                 .getProperty("modeler.element.properties.timerEvent_intermediate.interrupting"));

               jQuery("label[for='eventTriggerInput']")
                     .text(
                           m_i18nUtils
                                 .getProperty("modeler.element.properties.timerEvent_intermediate.eventTrigger"));

               jQuery("label[for='delayTimerInput']")
                     .text(
                           m_i18nUtils
                                 .getProperty("modeler.element.properties.timerEvent_intermediate.delayTimer"));

               jQuery("label[for='dataSelect']")
                     .text(
                           m_i18nUtils
                                 .getProperty("modeler.element.properties.timerEvent_intermediate.eventTrigger.data"));

               jQuery("label[for='dataPathInput']")
                     .text(
                           m_i18nUtils
                                 .getProperty("modeler.element.properties.timerEvent_intermediate.eventTrigger.data.path"));

               /*jQuery("label[for='eventActionSelect']")
                     .text(
                           m_i18nUtils
                                 .getProperty("modeler.element.properties.timerEvent_intermediate.eventAction"));*/

               this.configurationSpan = this.mapInputId("configuration");

               this.configurationSpan
                     .text(m_i18nUtils
                           .getProperty("modeler.element.properties.event.configuration"));
               this.parametersSpan = this.mapInputId("parameters");

               this.parametersSpan
                     .text(m_i18nUtils
                           .getProperty("modeler.element.properties.event.parameters"));

               // this.autoBindingInput = this.mapInputId("autoBindingInput");
               this.logHandlerInput = this.mapInputId("logHandlerInput");
               this.consumeOnMatchInput = this.mapInputId("consumeOnMatchInput");

               this.interruptingInput = this
                     .mapInputId("interruptingInput");

               this.eventTriggerSelect = this
               .mapInputId("eventTriggerSelect");

               this.initializeEventTriggerSelect(this.eventTriggerSelect);

               this.eventTriggerInput = this
                     .mapInputId("eventTriggerInput");


               this.eventTriggerSelect.change({
                  overlay : this
               }, function(event) {
                  var overlay = event.data.overlay;
                  var eventTrigger = overlay.eventTriggerSelect.val();

                  //show/hide text field
                  if ('' == eventTrigger) {
                     overlay.eventTriggerInput.show();
                     overlay.submitEventTriggerChanges(overlay.eventTriggerSelect.val());
                  } else {
                     overlay.eventTriggerInput.hide();
                     overlay.submitEventTriggerChanges(overlay.eventTriggerSelect.val());
                  }
               });

               this.eventTriggerInput.change({
                  overlay : this
                  },
                  function(event) {
                     var overlay = event.data.overlay;
                     if (overlay.validate()) {
                        overlay.submitEventTriggerChanges(overlay.eventTriggerInput.val());
                     }
                  });

               this.logHandlerInput.change({
                  overlay : this
               }, function(event) {
                  var overlay = event.data.overlay;
                  overlay.submitChanges({
                     modelElement : {
                        logHandler :  overlay.logHandlerInput.prop("checked")
                     }
                  });
               });
           
               this.consumeOnMatchInput.change({
                 overlay : this
              }, function(event) {
                 var overlay = event.data.overlay;
                 overlay.submitChanges({
                    modelElement : {
                      consumeOnMatch :  overlay.consumeOnMatchInput.prop("checked")
                    }
                 });
              });
               
               m_utils
                      .jQuerySelect("#writeToDataHeader")
                      .text(
                              m_i18nUtils
                                      .getProperty("modeler.element.properties.errorEvent_intermediate.writetoData"));
              // data
              m_utils
                      .jQuerySelect("label[for='dataActionDataSelect']")
                      .text(
                              m_i18nUtils
                                      .getProperty("modeler.element.properties.commonProperties.data"));

              this.dataActionDataSelect = this
                      .mapInputId("#dataActionDataSelect");

              this.dataActionDataSelect.change({
                that: this
              }, function(event) {
                // reset data path
                event.data.that.dataActionPathInput.val("");
                event.data.that.setAutoCompleteMatches(event.data.that.dataActionDataSelect.val());
                
                event.data.that.submitDataAction();
              });

              // dataPath
              m_utils
                      .jQuerySelect("label[for='dataActionPathInput']")
                      .text(
                              m_i18nUtils
                                      .getProperty("modeler.element.properties.timerEvent_intermediate.eventTrigger.data.path"));

              this.dataActionPathInput = this
                      .mapInputId("#dataActionPathInput");

              /*Setup autocomplete for data paths*/
              m_utils.jQuerySelect(this.dataActionPathInput)
              .autocomplete({
                minLength: 0,
                  minChars: 0,
                  autoFill: true,
                  mustMatch: true,
                  matchContains: false
              })
              .on("focus",function(){
                /*Force the dropdown menu to display all items on focus*/
                m_utils.jQuerySelect(this).autocomplete("search","");
              })
              .on("autocompletechange",function(event,ui){
                thisOverlay.submitDataAction();
              });
              
            };

            /**
             * 
             */
            IntermediateErrorEventIntegrationOverlay.prototype.setAutoCompleteMatches = function(dataFullId, delim){
              var matches=[],    /*matches for the autocomplete option*/
                  typeDecl,      /*typeDecl returned by matching the schemaName*/
                  isDelimDefault,/*track whether our delim has the default value*/
                  tempStr,       /*temp match string before we parse the schemaName from it*/
                  i;         /*iterator*/
              
              isDelimDefault =(delim)?false:true;
              delim=delim || ".";
              
              //check if the id qualified or placeholder
              if(dataFullId.indexOf(":") != -1){
                paramDef = { dataFullId: dataFullId, id:""}
                matches=m_parsingUtils.parseParamDefToStringFrags(paramDef) || [];
              }             
              
              /*Replacing '.' delimiter from the parse function and stripping the rootName*/
              for(i=0;i<matches.length;i++){
                if(isDelimDefault){
                  tempStr=matches[i].replace(/\./g,delim);
                }
                matches[i]=tempStr.slice(tempStr.indexOf(delim)+1);
              }
              
              /*Set Autocomplete source to our new match function*/
              m_utils.jQuerySelect(this.dataActionPathInput)
              .autocomplete("option","source",function(req,res){
                var match=req.term,
                  filtered=[],
                  temp;

                for(var j=0; j< matches.length; j++){
                  temp=matches[j];
                  if(temp.indexOf(match)==0 ){
                    if(temp.indexOf(delim,match.length)==-1){
                      if(temp.lastIndexOf(delim)>0){
                        temp=temp.slice(temp.lastIndexOf(delim)+1);
                      }
                      filtered.push({label:temp,value:matches[j]});
                    }
                  }
                }
                res(filtered);
              });
            }
            
            
            /**
             * 
             */
            IntermediateErrorEventIntegrationOverlay.prototype.populateData = function() {
              this.dataActionDataSelect.empty();
              this.scopeModel = this.page.getModel();

              var modelElement = this.page.propertiesPanel.element.modelElement;

              this.dataActionDataSelect
                      .append("<option value=\"TO_BE_DEFINED\">"
                              + m_i18nUtils
                                      .getProperty("modeler.general.toBeDefined")
                              + "</option>");

              if (this.scopeModel) {
                var modelname = m_i18nUtils
                        .getProperty("modeler.element.properties.commonProperties.thisModel");
                this.dataActionDataSelect.append("<optgroup label=\""
                        + modelname + "\">");

                for ( var i in this.scopeModel.dataItems) {
                  var dataItem = this.scopeModel.dataItems[i];
                  this.dataActionDataSelect.append("<option value='"
                          + dataItem.getFullId() + "'>" + dataItem.name
                          + "</option>");
                }
              }

              if (modelElement.setDataAction
                      && modelElement.setDataAction.dataId) {
                this.dataActionDataSelect
                        .val(modelElement.setDataAction.dataId);
              } else {
                this.dataActionDataSelect.val(m_constants.TO_BE_DEFINED);
              }

              if (modelElement.setDataAction
                      && modelElement.setDataAction.dataPath) {
                this.dataActionPathInput
                        .val(modelElement.setDataAction.dataPath);
              } else {
                this.dataActionPathInput.val("");
              }
              
              this.setAutoCompleteMatches(this.dataActionDataSelect.val());
            }
            
            /**
            *
            */
            IntermediateErrorEventIntegrationOverlay.prototype.submitEventTriggerChanges = function(value) {
               this.submitChanges({
                  modelElement : {
                     attributes : {
                        "carnot:engine:exceptionName" : value
                     }
                  }
               });
            };
            
            /**
             * 
             */
            IntermediateErrorEventIntegrationOverlay.prototype.submitDataAction = function(
                    value) {
              var setDataAction = null;
              if (this.dataActionDataSelect.val() == m_constants.TO_BE_DEFINED) {
                setDataAction = null;
              } else {
                setDataAction = {
                  dataId: this.dataActionDataSelect.val(),
                  dataPath: this.dataActionPathInput.val()
                }
              }

              this.submitChanges({
                modelElement: {
                  setDataAction: setDataAction
                }
              });
            };

            /**
            *
            * @param select
            */
            IntermediateErrorEventIntegrationOverlay.prototype.initializeEventTriggerSelect = function(
                  select) {
               select
                     .append("<option value='java.lang.Exception'>"
                           + m_i18nUtils
                                 .getProperty("modeler.element.properties.errorEvent_intermediate.eventTrigger.general")
                           + "</option>");

               select
                     .append("<option value='java.io.IOException'>"
                           + m_i18nUtils
                                 .getProperty("modeler.element.properties.errorEvent_intermediate.eventTrigger.network")
                           + "</option>");
               select
                     .append("<option value='java.lang.RuntimeException'>"
                           + m_i18nUtils
                                 .getProperty("modeler.element.properties.errorEvent_intermediate.eventTrigger.runtime")
                           + "</option>");
               select
                     .append("<option value='javax.xml.ws.WebServiceException'>"
                           + m_i18nUtils
                                 .getProperty("modeler.element.properties.errorEvent_intermediate.eventTrigger.webservice")
                           + "</option>");
               select
                     .append("<option value=''>"
                           + m_i18nUtils
                                 .getProperty("modeler.element.properties.errorEvent_intermediate.eventTrigger.other")
                           + "</option>");
            };

            /**
            * initialize data
            */
            IntermediateErrorEventIntegrationOverlay.prototype.update = function() {
               // retrieve and populated stored values
               // this.showHideEventTriggerFields('constant');
               // this.autoBindingInput.attr("disabled", "disabled");
               // this.eventActionSelect.attr("disabled", "disabled");
              
              jQuery("label[for='consumeOnMatchInput']").removeClass("invisible");
              m_dialog.makeVisible(this.consumeOnMatchInput);
              
              jQuery("label[for='logHandlerInput']")
              .text(
                    m_i18nUtils
                          .getProperty("modeler.propertiesPage.activity.excludedUsers.logToAuditTrail"));
              
               var modelElement = this.page.propertiesPanel.element.modelElement;
               this.interruptingInput.attr("checked", "checked");
               this.interruptingInput.attr("disabled", "disabled");

               this.logHandlerInput.prop("checked", modelElement.logHandler);
               this.consumeOnMatchInput.prop("checked", modelElement.consumeOnMatch);

               //this.eventTriggerSelect.val('java.lang.Exception');
               this.eventTriggerInput.hide();
               var exception = null;

               if (modelElement.attributes) {
                  exception = modelElement.attributes["carnot:engine:exceptionName"];
                  if (null != exception) {
                     if ("java.lang.Exception" === exception
                           || "java.io.IOException" === exception
                           || "java.lang.RuntimeException" === exception
                           || "javax.xml.ws.WebServiceException" === exception) {
                        this.eventTriggerInput.hide();
                        this.eventTriggerSelect.val(exception);
                     } else {
                        this.eventTriggerInput.show();
                        this.eventTriggerSelect.val('');
                        this.eventTriggerInput.val(exception);
                     }
                  } else {
                     this.eventTriggerInput.show();
                     this.eventTriggerSelect.val('');
                     this.eventTriggerInput.val('');
                  }
               }
               
               this.populateData();
            };

            /*IntermediateErrorEventIntegrationOverlay.prototype.initializeInterruptingSelect = function(
                  select) {
               select
                     .append("<option value='abortActivity'>"
                           + m_i18nUtils
                                 .getProperty("modeler.element.properties.event.abortActivity")
                           + "</option>");
               select
                     .append("<option value='completeActivity'>"
                           + m_i18nUtils
                                 .getProperty("modeler.element.properties.event.completeActivity")
                           + "</option>");
            };*/

            /*IntermediateErrorEventIntegrationOverlay.prototype.initializeEventActionSelect = function(
                  select) {
               select
                     .append("<option value='exceptionFlow'>"
                           + m_i18nUtils
                                 .getProperty("modeler.element.properties.timerEvent_intermediate.eventAction.exceptionFlow")
                           + "</option>");
            };*/

            /**
            *
            */
            IntermediateErrorEventIntegrationOverlay.prototype.getEndpointUri = function() {
               var uri = "timer://timerEndpoint";
               var separator = "?";

               return uri;
            };

            IntermediateErrorEventIntegrationOverlay.prototype.activate = function() {
               // It is invoked when there are multiple options in overlay
               // dropdown, here we have only one option.
            };

            IntermediateErrorEventIntegrationOverlay.prototype.getRouteDefinitions = function() {
               //not required in this case?
               return "";
            };



            /**
            *
            */
            IntermediateErrorEventIntegrationOverlay.prototype.validate = function() {
               return true;
            };
         }
      });