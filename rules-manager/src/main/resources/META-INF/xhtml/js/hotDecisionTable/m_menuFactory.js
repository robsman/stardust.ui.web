/*
 * Encapsulates JqueryUI.menu creation. Intended for use by other modules
 * which need to churn out Jquery menus dynamically.
 * Request the instance with a position parameter @see http://api.jqueryui.com/position/
 * Request the menu using instance.getMenu(...)
 * @Constructor(s):
 * 	var myFac=menuFactory(position);
 * @Function.getMenu:
 * 	getMenu(keyElement,menuString,callback)
 *  keyElement= element we will attach the menu to.
 *  menuString= content of the menu
 *  callback= handler for menu item click events.
 */
define(["bpm-modeler/js/m_utils"],function(m_utils){
  var menuFactory=function(position){
  return {
    getMenu: function(keyElement,menuString,callback){
    	if(!keyElement){return;}
        var $menuFrag,
            $menu,
            $keyElement;
        
        $menuFrag=$(menuString);
        $keyElement=m_utils.jQuerySelect(keyElement);
        $menu=$menuFrag.menu({
          select: function(event, ui) {
                      if(callback){
                        callback(event,ui);
                      }
                      $menu.toggle( "blind",{}, 250 );
          }
        });
        $menu.hide();
        $keyElement.on("click",function(){
          $menu.show().position(position);
        });
      
        $menu.appendTo($keyElement);
        return $keyElement;
      }
    };
  };
  return menuFactory;
});