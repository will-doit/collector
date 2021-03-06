package com.willc.collector.lib.map.event;

import java.util.Collection;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by stg on 17/10/15.
 */

public class FocusMapChangedManager {
    private Collection<FocusMapChangedListener> listeners;

    public FocusMapChangedManager() {
    }

    public void addListener(FocusMapChangedListener listener) {
        if(this.listeners == null) {
            this.listeners = new HashSet();
        }
        this.listeners.add(listener);
    }

    public void removeListener(FocusMapChangedListener listener) {
        if(this.listeners != null) {
            this.listeners.remove(listener);
        }
    }

    public void fireListener() {
        if(this.listeners != null) {
            EventObject event = new EventObject(this);
            this.notifyListeners(event);
        }
    }

    private void notifyListeners(EventObject event) {
        Iterator iter = this.listeners.iterator();

        while(iter.hasNext()) {
            FocusMapChangedListener listener = (FocusMapChangedListener)iter.next();
            listener.doEvent(event);
        }
    }
}
