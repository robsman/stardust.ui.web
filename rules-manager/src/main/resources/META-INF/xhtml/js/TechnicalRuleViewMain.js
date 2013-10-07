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
				'raphael' : [ 'bpm-modeler/js/libs/raphael/2.0.1/raphael',
						'//cdnjs.cloudflare.com/ajax/libs/raphael/2.0.1/raphael-min' ],
				'angularjs' : [ 'bpm-modeler/js/libs/angular/angular-1.0.2',
						'//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min' ],
				'mustache' : [
						'bpm-modeler/js/libs/mustache/mustache',
						'https://raw.github.com/janl/mustache.js/6d1954cb5c125c40548c9952efe79a4534c6760a/mustache' ],
				'jquery-ui': [
				        'bpm-modeler/js/libs/jquery/plugins/jquery-ui-1.10.2.min',
				        '//ajax.googleapis.com/ajax/libs/jqueryui/1.10.2/jquery-ui.min'],
				'jquery.atmosphere' : [
						'bpm-modeler/js/libs/jquery/plugins/jquery.atmosphere',
						'https://raw.github.com/Atmosphere/atmosphere/cc760abedaa3d1f8bd7952c9555f7f40b8f41e2e/modules/jquery/src/main/webapp/jquery/jquery.atmosphere' ],
				'jquery.download' : [
						'bpm-modeler/js/libs/jquery/plugins/download.jQuery',
						'https://raw.github.com/filamentgroup/jQuery-File-Download/master/jQuery.download' ],
				'jquery.jeditable' : [
						'bpm-modeler/js/libs/jquery/plugins/jquery.jeditable',
						'https://raw.github.com/tuupola/jquery_jeditable/bae12d99ab991cd915805667ef72b8c9445548e0/jquery.jeditable' ],
				'jquery.form' : [
						'bpm-modeler/js/libs/jquery/plugins/jquery.form',
						'https://raw.github.com/malsup/form/5d413a0169b673c9ee81d5f458b1c955ff1b8027/jquery.form' ],
				'jquery.jstree' : [
						'bpm-modeler/js/libs/jquery/plugins/jquery.jstree',
						'https://jstree.googlecode.com/svn-history/r191/trunk/jquery.jstree' ],
				'jquery.simplemodal' : [
						'bpm-modeler/js/libs/jquery/plugins/jquery.simplemodal.1.4.1.min',
						'//simplemodal.googlecode.com/files/jquery.simplemodal.1.4.1.min' ],
				'jquery.tablescroll' : [
						'bpm-modeler/js/libs/jquery/plugins/jquery.tablescroll',
						'https://raw.github.com/farinspace/jquery.tableScroll/master/jquery.tablescroll' ],
				'jquery.treeTable' : [
						'bpm-modeler/js/libs/jquery/plugins/jquery.treeTable',
						'https://raw.github.com/ludo/jquery-treetable/f98c6d07a02cb48052e9d4e033ce7dcdf64218e1/src/javascripts/jquery.treeTable' ],
				'jquery.url' : [
						'bpm-modeler/js/libs/jquery/plugins/jquery.url',
						'https://raw.github.com/allmarkedup/jQuery-URL-Parser/472315f02afbfd7193184300cc381163e19b4a16/jquery.url' ],
				'jquery.ba-outside-events' : [
						'rules-manager/js/libs/jquery/plugins/jquery.ba-outside-events',
						'https://github.com/cowboy/jquery-outside-events/blob/master/jquery.ba-outside-events.js' ],
				'common-plugins' : '../services/rest/bpm-modeler/config/ui/plugins/common-plugins',
				'i18n' : 'common/InfinityBPMI18N',
				'ace': ['rules-manager/js/libs/ACE/ace', 'https://github.com/ajaxorg/ace-builds/blob/master/src/ace']
			},
			shim : {
				'json' : {
					exports : "JSON"
				},
				'i18n' : {
					exports : "InfinityBPMI18N"
				},
				'jquery-ui' : [ 'jquery' ],
				'jquery.download' : [ 'jquery' ],
				'jquery.form' : [ 'jquery' ],
				'jquery.jeditable' : [ 'jquery' ],
				'jquery.simplemodal' : [ 'jquery' ],
				'jquery.tablescroll' : [ 'jquery' ],
				'jquery.treeTable' : [ 'jquery' ],
				'jquery.url' : [ 'jquery' ],
				'jquery.ba-outside-events': ['jquery']
			}
		});

require([ "require", "jquery", "jquery-ui", "jquery.download", "jquery.form",
		"jquery.jeditable", "jquery.simplemodal",
		"jquery.tablescroll", "jquery.treeTable", "jquery.url",
        "jquery.ba-outside-events",
		"bpm-modeler/js/m_utils", "i18n", "common-plugins",
		"bpm-modeler/js/m_communicationController",
		"bpm-modeler/js/m_urlUtils", "bpm-modeler/js/m_constants",
		"bpm-modeler/js/m_command", "bpm-modeler/js/m_commandsController",
		"bpm-modeler/js/m_view", "rules-manager/js/RuleSetView",
		"rules-manager/js/libs/ACE/ace",
		"rules-manager/js/TechnicalRuleView"], function(
		require) {
	
	var techRuleView=require("rules-manager/js/TechnicalRuleView");
	/*pass in our selectors the view will bind its functionality to*/
	var options={
			selectors:{
				id: "#techRuleView",            /*Main view*/
				drlEditor: "#techRuleCodeEditor",/*text area we will co-opt as our drlEditor*/
				uuidOutput: "#uuidOutput", /*displays techrule metadata*/
				idOutput: "#idOutput",/*displays techrule metadata*/
				nameInput: "#nameInput",/*update our techRule name->flows back to ruleset*/
				tabs: "#techRuleTabs",/*root element for our jqueryui tabs*/
				drlEditorReplaceOptionVal: "#drlEditorReplaceOptionVal",/*displays currently selected replace option*/
				drlEditorReplaceOption: "#drlEditorReplaceOption",/*element to which the replace options menu toggles-on-click*/
				drlEditorReplaceVal: "#drlEditorReplaceVal",/*val to textReplace with in our drlEditor*/
				drlEditorReplaceAction:"#drlEditorReplaceAction",/*inititate a text replace in our drlEditor*/
				drlEditorFindVal: "#drlEditorFindVal",/*element with the val we will search for*/
				drlFindBackwards: "#drlFindBackwards",/*iniitates a find backwards text search on click*/
				drlFindForwards: "#drlFindForwards",/*initiates a find forward text search on click*/
				gotoLineNo: "#gotoLineNo",/*input element monitored for line number jumps*/
				selectedFontSize: "#selectedFontSize",/*element to display the selected font size*/
				fontDropdown: "#fontDropdown",/* element which the fontSize menu will toggle-on-click*/
				drlSelectedTheme: "#drlSelectedTheme",/*element which will show the name of the selected theme*/
				themeDropdown: "#themeDropdown"/*element to which the theme menu will toggle-on-click*/
			}
	};
	
	techRuleView.initialize(BridgeUtils.View.getActiveViewParams().param("ruleSetUuid"),
							BridgeUtils.View.getActiveViewParams().param("uuid"),
							options);

});
