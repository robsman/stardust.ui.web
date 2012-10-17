define([ 'm_defaultPaletteHandler', 'm_decorationPalette' ], function(m_defaultPaletteHandler, m_decorationPalette) {
	return {
		// sections
		diagramToolbarPalette : [ {
			id : "drawingPalette",
			title : "Drawing",
			visibility : "always"
		}, {
			id : "activityPalette",
			title : "Activities and Gateways",
			visibilty : "always"
		}, {
			id : "eventPalette",
			title : "Events",
			visibility : "always"
		}, {
			id : "dataPalette",
			title : "Data",
			visibility : "always"
		}, {
			id : "lanePalette",
			title : "Pools and Lanes",
			visibility : "always"
		}, {
			id : "connectorPalette",
			title : "Data and Sequence Flow",
			visibility : "always"
		}, {
			id : "annotationPalette",
			title : "Annotations",
			visibility : "always"
		}, {
			id : "decorationPalette",
			title : "Decoration",
			contentHtmlUrl : "decorationPalette.html",
			provider: m_decorationPalette,
			visibility : "preview"
		}, ],
		// entries
		diagramToolbarPaletteEntry : [ {
			id : "selectModeButton",
			paletteId : "drawingPalette",
			title : "Select Mode",
			iconUrl : "../../images/icons/select.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "setSelectMode",
			visibility : "always"
		}, {
			id : "separatorModeButton",
			paletteId : "drawingPalette",
			title : "Separator Mode",
			iconUrl : "../../images/icons/separator.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "setSeparatorMode",
			visibility : "always"
		}, {
			id : "zoomInButton",
			paletteId : "drawingPalette",
			title : "Zoom In",
			iconUrl : "../../images/icons/zoom-in.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "zoomIn",
			visibility : "always"
		}, {
			id : "zoomOutButton",
			paletteId : "drawingPalette",
			title : "Zoom Out",
			iconUrl : "../../images/icons/zoom-out.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "zoomOut",
			visibility : "always"
		}, {
			id : "flipOrientationButton",
			paletteId : "drawingPalette",
			title : "Flip Orientation",
			iconUrl : "../../images/icons/horizontal-flip.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "flipOrientation",
			visibility : "always"
		}, {
			id : "printButton",
			paletteId : "drawingPalette",
			title : "Print",
			iconUrl : "../../images/icons/print.gif",
			provider : m_defaultPaletteHandler,
			handlerMethod: "print",
			visibility : "always"
		}, {
			id : "activityButton",
			paletteId : "activityPalette",
			title : "Create Activity",
			iconUrl : "../../images/icons/activity.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "createActivity",
			visibility : "always"
		}, {
			id : "gatewayButton",
			paletteId : "activityPalette",
			title : "Create Gateway",
			iconUrl : "../../images/icons/gateway.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "createGateway",
			visibility : "always"
		}, {
			id : "startEventButton",
			paletteId : "eventPalette",
			title : "Create Start Event",
			iconUrl : "../../images/icons/start_event_with_border.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "createStartEvent",
			visibility : "always"
		}, {
			id : "endEventButton",
			paletteId : "eventPalette",
			title : "Create End Event",
			iconUrl : "../../images/icons/end_event_with_border.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "createEndEvent",
			visibility : "always"
		}, {
			id : "dataButton",
			paletteId : "dataPalette",
			title : "Create Primitive Data",
			iconUrl : "../../images/icons/data.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "createData",
			visibility : "always"
		}, {
			id : "swimlaneButton",
			paletteId : "lanePalette",
			title : "Create Swimlane",
			iconUrl : "../../images/icons/lane.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "createSwimlane",
			visibility : "always"
		}, {
			id : "connectorButton",
			paletteId : "connectorPalette",
			title : "Create Connector",
			iconUrl : "../../images/icons/connect.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "createConnector",
			visibility : "always"
		}, {
			id : "annotationButton",
			paletteId : "annotationPalette",
			title : "Create Annotation",
			iconUrl : "../../images/icons/annotation.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "createAnnotation",
			visibility : "always"
		}, ]
	};
});