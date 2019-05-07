package it.polimi.se2019.controller.events;

import it.polimi.se2019.controller.EventVisitable;
import it.polimi.se2019.controller.EventVisitor;
import it.polimi.se2019.view.View;

public class ConnectionRequest implements EventVisitable {

    String username;
    String salt;
    String password;
    boolean existingGame;
    String mode;
    View vv;

    public ConnectionRequest(String username, String salt, String password, boolean existingGame, String mode) {
        this.username = username;
        this.salt = salt;
        this.password = password;
        this.existingGame = existingGame;
        this.mode = mode;
    }

    public void accept(EventVisitor visitor) {
        visitor.visit(this);
    }


    public String getMode() {
        return mode;
    }

    public void setVv(View vv) {
        this.vv = vv;
    }

    public View getVv() {
        return vv;
    }

    public boolean getExistingGame() {
        return existingGame;
    }

    public String getToken() {
        if (!(username.contains("$") || password.contains("$")))
            return username + "$" + salt + "$" + (password+salt).hashCode();
        else
            throw new IllegalArgumentException();
    }
}
