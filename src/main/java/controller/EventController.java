package controller;

public class EventController {
    private Visitor curVisitor = null;

    public void setCurVisitor(Visitor visitor){
        curVisitor=visitor;
    }

    public Visitor getCurVisitor(){
        return curVisitor;
    }
}
