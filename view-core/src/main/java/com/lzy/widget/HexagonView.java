package com.lzy.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 作    者：廖子尧
 * 版    本：1.0
 * 创建日期：2015/12/15
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class HexagonView extends ImageView {

    private static final double RADIAN30 = Math.PI / 6.0;   //30 弧度
    private static final ScaleType SCALE_TYPE = ScaleType.CENTER_CROP;      //只允许CENTER_CROP模式
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;  //默认创建的格式
    private static final int COLOR_DRAWABLE_DIMENSION = 2;   //对于 colorDrawable 的大小
    public static final int HORIZONTAL = 0; //水平放置
    public static final int VERTICAL = 1;   //竖直放置

    //以下是自定义属性
    private String text = "";                // 文字
    private int textSize = 12;               // 文字大小，单位sp
    private int textColor = 0xFFFFFFFF;      // 文字颜色
    private int borderWidth = 2;             // 边框宽度，单位 dp
    private int borderColor = 0xFFFF0000;    // 边框颜色
    private int fillColor = 0xFF0000FF;      // 背景颜色
    private int corner = 10;                 // 圆角度数，单位 dp
    private int breakLineCount = 4;          // 文字换行长度，单位 字节， 一个汉字占两个字节
    private int maxLine = 3;                 // 最多的行数，大于等于两行时，最后一行显示 "..."
    private int textSpacing = 4;             // 两行文字的间距，只有一行文字时无效，单位 dp
    private int hexagonOrientation = VERTICAL;   // 默认显示的文字
    private boolean borderOverlay = false;   // true表示边框会覆盖一部分图片，false表示边框不会覆盖在图片之上

    //以下是成员变量
    private final Matrix mShaderMatrix = new Matrix();  //对图片缩放的矩阵
    private final Paint mBitmapPaint = new Paint();     //图片的画笔
    private final Paint mTextPaint = new Paint();       //只有文字时文字的画笔
    private final Paint mTextBitmapPaint = new Paint();       //只有文字时文字的画笔
    private final Paint mBorderPaint = new Paint();     //边框的画笔
    private final Paint mFillPaint = new Paint();       //背景色的画笔
    private List<String> lineList = new ArrayList<>();     //每行文字的集合
    private List<Float> textBaseYList = new ArrayList<>(); //每行文字的BaseLine的集合
    private BitmapShader mBitmapShader;                 //用于绘制性状的 BitmapShader
    private Bitmap mBitmap;            //设置的图片
    private ColorFilter mColorFilter;  //滤色
    private Path mDrawPath;            //可以绘制的路径
    private Path mBorderPath;          //绘制边框的路径
    private Path mBitmapPath;          //绘制图片的路径

    private List<PointF> mDrawPathPointList;    //最外边六边形的所有点的集合
    private List<PointF> mBitmapPointList;      //最内边六边形的所有点的集合
    private float mHexagonWidth;   //六边形的真实宽
    private float mHexagonHeight;  //六边形的真实高
    private float mTranslateX;     //画布需要在X反向平移的距离
    private float mTranslateY;     //画布需要在Y方向平移的距离
    private AnimationSet mAnimationSet;    //点击时执行动画的集合
    private OnHexagonViewClickListener mListener;      //点击六边形的监听
    private boolean isLasso = false;       //是否点中多边形
    private LassoUtils mLasso;             //判断点击点是否在多边形内部的工具类
    private boolean isNeedMore;            //是否需要添加三个点

    public interface OnHexagonViewClickListener {
        void onClick(View view);
    }

    public void setOnHexagonClickListener(OnHexagonViewClickListener listener) {
        this.mListener = listener;
    }

    public HexagonView(Context context) {
        this(context, null);
    }

    public HexagonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HexagonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, getResources().getDisplayMetrics());
        borderWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, borderWidth, getResources().getDisplayMetrics());
        corner = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, corner, getResources().getDisplayMetrics());
        textSpacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textSpacing, getResources().getDisplayMetrics());

        TypedArray typeA = context.obtainStyledAttributes(attrs, R.styleable.HexagonView);
        text = typeA.getString(R.styleable.HexagonView_hexagonText);
        textSize = typeA.getDimensionPixelSize(R.styleable.HexagonView_hexagonTextSize, textSize);
        textColor = typeA.getColor(R.styleable.HexagonView_hexagonTextColor, textColor);
        borderWidth = typeA.getDimensionPixelSize(R.styleable.HexagonView_hexagonBorderWidth, borderWidth);
        borderColor = typeA.getColor(R.styleable.HexagonView_hexagonBorderColor, borderColor);
        fillColor = typeA.getColor(R.styleable.HexagonView_hexagonFillColor, fillColor);
        corner = typeA.getDimensionPixelSize(R.styleable.HexagonView_hexagonCorner, corner);
        breakLineCount = typeA.getInt(R.styleable.HexagonView_hexagonBreakLineCount, breakLineCount);
        maxLine = typeA.getInt(R.styleable.HexagonView_hexagonMaxLine, maxLine);
        textSpacing = typeA.getDimensionPixelSize(R.styleable.HexagonView_hexagonTextSpacing, textSpacing);
        borderOverlay = typeA.getBoolean(R.styleable.HexagonView_hexagonBorderOverlay, borderOverlay);
        hexagonOrientation = typeA.getInt(R.styleable.HexagonView_hexagonOrientation, hexagonOrientation);
        typeA.recycle();

        //默认不允许设置外部点击事件，如果要点击，需要使用OnHexagonViewClickListener接口
        setClickable(false);
        //初始化对触摸点坐标判断的方法
        mLasso = LassoUtils.getInstance();
        initAnimation();
    }

    /**
     * 初始化补间动画
     */
    private void initAnimation() {
        float start = 1.0f;
        float end = 0.9f;
        ScaleAnimation startAnimation = new ScaleAnimation(start, end, start, end, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        startAnimation.setDuration(30);
        ScaleAnimation endAnimation = new ScaleAnimation(end, start, end, start, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        endAnimation.setDuration(30);
        mAnimationSet = new AnimationSet(false);
        mAnimationSet.addAnimation(startAnimation);
        mAnimationSet.addAnimation(endAnimation);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 判断是否点中
                isLasso = mLasso.contains(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                //如果滑出表示取消点击
                isLasso = mLasso.contains(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                // View 中复制过来的代码，用于检测是否是点击动作，例如可能出现，按下后向上滑动，此时应不响应点击事件
                boolean focusTaken = false;
                if (isFocusable() && isFocusableInTouchMode() && !isFocused()) {
                    focusTaken = requestFocus();
                }
                //如果选中，就执行点击事件
                if (!focusTaken && isLasso && mListener != null) {
                    mListener.onClick(this);
                    startAnimation(mAnimationSet);
                }
                isLasso = false;
                break;
            default:
                isLasso = false;
                break;
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }

    private void setup() {
        if (getWidth() == 0 && getHeight() == 0) return;

        int drawWidth = Math.max(getWidth() - getPaddingLeft() - getPaddingRight(), 0);
        int drawHeight = Math.max(getHeight() - getPaddingTop() - getPaddingBottom(), 0);
        mTranslateX = getPaddingLeft();
        mTranslateY = getPaddingTop();
        if (hexagonOrientation == VERTICAL) {
            //竖直方向 高除以宽 大于 2除以根号3  表示宽度不足，这时以宽度为基准，否则以高度为基准
            if ((drawHeight * 1.0f / drawWidth) > (2.0f / Math.sqrt(3.0))) {
                mHexagonWidth = drawWidth;
                mHexagonHeight = (float) (2.0f * drawWidth / Math.sqrt(3.0));
                mTranslateY = (drawHeight - mHexagonHeight) / 2 + getPaddingTop();
            } else {
                mHexagonWidth = (float) (Math.sqrt(3.0) / 2 * drawHeight);
                mHexagonHeight = drawHeight;
                mTranslateX = (drawWidth - mHexagonWidth) / 2 + getPaddingLeft();
            }
        } else if (hexagonOrientation == HORIZONTAL) {
            //水平方向 宽除以高 大于 2除以根号3  表示高度不足，这时以高度为基准，否则以宽度为基准
            if ((drawWidth * 1.0f / drawHeight) > (2.0f / Math.sqrt(3.0))) {
                mHexagonWidth = (float) (2.0f * drawHeight / Math.sqrt(3.0));
                mHexagonHeight = drawHeight;
                mTranslateX = (drawWidth - mHexagonWidth) / 2 + getPaddingLeft();
            } else {
                mHexagonWidth = drawWidth;
                mHexagonHeight = (float) (Math.sqrt(3.0) / 2 * drawWidth);
                mTranslateY = (drawHeight - mHexagonHeight) / 2 + getPaddingTop();
            }
        }

        CornerPathEffect cornerPathEffect = new CornerPathEffect(corner);

        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setPathEffect(cornerPathEffect);

        mTextBitmapPaint.setAntiAlias(true);
        mTextBitmapPaint.setPathEffect(cornerPathEffect);

        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(borderColor);
        mBorderPaint.setStrokeWidth(borderWidth);
        mBorderPaint.setPathEffect(cornerPathEffect);

        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setAntiAlias(true);
        mFillPaint.setColor(fillColor);
        mFillPaint.setPathEffect(cornerPathEffect);

        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(textSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mDrawPath = getHexagonDrawPath(mHexagonWidth, mHexagonHeight);
        mBorderPath = getBorderPath(mDrawPathPointList, borderWidth);
        mBitmapPath = getBitmapPath(mDrawPathPointList, borderWidth);

        //以下是对文字进行换行
        if (TextUtils.isEmpty(text)) text = "";
        lineList.clear();
        breakTextLine(text, breakLineCount, maxLine - 1);
        if (isNeedMore) lineList.add("...");
        textBaseYList = getBaseLineList(lineList.size());

        //获取文字的bitmap
        Bitmap textBitmap = getTextBitmap(lineList);
        if (textBitmap != null) {
            BitmapShader textBitmapShader = new BitmapShader(textBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mTextBitmapPaint.setShader(textBitmapShader);
        }

        if (mBitmap != null) {
            mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mBitmapPaint.setShader(mBitmapShader);
            if (borderOverlay) updateShaderMatrix(mBitmap, mHexagonWidth, mHexagonHeight);
            else {
                if (hexagonOrientation == VERTICAL)
                    updateShaderMatrix(mBitmap, mBitmapPointList.get(1).x - mBitmapPointList.get(5).x, mBitmapPointList.get(3).y - mBitmapPointList.get(0).y);
                if (hexagonOrientation == HORIZONTAL)
                    updateShaderMatrix(mBitmap, mBitmapPointList.get(3).x - mBitmapPointList.get(0).x, mBitmapPointList.get(5).y - mBitmapPointList.get(1).y);
            }
        }
        mLasso.setLassoList(mDrawPathPointList);

        //通知界面重绘
        invalidate();
    }

    /**
     * 根据文字的行数，得到绘制有文字的 bitmap，为了方便用path切割图片，所以先把文字转成了bitmap
     */
    private Bitmap getTextBitmap(List<String> lineList) {
        if (mHexagonWidth <= 0 || mHexagonHeight <= 0) return null;
        Bitmap target = Bitmap.createBitmap((int) mHexagonWidth, (int) mHexagonHeight, BITMAP_CONFIG);
        Canvas canvas = new Canvas(target);
        for (int i = 0; i < lineList.size(); i++) {
            canvas.drawText(lineList.get(i), mHexagonWidth / 2, textBaseYList.get(i), mTextPaint);  //画文字
        }
        return target;
    }

    /**
     * 迭代法截断字符串
     *
     * @param text           需要换行的文本
     * @param breakLineCount 每行显示的最大数
     * @param maxLine        最多显示的行数
     */
    public void breakTextLine(String text, int breakLineCount, int maxLine) {
        if (TextUtils.isEmpty(text)) text = "";
        String line = getSubString(text, 0, breakLineCount);
        //只有一行的时候
        if (maxLine == 0) {
            isNeedMore = false;
            lineList.add(line);
            return;
        }
        //显示两行及其以上的时候
        if (lineList.size() < maxLine) {
            if (getWordCount(text) > breakLineCount) {
                isNeedMore = true;
                lineList.add(line);
                String otherString = getSubString(text, getWordCount(line), getWordCount(text));
                breakTextLine(otherString, breakLineCount, maxLine);
            } else {
                isNeedMore = false;
                lineList.add(line);
            }
        }
    }

    /**
     * 当最后一行是 "..." 的时候，让最后这行只占一半的字体高度，视觉上好看点
     *
     * @param lines 一共的行数
     * @return 每行文字的 baseLineY 的集合
     */
    private List<Float> getBaseLineList(int lines) {
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float fontHeight = fontMetrics.bottom - fontMetrics.top;
        float textBaseY = mHexagonHeight - (mHexagonHeight - fontHeight) / 2 - fontMetrics.bottom;

        List<Float> list = new ArrayList<>();
        int offset = -(lines - 1);
        for (int i = 0; i < lines; i++) {
            //当最后一行是点的时候，除最后一行外整体向下平移 fontHeight/4，最后一行向上平移 fontHeight/4
            if (isNeedMore) {
                if (i != lines - 1) {
                    //不是最后一行,向下平移 fontHeight/4
                    list.add(textBaseY + offset * (fontHeight / 2 + textSpacing / 2) + fontHeight / 4);
                    offset += 2;
                } else {
                    //最后一行，向上平移 fontHeight/4
                    list.add(textBaseY + offset * (fontHeight / 2 + textSpacing / 2) - fontHeight / 4);
                }
            } else {
                list.add(textBaseY + offset * (fontHeight / 2 + textSpacing / 2));
                offset += 2;
            }
        }
        return list;
    }

    /**
     * 将 bitmap 按照 目标 width 和 height 缩放，并且平移到中间
     * 效果等效于 ImageView 的 CENTER_CROP
     */
    private void updateShaderMatrix(Bitmap bitmap, float width, float height) {
        float scale, dx = 0, dy = 0;

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        mShaderMatrix.set(null);
        if (bitmapWidth * height > width * bitmapHeight) {
            scale = height / (float) bitmapHeight;     //图片的宽高比 大于 有效绘制区域的宽高比，此时缩放比以 高度的缩放比为基准
            dx = (width - bitmapWidth * scale) * 0.5f; //dx 为负值，表示向左平移
        } else {
            scale = width / (float) bitmapWidth;        //图片的宽高比 小于 有效绘制区域的宽高比，此时缩放比以 宽度的缩放比为基准
            dy = (height - bitmapHeight * scale) * 0.5f;//dy 为负值，表示向上平移
        }
        //设置图片的缩放大小
        //注意： matrix 的表象的像一个队列，pre 方法总是在队头插，post 方法总是在队尾插，set 方法总是在中间插
        //所以一般情况下，post 基本可以满足所有需求
        mShaderMatrix.postScale(scale, scale);
        //设置图片的平移距离
        if (!borderOverlay) {
            //将图片根据边框的大小右和下偏移
            if (hexagonOrientation == HORIZONTAL) {
                dx = (float) (dx + 0.5f + borderWidth / Math.cos(RADIAN30));
                dy = dy + 0.5f + borderWidth;
            }
            if (hexagonOrientation == VERTICAL) {
                dx = dx + 0.5f + borderWidth;
                dy = (float) (dy + 0.5f + borderWidth / Math.cos(RADIAN30));
            }
            mShaderMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
        } else {
            mShaderMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
        }
        mBitmapShader.setLocalMatrix(mShaderMatrix);  //最后赋值给BitmapShader
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 有效的绘制区域已经没有了，就不绘制了
        if (hexagonOrientation == VERTICAL && (mBitmapPointList.get(1).x - mBitmapPointList.get(5).x <= 0)) return;
        if (hexagonOrientation == HORIZONTAL && (mBitmapPointList.get(5).y - mBitmapPointList.get(1).y <= 0)) return;

        canvas.translate(mTranslateX, mTranslateY);       //平移画布，保证居中显示
        if (mBitmap == null) canvas.drawPath(mBitmapPath, mFillPaint);  //画背景色，只在没有图片的时候画
        else canvas.drawPath(mBitmapPath, mBitmapPaint);  //画背景图片
        canvas.drawPath(mBitmapPath, mTextBitmapPaint);     //将文字转换成bitmap，方便用path切割
        if (borderWidth > 0) canvas.drawPath(mBorderPath, mBorderPaint);       //画边框
    }

    /**
     * 构造六边形的可绘制区域
     */
    public Path getHexagonDrawPath(float width, float height) {
        Path path = new Path();
        mDrawPathPointList = new ArrayList<>();
        if (hexagonOrientation == VERTICAL) {
            path.moveTo(width / 2, 0);
            path.lineTo(width, height / 4);
            path.lineTo(width, height * 3 / 4);
            path.lineTo(width / 2, height);
            path.lineTo(0, height * 3 / 4);
            path.lineTo(0, height / 4);
            path.close();
            mDrawPathPointList.add(new PointF(width / 2, 0));
            mDrawPathPointList.add(new PointF(width, height / 4));
            mDrawPathPointList.add(new PointF(width, height * 3 / 4));
            mDrawPathPointList.add(new PointF(width / 2, height));
            mDrawPathPointList.add(new PointF(0, height * 3 / 4));
            mDrawPathPointList.add(new PointF(0, height / 4));
        }
        if (hexagonOrientation == HORIZONTAL) {
            path.moveTo(0, height / 2);
            path.lineTo(width / 4, 0);
            path.lineTo(width * 3 / 4, 0);
            path.lineTo(width, height / 2);
            path.lineTo(width * 3 / 4, height);
            path.lineTo(width / 4, height);
            path.close();
            mDrawPathPointList.add(new PointF(0, height / 2));
            mDrawPathPointList.add(new PointF(width / 4, 0));
            mDrawPathPointList.add(new PointF(width * 3 / 4, 0));
            mDrawPathPointList.add(new PointF(width, height / 2));
            mDrawPathPointList.add(new PointF(width * 3 / 4, height));
            mDrawPathPointList.add(new PointF(width / 4, height));
        }
        return path;
    }

    /**
     * 根据六边形绘制区域的大小和边框大小，得到边框的绘制区域
     */
    private Path getBorderPath(List<PointF> pointList, int borderWidth) {
        //为避免计算误差，小于等于0 的时候，直接使用外层边框
        if (borderWidth <= 0) return mDrawPath;
        if (hexagonOrientation == VERTICAL) return makeVerticalHexagonPath(pointList, borderWidth / 2.0f, null);
        if (hexagonOrientation == HORIZONTAL) return makeHorizontalHexagonPath(pointList, borderWidth / 2.0f, null);
        return null;
    }

    /**
     * 根据六边形绘制区域的大小和边框大小，得到图片的绘制区域
     */
    private Path getBitmapPath(List<PointF> pointList, int borderWidth) {
        //为避免计算误差，小于等于0 的时候，直接使用外层边框
        if (borderWidth <= 0) return mDrawPath;
        //true表示边框会覆盖一部分图片，false表示边框不会覆盖在图片之上
        if (borderOverlay) return mDrawPath;
        mBitmapPointList = new ArrayList<>();
        if (hexagonOrientation == VERTICAL) return makeVerticalHexagonPath(pointList, borderWidth, mBitmapPointList);
        if (hexagonOrientation == HORIZONTAL)
            return makeHorizontalHexagonPath(pointList, borderWidth, mBitmapPointList);
        return null;
    }

    /**
     * 对于竖直方向的六边形进行 offset 缩放偏移
     */
    private Path makeVerticalHexagonPath(List<PointF> pointList, float offset, List<PointF> pointFList) {
        Path path = new Path();
        path.moveTo(pointList.get(0).x, (float) (pointList.get(0).y + offset / Math.cos(RADIAN30)));
        path.lineTo(pointList.get(1).x - offset, (float) (pointList.get(1).y + offset * Math.tan(RADIAN30)));
        path.lineTo(pointList.get(2).x - offset, (float) (pointList.get(2).y - offset * Math.tan(RADIAN30)));
        path.lineTo(pointList.get(3).x, (float) (pointList.get(3).y - offset / Math.cos(RADIAN30)));
        path.lineTo(pointList.get(4).x + offset, (float) (pointList.get(4).y - offset * Math.tan(RADIAN30)));
        path.lineTo(pointList.get(5).x + offset, (float) (pointList.get(5).y + offset * Math.tan(RADIAN30)));
        path.close();
        if (pointFList != null) {
            pointFList.add(new PointF(pointList.get(0).x, (float) (pointList.get(0).y + offset / Math.cos(RADIAN30))));
            pointFList.add(new PointF(pointList.get(1).x - offset, (float) (pointList.get(1).y + offset * Math.tan(RADIAN30))));
            pointFList.add(new PointF(pointList.get(2).x - offset, (float) (pointList.get(2).y - offset * Math.tan(RADIAN30))));
            pointFList.add(new PointF(pointList.get(3).x, (float) (pointList.get(3).y - offset / Math.cos(RADIAN30))));
            pointFList.add(new PointF(pointList.get(4).x + offset, (float) (pointList.get(4).y - offset * Math.tan(RADIAN30))));
            pointFList.add(new PointF(pointList.get(5).x + offset, (float) (pointList.get(5).y + offset * Math.tan(RADIAN30))));
        }
        return path;
    }

    /**
     * 对于水平方向的六边形进行 offset 缩放偏移
     */
    private Path makeHorizontalHexagonPath(List<PointF> pointList, float offset, List<PointF> pointFList) {
        Path path = new Path();
        path.moveTo((float) (pointList.get(0).x + offset / Math.cos(RADIAN30)), pointList.get(0).y);
        path.lineTo((float) (pointList.get(1).x + offset * Math.tan(RADIAN30)), pointList.get(1).y + offset);
        path.lineTo((float) (pointList.get(2).x - offset * Math.tan(RADIAN30)), pointList.get(2).y + offset);
        path.lineTo((float) (pointList.get(3).x - offset / Math.cos(RADIAN30)), pointList.get(3).y);
        path.lineTo((float) (pointList.get(4).x - offset * Math.tan(RADIAN30)), pointList.get(4).y - offset);
        path.lineTo((float) (pointList.get(5).x + offset * Math.tan(RADIAN30)), pointList.get(5).y - offset);
        path.close();
        if (pointFList != null) {
            pointFList.add(new PointF((float) (pointList.get(0).x + offset / Math.cos(RADIAN30)), pointList.get(0).y));
            pointFList.add(new PointF((float) (pointList.get(1).x + offset * Math.tan(RADIAN30)), pointList.get(1).y + offset));
            pointFList.add(new PointF((float) (pointList.get(2).x - offset * Math.tan(RADIAN30)), pointList.get(2).y + offset));
            pointFList.add(new PointF((float) (pointList.get(3).x - offset / Math.cos(RADIAN30)), pointList.get(3).y));
            pointFList.add(new PointF((float) (pointList.get(4).x - offset * Math.tan(RADIAN30)), pointList.get(4).y - offset));
            pointFList.add(new PointF((float) (pointList.get(5).x + offset * Math.tan(RADIAN30)), pointList.get(5).y - offset));
        }
        return path;
    }

    /**
     * 将传入的drawable转换成bitmap
     */
    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) return null;
        if (drawable instanceof BitmapDrawable) return ((BitmapDrawable) drawable).getBitmap();

        try {
            Bitmap bitmap;
            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLOR_DRAWABLE_DIMENSION, COLOR_DRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param str    需要截取的字符串
     * @param length 长度按字节数的长度计算，一个汉字为两个字节，例如需要截取两个汉字的长度，length = 4
     * @return 截取的字符串，不会出现半个字符
     */
    public String getSubString(String str, int length) {
        if (length < 0) return "";
        int count = 0, offset;
        char[] c = str.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] > 256) {
                offset = 2;
                count += 2;
            } else {
                offset = 1;
                count++;
            }
            if (count == length) return str.substring(0, i + 1);
            if ((count == length + 1 && offset == 2)) return str.substring(0, i);
        }
        return "";
    }

    /**
     * @param str  需要截取的字符串
     * @param from 开始截取的位置，按字节数算
     * @param to   结束截取的位置，按字节数算
     * @return 截取的字符串
     */
    public String getSubString(String str, int from, int to) {
        if (from < 0) from = 0;
        if (to > getWordCount(str)) to = getWordCount(str);
        String toString = getSubString(str, to);
        String fromString = getSubString(str, from);
        return toString.substring(fromString.length());
    }

    /**
     * 获得字符串长度，一个汉字占用两个字节
     */
    public int getWordCount(String s) {
        int length = 0;
        for (int i = 0; i < s.length(); i++) {
            int ascii = Character.codePointAt(s, i);
            if (ascii >= 0 && ascii <= 255) length++;
            else length += 2;
        }
        return length;
    }

    /*--------------------------------------------------------------------------------------------*/
    /*----------------------------------以下为重写的方法--------------------------------------------*/
    /*--------------------------------------------------------------------------------------------*/
    @Override
    public ScaleType getScaleType() {
        return SCALE_TYPE;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType != SCALE_TYPE) {
            throw new IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType));
        }
    }

    @Override
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        if (adjustViewBounds) {
            throw new IllegalArgumentException("adjustViewBounds not supported.");
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mBitmap = bm;
        setup();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mBitmap = getBitmapFromDrawable(drawable);
        setup();
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        super.setImageResource(resId);
        mBitmap = getBitmapFromDrawable(getDrawable());
        setup();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        mBitmap = uri != null ? getBitmapFromDrawable(getDrawable()) : null;
        setup();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (cf == mColorFilter) return;

        mColorFilter = cf;
        mBitmapPaint.setColorFilter(mColorFilter);
        mFillPaint.setColorFilter(mColorFilter);
        invalidate();
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        setup();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        setup();
    }

    public void setTextResource(@StringRes int textRes) {
        setText(getContext().getResources().getString(textRes));
    }

    /** @return 返回文字的大小，单位像素 */
    public int getTextSize() {
        return textSize;
    }

    /** @param textSize 设置文字的大小，单位 sp */
    public void setTextSize(int textSize) {
        this.textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, getResources().getDisplayMetrics());
        setup();
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
        mTextPaint.setColor(textColor);
        setup();
    }

    public void setTextColorResource(@ColorRes int textColorRes) {
        setTextColor(getContext().getResources().getColor(textColorRes));
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        setup();
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(@ColorInt int borderColor) {
        this.borderColor = borderColor;
        mBorderPaint.setColor(borderColor);
        invalidate();
    }

    public void setBorderColorResource(@ColorRes int borderColorRes) {
        setBorderColor(getContext().getResources().getColor(borderColorRes));
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(@ColorInt int fillColor) {
        this.fillColor = fillColor;
        mFillPaint.setColor(fillColor);
        invalidate();
    }

    public void setFillColorResource(@ColorRes int fillColorRes) {
        setFillColor(getContext().getResources().getColor(fillColorRes));
    }

    public int getCorner() {
        return corner;
    }

    public void setCorner(int corner) {
        this.corner = corner;
        setup();
    }

    public int getBreakLineCount() {
        return breakLineCount;
    }

    public void setBreakLineCount(int breakLineCount) {
        this.breakLineCount = breakLineCount;
        setup();
    }

    public int getMaxLine() {
        return maxLine;
    }

    public void setMaxLine(int maxLine) {
        this.maxLine = maxLine;
        setup();
    }

    public int getTextSpacing() {
        return textSpacing;
    }

    public void setTextSpacing(int textSpacing) {
        this.textSpacing = textSpacing;
        setup();
    }

    public int getHexagonOrientation() {
        return hexagonOrientation;
    }

    public void setHexagonOrientation(int hexagonOrientation) {
        this.hexagonOrientation = hexagonOrientation;
        setup();
    }

    public boolean isBorderOverlay() {
        return borderOverlay;
    }

    public void setBorderOverlay(boolean borderOverlay) {
        this.borderOverlay = borderOverlay;
        setup();
    }

    /**
     * 核心判断，一个点是否在外凸多边形内
     * 原理射线法判断，如果一个点在多边形内部，必然与多边形有奇数个交点
     * 反之如果在多边形外部，必然有偶数个交点
     */
    public static class LassoUtils {
        private static final LassoUtils instance = new LassoUtils();//饿汉单例模式
        private float[] mPolyX, mPolyY; // 多边形各个点坐标
        private int mPolySize; // 有几个点

        private LassoUtils() {
        }

        public static LassoUtils getInstance() {
            return instance;
        }

        /**
         * 构造 多边形路径
         */
        public void setLassoList(List<PointF> pointFs) {
            mPolySize = pointFs.size();
            mPolyX = new float[mPolySize];
            mPolyY = new float[mPolySize];

            for (int i = 0; i < mPolySize; i++) {
                mPolyX[i] = pointFs.get(i).x;
                mPolyY[i] = pointFs.get(i).y;
            }
        }

        /**
         * 射线法判断点是否在多边形内部
         */
        public boolean contains(float x, float y) {
            boolean result = false;
            for (int i = 0, j = mPolySize - 1; i < mPolySize; j = i++) {
                if ((mPolyY[i] < y && mPolyY[j] >= y) || (mPolyY[j] < y && mPolyY[i] >= y)) {
                    if (mPolyX[i] + (y - mPolyY[i]) / (mPolyY[j] - mPolyY[i]) * (mPolyX[j] - mPolyX[i]) < x) {
                        result = !result;
                    }
                }
            }
            return result;
        }
    }
}