package org.telegram.ui;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.LocationCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.voip.VoIPHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import kotlin.Unit;
import tw.nekomimi.nekogram.BottomBuilder;

public class CallLogActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private ListAdapter listViewAdapter;
    private EmptyTextProgressView emptyView;
    private LinearLayoutManager layoutManager;
    private RecyclerListView listView;
    private ImageView floatingButton;

    private ArrayList<CallLogRow> calls = new ArrayList<>();
    private boolean loading;
    private boolean firstLoaded;
    private boolean endReached;

    private int prevPosition;
    private int prevTop;
    private boolean scrollUpdated;
    private boolean floatingHidden;
    private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();

    private Drawable greenDrawable;
    private Drawable greenDrawable2;
    private Drawable redDrawable;
    private ImageSpan iconOut, iconIn, iconMissed;
    private TLRPC.User lastCallUser;

    private static final int TYPE_OUT = 0;
    private static final int TYPE_IN = 1;
    private static final int TYPE_MISSED = 2;

    @Override
    @SuppressWarnings("unchecked")
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.didReceiveNewMessages && firstLoaded) {
            boolean scheduled = (Boolean) args[2];
            if (scheduled) {
                return;
            }
            ArrayList<MessageObject> arr = (ArrayList<MessageObject>) args[1];
            for (MessageObject msg : arr) {
                if (msg.messageOwner.action instanceof TLRPC.TL_messageActionPhoneCall) {
                    int fromId = msg.getFromChatId();
					int userID = fromId == UserConfig.getInstance(currentAccount).getClientUserId() ? msg.messageOwner.peer_id.user_id : fromId;
                    int callType = fromId == UserConfig.getInstance(currentAccount).getClientUserId() ? TYPE_OUT : TYPE_IN;
                    TLRPC.PhoneCallDiscardReason reason = msg.messageOwner.action.reason;
                    if (callType == TYPE_IN && (reason instanceof TLRPC.TL_phoneCallDiscardReasonMissed || reason instanceof TLRPC.TL_phoneCallDiscardReasonBusy)) {
                        callType = TYPE_MISSED;
                    }
                    if (calls.size() > 0) {
                        CallLogRow topRow = calls.get(0);
                        if (topRow.user.id == userID && topRow.type == callType) {
                            topRow.calls.add(0, msg.messageOwner);
                            listViewAdapter.notifyItemChanged(0);
                            continue;
                        }
                    }
                    CallLogRow row = new CallLogRow();
                    row.calls = new ArrayList<>();
                    row.calls.add(msg.messageOwner);
                    row.user = MessagesController.getInstance(currentAccount).getUser(userID);
                    row.type = callType;
                    row.video = msg.isVideoCall();calls.add(0, row);
                    listViewAdapter.notifyItemInserted(0);
                }
            }
        } else if (id == NotificationCenter.messagesDeleted && firstLoaded) {
            boolean scheduled = (Boolean) args[2];
            if (scheduled) {
                return;
            }
            boolean didChange = false;
            ArrayList<Integer> ids = (ArrayList<Integer>) args[0];
            Iterator<CallLogRow> itrtr = calls.iterator();
            while (itrtr.hasNext()) {
                CallLogRow row = itrtr.next();
                Iterator<TLRPC.Message> msgs = row.calls.iterator();
                while (msgs.hasNext()) {
                    TLRPC.Message msg = msgs.next();
                    if (ids.contains(msg.id)) {
                        didChange = true;
                        msgs.remove();
                    }
                }
                if (row.calls.size() == 0)
                    itrtr.remove();
            }
            if (didChange && listViewAdapter != null)
                listViewAdapter.notifyDataSetChanged();
        }
    }

    private class CustomCell extends FrameLayout {

        private ImageView imageView;
        private ProfileSearchCell profileSearchCell;

        public CustomCell(Context context) {
            super(context);

            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            profileSearchCell = new ProfileSearchCell(context);
            profileSearchCell.setPadding(LocaleController.isRTL ? AndroidUtilities.dp(32) : 0, 0, LocaleController.isRTL ? 0 : AndroidUtilities.dp(32), 0);
            profileSearchCell.setSublabelOffset(AndroidUtilities.dp(LocaleController.isRTL ? 2 : -2), -AndroidUtilities.dp(4));
            addView(profileSearchCell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            imageView = new ImageView(context);

            imageView.setAlpha(214);
            imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_featuredStickers_addButton), PorterDuff.Mode.SRC_IN));
            imageView.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 1));
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setOnClickListener(callBtnClickListener);
            imageView.setContentDescription(LocaleController.getString("Call", R.string.Call));
            addView(imageView, LayoutHelper.createFrame(48, 48, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL, 8, 0, 8, 0));
        }
    }

    private View.OnClickListener callBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CallLogRow row = (CallLogRow) v.getTag();
            TLRPC.UserFull userFull = getMessagesController().getUserFull(row.user.id);
			VoIPHelper.startCall(lastCallUser = row.user, row.video, row.video || userFull != null && userFull.video_calls_available,getParentActivity(), null);
        }
    };

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        getCalls(0, 50);

        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didReceiveNewMessages);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.messagesDeleted);

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didReceiveNewMessages);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.messagesDeleted);
    }

    @Override
    public View createView(Context context) {
        greenDrawable = getParentActivity().getResources().getDrawable(R.drawable.ic_call_made_green_18dp).mutate();
        greenDrawable.setBounds(0, 0, greenDrawable.getIntrinsicWidth(), greenDrawable.getIntrinsicHeight());
        greenDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_calls_callReceivedGreenIcon), PorterDuff.Mode.SRC_IN));
        iconOut = new ImageSpan(greenDrawable, ImageSpan.ALIGN_BOTTOM);
        greenDrawable2 = getParentActivity().getResources().getDrawable(R.drawable.ic_call_received_green_18dp).mutate();
        greenDrawable2.setBounds(0, 0, greenDrawable2.getIntrinsicWidth(), greenDrawable2.getIntrinsicHeight());
        greenDrawable2.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_calls_callReceivedGreenIcon), PorterDuff.Mode.SRC_IN));
        iconIn = new ImageSpan(greenDrawable2, ImageSpan.ALIGN_BOTTOM);
        redDrawable = getParentActivity().getResources().getDrawable(R.drawable.ic_call_received_green_18dp).mutate();
        redDrawable.setBounds(0, 0, redDrawable.getIntrinsicWidth(), redDrawable.getIntrinsicHeight());
        redDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_calls_callReceivedRedIcon), PorterDuff.Mode.SRC_IN));
        iconMissed = new ImageSpan(redDrawable, ImageSpan.ALIGN_BOTTOM);

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("Calls", R.string.Calls));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        emptyView = new EmptyTextProgressView(context);
        emptyView.setText(LocaleController.getString("NoCallLog", R.string.NoCallLog));
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView = new RecyclerListView(context);
        listView.setEmptyView(emptyView);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(listViewAdapter = new ListAdapter(context));
        listView.setVerticalScrollbarPosition(LocaleController.isRTL ? RecyclerListView.SCROLLBAR_POSITION_LEFT : RecyclerListView.SCROLLBAR_POSITION_RIGHT);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView.setOnItemClickListener((view, position) -> {
            if (position < 0 || position >= calls.size()) {
                return;
            }
            CallLogRow row = calls.get(position);
            Bundle args = new Bundle();
            args.putInt("user_id", row.user.id);
            args.putInt("message_id", row.calls.get(0).id);
            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.closeChats);
            presentFragment(new ChatActivity(args), true);
        });
        listView.setOnItemLongClickListener((view, position) -> {
            if (position < 0 || position >= calls.size()) {
                return false;
            }
            final CallLogRow row = calls.get(position);
            ArrayList<String> items = new ArrayList<>();
            items.add(LocaleController.getString("Delete", R.string.Delete));
            if (VoIPHelper.canRateCall((TLRPC.TL_messageActionPhoneCall) row.calls.get(0).action)) {
                items.add(LocaleController.getString("CallMessageReportProblem", R.string.CallMessageReportProblem));
            }
            new AlertDialog.Builder(getParentActivity())
                    .setTitle(LocaleController.getString("Calls", R.string.Calls))
                    .setItems(items.toArray(new String[0]), (dialog, which) -> {
                        switch (which) {
                            case 0:
                                confirmAndDelete(row);
                                break;
                            case 1:
                                VoIPHelper.showRateAlert(getParentActivity(), (TLRPC.TL_messageActionPhoneCall) row.calls.get(0).action);
                                break;
                        }
                    })
                    .show();
            return true;
        });
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = firstVisibleItem == RecyclerView.NO_POSITION ? 0 : Math.abs(layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
                if (visibleItemCount > 0) {
                    int totalItemCount = listViewAdapter.getItemCount();
                    if (!endReached && !loading && !calls.isEmpty() && firstVisibleItem + visibleItemCount >= totalItemCount - 5) {
                        final CallLogRow row = calls.get(calls.size() - 1);
                        AndroidUtilities.runOnUIThread(() -> getCalls(row.calls.get(row.calls.size() - 1).id, 100));
                    }
                }

                if (floatingButton.getVisibility() != View.GONE) {
                    final View topChild = recyclerView.getChildAt(0);
                    int firstViewTop = 0;
                    if (topChild != null) {
                        firstViewTop = topChild.getTop();
                    }
                    boolean goingDown;
                    boolean changed = true;
                    if (prevPosition == firstVisibleItem) {
                        final int topDelta = prevTop - firstViewTop;
                        goingDown = firstViewTop < prevTop;
                        changed = Math.abs(topDelta) > 1;
                    } else {
                        goingDown = firstVisibleItem > prevPosition;
                    }
                    if (changed && scrollUpdated) {
                        hideFloatingButton(goingDown);
                    }
                    prevPosition = firstVisibleItem;
                    prevTop = firstViewTop;
                    scrollUpdated = true;
                }
            }
        });

        if (loading) {
            emptyView.showProgress();
        } else {
            emptyView.showTextView();
        }


        floatingButton = new ImageView(context);
        floatingButton.setVisibility(View.VISIBLE);
        floatingButton.setScaleType(ImageView.ScaleType.CENTER);

        Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), Theme.getColor(Theme.key_chats_actionBackground), Theme.getColor(Theme.key_chats_actionPressedBackground));
        if (Build.VERSION.SDK_INT < 21) {
            Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.SRC_IN));
            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
            drawable = combinedDrawable;
        }
        floatingButton.setBackgroundDrawable(drawable);
        floatingButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_actionIcon), PorterDuff.Mode.SRC_IN));
        floatingButton.setImageResource(R.drawable.ic_call);
        floatingButton.setContentDescription(LocaleController.getString("Call", R.string.Call));
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            floatingButton.setStateListAnimator(animator);
            floatingButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        frameLayout.addView(floatingButton, LayoutHelper.createFrame(Build.VERSION.SDK_INT >= 21 ? 56 : 60, Build.VERSION.SDK_INT >= 21 ? 56 : 60, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, 14));
        floatingButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean("destroyAfterSelect", true);
            args.putBoolean("returnAsResult", true);
            args.putBoolean("onlyUsers", true);
            args.putBoolean("allowSelf", false);
            ContactsActivity contactsFragment = new ContactsActivity(args);
            contactsFragment.setDelegate((user, param, activity) -> {
				TLRPC.UserFull userFull = getMessagesController().getUserFull(user.id);
				VoIPHelper.startCall(lastCallUser = user, false, userFull != null && userFull.video_calls_available, getParentActivity(), null);
			});
            presentFragment(contactsFragment);
        });

        return fragmentView;
    }

    private void hideFloatingButton(boolean hide) {
        if (floatingHidden == hide) {
            return;
        }
        floatingHidden = hide;
        ObjectAnimator animator = ObjectAnimator.ofFloat(floatingButton, "translationY", floatingHidden ? AndroidUtilities.dp(100) : 0).setDuration(300);
        animator.setInterpolator(floatingInterpolator);
        floatingButton.setClickable(!hide);
        animator.start();
    }

    private void getCalls(int max_id, final int count) {
        if (loading) {
            return;
        }
        loading = true;
        if (emptyView != null && !firstLoaded) {
            emptyView.showProgress();
        }
        if (listViewAdapter != null) {
            listViewAdapter.notifyDataSetChanged();
        }
        TLRPC.TL_messages_search req = new TLRPC.TL_messages_search();
        req.limit = count;
        req.peer = new TLRPC.TL_inputPeerEmpty();
        req.filter = new TLRPC.TL_inputMessagesFilterPhoneCalls();
        req.q = "";
        req.offset_id = max_id;
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null) {
                SparseArray<TLRPC.User> users = new SparseArray<>();
                TLRPC.messages_Messages msgs = (TLRPC.messages_Messages) response;
                endReached = msgs.messages.isEmpty();
                for (int a = 0; a < msgs.users.size(); a++) {
                    TLRPC.User user = msgs.users.get(a);
                    users.put(user.id, user);
                }
                CallLogRow currentRow = calls.size() > 0 ? calls.get(calls.size() - 1) : null;
                for (int a = 0; a < msgs.messages.size(); a++) {
                    TLRPC.Message msg = msgs.messages.get(a);
                    if (msg.action == null || msg.action instanceof TLRPC.TL_messageActionHistoryClear) {
                        continue;
                    }
                    int callType = MessageObject.getFromChatId(msg) == UserConfig.getInstance(currentAccount).getClientUserId() ? TYPE_OUT : TYPE_IN;
                    TLRPC.PhoneCallDiscardReason reason = msg.action.reason;
                    if (callType == TYPE_IN && (reason instanceof TLRPC.TL_phoneCallDiscardReasonMissed || reason instanceof TLRPC.TL_phoneCallDiscardReasonBusy)) {
                        callType = TYPE_MISSED;
                    }
                    int fromId = MessageObject.getFromChatId(msg);
					int userID = fromId == UserConfig.getInstance(currentAccount).getClientUserId() ? msg.peer_id.user_id : fromId;
                    if (currentRow == null || currentRow.user.id != userID || currentRow.type != callType) {
                        if (currentRow != null && !calls.contains(currentRow)) {
                            calls.add(currentRow);
                        }
                        CallLogRow row = new CallLogRow();
                        row.calls = new ArrayList<>();
                        row.user = users.get(userID);
                        row.type = callType;
                        row.video = msg.action != null && msg.action.video;currentRow = row;
                    }
                    currentRow.calls.add(msg);
                }
                if (currentRow != null && currentRow.calls.size() > 0 && !calls.contains(currentRow)) {
                    calls.add(currentRow);
                }
            } else {
                endReached = true;
            }
            loading = false;
            firstLoaded = true;
            if (emptyView != null) {
                emptyView.showTextView();
            }
            if (listViewAdapter != null) {
                listViewAdapter.notifyDataSetChanged();
            }
        }), ConnectionsManager.RequestFlagFailOnServerErrors);
        ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
    }

    private void confirmAndDelete(final CallLogRow row) {
        if (getParentActivity() == null)
            return;
        BottomBuilder builder = new BottomBuilder(getParentActivity());

        builder.addTitle(LocaleController.getString("ConfirmDeleteCallLog", R.string.ConfirmDeleteCallLog));
        builder.addItem(LocaleController.getString("Delete", R.string.Delete), R.drawable.baseline_delete_24, (cell) -> {
            ArrayList<Integer> ids = new ArrayList<>();
            for (TLRPC.Message msg : row.calls) {
                ids.add(msg.id);
            }
            MessagesController.getInstance(currentAccount).deleteMessages(ids, null, null, 0, 0, false, false);
            return Unit.INSTANCE;
        });
        builder.addCancelItem();
        builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listViewAdapter != null) {
            listViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 101|| requestCode == 102) {
			boolean allGranted = true;
            for (int a = 0; a < grantResults.length; a++) {
				if (grantResults[a] != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
					break;
				}
			}
			if (grantResults.length > 0 && allGranted) {
				TLRPC.UserFull userFull = lastCallUser != null ? getMessagesController().getUserFull(lastCallUser.id) : null;
				VoIPHelper.startCall(lastCallUser, requestCode == 102, requestCode == 102 || userFull != null && userFull.video_calls_available,getParentActivity(), null);
            } else {
                VoIPHelper.permissionDenied(getParentActivity(), null, requestCode);
            }
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getAdapterPosition() != calls.size();
        }

        @Override
        public int getItemCount() {
            int count = calls.size();
            if (!calls.isEmpty()) {
                if (!endReached) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    CustomCell cell = new CustomCell(mContext);
                    view = cell;
                    view.setTag(new ViewItem(cell.imageView, cell.profileSearchCell));
                    break;
                case 1:
                    view = new LoadingCell(mContext);
                    break;
                case 2:
                default:
                    view = new TextInfoPrivacyCell(mContext);
                    view.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder.getItemViewType() == 0) {
                CustomCell customCell = (CustomCell) holder.itemView;
				ViewItem viewItem = (ViewItem) customCell.getTag();

                CallLogRow row = calls.get(position);customCell.imageView.setImageResource(row.video ? R.drawable.profile_video : R.drawable.profile_phone);
				ProfileSearchCell cell = viewItem.cell;
                TLRPC.Message last = row.calls.get(0);
                SpannableString subtitle;
                String ldir = LocaleController.isRTL ? "\u202b" : "";
                if (row.calls.size() == 1) {
                    subtitle = new SpannableString(ldir + "  " + LocaleController.formatDateCallLog(last.date));
                } else {
                    subtitle = new SpannableString(String.format(ldir + "  (%d) %s", row.calls.size(), LocaleController.formatDateCallLog(last.date)));
                }
                switch (row.type) {
                    case TYPE_OUT:
                        subtitle.setSpan(iconOut, ldir.length(), ldir.length() + 1, 0);
                        //cell.setContentDescription(LocaleController.getString("CallMessageOutgoing", R.string.CallMessageOutgoing));
                        break;
                    case TYPE_IN:
                        subtitle.setSpan(iconIn, ldir.length(), ldir.length() + 1, 0);
                        //cell.setContentDescription(LocaleController.getString("CallMessageIncoming", R.string.CallMessageIncoming));
                        break;
                    case TYPE_MISSED:
                        subtitle.setSpan(iconMissed, ldir.length(), ldir.length() + 1, 0);
                        //cell.setContentDescription(LocaleController.getString("CallMessageIncomingMissed", R.string.CallMessageIncomingMissed));
                        break;
                }
                cell.setData(row.user, null, null, subtitle, false, false);
                cell.useSeparator = position != calls.size() - 1 || !endReached;
                viewItem.button.setTag(row);
            }
        }

        @Override
        public int getItemViewType(int i) {
            if (i < calls.size()) {
                return 0;
            } else if (!endReached && i == calls.size()) {
                return 1;
            }
            return 2;
        }
    }

    private static class ViewItem {
        public ProfileSearchCell cell;
        public ImageView button;

        public ViewItem(ImageView button, ProfileSearchCell cell) {
            this.button = button;
            this.cell = cell;
        }
    }

    private static class CallLogRow {
        public TLRPC.User user;
        public List<TLRPC.Message> calls;
        public int type;
    public boolean video;
	}

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        ThemeDescription.ThemeDescriptionDelegate cellDelegate = () -> {
            if (listView != null) {
                int count = listView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = listView.getChildAt(a);
                    if (child instanceof CustomCell) {
                        CustomCell cell = (CustomCell) child;
                        cell.profileSearchCell.update(0);
                    }
                }
            }
        };


        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{LocationCell.class, CustomCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        themeDescriptions.add(new ThemeDescription(emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder));
        themeDescriptions.add(new ThemeDescription(emptyView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_progressCircle));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{LoadingCell.class}, new String[]{"progressBar"}, null, null, null, Theme.key_progressCircle));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4));

        themeDescriptions.add(new ThemeDescription(floatingButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_chats_actionIcon));
        themeDescriptions.add(new ThemeDescription(floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_chats_actionBackground));
        themeDescriptions.add(new ThemeDescription(floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_chats_actionPressedBackground));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{CustomCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_featuredStickers_addButton));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{CustomCell.class}, null, new Drawable[]{Theme.dialogs_verifiedCheckDrawable}, null, Theme.key_chats_verifiedCheck));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{CustomCell.class}, null, new Drawable[]{Theme.dialogs_verifiedDrawable}, null, Theme.key_chats_verifiedBackground));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{CustomCell.class}, Theme.dialogs_offlinePaint, null, null, Theme.key_windowBackgroundWhiteGrayText3));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{CustomCell.class}, Theme.dialogs_onlinePaint, null, null, Theme.key_windowBackgroundWhiteBlueText3));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{CustomCell.class}, null, new Paint[]{Theme.dialogs_namePaint[0], Theme.dialogs_namePaint[1], Theme.dialogs_searchNamePaint}, null, null, Theme.key_chats_name));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{CustomCell.class}, null, new Paint[]{Theme.dialogs_nameEncryptedPaint[0], Theme.dialogs_nameEncryptedPaint[1], Theme.dialogs_searchNameEncryptedPaint}, null, null, Theme.key_chats_secretName));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{CustomCell.class}, null, Theme.avatarDrawables, null, Theme.key_avatar_text));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundRed));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundOrange));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundViolet));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundGreen));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundCyan));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundBlue));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundPink));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, null, new Drawable[]{greenDrawable, greenDrawable2, Theme.calllog_msgCallUpRedDrawable, Theme.calllog_msgCallDownRedDrawable}, null, Theme.key_calls_callReceivedGreenIcon));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, null, new Drawable[]{redDrawable, Theme.calllog_msgCallUpGreenDrawable, Theme.calllog_msgCallDownGreenDrawable}, null, Theme.key_calls_callReceivedRedIcon));

        return themeDescriptions;
    }
}
