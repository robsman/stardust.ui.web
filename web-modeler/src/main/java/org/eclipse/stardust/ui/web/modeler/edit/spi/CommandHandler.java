package org.eclipse.stardust.ui.web.modeler.edit.spi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Declares a command handling Spring bean. Implies "singleton" scope. Actual handler methods must be annotated with {@link OnCommand}.
 * <p>
 * This annotation must be used to enable automatic discovery of command handlers.
 *
 * @author robert.sauer
 * @see OnCommand
 */
@Retention(RetentionPolicy.RUNTIME)
@Component
@Scope("singleton")
public @interface CommandHandler
{
}
