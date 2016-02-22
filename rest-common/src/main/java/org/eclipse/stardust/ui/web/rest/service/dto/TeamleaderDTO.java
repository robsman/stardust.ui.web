package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;


/***
 * 
 * @author Johnson.Quadras
 *
 */
public class TeamleaderDTO extends AbstractDTO
{
   public UserItem user;

   public ModelParticipant team;

   public RoleInfo teamleaderRole;

   public String teamName;

   public TeamleaderDTO(UserItem user, RoleInfo teamleaderRole, ModelParticipant team)
   {
      this.user = user;
      this.team = team;
      this.teamName = ModelHelper.getParticipantName(teamleaderRole);
      this.teamleaderRole = teamleaderRole;
   }

   public boolean equals(Object obj)
   {
      if (obj instanceof TeamleaderDTO)
      {
         TeamleaderDTO tl = (TeamleaderDTO) obj;
         return user.getUser().getOID() == tl.user.getUser().getOID()
               && team.getRuntimeElementOID() == tl.team.getRuntimeElementOID();
      }
      return false;
   }

   public int hashCode()
   {
      int hash = 7;
      hash = 31 * hash + (int) user.getUser().getOID();
      hash = 31 * hash + (int) team.getRuntimeElementOID();
      return hash;
   }
}