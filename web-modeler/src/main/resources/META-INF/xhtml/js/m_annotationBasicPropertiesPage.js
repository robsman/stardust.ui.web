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
				"m_dialog", "m_propertiesPage"],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_dialog, m_propertiesPage) {
			return {
				create : function(propertiesPanel) {
					var page = new AnnotationBasicPropertiesPage(propertiesPanel);

					page.initialize();

					return page;
				}
			};
	
			/**
			 * 
			 */
			function AnnotationBasicPropertiesPage(propertiesPanel) {
				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, "basicPropertiesPage",
						"General Properties",
						"../../images/icons/basic-properties-page.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(AnnotationBasicPropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				AnnotationBasicPropertiesPage.prototype.initialize = function() {
					this.contentTextarea = this.mapInputId("contentTextarea");
					
					this.registerInputForElementChangeSubmission(this.contentTextarea, "content");
				};

				/**
				 * 
				 */
				AnnotationBasicPropertiesPage.prototype.setElement = function() {
					this.contentTextarea.val(this.getElement().content);
				};

				/**
				 * 
				 */
				AnnotationBasicPropertiesPage.prototype.validate = function() {
					this.propertiesPanel.clearErrorMessages();
					this.contentTextarea.removeClass("error");

					return true;
				};
			}
		});