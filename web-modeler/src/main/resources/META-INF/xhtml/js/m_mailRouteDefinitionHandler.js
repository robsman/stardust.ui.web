/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies MailIntegrationOverlay distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * Helper functions for Email route generation
 *
 * @author sabri.bousselmi
 */
define(
         [ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_globalVariables",
                  "bpm-modeler/js/m_constants", "bpm-modeler/js/m_routeDefinitionUtils","bpm-modeler/js/MailIntegrationOverlayTestTabHandler","bpm-modeler/js/MailIntegrationOverlayResponseTabHandler" ],
         function(m_utils, m_i18nUtils, m_globalVariables, m_constants, m_routeDefinitionUtils,mailIntegrationOverlayTestTabHandler,mailIntegrationOverlayResponseTabHandler)
         {
            return {
               createRouteForEmail : function(attributes,accessPoints)
               {
                  var handler = new MailRouteDefinitionHandler();
                  return handler.createRouteForEmail(attributes,accessPoints);
               }
            };
            
            
            function MailRouteDefinitionHandler()
            {
               
               /**
                * Generate route definition for email App
                */
               MailRouteDefinitionHandler.prototype.createRouteForEmail = function(attributes,accessPoints)
               {
                  if(!attributes["stardust:emailOverlay::protocol"])
                     attributes["stardust:emailOverlay::protocol"]="smtp";
                  if(!attributes["stardust:emailOverlay::mailFormat"])
                     attributes["stardust:emailOverlay::mailFormat"]="text/plain";
                  var route = "";
                  var includeAttachmentBean = this.includeAttachmentBean(attributes,accessPoints);
                  
                  if(this.includeProcessTemplateConfigurations(attributes,accessPoints))
                  {
                    // process template configuration
                      route += "<to uri=\"bean:documentHandler?method=processTemplateConfigurations\"/>\n";
                  }
                  
                  if(attributes["stardust:emailOverlay::templateSource"]  == "classpath"
                     || attributes["stardust:emailOverlay::templateSource"]  == "repository")
                  {
                     route += this.createRouteForRepositoryOrClassPathContent(attributes,accessPoints);
                     
                  } else
                  {
                     route += this.createRouteForEmbeddedOrDataContent(attributes,accessPoints);
                  }
                  
                  // set content type
                  route += "<setHeader headerName=\"contentType\">\n";
                  route += "   <constant>" + attributes["stardust:emailOverlay::mailFormat"]
                           + "; charset=\"utf-8\"</constant>\n";
                  route += "</setHeader>\n";
                  
                  // add attachment document
                  if (includeAttachmentBean)
                     route += "<to uri=\"bean:documentHandler?method=toAttachment\"/>\n";
                  
                  // execute smpt endpoint
                  route += "<to uri=\"" + attributes["stardust:emailOverlay::protocol"] + "://"+ attributes["stardust:emailOverlay::server"];
                  if (!m_utils.isEmptyString(attributes["stardust:emailOverlay::user"])
                           && !m_utils.isEmptyString(attributes["stardust:emailOverlay::pwd"]))
                  {
                     route += "?username=" + attributes["stardust:emailOverlay::user"];
                     route += "&amp;password=" + attributes["stardust:emailOverlay::pwd"];
                  }
                  else if (!m_utils.isEmptyString(attributes["stardust:emailOverlay::user"]))
                  {
                     route += "?username=" + attributes["stardust:emailOverlay::user"];
                  }
                  route += "\"/>";
                  
                  // store attachments
                  if(attributes["stardust:emailOverlay::storeAttachments"]){
                     route += "<to uri=\"bean:documentHandler?method=storeExchangeAttachments\"/>\n";
                  }
                  // store email content
                  if(attributes["stardust:emailOverlay::storeEmail"]){
                     route += "<convertBodyTo type=\"javax.mail.internet.MimeMessage\"/>\n";
                     route += "<setHeader headerName=\"ippDmsDocumentName\">\n";
                     route += "   <simple>$simple{header.subject}.eml</simple>\n";
                     route += "</setHeader>\n";
                     route += "<to uri=\"bean:documentHandler?method=toDocument\"/>";
                  }
                  return route;
               };
               
               MailRouteDefinitionHandler.prototype.createRouteForRepositoryOrClassPathContent = function(attributes,accessPoints)
               {
                  var route = "";
                  // set headers for email: from, to, cc, bcc, subject
                  if (attributes["stardust:emailOverlay::from"])
                  {
                     route += this.setHeaderInputForEmailRepositoryOrClassPathMode("from", attributes["stardust:emailOverlay::from"]);
                  }
                  if (attributes["stardust:emailOverlay::to"])
                  {
                     route += this.setHeaderInputForEmailRepositoryOrClassPathMode("to", attributes["stardust:emailOverlay::to"]);
                  }
                  if (attributes["stardust:emailOverlay::cc"])
                  {
                     route += this.setHeaderInputForEmailRepositoryOrClassPathMode("cc", attributes["stardust:emailOverlay::cc"]);
                  }
                  if (attributes["stardust:emailOverlay::bcc"])
                  {
                     route += this.setHeaderInputForEmailRepositoryOrClassPathMode("bcc", attributes["stardust:emailOverlay::bcc"]);
                  }
                  if(attributes["stardust:emailOverlay::subject"])
                  {
                     route += this.setHeaderInputForEmailRepositoryOrClassPathMode("subject", attributes["stardust:emailOverlay::subject"]);
                  }
                  
                   route += m_routeDefinitionUtils.createTemplatingHandlerRouteDefinition(
                            "text", attributes["stardust:emailOverlay::templateSource"] , null,
                            attributes["stardust:emailOverlay::templatePath"], null, false);
                   
                   return route;
               };
               
               // create route for Email Embedded/Data content 
               MailRouteDefinitionHandler.prototype.createRouteForEmbeddedOrDataContent = function(attributes,accessPoints)
               {
                  var route = "";
                  // set headers for email: from, to, cc, bcc, subject
                  if (attributes["stardust:emailOverlay::from"])
                  {
                     route += this.setHeaderInputForEmailEmbeddedOrDataMode("from", attributes["stardust:emailOverlay::from"]);
                  }
                  if (attributes["stardust:emailOverlay::to"])
                  {
                     route += this.setHeaderInputForEmailEmbeddedOrDataMode("to", attributes["stardust:emailOverlay::to"]);
                  }
                  if (attributes["stardust:emailOverlay::cc"])
                  {
                     route += this.setHeaderInputForEmailEmbeddedOrDataMode("cc",attributes["stardust:emailOverlay::cc"]);
                  }
                  if (attributes["stardust:emailOverlay::bcc"])
                  {
                     route += this.setHeaderInputForEmailEmbeddedOrDataMode("bcc", attributes["stardust:emailOverlay::bcc"]);
                  }
                  if(attributes["stardust:emailOverlay::subject"])
                  {
                     route += this.setHeaderInputForEmailEmbeddedOrDataMode("subject", attributes["stardust:emailOverlay::subject"]);
                  }
                  
                  // convert to native object before JS execution
                  route += "<to uri=\"ipp:data:toNativeObject\"/>\n";
                
                  // generate js route
                  route += this.createJsRouteForEmail(attributes,accessPoints);
                  
                  return route;
               };
               
               MailRouteDefinitionHandler.prototype.setHeaderInputForEmailRepositoryOrClassPathMode = function(Headerkey, HeaderInputContent)
               {
                  var header = "";
                  header += "<choice>\n";
                  header += "  <when>\n";
                  header += "    <simple>$simple{in.header." + Headerkey + "} == null</simple>\n";
                  
                  if(this.isEmailTemplateVariable(HeaderInputContent) || this.isVelocityVariable(HeaderInputContent))
                  {
                     HeaderInputContent = HeaderInputContent.replace(new RegExp("\n", 'g'), " ")
                                                      .replace(new RegExp("toDate", 'g'), "formatDate")
                                                      .replace(new RegExp("{{", 'g'), "$").replace(
                                                               new RegExp("}}", 'g'), "");
                     
                     header += m_routeDefinitionUtils
                              .createTemplatingHandlerRouteDefinition("text", "embedded",
                                       HeaderInputContent, null, null, false);
                     header += "<convertBodyTo type=\"java.lang.String\"/>\n";
                     header += "    <setHeader headerName=\"" + Headerkey + "\">\n";
                     header += "       <simple>$simple{body}</simple>\n";

                  } else
                  {
                     header += "    <setHeader headerName=\"" + Headerkey + "\">\n";
                     header += "        <constant>"
                              +                HeaderInputContent
                              +        "</constant>\n";
                  }
                  
                  header += "    </setHeader>\n";
                  header += "  </when>\n";
                  header += "</choice>\n";
                  return header;
               };
               /**
                * Return true if it is a variable data that will be used by templating engine
                */
               MailRouteDefinitionHandler.prototype.isEmailTemplateVariable = function(HeaderInputContent)
               {
                  return HeaderInputContent.startsWith('{{')&& HeaderInputContent.endsWith("}}");
               }
               
               /**
                * Return true if it is a variable data that will be used by templating engine
                */
               MailRouteDefinitionHandler.prototype.isVelocityVariable = function(HeaderInputContent)
               {
                  return HeaderInputContent.startsWith('$')&& !this.isConfigurationVariable(HeaderInputContent);
               }
               
               /**
                * Return true if it is a Configuration variable (starts with ${)
                */
               MailRouteDefinitionHandler.prototype.isConfigurationVariable = function(HeaderInputContent)
               {
                  HeaderInputContent=HeaderInputContent.trim();
                  return HeaderInputContent.startsWith("${") && HeaderInputContent.endsWith("}");
               }
               
               MailRouteDefinitionHandler.prototype.setHeaderInputForEmailEmbeddedOrDataMode = function(Headerkey, HeaderInputContent)
               {
                  var header = "";
                  header += "<choice>\n";
                  header += "  <when>\n";
                  header += "    <simple>$simple{in.header." + Headerkey + "} == null</simple>\n";
                  
                  if(this.isVelocityVariable(HeaderInputContent) )
                  {
                     header += m_routeDefinitionUtils
                              .createTemplatingHandlerRouteDefinition("text", "embedded",
                                       HeaderInputContent, null, null, false);
                     header += "    <setHeader headerName=\"" + Headerkey + "\">\n";
                     header += "       <simple>'$simple{body}'</simple>\n";

                  } else
                  {
                   header += "    <setHeader headerName=\"" + Headerkey + "\">\n";
                   header += "        <constant>'"
                            + HeaderInputContent.replace(new RegExp("\n", 'g'), " ")
                                           .replace(new RegExp("toDate", 'g'), "formatDate")
                                           .replace(new RegExp("{{", 'g'), "' + ").replace(
                                                    new RegExp("}}", 'g'), " + '")
                            + "'</constant>\n";
                  }
                  
                  header += "    </setHeader>\n";
                  header += "  </when>\n";
                  header += "</choice>\n";
                  return header;
               };
               
               MailRouteDefinitionHandler.prototype.createJsRouteForEmail = function(attributes,accessPoints)
               {
                  var route = "";
                  route += "<setHeader headerName=\"CamelLanguageScript\">\n";
                  route += "   <constant>\n";
                  route += "function setOutHeader(key, output){\n\tif (output &amp;&amp; output != 'undefined') {\n\t exchange.out.headers.put(key,output);\n\t}\n}\n";
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
                  route += "var processInstanceOid = request.headers.get('ippProcessInstanceOid');\n";
                  route += "var activityInstanceOid = request.headers.get('ippActivityInstanceOid');\n";
                  route += "var partition = request.headers.get('ippPartition');\n";
                  route += "var investigate = false;\n";
                  route += "var attachments = {};\n";
                  route += "exchange.out.attachments=request.attachments;\n";
                  for (var n = 0; n < accessPoints.length; ++n)
                  {
                     var accessPoint = accessPoints[n];
                     if (accessPoint.direction == m_constants.OUT_ACCESS_POINT)
                     {
                        continue;
                     }
                     if (accessPoint.dataType == "primitive")
                     {
                        route += "var " + accessPoint.id + ";\n";
                        route += "if(request.headers.get('" + accessPoint.id
                                 + "')!=null){\n";
                        route += accessPoint.id + " =  request.headers.get('"
                                 + accessPoint.id + "');\n";
                        route += "}\n";
                     }
                     else if (accessPoint.dataType == "struct")
                     {
                        route += "var " + accessPoint.id + ";\n";
                        route += "if(request.headers.get('" + accessPoint.id
                                 + "')!=null){\n";
                        route += accessPoint.id + " =  eval('(' + request.headers.get('"
                                 + accessPoint.id + "')+ ')');\n";
                        route += accessPoint.id + "=visitMembers(" + accessPoint.id
                                 + ", recursiveFunction);\n";
                        route += "}\n";
                     }
                     else if (accessPoint.dataType == "dmsDocument")
                     {
                        route += "var " + accessPoint.id + ";\n";
                        route += "if(request.headers.get('" + accessPoint.id
                                 + "')!=null){\n";
                        route += accessPoint.id + " =  request.headers.get('"
                                 + accessPoint.id + "');\n";
                        route += "attachments[" + accessPoint.id + "]"
                                 + " =  request.headers.get('" + accessPoint.id + "');\n";
                        route += "}\n";
                     }
                  }
                  route += "\n";
                  var markup = attributes["stardust:emailOverlay::mailTemplate"];//CKEDITOR.instances[mailIntegrationOverlay.mailTemplateEditor.id].getData();
                  if (attributes["stardust:emailOverlay::responseType"] != "none")
                  {
                     markup += mailIntegrationOverlayTestTabHandler.createResponseOptionString(attributes["stardust:emailOverlay::responseType"],attributes["stardust:emailOverlay::responseOptionType"],attributes["stardust:emailOverlay::responseHttpUrl"] );
                  }
                  markup = markup.replace(new RegExp("(&#39;)", 'g'), "\\'");
                  markup = markup.replace(new RegExp("(&amp;)", 'g'), "&");
                  markup = markup.replace(new RegExp("(&quot;)", 'g'), "\"");
                  route += "<![CDATA[\n";
                  route += "if(subject && subject.indexOf(\"" + "'" + "\") === 0){\n";
                  route += "var subject= eval('(' + subject+ ')');\n";
                  route += "} else {\n";
                  route += "var subject= eval(subject);\n";
                  route += "}\n";
                  route += "if(to && to.indexOf(\"" + "'" + "\") === 0){\n";
                  route += "var to= eval('(' + to+ ')');\n";
                  route += "} else {\n";
                  route += "var to= eval(to);\n";
                  route += "}\n";
                  route += "if(from && from.indexOf(\"" + "'" + "\") === 0){\n";
                  route += "var from= eval('(' + from+ ')');\n";
                  route += "} else {\n";
                  route += "var from= eval(from);\n";
                  route += "}\n";
                  route += "if(cc && cc.indexOf(\"" + "'" + "\") === 0){\n";
                  route += "var cc= eval('(' + cc+ ')');\n";
                  route += "} else {\n";
                  route += "var cc= eval(cc);\n";
                  route += "}\n";
                  route += "if(bcc && bcc.indexOf(\"" + "'" + "\") === 0){\n";
                  route += "var bcc= eval('(' + bcc+ ')');\n";
                  route += "} else {\n";
                  route += "var bcc= eval(bcc);\n";
                  route += "}\n";
                  route += "\nvar response=' ';\n";
                  if (attributes["stardust:emailOverlay::templateSource"] == "data")
                  {
                     route += "if(mailContentAP){\n";
                     route += "      response = String(mailContentAP);";
                     route += "\n}";
                  }
                  else
                  {// embedded
                     route += "      response = '"
                              + markup.replace(new RegExp("\n", 'g'), " ").replace(
                                       new RegExp("toDate", 'g'), "formatDate").replace(
                                       new RegExp("{{", 'g'), "' + ").replace(
                                       new RegExp("}}", 'g'), " + '") + "';\n";
                  }
                  route += "]]>";
                 route += "\nsetOutHeader('response', response.toString());\n";
                  if (attributes["stardust:emailOverlay::includeUniqueIdentifierInSubject"] != null
                           && attributes["stardust:emailOverlay::includeUniqueIdentifierInSubject"]==true)
                  {
                     route += "  setOutHeader('subject', '#ID:' + (partition + '|' + processInstanceOid + '|' + activityInstanceOid).hashCode() + '# - ' + subject);\n";
                  }
                  else
                  {
                     route += "      if (subject){\n";
                     route += "        setOutHeader('subject', subject.toString());\n";
                     route += "      }\n";
                  }
                  route += "      if (to){\n";
                  route += "        setOutHeader('to', to.toString());\n";
                  route += "      }\n";
                  route += "      if (from){\n";
                  route += "        setOutHeader('from', from.toString());\n";
                  route += "      }\n";
                  route += "      if (cc){\n";
                  route += "        setOutHeader('cc', cc.toString());\n";
                  route += "      }\n";
                  route += "      if (bcc){\n";
                  route += "        setOutHeader('bcc', bcc.toString());\n";
                  route += "      }\n";
                  route += "      for (var doc in attachments){\n";
                  route += "        setOutHeader(doc, attachments[doc]);\n";
                  route += "      }\n";
                  route += "   </constant>\n";
                  route += "</setHeader>\n";
                  // execute java sript
                  route += "<to uri=\"language:rhino-nonjdk\"/>\n";
                  
                  // set processed response to body
                  route += "<setBody>\n";
                  route += "   <simple>$simple{in.header.response}</simple>\n";
                  route += "</setBody>\n";
                  
                  // remove processed response from header. CRNT-35370
                  route += "<removeHeaders pattern=\"response\"/>\n";
                  
                  return route;
               };
               
               MailRouteDefinitionHandler.prototype.includeAttachmentBean = function(attributes,accessPoints)
               {
                  if(attributes["stardust:emailOverlay::attachmentsTemplateSource"]=="CORRESPONDENCE")
                     return true;
                  for (var n = 0; n < accessPoints.length; ++n)
                  {
                     var accessPoint = accessPoints[n];
                     if (accessPoint.dataType == m_constants.DOCUMENT_DATA_TYPE)
                     {
                        return true;
                     }
                  }
                  return false;
               };
               
               MailRouteDefinitionHandler.prototype.includeProcessTemplateConfigurations = function(attributes,accessPoints) 
               {
                  var correspondenceAp = m_routeDefinitionUtils.findAccessPoint(accessPoints, "CORRESPONDENCE");
                  if(correspondenceAp)
                     return true;
                  if(!attributes["stardust:emailOverlay::templateConfigurations"])
                     attributes["stardust:emailOverlay::templateConfigurations"]="[]";
                  
                 if ((attributes["stardust:emailOverlay::attachmentsTemplateSource"]== "embedded" || 
                          attributes["stardust:emailOverlay::attachmentsTemplateSource"] == undefined)
                  && JSON.parse(attributes["stardust:emailOverlay::templateConfigurations"]).length == 0)
               return false;
                 
                 return true;
               };
            };
            
         });