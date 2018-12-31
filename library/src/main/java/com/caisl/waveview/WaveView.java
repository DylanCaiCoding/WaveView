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
     * 控件宽度
     */
    private int mTotalWidth;
    /**
     * 控件高度
     */
    private int mTotalHeight;
    /**
     * 是否开始动画
     */
    private boolean mStartAnim;
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
     * 水位线高度
     */
    private float mWaterLineHeight;
    /**
     * 水位线纵坐标
     */
    private float mWaterLineY;
    /**
     * 波浪振幅
     */
    private float mWaveAmplitude;
    /**
     * 波浪振幅2
     */
    private float mWaveAmplitude2;
    /**
     * 波浪波长
     */
    private float mWaveLength;
    /**
     * 波浪波长2
     */
    private float mWaveLength2;
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
    private float mLastOffset;
    /**
     * 波浪当前偏移量2
     */
    private float mLastOffset2;
    /**
     * 波浪周期时长
     */
    private int mCycleDuration;
    /**
     * 波浪周期时长2
     */
    private int mCycleDuration2;
    /**
     * 波浪移动方向（左、右）
     */
    private int mMoveDirection;
    /**
     * 波浪位置（上、下）
     */
    private int mWaveLocation;
    /**
     * 波浪绘制模式
     */
    private int mDrawMode;

    private static final int MOVE_DIRECTION_LEFT = -1;

    private static final int MOVE_DIRECTION_RIGHT = -2;

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
        mWaveLength = array.getDimension(R.styleable.WaveView_waveLength, 0);
        mWaveLength2 = array.getDimension(R.styleable.WaveView_waveLength2, 0);
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
            //如果控件高度是 wrap_content 和设置了水位线高度，则高度设为水位线高度 + 最大振幅
            int height = (int) (mWaterLineHeight + Math.max(mWaveAmplitude, mWaveAmplitude2));
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mTotalWidth = getMeasuredWidth();
        mTotalHeight = getMeasuredHeight();

        if (mWaterLineHeight == 0) {
            //如果没有设置水位线高度，则水位线纵坐标为 控件高度 - 最大振幅
            if (mWaveLocation == WAVE_LOCATION_TOP) {
                mWaterLineY = mTotalHeight - Math.max(mWaveAmplitude, mWaveAmplitude2);
            } else {
                mWaterLineY = Math.max(mWaveAmplitude, mWaveAmplitude2);
            }
        } else {
            if (mWaveLocation == WAVE_LOCATION_TOP) {
                mWaterLineY = mWaterLineHeight;
            } else {
                mWaterLineY = mTotalHeight - mWaterLineHeight;
            }
        }

        if (mWaveLength == 0) {
            //如果没有设置波长，则波长为总宽度
            mWaveLength = mTotalWidth;
        }
        if (mDefOffset >= mWaveLength) {
            //处理默认偏移量，不超过波长大小
            mDefOffset = mDefOffset % mWaveLength;
        }

        if (mWaveCount > 1) {
            if (mWaveLength2 == 0) {
                //如果没有设置第二个波长，则第二个波长为第一个波长
                mWaveLength2 = mWaveLength;
            }
            if (mDefOffset2 >= mWaveLength2) {
                //处理第二个默认偏移量，不超过第二个波长大小
                mDefOffset2 = mDefOffset2 % mWaveLength2;
            }
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
            canvas.drawPath(getPath(mWaveLength2, mWaveAmplitude2, mOffset2), mPaint);
        }
        mPaint.setColor(mWaveColor);
        canvas.drawPath(getPath(mWaveLength, mWaveAmplitude, mOffset), mPaint);
    }

    /**
     * 获取路径
     *
     * @param waveLength    波长
     * @param waveAmplitude 振幅
     * @param offset        偏移量
     * @return 路径
     */
    private Path getPath(float waveLength, float waveAmplitude, float offset) {
        mPath.reset();
        // TODO: 2018/12/30 增加初始偏移量
        //计算波浪曲线路径
        if (mDrawMode == DRAW_MODE_BEZIER) { //用贝塞尔曲线计算波浪曲线路径
            float startX;
            float controlPointsY;
            float halfWaveLength = waveLength / 2;
            int extraHalfWaveCount = 2;
            int totalHalfWaveCount = (int) (mTotalWidth / halfWaveLength) + 1 + extraHalfWaveCount;

            if (mMoveDirection == MOVE_DIRECTION_LEFT) {
                startX = -offset;
            } else {
                startX = halfWaveLength * -extraHalfWaveCount + offset;
            }
            mPath.moveTo(startX, mWaterLineY);

            for (int i = 0; i < totalHalfWaveCount; i++) {
                controlPointsY = getControlPointsY(waveAmplitude, i);
                mPath.rQuadTo(halfWaveLength / 2, controlPointsY, halfWaveLength, 0);
            }
        } else { //用三角函数计算波浪曲线路径
            if (mMoveDirection == MOVE_DIRECTION_RIGHT) {
                offset = -offset;
            }

            int x = 0;
            float y;
            while (x < mTotalWidth) {
                y = getTrigonometricY(waveLength, waveAmplitude, offset, x);
                mPath.lineTo(x, y);
                x++;
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
     * 获取贝塞尔控制点纵坐标
     *
     * @param waveAmplitude 波浪振幅
     * @param halfWaveIndex 半波浪的索引位置
     * @return 控制点纵坐标
     */
    private float getControlPointsY(float waveAmplitude, int halfWaveIndex) {
        int indexDirection = halfWaveIndex % 2 == 0 ? 1 : -1;
        return waveAmplitude * indexDirection * 2;
    }

    /**
     * 获取三角函数纵坐标
     *
     * @param waveLength    波长
     * @param waveAmplitude 振幅
     * @param offset        偏移量
     * @param x             横坐标
     * @return 三角函数纵坐标
     */
    private float getTrigonometricY(float waveLength, float waveAmplitude, float offset, int x) {
        if (waveAmplitude == 0) {
            return mWaterLineY;
        }
        double w = 2 * Math.PI / waveLength; //角速度 ω = 2π/T
        double angle = w * x + offset / waveLength * 2 * Math.PI;
        double trigonometricValue;
        if (mDrawMode == DRAW_MODE_SIN) {
            trigonometricValue = Math.sin(angle);
        } else if (mDrawMode == DRAW_MODE_COS) {
            trigonometricValue = Math.cos(angle);
        } else {
            trigonometricValue = 0;
        }
        return (float) (waveAmplitude * trigonometricValue + mWaterLineY);
    }

    /**
     * 开始动画
     */
    public void startAnim() {
        if (mWaveLength == 0 || mAnimator != null) {
            return;
        }
        mAnimator = ValueAnimator.ofFloat(0, mWaveLength);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mStartAnim) {
                    float offset = (float) animation.getAnimatedValue();
                    if (mWaveCount > 1) {
                        mOffset2 = mLastOffset2 + offset * mCycleDuration / mCycleDuration2;
                        if (mOffset2 >= mWaveLength2) {
                            mOffset2 = mOffset2 % mWaveLength2;
                        }
                    }
                    mOffset = offset + mLastOffset;
                    if (mOffset >= mWaveLength) {
                        mOffset = mOffset % mWaveLength;
                    }
                    postInvalidate();
                }
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mLastOffset = mOffset;
                mLastOffset2 = mOffset2;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                mLastOffset = mOffset;
                mLastOffset2 = mOffset2;
            }
        });
        mAnimator.setDuration(mCycleDuration);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.start();
        mStartAnim = true;
    }

    /**
     * 停止动画
     */
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

    private void changeCircleDuration(long circleDuration, long changeDuration) {
        // TODO: 2018/12/31 改变周期时长
    }

    private void changeCircleDuration2(long circleDuration, long changeDuration) {
        // TODO: 2018/12/31 改变周期时长
    }
}