package org.eclipse.stardust.ui.web.modeler.spi;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Qualifier;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({TYPE, FIELD, PARAMETER})
@SPI(status = Status.Beta, useRestriction = UseRestriction.Internal)
@Qualifier
public @interface ModelFormat
{
   public static final String XPDL = "xpdl";

   public static final String BPMN2 = "bpmn2";

   String value();
}
