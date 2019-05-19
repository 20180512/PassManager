package com.android.passmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.android.passmanager.Util.DbUtil;
import com.android.passmanager.Util.FileUtil;
import com.android.passmanager.Util.SurperAdapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.android.passmanager.Util.Aes.decrypt;
import static com.android.passmanager.Util.Aes.encrypt;
import static com.android.passmanager.Util.MyToast.showToast;


public class PassList extends AppCompatActivity {
    private static final String TAG ="PassList" ;
    private RecyclerView gridView;
    private TextView title_text;
    private EditText search_ed;
    private ImageView serch_img;
    private FileListAdapter myAdapter;
    private String inPass;
    private ImageView backupIcon;
    float x;
    private LinkedList <Account> s;
    private SurperAdapter<Account> accountAdapter;
    private String firstInTag;
    private FloatingActionButton add;
    Database passDb = new Database( this , "passEngine.db" , null , 1 , "Pass" );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_pass_list );
        //检查权限
        requestPermission();
        
        initView();
        
        initData();
        
        initAction();
        /*Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );*/
        refresh( 0 );
    }

    private void initData() {
        inPass = getIntent().getStringExtra( "p" );
        firstInTag = getIntent().getStringExtra( "firstTag" );
        accountAdapter = new SurperAdapter <Account>(s,this) {
            @Override
            public int getLayoutId(int viewType) {
                return R.layout.acount_list_itme_layout;
            }

            @Override
            public void convert(final VH holder , Account data , final int position , Context mContext) {
                holder.setText( R.id.title_item,decrypt( inPass,data.getTitle() ) );
                holder.setText( R.id.account_item,decrypt( inPass,data.getAccount() ) );
                holder.itemView.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Dialog dialog=getDialog( v, 2);
                        final TextView pass =dialog.findViewById( R.id.textview_pass );
                        pass.setText( decrypt( inPass,accountAdapter.getmDatas().get( position ).getPassword()) );
                        Button up = dialog.findViewById( R.id. updata);
                        up.setOnClickListener( new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                BottomSheetDialog bottomSheetDialog=getBottomSheetDialog( v );
                                TextView[] t=inBottomSheetDialog( bottomSheetDialog );
                                t[0].setText( "修改" );
                                t[1].setText( decrypt( inPass,accountAdapter.getmDatas().get( position ).getTitle()) );
                                t[2].setText( decrypt( inPass,accountAdapter.getmDatas().get( position ).getAccount()) );
                                bottomSheetDialog.show();
                            }
                        } );
                        Button copy=dialog.findViewById(  R.id.copy );
                        copy.setOnClickListener( new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ClipboardManager clipboard = (ClipboardManager) getSystemService( Context.CLIPBOARD_SERVICE );
                                ClipData clip = ClipData.newPlainText( null , pass.getText().toString().trim() );
                                clipboard.setPrimaryClip( clip );
                                showToast( PassList.this , "密码已复制到粘贴板" , 1000 );
                                dialog.dismiss();
                            }
                        } );
                        dialog.show();
                    }
                } );
                holder.itemView.setOnLongClickListener( new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        return false;
                    }
                } );
            }
        };
    }

    private void initAction() {
        add.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstInTag != null && firstInTag.equals( "yes" )) {
                    View view=getView( R.layout.meg_dialog );
                    popWindowsShow( v,v.getWidth() - 65,0 ,view);
                    firstInTag = null;
                }

                final BottomSheetDialog bottomSheetDialog = getBottomSheetDialog( v );
                TextView[] t = inBottomSheetDialog( bottomSheetDialog );
                behavior( t , bottomSheetDialog , -1 );
            }
        } );

        serch_img.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (search_ed.getVisibility() == View.VISIBLE) {
                    search_ed.setVisibility( View.GONE );
                    title_text.setVisibility( View.VISIBLE );
                } else {
                    search_ed.setVisibility( View.VISIBLE );
                    title_text.setVisibility( View.GONE );
                }
            }
        } );
        
        search_ed.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s , int start , int count , int after) {
            }

            @Override
            public void onTextChanged(CharSequence s , int start , int before , int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                refresh( 1 );
            }
        } );

        backupIcon.setOnClickListener( new View.OnClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onClick(View view) {
                final Dialog dialog = getDialog( view,1 );
                final Button qiut = dialog.findViewById( R.id.quit );
                qiut.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                } );
                final Button backup = dialog.findViewById( R.id.backup );

                final ListView listView = dialog.findViewById( R.id.db_list );
                List list = getBackupFileNameList( getDBFiles() );
                myAdapter = new FileListAdapter( list ,PassList.this );
                listView.setAdapter( myAdapter );

                backup.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String date = getFormatData();

                        int i = DbUtil.DbBackups( date , PassList.this );

                        if (i == 1) {
                            showToast( PassList.this , "已备份至sdcard/PassManage目录下！" , 1000 );
                        } else {
                            showToast( PassList.this , "木有设备存储读写权限！" , 1000 );
                        }
                        myAdapter.setFiles( getBackupFileNameList( getDBFiles() ) );
                        myAdapter.notifyDataSetChanged();
                    }
                } );
                listView.setOnTouchListener( new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v , MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                x = event.getX();
                                Log.e(TAG,""+x);
                                break;
                            case MotionEvent.ACTION_MOVE:
                                x = event.getX();
                                Log.e(TAG,""+x);
                                break;
                        }
                        return false;
                    }
                } );

                listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onItemClick(AdapterView <?> parent , View view , int position , long id) {
                        initPopWindow( view, getDBFiles() , position );

                    }
                } );


                dialog.show();
            }
        } );
    }

    private void initView() {
        backupIcon = findViewById( R.id.backup_list );
        gridView =findViewById( R.id.pass_list );
        title_text = findViewById( R.id.text_pass_list );
        search_ed = findViewById( R.id.search );
        serch_img = findViewById( R.id.search_img );
        add = findViewById( R.id.floatingActionButton );
        
    }

    private List <String> getBackupFileNameList(File[] files) {
        if (files != null) {
            final List <String> listItem = new ArrayList <>();
            for (int i = 0; i < files.length; i++) {
                listItem.add( files[i].getName() );
            }
            return listItem;
        }

        return null;
    }

    private File[] getDBFiles() {
        File file = new File( Environment.getExternalStorageDirectory().getPath() + File.separator + "PassManage" );
        if (!file.exists()) {
            file.mkdirs();
        }

        File[] files = file.listFiles();
        return files;
    }

    private Dialog getDialog(View view,int type) {
        final Dialog dialog = new Dialog( view.getContext() );
        dialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
        dialog.setCancelable( true );
        switch (type){
            case 1:
                dialog.setContentView( R.layout.show_backup_db_dialog );
                break;
            case 2:
                dialog.setContentView( R.layout.pass_show_dialog );
                break;
            case 3:
                dialog.setContentView( R.layout.pass_input_dialog );
                break;
            default:
                break;
        }


        return dialog;
    }

    private void behavior(final TextView[] t , final Dialog bottomSheetDialog , final int posi) {
        t[0].setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Account account = new Account();
                String[] inputMsgT = new String[4];
                inputMsgT[0] = t[1].getText().toString();
                inputMsgT[1] = t[2].getText().toString();
                inputMsgT[2] = t[3].getText().toString();
                inputMsgT[3] = t[4].getText().toString();
                //String key = "null";
                String[] inputMsg = encryptStringArray( inputMsgT , inPass );
                if (!inputMsg[0].equals( "" ) && !inputMsg[1].equals( "" ) && !inputMsg[2].equals( "" ) && !inputMsg[3].equals( "" )) {
                    if (inputMsg[2].equals( inputMsg[3] )) {
                        account.setTitle( inputMsg[0] );
                        account.setAccount( inputMsg[1] );
                        account.setPassword( inputMsg[2] );
                        if (posi < 0) {
                            passDb.addData( account );
                            DbUtil.DbBackups( getFormatData() + "（自动备份）" , PassList.this );
                        } else {
                            passDb.upData( account , accountAdapter.getmDatas().get( posi ).getId() );
                        }
                        //垃圾回收器回收
                        inputMsg = null;
                        bottomSheetDialog.dismiss();
                        refresh( 0 );
                        System.gc();
                    } else {
                        showToast( PassList.this , "输入密码匹配失败！" , 1000 );
                    }
                    //account.setUpTime(passDb.getUpTime(1));
                } else {
                    showToast( PassList.this , "非法输入！" , 700 );
                }
            }
        } );
    }

    private TextView[] inBottomSheetDialog(Dialog bottomSheetDialog) {
        TextView[] t = new TextView[6];
        t[0] = bottomSheetDialog.findViewById( R.id.add_new_message );
        t[1] = bottomSheetDialog.findViewById( R.id.title );
        t[2] = bottomSheetDialog.findViewById( R.id.account );
        t[3] = bottomSheetDialog.findViewById( R.id.password );
        t[4] = bottomSheetDialog.findViewById( R.id.reply );
        t[5] = bottomSheetDialog.findViewById( R.id.secPass );
        return t;
    }

    private BottomSheetDialog getBottomSheetDialog(View view) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog( view.getContext() );
        View sheetView = LayoutInflater.from( view.getContext() ).inflate( R.layout.add_account_dialog , null );
        bottomSheetDialog.setContentView( sheetView );
        bottomSheetDialog.setCancelable( true );
        bottomSheetDialog.show();
        return bottomSheetDialog;
    }

    private void refresh(int something) {
        s = passDb.getAll();
        String temp = search_ed.getText().toString().trim();
        if (something == 0 || temp.equals( "" )) {
            accountAdapter.setmDatas( s );
        } else if (something == 1) {
            LinkedList <Account> r = new LinkedList <>();
            for (int i = 0; i < s.size(); i++) {
                boolean title = decrypt( inPass , s.get( i ).getTitle() ).contains( temp );
                boolean pass = decrypt( inPass , s.get( i ).getAccount() ).contains( temp );
                if (title || pass) {
                    r.add( s.get( i ) );
                }
            }
            accountAdapter.setmDatas( r );
        }
        GridLayoutManager grid=new GridLayoutManager(this,2);
        gridView.setLayoutManager(grid);
        gridView.setAdapter( accountAdapter );

    }

    private String[] encryptStringArray(String[] strings , String key) {
        //key = getMD5( key , 16 );
        for (int i = 0; i < strings.length; i++) {
            if (strings[i] != null && !strings[i].equals( "" )) {
                strings[i] = encrypt( key , strings[i] );
            }
        }
        return strings;
    }

    /**
     * 请求授权
     */
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission( this ,
                Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED) { //表示未授权时
            //进行授权
            ActivityCompat.requestPermissions( this , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE} , 1 );
        }
    }

    /**
     * 权限申请返回结果
     *
     * @param requestCode  请求码
     * @param permissions  权限数组
     * @param grantResults 申请结果数组，里面都是int类型的数
     */
    @Override
    public void onRequestPermissionsResult(int requestCode , @NonNull String[] permissions , @NonNull int[] grantResults) {
        super.onRequestPermissionsResult( requestCode , permissions , grantResults );
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //同意权限申请
                } else { //拒绝权限申请
                    Toast.makeText( this , "权限被拒绝" , Toast.LENGTH_SHORT ).show();
                }
                break;
            default:
                break;
        }
    }

    private PopupWindow popWindowsShow(View pareV, int xoff, int yoff,View view) {
        //1.构造一个PopupWindow，参数依次是加载的View，宽高
        final PopupWindow popWindow = new PopupWindow( view ,
                ViewGroup.LayoutParams.WRAP_CONTENT , ViewGroup.LayoutParams.WRAP_CONTENT , true );
        popWindow.setAnimationStyle( R.anim.anim_pop );  //设置加载动画
        popWindow.setTouchable( true );
        popWindow.setTouchInterceptor( new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v , MotionEvent event) {
                return false;
            }
        } );
        popWindow.setBackgroundDrawable( new ColorDrawable( 0x49463e ) );    //要为popWindow设置一个背景才有效
        //设置popupWindow显示的位置，参数依次是参照View，x轴的偏移量，y轴的偏移量
        popWindow.showAsDropDown( pareV , xoff , yoff );
        return popWindow;
    }

    private View getView(int resid){
        View view =LayoutInflater.from( this ).inflate( resid, null , false );
        return view;
    }

    @SuppressLint("ResourceType")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initPopWindow(View pareV , final File[] files , final int position) {
        View view=getView( R.layout.item_popup );
        //1.构造一个PopupWindow，参数依次是加载的View，宽高
        final PopupWindow popWindow = popWindowsShow( pareV , (int) x-35 , -pareV.getHeight() / 2 + 20 ,view);
        //设置popupWindow里的按钮的事件
        TextView btn_xixi = view.findViewById( R.id.delete_db );
        TextView btn_hehe = view.findViewById( R.id.rest_db );
        btn_xixi.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = FileUtil.deleteFile( files[position].getName() );
                String s = i==0 ? "删除成功！":"删除失败！";
                showToast( PassList.this , s , 1000 );
                myAdapter.setFiles( getBackupFileNameList( getDBFiles() ) );
                myAdapter.notifyDataSetChanged();
                popWindow.dismiss();
            }
        } );

        btn_hehe.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DbUtil.restore(files[position].getName(),PassList.this);
                int i = DbUtil.restore( files[position].getName() , PassList.this );
                String s = i==1 ? "还原成功！":"还原失败！";
                if (i == 1) refresh( 0 );
                showToast( PassList.this , s , 1000 );
                myAdapter.setFiles( getBackupFileNameList( getDBFiles() ) );
                myAdapter.notifyDataSetChanged();
                popWindow.dismiss();
            }
        } );
    }

    private static String getFormatData() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd-HH-mm-ss" );//设置日期格式
        return df.format( new Date() );
    }


}
