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

require([ "require", "jquery", "jquery-ui","rules-manager/js/m_technicalRuleView","i18n","rules-manager/js/libs/ACE/ace",], function(require) {
	
	var techRuleView=require("rules-manager/js/m_technicalRuleView");
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
				drlFindBackwards: "#drlFindBackwards",/*initates a find backwards text search on click*/
				drlFindForwards: "#drlFindForwards",/*initiates a find forward text search on click*/
				gotoLineNo: "#gotoLineNo",/*input element monitored for line number jumps*/
				selectedFontSize: "#selectedFontSize",/*element to display the selected font size*/
				fontDropdown: "#fontDropdown",/* element which the fontSize menu will toggle-on-click*/
				drlSelectedTheme: "#drlSelectedTheme",/*element which will show the name of the selected theme*/
				themeDropdown: "#themeDropdown", /*element to which the theme menu will toggle-on-click*/
				codeTab: "#techRuleCodeTab", /*tab (li) for our code view*/
				findLabel: "#findLabel", /*Code editor toolbar label*/
				lineNumberLabel: "#lineNumberLabel", /*Code editor toolbar label*/
				replaceLabel: "#replaceLabel", /*Code editor toolbar label*/
				replaceMenu: "#replaceMenu", /*Menu selector for toolbar replace menu*/
				optReplaceCurrent: "#optReplaceCurrent", /*menu option for toolbar replace menu*/
				optReplaceAll: "#optReplaceAll", /*menu option for toolbar replace menu*/
				idLabel:"#idLabel", /*label for our rule ID*/
				uuidLabel:"#uuidLabel", /* label for our rule UUID*/
				descriptionLabel:"#descriptionLabel", /*label for rule description textarea*/
				nameLabel:"#nameLabel", /*label for rule name*/
				descriptionTextarea: "#descriptionTextarea", /*text area for our technical Rule description*/
				stringifyParamDefs: "#stringifyParamDefs" /*dump drl typeDefs into our editor*/
			},
			i18nMaps:{
				codeTab: {path:"rules.propertyView.technicalruleview.ruleeditor.tab",defaultText:"NA",attr:"text"},
				findLabel: {path:"rules.propertyView.technicalruleview.ruleeditor.toolbar.label.find",defaultText:"NA",attr:"text"},
				lineNumberLabel: {path:"rules.propertyView.technicalruleview.ruleeditor.toolbar.label.lineNumber",defaultText:"NA",attr:"text"},
				replaceLabel:  {path:"rules.propertyView.technicalruleview.ruleeditor.toolbar.label.replace",defaultText:"NA",attr:"text"},
				drlFindForwards: {path:"rules.propertyView.technicalruleview.ruleeditor.toolbar.tooltip.gotoNext",defaultText:"NA",attr:"title"},
				drlFindBackwards: {path:"rules.propertyView.technicalruleview.ruleeditor.toolbar.tooltip.gotoPrevious",defaultText:"NA",attr:"title"},
				fontDropdown: {path:"rules.propertyView.technicalruleview.ruleeditor.toolbar.tooltip.selectFontSize",defaultText:"NA",attr:"title"},
				themeDropdown: {path:"rules.propertyView.technicalruleview.ruleeditor.toolbar.tooltip.selectTheme",defaultText:"NA",attr:"title"},
				drlEditorReplaceAction: {path:"rules.propertyView.technicalruleview.ruleeditor.toolbar.label.replace",defaultText:"NA",attr:"title"},
				optReplaceCurrent:	{path:"rules.propertyView.technicalruleview.ruleeditor.toolbar.label.replaceCurrent",defaultText:"NA",attr:"text"},
				optReplaceAll: {path:"rules.propertyView.technicalruleview.ruleeditor.toolbar.label.replaceAll",defaultText:"NA",attr:"text"},
				idLabel:{path:"rules.element.properties.commonProperties.id",defaultText:"NA",attr:"text"},
				nameLabel:{path:"rules.element.properties.commonProperties.name",defaultText:"NA",attr:"text"},
				descriptionLabel:{path:"rules.element.properties.commonProperties.description",defaultText:"NA",attr:"text"},
				uuidLabel:{path:"rules.element.properties.commonProperties.uuid",defaultText:"NA",attr:"text"},
				drlEditorReplaceOptionVal: {path:"rules.propertyView.technicalruleview.ruleeditor.toolbar.label.replaceCurrent",defaultText:"NA",attr:"text"}
			}
	};
	
	techRuleView.initialize(BridgeUtils.View.getActiveViewParams().param("ruleSetUuid"),
							BridgeUtils.View.getActiveViewParams().param("uuid"),
							options);

});
