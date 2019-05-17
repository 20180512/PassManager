package com.android.passmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.android.passmanager.Util.Aes;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;


import static com.android.passmanager.Util.MyToast.showToast;


public class Login extends AppCompatActivity {

    private static final String TAG = "Login";
    private EditText userin;
    private String userinStr;
    private EditText passin;
    private String passinStr;
    private TextView tip,underText;
    private String[] str;
    private ImageView fingerview;
    private boolean fingerTouch=true;
    private KeyStore keyStore;
    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    FingerprintHandler helper;
    Database login;
    WebView mWebView;
    //Database passDb = new Database( this , "passEngine.db" , null , 1,"Pass" );

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login );
        mWebView = findViewById(R.id.background);
        WebSettings webSettings = mWebView.getSettings();
        //webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        // 下面的一句话是必须的，必须要打开javaScript不然所做一切都是徒劳的
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
        mWebView.loadUrl("file:///android_asset/login_background.html");
        tip= findViewById( R.id.tip );
        underText=findViewById( R.id.lost_passid );
        login =new Database( this , "login.db" , null , 1,"Login"  );
        str =login.getDate();
        userin = findViewById( R.id.userinput);
        if(str!=null){
            userin.setText( str[0] );
        }else {
            tip.setText( getString(R.string.input_then_query) );
        }
        underText.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //帮助文字
                // underText.setText(  );
            }
        } );
        //数据升级操作
        /*tip.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkedList <Account> accounts=passDb.getAll();
                for(int i =0;i<accounts.size();i++){
                    String title=encrypt(str[1],accounts.get( i ).getTitle());
                    String acc =encrypt(str[1],accounts.get( i ).getAccount());
                    String pass =encrypt(str[1],decrypt( str[1] , accounts.get( i ).getPassword() ));
                    Account account =new Account( title,acc,pass );
                    passDb.upData( account,accounts.get( i ).getId() );

                }
                showToast( Login.this,"变更完成！",1000 );
            }
        } );*/
        passin =findViewById( R.id.passinput );
        fingerview = findViewById( R.id.fingerView );
        fingerview.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userinStr =userin.getText().toString().trim();
                passinStr = Aes.getMD5(passin.getText().toString().trim(),16 );
                if (passin.getText().toString().trim().equals( "" )&&userinStr.equals( "" )){
                    showToast( Login.this,getString(R.string.input_name_password),1000 );
                }else if (userinStr.equals( "" )){
                    showToast( Login.this,getString(R.string.input_name),1000 );
                    Log.w(TAG,"loginName empty");
                }else if(passin.getText().toString().trim().equals( "" )){
                    showToast( Login.this,getString(R.string.input_password),1000 );
                    Log.w(TAG,"password empty");
                }else if(str!=null&&userinStr.equals( str[0] )&&passinStr.equals( str[1] )){
                    Intent intent =new Intent( Login.this,PassList.class );
                    intent.putExtra( "p",passinStr );
                    startActivity( intent );
                    Login.this.finish();
                    startAuth( 0 );
                }else if(str==null){
                    final String clearPass =passin.getText().toString().trim();

                    //showToast( Login.this,"你的密码是："+passin.getText().toString().trim()+"，请保管妥当",3000 );
                    final Dialog dialog = new Dialog( v.getContext() );
                    dialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
                    dialog.setCancelable( true );
                    dialog.setContentView( R.layout.pass_input_auth_dialog );
                    TextView dialogName =dialog.findViewById( R.id.dialog_title );
                    dialogName.setText( getString(R.string.remenber_tip) );
                    final EditText editText = dialog.findViewById( R.id.pass_deco );
                    TextView textView = dialog.findViewById( R.id.textview_pass );
                    final Button update = dialog.findViewById( R.id.quit );
                    final Button enter = dialog.findViewById( R.id.back );
                    editText.setVisibility( View.GONE );
                    textView.setVisibility( View.VISIBLE );
                    textView.setText( clearPass );
                    enter.setText( getString(R.string.copy) );
                    update.setText( getString(R.string.i_am_remenber) );
                    enter.setOnClickListener( new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService( Context.CLIPBOARD_SERVICE );
                            ClipData clip = ClipData.newPlainText( null , clearPass );
                            clipboard.setPrimaryClip( clip );
                            Log.i( TAG,"password copy into clipboard" );
                            showToast( Login.this,getString(R.string.copyed),1000 );
                        }
                    } );

                    update.setOnClickListener( new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent =new Intent( Login.this,PassList.class );
                            intent.putExtra( "p",passinStr );
                            intent.putExtra( "firstTag","yes" );
                            startActivity( intent );
                            //用户点击已记住将密码写进数据库
                            login.addData( new Account("login",userinStr,passinStr));
                            Log.i( TAG,"password write into db" );

                            Login.this.finish();
                            startAuth( 0 );
                            dialog.dismiss();


                        }
                    } );
                    dialog.show();

                }else{
                    showToast( Login.this,getString(R.string.login_name_password_err),3000 );
                    Log.w(TAG,"password err");

                }


            }
        } );
        startAuth( 1 );
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void generateKey() throws FingerprintException {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            KeyGenerator keyGenerator = KeyGenerator.getInstance( KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore" );
            keyStore.load(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyGenerator.init(new

                        KeyGenParameterSpec.Builder(KEY_NAME,
                        KeyProperties.PURPOSE_ENCRYPT |
                                KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(
                                KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build());
            }

            keyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            exc.printStackTrace();
            throw new FingerprintException(exc);
        }
    }

    private class FingerprintException extends Exception {
        FingerprintException(Exception e) {
            super(e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean initCipher() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cipher = Cipher.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES + "/"
                                + KeyProperties.BLOCK_MODE_CBC + "/"
                                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            }
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (@SuppressLint("NewApi") KeyPermanentlyInvalidatedException e) {

            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    public void startAuth(int i){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService( KEYGUARD_SERVICE );
            FingerprintManager fingerprintManager = (FingerprintManager) getSystemService( FINGERPRINT_SERVICE );
            if (!fingerprintManager.isHardwareDetected()&&fingerTouch) {
                tip.setText( getString(R.string.click_to_login) );
                fingerTouch=false;
                userin = findViewById( R.id.userinput);
                userin.setText( Build.MODEL );
            }else if (fingerTouch){
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                    showToast( Login.this,getString(R.string.please_accect_permission),1000 );
                }

                if (!fingerprintManager.hasEnrolledFingerprints()) {
                    showToast( Login.this,getString(R.string.no_finger),1000 );
                }

                if (!keyguardManager.isKeyguardSecure()) {
                    showToast( Login.this,getString(R.string.unlock_then_try),1000 );
                } else {
                    try {
                        generateKey();
                    } catch (FingerprintException e) {
                        e.printStackTrace();
                    }

                    if (initCipher()&&i==1) {
                        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject( cipher );
                        final boolean noData = (str == null);
                        if (noData) {
                            String[] str1 = new String[2];
                            str1[0]="null";
                            str1[1]="null";
                            helper = new FingerprintHandler(this, Login.this,noData,str1[1]);
                        }else{
                            helper = new FingerprintHandler(this, Login.this,noData,str[1]);
                        }


                        helper.startAuth( fingerprintManager, cryptoObject );
                    }else{
                        helper.finishAuth();
                    }
                }
            }

        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onStart() {
        super.onStart();
        WebSettings webSettings = mWebView.getSettings();
        //webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        // 下面的一句话是必须的，必须要打开javaScript不然所做一切都是徒劳的
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
        mWebView.loadUrl("file:///android_asset/login_background.html");
    }
}
