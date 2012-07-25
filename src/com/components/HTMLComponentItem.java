/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.components;
import com.Utilities;

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
    
    public HTMLComponentItem() {
        super(COMP_HTMLTEXT);
    }
    public Component createComponent(String _sText) {
        Container cTextComp = new Container();//new HTMLRequestHandler());
        //cTextComp.setWidth(500);
        cTextComp.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        Vector vComponents = chopHTMLString(_sText);
        for(int i = 0; i < vComponents.size(); i++)
        {
            Object oComp = vComponents.elementAt(i);
            boolean addComp = true;
            
            //We may get empty containers, if that happens we strip them out to save space.
            if(oComp instanceof Container && ((Container)oComp).getComponentCount() <= 0) {
                addComp = false;
            }
            if(addComp) {
                cTextComp.addComponent((Component)oComp);
            }
        }
        
        cTextComp.getUnselectedStyle().setMargin(0, 0, 0, 0);
        cTextComp.getSelectedStyle().setMargin(0, 0, 0, 0);
        m_cComponent = cTextComp;
        return m_cComponent;
    }
    
    
    
    private static Vector chopHTMLString(String _sText) {
        Vector vOutput = new Vector();
        Stack stLastTags = new Stack();
        char[] breakItems = {' ', '\n', '\t', '<'};
        boolean isBold = false;
        boolean isItalic = false;
        boolean isHeader = false;
        int tableIdx = 0;  
        boolean hasLeadingSpace = false;
        
        Container cCurrentCont = new Container();
        vOutput.addElement(cCurrentCont);//Prep the text with a starting container.     
        String sCurrentLink = null;
        while( _sText.length() > 0) {
            String word = "";
            int nextIdx = 0;
            
            //We must always have an active container.
            if(cCurrentCont == null ) {                        
                cCurrentCont = new Container();
                vOutput.addElement(cCurrentCont);
            }
            //TODO: check for email addresses
            if(_sText.charAt(0) == '<') { //Deal with Tags
                //deal with HTML tag.
                boolean isSingleTag = false;
                String tag = "";                                    
                boolean isEndTag = false;
                
                //Get and store the tag identifier
                int tagIdx = _sText.indexOf(' ');
                int endIdx = _sText.indexOf('>');
                if(tagIdx != -1 && tagIdx < endIdx){
                    endIdx = tagIdx;
                }
                if(endIdx == -1) {//Something went massively wrong if we have no end >
                    break;
                }
                if(_sText.charAt(1) == '/') {
                    tag = _sText.substring(2, endIdx);
                    isEndTag = true;
                    stLastTags.pop();
                }else {                    
                    tagIdx = _sText.indexOf('/');
                    if(tagIdx != -1 && tagIdx < endIdx){
                        endIdx = tagIdx;
                        isSingleTag = true;
                    }
                    tag = _sText.substring(1, endIdx);
                    if(!isSingleTag) {
                        stLastTags.push(tag);
                    }
                }
                if(tag.equalsIgnoreCase("a")) { //images & links
                    if(isEndTag) {
                        sCurrentLink = null;
                    }else {
                        int startImgIdx = _sText.indexOf("title=\"")+7;
                        int endImgIdx = _sText.indexOf("\"", startImgIdx);
                        String titleText = _sText.substring(startImgIdx, endImgIdx);

                        startImgIdx = _sText.indexOf("href=\"")+6;
                        endImgIdx = _sText.indexOf("\"", startImgIdx);
                        String linkText = _sText.substring(startImgIdx, endImgIdx);

                        sCurrentLink = linkText;
                    }
                } else if(tag.equalsIgnoreCase("b")) { //divs we don't need.
                    if(isEndTag) {
                        isBold = false;
                    }else {
                        isBold = true;
                    }
                } else if(tag.equalsIgnoreCase("div")) { //divs we don't need.
                } else if(tag.equalsIgnoreCase("img")) { //images (often found in links)
                    int startImgIdx = _sText.indexOf("alt=\"")+5;
                    int endImgIdx = _sText.indexOf("\"", startImgIdx);
                    String altText = _sText.substring(startImgIdx, endImgIdx);
                    
                    startImgIdx = _sText.indexOf("src=\"")+5;
                    endImgIdx = _sText.indexOf("\"", startImgIdx);
                    String srcText = _sText.substring(startImgIdx, endImgIdx);
                    
                    int width = -1;
                    int height = -1;
                    startImgIdx = _sText.indexOf("width=\"")+7;
                    if(startImgIdx > -1) {
                        endImgIdx = _sText.indexOf("\"", startImgIdx);
                        width = Integer.parseInt(_sText.substring(startImgIdx, endImgIdx));
                    }
                    
                    startImgIdx = _sText.indexOf("height=\"")+8;
                    if(startImgIdx > -1) {
                        endImgIdx = _sText.indexOf("\"", startImgIdx);
                        height = Integer.parseInt(_sText.substring(startImgIdx, endImgIdx));
                    }
                    
                    if(altText.length() <= 0) {
                        altText = srcText.substring(srcText.lastIndexOf('/') + 1);
                        altText = altText.replace('_', ' ');
                    }
                    //If the image is less than 51 pixels it is likely an icon; so just go ahead and show it.
                    if(width > 0 && width <= 50 && height > 0 && height <= 50 && tableIdx <= 0) {
                        Label newLabel = new Label();
                        newLabel.setUIID("no_MarginsTransparent");
                        ImageDownloadService img = new ImageDownloadService("http:"+srcText, newLabel);
                        NetworkManager.getInstance().addToQueue(img);
                        cCurrentCont.addComponent( newLabel);
                    }else if(tableIdx <= 0) {
                        //System.out.println("adding image: "+altText+", "+srcText);
                        ImageButton newLink = new ImageButton(altText, srcText);
                        //Add button to the list, reset the container
                        cCurrentCont = null;
                        vOutput.addElement(newLink);
                    }
                    //newLink.setUIID("LabelButtonLink");
                    
                }else if(tag.equalsIgnoreCase("p")) {//Paragraphs are new containers                    
                    cCurrentCont = null;
                }else if(tag.equalsIgnoreCase("h2")) {//headers are new containers
                    cCurrentCont = null;
                    if(isEndTag) {
                        isHeader = false;
                    }else {
                        isHeader = true;
                        //TODO: check the previous tag and see if it is ul or ol
                    }
                }else if(tag.equalsIgnoreCase("li")) {//points are new containers
                    cCurrentCont = null;
                }else if(tag.equalsIgnoreCase("table")) {
                    cCurrentCont = null;
                    if(isEndTag) {
                        tableIdx--;
                    }else {
                        tableIdx++;
                    }
                }else if(tag.equalsIgnoreCase("ul")) {//Paragraphs are new containers
                }
                nextIdx = _sText.indexOf(">")+1;//+1 to take off the >
                //System.out.println("html: "+_sText.substring(0, nextIdx));
            }else  if(_sText.charAt(0) == ' ') { //Deal with leading spaces
                hasLeadingSpace = true;
                _sText = _sText.trim();
                continue;
            }else {//Deal with text
                int breakItem = 0;
                for(int i = 0; i < breakItems.length; i++) {
                    int idx = _sText.indexOf(breakItems[i]);
                    
                    if(nextIdx <= 0 || (idx > 0 && idx < nextIdx)) {
                        nextIdx = idx;
                        breakItem = i;
                    }
                }
                String text = "";
                if(hasLeadingSpace) {
                    text += " ";
                    hasLeadingSpace = false;
                }
                
                //if we broke on a space, keep that space.
                if(breakItems[breakItem] == ' ') {
                    nextIdx++;
                }
                if(nextIdx > 0) {
                    text += _sText.substring(0, nextIdx);
                }else {
                    text += _sText;
                }
                //System.out.println("label: "+text);
                //text = text.trim();
                //text = "\n"+com.sun.lwuit.html.HTMLUtils.encodeString(text);
                Component newComp = null;
                if(sCurrentLink != null && sCurrentLink.length() > 0) {
                    LinkButton newLink = new LinkButton(text, sCurrentLink);
                    newLink.setUIID("LabelButtonLink");
                    newComp = newLink;
                }else {
                    Label newLabel = new Label(text);
                    newLabel.setUIID("No_Margins");
                    newComp = newLabel;
                }
                
                if(isHeader) {
                }
                if(isBold) {
                }
                if(cCurrentCont != null) {
                    if(tableIdx <= 0) {//Axthelm - turning tables off for now.
                        //System.out.println("adding: "+text);
                        cCurrentCont.addComponent(newComp);
                    }
                }else {                      
                    System.out.println("adding to main");
                    vOutput.addElement(newComp);
                }
            }
            if(nextIdx == -1) {
                break;
            }
            _sText = _sText.substring(nextIdx);            
            //_sText = _sText.trim();
        }//end while( _sText.length() > 0)
        return vOutput;
    }//end chopHTMLString(String _sText)
    
    private Container parseHtmlString(String _sText) {
        int iCurrentIdx = 0;
        int iNextTagIdx = 0;
        int iNextTagLength = -1;        
        int iEndNextTagIdx = 0;
        return null;
        
    }//end  parseHtmlString(String _sText)
    private Component parseText(String _sText, int styleMask) {
        
        Label newLabel = new Label(_sText);
        newLabel.setUIID("No_Margins");
        return (Component)newLabel;
    }//end parseText(String _sText)
    
    private Component parseLink(String _sText, int _iStyleMask) {
        return null;
    }//end parseLink(String _sText)
    
    private Component parseBold(String _sText, int _iStyleMask) {
        _iStyleMask += STYLE_BOLD;
        return null;
    }//end parseBold(String _sText)
    
    private Component parseHeader(String _sText, int _iStyleMask) {
        _iStyleMask += STYLE_HEADER;
        return null;
    }//end parseHeader(String _sText)
    
    private Component parseItalic(String _sText, int _iStyleMask) {
        _iStyleMask += STYLE_ITALIC;
        return null;
    }//end parseItalic(String _sText)
    
    private Component parseList(String _sText, int _iStyleMask) {
        return null;
    }//end parseList(String _sText)
    
    private Component parseParagraph(String _sText, int _iStyleMask) {
        return null;
    }//end parseParagraph(String _sText)
    
    private Component parseTable(String _sText, int _iStyleMask) {
        return null;
    }//end parseTable(String _sText)
}

