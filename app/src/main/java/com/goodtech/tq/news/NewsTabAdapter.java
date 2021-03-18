package com.goodtech.tq.news;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.goodtech.tq.R;
import com.goodtech.tq.fragment.viewholder.NativeExpressAD2Holder;
import com.goodtech.tq.models.NewsDataBean;
import com.qq.e.ads.nativ.NativeExpressADView;

import java.util.List;

public class NewsTabAdapter extends BaseAdapter {
    private final List<Object> list;
    private final Context context;
    private final int IMAGE_01 =0;
    private final int IMAGE_02 = 1;
    private final int IMAGE_03 = 2;
    private final int TYPE_AD = 3;
    public NewsTabAdapter(Context context, List<Object> list){
        this.context = context;
        this.list = list;
    }

    // 把返回的NativeExpressADView添加到数据集里面去
    public void addADViewToPosition(int position, NativeExpressADView adView) {
        if (position >= 0 && position < list.size() && adView != null) {
            list.add(position, adView);
        }
    }
    // 移除NativeExpressADView的时候是一条一条移除的
    public void removeADView(int position, NativeExpressADView adView) {
        list.remove(position);
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    private static final String TAG = "NewsTabAdapter";
    @Override
    public int getItemViewType(int position) {

        Object data = list.get(position);
        Log.e(TAG, "getItemViewType: " + data);
        if (data instanceof NativeExpressADView) {
            return TYPE_AD;
        }

        if (((NewsDataBean)list.get(position)).getThumbnail_pic_s() != null &&
                ((NewsDataBean)list.get(position)).getThumbnail_pic_s02() !=null &&
                ((NewsDataBean)list.get(position)).getThumbnail_pic_s03() !=null){
            return IMAGE_03;
        }else if (((NewsDataBean)list.get(position)).getThumbnail_pic_s() !=null &&
                ((NewsDataBean)list.get(position)).getThumbnail_pic_s02() !=null){
            return IMAGE_02;
        }
        return IMAGE_01;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        switch (getItemViewType(position)) {
            case TYPE_AD: {
                NativeExpressAD2Holder holder;
                if (convertView == null){
                    convertView =View.inflate(context, NativeExpressAD2Holder.getResource(),null);
                    holder =new NativeExpressAD2Holder(convertView);
                    convertView.setTag(holder);
                }else {
                    holder = (NativeExpressAD2Holder) convertView.getTag();
                }

                holder.setAdView((NativeExpressADView) list.get(position));
            }
            break;

            case IMAGE_01: {
                Image01_ViewHolder holder;
                if (convertView == null){
                    convertView =View.inflate(context, R.layout.item_layout01,null);
                    holder =new Image01_ViewHolder();

                    //查找控件
                    holder.author_name = convertView.findViewById(R.id.author_name);
                    holder.title = convertView.findViewById(R.id.title);
                    holder.image = convertView.findViewById(R.id.image);
                    convertView.setTag(holder);
                }else {
                    holder = (Image01_ViewHolder) convertView.getTag();
                }

                //获取数据重新赋值
                holder.title.setText(((NewsDataBean)list.get(position)).getTitle());
                holder.author_name.setText(((NewsDataBean)list.get(position)).getAuthor_name());
                RequestOptions options = new RequestOptions()
                        .placeholder(R.mipmap.ic_launcher)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .error(R.mipmap.ic_launcher);
                Glide.with(context).load(((NewsDataBean)list.get(position)).getThumbnail_pic_s()).apply(options).into(holder.image);
            }
            break;

            case IMAGE_02: {
                Image02_ViewHolder holder;
                if (convertView == null){
                    convertView =View.inflate(context, R.layout.item_layout02,null);
                    holder =new Image02_ViewHolder();

                    //查找控件
                    holder.image002 = convertView.findViewById(R.id.image002);
                    holder.image001 = convertView.findViewById(R.id.image001);
                    holder.title = convertView.findViewById(R.id.title);
                    holder.author_name = convertView.findViewById(R.id.author_name);
                    convertView.setTag(holder);
                }else {
                    holder = (Image02_ViewHolder) convertView.getTag();
                }

                //获取数据重新赋值
                holder.title.setText(((NewsDataBean)list.get(position)).getTitle());
                holder.author_name.setText(((NewsDataBean)list.get(position)).getAuthor_name());
                RequestOptions options = new RequestOptions()
                        .placeholder(R.mipmap.ic_launcher)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .error(R.mipmap.ic_launcher);
                Glide.with(context).load(((NewsDataBean)list.get(position)).getThumbnail_pic_s()).apply(options).into(holder.image001);
                Glide.with(context).load(((NewsDataBean)list.get(position)).getThumbnail_pic_s02()).apply(options).into(holder.image002);
            }
            break;

            default: {
                Image03_ViewHolder holder;
                if (convertView == null) {
                    convertView = View.inflate(context, R.layout.item_layout03, null);
                    holder = new Image03_ViewHolder();

                    //查找控件
                    holder.image01 = convertView.findViewById(R.id.image01);
                    holder.image02 = convertView.findViewById(R.id.image02);
                    holder.image03 = convertView.findViewById(R.id.image03);
                    holder.title = convertView.findViewById(R.id.title);
                    holder.author_name = convertView.findViewById(R.id.author_name);
                    convertView.setTag(holder);
                } else {
                    holder = (Image03_ViewHolder) convertView.getTag();
                }

                //获取数据重新赋值
                holder.title.setText(((NewsDataBean)list.get(position)).getTitle());
                holder.author_name.setText(((NewsDataBean)list.get(position)).getAuthor_name());
                RequestOptions options = new RequestOptions()
                        .placeholder(R.mipmap.ic_launcher)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .error(R.mipmap.ic_launcher);
                Glide.with(context).load(((NewsDataBean)list.get(position)).getThumbnail_pic_s()).apply(options).into(holder.image01);
                Glide.with(context).load(((NewsDataBean)list.get(position)).getThumbnail_pic_s02()).apply(options).into(holder.image02);
                Glide.with(context).load(((NewsDataBean)list.get(position)).getThumbnail_pic_s03()).apply(options).into(holder.image03);
            }
                break;

        }
        return convertView;
    }

    static  class  Image01_ViewHolder{
        TextView title,author_name;
        ImageView image;
    }
    static  class  Image02_ViewHolder{
        TextView title,author_name;
        ImageView image001,image002;
    }
    static  class  Image03_ViewHolder{
        TextView title,author_name;
        ImageView image01,image02,image03;
    }
}