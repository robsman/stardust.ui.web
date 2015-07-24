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
     "bpm-modeler/js/m_angularContextUtils", "bpm-modeler/js/m_extensionManager"],
    function (m_utils, m_constants, m_urlUtils, m_session, m_command,
               m_commandsController, m_dialog, m_modelElementView, m_model,
               m_dataTypeSelector, m_parameterDefinitionsPanel,
               m_codeEditorAce, m_i18nUtils, m_angularContextUtils, m_extensionManager) {
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
            
        };

        /**
         *
         */
        function TemplateApplicationView() {
       /*     var view = m_modelElementView.create(true);

            m_utils.inheritFields(this, view);
            m_utils.inheritMethods(TemplateApplicationView.prototype, view);
*/
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
                this.availableApplicationsInput = m_utils.jQuerySelect("#configurationTab #availableApplicationsInput");
                this.processInterfacesInput = m_utils.jQuerySelect("#configurationTab #processInterfacesInput");
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
                this.applicationId=this.getExtendedAttributeValue("stardust:application::template::applicationId");
                this.processId=this.getExtendedAttributeValue("stardust:application::template::processId");
            };
            /**
            *
            */
            TemplateApplicationView.prototype.getAvailableApplications = function () {
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
            this.applicationId;
            /**
            *
            */
            TemplateApplicationView.prototype.applicationIdChanged = function () {
                if(!this.validate())
                    return;
                m_utils.debug("===> Application ID Change" + this.applicationId);
                this.submit();
            };
            
            /**
            *
            */
            TemplateApplicationView.prototype.getSelectedApplication = function () {
                var application;
                if(!m_utils.isEmptyString(this.applicationId)){
                    application = m_model.findApplication(this.applicationId);
                }
                return application;
            }
            

            this.processId; //references the process Id selected in the available Processes dropdown
            /**
            * Invoked when change event occurs in availaleProcessesSelect
            */
            TemplateApplicationView.prototype.processIdChanged = function () {
                if(!this.validate())
                    return;
                m_utils.debug("===> Process ID change" + this.processId);
                this.submit();
            }

            /**
             *Returns a list of processes having Formal Parameters
             */
            TemplateApplicationView.prototype.getProcessesHavingFormalParameters = function () {
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
            };

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
                this.availableApplicationsInput.removeClass("error");
                this.processInterfacesInput.removeClass("error");

                if(!m_utils.isEmptyString(this.applicationId) && !m_utils.isEmptyString(this.processId)){
                    this.errorMessages.push("Please select an application or a process Name. You cannot have both selected.");
                    this.availableApplicationsInput.addClass("error");
                    this.processInterfacesInput.addClass("error");
                }
                
                if (this.errorMessages.length > 0) {
                    this.showErrorMessages();

                    return false;
                }

                return true;
            };
            /**
             *
             */
            TemplateApplicationView.prototype.submit= function () {
                var attributes=this.getApplication().attributes;
                attributes["stardust:application::template::applicationId"]=this.applicationId;
                attributes["stardust:application::template::processId"]=this.processId;
                this
                  .submitChanges(
                           {
                              attributes :attributes
                           }, false);
            };

        }

    });