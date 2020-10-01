/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Property;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;

import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import tw.nekomimi.nekogram.NekoConfig;

public class FilterTabsView extends FrameLayout {

    public interface FilterTabsViewDelegate {
        void onPageSelected(int page, boolean forward);
        void onPageScrolled(float progress);
        void onSamePageSelected();
        int getTabCounter(int tabId);
        boolean didSelectTab(TabView tabView, boolean selected);
        boolean isTabMenuVisible();
        void onDeletePressed(int id);
        void onPageReorder(int fromId, int toId);
        boolean canPerformActions();
    }

    private class Tab {
        public int id;
        public String title;
        public int titleWidth;
        public int counter;

        public Tab(int i, String t) {
            id = i;
            title = t;
        }

        public int getWidth(boolean store) {
            int width = titleWidth = (int) Math.ceil(textPaint.measureText(title));
            int c;
            if (store) {
                c = delegate.getTabCounter(id);
                if (c < 0) {
                    c = 0;
                }
                if (store) {
                    counter = c;
                }
            } else {
                c = counter;
            }
            if (c > 0) {
                String counterText = String.format("%d", c);
                int counterWidth = (int) Math.ceil(textCounterPaint.measureText(counterText));
                int countWidth = Math.max(AndroidUtilities.dp(10), counterWidth) + AndroidUtilities.dp(10);
                width += countWidth + AndroidUtilities.dp(6);
            }
            return Math.max(AndroidUtilities.dp(40), width);
        }

        public boolean setTitle(String newTitle) {
            if (TextUtils.equals(title, newTitle)) {
                return false;
            }
            title = newTitle;
            return true;
        }
    }

    public class TabView extends View {

        private Tab currentTab;
        private int textHeight;
        private int tabWidth;
        private int currentPosition;
        private RectF rect = new RectF();
        private String currentText;
        private StaticLayout textLayout;
        private int textOffsetX;

        public TabView(Context context) {
            super(context);
        }

        public void setTab(Tab tab, int position) {
            currentTab = tab;
            currentPosition = position;
            requestLayout();
        }

        public int getId() {
            return currentTab.id;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int w = currentTab.getWidth(false) + AndroidUtilities.dp(32) + additionalTabWidth;
            setMeasuredDimension(w, MeasureSpec.getSize(heightMeasureSpec));
        }

        @SuppressLint("DrawAllocation")
        @Override
        protected void onDraw(Canvas canvas) {
            if (currentTab.id != Integer.MAX_VALUE && editingAnimationProgress != 0) {
                canvas.save();
                float p = editingAnimationProgress * (currentPosition % 2 == 0 ? 1.0f : -1.0f);
                canvas.translate(AndroidUtilities.dp(0.66f) * p, 0);
                canvas.rotate(p, getMeasuredWidth() / 2, getMeasuredHeight() / 2);
            }
            String key;
            String animateToKey;
            String otherKey;
            String animateToOtherKey;
            String unreadKey;
            String unreadOtherKey;
            int id1;
            int id2;
            if (manualScrollingToId != -1) {
                id1 = manualScrollingToId;
                id2 = selectedTabId;
            } else {
                id1 = selectedTabId;
                id2 = previousId;
            }
            if (currentTab.id == id1) {
                key = activeTextColorKey;
                animateToKey = aActiveTextColorKey;
                otherKey = unactiveTextColorKey;
                animateToOtherKey = aUnactiveTextColorKey;
                unreadKey = Theme.key_chats_tabUnreadActiveBackground;
                unreadOtherKey = Theme.key_chats_tabUnreadUnactiveBackground;
            } else {
                key = unactiveTextColorKey;
                animateToKey = aUnactiveTextColorKey;
                otherKey = activeTextColorKey;
                animateToOtherKey = aUnactiveTextColorKey;
                unreadKey = Theme.key_chats_tabUnreadUnactiveBackground;
                unreadOtherKey = Theme.key_chats_tabUnreadActiveBackground;
            }
            if (animateToKey == null) {
                if ((animatingIndicator || manualScrollingToId != -1) && (currentTab.id == id1 || currentTab.id == id2)) {
                    textPaint.setColor(ColorUtils.blendARGB(Theme.getColor(otherKey), Theme.getColor(key), animatingIndicatorProgress));
                } else {
                    textPaint.setColor(Theme.getColor(key));
                }
            } else {
                int color1 = Theme.getColor(key);
                int color2 = Theme.getColor(animateToKey);
                if ((animatingIndicator || manualScrollingToPosition != -1) && (currentTab.id == id1 || currentTab.id == id2)) {
                    int color3 = Theme.getColor(otherKey);
                    int color4 = Theme.getColor(animateToOtherKey);
                    textPaint.setColor(ColorUtils.blendARGB(ColorUtils.blendARGB(color3, color4, animationValue), ColorUtils.blendARGB(color1, color2, animationValue), animatingIndicatorProgress));
                } else {
                    textPaint.setColor(ColorUtils.blendARGB(color1, color2, animationValue));
                }
            }

            int counterWidth;
            int countWidth;
            String counterText;
            if (currentTab.counter > 0) {
                counterText = String.format("%d", currentTab.counter);
                counterWidth = (int) Math.ceil(textCounterPaint.measureText(counterText));
                countWidth = Math.max(AndroidUtilities.dp(10), counterWidth) + AndroidUtilities.dp(10);
            } else {
                counterText = null;
                counterWidth = 0;
                countWidth = 0;
            }

            if (currentTab.id != Integer.MAX_VALUE && (isEditing || editingStartAnimationProgress != 0)) {
                countWidth = (int) (countWidth + (AndroidUtilities.dp(20) - countWidth) * editingStartAnimationProgress);
            }

            tabWidth = currentTab.titleWidth + (countWidth != 0 ? countWidth + AndroidUtilities.dp(6 * (counterText != null ? 1.0f : editingStartAnimationProgress)) : 0);
            int textX = (getMeasuredWidth() - tabWidth) / 2;
            if (!TextUtils.equals(currentTab.title, currentText)) {
                currentText = currentTab.title;
                CharSequence text = Emoji.replaceEmoji(currentText, textPaint.getFontMetricsInt(), AndroidUtilities.dp(15), false);
                textLayout = new StaticLayout(text, textPaint, AndroidUtilities.dp(400), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
                textHeight = textLayout.getHeight();
                textOffsetX = (int) -textLayout.getLineLeft(0);
            }
            if (textLayout != null) {
                canvas.save();
                canvas.translate(textX + textOffsetX, (getMeasuredHeight() - textHeight) / 2 + 1);
                textLayout.draw(canvas);
                canvas.restore();
            }

            if (counterText != null || currentTab.id != Integer.MAX_VALUE && (isEditing || editingStartAnimationProgress != 0)) {
                if (aBackgroundColorKey == null) {
                    textCounterPaint.setColor(Theme.getColor(backgroundColorKey));
                } else {
                    int color1 = Theme.getColor(backgroundColorKey);
                    int color2 = Theme.getColor(aBackgroundColorKey);
                    textCounterPaint.setColor(ColorUtils.blendARGB(color1, color2, animationValue));
                }
                if (Theme.hasThemeKey(unreadKey) && Theme.hasThemeKey(unreadOtherKey)) {
                    int color1 = Theme.getColor(unreadKey);
                    if ((animatingIndicator || manualScrollingToPosition != -1) && (currentTab.id == id1 || currentTab.id == id2)) {
                        int color3 = Theme.getColor(unreadOtherKey);
                        counterPaint.setColor(ColorUtils.blendARGB(color3, color1, animatingIndicatorProgress));
                    } else {
                        counterPaint.setColor(color1);
                    }
                } else {
                    counterPaint.setColor(textPaint.getColor());
                }

                int x = textX + currentTab.titleWidth + AndroidUtilities.dp(6);
                int countTop = (getMeasuredHeight() - AndroidUtilities.dp(20)) / 2;

                if (currentTab.id != Integer.MAX_VALUE && (isEditing || editingStartAnimationProgress != 0) && counterText == null) {
                    counterPaint.setAlpha((int) (editingStartAnimationProgress * 255));
                } else {
                    counterPaint.setAlpha(255);
                }

                rect.set(x, countTop, x + countWidth, countTop + AndroidUtilities.dp(20));
                canvas.drawRoundRect(rect, 11.5f * AndroidUtilities.density, 11.5f * AndroidUtilities.density, counterPaint);

                if (counterText != null) {
                    if (currentTab.id != Integer.MAX_VALUE) {
                        textCounterPaint.setAlpha((int) (255 * (1.0f - editingStartAnimationProgress)));
                    }
                    canvas.drawText(counterText, rect.left + (rect.width() - counterWidth) / 2, countTop + AndroidUtilities.dp(14.5f), textCounterPaint);
                }
                if (currentTab.id != Integer.MAX_VALUE && (isEditing || editingStartAnimationProgress != 0)) {
                    deletePaint.setColor(textCounterPaint.getColor());
                    deletePaint.setAlpha((int) (255 * editingStartAnimationProgress));
                    int side = AndroidUtilities.dp(3);
                    canvas.drawLine(rect.centerX() - side, rect.centerY() - side, rect.centerX() + side, rect.centerY() + side, deletePaint);
                    canvas.drawLine(rect.centerX() - side, rect.centerY() + side, rect.centerX() + side, rect.centerY() - side, deletePaint);
                }
            }
            if (currentTab.id != Integer.MAX_VALUE && editingAnimationProgress != 0) {
                canvas.restore();
            }
        }
    }

    private TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint textCounterPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private Paint deletePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private Paint counterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private ArrayList<Tab> tabs = new ArrayList<>();

    private Bitmap crossfadeBitmap;
    private Paint crossfadePaint = new Paint();
    private float crossfadeAlpha;
    private boolean commitCrossfade;

    private boolean isEditing;
    private long lastEditingAnimationTime;
    private boolean editingForwardAnimation;
    private float editingAnimationProgress;
    private float editingStartAnimationProgress;

    private AnimatorSet colorChangeAnimator;

    private boolean orderChanged;

    private boolean ignoreLayout;

    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;
    private ListAdapter adapter;

    private FilterTabsViewDelegate delegate;

    private int currentPosition;
    private int selectedTabId = -1;
    private int allTabsWidth;

    private int additionalTabWidth;

    private boolean animatingIndicator;
    private float animatingIndicatorProgress;
    private int manualScrollingToPosition = -1;
    private int manualScrollingToId = -1;

    private int scrollingToChild = -1;
    private GradientDrawable selectorDrawable;

    private String tabLineColorKey = Theme.key_actionBarTabLine;
    private String activeTextColorKey = Theme.key_actionBarTabActiveText;
    private String unactiveTextColorKey = Theme.key_actionBarTabUnactiveText;
    private String selectorColorKey = Theme.key_actionBarTabSelector;
    private String backgroundColorKey = Theme.key_actionBarDefault;
    private String aTabLineColorKey;
    private String aActiveTextColorKey;
    private String aUnactiveTextColorKey;
    private String aBackgroundColorKey;

    private int prevLayoutWidth;

    private boolean invalidated;

    private CubicBezierInterpolator interpolator = CubicBezierInterpolator.EASE_OUT_QUINT;

    private SparseIntArray positionToId = new SparseIntArray(5);
    private SparseIntArray idToPosition = new SparseIntArray(5);
    private SparseIntArray positionToWidth = new SparseIntArray(5);
    private SparseIntArray positionToX = new SparseIntArray(5);

    private boolean animationRunning;
    private long lastAnimationTime;
    private float animationTime;
    private int previousPosition;
    private int previousId;
    private Runnable animationRunnable = new Runnable() {
        @Override
        public void run() {
            if (!animatingIndicator) {
                return;
            }
            long newTime = SystemClock.elapsedRealtime();
            long dt = (newTime - lastAnimationTime);
            if (dt > 17) {
                dt = 17;
            }
            animationTime += dt / 200.0f;
            setAnimationIdicatorProgress(interpolator.getInterpolation(animationTime));
            if (animationTime > 1.0f) {
                animationTime = 1.0f;
            }
            if (animationTime < 1.0f) {
                AndroidUtilities.runOnUIThread(animationRunnable);
            } else {
                animatingIndicator = false;
                setEnabled(true);
                if (delegate != null) {
                    delegate.onPageScrolled(1.0f);
                }
            }
        }
    };

    private float animationValue;
    private final Property<FilterTabsView, Float> COLORS = new AnimationProperties.FloatProperty<FilterTabsView>("animationValue") {
        @Override
        public void setValue(FilterTabsView object, float value) {
            animationValue = value;

            int color1 = Theme.getColor(tabLineColorKey);
            int color2 = Theme.getColor(aTabLineColorKey);
            selectorDrawable.setColor(ColorUtils.blendARGB(color1, color2, value));

            listView.invalidateViews();
            listView.invalidate();
            object.invalidate();
        }

        @Override
        public Float get(FilterTabsView object) {
            return animationValue;
        }
    };

    public FilterTabsView(Context context) {
        super(context);
        textCounterPaint.setTextSize(AndroidUtilities.dp(13));
        textCounterPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textPaint.setTextSize(AndroidUtilities.dp(15));
        textPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        deletePaint.setStyle(Paint.Style.STROKE);
        deletePaint.setStrokeCap(Paint.Cap.ROUND);
        deletePaint.setStrokeWidth(AndroidUtilities.dp(1.5f));

        selectorDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, null);
        float rad = AndroidUtilities.dpf2(3);
        selectorDrawable.setCornerRadii(new float[]{rad, rad, rad, rad, 0, 0, 0, 0});
        selectorDrawable.setColor(Theme.getColor(tabLineColorKey));

        setHorizontalScrollBarEnabled(false);
        listView = new RecyclerListView(context) {
            @Override
            public void setAlpha(float alpha) {
                super.setAlpha(alpha);
                FilterTabsView.this.invalidate();
            }

            @Override
            protected boolean allowSelectChildAtPosition(View child) {
                return FilterTabsView.this.isEnabled() && delegate.canPerformActions();
            }

            @Override
            protected boolean canHighlightChildAt(View child, float x, float y) {
                if (isEditing) {
                    TabView tabView = (TabView) child;
                    int side = AndroidUtilities.dp(6);
                    if (tabView.rect.left - side < x && tabView.rect.right + side > x) {
                        return false;
                    }
                }
                return super.canHighlightChildAt(child, x, y);
            }
        };
        ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        listView.setSelectorType(7);
        listView.setSelectorDrawableColor(Theme.getColor(selectorColorKey));
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
                    @Override
                    protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
                        int dx = calculateDxToMakeVisible(targetView, getHorizontalSnapPreference());
                        if (dx > 0 || dx == 0 && targetView.getLeft() - AndroidUtilities.dp(21) < 0) {
                            dx += AndroidUtilities.dp(60);
                        } else if (dx < 0 || dx == 0 && targetView.getRight() + AndroidUtilities.dp(21) > getMeasuredWidth()) {
                            dx -= AndroidUtilities.dp(60);
                        }

                        final int dy = calculateDyToMakeVisible(targetView, getVerticalSnapPreference());
                        final int distance = (int) Math.sqrt(dx * dx + dy * dy);
                        final int time = Math.max(180, calculateTimeForDeceleration(distance));
                        if (time > 0) {
                            action.update(-dx, -dy, time, mDecelerateInterpolator);
                        }
                    }
                };
                linearSmoothScroller.setTargetPosition(position);
                startSmoothScroll(linearSmoothScroller);
            }

            @Override
            public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
                if (delegate.isTabMenuVisible()) {
                    dx = 0;
                }
                return super.scrollHorizontallyBy(dx, recycler, state);
            }
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(listView);
        listView.setPadding(AndroidUtilities.dp(7), 0, AndroidUtilities.dp(7), 0);
        listView.setClipToPadding(false);
        listView.setDrawSelectorBehind(true);
        listView.setAdapter(adapter = new ListAdapter(context));
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (!delegate.canPerformActions()) {
                return;
            }
            TabView tabView = (TabView) view;
            if (isEditing) {
                if (position != 0) {
                    int side = AndroidUtilities.dp(6);
                    if (tabView.rect.left - side < x && tabView.rect.right + side > x) {
                        delegate.onDeletePressed(tabView.currentTab.id);
                    }
                }
                return;
            }
            if (position == currentPosition && delegate != null) {
                delegate.onSamePageSelected();
                return;
            }
            scrollToTab(tabView.currentTab.id, position);
        });
        listView.setOnItemLongClickListener((view, position) -> {
            if (!delegate.canPerformActions() || isEditing || !delegate.didSelectTab((TabView) view, position == currentPosition)) {
                return false;
            }
            listView.hideSelector(true);
            return true;
        });
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                invalidate();
            }
        });
        addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
    }

    public void setDelegate(FilterTabsViewDelegate filterTabsViewDelegate) {
        delegate = filterTabsViewDelegate;
    }

    public boolean isAnimatingIndicator() {
        return animatingIndicator;
    }

    private void scrollToTab(int id, int position) {
        boolean scrollingForward = currentPosition < position;
        scrollingToChild = -1;
        previousPosition = currentPosition;
        previousId = selectedTabId;
        currentPosition = position;
        selectedTabId = id;

        if (animatingIndicator) {
            AndroidUtilities.cancelRunOnUIThread(animationRunnable);
            animatingIndicator = false;
        }

        animationTime = 0;
        animatingIndicatorProgress = 0;
        animatingIndicator = true;
        setEnabled(false);

        AndroidUtilities.runOnUIThread(animationRunnable, 16);

        if (delegate != null) {
            delegate.onPageSelected(id, scrollingForward);
        }
        scrollToChild(position);
    }

    public void selectFirstTab() {
        scrollToTab(Integer.MAX_VALUE, 0);
    }

    public void setAnimationIdicatorProgress(float value) {
        animatingIndicatorProgress = value;
        listView.invalidateViews();
        invalidate();
        if (delegate != null) {
            delegate.onPageScrolled(value);
        }
    }

    public Drawable getSelectorDrawable() {
        return selectorDrawable;
    }

    public RecyclerListView getTabsContainer() {
        return listView;
    }

    public int getNextPageId(boolean forward) {
        return positionToId.get(currentPosition + (forward ? 1 : -1), -1);
    }

    public void removeTabs() {
        tabs.clear();
        positionToId.clear();
        idToPosition.clear();
        positionToWidth.clear();
        positionToX.clear();
        allTabsWidth = 0;
    }

    public boolean hasTab(int id) {
        return idToPosition.get(id, -1) != -1;
    }

    public void resetTabId() {
        selectedTabId = -1;
    }

    public void beginCrossfade() {
        try {
            Bitmap bitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            this.draw(canvas);
            crossfadeBitmap = bitmap;
            crossfadeAlpha = 1.0f;
            commitCrossfade = false;
            listView.invalidate();
            invalidate();
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    public void commitCrossfade() {
        if (crossfadeBitmap == null) {
            return;
        }
        commitCrossfade = true;
        listView.invalidate();
        invalidate();
    }

    public void addTab(int id, String text) {
        int position = tabs.size();
        if (position == 0 && selectedTabId == -1) {
            selectedTabId = id;
        }
        positionToId.put(position, id);
        idToPosition.put(id, position);
        if (selectedTabId != -1 && selectedTabId == id) {
            currentPosition = position;
        }

        Tab tab = new Tab(id, text);
        allTabsWidth += tab.getWidth(true) + AndroidUtilities.dp(32);
        tabs.add(tab);
    }

    public void finishAddingTabs() {
        adapter.notifyDataSetChanged();
    }

    public void animateColorsTo(String line, String active, String unactive, String selector, String background) {
        if (colorChangeAnimator != null) {
            colorChangeAnimator.cancel();
        }
        aTabLineColorKey = line;
        aActiveTextColorKey = active;
        aUnactiveTextColorKey = unactive;
        aBackgroundColorKey = background;
        selectorColorKey = selector;
        listView.setSelectorDrawableColor(Theme.getColor(selectorColorKey));

        colorChangeAnimator = new AnimatorSet();
        colorChangeAnimator.playTogether(ObjectAnimator.ofFloat(this, COLORS, 0.0f, 1.0f));
        colorChangeAnimator.setDuration(200);
        colorChangeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                tabLineColorKey = aTabLineColorKey;
                backgroundColorKey = aBackgroundColorKey;
                activeTextColorKey = aActiveTextColorKey;
                unactiveTextColorKey = aUnactiveTextColorKey;
                aTabLineColorKey = null;
                aActiveTextColorKey = null;
                aUnactiveTextColorKey = null;
                aBackgroundColorKey = null;
            }
        });
        colorChangeAnimator.start();
    }

    public int getCurrentTabId() {
        return selectedTabId;
    }

    public int getFirstTabId() {
        return positionToId.get(0, 0);
    }

    private void updateTabsWidths() {
        positionToX.clear();
        positionToWidth.clear();
        int xOffset = AndroidUtilities.dp(7);
        for (int a = 0, N = tabs.size(); a < N; a++) {
            int tabWidth = tabs.get(a).getWidth(false);
            positionToWidth.put(a, tabWidth);
            positionToX.put(a, xOffset + additionalTabWidth / 2);
            xOffset += tabWidth + AndroidUtilities.dp(32) + additionalTabWidth;
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean result = super.drawChild(canvas, child, drawingTime);
        if (child == listView) {
            final int height = getMeasuredHeight();
            selectorDrawable.setAlpha((int) (255 * listView.getAlpha()));
            int indicatorX = 0;
            int indicatorWidth = 0;
            if (animatingIndicator || manualScrollingToPosition != -1) {
                int position = layoutManager.findFirstVisibleItemPosition();
                if (position != RecyclerListView.NO_POSITION) {
                    RecyclerListView.ViewHolder holder = listView.findViewHolderForAdapterPosition(position);
                    if (holder != null) {
                        int idx1;
                        int idx2;
                        if (animatingIndicator) {
                            idx1 = previousPosition;
                            idx2 = currentPosition;
                        } else {
                            idx1 = currentPosition;
                            idx2 = manualScrollingToPosition;
                        }
                        int prevX = positionToX.get(idx1);
                        int newX = positionToX.get(idx2);
                        int prevW = positionToWidth.get(idx1);
                        int newW = positionToWidth.get(idx2);
                        if (additionalTabWidth != 0) {
                            indicatorX = (int) (prevX + (newX - prevX) * animatingIndicatorProgress) + AndroidUtilities.dp(16);
                        } else {
                            int x = positionToX.get(position);
                            indicatorX = (int) (prevX + (newX - prevX) * animatingIndicatorProgress) - (x - holder.itemView.getLeft()) + AndroidUtilities.dp(16);
                        }
                        indicatorWidth = (int) (prevW + (newW - prevW) * animatingIndicatorProgress);
                    }
                }
            } else {
                RecyclerListView.ViewHolder holder = listView.findViewHolderForAdapterPosition(currentPosition);
                if (holder != null) {
                    TabView tabView = (TabView) holder.itemView;
                    indicatorWidth = Math.max(AndroidUtilities.dp(40), tabView.tabWidth);
                    indicatorX = (int) (tabView.getX() + (tabView.getMeasuredWidth() - indicatorWidth) / 2);
                }
            }
            if (indicatorWidth != 0) {
                selectorDrawable.setBounds(indicatorX, height - AndroidUtilities.dpr(4), indicatorX + indicatorWidth, height);
                selectorDrawable.draw(canvas);
            }
            if (crossfadeBitmap != null) {
                crossfadePaint.setAlpha((int) (crossfadeAlpha * 255));
                canvas.drawBitmap(crossfadeBitmap, 0, 0, crossfadePaint);
            }
        }
        long newTime = SystemClock.elapsedRealtime();
        long dt = Math.min(17, newTime - lastEditingAnimationTime);
        lastEditingAnimationTime = newTime;
        boolean invalidate = false;
        if (isEditing || editingAnimationProgress != 0.0f) {
            if (editingForwardAnimation) {
                boolean lessZero = editingAnimationProgress <= 0;
                editingAnimationProgress += dt / 120.0f;
                if (!isEditing && lessZero && editingAnimationProgress >= 0) {
                    editingAnimationProgress = 0;
                }
                if (editingAnimationProgress >= 1.0f) {
                    editingAnimationProgress = 1.0f;
                    editingForwardAnimation = false;
                }
            } else {
                boolean greaterZero = editingAnimationProgress >= 0;
                editingAnimationProgress -= dt / 120.0f;
                if (!isEditing && greaterZero && editingAnimationProgress <= 0) {
                    editingAnimationProgress = 0;
                }
                if (editingAnimationProgress <= -1.0f) {
                    editingAnimationProgress = -1.0f;
                    editingForwardAnimation = true;
                }
            }
            invalidate = true;
        }
        if (isEditing) {
            if (editingStartAnimationProgress < 1.0f) {
                editingStartAnimationProgress += dt / 180.0f;
                if (editingStartAnimationProgress > 1.0f) {
                    editingStartAnimationProgress = 1.0f;
                }
                invalidate = true;
            }
        } else if (!isEditing) {
            if (editingStartAnimationProgress > 0.0f) {
                editingStartAnimationProgress -= dt / 180.0f;
                if (editingStartAnimationProgress < 0.0f) {
                    editingStartAnimationProgress = 0.0f;
                }
                invalidate = true;
            }
        }
        if (commitCrossfade) {
            crossfadeAlpha -= dt / 180.0f;
            if (crossfadeAlpha < 0.0f) {
                commitCrossfade = false;
                if (crossfadeBitmap != null) {
                    crossfadeBitmap.recycle();
                    crossfadeBitmap = null;
                }
            }
            invalidate = true;
        }
        if (invalidate) {
            listView.invalidateViews();
            invalidate();
        }
        return result;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!tabs.isEmpty()) {
            int width = MeasureSpec.getSize(widthMeasureSpec) - AndroidUtilities.dp(7) - AndroidUtilities.dp(7);
            Tab firstTab = tabs.get(0);
            if (!NekoConfig.hideAllTab) firstTab.setTitle(LocaleController.getString("FilterAllChats", R.string.FilterAllChats));
            int tabWith = firstTab.getWidth(false);
            if (!NekoConfig.hideAllTab) firstTab.setTitle(allTabsWidth > width ? LocaleController.getString("FilterAllChatsShort", R.string.FilterAllChatsShort) : LocaleController.getString("FilterAllChats", R.string.FilterAllChats));
            int trueTabsWidth = allTabsWidth - tabWith;
            trueTabsWidth += firstTab.getWidth(false);
            int prevWidth = additionalTabWidth;
            additionalTabWidth = trueTabsWidth < width ? (width - trueTabsWidth) / tabs.size() : 0;
            if (prevWidth != additionalTabWidth) {
                ignoreLayout = true;
                adapter.notifyDataSetChanged();
                ignoreLayout = false;
            }
            updateTabsWidths();
            invalidated = false;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void requestLayout() {
        if (ignoreLayout) {
            return;
        }
        super.requestLayout();
    }

    private void scrollToChild(int position) {
        if (tabs.isEmpty() || scrollingToChild == position || position < 0 || position >= tabs.size()) {
            return;
        }
        scrollingToChild = position;
        listView.smoothScrollToPosition(position);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (prevLayoutWidth != r - l) {
            prevLayoutWidth = r - l;
            scrollingToChild = -1;
            if (animatingIndicator) {
                AndroidUtilities.cancelRunOnUIThread(animationRunnable);
                animatingIndicator = false;
                setEnabled(true);
                if (delegate != null) {
                    delegate.onPageScrolled(1.0f);
                }
            }
        }
    }

    public void selectTabWithId(int id, float progress) {
        int position = idToPosition.get(id, -1);
        if (position < 0) {
            return;
        }
        if (progress < 0) {
            progress = 0;
        } else if (progress > 1.0f) {
            progress = 1.0f;
        }

        if (progress > 0) {
            manualScrollingToPosition = position;
            manualScrollingToId = id;
        } else {
            manualScrollingToPosition = -1;
            manualScrollingToId = -1;
        }
        animatingIndicatorProgress = progress;
        listView.invalidateViews();
        invalidate();
        scrollToChild(position);

        if (progress >= 1.0f) {
            manualScrollingToPosition = -1;
            manualScrollingToId = -1;
            currentPosition = position;
            selectedTabId = id;
        }
    }

    private int getChildWidth(TextView child) {
        Layout layout = child.getLayout();
        if (layout != null) {
            int w = (int) Math.ceil(layout.getLineWidth(0)) + AndroidUtilities.dp(2);
            if (child.getCompoundDrawables()[2] != null) {
                w += child.getCompoundDrawables()[2].getIntrinsicWidth() + AndroidUtilities.dp(6);
            }
            return w;
        } else {
            return child.getMeasuredWidth();
        }
    }

    public void onPageScrolled(int position, int first) {
        if (currentPosition == position) {
            return;
        }
        currentPosition = position;
        if (position >= tabs.size()) {
            return;
        }
        if (first == position && position > 1) {
            scrollToChild(position - 1);
        } else {
            scrollToChild(position);
        }
        invalidate();
    }

    public boolean isEditing() {
        return isEditing;
    }

    public void setIsEditing(boolean value) {
        isEditing = value;
        editingForwardAnimation = true;
        listView.invalidateViews();
        invalidate();
        if (!isEditing && orderChanged) {
            MessagesStorage.getInstance(UserConfig.selectedAccount).saveDialogFiltersOrder();
            TLRPC.TL_messages_updateDialogFiltersOrder req = new TLRPC.TL_messages_updateDialogFiltersOrder();
            ArrayList<MessagesController.DialogFilter> filters = MessagesController.getInstance(UserConfig.selectedAccount).dialogFilters;
            for (int a = 0, N = filters.size(); a < N; a++) {
                MessagesController.DialogFilter filter = filters.get(a);
                req.order.add(filters.get(a).id);
            }
            ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (response, error) -> {

            });
            orderChanged = false;
        }
    }

    public void checkTabsCounter() {
        boolean changed = false;
        for (int a = 0, N = tabs.size(); a < N; a++) {
            Tab tab = tabs.get(a);
            if (tab.counter == delegate.getTabCounter(tab.id)) {
                continue;
            }
            changed = true;
            int oldWidth = positionToWidth.get(a);
            int width = tab.getWidth(true);
            if (oldWidth != width || invalidated) {
                invalidated = true;
                requestLayout();
                adapter.notifyDataSetChanged();
                allTabsWidth = 0;
                if (!NekoConfig.hideAllTab) tabs.get(0).setTitle(LocaleController.getString("FilterAllChats", R.string.FilterAllChats));
                for (int b = 0; b < N; b++) {
                    allTabsWidth += tabs.get(b).getWidth(true) + AndroidUtilities.dp(32);
                }
                break;
            }
        }
        if (changed) {
            listView.invalidateViews();
        }
    }

    public void notifyTabCounterChanged(int id) {
        int position = idToPosition.get(id, -1);
        if (position < 0 || position >= tabs.size()) {
            return;
        }
        Tab tab = tabs.get(position);
        if (tab.counter == delegate.getTabCounter(tab.id)) {
            return;
        }
        listView.invalidateViews();
        int oldWidth = positionToWidth.get(position);
        int width = tab.getWidth(true);
        if (oldWidth != width || invalidated) {
            invalidated = true;
            requestLayout();
            adapter.notifyDataSetChanged();
            allTabsWidth = 0;
            if (!NekoConfig.hideAllTab) tabs.get(0).setTitle(LocaleController.getString("FilterAllChats", R.string.FilterAllChats));
            for (int b = 0, N = tabs.size(); b < N; b++) {
                allTabsWidth += tabs.get(b).getWidth(true) + AndroidUtilities.dp(32);
            }
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return tabs.size();
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RecyclerListView.Holder(new TabView(mContext));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            TabView tabView = (TabView) holder.itemView;
            tabView.setTab(tabs.get(position), position);
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        public void swapElements(int fromIndex, int toIndex) {
            int idx1 = fromIndex - 1;
            int idx2 = toIndex - 1;
            int count = tabs.size() - 1;
            if (idx1 < 0 || idx2 < 0 || idx1 >= count || idx2 >= count) {
                return;
            }
            ArrayList<MessagesController.DialogFilter> filters = MessagesController.getInstance(UserConfig.selectedAccount).dialogFilters;
            MessagesController.DialogFilter filter1 = filters.get(idx1);
            MessagesController.DialogFilter filter2 = filters.get(idx2);
            int temp = filter1.order;
            filter1.order = filter2.order;
            filter2.order = temp;
            filters.set(idx1, filter2);
            filters.set(idx2, filter1);

            Tab tab1 = tabs.get(fromIndex);
            Tab tab2 = tabs.get(toIndex);
            temp = tab1.id;
            tab1.id = tab2.id;
            tab2.id = temp;

            delegate.onPageReorder(tab2.id, tab1.id);

            if (currentPosition == fromIndex) {
                currentPosition = toIndex;
                selectedTabId = tab1.id;
            } else if (currentPosition == toIndex) {
                currentPosition = fromIndex;
                selectedTabId = tab2.id;
            }

            if (previousPosition == fromIndex) {
                previousPosition = toIndex;
                previousId = tab1.id;
            } else if (previousPosition == toIndex) {
                previousPosition = fromIndex;
                previousId = tab2.id;
            }

            tabs.set(fromIndex, tab2);
            tabs.set(toIndex, tab1);

            updateTabsWidths();

            orderChanged = true;
            notifyItemMoved(fromIndex, toIndex);
        }
    }

    public class TouchHelperCallback extends ItemTouchHelper.Callback {

        @Override
        public boolean isLongPressDragEnabled() {
            return isEditing;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (!isEditing || viewHolder.getAdapterPosition() == 0) {
                return makeMovementFlags(0, 0);
            }
            return makeMovementFlags(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
            if (source.getAdapterPosition() == 0 || target.getAdapterPosition() == 0) {
                return false;
            }
            adapter.swapElements(source.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                listView.cancelClickRunnables(false);
                viewHolder.itemView.setPressed(true);
                viewHolder.itemView.setBackgroundColor(Theme.getColor(backgroundColorKey));
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setPressed(false);
            viewHolder.itemView.setBackground(null);
        }
    }
}
