/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_user" ], function(m_utils, m_constants, m_user) {

	var RETRIEVE = "RETRIEVE";
	var CREATE = "CREATE";
	var UPDATE = "UPDATE";
	var DELETE = "DELETE";

	return {
		RETRIEVE : RETRIEVE,
		CREATE : CREATE,
		UPDATE : UPDATE,
		DELETE : DELETE,

		createCreateCommand : function(command, modelId, oid, changes) {
			return new ChangeDescriptor(command, modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createCreateProcessCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("process.create", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createDeleteProcessCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("process.delete", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createCreatePrimitiveDataCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("primitiveData.create", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createCreateDocumentDataCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("documentData.create", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createCreateTypeDeclarationCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("typeDeclaration.create", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createCreateStructuredDataCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("structuredData.create", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createDeleteDataCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("data.delete", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createCreateRoleCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("role.create", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createCreateConditionalPerformerCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("conditionalPerformer.create", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createCreateOrganizationCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("organization.create", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createUpdateTeamLeaderCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("organization.updateTeamLeader", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createDeleteParticipantCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("participant.delete", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createCreateWebServiceAppCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("webServiceApplication.create", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createCreateMessageTransfromationAppCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("messageTransformationApplication.create", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createCreateCamelAppCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("camelApplication.create", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createCreateUiMashupAppCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("uiMashupApplication.create", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createDeleteApplicationCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("application.delete", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createCreateModelCommand : function(changes) {
			return new ChangeDescriptor("model.create", undefined, [ {
				changes : changes
			} ]);
		},
		createUpdateModelLockStatusCommand : function(uuid, modelId, changes) {
			return new ChangeDescriptor("modelLockStatus.update", modelId, [ {
				uuid : uuid,
				changes : changes
			} ]);
		},
		createUpdateModelCommand : function(uuid, modelId, changes) {
			return new ChangeDescriptor("model.update", modelId, [ {
				uuid : uuid,
				changes : changes
			} ]);
		},
		createDeleteModelCommand : function(uuid, modelId, changes) {
			return new ChangeDescriptor("model.delete", modelId, [ {
				uuid : uuid,
				changes : changes
			} ]);
		},
		createCreateStructuredDataTypeCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("structuredDataType.create", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createDeleteStructuredDataTypeCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("structuredDataType.delete", modelId, [ {
				oid : oid,
				changes : changes
			} ]);
		},
		createMoveNodeSymbolCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("nodeSymbol.move", modelId, [{oid: oid, changes: changes}]);
		},
		createCreateNodeCommand : function(commandType, modelId, oid, changes) {
			return new ChangeDescriptor(commandType, modelId, [{oid: oid, changes: changes}]);
		},
		createUpdateModelElementCommand : function(modelId, oid, changes) {
			return new ChangeDescriptor("modelElement.update", modelId, [{oid: oid, changes: changes}]);
		},
		createUpdateDiagramCommand : function(modelId, changeDescriptions) {
			return new ChangeDescriptor("modelElement.update", modelId,
					changeDescriptions);
		},
		//TODO: temporary - later all commands will accept OID and/or UUID.
		createUpdateModelElementWithUUIDCommand : function(modelId, uuid, changes) {
			return new ChangeDescriptor("modelElement.update", modelId, [{uuid: uuid, changes: changes}]);
		},
		createRemoveNodeCommand : function(commandType, modelId, oid, changes) {
			return new ChangeDescriptor(commandType, modelId, [{oid: oid, changes: changes}]);
		},

		createAcceptInvite: function(oldObject, prospect) {
									//type,                            path,      operation,  oldObject, newObject, modelElement
			return new ChangeEvent(m_constants.ACCEPT_INVITE_COMMAND, "/users", "acceptInvite", oldObject, prospect);
		},
		createDeclineInvite: function(oldObject, prospect) {
			return new ChangeEvent(m_constants.DECLINE_INVITE_COMMAND, "/users", "declineInvite", oldObject, prospect);
		},

		createFetchOwner : function(owner){
			return new ChangeEvent(m_constants.UPDATE_OWNER, "/users", "updateOwner", owner, null);
		},

		createFetchProspects : function(owner){
			return new ChangeEvent(m_constants.UPDATE_INVITED_USERS_COMMAND, "/users", "getAllProspects", owner, null);
		},

		createFetchCollaborators : function(owner){
			return new ChangeEvent(m_constants.UPDATE_INVITED_USERS_COMMAND, "/users", "getAllCollaborators", owner, null);
		},
		createRequestJoinCommand : function(prospect) {
			return new ChangeEvent(m_constants.REQUEST_JOIN_COMMAND, "/users", "requestJoin", null, prospect);
		},
		createConfirmJoinCommand : function(participant) {
			return new ChangeEvent(m_constants.CONFIRM_JOIN_COMMAND, "/users", "confirmJoin", participant, null);
		},
		createSubmitChatMessageCommand : function(message) {
			return new ChangeEvent(m_constants.SUBMIT_CHAT_MESSAGE_COMMAND, "/users", "submitChatMessage", null, message);
		},
		createUserProfileChangeCommand : function(profile) {
			return new ChangeEvent(m_constants.CHANGE_USER_PROFILE_COMMAND, profile);
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