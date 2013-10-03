define(["jquery","bootstrap","./m_images"],function($,bootstrap,images){
    var chFactory={
    		depTest: function(){console.log();},
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
              $opBadge.attr("data-meta-head",meta)
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
              /*Build Popover menu for operator selection*/
              popover=$opBadge.popover({
                html:true,
                title:"<b>Select Operator</b>",
                container: 'body',
                content:function(){
                  var $well=$("<div class='well'></div>"),
                      $eq=$("<div class='pointy'><span class='ipp-badge black operator'>=</span> Equals</div><p/>"),
                      $gt=$("<div class='pointy'><span class='ipp-badge black operator'>></span> Greater Than</div><p/>"),
                      $lt=$("<div class='pointy'><span class='ipp-badge black operator'><</span> Less Than</div></p>"),
                      $not=$("<div class='pointy'><span class='ipp-badge black operator'>!=</span> Not</div></p>"),
                      $inSet=$("<div class='pointy'><span class='ipp-badge black operator'>a&#8712;A</span> In Set</div></p>"),
                      $NotiInSet=$("<div class='pointy'><span class='ipp-badge black operator'>a&#8713;A</span>Not In Set</div></p>");
                  
                  $([$eq,$gt,$lt,$not,$inSet,$NotiInSet]).each(function(){
                    $well.append(this);
                    $(this).on("click",function(){
                    	
                      var myOperator=$(".operator",this)[0].innerText,
                          meta=$opBadge.attr("data-meta-head")
                          pattern=/(\|)[>,<,!=,=](\|)/,
                          colHeaders=instance.getSettings().colHeaders;
                      
                      if(pattern.test(meta)===false){
                        console.log("$opBadge.popover.content:Bad pattern in header meta-data");
                      } 
                      meta=meta.replace(pattern, '|' + myOperator + '|');
                      colHeaders.splice(col,1,meta);
                      $opBadge.attr("data-meta-head",meta);
                      $opBadge.text(myOperator);
                      $opBadge.popover("hide");
                    });
                  });
                  return $well;
                }
              });
        
              //build our removeCol image and add click handler
              $img=$(images.remove).on("click",function(){
                    var settings=instance.getSettings();
                    settings.helperFunctions.removeColumn(instance,col);
                  }
                );
            
            //$span.append($img);
            return $span;
          }
    }
    return chFactory;
  }
);