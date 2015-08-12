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
 * @author Johnson.Quadras
 */

html5Deps.loadStyleSheets("../../../");

var configs = html5Deps
		.prepareRequireJsConfig({
			baseUrl : "../../..",
			paths : {
				'i18n' : [ 'common/InfinityBPMI18N' ],
			},
			shim : {
				'i18n' : {
					exports : "InfinityBPMI18N"
				}
			},
			deps : ["require","html5-views-common/scripts/services/sdCorrespondenceService","html5-views-common/scripts/controllers/sdCorrespondenceCtrl"]
		});

require.config({
	baseUrl : configs.baseUrl,
	paths : configs.paths,
	shim : configs.shim,
	waitSeconds : 0
});

require(configs.deps, function(require, sdCorrespondenceService,sdCorrespondenceCtrl) {
	jQuery(document).ready(
			function() {
			    var app = angular.module('correspondenceApplication',[]),
		    	appModule;
			    sdCorrespondenceService.init(angular, app.name)
			    sdCorrespondenceCtrl.init(angular, app.name);
				// Register top level module
				portalApplication.registerModule('correspondenceApplication');
				var appModule = [ "correspondenceApplication" ];
				html5Deps.bootstrapAngular(appModule);

			});
});
