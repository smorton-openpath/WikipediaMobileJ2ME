/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.components;

import com.mainMIDlet;
import com.Utilities;

import com.sun.lwuit.*;
/**
 *
 * @author caxthelm
 */
public class ListComponentItem extends ComponentItem {
    
    
    public ListComponentItem(int _iActionId) {
        super(COMP_LIST);        
        setActionID(_iActionId);
    }
    
    public Component createComponent(String _sText) {
        m_sCompTag = _sText;
        m_cComponent = (Component)mainMIDlet.getBuilder().createContainer(mainMIDlet.getResources(), "ListItem");
        if(m_cComponent != null) {
            Button cButton = (Button)mainMIDlet.getBuilder().findByName("TextButton", (Container)m_cComponent);
            if(cButton != null ) {
                Command newCommand = new Command(_sText, m_iActionId);
                cButton.setCommand(newCommand);
            }
        }
        return m_cComponent;
    }//end createComponent(String _sText)
}
