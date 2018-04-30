package ir.eitaa.tgnet;

import java.util.ArrayList;

public class RPCRequest
{
  boolean cancelled;
  public boolean completed = false;
  public RequestDelegate completionBlock;
  public boolean confirmed;
  public int connectionToken = 0;
  public int connectionType;
  public int failedByFloodWait = 0;
  public int flags;
  public boolean initRequest = false;
  public int lastResendTime = 0;
  public long messageId;
  public int minStartTime;
  public QuickAckDelegate quickAckBlock;
  public TLObject rawRequest;
  public ArrayList<Long> respondsToMessageIds = new ArrayList();
  public int retryCount = 0;
  public TLObject rpcRequest;
  public int runningDatacenterId;
  public boolean salt = false;
  public int serializedLength;
  public int serverFailureCount;
  public int startTime;
  long token;
  public boolean wait = false;
  
  public void addRespondMessageId(long paramLong)
  {
    this.respondsToMessageIds.add(Long.valueOf(paramLong));
  }
  
  boolean respondsToMessageId(long paramLong)
  {
    return (this.messageId == paramLong) || (this.respondsToMessageIds.contains(Long.valueOf(paramLong)));
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/tgnet/RPCRequest.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */