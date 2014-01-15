package org.eclipse.stardust.ui.web.modeler.spi;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Scope;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;

/**
 * Indicates a component/service to be scoped within a modeling session.
 * <p>
 * This is mostly a convenience and equivalent to the use of {@code @Scope("modelingSession")}
 *
 * @author Robert.Sauer
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(TYPE)
@Scope(ModelingSessionScoped.MODELING_SESSION)
@SPI(status = Status.Beta, useRestriction = UseRestriction.Internal)
public @interface ModelingSessionScoped
{
   public static final String MODELING_SESSION = "modelingSession";
}
