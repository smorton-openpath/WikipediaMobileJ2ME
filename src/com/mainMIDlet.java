
package com;

import com.pages.ArticlePage;
import com.pages.BasePage;
import com.pages.SplashPage;

import com.sun.lwuit.*;
import com.sun.lwuit.io.NetworkManager;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.Resources;
import com.sun.lwuit.util.UIBuilder;

import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import javax.microedition.midlet.MIDlet;

public class mainMIDlet extends MIDlet
{

    private static Resources m_rMainResource = null;
    private static UIBuilder m_rMainBuilder = null;
    private static Hashtable m_hMainLocale = null;
    private static mainMIDlet m_oInstance;
    private static boolean m_bHasTouch = false;
    
    private static String m_sLanguage = "en";
    
    
    private static Vector m_vScreenStack = null;
    private static DeviceStorage m_oSavedPages = null;
    private static DeviceStorage m_oSavedBookmarks = null;
    
    private static String m_sPreviousSearchQuery = "";
    private static String RMS_SAVEDPAGES = "SavedPages";
    private static String RMS_BOOKMARKS = "Bookmarks";
    
    public static final int TYPE_S40 = 0;
    public static final int TYPE_S40W = 1;
    public static final int TYPE_S60 = 2;
    
    public static int m_iPhoneType = TYPE_S40;
    
    public static boolean m_bUseMainSection = true;
    
    public static mainMIDlet getMIDlet() {
        return m_oInstance;
    }//end getMIDlet()
    
    public void startApp() {
        m_oInstance = this;
        Display.init(this);
        m_vScreenStack = new Vector();
        m_bHasTouch = Display.getInstance().isTouchScreenDevice();
        Display.getInstance().setDefaultVirtualKeyboard(null);
        boolean bHaveNetwork = true;
        try {
            HttpConnection connector = (HttpConnection) Connector.open("http://en.m.wikipedia.org");
            if(connector != null) {
                int responseCode = connector.getResponseCode();
                connector.close();
            }
        }catch(Exception e) {
            bHaveNetwork = false;
            e.printStackTrace();
        }
        if(bHaveNetwork) {
            NetworkManager.getInstance().start();
        }
        //getWidth();
        //Load the Theme
        if(getResources() == null || getBuilder() == null) {
            //TODO: Something went wrong.  Deal with this.
        }
        try {
            String sBaseURL = getString("BaseURL");
            String sBaseAPI = getString("BaseAPI");
            if(sBaseURL.length() > 0) {
                NetworkController.BASE_URL = sBaseURL;
            }
            if(sBaseAPI.length() > 0) {
                NetworkController.WEBAPI = sBaseAPI;
            }
            
        } catch (Exception e)        
        {
            e.printStackTrace();
        }
        //Even if something goes wrong we should still display the splash screen.
        //It is needed as a background for any error messages.
        setCurrentPage(new SplashPage(bHaveNetwork));        
    }//end startApp()
    
    public void endApp() {
        NetworkController.getInstance().cancelNetwork();
        NetworkController.hideLoadingDialog();
    }//end endApp()

    public void pauseApp() {
        NetworkController.getInstance().cancelNetwork();
        NetworkController.hideLoadingDialog();
    }//end pauseApp()

    public void destroyApp(boolean unconditional) {
    }//end destroyApp(boolean unconditional)
    
    public static Resources getResources() {
        if(m_rMainResource == null) {
            try {
                
                m_rMainResource = Resources.open("/WikiResource.res");//getClass().getResourceAsStream("/theme.res")
                UIManager.getInstance().setThemeProps(m_rMainResource.getTheme("LargeTheme"));
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        return m_rMainResource;
    }//end getResources()
    
    public static UIBuilder getBuilder() {
        if(m_rMainBuilder == null) {
            
            m_rMainBuilder = new UIBuilder();
            m_rMainBuilder.setHomeForm("SearchPageForm");
            m_rMainBuilder.setKeepResourcesInRam(false);
            m_rMainBuilder.registerCustomComponent("WebBrowser", com.sun.lwuit.io.ui.WebBrowser.class);
        }
        return m_rMainBuilder;
    }//end getBuilder()
    
    public static boolean isTouchEnabled() {
        return m_bHasTouch;
    }//end isTouchEnabled()
    
    public static String getString(String _sKey) {
        if(m_hMainLocale == null) {
            String defaultLocale = System.getProperty("microedition.locale");
            if(defaultLocale.length() > 2) {
                defaultLocale = defaultLocale.substring(0,2);
            }
            setLanguage(defaultLocale);
            pullL10N(defaultLocale);
            if(m_hMainLocale == null) {
                return "";
            }
            UIManager.getInstance().setResourceBundle(m_hMainLocale);
        }
        String text = "";
        Object item = m_hMainLocale.get(_sKey);
        if(item != null) {
            text = item.toString();
        }else {
            System.out.println("missing string: "+_sKey);
        }
        return text;
    }//end getString(String _sKey)
    
    private static void pullL10N(String _sLangCode) {
        
        try {
            //throws an exception if all of the strings aren't present
            m_hMainLocale = getResources().getL10N("WikiLoc",_sLangCode);
        }catch(Exception e) {
            m_hMainLocale = getResources().getL10N("WikiLoc","en");
        }
    }//end pullL10N(String _sLangCode)
    
    public static String getLanguage() {
        return m_sLanguage;
    }//end getLanguage()
    
    public static void setLanguage(String _sLanguage) {
        m_sLanguage = _sLanguage;
    }//end setLanguage(String _sLanguage)
    
    public static void setCurrentPage(BasePage _oPage, boolean _bResetStack) {
        //System.out.println("pageLoaded: "+page.isLoaded);
        if(!_oPage.m_bIsLoaded) {
            return;
        }
        if(_bResetStack) {
            while(!m_vScreenStack.isEmpty()) {
                BasePage item = (BasePage)m_vScreenStack.lastElement();
                item.dispose();
                m_vScreenStack.removeElement(item);
                getBuilder().back(null);
            }
            //screenStack.removeAllElements();
        }
        m_vScreenStack.addElement(_oPage);
        _oPage.showForm();
        Thread.yield();
        System.out.println("!@#$% page memory: "+Runtime.getRuntime().freeMemory());
        //System.out.println("checkSize: "+screenStack.size());
    }
    public static void setCurrentPage(BasePage _oPage) {
        setCurrentPage(_oPage, false);
    }//end setCurrentPage(BasePage _oPage)
    
    public static BasePage getCurrentPage() {
        BasePage item = (BasePage)m_vScreenStack.lastElement();
        //System.out.println("got Page: "+item.getType());
        return item;
    }//end getCurrentPage()
    
    protected void SizeChanged() {
        getCurrentPage().refreshPage();
    }
    
    public static void pageBack() {
        if(m_vScreenStack.size() < 2) {//Don't go back if we have nothing to go back to.
            //System.out.println("nothing to go back to");
            return;
        }
        //System.out.println("wanting to show: " +newLast.getType());
        //newLast.showForm();
        BasePage last = (BasePage)m_vScreenStack.lastElement();
        m_vScreenStack.removeElementAt(m_vScreenStack.size() - 1);
        last.dispose();
        getBuilder().back(((BasePage)m_vScreenStack.lastElement()).getForm());
        BasePage newLast = (BasePage)m_vScreenStack.lastElement();
        //newLast.refreshPage();
        //newLast.updateSoftkeys();
        newLast.showForm();
        Thread.yield();
        //System.out.println("checkSize: "+screenStack.size());
    }//end pageBack()
    
    public static void dialogBack() {
        if(m_vScreenStack.size() < 2) {//Don't go back if we have nothing to go back to.
            return;
        }
        //System.out.println("wanting to show: " +newLast.getType());
        BasePage last = (BasePage)m_vScreenStack.lastElement();
        last.dispose();
        m_vScreenStack.removeElementAt(m_vScreenStack.size() - 1);
        BasePage newLast = (BasePage)m_vScreenStack.lastElement();
        newLast.showForm();
        Thread.yield();
        //System.out.println("checkSize: "+screenStack.size());
    }//end dialogBack()
    
    public static void showAboutDialog() {
        try  {
            Dialog about = (Dialog)getBuilder().createContainer(getResources(), "AboutDialog");
            about.setScrollableY(true);
            TextArea textArea = (TextArea)getBuilder().findByName("AboutText", about);
            String OkSk = mainMIDlet.getString("OKLabel");
            about.addCommand(new Command(OkSk));
            
            String title = mainMIDlet.getString("AppTitle");
            String text = mainMIDlet.getString("CopyrightText");
            String supportText = mainMIDlet.getString("SupportText");
            String version = mainMIDlet.getMIDlet().getAppProperty("MIDlet-Version");
            String finalText = ""+title+"\n"+text+"\n"+supportText+"\n\nver: "+version + ", Rev "+mainMIDlet.getMIDlet().getAppProperty("MIDlet-Revision");
            if(textArea != null && about != null) {
                textArea.setText(finalText);
                about.show(10, 10, 10, 10, false);
            }else{
                //System.out.println("failed: "+textArea+", "+about);
            }
        }catch (Exception e ) {
            e.printStackTrace();
        }
    }//end showAboutDialog()
    
    public static DeviceStorage getBookmarks() {
        if(m_oSavedBookmarks == null) {
            m_oSavedBookmarks = new DeviceStorage(RMS_BOOKMARKS);
        }
        return m_oSavedBookmarks;
    }
    
    
    
}