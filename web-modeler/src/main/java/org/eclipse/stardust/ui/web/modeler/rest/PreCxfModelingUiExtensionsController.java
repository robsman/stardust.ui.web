package org.eclipse.stardust.ui.web.modeler.rest;

import javax.ws.rs.Path;

/**
 * Backwards compatibility with pre-CXF deployments. Adds the /bpm-modeler path prefix.
 *
 * @author Robert.Sauer
 *
 */
@Deprecated
@Path("/bpm-modeler/config/ui")
public class PreCxfModelingUiExtensionsController extends org.eclipse.stardust.ui.web.modeler.service.rest.ModelingUiExtensionsController
{
}
