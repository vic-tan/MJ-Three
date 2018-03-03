package com.cast.lottery.mj.fragment;


import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.cast.lottery.mj.R;
import com.cast.lottery.mj.activity.KJDetailDialog;
import com.cast.lottery.mj.data.LotteryServiceManager;
import com.cast.lottery.mj.listener.RecyclerItemClickListener;
import com.cast.lottery.mj.models.Ball;
import com.cast.lottery.mj.models.Lottery;
import com.cast.lottery.mj.models.LotteryDetail;
import com.cast.lottery.mj.utils.LotteryUtils;
import com.cast.lottery.mj.widgets.KJLineItemView;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.view.LineChartView;
import rx.Subscriber;

/**
 * Created by Kevin on 2017/8/2.
 */

public class LatestFragment extends BaseContentFragment {
    private LatestLotteryAdapter latestLotteryAdapter;
    private SpinKitView spn_kit;
    private KJDetailDialog kjDetailDialog;

    public LatestFragment() {

    }

    public static BaseContentFragment newInstance() {
        return new LatestFragment();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (getData().size() == 0) {
            fetchData();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_latest, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        latestLotteryAdapter = new LatestLotteryAdapter();
        recyclerView.setAdapter(latestLotteryAdapter);
        spn_kit = (SpinKitView) rootView.findViewById(R.id.spin_kit);
        setOnItemClick(recyclerView);
        return rootView;
    }

    public void fetchData() {
        spn_kit.setVisibility(View.VISIBLE);
        //load local cache
        try {
            InputStream inputStream = getContext().getAssets().open("lottery_local_cache");
            InputStreamReader r = new InputStreamReader(inputStream);
            StringBuffer stringBuffer = new StringBuffer();
            char[] cbuf  = new char[10];
            while(r.read(cbuf)!=-1){
                stringBuffer.append(cbuf);
            }
            String str = stringBuffer.toString();
            Log.d("readCache",str);
            List<Lottery.Entity> list = new Gson().fromJson(str, new TypeToken<List<Lottery.Entity>>() {
            }.getType());

            getData().clear();
            getData().addAll(list);
            latestLotteryAdapter.notifyDataSetChanged();
            spn_kit.setVisibility(View.GONE);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        spn_kit.setVisibility(View.VISIBLE);
//        LotteryServiceManager.getInstance().getLastData360(new Subscriber<List<Lottery.IEntity>>() {
//            @Override
//            public void onCompleted() {
//                spn_kit.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                spn_kit.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onNext(List<Lottery.IEntity> list) {
//                Log.d("getLastData360", list.toString());
//                getData().clear();
//                getData().addAll(list);
//                latestLotteryAdapter.notifyDataSetChanged();
//                spn_kit.setVisibility(View.GONE);
//            }
//        });
    }

    private void setOnItemClick(RecyclerView recyclerView) {
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                LotteryServiceManager.getInstance().getLotteryDetail(new Subscriber<LotteryDetail>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(LotteryDetail lotteryDetail) {
                        if (kjDetailDialog == null) {
                            kjDetailDialog = new KJDetailDialog(getActivity());
                        }
                        kjDetailDialog.setLotteryDetailView(lotteryDetail);
                        kjDetailDialog.bind();
                        kjDetailDialog.setCanceledOnTouchOutside(false);
                        kjDetailDialog.show();

                    }
                }, LotteryUtils.getId(getData().get(position).getLotName()), getData().get(position).getIssue());
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));
    }


    private class LatestLotteryAdapter extends RecyclerView.Adapter<LastLotteryHolder> {


        @Override
        public LastLotteryHolder onCreateViewHolder(ViewGroup parent, int pos) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.latest_list_item, parent, false);
            return new LastLotteryHolder(view);
        }

        @Override
        public void onBindViewHolder(LastLotteryHolder holder, int pos) {
            Lottery.IEntity iEntity = LatestFragment.this.getData().get(pos);

            holder.name.setImageDrawable(buildItemNameDrawable(iEntity.getLotName()));
            holder.phase.setText("第" + iEntity.getIssue() + "期");
            holder.time.setText("开奖日期" + iEntity.getDate());
            DisplayMetrics outMetrics = null;
            outMetrics = new DisplayMetrics();
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
            holder.ball.setWidthHeight(outMetrics.widthPixels, outMetrics.heightPixels);
            List<Ball> balls = LotteryUtils.getBall(iEntity);
            holder.ball.addViewList(balls);

            List<Ball> numBalls = getNumBalls(balls);
            fillChart(holder.chart, numBalls);
        }

        public void fillChart(LineChartView chart, List<Ball> balls) {


            List<Line> lines = new ArrayList<Line>();

            List<PointValue> values = new ArrayList<PointValue>();
            PointValue pointValue;
            for (int j = 0; j < balls.size(); ++j) {
                pointValue = new PointValue(j + 1, Float.parseFloat(balls.get(j).getNum()));
                values.add(pointValue);
            }

            Line line = new Line(values);
            line.setShape(ValueShape.CIRCLE);
            line.setCubic(false);
            line.setFilled(false);
            line.setHasLabels(true);
            line.setHasLabelsOnlyForSelected(false);
            line.setHasLines(true);
            line.setHasPoints(true);

            lines.add(line);

            LineChartData data = new LineChartData(lines);

            Axis axisX = new Axis();
            Axis axisY = new Axis().setHasLines(true);
            axisY.setName("走势");
            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);

            data.setBaseValue(Float.NEGATIVE_INFINITY);
            chart.setLineChartData(data);

            chart.setLineChartData(data);

        }


        private List<Ball> getNumBalls(List<Ball> balls) {
            List<Ball> numBalls = new ArrayList<>();
            Ball ball;
            for (int i = 1; i <= balls.size(); ++i) {
                ball = balls.get(i - 1);
                if (isNumeric(ball.getNum())) {
                    numBalls.add(ball);
                }
            }
            return numBalls;
        }

        private int getSign() {
            int[] sign = new int[]{-1, 1};
            return sign[Math.round((float) Math.random())];
        }

        public boolean isNumeric(String str) {
            for (int i = 0; i < str.length(); i++) {
                if (!Character.isDigit(str.charAt(i))) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int getItemCount() {
            return getData().size();
        }
    }

    private class LastLotteryHolder extends RecyclerView.ViewHolder {


        private ImageView name;
        private TextView phase;
        private TextView time;
        private KJLineItemView ball;
        private LineChartView chart;

        public LastLotteryHolder(View itemView) {
            super(itemView);
            name = (ImageView) itemView.findViewById(R.id.name);
            phase = (TextView) itemView.findViewById(R.id.text_phase);
            time = (TextView) itemView.findViewById(R.id.text_timedraw);
            ball = (KJLineItemView) itemView.findViewById(R.id.view_ball);
            chart = (LineChartView) itemView.findViewById(R.id.chart);
        }
    }


    @Override
    public View getContainerView(View view) {
        return view.findViewById(R.id.container);
    }

    private TextDrawable buildItemNameDrawable(String text) {
        return TextDrawable.builder()
                .beginConfig()
                .textColor(Color.WHITE)
                .useFont(Typeface.DEFAULT)
                .fontSize(46) /* size in px */
                .width(160)  // width in px
                .height(160) // height in px
                .bold()
                .toUpperCase()
                .endConfig()
                .buildRound(text, getContext().getResources().getColor(R.color.color_ffca28));
    }

    @Override
    public void onRefresh() {
        fetchData();
    }
}
