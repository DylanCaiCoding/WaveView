package com.caisl.waveview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;


/**
 * Creator: caisl;
 * Date created: 2018/12/13;
 * Contact: 361366288@qq.com;
 * Description: 波浪控件
 */
public class WaveView extends View {
    private static final String TAG = "WaveView";

    /**
     * 波浪数量
     */
    private int mWaveCount;
    /**
     * 波浪颜色
     */
    private int mWaveColor;
    /**
     * 波浪颜色2
     */
    private int mWaveColor2;
    /**
     * 控件宽度
     */
    private int mTotalWidth;
    /**
     * 控件高度
     */
    private int mTotalHeight;
    /**
     * 水位线高度
     */
    private float mWaterLineHeight;
    /**
     * 波浪振幅
     */
    private float mWaveAmplitude;
    /**
     * 波浪振幅2
     */
    private float mWaveAmplitude2;
    /**
     * 波浪宽度
     */
    private float mWaveWidth;
    /**
     * 波浪宽度2
     */
    private float mWaveWidth2;
    /**
     * 波浪偏移量
     */
    private float mOffset;
    /**
     * 波浪偏移量2
     */
    private float mOffset2;
    /**
     * 波浪默认偏移量
     */
    private float mDefOffset;
    /**
     * 波浪默认偏移量2
     */
    private float mDefOffset2;
    /**
     * 波浪当前偏移量
     */
    private float mCurrentOffset;
    /**
     * 波浪当前偏移量2
     */
    private float mCurrentOffset2;
    /**
     * 波浪移动方向（左、右）
     */
    private int mMoveDirection;
    /**
     * 波浪位置（上、下）
     */
    private int mWaveLocation;
    /**
     * 波浪周期时长
     */
    private int mCycleDuration;
    /**
     * 波浪周期时长2
     */
    private int mCycleDuration2;
    /**
     * 波浪绘制模式
     */
    private int mDrawMode;
    /**
     * 是否开始动画
     */
    private boolean mStartAnim;

    private static final int MOVE_DIRECTION_LEFT = -1;

    private static final int MOVE_DIRECTION_RIGHT =-2;

    private static final int WAVE_LOCATION_TOP = -1;

    private static final int WAVE_LOCATION_BOTTOM = -2;

    private static final int DRAW_MODE_BEZIER = -1;

    private static final int DRAW_MODE_SIN = -2;

    private static final int DRAW_MODE_COS = -3;

    private ValueAnimator mAnimator;

    private Paint mPaint;

    private Path mPath;

    public WaveView(Context context) {
        this(context, null);
    }

    public WaveView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WaveView);
        mWaterLineHeight = array.getDimension(R.styleable.WaveView_waterLineHeight, 0);
        mWaveCount = array.getInteger(R.styleable.WaveView_waveCount, 1);
        mCycleDuration = array.getInteger(R.styleable.WaveView_cycleDuration, 5000);
        mCycleDuration2 = array.getInteger(R.styleable.WaveView_cycleDuration2, mCycleDuration);
        mWaveColor = array.getColor(R.styleable.WaveView_waveColor, 0x881E90FF);
        mWaveColor2 = array.getColor(R.styleable.WaveView_waveColor2, 0x881E90FF);
        mWaveAmplitude = array.getDimension(R.styleable.WaveView_waveAmplitude, 30);
        mWaveAmplitude2 = array.getDimension(R.styleable.WaveView_waveAmplitude2, 30);
        mWaveWidth = array.getDimension(R.styleable.WaveView_waveWidth, 0);
        mWaveWidth2 = array.getDimension(R.styleable.WaveView_waveWidth2, 0);
        mDefOffset = array.getDimension(R.styleable.WaveView_waveDefOffset, 0);
        mDefOffset2 = array.getDimension(R.styleable.WaveView_waveDefOffset, 0);
        mMoveDirection = array.getInt(R.styleable.WaveView_moveDirection, MOVE_DIRECTION_RIGHT);
        mWaveLocation = array.getInt(R.styleable.WaveView_waveLocation, WAVE_LOCATION_BOTTOM);
        mDrawMode = array.getInt(R.styleable.WaveView_drawMode, DRAW_MODE_BEZIER);
        mStartAnim = array.getBoolean(R.styleable.WaveView_startAnim, true);
        array.recycle();

        initViews();
    }

    private void initViews() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            //如果控件宽度是 wrap_content，就改成 match_parent
            getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT && mWaterLineHeight > 0) {
            //如果控件高度是 wrap_content 和设置了水位线高度，则高度设为水位线高度 + 振幅
            int height = (int) (mWaterLineHeight + Math.max(mWaveAmplitude, mWaveAmplitude2));
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mTotalWidth = getMeasuredWidth();
        mTotalHeight = getMeasuredHeight();
        if (mWaveWidth == 0) {
            //如果没设波浪宽度，则波浪宽度为总宽度
            mWaveWidth = mTotalWidth;
        }
        if (mWaveWidth2 == 0) {
            //如果没设第二个波浪的宽度，则第二个波浪宽度为第一个波浪宽度
            mWaveWidth2 = mWaveWidth;
        }
        if (mWaterLineHeight == 0) {
            //如果没设水位线高度，则水位线高度为 控件高度 - 振幅
            if (mWaveLocation == WAVE_LOCATION_TOP) {
                mWaterLineHeight = mTotalHeight - Math.max(mWaveAmplitude, mWaveAmplitude2);
            } else {
                mWaterLineHeight = Math.max(mWaveAmplitude, mWaveAmplitude2);
            }
        }
        if (mDefOffset >= mWaveWidth) {
            mDefOffset = mDefOffset % mWaveWidth;
        }
        if (mDefOffset2 >= mWaveWidth2) {
            mDefOffset2 = mDefOffset2 % mWaveWidth2;
        }
        if (mStartAnim) {
            startAnim();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mWaveCount > 1) {
            mPaint.setColor(mWaveColor2);
            canvas.drawPath(getPath(mWaveWidth2, mWaveAmplitude2, mOffset2), mPaint);
        }
        mPaint.setColor(mWaveColor);
        canvas.drawPath(getPath(mWaveWidth, mWaveAmplitude, mOffset), mPaint);
    }

    private Path getPath(float waveWidth, float waveAmplitude, float offset) {
        mPath.reset();

        //计算波浪线路径
        if (mDrawMode == DRAW_MODE_BEZIER) { //用贝塞尔曲线计算路径
            float halfWaveWidth = waveWidth / 2;
            if (mMoveDirection == MOVE_DIRECTION_LEFT) { //向左移动
                mPath.moveTo(-offset, mWaterLineHeight);
            } else { //向右移动
                mPath.moveTo(halfWaveWidth * -3 + offset, mWaterLineHeight); //起始坐标
            }
            for (int i = 0; i < 5; i++) {// TODO: 2018/12/30 根据 waveWidth 计算波浪总个数
                float controlPointsHeight = i % 2 == 0 ? waveAmplitude * -2 : waveAmplitude * 2;
                mPath.rQuadTo(halfWaveWidth / 2, controlPointsHeight, halfWaveWidth, 0);
            }
        } else { //用三角函数计算路径
            int index = 0;
            float w = (float) (2 * Math.PI / waveWidth); //角速度 ω = 2π/T
            if (mMoveDirection == MOVE_DIRECTION_RIGHT) {
                offset = -offset;
            }
            while (index < waveWidth) {
                float y = getTrigonometricY(waveWidth, waveAmplitude, offset, index, w);
                mPath.lineTo(index, y);
                index++;
            }
        }

        //闭合路径
        if (mWaveLocation == WAVE_LOCATION_TOP) {
            mPath.lineTo(mTotalWidth, 0);
            mPath.lineTo(0, 0);
        } else {
            mPath.lineTo(mTotalWidth, mTotalHeight);
            mPath.lineTo(0, mTotalHeight);
        }
        mPath.close();
        return mPath;
    }

    /**
     * 获取三角函数的纵坐标
     */
    private float getTrigonometricY(float waveWidth, float waveAmplitude, float offset, int index, float w) {
        double angle = w * index + offset / waveWidth * 2 * Math.PI;
        double trigonometricValue;
        float dy;
        if (mDrawMode == DRAW_MODE_SIN) {
            trigonometricValue = Math.sin(angle);
        } else if (mDrawMode == DRAW_MODE_COS) {
            trigonometricValue = Math.cos(angle);
        } else {
            return 0;
        }
        if (mWaveLocation == WAVE_LOCATION_TOP) {
            dy = mWaterLineHeight;
        }else {
            dy = Math.max(mWaveAmplitude,mWaveAmplitude2);
        }
        return (float) (waveAmplitude * trigonometricValue + dy);
    }

    public void startAnim() {
        if (mWaveWidth == 0 || mAnimator != null) {
            return;
        }
        mAnimator = ValueAnimator.ofFloat(0, mWaveWidth);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mStartAnim) {
                    float offset = (float) animation.getAnimatedValue();
                    if (mWaveCount > 1) {
                        mOffset2 = mCurrentOffset2 + offset * mCycleDuration / mCycleDuration2;
                        if (mOffset2 >= mWaveWidth2) {
                            mOffset2 = mOffset2 % mWaveWidth2;
                        }
                    }
                    mOffset = offset + mCurrentOffset;
                    if (mOffset >= mWaveWidth) {
                        mOffset = mOffset % mWaveWidth;
                    }
                    postInvalidate();
                }
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mCurrentOffset = mOffset;
                mCurrentOffset2 = mOffset2;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                mCurrentOffset2 = mOffset2;
            }
        });
        mAnimator.setDuration(mCycleDuration);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.start();
        mStartAnim = true;
    }

    public void stopAnim() {
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
            mStartAnim = false;
        }
    }

    private void moveWaterLine(float height, long duration) {
        // TODO: 2018/12/30 实现移动水位线动画
    }
}