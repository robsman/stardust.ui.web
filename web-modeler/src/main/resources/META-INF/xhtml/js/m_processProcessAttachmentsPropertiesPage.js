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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_command",
				"bpm-modeler/js/m_propertiesPage","bpm-modeler/js/m_i18nUtils"],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_propertiesPage,m_i18nUtils) {
			return {
				create : function(propertiesPanel) {
					i18nProcessStaticLabels();
					var page = new ProcessProcessAttachmentsPropertiesPage(
							propertiesPanel);

					page.initialize();

					return page;
				}
			};


			function i18nProcessStaticLabels() {
				// headingdata
				jQuery("#propertiesText")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.processAttachments.heading"));
				// jQuery("#commentsHeading").html(m_i18nUtils.getProperty("modeler.element.properties.commonProperties.comments"));
				$("label[for='supportsAttachmentsInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.processAttachmentText.checkboxSupport"));
				$("label[for='uniquePerRootProcessInstanceInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.processAttachmentText.checkboxInstanceSupport"));
				//jQuery("title").html("+testing");

			}
			/**
			 *
			 */
			function ProcessProcessAttachmentsPropertiesPage(propertiesPanel) {

                var processAttachText = m_i18nUtils.getProperty("modeler.processDefinition.propertyPages.processAttachments.heading");
				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, "processAttachmentsPropertiesPage",
						processAttachText,
						"plugins/bpm-modeler/images/icons/process-attachment.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						ProcessProcessAttachmentsPropertiesPage.prototype,
						propertiesPage);

				/**
				 *
				 */
				ProcessProcessAttachmentsPropertiesPage.prototype.initialize = function() {
					this.supportsAttachmentsInput = this
							.mapInputId("supportsAttachmentsInput");
					this.uniquePerRootProcessInstanceInput = this
							.mapInputId("uniquePerRootProcessInstanceInput");

					this.supportsAttachmentsInput
							.change(
									{
										page : this
									},
									function(event) {
										var page = event.data.page;

										if (page.supportsAttachmentsInput
												.is(":checked")) {
											if (!page.hasProcessAttachmentsDataPathes()) {
												page.addProcessAttachmentsDataPathes();
											}
										} else {
											if (page.hasProcessAttachmentsDataPathes()) {
												page.removeProcessAttachmentsDataPathes();
											}
										}
									});

					this
							.registerCheckboxInputForModelElementAttributeChangeSubmission(
									this.uniquePerRootProcessInstanceInput,
									"carnot:engine:dms:byReference");
				};

				/**
				 *
				 */
				ProcessProcessAttachmentsPropertiesPage.prototype.hasProcessAttachmentsDataPathes = function() {
					for ( var n = 0; n < this.propertiesPanel.element.dataPathes.length; ++n) {
						if (this.propertiesPanel.element.dataPathes[n].id == "PROCESS_ATTACHMENTS") {
							return true;
						}
					}

					return false;
				};

				/**
				 *
				 */
				ProcessProcessAttachmentsPropertiesPage.prototype.addProcessAttachmentsDataPathes = function() {
					this.propertiesPanel.element.dataPathes.push({
						id : "PROCESS_ATTACHMENTS",
						name : "PROCESS_ATTACHMENTS",
						direction : "IN",
						descriptor : false,
						keyDescriptor : false,
						dataFullId : this
						.propertiesPanel.diagram.modelId
						+ ":"
						+ "PROCESS_ATTACHMENTS",
						dataPath : null
					});
					this.propertiesPanel.element.dataPathes.push({
						id : "PROCESS_ATTACHMENTS",
						name : "PROCESS_ATTACHMENTS",
						direction : "OUT",
						descriptor : false,
						keyDescriptor : false,
						dataFullId : this
						.propertiesPanel.diagram.modelId
						+ ":"
						+ "PROCESS_ATTACHMENTS",
						dataPath : null
					});
					this.submitChanges({
						dataPathes : this.propertiesPanel.element.dataPathes
					});
				};

				/**
				 *
				 */
				ProcessProcessAttachmentsPropertiesPage.prototype.removeProcessAttachmentsDataPathes = function() {
					var changedPathes = [];

					for ( var n = 0; n < this.propertiesPanel.element.dataPathes.length; ++n) {
						if (this.propertiesPanel.element.dataPathes[n].id != "PROCESS_ATTACHMENTS") {
							changedPathes
									.push(this.getModelElement().dataPathes[n]);
						}
					}

					this.propertiesPanel.element.dataPathes = changedPathes;

					this.submitChanges({
						dataPathes : this.propertiesPanel.element.dataPathes
					});
				};

				/**
				 *
				 */
				ProcessProcessAttachmentsPropertiesPage.prototype.setElement = function() {
					this.supportsAttachmentsInput.attr("checked", this
							.hasProcessAttachmentsDataPathes());
					this.uniquePerRootProcessInstanceInput
							.attr(
									"checked",
									this.getModelElement().attributes["carnot:engine:dms:byReference"] == true);
					if (this.supportsAttachmentsInput.is(":checked")) {
						this.uniquePerRootProcessInstanceInput.removeAttr("disabled");
					} else {
						this.uniquePerRootProcessInstanceInput.attr("disabled", true);
					}
				};

				/**
				 *
				 */
				ProcessProcessAttachmentsPropertiesPage.prototype.validate = function() {
					this.propertiesPanel.clearErrorMessages();

					return true;
				};
			}
		});