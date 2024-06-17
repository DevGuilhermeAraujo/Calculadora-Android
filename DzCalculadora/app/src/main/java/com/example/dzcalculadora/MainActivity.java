package com.example.dzcalculadora;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;

    private TextView txtOperacaoAtual;
    private TextView txtHistorico;
    private StringBuilder currentInput = new StringBuilder();
    private float firstValue = 0;
    private float secondValue = 0;
    private String operator = "";
    private boolean isNewOperation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtOperacaoAtual = findViewById(R.id.txtOperacaoAtual);
        txtHistorico = findViewById(R.id.txtHistorico);

        // Verificar se a permissão WRITE_EXTERNAL_STORAGE já foi concedida
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permissão
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }

        setNumberButtonListeners();
        setOperatorButtonListeners();
        setEqualsButtonListener();
        setClearButtonListener();
        setPercentageButtonListener(); // Adicionando a nova funcionalidade de Porcentagem
    }

    private void setNumberButtonListeners() {
        int[] numberButtonIds = {
                R.id.btnZero, R.id.btnUm, R.id.btnDois, R.id.btnTres,
                R.id.btnQuatro, R.id.btnCinco, R.id.btnSeis,
                R.id.btnSete, R.id.btnOito, R.id.btnNove
        };

        View.OnClickListener listener = v -> {
            Button button = (Button) v;
            if (isNewOperation) {
                txtOperacaoAtual.setText("");
                currentInput.setLength(0);
                isNewOperation = false;
            }
            currentInput.append(button.getText().toString());
            txtOperacaoAtual.setText(currentInput);
        };

        for (int id : numberButtonIds) {
            findViewById(id).setOnClickListener(listener);
        }
    }

    private void setOperatorButtonListeners() {
        int[] operatorButtonIds = {
                R.id.btnAdicao, R.id.btnSubtracao, R.id.btnMultiplicacao, R.id.btnDivisao
        };

        View.OnClickListener listener = v -> {
            Button button = (Button) v;
            operator = button.getText().toString();
            firstValue = Float.parseFloat(currentInput.toString());
            txtHistorico.setText(currentInput + operator);
            txtOperacaoAtual.setText(null);
            currentInput.setLength(0);
            isNewOperation = false;
        };

        for (int id : operatorButtonIds) {
            findViewById(id).setOnClickListener(listener);
        }
    }

    private void setEqualsButtonListener() {
        findViewById(R.id.btnIgual).setOnClickListener(v -> {
            if (!isNewOperation) {
                secondValue = Float.parseFloat(currentInput.toString());
                float result = calculateResult(firstValue, secondValue, operator);
                txtHistorico.setText(txtHistorico.getText().toString() + currentInput.toString() + " = ");
                txtOperacaoAtual.setText(String.valueOf(result));
                currentInput.setLength(0);
                isNewOperation = true;
            }
        });
    }

    private void setClearButtonListener() {
        findViewById(R.id.btnClearAll).setOnClickListener(v -> {
            currentInput.setLength(0);
            firstValue = 0;
            secondValue = 0;
            operator = "";
            txtOperacaoAtual.setText("");
            txtHistorico.setText("");
            isNewOperation = true;
        });
    }

    private void setPercentageButtonListener() {
        findViewById(R.id.btnPorcentagem).setOnClickListener(v -> {
            if (!isNewOperation && currentInput.length() > 0) {
                float percentage = Float.parseFloat(currentInput.toString()) / 100;
                // Calcula a porcentagem do primeiro valor
                float result = firstValue * percentage;
                txtOperacaoAtual.setText(String.valueOf(result));
                currentInput.setLength(0);
                isNewOperation = true;
            }
        });
    }

    private float calculateResult(float firstValue, float secondValue, String operator) {
        float result = 0;
        switch (operator) {
            case "+":
                result = firstValue + secondValue;
                break;
            case "-":
                result = firstValue - secondValue;
                break;
            case "*":
                result = firstValue * secondValue;
                break;
            case "/":
                if (secondValue != 0) {
                    result = firstValue / secondValue;
                } else {
                    // Tratar divisão por zero
                }
                break;
        }
        return result;
    }



    private void insertHistorico(String conteudo) {
        File diretorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File arquivo = new File(diretorio, "HCALC.txt");

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String dataHoraFormatada = dateFormat.format(new Date());

        try {
            FileWriter escritor = new FileWriter(arquivo, true);
            BufferedWriter bufferEscrita = new BufferedWriter(escritor);
            bufferEscrita.write(dataHoraFormatada + "\n" + conteudo + "\n");
            bufferEscrita.close();
            escritor.close();
        } catch (IOException e) {
            Log.e("TAG", "Erro ao manipular arquivo", e);
        }
    }

    private String selectHistorico() {
        File diretorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File arquivo = new File(diretorio, "HCALC.txt");

        StringBuilder texto = new StringBuilder();
        try {
            BufferedReader bufferLeitura = new BufferedReader(new FileReader(arquivo));
            String linha;
            while ((linha = bufferLeitura.readLine()) != null) {
                texto.append(linha).append("\n");
            }
            bufferLeitura.close();
        } catch (IOException e) {
            Log.e("TAG", "Erro ao manipular arquivo", e);
        }
        return texto.toString();
    }
}
