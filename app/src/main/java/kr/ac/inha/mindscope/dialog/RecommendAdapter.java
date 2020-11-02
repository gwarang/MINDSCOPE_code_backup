package kr.ac.inha.mindscope.dialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import java.util.ArrayList;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import kr.ac.inha.mindscope.R;


public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.ViewHolder>{

    private ArrayList<String> mData = null;
    Context context;
    SharedPreferences interventionPrefs;
    SharedPreferences.Editor interventionPrefsEditor;
    private int lastSelectedPosition = -1;


    public class ViewHolder extends RecyclerView.ViewHolder {
        RadioButton radioButton;

        ViewHolder(View itemView){
            super(itemView);
            interventionPrefs = context.getSharedPreferences("intervention", Context.MODE_PRIVATE);
            interventionPrefsEditor = interventionPrefs.edit();
            radioButton = itemView.findViewById(R.id.recommend_item);
            radioButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    lastSelectedPosition = getAbsoluteAdapterPosition();
                    Log.d("radio", "abs pos : " + getAbsoluteAdapterPosition());
                    Log.d("radio", "binding pos : " + getBindingAdapterPosition());
                    interventionPrefsEditor.putInt("intervention_radio_pos", 0);
                    interventionPrefsEditor.apply();
                    Handler handler = new Handler();
                    final Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    };
                    handler.post(r);
                }
            });
        }
    }

    RecommendAdapter(ArrayList<String> list){
        mData = list;
    }

    @NonNull
    @Override
    public RecommendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.recommend_item, parent, false);
        RecommendAdapter.ViewHolder vh = new RecommendAdapter.ViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String text = mData.get(position).replace("_", " ");
        holder.radioButton.setText(text);
        Log.d("radio", "last pos : " + lastSelectedPosition);
        Log.d("radio", "pos : " + position);
        holder.radioButton.setChecked(position == lastSelectedPosition);

//        notifyItemChanged(0, mData.size());
        if(lastSelectedPosition >= 0 && lastSelectedPosition < mData.size()){
            interventionPrefsEditor.putString("intervention_radio_contents", mData.get(lastSelectedPosition).replace("_", " "));
            interventionPrefsEditor.apply();
            Log.d("radio", "lastSelected pos + con" + lastSelectedPosition + " " + mData.get(lastSelectedPosition));
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
