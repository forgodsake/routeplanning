package com.google.routeplanning;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapLongClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
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

public class MainActivity extends Activity implements BDLocationListener{

	private MapView mapView;
	private boolean useDefaultIcon;
	private BaiduMap mBaiduMap;
	private LatLng stlatlng;
	private LatLng edlatlng;
	// 定位相关
	private LocationClient mLocClient;
	private boolean isFirst=true;

	// 自定义起点终点 图标：

	class MyWalkingRouteOverlay extends WalkingRouteOverlay {

		public MyWalkingRouteOverlay(BaiduMap baiduMap) {

			super(baiduMap);

		}

		@Override
		public BitmapDescriptor getStartMarker() {

			if (useDefaultIcon) {

				return BitmapDescriptorFactory.fromResource(R.drawable.st);

			}

			return null;

		}

		@Override
		public BitmapDescriptor getTerminalMarker() {

			if (useDefaultIcon) {

				return BitmapDescriptorFactory.fromResource(R.drawable.ed);

			}

			return null;

		}
	}

	@Override
	public void onReceiveLocation(BDLocation location) {

		stlatlng = new LatLng(location.getLatitude(), location.getLongitude());
		if (location == null || mapView == null)
			return;
		if (isFirst) {
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(90).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			LatLng ll1 = new LatLng(location.getLatitude(),
					location.getLongitude());
			MapStatusUpdate u1 = MapStatusUpdateFactory.newLatLng(ll1);
			mBaiduMap.animateMapStatus(u1);
			isFirst = false;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);
		// 初始化mapview对象，并且设置显示缩放控件
		mapView = (MapView) findViewById(R.id.bmapsView);
		mapView.showZoomControls(true);

		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(this);//位置监听
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 是否打开gps
		option.setLocationMode(LocationMode.Hight_Accuracy);//定位模式设置
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000); //1000毫秒更新一次位置
		mLocClient.setLocOption(option);
		mLocClient.start();

		final RoutePlanSearch mSearch = RoutePlanSearch.newInstance();

		final OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
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

		mBaiduMap = mapView.getMap();
		MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.zoomBy(7);
		mBaiduMap.animateMapStatus(mapStatusUpdate);
		mBaiduMap.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(LatLng arg0) {
				edlatlng = arg0;
				mSearch.setOnGetRoutePlanResultListener(listener);
				PlanNode stNode = PlanNode.withLocation(stlatlng);
				PlanNode edNode = PlanNode.withLocation(edlatlng);
				mSearch.walkingSearch(new WalkingRoutePlanOption().from(stNode)
						.to(edNode));
			}
		});

	}


}
