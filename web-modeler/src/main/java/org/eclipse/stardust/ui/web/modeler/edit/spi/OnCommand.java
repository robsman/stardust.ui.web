package org.eclipse.stardust.ui.web.modeler.edit.spi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.google.gson.JsonObject;

/**
 * Declares a command handler method. Handler methods must accept three parameters
 * <ol>
 * <li> the model
 * <li> the context element
 * <li> the {@link JsonObject} representation of command arguments
 * </ol>
 * <p>
 * Example:
 * <pre>
 * {@code
 * &#64;CommandHandler
 * public class MoveNodeSymbolHandler
 * {
 *    &#64;OnCommand(commandId = "nodeSymbol.move")
 *    public void handleMoveNode(ModelType model, INodeSymbol nodeSymbol, JsonObject request)
 *    {
 *       ...
 *    }
 * }
 * </pre>
 *
 * @author robert.sauer
 * @see @CommandHandler
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OnCommand
{
   String commandId();
}
