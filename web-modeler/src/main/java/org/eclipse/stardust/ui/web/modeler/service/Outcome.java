package org.eclipse.stardust.ui.web.modeler.service;

import javax.ws.rs.core.Response;

import org.eclipse.emf.ecore.EObject;

public class Outcome
{
   enum Type
   {
      SeeThis,
      SeeOther,
      Legacy,
      Error,
   }

   public static Outcome seeThis()
   {
      return new Outcome(Type.SeeThis, null, null);
   }

   public static Outcome seeOther(EObject changeRoot)
   {
      return new Outcome(Type.SeeOther, changeRoot, null);
   }

   public static Outcome legacy(String response)
   {
      return new Outcome(Type.Legacy, null, response);
   }

   public static Outcome error(Response.Status status, String message)
   {
      return new Outcome(Type.Error, null, message);
   }

   private final Type type;

   public final EObject changeRoot;

   public final String response;

   private Outcome(Type type, EObject changeRoot, String response)
   {
      this.type = type;
      this.changeRoot = changeRoot;
      this.response = response;
   }

   public boolean isSeeThis()
   {
      return Type.SeeThis == type;
   }

   public boolean isSeeOther()
   {
      return Type.SeeOther == type;
   }

   public boolean isLegacy()
   {
      return Type.Legacy == type;
   }

   public boolean isError()
   {
      return Type.Error == type;
   }
}
