/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pages;

import com.*;
import com.components.LinkButton;
import com.components.ImageButton;
import com.components.TableButton;
import com.components.HTMLComponentItem;
import com.components.SectionComponentItem;

import com.sun.lwuit.*;
import com.sun.lwuit.events.*;
import com.sun.lwuit.Display;


import java.util.Vector;
import java.util.Stack;
import java.util.Hashtable;

/**
 *
 * @author caxthelm
 */
public class ArticlePage extends BasePage {
    //Common Command Ids ;
    private final int COMMAND_BACK = COMMAND_RIGHT;
    private final int COMMAND_SEARCH = COMMAND_CENTER;
    private final int COMMAND_SAVEPAGE = COMMAND_LEFT;
    private final int COMMAND_BOOKMARK = COMMAND_SAVEPAGE + 1;
    private final int COMMAND_DELETEBOOKMARK = COMMAND_BOOKMARK + 1;
    //private final int Command_Privacy = Command_Terms + 1;
    private final int COMMAND_HOME = COMMAND_DELETEBOOKMARK + 1;
    
    private Vector m_vArticleStack;
    
    private Label m_cTitleLabel;
    
    //Lwuit Commands:
    boolean m_bStartWithSearch = false;
    String m_sTitle = "";
    String m_sCurrentSections = "0";
    TextField searchTextField = null;
    
    Hashtable m_oComponentList = new Hashtable();
    
    private int[] m_iToRequest = new int[6];
    
    public int[] getRequestInts() {
        return m_iToRequest;
    }
    
    public ArticlePage(String _sTitle, boolean _bPerformSearch) {
        super("ArticlePageForm", PAGE_MAIN);
        
        
        m_vArticleStack = new Vector();
        
        if(!m_bIsLoaded) {
            //TODO: make error dialog.
            System.err.println("We failed to load");
            return;
        }
        m_bStartWithSearch = _bPerformSearch;
        m_sTitle = _sTitle;
        m_cTitleLabel = (Label)mainMIDlet.getBuilder().findByName("SubjectTitleLabel", m_cHeaderContainer);
        if(m_cTitleLabel != null) {
            String realTitle = m_sTitle.replace('_', ' ');
            m_cTitleLabel.setText(realTitle);
        }
        try {
            //Create dynamic components here.
            
            
            m_cForm.addShowListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    m_cForm.removeShowListener(this);
                    if(m_bStartWithSearch) {                        
                        //addData(m_oData, NetworkController.PARSE_SEARCH);
                        NetworkController.getInstance().performSearch(mainMIDlet.getLanguage(), m_sTitle, 0);
                    }else {
                        NetworkController.getInstance().fetchArticle(mainMIDlet.getLanguage(), m_sTitle, "0");
                    }
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
        str = mainMIDlet.getString("HomeSK");
        m_cForm.addCommand(new Command(str, COMMAND_HOME), i++);
        str = mainMIDlet.getString("SearchSK");
        m_cForm.addCommand(new Command(str, COMMAND_SEARCH), i++);
        //str = mainMIDlet.getString("SavepageSK");
        //m_cForm.addCommand(new Command(str, COMMAND_SAVEPAGE), i++);
        if(mainMIDlet.getBookmarks().recordExists(m_sTitle)){
            str = mainMIDlet.getString("DeleteBookmarkSK");
            m_cForm.addCommand(new Command(str, COMMAND_DELETEBOOKMARK), i++);
        }else {
            str = mainMIDlet.getString("BookmarkSK");
            m_cForm.addCommand(new Command(str, COMMAND_BOOKMARK), i++);
        }
        //str = mainMIDlet.getString("PrivacySK");
        //mForm.addCommand(new Command(str, Command_Privacy), Command_Privacy);
        str = mainMIDlet.getString("BackSK");
        m_cForm.addCommand(new Command(str, COMMAND_BACK), i++);
        
    }//end updateSoftkeys()
    
    public void actionPerformed(ActionEvent ae) {
        System.err.println("Action article: " + ae.getCommand().getId());
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
                    if(m_vArticleStack != null && m_vArticleStack.size() > 1) {
                        //Axthelm: removing the top of the stack (it is the current page).
                        m_vArticleStack.removeElementAt(m_vArticleStack.size() - 1); 
                        String[] titleAndSections = (String[])m_vArticleStack.lastElement();
                        System.out.println("popping: "+titleAndSections[0]);
                        m_vArticleStack.removeElementAt(m_vArticleStack.size() - 1);                    
                        NetworkController.getInstance().fetchArticle(mainMIDlet.getLanguage(), titleAndSections[0], titleAndSections[1]);
                        
                    } else {
                        mainMIDlet.pageBack();
                    }
                break;
            case COMMAND_LANGUAGE:
                NetworkController.getInstance().searchArticleLanguages(mainMIDlet.getLanguage(), m_sTitle, null);
                break;
            case COMMAND_SEARCH:
                    mainMIDlet.setCurrentPage(new SearchPage());
                break;
            case COMMAND_SAVEPAGE:
                    //mainMIDlet.setCurrentPage(new SearchPage());
                break;
            case COMMAND_BOOKMARK:
                if(!mainMIDlet.getBookmarks().recordExists(m_sTitle)) {
                    mainMIDlet.getBookmarks().saveRecord(m_sTitle);
                }
                break;
            case COMMAND_DELETEBOOKMARK:
                if(mainMIDlet.getBookmarks().recordExists(m_sTitle)) {
                    mainMIDlet.getBookmarks().deleteRecord(m_sTitle);
                }
                break;
            case COMMAND_HOME:
                    mainMIDlet.setCurrentPage(new MainPage(), true);
                break;
            case COMMAND_IMAGE:
                {
                    Component oComp = ae.getComponent();
                    if(oComp instanceof LinkButton) {
                        String url = "http:"+((LinkButton)oComp).getLink();
                        System.out.println("url: "+url);
                        mainMIDlet.setCurrentPage(new ImageDialog(
                                ((LinkButton)oComp).getOtherInfo(), ((LinkButton)oComp).getText(), url));
                    }
                }
                break;
            case COMMAND_LINK: //internal links
                {
                    Component oComp = ae.getComponent();
                    if(oComp instanceof LinkButton) {
                        String url = ((LinkButton)oComp).getLink();
                        int wikiIdx = url.indexOf("/wiki/");
                        if(wikiIdx >= 0) {
                            String title = url.substring(wikiIdx + 6);
                            //System.out.println("linkTitle: "+title);
                            NetworkController.getInstance().fetchArticle(mainMIDlet.getLanguage(), title,  "0");
                        }
                    }
                }
                break;
            case COMMAND_TABLE:
                {
                    Component oComp = ae.getComponent();
                    if(oComp instanceof LinkButton) {
                        //System.out.println("tableButton: "+((LinkButton)oComp).getLink());
                        mainMIDlet.setCurrentPage(new TablePage(((LinkButton)oComp).getText(), ((LinkButton)oComp).getLink()));
                    }
                }
                break;
            default://dealing with the dynamic events
                {
                    if(commandId > 40) {
                        String sID = String.valueOf(commandId - 40);
                        Object section = m_oComponentList.get(new Integer(commandId));
                        if(section instanceof SectionComponentItem) {
                            SectionComponentItem sectionItem = (SectionComponentItem)section;
                            if(sectionItem.isActive())
                            {
                                sectionItem.setActive(false);
                            }else {
                                int arrayLevel = Integer.parseInt(sectionItem.getTag()) - 1;
                                m_iToRequest[arrayLevel] = Integer.parseInt(sID);
                                m_sCurrentSections = "0";
                                
                                for(int i = 0; i < arrayLevel + 1; i++) {
                                    m_sCurrentSections += "|" + m_iToRequest[i];
                                }
                                
                                String[] toAdd = new String[2];
                                toAdd[0] = m_sTitle;
                                toAdd[1] = m_sCurrentSections;
                                m_vArticleStack.addElement(toAdd);
                                NetworkController.getInstance().fetchArticle(mainMIDlet.getLanguage(), m_sTitle,  m_sCurrentSections);
                            }
                        }//end if(section instanceof SectionComponentItem)
                    }//end if(commandId > 40)
                }
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
        //addData(null, NetworkController.PARSE_SEARCH);
        Thread.yield();
        
        super.refreshPage();
    }//end checkRefresh()
    
    public void addData(Object _results, int _iResultType) {
        //System.out.println("results: "+_iResultType +", "+_results);
        if(_results == null) {
            //We have nothing, make the data call.
            if(_iResultType == NetworkController.PARSE_SEARCH) {
                NetworkController.getInstance().performSearch(mainMIDlet.getLanguage(), m_sTitle, 0);
            }else if(_iResultType == NetworkController.FETCH_ARTICLE) {                
                NetworkController.getInstance().fetchArticle(mainMIDlet.getLanguage(), m_sTitle, m_sCurrentSections);
            }
        }else {
            switch(_iResultType) { 
                case NetworkController.PARSE_SEARCH:
                    parseSearch(_results);
                    break;
                case NetworkController.FETCH_ARTICLE:
                    String[] titleAndSections = null;
                    parseArticle(_results);
                    if(!m_vArticleStack.isEmpty()) {
                        titleAndSections = (String[])m_vArticleStack.lastElement();
                    }
                    //if we already have this article on the stack, don't add it.
                    if(titleAndSections == null || !titleAndSections[0].equalsIgnoreCase(m_sTitle))
                    {
                        String[] toAdd = new String[2];
                        toAdd[0] = m_sTitle;
                        toAdd[1] = m_sCurrentSections;
                        m_vArticleStack.addElement(toAdd);
                        System.out.println("pushing: "+toAdd[0]);
                    }else if (titleAndSections[0].equalsIgnoreCase(m_sTitle)) {
                        m_vArticleStack.removeElementAt(m_vArticleStack.size() - 1);
                    }
                    break;
                case NetworkController.SEARCH_LANGUAGES:
                    parseLanguage(_results);
                    break;
            }
            
        }
    }//end addData(Object _results, int _iResultType) 
    
    public void parseSearch(Object _results) {
        String realTitle = m_sTitle.replace('_', ' ');
        mainMIDlet.setCurrentPage(new ListDialog(realTitle, _results, 0));
    }//end parseSearch(Object _results)
    
    public void parseArticle(Object _results) {
        
        m_sTitle = Utilities.getNormalizedTitleFromJSON((JsonObject)_results);
        if(m_cTitleLabel != null) {
            String realTitle = m_sTitle.replace('_', ' ');
            m_cTitleLabel.setText(realTitle);
        }
        

        Vector sections = Utilities.getSectionsFromJSON((JsonObject)_results);
        if(m_cContentContainer != null && sections != null && sections.size() > 0)
        {
            m_cContentContainer.removeAll();
            m_cContentContainer.invalidate();
            m_cContentContainer.repaint();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Deal with the main article text first.
           
            Object oTextItem = sections.firstElement();
            if(oTextItem instanceof JsonObject) {
                String sText = (String)((JsonObject)oTextItem).get("text");
                sText = Utilities.stripSlash(sText); 
                HTMLComponentItem oHTMLItem = new HTMLComponentItem();
                Component cTextComp = oHTMLItem.createComponent(sText);
                if(cTextComp != null) {                   
                    m_cContentContainer.addComponent(cTextComp);
                }
            }//end if(oTextItem instanceof JsonObject)
            
            
            //Add in the other sections
            //Since we can cascade through sub-sections, we are using an Array to denote which level should get the child.
            //TODO: There must be a better way to do this.
            SectionComponentItem[] aSections = new SectionComponentItem[6];
            for(int i = 1; i < sections.size(); i++) {
                //System.out.println(sections.elementAt(i));
                JsonObject oSection = (JsonObject)sections.elementAt(i);
                String sTitle = (String)oSection.get("line");
                String sText = (String)oSection.get("text");
                boolean bActive = false;
                
                if(sText != null) {
                    bActive = true;
                }
                
                String sTocLevel = oSection.getString("toclevel");
                String sNumber = oSection.getString("number");
                //String sLevel = oSection.getString("level");
                String sID = oSection.getString("id");
                
                int arrayLevel = Integer.parseInt(sTocLevel) - 1;//TocLevels begin at 1;
                if(arrayLevel == 0 || (arrayLevel > 0 && aSections[arrayLevel - 1] != null 
                        && aSections[arrayLevel - 1].isActive())) 
                {
                    SectionComponentItem sectionItem = new SectionComponentItem(sTitle, 40 + i, sTocLevel);
                    Component cSectionComp = sectionItem.createComponent(sTitle, bActive, Integer.parseInt(sTocLevel));
                    if(cSectionComp != null) {
                        if(bActive) {
                            cSectionComp.requestFocus();
                            cSectionComp.setFocus(true);
                        }
                        m_oComponentList.put(new Integer(40 + i), sectionItem);
                            //Whatever level we are at we shouldn't have any more sub-levels yet.
                        for(int j = arrayLevel; j < 5; j++)
                        {
                            aSections[j] = null;
                        }
                        //set this item into the array and set it to the child of the parent.
                        aSections[arrayLevel] = sectionItem;

                        //System.out.println(sText);
                        
                        if(sText != null && !(sText.length() < 1)) {
                            //TODO: Need to strip out the <h2> tag at the beginning
                            sText = Utilities.stripSlash(sText);
                            sectionItem.addText(sText);
                        }
                        
                        if(arrayLevel == 0) {
                            m_cContentContainer.addComponent(cSectionComp);
                        }else {
                            aSections[arrayLevel - 1].addSubsection(sectionItem);
                        }
                    }//end if(cSectionComp != null)
                }
            }//end for(int i = 1; i > sections.size(); i++)
            if(m_sCurrentSections.equalsIgnoreCase("0")) {            
                Component first = m_cContentContainer.findFirstFocusable();
                if(first != null)
                {
                    first.setFocus(true);
                    first.requestFocus();
                }
            }
        }//end if(m_cContentContainer != null && sections != null && sections.size() > 0)
        m_cForm.repaint();
    }//end parseArticle(Object _results)
    
    public void parseLanguage(Object _results) {
        String realTitle = m_sTitle.replace('_', ' ');
        mainMIDlet.setCurrentPage(new LanguageDialog(realTitle, _results, 0));
    }//end parseSearch(Object _results)
    
    
}
