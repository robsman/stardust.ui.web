package org.eclipse.stardust.engine.extensions.templating.core;

import org.apache.velocity.VelocityContext;

public interface IRequestHandler
{
   byte[] handleRequest(TemplatingRequest request,VelocityContext velocityContext) throws ServiceException;
}
