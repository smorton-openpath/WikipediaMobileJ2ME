/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.components;
import com.HTMLParser;
import com.Utilities;
import com.mainMIDlet;

import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Label;
import com.sun.lwuit.io.NetworkManager;
import com.sun.lwuit.io.services.ImageDownloadService;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;

import java.util.Vector;
import java.util.Stack;

/**
 *
 * @author caxthelm
 */

public class HTMLComponentItem extends ComponentItem {
    private final int STYLE_BOLD = 1;
    private final int STYLE_ITALIC = 2;
    private final int STYLE_HEADER = 4;
    private final int STYLE_LINK = 8;
    
    public HTMLComponentItem() {
        super(COMP_HTMLTEXT);
    }
    public Component createComponent(String _sText) {
        m_cComponent = HTMLParser.parseHtml(_sText, false);
        return m_cComponent;
    }//end createComponent(String _sText)
    
    public Component createComponent(Vector _vTags, Vector _tableVector) {
        m_cComponent = HTMLParser.parseHtml(_vTags, _tableVector, false);
        return m_cComponent;
    }//end createComponent(Vector _vTags, Vector _tableVector)
    
}

