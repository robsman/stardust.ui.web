/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Yogesh.Manware
 */

angular.module('STARTDUST_REPORTING', [])
    .controller(
    'Controller', [
    '$scope',

function($scope) {
    $scope.reportData = __reportData;
    $scope.isSeriesGroup = __isSeriesGroup;
    $scope.isRecordSet = !__isSeriesGroup;
    
    
    if($scope.isRecordSet){
    	$scope.recordSet; // simplified/unqualified descriptor ids 
        $scope.qualifiedRecordSet; // qualified descriptor ids

        if ($scope.reportData.rows) {
            $scope.recordSet = [];
            $scope.qualifiedRecordSet = [];
            for (var i = 0; i < $scope.reportData.rows.length; i++) {
                var record = {};
                var qRecord = {};
                
                for (var j = 0; j < $scope.reportData.columns.length; j++) {
                    var columnName = $scope.reportData.columns[j];
                    if (columnName.lastIndexOf("}") != -1) {
                        columnName = columnName.substring(
                        columnName.lastIndexOf("}") + 1,
                        columnName.length);
                    }

                    record[columnName] = $scope.reportData.rows[i][j];
                    qRecord[$scope.reportData.columns[j]] = $scope.reportData.rows[i][j];
                }
                $scope.recordSet.push(record);
                $scope.qualifiedRecordSet.push(qRecord);
            }
        };
    }else if($scope.isSeriesGroup){
    	$scope.seriesGroupData = {};
    	for ( var i in $scope.reportData) {
			var dimArray = [];
    		for ( var j in $scope.reportData[i]) {
    			var rDimension = {};
    			rDimension.dimension = $scope.reportData[i][j][0]; 
    			rDimension.max = $scope.reportData[i][j][1];
    			rDimension.min = $scope.reportData[i][j][2];
    			rDimension.avg = $scope.reportData[i][j][3];
    			rDimension.std = $scope.reportData[i][j][4];
    			rDimension.count = $scope.reportData[i][j][5];
    			dimArray.push(rDimension);
			}
    		$scope.seriesGroupData[i] = dimArray;  
		}
    }
}])
    .filter('unique', function() {
    return function(items, name) {
        var arrayToReturn = [];

        for (var i = 0; i < items.length; i++) {
            var exist = false;
            for (var j = 0; j < arrayToReturn.length; j++) {
                if (items[i][name] == arrayToReturn[j][name]) {
                    exist = true;
                }
            }
            if (!exist) {
                arrayToReturn.push(items[i]);
            }
        }
        return arrayToReturn;
    };
})
    .directive(
    'sdReportFrame',

function($compile) {
    	// Input attributes
	    // path : report definition path
	    // parameters: input parameters to report definition {{'&Param1Name='+record.param1Name+'&param2Name='+record.param2Name}}
	    // qualifiedParameters : indicates whether the supplied parameter ids are qualified or simplified
	    // any other attributes to iFrame tag: optional and default attributes are set in this directive.
	    // but if attributes are supplied as directive attributes, these attributes overwrite and merge with default attributes.
    return {
        restrict: 'E',
        transclude: false,
        scope: {
            path: '@',
            parameters: '@',
            qualifiedParameters: '@'
        },
        link: function(scope, elem, attrs) {

            // defaults
            var iFrameAttributes = {
                style: "border: none; width: 100%; height: 100%;",
                allowtransparency: "true",
                frameborder: "0",
                sandbox: "allow-same-origin allow-forms allow-scripts",
                scrolling: "auto"
            };

            // merge table options
            for (var iAttr in attrs.$attr) {
                if (iAttr != "path" && iAttr != "parameters") iFrameAttributes[iAttr] = attrs[iAttr];
            }

            var attributesStr = "";
            // iterate over all attributes
            for (var prop in iFrameAttributes) {
                attributesStr += prop + '= "' + iFrameAttributes[prop] + '" ';
            }

            if (scope.parameters && scope.parameters.indexOf("&") != 0) {
                scope.parameters = "&" + scope.parameters;
            }

            if (!scope.parameters) {
                scope.parameters = "";
            }

            var rootUrl = window.location.href.substring(0,
            location.href.indexOf("/plugins"));

            var TEMPLATE = '<iframe src="' + rootUrl + '/plugins/views-common/views/report/reportViewer.html?viewMode=embedded&path=' + scope.path + '&qualifiedParameters=' +  scope.qualifiedParameters  + scope.parameters + '" ' + attributesStr + '></iframe>';

            var e = $compile(TEMPLATE)(scope);
            elem.replaceWith(e);
        }
    };
});