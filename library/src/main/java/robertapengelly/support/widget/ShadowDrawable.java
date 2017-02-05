package robertapengelly.support.widget;

import  android.content.res.Resources;
import  android.graphics.Canvas;
import  android.graphics.Color;
import  android.graphics.ColorFilter;
import  android.graphics.LinearGradient;
import  android.graphics.Paint;
import  android.graphics.Path;
import  android.graphics.PixelFormat;
import  android.graphics.RadialGradient;
import  android.graphics.Rect;
import  android.graphics.RectF;
import  android.graphics.Shader;
import  android.graphics.drawable.Drawable;
import  android.util.TypedValue;

class ShadowDrawable extends Drawable {

    private final static float SHADOW_MULTIPLIER = 1.5f;
    
    private int mInsetBottom = 0;
    private int mInsetLeft = 0;
    private int mInsetRight = 0;
    private int mInsetTop = 0;
    
    private final int mInsetShadow; // extra shadow to avoid gaps between card and shadow
    private final int mShadowEndColor;
    private final int mShadowStartColor;
    
    private final RectF mViewBounds;
    
    private boolean mDirty = true;
    
    /** If shadow size is set to a value above max shadow, we print a warning. */
    private boolean mPrintedShadowClipWarning = false;
    
    private float mCornerRadius;
    
    // actual value set by developer
    private float mRawMaxShadowSize;
    
    // actual value set by developer
    private float mRawShadowSize;
    
    // multiplied value to account for shadow offset
    private float mShadowSize;
    
    private Paint mCornerShadowPaint;
    
    private Paint mEdgeShadowPaint;
    
    private Path mCornerShadowPath;
    
    ShadowDrawable(Resources resources, float radius, float shadowSize) {
    
        mInsetShadow = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, resources.getDisplayMetrics());
        mShadowStartColor = Color.parseColor("#37000000");
        mShadowEndColor = Color.parseColor("#03000000");
        
        mCornerShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mCornerShadowPaint.setStyle(Paint.Style.FILL);
        mCornerRadius = (int) (radius + .5f);
        
        mEdgeShadowPaint = new Paint(mCornerShadowPaint);
        mEdgeShadowPaint.setAntiAlias(false);
        
        mViewBounds = new RectF();
        
        setShadowSize(shadowSize, shadowSize);
    
    }
    
    private void buildComponents(Rect bounds) {
    
        final float verticalOffset = (mRawMaxShadowSize * SHADOW_MULTIPLIER);
        
        mViewBounds.set((bounds.left + mRawMaxShadowSize), (bounds.top + verticalOffset),
            (bounds.right - mRawMaxShadowSize), (bounds.bottom - verticalOffset));
        
        buildShadowCorners();
    
    }
    
    private void buildShadowCorners() {
    
        RectF innerBounds = new RectF(-mCornerRadius, -mCornerRadius, mCornerRadius, mCornerRadius);
        
        RectF outerBounds = new RectF(innerBounds);
        outerBounds.inset(-mShadowSize, -mShadowSize);
        
        if (mCornerShadowPath == null)
            mCornerShadowPath = new Path();
        else
            mCornerShadowPath.reset();
        
        mCornerShadowPath.setFillType(Path.FillType.EVEN_ODD);
        mCornerShadowPath.moveTo(-mCornerRadius, 0);
        mCornerShadowPath.rLineTo(-mShadowSize, 0);
        
        // outer arc
        mCornerShadowPath.arcTo(outerBounds, 180f, 90f, false);
        
        // inner arc
        mCornerShadowPath.arcTo(innerBounds, 270f, -90f, false);
        
        mCornerShadowPath.close();
        
        float startRatio = (mCornerRadius / (mCornerRadius + mShadowSize));
        
        mCornerShadowPaint.setShader(new RadialGradient(0, 0, (mCornerRadius + mShadowSize),
            new int[] { mShadowStartColor, mShadowStartColor, mShadowEndColor },
                new float[] { 0f, startRatio, 1f }, Shader.TileMode.CLAMP));
        
        // we offset the content shadowSize/2 pixels up to make it more realistic.
        // this is why edge shadow shader has some extra space
        // When drawing bottom edge shadow, we use that extra space.
        mEdgeShadowPaint.setShader(new LinearGradient(0, (-mCornerRadius + mShadowSize), 0,
            (-mCornerRadius - mShadowSize), new int[] { mShadowStartColor, mShadowStartColor, mShadowEndColor },
                new float[] { 0f, .5f, 1f }, Shader.TileMode.CLAMP));
        
        mEdgeShadowPaint.setAntiAlias(false);
    
    }
    
    @Override
    public void draw(Canvas canvas) {
    
        if (mDirty) {
        
            Rect bounds = getBounds();
            
            bounds.bottom -= mInsetBottom;
            bounds.left += mInsetLeft;
            bounds.right -= mInsetRight;
            bounds.top += mInsetTop;
            
            buildComponents(bounds);
            mDirty = false;
        
        }
        
        canvas.translate(0, (mRawShadowSize / 2));
        drawShadow(canvas);
        
        canvas.translate(0, (-mRawShadowSize / 2));
    
    }
    
    private void drawShadow(Canvas canvas) {
    
        final float edgeShadowTop = (-mCornerRadius - mShadowSize);
        final float inset = (mCornerRadius + mInsetShadow + mRawShadowSize / 2);
        
        final boolean drawHorizontalEdges = ((mViewBounds.width() - 2 * inset) > 0);
        final boolean drawVerticalEdges = ((mViewBounds.height() - 2 * inset) > 0);
        
        // LT
        int saved = canvas.save();
        
        canvas.translate((mViewBounds.left + inset), (mViewBounds.top + inset));
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        
        if (drawHorizontalEdges)
            canvas.drawRect(0, edgeShadowTop, (mViewBounds.width() - 2 * inset), (mViewBounds.height() / 2 - inset),
                mEdgeShadowPaint);
        
        canvas.restoreToCount(saved);
        
        // LB
        saved = canvas.save();
        
        canvas.translate((mViewBounds.left + inset), (mViewBounds.bottom - inset));
        canvas.rotate(270f);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        
        if (drawVerticalEdges)
            canvas.drawRect(0, edgeShadowTop, (mViewBounds.height() - 2 * inset), 0, mEdgeShadowPaint);
        
        canvas.restoreToCount(saved);
        
        // RB
        saved = canvas.save();
        
        canvas.translate((mViewBounds.right - inset), (mViewBounds.bottom - inset));
        canvas.rotate(180f);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        
        if (drawHorizontalEdges)
            canvas.drawRect(0, edgeShadowTop, (mViewBounds.width() - 2 * inset), (mViewBounds.height() / 2 - inset),
                mEdgeShadowPaint);
        
        canvas.restoreToCount(saved);
        
        // RT
        saved = canvas.save();
        
        canvas.translate((mViewBounds.right - inset), (mViewBounds.top + inset));
        canvas.rotate(90f);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        
        if (drawVerticalEdges)
            canvas.drawRect(0, edgeShadowTop, (mViewBounds.height() - 2 * inset), 0, mEdgeShadowPaint);
        
        canvas.restoreToCount(saved);
    
    }
    
    @Override
    public int getAlpha() {
        return mEdgeShadowPaint.getAlpha();
    }
    
    float getMinHeight() {
    
        final float content = (2 * Math.max(mRawMaxShadowSize, (mCornerRadius + mInsetShadow
            + mRawMaxShadowSize * SHADOW_MULTIPLIER / 2)));
                        
        return (content + (mRawMaxShadowSize * SHADOW_MULTIPLIER + mInsetShadow) * 2);
    
    }
    
    float getMinWidth() {
    
        final float content = (2 * Math.max(mRawMaxShadowSize, (mCornerRadius + mInsetShadow + mRawMaxShadowSize / 2)));
        
        return (content + (mRawMaxShadowSize + mInsetShadow) * 2);
    
    }
    
    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
    
    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        
        mDirty = true;
    
    }
    
    @Override
    public void setAlpha(int alpha) {
    
        mCornerShadowPaint.setAlpha(alpha);
        mEdgeShadowPaint.setAlpha(alpha);
    
    }
    
    @Override
    public void setColorFilter(ColorFilter cf) {}
    
    void setInsets(int insetLeft, int insetTop, int insetRight, int insetBottom) {
    
        mDirty = true;
        
        mInsetBottom = insetBottom;
        mInsetLeft = insetLeft;
        mInsetRight = insetRight;
        mInsetTop = insetTop;
        
        invalidateSelf();
    
    }
    
    void setShadowSize(float shadowSize, float maxShadowSize) {
    
        if (shadowSize < 0f)
            throw new IllegalArgumentException("Invalid shadow size " + shadowSize + ". Must be >= 0");
        
        if (maxShadowSize < 0f)
            throw new IllegalArgumentException("Invalid max shadow size " + maxShadowSize + ". Must be >= 0");
        
        maxShadowSize = toEven(maxShadowSize);
        shadowSize = toEven(shadowSize);
        
        if (shadowSize > maxShadowSize) {
        
            shadowSize = maxShadowSize;
            
            if (!mPrintedShadowClipWarning)
                mPrintedShadowClipWarning = true;
        
        }
        
        if ((mRawShadowSize == shadowSize) && (mRawMaxShadowSize == maxShadowSize))
            return;
        
        mRawMaxShadowSize = maxShadowSize;
        mRawShadowSize = shadowSize;
        
        mDirty = true;
        mShadowSize = (int) (shadowSize * SHADOW_MULTIPLIER + mInsetShadow + .5f);
        
        invalidateSelf();
    
    }
    
    /** Casts the value to an even integer. */
    private int toEven(float value) {
    
        int i = (int) (value + .5f);
        
        if ((i % 2) == 1)
            return i - 1;
        
        return i;
    
    }

}