package passmanage.android.com;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;

import static passmanage.android.com.Util.MyToast.showToast;

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {
    private int AuthTimes=0;
    private boolean firstIn;
    private CancellationSignal cancellationSignal;
    private Context context;
    private Activity activity;
    private String pass;

    public FingerprintHandler(Context mContext, Activity activity,boolean firstIn,String pass) {
        this.context = mContext;
        this.activity = activity;
        this.firstIn = firstIn;
        this.pass = pass;
    }

    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {

        cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);

    }

    public void finishAuth(){

        cancellationSignal.cancel();
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        //showToast(context,"验证错误！",1000);

    }

    @Override
    public void onAuthenticationFailed() {
        showToast(activity,"验证失败！",1000);
        AuthTimes++;
        if (AuthTimes>=3){
            finishAuth();
            showToast(activity,"三次错误，退出验证！",1000);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        //MyToast(context,"未知情况！",1000);
    }
    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        if(firstIn){
            showToast(context,"第一次请使用密码登录！",1000);
        }else{
            finishAuth();
            Intent i = new Intent(context, PassList.class);
            //showToast(context,"验证成功！",1000);
            i.putExtra( "p",pass );
            context.startActivity(i);
            activity.finish();
        }



    }

}