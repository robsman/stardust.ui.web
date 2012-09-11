package org.eclipse.stardust.ui.web.modeler.bpmn2.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.URIHandlerImpl;

public class DirectStreamsURIHandler extends URIHandlerImpl
{
   private final UUID authority = UUID.randomUUID();

   private final Map<String, InputStream> inStreams = new HashMap<String, InputStream>();

   private final Map<String, OutputStream> outStreams = new HashMap<String, OutputStream>();

   public URI registerInputStream(InputStream inStream)
   {
      URI streamUri = createStreamUri("istream", UUID.randomUUID().toString());
      inStreams.put(streamUri.toString(), inStream);
      return streamUri;
   }

   public URI registerOutputStream(OutputStream outStream)
   {
      URI streamUri = createStreamUri("ostream", UUID.randomUUID().toString());
      outStreams.put(streamUri.toString(), outStream);
      return streamUri;
   }

   @Override
   public boolean canHandle(URI uri)
   {
      return ("istream".equals(uri.scheme()) && inStreams.containsKey(uri.toString()))
            || ("ostream".equals(uri.scheme()) && outStreams.containsKey(uri.toString()));
   }

   @Override
   public InputStream createInputStream(URI uri, Map<? , ? > options) throws IOException
   {
      return inStreams.get(uri.toString());
   }

   @Override
   public OutputStream createOutputStream(URI uri, Map<? , ? > options)
         throws IOException
   {
      return outStreams.get(uri.toString());
   }

   private URI createStreamUri(String scheme, String streamId)
   {
      return URI.createHierarchicalURI(scheme, authority.toString(), null,
            new String[] {streamId}, null, null);
   }
}
