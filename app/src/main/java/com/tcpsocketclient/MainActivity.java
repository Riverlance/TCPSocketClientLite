package com.tcpsocketclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    // Constants
    public static final String APP_NAME = "TCP Socket Client";
    public static final int DEFAULT_PORT = 7171;
    public static final int DEFAULT_TIMETOKICK = 60000; // Has a copy on server
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
    public static final short OPCODE_STC_TOAST = 5;

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
    public ScrollView scrollView;

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
        scrollView = findViewById(R.id.scrollView);
        loadDefaultValues();
    }

    public void onClickSendButton(View view) {
        // Get values from views
        String serverIP = ipEditText.getText().toString();
        String port = portEditText.getText().toString();
        String message = inputEditText.getText().toString();

        // Clear input
        inputEditText.setText("");

        // Basic errors
        String err = null;
        if (serverIP.equals("")) {
            err = "Digite o IP do servidor.";
        } else if (port.equals("")) {
            err = "Digite a Porta.";
        } else if (message.equals("")) {
            err = "Digite um comando.";
        }
        if (err != null) {
            Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            return;
        }

        // Saving data at app preferences
        spe.putString("serverIP", serverIP);
        spe.putInt("port", !port.equals("") ? Integer.parseInt(port) : DEFAULT_PORT);
        spe.commit();

        String values[] = message.split(" ");

        if (values.length > 0) {
            String command = values[0];
            String username = sp.getString("username", "");

            // If is command "rename" or (other command and has an username)
            if (command.equals("rename") || !username.equals("")) {

                // Command: bye
                if (command.equals("bye")) {
                    //Toast.makeText(this, String.format("Comando: '%s'", command), Toast.LENGTH_SHORT).show();

                    ProtocolSender protocolSender = new ProtocolSender();
                    protocolSender.execute(String.format("%d", OPCODE_CTS_SELFDISCONNECT));

                // Command: send
                // send -all <message>
                // send -user <targetUsername> <message>
                } else if (command.equals("send")) {
                    if (values.length == 1 || (values.length == 2 && !values[1].equals("-all") && !values[1].equals("-user"))) {
                        err = "Deve-se incluir -all ou -user como parâmetro do comando.";
                    } else if (values.length == 2 && values[1].equals("-user")) {
                        err = "Deve-se incluir o nome do usuário após o parâmetro.";
                    } else if (values.length == 2 && values[1].equals("-all") || values.length == 3 && values[1].equals("-user")) {
                        err = "Deve-se incluir uma mensagem no final.";
                    } else {
                        String param = values[1];

                        if (param.equals("-all")) {
                            String value = values[2]; // Message
                            // Toast.makeText(this, String.format("Comando: '%s' | Param: '%s' | Value: '%s'", command, param, value), Toast.LENGTH_SHORT).show();

                            ProtocolSender protocolSender = new ProtocolSender();
                            protocolSender.execute(String.format("%d", OPCODE_CTS_SENDMESSAGE), param, value);

                        } else if (param.equals("-user")) {
                            String targetUsername = values[2];
                            String value = values[3]; // Message
                            // Toast.makeText(this, String.format("Comando: '%s' | Param: '%s' | targetUsername: '%s' | Value: '%s'", command, param, targetUsername, value), Toast.LENGTH_SHORT).show();

                            ProtocolSender protocolSender = new ProtocolSender();
                            protocolSender.execute(String.format("%d", OPCODE_CTS_SENDMESSAGE), targetUsername, value);
                        }
                    }

                // Command: list
                } else if (command.equals("list")) {
                    //Toast.makeText(this, String.format("Comando: '%s'", command), Toast.LENGTH_SHORT).show();

                    ProtocolSender protocolSender = new ProtocolSender();
                    protocolSender.execute(String.format("%d", OPCODE_CTS_VIEWUSERS));

                // Command: rename
                // rename <newUsername>
                } else if (command.equals("rename")) {
                    if (values.length == 1) {
                        err = "Deve-se incluir o novo nome após o comando.";
                    } else {
                        String newUsername = values[1];

                        if (!username.equals(newUsername)) {
                            //Toast.makeText(this, String.format("Comando: '%s' | newUsername: '%s'", command, newUsername), Toast.LENGTH_SHORT).show();

                            ProtocolSender protocolSender = new ProtocolSender();
                            protocolSender.execute(String.format("%d", OPCODE_CTS_RENAMESELF), newUsername);

                        } else {
                            err = String.format("Você já está com o nome de usuário '%s'.", username);
                        }
                    }

                } else {
                    err = "Comando inválido.";
                }

            } else {
                err = "Use o comando rename para criar seu nome de usuário.";
            }

        } else {
            err = "Comando inválido.";
        }
        if (err != null) {
            Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void loadDefaultValues() {
        // Loading data from app preferences
        ipEditText.setText(sp.getString("serverIP", ""));
        portEditText.setText(String.format("%d", sp.getInt("port", DEFAULT_PORT)));
    }

    public void scrollLog(final boolean bottom) {
        // Scrolls to down
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(bottom ? ScrollView.FOCUS_DOWN : ScrollView.FOCUS_UP);
            }
        });
    }
    public void scrollLog() {
        scrollLog(true);
    }

    public void log(String string) {
        logTextView.setText(String.format("%s\n%s", logTextView.getText(), string));
        scrollLog();
    }
}
