package com.tcpsocketclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    // Constants
    public static final String APP_NAME = "TCP Socket Client";
    public static final int DEFAULT_PORT = 7171;
    public static final int DEFAULT_TIMETOKICK = 20000; // Has a copy on server
    // Opcodes (Operation Codes)
    // CTS - Client to Server
    public static final short OPCODE_CTS_SENDMESSAGE = 1;
    public static final short OPCODE_CTS_SELFDISCONNECT = 2; // Request
    public static final short OPCODE_CTS_VIEWUSERS = 3; // Request
    public static final short OPCODE_CTS_RENAMESELF = 4;
    // STC - Server to Client
    public static final short OPCODE_STC_SENDMESSAGE = 1;
    public static final short OPCODE_STC_SELFDISCONNECT = 2; // Answer
    public static final short OPCODE_STC_VIEWUSERS = 3; // Answer
    public static final short OPCODE_STC_RENAMESELF = 4;

    // Needed stuffs
    public static MainActivity mainActivity;
    private SharedPreferences sp;
    private SharedPreferences.Editor spe;
    public static Thread protocolParserThread;

    // Views
    public EditText ipEditText;
    public EditText portEditText;
    public TextView logTextView;
    public EditText inputEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Needed stuffs
        mainActivity = this;
        sp = getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        spe = sp.edit();
        // Client listening server
        if (protocolParserThread == null) {
            protocolParserThread = new Thread(new ProtocolParser());
            protocolParserThread.start();
        }

        // Views
        ipEditText = findViewById(R.id.ipEditText);
        portEditText = findViewById(R.id.portEditText);
        logTextView = findViewById(R.id.logTextView);
        inputEditText = findViewById(R.id.inputEditText);
        loadDefaultValues();
    }

    public void onClickSendButton(View view) {
        String serverIP = ipEditText.getText().toString();
        String port = portEditText.getText().toString();
        String message = inputEditText.getText().toString();

        String err = null;
        if (serverIP.equals("")) {
            err = "Digite o IP do servidor.";
        } else if (port.equals("")) {
            err = "Digite a Porta.";
        } else if (message.equals("")) {
            err = "Digite uma mensagem.";
        }
        if (err != null) {
            Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            return;
        }

        // Saving data at app preferences
        //spe.putString("serverIP", serverIP);
        //spe.putInt("port", !port.equals("") ? Integer.parseInt(port) : DEFAULT_PORT);
        //spe.commit();
    }

    public void loadDefaultValues() {
        // Loading data from app preferences
        ipEditText.setText(sp.getString("serverIP", ""));
        portEditText.setText(String.format("%d", sp.getInt("port", DEFAULT_PORT)));
    }
}
