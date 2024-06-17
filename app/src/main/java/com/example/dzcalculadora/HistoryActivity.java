package com.example.dzcalculadora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HistoryActivity extends AppCompatActivity {
    private Button findButtonById(int id) {
        return findViewById(id);
    }
    //Método para diminuir as redundâncias no código
    private TextView findTextViewById(int id) {
        return findViewById(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Button btnRetornar = findButtonById(R.id.btnRetornar);

        TextView txvVrHistorico = findViewById(R.id.txvVrHistorico);

        String historico = getIntent().getStringExtra("history");
        txvVrHistorico.setText(historico);

        btnRetornar.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
            // Iniciar a MainActivity
            startActivity(intent);
            // Finalizar a HistoricoActivity para remover da pilha de atividades
            finish();
        });
    }
}