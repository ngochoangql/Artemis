package com.example.artemis;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.artemis.Data.DeviceData;
import com.example.artemis.Data.UserDatabase;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Analytics#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Analytics extends Fragment {

    LinearLayout analytics;
    List<DeviceData> mListDevice;
    public Analytics() {
        // Required empty public constructor
    }

    public static Analytics newInstance(String param1, String param2) {
        Analytics fragment = new Analytics();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);
        BarChart barChart = view.findViewById(R.id.barChartKwh);

        int dayInMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) ;
        // Tạo danh sách các thanh cột (BarEntry) cho biểu đồ

        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 1; i <= dayInMonth; i++) {
            // Tạo các BarEntry với giá trị mẫu (ví dụ: random)
            float value = (float) Math.random() * 100; // Giả sử giá trị random từ 0 đến 100
            entries.add(new BarEntry(i, value));
        }

        // Tạo dataset từ danh sách các BarEntry
        BarDataSet dataSet = new BarDataSet(entries, "Số liệu theo tháng");
        dataSet.setColor(Color.BLUE); // Màu của cột


        // Tạo dữ liệu biểu đồ từ dataset
        BarData barData = new BarData(dataSet);


        // Cấu hình hiển thị cho biểu đồ
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false); // Tắt miêu tả
        barChart.getXAxis().setEnabled(false); // Tắt trục X
        barChart.getAxisRight().setEnabled(false); // Tắt trục phải
        barChart.animateY(1000); // Hiệu ứng animation
        barChart.invalidate();
        // Cấu hình XAxis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Đặt nhãn ở dưới
        xAxis.setGranularity(1f); // Đặt khoảng cách giữa các nhãn
        xAxis.setGranularityEnabled(true);

        analytics = view.findViewById(R.id.analyticsS);
        mListDevice = UserDatabase.getInstance(view.getContext()).deviceDataDao().getAll();


        for (DeviceData item : mListDevice) {
            View itemView = LayoutInflater.from(view.getContext()).inflate(R.layout.analytics_item, analytics, false);
            // Set thông tin cho itemView nếu cần
//            TextView textView = itemView.findViewById(R.id.deviceName);
//            FrameLayout frameLayout = itemView.findViewById(R.id.deviceItem);
//            textView.setText(item.device_name);

            // Hiển thị biểu
            PieChart pieChart = itemView.findViewById(R.id.pieChart);

            // Tạo dữ liệu mẫu cho PieChart
            ArrayList<PieEntry> entriesPieChart = new ArrayList<>();
            entriesPieChart.add(new PieEntry(40, "Use"));
            entriesPieChart.add(new PieEntry(50, "Unuse"));


            // Tạo dataset từ danh sách các PieEntry
            PieDataSet dataSetPieChart = new PieDataSet(entriesPieChart,"Giờ hoạt động");

            // Thiết lập màu sắc cho các phần tử trong biểu đồ
            dataSetPieChart.setColors(Color.GREEN, Color.BLACK);

            // Tạo dữ liệu biểu đồ từ dataset
            PieData pieData = new PieData(dataSetPieChart);

            // Cấu hình hiển thị cho biểu đồ
            pieChart.setData(pieData);
            pieChart.getDescription().setEnabled(false); // Tắt miêu tả

            // Hiển thị biểu đồ
            pieChart.invalidate();
            analytics.addView(itemView);
        }
        return view;
    }
}