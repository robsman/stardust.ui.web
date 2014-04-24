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
define(["bpm-modeler/js/m_utils"], function(m_utils) {
	return {
		openView : function(modelerLinkId, formId, viewId, viewParams, viewIdentity) {
			var portalWinDoc = m_utils.getOutlineWindowAndDocument();

			var link = portalWinDoc.doc.getElementById(modelerLinkId);
			var linkForm = portalWinDoc.win.contentWindow.formOf(link);

			linkForm[formId + ':_idcl'].value = modelerLinkId;
			linkForm['viewParams'].value = viewParams;
			linkForm['viewId'].value = viewId;
			linkForm['viewIdentity'].value = viewIdentity;

			portalWinDoc.win.contentWindow.iceSubmit(linkForm, link);
		},

		openModelDeploymentDialog : function(modelDeployerLinkId, modelFileName, modleFilePath, formId) {
			var portalWinDoc = m_utils.getOutlineWindowAndDocument();

			var link = portalWinDoc.doc.getElementById(modelDeployerLinkId);
			var linkForm = portalWinDoc.win.contentWindow.formOf(link);

			linkForm[formId + ':_idcl'].value = modelDeployerLinkId;
			linkForm['allowBrowse'].value = "false";
			linkForm['fromlaunchPanels'].value = "true";
			linkForm['fileName'].value = modelFileName;
			linkForm['filePath'].value = modleFilePath;

			portalWinDoc.win.contentWindow.iceSubmit(linkForm, link);
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
		updateView : function(modelerLinkId, formId, viewId, viewParams, viewIdentity) {
			var portalWinDoc = m_utils.getOutlineWindowAndDocument();

			var link = portalWinDoc.doc.getElementById(modelerLinkId);
			var linkForm = portalWinDoc.win.contentWindow.formOf(link);

			linkForm[formId + ':_idcl'].value = modelerLinkId;
			linkForm['viewParams'].value = viewParams;
			linkForm['viewId'].value = viewId;
			linkForm['viewIdentity'].value = viewIdentity;

			portalWinDoc.win.contentWindow.iceSubmit(linkForm, link);
		},

		/**
		 *
		 * @param modelerLinkId
		 * @param formId
		 * @param uuid
		 * @returns
		 */
		closeView : function(modelerLinkId, formId, uuid) {
			if(!modelerLinkId)
			{
				m_utils.debug("modelerLinkId is null");
				return;
			}
			if(!formId)
			{
				m_utils.debug("formId is null");
				return;
			}
			var portalWinDoc = m_utils.getOutlineWindowAndDocument();

			var link = portalWinDoc.doc.getElementById(modelerLinkId);
			var linkForm = portalWinDoc.win.contentWindow.formOf(link);

			linkForm[formId + ':_idcl'].value = modelerLinkId;
			linkForm['uuid'].value = uuid;

			portalWinDoc.win.contentWindow.iceSubmit(linkForm, link);
		},

		/**
		 *
		 * @param modelerLinkId
		 * @param formId
		 * @returns
		 */
		openImportModelDialog : function(modelerLinkId, formId) {
			var portalWinDoc = m_utils.getOutlineWindowAndDocument();

			var link = portalWinDoc.doc.getElementById(modelerLinkId);
			var linkForm = portalWinDoc.win.contentWindow.formOf(link);

			linkForm['fromlaunchPanels'].value = "true";
			linkForm[formId + ':_idcl'].value = modelerLinkId;
			portalWinDoc.win.contentWindow.iceSubmit(linkForm, link);
		}
	};
});
