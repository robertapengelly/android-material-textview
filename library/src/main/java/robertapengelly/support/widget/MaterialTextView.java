package robertapengelly.support.widget;

import  android.content.Context;
import  android.content.res.ColorStateList;
import  android.content.res.Resources;
import  android.content.res.Resources.Theme;
import  android.content.res.TypedArray;
import  android.content.res.XmlResourceParser;
import  android.graphics.Canvas;
import  android.graphics.Color;
import  android.graphics.LinearGradient;
import  android.graphics.Paint;
import  android.graphics.Path;
import  android.graphics.RadialGradient;
import  android.graphics.Rect;
import  android.graphics.RectF;
import  android.graphics.Shader;
import  android.graphics.drawable.Drawable;
import  android.os.Build;
import  android.text.method.TransformationMethod;
import  android.util.AttributeSet;
import  android.util.TypedValue;
import  android.util.Xml;
import  android.view.MotionEvent;
import  android.view.View;
import  android.view.ViewGroup;
import  android.view.accessibility.AccessibilityEvent;
import  android.view.accessibility.AccessibilityNodeInfo;
import  android.widget.LinearLayout;
import  android.widget.RelativeLayout;
import  android.widget.TextView;

import  java.io.IOException;
import  java.lang.reflect.Field;
import  java.util.Locale;

import  org.xmlpull.v1.XmlPullParser;
import  org.xmlpull.v1.XmlPullParserException;

import  robertapengelly.support.graphics.drawable.GradientDrawable;
import  robertapengelly.support.graphics.drawable.LayerDrawable;
import  robertapengelly.support.graphics.drawable.LollipopDrawable;
import  robertapengelly.support.graphics.drawable.LollipopDrawablesCompat;
import  robertapengelly.support.materialtextview.R;
import  robertapengelly.support.view.DrawableHotspotTouch;

public class MaterialTextView extends TextView {

    // used to calculate content padding
    private final static double COS_45 = Math.cos(Math.toRadians(45));
    
    private final static float SHADOW_MULTIPLIER = 1.5f;
    
    // extra shadow to avoid gaps between view and shadow
    private final int mInsetShadow;
    
    private final int mShadowEndColor;
    private final int mShadowStartColor;
    
    private final RectF mBounds;
    
    private float mElevation = 0;
    
    // actual value set by developer
    private float mRawShadowSize;
    
    // multiplied value to account for shadow offset
    private float mShadowSize;
    
    private int mDefaultPaddingBottom;
    private int mDefaultPaddingLeft;
    private int mDefaultPaddingRight;
    private int mDefaultPaddingTop;
    
    private int mElevationAlpha = 0;
    
    private DrawableHotspotTouch mDrawableHotspotTouch;
    private Drawable mBackground;
    
    private Object mEditor;
    
    private Paint mCornerShadowPaint;
    private Paint mEdgeShadowPaint;
    
    private Path mCornerShadowPath;
    
    public MaterialTextView(Context context) {
        this(context, null);
    }
    
    public MaterialTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textStyle);
    }
    
    public MaterialTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
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
                
                if (resid != 0) {
                
                    setBackgroundResource(resid);
                    continue;
                
                }
                
                int color = a.getColor(attr, 0);
                
                if (color != 0)
                    setBackgroundColor(color);
            
            } else if (attr == R.styleable.MaterialTextView_android_textCursorDrawable) {
            
                // Setting the text cursor isn't available on pre-honeycomb devices so we can
                // just continue the loop.
                if (Build.VERSION.SDK_INT < 12)
                    continue;
                
                int resid = a.getResourceId(attr, 0);
                
                try {
                
                    Drawable drawable = LollipopDrawablesCompat.getDrawable(getResources(), resid, getContext().getTheme());
                    
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
            
                int resid = a.getResourceId(attr, 0);
                
                if (resid != 0) {
                
                    setBackgroundResource(resid);
                    continue;
                
                }
                
                int color = a.getColor(attr, 0);
                
                if (color != 0)
                    setBackgroundColor(color);
            
            }
        
        }
        
        int defaultPadding = a.getDimensionPixelSize(R.styleable.MaterialTextView_android_padding, 0);
        
        mDefaultPaddingBottom = a.getDimensionPixelSize(R.styleable.MaterialTextView_android_paddingBottom, defaultPadding);
        mDefaultPaddingLeft = a.getDimensionPixelSize(R.styleable.MaterialTextView_android_paddingLeft, defaultPadding);
        mDefaultPaddingRight = a.getDimensionPixelSize(R.styleable.MaterialTextView_android_paddingRight, defaultPadding);
        mDefaultPaddingTop = a.getDimensionPixelSize(R.styleable.MaterialTextView_android_paddingTop, defaultPadding);
        
        if (a.hasValue(R.styleable.MaterialTextView_elevation))
            setElevation(a.getDimensionPixelOffset(R.styleable.MaterialTextView_elevation, 0));
        
        if (a.hasValue(R.styleable.MaterialTextView_textAllCaps))
            setAllCaps(a.getBoolean(R.styleable.MaterialTextView_textAllCaps, false));
        
        a.recycle();
        
        mBounds = new RectF();
        
        mCornerShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mCornerShadowPaint.setAlpha(mElevationAlpha);
        mCornerShadowPaint.setStyle(Paint.Style.FILL);
        
        mEdgeShadowPaint = new Paint(mCornerShadowPaint);
        mEdgeShadowPaint.setAlpha(mElevationAlpha);
        mEdgeShadowPaint.setAntiAlias(false);
        
        // shadow variables
        mInsetShadow = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        mShadowEndColor = Color.parseColor("#03000000");
        mShadowStartColor = Color.parseColor("#37000000");
    
    }
    
    private boolean buildShadowCorners() {
    
        if (mShadowSize <= 0)
            return false;
        
        RectF innerBounds = new RectF(0, 0, 0, 0);
        
        RectF outerBounds = new RectF(innerBounds);
        outerBounds.inset(-mShadowSize, -mShadowSize);

        if (mCornerShadowPath == null)
            mCornerShadowPath = new Path();
        else
            mCornerShadowPath.reset();
        
        mCornerShadowPath.setFillType(Path.FillType.EVEN_ODD);
        
        mCornerShadowPath.moveTo(0, 0);
        mCornerShadowPath.rLineTo(-mShadowSize, 0);
        
        // outer arc
        mCornerShadowPath.arcTo(outerBounds, 180f, 90f, false);
        
        // inner arc
        mCornerShadowPath.arcTo(innerBounds, 270f, -90f, false);
        
        mCornerShadowPath.close();
        
        float startRatio = (0 / mShadowSize);
        mCornerShadowPaint.setShader(new RadialGradient(0, 0, mShadowSize,
            new int[]{mShadowStartColor, mShadowStartColor, mShadowEndColor},
                new float[] { 0f, startRatio, 1f }, Shader.TileMode.CLAMP));

        // we offset the content shadowSize/2 pixels up to make it more realistic.
        // this is why edge shadow shader has some extra space
        // When drawing bottom edge shadow, we use that extra space.
        mEdgeShadowPaint.setShader(new LinearGradient(0, mShadowSize, 0,
            -mShadowSize, new int[]{mShadowStartColor, mShadowStartColor, mShadowEndColor},
                new float[] { 0f, .5f, 1f }, Shader.TileMode.CLAMP));
        
        mEdgeShadowPaint.setAntiAlias(false);
        
        return true;
    
    }
    
    @Override
    public void draw(Canvas canvas) {
    
        if (Build.VERSION.SDK_INT < 21) {
        
            canvas.translate(0, mRawShadowSize / 2);
            drawShadow(canvas);
            canvas.translate(0, -mRawShadowSize / 2);
        
        }
        
        super.draw(canvas);
    
    }
    
    private void drawShadow(Canvas canvas) {
    
        if (mCornerShadowPath == null)
            return;
        
        final float edgeShadowTop = -mShadowSize;
        final float inset = mInsetShadow + (mRawShadowSize / 2);
        
        final boolean drawHorizontalEdges = ((mBounds.width() - 2 * inset) > 0);
        final boolean drawVerticalEdges = ((mBounds.height() - 2 * inset) > 0);
        
        // LT
        int saved = canvas.save();
        
        canvas.translate((mBounds.left + inset), (mBounds.top + inset));
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        
        if (drawHorizontalEdges)
            canvas.drawRect(0, edgeShadowTop, (mBounds.width() - 2 * inset), 0, mEdgeShadowPaint);
        
        canvas.restoreToCount(saved);
        
        // RB
        saved = canvas.save();
        
        canvas.translate((mBounds.right - inset), (mBounds.bottom - inset));
        canvas.rotate(180f);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        
        if (drawHorizontalEdges)
            canvas.drawRect(0, edgeShadowTop, (mBounds.width() - 2 * inset), mShadowSize, mEdgeShadowPaint);
        
        canvas.restoreToCount(saved);
        
        // LB
        saved = canvas.save();
        
        canvas.translate((mBounds.left + inset), (mBounds.bottom - inset));
        canvas.rotate(270f);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        
        if (drawVerticalEdges)
            canvas.drawRect(0, edgeShadowTop, (mBounds.height() - 2 * inset), 0, mEdgeShadowPaint);
        
        canvas.restoreToCount(saved);
        
        // RT
        saved = canvas.save();
        
        canvas.translate((mBounds.right - inset), (mBounds.top + inset));
        canvas.rotate(90f);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        
        if (drawVerticalEdges)
            canvas.drawRect(0, edgeShadowTop, (mBounds.height() - 2 * inset), 0, mEdgeShadowPaint);
        
        canvas.restoreToCount(saved);
    
    }
    
    @Override
    public Drawable getBackground() {
    
        if (mBackground == null)
            return super.getBackground();
        
        return mBackground;
    
    }
    
    public float getElevation() {
    
        if (Build.VERSION.SDK_INT >= 21)
            return super.getElevation();
        
        return mElevation;
    
    }
    
    private void getElevationAlphaFromColor(int color) {
    
        String hexColor = String.format("#%06X", color);
        mElevationAlpha = Integer.parseInt(hexColor.substring(1, 3), 16);
        
        android.util.Log.i("MaterialTextView", "Hex: " + hexColor + ",  Alpha: " + mElevationAlpha);
    
    }
    
    private void getElevationAlphaFromLayerList(Resources r, XmlPullParser parser, AttributeSet attrs)
        throws XmlPullParserException, IOException {
        
        final int innerDepth = (parser.getDepth() + 1);
        
        int depth, type;
        
        while (((type = parser.next()) != XmlPullParser.END_DOCUMENT) && (((depth = parser.getDepth()) >= innerDepth)
            || (type != XmlPullParser.END_TAG))) {
            
            if (type != XmlPullParser.START_TAG)
                continue;
            
            if ((depth > innerDepth) || !parser.getName().equals("item"))
                continue;
            
            final TypedArray a = obtainAttributes(r, getContext().getTheme(), attrs, R.styleable.LayerDrawableItem);
            
            int resid = a.getResourceId(R.styleable.LayerDrawableItem_android_drawable, 0);
            
            if (resid != 0) {
            
                getElevationAlphaFromResource(resid);
                return;
            
            }
            
            //noinspection StatementWithEmptyBody
            while ((type = parser.next()) == XmlPullParser.TEXT);
            
            if (type != XmlPullParser.START_TAG)
                throw new XmlPullParserException(parser.getPositionDescription()
                    + ": <item> tag requires a 'drawable' attribute or child tag defining a drawable");
            
            getElevationAlphaFromResourceXmlInner(r, parser, attrs);
            
            a.recycle();
        
        }
    
    }
    
    private void getElevationAlphaFromResource(int resid) {
    
        Resources res = getResources();
        TypedValue value = new TypedValue();
        
        res.getValue(resid, value, true);
        
         if (value.string == null)
            throw new Resources.NotFoundException("Resource \"" + res.getResourceName(value.resourceId) + "\" (" +
                Integer.toHexString(value.resourceId) + ")  is not a Drawable (color or path): " + value);
        
        String file = value.string.toString();
        
        if (file.endsWith(".xml")) {
        
            try {
            
                XmlResourceParser rp = res.getAssets().openXmlResourceParser(value.assetCookie, file);
                getElevationAlphaFromResourceXml(res, rp);
                
                rp.close();
            
            } catch (Exception ignored) {}
        
        }
    
    }
    
    private void getElevationAlphaFromResourceXml(Resources r, XmlPullParser parser) throws XmlPullParserException, IOException {
    
        AttributeSet attrs = Xml.asAttributeSet(parser);
        
        int type;
        
        //noinspection StatementWithEmptyBody
        while (((type = parser.next()) != XmlPullParser.START_TAG) && (type != XmlPullParser.END_DOCUMENT));
        
        if (type != XmlPullParser.START_TAG)
            throw new XmlPullParserException("No start tag found");
        
        getElevationAlphaFromResourceXmlInner(r, parser, attrs);
    
    }
    
    private void getElevationAlphaFromResourceXmlInner(Resources r, XmlPullParser parser, AttributeSet attrs)
        throws XmlPullParserException, IOException {
        
        String name = parser.getName();
        
        try {
        
            switch (name) {
            
                case "layer-list":
                    getElevationAlphaFromLayerList(r, parser, attrs);
                    break;
                case "ripple":
                    getElevationAlphaFromRipple(r, parser, attrs);
                    break;
                case "shape":
                    getElevationAlphaFromShape(r, parser, attrs);
                    break;
            
            }
        
        } catch (Exception ignored) {}
    
    }
    
    private void getElevationAlphaFromRipple(Resources r, XmlPullParser parser, AttributeSet attrs)
        throws XmlPullParserException, IOException {
        
        final int innerDepth = (parser.getDepth() + 1);
        
        int depth, type;
        
        while (((type = parser.next()) != XmlPullParser.END_DOCUMENT) && (((depth = parser.getDepth()) >= innerDepth)
            || (type != XmlPullParser.END_TAG))) {
            
            if (type != XmlPullParser.START_TAG)
                continue;
            
            if ((depth > innerDepth) || !parser.getName().equals("item"))
                continue;
            
            final TypedArray a = obtainAttributes(r, getContext().getTheme(), attrs, R.styleable.LayerDrawableItem);
            
            int id = a.getResourceId(R.styleable.LayerDrawableItem_android_id, View.NO_ID);
            
            if (id != android.R.id.mask) {
            
                int resid = a.getResourceId(R.styleable.LayerDrawableItem_android_drawable, 0);
                
                if (resid != 0) {
                
                    getElevationAlphaFromResource(resid);
                    return;
                
                }
                
                //noinspection StatementWithEmptyBody
                while ((type = parser.next()) == XmlPullParser.TEXT);
                
                if (type != XmlPullParser.START_TAG)
                    throw new XmlPullParserException(parser.getPositionDescription()
                        + ": <item> tag requires a 'drawable' attribute or child tag defining a drawable");
                
                getElevationAlphaFromResourceXmlInner(r, parser, attrs);
            
            }
            
            a.recycle();
        
        }
    
    }
    
    private void getElevationAlphaFromShape(Resources r, XmlPullParser parser, AttributeSet attrs)
        throws XmlPullParserException, IOException {
        
        String name;
        TypedArray a;
        
        final int innerDepth = (parser.getDepth() + 1);
        int depth, type;
        
        while (((type = parser.next()) != XmlPullParser.END_DOCUMENT)
            && (((depth = parser.getDepth()) >= innerDepth) || (type != XmlPullParser.END_TAG))) {
            
            if (type != XmlPullParser.START_TAG)
                continue;
            
            if (depth > innerDepth)
                continue;
            
            name = parser.getName();
            
            if (name.equals("solid")) {
            
                a = obtainAttributes(r, getContext().getTheme(), attrs, R.styleable.GradientDrawableSolid);
                
                final ColorStateList colorStateList = a.getColorStateList(R.styleable.GradientDrawableSolid_android_color);
                
                a.recycle();
                
                if (colorStateList != null ) {
                
                    int color = colorStateList.getDefaultColor();
                    
                    if (color != 0)
                        getElevationAlphaFromColor(color);
                
                }
            
            }
        
        }
    
    }
    
    @Override
    public int getPaddingBottom() {
        return mDefaultPaddingBottom;
    }
    
    @Override
    public int getPaddingLeft() {
        return mDefaultPaddingLeft;
    }
    
    @Override
    public int getPaddingRight() {
        return mDefaultPaddingRight;
    }
    
    @Override
    public int getPaddingTop() {
        return mDefaultPaddingTop;
    }
    
    /**
     * Obtains styled attributes from the theme, if available, or unstyled
     * resources if the theme is null.
     */
    private static TypedArray obtainAttributes(Resources res, Theme theme, AttributeSet set, int[] attrs) {
    
        if (theme == null)
            return res.obtainAttributes(set, attrs);
        
        return theme.obtainStyledAttributes(set, attrs, 0, 0);
    
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
        
        // View is offset SHADOW_MULTIPLIER * maxShadowSize to account for the shadow shift.
        // We could have different top-bottom offsets to avoid extra gap above but in that case
        // center aligning Views inside the CardView would be problematic.
        final float verticalOffset = (mRawShadowSize * SHADOW_MULTIPLIER);
        mBounds.set(mRawShadowSize, verticalOffset, (w - mRawShadowSize), (h - verticalOffset));
        
        buildShadowCorners();
        
        if ((mBackground != null) && (super.getBackground() != mBackground)) {
        
            int vOffset = (int) Math.ceil((float) (mRawShadowSize * SHADOW_MULTIPLIER + (1 - COS_45) * 0));
            
            LayerDrawable background = (LayerDrawable) getBackground();
            background.setLayerInset(0, 0, (vOffset / 4), 0, vOffset);
        
        }
    
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return (((mDrawableHotspotTouch != null) && mDrawableHotspotTouch.onTouch(this, event))
            || super.onTouchEvent(event));
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
    
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        
        setBackgroundDrawable(drawable);
        
        getElevationAlphaFromColor(color);
        
        if (mCornerShadowPaint != null)
            mCornerShadowPaint.setAlpha(mElevationAlpha);
        
        if (mEdgeShadowPaint != null)
            mEdgeShadowPaint.setAlpha(mElevationAlpha);
    
    }
    
    @Override
    public void setBackground(Drawable drawable) {
        setBackgroundDrawable(drawable);
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public void setBackgroundDrawable(Drawable drawable) {
    
        mElevationAlpha = 0;
        
        if (mCornerShadowPaint != null)
            mCornerShadowPaint.setAlpha(mElevationAlpha);
        
        if (mEdgeShadowPaint != null)
            mEdgeShadowPaint.setAlpha(mElevationAlpha);
        
        if (Build.VERSION.SDK_INT < 21) {
        
            mBackground = drawable;
            
            int vOffset = (int) Math.ceil((float) (mRawShadowSize * SHADOW_MULTIPLIER + (1 - COS_45) * 0));
            
            LayerDrawable layer = new LayerDrawable(new Drawable[] { mBackground } );
            layer.setLayerInset(0, 0, (vOffset / 4), 0, vOffset);
            
            android.util.Log.i("MaterialTextView", "vOffset: " + vOffset);
            
            super.setBackgroundDrawable(layer);
        
        } else
            super.setBackgroundDrawable(drawable);
        
        if (drawable instanceof LollipopDrawable)
            mDrawableHotspotTouch = new DrawableHotspotTouch((LollipopDrawable) drawable);
        else
            mDrawableHotspotTouch = null;
    
    }
    
    @Override
    public void setBackgroundResource(int resid) {
    
        // Convert the background to a LollipopDrawable to use theme references and ripple in xml on pre-lollipop devices.
        Drawable drawable = LollipopDrawablesCompat.getDrawable(getResources(), resid, getContext().getTheme());
        setBackgroundDrawable(drawable);
        
        getElevationAlphaFromResource(resid);
        
        if (mCornerShadowPaint != null)
            mCornerShadowPaint.setAlpha(mElevationAlpha);
        
        if (mEdgeShadowPaint != null)
            mEdgeShadowPaint.setAlpha(mElevationAlpha);
    
    }
    
    public void setElevation(float elevation) {
    
        if (getElevation() != elevation) {
        
            if (Build.VERSION.SDK_INT >= 21) {
            
                super.setElevation(elevation);
                return;
            
            }
            
            mElevation = elevation;
            mRawShadowSize = toeven(elevation);
            mShadowSize = (int) (mRawShadowSize * SHADOW_MULTIPLIER + mInsetShadow + .5f);
            
            int vOffset = (int) Math.ceil((float) (mRawShadowSize * SHADOW_MULTIPLIER + (1 - COS_45) * 0));
            super.setPadding(getPaddingLeft(), (getPaddingTop() + (vOffset / 4)),
                getPaddingRight(), (getPaddingBottom() + vOffset));
            
            invalidate();
        
        }
    
    }
    
    public void setLayoutParams(ViewGroup.LayoutParams params) {
    
        if (mBackground != null) {
        
            int vOffset = (int) Math.ceil((float) (mRawShadowSize * SHADOW_MULTIPLIER + (1 - COS_45) * 0));
            
            if (params instanceof LinearLayout.LayoutParams) {
            
                LinearLayout.LayoutParams llparams = (LinearLayout.LayoutParams) params;
                llparams.bottomMargin -= vOffset;
                llparams.topMargin -= (vOffset / 4);
                
                params = llparams;
            
            } else if (params instanceof RelativeLayout.LayoutParams) {
            
                RelativeLayout.LayoutParams rlparams = (RelativeLayout.LayoutParams) params;
                rlparams.bottomMargin -= vOffset;
                rlparams.topMargin -= (vOffset / 4);
                
                params = rlparams;
            
            }
        
        }
        
        super.setLayoutParams(params);
    
    }
    
    @Override
    public void setPadding(int left, int top, int right, int bottom) {
    
        int vOffset = (int) Math.ceil((float) (mRawShadowSize * SHADOW_MULTIPLIER + (1 - COS_45) * 0));
        
        // to get the corrent padding dimensions we first need to check if the bottom
        // already covers the shadow offset and subtract the offset if present.
        if (bottom == (getPaddingBottom() + vOffset))
            bottom -= vOffset;
        
        if (top == (getPaddingTop() + (vOffset / 4)))
            top -= (vOffset / 4);
        
        // store the new padding dimensions
        if (mDefaultPaddingBottom != bottom)
            mDefaultPaddingBottom = bottom;
        
        if (mDefaultPaddingLeft != left)
            mDefaultPaddingLeft = left;
        
        if (mDefaultPaddingRight != right)
            mDefaultPaddingRight = right;
        
        if (mDefaultPaddingTop != top)
            mDefaultPaddingTop = top;
        
        // re-add the shadow offset to top and bottom
        if (mBackground != null) {
        
            bottom += vOffset;
            top += (vOffset / 4);
        
        }
        
        // set the padding of the view using the new dimensions
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
        
            Drawable drawable = LollipopDrawablesCompat.getDrawable(getResources(), resid, getContext().getTheme());
            
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
    
    /** Casts the value to an even integer. */
    private int toeven(float value) {
    
        int i = (int) (value + .5f);
        
        if ((i % 2) == 1)
            return (i - 1);
        
        return i;
    
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