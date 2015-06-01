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

/**
 * Service to handle our JSON structure for benchmarks. Provides the methods needed to
 * create and update our JSON clientside.
 */
(function(){
	
	'use strict';
	
	//Private: base structure of benchmark data.
	//This is common to activities and processes.
	var baseBenchmarkData = 
	{
	  "enableDueDate": true,
	  "isFreeForm": false,
	  "freeFormScript": "",
	  "dueDate": {
	    "dataReference": "",
	    "dayType": "",
	    "offset": {
	      "amount": 0,
	      "unit": "h",
	      "offsetTime": "00:00 AM"
	    }
	  },
	  "categories": []
	};
	
	//Private: base structure of a benchmark
	var baseBenchmark =  
	{
		"benchmark":
		{
		  "id": "",
		  "name": "Default Benchmark",
		  "description": "",
		  "author": "",
		  "lastModified": "",
		  "validFrom": "",
		  "categories": [],
		  "models": []
		}
	};
	
	//models have no benchmark data
	var baseModel =
	{
		"id" : "",
		"oid": "",
		"processDefinitions" : []
	};
	
	//Activity with undefined benchmark data, builder should initialize as needed
	var baseProcessDefinition = 
	{
		"id": "",
		"oid": "",
		"benchmarkData" : undefined,
		"activities": []
	};
	
	//Activity with undefined benchmark data, builder should initialize as needed
	var baseActivity = 
	{
		"id": "",
		"oid": "",
		"benchmarkData" : undefined
	};
	
	//Top level category structure.
	var baseCategory = {
			"id" : "0000-0000-0000-0000",
			"name" : "Default Category",
			"color" : "#00FF00"
	}
	
	//Basic JSON structure of a Category
	//TODO: determine how categories will be generated considering ordinal ids???
	var baseCategoryData = 
	{
        "categoryId": "0000-0000-0000-0000",
        "condition": {
          "lhs": "CurrentTime",
          "operator": "NotLaterThan",
          "rhs": "BusinessDate"
        },
        "useBusinessDays": false,
        "applyOffset": false,
        "offset": {
          "amount": 0,
          "unit": "d",
          "offsetTime": "00:00 AM"
        }
      };
	
	/**
	 * Constructor
	 */
	function benchmarkBuilderService(){
		//Stubbed
	}
	
	benchmarkBuilderService.prototype.cloneCategories = function(categories){
		var clonedCats = {};
		angular.copy(categories, clonedCats);
		return clonedCats;
	};
	
	/**
	 * Return a RC4 compliant UUID
	 */
	benchmarkBuilderService.prototype.getUUID = function(){
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return v.toString(16);
        });
	};
	
	/**
	 * Returns the base benchmark structure
	 * @returns
	 */
	benchmarkBuilderService.prototype.getBaseBenchmark = function(){
		var baseCopy={};
		angular.copy(baseBenchmark, baseCopy);
		baseCopy.benchmark.categories.push(this.getBaseCategory());
		return baseCopy;
	}
	
	
	/**
	 * Returns the base Category structure of the top level benchmark.
	 * @returns
	 */
	benchmarkBuilderService.prototype.getBaseCategory = function(){
		var baseCopy={};
		angular.copy(baseCategory, baseCopy);
		return baseCopy;
	}
	
	/**
	 * Returns the base Model structure
	 * @returns
	 */
	benchmarkBuilderService.prototype.getBaseModel = function(model){
		var baseCopy={};
		angular.copy(baseModel, baseCopy);
		return baseCopy;
	}
	
	/**
	 * Returns the base Process Definition structure
	 * @returns
	 */
	benchmarkBuilderService.prototype.getBaseProcessDefinition = function(categories){
		var baseCopy={};
		angular.copy(baseProcessDefinition, baseCopy);
		baseCopy.benchmarkData = this.getBaseBenchmarkData(categories);
		
		//If not categories passed in then use a single default
		if(!categories){
			baseCopy.categories.push(this.getBaseCategoryData());
		}
		//else match the number of categories and link with categoryId; 
		else{
			this.populateCategories(baseCopy,categories)
		}
		
		return baseCopy;
	}
	
	/**
	 * Returns the base Activity structure
	 * @returns
	 */
	benchmarkBuilderService.prototype.getBaseActivity = function(categories){
		var baseCopy={},
			tempCatBase,
			tempCatNew,
			i;
		
		angular.copy(baseActivity, baseCopy);
		baseCopy.benchmarkData = this.getBaseBenchmarkData();
		
		//If not categories passed in then use a single default
		if(!categories){
			baseCopy.categories.push(this.getBaseCategoryData());
		}
		//else match the number of categories and link with categoryId; 
		else{
			this.populateCategories(baseCopy,categories)
		}
		
		return baseCopy;
	}
	
	/**
	 * Given an array of categories and an element containing a baseCopy element containing a categories
	 * array with N elements, populate the baseCopy's categories with N elements and seed with the 
	 * correct category Id.
	 * @param baseCopy
	 * @param categories
	 */
	benchmarkBuilderService.prototype.populateCategories = function(baseCopy,categories){
		var tempCatBase,
			tempCatNew,
			i;
		
		for(i=0;i<categories.length;i++){
			tempCatBase = categories[i];
			tempCatNew = this.getBaseCategoryData();
			tempCatNew.categoryId = tempCatBase.id;
			baseCopy.benchmarkData.categories.push(tempCatNew);
		}
	}
	
	/**
	 * @returns default JSON representing the benchmark data  of an element
	 */
	benchmarkBuilderService.prototype.getBaseBenchmarkData = function(){
		var baseCopy={}
		angular.copy(baseBenchmarkData, baseCopy);
		return baseCopy;
	}
	
	/**
	 * Returns the base Activity structure
	 * @returns
	 */
	benchmarkBuilderService.prototype.getBaseCategoryData = function(){
		var baseCopy={};
		angular.copy(baseCategoryData, baseCopy);
		return baseCopy;
	}
	
	//Angular dependency injection
	benchmarkBuilderService.$inject = [];
	
	angular.module("benchmark-app.services")
	.service("benchmarkBuilderService",benchmarkBuilderService);
})();