var CommonUtils = function() {

	var timerInfo = {};
	var cursorTimer = undefined;

	// Supports multiple times
	function startTimer(id, interval) {
		var timerId = window.setInterval(new TimerHandler(id).timerInvoked,
				interval);
		timerInfo[id] = timerId;
	}

	function stopTimer(id) {
		if (timerInfo[id]) {
			window.clearInterval(timerInfo[id]);
		}
	}

	/**
	 * change Cursor Style
	 */
	function changeMouseCursorStyle(style) {
		changeMouseCursorStyle_(style);
		// restore cursor type to default, in case of error
		// cursor remains in progress or wait state
		// in few cases for Firefox because of unknown reasons
		// After 10 seconds the cursor type would be reset
		if ("default" != style) {
			//clear previous timer
			if (cursorTimer) {
				clearTimeout(cursorTimer);
			}
			cursorTimer = setTimeout(function() {
				changeMouseCursorStyle_('default')
			}, 10000);
		}
	}

	/**
	 * change Cursor Style
	 */
	function changeMouseCursorStyle_(style) {
		var portalMainWnd = mainIppFrame["ippPortalMain"];
		var portalMainBody = portalMainWnd.document
				.getElementsByTagName("body")[0];

		portalMainBody.style.cursor = style;

		//change cursor style on all iframes
		doWithContentFrame(null, function(contentFrame) {
			if (contentFrame.contentDocument) {
				contentFrame.contentDocument.body.style.cursor = style;
			}
		});
	}
	return {
		startTimer : function(id, interval) {
			startTimer(id, interval);
		},

		stopTimer : function(id) {
			stopTimer(id);
		},

		changeMouseCursorStyle : function(style) {
			return changeMouseCursorStyle(style);
		}
	};
}();

function TimerHandler(id) {
	this.id = id;
	this.timerInvoked = timerInvoked;
	function timerInvoked() {
		var modelerLaunchPanel = document.getElementById("portalLaunchPanels");
		var timeField = modelerLaunchPanel.contentDocument
				.getElementById("viewFormLP:timerInvoked");
		if (timeField) {
			timeField.value = id + ":" + new Date().getTime();
			modelerLaunchPanel.contentWindow.iceSubmitPartial(
					modelerLaunchPanel.contentDocument
							.getElementById("viewFormLP"), timeField);
		}
	}
}
