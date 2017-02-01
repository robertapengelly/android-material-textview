# MaterialTextView

Backported material styled TextView for use on pre-lollipop devices. Supports Android 2.3 API 9 (GINGERBREAD) and up.

Preview

![materialtextviewscreenshot](https://cloud.githubusercontent.com/assets/5245027/22506974/1eecfcd8-e87b-11e6-9d71-36769470f504.png)

# Installation

    Step 1. Add the JitPack repository to your build file
    
    Add it in your root build.gradle at the end of repositories:
    
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
         }
    }
    
    Step 2. Add the dependency
    
    dependencies {
        compile 'com.github.robertapengelly:android-material-textview:1.0.4'
    }

# Usage

    Styling
    
    The following style attributes are lollipop defaults for the theme. Change the values to better suit your app.
    
    Pre-Honycomb devices (values/styles.xml)
    
        <style name="AppTheme" parent="@android:style/Theme.NoTitleBar">
            <!-- Text colors -->
            <item name="android:textColorPrimary">@color/primary_text_material_dark</item>
            <item name="android:textColorPrimaryInverse">@color/primary_text_material_light</item>
            <item name="android:textColorPrimaryDisableOnly">@color/primary_text_disable_only_material_dark</item>
            <item name="android:textColorSecondary">@color/secondary_text_material_dark</item>
            <item name="android:textColorSecondaryInverse">@color/secondary_text_material_light</item>
            <item name="android:textColorTertiary">@color/secondary_text_material_dark</item>
            <item name="android:textColorTertiaryInverse">@color/secondary_text_material_light</item>
            <item name="android:textColorHint">@color/hint_foreground_material_dark</item>
            <item name="android:textColorHintInverse">@color/hint_foreground_material_light</item>
            <item name="android:textColorHighlight">@color/highlighted_text_material_dark</item>
            <item name="android:textColorLink">@color/link_text_material_dark</item>
            
            <!-- Text styles -->
            <item name="android:textAppearance">@style/TextAppearance.Material</item>
            <item name="android:textAppearanceInverse">@style/TextAppearance.Material.Inverse</item>
            <item name="android:textAppearanceLarge">@style/TextAppearance.Material.Large</item>
            <item name="android:textAppearanceLargeInverse">@style/TextAppearance.Material.Large.Inverse</item>
            <item name="android:textAppearanceMedium">@style/TextAppearance.Material.Medium</item>
            <item name="android:textAppearanceMediumInverse">@style/TextAppearance.Material.Medium.Inverse</item>
            <item name="android:textAppearanceSmall">@style/TextAppearance.Material.Small</item>
            <item name="android:textAppearanceSmallInverse">@style/TextAppearance.Material.Small.Inverse</item>
            
            <item name="android:textViewStyle">@style/Widget.Material.TextView</item>
            
            <!-- Color palette -->
            <item name="colorAccent">@color/accent_material_dark</item>
        </style>
    
    Honycomb and newer (values-v11/styles.xml)
    
        <style name="AppTheme" parent="@android:style/Theme.Holo.NoActionBar">
            <!-- Text colors -->
            <item name="android:textColorPrimary">@color/primary_text_material_dark</item>
            <item name="android:textColorPrimaryInverse">@color/primary_text_material_light</item>
            <item name="android:textColorPrimaryDisableOnly">@color/primary_text_disable_only_material_dark</item>
            <item name="android:textColorSecondary">@color/secondary_text_material_dark</item>
            <item name="android:textColorSecondaryInverse">@color/secondary_text_material_light</item>
            <item name="android:textColorTertiary">@color/secondary_text_material_dark</item>
            <item name="android:textColorTertiaryInverse">@color/secondary_text_material_light</item>
            <item name="android:textColorHint">@color/hint_foreground_material_dark</item>
            <item name="android:textColorHintInverse">@color/hint_foreground_material_light</item>
            <item name="android:textColorHighlight">@color/highlighted_text_material_dark</item>
            <item name="android:textColorLink">@color/link_text_material_dark</item>
            
            <!-- Text styles -->
            <item name="android:textAppearance">@style/TextAppearance.Material</item>
            <item name="android:textAppearanceInverse">@style/TextAppearance.Material.Inverse</item>
            <item name="android:textAppearanceLarge">@style/TextAppearance.Material.Large</item>
            <item name="android:textAppearanceLargeInverse">@style/TextAppearance.Material.Large.Inverse</item>
            <item name="android:textAppearanceMedium">@style/TextAppearance.Material.Medium</item>
            <item name="android:textAppearanceMediumInverse">@style/TextAppearance.Material.Medium.Inverse</item>
            <item name="android:textAppearanceSmall">@style/TextAppearance.Material.Small</item>
            <item name="android:textAppearanceSmallInverse">@style/TextAppearance.Material.Small.Inverse</item>
            
            <item name="android:textViewStyle">@style/Widget.Material.TextView</item>
            
            <!-- Color palette -->
            <item name="colorAccent">@color/accent_material_dark</item>
        </style>
    
    Lollipop and newer (values-v21/styles.xml)
    
        <style name="AppTheme" parent="@android:style/Theme.Material.NoActionBar">
            <!-- Text colors -->
            <item name="android:textColorPrimary">@color/primary_text_material_dark</item>
            <item name="android:textColorPrimaryInverse">@color/primary_text_material_light</item>
            <item name="android:textColorPrimaryDisableOnly">@color/primary_text_disable_only_material_dark</item>
            <item name="android:textColorSecondary">@color/secondary_text_material_dark</item>
            <item name="android:textColorSecondaryInverse">@color/secondary_text_material_light</item>
            <item name="android:textColorTertiary">@color/secondary_text_material_dark</item>
            <item name="android:textColorTertiaryInverse">@color/secondary_text_material_light</item>
            <item name="android:textColorHint">@color/hint_foreground_material_dark</item>
            <item name="android:textColorHintInverse">@color/hint_foreground_material_light</item>
            <item name="android:textColorHighlight">@color/highlighted_text_material_dark</item>
            <item name="android:textColorLink">@color/link_text_material_dark</item>
            
            <!-- Text styles -->
            <item name="android:textAppearance">@style/TextAppearance.Material</item>
            <item name="android:textAppearanceInverse">@style/TextAppearance.Material.Inverse</item>
            <item name="android:textAppearanceLarge">@style/TextAppearance.Material.Large</item>
            <item name="android:textAppearanceLargeInverse">@style/TextAppearance.Material.Large.Inverse</item>
            <item name="android:textAppearanceMedium">@style/TextAppearance.Material.Medium</item>
            <item name="android:textAppearanceMediumInverse">@style/TextAppearance.Material.Medium.Inverse</item>
            <item name="android:textAppearanceSmall">@style/TextAppearance.Material.Small</item>
            <item name="android:textAppearanceSmallInverse">@style/TextAppearance.Material.Small.Inverse</item>
            
            <item name="android:textViewStyle">@style/Widget.Material.TextView</item>
            
            <!-- Color palette -->
            <item name="android:colorAccent">@color/accent_material_dark</item>
        </style>
    
    Adding a MaterialTextView widget (layout/activity_main.xml)
    
    If you use android:elevation it will be replaced with app:elevation if specified.
    If you use android:textAllCaps it will be replaced with app:textAllCaps if specified.
    
    If you want to use a ripple background on all-devices add app:background="@drawable/your_drawable_name" and it will be inflated.
    When you use app:background it will only take effect if there's no background.
    
        <?xml version="1.0" encoding="utf-8" ?>
        <LinearLayout
            xmlns:android="http://schemas.android.com/apk/res/android"
            xmlns:app="http://schemas.android.com/apk/res-auto"
            android:layout_height="match_parent"
            android:layout_width="match_parent"
            android:orientation="vertical">
            
            <robertapengelly.support.widget.MaterialTextView
                android:layout_height="wrap_content"
                android:layout_width="match_parent"
                android:text="MaterialEditText"
                app:elevation="4dp"
                app:textAllCaps="true" />
        
        </LinearLayout>
