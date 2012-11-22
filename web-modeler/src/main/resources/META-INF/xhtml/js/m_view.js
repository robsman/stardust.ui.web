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
				this.errorMessagesList = jQuery("#errorMessagesList");
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