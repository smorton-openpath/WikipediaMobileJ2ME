/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.components;

import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Label;
import com.sun.lwuit.layouts.BoxLayout;

import java.util.Vector;
import java.util.Stack;

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
        Vector vComponents = chopHTMLString(_sText);
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
    
    
    
    private static Vector chopHTMLString(String _sText) {
        Vector vOutput = new Vector();
        Stack stLastTags = new Stack();
        char[] breakItems = {' ', '\n', '\t', '<'};
        boolean isBold = false;
        boolean isItalic = false;
        boolean isHeader = false;
        boolean hasLeadingSpace = false;
        
        Container cCurrentCont = new Container();
        vOutput.addElement(cCurrentCont);//Prep the text with a starting container.
        Container cCurrentTable = null;       
        String sCurrentLink = null;
        while( _sText.length() > 0) {
            String word = "";
            int nextIdx = 0;
                
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
                    
                    ImageButton newLink = new ImageButton(altText, srcText);
                    //newLink.setUIID("LabelButtonLink");
                    if(cCurrentCont != null) {
                        cCurrentCont.addComponent(newLink);
                    }else {
                        vOutput.addElement(newLink);
                    }
                }else if(tag.equalsIgnoreCase("p")) {//Paragraphs are new containers
                    if(isEndTag) {
                        cCurrentCont = null;
                    }else {
                        cCurrentCont = new Container();
                        vOutput.addElement(cCurrentCont);
                    }
                }else if(tag.equalsIgnoreCase("h2")) {//Paragraphs are new containers
                    if(isEndTag) {
                        cCurrentCont = null;
                        isHeader = false;
                    }else {
                        cCurrentCont = new Container();
                        vOutput.addElement(cCurrentCont);
                        isHeader = true;
                        //TODO: check the previous tag and see if it is ul or ol
                    }
                }else if(tag.equalsIgnoreCase("li")) {//Paragraphs are new containers
                    if(isEndTag) {
                        cCurrentCont = null;
                    }else {
                        cCurrentCont = new Container();
                        vOutput.addElement(cCurrentCont);
                        //TODO: check the previous tag and see if it is ul or ol
                    }
                }else if(tag.equalsIgnoreCase("table")) {
                    if(isEndTag) {
                        //once done with the table put a new container back.
                        cCurrentTable = null;
                        cCurrentCont = new Container();
                        vOutput.addElement(cCurrentCont);
                    }else {
                        //Take away the old container and add in a new table.
                        cCurrentCont = null;
                        cCurrentTable = new Container();
                        //vOutput.addElement(cCurrentTable);
                        //TODO: check the previous tag and see if it is ul or ol
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
                    cCurrentCont.addComponent(newComp);
                }else if(cCurrentTable != null) {
                    cCurrentTable.addComponent(newComp);
                }else {
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
}

