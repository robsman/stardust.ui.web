/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

if (!window.bpm) {
	bpm = {};
}

if (!window.bpm.portal) {
	bpm.portal = {};
}

if (!window.bpm.portal.Interaction) {
	bpm.portal.Interaction = function Interaction(options) {
		this.options = options;

		if (!this.options) {
			this.options = {};
		}

		this.test = false;
		this.url = null;
		this.callbackUrl = null;
		this.metadata = null;
		this.input = null;
		this.output = {};

		/**
		 *
		 */
		Interaction.prototype.bind = function() {
			console.log("Options");
			console.log(this.options);

			var deferred = jQuery.Deferred();

			this.url = window.location;

			// Extract callback URL

			console.log("Binding Interaction Object from URL: " + this.url);

			this.callbackUrl = jQuery.url(window.location.search).param(
					"ippInteractionUri");

			this.baseUrl = this.callbackUrl.indexOf("/plugins") >= 0 ? this.callbackUrl
					.substring(0, this.callbackUrl.indexOf("/plugins"))
					: this.callbackUrl.substring(0, this.callbackUrl
							.indexOf("/services"));

			console.log("Callback URL: " + this.callbackUrl);
			console.log("Base URL: " + this.baseUrl);

			this.mode = jQuery.url(window.location.search).param("ippMode");

			console.log("Mode: "
					+ jQuery.url(window.location.search).param("ippMode"));

			if (!this.mode) {
				this.portalMainWnd = this.getIppWindow();

				console.log("Portal Main Window");
				console.log(this.portalMainWnd);
			}

			// Get metadata

			this.metadata = {};
			this.transfer = {};

			if (this.mode === "test") {
				this.transfer.person = {
					firstName : "Lara",
					lastName : "Croft",
					gender : "Female",
					dateOfBirth : new Date(),
					monthlySalary : 12300,
					numberOfDependents : 2,
					personallyKnown : true,
					accounts : [ {
						id : 134456779,
						type : "Checking",
						balance : 1345.99,
						lastTransfer : new Date()
					}, {
						id : 134456780,
						type : "Savings",
						balance : 100888.00,
						lastTransfer : new Date()
					} ]
				};

				this.transfer.loan = {};

				deferred.resolve();
			} else {
				// Get input

				var self = this;

				jQuery
						.ajax({
							type : "GET",
							url : this.callbackUrl + "/inData",
							contentType : "application/xml"
						})
						.done(
								function(data) {
									console.log(data);

									var json = data;

									if (self.mode) {
										self.transfer = json;
									} else {
										var converter = new X2JS();

										json = converter.xml2json(data);

										console.log(json);

										self.transfer = {};

										if (json.inDataValues
												&& json.inDataValues.parameter) {

											for ( var n = 0; n < json.inDataValues.parameter_asArray.length; ++n) {
												if (json.inDataValues.parameter_asArray[n].primitive) {
													self.transfer[json.inDataValues.parameter_asArray[n].name] = json.inDataValues.parameter_asArray[n].primitive;
												} else {
													// Determine structure name

													var structureName;

													for (structureName in json.inDataValues.parameter_asArray[n].xml) {
														if (structureName
																.indexOf("_asArray") != -1) {
															structureName = structureName
																	.substring(
																			0,
																			structureName
																					.indexOf("_asArray"));
															self.transfer[json.inDataValues.parameter_asArray[n].name] = json.inDataValues.parameter_asArray[n].xml[structureName];

															break;
														}
													}
												}
											}

											console.log(self.transfer);
										}
									}

									deferred.resolve();
								}).fail(function() {
							console.log("Error retrieving input data");

							deferred.reject();
						});
			}

			return deferred.promise();
		};

		/**
		 * Post all output data cached in the Interaction object to the server,
		 */
		Interaction.prototype.post = function() {
			var data;
			var contentType;

			console.log("Transfer Object");
			console.log(this.transfer);

			if (this.mode === "test") {
			} else if (this.mode === "modeler") {
				data = JSON.stringify(this.transfer);

				console.log("Stringified");
				console.log(this.transfer);

				jQuery.ajax({
					type : "PUT",
					url : this.callbackUrl + "/outData",
					contentType : "application/json",
					data : data
				}).done(function() {
					console.log("Done!");
				}).fail(function() {
					console.log("Fail!");
				});
			} else {
				console.log("Submit");
				console.log(this.transfer);

				var converter = new X2JS();

				for ( var name in this.transfer) {
					console.log("Parameter " + name);

					var data = null;

					if (typeof this.transfer[name] !== "object") {
						data = this.transfer[name];
						contentType = "text/plain";
					} else {
						var envelope = {};

						envelope[name] = this.transfer[name];

						data = converter.json2xml_str(envelope);
						contentType = "application/xml";
					}

					console.log("Value to be submitted: " + data);
					console.log("Content Type: " + contentType);

					jQuery.ajax({
						type : "PUT",
						url : this.callbackUrl + "/outData/" + name,
						contentType : contentType,
						data : "" + data
					}).done(function() {
						console.log("Done!");
					}).fail(function() {
						console.log("Fail!");
					});
				}
			}
		};

		/**
		 *
		 */
		Interaction.prototype.isThisIppWindow = function(win) {
			try {
				var baseLocation = String(win.document.location);

				// Remove Query Params

				if (-1 != baseLocation.indexOf("?")) {
					baseLocation = baseLocation.substr(0, baseLocation
							.indexOf("?"));
				}

				// Check url, it should either read main.iface or
				// login.iface

				if (-1 != baseLocation.indexOf("main.iface")
						|| -1 != baseLocation.indexOf("login.iface")) {
					return true;
				} else {
					return false;
				}
			} catch (e) {
				// May be Access Control restriction

				return false;
			}
		};

		/**
		 *
		 */
		Interaction.prototype.findIppWindow = function(win) {
			if (!isThisIppWindow(win)) {
				var frames = win.frames;

				for ( var i = 0; i < frames.length; i++) {
					var ippWindow = this.findIppWindow(frames[i]);

					if (ippWindow != null) {
						return ippWindow;
					}
				}

				return null;
			} else {
				return win;
			}
		};

		Interaction.prototype.findIppWindowBottomUp = function(win) {
			if (!this.isThisIppWindow(win)) {
				if (win.parent != null && win.parent != win) {
					return this.findIppWindowBottomUp(win.parent);
				}
			} else {
				return win;
			}
		};

		Interaction.prototype.getIppWindow = function() {
			try {
				var ippWindow = this.findIppWindowBottomUp(window);

				if (ippWindow == null && window.opener != null) {
					ippWindow = this.findIppWindowBottomUp(window.opener);
				}

				if (null != ippWindow) {
					return ippWindow["ippPortalMain"];
				} else {
					// Assume parent or opener is IPP window, but there
					// is no access to window object, due to access
					// restriction
					if (null != window.parent && window.parent != window) {
						ippWindow = window.parent;
					} else if (null != window.opener) {
						ippWindow = window.opener;
					}
				}
				return ippWindow;
			} catch (x) {
				alert(getMessage("portal.common.js.ippMainWindow.notFound",
						"Error getting Stardust Window. Portal will not work properly.")
						+ "\n" + x);

				return null;
			}
		};

		/**
		 *
		 */
		Interaction.prototype.closeEmbeddedActivityPanel = function(
				targetWindow, commandId) {
			if (targetWindow) {
				try {
					if (targetWindow.InfinityBpm.ProcessPortal) {
						if ('complete' === commandId) {
							targetWindow.InfinityBpm.ProcessPortal
									.completeActivity();
						} else if ('suspendAndSave' === commandId) {
							targetWindow.InfinityBpm.ProcessPortal
									.suspendActivity(true);
						} else if ('suspend' === commandId) {
							targetWindow.InfinityBpm.ProcessPortal
									.suspendActivity(false);
						} else if ('abort' === commandId) {
							targetWindow.InfinityBpm.ProcessPortal
									.abortActivity();
						} else if ('qaPass' === commandId) {
							targetWindow.InfinityBpm.ProcessPortal
									.qaPassActivity();
						} else if ('qaFail' === commandId) {
							targetWindow.InfinityBpm.ProcessPortal
									.qaFailActivity();
						}
						return;
					} else {
						// alert('Did not find InfinityBpm.ProcessPortal
						// module in main page' + typeof
						// targetWindow.InfinityBpm.ProcessPortal);
					}
				} catch (x1) {
					// probably forbidden to access location, assuming
					// other page
					// alert('Failed invoking top level IPP function:
					// ');
				}

				// trying postMessage
				try {
					if (targetWindow.postMessage) {
						// alert('Using post message ... ');
						// alert('Target window: ' +
						// targetWindow.toString() + ' Command id: ' +
						// commandId);
						this.sleep(2000);
						targetWindow.postMessage(commandId, "*");
						this.sleep(3000);
						// alert('Post message finished');
						return;
					}
				} catch (x2) {
					// failed using postMessage, fall back to FIM
					// alert('Failed invoking postMessage: ' + x2);
				}

				try {
					// alert('Unfortunately this browser is currently
					// not yet supported.');
					alert(getMessage(
							"portal.common.js.processPortal.api.notAvailable",
							"The Process Portal API is not available."));
					return;

					ifrm = document.createElement("IFRAME");
					ifrm.setAttribute('style',
							'display: none; width: 0px; height: 0px;');
					// TODO replace with dynamic URL determination
					ifrm.setAttribute("src",
							"http:localhost:9090/ipp/ipp/process/remoteControl/"
									+ commandId + "EmbeddedActivityPanel.html");
					document.body.appendChild(ifrm);
				} catch (x3) {
					// alert('Failed triggering cross domain panel
					// close: ' + x3.description)
				}
			}
		};

		/**
		 *
		 */
		Interaction.prototype.sleep = function(ms) {
			var dt = new Date();
			dt.setTime(dt.getTime() + ms);
			while (new Date().getTime() < dt.getTime())
				;
		};

		/**
		 *
		 */
		Interaction.prototype.getMessage = function(messageProp, defaultMsg) {
			if (InfinityBPMI18N && InfinityBPMI18N.common) {
				var propVal = InfinityBPMI18N.common.getProperty(messageProp,
						defaultMsg);
				if (propVal && propVal != "") {
					return propVal;
				}
			}

			return defaultMsg;
		};

		/**
		 *
		 */
		Interaction.prototype.completeActivity = function() {
			console.log("Complete");

			if (this.mode !== "test" && this.mode !== "modeler") {
				console.log("Delegating complete() to portal frame.");

				this.closeEmbeddedActivityPanel(this.portalMainWnd, 'complete');
			}
		};

		/**
		 *
		 */
		Interaction.prototype.qaPassActivity = function() {
			console.log("Delegating qaPassActivity() to portal frame.");

			this.closeEmbeddedActivityPanel(this.portalMainWnd, 'qaPass');
		};

		/**
		 *
		 */
		Interaction.prototype.qaFailActivity = function() {
			console.log("Delegating qaFailActivity() to portal frame.");

			this.closeEmbeddedActivityPanel(this.portalMainWnd, 'qaFail');
		};

		/**
		 *
		 */
		Interaction.prototype.suspendActivity = function(saveOutParams) {
			console.log("Suspend with data");
			this.post();

			if (this.mode !== "test" && this.mode !== "modeler") {
				console.log("Delegating suspend() to portal frame.");

				this.closeEmbeddedActivityPanel(this.portalMainWnd,
						saveOutParams ? 'suspendAndSave' : 'suspend');
			}
		};

		/**
		 *
		 */
		Interaction.prototype.abortActivity = function() {
			console.log("Delegating abort() to portal frame.");

			if (this.mode !== "test" && this.mode !== "modeler") {
				this.closeEmbeddedActivityPanel(portalMainWnd, 'abort');
			}
		};
	}
}