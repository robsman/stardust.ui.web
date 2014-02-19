define(["jquery","jquery-mobile"],function(jQuery,jqm){
(function($){
	  $.widget("mobile.inlineAlert", $.mobile.widget, {
		  
	    /*Available options for the widget are specified here, along with default values. */
	    options: {
	      msg : "Default Text",
	      category : "warning",
	      visible : "true"
	    },
	    
	    /*Mandatory method - automatically called by jQuery Mobile to initialise the widget. */
	    _create: function() {
	      var opts = $.extend(this.options, this.element.data("options")),
	      	  $span = $("<span>" + opts.msg + "</span>"),
	      	  categoryClass="jqm-cstm-" + opts.category,
	      	  that = this;
	      
	      this.element.addClass("jqm-cstm-inlineAlert");
	      this.element.addClass(categoryClass);
	      this.element.append($span);
	      this.element.on("click",function(e){
	    	  that.element.addClass("jqm-cstm-hide");
	    	  that.element.removeClass("jqm-cstm-show");
	    	  $span.text("");
	      });
	      
	      if(opts.visible){
	    	  this.element.addClass("jqm-cstm-show");
	      }else{
	    	  this.element.addClass("jqm-cstm-hide");
	      }
	      
	      $(document).trigger("inlineAlertcreate");
	    },
	    
	    /** Custom method to handle updates. */
	    _update: function() {;
		     var opts = $.extend(this.options, this.element.data("options")),
		         $span = $("<span class='jqm-cstm-" + opts.category + ">" + opts.msg + "</span>");
		    
		     this.element.empty();
		      
		     this.element.append($span);
		      
		     if(opts.visible){
		    	 this.element.addClass("jqm-cstm-show");
		     }else{
		    	 this.element.addClass("jqm-cstm-hide");
		     }
		     
		     $(document).trigger("inlineAlertupdate");
	    },
	    /* Externally callable method to force a refresh of the widget. */
	    refresh: function() {
	    	return this._update();
	    },
	    show: function(msg,hideIn){
	    	var that = this;
	    	if(msg){
	    		$("span",this.element).text(msg);
	    	}
	    	if(hideIn){
	    		setTimeout(function(){that.hide();},hideIn)
	    	}
	    	this.element.removeClass("jqm-cstm-hide");
	    	this.element.addClass("jqm-cstm-show");
	    },
	    hide: function(){	    	
	    	this.element.removeClass("jqm-cstm-show");
	    	this.element.addClass("jqm-cstm-hide");
	    }
	  });
	  /* Handler which initialises all widget instances during page creation. */
	  $(document).bind("pagecreate", function(e) {
	    $(document).trigger("inlineAlertbeforecreate");
	    return $(":jqmData(role='inlineAlert')", e.target).inlineAlert();
	  });
	})(jQuery);
});