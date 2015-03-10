package org.eclipse.stardust.ui.web.plugin.support.resources.spi;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;

/**
 * Extension point for resolving resource patterns (e.g. "META-INF/webapp/skins/*.css") to
 * actual resources.
 *
 * @see ResourcePatternResolver
 */
@SPI(status = Status.Experimental, useRestriction = UseRestriction.Internal)
public interface PluginResourcesResolver
{
   /**
    * Callback to perform resource resolution.
    *
    * @param globalResolver
    *           The resolver used for global resource discovery (typically the Web
    *           application context).
    * @param locationPattern
    *           The pattern to be resolved (
    *           {@link ResourcePatternResolver#getResources(String)}).
    * @return The list of matching resources. May be {@code null} or empty.
    * @throws IOException
    */
   List<Resource> resolveResources(ResourcePatternResolver globalResolver, String locationPattern) throws IOException;
}
