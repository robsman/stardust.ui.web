define([], function() {
	return {
		view : [ {
			viewId : "modelView",
			viewLabel : "Model",
			viewHtmlUrl : "../views/modeler/modelView.html"
		}, {
			viewId : "processDefinitionView",
			viewLabel : "Process Definition",
			viewHtmlUrl : "../views/modeler/processDefinitionView.html"
		}, {
			viewId : "roleView",
			viewLabel : "Role",
			viewHtmlUrl : "../views/modeler/roleView.html"
		}, {
			viewId : "organizationView",
			viewLabel : "Organization",
			viewHtmlUrl : "../views/modeler/organizationView.html"
		},
		{
			viewId : "conditionalPerformerView",
			viewLabel : "Conditional Performer",
			viewHtmlUrl : "../views/modeler/conditionalPerformerView.html"
		},
		{
			viewId : "dataView",
			viewLabel : "Data",
			viewHtmlUrl : "../views/modeler/dataView.html"
		},
		{
			viewId : "genericApplicationView",
			viewLabel : "Unsupported Application",
			viewHtmlUrl : "../views/modeler/genericApplicationView.html"
		},
		{
			viewId : "webServiceApplicationView",
			viewLabel : "Web Service",
			viewHtmlUrl : "../views/modeler/webServiceApplicationView.html"
		},
		{
			viewId : "uiMashupApplicationView",
			viewLabel : "UI Mashup",
			viewHtmlUrl : "../views/modeler/uiMashupApplicationView.html"
		},
		{
			viewId : "camelApplicationView",
			viewLabel : "Camel Route",
			viewHtmlUrl : "../views/modeler/camelApplicationView.html"
		},
		{
			viewId : "messageTransformationApplicationView",
			viewLabel : "Message Transformation",
			viewHtmlUrl : "../views/modeler/messageTransformationApplicationView.html"
		},
		{
			viewId : "xsdStructuredDataTypeView",
			viewLabel : "Structured Data",
			viewHtmlUrl : "../views/modeler/xsdStructuredDataTypeView.html"
		}  ]
	};
});