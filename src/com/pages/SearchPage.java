/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pages;

import com.sun.lwuit.*;
import com.sun.lwuit.events.*;
import com.sun.lwuit.Display;


import java.util.Vector;

import com.mainMIDlet;
import com.NetworkController;
import javax.microedition.lcdui.Canvas;
/**
 *
 * @author caxthelm
 */
public class SearchPage extends BasePage {
    //Common Command Ids ;
    private final int COMMAND_BACK = COMMAND_RIGHT;
    private final int COMMAND_HOME = COMMAND_CENTER;
    
    //Lwuit Commands:   
    
    TextField m_cSearchTextField = null;
    Button m_cSearchButton = null;
    int m_iFirstBookmarkNum = 0;
    public SearchPage() {
        super("SearchPageForm", PAGE_SEARCH);
        try {
            if(!m_bIsLoaded) {
                //TODO: make error dialog.
                System.err.println("We failed to load");
                return;
            }
            //Create dynamic components here.
            
            m_cSearchTextField = (TextField)mainMIDlet.getBuilder().findByName("SearchTextField", m_cHeaderContainer);
            m_cSearchTextField.setHandlesInput(true);
            m_cSearchButton = (Button)mainMIDlet.getBuilder().findByName("SearchIconButton", m_cHeaderContainer);            
            if(m_cSearchButton != null) {
                m_cSearchButton.setVisible(false);
                
                m_cForm.addGameKeyListener(Canvas.LEFT, new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        m_cSearchButton.requestFocus();
                    }
                });
            }
            if(m_cSearchTextField != null) {
                m_cSearchTextField.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        TextField myText = (TextField)ev.getComponent();
                        if(ev.getKeyEvent() == Canvas.LEFT) {
                            m_cSearchButton.requestFocus();
                        } else {
                            if(!Display.getInstance().editingText) {
                                Display.getInstance().editString(ev.getComponent(), myText.getMaxSize(), myText.getConstraint(), myText.getText());
                            }
                        }
                    }
                });
                
                m_cSearchTextField.addDataChangeListener(new DataChangedListener()  {
                    public void dataChanged(int i, int i1) {
                        
                        
                        if(m_cSearchTextField != null) {
                            String message = m_cSearchTextField.getText();
                            if(m_cSearchButton != null) {
                                if(message != null && !message.equalsIgnoreCase(""))
                                {
                                    m_cSearchButton.setVisible(true);
                                }else 
                                    m_cSearchButton.setVisible(false);
                            }
                            if(message.indexOf('\n') > -1) {
                                m_cSearchTextField.setText(message.trim());
                                performSearch();
                            }
                        }
                        m_cForm.repaint();
                    }
                });
            }
            
            m_cForm.addKeyListener(-4, new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    Component focusedComp = m_cForm.getFocused();
                    if(!(focusedComp instanceof TextField) || ((TextField)focusedComp).getText().length() == 0) {
                        //mainMIDlet.setCurrentPage(new BrowsePage(), true);
                    }
                }
            });
            m_cForm.addKeyListener(-3, new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    Component focusedComp = m_cForm.getFocused();
                    if(!(focusedComp instanceof TextField) || ((TextField)focusedComp).getText().length() == 0) {
                        //mainMIDlet.setCurrentPage(new WatchListPage(), true);
                    }
                }
            });
            //Add softkeys here.
            m_cForm.addShowListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    m_cForm.removeShowListener(this);
                    addData(null, NetworkController.PARSE_SEARCH);
                }
            });
            updateSoftkeys();
            m_cForm.addCommandListener(this);
            mainMIDlet.getBuilder().setHomeForm("SearchPageForm");
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
        //str = mainMIDlet.getString("BackSK");
        //m_cForm.addCommand(new Command(str, COMMAND_BACK), i++);
        
    }//end updateSoftkeys()
    
    public void actionPerformed(ActionEvent ae) {
        System.err.println("Action search: " + ae.getCommand().getId());
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
                //mainMIDlet.pageBack();
                mainMIDlet.setCurrentPage(new MainPage(), true);
                break;
            case COMMAND_HOME:
                {
                    mainMIDlet.setCurrentPage(new MainPage(), true);
                }
                break;                
            case COMMAND_SEARCHBUTTON:
        performSearch();
                break;
            default:
                if(commandId >= m_iFirstBookmarkNum){
                    //Bookmarks
                    Button bookmarkComp = (Button)ae.getComponent();
                    mainMIDlet.setCurrentPage(new ArticlePage(bookmarkComp.getText(), false));
                }else {
                    //Saved Pages
                }
                break;
        }
    }//end actionPerformed(ActionEvent ae)

    private void performSearch() {
        //TODO: Network connection to get "did you mean" items. 
        if(m_cSearchButton != null && m_cSearchButton.isVisible()) {
            String text = "";
            if(m_cSearchTextField != null) {
                text = m_cSearchTextField.getText();
            }
            if(text.length() > 0) {
                mainMIDlet.setCurrentPage(new ArticlePage(text, true));
            }
        }
    }
    
    public void refreshPage() {        
        m_cForm.addShowListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                //System.out.println("reshowing search");
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
        //Create Saved Pages section.
        if(m_cContentContainer == null) {
            return;//TODO: display error.
        }
        int itemCounter = 40;
        String midletSavedPages = mainMIDlet.getMIDlet().getAppProperty("AllowSavePages");
        int numSavePagesAllowed = 0;
        if(midletSavedPages != null && midletSavedPages.length() > 0) {
            numSavePagesAllowed = Integer.parseInt(midletSavedPages);
        }
        if(numSavePagesAllowed > 0) {
            Container cCont = mainMIDlet.getBuilder().createContainer(mainMIDlet.getResources(), "SubjectTitleItem");
            if(cCont != null) {
                Label cTitle = (Label)mainMIDlet.getBuilder().findByName("SubjectTitleLabel", cCont);
                if(cTitle != null) {
                    cTitle.setText(mainMIDlet.getString("SavedPagesTitle"));
                }
            }
            m_cContentContainer.addComponent(cCont);
        }
        
        //Create Bookmark section.
        Container cCont = mainMIDlet.getBuilder().createContainer(mainMIDlet.getResources(), "SubjectTitleItem");
        if(cCont != null) {
            Label cTitle = (Label)mainMIDlet.getBuilder().findByName("SubjectTitleLabel", cCont);
            if(cTitle != null) {
                cTitle.setText(mainMIDlet.getString("BookmarksTitle"));
            }
        }
        m_cContentContainer.addComponent(cCont);
        Vector vBookmarks = mainMIDlet.getBookmarks().loadRecords();
        itemCounter++;
        m_iFirstBookmarkNum = itemCounter;
        for(int i = 0; i < vBookmarks.size(); itemCounter++, i++) {
            String bookmark = vBookmarks.elementAt(i).toString();
            bookmark = bookmark.replace('_', ' ');
            Button newButton = new Button();
            newButton.setUIID("LabelButtonLink");
            newButton.setCommand(new Command(bookmark, itemCounter));
            m_cContentContainer.addComponent(newButton);
            
        }
        
        m_cForm.repaint();
    }//end addData(Object _results)
    
    
}
