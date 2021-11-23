package com.lzy.imagepicker.view;


import com.lzy.imagepicker.ResourceTable;
import com.lzy.imagepicker.util.ResUtil;
import com.lzy.imagepicker.util.Utils;
import ohos.agp.components.AttrSet;
import ohos.agp.components.Component;
import ohos.agp.components.Image;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.render.*;
import ohos.agp.utils.Color;
import ohos.agp.utils.Matrix;
import ohos.agp.utils.Point;
import ohos.agp.utils.RectFloat;
import ohos.app.Context;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.media.image.ImagePacker;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.media.image.common.ImageFormat;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.Rect;
import ohos.multimodalinput.event.TouchEvent;
import ohos.utils.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ================================================
 * 作    者：廖子尧
 * 版    本：1.0
 * 创建日期：2016/1/7
 * 描    述：
 * Matrix 的9个值分别为  缩放  平移  倾斜
 * MSCALE_X	 MSKEW_X	MTRANS_X
 * MSKEW_Y	 MSCALE_Y	MTRANS_Y
 * MPERSP_0  MPERSP_1	MPERSP_2
 * 修订历史：
 * ================================================
 */

public class CropImageView extends Image {

    /******************************** 中间的FocusView绘图相关的参数 *****************************/
    public enum Style {
        RECTANGLE, CIRCLE
    }

    private Style[] styles = {Style.RECTANGLE, Style.CIRCLE};

    private int mMaskColor = 0xAF000000;   //暗色
    private int mBorderColor = 0xAA808080; //焦点框的边框颜色
    private int mBorderWidth = 1;         //焦点边框的宽度（画笔宽度）
    private int mFocusWidth = 250;         //焦点框的宽度
    private int mFocusHeight = 250;        //焦点框的高度
    private int mDefaultStyleIndex = 0;    //默认焦点框的形状

    private Style mStyle = styles[mDefaultStyleIndex];
    private Paint mBorderPaint = new Paint();
    private Path mFocusPath = new Path();
    private RectFloat mFocusRect = new RectFloat();

    /******************************** 图片缩放位移控制的参数 ************************************/
    private static final float MAX_SCALE = 4.0f;  //最大缩放比，图片缩放后的大小与中间选中区域的比值
    private static final int NONE = 0;   // 初始化
    private static final int DRAG = 1;   // 拖拽
    private static final int ZOOM = 2;   // 缩放
    private static final int ROTATE = 3; // 旋转
    private static final int ZOOM_OR_ROTATE = 4;  // 缩放或旋转

    private static final int SAVE_SUCCESS = 1001;  // 缩放或旋转
    private static final int SAVE_ERROR = 1002;  // 缩放或旋转

    private int mImageWidth;
    private int mImageHeight;
    private int mRotatedImageWidth;
    private int mRotatedImageHeight;
    private Matrix matrix = new Matrix();      //图片变换的matrix
    private Matrix savedMatrix = new Matrix(); //开始变幻的时候，图片的matrix
    private Point pA = new Point();          //第一个手指按下点的坐标
    private Point pB = new Point();          //第二个手指按下点的坐标
    private Point midPoint = new Point();    //两个手指的中间点
    private Point doubleClickPos = new Point();  //双击图片的时候，双击点的坐标
    private Point mFocusMidPoint = new Point();  //中间View的中间点
    private int mode = NONE;            //初始的模式
    private long doubleClickTime = 0;   //第二次双击的时间
    private double rotation = 0;        //手指旋转的角度，不是90的整数倍，可能为任意值，需要转换成level
    private float oldDist = 1;          //双指第一次的距离
    private int sumRotateLevel = 0;     //旋转的角度，90的整数倍
    private float mMaxScale = MAX_SCALE;//程序根据不同图片的大小，动态得到的最大缩放比
    private boolean isInited = false;   //是否经过了 onSizeChanged 初始化
    private boolean mSaving = false;    //是否正在保存
    private static EventHandler mHandler = new InnerHandler();
    private PixelMap pixelMap;
    private Canvas canvas;

    public CropImageView(Context context) {
        this(context, null);

    }

    public CropImageView(Context context, AttrSet attrs) {
        this(context, attrs, null);

    }


    public CropImageView(Context context, AttrSet attrs, String defStyle) {
        super(context, attrs, defStyle);

        mFocusWidth = Utils.vp2px(mFocusWidth, context);
        mFocusHeight = Utils.vp2px(mFocusHeight, context);
        mBorderWidth = Utils.vp2px(mBorderWidth, context);
        isInited = true;

//        TypedArray a = context.obtainStyledAttributes(attrs, ResourceTable.styleable.CropImageView);
//        mMaskColor = ResUtil.getColor(ResourceTable.styleable.CropImageView_cropMaskColor, mMaskColor);
//        mBorderColor = a.getColor(ResourceTable.styleable.CropImageView_cropBorderColor, mBorderColor);
//        mBorderWidth = a.getDimensionPixelSize(ResourceTable.styleable.CropImageView_cropBorderWidth, mBorderWidth);
//        mFocusWidth = a.getDimensionPixelSize(ResourceTable.styleable.CropImageView_cropFocusWidth, mFocusWidth);
//        mFocusHeight = a.getDimensionPixelSize(ResourceTable.styleable.CropImageView_cropFocusHeight, mFocusHeight);
//        mDefaultStyleIndex = a.getInteger(ResourceTable.styleable.CropImageView_cropStyle, mDefaultStyleIndex);
        mMaskColor = ResUtil.getColorValue(attrs, "cropMaskColor", mMaskColor);
        mBorderColor = ResUtil.getColorValue(attrs, "cropBorderColor", mBorderColor);
        mBorderWidth = ResUtil.getDimensionValue(attrs, "cropBorderWidth", mBorderWidth);
        mFocusWidth = ResUtil.getDimensionValue(attrs, "cropFocusWidth", mFocusWidth);
        mFocusHeight = ResUtil.getDimensionValue(attrs, "cropFocusHeight", mFocusHeight);
        mDefaultStyleIndex = ResUtil.getIntValue(attrs, "cropStyle", mDefaultStyleIndex);

        mStyle = styles[mDefaultStyleIndex];
//        a.recycle();

        setScaleMode(ScaleMode.STRETCH);


        //只允许图片为当前的缩放模式
//        setScaleType(ScaleType.MATRIX);
        setLayoutRefreshedListener(new LayoutRefreshedListener() {
            @Override
            public void onRefreshed(Component component) {
                initImage(pixelMap);
                component.addDrawTask(new DrawTask() {
                    @Override
                    public void onDraw(Component component, Canvas canvas) {
                        canvas.concat(matrix);

                        if (Style.RECTANGLE == mStyle) {

                            mFocusPath.addRect(mFocusRect, Path.Direction.COUNTER_CLOCK_WISE);
                            canvas.save();
                            canvas.clipRect(new RectFloat(0, 0, getWidth(), getHeight()));
                            canvas.clipPath(mFocusPath, Canvas.ClipOp.DIFFERENCE);
                            canvas.drawColor(mMaskColor, Canvas.PorterDuffMode.DST_IN);
                            canvas.restore();
                        } else if (Style.CIRCLE == mStyle) {
                            float radius = Math.min((mFocusRect.right - mFocusRect.left) / 2, (mFocusRect.bottom - mFocusRect.top) / 2);

                            mFocusPath.addCircle(mFocusMidPoint.getPointX(), mFocusMidPoint.getPointY(), radius, Path.Direction.COUNTER_CLOCK_WISE);

                            canvas.save();
                            canvas.clipRect(new RectFloat(0, 0, getWidth(), getHeight()));

                            canvas.clipPath(mFocusPath, Canvas.ClipOp.DIFFERENCE);
                            canvas.drawColor(mMaskColor, Canvas.PorterDuffMode.DST_IN);
                            canvas.restore();
                        }
                        mBorderPaint.setColor(new Color(mBorderColor));
                        mBorderPaint.setStyle(Paint.Style.STROKE_STYLE);
                        mBorderPaint.setStrokeWidth(mBorderWidth);

                        mBorderPaint.setAntiAlias(true);
                        canvas.drawPath(mFocusPath, mBorderPaint);
                        mFocusPath.reset();
                    }
                });
            }
        });
    }


    @Override
    public void setPixelMap(PixelMap pixelMap) {

        super.setPixelMap(pixelMap);
        this.pixelMap = pixelMap;
        initImage(pixelMap);
    }


    @Override
    public void setImageElement(Element element) {

        super.setImageElement(element);
    }

    @Override
    public void setPixelMap(int resId) {

        super.setPixelMap(resId);
    }



    /**
     * 初始化图片和焦点框
     * @param pixelMap  传入PixelMap对象
     */
    private void initImage(PixelMap pixelMap) {

        if (!isInited || pixelMap == null) {

            return;
        }

        mode = NONE;
//        matrix = getImageMatrix();
        mImageWidth = mRotatedImageWidth = pixelMap.getImageInfo().size.width;
        mImageHeight = mRotatedImageHeight = pixelMap.getImageInfo().size.height;

        //计算出焦点框的中点的坐标和上、下、左、右边的x或y的值
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        float midPointX = viewWidth / 2;
        float midPointY = viewHeight / 2;
        mFocusMidPoint = new Point(midPointX, midPointY);

        if (mStyle == Style.CIRCLE) {
            int focusSize = Math.min(mFocusWidth, mFocusHeight);
            mFocusWidth = focusSize;
            mFocusHeight = focusSize;

        }
        mFocusRect.left = mFocusMidPoint.getPointX() - mFocusWidth / 2;
        mFocusRect.right = mFocusMidPoint.getPointX() + mFocusWidth / 2;
        mFocusRect.top = mFocusMidPoint.getPointY() - mFocusHeight / 2;
        mFocusRect.bottom = mFocusMidPoint.getPointY() + mFocusHeight / 2;

        //适配焦点框的缩放比例（图片的最小边不小于焦点框的最小边）
        float fitFocusScale = getScale(mImageWidth, mImageHeight, mFocusWidth, mFocusHeight, true);

        mMaxScale = fitFocusScale * MAX_SCALE;
        //适配显示图片的ImageView的缩放比例（图片至少有一边是铺满屏幕的显示的情形）
        float fitViewScale = getScale(mImageWidth, mImageHeight, viewWidth, viewHeight, false);

        //确定最终的缩放比例,在适配焦点框的前提下适配显示图片的ImageView，
        //方案：首先满足适配焦点框，如果还能适配显示图片的ImageView，则适配它，即取缩放比例的最大值。
        //采取这种方案的原因：有可能图片很长或者很高，适配了ImageView的时候可能会宽/高已经小于焦点框的宽/高
        float scale = fitViewScale > fitFocusScale ? fitViewScale : fitFocusScale;

        //图像中点为中心进行缩放
        matrix.setScale(scale, scale, mImageWidth / 2, mImageHeight / 2);
        setScaleMode(scale == fitViewScale ? ScaleMode.INSIDE : ScaleMode.CENTER);
//        setScale(mImageWidth / 2, mImageHeight / 2);
        float[] mImageMatrixValues = new float[9];
        matrix.getElements(mImageMatrixValues); //获取缩放后的mImageMatrix的值
        float transX = mFocusMidPoint.getPointX() - (mImageMatrixValues[2] + mImageWidth * mImageMatrixValues[0] / 2);  //X轴方向的位移
        float transY = mFocusMidPoint.getPointY() - (mImageMatrixValues[5] + mImageHeight * mImageMatrixValues[4] / 2); //Y轴方向的位移

        matrix.postTranslate(transX, transY);
//        setImageMatrix(matrix);
        invalidate();


        if (getTouchEventListener() == null)
            setTouchEventListener(new TouchEventListener() {
                @Override
                public boolean onTouchEvent(Component component, TouchEvent event) {
//                if (mSaving || null == getImageElement()) {
//                    return super.onTouchEvent(event);
//                }
                    switch (event.getAction()) {
                        case TouchEvent.PRIMARY_POINT_DOWN:  //第一个点按下

                            savedMatrix.setMatrix(matrix);   //以后每次需要变换的时候，以现在的状态为基础进行变换
                            pA.modify(event.getPointerPosition(0).getX(), event.getPointerPosition(0).getY());
                            pB.modify(event.getPointerPosition(0).getX(), event.getPointerPosition(0).getY());
                            mode = DRAG;
                            break;
                        case TouchEvent.OTHER_POINT_DOWN:  //第二个点按下

                            if (event.getIndex() > 1) break;
                            pA.modify(event.getPointerPosition(0).getX(), event.getPointerPosition(0).getY());
                            pB.modify(event.getPointerPosition(1).getX(), event.getPointerPosition(1).getY());
                            midPoint.modify((pA.getPointX() + pB.getPointX()) / 2, (pA.getPointY() + pB.getPointY()) / 2);
                            oldDist = spacing(pA, pB);
                            savedMatrix.setMatrix(matrix);  //以后每次需要变换的时候，以现在的状态为基础进行变换
                            if (oldDist > 10f) mode = ZOOM_OR_ROTATE;//两点之间的距离大于10才有效
                            break;
                        case TouchEvent.POINT_MOVE:
                            if (mode == ZOOM_OR_ROTATE) {

                                Point pC = new Point(event.getPointerPosition(1).getX() - event.getPointerPosition(0).getX() + pA.getPointY(),
                                        event.getPointerPosition(1).getY() - event.getPointerPosition(0).getY() + pA.getPointY());
                                double a = spacing(pB.getPointX(), pB.getPointY(), pC.getPointX(), pC.getPointY());
                                double b = spacing(pA.getPointX(), pA.getPointY(), pC.getPointX(), pC.getPointY());
                                double c = spacing(pA.getPointX(), pA.getPointY(), pB.getPointX(), pB.getPointY());
                                if (a >= 10) {
                                    double cosB = (a * a + c * c - b * b) / (2 * a * c);
                                    double angleB = Math.acos(cosB);
                                    double PID4 = Math.PI / 4;
                                    //旋转时，默认角度在 45 - 135 度之间
                                    if (angleB > PID4 && angleB < 3 * PID4) mode = ROTATE;
                                    else mode = ZOOM;
                                }
                            }
                            if (mode == DRAG) {

                                matrix.setMatrix(savedMatrix);
                                matrix.postTranslate(event.getPointerPosition(0).getX() - pA.getPointX(), event.getPointerPosition(0).getY() - pA.getPointY());
                                fixTranslation();
//                                setImageMatrix(matrix);
                            } else if (mode == ZOOM) {

                                float newDist = spacing(event.getPointerPosition(0).getX(), event.getPointerPosition(0).getY(), event.getPointerPosition(1).getX(), event.getPointerPosition(1).getY());
                                if (newDist > 10f) {
                                    matrix.setMatrix(savedMatrix);
                                    // 这里之所以用 maxPostScale 矫正一下，主要是防止缩放到最大时，继续缩放图片会产生位移
                                    float tScale = Math.min(newDist / oldDist, maxPostScale());
                                    if (tScale != 0) {
                                        matrix.postScale(tScale, tScale, midPoint.getPointX(), midPoint.getPointY());
                                        fixScale();
                                        fixTranslation();
//                                        setImageMatrix(matrix);
                                    }
                                }
                            } else if (mode == ROTATE) {

                                Point pC = new Point(event.getPointerPosition(1).getX() - event.getPointerPosition(0).getX() + pA.getPointX(),
                                        event.getPointerPosition(1).getX() - event.getPointerPosition(0).getY() + pA.getPointY());
                                double a = spacing(pB.getPointX(), pB.getPointY(), pC.getPointX(), pC.getPointY());
                                double b = spacing(pA.getPointX(), pA.getPointY(), pC.getPointX(), pC.getPointY());
                                double c = spacing(pA.getPointX(), pA.getPointY(), pB.getPointX(), pB.getPointY());
                                if (b > 10) {
                                    double cosA = (b * b + c * c - a * a) / (2 * b * c);
                                    double angleA = Math.acos(cosA);
                                    double ta = pB.getPointY() - pA.getPointY();
                                    double tb = pA.getPointX() - pB.getPointX();
                                    double tc = pB.getPointX() * pA.getPointY() - pA.getPointX() * pB.getPointY();
                                    double td = ta * pC.getPointX() + tb * pC.getPointY() + tc;
                                    if (td > 0) {
                                        angleA = 2 * Math.PI - angleA;
                                    }
                                    rotation = angleA;
                                    matrix.setMatrix(savedMatrix);
                                    matrix.postRotate((float) (rotation * 180 / Math.PI), midPoint.getPointX(), midPoint.getPointY());
//                                    setRotation((float) (rotation * 180 / Math.PI));
//                                    setImageMatrix(matrix);
                                }
                            }
                            break;
//            case TouchEvent.ACTION_UP:
                        case TouchEvent.PRIMARY_POINT_UP:
                            if (mode == DRAG) {
                                if (spacing(pA, pB) < 50) {
                                    long now = System.currentTimeMillis();
                                    if (now - doubleClickTime < 500 && spacing(pA, doubleClickPos) < 50) {
                                        doubleClick(pA.getPointX(), pA.getPointY());
                                        now = 0;
                                    }
                                    doubleClickPos.modify(pA);
                                    doubleClickTime = now;
                                }
                            } else if (mode == ROTATE) {
                                int rotateLevel = (int) Math.floor((rotation + Math.PI / 4) / (Math.PI / 2));
                                if (rotateLevel == 4) rotateLevel = 0;
                                matrix.setMatrix(savedMatrix);
                                matrix.postRotate(90 * rotateLevel, midPoint.getPointX(), midPoint.getPointY());
                                if (rotateLevel == 1 || rotateLevel == 3) {
                                    int tmp = mRotatedImageWidth;
                                    mRotatedImageWidth = mRotatedImageHeight;
                                    mRotatedImageHeight = tmp;
                                }
                                fixScale();
                                fixTranslation();
//                                setImageMatrix(matrix);
                                sumRotateLevel += rotateLevel;
                            }
                            mode = NONE;
                            break;
                    }
                    //解决部分机型无法拖动的问题
                    invalidate();
//        ViewCompat.postInvalidateOnAnimation(this);
                    return true;
                }
            });
    }


    /**
     * 计算边界缩放比例 isMinScale 是否最小比例，true 最小缩放比例， false 最大缩放比例
     * @param bitmapHeight pixelmap高度
     * @param bitmapWidth pixelmap宽度
     * @param isMinScale 是否最小缩放
     * @param min_height 最小高度
     * @param min_width 最小宽度
     * @return 转换比例
     * */
    private float getScale(int bitmapWidth, int bitmapHeight, int min_width, int min_height, boolean isMinScale) {
        float scale;
        float scaleX = (float) min_width / bitmapWidth;
        float scaleY = (float) min_height / bitmapHeight;
        if (isMinScale) {
            scale = scaleX > scaleY ? scaleX : scaleY;
        } else {
            scale = scaleX < scaleY ? scaleX : scaleY;
        }
        return scale;
    }


    /**
     * 修正图片的缩放比
     */
    private void fixScale() {
        float imageMatrixValues[] = new float[9];
        matrix.getElements(imageMatrixValues);
        float currentScale = Math.abs(imageMatrixValues[0]) + Math.abs(imageMatrixValues[1]);
        float minScale = getScale(mRotatedImageWidth, mRotatedImageHeight, mFocusWidth, mFocusHeight, true);
        mMaxScale = minScale * MAX_SCALE;

        //保证图片最小是占满中间的焦点空间
        if (currentScale < minScale) {
            float scale = minScale / currentScale;
            matrix.postScale(scale, scale);
            setScale(scale, scale);
        } else if (currentScale > mMaxScale) {
            float scale = mMaxScale / currentScale;
            matrix.postScale(scale, scale);
            setScale(scale, scale);
        }
    }

    /**
     * 修正图片的位移
     */
    private void fixTranslation() {
        RectFloat imageRect = new RectFloat(0, 0, mImageWidth, mImageHeight);
        matrix.mapRect(imageRect);  //获取当前图片（缩放以后的）相对于当前控件的位置区域，超过控件的上边缘或左边缘为负
        float deltaX = 0, deltaY = 0;
        if (imageRect.left > mFocusRect.left) {
            deltaX = -imageRect.left + mFocusRect.left;
        } else if (imageRect.right < mFocusRect.right) {
            deltaX = -imageRect.right + mFocusRect.right;
        }
        if (imageRect.top > mFocusRect.top) {
            deltaY = -imageRect.top + mFocusRect.top;
        } else if (imageRect.bottom < mFocusRect.bottom) {
            deltaY = -imageRect.bottom + mFocusRect.bottom;
        }
//        setTranslation(deltaX, deltaY);
        matrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 获取当前图片允许的最大缩放比
     * @return  maxPostScale
     */
    private float maxPostScale() {
        float imageMatrixValues[] = new float[9];
        matrix.getElements(imageMatrixValues);
        float curScale = Math.abs(imageMatrixValues[0]) + Math.abs(imageMatrixValues[1]);
        return mMaxScale / curScale;
    }

    /**
     * 计算两点之间的距离
     * @param x1 点1横坐标
     * @param x2 点2横坐标
     * @param y1 点1纵坐标
     * @param y2 点2纵坐标
     * @return 两点之间的距离
     */
    private float spacing(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 计算两点之间的距离
     * @param pA point a
     * @param pB point b
     * @return 两点之间的距离
     */
    private float spacing(Point pA, Point pB) {
        return spacing(pA.getPointX(), pA.getPointY(), pB.getPointX(), pB.getPointY());
    }

    /**
     * 双击触发的方法
     * @param x
     * @param y
     */
    private void doubleClick(float x, float y) {
        float p[] = new float[9];
        matrix.getElements(p);
        float curScale = Math.abs(p[0]) + Math.abs(p[1]);
        float minScale = getScale(mRotatedImageWidth, mRotatedImageHeight, mFocusWidth, mFocusHeight, true);
        if (curScale < mMaxScale) {
            //每次双击的时候，缩放加 minScale
            float toScale = Math.min(curScale + minScale, mMaxScale) / curScale;
            matrix.postScale(toScale, toScale, x, y);
        } else {
            float toScale = minScale / curScale;
            matrix.postScale(toScale, toScale, x, y);
            fixTranslation();
        }
//        setImageMatrix(matrix);
    }

    /**
     * 获取裁剪过的PixelMap
     * @param expectWidth     期望的宽度
     * @param exceptHeight    期望的高度
     * @param isSaveRectangle 是否按矩形区域保存图片
     * @return 裁剪后的Bitmap
     */
    public PixelMap getCropBitmap(int expectWidth, int exceptHeight, boolean isSaveRectangle) {
        if (expectWidth <= 0 || exceptHeight < 0) return null;
//        PixelMap srcBitmap =  getPixelMap();

        pixelMap = rotate(pixelMap, sumRotateLevel * 90);  //最好用level，因为角度可能不是90的整数
        return makeCropBitmap(pixelMap, mFocusRect, getImageMatrixRect(), expectWidth, exceptHeight, isSaveRectangle);

    }

    /**
     * 对PixelMap进行旋转
     * @param pixelmap 要旋转的图片
     * @param degrees  选择的角度（单位 度）
     * @return 旋转后的Bitmap
     */
    public PixelMap rotate(PixelMap pixelmap, int degrees) {

        if (degrees != 0 && pixelmap != null) {
//            Matrix matrix = new Matrix();
//            matrix.setRotate(degrees, (float) pixelmap.getImageInfo().size.width / 2, (float) pixelmap.getImageInfo().size.height / 2);
            PixelMap.InitializationOptions initOptions = new PixelMap.InitializationOptions();
            Rect rect = new Rect(0, 0, pixelmap.getImageInfo().size.width, pixelmap.getImageInfo().size.height);
            try {
                ImageSource.DecodingOptions options = new ImageSource.DecodingOptions();
                options.rotateDegrees = degrees;
                options.desiredSize.width = pixelmap.getImageInfo().size.width / 2;
                options.desiredSize.height = pixelmap.getImageInfo().size.height / 2;

                PixelMap rotateBitmap = PixelMap.create(pixelmap, rect, initOptions);
//                PixelMap rotateBitmap = PixelMap.create(pixelmap,  matrix, true);

                if (pixelmap != rotateBitmap) {
                    return rotateBitmap;
                }
            } catch (OutOfMemoryError ex) {
                return pixelmap;
            }
        }
        return pixelmap;
    }

    /**
     * 获取图片的MatrixRect
     * @return 获取当前图片显示的矩形区域
     */
    private RectFloat getImageMatrixRect() {
        RectFloat rectF = new RectFloat(0, 0, pixelMap.getImageInfo().size.width, pixelMap.getImageInfo().size.height);
        matrix.mapRect(rectF);
        return rectF;
    }

    /**
     * 裁剪PixelMap
     * @param pixelmap        需要裁剪的图片
     * @param focusRect       中间需要裁剪的矩形区域
     * @param imageMatrixRect 当前图片在屏幕上的显示矩形区域
     * @param expectWidth     希望获得的图片宽度，如果图片宽度不足时，拉伸图片
     * @param exceptHeight    希望获得的图片高度，如果图片高度不足时，拉伸图片
     * @param isSaveRectangle 是否希望按矩形区域保存图片
     * @return 裁剪后的图片的Bitmap
     */
    private PixelMap makeCropBitmap(PixelMap pixelmap, RectFloat focusRect, RectFloat imageMatrixRect, int expectWidth, int exceptHeight, boolean isSaveRectangle) {
        if (imageMatrixRect == null || pixelmap == null) {
            return null;
        }
        float scale = imageMatrixRect.getWidth() / pixelmap.getImageInfo().size.width;
        int left = (int) ((focusRect.left - imageMatrixRect.left) / scale);
        int top = (int) ((focusRect.top - imageMatrixRect.top) / scale);
        int width = (int) (focusRect.getWidth() / scale);
        int height = (int) (focusRect.getHeight() / scale);

        if (left < 0) left = 0;
        if (top < 0) top = 0;
        if (left + width > pixelmap.getImageInfo().size.width) width = pixelmap.getImageInfo().size.width - left;
        if (top + height > pixelmap.getImageInfo().size.height) height = pixelmap.getImageInfo().size.height - top;

        try {
            PixelMap.InitializationOptions options = new PixelMap.InitializationOptions();
            Rect rect = new Rect(left, top, width, height);
            pixelmap = PixelMap.create(pixelmap, rect, options);
            if (expectWidth != width || exceptHeight != height) {
                options.size.height = exceptHeight;
                options.size.width = expectWidth;
                options.useSourceIfMatch = true;

                pixelmap = PixelMap.create(pixelmap, options);

                if (mStyle == CropImageView.Style.CIRCLE && !isSaveRectangle) {

                    //如果是圆形，就将图片裁剪成圆的
                    int length = Math.min(expectWidth, exceptHeight);
                    int radius = length / 2;
                    options.size.width = length;
                    options.size.height = length;
                    options.pixelFormat = PixelFormat.ARGB_8888;
                    PixelMap circleBitmap = PixelMap.create(options);
                    Texture texture = new Texture(circleBitmap);
                    Canvas canvas = new Canvas(texture);
                    PixelMapHolder holder = new PixelMapHolder(pixelmap);
                    PixelMapShader bitmapShader = new PixelMapShader(holder, PixelMapShader.TileMode.CLAMP_TILEMODE, Shader.TileMode.CLAMP_TILEMODE);
                    Paint paint = new Paint();
                    paint.setShader(bitmapShader, Paint.ShaderType.PIXELMAP_SHADER);
                    canvas.drawCircle(expectWidth / 2f, exceptHeight / 2f, radius, paint);
                    pixelmap = texture.getPixelMap();
                }
            }
        } catch (OutOfMemoryError e) {
            return pixelmap;
        }
        return pixelmap;
    }

    /**
     * 保存PixelMap到文件
     * @param folder          希望保存的文件夹
     * @param expectWidth     希望保存的图片宽度
     * @param exceptHeight    希望保存的图片高度
     * @param isSaveRectangle 是否希望按矩形区域保存图片
     */
    public void saveBitmapToFile(File folder, int expectWidth, int exceptHeight, boolean isSaveRectangle) {
        if (mSaving) return;
        mSaving = true;
        final PixelMap croppedImage = getCropBitmap(expectWidth, exceptHeight, isSaveRectangle);
        ImagePacker.PackingOptions options = new ImagePacker.PackingOptions();
        options.format = "image/jpeg";
        File saveFile = createFile(folder, "IMG_", ".jpg");
        final File finalSaveFile = saveFile;
        new Thread() {
            @Override
            public void run() {
                saveOutput(croppedImage, options, finalSaveFile);
            }
        }.start();
    }

    /**
     * 根据系统时间、前缀、后缀产生一个文件
     * @param suffix suffix
     * @param prefix prefix
     * @param folder folder
     * @return file file
     */
    private File createFile(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()){
            boolean mkdirs = folder.mkdirs();
        }
        try {
            File nomedia = new File(folder, ".nomedia");  //在当前文件夹底下创建一个 .nomedia 文件
            if (!nomedia.exists()){
                boolean create = nomedia.createNewFile();
            }
        } catch (IOException e) {
            return new File(folder, "filename");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename);
    }

    /**
     * 将图片保存在本地
     * @param croppedImage 被裁减的PixelMap
     * @param outputFormat 输出rgb格式
     * @param saveFile 存储目标文件
     */
    private void saveOutput(PixelMap croppedImage, ImagePacker.PackingOptions outputFormat, File saveFile) {
        FileOutputStream outputStream = null;
        ImagePacker imagePacker = ImagePacker.create();
        try {
            outputStream = new FileOutputStream(saveFile);
//            outputStream = getContext().getContentResolver().openOutputStream(Uri.getUriFromFile(saveFile));
            if (outputStream != null) {
                outputFormat.quality = 90;
                imagePacker.initializePacking(outputStream, outputFormat);
                boolean result = imagePacker.addImage(croppedImage);
                long dataSize = imagePacker.finalizePacking();

//                croppedImage.compress(outputFormat, 90, outputStream);
            }
            mHandler.sendEvent(InnerEvent.get(SAVE_SUCCESS, saveFile));
        } catch (IOException ex) {
            mHandler.sendEvent(InnerEvent.get(SAVE_ERROR, saveFile));
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    mHandler.sendEvent(InnerEvent.get(SAVE_ERROR, saveFile));
                }
            }
        }
        mSaving = false;
        croppedImage.release();
    }

    private static class InnerHandler extends EventHandler {
        public InnerHandler() {
            super(EventRunner.getMainEventRunner());
        }

        @Override
        public void sendEvent(InnerEvent event) throws IllegalArgumentException {
            File saveFile = (File) event.object;
            switch (event.eventId) {
                case SAVE_SUCCESS:
                    if (mListener != null) mListener.onBitmapSaveSuccess(saveFile);
                    break;
                case SAVE_ERROR:
                    if (mListener != null) mListener.onBitmapSaveError(saveFile);
                    break;
            }
        }
    }

    /**
     * 图片保存完成的监听
     */
    private static OnBitmapSaveCompleteListener mListener;

    public interface OnBitmapSaveCompleteListener {
        void onBitmapSaveSuccess(File file);

        void onBitmapSaveError(File file);
    }

    /**
     * 设置图片保存成功的回调
     * @param listener
     */
    public void setOnBitmapSaveCompleteListener(OnBitmapSaveCompleteListener listener) {
        mListener = listener;
    }

    /**
     * 返回焦点框宽度
     * @return 焦点框宽度
     */
    public int getFocusWidth() {
        return mFocusWidth;
    }

    /**
     * 设置焦点框的宽度
     * @param width 焦点宽度
     */
    public void setFocusWidth(int width) {
        mFocusWidth = width;
        initImage(this.pixelMap);
    }

    /**
     * 获取焦点框的高度
     * @return 焦点框高度
     */
    public int getFocusHeight() {
        return mFocusHeight;
    }

    /**
     * 设置焦点框的高度
     * @param height 焦点框高度
     */
    public void setFocusHeight(int height) {
        mFocusHeight = height;
        initImage(this.pixelMap);
    }

    /**
     * 返回阴影颜色
     * @return maskColor 阴影颜色
     */
    public int getMaskColor() {
        return mMaskColor;
    }

    /**
     * 设置阴影颜色
     * @param color 阴影颜色
     */
    public void setMaskColor(int color) {
        mMaskColor = color;
        invalidate();
    }

    /**
     * 返回焦点框边框颜色
     * @return  focusColor 焦点框边框颜色
     */
    public int getFocusColor() {
        return mBorderColor;
    }

    /**
     * 设置焦点框边框颜色
     * @param color border color
     */
    public void setBorderColor(int color) {
        mBorderColor = color;
        invalidate();
    }

    /**
     * 返回焦点框边框绘制宽度
     * @return borderWidth
     */
    public float getBorderWidth() {
        return mBorderWidth;
    }

    /**
     * 设置焦点边框宽度
     * @param width borderWidth
     */
    public void setBorderWidth(int width) {
        mBorderWidth = width;
        invalidate();
    }

    /**
     * 设置焦点框的形状
     * @param style focusStyle
     */
    public void setFocusStyle(Style style) {
        this.mStyle = style;
        invalidate();
    }

    /**
     * 获取焦点框的形状
     * @return focusStyle
     */
    public Style getFocusStyle() {
        return mStyle;
    }
}