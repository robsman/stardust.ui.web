package org.eclipse.stardust.engine.extensions.templating.imageprovider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import fr.opensagres.xdocreport.core.document.ImageFormat;
import fr.opensagres.xdocreport.document.images.AbstractInputStreamImageProvider;

public class HttpImageProvider extends AbstractInputStreamImageProvider
{

   private HttpClient client = getClient();

   private HttpMethod method;

   private HttpClient getClient()
   {
      HttpClient client = new HttpClient();
      HostConfiguration hostConfiguration = client.getHostConfiguration();
      String host = System.getProperty("http.proxyHost");
      String portNumber = System.getProperty("http.proxyPort");
      Integer port = null;
      if (portNumber != null && !portNumber.isEmpty())
         port = Integer.parseInt(portNumber);
      if ((host != null && !host.isEmpty()) && (port != null && port > 0))
      {
         hostConfiguration.setProxy(host, port);
         client.setHostConfiguration(hostConfiguration);
      }
      return client;
   }

   public HttpImageProvider(String url) throws IOException
   {
      super(true);
      method = new GetMethod(url);
      int statusCode = client.executeMethod(method);
      if (statusCode != HttpStatus.SC_OK)
      {
         throw new IOException(
               url + " is not rachable. Call failed with the following error: "
                     + method.getStatusLine());
      }
   }

   @Override
   public ImageFormat getImageFormat()
   {
      Header[] header = method.getResponseHeaders("Content-Type");
      if (header != null && header.length > 0)
         return getFormatByMediaType(header[0].getValue());
      return getFormatByMediaType("");
   }

   @Override
   protected InputStream getInputStream() throws IOException
   {

      return new ByteArrayInputStream(method.getResponseBody());
   }

   public static ImageFormat getFormatByMediaType(String mediaType)
   {
      if (mediaType.equalsIgnoreCase("image/bmp"))
         return ImageFormat.bmp;
      if (mediaType.equalsIgnoreCase("image/gif"))
         return ImageFormat.gif;
      if (mediaType.equalsIgnoreCase("image/jpeg"))
         return ImageFormat.jpeg;
      if (mediaType.equalsIgnoreCase("image/png"))
         return ImageFormat.png;
      if (mediaType.equalsIgnoreCase("image/tiff"))
         return ImageFormat.tiff;
      return ImageFormat.jpeg;
   }
}
