define(["jquery","./m_images"],function($,images){
     //Functions to customize rendering of columns and headers in the HoT.
    var renderEngines={
      depTest: function(){console.log(Handsontable);},
      rowHeader:function (instance, td, row, col, prop, value, cellProperties) {
    	  return;
          var sHtml=$('<div class=""></div>');
              imgRemove=$(images.remove)
              .on("click",function(){
            	if(instance.getSettings().data.length>1){
            		instance.alter("remove_row",row);
            	}
              });
          sHtml.append(imgRemove);
          $(td).empty()
               .append(sHtml); //empty is needed because you are rendering to an existing cell
        return td;
      },
      autoComplete:function (instance, td, row, col, prop, value, cellProperties) {
        Handsontable.AutocompleteCell.renderer.apply(this, arguments);
        td.style.fontStyle = 'italic';
        td.title = 'Type to show the list of options';
      }
    }; 
    
    return renderEngines;

});