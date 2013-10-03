define(["jquery","jquery-ui","bootstrap","./m_treeFactory"],function($,jQueryui,bootstrap,modelTreeFactory){
  var popoverFactory={
    attachPopover: function(selector,tableSelector,treeData){
      var popoverMenu=$(selector).popover({
        title:"Select Category",
        content:function(){
          var domString="<div>" +
                         "<div data-category='Attribute' id='add-attr' class='ipp-menuitem ipp-menulabel'>"+
                           "<span class='ipp-bullet'>&#x2022;</span>"+
                           "<span>Attribute<span>"+
                         "</div>" +
                          "<div data-category='Condition' id='add-cond' class='ipp-menuitem ipp-menulabel'>"+
                           "<span class='ipp-bullet'>&#x2022;</span>"+
                           "<span>Condition<span>"+
                         "</div>" +
                          "<div data-category='Action' id='add-actn' class='ipp-menuitem ipp-menulabel'>"+
                           "<span class='ipp-bullet'>&#x2022;</span>"+
                           "<span>Action<span>"+
                         "</div>" +
                       "</div>",
              $domFrag=$(domString);
              $("#add-attr,#add-actn,#add-cond",$domFrag).each(function(){
                  var category=$(this).attr("data-category");
                  
                  $(this).on("click",function(){
                     var tree=modelTreeFactory.getTree(category,tableSelector,treeData);
                     var myDialog=$(tree).dialog({autoOpen: false,title:"Add Column"});
                     popoverMenu.popover("hide");
                     myDialog.dialog("open");
                  });
              });
          return $domFrag;
        },
        html:true,
        placement:"bottom"});
    }
  };
  return popoverFactory;
});