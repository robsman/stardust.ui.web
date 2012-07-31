package org.eclipse.stardust.ui.web.modeler.edit.spi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Retention(RetentionPolicy.RUNTIME)
@Component
@Scope("singleton")
public @interface CommandHandler
{
}
