/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.viewscommon.views.document;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.core.SessionSharedObjectsMap;
import org.eclipse.stardust.ui.web.viewscommon.views.document.tiff.TIFFDocumentHolder;



/**
 * @author Shrikant.Gangal
 * 
 */
@Path("documentRepoService")
public class DocumentRepositoryRestlet
{
   private static final String imgConfigMapKey = "IMAGE_VIEWER_CONFIGURATION";
   
   private static final String USER_INFO_DATE_FORMAT = "MM/dd/yy hh:mm a";
   
   @Context
   private ServletContext servletContext;

   @Context
   private HttpServletRequest httpRequest;

   /**
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/retrieveStamps")
   public Response retrieveStamps()
   {
      Response response = null;
      response = Response.ok(PharmacyCommonServicesUtils.getStampsJSON(httpRequest), APPLICATION_JSON_TYPE).build();
      return response;
   }

   /**
    * @param docId
    * @return
    */
   @GET
   @Produces(MediaType.TEXT_PLAIN)
   @Path("/documentDownloadURL/{documentId}")
   public Response getDocumentDownloadURL(@PathParam("documentId") String docId)
   {
      Response response = null;
      response = Response.ok(PharmacyCommonServicesUtils.retrieveDocumentDownloadURL(httpRequest, docId), MediaType.TEXT_PLAIN).build();
      return response;
   }

   /**
    * @param docId
    * @param pageNo
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/retrievePageDimensions/{documentId}/{pageNo}/{randomPostfix}")
   public Response retrievePageDimensions(@PathParam("documentId") String docId, @PathParam("pageNo") int pageNo)
   {
      SessionSharedObjectsMap objMap = (SessionSharedObjectsMap) httpRequest.getSession().getAttribute(
            "sessionSharedObjectsMap");

      TIFFDocumentHolder tiffDocHolder = (TIFFDocumentHolder) objMap.getObject(docId);

      Response response = null;
      if (null != tiffDocHolder)
      {
         int width = tiffDocHolder.getPageWidth(pageNo);
         int height = tiffDocHolder.getPageHeight(pageNo);
         response = Response.ok("{\"width\" : " + width + ", \"height\" : " + height + "}", APPLICATION_JSON_TYPE)
               .build();
      }
      else
      {
         response = Response.ok("{}", APPLICATION_JSON_TYPE).build();
      }

      return response;
   }

   /**
    * @param docId
    * @param pageNo
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/getUser")
   public Response getUser()
   {
      SessionSharedObjectsMap sessionSharedMap = (SessionSharedObjectsMap) httpRequest.getSession().getAttribute(
            "sessionSharedObjectsMap");

      SessionContext sessionContext = (SessionContext) sessionSharedMap.getObject("SESSION_CONTEXT");
      User user = sessionContext.getUser();
      Response response = null;
      if (null != user)
      {
         response = Response.ok(
               "{\"user\" : \"" + user.getFirstName() + " " + user.getLastName() + "\", \"timeStamp\" : \""
                     + getCurrentDate(getLocale()) + "\"}", APPLICATION_JSON_TYPE).build();
      }
      else
      {
         response = Response.ok("{}", APPLICATION_JSON_TYPE).build();
      }
      return response;
   }
   
   /**
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/imageViewerConfig")
   public Response getImageViewerConfiguration()
   {
      SessionSharedObjectsMap objMap = (SessionSharedObjectsMap) httpRequest.getSession().getAttribute(
      "sessionSharedObjectsMap");

      return Response.ok(getJSONFromMap((Map<String, String>) objMap.getObject(imgConfigMapKey)), APPLICATION_JSON_TYPE).build();
   }
   
   /**
    * @param docId
    * @param pageNo
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/documentPageSequence/{documentId}/{randomPostfix}")
   public Response retrieveDocumentPageSequence(@PathParam("documentId") String docId)
   {
      SessionSharedObjectsMap objMap = (SessionSharedObjectsMap) httpRequest.getSession().getAttribute(
            "sessionSharedObjectsMap");

      TIFFDocumentHolder tiffDocHolder = (TIFFDocumentHolder) objMap.getObject(docId);
      
      List<Integer> pgSeq = tiffDocHolder.getCurrentPageOrder();
      StringBuffer jsonPgSeq = new StringBuffer("[");
      for(int i = 0; i < pgSeq.size(); i ++)
      {
         jsonPgSeq.append("\"");
         jsonPgSeq.append(pgSeq.get(i));
         jsonPgSeq.append("\"");
         if (i < (pgSeq.size() - 1))
         {
            jsonPgSeq.append(", ");
         }
      }
      jsonPgSeq.append("]");
  
      return Response.ok(jsonPgSeq.toString(), APPLICATION_JSON_TYPE).build();
   }   

   /**
    * @return
    */
   private String getCurrentDate(Locale locale)
   {
      SimpleDateFormat format = new SimpleDateFormat(USER_INFO_DATE_FORMAT, locale);
      return format.format(new Date());
   }
   
   private String getJSONFromMap(Map<String, String> map)
   {
      Set<String> keys = map.keySet();
      int count = 0;
      StringBuffer jsonBuff = new StringBuffer("{");
      for (String key : keys)
      {
         count++;
         jsonBuff.append("\"");
         jsonBuff.append(key);
         jsonBuff.append("\"");
         jsonBuff.append(" : ");
         jsonBuff.append("\"");
         jsonBuff.append(map.get(key));
         jsonBuff.append("\"");
         if(count < keys.size())
         {
            jsonBuff.append(",");
         }
      }
      jsonBuff.append("}");
      
      return jsonBuff.toString();
   }
   
   
   /**
    * @return
    */
   public Locale getLocale()
   {
      StringTokenizer tok = new StringTokenizer(httpRequest.getHeader("Accept-language"), ",");
      if (tok.hasMoreTokens())
      {
         return new Locale(tok.nextToken().substring(0, 2));
      }      
      return new Locale("en");
   }
}
