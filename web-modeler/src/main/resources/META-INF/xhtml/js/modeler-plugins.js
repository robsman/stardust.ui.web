/**
 * @author Omkar.Patil
 */

define([ "require", "bpm-modeler/js/m_extensionManager", "bpm-modeler/js/m_logger", "bpm-modeler/js/m_utils",
		"bpm-modeler/js/m_communicationController", "bpm-modeler/js/m_canvasManager", "bpm-modeler/js/m_toolbarManager",
		"bpm-modeler/js/m_modelerViewLayoutManager", "bpm-modeler/js/m_urlUtils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_user",
		"bpm-modeler/js/m_command", "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_accessPoint", "bpm-modeler/js/m_diagram",
		"bpm-modeler/js/m_modelerCanvasController", "bpm-modeler/js/m_modelerViewLayoutManager", "bpm-modeler/js/m_defaultPaletteHandler",
		"bpm-modeler/js/m_dataTypeSelector", "bpm-modeler/js/m_propertiesPanel", "bpm-modeler/js/m_activityPropertiesPanel",
		"bpm-modeler/js/m_gatewayPropertiesPanel", "bpm-modeler/js/m_eventPropertiesPanel",
		"bpm-modeler/js/m_dataFlowPropertiesPanel", "bpm-modeler/js/m_controlFlowPropertiesPanel",
		"bpm-modeler/js/m_command", "bpm-modeler/js/m_drawable", "bpm-modeler/js/m_symbol", "bpm-modeler/js/m_poolSymbol",
		"bpm-modeler/js/m_swimlaneSymbol", "bpm-modeler/js/m_activitySymbol", "bpm-modeler/js/m_eventSymbol",
		"bpm-modeler/js/m_gatewaySymbol", "bpm-modeler/js/m_dataSymbol", "bpm-modeler/js/m_testSymbol", "bpm-modeler/js/m_connection",
		"bpm-modeler/js/m_jsfViewManager", "bpm-modeler/js/m_processBasicPropertiesPage",
		"bpm-modeler/js/m_processProcessInterfacePropertiesPage",
		"bpm-modeler/js/m_processDataPathPropertiesPage", "bpm-modeler/js/m_processDisplayPropertiesPage",
		"bpm-modeler/js/m_processTestPropertiesPage",
		"bpm-modeler/js/m_processProcessAttachmentsPropertiesPage",
		"bpm-modeler/js/m_activityBasicPropertiesPage", "bpm-modeler/js/m_activityProcessingPropertiesPage",
		"bpm-modeler/js/m_activityControllingPropertiesPage",
		"bpm-modeler/js/m_activityQualityControlPropertiesPage", "bpm-modeler/js/m_activityServiceParametersPropertiesPage", "bpm-modeler/js/m_eventBasicPropertiesPage",
		"bpm-modeler/js/m_gatewayBasicPropertiesPage", "bpm-modeler/js/m_swimlaneBasicPropertiesPage",
		"bpm-modeler/js/m_dataBasicPropertiesPage", , "bpm-modeler/js/m_annotationBasicPropertiesPage", "bpm-modeler/js/m_controlFlowBasicPropertiesPage",
		"bpm-modeler/js/m_controlFlowTransactionPropertiesPage",
		"bpm-modeler/js/m_dataFlowBasicPropertiesPage", "bpm-modeler/js/m_decorationPalette", "bpm-modeler/js/m_commentsPanel", "bpm-modeler/js/m_modelElementCommentsPropertiesPage" ], function(
		require, m_extensionManager) {
	// inject plugins module loader to extensions manager
	m_extensionManager.initialize(require);
	return {};
});
