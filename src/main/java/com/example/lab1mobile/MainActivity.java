package com.example.lab1mobile;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {

    private EditText coefficientAInput, coefficientBInput, coefficientCInput;
    private EditText leftBoundInput, rightBoundInput, toleranceInput;
    private Spinner equationTypeSpinner;
    private TextView resultText, cText;

    public static final String itemAText = "ax + b = 0";
    public static final String itemBText = "ax^2 + bx + c = 0";
    public static final String itemCText = "ax^3 + bx^2 + d = 0 (бісекція)";
    public static final String itemDText = "ax^3 + bx^2 + d = 0 (хорд)";
    public static final String itemEText = "ax^3 + bx^2 + d = 0 (ітерацій)";

    public static final String FILE_NAME = "function_data.txt";
    private static final int FILE_PICKER_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coefficientAInput = findViewById(R.id.coefficientAInput);
        coefficientBInput = findViewById(R.id.coefficientBInput);
        coefficientCInput = findViewById(R.id.coefficientCInput);
        leftBoundInput = findViewById(R.id.leftBoundInput);
        rightBoundInput = findViewById(R.id.rightBoundInput);
        toleranceInput = findViewById(R.id.toleranceInput);
        cText = findViewById(R.id.cText);
        equationTypeSpinner = findViewById(R.id.equationTypeSpinner);
        resultText = findViewById(R.id.resultText);
        Button calculateButton = findViewById(R.id.calculateButton);
        Button saveButton = findViewById(R.id.saveButton);
        Button loadButton = findViewById(R.id.loadButton);
        Button openChartButton = findViewById(R.id.openChart);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.equation_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        equationTypeSpinner.setAdapter(adapter);

        equationTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = equationTypeSpinner.getSelectedItem().toString();

                if (selectedItem.equals(itemAText)) {
                    coefficientCInput.setVisibility(View.INVISIBLE);
                    cText.setVisibility(View.INVISIBLE);
                } else {
                    coefficientCInput.setVisibility(View.VISIBLE);
                    cText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                coefficientCInput.setVisibility(View.INVISIBLE);
                cText.setVisibility(View.INVISIBLE);
            }
        });

        calculateButton.setOnClickListener(view -> solveEquation());

        saveButton.setOnClickListener(view -> saveDataToFile());

        loadButton.setOnClickListener(view -> pickFile());

        openChartButton.setOnClickListener(view -> openChart());
    }

    private void openChart() {
        Intent intent = new Intent(this, ChartActivity.class);

        String equationType = equationTypeSpinner.getSelectedItem().toString();
        double a = Double.parseDouble(coefficientAInput.getText().toString().replace(",","."));
        double b = Double.parseDouble(coefficientBInput.getText().toString().replace(",","."));
        double c = Double.parseDouble(coefficientCInput.getText().toString().isEmpty() ? "0.0" : coefficientCInput.getText().toString().replace(",","."));
        double left = Double.parseDouble(leftBoundInput.getText().toString().replace(",","."));
        double right = Double.parseDouble(rightBoundInput.getText().toString().replace(",","."));

        intent.putExtra("EQUATION_TYPE", equationType);
        intent.putExtra("A", a);
        intent.putExtra("B", b);
        intent.putExtra("C", c);
        intent.putExtra("left", left);
        intent.putExtra("right", right);

        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            loadDataFromFile(uri); // Передаємо обраний файл для зчитування
        }
    }

    private void solveEquation() {
        try {
            double a = Double.parseDouble(coefficientAInput.getText().toString().replace(",","."));
            double b = Double.parseDouble(coefficientBInput.getText().toString().replace(",","."));
            double c = Double.parseDouble(coefficientCInput.getText().toString().isEmpty() ? "0.0" : coefficientCInput.getText().toString().replace(",","."));
            double left = Double.parseDouble(leftBoundInput.getText().toString().replace(",","."));
            double right = Double.parseDouble(rightBoundInput.getText().toString().replace(",","."));
            double tolerance = Double.parseDouble(toleranceInput.getText().toString().replace(",","."));
            String equationType = equationTypeSpinner.getSelectedItem().toString();

            double root = 0;
            int iterations = 0;

            switch (equationType) {
                case itemAText:
                    root = solveLinear(a, b);
                    iterations = 1;
                    break;
                case itemBText:
                    root = solveQuadratic(a, b, c, left, right);
                    iterations = 1;
                    break;
                case itemCText:
                    root = solveBisection(a, b, c, left, right, tolerance);
                    iterations = (int) Math.ceil(Math.log((right - left) / tolerance) / Math.log(2));
                    break;
                case itemDText:
                    Pair<Double, Integer> result = solveSecantMethod(a, b, c, left, right, tolerance);
                    root = result.first;
                    iterations = result.second;
                    break;
                case itemEText:
                    Pair<Double, Integer> result2 = solveFixedPointMethod(a, b, c, left, right, tolerance);
                    root = result2.first;
                    iterations = result2.second;
                    break;
                default:
                    Toast.makeText(this, "Невірний тип рівняння", Toast.LENGTH_LONG).show();
                    return;
            }

            if (root < left || root > right) {
                Toast.makeText(this, "Не існує кореня в межах ренжах.", Toast.LENGTH_LONG).show();
                resultText.setText("");
                return;
            }

            resultText.setText("Корінь: " + String.format("%.5f", root) +
                    "\nТочність: " + tolerance +
                    "\nІтерацій: " + iterations);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Помилка: " + e, Toast.LENGTH_LONG).show();
        }
    }

    private Pair<Double, Integer> solveFixedPointMethod(double a, double b, double c, double left, double right, double tolerance) {
        int iteration = 0;
        double x1;
        double x0 = (left + right) / 2;

        int maxIteration = 100;
        while (iteration < maxIteration) {
            x1 = transformation(x0, a, b, c);

            // Перевірка на точність
            if (Math.abs(x1 - x0) < tolerance) {
                return Pair.create(x1, iteration);
            }

            x0 = x1;
            iteration++;
        }

        return Pair.create(x0, iteration);
    }

    public static double transformation(double x, double a, double b, double c) {
        return Math.cbrt(-(b * x * x + c) / a);
    }

    private Pair<Double, Integer> solveSecantMethod(double a, double b, double d, double x0, double x1, double tolerance) {
        int iteration = 0;
        double f_x0 = f2(a, b, d, x0);
        double f_x1 = f2(a, b, d, x1);


        int maxIteration = 100;
        while (iteration < maxIteration) {
            double x2 = x1 - (f_x1 * (x1 - x0)) / (f_x1 - f_x0);
            double f_x2 = f2(a, b, d, x2);

            if (Math.abs(f_x2) < tolerance) {
                return Pair.create(x2, iteration);
            }

            x0 = x1;
            x1 = x2;
            f_x0 = f_x1;
            f_x1 = f_x2;

            iteration++;
        }

        return Pair.create(x1, iteration);
    }

    private double solveLinear(double a, double b) {
        double result;
        if (a == 0) {
            Toast.makeText(this, "Не можна ділити на 0.", Toast.LENGTH_LONG).show();
            return 0;
        } else {
            result = -b / a;
        }
        return result;
    }

    private double solveQuadratic(double a, double b, double c, double left, double right) {
        double discriminant = b * b - 4 * a * c;
        if (discriminant >= 0) {
            double r1 = (-b + Math.sqrt(discriminant)) / (2 * a);
            double r2 = (-b - Math.sqrt(discriminant)) / (2 * a);
            return Stream.of(r1, r2)
                    .filter(e -> e >= left && e <= right)
                    .findFirst().orElseGet(() -> left - 1.0);
        } else {
            Toast.makeText(this, "Від'ємний дискримінант! Немає коренів.", Toast.LENGTH_LONG).show();
            return 0;
        }
    }

    private double solveBisection(double a, double b, double c, double left, double right, double tolerance) {
        double mid;
        while ((right - left) / 2 > tolerance) {
            mid = (left + right) / 2;
            if (f2(a, b, c, mid) * f2(a, b, c, left) < 0) {
                right = mid;
            } else {
                left = mid;
            }
        }
        return (left + right) / 2;
    }

    private double f(double a, double b, double c, double x) {
        return a * x * x + b * x + c;
    }

    private double f2(double a, double b, double c, double x) {
        return a * x * x * x + b * x * x + c;
    }

    private void saveDataToFile() {
        try {
            double a = Double.parseDouble(coefficientAInput.getText().toString().replace(",","."));
            double b = Double.parseDouble(coefficientBInput.getText().toString().replace(",","."));
            double c = Double.parseDouble(coefficientCInput.getText().toString().isEmpty() ? "0.0" : coefficientCInput.getText().toString().replace(",","."));
            double left = Double.parseDouble(leftBoundInput.getText().toString().replace(",","."));
            double right = Double.parseDouble(rightBoundInput.getText().toString().replace(",","."));
            double tolerance = Double.parseDouble(toleranceInput.getText().toString().replace(",","."));
            String type = equationTypeSpinner.getSelectedItem().toString();
            double step = (right - left) / 100;
            String equationType = equationTypeSpinner.getSelectedItem().toString();

            StringBuilder data = new StringBuilder();
            data.append(String.format("%.5f;%.5f;%.5f;%.5f;%.5f;%.5f;%s\n", a, b, c, left, right, tolerance, type));

            double y;
            for (double x = left; x <= right; x += step) {
                switch (equationType) {
                    case itemAText:
                        y = a * x + b;
                        break;
                    case itemBText:
                        y = f(a, b, c, x);
                        break;
                    case itemCText:
                    case itemDText:
                    case itemEText:
                        y = f2(a, b, c, x);
                        break;
                    default:
                        Toast.makeText(this, "Невірний тип рівняння", Toast.LENGTH_LONG).show();
                        return;
                }
                data.append(String.format("%.5f, %.5f\n", x, y));
            }

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "function_data.txt"); // Ім'я файлу
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain"); // Тип файлу
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS); // Папка "Завантаження"

            // Отримуємо URI для збереження
            Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);

            try (OutputStream outputStream = getContentResolver().openOutputStream(uri);
                 OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                 BufferedWriter bufferedWriter = new BufferedWriter(writer)) {

                bufferedWriter.write(data.toString()); // Записуємо текст у файл
                bufferedWriter.flush(); // Очищаємо буфер

                Toast.makeText(this, "Файл збережений у папці Завантаження у файлі " + FILE_NAME, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Помилка збереження даних", Toast.LENGTH_LONG).show();
        }
    }

    private void loadDataFromFile(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String firstLine = reader.readLine();
            if (firstLine != null) {
                String[] params = firstLine.split(";");

                coefficientAInput.setText(params[0]); // a
                coefficientBInput.setText(params[1]); // b
                coefficientCInput.setText(params[2]); // c
                leftBoundInput.setText(params[3]);    // left
                rightBoundInput.setText(params[4]);   // right
                toleranceInput.setText(params[5]);    // precision
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) equationTypeSpinner.getAdapter();
                equationTypeSpinner.setSelection(adapter.getPosition(params[6]));    // type
            }

            inputStream.close();
            Toast.makeText(this, "Дані завантажені та проставлені у поля", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Помилка завантаження даних", Toast.LENGTH_LONG).show();
        }
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
    }

}
