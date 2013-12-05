package org.eclipse.stardust.ui.web.modeler.utils.test;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

public class MockUriInfo implements UriInfo
{
   private String base;
   private String path;

   public void setBase(String base)
   {
      this.base = base;
   }

   @Override
   public String getPath()
   {
      return path;
   }

   public void setPath(String path)
   {
      this.path = path;
   }

   @Override
   public String getPath(boolean decode)
   {
      throw new UnsupportedOperationException("Not (yet) implemented.");
   }

   @Override
   public List<PathSegment> getPathSegments()
   {
      throw new UnsupportedOperationException("Not (yet) implemented.");
   }

   @Override
   public List<PathSegment> getPathSegments(boolean decode)
   {
      throw new UnsupportedOperationException("Not (yet) implemented.");
   }

   @Override
   public URI getRequestUri()
   {
      throw new UnsupportedOperationException("Not (yet) implemented.");
   }

   @Override
   public UriBuilder getRequestUriBuilder()
   {
      throw new UnsupportedOperationException("Not (yet) implemented.");
   }

   @Override
   public URI getAbsolutePath()
   {
      return getBaseUri().resolve(getPath());
   }

   @Override
   public UriBuilder getAbsolutePathBuilder()
   {
      throw new UnsupportedOperationException("Not (yet) implemented.");
   }

   @Override
   public URI getBaseUri()
   {
      return URI.create(base);
   }

   @Override
   public UriBuilder getBaseUriBuilder()
   {
      throw new UnsupportedOperationException("Not (yet) implemented.");
   }

   @Override
   public MultivaluedMap<String, String> getPathParameters()
   {
      throw new UnsupportedOperationException("Not (yet) implemented.");
   }

   @Override
   public MultivaluedMap<String, String> getPathParameters(boolean decode)
   {
      throw new UnsupportedOperationException("Not (yet) implemented.");
   }

   @Override
   public MultivaluedMap<String, String> getQueryParameters()
   {
      throw new UnsupportedOperationException("Not (yet) implemented.");
   }

   @Override
   public MultivaluedMap<String, String> getQueryParameters(boolean decode)
   {
      throw new UnsupportedOperationException("Not (yet) implemented.");
   }

   @Override
   public List<String> getMatchedURIs()
   {
      throw new UnsupportedOperationException("Not (yet) implemented.");
   }

   @Override
   public List<String> getMatchedURIs(boolean decode)
   {
      throw new UnsupportedOperationException("Not (yet) implemented.");
   }

   @Override
   public List<Object> getMatchedResources()
   {
      throw new UnsupportedOperationException("Not (yet) implemented.");
   }

}
