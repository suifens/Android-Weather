package com.goodtech.tq.news;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.goodtech.tq.R;

import java.util.List;

public class NewsTabAdapter extends BaseAdapter {
    private final List<NewsBean.ResultBean.DataBean> list;
    private final Context context;
    private final int IMAGE_01 =0;
    private final int IMAGE_02 = 1;
    private final int IMAGE_03 = 2;
    public NewsTabAdapter(Context context, List<NewsBean.ResultBean.DataBean> list){
        this.context = context;
        this.list = list;
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
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).getThumbnail_pic_s() != null &&
                list.get(position).getThumbnail_pic_s02() !=null &&
                list.get(position).getThumbnail_pic_s03() !=null){
            return IMAGE_03;
        }else if (list.get(position).getThumbnail_pic_s() !=null &&
                list.get(position).getThumbnail_pic_s02() !=null){
            return IMAGE_02;
        }
        return IMAGE_01;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == IMAGE_01){
            Image01_ViewHolder holder;
            if (convertView == null){
                convertView =View.inflate(context, R.layout.item_layout01,null);
                holder =new Image01_ViewHolder();

                //查找控件
                holder.author_name = (TextView) convertView.findViewById(R.id.author_name);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.image = (ImageView) convertView.findViewById(R.id.image);
                convertView.setTag(holder);
            }else {
                holder = (Image01_ViewHolder) convertView.getTag();
            }

            //获取数据重新赋值
            holder.title.setText(list.get(position).getTitle());
            holder.author_name.setText(list.get(position).getAuthor_name());
            RequestOptions options = new RequestOptions()
                    .placeholder(R.mipmap.ic_launcher)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .error(R.mipmap.ic_launcher);
            Glide.with(context).load(list.get(position).getThumbnail_pic_s()).apply(options).into(holder.image);
        }else if (getItemViewType(position) == IMAGE_02){
            Image02_ViewHolder holder;
            if (convertView == null){
                convertView =View.inflate(context, R.layout.item_layout02,null);
                holder =new Image02_ViewHolder();

                //查找控件
                holder.image002 = (ImageView) convertView.findViewById(R.id.image002);
                holder.image001 = (ImageView) convertView.findViewById(R.id.image001);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
            }else {
                holder = (Image02_ViewHolder) convertView.getTag();
            }

            //获取数据重新赋值
            holder.title.setText(list.get(position).getTitle());
            RequestOptions options = new RequestOptions()
                    .placeholder(R.mipmap.ic_launcher)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .error(R.mipmap.ic_launcher);
            Glide.with(context).load(list.get(position).getThumbnail_pic_s()).apply(options).into(holder.image001);
            Glide.with(context).load(list.get(position).getThumbnail_pic_s02()).apply(options).into(holder.image002);
            //Glide.with(context).load(list.get(position).getThumbnail_pic_s()).apply(options).into(holder.image001);
           // Glide.with(context).load(list.get(position).getThumbnail_pic_s02()).apply(options).into(holder.image002);
        } else {
            Image03_ViewHolder holder;
            if (convertView == null){
                convertView =View.inflate(context, R.layout.item_layout03,null);
                holder =new Image03_ViewHolder();

                //查找控件
                holder.image01 = (ImageView) convertView.findViewById(R.id.image01);
                holder.image02 = (ImageView) convertView.findViewById(R.id.image02);
                holder.image03 = (ImageView) convertView.findViewById(R.id.image03);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
            }else {
                holder = (Image03_ViewHolder) convertView.getTag();
            }

            //获取数据重新赋值
            holder.title.setText(list.get(position).getTitle());
            RequestOptions options = new RequestOptions()
                    .placeholder(R.mipmap.ic_launcher)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .error(R.mipmap.ic_launcher);
//            GlideUtil.load(context,list.get(position).getThumbnail_pic_s(),holder.image01,options);
//            GlideUtil.load(context,list.get(position).getThumbnail_pic_s02(),holder.image02,options);
//            GlideUtil.load(context,list.get(position).getThumbnail_pic_s03(),holder.image03,options);
            Glide.with(context).load(list.get(position).getThumbnail_pic_s()).apply(options).into(holder.image01);
            Glide.with(context).load(list.get(position).getThumbnail_pic_s02()).apply(options).into(holder.image02);
            Glide.with(context).load(list.get(position).getThumbnail_pic_s03()).apply(options).into(holder.image03);
        }
        return convertView;
    }

    static  class  Image01_ViewHolder{
        TextView title,author_name;
        ImageView image;
    }
    static  class  Image02_ViewHolder{
        TextView title;
        ImageView image001,image002;
    }
    static  class  Image03_ViewHolder{
        TextView title;
        ImageView image01,image02,image03;
    }
}