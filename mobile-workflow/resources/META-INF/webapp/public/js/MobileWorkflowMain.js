/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

var bpm = null;

require
		.config({
			baseUrl : "./",
			paths : {
				'jquery' : [ 'js/libs/jquery/jquery-1.7.2.min',
						'//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min' ],
				'jquery-ui' : [
						'js/libs/jquery/plugins/jquery-ui-1.10.2.custom.min',
						'//ajax.googleapis.com/ajax/libs/jqueryui/1.10.2/jquery-ui.min' ],
				'jquery-mobile' : [ 'http://code.jquery.com/mobile/1.3.1/jquery.mobile-1.3.1.min' ],
				'jquery.base64' : [ 'js/libs/jquery/plugins/jquery.base64', '' ],
				'angularjs' : [ 'js/libs/angular/angular-1.0.2',
						'//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min' ],
				'xml2json' : [ 'js/libs/misc/xml2js' ]
			},
			shim : {
				'jquery-ui' : [ 'jquery' ],
				'jquery.base64' : [ 'jquery' ],
				'angularjs' : {
					require : "jquery",
					exports : "angular"
				}
			}
		});

require([ "require", "jquery", "angularjs", "jquery-ui", "jquery-mobile",
		"jquery.base64", "xml2json", "js/TestWorkflowService",
		"js/WorkflowService", "js/Deck", "js/LoginPage", "js/DashboardPage",
		"js/StartableProcessesPage", "js/WorklistPage",
		"js/ActivityInstancePage", "js/ProcessPage", "js/NotesPage",
		"js/NotePage", "js/UserPage", "js/ReportsPage", "js/ReportPage" ],
		function(require, jquery, angularjs, jqueryUi, jqueryMobile,
				jqueryBase64, xml2json, TestWorkflowService, WorkflowService,
				Deck, LoginPage, DashboardPage, StartableProcessesPage,
				WorklistPage, ActivityInstancePage, ProcessPage, NotesPage,
				NotePage, UserPage, ReportsPage, ReportPage) {
			jQuery(document).ready(
					function() {
						window.top.deck = new bpm.mobile_workflow.Deck();

						window.top.deck = window.top.deck.initialize(angularjs,
								new bpm.mobile_workflow.LoginPage());
						
						console.log("Scope");
						console.log(window.top.deck);
					});
		});
