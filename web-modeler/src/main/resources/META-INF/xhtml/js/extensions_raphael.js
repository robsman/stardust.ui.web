/**
 * @author Omkar.Patil
 */
/* connector function for Raphael */
define(["raphael"], function(Raphael){
	Raphael.fn.connection = function(obj1, obj2, line, bg, connectorType){
		
	    if (obj1.line && obj1.from && obj1.to) {
	        line = obj1;
	        obj1 = line.from;
	        obj2 = line.to;
	    }
	    
	    /* My STRAIGHT LINE ... START */
	    var bb1 = obj1.getBBox(), bb2 = obj2.getBBox(), bb1CentreX = bb1.x + bb1.width / 2, bb1CentreY = bb1.y + bb1.height / 2, bb2CentreX = bb2.x + bb2.width / 2, bb2CentreY = bb2.y + bb2.height / 2, angle = Raphael.angle(bb1CentreX, bb1CentreY, bb2CentreX, bb2CentreY), x1, y1, x2, y2;
	    //Logger.log("Angle = ", angle);    
	    
	    if (angle >= 45 && angle < 135) {
	        x1 = bb1.x + bb1.width / 2, y1 = bb1.y - 1, x2 = bb2.x + bb2.width / 2, y2 = bb2.y + bb2.height;
	    }
	    else 
	        if (angle >= 135 && angle < 225) {
	            x1 = bb1.x + bb1.width - 1, y1 = bb1.y + bb1.height / 2, x2 = bb2.x - 1, y2 = bb2.y + bb2.height / 2;
	        }
	        else 
	            if (angle >= 225 && angle < 315) {
	                x1 = bb1.x + bb1.width / 2, y1 = bb1.y + bb1.height - 1, x2 = bb2.x + bb2.width / 2, y2 = bb2.y - 1;
	            }
	            else {
	                x1 = bb1.x - 1, y1 = bb1.y + bb1.height / 2, x2 = bb2.x + bb2.width - 1, y2 = bb2.y + bb2.height / 2;
	            }
	    
	    var path = ["M", x1, y1, "L", x2, y2].join(",");
	    
	    var arrowAngle = Math.atan2(x1 - x2, y2 - y1);
	    arrowAngle = (arrowAngle / (2 * Math.PI)) * 360;
	    var size = 8;
	    var arrowPath = ["M", x2, y2, "L", (x2 - size * 3/4), (y2 - size * 3/4), "L", (x2 - size * 3/4), (y2 + size * 3/4), "L", x2, y2].join(",");
	    
	    if (line && line.line) {
			line.bg &&
			line.bg.attr({
				path: path
			});
			line.line.attr({
				path: path
			});
			if(typeof line.arrow != 'undefined') {
				line.arrow.attr({
					path: arrowPath,
					"fill": "#107F7B"
				}).rotate((90 + arrowAngle), x2, y2);
			}
		}
		else {
			var color = typeof line == "string" ? line : "#107F7B";
			var color =  connectorType != "connector" ? "#D6D6D6" : color;
			var connector = {
				bg: bg && bg.split &&
				this.path(path).attr({
					stroke: bg.split("|")[0],
					fill: "none",
					"stroke-width": bg.split("|")[1] || 3
				}),
				line: this.path(path).attr({
					stroke: color,
					fill: "none"
				}),
				from: obj1,
				to: obj2,
				customProps: {}
			};
			
			if(connectorType != "roleassoc") {
				var fillColor = connectorType == "dataassoc" ? "#D6D6D6" : "#107F7B"; 
				connector.arrow = this.path(arrowPath).attr("fill", fillColor).rotate((90 + arrowAngle), x2, y2);
			}
			
			connector.customProps.remove = function() {
				connector.arrow.remove();
				connector.line.remove();
				connector.bg.remove();
			};
			
			/*jQuery(connector.line.node).click(function(e){
				jQuery(document).trigger('CONNECTOR_SELECTED', {
					element: connector
				});
			});
			
			jQuery(connector.arrow.node).click(function(e){
				jQuery(document).trigger('CONNECTOR_SELECTED', {
					element: connector
				});
			});*/
			
			return connector;
		}
    }
    /* My STRAIGHT LINE ... END */
	
	return {};
});
