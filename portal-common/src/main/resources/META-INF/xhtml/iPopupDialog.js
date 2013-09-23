/*
 * Supports creation of modal pop-up dialogs from child iframes.
 *
 * */
var iPopupDialog = function() {
	var popupData;
	var popupDialogDiv;
	var popupDialogIFrame;

	function createDialog() {
		popupDialogDiv = document.createElement('div');
		popupDialogDiv.id = "jsPopupDiv";
		document.getElementsByTagName('body')[0].appendChild(popupDialogDiv);
		popupDialogDiv.innerHTML = '<iframe id="popupDialogIFrame" width="0px" height="0px"  style="z-index:10000; overflow : hidden;" src="plugins/common/iPopupDialog.html" frameborder="0"></iframe>';
		popupDialogIFrame = document.getElementById('popupDialogIFrame');
		popupDialogIFrame.style.visibility = "hidden";
	}

	function openDialog(data) {
		if (!popupDialogDiv) {
			createDialog();
		}

		popupData = data;

		popupDialogIFrame.style.visibility = "visible";
		popupDialogIFrame.style.position = 'absolute';
		popupDialogIFrame.style.left = "0px";
		popupDialogIFrame.style.top = "0px";
		popupDialogIFrame.style.width = document.body.scrollWidth + "px";
		popupDialogIFrame.style.height = document.body.scrollHeight + "px";
	}

	function closePopup() {
		var popupDialogIFrame = document.getElementById('popupDialogIFrame');
		popupDialogIFrame.style.visibility = "hidden";
		var popupElem = document.getElementById("jsPopupDiv");
		popupElem.parentNode.removeChild(popupElem);
		popupDialogDiv = undefined;
	}

	function centerPopup() {
		// TODO - to be replaced as InfinityBpm.Core is no longer used after HTML5 Move
//		var innerDoc = (popupDialogIFrame.contentDocument) ? popupDialogIFrame.contentDocument : popupDialogIFrame.contentWindow.document;
//		InfinityBpm.Core.positionMessageDialog("iframePopupDialog", innerDoc);
	}

	return {
		openPopup : openDialog,
		closePopup : closePopup,
		centerPopup : centerPopup,
		getPopupData : function () {
			return popupData;
		}
	};
}();