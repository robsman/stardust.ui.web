/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.documentation;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
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
import org.eclipse.stardust.ui.web.rest.component.service.UserService;
import org.eclipse.stardust.ui.web.rest.documentation.EndPointDTO.ParameterDTO;
import org.eclipse.stardust.ui.web.rest.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
@Path("/portal-rest")
public class RestResource
{
   private static final String REST_COMMON_PACKAGE = "org.eclipse.stardust.ui.web.rest.resource";

   @Context
   private HttpServletRequest httpRequest;

   @Autowired
   private UserService userService;

   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("")
   public Response getAllRestEndpoints()
   {
      // make sure user is logged in
      userService.getLoggedInUser();

      Map<String, Map<String, ResourceDTO>> endpointsContainerDTOs = new TreeMap<String, Map<String, ResourceDTO>>();

      try
      {

         endpointsContainerDTOs.put("bpm-reporting",
               this.searchAllEndPoints("org.eclipse.stardust.ui.web.reporting.beans.rest"));
         endpointsContainerDTOs.put("simple-modeler",
               this.searchAllEndPoints("com.infinity.bpm.ui.web.simple_modeler.service.rest"));

         endpointsContainerDTOs.put("rest-common", this.searchAllEndPoints(REST_COMMON_PACKAGE));

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

         endpointsContainerDTOs.put("views-common",
               this.searchAllEndPoints("org.eclipse.stardust.ui.web.viewscommon.views.document"));
         endpointsContainerDTOs.get("views-common").putAll(
               this.searchAllEndPoints("org.eclipse.stardust.ui.web.viewscommon.common.controller.mashup.service"));
         endpointsContainerDTOs.get("views-common").putAll(
               this.searchAllEndPoints("org.eclipse.stardust.ui.web.viewscommon.docmgmt"));
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
      /*
       * System.out.println(httpRequest.getContextPath());
       * System.out.println(httpRequest.getPathInfo());
       * System.out.println(httpRequest.getRequestURI());
       * System.out.println(httpRequest.getRequestURL());
       */
      String path = httpRequest.getRequestURL().toString();
      int e = path.indexOf("portal-rest");
      return path.substring(0, e);
   }

   private String getBasePath()
   {
      return httpRequest.getRequestURI().substring(httpRequest.getContextPath().length() + 1);
   }

   /**
    * @param basePkg
    * @return
    * @throws IOException
    * @throws ClassNotFoundException
    */
   private Map<String, ResourceDTO> searchAllEndPoints(String basePkg) throws IOException, ClassNotFoundException
   {
      Map<String, ResourceDTO> containerDTOs = new TreeMap<String, ResourceDTO>();
      String basePath = getBaseURL();

      // Map<String, Class> jrebelResources = getAllClassesFromBasePackageJrebel(basePkg);
      // containerDTOs = searchAllEndPoints(jrebelResources, basePath, containerDTOs);
      containerDTOs.putAll(searchAllEndPoints(getAllClassesFromBasePackage(basePkg), basePath));

      return containerDTOs;
   }

   /**
    * @param resources
    * @param basePath
    * @return
    */
   @SuppressWarnings("rawtypes")
   private Map<String, ResourceDTO> searchAllEndPoints(Map<String, Class> resources, String basePath)
   {
      Map<String, ResourceDTO> containerDTOs = new TreeMap<String, ResourceDTO>();

      for (Class< ? > resource : resources.values())
      {
         Path pathAnnoation = resource.getAnnotation(Path.class);
         if (pathAnnoation != null) // it is valid resource
         {
            ResourceDTO containerDTO = new ResourceDTO();

            String name = StringUtils.substringAfterLast(resource.getName(), ".");
            containerDTOs.put(name, containerDTO);

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
    * This method works only when Jrebel is enabled
    */
   @SuppressWarnings("rawtypes")
   private Map<String, Class> getAllClassesFromBasePackageJrebel(String basePkg) throws IOException,
         ClassNotFoundException
   {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      String basePath = basePkg.replace('.', '/');
      Enumeration<URL> resources = classloader.getResources(basePath);
      List<File> dirs = new ArrayList<File>();

      while (resources.hasMoreElements())
      {
         URL resource = resources.nextElement();
         dirs.add(new File(resource.getFile()));
      }

      Map<String, Class> classes = new HashMap<String, Class>();
      for (File directory : dirs)
      {
         classes.putAll(getClasses(directory, basePkg));
      }
      return classes;
   }

   /**
    * Returns all of the classes in the specified package (including sub-packages).
    * 
    * @param basePackage
    * @return
    * @throws IOException
    * @throws ClassNotFoundException
    */
   private Map<String, Class> getAllClassesFromBasePackage(String basePackage) throws IOException,
         ClassNotFoundException
   {
      ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
      MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

      String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + resolveBasePackage(basePackage)
            + "/" + "**/*.class";
      Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);

      Map<String, Class> classes = new HashMap<String, Class>();
      for (Resource resource : resources)
      {
         if (resource.isReadable())
         {
            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
            if (isResource(metadataReader))
            {
               classes.put(metadataReader.getClassMetadata().getClassName(),
                     Class.forName(metadataReader.getClassMetadata().getClassName()));
            }
         }
      }
      return classes;
   }

   private String resolveBasePackage(String basePackage)
   {
      return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
   }

   private boolean isResource(MetadataReader metadataReader) throws ClassNotFoundException
   {
      try
      {
         Class c = Class.forName(metadataReader.getClassMetadata().getClassName());
         if (c.getAnnotation(Path.class) != null)
         {
            return true;
         }
      }
      catch (Throwable e)
      {
      }
      return false;
   }

   private boolean isResource(Class< ? > resource) throws ClassNotFoundException
   {
      try
      {
         if (resource.getAnnotation(Path.class) != null)
         {
            return true;
         }
      }
      catch (Throwable e)
      {
      }
      return false;
   }

   /**
    * Returns a list of all the classes from the package in the specified directory. Calls
    * itself recursively until no more directories are found.
    */
   @SuppressWarnings("rawtypes")
   private Map<String, Class> getClasses(File dir, String pkg) throws ClassNotFoundException
   {
      Map<String, Class> classes = new HashMap<String, Class>();
      if (!dir.exists())
      {
         return classes;
      }
      File[] files = dir.listFiles();
      for (File file : files)
      {
         if (file.isDirectory())
         {
            classes.putAll(getClasses(file, pkg + "." + file.getName()));
         }
         else if (file.getName().endsWith(".class"))
         {
            String resName = pkg + '.' + StringUtils.substringBeforeLast(file.getName(), ".");
            Class< ? > resource = Class.forName(resName);
            if (isResource(resource))
            {
               classes.put(resName, resource);
            }
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
      String contextPath = httpRequest.getContextPath();
      int i = newEndpoint.uri.indexOf(contextPath);
      newEndpoint.relativePath = newEndpoint.uri.substring(i, newEndpoint.uri.length());

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
      
      DTODescription dtos = javaMethod.getAnnotation(DTODescription.class);
      if (dtos != null)
      {
         newEndpoint.requestDTO = dtos.request();
         newEndpoint.responseDTO = dtos.response();
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
