/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.misc;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.eclipse.stardust.ui.web.rest.misc.EndPointDTO.ParameterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
@Component
@Path("/portal-rest")
public class RestResource
{
   private static final String REST_COMMON_PACKAGE = "org.eclipse.stardust.ui.web.rest";

   @Context
   private HttpServletRequest httpRequest;

   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("")
   public Response getAllRestEndpoints()
   {
      Map<String, Map<String, ResourceDTO>> endpointsContainerDTOs = new TreeMap<String, Map<String, ResourceDTO>>();

      try
      {
         endpointsContainerDTOs.put("rest-common", this.searchAllEndPoints(REST_COMMON_PACKAGE));
         endpointsContainerDTOs.put("simple-modeler",
               this.searchAllEndPoints("com.infinity.bpm.ui.web.simple_modeler.service.rest"));
         endpointsContainerDTOs.put("benchmark", this.searchAllEndPoints("org.eclipse.stardust.ui.web.benchmark.rest"));
         endpointsContainerDTOs.put("process-portal",
               this.searchAllEndPoints("org.eclipse.stardust.ui.web.processportal.service.rest"));
         endpointsContainerDTOs.put("common",
               this.searchAllEndPoints("org.eclipse.stardust.ui.web.common.services.rest"));
         endpointsContainerDTOs.get("common").putAll(this.searchAllEndPoints("org.eclipse.stardust.ui.web.html5.rest"));
         endpointsContainerDTOs.put("document-triage",
               this.searchAllEndPoints("org.eclipse.stardust.ui.web.documenttriage.rest"));

         endpointsContainerDTOs.put("rules-manager",
               this.searchAllEndPoints("org.eclipse.stardust.ui.web.rules_manager.service.rest"));

         endpointsContainerDTOs.put("mobile-workflow", this.searchAllEndPoints("org.eclipse.stardust.ui.mobile.rest"));

         endpointsContainerDTOs.put("bpm-modeler",
               this.searchAllEndPoints("org.eclipse.stardust.ui.web.modeler.service.rest"));

         endpointsContainerDTOs.put("graphics-common",
               this.searchAllEndPoints("org.eclipse.stardust.ui.web.graphics.service.rest"));

         endpointsContainerDTOs.put("business-object-management",
               this.searchAllEndPoints("org.eclipse.stardust.ui.web.business_object_management.rest"));

         endpointsContainerDTOs.put("bpm-reporting",
               this.searchAllEndPoints("org.eclipse.stardust.ui.web.reporting.beans.rest"));

         endpointsContainerDTOs.put("views-common",
               this.searchAllEndPoints("org.eclipse.stardust.ui.web.viewscommon.views.document"));
         endpointsContainerDTOs.get("views-common").putAll(
               this.searchAllEndPoints("org.eclipse.stardust.ui.web.viewscommon.common.controller.mashup.service"));
         endpointsContainerDTOs.get("views-common").putAll(
               this.searchAllEndPoints("\"org.eclipse.stardust.ui.web.viewscommon.docmgmt\""));
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      catch (ClassNotFoundException e)
      {
         e.printStackTrace();
      }

      return Response.ok(AbstractDTO.toJson(endpointsContainerDTOs), MediaType.APPLICATION_JSON).build();
   }

   private String getBaseURL()
   {
      String path = httpRequest.getRequestURL().toString();
      int e = path.indexOf("portal-rest");
      return path.substring(0, e);
   }

   private String getBasePath()
   {
      return httpRequest.getRequestURI().substring(httpRequest.getContextPath().length() + 1);
   }

   @SuppressWarnings("rawtypes")
   private Map<String, ResourceDTO> searchAllEndPoints(String basePkg) throws IOException, ClassNotFoundException
   {
      Map<String, ResourceDTO> containerDTOs = new TreeMap<String, ResourceDTO>();

      List<Class> resources = getAllClassesFromBasePackage(basePkg);

      String basePath = getBaseURL();

      for (Class< ? > resource : resources)
      {
         Path pathAnnoation = resource.getAnnotation(Path.class);
         if (pathAnnoation != null) // it is valid resource
         {
            ResourceDTO containerDTO = new ResourceDTO();
            containerDTOs.put(StringUtils.substringAfterLast(resource.getName(), "."), containerDTO);

            containerDTO.qualifiedName = resource.getName();
            if (resource.getAnnotation(Description.class) != null)
            {
               containerDTO.description = resource.getAnnotation(Description.class).value();
            }

            containerDTO.basePath = getBasePath() + pathAnnoation.value();

            String endPointUrl = basePath + pathAnnoation.value();

            Method[] methods = resource.getMethods();
            List<EndPointDTO> endpointDTOs = new ArrayList<EndPointDTO>();
            containerDTO.endpoints = endpointDTOs;

            for (Method method : methods)
            {
               if (method.isAnnotationPresent(GET.class))
               {
                  endpointDTOs.add(createEndpoint(method, HttpMethod.GET, endPointUrl));
               }
               else if (method.isAnnotationPresent(PUT.class))
               {
                  endpointDTOs.add(createEndpoint(method, HttpMethod.PUT, endPointUrl));
               }
               else if (method.isAnnotationPresent(POST.class))
               {
                  endpointDTOs.add(createEndpoint(method, HttpMethod.POST, endPointUrl));
               }
               else if (method.isAnnotationPresent(DELETE.class))
               {
                  endpointDTOs.add(createEndpoint(method, HttpMethod.DELETE, endPointUrl));
               }
            }
         }
      }

      return containerDTOs;
   }

   /**
    * Returns all of the classes in the specified package (including sub-packages).
    */
   @SuppressWarnings("rawtypes")
   private List<Class> getAllClassesFromBasePackage(String basePkg) throws IOException, ClassNotFoundException
   {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      String basePath = basePkg.replace('.', '/');
      Enumeration<URL> resources = classloader.getResources(basePath);
      List<File> dirs = new ArrayList<File>();

      int count = 0;
      while (resources.hasMoreElements())
      {
         URL resource = resources.nextElement();
         dirs.add(new File(resource.getFile()));
      }

      ArrayList<Class> classes = new ArrayList<Class>();
      for (File directory : dirs)
      {
         classes.addAll(getClasses(directory, basePkg));
      }
      return classes;
   }

   /**
    * Returns a list of all the classes from the package in the specified directory. Calls
    * itself recursively until no more directories are found.
    */
   @SuppressWarnings("rawtypes")
   private List<Class> getClasses(File dir, String pkg) throws ClassNotFoundException
   {
      List<Class> classes = new ArrayList<Class>();
      if (!dir.exists())
      {
         return classes;
      }
      File[] files = dir.listFiles();
      for (File file : files)
      {
         if (file.isDirectory())
         {
            classes.addAll(getClasses(file, pkg + "." + file.getName()));
         }
         else if (file.getName().endsWith(".class"))
         {
            classes.add(Class.forName(pkg + '.' + StringUtils.substringBeforeLast(file.getName(), ".")));
         }
      }
      return classes;
   }

   /**
    * Create an endpoint object to represent the REST endpoint defined in the specified
    * Java method.
    */
   private EndPointDTO createEndpoint(Method javaMethod, String httpMethod, String classUri)
   {
      EndPointDTO newEndpoint = new EndPointDTO();
      newEndpoint.httpMethod = httpMethod;
      newEndpoint.method = javaMethod.getName();

      Path path = javaMethod.getAnnotation(Path.class);
      if (path != null)
      {
         newEndpoint.uri = classUri + path.value();
         newEndpoint.path = path.value();
      }
      else
      {
         newEndpoint.uri = classUri;
      }

      newEndpoint.uri = newEndpoint.uri.replace("//", "/");
      newEndpoint.uri = newEndpoint.uri.replace("//", "/");

      Description description = javaMethod.getAnnotation(Description.class);
      if (description != null)
      {
         newEndpoint.description = description.value();
      }

      RequestDescription req = javaMethod.getAnnotation(RequestDescription.class);
      if (req != null)
      {
         newEndpoint.requestDescription = req.value();
      }
      ResponseDescription res = javaMethod.getAnnotation(ResponseDescription.class);
      if (res != null)
      {
         newEndpoint.responseDescription = res.value();
      }

      exploreParameters(javaMethod, newEndpoint);
      return newEndpoint;
   }

   /**
    * Get the parameters for the specified endpoint from the provided java method.
    */
   @SuppressWarnings("rawtypes")
   private void exploreParameters(Method method, EndPointDTO endpointDTO)
   {
      Annotation[][] methodAnnotations = method.getParameterAnnotations();
      Class[] parameterTypes = method.getParameterTypes();

      for (int i = 0; i < parameterTypes.length; i++)
      {

         Class parameter = parameterTypes[i];

         // ignore parameters used to access context
         if ((parameter == Request.class) || (parameter == javax.servlet.http.HttpServletResponse.class)
               || (parameter == javax.servlet.http.HttpServletRequest.class))
         {
            continue;
         }

         ParameterDTO parameterDTO = new ParameterDTO(parameter, methodAnnotations[i]);

         switch (parameterDTO.type)
         {
         case Path:
            endpointDTO.pathParams.put(parameterDTO.name, parameterDTO);
            break;
         case Query:
            endpointDTO.queryParams.put(parameterDTO.name, parameterDTO);
            break;
         case Default:
            endpointDTO.defaultParams.put(parameterDTO.name, parameterDTO);
            break;
         }
      }
   }
}
