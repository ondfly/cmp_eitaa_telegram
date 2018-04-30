package ir.eitaa.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.messenger.ApplicationLoader;
import ir.eitaa.messenger.LocaleController;
import ir.eitaa.messenger.MessagesController;
import ir.eitaa.tgnet.TLRPC.Chat;
import ir.eitaa.ui.ActionBar.ActionBar;
import ir.eitaa.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import ir.eitaa.ui.ActionBar.ActionBarMenu;
import ir.eitaa.ui.ActionBar.BaseFragment;
import ir.eitaa.ui.Components.LayoutHelper;

public class ChangeChatNameActivity
  extends BaseFragment
{
  private static final int done_button = 1;
  private int chat_id;
  private View doneButton;
  private EditText firstNameField;
  private View headerLabelView;
  
  public ChangeChatNameActivity(Bundle paramBundle)
  {
    super(paramBundle);
  }
  
  private void saveName()
  {
    MessagesController.getInstance().changeChatTitle(this.chat_id, this.firstNameField.getText().toString());
  }
  
  public View createView(Context paramContext)
  {
    int j = 3;
    this.actionBar.setBackButtonImage(2130837705);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("EditName", 2131165596));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          ChangeChatNameActivity.this.finishFragment();
        }
        while ((paramAnonymousInt != 1) || (ChangeChatNameActivity.this.firstNameField.getText().length() == 0)) {
          return;
        }
        ChangeChatNameActivity.this.saveName();
        ChangeChatNameActivity.this.finishFragment();
      }
    });
    this.doneButton = this.actionBar.createMenu().addItemWithWidth(1, 2130837725, AndroidUtilities.dp(56.0F));
    TLRPC.Chat localChat = MessagesController.getInstance().getChat(Integer.valueOf(this.chat_id));
    LinearLayout localLinearLayout = new LinearLayout(paramContext);
    this.fragmentView = localLinearLayout;
    this.fragmentView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
    ((LinearLayout)this.fragmentView).setOrientation(1);
    this.fragmentView.setOnTouchListener(new View.OnTouchListener()
    {
      public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
      {
        return true;
      }
    });
    this.firstNameField = new EditText(paramContext);
    this.firstNameField.setText(localChat.title);
    this.firstNameField.setTextSize(1, 18.0F);
    this.firstNameField.setHintTextColor(-6842473);
    this.firstNameField.setTextColor(-14606047);
    this.firstNameField.setMaxLines(3);
    this.firstNameField.setPadding(0, 0, 0, 0);
    paramContext = this.firstNameField;
    int i;
    if (LocaleController.isRTL)
    {
      i = 5;
      paramContext.setGravity(i);
      this.firstNameField.setInputType(180224);
      this.firstNameField.setImeOptions(6);
      paramContext = this.firstNameField;
      i = j;
      if (LocaleController.isRTL) {
        i = 5;
      }
      paramContext.setGravity(i);
      AndroidUtilities.clearCursorDrawable(this.firstNameField);
      this.firstNameField.setOnEditorActionListener(new TextView.OnEditorActionListener()
      {
        public boolean onEditorAction(TextView paramAnonymousTextView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
        {
          if ((paramAnonymousInt == 6) && (ChangeChatNameActivity.this.doneButton != null))
          {
            ChangeChatNameActivity.this.doneButton.performClick();
            return true;
          }
          return false;
        }
      });
      localLinearLayout.addView(this.firstNameField, LayoutHelper.createLinear(-1, 36, 24.0F, 24.0F, 24.0F, 0.0F));
      if (this.chat_id <= 0) {
        break label359;
      }
      this.firstNameField.setHint(LocaleController.getString("GroupName", 2131165721));
    }
    for (;;)
    {
      this.firstNameField.setSelection(this.firstNameField.length());
      return this.fragmentView;
      i = 3;
      break;
      label359:
      this.firstNameField.setHint(LocaleController.getString("EnterListName", 2131165625));
    }
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    this.chat_id = getArguments().getInt("chat_id", 0);
    return true;
  }
  
  public void onResume()
  {
    super.onResume();
    if (!ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getBoolean("view_animations", true))
    {
      this.firstNameField.requestFocus();
      AndroidUtilities.showKeyboard(this.firstNameField);
    }
  }
  
  public void onTransitionAnimationEnd(boolean paramBoolean1, boolean paramBoolean2)
  {
    if (paramBoolean1) {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          if (ChangeChatNameActivity.this.firstNameField != null)
          {
            ChangeChatNameActivity.this.firstNameField.requestFocus();
            AndroidUtilities.showKeyboard(ChangeChatNameActivity.this.firstNameField);
          }
        }
      }, 100L);
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/ChangeChatNameActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */