/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.allapps;

import android.annotation.SuppressLint;
/// M: Add for OP customization.
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
/// M: Add for OP customization.
import android.graphics.BitmapFactory;
import android.graphics.Point;
/// M: Add for OP customization.
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/// M: Add for OP customization.
import com.android.launcher3.Alarm;
import com.android.launcher3.AppInfo;
import com.android.launcher3.BaseContainerView;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.CellLayout;
import com.android.launcher3.DeleteDropTarget;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.DragSource;
/// M: Add for OP customization. @{
import com.android.launcher3.DragScroller;
import com.android.launcher3.DragController;
import com.android.launcher3.DragView;
/// @}
import com.android.launcher3.DropTarget;
import com.android.launcher3.ExtendedEditText;
import com.android.launcher3.Folder;
/// M: Add for OP customization. @{
import com.android.launcher3.FolderIcon;
import com.android.launcher3.FolderIcon.FolderRingAnimator;
import com.android.launcher3.FolderInfo;
/// @}
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
/// M: Add for OP customization. @{
import com.android.launcher3.LauncherExtPlugin;
import com.android.launcher3.LauncherModelPluginEx;
/// @}
import com.android.launcher3.LauncherTransitionable;
/// M: Add for OP customization.
import com.android.launcher3.OnAlarmListener;
import com.android.launcher3.R;
/// M: Add for OP customization.
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.Utilities;
import com.android.launcher3.Workspace;
/// M: Add for OP customization. @{
import com.android.launcher3.op.AllApps;
import com.android.launcher3.op.LauncherLog;
/// @}
import com.android.launcher3.util.ComponentKey;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;



/**
 * A merge algorithm that merges every section indiscriminately.
 */
final class FullMergeAlgorithm implements AlphabeticalAppsList.MergeAlgorithm {

    @Override
    public boolean continueMerging(AlphabeticalAppsList.SectionInfo section,
           AlphabeticalAppsList.SectionInfo withSection,
           int sectionAppCount, int numAppsPerRow, int mergeCount) {
        // Don't merge the predicted apps
        if (section.firstAppItem.viewType != AllAppsGridAdapter.ICON_VIEW_TYPE
            /// M: Add for OP customization.
            && section.firstAppItem.viewType != AllAppsGridAdapter.FOLDER_VIEW_TYPE) {
            return false;
        }
        // Otherwise, merge every other section
        return true;
    }
}

/**
 * The logic we use to merge multiple sections.  We only merge sections when their final row
 * contains less than a certain number of icons, and stop at a specified max number of merges.
 * In addition, we will try and not merge sections that identify apps from different scripts.
 */
final class SimpleSectionMergeAlgorithm implements AlphabeticalAppsList.MergeAlgorithm {

    private int mMinAppsPerRow;
    private int mMinRowsInMergedSection;
    private int mMaxAllowableMerges;
    private CharsetEncoder mAsciiEncoder;

    public SimpleSectionMergeAlgorithm(int minAppsPerRow,
            int minRowsInMergedSection, int maxNumMerges) {
        mMinAppsPerRow = minAppsPerRow;
        mMinRowsInMergedSection = minRowsInMergedSection;
        mMaxAllowableMerges = maxNumMerges;
        mAsciiEncoder = Charset.forName("US-ASCII").newEncoder();
    }

    @Override
    public boolean continueMerging(AlphabeticalAppsList.SectionInfo section,
           AlphabeticalAppsList.SectionInfo withSection,
           int sectionAppCount, int numAppsPerRow, int mergeCount) {
        // Don't merge the predicted apps
        if (section.firstAppItem.viewType != AllAppsGridAdapter.ICON_VIEW_TYPE
            /// M: Add for OP customization.
            && section.firstAppItem.viewType != AllAppsGridAdapter.FOLDER_VIEW_TYPE) {
            return false;
        }

        // Continue merging if the number of hanging apps on the final row is less than some
        // fixed number (ragged), the merged rows has yet to exceed some minimum row count,
        // and while the number of merged sections is less than some fixed number of merges
        int rows = sectionAppCount / numAppsPerRow;
        int cols = sectionAppCount % numAppsPerRow;

        // Ensure that we do not merge across scripts, currently we only allow for english and
        // native scripts so we can test if both can just be ascii encoded
        boolean isCrossScript = false;
        if (section.firstAppItem != null && withSection.firstAppItem != null) {
            isCrossScript = mAsciiEncoder.canEncode(section.firstAppItem.sectionName) !=
                    mAsciiEncoder.canEncode(withSection.firstAppItem.sectionName);
        }
        return (0 < cols && cols < mMinAppsPerRow) &&
                rows < mMinRowsInMergedSection &&
                mergeCount < mMaxAllowableMerges &&
                !isCrossScript;
    }
}

/**
 * The all apps view container.
 */
public class AllAppsContainerView extends BaseContainerView implements DragSource,
        LauncherTransitionable, View.OnTouchListener, View.OnLongClickListener,
        AllAppsSearchBarController.Callbacks,
        /// M: Add for OP customization.
        DragScroller, DropTarget, DragController.DragListener {

    /// M: Add for OP customization.
    private static final String TAG = "Launcher3.AllAppsContainerView";
    private static final int MIN_ROWS_IN_MERGED_SECTION_PHONE = 3;
    private static final int MAX_NUM_MERGES_PHONE = 2;

    private final Launcher mLauncher;
    /// M: Modify for OP customization. @{
    public final AlphabeticalAppsList mApps;
    public final AllAppsGridAdapter mAdapter;
    /// @}
    private final RecyclerView.LayoutManager mLayoutManager;
    private final RecyclerView.ItemDecoration mItemDecoration;

    // The computed bounds of the container
    private final Rect mContentBounds = new Rect();

    private AllAppsRecyclerView mAppsRecyclerView;
    private AllAppsSearchBarController mSearchBarController;

    private View mSearchContainer;
    private ExtendedEditText mSearchInput;
    private HeaderElevationController mElevationController;

    private SpannableStringBuilder mSearchQueryBuilder = null;

    private int mSectionNamesMargin;
    private int mNumAppsPerRow;
    private int mNumPredictedAppsPerRow;
    private int mRecyclerViewTopBottomPadding;
    // This coordinate is relative to this container view
    private final Point mBoundsCheckLastTouchDownPos = new Point(-1, -1);
    // This coordinate is relative to its parent
    private final Point mIconLastTouchPos = new Point();

    /// M: Add for OP customization. @{
    private boolean mSupportEditApps = false;
    public boolean mIsInEditMode = false;
    private DragController mDragController;
    private AppInfo mDraggingPaddingAppInfo = null;
    private int[] mCellSize = new int[2];
    private int[] mLastDragPos = new int[2];
    private int mScrollZone = 0;
    private boolean mDraggingInSrollZone = false;
    private float mMaxDistanceForFolderCreation;
    private int mDragMode = Workspace.DRAG_MODE_NONE;
    private int[] mTargetCell = new int[2];
    private int mMaxAppsCountInOnePage = 0;
    /// @}

    public AllAppsContainerView(Context context) {
        this(context, null);
    }

    public AllAppsContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Resources res = context.getResources();

        mLauncher = (Launcher) context;
        mSectionNamesMargin = res.getDimensionPixelSize(R.dimen.all_apps_grid_view_start_margin);
        mApps = new AlphabeticalAppsList(context);
        mAdapter = new AllAppsGridAdapter(mLauncher, mApps, this, mLauncher, this);
        mApps.setAdapter(mAdapter);
        mLayoutManager = mAdapter.getLayoutManager();
        mItemDecoration = mAdapter.getItemDecoration();
        mRecyclerViewTopBottomPadding =
                res.getDimensionPixelSize(R.dimen.all_apps_list_top_bottom_padding);

        mSearchQueryBuilder = new SpannableStringBuilder();
        Selection.setSelection(mSearchQueryBuilder, 0);

        /// M: Add for OP customization. @{
        mSupportEditApps = LauncherExtPlugin.getInstance().supportEditAndHideApps();
        mDragController = mLauncher.getDragController();
        mScrollZone = mLauncher.getResources()
                .getDimensionPixelSize(R.dimen.scroll_zone);
        DeviceProfile grid = mLauncher.getDeviceProfile();
        mMaxDistanceForFolderCreation = (0.55f * grid.iconSizePx);
        /// @}
    }

    /**
     * Sets the current set of predicted apps.
     */
    public void setPredictedApps(List<ComponentKey> apps) {
        mApps.setPredictedApps(apps);
    }

    /**
     * Sets the current set of apps.
     */
    public void setApps(List<AppInfo> apps) {
        mApps.setApps(apps);
    }

    /**
     * Adds new apps to the list.
     */
    public void addApps(List<AppInfo> apps) {
        mApps.addApps(apps);
    }

    /**
     * Updates existing apps in the list
     */
    public void updateApps(List<AppInfo> apps) {
        mApps.updateApps(apps);
    }

    /**
     * Removes some apps from the list.
     */
    public void removeApps(List<AppInfo> apps) {
        mApps.removeApps(apps);
    }

    /**
     * Sets the search bar that shows above the a-z list.
     */
    public void setSearchBarController(AllAppsSearchBarController searchController) {
        if (mSearchBarController != null) {
            throw new RuntimeException("Expected search bar controller to only be set once");
        }
        mSearchBarController = searchController;
        mSearchBarController.initialize(mApps, mSearchInput, mLauncher, this);
        mAdapter.setSearchController(mSearchBarController);

        updateBackgroundAndPaddings();
    }

    /**
     * Scrolls this list view to the top.
     */
    public void scrollToTop() {
        mAppsRecyclerView.scrollToTop();
    }

    /**
     * Focuses the search field and begins an app search.
     */
    public void startAppsSearch() {
        if (mSearchBarController != null) {
            mSearchBarController.focusSearchField();
        }
    }

    /**
     * Resets the state of AllApps.
     */
    public void reset() {
        // Reset the search bar and base recycler view after transitioning home
        mSearchBarController.reset();
        mAppsRecyclerView.reset();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // This is a focus listener that proxies focus from a view into the list view.  This is to
        // work around the search box from getting first focus and showing the cursor.
        getContentView().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mAppsRecyclerView.requestFocus();
                }
            }
        });

        mSearchContainer = findViewById(R.id.search_container);
        mSearchInput = (ExtendedEditText) findViewById(R.id.search_box_input);
        mElevationController = Utilities.ATLEAST_LOLLIPOP
                ? new HeaderElevationController.ControllerVL(mSearchContainer)
                : new HeaderElevationController.ControllerV16(mSearchContainer);

        // Load the all apps recycler view
        mAppsRecyclerView = (AllAppsRecyclerView) findViewById(R.id.apps_list_view);
        mAppsRecyclerView.setApps(mApps);
        mAppsRecyclerView.setLayoutManager(mLayoutManager);
        mAppsRecyclerView.setAdapter(mAdapter);
        mAppsRecyclerView.setHasFixedSize(true);
        mAppsRecyclerView.addOnScrollListener(mElevationController);
        mAppsRecyclerView.setElevationController(mElevationController);

        /// M: Add for OP customization. @{
        DeviceProfile grid = mLauncher.getDeviceProfile();
        mAppsRecyclerView.setDeviceProfile(grid);
        /// @}

        if (mItemDecoration != null) {
            mAppsRecyclerView.addItemDecoration(mItemDecoration);
        }

        // Precalculate the prediction icon and normal icon sizes
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(
                getResources().getDisplayMetrics().widthPixels, MeasureSpec.AT_MOST);
        final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                getResources().getDisplayMetrics().heightPixels, MeasureSpec.AT_MOST);

        BubbleTextView icon = (BubbleTextView) layoutInflater.inflate(
                R.layout.all_apps_icon, this, false);
        icon.applyDummyInfo();
        icon.measure(widthMeasureSpec, heightMeasureSpec);
        BubbleTextView predIcon = (BubbleTextView) layoutInflater.inflate(
                R.layout.all_apps_prediction_bar_icon, this, false);
        predIcon.applyDummyInfo();
        predIcon.measure(widthMeasureSpec, heightMeasureSpec);
        mAppsRecyclerView.setPremeasuredIconHeights(predIcon.getMeasuredHeight(),
                icon.getMeasuredHeight());

        updateBackgroundAndPaddings();
    }

    @Override
    public void onBoundsChanged(Rect newBounds) {
        mLauncher.updateOverlayBounds(newBounds);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mContentBounds.set(mContentPadding.left, mContentPadding.top,
                MeasureSpec.getSize(widthMeasureSpec) - mContentPadding.right,
                MeasureSpec.getSize(heightMeasureSpec) - mContentPadding.bottom);

        // Update the number of items in the grid before we measure the view
        // TODO: mSectionNamesMargin is currently 0, but also account for it,
        // if it's enabled in the future.
        int availableWidth = (!mContentBounds.isEmpty() ? mContentBounds.width() :
                MeasureSpec.getSize(widthMeasureSpec))
                    - 2 * mAppsRecyclerView.getMaxScrollbarWidth();
        DeviceProfile grid = mLauncher.getDeviceProfile();
        grid.updateAppsViewNumCols(getResources(), availableWidth);
        if (mNumAppsPerRow != grid.allAppsNumCols ||
                mNumPredictedAppsPerRow != grid.allAppsNumPredictiveCols) {
            mNumAppsPerRow = grid.allAppsNumCols;
            mNumPredictedAppsPerRow = grid.allAppsNumPredictiveCols;

            // If there is a start margin to draw section names, determine how we are going to merge
            // app sections
            boolean mergeSectionsFully = mSectionNamesMargin == 0 || !grid.isPhone;
            AlphabeticalAppsList.MergeAlgorithm mergeAlgorithm = mergeSectionsFully ?
                    new FullMergeAlgorithm() :
                    new SimpleSectionMergeAlgorithm((int) Math.ceil(mNumAppsPerRow / 2f),
                            MIN_ROWS_IN_MERGED_SECTION_PHONE, MAX_NUM_MERGES_PHONE);

            mAppsRecyclerView.setNumAppsPerRow(grid, mNumAppsPerRow);
            mAdapter.setNumAppsPerRow(mNumAppsPerRow);
            mApps.setNumAppsPerRow(mNumAppsPerRow, mNumPredictedAppsPerRow, mergeAlgorithm);

            if (mNumAppsPerRow > 0) {
                int iconSize = availableWidth / mNumAppsPerRow;
                int iconSpacing = (iconSize - grid.allAppsIconSizePx) / 2;
                mSearchInput.setPaddingRelative(iconSpacing, 0, iconSpacing, 0);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Update the background and padding of the Apps view and children.  Instead of insetting the
     * container view, we inset the background and padding of the recycler view to allow for the
     * recycler view to handle touch events (for fast scrolling) all the way to the edge.
     */
    @Override
    protected void onUpdateBgPadding(Rect padding, Rect bgPadding) {
        mAppsRecyclerView.updateBackgroundPadding(bgPadding);
        mAdapter.updateBackgroundPadding(bgPadding);
        mElevationController.updateBackgroundPadding(bgPadding);

        // Pad the recycler view by the background padding plus the start margin (for the section
        // names)
        int maxScrollBarWidth = mAppsRecyclerView.getMaxScrollbarWidth();
        int startInset = Math.max(mSectionNamesMargin, maxScrollBarWidth);
        int topBottomPadding = mRecyclerViewTopBottomPadding;
        if (Utilities.isRtl(getResources())) {
            mAppsRecyclerView.setPadding(padding.left + maxScrollBarWidth,
                    topBottomPadding, padding.right + startInset, topBottomPadding);
        } else {
            mAppsRecyclerView.setPadding(padding.left + startInset, topBottomPadding,
                    padding.right + maxScrollBarWidth, topBottomPadding);
        }

        MarginLayoutParams lp = (MarginLayoutParams) mSearchContainer.getLayoutParams();
        lp.leftMargin = padding.left;
        lp.rightMargin = padding.right;
        mSearchContainer.setLayoutParams(lp);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Determine if the key event was actual text, if so, focus the search bar and then dispatch
        // the key normally so that it can process this key event
        if (!mSearchBarController.isSearchFieldFocused() &&
                event.getAction() == KeyEvent.ACTION_DOWN) {
            final int unicodeChar = event.getUnicodeChar();
            final boolean isKeyNotWhitespace = unicodeChar > 0 &&
                    !Character.isWhitespace(unicodeChar) && !Character.isSpaceChar(unicodeChar);
            if (isKeyNotWhitespace) {
                boolean gotKey = TextKeyListener.getInstance().onKeyDown(this, mSearchQueryBuilder,
                        event.getKeyCode(), event);
                if (gotKey && mSearchQueryBuilder.length() > 0) {
                    mSearchBarController.focusSearchField();
                }
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return handleTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return handleTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                mIconLastTouchPos.set((int) ev.getX(), (int) ev.getY());
                break;
        }
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        // Return early if this is not initiated from a touch
        if (!v.isInTouchMode()) return false;
        // When we have exited all apps or are in transition, disregard long clicks
        if (!mLauncher.isAppsViewVisible() ||
                mLauncher.getWorkspace().isSwitchingState()) return false;
        // Return if global dragging is not enabled
        if (!mLauncher.isDraggingEnabled()) return false;

        // Start the drag
        mLauncher.getWorkspace().beginDragShared(v, mIconLastTouchPos, this, false);
        // Enter spring loaded mode
        /// M: ALPS02295402, modify for RtoL layout. @{
        // In RtoL, enter spring loaded mode should later, or else, it has time sequence issue.
        if (!(mSupportEditApps && mIsInEditMode)) {
            if (Utilities.isRtl(getResources())) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLauncher.enterSpringLoadedDragMode();
                    }
                }, 100);
            } else {
                mLauncher.enterSpringLoadedDragMode();
            }
        }
        /// @}

        return false;
    }

    @Override
    public boolean supportsFlingToDelete() {
        return true;
    }

    @Override
    public boolean supportsAppInfoDropTarget() {
        return true;
    }

    @Override
    public boolean supportsDeleteDropTarget() {
        return false;
    }

    @Override
    public float getIntrinsicIconScaleFactor() {
        DeviceProfile grid = mLauncher.getDeviceProfile();
        return (float) grid.allAppsIconSizePx / grid.iconSizePx;
    }

    @Override
    public void onFlingToDeleteCompleted() {
        // We just dismiss the drag when we fling, so cleanup here
        mLauncher.exitSpringLoadedDragModeDelayed(true,
                Launcher.EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
        mLauncher.unlockScreenOrientation(false);
    }

    @Override
    public void onDropCompleted(View target, DropTarget.DragObject d, boolean isFlingToDelete,
            boolean success) {
        if (isFlingToDelete || !success || (target != mLauncher.getWorkspace() &&
                !(target instanceof DeleteDropTarget) && !(target instanceof Folder))) {
            // Exit spring loaded mode if we have not successfully dropped or have not handled the
            // drop in Workspace
            /// M: ALPS02295402, modify this for RtoL layout. @{
            // In RtoL, the time should be longer, or else, it has time sequence issue.
            if (Utilities.isRtl(getResources())) {
                mLauncher.exitSpringLoadedDragModeDelayed(true,
                    Launcher.EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT_RTOL, null);
            } else {
                mLauncher.exitSpringLoadedDragModeDelayed(true,
                    Launcher.EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
            }
            /// @}
        }
        mLauncher.unlockScreenOrientation(false);

        // Display an error message if the drag failed due to there not being enough space on the
        // target layout we were dropping on.
        if (!success) {
            boolean showOutOfSpaceMessage = false;
            if (target instanceof Workspace) {
                int currentScreen = mLauncher.getCurrentWorkspaceScreen();
                Workspace workspace = (Workspace) target;
                CellLayout layout = (CellLayout) workspace.getChildAt(currentScreen);
                ItemInfo itemInfo = (ItemInfo) d.dragInfo;
                if (layout != null) {
                    showOutOfSpaceMessage =
                            !layout.findCellForSpan(null, itemInfo.spanX, itemInfo.spanY);
                }
            }
            if (showOutOfSpaceMessage) {
                mLauncher.showOutOfSpaceMessage(false);
            }

            d.deferDragViewCleanupPostAnimation = false;
        }
    }

    @Override
    public void onLauncherTransitionPrepare(Launcher l, boolean animated, boolean toWorkspace) {
        // Do nothing
    }

    @Override
    public void onLauncherTransitionStart(Launcher l, boolean animated, boolean toWorkspace) {
        // Do nothing
    }

    @Override
    public void onLauncherTransitionStep(Launcher l, float t) {
        // Do nothing
    }

    @Override
    public void onLauncherTransitionEnd(Launcher l, boolean animated, boolean toWorkspace) {
        if (toWorkspace) {
            reset();
        /// M: Add for OP customization. @{
        } else {
            if (mIsInEditMode) {
                enterEditMode();
                mAdapter.notifyDataSetChanged();
            }
        /// @}
        }
    }

    /**
     * Handles the touch events to dismiss all apps when clicking outside the bounds of the
     * recycler view.
     */
    private boolean handleTouchEvent(MotionEvent ev) {
        DeviceProfile grid = mLauncher.getDeviceProfile();
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mContentBounds.isEmpty()) {
                    // Outset the fixed bounds and check if the touch is outside all apps
                    Rect tmpRect = new Rect(mContentBounds);
                    tmpRect.inset(-grid.allAppsIconSizePx / 2, 0);
                    if (ev.getX() < tmpRect.left || ev.getX() > tmpRect.right) {
                        mBoundsCheckLastTouchDownPos.set(x, y);
                        return true;
                    }
                } else {
                    // Check if the touch is outside all apps
                    if (ev.getX() < getPaddingLeft() ||
                            ev.getX() > (getWidth() - getPaddingRight())) {
                        mBoundsCheckLastTouchDownPos.set(x, y);
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mBoundsCheckLastTouchDownPos.x > -1) {
                    ViewConfiguration viewConfig = ViewConfiguration.get(getContext());
                    float dx = ev.getX() - mBoundsCheckLastTouchDownPos.x;
                    float dy = ev.getY() - mBoundsCheckLastTouchDownPos.y;
                    float distance = (float) Math.hypot(dx, dy);
                    if (distance < viewConfig.getScaledTouchSlop()) {
                        // The background was clicked, so just go home
                        Launcher launcher = (Launcher) getContext();
                        launcher.showWorkspace(true);
                        return true;
                    }
                }
                // Fall through
            case MotionEvent.ACTION_CANCEL:
                mBoundsCheckLastTouchDownPos.set(-1, -1);
                break;
        }
        return false;
    }

    @Override
    public void onSearchResult(String query, ArrayList<ComponentKey> apps) {
        if (apps != null) {
            if (mApps.setOrderedFilter(apps)) {
                mAppsRecyclerView.onSearchResultsChanged();
            }
            mAdapter.setLastSearchQuery(query);
        }
    }

    @Override
    public void clearSearchResult() {
        if (mApps.setOrderedFilter(null)) {
            mAppsRecyclerView.onSearchResultsChanged();
        }

        // Clear the search query
        mSearchQueryBuilder.clear();
        mSearchQueryBuilder.clearSpans();
        Selection.setSelection(mSearchQueryBuilder, 0);
    }

    /// M: Add for OP customization. @{

    // Enter edit mode, allow user to rearrange application icons.
    public void enterEditMode() {
        LauncherLog.d(TAG, "enterEditMode:");

        // Make apps customized pane can receive drag and drop event.
        mDragController.setDragScoller(this);
        mDragController.setMoveTarget(this);
        mDragController.addDropTarget(this);
        mDragController.addDragListener(this);

        // Get the cell size when needed.
        if (mCellSize[0] == 0 || mCellSize[1] == 0) {
            mCellSize = mAppsRecyclerView.getCellSize();
        }

        // Set the max app count in one page.
        if (mMaxAppsCountInOnePage == 0 && mAppsRecyclerView.getChildCount() > 0) {
            LauncherLog.d(TAG, "enterEditMode: mAppsRecyclerView.getHeight()="
                + mAppsRecyclerView.getHeight() + ", mCellSize[1]=" + mCellSize[1]
                + ", mNumAppsPerRow=" + mNumAppsPerRow);
            int num = mAppsRecyclerView.getHeight() / mCellSize[1];
            mMaxAppsCountInOnePage = mNumAppsPerRow * num;
            mApps.setMaxAppNumInPage(mMaxAppsCountInOnePage);
        }

        // Reset the target page to 0.
        mAppsRecyclerView.mTargetPage = 0;

        mApps.refreshView(true);

        LauncherLog.d(TAG, "enterEditMode: mMaxAppsCountInOnePage="
            + mMaxAppsCountInOnePage);
    }

    // Exit edit mode.
    public void exitEditMode() {
        mIsInEditMode = false;

        // Make apps customized pane can't receive drag and drop event when exit edit mode.
        mDragController.setDragScoller(mLauncher.getWorkspace());
        mDragController.setMoveTarget(mLauncher.getWorkspace());
        mDragController.removeDropTarget(this);
        mDragController.removeDragListener(this);

        // Close folder when needed.
        mLauncher.closeFolder();

        mApps.refreshView(true);

        LauncherLog.d(TAG, "exitEditMode:");
    }

    public void setEditModeFlag() {
        mIsInEditMode = true;
    }

    public void clearEditModeFlag() {
        mIsInEditMode = false;
    }

    @Override
    public void scrollLeft() {
    }

    @Override
    public void scrollRight() {
    }

    @Override
    public boolean onEnterScrollArea(int x, int y, int direction) {
        return false;
    }

    @Override
    public boolean onExitScrollArea() {
        return false;
    }

    @Override
    public boolean isDropEnabled() {
        return mIsInEditMode;
    }

    @Override
    public void onDrop(DragObject d) {
        d.deferDragViewCleanupPostAnimation = false;

        ItemInfo dragInfo = (ItemInfo) (d.dragInfo);

        // Drag a app from folder to app list.
        if (dragInfo instanceof ShortcutInfo) {
            AppInfo info = ((ShortcutInfo) dragInfo).makeAppInfo();
            dragInfo = info;
            dragInfo.container = AppInfo.NO_ID;
            dragInfo.screenId = 0;
            // Add it back to app list.
            mApps.mItemsBackup.add(info);
        }

        // Update the origin page info to data base.
        if (mDragMode == Workspace.DRAG_MODE_REORDER) {
            int index = mApps.mItems.indexOf(mDraggingPaddingAppInfo);
            LauncherLog.d(TAG, "onDrop, index=" + index
                + ", mDraggingPaddingAppInfo:" + mDraggingPaddingAppInfo);
            dragInfo.screenId = mDraggingPaddingAppInfo.screenId;
            dragInfo.mPos = mDraggingPaddingAppInfo.mPos;
            dragInfo.cellX = mDraggingPaddingAppInfo.cellX;

            if (index < 0) {
                if (dragInfo instanceof AppInfo) {
                    mApps.mItems.add((AppInfo) dragInfo);
                } else if (dragInfo instanceof FolderInfo) {
                    mApps.mItems.add((FolderInfo) dragInfo);
                }
            } else {
                if (dragInfo instanceof AppInfo) {
                    mApps.mItems.set(index, (AppInfo) dragInfo);
                } else if (dragInfo instanceof FolderInfo) {
                    mApps.mItems.set(index, (FolderInfo) dragInfo);
                }
            }
        } else if (mDragMode == Workspace.DRAG_MODE_CREATE_FOLDER) {
            createUserFolderIfNecessary(d.dragView, (AppInfo) dragInfo);

        } else if (mDragMode == Workspace.DRAG_MODE_ADD_TO_FOLDER) {
            int pageId = mAppsRecyclerView.mTargetPage <= 0
                            ? 0 : mAppsRecyclerView.mTargetPage;
            int pos = mAppsRecyclerView.mTargetPage * mMaxAppsCountInOnePage
                        + mTargetCell[1] * mNumAppsPerRow + mTargetCell[0];

            FolderInfo folderInfo = null;
            if (pos < mApps.mItems.size()) {
                ItemInfo itemInfo = mApps.mItems.get(pos);
                folderInfo = (itemInfo instanceof FolderInfo) ? (FolderInfo) itemInfo : null;
            }

            dragInfo.cellX = mTargetCell[0];
            dragInfo.cellY = mTargetCell[1];
            dragInfo.screenId = pageId;
            dragInfo.mPos = folderInfo.mPos;
            dragInfo.container = folderInfo.id;
            ShortcutInfo dragTag = ((AppInfo) dragInfo).makeShortcut();
            folderInfo.add(dragTag);

            mApps.mItems.remove(dragInfo);
            mApps.mItemsBackup.remove(dragInfo);

            //update the origin page info to data base.
            LauncherModelPluginEx.moveAllAppsItemInDatabase(mLauncher, dragInfo,
                folderInfo.id, dragInfo.screenId, mTargetCell[0], mTargetCell[1]);
        } else if (mDragMode == Workspace.DRAG_MODE_NONE) {
            LauncherLog.d(TAG, "onDrop, dragMode is none");
            dragInfo.screenId = 0;
            dragInfo.mPos = mApps.mItemsBackup.size();
            dragInfo.cellX = dragInfo.mPos;
            dragInfo.cellY = 0;

            if (dragInfo instanceof AppInfo) {
                mApps.mItems.add((AppInfo) dragInfo);
            } else if (dragInfo instanceof FolderInfo) {
                mApps.mItems.add((FolderInfo) dragInfo);
            }
        }

        mApps.mItems.remove(mDraggingPaddingAppInfo);
        mApps.updateAllItemOrderByPos(mApps.mItems);

        int tempCell[] = {-1, -1};
        mAppsRecyclerView.setCrrentDragCell(tempCell);

        mApps.refreshView(true);

        for (ItemInfo info : mApps.mItems) {
            mApps.updateItemInDatabase(info);
        }

        setDragMode(Workspace.DRAG_MODE_NONE);
    }

    @Override
    public void onDragEnter(DragObject d) {
        if (mSupportEditApps && mIsInEditMode) {
            // If it is in search mode, clear it.
            if (mSearchBarController.isSearchFieldFocused()) {
                mSearchBarController.reset();
            }

            ItemInfo info = (ItemInfo) (d.dragInfo);

            LauncherLog.d(TAG, "onDragEnter: targetPage="
                + mAppsRecyclerView.mTargetPage
                + ", mMaxAppsCountInOnePage=" + mMaxAppsCountInOnePage);

            // If no dragging padding app info here, create it first.
            if (mDraggingPaddingAppInfo == null) {
                LauncherLog.d(TAG, "onDragEnter: mDraggingPaddingAppInfo is null");
                if (info instanceof FolderInfo) {
                    AppInfo tempinfo = ((FolderInfo) info).contents.get(0).makeAppInfo();
                    mDraggingPaddingAppInfo = new AppInfo(tempinfo);
                } else if (info instanceof ShortcutInfo) {
                    AppInfo tempinfo = ((ShortcutInfo) info).makeAppInfo();
                    mDraggingPaddingAppInfo = new AppInfo(tempinfo);
                } else {
                    mDraggingPaddingAppInfo = new AppInfo((AppInfo) info);
                }
                mDraggingPaddingAppInfo.container = AppInfo.NO_ID;
                mDraggingPaddingAppInfo.isForPadding = AppInfo.DRAGGING_PADDING_APP;
                mDraggingPaddingAppInfo.isVisible = true;
                mDraggingPaddingAppInfo.title = "DraggingPadding";
                ComponentName cn = new ComponentName("com.op09.launcher",
                    "com.op09.draggingPadding");
                mDraggingPaddingAppInfo.intent.setComponent(cn);
                mDraggingPaddingAppInfo.componentName = cn;
                mDraggingPaddingAppInfo.iconBitmap = BitmapFactory.decodeResource(
                    mLauncher.getResources(), R.drawable.ic_launcher_edit_holo);
            }

            mDraggingPaddingAppInfo.mPos = info.mPos;
            mDraggingPaddingAppInfo.screenId =  info.screenId;
            mDraggingPaddingAppInfo.cellX = info.cellX;
            mDraggingPaddingAppInfo.cellY = info.cellY;

            if (info instanceof AppInfo) {
                int index = mApps.mItems.indexOf(info);
                if (index < 0) {
                    // Add dragging padding app at the end of app list.
                    mApps.mItems.remove(mDraggingPaddingAppInfo);
                    mApps.mItems.add(mDraggingPaddingAppInfo);
                } else {
                    // Instead dragging app with the dragging padding app in the app list.
                    mApps.mItems.set(index, mDraggingPaddingAppInfo);
                }
                LauncherLog.d(TAG, "onDragEnter_App, app=" + ((AppInfo) info));
                LauncherLog.d(TAG, "onDragEnter_App, mDraggingPaddingAppInfo="
                    + mDraggingPaddingAppInfo + ", index=" + index);
            } else if (info instanceof FolderInfo) {
                int index = mApps.mItems.indexOf(info);
                // Instead dragging folder with the dragging padding app in the app list.
                mApps.mItems.set(index, mDraggingPaddingAppInfo);
                LauncherLog.d(TAG, "onDragEnter_folder, folder=" + ((FolderInfo) info));
                LauncherLog.d(TAG, "onDragEnter_folder, mDraggingPaddingAppInfo="
                    + mDraggingPaddingAppInfo + ", index=" + index);
            } else if (info instanceof ShortcutInfo) {
                // Drag a app from folder to app list.
                boolean find = false;
                for (ItemInfo itemInfo : mApps.mItems) {
                    if (itemInfo instanceof FolderInfo
                        && (itemInfo.id == info.container)) {
                        FolderInfo folderInfo = (FolderInfo) itemInfo;
                        folderInfo.remove((ShortcutInfo) info);
                        break;
                    }
                }
                LauncherLog.d(TAG, "onDragEnter_app in folder, app=" + ((ShortcutInfo) info));
                LauncherLog.d(TAG, "onDragEnter_app in folder, mDraggingPaddingAppInfo="
                    + mDraggingPaddingAppInfo);
            }

            mApps.refreshView(false);

            mLastDragPos[0] = -1;
            mLastDragPos[1] = -1;

            setDragMode(Workspace.DRAG_MODE_REORDER);
            mDraggingInSrollZone = false;
        }
    }

    @Override
    public void onDragOver(DragObject d) {
        ItemInfo item = (ItemInfo) d.dragInfo;

        // Drag a app from folder to app list.
        if (item instanceof ShortcutInfo) {
            AppInfo info = ((ShortcutInfo) item).makeAppInfo();
            item = info;
        }

        float[] r = d.getVisualCenter(null);

        // Map the view center location to recycler view.
        int[] location = new int[2];
        mAppsRecyclerView.getLocationInWindow(location);
        r[1] = r[1] - location[1];

        AllAppsRecyclerView.ScrollPositionState scrollPosState =
            new AllAppsRecyclerView.ScrollPositionState();

        mAppsRecyclerView.getCurScrollState(scrollPosState);

        // Check srcoll area.
        if (r[1] < mScrollZone && !mDraggingInSrollZone) {
            int paddingIndex = mApps.mItems.indexOf(mDraggingPaddingAppInfo);
            if (paddingIndex >= 0 && paddingIndex < (mApps.mItems.size() - 1)
                && paddingIndex < (mAppsRecyclerView.mTargetPage * mMaxAppsCountInOnePage)) {
                mApps.mItems.remove(mDraggingPaddingAppInfo);
                mApps.mItems.add(mDraggingPaddingAppInfo);
                mApps.updateAllItemOrderByPos(mApps.mItems);
                mApps.refreshView(false);
            }

            if (scrollPosState.rowIndex < 0 || scrollPosState.rowHeight <= 0) {
                LauncherLog.d(TAG, "onDragOver scroll up: invalid scrollPosState"
                    + ", rowIndex=" + scrollPosState.rowIndex
                    + ", rowHeight=" + scrollPosState.rowHeight);
                return;
            }

            mAppsRecyclerView.mTargetPage = mAppsRecyclerView.mTargetPage <= 0
                ? 0 : mAppsRecyclerView.mTargetPage - 1;
            LauncherLog.d(TAG, "onDragOver scroll up: targetPage="
                + mAppsRecyclerView.mTargetPage);

            mAppsRecyclerView.smoothSnapToPosition(
                mAppsRecyclerView.mTargetPage * mMaxAppsCountInOnePage + 1,
                scrollPosState);
            mDraggingInSrollZone = true;
            setDragMode(Workspace.DRAG_MODE_NONE);
            return;
        } else if ((r[1] > mAppsRecyclerView.getHeight() - mScrollZone)
                && !mDraggingInSrollZone) {
            int paddingIndex = mApps.mItems.indexOf(mDraggingPaddingAppInfo);
            if (paddingIndex >= 0 && paddingIndex < (mApps.mItems.size() - 1)
                && paddingIndex < (mAppsRecyclerView.mTargetPage * mMaxAppsCountInOnePage)) {
                mApps.mItems.remove(mDraggingPaddingAppInfo);
                mApps.mItems.add(mDraggingPaddingAppInfo);
                mApps.updateAllItemOrderByPos(mApps.mItems);
                mApps.refreshView(false);
            }

            if (scrollPosState.rowIndex < 0 || scrollPosState.rowHeight <= 0) {
                LauncherLog.d(TAG, "onDragOver scroll down: invalid scrollPosState"
                    + ", rowIndex=" + scrollPosState.rowIndex
                    + ", rowHeight=" + scrollPosState.rowHeight);
                return;
            }
            int pageNum = (mApps.mItems.size() + mMaxAppsCountInOnePage - 1)
                    / mMaxAppsCountInOnePage;
            mAppsRecyclerView.mTargetPage = (mAppsRecyclerView.mTargetPage >= pageNum - 1)
                ? (pageNum - 1) : mAppsRecyclerView.mTargetPage + 1;
            LauncherLog.d(TAG, "onDragOver scroll down: targetPage="
                + mAppsRecyclerView.mTargetPage);

            mAppsRecyclerView.smoothSnapToPosition(
                mAppsRecyclerView.mTargetPage * mMaxAppsCountInOnePage + 1,
                scrollPosState);
            mDraggingInSrollZone = true;
            setDragMode(Workspace.DRAG_MODE_NONE);
            return;
        } else {
            if ((r[1] >= mScrollZone) &&
                (r[1] <= mAppsRecyclerView.getHeight() - mScrollZone)) {
                mDraggingInSrollZone = false;
            } else {
                return;
            }
        }

        r[0] = r[0] < 0 ? 0 : r[0];
        r[1] = r[1] < 0 ? 0 : r[1];

        int cellX = (int) (r[0] / mCellSize[0]);
        int cellY = (int) (r[1] / mCellSize[1]);

        /*
         * If there is no last drag position, such as dragging an app
         * or dragging an app out of folder, no need to update items
         * and refresh view, so save the drag position and return directly.
         */
        if (mLastDragPos[0] == -1 && mLastDragPos[1] == -1) {
            mLastDragPos[0] = cellX;
            mLastDragPos[1] = cellY;
            return;
        }

        mApps.mItems.remove(mDraggingPaddingAppInfo);

        mTargetCell[0] = cellX;
        mTargetCell[1] = cellY;
        float dis = getDistanceFromCell(r[0], r[1], mTargetCell);
        int pos = mAppsRecyclerView.mTargetPage * mMaxAppsCountInOnePage
                + cellY * mNumAppsPerRow + cellX;

        ItemInfo tempInfo = null;
        if (pos < mApps.mItems.size()) {
            tempInfo = mApps.mItems.get(pos);
        }

        if ((item instanceof AppInfo) && (pos < mApps.mItems.size())
            && willCreateUserFolder(tempInfo, dis)) {
            // Set the target cell to draw the folder ring
            mAppsRecyclerView.setCrrentDragCell(mTargetCell);
            mApps.refreshView(false);

            mLastDragPos[0] = -1;
            mLastDragPos[1] = -1;

            if (tempInfo instanceof AppInfo) {
                LauncherLog.d(TAG, "onDragOver create folder: tempInfo="
                    + ((AppInfo) tempInfo));
                setDragMode(Workspace.DRAG_MODE_CREATE_FOLDER);
                if (!mFolderCreationAlarm.alarmPending()) {
                    mFolderCreationAlarm.setOnAlarmListener(new
                            FolderCreationAlarmListener(mAppsRecyclerView,
                            mTargetCell[0], mTargetCell[1]));
                    mFolderCreationAlarm.setAlarm(Workspace.FOLDER_CREATION_TIMEOUT);
                    return;
                }

            } else if (tempInfo instanceof FolderInfo) {
                LauncherLog.d(TAG, "onDragOver add to folder: tempInfo="
                    + ((FolderInfo) tempInfo));
                if (willAddToExistingUserFolder(d.dragInfo, mAppsRecyclerView,
                        mTargetCell, dis, (FolderInfo) tempInfo)) {

                    View dropOverView = getViewByItemInfo(mAppsRecyclerView, tempInfo);

                    mDragOverFolderIcon = ((FolderIcon) dropOverView);
                    mDragOverFolderIcon.onDragEnter(item);
                }
                setDragMode(Workspace.DRAG_MODE_ADD_TO_FOLDER);
                if (!mAddToFolderAlarm.alarmPending()) {
                    mAddToFolderAlarm.setOnAlarmListener(new
                            AddToFolderAlarmListener(mAppsRecyclerView,
                            mTargetCell[0], mTargetCell[1]));
                    mAddToFolderAlarm.setAlarm(Workspace.FOLDER_CREATION_TIMEOUT);
                }
            }
            return;
        }
        setDragMode(Workspace.DRAG_MODE_REORDER);

        int tempCell[] = {-1, -1};
        mAppsRecyclerView.setCrrentDragCell(tempCell);

        pos = mAppsRecyclerView.mTargetPage * mMaxAppsCountInOnePage
                + cellY * mNumAppsPerRow + cellX;
        if (pos >= mApps.mItems.size()) {
            mApps.mItems.add(mDraggingPaddingAppInfo);
        } else {
            mApps.mItems.add(pos, mDraggingPaddingAppInfo);
        }

        mApps.updateAllItemOrderByPos(mApps.mItems);

        mApps.refreshView(false);

    }

    @Override
    public void onDragExit(DragObject d) {
        LauncherLog.d(TAG, "onDragExit: dragObject=" + d);
        int tempCell[] = {-1, -1};
        mAppsRecyclerView.setCrrentDragCell(tempCell);
        cleanupFolderCreation();
        cleanupAddToFolder();
    }

    @Override
    public void onFlingToDelete(DragObject dragObject, PointF vec) {
    }

    @Override
    public boolean acceptDrop(DragObject dragObject) {
        return true;
    }

    @Override
    public void prepareAccessibilityDrop() {
    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        mLauncher.getDragLayer().getDescendantRectRelativeToSelf(this, outRect);
    }

    @Override
    public void getLocationInDragLayer(int[] loc) {
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
    }

    @Override
    public void onDragEnd() {
        /*
         * If there is only one item in folder (this folder will be one only when existed),
         * remove the folder and put the item in list directly.
         */
        for (ItemInfo itemInfo : mApps.mItemsBackup) {
            if (itemInfo instanceof FolderInfo) {
                if (((FolderInfo) itemInfo).contents.size() == 1) {
                    AppInfo appInfo = ((FolderInfo) itemInfo).contents
                                .get(0).makeAppInfo();
                    appInfo.container = AllApps.CONTAINER_ALLAPP;
                    appInfo.screenId = 0;
                    appInfo.cellX = itemInfo.cellX;
                    appInfo.cellY = itemInfo.cellY;

                    // Replace the folder with app in the item list
                    int index = mApps.mItemsBackup.indexOf(itemInfo);
                    mApps.mItemsBackup.set(index, appInfo);

                    index = mApps.mItems.indexOf(itemInfo);
                    mApps.mItems.set(index, appInfo);

                    mApps.updateItemInDatabase(appInfo);
                    mApps.deleteItemInDatabase(itemInfo);
                    break;
                }
            }
        }
        mApps.refreshView(false);
    }

    private float getDistanceFromCell(float x, float y, int[] cell) {
        int[] cellCenterPoint = new int[2];
        if (mCellSize[0] == 0 || mCellSize[1] == 0) {
            mCellSize = mAppsRecyclerView.getCellSize();
        }

        // Calculate the cell center coordinate.
        cellCenterPoint[0] = cell[0] * mCellSize[0] + mCellSize[0] / 2;
        cellCenterPoint[1] = cell[1] * mCellSize[1] + mCellSize[1] / 2;

        // Calculate the distance between (x, y) and cell center.
        float distance = (float) Math.sqrt(Math.pow(x - cellCenterPoint[0], 2) +
                Math.pow(y - cellCenterPoint[1], 2));
        return distance;
    }

    boolean willCreateUserFolder(ItemInfo info, float distance) {
        LauncherLog.d(TAG, "willCreateUserFolder:distance:" + distance
            + ", mMaxDistanceForFolderCreation: " + mMaxDistanceForFolderCreation);
        if (info == null) {
            return false;
        }

        if (distance > mMaxDistanceForFolderCreation) {
            return false;
        }
        if ((info instanceof AppInfo) &&
            (((AppInfo) info).isForPadding != AppInfo.NOT_PADDING_APP)) {
            return false;
        }
        return true;
    }

    boolean willAddToExistingUserFolder(Object dragInfo, AllAppsRecyclerView target,
            int[] targetCell, float distance, FolderInfo folderInfo) {
        if (distance > mMaxDistanceForFolderCreation) {
            return false;
        }

        View dropOverView = getViewByItemInfo(target, folderInfo);
        if (dropOverView instanceof FolderIcon) {
            FolderIcon fi = (FolderIcon) dropOverView;
            if (fi.acceptDrop(dragInfo)) {
                return true;
            }
        }
        return false;
    }

    void setDragMode(int dragMode) {
        if (dragMode != mDragMode) {
            if (dragMode == Workspace.DRAG_MODE_NONE) {
                cleanupAddToFolder();
                // We don't want to cancel the re-order alarm every time the target cell changes
                // as this feels to slow / unresponsive.
                // cleanupReorder(false);
                cleanupFolderCreation();
            } else if (dragMode == Workspace.DRAG_MODE_ADD_TO_FOLDER) {
                // cleanupReorder(true);
                cleanupFolderCreation();
            } else if (dragMode == Workspace.DRAG_MODE_CREATE_FOLDER) {
                cleanupAddToFolder();
                // cleanupReorder(true);
            } else if (dragMode == Workspace.DRAG_MODE_REORDER) {
                cleanupAddToFolder();
                cleanupFolderCreation();
            }
            mDragMode = dragMode;
        }
    }

    public void setItems(ArrayList<ItemInfo> allItems) {
        // If the item list is not null, create the dragging padding app.
        if (allItems.size() > 0) {
            ItemInfo info = allItems.get(0);
            if (info instanceof FolderInfo) {
                AppInfo tempinfo = ((FolderInfo) info).contents.get(0).makeAppInfo();
                mDraggingPaddingAppInfo = new AppInfo(tempinfo);
            } else {
                mDraggingPaddingAppInfo = new AppInfo((AppInfo) info);
            }
            mDraggingPaddingAppInfo.isForPadding = AppInfo.DRAGGING_PADDING_APP;
            mDraggingPaddingAppInfo.isVisible = true;
            mDraggingPaddingAppInfo.title = "DraggingPadding";
            ComponentName cn = new ComponentName("com.op09.launcher",
                "com.op09.draggingPadding");
            mDraggingPaddingAppInfo.intent.setComponent(cn);
            mDraggingPaddingAppInfo.componentName = cn;
            mDraggingPaddingAppInfo.iconBitmap = BitmapFactory.decodeResource(
                mLauncher.getResources(), R.drawable.ic_launcher_edit_holo);
        }
        mApps.setItems(allItems);
    }

    boolean createUserFolderIfNecessary(DragView dragView, AppInfo dragInfo) {
        AllAppsRecyclerView.ScrollPositionState scrollPosState =
            new AllAppsRecyclerView.ScrollPositionState();

        mAppsRecyclerView.stopScroll();
        mAppsRecyclerView.getCurScrollState(scrollPosState);
        if (scrollPosState.rowIndex <= -1) {
            scrollPosState.rowIndex = 0;
        }
        int pageId = mAppsRecyclerView.mTargetPage <= 0
                        ? 0 : mAppsRecyclerView.mTargetPage;
        int pos = pageId * mMaxAppsCountInOnePage
                    + mTargetCell[1] * mNumAppsPerRow + mTargetCell[0];
        LauncherLog.d(TAG, "createUserFolderIfNecessary: pos:"
            +  pos + ",mTargetCell[1]:" + mTargetCell[1]
            + ",mTargetCell[0]:" + mTargetCell[0]);

        final FolderInfo folderInfo = new FolderInfo();
        folderInfo.title = mLauncher.getResources().getText(R.string.folder_name);
        folderInfo.cellX = pos;
        folderInfo.cellY = 0;
        folderInfo.screenId = 0;
        folderInfo.mPos = pos;

        LauncherModelPluginEx.addFolderItemToDatabase(
                mLauncher, folderInfo, 0, folderInfo.cellX, 0, false);

        // The drag app will be added behind the destination app.
        dragInfo.cellX = 1;
        dragInfo.cellY = 0;
        dragInfo.screenId = pageId;
        dragInfo.mPos = folderInfo.mPos;
        dragInfo.container = folderInfo.id;
        ShortcutInfo dragTag = dragInfo.makeShortcut();

        mApps.mItems.remove(dragInfo);
        mApps.mItemsBackup.remove(dragInfo);

        LauncherModelPluginEx.moveAllAppsItemInDatabase(mLauncher, dragInfo,
            dragInfo.container, dragInfo.screenId, dragInfo.cellX, dragInfo.cellY);

        // The destination app will be the 1st app in this folder.
        AppInfo destAppInfo = (AppInfo) mApps.mItems.get(pos);
        destAppInfo.cellX = 0;
        destAppInfo.cellY = 0;
        destAppInfo.container = folderInfo.id;
        ShortcutInfo destTag = destAppInfo.makeShortcut();

        LauncherModelPluginEx.moveAllAppsItemInDatabase(mLauncher, destAppInfo,
            destAppInfo.container, destAppInfo.screenId, destAppInfo.cellX, destAppInfo.cellY);

        folderInfo.add(dragTag);
        folderInfo.add(destTag);

        // Replace the dest app with this folder in the item list and backup list.
        int index  = mApps.mItems.indexOf(destAppInfo);
        mApps.mItems.set(index, folderInfo);
        index = mApps.mItemsBackup.indexOf(destAppInfo);
        if (index >= 0 && index < mApps.mItemsBackup.size()) {
            mApps.mItemsBackup.set(index, folderInfo);
        } else {
            mApps.mItemsBackup.add(folderInfo);
        }

        return true;
    }

    private void cleanupFolderCreation() {
        if (mDragFolderRingAnimator != null) {
            mDragFolderRingAnimator.animateToNaturalState();
            mDragFolderRingAnimator = null;
        }
        mFolderCreationAlarm.setOnAlarmListener(null);
        mFolderCreationAlarm.cancelAlarm();
    }

    private void cleanupAddToFolder() {
        if (mDragOverFolderIcon != null) {
            mDragOverFolderIcon.onDragExit();
            mDragOverFolderIcon = null;
        }
        if (mAddToFolderRingAnimator != null) {
            mAddToFolderRingAnimator.animateToNaturalState();
            mAddToFolderRingAnimator = null;
        }
        mAddToFolderAlarm.setOnAlarmListener(null);
        mAddToFolderAlarm.cancelAlarm();
    }

    private final Alarm mFolderCreationAlarm = new Alarm();
    private FolderRingAnimator mDragFolderRingAnimator = null;
    private final Alarm mAddToFolderAlarm = new Alarm();
    private FolderRingAnimator mAddToFolderRingAnimator = null;
    private FolderIcon mDragOverFolderIcon = null;

    // Create alarm listener.
    public class FolderCreationAlarmListener implements OnAlarmListener {
        RecyclerView mRecyclerView;
        int mCellX;
        int mCellY;

        public FolderCreationAlarmListener(RecyclerView recyclerView,
                int cellX, int cellY) {
            this.mRecyclerView = recyclerView;
            this.mCellX = cellX;
            this.mCellY = cellY;
        }

        public void onAlarm(Alarm alarm) {
            if (mDragFolderRingAnimator != null) {
                // This shouldn't happen ever, but just in case, make sure we clean up the mess.
                mDragFolderRingAnimator.animateToNaturalState();
            }
            mDragFolderRingAnimator = new FolderRingAnimator(mLauncher, null);
            mDragFolderRingAnimator.mIsPageViewFolderIcon = true;
            mDragFolderRingAnimator.setCell(mCellX, mCellY);
            mDragFolderRingAnimator.setRecyclerView(mRecyclerView);
            mDragFolderRingAnimator.animateToAcceptState();

            ((AllAppsRecyclerView) mRecyclerView).showFolderAccept(mDragFolderRingAnimator);
            ((AllAppsRecyclerView) mRecyclerView).clearDragOutlines();

            LauncherLog.d(TAG, "FolderCreationAlarmListener, onAlarm");
            // setDragMode(Workspace.DRAG_MODE_CREATE_FOLDER);
        }
    }

    public class AddToFolderAlarmListener implements OnAlarmListener {
        RecyclerView mRecyclerView;
        int mCellX;
        int mCellY;

        public AddToFolderAlarmListener(RecyclerView recyclerView,
                int cellX, int cellY) {
            this.mRecyclerView = recyclerView;
            this.mCellX = cellX;
            this.mCellY = cellY;
        }

        public void onAlarm(Alarm alarm) {
            if (mAddToFolderRingAnimator != null) {
                mAddToFolderRingAnimator.animateToNaturalState();
            }
            mAddToFolderRingAnimator = new FolderRingAnimator(mLauncher, null);
            mAddToFolderRingAnimator.mIsPageViewFolderIcon = true;
            mAddToFolderRingAnimator.setCell(mCellX, mCellY);
            mAddToFolderRingAnimator.setRecyclerView(mRecyclerView);
            mAddToFolderRingAnimator.animateToAcceptState();
            ((AllAppsRecyclerView) mRecyclerView).showFolderAccept(mAddToFolderRingAnimator);
            ((AllAppsRecyclerView) mRecyclerView).clearDragOutlines();

            LauncherLog.d(TAG, "AddToFolderAlarmListener, onAlarm");
        }
    }

    private View getViewByItemInfo(AllAppsRecyclerView target, ItemInfo info) {
        View childView = null;

        for (int i = 0; i < target.getChildCount(); i++) {
            childView = target.getChildAt(i);
            Object tag = childView.getTag();
            if (tag != null && tag == info) {
                return childView;
            }
        }
        return null;
    }

    /// @}
}
