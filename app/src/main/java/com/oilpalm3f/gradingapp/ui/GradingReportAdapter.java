package com.oilpalm3f.gradingapp.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.github.chrisbanes.photoview.PhotoView;
import com.oilpalm3f.gradingapp.R;
import com.oilpalm3f.gradingapp.database.DataAccessHandler;
import com.oilpalm3f.gradingapp.database.Queries;
import com.oilpalm3f.gradingapp.dbmodels.GradingReportModel;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GradingReportAdapter extends RecyclerView.Adapter<GradingReportAdapter.CollectionReportViewHolder> {

    private static final String LOG_TAG = GradingReportAdapter.class.getName();
    private List<GradingReportModel> mList;
    private Context context;
    private GradingReportModel item;
    private DataAccessHandler dataAccessHandler = null;
   private onPrintOptionSelected onPrintSelected;
    SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy");
    int row_index = -1;
    LayoutInflater mInflater;
    public GradingReportAdapter(Context context) {
        this.context = context;
        mList = new ArrayList<>();
        dataAccessHandler = new DataAccessHandler(context);
    }

    @Override
    public CollectionReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.gradingreport_item, null);
        CollectionReportViewHolder myHolder = new CollectionReportViewHolder(view);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(CollectionReportViewHolder holder, final int position) {
       item = mList.get(position);

        if (item == null)
            return;

        holder.tvtokennumber.setText(item.getTokenNumber().trim());
        holder.tv_cccode.setText(item.getCCCode().trim());
       holder.tvFruitType.setText(item.getFruitType().trim());
        holder.tvgrossweight.setText(item.getGrossWeight().trim() + " " );
//        String plotCodes = TextUtils.join(", ",dataAccessHandler.getListOfCodes(Queries.getInstance().getPlotCodes(item.getCode())).toArray());


        try {
            Date oneWayTripDate = input.parse(item.getTokenDate());
          String  datetimevaluereq = output.format(oneWayTripDate);
            holder.tvtokendate.setText(datetimevaluereq);

            Log.e("===============", "======currentData======" + output.format(oneWayTripDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if( holder.sublinear.getVisibility() == View.VISIBLE) {
            holder.image_less.setVisibility(View.VISIBLE);
            holder.image_more.setVisibility(View.GONE);
        }
        else {
            holder.image_less.setVisibility(View.GONE);
            holder.image_more.setVisibility(View.VISIBLE);
        }


        if(row_index== position)
        {
            holder.sublinear.setVisibility(View.VISIBLE);
            holder.image_less.setVisibility(View.VISIBLE);
            holder.image_more.setVisibility(View.GONE);
            holder.bind(item);
            // holder.createdDateTextView.setVisibility(View.VISIBLE);

        }else{
            holder.sublinear.setVisibility(View.GONE);
            holder.image_more.setVisibility(View.VISIBLE);
            holder.image_less.setVisibility(View.GONE);
            //  holder.createdDateTextView.setVisibility(View.GONE);
        }




        holder.printBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != onPrintSelected) {
                    onPrintSelected.printOptionSelected(position);
                }
            }
        });
        holder.viewimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //    Context context=context.getApplicationContext();
                mInflater = LayoutInflater.from(context);
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
                View mView =mInflater.inflate(R.layout.dialog_custom_layout, null);
                //  Picasso.with(mContext).load(getCollectionInfoById.getResult().getReceiptImg()).error(R.drawable.ic_user).into(photoView);
                PhotoView photoView = mView.findViewById(R.id.imageView);
                TextView cancel =mView.findViewById(R.id.cancel);

                String imagelocation = dataAccessHandler.getOnlyOneValueFromDb(Queries.getInstance().getImageQuery(item.getTokenNumber()));

                Log.e("===============", "======imagelocation======" +imagelocation);

                File imgFile = new File(imagelocation);

                if(imgFile.exists()){

                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());


                    photoView.setImageBitmap(myBitmap);}


//                if(imagelocation!=null)
//                    Picasso.with(context).load(imagelocation).error(R.drawable.gallery).placeholder( R.drawable.progress_animation).into(photoView);
//                    //  Picasso.with(mContext).load(getCollectionInfoById.getResult().getReceiptImg()).error(R.drawable.ic_user).into(photoView);
//                else
//                    Picasso.with(context).load(R.drawable.gallery).error(R.drawable.ico_btn_photo).placeholder( R.drawable.progress_animation).into(photoView);
//                //photoView.setImageResource(Integer.parseInt(getCollectionInfoById.getResult().getReceiptImg()));
                mBuilder.setView(mView);


                final AlertDialog mDialog = mBuilder.create();
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDialog.dismiss();
                    }
                });
                mDialog.show();
            }

        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean expanded = item.isExpanded();
                item.setExpanded(!expanded);
                //  notifyItemChanged(position);
                int oldindex=  row_index;
                row_index = position;
                notifyItemChanged(oldindex);
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
       return mList.size();

    }

    public void updateAdapter(List<GradingReportModel> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public class CollectionReportViewHolder extends RecyclerView.ViewHolder {
        private TextView tvtokennumber;
        private TextView tv_cccode;
        private TextView tvFruitType;
        private TextView tvgrossweight;
        private TextView tvtokendate;
        private TextView tvunripen;
        private TextView tvunderripe;
        private TextView tvripen;
        private TextView tvoverripen;
        private TextView tvdiseased,tvemptybunches,tvffbqualitylong,tvffbqualitymedium,tvffbqualityshort,tvffbqualityoptium,tvloosefruit,tvloosefruitweight,tvgradername,tvrejectedbunches;
        private ImageView printBtn;
        private LinearLayout linearunripen,linearunderripe,linearripen,linearoverripe,lineardiseased,learemptybunches,linearffbqualitylong,linearffbqualitymedium,linearffbqualityshort,linearffbqualityoptimum,
                linearloosefruit,linearloosefruitweight,lineargradername,sublinear;
        public ImageView image_less,image_more,viewimage;
        public CollectionReportViewHolder(View view) {
            super(view);
            tvtokennumber = (TextView) view.findViewById(R.id.tvtokennumber);
            tv_cccode = (TextView) view.findViewById(R.id.tv_cccode);
            tvFruitType = (TextView) view.findViewById(R.id.tvFruitType);
            tvgrossweight = (TextView) view.findViewById(R.id.tvgrossweight);
            tvtokendate = (TextView) view.findViewById(R.id.tvtokendate);
            tvunripen = (TextView) view.findViewById(R.id.tvunripen);
            tvunderripe = (TextView) view.findViewById(R.id.tvunderripe);
            tvripen = (TextView) view.findViewById(R.id.tvripen);
            tvoverripen = (TextView) view.findViewById(R.id.tvoverripen);
            tvdiseased = (TextView) view.findViewById(R.id.tvdiseased);
            tvemptybunches =(TextView)view.findViewById(R.id.tvemptybunches);
           printBtn = (ImageView) view.findViewById(R.id.printBtn);
            tvffbqualitylong = (TextView)view.findViewById(R.id.tvffbqualitylong);
            tvffbqualitymedium =(TextView)view.findViewById(R.id.tvffbqualitymedium);
            tvffbqualityshort =(TextView)view.findViewById(R.id.tvffbqualityshort);
            tvffbqualityoptium = (TextView)view.findViewById(R.id.tvffbqualityoptium);
            tvloosefruit =(TextView)view.findViewById(R.id.tvloosefruit);
            tvloosefruitweight  =(TextView)view.findViewById(R.id.tvloosefruitweight);
            tvgradername =(TextView)view.findViewById(R.id.tvgradername);
            tvrejectedbunches = (TextView)view.findViewById(R.id.tvrejectedbunches);
            linearunripen = (LinearLayout)view.findViewById(R.id.linearunripen);
            linearunderripe = (LinearLayout)view.findViewById(R.id.linearunderripe);
            linearripen = (LinearLayout)view.findViewById(R.id.linearripen);
            linearoverripe = (LinearLayout)view.findViewById(R.id.linearoverripe);
            lineardiseased = (LinearLayout)view.findViewById(R.id.lineardiseased);
            learemptybunches = (LinearLayout)view.findViewById(R.id.learemptybunches);
            linearffbqualitylong = (LinearLayout)view.findViewById(R.id.linearffbqualitylong);
            linearffbqualitymedium = (LinearLayout)view.findViewById(R.id.linearffbqualitymedium);

            linearffbqualityshort = (LinearLayout)view.findViewById(R.id.linearffbqualityshort);
            linearffbqualityoptimum = (LinearLayout)view.findViewById(R.id.linearffbqualityoptimum);
            linearloosefruit = (LinearLayout)view.findViewById(R.id.linearloosefruit);
            linearloosefruitweight = (LinearLayout)view.findViewById(R.id.linearloosefruitweight);
            lineargradername = (LinearLayout)view.findViewById(R.id.lineargradername);
            sublinear =(LinearLayout)view.findViewById(R.id.sublinear);
            image_less =view.findViewById(R.id.image_less);
            image_more =view.findViewById(R.id.image_more);
            viewimage = view.findViewById(R.id.viewimage);


        }

        public void bind(GradingReportModel item) {

            boolean expanded = item.isExpanded();

            sublinear.setVisibility(expanded ? View.VISIBLE : View.GONE);
            if(item.getUnRipen() != 0){
                tvunripen.setText(item.getUnRipen()+"");}
            else{
                linearunripen.setVisibility(View.GONE);}

            if(item.getUnderRipe()!=0 ){
                tvunderripe.setText(item.getUnderRipe()+"");
            }
            else{
                linearunderripe.setVisibility(View.GONE);
            }
            if (item.getRipen() != 0) {
                tvripen.setText(item.getRipen()+"");
            }
            else {
                linearripen.setVisibility(View.GONE);
            }
            if(item.getOverRipe()!= 0){
                tvoverripen.setText(item.getOverRipe()+"");}
            else{
                linearoverripe.setVisibility(View.GONE);}
            if (item.getDiseased()!=0)
            {
                tvdiseased.setText(item.getDiseased()+"");}
            else {
                lineardiseased.setVisibility(View.GONE);
            }
            if (item.getEmptyBunches()!=0){
                tvemptybunches.setText(item.getEmptyBunches()+"");}
            else {
                learemptybunches.setVisibility(View.GONE);
            }
            if (item.getFFBQualityLong()!=0){
                tvffbqualitylong.setText(""+item.getFFBQualityLong());}
            else{
                linearffbqualitylong.setVisibility(View.GONE);
            }
            if(item.getFFBQualityMedium()!=0){
                tvffbqualitymedium.setText(item.getFFBQualityMedium()+"");
            }
            else{
                linearffbqualitymedium.setVisibility(View.GONE);

            }
            if(item.getFFBQualityShort()!=0){
                tvffbqualityshort.setText(item.getFFBQualityShort()+"");}
            else{
                linearffbqualityshort.setVisibility(View.GONE);
            }
            if (item.getFFBQualityOptimum()!=0){
                tvffbqualityoptium.setText(item.getFFBQualityOptimum()+"");}
            else{
                linearffbqualityoptimum.setVisibility(View.GONE);
            }

            if(item.getLooseFruitWeight()!= null  ){
                tvloosefruitweight.setText(item.getLooseFruitWeight()+"");}
            else{
                linearloosefruitweight.setVisibility(View.GONE);
            }

            tvgradername.setText(item.getGraderName()+"");

            if(item.getRejectedBunches()!= 0)
                tvrejectedbunches.setText(item.getRejectedBunches()+"");
            else{
                // holder.r.setVisibility(View.GONE);
            }
            if( sublinear.getVisibility() == View.VISIBLE) {
                image_less.setVisibility(View.VISIBLE);
                image_more.setVisibility(View.GONE);
            }
            else {
                image_less.setVisibility(View.GONE);
                image_more.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setonPrintSelected(final onPrintOptionSelected onPrintSelected) {
        this.onPrintSelected = onPrintSelected;
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }
}
