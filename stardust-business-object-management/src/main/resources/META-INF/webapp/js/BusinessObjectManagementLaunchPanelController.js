/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define(
		[ "business-object-management/js/BusinessObjectManagementService" ],
		function(BusinessObjectManagementService) {
			return {
				create : function() {
					var controller = new BusinessObjectManagementLaunchPanelController();

					return controller;
				}
			};

			/**
			 * 
			 */
			function BusinessObjectManagementLaunchPanelController() {
				/**
				 * 
				 */
				BusinessObjectManagementLaunchPanelController.prototype.initialize = function() {
					var self = this;

					BusinessObjectManagementService
							.instance()
							.getBusinessObjects()
							.done(
									function(businessObjectModels) {
										self.businessObjectModels = businessObjectModels;

										self.refreshBusinessObjects();
										self.safeApply();
									}).fail(function() {
							});

					return this;
				};

				/**
				 * 
				 */
				BusinessObjectManagementLaunchPanelController.prototype.refreshBusinessObjects = function() {
					this.businessObjects = [];

					for (var n = 0; n < this.businessObjectModels.length; ++n) {
						for (var m = 0; m < this.businessObjectModels[n].businessObjects.length; ++m) {
							if (!this.businessObjectModels[n].businessObjects[m].types) {
								this.businessObjectModels[n].businessObjects[m].types = {};
							}

							this.businessObjectModels[n].businessObjects[m].modelOid = this.businessObjectModels[n].oid;
							this.businessObjectModels[n].businessObjects[m].label = this.businessObjectModels[n].name
									+ "/"
									+ this.businessObjectModels[n].businessObjects[m].name;
							this.businessObjects
									.push(this.businessObjectModels[n].businessObjects[m]);
						}
					}
				};

				/**
				 * 
				 */
				BusinessObjectManagementLaunchPanelController.prototype.openBusinessObjectManagementView = function(
						businessObject) {
					if (this.businessObject) {
						var queryString = "modelOid=" + businessObject.modelOid
								+ "&businessObjectId=" + businessObject.id
								+ "&businessObjectName=" + businessObject.label;

						this.openView("businessObjectManagementView",
								queryString, window.btoa(queryString));
					}
				};

				/**
				 * TODO - re-use a Util from web-modeler
				 */
				BusinessObjectManagementLaunchPanelController.prototype.openView = function(
						viewId, viewParams, viewIdentity) {
					var portalWinDoc = this.getOutlineWindowAndDocument();
					var link = jQuery("a[id $= 'view_management_link']",
							portalWinDoc.doc);
					var linkId = link.attr('id');
					var form = link.parents('form:first');
					var formId = form.attr('id');

					link = portalWinDoc.doc.getElementById(linkId);

					var linkForm = portalWinDoc.win.formOf(link);

					linkForm[formId + ':_idcl'].value = linkId;
					linkForm['viewParams'].value = viewParams;
					linkForm['viewId'].value = viewId;
					linkForm['viewIdentity'].value = viewIdentity;

					portalWinDoc.win.iceSubmit(linkForm, link);
				};

				/**
				 * TODO - re-use a Util from web-modeler
				 */
				BusinessObjectManagementLaunchPanelController.prototype.updateView = function(
						viewId, viewParams, viewIdentity) {
					var portalWinDoc = this.getOutlineWindowAndDocument();
					var link = jQuery("a[id $= 'view_updater_link']",
							portalWinDoc.doc);
					var linkId = link.attr('id');
					var form = link.parents('form:first');
					var formId = form.attr('id');

					link = portalWinDoc.doc.getElementById(linkId);

					var linkForm = portalWinDoc.win.formOf(link);

					linkForm[formId + ':_idcl'].value = linkId;
					linkForm['viewParams'].value = viewParams;
					linkForm['viewId'].value = viewId;
					linkForm['viewIdentity'].value = viewIdentity;

					portalWinDoc.win.iceSubmit(linkForm, link);
				};

				/*
				 * 
				 */
				BusinessObjectManagementLaunchPanelController.prototype.getOutlineWindowAndDocument = function() {
					return {
						win : parent.document
								.getElementById("portalLaunchPanels").contentWindow,
						doc : parent.document
								.getElementById("portalLaunchPanels").contentDocument
					};
				};

				/**
				 * 
				 */
				BusinessObjectManagementLaunchPanelController.prototype.safeApply = function(
						fn) {
					var phase = this.$root.$$phase;

					if (phase == '$apply' || phase == '$digest') {
						if (fn && (typeof (fn) === 'function')) {
							fn();
						}
					} else {
						this.$apply(fn);
					}
				};
			}
		});
