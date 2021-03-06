package org.eclipse.stardust.ui.web.rest.documentation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DTODescription {
   String request() default "";
   String response() default "";
}
