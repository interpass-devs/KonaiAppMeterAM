package com.konai.appmeter.driver_am.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.konai.appmeter.driver_am.R;
import com.konai.appmeter.driver_am.setting.AMBlestruct;

import java.util.ArrayList;

public class MenuAdapter extends RecyclerView.Adapter {

    Context mContext;
    ArrayList<String> items;
    String whichMenu;


    //커스텀 터치리스너 interface
    public interface onItemTouchListener{
        void onItemTouch(View v, int pos, MotionEvent event);
    }

    private onItemTouchListener mTouchListener = null;

    public void setmTouchListener(onItemTouchListener mtouchListener) {
        this.mTouchListener = mtouchListener;
    }

    public void menuTitleTouch(String which_menu) {
        this.whichMenu = which_menu;
    }

    //커스텀 클릭리스너 interface
    public interface onItemClickListener{
        void onItemClick(View v, int pos, String numType);
    }

    private onItemClickListener mListener = null;

    public void setmClickListener(onItemClickListener mListener) {
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
            vh.menu_text.setTextSize(4.0f * 8);
            vh.menu_text.setGravity(Gravity.CENTER);
        }else {
            try {
                if (vh.menu_text.getText().toString().contains("기본인쇄") && vh.menu_text.getText().toString().contains("상세인쇄")) {

                    vh.menu_text.setVisibility(View.GONE);

                    vh.print_btn_layout.setVisibility(View.VISIBLE);
                    vh.print_btn_1.setText(vh.menu_text.getText().toString().substring(0,7));
                    vh.print_btn_2.setText(vh.menu_text.getText().toString().substring(9,16));

                }else {
                    vh.print_btn_layout.setVisibility(View.GONE);
                }
            }catch (Exception e) {

            }

        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }



    class VH extends RecyclerView.ViewHolder {
        TextView menu_text, print_btn_1, print_btn_2;
        LinearLayout print_btn_layout;

        public VH(@NonNull View itemView) {
            super(itemView);
            menu_text = itemView.findViewById(R.id.menu_text);
            print_btn_layout = itemView.findViewById(R.id.print_btn_layout);
            print_btn_1 = itemView.findViewById(R.id.print_btn_1);
            print_btn_2 = itemView.findViewById(R.id.print_btn_2);


            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    int post = getLayoutPosition();

                    //제품정보 터치 못하게
                    if (whichMenu.contains("제품정보") | whichMenu.contains("출근후집계") | whichMenu.contains("법인집계")) {
                        //do not touch title & contents
                    }else {
                        if (post != RecyclerView.NO_POSITION) {
                            if (post == 0) {
                                //do nothing
                            }else {
                                if (mTouchListener != null) {
                                    mTouchListener.onItemTouch(v, post, event);
                                }
                            }
                        }
                    }
                    return false;
                }
            });




            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int position = getLayoutPosition();

                    Log.d("whichMenu_1", whichMenu+"");

                    //제품정보 클릭 못하게
                    if (whichMenu.contains("제품정보")) {
                        //do nothing
                    }
                    else  if ( whichMenu.contains("오늘집계")
                            || whichMenu.contains("전일집계")
                            || whichMenu.contains("기간집계")
                            || whichMenu.contains("출근후집계")
                            || whichMenu.contains("법인집계")
                            || whichMenu.contains("제목없음")) {

                        if (print_btn_layout.getVisibility() == View.VISIBLE) {
                            print_btn_1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mListener != null) {
                                        mListener.onItemClick(v, position, "1");
                                    }
                                }
                            });
                            print_btn_2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mListener != null) {
                                        mListener.onItemClick(v, position, "2");
                                    }
                                }
                            });
                        }else {
                            //do nothing
                        }
                    }
                    else {
                        if (position != RecyclerView.NO_POSITION) {
                            if (position == 0) {
                                //do nothing
                                //제목은 클릭 못하게하기
                            }else {
                                Log.d("position>", position+"");
                                if (mListener != null) {
                                    mListener.onItemClick(v, position, "0");
                                }
                            }
                        }
                    }
                }
            });





        }
    }


}
