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
 * @author Robert Sauer
 */
commonRequire.config({});

require(["require",
	 "jquery",
	 "json",
	 "raphael",

	 "jquery-ui",
	 "jquery.download",
	 "jquery.form",
	 "jquery.jeditable",
	 "jquery.simplemodal",
	 "jquery.tablescroll",
	 "jquery.treeTable",
	 "jquery.url",
	 "ace",
	 "jquery.jqprint",

	 "jslint",

	 "modeler-plugins",
	 "bpm-modeler/js/extensions_jquery",
	 "bpm-modeler/js/extensions_raphael",
	 "bpm-modeler/js/m_modelerViewLayoutManager",
	 "i18n"
	 ], function (require) {

		BridgeUtils.getTimeoutService()(function(){
				require("bpm-modeler/js/m_modelerViewLayoutManager").initialize(BridgeUtils.View.getActiveViewParams().param("fullId"));
		});
});

