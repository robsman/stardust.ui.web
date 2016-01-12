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
 */
bpm.portal.modelerRequire.config();

require(["require",
         "json",
         "jquery",
         "jquery-ui",
         "jquery.tablescroll",
         "jquery.treeTable",
		 "jquery.url",
		 "ace",
		 "ckeditor",
		 "common-plugins",
		 "i18n",
		 "bpm-modeler/js/m_decoratorApplicationView"],
		 function(require) {

	BridgeUtils.getTimeoutService()(function(){
		require("bpm-modeler/js/m_decoratorApplicationView").initialize(BridgeUtils.View.getActiveViewParams().param("fullId"));
	});
});

