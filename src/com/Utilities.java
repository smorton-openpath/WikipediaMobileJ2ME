/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.util.Vector;
import java.util.Stack;
import com.LinkButton;

import com.sun.lwuit.Button;
import com.sun.lwuit.Label;
import com.sun.lwuit.Container;
import com.sun.lwuit.Component;
/**
 *
 * @author caxthelm
 */
public class Utilities {
    
    public static String replace(String _sNeedle, String _sReplacement, String _sHaystack) {
        String result = "";
        int index = _sHaystack.indexOf(_sNeedle);
        if(index == 0) {
            result = _sReplacement+_sHaystack.substring(_sNeedle.length());
            return replace(_sNeedle, _sReplacement, result);
        }else if(index > 0) {
            result = _sHaystack.substring(0,index)+ _sReplacement +_sHaystack.substring(index+_sNeedle.length());
            return replace(_sNeedle, _sReplacement, result);
        }else {
            return _sHaystack;
        }
    }//end replace(String needle, String replacement, String haystack)
    
    public static String deletePart(String _sNeedle, String _sHaystack) {
        String result = "";
        int index = _sHaystack.indexOf(_sNeedle);
        while(index > 0) {
            String temp = _sHaystack.substring(0, index);
            temp += _sHaystack.substring(index + _sNeedle.length());
            _sHaystack = temp;
            index = _sHaystack.indexOf(_sNeedle);
        }
        return _sHaystack;
    }//end replace(String needle, String replacement, String haystack)
    
    public static String stripSlash(String _sHaystack) {
        String result = "";
        int index = _sHaystack.indexOf('\\');
        while(index > 0 && index + 1 < _sHaystack.length()) {
            String temp = _sHaystack.substring(0, index);
            switch((char)_sHaystack.charAt(index+1)) {
                case '\"':
                    temp += '\"';
                    break;
                case '\'':
                    temp += '\'';
                    break;
                case 'n':
                    temp += '\n';
                    break;
                case 't':
                    temp += '\t';
                    break;
                case 'r':
                    temp += '\r';
                    break;
                case '\\':
                    temp += '\\';
                    break;
                case '/':
                    temp += '/';
                    break;
            }
            temp += _sHaystack.substring(index + 2);
            _sHaystack = temp;
            index = _sHaystack.indexOf('\\');
        }
        return _sHaystack;
    }//end replace(String needle, String replacement, String haystack)
    
    public static Vector chopHTMLString(String _sText) {
        Vector vOutput = new Vector();
        Stack stLastTags = new Stack();
        char[] breakItems = {' ', '\n', '\t', '<'};
        boolean isBold = false;
        boolean isItalic = false;
        boolean isHeader = false;
        boolean isLink = false;
        boolean hasLeadingSpace = false;
        int iListOrder = -1; //-1 for no list, 0 for unordered list
        Container cCurrentCont = null;
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
                        isLink = false;
                    }else {
                        isLink = true;
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
                    
                    LinkButton newLink = new LinkButton(altText, srcText);
                    newLink.setUIID("SoftButton");
                    System.out.println("style: "+newLink.getUIID());
                    if(cCurrentCont != null) {
                        cCurrentCont.addComponent(newLink);
                    }else {
                        vOutput.addElement(newLink);
                    }
                }else if(tag.equalsIgnoreCase("p")) {//Paragraphs are new containers
                    if(isEndTag) {
                        vOutput.addElement(cCurrentCont);
                        cCurrentCont = null;
                    }else {
                        cCurrentCont = new Container();
                    }
                }else if(tag.equalsIgnoreCase("h2")) {//Paragraphs are new containers
                    if(isEndTag) {
                        vOutput.addElement(cCurrentCont);
                        cCurrentCont = null;
                        isHeader = false;
                    }else {
                        cCurrentCont = new Container();
                        isHeader = true;
                        //TODO: check the previous tag and see if it is ul or ol
                    }
                }else if(tag.equalsIgnoreCase("li")) {//Paragraphs are new containers
                    if(isEndTag) {
                        vOutput.addElement(cCurrentCont);
                        cCurrentCont = null;
                    }else {
                        cCurrentCont = new Container();
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
                if(isLink) {
                    LinkButton newLink = new LinkButton(text, "");
                    newComp = newLink;
                    newLink.setUIID("SoftButton");
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
                }else {
                    vOutput.addElement(newComp);
                }
            }
            if(nextIdx == -1) {
                break;
            }
            _sText = _sText.substring(nextIdx);
            //_sText = _sText.trim();
        }
        return vOutput;
    }
    
    public static Vector getSectionsFromJSON(JsonObject _oJson) {
        Vector vReturnVec = null;
        if(_oJson == null)
            return null;
        
        Object oMobileView = _oJson.get("mobileview");
        if(oMobileView != null && oMobileView instanceof JsonObject) {
            Object oSections = ((JsonObject)oMobileView).get("sections");
            if(oSections != null && oSections instanceof Vector) {
                vReturnVec = (Vector)oSections;
            }
        }
        return vReturnVec;
    }
}
