package com.eqdd.wavesidebarlibrary;

/**
 * Created by lvzhihao on 17-5-13.
 */

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 波浪侧边栏
 * author: imilk
 * https://github.com/Solartisan/WaveSideBar.git
 */
public class WaveSideBarView extends View {


    private static final String TAG = "WaveSlideBarView";

    // 计算波浪贝塞尔曲线的角弧长值
    private static final double ANGLE = Math.PI * 45 / 180;
    private static final double ANGLE_R = Math.PI * 90 / 180;
    private OnTouchLetterChangeListener listener;

    // 渲染字母表
    private List<String> mLetters=new ArrayList<>();

    // 当前选中的位置
    private int mChoose = -1;

    // 字母列表画笔
    private Paint mLettersPaint = new Paint();

    // 提示字母画笔
    private Paint mTextPaint = new Paint();
    // 波浪画笔
    private Paint mWavePaint = new Paint();

    private float mTextSize;
    private float mLargeTextSize;
    private int mTextColor;
    private int mWaveColor;
    private int mTextColorChoose;
    private int mWidth;
    private int mHeight;
    private int mItemHeight;
    private int mPadding;

    // 波浪路径
    private Path mWavePath = new Path();

    // 圆形路径
    private Path mBallPath = new Path();

    // 手指滑动的Y点作为中心点
    private int mCenterY; //中心点Y

    // 贝塞尔曲线的分布半径
    private int mRadius;

    // 圆形半径
    private int mBallRadius;
    // 用于过渡效果计算
    ValueAnimator mRatioAnimator;

    // 用于绘制贝塞尔曲线的比率
    private float mRatio;

    // 选中字体的坐标
    private float mPosX, mPosY;

    // 圆形中心点X
    private float mBallCentreX;
    private Map<String, Integer> letterPositionMap = new HashMap<>();
    private RecyclerView recyclerView;
    private boolean move;
    private LinearLayoutManager mLinearLayoutManager;
    private int mIndex;


    public WaveSideBarView(Context context) {
        this(context, null);
    }

    public WaveSideBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveSideBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public void setLetterPositionMap(Map<String, Integer> letterPositionMap) {
        this.letterPositionMap = letterPositionMap;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        if (this.recyclerView!=recyclerView) {
            this.recyclerView = recyclerView;
            mLinearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    //在这里进行第二次滚动（最后的100米！）
                    if (move) {
                        move = false;
                        //获取要置顶的项在当前屏幕的位置，mIndex是记录的要置顶项在RecyclerView中的位置
                        int n = mIndex - mLinearLayoutManager.findFirstVisibleItemPosition();
                        if (0 <= n && n < recyclerView.getChildCount()) {
                            //获取要置顶的项顶部离RecyclerView顶部的距离
                            int top = recyclerView.getChildAt(n).getTop();
                            //最后的移动
                            recyclerView.scrollBy(0, top);
                        }
                    }
                }
            });
        }
    }

    private void init(Context context, AttributeSet attrs) {
        List<String> strings = Arrays.asList(context.getResources().getStringArray(com.eqdd.wavesidebarlibrary.R.array.waveSideBarLetters));
        mLetters.addAll(strings);

        mTextColor = Color.parseColor("#969696");
        mWaveColor = Color.parseColor("#be69be91");
        mTextColorChoose = context.getResources().getColor(android.R.color.white);
        mTextSize = context.getResources().getDimensionPixelSize(com.eqdd.wavesidebarlibrary.R.dimen.textSize_sidebar);
        mLargeTextSize = context.getResources().getDimensionPixelSize(com.eqdd.wavesidebarlibrary.R.dimen.large_textSize_sidebar);
        mPadding = context.getResources().getDimensionPixelSize(com.eqdd.wavesidebarlibrary.R.dimen.textSize_sidebar_padding);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, com.eqdd.wavesidebarlibrary.R.styleable.WaveSideBarView);
            mTextColor = a.getColor(com.eqdd.wavesidebarlibrary.R.styleable.WaveSideBarView_sidebarTextColor, mTextColor);
            mTextColorChoose = a.getColor(com.eqdd.wavesidebarlibrary.R.styleable.WaveSideBarView_sidebarChooseTextColor, mTextColorChoose);
            mTextSize = a.getFloat(com.eqdd.wavesidebarlibrary.R.styleable.WaveSideBarView_sidebarTextSize, mTextSize);
            mLargeTextSize = a.getFloat(com.eqdd.wavesidebarlibrary.R.styleable.WaveSideBarView_sidebarLargeTextSize, mLargeTextSize);
            mWaveColor = a.getColor(com.eqdd.wavesidebarlibrary.R.styleable.WaveSideBarView_sidebarBackgroundColor, mWaveColor);
            mRadius = a.getInt(com.eqdd.wavesidebarlibrary.R.styleable.WaveSideBarView_sidebarRadius, context.getResources().getDimensionPixelSize(com.eqdd.wavesidebarlibrary.R.dimen.radius_sidebar));
            mBallRadius = a.getInt(com.eqdd.wavesidebarlibrary.R.styleable.WaveSideBarView_sidebarBallRadius, context.getResources().getDimensionPixelSize(com.eqdd.wavesidebarlibrary.R.dimen.ball_radius_sidebar));
            a.recycle();
        }

        mWavePaint = new Paint();
        mWavePaint.setAntiAlias(true);
        mWavePaint.setStyle(Paint.Style.FILL);
        mWavePaint.setColor(mWaveColor);

        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mTextColorChoose);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(mLargeTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     *
     * @param datas item已按照字母顺序排好序的数据
     * @param onLetterGet 获取你排序所依照的属性
     * @param from 从RecyclerView的第几项开始，一般设置头部数量
     * @param <T>
     */
    public <T> void setData(List<T> datas, OnLetterGet<T> onLetterGet, int from) {

        if (datas==null||datas.size()<=0){
            return;
        }
        mLetters.clear();
        letterPositionMap.clear();
        if (datas != null) {
            for (int i = 0; i < datas.size(); i++) {
                String headPinyin = PinYinUtil.getPinyin(onLetterGet.letterGet(datas.get(i))).substring(0, 1);
                if (i == 0) {
                    mLetters.add(headPinyin);
                    letterPositionMap.put(headPinyin, from + i);
                } else {
                    if (!headPinyin.equals(mLetters.get(mLetters.size() - 1))) {
                        mLetters.add(headPinyin);
                        letterPositionMap.put(headPinyin, from + i);
                    }
                }
            }
            resetItemHeight();
            invalidate();
        }
    }

    public interface OnLetterGet<T> {
        String letterGet(T t);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final float y = event.getY();
        final float x = event.getX();

        int startY=mHeight/2-(mItemHeight*mLetters.size())/2;
        final int oldChoose = mChoose;
        int newChoose = (int) ((y-startY) / mItemHeight);
        if (newChoose<0){
            newChoose=0;
        }
        if (newChoose>mLetters.size()-1){
            newChoose=mLetters.size()-1;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (x < mWidth - 2 * mRadius) {
                    return false;
                }
                startAnimator(mRatio, 1.0f);
                mCenterY = (int) y;
                if (oldChoose != newChoose) {
                    if (newChoose >= 0 && newChoose < mLetters.size()) {
                        mChoose = newChoose;
                        if (recyclerView != null && letterPositionMap.size() > 0) {
                            moveToPosition(letterPositionMap.get(mLetters.get(newChoose)));
//                            recyclerView.scrollToPosition(letterPositionMap.get(mLetters.get(newChoose)));
                        }
                        if (listener != null) {
                            listener.onLetterChange(mLetters.get(newChoose));
                        }
                    }
                }
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                mCenterY = (int) y;
                if (oldChoose != newChoose) {
                    if (newChoose >= 0 && newChoose < mLetters.size()) {
                        mChoose = newChoose;
                        if (recyclerView != null && letterPositionMap.size() > 0) {
                            moveToPosition(letterPositionMap.get(mLetters.get(newChoose)));
//                            recyclerView.scrollToPosition());
                        }
                        if (listener != null) {
                            listener.onLetterChange(mLetters.get(newChoose));
                        }
                    }
                }
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                startAnimator(mRatio, 0f);
                mChoose = -1;
                break;
            default:
                break;
        }
        return true;
    }

    private void moveToPosition(int n) {
        mIndex =n;
        //先从RecyclerView的LayoutManager中获取第一项和最后一项的Position
        int firstItem = mLinearLayoutManager.findFirstVisibleItemPosition();
        int lastItem = mLinearLayoutManager.findLastVisibleItemPosition();
        System.out.println(firstItem+"  "+lastItem);
        //然后区分情况
        if (n <= firstItem ){
            //当要置顶的项在当前显示的第一个项的前面时
            recyclerView.scrollToPosition(n);
        }else if ( n <= lastItem ){
            //当要置顶的项已经在屏幕上显示时
            int top = recyclerView.getChildAt(n - firstItem).getTop();
            recyclerView.scrollBy(0, top);
        }else{
            //当要置顶的项在当前显示的最后一项的后面时
            recyclerView.scrollToPosition(n);
            //这里这个变量是用在RecyclerView滚动监听里面的
            move = true;
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mWidth = getMeasuredWidth();
        //默认字母所占高度为字体大小的2倍，当字母太多时，高度平均分配
        resetItemHeight();
    }

    private void resetItemHeight() {
        if (mLetters.size()<=0){
            return;
        }
        mItemHeight = (mHeight - (int)(2*mTextSize) )/ mLetters.size();
        if (mItemHeight>=(2*mTextSize)){
            mItemHeight=(int)(2*mTextSize);
        }
        mPosX = mWidth - 1.6f * mTextSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制字母列表
        drawLetters(canvas);

        //绘制波浪
        drawWavePath(canvas);

        //绘制圆
        drawBallPath(canvas);

        //绘制选中的字体
        drawChooseText(canvas);

    }

    private void drawLetters(Canvas canvas) {

        RectF rectF = new RectF();
        rectF.left = mPosX - mTextSize;
        rectF.right = mPosX + mTextSize;
        rectF.top = 0;
        rectF.bottom = mHeight;

//        mLettersPaint.reset();
//        mLettersPaint.setStyle(Paint.Style.FILL);
//        mLettersPaint.setColor(Color.parseColor("#F9F9F9"));
//        mLettersPaint.setAntiAlias(true);
//        canvas.drawRoundRect(rectF, mTextSize, mTextSize, mLettersPaint);
//
//        mLettersPaint.reset();
//        mLettersPaint.setStyle(Paint.Style.STROKE);
//        mLettersPaint.setColor(mTextColor);
//        mLettersPaint.setAntiAlias(true);
//        canvas.drawRoundRect(rectF, mTextSize, mTextSize, mLettersPaint);

        int startY=mHeight/2-(mItemHeight*mLetters.size())/2;
        for (int i = 0; i < mLetters.size(); i++) {
            mLettersPaint.reset();
            mLettersPaint.setColor(mTextColor);
            mLettersPaint.setAntiAlias(true);
            mLettersPaint.setTextSize(mTextSize);
            mLettersPaint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fontMetrics = mLettersPaint.getFontMetrics();
            float posY = startY+mItemHeight * i +mItemHeight/2+ ((fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom);

            if (i == mChoose) {
                mPosY = posY;
                Paint paint = new Paint();
                paint.setColor(Color.parseColor("#ff0000"));
                canvas.drawRect(rectF.left ,startY+mItemHeight*i+1,rectF.right,startY+mItemHeight*(i+1)-1,paint);
            } else {

                canvas.drawText(mLetters.get(i), mPosX, posY, mLettersPaint);
            }
        }

    }

    private void drawChooseText(Canvas canvas) {
        if (mChoose != -1) {
            // 绘制右侧选中字符
            mLettersPaint.reset();
            mLettersPaint.setColor(mTextColorChoose);
            mLettersPaint.setTextSize(mTextSize);
            mLettersPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(mLetters.get(mChoose), mPosX, mPosY, mLettersPaint);

            // 绘制提示字符
            if (mRatio >= 0.9f) {
                String target = mLetters.get(mChoose);
                Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
                float baseline = Math.abs(-fontMetrics.bottom - fontMetrics.top);
                float x = mBallCentreX;
                float y = mCenterY + baseline / 2;
                canvas.drawText(target, x, y, mTextPaint);
            }
        }
    }

    /**
     * 绘制波浪
     *
     * @param canvas
     */
    private void drawWavePath(Canvas canvas) {
        mWavePath.reset();
        // 移动到起始点
        mWavePath.moveTo(mWidth, mCenterY - 3 * mRadius);
        //计算上部控制点的Y轴位置
        int controlTopY = mCenterY - 2 * mRadius;

        //计算上部结束点的坐标
        int endTopX = (int) (mWidth - mRadius * Math.cos(ANGLE) * mRatio);
        int endTopY = (int) (controlTopY + mRadius * Math.sin(ANGLE));
        mWavePath.quadTo(mWidth, controlTopY, endTopX, endTopY);

        //计算中心控制点的坐标
        int controlCenterX = (int) (mWidth - 1.8f * mRadius * Math.sin(ANGLE_R) * mRatio);
        int controlCenterY = mCenterY;
        //计算下部结束点的坐标
        int controlBottomY = mCenterY + 2 * mRadius;
        int endBottomX = endTopX;
        int endBottomY = (int) (controlBottomY - mRadius * Math.cos(ANGLE));
        mWavePath.quadTo(controlCenterX, controlCenterY, endBottomX, endBottomY);

        mWavePath.quadTo(mWidth, controlBottomY, mWidth, controlBottomY + mRadius);

        mWavePath.close();
        canvas.drawPath(mWavePath, mWavePaint);
    }

    private void drawBallPath(Canvas canvas) {
        //x轴的移动路径
        mBallCentreX = (mWidth + mBallRadius) - (2.0f * mRadius + 2.0f * mBallRadius) * mRatio;

        mBallPath.reset();
        mBallPath.addCircle(mBallCentreX, mCenterY, mBallRadius, Path.Direction.CW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mBallPath.op(mWavePath, Path.Op.DIFFERENCE);
        }

        mBallPath.close();
        canvas.drawPath(mBallPath, mWavePaint);

    }


    private void startAnimator(float... value) {
        if (mRatioAnimator == null) {
            mRatioAnimator = new ValueAnimator();
        }
        mRatioAnimator.cancel();
        mRatioAnimator.setFloatValues(value);
        mRatioAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator value) {
                mRatio = (float) value.getAnimatedValue();
                invalidate();
            }
        });
        mRatioAnimator.start();
    }


    public void setOnTouchLetterChangeListener(OnTouchLetterChangeListener listener) {
        this.listener = listener;
    }

    public List<String> getLetters() {
        return mLetters;
    }

    public void setLetters(List<String> letters) {
        this.mLetters = letters;
        invalidate();
    }

    public interface OnTouchLetterChangeListener {
        void onLetterChange(String letter);
    }
}