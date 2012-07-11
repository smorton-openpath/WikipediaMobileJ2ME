/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.util.Vector;
import com.sun.lwuit.Component;
import com.sun.lwuit.html.DefaultHTMLCallback;
import com.sun.lwuit.html.HTMLComponent;
import com.sun.lwuit.Container;
import com.sun.lwuit.Label;
import com.sun.lwuit.Button;
import com.sun.lwuit.layouts.BoxLayout;

/**
 *
 * @author caxthelm
 */

public class HTMLComponentItem extends ComponentItem {
    
    public HTMLComponentItem(String _sText) {
        super(COMP_HTMLTEXT);
        Container cTextComp = new Container();//new HTMLRequestHandler());
        //cTextComp.setWidth(500);
        cTextComp.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        Vector vComponents = Utilities.chopHTMLString(_sText);
        System.out.println("number of components: "+ vComponents.size());
        for(int i = 0; i < vComponents.size(); i++)
        {
            Object oComp = vComponents.elementAt(i);
            if(oComp instanceof Label) { 
                ((Label)oComp).setUIID("No_Margins");
            }else if(oComp instanceof Button) { 
                //((Button)oComp).setUIID("HTMLLink");
            }
            cTextComp.addComponent((Component)oComp);
        }
        
        cTextComp.getUnselectedStyle().setMargin(0, 0, 0, 0);
        cTextComp.getSelectedStyle().setMargin(0, 0, 0, 0);
        m_cComponent = cTextComp;
    }  
}

