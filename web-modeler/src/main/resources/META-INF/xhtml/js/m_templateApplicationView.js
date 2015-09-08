/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
    ["bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
     "bpm-modeler/js/m_urlUtils", "bpm-modeler/js/m_session",
     "bpm-modeler/js/m_command",
     "bpm-modeler/js/m_commandsController",
     "bpm-modeler/js/m_dialog", "bpm-modeler/js/m_modelElementView",
     "bpm-modeler/js/m_model", "bpm-modeler/js/m_dataTypeSelector",
     "bpm-modeler/js/m_parameterDefinitionsPanel",
     "bpm-modeler/js/m_codeEditorAce", "bpm-modeler/js/m_i18nUtils",
     "bpm-modeler/js/m_angularContextUtils", "bpm-modeler/js/m_extensionManager","bpm-modeler/js/m_routeDefinitionUtils"],
    function (m_utils, m_constants, m_urlUtils, m_session, m_command,
               m_commandsController, m_dialog, m_modelElementView, m_model,
               m_dataTypeSelector, m_parameterDefinitionsPanel,
               m_codeEditorAce, m_i18nUtils, m_angularContextUtils, m_extensionManager,m_routeDefinitionUtils) {
        return {
            initialize: function (fullId) {
                m_utils.initializeWaitCursor(m_utils.jQuerySelect("html"));
                m_utils.showWaitCursor();

                m_utils.jQuerySelect("#hideGeneralProperties").hide();
                initViewCollapseClickHandlers();

                var view = new TemplateApplicationView();
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
        function initViewCollapseClickHandlers() {
            m_utils.jQuerySelect("#showGeneralProperties").click(function () {
                m_utils.jQuerySelect("#showAllProperties").hide();
                m_utils.jQuerySelect("#hideGeneralProperties").show();
            });
            m_utils.jQuerySelect("#hideGeneralProperties").click(function () {
                m_utils.jQuerySelect("#showAllProperties").show();
                m_utils.jQuerySelect("#hideGeneralProperties").hide();
            });
        }

        function i18uimashupproperties() {

            m_utils.jQuerySelect("#hideGeneralProperties label")
                .text(m_i18nUtils.getProperty("modeler.element.properties.commonProperties.generalProperties"));

            m_utils.jQuerySelect("#showGeneralProperties label")
                .text(m_i18nUtils.getProperty("modeler.element.properties.commonProperties.generalProperties"));

            m_utils.jQuerySelect("label[for='guidOutput']")
                .text(
                m_i18nUtils
                .getProperty("modeler.element.properties.commonProperties.uuid"));
            m_utils.jQuerySelect("label[for='idOutput']")
                .text(
                m_i18nUtils
                .getProperty("modeler.element.properties.commonProperties.id"));

            m_utils.jQuerySelect("#applicationName")
                .text(
                m_i18nUtils
                .getProperty("modeler.element.properties.commonProperties.applicationName"));
            m_utils.jQuerySelect("#description")
                .text(
                m_i18nUtils
                .getProperty("modeler.element.properties.commonProperties.description"));

            m_utils.jQuerySelect("#configuration")
                .text(
                m_i18nUtils
                .getProperty("modeler.element.properties.commonProperties.configuration"));

            m_utils.jQuerySelect("label[for='publicVisibilityCheckbox']")
                .text(
                m_i18nUtils
                .getProperty("modeler.element.properties.commonProperties.publicVisibility"));
            m_utils.jQuerySelect("label[for='availableApplicationsInput']").text(m_i18nUtils.getProperty("modeler.element.properties.applicationTemplate.availableApplications"));
            m_utils.jQuerySelect("label[for='processInterfacesInput']").text(m_i18nUtils.getProperty("modeler.element.properties.applicationTemplate.processInterfaces"));
            m_utils.jQuerySelect("#noSelectionErrorMessage").text(m_i18nUtils.getProperty("modeler.element.properties.applicationTemplate.noSelectionErrorMessage"));
            
           // m_utils.jQuerySelect("label[for='availableModelsInput']").text(m_i18nUtils.getProperty("modeler.element.properties.applicationTemplate.availableApplications"));
           // m_utils.jQuerySelect("label[for='availableModelsInput']").text(m_i18nUtils.getProperty("modeler.element.properties.applicationTemplate.availableAppsOrProcInter"));
        };

        /**
         *
         */
        function TemplateApplicationView() {
            var modelElementView = m_modelElementView.create(true);
            m_utils.inheritFields(this, modelElementView);
            m_utils.inheritMethods(TemplateApplicationView.prototype,
                  modelElementView);
            /**
             *
             */
            TemplateApplicationView.prototype.initialize = function (
            application) {
                this.id = "templateApplicationView";
                this.application = application;
                this.view = m_utils.jQuerySelect("#" + this.id);
                this.initializeModelElementView(application);
                this.overlayAnchor = m_utils.jQuerySelect("#overlayAnchor");

                this.publicVisibilityCheckbox = m_utils.jQuerySelect("#publicVisibilityCheckbox");
              /*  this.availableApplicationsInput = m_utils.jQuerySelect("#configurationTab #availableApplicationsInput");
                this.processInterfacesInput = m_utils.jQuerySelect("#configurationTab #processInterfacesInput");*/
                this.view.css("visibility", "visible");
                this.publicVisibilityCheckbox.change({
                    "view": this
                },
                                                     function (event) {
                    var view = event.data.view;

                    if (view.modelElement.attributes["carnot:engine:visibility"] && view.modelElement.attributes["carnot:engine:visibility"] != "Public") {
                        view
                            .submitChanges({
                            attributes: {
                                "carnot:engine:visibility": "Public"
                            }
                        });
                    } else {
                        view
                            .submitChanges({
                            attributes: {
                                "carnot:engine:visibility": "Private"
                            }
                        });
                    }
                });
                this.setOverlay("templateApplicationView");
                this.update();
            };
               TemplateApplicationView.prototype.i18nLabels = function(key)
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
                  labels['model.name.title'] = m_i18nUtils.getProperty("modeler.element.properties.applicationTemplate.model.name");
                  labels['header.dataType.title'] = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.dataType");
                  labels['header.direction.title'] = m_i18nUtils.getProperty("modeler.element.properties.applicationTemplate.direction.name");
                  labels['header.type.title'] = m_i18nUtils.getProperty("modeler.element.properties.applicationTemplate.application.type");
                  
                  
                  
                  labels['none.title'] = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.none");
                  labels['application.name.title'] = m_i18nUtils.getProperty("modeler.element.properties.applicationTemplate.application.name");
                  labels['inputs.title'] = m_i18nUtils.getProperty("modeler.element.properties.applicationTemplate.inputs");
                  labels['outputs.title'] = m_i18nUtils.getProperty("modeler.element.properties.applicationTemplate.outputs");
                  labels['header.name.title'] = m_i18nUtils.getProperty("modeler.element.properties.applicationTemplate.header.name");
                  
                   
                  return labels;
               };
            /**
             *
             */
            TemplateApplicationView.prototype.getApplication = function () {
                return this.application;
            };

            /**
             *
             */
            TemplateApplicationView.prototype.setModelElement = function (
            application) {
                this.application = application;

                m_utils.debug("===> Application");
                m_utils.debug(application);

                this.initializeModelElement(application);

                if (!this.application.attributes["carnot:engine:visibility"] || "Public" == this.application.attributes["carnot:engine:visibility"]) {
                    this.publicVisibilityCheckbox.attr("checked", true);
                } else {
                    this.publicVisibilityCheckbox.attr("checked", false);
                }
            };
            /**
            *
            */
            TemplateApplicationView.prototype.update = function () {
                this.modelId=this.getExtendedAttributeValue("stardust:application::template::modelId");
                this.eltId=this.getExtendedAttributeValue("stardust:application::template::elementId");
                this.elementType=this.getExtendedAttributeValue("stardust:application::template::elementType");
                this.getSelectedApplication();
//                this.accessPointsConfiguration=[];
//                this.accessPointsConfiguration= this.populateAccessPointsConfiguration();
//
//                var providedConfigurationString=this.getExtendedAttributeValue("stardust:application::template::configuration");
//                if(providedConfigurationString){
//                	//retrieve value for a certain AP; if
//                	var providedConfiguration=JSON.parse(providedConfigurationString);
//                	this.accessPointsConfiguration=this.updateValuesForAccessPointsConfiguration(this.accessPointsConfiguration, providedConfiguration);
//                }
                
                
                
            };
//            /**
//            *
//            */
//            TemplateApplicationView.prototype.updateValuesForAccessPointsConfiguration = function (apConfiguration, configuration) { 
//        		var response=[];
//        		for(var i in apConfiguration)
//            	{
//        			var elt=apConfiguration[i];
//        			var providedConfig=this.findConfigurationById(configuration, elt.ap.id);
//        			if(providedConfig)
//        				response.push({"ap":elt.ap,"value":providedConfig.value});
//        			else
//        				response.push({"ap":elt.ap,"value":""});
//            	}
//            	
//            	return response;
//            }
//            TemplateApplicationView.prototype.findConfigurationById= function (
//            		configuration, id)
//           {
//              var elt = null;
//              for (var n = 0; n < configuration.length; n++)
//              {
//                 var ap = configuration[n].ap;
//                 if (ap.id == id)
//                 {
//                    elt = configuration[n];
//                    break;
//                 }
//              }
//              return elt;
//           }
            
            
//            /**
//            *
//            */
//            TemplateApplicationView.prototype.populateAccessPointsConfiguration = function () { 
//            	var app=this.application;//getSelectedApplication();
//            	var response=[];
//            	if(app){
//            		configurationElements=[];
//            		for(var i in app.contexts.application.accessPoints)
//	            	{
//            			var ap=app.contexts.application.accessPoints[i];
//            			
//            			response.push({"ap":ap,"value":""});
//	            	}
//            	
//            	}
//            	return response;
//            }
            
            
            this.modelId;
            this.eltId;
            this.elementType;
            /**
            *
            */
            TemplateApplicationView.prototype.getAvailableModels = function () {
                var models = m_model.getModels();
                var availableModels = [];
                for (var i in models) {
                    var model = models[i];
                    availableModels.push(model);
                }
                return availableModels;
            };
            /**
            *
            */
            TemplateApplicationView.prototype.modelIdChanged = function () {
                if(!this.validate())
                    return;
                m_utils.debug("===> Application ID Change" + this.modelId);
                this.submit();
            };
            
           
           /**
            *
            */
            TemplateApplicationView.prototype.getAvailableElements = function () { 
                var elts={};
                elts.applications=[];
                elts.processes=[];
                
                if(this.modelId){
                    var model = m_model.findModel(this.modelId);
                    for (var appId in model.applications) {
                        var app=model.applications[appId];
                        elts.applications.push(app);
                    }
                    
                    for (var j in model.processes) {
                        var process = model.processes[j]
                        if (this.hasProcessInterface(process)) {
                            elts.processes.push(process);
                        }
                    }
                }
                return elts;
            };
            var selectedApplication;
             /**
            *
            */
            TemplateApplicationView.prototype.elementChanged = function () {
                if(!this.validate())
                    return;
                m_utils.debug("===> Element ID Change" + this.eltId);
                var attributes = this.getApplication().attributes;
                var submitElements = {};
                this.selectedApplication=this.findSelectedApplicationById(this.eltId);
                if(this.selectedApplication){
                	var accessPoints= this.selectedApplication.contexts.application.accessPoints;
                	
                	attributes["stardust:application::template::elementId"]=this.eltId;
                	attributes["stardust:application::template::elementType"]=this.getElementType(this.eltId);
                	submitElements.contexts = {
                            application : {
                               accessPoints : accessPoints
                            }
                         };
                }else{
                	attributes["stardust:application::template::elementId"]=null;
                	attributes["stardust:application::template::elementType"]=null;
                	this.elementType=null;
                	this.elementId=null;
                }
                	
                submitElements.attributes = attributes;
                this.submitChanges(submitElements, true);
                this.application.contexts=this.selectedApplication.contexts;
               // this.accessPointsConfiguration= this.populateAccessPointsConfiguration();
                //this.submit();
            };
            
            /**
            *
            */
            TemplateApplicationView.prototype.valueChanged = function (ap) { 
            	m_utils.debug("===>Value changed for " + ap.id);
            	if(ap.attributes["carnot:engine:defaultValue"])
            		m_utils.debug("Default value " + ap.attributes["carnot:engine:defaultValue"]);

	            var accessPoints= this.getApplication().contexts.application.accessPoints;
            	for (var i in accessPoints) {
                    var elt=accessPoints[i];
                    if(elt.id==ap.id){
                    	
                    	elt.attributes["carnot:engine:visibility"]=false;
                    	elt.attributes["carnot:engine:defaultValue"]=ap.attributes["carnot:engine:defaultValue"];
                    	
                    }
            	}
                var submitElements = {};
                var attributes = this.getApplication().attributes;
                submitElements.attributes = attributes;
                submitElements.contexts = {
                        application : {
                           accessPoints : accessPoints
                        }
                     };
                
                this.submitChanges(submitElements, true);
            }
            /**
            *
            */
            /*TemplateApplicationView.prototype.getAvailableApplications = function () {
                var models = m_model.getModels();
                var availableApplications = [];
                for (var i in models) {
                    var model = models[i];
                    for (var appId in model.applications) {
                        var app=model.applications[appId];
                        if(app.getFullId()!=this.getApplication().getFullId())
                            availableApplications.push(model.applications[appId]);
                    }
                }
                return availableApplications;
            }
            this.applicationId;*/
            /**
            *
            */
            /*TemplateApplicationView.prototype.applicationIdChanged = function () {
                if(!this.validate())
                    return;
                m_utils.debug("===> Application ID Change" + this.applicationId);
                this.submit();
            };*/
            
            /**
            *
            */
           /* TemplateApplicationView.prototype.getSelectedApplication = function () {
                var application;
                if(!m_utils.isEmptyString(this.applicationId)){
                    application = m_model.findApplication(this.applicationId);
                }
                return application;
            }*/
            

            //this.processId; //references the process Id selected in the available Processes dropdown
            /**
            * Invoked when change event occurs in availaleProcessesSelect
            */
         /*   TemplateApplicationView.prototype.processIdChanged = function () {
                if(!this.validate())
                    return;
                m_utils.debug("===> Process ID change" + this.processId);
                this.submit();
            }*/

            /**
             *Returns a list of processes having Formal Parameters
             */
           /* TemplateApplicationView.prototype.getProcessesHavingFormalParameters = function () {
                var models = m_model.getModels();
                var processes = [];
                for (var i in models) {
                    var model = models[i];
                    for (var j in model.processes) {
                        var process = model.processes[j]
                        if (this.hasProcessInterface(process)) {
                            processes.push(process);
                        }
                    }
                }
                return processes;
            };*/

            /**
             *Returns a list of available structured types in all Models
             */
            TemplateApplicationView.prototype.getAllStructuredTypes = function () {
                var models = m_model.getModels();
                var typeDeclarations = [];
                for (var i in models) {
                    var model = models[i];
                    for (var j in model.typeDeclarations) {
                        var typeDeclaration = model.typeDeclarations[j]
                            typeDeclarations.push(typeDeclaration);
                    }
                }
                return typeDeclarations;
            };
            
            /**
            *
            */
            
            TemplateApplicationView.prototype.findSelectedApplicationById = function (applicationId) {
                var application;
                if(!m_utils.isEmptyString(applicationId)){
                    application = m_model.findApplication(applicationId);
                }
                return application;
            }
            TemplateApplicationView.prototype.getSelectedApplication = function () {
            	if(!this.selectedApplication)
            		this.selectedApplication=this.findSelectedApplicationById(this.eltId);
                return this.selectedApplication;
            }
            
            /**
            * Returns true is the Process definition has process interfaces
            */
           TemplateApplicationView.prototype.hasProcessInterface = function (process) {
                var hasProcessInterface = false;
                if (process.processInterfaceType != "noInterface") {
                    hasProcessInterface = true;
                }
                return hasProcessInterface;
            };

            /**
             * Used to inject AngularJS context
             */
            TemplateApplicationView.prototype.setOverlay = function (overlay) {
                var deferred = jQuery.Deferred();
                this.overlayAnchor.empty();

                var self = this;
                var success = true;
                if (success) {
                    deferred.resolve();
                    self.safeApply(true);
                } else {
                    deferred.reject();
                }

                return deferred.promise();
            };

            /**
             *
             */
            TemplateApplicationView.prototype.safeApply = function (force) {
                var self = this;
                m_angularContextUtils.runInActiveViewContext(function ($scope) {
                    if (force) {
                        $scope.overlayPanel = self;
                    }
                });
            };

            TemplateApplicationView.prototype.toString = function () {
                return "Lightdust.TemplateApplicationView";
            };
           /**
            * Returns the Extended attribute value
            */
           TemplateApplicationView.prototype.getExtendedAttributeValue = function(key)
           {
              return this.getApplication().attributes[key];
           };
            /*
             *
             */
            TemplateApplicationView.prototype.validate = function () {
                this.clearErrorMessages();
                /*this.availableApplicationsInput.removeClass("error");
                this.processInterfacesInput.removeClass("error");

                if(!m_utils.isEmptyString(this.applicationId) && !m_utils.isEmptyString(this.processId)){
                    this.errorMessages.push("Please select an application or a process Name. You cannot have both selected.");
                    this.availableApplicationsInput.addClass("error");
                    this.processInterfacesInput.addClass("error");
                }
                */
                if (this.errorMessages.length > 0) {
                    this.showErrorMessages();

                    return false;
                }

                return true;
            };
            TemplateApplicationView.prototype.getElementType= function (elementId) {
            	if(elementId){
                    var app=this.findSelectedApplicationById(elementId);
                    if(app!=null && app.type=="application")
                    	this.elementType= "application";
                    if(!app){
                        var process=m_model.findProcess(elementId);
                        if(process!=null && process.type=="processDefinition")
                        	this.elementType= "process";
                    }
                }
                if(this.elementType)
                	return this.elementType;
                return ;
            }
            /**
             *
             */
            TemplateApplicationView.prototype.submit= function () {
            
                var attributes=this.getApplication().attributes;
                attributes["stardust:application::template::modelId"]=this.modelId;
                attributes["stardust:application::template::elementId"]=this.eltId;
                attributes["stardust:application::template::elementType"]=this.getElementType(this.eltId);

            };

        }

    });