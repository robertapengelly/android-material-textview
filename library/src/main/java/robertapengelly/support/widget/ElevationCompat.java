package robertapengelly.support.widget;

import  android.content.res.ColorStateList;
import  android.content.res.Resources;
import  android.content.res.Resources.Theme;
import  android.content.res.TypedArray;
import  android.content.res.XmlResourceParser;
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
    
    static int getShadowAlphaFromDrawable(Resources res, int resid, Resources.Theme theme) {
    
        TypedValue value = new TypedValue();
        res.getValue(resid, value, true);
        
        if (value.resourceId == 0)
            return 0;
        
        if (value.string == null)
            throw new Resources.NotFoundException("Resource \"" + res.getResourceName(value.resourceId) + "\" (" +
                Integer.toHexString(value.resourceId) + ")  is not a Drawable (color or path): " + value);
        
        String file = value.string.toString();
        
        if (!file.endsWith(".xml"))
            return 0;
        
        int alpha = 0;
        
        try {
        
            XmlResourceParser rp = res.getAssets().openXmlResourceParser(value.assetCookie, file);
            
            alpha = getShadowAlphaFromXml(res, rp, theme);
            
            rp.close();
        
        } catch (Exception ignored) {}
        
        return alpha;
    
    }
    
    private static int getShadowAlphaFromLayerList(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme)
        throws XmlPullParserException, IOException {
        
        final int innerDepth = (parser.getDepth() + 1);
        
        int alpha = 0;
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
                return getShadowAlphaFromDrawable(r, resid, theme);
            else {
            
                //noinspection StatementWithEmptyBody
                while ((type = parser.next()) == XmlPullParser.TEXT);
                
                if (type != XmlPullParser.START_TAG)
                    throw new XmlPullParserException(parser.getPositionDescription()
                        + ": <item> tag requires a 'drawable' attribute or child tag defining a drawable");
                
                return getShadowAlphaFromXmlInner(r, parser, attrs, theme);
            
            }
        
        }
        
        return alpha;
    
    }
    
    private static int getShadowAlphaFromRipple(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme)
        throws XmlPullParserException, IOException {
        
        final int innerDepth = (parser.getDepth() + 1);
        
        int alpha = 0;
        int depth, type;
        
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
                    alpha = getShadowAlphaFromDrawable(r, resid, theme);
                else {
                
                    //noinspection StatementWithEmptyBody
                    while ((type = parser.next()) == XmlPullParser.TEXT);
                    
                    if (type != XmlPullParser.START_TAG)
                        throw new XmlPullParserException(parser.getPositionDescription()
                            + ": <item> tag requires a 'drawable' attribute or child tag defining a drawable");
                    
                    alpha = getShadowAlphaFromXmlInner(r, parser, attrs, theme);
                
                }
            
            }
        
        }
        
        return alpha;
    
    }
    
    static void getShadowStatesFromResource(Resources res, int resid, Resources.Theme theme, StateListDrawable shadows) {
    
        TypedValue value = new TypedValue();
        res.getValue(resid, value, true);
        
        if (value.resourceId == 0)
            return;
        
        if (value.string == null)
            throw new Resources.NotFoundException("Resource \"" + res.getResourceName(value.resourceId) + "\" (" +
                Integer.toHexString(value.resourceId) + ")  is not a Drawable (color or path): " + value);
        
        String file = value.string.toString();
        
        if (!file.endsWith(".xml"))
            return;
        
        try {
        
            XmlResourceParser rp = res.getAssets().openXmlResourceParser(value.assetCookie, file);
            
            AttributeSet attrs = Xml.asAttributeSet(rp);
            
            int type;
            
            //noinspection StatementWithEmptyBody
            while (((type = rp.next()) != XmlPullParser.START_TAG) && (type != XmlPullParser.END_DOCUMENT));
            
            if (type != XmlPullParser.START_TAG)
                throw new XmlPullParserException("No start tag found");
            
            final int innerDepth = (rp.getDepth() + 1);
            
            int alpha, depth, shadow = 0;
            
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
                    alpha = getShadowAlphaFromDrawable(res, drawableRes, theme);
                else {
                
                    //noinspection StatementWithEmptyBody
                    while ((type = rp.next()) == XmlPullParser.TEXT);
                    
                    if (type != XmlPullParser.START_TAG)
                        throw new XmlPullParserException(rp.getPositionDescription()
                            + ": <item> tag requires a 'drawable' attribute or child tag defining a drawable");
                    
                    alpha = getShadowAlphaFromXmlInner(res, rp, attrs, theme);
                
                }
                
                shadows.getStateDrawable(shadow).setAlpha(alpha);
                shadow++;
            
            }
            
            rp.close();
        
        } catch (Exception ignored) {}
    
    }
    
    private static int getShadowAlphaFromShape(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme)
        throws XmlPullParserException, IOException {
        
        TypedArray a;
        
        final int innerDepth = (parser.getDepth() + 1);
        int alpha = 0, depth, type;
        
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
                
                if (colorStateList != null)
                    alpha = getShadowAlphaFromColor(colorStateList.getDefaultColor());
                
                a.recycle();
            
            }
        
        }
        
        return alpha;
    
    }
    
    private static int getShadowAlphaFromXml(Resources r, XmlPullParser parser, Resources.Theme theme)
        throws XmlPullParserException, IOException {
        
        AttributeSet attrs = Xml.asAttributeSet(parser);
        
        int type;
        
        //noinspection StatementWithEmptyBody
        while (((type = parser.next()) != XmlPullParser.START_TAG) && (type != XmlPullParser.END_DOCUMENT));
        
        if (type != XmlPullParser.START_TAG)
            throw new XmlPullParserException("No start tag found");
        
        return getShadowAlphaFromXmlInner(r, parser, attrs, theme);
    
    }
    
    private static int getShadowAlphaFromXmlInner(Resources r, XmlPullParser parser, AttributeSet attrs,
        Resources.Theme theme) throws XmlPullParserException, IOException {
        
        final String name = parser.getName();
        
        int alpha = 0;
        
        try {
        
            switch (name) {
            
                case "layer-list":
                    alpha = getShadowAlphaFromLayerList(r, parser, attrs, theme);
                    break;
                case "ripple":
                    alpha = getShadowAlphaFromRipple(r, parser, attrs, theme);
                    break;
                case "shape":
                    alpha = getShadowAlphaFromShape(r, parser, attrs, theme);
                    break;
            
            }
        
        } catch (Exception ignore) {}
        
        return alpha;
    
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