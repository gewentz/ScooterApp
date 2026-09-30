package org.cocos2dx.lib;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;

/* JADX INFO: loaded from: classes.dex */
public class Cocos2dxEditBoxHelper {
    private static final String TAG = "Cocos2dxEditBoxHelper";
    private static Cocos2dxActivity mCocos2dxActivity = null;
    private static SparseArray<Cocos2dxEditBox> mEditBoxArray = null;
    private static ResizeLayout mFrameLayout = null;
    private static float mPadding = 5.0f;
    private static int mViewTag;

    private static native void editBoxEditingChanged(int i, String str);

    private static native void editBoxEditingDidBegin(int i);

    private static native void editBoxEditingDidEnd(int i, String str, int i2);

    public static void __editBoxEditingDidBegin(int i) {
        editBoxEditingDidBegin(i);
    }

    public static void __editBoxEditingChanged(int i, String str) {
        editBoxEditingChanged(i, str);
    }

    public static void __editBoxEditingDidEnd(int i, String str, int i2) {
        editBoxEditingDidEnd(i, str, i2);
    }

    public Cocos2dxEditBoxHelper(ResizeLayout resizeLayout) {
        mFrameLayout = resizeLayout;
        mCocos2dxActivity = (Cocos2dxActivity) Cocos2dxActivity.getContext();
        mEditBoxArray = new SparseArray<>();
    }

    public static int getPadding(float f) {
        return (int) (mPadding * f);
    }

    /* JADX INFO: renamed from: org.cocos2dx.lib.Cocos2dxEditBoxHelper$1, reason: invalid class name */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ int val$height;
        final /* synthetic */ int val$index;
        final /* synthetic */ int val$left;
        final /* synthetic */ float val$scaleX;
        final /* synthetic */ int val$top;
        final /* synthetic */ int val$width;

        AnonymousClass1(float f, int i, int i2, int i3, int i4, int i5) {
            this.val$scaleX = f;
            this.val$left = i;
            this.val$top = i2;
            this.val$width = i3;
            this.val$height = i4;
            this.val$index = i5;
        }

        @Override // java.lang.Runnable
        public void run() {
            final Cocos2dxEditBox cocos2dxEditBox = new Cocos2dxEditBox(Cocos2dxEditBoxHelper.mCocos2dxActivity);
            cocos2dxEditBox.setFocusable(true);
            cocos2dxEditBox.setFocusableInTouchMode(true);
            cocos2dxEditBox.setInputFlag(5);
            cocos2dxEditBox.setInputMode(6);
            cocos2dxEditBox.setReturnType(0);
            cocos2dxEditBox.setHintTextColor(-7829368);
            cocos2dxEditBox.setVisibility(8);
            cocos2dxEditBox.setBackgroundColor(0);
            cocos2dxEditBox.setTextColor(-1);
            cocos2dxEditBox.setSingleLine();
            cocos2dxEditBox.setOpenGLViewScaleX(this.val$scaleX);
            cocos2dxEditBox.setPadding(Cocos2dxEditBoxHelper.getPadding(this.val$scaleX), 0, 0, 0);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
            layoutParams.leftMargin = this.val$left;
            layoutParams.topMargin = this.val$top;
            layoutParams.width = this.val$width;
            layoutParams.height = this.val$height;
            layoutParams.gravity = 51;
            Cocos2dxEditBoxHelper.mFrameLayout.addView(cocos2dxEditBox, layoutParams);
            cocos2dxEditBox.setTag(Boolean.FALSE);
            cocos2dxEditBox.addTextChangedListener(new TextWatcher() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.1.1
                @Override // android.text.TextWatcher
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override // android.text.TextWatcher
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override // android.text.TextWatcher
                public void afterTextChanged(final Editable editable) {
                    if (!cocos2dxEditBox.getChangedTextProgrammatically().booleanValue() && ((Boolean) cocos2dxEditBox.getTag()).booleanValue()) {
                        Cocos2dxEditBoxHelper.mCocos2dxActivity.runOnGLThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.1.1.1
                            @Override // java.lang.Runnable
                            public void run() {
                                Cocos2dxEditBoxHelper.__editBoxEditingChanged(AnonymousClass1.this.val$index, editable.toString());
                            }
                        });
                    }
                    cocos2dxEditBox.setChangedTextProgrammatically(Boolean.FALSE);
                }
            });
            cocos2dxEditBox.setOnFocusChangeListener(new View.OnFocusChangeListener() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.1.2
                @Override // android.view.View.OnFocusChangeListener
                public void onFocusChange(View view, boolean z) {
                    cocos2dxEditBox.setTag(Boolean.TRUE);
                    cocos2dxEditBox.setChangedTextProgrammatically(Boolean.FALSE);
                    if (z) {
                        Cocos2dxEditBoxHelper.mCocos2dxActivity.runOnGLThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.1.2.1
                            @Override // java.lang.Runnable
                            public void run() {
                                AnonymousClass2 anonymousClass2 = AnonymousClass2.this;
                                cocos2dxEditBox.endAction = 0;
                                Cocos2dxEditBoxHelper.__editBoxEditingDidBegin(AnonymousClass1.this.val$index);
                            }
                        });
                        Cocos2dxEditBox cocos2dxEditBox2 = cocos2dxEditBox;
                        cocos2dxEditBox2.setSelection(cocos2dxEditBox2.getText().length());
                        Cocos2dxEditBoxHelper.mFrameLayout.setEnableForceDoLayout(true);
                        Cocos2dxEditBoxHelper.mCocos2dxActivity.getGLSurfaceView().setSoftKeyboardShown(true);
                        Log.d(Cocos2dxEditBoxHelper.TAG, "edit box get focus");
                        return;
                    }
                    cocos2dxEditBox.setVisibility(8);
                    final String str = new String(cocos2dxEditBox.getText().toString());
                    Cocos2dxEditBoxHelper.mCocos2dxActivity.runOnGLThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.1.2.2
                        @Override // java.lang.Runnable
                        public void run() {
                            AnonymousClass2 anonymousClass2 = AnonymousClass2.this;
                            Cocos2dxEditBoxHelper.__editBoxEditingDidEnd(AnonymousClass1.this.val$index, str, cocos2dxEditBox.endAction);
                        }
                    });
                    Cocos2dxEditBoxHelper.mCocos2dxActivity.hideVirtualButton();
                    Cocos2dxEditBoxHelper.mFrameLayout.setEnableForceDoLayout(false);
                    Log.d(Cocos2dxEditBoxHelper.TAG, "edit box lose focus");
                }
            });
            cocos2dxEditBox.setOnKeyListener(new View.OnKeyListener() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.1.3
                @Override // android.view.View.OnKeyListener
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (keyEvent.getAction() != 0 || i != 66 || (cocos2dxEditBox.getInputType() & 131072) == 131072) {
                        return false;
                    }
                    Cocos2dxEditBoxHelper.closeKeyboardOnUiThread(AnonymousClass1.this.val$index);
                    return true;
                }
            });
            cocos2dxEditBox.setOnEditorActionListener(new TextView.OnEditorActionListener() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.1.4
                @Override // android.widget.TextView.OnEditorActionListener
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == 5) {
                        cocos2dxEditBox.endAction = 1;
                        Cocos2dxEditBoxHelper.closeKeyboardOnUiThread(AnonymousClass1.this.val$index);
                        return true;
                    }
                    if (i != 6 && i != 4 && i != 3 && i != 2) {
                        return false;
                    }
                    cocos2dxEditBox.endAction = 3;
                    Cocos2dxEditBoxHelper.closeKeyboardOnUiThread(AnonymousClass1.this.val$index);
                    return false;
                }
            });
            Cocos2dxEditBoxHelper.mEditBoxArray.put(this.val$index, cocos2dxEditBox);
        }
    }

    public static int createEditBox(int i, int i2, int i3, int i4, float f) {
        mCocos2dxActivity.runOnUiThread(new AnonymousClass1(f, i, i2, i3, i4, mViewTag));
        int i5 = mViewTag;
        mViewTag = i5 + 1;
        return i5;
    }

    public static void removeEditBox(final int i) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.2
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox cocos2dxEditBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(i);
                if (cocos2dxEditBox != null) {
                    Cocos2dxEditBoxHelper.mEditBoxArray.remove(i);
                    Cocos2dxEditBoxHelper.mFrameLayout.removeView(cocos2dxEditBox);
                    Log.e(Cocos2dxEditBoxHelper.TAG, "remove EditBox");
                }
            }
        });
    }

    public static void setFont(final int i, final String str, final float f) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.3
            @Override // java.lang.Runnable
            public void run() {
                Typeface typefaceCreate;
                Cocos2dxEditBox cocos2dxEditBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(i);
                if (cocos2dxEditBox != null) {
                    if (!str.isEmpty()) {
                        if (str.endsWith(".ttf")) {
                            try {
                                typefaceCreate = Cocos2dxTypefaces.get(Cocos2dxActivity.getContext(), str);
                            } catch (Exception unused) {
                                Log.e("Cocos2dxEditBoxHelper", "error to create ttf type face: " + str);
                                typefaceCreate = Typeface.create(str, 0);
                            }
                        } else {
                            typefaceCreate = Typeface.create(str, 0);
                        }
                    } else {
                        typefaceCreate = Typeface.DEFAULT;
                    }
                    float f2 = f;
                    if (f2 >= 0.0f) {
                        cocos2dxEditBox.setTextSize(0, f2);
                    }
                    cocos2dxEditBox.setTypeface(typefaceCreate);
                }
            }
        });
    }

    public static void setFontColor(final int i, final int i2, final int i3, final int i4, final int i5) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.4
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox cocos2dxEditBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(i);
                if (cocos2dxEditBox != null) {
                    cocos2dxEditBox.setTextColor(Color.argb(i5, i2, i3, i4));
                }
            }
        });
    }

    public static void setPlaceHolderText(final int i, final String str) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.5
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox cocos2dxEditBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(i);
                if (cocos2dxEditBox != null) {
                    cocos2dxEditBox.setHint(str);
                }
            }
        });
    }

    public static void setPlaceHolderTextColor(final int i, final int i2, final int i3, final int i4, final int i5) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.6
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox cocos2dxEditBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(i);
                if (cocos2dxEditBox != null) {
                    cocos2dxEditBox.setHintTextColor(Color.argb(i5, i2, i3, i4));
                }
            }
        });
    }

    public static void setMaxLength(final int i, final int i2) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.7
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox cocos2dxEditBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(i);
                if (cocos2dxEditBox != null) {
                    cocos2dxEditBox.setMaxLength(i2);
                }
            }
        });
    }

    public static void setVisible(final int i, final boolean z) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.8
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox cocos2dxEditBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(i);
                if (cocos2dxEditBox != null) {
                    cocos2dxEditBox.setVisibility(z ? 0 : 8);
                }
            }
        });
    }

    public static void setText(final int i, final String str) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.9
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox cocos2dxEditBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(i);
                if (cocos2dxEditBox != null) {
                    cocos2dxEditBox.setChangedTextProgrammatically(Boolean.TRUE);
                    cocos2dxEditBox.setText(str);
                    cocos2dxEditBox.setSelection(cocos2dxEditBox.getText().length());
                }
            }
        });
    }

    public static void setReturnType(final int i, final int i2) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.10
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox cocos2dxEditBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(i);
                if (cocos2dxEditBox != null) {
                    cocos2dxEditBox.setReturnType(i2);
                }
            }
        });
    }

    public static void setTextHorizontalAlignment(final int i, final int i2) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.11
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox cocos2dxEditBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(i);
                if (cocos2dxEditBox != null) {
                    cocos2dxEditBox.setTextHorizontalAlignment(i2);
                }
            }
        });
    }

    public static void setInputMode(final int i, final int i2) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.12
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox cocos2dxEditBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(i);
                if (cocos2dxEditBox != null) {
                    cocos2dxEditBox.setInputMode(i2);
                }
            }
        });
    }

    public static void setInputFlag(final int i, final int i2) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.13
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox cocos2dxEditBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(i);
                if (cocos2dxEditBox != null) {
                    cocos2dxEditBox.setInputFlag(i2);
                }
            }
        });
    }

    public static void setEditBoxViewRect(final int i, final int i2, final int i3, final int i4, final int i5) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.14
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox cocos2dxEditBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(i);
                if (cocos2dxEditBox != null) {
                    cocos2dxEditBox.setEditBoxViewRect(i2, i3, i4, i5);
                }
            }
        });
    }

    public static void openKeyboard(final int i) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.15
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBoxHelper.openKeyboardOnUiThread(i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void openKeyboardOnUiThread(int i) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Log.e(TAG, "openKeyboardOnUiThread doesn't run on UI thread!");
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) Cocos2dxActivity.getContext().getSystemService("input_method");
        Cocos2dxEditBox cocos2dxEditBox = mEditBoxArray.get(i);
        if (cocos2dxEditBox != null) {
            cocos2dxEditBox.requestFocus();
            mCocos2dxActivity.getGLSurfaceView().requestLayout();
            inputMethodManager.showSoftInput(cocos2dxEditBox, 0);
            mCocos2dxActivity.getGLSurfaceView().setSoftKeyboardShown(true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void closeKeyboardOnUiThread(int i) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Log.e(TAG, "closeKeyboardOnUiThread doesn't run on UI thread!");
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) Cocos2dxActivity.getContext().getSystemService("input_method");
        Cocos2dxEditBox cocos2dxEditBox = mEditBoxArray.get(i);
        if (cocos2dxEditBox != null) {
            inputMethodManager.hideSoftInputFromWindow(cocos2dxEditBox.getWindowToken(), 0);
            mCocos2dxActivity.getGLSurfaceView().setSoftKeyboardShown(false);
            mCocos2dxActivity.getGLSurfaceView().requestFocus();
            mCocos2dxActivity.hideVirtualButton();
        }
    }

    public static void closeKeyboard(final int i) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.16
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBoxHelper.closeKeyboardOnUiThread(i);
            }
        });
    }
}
