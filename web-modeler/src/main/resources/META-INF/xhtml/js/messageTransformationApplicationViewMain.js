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
 * @author Marc Gille
 * @author Robert Sauer
 */
bpm.portal.modelerRequire.config();

require(["require",
		 "jquery",
		 "jquery-ui",
		 "jquery.form",
		 "jquery.jeditable",
		 "jquery.simplemodal",
		 "jquery.tablescroll",
		 "jquery.treeTable",
		 "jquery.url",
		 "ace",

		 "jslint",

		 "common-plugins",
		 "i18n",
         "bpm-modeler/js/m_messageTransformationApplicationView"],
		 function(require) {

		BridgeUtils.getTimeoutService()(function(){
			require("bpm-modeler/js/m_messageTransformationApplicationView").initialize(BridgeUtils.View.getActiveViewParams().param("fullId"));
		});
});

