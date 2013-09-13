/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/
var UiUtils = function() {
	function softClick(contentWindow, type, linkName) {
		try {
			var linkChars = linkName.length;
			var allLinks = contentWindow.document.getElementsByTagName(type);
			for ( var i = 0; i < allLinks.length; i++) {
				var check = String_endsWith(allLinks[i].id, linkName);
				if (check) {
					var form = allLinks[i].form;
					var formId = form.id;
					return contentWindow.iceSubmitPartial(form, allLinks[i]);
					// iceSubmitPartial(form, allLinks[i],MouseEvent.CLICK);
				}
			}
		} catch (e) {
			//alert(e);
		}
	}

	function String_endsWith(str, subStr) {
		try {
			return str.length >= subStr.length
					&& (str.substring(str.length - subStr.length) == subStr);
		} catch (e) {
			//alert(e);
		}
	}

	function softClickHtmlLink(id) {
		var aLink = document.getElementById(id);
		if (aLink) {
			aLink.click();
		}
	}

	return {
		softClick : function(contentWindow, type, linkName) {
			softClick(contentWindow, type, linkName);
		},
		String_endsWith : function(str, subStr) {
			return String_endsWith(str, subStr);
		},
		softClickHtmlLink : function(id) {
			softClickHtmlLink(id);
		}
	};
}();
