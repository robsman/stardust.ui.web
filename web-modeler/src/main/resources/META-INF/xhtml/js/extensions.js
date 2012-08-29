var extensions = {
	viewManager : [ {
		moduleUrl : "m_jsfViewManager"
	} ],
	propertiesPage : [ {
		panelId : "processPropertiesPanel",
		pageId : "basicPropertiesPage",
		pageJavaScriptUrl : "m_processBasicPropertiesPage",
		visibility : "always"
	}, {
		panelId : "processPropertiesPanel",
		pageId : "processInterfacePropertiesPage",
		pageJavaScriptUrl : "m_processProcessInterfacePropertiesPage",
		visibility : "always"
	}, {
		panelId : "processPropertiesPanel",
		pageId : "dataPathPropertiesPage",
		pageJavaScriptUrl : "m_processDataPathPropertiesPage",
		visibility : "always"
	}, {
		panelId : "processPropertiesPanel",
		pageId : "displayPropertiesPage",
		pageJavaScriptUrl : "m_processDisplayPropertiesPage",
		visibility : "always"
	}, {
		panelId : "processPropertiesPanel",
		pageId : "processAttachmentsPropertiesPage",
		pageHtmlUrl : "processDefinitionProcessAttachmentsPropertiesPage.html",
		pageJavaScriptUrl : "m_processProcessAttachmentsPropertiesPage",
		visibility : "always"
	}, {
		panelId : "activityPropertiesPanel",
		pageId : "basicPropertiesPage",
		pageJavaScriptUrl : "m_activityBasicPropertiesPage",
		visibility : "always"
	}, {
		panelId : "activityPropertiesPanel",
		pageId : "processingPropertiesPage",
		pageJavaScriptUrl : "m_activityProcessingPropertiesPage",
		visibility : "preview"
	}, {
		panelId : "activityPropertiesPanel",
		pageId : "controllingPropertiesPage",
		pageHtmlUrl : "activityControllingPropertiesPage.html",
		pageJavaScriptUrl : "m_activityControllingPropertiesPage",
		visibility : "always"
	}, {
		panelId : "activityPropertiesPanel",
		pageId : "qualityControlPropertiesPage",
		pageJavaScriptUrl : "m_activityQualityControlPropertiesPage",
		visibility : "preview"
	}, {
		panelId : "eventPropertiesPanel",
		pageId : "basicPropertiesPage",
		pageJavaScriptUrl : "m_eventBasicPropertiesPage",
		visibility : "always"
	}, {
		panelId : "gatewayPropertiesPanel",
		pageId : "basicPropertiesPage",
		pageJavaScriptUrl : "m_gatewayBasicPropertiesPage",
		visibility : "always"
	}, {
		panelId : "swimlanePropertiesPanel",
		pageId : "basicPropertiesPage",
		pageJavaScriptUrl : "m_swimlaneBasicPropertiesPage",
		visibility : "always"
	}, {
		panelId : "dataPropertiesPanel",
		pageId : "basicPropertiesPage",
		pageJavaScriptUrl : "m_dataBasicPropertiesPage",
		visibility : "always"
	}, {
		panelId : "controlFlowPropertiesPanel",
		pageId : "basicPropertiesPage",
		pageJavaScriptUrl : "m_controlFlowBasicPropertiesPage",
		visibility : "always"
	}, {
		panelId : "controlFlowPropertiesPanel",
		pageId : "transactionPropertiesPage",
		pageHtmlUrl : "controlFlowTransactionPropertiesPage.html",
		pageJavaScriptUrl : "m_controlFlowTransactionPropertiesPage",
		visibility : "always"
	}, {
		panelId : "dataFlowPropertiesPanel",
		pageId : "basicPropertiesPage",
		pageJavaScriptUrl : "m_dataFlowBasicPropertiesPage",
		visibility : "always"
	} ],
	diagramToolbarPalette : [ {
		id : "drawingPalette",
		title : "Drawing",
		visibility : "always"
	}, {
		id : "activityPalette",
		title : "Activities and Gateways",
		visibility : "always"
	}, {
		id : "eventPalette",
		title : "Events",
		visibility : "always"
	}, {
		id : "dataPalette",
		title : "Data"
	}, {
		id : "lanePalette",
		title : "Pools and Lanes",
		visibility : "always"
	}, {
		id : "connectorPalette",
		title : "Data and Sequence Flow",
		visibility : "always"
	}, {
		id : "decorationPalette",
		title : "Decoration",
		contentHtmlUrl : "decorationPalette.html",
		controllerJavaScriptUrl : "m_decorationPalette",
		visibility : "preview"
	} ],
	diagramToolbarPaletteEntry : [ {
		id : "selectModeButton",
		paletteId : "drawingPalette",
		title : "Select Mode",
		iconUrl : "../../images/icons/select.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "setSelectMode",
		visibility : "always"
	}, {
		id : "separatorModeButton",
		paletteId : "drawingPalette",
		title : "Separator Mode",
		iconUrl : "../../images/icons/separator.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "setSeparatorMode",
		visibility : "always"
	}, {
		id : "zoomInButton",
		paletteId : "drawingPalette",
		title : "Zoom In",
		iconUrl : "../../images/icons/zoom-in.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "zoomIn",
		visibility : "always"
	}, {
		id : "zoomOutButton",
		paletteId : "drawingPalette",
		title : "Zoom Out",
		iconUrl : "../../images/icons/zoom-out.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "zoomOut",
		visibility : "always"
	}, {
		id : "undoButton",
		paletteId : "drawingPalette",
		title : "Undo",
		iconUrl : "../../images/icons/undo.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "undo",
		visibility : "always"
	}, {
		id : "redoButton",
		paletteId : "drawingPalette",
		title : "Redo",
		iconUrl : "../../images/icons/redo.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "redo",
		visibility : "always"
	}, {
		id : "flipOrientationButton",
		paletteId : "drawingPalette",
		title : "Flip Orientation",
		iconUrl : "../../images/icons/horizontal-flip.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "flipOrientation",
		visibility : "always"
	}, {
		id : "printButton",
		paletteId : "drawingPalette",
		title : "Print",
		iconUrl : "../../images/icons/print.gif",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "print",
		visibility : "always"
	}, {
		id : "activityButton",
		paletteId : "activityPalette",
		title : "Create Activity",
		iconUrl : "../../images/icons/activity-manual.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "createActivity",
		visibility : "always"
	}, {
		id : "gatewayButton",
		paletteId : "activityPalette",
		title : "Create Gateway",
		iconUrl : "../../images/icons/gateway.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "createGateway",
		visibility : "always"
	}, {
		id : "startEventButton",
		paletteId : "eventPalette",
		title : "Create Start Event",
		iconUrl : "../../images/icons/start.PNG",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "createStartEvent",
		visibility : "always"
	}, {
		id : "endEventButton",
		paletteId : "eventPalette",
		title : "Create End Event",
		iconUrl : "../../images/icons/stop.PNG",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "createEndEvent",
		visibility : "always"
	}, {
		id : "dataButton",
		paletteId : "dataPalette",
		title : "Create Primitive Data",
		iconUrl : "../../images/icons/database.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "createData",
		visibility : "always"
	}, {
		id : "swimlaneButton",
		paletteId : "lanePalette",
		title : "Create Swimlane",
		iconUrl : "../../images/icons/lane.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "createSwimlane",
		visibility : "always"
	}, {
		id : "connectorButton",
		paletteId : "connectorPalette",
		title : "Create Connector",
		iconUrl : "../../images/icons/connector.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "createConnector",
		visibility : "always"
	} ],
	applicationType : [
			{
				id : "interactive",
				readableName : "UI Mashup",
				iconPath : "../images/icons/application-c-ext-web.png",
				viewId : "uiMashupApplicationView"
			},
			{
				id : "webservice",
				readableName : "Web Service",
				iconPath : "../images/icons/application-web-service.png",
				viewId : "webServiceApplicationView"
			},
			{
				id : "messageTransformationBean",
				readableName : "Message Transformation Application",
				iconPath : "../images/icons/application-message-trans.png",
				viewId : "messageTransformationApplicationView"
			}, {
				id : "camelBean",
				readableName : "Camel Application",
				iconPath : "../images/icons/application-camel.png",
				viewId : "camelApplicationView"
			}, {
				id : "plainJava",
				readableName : "Plain Java Application",
				iconPath : "../images/icons/application-plain-java.png",
				viewId : "genericApplicationView"
			}, {
				id : "rulesEngineBean",
				readableName : "Business Rules Application",
				iconPath : "../images/icons/application-plain-java.png",
				viewId : "genericApplicationView"
			} ],
			dataType : [
			       			{
			       				id : "serializable",
			       				readableName : "Serializable Java Class",
			       				iconPath : "../images/icons/application-c-ext-web.png",
			       			},
			       			{
			       				id : "entity",
			       				readableName : "Entity Bean",
			       				iconPath : "../images/icons/application-web-service.png",
			       			},
			       			{
			       				id : "dmsDocumentList",
			       				readableName : "Document List",
			       				iconPath : "../images/icons/application-web-service.png",
			       			}]
};