/**
 * @author Omkar.Patil
 */

define([ "require", "m_extensionManager", "m_logger", "m_utils",
		"m_communicationController", "m_canvasManager", "m_toolbarManager",
		"m_modelerViewLayoutManager", "m_urlUtils", "m_constants", "m_user",
		"m_command", "m_commandsController", "m_accessPoint", "m_diagram",
		"m_modelerCanvasController", "m_defaultPaletteHandler",
		"m_dataTypeSelector", "m_propertiesPanel", "m_activityPropertiesPanel",
		"m_gatewayPropertiesPanel", "m_eventPropertiesPanel",
		"m_dataFlowPropertiesPanel", "m_controlFlowPropertiesPanel",
		"m_command", "m_drawable", "m_symbol", "m_poolSymbol",
		"m_swimlaneSymbol", "m_activitySymbol", "m_eventSymbol",
		"m_gatewaySymbol", "m_dataSymbol", "m_testSymbol", "m_connection",
		"m_jsfViewManager", "m_processBasicPropertiesPage",
		"m_processProcessInterfacePropertiesPage",
		"m_processDataPathPropertiesPage", "m_processDisplayPropertiesPage",
		"m_processTestPropertiesPage",
		"m_processProcessAttachmentsPropertiesPage",
		"m_activityBasicPropertiesPage", "m_activityProcessingPropertiesPage",
		"m_activityControllingPropertiesPage",
		"m_activityQualityControlPropertiesPage", "m_activityServiceParametersPropertiesPage", "m_eventBasicPropertiesPage",
		"m_gatewayBasicPropertiesPage", "m_swimlaneBasicPropertiesPage",
		"m_dataBasicPropertiesPage", "m_controlFlowBasicPropertiesPage",
		"m_controlFlowTransactionPropertiesPage",
		"m_dataFlowBasicPropertiesPage", "m_decorationPalette", "m_modelElementAnnotationsPropertiesPage" ], function(
		require, m_extensionManager) {
	// inject plugins module loader to extensions manager
	m_extensionManager.initialize(require);
	return {};
});
