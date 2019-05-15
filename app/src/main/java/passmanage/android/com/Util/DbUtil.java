package passmanage.android.com.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static passmanage.android.com.Util.FileUtil.fileCopy;
import static passmanage.android.com.Util.FileUtil.reNameFile;

/**
 * 数据库还原类
 */
public class DbUtil {
    @SuppressLint("SdCardPath")
    private static final String FileDirName="PassManage";

    public  static int restore(String s, Context context) {
        String cachePath = context.getCacheDir().getPath();

        File cacheDir = new File( cachePath );
        File sdcardDBFile = new File( Environment.getExternalStorageDirectory().getPath() + File.separator + FileDirName , s );

        File oldNameFile = new File( cachePath , s );
        File newNameFile = new File( cachePath , "passEngine.db" );

        File dbFile = context.getDatabasePath( "passEngine.db" );

        try {
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            oldNameFile.createNewFile();

            fileCopy( sdcardDBFile , oldNameFile );

            oldNameFile.renameTo( newNameFile );
        } catch (IOException e1) {
            e1.printStackTrace();
            return -1;
        }


        try {
            fileCopy( newNameFile , dbFile );
        } catch (IOException e1) {

            e1.printStackTrace();
            return -1;
        }
        return 1;

    }

    public static int DbBackups(String date,Context context) {
        String path = context.getDatabasePath( "passEngine.db" ).getPath();

        File dbFile = new File( path );
        File exportDir = new File( Environment.getExternalStorageDirectory().getPath()+File.separator+FileDirName );

        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File backup = new File( exportDir, dbFile.getName() );
        try {
            backup.createNewFile();
            fileCopy( dbFile, backup );
        } catch (IOException e) {
            e.printStackTrace();
        }

        File oldName = new File( Environment.getExternalStorageDirectory().getPath()+File.separator+FileDirName,"passEngine.db" );
        File newName = new File( Environment.getExternalStorageDirectory().getPath()+File.separator+FileDirName,"USER_DATA" + date );

        if (oldName.renameTo( newName )) {
            return 1;
        } else {
            return -1;
        }
    }

    public static String StringFilter(String str)throws PatternSyntaxException {
        String regEx = "[/\\:*?<>|\"\n\t'-]"; //要过滤掉的字符
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

}