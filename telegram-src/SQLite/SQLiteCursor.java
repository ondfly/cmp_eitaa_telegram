package org.telegram.SQLite;

import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.tgnet.NativeByteBuffer;

public class SQLiteCursor
{
  public static final int FIELD_TYPE_BYTEARRAY = 4;
  public static final int FIELD_TYPE_FLOAT = 2;
  public static final int FIELD_TYPE_INT = 1;
  public static final int FIELD_TYPE_NULL = 5;
  public static final int FIELD_TYPE_STRING = 3;
  private boolean inRow = false;
  private SQLitePreparedStatement preparedStatement;
  
  public SQLiteCursor(SQLitePreparedStatement paramSQLitePreparedStatement)
  {
    this.preparedStatement = paramSQLitePreparedStatement;
  }
  
  public byte[] byteArrayValue(int paramInt)
    throws SQLiteException
  {
    checkRow();
    return columnByteArrayValue(this.preparedStatement.getStatementHandle(), paramInt);
  }
  
  public NativeByteBuffer byteBufferValue(int paramInt)
    throws SQLiteException
  {
    checkRow();
    long l = columnByteBufferValue(this.preparedStatement.getStatementHandle(), paramInt);
    if (l != 0L) {
      return NativeByteBuffer.wrap(l);
    }
    return null;
  }
  
  void checkRow()
    throws SQLiteException
  {
    if (!this.inRow) {
      throw new SQLiteException("You must call next before");
    }
  }
  
  native byte[] columnByteArrayValue(long paramLong, int paramInt);
  
  native long columnByteBufferValue(long paramLong, int paramInt);
  
  native double columnDoubleValue(long paramLong, int paramInt);
  
  native int columnIntValue(long paramLong, int paramInt);
  
  native int columnIsNull(long paramLong, int paramInt);
  
  native long columnLongValue(long paramLong, int paramInt);
  
  native String columnStringValue(long paramLong, int paramInt);
  
  native int columnType(long paramLong, int paramInt);
  
  public void dispose()
  {
    this.preparedStatement.dispose();
  }
  
  public double doubleValue(int paramInt)
    throws SQLiteException
  {
    checkRow();
    return columnDoubleValue(this.preparedStatement.getStatementHandle(), paramInt);
  }
  
  public long getStatementHandle()
  {
    return this.preparedStatement.getStatementHandle();
  }
  
  public int getTypeOf(int paramInt)
    throws SQLiteException
  {
    checkRow();
    return columnType(this.preparedStatement.getStatementHandle(), paramInt);
  }
  
  public int intValue(int paramInt)
    throws SQLiteException
  {
    checkRow();
    return columnIntValue(this.preparedStatement.getStatementHandle(), paramInt);
  }
  
  public boolean isNull(int paramInt)
    throws SQLiteException
  {
    checkRow();
    return columnIsNull(this.preparedStatement.getStatementHandle(), paramInt) == 1;
  }
  
  public long longValue(int paramInt)
    throws SQLiteException
  {
    checkRow();
    return columnLongValue(this.preparedStatement.getStatementHandle(), paramInt);
  }
  
  public boolean next()
    throws SQLiteException
  {
    int i = this.preparedStatement.step(this.preparedStatement.getStatementHandle());
    int j = i;
    if (i == -1)
    {
      j = 6;
      for (;;)
      {
        int m = j - 1;
        int k = i;
        if (j != 0) {}
        try
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("sqlite busy, waiting...");
          }
          Thread.sleep(500L);
          j = this.preparedStatement.step();
          i = j;
          if (i == 0)
          {
            k = i;
            j = k;
            if (k != -1) {
              break;
            }
            throw new SQLiteException("sqlite busy");
          }
          j = m;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
          j = m;
        }
      }
    }
    if (j == 0) {}
    for (boolean bool = true;; bool = false)
    {
      this.inRow = bool;
      return this.inRow;
    }
  }
  
  public String stringValue(int paramInt)
    throws SQLiteException
  {
    checkRow();
    return columnStringValue(this.preparedStatement.getStatementHandle(), paramInt);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/SQLite/SQLiteCursor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */