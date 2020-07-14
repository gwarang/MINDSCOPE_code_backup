package kr.ac.inha.mindscope;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ViewHolderPage extends RecyclerView.ViewHolder{

    private TextView fsv_title;
    private RelativeLayout rl_layout;
    private TextView fsv_contents;


    DataPage data;

    ViewHolderPage(View itemView){
        super(itemView);
        fsv_title = itemView.findViewById(R.id.fsv_title);
        rl_layout = itemView.findViewById(R.id.rl_layout);
        fsv_contents = itemView.findViewById(R.id.fsv_contents);
    }

    public void onBind(DataPage data){
        this.data = data;

        fsv_title.setText(data.getTitle());
        fsv_title.setTextColor(data.getColor());
        //        rl_layout.setBackgroundResource(data.getColor());
        fsv_contents.setText(data.getContents());
    }
}

