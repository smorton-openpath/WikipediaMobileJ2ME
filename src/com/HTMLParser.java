/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import com.components.LinkButton;
import com.components.ImageButton;
import com.components.TableButton;


import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Label;
import com.sun.lwuit.io.NetworkManager;
import com.sun.lwuit.io.services.ImageDownloadService;
import com.sun.lwuit.layouts.*;
import com.sun.lwuit.table.*;
import com.sun.lwuit.Font;

import java.util.Vector;
import java.util.Stack;

/**
 *
 * @author caxthelm
 */

public class HTMLParser {
    private static final int STYLE_BOLD = 1;
    private static final int STYLE_ITALIC = 2;
    private static final int STYLE_HEADER = 4;
    private static final int STYLE_LINK = 8;
    private static final int STYLE_SHOWTABLES = 16;
    
    private static Vector tableVector = null;
    
    private static Font m_oFontBold = Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_BOLD , Font.SIZE_MEDIUM);
    private static Font m_oFontItalic = Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_ITALIC, Font.SIZE_MEDIUM);
    private static Font m_oFontBoldItalic = Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_BOLD | Font.STYLE_ITALIC, Font.SIZE_MEDIUM);
    
    public HTMLParser() {
    }
    
    public static Component parseHtml(String _sText, boolean _bShowTables) {
        Container cTextComp = new Container();//new HTMLRequestHandler());
        //cTextComp.setWidth(500);
        if(_sText == null) {            
            return cTextComp;
        }
        _sText = Utilities.replace("&#160;", " ", _sText);
        cTextComp.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
//        Vector vComponents = chopHTMLString(_sText);
        int initialParams = 0;
        Vector tags = Utilities.tokenizeString(_sText);
        if(_bShowTables) {
            initialParams += STYLE_SHOWTABLES;
            
        }else {
            tableVector = takeOutTables(_sText);
        }
        _sText = null;
        System.gc();
        Vector vComponents = parseHtmlTagVector(tags, initialParams);
        Container cLabelContainer = null;
        for(int i = 0; i < vComponents.size(); i++) {
            Object oComp = vComponents.elementAt(i);
            boolean addComp = true;
            
            //Failsafe to make sure all labels are in a container.
            if(oComp instanceof Label || oComp instanceof LinkButton) {
                if(cLabelContainer == null) {
                    cLabelContainer = new Container();
                    cTextComp.addComponent(cLabelContainer);
                }
                cLabelContainer.addComponent((Component)oComp);
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
        }
        
        cTextComp.getUnselectedStyle().setMargin(0, 0, 0, 0);
        cTextComp.getSelectedStyle().setMargin(0, 0, 0, 0);
        tableVector = null;
        return cTextComp;
    }
    
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
    private static Vector parseHtmlTagVector(Vector _vTags, int _iStyleMask) {
        nestedDepth++;
        //System.out.println("   ...depth: "+nestedDepth);
        Vector components = new Vector();
        
        while(_vTags.size() > 0) {
            String tag = (String) (_vTags.elementAt(0));
            //System.out.println("in parseHtmlTagVector, head tag is " + _vTags.firstElement().toString());
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
                    return flattenVectors(components);
                }
                
                if( baseTag.equalsIgnoreCase("a")) {
                    components.addElement(parseLink(_vTags, _iStyleMask));
                } else if( baseTag.equalsIgnoreCase("b")) {
                    components.addElement(parseBold(_vTags, _iStyleMask));
                } else if( baseTag.equalsIgnoreCase("br")) {
                    
                    components.addElement(parseText(" \n", _iStyleMask));
                    _vTags.removeElementAt(0);
                } else if(baseTag.equalsIgnoreCase("h2")) {
                    components.addElement(parseHeader(_vTags, _iStyleMask));
                } else if(baseTag.equalsIgnoreCase("i")) {
                    components.addElement(parseItalic(_vTags, _iStyleMask));
                } else if(baseTag.equalsIgnoreCase("img")) {
                    components.addElement(parseImage(_vTags, _iStyleMask));
                } else if(baseTag.equalsIgnoreCase("li")) {
                   components.addElement(parseListLine(_vTags, _iStyleMask));
                } else if(baseTag.equalsIgnoreCase("p")) {
                   components.addElement(parseParagraph(_vTags, _iStyleMask));
                } else if(baseTag.equalsIgnoreCase("table")) {
                    components.addElement(parseTable(_vTags, _iStyleMask));
                } else if(baseTag.equalsIgnoreCase("tr")) {
                    components.addElement(parseTableRow(_vTags, _iStyleMask));
                } else if(baseTag.equalsIgnoreCase("th")) {
                    components.addElement(parseTableHeader(_vTags, _iStyleMask));
                } else if(baseTag.equalsIgnoreCase("td")) {
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
                    components.addElement(text);
                }
                _vTags.removeElementAt(0);
            }
            //String tag2 = (String) (_vTags.elementAt(0));
        }//end while
        nestedDepth--;
        //System.out.println("   ...depth3: "+nestedDepth);
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
            Label newLabel = new Label(_sText);
            newLabel.setUIID("No_Margins");
            newComp = newLabel;
        }
        if((_iStyleMask & STYLE_BOLD) != 0 && (_iStyleMask & STYLE_ITALIC) != 0) {
            newComp.getStyle().setFont(m_oFontBoldItalic);
            newComp.getSelectedStyle().setFont(m_oFontBoldItalic);
            newComp.getPressedStyle().setFont(m_oFontBoldItalic);
        }else if((_iStyleMask & STYLE_BOLD) != 0 || (_iStyleMask & STYLE_HEADER) != 0) {
            newComp.getStyle().setFont(m_oFontBold);
            newComp.getSelectedStyle().setFont(m_oFontBold);
            newComp.getPressedStyle().setFont(m_oFontBold);
        }else if((_iStyleMask & STYLE_ITALIC) != 0) {
            newComp.getStyle().setFont(m_oFontItalic);
            newComp.getSelectedStyle().setFont(m_oFontItalic);
            newComp.getPressedStyle().setFont(m_oFontItalic);
        }
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
        _iStyleMask += STYLE_HEADER;
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
        if(width > 0 && width <= 60 ){//&& height > 0 && height <= 50) {
            Label newLabel = new Label();
            newLabel.setUIID("no_MarginsTransparent");
            ImageDownloadService img = new ImageDownloadService("http:"+srcText, newLabel);
            NetworkManager.getInstance().addToQueue(img);
            returnVec.addElement( newLabel);
            //System.out.println("made image: "+srcText);
        }else {
            //System.out.println("adding image: "+altText+", "+srcText);
            ImageButton newLink = new ImageButton(altText, srcText);
            //Add button to the list, reset the container
            returnVec.addElement(newLink);
        }
        return returnVec;
    }//end parseImage(Vector tags, int _iStyleMask)
    
    //<li>
    private static Vector parseListLine(Vector _vTags, int _iStyleMask) {       
        //Just do what paragraph does.
        return parseParagraph(_vTags, _iStyleMask);
    }//end parseListLine(Vector tags, int _iStyleMask)
    
    //<p>
    private static Vector parseParagraph(Vector _vTags, int _iStyleMask) {
        _vTags.removeElementAt(0);
        Vector compVec = flattenVectors(parseHtmlTagVector(_vTags, _iStyleMask));
        Vector returnVec = new Vector();
        Container newContainer = new Container();
        for(int i = 0; i < compVec.size(); i++) {
            //System.out.println("para: "+compVec.elementAt(i));
            newContainer.addComponent((Component)compVec.elementAt(i));
        }
        returnVec.addElement(newContainer);
        return returnVec;
    }//end parseParagraph(Vector tags, int _iStyleMask)
    
    //<table>
    private static Vector parseTable(Vector _vTags, int _iStyleMask) {
        Vector returnVec = new Vector();
        
        _vTags.removeElementAt(0);
        Vector compVec = parseHtmlTagVector(_vTags, _iStyleMask);
        compVec = stripVector(compVec);
        if((_iStyleMask & STYLE_SHOWTABLES) != 0 || (compVec.size() < 2 && false)) {
            Container newContainer = new Container();
            newContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
            newContainer.setUIID("Table");
            newContainer.setSnapToGrid(true);
            
            newContainer.setScrollableY(true);
            //newContainer.setScrollableX(true);
            int j = 0;
            for(int i = 0; i < compVec.size(); i++) {
                newContainer.addComponent((Component)compVec.elementAt(i));
            }
            
            returnVec.addElement(newContainer);
            if((_iStyleMask & STYLE_SHOWTABLES) == 0) {
                tableVector.removeElementAt(0);
            }
        }else {
            String title = null;//
            //System.out.println("title: "+title +", "+ compVec.firstElement());
            
            //Axthelm - This is ugly code, but I couldn't think of a better way to pull out the first item.
            //Axthelm - we expect the title to be the first item of the first row.
            if(compVec != null && compVec.size() > 0){
                if(compVec.firstElement() instanceof Label) {//if first item is a label, use that.
                    title = ((Label)compVec.firstElement()).getText();
                }else if(compVec.firstElement() instanceof Container) {//else use the first row.
                    Container trCont = (Container)compVec.firstElement();
                    if(trCont.getComponentAt(0) instanceof Label) {//if the row contains just a label (odd), use that.
                        title = ((Label)trCont.getComponentAt(0)).getText();
                    }else if(trCont.getComponentAt(0) instanceof Container) {//more likely it has a group of labels
                        Container tdCont = (Container)trCont.getComponentAt(0);
                        title = "";
                        // put them together and use it.
                        for(int i = 0; i < tdCont.getComponentCount(); i++) {
                            try{
                                title += ((Label)tdCont.getComponentAt(i)).getText();
                            } catch(ClassCastException cce) {
                                // If we don't have a network connection, we throw an exception here.
                            }
                        }//end for(int i = 0; i < tdCont.getComponentCount(); i++)
                    }//end if(trCont.getComponentAt(0) instanceof Label) - else
                }//end if(compVec.firstElement() instanceof Label) - else
            }//end if(compVec != null && compVec.size() > 0)
            
            if(title == null || title.length() <= 0) {
                title = mainMIDlet.getString("Table");
            }
            if(tableVector != null && tableVector.size() > 0) {
                TableButton newTable = new TableButton(title, (String)tableVector.elementAt(0));
                returnVec.addElement(newTable);
                tableVector.removeElementAt(0);
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
        newContainer.setUIID("TableRow");
        //System.out.println("  Row Comp: "+compVec.size());
        if(compVec.size() == 1) {
            BorderLayout border = new BorderLayout();
            border.setCenterBehavior(BorderLayout.CENTER_BEHAVIOR_CENTER_ABSOLUTE);
            newContainer.setLayout(border);
            //newContainer.setUIID("No_Margins");
            ((Component)compVec.elementAt(0)).setUIID("TableCellSpecial");
            newContainer.addComponent(BorderLayout.CENTER, (Component)compVec.elementAt(0));
        }else {
            newContainer.setLayout(new GridLayout(1, compVec.size()));
            for(int i = 0; i < compVec.size(); i++) {
                //System.out.println("  Row Comp: "+compVec.elementAt(i));
                newContainer.addComponent((Component)compVec.elementAt(i));
            }
        }
        newContainer.invalidate();
        returnVec.addElement(newContainer);
        //System.out.println(" ~table Row real: "+j+" of "+compVec.size());
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
        newContainer.setLayout(new BoxLayout(BoxLayout.X_AXIS));
        for(int i = 0; i < compVec.size(); i++) {
            //System.out.println("para: "+compVec.elementAt(i));
            newContainer.addComponent((Component)compVec.elementAt(i));
        }
        /*Label spacer = new Label(" ");
        spacer.setUIID("No_Margins");
        newContainer.addComponent(spacer);
        newContainer.invalidate();*/
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
        
        newContainer.setUIID("TableCell");
        for(int i = 0; i < compVec.size(); i++) {
            //System.out.println("cell: "+compVec.elementAt(i));
            newContainer.addComponent((Component)compVec.elementAt(i));
        }
        Label spacer = new Label(" ");
        spacer.setUIID("No_Margins");
        newContainer.addComponent(spacer);
        newContainer.invalidate();
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
    
    private static Vector takeOutTables(String _sText) {
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
            tableIdx = _sText.indexOf("<table", tableIdx);
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
                endTableIdx = _sText.indexOf("</table>", endTableIdx + 1);
            }
            //System.out.println("final Table: "+tableIdx+", "+endTableIdx);
            //That last end tag is our endpoint.  Put that string in our collection vector.
            if(endTableIdx != -1) {
                String test = _sText.substring(tableIdx, endTableIdx+8);
                //System.out.println("table: "+test);
                returnVec.addElement(test);
            }
            tableIdx++;//look for the next consecutive table.
        }
        return returnVec;
    }//end takeOutTables(String _sText)
}

