package activity.hit.edu.cn;


import hit.edu.cn.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;


public class MyRefreshListView extends ListView implements OnScrollListener {

	/*状态编码，共四个状态*/
    private static final int TAP_TO_REFRESH = 1;
    private static final int PULL_TO_REFRESH = 2;
    private static final int RELEASE_TO_REFRESH = 3;
    private static final int REFRESHING = 4;

    private static final String TAG = "PullToRefreshListView";

    /*刷新回调接口*/
    private OnRefreshListener mOnRefreshListener;

    /**
     * 列表滚动的监听器
     */
    private OnScrollListener mOnScrollListener;
    private LayoutInflater mInflater;

    private RelativeLayout mRefreshHeaderView;   //headerView布局
    private TextView mRefreshViewText;
    private ImageView mRefreshViewImage;
    private ProgressBar mRefreshViewProgress;
    private TextView mRefreshViewLastUpdated;

    private int mCurrentScrollState;
    private int mRefreshState;

    private RotateAnimation mFlipAnimation;   
    private RotateAnimation mReverseFlipAnimation;

    private int mRefreshViewHeight;
    private int mRefreshOriginalTopPadding;
    private int mLastMotionY;

    private boolean mBounceHack;

    public MyRefreshListView(Context context) {
        super(context);
        init(context);
    }

    public MyRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyRefreshListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        /*设置箭头旋转动画*/
        mFlipAnimation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(250);
        mFlipAnimation.setFillAfter(true);
        mReverseFlipAnimation = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        mReverseFlipAnimation.setDuration(250);
        mReverseFlipAnimation.setFillAfter(true);

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mRefreshHeaderView = (RelativeLayout) mInflater.inflate(R.layout.pull_to_refresh_header, this, false);
        mRefreshViewText = (TextView) mRefreshHeaderView.findViewById(R.id.pull_to_refresh_text);
        mRefreshViewImage = (ImageView) mRefreshHeaderView.findViewById(R.id.pull_to_refresh_image);
        mRefreshViewProgress = (ProgressBar) mRefreshHeaderView.findViewById(R.id.pull_to_refresh_progress);
        mRefreshViewLastUpdated = (TextView) mRefreshHeaderView.findViewById(R.id.pull_to_refresh_updated_at);

        mRefreshViewImage.setMinimumHeight(50);
        mRefreshHeaderView.setOnClickListener(new OnClickRefreshListener());   /*给headerView设置点击事件监听器,这个的用处是当list的条目不足以显示一屏以上时,
        																		 headerView会直接显示出来*/
        mRefreshOriginalTopPadding = mRefreshHeaderView.getPaddingTop();    //取得headerView初始的上边距,并保存

        mRefreshState = TAP_TO_REFRESH;  //设置初始刷新状态为“点击刷新”

        addHeaderView(mRefreshHeaderView);  //将headerView添加到listView中

        super.setOnScrollListener(this);    //设置listView的滚动监听器为其本身

        measureView(mRefreshHeaderView);  //为高度测量做准备
        mRefreshViewHeight = mRefreshHeaderView.getMeasuredHeight();  //取得headerView的高度
    }

    /**
     * 自定义点击监听器，实现系统自带的监听器，执行刷新函数
     */
    private class OnClickRefreshListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (mRefreshState != REFRESHING) {
                prepareForRefresh();   //调用刷新准备函数
                onRefresh();		   //执行刷新
            }
        }

    }
    
    /*
     *刷新准备函数，执行headerView中的控件切换显示 
     */
    public void prepareForRefresh() {
        resetHeaderPadding();

        mRefreshViewImage.setVisibility(View.GONE);   //箭头提示图片隐藏
        mRefreshViewImage.setImageDrawable(null);   //需要这句因为有时候刷新时箭头图片并不会消失
        mRefreshViewProgress.setVisibility(View.VISIBLE);  //进度条转动
        mRefreshViewText.setText(R.string.pull_to_refresh_refreshing_label);  //文字提示变为“正在刷新”

        mRefreshState = REFRESHING;   //状态变量变化
    }

    /**
     * 设置headerView的初始边距
     */
    private void resetHeaderPadding() {
        mRefreshHeaderView.setPadding(
                mRefreshHeaderView.getPaddingLeft(),
                mRefreshOriginalTopPadding,
                mRefreshHeaderView.getPaddingRight(),
                mRefreshHeaderView.getPaddingBottom());
    }
    
    /**
     *刷新函数，调用 mOnRefreshListener的刷新函数
     */
    public void onRefresh() {
        Log.d(TAG, "onRefresh");

        if (mOnRefreshListener != null) {
            mOnRefreshListener.onRefresh();
        }
    }
    
    /**
     * 自定义的接口，用于刷新时回调,实例化时需要实现其中的函数,用于更新list
     */
    public interface OnRefreshListener {
        /**
         * 刷新函数，用于执行list的内容刷新,需要根据list内容不同来实现
         */
        public void onRefresh();
    }
    
    /*
     *headerView高度测量前需要调用的测量准备函数 
     */
    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0,
                0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setSelection(1);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);

        setSelection(1);
    }

    /**
     *注册滚动监听器函数
     */
    @Override
    public void setOnScrollListener(AbsListView.OnScrollListener l) {
        mOnScrollListener = l;
    }

    /**
     *注册刷新监听器，用于刷新时执行其中的函数
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int y = (int) event.getY();
        mBounceHack = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:     //如果用户已经抬起了手指
                if (!isVerticalScrollBarEnabled()) {
                    setVerticalScrollBarEnabled(true);
                }
                if (getFirstVisiblePosition() == 0 && mRefreshState != REFRESHING) {
                    if ((mRefreshHeaderView.getBottom() >= mRefreshViewHeight || mRefreshHeaderView.getTop() >= 0)   //超出了headerView的高度
                            && mRefreshState == RELEASE_TO_REFRESH) {      //并且当前状态为“松开刷新”,则开始执行刷新 
                        // Initiate the refresh
                        mRefreshState = REFRESHING;
                        prepareForRefresh();
                        onRefresh();
                    } 
                    else if (mRefreshHeaderView.getBottom() < mRefreshViewHeight || mRefreshHeaderView.getTop() <= 0) {  //如果未超出范围,则弹回
                        // Abort refresh and scroll down below the refresh view
                        resetHeader();
                        setSelection(1);
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:   //按下屏幕，则记录按下的纵坐标
                mLastMotionY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                applyHeaderPadding(event);
                break;
        }
        return super.onTouchEvent(event);
    }

    private void applyHeaderPadding(MotionEvent ev) {
        // getHistorySize has been available since API 1
        int pointerCount = ev.getHistorySize();

        for (int p = 0; p < pointerCount; p++) {
            if (mRefreshState == RELEASE_TO_REFRESH) {  //当headerView完全显示出来后，再次下拉则会显得很费力的效果
                if (isVerticalFadingEdgeEnabled()) {
                    setVerticalScrollBarEnabled(false);
                }

                int historicalY = (int) ev.getHistoricalY(p);

                // 当headerView完全显示出来后,用户的滚动距离减半显示,达到费力效果
                int topPadding = (int) (((historicalY - mLastMotionY)
                        - mRefreshViewHeight) / 2);     //因为mLastMotionY是按下时的Y值，此时headerView还未出来，所以最后的偏移距离要减去headerView的高度

                mRefreshHeaderView.setPadding(
                        mRefreshHeaderView.getPaddingLeft(),
                        topPadding,
                        mRefreshHeaderView.getPaddingRight(),
                        mRefreshHeaderView.getPaddingBottom());
            }
        }
    }

    

    /**
     * 重置headerView为初始状态
     */
    private void resetHeader() {
        if (mRefreshState != TAP_TO_REFRESH) {
            mRefreshState = TAP_TO_REFRESH;   //重置刷新状态变量为初始状态

            resetHeaderPadding();   //重置边距

            mRefreshViewText.setText(R.string.pull_to_refresh_tap_label);   //文字为“点击刷新”,如果list未占满一屏则会显示，否则不会显示出来
            // Replace refresh drawable with arrow drawable
            mRefreshViewImage.setImageResource(R.drawable.ic_pulltorefresh_arrow);
            // Clear the full rotation animation
            mRefreshViewImage.clearAnimation();
            // Hide progress bar and arrow.
            mRefreshViewImage.setVisibility(View.GONE);    
            mRefreshViewProgress.setVisibility(View.GONE);   //箭头和滚动条全部隐藏
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        /*
         *当用户滑动屏幕了,那么显示headerView的对应提示信息 
         */
        if (mCurrentScrollState == SCROLL_STATE_TOUCH_SCROLL //如果用户正在触摸屏幕、滑动、且手指还未离开屏幕
        		&& mRefreshState != REFRESHING) {
            if (firstVisibleItem == 0) {
                mRefreshViewImage.setVisibility(View.VISIBLE);
                if ((mRefreshHeaderView.getBottom() >= mRefreshViewHeight + 20
                        || mRefreshHeaderView.getTop() >= 0)
                        && mRefreshState != RELEASE_TO_REFRESH) {
                    mRefreshViewText.setText(R.string.pull_to_refresh_release_label);
                    mRefreshViewImage.clearAnimation();
                    mRefreshViewImage.startAnimation(mFlipAnimation);
                    mRefreshState = RELEASE_TO_REFRESH;
                } else if (mRefreshHeaderView.getBottom() < mRefreshViewHeight + 20
                        && mRefreshState != PULL_TO_REFRESH) {
                    mRefreshViewText.setText(R.string.pull_to_refresh_pull_label);
                    if (mRefreshState != TAP_TO_REFRESH) {
                        mRefreshViewImage.clearAnimation();
                        mRefreshViewImage.startAnimation(mReverseFlipAnimation);
                    }
                    mRefreshState = PULL_TO_REFRESH;
                }
            } 
            else {
                mRefreshViewImage.setVisibility(View.GONE);
                resetHeader();
            }
        }
        else if (mCurrentScrollState == SCROLL_STATE_FLING //如果用户刚刚触摸了屏幕、滑动、且手指已离开屏幕,表示仅是普通的滑一下,那么只是简单地将第二项显示在最上面
                && firstVisibleItem == 0
                && mRefreshState != REFRESHING) {
            setSelection(1);
            mBounceHack = true;   //反弹效果标志变量
        } 
        else if (mBounceHack && mCurrentScrollState == SCROLL_STATE_FLING) {
            setSelection(1);
        }

        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(view, firstVisibleItem,
                    visibleItemCount, totalItemCount);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mCurrentScrollState = scrollState;  //记录当前滚动状态

        if (mCurrentScrollState == SCROLL_STATE_IDLE) {  //表示空闲状态,即没有滚动操作
            mBounceHack = false;
        }

        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    
    /**
     *重载函数,用于完成刷新后的操作：更新刷新日期（如果有的话）和调用另一个无参数兄弟函数
     */
    public void onRefreshComplete(String lastUpdated) {
        setLastUpdated(lastUpdated);
        onRefreshComplete();
    }

    /**
     * 设置显示最后刷新时间
     */
    public void setLastUpdated(String lastUpdated) {
        if (lastUpdated != null) {
            mRefreshViewLastUpdated.setVisibility(View.VISIBLE);
            mRefreshViewLastUpdated.setText(lastUpdated);
        } else {
            mRefreshViewLastUpdated.setVisibility(View.GONE);
        }
    }
    
    /**
     * 重载函数,用于完成刷新后的操作：重置headerView
     */
    public void onRefreshComplete() {        
        Log.d(TAG, "onRefreshComplete");
        resetHeader();

        //如果headerView仍可见，则显示下一项
        if (mRefreshHeaderView.getBottom() > 0) {
            invalidateViews();  //冲新加载所有view
            setSelection(1);
        }
    }
}
