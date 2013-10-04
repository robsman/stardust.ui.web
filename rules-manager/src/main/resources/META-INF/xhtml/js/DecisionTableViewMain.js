/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

require
		.config({
			baseUrl : "plugins/",
			paths : {
				'jquery' : [ 'bpm-modeler/js/libs/jquery/jquery-1.7.2',
						'//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min' ],
				'json' : [ 'bpm-modeler/js/libs/json/json2',
						'//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2' ],
				'jquery-ui': [
				        'bpm-modeler/js/libs/jquery/plugins/jquery-ui-1.10.2.min',
				        '//ajax.googleapis.com/ajax/libs/jqueryui/1.10.2/jquery-ui.min'],
				'common-plugins' : '../services/rest/bpm-modeler/config/ui/plugins/common-plugins',
				'i18n' : 'common/InfinityBPMI18N',
				'ace': ['rules-manager/js/libs/ACE/ace', 'https://github.com/ajaxorg/ace-builds/blob/master/src/ace'],
				'bootstrap3' :   ['rules-manager/js/libs/bootstrap/bootstrap'],
				'Handsontable': ['rules-manager/js/libs/Handsontable/jquery.handsontable.full.0917.js'],
				'jstree': ['rules-manager/js/libs/jstree/jstree1.0.3/jquery.jstree']
			},
			shim : {
				'json' : {
					exports : "JSON"
				},
				'i18n' : {
					exports : "InfinityBPMI18N"
				},
				'jquery-ui' : [ 'jquery' ],
				'bootstrap3' :{deps: ['jquery']},
			    'Handsontable' : {deps: ["jquery"]},
			    'jstree' : {deps: ["jquery"]}
			}
		});

require([ "require", 
          "jquery", 
          "jquery-ui", 
		  "bpm-modeler/js/m_utils", 
		  "i18n", 
		  "common-plugins",
		  "bpm-modeler/js/m_communicationController",
		  "bpm-modeler/js/m_urlUtils", 
		  "bpm-modeler/js/m_constants",
		  "bpm-modeler/js/m_command", 
		  "bpm-modeler/js/m_commandsController",
		  "bpm-modeler/js/m_view", 
		  "rules-manager/js/DecisionTableView",
		  "rules-manager/js/libs/ACE/ace",
		  "Handsontable",
		  "bootstrap3",
		  "jstree"], 
		function(require) {
		var decisionTblView=require("rules-manager/js/DecisionTableView");
		var options={
				selectors: {
					id: "#DecisionTableView", /*ID of our main view container*/
					uuidOutput: "#uuidOutput", /*element that display the uuid of our decision table*/
					idOutput: "#idOutput", /*element that display the id of our decision table*/
					nameInput: "#nameInput", /*element that will bind its val to the name of our decision table*/
					decisionTable: "#decisionTableBuilder", /*element that will be our decision table*/
					drlEditor: "decisionTableCodeEditor", /*element that will be our ace code editor*/
					columnTreeButton:"#categoryDropdown", /*element which onclick will launch a column-add-tree*/
					addRowBtn: ".btn-add-row", /**/
					exportBtn: ".export-icon", /**/
					addIcons: ".add-icon", /**/
					importIcons: ".import-icon", /**/
					decisionTableTabs: "#decisionTableTabs", /**/
					decisionTableCodeTab: "#decisionTableCodeTab", /**/
					hideNonDataColumns: "#hideNonDataCols"
		}};
		
		decisionTblView.initialize(BridgeUtils.View.getActiveViewParams().param("ruleSetUuid"),
								   BridgeUtils.View.getActiveViewParams().param("uuid"),options);
});
