Core Modifications required to *.js files included in this folder.
-------------------------------------------------------------------

1. jquery.jqplot.min.js

Modification: Remove canvas support tests obtained from http://www.modernizr.com (see https://dev.eclipse.org/ipzilla/show_bug.cgi?id=7667 for details)

Change:
	line#: -
	From: L.jqplot.support_canvas=function(){if(typeof L.jqplot.support_canvas.result=="undefined"){L.jqplot.support_canvas.result=!!document.createElement("canvas").getContext}return L.jqplot.support_canvas.result};
	To:   L.jqplot.support_canvas=function(){if(typeof L.jqplot.support_canvas.result=="undefined"){L.jqplot.support_canvas.result=true}return L.jqplot.support_canvas.result};
	
	line#: -
	From: L.jqplot.support_canvas_text=function(){if(typeof L.jqplot.support_canvas_text.result=="undefined"){if(window.G_vmlCanvasManager!==u&&window.G_vmlCanvasManager._version>887){L.jqplot.support_canvas_text.result=true}else{L.jqplot.support_canvas_text.result=!!(document.createElement("canvas").getContext&&typeof document.createElement("canvas").getContext("2d").fillText=="function")}}return L.jqplot.support_canvas_text.result};
	To:   L.jqplot.support_canvas_text=function(){if(typeof L.jqplot.support_canvas_text.result=="undefined"){L.jqplot.support_canvas_text.result=true}return L.jqplot.support_canvas_text.result};

====

Non-minified version (jquery.jqplot.js):

Before:

    // canvas related tests taken from modernizer:
    // Copyright (c) 2009 - 2010 Faruk Ates.
    // http://www.modernizr.com
    
    $.jqplot.support_canvas = function() {
        if (typeof $.jqplot.support_canvas.result == 'undefined') {
            $.jqplot.support_canvas.result = !!document.createElement('canvas').getContext; 
        }
        return $.jqplot.support_canvas.result;
    };
            
    $.jqplot.support_canvas_text = function() {
        if (typeof $.jqplot.support_canvas_text.result == 'undefined') {
            if (window.G_vmlCanvasManager !== undefined && window.G_vmlCanvasManager._version > 887) {
                $.jqplot.support_canvas_text.result = true;
            }
            else {
                $.jqplot.support_canvas_text.result = !!(document.createElement('canvas').getContext && typeof document.createElement('canvas').getContext('2d').fillText == 'function');
            }
             
        }
        return $.jqplot.support_canvas_text.result;
    };

After:

    $.jqplot.support_canvas = function() {
        if (typeof $.jqplot.support_canvas.result == 'undefined') {
            $.jqplot.support_canvas.result = true; // TODO: always return "true" (for now)
        }
        return $.jqplot.support_canvas.result;
    };
            
    $.jqplot.support_canvas_text = function() {
        if (typeof $.jqplot.support_canvas_text.result == 'undefined') {
            $.jqplot.support_canvas_text.result = true; // TODO: always return "true" (for now)
        }
        return $.jqplot.support_canvas_text.result;
    };
