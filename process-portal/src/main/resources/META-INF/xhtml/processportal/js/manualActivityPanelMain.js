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
 * @author Subodh.Godbole
 */
bpm.portal.viewsCommonRequire.config({
	baseUrl : "../../plugins/"
});

require(["require", "jquery", "jquery-ui", "angularjs", "jquery-base64", "portalSupport", "processportal/js/m_manualActivityPanel",
         "bpm.portal.GenericAngularApp", "bpm.portal.GenericController"], function(){
	require("processportal/js/m_manualActivityPanel").initialize();
});