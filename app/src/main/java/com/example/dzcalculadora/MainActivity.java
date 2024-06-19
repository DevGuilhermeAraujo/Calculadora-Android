package com.example.dzcalculadora;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 123;

    private TextView txtOperacaoAtual;
    private TextView txtResultado;
    private StringBuilder currentInput = new StringBuilder();
    private boolean isNewOperation = true;
    private int openParenthesesCount = 0; // Contador de parênteses abertos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtOperacaoAtual = findViewById(R.id.txtOperacaoAtual);
        txtResultado = findViewById(R.id.txtResultado);

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
        setDecimalButtonListeners();
        setEqualsButtonListener();
        setClearButtonListener();
        setPercentageButtonListener(); // Adicionando a nova funcionalidade de Porcentagem
        setParenthesesButtonListener(); // Adicionando a funcionalidade de parênteses

        // Adiciona o TextWatcher para calcular e mostrar a prévia do resultado
        txtOperacaoAtual.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Não necessário implementar
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String expression = s.toString().replace('x', '*').replace('÷', '/').replace(',', '.');

                // Verifica se há um operador seguido de um número na expressão
                if (hasOperatorFollowedByNumber(expression)) {
                    try {
                        float result = evaluateExpression(expression);
                        txtResultado.setText(formatResult(result));
                    } catch (Exception e) {
                        txtResultado.setText("");
                    }
                } else {
                    txtResultado.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Não necessário implementar
            }
        });
    }

    private void setNumberButtonListeners() {
        int[] numberButtonIds = {
                R.id.btnZero, R.id.btnUm, R.id.btnDois, R.id.btnTres,
                R.id.btnQuatro, R.id.btnCinco, R.id.btnSeis,
                R.id.btnSete, R.id.btnOito, R.id.btnNove
        };

        View.OnClickListener listener = v -> {
            Button button = (Button) v;
            String buttonText = button.getText().toString();

            if (isNewOperation) {
                isNewOperation = false;
                // Limpar a prévia do resultado ao começar um novo número
                txtResultado.setText("");
            }
            currentInput.append(buttonText);
            txtOperacaoAtual.append(buttonText);
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
            String buttonText = button.getText().toString();

            // Adiciona o número atual e o operador ao StringBuilder
            if (!isNewOperation && currentInput.length() > 0) {
                currentInput.append(" " + buttonText + " ");
                txtOperacaoAtual.append(" " + buttonText + " ");
                isNewOperation = true; // Define isNewOperation para true após adicionar o operador
            }
        };

        for (int id : operatorButtonIds) {
            findViewById(id).setOnClickListener(listener);
        }
    }

    private void setEqualsButtonListener() {
        findViewById(R.id.btnIgual).setOnClickListener(v -> {
            String expression = currentInput.toString().replace('x', '*').replace('÷', '/').replace(',', '.');
            try {
                float result = evaluateExpression(expression);
                txtOperacaoAtual.setText(formatResult(result));
                txtResultado.setText(""); // Mostra o resultado final no txtResultado
                currentInput.setLength(0);
                currentInput.append(result);
                isNewOperation = false;
            } catch (Exception e) {
                Log.e("Calculator", "Erro durante avaliação da expressão: " + expression, e); // Log de erro
                txtOperacaoAtual.setText("Erro");
                txtResultado.setText("");
                currentInput.setLength(0);
                isNewOperation = true;
            }
        });
    }

    private boolean hasOperatorFollowedByNumber(String expression) {
        // Verifica se a expressão contém um operador seguido de um número
        return expression.matches(".*[+\\-*/]\\s*\\d+");
    }

    private void setDecimalButtonListeners() {
        int commaButtonId = R.id.btnVirgula;

        View.OnClickListener listener = v -> {
            // Verifica se já existe uma vírgula no currentInput
            if (!currentInput.toString().contains(",")) {
                Button button = (Button) v;
                String buttonText = button.getText().toString();

                if (isNewOperation) {
                    isNewOperation = false;
                }
                currentInput.append(buttonText);
                txtOperacaoAtual.append(buttonText);
            }
        };

        findViewById(commaButtonId).setOnClickListener(listener);
    }

    private void setClearButtonListener() {
        findViewById(R.id.btnClearAll).setOnClickListener(v -> {
            currentInput.setLength(0);
            txtOperacaoAtual.setText("");
            txtResultado.setText("");
            isNewOperation = true;
            openParenthesesCount = 0; // Reinicia o contador de parênteses
        });
    }

    private void setPercentageButtonListener() {
        findViewById(R.id.btnPorcentagem).setOnClickListener(v -> {
            if (currentInput.length() > 0) {
                float percentage = Float.parseFloat(currentInput.toString().replace(',', '.')) / 100;
                txtOperacaoAtual.setText(formatResult(percentage));
                currentInput.setLength(0);
                currentInput.append(percentage);
                isNewOperation = true;
            }
        });
    }

    private void setParenthesesButtonListener() {
        findViewById(R.id.btnParenteses).setOnClickListener(v -> {
            if (isNewOperation || currentInput.length() == 0 || isOpenParenthesesLast()) {
                // Adiciona parênteses abertos
                currentInput.append("(");
                txtOperacaoAtual.append("(");
                openParenthesesCount++;
            } else if (openParenthesesCount > 0 && !isOpenParenthesesLast() && !isOperatorLast()) {
                // Adiciona parênteses fechados
                currentInput.append(")");
                txtOperacaoAtual.append(")");
                openParenthesesCount--;
            }
            isNewOperation = false;
        });
    }

    // Método para verificar se o último caractere é um parêntese aberto
    private boolean isOpenParenthesesLast() {
        String input = currentInput.toString().trim();
        return input.endsWith("(");
    }

    // Método para verificar se o último caractere é um operador
    private boolean isOperatorLast() {
        String input = currentInput.toString().trim();
        return input.endsWith("+") || input.endsWith("-") || input.endsWith("*") || input.endsWith("/");
    }

    // Método para avaliar a expressão matemática
    private float evaluateExpression(String expression) {
        ArrayList<String> postfix = infixToPostfix(expression);
        return evaluatePostfix(postfix);
    }

    private ArrayList<String> infixToPostfix(String expression) {
        Stack<String> stack = new Stack<>();
        ArrayList<String> postfix = new ArrayList<>();
        // Divide a expressão em tokens corretos
        String[] tokens = expression.split("(?<=op)|(?=op)".replace("op", "[-+*/()]"));

        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;
            if (isNumber(token)) {
                postfix.add(token);
            } else if (token.equals("(")) {
                stack.push(token);
            } else if (token.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    postfix.add(stack.pop());
                }
                if (!stack.isEmpty() && stack.peek().equals("(")) {
                    stack.pop(); // Remove o '('
                } else {
                    Log.e("InfixToPostfix", "Parênteses desbalanceados na expressão.");
                    throw new IllegalArgumentException("Parênteses desbalanceados na expressão.");
                }
            } else if (isOperator(token)) {
                while (!stack.isEmpty() && precedence(stack.peek()) >= precedence(token)) {
                    postfix.add(stack.pop());
                }
                stack.push(token);
            } else {
                Log.e("InfixToPostfix", "Token não reconhecido: " + token);
            }
        }

        while (!stack.isEmpty()) {
            String top = stack.pop();
            if (top.equals("(") || top.equals(")")) {
                Log.e("InfixToPostfix", "Parênteses desbalanceados na expressão.");
                throw new IllegalArgumentException("Parênteses desbalanceados na expressão.");
            }
            postfix.add(top);
        }

        Log.d("InfixToPostfix", "Expressão pós-fixa: " + postfix.toString());
        return postfix;
    }




    private float evaluatePostfix(ArrayList<String> postfix) {
        Stack<Float> stack = new Stack<>();

        for (String token : postfix) {
            if (isNumber(token)) {
                stack.push(Float.parseFloat(token));
            } else if (isOperator(token)) {
                if (stack.size() < 2) {
                    Log.e("EvaluatePostfix", "Operadores e operandos desbalanceados. Token: " + token);
                    throw new IllegalArgumentException("Expressão pós-fixa inválida: operadores e operandos desbalanceados.");
                }
                float b = stack.pop();
                float a = stack.pop();
                switch (token) {
                    case "+":
                        stack.push(a + b);
                        break;
                    case "-":
                        stack.push(a - b);
                        break;
                    case "*":
                        stack.push(a * b);
                        break;
                    case "/":
                        if (b == 0) {
                            Log.e("EvaluatePostfix", "Divisão por zero. Token: " + token);
                            throw new ArithmeticException("Divisão por zero.");
                        }
                        stack.push(a / b);
                        break;
                }
            } else {
                Log.e("EvaluatePostfix", "Token não reconhecido: " + token);
            }
        }

        if (stack.size() != 1) {
            Log.e("EvaluatePostfix", "Expressão pós-fixa inválida: operadores e operandos desbalanceados. Tamanho da pilha: " + stack.size());
            throw new IllegalArgumentException("Expressão pós-fixa inválida: operadores e operandos desbalanceados.");
        }

        return stack.pop();
    }


    private boolean isNumber(String token) {
        try {
            Float.parseFloat(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("x") || token.equals("÷") || token.equals("*") || token.equals("/");
    }

    private int precedence(String operator) {
        switch (operator) {
            case "+":
            case "-":
                return 1;
            case "x":
            case "÷":
            case "*":
            case "/":
                return 2;
            default:
                return -1;
        }
    }

    // Método para formatar o resultado da operação
    private String formatResult(float result) {
        if (result == (long) result) {
            return String.format("%d", (long) result);
        } else {
            return String.format("%s", result);
        }
    }

    // Outros métodos (setNumberButtonListeners, setOperatorButtonListeners, setDecimalButtonListeners, etc.) continuam da mesma forma

}
