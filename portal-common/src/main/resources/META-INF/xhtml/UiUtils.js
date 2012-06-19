/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
function softClick(type,linkName)
{
	var linkChars = linkName.length;
	var allLinks = document.getElementsByTagName(type);
	for (var i = 0; i < allLinks.length; i++)
	{
		var check=String_endsWith(allLinks[i].id,linkName);
		if(check)
		{			
			var form = formOf(allLinks[i]);
			var formId = form.id;			
			return iceSubmitPartial(form,allLinks[i]);	
			//iceSubmitPartial(form, allLinks[i],MouseEvent.CLICK);
		}	
	}
}

function String_endsWith(str,subStr) {
    return str.length >= subStr.length && (str.substring(str.length - subStr.length) == subStr);
}
