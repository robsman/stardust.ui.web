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
		dataType : [ {
			id : "serializable",
			readableName: "Serializable Java Class",
			iconPath: "plugins/bpm-modeler/images/icons/data-serializable.png"
		}, {
			id : "entity",
			readableName: "Entity Bean",
			iconPath: "plugins/bpm-modeler/images/icons/data-entity.png"
		}, {
			id : "dmsDocumentList",
			readableName: "Document List",
			iconPath: "plugins/bpm-modeler/images/icons/data-document-list.png"
		}, {
			id : "hibernate",
			readableName: "Hibernate Data",
			iconPath: "plugins/bpm-modeler/images/icons/data-hibernate.png"
		}, {
			id : "dmsFolder",
			readableName: "DMS Folder",
			iconPath: "plugins/bpm-modeler/images/icons/data-folder.png"
		}, {
			id : "dmsFolderList",
			readableName: "DMS Folder List",
			iconPath: "plugins/bpm-modeler/images/icons/data-folder-list.png"
		}, {
			id : "plainXML",
			readableName: "Plain XML",
			iconPath: "plugins/bpm-modeler/images/icons/data-xml.png"
		}, {
			id : "primitive",
			readableName: "Primitive",
			iconPath: "plugins/bpm-modeler/images/icons/data-primitive.png",
			supported: true
		}, {
			id : "dmsDocument",
			readableName: "DMS Document",
			iconPath: "plugins/bpm-modeler/images/icons/data-document.png",
			supported: true
		}, {
			id : "struct",
			readableName: "Structured Data",
			iconPath: "plugins/bpm-modeler/images/icons/data-structured.png",
			supported: true
		} ]
	};
});