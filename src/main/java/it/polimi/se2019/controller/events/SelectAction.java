package it.polimi.se2019.controller.events;

import it.polimi.se2019.controller.Visitable;
import it.polimi.se2019.controller.Visitor;
import it.polimi.se2019.view.ViewAction;

public class SelectAction implements Visitable {
    private ViewAction selectedAction;
    public void accept(Visitor visitor){
        visitor.visit(this);
    }
}