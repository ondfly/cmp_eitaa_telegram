package org.telegram.SQLite;

import java.io.File;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;

public class SQLiteDatabase
{
  private boolean inTransaction = false;
  private boolean isOpen = false;
  private final long sqliteHandle = opendb(paramString, ApplicationLoader.getFilesDirFixed().getPath());
  
  public SQLiteDatabase(String paramString)
    throws SQLiteException
  {}
  
  public void beginTransaction()
    throws SQLiteException
  {
    if (this.inTransaction) {
      throw new SQLiteException("database already in transaction");
    }
    this.inTransaction = true;
    beginTransaction(this.sqliteHandle);
  }
  
  native void beginTransaction(long paramLong);
  
  void checkOpened()
    throws SQLiteException
  {
    if (!this.isOpen) {
      throw new SQLiteException("Database closed");
    }
  }
  
  public void close()
  {
    if (this.isOpen) {}
    try
    {
      commitTransaction();
      closedb(this.sqliteHandle);
      this.isOpen = false;
      return;
    }
    catch (SQLiteException localSQLiteException)
    {
      for (;;)
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.e(localSQLiteException.getMessage(), localSQLiteException);
        }
      }
    }
  }
  
  native void closedb(long paramLong)
    throws SQLiteException;
  
  public void commitTransaction()
  {
    if (!this.inTransaction) {
      return;
    }
    this.inTransaction = false;
    commitTransaction(this.sqliteHandle);
  }
  
  native void commitTransaction(long paramLong);
  
  public SQLitePreparedStatement executeFast(String paramString)
    throws SQLiteException
  {
    return new SQLitePreparedStatement(this, paramString, true);
  }
  
  public Integer executeInt(String paramString, Object... paramVarArgs)
    throws SQLiteException
  {
    checkOpened();
    paramString = queryFinalized(paramString, paramVarArgs);
    try
    {
      boolean bool = paramString.next();
      if (!bool) {
        return null;
      }
      int i = paramString.intValue(0);
      return Integer.valueOf(i);
    }
    finally
    {
      paramString.dispose();
    }
  }
  
  public void finalize()
    throws Throwable
  {
    super.finalize();
    close();
  }
  
  public long getSQLiteHandle()
  {
    return this.sqliteHandle;
  }
  
  native long opendb(String paramString1, String paramString2)
    throws SQLiteException;
  
  public SQLiteCursor queryFinalized(String paramString, Object... paramVarArgs)
    throws SQLiteException
  {
    checkOpened();
    return new SQLitePreparedStatement(this, paramString, true).query(paramVarArgs);
  }
  
  public boolean tableExists(String paramString)
    throws SQLiteException
  {
    checkOpened();
    return executeInt("SELECT rowid FROM sqlite_master WHERE type='table' AND name=?;", new Object[] { paramString }) != null;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/SQLite/SQLiteDatabase.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */