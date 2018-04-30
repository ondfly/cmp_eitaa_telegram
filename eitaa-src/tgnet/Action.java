package ir.eitaa.tgnet;

import java.util.HashMap;

public class Action
{
  public ActionDelegate delegate;
  
  public void cancel() {}
  
  public void execute(HashMap paramHashMap) {}
  
  public static abstract interface ActionDelegate
  {
    public abstract void ActionDidFailExecution(Action paramAction);
    
    public abstract void ActionDidFinishExecution(Action paramAction, HashMap<String, Object> paramHashMap);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/tgnet/Action.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */