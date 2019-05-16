package com.android.passmanager;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedList;


import static com.android.passmanager.Util.Aes.decrypt;


public class AccountAdapter extends BaseAdapter {
    private String inPass;
    private Context mContext;
    private LayoutInflater layoutInflater;
    private LinkedList<Account> accountList;

    public AccountAdapter(){

    }

    public AccountAdapter(LinkedList accountList,Context mContext,String inPass){
        this.accountList=accountList;
        this.mContext = mContext;
        this.inPass =inPass;
        this.layoutInflater=LayoutInflater.from( mContext );
    }

    @Override
    public int getCount() {
        return accountList.size();
    }

    @Override
    public Object getItem(int position) {
        Object o=null;
        try{
            o=accountList.get( position );
        }catch (Exception e){

        }
        return o;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(int position, View convertView , ViewGroup parent) {
        ViewHolder holder=null;
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.acount_list_itme_layout,parent,false);
            holder = new ViewHolder();
            holder.account =convertView.findViewById( R.id.account_item );
            holder.title = convertView.findViewById(R.id.title_item);
            holder.password=convertView.findViewById( R.id.pass_item );
            holder.linearLayout=convertView.findViewById( R.id.item_view );
            /*holder.add=convertView.findViewById( R.id.ic_add );*/
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        /*if(position<accountList.size()){

        }else{
            holder.add.setImageResource( R.drawable.ic_add_black_24dp );
            holder.add.setBackground( mContext.getDrawable( R.drawable.ic_add_95dp ) );
            //convertView.setBackground( mContext.getDrawable( R.drawable.ic_add_95dp ) );

            holder.password.setVisibility( View.GONE );
            holder.account.setVisibility( View.GONE );
            holder.title.setVisibility( View.GONE );
        }*/
        holder.title.setText(   decrypt( inPass , accountList.get( position ).getTitle() ) );
        holder.account.setText( decrypt( inPass ,accountList.get( position ).getAccount() ));

        return convertView;
    }


    private class ViewHolder{
        LinearLayout linearLayout;
        TextView title;
        TextView account;
        TextView password;
        /*ImageView add;*/

    }

    public LinkedList<Account> getAccountList() {
        return accountList;
    }

    public void setAccountList(LinkedList <Account> accountList) {
        this.accountList = accountList;
    }
}
