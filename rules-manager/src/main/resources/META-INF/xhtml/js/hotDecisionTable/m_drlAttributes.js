define([],function(){
	var attributes={
		"salience":{
			type:"xsd:int",
			defaultValue:0,
			description:"Salience is a form of priority where rules with higher salience values" +
						"are given higher priority when ordered in the Activation queue. Salience" +
						"defaults to zero, and can be negative or positive."},
		"enabled":{
			type:"xsd:boolean",
			defaultValue:"false",
			description:"A rule cannot activate if enabled is set to false."},
		"date-effective":{
			type:"xsd:date",
			defaultValue:"",
			description:"A rule can only activate if the current date and time is after date-effective attribute."},
		"date-expires":{
			type:"xsd:date",
			defaultValue:"",
			description:"A rule cannot activate if the current date and time is after the date-expires attribute."},
		"no-loop":{
			type:"xsd:boolean",
			defaultValue:"false",
			description:"Setting no-loop to true will skip the creation of another Activation for the rule with the current set of facts."},
		"agenda-group":{
			type:"string",
			defaultValue:"MAIN",
			description:"Agenda groups allow the Agenda to be partitioned providing more execution control. Only rules in the agenda group that has acquired the focus are allowed to fire."},
		"activation-group":{
			type:"string",
			defaultValue:"NA",
			description:"Rules that belong to the same activation-group, identified by this attribute's string value, will only fire exclusively. More precisely, the first rule in an activation-group to fire will cancel all pending activations of all rules in the group, i.e., stop them from firing."},
		"duration":{
			type:"xsd:double",
			defaultValue:0,
			description:"The duration dictates that the rule will fire after a specified duration, if it is still true."},
		"auto-focus":{
			type:"xsd:boolean",
			defaultValue:"false",
			description:"When a rule is activated where the auto-focus value is true and the rule's agenda group does not have focus yet, then it is given focus, allowing the rule to potentially fire."},
		"lock-on-active":{
			type:"xsd:boolean",
			defaultValue:"false",
			description:"Whenever a ruleflow-group becomes active or an agenda-group receives the focus, any rule within that group that has lock-on-active set to true will not be activated any more; irrespective of the origin of the update, the activation of a matching rule is discarded."},
		"ruleflow-group":{
			type:"string",
			defaultValue:"NA",
			description:"Rules that are assembled by the same ruleflow-group identifier fire only when their group is active."},
		"dialect":{
			type:"string",
			defaultValue:"NA",
			description:"Specifies the language to be used for any code expressions in the LHS or the RHS code block, i.e., Java or MVEL."}
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