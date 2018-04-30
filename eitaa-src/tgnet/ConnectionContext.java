package ir.eitaa.tgnet;

import ir.eitaa.messenger.Utilities;
import java.security.SecureRandom;
import java.util.ArrayList;

public class ConnectionContext
{
  public static final boolean isDebugSession = false;
  private ArrayList<Long> messagesIdsForConfirmation = new ArrayList();
  private int nextSeqNo = 0;
  private ArrayList<Long> processedMessageIds = new ArrayList();
  private ArrayList<Long> processedSessionChanges = new ArrayList();
  private long sessionId;
  
  public ConnectionContext()
  {
    genereateNewSessionId();
  }
  
  private void genereateNewSessionId()
  {
    this.sessionId = Utilities.random.nextLong();
  }
  
  public void addMessageToConfirm(long paramLong)
  {
    if (this.messagesIdsForConfirmation.contains(Long.valueOf(paramLong))) {
      return;
    }
    this.messagesIdsForConfirmation.add(Long.valueOf(paramLong));
  }
  
  public void addProcessedMessageId(long paramLong)
  {
    if (this.processedMessageIds.size() > 1224)
    {
      int i = 0;
      while (i < Math.min(this.processedMessageIds.size(), 225))
      {
        this.processedMessageIds.remove(0);
        i += 1;
      }
    }
    this.processedMessageIds.add(Long.valueOf(paramLong));
  }
  
  public void addProcessedSession(long paramLong)
  {
    this.processedSessionChanges.add(Long.valueOf(paramLong));
  }
  
  public NetworkMessage generateConfirmationRequest()
  {
    NetworkMessage localNetworkMessage = null;
    if (!this.messagesIdsForConfirmation.isEmpty())
    {
      TLRPC.TL_msgs_ack localTL_msgs_ack = new TLRPC.TL_msgs_ack();
      localTL_msgs_ack.msg_ids = new ArrayList();
      localTL_msgs_ack.msg_ids.addAll(this.messagesIdsForConfirmation);
      NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(true);
      localTL_msgs_ack.serializeToStream(localNativeByteBuffer);
      localNetworkMessage = new NetworkMessage();
      localNetworkMessage.protoMessage = new TLRPC.TL_protoMessage();
      localNetworkMessage.protoMessage.msg_id = ConnectionsManager.getInstance().generateMessageId();
      localNetworkMessage.protoMessage.seqno = generateMessageSeqNo(false);
      localNetworkMessage.protoMessage.bytes = localNativeByteBuffer.length();
      localNetworkMessage.protoMessage.body = localTL_msgs_ack;
      this.messagesIdsForConfirmation.clear();
    }
    return localNetworkMessage;
  }
  
  public int generateMessageSeqNo(boolean paramBoolean)
  {
    int j = this.nextSeqNo;
    if (paramBoolean) {
      this.nextSeqNo += 1;
    }
    if (paramBoolean) {}
    for (int i = 1;; i = 0) {
      return i + j * 2;
    }
  }
  
  public long getSissionId()
  {
    return this.sessionId;
  }
  
  public boolean hasMessagesToConfirm()
  {
    return !this.messagesIdsForConfirmation.isEmpty();
  }
  
  boolean isMessageIdProcessed(long paramLong)
  {
    return this.processedMessageIds.contains(Long.valueOf(paramLong));
  }
  
  public boolean isSessionProcessed(long paramLong)
  {
    return this.processedSessionChanges.contains(Long.valueOf(paramLong));
  }
  
  public void recreateSession()
  {
    this.processedMessageIds.clear();
    this.messagesIdsForConfirmation.clear();
    this.processedSessionChanges.clear();
    this.nextSeqNo = 0;
    genereateNewSessionId();
  }
  
  public void setSessionId(long paramLong)
  {
    this.sessionId = paramLong;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/tgnet/ConnectionContext.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */