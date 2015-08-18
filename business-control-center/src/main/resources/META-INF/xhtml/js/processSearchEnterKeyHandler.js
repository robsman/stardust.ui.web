var portalForm = document.getElementById('subView:portalContentForm');

function handleEnterKeyPress(e) {
	if (e.which === 13) {
		var searchButton = document.getElementById('subView:portalContentForm:submitSearch');
		if (portalForm && searchButton)
			iceSubmitPartial(portalForm, searchButton, e);
	}
}

//portalForm.addEventListener("keydown", handleEnterKeyPress); //function does not get invoked until the focus is on any of the input fields
document.addEventListener("keydown", handleEnterKeyPress);