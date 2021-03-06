package com.willc.collector.interoperation.acticity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.willc.collector.R;
import com.willc.collector.datamgr.GeoCollectManager;
import com.willc.collector.interoperation.CollectInteroperator;
import com.willc.collector.lib.map.IMap;
import com.willc.collector.lib.map.Map;
import com.willc.collector.lib.view.MapView;
import com.willc.collector.tools.DrawingTool;
import com.willc.collector.tools.EditTools;
import com.willc.collector.tools.GPSUtil;

import java.io.IOException;
import java.util.EventObject;

import srs.Geometry.Envelope;
import srs.Geometry.srsGeometryType;

/**
 * 要素采集和节点编辑Activity
 */
public class CollectActivity extends Activity {
    private MapView mapView = null;
    // The tool of collecting points Manually
    private DrawingTool drawingTool = null;

    private IMap map = null;
    // The geometry type of the current feature collecting
    private srsGeometryType mtype = null;

    // UI references.
    private LinearLayout actionBack = null;
    private LinearLayout actionSave = null;
    private LinearLayout actionGPS = null;
    private LinearLayout actionUndo = null;
    private LinearLayout actionDelpt = null;
    private LinearLayout actionClear = null;
    private TextView txtTitle = null;

    private SharedPreferences shared;
    public static String Area_value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_collect);

        mapView = (MapView) findViewById(R.id.map_collect);

        try {
            CollectInteroperator.init(this.loadMap(), srsGeometryType.Polygon);

            mapView.setMap(CollectInteroperator.getMap());
            mapView.Refresh();
            GeoCollectManager.setMapControl(mapView);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set Geometry Type to GeoCollectManager
        mtype = CollectInteroperator.getGeometryType();
        GeoCollectManager.setGeometryType(mtype);

        // Edit settings
        if (!CollectInteroperator.isNew()) {
            try {
                GeoCollectManager.getCollector().setEditGeometry(CollectInteroperator.getEditGeometry());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Set title according to Geometry Type
        txtTitle = (TextView) findViewById(R.id.title);
        txtTitle.setText("量房采集");


        // Init DrawingTool tool and Set it to BuddyControl
        if (drawingTool == null) {
            drawingTool = new DrawingTool(this);
        }
        drawingTool.OnCreate(mapView);
        mapView.setDrawTool(drawingTool);

        // Initial action controls
        actionBack = (LinearLayout) findViewById(R.id.action_back);
        actionSave = (LinearLayout) findViewById(R.id.action_save);
        actionGPS = (LinearLayout) findViewById(R.id.action_gps);
        actionUndo = (LinearLayout) findViewById(R.id.action_undo);
        actionDelpt = (LinearLayout) findViewById(R.id.action_delpt);
        actionClear = (LinearLayout) findViewById(R.id.action_clear);

        // Set click event to them
        bindEventToActions();
    }



    private void bindEventToActions() {
        actionBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
        actionSave.setOnClickListener(new OnClickListener() {
            @Override
			public void onClick(View v) {
				Intent intent = getIntent();
				if (GeoCollectManager.getCollector().isGeometryValid(CollectActivity.this)) {
					boolean isSuccess = false;
					if (CollectInteroperator.isNew()) {
					    shared = getSharedPreferences("Area", MODE_PRIVATE);
                        Area_value = shared.getString("AreaValue", "");
						isSuccess = CollectInteroperator.CollectEventManager.fireCollectSave(new EventObject(v));
					} else if (intent.getBooleanExtra("obtainArea", false)) {
						isSuccess = CollectInteroperator.CollectEventManager.fireAreaSave(new EventObject(v));
					} else {
						isSuccess = CollectInteroperator.CollectEventManager.fireEditSave(new EventObject(v));
					}
					if (isSuccess) {
						// 释放资源
						try {
							dispose();
						} catch (IOException e) {
							showToast(e.getMessage());
						}
						
						if(intent.getBooleanExtra("obtainArea", false)){
						    shared = getSharedPreferences("Area", MODE_PRIVATE);
	                        Area_value = shared.getString("AreaValue", "");
	                        intent.putExtra("areaValue", CollectInteroperator.getMCaArea());
                            CollectActivity.this.setResult(601, intent);
						}
						finish();
					}
				}
			}
        });
        actionGPS.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    GPSUtil.addPointForCollecting( mapView.getMap().getGeoProjectType());
                    drawingTool.setValues();
                } catch (Exception e) {
                    showToast(e.getMessage());
                }
            }
        });
        actionUndo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    EditTools.undo();
                    drawingTool.setValues();
                } catch (Exception e) {
                    showToast(e.getMessage());
                }
            }
        });
        if (mtype != srsGeometryType.Point) {
            actionDelpt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        EditTools.delpt();
                        drawingTool.setValues();
                    } catch (Exception e) {
                        showToast(e.getMessage());
                    }
                }
            });
            actionClear.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        EditTools.clear();
                        drawingTool.setValues();
                    } catch (Exception e) {
                        showToast(e.getMessage());
                    }
                }
            });
        } else {
            actionDelpt.setVisibility(View.GONE);
            actionClear.setVisibility(View.GONE);
        }
    }

    private String getTitleDesc() {
        switch (mtype) {
            case Point:
                return "点要素采集";
            case Polyline:
                return "线要素采集";
            case Polygon:
                return "面要素采集";
            default:
                return "量算";
        }
    }

    private void back() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                CollectActivity.this);
        builder.setTitle("是否放弃采集?");
        builder.setMessage("是否确定要放弃已采集的要素?");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("放弃", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	 Intent intent = getIntent();
                // 触发返回处理事件
                boolean isSuccess = false;
                if (CollectInteroperator.isNew()) {
                    isSuccess = CollectInteroperator.CollectEventManager
                            .fireCollectBack(new EventObject(
                                    CollectActivity.this));
                }else if(intent.getBooleanExtra("obtainArea", false)){
                	isSuccess = CollectInteroperator.CollectEventManager
                            .fireAreaBack(new EventObject(
                                    CollectActivity.this));
                } else {
                    isSuccess = CollectInteroperator.CollectEventManager
                            .fireEditBack(new EventObject(CollectActivity.this));
                }
                if (isSuccess) {
                    // 回收资源
                    try {
                        dispose();
                    } catch (IOException e) {
                        showToast(e.getMessage());
                    }
                    if(intent.getBooleanExtra("obtainArea", false))
                    {
                        CollectActivity.this.setResult(602, intent);
                    }else{
                    	CollectActivity.this.setResult(604, intent);
                    }
                    // 关闭activity
                    finish();
                }
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            back();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 要素采集资源释放
     *
     * @throws IOException
     */
    private void dispose() throws IOException {
        mapView.setDrawTool(null);
        drawingTool = null;
        mtype = null;
        GeoCollectManager.dispose();
        CollectInteroperator.dispose();
        System.gc();
    }

    @SuppressLint("ShowToast")
    private void showToast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT);
    }

    /**
     * 测试用 加载测试数据
     *
     * @throws Exception
     */
    public IMap loadMap() throws Exception {
        if (this.map == null) {
            this.map = new Map(new Envelope(0, 0, 100D, 100D));

            // 加载影像文件数据 /TestData/IMAGE/长葛10村.tif /test/辉县市/IMAGE/01.tif  /storage/emulated/0/FlightTarget/廊坊.tif
            /*final String tifPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/collector/长葛10村.tif";
            Log.i(TAG, tifPath);

            File tifFile = new File(tifPath);
            if (tifFile.exists()) {
                IRasterLayer layer = new RasterLayer(tifPath);
                if (layer != null) {
                    this.map.AddLayer(layer);
                }
            }*/

            // 加载shp矢量文件数据 /TestData/Data/调查村.shp /test/辉县市/TASK/村边界.shp
            /*final String shpPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/collector/Data/调查村.shp";
            Log.d(TAG, shpPath);

            File shpFile = new File(shpPath);
            if (shpFile.exists()) {
                IFeatureLayer layer = new FeatureLayer(shpPath);
                if (layer != null) {
                    this.map.AddLayer(layer);
                }
            }

            this.map.setExtent(((IFeatureLayer) map.GetLayer(0)).getFeatureClass().getGeometry(1).Extent());
            this.map.setGeoProjectType(ProjCSType.ProjCS_WGS1984_Albers_BJ);*/
        }
        return this.map;
    }
}
