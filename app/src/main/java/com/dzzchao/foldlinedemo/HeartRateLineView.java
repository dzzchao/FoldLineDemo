package com.dzzchao.foldlinedemo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.dzzchao.foldlinedemo.ConvertUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author chao.zhang
 */
public class HeartRateLineView extends View {
    private final String TAG = "HeartRateLine";
    private final Context mContext;
    private Paint mPaint = new Paint();
    private TextPaint mTextPaint = new TextPaint();
    /**
     * 某时刻心率数据展示框的高
     */
    private float mSelectRateShowHeight;
    /**
     * 某时刻心率数据展示框的宽
     */
    private float mSelectRateShowWidth;
    /**
     * 某时刻心率数据的值
     */
    private int mSelectRateShowValue = 0;
    /**
     * 某时刻的时间
     */
    private String mSelectRateShowTime = "";

    /**
     * X坐标轴的高度
     */
    private float mXHeight;

    /**
     * Y坐标轴的宽度
     */
    private float mYWidth;

    /**
     * 心率数据
     */
    private TreeMap<Float, Integer> mMapHeartrateData = new TreeMap<>();

    /**
     * 心率的x坐标轴集合
     */
    private List<Float> mListHeartrateDataX = new LinkedList<>();

    /**
     * 心率值集合
     */
    private List<Float> mListHeartrateDataTime = new LinkedList<>();


    private float mVerticalLineX = -1000;   // 纵线x轴坐标

    private boolean mIsConsumeEvent = false;    // 是否消费事件
    private float mNowKey;  // 当前的Key

    public HeartRateLineView(Context context) {
        this(context, null);
    }

    public HeartRateLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeartRateLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mSelectRateShowHeight = ConvertUtil.dp2px(context, 50);
        mSelectRateShowWidth = ConvertUtil.dp2px(context, 100);
        mXHeight = ConvertUtil.dp2px(context, 30);
        mYWidth = ConvertUtil.dp2px(context, 30);
    }

    /**
     * 设置心率数据
     */
    public void setMapHeartrateData(Map<Float, Integer> mapHeartrateData) {
        if (mapHeartrateData == null || mapHeartrateData.size() < 1) {
            Log.e(TAG, "传过来的心率数据为空！");
            return;
        }
        this.mMapHeartrateData.clear();
        this.mMapHeartrateData.putAll(mapHeartrateData);
        if (mMapHeartrateData.size() == 1) {
            invalidate();
        } else {
            startAnimator();
        }
    }

    public void setmIsConsumeEvent(boolean isConsumeEvent) {
        this.mIsConsumeEvent = isConsumeEvent;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        float width = getWidth();
        float height = getHeight();


        //处理 padding

        float paddingLeft = (getPaddingLeft() == 0) ? ConvertUtil.dp2px(mContext, 30) : getPaddingLeft();
        float paddingRight = (getPaddingRight() == 0) ? ConvertUtil.dp2px(mContext, 30) : getPaddingRight();

//        float paddingLeft = getPaddingLeft();
//        float paddingRight = getPaddingRight();

        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();


        Log.d(TAG, "onDraw: paddingLeft:" + paddingLeft + " paddingRight:" + paddingRight +
                " paddingTop:" + paddingTop + " paddingBottom:" + paddingBottom +
                " width:" + width + " height:" + height);

        // Y轴
        drawY(canvas, paddingLeft, paddingTop + mSelectRateShowHeight, paddingLeft + mYWidth, height - paddingBottom - mXHeight);
        // X轴
        drawX(canvas, paddingLeft + mYWidth, height - paddingBottom - mXHeight, width - paddingRight, height - paddingBottom);

        //心率数据展示框
        drawHeartrateLine(canvas, paddingLeft + mYWidth, paddingTop + mSelectRateShowHeight, width - paddingRight, height - paddingBottom - mXHeight);

        if (mVerticalLineX >= paddingLeft + mYWidth && mVerticalLineX <= width - paddingRight && mIsConsumeEvent) {
            drawSelectRateResult(canvas, mVerticalLineX - mSelectRateShowWidth / 2, paddingTop, mVerticalLineX + mSelectRateShowWidth / 2, paddingTop + mSelectRateShowHeight);
            drawVerticalLine(canvas, mVerticalLineX, paddingTop + mSelectRateShowHeight, height - paddingBottom - mXHeight);
        } else {
            drawTextSlidingToShow(canvas, paddingLeft, paddingTop, width - paddingRight, paddingTop + mSelectRateShowHeight);
        }
    }


    /**
     * 绘制左侧Y轴的标注
     *
     * @param left   绘制区域的左侧坐标
     * @param top    绘图区域的顶部坐标
     * @param right  绘制区域的右侧坐标
     * @param bottom 绘制区域的底部坐标
     */
    private void drawY(Canvas canvas, float left, float top, float right, float bottom) {
        mTextPaint.reset();
        mTextPaint.setTextSize(ConvertUtil.sp2px(mContext, 12));
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(Color.parseColor("#CCFFFFFF"));
        // 设置抗锯齿
        mTextPaint.setAntiAlias(true);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float centerLine = (fontMetrics.bottom + fontMetrics.top) / 2;
        int textContent = 30;
        float centerX = (left + right) / 2;
        float unitY = (bottom - top) / 6;
        for (int i = 1; i <= 6; i++) {
            float textY = bottom - centerLine - unitY * i;
            canvas.drawText(textContent + "", centerX, textY, mTextPaint);
            textContent += 30;
        }
    }

    /**
     * 绘制X轴坐标上的点和数据
     *
     * @param left   绘制区域的左侧坐标
     * @param top    绘图区域的顶部坐标
     * @param right  绘制区域的右侧坐标
     * @param bottom 绘制区域的底部坐标
     */
    private void drawX(Canvas canvas, float left, float top, float right, float bottom) {
        mTextPaint.reset();
        mPaint.reset();
        mTextPaint.setTextSize(ConvertUtil.sp2px(mContext, 14));
        mTextPaint.setColor(Color.parseColor("#CCFFFFFF"));
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setAntiAlias(true);

        mPaint.setColor(Color.parseColor("#CCFFFFFF"));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float baseLine = (fontMetrics.bottom + fontMetrics.top) / 2;
        float radiusX = ConvertUtil.dp2px(mContext, 2);
        float radius = radiusX;
        float unitX = (right - left - radius * 2) / 24;

        for (int i = 1; i <= 24; i++) {
            float x = left + unitX * i + radiusX;
            if (i % 5 == 0) {
                radius = radiusX * 2;
                canvas.drawText(i + "h", x, bottom + baseLine, mTextPaint);
            } else {
                radius = radiusX;
            }
            canvas.drawCircle(x, top, radius, mPaint);
        }
        mTextPaint.reset();
        mPaint.reset();
    }

    /**
     * 画心率折线
     *
     * @param left   绘制区域的左侧坐标
     * @param top    绘图区域的顶部坐标
     * @param right  绘制区域的右侧坐标
     * @param bottom 绘制区域的底部坐标
     */
    private void drawHeartrateLine(Canvas canvas, float left, float top, float right, float bottom) {
        mTextPaint.reset();
        mPaint.reset();

        mTextPaint.setTextSize(ConvertUtil.sp2px(mContext, 14));
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float baseLine = (fontMetrics.bottom + fontMetrics.top) / 2;
        float radios = ConvertUtil.dp2px(mContext, 3);
        float unitX = (right - left - radios * 2) / 24;
        float unitY = (bottom - top + baseLine) / 180;

        int maxRateValue = 0;   // 最大心率值

        // 绘制参考线
        mPaint.setColor(Color.parseColor("#E88D5C"));
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(ConvertUtil.dp2px(mContext, 1));
        float[] lines = new float[]{left, bottom - 60 * unitY, right, bottom - 60 * unitY
                , left, bottom - 100 * unitY, right, bottom - 100 * unitY
        };
        canvas.drawLines(lines, mPaint);

        if (mMapHeartrateData == null || mMapHeartrateData.size() < 1) {
            return;
        } else if (mMapHeartrateData.size() == 1) {
            mPaint.reset();
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.WHITE);
            Float key = mMapHeartrateData.firstKey();
            float x = key * unitX + left + radios;
            mListHeartrateDataX.clear();
            mListHeartrateDataTime.clear();
            mListHeartrateDataX.add(x);
            mListHeartrateDataTime.add(key);
            canvas.drawCircle(x, bottom - mMapHeartrateData.get(key) * unitY, radios, mPaint);
            mPaint.reset();
            return;
        }

        mPaint.reset();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(ConvertUtil.dp2px(mContext, 2));
        mPaint.setAntiAlias(true);

        float firstKey = mMapHeartrateData.firstKey();

        Path path = new Path();
        mPaint.setStyle(Paint.Style.STROKE);
        mListHeartrateDataX.clear();
        mListHeartrateDataTime.clear();
        for (Float key : mMapHeartrateData.keySet()) {
            float x = key * unitX + left + radios;
            mListHeartrateDataX.add(x);
            mListHeartrateDataTime.add(key);
            if (key > mNowKey) {
                break;
            }
            if (key == firstKey) {
                path.moveTo(x, (bottom - (mMapHeartrateData.get(key)) * unitY));
            } else {
                path.lineTo(x, (bottom - (mMapHeartrateData.get(key)) * unitY));
            }
            maxRateValue = maxRateValue < mMapHeartrateData.get(key) ? mMapHeartrateData.get(key) : maxRateValue;
        }
        canvas.drawPath(path, mPaint);
        path.lineTo(mNowKey * unitX + left + radios, bottom);
        path.lineTo(firstKey * unitX + left + radios, bottom);
        path.close();
        int[] shadeColors = new int[]{Color.argb(0x99, 0xFF, 0xFF, 0xFF), Color.argb(0x00, 0xFF, 0xFF, 0xFF)};
        Shader mShader = new LinearGradient(0, bottom - maxRateValue * unitY, 0, bottom, shadeColors, null, Shader.TileMode.CLAMP);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setShader(mShader);
        canvas.drawPath(path, mPaint);

        mPaint.reset();
        mTextPaint.reset();
    }

    /**
     * 绘制选中的某时刻的心率展示的数据框
     *
     * @param canvas
     * @param left   绘制区域的左侧坐标
     * @param top    绘图区域的顶部坐标
     * @param right  绘制区域的右侧坐标
     * @param bottom 绘制区域的底部坐标
     */
    private void drawSelectRateResult(Canvas canvas, float left, float top, float right, float bottom) {
        mTextPaint.reset();
        mPaint.reset();

        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);

        float radius = (bottom - top) / 2;

        RectF rectLeft = new RectF((int) left, (int) top, (int) (left + radius * 2), (int) bottom);
        RectF rectRight = new RectF((int) (right - radius * 2), (int) top, (int) right, (int) bottom);

        Path path = new Path();
        path.moveTo(left + radius, bottom);
        path.arcTo(rectLeft, 90, 180);
        path.lineTo(right - radius, top);
        path.arcTo(rectRight, -90, 180);
        path.close();

        canvas.drawPath(path, mPaint);

        float textSize = ConvertUtil.sp2px(mContext, 14);

        mTextPaint.setColor(Color.parseColor("#FF2F63"));
        mTextPaint.setTextSize(textSize);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float baseLine = (fontMetrics.bottom + fontMetrics.top) / 2;
        canvas.drawText(mSelectRateShowValue + "", left + ConvertUtil.dp2px(mContext, 12), bottom + baseLine, mTextPaint);
        mTextPaint.reset();

        mTextPaint.setColor(Color.parseColor("#FF2F63"));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(textSize);
        fontMetrics = mTextPaint.getFontMetrics();
        baseLine = (fontMetrics.bottom + fontMetrics.top) / 2;
        mTextPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(mSelectRateShowTime, right - ConvertUtil.dp2px(mContext, 12), top - baseLine + textSize, mTextPaint);
        canvas.drawText("BPM", right - ConvertUtil.dp2px(mContext, 12), bottom + baseLine, mTextPaint);

        mTextPaint.reset();
        mPaint.reset();

    }

    /**
     * 绘制“点击滑动以查看”
     *
     * @param canvas
     * @param left   绘制区域的左侧坐标
     * @param top    绘图区域的顶部坐标
     * @param right  绘制区域的右侧坐标
     * @param bottom 绘制区域的底部坐标
     */
    private void drawTextSlidingToShow(Canvas canvas, float left, float top, float right, float bottom) {
        mTextPaint.reset();
        mTextPaint.setColor(Color.parseColor("#66FFFFFF"));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(ConvertUtil.sp2px(mContext, 14));
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float baseLine = (fontMetrics.bottom + fontMetrics.top) / 2;
        canvas.drawText("点按滑动以查看", (left + right) / 2, (top + bottom) / 2 - baseLine, mTextPaint);
        mTextPaint.reset();
    }

    /**
     * 绘制滑动的线
     *
     * @param canvas
     * @param left   绘制区域的左侧坐标
     * @param top    绘图区域的顶部坐标
     * @param bottom 绘制区域的底部坐标
     */
    private void drawVerticalLine(Canvas canvas, float left, float top, float bottom) {
        mPaint.reset();
        mPaint.setStrokeWidth(ConvertUtil.dp2px(mContext, 2));
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        canvas.drawLine(left, top, left, bottom, mPaint);
        mPaint.reset();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {// 使用wrap_content默认大小是200
            setMeasuredDimension(600, 400);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(600, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, 400);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (mListHeartrateDataX.size() < 1 || mListHeartrateDataTime.size() < 1 || mListHeartrateDataX.size() != mListHeartrateDataTime.size()) {
                    Log.e(TAG, "error data!");
                    break;
                }
                float x = event.getX();
                int size = mListHeartrateDataX.size();

                float firstX = mListHeartrateDataX.get(0);
                float lastX = mListHeartrateDataX.get(size - 1);
                if (x <= firstX) {
                    mVerticalLineX = firstX;
                    float time = mListHeartrateDataTime.get(0);
                    int minute = (int) ((time - (int) time) * 60);
                    mSelectRateShowTime = "" + (int) time + ":" + (minute > 10 ? minute : "0" + minute);
                    mSelectRateShowValue = mMapHeartrateData.get(time);
                    break;
                } else if (x >= lastX) {
                    mVerticalLineX = lastX;
                    float time = mListHeartrateDataTime.get(size - 1);
                    int minute = (int) ((time - (int) time) * 60);
                    mSelectRateShowTime = "" + (int) time + ":" + (minute > 10 ? minute : "0" + minute);
                    mSelectRateShowValue = mMapHeartrateData.get(time);
                    break;
                }
                for (int i = 0; i < size - 1; i++) {
                    float nowX = mListHeartrateDataX.get(i);
                    float nextX = mListHeartrateDataX.get(i + 1);
                    if (x >= nowX && x <= nextX) {
                        if (Math.abs(x - nowX) <= Math.abs(x - nextX)) {
                            mVerticalLineX = nowX;
                            float time = mListHeartrateDataTime.get(i);
                            int minute = (int) ((time - (int) time) * 60);
                            mSelectRateShowTime = "" + (int) time + ":" + (minute > 10 ? minute : "0" + minute);
                            mSelectRateShowValue = mMapHeartrateData.get(time);
                        } else {
                            mVerticalLineX = nextX;
                            float time = mListHeartrateDataTime.get(i + 1);
                            int minute = (int) ((time - (int) time) * 60);
                            mSelectRateShowTime = "" + (int) time + ":" + (minute > 10 ? minute : "0" + minute);
                            mSelectRateShowValue = mMapHeartrateData.get(time);
                        }
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mVerticalLineX = -1000;
                mIsConsumeEvent = false;
                break;
            default:
                break;
        }
        invalidate();
        return super.onTouchEvent(event);
    }

    /**
     * 绘制折线的动画
     */
    public void startAnimator() {
        ValueAnimator animator = ValueAnimator.ofFloat(mMapHeartrateData.firstKey(), mMapHeartrateData.lastKey());
        animator.setDuration((long) (0.1 * mMapHeartrateData.size() * 1000));
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mNowKey = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }
}
