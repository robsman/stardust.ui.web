/*****************************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ****************************************************************************************/

define(
         [ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
                  "bpm-modeler/js/m_urlUtils", "bpm-modeler/js/m_session",
                  "bpm-modeler/js/m_command", "bpm-modeler/js/m_commandsController",
                  "bpm-modeler/js/m_dialog", "bpm-modeler/js/m_modelElementView",
                  "bpm-modeler/js/m_model", "bpm-modeler/js/m_dataTypeSelector",
                  "bpm-modeler/js/m_parameterDefinitionsPanel",
                  "bpm-modeler/js/m_codeEditorAce", "bpm-modeler/js/m_i18nUtils",
                  "bpm-modeler/js/m_angularContextUtils",
                  "bpm-modeler/js/m_extensionManager",
                  "bpm-modeler/js/m_routeDefinitionUtils" ],
         function(m_utils, m_constants, m_urlUtils, m_session, m_command,
                  m_commandsController, m_dialog, m_modelElementView, m_model,
                  m_dataTypeSelector, m_parameterDefinitionsPanel, m_codeEditorAce,
                  m_i18nUtils, m_angularContextUtils, m_extensionManager,
                  m_routeDefinitionUtils)
         {
            return {
               initialize : function(fullId)
               {
                  m_utils.initializeWaitCursor(m_utils.jQuerySelect("html"));
                  m_utils.showWaitCursor();

                  m_utils.jQuerySelect("#hideGeneralProperties").hide();
                  initViewCollapseClickHandlers();

                  var view = new DecoratorApplicationView();
                  i18uimashupproperties();
                  // TODO Unregister!
                  // In Initializer?

                  m_commandsController.registerCommandHandler(view);

                  view.initialize(m_model.findApplication(fullId));
                  m_utils.hideWaitCursor();
               }
            };

            /**
             * 
             */
            function initViewCollapseClickHandlers()
            {
               m_utils.jQuerySelect("#showGeneralProperties").click(function()
               {
                  m_utils.jQuerySelect("#showAllProperties").hide();
                  m_utils.jQuerySelect("#hideGeneralProperties").show();
               });
               m_utils.jQuerySelect("#hideGeneralProperties").click(function()
               {
                  m_utils.jQuerySelect("#showAllProperties").show();
                  m_utils.jQuerySelect("#hideGeneralProperties").hide();
               });
            }

            function i18uimashupproperties()
            {

               m_utils
                        .jQuerySelect("#hideGeneralProperties label")
                        .text(
                                 m_i18nUtils
                                          .getProperty("modeler.element.properties.commonProperties.generalProperties"));

               m_utils
                        .jQuerySelect("#showGeneralProperties label")
                        .text(
                                 m_i18nUtils
                                          .getProperty("modeler.element.properties.commonProperties.generalProperties"));

               m_utils
                        .jQuerySelect("label[for='guidOutput']")
                        .text(
                                 m_i18nUtils
                                          .getProperty("modeler.element.properties.commonProperties.uuid"));
               m_utils
                        .jQuerySelect("label[for='idOutput']")
                        .text(
                                 m_i18nUtils
                                          .getProperty("modeler.element.properties.commonProperties.id"));

               m_utils
                        .jQuerySelect("#applicationName")
                        .text(
                                 m_i18nUtils
                                          .getProperty("modeler.element.properties.commonProperties.applicationName"));
               m_utils
                        .jQuerySelect("#description")
                        .text(
                                 m_i18nUtils
                                          .getProperty("modeler.element.properties.commonProperties.description"));

               m_utils
                        .jQuerySelect("#configuration")
                        .text(
                                 m_i18nUtils
                                          .getProperty("modeler.element.properties.commonProperties.configuration"));

               m_utils
                        .jQuerySelect("label[for='publicVisibilityCheckbox']")
                        .text(
                                 m_i18nUtils
                                          .getProperty("modeler.element.properties.commonProperties.publicVisibility"));
               m_utils
                        .jQuerySelect("label[for='availableApplicationsInput']")
                        .text(
                                 m_i18nUtils
                                          .getProperty("modeler.element.properties.decoratorApplication.availableApplications"));
               m_utils
                        .jQuerySelect("label[for='processInterfacesInput']")
                        .text(
                                 m_i18nUtils
                                          .getProperty("modeler.element.properties.decoratorApplication.processInterfaces"));
               m_utils
                        .jQuerySelect("#noSelectionErrorMessage")
                        .text(
                                 m_i18nUtils
                                          .getProperty("modeler.element.properties.decoratorApplication.noSelectionErrorMessage"));

               // m_utils.jQuerySelect("label[for='availableModelsInput']").text(m_i18nUtils.getProperty("modeler.element.properties.applicationTemplate.availableApplications"));
               // m_utils.jQuerySelect("label[for='availableModelsInput']").text(m_i18nUtils.getProperty("modeler.element.properties.applicationTemplate.availableAppsOrProcInter"));
            }
            ;

            /**
             * 
             */
            function DecoratorApplicationView()
            {
               var modelElementView = m_modelElementView.create(true);
               m_utils.inheritFields(this, modelElementView);
               m_utils.inheritMethods(DecoratorApplicationView.prototype,
                        modelElementView);
               /**
                * 
                */
               DecoratorApplicationView.prototype.initialize = function(application)
               {
                  this.id = "decoratorApplicationView";
                  this.application = application;
                  this.accessPoints= angular.copy(this.getApplication().contexts.application.accessPoints);
                  this.view = m_utils.jQuerySelect("#" + this.id);
                  this.initializeModelElementView(application);
                  this.overlayAnchor = m_utils.jQuerySelect("#overlayAnchor");

                  this.publicVisibilityCheckbox = m_utils
                           .jQuerySelect("#publicVisibilityCheckbox");
                  /*
                   * this.availableApplicationsInput =
                   * m_utils.jQuerySelect("#configurationTab
                   * #availableApplicationsInput"); this.processInterfacesInput =
                   * m_utils.jQuerySelect("#configurationTab #processInterfacesInput");
                   */
                  this.view.css("visibility", "visible");
                  this.publicVisibilityCheckbox
                           .change(
                                    {
                                       "view" : this
                                    },
                                    function(event)
                                    {
                                       var view = event.data.view;

                                       if (view.modelElement.attributes["carnot:engine:visibility"]
                                                && view.modelElement.attributes["carnot:engine:visibility"] != "Public")
                                       {
                                          view.submitChanges({
                                             attributes : {
                                                "carnot:engine:visibility" : "Public"
                                             }
                                          });
                                       }
                                       else
                                       {
                                          view.submitChanges({
                                             attributes : {
                                                "carnot:engine:visibility" : "Private"
                                             }
                                          });
                                       }
                                    });
                  this.setOverlay("decoratorApplicationView");
                  this.setModelElement(application);
                  this.update();
               };
               DecoratorApplicationView.prototype.i18nLabels = function(key)
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
                  labels['model.name.title'] = m_i18nUtils
                           .getProperty("modeler.element.properties.decoratorApplication.model.name");
                  labels['header.dataType.title'] = m_i18nUtils
                           .getProperty("modeler.element.properties.commonProperties.dataType");
                  labels['header.direction.title'] = m_i18nUtils
                           .getProperty("modeler.element.properties.decoratorApplication.direction.name");
                  labels['header.type.title'] = m_i18nUtils
                           .getProperty("modeler.element.properties.decoratorApplication.application.type");

                  labels['none.title'] = m_i18nUtils
                           .getProperty("modeler.element.properties.commonProperties.none");
                  labels['application.name.title'] = m_i18nUtils
                           .getProperty("modeler.element.properties.decoratorApplication.application.name");
                  labels['inputs.title'] = m_i18nUtils
                           .getProperty("modeler.element.properties.decoratorApplication.inputs");
                  labels['outputs.title'] = m_i18nUtils
                           .getProperty("modeler.element.properties.decoratorApplication.outputs");
                  labels['header.name.title'] = m_i18nUtils
                           .getProperty("modeler.element.properties.decoratorApplication.header.name");

                  return labels;
               }
               ;
               /**
                * 
                */
               DecoratorApplicationView.prototype.getApplication = function()
               {
                  return this.application;
               };

               /**
                * 
                */
               DecoratorApplicationView.prototype.setModelElement = function(application)
               {
                  this.application = application;
                  m_utils.debug("===> Application");
                  m_utils.debug(application);

                  this.initializeModelElement(application);

                  if (!this.application.attributes["carnot:engine:visibility"]
                           || "Public" == this.application.attributes["carnot:engine:visibility"])
                  {
                     this.publicVisibilityCheckbox.attr("checked", true);
                  }
                  else
                  {
                     this.publicVisibilityCheckbox.attr("checked", false);
                  }
               };
               /**
                * 
                */
               DecoratorApplicationView.prototype.update = function()
               {
                  this.modelId = this
                           .getExtendedAttributeValue("stardust:application::decorator::modelId");
                  this.eltId = this
                           .getExtendedAttributeValue("stardust:application::decorator::elementId");
                  this.elementType = this
                           .getExtendedAttributeValue("stardust:application::decorator::elementType");
                  if (this.elementType == "application")
                  {
                     var application=this.getSelectedApplication();
                     if(!application){
                        m_utils.debug("===> The Application"+this.eltId+" is not available. resetting current Application");
                        this.resetDecoratorApplication();
                     }
                  }
                  else
                  {
                    var pd= this.getSelectedProcessDefinition();
                    if(!pd || !this.hasProcessInterface(pd)){
                       m_utils.debug("===> The Process Definition "+this.eltId+" is not available. resetting current Application");
                       this.resetDecoratorApplication();
                    }
                  }
               };
               DecoratorApplicationView.prototype.resetViewElements= function(){
                  this.eltId=null;
                  this.elementType=null;
               }
               DecoratorApplicationView.prototype.resetDecoratorApplication= function(){
                  var attributes = this.getApplication().attributes;
                  attributes["stardust:application::decorator::elementId"] = null;
                  attributes["stardust:application::decorator::elementType"] = null;
                  var submitElements = {};
                  submitElements.attributes=attributes;
                  submitElements.contexts = {
                           application : {
                              accessPoints : null
                           }
                        };
                  this.submitChanges(submitElements, true);
                  this.accessPoints= angular.copy(this.getApplication().contexts.application.accessPoints);
                  this.resetViewElements();
                  
               }
               this.modelId;
               this.eltId;
               this.elementType;
               
               /**
                * 
                */
               DecoratorApplicationView.prototype.getAvailableModels = function()
               {
                  var models = m_model.getModels();
                  var availableModels = [];
                  for ( var i in models)
                  {
                     var model = models[i];
                     if (model.id != this.getApplication().model.id)
                        availableModels.push(model);
                  }
                  return availableModels;
               };
               /**
                * 
                */
               DecoratorApplicationView.prototype.modelIdChanged = function()
               {
                  if (!this.validate())
                     return;
                  m_utils.debug("===> Application ID Change" + this.modelId);
                  this.submit();
               };

               /**
                * 
                */
               DecoratorApplicationView.prototype.getAvailableElements = function()
               {
                  var elts = {};
                  elts.applications = [];
                  elts.processes = [];

                  if (this.modelId)
                  {
                     var model = m_model.findModel(this.modelId);
                     for ( var appId in model.applications)
                     {
                        var app = model.applications[appId];
                        elts.applications.push(app);
                     }

                     for ( var j in model.processes)
                     {
                        var process = model.processes[j]
                        if (this.hasProcessInterface(process))
                        {
                           elts.processes.push(process);
                        }
                     }
                  }
                  return elts;
               };
               var selectedApplication;
               var selectedProcessDefinition;
               this.accessPoints;
               /**
                * 
                */
               DecoratorApplicationView.prototype.elementChanged = function()
               {
                  if (!this.validate())
                     return;
                  m_utils.debug("===> Element ID Change" + this.eltId);
                  var attributes = this.getApplication().attributes;
                  var submitElements = {};
                  this.elementType = null;
                  this.elementType = this.getElementType(this.eltId);

                  if (this.elementType == "application")
                  {
                     this.selectedApplication = this
                              .findSelectedApplicationById(this.eltId);
                     if (this.selectedApplication)
                     {
                      //  var accessPoints = this.selectedApplication.contexts.application.accessPoints;
                        attributes["stardust:application::decorator::elementId"] = this.eltId;
                        attributes["stardust:application::decorator::elementType"] = this.elementType;
                        submitElements.contexts = {
                           application : {
                              accessPoints : this.selectedApplication.contexts.application.accessPoints
                           }
                        };
                     }
                     else
                     {
                        attributes["stardust:application::decorator::elementId"] = null;
                        attributes["stardust:application::decorator::elementType"] = null;
                        this.elementType = null;
                        this.elementId = null;
                     }
                     this.application.contexts = this.selectedApplication.contexts;
                  }
                  else if (this.elementType == "process")
                  {
                     this.selectedProcessDefinition = this
                              .findSelectedProcessDefinitionById(this.eltId);
                     attributes["stardust:application::decorator::elementId"] = this.eltId;
                     attributes["stardust:application::decorator::elementType"] = this.elementType;
                     submitElements.contexts = {
                        application : {
                           accessPoints : this.selectedProcessDefinition.formalParameters
                        }
                     };
                  }
                  else
                  {
                     attributes["stardust:application::decorator::elementId"] = null;
                     attributes["stardust:application::decorator::elementType"] = null;
                     this.elementType = null;
                     this.elementId = null;
                     submitElements.contexts = {
                        application : {
                           accessPoints : null
                        }
                     };
                  }
                  submitElements.attributes = attributes;
                  this.submitChanges(submitElements, true);
                  this.accessPoints= angular.copy(submitElements.contexts.application.accessPoints);
               };

               /**
                * 
                */

				DecoratorApplicationView.prototype.valueChanged = function(ap)
				{
					m_utils.debug("===>Value changed for " + ap.id);
					if (ap.attributes["carnot:engine:defaultValue"]){
						m_utils.debug("Default value "
								  + ap.attributes["carnot:engine:defaultValue"]);

						if (ap.primitiveDataType=="Timestamp"){
							var date=jQuery.datepicker.parseDate("yy/mm/dd",  ap.attributes["carnot:engine:defaultValue"]);
							if(date){
								ap.attributes["carnot:engine:defaultValue"]=jQuery.datepicker.formatDate("yy/mm/dd",date )+" 00:00:00:000";
							}
						}
					}
					var submitElements = {};
					var attributes = this.getApplication().attributes;
					submitElements.attributes = attributes;
					submitElements.contexts = {
						application : {
							accessPoints : this.accessPoints
						}
					};
					this.submitChanges(submitElements, true); 
				};

               /**
                * Returns a list of available structured types in all Models
                */
               DecoratorApplicationView.prototype.getAllStructuredTypes = function()
               {
                  var models = m_model.getModels();
                  var typeDeclarations = [];
                  for ( var i in models)
                  {
                     var model = models[i];
                     for ( var j in model.typeDeclarations)
                     {
                        var typeDeclaration = model.typeDeclarations[j]
                        typeDeclarations.push(typeDeclaration);
                     }
                  }
                  return typeDeclarations;
               };

               DecoratorApplicationView.prototype.findSelectedProcessDefinitionById = function(
                        pdId)
               {
                  var pd;
                  if (!m_utils.isEmptyString(pdId))
                  {
                     pd = m_model.findProcess(pdId);
                  }
                  return pd;
               }
               DecoratorApplicationView.prototype.getSelectedProcessDefinition = function()
               {
                  if (!this.selectedProcessDefinition)
                     this.selectedProcessDefinition = this
                              .findSelectedProcessDefinitionById(this.eltId);
                  return this.selectedProcessDefinition;
               }

               /**
                * 
                */
               DecoratorApplicationView.prototype.findSelectedApplicationById = function(
                        applicationId)
               {
                  var application;
                  if (!m_utils.isEmptyString(applicationId))
                  {
                     application = m_model.findApplication(applicationId);
                  }
                  return application;
               }
               /**
                * 
                */
               DecoratorApplicationView.prototype.getSelectedApplication = function()
               {
                  if (!this.selectedApplication)
                     this.selectedApplication = this
                              .findSelectedApplicationById(this.eltId);
                  return this.selectedApplication;
               }

               /**
                * Returns true is the Process definition has process interfaces
                */
               DecoratorApplicationView.prototype.hasProcessInterface = function(process)
               {
                  var hasProcessInterface = false;
                  if (process.processInterfaceType != "noInterface")
                  {
                     hasProcessInterface = true;
                  }
                  return hasProcessInterface;
               };

               /**
                * Used to inject AngularJS context
                */
               DecoratorApplicationView.prototype.setOverlay = function(overlay)
               {
                  var deferred = jQuery.Deferred();
                  this.overlayAnchor.empty();

                  var self = this;
                  var success = true;
                  if (success)
                  {
                     deferred.resolve();
                     self.safeApply(true);
                  }
                  else
                  {
                     deferred.reject();
                  }

                  return deferred.promise();
               };

               /**
                *
                */
               DecoratorApplicationView.prototype.safeApply = function(force)
               {
                  var self = this;
                  m_angularContextUtils.runInActiveViewContext(function($scope)
                  {
                     if (force)
                     {
                        $scope.overlayPanel = self;
                     }
                  });
               };

               DecoratorApplicationView.prototype.toString = function()
               {
                  return "Lightdust.DecoratorApplicationView";
               };
               /**
                * Returns the Extended attribute value
                */
               DecoratorApplicationView.prototype.getExtendedAttributeValue = function(
                        key)
               {
                  return this.getApplication().attributes[key];
               };
               /*
                *
                */
               DecoratorApplicationView.prototype.validate = function()
               {
                  this.clearErrorMessages();
                  /*this.availableApplicationsInput.removeClass("error");
                  this.processInterfacesInput.removeClass("error");

                  if(!m_utils.isEmptyString(this.applicationId) && !m_utils.isEmptyString(this.processId)){
                      this.errorMessages.push("Please select an application or a process Name. You cannot have both selected.");
                      this.availableApplicationsInput.addClass("error");
                      this.processInterfacesInput.addClass("error");
                  }
                   */
                  if (this.errorMessages.length > 0)
                  {
                     this.showErrorMessages();

                     return false;
                  }

                  return true;
               };
               DecoratorApplicationView.prototype.getElementType = function(elementId)
               {
                  if (elementId)
                  {
                     var app = this.findSelectedApplicationById(elementId);
                     if (app != null && app.type == "application")
                        this.elementType = "application";
                     if (!app)
                     {
                        var process = m_model.findProcess(elementId);
                        if (process != null && process.type == "processDefinition")
                           this.elementType = "process";
                     }
                  }
                  if (this.elementType)
                     return this.elementType;
                  return;
               }
               /**
                *
                */
               DecoratorApplicationView.prototype.submit = function()
               {

                  var attributes = this.getApplication().attributes;
                  attributes["stardust:application::decorator::modelId"] = this.modelId;
                  attributes["stardust:application::decorator::elementId"] = this.eltId;
                  attributes["stardust:application::decorator::elementType"] = this
                           .getElementType(this.eltId);
               };
            }
         });