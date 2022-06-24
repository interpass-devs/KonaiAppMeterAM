package com.konai.appmeter.driver_am.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.konai.appmeter.driver_am.R;

import java.util.ArrayList;

public class MenuAdapter extends RecyclerView.Adapter {

    Context mContext;
    ArrayList<String> items;

    //커스텀 터치리스너 interface
    public interface onItemTouchListener{
        void onItemTouch(View v, int pos, MotionEvent event);
    }

    private onItemTouchListener mTouchListener = null;

    public void setmTouchListener(onItemTouchListener mtouchListener) {
        this.mTouchListener = mtouchListener;
    }


    //커스텀 클릭리스너 interface
    public interface onItemClickListener{
        void onItemClick(View v, int pos);
    }

    private onItemClickListener mListener = null;

    public void setmListener(onItemClickListener mListener) {
        this.mListener = mListener;
    }



    public MenuAdapter() {

    }

    public MenuAdapter(Context mContext, ArrayList<String> items) {
        this.mContext = mContext;
        this.items = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.layout_menu, parent, false);

        VH holder = new VH(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        VH vh = (VH) holder;
        vh.menu_text.setText(items.get(position));

        if (position == 0) {
//            vh.menu_text.setTypeface(vh.menu_text.getTypeface(), Typeface.BOLD);
            vh.menu_text.setTextSize(4.0f * 8);
        }else {
            //do nothing
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class VH extends RecyclerView.ViewHolder {
        TextView menu_text;

        public VH(@NonNull View itemView) {
            super(itemView);
            menu_text = itemView.findViewById(R.id.menu_text);



            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int post = getLayoutPosition();
                    if (post != RecyclerView.NO_POSITION) {
                        if (mTouchListener != null) {
                            mTouchListener.onItemTouch(v, post, event);
                        }
                    }
                    return false;
                }
            });


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getLayoutPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (mListener != null) {
                            mListener.onItemClick(v, position);
                        }
                    }
                }
            });
        }
    }


}
