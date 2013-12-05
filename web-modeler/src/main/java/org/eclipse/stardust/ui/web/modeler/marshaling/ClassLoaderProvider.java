package org.eclipse.stardust.ui.web.modeler.marshaling;

/**
 * Allows to inject class loaders into ModelMarshalling/Unmarshalling.
 *
 * @author Marc.Gille
 *
 */
public interface ClassLoaderProvider
{
   public ClassLoader classLoader();
}
