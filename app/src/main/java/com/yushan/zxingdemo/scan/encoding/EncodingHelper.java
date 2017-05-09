package com.yushan.zxingdemo.scan.encoding;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.yushan.zxingdemo.scan.Utils.CommonUtil;
import com.yushan.zxingdemo.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yushan
 */
public final class EncodingHelper {
    private static final int BLACK = 0xff434343;
    private static final int WHITE = 0xffffffff;
    private final JSONObject json;
    private final int width;  // 图像宽度
    private final int height; // 图像高度
    private Bitmap mBitmap;
    private Boolean hasBK;
    private Context mContext;

    public EncodingHelper(Context context, JSONObject MsgJson, int widthAndHeight) {

        mContext = context;
        json = MsgJson;
        width = CommonUtil.dip2px(context, widthAndHeight);
        height = CommonUtil.dip2px(context, widthAndHeight);
    }

    /**
     * 获取带边界的二维码
     *
     * @param hasBK
     * @return
     */
    public Bitmap getBitmap(Boolean hasBK) {
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = dealData();
        } catch (WriterException e) {
            e.printStackTrace();
        }

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitMatrix.get(x, y)) {
                    pixels[y * width + x] = BLACK;
                } else {
                    pixels[y * width + x] = WHITE;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        // 画边框
        if (hasBK != null && hasBK == true) {
            Paint paint = new Paint();
            Canvas canvas = new Canvas(bitmap);

            drawBK(paint, canvas);
        }

        return bitmap;
    }

    /**
     * 获取带图标或边界的二维码
     *
     * @param drawableId
     * @param hasBK
     * @return
     */
    public Bitmap getBitmapWithSingOrBK(int drawableId, Boolean hasBK) {
        mBitmap = toRoundCorner(((BitmapDrawable) mContext.getResources().getDrawable(drawableId)).getBitmap(), 10);

        if (mBitmap != null) {
            Bitmap bitmapWithSign = bitmapWithSign(getBitmap(hasBK));

            return bitmapWithSign;
        } else {
            return getBitmap(hasBK);
        }
    }

    /**
     * 生成二维码矩阵
     *
     * @return
     * @throws WriterException
     */
    private BitMatrix dealData() throws WriterException {

        String content = json.toString();

        Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8"); // 字符编码
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M); // 纠错等级
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content,
                BarcodeFormat.QR_CODE, width, height, hints); // 生成矩阵

        return bitMatrix;
    }

    /**
     * 生成带图标的二维码
     *
     * @param bitmap
     * @return
     */
    private Bitmap bitmapWithSign(Bitmap bitmap) {

        Paint paint = new Paint();
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(0, 0, 0, 0);// 透明色
        Rect outDst = new Rect();
        outDst.left = width * 2 / 5 - 6;
        outDst.top = height * 2 / 5 - 6;
        outDst.right = width * 3 / 5 + 6;
        outDst.bottom = height * 3 / 5 + 6;
        canvas.drawBitmap(((BitmapDrawable) mContext.getResources().getDrawable(
                R.drawable.ewm_bg)).getBitmap(), null, outDst, paint);
        Rect inDst = new Rect();

        inDst.left = width * 2 / 5;
        inDst.top = height * 2 / 5;
        inDst.right = width * 3 / 5;
        inDst.bottom = height * 3 / 5;

        canvas.drawBitmap(mBitmap, null, inDst, paint);

        inDst = null;
        outDst = null;
        paint = null;
        canvas = null;

        return bitmap;
    }

    /**
     * 绘制边界
     *
     * @param paint
     * @param canvas
     */
    private void drawBK(Paint paint, Canvas canvas) {
        paint.setColor(Color.GRAY);
        canvas.drawRect(0, 0, width, 1, paint);
        canvas.drawRect(0, 0, 1, height, paint);
        canvas.drawRect(0, height - 1, width, height + 1, paint);
        canvas.drawRect(width - 1, 0, width, height, paint);
    }

    /**
     * 绘制圆角
     *
     * @param bitmap
     * @param pixels
     * @return
     */
    private Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

}
