/**
 * @author Marc.Gille
 */
define(
		[ "jquery", "m_utils", "m_constants" ],
		function(jQuery, m_utils, m_constants) {

			var INFO_MESSAGE = 0;
			var ERROR_MESSAGE = 1;
			var lastSaveDate;

			var messagePanel = jQuery("#messagePanel");
			var messageDisplay = jQuery("#messageDisplay");
			var messageIcon = jQuery("#messageIcon");
			var lastSaveDateDisplay = jQuery("#lastSaveDateDisplay");
			updateLastSavedLabel("Not saved yet");

			var messages = [];

			return {
				markSaved : markSaved,
				markModified : markModified,
				showMessage : showMessage,
				showErrorMessage : showErrorMessage,
				clear : clear,
				clearErrorMessages : clearErrorMessages
			};

			/**
			 *
			 */
			function markModified() {
				if (lastSaveDate) {
					updateLastSavedLabel(m_utils.prettyDateTime(lastSaveDate));
				} else {
					updateLastSavedLabel("Not saved yet");
				}
			}

			/**
			 *
			 */
			function updateLastSavedLabel(label) {
				$("#lastSaveDateDisplay").html(label);
			}

			/**
			 *
			 */
			function markSaved() {
				lastSaveDate = new Date();
				updateLastSavedLabel("Just Now");
			}

			/**
			 *
			 */
			function showMessage(message) {
				clearDisplay();
				messages.push(new Message(INFO_MESSAGE, message));
				messagePanel.attr("class", "messagePanelHighlight");
				messageIcon.parent().attr("class", "infoSeverityIssueItem");
				messageDisplay.append(message);

			}

			/**
			 *
			 */
			function showErrorMessage(message) {
				clearDisplay();
				messages.push(new Message(ERROR_MESSAGE, message));
				messagePanel.attr("class", "messagePanelHighlight");
				messageIcon.parent().attr("class", "errorSeverityIssueItem");
				messageDisplay.append(message);
			}

			/**
			 * clearing error messages
			 */
			function clearErrorMessages() {
				clearDisplay();
				messagePanel.removeAttr("class");
				messageIcon.parent().removeAttr("class");
				messageDisplay.removeAttr("class");
				messageDisplay.empty();

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
					messagePanel.attr("class", "messagePanelHighlight");
					messageIcon.parent().attr("class", "infoSeverityIssueItem");
					messageDisplay.append(message.content);
				}

			}

			/**
			 *
			 */
			function clear() {
				clearDisplay();
				messagePanel.removeAttr("class");
				messageIcon.parent().removeAttr("class");
				messageDisplay.removeAttr("class");
				messageDisplay.empty();
				messages.pop();

				if (messages.length > 0) {
					message = messages[messages.length - 1];

					if (message.type == ERROR_MESSAGE) {
						messageDisplay.attr("class", "errorMessage");
						messageDisplay.append(message.content);

					} else {
						//TODO - Do we need this?
						//messageDisplay.append(message.content);
					}
				}
			}

			/**
			 *
			 */
			function clearDisplay() {
				messageDisplay.removeAttr("class");
				messageDisplay.empty();
			}

			function Message(type, content) {
				this.type = type;
				this.content = content;
			}
		});