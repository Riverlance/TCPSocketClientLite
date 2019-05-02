package com.tcpsocketclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ProtocolParser implements Runnable {
    // Needed stuffs
    private SharedPreferences sp;
    private SharedPreferences.Editor spe;
    // Allows to use UI within this background task
    // This is needed because this class is executed in another thread that is different of the main UI thread
    Handler handler;

    public ProtocolParser() {
        // Needed stuffs
        sp = MainActivity.mainActivity.getSharedPreferences(MainActivity.APP_NAME, Context.MODE_PRIVATE);
        spe = sp.edit();
        handler = new Handler();
    }

    @Override
    public void run() {
        // Params to listen the server
        final int port = sp.getInt("port", MainActivity.DEFAULT_PORT);

        try {
            // Socket connection
            ServerSocket serverSocket = new ServerSocket(port);

            while (true) {
                // Stream
                Socket socket = serverSocket.accept();
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                // Basic data
                short opcode = dataInputStream.readShort(); // Opcode Server to Client
                final String username = dataInputStream.readUTF();

                if (opcode == MainActivity.OPCODE_STC_SENDMESSAGE) {
                    final String message = dataInputStream.readUTF();

                    // Execute in UI
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Write on log
                            MainActivity.mainActivity.log(message);
                        }
                    });

                } else if (opcode == MainActivity.OPCODE_STC_SELFDISCONNECT) {
                    // Do nothing

                } else if (opcode == MainActivity.OPCODE_STC_VIEWUSERS) {
                    final String output = dataInputStream.readUTF();

                    // Execute in UI
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Write on log
                            MainActivity.mainActivity.log(output);
                        }
                    });

                } else if (opcode == MainActivity.OPCODE_STC_RENAMESELF) {
                    String newUsername = dataInputStream.readUTF();
                    spe.putString("username", newUsername);
                    spe.commit();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.mainActivity.getApplicationContext(), "Renomeado com sucesso.", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else if (opcode == MainActivity.OPCODE_STC_TOAST) {
                    final String message = dataInputStream.readUTF();

                    // Execute in UI
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.mainActivity.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            // Never happens because Client is always listening
            // dataInputStream.close();
            // serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
