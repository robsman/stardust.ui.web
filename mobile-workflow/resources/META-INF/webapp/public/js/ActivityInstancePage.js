/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ "js/Utils", "js/WorkflowService"],
		function(Utils, WorkflowService) {
			return {
				create : function(deck) {
					var page = new ActivityInstancePage();

					page.initialize(deck);

					return page;
				}
			};

			function ActivityInstancePage() {
				this.id = "activityInstancePage";

				/**
				 * 
				 */
				ActivityInstancePage.prototype.initialize = function(deck) {
					this.deck = deck;
				};

				/**
				 * 
				 */
				ActivityInstancePage.prototype.show = function() {
					var deferred = jQuery.Deferred();

					$("#" + this.id + " #titleHeader").empty();
					$("#" + this.id + " #titleHeader").append(
							this.activityInstance.activityName + " ("
									+ this.activityInstance.oid + ")");

					var self = this;

					WorkflowService
							.instance()
							.activateActivity(this.activityInstance)
							.done(
									function(activityInstance) {
										self.activityInstance = activityInstance;

										console.log("Activity Instance");
										console.log(self.activityInstance);

										if (activityInstance.activity.implementation === "manual") {
											// TODO Leverage new implementation
											// fro Manual Activities
										} else {
											self.externalWebAppUrl = self.activityInstance.activity.contexts.externalWebApp['carnot:engine:ui:externalWebApp:uri']
													+ "?ippDevice=mobile&ippInteractionUri="
													+ activityInstance.activity.contexts.externalWebApp.interactionId;
										}
										deferred.resolve();
									}).fail(function() {
								deferred.reject();
							});

					return deferred.promise();
				};

				/**
				 * 
				 */
				ActivityInstancePage.prototype.complete = function() {
					console.log("ActivityInstancePage.prototype.complete");

					var self = this;

					WorkflowService.instance().completeActivity(
							this.activityInstance).done(
							function(activityInstance) {
								self.deck.popPage();

								if (activityInstance != null) {
									self.deck
											.pushPage(new ActivityInstancePage(
													self.deck));
									console.log("New Activity Instance set");
								} else {
									console.log("Processing completed");
								}
							});
				};

				/**
				 * 
				 */
				ActivityInstancePage.prototype.openSuspendDialog = function() {
					$("#suspendConfirmationDialog").popup("open");
				};

				/**
				 * 
				 */
				ActivityInstancePage.prototype.suspendActivityInstance = function() {
					var self = this;

					WorkflowService.instance().suspendActivity(
							this.activityInstance).done(function() {
						self.closeSuspendDialog();
						self.deck.popPage();
					});
				};

				/**
				 * 
				 */
				ActivityInstancePage.prototype.closeSuspendDialog = function() {
					$("#suspendConfirmationDialog").popup("close");
				};

				/**
				 * 
				 */
				ActivityInstancePage.prototype.openNotesPage = function() {
					this.deck.notesPage.processInstanceOid = this.activityInstance.processInstanceOid;
					this.deck.pushPage(this.deck.notesPage);
				};

				/**
				 * 
				 */
				ActivityInstancePage.prototype.openProcessPage = function() {
					this.deck.processPage.processInstanceOid = this.activityInstance.processInstanceOid;
					this.deck.pushPage(this.deck.processPage);
				};
			}
		});
