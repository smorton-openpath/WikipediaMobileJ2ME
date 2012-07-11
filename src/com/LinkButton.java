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
    String m_sOtherInfo;
        
    public LinkButton() {
        super();
        
    }//end LinkButton()
    
    public LinkButton(String _sText, String _sLink) {
        super(_sText);
        setCommand(_sText);
        m_sLink = _sLink;
    }//end LinkButton(String _sText, String _sLink)
    
    public void setCommand(String _sText) {
        Command test = new Command(_sText, com.pages.BasePage.COMMAND_LINK);
        this.setCommand(test);
    }//end setCommand(String _sText)
    
    
    public void setOtherInfo(String _sOtherInfo) {
        m_sOtherInfo = _sOtherInfo;
    }//end setOtherInfo(String _sOtherInfo)
    
    public String getOtherInfo() {
        return m_sOtherInfo;
    }//end getOtherInfo()
    
    public String getLink() {
        return m_sLink;
    }//end getLink() 
}
