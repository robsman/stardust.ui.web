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
 * @author Marc.Gille
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_dialog", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_angularContextUtils", 
				"bpm-modeler/js/m_modelerUtils" ],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_dialog, m_model, m_i18nUtils, m_angularContextUtils, m_modelerUtils) {

			return {
				createPropertiesPage : function(propertiesPanel, id, titel,
						imageUrl) {
					return new PropertiesPage(propertiesPanel, id, titel,
							imageUrl);
				},
				createPage : function(propertiesPanel, extCfg) {
		          return new PropertiesPage(propertiesPanel, extCfg.id, extCfg.title,
		                  extCfg.imageUrl);
		        }
			};

			function PropertiesPage(propertiesPanel, id, title, imageUrl) {
			  this.propertiesPanel = propertiesPanel;
				this.id = id;
				this.title = title;
				this.page = m_utils.jQuerySelect("#" + this.propertiesPanel.id + " #"
						+ this.id);

				if (imageUrl == null) {
					this.imageUrl = "plugins/bpm-modeler/images/icons/page_white.png";
				} else {
					this.imageUrl = imageUrl;
				}
				/**
				 * 
				 */
				PropertiesPage.prototype.mapInputId = function(inputId) {
					return m_utils.jQuerySelect("#" + this.propertiesPanel.id + " #"
							+ this.id + " #" + inputId);
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.getElement = function() {
					return this.propertiesPanel.element;
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.setElement = function() {
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.validate = function() {
					return true;
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.show = function() {
					m_dialog.makeVisible(this.page);
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.hide = function() {
					m_dialog.makeInvisible(this.page);
				};

				/**
				 * Returns the model element the Properties Pages are working
				 * on. This might be the Model Element a Symbol is representing
				 * (e.g. an Activity), a Data underneath a Data Symbol or the
				 * Process Definition itself.
				 */
				PropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.getModelElement();
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.assembleChangedObjectFromProperty = function(
						property, value) {
					return this.propertiesPanel
							.assembleChangedObjectFromProperty(property, value);
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.assembleChangedObjectFromAttribute = function(
						attribute, value) {
					return this.propertiesPanel
							.assembleChangedObjectFromAttribute(attribute,
									value);
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.registerInputForElementChangeSubmission = function(
						input, property) {
					input.change({
						page : this,
						input : input
					}, function(event) {
						var page = event.data.page;
						var input = event.data.input;

						if (!page.validate()) {
							return;
						}

						if (page.getElement()[property] != input.val()) {

							var change = {};

							change[property] = input.val();

							page.submitChanges(change);
						}
					});
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.registerInputForModelElementChangeSubmission = function(
						input, property) {
					input.change({
						"page" : this,
						"input" : input
					}, function(event) {
						var page = event.data.page;
						var input = event.data.input;

						if (!page.validate(input, property)) {
							return;
						}

						if (page.getModelElement()[property] != input.val()) {
							page.submitChanges(page
									.assembleChangedObjectFromProperty(
											property, input.val()));
						}
					});
				};
				
				/**
				 * 
				 */
				PropertiesPage.prototype.registerInputForModelElementAttributeChangeSubmission = function(
						input, attribute) {
					input
							.change(
									{
										"page" : this,
										"input" : input
									},
									function(event) {
										var page = event.data.page;
										var input = event.data.input;

										if (!page.validate()) {
											return;
										}

										if (page.getModelElement().attributes[attribute] != input
												.val()) {
											page
													.submitChanges(page
															.assembleChangedObjectFromAttribute(
																	attribute,
																	input.val()));
										}
									});
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.registerCheckboxInputForModelElementChangeSubmission = function(
						input, property) {
					input.click({
						"page" : this,
						"input" : input
					}, function(event) {
						var page = event.data.page;
						var input = event.data.input;

						if (!page.validate()) {
							return;
						}

						if (page.getModelElement()[property] != input
								.is(":checked")) {
							page.submitChanges(page
									.assembleChangedObjectFromProperty(
											property, input.is(":checked")));
						}
					});
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.registerCheckboxInputForModelElementAttributeChangeSubmission = function(
						input, attribute) {
					input
							.click(
									{
										"page" : this,
										"input" : input
									},
									function(event) {
										var page = event.data.page;
										var input = event.data.input;

										if (!page.validate()) {
											return;
										}

										if (page.getModelElement().attributes[attribute] != input
												.is(":checked")) {
											page
													.submitChanges(page
															.assembleChangedObjectFromAttribute(
																	attribute,
																	input
																			.is(":checked")));
										}
									});
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.getModel = function() {
					return this.propertiesPanel.getModel();
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.submitChanges = function(changes) {
					if (this.validate(changes)) {
						this.propertiesPanel.submitChanges(changes);
					}
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.validateCircularModelReference = function(
						input) {
					var otherModelId = m_model.stripModelId(input.val());
					if (this.getModel().id != otherModelId
							&& m_model.isModelReferencedIn(this.getModel().id,
									otherModelId)) {
						this.propertiesPanel.errorMessages
								.push(m_i18nUtils
										.getProperty("modeler.propertyPages.commonProperties.errorMessage.modelCircularReferenceNotAllowed"));
						input.addClass("error");
						input.focus();
						this.propertiesPanel.showErrorMessages();

						return false;
					}

					return true;
				};
				
				/*
				 * 
				 */
				PropertiesPage.prototype.safeApply = function(force) {
					var self = this;
					m_angularContextUtils.runInAngularContext(function($scope){
						if (force) {
							$scope.page = self;
						}
					}, m_utils.jQuerySelect("#" + self.id).get(0));
				};
				
				/**
				 * 
				 */
				PropertiesPage.prototype.broadcastElementChangedEvent = function() {
          var self = this;
          m_angularContextUtils.runInAngularContext(function($scope){
            $scope.$broadcast('PAGE_ELEMENT_CHANGED', self);
          }, m_utils.jQuerySelect("#" + self.id).get(0));
        };
        
        /**
         * 
         */
        PropertiesPage.prototype.findApplication = function(appFullId) {
          return m_model.findApplication(appFullId);
        };
        
        /**
         * 
         */
        PropertiesPage.prototype.getModels = function(appFullId) {
          return m_model.getModels();
        };
        
        /**
         * 
         */
        PropertiesPage.prototype.openApplicationView = function(application) {
          m_modelerUtils.openApplicationView(application)
        };
        
        /**
         * 
         */
        PropertiesPage.prototype.getMModel = function(application) {
          return m_model;
        };
			}
		});
