/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC    - initial API and implementation and/or initial documentation
 *    Dmitry Baranovskiy - functions createUUID() and the jQuery function indicated below
 *                         are duplicated from raphael.js version 1.5.2
 *******************************************************************************/
(function () {
    if (Raphael.vml) {
        var reg = / progid:\S+BasicImage\([^\)]+\)/g;
		
        Raphael.el.invert = function (enable) {
			if (this.node != null) {
				var s = this.node.style,
					f = s.filter;
				f = f.replace(reg, "");

				if (enable != "0") {
					s.filter = f + " progid:DXImageTransform.Microsoft.BasicImage(invert=1)";
				} else {
					s.filter = f;
				}
			}
        };
    } else {
		// Function duplicated from raphael.js version 1.5.2
		function createUUID() {
			// http://www.ietf.org/rfc/rfc4122.txt
			var s = [],
				i = 0;
			for (; i < 32; i++) {
				s[i] = (~~(Math.random() * 16)).toString(16);
			}
			s[12] = 4;  // bits 12-15 of the time_hi_and_version field to 0010
			s[16] = ((s[16] & 3) | 8).toString(16);  // bits 6-7 of the clock_seq_hi_and_reserved to 01
			return "r-" + s.join("");
		}
		
		// Function duplicated from raphael.js version 1.5.2
        var $ = function (el, attr) {
            if (attr) {
                for (var key in attr) {
					if (attr.hasOwnProperty(key)) {
						el.setAttribute(key, attr[key]);
					}
				}
            } else {
                el = document.createElementNS("http://www.w3.org/2000/svg", el);
				return el;
            }
        };
		
        Raphael.el.invert = function (enable) {
            if (enable != "0") {
				var fltr = $("filter"),
					componentTransfer = $("feComponentTransfer"),
					funcR = $("feFuncR"),
					funcG = $("feFuncG"),
					funcB = $("feFuncB");
				fltr.id = createUUID();
				
				$(componentTransfer, {});
				$(funcR, {type: "table", tableValues: "1 0"});
				$(funcG, {type: "table", tableValues: "1 0"});
				$(funcB, {type: "table", tableValues: "1 0"});
				componentTransfer.appendChild(funcR);
				componentTransfer.appendChild(funcG);
				componentTransfer.appendChild(funcB);
				
				fltr.appendChild(componentTransfer);
				
				this.paper.defs.appendChild(fltr);
				this._desaturate = fltr;
				$(this.node, {filter: "url(#" + fltr.id + ")"});
            } else {
                if (this._desaturate) {
                    this._desaturate.parentNode.removeChild(this._desaturate);
                    delete this._desaturate;
                }
                this.node.removeAttribute("filter");
            }
        };
    }
})();