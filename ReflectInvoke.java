import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
public class ReflectInvoke {
	static Object invokeMethod(String class_name, String method_name, Object caller, Class[] param_type, Object[] param_value) {
		try {
			Class<?> target_class = Class.forName(class_name);
			Method target_method = target_class.getMethod(method_name, param_type);
			return target_method.invoke(caller, param_value);
		} catch(ClassNotFoundException e) {
			LogUtils.printLog("Class " + class_name + " not found", 2);
		} catch(NoSuchMethodException e) {
			LogUtils.printLog("No Such Method " + method_name, 2);
		} catch(InvocationTargetException e) {
			LogUtils.printLog("Method " + method_name + " invocation exception happened.", 2);
		} catch(IllegalAccessException e) {
			LogUtils.printLog("Method " + method_name + " IllegalAccessException happened", 2);
		}
		
		return null;
	}
	
	static Object getFieldObject(String class_name, String field_name, Object caller) {
		try {
			Class<?> target_class = Class.forName(class_name);
			Field target_field = target_class.getDeclaredField(field_name);
			target_field.setAccessible(true);
			return target_field.get(caller);
		} catch(ClassNotFoundException e) {
			LogUtils.printLog("Class " + class_name + " not found", 2);
		} catch(NoSuchFieldException e) {
			LogUtils.printLog("No such field " + field_name, 2);
		} catch(IllegalAccessException e) {
			LogUtils.printLog("Get field " + field_name + " IllegalAccessException happened", 2);
		}
		
		return null;
	}
	
	static boolean setFieldObject(String class_name, String field_name, Object caller, Object field_value) {
		try {
			Class<?> target_class = Class.forName(class_name);
			Field target_field = target_class.getDeclaredField(field_name);
			target_field.setAccessible(true);
			target_field.set(caller, field_value);
			return true;
		} catch(ClassNotFoundException e) {
			LogUtils.printLog("Class " + class_name + " not found", 2);
		} catch(NoSuchFieldException e) {
			LogUtils.printLog("No such field " + field_name, 2);
		} catch(IllegalAccessException e) {
			LogUtils.printLog("Set Field " + field_name + " happened", 2);
		}
		
		return false;
	}
}
