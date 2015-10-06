package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.Set;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

@DTOClass
public class BenchmarkCategoryDTO extends AbstractDTO implements Comparable<BenchmarkCategoryDTO>
{
   public String color; 
   public int index;
   public String name;
   public Long count;
   public Set<Long> instanceOids;
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((color == null) ? 0 : color.hashCode());
      result = prime * result + index;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
   }
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      BenchmarkCategoryDTO other = (BenchmarkCategoryDTO) obj;
      if (color == null)
      {
         if (other.color != null)
            return false;
      }
      else if (!color.equals(other.color))
         return false;
      if (index != other.index)
         return false;
      if (name == null)
      {
         if (other.name != null)
            return false;
      }
      else if (!name.equals(other.name))
         return false;
      return true;
   }
   
   @Override
   public int compareTo(BenchmarkCategoryDTO o)
   {
      int index = o.index;       
      //ascending order
      return this.index - index;
   }
   
   
}
