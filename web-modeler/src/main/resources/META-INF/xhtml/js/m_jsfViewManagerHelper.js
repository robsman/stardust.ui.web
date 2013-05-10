/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * Helper functions assist in handling jsf views.
 *
 * @author Yogesh.Manware
 */
define([], function() {
	return {
		openView : function(modelerLinkId, formId, viewId, viewParams,
				viewIdentity) {

			var link = getIPPPortalMainWindow().document
					.getElementById(modelerLinkId);

			var linkForm = getIPPPortalMainWindow().formOf(link);

			linkForm[formId + ':_idcl'].value = modelerLinkId;
			linkForm['viewParams'].value = viewParams;
			linkForm['viewId'].value = viewId;
			linkForm['viewIdentity'].value = viewIdentity;

			getIPPPortalMainWindow().iceSubmit(linkForm, link);
		},

		openModelDeploymentDialog : function(modelDeployerLinkId,
				modelFileName, modleFilePath, formId) {

			var link = getIPPPortalMainWindow().document
					.getElementById(modelDeployerLinkId);

			// formOf and iceSubmit are javascript functions provided by
			// icefaces
			var linkForm = getIPPPortalMainWindow().formOf(link);

			linkForm[formId + ':_idcl'].value = modelDeployerLinkId;
			linkForm['allowBrowse'].value = "false";
			linkForm['fileName'].value = modelFileName;
			linkForm['filePath'].value = modleFilePath;

			getIPPPortalMainWindow().iceSubmit(linkForm, link);
		},

		/**
		 * TODO: This method appears to be same as OpenView, do we require this?
		 *
		 * @param modelerLinkId
		 * @param formId
		 * @param viewId
		 * @param viewParams
		 * @param viewIdentity
		 * @returns
		 */
		updateView : function(modelerLinkId, formId, viewId, viewParams,
				viewIdentity) {
			var link = getIPPPortalMainWindow().document
					.getElementById(modelerLinkId);

			var linkForm = getIPPPortalMainWindow().formOf(link);

			linkForm[formId + ':_idcl'].value = modelerLinkId;
			linkForm['viewParams'].value = viewParams;
			linkForm['viewId'].value = viewId;
			linkForm['viewIdentity'].value = viewIdentity;

			getIPPPortalMainWindow().iceSubmit(linkForm, link);
		},

		/**
		 *
		 * @param modelerLinkId
		 * @param formId
		 * @param uuid
		 * @returns
		 */
		closeView : function(modelerLinkId, formId, uuid) {
			var link = getIPPPortalMainWindow().document
					.getElementById(modelerLinkId);

			// formOf and iceSubmit are javascript functions provided by
			// icefaces
			var linkForm = getIPPPortalMainWindow().formOf(link);

			linkForm[formId + ':_idcl'].value = modelerLinkId;
			linkForm['uuid'].value = uuid;

			getIPPPortalMainWindow().iceSubmit(linkForm, link);
		},

		/**
		 *
		 * @param modelerLinkId
		 * @param formId
		 * @returns
		 */
		openImportModelDialog : function(modelerLinkId, formId) {
			var link = getIPPPortalMainWindow().document
					.getElementById(modelerLinkId);

			// formOf and iceSubmit are javascript functions provided by
			// icefaces
			var linkForm = getIPPPortalMainWindow().formOf(link);

			linkForm[formId + ':_idcl'].value = modelerLinkId;
			getIPPPortalMainWindow().iceSubmit(linkForm, link);
		},
	};

	function getIPPPortalMainWindow() {
		return window.top.frames['ippPortalMain'];
	}
});
