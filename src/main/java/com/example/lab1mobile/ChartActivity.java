package com.example.lab1mobile;

import static com.example.lab1mobile.MainActivity.itemAText;
import static com.example.lab1mobile.MainActivity.itemBText;
import static com.example.lab1mobile.MainActivity.itemCText;
import static com.example.lab1mobile.MainActivity.itemDText;
import static com.example.lab1mobile.MainActivity.itemEText;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class ChartActivity extends AppCompatActivity {

    private LineChart lineChart;
    private static final double OFFSET = 2.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        lineChart = findViewById(R.id.lineChart);

        Intent intent = getIntent();
        String equationType = intent.getStringExtra("EQUATION_TYPE");
        double a = intent.getDoubleExtra("A", 0.0);
        double b = intent.getDoubleExtra("B", 0.0);
        double c = intent.getDoubleExtra("C", 0.0);
        double left = intent.getDoubleExtra("left", 0.0);
        double right = intent.getDoubleExtra("right", 0.0);

        try {
            ArrayList<Entry> entries = generateFunctionData(a, b, c, left - OFFSET, right + OFFSET, equationType);
            displayChart(entries, equationType);
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка построения графика", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<Entry> generateFunctionData(double a, double b, double c, double left, double right, String equationType) {
        ArrayList<Entry> entries = new ArrayList<>();
        double step = (right - left) / 100.0;

        for (double x = left; x <= right; x += step) {
            double y;
            switch (equationType) {
                case itemAText:
                    y = a * x + b;
                    break;
                case itemBText:
                    y = a * x * x + b * x + c;
                    break;
                case itemCText:
                case itemDText:
                case itemEText:
                    y = a * x * x * x + b * x + c;
                    break;
                default:
                    throw new IllegalArgumentException("Тип рівняння не підтримується");
            }
            entries.add(new Entry((float) x, (float) y));
        }

        return entries;
    }

    private void displayChart(ArrayList<Entry> entries, String equationType) {
        LineDataSet dataSet = new LineDataSet(entries, equationType);
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        Legend legend = lineChart.getLegend();
        legend.setTextSize(12f);

        lineChart.invalidate();
    }
}
