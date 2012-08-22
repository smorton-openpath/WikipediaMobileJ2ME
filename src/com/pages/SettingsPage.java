/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pages;

import com.JsonObject;
import com.sun.lwuit.*;
import com.sun.lwuit.events.*;
import com.sun.lwuit.Display;


import java.util.Vector;

import com.mainMIDlet;
import com.NetworkController;
import com.Utilities;
import com.components.ListComponentItem;
import java.util.Enumeration;
import java.util.Hashtable;
/**
 *
 * @author caxthelm
 */
public class SettingsPage extends BasePage {
    //Common Command Ids ;
    private final int COMMAND_BACK = COMMAND_RIGHT;
    private final int COMMAND_TERMS = 23;
    private final int COMMAND_PRIVACY = 24;
    private final int COMMAND_ABOUT = 25;
    
    //Lwuit Commands:   
    
    TextField searchTextField = null;
    Container m_cLanguageContainer = null;
    private int m_iCurrentOffset = 0;
    private String m_sContinue = "";
    Hashtable m_hListObjects = new Hashtable();
    public SettingsPage() {
        super("SettingsPageForm", PAGE_SETTINGS);
        
        if(!m_bIsLoaded) {
            //TODO: make error dialog.
            System.err.println("We failed to load");
            return;
        }
        try {
            //Create dynamic components here.
            
            searchTextField = (TextField)mainMIDlet.getBuilder().findByName("SearchTextField", m_cForm);
            if(searchTextField != null) {
                searchTextField.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        TextField myText = (TextField)ev.getComponent();
                        if(!Display.getInstance().editingText) {
                            Display.getInstance().editString(ev.getComponent(), myText.getMaxSize(), myText.getConstraint(), myText.getText());
                        }
                    }
                });
            }
            m_cForm.addShowListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    m_cForm.removeShowListener(this);
                    NetworkController.getInstance().searchLanguages(mainMIDlet.getLanguage(), m_sContinue);
                    //addData(null, NetworkController.PARSE_SEARCH);
                }
            });
            updateSoftkeys();
            m_cForm.addCommandListener(this);
            //mForm.repaint();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }//end SearchPage()
    
    public void updateSoftkeys() {
        int i = 0;
        m_cForm.removeAllCommands();
        String  str = "";
        str = mainMIDlet.getString("BackSK");
        m_cForm.addCommand(new Command(str, COMMAND_BACK), i++);
    }//end updateSoftkeys()
    
    public void actionPerformed(ActionEvent ae) {
        System.err.println("Action Settings: " + ae.getCommand().getId());
        int commandId = ae.getCommand().getId();
        if(commandId == COMMAND_OK) {
            Component focusedComp = m_cForm.getFocused();
            if(focusedComp instanceof Button){
                Button test = (Button)focusedComp;
                commandId = test.getCommand().getId();
            }else if(focusedComp instanceof Container) {
                Container testCont = (Container)focusedComp;
                Button test = (Button)testCont.getLeadComponent();
                if(test != null && testCont.getLeadComponent() instanceof Button) {
                    commandId = test.getCommand().getId();
                }
            }
        }
        switch(commandId) {                
            //Softkeys
            case COMMAND_BACK:
                mainMIDlet.pageBack();
                break;
            case COMMAND_TERMS:
                mainMIDlet.setCurrentPage(new ArticlePage("Terms of Use", false, true));
                break;
            case COMMAND_PRIVACY:
                mainMIDlet.setCurrentPage(new ArticlePage("Privacy Policy", false, true));
                //mainMIDlet.setCurrentPage(new ArticlePage(title, false));
                break;
            case COMMAND_ABOUT:
                mainMIDlet.showAboutDialog();
                break;
            case COMMAND_NEXT:
                m_iCurrentOffset += NetworkController.SEARCH_LIMIT;                    
                NetworkController.getInstance().searchLanguages(mainMIDlet.getLanguage(), m_sContinue);
                break;
            case COMMAND_PREV:
                if(m_iCurrentOffset >= NetworkController.SEARCH_LIMIT) {
                    m_iCurrentOffset -= NetworkController.SEARCH_LIMIT;
                    //NetworkController.getInstance().performSearch(mainMIDlet.getLanguage(), m_sSearchText, m_iCurrentOffset);
                }
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
                    System.out.println("requesting article: "+lang);
                    mainMIDlet.setLanguage(lang);
                    //Dialog
                    String netTitle = mainMIDlet.getString("LangDialogTitle");
                    m_cFailDialog = new Dialog(netTitle);
                    if(m_cFailDialog != null) {
                        String OkSk = mainMIDlet.getString("ok");
                        Command commands = new Command(OkSk);
                        m_cFailDialog.addCommand(new Command(OkSk){
                            public void actionPerformed(ActionEvent ev) {
                                m_cFailDialog.dispose();
                                m_cForm.show();
                                }
                            });

                        String netText = mainMIDlet.getString("LangDialogText") + text+"("+lang+")";
                        TextArea textArea = new TextArea(netText);
                        textArea.setFocusable(false);
                        textArea.setUIID("Label");
                        textArea.setEditable(false);
                        m_cFailDialog.addComponent(textArea);

                        int width = Display.getInstance().getDisplayWidth();
                        int height = Display.getInstance().getDisplayHeight();
                        // Devices with very small screens should use showmodeless instead
                        if(height < 130) {
                            m_cFailDialog.showModeless();
                        } else {
                            m_cFailDialog.show(height/5, height/5, width/16, width/16, false, false);
                        }
                    }//end dialog
                    //System.out.println("the selection: " + mainItem.getLocName()+", value: " + newInt.intValue());
                    //System.out.println("the new selection: " + mainItem.getSelected().getLocName()+", value: " + mainItem.getSelected().getID());
                        //mainMIDlet.insertCurrentCat(item, typeIdex);
                }
                //mainMIDlet.dialogBack();
                break;
        }
    } //end actionPerformed(ActionEvent ae)
    
    public void refreshPage() {        
        m_cForm.addShowListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                //Putting the refresh in a show listener to make sure the page is ready to refresh.
                m_cForm.removeShowListener(this);
                checkRefresh();
            }
        });
    }//end refreshPage()
    
    private void checkRefresh() {
        NetworkController.hideLoadingDialog();
        addData(null, NetworkController.PARSE_SEARCH);
        Thread.yield();
        
        super.refreshPage();
    }//end checkRefresh()
    
    public void addData(Object _results, int _iResultType) {
        //System.out.println("results: "+_results);
        if(m_cContentContainer != null && m_cLanguageContainer == null) {
            m_cLanguageContainer = (Container)mainMIDlet.getBuilder().findByName("LanguageContainer", m_cContentContainer);
        }
        if(m_cLanguageContainer == null) {
            return;
        }
        
        m_cLanguageContainer.removeAll();
        m_hListObjects.clear();
        m_sContinue = null;
        Object oQuery = ((JsonObject)_results).get("query-continue");
        if(oQuery != null && oQuery instanceof JsonObject) {
            Object oSearchInfo = ((JsonObject)oQuery).get("sitematrix");
            if(oSearchInfo != null && oSearchInfo instanceof JsonObject) {
                JsonObject searchObj = (JsonObject)oSearchInfo;
                m_sContinue = (String)searchObj.get("smcontinue");
            }
        }
        //Previous Button
        if(m_iCurrentOffset > 0) {
            Button prevButton = new Button();
            prevButton.setUIID("Button");
            String text = mainMIDlet.getString("PrevPage");
            Command nextPage = new Command(text, COMMAND_PREV);
            prevButton.setCommand(nextPage);
            m_cLanguageContainer.addComponent(prevButton);
        }

        //List Items
        //Vector vItems = Utilities.getLanguagesFromJSON((JsonObject)_results);
        Hashtable vItems = null;
        oQuery = ((JsonObject)_results).get("sitematrix");
        //System.out.println("results: "+oQuery);
        if(oQuery != null && oQuery instanceof Hashtable) {
            vItems = (Hashtable)oQuery;
            //int i = 0;
            int offset = -1;
            for(Enumeration en = vItems.keys(); en.hasMoreElements();){
                int tryer = -1;
                try {//Try-catch to catch any non-numeric objects.  We don't want those.
                    tryer = Integer.parseInt((String)en.nextElement());
                }catch(Exception e){
                    continue;
                }
                if(offset == -1 || tryer < offset) {
                    offset = tryer;
                }
            }
            if(offset < 0) {//if we have nothing valid, just fail out.
                m_cForm.repaint();
                return;
            }
            for(int i = 0 + offset; i - offset < vItems.size() ; i++) {
                if(!vItems.containsKey(""+i)) {
                    continue;
                }
                JsonObject item = (JsonObject)vItems.get(""+i);
                ListComponentItem listItem = new ListComponentItem(40+i);
                Component comp = listItem.createComponent(Utilities.decodeEverything((String)item.get("code")+" - "+(String)item.get("name")));
                if(comp != null) {
                    m_cLanguageContainer.addComponent(comp);
                    m_hListObjects.put(new Integer(40+i), listItem);
                }
            }//end for(Enumeration en = vItems.keys(); en.hasMoreElements();i++)
            
        }

        //Next Button
        if(vItems != null && vItems.size() >= NetworkController.SEARCH_LIMIT 
                && (m_sContinue != null && m_sContinue.length() > 0))
        {
            Button nextButton = new Button();
            nextButton.setUIID("Button");
            String text = mainMIDlet.getString("NextPage");
            Command nextPage = new Command(text, COMMAND_NEXT);
            nextButton.setCommand(nextPage);
            m_cLanguageContainer.addComponent(nextButton);
        }
        Component first = m_cLanguageContainer.findFirstFocusable();
        if(first != null) {
            first.setFocus(true);
            first.requestFocus();
        }
        
        m_cForm.repaint();
    }//end addData(Object _results)
    
    
}
