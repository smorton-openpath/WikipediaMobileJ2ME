/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pages;

import com.sun.lwuit.*;
import com.sun.lwuit.events.*;
import com.sun.lwuit.util.*;
import com.sun.lwuit.plaf.*;
import com.sun.lwuit.io.*;
import com.sun.lwuit.io.ui.*;
import com.sun.lwuit.Display;

import java.util.Hashtable;
import java.util.Vector;

import com.Utilities;
import com.mainMIDlet;
import com.JsonObject;
import com.NetworkController;
import com.components.ComponentItem;
import com.components.ListComponentItem;
/**
 *
 * @author caxthelm
 */
public class LanguageDialog extends BasePage
{
    //Common Command Ids ;
    private final int COMMAND_BACK = COMMAND_RIGHT;
    
    //Lwuit Commands:
    
    Container m_cListContainer = null;
    Hashtable m_hListObjects = new Hashtable();
    private int m_iCurrentOffset = 0;
    private String m_sContinue = "";
    private int m_iMaxResults = 0;
    private String m_sSearchText = "";
    
    
    public LanguageDialog(String _sTitle, Object _results, int _idx) {
        super("ListDialog", DIALOG_LANGUAGE);
        try {
            if(!m_bIsLoaded) {
                //TODO: make error dialog.
                System.err.println("We failed to load");
                return;
            }
            m_sSearchText = _sTitle;
            
            Label cTitle = (Label)mainMIDlet.getBuilder().findByName("SubjectTitleLabel", (Container)m_cDialog);
            if(cTitle != null) {
                cTitle.setText(m_sSearchText);
            }
            //Create dynamic components here.
            
            //Add softkeys here.
            if(!mainMIDlet.isTouchEnabled()) {
                String  str = mainMIDlet.getString("OkSK");
                //mForm.addCommand(new Command(str, Command_Back), Command_Back);
                m_cDialog.addCommand(new Command(str, COMMAND_OK));
                
                m_cDialog.addKeyListener(-5, this);
            }else {
                String  str = mainMIDlet.getString("BackSK");
                //mForm.addCommand(new Command(str, Command_Back), Command_Back);
                m_cDialog.addCommand(new Command(str, COMMAND_BACK));

                m_cDialog.addKeyListener(-5, this);
            }
            m_cDialog.addCommandListener(this);
            
            
            if(_results != null) {
                addData(_results, NetworkController.SEARCH_LANGUAGES);
            }
            new Thread(new Runnable()  {
                public void run()  {
                    m_cDialog.show(10, 10, 10, 10, false);
                }
            }).start();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }//end ListDialog(Vector _vItems, int _idx, boolean _isAttribute)
    
    public void actionPerformed(ActionEvent ae) {
        int commandId = -2;
        if(ae.getKeyEvent() == -5) {
            commandId = COMMAND_OK;
        }else {
            commandId = ae.getCommand().getId();
        }
        if(commandId == COMMAND_OK) {
            Component focusedComp = m_cDialog.getFocused();
            if(focusedComp instanceof Button) {
                Button test = (Button)focusedComp;
                commandId = test.getCommand().getId();
            }else if(focusedComp instanceof Container) {
                Container testCont = (Container)focusedComp;
                Button test = (Button)testCont.getLeadComponent();
                if(test != null && testCont.getLeadComponent() instanceof Button) {
                    commandId = test.getCommand().getId();
                }
            }
        }//end if(commandId == COMMAND_OK)
        System.err.println("Action listDialog: " + commandId);
        switch(commandId) {
            case COMMAND_BACK:
                m_cDialog.dispose();
                mainMIDlet.dialogBack();
                break;
                
            case COMMAND_NEXT:
                    m_iCurrentOffset += NetworkController.SEARCH_LIMIT;                    
                    NetworkController.getInstance().searchArticleLanguages(mainMIDlet.getLanguage(), m_sSearchText, m_sContinue);
                ae.consume();
                break;
            case COMMAND_PREV:
                if(m_iCurrentOffset >= NetworkController.SEARCH_LIMIT) {
                    m_iCurrentOffset -= NetworkController.SEARCH_LIMIT;
                    //NetworkController.getInstance().performSearch(mainMIDlet.getLanguage(), m_sSearchText, m_iCurrentOffset);
                }
                ae.consume();
                break;
                
                
            default://dealing with the dynamic events
                {
                    Integer newInt = new Integer(commandId);
                    ListComponentItem comp = (ListComponentItem)m_hListObjects.get(newInt);
                    if(comp == null)
                        break;
                    String text = comp.getTag();
                    String lang = text.substring(0, text.indexOf(" ")).trim();
                    text = text.substring(text.indexOf(" - ") + 3);
                    //System.out.println("requesting article: "+text);
                    m_cDialog.dispose();
                    mainMIDlet.dialogBack();
                    Thread.yield();
                    NetworkController.getInstance().fetchArticle(lang, text, "0");
                    //System.out.println("the selection: " + mainItem.getLocName()+", value: " + newInt.intValue());
                    //System.out.println("the new selection: " + mainItem.getSelected().getLocName()+", value: " + mainItem.getSelected().getID());
                        //mainMIDlet.insertCurrentCat(item, typeIdex);
                }
                //mainMIDlet.dialogBack();
                break;
        }
    }//end actionPerformed(ActionEvent ae)
    
    
    public void addData(Object _results, int _iResultType) {
        //System.out.println("results: "+_iResultType+", "+_results);
        if(!(_results instanceof JsonObject)) {
            return;//TODO: do something with the dialog.
        }
        
        if(m_cContentContainer != null) {
            m_cContentContainer.removeAll();
            m_hListObjects.clear();
            m_sContinue = null;
            Object oQuery = ((JsonObject)_results).get("query-continue");
            if(oQuery != null && oQuery instanceof JsonObject) {
                Object oSearchInfo = ((JsonObject)oQuery).get("langlinks");
                if(oSearchInfo != null && oSearchInfo instanceof JsonObject) {
                    JsonObject searchObj = (JsonObject)oSearchInfo;
                    m_sContinue = (String)searchObj.get("llcontinue");
                }
            }
            //Previous Button
            if(m_iCurrentOffset > 0) {
                Button prevButton = new Button();
                prevButton.setUIID("Button");
                String text = mainMIDlet.getString("PrevPage");
                Command nextPage = new Command(text, COMMAND_PREV);
                prevButton.setCommand(nextPage);
                m_cContentContainer.addComponent(prevButton);
            }
            
            //List Items
            Vector vItems = Utilities.getLanguagesFromJSON((JsonObject)_results);
            if(vItems == null) {
                System.out.println("no resutls: "+_results);
                return;
            }
            for(int i = 0; 
                    i < vItems.size() && i < NetworkController.SEARCH_LIMIT ; i++)
            {
                 JsonObject item = (JsonObject)vItems.elementAt(i);
                 ListComponentItem listItem = new ListComponentItem(40+i);
                 Component comp = listItem.createComponent(Utilities.decodeEverything((String)item.get("lang")+" - "+(String)item.get("*")));
                 if(comp != null) {
                     
                     m_cContentContainer.addComponent(comp);
                     m_hListObjects.put(new Integer(40+i), listItem);
                 }
            }
            
            //Next Button
            if(vItems.size() >= NetworkController.SEARCH_LIMIT 
                    && (m_sContinue != null && m_sContinue.length() > 0))
            {
                Button nextButton = new Button();
                nextButton.setUIID("Button");
                String text = mainMIDlet.getString("NextPage");
                Command nextPage = new Command(text, COMMAND_NEXT);
                nextButton.setCommand(nextPage);
                m_cContentContainer.addComponent(nextButton);
            }
            Component first = m_cContentContainer.findFirstFocusable();
            if(first != null) {
                first.setFocus(true);
                first.requestFocus();
            }
        }//end if(mContentContainer != null)
        m_cDialog.repaint();
    }//end addData(Object _results, int _iResultType)
}
