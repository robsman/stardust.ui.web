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
commonRequire.config({});

require(["require",
         "jquery",
         "jquery-ui",
         "jquery.download",
         "jquery.form",
         "jquery.jeditable",
         "jquery.simplemodal",
         "jquery.tablescroll",
         "jquery.treeTable",
         "jquery.url",
         "bpm-modeler/js/m_utils",
         "i18n",
		 "common-plugins",
		 "bpm-modeler/js/m_communicationController",
		 "bpm-modeler/js/m_urlUtils",
		 "bpm-modeler/js/m_constants",
		 "bpm-modeler/js/m_command",
		 "bpm-modeler/js/m_commandsController",
		 "bpm-modeler/js/m_view",
		 "bpm-modeler/js/m_modelView"], function(require) {

		BridgeUtils.getTimeoutService()(function(){
			require("bpm-modeler/js/m_modelView").initialize(BridgeUtils.View.getActiveViewParams().param("modelId"));
		});
});

