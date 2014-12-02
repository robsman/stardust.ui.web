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
                  "bpm-modeler/js/m_constants", "bpm-modeler/js/m_routeDefinitionUtils" ],
         function(m_utils, m_i18nUtils, m_globalVariables, m_constants, m_routeDefinitionUtils)
         {
            return {
               createRouteForEmail : function(mailIntegrationOverlay)
               {
                  return createRouteForEmail(mailIntegrationOverlay);
               }
            };

            /**
             * Generate route definition for email App
             */
            function createRouteForEmail(mailIntegrationOverlay)
            {
               var mailRouteDefinitionHandler = new MailRouteDefinitionHandler();
               var route = "";
               var includeAttachmentBean = mailRouteDefinitionHandler.includeAttachmentBean(mailIntegrationOverlay
                        .getApplication().contexts.application.accessPoints);
               
               // process template configuration
               route += "<to uri=\"bean:documentHandler?method=processTemplateConfigurations\"/>\n";
               
               if(mailIntegrationOverlay.templateSourceSelect.val() == "classpath"
                  || mailIntegrationOverlay.templateSourceSelect.val() == "repository")
               {
                  route += mailRouteDefinitionHandler.createRouteForRepositoryOrClassPathContent(mailIntegrationOverlay, mailRouteDefinitionHandler);
                  
               } else
               {
                  route += mailRouteDefinitionHandler.createRouteForEmbeddedOrDataContent(mailIntegrationOverlay, mailRouteDefinitionHandler);
               }
               
               // set content type
               route += "<setHeader headerName=\"contentType\">\n";
               route += "   <constant>" + mailIntegrationOverlay.mailFormatSelect.val()
                        + "</constant>\n";
               route += "</setHeader>\n";
               
               // add attachment document
               if (includeAttachmentBean)
                  route += "<to uri=\"bean:documentHandler?method=toAttachment\"/>\n";
               
               // execute smpt endpoint
               route += "<to uri=\"" + mailIntegrationOverlay.protocolSelect.val() + "://"
                        + mailIntegrationOverlay.serverInput.val();
               if (!m_utils.isEmptyString(mailIntegrationOverlay.userInput.val())
                        && !m_utils.isEmptyString(mailIntegrationOverlay.passwordInput.val()))
               {
                  route += "?username=" + mailIntegrationOverlay.userInput.val();
                  route += "&amp;password=" + mailIntegrationOverlay.passwordInput.val();
               }
               else if (!m_utils.isEmptyString(mailIntegrationOverlay.userInput.val()))
               {
                  route += "?username=" + mailIntegrationOverlay.userInput.val();
               }
               route += "\"/>";
               
               // store attachments
               if(mailIntegrationOverlay.storeAttachmentsInput.prop("checked")){
                  route += "<to uri=\"bean:documentHandler?method=storeExchangeAttachments\"/>\n";
               }
               // store email content
               if(mailIntegrationOverlay.storeEmailInput.prop("checked")){
                  route += "<convertBodyTo type=\"javax.mail.internet.MimeMessage\"/>\n";
                  route += "<setHeader headerName=\"ippDmsDocumentName\">\n";
                  route += "   <simple>$simple{header.subject}.eml</simple>\n";
                  route += "</setHeader>\n";
                  route += "<to uri=\"bean:documentHandler?method=toDocument\"/>";
               }
               return route;
            };
            
            function MailRouteDefinitionHandler()
            {
               
               // create route for Email Repository/ClassPath content 
               MailRouteDefinitionHandler.prototype.createRouteForRepositoryOrClassPathContent = function(mailIntegrationOverlay, mailRouteDefinitionHandler)
               {
                  var route = "";
                  // set headers for email: from, to, cc, bcc, subject
                  if (mailIntegrationOverlay.fromInput.val())
                  {
                     route += mailRouteDefinitionHandler.setHeaderInputForEmailRepositoryOrClassPathMode("from", mailIntegrationOverlay.fromInput.val());
                  }
                  if (mailIntegrationOverlay.toInput.val())
                  {
                     route += mailRouteDefinitionHandler.setHeaderInputForEmailRepositoryOrClassPathMode("to", mailIntegrationOverlay.toInput.val());
                  }
                  if (mailIntegrationOverlay.ccInput.val())
                  {
                     route += mailRouteDefinitionHandler.setHeaderInputForEmailRepositoryOrClassPathMode("cc", mailIntegrationOverlay.ccInput.val());
                  }
                  if (mailIntegrationOverlay.bccInput.val())
                  {
                     route += mailRouteDefinitionHandler.setHeaderInputForEmailRepositoryOrClassPathMode("bcc", mailIntegrationOverlay.bccInput.val());
                  }
                  if(mailIntegrationOverlay.subjectInput.val())
                  {
                     route += mailRouteDefinitionHandler.setHeaderInputForEmailRepositoryOrClassPathMode("subject", mailIntegrationOverlay.subjectInput.val());
                  }
                  
                   route += m_routeDefinitionUtils.createTemplatingHandlerRouteDefinition(
                            "text", mailIntegrationOverlay.templateSourceSelect.val(), null,
                            mailIntegrationOverlay.templatePathInput.val(), null, false);
                   
                   return route;
               };
               
               // create route for Email Embedded/Data content 
               MailRouteDefinitionHandler.prototype.createRouteForEmbeddedOrDataContent = function(mailIntegrationOverlay, mailRouteDefinitionHandler)
               {
                  var route = "";
                  // set headers for email: from, to, cc, bcc, subject
                  if (mailIntegrationOverlay.fromInput.val())
                  {
                     route += mailRouteDefinitionHandler.setHeaderInputForEmailEmbeddedOrDataMode("from", mailIntegrationOverlay.fromInput.val());
                  }
                  if (mailIntegrationOverlay.toInput.val())
                  {
                     route += mailRouteDefinitionHandler.setHeaderInputForEmailEmbeddedOrDataMode("to", mailIntegrationOverlay.toInput.val());
                  }
                  if (mailIntegrationOverlay.ccInput.val())
                  {
                     route += mailRouteDefinitionHandler.setHeaderInputForEmailEmbeddedOrDataMode("cc", mailIntegrationOverlay.ccInput.val());
                  }
                  if (mailIntegrationOverlay.bccInput.val())
                  {
                     route += mailRouteDefinitionHandler.setHeaderInputForEmailEmbeddedOrDataMode("bcc", mailIntegrationOverlay.bccInput.val());
                  }
                  if(mailIntegrationOverlay.subjectInput.val())
                  {
                     route += mailRouteDefinitionHandler.setHeaderInputForEmailEmbeddedOrDataMode("subject", mailIntegrationOverlay.subjectInput.val());
                  }
                  
                  // convert to native object before JS execution
                  route += "<to uri=\"ipp:data:toNativeObject\"/>\n";
                
                  // generate js route
                  route += mailRouteDefinitionHandler.createJsRouteForEmail(mailIntegrationOverlay);
                  
                  return route;
               };
               
               MailRouteDefinitionHandler.prototype.setHeaderInputForEmailRepositoryOrClassPathMode = function(Headerkey, HeaderInputContent)
               {
                  var header = "";
                  header += "<choice>\n";
                  header += "  <when>\n";
                  header += "    <simple>$simple{in.header." + Headerkey + "} == null</simple>\n";
                  
                  if(HeaderInputContent.indexOf("{{") != -1 || HeaderInputContent.indexOf("$") != -1)
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
               
               MailRouteDefinitionHandler.prototype.setHeaderInputForEmailEmbeddedOrDataMode = function(Headerkey, HeaderInputContent)
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
               
               MailRouteDefinitionHandler.prototype.createJsRouteForEmail = function(mailIntegrationOverlay)
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
                  route += "        if (MailIntegrationOverlay == 0) return hash;\n";
                  route += "        for (var i = 0; i &lt; MailIntegrationOverlay.length; i++) {\n";
                  route += "           var character = MailIntegrationOverlay.charCodeAt(i);\n";
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
                  for (var n = 0; n < mailIntegrationOverlay.getApplication().contexts.application.accessPoints.length; ++n)
                  {
                     var accessPoint = mailIntegrationOverlay.getApplication().contexts.application.accessPoints[n];
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
                  var markup = CKEDITOR.instances[mailIntegrationOverlay.mailTemplateEditor.id].getData();
                  if (mailIntegrationOverlay.responseTypeSelect.val() != "none")
                  {
                     markup += mailIntegrationOverlay.createResponseOptionString();
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
                  if (mailIntegrationOverlay.templateSourceSelect.val() == "data")
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
                  route += "\n      setOutHeader('response', response);\n";
                  if (mailIntegrationOverlay.identifierInSubjectInput.val() != null
                           && mailIntegrationOverlay.identifierInSubjectInput.prop("checked"))
                  {
                     route += "  setOutHeader('subject', '#ID:' + (partition + '|' + processInstanceOid + '|' + activityInstanceOid).hashCode() + '# - ' + subject);\n";
                  }
                  else
                  {
                     route += "      if (subject){\n";
                     route += "        setOutHeader('subject', subject);\n";
                     route += "      }\n";
                  }
                  route += "      if (to){\n";
                  route += "        setOutHeader('to', to);\n";
                  route += "      }\n";
                  route += "      if (from){\n";
                  route += "        setOutHeader('from', from);\n";
                  route += "      }\n";
                  route += "      if (cc){\n";
                  route += "        setOutHeader('cc', cc);\n";
                  route += "      }\n";
                  route += "      if (bcc){\n";
                  route += "        setOutHeader('bcc', bcc);\n";
                  route += "      }\n";
                  route += "      for (var doc in attachments){\n";
                  route += "        setOutHeader(doc, attachments[doc]);\n";
                  route += "      }\n";
                  route += "   </constant>\n";
                  route += "</setHeader>\n";
                  // execute java sript
                  route += "<to uri=\"language:javascript\"/>\n";
                  
                  // set processed response to body
                  route += "<setBody>\n";
                  route += "   <simple>$simple{in.header.response}</simple>\n";
                  route += "</setBody>\n";
                  
                  return route;
               };
               
               MailRouteDefinitionHandler.prototype.includeAttachmentBean = function(accessPoints)
               {
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
            };
            
         });
