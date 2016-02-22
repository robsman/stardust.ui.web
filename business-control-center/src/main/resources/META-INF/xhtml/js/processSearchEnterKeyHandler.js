/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
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