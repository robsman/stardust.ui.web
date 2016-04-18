package org.eclipse.stardust.engine.extensions.templating.core;

public interface IRequestHandler
{
   byte[] handleRequest(TemplatingRequest request) throws ServiceException;
}
