/**
 * @author Omkar.Patil
 * @author Robert Sauer
 */

require
		.config({
			baseUrl : "../../",
			paths : {
				'jquery' : [ 'bpm-modeler/js/libs/jquery/jquery-1.7.2',
						'//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min' ],
				'json' : [ 'bpm-modeler/js/libs/json/json2',
						'//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2' ],
				'jquery-ui' : [
						'bpm-modeler/js/libs/jquery/plugins/jquery-ui-1.10.2.min',
						'//ajax.googleapis.com/ajax/libs/jqueryui/1.10.2/jquery-ui.min' ],
				'jquery.atmosphere' : [
						'bpm-modeler/js/libs/jquery/plugins/jquery.atmosphere',
						'https://raw.github.com/Atmosphere/atmosphere/e6b2a1730d5c94e5d699f7656a76d1439c85889c/modules/jquery/src/main/webapp/jquery/jquery.atmosphere' ],
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
						'rules-manager/js/libs/jquery/plugins/jquery.jstree',
						'https://jstree.googlecode.com/svn-history/r191/trunk/jquery.jstree' ],
				'jquery.simplemodal' : [
						'bpm-modeler/js/libs/jquery/plugins/jquery.simplemodal.1.4.1.min',
						'//simplemodal.googlecode.com/files/jquery.simplemodal.1.4.1.min' ],
				'jquery.url' : [
						'bpm-modeler/js/libs/jquery/plugins/jquery.url',
						'https://raw.github.com/allmarkedup/jQuery-URL-Parser/472315f02afbfd7193184300cc381163e19b4a16/jquery.url' ],

				'outline-plugins' : '../services/rest/bpm-modeler/config/ui/plugins/outline-plugins',
				'i18n' : 'common/InfinityBPMI18N'
			},
			shim : {
				'i18n' : {
					exports : "InfinityBPMI18N"
				},
				'jquery-ui' : [ 'jquery' ],
				'jquery.download' : [ 'jquery' ],
				'jquery.form' : [ 'jquery' ],
				'jquery.jstree' : [ 'jquery' ],
				'jquery.simplemodal' : [ 'jquery' ],
				'jquery.url' : [ 'jquery' ]
			}
		});

define([ "jquery", "jquery-ui", "jquery.download",
		"jquery.form", "jquery.simplemodal", "jquery.url", "jquery.jstree",
		"outline-plugins", "i18n", "rules-manager/js/m_outline" ], function() {
	var outline = require('rules-manager/js/m_outline');
	var options={
			selectors:{
				createRuleSetButton: "#createRuleSetButton",
				importRuleSet: "#importRuleSet",
				undoChange: "#undoChange",
				redoChange: "#redoChange",
				saveAllRules: "#saveAllRules",
				refreshRules: "#refreshRules",
				lastSavelLabel: "#lastsave",
				lastSaveDateDisplay: "#lastSaveDateDisplay"
			},
			i18nMaps: {
				createRuleSetButton: {path:"rules.outline.toolbar.tooltip.createRuleSet",defaultText:"NA",attr:"title"},
				importRuleSet: {path:"rules.outline.toolbar.tooltip.importRuleSet",defaultText:"NA",attr:"title"},
				undoChange: {path:"rules.outline.toolbar.tooltip.undo",defaultText:"NA",attr:"title"},
				redoChange: {path:"rules.outline.toolbar.tooltip.redo",defaultText:"NA",attr:"title"},
				saveAllRules: {path:"rules.outline.toolbar.tooltip.saveAll",defaultText:"NA",attr:"title"},
				refreshRules: {path:"rules.outline.toolbar.tooltip.refresh",defaultText:"NA",attr:"title"},
				lastSavelLabel: {path:"rules.outline.labels.lastSave",defaultText:"NA",attr:"text"}
			}
	};
	outline.init(null,null,options);
});
