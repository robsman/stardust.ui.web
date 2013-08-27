/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ 'bpmn2-modeler/js/modelOperationsHandler', "bpm-modeler/js/m_i18nUtils" ],
		function(handler, m_i18nUtils) {
	return {
		menuOption : [ {
			id : "cloneToXpdl",
			nodeType : "model",
			label :  "Clone Model", //m_i18nUtils.getProperty("bpmn2Modeler.outline.cloneModelCmd"),
			provider : handler,
			handlerMethod: "cloneModel",
			visibility : "preview"
		}]
	};
});