package smartparking.poscoict.psj.poscoict_project.SJ_Event;

import lombok.Data;

@Data
public class NotiValueChangeEvent {

    boolean value = false;

    public NotiValueChangeEvent(boolean b){
        value = b;
    }

    private listener l = null;

    public interface listener{
        public void onChange(boolean b);
    }

    public void setChangeListener(listener mListener){
        l = mListener;
    }

    public void somethingChanged(){
        if(l != null){
            l.onChange(value);
        }
    }

}
