package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemSearchListener;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.LetterSectionCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.SectionsAdapter;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class CountrySelectActivity
  extends BaseFragment
{
  private CountrySelectActivityDelegate delegate;
  private EmptyTextProgressView emptyView;
  private RecyclerListView listView;
  private CountryAdapter listViewAdapter;
  private boolean needPhoneCode;
  private CountrySearchAdapter searchListViewAdapter;
  private boolean searchWas;
  private boolean searching;
  
  public CountrySelectActivity(boolean paramBoolean)
  {
    this.needPhoneCode = paramBoolean;
  }
  
  public View createView(Context paramContext)
  {
    int i = 1;
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("ChooseCountry", 2131493246));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          CountrySelectActivity.this.finishFragment();
        }
      }
    });
    this.actionBar.createMenu().addItem(0, 2131165356).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener()
    {
      public void onSearchCollapse()
      {
        CountrySelectActivity.this.searchListViewAdapter.search(null);
        CountrySelectActivity.access$002(CountrySelectActivity.this, false);
        CountrySelectActivity.access$202(CountrySelectActivity.this, false);
        CountrySelectActivity.this.listView.setAdapter(CountrySelectActivity.this.listViewAdapter);
        CountrySelectActivity.this.listView.setFastScrollVisible(true);
        CountrySelectActivity.this.emptyView.setText(LocaleController.getString("ChooseCountry", 2131493246));
      }
      
      public void onSearchExpand()
      {
        CountrySelectActivity.access$002(CountrySelectActivity.this, true);
      }
      
      public void onTextChanged(EditText paramAnonymousEditText)
      {
        paramAnonymousEditText = paramAnonymousEditText.getText().toString();
        CountrySelectActivity.this.searchListViewAdapter.search(paramAnonymousEditText);
        if (paramAnonymousEditText.length() != 0)
        {
          CountrySelectActivity.access$202(CountrySelectActivity.this, true);
          if (CountrySelectActivity.this.listView != null)
          {
            CountrySelectActivity.this.listView.setAdapter(CountrySelectActivity.this.searchListViewAdapter);
            CountrySelectActivity.this.listView.setFastScrollVisible(false);
          }
          if (CountrySelectActivity.this.emptyView == null) {}
        }
      }
    }).getSearchField().setHint(LocaleController.getString("Search", 2131494298));
    this.searching = false;
    this.searchWas = false;
    this.listViewAdapter = new CountryAdapter(paramContext);
    this.searchListViewAdapter = new CountrySearchAdapter(paramContext, this.listViewAdapter.getCountries());
    this.fragmentView = new FrameLayout(paramContext);
    FrameLayout localFrameLayout = (FrameLayout)this.fragmentView;
    this.emptyView = new EmptyTextProgressView(paramContext);
    this.emptyView.showTextView();
    this.emptyView.setShowAtCenter(true);
    this.emptyView.setText(LocaleController.getString("NoResult", 2131493906));
    localFrameLayout.addView(this.emptyView, LayoutHelper.createFrame(-1, -1.0F));
    this.listView = new RecyclerListView(paramContext);
    this.listView.setSectionsType(1);
    this.listView.setEmptyView(this.emptyView);
    this.listView.setVerticalScrollBarEnabled(false);
    this.listView.setFastScrollEnabled();
    this.listView.setLayoutManager(new LinearLayoutManager(paramContext, 1, false));
    this.listView.setAdapter(this.listViewAdapter);
    paramContext = this.listView;
    if (LocaleController.isRTL) {}
    for (;;)
    {
      paramContext.setVerticalScrollbarPosition(i);
      localFrameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1.0F));
      this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          if ((CountrySelectActivity.this.searching) && (CountrySelectActivity.this.searchWas))
          {
            paramAnonymousView = CountrySelectActivity.this.searchListViewAdapter.getItem(paramAnonymousInt);
            if (paramAnonymousInt >= 0) {
              break label88;
            }
          }
          label88:
          do
          {
            int i;
            int j;
            do
            {
              return;
              i = CountrySelectActivity.this.listViewAdapter.getSectionForPosition(paramAnonymousInt);
              j = CountrySelectActivity.this.listViewAdapter.getPositionInSectionForPosition(paramAnonymousInt);
            } while ((j < 0) || (i < 0));
            paramAnonymousView = CountrySelectActivity.this.listViewAdapter.getItem(i, j);
            break;
            CountrySelectActivity.this.finishFragment();
          } while ((paramAnonymousView == null) || (CountrySelectActivity.this.delegate == null));
          CountrySelectActivity.this.delegate.didSelectCountry(paramAnonymousView.name, paramAnonymousView.shortname);
        }
      });
      this.listView.setOnScrollListener(new RecyclerView.OnScrollListener()
      {
        public void onScrollStateChanged(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt)
        {
          if ((paramAnonymousInt == 1) && (CountrySelectActivity.this.searching) && (CountrySelectActivity.this.searchWas)) {
            AndroidUtilities.hideKeyboard(CountrySelectActivity.this.getParentActivity().getCurrentFocus());
          }
        }
      });
      return this.fragmentView;
      i = 2;
    }
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, "actionBarDefaultSearch");
    ThemeDescription localThemeDescription8 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, "actionBarDefaultSearchPlaceholder");
    ThemeDescription localThemeDescription9 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
    RecyclerListView localRecyclerListView = this.listView;
    Paint localPaint = Theme.dividerPaint;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, localThemeDescription9, new ThemeDescription(localRecyclerListView, 0, new Class[] { View.class }, localPaint, null, null, "divider"), new ThemeDescription(this.listView, ThemeDescription.FLAG_FASTSCROLL, null, null, null, null, "fastScrollActive"), new ThemeDescription(this.listView, ThemeDescription.FLAG_FASTSCROLL, null, null, null, null, "fastScrollInactive"), new ThemeDescription(this.listView, ThemeDescription.FLAG_FASTSCROLL, null, null, null, null, "fastScrollText"), new ThemeDescription(this.emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "emptyListPlaceholder"), new ThemeDescription(this.listView, 0, new Class[] { TextSettingsCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { TextSettingsCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteValueText"), new ThemeDescription(this.listView, ThemeDescription.FLAG_SECTIONS, new Class[] { LetterSectionCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText4") };
  }
  
  public boolean onFragmentCreate()
  {
    return super.onFragmentCreate();
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.listViewAdapter != null) {
      this.listViewAdapter.notifyDataSetChanged();
    }
  }
  
  public void setCountrySelectActivityDelegate(CountrySelectActivityDelegate paramCountrySelectActivityDelegate)
  {
    this.delegate = paramCountrySelectActivityDelegate;
  }
  
  public static class Country
  {
    public String code;
    public String name;
    public String shortname;
  }
  
  public class CountryAdapter
    extends RecyclerListView.SectionsAdapter
  {
    private HashMap<String, ArrayList<CountrySelectActivity.Country>> countries = new HashMap();
    private Context mContext;
    private ArrayList<String> sortedCountries = new ArrayList();
    
    public CountryAdapter(Context paramContext)
    {
      this.mContext = paramContext;
      try
      {
        InputStream localInputStream = ApplicationLoader.applicationContext.getResources().getAssets().open("countries.txt");
        BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localInputStream));
        for (;;)
        {
          paramContext = localBufferedReader.readLine();
          if (paramContext == null) {
            break;
          }
          paramContext = paramContext.split(";");
          CountrySelectActivity.Country localCountry = new CountrySelectActivity.Country();
          localCountry.name = paramContext[2];
          localCountry.code = paramContext[0];
          localCountry.shortname = paramContext[1];
          String str = localCountry.name.substring(0, 1).toUpperCase();
          ArrayList localArrayList = (ArrayList)this.countries.get(str);
          paramContext = localArrayList;
          if (localArrayList == null)
          {
            paramContext = new ArrayList();
            this.countries.put(str, paramContext);
            this.sortedCountries.add(str);
          }
          paramContext.add(localCountry);
        }
        return;
      }
      catch (Exception paramContext)
      {
        FileLog.e(paramContext);
        for (;;)
        {
          Collections.sort(this.sortedCountries, new Comparator()
          {
            public int compare(String paramAnonymousString1, String paramAnonymousString2)
            {
              return paramAnonymousString1.compareTo(paramAnonymousString2);
            }
          });
          paramContext = this.countries.values().iterator();
          while (paramContext.hasNext()) {
            Collections.sort((ArrayList)paramContext.next(), new Comparator()
            {
              public int compare(CountrySelectActivity.Country paramAnonymousCountry1, CountrySelectActivity.Country paramAnonymousCountry2)
              {
                return paramAnonymousCountry1.name.compareTo(paramAnonymousCountry2.name);
              }
            });
          }
          localBufferedReader.close();
          localInputStream.close();
        }
      }
    }
    
    public int getCountForSection(int paramInt)
    {
      int j = ((ArrayList)this.countries.get(this.sortedCountries.get(paramInt))).size();
      int i = j;
      if (paramInt != this.sortedCountries.size() - 1) {
        i = j + 1;
      }
      return i;
    }
    
    public HashMap<String, ArrayList<CountrySelectActivity.Country>> getCountries()
    {
      return this.countries;
    }
    
    public CountrySelectActivity.Country getItem(int paramInt1, int paramInt2)
    {
      if ((paramInt1 < 0) || (paramInt1 >= this.sortedCountries.size())) {}
      ArrayList localArrayList;
      do
      {
        return null;
        localArrayList = (ArrayList)this.countries.get(this.sortedCountries.get(paramInt1));
      } while ((paramInt2 < 0) || (paramInt2 >= localArrayList.size()));
      return (CountrySelectActivity.Country)localArrayList.get(paramInt2);
    }
    
    public int getItemViewType(int paramInt1, int paramInt2)
    {
      if (paramInt2 < ((ArrayList)this.countries.get(this.sortedCountries.get(paramInt1))).size()) {
        return 0;
      }
      return 1;
    }
    
    public String getLetter(int paramInt)
    {
      int i = getSectionForPosition(paramInt);
      paramInt = i;
      if (i == -1) {
        paramInt = this.sortedCountries.size() - 1;
      }
      return (String)this.sortedCountries.get(paramInt);
    }
    
    public int getPositionForScrollProgress(float paramFloat)
    {
      return (int)(getItemCount() * paramFloat);
    }
    
    public int getSectionCount()
    {
      return this.sortedCountries.size();
    }
    
    public View getSectionHeaderView(int paramInt, View paramView)
    {
      Object localObject = paramView;
      if (paramView == null)
      {
        localObject = new LetterSectionCell(this.mContext);
        ((LetterSectionCell)localObject).setCellHeight(AndroidUtilities.dp(48.0F));
      }
      ((LetterSectionCell)localObject).setLetter(((String)this.sortedCountries.get(paramInt)).toUpperCase());
      return (View)localObject;
    }
    
    public boolean isEnabled(int paramInt1, int paramInt2)
    {
      return paramInt2 < ((ArrayList)this.countries.get(this.sortedCountries.get(paramInt1))).size();
    }
    
    public void onBindViewHolder(int paramInt1, int paramInt2, RecyclerView.ViewHolder paramViewHolder)
    {
      CountrySelectActivity.Country localCountry;
      TextSettingsCell localTextSettingsCell;
      String str;
      if (paramViewHolder.getItemViewType() == 0)
      {
        localCountry = (CountrySelectActivity.Country)((ArrayList)this.countries.get(this.sortedCountries.get(paramInt1))).get(paramInt2);
        localTextSettingsCell = (TextSettingsCell)paramViewHolder.itemView;
        str = localCountry.name;
        if (!CountrySelectActivity.this.needPhoneCode) {
          break label94;
        }
      }
      label94:
      for (paramViewHolder = "+" + localCountry.code;; paramViewHolder = null)
      {
        localTextSettingsCell.setTextAndValue(str, paramViewHolder, false);
        return;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      float f2 = 72.0F;
      float f3 = 54.0F;
      switch (paramInt)
      {
      default: 
        paramViewGroup = new DividerCell(this.mContext);
        if (LocaleController.isRTL)
        {
          f1 = 24.0F;
          paramInt = AndroidUtilities.dp(f1);
          if (!LocaleController.isRTL) {
            break label153;
          }
        }
        break;
      }
      label106:
      label140:
      label153:
      for (float f1 = f2;; f1 = 24.0F)
      {
        paramViewGroup.setPadding(paramInt, 0, AndroidUtilities.dp(f1), 0);
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new TextSettingsCell(this.mContext);
        if (LocaleController.isRTL)
        {
          f1 = 16.0F;
          paramInt = AndroidUtilities.dp(f1);
          if (!LocaleController.isRTL) {
            break label140;
          }
        }
        for (f1 = f3;; f1 = 16.0F)
        {
          paramViewGroup.setPadding(paramInt, 0, AndroidUtilities.dp(f1), 0);
          break;
          f1 = 54.0F;
          break label106;
        }
        f1 = 72.0F;
        break;
      }
    }
  }
  
  public class CountrySearchAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private HashMap<String, ArrayList<CountrySelectActivity.Country>> countries;
    private Context mContext;
    private ArrayList<CountrySelectActivity.Country> searchResult;
    private Timer searchTimer;
    
    public CountrySearchAdapter(HashMap<String, ArrayList<CountrySelectActivity.Country>> paramHashMap)
    {
      this.mContext = paramHashMap;
      HashMap localHashMap;
      this.countries = localHashMap;
    }
    
    private void processSearch(final String paramString)
    {
      Utilities.searchQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          if (paramString.trim().toLowerCase().length() == 0)
          {
            CountrySelectActivity.CountrySearchAdapter.this.updateSearchResults(new ArrayList());
            return;
          }
          ArrayList localArrayList = new ArrayList();
          Object localObject = paramString.substring(0, 1);
          localObject = (ArrayList)CountrySelectActivity.CountrySearchAdapter.this.countries.get(((String)localObject).toUpperCase());
          if (localObject != null)
          {
            localObject = ((ArrayList)localObject).iterator();
            while (((Iterator)localObject).hasNext())
            {
              CountrySelectActivity.Country localCountry = (CountrySelectActivity.Country)((Iterator)localObject).next();
              if (localCountry.name.toLowerCase().startsWith(paramString)) {
                localArrayList.add(localCountry);
              }
            }
          }
          CountrySelectActivity.CountrySearchAdapter.this.updateSearchResults(localArrayList);
        }
      });
    }
    
    private void updateSearchResults(final ArrayList<CountrySelectActivity.Country> paramArrayList)
    {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          CountrySelectActivity.CountrySearchAdapter.access$1202(CountrySelectActivity.CountrySearchAdapter.this, paramArrayList);
          CountrySelectActivity.CountrySearchAdapter.this.notifyDataSetChanged();
        }
      });
    }
    
    public CountrySelectActivity.Country getItem(int paramInt)
    {
      if ((paramInt < 0) || (paramInt >= this.searchResult.size())) {
        return null;
      }
      return (CountrySelectActivity.Country)this.searchResult.get(paramInt);
    }
    
    public int getItemCount()
    {
      if (this.searchResult == null) {
        return 0;
      }
      return this.searchResult.size();
    }
    
    public int getItemViewType(int paramInt)
    {
      return 0;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return true;
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      CountrySelectActivity.Country localCountry = (CountrySelectActivity.Country)this.searchResult.get(paramInt);
      TextSettingsCell localTextSettingsCell = (TextSettingsCell)paramViewHolder.itemView;
      String str = localCountry.name;
      if (CountrySelectActivity.this.needPhoneCode)
      {
        paramViewHolder = "+" + localCountry.code;
        if (paramInt == this.searchResult.size() - 1) {
          break label93;
        }
      }
      label93:
      for (boolean bool = true;; bool = false)
      {
        localTextSettingsCell.setTextAndValue(str, paramViewHolder, bool);
        return;
        paramViewHolder = null;
        break;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      return new RecyclerListView.Holder(new TextSettingsCell(this.mContext));
    }
    
    public void search(final String paramString)
    {
      if (paramString == null)
      {
        this.searchResult = null;
        return;
      }
      try
      {
        if (this.searchTimer != null) {
          this.searchTimer.cancel();
        }
        this.searchTimer = new Timer();
        this.searchTimer.schedule(new TimerTask()
        {
          public void run()
          {
            try
            {
              CountrySelectActivity.CountrySearchAdapter.this.searchTimer.cancel();
              CountrySelectActivity.CountrySearchAdapter.access$802(CountrySelectActivity.CountrySearchAdapter.this, null);
              CountrySelectActivity.CountrySearchAdapter.this.processSearch(paramString);
              return;
            }
            catch (Exception localException)
            {
              for (;;)
              {
                FileLog.e(localException);
              }
            }
          }
        }, 100L, 300L);
        return;
      }
      catch (Exception localException)
      {
        for (;;)
        {
          FileLog.e(localException);
        }
      }
    }
  }
  
  public static abstract interface CountrySelectActivityDelegate
  {
    public abstract void didSelectCountry(String paramString1, String paramString2);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/CountrySelectActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */