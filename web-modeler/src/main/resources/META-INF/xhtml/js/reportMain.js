/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

'use strict';

/**
 * @author Robert Sauer
 */
bpm.portal.modelerRequire.config();

define('reportMain',[
		 "reportApp",
		 "jquery",
		 "common-plugins",
		 "reportControllers"
], function(reportApp, jQuery) {
	m_utils.jQuerySelect(document).ready(function() {
		reportApp.init();
	});
});

