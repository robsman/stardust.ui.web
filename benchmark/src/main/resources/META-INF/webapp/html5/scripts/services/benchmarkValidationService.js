/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Zachary Z McCain
 */

(function(){
	
	'use strict';
	
	//constructor
	function benchmarkValidationService(){
		this.models={}; //hashmap of the models we are working with, for quick lookups
		this.modelData={}; //hashmap of the model data we are testing against
		this.invalidData = {}; //invalid data we will test against in our validation functions
	}
	
	/**
	 * Tests if a benchmark is valid. Only returns the last error detected so multiple
	 * errors need to rely on multiple invocations with the assumption that someone
	 * is fixing the prior detected error before the next invocation.
	 * @param benchmark
	 */
	benchmarkValidationService.prototype.isBenchmarkValid = function(benchmark){
		var result = {"isValid" : true, "errorCode" : -1},
			categoryNameEmpty = false;
		
		if(!benchmark.name){
			result.isValid = false;
			result.errorCode = 1; //Empty Benchmark Name
		}
		
		categoryNameEmpty = benchmark.categories.some(function(category){
			return category.name == "";
		});
		
		if(categoryNameEmpty){
			result.isValid = false;
			result.errorCode = 2; //Empty Category Name
		}
		
		return result;
	};
	
	/**
	 * tests if a models id to determine if it is present in our
	 * invalidData collection.
	 * @param model
	 * @returns true|false
	 */
	benchmarkValidationService.prototype.isModelValid = function(model){
		return !this.invalidData.hasOwnProperty(model.id);
	};
	
	/**
	 * Tests a process definition to determine if it is present in our invalid data collection,
	 * will call isModelValid first as part of the evaluation.
	 * @param model
	 * @param procDef
	 * @returns
	 */
	benchmarkValidationService.prototype.isProcessDefinitionValid = function(model,procDef){
		return !this.invalidData.hasOwnProperty(model.id+procDef.id);
	};
	
	/**
	 * Tests an activity to determine if it is present in our invalid data collection.
	 * Test model - > activty prior to testing activity.
	 * @param model
	 * @param procDef
	 * @param activity
	 */
	benchmarkValidationService.prototype.isActivityValid = function(model,procDef,activity){
		return !this.invalidData.hasOwnProperty(model.id + procDef.id + activity.id);
	};
	
	/**
	 * leverage our  hashMap to quickly tell if a condition is valid
	 * @param categoryId
	 * @returns
	 */
	benchmarkValidationService.prototype.isDataRefValid = function(modelId){
		return !this.invalidData.hasOwnProperty(modelId);
	}
	
	/**
	 * Initializes the service with the models we will validate against
	 * @param models
	 */
	benchmarkValidationService.prototype.initModelData = function(models){
		var results= [], //any invalid data references
			that = this;
		
		//reset model and model data environment hashmaps
		this.models = {};
		this.modelData = {};
		
		//convert models to a hashmap id->data we can lookup.
		models.forEach(function(model){
			that.models[model.id] = model.data;
			//create a hashmap of every modelData id so we can quickly validate our data;
			model.data.forEach(function(datum){
				that.modelData[datum.qualifiedId]=datum;
			});
		});
	}
	/**
	 * initModel should be called prior to this function invocation,
	 * sets the benchmark we will evaluate against our model data in all
	 * calls.
	 * NOTE: we set our validation environment explicitly so as not to have to 
	 * rely on running this code on each validation (which if we passed in the benchmark or models
	 * per validation would be required). As angular will invoke a lot of digests we want this to be
	 * performant. For that reason the service expects the service user to keep the environment updated
	 * as needed rather than passing variables that would hold the complete state required per validation
	 * invocation call (which is normally my preference but in this case I will ).
	 * @param benchmark
	 * @param models
	 */
	benchmarkValidationService.prototype.setValidationBenchmark = function(benchmark){
		var that = this;
		
		//reset our validation environment
		this.invalidData = {};
		
		//now loop through benchmark structure and validate at each level
		benchmark.models.forEach(function(model){
			model.processDefinitions.forEach(function(procDef){
				procDef.categoryConditions.forEach(function(catCond){
					if(catCond.type==="dataExpression" && catCond.details.condition.rhs.type==='data'){
						console.log("CATCOND");
						console.log(catCond);
						//if we don't find a matching rhs id in our model data then push
						//an entries onto our invalidData collection
						if(!that.modelData[catCond.details.condition.rhs.id]){
							that.invalidData[model.id]=true; //entry for the model
							that.invalidData[model.id + procDef.id]=true; //entry for the proc def
							that.invalidData[catCond.details.condition.rhs.id]=true; //entry for bad data reference
						}
					}
				});
				procDef.activities.forEach(function(activity){
					activity.categoryConditions.forEach(function(catCond){
						if(catCond.type==="dataExpression" && catCond.details.condition.rhs.type==='data'){
							//if we don't find a matching rhs id in our model data then push
							//an entry onto our invalidData collection
							if(!that.modelData[catCond.details.condition.rhs.id]){
								that.invalidData[model.id]=true; //entry for the model
								that.invalidData[model.id + procDef.id]=true; //entry for the proc def
								that.invalidData[model.id + procDef.id + activity.id]=true; //entry for the activity
								that.invalidData[catCond.details.condition.rhs.id]=true; //entry for bad data reference
							}
						}
					});
				});
			});
		});
		
	};
	
	benchmarkValidationService.$inject=[];
	
	angular.module("benchmark-app.services")
	.service("benchmarkValidationService",benchmarkValidationService);
	
})();