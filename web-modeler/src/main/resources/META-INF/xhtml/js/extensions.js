var extensions = {
	viewManager : [ {
		moduleUrl : "m_jsfViewManager"
	} ],
	propertiesPage : [ {
		panelId : "processPropertiesPanel",
		pageId : "basicPropertiesPage",
		pageJavaScriptUrl : "m_processBasicPropertiesPage"
	}, {
		panelId : "processPropertiesPanel",
		pageId : "processInterfacePropertiesPage",
		pageJavaScriptUrl : "m_processProcessInterfacePropertiesPage"
	}, {
		panelId : "processPropertiesPanel",
		pageId : "dataPathPropertiesPage",
		pageJavaScriptUrl : "m_processDataPathPropertiesPage"
	}, {
		panelId : "processPropertiesPanel",
		pageId : "displayPropertiesPage",
		pageJavaScriptUrl : "m_processDisplayPropertiesPage"
	}, {
		panelId : "activityPropertiesPanel",
		pageId : "basicPropertiesPage",
		pageJavaScriptUrl : "m_activityBasicPropertiesPage"
	}, {
		panelId : "activityPropertiesPanel",
		pageId : "processingPropertiesPage",
		pageJavaScriptUrl : "m_activityProcessingPropertiesPage"
	}, {
		panelId : "activityPropertiesPanel",
		pageId : "controllingPropertiesPage",
		pageHtmlUrl : "activityControllingPropertiesPage.html",
		pageJavaScriptUrl : "m_activityControllingPropertiesPage"
	}, {
		panelId : "activityPropertiesPanel",
		pageId : "qualityControlPropertiesPage",
		pageJavaScriptUrl : "m_activityQualityControlPropertiesPage"
	}, {
		panelId : "eventPropertiesPanel",
		pageId : "basicPropertiesPage",
		pageJavaScriptUrl : "m_eventBasicPropertiesPage"
	}, {
		panelId : "gatewayPropertiesPanel",
		pageId : "basicPropertiesPage",
		pageJavaScriptUrl : "m_gatewayBasicPropertiesPage"
	}, {
		panelId : "swimlanePropertiesPanel",
		pageId : "basicPropertiesPage",
		pageJavaScriptUrl : "m_swimlaneBasicPropertiesPage"
	}, {
		panelId : "dataPropertiesPanel",
		pageId : "basicPropertiesPage",
		pageJavaScriptUrl : "m_dataBasicPropertiesPage"
	}, {
		panelId : "controlFlowPropertiesPanel",
		pageId : "basicPropertiesPage",
		pageJavaScriptUrl : "m_controlFlowBasicPropertiesPage"
	}, {
		panelId : "dataFlowPropertiesPanel",
		pageId : "basicPropertiesPage",
		pageJavaScriptUrl : "m_dataFlowBasicPropertiesPage"
	} ],
	diagramToolbarPalette : [ {
		id : "drawingPalette",
		title : "Drawing"
	}, {
		id : "activityPalette",
		title : "Activities and Gateways"
	}, {
		id : "eventPalette",
		title : "Events"
	}, {
		id : "dataPalette",
		title : "Data"
	}, {
		id : "lanePalette",
		title : "Pools and Lanes"
	}, {
		id : "connectorPalette",
		title : "Data and Control Flow"
	} ],
	diagramToolbarPaletteEntry : [ {
		id : "selectModeButton",
		paletteId : "drawingPalette",
		title : "Select Mode",
		iconUrl : "../../images/icons/select.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "setSelectMode"
	}, {
		id : "separatorModeButton",
		paletteId : "drawingPalette",
		title : "Separator Mode",
		iconUrl : "../../images/icons/separator.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "setSeparatorMode"
	}, {
		id : "zoomInButton",
		paletteId : "drawingPalette",
		title : "Zoom In",
		iconUrl : "../../images/icons/zoom-in.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "zoomIn"
	}, {
		id : "zoomOutButton",
		paletteId : "drawingPalette",
		title : "Zoom Out",
		iconUrl : "../../images/icons/zoom-out.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "zoomOut"
	}, {
		id : "undoButton",
		paletteId : "drawingPalette",
		title : "Undo",
		iconUrl : "../../images/icons/undo.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "undo"
	}, {
		id : "redoButton",
		paletteId : "drawingPalette",
		title : "Redo",
		iconUrl : "../../images/icons/redo.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "redo"
	}, {
		id : "flipOrientationButton",
		paletteId : "drawingPalette",
		title : "Flip Orientation",
		iconUrl : "../../images/icons/horizontal-flip.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "flipOrientation"
	}, {
		id : "printButton",
		paletteId : "drawingPalette",
		title : "Print",
		iconUrl : "../../images/icons/print.gif",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "print"
	}, {
		id : "activityButton",
		paletteId : "activityPalette",
		title : "Create Activity",
		iconUrl : "../../images/icons/activity-manual.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "createActivity"
	}, {
		id : "gatewayButton",
		paletteId : "activityPalette",
		title : "Create Gateway",
		iconUrl : "../../images/icons/gateway.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "createGateway"
	}, {
		id : "startEventButton",
		paletteId : "eventPalette",
		title : "Create Start Event",
		iconUrl : "../../images/icons/start.PNG",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "createStartEvent"
	}, {
		id : "endEventButton",
		paletteId : "eventPalette",
		title : "Create End Event",
		iconUrl : "../../images/icons/stop.PNG",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "createEndEvent"
	}, {
		id : "dataButton",
		paletteId : "dataPalette",
		title : "Create Primitive Data",
		iconUrl : "../../images/icons/database.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "createData"
	}, {
		id : "swimlaneButton",
		paletteId : "lanePalette",
		title : "Create Swimlane",
		iconUrl : "../../images/icons/lane.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "createSwimlane"
	}, {
		id : "connectorButton",
		paletteId : "connectorPalette",
		title : "Create Connector",
		iconUrl : "../../images/icons/connector.png",
		handler : "m_defaultPaletteHandler",
		handlerMethod : "createConnector"
	} ]
};