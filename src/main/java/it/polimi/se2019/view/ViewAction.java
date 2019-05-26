package it.polimi.se2019.view;

import it.polimi.se2019.model.actions.Action;
import it.polimi.se2019.model.actions.SubAction;

import java.io.Serializable;
import java.util.List;

public class ViewAction implements Serializable {
    int movements;
    boolean reload;
    boolean grab;
    boolean shoot;
    String type;
    public ViewAction(Action a) {
        List<SubAction> subActions = a.getSubActions();
        this.movements = a.getMovements();
        this.reload = subActions.contains(SubAction.RELOAD);
        this.grab = subActions.contains(SubAction.GRAB);
        this.shoot = subActions.contains(SubAction.SHOOT);
        this.type = a.toString();
    }

    public String getType(){
        return this.type;
    }
}
