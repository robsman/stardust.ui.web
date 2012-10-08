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
		[ "m_utils", "m_constants", "m_session", "m_commandsController",
				"m_command", "m_propertiesPage", "m_commentsPanel" ],
		function(m_utils, m_constants, m_session, m_commandsController,
				m_command, m_propertiesPage, m_commentsPanel) {
			return {
				create : function(propertiesPanel, id) {
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
				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, id, "Comments",
						"../../images/icons/comments-properties-page.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(CommentsPropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				CommentsPropertiesPage.prototype.initialize = function() {
					this.commentsPanel = m_commentsPanel.create({scope: this.id, submitHandler: this});
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
					this.submitChanges(this.assembleChangedObjectFromProperty("comments", comments));
				};
			}
		});