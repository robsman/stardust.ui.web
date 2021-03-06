/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ 'bpm-modeler/js/m_defaultPaletteHandler', 'bpm-modeler/js/m_decorationPalette', "bpm-modeler/js/m_i18nUtils" ],
		function(m_defaultPaletteHandler, m_decorationPalette, m_i18nUtils) {
	return {
		// sections
		diagramToolbarPalette : [ {
			id : "drawingPalette",
			title : m_i18nUtils.getProperty("modeler.diagram.toolbar.panels.drawing.title"),
			visibility : "always"
		}, {
			id : "activityPalette",
			title :  m_i18nUtils.getProperty("modeler.diagram.toolbar.panels.activitiesGateway.title"),
			visibilty : "always"
		}, {
			id : "eventPalette",
			title :  m_i18nUtils.getProperty("modeler.diagram.toolbar.panels.events.title"),
			visibility : "always"
		}, {
			id : "dataPalette",
			title : m_i18nUtils.getProperty("modeler.element.properties.commonProperties.data"),
			visibility : "always"
		}, {
			id : "lanePalette",
			title : m_i18nUtils.getProperty("modeler.diagram.toolbar.panels.poolsAndLanes.title"),
			visibility : "always"
		}, {
			id : "connectorPalette",
			title : m_i18nUtils.getProperty("modeler.diagram.toolbar.panels.dataSequence.title"),
			visibility : "always"
		}, {
			id : "annotationPalette",
			title : m_i18nUtils.getProperty("modeler.diagram.toolbar.panels.annotations.title"),
			visibility : "always"
		}, {
			id : "decorationPalette",
			title : m_i18nUtils.getProperty("modeler.diagram.toolbar.panels.decoration.title"),
			contentHtmlUrl : "plugins/bpm-modeler/views/modeler/decorationPalette.html",
			provider: m_decorationPalette,
			visibility : "preview"
		}, ],
		// entries
		diagramToolbarPaletteEntry : [ {
			id : "selectModeButton",
			paletteId : "drawingPalette",
			title :  m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.selectMode.title"),
			iconUrl : "plugins/bpm-modeler/images/icons/cursor.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "setSelectMode",
			visibility : "always"
		}, {
			id : "separatorModeButton",
			paletteId : "drawingPalette",
			title : m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.separateMode.title"),
			iconUrl : "plugins/bpm-modeler/images/icons/separator.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "setSeparatorMode",
			visibility : "preview"
		}, {
			id : "zoomInButton",
			paletteId : "drawingPalette",
			title : m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.zoomIn.title"),
			iconUrl : "plugins/bpm-modeler/images/icons/zoom_in.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "zoomIn",
			visibility : "always",
			styleClass: "noDataChange"
		}, {
			id : "zoomOutButton",
			paletteId : "drawingPalette",
			title : m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.zoomOut.title"),
			iconUrl : "plugins/bpm-modeler/images/icons/zoom_out.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "zoomOut",
			visibility : "always",
			styleClass: "noDataChange"
		}, {
			id : "flipOrientationButton",
			paletteId : "drawingPalette",
			title : m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.flipOrientation.title"),
			iconUrl : "plugins/bpm-modeler/images/icons/horizontal-flip.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "flipOrientation",
			visibility : "preview"
		}, {
			id : "printButton",
			paletteId : "drawingPalette",
			title : m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.print.title"),
			iconUrl : "plugins/bpm-modeler/images/icons/printer.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "print",
			visibility : "always",
			styleClass: "noDataChange"
		}, {
			id : "activityButton",
			paletteId : "activityPalette",
			title : m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.createActivity.title"),
			iconUrl : "plugins/bpm-modeler/images/icons/activity.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "createActivity",
			visibility : "always"
		}, {
			id : "gatewayButton",
			paletteId : "activityPalette",
			title : m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.createGateway.title"),
			iconUrl : "plugins/bpm-modeler/images/icons/gateway.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "createGateway",
			visibility : "always"
		}, {
			id : "startEventButton",
			paletteId : "eventPalette",
			title : m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.createStartEvent.title"),
			iconUrl : "plugins/bpm-modeler/images/icons/start-event-toolbar.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "createStartEvent",
			visibility : "always"
		}, {
			id : "intermediateEventButton",
			paletteId : "eventPalette",
			title : m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.createIntermediateEvent.title"),
			iconUrl : "plugins/bpm-modeler/images/icons/intermediate-event-toolbar.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "createIntermediateEvent",
			visibility : "always"
		}, {
			id : "endEventButton",
			paletteId : "eventPalette",
			title : m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.createEndEvent.title"),
			iconUrl : "plugins/bpm-modeler/images/icons/end-event-toolbar.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "createEndEvent",
			visibility : "always"
		}, {
			id : "dataButton",
			paletteId : "dataPalette",
			title : m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.createPrimitiveData.title"),
			iconUrl : "plugins/bpm-modeler/images/icons/data.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "createData",
			visibility : "always"
		}, {
			id : "swimlaneButton",
			paletteId : "lanePalette",
			title :m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.createSwimlane.title"),
			iconUrl : "plugins/bpm-modeler/images/icons/lane.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "createSwimlane",
			visibility : "always"
		}, {
			id : "connectorButton",
			paletteId : "connectorPalette",
			title : m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.createConnector.title"),
			iconUrl : "plugins/bpm-modeler/images/icons/connect.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "createConnector",
			visibility : "always"
		}, {
			id : "annotationButton",
			paletteId : "annotationPalette",
			title : m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.createAnnotation.title"),
			iconUrl : "plugins/bpm-modeler/images/icons/edit.png",
			provider : m_defaultPaletteHandler,
			handlerMethod: "createAnnotation",
			visibility : "always"
		}, ]
	};
});