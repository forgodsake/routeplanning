package com.google.routeplanning;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapLongClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;

public class MainActivity extends Activity {

	private MapView mapView;
	private boolean useDefaultIcon;
	private BaiduMap mBaiduMap;
	private LatLng latlng;
	
	// 自定义起点终点 图标：

		 class MyWalkingRouteOverlay extends WalkingRouteOverlay {

			public MyWalkingRouteOverlay(BaiduMap baiduMap) {

				super(baiduMap);

			}

			@Override
			public BitmapDescriptor getStartMarker() {

				if (useDefaultIcon) {

					return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);

				}

				return null;

			}

			@Override
			public BitmapDescriptor getTerminalMarker() {

				if (useDefaultIcon) {

					return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);

				}

				return null;

			}
		}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 初始化mapview对象，并且设置显示缩放控件
		mapView = (MapView) findViewById(R.id.bmapsView);
		mapView.showZoomControls(true);
		
		mBaiduMap = mapView.getMap();
		mBaiduMap.setOnMapLongClickListener(new OnMapLongClickListener() {
			
			@Override
			public void onMapLongClick(LatLng arg0) {
				latlng = arg0;
			}
		});
		RoutePlanSearch mSearch = RoutePlanSearch.newInstance();
		OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
			public void onGetWalkingRouteResult(WalkingRouteResult result) {
				// 获取步行线路规划结果

				if (result == null
						|| result.error != SearchResult.ERRORNO.NO_ERROR) {

					Toast.makeText(MainActivity.this, "抱歉，未找到结果",

					Toast.LENGTH_SHORT).show();

				}

				if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {

					// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息

					// result.getSuggestAddrInfo()

					return;

				}

				if (result.error == SearchResult.ERRORNO.NO_ERROR) {

					// route = result.getRouteLines().get(0);
					useDefaultIcon = true;

					WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(

					mBaiduMap);

					mBaiduMap.setOnMarkerClickListener(overlay);

					overlay.setData(result.getRouteLines().get(0));

					overlay.addToMap();

					overlay.zoomToSpan();

				}
			}

			public void onGetTransitRouteResult(TransitRouteResult result) {
				// 获取公交换乘路径规划结果
			}

			public void onGetDrivingRouteResult(DrivingRouteResult result) {
				// 获取驾车线路规划结果
			}
		};
		mSearch.setOnGetRoutePlanResultListener(listener);
		PlanNode stNode = PlanNode.withLocation(arg0);
		PlanNode edNode = PlanNode.withLocation(latlng);
		mSearch.walkingSearch(new WalkingRoutePlanOption().from(stNode).to(
				edNode));
	}

}
