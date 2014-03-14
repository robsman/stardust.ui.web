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
 * Modeler specific utility functions.
 *
 * @author shrikant.gangal
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_model", "bpm-modeler/js/m_jsfViewManager" ],
		function(m_utils, m_model, m_jsfViewManager) {
			var SCROLL_PANE_ID = "scrollpane";
			var viewManager = m_jsfViewManager.create();
			var appTypeViewIdMap = {
				"messageTransformationBean" : "messageTransformationApplicationView",
				"webservice" : "webServiceApplicationView",
				"camelSpringProducerApplication" : "camelApplicationView",
				"interactive" : "uiMashupApplicationView"
			};

			return {
				getModelerScrollPosition : function() {
					return getScrollpaneScrollPosition();
				},

				closeAllModelerViews : closeAllModelerViews,

				disableToolbarControl : disableToolbarControl,

				enableToolbarControl : enableToolbarControl,

				getUniqueNameForElement : getUniqueNameForElement,

				openApplicationView : openApplicationView,

				getViewIdForApplicationType : getViewIdForApplicationType,

				openParticipantView : openParticipantView,

				openTypeDeclarationView : openTypeDeclarationView,

				fixDivTop : fixDivTop
			};

			function getScrollpaneScrollPosition() {
				var scrollPane = m_utils.jQuerySelect("#" + SCROLL_PANE_ID).get(0);
				if (scrollPane) {
					return {
						top : scrollPane.scrollTop,
						left : scrollPane.scrollLeft
					}
				}
			}

			/**
			 * Fire close views for all models so that only
			 * modeler related views will be closed.
			 */
			function closeAllModelerViews() {

				var models = m_model.getModels();
				for (var i in models) {
					viewManager.closeViewsForElement(models[i].uuid);
				}
			}

			function disableToolbarControl(input) {
				input.attr("disabled", true);
				input.fadeTo(0, 0.5);
				input.removeClass("toolbarButton");
				input.css("cursor", "default");
				input.css("background", "none");
				input.css("border", "none");
			}

			function enableToolbarControl(input) {
				input.removeAttr("disabled");
				input.css("background", "");
				input.css("border", "");
				input.fadeTo(0, 1);
				input.css("cursor", "pointer");
				input.addClass("toolbarButton");
			}

			/**
			 *
			 */
			function getUniqueNameForElement(modelId, namePrefix) {
				var suffix = 0;
				var name = namePrefix + " " + (++suffix);
				var model = m_model.findModel(modelId);
				if (model) {
					while (model.findModelElementByName(name)) {
						var name = namePrefix + " " + (++suffix);
					}
				}

				return name;
			}

			/**
			 *
			 */
			function openApplicationView(application) {
				if (application) {
					var model = application.model;
					viewManager.openView(
							getViewIdForApplicationType(application.applicationType),
							"modelId="
									+ encodeURIComponent(model.id)
									+ "&applicationId="
									+ encodeURIComponent(application.id)
									+ "&applicationName="
									+ encodeURIComponent(application.name)
									+ "&fullId="
									+ encodeURIComponent(application
											.getFullId())
									+ "&uuid="
									+ application.uuid
									+ "&modelUUID="
									+ model.uuid,
							application.uuid);
				}
			};

			/**
			 *
			 */
			function getViewIdForApplicationType(appType) {
				if (appType) {
					var viewId = appTypeViewIdMap[appType];
					return viewId ? viewId : "genericApplicationView";
				}
			};

			/**
			 *
			 */
			function openParticipantView(participant) {
				if (participant) {
					var paramPrefix = participant.type.substring(0, participant.type.indexOf("Participant"));
					var model = participant.model;
					viewManager.openView(
							paramPrefix + "View",
								paramPrefix + "Id="
										+ encodeURIComponent(participant.id)
										+ "&modelId="
										+ encodeURIComponent(model.id)
										+ "&" + paramPrefix + "Name="
										+ encodeURIComponent(participant.name)
										+ "&fullId="
										+ encodeURIComponent(participant
												.getFullId())
										+ "&uuid="
										+ participant.uuid
										+ "&modelUUID="
										+ model.uuid,
										participant.uuid);
				}
			};

			function openTypeDeclarationView(structuredDataType) {
				if (structuredDataType) {
					var model = structuredDataType.model;
					viewManager.openView(
							"xsdStructuredDataTypeView",
							"modelId="
									+ encodeURIComponent(model.id)
									+ "&structuredDataTypeId="
									+ encodeURIComponent(structuredDataType.id)
									+ "&structuredDataTypeName="
									+ encodeURIComponent(structuredDataType.name)
									+ "&fullId="
									+ encodeURIComponent(structuredDataType
											.getFullId())
									+ "&uuid="
									+ structuredDataType.uuid
									+ "&modelUUID="
									+ model.uuid,
							structuredDataType.uuid);
				}
			};

			function fixDivTop(jDiv) {
				$(window).scroll(function() {
				    if ($(window).scrollTop() > jDiv[0].scrollTop) {
				    	jDiv.css({'position': 'fixed', 'top': '0', 'width': '100%'});
				    }
				    else {
				    	jDiv.css({'position': 'static', 'top': 'auto', 'width': '100%'});
				    }
				});
			}
		});
