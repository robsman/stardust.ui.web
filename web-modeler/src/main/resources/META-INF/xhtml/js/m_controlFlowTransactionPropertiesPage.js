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
					var page = new ControlFlowTransactionPropertiesPage(
							propertiesPanel);

					page.initialize();

					return page;
				}
			};

			/**
			 * 
			 */
			function ControlFlowTransactionPropertiesPage(propertiesPanel) {
				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, "transactionPropertiesPage",
						"Transactional Behavior",
						"../../images/icons/control-flow-transaction-properties-page.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						ControlFlowTransactionPropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				ControlFlowTransactionPropertiesPage.prototype.initialize = function() {
					this.forkOnTraversalInput = this
							.mapInputId("forkOnTraversalInput");
					
					this.registerCheckboxInputForModelElementChangeSubmission(this.forkOnTraversalInput, "forkOnTraversal");
				};

				/**
				 * 
				 */
				ControlFlowTransactionPropertiesPage.prototype.setElement = function() {
				};

				/**
				 * 
				 */
				ControlFlowTransactionPropertiesPage.prototype.validate = function() {
					this.propertiesPanel.clearErrorMessages();

					return true;
				};
			}
		});