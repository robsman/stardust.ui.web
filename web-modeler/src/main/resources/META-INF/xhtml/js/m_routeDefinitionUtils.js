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
                        convertToPdf)
               {
                  return createTemplatingHandlerRouteDefinition(format, location,
                           embeddedTemplateContent, templatePath, generateFileOutputName,
                           convertToPdf);
               },
               createRouteForVelocityTemplates : function(format, location,
                        embeddedTemplateContent, templatePath, generateFileOutputName,
                        convertToPdf)
               {
                  return createRouteForVelocityTemplates(format, location,
                           embeddedTemplateContent, templatePath, generateFileOutputName,
                           convertToPdf);
               },
               createRouteForXDocReportTemplates : function(format, location,
                        embeddedTemplateContent, templatePath, generateFileOutputName,
                        convertToPdf)
               {
                  return createRouteForXDocReportTemplates(format, location,
                           embeddedTemplateContent, templatePath, generateFileOutputName,
                           convertToPdf);
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
                     convertToPdf)
            {
               if (format != "docx")
               {
                  return createRouteForVelocityTemplates(format, location,
                           embeddedTemplateContent, templatePath, generateFileOutputName,
                           convertToPdf);
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
                     convertToPdf)
            {
               // create RouteDefinition that handle velocity templates
               var routeDefinition = "";
               routeDefinition += "<process ref=\"customVelocityContextAppender\"/>\n";
               if (location == "embedded")
               {
                  routeDefinition += "<setHeader headerName=\"CamelVelocityTemplate\">\n";
                  routeDefinition += "   <constant>\n";
                  routeDefinition += "<![CDATA[";
                  routeDefinition += "#parse(\"commons.vm\")\n";
                  routeDefinition += "#getInputs()\n";
                  routeDefinition += embeddedTemplateContent;
                  routeDefinition += "\n";
                  routeDefinition += "#setOutputs()\n";
                  routeDefinition += "]]>\n";
                  routeDefinition += "   </constant>\n";
                  routeDefinition += "</setHeader>\n";
               }
               else if (location == "classpath")
               {
                  routeDefinition += "<setHeader headerName=\"CamelVelocityResourceUri\">\n";
                  routeDefinition += "   <constant>" + templatePath + "</constant>\n";
                  routeDefinition += "</setHeader>\n";
               }
               else if (location == "repository" || location == "data")
               {
                  if (location == "repository")
                  {
                     routeDefinition += "<setHeader headerName=\"ippDmsTargetPath\">\n";
                     routeDefinition += "   <constant>templates/" + templatePath;
                     routeDefinition += "</constant>\n";
                     routeDefinition += "</setHeader>\n";
                  }
                  routeDefinition += "<to uri=\"bean:documentHandler?method=retrieveContent\"/>";
                  routeDefinition += "<setHeader headerName=\"CamelVelocityTemplate\">\n";
                  routeDefinition += "   <simple>$simple{header.ippDmsDocumentContent}</simple>\n";
                  routeDefinition += "</setHeader>\n";
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
               if (convertToPdf)
               {
                  routeDefinition += "<to uri=\"bean:pdfConverterProcessor?method=process\"/>\n";
               }
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
               if (location == "repository" || location == "data")
               {
                  routeDefinition += "<setHeader headerName=\"ippDmsTargetPath\">\n";
                  routeDefinition += "   <constant>templates/" + templatePath
                  routeDefinition += "</constant>\n";
                  routeDefinition += "</setHeader>\n";
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
               routeDefinition += "<setHeader headerName=\"output\">\n";
               routeDefinition += "<simple>$simple{body}</simple>\n";
               routeDefinition += "</setHeader>\n";
               return routeDefinition;
            }
         });
