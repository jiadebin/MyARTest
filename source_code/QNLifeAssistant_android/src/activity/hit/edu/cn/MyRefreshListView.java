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

	/*״̬���룬���ĸ�״̬*/
    private static final int TAP_TO_REFRESH = 1;
    private static final int PULL_TO_REFRESH = 2;
    private static final int RELEASE_TO_REFRESH = 3;
    private static final int REFRESHING = 4;

    private static final String TAG = "PullToRefreshListView";

    /*ˢ�»ص��ӿ�*/
    private OnRefreshListener mOnRefreshListener;

    /**
     * �б�����ļ�����
     */
    private OnScrollListener mOnScrollListener;
    private LayoutInflater mInflater;

    private RelativeLayout mRefreshHeaderView;   //headerView����
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
        /*���ü�ͷ��ת����*/
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
        mRefreshHeaderView.setOnClickListener(new OnClickRefreshListener());   /*��headerView���õ���¼�������,������ô��ǵ�list����Ŀ��������ʾһ������ʱ,
        																		 headerView��ֱ����ʾ����*/
        mRefreshOriginalTopPadding = mRefreshHeaderView.getPaddingTop();    //ȡ��headerView��ʼ���ϱ߾�,������

        mRefreshState = TAP_TO_REFRESH;  //���ó�ʼˢ��״̬Ϊ�����ˢ�¡�

        addHeaderView(mRefreshHeaderView);  //��headerView��ӵ�listView��

        super.setOnScrollListener(this);    //����listView�Ĺ���������Ϊ�䱾��

        measureView(mRefreshHeaderView);  //Ϊ�߶Ȳ�����׼��
        mRefreshViewHeight = mRefreshHeaderView.getMeasuredHeight();  //ȡ��headerView�ĸ߶�
    }

    /**
     * �Զ�������������ʵ��ϵͳ�Դ��ļ�������ִ��ˢ�º���
     */
    private class OnClickRefreshListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (mRefreshState != REFRESHING) {
                prepareForRefresh();   //����ˢ��׼������
                onRefresh();		   //ִ��ˢ��
            }
        }

    }
    
    /*
     *ˢ��׼��������ִ��headerView�еĿؼ��л���ʾ 
     */
    public void prepareForRefresh() {
        resetHeaderPadding();

        mRefreshViewImage.setVisibility(View.GONE);   //��ͷ��ʾͼƬ����
        mRefreshViewImage.setImageDrawable(null);   //��Ҫ�����Ϊ��ʱ��ˢ��ʱ��ͷͼƬ��������ʧ
        mRefreshViewProgress.setVisibility(View.VISIBLE);  //������ת��
        mRefreshViewText.setText(R.string.pull_to_refresh_refreshing_label);  //������ʾ��Ϊ������ˢ�¡�

        mRefreshState = REFRESHING;   //״̬�����仯
    }

    /**
     * ����headerView�ĳ�ʼ�߾�
     */
    private void resetHeaderPadding() {
        mRefreshHeaderView.setPadding(
                mRefreshHeaderView.getPaddingLeft(),
                mRefreshOriginalTopPadding,
                mRefreshHeaderView.getPaddingRight(),
                mRefreshHeaderView.getPaddingBottom());
    }
    
    /**
     *ˢ�º��������� mOnRefreshListener��ˢ�º���
     */
    public void onRefresh() {
        Log.d(TAG, "onRefresh");

        if (mOnRefreshListener != null) {
            mOnRefreshListener.onRefresh();
        }
    }
    
    /**
     * �Զ���Ľӿڣ�����ˢ��ʱ�ص�,ʵ����ʱ��Ҫʵ�����еĺ���,���ڸ���list
     */
    public interface OnRefreshListener {
        /**
         * ˢ�º���������ִ��list������ˢ��,��Ҫ����list���ݲ�ͬ��ʵ��
         */
        public void onRefresh();
    }
    
    /*
     *headerView�߶Ȳ���ǰ��Ҫ���õĲ���׼������ 
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
     *ע���������������
     */
    @Override
    public void setOnScrollListener(AbsListView.OnScrollListener l) {
        mOnScrollListener = l;
    }

    /**
     *ע��ˢ�¼�����������ˢ��ʱִ�����еĺ���
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int y = (int) event.getY();
        mBounceHack = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:     //����û��Ѿ�̧������ָ
                if (!isVerticalScrollBarEnabled()) {
                    setVerticalScrollBarEnabled(true);
                }
                if (getFirstVisiblePosition() == 0 && mRefreshState != REFRESHING) {
                    if ((mRefreshHeaderView.getBottom() >= mRefreshViewHeight || mRefreshHeaderView.getTop() >= 0)   //������headerView�ĸ߶�
                            && mRefreshState == RELEASE_TO_REFRESH) {      //���ҵ�ǰ״̬Ϊ���ɿ�ˢ�¡�,��ʼִ��ˢ�� 
                        // Initiate the refresh
                        mRefreshState = REFRESHING;
                        prepareForRefresh();
                        onRefresh();
                    } 
                    else if (mRefreshHeaderView.getBottom() < mRefreshViewHeight || mRefreshHeaderView.getTop() <= 0) {  //���δ������Χ,�򵯻�
                        // Abort refresh and scroll down below the refresh view
                        resetHeader();
                        setSelection(1);
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:   //������Ļ�����¼���µ�������
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
            if (mRefreshState == RELEASE_TO_REFRESH) {  //��headerView��ȫ��ʾ�������ٴ���������Եúܷ�����Ч��
                if (isVerticalFadingEdgeEnabled()) {
                    setVerticalScrollBarEnabled(false);
                }

                int historicalY = (int) ev.getHistoricalY(p);

                // ��headerView��ȫ��ʾ������,�û��Ĺ������������ʾ,�ﵽ����Ч��
                int topPadding = (int) (((historicalY - mLastMotionY)
                        - mRefreshViewHeight) / 2);     //��ΪmLastMotionY�ǰ���ʱ��Yֵ����ʱheaderView��δ��������������ƫ�ƾ���Ҫ��ȥheaderView�ĸ߶�

                mRefreshHeaderView.setPadding(
                        mRefreshHeaderView.getPaddingLeft(),
                        topPadding,
                        mRefreshHeaderView.getPaddingRight(),
                        mRefreshHeaderView.getPaddingBottom());
            }
        }
    }

    

    /**
     * ����headerViewΪ��ʼ״̬
     */
    private void resetHeader() {
        if (mRefreshState != TAP_TO_REFRESH) {
            mRefreshState = TAP_TO_REFRESH;   //����ˢ��״̬����Ϊ��ʼ״̬

            resetHeaderPadding();   //���ñ߾�

            mRefreshViewText.setText(R.string.pull_to_refresh_tap_label);   //����Ϊ�����ˢ�¡�,���listδռ��һ�������ʾ�����򲻻���ʾ����
            // Replace refresh drawable with arrow drawable
            mRefreshViewImage.setImageResource(R.drawable.ic_pulltorefresh_arrow);
            // Clear the full rotation animation
            mRefreshViewImage.clearAnimation();
            // Hide progress bar and arrow.
            mRefreshViewImage.setVisibility(View.GONE);    
            mRefreshViewProgress.setVisibility(View.GONE);   //��ͷ�͹�����ȫ������
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        /*
         *���û�������Ļ��,��ô��ʾheaderView�Ķ�Ӧ��ʾ��Ϣ 
         */
        if (mCurrentScrollState == SCROLL_STATE_TOUCH_SCROLL //����û����ڴ�����Ļ������������ָ��δ�뿪��Ļ
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
        else if (mCurrentScrollState == SCROLL_STATE_FLING //����û��ոմ�������Ļ������������ָ���뿪��Ļ,��ʾ������ͨ�Ļ�һ��,��ôֻ�Ǽ򵥵ؽ��ڶ�����ʾ��������
                && firstVisibleItem == 0
                && mRefreshState != REFRESHING) {
            setSelection(1);
            mBounceHack = true;   //����Ч����־����
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
        mCurrentScrollState = scrollState;  //��¼��ǰ����״̬

        if (mCurrentScrollState == SCROLL_STATE_IDLE) {  //��ʾ����״̬,��û�й�������
            mBounceHack = false;
        }

        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    
    /**
     *���غ���,�������ˢ�º�Ĳ���������ˢ�����ڣ�����еĻ����͵�����һ���޲����ֵܺ���
     */
    public void onRefreshComplete(String lastUpdated) {
        setLastUpdated(lastUpdated);
        onRefreshComplete();
    }

    /**
     * ������ʾ���ˢ��ʱ��
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
     * ���غ���,�������ˢ�º�Ĳ���������headerView
     */
    public void onRefreshComplete() {        
        Log.d(TAG, "onRefreshComplete");
        resetHeader();

        //���headerView�Կɼ�������ʾ��һ��
        if (mRefreshHeaderView.getBottom() > 0) {
            invalidateViews();  //���¼�������view
            setSelection(1);
        }
    }
}
