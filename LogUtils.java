import android.util.Log;
public class LogUtils {
	private static boolean log_on = false;
	private static String LOG_TAG = null;
	static void initLogUtils(String tag, boolean on) {
		log_on = on;
		if(tag != null) {
			LOG_TAG = tag;
		} else {
			LOG_TAG = "Default Shell";
		}
	}
	
	//level :0---->info, 1---->debug, 2---->warn, 3---->error
	static void printLog(String line, int level) {
		if(!log_on) {
			return ;
		}
		
		switch(level) {
		case 0:
			Log.i(LOG_TAG, line);
			break;
		case 1:
			Log.d(LOG_TAG, line);
			break;
		case 2:
			Log.w(LOG_TAG, line);
			break;
		case 3:
			Log.e(LOG_TAG, line);
			break;
		default:
			Log.e(LOG_TAG, "invalid log level " + level + "in printLog");
		}
	}
}
