/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Yogesh.Manware
 */

require.config({
	baseUrl: "../../",
	paths: {
		'jquery' : ['views-common/js/libs/jquery/jquery-1.7.2', '//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min'],
		'jquery.form': ['views-common/js/libs/jquery/plugins/jquery.form', 'https://raw.github.com/malsup/form/5d413a0169b673c9ee81d5f458b1c955ff1b8027/jquery.form'],
		'json' : ['views-common/js/libs/json/json2', '//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2'] 
	},
	shime: {
		'jquery.form' : [ 'jquery' ],
	}
});

require(["jquery", "jquery.form", "../plugins/views-common/js/m_fileUploadDialog"], function(){
	require("../plugins/views-common/js/m_fileUploadDialog").initialize();
});