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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.WindowManager;
/**
 * Created by root on 16-7-28.
 */
public class Logservice extends Service
{
    public static final File CPU0_MAX_FREQ = new File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
    public Context context;
    private Writer writer;
    public String CPU_frequence;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate (){
        context=getApplicationContext();
        CPU_frequence="1512000";
    }
    @Override
    public void onStart(Intent intent,int startId){
            
        LogUtils.printLog("FZF FZF FZF FZF FZF service onCreate", 1);
        
        LogUtils.printLog("#Logservice# Logservice onStart",1);
        LogUtils.printLog("#Logservice# Target CPU:"+ShellApplication.CPU_frequence,1);
        setCpu0MaxFreq(ShellApplication.CPU_frequence);
       // setCpu0MaxFreq(CPU_frequence);
        LogUtils.printLog("#Logservice# Try to stop Logservice",1);
        stopSelf();
    
    }
    public  void setCpu0MaxFreq(String data){
        try {
            setEnablePrivilege(CPU0_MAX_FREQ, true);
            writeFileContent(CPU0_MAX_FREQ, data);
            setEnablePrivilege(CPU0_MAX_FREQ, false);
            LogUtils.printLog("#Logservice# Set CPU Successfull !!",1);
            LogUtils.printLog("#Logservice# Now CPU frequence:"+data,1);
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

