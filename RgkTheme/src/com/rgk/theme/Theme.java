package com.rgk.theme;

import android.graphics.drawable.Drawable;

class Theme{
    private String  mName;
    private Drawable mPreview;
    private Drawable mLauncher;
    private Drawable mKeyguard;

    public Theme(String name){
        this.mName=name;
    }

    public String getName() {
        return mName;
    }
    public void setName(String name) {
        this.mName = name;
    }
    public Drawable getPreview() {
        return mPreview;
    }
    public void setPreview(Drawable preview) {
        this.mPreview = preview;
    }
    public Drawable getLauncher() {
        return mLauncher;
    }
    public void setLauncher(Drawable launcher) {
        this.mLauncher = launcher;
    }
    public Drawable getKeyguard() {
        return mKeyguard;
    }
    public void setKeyguard(Drawable keyguard) {
        this.mKeyguard = keyguard;
    }
}