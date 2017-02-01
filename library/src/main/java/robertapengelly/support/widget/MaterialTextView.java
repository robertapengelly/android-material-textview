package robertapengelly.support.widget;

import  android.content.Context;
import  android.content.res.TypedArray;
import  android.graphics.Rect;
import  android.graphics.drawable.Drawable;
import  android.os.Build;
import  android.text.method.TransformationMethod;
import  android.util.AttributeSet;
import  android.util.TypedValue;
import  android.view.MotionEvent;
import  android.view.View;
import  android.view.ViewGroup;
import  android.view.accessibility.AccessibilityEvent;
import  android.view.accessibility.AccessibilityNodeInfo;
import  android.widget.TextView;

import  java.lang.reflect.Field;
import  java.util.Locale;

import  robertapengelly.support.graphics.drawable.GradientDrawable;
import  robertapengelly.support.graphics.drawable.LayerDrawable;
import  robertapengelly.support.graphics.drawable.LollipopDrawable;
import  robertapengelly.support.graphics.drawable.LollipopDrawablesCompat;
import  robertapengelly.support.graphics.drawable.StateListDrawable;
import  robertapengelly.support.materialtextview.R;
import  robertapengelly.support.view.DrawableHotspotTouch;

public class MaterialTextView extends TextView {

    private float mElevation = 0, mRadius = 0;
    
    private int mBaseHeight, mBaseWidth, mMinHeight, mMinWidth;
    private int mOriginalMarginBottom, mOriginalMarginTop;
    private int mOriginalPaddingBottom, mOriginalPaddingLeft, mOriginalPaddingRight, mOriginalPaddingTop;
    
    private Drawable mShadow = null;
    private DrawableHotspotTouch mDrawableHotspotTouch;
    
    private Object mEditor;
    
    /**
     * MaterialTextView requires to have a particular minimum size to draw shadows before API 21. If
     * developer also sets min width/height, they might be overridden.
     *
     * MaterialTextView works around this issue by recording user given parameters and using an internal
     * method to set them.
     */
    int mUserSetMinWidth, mUserSetMinHeight;
    
    public MaterialTextView(Context context) {
        this(context, null);
    }
    
    public MaterialTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }
    
    public MaterialTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        mRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics());
        
        if (Build.VERSION.SDK_INT >= 11) {
        
            try {
            
                Field field = TextView.class.getDeclaredField("mEditor");
                field.setAccessible(true);
                
                mEditor = field.get(this);
            
            } catch (Exception ignored) {}
        
        }
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaterialTextView, defStyleAttr, 0);
        
        int ap = a.getResourceId(R.styleable.MaterialTextView_android_textAppearance, -1);
        
        if (ap != -1)
            setTextAppearanceInternal(ap);
        
        int count = a.getIndexCount();
        
        for (int i = 0; i < count; ++i) {
        
            int attr = a.getIndex(i);
            
            if (attr == R.styleable.MaterialTextView_android_background) {
            
                int resid = a.getResourceId(attr, 0);
                
                if (resid != 0)
                    setBackgroundResource(resid);
                else {
                
                    int color = a.getColor(attr, 0);
                    
                    if (color != 0)
                        setBackgroundColor(color);
                
                }
            
            } else if (attr == R.styleable.MaterialTextView_android_textCursorDrawable) {
            
                // Setting the text cursor isn't available on pre-honeycomb devices so we can
                // just continue the loop.
                if (Build.VERSION.SDK_INT < 12)
                    continue;
                
                int resid = a.getResourceId(attr, 0);
                
                try {
                
                    Drawable drawable = LollipopDrawablesCompat.getDrawable(context, resid);
                    
                    Field field = mEditor.getClass().getDeclaredField("mCursorDrawable");
                    field.setAccessible(true);
                    field.set(mEditor, new Drawable[] { drawable, drawable });
                
                } catch (Exception ignored) {}
            
            } else if (attr == R.styleable.MaterialTextView_android_textSelectHandleLeft) {
            
                int resid = a.getResourceId(attr, 0);
                setTextSelectHandle("mSelectHandleLeft", resid);
            
            } else if (attr == R.styleable.MaterialTextView_android_textSelectHandle) {
            
                int resid = a.getResourceId(attr, 0);
                setTextSelectHandle("mSelectHandleCenter", resid);
            
            } else if (attr == R.styleable.MaterialTextView_android_textSelectHandleRight) {
            
                int resid = a.getResourceId(attr, 0);
                setTextSelectHandle("mSelectHandleRight", resid);
            
            } else if (attr == R.styleable.MaterialTextView_background) {
            
                if (getBackground() != null)
                    continue;
                
                int resid = a.getResourceId(attr, 0);
                
                if (resid != 0)
                    setBackgroundResource(resid);
            
            }
        
        }
        
        if (a.hasValue(R.styleable.MaterialTextView_elevation))
            setElevation(a.getDimension(R.styleable.MaterialTextView_elevation, 0));
        
        if (a.hasValue(R.styleable.MaterialTextView_textAllCaps))
            setAllCaps(a.getBoolean(R.styleable.MaterialTextView_textAllCaps, false));
        
        int padding = a.getDimensionPixelOffset(R.styleable.MaterialTextView_android_padding, 0);
        mOriginalPaddingBottom = a.getDimensionPixelOffset(R.styleable.MaterialTextView_android_paddingBottom, padding);
        mOriginalPaddingLeft = a.getDimensionPixelOffset(R.styleable.MaterialTextView_android_paddingLeft, padding);
        mOriginalPaddingRight = a.getDimensionPixelOffset(R.styleable.MaterialTextView_android_paddingRight, padding);
        mOriginalPaddingTop = a.getDimensionPixelOffset(R.styleable.MaterialTextView_android_paddingTop, padding);
        
        mUserSetMinHeight = a.getDimensionPixelSize(R.styleable.MaterialTextView_android_minHeight, 0);
        mUserSetMinWidth = a.getDimensionPixelSize(R.styleable.MaterialTextView_android_minWidth, 0);
        
        a.recycle();
    
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
    
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        
        if (mShadow != null) {
        
            LayerDrawable layers = (LayerDrawable) getBackground();
            
            Drawable background = layers.getDrawable(1);
            
            if (!background.getBounds().contains(x, y))
                return false;
        
        }
        
        return super.dispatchTouchEvent(event);
    
    }
    
    public float getElevation() {
    
        if (Build.VERSION.SDK_INT >= 23)
            return super.getElevation();
        
        return mElevation;
    
    }
    
    @Override
    public int getPaddingBottom() {
        return mOriginalPaddingBottom;
    }
    
    @Override
    public int getPaddingLeft() {
        return mOriginalPaddingLeft;
    }
    
    @Override
    public int getPaddingRight() {
        return mOriginalPaddingRight;
    }
    
    @Override
    public int getPaddingTop() {
        return mOriginalPaddingTop;
    }
    
    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        
        event.setClassName(MaterialTextView.class.getName());
    
    }
    
    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        
        if (Build.VERSION.SDK_INT < 14)
            return;
        
        info.setClassName(MaterialTextView.class.getName());
    
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        if ((mBaseHeight > 0) && (mBaseWidth > 0))
            return;
        
        mBaseHeight = h;
        mBaseWidth = w;
        
        updateShadow();
    
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return (((mDrawableHotspotTouch != null) && mDrawableHotspotTouch.onTouch(this, event)) || super.onTouchEvent(event));
    }
    
    /**
     * Sets the properties of this field to transform input to ALL CAPS
     * display. This may use a "small caps" formatting if available.
     * This setting will be ignored if this field is editable or selectable.
     *
     * This call replaces the current transformation method. Disabling this
     * will not necessarily restore the previous behavior from before this
     * was enabled.
     *
     * @see #setTransformationMethod(TransformationMethod)
     */
    public void setAllCaps(boolean allCaps) {
    
        if (allCaps)
            setTransformationMethod(new AllCapsTransformationMethod(getContext()));
        else
            setTransformationMethod(null);
    
    }
    
    @Override
    public void setBackgroundColor(int color) {
    
        Drawable drawable = new GradientDrawable();
        ((GradientDrawable) drawable).setColor(color);
        
        if (Build.VERSION.SDK_INT < 21) {
        
            RoundRectDrawableWithShadow shadow = new RoundRectDrawableWithShadow(getResources(), mRadius, mElevation);
            shadow.setAlpha(ElevationCompat.getShadowAlphaFromColor(color));
            
            mShadow = shadow;
            
            LayerDrawable layers = new LayerDrawable(new Drawable[] { mShadow, drawable });
            
            //noinspection deprecation
            super.setBackgroundDrawable(layers);
            
            updateShadow();
        
        } else
            //noinspection deprecation
            super.setBackgroundDrawable(drawable);
        
        mDrawableHotspotTouch = null;
    
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public void setBackgroundDrawable(Drawable drawable) {
        super.setBackgroundDrawable(drawable);
        
        if (drawable instanceof LollipopDrawable)
            mDrawableHotspotTouch = new DrawableHotspotTouch((LollipopDrawable) drawable);
        else
            mDrawableHotspotTouch = null;
    
    }
    
    @Override
    public void setBackgroundResource(int resid) {
    
        Drawable drawable = LollipopDrawablesCompat.getDrawable(getContext(), resid);
        
        if (Build.VERSION.SDK_INT < 21) {
        
            String name = ElevationCompat.getResourceName(getResources(), resid);
            
            if (name != null) {
            
                if (name.equals("selector")) {
                
                    StateListDrawable shadows = new StateListDrawable();
                    StateListDrawable states = (StateListDrawable) drawable;
                    
                    RoundRectDrawableWithShadow shadow;
                    
                    int count = states.getStateCount();
                    
                    for (int i = 0; i < count; ++i) {
                    
                        shadow = new RoundRectDrawableWithShadow(getResources(), mRadius, mElevation);
                        shadow.setAlpha(0);
                        
                        shadows.addState(states.getStateSet(i), shadow);
                    
                    }
                    
                    ElevationCompat.getShadowStatesFromResource(getResources(), resid, getContext().getTheme(), shadows);
                    
                    mShadow = shadows;
                
                } else {
                
                    RoundRectDrawableWithShadow shadow = new RoundRectDrawableWithShadow(getResources(), mRadius, mElevation);
                    shadow.setAlpha(ElevationCompat.getShadowAlphaFromDrawable(getResources(),
                        resid, getContext().getTheme()));
                    
                    mShadow = shadow;
                
                }
                
                LayerDrawable layers = new LayerDrawable(new Drawable[] { mShadow, drawable });
                
                //noinspection deprecation
                super.setBackgroundDrawable(layers);
            
            }
            
            updateShadow();
        
        } else
            //noinspection deprecation;
            super.setBackgroundDrawable(drawable);
        
        if (drawable instanceof LollipopDrawable)
            mDrawableHotspotTouch = new DrawableHotspotTouch((LollipopDrawable) drawable);
        else
            mDrawableHotspotTouch = null;
    
    }
    
    public void setElevation(float elevation) {
    
        if (getElevation() != elevation) {
        
            if (Build.VERSION.SDK_INT >= 21) {
            
                super.setElevation(elevation);
                return;
            
            }
            
            mElevation = elevation;
            updateShadow();
        
        }
    
    }
    
    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
    
        if (params instanceof ViewGroup.MarginLayoutParams) {
        
            ViewGroup.MarginLayoutParams mlparams = (ViewGroup.MarginLayoutParams) params;
            
            mOriginalMarginBottom = mlparams.bottomMargin;
            mOriginalMarginTop = mlparams.topMargin;
        
        }
        
        super.setLayoutParams(params);
        
        if (mShadow == null)
            return;
        
        int shadowHeight = 0, shadowWidth = 0;
        
        if (mShadow instanceof StateListDrawable) {
        
            RoundRectDrawableWithShadow shadow;
            StateListDrawable states = (StateListDrawable) mShadow;
            
            int count = states.getStateCount();
            
            for (int i = 0; i < count; ++i) {
            
                shadow = (RoundRectDrawableWithShadow) states.getStateDrawable(i);
                shadow.setShadowSize(mElevation, mElevation);
                
                shadowHeight = Math.max(shadowHeight, (int) Math.ceil(shadow.getMinHeight()));
                shadowWidth = Math.max(shadowWidth, (int) Math.ceil(shadow.getMinWidth()));
            
            }
        
        } else {
        
            RoundRectDrawableWithShadow shadow = (RoundRectDrawableWithShadow) mShadow;
            shadow.setShadowSize(mElevation, mElevation);
            
            shadowHeight = (int) Math.ceil(shadow.getMinHeight());
            shadowWidth = (int) Math.ceil(shadow.getMinWidth());
        
        }
        
        if ((mBaseHeight == 0) || (mBaseWidth == 0))
            return;
        
        mBaseHeight = getMeasuredHeight();
        mBaseWidth = getMeasuredWidth();
        
        mMinHeight = Math.max(mBaseHeight, shadowHeight);
        
        if (mMinHeight < (mBaseHeight + (shadowHeight / 2)))
            mMinHeight += (shadowHeight / 2);
        
        if (mMinHeight <= mUserSetMinHeight)
            mMinHeight = mUserSetMinHeight;
        
        super.setMinHeight(mMinHeight);
        
        mMinWidth = Math.max(mBaseWidth, shadowWidth);
        
        if (mMinWidth < (mBaseWidth + (shadowWidth / 2)))
            mMinWidth += (shadowWidth / 2);
        
        if (mMinWidth <= mUserSetMinWidth)
            mMinWidth = mUserSetMinWidth;
        
        super.setMinWidth(mMinWidth);
        
        int contentHeight = ((mMinHeight / 2) - (mBaseHeight / 2));
        
        LayerDrawable layers = (LayerDrawable) getBackground();
        layers.setLayerInset(1, 0, contentHeight, 0, contentHeight);
        
        super.setPadding(getPaddingLeft(), (getPaddingTop() + contentHeight),
            getPaddingRight(), (getPaddingBottom() + contentHeight));
        
        if (!(params instanceof ViewGroup.MarginLayoutParams))
            return;
        
        ViewGroup.MarginLayoutParams mlparams = (ViewGroup.MarginLayoutParams) params;
        
        mlparams.bottomMargin = (mOriginalMarginBottom - contentHeight);
        mlparams.topMargin = (mOriginalMarginTop - contentHeight);
        
        super.setLayoutParams(mlparams);
    
    }
    
    @Override
    public void setMinimumHeight(int minHeight) {
    
        mUserSetMinHeight = minHeight;
        super.setMinimumHeight(minHeight);
    
    }
    
    @Override
    public void setMinimumWidth(int minWidth) {
    
        mUserSetMinWidth = minWidth;
        super.setMinimumWidth(minWidth);
    
    }
    
    public void setPadding(int left, int top, int right, int bottom) {
    
        if (mOriginalPaddingBottom != bottom)
            mOriginalPaddingBottom = bottom;
        
        if (mOriginalPaddingLeft != left)
            mOriginalPaddingLeft = left;
        
        if (mOriginalPaddingRight != right)
            mOriginalPaddingRight = right;
        
        if (mOriginalPaddingTop != top)
            mOriginalPaddingTop = top;
        
        super.setPadding(left, top, right, bottom);
        
        if (mShadow == null)
            return;
        
        int contentHeight = ((mMinHeight / 2) - (mBaseHeight / 2));
        
        bottom += contentHeight;
        top += contentHeight;
        
        super.setPadding(left, top, right, bottom);
    
    }
    
    public void setTextAppearance(int resid) {
        super.setTextAppearance(resid);
        
        setTextAppearanceInternal(resid);
    
    }
    
    private void setTextAppearanceInternal(int resid) {
    
        TypedArray appearance = getContext().obtainStyledAttributes(resid, R.styleable.TextAppearance);
        
        if (appearance != null) {
        
            if (appearance.hasValue(R.styleable.TextAppearance_textAllCaps))
                setAllCaps(appearance.getBoolean(R.styleable.TextAppearance_textAllCaps, true));
            
            appearance.recycle();
        
        }
    
    }
    
    private void setTextSelectHandle(String field_id, int resid) {
    
        try {
        
            Drawable drawable = LollipopDrawablesCompat.getDrawable(getContext(), resid);
            
            if (mEditor == null) {
            
                final Field field = TextView.class.getDeclaredField(field_id);
                field.setAccessible(true);
                field.set(this, drawable);
            
            } else {
            
                final Field field = mEditor.getClass().getDeclaredField(field_id);
                field.setAccessible(true);
                field.set(mEditor, drawable);
            
            }
        
        } catch (Exception ignored) {}
    
    }
    
    private void updateShadow() {
    
        if (mShadow == null)
            return;
        
        int shadowHeight = 0, shadowWidth = 0;
        
        if (mShadow instanceof StateListDrawable) {
        
            RoundRectDrawableWithShadow shadow;
            StateListDrawable states = (StateListDrawable) mShadow;
            
            int count = states.getStateCount();
            
            for (int i = 0; i < count; ++i) {
            
                shadow = (RoundRectDrawableWithShadow) states.getStateDrawable(i);
                shadow.setShadowSize(mElevation, mElevation);
                
                shadowHeight = Math.max(shadowHeight, (int) Math.ceil(shadow.getMinHeight()));
                shadowWidth = Math.max(shadowWidth, (int) Math.ceil(shadow.getMinWidth()));
            
            }
        
        } else {
        
            RoundRectDrawableWithShadow shadow = (RoundRectDrawableWithShadow) mShadow;
            shadow.setShadowSize(mElevation, mElevation);
            
            shadowHeight = (int) Math.ceil(shadow.getMinHeight());
            shadowWidth = (int) Math.ceil(shadow.getMinWidth());
        
        }
        
        if ((mBaseHeight == 0) || (mBaseWidth == 0))
            return;
        
        mMinHeight = Math.max(mBaseHeight, shadowHeight);
        
        if (mMinHeight < (mBaseHeight + (shadowHeight / 2)))
            mMinHeight += (shadowHeight / 2);
        
        if (mMinHeight <= mUserSetMinHeight)
            mMinHeight = mUserSetMinHeight;
        
        super.setMinHeight(mMinHeight);
        
        mMinWidth = Math.max(mBaseWidth, shadowWidth);
        
        if (mMinWidth < (mBaseWidth + (shadowWidth / 2)))
            mMinWidth += (shadowWidth / 2);
        
        if (mMinWidth <= mUserSetMinWidth)
            mMinWidth = mUserSetMinWidth;
        
        super.setMinWidth(mMinWidth);
        
        int contentHeight = ((mMinHeight / 2) - (mBaseHeight / 2));
        
        LayerDrawable layers = (LayerDrawable) getBackground();
        layers.setLayerInset(1, 0, contentHeight, 0, contentHeight);
        
        super.setPadding(getPaddingLeft(), (getPaddingTop() + contentHeight),
            getPaddingRight(), (getPaddingBottom() + contentHeight));
        
        if (!(getLayoutParams() instanceof ViewGroup.MarginLayoutParams))
            return;
        
        ViewGroup.MarginLayoutParams mlparams = (ViewGroup.MarginLayoutParams) getLayoutParams();
        
        mlparams.bottomMargin = (mOriginalMarginBottom - contentHeight);
        mlparams.topMargin = (mOriginalMarginTop - contentHeight);
        
        super.setLayoutParams(mlparams);
    
    }
    
    /** Transforms source text into an ALL CAPS string, locale-aware. */
    private class AllCapsTransformationMethod implements TransformationMethod {
    
        private Locale mLocale;
        
        AllCapsTransformationMethod(Context context) {
        
            if (Build.VERSION.SDK_INT >= 24)
                mLocale = context.getResources().getConfiguration().getLocales().get(0);
            else
                //noinspection deprecation
                mLocale = context.getResources().getConfiguration().locale;
        
        }
        
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return ((source != null) ? source.toString().toUpperCase(mLocale) : null);
        }
        
        @Override
        public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction,
            Rect previouslyFocusedRect) {}
    
    }

}