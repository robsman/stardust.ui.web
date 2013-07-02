/*
 *
 */

if (!window["InfinityBpm"]) {
	InfinityBpm = new function() {
	}
} // !InfinityBpm

if (!window["InfinityBpm.ProcessPortal"]) {
	InfinityBpm.ProcessPortal = new function() {

		function handleIppAiClosePanelCommandConfirmation(commandId) {

			var iframeWindow;
			var iframeId = BridgeUtils.View.getIframeIdForActiveView();
			if(iframeId) {
				var iframe = document.getElementById(iframeId);
				if (iframe) {
					iframeWindow = iframe.contentWindow;
				}
			}

			if (!iframeWindow) {
				alert("Something is wrong with ProcessPortal.js");
				return;
			}

			var ippPortalDom = iframeWindow.document;
			var divRemoteControl = ippPortalDom.getElementById('ippProcessPortalActivityPanelRemoteControl');
			if ( !divRemoteControl) {
				alert('Could not find the IPP Process Portal Remote Control infrastructure.');
				return
			}

			var fldCommandId = divRemoteControl.getElementsByTagName('input')[0];
			if (fldCommandId)
			{
				fldCommandId.value = commandId;

				try
			    {
					if ('function' === typeof iframeWindow.submitForm) {
						// Trinidad
						iframeWindow.submitForm(fldCommandId.form, 1, {source: fldCommandId.id});
					} else if ('function' === typeof iframeWindow.iceSubmitPartial) {
						// ICEfaces
						iframeWindow.iceSubmitPartial(fldCommandId.form, fldCommandId, null);
					}

					return;
			    }
			    catch (x) {
				alert('Failed submitting form: ' + x);
			    }
			  }
			  else {
				  alert('Could not find the command field.');
			  }
		}

		return {
			completeActivity: function() {
				try {
					handleIppAiClosePanelCommandConfirmation('complete');
				} catch (e) {
					alert('Failed completing activity: ' + e.message);
				}
			},

			qaPassActivity: function() {
				try {
					handleIppAiClosePanelCommandConfirmation('qaPass');
				} catch (e) {
					alert('Exception occurred while Quality Assurance Pass activity: ' + e.message);
			  }
			},

			qaFailActivity: function() {
				try {
					handleIppAiClosePanelCommandConfirmation('qaFail');
				} catch (e) {
					alert('Exception occurred while Quality Assurance Fail activity: ' + e.message);
				}
			},

			suspendActivity: function(saveOutParams) {
				try {
					handleIppAiClosePanelCommandConfirmation(saveOutParams ? 'suspendAndSave' : 'suspend');
				} catch (e) {
					alert('Failed suspending activity: ' + e.message);
				}
			},

			abortActivity: function() {
				try {
					handleIppAiClosePanelCommandConfirmation('abort');
				} catch (e) {
					alert('Failed aborting activity: ' + e.message);
				}
			},

			activityClosePanelCommand: function(commandId) {
				try {
					handleIppAiClosePanelCommandConfirmation(commandId);
				} catch (e) {
					alert('Failed processing activity command: ' + e.message);
				}
			}
		}
	};
}