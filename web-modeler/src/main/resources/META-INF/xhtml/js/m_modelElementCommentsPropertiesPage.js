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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_session", "bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_propertiesPage", "bpm-modeler/js/m_commentsPanel", "bpm-modeler/js/m_i18nUtils"  ],
		function(m_utils, m_constants, m_session, m_commandsController,
				m_command, m_propertiesPage, m_commentsPanel, m_i18nUtils) {
			return {
				create : function(propertiesPanel, id) {
					// I18N static labels on the page
					i18nStaticLabels();
					var page = new CommentsPropertiesPage(propertiesPanel,
							id);

					page.initialize();

					return page;
				}
			};

			/**
			 * 
			 */
			function CommentsPropertiesPage(propertiesPanel, id) {
				var commentsText = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.comments");
				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, id, commentsText,
						"../../images/icons/comments.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(CommentsPropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				CommentsPropertiesPage.prototype.initialize = function() {
					this.commentsPanel = m_commentsPanel.create({scope: "#" + this.propertiesPanel.id + " #" + this.id, submitHandler: this});
				};

				/**
				 * 
				 */
				CommentsPropertiesPage.prototype.setElement = function() {
					this.commentsPanel.setComments(this.propertiesPanel.getModelElement().comments);
				};

				/**
				 * 
				 */
				CommentsPropertiesPage.prototype.validate = function() {
					this.propertiesPanel.clearErrorMessages();

					return true;
				};

				/**
				 * 
				 */
				CommentsPropertiesPage.prototype.submitCommentsChanges = function(comments) {
					m_utils.debug("Submit Comment");
					m_utils.debug(this.assembleChangedObjectFromProperty("comments", comments));
					
					this.submitChanges(this.assembleChangedObjectFromProperty("comments", comments));
				};
			}
			/**
			 * 
			 */
			function i18nStaticLabels() {
				jQuery("#commentsHeading").html(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.comments"));		
				
				jQuery("#deleteButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.delete"));
				jQuery("#submitButton")
						.attr(
								"value",
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.submit"));

			}
			;
		});