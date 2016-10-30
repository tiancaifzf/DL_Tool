import java.util.ArrayList;

import android.app.Application;
import android.app.Instrumentation;
import android.content.pm.ApplicationInfo;
import android.util.ArrayMap;
import dalvik.system.DexClassLoader;

public class ActivityThreadUtils {
	static Object getActivityThread() {
	Object activity_thread_object = ReflectInvoke.invokeMethod("android.app.ActivityThread", 
			"currentActivityThread", null, new Class[] {}, new Object[] {});
	return activity_thread_object;
	}
	
	static ArrayMap getmPackages(Object activity_thread) {
		if(activity_thread == null) {
			LogUtils.printLog("null ActivityThread referrence passed to getmPackages", 3);
			return null;
		}
		ArrayMap mPackages = (ArrayMap)ReflectInvoke.getFieldObject("android.app.ActivityThread", "mPackages", activity_thread);
		return mPackages;
	}
	
	static ClassLoader getApkClassLoader(Object loaded_apk) {
		if(loaded_apk == null) {
			LogUtils.printLog("Null LoadedApk Ref passed to getApkClassLoader", 3);
			return null;
		}
		ClassLoader cl = (ClassLoader)ReflectInvoke.getFieldObject("android.app.LoadedApk", "mClassLoader", loaded_apk);
		return cl;
	}
	
	static boolean setApkClassLoader(Object loaded_apk, DexClassLoader dx_loader) {
		if(loaded_apk == null) {
			LogUtils.printLog("Null loaded apk referrence can't set classloader", 3);
			return false;
		}
		
		if(dx_loader == null) {
			LogUtils.printLog("Set null dexclassloader is not valid!", 2);
			return false;
		}
		
		boolean ret = ReflectInvoke.setFieldObject("android.app.LoadedApk", "mClassLoader", loaded_apk, dx_loader);
		return ret;
	}
	
	static Object getmBoundApplication(Object activity_thread_obj) {
		if(activity_thread_obj == null) {
			LogUtils.printLog("Null activity thread referrence passed to getmBoundApplication", 3);
			return null;
		}
		
		Object app_bind_data = ReflectInvoke.getFieldObject("android.app.ActivityThread", "mBoundApplication", activity_thread_obj);
		return app_bind_data;
	}
	
	static Object getLoadedApkInfo(Object app_bind_data) {
		if(app_bind_data == null) {
			LogUtils.printLog("Null app bind data referrence passed to getLoadedApkInfo", 3);
			return null;
		}
		
		Object loaded_apk_info = ReflectInvoke.getFieldObject("android.app.ActivityThread$AppBindData", "info", app_bind_data);
		return loaded_apk_info;
	}
	
	static ArrayList<Application> getmAllApplications(Object activity_thread_obj) {
		if(activity_thread_obj == null) {
			LogUtils.printLog("null activity thread obj passed to getmAllApplications", 3);
			return null;
		}
		
		ArrayList<Application> all_application_list = (ArrayList<Application>) ReflectInvoke.getFieldObject("android.app.ActivityThread", "mAllApplications", activity_thread_obj);
		return all_application_list;
	}
	
	static ApplicationInfo getmApplicationInfo(Object loaded_apk_obj) {
		if(loaded_apk_obj == null) {
			LogUtils.printLog("Null LoadedApk Referrence passed to getmApplicationInfo!", 3);
			return null;
		}
		
		ApplicationInfo app_info = (ApplicationInfo)ReflectInvoke.getFieldObject("android.app.LoadedApk", "mApplicationInfo", loaded_apk_obj);
		return app_info;
	}
	
	static ApplicationInfo getBindApplicationInfo(Object mBoundApplication) {
		if(mBoundApplication == null) {
			LogUtils.printLog("Null mBoundApplication referrence passed to getBindApplicationInfo", 3);
			return null;
		}
		
		ApplicationInfo bind_app_info = (ApplicationInfo)ReflectInvoke.getFieldObject("android.app.ActivityThread$AppBindData", "appInfo", mBoundApplication);
		return bind_app_info;
	}
	
	static Application makeApplication(Object loaded_apk_obj) {
		if(loaded_apk_obj == null) {
			LogUtils.printLog("Null LoadedApk Referrence passed to makeApplication!", 3);
			return null;
		}
		
		Application app = (Application)ReflectInvoke.invokeMethod("android.app.LoadedApk", "makeApplication", 
				loaded_apk_obj, new Class[] {boolean.class, Instrumentation.class}, new Object[] {false, null});
		return app;
	}
	
	static ArrayMap getProviderMap(Object activity_thread_obj) {
		if(activity_thread_obj == null) {
			LogUtils.printLog("Null ActivityThread Referrence passed to getProviderMap", 3);
			return null;
		}
		
		ArrayMap mProviderMap = (ArrayMap)ReflectInvoke.getFieldObject("android.app.ActivityThread", "mProviderMap", activity_thread_obj);
		return mProviderMap;
	}
}
