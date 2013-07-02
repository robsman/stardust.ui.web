/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([], function() {
	return {
		applicationType : [ {
			id : "interactive",
			readableName: "UI Mashup",
			iconPath: "plugins/bpm-modeler/images/icons/application-c-ext-web.png",
			viewId: "uiMashupApplicationView"
		}, {
			id : "webservice",
			readableName: "Web Service",
			iconPath: "plugins/bpm-modeler/images/icons/application-web-service.png",
			viewId: "webServiceApplicationView"
		}, {
			id : "messageTransformationBean",
			readableName: "Message Transformation Application",
			iconPath: "plugins/bpm-modeler/images/icons/application-message-trans.png",
			viewId: "messageTransformationApplicationView"
		}, {
			id : "camelSpringProducerApplication",
			readableName: "Camel Application",
			iconPath: "plugins/bpm-modeler/images/icons/application-camel.png",
			viewId: "camelApplicationView"
		}, {
			id : "plainJava",
			readableName: "Plain Java Application",
			iconPath: "plugins/bpm-modeler/images/icons/application-plain-java.png",
			viewId: "genericApplicationView"
		}, {
			id : "rulesEngineBean",
			readableName: "Business Rules Application",
			iconPath: "plugins/bpm-modeler/images/icons/application-drools.png",
			viewId: "genericApplicationView"
		}, {
			id : "dmsOperation",
			readableName: "Document Management Application",
			iconPath: "plugins/bpm-modeler/images/icons/application-plain-java.png",
			viewId: "genericApplicationView"
		}, {
			id : "jms",
			readableName: "JMS Application",
			iconPath: "plugins/bpm-modeler/images/icons/application-plain-java.png",
			viewId: "genericApplicationView"
		}, {
			id : "mailBean",
			readableName: "Mail Application",
			iconPath: "plugins/bpm-modeler/images/icons/application-plain-java.png",
			viewId: "genericApplicationView"
		}, {
			id : "messageParsingBean",
			readableName: "Message Parsing Application",
			iconPath: "plugins/bpm-modeler/images/icons/application-plain-java.png",
			viewId: "genericApplicationView"
		}, {
			id : "messageSerializationBean",
			readableName: "Message Serialization Application",
			iconPath: "plugins/bpm-modeler/images/icons/application-plain-java.png",
			viewId: "genericApplicationView"
		}, {
			id : "sessionBean",
			readableName: "EJB Session Bean Application",
			iconPath: "plugins/bpm-modeler/images/icons/application-plain-java.png",
			viewId: "genericApplicationView"
		}, {
			id : "springBean",
			readableName: "Spring Bean Application",
			iconPath: "plugins/bpm-modeler/images/icons/application-drools.png",
			viewId: "genericApplicationView"
		}, {
			id : "xslMessageTransformationBean",
			readableName: "XSL Message Transformation Application",
			iconPath: "plugins/bpm-modeler/images/icons/application-drools.png",
			viewId: "genericApplicationView"
		} ]
	};
});