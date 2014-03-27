package org.eclipse.stardust.ui.web.reporting.common.validation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(value=RetentionPolicy.RUNTIME)
@Target(value=ElementType.FIELD)
public abstract @interface NotNull {

}