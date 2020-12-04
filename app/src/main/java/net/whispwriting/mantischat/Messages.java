package net.whispwriting.mantischat;

public class Messages {

    private String message, type;

    public Messages(String message, String type){
        this.message = message;
        this.type = type;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type = type;
    }
}
