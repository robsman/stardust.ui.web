define([], function() {
	return {
		view : [ {
			viewId : "modelView",
			viewLabel : "Model",
			viewHtmlUrl : "bpm-modeler/views/modeler/modelView.html"
		}, {
			viewId : "processDefinitionView",
			viewLabel : "Process Definition",
			viewHtmlUrl : "bpm-modeler/views/modeler/processDefinitionView.html"
		}, {
			viewId : "roleView",
			viewLabel : "Role",
			viewHtmlUrl : "bpm-modeler/views/modeler/roleView.html"
		}, {
			viewId : "organizationView",
			viewLabel : "Organization",
			viewHtmlUrl : "bpm-modeler/views/modeler/organizationView.html"
		},
		{
			viewId : "conditionalPerformerView",
			viewLabel : "Conditional Performer",
			viewHtmlUrl : "bpm-modeler/views/modeler/conditionalPerformerView.html"
		},
		{
			viewId : "dataView",
			viewLabel : "Data",
			viewHtmlUrl : "bpm-modeler/views/modeler/dataView.html"
		},
		{
			viewId : "genericApplicationView",
			viewLabel : "Unsupported Application",
			viewHtmlUrl : "bpm-modeler/views/modeler/genericApplicationView.html"
		},
		{
			viewId : "webServiceApplicationView",
			viewLabel : "Web Service",
			viewHtmlUrl : "bpm-modeler/views/modeler/webServiceApplicationView.html"
		},
		{
			viewId : "uiMashupApplicationView",
			viewLabel : "UI Mashup",
			viewHtmlUrl : "bpm-modeler/views/modeler/uiMashupApplicationView.html"
		},
		{
			viewId : "camelApplicationView",
			viewLabel : "Camel Route",
			viewHtmlUrl : "bpm-modeler/views/modeler/camelApplicationView.html"
		},
		{
			viewId : "messageTransformationApplicationView",
			viewLabel : "Message Transformation",
			viewHtmlUrl : "bpm-modeler/views/modeler/messageTransformationApplicationView.html"
		},
		{
			viewId : "xsdStructuredDataTypeView",
			viewLabel : "Structured Data",
			viewHtmlUrl : "bpm-modeler/views/modeler/xsdStructuredDataTypeView.html"
		}  ]
	};
});