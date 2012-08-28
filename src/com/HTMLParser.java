/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import com.components.LinkButton;
import com.components.ImageButton;
import com.components.TableButton;
import com.pages.BasePage;


import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Label;
import com.sun.lwuit.io.NetworkManager;
import com.sun.lwuit.io.services.ImageDownloadService;
import com.sun.lwuit.layouts.*;
import com.sun.lwuit.table.*;
import com.sun.lwuit.Font;
import com.sun.lwuit.Display;
import com.sun.lwuit.Button;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Stack;

/**
 *
 * @author caxthelm
 */

public class HTMLParser {
    private static final int STYLE_BOLD = 1;
    private static final int STYLE_ITALIC = 2;
    private static final int STYLE_ORDEREDLIST = 4;
    private static final int STYLE_LINK = 8;
    private static final int STYLE_SHOWTABLES = 16;
    private static final int STYLE_INTABLE = 32;
    
    private static boolean lowOnMemory = false;
    
    private static int TABLE_WIDTH = 240;
    private static final int TABLE_WIDTH_MODIFIER = 2;
    
    private static Vector tableVector = null;
    
    private static Font m_oFontBold = Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_BOLD , Font.SIZE_MEDIUM);
    private static Font m_oFontItalic = Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_ITALIC, Font.SIZE_SMALL);
    private static Font m_oFontBoldItalic = Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_BOLD | Font.STYLE_ITALIC, Font.SIZE_MEDIUM);
    
    public HTMLParser() {
    }
    public static Component parseHtml(String _sText, boolean _bShowTables) {
        if(_sText == null) {            
            return new Container();
        }
        Vector tableVector = null;
        if(!_bShowTables) {
            tableVector = takeOutTables(_sText);
        }
//      Vector vComponents = chopHTMLString(_sText);
        Vector tags = Utilities.tokenizeString(_sText);
        _sText = null;
        System.gc();
        return parseHtml(tags, tableVector, _bShowTables);
    }//end parseHtml(String _sText, boolean _bShowTables)
    
    public static Component parseHtml(Vector _vTags, Vector _tableVector, boolean _bShowTables) {
        //System.out.println("!@#$% html Mem start1: "+Runtime.getRuntime().freeMemory());
        int initialParams = 0;
        if(_bShowTables) {
            initialParams += STYLE_SHOWTABLES;            
        }else {
            tableVector = _tableVector;
        }
        System.gc();
        Vector vComponents = parseHtmlTagVector(_vTags, initialParams);
        _vTags.removeAllElements();
        _vTags = null;
        if(tableVector != null) {
            tableVector.removeAllElements();
            tableVector = null;
        }
        
        System.gc();
        Thread.yield();
        
        Container cLabelContainer = null;
        
        Container cTextComp = new Container();
        cTextComp.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        while(vComponents.size() > 0) {
            Object oComp = vComponents.firstElement();
            vComponents.removeElement(oComp);
            boolean addComp = true;
            
            //Failsafe to make sure all labels are in a container.
            if(oComp instanceof Label || oComp instanceof LinkButton) {
                if(cLabelContainer == null) {
                    cLabelContainer = new Container();
                    cTextComp.addComponent(cLabelContainer);
                }
//                try {
                cLabelContainer.addComponent((Component)oComp);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    if(oComp instanceof Label) {
//                        System.out.println(" 000 and the label was " + ((Label) oComp).getText());
//                    } else {
//                        System.out.println(" 000 " + oComp.toString());
//                    }
//                }
                addComp = false;
            }else {
                cLabelContainer = null;
            }
            
            //We may get empty containers, if that happens we strip them out to save space.
            if(oComp == null || oComp instanceof Container && ((Container)oComp).getComponentCount() <= 0) {
                addComp = false;
            }
            if(addComp) {
                cTextComp.addComponent((Component)oComp);
            }
        }//end while(vComponents.size() > 0)
        
        cTextComp.getUnselectedStyle().setMargin(0, 0, 0, 0);
        cTextComp.getSelectedStyle().setMargin(0, 0, 0, 0);
        System.gc();
        Thread.yield();
        
        //System.out.println("!@#$% html Mem finish1: "+Runtime.getRuntime().freeMemory());
        return cTextComp;
    }//end parseHtml(String _sText, boolean _bShowTables)
    
    private static String getTagID(String _sText) {
        //Get the tag identifier
        String tag = "";
        int tagIdx = _sText.indexOf(' ');
        int endIdx = _sText.indexOf('>');
        if(tagIdx != -1 && tagIdx < endIdx){
            endIdx = tagIdx;
        }
        if(endIdx == -1) {//Something went massively wrong if we have no end >
            return "";
        }
        if(_sText.charAt(1) == '/') {
            tag = _sText.substring(2, endIdx);
        }else {                    
            tagIdx = _sText.indexOf('/');
            if(tagIdx != -1 && tagIdx < endIdx){
                endIdx = tagIdx;
            }
            tag = _sText.substring(1, endIdx);
        }
        return tag;
    }//end getTagID(String _sText)
    
    static int nestedDepth = 0;
    static int thisWordCount = 0;
    private static Vector parseHtmlTagVector(Vector _vTags, int _iStyleMask) {
        
        System.gc();
        
        nestedDepth++;
        //System.out.println("   ...depth: "+nestedDepth);
        Vector components = new Vector();
        
        while(_vTags.size() > 0) {
            String tag = (String) (_vTags.elementAt(0));
            //System.out.println("in parseHtmlTagVector, head tag is " + _vTags.firstElement().toString());
            //System.out.println("!@#$% html Mem tag: "+tag+", mem: "+Runtime.getRuntime().freeMemory());
            String baseTag = getTagID(tag);
            if( tag.charAt(0) == '<') {
                // It's a tag!  Parse it as one!
               
                if(tag.indexOf("/") == 1) {
                    //It's a close tag
                    nestedDepth--;
                    //System.out.println("   ...depth2: "+nestedDepth);
                    //System.out.println("      ...closing tag was: " + tag);
                    //System.out.println("      ...components length was " + components.size());
                    _vTags.removeElementAt(0);
                    System.gc();
                    Thread.yield();
                    return flattenVectors(components);
                }
                if( baseTag.equalsIgnoreCase("a")) {
                    components.addElement(parseLink(_vTags, _iStyleMask));
                } else if( baseTag.equalsIgnoreCase("b")) {
                    components.addElement(parseBold(_vTags, _iStyleMask));
                } else if( baseTag.equalsIgnoreCase("br") || baseTag.equalsIgnoreCase("hr")) {
                    components.addElement(parseText(" \n", _iStyleMask));
                    _vTags.removeElementAt(0);
                } else if(baseTag.equalsIgnoreCase("h2")) {
                    thisWordCount = 0;
                    components.addElement(parseHeader(_vTags, _iStyleMask));
                } else if(baseTag.equalsIgnoreCase("i")) {
                    components.addElement(parseItalic(_vTags, _iStyleMask));
                } else if(baseTag.equalsIgnoreCase("img")) {
                    components.addElement(parseImage(_vTags, _iStyleMask));
                } else if(baseTag.equalsIgnoreCase("li")) {
                   components.addElement(parseListLine(_vTags, _iStyleMask));
                } else if(baseTag.equalsIgnoreCase("ol")) {
                    components.addElement(parseOrderedList(_vTags, _iStyleMask));
                }else if(baseTag.equalsIgnoreCase("p")) {
                    System.out.println(" --- in p case, memory is " + Runtime.getRuntime().freeMemory());
                    System.out.println("  --- (and the nested depth is " + nestedDepth + ")");
                    if(Runtime.getRuntime().freeMemory() > 100000) {
                        components.addElement(parseParagraph(_vTags, _iStyleMask));
                    } else {
                        lowOnMemory = false;
                        Label ellipsis = new Label();
                        ellipsis.setText("...");
                        components.addElement(ellipsis);
                    }
                } else if(baseTag.equalsIgnoreCase("table")) {
                    //System.out.println("   ---   in parseHtmlTagVector, found a table");
                    components.addElement(parseTable(_vTags, _iStyleMask));
                } else if(baseTag.equalsIgnoreCase("tr")) {
                    //System.out.println("   ---   in parseHtmlTagVector, found a tr");
                    components.addElement(parseTableRow(_vTags, _iStyleMask));
                } else if(baseTag.equalsIgnoreCase("th")) {
                    //System.out.println("   ---   in parseHtmlTagVector, found a th");
                    components.addElement(parseTableHeader(_vTags, _iStyleMask));
                } else if(baseTag.equalsIgnoreCase("td")) {
                    //System.out.println("   ---   in parseHtmlTagVector, found a td");
                    components.addElement(parseTableCell(_vTags, _iStyleMask));
                } else if(baseTag.equalsIgnoreCase("ul")) {
                    components.addElement(parseList(_vTags, _iStyleMask));
                }else {
                    components.addElement(parseDefault(_vTags, _iStyleMask));
                    
                }
            } else {
                // It's not a tag.  Just text.
                Component text = parseText(tag, _iStyleMask);
                if(text != null) {
                    thisWordCount++;
                    if(mainMIDlet.getCurrentPage().getType() == BasePage.PAGE_MAIN) {
                        if(thisWordCount < 60) {
                            components.addElement(text);
                        } else if(thisWordCount == 60) {
                            components.addElement(new Label("..."));
                        }
                    } else {
                        components.addElement(text);
                    }
                }
                _vTags.removeElementAt(0);
            }
            //String tag2 = (String) (_vTags.elementAt(0));
        }//end while
        nestedDepth--;
        //System.out.println("depth at end of parsehtmltagvectors: "+nestedDepth);
        return flattenVectors(components);
    }//end  parseHtmlString(String _sText)

    private static Component parseText(String _sText, int _iStyleMask) {
        //System.out.println("setting Text: "+_sText);
        if(_sText.indexOf("\n") >= 0) {
            _sText = _sText.replace('\n', '\r');//return null;//
            
        }
        Component newComp = null;
        if((_iStyleMask & STYLE_LINK) != 0) {
            LinkButton newLink = new LinkButton(_sText);
            newLink.setUIID("LabelButtonLink");
            newComp = newLink;
        }else {
            if(_sText.indexOf(" ") == -1) {
                _sText = " "+_sText;
            }
            Label newLabel = new Label(_sText);
            newLabel.setUIID("No_Margins");
            newComp = newLabel;
        }
        if((_iStyleMask & STYLE_BOLD) != 0 && (_iStyleMask & STYLE_ITALIC) != 0) {
            newComp.getStyle().setFont(m_oFontBoldItalic);
            newComp.getSelectedStyle().setFont(m_oFontBoldItalic);
            newComp.getPressedStyle().setFont(m_oFontBoldItalic);
        }else if((_iStyleMask & STYLE_BOLD) != 0 ) {
            newComp.getStyle().setFont(m_oFontBold);
            newComp.getSelectedStyle().setFont(m_oFontBold);
            newComp.getPressedStyle().setFont(m_oFontBold);
        }else if((_iStyleMask & STYLE_ITALIC) != 0) {
            newComp.getStyle().setFont(m_oFontItalic);
            newComp.getSelectedStyle().setFont(m_oFontItalic);
            newComp.getPressedStyle().setFont(m_oFontItalic);
        }
        newComp.getStyle().setMargin(1, 1, 1, 1);
        newComp.getSelectedStyle().setMargin(1, 1, 1, 1);
        newComp.getPressedStyle().setMargin(1, 1, 1, 1);
        return newComp;
    }//end parseText(String _sText)
    
    //<a>
    private static Vector parseLink(Vector _vTags, int _iStyleMask) {
        String tag = (String) (_vTags.firstElement());
        _vTags.removeElementAt(0);
        _iStyleMask += STYLE_LINK;
        Vector compVec = flattenVectors(parseHtmlTagVector(_vTags, _iStyleMask));
        
        int startImgIdx = tag.indexOf("title=\"")+7;
        int endImgIdx = tag.indexOf("\"", startImgIdx);
        String titleText = tag.substring(startImgIdx, endImgIdx);

        startImgIdx = tag.indexOf("href=\"")+6;
        endImgIdx = tag.indexOf("\"", startImgIdx);
        String linkText = tag.substring(startImgIdx, endImgIdx);

        for(int i = 0; i < compVec.size(); i++) {
            Object oComp = (Object)compVec.elementAt(i);
            if(oComp instanceof LinkButton) {
                ((LinkButton)oComp).setLink(linkText);
            }
        }
        return compVec;
    }//end parseLink(Vector tags, int _iStyleMask)
    
    //<b>
    private static Vector parseBold(Vector _vTags, int _iStyleMask) {
        _iStyleMask += STYLE_BOLD;
        _vTags.removeElementAt(0);
        return flattenVectors(parseHtmlTagVector(_vTags, _iStyleMask));
    }//end parseBold(Vector tags, int _iStyleMask)
    
    //<br/>
    private static Vector parseBreak(Vector _vTags, int _iStyleMask) {
        _vTags.removeElementAt(0);
        return flattenVectors(parseHtmlTagVector(_vTags, _iStyleMask));   
    }//end parseBreak(Vector tags, int _iStyleMask)
    
    //None
    private static Vector parseDefault(Vector _vTags, int _iStyleMask) {
        _vTags.removeElementAt(0);
        return flattenVectors(parseHtmlTagVector(_vTags, _iStyleMask));   
    }//end parseBreak(Vector tags, int _iStyleMask)
    
    //<h2>
    private static Vector parseHeader(Vector _vTags, int _iStyleMask) {
        _iStyleMask += STYLE_BOLD;
        return parseParagraph(_vTags, _iStyleMask);
    }//end parseHeader(Vector tags, int _iStyleMask)
    
    //<i>
    private static Vector parseItalic(Vector _vTags, int _iStyleMask) {
        _iStyleMask += STYLE_ITALIC;
        _vTags.removeElementAt(0);
        return flattenVectors(parseHtmlTagVector(_vTags, _iStyleMask));
    }//end parseItalic(Vector tags, int _iStyleMask)
    
    //<img>
    private static Vector parseImage(Vector _vTags, int _iStyleMask) {
        String tag = (String) (_vTags.firstElement());
        _vTags.removeElementAt(0);
        //pull out src parameter
        int startImgIdx = tag.indexOf("src=\"")+5;
        int endImgIdx = tag.indexOf("\"", startImgIdx);
        String srcText = tag.substring(startImgIdx, endImgIdx);
        
        //Pull out alt parameter
        startImgIdx = tag.indexOf("alt=\"")+5;
        endImgIdx = tag.indexOf("\"", startImgIdx);
        String altText = tag.substring(startImgIdx, endImgIdx);
        
        //double check alt text
        if(altText.length() <= 0) {
            altText = srcText.substring(srcText.lastIndexOf('/') + 1);
            altText = altText.replace('_', ' ');
        }

        //pull out height and width
        int width = -1;
        int height = -1;
        startImgIdx = tag.indexOf("width=\"")+7;
        if(startImgIdx > -1) {
            endImgIdx = tag.indexOf("\"", startImgIdx);
            width = Integer.parseInt(tag.substring(startImgIdx, endImgIdx));
        }

        startImgIdx = tag.indexOf("height=\"")+8;
        if(startImgIdx > -1) {
            endImgIdx = tag.indexOf("\"", startImgIdx);
            height = Integer.parseInt(tag.substring(startImgIdx, endImgIdx));
        }
        Vector returnVec = new Vector();
        
        //If the image is less than 51 pixels it is likely an icon; so just go ahead and show it.        
        if(width > 0 && width <= 60){//&& height > 0 && height <= 50) {
            Label newLabel = new Label();
            newLabel.setUIID("no_MarginsTransparent");
            ImageDownloadService img = new ImageDownloadService("http:"+srcText, newLabel);
            NetworkManager.getInstance().addToQueue(img);
            returnVec.addElement( newLabel);
            //System.out.println("made image: "+srcText);
        }else {
            //System.out.println("adding image: "+altText+", "+srcText);
            ImageButton newLink = new ImageButton(altText, srcText);
            if((_iStyleMask & STYLE_SHOWTABLES) != 0){
                //setLayout(newLink, TABLE_WIDTH / TABLE_WIDTH_MODIFIER);
            }
        
            //Add button to the list, reset the container
            returnVec.addElement(newLink);
        }
        return returnVec;
    }//end parseImage(Vector tags, int _iStyleMask)
    
    //<li>
    private static Vector parseListLine(Vector _vTags, int _iStyleMask) {       
        //Just do what paragraph does.
        _vTags.removeElementAt(0);
        Vector compVec = flattenVectors(parseHtmlTagVector(_vTags, _iStyleMask));
        Vector returnVec = new Vector();
        Container newContainer = new Container();
        newContainer.getStyle().setMargin(1, 1, 1, 1);
        newContainer.getStyle().setPadding(1, 1, 0, 0);
        int heightToSet = 20;
        int thisLineWidth = 0;
        int tallestHeightThisLine = 0;
        for(int i = 0; i < compVec.size(); i++) {
            //System.out.println("para: "+compVec.elementAt(i));
            Component pulledComp = (Component)compVec.elementAt(i);            
            newContainer.addComponent(pulledComp);
            
            if (pulledComp.getPreferredH() + 8 > tallestHeightThisLine) {
                tallestHeightThisLine = pulledComp.getPreferredH() + 8;
            }
            
            thisLineWidth += pulledComp.getPreferredW();
            if(thisLineWidth + pulledComp.getPreferredW() > com.sun.lwuit.Display.getInstance().getDisplayWidth()) {
                thisLineWidth = 0;
                heightToSet += tallestHeightThisLine;
                tallestHeightThisLine = 0;
            }
        }//end for(int i = 0; i < compVec.size(); i++)
        
        newContainer.layoutContainer();
        newContainer.invalidate();
        newContainer.layoutContainer();
        newContainer.setPreferredH(heightToSet - 10);
        returnVec.addElement(newContainer);
        return returnVec;
    }//end parseListLine(Vector tags, int _iStyleMask)
    
    //<ol>
    private static Vector parseOrderedList(Vector _vTags, int _iStyleMask) {
        _vTags.removeElementAt(0);
        _iStyleMask |= STYLE_ORDEREDLIST;
        Vector compVec = parseHtmlTagVector(_vTags, _iStyleMask);
        Vector returnVec = new Vector();
        Container newContainer = new Container();
        newContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        
        int heightToSet = 0;
        int thisLineWidth = 0;
        int tallestHeightThisLine = 0;
        for(int i = 0; i < compVec.size(); i++) {
            //System.out.println("para: "+compVec.elementAt(i));
            Component pulledComp = (Component)compVec.elementAt(i);
            
            if(pulledComp instanceof Container && ((Container)pulledComp).getComponentCount() > 1) {
                Component item = ((Container)pulledComp).getComponentAt(0);
                if(item instanceof Label ) {
                    Label textItem = (Label)item;
                    textItem.setText(""+((i + 1)/2));
                }
            }
            newContainer.addComponent(pulledComp);
            
            if (pulledComp.getPreferredH() + 8 > tallestHeightThisLine) {
                tallestHeightThisLine = pulledComp.getPreferredH() + 8;
            }
            
            thisLineWidth += pulledComp.getPreferredW();
            if(thisLineWidth + pulledComp.getPreferredW() > com.sun.lwuit.Display.getInstance().getDisplayWidth()) {
                thisLineWidth = 0;
                heightToSet += tallestHeightThisLine;
                tallestHeightThisLine = 0;
            }
        }
        newContainer.setPreferredH(heightToSet - 10);
        returnVec.addElement(newContainer);
        return returnVec;
    }//end parseList(Vector tags, int _iStyleMask)
    //<p>
    private static Vector parseParagraph(Vector _vTags, int _iStyleMask) {
        _vTags.removeElementAt(0);
        Vector compVec = flattenVectors(parseHtmlTagVector(_vTags, _iStyleMask));
        Vector returnVec = new Vector();
        Container newContainer = new Container();
        newContainer.getStyle().setMargin(1, 1, 1, 1);
        newContainer.getStyle().setPadding(1, 1, 0, 0);
        for(int i = 0; i < compVec.size(); i++) {
            //System.out.println("para: "+compVec.elementAt(i));
            Component pulledComp = (Component)compVec.elementAt(i);            
            newContainer.addComponent(pulledComp);           
            
        }//end for(int i = 0; i < compVec.size(); i++)
        newContainer.layoutContainer();
        newContainer.invalidate();
        newContainer.layoutContainer();
        returnVec.addElement(newContainer);
        return returnVec;
    }//end parseParagraph(Vector tags, int _iStyleMask)
    
    //<table>
    private static Vector parseTable(Vector _vTags, int _iStyleMask) {
        
        TABLE_WIDTH = Display.getInstance().getDisplayWidth() * TABLE_WIDTH_MODIFIER;
        Vector returnVec = new Vector();
        _vTags.removeElementAt(0);
        Vector compVec = parseHtmlTagVector(_vTags, _iStyleMask + STYLE_INTABLE);
        compVec = stripVector(compVec);
        if((_iStyleMask & STYLE_SHOWTABLES) != 0) {
            Container newContainer = new Container();
            newContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
            newContainer.setUIID("Table");
            newContainer.setSnapToGrid(true);
            for(int i = 0; i < compVec.size(); i++) {
                newContainer.addComponent((Component)compVec.elementAt(i));
            }
            newContainer.setWidth(TABLE_WIDTH);
            
            returnVec.addElement(newContainer);
            if((_iStyleMask & STYLE_SHOWTABLES) == 0) {
                tableVector.removeElementAt(0);
            }            
        }else if((_iStyleMask & STYLE_INTABLE) == 0){
            String title = "";//
            //System.out.println("title: "+title +", "+ compVec.firstElement());
            
            //Axthelm - This is ugly code, but I couldn't think of a better way to pull out the first item.
            //Axthelm - we expect the title to be the first item of the first row.
            if(compVec != null && compVec.size() > 0){
                for(int i = 0; i < compVec.size(); i++) {
                    try {
                        if(compVec.elementAt(i) instanceof Label) {//if first item is a label, use that.
                            title += ((Label)compVec.elementAt(i)).getText();
                        }
                    }catch(Exception e) {
                    }
                }
            }//end if(compVec != null && compVec.size() > 0)
            
            if(title == null || title.length() <= 0) {
                title = mainMIDlet.getString("Table");
            }
            if(tableVector != null && tableVector.size() > 0) {
                TableButton newTable = new TableButton(title, (String)tableVector.firstElement());
                returnVec.addElement(newTable);
                tableVector.removeElement(tableVector.firstElement());
            }
            compVec.removeAllElements();
            System.gc();
        }
        return returnVec;
    }//end parseTable(Vector tags, int _iStyleMask)
    
    //<tr>
    private static Vector parseTableRow(Vector _vTags, int _iStyleMask) {
        _vTags.removeElementAt(0);
        Vector compVec = parseHtmlTagVector(_vTags, _iStyleMask);
        compVec = stripVector(compVec);
        Vector returnVec = new Vector();
        Container newContainer = new Container();
        //newContainer.setLayout(new BoxLayout(BoxLayout.X_AXIS));
        if(compVec.size() > 0) {
            newContainer.setLayout(new GridLayout(1, compVec.size()));
        }
        newContainer.setUIID("TableRow");
        if(compVec.size() == 1) {
            Component pulledComp = (Component)compVec.elementAt(0);
            newContainer.setUIID("No_Margins");
            pulledComp.setUIID("TableCellSpecial");
            newContainer.addComponent(pulledComp);
            setLayout(pulledComp, TABLE_WIDTH);
        }else {
            for(int i = 0; i < compVec.size(); i++) {
                Component pulledComp = (Component)compVec.elementAt(i);                
                newContainer.addComponent( pulledComp);                
                setLayout(pulledComp, TABLE_WIDTH / compVec.size());              
            }
        }
        setLayout(newContainer, -1);
        
        //System.out.println("row width, height: "+newContainer.getPreferredW()+", "+newContainer.getPreferredH()+" = "+newContainer.getHeight());
        returnVec.addElement(newContainer);
        return returnVec;
    }//end parseTableRow(Vector tags, int _iStyleMask)
    
    //<th>
    private static Vector parseTableHeader(Vector _vTags, int _iStyleMask) {
        Vector returnVec = new Vector();
        Container newContainer = new Container();
        _vTags.removeElementAt(0);
        _iStyleMask += STYLE_BOLD;
        Vector compVec = flattenVectors(parseHtmlTagVector(_vTags, _iStyleMask));
        compVec = stripVector(compVec);
        
        newContainer.setUIID("TableCell");
        for(int i = 0; i < compVec.size(); i++) {
            newContainer.addComponent((Component)compVec.elementAt(i));
        }
        setLayout(newContainer, -1);
        returnVec.addElement(newContainer);
        return returnVec;
    }//end parseTableHeader(Vector tags, int _iStyleMask)
    
    //<td>
    private static Vector parseTableCell(Vector _vTags, int _iStyleMask) {
        Vector returnVec = new Vector();
        Container newContainer = new Container();
        _vTags.removeElementAt(0);
        Vector compVec = flattenVectors(parseHtmlTagVector(_vTags, _iStyleMask));
        compVec = stripVector(compVec);
        
        //newContainer.setLayout(new FlowLayout(Component.LEFT));
        //newContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        newContainer.setUIID("TableCell");
        if(compVec.size() == 1) {
            BorderLayout border = new BorderLayout();
            border.setCenterBehavior(BorderLayout.CENTER_BEHAVIOR_SCALE);
            newContainer.setLayout(border);
            newContainer.addComponent(BorderLayout.CENTER, (Component)compVec.elementAt(0));
            //newContainer.addComponent((Component)compVec.elementAt(0));
        }else {
            for(int i = 0; i < compVec.size(); i++) {
                //System.out.println("cell: "+compVec.elementAt(i));
                Component pulledComp = (Component)compVec.elementAt(i);
                newContainer.addComponent(pulledComp);
            }
            Label spacer = new Label(" ");
            spacer.setUIID("No_Margins");
            newContainer.addComponent(spacer);
        }
        setLayout(newContainer, -1);
        returnVec.addElement(newContainer);
        
        return returnVec;
    }//end parseTableCell(Vector tags, int _iStyleMask)

    private static Vector removeFirstVectorElement(Vector _vOldVector) {
        _vOldVector.removeElementAt(0);
        return _vOldVector;
    }//end removeFirstVectorElement(Vector _vOldVector)
    
    //<ul>
    private static Vector parseList(Vector _vTags, int _iStyleMask) {
        _vTags.removeElementAt(0);
        Vector compVec = parseHtmlTagVector(_vTags, _iStyleMask);
        Vector returnVec = new Vector();
        Container newContainer = new Container();
        newContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        for(int i = 0; i < compVec.size(); i++) {
            //System.out.println("para: "+compVec.elementAt(i));
            newContainer.addComponent((Component)compVec.elementAt(i));
        }
        returnVec.addElement(newContainer);
        return returnVec;
    }//end parseList(Vector tags, int _iStyleMask)
    
    private static Vector flattenVectors(Vector _vToFlatten) {
       Vector flattened = new Vector();
        
        for(int i=0; i<_vToFlatten.size(); i++) {
            if(_vToFlatten.elementAt(i) instanceof Vector) {
                Vector innerVec = (Vector) _vToFlatten.elementAt(i);
                for(int j=0; j<innerVec.size(); j++) {
                    flattened.addElement(innerVec.elementAt(j));
                }
            } else {
                flattened.addElement(_vToFlatten.elementAt(i));
            }
        }
        _vToFlatten.removeAllElements();
        return flattened;
    }//end flattenVectors(Vector _vToFlatten)
    
    private static Vector stripVector(Vector _vToStrip) {
        Vector returnVec = new Vector();
        if(_vToStrip == null || _vToStrip.size() <= 0) {
            return returnVec;
        }
        
        for(int i = 0; i < _vToStrip.size(); i++) {
            Object element = _vToStrip.elementAt(i);
            if(element instanceof Label) {
                Label labelElement = (Label)element;
                String text = labelElement.getText();
                String textTrim = text.trim();
                if(labelElement.getIcon() != null || (text != null && text.length() > 0 
                        && textTrim.length() > 0)) 
                {
                    returnVec.addElement(element);
                    //System.out.println("adding element: "+element);
                }
            }else {
                returnVec.addElement(element);
            }
        }
        
        return returnVec;
    }//end stripVector(Vector _vToStrip)
    
    public static Vector takeOutTables(String _sText) {
        Vector returnVec = new Vector();
        if(_sText == null || _sText.length() <= 0) {
            //System.out.println("takeOutTables() no string");
            return null;
        }
        int tableIdx = 0;
        int endTableIdx = 0;
        while (tableIdx != -1 && tableIdx < _sText.length()) {//Table start loop
            int nextTable = 0;
            int numTable = 0;
            //look for the next isntance of <table
            tableIdx = _sText.indexOf("<table", endTableIdx);
            endTableIdx = _sText.indexOf("</table>", tableIdx);//Check the next end tag
            nextTable = tableIdx;
            if(tableIdx == -1 || endTableIdx == -1) {
                //System.out.println("found no tables");
                break;//no tables, failure
            }
            //If we found one, we need one ending, and start looping to find any nested.
            //System.out.println("Starting Table: "+tableIdx+", "+endTableIdx);
            while(nextTable != -1 && nextTable < endTableIdx) {//look for nested tables.                
                nextTable = _sText.indexOf("<table", nextTable + 1);
                //System.out.println("new table: "+nextTable);
                if(nextTable == -1 || nextTable > endTableIdx) {
                    break;
                }else {
                    //if we find a nested we know we have to find one more end.
                    numTable++;
                }
            }
            //After finding all nested loop through that many end tags.
            for(; numTable > 0 && endTableIdx != -1; numTable--) {
                //System.out.println("got next end: " +numTable);
                int endIdx = _sText.indexOf("</table>", endTableIdx + 1);
                if(endIdx == -1) {
                    break;
                }
                endTableIdx = endIdx;
            }
            //System.out.println("final Table: "+tableIdx+", "+endTableIdx);
            //That last end tag is our endpoint.  Put that string in our collection vector.
            if(endTableIdx != -1) {
                String test = _sText.substring(tableIdx, endTableIdx+8);
                //System.out.println("table: "+test);
                returnVec.addElement(test);
            }else {
                break;
            }
            tableIdx++;//look for the next consecutive table.
        }
        return returnVec;
    }//end takeOutTables(String _sText)
    
    public static String takeOutTableString(String _sText, Vector _vTableVectors) {
        if(_vTableVectors == null || _vTableVectors.size() == 0) {
            return _sText;
        }
        for(int i = 0; i < _vTableVectors.size(); i++) {
            String tableStr = (String)_vTableVectors.elementAt(i);
            int startIdx = _sText.indexOf(tableStr);
            int length = tableStr.length();
            String title = mainMIDlet.getString("Table");
            int titleEndIdx = tableStr.indexOf("</th>");
            if(titleEndIdx > -1) {
                int titleStartIdx = tableStr.indexOf(">");
                int lastIdx = titleStartIdx;
                while(lastIdx < titleEndIdx) {
                    titleStartIdx = lastIdx;
                    lastIdx = tableStr.indexOf(">", lastIdx + 1);
                }
                title = tableStr.substring(titleStartIdx + 1, titleEndIdx);
            }
            String temp = _sText.substring(0, startIdx);
            temp += "<table>";
            temp += title;
            temp += "</table>";
            temp +=_sText.substring(startIdx + length);
            _sText = temp;
        }
        return _sText;
    }//end takeOutTableString(String _sText, Vector _vTableVectors)
    
    private static void setLayout(Component _cContainer, int _iForcedWidth) {
        
        if(_iForcedWidth > 0 && _cContainer.getPreferredW() > _iForcedWidth) {
            _cContainer.setPreferredW(_iForcedWidth);
        }
        if(_cContainer instanceof Container) {
            ((Container)_cContainer).layoutContainer();
            ((Container)_cContainer).invalidate();
            ((Container)_cContainer).layoutContainer();
        }
    }//end setLayout(Component _cContainer, int _iForcedWidth)
    
    public static void resetAllTable(Component _cComp) {
        if(_cComp == null) {
            return;
        }
        if(_cComp instanceof Container) {
            int totalWidth = 0;
            int tallestPrefH = 0;
            int totalHeight = 0;
            
            Container newContainer = ((Container)_cComp);
            int childSize = newContainer.getComponentCount();
            int lines = 1;
            for(int i = 0; i < childSize; i++) {
                Component pulledComp = newContainer.getComponentAt(i);
                resetAllTable(pulledComp);
                
                if(newContainer.getLayout() instanceof FlowLayout) {
                    //recalculate height for flow layouts.
                    if(i == 0) {
                        totalHeight = pulledComp.getHeight();
                        //System.out.println("new line: "+tallestPrefH+", "+totalHeight);
                    }
                    if(pulledComp.getHeight() > tallestPrefH) {
                        tallestPrefH = pulledComp.getHeight();
                    }
                    totalWidth += pulledComp.getPreferredW();
                    //System.out.println("cell width: "+newContainer.getPreferredW()+", "+totalWidth);
                    if(totalWidth >= newContainer.getPreferredW() - 10) {
                        lines++;
                        totalHeight += tallestPrefH;
                        //System.out.println("new line: "+tallestPrefH+", "+totalHeight);
                        tallestPrefH = 0;
                        totalWidth = pulledComp.getPreferredW();
                    }
                }//end if(newContainer.getLayout() instanceof FlowLayout)
            }//end for(int i = 0; i < childSize; i++)
            //System.out.println("total cell height: "+totalHeight+", "+lines+", "+childSize);
            if(newContainer.getLayout() instanceof FlowLayout) {
                newContainer.setPreferredH(totalHeight 
                        + newContainer.getStyle().getPadding(Component.BOTTOM) 
                        + newContainer.getStyle().getPadding(Component.TOP));
            }
        }
        setLayout(_cComp, -1);
    }//end resetAllTable(Component _cComp)
}

