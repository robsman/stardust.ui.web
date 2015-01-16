/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies SmsIntegrationOverlay distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * Helper functions for Sms route generation
 *
 */
define(
         [ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_globalVariables",
                  "bpm-modeler/js/m_constants", "bpm-modeler/js/m_routeDefinitionUtils" ],
         function(m_utils, m_i18nUtils, m_globalVariables, m_constants, m_routeDefinitionUtils)
         {
            return {
               createRouteForSms : function(smsIntegrationOverlay)
               {
                  var handler = new SmsRouteDefinitionHandler();
                  return handler.createRouteForSms(smsIntegrationOverlay);
               }
            };
            
            
            function SmsRouteDefinitionHandler()
            {
               
               /**
                * Generate route definition for sms App
                */
               SmsRouteDefinitionHandler.prototype.createRouteForSms = function(smsIntegrationOverlay)
               {
					  var route = "<to uri=\"ipp:data:toNativeObject\"/>\n";

					  route += this.setHeaderInputForSms("CamelSmppDestAddr", smsIntegrationOverlay.destinationAddressInput.val());
					  route += this.setHeaderInputForSms("CamelSmppSourceAddr", smsIntegrationOverlay.sourceAddressInput.val());
					  
					  route += "<setHeader headerName=\"CamelLanguageScript\">\n";
					  route += "   <constant>\n";
					  route += "function setOutHeader(key, output){\nexchange.out.headers.put(key,output);}\n";
					  route += "function formatDate(format,value){\n  return new java.text.SimpleDateFormat(format).format(value);}\n";
					  route += "function isArray(obj) {\n\tif (Array.isArray) {\n\t\treturn Array.isArray(obj);\n\t} else {\n\treturn Object.prototype.toString.call(obj) === '[object Array]';\n\t}\n}\n";
					  
					  route += "function visitMembers(obj, callback) {\n\tvar i = 0, length = obj.length;\n\tif (isArray(obj)) {\n\t\t";
					  route += "for(; i &lt; length; i++) {\n\t\tobj[i]= callback(i, obj[i]);\n\t\t}\n";
					  route += "} else {\n\t\tfor (i in obj) {\n\t\tobj[i]=  callback(i, obj[i]);}\n\t}\n\treturn obj;\n}\n";
					  
					  route += "function recursiveFunction(key, val) {\n";
					  route += "\tif (val instanceof Object || isArray(val)) {\n";
					  route += "\t\treturn visitMembers(val, recursiveFunction);\n";
					  route += "\t} else {\n";
					  route += "\t\treturn actualFunction(val, typeof val);\n";
					  route += "\t}\n";
					  route += "}\n";
					  
					  route += "function actualFunction(value, type) {\n";
					  route += "\tvar dataAsLong;\n";
					  route += "\tif (type === 'string') {\n";
					  route += "\t\tdataAsLong =new RegExp(/\\/Date\\((-?\\d*)\\)\\//).exec(value);\n";
					  route += "\tif (dataAsLong) {\n";
					  route += "\t\treturn new java.util.Date(+dataAsLong[1]);\n";
					  route += "\t}\n";
					  route += "}\n";
					  route += "return value;\n";
					  route += "}\n";

					  route += "     String.prototype.hashCode = function() {";
					  route += "        var hash = 0;\n";
					  route += "        if (this == 0) return hash;\n";
					  route += "        for (var i = 0; i &lt; this.length; i++) {\n";
					  route += "           var character = this.charCodeAt(i);\n";
					  route += "           hash = ((hash&lt;&lt;5)-hash)+character;\n";
					  route += "           hash = hash &amp; hash;\n";
					  route += "        }\n";
					  route += "        return hash;\n";
					  route += "     }\n";
					  for ( var n = 0; n < smsIntegrationOverlay.getApplication().contexts.application.accessPoints.length; ++n) {

						 var accessPoint = smsIntegrationOverlay.getApplication().contexts.application.accessPoints[n];

						 if (accessPoint.direction == m_constants.OUT_ACCESS_POINT) {
							continue;
						 }

						 if (accessPoint.dataType == "primitive") {
							if ((accessPoint.id != "CamelSmppDestAddr")&&(accessPoint.id != "CamelSmppSourceAddr")){
								route += "var " + accessPoint.id + ";\n";
								route += "if(request.headers.get('"
									  + accessPoint.id + "')!=null){\n";
								route += accessPoint.id
									  + " =  request.headers.get('"
									  + accessPoint.id + "');\n";
								route +=  "setOutHeader('"+accessPoint.id+"',"+ accessPoint.id+");\n";
								route += "}\n";
							}else{
								route += "var " + accessPoint.id + " = request.headers.get('"+ accessPoint.id + "');\n";
								route += "if(request.headers.get('"+ accessPoint.id + "')!=null){\n";
								route += "<![CDATA[\n";						
								route += "if("+accessPoint.id+" && "+accessPoint.id+".indexOf(\"" + "'" + "\") === 0){\n";
								route += ""+accessPoint.id+"= eval('(' + "+accessPoint.id+"+ ')');\n";
								route +=  "setOutHeader('"+accessPoint.id+"',"+ accessPoint.id+");\n";
								route += "} else {\n";
								route +=  "setOutHeader('"+accessPoint.id+"',"+ accessPoint.id+");\n";
								route += "}\n";											  
								route += "]]>";
								route += "}\n";
							}

						 } else if (accessPoint.dataType == "struct") {
							
							route += "var " + accessPoint.id + ";\n";
							route += "if(request.headers.get('"
								  + accessPoint.id + "')!=null){\n";
							route += accessPoint.id
								  + " =  eval('(' + request.headers.get('"
								  + accessPoint.id + "')+ ')');\n";
							route +=  accessPoint.id+"=visitMembers("+accessPoint.id+", recursiveFunction);\n";
							route +=  "setOutHeader('"+accessPoint.id+"',"+ accessPoint.id+");\n";
							route += "}\n";
							
						 }
						 }
					  route += "\n";
					  
					  var messageContent = smsIntegrationOverlay.codeEditor.getEditor().getSession().getValue();
					  if (messageContent != null && messageContent != "")
					  {
						 messageContent = m_utils.encodeXmlPredfinedCharacters(messageContent);
					  }
					  route+="<![CDATA[";
					  route += "      response = '"
							+ messageContent.replace(new RegExp("\n", 'g'), " ")
								  .replace(new RegExp("toDate", 'g'), "formatDate")
								  .replace(new RegExp("{{", 'g'), "' + ")
								  .replace(new RegExp("}}", 'g'), " + '")
							+ "';\n";
					  route+="]]>";
					  route += "      setOutHeader('response', response);\n";
					  route += "   </constant>\n";
					  route += "</setHeader>\n";

					  route += "<to uri=\"language:javascript\"/>\n";
					  route += "<setBody>\n";
					  route += "   <simple>$simple{in.header.response}</simple>\n";
					  route += "</setBody>\n";
					  route +="<to uri=\"smpp://"+smsIntegrationOverlay.userNameInput.val()+"@"+smsIntegrationOverlay.hostNameInput.val()+":"+smsIntegrationOverlay.portInput.val()+"?lazySessionCreation=true&amp;password=";
					  
					  if(smsIntegrationOverlay.useCVforPassowrdInput.prop("checked"))
					  {
						 route += "${";
						 route += smsIntegrationOverlay.passowrdInput.val();
						 route += ":Password}";
					  } else
					  {
						 route += smsIntegrationOverlay.passowrdInput.val();
					  }
					route +="\"/>";

                  return route;
               };
               
			    SmsRouteDefinitionHandler.prototype.setHeaderInputForSms = function(Headerkey, HeaderInputContent)
               {
                  var header = "";
                  header += "<choice>\n";
                  header += "  <when>\n";
                  header += "    <simple>$simple{in.header." + Headerkey + "} == null</simple>\n";
                  
                  if(HeaderInputContent.indexOf("$") != -1)
                  {
                     header += m_routeDefinitionUtils
                              .createTemplatingHandlerRouteDefinition("text", "embedded",
                                       HeaderInputContent, null, null, false);
                     header += "    <setHeader headerName=\"" + Headerkey + "\">\n";
                     header += "       <simple>'$simple{body}'</simple>\n";

                  } else
                  {
                   header += "    <setHeader headerName=\"" + Headerkey + "\">\n";
                   header += "        <simple>'"
                            + HeaderInputContent.replace(new RegExp("\n", 'g'), " ")
                                           .replace(new RegExp("toDate", 'g'), "formatDate")
                                           .replace(new RegExp("{{", 'g'), "' + ").replace(
                                                    new RegExp("}}", 'g'), " + '")
                            + "'</simple>\n";
                  }
                  
                  header += "    </setHeader>\n";
				 
                  header += "  </when>\n";
                  header += "</choice>\n";
                  return header;
               };
			   


            };
            
         });
