package bean;

import java.awt.Desktop.Action;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author fabricio
 */
public class ChatMessage implements Serializable {

    private String name;
    private String para;
    private String text;
    private String nameReserved;
    private Set<String> setOnlines = new HashSet<String>();
    private Action action;

    public enum Action {

        CONNECT, DISCONNECT, SEND_ONE, SEND_ALL, USERS_ONLINE
    }

    public String getName() {
        return name;
    }
    
    public String getPara(){
        return this.para;
    }

    public String getText() {
        return text;
    }

    public String getNameReserved() {
        return nameReserved;
    }

    public Set<String> getSetOnlines() {
        return setOnlines;
    }

    public Action getAction() {
        return action;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setPara(String para){
        this.para = para;
    }
    
    public void setText(String text) {
        this.text = text;
    }

    public void setNameReserved(String nameReserved) {
        this.nameReserved = nameReserved;
    }

    public void setSetOnlines(Set<String> setOnlines) {
        this.setOnlines = setOnlines;
    }

    public void setAction(Action action) {
        this.action = action;
    }

}
