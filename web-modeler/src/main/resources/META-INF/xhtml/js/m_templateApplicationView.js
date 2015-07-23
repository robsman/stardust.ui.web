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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_urlUtils", "bpm-modeler/js/m_session",
				"bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_dialog", "bpm-modeler/js/m_modelElementView",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_dataTypeSelector",
				"bpm-modeler/js/m_parameterDefinitionsPanel",
				"bpm-modeler/js/m_codeEditorAce", "bpm-modeler/js/m_i18nUtils", 
				"bpm-modeler/js/m_angularContextUtils", "bpm-modeler/js/m_extensionManager" ],
		function(m_utils, m_constants, m_urlUtils, m_session, m_command,
				m_commandsController, m_dialog, m_modelElementView, m_model,
				m_dataTypeSelector, m_parameterDefinitionsPanel,
				m_codeEditorAce, m_i18nUtils, m_angularContextUtils, m_extensionManager) {
			return {
				initialize : function(fullId) {
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
				m_utils.jQuerySelect("#showGeneralProperties").click(function() {
					m_utils.jQuerySelect("#showAllProperties").hide();
					m_utils.jQuerySelect("#hideGeneralProperties").show();
				});
				m_utils.jQuerySelect("#hideGeneralProperties").click(function() {
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
			}

			/**
			 *
			 */
			function TemplateApplicationView() {
				var view = m_modelElementView.create(true);

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(TemplateApplicationView.prototype, view);

				/**
				 *
				 */
				TemplateApplicationView.prototype.initialize = function(
						application) {
					this.id = "templateApplicationView";
					this.view = m_utils.jQuerySelect("#" + this.id);
					this.initializeModelElementView(application);

					
					this.publicVisibilityCheckbox = m_utils.jQuerySelect("#publicVisibilityCheckbox");
                    this.availableApplicationsInput = m_utils.jQuerySelect("#configurationTab #availableApplicationsInput");
                    this.processInterfacesInput = m_utils.jQuerySelect("#configurationTab #processInterfacesInput");
                    this.view.css("visibility", "visible");
                    this.publicVisibilityCheckbox.change(
                    {
                        "view" : this
                    },
                    function(event) {
                        var view = event.data.view;

                        if (view.modelElement.attributes["carnot:engine:visibility"]
                                && view.modelElement.attributes["carnot:engine:visibility"] != "Public") {
                            view
                                    .submitChanges({
                                        attributes : {
                                            "carnot:engine:visibility" : "Public"
                                        }
                                    });
                        } else {
                            view
                                    .submitChanges({
                                        attributes : {
                                            "carnot:engine:visibility" : "Private"
                                        }
                                    });
                        }
                    });
                    
                    this.availableApplicationsInput.change(
                    {
                        "view" : this
                    },
                    function(event) {
                        var view = event.data.view;
                    
                     });
                    this.processInterfacesInput.change(
                    {
                        "view" : this
                    },
                    function(event) {
                        var view = event.data.view;
                    
                     });
                    this.update();
				};

				/**
				 *
				 */
				TemplateApplicationView.prototype.getApplication = function() {
					return this.application;
				};



				/**
             * 
             */
				TemplateApplicationView.prototype.setModelElement = function(
                  application) {
               this.application = application;

               m_utils.debug("===> Application");
               m_utils.debug(application);

               this.initializeModelElement(application);

               if (!this.application.attributes["carnot:engine:visibility"]
                     || "Public" == this.application.attributes["carnot:engine:visibility"]) {
                  this.publicVisibilityCheckbox.attr("checked", true);
               } else {
                  this.publicVisibilityCheckbox.attr("checked", false);
               }

               m_utils.debug("===> Updating Overlay");

            };
            TemplateApplicationView.prototype.update = function() {
               var models=m_model.getModels();
                this.populateApplicationsSelect(models);
                this.populateProcessInterfacesSelect(models);
               
            }
            TemplateApplicationView.prototype.populateApplicationsSelect = function(models) {
                this.availableApplicationsInput.empty();
                this.availableApplicationsInput.append("<option value=\"" + m_constants.TO_BE_DEFINED + "\">" + m_i18nUtils.getProperty("None") + "</option>");
                for ( var i in models) {
                    var model=models[i];
                    this.availableApplicationsInput.append("<optgroup label=\""+model.name+"\">");
                    var availableApplications=model.applications;
                    for ( var id in availableApplications) {
                        this.availableApplicationsInput.append("<option value=\"" +availableApplications[id].getFullId() + "\">  " + availableApplications[id].name + "</option>");        
                        
                    }
                    this.availableApplicationsInput.append("</optgroup>");
                }
            };
            TemplateApplicationView.prototype.populateProcessInterfacesSelect = function(models) {
                this.processInterfacesInput.empty();
                this.processInterfacesInput.append("<option value=\"" + m_constants.TO_BE_DEFINED + "\">" + m_i18nUtils.getProperty("None") + "</option>");
                for ( var i in models) {
                    var model=models[i];
                    if(this.hasProcessInterface(model)){
                        this.processInterfacesInput.append("<optgroup label=\""+model.name+"\">");
                        var processes=model.processes;
                        for ( var j in processes) {
                            var process=processes[j];
                             this.processInterfacesInput.append("<optgroup label=\""+process.name+"\">");
                            if(process.processInterfaceType!="noInterface"){
                                var formalParameters=process.formalParameters;
                                for ( var k in formalParameters) {
                                    var formalParameter=formalParameters[k];
                                    this.processInterfacesInput.append("<option value=\"" +formalParameter.dataFullId + "\">    " + formalParameter.name + "</option>");        
                                }
                            }
                            this.processInterfacesInput.append("</optgroup>");
                        }
                        this.processInterfacesInput.append("</optgroup>");
                    }
                }
            };
            
            TemplateApplicationView.prototype.hasProcessInterface = function(model) {
                var hasProcessInterface=false;
                var processes=model.processes;
                for ( var j in processes) {
                    var process=processes[j];
                    if(process.processInterfaceType!="noInterface"){
                        hasProcessInterface= true;
                        break;
                    }
                }
                
                return hasProcessInterface;
            };
				/**
				 *
				 */
				TemplateApplicationView.prototype.toString = function() {
					return "Lightdust.TemplateApplicationView";
				};

				/*
				 *
				 */
				TemplateApplicationView.prototype.validate = function() {
					this.clearErrorMessages();

//					this.nameInput.removeClass("error");
//
//					if (m_utils.isEmptyString(this.nameInput.val())) {
//						this.errorMessages.push("Data name must not be empty.");
//						this.nameInput.addClass("error");
//					}

					if (this.errorMessages.length > 0) {
						this.showErrorMessages();

						return false;
					}

					return true;
				};

				/**
				 *
				 */
				TemplateApplicationView.prototype.submitParameterDefinitionsChanges = function(
						parameterDefinitionsChanges) {
					// Context is regenerated on the server - hence, all data
					// need to be provided
//					parameterDefinitionsChanges = this.splitINOUTParameters(parameterDefinitionsChanges);
//					this.submitChanges({
//						contexts : {
//							"externalWebApp" : {
//								accessPoints : parameterDefinitionsChanges,
//								attributes : this.getContext().attributes
//							}
//						}
//					});
				};

				}

		});