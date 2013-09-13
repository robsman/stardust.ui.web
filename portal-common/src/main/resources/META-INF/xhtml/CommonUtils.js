var CommonUtils = function() {

	var timerInfo = {};

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

	return {
		startTimer : function(id, interval) {
			startTimer(id, interval);
		},

		stopTimer : function(id) {
			stopTimer(id);
		}
	};
}();

function TimerHandler(id) {
	this.id = id;
	this.timerInvoked = timerInvoked;
	function timerInvoked() {
		var modelerLaunchPanel=document.getElementById("modelerLaunchPanels");
		var timeField = modelerLaunchPanel.contentDocument.getElementById("viewFormLP:timerInvoked");
		if (timeField) {
			timeField.value = id + ":" + new Date().getTime();
			modelerLaunchPanel.contentWindow.iceSubmitPartial(modelerLaunchPanel.contentDocument.getElementById("viewFormLP"), timeField);
		}
	}
}
