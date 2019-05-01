package com.tcpsocketclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

public class ProtocolSender extends AsyncTask<String, Void, String> { // <Params, Progress, Result>
    // Needed stuffs
    private SharedPreferences sp;
    // Allows to use UI within this background task
    // This is needed because this class is executed in another thread that is different of the main UI thread
    Handler handler;

    public ProtocolSender() {
        // Needed stuffs
        sp = MainActivity.mainActivity.getSharedPreferences(MainActivity.APP_NAME, Context.MODE_PRIVATE);
        handler = new Handler();
    }

    @Override
    protected String doInBackground(String... strings) {
        // Params to connect to server
        short opcode = Short.parseShort(strings[0]);
        final String serverIP = sp.getString("serverIP", "");
        final int port = sp.getInt("port", MainActivity.DEFAULT_PORT);
        final String username = sp.getString("username", "");

        try {
            // Socket connection and stream
            Socket socket = new Socket(serverIP, port);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            // Basic data
            dataOutputStream.writeShort(opcode); // Operation code
            dataOutputStream.writeUTF(username);
            dataOutputStream.writeUTF(Utils.getIPAddress()); // Self IPv4 (Client)

            if (opcode == MainActivity.OPCODE_CTS_SENDMESSAGE) {
                dataOutputStream.writeUTF(strings[1]); // param or targetUsername
                dataOutputStream.writeUTF(strings[2]); // message

            } else if (opcode == MainActivity.OPCODE_CTS_SELFDISCONNECT) {
                // Do nothing

            } else if (opcode == MainActivity.OPCODE_CTS_VIEWUSERS) {
                // Do nothing

            } else if (opcode == MainActivity.OPCODE_CTS_RENAMESELF) {
                dataOutputStream.writeUTF(strings[1]); // newUsername
            }

            // Close stream and socket connection
            dataOutputStream.close();
            socket.close();

        } catch (ConnectException e) {
            // Execute in UI
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Context context = MainActivity.mainActivity.getApplicationContext();
                    Toast.makeText(context, String.format("Servidor nao encontrado ou ocupado.\nIP: %s (%d)\nUser: %s", serverIP, port, username), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
