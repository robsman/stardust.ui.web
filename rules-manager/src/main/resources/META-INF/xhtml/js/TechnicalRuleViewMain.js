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
				'jquery-ui': [
				        'bpm-modeler/js/libs/jquery/plugins/jquery-ui-1.10.2.min',
				        '//ajax.googleapis.com/ajax/libs/jqueryui/1.10.2/jquery-ui.min'],
				'common-plugins' : '../services/rest/bpm-modeler/config/ui/plugins/common-plugins',
				'ace': ['rules-manager/js/libs/ACE/ace', 'https://github.com/ajaxorg/ace-builds/blob/master/src/ace'],
				'i18n' : 'common/InfinityBPMI18N'
			},
			shim : {
				'i18n' : {
					exports : "InfinityBPMI18N"
				},
				'jquery-ui' : [ 'jquery' ]
			}
		});

require([ "require", "jquery", "jquery-ui","rules-manager/js/TechnicalRuleView","i18n","rules-manager/js/libs/ACE/ace",], function(require) {
	
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
