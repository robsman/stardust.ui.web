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
bpm.portal.viewsCommonRequire.config({
	baseUrl : "../../"
});

require(["jquery", "jquery.form", "../plugins/views-common/js/m_fileUploadDialog"], function(){
	require("../plugins/views-common/js/m_fileUploadDialog").initialize();
});