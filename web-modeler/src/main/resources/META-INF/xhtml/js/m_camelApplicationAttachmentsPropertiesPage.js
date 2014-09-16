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
 * @author Subodh.Godbole
 */
define(["bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_session",
		"bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_command", "bpm-modeler/js/m_propertiesPage",
		"bpm-modeler/js/m_i18nUtils"],
		function(m_utils, m_constants, m_session, m_commandsController, m_command, m_propertiesPage, m_i18nUtils) {
			return {
				create : function(view, id) {
					var page = new CamelApplicationAttachmentsPropertiesPage(view, id);

					return page;
				}
			};

			/**
			 * 
			 */
			function CamelApplicationAttachmentsPropertiesPage(view, id) {
				var title = m_i18nUtils.getProperty("modeler.model.applicationOverlay.email.attachments.title");
				var propertiesPage = m_propertiesPage.createPropertiesPage(view, id, title, 
						"plugins/bpm-modeler/images/icons/data-folder.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(CamelApplicationAttachmentsPropertiesPage.prototype, propertiesPage);

				/*
				 * Expose current property Page on view with same name! Later this will be moved to framework!
				 */
				view.CamelApplicationAttachmentsPropertiesPage = this;

				/*
				 * Do initialisation here in constructor itself!
				 * Whatever data added to "this" in constructor will be available on $scope
				 */
				this.view = view;

				var templateConfigurationsJson = view.application.attributes["stardust:emailOverlay::templateConfigurations"];
				if (!templateConfigurationsJson) {
					templateConfigurationsJson = "[]";
				}

				this.templateConfigurations = JSON.parse(templateConfigurationsJson);

				this.sourceOptions = [
				    {value: "repository", title: "Document Repository"},
				    {value: "classpath", title: "Classpath"}
				];

				this.formatOptions = [
				    {value: "plain", title: "Plain"},
				  	{value: "pdf", title: "PDF"}
				];

				this.i18nValues = initI18nLabels();

				/**
				 * 
				 */
				CamelApplicationAttachmentsPropertiesPage.prototype.addConfiguration = function() {
					this.templateConfigurations.push({
						"tName" : "New" + (this.templateConfigurations.length + 1),
						"tPath" : "New" + (this.templateConfigurations.length + 1),
						"tSource" : "repository",
						"tFormat" : "plain"
					});

					this.submitSingleAttributeChange("stardust:emailOverlay::templateConfigurations",
							angular.toJson(this.templateConfigurations));
				};

				/**
				 * 
				 */
				CamelApplicationAttachmentsPropertiesPage.prototype.deleteConfiguration = function(index) {
					this.templateConfigurations.splice(index, 1);

					this.submitSingleAttributeChange("stardust:emailOverlay::templateConfigurations",
							angular.toJson(this.templateConfigurations));
				};

				/**
				 * 
				 */
				CamelApplicationAttachmentsPropertiesPage.prototype.submitSingleAttributeChange = function(
						attribute, value) {

					if (this.getModelElement().attributes[attribute] != value) {
						var modelElement = {
							attributes : {}
						};
						modelElement.attributes[attribute] = value;
						this.view.submitChanges(modelElement, true);
					}
				};

				/**
				 * 
				 */
				CamelApplicationAttachmentsPropertiesPage.prototype.i18nLabels = function(key) {
					return this.i18nValues[key];
				};

				/**
				 * 
				 */
				CamelApplicationAttachmentsPropertiesPage.prototype.setElement = function() {
				};
			}

			/**
			 * 
			 */
			function initI18nLabels() {
				var labels = {};
				labels['templateConfigurations.title'] = 
					m_i18nUtils.getProperty("modeler.model.applicationOverlay.email.attachments.templateConfigurations.title");
				labels['templateConfigurations.add.title'] = 
					m_i18nUtils.getProperty("modeler.model.applicationOverlay.email.attachments.templateConfigurations.add.title");
			
				// TODO: Add more labels

				return labels;
			}
		});