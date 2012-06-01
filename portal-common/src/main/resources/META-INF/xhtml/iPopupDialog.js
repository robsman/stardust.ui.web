var iPopupDialog = function() {
	var id;
	var isModal;
	var popupData;
	var popupDialogDiv;
	var popupDialogIFrame;

	function createDialog() {
		popupDialogDiv = document.createElement('div');
		popupDialogDiv.id = "jsPopupDiv";
		document.getElementsByTagName('body')[0].appendChild(popupDialogDiv);
		popupDialogDiv.innerHTML = '<iframe id="popupDialogIFrame" width="0px" height="0px"  style="z-index:10000; overflow : hidden;" src="iPopupDialog.html" frameborder="0"></iframe>';
		popupDialogIFrame = document.getElementById('popupDialogIFrame');
		popupDialogIFrame.style.visibility = "hidden";
	}
	
	function openDialog(data) {
		if (!popupDialogDiv) {
			createDialog(popupData);
		}
		popupData = data
		popupDialogIFrame.style.visibility = "visible";
		popupDialogIFrame.style.position = 'absolute';
		popupDialogIFrame.style.left = "0px";
		popupDialogIFrame.style.top = "0px";
		var p = InfinityBpm.Core.getIppWindow();
		popupDialogIFrame.style.width = p.document.body.scrollWidth + "px";
		popupDialogIFrame.style.height = p.document.body.scrollHeight + "px";
		centerPopup();
	}
	
	function closePopup() {
		var popupDialogIFrame = document.getElementById('popupDialogIFrame');
		popupDialogIFrame.style.visibility = "hidden";
		var popupElem = document.getElementById("jsPopupDiv");
		popupElem.parentNode.removeChild(popupElem);
		popupDialogDiv = undefined;
	}
	
	function setPopupContentCallback(callback) {
		callback({
			title : popupData.title,
			message : popupData.message,
			acceptButtonText : popupData.acceptButtonText,
			cancelButtonText : popupData.cancelButtonText,
			cancelFunction : function () {
				closePopup();
			},
			acceptFunction : function () {
				popupData.acceptCallback();
				closePopup();
			}
		});
		centerPopup();
	}
	
	function centerPopup() {
		var innerDoc = (popupDialogIFrame.contentDocument) ? popupDialogIFrame.contentDocument : popupDialogIFrame.contentWindow.document;
		InfinityBpm.Core.positionMessageDialog("iframePopupDialog", innerDoc);
	}
	
	return {
		openPopup : openDialog,
		setPopupContentCallback : setPopupContentCallback
	};
}();