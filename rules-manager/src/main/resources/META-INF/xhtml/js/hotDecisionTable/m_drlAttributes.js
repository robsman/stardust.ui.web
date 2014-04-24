define(["rules-manager/js/m_i18nUtils"],function(m_i18nUtils){
	var attributes={
		"salience":{
			type:"int",
			defaultValue:0,
			description:m_i18nUtils.getProperty("rules.propertyView.decisiontableview.dialog.addcolumn.tree.node.attribute.salience.tooltip","NA")},
		"enabled":{
			type:"boolean",
			defaultValue:"false",
			description: m_i18nUtils.getProperty("rules.propertyView.decisiontableview.dialog.addcolumn.tree.node.attribute.enabled.tooltip","NA")},
		"date-effective":{
			type:"date",
			defaultValue:"",
			description:m_i18nUtils.getProperty("rules.propertyView.decisiontableview.dialog.addcolumn.tree.node.attribute.date-effective.tooltip","NA")},
		"date-expires":{
			type:"date",
			defaultValue:"",
			description:m_i18nUtils.getProperty("rules.propertyView.decisiontableview.dialog.addcolumn.tree.node.attribute.date-expires.tooltip","NA")},
		"no-loop":{
			type:"boolean",
			defaultValue:"false",
			description:m_i18nUtils.getProperty("rules.propertyView.decisiontableview.dialog.addcolumn.tree.node.attribute.no-loop.tooltip","NA")},
		"agenda-group":{
			type:"string",
			defaultValue:"MAIN",
			description:m_i18nUtils.getProperty("rules.propertyView.decisiontableview.dialog.addcolumn.tree.node.attribute.agenda-group.tooltip","NA")},
		"activation-group":{
			type:"string",
			defaultValue:"NA",
			description:m_i18nUtils.getProperty("rules.propertyView.decisiontableview.dialog.addcolumn.tree.node.attribute.activation-group.tooltip","NA")},
		"duration":{
			type:"double",
			defaultValue:0,
			description:m_i18nUtils.getProperty("rules.propertyView.decisiontableview.dialog.addcolumn.tree.node.attribute.duration.tooltip","NA")},
		"auto-focus":{
			type:"boolean",
			defaultValue:"false",
			description:m_i18nUtils.getProperty("rules.propertyView.decisiontableview.dialog.addcolumn.tree.node.attribute.auto-focus.tooltip","NA")},
		"lock-on-active":{
			type:"boolean",
			defaultValue:"false",
			description:m_i18nUtils.getProperty("rules.propertyView.decisiontableview.dialog.addcolumn.tree.node.attribute.lock-on-active.tooltip","NA")},
		"ruleflow-group":{
			type:"string",
			defaultValue:"NA",
			description:m_i18nUtils.getProperty("rules.propertyView.decisiontableview.dialog.addcolumn.tree.node.attribute.ruleflow-group.tooltip","NA")},
		"dialect":{
			type:"string",
			defaultValue:"NA",
			description:m_i18nUtils.getProperty("rules.propertyView.decisiontableview.dialog.addcolumn.tree.node.attribute.dialect.tooltip","NA")}
	};
	return {
		getAllAttributes: function(){return attributes;},
		getAllAttributesAsArray: function(){
			var attrArr=[];
			for(var k in attributes){
				if(attributes.hasOwnProperty(k)){
					attrArr.push(attributes[k]);
				}
			}
			return attrArr;
		},
		getAttributeByName: function(k){return attributes[k];},
		getAttributeAsJSTreeData:function(k,iconURL){
			var myAttr;
			if(attributes.hasOwnProperty(k)){
				myAttr=attributes[k];
				return {
					data:{title: k ,icon: iconURL},
					attr:{title: myAttr.description,defaultValue: myAttr.defaultValue},
					metadata:{type: myAttr.type,ref: "primitive",isParamDef:false}
					};
			}
		}	
	};
});