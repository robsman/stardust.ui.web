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
		[ "js/Utils", "js/WorkflowService" ],
		function(Utils, WorkflowService) {
			return {
				create : function(deck) {
					var page = new WorklistPage();

					page.initialize(deck);

					return page;
				}
			};

			function WorklistPage() {
				this.id = "worklistPage";

				/**
				 * 
				 */
				WorklistPage.prototype.initialize = function(deck) {
					this.deck = deck;
				};

				/**
				 * 
				 */
				WorklistPage.prototype.show = function() {
					var deferred = jQuery.Deferred();

					var self = this;

					WorkflowService
							.instance()
							.getWorklist()
							.done(
									function(worklist) {
										self.worklist = [];

										for ( var n in worklist) {
											var activityInstance = worklist[n];

											var descriptors = "";
											var start = true;

											for ( var x in activityInstance.descriptors) {
												if (start) {
													start = false;
												} else {
													descriptors += ", ";
												}

												descriptors += x;
												descriptors += ": ";
												descriptors += activityInstance.descriptors[x] == null ? " -"
														: activityInstance.descriptors[x];
												descriptors += " ";
											}

											activityInstance.descriptors = descriptors;

											self.worklist
													.push(activityInstance);
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
				WorklistPage.prototype.openActivityInstancePage = function(
						activityInstance) {
					this.deck.activityInstancePage.activityInstance = activityInstance;
					this.deck.pushPage(this.deck.activityInstancePage);
				};

				/**
				 * 
				 */
				WorklistPage.prototype.back = function() {
					this.deck.popPage();
				};
			}
		});
