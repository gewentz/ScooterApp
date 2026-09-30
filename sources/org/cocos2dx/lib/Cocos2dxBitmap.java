package org.cocos2dx.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/* JADX INFO: loaded from: classes.dex */
public final class Cocos2dxBitmap {
    private static final int HORIZONTAL_ALIGN_CENTER = 3;
    private static final int HORIZONTAL_ALIGN_LEFT = 1;
    private static final int HORIZONTAL_ALIGN_RIGHT = 2;
    private static final int VERTICAL_ALIGN_BOTTOM = 2;
    private static final int VERTICAL_ALIGN_CENTER = 3;
    private static final int VERTICAL_ALIGN_TOP = 1;
    private static Context sContext;

    private static native void nativeInitBitmapDC(int i, int i2, byte[] bArr);

    public static void setContext(Context context) {
        sContext = context;
    }

    public static int getTextHeight(String str, int i, float f, Typeface typeface) {
        TextPaint textPaint = new TextPaint(129);
        textPaint.setTextSize(f);
        textPaint.setTypeface(typeface);
        int length = str.length();
        int i2 = 0;
        int i3 = 0;
        while (i3 < length) {
            String str2 = str;
            int iBreakText = textPaint.breakText(str2, i3, length, true, i, null);
            if (iBreakText == 0) {
                i3++;
            } else {
                i3 += iBreakText;
                i2++;
            }
            str = str2;
        }
        return (int) Math.floor(i2 * (Math.abs(textPaint.ascent()) + Math.abs(textPaint.descent())));
    }

    public static Typeface calculateShrinkTypeFace(String str, int i, int i2, Layout.Alignment alignment, float f, TextPaint textPaint, boolean z) {
        if (i == 0 || i2 == 0) {
            return textPaint.getTypeface();
        }
        float fCeil = i + 1;
        float textHeight = i2 + 1;
        float f2 = f + 1.0f;
        if (z) {
            while (true) {
                if (textHeight <= i2 && fCeil <= i) {
                    break;
                }
                float f3 = f2 - 1.0f;
                StaticLayout staticLayout = new StaticLayout(str, textPaint, i, alignment, 1.0f, 0.0f, false);
                float width = staticLayout.getWidth();
                float lineTop = staticLayout.getLineTop(staticLayout.getLineCount());
                textPaint.setTextSize(f3);
                if (f3 <= 0.0f) {
                    textPaint.setTextSize(f);
                    break;
                }
                textHeight = lineTop;
                fCeil = width;
                f2 = f3;
            }
        } else {
            do {
                if (fCeil > i || textHeight > i2) {
                    f2 -= 1.0f;
                    fCeil = (int) Math.ceil(Layout.getDesiredWidth(str, textPaint));
                    textHeight = getTextHeight(str, (int) fCeil, f2, textPaint.getTypeface());
                    textPaint.setTextSize(f2);
                }
            } while (f2 > 0.0f);
            textPaint.setTextSize(f);
        }
        return textPaint.getTypeface();
    }

    public static boolean createTextBitmapShadowStroke(byte[] bArr, String str, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, boolean z, float f, float f2, float f3, float f4, boolean z2, int i9, int i10, int i11, int i12, float f5, boolean z3, int i13) {
        String str2;
        TextPaint textPaint;
        Layout.Alignment alignment;
        int i14;
        StaticLayout staticLayout;
        int i15;
        int i16;
        int i17;
        int i18 = 0;
        if (bArr != null && bArr.length != 0) {
            String str3 = new String(bArr);
            Layout.Alignment alignment2 = Layout.Alignment.ALIGN_NORMAL;
            int i19 = i6 & 15;
            if (i19 == 2) {
                alignment2 = Layout.Alignment.ALIGN_OPPOSITE;
            } else if (i19 == 3) {
                alignment2 = Layout.Alignment.ALIGN_CENTER;
            }
            Layout.Alignment alignment3 = alignment2;
            TextPaint textPaintNewPaint = newPaint(str, i);
            if (z2) {
                textPaintNewPaint.setStyle(Paint.Style.STROKE);
                textPaintNewPaint.setStrokeWidth(f5);
            }
            int iCeil = i7 <= 0 ? (int) Math.ceil(Layout.getDesiredWidth(str3, textPaintNewPaint)) : i7;
            boolean z4 = true;
            if (i13 == 1 && !z3) {
                staticLayout = new StaticLayout(str3, textPaintNewPaint, (int) Math.ceil(Layout.getDesiredWidth(str3, textPaintNewPaint)), alignment3, 1.0f, 0.0f, false);
                i15 = 3;
                i16 = 2;
                i14 = i7;
            } else {
                if (i13 == 2) {
                    str2 = str3;
                    textPaint = textPaintNewPaint;
                    alignment = alignment3;
                    i14 = i7;
                    calculateShrinkTypeFace(str2, i14, i8, alignment, i, textPaint, z3);
                } else {
                    str2 = str3;
                    textPaint = textPaintNewPaint;
                    alignment = alignment3;
                    i14 = i7;
                }
                String str4 = str2;
                Layout.Alignment alignment4 = alignment;
                textPaintNewPaint = textPaint;
                z4 = true;
                i15 = 3;
                i16 = 2;
                staticLayout = new StaticLayout(str4, textPaintNewPaint, iCeil, alignment4, 1.0f, 0.0f, false);
            }
            int width = staticLayout.getWidth();
            int lineTop = staticLayout.getLineTop(staticLayout.getLineCount());
            int iMax = Math.max(width, i14);
            int i20 = i8 > 0 ? i8 : lineTop;
            if (i13 == z4 && !z3 && i14 > 0) {
                iMax = i14;
            }
            if (iMax != 0 && i20 != 0) {
                if (i19 == i15) {
                    i17 = (iMax - width) / i16;
                } else {
                    i17 = i19 == i16 ? iMax - width : 0;
                }
                int i21 = (i6 >> 4) & 15;
                if (i21 == i16) {
                    i18 = i20 - lineTop;
                } else if (i21 == i15) {
                    i18 = (i20 - lineTop) / 2;
                }
                Bitmap bitmapCreateBitmap = Bitmap.createBitmap(iMax, i20, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmapCreateBitmap);
                canvas.translate(i17, i18);
                if (z2) {
                    textPaintNewPaint.setARGB(i12, i9, i10, i11);
                    staticLayout.draw(canvas);
                }
                textPaintNewPaint.setStyle(Paint.Style.FILL);
                textPaintNewPaint.setARGB(i5, i2, i3, i4);
                staticLayout.draw(canvas);
                initNativeObject(bitmapCreateBitmap);
                return z4;
            }
        }
        return false;
    }

    private static TextPaint newPaint(String str, int i) {
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(i);
        textPaint.setAntiAlias(true);
        if (str.endsWith(".ttf")) {
            try {
                textPaint.setTypeface(Cocos2dxTypefaces.get(sContext, str));
                return textPaint;
            } catch (Exception unused) {
                Log.e("Cocos2dxBitmap", "error to create ttf type face: " + str);
                textPaint.setTypeface(Typeface.create(str, 0));
                return textPaint;
            }
        }
        textPaint.setTypeface(Typeface.create(str, 0));
        return textPaint;
    }

    private static void initNativeObject(Bitmap bitmap) {
        byte[] pixels = getPixels(bitmap);
        if (pixels == null) {
            return;
        }
        nativeInitBitmapDC(bitmap.getWidth(), bitmap.getHeight(), pixels);
    }

    private static byte[] getPixels(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        byte[] bArr = new byte[bitmap.getWidth() * bitmap.getHeight() * 4];
        ByteBuffer byteBufferWrap = ByteBuffer.wrap(bArr);
        byteBufferWrap.order(ByteOrder.nativeOrder());
        bitmap.copyPixelsToBuffer(byteBufferWrap);
        return bArr;
    }

    public static int getFontSizeAccordingHeight(int i) {
        TextPaint textPaint = new TextPaint();
        Rect rect = new Rect();
        textPaint.setTypeface(Typeface.DEFAULT);
        boolean z = false;
        int i2 = 1;
        while (!z) {
            textPaint.setTextSize(i2);
            textPaint.getTextBounds("SghMNy", 0, 6, rect);
            i2++;
            if (i - rect.height() <= 2) {
                z = true;
            }
        }
        return i2;
    }

    private static String getStringWithEllipsis(String str, float f, float f2) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        TextPaint textPaint = new TextPaint();
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextSize(f2);
        return TextUtils.ellipsize(str, textPaint, f, TextUtils.TruncateAt.END).toString();
    }
}
