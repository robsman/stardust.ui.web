/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "m_utils", "m_constants", "m_user" ], function(m_utils, m_constants, m_user) {

	var RETRIEVE = "RETRIEVE";
	var CREATE = "CREATE";
	var UPDATE = "UPDATE";
	var DELETE = "DELETE";

	return {
		RETRIEVE : RETRIEVE,
		CREATE : CREATE,
		UPDATE : UPDATE,
		DELETE : DELETE,

		// @deprecated
		createCommand : function(url, data) {
			return new Command(url, data, CREATE);
		},
		// @deprecated
		createRetrieveCommand : function(url, data) {
			return new Command(url, data, RETRIEVE);
		},
		// @deprecated
		createCreateCommand : function(path, object) {
			return new ChangeEvent(m_constants.CREATE_COMMAND, path, null, null, object);
		},
		// @deprecated
		createDeleteCommand : function(path, object) {
			return new ChangeEvent(m_constants.DELETE_COMMAND, path, null, object, null);
		},
		// @deprecated
		createUpdateCommand : function(path, oldObject, newObject) {
			return new ChangeEvent(m_constants.UPDATE_COMMAND, path, null, oldObject, newObject);
		},
		// @deprecated
		createRenameCommand : function(path, oldObject, newObject) {
			return new ChangeEvent(m_constants.RENAME_COMMAND, path, "rename", oldObject, newObject);
		},
		// @deprecated
		createChangeGeometryCommand : function(path, oldObject, newObject, modelElement) {
			return new ChangeEvent(m_constants.UPDATE_GEOMETRY_COMMAND, path, null, oldObject, newObject, modelElement);
		},
		createCreateProcessCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("process.create", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createMoveNodeSymbolCommand : function(baseUri, targetElement, newObject, context) {
			// TODO This code needs be removed as soon modelId passed instead of context (see createUpdateModelElementCommand) 
			var modelId = context.diagram.model.id;

			return new ChangeDescriptor("nodeSymbol.move", modelId, [{oid: targetElement.oid, changes: newObject}]);
		},
		createCreateNodeCommand : function(commandType, baseUri, targetElement, newObject, context) {
			// TODO This code needs be removed as soon modelId passed instead of context (see createUpdateModelElementCommand) 
			var modelId = context.diagram.model.id;

			return new ChangeDescriptor(commandType, modelId, [{oid: targetElement.oid, changes: newObject}]);
		},
		createUpdateModelElementCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("modelElement.update", modelId, [{oid: oid, changes: changes}]);
		},
		createRemoveNodeCommand : function(commandType, baseUri, targetElement, newObject, context) {
			// TODO This code needs be removed as soon modelId passed instead of context (see createUpdateModelElementCommand) 
			var modelId = context.diagram.model.id;

			return new ChangeDescriptor(commandType, modelId, [{oid: targetElement.oid, changes: newObject}]);
		},
		// TODO Might be simple Request causing command to be broadcasted
		createRequestJoinCommand : function(prospect) {
			return new ChangeEvent(m_constants.REQUEST_JOIN_COMMAND, "/users", "requestJoin", null, prospect);
		},
		createConfirmJoinCommand : function(participant) {
			return new ChangeEvent(m_constants.CONFIRM_JOIN_COMMAND, "/users", "confirmJoin", participant, null);
		},
		createSubmitChatMessageCommand : function(message) {
			return new ChangeEvent(m_constants.SUBMIT_CHAT_MESSAGE_COMMAND, "/users", "submitChatMessage", null, message);
		}
	};

	/**
	 *
	 */
	function Command(url, data, type) {
		m_utils.debug("===> Url: " + url);
		m_utils.debug("===> Type: " + type);
		m_utils.debug("===> Data:\n");
		m_utils.debug(data);

		this.url = url;
		this.data = JSON.stringify(data);
		this.type = type;

		/**
		 *
		 */
		Command.prototype.toString = function() {
			return "Lightdust.Command";
		};
	}

	// TODO Merge
	/**
	 *
	 */
	function ChangeEvent(type, path, operation, oldObject, newObject, modelElement) {
		this.account = m_user.getCurrentUser().account;
		this.timestamp = new Date();
		this.type = type;
		this.path = path;
		this.operation = operation;
		this.oldObject = oldObject;
		this.newObject = newObject;

		if (modelElement) {
			this.modelElement = modelElement;
		}

		/**
		 *
		 */
		ChangeEvent.prototype.toString = function() {
			return "Lightdust.ChangeEvent";
		};
	}

	function ChangeDescriptor(commandId, modelId, changeDescriptions) {
		this.account = m_user.getCurrentUser().account;
		this.timestamp = new Date();
		this.commandId = commandId;
		this.modelId = modelId;
		this.changeDescriptions = changeDescriptions;

		this.path = "/sessions/changes";

		/**
		 *
		 */
		ChangeDescriptor.prototype.toString = function() {
			return "Lightdust.ChangeDescriptor";
		};
	}

});