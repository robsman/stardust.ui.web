/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * View Management
 * 
 * @author Marc.Gille
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_extensionManager",
				"bpm-modeler/js/m_urlUtils", "orion/bootstrap" ],
		function(Utils, m_extensionManager, m_urlUtils, bootstrap) {
			return {
				create : function() {
					window.top.orionFileManager = new OrionFileManager();

					window.top.orionFileManager.initialize();

					return window.top.orionFileManager;
				},
				getInstance : function() {
					return window.top.orionFileManager;
				},
				getFileName : getFileName,
				getFileExtension : getFileExtension
			};

			/**
			 * @class Provides operations on process model files, folders, and
			 *        projects.
			 * @name OrionFileManager
			 */
			function OrionFileManager() {
				/**
				 * 
				 */
				OrionFileManager.prototype.toString = function() {
					return "Lightdust.OrionFileManager";
				};

				/**
				 * 
				 */
				OrionFileManager.prototype.initialize = function() {
					var deferred = jQuery.Deferred();

					this.extensionManagers = {};
					this.fileContents = {};
				};

				/**
				 * 
				 */
				OrionFileManager.prototype.getViewInfo = function(uri) {
					var extensionManager = this.extensionManagers[getFileExtension(uri)];

					if (extensionManager) {
						return extensionManager.getViewInfo(uri);
					}
					
					throw "No Extension Manager available for URI " + uri;
				};

				/**
				 * 
				 */
				OrionFileManager.prototype.bootstrap = function(uri) {
					this.startFileUri = uri;
					var deferred = jQuery.Deferred();

					Utils.debug("Start boostrapping");
					Utils.debug(this);

					var fileManager = this;

					bootstrap
							.startup()
							.then(
									function(core) {
										Utils.debug("Before install");
										core.pluginRegistry
												.installPlugin(
														"http://localhost:9090/plugins/fileClientPlugin.html")
												.then(
														function(plugin) {
															plugin
																	.start()
																	.then(
																			function() {
																				fileManager.fileService = core.serviceRegistry
																						.getService("orion.core.file");
																				fileManager
																						.retrieveFileContent(
																								uri)
																						.done(
																								function() {
																									fileManager
																											.loadElements()
																											.done(
																													deferred.resolve)
																											.fail(
																													deferred.reject);
																								})
																						.fail(
																								deferred.reject);
																			},
																			function(
																					error) {
																				Utils
																						.debug("Plugin Start failed:");
																				Utils
																						.debug(error);
																				deferred
																						.reject();
																			});
														},
														function(error) {
															Utils
																	.debug("Plugin Install failed:");
															Utils.debug(error);
															deferred.reject();
														});
									}, function(error) {
										Utils.debug("Startup failed");
										Utils.debug(error);
										deferred.reject();
									});

					return deferred.promise();
				};

				/**
				 * 
				 */
				OrionFileManager.prototype.addExtensionManager = function(
						extension, manager) {
					this.extensionManagers[extension] = manager;
				};

				/**
				 * Load model elements (Process Models, Rule Sets) for all files
				 * in this.fileContents
				 */
				OrionFileManager.prototype.loadElements = function() {
					var deferred = jQuery.Deferred();

					Utils.debug("Load elements");

					var uris = [];
					var contents = [];

					for ( var uri in this.fileContents) {
						uris.push(uri);
						contents.push(this.fileContents[uri]);
					}

					Utils.debug("Content count: " + uris.length);

					var n = 0;

					this.loadElementsRecursively(n, uris, contents).done(
							deferred.resolve).fail(deferred.reject);

					return deferred.promise();
				};

				/**
				 * 
				 */
				OrionFileManager.prototype.retrieveContentRecursively = function(
						n, uris) {
					var deferred = jQuery.Deferred();

					Utils.debug("n = " + n);

					var fileManager = this;

					if (n == uris.length) {
						deferred.resolve();
					} else {
						this.fileService
								.read(uris[n])
								.then(
										function(content) {
											Utils
													.debug("Content retrieved for "
															+ uris[n]);
											fileManager.fileContents[uris[n]] = content;

											// Loading of elements for a single
											// content loaded

											++n;
											fileManager
													.retrieveContentRecursively(
															n, uris).done(
															deferred.resolve)
													.fail(deferred.reject);
										},
										function(error) {
											Utils
													.debug("Content retrieval failed:");
											Utils.debug(error);
											deferred.reject();
										});
					}

					return deferred.promise();
				};

				/**
				 * 
				 */
				OrionFileManager.prototype.loadElementsRecursively = function(
						n, uris, contents) {
					var deferred = jQuery.Deferred();

					Utils.debug("n = " + n);

					if (n == uris.length) {
						deferred.resolve();
					} else {
						var extension = getFileExtension(uris[n]);

						Utils.debug("File Extension: " + extension);

						var fileManager = this;

						if (this.extensionManagers[extension]) {
							this.extensionManagers[extension].loadElements(
									uris[n], contents[n]).done(
									function() {
										++n;
										fileManager.loadElementsRecursively(n,
												uris, contents).done(
												deferred.resolve).fail(
												deferred.reject);
									}).fail(deferred.reject);
						} else {
							// Skip the file

							Utils.debug("Skip file " + uris[n]);

							++n;
							fileManager.loadElementsRecursively(n, uris,
									contents).done(deferred.resolve).fail(
									deferred.reject);
						}
					}

					return deferred.promise();
				};

				/**
				 * 
				 */
				OrionFileManager.prototype.retrieveFileContent = function(uri) {
					var deferred = jQuery.Deferred();
					var fileManager = this;

					Utils.debug("===> Retrieving file");
					Utils.debug(uri);

					this.fileService
							.read(uri, true)
							.then(
									function(file) {
										Utils.debug("===> Metadata retrieved");
										Utils.debug(file);

										var location = file.Parents[0].Location;
										var path = location.substring(location
												.indexOf("/file"));

										Utils.debug(location);
										Utils.debug(path);

										fileManager.fileService
												.fetchChildren(
														path + "?depth=1")
												.then(
														function(files) {
															fileManager.uris = [];

															for ( var i = 0; i < files.length; ++i) {
																var location = files[i].Location;
																var path = location
																		.substring(location
																				.indexOf("/file"));
																fileManager.uris
																		.push(path);
															}

															var n = 0;

															fileManager
																	.retrieveContentRecursively(
																			n,
																			fileManager.uris)
																	.done(
																			deferred.resolve)
																	.fail(
																			deferred.reject);
														},
														function(error) {
															Utils
																	.debug("===> Failed to retrieve Children");
															Utils.debug(error)
														});
									}, function(error) {
									});

					return deferred.promise();
				};

				/**
				 * 
				 */
				OrionFileManager.prototype.saveFileContent = function(uri,
						content) {
					var deferred = jQuery.Deferred();

					Utils.debug("Save content under URI: " + uri);
					Utils.debug(content);

					this.fileService.write(uri, content).then(deferred.resolve,
							deferred.reject);

					return deferred.promise();
				};
			}

			/**
			 * 
			 */
			function getFileName(path) {
				var temp = path.split("/");

				temp = temp[temp.length - 1];
				temp = temp.split(".");
				temp = temp[0];

				return temp;
			}

			/**
			 * 
			 */
			function getFileExtension(path) {
				var temp = path.split("/");

				temp = temp[temp.length - 1];
				temp = temp.split(".");
				temp = temp[1];

				return temp;
			}
		});