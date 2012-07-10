package org.eclipse.stardust.ui.web.modeler.marshaling;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.ISwimlaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

import com.google.gson.JsonObject;

public class ModelElementMarshaller
{
   public static JsonObject toJson(IModelElement modelElement)
   {
      JsonObject jsResult;
      String objectUri = null;
      // TODO generically dispatch REST generation from IModelElement
      if (modelElement instanceof StartEventSymbol)
      {
         jsResult = toStartEventJson((StartEventSymbol) modelElement);
      }
      else
      {
         jsResult = new JsonObject();
         jsResult.addProperty(ModelService.OID_PROPERTY, modelElement.getElementOid());
         jsResult.addProperty(ModelService.TYPE_PROPERTY, modelElement.getClass().getName());
         jsResult.addProperty("moreContent", "TODO");

         objectUri = "...";
      }

      return jsResult;
   }

   public static JsonObject toStartEventJson(StartEventSymbol startEventSymbol)
   {
      JsonObject eventSymbolJson = new JsonObject();

      int laneOffsetX = 0;
      int laneOffsetY = 0;
      ISwimlaneSymbol container = (startEventSymbol.eContainer() instanceof ISwimlaneSymbol)
            ? (ISwimlaneSymbol) startEventSymbol.eContainer()
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

      eventSymbolJson.addProperty(ModelService.OID_PROPERTY,
            startEventSymbol.getElementOid());

      // TODO check this math
      eventSymbolJson.addProperty(ModelService.X_PROPERTY,
            startEventSymbol.getXPos() + laneOffsetX
                  + ModelService.POOL_LANE_MARGIN
                  + (startEventSymbol.getWidth() / 2)
                  - ModelService.START_END_SYMBOL_LEFT_OFFSET);
      eventSymbolJson.addProperty(ModelService.Y_PROPERTY,
            startEventSymbol.getYPos() + laneOffsetY
                  + ModelService.POOL_LANE_MARGIN
                  + ModelService.POOL_SWIMLANE_TOP_BOX_HEIGHT);

      JsonObject eventJson = new JsonObject();
      eventSymbolJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, eventJson);

      eventJson.addProperty(ModelService.TYPE_PROPERTY, ModelService.EVENT_KEY);
      eventJson.addProperty(ModelService.EVENT_TYPE_PROPERTY, ModelService.START_EVENT);
      // eventJson.put(ID_PROPERTY,
      // String.valueOf(startEventSymbol.getModelElement().getId()));
      // loadDescription(eventJson,
      // startEventSymbol.getModelElement());
      // loadAttributes(startEventSymbol.getModelElement(),
      // eventJson);

      return eventSymbolJson;
   }

}
