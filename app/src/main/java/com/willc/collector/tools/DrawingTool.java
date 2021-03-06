/**
 *
 */
package com.willc.collector.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.willc.collector.R;
import com.willc.collector.datamgr.GeoCollectManager;
import com.willc.collector.interoperation.CollectInteroperator;
import com.willc.collector.interoperation.acticity.CollectActivity;
import com.willc.collector.lib.map.IMap;
import com.willc.collector.lib.tools.BaseTool;
import com.willc.collector.lib.view.BaseControl;
import com.willc.collector.lib.view.MapView;

import java.io.IOException;
import java.math.BigDecimal;

import srs.GPS.GPSConvert;
import srs.Geometry.IPoint;

/**
 * @author keqian
 */
public class DrawingTool extends BaseTool {
    private static final String TAG = DrawingTool.class.getSimpleName();

    private static int MODE_COLLECT = 0;
    //private static int MODE_COLLECT = 0;

    Context mContext = null;
    IMap mMapCurrent = null;
    // MapControl杈撳嚭鐨勫簳鍥�
    Bitmap mBitExMap = null;
    // 绉诲姩鏃剁殑鐢诲竷
    Bitmap mBitmapCurrentBack = null;

    /**
     * onTouch()中, ACTION_DOWN时的点对象
     */
    PointF mDownPt = null;

    /**
     * 采集新建标识--还没有任何点
     */
    boolean mIsFirstLine = false;

    /**
     * 如果ACTION_DOWN的位置在最后点的附近[32px], 则mCanDrawLine=true.
     * 即可以画collecting line.
     */
    boolean mCanDrawLine = false;

    boolean mIsDraw = true;
    boolean mCanDrag = false;
    boolean mIsDrag = false;
    boolean mIsMidFocused = false;
    boolean mIsMaginify = false;

    public double areaValue;
    public double lengthValue;
    public IPoint positionValue;
    public double lastSideValue;
    public double lastlSideValue;
    public double angleValue;
    public double[] eachAngle;
    public double[] gpsPoint = new double[2];

    private CollectActivity mCollectActivity;
    private TextView actionAreaValue, actionPerimeterValue, actionPositionValue;
    private TextView actionLastSideValue, actionLastlSideValue, actionAngleValue;
    private LinearLayout textTopLinearLayout;
    private LinearLayout textTop2LinearLayout;
    private LinearLayout textTop3LinearLayout;

    private SharedPreferences.Editor shared_edit;

    public DrawingTool(Context context) {
        super.setRate();
        mContext = context;
        mDownPt = new PointF();
        mCollectActivity = (CollectActivity) context;
    }

    public void OnCreate(BaseControl buddyControl) {
        this.setBuddyControl(buddyControl);
        this.mEnable = true;

        if (mMapCurrent == null) {
            mMapCurrent = ((MapView) getBuddyControl()).getMap();
        }
        mBitExMap = mMapCurrent.ExportMap(false);
        mBitmapCurrentBack = mBitExMap.copy(Config.RGB_565, true);
        BitmapDrawable bd = new BitmapDrawable(getBuddyControl().getContext().getResources(), mBitmapCurrentBack);
        (getBuddyControl()).setBackgroundDrawable(bd);

        //setValues();
    }

    public void setValues() {
        textTopLinearLayout = (LinearLayout) mCollectActivity.findViewById(R.id.text_top);
        textTop2LinearLayout = (LinearLayout) mCollectActivity.findViewById(R.id.text_top2);
        textTop3LinearLayout = (LinearLayout) mCollectActivity.findViewById(R.id.text_top3);
        textTopLinearLayout.getBackground().setAlpha(160);
        textTop2LinearLayout.getBackground().setAlpha(160);
        textTop3LinearLayout.getBackground().setAlpha(160);

        areaValue = GeoCollectManager.getCollector().getArea();
        lengthValue = GeoCollectManager.getCollector().getLength();
        positionValue = GeoCollectManager.getCollector().getPosition();
        angleValue = GeoCollectManager.getCollector().getAngle();
        if (GeoCollectManager.getCollector().getEachSideAngle().length > 0) {
            if (GeoCollectManager.getCollector().getEachSideAngle().length > 1) {
                for (int i = 0; i < GeoCollectManager.getCollector().getEachSideAngle().length; i++) {
                    System.out.println("angle+++++++++++:" + String.format("%.1f", GeoCollectManager.getCollector().getEachSideAngle()[i]));
                }
            }
        }
        if (GeoCollectManager.getCollector().getEachSideLength().length > 0) {
            if (GeoCollectManager.getCollector().getEachSideLength().length > 1) {
                for (int i = 0; i < GeoCollectManager.getCollector().getEachSideLength().length; i++) {
                    System.out.println("length+++++++++++:" + String.format("%.1f", GeoCollectManager.getCollector().getEachSideLength()[i]));
                }
            }
        }

        if (GeoCollectManager.getCollector().getLastSideLength().length > 0) {
            if (GeoCollectManager.getCollector().getLastSideLength().length > 1) {//濡傛灉鏁扮粍闀垮害澶т簬1锛屽垯璇存槑鑷冲皯鏈変笁鏉¤竟
                lastlSideValue = GeoCollectManager.getCollector().getLastSideLength()[0];
                lastSideValue = GeoCollectManager.getCollector().getLastSideLength()[1];
                System.out.println("鍊掓暟绗簩鏉¤竟闀垮害锛�" + lastlSideValue);
                System.out.println("鏈�鍚庝竴鏉¤竟闀垮害锛�" + lastSideValue);
            } else {//濡傛灉鍙湁涓�鏉¤竟锛堜袱涓偣锛夛紝閭ｄ箞杩欏氨鏄渶鍚庝竴鏉¤竟
                lastlSideValue = GeoCollectManager.getCollector().getLastSideLength()[0];
                System.out.println("鏈�鍚庝竴鏉¤竟闀垮害锛�" + lastlSideValue);
            }
        } else {
            lastSideValue = -1;
            lastlSideValue = -1;
        }


        actionAreaValue = (TextView) mCollectActivity.findViewById(R.id.action_area_value);
        actionPerimeterValue = (TextView) mCollectActivity.findViewById(R.id.action_perimeter_value);
        actionPositionValue = (TextView) mCollectActivity.findViewById(R.id.action_position_value);
        actionLastSideValue = (TextView) mCollectActivity.findViewById(R.id.action_lastside_value);
        actionLastlSideValue = (TextView) mCollectActivity.findViewById(R.id.action_lastlside_value);
        actionAngleValue = (TextView) mCollectActivity.findViewById(R.id.action_angle_value);
        /** 闈㈢Н*/
        if (actionAreaValue != null) {
            actionAreaValue.setText(mCollectActivity.getResources().getString(R.string.action_area)
                    + String.format("%.4f", areaValue / 666.666));
            shared_edit = mCollectActivity.getSharedPreferences("Area", Context.MODE_PRIVATE).edit();
            shared_edit.putString("AreaValue", String.valueOf(String.format("%.4f", areaValue / 666.666)));
            shared_edit.commit();
        } else {
            actionAreaValue.setVisibility(View.GONE);
        }
        /** 鍛ㄩ暱*/
        if (actionPerimeterValue != null) {
            if (lengthValue != -1) {
                actionPerimeterValue.setText(mCollectActivity.getResources().getString(R.string.action_perimeter)
                        + String.format("%.1f", lengthValue));
            } else {
                actionPerimeterValue.setText(mCollectActivity.getResources().getString(R.string.action_perimeter)
                        + mCollectActivity.getResources().getString(R.string.action_invalid));
            }
        } else {
            actionPerimeterValue.setVisibility(View.GONE);
        }
        /** 浣嶇疆*/
        if (positionValue != null) {
            actionPositionValue.setVisibility(View.VISIBLE);
            gpsPoint = GPSConvert.PROJECT2GEO(positionValue.X(), positionValue.Y(), mMapCurrent.getGeoProjectType());
            actionPositionValue.setText(mCollectActivity.getResources().getString(R.string.action_position)
                    + String.format("%.7f", gpsPoint[1]) + "," + String.format("%.7f", gpsPoint[0]));
        } else {
            actionPositionValue.setVisibility(View.GONE);
        }
        /** 鏈�鍚庝竴鏉¤竟*/
        if (actionLastSideValue != null) {
            if (lastSideValue != -1) {
                actionLastSideValue.setText(mCollectActivity.getResources().getString(R.string.action_lastside)
                        + String.format("%.1f", lastSideValue));
            } else {
                actionLastSideValue.setText(mCollectActivity.getResources().getString(R.string.action_lastside)
                        + mCollectActivity.getResources().getString(R.string.action_invalid));
            }
        } else {
            actionLastSideValue.setVisibility(View.GONE);
        }
        /** 鍊掓暟绗簩鏉¤竟*/
        if (actionLastlSideValue != null) {
            if (lastlSideValue != -1) {
                actionLastlSideValue.setText(mCollectActivity.getResources().getString(R.string.action_lastlside)
                        + String.format("%.1f", lastlSideValue));
            } else {
                actionLastlSideValue.setText(mCollectActivity.getResources().getString(R.string.action_lastlside)
                        + mCollectActivity.getResources().getString(R.string.action_invalid));
            }
        } else {
            actionLastlSideValue.setVisibility(View.GONE);
        }
        /** 瑙掑害*/
        if (actionAngleValue != null) {
            if (angleValue != -1) {
                actionAngleValue.setText(mCollectActivity.getResources().getString(R.string.action_angle)
                        + String.format("%.1f", angleValue));
            } else {
                actionAngleValue.setText(mCollectActivity.getResources().getString(R.string.action_angle)
                        + mCollectActivity.getResources().getString(R.string.action_invalid));
            }
        } else {
            actionAngleValue.setVisibility(View.GONE);
        }
    }

    public BigDecimal reservedDecimal(double x) {
        BigDecimal bd = new BigDecimal(x);
        bd = bd.setScale(1, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean flag = false;
        try {
            int mode = 0;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDownPt.set(event.getX(), event.getY());
                    IPoint point = toWorldPoint(mDownPt);
                    Log.e(TAG, String.format("action_down world point[x=%s,y=%s]", point.X(), point.Y()));

                    final int sizeOfPoints = GeoCollectManager.getCollector().getPointsSize();
                    if (sizeOfPoints == 0) {
                        Log.e(TAG, String.format("size of points is %s", sizeOfPoints));
                        addPoint(mDownPt);
                        mIsFirstLine = true;
                        flag = true;
                    } else if (sizeOfPoints >= 2) {
                        if (GeoCollectManager.getCollector().isNearLastPoint(point.X(), point.Y(), -1)) {

                            Log.e(TAG, "action_down point near last point");
                            mCanDrawLine = true;
                            flag = true;
                        }
                    }

                    /*if (GeoCollectManager.getCollector().isPointFocused(point.X(), point.Y())) {
                        GeoCollectManager.getCollector().refresh();
                        Vibrator vibrator = (Vibrator) (mContext.getSystemService(Context.VIBRATOR_SERVICE));
                        vibrator.vibrate(60);
                        mIsDraw = false;
                        mCanDrag = true;
                        mode = 1;
                        flag = true;
                    } else if (GeoCollectManager.getCollector().isMidPointFocused(point.X(), point.Y())) {
                        GeoCollectManager.getCollector().refresh();
                        Vibrator vibrator = (Vibrator) (mContext.getSystemService(Context.VIBRATOR_SERVICE));
                        vibrator.vibrate(60);
                        mIsDraw = false;
                        mCanDrag = true;
                        mIsMidFocused = true;
                        mode = 1;
                        flag = true;
                    } else {
                        mIsDraw = true;
                        mCanDrag = false;
                    }*/
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.e(TAG, String.format("action_move screen point[x=%s,y=%s]", event.getX(), event.getY()));
                    if (mIsFirstLine) {
                        drawLine(event);
                        flag = true;
                    } else if (mCanDrawLine) {
                        drawLine(event);
                        flag = true;
                    }

                    /*if (mCanDrag) {
                        switch (CollectInteroperator.getGeometryType()) {
                            case Point:
                                pointDrag(event);
                                break;
                            case Polyline:
                                lineDrag(event);
                                break;
                            case Polygon:
                                polygonDrag(event);
                                break;
                            default:
                                break;
                        }
                        mIsDrag = true;
                        flag = true;
                    } else {
                        double xx = event.getX() - mDownPt.x;
                        double yy = event.getY() - mDownPt.y;
                        if (Math.sqrt(xx * xx + yy * yy) > 10) {
                            mIsDraw = false;
                        }
                    }*/
                    break;
                case MotionEvent.ACTION_UP:
                    Log.e(TAG, String.format("action_up screen point[x=%s,y=%s]", event.getX(), event.getY()));
                    if (mIsFirstLine) {
                        addPoint(new PointF(event.getX(), event.getY()));
                        mIsFirstLine = false;
                        flag = true;
                    } else if (mCanDrawLine) {
                        addPoint(new PointF(event.getX(), event.getY()));
                        mCanDrawLine = false;
                        flag = true;
                    }

                    /*if (mIsDraw) {
                        //娣诲姞涓�涓喘鏂扮殑鐐�
                        addPoint(new PointF(event.getX(), event.getY()));
                        //setValues();
                        GeoCollectManager.getCollector().refresh();
                        flag = true;
                        if (mIsMaginify) {
                            flag = false;
                        }
                    }

                    if (mCanDrag) {
                        if (mIsDrag) {
                            // Change focused points' x & y
                            //淇敼涓�涓幇鏈夌殑鐐�
                            updatePoint(new PointF(event.getX(), event.getY()));
                            //setValues();
                            GeoCollectManager.getCollector().refresh();
                            mCanDrag = false;
                            mIsDrag = false;
                            flag = true;
                        } else {
                            mCanDrag = false;
                            flag = true;
                        }
                    }*/
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 娣诲姞鐐�,灏嗗睆骞曞潗鏍囪浆鎹负瀹為檯鍦扮悊鍧愭爣,骞跺悜pCollection涓姞鐐�
     *
     * @throws IOException
     */
    private void addPoint(PointF pf) throws IOException {
        try {
            if (mIsMidFocused) {
                GeoCollectManager.getCollector().addPointMid(toWorldPoint(pf));
                GeoCollectManager.getCollector().refresh();
                mIsMidFocused = false;
            } else {
                GeoCollectManager.getCollector().addPoint(toWorldPoint(pf));
                GeoCollectManager.getCollector().refresh();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 淇敼鐐�,灏嗗睆骞曞潗鏍囪浆鎹负瀹為檯鍦扮悊鍧愭爣,骞朵慨鏀筽Collection涓鐐瑰潗鏍�
     *
     * @throws IOException
     */
    private void updatePoint(PointF pf) throws IOException {
        GeoCollectManager.getCollector().updatePoint(toWorldPoint(pf));
    }

    private IPoint toWorldPoint(PointF pf) {
        return getBuddyControl().ToWorldPoint(new PointF(pf.x * mRate, pf.y * mRate));
    }

    private void pointDrag(MotionEvent event) throws Exception {
        // clear elements
        GeoCollectManager.getCollector().clearElements();
        // Prepare
        mBitExMap = mMapCurrent.ExportMap(false);
        mBitmapCurrentBack = mBitExMap.copy(Config.RGB_565, true);
        Canvas canvas = new Canvas(mBitmapCurrentBack);
        // draw point
        GeoCollectManager.getCollector().drawPointsOnCanvas(canvas, event.getX(), event.getY());
        BitmapDrawable bg = new BitmapDrawable(getBuddyControl().getContext().getResources(), mBitmapCurrentBack);
        getBuddyControl().setBackgroundDrawable(bg);
    }

    private void drawLine(MotionEvent event) throws Exception {
        // Prepare
        mBitExMap = mMapCurrent.ExportMap(false);
        mBitmapCurrentBack = mBitExMap.copy(Config.RGB_565, true);
        Canvas canvas = new Canvas(mBitmapCurrentBack);

        // Collecting Line
        GeoCollectManager.getCollector().drawLineOnCanvas(canvas, event.getX(), event.getY());
        BitmapDrawable bg = new BitmapDrawable(getBuddyControl().getContext().getResources(), mBitmapCurrentBack);
        getBuddyControl().setBackgroundDrawable(bg);
    }

    private void lineDrag(MotionEvent event) throws Exception {
        // Clear elements
        GeoCollectManager.getCollector().clearElements();
        // Prepare
        mBitExMap = mMapCurrent.ExportMap(false);
        mBitmapCurrentBack = mBitExMap.copy(Config.RGB_565, true);
        Canvas canvas = new Canvas(mBitmapCurrentBack);
        // Draw Path and Points on canvas
        if (mIsMidFocused) {
            addPoint(new PointF(event.getX(), event.getY()));
            mIsMidFocused = false;
        }
        // Path
        GeoCollectManager.getCollector().drawPathOnCanvas(canvas, event.getX(), event.getY());
        // Points
        GeoCollectManager.getCollector().drawPointsOnCanvas(canvas, event.getX(), event.getY());
        BitmapDrawable bg = new BitmapDrawable(getBuddyControl().getContext().getResources(), mBitmapCurrentBack);
        getBuddyControl().setBackgroundDrawable(bg);
    }

    private void polygonDrag(MotionEvent event) throws Exception {
        // Clear elements
        GeoCollectManager.getCollector().clearElements();
        // Prepare to draw on Canvas
        mBitExMap = mMapCurrent.ExportMap(false);
        mBitmapCurrentBack = mBitExMap.copy(Config.RGB_565, true);
        Canvas canvas = new Canvas(mBitmapCurrentBack);
        // Draw Path and Points on canvas
        if (mIsMidFocused) {
            addPoint(new PointF(event.getX(), event.getY()));
            mIsMidFocused = false;
        }
        // Path
        GeoCollectManager.getCollector().drawPathOnCanvas(canvas, event.getX(), event.getY());
        // Points
        GeoCollectManager.getCollector().drawPointsOnCanvas(canvas, event.getX(), event.getY());
        BitmapDrawable bg = new BitmapDrawable(getBuddyControl().getContext().getResources(), mBitmapCurrentBack);
        getBuddyControl().setBackgroundDrawable(bg);
    }

    @Override
    public String getText() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bitmap getBitmap() {
        // TODO Auto-generated method stub
        return null;
    }
}
