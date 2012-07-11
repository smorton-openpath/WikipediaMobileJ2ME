/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.html.HTMLCallback;
import com.sun.lwuit.html.HTMLComponent;

/**
 *
 * @author caxthelm
 */
public class LinkButton extends Button {
    
    boolean linkVisited;
    String m_sLink;
    
    private ActionListener m_cActionListener;
    
    public LinkButton() {
        super();
        
    }
    
    public LinkButton(String _sText, String _sLink) {
        super(_sText);
        setCommand(_sText);
        m_sLink = _sLink;
    }
    public void setCommand(String _sText) {
        Command test = new Command(_sText, com.pages.BasePage.COMMAND_LINK);
        this.setCommand(test);
    }
    
    public ActionListener getActionListener() {
        return m_cActionListener;
    }
    
    public String getLink() {
        return m_sLink;
    }
    
    public void setActionListener(ActionListener _cActionListener) {
        if(_cActionListener != null) {
            this.setActionListener(_cActionListener);
        }else {
            this.removeActionListener(m_cActionListener);
        }
        m_cActionListener = _cActionListener;
    }
}
