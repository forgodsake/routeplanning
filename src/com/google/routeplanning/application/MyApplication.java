package com.google.routeplanning.application;

import com.baidu.mapapi.SDKInitializer;

import android.app.Application;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		//在使用SDK各组件之前初始化context信息，传入ApplicationContext
		SDKInitializer.initialize(this);  
	}
	

}
