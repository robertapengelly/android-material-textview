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

import  robertapengelly.support.graphics.drawable.LollipopDrawable;
import  robertapengelly.support.graphics.drawable.LollipopDrawablesCompat;
import  robertapengelly.support.materialtextview.R;

public class MaterialTextView extends TextView {

    // used to calculate content padding
    final static double COS_45 = Math.cos(Math.toRadians(45));
    
    final static float SHADOW_MULTIPLIER = 1.5f;
    
    // extra shadow to avoid gaps between view and shadow
    private final int mInsetShadow;
    
    private final int mShadowEndColor;
    private final int mShadowStartColor;
    
    private final RectF mBounds;
    
    private boolean mBackgroundNeedsUpdate = false;
    private boolean mCanHaveElevation = false;
    
    private float mElevation = 0;
    
    // actual value set by developer
    private float mRawShadowSize;
    
    // multiplied value to account for shadow offset
    private float mShadowSize;
    
    private int mDefaultPaddingBottom;
    private int mDefaultPaddingLeft;
    private int mDefaultPaddingRight;
    private int mDefaultPaddingTop;
    
    private Object mEditor;
    
    private Paint mCornerShadowPaint;
    private Paint mEdgeShadowPaint;
    
    private Path mCornerShadowPath;
    
    public MaterialTextView(Context context) {
        this(context, null);
    }
    
    public MaterialTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
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
        
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MaterialTextView, defStyleAttr, 0);
        
        int ap = a.getResourceId(R.styleable.MaterialTextView_android_textAppearance, -1);
        
        if (ap != -1)
            setTextAppearanceInternal(ap);
        
        int count = a.getIndexCount();
        
        for (int i = 0; i < count; ++i) {
        
            int attr = a.getIndex(i);
            
            if (attr == R.styleable.MaterialTextView_android_background) {
            
                int resid = a.getResourceId(attr, 0);
                
                if (resid == 0) {
                
                    int color = a.getColor(attr, 0);
                    
                    if (color != 0) {
                    
                        int elevationAlpha = Integer.parseInt(String.format("#%06X", color).substring(1, 3), 16);
                        
                        if (elevationAlpha == 255)
                            mCanHaveElevation = true;
                    
                    }
                    
                    continue;
                
                }
                
                // Convert the background to a LollipopDrawable to use theme references in xml on pre-lollipop devices.
                Drawable drawable = LollipopDrawablesCompat.getDrawable(getResources(),
                    resid, context.getTheme());
                
                if (Build.VERSION.SDK_INT >= 16)
                    setBackground(drawable);
                else
                    //noinspection deprecation
                    setBackgroundDrawable(drawable);
                
                checkIfCanHaveElevation(resid);
            
            } else if (attr == R.styleable.MaterialTextView_android_textCursorDrawable) {
            
                // Setting the text cursor isn't available on pre-honeycomb devices so we can
                // just continue the loop.
                if (Build.VERSION.SDK_INT < 12)
                    continue;
                
                int resid = a.getResourceId(attr, 0);
                
                try {
                
                    Drawable drawable = LollipopDrawablesCompat.getDrawable(getResources(), resid, getContext().getTheme());
                    
                    if (resid == R.drawable.text_cursor_material)
                        tintDrawable((LollipopDrawable) drawable);
                    
                    Field field = mEditor.getClass().getDeclaredField("mCursorDrawable");
                    field.setAccessible(true);
                    field.set(mEditor, new Drawable[] { drawable, drawable });
                
                } catch (Exception ignored) {}
            
            } else if (attr == R.styleable.MaterialTextView_android_textSelectHandleLeft) {
            
                int resid = a.getResourceId(attr, 0);
                setTextSelectHandle("mSelectHandleLeft", resid, (resid == R.drawable.text_select_handle_left_material));
            
            } else if (attr == R.styleable.MaterialTextView_android_textSelectHandle) {
            
                int resid = a.getResourceId(attr, 0);
                setTextSelectHandle("mSelectHandleCenter", resid, (resid == R.drawable.text_select_handle_middle_material));
            
            } else if (attr == R.styleable.MaterialTextView_android_textSelectHandleRight) {
            
                int resid = a.getResourceId(attr, 0);
                setTextSelectHandle("mSelectHandleRight", resid, (resid == R.drawable.text_select_handle_right_material));
            
            } else if (attr == R.styleable.MaterialTextView_background) {
            
                // We only want to apply the compatibility background if android:background wasn't specified.
                // The app:background should only be used for <ripple> xml because on pre-lollipop devices
                // an invalid drawable tag ripple exception will be thrown if android:background is provided.
                if (getBackground() == null) {
                
                    int resid = a.getResourceId(attr, 0);
                    
                    if (resid == 0)
                        continue;
                    
                    Drawable drawable = LollipopDrawablesCompat.getDrawable(getResources(),
                        resid, context.getTheme());
                    
                    if (Build.VERSION.SDK_INT >= 16)
                        setBackground(drawable);
                    else
                        //noinspection deprecation
                        setBackgroundDrawable(drawable);
                    
                    checkIfCanHaveElevation(resid);
                
                }
            
            }
        
        }
        
        int defaultPadding = a.getDimensionPixelSize(R.styleable.MaterialTextView_android_padding, 0);
        
        mDefaultPaddingBottom = a.getDimensionPixelSize(R.styleable.MaterialTextView_android_paddingBottom, defaultPadding);
        mDefaultPaddingLeft = a.getDimensionPixelSize(R.styleable.MaterialTextView_android_paddingLeft, defaultPadding);
        mDefaultPaddingRight = a.getDimensionPixelSize(R.styleable.MaterialTextView_android_paddingRight, defaultPadding);
        mDefaultPaddingTop = a.getDimensionPixelSize(R.styleable.MaterialTextView_android_paddingTop, defaultPadding);
        
        setElevation(a.getDimensionPixelOffset(R.styleable.MaterialTextView_elevation, 0));
        
        if (a.hasValue(R.styleable.MaterialTextView_textAllCaps))
            setAllCaps(a.getBoolean(R.styleable.MaterialTextView_textAllCaps, false));
        
        a.recycle();
        
        mBounds = new RectF();
        
        mCornerShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mCornerShadowPaint.setStyle(Paint.Style.FILL);
        
        mEdgeShadowPaint = new Paint(mCornerShadowPaint);
        mEdgeShadowPaint.setAntiAlias(false);
        
        // shadow variables
        mInsetShadow = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        mShadowEndColor = Color.parseColor("#03000000");
        mShadowStartColor = Color.parseColor("#37000000");
        
        if (Build.VERSION.SDK_INT >= 21)
            setClipToOutline(true);
    
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
    
    private void checkIfCanHaveElevation(int resid) {
    
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
                checkIfCanHaveElevationFromXml(res, rp);
                
                rp.close();
            
            } catch (Exception ignored) {}
        
        }
    
    }
    
    private void checkIfCanHaveElevationFromXml(Resources r, XmlPullParser parser) throws XmlPullParserException, IOException {
    
        AttributeSet attrs = Xml.asAttributeSet(parser);
        
        int type;
        
        //noinspection StatementWithEmptyBody
        while (((type = parser.next()) != XmlPullParser.START_TAG) && (type != XmlPullParser.END_DOCUMENT));
        
        if (type != XmlPullParser.START_TAG)
            throw new XmlPullParserException("No start tag found");
        
        checkIfCanHaveElevationFromXmlInner(r, parser, attrs);
    
    }
    
    public void checkIfCanHaveElevationFromXmlInner(Resources r, XmlPullParser parser, AttributeSet attrs)
        throws XmlPullParserException, IOException {
        
        String name = parser.getName();
        
        try {
        
            if (name.equals("shape")) {
            
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
                        
                        final ColorStateList colorStateList =
                            a.getColorStateList(R.styleable.GradientDrawableSolid_android_color);
                        
                        a.recycle();
                        
                        if (colorStateList != null ) {
                        
                            int color = colorStateList.getDefaultColor();
                            
                            if (color != 0) {
                            
                                int elevationAlpha = Integer.parseInt(String.format("#%06X", color).substring(1, 3), 16);
                                
                                if (elevationAlpha == 255)
                                    mCanHaveElevation = true;
                            
                            }
                        
                        }
                    
                    }
                
                }
            
            } else if (name.equals("layer-list") || name.equals("ripple")) {
            
                final int innerDepth = (parser.getDepth() + 1);
                
                int depth, type;
                
                while (((type = parser.next()) != XmlPullParser.END_DOCUMENT) && (((depth = parser.getDepth()) >= innerDepth)
                    || (type != XmlPullParser.END_TAG))) {
                    
                    if (type != XmlPullParser.START_TAG)
                        continue;
                    
                    if ((depth > innerDepth) || !parser.getName().equals("item"))
                        continue;
                    
                    final TypedArray a = obtainAttributes(r, getContext().getTheme(), attrs, R.styleable.LayerDrawableItem);
                    
                    int id = getResourceId(getContext().getTheme(), a, null,
                        R.styleable.LayerDrawableItem_android_id, View.NO_ID);
                    
                    if (id != android.R.id.mask) {
                    
                        Drawable dr = getDrawable(getContext().getTheme(), a, null,
                            R.styleable.LayerDrawableItem_android_drawable);
                        
                        if (dr == null) {
                        
                            //noinspection StatementWithEmptyBody
                            while ((type = parser.next()) == XmlPullParser.TEXT);
                            
                            if (type != XmlPullParser.START_TAG)
                                throw new XmlPullParserException(parser.getPositionDescription()
                                    + ": <item> tag requires a 'drawable' attribute or child tag defining a drawable");
                            
                            checkIfCanHaveElevationFromXmlInner(r, parser, attrs);
                        
                        }
                    
                    }
                    
                    a.recycle();
                
                }
            
            }
        
        } catch (Exception ignored) {}
    
    }
    
    @Override
    public void draw(Canvas canvas) {
    
        if ((Build.VERSION.SDK_INT < 21) && mCanHaveElevation) {
        
            canvas.translate(0, mRawShadowSize / 2);
            drawShadow(canvas);
            canvas.translate(0, -mRawShadowSize / 2);
        
        }
        
        super.draw(canvas);
        
        if (mBackgroundNeedsUpdate) {
        
            mBackgroundNeedsUpdate = false;
            
            if (getBackground() != null) {
            
                int vOffset = (int) Math.ceil((float) (mRawShadowSize * SHADOW_MULTIPLIER + (1 - COS_45) * 0));
                
                Drawable background = getBackground();
                background.setBounds(0, 0, getMeasuredWidth (), (getMeasuredHeight () - vOffset));
                
                invalidate();
            
            }
        
        }
    
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
    
    private static int getColorFromAttrRes(Context context, int attr) {
    
        TypedArray a = context.obtainStyledAttributes(new int[] { attr });
        
        try {
            return a.getColor(0, 0);
        } finally {
            a.recycle();
        }
    
    }
    
    /**
     * Retrieve the Drawable for the attribute at <var>index</var>.
     *
     * @param index Index of attribute to retrieve.
     * @return Drawable for the attribute, or null if not defined.
     */
    private static Drawable getDrawable(Resources.Theme theme, TypedArray a, TypedValue[] values, int index) {
    
        final int[] TEMP_ARRAY = new int[1];
        
        if ((values != null) && (theme != null)) {
        
            TypedValue v = values[index];
            
            if (v.type == TypedValue.TYPE_ATTRIBUTE) {
            
                TEMP_ARRAY[0] = v.data;
                TypedArray tmp = theme.obtainStyledAttributes(null, TEMP_ARRAY, 0, 0);
                
                try {
                    return tmp.getDrawable(0);
                } finally {
                    tmp.recycle();
                }
            
            }
        
        }
        
        if (a != null)
            return LollipopDrawablesCompat.getDrawable(a, index, theme);
        
        return null;
    
    }
    
    /**
     * The base elevation of this view relative to its parent, in pixels.
     *
     * @return The base depth position of the view, in pixels.
     */
    public float getElevation() {
    
        if (Build.VERSION.SDK_INT >= 21)
            return super.getElevation();
        
        return mElevation;
    
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
     * Retrieve the resource identifier for the attribute at
     * <var>index</var>.  Note that attribute resource as resolved when
     * the overall {@link TypedArray} object is retrieved.  As a
     * result, this function will return the resource identifier of the
     * final resource value that was found, <em>not</em> necessarily the
     * original resource that was specified by the attribute.
     *
     * @param index Index of attribute to retrieve.
     * @param def   Value to return if the attribute is not defined or
     *              not a resource.
     * @return Attribute resource identifier, or defValue if not defined.
     */
    private static int getResourceId(Resources.Theme theme, TypedArray a, TypedValue[] values, int index, int def) {
    
        final int[] TEMP_ARRAY = new int[1];
        
        if ((values != null) && (theme != null)) {
        
            TypedValue v = values[index];
            
            if (v.type == TypedValue.TYPE_ATTRIBUTE) {
            
                TEMP_ARRAY[0] = v.data;
                TypedArray tmp = theme.obtainStyledAttributes(null, TEMP_ARRAY, 0, 0);
                
                try {
                    return tmp.getResourceId(0, def);
                } finally {
                    tmp.recycle();
                }
            
            }
        
        }
        
        if (a != null)
            return a.getResourceId(index, def);
        
        return def;
    
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
    
        // View is offset SHADOW_MULTIPLIER * maxShadowSize to account for the shadow shift.
        // We could have different top-bottom offsets to avoid extra gap above but in that case
        // center aligning Views inside the CardView would be problematic.
        final float verticalOffset = (mRawShadowSize * SHADOW_MULTIPLIER);
        mBounds.set(mRawShadowSize, verticalOffset, (w - mRawShadowSize), (h - verticalOffset));
        
        if (mCanHaveElevation) {
        
            if (buildShadowCorners()) {
            
                mBackgroundNeedsUpdate = true;
                
                ViewGroup.LayoutParams params = getLayoutParams();
                
                if (params instanceof LinearLayout.LayoutParams) {
                
                    int vOffset = (int) Math.ceil((float) (mRawShadowSize * SHADOW_MULTIPLIER + (1 - COS_45) * 0));
                    
                    LinearLayout.LayoutParams llparams = (LinearLayout.LayoutParams) params;
                    llparams.bottomMargin -= vOffset;
                    
                    setLayoutParams(llparams);
                
                }
                
                if (params instanceof RelativeLayout.LayoutParams) {
                
                    int vOffset = (int) Math.ceil((float) (mRawShadowSize * SHADOW_MULTIPLIER + (1 - COS_45) * 0));
                    
                    RelativeLayout.LayoutParams rlparams = (RelativeLayout.LayoutParams) params;
                    rlparams.bottomMargin -= vOffset;
                    
                    setLayoutParams(rlparams);
                
                }
            
            }
        
        }
        
        invalidate();
    
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
    public void setBackgroundResource(int resid) {
    
        // Convert the background to a LollipopDrawable to use theme references and ripple in xml on pre-lollipop devices.
        Drawable drawable = LollipopDrawablesCompat.getDrawable(getResources(),
            resid, getContext().getTheme());
        
        checkIfCanHaveElevation(resid);
        
        if (mCanHaveElevation) {
        
            mBackgroundNeedsUpdate = true;
            
            int vOffset = (int) Math.ceil((float) (mRawShadowSize * SHADOW_MULTIPLIER + (1 - COS_45) * 0));
            super.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), (getPaddingBottom() + vOffset));
        
        }
        
        if (Build.VERSION.SDK_INT >= 16)
            setBackground(drawable);
        else
            //noinspection deprecation
            setBackgroundDrawable(drawable);
    
    }
    
    /** Sets the base elevation of this view, in pixels. */
    public void setElevation(float elevation) {
    
        if (elevation != getElevation()) {
        
            if (Build.VERSION.SDK_INT >= 21)
                super.setElevation(elevation);
            
            mElevation = elevation;
            mRawShadowSize = toEven(elevation);
            mShadowSize = (int) (mRawShadowSize * SHADOW_MULTIPLIER + mInsetShadow + .5f);
            
            if (mCanHaveElevation) {
            
                int vOffset = (int) Math.ceil((float) (mRawShadowSize * SHADOW_MULTIPLIER + (1 - COS_45) * 0));
                super.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), (getPaddingBottom() + vOffset));
            
            }
            
            invalidate();
        
        }
    
    }
    
    @Override
    public void setPadding(int left, int top, int right, int bottom) {
    
        int vOffset = (int) Math.ceil((float) (mRawShadowSize * SHADOW_MULTIPLIER + (1 - COS_45) * 0));
        
        // to get the corrent padding dimensions we first need to check if the bottom
        // already covers the shadow offset and subtract the offset if present.
        if (bottom == getPaddingBottom() + vOffset)
            bottom -= vOffset;
        
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
        if (mCanHaveElevation)
            bottom += vOffset;
        
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
    
    private void setTextSelectHandle(String field_id, int resid, boolean needsTint) {
    
        try {
        
            Drawable drawable = LollipopDrawablesCompat.getDrawable(getResources(), resid, getContext().getTheme());
            
            if (needsTint)
                tintDrawable((LollipopDrawable) drawable);
            
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
    private int toEven(float value) {
    
        int i = (int) (value + .5f);
        
        if ((i % 2) == 1)
            return (i - 1);
        
        return i;
    
    }
    
    private void tintDrawable(LollipopDrawable drawable) {
    
        int colorAccent = getColorFromAttrRes(getContext(), R.attr.colorAccent);
        
        if (colorAccent == 0)
            if (Build.VERSION.SDK_INT >= 21)
                colorAccent = getColorFromAttrRes(getContext(), android.R.attr.colorAccent);
        
        if (colorAccent == 0) {
        
            final TypedArray aa = getContext().obtainStyledAttributes(new int[] { android.R.attr.windowBackground });
            final int themeColorBackground = aa.getColor(0, 0);
            aa.recycle();
            
            final float[] hsv = new float[3];
            Color.colorToHSV(themeColorBackground, hsv);
            
            if (hsv[2] > 0.5f) {
            
                if (Build.VERSION.SDK_INT >= 23)
                    colorAccent = getResources().getColor(R.color.accent_material_light, getContext().getTheme());
                else
                    //noinspection deprecation
                    colorAccent = getResources().getColor(R.color.accent_material_light);
            
            } else {
            
                if (Build.VERSION.SDK_INT >= 23)
                    colorAccent = getResources().getColor(R.color.accent_material_dark, getContext().getTheme());
                else
                    //noinspection deprecation
                    colorAccent = getResources().getColor(R.color.accent_material_dark);
            
            }
        
        }
        
        drawable.setTint(colorAccent);
    
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