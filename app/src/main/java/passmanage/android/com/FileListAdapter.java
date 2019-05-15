package passmanage.android.com;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class FileListAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> files;

    public FileListAdapter(List files, Context mContext){
        this.files =files;
        this.mContext = mContext;
    }
    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position , View convertView , ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.db_file_item_layout,parent,false);
            holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.file_name);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.title.setText( files.get(position));

        return convertView;
    }

    private class ViewHolder{
        TextView title;

    }

    List <String> getFiles() {
        return files;
    }

    public void setFiles(List <String> files) {
        this.files = files;
    }
}
