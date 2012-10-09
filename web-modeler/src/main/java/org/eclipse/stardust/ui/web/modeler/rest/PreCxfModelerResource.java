package org.eclipse.stardust.ui.web.modeler.rest;

import javax.ws.rs.Path;

/**
 * Backwards compatibility with pre-CXF deployments. Adds the /bpm-modeler path prefix.
 *
 * @author Robert.Sauer
 *
 */
@Deprecated
@Path("/bpm-modeler/modeler/{randomPostFix}")
public class PreCxfModelerResource extends org.eclipse.stardust.ui.web.modeler.service.rest.ModelerResource
{
}
