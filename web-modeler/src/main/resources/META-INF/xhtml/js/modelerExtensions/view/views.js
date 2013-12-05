/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([], function() {
	return {
		view : [ {
			viewId : "modelView",
			label : "Model ${viewParams.modelName}[20]",
			viewHtmlUrl : "plugins/bpm-modeler/views/modeler/modelView.html",
			iconUrl : "plugins/bpm-modeler/images/icons/model.png"
		}, {
			viewId : "processDefinitionView",
			label : "Process Definition ${viewParams.processName}[20]",
			viewHtmlUrl : "plugins/bpm-modeler/views/modeler/processDefinitionView.html",
			iconUrl : "plugins/bpm-modeler/images/icons/process.png"
		}, {
			viewId : "roleView",
			label : "Role ${viewParams.roleName}[20]",
			viewHtmlUrl : "plugins/bpm-modeler/views/modeler/roleView.html",
			iconUrl : "plugins/bpm-modeler/images/icons/role.png"
		}, {
			viewId : "organizationView",
			label : "Organization ${viewParams.organizationName}[20]",
			viewHtmlUrl : "plugins/bpm-modeler/views/modeler/organizationView.html",
			iconUrl : "plugins/bpm-modeler/images/icons/organization.png"
		},
		{
			viewId : "conditionalPerformerView",
			label : "Conditional Performer ${viewParams.conditionalPerformerName}[20]",
			viewHtmlUrl : "plugins/bpm-modeler/views/modeler/conditionalPerformerView.html",
			iconUrl : "plugins/bpm-modeler/images/icons/conditional.png"
		},
		{
			viewId : "dataView",
			label : "Data ${viewParams.dataName}[20]",
			viewHtmlUrl : "plugins/bpm-modeler/views/modeler/dataView.html",
			iconUrl : "plugins/bpm-modeler/images/icons/data.png"
		},
		{
			viewId : "genericApplicationView",
			label : "Unsupported Application ${viewParams.applicationName}[20]",
			viewHtmlUrl : "plugins/bpm-modeler/views/modeler/genericApplicationView.html",
			iconUrl : "plugins/bpm-modeler/images/icons/applications-blue.png"
		},
		{
			viewId : "webServiceApplicationView",
			label : "Web Service ${viewParams.applicationName}[20]",
			viewHtmlUrl : "plugins/bpm-modeler/views/modeler/webServiceApplicationView.html",
			iconUrl : "plugins/bpm-modeler/images/icons/application-web-service.png"
		},
		{
			viewId : "uiMashupApplicationView",
			label : "UI Mashup ${viewParams.applicationName}[20]",
			viewHtmlUrl : "plugins/bpm-modeler/views/modeler/uiMashupApplicationView.html",
			iconUrl : "plugins/bpm-modeler/images/icons/application-c-ext-web.png"
		},
		{
			viewId : "camelApplicationView",
			label : "Camel Route ${viewParams.applicationName}[20]",
			viewHtmlUrl : "plugins/bpm-modeler/views/modeler/camelApplicationView.html",
			iconUrl : "plugins/bpm-modeler/images/icons/application-camel.png"
		},
		{
			viewId : "messageTransformationApplicationView",
			label : "Message Transformation ${viewParams.applicationName}[20]",
			viewHtmlUrl : "plugins/bpm-modeler/views/modeler/messageTransformationApplicationView.html",
			iconUrl : "plugins/bpm-modeler/images/icons/application-message-trans.png"
		},
		{
			viewId : "xsdStructuredDataTypeView",
			label : "Structured Data ${viewParams.structuredDataTypeName}[20]",
			viewHtmlUrl : "plugins/bpm-modeler/views/modeler/xsdStructuredDataTypeView.html",
			iconUrl : "plugins/bpm-modeler/images/icons/structured-type.png"
		}  ]
	};
});