/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ 'bpm-modeler/js/DefaultOutlineHandler', "bpm-modeler/js/m_i18nUtils" ],
		function(DefaultOutlineHandler, m_i18nUtils) {
	return {
		menuOption : [ {
			id : "openCreateApplicationWrapperProcessWizardMenuOption",
			nodeType : "application",
			label :  "Create Service Wrapper Process", //m_i18nUtils.getProperty("modeler.outline.openCreateApplicationWrapperProcessWizard"),
			provider : DefaultOutlineHandler,
			handlerMethod: "openCreateApplicationWrapperProcessWizard",
			visibility : "always"
		},
		{
			id : "openProcessInterfaceTestWrapperProcessWizardMenuOption",
			nodeType : "process",
			label :  "Create Process Interface Test Wrapper Process", //m_i18nUtils.getProperty("modeler.outline.openCreateApplicationWrapperProcessWizard"),
			provider : DefaultOutlineHandler,
			handlerMethod: "openProcessInterfaceTestWrapperProcessWizard",
			visibility : "always"
		}]
	};
});