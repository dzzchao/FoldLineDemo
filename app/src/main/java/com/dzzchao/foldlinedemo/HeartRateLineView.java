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


    /**
     * 绘制主颜色
     */
//    int mColor = Color.parseColor("#CCFFFFFF");
    int mColor = 0xCCFFFFFF;


    /**
     * 纵线x轴坐标
     */
    private float mVerticalLineX = -1;

    /**
     * 是否消费事件
     */
    private boolean mIsConsumeEvent = false;
    /**
     * 当前的Key
     */
    private float mNowKey = 0;
    /**
     * 绘制字体大小
     */
    private int mTextSize;

    public HeartRateLineView(Context context) {
        this(context, null);
    }

    public HeartRateLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeartRateLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mSelectRateShowHeight = ConvertUtil.dp2px(context, 30);
        mSelectRateShowWidth = ConvertUtil.dp2px(context, 80);
        mXHeight = ConvertUtil.dp2px(context, 30);
        mYWidth = ConvertUtil.dp2px(context, 30);
        mTextSize = ConvertUtil.sp2px(mContext, 12);
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(ConvertUtil.dp2px(mContext, 360), ConvertUtil.dp2px(mContext, 360));
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(ConvertUtil.dp2px(mContext, 360), heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, ConvertUtil.dp2px(mContext, 360));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        //处理 padding
        float paddingLeft = (getPaddingLeft() == 0) ? ConvertUtil.dp2px(mContext, 30) : getPaddingLeft();
        float paddingRight = (getPaddingRight() == 0) ? ConvertUtil.dp2px(mContext, 30) : getPaddingRight();
        float paddingTop = (getPaddingRight() == 0) ? ConvertUtil.dp2px(mContext, 30) : getPaddingRight();
        float paddingBottom = (getPaddingRight() == 0) ? ConvertUtil.dp2px(mContext, 30) : getPaddingRight();


        Log.d(TAG, "onDraw: paddingLeft:" + paddingLeft + " paddingRight:" + paddingRight +
                " paddingTop:" + paddingTop + " paddingBottom:" + paddingBottom +
                " width:" + width + " height:" + height);

        // Y轴
        drawY(canvas, paddingLeft, paddingTop + mSelectRateShowHeight,
                paddingLeft + mYWidth, height - paddingBottom - mXHeight);
        // X轴
        drawX(canvas, paddingLeft + mYWidth, height - paddingBottom - mXHeight,
                width - paddingRight, height - paddingBottom);

        //心率数据展示框
        drawHeartrateLine(canvas, paddingLeft + mYWidth, paddingTop + mSelectRateShowHeight,
                width - paddingRight, height - paddingBottom - mXHeight);

        if (mVerticalLineX >= paddingLeft + mYWidth && mVerticalLineX <= width - paddingRight && mIsConsumeEvent) {
            //心率数值详情框
            drawSelectRateResult(canvas, mVerticalLineX - mSelectRateShowWidth / 2, paddingTop,
                    mVerticalLineX + mSelectRateShowWidth / 2, paddingTop + mSelectRateShowHeight);
            //数值框下面的线
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
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(mColor);
        // 设置抗锯齿
        mTextPaint.setAntiAlias(true);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float unitY = (bottom - top) / 6;
        //baseline 公式 int baseline = height/2 + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent,height 是文字区域的高度
        float baseLine = (fontMetrics.bottom - fontMetrics.top) / 2 + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
        // 文字内容
        int textContent = 30;
        for (int i = 1; i <= 6; i++) {
            // Y值在 baseline 上，最后减掉的是字体一半的高度
            float textY = bottom - unitY * i + baseLine - (fontMetrics.bottom - fontMetrics.top) / 2;
            //X坐标为x轴绘制区域的中心点，因为设置了mTextPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(textContent + "", (right + left) / 2, textY, mTextPaint);
            textContent += 30;
        }

        // 辅助绘制的矩形框，正式版注释掉
//        mPaint.reset();
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setColor(mColor);
//        canvas.drawRect(left, top, right, bottom, mPaint);
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

        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setAntiAlias(true);

        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        //宽度分成24份
        float unitX = (right - left) / 24;
        //半径距离
        float radius;
        for (int i = 1; i <= 24; i++) {
            float x = left + unitX * i;
            if (i % 5 == 0) {
                //当为5的倍数的时候，半径 * 2
                radius = ConvertUtil.dp2px(mContext, 4);
                // 画文字，用bottom当baseline
                canvas.drawText(i + "h", x, bottom, mTextPaint);
            } else {
                radius = ConvertUtil.dp2px(mContext, 2);
            }
            canvas.drawCircle(x, top, radius, mPaint);
        }

        //辅助绘制的矩形框
//        mPaint.reset();
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setColor(mColor);
//        canvas.drawRect(left, top, right, bottom, mPaint);
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
        mPaint.reset();

        //宽度的一个单位，最大是24个单位
        float unitX = (right - left) / 24;
        //高度的一个单位，最大是180个单位
        float unitY = (bottom - top) / 180;

        // 心率最高值，用来绘制渐变
        int maxRateValue = 0;

        // 绘制参考线 60-100
        mPaint.setColor(Color.parseColor("#E88D5C"));
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(ConvertUtil.dp2px(mContext, 1));
        float[] lines = new float[]{left, bottom - 60 * unitY, right, bottom - 60 * unitY,
                left, bottom - 100 * unitY, right, bottom - 100 * unitY};
        canvas.drawLines(lines, mPaint);


        //如果只有一条数据，就画一个点
        if (mMapHeartrateData.size() == 1) {
            mPaint.reset();
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.WHITE);
            mPaint.setAntiAlias(true);
            Float key = mMapHeartrateData.firstKey();
            float x = key * unitX + left;
            mListHeartrateDataX.clear();
            mListHeartrateDataTime.clear();
            mListHeartrateDataX.add(x);
            mListHeartrateDataTime.add(key);
            canvas.drawCircle(x, bottom - mMapHeartrateData.get(key) * unitY, ConvertUtil.dp2px(mContext, 2), mPaint);
            mPaint.reset();
            return;
        }

        //画折线
        mPaint.reset();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(ConvertUtil.dp2px(mContext, 2));
        mPaint.setAntiAlias(true);
        Path path = new Path();
        mListHeartrateDataX.clear();
        mListHeartrateDataTime.clear();
        float firstKey = mMapHeartrateData.firstKey();
        for (Float key : mMapHeartrateData.keySet()) {
            float x = key * unitX + left;
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


        //draw 渐变色
        path.lineTo(mNowKey * unitX + left, bottom);
        path.lineTo(firstKey * unitX + left, bottom);
        path.close();
        int[] shadeColors = new int[]{Color.argb(0x99, 0xFF, 0xFF, 0xFF), Color.argb(0x00, 0xFF, 0xFF, 0xFF)};
        Shader mShader = new LinearGradient(0, bottom - maxRateValue * unitY, 0, bottom, shadeColors, null, Shader.TileMode.CLAMP);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setShader(mShader);
        canvas.drawPath(path, mPaint);

        //辅助绘制的矩形框
//        mPaint.reset();
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setColor(mColor);
//        canvas.drawRect(left, top, right, bottom, mPaint);
    }

    /**
     * 绘制选中的某时刻的心率展示的数据框
     *
     * @param left   绘制区域的左侧坐标
     * @param top    绘图区域的顶部坐标
     * @param right  绘制区域的右侧坐标
     * @param bottom 绘制区域的底部坐标
     */
    private void drawSelectRateResult(Canvas canvas, float left, float top, float right, float bottom) {
        mTextPaint.reset();
        mPaint.reset();

        //绘制胶囊形状View
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        float radius = (bottom - top) / 2;
        RectF rectLeft = new RectF(left, top, left + radius * 2, bottom);
        RectF rectRight = new RectF(right - radius * 2, top, right, bottom);
        Path path = new Path();
        path.moveTo(left + radius, bottom);
        path.arcTo(rectLeft, 90, 180);
        path.lineTo(right - radius, top);
        path.arcTo(rectRight, -90, 180);
        path.close();
        canvas.drawPath(path, mPaint);

        //draw 心率值
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.parseColor("#FF2F63"));
        mTextPaint.setTextSize(mTextSize);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float baseLine = (bottom + top) / 2 + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
        canvas.drawText(mSelectRateShowValue + "", left + ConvertUtil.dp2px(mContext, 12), baseLine, mTextPaint);
        mTextPaint.setTextAlign(Paint.Align.RIGHT);

        baseLine = ((bottom + top) / 2 + top) / 2 + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
        canvas.drawText(mSelectRateShowTime, right - ConvertUtil.dp2px(mContext, 12), baseLine, mTextPaint);
        baseLine = ((bottom + top) / 2 + bottom) / 2 + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
        canvas.drawText("BPM", right - ConvertUtil.dp2px(mContext, 12), baseLine, mTextPaint);

        // 辅助绘制的矩形框
//        mPaint.reset();
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setColor(mColor);
//        canvas.drawRect(left, top, right, bottom, mPaint);
    }

    /**
     * 绘制“点击滑动以查看”
     *
     * @param left   绘制区域的左侧坐标
     * @param top    绘图区域的顶部坐标
     * @param right  绘制区域的右侧坐标
     * @param bottom 绘制区域的底部坐标
     */
    private void drawTextSlidingToShow(Canvas canvas, float left, float top, float right, float bottom) {
        mTextPaint.reset();
        mTextPaint.setColor(Color.parseColor("#66FFFFFF"));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float baseLine = (bottom + top) / 2 + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
        canvas.drawText("点按滑动以查看", (left + right) / 2, baseLine, mTextPaint);
        mTextPaint.reset();

        // 辅助绘制的矩形框
//        mPaint.reset();
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setColor(mColor);
//        canvas.drawRect(left, top, right, bottom, mPaint);
//        canvas.drawRect(left, top, right, (bottom + top) / 2, mPaint);
    }

    /**
     * 绘制滑动的线
     *
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
                for (int i = 1; i < size - 1; i++) {
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
                mVerticalLineX = -1;
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
        animator.setDuration(1000L);
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
