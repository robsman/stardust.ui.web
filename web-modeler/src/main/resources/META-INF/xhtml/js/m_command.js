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
		createCreateCommand : function(path, object) {
			return new ChangeEvent(m_constants.CREATE_COMMAND, path, null, null, object);
		},
		createDeleteCommand : function(path, object) {
			return new ChangeEvent(m_constants.DELETE_COMMAND, path, null, object, null);
		},
		createUpdateCommand : function(path, oldObject, newObject) {
			return new ChangeEvent(m_constants.UPDATE_COMMAND, path, null, oldObject, newObject);
		},
		createRenameCommand : function(path, oldObject, newObject) {
			return new ChangeEvent(m_constants.RENAME_COMMAND, path, "rename", oldObject, newObject);
		},
		createChangeGeometryCommand : function(path, oldObject, newObject, modelElement) {
			return new ChangeEvent(m_constants.UPDATE_GEOMETRY_COMMAND, path, null, oldObject, newObject, modelElement);
		},				
		createMoveNodeSymbolCommand : function(baseUri, targetElement, newObject, context) {
			return new ChangeDescriptor("nodeSymbol.move", [{oid: targetElement.oid, changes: newObject}], context);
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
		},				
		patchRenamePath: function(renameCommand)
		{
			var path = renameCommand.path;
			var steps = path.split("/");
			var newPath = "";
			
			for (var n = 0; n < steps.length - 1; ++n)
				{
				newPath += "/";
				newPath += steps[n];
				}
			
			newPath += "/";
			newPath += renameCommand.newObject.id;
			
			renameCommand.path = newPath;
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

	function ChangeDescriptor(commandId, changeDescriptions, context) {
		this.account = m_user.getCurrentUser().account;
		this.timestamp = new Date();
		this.commandId = commandId;
		this.changeDescriptions = changeDescriptions;

		this.path = "/sessions/" + context.diagram.model.id + "/" + context.diagram.process.id + "/changes";

		/**
		 *
		 */
		ChangeDescriptor.prototype.toString = function() {
			return "Lightdust.ChangeDescriptor";
		};
	}

});