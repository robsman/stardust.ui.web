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
				'jquery' : [ '../portal-shell/js/libs/jquery/1.9.1/jquery.min',
						'//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min' ],
				'json' : [ 'bpm-modeler/js/libs/json/json2',
						'//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2' ],
				'jquery-ui': [
				        'bpm-modeler/js/libs/jquery/plugins/jquery-ui-1.10.2.min',
				        '//ajax.googleapis.com/ajax/libs/jqueryui/1.10.2/jquery-ui.min'],
				'i18n' : 'common/InfinityBPMI18N',
				'Handsontable': [
		                'rules-manager/js/libs/jquery/plugins/jquery.handsontable.full.0917',
		                '//raw.github.com/warpech/jquery-handsontable/v0.9.17/dist/jquery.handsontable.full.js'],
				'jstree': ['rules-manager/js/libs/jquery/plugins/jquery.jstree',
				           'https://jstree.googlecode.com/svn-history/r191/trunk/jquery.jstree']
			},
			shim : {
				'json' : {exports : "JSON"},
				'i18n' : {exports : "InfinityBPMI18N"},
				'jquery-ui' : [ 'jquery' ],
			    'Handsontable' : {deps: ["jquery"]},
			    'jstree' : {deps: ["jquery"]}
			}
		});

require([ "require", 
          "jquery", 
          "jquery-ui", 
		  "i18n", 
		  "rules-manager/js/m_decisionTableView",
		  "Handsontable",
		  "jstree"], 
		function(require) {
		var decisionTblView=require("rules-manager/js/m_decisionTableView");
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
					decisionTableTabs: "#decisionTableTabs", /*parent container for our jqueryui tabcontrol*/
					decisionTableCodeTab: "#decisionTableCodeTab", /*drl editor tab of our tab control (removed)*/
					hideNonDataColumns: "#hideNonDataCols", /*toolbar image bound to function to hide certain columns in our dectbl*/
					decisionTableTab: "#decisionTableTab", /*tab for our decision table*/
					decTableNameLabel: "#decTableNameLabel", /*label for the decision table name (not the value of)*/
					decTableIdLbl: "#decTableIdLbl", /*label for the decision table id (not the value of)*/
					decTableUuidLbl: "#decTableUuidLbl", /*label for the decision table uuid (not the value of)*/
					decTableDescrLbl: "#decTableDescrLbl", /*label for the decision table descr (not the value of)*/
					exportData: "#exportData",
					importData: "#importData",
					descriptionTextarea: "#descriptionTextarea",
					addRow: "#addRow",
					decTableNameLbl: "#decTableNameLbl",
					alertPanel: "#alertPanel",
					alertPanelMsg: "#alertPanelMsg",
					tableSearchLbl: "#tableSearchLbl",
					tableSearch: "#tableSearch",
					decShowGeneralPropertiesTitle: "#decShowGeneralPropertiesTitle",
					decHideGeneralPropertiesTitle: "#decHideGeneralPropertiesTitle"
				},
				i18nMaps:{
					decisionTableTab: {path:"rules.propertyView.decisiontableview.decisiontable.tab",defaultText:"NA",attr:"text"},
					decTableNameLabel: {path:"rules.element.properties.commonProperties.name",defaultText:"NA",attr:"text"},
					decTableIdLbl: {path:"rules.element.properties.commonProperties.id",defaultText:"NA",attr:"text"},
					decTableUuidLbl: {path:"rules.element.properties.commonProperties.uuid",defaultText:"NA",attr:"text"},
					decTableDescrLbl:{path:"rules.element.properties.commonProperties.description",defaultText:"NA",attr:"text"},
					hideNonDataColumns:{path:"rules.propertyView.decisiontableview.toolbar.tooltip.hide",defaultText:"NA",attr:"title"},
					exportData: {path:"rules.propertyView.decisiontableview.toolbar.tooltip.export",defaultText:"NA",attr:"title"},
					importData: {path:"rules.propertyView.decisiontableview.toolbar.tooltip.import",defaultText:"NA",attr:"title"},
					columnTreeButton:{path:"rules.propertyView.decisiontableview.toolbar.tooltip.addColumn",defaultText:"NA",attr:"title"},
					addRow:{path:"rules.propertyView.decisiontableview.toolbar.tooltip.addRow",defaultText:"NA",attr:"title"},
					decTableNameLbl:{path:"rules.element.properties.commonProperties.name",defaultText:"NA",attr:"text"},
					tableSearchLbl: {path:"rules.outline.toolbar.label.search",defaultText:"NA",attr:"text"},
					decShowGeneralPropertiesTitle: {path:"rules.element.properties.generalProperties.title",defaultText:"NA",attr:"text"},
					decHideGeneralPropertiesTitle: {path:"rules.element.properties.generalProperties.title",defaultText:"NA",attr:"text"}
				}
		};
		
		BridgeUtils.getTimeoutService()(function(){
			var uuid,
				ruleSetUuid,
				mode;
			
			uuid = BridgeUtils.View.getActiveViewParams().param("uuid");
			ruleSetUuid = BridgeUtils.View.getActiveViewParams().param("ruleSetUuid");
			mode = BridgeUtils.View.getActiveViewParams().param("mode");
			
			decisionTblView.initialize(ruleSetUuid,uuid,options,mode);
		});
});
