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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_command", "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_dialog" ],
		function(m_utils, m_constants, m_command, m_commandsController, m_dialog) {
			return {
				create : function(id) {
					var view = new View();

					return view;
				}
			};

			/**
			 *
			 */
			function View() {
				this.id = null;
				this.errorMessagesList = m_utils.jQuerySelect("#errorMessagesList");
				this.errorMessages = [];

				View.prototype.clearErrorMessages = function() {
					m_dialog.makeInvisible(this.errorMessagesList);
					this.errorMessages = [];
					this.errorMessagesList.empty();
				};

				/**
				 *
				 */
				View.prototype.showErrorMessages = function() {
					if (this.errorMessages.length != 0) {
						m_dialog.makeVisible(this.errorMessagesList);

						for ( var n in this.errorMessages) {
							this.errorMessagesList.append("<li>"
									+ this.errorMessages[n] + "</li>");
						}
					}
				};
			}
		});