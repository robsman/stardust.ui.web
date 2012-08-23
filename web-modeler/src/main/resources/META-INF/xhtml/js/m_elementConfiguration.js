/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "m_utils" ], function(m_utils) {

	var supportedDataTypes = [ "primitive", "dmsDocument", "struct" ];

	var unSupportedDataTypes = [ "entity", "serializable", "dmsDocumentList",
			"dmsFolder", "hibernate" ];

	var validDataTypes = supportedDataTypes.concat(unSupportedDataTypes);

	var supportedAppTypes = [ "webservice", "messageTransformationBean",
			"camelBean", "interactive" ];

	var unSupportedAppTypes = [ "dmsOperation", "messageParsingBean",
			"messageSerializationBean", "springBean", "mailBean", "jms",
			"plainJava", "xslMessageTransformationBean", "rulesEngineBean",
			"sessionBean", "camelSpringProducerApplication" ];

	var validAppTypes = supportedAppTypes.concat(unSupportedAppTypes);

	var supportedParticipants = [ "roleParticipant", "organizationParticipant" ];

	var unSupportedParticipants = [ "conditionalPerformerParticipant" ];

	var validParticipants = supportedParticipants
			.concat(unSupportedParticipants);

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