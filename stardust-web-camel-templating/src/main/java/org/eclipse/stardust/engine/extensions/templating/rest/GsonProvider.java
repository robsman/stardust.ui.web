package org.eclipse.stardust.engine.extensions.templating.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.eclipse.stardust.engine.extensions.json.GsonHandler;
import org.eclipse.stardust.engine.extensions.templating.core.TemplatingRequest;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class GsonProvider implements MessageBodyReader<TemplatingRequest>
{
   private final GsonHandler gson = new GsonHandler();

   @Override
   public boolean isReadable(Class< ? > type, Type genericType, Annotation[] annotations,
         MediaType mediaType)
   {
      return true;
   }

   @SuppressWarnings("unchecked")
   @Override
   public TemplatingRequest readFrom(Class<TemplatingRequest> type, Type genericType,
         Annotation[] annotations, MediaType mediaType,
         MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
               throws IOException, WebApplicationException
   {
      InputStreamReader streamReader = new InputStreamReader(entityStream, "UTF-8");
      Map<String, Object> requestMap = (Map<String, Object>) gson.fromJson(streamReader,
            Object.class);
      TemplatingRequest request = new TemplatingRequest();
      request.fromMap(requestMap);
      return request;
   }
}
