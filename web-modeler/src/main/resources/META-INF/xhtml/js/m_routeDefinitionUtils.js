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
 * Helper functions for route definition generation
 *
 * @author
 */
define(
         [ "bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_globalVariables",
                  "bpm-modeler/js/m_constants" ],
         function(m_i18nUtils, m_globalVariables, m_constants)
         {
            return {
               createTemplatingHandlerRouteDefinition : function(format, location,
                        embeddedTemplateContent, templatePath, generateFileOutputName,
                        convertToPdf, defaultInAp)
               {
                  return createTemplatingHandlerRouteDefinition(format, location,
                           embeddedTemplateContent, templatePath, generateFileOutputName,
                           convertToPdf,defaultInAp);
               },
               createRouteForVelocityTemplates : function(format, location,
                        embeddedTemplateContent, templatePath, generateFileOutputName,
                        convertToPdf,defaultInAp)
               {
                  return createRouteForVelocityTemplates(format, location,
                           embeddedTemplateContent, templatePath, generateFileOutputName,
                           convertToPdf,defaultInAp);
               },
               createRouteForXDocReportTemplates : function(format, location,
                        embeddedTemplateContent, templatePath, generateFileOutputName,
                        convertToPdf)
               {
                  return createRouteForXDocReportTemplates(format, location,
                           embeddedTemplateContent, templatePath, generateFileOutputName,
                           convertToPdf);
               },
               findAccessPoint : function(accessPoints, accessPointId)
               {
                  return findAccessPoint(accessPoints, accessPointId);
               },
               filterAccessPoint : function(accessPoints, accessPointId)
               {
                  return filterAccessPoint(accessPoints, accessPointId);
               }
            };

            /**
             * Returns a String representation of a Camel RouteDefinition that will be use
             * to handle Templates processing at runtime.
             *
             * @param format:
             *           txt, html,xml, docx
             * @param location:
             *           embedded, classpath,repository, data
             * @param embeddedTemplateContent:content
             *           of the template when location is set to embedded
             * @param templatePath:
             *           the path of the template when location is reposity or classpath;
             *           for repository if the location is relative templates will be
             *           appended
             * @param generateFileOutputName:
             *           the name of the generated file
             * @param convertToPdf:
             *           is true the output will be converted to pdf file.
             * @returns
             */
            function createTemplatingHandlerRouteDefinition(format, location,
                     embeddedTemplateContent, templatePath, generateFileOutputName,
                     convertToPdf,defaultInAp)
            {
               if (format != "docx")
               {
                  return createRouteForVelocityTemplates(format, location,
                           embeddedTemplateContent, templatePath, generateFileOutputName,
                           convertToPdf,defaultInAp);
               }
               else
               {
                  return createRouteForXDocReportTemplates(format, location,
                           embeddedTemplateContent, templatePath, generateFileOutputName,
                           convertToPdf);
               }
            }

            /**
             * Returns a String representation of a Camel RouteDefinition that will be use
             * to handle Velocity Templates.
             *
             * @param format:
             *           txt, html,xml, docx
             * @param location:
             *           embedded, classpath,repository, data
             * @param embeddedTemplateContent:content
             *           of the template when location is set to embedded
             * @param templatePath:
             *           the path of the template when location is reposity or classpath;
             *           for repository if the location is relative templates will be
             *           appended
             * @param generateFileOutputName:
             *           the name of the generated file
             * @param convertToPdf:
             *           is true the output will be converted to pdf file.
             * @returns The route definition String
             */
            function createRouteForVelocityTemplates(format, location,
                     embeddedTemplateContent, templatePath, generateFileOutputName,
                     convertToPdf,defaultInAp)
            {
               // create RouteDefinition that handle velocity templates
               var routeDefinition = "";
               routeDefinition += "<process ref=\"customVelocityContextAppender\"/>\n";
               if (location == "embedded")
               {
                  routeDefinition += "<setHeader headerName=\"CamelTemplatingTemplateContent\">\n";
                  routeDefinition += "   <constant>\n";
                  routeDefinition += "<![CDATA[";
                  routeDefinition += embeddedTemplateContent;
                  routeDefinition += "]]>\n";
                  routeDefinition += "   </constant>\n";
                  routeDefinition += "</setHeader>\n";
               }
               else if (location == "classpath")
               {
                  //Handle Classpath
               }
               else if (location == "data")
               {
                  if( defaultInAp!=null && defaultInAp.dataType=="primitive"){
                     routeDefinition += "<setHeader headerName=\"CamelTemplatingTemplateContent\">\n";
                     routeDefinition += "   <simple>$simple{header.defaultInputAp}</simple>\n";
                     routeDefinition += "</setHeader>\n";
                  }else{
                     routeDefinition += "<to uri=\"bean:documentHandler?method=retrieveContent\"/>";
                     routeDefinition += "<setHeader headerName=\"CamelTemplatingTemplateContent\">\n";
                     routeDefinition += "   <simple>$simple{header.ippDmsDocumentContent}</simple>\n";
                     routeDefinition += "</setHeader>\n";
                  }
               }
               var uri = "templating:" + location + "?format=" + format;
               if (templatePath != null && templatePath != "")
                  uri += "&amp;template=" + templatePath
               if (generateFileOutputName != null && generateFileOutputName != "")
                  uri += "&amp;outputName=" + generateFileOutputName;
               if (convertToPdf)
               {
                  uri += "&amp;convertToPdf=" + convertToPdf;
               }
               routeDefinition += "<to uri=\"" + uri + "\" />\n";
               return routeDefinition;
            }
            /**
             * Returns a String representation of a Camel RouteDefinition that will be use
             * to handle XDocReport Templates.
             *
             * @param format:
             *           txt, html,xml, docx
             * @param location:
             *           embedded, classpath,repository, data
             * @param embeddedTemplateContent:content
             *           of the template when location is set to embedded
             * @param templatePath:
             *           the path of the template when location is reposity or classpath;
             *           for repository if the location is relative templates will be
             *           appended
             * @param generateFileOutputName:
             *           the name of the generated file
             * @param convertToPdf:
             *           is true the output will be converted to pdf file.
             * @returns The route definition String
             */
            function createRouteForXDocReportTemplates(format, location,
                     embeddedTemplateContent, templatePath, generateFileOutputName,
                     convertToPdf)
            {
               var routeDefinition = "";
               routeDefinition += "<process ref=\"customVelocityContextAppender\"/>\n";
               if (location == "data")
               {
                  routeDefinition += "<to uri=\"bean:documentHandler?method=retrieveContent\"/>\n";
                  routeDefinition += "<setHeader headerName=\"CamelTemplatingTemplateContent\">\n";
                  routeDefinition += "   <simple>$simple{header.ippDmsDocumentContent}</simple>\n";
                  routeDefinition += "</setHeader>\n";
               }
               var uri = "templating:" + location + "?";
               uri += "format=" + format;
               if (templatePath != null && templatePath != "")
                  uri += "&amp;template=" + templatePath;
               if (generateFileOutputName != null && generateFileOutputName != "")
                  uri += "&amp;outputName=" + generateFileOutputName;
               if (convertToPdf)
               {
                  uri += "&amp;convertToPdf=" + convertToPdf;
               }
               routeDefinition += "<to uri=\"" + uri + "\" />";
               routeDefinition += "<setHeader headerName=\"ippDmsDocumentName\">\n";
               routeDefinition += "   <simple>$simple{header.CamelTemplatingOutputName}</simple>\n";
               routeDefinition += "</setHeader>\n";
               routeDefinition += "<to uri=\"bean:documentHandler?method=toDocument\"/>";
               routeDefinition += "<setHeader headerName=\"defaultOutputAp\">\n";
               routeDefinition += "<simple>$simple{body}</simple>\n";
               routeDefinition += "</setHeader>\n";
               return routeDefinition;
            }

            function findAccessPoint(
                     accessPoints, accessPointId)
            {
               var accessPopint = null;
               for (var n = 0; n < accessPoints.length; n++)
               {
                  var ap = accessPoints[n];
                  if (ap.id == accessPointId)
                  {
                     accessPopint = ap;
                     break;
                  }
               }
               return accessPopint;
            }
            /**
             * exclude accessPointId from the accessPoints List
             */
            function filterAccessPoint(
                     accessPoints, accessPointId)
            {
               var filteredAccessPoints = [];
               for (var n = 0; n < accessPoints.length; n++)
               {
                  var ap = accessPoints[n];
                  if (ap.id != accessPointId)
                  {
                     filteredAccessPoints.push(ap);
                  }
               }
               return filteredAccessPoints;
            }
         });
