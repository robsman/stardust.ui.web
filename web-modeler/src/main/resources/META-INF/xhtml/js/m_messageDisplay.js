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
		[ "jquery", "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants","bpm-modeler/js/m_i18nUtils"  ],
		function(jquery, m_utils, m_constants, m_i18nUtils){

			var INFO_MESSAGE = 0;
			var ERROR_MESSAGE = 1;
			var lastSaveDate;

			var messages = [];

			return {
				markSaved : markSaved,
				markModified : markModified,
				updateLastSavedLabel : updateLastSavedLabel,
				showMessage : showMessage,
				showErrorMessage : showErrorMessage,
				clear : clear,
				clearErrorMessages : clearErrorMessages,
				clearAllMessages : clearAllMessages
			};

			/**
			 *
			 */
			function markModified() {
				if (lastSaveDate) {
					updateLastSavedLabel(m_utils.prettyDateTime(lastSaveDate));
				} else {
					updateLastSavedLabel(m_i18nUtils.getProperty("modeler.outline.unSavedMessage.title"));
				}
			}

			/**
			 *
			 */
			function updateLastSavedLabel(label) {
				if (m_utils.jQuerySelect("#lastSaveDateDisplay"))
					m_utils.jQuerySelect("#lastSaveDateDisplay").html(label);
			}

			/**
			 *
			 */
			function markSaved() {
				lastSaveDate = new Date();
				updateLastSavedLabel(m_i18nUtils.getProperty("modeler.element.properties.sessionProperties.justNow"));
			}

			/**
			 *
			 */
			function showMessage(message) {
				clearDisplay();
				messages.push(new Message(INFO_MESSAGE, message));
				m_utils.jQuerySelect("#messagePanel").attr("class", "messagePanelHighlight");
				m_utils.jQuerySelect("#messageIcon").parent().attr("class", "infoSeverityIssueItem");
				m_utils.jQuerySelect("#messageDisplay").append(message);

			}

			/**
			 *
			 */
			function showErrorMessage(message) {
				clearDisplay();
				messages.push(new Message(ERROR_MESSAGE, message));
				m_utils.jQuerySelect("#messagePanel").attr("class", "messagePanelHighlight");
				m_utils.jQuerySelect("#messageIcon").parent().attr("class", "errorSeverityIssueItem");
				m_utils.jQuerySelect("#messageDisplay").append(message);
			}

			/**
			 * clearing error messages
			 */
			function clearErrorMessages() {
				clearDisplay();
				m_utils.jQuerySelect("#messagePanel").removeAttr("class");
				m_utils.jQuerySelect("#messageIcon").parent().removeAttr("class");
				m_utils.jQuerySelect("#messageDisplay").removeAttr("class");
				m_utils.jQuerySelect("#messageDisplay").empty();

				for(var n in messages){
					message = messages[n];
					// remove all error messages, before connection rules
					// validation
					if (message && message.type == ERROR_MESSAGE) {
						m_utils.removeItemFromArray(messages,message);
					}
				}

				if (messages.length > 0) {
					message = messages[messages.length - 1];
					m_utils.jQuerySelect("#messagePanel").attr("class", "messagePanelHighlight");
					m_utils.jQuerySelect("#messageIcon").parent().attr("class", "infoSeverityIssueItem");
					m_utils.jQuerySelect("#messageDisplay").append(message.content);
				}

			}

			/**
			 *
			 */
			function clear() {
				clearDisplay();
				m_utils.jQuerySelect("#messagePanel").removeAttr("class");
				m_utils.jQuerySelect("#messageIcon").parent().removeAttr("class");
				m_utils.jQuerySelect("#messageDisplay").removeAttr("class");
				m_utils.jQuerySelect("#messageDisplay").empty();
				messages.pop();

				if (messages.length > 0) {
					message = messages[messages.length - 1];

					if (message.type == ERROR_MESSAGE) {
						m_utils.jQuerySelect("#messageDisplay").attr("class", "errorMessage");
						m_utils.jQuerySelect("#messageDisplay").append(message.content);

					} else {
						//TODO - Do we need this?
						//messageDisplay.append(message.content);
					}
				}
			}

			/**
			 *
			 */
			function clearAllMessages() {
				clearDisplay();
				m_utils.jQuerySelect("#messagePanel").removeAttr("class");
				m_utils.jQuerySelect("#messageIcon").parent().removeAttr("class");
				m_utils.jQuerySelect("#messageDisplay").removeAttr("class");
				m_utils.jQuerySelect("#messageDisplay").empty();
				messages.length = 0;
			}

			function collapse() {

			}

			/**
			 *
			 */
			function clearDisplay() {
				m_utils.jQuerySelect("#messageDisplay").removeAttr("class");
				m_utils.jQuerySelect("#messageDisplay").empty();
			}

			function Message(type, content) {
				this.type = type;
				this.content = content;
			}
		});