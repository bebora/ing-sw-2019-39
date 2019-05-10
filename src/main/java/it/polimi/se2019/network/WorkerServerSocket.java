package it.polimi.se2019.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.se2019.controller.EventVisitable;
import it.polimi.se2019.controller.LobbyController;
import it.polimi.se2019.controller.events.ConnectionRequest;
import it.polimi.se2019.controller.events.EventDeserializer;
import it.polimi.se2019.controller.events.IncorrectEvent;
import it.polimi.se2019.model.updatemessage.UpdateVisitable;
import it.polimi.se2019.view.View;
import it.polimi.se2019.view.VirtualView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class WorkerServerSocket extends Thread {
    private Socket socket;
    private View virtualView;
    private GsonBuilder gsonBuilder;
    BlockingQueue queue = new LinkedBlockingDeque();

    public WorkerServerSocket(Socket socket, LobbyController lobbyController) {
        this.socket = socket;
        this.gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(EventVisitable.class, new EventDeserializer());
        Gson gson = gsonBuilder.create();
        String json;
        try {
            BufferedReader jsonReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            json = jsonReader.readLine();
        }
        catch (IOException e) {
            //TODO LOGGER
            throw new UnsupportedOperationException();
        }
        EventVisitable event = gson.fromJson(json, EventVisitable.class);
        try {
            ConnectionRequest connection = (ConnectionRequest) event;
            virtualView = new VirtualView(lobbyController);
            //TODO SET INTERFACE TO SEND UPDATES
            String username = connection.getUsername();
            String password = connection.getPassword();
            String mode = connection.getMode();
            boolean signingUp = connection.getExistingGame();
            if (signingUp)
                lobbyController.connectPlayer(username,password,mode, virtualView);
            else
                ;
                //TODO RECONNECT;
        }
        catch (IncorrectEvent e){
            event = null;
            //TODO LOG INCORRECT EVENT
        }
        if (event == null) {
            //TODO send update popup, wrong message
            try {
                socket.close();
            }
            catch (IOException e) {
                //TODO LOGGER
                throw new AuthenticationErrorException();
            }
        }
    }


    @Override
    public void run() {
        //TODO run listener and sender
    }

    public void update(UpdateVisitable update) {
        //TODO serialize update

        /*try {
            //queue.put(serializedUpdate);
        }
        catch (InterruptedException e) {
            //TODO insert logger class to log exceptions
        }
    }*/
    }

    private class Updater extends Thread {
            @Override
            public void run() {
                //TODO take message from queue and send them using oos
            }
    }

    private class Listener extends Thread {
        @Override
        public void run() {
            //TODO Get messages, parse them and use messageHandler relative visitor
        }
    }


}
