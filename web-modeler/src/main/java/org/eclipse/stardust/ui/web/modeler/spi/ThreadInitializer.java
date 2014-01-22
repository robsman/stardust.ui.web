package org.eclipse.stardust.ui.web.modeler.spi;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;

/**
 * Initialize the current thread for request processing, like setting up thread locals.
 * <p>
 * Please note: use of this SPI should only be the last resort and most probably indicates
 * an API design smell.
 *
 * @author Robert.Sauer
 */
@SPI(status=Status.Internal, useRestriction=UseRestriction.Internal)
public interface ThreadInitializer
{
   void initialize();
}
