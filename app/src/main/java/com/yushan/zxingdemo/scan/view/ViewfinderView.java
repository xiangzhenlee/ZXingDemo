package com.yushan.zxingdemo.scan.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.yushan.zxingdemo.R;
import com.yushan.zxingdemo.scan.camera.CameraManager;

import java.util.Collection;
import java.util.HashSet;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points. �Զ����View������ʱ�м���ʾ��
 */
public final class ViewfinderView extends View {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192,
            128, 64};
    private static final long ANIMATION_DELAY = 15L;
    private static final int OPAQUE = 0xFF;

    private final Paint paint;
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;
    private final int frameColor;
    private Collection<ResultPoint> possibleResultPoints;
    private int frameBoderColor;
    /**
     * 扫描边框下面文字的内容标识
     */
    private int textType = -1;
    public static final int TEXTCONTENTTYPE_REGISTER = 1;
    public static final int TEXTCONTENTTYPE_SCAN = 2;
    public static final int TEXTCONTENTTYPE_ADDFFRIEND = 3;
    private String scanContent;
    /**
     * 中间滑动线的最顶端位置
     */
    private int slideTop;
    /**
     * 中间那条线每次刷新移动的距离
     */
    private static final int SPEEN_DISTANCE = 5;
    /**
     * 中间滑动线的最底端位置
     */
    private int slideBottom;
    /**
     * 扫描框中的中间线的与扫描框左右的间隙
     */
    private static final int MIDDLE_LINE_PADDING = 5;
    /**
     * 扫描框中的中间线的宽度
     */
    private static final int MIDDLE_LINE_WIDTH = 6;
    /**
     * 手机的屏幕密度
     */
    private static float density;
    /**
     * 字体大小
     */
    private static final int TEXT_SIZE = 14;
    /**
     * 字体距离扫描框下面的距离
     */
    private static final int TEXT_PADDING_TOP = 30;
    private float textX;// 字的x
    private float textY;// 字的y
    boolean isFirst;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = context.getResources().getDisplayMetrics().density;
        // Initialize these once for performance rather than calling them every
        // time in onDraw().
        paint = new Paint();
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultColor = resources.getColor(R.color.result_view);
        frameColor = resources.getColor(R.color.viewfinder_frame);
        frameBoderColor = resources.getColor(R.color.viewfinder_boder);
        possibleResultPoints = new HashSet<ResultPoint>(5);
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        Rect frame = CameraManager.get().getFramingRect();
        if (frame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        // 初始化中间线滑动的最上边和最下边
        if (!isFirst) {
            isFirst = true;
            slideTop = frame.top;
            slideBottom = frame.bottom;
        }
        // Draw the exterior (i.e. outside the framing rect) darkened,黑色区域
        // 这里画取景框四周的四个阴影矩形
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
                paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(OPAQUE);
            canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
        } else {
            if (textType != TEXTCONTENTTYPE_REGISTER) {
                // Draw a two pixel solid black border inside the framing rect，画黑框
                paint.setColor(frameColor);
                canvas.drawRect(frame.left, frame.top, frame.right + 1,
                        frame.top + 2, paint);
                canvas.drawRect(frame.left, frame.top + 2, frame.left + 2,
                        frame.bottom - 1, paint);
                canvas.drawRect(frame.right - 1, frame.top, frame.right + 1,
                        frame.bottom - 1, paint);
                canvas.drawRect(frame.left, frame.bottom - 1, frame.right + 1,
                        frame.bottom + 1, paint);
                // Draw a two pixel solid black border inside the framing rect，画黑框
                paint.setColor(frameBoderColor);
                canvas.drawRect(frame.left, frame.top, frame.left + 30,
                        frame.top + 7, paint);
                canvas.drawRect(frame.left, frame.top, frame.left + 7,
                        frame.top + 31, paint);
                canvas.drawRect(frame.left, frame.bottom - 7, frame.left + 30,
                        frame.bottom + 1, paint);
                canvas.drawRect(frame.left, frame.bottom - 30, frame.left + 7,
                        frame.bottom + 1, paint);
                canvas.drawRect(frame.right - 7, frame.top, frame.right + 1,
                        frame.top + 30, paint);
                canvas.drawRect(frame.right - 30, frame.top, frame.right + 1,
                        frame.top + 7, paint);
                canvas.drawRect(frame.right - 7, frame.bottom - 30,
                        frame.right + 1, frame.bottom + 1, paint);
                canvas.drawRect(frame.right - 30, frame.bottom - 7,
                        frame.right + 1, frame.bottom + 1, paint);
            }
            // canvas.drawRect(frame.left, frame.bottom+1, frame.left+30,
            // frame.bottom-7, paint);
            // Draw a red "laser scanner" line through the middle to show
            // decoding is active
            // paint.setColor(laserColor);
            // paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
            // scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
            // int middle = frame.height() / 2 + frame.top;
            // canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1,
            // middle + 2, paint);
            // 绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE
            slideTop += SPEEN_DISTANCE;
            if (slideTop >= frame.bottom) {
                slideTop = frame.top;
            }
            Rect lineRect = new Rect();
            lineRect.left = frame.left;
            lineRect.right = frame.right;
            lineRect.top = slideTop;
            lineRect.bottom = slideTop + 18;
            canvas.drawBitmap(((BitmapDrawable) (getResources()
                            .getDrawable(R.drawable.qrcode_scan_line))).getBitmap(),
                    null, lineRect, paint);
            // 画扫描框下面的字 换行方法
            TextPaint textPaint = new TextPaint();
            // textPaint.setARGB(0xFF, 0xFF, 0, 0);
            textPaint.setTextSize(TEXT_SIZE * density);
            textPaint.setColor(Color.WHITE);
            if (textType == TEXTCONTENTTYPE_SCAN) {
                scanContent = "请扫描二维码或条形码";
                textX = frame.left;
                textY = frame.bottom + (float) TEXT_PADDING_TOP;
            } else if (textType == TEXTCONTENTTYPE_REGISTER) {
                scanContent = "请对准设备包装盒上的条形码，进行扫描！";
                textX = frame.left;
//				textY = frame.top - 80 * density;
                textY = frame.top - 3 * TEXT_PADDING_TOP;
            } else if (textType == TEXTCONTENTTYPE_ADDFFRIEND) {
                scanContent = "请扫描二维码或条形码";
                textX = frame.left;
                textY = frame.bottom + (float) TEXT_PADDING_TOP;
            }
            /**
             * * aboutTheGame ：要 绘制 的 字符串 ,textPaint(TextPaint 类型)设置了字符串格式及属性
             * 的画笔,frame.right-frame.left为设置 画多宽后 换行，后面的参数是对齐方式...
             */
            StaticLayout layout = new StaticLayout(scanContent, textPaint,
                    frame.right - frame.left, Alignment.ALIGN_CENTER, 1.5F,
                    0.0F, true);
            // 从 (20,80)的位置开始绘制

            canvas.translate(textX, textY);
            layout.draw(canvas);
            // paint.setColor(Color.WHITE);
            // paint.setTextSize(TEXT_SIZE * density);
            // paint.setAlpha(0x90);
            // paint.setTypeface(Typeface.create("System", Typeface.BOLD));
            // canvas.drawText(getResources().getString(R.string.scan_text),
            // frame.left, (float) (frame.bottom + (float)TEXT_PADDING_TOP
            // *density), paint);
            // Collection<ResultPoint> currentPossible = possibleResultPoints;
            // Collection<ResultPoint> currentLast = lastPossibleResultPoints;
            // if (currentPossible.isEmpty()) {
            // lastPossibleResultPoints = null;
            // } else {
            // possibleResultPoints = new HashSet<ResultPoint>(5);
            // lastPossibleResultPoints = currentPossible;
            // paint.setAlpha(OPAQUE);
            // paint.setColor(resultPointColor);
            // for (ResultPoint point : currentPossible) {
            // canvas.drawCircle(frame.left + point.getX(), frame.top
            // + point.getY(), 6.0f, paint);
            // }
            // }
            // if (currentLast != null) {
            // paint.setAlpha(OPAQUE / 2);
            // paint.setColor(resultPointColor);
            // for (ResultPoint point : currentLast) {
            // canvas.drawCircle(frame.left + point.getX(), frame.top
            // + point.getY(), 3.0f, paint);
            // }
            // }

            // Request another update at the animation interval, but only
            // repaint the laser line,
            // not the entire viewfinder mask.
            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
                    frame.right, frame.bottom);
        }
    }

    public void setShowInfoType(int type) {
        textType = type;
    }

    public void drawViewfinder() {
        resultBitmap = null;
        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live
     * scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        possibleResultPoints.add(point);
    }

}
