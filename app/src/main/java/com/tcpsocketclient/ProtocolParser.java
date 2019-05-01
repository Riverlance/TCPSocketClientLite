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
    Handler handler = new Handler();

    public ProtocolParser() {
        // Needed stuffs
        sp = MainActivity.mainActivity.getSharedPreferences(MainActivity.APP_NAME, Context.MODE_PRIVATE);
        spe = sp.edit();
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

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.mainActivity.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                /*
                if (opcode == MainActivity.OPCODE_STC_SELFCONNECT) {
                    // Execute in UI
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.mainActivity.localLogin(username);
                        }
                    });

                } else if (opcode == MainActivity.OPCODE_STC_SELFDISCONNECT) {
                    // Execute in UI
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.mainActivity.localLogout();
                        }
                    });

                } else if (opcode == MainActivity.OPCODE_STC_UPDATEDUSERSLIST) {
                    int size = dataInputStream.readInt();

                    final ArrayList<String> tempUsernames = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        String _username = dataInputStream.readUTF();
                        tempUsernames.add(_username);
                    }

                    // Execute in UI
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Clear usersMap
                            MainActivity.mainActivity.usersMap = new HashMap<>();

                            for (String tempUsername : tempUsernames) {
                                User tempUser = new User(tempUsername);
                                MainActivity.mainActivity.usersMap.put(tempUsername, tempUser);

                                // Update chat (add missing users to chat list)
                                if (MainActivity.usersListActivity != null) {
                                    if (MainActivity.mainActivity.findChatUser(tempUsername) == -1) {
                                        MainActivity.mainActivity.addChatUser(tempUser, false);
                                    }
                                }
                            }
                            // Update chat (rows)
                            if (MainActivity.usersListActivity != null) {
                                for (int i = 0; i < MainActivity.mainActivity.chatsList.size(); i++) {
                                    String chatUsername = MainActivity.mainActivity.chatsList.get(i).username;
                                    boolean isOnline = MainActivity.mainActivity.usersMap.containsKey(chatUsername);
                                    MainActivity.usersListActivity.updateRow(i, isOnline);
                                }
                            }
                            MainActivity.mainActivity.notifyChatUsersDataChanged();
                        }
                    });

                } else if (opcode == MainActivity.OPCODE_STC_SENDMESSAGE) {
                    System.out.println("recebeu");
                    if (MainActivity.mainActivity.messageListActivity != null) {
                        System.out.println("ta na messagelist");
                        String _username = dataInputStream.readUTF(); // Who sent
                        String targetUsername = dataInputStream.readUTF(); // Target username (or if is global chat)
                        String message = dataInputStream.readUTF();

                        User _user = MainActivity.mainActivity.usersMap.get(_username);
                        if (_user == null) {
                            System.out.println("target existe");
                            return;
                        }

                        Message messageObj = new Message();
                        messageObj.username = _username;
                        messageObj.value = message;
                        messageObj.sentByMe = _username.equals(username);
                        _user.messages.add(messageObj);

                        System.out.println(String.format("Username: '%s' | Message: '%s' | SentByMe: %s", _username, message, messageObj.sentByMe ? "true" : "false"));

                        // Execute in UI
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //MainActivity.mainActivity.notifyChatMessagesDataChanged();
                            }
                        });
                    }

                } else if (opcode == MainActivity.OPCODE_STC_FRIENDLOGGEDIN) {
                    final String friendUsername = dataInputStream.readUTF();

                    final User tempUser = new User(friendUsername);

                    if (!MainActivity.usersMap.containsKey(friendUsername)) {
                        MainActivity.usersMap.put(friendUsername, tempUser);
                    }

                    // Execute in UI
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Add friend to chat list
                            MainActivity.mainActivity.addChatUser(tempUser);
                            // Toast.makeText(MainActivity.mainActivity.getApplicationContext(), String.format("My friend logged in: %s", friendUsername), Toast.LENGTH_SHORT).show();

                            int friendChatUserPos = MainActivity.mainActivity.findChatUser(friendUsername);
                            if (friendChatUserPos != -1) {
                                MainActivity.usersListActivity.updateRow(friendChatUserPos, true);
                            }
                        }
                    });

                } else if (opcode == MainActivity.OPCODE_STC_FRIENDLOGGEDOUT) {
                    final String friendUsername = dataInputStream.readUTF();

                    final User friendUser = MainActivity.usersMap.get(friendUsername);
                    if (friendUser != null) {
                        MainActivity.usersMap.remove(friendUsername);
                    }

                    // Execute in UI
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Remove friend from chat list (we don't want this, so leave it commented)
                            // MainActivity.mainActivity.removeChatUser(friendUsername);
                            // Toast.makeText(MainActivity.mainActivity.getApplicationContext(), String.format("My friend logged out: %s", friendUsername), Toast.LENGTH_SHORT).show();

                            int friendChatUserPos = MainActivity.mainActivity.findChatUser(friendUsername);
                            if (friendChatUserPos != -1) {
                                MainActivity.usersListActivity.updateRow(friendChatUserPos, false);
                            }
                        }
                    });
                }
                */
            }

            // Never happens because Client is always listening
            // dataInputStream.close();
            // serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
