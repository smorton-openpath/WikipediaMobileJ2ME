/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import com.components.LinkButton;

import java.util.Vector;
import java.util.Stack;
import java.util.Enumeration;

import com.sun.lwuit.Button;
import com.sun.lwuit.Label;
import com.sun.lwuit.Container;
import com.sun.lwuit.Component;
import com.sun.lwuit.events.ActionListener;
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
    }//end deletePart(String _sNeedle, String _sHaystack)
    
    public static String decodeEverything(String _sHaystack) {
        String result = null;
        result = decodeHTML(_sHaystack);
        System.gc();
        Thread.yield();
        result = stripSlash(result);
        System.gc();
        Thread.yield();
        result = decodePercent(result);
        _sHaystack = "";
        System.gc();
        Thread.yield();
        
        return result;
    }
    
    public static String stripSlash(String _sHaystack) {
        String result = "";
        int index = _sHaystack.indexOf('\\');
        while((index > -1) && index + 1 < _sHaystack.length()) {
            String temp = _sHaystack.substring(0, index);
            boolean foundU = false;
            switch(_sHaystack.charAt(index+1)) {
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
                case 'u':
                    foundU = true;
                    break;
            }
            if(index > -1) {
                if(foundU) {
                    temp += com.sun.lwuit.html.HTMLUtils.convertHTMLCharEntity("#x" + _sHaystack.substring(index + 2, index + 6));
                    temp += _sHaystack.substring(index + 6);
                } else {
                    temp += _sHaystack.substring(index + 2);
                }
                _sHaystack = temp;
                index = _sHaystack.indexOf('\\');
            }
        }
        return _sHaystack;
    }//end stripSlash(String _sHaystack)
    
    public static String decodeHTML(String _sHaystack) {
        StringBuffer result = new StringBuffer();
        int index = _sHaystack.indexOf('&');
        while((index > -1) && index + 1 < _sHaystack.length()) {
            result.append(_sHaystack.substring(0, index));
            _sHaystack = _sHaystack.substring(index);
            int endIndex = _sHaystack.indexOf(';');
            if(endIndex == -1 || endIndex > 6)
            {
                index = 1;//If there are too many characters between & and ; then the & is not an encoded character.
                index = _sHaystack.indexOf('&', index);
                continue;
            }
            String chunk = "";
            boolean haveGood = true;
            for(int i = 1; i < endIndex; i++)//0 is &
            {
                if((_sHaystack.charAt(i) >= 'A' && _sHaystack.charAt(i) <= 'Z')
                        || (_sHaystack.charAt(i) >= 'a' && _sHaystack.charAt(i) <= 'z')
                        || (_sHaystack.charAt(i) >= '0' && _sHaystack.charAt(i) <= '9')
                        || (_sHaystack.charAt(i) == '#')) {
                    chunk += _sHaystack.charAt(i);
                }else {
                    haveGood = false;
                    break;
                }
            }
            if(!haveGood) {
                index = 1;//if there is a non-letter between the & and ; the & is not an encoded character
                index = _sHaystack.indexOf('&', index);
                continue;
            }
            chunk = chunk.toLowerCase();
            if(chunk.equalsIgnoreCase("amp")) {
                result.append("&");
            }else if(chunk.equalsIgnoreCase("gt")) {
                result.append(">");
            }else if(chunk.equalsIgnoreCase("lt")) {
                result.append("<");
            }else if(chunk.equalsIgnoreCase("quot") || chunk.equalsIgnoreCase("ldquo") || chunk.equalsIgnoreCase("rdquo")) {
                result.append("\"");
            }else if(chunk.equalsIgnoreCase("lsquo") || chunk.equalsIgnoreCase("rsquo")) {
                result.append("\'");
            }else if(chunk.equalsIgnoreCase("#8211") || chunk.equalsIgnoreCase("#8212") 
                    || chunk.equalsIgnoreCase("ndash") || chunk.equalsIgnoreCase("mdash")) {
                result.append("-");
            }
            _sHaystack = _sHaystack.substring(endIndex + 1);
            index = _sHaystack.indexOf('&');
        }
        result.append(_sHaystack);
        return result.toString();
    }//end decodeHTML(String _sHaystack)
    
    public static String decodePercent(String _sHaystack) {
        StringBuffer result = new StringBuffer();
        int index = _sHaystack.indexOf('%');
        while((index > -1) && index + 1 < _sHaystack.length()) {
            result.append(_sHaystack.substring(0, index));
            _sHaystack = _sHaystack.substring(index);
            String chunk = "";
            boolean haveGood = true;
            for(int i = 1; i < 3; i++)//0 is %
            {
                if(((char)_sHaystack.charAt(i) >= 'A' && _sHaystack.charAt(i) <= 'F')
                        || (_sHaystack.charAt(i) >= 'a' && _sHaystack.charAt(i) <= 'f')
                        || (_sHaystack.charAt(i) >= '0' && _sHaystack.charAt(i) <= '9')) {
                    chunk += _sHaystack.charAt(i);
                }else {
                    haveGood = false;
                    break;
                }
            }
            if(!haveGood) {
                index = 1;//if there is a non-letter between the & and ; the & is not an encoded character
                index = _sHaystack.indexOf('%', index);
                continue;
            }
            chunk = chunk.toLowerCase();
            int iChar = 0;
            for(int i = 0; i < 2; i++) {
                if((chunk.charAt(i) >= 'a' && chunk.charAt(i) <= 'f')) {
                    iChar += 10 + (int)chunk.charAt(i) - (int)'a';
                }else {
                    iChar += (int)chunk.charAt(i) - (int)'0';
                }
                if(i == 0) {
                    iChar *= 16;
                }
            }
            //System.out.println("char: "+iChar);
            result.append((char)iChar);
            _sHaystack = _sHaystack.substring(3);
            index = _sHaystack.indexOf('%');
        }
        result.append(_sHaystack);
        return result.toString();
    }//end decodePercent(String _sHaystack)
    
    //Used to strip out html from small sections of text.
    public static String stripHTML(String _sHaystack) {
        boolean done = false;
        StringBuffer retString = new StringBuffer();
        while(!done && _sHaystack.length() > 0) {
            int startIdx = _sHaystack.indexOf('<');
            int endIdx = _sHaystack.indexOf('>');
            int nextStartIdx = _sHaystack.indexOf('<');
            
            //we have no more real tags.
            if(startIdx == -1 || endIdx == -1){
                retString.append(_sHaystack);
                done = true;
            }
            //there was a non-tag '<' to deal with.
            if(endIdx < startIdx) {
                startIdx = _sHaystack.indexOf('<', startIdx);
                retString.append(_sHaystack.substring(0, startIdx + 1));
                _sHaystack = _sHaystack.substring(startIdx + 1);
                continue;
            }
            retString.append(_sHaystack.substring(0, startIdx));
            _sHaystack = _sHaystack.substring(endIdx + 1);
        }
        return retString.toString();
    }//end stripHTML(String _sHaystack)
    
    public static Vector tokenizeString(String _sText) {
        Vector vOutput = new Vector();
        char[] breakItems = {' ', '\n', '\t', '<'};
        boolean done = false;
        int iCurrentIdx = 0;
        int iFullLength = _sText.length();
        while (!done && iCurrentIdx < iFullLength) {
            int nextIdx = 0;
            StringBuffer text = new StringBuffer();
            if(_sText.charAt(iCurrentIdx) == '<') {//parse tags
                int tagIdx = _sText.indexOf(' ', iCurrentIdx);
                nextIdx = _sText.indexOf('>', iCurrentIdx) + 1;
                if(nextIdx == -1) {//Something went massively wrong if we have no end >
                    return vOutput;
                }
                text.append(_sText.substring(iCurrentIdx, nextIdx));
            }else {//parse text
                int breakItem = 0;
                for(int i = 0; i < breakItems.length; i++) {
                    int idx = _sText.indexOf(breakItems[i], iCurrentIdx);

                    if(nextIdx <= 0 || (idx > 0 && idx < nextIdx)) {
                        nextIdx = idx;
                        breakItem = i;
                    }
                }
                //if we broke on a space, keep that space.
                if(breakItems[breakItem] != '<') {
                    nextIdx++;
                }
                if(nextIdx > 0) {
                    text.append(_sText.substring(iCurrentIdx, nextIdx));
                }else {
                    text.append(_sText);
                    nextIdx = _sText.length() - 1;
                }
            }
            if(nextIdx <= iCurrentIdx) {
                //System.out.println("failed at: "+iCurrentIdx+", "+nextIdx);
                done = true;
            }
            if(text.length() > 0) {
                //System.out.println("adding: "+text);
                vOutput.addElement(text.toString());
            }
            iCurrentIdx = nextIdx;
        }
        return vOutput;
    }//end tokenizeString(String _sText)
    
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
    }//end getSectionsFromJSON(JsonObject _oJson)
    
    public static Vector getQueryResultsFromJSON(JsonObject _oJson) {
        Vector vReturnVec = null;
        if(_oJson == null)
            return null;
        
        Object oQuery = _oJson.get("query");
        if(oQuery != null && oQuery instanceof JsonObject) {
            Object oSearch = ((JsonObject)oQuery).get("search");
            if(oSearch != null && oSearch instanceof Vector) {
                vReturnVec = (Vector)oSearch;
            }
        }
        return vReturnVec;
    }//end getQueryResultsFromJSON(JsonObject _oJson)
    
    public static Vector getLanguagesFromJSON(JsonObject _oJson) {
        /*This structure is a little weird. it has base object -> query->pages
         * 
         */
        Vector vReturnVec = null;
        if(_oJson == null)
            return null;
        
        Object oQuery = _oJson.get("query");
        if(oQuery != null && oQuery instanceof JsonObject) {
            Object oPages = ((JsonObject)oQuery).get("pages");
            if(oPages != null && oPages instanceof JsonObject) {
                Enumeration pages = ((JsonObject)oPages).elements();
                while (pages.hasMoreElements()) {
                    JsonObject page = (JsonObject) pages.nextElement();
                    if(page.isEmpty()) {
                        continue;
                    }
                    vReturnVec = (Vector)page.get("langlinks");
                }
            }
        }
        return vReturnVec;
    }//end getSectionsFromJSON(JsonObject _oJson)
    
    public static Vector getMainLanguagesFromJSON(JsonObject _oJson) {
        /*This structure is a little weird. it has base object -> query->pages
         * 
         */
        Vector vReturnVec = null;
        if(_oJson == null)
            return null;
        
        Object oQuery = _oJson.get("sitematrix");
        if(oQuery != null && oQuery instanceof JsonObject) {
            Object oPages = ((JsonObject)oQuery).get("pages");
            if(oPages != null && oPages instanceof JsonObject) {
                Enumeration pages = ((JsonObject)oPages).elements();
                while (pages.hasMoreElements()) {
                    JsonObject page = (JsonObject) pages.nextElement();
                    if(page.isEmpty()) {
                        continue;
                    }
                    vReturnVec = (Vector)page.get("sitematrix");
                }
            }
        }
        return vReturnVec;
    }//end getSectionsFromJSON(JsonObject _oJson)
    
    public static String getNormalizedTitleFromJSON(JsonObject _oJson) {
        return ((JsonObject)((JsonObject)_oJson).get("mobileview")).getString("normalizedtitle");
    }//end getNormalizedTitleFromJSON(JsonObject _oJson)
}
