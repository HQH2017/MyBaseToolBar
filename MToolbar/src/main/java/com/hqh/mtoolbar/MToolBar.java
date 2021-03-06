package com.hqh.mtoolbar;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;

import com.hqh.mtoolbar.R;
import com.hqh.mtoolbar.util.ScreenUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * @Anthor : HQH
 * @Date : 2020/01/13
 *
 * 暂时不显示 Listview
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MToolBar extends Toolbar implements View.OnClickListener{

    // 搜索框
    private FrameLayout search_fl;
    private EditText etInput;   // 输入框
    private ImageView ivDelete; // 删除键
    private ListView lvTips;    // 弹出列表
    private ArrayAdapter<String> mHintAdapter;  // 提示adapter （推荐adapter）
    private ArrayAdapter<String> mAutoCompleteAdapter; // 自动补全adapter 只显示名字
    private SearchViewListener mListener; // 搜索回调接口
    public void setSearchViewListener(SearchViewListener listener) {
        mListener = listener;
    }

    @IntDef({TITLE_ELLIPSIS, SUBTITLE_ELLIPSIS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EllipsisMode {
    }
    public static final int TITLE_ELLIPSIS = 1;//缩略主标题
    public static final int SUBTITLE_ELLIPSIS = 0;//缩略副标题
    private int mEllipsisMode;//缩略模式
    //    private int mTitleMode;//标题显示模式
    private Context mContext;
    private View mStatusBar;//状态栏
    private View mBackView;//返回按钮
    private View mBottomDivider;//状态栏底部分割线
    private LinearLayout mRootView;//根部局
    private TextView mTitleTextView;//标题
    private LinearLayout mLayoutCenter;//中心布局
    private LinearLayout mLayoutLeft, mLayoutRight;//左右布局
    private int mSubTextColorId = Color.BLACK;//副标题文本颜色
    private float mSubTextSize = 16;//副标题文本字体大小 sp

    public MToolBar(Context context) {
        super(context);
        initView(context);
    }

    public MToolBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MToolBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        //toolbar默认marginLeft ，所以定位到(0,0) 防止偏移
        this.setContentInsetsAbsolute(0, 0);
        this.mContext = context;
        View view = inflate(context, R.layout.layout_toolbar, this);
        mStatusBar = view.findViewById(R.id.mStatusBar);
        mBottomDivider = view.findViewById(R.id.mBottomDivider);
        mRootView = (LinearLayout) view.findViewById(R.id.mRootView);
        mTitleTextView = (TextView) view.findViewById(R.id.mTitleTextView);
        mLayoutLeft = (LinearLayout) view.findViewById(R.id.mLayoutLeft);    // 左边
        mLayoutRight = (LinearLayout) view.findViewById(R.id.mLayoutRight);  // 右边
        mLayoutCenter = (LinearLayout) view.findViewById(R.id.mLayoutCenter);// 中间
        // 搜索框
        search_fl = view.findViewById(R.id.search_fl);
        etInput = view.findViewById(R.id.search_et_input);
        ivDelete = view.findViewById(R.id.search_iv_delete);
        lvTips = view.findViewById(R.id.search_lv_tips);

        ivDelete.setOnClickListener(this);
        etInput.addTextChangedListener(new EditChangedListener());
        etInput.setOnClickListener(this);
        etInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    lvTips.setVisibility(GONE);
                    notifyStartSearching(etInput.getText().toString());
                }
                return true;
            }
        });
    }

    /** 搜索框只作显示页面,点击跳转用 **/
    public void setShowEditPage(boolean isEdit) {
        etInput.setFocusable(false);
        etInput.setFocusableInTouchMode(false);
    }

    /**
     * 通知监听者 进行搜索操作
     * @param text
     */
    private void notifyStartSearching(String text){
        if (mListener != null) {
            mListener.onSearch(etInput.getText().toString());
        }
        //隐藏软键盘
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 设置热搜版提示 adapter
     */
    public void setTipsHintAdapter(ArrayAdapter<String> adapter) {
        this.mHintAdapter = adapter;
        if (lvTips.getAdapter() == null) {
            lvTips.setAdapter(mHintAdapter);
        }
    }

    /**
     * 设置自动补全adapter
     */
    public void setAutoCompleteAdapter(ArrayAdapter<String> adapter) {
        this.mAutoCompleteAdapter = adapter;
    }

    /**
     * 根部局
     *
     * @return
     */
    public ViewGroup getRootView() {
        return mRootView;
    }


    /**
     * 获取状态栏高度
     *
     * @return 状态栏高度
     */
    public int getStatusBarHeight() {
        Resources resources = Resources.getSystem();
        int result = 0; //获取状态栏高度的资源id
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 显示状态栏 ((此功能需配合沉浸式API>=19才会生效))
     * 默认为透明，保持和toolbar一样的颜色
     */
    public void setStatusBarTransparent() {
        setStatusBarColor(Color.TRANSPARENT);
    }

    /**
     * 显示状态栏颜色 (此功能需配合沉浸式API>=19才会生效)
     *
     * @param colorId 状态栏颜色
     */
    public void setStatusBarColor(@ColorInt int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//android 19 4.4 以上才支持沉浸式
            mStatusBar.setVisibility(VISIBLE);
            mStatusBar.setBackgroundColor(colorId);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mStatusBar.getLayoutParams();
            params.height = getStatusBarHeight();
        }
    }

    /**
     * 隐藏 statusBar
     */
    public void hideStatusBar() {
        mStatusBar.setVisibility(GONE);
    }

    /**
     * toolbar 和布局的分割线 (默认不显示)
     *
     * @param colorId
     * @param height
     */
    public void setBottomDivider(@ColorInt int colorId, int height) {
        mBottomDivider.setVisibility(VISIBLE);
        mBottomDivider.setBackgroundColor(colorId);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mBottomDivider.getLayoutParams();
        params.height = height;
    }

    /**
     * 隐藏 BottomDivider
     */
    public void hideBottomDivider() {
        mBottomDivider.setVisibility(GONE);
    }

    /**
     * 设置返回按钮 限制只能添加一个BackButton
     */
    public void setBackButton(@DrawableRes int resId) {
        if (resId == 0) {
            return;
        }
        if (mBackView != null) removeLeftView(mBackView);
        mBackView = createImageMenu(mContext, resId, new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() instanceof Activity)
                    ((Activity) getContext()).onBackPressed();//调用activity的返回键
            }
        });
        int padding = ScreenUtil.dip2px(mContext, 12);
        mBackView.setPadding(padding / 3, 0, padding, 0);
        addLeftView(mBackView);
    }

    /**
     * 带有 图标和 文字 二种可选的 返回按钮 文字 颜色 大小 默认为
     *
     * @param resId
     * @param text
     */
    public void setBackButton(@DrawableRes int resId, CharSequence text) {
        setBackButton(resId, text, mSubTextColorId, mSubTextSize);
    }

    /**
     * 带有 图标和 文字 二种可选的 返回按钮
     *
     * @param resId
     * @param text
     * @param textColorId
     * @param textSize
     */
    public void setBackButton(@DrawableRes int resId, CharSequence text, int textColorId, float textSize) {
        if (resId == 0) {
            return;
        }

        if (mBackView != null) removeLeftView(mBackView);//限制只能添加一个BackButton
        mBackView = createBackLayout(mContext, resId, text, textColorId, textSize, new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() instanceof Activity)
                    ((Activity) getContext()).onBackPressed();//调用activity的返回键
            }
        });

        int padding = ScreenUtil.dip2px(mContext, 10);
        mBackView.setPadding(0, 0, padding, 0);

        addLeftView(mBackView);
    }


    /**
     * 隐藏返回按钮
     */
    public void hideBackButton() {
        if (mBackView != null) {
            mBackView.setVisibility(GONE);
        }
    }

    /** 左边同时有文字和图标 **/
    public void setLeftImageTxt(@DrawableRes int resId, CharSequence text, int textColorId, float textSize, OnClickListener listener) {
        if (resId == 0) {
            return;
        }
        if (mBackView != null) removeLeftView(mBackView);//限制只能添加一个BackButton
        mBackView = createBackLayout(mContext, resId, text, textColorId, textSize, listener);
        int padding = ScreenUtil.dip2px(mContext, 1);
        mBackView.setPadding(0, 0, padding, 0);
        addLeftView(mBackView);
    }

    /**
     * 隐藏/显示中间的title 或  搜索框
     * true :隐藏文字同时显示搜索框； false:则相反
     */
    public void changeShowview(boolean isflat) {
        if (isflat) {
            if (mTitleTextView != null)  mTitleTextView.setVisibility(GONE);
            if (search_fl != null) search_fl.setVisibility(VISIBLE);
        } else {
            if (mTitleTextView != null) mTitleTextView.setVisibility(VISIBLE);
            if (search_fl != null) search_fl.setVisibility(GONE);
        }
    }


    /**
     * 添加左边View
     *
     * @param view
     */
    public void addLeftView(View view) {
        mLayoutLeft.addView(view);
    }

    public void addLeftView(View view, int index) {
        mLayoutLeft.addView(view, index);
    }

    public void addLeftView(View view, ViewGroup.LayoutParams params) {
        mLayoutLeft.addView(view, params);
    }

    public void addLeftView(View view, int index, ViewGroup.LayoutParams params) {
        mLayoutLeft.addView(view, index, params);
    }

    /**
     * 移除左边View
     *
     * @param view
     */
    public void removeLeftView(View view) {
        mLayoutLeft.removeView(view);
    }

    public void removeLeftView(int index) {
        mLayoutLeft.removeViewAt(index);
    }

    public void removeAllLeftView() {
        mLayoutLeft.removeAllViews();
    }

    /**
     * 显示左边图形菜单按钮
     */
    public void addLeftImage(@DrawableRes int resId, OnClickListener listener) {
        ImageView imageMenu = createImageMenu(mContext, resId, listener);
        imageMenu.setImageResource(resId);
        imageMenu.setOnClickListener(listener);
        addLeftView(imageMenu);
    }

    /**
     * 显示左边文本菜单按钮
     */
    public void addLeftText(CharSequence text, OnClickListener listener) {
        TextView textMenu = createTextMenu(mContext, text, mSubTextColorId, mSubTextSize, listener);
        addLeftView(textMenu);
    }

    public void addLeftText(CharSequence text, @ColorInt int colorId, float textSize, OnClickListener listener) {
        TextView textMenu = createTextMenu(mContext, text, colorId, textSize, listener);
        addLeftView(textMenu);
    }

    public void addLeftText(@StringRes int resId, OnClickListener listener) {
        addLeftText(getContext().getText(resId), listener);
    }

    public void addLeftText(@StringRes int resId, @ColorInt int colorId, int textSize, OnClickListener listener) {
        addLeftText(getContext().getText(resId), colorId, textSize, listener);
    }


    /**
     * 添加右边View
     *
     * @param view
     */
    public void addRightView(View view) {
        mLayoutRight.addView(view);
    }

    public void addRightView(View view, int index) {
        mLayoutRight.addView(view, index);
    }

    public void addRightView(View view, ViewGroup.LayoutParams params) {
        mLayoutRight.addView(view, params);
    }

    public void addRightView(View view, int index, ViewGroup.LayoutParams params) {
        mLayoutRight.addView(view, index, params);
    }

    /**
     * 移除右边View
     *
     * @param view
     */
    public void removeRightView(View view) {
        mLayoutRight.removeView(view);
    }

    public void removeRightView(int index) {
        mLayoutRight.removeViewAt(index);
    }

    public void removeAllRightView() {
        mLayoutRight.removeAllViews();
    }

    /**
     * 显示右边文本菜单按钮
     *
     * @param text
     * @param listener
     */
    public void addRightText(CharSequence text, @ColorInt int colorId, float textSize, OnClickListener listener) {
        TextView textMenu = createTextMenu(mContext, text, colorId, textSize, listener);
        addRightView(textMenu);
    }

    public void addRightText(CharSequence text, OnClickListener listener) {
        TextView textMenu = createTextMenu(mContext, text, mSubTextColorId, mSubTextSize, listener);
        addRightView(textMenu);
    }

    public void addRightText(@StringRes int text, OnClickListener listener) {
        addRightText(getContext().getText(text), listener);
    }

    public void addRightText(@StringRes int text, @ColorInt int colorId, float textSize, OnClickListener listener) {
        addRightText(getContext().getText(text), colorId, textSize, listener);
    }


    /**
     * 显示右边图形菜单按钮
     */
    public void addRightImage(@DrawableRes int resId, OnClickListener listener) {
        ImageView imageMenu = createImageMenu(mContext, resId, listener);
        imageMenu.setImageResource(resId);
        imageMenu.setOnClickListener(listener);
        addRightView(imageMenu);
    }

    ////////////////////////////////////////
    /**
     * 增加右边图形菜单 搜索框 按钮（imageview）
     */
    public void addRightSearchImage(@DrawableRes int resId) {
        ImageView imageMenu = createImageMenu(mContext, resId, null);
        imageMenu.setImageResource(resId);
        imageMenu.setClickable(true);
        imageMenu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) { // 回调搜索框的文字
                notifyStartSearching(etInput.getText().toString());
            }
        });
        addRightView(imageMenu);
    }
    /**
     * 增加右边图形菜单 搜索框 按钮（textview）
     */
    public void addRightSearchText(@StringRes int text, @ColorInt int colorId, float textSize) {
        TextView textMenu = createTextMenu(mContext, getContext().getText(text), colorId, textSize, null);
        textMenu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) { // 回调搜索框的文字
                notifyStartSearching(etInput.getText().toString());
            }
        });
        addRightView(textMenu);
    }

    ////////////////////////////////////////

    /**
     * 添加中间View
     *
     * @param view
     */
    public void addCenterView(View view) {
        mLayoutCenter.addView(view);
    }

    public void addCenterView(View view, int index) {
        mLayoutCenter.addView(view, index);

    }

    public void addCenterView(View view, ViewGroup.LayoutParams params) {
        mLayoutCenter.addView(view, params);
    }

    public void addCenterView(View view, int index, ViewGroup.LayoutParams params) {
        mLayoutCenter.addView(view, index, params);
    }

    /**
     * 移除中间View
     *
     * @param view
     */
    public void removeCenterView(View view) {
        mLayoutCenter.removeView(view);
    }

    public void removeCenterView(int index) {
        mLayoutCenter.removeViewAt(index);
    }

    public void removeAllCenterView() {
        mLayoutCenter.removeAllViews();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // keepTitleViewCenterForParent();
    }

    /**
     * 保持TitleView和左右屏幕边距离相等，文字居中
     */
    private void keepTitleViewCenterForParent() {
        int screenWidth = ScreenUtil.getScreenWidth(getContext());
        int margin = Math.max(mLayoutLeft.getWidth(), mLayoutRight.getWidth());

        if (mEllipsisMode == SUBTITLE_ELLIPSIS) {
            // mTitleTextView.setVisibility(VISIBLE);
            if (mTitleTextView.getWidth() + margin * 2 > screenWidth
                    && mLayoutLeft.getWidth() != mLayoutRight.getWidth()) {//超出了屏幕宽度要做缩略
                //相等就不再设置，避免死循环
                ellipsisSubTitle();
            }
        } else {
            if (margin > screenWidth / 2f) {
                mTitleTextView.setVisibility(GONE);
                return;//超出屏幕一半，Title已经没有显示的地方了
            } else {
                // mTitleTextView.setVisibility(VISIBLE);
            }
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTitleTextView.getLayoutParams();
            if (margin == params.leftMargin && margin == params.rightMargin)
                return;//相等就不再设置，避免死循环
            ellipsisTitle(margin);
        }

    }


    /**
     * 缩略副标题 策略
     */
    private void ellipsisSubTitle() {
//        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);//创建新的params
//        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
//
//        final RelativeLayout.LayoutParams subLeftParams = new RelativeLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);//创建新的params
//        subLeftParams.addRule(RelativeLayout.LEFT_OF, R.id.mTitleTextView);
//        subLeftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//
//
//        final RelativeLayout.LayoutParams subRightParams = new RelativeLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);//创建新的params
//        subRightParams.addRule(RelativeLayout.RIGHT_OF, R.id.mTitleTextView);
//        subRightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//
//        post(new Runnable() {
//            @Override
//            public void run() {
//                mTitleTextView.setLayoutParams(params);
//                mTitleTextView.setGravity(Gravity.CENTER);
//                mLayoutLeft.setLayoutParams(subLeftParams);
//                mLayoutRight.setLayoutParams(subRightParams);
//            }
//        });
    }

    /**
     * 缩略主标题 策略
     *
     * @param margin
     */
    private void ellipsisTitle(int margin) {
//        final RelativeLayout.LayoutParams subLeftParams = new RelativeLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);//创建新的params
//        subLeftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//
//        final RelativeLayout.LayoutParams subRightParams = new RelativeLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);//创建新的params
//        subRightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//
//        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);//创建新的params
//        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
//        params.leftMargin = margin;
//        params.rightMargin = margin;
//
//        post(new Runnable() {
//            @Override
//            public void run() {
//                mTitleTextView.setLayoutParams(params);
//                mTitleTextView.setGravity(Gravity.CENTER);
//                mLayoutLeft.setLayoutParams(subLeftParams);
//                mLayoutRight.setLayoutParams(subRightParams);
//
//            }
//        });

    }


    /**
     * 设置 titleText
     *
     * @param titleText
     */
    @Override
    public void setTitle(CharSequence titleText) {
        mTitleTextView.setText(titleText);
    }

    @Override
    public void setTitle(@StringRes int resId) {
        setTitle(getContext().getText(resId));
    }

    // 隐藏Title


    /**
     * 设置缩略模式 是副标题缩略 还是主标题缩略(注意 缩略主标题可能会导致标题不显示，慎用)  默认副标题缩略{@link EllipsisMode}
     *
     * @param ellipsisMode
     */
    public void setEllipsisMode(@EllipsisMode int ellipsisMode) {
        mEllipsisMode = ellipsisMode;
        requestLayout();
    }

    public int getEllipsisMode() {
        return mEllipsisMode;
    }

    /**
     * 获取title
     *
     * @return
     */
    @Override
    public CharSequence getTitle() {
        return mTitleTextView.getText();
    }

    /**
     * 设置标题 颜色
     *
     * @param colorId
     */
    @Override
    public void setTitleTextColor(@ColorInt int colorId) {
        mTitleTextView.setTextColor(colorId);
    }

    /**
     * 设置标题字体大小
     *
     * @param textSize
     */
    public void setTitleTextSize(float textSize) {
        mTitleTextView.setTextSize(textSize);
    }

    /**
     * 设置标题 加粗
     *
     * @param bold
     */
    public void setTitleBoldText(boolean bold) {
        mTitleTextView.getPaint().setFakeBoldText(bold);
    }

    /**
     * 设置全局副标题 文本颜色
     * <p>
     * 优先级 setSubTextColor < addLeftText(TextView)/addRightText(TextView)
     *
     * @param subTextColorId
     */
    public void setSubTextColor(@ColorInt int subTextColorId) {
        this.mSubTextColorId = subTextColorId;
        setChildTextColor(mLayoutLeft);
        setChildTextColor(mLayoutRight);
        setChildTextColor(mLayoutCenter);
    }

    /**
     * 设置全局副标题文本 字体大小
     * <p>
     * 优先级setSubTextColor < addLeftText(TextView)/addRightText(TextView)
     *
     * @param subTextSize
     */
    public void setSubTextSize(float subTextSize) {
        this.mSubTextSize = subTextSize;
        setChildTextSize(mLayoutLeft);
        setChildTextSize(mLayoutRight);
        setChildTextSize(mLayoutCenter);
    }

    /**
     * 遍历设置字体大小
     *
     * @param view
     */
    private void setChildTextSize(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = viewGroup.getChildAt(i);
                setChildTextSize(child);
            }
        } else if (view instanceof TextView) {
            ((TextView) view).setTextSize(mSubTextSize);
        }
    }

    /**
     * 遍历设置字体颜色
     *
     * @param view
     */
    private void setChildTextColor(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = viewGroup.getChildAt(i);
                setChildTextSize(child);
            }
        } else if (view instanceof TextView) {
            ((TextView) view).setTextSize(mSubTextColorId);
        }
    }


    /**
     * 设置整个toolbar背景颜色
     *
     * @param colorId
     */
    @Override
    public void setBackgroundColor(@ColorInt int colorId) {
        mRootView.setBackgroundColor(colorId);
    }

    @Override
    public void setBackgroundResource(@DrawableRes int resId) {
        mRootView.setBackgroundResource(resId);
    }


    public TextView getTitleTextView() {
        return mTitleTextView;
    }

    public View getStatusBar() {
        return mStatusBar;
    }

    public View getBottomDivider() {
        return mBottomDivider;
    }

    public View getLeftView(int index) {
        return mLayoutLeft.getChildAt(index);
    }

    public View getRightView(int index) {
        return mLayoutRight.getChildAt(index);
    }

    public LinearLayout getLeftLayout() {
        return mLayoutLeft;
    }

    public LinearLayout getRightLayout() {
        return mLayoutRight;
    }

//    public FrameLayout getCenterLayout() {
//        return mLayoutCenter;
//    }
    public LinearLayout getCenterLayout() {
    return mLayoutCenter;
}


    /**
     * builder 模式
     */
    public static class Builder {
        private Context mContext;
        private CharSequence titleText;
        private int titleTextResId;
        private int backResId;
        private int statusBarColorId,
                bottomDividerColorId = Color.BLACK,
                backgroundColorId = Color.BLUE,
                titleColorId = Color.BLACK,//均设置默认值
                subTextColorId = Color.BLACK;//均设置默认值
        private float titleTextSize = 18, subTextSize = 16;
        private boolean showStatusBar = false;//是否显示StatusBar
        private int bottomDividerHeight = 0;
        private ArrayList<View> leftViewList, rightViewList;

        public Builder(Context context) {
            this.mContext = context;
        }

        public Builder setBackButton(@DrawableRes int backResId) {
            this.backResId = backResId;
            return this;
        }

        public Builder setBottomDivider(@ColorInt int bottomDividerColorId, int bottomDividerHeight) {
            this.bottomDividerColorId = bottomDividerColorId;
            this.bottomDividerHeight = bottomDividerHeight;
            return this;
        }

        /**
         * leftImage ScaleType默认为 ScaleType.CENTER
         *
         * @param imgResId
         * @param listener
         * @return
         */
        public Builder addLeftImage(@DrawableRes int imgResId, OnClickListener listener) {
            return addLeftImage(imgResId, ImageView.ScaleType.CENTER, listener);
        }

        public Builder addLeftImage(@DrawableRes int imgResId, ImageView.ScaleType scaleType, OnClickListener listener) {
            if (leftViewList == null) leftViewList = new ArrayList<>();
            leftViewList.add(createImageMenu(mContext, imgResId, scaleType, listener));
            return this;
        }

        /**
         * addRightImage ScaleType默认为 ScaleType.CENTER
         *
         * @param imgResId
         * @param listener
         * @return
         */
        public Builder addRightImage(@DrawableRes int imgResId, OnClickListener listener) {
            return addRightImage(imgResId, ImageView.ScaleType.CENTER, listener);
        }

        public Builder addRightImage(@DrawableRes int imgResId, ImageView.ScaleType scaleType, OnClickListener listener) {
            if (rightViewList == null) rightViewList = new ArrayList<>();
            rightViewList.add(createImageMenu(mContext, imgResId, scaleType, listener));
            return this;
        }


        /**
         * leftText 默认subTextColor Color.BLACK textSize 16sp
         *
         * @param text
         * @param listener
         * @return
         */
        public Builder addLeftText(CharSequence text, OnClickListener listener) {
            return addLeftText(text, subTextColorId, subTextSize, listener);
        }


        public Builder addLeftText(CharSequence text, @ColorInt int textColorId, float textSize, OnClickListener listener) {
            if (leftViewList == null) leftViewList = new ArrayList<>();
            leftViewList.add(createTextMenu(mContext, text, textColorId, textSize, listener));
            return this;
        }

        /**
         * leftText 默认subTextColor Color.BLACK textSize 16sp
         *
         * @param textResId
         * @param listener
         * @return
         */
        public Builder addLeftText(@StringRes int textResId, OnClickListener listener) {
            return addLeftText(mContext.getString(textResId), subTextColorId, subTextSize, listener);
        }

        public Builder addLeftText(@StringRes int textResId, @ColorInt int textColorId, float textSize, OnClickListener listener) {
            return addLeftText(mContext.getString(textResId), textColorId, textSize, listener);
        }


        /**
         * rightText 默认subTextColor Color.BLACK textSize 16sp
         *
         * @param text
         * @param listener
         * @return
         */
        public Builder addRightText(CharSequence text, OnClickListener listener) {
            return addRightText(text, subTextColorId, subTextSize, listener);
        }

        public Builder addRightText(CharSequence text, @ColorInt int textColorId, float textSize, OnClickListener listener) {
            if (rightViewList == null) rightViewList = new ArrayList<>();
            rightViewList.add(createTextMenu(mContext, text, textColorId, textSize, listener));
            return this;
        }

        /**
         * rightText 默认subTextColor Color.BLACK textSize 16sp
         *
         * @param textResId
         * @param listener
         * @return
         */
        public Builder addRightText(@StringRes int textResId, OnClickListener listener) {
            return addRightText(mContext.getString(textResId), listener);
        }

        public Builder addRightText(@StringRes int textResId, @ColorInt int textColorId, float textSize, OnClickListener listener) {
            return addRightText(mContext.getString(textResId), textColorId, textSize, listener);
        }

        /**
         * addView
         *
         * @param view
         * @return
         */
        public Builder addLeftView(View view) {
            if (leftViewList == null) leftViewList = new ArrayList<>();
            leftViewList.add(view);
            return this;
        }

        public Builder addRightView(View view) {
            if (rightViewList == null) rightViewList = new ArrayList<>();
            rightViewList.add(view);
            return this;
        }

        public Builder setTitle(CharSequence titleText) {
            this.titleText = titleText;
            return this;
        }

        public Builder setTitle(@StringRes int titleTextResId) {
            this.titleTextResId = titleTextResId;
            return this;
        }

        /**
         * setTitleTextColor  和 setAllTextColor 顺序 按最新设置的那个算
         *
         * @param titleColorId
         * @return
         */
        public Builder setTitleTextColor(@ColorInt int titleColorId) {
            this.titleColorId = titleColorId;
            return this;
        }

        public Builder setTitleTextSize(float titleTextSize) {
            this.titleTextSize = titleTextSize;
            return this;
        }

        /**
         * 设置所有副标题文本颜色
         * 要在 添加副标题前调用，否则不起作用
         *
         * @param subTextColorId
         * @return
         */
        public Builder setSubTextColor(@ColorInt int subTextColorId) {
            this.subTextColorId = subTextColorId;
            return this;
        }

        public Builder setSubTextSize(float subTextSize) {
            this.subTextSize = subTextSize;
            return this;
        }

        public Builder setBackgroundColor(@ColorInt int backgroundColorId) {
            this.backgroundColorId = backgroundColorId;
            return this;
        }

        public Builder setStatusBarColor(@ColorInt int statusBarColorId) {
            this.statusBarColorId = statusBarColorId;
            this.showStatusBar = true;
            return this;
        }

        public MToolBar build() {
            MToolBar toolbar = new MToolBar(mContext);

            /*** 默认隐藏 也可以将backResId 设置一个默认值*/
            toolbar.setBackButton(backResId);

            /*** titleText */
            if (TextUtils.isEmpty(titleText)) {
                if (titleTextResId > 0) toolbar.setTitle(titleTextResId);
                else toolbar.setTitle(null);
            } else toolbar.setTitle(titleText);

            toolbar.setTitleTextColor(titleColorId);
            toolbar.setTitleTextSize(titleTextSize);

            /*** Menu Text color textSize*/
            toolbar.setSubTextColor(subTextColorId);
            toolbar.setSubTextSize(subTextSize);

            /*** leftMenu */
            if (leftViewList != null && !leftViewList.isEmpty()) {
                for (View view : leftViewList) {
                    toolbar.addLeftView(view);
                }
            }

            /*** rightMenu */
            if (rightViewList != null && !rightViewList.isEmpty()) {
                for (View view : rightViewList) {
                    toolbar.addRightView(view);
                }
            }

            if (bottomDividerHeight > 0)
                toolbar.setBottomDivider(bottomDividerColorId, bottomDividerHeight);

            if (showStatusBar) toolbar.setStatusBarColor(statusBarColorId);

            toolbar.setBackgroundColor(backgroundColorId);

            return toolbar;
        }
    }


    /**
     * 创建文字菜单
     *
     * @param context
     * @param text
     * @param textColorId
     * @param textSize
     * @param listener
     * @return
     */
    public static TextView createTextMenu(Context context, CharSequence text, @ColorInt int textColorId, float textSize, OnClickListener listener) {
        TextView textMenu = new TextView(context);
        textMenu.setTextColor(textColorId);
        textMenu.setTextSize(textSize);
        textMenu.setGravity(Gravity.CENTER);
        int padding = ScreenUtil.dip2px(context, 5);
        textMenu.setPadding(padding, 0, padding, 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        textMenu.setLayoutParams(params);
        textMenu.setText(text);
        if (listener != null) textMenu.setOnClickListener(listener);
        return textMenu;
    }

    public static ImageView createImageMenu(Context context, @DrawableRes int imageResId, OnClickListener listener) {
        return createImageMenu(context, imageResId, ImageView.ScaleType.CENTER, listener);
    }

    /**
     * 创建图片菜单
     *
     * @param context
     * @param imageResId
     * @param scaleType
     * @param listener
     * @return
     */
    public static ImageView createImageMenu(Context context, @DrawableRes int imageResId, ImageView.ScaleType scaleType, OnClickListener listener) {
        ImageView imageMenu = new ImageView(context);
        int padding = ScreenUtil.dip2px(context, 5);
        imageMenu.setPadding(padding, 0, padding, 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        imageMenu.setScaleType(scaleType);
        imageMenu.setLayoutParams(params);
        imageMenu.setImageResource(imageResId);
        if (listener != null) imageMenu.setOnClickListener(listener);
        return imageMenu;
    }

    /**
     * 创建 带文字 和图案 的返回键
     *
     * @param context
     * @param imageResId
     * @param text
     * @param textColorId
     * @param textSize_SP
     * @param listener
     * @return
     */
    public static LinearLayout createBackLayout(Context context, @DrawableRes int imageResId, CharSequence text, @ColorInt int textColorId, float textSize_SP, OnClickListener listener) {
        LinearLayout backLayout = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        backLayout.setLayoutParams(params);
        if (imageResId != 0) {
            ImageView iv = createImageMenu(context, imageResId, ImageView.ScaleType.CENTER_INSIDE, null);

            iv.setPadding(0, 0, 0, 0);
            backLayout.addView(iv);
        }
        if (!TextUtils.isEmpty(text)) {
            TextView tv = createTextMenu(context, text, textColorId, textSize_SP, null);
            tv.setPadding(0, 0, 0, 0);
            backLayout.addView(tv);
        }

        int padding = ScreenUtil.dip2px(context, 5);
        backLayout.setPadding(padding, 0, padding, 0);
        backLayout.setGravity(Gravity.CENTER);
        backLayout.setOnClickListener(listener);

        return backLayout;
    }

    // 搜索edittext
    private class EditChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if (!"".equals(charSequence.toString())) {
                ivDelete.setVisibility(VISIBLE);
//                lvTips.setVisibility(VISIBLE);
//                if (mAutoCompleteAdapter != null && lvTips.getAdapter() != mAutoCompleteAdapter) {
//                    lvTips.setAdapter(mAutoCompleteAdapter);
//                }
                //更新autoComplete数据
                if (mListener != null) {
                    mListener.onRefreshAutoComplete(charSequence + "");
                }
            } else {
                ivDelete.setVisibility(GONE);
//                if (mHintAdapter != null) {
//                    lvTips.setAdapter(mHintAdapter);
//                }
//                lvTips.setVisibility(GONE);
            }

        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.search_et_input) {
//            lvTips.setVisibility(VISIBLE);
            if (mListener != null) {
                mListener.onSearchClick();
            }
        } else if (id == R.id.search_iv_delete) {
            etInput.setText("");
            ivDelete.setVisibility(GONE);
        }
    }

    /**
     * search view回调方法
     */
    public interface SearchViewListener {
        /**
         * 更新自动补全内容
         *
         * @param text 传入补全后的文本
         */
        void onRefreshAutoComplete(String text);
        /**
         * 开始搜索
         *
         * @param text 传入输入框的文本
         */
        void onSearch(String text);
        /**
         * 提示列表项点击时回调方法 (提示/自动补全)
         */
        // void onTipsItemClick(String text);
        /**
         * 只作点击跳转
         */
        void onSearchClick();
    }

}
