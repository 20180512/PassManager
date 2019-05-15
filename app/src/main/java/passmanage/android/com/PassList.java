package passmanage.android.com;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import passmanage.android.com.Util.DbUtil;
import passmanage.android.com.Util.FileUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static passmanage.android.com.Util.Aes.*;
import static passmanage.android.com.Util.DbUtil.StringFilter;
import static passmanage.android.com.Util.MyToast.showToast;

public class PassList extends AppCompatActivity{
    private GridView gridView;
    private TextView title_text;
    private EditText search_ed;
    private ImageView serch_img;
    private Context mContext;
    private FileListAdapter myAdapter;
    private String inPass;
    float x;
    private LinkedList<Account> s;
    AccountAdapter accountAdapter;
    private String firstInTag;
    Database passDb = new Database( this , "passEngine.db" , null , 1,"Pass" );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_pass_list );
        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );
        requestPermission();
        mContext=this;
        inPass =getIntent().getStringExtra( "p" );
        firstInTag=getIntent().getStringExtra( "firstTag" );
        title_text=findViewById( R.id.text_pass_list );
        search_ed =findViewById( R.id.search );
        serch_img=findViewById( R.id.search_img );
        serch_img.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(search_ed.getVisibility()==View.VISIBLE){
                    search_ed.setVisibility( View.GONE );
                    title_text.setVisibility( View.VISIBLE );
                }else{
                    search_ed.setVisibility( View.VISIBLE );
                    title_text.setVisibility( View.GONE);
                }
            }
        } );
        search_ed.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s , int start , int count , int after) { }

            @Override
            public void onTextChanged(CharSequence s , int start , int before , int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                bindView( 1 );
            }
        } );
        gridView = findViewById( R.id.pass_list );
        ImageView add_image = findViewById( R.id.add_view );
        add_image.setOnClickListener( new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if(firstInTag!=null&&firstInTag.equals( "yes" )){
                    messageDialog(view);
                    firstInTag=null;
                }

                final BottomSheetDialog bottomSheetDialog = getBottomSheetDialog( view );
                TextView[] t = inBottomSheetDialog( bottomSheetDialog );
                for (int i =0;i<t.length;i++){
                    t[i].setTextColor( Color.BLACK );
                    t[i].setHintTextColor( getColor( R.color.hint_color ) );
                }
                behavior( t , bottomSheetDialog , -1 );
            }
        } );
        add_image.setOnLongClickListener( new View.OnLongClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onLongClick(View view) {
                final Dialog dialog = getDialog( view );
                final EditText editText = dialog.findViewById( R.id.pass_deco );
                editText.setVisibility( View.GONE );
                final TextView dialogTitle =dialog.findViewById( R.id.dialog_title );
                dialogTitle.setText("备份还原数据");
                final Button update = dialog.findViewById( R.id.quit );
                update.setText( "取消" );
                update.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                } );
                final Button enter = dialog.findViewById( R.id.back );
                enter.setText( "备份" );

                final ListView listView = dialog.findViewById(R.id.db_list);
                List list=getList(getDBFiles());
                myAdapter = new FileListAdapter( list,mContext );
                listView.setVisibility( View.VISIBLE );
                listView.setAdapter(myAdapter);

                enter.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String date = getFormatData();

                        int i = DbUtil.DbBackups( date ,PassList.this);

                        if(i==1){
                            showToast( PassList.this,"已备份至sdcard/PassManage目录下！",1000 );
                        }else {
                            showToast( PassList.this, "木有设备存储读写权限！", 1000 );
                        }
                        myAdapter.setFiles( getList(getDBFiles()) );
                        myAdapter.notifyDataSetChanged();
                    }
                } );

                listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        initPopWindow(view,getDBFiles(),position);
                    }
                } );

                listView.setOnTouchListener( new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v , MotionEvent event) {
                        switch (event.getAction()){
                            case MotionEvent.ACTION_DOWN:
                                x=event.getX();
                                //System.out.println( x );
                                break;
                            case MotionEvent.ACTION_MOVE:
                                x=event.getX();
                                //System.out.println( x );
                                break;
                        }
                        return false;
                    }
                } );
                dialog.show();
                return true;
            }
        } );


        gridView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView <?> parent , View view , final int position , long id) {
                final Dialog dialog = getDialog( view );
                final EditText editText = dialog.findViewById( R.id.pass_deco );
                final Button update = dialog.findViewById( R.id.quit );
                final Button enter = dialog.findViewById( R.id.back );
                final int posi = position;
                String cryptoGraph = accountAdapter.getAccountList().get( posi ).getPassword().trim();
                //String s = editText.getText().toString();
                TextView textView = dialog.findViewById( R.id.textview_pass );
                final String clearPass = decrypt( inPass , cryptoGraph );
                textView.setVisibility( View.VISIBLE );
                textView.setText( clearPass );
                editText.setVisibility( View.GONE );
                enter.setText( " ⬛ 复制" );
                enter.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService( Context.CLIPBOARD_SERVICE );
                        ClipData clip = ClipData.newPlainText( null , clearPass );
                        clipboard.setPrimaryClip( clip );
                        showToast( PassList.this , "密码已复制到粘贴板" , 1000 );
                    }
                } );

                update.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        final BottomSheetDialog bottomSheetDialog = getBottomSheetDialog( v );
                        final TextView[] t = inBottomSheetDialog( bottomSheetDialog );
                        t[0].setText( " ⬛ 修改" );
                        t[1].setText( decrypt( inPass , accountAdapter.getAccountList().get( position ).getTitle() ) );
                        t[2].setText( decrypt( inPass , accountAdapter.getAccountList().get( position ).getAccount() ));
                        behavior( t , bottomSheetDialog , posi );
                    }
                } );
                dialog.show();
            }
        } );

        gridView.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView <?> parent , View view , int position , long id) {
                final Dialog dialog = getDialog( view );
                final EditText editText = dialog.findViewById( R.id.pass_deco );
                final Button delete = dialog.findViewById( R.id.quit );
                final Button deleteAll = dialog.findViewById( R.id.back );
                final int posi = position;
                delete.setText( " ⬛ 删除" );
                deleteAll.setText( " ⬛ 删除全部" );
                delete.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        passDb.deleteData( accountAdapter.getAccountList().get( posi ).getId() );
                        showToast( PassList.this , "已删除!" , 1000 );
                        bindView(0);
                        dialog.dismiss();
                    }
                } );
                deleteAll.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //String cryptoGraph = accountAdapter.getAccountList().get( posi ).getPassword().trim();
                        String s = getMD5( editText.getText().toString(),16);
                        //final String clearPass = decrypt( getMD5( s , 16 ) , cryptoGraph );
                        if (s.equals(inPass )) {
                            passDb.deleteAll();
                            showToast( PassList.this , "已删除全部！" , 1000 );
                            dialog.dismiss();
                            bindView(0);
                        } else {
                            showToast( PassList.this , "密码错误！" , 1000 );
                        }
                    }
                } );
                dialog.show();
                return true;
            }
        } );
        bindView(0);
    }

    private List <String> getList(File[] files) {
        if(files!=null){
            final List<String> listItem = new ArrayList<>();
            for(int i =0;i<files.length;i++){
                listItem.add( files[i].getName() );
            }
            return listItem;
        }

        return null;
    }

    private File[] getDBFiles() {
        File file  = new File("/sdcard/PassManage");
        if (!file.exists()) {
            file.mkdirs();
        }

        File[] files = file.listFiles();
        return files;
    }

    private Dialog getDialog(View view) {
        final Dialog dialog = new Dialog( view.getContext() );
        dialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
        dialog.setCancelable( true );
        dialog.setContentView( R.layout.pass_input_auth_dialog );
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
                            DbUtil.DbBackups( getFormatData()+"（自动备份）" ,PassList.this);
                        } else {
                            passDb.upData( account , accountAdapter.getAccountList().get( posi ).getId() );
                        }
                        //垃圾回收器回收
                        inputMsg=null;
                        bottomSheetDialog.dismiss();
                        bindView(0);
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

    private void bindView(int something) {
        s=passDb.getAll();
        String temp=search_ed.getText().toString().trim();
        if(something==0||temp.equals( "" )){

            accountAdapter = new AccountAdapter( s , PassList.this,inPass );

        }else if(something==1){
            LinkedList<Account> r=new LinkedList <>(  );
            for(int i=0;i<s.size();i++){
                boolean title=decrypt( inPass , s.get( i ).getTitle() ).contains( temp );
                boolean pass=decrypt( inPass , s.get( i ).getAccount() ).contains( temp );
                if(title||pass){
                    r.add( s.get( i ) );
                }
            }
            accountAdapter = new AccountAdapter( r, PassList.this,inPass );
        }
        gridView.setAdapter( accountAdapter );
        gridView.setSelection( accountAdapter.getCount()-1 );
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
    private void requestPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){ //表示未授权时
            //进行授权
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
    }

    /**
     * 权限申请返回结果
     * @param requestCode 请求码
     * @param permissions 权限数组
     * @param grantResults  申请结果数组，里面都是int类型的数
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //同意权限申请
                }else { //拒绝权限申请
                    Toast.makeText(this,"权限被拒绝",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void messageDialog(View pareV){
        View view = LayoutInflater.from( mContext ).inflate( R.layout.meg_dialog , null , false );
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
        popWindow.showAsDropDown( pareV , pareV.getWidth()-65 , 0 );
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initPopWindow(View pareV , final File[] files , final int position) {

        View view = LayoutInflater.from( mContext ).inflate( R.layout.item_popup , null , false );
        TextView btn_xixi = view.findViewById( R.id.delete_db );
        TextView btn_hehe =view.findViewById(R.id.rest_db);
        //1.构造一个PopupWindow，参数依次是加载的View，宽高
        final PopupWindow popWindow = new PopupWindow( view ,
                ViewGroup.LayoutParams.WRAP_CONTENT , ViewGroup.LayoutParams.WRAP_CONTENT , true );

        popWindow.setAnimationStyle( R.anim.anim_pop );  //设置加载动画

        //这些为了点击非PopupWindow区域，PopupWindow会消失的，如果没有下面的
        //代码的话，你会发现，当你把PopupWindow显示出来了，无论你按多少次后退键
        //PopupWindow并不会关闭，而且退不出程序，加上下述代码可以解决这个问题
        popWindow.setTouchable( true );
        popWindow.setTouchInterceptor( new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v , MotionEvent event) {
                /*switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        x=event.getX();
                        System.out.println( x );
                        break;
                    case MotionEvent.ACTION_MOVE:
                        x=event.getX();
                        System.out.println( x );
                        break;
                }*/
                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        } );
        popWindow.setBackgroundDrawable( new ColorDrawable( 0x00000000 ) );    //要为popWindow设置一个背景才有效


        //设置popupWindow显示的位置，参数依次是参照View，x轴的偏移量，y轴的偏移量
        popWindow.showAsDropDown( pareV , (int)(x-pareV.getX()-45) , -pareV.getHeight()/2+20 );

        //设置popupWindow里的按钮的事件
        btn_xixi.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i=FileUtil.deleteFile( files[position].getName() );
                if(i==0){
                    showToast( PassList.this,"删除成功！",1000 );
                }else if(i==1){
                    showToast( PassList.this,"删除失败！",1000 );
                }
                myAdapter.setFiles( getList(getDBFiles()) );
                myAdapter.notifyDataSetChanged();
                popWindow.dismiss();
            }
        } );

        btn_hehe.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DbUtil.restore(files[position].getName(),PassList.this);
                int i =DbUtil.restore(files[position].getName(),PassList.this);
                if(i == 1){
                    bindView(0);
                    showToast(PassList.this,"还原成功！",1000 );
                }else {
                    showToast( PassList.this, "还原失败！", 1000 );
                }
                myAdapter.setFiles( getList(getDBFiles()) );
                myAdapter.notifyDataSetChanged();
                popWindow.dismiss();
            }
        } );
    }

    private static String getFormatData(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");//设置日期格式
        String date = df.format( new Date() );
        return date;
    }




}
