package org.eclipse.stardust.ui.web.modeler.edit.spi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface OnCommand
{
   String commandId();
}
