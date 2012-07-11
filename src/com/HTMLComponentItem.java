/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.util.Vector;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Label;
import com.sun.lwuit.Button;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.events.ActionListener;

/**
 *
 * @author caxthelm
 */

public class HTMLComponentItem extends ComponentItem {
    
    public HTMLComponentItem() {
        super(COMP_HTMLTEXT);
    }
    public Component createComponent(String _sText) {
        Container cTextComp = new Container();//new HTMLRequestHandler());
        //cTextComp.setWidth(500);
        cTextComp.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        Vector vComponents = Utilities.chopHTMLString(_sText);
        for(int i = 0; i < vComponents.size(); i++)
        {
            Object oComp = vComponents.elementAt(i);
            cTextComp.addComponent((Component)oComp);
        }
        
        cTextComp.getUnselectedStyle().setMargin(0, 0, 0, 0);
        cTextComp.getSelectedStyle().setMargin(0, 0, 0, 0);
        m_cComponent = cTextComp;
        return m_cComponent;
    }  
}

