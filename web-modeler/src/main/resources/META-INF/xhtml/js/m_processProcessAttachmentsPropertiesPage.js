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
		[ "m_utils", "m_constants", "m_commandsController", "m_command",
				"m_propertiesPage" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_propertiesPage) {
			return {
				create : function(propertiesPanel) {
					var page = new ProcessProcessAttachmentsPropertiesPage(
							propertiesPanel);

					page.initialize();

					return page;
				}
			};

			/**
			 * 
			 */
			function ProcessProcessAttachmentsPropertiesPage(propertiesPanel) {
				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, "processAttachmentsPropertiesPage",
						"Process Attachments",
						"../../images/icons/generic-properties-page.png");

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