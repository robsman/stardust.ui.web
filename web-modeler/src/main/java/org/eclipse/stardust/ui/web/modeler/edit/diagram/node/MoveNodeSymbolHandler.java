package org.eclipse.stardust.ui.web.modeler.edit.diagram.node;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.HEIGHT_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.WIDTH_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.X_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.Y_PROPERTY;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.carnot.INodeSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ISwimlaneSymbol;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;

@CommandHandler
public class MoveNodeSymbolHandler
{
   @OnCommand(commandId = "nodeSymbol.move")
   public void handleMoveNode(INodeSymbol nodeSymbol, JsonObject request)
   {
      int laneOffsetX = 0;
      int laneOffsetY = 0;
      ISwimlaneSymbol container = (nodeSymbol.eContainer() instanceof ISwimlaneSymbol)
            ? (ISwimlaneSymbol) nodeSymbol.eContainer()
            : null;
      while (null != container)
      {
         laneOffsetX += container.getXPos();
         laneOffsetY += container.getYPos();

         // recurse
         container = (container.eContainer() instanceof ISwimlaneSymbol)
               ? (ISwimlaneSymbol) container.eContainer()
               : null;
      }

      nodeSymbol.setXPos(extractInt(request, X_PROPERTY) - laneOffsetX);
      nodeSymbol.setYPos(extractInt(request, Y_PROPERTY) - laneOffsetY);

      if (request.has(WIDTH_PROPERTY))
      {
         nodeSymbol.setWidth(extractInt(request, WIDTH_PROPERTY));
      }
      if (request.has(HEIGHT_PROPERTY))
      {
         nodeSymbol.setHeight(extractInt(request, HEIGHT_PROPERTY));
      }
   }
}
