define(["./m_images", "bootstrap","./m_menuFactory"],function(images, bootstrap,menuFactory){
    var chFactory={
          AttributeHeader: function(meta,col,instance,th){
              var metaData=meta.split("|"),
                  $span,
                  $typeBadge,
                  categoryPrefix,
                  $opBadge=$("<div class='ipp-badge pointy nudgeLeft pop'>" + metaData[1] + "</div>"),
                  category=metaData[2],
                  modelText=metaData[0],
                  leafModel=modelText.split(".").slice(-1)[0],
                  labelColor,
                  needTooltip=false,
                  $prefix="",
                  popover,
                  $img;
              
              /*convert our meta text header to a data attribute to attach to the
              DOM conversion of the original text header.*/
              $opBadge.attr("data-meta-head",meta);
              
              if(metaData[2]==="Header"){
                $span="<span class=''>" + metaData[0] + "</span>";
                return $span;
              }
        
              /*Pick colors, TODO, add classes not hardcoded vals*/
              switch(category){
                case "Attribute":
                  categoryPrefix="Attr.";
                  break;
                case "Condition":
                  categoryPrefix="Cnd.";
                  break;
                case "Action":
                  categoryPrefix="Actn.";
                  break;
                default:
                  break;
              }
              
              $typeBadge=$("<span  class='cursive'>" + categoryPrefix + "</span>");
              labelColor="#AAAAAA";
              $opBadge.css("background-color",labelColor);
              $opBadge.css("height","10px");
              $opBadge.css("border-radius","4px");
              
              /*quick test to determine if we have a model with structure*/
              if(leafModel!==modelText){
                needTooltip=true;
              }
              
              /*if model has a hierarchy then add a prefix with a tooltip to
              communicate the complete model structure to the user.*/
              if(needTooltip){
                $prefix=$("<span></span>").append(modelText.charAt(0) + ".")
                .addClass("cursor-default")
                .tooltip({title: modelText,container:"body"});
              }
              
              $span=$("<span></span>")
                .addClass("")
                .append($prefix)
                .append(leafModel)
                .append($opBadge);
              
              //Build Jqueryui menu
              var menuString="<ul style='position:absolute;z-index:9999'>" +
                               "<li class='pointy'><a value='=' href='#'><span class='ipp-badge black operator pull-left'>=</span>Equals</a></li>" +
                               "<li class='pointy'><a value='&gt;'  href='#'><span value='&gt;' class='ipp-badge black operator pull-left'>></span> Greater Than</a></li>" +
                               "<li class='pointy'><a value='&lt;' href='#'><span  class='ipp-badge black operator pull-left'><</span> Less Than</a></li>" +
                               "<li class='pointy'><a value='!=' href='#'><span class='ipp-badge black operator pull-left'>!=</span> Not</a></li>" +
                               "<li class='pointy'><a value='a&#8712;A' href='#'><span class='ipp-badge black  operator pull-left'>a&#8712;A</span> In Set</a></li>" +
                               "<li class='pointy'><a value='a&#8713;A' href='#'><span class='ipp-badge black operator pull-left'>a&#8713;A</span>Not In Set</a></li>" +
                             "</ul>";
                             
               var callback=function(e,ui){
                      var myOperator=$("a",ui.item).attr("value"),
                          meta=$opBadge.attr("data-meta-head");
                          pattern=/(\|)(!=|<|>|=|a∈A|a∉A)(\|)/,
                          colHeaders=instance.getSettings().colHeaders;
                      
                      if(pattern.test(meta)===false){
                        console.log("$opBadge.popover.content:Bad pattern in header meta-data");
                      } 
                      meta=meta.replace(pattern, '|' + myOperator + '|');
                      colHeaders.splice(col,1,meta);
                      $opBadge.attr("data-meta-head",meta);
                      $opBadge.text(myOperator);
                      e.stopPropagation();
               };
               var position={  my: "left top",at: "left bottom", of: $opBadge};
               var mnuFacInstance=menuFactory(position);
               mnuFacInstance.getMenu(th,menuString,callback);
            return $span;
          }
    }
    return chFactory;
  }
);