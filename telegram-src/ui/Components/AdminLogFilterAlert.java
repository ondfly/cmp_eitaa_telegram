package org.telegram.ui.Components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.TLRPC.ChannelParticipant;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEventsFilter;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.BottomSheet.BottomSheetCell;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.CheckBoxUserCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.StickerPreviewViewer;

public class AdminLogFilterAlert
  extends BottomSheet
{
  private ListAdapter adapter;
  private int adminsRow;
  private int allAdminsRow;
  private ArrayList<TLRPC.ChannelParticipant> currentAdmins;
  private TLRPC.TL_channelAdminLogEventsFilter currentFilter;
  private AdminLogFilterAlertDelegate delegate;
  private int deleteRow;
  private int editRow;
  private boolean ignoreLayout;
  private int infoRow;
  private boolean isMegagroup;
  private int leavingRow;
  private RecyclerListView listView;
  private int membersRow;
  private FrameLayout pickerBottomLayout;
  private int pinnedRow;
  private int reqId;
  private int restrictionsRow;
  private BottomSheet.BottomSheetCell saveButton;
  private int scrollOffsetY;
  private SparseArray<TLRPC.User> selectedAdmins;
  private Drawable shadowDrawable;
  private Pattern urlPattern;
  
  public AdminLogFilterAlert(Context paramContext, TLRPC.TL_channelAdminLogEventsFilter paramTL_channelAdminLogEventsFilter, SparseArray<TLRPC.User> paramSparseArray, boolean paramBoolean)
  {
    super(paramContext, false);
    if (paramTL_channelAdminLogEventsFilter != null)
    {
      this.currentFilter = new TLRPC.TL_channelAdminLogEventsFilter();
      this.currentFilter.join = paramTL_channelAdminLogEventsFilter.join;
      this.currentFilter.leave = paramTL_channelAdminLogEventsFilter.leave;
      this.currentFilter.invite = paramTL_channelAdminLogEventsFilter.invite;
      this.currentFilter.ban = paramTL_channelAdminLogEventsFilter.ban;
      this.currentFilter.unban = paramTL_channelAdminLogEventsFilter.unban;
      this.currentFilter.kick = paramTL_channelAdminLogEventsFilter.kick;
      this.currentFilter.unkick = paramTL_channelAdminLogEventsFilter.unkick;
      this.currentFilter.promote = paramTL_channelAdminLogEventsFilter.promote;
      this.currentFilter.demote = paramTL_channelAdminLogEventsFilter.demote;
      this.currentFilter.info = paramTL_channelAdminLogEventsFilter.info;
      this.currentFilter.settings = paramTL_channelAdminLogEventsFilter.settings;
      this.currentFilter.pinned = paramTL_channelAdminLogEventsFilter.pinned;
      this.currentFilter.edit = paramTL_channelAdminLogEventsFilter.edit;
      this.currentFilter.delete = paramTL_channelAdminLogEventsFilter.delete;
    }
    if (paramSparseArray != null) {
      this.selectedAdmins = paramSparseArray.clone();
    }
    this.isMegagroup = paramBoolean;
    int i = 1;
    if (this.isMegagroup)
    {
      this.restrictionsRow = 1;
      i = 1 + 1;
      int j = i + 1;
      this.adminsRow = i;
      i = j + 1;
      this.membersRow = j;
      j = i + 1;
      this.infoRow = i;
      int k = j + 1;
      this.deleteRow = j;
      i = k + 1;
      this.editRow = k;
      if (!this.isMegagroup) {
        break label680;
      }
      j = i + 1;
      this.pinnedRow = i;
      i = j;
    }
    for (;;)
    {
      this.leavingRow = i;
      this.allAdminsRow = (i + 2);
      this.shadowDrawable = paramContext.getResources().getDrawable(2131165640).mutate();
      this.shadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("dialogBackground"), PorterDuff.Mode.MULTIPLY));
      this.containerView = new FrameLayout(paramContext)
      {
        protected void onDraw(Canvas paramAnonymousCanvas)
        {
          AdminLogFilterAlert.this.shadowDrawable.setBounds(0, AdminLogFilterAlert.this.scrollOffsetY - AdminLogFilterAlert.backgroundPaddingTop, getMeasuredWidth(), getMeasuredHeight());
          AdminLogFilterAlert.this.shadowDrawable.draw(paramAnonymousCanvas);
        }
        
        public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
        {
          if ((paramAnonymousMotionEvent.getAction() == 0) && (AdminLogFilterAlert.this.scrollOffsetY != 0) && (paramAnonymousMotionEvent.getY() < AdminLogFilterAlert.this.scrollOffsetY))
          {
            AdminLogFilterAlert.this.dismiss();
            return true;
          }
          return super.onInterceptTouchEvent(paramAnonymousMotionEvent);
        }
        
        protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
        {
          super.onLayout(paramAnonymousBoolean, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3, paramAnonymousInt4);
          AdminLogFilterAlert.this.updateLayout();
        }
        
        protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
        {
          paramAnonymousInt2 = View.MeasureSpec.getSize(paramAnonymousInt2);
          int j = paramAnonymousInt2;
          if (Build.VERSION.SDK_INT >= 21) {
            j = paramAnonymousInt2 - AndroidUtilities.statusBarHeight;
          }
          getMeasuredWidth();
          int i = AndroidUtilities.dp(48.0F);
          int k;
          if (AdminLogFilterAlert.this.isMegagroup)
          {
            paramAnonymousInt2 = 9;
            paramAnonymousInt2 = paramAnonymousInt2 * AndroidUtilities.dp(48.0F) + i + AdminLogFilterAlert.backgroundPaddingTop;
            k = paramAnonymousInt2;
            if (AdminLogFilterAlert.this.currentAdmins != null) {
              k = paramAnonymousInt2 + ((AdminLogFilterAlert.this.currentAdmins.size() + 1) * AndroidUtilities.dp(48.0F) + AndroidUtilities.dp(20.0F));
            }
            if (k >= j / 5 * 3.2F) {
              break label222;
            }
          }
          label222:
          for (i = 0;; i = j / 5 * 2)
          {
            paramAnonymousInt2 = i;
            if (i != 0)
            {
              paramAnonymousInt2 = i;
              if (k < j) {
                paramAnonymousInt2 = i - (j - k);
              }
            }
            i = paramAnonymousInt2;
            if (paramAnonymousInt2 == 0) {
              i = AdminLogFilterAlert.backgroundPaddingTop;
            }
            if (AdminLogFilterAlert.this.listView.getPaddingTop() != i)
            {
              AdminLogFilterAlert.access$602(AdminLogFilterAlert.this, true);
              AdminLogFilterAlert.this.listView.setPadding(0, i, 0, 0);
              AdminLogFilterAlert.access$602(AdminLogFilterAlert.this, false);
            }
            super.onMeasure(paramAnonymousInt1, View.MeasureSpec.makeMeasureSpec(Math.min(k, j), 1073741824));
            return;
            paramAnonymousInt2 = 7;
            break;
          }
        }
        
        public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
        {
          return (!AdminLogFilterAlert.this.isDismissed()) && (super.onTouchEvent(paramAnonymousMotionEvent));
        }
        
        public void requestLayout()
        {
          if (AdminLogFilterAlert.this.ignoreLayout) {
            return;
          }
          super.requestLayout();
        }
      };
      this.containerView.setWillNotDraw(false);
      this.containerView.setPadding(backgroundPaddingLeft, 0, backgroundPaddingLeft, 0);
      this.listView = new RecyclerListView(paramContext)
      {
        public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
        {
          boolean bool1 = false;
          boolean bool2 = StickerPreviewViewer.getInstance().onInterceptTouchEvent(paramAnonymousMotionEvent, AdminLogFilterAlert.this.listView, 0, null);
          if ((super.onInterceptTouchEvent(paramAnonymousMotionEvent)) || (bool2)) {
            bool1 = true;
          }
          return bool1;
        }
        
        public void requestLayout()
        {
          if (AdminLogFilterAlert.this.ignoreLayout) {
            return;
          }
          super.requestLayout();
        }
      };
      this.listView.setLayoutManager(new LinearLayoutManager(getContext(), 1, false));
      paramTL_channelAdminLogEventsFilter = this.listView;
      paramSparseArray = new ListAdapter(paramContext);
      this.adapter = paramSparseArray;
      paramTL_channelAdminLogEventsFilter.setAdapter(paramSparseArray);
      this.listView.setVerticalScrollBarEnabled(false);
      this.listView.setClipToPadding(false);
      this.listView.setEnabled(true);
      this.listView.setGlowColor(Theme.getColor("dialogScrollGlow"));
      this.listView.setOnScrollListener(new RecyclerView.OnScrollListener()
      {
        public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2)
        {
          AdminLogFilterAlert.this.updateLayout();
        }
      });
      this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          if ((paramAnonymousView instanceof CheckBoxCell))
          {
            paramAnonymousView = (CheckBoxCell)paramAnonymousView;
            bool2 = paramAnonymousView.isChecked();
            if (!bool2)
            {
              bool1 = true;
              paramAnonymousView.setChecked(bool1, true);
              if (paramAnonymousInt != 0) {
                break label389;
              }
              if (!bool2) {
                break label371;
              }
              AdminLogFilterAlert.access$1002(AdminLogFilterAlert.this, new TLRPC.TL_channelAdminLogEventsFilter());
              paramAnonymousView = AdminLogFilterAlert.this.currentFilter;
              localObject = AdminLogFilterAlert.this.currentFilter;
              localTL_channelAdminLogEventsFilter1 = AdminLogFilterAlert.this.currentFilter;
              localTL_channelAdminLogEventsFilter2 = AdminLogFilterAlert.this.currentFilter;
              localTL_channelAdminLogEventsFilter3 = AdminLogFilterAlert.this.currentFilter;
              localTL_channelAdminLogEventsFilter4 = AdminLogFilterAlert.this.currentFilter;
              localTL_channelAdminLogEventsFilter5 = AdminLogFilterAlert.this.currentFilter;
              localTL_channelAdminLogEventsFilter6 = AdminLogFilterAlert.this.currentFilter;
              localTL_channelAdminLogEventsFilter7 = AdminLogFilterAlert.this.currentFilter;
              localTL_channelAdminLogEventsFilter8 = AdminLogFilterAlert.this.currentFilter;
              localTL_channelAdminLogEventsFilter9 = AdminLogFilterAlert.this.currentFilter;
              localTL_channelAdminLogEventsFilter10 = AdminLogFilterAlert.this.currentFilter;
              localTL_channelAdminLogEventsFilter11 = AdminLogFilterAlert.this.currentFilter;
              AdminLogFilterAlert.this.currentFilter.delete = false;
              localTL_channelAdminLogEventsFilter11.edit = false;
              localTL_channelAdminLogEventsFilter10.pinned = false;
              localTL_channelAdminLogEventsFilter9.settings = false;
              localTL_channelAdminLogEventsFilter8.info = false;
              localTL_channelAdminLogEventsFilter7.demote = false;
              localTL_channelAdminLogEventsFilter6.promote = false;
              localTL_channelAdminLogEventsFilter5.unkick = false;
              localTL_channelAdminLogEventsFilter4.kick = false;
              localTL_channelAdminLogEventsFilter3.unban = false;
              localTL_channelAdminLogEventsFilter2.ban = false;
              localTL_channelAdminLogEventsFilter1.invite = false;
              ((TLRPC.TL_channelAdminLogEventsFilter)localObject).leave = false;
              paramAnonymousView.join = false;
              label261:
              i = AdminLogFilterAlert.this.listView.getChildCount();
              paramAnonymousInt = 0;
              label274:
              if (paramAnonymousInt >= i) {
                break label869;
              }
              paramAnonymousView = AdminLogFilterAlert.this.listView.getChildAt(paramAnonymousInt);
              localObject = AdminLogFilterAlert.this.listView.findContainingViewHolder(paramAnonymousView);
              j = ((RecyclerView.ViewHolder)localObject).getAdapterPosition();
              if ((((RecyclerView.ViewHolder)localObject).getItemViewType() == 0) && (j > 0) && (j < AdminLogFilterAlert.this.allAdminsRow - 1))
              {
                paramAnonymousView = (CheckBoxCell)paramAnonymousView;
                if (bool2) {
                  break label383;
                }
              }
            }
            label371:
            label383:
            for (bool1 = true;; bool1 = false)
            {
              paramAnonymousView.setChecked(bool1, true);
              paramAnonymousInt += 1;
              break label274;
              bool1 = false;
              break;
              AdminLogFilterAlert.access$1002(AdminLogFilterAlert.this, null);
              break label261;
            }
            label389:
            if (paramAnonymousInt == AdminLogFilterAlert.this.allAdminsRow)
            {
              if (bool2)
              {
                AdminLogFilterAlert.access$1202(AdminLogFilterAlert.this, new SparseArray());
                i = AdminLogFilterAlert.this.listView.getChildCount();
                paramAnonymousInt = 0;
                label433:
                if (paramAnonymousInt >= i) {
                  break label869;
                }
                paramAnonymousView = AdminLogFilterAlert.this.listView.getChildAt(paramAnonymousInt);
                localObject = AdminLogFilterAlert.this.listView.findContainingViewHolder(paramAnonymousView);
                ((RecyclerView.ViewHolder)localObject).getAdapterPosition();
                if (((RecyclerView.ViewHolder)localObject).getItemViewType() == 2)
                {
                  paramAnonymousView = (CheckBoxUserCell)paramAnonymousView;
                  if (bool2) {
                    break label517;
                  }
                }
              }
              label517:
              for (bool1 = true;; bool1 = false)
              {
                paramAnonymousView.setChecked(bool1, true);
                paramAnonymousInt += 1;
                break label433;
                AdminLogFilterAlert.access$1202(AdminLogFilterAlert.this, null);
                break;
              }
            }
            else
            {
              if (AdminLogFilterAlert.this.currentFilter == null)
              {
                AdminLogFilterAlert.access$1002(AdminLogFilterAlert.this, new TLRPC.TL_channelAdminLogEventsFilter());
                paramAnonymousView = AdminLogFilterAlert.this.currentFilter;
                localObject = AdminLogFilterAlert.this.currentFilter;
                localTL_channelAdminLogEventsFilter1 = AdminLogFilterAlert.this.currentFilter;
                localTL_channelAdminLogEventsFilter2 = AdminLogFilterAlert.this.currentFilter;
                localTL_channelAdminLogEventsFilter3 = AdminLogFilterAlert.this.currentFilter;
                localTL_channelAdminLogEventsFilter4 = AdminLogFilterAlert.this.currentFilter;
                localTL_channelAdminLogEventsFilter5 = AdminLogFilterAlert.this.currentFilter;
                localTL_channelAdminLogEventsFilter6 = AdminLogFilterAlert.this.currentFilter;
                localTL_channelAdminLogEventsFilter7 = AdminLogFilterAlert.this.currentFilter;
                localTL_channelAdminLogEventsFilter8 = AdminLogFilterAlert.this.currentFilter;
                localTL_channelAdminLogEventsFilter9 = AdminLogFilterAlert.this.currentFilter;
                localTL_channelAdminLogEventsFilter10 = AdminLogFilterAlert.this.currentFilter;
                localTL_channelAdminLogEventsFilter11 = AdminLogFilterAlert.this.currentFilter;
                AdminLogFilterAlert.this.currentFilter.delete = true;
                localTL_channelAdminLogEventsFilter11.edit = true;
                localTL_channelAdminLogEventsFilter10.pinned = true;
                localTL_channelAdminLogEventsFilter9.settings = true;
                localTL_channelAdminLogEventsFilter8.info = true;
                localTL_channelAdminLogEventsFilter7.demote = true;
                localTL_channelAdminLogEventsFilter6.promote = true;
                localTL_channelAdminLogEventsFilter5.unkick = true;
                localTL_channelAdminLogEventsFilter4.kick = true;
                localTL_channelAdminLogEventsFilter3.unban = true;
                localTL_channelAdminLogEventsFilter2.ban = true;
                localTL_channelAdminLogEventsFilter1.invite = true;
                ((TLRPC.TL_channelAdminLogEventsFilter)localObject).leave = true;
                paramAnonymousView.join = true;
                paramAnonymousView = AdminLogFilterAlert.this.listView.findViewHolderForAdapterPosition(0);
                if (paramAnonymousView != null) {
                  ((CheckBoxCell)paramAnonymousView.itemView).setChecked(false, true);
                }
              }
              if (paramAnonymousInt != AdminLogFilterAlert.this.restrictionsRow) {
                break label1104;
              }
              paramAnonymousView = AdminLogFilterAlert.this.currentFilter;
              localObject = AdminLogFilterAlert.this.currentFilter;
              localTL_channelAdminLogEventsFilter1 = AdminLogFilterAlert.this.currentFilter;
              localTL_channelAdminLogEventsFilter2 = AdminLogFilterAlert.this.currentFilter;
              if (AdminLogFilterAlert.this.currentFilter.kick) {
                break label1098;
              }
              bool1 = true;
              localTL_channelAdminLogEventsFilter2.unban = bool1;
              localTL_channelAdminLogEventsFilter1.unkick = bool1;
              ((TLRPC.TL_channelAdminLogEventsFilter)localObject).ban = bool1;
              paramAnonymousView.kick = bool1;
            }
            label869:
            if ((AdminLogFilterAlert.this.currentFilter != null) && (!AdminLogFilterAlert.this.currentFilter.join) && (!AdminLogFilterAlert.this.currentFilter.leave) && (!AdminLogFilterAlert.this.currentFilter.leave) && (!AdminLogFilterAlert.this.currentFilter.invite) && (!AdminLogFilterAlert.this.currentFilter.ban) && (!AdminLogFilterAlert.this.currentFilter.unban) && (!AdminLogFilterAlert.this.currentFilter.kick) && (!AdminLogFilterAlert.this.currentFilter.unkick) && (!AdminLogFilterAlert.this.currentFilter.promote) && (!AdminLogFilterAlert.this.currentFilter.demote) && (!AdminLogFilterAlert.this.currentFilter.info) && (!AdminLogFilterAlert.this.currentFilter.settings) && (!AdminLogFilterAlert.this.currentFilter.pinned) && (!AdminLogFilterAlert.this.currentFilter.edit) && (!AdminLogFilterAlert.this.currentFilter.delete))
            {
              AdminLogFilterAlert.this.saveButton.setEnabled(false);
              AdminLogFilterAlert.this.saveButton.setAlpha(0.5F);
            }
          }
          label1098:
          label1104:
          while (!(paramAnonymousView instanceof CheckBoxUserCell))
          {
            do
            {
              for (;;)
              {
                boolean bool2;
                TLRPC.TL_channelAdminLogEventsFilter localTL_channelAdminLogEventsFilter1;
                TLRPC.TL_channelAdminLogEventsFilter localTL_channelAdminLogEventsFilter2;
                TLRPC.TL_channelAdminLogEventsFilter localTL_channelAdminLogEventsFilter3;
                TLRPC.TL_channelAdminLogEventsFilter localTL_channelAdminLogEventsFilter4;
                TLRPC.TL_channelAdminLogEventsFilter localTL_channelAdminLogEventsFilter5;
                TLRPC.TL_channelAdminLogEventsFilter localTL_channelAdminLogEventsFilter6;
                TLRPC.TL_channelAdminLogEventsFilter localTL_channelAdminLogEventsFilter7;
                TLRPC.TL_channelAdminLogEventsFilter localTL_channelAdminLogEventsFilter8;
                TLRPC.TL_channelAdminLogEventsFilter localTL_channelAdminLogEventsFilter9;
                TLRPC.TL_channelAdminLogEventsFilter localTL_channelAdminLogEventsFilter10;
                TLRPC.TL_channelAdminLogEventsFilter localTL_channelAdminLogEventsFilter11;
                int i;
                int j;
                return;
                bool1 = false;
              }
              if (paramAnonymousInt == AdminLogFilterAlert.this.adminsRow)
              {
                paramAnonymousView = AdminLogFilterAlert.this.currentFilter;
                localObject = AdminLogFilterAlert.this.currentFilter;
                if (!AdminLogFilterAlert.this.currentFilter.demote) {}
                for (bool1 = true;; bool1 = false)
                {
                  ((TLRPC.TL_channelAdminLogEventsFilter)localObject).demote = bool1;
                  paramAnonymousView.promote = bool1;
                  break;
                }
              }
              if (paramAnonymousInt == AdminLogFilterAlert.this.membersRow)
              {
                paramAnonymousView = AdminLogFilterAlert.this.currentFilter;
                localObject = AdminLogFilterAlert.this.currentFilter;
                if (!AdminLogFilterAlert.this.currentFilter.join) {}
                for (bool1 = true;; bool1 = false)
                {
                  ((TLRPC.TL_channelAdminLogEventsFilter)localObject).join = bool1;
                  paramAnonymousView.invite = bool1;
                  break;
                }
              }
              if (paramAnonymousInt == AdminLogFilterAlert.this.infoRow)
              {
                paramAnonymousView = AdminLogFilterAlert.this.currentFilter;
                localObject = AdminLogFilterAlert.this.currentFilter;
                if (!AdminLogFilterAlert.this.currentFilter.info) {}
                for (bool1 = true;; bool1 = false)
                {
                  ((TLRPC.TL_channelAdminLogEventsFilter)localObject).settings = bool1;
                  paramAnonymousView.info = bool1;
                  break;
                }
              }
              if (paramAnonymousInt == AdminLogFilterAlert.this.deleteRow)
              {
                paramAnonymousView = AdminLogFilterAlert.this.currentFilter;
                if (!AdminLogFilterAlert.this.currentFilter.delete) {}
                for (bool1 = true;; bool1 = false)
                {
                  paramAnonymousView.delete = bool1;
                  break;
                }
              }
              if (paramAnonymousInt == AdminLogFilterAlert.this.editRow)
              {
                paramAnonymousView = AdminLogFilterAlert.this.currentFilter;
                if (!AdminLogFilterAlert.this.currentFilter.edit) {}
                for (bool1 = true;; bool1 = false)
                {
                  paramAnonymousView.edit = bool1;
                  break;
                }
              }
              if (paramAnonymousInt == AdminLogFilterAlert.this.pinnedRow)
              {
                paramAnonymousView = AdminLogFilterAlert.this.currentFilter;
                if (!AdminLogFilterAlert.this.currentFilter.pinned) {}
                for (bool1 = true;; bool1 = false)
                {
                  paramAnonymousView.pinned = bool1;
                  break;
                }
              }
            } while (paramAnonymousInt != AdminLogFilterAlert.this.leavingRow);
            paramAnonymousView = AdminLogFilterAlert.this.currentFilter;
            if (!AdminLogFilterAlert.this.currentFilter.leave) {}
            for (bool1 = true;; bool1 = false)
            {
              paramAnonymousView.leave = bool1;
              break;
            }
            AdminLogFilterAlert.this.saveButton.setEnabled(true);
            AdminLogFilterAlert.this.saveButton.setAlpha(1.0F);
            return;
          }
          paramAnonymousView = (CheckBoxUserCell)paramAnonymousView;
          if (AdminLogFilterAlert.this.selectedAdmins == null)
          {
            AdminLogFilterAlert.access$1202(AdminLogFilterAlert.this, new SparseArray());
            localObject = AdminLogFilterAlert.this.listView.findViewHolderForAdapterPosition(AdminLogFilterAlert.this.allAdminsRow);
            if (localObject != null) {
              ((CheckBoxCell)((RecyclerView.ViewHolder)localObject).itemView).setChecked(false, true);
            }
            paramAnonymousInt = 0;
            while (paramAnonymousInt < AdminLogFilterAlert.this.currentAdmins.size())
            {
              localObject = MessagesController.getInstance(AdminLogFilterAlert.this.currentAccount).getUser(Integer.valueOf(((TLRPC.ChannelParticipant)AdminLogFilterAlert.this.currentAdmins.get(paramAnonymousInt)).user_id));
              AdminLogFilterAlert.this.selectedAdmins.put(((TLRPC.User)localObject).id, localObject);
              paramAnonymousInt += 1;
            }
          }
          boolean bool1 = paramAnonymousView.isChecked();
          Object localObject = paramAnonymousView.getCurrentUser();
          if (bool1)
          {
            AdminLogFilterAlert.this.selectedAdmins.remove(((TLRPC.User)localObject).id);
            if (bool1) {
              break label1742;
            }
          }
          label1742:
          for (bool1 = true;; bool1 = false)
          {
            paramAnonymousView.setChecked(bool1, true);
            return;
            AdminLogFilterAlert.this.selectedAdmins.put(((TLRPC.User)localObject).id, localObject);
            break;
          }
        }
      });
      this.containerView.addView(this.listView, LayoutHelper.createFrame(-1, -1.0F, 51, 0.0F, 0.0F, 0.0F, 48.0F));
      paramTL_channelAdminLogEventsFilter = new View(paramContext);
      paramTL_channelAdminLogEventsFilter.setBackgroundResource(2131165343);
      this.containerView.addView(paramTL_channelAdminLogEventsFilter, LayoutHelper.createFrame(-1, 3.0F, 83, 0.0F, 0.0F, 0.0F, 48.0F));
      this.saveButton = new BottomSheet.BottomSheetCell(paramContext, 1);
      this.saveButton.setBackgroundDrawable(Theme.getSelectorDrawable(false));
      this.saveButton.setTextAndIcon(LocaleController.getString("Save", 2131494286).toUpperCase(), 0);
      this.saveButton.setTextColor(Theme.getColor("dialogTextBlue2"));
      this.saveButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          AdminLogFilterAlert.this.delegate.didSelectRights(AdminLogFilterAlert.this.currentFilter, AdminLogFilterAlert.this.selectedAdmins);
          AdminLogFilterAlert.this.dismiss();
        }
      });
      this.containerView.addView(this.saveButton, LayoutHelper.createFrame(-1, 48, 83));
      this.adapter.notifyDataSetChanged();
      return;
      this.restrictionsRow = -1;
      break;
      label680:
      this.pinnedRow = -1;
    }
  }
  
  @SuppressLint({"NewApi"})
  private void updateLayout()
  {
    int j = 0;
    int i;
    if (this.listView.getChildCount() <= 0)
    {
      localObject = this.listView;
      i = this.listView.getPaddingTop();
      this.scrollOffsetY = i;
      ((RecyclerListView)localObject).setTopGlowOffset(i);
      this.containerView.invalidate();
    }
    do
    {
      return;
      localObject = this.listView.getChildAt(0);
      RecyclerListView.Holder localHolder = (RecyclerListView.Holder)this.listView.findContainingViewHolder((View)localObject);
      int k = ((View)localObject).getTop() - AndroidUtilities.dp(8.0F);
      i = j;
      if (k > 0)
      {
        i = j;
        if (localHolder != null)
        {
          i = j;
          if (localHolder.getAdapterPosition() == 0) {
            i = k;
          }
        }
      }
    } while (this.scrollOffsetY == i);
    Object localObject = this.listView;
    this.scrollOffsetY = i;
    ((RecyclerListView)localObject).setTopGlowOffset(i);
    this.containerView.invalidate();
  }
  
  protected boolean canDismissWithSwipe()
  {
    return false;
  }
  
  public void setAdminLogFilterAlertDelegate(AdminLogFilterAlertDelegate paramAdminLogFilterAlertDelegate)
  {
    this.delegate = paramAdminLogFilterAlertDelegate;
  }
  
  public void setCurrentAdmins(ArrayList<TLRPC.ChannelParticipant> paramArrayList)
  {
    this.currentAdmins = paramArrayList;
    if (this.adapter != null) {
      this.adapter.notifyDataSetChanged();
    }
  }
  
  public static abstract interface AdminLogFilterAlertDelegate
  {
    public abstract void didSelectRights(TLRPC.TL_channelAdminLogEventsFilter paramTL_channelAdminLogEventsFilter, SparseArray<TLRPC.User> paramSparseArray);
  }
  
  private class ListAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private Context context;
    
    public ListAdapter(Context paramContext)
    {
      this.context = paramContext;
    }
    
    public int getItemCount()
    {
      int i;
      if (AdminLogFilterAlert.this.isMegagroup)
      {
        i = 9;
        if (AdminLogFilterAlert.this.currentAdmins == null) {
          break label46;
        }
      }
      label46:
      for (int j = AdminLogFilterAlert.this.currentAdmins.size() + 2;; j = 0)
      {
        return i + j;
        i = 7;
        break;
      }
    }
    
    public int getItemViewType(int paramInt)
    {
      if ((paramInt < AdminLogFilterAlert.this.allAdminsRow - 1) || (paramInt == AdminLogFilterAlert.this.allAdminsRow)) {
        return 0;
      }
      if (paramInt == AdminLogFilterAlert.this.allAdminsRow - 1) {
        return 1;
      }
      return 2;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return paramViewHolder.getItemViewType() != 1;
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      boolean bool5 = false;
      boolean bool6 = false;
      boolean bool1 = false;
      boolean bool7 = false;
      boolean bool8 = false;
      boolean bool9 = false;
      boolean bool10 = false;
      boolean bool11 = false;
      boolean bool3 = false;
      boolean bool2 = true;
      boolean bool4 = true;
      switch (paramViewHolder.getItemViewType())
      {
      case 1: 
      default: 
      case 0: 
        label756:
        do
        {
          return;
          paramViewHolder = (CheckBoxCell)paramViewHolder.itemView;
          if (paramInt == 0)
          {
            localObject = LocaleController.getString("EventLogFilterAll", 2131493479);
            if (AdminLogFilterAlert.this.currentFilter == null) {}
            for (bool1 = true;; bool1 = false)
            {
              paramViewHolder.setText((String)localObject, "", bool1, true);
              return;
            }
          }
          if (paramInt == AdminLogFilterAlert.this.restrictionsRow)
          {
            localObject = LocaleController.getString("EventLogFilterNewRestrictions", 2131493487);
            if (AdminLogFilterAlert.this.currentFilter != null)
            {
              bool1 = bool3;
              if (AdminLogFilterAlert.this.currentFilter.kick)
              {
                bool1 = bool3;
                if (AdminLogFilterAlert.this.currentFilter.ban)
                {
                  bool1 = bool3;
                  if (AdminLogFilterAlert.this.currentFilter.unkick)
                  {
                    bool1 = bool3;
                    if (!AdminLogFilterAlert.this.currentFilter.unban) {}
                  }
                }
              }
            }
            else
            {
              bool1 = true;
            }
            paramViewHolder.setText((String)localObject, "", bool1, true);
            return;
          }
          if (paramInt == AdminLogFilterAlert.this.adminsRow)
          {
            localObject = LocaleController.getString("EventLogFilterNewAdmins", 2131493485);
            if (AdminLogFilterAlert.this.currentFilter != null)
            {
              bool1 = bool5;
              if (AdminLogFilterAlert.this.currentFilter.promote)
              {
                bool1 = bool5;
                if (!AdminLogFilterAlert.this.currentFilter.demote) {}
              }
            }
            else
            {
              bool1 = true;
            }
            paramViewHolder.setText((String)localObject, "", bool1, true);
            return;
          }
          if (paramInt == AdminLogFilterAlert.this.membersRow)
          {
            localObject = LocaleController.getString("EventLogFilterNewMembers", 2131493486);
            if (AdminLogFilterAlert.this.currentFilter != null)
            {
              bool1 = bool6;
              if (AdminLogFilterAlert.this.currentFilter.invite)
              {
                bool1 = bool6;
                if (!AdminLogFilterAlert.this.currentFilter.join) {}
              }
            }
            else
            {
              bool1 = true;
            }
            paramViewHolder.setText((String)localObject, "", bool1, true);
            return;
          }
          if (paramInt == AdminLogFilterAlert.this.infoRow)
          {
            if (AdminLogFilterAlert.this.isMegagroup)
            {
              localObject = LocaleController.getString("EventLogFilterGroupInfo", 2131493483);
              if ((AdminLogFilterAlert.this.currentFilter == null) || (AdminLogFilterAlert.this.currentFilter.info)) {
                bool1 = true;
              }
              paramViewHolder.setText((String)localObject, "", bool1, true);
              return;
            }
            localObject = LocaleController.getString("EventLogFilterChannelInfo", 2131493480);
            if (AdminLogFilterAlert.this.currentFilter != null)
            {
              bool1 = bool7;
              if (!AdminLogFilterAlert.this.currentFilter.info) {}
            }
            else
            {
              bool1 = true;
            }
            paramViewHolder.setText((String)localObject, "", bool1, true);
            return;
          }
          if (paramInt == AdminLogFilterAlert.this.deleteRow)
          {
            localObject = LocaleController.getString("EventLogFilterDeletedMessages", 2131493481);
            if (AdminLogFilterAlert.this.currentFilter != null)
            {
              bool1 = bool8;
              if (!AdminLogFilterAlert.this.currentFilter.delete) {}
            }
            else
            {
              bool1 = true;
            }
            paramViewHolder.setText((String)localObject, "", bool1, true);
            return;
          }
          if (paramInt == AdminLogFilterAlert.this.editRow)
          {
            localObject = LocaleController.getString("EventLogFilterEditedMessages", 2131493482);
            if (AdminLogFilterAlert.this.currentFilter != null)
            {
              bool1 = bool9;
              if (!AdminLogFilterAlert.this.currentFilter.edit) {}
            }
            else
            {
              bool1 = true;
            }
            paramViewHolder.setText((String)localObject, "", bool1, true);
            return;
          }
          if (paramInt == AdminLogFilterAlert.this.pinnedRow)
          {
            localObject = LocaleController.getString("EventLogFilterPinnedMessages", 2131493488);
            if (AdminLogFilterAlert.this.currentFilter != null)
            {
              bool1 = bool10;
              if (!AdminLogFilterAlert.this.currentFilter.pinned) {}
            }
            else
            {
              bool1 = true;
            }
            paramViewHolder.setText((String)localObject, "", bool1, true);
            return;
          }
          if (paramInt == AdminLogFilterAlert.this.leavingRow)
          {
            localObject = LocaleController.getString("EventLogFilterLeavingMembers", 2131493484);
            bool1 = bool4;
            if (AdminLogFilterAlert.this.currentFilter != null) {
              if (!AdminLogFilterAlert.this.currentFilter.leave) {
                break label756;
              }
            }
            for (bool1 = bool4;; bool1 = false)
            {
              paramViewHolder.setText((String)localObject, "", bool1, false);
              return;
            }
          }
        } while (paramInt != AdminLogFilterAlert.this.allAdminsRow);
        localObject = LocaleController.getString("EventLogAllAdmins", 2131493458);
        bool1 = bool11;
        if (AdminLogFilterAlert.this.selectedAdmins == null) {
          bool1 = true;
        }
        paramViewHolder.setText((String)localObject, "", bool1, true);
        return;
      }
      paramViewHolder = (CheckBoxUserCell)paramViewHolder.itemView;
      int i = ((TLRPC.ChannelParticipant)AdminLogFilterAlert.this.currentAdmins.get(paramInt - AdminLogFilterAlert.this.allAdminsRow - 1)).user_id;
      Object localObject = MessagesController.getInstance(AdminLogFilterAlert.this.currentAccount).getUser(Integer.valueOf(i));
      if ((AdminLogFilterAlert.this.selectedAdmins == null) || (AdminLogFilterAlert.this.selectedAdmins.indexOfKey(i) >= 0))
      {
        bool1 = true;
        if (paramInt == getItemCount() - 1) {
          break label920;
        }
      }
      for (;;)
      {
        paramViewHolder.setUser((TLRPC.User)localObject, bool1, bool2);
        return;
        bool1 = false;
        break;
        label920:
        bool2 = false;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      paramViewGroup = null;
      switch (paramInt)
      {
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new CheckBoxCell(this.context, 1);
        paramViewGroup.setBackgroundDrawable(Theme.getSelectorDrawable(false));
        continue;
        ShadowSectionCell localShadowSectionCell = new ShadowSectionCell(this.context);
        localShadowSectionCell.setSize(18);
        paramViewGroup = new FrameLayout(this.context);
        ((FrameLayout)paramViewGroup).addView(localShadowSectionCell, LayoutHelper.createFrame(-1, -1.0F));
        paramViewGroup.setBackgroundColor(Theme.getColor("dialogBackgroundGray"));
        continue;
        paramViewGroup = new CheckBoxUserCell(this.context, true);
      }
    }
    
    public void onViewAttachedToWindow(RecyclerView.ViewHolder paramViewHolder)
    {
      boolean bool3 = true;
      boolean bool4 = true;
      boolean bool5 = true;
      boolean bool6 = true;
      boolean bool7 = true;
      boolean bool8 = true;
      boolean bool9 = true;
      boolean bool1 = true;
      boolean bool10 = true;
      boolean bool2 = true;
      int i = paramViewHolder.getAdapterPosition();
      switch (paramViewHolder.getItemViewType())
      {
      case 1: 
      default: 
      case 0: 
        label187:
        label252:
        label317:
        label369:
        label421:
        label473:
        label525:
        label577:
        do
        {
          return;
          paramViewHolder = (CheckBoxCell)paramViewHolder.itemView;
          if (i == 0)
          {
            if (AdminLogFilterAlert.this.currentFilter == null) {}
            for (bool1 = true;; bool1 = false)
            {
              paramViewHolder.setChecked(bool1, false);
              return;
            }
          }
          if (i == AdminLogFilterAlert.this.restrictionsRow)
          {
            bool1 = bool2;
            if (AdminLogFilterAlert.this.currentFilter != null) {
              if ((!AdminLogFilterAlert.this.currentFilter.kick) || (!AdminLogFilterAlert.this.currentFilter.ban) || (!AdminLogFilterAlert.this.currentFilter.unkick) || (!AdminLogFilterAlert.this.currentFilter.unban)) {
                break label187;
              }
            }
            for (bool1 = bool2;; bool1 = false)
            {
              paramViewHolder.setChecked(bool1, false);
              return;
            }
          }
          if (i == AdminLogFilterAlert.this.adminsRow)
          {
            bool1 = bool3;
            if (AdminLogFilterAlert.this.currentFilter != null) {
              if ((!AdminLogFilterAlert.this.currentFilter.promote) || (!AdminLogFilterAlert.this.currentFilter.demote)) {
                break label252;
              }
            }
            for (bool1 = bool3;; bool1 = false)
            {
              paramViewHolder.setChecked(bool1, false);
              return;
            }
          }
          if (i == AdminLogFilterAlert.this.membersRow)
          {
            bool1 = bool4;
            if (AdminLogFilterAlert.this.currentFilter != null) {
              if ((!AdminLogFilterAlert.this.currentFilter.invite) || (!AdminLogFilterAlert.this.currentFilter.join)) {
                break label317;
              }
            }
            for (bool1 = bool4;; bool1 = false)
            {
              paramViewHolder.setChecked(bool1, false);
              return;
            }
          }
          if (i == AdminLogFilterAlert.this.infoRow)
          {
            bool1 = bool5;
            if (AdminLogFilterAlert.this.currentFilter != null) {
              if (!AdminLogFilterAlert.this.currentFilter.info) {
                break label369;
              }
            }
            for (bool1 = bool5;; bool1 = false)
            {
              paramViewHolder.setChecked(bool1, false);
              return;
            }
          }
          if (i == AdminLogFilterAlert.this.deleteRow)
          {
            bool1 = bool6;
            if (AdminLogFilterAlert.this.currentFilter != null) {
              if (!AdminLogFilterAlert.this.currentFilter.delete) {
                break label421;
              }
            }
            for (bool1 = bool6;; bool1 = false)
            {
              paramViewHolder.setChecked(bool1, false);
              return;
            }
          }
          if (i == AdminLogFilterAlert.this.editRow)
          {
            bool1 = bool7;
            if (AdminLogFilterAlert.this.currentFilter != null) {
              if (!AdminLogFilterAlert.this.currentFilter.edit) {
                break label473;
              }
            }
            for (bool1 = bool7;; bool1 = false)
            {
              paramViewHolder.setChecked(bool1, false);
              return;
            }
          }
          if (i == AdminLogFilterAlert.this.pinnedRow)
          {
            bool1 = bool8;
            if (AdminLogFilterAlert.this.currentFilter != null) {
              if (!AdminLogFilterAlert.this.currentFilter.pinned) {
                break label525;
              }
            }
            for (bool1 = bool8;; bool1 = false)
            {
              paramViewHolder.setChecked(bool1, false);
              return;
            }
          }
          if (i == AdminLogFilterAlert.this.leavingRow)
          {
            bool1 = bool9;
            if (AdminLogFilterAlert.this.currentFilter != null) {
              if (!AdminLogFilterAlert.this.currentFilter.leave) {
                break label577;
              }
            }
            for (bool1 = bool9;; bool1 = false)
            {
              paramViewHolder.setChecked(bool1, false);
              return;
            }
          }
        } while (i != AdminLogFilterAlert.this.allAdminsRow);
        if (AdminLogFilterAlert.this.selectedAdmins == null) {}
        for (;;)
        {
          paramViewHolder.setChecked(bool1, false);
          return;
          bool1 = false;
        }
      }
      paramViewHolder = (CheckBoxUserCell)paramViewHolder.itemView;
      i = ((TLRPC.ChannelParticipant)AdminLogFilterAlert.this.currentAdmins.get(i - AdminLogFilterAlert.this.allAdminsRow - 1)).user_id;
      bool1 = bool10;
      if (AdminLogFilterAlert.this.selectedAdmins != null) {
        if (AdminLogFilterAlert.this.selectedAdmins.indexOfKey(i) < 0) {
          break label688;
        }
      }
      label688:
      for (bool1 = bool10;; bool1 = false)
      {
        paramViewHolder.setChecked(bool1, false);
        return;
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/AdminLogFilterAlert.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */