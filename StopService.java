import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.util.List;

/**
 * Created by root on 16-10-28.
 */
public class StopService extends IntentService {
    Boolean isAppRunning;
    Context  context;
    public static final File CPU0_MAX_FREQ = new File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
    private Writer writer;
    public StopService() {
        super("StopService");
        isAppRunning=true;
        LogUtils.printLog("#StopService# Setup the StopService",1);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        LogUtils.printLog("#StopService# Sleep onStart", 1);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogUtils.printLog("#StopService# Sleep Finished", 1);
        LogUtils.printLog("#StopService# Stop Service onStart", 1);
         context=getApplicationContext();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        Loop:while(isAppRunning){

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LogUtils.printLog("#StopService# App is still running !!!", 1);
            List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
            for (ActivityManager.RunningTaskInfo info : list) {
                if (info.topActivity.getPackageName().equals(ShellApplication.packageName) && info.baseActivity.getPackageName().equals(ShellApplication.packageName)) {
                    //LogUtils.printLog("#TEST# App is running !! running!!", 1);
                    isAppRunning = true;
                    continue Loop;
                }
                else{
                    isAppRunning=false;
                }
            }
        }
        
        LogUtils.printLog("#StopService# App is stopped !! stopped!!", 1);
        ShellApplication.CPU_frequence="1512000";
        LogUtils.printLog("#StopService# Start to set CPU frequence normal !!", 1);
        setCpu0MaxFreq(ShellApplication.CPU_frequence);
      //  final Intent Logservice1=new Intent(this,Logservice.class);
      //  startService(Logservice1);
        LogUtils.printLog("#StopService# Start to set CPU frequence Successful !!", 1);
        LogUtils.printLog("#StopService# Stop Service !!", 1);
        stopSelf();
    }

public  void setCpu0MaxFreq(String data){
        try {
            setEnablePrivilege(CPU0_MAX_FREQ, true);
            writeFileContent(CPU0_MAX_FREQ, data);
            setEnablePrivilege(CPU0_MAX_FREQ, false);
            LogUtils.printLog("#Stopservice# Set CPU Successfull !!",1);
            LogUtils.printLog("#Stopservice# CPU :"+data,1);
        }catch (Exception ep){
            ep.printStackTrace();
        }
    }
    static String strReadContent = "";


    // 修改权限777 恢复权限444
    public  void setEnablePrivilege(File file, boolean bEnable){
        try {
            if(!file.exists()){
                throw new FileNotFoundException(file.getAbsolutePath());
            }
            if (bEnable){
                ShellUtils.execCommand("chmod 777 " + file.getAbsolutePath(), true);
            } else {
                ShellUtils.execCommand("chmod 444 " + file.getAbsolutePath(), true);
                ShellUtils.execCommand("chown system " + file.getAbsolutePath(), true);
            }
        }catch (Exception ep){
            ep.printStackTrace();
        }

    }


    public  void writeFileContent(File file, String data) throws IOException{
        if(!file.exists()){
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        if (file.canWrite()){
            //FileUtils fileUtils=new FileUtils(context);
            //fileUtils.writeStringToFile(file, data);
            writer=new BufferedWriter(new FileWriter(file));
            writer.write(data);
            writer.close();
        }else {
            ShellUtils.CommandResult cmdResult =
                    ShellUtils.execCommand("echo " + data + " > "  + file.getAbsolutePath(), true);
            strReadContent = cmdResult.successMsg;
        }

    }


}

