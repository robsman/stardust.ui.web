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
package org.eclipse.stardust.ui.web.viewscommon.docmgmt;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.MessageFormat;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;

/**
 * @author Yogesh.Manware
 * 
 */
public class FileSystemDocumentServlet extends HttpServlet
{
   public static final String CLIENT_CONTEXT_PARAM = "clientContext";
   private static final long serialVersionUID = 1694187460230339876L;
   private static final int downloadBufferSize = 16 * 1024;
   private String context;

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
    */
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      context = config.getInitParameter(CLIENT_CONTEXT_PARAM);
      context = context != null ? context.toLowerCase() : null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
    * javax.servlet.http.HttpServletResponse)
    */
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      final String fileUri = extractFileUri(req);

      if (!StringUtils.isEmpty(fileUri))
      {
         // make sure any previous content is discarded
         resp.resetBuffer();
         resp.setBufferSize(downloadBufferSize);

         try
         {
            final int status = doDownloadFileContent(fileUri, resp, req.getSession().getId());
            resp.setStatus(status);
            resp.flushBuffer();
         }
         catch (Exception cdfe)
         {
            throw new ServletException(MessageFormat.format("Content download for file ''{0}'' failed", fileUri), cdfe);
         }
      }
      else
      {
         super.doGet(req, resp);
      }
   }

   /**
    * @param resourcePath
    * @param userOid
    * @param mimeType
    * @param sessionID
    * @return
    */
   public static String encodeFSDServletToken(String resourcePath, long userOid, String mimeType, String sessionID)
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append(userOid);
      buffer.append("/").append(sessionID);
      buffer.append("/").append(System.currentTimeMillis());
      buffer.append("/").append(resourcePath.replace('\\', '/'));
      buffer.append("#").append(mimeType);

      return new String(Base64.encode(buffer.toString().getBytes()));
   }

   /**
    * @param fileUri
    * @param resp
    * @return
    * @throws IOException
    */
   private int doDownloadFileContent(final String fileUri, final HttpServletResponse resp, final String sessionId)
         throws IOException
   {
      int result = HttpServletResponse.SC_BAD_REQUEST;

      final DecodedRequest request = decodeRequest(fileUri);

      if (null != request)
      {
         if (!(StringUtils.isNotEmpty(sessionId) && sessionId.equals(request.sessionId)))
         {
            return HttpServletResponse.SC_FORBIDDEN;
         }

         try
         {
            try
            {
               File file = new File(request.resourcePath);
               if (null != file)
               {
                  resp.setContentLength((int) file.length());
                  resp.setContentType(request.mimeType);

                  resp.setHeader("Content-Disposition", "inline; filename=" + file.getName() + ";");

                  byte[] bbuf = new byte[4096];
                  DataInputStream in = new DataInputStream(new FileInputStream(file));
                  int length = 0;
                  ServletOutputStream op = resp.getOutputStream();
                  while ((in != null) && ((length = in.read(bbuf)) != -1))
                  {
                     op.write(bbuf, 0, length);
                  }

                  in.close();
                  op.flush();
                  op.close();

                  result = HttpServletResponse.SC_OK;
               }
            }
            catch (IOException ioe)
            {
               throw new PublicException(MessageFormat.format("Failed retrieving content for file ''{0}''.",
                     new Object[] {request.resourcePath}), ioe);
            }

         }
         finally
         {
         }
      }

      // report outcome
      return result;
   }

   /**
    * @param uri
    * @return
    */
   private static DecodedRequest decodeRequest(String uri)
   {
      DecodedRequest result = new DecodedRequest();

      String decodedToken;
      try
      {
         decodedToken = new String(Base64.decode(uri.getBytes()));
         int splitIdx = decodedToken.indexOf("/");

         splitIdx = decodedToken.indexOf("/");
         if (-1 != splitIdx)
         {
            decodedToken = decodedToken.substring(splitIdx + 1);

            splitIdx = decodedToken.indexOf("/");

            if (-1 != splitIdx)
            {
               result.sessionId = decodedToken.substring(0, splitIdx);
               decodedToken = decodedToken.substring(splitIdx + 1);

               splitIdx = decodedToken.indexOf("/");

               if (-1 != splitIdx)
               {
                  decodedToken = decodedToken.substring(splitIdx + 1);

                  splitIdx = decodedToken.indexOf("#");

                  if (-1 != splitIdx)
                  {
                     result.resourcePath = decodedToken.substring(0, splitIdx);
                     result.mimeType = decodedToken.substring(splitIdx + 1);
                  }
               }
            }
         }
      }
      catch (InternalException ie)
      {
         result = null;
      }

      return result;
   }

   /**
    * @param req
    * @return
    */
   private String extractFileUri(HttpServletRequest req)
   {
      final String servletPath = req.getServletPath();
      final String requestUri = req.getRequestURI();
      final String contextPath = req.getContextPath();

      final StringBuffer prefixBuilder = new StringBuffer();
      prefixBuilder.append(contextPath);

      if (!contextPath.endsWith("/") && !servletPath.startsWith("/"))
      {
         prefixBuilder.append("/");
      }
      prefixBuilder.append(servletPath);
      if (!servletPath.endsWith("/"))
      {
         prefixBuilder.append("/");
      }

      final String prefix = prefixBuilder.toString();

      String fileUri = requestUri.startsWith(prefix) ? requestUri.substring(prefix.length()) : null;

      try
      {
         fileUri = URLDecoder.decode(fileUri, "UTF-8");
      }
      catch (UnsupportedEncodingException uee)
      {
         // ignore
      }

      return fileUri;
   }

   /**
    * @author Yogesh.Manware
    * 
    */
   private static class DecodedRequest
   {
      public String sessionId;
      public String resourcePath;
      public String mimeType;
   }
}
