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
commonRequire.config({baseUrl: "../../../"});

require(["require",
         "jquery",
         "jquery.tablescroll",
         "jquery.treeTable",
		 "jquery.url",
		 "common-plugins",
		 "i18n",
		 "bpm-modeler/js/m_importTypeDeclarationsWizard"
], function(require) {
	require("bpm-modeler/js/m_importTypeDeclarationsWizard").initialize();
});

