/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

define(function() {

	return {
		// Diagram

		DIAGRAM_WIDTH : 1000,
		DIAGRAM_HEIGHT : 600,
		DIAGRAM_PANNING_INTERVAL_MILLIS : 100,
		DIAGRAM_PANNING_INCREMENT : 20,
		DIAGRAM_FLOW_ORIENTATION_VERTICAL : "DIAGRAM_FLOW_ORIENTATION_VERTICAL",
		DIAGRAM_FLOW_ORIENTATION_HORIZONTAL : "DIAGRAM_FLOW_ORIENTATION_HORIZONTAL",
		ZOOM_INCREMENT : 0.2,
		VIEWPORT_WIDTH : 1000,
		VIEWPORT_HEIGHT : 550,
		PANNING_SENSOR_WIDTH : 30,

		// Default graphics parameters

		DEFAULT_FONT_FAMILY : "Arial",
		DEFAULT_FONT_SIZE : 11,
		DEFAULT_STROKE_COLOR : "black",
		DEFAULT_STROKE_WIDTH : 0.5,
		SELECT_STROKE_COLOR : "#9d2127",
		SELECT_STROKE_WIDTH : 1.5,

		GLOW_WIDTH : 7.0,
		GLOW_OPACITY : 0.8,

		SELECT_FRAME_MARGIN : 7,
		SELECT_FRAME_STROKE : "#DDD7D7",
		SELECT_FRAME_STROKE_WIDTH : 1.0,
		SELECT_FRAME_DASHARRAY : "- ",
		SELECT_FRAME_R : 3,
		HIDDEN_FRAME_STROKE_WIDTH : 5.0,

		DEFAULT_ANCHOR_WIDTH : 6,
		DEFAULT_ANCHOR_HEIGHT : 6,
		SELECT_ANCHOR_WIDTH : 12,
		SELECT_ANCHOR_HEIGHT : 12,
		DEFAULT_ANCHOR_STROKE_COLOR : "black",
		DEFAULT_ANCHOR_STROKE_WIDTH : 0.5,
		DEFAULT_ANCHOR_FILL_COLOR : "white",
		SELECT_ANCHOR_STROKE_COLOR : "#b1252c",
		SELECT_ANCHOR_FILL_COLOR : "#b1252c",

		DEFAULT_FLY_OUT_MENU_WIDTH : 60,
		DEFAULT_FLY_OUT_MENU_HEIGHT : 30,
		FLY_OUT_MENU_STROKE : "#baa95b",
		FLY_OUT_MENU_STROKE_WIDTH : 0,
		FLY_OUT_MENU_FILL : "#fdfccc",
		FLY_OUT_MENU_START_OPACITY : 0,
		FLY_OUT_MENU_END_OPACITY : 1,
		FLY_OUT_MENU_CONTENT_MARGIN : 30,
		FLY_OUT_MENU_EMPTY_MARGIN : 10,
		FLY_OUT_MENU_ITEM_MARGIN : 5,
		FLY_OUT_MENU_R : 3,

		PROXIMITY_SENSOR_MARGIN : 20,

		// Drawable

		DRAWABLE_FLY_OUT_MENU_FADE_TIME : 1500,

		// Symbol

		SYMBOL_CREATED_STATE : 0,
		SYMBOL_PREPARED_STATE : 1,
		SYMBOL_COMPLETED_STATE : 2,
		FROM_ANCHOR_POINT : 0,
		TO_ANCHOR_POINT : 1,
		UNDEFINED_ORIENTATION : -1,
		NORTH : 0,
		EAST : 1,
		SOUTH : 2,
		WEST : 3,
		// Symbol should not be allowed to shrink below some size
		SYMBOL_MIN_SIZE : 10,

		// Activity Symbol

		ACTIVITY_SYMBOL : "activitySymbol",
		ACTIVITY_SYMBOL_DEFAULT_WIDTH : 180,
		ACTIVITY_SYMBOL_DEFAULT_HEIGHT : 50,
		ACTIVITY_SYMBOL_DEFAULT_FILL_COLOR : "0-white-#DEE0E0",
		ACTIVITY_SYMBOL_DEFAULT_FILL_OPACITY : 0.9,
		ACTIVITY_SYMBOL_DEFAULT_STROKE_COLOR : 'black',
		ACTIVITY_SYMBOL_DEFAULT_STROKE_WIDTH : 0.75,
		ACTIVITY_SYMBOL_DEFAULT_R : 4,
		ACTIVITY_BOUNDARY_EVENT_OFFSET : 15,

		// Gateway Symbol

		GATEWAY_SYMBOL : "gateSymbol",

		GATEWAY_SYMBOL_DEFAULT_WIDTH : 40,
		GATEWAY_SYMBOL_DEFAULT_HEIGHT : 40,
		GATEWAY_SYMBOL_DEFAULT_FILL_COLOR : 'white',
		GATEWAY_SYMBOL_DEFAULT_FILL_OPACITY : 0.9,
		GATEWAY_SYMBOL_DEFAULT_STROKE_COLOR : 'black',
		GATEWAY_SYMBOL_DEFAULT_STROKE_WIDTH : 0.75,
		GATEWAY_SYMBOL_PLUS_OFFSET : 8,
		GATEWAY_SYMBOL_PLUS_STROKE_WIDTH : 3.5,
		GATEWAY_SYMBOL_CROSS_OFFSET : 13,
		GATEWAY_SYMBOL_CROSS_STROKE_WIDTH : 3.5,
		GATEWAY_SYMBOL_OR_RADIUS : 9,
		GATEWAY_SYMBOL_OR_STROKE_WIDTH : 2.5,
		GATEWAY_SYMBOL_DEFAULT_WIDTH_EC : 180, //added to be able to see icons properly in Eclipse
		GATEWAY_SYMBOL_DEFAULT_HEIGHT_EC : 50, //added to be able to see icons properly in Eclipse

		// Event Symbol

		EVENT_SYMBOL : "eventSymbol",
		EVENT_DEFAULT_RADIUS : 13,
		EVENT_ICON_WIDTH : 16,
		EVENT_DEFAULT_FILL : 'white',
		EVENT_START_STROKE_WIDTH : 1.0,
		EVENT_INTERMEDIATE_STROKE_WIDTH : 1.0,
		EVENT_STOP_STROKE_WIDTH : 2.0,
		EVENT_INTERRUPTING_STROKE_DASHARRAY	: "",
		EVENT_NON_INTERRUPTING_STROKE_DASHARRAY	: "-",
		EVENT_ICON_WIDTH_EC : 110, //added to be able to see icons properly in Eclipse
		EVENT_ICON_HEIGHT_EC : 50, //added to be able to see icons properly in Eclipse

		// Data Symbol

		DATA_SYMBOL : "dataSymbol",
		DATA_SYMBOL_DEFAULT_WIDTH : 30,
		DATA_SYMBOL_DEFAULT_HEIGHT : 40,
		DATA_SYMBOL_DEFAULT_FILL_COLOR : 'white',
		DATA_SYMBOL_DEFAULT_FILL_OPACITY : 0.9,
		DATA_SYMBOL_DEFAULT_STROKE_COLOR : 'black',
		DATA_SYMBOL_DEFAULT_STROKE_WIDTH : 0.75,
		DATA_SYMBOL_DOG_EAR_OFFSET : 8,
		DATA_SYMBOL_DEFAULT_WIDTH_EC : 100, //added to be able to see icons properly in Eclipse
		DATA_SYMBOL_DEFAULT_HEIGHT_EC : 70, //added to be able to see icons properly in Eclipse

		// Annotation Symbol

		ANNOTATION_SYMBOL : "annotationSymbol",

		ANNOTATION_SYMBOL_DEFAULT_WIDTH : 80,
		ANNOTATION_SYMBOL_DEFAULT_HEIGHT : 30,
		ANNOTATION_SYMBOL_DEFAULT_FILL_COLOR : 'white',
		ANNOTATION_SYMBOL_DEFAULT_STROKE_WIDTH : 0.5,
		ANNOTATION_SYMBOL_TEXT_MAX : 14,

		ASSOCIATION : "association",

		// Connection

		UNKNOWN_FLOW_COLOR : "#aaaaaa",
		DATA_FLOW_COLOR : "#b8d2f3",
		CONTROL_FLOW_COLOR : "#7f8d9f",
		CONNECTION_STROKE_WIDTH : 1.5,
		CONNECTION_SELECT_STROKE_WIDTH : 2.0,
		CONNECTION_MINIMAL_SEGMENT_LENGTH : 16,
		CONNECTION_DEFAULT_STROKE_WIDTH : 1.0,
		CONNECTION_DEFAULT_ARROW_LENGTH : 14.0,
		CONNECTION_DEFAULT_ARROW_WIDTH : 6.0,
		CONNECTION_AUXILIARY_PICK_PATH_WIDTH : 6.0,
		CONNECTION_EXPRESSION_OFFSET : 15,
		CONNECTION_DEFAULT_PATH_OFFSET : 15,
		CONNECTION_DEFAULT_PATH_LENGTH : 8,
		CONNECTION_DEFAULT_EDGE_RADIUS : 5,

		// Pool and Swimlane

		POOL_SYMBOL : "poolSymbol",
		SWIMLANE_SYMBOL : "swimlaneSymbol",
		POOL_SWIMLANE_MARGIN : 12,
		SWIMLANE_SYMBOL_MARGIN : 10,
		POOL_SWIMLANE_TOP_BOX_HEIGHT : 20,
		POOL_SWIMLANE_STROKE_WIDTH : 1.2,
		POOL_SWIMLANE_SELECT_BOX_COLOR : "black",
		POOL_COLOR : "#d5d5d2",
		SWIMLANE_COLOR : "#c7d8db",
		LANE_DEFAULT_WIDTH : 375,
		LANE_DEFAULT_HEIGHT : 580,
		LANE_MIN_WIDTH : 80,
		// Adjustment required on Symbols
		POOL_LANE_MARGIN : 5,

		// Comments

		COMMENT_COUNT_COLOR : "black",
		COMMENT_COUNT_FONT_WEIGHT : "bold",
		COMMENT_COUNT_FONT_SIZE : 11,

		// Other graphics

		SNAP_LINE_STROKE_WIDTH : 0.5,
		SNAP_LINE_COLOR : "#b1252c",
		SNAP_LINE_DASHARRAY : "- ",

		SEPARATOR_LINE_STROKE_WIDTH : 0.5,
		SEPARATOR_LINE_COLOR : "green",
		SEPARATOR_LINE_DASHARRAY : "- ",

		RUBBERBAND_STROKE_WIDTH : 0.5,
		RUBBERBAND_COLOR : "green",
		RUBBERBAND_DASHARRAY : "- ",

		// Model

		MODEL : "model",
		DATE_OF_CREATION : "dateOfCreation",
		DATE_OF_MODIFICATION : "dateOfModification",

		// Process Definition

		PROCESS_DEFINITION : "processDefinition",
		PROCESS : "process",
		NO_PROCESS_INTERFACE_KEY : "noInterface",
		PROVIDES_PROCESS_INTERFACE_KEY : "providesProcessInterface",
		IMPLEMENTS_PROCESS_INTERFACE_KEY : "implementsProcessInterface",

		// Application

		APPLICATION : "application",
		JAVA_APPLICATION_TYPE : "plainJava",
		SPRING_BEAN_APPLICATION_TYPE : "springBean",
		SESSION_BEAN_APPLICATION_TYPE : "sessionBean",

		// Participant

		PARTICIPANT : "participant",
		ROLE_PARTICIPANT_TYPE : "roleParticipant",
		TEAM_LEADER_TYPE : "teamLeader",
		ORGANIZATION_PARTICIPANT_TYPE : "organizationParticipant",
		CONDITIONAL_PERFORMER_PARTICIPANT_TYPE : "conditionalPerformerParticipant",
		TEAM_LEADER_KEY : "isTeamLeader",
		CARDINALITY : "cardinality",

		// Activity

		ACTIVITY : "activity",
		SUBPROCESS_ACTIVITY_TYPE : "Subprocess",
		TASK_ACTIVITY_TYPE : "Task",
		GATEWAY_ACTIVITY_TYPE : "Gateway",
		NONE_TASK_TYPE : "none",
		MANUAL_TASK_TYPE : "manual",
		RECEIVE_TASK_TYPE : "receive",
		RULE_TASK_TYPE : "rule",
		SCRIPT_TASK_TYPE : "script",
		SEND_TASK_TYPE : "send",
		SERVICE_TASK_TYPE : "service",
		USER_TASK_TYPE : "user",
		SINGLE_PROCESSING_TYPE : "SINGLE_PROCESSING_TYPE",
		PARALLEL_MULTI_PROCESSING_TYPE : "PARALLEL_MULTI_PROCESSING_TYPE",
		SEQUENTIAL_MULTI_PROCESSING_TYPE : "SEQUENTIAL_MULTI_PROCESSING_TYPE",

		// Data

		DATA : "data",
		PRIMITIVE_DATA_TYPE : "primitive",
		BOOLEAN_PRIMITIVE_DATA_TYPE : "boolean",
		STRING_PRIMITIVE_DATA_TYPE : "string",
		DATE_PRIMITIVE_DATA_TYPE : "date",
		INTEGER_PRIMITIVE_DATA_TYPE : "int",
		DOUBLE_PRIMITIVE_DATA_TYPE : "double",
		DECIMAL_PRIMITIVE_DATA_TYPE : "decimal",
		DATE_PRIMITIVE_DATA_TYPE : "Calendar",
		STRUCTURED_DATA_TYPE : "struct",
		TYPE_DECLARATION_PROPERTY : "typeDeclaration",
		DOCUMENT_DATA_TYPE : "dmsDocument",
		ENTITY_DATA_TYPE : "entity",
		HIBERNATE_DATA_TYPE : "hibernate",

		// Type Declarations

		STRUCTURE_TYPE : "STRUCTURE_TYPE",
		ENUMERATION_TYPE : "ENUMERATION_TYPE",

		// Gateway

		GATEWAY : "gateway",
		AND_GATEWAY_TYPE : "and",
		XOR_GATEWAY_TYPE : "xor",
		OR_GATEWAY_TYPE : "or",

		// Event

		EVENT : "event",
		START_EVENT_TYPE : "startEvent",
		INTERMEDIATE_EVENT_TYPE : "intermediateEvent",
		STOP_EVENT_TYPE : "stopEvent",
		GENERIC_CAMEL_ROUTE_EVENT_CLASS : "genericCamelRouteEvent",
		NONE_EVENT_CLASS : "none",
		TIMER_EVENT_CLASS : "timer",
		MESSAGE_EVENT_CLASS : "message",
		ERROR_EVENT_CLASS : "exception",

		// Access Points

		IN_ACCESS_POINT : "IN",
		OUT_ACCESS_POINT : "OUT",
		IN_OUT_ACCESS_POINT : "INOUT",
		PRIMITIVE_ACCESS_POINT : "PRIMITIVE_ACCESS_POINT",
		JAVA_CLASS_ACCESS_POINT : "JAVA_CLASS_ACCESS_POINT",
		DATA_STRUCTURE_ACCESS_POINT : "DATA_STRUCTURE_ACCESS_POINT",
		ANY_ACCESS_POINT : "ANY_ACCESS_POINT",

		// Control Flow

		CONTROL_FLOW : "controlFlow",
		CONTROL_FLOW_CONNECTION : "controlFlowConnection",
		// Data Flow

		DATA_FLOW : "dataFlow",
		DATA_FLOW_CONNECTION : "dataFlowConnection",
		// Properties Panels

		TO_BE_DEFINED : "TO_BE_DEFINED",
		AUTO_GENERATED_UI : "AUTO_GENERATED_UI",

		// Servlet related constants.

		TIFF_RENDERER_SERVLET_PATH : "/IppTiffRenderer",
		ANNOTATIONS_RESTLET_PATH : "/services/rest/graphics-common/documents/",
		ANNOTATIONS_RESTLET_PATH_PAGE : "/pages/",
		DMS_RESTLET_PATH : "/services/rest/views-common/documentRepoService",
		STAMPS_GET_REQUEST_URL : "/retrieveStamps",
		DOCUMENT_DOWNLOAD_TOKEN_URL : "/documentDownloadURL",

		// Events

		CANVAS_CLICKED_EVENT : "CANVAS_CLICKED",
		TOOL_CLICK_EVENT_POSTFIX : "_TOOL_CLICKED",
		STICKEY_NOTE_ADD_EVENT : "STICKEY_NOTE_ADDED",
		HIGHLIGHTER_ADD_EVENT : "HIGHLIGHTER_ADDED",

		// Annotations

		HIGHLIGHTER_DEFAULT_WIDTH : 220,
		HIGHLIGHTER_DEFAULT_HEIGHT : 100,
		HIGHLIGHTER_DEFAULT_COLOUR : '00FFFF',
		HIGHLIGHTER_DEFAULT_OPACITY : 0.5,

		STICKY_NOTE_DEFAULT_WIDTH : 280,
		STICKY_NOTE_DEFAULT_HEIGHT : 180,
		STICKY_NOTE_DEFAULT_COLOUR : 'yellow',
		STICKY_NOTE_DEFAULT_OPACITY : 1,

		STAMP_DEFAULT_WIDTH : 150,
		STAMP_DEFAULT_HEIGHT : 70,
		STAMP_PREVIEW_WIDTH : 70,
		STAMP_PREVIEW_HEIGHT : 18,

		ANNOTATION_MIN_SIZE : 30,

		// Page navigation

		PAGE_CHANGE_EVENT : "PAGE_CHANGED",
		PAGE_NAVIGATION_EVENT : "PAGE_NAVIGATION_TRIGGERED",
		ZOOM_IN_EVENT : "ZOOM_IN",
		ZOOM_OUT_EVENT : "ZOOM_OUT",
		ZOOM_LEVEL_CHANGE_EVENT : "ZOOM_LEVEL_CHANGED",
		ROTATE_EVENT : "ROTATE",

		ORIENTATION_NORTH : 'N',
		ORIENTATION_EAST : 'E',
		ORIENTATION_WEST : 'W',
		ORIENTATION_SOUTH : 'S',

		// Commands

		CREATE_COMMAND : "CREATE_COMMAND",
		RENAME_COMMAND : "RENAME_COMMAND",
		UPDATE_COMMAND : "UPDATE_COMMAND",
		UPDATE_GEOMETRY_COMMAND : "UPDATE_GEOMETRY_COMMAND",
		DELETE_COMMAND : "DELETE_COMMAND",
		REQUEST_JOIN_COMMAND : "REQUEST_JOIN_COMMAND",
		CONFIRM_JOIN_COMMAND : "CONFIRM_JOIN_COMMAND",
		SUBMIT_CHAT_MESSAGE_COMMAND : "SUBMIT_CHAT_MESSAGE_COMMAND",
		ACCEPT_INVITE_COMMAND : "ACCEPT_INVITE_COMMAND",
		DECLINE_INVITE_COMMAND : "DECLINE_INVITE_COMMAND",
		FETCH_LISTS_COMMAND : "FETCH_LISTS_COMMAND",
		UPDATE_OWNER : "UPDATE_OWNER",
		UPDATE_INVITED_USERS_COMMAND : "UPDATE_INVITED_USERS_COMMAND",
		CHANGE_USER_PROFILE_COMMAND : "CHANGE_USER_PROFILE_COMMAND",

		// User

		BUSINESS_ANALYST_ROLE : "BusinessAnalyst",
		INTEGRATOR_ROLE : "Integrator",

		// Web-service
		DYNAMICALLY_BOUND_SERVICE : "Dynamically bound Service",

		//General
		NEW_LINE : 	"\n",
		EXTERNAL_REFERENCE_PROPERTY : "externalReference",
		VIEW_ICON_PARAM_KEY : "viewIcon",
		DEFAULT_TEXT_WIDTH : 50,
		DEFAULT_TEXT_HEIGHT : 15,
		ADMIN_ROLE_ID : "Administrator",
		EXTERNAL_SCHEMA_CLASSIFIER_TOKEN : "ExternalReference"

	};
});