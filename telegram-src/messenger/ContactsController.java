package org.telegram.messenger;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Build.VERSION;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.support.SparseLongArray;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.InputUser;
import org.telegram.tgnet.TLRPC.PrivacyRule;
import org.telegram.tgnet.TLRPC.TL_accountDaysTTL;
import org.telegram.tgnet.TLRPC.TL_account_getAccountTTL;
import org.telegram.tgnet.TLRPC.TL_account_getPrivacy;
import org.telegram.tgnet.TLRPC.TL_account_privacyRules;
import org.telegram.tgnet.TLRPC.TL_contact;
import org.telegram.tgnet.TLRPC.TL_contactStatus;
import org.telegram.tgnet.TLRPC.TL_contacts_contactsNotModified;
import org.telegram.tgnet.TLRPC.TL_contacts_deleteContacts;
import org.telegram.tgnet.TLRPC.TL_contacts_getContacts;
import org.telegram.tgnet.TLRPC.TL_contacts_getStatuses;
import org.telegram.tgnet.TLRPC.TL_contacts_importContacts;
import org.telegram.tgnet.TLRPC.TL_contacts_importedContacts;
import org.telegram.tgnet.TLRPC.TL_contacts_resetSaved;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_help_getInviteText;
import org.telegram.tgnet.TLRPC.TL_help_inviteText;
import org.telegram.tgnet.TLRPC.TL_importedContact;
import org.telegram.tgnet.TLRPC.TL_inputPhoneContact;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyKeyChatInvite;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyKeyPhoneCall;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyKeyStatusTimestamp;
import org.telegram.tgnet.TLRPC.TL_popularContact;
import org.telegram.tgnet.TLRPC.TL_user;
import org.telegram.tgnet.TLRPC.TL_userStatusLastMonth;
import org.telegram.tgnet.TLRPC.TL_userStatusLastWeek;
import org.telegram.tgnet.TLRPC.TL_userStatusRecently;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.Vector;
import org.telegram.tgnet.TLRPC.contacts_Contacts;

public class ContactsController
{
  private static volatile ContactsController[] Instance = new ContactsController[3];
  private ArrayList<TLRPC.PrivacyRule> callPrivacyRules;
  private int completedRequestsCount;
  public ArrayList<TLRPC.TL_contact> contacts = new ArrayList();
  public HashMap<String, Contact> contactsBook = new HashMap();
  private boolean contactsBookLoaded;
  public HashMap<String, Contact> contactsBookSPhones = new HashMap();
  public HashMap<String, TLRPC.TL_contact> contactsByPhone = new HashMap();
  public HashMap<String, TLRPC.TL_contact> contactsByShortPhone = new HashMap();
  public ConcurrentHashMap<Integer, TLRPC.TL_contact> contactsDict = new ConcurrentHashMap(20, 1.0F, 2);
  public boolean contactsLoaded;
  private boolean contactsSyncInProgress;
  private int currentAccount;
  private ArrayList<Integer> delayedContactsUpdate = new ArrayList();
  private int deleteAccountTTL;
  private ArrayList<TLRPC.PrivacyRule> groupPrivacyRules;
  private boolean ignoreChanges;
  private String inviteLink;
  private String lastContactsVersions = "";
  private final Object loadContactsSync = new Object();
  private int loadingCallsInfo;
  private boolean loadingContacts;
  private int loadingDeleteInfo;
  private int loadingGroupInfo;
  private int loadingLastSeenInfo;
  private boolean migratingContacts;
  private final Object observerLock = new Object();
  public ArrayList<Contact> phoneBookContacts = new ArrayList();
  private ArrayList<TLRPC.PrivacyRule> privacyRules;
  private String[] projectionNames = { "lookup", "data2", "data3", "data5" };
  private String[] projectionPhones = { "lookup", "data1", "data2", "data3", "display_name", "account_type" };
  private HashMap<String, String> sectionsToReplace = new HashMap();
  public ArrayList<String> sortedUsersMutualSectionsArray = new ArrayList();
  public ArrayList<String> sortedUsersSectionsArray = new ArrayList();
  private Account systemAccount;
  private boolean updatingInviteLink;
  public HashMap<String, ArrayList<TLRPC.TL_contact>> usersMutualSectionsDict = new HashMap();
  public HashMap<String, ArrayList<TLRPC.TL_contact>> usersSectionsDict = new HashMap();
  
  public ContactsController(int paramInt)
  {
    this.currentAccount = paramInt;
    if (MessagesController.getMainSettings(this.currentAccount).getBoolean("needGetStatuses", false)) {
      reloadContactsStatuses();
    }
    this.sectionsToReplace.put("À", "A");
    this.sectionsToReplace.put("Á", "A");
    this.sectionsToReplace.put("Ä", "A");
    this.sectionsToReplace.put("Ù", "U");
    this.sectionsToReplace.put("Ú", "U");
    this.sectionsToReplace.put("Ü", "U");
    this.sectionsToReplace.put("Ì", "I");
    this.sectionsToReplace.put("Í", "I");
    this.sectionsToReplace.put("Ï", "I");
    this.sectionsToReplace.put("È", "E");
    this.sectionsToReplace.put("É", "E");
    this.sectionsToReplace.put("Ê", "E");
    this.sectionsToReplace.put("Ë", "E");
    this.sectionsToReplace.put("Ò", "O");
    this.sectionsToReplace.put("Ó", "O");
    this.sectionsToReplace.put("Ö", "O");
    this.sectionsToReplace.put("Ç", "C");
    this.sectionsToReplace.put("Ñ", "N");
    this.sectionsToReplace.put("Ÿ", "Y");
    this.sectionsToReplace.put("Ý", "Y");
    this.sectionsToReplace.put("Ţ", "Y");
  }
  
  private void applyContactsUpdates(ArrayList<Integer> paramArrayList1, ConcurrentHashMap<Integer, TLRPC.User> paramConcurrentHashMap, final ArrayList<TLRPC.TL_contact> paramArrayList, ArrayList<Integer> paramArrayList2)
  {
    final Object localObject1;
    if (paramArrayList != null)
    {
      localObject1 = paramArrayList2;
      if (paramArrayList2 != null) {}
    }
    else
    {
      paramArrayList2 = new ArrayList();
      localObject2 = new ArrayList();
      i = 0;
      paramArrayList = paramArrayList2;
      localObject1 = localObject2;
      if (i < paramArrayList1.size())
      {
        paramArrayList = (Integer)paramArrayList1.get(i);
        if (paramArrayList.intValue() > 0)
        {
          localObject1 = new TLRPC.TL_contact();
          ((TLRPC.TL_contact)localObject1).user_id = paramArrayList.intValue();
          paramArrayList2.add(localObject1);
        }
        for (;;)
        {
          i += 1;
          break;
          if (paramArrayList.intValue() < 0) {
            ((ArrayList)localObject2).add(Integer.valueOf(-paramArrayList.intValue()));
          }
        }
      }
    }
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("process update - contacts add = " + paramArrayList.size() + " delete = " + ((ArrayList)localObject1).size());
    }
    paramArrayList2 = new StringBuilder();
    Object localObject2 = new StringBuilder();
    int i = 0;
    int j = 0;
    final Object localObject3;
    if (j < paramArrayList.size())
    {
      localObject3 = (TLRPC.TL_contact)paramArrayList.get(j);
      paramArrayList1 = null;
      if (paramConcurrentHashMap != null) {
        paramArrayList1 = (TLRPC.User)paramConcurrentHashMap.get(Integer.valueOf(((TLRPC.TL_contact)localObject3).user_id));
      }
      if (paramArrayList1 == null)
      {
        paramArrayList1 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((TLRPC.TL_contact)localObject3).user_id));
        label261:
        if ((paramArrayList1 != null) && (!TextUtils.isEmpty(paramArrayList1.phone))) {
          break label303;
        }
        i = 1;
      }
      for (;;)
      {
        j += 1;
        break;
        MessagesController.getInstance(this.currentAccount).putUser(paramArrayList1, true);
        break label261;
        label303:
        localObject3 = (Contact)this.contactsBookSPhones.get(paramArrayList1.phone);
        if (localObject3 != null)
        {
          k = ((Contact)localObject3).shortPhones.indexOf(paramArrayList1.phone);
          if (k != -1) {
            ((Contact)localObject3).phoneDeleted.set(k, Integer.valueOf(0));
          }
        }
        if (paramArrayList2.length() != 0) {
          paramArrayList2.append(",");
        }
        paramArrayList2.append(paramArrayList1.phone);
      }
    }
    int k = 0;
    j = i;
    i = k;
    if (i < ((ArrayList)localObject1).size())
    {
      localObject3 = (Integer)((ArrayList)localObject1).get(i);
      Utilities.phoneBookQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          ContactsController.this.deleteContactFromPhoneBook(localObject3.intValue());
        }
      });
      paramArrayList1 = null;
      if (paramConcurrentHashMap != null) {
        paramArrayList1 = (TLRPC.User)paramConcurrentHashMap.get(localObject3);
      }
      if (paramArrayList1 == null)
      {
        paramArrayList1 = MessagesController.getInstance(this.currentAccount).getUser((Integer)localObject3);
        label471:
        if (paramArrayList1 != null) {
          break label507;
        }
        k = 1;
      }
      for (;;)
      {
        i += 1;
        j = k;
        break;
        MessagesController.getInstance(this.currentAccount).putUser(paramArrayList1, true);
        break label471;
        label507:
        k = j;
        if (!TextUtils.isEmpty(paramArrayList1.phone))
        {
          localObject3 = (Contact)this.contactsBookSPhones.get(paramArrayList1.phone);
          if (localObject3 != null)
          {
            k = ((Contact)localObject3).shortPhones.indexOf(paramArrayList1.phone);
            if (k != -1) {
              ((Contact)localObject3).phoneDeleted.set(k, Integer.valueOf(1));
            }
          }
          if (((StringBuilder)localObject2).length() != 0) {
            ((StringBuilder)localObject2).append(",");
          }
          ((StringBuilder)localObject2).append(paramArrayList1.phone);
          k = j;
        }
      }
    }
    if ((paramArrayList2.length() != 0) || (((StringBuilder)localObject2).length() != 0)) {
      MessagesStorage.getInstance(this.currentAccount).applyPhoneBookUpdates(paramArrayList2.toString(), ((StringBuilder)localObject2).toString());
    }
    if (j != 0)
    {
      Utilities.stageQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          ContactsController.this.loadContacts(false, 0);
        }
      });
      return;
    }
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        boolean bool = true;
        int i = 0;
        while (i < paramArrayList.size())
        {
          localObject = (TLRPC.TL_contact)paramArrayList.get(i);
          if (ContactsController.this.contactsDict.get(Integer.valueOf(((TLRPC.TL_contact)localObject).user_id)) == null)
          {
            ContactsController.this.contacts.add(localObject);
            ContactsController.this.contactsDict.put(Integer.valueOf(((TLRPC.TL_contact)localObject).user_id), localObject);
          }
          i += 1;
        }
        i = 0;
        while (i < localObject1.size())
        {
          localObject = (Integer)localObject1.get(i);
          TLRPC.TL_contact localTL_contact = (TLRPC.TL_contact)ContactsController.this.contactsDict.get(localObject);
          if (localTL_contact != null)
          {
            ContactsController.this.contacts.remove(localTL_contact);
            ContactsController.this.contactsDict.remove(localObject);
          }
          i += 1;
        }
        if (!paramArrayList.isEmpty())
        {
          ContactsController.this.updateUnregisteredContacts(ContactsController.this.contacts);
          ContactsController.this.performWriteContactsToPhoneBook();
        }
        ContactsController.this.performSyncPhoneBook(ContactsController.this.getContactsCopy(ContactsController.this.contactsBook), false, false, false, false, true, false);
        Object localObject = ContactsController.this;
        if (!paramArrayList.isEmpty()) {}
        for (;;)
        {
          ((ContactsController)localObject).buildContactsSectionsArrays(bool);
          NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
          return;
          bool = false;
        }
      }
    });
  }
  
  private void buildContactsSectionsArrays(boolean paramBoolean)
  {
    if (paramBoolean) {
      Collections.sort(this.contacts, new Comparator()
      {
        public int compare(TLRPC.TL_contact paramAnonymousTL_contact1, TLRPC.TL_contact paramAnonymousTL_contact2)
        {
          paramAnonymousTL_contact1 = MessagesController.getInstance(ContactsController.this.currentAccount).getUser(Integer.valueOf(paramAnonymousTL_contact1.user_id));
          paramAnonymousTL_contact2 = MessagesController.getInstance(ContactsController.this.currentAccount).getUser(Integer.valueOf(paramAnonymousTL_contact2.user_id));
          return UserObject.getFirstName(paramAnonymousTL_contact1).compareTo(UserObject.getFirstName(paramAnonymousTL_contact2));
        }
      });
    }
    HashMap localHashMap = new HashMap();
    ArrayList localArrayList2 = new ArrayList();
    int i = 0;
    while (i < this.contacts.size())
    {
      TLRPC.TL_contact localTL_contact = (TLRPC.TL_contact)this.contacts.get(i);
      Object localObject1 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(localTL_contact.user_id));
      if (localObject1 == null)
      {
        i += 1;
      }
      else
      {
        Object localObject2 = UserObject.getFirstName((TLRPC.User)localObject1);
        localObject1 = localObject2;
        if (((String)localObject2).length() > 1) {
          localObject1 = ((String)localObject2).substring(0, 1);
        }
        if (((String)localObject1).length() == 0) {}
        for (localObject1 = "#";; localObject1 = ((String)localObject1).toUpperCase())
        {
          localObject2 = (String)this.sectionsToReplace.get(localObject1);
          if (localObject2 != null) {
            localObject1 = localObject2;
          }
          ArrayList localArrayList1 = (ArrayList)localHashMap.get(localObject1);
          localObject2 = localArrayList1;
          if (localArrayList1 == null)
          {
            localObject2 = new ArrayList();
            localHashMap.put(localObject1, localObject2);
            localArrayList2.add(localObject1);
          }
          ((ArrayList)localObject2).add(localTL_contact);
          break;
        }
      }
    }
    Collections.sort(localArrayList2, new Comparator()
    {
      public int compare(String paramAnonymousString1, String paramAnonymousString2)
      {
        int i = paramAnonymousString1.charAt(0);
        int j = paramAnonymousString2.charAt(0);
        if (i == 35) {
          return 1;
        }
        if (j == 35) {
          return -1;
        }
        return paramAnonymousString1.compareTo(paramAnonymousString2);
      }
    });
    this.usersSectionsDict = localHashMap;
    this.sortedUsersSectionsArray = localArrayList2;
  }
  
  /* Error */
  private boolean checkContactsInternal()
  {
    // Byte code:
    //   0: iconst_0
    //   1: istore 5
    //   3: iconst_0
    //   4: istore 7
    //   6: iconst_0
    //   7: istore_1
    //   8: iconst_0
    //   9: istore 6
    //   11: iconst_0
    //   12: istore 4
    //   14: iload_1
    //   15: istore_2
    //   16: aload_0
    //   17: invokespecial 620	org/telegram/messenger/ContactsController:hasContactsPermission	()Z
    //   20: ifne +5 -> 25
    //   23: iconst_0
    //   24: ireturn
    //   25: iload_1
    //   26: istore_2
    //   27: getstatic 626	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   30: invokevirtual 632	android/content/Context:getContentResolver	()Landroid/content/ContentResolver;
    //   33: astore 10
    //   35: aconst_null
    //   36: astore 9
    //   38: aconst_null
    //   39: astore 8
    //   41: iload 4
    //   43: istore_1
    //   44: iload 6
    //   46: istore_3
    //   47: aload 10
    //   49: getstatic 638	android/provider/ContactsContract$RawContacts:CONTENT_URI	Landroid/net/Uri;
    //   52: iconst_1
    //   53: anewarray 206	java/lang/String
    //   56: dup
    //   57: iconst_0
    //   58: ldc_w 640
    //   61: aastore
    //   62: aconst_null
    //   63: aconst_null
    //   64: aconst_null
    //   65: invokevirtual 646	android/content/ContentResolver:query	(Landroid/net/Uri;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;
    //   68: astore 10
    //   70: iload 7
    //   72: istore_1
    //   73: aload 10
    //   75: ifnull +222 -> 297
    //   78: aload 10
    //   80: astore 8
    //   82: iload 4
    //   84: istore_1
    //   85: aload 10
    //   87: astore 9
    //   89: iload 6
    //   91: istore_3
    //   92: new 484	java/lang/StringBuilder
    //   95: dup
    //   96: invokespecial 485	java/lang/StringBuilder:<init>	()V
    //   99: astore 11
    //   101: aload 10
    //   103: astore 8
    //   105: iload 4
    //   107: istore_1
    //   108: aload 10
    //   110: astore 9
    //   112: iload 6
    //   114: istore_3
    //   115: aload 10
    //   117: invokeinterface 651 1 0
    //   122: ifeq +76 -> 198
    //   125: aload 10
    //   127: astore 8
    //   129: iload 4
    //   131: istore_1
    //   132: aload 10
    //   134: astore 9
    //   136: iload 6
    //   138: istore_3
    //   139: aload 11
    //   141: aload 10
    //   143: aload 10
    //   145: ldc_w 640
    //   148: invokeinterface 655 2 0
    //   153: invokeinterface 659 2 0
    //   158: invokevirtual 491	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   161: pop
    //   162: goto -61 -> 101
    //   165: astore 10
    //   167: aload 8
    //   169: astore 9
    //   171: iload_1
    //   172: istore_3
    //   173: aload 10
    //   175: invokestatic 663	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   178: iload_1
    //   179: istore_2
    //   180: aload 8
    //   182: ifnull +14 -> 196
    //   185: iload_1
    //   186: istore_2
    //   187: aload 8
    //   189: invokeinterface 666 1 0
    //   194: iload_1
    //   195: istore_2
    //   196: iload_2
    //   197: ireturn
    //   198: aload 10
    //   200: astore 8
    //   202: iload 4
    //   204: istore_1
    //   205: aload 10
    //   207: astore 9
    //   209: iload 6
    //   211: istore_3
    //   212: aload 11
    //   214: invokevirtual 500	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   217: astore 11
    //   219: aload 10
    //   221: astore 8
    //   223: iload 4
    //   225: istore_1
    //   226: iload 5
    //   228: istore_2
    //   229: aload 10
    //   231: astore 9
    //   233: iload 6
    //   235: istore_3
    //   236: aload_0
    //   237: getfield 194	org/telegram/messenger/ContactsController:lastContactsVersions	Ljava/lang/String;
    //   240: invokevirtual 605	java/lang/String:length	()I
    //   243: ifeq +34 -> 277
    //   246: aload 10
    //   248: astore 8
    //   250: iload 4
    //   252: istore_1
    //   253: iload 5
    //   255: istore_2
    //   256: aload 10
    //   258: astore 9
    //   260: iload 6
    //   262: istore_3
    //   263: aload_0
    //   264: getfield 194	org/telegram/messenger/ContactsController:lastContactsVersions	Ljava/lang/String;
    //   267: aload 11
    //   269: invokevirtual 669	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   272: ifne +5 -> 277
    //   275: iconst_1
    //   276: istore_2
    //   277: aload 10
    //   279: astore 8
    //   281: iload_2
    //   282: istore_1
    //   283: aload 10
    //   285: astore 9
    //   287: iload_2
    //   288: istore_3
    //   289: aload_0
    //   290: aload 11
    //   292: putfield 194	org/telegram/messenger/ContactsController:lastContactsVersions	Ljava/lang/String;
    //   295: iload_2
    //   296: istore_1
    //   297: iload_1
    //   298: istore_2
    //   299: aload 10
    //   301: ifnull -105 -> 196
    //   304: iload_1
    //   305: istore_2
    //   306: aload 10
    //   308: invokeinterface 666 1 0
    //   313: iload_1
    //   314: istore_2
    //   315: goto -119 -> 196
    //   318: astore 8
    //   320: aload 8
    //   322: invokestatic 663	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   325: goto -129 -> 196
    //   328: astore 8
    //   330: aload 9
    //   332: ifnull +12 -> 344
    //   335: iload_3
    //   336: istore_2
    //   337: aload 9
    //   339: invokeinterface 666 1 0
    //   344: iload_3
    //   345: istore_2
    //   346: aload 8
    //   348: athrow
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	349	0	this	ContactsController
    //   7	307	1	bool1	boolean
    //   15	331	2	bool2	boolean
    //   46	299	3	bool3	boolean
    //   12	239	4	bool4	boolean
    //   1	253	5	bool5	boolean
    //   9	252	6	bool6	boolean
    //   4	67	7	bool7	boolean
    //   39	241	8	localObject1	Object
    //   318	3	8	localException1	Exception
    //   328	19	8	localObject2	Object
    //   36	302	9	localObject3	Object
    //   33	111	10	localObject4	Object
    //   165	142	10	localException2	Exception
    //   99	192	11	localObject5	Object
    // Exception table:
    //   from	to	target	type
    //   47	70	165	java/lang/Exception
    //   92	101	165	java/lang/Exception
    //   115	125	165	java/lang/Exception
    //   139	162	165	java/lang/Exception
    //   212	219	165	java/lang/Exception
    //   236	246	165	java/lang/Exception
    //   263	275	165	java/lang/Exception
    //   289	295	165	java/lang/Exception
    //   16	23	318	java/lang/Exception
    //   27	35	318	java/lang/Exception
    //   187	194	318	java/lang/Exception
    //   306	313	318	java/lang/Exception
    //   337	344	318	java/lang/Exception
    //   346	349	318	java/lang/Exception
    //   47	70	328	finally
    //   92	101	328	finally
    //   115	125	328	finally
    //   139	162	328	finally
    //   173	178	328	finally
    //   212	219	328	finally
    //   236	246	328	finally
    //   263	275	328	finally
    //   289	295	328	finally
  }
  
  private void deleteContactFromPhoneBook(int paramInt)
  {
    if (!hasContactsPermission()) {
      return;
    }
    synchronized (this.observerLock)
    {
      this.ignoreChanges = true;
    }
    try
    {
      ApplicationLoader.applicationContext.getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("caller_is_syncadapter", "true").appendQueryParameter("account_name", this.systemAccount.name).appendQueryParameter("account_type", this.systemAccount.type).build(), "sync2 = " + paramInt, null);
      synchronized (this.observerLock)
      {
        this.ignoreChanges = false;
        return;
      }
      localObject3 = finally;
      throw ((Throwable)localObject3);
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
    }
  }
  
  public static String formatName(String paramString1, String paramString2)
  {
    int j = 0;
    String str = paramString1;
    if (paramString1 != null) {
      str = paramString1.trim();
    }
    paramString1 = paramString2;
    if (paramString2 != null) {
      paramString1 = paramString2.trim();
    }
    int i;
    if (str != null)
    {
      i = str.length();
      if (paramString1 != null) {
        j = paramString1.length();
      }
      paramString2 = new StringBuilder(j + i + 1);
      if (LocaleController.nameDisplayOrder != 1) {
        break label141;
      }
      if ((str == null) || (str.length() <= 0)) {
        break label121;
      }
      paramString2.append(str);
      if ((paramString1 != null) && (paramString1.length() > 0))
      {
        paramString2.append(" ");
        paramString2.append(paramString1);
      }
    }
    for (;;)
    {
      return paramString2.toString();
      i = 0;
      break;
      label121:
      if ((paramString1 != null) && (paramString1.length() > 0))
      {
        paramString2.append(paramString1);
        continue;
        label141:
        if ((paramString1 != null) && (paramString1.length() > 0))
        {
          paramString2.append(paramString1);
          if ((str != null) && (str.length() > 0))
          {
            paramString2.append(" ");
            paramString2.append(str);
          }
        }
        else if ((str != null) && (str.length() > 0))
        {
          paramString2.append(str);
        }
      }
    }
  }
  
  private int getContactsHash(ArrayList<TLRPC.TL_contact> paramArrayList)
  {
    long l = 0L;
    paramArrayList = new ArrayList(paramArrayList);
    Collections.sort(paramArrayList, new Comparator()
    {
      public int compare(TLRPC.TL_contact paramAnonymousTL_contact1, TLRPC.TL_contact paramAnonymousTL_contact2)
      {
        if (paramAnonymousTL_contact1.user_id > paramAnonymousTL_contact2.user_id) {
          return 1;
        }
        if (paramAnonymousTL_contact1.user_id < paramAnonymousTL_contact2.user_id) {
          return -1;
        }
        return 0;
      }
    });
    int j = paramArrayList.size();
    int i = -1;
    if (i < j)
    {
      if (i == -1) {}
      for (l = (l * 20261L + 2147483648L + UserConfig.getInstance(this.currentAccount).contactsSavedCount) % 2147483648L;; l = (l * 20261L + 2147483648L + ((TLRPC.TL_contact)paramArrayList.get(i)).user_id) % 2147483648L)
      {
        i += 1;
        break;
      }
    }
    return (int)l;
  }
  
  public static ContactsController getInstance(int paramInt)
  {
    Object localObject1 = Instance[paramInt];
    if (localObject1 == null) {}
    try
    {
      Object localObject3 = Instance[paramInt];
      localObject1 = localObject3;
      if (localObject3 == null)
      {
        localObject3 = Instance;
        localObject1 = new ContactsController(paramInt);
        localObject3[paramInt] = localObject1;
      }
      return (ContactsController)localObject1;
    }
    finally
    {
      for (;;) {}
    }
    throw ((Throwable)localObject1);
    return (ContactsController)localObject1;
  }
  
  private boolean hasContactsPermission()
  {
    if (Build.VERSION.SDK_INT >= 23) {
      return ApplicationLoader.applicationContext.checkSelfPermission("android.permission.READ_CONTACTS") == 0;
    }
    Object localObject3 = null;
    Object localObject1 = null;
    for (;;)
    {
      try
      {
        localCursor = ApplicationLoader.applicationContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, this.projectionPhones, null, null, null);
        if (localCursor != null)
        {
          localObject1 = localCursor;
          localObject3 = localCursor;
          int i = localCursor.getCount();
          if (i != 0) {}
        }
        else
        {
          if (localCursor != null) {}
          try
          {
            localCursor.close();
            return false;
          }
          catch (Exception localException1)
          {
            FileLog.e(localException1);
            continue;
          }
        }
      }
      catch (Throwable localThrowable)
      {
        Cursor localCursor;
        localObject3 = localException2;
        FileLog.e(localThrowable);
        if (localException2 == null) {
          continue;
        }
        try
        {
          localException2.close();
        }
        catch (Exception localException3)
        {
          FileLog.e(localException3);
        }
        continue;
      }
      finally
      {
        if (localObject3 == null) {
          break label157;
        }
      }
      try
      {
        localCursor.close();
        return true;
      }
      catch (Exception localException2)
      {
        FileLog.e(localException2);
      }
    }
    try
    {
      ((Cursor)localObject3).close();
      label157:
      throw ((Throwable)localObject2);
    }
    catch (Exception localException4)
    {
      for (;;)
      {
        FileLog.e(localException4);
      }
    }
  }
  
  private boolean isNotValidNameString(String paramString)
  {
    if (TextUtils.isEmpty(paramString)) {}
    int j;
    do
    {
      return true;
      j = 0;
      int i = 0;
      int m = paramString.length();
      while (i < m)
      {
        int n = paramString.charAt(i);
        int k = j;
        if (n >= 48)
        {
          k = j;
          if (n <= 57) {
            k = j + 1;
          }
        }
        i += 1;
        j = k;
      }
    } while (j > 3);
    return false;
  }
  
  private void performWriteContactsToPhoneBook()
  {
    final ArrayList localArrayList = new ArrayList();
    localArrayList.addAll(this.contacts);
    Utilities.phoneBookQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        ContactsController.this.performWriteContactsToPhoneBookInternal(localArrayList);
      }
    });
  }
  
  private void performWriteContactsToPhoneBookInternal(ArrayList<TLRPC.TL_contact> paramArrayList)
  {
    Object localObject1;
    Object localObject2;
    try
    {
      if (!hasContactsPermission()) {
        return;
      }
      localObject1 = ContactsContract.RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("account_name", this.systemAccount.name).appendQueryParameter("account_type", this.systemAccount.type).build();
      localObject2 = ApplicationLoader.applicationContext.getContentResolver().query((Uri)localObject1, new String[] { "_id", "sync2" }, null, null, null);
      localObject1 = new SparseLongArray();
      if (localObject2 == null) {
        return;
      }
      while (((Cursor)localObject2).moveToNext()) {
        ((SparseLongArray)localObject1).put(((Cursor)localObject2).getInt(1), ((Cursor)localObject2).getLong(0));
      }
      ((Cursor)localObject2).close();
    }
    catch (Exception paramArrayList)
    {
      FileLog.e(paramArrayList);
      return;
    }
    int i = 0;
    while (i < paramArrayList.size())
    {
      localObject2 = (TLRPC.TL_contact)paramArrayList.get(i);
      if (((SparseLongArray)localObject1).indexOfKey(((TLRPC.TL_contact)localObject2).user_id) < 0) {
        addContactToPhoneBook(MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((TLRPC.TL_contact)localObject2).user_id)), false);
      }
      i += 1;
    }
  }
  
  private HashMap<String, Contact> readContactsFromPhoneBook()
  {
    Object localObject1;
    if (!UserConfig.getInstance(this.currentAccount).syncContacts)
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("contacts sync disabled");
      }
      localObject1 = new HashMap();
    }
    for (;;)
    {
      return (HashMap<String, Contact>)localObject1;
      if (!hasContactsPermission())
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("app has no contacts permissions");
        }
        return new HashMap();
      }
      Object localObject9 = null;
      Object localObject8 = null;
      Object localObject11 = null;
      Object localObject12 = null;
      Object localObject6 = null;
      Object localObject10 = null;
      localObject1 = localObject11;
      Object localObject4 = localObject8;
      Object localObject5 = localObject9;
      try
      {
        StringBuilder localStringBuilder = new StringBuilder();
        localObject1 = localObject11;
        localObject4 = localObject8;
        localObject5 = localObject9;
        localContentResolver = ApplicationLoader.applicationContext.getContentResolver();
        localObject1 = localObject11;
        localObject4 = localObject8;
        localObject5 = localObject9;
        localHashMap = new HashMap();
        localObject1 = localObject11;
        localObject4 = localObject8;
        localObject5 = localObject9;
        localArrayList = new ArrayList();
        localObject1 = localObject11;
        localObject4 = localObject8;
        localObject5 = localObject9;
        localObject9 = localContentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, this.projectionPhones, null, null, null);
        localObject8 = localObject9;
        if (localObject9 != null)
        {
          localObject1 = localObject11;
          localObject4 = localObject9;
          localObject5 = localObject9;
          i = ((Cursor)localObject9).getCount();
          localObject6 = localObject12;
          if (i > 0)
          {
            if (0 != 0) {
              break label2673;
            }
            localObject1 = localObject11;
            localObject4 = localObject9;
            localObject5 = localObject9;
            localObject6 = new HashMap(i);
            i = 1;
            for (;;)
            {
              localObject1 = localObject6;
              localObject4 = localObject9;
              localObject5 = localObject9;
              if (!((Cursor)localObject9).moveToNext()) {
                break label1690;
              }
              localObject1 = localObject6;
              localObject4 = localObject9;
              localObject5 = localObject9;
              localObject11 = ((Cursor)localObject9).getString(1);
              localObject1 = localObject6;
              localObject4 = localObject9;
              localObject5 = localObject9;
              localObject8 = ((Cursor)localObject9).getString(5);
              localObject10 = localObject8;
              if (localObject8 == null) {
                localObject10 = "";
              }
              localObject1 = localObject6;
              localObject4 = localObject9;
              localObject5 = localObject9;
              if (((String)localObject10).indexOf(".sim") == 0) {
                break;
              }
              bool = true;
              localObject1 = localObject6;
              localObject4 = localObject9;
              localObject5 = localObject9;
              if (!TextUtils.isEmpty((CharSequence)localObject11))
              {
                localObject1 = localObject6;
                localObject4 = localObject9;
                localObject5 = localObject9;
                localObject12 = PhoneFormat.stripExceptNumbers((String)localObject11, true);
                localObject1 = localObject6;
                localObject4 = localObject9;
                localObject5 = localObject9;
                if (!TextUtils.isEmpty((CharSequence)localObject12))
                {
                  localObject8 = localObject12;
                  localObject1 = localObject6;
                  localObject4 = localObject9;
                  localObject5 = localObject9;
                  if (((String)localObject12).startsWith("+"))
                  {
                    localObject1 = localObject6;
                    localObject4 = localObject9;
                    localObject5 = localObject9;
                    localObject8 = ((String)localObject12).substring(1);
                  }
                  localObject1 = localObject6;
                  localObject4 = localObject9;
                  localObject5 = localObject9;
                  str = ((Cursor)localObject9).getString(0);
                  localObject1 = localObject6;
                  localObject4 = localObject9;
                  localObject5 = localObject9;
                  localStringBuilder.setLength(0);
                  localObject1 = localObject6;
                  localObject4 = localObject9;
                  localObject5 = localObject9;
                  DatabaseUtils.appendEscapedSQLString(localStringBuilder, str);
                  localObject1 = localObject6;
                  localObject4 = localObject9;
                  localObject5 = localObject9;
                  localObject11 = localStringBuilder.toString();
                  localObject1 = localObject6;
                  localObject4 = localObject9;
                  localObject5 = localObject9;
                  localContact = (Contact)localHashMap.get(localObject8);
                  if (localContact == null) {
                    break label834;
                  }
                  localObject1 = localObject6;
                  localObject4 = localObject9;
                  localObject5 = localObject9;
                  if (!localContact.isGoodProvider)
                  {
                    localObject1 = localObject6;
                    localObject4 = localObject9;
                    localObject5 = localObject9;
                    if (!((String)localObject10).equals(localContact.provider))
                    {
                      localObject1 = localObject6;
                      localObject4 = localObject9;
                      localObject5 = localObject9;
                      localStringBuilder.setLength(0);
                      localObject1 = localObject6;
                      localObject4 = localObject9;
                      localObject5 = localObject9;
                      DatabaseUtils.appendEscapedSQLString(localStringBuilder, localContact.key);
                      localObject1 = localObject6;
                      localObject4 = localObject9;
                      localObject5 = localObject9;
                      localArrayList.remove(localStringBuilder.toString());
                      localObject1 = localObject6;
                      localObject4 = localObject9;
                      localObject5 = localObject9;
                      localArrayList.add(localObject11);
                      localObject1 = localObject6;
                      localObject4 = localObject9;
                      localObject5 = localObject9;
                      localContact.key = str;
                      localObject1 = localObject6;
                      localObject4 = localObject9;
                      localObject5 = localObject9;
                      localContact.isGoodProvider = bool;
                      localObject1 = localObject6;
                      localObject4 = localObject9;
                      localObject5 = localObject9;
                      localContact.provider = ((String)localObject10);
                    }
                  }
                }
              }
            }
          }
        }
      }
      catch (Throwable localThrowable)
      {
        HashMap localHashMap;
        boolean bool;
        String str;
        for (;;)
        {
          localObject5 = localObject4;
          FileLog.e(localThrowable);
          if (localObject1 != null)
          {
            localObject5 = localObject4;
            ((HashMap)localObject1).clear();
          }
          localObject5 = localObject1;
          if (localObject4 != null) {}
          try
          {
            ((Cursor)localObject4).close();
            localObject5 = localObject1;
          }
          catch (Exception localException2)
          {
            for (;;)
            {
              try
              {
                int k;
                int j;
                ((Cursor)localObject5).close();
                throw ((Throwable)localObject2);
                localObject3 = localThrowable;
                localObject4 = localObject9;
                localObject5 = localObject9;
                localContact.first_name = ((String)localObject11);
                localObject3 = localThrowable;
                localObject4 = localObject9;
                localObject5 = localObject9;
                localContact.last_name = "";
                continue;
                localObject3 = localThrowable;
                localObject4 = localObject9;
                localObject5 = localObject9;
                localObject11 = LocaleController.getString("PhoneMobile", 2131494152);
                continue;
                if (k == 1)
                {
                  localObject3 = localThrowable;
                  localObject4 = localObject9;
                  localObject5 = localObject9;
                  ((Contact)localObject10).phoneTypes.add(LocaleController.getString("PhoneHome", 2131494150));
                  continue;
                }
                if (k == 2)
                {
                  localObject3 = localThrowable;
                  localObject4 = localObject9;
                  localObject5 = localObject9;
                  ((Contact)localObject10).phoneTypes.add(LocaleController.getString("PhoneMobile", 2131494152));
                  continue;
                }
                if (k == 3)
                {
                  localObject3 = localThrowable;
                  localObject4 = localObject9;
                  localObject5 = localObject9;
                  ((Contact)localObject10).phoneTypes.add(LocaleController.getString("PhoneWork", 2131494158));
                  continue;
                }
                if (k == 12)
                {
                  localObject3 = localThrowable;
                  localObject4 = localObject9;
                  localObject5 = localObject9;
                  ((Contact)localObject10).phoneTypes.add(LocaleController.getString("PhoneMain", 2131494151));
                  continue;
                }
                localObject3 = localThrowable;
                localObject4 = localObject9;
                localObject5 = localObject9;
                ((Contact)localObject10).phoneTypes.add(LocaleController.getString("PhoneOther", 2131494157));
                continue;
                localObject3 = localThrowable;
                localObject4 = localObject9;
                localObject5 = localObject9;
              }
              catch (Exception localException2)
              {
                try
                {
                  ((Cursor)localObject9).close();
                  localObject8 = null;
                  localObject3 = localThrowable;
                  localObject4 = localObject8;
                  localObject5 = localObject8;
                  localObject9 = TextUtils.join(",", localArrayList);
                  localObject3 = localThrowable;
                  localObject4 = localObject8;
                  localObject5 = localObject8;
                  localObject8 = localContentResolver.query(ContactsContract.Data.CONTENT_URI, this.projectionNames, "lookup IN (" + (String)localObject9 + ") AND " + "mimetype" + " = '" + "vnd.android.cursor.item/name" + "'", null, null);
                  localObject3 = localObject8;
                  if (localObject8 != null)
                  {
                    localObject3 = localThrowable;
                    localObject4 = localObject8;
                    localObject5 = localObject8;
                    if (((Cursor)localObject8).moveToNext())
                    {
                      localObject3 = localThrowable;
                      localObject4 = localObject8;
                      localObject5 = localObject8;
                      localObject12 = ((Cursor)localObject8).getString(0);
                      localObject3 = localThrowable;
                      localObject4 = localObject8;
                      localObject5 = localObject8;
                      localObject9 = ((Cursor)localObject8).getString(1);
                      localObject3 = localThrowable;
                      localObject4 = localObject8;
                      localObject5 = localObject8;
                      localObject10 = ((Cursor)localObject8).getString(2);
                      localObject3 = localThrowable;
                      localObject4 = localObject8;
                      localObject5 = localObject8;
                      localObject11 = ((Cursor)localObject8).getString(3);
                      localObject3 = localThrowable;
                      localObject4 = localObject8;
                      localObject5 = localObject8;
                      localObject12 = (Contact)localThrowable.get(localObject12);
                      if (localObject12 == null) {
                        continue;
                      }
                      localObject3 = localThrowable;
                      localObject4 = localObject8;
                      localObject5 = localObject8;
                      if (((Contact)localObject12).namesFilled) {
                        continue;
                      }
                      localObject3 = localThrowable;
                      localObject4 = localObject8;
                      localObject5 = localObject8;
                      if (((Contact)localObject12).isGoodProvider)
                      {
                        if (localObject9 != null)
                        {
                          localObject3 = localThrowable;
                          localObject4 = localObject8;
                          localObject5 = localObject8;
                          ((Contact)localObject12).first_name = ((String)localObject9);
                          if (localObject10 != null)
                          {
                            localObject3 = localThrowable;
                            localObject4 = localObject8;
                            localObject5 = localObject8;
                            ((Contact)localObject12).last_name = ((String)localObject10);
                            localObject3 = localThrowable;
                            localObject4 = localObject8;
                            localObject5 = localObject8;
                            if (!TextUtils.isEmpty((CharSequence)localObject11))
                            {
                              localObject3 = localThrowable;
                              localObject4 = localObject8;
                              localObject5 = localObject8;
                              if (TextUtils.isEmpty(((Contact)localObject12).first_name)) {
                                continue;
                              }
                              localObject3 = localThrowable;
                              localObject4 = localObject8;
                              localObject5 = localObject8;
                              ((Contact)localObject12).first_name = (((Contact)localObject12).first_name + " " + (String)localObject11);
                            }
                            localObject3 = localThrowable;
                            localObject4 = localObject8;
                            localObject5 = localObject8;
                            ((Contact)localObject12).namesFilled = true;
                          }
                        }
                        else
                        {
                          localObject3 = localThrowable;
                          localObject4 = localObject8;
                          localObject5 = localObject8;
                          ((Contact)localObject12).first_name = "";
                          continue;
                        }
                        localObject3 = localThrowable;
                        localObject4 = localObject8;
                        localObject5 = localObject8;
                        ((Contact)localObject12).last_name = "";
                        continue;
                        localObject3 = localThrowable;
                        localObject4 = localObject8;
                        localObject5 = localObject8;
                        ((Contact)localObject12).first_name = ((String)localObject11);
                        continue;
                      }
                      localObject3 = localThrowable;
                      localObject4 = localObject8;
                      localObject5 = localObject8;
                      if (!isNotValidNameString((String)localObject9))
                      {
                        localObject3 = localThrowable;
                        localObject4 = localObject8;
                        localObject5 = localObject8;
                        if (!((Contact)localObject12).first_name.contains((CharSequence)localObject9))
                        {
                          localObject3 = localThrowable;
                          localObject4 = localObject8;
                          localObject5 = localObject8;
                          if (((String)localObject9).contains(((Contact)localObject12).first_name)) {}
                        }
                      }
                      else
                      {
                        localObject3 = localThrowable;
                        localObject4 = localObject8;
                        localObject5 = localObject8;
                        if (isNotValidNameString((String)localObject10)) {
                          continue;
                        }
                        localObject3 = localThrowable;
                        localObject4 = localObject8;
                        localObject5 = localObject8;
                        if (!((Contact)localObject12).last_name.contains((CharSequence)localObject10))
                        {
                          localObject3 = localThrowable;
                          localObject4 = localObject8;
                          localObject5 = localObject8;
                          if (!((String)localObject9).contains(((Contact)localObject12).last_name)) {
                            continue;
                          }
                        }
                      }
                      if (localObject9 != null)
                      {
                        localObject3 = localThrowable;
                        localObject4 = localObject8;
                        localObject5 = localObject8;
                        ((Contact)localObject12).first_name = ((String)localObject9);
                        localObject3 = localThrowable;
                        localObject4 = localObject8;
                        localObject5 = localObject8;
                        if (!TextUtils.isEmpty((CharSequence)localObject11))
                        {
                          localObject3 = localThrowable;
                          localObject4 = localObject8;
                          localObject5 = localObject8;
                          if (!TextUtils.isEmpty(((Contact)localObject12).first_name))
                          {
                            localObject3 = localThrowable;
                            localObject4 = localObject8;
                            localObject5 = localObject8;
                            ((Contact)localObject12).first_name = (((Contact)localObject12).first_name + " " + (String)localObject11);
                          }
                        }
                        else
                        {
                          if (localObject10 == null) {
                            continue;
                          }
                          localObject3 = localThrowable;
                          localObject4 = localObject8;
                          localObject5 = localObject8;
                          ((Contact)localObject12).last_name = ((String)localObject10);
                        }
                      }
                      else
                      {
                        localObject3 = localThrowable;
                        localObject4 = localObject8;
                        localObject5 = localObject8;
                        ((Contact)localObject12).first_name = "";
                        continue;
                      }
                      localObject3 = localThrowable;
                      localObject4 = localObject8;
                      localObject5 = localObject8;
                      ((Contact)localObject12).first_name = ((String)localObject11);
                      continue;
                      localObject3 = localThrowable;
                      localObject4 = localObject8;
                      localObject5 = localObject8;
                      ((Contact)localObject12).last_name = "";
                      continue;
                    }
                    localObject3 = localThrowable;
                    localObject4 = localObject8;
                    localObject5 = localObject8;
                  }
                }
                catch (Exception localException2)
                {
                  try
                  {
                    ((Cursor)localObject8).close();
                    Object localObject3 = null;
                    localObject5 = localThrowable;
                    if (localObject3 == null) {
                      continue;
                    }
                    try
                    {
                      ((Cursor)localObject3).close();
                      localObject5 = localThrowable;
                    }
                    catch (Exception localException1)
                    {
                      FileLog.e(localException1);
                      localObject5 = localThrowable;
                    }
                    continue;
                    localException4 = localException4;
                    FileLog.e(localException4);
                    localObject5 = localException1;
                    continue;
                    localException5 = localException5;
                    FileLog.e(localException5);
                    continue;
                    localException2 = localException2;
                  }
                  catch (Exception localException3)
                  {
                    continue;
                  }
                }
              }
              localObject10 = localObject11;
            }
          }
          localObject1 = localObject5;
          if (localObject5 != null) {
            break;
          }
          return new HashMap();
          bool = false;
        }
        label834:
        localObject1 = localThrowable;
        localObject4 = localObject9;
        localObject5 = localObject9;
        if (!localArrayList.contains(localObject11))
        {
          localObject1 = localThrowable;
          localObject4 = localObject9;
          localObject5 = localObject9;
          localArrayList.add(localObject11);
        }
        localObject1 = localThrowable;
        localObject4 = localObject9;
        localObject5 = localObject9;
        k = ((Cursor)localObject9).getInt(2);
        localObject1 = localThrowable;
        localObject4 = localObject9;
        localObject5 = localObject9;
        localObject11 = (Contact)localThrowable.get(str);
        if (localObject11 == null)
        {
          localObject1 = localThrowable;
          localObject4 = localObject9;
          localObject5 = localObject9;
          localContact = new Contact();
          localObject1 = localThrowable;
          localObject4 = localObject9;
          localObject5 = localObject9;
          localObject11 = ((Cursor)localObject9).getString(4);
          if (localObject11 == null)
          {
            localObject11 = "";
            label978:
            localObject1 = localThrowable;
            localObject4 = localObject9;
            localObject5 = localObject9;
            if (!isNotValidNameString((String)localObject11)) {
              break label1332;
            }
            localObject1 = localThrowable;
            localObject4 = localObject9;
            localObject5 = localObject9;
            localContact.first_name = ((String)localObject11);
            localObject1 = localThrowable;
            localObject4 = localObject9;
            localObject5 = localObject9;
          }
          for (localContact.last_name = "";; localContact.last_name = ((String)localObject11).substring(j + 1, ((String)localObject11).length()).trim())
          {
            localObject1 = localThrowable;
            localObject4 = localObject9;
            localObject5 = localObject9;
            localContact.provider = ((String)localObject10);
            localObject1 = localThrowable;
            localObject4 = localObject9;
            localObject5 = localObject9;
            localContact.isGoodProvider = bool;
            localObject1 = localThrowable;
            localObject4 = localObject9;
            localObject5 = localObject9;
            localContact.key = str;
            j = i + 1;
            localObject1 = localThrowable;
            localObject4 = localObject9;
            localObject5 = localObject9;
            localContact.contact_id = i;
            localObject1 = localThrowable;
            localObject4 = localObject9;
            localObject5 = localObject9;
            localThrowable.put(str, localContact);
            i = j;
            localObject10 = localContact;
            localObject1 = localThrowable;
            localObject4 = localObject9;
            localObject5 = localObject9;
            ((Contact)localObject10).shortPhones.add(localObject8);
            localObject1 = localThrowable;
            localObject4 = localObject9;
            localObject5 = localObject9;
            ((Contact)localObject10).phones.add(localObject12);
            localObject1 = localThrowable;
            localObject4 = localObject9;
            localObject5 = localObject9;
            ((Contact)localObject10).phoneDeleted.add(Integer.valueOf(0));
            if (k != 0) {
              break label1504;
            }
            localObject1 = localThrowable;
            localObject4 = localObject9;
            localObject5 = localObject9;
            localObject11 = ((Cursor)localObject9).getString(3);
            localObject1 = localThrowable;
            localObject4 = localObject9;
            localObject5 = localObject9;
            localObject12 = ((Contact)localObject10).phoneTypes;
            if (localObject11 == null) {
              break label1478;
            }
            localObject1 = localThrowable;
            localObject4 = localObject9;
            localObject5 = localObject9;
            ((ArrayList)localObject12).add(localObject11);
            localObject1 = localThrowable;
            localObject4 = localObject9;
            localObject5 = localObject9;
            localHashMap.put(localObject8, localObject10);
            break;
            localObject1 = localThrowable;
            localObject4 = localObject9;
            localObject5 = localObject9;
            localObject11 = ((String)localObject11).trim();
            break label978;
            label1332:
            localObject1 = localThrowable;
            localObject4 = localObject9;
            localObject5 = localObject9;
            j = ((String)localObject11).lastIndexOf(' ');
            if (j == -1) {
              break label1437;
            }
            localObject1 = localThrowable;
            localObject4 = localObject9;
            localObject5 = localObject9;
            localContact.first_name = ((String)localObject11).substring(0, j).trim();
            localObject1 = localThrowable;
            localObject4 = localObject9;
            localObject5 = localObject9;
          }
        }
      }
      finally
      {
        for (;;)
        {
          ContentResolver localContentResolver;
          ArrayList localArrayList;
          Contact localContact;
          if (localObject5 != null) {}
          label1437:
          label1478:
          label1504:
          label1690:
          label2673:
          int i = 1;
          Object localObject7 = localObject10;
        }
      }
    }
  }
  
  private void reloadContactsStatusesMaybe()
  {
    try
    {
      if (MessagesController.getMainSettings(this.currentAccount).getLong("lastReloadStatusTime", 0L) < System.currentTimeMillis() - 86400000L) {
        reloadContactsStatuses();
      }
      return;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
  }
  
  private void saveContactsLoadTime()
  {
    try
    {
      MessagesController.getMainSettings(this.currentAccount).edit().putLong("lastReloadStatusTime", System.currentTimeMillis()).commit();
      return;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
  }
  
  private void updateUnregisteredContacts(ArrayList<TLRPC.TL_contact> paramArrayList)
  {
    HashMap localHashMap = new HashMap();
    int i = 0;
    Object localObject2;
    if (i < paramArrayList.size())
    {
      localObject1 = (TLRPC.TL_contact)paramArrayList.get(i);
      localObject2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((TLRPC.TL_contact)localObject1).user_id));
      if ((localObject2 == null) || (TextUtils.isEmpty(((TLRPC.User)localObject2).phone))) {}
      for (;;)
      {
        i += 1;
        break;
        localHashMap.put(((TLRPC.User)localObject2).phone, localObject1);
      }
    }
    paramArrayList = new ArrayList();
    Object localObject1 = this.contactsBook.entrySet().iterator();
    if (((Iterator)localObject1).hasNext())
    {
      localObject2 = (Contact)((Map.Entry)((Iterator)localObject1).next()).getValue();
      int k = 0;
      i = 0;
      for (;;)
      {
        int j = k;
        if (i < ((Contact)localObject2).phones.size())
        {
          if ((localHashMap.containsKey((String)((Contact)localObject2).shortPhones.get(i))) || (((Integer)((Contact)localObject2).phoneDeleted.get(i)).intValue() == 1)) {
            j = 1;
          }
        }
        else
        {
          if (j != 0) {
            break;
          }
          paramArrayList.add(localObject2);
          break;
        }
        i += 1;
      }
    }
    Collections.sort(paramArrayList, new Comparator()
    {
      public int compare(ContactsController.Contact paramAnonymousContact1, ContactsController.Contact paramAnonymousContact2)
      {
        String str2 = paramAnonymousContact1.first_name;
        String str1 = str2;
        if (str2.length() == 0) {
          str1 = paramAnonymousContact1.last_name;
        }
        str2 = paramAnonymousContact2.first_name;
        paramAnonymousContact1 = str2;
        if (str2.length() == 0) {
          paramAnonymousContact1 = paramAnonymousContact2.last_name;
        }
        return str1.compareTo(paramAnonymousContact1);
      }
    });
    this.phoneBookContacts = paramArrayList;
  }
  
  public void addContact(TLRPC.User paramUser)
  {
    if ((paramUser == null) || (TextUtils.isEmpty(paramUser.phone))) {
      return;
    }
    TLRPC.TL_contacts_importContacts localTL_contacts_importContacts = new TLRPC.TL_contacts_importContacts();
    ArrayList localArrayList = new ArrayList();
    TLRPC.TL_inputPhoneContact localTL_inputPhoneContact = new TLRPC.TL_inputPhoneContact();
    localTL_inputPhoneContact.phone = paramUser.phone;
    if (!localTL_inputPhoneContact.phone.startsWith("+")) {
      localTL_inputPhoneContact.phone = ("+" + localTL_inputPhoneContact.phone);
    }
    localTL_inputPhoneContact.first_name = paramUser.first_name;
    localTL_inputPhoneContact.last_name = paramUser.last_name;
    localTL_inputPhoneContact.client_id = 0L;
    localArrayList.add(localTL_inputPhoneContact);
    localTL_contacts_importContacts.contacts = localArrayList;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_contacts_importContacts, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error != null) {
          return;
        }
        paramAnonymousTLObject = (TLRPC.TL_contacts_importedContacts)paramAnonymousTLObject;
        MessagesStorage.getInstance(ContactsController.this.currentAccount).putUsersAndChats(paramAnonymousTLObject.users, null, true, true);
        int i = 0;
        while (i < paramAnonymousTLObject.users.size())
        {
          paramAnonymousTL_error = (TLRPC.User)paramAnonymousTLObject.users.get(i);
          Utilities.phoneBookQueue.postRunnable(new Runnable()
          {
            public void run()
            {
              ContactsController.this.addContactToPhoneBook(paramAnonymousTL_error, true);
            }
          });
          Object localObject = new TLRPC.TL_contact();
          ((TLRPC.TL_contact)localObject).user_id = paramAnonymousTL_error.id;
          ArrayList localArrayList = new ArrayList();
          localArrayList.add(localObject);
          MessagesStorage.getInstance(ContactsController.this.currentAccount).putContacts(localArrayList, false);
          if (!TextUtils.isEmpty(paramAnonymousTL_error.phone))
          {
            ContactsController.formatName(paramAnonymousTL_error.first_name, paramAnonymousTL_error.last_name);
            MessagesStorage.getInstance(ContactsController.this.currentAccount).applyPhoneBookUpdates(paramAnonymousTL_error.phone, "");
            localObject = (ContactsController.Contact)ContactsController.this.contactsBookSPhones.get(paramAnonymousTL_error.phone);
            if (localObject != null)
            {
              int j = ((ContactsController.Contact)localObject).shortPhones.indexOf(paramAnonymousTL_error.phone);
              if (j != -1) {
                ((ContactsController.Contact)localObject).phoneDeleted.set(j, Integer.valueOf(0));
              }
            }
          }
          i += 1;
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            Iterator localIterator = paramAnonymousTLObject.users.iterator();
            while (localIterator.hasNext())
            {
              TLRPC.User localUser = (TLRPC.User)localIterator.next();
              MessagesController.getInstance(ContactsController.this.currentAccount).putUser(localUser, false);
              if (ContactsController.this.contactsDict.get(Integer.valueOf(localUser.id)) == null)
              {
                TLRPC.TL_contact localTL_contact = new TLRPC.TL_contact();
                localTL_contact.user_id = localUser.id;
                ContactsController.this.contacts.add(localTL_contact);
                ContactsController.this.contactsDict.put(Integer.valueOf(localTL_contact.user_id), localTL_contact);
              }
            }
            ContactsController.this.buildContactsSectionsArrays(true);
            NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
          }
        });
      }
    }, 6);
  }
  
  public long addContactToPhoneBook(TLRPC.User arg1, boolean paramBoolean)
  {
    if ((this.systemAccount == null) || (??? == null) || (TextUtils.isEmpty(???.phone))) {}
    while (!hasContactsPermission()) {
      return -1L;
    }
    l2 = -1L;
    synchronized (this.observerLock)
    {
      this.ignoreChanges = true;
      ??? = ApplicationLoader.applicationContext.getContentResolver();
      if (!paramBoolean) {}
    }
    try
    {
      ((ContentResolver)???).delete(ContactsContract.RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("caller_is_syncadapter", "true").appendQueryParameter("account_name", this.systemAccount.name).appendQueryParameter("account_type", this.systemAccount.type).build(), "sync2 = " + ???.id, null);
      ArrayList localArrayList = new ArrayList();
      ContentProviderOperation.Builder localBuilder = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI);
      localBuilder.withValue("account_name", this.systemAccount.name);
      localBuilder.withValue("account_type", this.systemAccount.type);
      localBuilder.withValue("sync1", ???.phone);
      localBuilder.withValue("sync2", Integer.valueOf(???.id));
      localArrayList.add(localBuilder.build());
      localBuilder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
      localBuilder.withValueBackReference("raw_contact_id", 0);
      localBuilder.withValue("mimetype", "vnd.android.cursor.item/name");
      localBuilder.withValue("data2", ???.first_name);
      localBuilder.withValue("data3", ???.last_name);
      localArrayList.add(localBuilder.build());
      localBuilder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
      localBuilder.withValueBackReference("raw_contact_id", 0);
      localBuilder.withValue("mimetype", "vnd.android.cursor.item/vnd.org.telegram.messenger.android.profile");
      localBuilder.withValue("data1", Integer.valueOf(???.id));
      localBuilder.withValue("data2", "Telegram Profile");
      localBuilder.withValue("data3", "+" + ???.phone);
      localBuilder.withValue("data4", Integer.valueOf(???.id));
      localArrayList.add(localBuilder.build());
      for (;;)
      {
        try
        {
          ??? = ((ContentResolver)???).applyBatch("com.android.contacts", localArrayList);
          l1 = l2;
          if (??? != null)
          {
            l1 = l2;
            if (???.length > 0)
            {
              l1 = l2;
              if (???[0].uri != null) {
                l1 = Long.parseLong(???[0].uri.getLastPathSegment());
              }
            }
          }
        }
        catch (Exception ???)
        {
          FileLog.e(???);
          long l1 = l2;
          continue;
        }
        synchronized (this.observerLock)
        {
          this.ignoreChanges = false;
          return l1;
        }
      }
      ??? = finally;
      throw ???;
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
    }
  }
  
  public void checkAppAccount()
  {
    localAccountManager = AccountManager.get(ApplicationLoader.applicationContext);
    try
    {
      arrayOfAccount = localAccountManager.getAccountsByType("org.telegram.messenger");
      this.systemAccount = null;
      i = 0;
    }
    catch (Throwable localThrowable)
    {
      try
      {
        for (;;)
        {
          Account[] arrayOfAccount;
          int i;
          Account localAccount;
          int m;
          int j;
          int k;
          TLRPC.User localUser;
          localAccountManager.removeAccount(arrayOfAccount[i], null, null);
          i += 1;
          continue;
          j += 1;
        }
        localThrowable = localThrowable;
        if (!UserConfig.getInstance(this.currentAccount).isClientActivated()) {
          break label226;
        }
        readContacts();
        if (this.systemAccount != null) {
          break label226;
        }
        try
        {
          this.systemAccount = new Account("" + UserConfig.getInstance(this.currentAccount).getClientUserId(), "org.telegram.messenger");
          localAccountManager.addAccountExplicitly(this.systemAccount, "", null);
          return;
        }
        catch (Exception localException1)
        {
          return;
        }
      }
      catch (Exception localException2)
      {
        for (;;) {}
      }
    }
    if (i < arrayOfAccount.length)
    {
      localAccount = arrayOfAccount[i];
      m = 0;
      j = 0;
      k = m;
      if (j < 3)
      {
        localUser = UserConfig.getInstance(j).getCurrentUser();
        if ((localUser != null) && (localAccount.name.equals("" + localUser.id)))
        {
          if (j == this.currentAccount) {
            this.systemAccount = localAccount;
          }
          k = 1;
        }
      }
      else if (k != 0) {}
    }
  }
  
  public void checkContacts()
  {
    Utilities.globalQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if (ContactsController.this.checkContactsInternal())
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("detected contacts change");
          }
          ContactsController.this.performSyncPhoneBook(ContactsController.this.getContactsCopy(ContactsController.this.contactsBook), true, false, true, false, true, false);
        }
      }
    });
  }
  
  public void checkInviteText()
  {
    Object localObject = MessagesController.getMainSettings(this.currentAccount);
    this.inviteLink = ((SharedPreferences)localObject).getString("invitelink", null);
    int i = ((SharedPreferences)localObject).getInt("invitelinktime", 0);
    if ((!this.updatingInviteLink) && ((this.inviteLink == null) || (Math.abs(System.currentTimeMillis() / 1000L - i) >= 86400L)))
    {
      this.updatingInviteLink = true;
      localObject = new TLRPC.TL_help_getInviteText();
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTLObject != null)
          {
            paramAnonymousTLObject = (TLRPC.TL_help_inviteText)paramAnonymousTLObject;
            if (paramAnonymousTLObject.message.length() != 0) {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  ContactsController.access$202(ContactsController.this, false);
                  SharedPreferences.Editor localEditor = MessagesController.getMainSettings(ContactsController.this.currentAccount).edit();
                  localEditor.putString("invitelink", ContactsController.access$402(ContactsController.this, paramAnonymousTLObject.message));
                  localEditor.putInt("invitelinktime", (int)(System.currentTimeMillis() / 1000L));
                  localEditor.commit();
                }
              });
            }
          }
        }
      }, 2);
    }
  }
  
  public void cleanup()
  {
    this.contactsBook.clear();
    this.contactsBookSPhones.clear();
    this.phoneBookContacts.clear();
    this.contacts.clear();
    this.contactsDict.clear();
    this.usersSectionsDict.clear();
    this.usersMutualSectionsDict.clear();
    this.sortedUsersSectionsArray.clear();
    this.sortedUsersMutualSectionsArray.clear();
    this.delayedContactsUpdate.clear();
    this.contactsByPhone.clear();
    this.contactsByShortPhone.clear();
    this.loadingContacts = false;
    this.contactsSyncInProgress = false;
    this.contactsLoaded = false;
    this.contactsBookLoaded = false;
    this.lastContactsVersions = "";
    this.loadingDeleteInfo = 0;
    this.deleteAccountTTL = 0;
    this.loadingLastSeenInfo = 0;
    this.loadingGroupInfo = 0;
    this.loadingCallsInfo = 0;
    Utilities.globalQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        ContactsController.access$002(ContactsController.this, false);
        ContactsController.access$102(ContactsController.this, 0);
      }
    });
    this.privacyRules = null;
  }
  
  public void deleteContact(final ArrayList<TLRPC.User> paramArrayList)
  {
    if ((paramArrayList == null) || (paramArrayList.isEmpty())) {
      return;
    }
    TLRPC.TL_contacts_deleteContacts localTL_contacts_deleteContacts = new TLRPC.TL_contacts_deleteContacts();
    final ArrayList localArrayList = new ArrayList();
    Iterator localIterator = paramArrayList.iterator();
    while (localIterator.hasNext())
    {
      TLRPC.User localUser = (TLRPC.User)localIterator.next();
      TLRPC.InputUser localInputUser = MessagesController.getInstance(this.currentAccount).getInputUser(localUser);
      if (localInputUser != null)
      {
        localArrayList.add(Integer.valueOf(localUser.id));
        localTL_contacts_deleteContacts.id.add(localInputUser);
      }
    }
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_contacts_deleteContacts, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error != null) {
          return;
        }
        MessagesStorage.getInstance(ContactsController.this.currentAccount).deleteContacts(localArrayList);
        Utilities.phoneBookQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            Iterator localIterator = ContactsController.22.this.val$users.iterator();
            while (localIterator.hasNext())
            {
              TLRPC.User localUser = (TLRPC.User)localIterator.next();
              ContactsController.this.deleteContactFromPhoneBook(localUser.id);
            }
          }
        });
        int i = 0;
        if (i < paramArrayList.size())
        {
          paramAnonymousTLObject = (TLRPC.User)paramArrayList.get(i);
          if (TextUtils.isEmpty(paramAnonymousTLObject.phone)) {}
          for (;;)
          {
            i += 1;
            break;
            UserObject.getUserName(paramAnonymousTLObject);
            MessagesStorage.getInstance(ContactsController.this.currentAccount).applyPhoneBookUpdates(paramAnonymousTLObject.phone, "");
            paramAnonymousTL_error = (ContactsController.Contact)ContactsController.this.contactsBookSPhones.get(paramAnonymousTLObject.phone);
            if (paramAnonymousTL_error != null)
            {
              int j = paramAnonymousTL_error.shortPhones.indexOf(paramAnonymousTLObject.phone);
              if (j != -1) {
                paramAnonymousTL_error.phoneDeleted.set(j, Integer.valueOf(1));
              }
            }
          }
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            int i = 0;
            Iterator localIterator = ContactsController.22.this.val$users.iterator();
            while (localIterator.hasNext())
            {
              TLRPC.User localUser = (TLRPC.User)localIterator.next();
              TLRPC.TL_contact localTL_contact = (TLRPC.TL_contact)ContactsController.this.contactsDict.get(Integer.valueOf(localUser.id));
              if (localTL_contact != null)
              {
                i = 1;
                ContactsController.this.contacts.remove(localTL_contact);
                ContactsController.this.contactsDict.remove(Integer.valueOf(localUser.id));
              }
            }
            if (i != 0) {
              ContactsController.this.buildContactsSectionsArrays(false);
            }
            NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(1) });
            NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
          }
        });
      }
    });
  }
  
  /* Error */
  public void deleteUnknownAppAccounts()
  {
    // Byte code:
    //   0: aload_0
    //   1: aconst_null
    //   2: putfield 691	org/telegram/messenger/ContactsController:systemAccount	Landroid/accounts/Account;
    //   5: getstatic 626	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   8: invokestatic 1056	android/accounts/AccountManager:get	(Landroid/content/Context;)Landroid/accounts/AccountManager;
    //   11: astore 6
    //   13: aload 6
    //   15: ldc_w 1058
    //   18: invokevirtual 1062	android/accounts/AccountManager:getAccountsByType	(Ljava/lang/String;)[Landroid/accounts/Account;
    //   21: astore 7
    //   23: iconst_0
    //   24: istore_1
    //   25: iload_1
    //   26: aload 7
    //   28: arraylength
    //   29: if_icmpge +113 -> 142
    //   32: aload 7
    //   34: iload_1
    //   35: aaload
    //   36: astore 8
    //   38: iconst_0
    //   39: istore 4
    //   41: iconst_0
    //   42: istore_2
    //   43: iload 4
    //   45: istore_3
    //   46: iload_2
    //   47: iconst_3
    //   48: if_icmpge +57 -> 105
    //   51: iload_2
    //   52: invokestatic 736	org/telegram/messenger/UserConfig:getInstance	(I)Lorg/telegram/messenger/UserConfig;
    //   55: invokevirtual 1066	org/telegram/messenger/UserConfig:getCurrentUser	()Lorg/telegram/tgnet/TLRPC$User;
    //   58: astore 9
    //   60: aload 9
    //   62: ifnull +66 -> 128
    //   65: aload 8
    //   67: getfield 696	android/accounts/Account:name	Ljava/lang/String;
    //   70: new 484	java/lang/StringBuilder
    //   73: dup
    //   74: invokespecial 485	java/lang/StringBuilder:<init>	()V
    //   77: ldc -64
    //   79: invokevirtual 491	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   82: aload 9
    //   84: getfield 1001	org/telegram/tgnet/TLRPC$User:id	I
    //   87: invokevirtual 494	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   90: invokevirtual 500	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   93: invokevirtual 669	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   96: istore 5
    //   98: iload 5
    //   100: ifeq +28 -> 128
    //   103: iconst_1
    //   104: istore_3
    //   105: iload_3
    //   106: ifne +15 -> 121
    //   109: aload 6
    //   111: aload 7
    //   113: iload_1
    //   114: aaload
    //   115: aconst_null
    //   116: aconst_null
    //   117: invokevirtual 1070	android/accounts/AccountManager:removeAccount	(Landroid/accounts/Account;Landroid/accounts/AccountManagerCallback;Landroid/os/Handler;)Landroid/accounts/AccountManagerFuture;
    //   120: pop
    //   121: iload_1
    //   122: iconst_1
    //   123: iadd
    //   124: istore_1
    //   125: goto -100 -> 25
    //   128: iload_2
    //   129: iconst_1
    //   130: iadd
    //   131: istore_2
    //   132: goto -89 -> 43
    //   135: astore 6
    //   137: aload 6
    //   139: invokevirtual 1142	java/lang/Exception:printStackTrace	()V
    //   142: return
    //   143: astore 8
    //   145: goto -24 -> 121
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	148	0	this	ContactsController
    //   24	101	1	i	int
    //   42	90	2	j	int
    //   45	61	3	k	int
    //   39	5	4	m	int
    //   96	3	5	bool	boolean
    //   11	99	6	localAccountManager	AccountManager
    //   135	3	6	localException1	Exception
    //   21	91	7	arrayOfAccount	Account[]
    //   36	30	8	localAccount	Account
    //   143	1	8	localException2	Exception
    //   58	25	9	localUser	TLRPC.User
    // Exception table:
    //   from	to	target	type
    //   0	23	135	java/lang/Exception
    //   25	32	135	java/lang/Exception
    //   51	60	135	java/lang/Exception
    //   65	98	135	java/lang/Exception
    //   109	121	143	java/lang/Exception
  }
  
  public void forceImportContacts()
  {
    Utilities.globalQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("force import contacts");
        }
        ContactsController.this.performSyncPhoneBook(new HashMap(), true, true, true, true, false, false);
      }
    });
  }
  
  public HashMap<String, Contact> getContactsCopy(HashMap<String, Contact> paramHashMap)
  {
    HashMap localHashMap = new HashMap();
    paramHashMap = paramHashMap.entrySet().iterator();
    while (paramHashMap.hasNext())
    {
      Object localObject = (Map.Entry)paramHashMap.next();
      Contact localContact = new Contact();
      localObject = (Contact)((Map.Entry)localObject).getValue();
      localContact.phoneDeleted.addAll(((Contact)localObject).phoneDeleted);
      localContact.phones.addAll(((Contact)localObject).phones);
      localContact.phoneTypes.addAll(((Contact)localObject).phoneTypes);
      localContact.shortPhones.addAll(((Contact)localObject).shortPhones);
      localContact.first_name = ((Contact)localObject).first_name;
      localContact.last_name = ((Contact)localObject).last_name;
      localContact.contact_id = ((Contact)localObject).contact_id;
      localContact.key = ((Contact)localObject).key;
      localHashMap.put(localContact.key, localContact);
    }
    return localHashMap;
  }
  
  public int getDeleteAccountTTL()
  {
    return this.deleteAccountTTL;
  }
  
  public String getInviteText(int paramInt)
  {
    if (this.inviteLink == null) {}
    for (String str1 = "https://telegram.org/dl"; paramInt <= 1; str1 = this.inviteLink) {
      return LocaleController.formatString("InviteText2", 2131493688, new Object[] { str1 });
    }
    try
    {
      String str2 = String.format(LocaleController.getPluralString("InviteTextNum", paramInt), new Object[] { Integer.valueOf(paramInt), str1 });
      return str2;
    }
    catch (Exception localException) {}
    return LocaleController.formatString("InviteText2", 2131493688, tmp81_78);
  }
  
  public boolean getLoadingCallsInfo()
  {
    return this.loadingCallsInfo != 2;
  }
  
  public boolean getLoadingDeleteInfo()
  {
    return this.loadingDeleteInfo != 2;
  }
  
  public boolean getLoadingGroupInfo()
  {
    return this.loadingGroupInfo != 2;
  }
  
  public boolean getLoadingLastSeenInfo()
  {
    return this.loadingLastSeenInfo != 2;
  }
  
  public ArrayList<TLRPC.PrivacyRule> getPrivacyRules(int paramInt)
  {
    if (paramInt == 2) {
      return this.callPrivacyRules;
    }
    if (paramInt == 1) {
      return this.groupPrivacyRules;
    }
    return this.privacyRules;
  }
  
  public boolean isLoadingContacts()
  {
    synchronized (this.loadContactsSync)
    {
      boolean bool = this.loadingContacts;
      return bool;
    }
  }
  
  public void loadContacts(boolean paramBoolean, final int paramInt)
  {
    synchronized (this.loadContactsSync)
    {
      this.loadingContacts = true;
      if (paramBoolean)
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("load contacts from cache");
        }
        MessagesStorage.getInstance(this.currentAccount).getContacts();
        return;
      }
    }
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("load contacts from server");
    }
    ??? = new TLRPC.TL_contacts_getContacts();
    ((TLRPC.TL_contacts_getContacts)???).hash = paramInt;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)???, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error == null)
        {
          paramAnonymousTLObject = (TLRPC.contacts_Contacts)paramAnonymousTLObject;
          if ((paramInt == 0) || (!(paramAnonymousTLObject instanceof TLRPC.TL_contacts_contactsNotModified))) {
            break label139;
          }
          ContactsController.this.contactsLoaded = true;
          if ((!ContactsController.this.delayedContactsUpdate.isEmpty()) && (ContactsController.this.contactsBookLoaded))
          {
            ContactsController.this.applyContactsUpdates(ContactsController.this.delayedContactsUpdate, null, null, null);
            ContactsController.this.delayedContactsUpdate.clear();
          }
          UserConfig.getInstance(ContactsController.this.currentAccount).lastContactsSyncTime = ((int)(System.currentTimeMillis() / 1000L));
          UserConfig.getInstance(ContactsController.this.currentAccount).saveConfig(false);
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              synchronized (ContactsController.this.loadContactsSync)
              {
                ContactsController.access$702(ContactsController.this, false);
                NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
                return;
              }
            }
          });
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("load contacts don't change");
          }
        }
        return;
        label139:
        UserConfig.getInstance(ContactsController.this.currentAccount).contactsSavedCount = paramAnonymousTLObject.saved_count;
        UserConfig.getInstance(ContactsController.this.currentAccount).saveConfig(false);
        ContactsController.this.processLoadedContacts(paramAnonymousTLObject.contacts, paramAnonymousTLObject.users, 0);
      }
    });
  }
  
  public void loadPrivacySettings()
  {
    Object localObject;
    if (this.loadingDeleteInfo == 0)
    {
      this.loadingDeleteInfo = 1;
      localObject = new TLRPC.TL_account_getAccountTTL();
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              if (paramAnonymousTL_error == null)
              {
                TLRPC.TL_accountDaysTTL localTL_accountDaysTTL = (TLRPC.TL_accountDaysTTL)paramAnonymousTLObject;
                ContactsController.access$2202(ContactsController.this, localTL_accountDaysTTL.days);
                ContactsController.access$2302(ContactsController.this, 2);
              }
              for (;;)
              {
                NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
                return;
                ContactsController.access$2302(ContactsController.this, 0);
              }
            }
          });
        }
      });
    }
    if (this.loadingLastSeenInfo == 0)
    {
      this.loadingLastSeenInfo = 1;
      localObject = new TLRPC.TL_account_getPrivacy();
      ((TLRPC.TL_account_getPrivacy)localObject).key = new TLRPC.TL_inputPrivacyKeyStatusTimestamp();
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              if (paramAnonymousTL_error == null)
              {
                TLRPC.TL_account_privacyRules localTL_account_privacyRules = (TLRPC.TL_account_privacyRules)paramAnonymousTLObject;
                MessagesController.getInstance(ContactsController.this.currentAccount).putUsers(localTL_account_privacyRules.users, false);
                ContactsController.access$2402(ContactsController.this, localTL_account_privacyRules.rules);
                ContactsController.access$2502(ContactsController.this, 2);
              }
              for (;;)
              {
                NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
                return;
                ContactsController.access$2502(ContactsController.this, 0);
              }
            }
          });
        }
      });
    }
    if (this.loadingCallsInfo == 0)
    {
      this.loadingCallsInfo = 1;
      localObject = new TLRPC.TL_account_getPrivacy();
      ((TLRPC.TL_account_getPrivacy)localObject).key = new TLRPC.TL_inputPrivacyKeyPhoneCall();
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              if (paramAnonymousTL_error == null)
              {
                TLRPC.TL_account_privacyRules localTL_account_privacyRules = (TLRPC.TL_account_privacyRules)paramAnonymousTLObject;
                MessagesController.getInstance(ContactsController.this.currentAccount).putUsers(localTL_account_privacyRules.users, false);
                ContactsController.access$2602(ContactsController.this, localTL_account_privacyRules.rules);
                ContactsController.access$2702(ContactsController.this, 2);
              }
              for (;;)
              {
                NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
                return;
                ContactsController.access$2702(ContactsController.this, 0);
              }
            }
          });
        }
      });
    }
    if (this.loadingGroupInfo == 0)
    {
      this.loadingGroupInfo = 1;
      localObject = new TLRPC.TL_account_getPrivacy();
      ((TLRPC.TL_account_getPrivacy)localObject).key = new TLRPC.TL_inputPrivacyKeyChatInvite();
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              if (paramAnonymousTL_error == null)
              {
                TLRPC.TL_account_privacyRules localTL_account_privacyRules = (TLRPC.TL_account_privacyRules)paramAnonymousTLObject;
                MessagesController.getInstance(ContactsController.this.currentAccount).putUsers(localTL_account_privacyRules.users, false);
                ContactsController.access$2802(ContactsController.this, localTL_account_privacyRules.rules);
                ContactsController.access$2902(ContactsController.this, 2);
              }
              for (;;)
              {
                NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
                return;
                ContactsController.access$2902(ContactsController.this, 0);
              }
            }
          });
        }
      });
    }
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
  }
  
  protected void markAsContacted(final String paramString)
  {
    if (paramString == null) {
      return;
    }
    Utilities.phoneBookQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        Uri localUri = Uri.parse(paramString);
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("last_time_contacted", Long.valueOf(System.currentTimeMillis()));
        ApplicationLoader.applicationContext.getContentResolver().update(localUri, localContentValues, null, null);
      }
    });
  }
  
  protected void migratePhoneBookToV7(final SparseArray<Contact> paramSparseArray)
  {
    Utilities.globalQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if (ContactsController.this.migratingContacts) {
          return;
        }
        ContactsController.access$002(ContactsController.this, true);
        HashMap localHashMap1 = new HashMap();
        Object localObject1 = ContactsController.this.readContactsFromPhoneBook();
        HashMap localHashMap2 = new HashMap();
        localObject1 = ((HashMap)localObject1).entrySet().iterator();
        Object localObject2;
        while (((Iterator)localObject1).hasNext())
        {
          localObject2 = (ContactsController.Contact)((Map.Entry)((Iterator)localObject1).next()).getValue();
          i = 0;
          while (i < ((ContactsController.Contact)localObject2).shortPhones.size())
          {
            localHashMap2.put(((ContactsController.Contact)localObject2).shortPhones.get(i), ((ContactsController.Contact)localObject2).key);
            i += 1;
          }
        }
        int i = 0;
        if (i < paramSparseArray.size())
        {
          localObject1 = (ContactsController.Contact)paramSparseArray.valueAt(i);
          int j = 0;
          for (;;)
          {
            if (j < ((ContactsController.Contact)localObject1).shortPhones.size())
            {
              localObject2 = (String)localHashMap2.get((String)((ContactsController.Contact)localObject1).shortPhones.get(j));
              if (localObject2 != null)
              {
                ((ContactsController.Contact)localObject1).key = ((String)localObject2);
                localHashMap1.put(localObject2, localObject1);
              }
            }
            else
            {
              i += 1;
              break;
            }
            j += 1;
          }
        }
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("migrated contacts " + localHashMap1.size() + " of " + paramSparseArray.size());
        }
        MessagesStorage.getInstance(ContactsController.this.currentAccount).putCachedPhoneBook(localHashMap1, true);
      }
    });
  }
  
  protected void performSyncPhoneBook(final HashMap<String, Contact> paramHashMap, final boolean paramBoolean1, final boolean paramBoolean2, final boolean paramBoolean3, final boolean paramBoolean4, final boolean paramBoolean5, final boolean paramBoolean6)
  {
    if ((!paramBoolean2) && (!this.contactsBookLoaded)) {
      return;
    }
    Utilities.globalQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int n = 0;
        int k = 0;
        int i1 = 0;
        final int j = 0;
        int m = 0;
        HashMap localHashMap3 = new HashMap();
        final Object localObject1 = paramHashMap.entrySet().iterator();
        final Object localObject2;
        final int i;
        while (((Iterator)localObject1).hasNext())
        {
          localObject2 = (ContactsController.Contact)((Map.Entry)((Iterator)localObject1).next()).getValue();
          i = 0;
          while (i < ((ContactsController.Contact)localObject2).shortPhones.size())
          {
            localHashMap3.put(((ContactsController.Contact)localObject2).shortPhones.get(i), localObject2);
            i += 1;
          }
        }
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("start read contacts from phone");
        }
        if (!paramBoolean3) {
          ContactsController.this.checkContactsInternal();
        }
        final HashMap localHashMap1 = ContactsController.this.readContactsFromPhoneBook();
        final HashMap localHashMap2 = new HashMap();
        int i4 = paramHashMap.size();
        ArrayList localArrayList = new ArrayList();
        final Object localObject3;
        Object localObject4;
        if (!paramHashMap.isEmpty())
        {
          Iterator localIterator = localHashMap1.entrySet().iterator();
          i = m;
          j = k;
          while (localIterator.hasNext())
          {
            localObject1 = (Map.Entry)localIterator.next();
            localObject3 = (String)((Map.Entry)localObject1).getKey();
            ContactsController.Contact localContact = (ContactsController.Contact)((Map.Entry)localObject1).getValue();
            localObject4 = (ContactsController.Contact)paramHashMap.get(localObject3);
            localObject2 = localObject4;
            localObject1 = localObject3;
            if (localObject4 == null)
            {
              k = 0;
              localObject2 = localObject4;
              localObject1 = localObject3;
              if (k < localContact.shortPhones.size())
              {
                localObject2 = (ContactsController.Contact)localHashMap3.get(localContact.shortPhones.get(k));
                if (localObject2 == null) {
                  break label546;
                }
                localObject1 = ((ContactsController.Contact)localObject2).key;
              }
            }
            if (localObject2 != null) {
              localContact.imported = ((ContactsController.Contact)localObject2).imported;
            }
            if ((localObject2 != null) && (((!TextUtils.isEmpty(localContact.first_name)) && (!((ContactsController.Contact)localObject2).first_name.equals(localContact.first_name))) || ((!TextUtils.isEmpty(localContact.last_name)) && (!((ContactsController.Contact)localObject2).last_name.equals(localContact.last_name)))))
            {
              m = 1;
              label396:
              if ((localObject2 != null) && (m == 0)) {
                break label725;
              }
              i1 = 0;
              n = i;
              k = j;
              j = i1;
              label417:
              if (j >= localContact.phones.size()) {
                break label697;
              }
              localObject3 = (String)localContact.shortPhones.get(j);
              ((String)localObject3).substring(Math.max(0, ((String)localObject3).length() - 7));
              localHashMap2.put(localObject3, localContact);
              if (localObject2 == null) {
                break label559;
              }
              i = ((ContactsController.Contact)localObject2).shortPhones.indexOf(localObject3);
              if (i == -1) {
                break label559;
              }
              localObject4 = (Integer)((ContactsController.Contact)localObject2).phoneDeleted.get(i);
              localContact.phoneDeleted.set(j, localObject4);
              if (((Integer)localObject4).intValue() != 1) {
                break label559;
              }
              i1 = n;
              i = k;
            }
            for (;;)
            {
              j += 1;
              k = i;
              n = i1;
              break label417;
              label546:
              k += 1;
              break;
              m = 0;
              break label396;
              label559:
              i = k;
              i1 = n;
              if (paramBoolean1)
              {
                i = k;
                if (m == 0)
                {
                  if (ContactsController.this.contactsByPhone.containsKey(localObject3))
                  {
                    i1 = n + 1;
                    i = k;
                  }
                  else
                  {
                    i = k + 1;
                  }
                }
                else
                {
                  localObject3 = new TLRPC.TL_inputPhoneContact();
                  ((TLRPC.TL_inputPhoneContact)localObject3).client_id = localContact.contact_id;
                  ((TLRPC.TL_inputPhoneContact)localObject3).client_id |= j << 32;
                  ((TLRPC.TL_inputPhoneContact)localObject3).first_name = localContact.first_name;
                  ((TLRPC.TL_inputPhoneContact)localObject3).last_name = localContact.last_name;
                  ((TLRPC.TL_inputPhoneContact)localObject3).phone = ((String)localContact.phones.get(j));
                  localArrayList.add(localObject3);
                  i1 = n;
                }
              }
            }
            label697:
            j = k;
            i = n;
            if (localObject2 != null)
            {
              paramHashMap.remove(localObject1);
              j = k;
              i = n;
              continue;
              label725:
              n = 0;
              m = i;
              k = j;
              if (n < localContact.phones.size())
              {
                localObject4 = (String)localContact.shortPhones.get(n);
                localObject3 = ((String)localObject4).substring(Math.max(0, ((String)localObject4).length() - 7));
                localHashMap2.put(localObject4, localContact);
                int i2 = ((ContactsController.Contact)localObject2).shortPhones.indexOf(localObject4);
                int i3 = 0;
                i1 = i3;
                j = i2;
                i = m;
                if (paramBoolean1)
                {
                  Object localObject5 = (TLRPC.TL_contact)ContactsController.this.contactsByPhone.get(localObject4);
                  if (localObject5 == null) {
                    break label1164;
                  }
                  localObject5 = MessagesController.getInstance(ContactsController.this.currentAccount).getUser(Integer.valueOf(((TLRPC.TL_contact)localObject5).user_id));
                  i1 = i3;
                  j = i2;
                  i = m;
                  if (localObject5 != null)
                  {
                    m += 1;
                    i1 = i3;
                    j = i2;
                    i = m;
                    if (TextUtils.isEmpty(((TLRPC.User)localObject5).first_name))
                    {
                      i1 = i3;
                      j = i2;
                      i = m;
                      if (TextUtils.isEmpty(((TLRPC.User)localObject5).last_name)) {
                        if (TextUtils.isEmpty(localContact.first_name))
                        {
                          i1 = i3;
                          j = i2;
                          i = m;
                          if (TextUtils.isEmpty(localContact.last_name)) {}
                        }
                        else
                        {
                          j = -1;
                          i1 = 1;
                          i = m;
                        }
                      }
                    }
                  }
                }
                label970:
                if (j == -1)
                {
                  m = k;
                  j = i;
                  if (paramBoolean1)
                  {
                    m = k;
                    j = i;
                    if (i1 != 0) {
                      break label1227;
                    }
                    localObject4 = (TLRPC.TL_contact)ContactsController.this.contactsByPhone.get(localObject4);
                    if (localObject4 == null) {
                      break label1313;
                    }
                    localObject4 = MessagesController.getInstance(ContactsController.this.currentAccount).getUser(Integer.valueOf(((TLRPC.TL_contact)localObject4).user_id));
                    if (localObject4 == null) {
                      break label1220;
                    }
                    i += 1;
                    if (((TLRPC.User)localObject4).first_name == null) {
                      break label1204;
                    }
                    localObject3 = ((TLRPC.User)localObject4).first_name;
                    label1066:
                    if (((TLRPC.User)localObject4).last_name == null) {
                      break label1212;
                    }
                    localObject4 = ((TLRPC.User)localObject4).last_name;
                    label1081:
                    if (((String)localObject3).equals(localContact.first_name))
                    {
                      m = k;
                      j = i;
                      if (((String)localObject4).equals(localContact.last_name)) {}
                    }
                    else
                    {
                      m = k;
                      j = i;
                      if (!TextUtils.isEmpty(localContact.first_name)) {
                        break label1227;
                      }
                      m = k;
                      j = i;
                      if (!TextUtils.isEmpty(localContact.last_name)) {
                        break label1227;
                      }
                      j = i;
                      m = k;
                    }
                  }
                }
                for (;;)
                {
                  n += 1;
                  k = m;
                  m = j;
                  break;
                  label1164:
                  i1 = i3;
                  j = i2;
                  i = m;
                  if (!ContactsController.this.contactsByShortPhone.containsKey(localObject3)) {
                    break label970;
                  }
                  i = m + 1;
                  i1 = i3;
                  j = i2;
                  break label970;
                  label1204:
                  localObject3 = "";
                  break label1066;
                  label1212:
                  localObject4 = "";
                  break label1081;
                  label1220:
                  m = k + 1;
                  j = i;
                  for (;;)
                  {
                    label1227:
                    localObject3 = new TLRPC.TL_inputPhoneContact();
                    ((TLRPC.TL_inputPhoneContact)localObject3).client_id = localContact.contact_id;
                    ((TLRPC.TL_inputPhoneContact)localObject3).client_id |= n << 32;
                    ((TLRPC.TL_inputPhoneContact)localObject3).first_name = localContact.first_name;
                    ((TLRPC.TL_inputPhoneContact)localObject3).last_name = localContact.last_name;
                    ((TLRPC.TL_inputPhoneContact)localObject3).phone = ((String)localContact.phones.get(n));
                    localArrayList.add(localObject3);
                    break;
                    label1313:
                    m = k;
                    j = i;
                    if (ContactsController.this.contactsByShortPhone.containsKey(localObject3))
                    {
                      j = i + 1;
                      m = k;
                    }
                  }
                  localContact.phoneDeleted.set(n, ((ContactsController.Contact)localObject2).phoneDeleted.get(j));
                  ((ContactsController.Contact)localObject2).phones.remove(j);
                  ((ContactsController.Contact)localObject2).shortPhones.remove(j);
                  ((ContactsController.Contact)localObject2).phoneDeleted.remove(j);
                  ((ContactsController.Contact)localObject2).phoneTypes.remove(j);
                  m = k;
                  j = i;
                }
              }
              j = k;
              i = m;
              if (((ContactsController.Contact)localObject2).phones.isEmpty())
              {
                paramHashMap.remove(localObject1);
                j = k;
                i = m;
              }
            }
          }
          if ((!paramBoolean2) && (paramHashMap.isEmpty()) && (localArrayList.isEmpty()) && (i4 == localHashMap1.size())) {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("contacts not changed!");
            }
          }
        }
        label1944:
        label1959:
        label2032:
        label2061:
        label2201:
        label2454:
        do
        {
          for (;;)
          {
            return;
            m = j;
            k = i;
            if (paramBoolean1)
            {
              m = j;
              k = i;
              if (!paramHashMap.isEmpty())
              {
                m = j;
                k = i;
                if (!localHashMap1.isEmpty())
                {
                  if (localArrayList.isEmpty()) {
                    MessagesStorage.getInstance(ContactsController.this.currentAccount).putCachedPhoneBook(localHashMap1, false);
                  }
                  m = j;
                  k = i;
                  if (1 == 0)
                  {
                    m = j;
                    k = i;
                    if (!paramHashMap.isEmpty())
                    {
                      AndroidUtilities.runOnUIThread(new Runnable()
                      {
                        public void run()
                        {
                          ArrayList localArrayList = new ArrayList();
                          if ((ContactsController.9.this.val$contactHashMap != null) && (!ContactsController.9.this.val$contactHashMap.isEmpty())) {
                            try
                            {
                              HashMap localHashMap = new HashMap();
                              i = 0;
                              if (i >= ContactsController.this.contacts.size()) {
                                break label168;
                              }
                              localObject = (TLRPC.TL_contact)ContactsController.this.contacts.get(i);
                              localObject = MessagesController.getInstance(ContactsController.this.currentAccount).getUser(Integer.valueOf(((TLRPC.TL_contact)localObject).user_id));
                              if ((localObject == null) || (TextUtils.isEmpty(((TLRPC.User)localObject).phone))) {
                                break label316;
                              }
                              localHashMap.put(((TLRPC.User)localObject).phone, localObject);
                            }
                            catch (Exception localException)
                            {
                              FileLog.e(localException);
                            }
                          }
                          label147:
                          if (!localArrayList.isEmpty()) {
                            ContactsController.this.deleteContact(localArrayList);
                          }
                          return;
                          label168:
                          int j = 0;
                          Object localObject = ContactsController.9.this.val$contactHashMap.entrySet().iterator();
                          label187:
                          ContactsController.Contact localContact;
                          int m;
                          if (((Iterator)localObject).hasNext())
                          {
                            localContact = (ContactsController.Contact)((Map.Entry)((Iterator)localObject).next()).getValue();
                            m = 0;
                          }
                          int k;
                          for (int i = 0;; i = k + 1) {
                            if (i < localContact.shortPhones.size())
                            {
                              TLRPC.User localUser = (TLRPC.User)localException.get((String)localContact.shortPhones.get(i));
                              k = i;
                              if (localUser != null)
                              {
                                m = 1;
                                localArrayList.add(localUser);
                                localContact.shortPhones.remove(i);
                                k = i - 1;
                              }
                            }
                            else
                            {
                              if (m != 0)
                              {
                                i = localContact.shortPhones.size();
                                if (i != 0) {
                                  break label187;
                                }
                              }
                              j += 1;
                              break label187;
                              break label147;
                              label316:
                              i += 1;
                              break;
                            }
                          }
                        }
                      });
                      k = i;
                      m = j;
                    }
                  }
                }
              }
            }
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("done processing contacts");
            }
            if (!paramBoolean1) {
              break label2454;
            }
            if (localArrayList.isEmpty()) {
              break;
            }
            if (BuildVars.LOGS_ENABLED) {
              FileLog.e("start import contacts");
            }
            if ((paramBoolean5) && (m != 0)) {
              if (m >= 30) {
                i = 1;
              }
            }
            for (;;)
            {
              if (BuildVars.LOGS_ENABLED) {
                FileLog.d("new phone book contacts " + m + " serverContactsInPhonebook " + k + " totalContacts " + ContactsController.this.contactsByPhone.size());
              }
              if (i == 0) {
                break label2201;
              }
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.hasNewContactsToImport, new Object[] { Integer.valueOf(i), ContactsController.9.this.val$contactHashMap, Boolean.valueOf(ContactsController.9.this.val$first), Boolean.valueOf(ContactsController.9.this.val$schedule) });
                }
              });
              return;
              m = n;
              k = i1;
              if (!paramBoolean1) {
                break;
              }
              localObject3 = localHashMap1.entrySet().iterator();
              i = j;
              do
              {
                m = n;
                k = i;
                if (!((Iterator)localObject3).hasNext()) {
                  break;
                }
                localObject1 = (Map.Entry)((Iterator)localObject3).next();
                localObject4 = (ContactsController.Contact)((Map.Entry)localObject1).getValue();
                localObject1 = (String)((Map.Entry)localObject1).getKey();
                k = 0;
                j = i;
                i = j;
              } while (k >= ((ContactsController.Contact)localObject4).phones.size());
              i = j;
              if (!paramBoolean4)
              {
                localObject2 = (String)((ContactsController.Contact)localObject4).shortPhones.get(k);
                localObject1 = ((String)localObject2).substring(Math.max(0, ((String)localObject2).length() - 7));
                localObject2 = (TLRPC.TL_contact)ContactsController.this.contactsByPhone.get(localObject2);
                if (localObject2 != null)
                {
                  localObject2 = MessagesController.getInstance(ContactsController.this.currentAccount).getUser(Integer.valueOf(((TLRPC.TL_contact)localObject2).user_id));
                  i = j;
                  if (localObject2 == null) {
                    break label2061;
                  }
                  j += 1;
                  if (((TLRPC.User)localObject2).first_name != null)
                  {
                    localObject1 = ((TLRPC.User)localObject2).first_name;
                    if (((TLRPC.User)localObject2).last_name == null) {
                      break label2032;
                    }
                    localObject2 = ((TLRPC.User)localObject2).last_name;
                    if (((String)localObject1).equals(((ContactsController.Contact)localObject4).first_name))
                    {
                      i = j;
                      if (((String)localObject2).equals(((ContactsController.Contact)localObject4).last_name)) {}
                    }
                    else
                    {
                      i = j;
                      if (!TextUtils.isEmpty(((ContactsController.Contact)localObject4).first_name)) {
                        break label2061;
                      }
                      i = j;
                      if (!TextUtils.isEmpty(((ContactsController.Contact)localObject4).last_name)) {
                        break label2061;
                      }
                      i = j;
                    }
                  }
                }
              }
              for (;;)
              {
                k += 1;
                j = i;
                break;
                localObject1 = "";
                break label1944;
                localObject2 = "";
                break label1959;
                i = j;
                if (ContactsController.this.contactsByShortPhone.containsKey(localObject1)) {
                  i = j + 1;
                }
                localObject1 = new TLRPC.TL_inputPhoneContact();
                ((TLRPC.TL_inputPhoneContact)localObject1).client_id = ((ContactsController.Contact)localObject4).contact_id;
                ((TLRPC.TL_inputPhoneContact)localObject1).client_id |= k << 32;
                ((TLRPC.TL_inputPhoneContact)localObject1).first_name = ((ContactsController.Contact)localObject4).first_name;
                ((TLRPC.TL_inputPhoneContact)localObject1).last_name = ((ContactsController.Contact)localObject4).last_name;
                ((TLRPC.TL_inputPhoneContact)localObject1).phone = ((String)((ContactsController.Contact)localObject4).phones.get(k));
                localArrayList.add(localObject1);
              }
              if ((paramBoolean2) && (i4 == 0) && (ContactsController.this.contactsByPhone.size() - k > ContactsController.this.contactsByPhone.size() / 3 * 2))
              {
                i = 2;
              }
              else
              {
                i = 0;
                continue;
                i = 0;
              }
            }
            if (paramBoolean6)
            {
              Utilities.stageQueue.postRunnable(new Runnable()
              {
                public void run()
                {
                  ContactsController.this.contactsBookSPhones = localHashMap2;
                  ContactsController.this.contactsBook = localHashMap1;
                  ContactsController.access$902(ContactsController.this, false);
                  ContactsController.access$1002(ContactsController.this, true);
                  if (ContactsController.9.this.val$first) {
                    ContactsController.this.contactsLoaded = true;
                  }
                  if ((!ContactsController.this.delayedContactsUpdate.isEmpty()) && (ContactsController.this.contactsLoaded))
                  {
                    ContactsController.this.applyContactsUpdates(ContactsController.this.delayedContactsUpdate, null, null, null);
                    ContactsController.this.delayedContactsUpdate.clear();
                  }
                  MessagesStorage.getInstance(ContactsController.this.currentAccount).putCachedPhoneBook(localHashMap1, false);
                  AndroidUtilities.runOnUIThread(new Runnable()
                  {
                    public void run()
                    {
                      ContactsController.this.updateUnregisteredContacts(ContactsController.this.contacts);
                      NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
                      NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsImported, new Object[0]);
                    }
                  });
                }
              });
              return;
            }
            localObject1 = new HashMap(localHashMap1);
            localObject2 = new SparseArray();
            localObject3 = ((HashMap)localObject1).entrySet().iterator();
            while (((Iterator)localObject3).hasNext())
            {
              localObject4 = (ContactsController.Contact)((Map.Entry)((Iterator)localObject3).next()).getValue();
              ((SparseArray)localObject2).put(((ContactsController.Contact)localObject4).contact_id, ((ContactsController.Contact)localObject4).key);
            }
            ContactsController.access$102(ContactsController.this, 0);
            j = (int)Math.ceil(localArrayList.size() / 500.0D);
            i = 0;
            while (i < j)
            {
              localObject3 = new TLRPC.TL_contacts_importContacts();
              k = i * 500;
              ((TLRPC.TL_contacts_importContacts)localObject3).contacts = new ArrayList(localArrayList.subList(k, Math.min(k + 500, localArrayList.size())));
              ConnectionsManager.getInstance(ContactsController.this.currentAccount).sendRequest((TLObject)localObject3, new RequestDelegate()
              {
                public void run(TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
                {
                  ContactsController.access$108(ContactsController.this);
                  int i;
                  if (paramAnonymous2TL_error == null)
                  {
                    if (BuildVars.LOGS_ENABLED) {
                      FileLog.d("contacts imported");
                    }
                    paramAnonymous2TLObject = (TLRPC.TL_contacts_importedContacts)paramAnonymous2TLObject;
                    if (!paramAnonymous2TLObject.retry_contacts.isEmpty())
                    {
                      i = 0;
                      while (i < paramAnonymous2TLObject.retry_contacts.size())
                      {
                        long l = ((Long)paramAnonymous2TLObject.retry_contacts.get(i)).longValue();
                        localObject1.remove(localObject2.get((int)l));
                        i += 1;
                      }
                      this.val$hasErrors[0] = true;
                      if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("result has retry contacts");
                      }
                    }
                    i = 0;
                    Object localObject;
                    while (i < paramAnonymous2TLObject.popular_invites.size())
                    {
                      paramAnonymous2TL_error = (TLRPC.TL_popularContact)paramAnonymous2TLObject.popular_invites.get(i);
                      localObject = (ContactsController.Contact)localHashMap1.get(localObject2.get((int)paramAnonymous2TL_error.client_id));
                      if (localObject != null) {
                        ((ContactsController.Contact)localObject).imported = paramAnonymous2TL_error.importers;
                      }
                      i += 1;
                    }
                    MessagesStorage.getInstance(ContactsController.this.currentAccount).putUsersAndChats(paramAnonymous2TLObject.users, null, true, true);
                    paramAnonymous2TL_error = new ArrayList();
                    i = 0;
                    while (i < paramAnonymous2TLObject.imported.size())
                    {
                      localObject = new TLRPC.TL_contact();
                      ((TLRPC.TL_contact)localObject).user_id = ((TLRPC.TL_importedContact)paramAnonymous2TLObject.imported.get(i)).user_id;
                      paramAnonymous2TL_error.add(localObject);
                      i += 1;
                    }
                    ContactsController.this.processLoadedContacts(paramAnonymous2TL_error, paramAnonymous2TLObject.users, 2);
                  }
                  for (;;)
                  {
                    if (ContactsController.this.completedRequestsCount == j)
                    {
                      if (!localObject1.isEmpty()) {
                        MessagesStorage.getInstance(ContactsController.this.currentAccount).putCachedPhoneBook(localObject1, false);
                      }
                      Utilities.stageQueue.postRunnable(new Runnable()
                      {
                        public void run()
                        {
                          ContactsController.this.contactsBookSPhones = ContactsController.9.4.this.val$contactsBookShort;
                          ContactsController.this.contactsBook = ContactsController.9.4.this.val$contactsMap;
                          ContactsController.access$902(ContactsController.this, false);
                          ContactsController.access$1002(ContactsController.this, true);
                          if (ContactsController.9.this.val$first) {
                            ContactsController.this.contactsLoaded = true;
                          }
                          if ((!ContactsController.this.delayedContactsUpdate.isEmpty()) && (ContactsController.this.contactsLoaded))
                          {
                            ContactsController.this.applyContactsUpdates(ContactsController.this.delayedContactsUpdate, null, null, null);
                            ContactsController.this.delayedContactsUpdate.clear();
                          }
                          AndroidUtilities.runOnUIThread(new Runnable()
                          {
                            public void run()
                            {
                              NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsImported, new Object[0]);
                            }
                          });
                          if (ContactsController.9.4.this.val$hasErrors[0] != 0) {
                            Utilities.globalQueue.postRunnable(new Runnable()
                            {
                              public void run()
                              {
                                MessagesStorage.getInstance(ContactsController.this.currentAccount).getCachedPhoneBook(true);
                              }
                            }, 1800000L);
                          }
                        }
                      });
                    }
                    return;
                    i = 0;
                    while (i < localObject3.contacts.size())
                    {
                      paramAnonymous2TLObject = (TLRPC.TL_inputPhoneContact)localObject3.contacts.get(i);
                      localObject1.remove(localObject2.get((int)paramAnonymous2TLObject.client_id));
                      i += 1;
                    }
                    this.val$hasErrors[0] = true;
                    if (BuildVars.LOGS_ENABLED) {
                      FileLog.d("import contacts error " + paramAnonymous2TL_error.text);
                    }
                  }
                }
              }, 6);
              i += 1;
            }
          }
          Utilities.stageQueue.postRunnable(new Runnable()
          {
            public void run()
            {
              ContactsController.this.contactsBookSPhones = localHashMap2;
              ContactsController.this.contactsBook = localHashMap1;
              ContactsController.access$902(ContactsController.this, false);
              ContactsController.access$1002(ContactsController.this, true);
              if (ContactsController.9.this.val$first) {
                ContactsController.this.contactsLoaded = true;
              }
              if ((!ContactsController.this.delayedContactsUpdate.isEmpty()) && (ContactsController.this.contactsLoaded))
              {
                ContactsController.this.applyContactsUpdates(ContactsController.this.delayedContactsUpdate, null, null, null);
                ContactsController.this.delayedContactsUpdate.clear();
              }
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  ContactsController.this.updateUnregisteredContacts(ContactsController.this.contacts);
                  NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
                  NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsImported, new Object[0]);
                }
              });
            }
          });
          return;
          Utilities.stageQueue.postRunnable(new Runnable()
          {
            public void run()
            {
              ContactsController.this.contactsBookSPhones = localHashMap2;
              ContactsController.this.contactsBook = localHashMap1;
              ContactsController.access$902(ContactsController.this, false);
              ContactsController.access$1002(ContactsController.this, true);
              if (ContactsController.9.this.val$first) {
                ContactsController.this.contactsLoaded = true;
              }
              if ((!ContactsController.this.delayedContactsUpdate.isEmpty()) && (ContactsController.this.contactsLoaded) && (ContactsController.this.contactsBookLoaded))
              {
                ContactsController.this.applyContactsUpdates(ContactsController.this.delayedContactsUpdate, null, null, null);
                ContactsController.this.delayedContactsUpdate.clear();
              }
            }
          });
        } while (localHashMap1.isEmpty());
        MessagesStorage.getInstance(ContactsController.this.currentAccount).putCachedPhoneBook(localHashMap1, false);
      }
    });
  }
  
  public void processContactsUpdates(ArrayList<Integer> paramArrayList, ConcurrentHashMap<Integer, TLRPC.User> paramConcurrentHashMap)
  {
    ArrayList localArrayList1 = new ArrayList();
    ArrayList localArrayList2 = new ArrayList();
    Iterator localIterator = paramArrayList.iterator();
    while (localIterator.hasNext())
    {
      Integer localInteger = (Integer)localIterator.next();
      int i;
      if (localInteger.intValue() > 0)
      {
        TLRPC.TL_contact localTL_contact = new TLRPC.TL_contact();
        localTL_contact.user_id = localInteger.intValue();
        localArrayList1.add(localTL_contact);
        if (!this.delayedContactsUpdate.isEmpty())
        {
          i = this.delayedContactsUpdate.indexOf(Integer.valueOf(-localInteger.intValue()));
          if (i != -1) {
            this.delayedContactsUpdate.remove(i);
          }
        }
      }
      else if (localInteger.intValue() < 0)
      {
        localArrayList2.add(Integer.valueOf(-localInteger.intValue()));
        if (!this.delayedContactsUpdate.isEmpty())
        {
          i = this.delayedContactsUpdate.indexOf(Integer.valueOf(-localInteger.intValue()));
          if (i != -1) {
            this.delayedContactsUpdate.remove(i);
          }
        }
      }
    }
    if (!localArrayList2.isEmpty()) {
      MessagesStorage.getInstance(this.currentAccount).deleteContacts(localArrayList2);
    }
    if (!localArrayList1.isEmpty()) {
      MessagesStorage.getInstance(this.currentAccount).putContacts(localArrayList1, false);
    }
    if ((!this.contactsLoaded) || (!this.contactsBookLoaded))
    {
      this.delayedContactsUpdate.addAll(paramArrayList);
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("delay update - contacts add = " + localArrayList1.size() + " delete = " + localArrayList2.size());
      }
      return;
    }
    applyContactsUpdates(paramArrayList, paramConcurrentHashMap, localArrayList1, localArrayList2);
  }
  
  public void processLoadedContacts(final ArrayList<TLRPC.TL_contact> paramArrayList, final ArrayList<TLRPC.User> paramArrayList1, final int paramInt)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        final boolean bool = true;
        final Object localObject1 = MessagesController.getInstance(ContactsController.this.currentAccount);
        Object localObject2 = paramArrayList1;
        if (paramInt == 1) {}
        for (;;)
        {
          ((MessagesController)localObject1).putUsers((ArrayList)localObject2, bool);
          localObject1 = new SparseArray();
          bool = paramArrayList.isEmpty();
          if (ContactsController.this.contacts.isEmpty()) {
            break label155;
          }
          int j;
          for (i = 0; i < paramArrayList.size(); i = j + 1)
          {
            localObject2 = (TLRPC.TL_contact)paramArrayList.get(i);
            j = i;
            if (ContactsController.this.contactsDict.get(Integer.valueOf(((TLRPC.TL_contact)localObject2).user_id)) != null)
            {
              paramArrayList.remove(i);
              j = i - 1;
            }
          }
          bool = false;
        }
        paramArrayList.addAll(ContactsController.this.contacts);
        label155:
        int i = 0;
        while (i < paramArrayList.size())
        {
          localObject2 = MessagesController.getInstance(ContactsController.this.currentAccount).getUser(Integer.valueOf(((TLRPC.TL_contact)paramArrayList.get(i)).user_id));
          if (localObject2 != null) {
            ((SparseArray)localObject1).put(((TLRPC.User)localObject2).id, localObject2);
          }
          i += 1;
        }
        Utilities.stageQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("done loading contacts");
            }
            if ((ContactsController.12.this.val$from == 1) && ((ContactsController.12.this.val$contactsArr.isEmpty()) || (Math.abs(System.currentTimeMillis() / 1000L - UserConfig.getInstance(ContactsController.this.currentAccount).lastContactsSyncTime) >= 86400L)))
            {
              ContactsController.this.loadContacts(false, ContactsController.this.getContactsHash(ContactsController.12.this.val$contactsArr));
              if (ContactsController.12.this.val$contactsArr.isEmpty()) {
                return;
              }
            }
            if (ContactsController.12.this.val$from == 0)
            {
              UserConfig.getInstance(ContactsController.this.currentAccount).lastContactsSyncTime = ((int)(System.currentTimeMillis() / 1000L));
              UserConfig.getInstance(ContactsController.this.currentAccount).saveConfig(false);
            }
            int i = 0;
            for (;;)
            {
              if (i >= ContactsController.12.this.val$contactsArr.size()) {
                break label262;
              }
              localObject1 = (TLRPC.TL_contact)ContactsController.12.this.val$contactsArr.get(i);
              if ((localObject1.get(((TLRPC.TL_contact)localObject1).user_id) == null) && (((TLRPC.TL_contact)localObject1).user_id != UserConfig.getInstance(ContactsController.this.currentAccount).getClientUserId()))
              {
                ContactsController.this.loadContacts(false, 0);
                if (!BuildVars.LOGS_ENABLED) {
                  break;
                }
                FileLog.d("contacts are broken, load from server");
                return;
              }
              i += 1;
            }
            label262:
            final Object localObject2;
            if (ContactsController.12.this.val$from != 1)
            {
              MessagesStorage.getInstance(ContactsController.this.currentAccount).putUsersAndChats(ContactsController.12.this.val$usersArr, null, true, true);
              localObject1 = MessagesStorage.getInstance(ContactsController.this.currentAccount);
              localObject2 = ContactsController.12.this.val$contactsArr;
              if (ContactsController.12.this.val$from == 2) {
                break label507;
              }
            }
            final ConcurrentHashMap localConcurrentHashMap;
            final HashMap localHashMap2;
            final HashMap localHashMap3;
            final ArrayList localArrayList2;
            final ArrayList localArrayList3;
            final HashMap localHashMap1;
            TLRPC.TL_contact localTL_contact;
            TLRPC.User localUser;
            label507:
            for (boolean bool = true;; bool = false)
            {
              ((MessagesStorage)localObject1).putContacts((ArrayList)localObject2, bool);
              Collections.sort(ContactsController.12.this.val$contactsArr, new Comparator()
              {
                public int compare(TLRPC.TL_contact paramAnonymous3TL_contact1, TLRPC.TL_contact paramAnonymous3TL_contact2)
                {
                  paramAnonymous3TL_contact1 = (TLRPC.User)ContactsController.12.1.this.val$usersDict.get(paramAnonymous3TL_contact1.user_id);
                  paramAnonymous3TL_contact2 = (TLRPC.User)ContactsController.12.1.this.val$usersDict.get(paramAnonymous3TL_contact2.user_id);
                  return UserObject.getFirstName(paramAnonymous3TL_contact1).compareTo(UserObject.getFirstName(paramAnonymous3TL_contact2));
                }
              });
              localConcurrentHashMap = new ConcurrentHashMap(20, 1.0F, 2);
              localHashMap2 = new HashMap();
              localHashMap3 = new HashMap();
              localArrayList2 = new ArrayList();
              localArrayList3 = new ArrayList();
              localObject2 = null;
              localHashMap1 = null;
              if (!ContactsController.this.contactsBookLoaded)
              {
                localObject2 = new HashMap();
                localHashMap1 = new HashMap();
              }
              i = 0;
              for (;;)
              {
                if (i >= ContactsController.12.this.val$contactsArr.size()) {
                  break label777;
                }
                localTL_contact = (TLRPC.TL_contact)ContactsController.12.this.val$contactsArr.get(i);
                localUser = (TLRPC.User)localObject1.get(localTL_contact.user_id);
                if (localUser != null) {
                  break;
                }
                i += 1;
              }
            }
            localConcurrentHashMap.put(Integer.valueOf(localTL_contact.user_id), localTL_contact);
            if ((localObject2 != null) && (!TextUtils.isEmpty(localUser.phone)))
            {
              ((HashMap)localObject2).put(localUser.phone, localTL_contact);
              localHashMap1.put(localUser.phone.substring(Math.max(0, localUser.phone.length() - 7)), localTL_contact);
            }
            Object localObject3 = UserObject.getFirstName(localUser);
            Object localObject1 = localObject3;
            if (((String)localObject3).length() > 1) {
              localObject1 = ((String)localObject3).substring(0, 1);
            }
            if (((String)localObject1).length() == 0) {}
            for (localObject1 = "#";; localObject1 = ((String)localObject1).toUpperCase())
            {
              localObject3 = (String)ContactsController.this.sectionsToReplace.get(localObject1);
              if (localObject3 != null) {
                localObject1 = localObject3;
              }
              ArrayList localArrayList1 = (ArrayList)localHashMap2.get(localObject1);
              localObject3 = localArrayList1;
              if (localArrayList1 == null)
              {
                localObject3 = new ArrayList();
                localHashMap2.put(localObject1, localObject3);
                localArrayList2.add(localObject1);
              }
              ((ArrayList)localObject3).add(localTL_contact);
              if (!localUser.mutual_contact) {
                break;
              }
              localArrayList1 = (ArrayList)localHashMap3.get(localObject1);
              localObject3 = localArrayList1;
              if (localArrayList1 == null)
              {
                localObject3 = new ArrayList();
                localHashMap3.put(localObject1, localObject3);
                localArrayList3.add(localObject1);
              }
              ((ArrayList)localObject3).add(localTL_contact);
              break;
            }
            label777:
            Collections.sort(localArrayList2, new Comparator()
            {
              public int compare(String paramAnonymous3String1, String paramAnonymous3String2)
              {
                int i = paramAnonymous3String1.charAt(0);
                int j = paramAnonymous3String2.charAt(0);
                if (i == 35) {
                  return 1;
                }
                if (j == 35) {
                  return -1;
                }
                return paramAnonymous3String1.compareTo(paramAnonymous3String2);
              }
            });
            Collections.sort(localArrayList3, new Comparator()
            {
              public int compare(String paramAnonymous3String1, String paramAnonymous3String2)
              {
                int i = paramAnonymous3String1.charAt(0);
                int j = paramAnonymous3String2.charAt(0);
                if (i == 35) {
                  return 1;
                }
                if (j == 35) {
                  return -1;
                }
                return paramAnonymous3String1.compareTo(paramAnonymous3String2);
              }
            });
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                ContactsController.this.contacts = ContactsController.12.this.val$contactsArr;
                ContactsController.this.contactsDict = localConcurrentHashMap;
                ContactsController.this.usersSectionsDict = localHashMap2;
                ContactsController.this.usersMutualSectionsDict = localHashMap3;
                ContactsController.this.sortedUsersSectionsArray = localArrayList2;
                ContactsController.this.sortedUsersMutualSectionsArray = localArrayList3;
                if (ContactsController.12.this.val$from != 2) {}
                synchronized (ContactsController.this.loadContactsSync)
                {
                  ContactsController.access$702(ContactsController.this, false);
                  ContactsController.this.performWriteContactsToPhoneBook();
                  ContactsController.this.updateUnregisteredContacts(ContactsController.12.this.val$contactsArr);
                  NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
                  if ((ContactsController.12.this.val$from != 1) && (!ContactsController.12.1.this.val$isEmpty))
                  {
                    ContactsController.this.saveContactsLoadTime();
                    return;
                  }
                }
                ContactsController.this.reloadContactsStatusesMaybe();
              }
            });
            if ((!ContactsController.this.delayedContactsUpdate.isEmpty()) && (ContactsController.this.contactsLoaded) && (ContactsController.this.contactsBookLoaded))
            {
              ContactsController.this.applyContactsUpdates(ContactsController.this.delayedContactsUpdate, null, null, null);
              ContactsController.this.delayedContactsUpdate.clear();
            }
            if (localObject2 != null)
            {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  Utilities.globalQueue.postRunnable(new Runnable()
                  {
                    public void run()
                    {
                      ContactsController.this.contactsByPhone = ContactsController.12.1.5.this.val$contactsByPhonesDictFinal;
                      ContactsController.this.contactsByShortPhone = ContactsController.12.1.5.this.val$contactsByPhonesShortDictFinal;
                    }
                  });
                  if (ContactsController.this.contactsSyncInProgress) {
                    return;
                  }
                  ContactsController.access$902(ContactsController.this, true);
                  MessagesStorage.getInstance(ContactsController.this.currentAccount).getCachedPhoneBook(false);
                }
              });
              return;
            }
            ContactsController.this.contactsLoaded = true;
          }
        });
      }
    });
  }
  
  public void readContacts()
  {
    synchronized (this.loadContactsSync)
    {
      if (this.loadingContacts) {
        return;
      }
      this.loadingContacts = true;
      Utilities.stageQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          if ((!ContactsController.this.contacts.isEmpty()) || (ContactsController.this.contactsLoaded)) {
            synchronized (ContactsController.this.loadContactsSync)
            {
              ContactsController.access$702(ContactsController.this, false);
              return;
            }
          }
          ContactsController.this.loadContacts(true, 0);
        }
      });
      return;
    }
  }
  
  public void reloadContactsStatuses()
  {
    saveContactsLoadTime();
    MessagesController.getInstance(this.currentAccount).clearFullUsers();
    final SharedPreferences.Editor localEditor = MessagesController.getMainSettings(this.currentAccount).edit();
    localEditor.putBoolean("needGetStatuses", true).commit();
    TLRPC.TL_contacts_getStatuses localTL_contacts_getStatuses = new TLRPC.TL_contacts_getStatuses();
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_contacts_getStatuses, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error == null) {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              ContactsController.23.this.val$editor.remove("needGetStatuses").commit();
              Object localObject1 = (TLRPC.Vector)paramAnonymousTLObject;
              if (!((TLRPC.Vector)localObject1).objects.isEmpty())
              {
                ArrayList localArrayList = new ArrayList();
                localObject1 = ((TLRPC.Vector)localObject1).objects.iterator();
                while (((Iterator)localObject1).hasNext())
                {
                  Object localObject2 = ((Iterator)localObject1).next();
                  TLRPC.TL_user localTL_user = new TLRPC.TL_user();
                  localObject2 = (TLRPC.TL_contactStatus)localObject2;
                  if (localObject2 != null)
                  {
                    if ((((TLRPC.TL_contactStatus)localObject2).status instanceof TLRPC.TL_userStatusRecently)) {
                      ((TLRPC.TL_contactStatus)localObject2).status.expires = -100;
                    }
                    for (;;)
                    {
                      TLRPC.User localUser = MessagesController.getInstance(ContactsController.this.currentAccount).getUser(Integer.valueOf(((TLRPC.TL_contactStatus)localObject2).user_id));
                      if (localUser != null) {
                        localUser.status = ((TLRPC.TL_contactStatus)localObject2).status;
                      }
                      localTL_user.status = ((TLRPC.TL_contactStatus)localObject2).status;
                      localArrayList.add(localTL_user);
                      break;
                      if ((((TLRPC.TL_contactStatus)localObject2).status instanceof TLRPC.TL_userStatusLastWeek)) {
                        ((TLRPC.TL_contactStatus)localObject2).status.expires = -101;
                      } else if ((((TLRPC.TL_contactStatus)localObject2).status instanceof TLRPC.TL_userStatusLastMonth)) {
                        ((TLRPC.TL_contactStatus)localObject2).status.expires = -102;
                      }
                    }
                  }
                }
                MessagesStorage.getInstance(ContactsController.this.currentAccount).updateUsers(localArrayList, true, true, true);
              }
              NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(4) });
            }
          });
        }
      }
    });
  }
  
  public void resetImportedContacts()
  {
    TLRPC.TL_contacts_resetSaved localTL_contacts_resetSaved = new TLRPC.TL_contacts_resetSaved();
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_contacts_resetSaved, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
    });
  }
  
  public void setDeleteAccountTTL(int paramInt)
  {
    this.deleteAccountTTL = paramInt;
  }
  
  public void setPrivacyRules(ArrayList<TLRPC.PrivacyRule> paramArrayList, int paramInt)
  {
    if (paramInt == 2) {
      this.callPrivacyRules = paramArrayList;
    }
    for (;;)
    {
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
      reloadContactsStatuses();
      return;
      if (paramInt == 1) {
        this.groupPrivacyRules = paramArrayList;
      } else {
        this.privacyRules = paramArrayList;
      }
    }
  }
  
  public void syncPhoneBookByAlert(final HashMap<String, Contact> paramHashMap, final boolean paramBoolean1, final boolean paramBoolean2, final boolean paramBoolean3)
  {
    Utilities.globalQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("sync contacts by alert");
        }
        ContactsController.this.performSyncPhoneBook(paramHashMap, true, paramBoolean1, paramBoolean2, false, false, paramBoolean3);
      }
    });
  }
  
  public static class Contact
  {
    public int contact_id;
    public String first_name;
    public int imported;
    public boolean isGoodProvider;
    public String key;
    public String last_name;
    public boolean namesFilled;
    public ArrayList<Integer> phoneDeleted = new ArrayList(4);
    public ArrayList<String> phoneTypes = new ArrayList(4);
    public ArrayList<String> phones = new ArrayList(4);
    public String provider;
    public ArrayList<String> shortPhones = new ArrayList(4);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/ContactsController.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */