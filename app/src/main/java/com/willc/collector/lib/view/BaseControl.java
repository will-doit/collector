package com.willc.collector.lib.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.view.View;

import com.willc.collector.lib.map.IActiveView;
import com.willc.collector.lib.tools.ITool;

import srs.Geometry.IPoint;
import srs.Layer.IElementContainer;
import srs.Layer.IGPSContainer;
import srs.tools.Event.DrawToolEnableManager;

/**
 * Created by stg on 17/10/15.
 */
public class BaseControl extends RelativeLayout implements View.OnClickListener,View.OnTouchListener {
    private ITool mTool;
    private DrawToolEnableManager mDrawToolEnableChanger = new DrawToolEnableManager();
    public int MODE = 0;

    public BaseControl(Context context) {
        super(context);
        this.setOnTouchListener(this);
    }

    public DrawToolEnableManager getDrawToolEnableChanger() {
        return this.mDrawToolEnableChanger != null?this.mDrawToolEnableChanger:null;
    }

    public BaseControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnTouchListener(this);
    }

    public void StartEdit() {
    }

    public void StopEdit() {
    }

    public void StopDraw() {

    }

    public ITool getDrawTool() {
        return this.mTool;
    }

    public void setDrawTool(ITool value) {
        if(value != this.mTool) {
            if(this.mTool != null) {
                this.mTool = null;
            }
            this.mTool = value;
        }
    }

    public void setGPSTool(ITool value) {
    }

    public boolean onTouch(View v, MotionEvent event) {
        return this.mTool != null?this.mTool.onTouch(v, event):true;
    }

    public void onClick(View v) {
        if(this.mTool != null) {
            this.mTool.onClick(v);
        }
    }

    public Bitmap getBitmap() {
        return null;
    }

    public IElementContainer getElementContainer() {
        return null;
    }

    public IGPSContainer getGPSContainer() {
        return null;
    }

    public IPoint ToWorldPoint(PointF point) {
        return null;
    }

    public PointF FromWorldPoint(IPoint point) {
        return null;
    }

    public double FromWorldDistance(double worldDistance) {
        return 0.0D;
    }

    public double ToWorldDistance(double deviceDistance) {
        return 0.0D;
    }

    public void Copy(BaseControl targetControl) {
    }

    public void setActiveView(IActiveView value) {
    }

    public IActiveView getActiveView() {
        return null;
    }

    public void refresh() {
    }

    public void partialRefresh() throws Exception {
    }

    public void drawTrack() {
    }

    public void drawTrack(Bitmap bit) {
    }
}