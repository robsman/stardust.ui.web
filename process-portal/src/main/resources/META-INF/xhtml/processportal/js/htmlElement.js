/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Subodh.Godbole
 */

define([], function() {
	return {
		create : function(tag, options) {
			return new HtmlElement(tag, options);
		}
	};
	
	/*
	 * 
	 */
	function HtmlElement(tag, options) {
		if (!options) {
			var options = {};
		}

		this.tag = tag;
		this.value = options.value ? options.value : "";
		this.attributes = options.attributes ? options.attributes : {}; /* HTML Attributes, could be added dynamically */
		this.children = options.children ? options.children : [];
		
		if(options.parent) {
			options.parent.children.push(this);
		}
		
		/*
		 * 
		 */
		HtmlElement.prototype.toString = function (indent) {
			if (indent == undefined) {
				indent = 0;
			}

			var tagValue = "";
			if (this.value) {
				tagValue += "Value:" + this.value;
			}
			
			var tagAttributes = "";
			for(var key in this.attributes) {
				tagAttributes += (key + ":" + this.attributes[key]) + " ";
			}
			
			if (tagAttributes.length > 0) {
				tagAttributes = "Attributes: {" + tagAttributes + "}";
			}

			var tagContents = "";
			if (tagValue.length > 0) {
				tagContents += tagValue;
			}
			if (tagAttributes.length > 0) {
				if (tagContents.length > 0) {
					tagContents += ", ";
				}
				tagContents += tagAttributes;
			}
			
			var ret = writeIndent(indent) + this.tag + (tagContents.length > 0 ? " (" + tagContents+ ")" : "");
			ret += "\n";
			
			indent++;
			for(var i = 0; i < this.children.length; i++) {
				ret += this.children[i].toString(indent);
			}
			
			return ret;
		};

		/*
		 * 
		 */
		HtmlElement.prototype.toHtml = function (indent) {
			if (indent == undefined) {
				indent = 0;
			}

			var hasChildren = this.children.length != 0;

			var ret = writeIndent(indent) + writeStartTag(this) + (hasChildren ? "\n" : "");
			indent++;

			if (this.value) {
				if (hasChildren) {
					ret += writeIndent(indent);
				}
				ret += this.value + (hasChildren ? "\n" : "");
			}

			for(var i = 0; i < this.children.length; i++) {
				ret += this.children[i].toHtml(indent);
			}

			indent--;
			if (hasChildren) {
				ret += writeIndent(indent);
			}
			ret += writeEndTag(this);

			return ret;
		};

		/*
		 * 
		 */
		function writeStartTag(elem) {
			var ret = "<" + elem.tag; 
			for(var key in elem.attributes) {
				ret += " " + key + "=\"" + elem.attributes[key] + "\"";
			}
			ret += ">";
			
			return ret;
		}

		/*
		 * 
		 */
		function writeEndTag(elem) {
			return "</" + elem.tag + ">\n";
		}

		/*
		 * 
		 */
		function writeIndent(indent) {
			var str = "";
			for(var i = 0; i < indent; i++) {
				str += "  ";
			}
			return str;
		};
	};
});