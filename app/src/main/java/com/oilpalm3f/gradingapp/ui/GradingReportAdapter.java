package com.oilpalm3f.gradingapp.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.oilpalm3f.gradingapp.R;
import com.oilpalm3f.gradingapp.database.DataAccessHandler;
import com.oilpalm3f.gradingapp.dbmodels.GradingReportModel;

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

        String fruitType;

        if (item.getFruitType().equalsIgnoreCase("01")){

            fruitType = "Collection";
        }else{
            fruitType = "Consignment";
        }

        holder.tvtokennumber.setText(item.getTokenNumber().trim());
        holder.tv_cccode.setText(item.getCCCode().trim());
       holder.tvFruitType.setText(fruitType);
        holder.tvgrossweight.setText(item.getGrossWeight().trim() + " " );
//        String plotCodes = TextUtils.join(", ",dataAccessHandler.getListOfCodes(Queries.getInstance().getPlotCodes(item.getCode())).toArray());


        try {
            Date oneWayTripDate = input.parse(item.getCreatedDate());
          String  datetimevaluereq = output.format(oneWayTripDate);
            holder.tvtokendate.setText(datetimevaluereq);

            Log.e("===============", "======currentData======" + output.format(oneWayTripDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }


       if(item.getUnRipen() != 0){
        holder.tvunripen.setText(item.getUnRipen()+"");}
       else{
           holder.linearunripen.setVisibility(View.GONE);}

           if(item.getUnderRipe()!=0 ){
               holder.tvunderripe.setText(item.getUnderRipe()+"");
           }
      else{
               holder.linearunderripe.setVisibility(View.GONE);
           }
        if (item.getRipen() != 0) {
            holder.tvripen.setText(item.getRipen()+"");
        }
        else {
            holder.linearripen.setVisibility(View.GONE);
        }
        if(item.getOverRipe()!= 0){
       holder.tvoverripen.setText(item.getOverRipe()+"");}
        else{
            holder.linearoverripe.setVisibility(View.GONE);}
        if (item.getDiseased()!=0)
        {
       holder.tvdiseased.setText(item.getDiseased()+"");}
        else {
            holder.lineardiseased.setVisibility(View.GONE);
        }
        if (item.getEmptyBunches()!=0){
        holder.tvemptybunches.setText(item.getEmptyBunches()+"");}
            else {
            holder.learemptybunches.setVisibility(View.GONE);
        }
        if (item.getFFBQualityLong()!= 0){
        holder.tvffbqualitylong.setText(""+item.getFFBQualityLong());}
        else{
            holder.linearffbqualitylong.setVisibility(View.GONE);
        }
        if(item.getFFBQualityMedium()!=0){
            holder.tvffbqualitymedium.setText(item.getFFBQualityMedium()+"");
        }
       else{
            holder.linearffbqualitymedium.setVisibility(View.GONE);

        }
       if(item.getFFBQualityShort()!=0){
        holder.tvffbqualityshort.setText(item.getFFBQualityShort()+"");}
       else{
           holder.linearffbqualityshort.setVisibility(View.GONE);
       }
       if (item.getFFBQualityOptimum()!=0){
        holder.tvffbqualityoptium.setText(item.getFFBQualityOptimum()+"");}
       else{
           holder.linearffbqualityoptimum.setVisibility(View.GONE);
       }

        if(item.getLooseFruitWeight()!= null  ){
        holder.tvloosefruitweight.setText(item.getLooseFruitWeight()+"");}
        else{
            holder.linearloosefruitweight.setVisibility(View.GONE);
        }

        holder.tvgradername.setText(item.getGraderName()+"");

        if(!TextUtils.isEmpty(item.getRejectedBunches()))
        holder.tvrejectedbunches.setText(item.getRejectedBunches()+"");
        else{
            holder.linearrejectedbunches.setVisibility(View.GONE);
        }

        holder.printBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != onPrintSelected) {
                    onPrintSelected.printOptionSelected(position);
                }
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
                linearloosefruit,linearloosefruitweight,lineargradername, linearrejectedbunches;

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
            linearrejectedbunches = (LinearLayout)view.findViewById(R.id.linearrejectedbunches);


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
