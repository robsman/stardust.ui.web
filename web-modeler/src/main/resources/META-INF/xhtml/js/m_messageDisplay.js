/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants" ],
		function(m_utils, m_constants) {

			var INFO_MESSAGE = 0;
			var ERROR_MESSAGE = 1;
			var lastSaveDate = null;

			var messageDisplay = jQuery("#messageDisplay");
			var lastSaveDateDisplay = jQuery("#lastSaveDateDisplay");

			lastSaveDateDisplay.append("Not saved yet");

			var modificationStatusDisplay = jQuery("#modificationStatusDisplay");

			var messages = [];

			return {
				markSaved : markSaved,
				markModified : markModified,
				showMessage : showMessage,
				showErrorMessage : showErrorMessage,
				clear : clear
			};

			/**
			 * 
			 */
			function markModified() {
				modificationStatusDisplay.empty();
				modificationStatusDisplay.append("(Modified)");
				lastSaveDateDisplay.empty();
				lastSaveDateDisplay.append(prettyDateTime(lastSaveDate));
			}

			/**
			 * 
			 */
			function markSaved() {
				lastSaveDate = new Date();

				modificationStatusDisplay.empty();
				lastSaveDateDisplay.empty();
				lastSaveDateDisplay.append("Just Now");
			}

			/**
			 * 
			 */
			function showMessage(message) {
				clearDisplay();
				messages.push(new Message(INFO_MESSAGE, message));
				messageDisplay.append(message);

			}

			/**
			 * 
			 */
			function showErrorMessage(message) {
				clearDisplay();
				messages.push(new Message(ERROR_MESSAGE, message));
				messageDisplay.attr("class", "errorMessage");
				messageDisplay.append(message);
			}

			/**
			 * 
			 */
			function clear() {
				clearDisplay();
				messageDisplay.removeAttr("class");
				messageDisplay.empty();
				messages.pop();

				if (messages.length > 0) {
					message = messages[messages.length - 1];

					if (message.type == ERROR_MESSAGE) {
						messageDisplay.attr("class", "errorMessage");
						messageDisplay.append(message.content);

					} else {
						messageDisplay.append(message.content);
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

			/**
			 * 
			 */
			function prettyDateTime(date) {
				if (date == null) {
					return "-";
				}
				
				var time_formats = [ [ 60, 'Less than a Minute' ], [ 90, '1 Minute' ], // 60*1.5
				[ 3600, 'Minutes', 60 ], // 60*60, 60
				[ 5400, '1 Hour' ], // 60*60*1.5
				[ 86400, 'Hours', 3600 ], // 60*60*24, 60*60
				[ 129600, '1 Day' ], // 60*60*24*1.5
				[ 604800, 'Days', 86400 ], // 60*60*24*7, 60*60*24
				[ 907200, '1 Week' ], // 60*60*24*7*1.5
				[ 2628000, 'Weeks', 604800 ], // 60*60*24*(365/12), 60*60*24*7
				[ 3942000, '1 Month' ], // 60*60*24*(365/12)*1.5
				[ 31536000, 'Months', 2628000 ], // 60*60*24*365,
				// 60*60*24*(365/12)
				[ 47304000, '1 Year' ], // 60*60*24*365*1.5
				[ 3153600000, 'Years', 31536000 ], // 60*60*24*365*100,
				// 60*60*24*365
				[ 4730400000, '1 Century' ], // 60*60*24*365*100*1.5
				];

				var seconds = (new Date().getTime() - date.getTime()) / 1000;
				var suffix = " ago";

				if (seconds < 0) {
					seconds = Math.abs(seconds);
					suffix = " from now";
				}

				var n = 0;
				var format;

				while (format = time_formats[n]) {
					if (seconds < format[0]) {
						if (format.length == 2) {
							return format[1] + suffix;
						} else {
							return Math.round(seconds / format[2]) + " "
									+ format[1] + suffix;
						}
					}
					
					++n;
				}

				if (seconds > 4730400000)
					return Math.round(seconds / 4730400000) + " Centuries"
							+ token;

				return "(Unknown)";
			}
		});