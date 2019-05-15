package passmanage.android.com.Util;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtil {

    public static void  fileCopy(File dbFile, File backup) throws IOException {
        FileChannel inChannel = new FileInputStream(dbFile).getChannel();
        FileChannel outChannel = new FileOutputStream(backup).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            Log.e("DbBackups", "数据库文件操作异常" + e.getMessage());
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    public static void reNameFile(String oldName,String newName){
        File old = new File(oldName);

        File newN = new File(newName);

        old.renameTo(newN);

    }

    public static int deleteFile(String str){
        @SuppressLint("SdCardPath")
        File file =new File( "/sdcard/PassManage/"+str );
        try{
            file.delete();
        }catch (Exception e){
            return -1;
        }

        return 0;
    }
}
