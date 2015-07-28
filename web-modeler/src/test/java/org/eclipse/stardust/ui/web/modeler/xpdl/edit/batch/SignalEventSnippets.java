package org.eclipse.stardust.ui.web.modeler.xpdl.edit.batch;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;

public class SignalEventSnippets
{
   public static String createInDataMapping(String signalEventUuid,
         String fromDataFullId, String toMsgProperty)
   {
      return createDataMapping(signalEventUuid, DirectionType.IN_LITERAL, toMsgProperty,
            fromDataFullId, null, null);
   }

   public static String createInDataMapping(String signalEventUuid,
         String fromDataFullId, String fromDataPath, String toMsgProperty)
   {
      return createDataMapping(signalEventUuid, DirectionType.IN_LITERAL, toMsgProperty,
            fromDataFullId, fromDataPath, null);
   }

   public static String createOutDataMapping(String signalEventUuid,
         String fromMsgProperty, String toDataFullId, boolean createVariable)
   {
      String variable = null;
      if(createVariable)
      {
         variable = "      {'name': 'dataFlow.uuid', 'expression': \"added[id='signalProperty3']/uuid\"}";
      }
               
      return createDataMapping(signalEventUuid, DirectionType.OUT_LITERAL, fromMsgProperty,
            toDataFullId, null, variable);
   }

   public static String createOutDataMapping(String signalEventUuid,
         String fromMsgProperty, String toDataFullId, String toDataPath)
   {
      return createDataMapping(signalEventUuid, DirectionType.OUT_LITERAL, fromMsgProperty,
            toDataFullId, toDataPath, null);
   }

   private static String createDataMapping(String signalEventUuid,
         DirectionType direction, String messageProperty, String dataFullId,
         String dataDerefPath, String variable)
   {
      JsonObject cmdJson = null;
      if(!StringUtils.isEmpty(variable))
      {
         cmdJson = new JsonParser().parse("" //
               + "  {" //
               + "    'commandId': 'dataFlow.create'," //
               + "    'uuid': '" + signalEventUuid + "'," //
               + "    'changes': {" //
               + "      'id': '" + messageProperty + "'," //
               + "      'name': '" + messageProperty + "'," //
               + "      'direction': '" + direction.getLiteral() + "'," //
               + "      'dataFullId': '" + dataFullId + "'" //
               + "    }," //
               + "    'variables': [" //
               + variable
               + "    ]" //
               + "  }" //
               + "").getAsJsonObject();
      }
      else
      {
         cmdJson = new JsonParser().parse("" //
               + "  {" //
               + "    'commandId': 'dataFlow.create'," //
               + "    'uuid': '" + signalEventUuid + "'," //
               + "    'changes': {" //
               + "      'id': '" + messageProperty + "'," //
               + "      'name': '" + messageProperty + "'," //
               + "      'direction': '" + direction.getLiteral() + "'," //
               + "      'dataFullId': '" + dataFullId + "'" //
               + "    }" //
               + "  }" //
               + "").getAsJsonObject();         
      }
      
      if ( !isEmpty(dataDerefPath))
      {
         cmdJson.getAsJsonObject("changes").addProperty(ModelerConstants.DATA_PATH_PROPERTY, dataDerefPath);
      }

      return cmdJson.toString();
   }
   
   public static String deleteDataMapping(String dataFlowUuid)
   {
      JsonObject cmdJson = new JsonParser().parse("" //
            + "  {" //
            + "    'commandId': 'dataFlow.delete'," //
            + "    'uuid': '" + dataFlowUuid + "'"//
            + "  }" //
            + "").getAsJsonObject();

      return cmdJson.toString();
   }
}
