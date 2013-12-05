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
 * @author Shrikant.Gangal
 *
 * The ModelsSaveStatus class is a signleton which at present does nothng more
 * than holding s global property indicating the saved status of models. This
 * can further be extended to hold per model status.
 *
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants" ],
		function(m_utils, m_constants) {

			return {
				setModelsSaved : function() {
					setModelsSavedStatus(true);
				},

				setModelsModified : function() {
					setModelsSavedStatus(false);
				},

				areModelsSaved : areModelsSaved
			};

			function setModelsSavedStatus(saveStatus) {
				getModelsSaveStatus().modelsSaved = saveStatus;
			}
			;

			function areModelsSaved() {
				return getModelsSaveStatus().modelsSaved;
			}

			function getModelsSaveStatus() {
				if (!window.top.modelerGlobalObjects) {
					window.top.modelerGlobalObjects = {};
				}

				if (!window.top.modelerGlobalObjects.modelsSaveStatus) {
					window.top.modelerGlobalObjects.modelsSaveStatus = new ModelsSaveStatus();
				}

				return window.top.modelerGlobalObjects.modelsSaveStatus;
			}
			;

			// TODO - save and update per-model model status
			function ModelsSaveStatus() {
				this.modelsSaved = true;
			}
			;
		});