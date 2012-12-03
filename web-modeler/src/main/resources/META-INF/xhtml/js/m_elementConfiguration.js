/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "bpm-modeler/js/m_utils" ], function(m_utils) {

	var supportedDataTypes = [ "primitive", "dmsDocument", "struct" ];

	var unSupportedDataTypes = [ "entity", "serializable", "dmsDocumentList",
			"dmsFolder", "dmsFolderList", "hibernate", "plainXML" ];

	var validDataTypes = supportedDataTypes.concat(unSupportedDataTypes);

	var supportedAppTypes = [ "webservice", "messageTransformationBean",
			"camelBean", "interactive" ];

	var unSupportedAppTypes = [ "dmsOperation", "messageParsingBean",
			"messageSerializationBean", "springBean", "mailBean", "jms",
			"plainJava", "xslMessageTransformationBean", "rulesEngineBean",
			"sessionBean", "camelSpringProducerApplication" ];

	var validAppTypes = supportedAppTypes.concat(unSupportedAppTypes);

	var supportedParticipants = [ "roleParticipant", "teamLeader", "organizationParticipant" ];

	var unSupportedParticipants = [ "conditionalPerformerParticipant" ];

	var validParticipants = supportedParticipants
			.concat(unSupportedParticipants);

	var elementTypeVsIconsMap = {
			"primitive" : "/plugins/bpm-modeler/images/icons/data-primitive.png",
			"dmsDocument" : "/plugins/bpm-modeler/images/icons/data-document.png",
			"struct" : "/plugins/bpm-modeler/images/icons/data-structured.png",
			"entity" : "/plugins/bpm-modeler/images/icons/data-entity.png",
			"serializable" : "/plugins/bpm-modeler/images/icons/data-serializable.png",
			"dmsDocumentList" : "/plugins/bpm-modeler/images/icons/data-document-list.png",
			"dmsFolder" : "/plugins/bpm-modeler/images/icons/data-folder.png",
			"dmsFolderList" : "/plugins/bpm-modeler/images/icons/data-folder-list.png",
			"plainXML" : "/plugins/bpm-modeler/images/icons/data-xml.png",
			"hibernate" : "/plugins/bpm-modeler/images/icons/data-hibernate.png",
			"webservice" : "/plugins/bpm-modeler/images/icons/application-web-service.png",
			"messageTransformationBean" : "/plugins/bpm-modeler/images/icons/application-message-trans.png",
			"camelBean" : "/plugins/bpm-modeler/images/icons/application-camel.png",
			"interactive" : "/plugins/bpm-modeler/images/icons/applications.png",
			"dmsOperation" : "/plugins/bpm-modeler/images/icons/application-dms.png",
			"messageParsingBean" : "/plugins/bpm-modeler/images/icons/application-message-p.png",
			"messageSerializationBean" : "/plugins/bpm-modeler/images/icons/application-message-s.png",
			"springBean" : "/plugins/bpm-modeler/images/icons/applications.png",
			"mailBean" : "/plugins/bpm-modeler/images/icons/application-mail.png",
			"jms" : "/plugins/bpm-modeler/images/icons/application-jms.png",
			"plainJava" : "/plugins/bpm-modeler/images/icons/application-plain-java.png",
			"xslMessageTransformationBean" : "/plugins/bpm-modeler/images/icons/",
			"rulesEngineBean" : "/plugins/bpm-modeler/images/icons/application-drools.png",
			"sessionBean" : "/plugins/bpm-modeler/images/icons/application-session.png",
			"camelSpringProducerApplication" : "/plugins/bpm-modeler/images/icons/application-camel.png",
			"roleParticipant" : "/plugins/bpm-modeler/images/icons/role.png",
			"teamLeader" : "/plugins/bpm-modeler/images/icons/manager.png",
			"organizationParticipant" : "/plugins/bpm-modeler/images/icons/organization.png",
			"conditionalPerformerParticipant" : "/plugins/bpm-modeler/images/icons/conditional.png"
	};

	return {
		getValidDataTypes : function() {
			return validDataTypes;
		},

		getSupportedDataTypes : function() {
			return supportedDataTypes;
		},

		getUnSupportedDataTypes : function() {
			return unSupportedDataTypes;
		},

		getValidAppTypes : function() {
			return validAppTypes;
		},

		getSupportedAppTypes : function() {
			return supportedAppTypes;
		},

		getUnSupportedAppTypes : function() {
			return unSupportedAppTypes;
		},

		getValidParticipants : function() {
			return validParticipants;
		},

		getSupportedParticipants : function() {
			return supportedParticipants;
		},

		getUnSupportedParticipants : function() {
			return unSupportedParticipants;
		},

		getIconForElementType : function(elementType) {
			return elementTypeVsIconsMap[elementType];
		},

		isSupportedDataType : function(dataType) {
			return (-1 != jQuery.inArray(dataType, supportedDataTypes));
		},

		isValidDataType : function(dataType) {
			return (-1 != jQuery.inArray(dataType, validDataTypes));
		},

		isValidAppType : function(appType) {
			return (-1 != jQuery.inArray(appType, validAppTypes));
		},

		isUnSupportedAppType : function(appType) {
			return (-1 != jQuery.inArray(appType, unSupportedAppTypes));
		}
	};
});