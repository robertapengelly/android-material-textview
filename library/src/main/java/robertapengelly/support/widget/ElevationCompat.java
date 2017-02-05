package robertapengelly.support.widget;

import  android.content.res.ColorStateList;
import  android.content.res.Resources;
import  android.content.res.Resources.Theme;
import  android.content.res.TypedArray;
import  android.content.res.XmlResourceParser;
import  android.graphics.drawable.Drawable;
import  android.util.AttributeSet;
import  android.util.TypedValue;
import  android.util.Xml;
import  android.view.View;

import  java.io.IOException;

import  org.xmlpull.v1.XmlPullParser;
import  org.xmlpull.v1.XmlPullParserException;

import  robertapengelly.support.graphics.drawable.StateListDrawable;
import  robertapengelly.support.materialtextview.R;

class ElevationCompat {

    static String getResourceName(Resources res, int resid) {
    
        TypedValue value = new TypedValue();
        res.getValue(resid, value, true);
        
        if (value.resourceId == 0)
            return null;
        
        if (value.string == null)
            throw new Resources.NotFoundException("Resource \"" + res.getResourceName(value.resourceId) + "\" (" +
                Integer.toHexString(value.resourceId) + ")  is not a Drawable (color or path): " + value);
        
        String file = value.string.toString();
        
        if (!file.endsWith(".xml"))
            return null;
        
        String name = null;
        
        try {
        
            XmlResourceParser rp = res.getAssets().openXmlResourceParser(value.assetCookie, file);
            
            int type;
            
            //noinspection StatementWithEmptyBody
            while (((type = rp.next()) != XmlPullParser.START_TAG) && (type != XmlPullParser.END_DOCUMENT));
            
            if (type != XmlPullParser.START_TAG)
                throw new XmlPullParserException("No start tag found");
            
            name = rp.getName();
            rp.close();
        
        } catch (Exception ignored) {}
        
        return name;
    
    }
    
    static int getShadowAlphaFromColor(int color) {
    
        String hexColor = String.format("#%08X", color);
        return Integer.parseInt(hexColor.substring(1, 3), 16);
    
    }
    
    private static Drawable getShadowFromInset(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme,
        MaterialTextView materialTextView) throws XmlPullParserException, IOException {
        
        Drawable shadow;
        
        final TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.InsetDrawable);
        final int resid = a.getResourceId(R.styleable.InsetDrawable_android_drawable, 0);
        
        if (resid != 0)
            shadow = getShadowFromResource(r, resid, theme, materialTextView);
        else {
        
            int type;
            
            //noinspection StatementWithEmptyBody
            while ((type=parser.next()) == XmlPullParser.TEXT);
            
            if (type != XmlPullParser.START_TAG)
                throw new XmlPullParserException(parser.getPositionDescription()
                    + ": <inset> tag requires a 'drawable' attribute or child tag defining a drawable");
            
            shadow = getShadowFromXmlInner(r, parser, attrs, theme, materialTextView);
        
        }
        
        if (shadow != null) {
        
            int insetBottom = 0, insetLeft = 0, insetRight = 0, insetTop = 0;
            
            final int N = a.getIndexCount();
            
            for (int i = 0; i < N; ++i) {
            
                final int attr = a.getIndex(i);
                
                if (attr == R.styleable.InsetDrawable_android_inset) {
                
                    final int inset = a.getDimensionPixelOffset(attr, Integer.MIN_VALUE);
                    
                    if (inset != Integer.MIN_VALUE) {
                    
                        insetBottom = inset;
                        insetLeft = inset;
                        insetRight = inset;
                        insetTop = inset;
                    
                    }
                
                } else if (attr == R.styleable.InsetDrawable_android_insetBottom)
                    insetBottom = a.getDimensionPixelOffset(attr, insetBottom);
                else if (attr == R.styleable.InsetDrawable_android_insetLeft)
                    insetLeft = a.getDimensionPixelOffset(attr, insetLeft);
                else if (attr == R.styleable.InsetDrawable_android_insetRight)
                    insetRight = a.getDimensionPixelOffset(attr, insetRight);
                else if (attr == R.styleable.InsetDrawable_android_insetTop)
                    insetTop = a.getDimensionPixelOffset(attr, insetTop);
            
            }
            
            ((ShadowDrawable) shadow).setInsets(insetLeft, insetTop, insetRight, insetBottom);
        
        }
        
        a.recycle();
        
        return shadow;
    
    }
    
    private static Drawable getShadowFromLayerList(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme,
        MaterialTextView materialTextView) throws XmlPullParserException, IOException {
        
        final int innerDepth = (parser.getDepth() + 1);
        int depth, type;
        
        while (((type = parser.next()) != XmlPullParser.END_DOCUMENT) && (((depth = parser.getDepth()) >= innerDepth)
            || (type != XmlPullParser.END_TAG))) {
            
            if (type != XmlPullParser.START_TAG)
                continue;
            
            if ((depth > innerDepth) || !parser.getName().equals("item"))
                continue;
            
            final TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.LayerDrawableItem);
            final int resid = a.getResourceId(R.styleable.LayerDrawableItem_android_drawable, 0);
            
            a.recycle();
            
            if (resid != 0)
                return getShadowFromResource(r, resid, theme, materialTextView);
            else {
            
                //noinspection StatementWithEmptyBody
                while ((type = parser.next()) == XmlPullParser.TEXT);
                
                if (type != XmlPullParser.START_TAG)
                    throw new XmlPullParserException(parser.getPositionDescription()
                        + ": <item> tag requires a 'drawable' attribute or child tag defining a drawable");
                
                return getShadowFromXmlInner(r, parser, attrs, theme, materialTextView);
            
            }
        
        }
        
        return null;
    
    }
    
    static Drawable getShadowFromResource(Resources res, int resid, Resources.Theme theme,
        MaterialTextView materialTextView) {
        
        TypedValue value = new TypedValue();
        res.getValue(resid, value, true);
        
        if (value.resourceId == 0)
            return null;
        
        if (value.string == null)
            throw new Resources.NotFoundException("Resource \"" + res.getResourceName(value.resourceId) + "\" (" +
                Integer.toHexString(value.resourceId) + ")  is not a Drawable (color or path): " + value);
        
        String file = value.string.toString();
        
        if (!file.endsWith(".xml"))
            return null;
        
        Drawable shadow = null;
        
        try {
        
            XmlResourceParser rp = res.getAssets().openXmlResourceParser(value.assetCookie, file);
            
            shadow = getShadowFromXml(res, rp, theme, materialTextView);
            
            rp.close();
        
        } catch (Exception ignored) {}
        
        return shadow;
    
    }
    
    private static Drawable getShadowFromRipple(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme,
        MaterialTextView materialTextView) throws XmlPullParserException, IOException {
        
        final int innerDepth = (parser.getDepth() + 1);
        int depth, type;
        
        Drawable shadow = null;
        
        while (((type = parser.next()) != XmlPullParser.END_DOCUMENT) && (((depth = parser.getDepth()) >= innerDepth)
            || (type != XmlPullParser.END_TAG))) {
            
            if (type != XmlPullParser.START_TAG)
                continue;
            
            if ((depth > innerDepth) || !parser.getName().equals("item"))
                continue;
            
            final TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.LayerDrawableItem);
            
            final int id = a.getResourceId(R.styleable.LayerDrawableItem_android_id, View.NO_ID);
            final int resid = a.getResourceId(R.styleable.LayerDrawableItem_android_drawable, 0);
            
            a.recycle();
            
            if (id != android.R.id.mask) {
            
                if (resid != 0)
                    shadow = getShadowFromResource(r, resid, theme, materialTextView);
                else {
                
                    //noinspection StatementWithEmptyBody
                    while ((type = parser.next()) == XmlPullParser.TEXT);
                    
                    if (type != XmlPullParser.START_TAG)
                        throw new XmlPullParserException(parser.getPositionDescription()
                            + ": <item> tag requires a 'drawable' attribute or child tag defining a drawable");
                    
                    shadow = getShadowFromXmlInner(r, parser, attrs, theme, materialTextView);
                
                }
            
            }
        
        }
        
        return shadow;
    
    }
    
    private static Drawable getShadowFromShape(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme,
        MaterialTextView materialTextView) throws XmlPullParserException, IOException {
        
        TypedArray a;
        
        final int innerDepth = (parser.getDepth() + 1);
        int depth, type;
        
        Drawable shadow = null;
        
        while (((type = parser.next()) != XmlPullParser.END_DOCUMENT)
            && (((depth = parser.getDepth()) >= innerDepth) || (type != XmlPullParser.END_TAG))) {
            
            if (type != XmlPullParser.START_TAG)
                continue;
            
            if (depth > innerDepth)
                continue;
            
            String name = parser.getName();
            
            if (name.equals("solid")) {
            
                a = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawableSolid);
                
                final ColorStateList colorStateList = a.getColorStateList(R.styleable.GradientDrawableSolid_android_color);
                
                if (colorStateList != null) {
                
                    shadow = new ShadowDrawable(r, materialTextView.getRadius(), materialTextView.getElevation());
                    shadow.setAlpha(getShadowAlphaFromColor(colorStateList.getDefaultColor()));
                
                }
                
                a.recycle();
            
            }
        
        }
        
        return shadow;
    
    }
    
    private static Drawable getShadowFromXml(Resources r, XmlPullParser parser, Resources.Theme theme,
        MaterialTextView materialTextView) throws XmlPullParserException, IOException {
        
        AttributeSet attrs = Xml.asAttributeSet(parser);
        
        int type;
        
        //noinspection StatementWithEmptyBody
        while (((type = parser.next()) != XmlPullParser.START_TAG) && (type != XmlPullParser.END_DOCUMENT));
        
        if (type != XmlPullParser.START_TAG)
            throw new XmlPullParserException("No start tag found");
        
        return getShadowFromXmlInner(r, parser, attrs, theme, materialTextView);
    
    }
    
    private static Drawable getShadowFromXmlInner(Resources r, XmlPullParser parser, AttributeSet attrs,
        Resources.Theme theme, MaterialTextView materialTextView) throws XmlPullParserException, IOException {
        
        final String name = parser.getName();
        
        Drawable shadow = null;
        
        try {
        
            switch (name) {
            
                case "inset":
                    shadow = getShadowFromInset(r, parser, attrs, theme, materialTextView);
                    break;
                case "layer-list":
                    shadow = getShadowFromLayerList(r, parser, attrs, theme, materialTextView);
                    break;
                case "ripple":
                    shadow = getShadowFromRipple(r, parser, attrs, theme, materialTextView);
                    break;
                case "shape":
                    shadow = getShadowFromShape(r, parser, attrs, theme, materialTextView);
                    break;
            
            }
        
        } catch (Exception ignore) {}
        
        return shadow;
    
    }
    
    static Drawable getShadowStatesFromResource(Resources res, int resid, Resources.Theme theme,
        MaterialTextView materialTextView, StateListDrawable states) {
    
        TypedValue value = new TypedValue();
        res.getValue(resid, value, true);
        
        if (value.resourceId == 0)
            return null;
        
        if (value.string == null)
            throw new Resources.NotFoundException("Resource \"" + res.getResourceName(value.resourceId) + "\" (" +
                Integer.toHexString(value.resourceId) + ")  is not a Drawable (color or path): " + value);
        
        String file = value.string.toString();
        
        if (!file.endsWith(".xml"))
            return null;
        
        StateListDrawable shadows = new StateListDrawable();
        
        try {
        
            XmlResourceParser rp = res.getAssets().openXmlResourceParser(value.assetCookie, file);
            
            AttributeSet attrs = Xml.asAttributeSet(rp);
            
            int type;
            
            //noinspection StatementWithEmptyBody
            while (((type = rp.next()) != XmlPullParser.START_TAG) && (type != XmlPullParser.END_DOCUMENT));
            
            if (type != XmlPullParser.START_TAG)
                throw new XmlPullParserException("No start tag found");
            
            final int innerDepth = (rp.getDepth() + 1);
            
            int depth, j = 0;
            Drawable shadow;
            
            while (((type = rp.next()) != XmlPullParser.END_DOCUMENT) && (((depth = rp.getDepth()) >= innerDepth)
                || (type != XmlPullParser.END_TAG))) {
                
                if (type != XmlPullParser.START_TAG)
                    continue;
                
                if ((depth > innerDepth) || !rp.getName().equals("item"))
                    continue;
                
                int drawableRes = 0;
                int i;
                
                final int numAttrs = attrs.getAttributeCount();
                
                for (i = 0; i < numAttrs; ++i) {
                
                    final int stateResId = attrs.getAttributeNameResource(i);
                    
                    if (stateResId == 0)
                        break;
                    
                    if (stateResId == android.R.attr.drawable)
                        drawableRes = attrs.getAttributeResourceValue(i, 0);
                
                }
                
                if (drawableRes != 0)
                    shadow = getShadowFromResource(res, drawableRes, theme, materialTextView);
                else {
                
                    //noinspection StatementWithEmptyBody
                    while ((type = rp.next()) == XmlPullParser.TEXT);
                    
                    if (type != XmlPullParser.START_TAG)
                        throw new XmlPullParserException(rp.getPositionDescription()
                            + ": <item> tag requires a 'drawable' attribute or child tag defining a drawable");
                    
                    shadow = getShadowFromXmlInner(res, rp, attrs, theme, materialTextView);
                
                }
                
                shadows.addState(states.getStateSet(j), shadow);
                j++;
            
            }
            
            rp.close();
        
        } catch (Exception ignored) {}
        
        return shadows;
    
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

}