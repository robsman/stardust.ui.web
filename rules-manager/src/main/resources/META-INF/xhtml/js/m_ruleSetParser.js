define(["rules-manager/js/hotDecisionTable/m_operators",
        "rules-manager/js/hotDecisionTable/m_utilities"],function(operators,utils){
	return {
		/*return JSON string primed for parsing into DRL*/
		toPreDRLFormat: function(rSet){
			
			var decTableCounter, /*number of decision tables in our ruleSet*/
				i,				 /*a counter for our 'for' loop*/
				tempTable,		 /*loop var for decision tables*/
				tempData,		 /*loop var for decision table.data*/
				tempRule,		 /*loop var for technicalRules*/
				tempOp,			 /*loop var for operators*/
				hashCode,		 /*computed hashcode used to look up operator objects*/	
				opCounter,		 /*number of operators we will loop over*/
				drlOps=[],		 /*array to hold operators corresponding to our colHeaders*/
				key,			 /*key var to use in for-in loops*/
				data;			 /*our return object we collect all our data into*/	
			
			/*basic initialization of our return object*/
			data={  uuid: rSet.uuid,
					id: rSet.id,
					name: rSet.name,
					creationDate: rSet.creationDate,
					lastModificationDate: rSet.lastModificationDate,
					facts: {},
					rules: {},
					decisionTables: {}
				};
			
			/*Set our parameter Definitions*/
			data.facts= rSet.parameterDefinitions;
			
			/*Set our Technical Rules*/
			data.rules=rSet.technicalRules;
			for(key in rSet.technicalRules){	
				/*drl of interest is private so we have to use our getter*/
				if(rSet.technicalRules.hasOwnProperty(key)){
					tempRule=rSet.technicalRules[key];
					data.rules[key].drl=tempRule.getDRL();
				}
			}
			
			/*Set our Decision Tables*/
			for(key in rSet.decisionTables ){
				/*decTable of interest is private so we have to use our getter*/
				if(rSet.decisionTables.hasOwnProperty(key)){
					tempTable=rSet.decisionTables[key];
					data.decisionTables[key]=tempTable;
					tempData=tempTable.getTableData();
					opCounter=tempData.colHeaders.length;
					for(i=0;i<opCounter;i++){
						hashCode=utils.hashString(tempData.colHeaders[i].split("|")[1]);
						tempOp=operators.getOperatorByHashCode(hashCode);
						drlOps.push(tempOp.operator);
					}
					data.decisionTables[key].tableData={
							columns: tempData.columns,
							data: tempData.data,
							colWidths: tempData.colWidths,
							colHeaders: tempData.colHeaders,
							drlOperators:drlOps
					};
				}
			}
			return JSON.stringify(data);
		}
	};
});