package com;
import com.sun.lwuit.*;
import com.sun.lwuit.Display;


import javax.microedition.io.HttpConnection;

/**
 * @author caxthelm
 */ 
public class NetworkController {
    
    public static String BASE_URL = "http://en.wikipedia.org";
    public static String WEBAPI = "/w/api.php";
    public static String APIVERSION = "";
    
    static String m_sResponseString = "";
    private static NetworkController m_oInstance = null;
    private static Dialog m_cLoadingDialog = null;
    private static NetworkThread m_tNetThread = null;
    public static final int SEARCH_LIMIT = 10;
    
//    static MemoryDisplayer md;
    
    public static final int PARSE_SEARCH = 0;
    public static final int FETCH_ARTICLE = 1;
    public static final int SEARCH_LANGUAGES = 2;
    public static final int FETCH_TERMS = 3;
    
    private static Slider m_cLoadingSlider = null;
    private NetworkController() {
    }

    public static NetworkController getInstance() {
        if (m_oInstance == null) {
            m_oInstance = new NetworkController();
        }
        return m_oInstance;
    }//end getInstance()

    /****Loading Methods ******************************************************/
    public static void showLoadingDialog() {
//        mainMIDlet.getCurrentPage().showLoadingDialog(false);
        if (m_cLoadingDialog == null) {
            //System.out.println("showing loading");
            m_cLoadingDialog = (Dialog)mainMIDlet.getBuilder().createContainer(mainMIDlet.getResources(), "LoadingDialog");
        }
        if(m_cLoadingDialog != null && !m_cLoadingDialog.isVisible()) {
            m_cLoadingSlider = (Slider)mainMIDlet.getBuilder().findByName("LoadingSlider", (Container)m_cLoadingDialog);
            int width = Display.getInstance().getDisplayWidth();
            int height = Display.getInstance().getDisplayHeight();
            if(height < 130) {
                // Devices with very small screens should use showmodeless instead
                m_cLoadingDialog.showModeless();
            } else {
                m_cLoadingDialog.show(height/3, height/3, width/4, width/4, false, false);
            }
            
//            md = new MemoryDisplayer(m_cLoadingDialog);
//            Display.getInstance().callSerially(md);

            Thread.yield();
            
        }
        
        //System.out.println("finished showing loading");
    }//end showLoadingDialog()
    
//    private static class MemoryDisplayer extends Thread {
//
//        Dialog dia;
//        public boolean shouldRun = false;
//        String memString;
//        Runtime runtime = Runtime.getRuntime();
//        
//        public void run() {
//            System.out.println("   ***   in MemoryDisplayer.run()");
//            shouldRun = true;
//            while(shouldRun) {
//                memString = " " + runtime.totalMemory() + " used / " + runtime.freeMemory() + " free.";
//                System.out.println("   ***   in MemoryDisplayer.run() loop : " + memString);
//                Label l = (Label)mainMIDlet.getBuilder().findByName("memlabel", dia);
//                l.setText(memString);
//                dia.repaint();
//                try {
//                    System.out.println("   ***   in MemoryDisplayer.run() try/catch");
//                    Thread.yield();
//                } catch(Exception e) {
//                    System.out.println("   ***   in MemoryDisplayer.run() CATCH!!!");
//                    e.printStackTrace();
//                }
//            }
//        }
//        
//        public MemoryDisplayer(Dialog d) {
//            dia = d;
//        }        
//    }
    
    public static void hideLoadingDialog() {
        if(m_cLoadingDialog != null && m_cLoadingDialog.isVisible()) {
            //System.out.println("disposing loading");
            m_cLoadingDialog.dispose();
            m_cLoadingDialog = null;
            m_cLoadingSlider = null;
//            md.shouldRun = false;
            Thread.yield();
        }
        //System.out.println("finished disposing loading");
    }//end hideLoadingDialog()

    /****Search ************************************************/
      
    public void performSearch(String _sLanguage, String _sSearchTerm, int _iOffset) {
        //http://en.wikipedia.org/w/api.php?action=query&list=search&format=json&srsearch=ted&srprop=hasrelated&srlimit=10
        
        StringBuffer url = new StringBuffer();
        url.append(WEBAPI +"?");
        
        //adding common items: action, props, format
        url.append("action=query");
        url.append("&");
        url.append("list=search");
        url.append("&");
        url.append("format=json");
        url.append("&");
        url.append("srprop=hasrelated");
        url.append("&");
        url.append("srlimit=");
        url.append(SEARCH_LIMIT);
        
        if(_iOffset > 0) {
            url.append("&");
            url.append("sroffset=");
            url.append(_iOffset);
        }
        
        String sKeyword = _sSearchTerm.trim();
        if (sKeyword != null && sKeyword.length() > 0) {
            url.append("&");
            url.append("srsearch=");           
            sKeyword = sKeyword.replace(' ', '_');
            // We append an underscore to the end of the keyword to ensure
            // that we get back a normalized title.
            url.append(HtmlEncode(sKeyword));
            //url.append("");
        }
        
        
        NetworkController.showLoadingDialog();
        //System.out.println("search Url: "+url.toString());
        networkNexus("http://"+_sLanguage+BASE_URL+url.toString(), "", HttpConnection.GET, PARSE_SEARCH);
        //return results;
    }//end performSearch(String url)
        
    public void fetchArticle(String _sLanguage, String _sSearchTerm, String _sSection) {
        //?action=mobileview&format=json&page=purple&sections=0&prop=text%7Csections
        /*if (_sSearchTerm != null && _sSearchTerm.length() == 0) {
            mainMIDlet.previousSearchQuery = _sSearchTerm;
        }else {
            mainMIDlet.previousSearchQuery = "";
        }*/
        StringBuffer url = new StringBuffer();
        url.append(WEBAPI +"?");
        
        //adding common items: action, props, format
        url.append("action=mobileview");
        url.append("&");
        url.append("format=json");
        url.append("&");
        url.append("prop=text%7Csections%7Cnormalizedtitle");
        url.append("&");
        url.append("sectionprop=toclevel%7Cline%7Cnumber");
        
        url.append("&devicememory=" + Runtime.getRuntime().totalMemory());
        
        if(_sSection == null || _sSection.length() == 0) {
            _sSection = "0";
        }
        if(!_sSection.equalsIgnoreCase("-1")) {
            url.append("&");
            url.append("sections=");
            url.append(_sSection);
        }
                
        String sKeyword = _sSearchTerm.trim();
        if (sKeyword != null && sKeyword.length() > 0) {
            url.append("&");
            sKeyword = sKeyword.replace(' ', '_');
            url.append("page=");           
            // We append an underscore to the end of the keyword to ensure
            // that we get back a normalized title.
            sKeyword = replaceUnicodeEscapes(sKeyword);
            
            //url.append(HtmlEncode(sKeyword) + "_");
            url.append(sKeyword + "_");
            
            //url.append("");
        }
        
        url.append("&noheadings=");
        
        if(_sLanguage == null || _sLanguage.length() <= 0) {
            _sLanguage = "en";
        }
        //performSearch(BASE_URL+url.toString());
        NetworkController.showLoadingDialog();
        //System.out.println("search Url: "+url.toString());
        networkNexus("http://"+_sLanguage+BASE_URL+url.toString(), "", HttpConnection.GET, FETCH_ARTICLE);
    }//end fetchArticle(String _sLanguage, String _sSearchTerm, String _sSection)
    
    public void fetchTermsOfUse(String _sLanguage, String _sSearchTerm, String _sSection){
        //http://wikimediafoundation.org/w/api.php?action=mobileview&format=json&page=Terms_of_use
        
        //?action=mobileview&format=json&page=purple&sections=0&prop=text%7Csections
        /*if (_sSearchTerm != null && _sSearchTerm.length() == 0) {
            mainMIDlet.previousSearchQuery = _sSearchTerm;
        }else {
            mainMIDlet.previousSearchQuery = "";
        }*/
        StringBuffer url = new StringBuffer();
        url.append(WEBAPI +"?");
        
        //adding common items: action, props, format
        url.append("action=mobileview");
        url.append("&");
        url.append("format=json");
        /*url.append("&");
        url.append("prop=text%7Csections%7Cnormalizedtitle");
        url.append("&");
        url.append("sectionprop=toclevel%7Cline%7Cnumber");*/

        
        if(_sSection == null || _sSection.length() == 0) {
            _sSection = "0";
        }
        
        if(!_sSection.equalsIgnoreCase("-1")) {
            url.append("&");
            url.append("sections=");
            url.append(_sSection);
        }
        
        String sKeyword = _sSearchTerm.trim();
        if (sKeyword != null && sKeyword.length() > 0) {
            url.append("&");
            sKeyword = sKeyword.replace(' ', '_');
            url.append("page=");           
            // We append an underscore to the end of the keyword to ensure
            // that we get back a normalized title.
            url.append(HtmlEncode(sKeyword) + "_");
            //url.append("");
        }
        
        //url.append("&noheadings=");
        //performSearch(BASE_URL+url.toString());
        NetworkController.showLoadingDialog();
        //System.out.println("search Url: "+url.toString());
        networkNexus("http://wikimediafoundation.org"+url.toString(), "", HttpConnection.GET, FETCH_TERMS);
    }
        
    public void searchArticleLanguages(String _sLanguage, String _sSearchTerm, String _sOffset) {
        //?action=mobileview&format=json&page=purple&sections=0&prop=text%7Csections
        /*if (_sSearchTerm != null && _sSearchTerm.length() == 0) {
            mainMIDlet.previousSearchQuery = _sSearchTerm;
        }else {
            mainMIDlet.previousSearchQuery = "";
        }*/
        StringBuffer url = new StringBuffer();
        url.append(WEBAPI +"?");
        
        //adding common items: action, props, format
        url.append("action=query");
        url.append("&");
        url.append("prop=langlinks");
        url.append("&");
        url.append("format=json");
        url.append("&");
        url.append("lllimit=");
        url.append(SEARCH_LIMIT);

        
        if(_sOffset != null && _sOffset.length() > 0) {
            
            url.append("&");
            url.append("llcontinue=");
            url.append(_sOffset);
        }        
        
        String sKeyword = _sSearchTerm.trim();
        if (sKeyword != null && sKeyword.length() > 0) {
            url.append("&");
            sKeyword = sKeyword.replace(' ', '_');
            url.append("titles=");           
            // We append an underscore to the end of the keyword to ensure
            // that we get back a normalized title.
            //url.append(HtmlEncode(sKeyword));
            url.append(replaceUnicodeEscapes(sKeyword));
            //url.append("");
        }
        
        if(_sLanguage == null || _sLanguage.length() <= 0) {
            _sLanguage = "en";
        }
        //performSearch(BASE_URL+url.toString());
        NetworkController.showLoadingDialog();
        //System.out.println("search Url: "+url.toString());
        networkNexus("http://"+_sLanguage+BASE_URL+url.toString(), "", HttpConnection.GET, SEARCH_LANGUAGES);
    }//end searchArticleLanguages(String _sLanguage, String _sSearchTerm, String _sOffset)
        
    public void searchLanguages(String _sLanguage, String _sOffset) {
        //?action=mobileview&format=json&page=purple&sections=0&prop=text%7Csections
        /*if (_sSearchTerm != null && _sSearchTerm.length() == 0) {
            mainMIDlet.previousSearchQuery = _sSearchTerm;
        }else {
            mainMIDlet.previousSearchQuery = "";
        }*/
        StringBuffer url = new StringBuffer();
        url.append(WEBAPI +"?");
        
        //adding common items: action, props, format
        url.append("action=sitematrix");
        url.append("&");
        url.append("format=json");
        url.append("&");
        url.append("smlimit=");
        url.append(SEARCH_LIMIT);

        
        if(_sOffset != null && _sOffset.length() > 0) {
            
            url.append("&");
            url.append("smcontinue=");
            url.append(_sOffset);
        }        
                
        if(_sLanguage == null || _sLanguage.length() <= 0) {
            _sLanguage = "en";
        }
        //performSearch(BASE_URL+url.toString());
        NetworkController.showLoadingDialog();
        //System.out.println("search Url: "+url.toString());
        networkNexus("http://www."+"mediawiki.org"+url.toString(), "", HttpConnection.GET, SEARCH_LANGUAGES);
    }//end searchLanguages(String _sLanguage, String _sOffset)
    
    /****Network Methods ******************************************************/
    
    public void networkNexus(String _sURL, String _sPostData, String _sMethod, int _iParseType) {
        //System.out.println("!@#$% start Mem1: "+Runtime.getRuntime().freeMemory());
        m_tNetThread = new NetworkThread(_sURL, _sPostData, _sMethod, _iParseType);
        Display.getInstance().callSerially(m_tNetThread);            
        //System.out.println("out: "+response);        
    }//end networkNexus(String uri, String postData, String method, int parseType)
    
    public static void cancelNetwork() {
        hideLoadingDialog();
        if(m_tNetThread != null && !m_tNetThread.isNetworkDone()) {
            m_tNetThread.setNetworkDone();
        }
        m_tNetThread = null;
    }//end cancelNetwork()

    
    public static String HtmlEncode(String _sInput) {
         return com.sun.lwuit.html.HTMLUtils.encodeString(_sInput);
    }//end HtmlEncode(String input)
    

    public static String htmlReplace(String _sNeedle, String _sHaystack) {
        int index = _sHaystack.indexOf(_sNeedle);
        if(index >= 0) {
            String replacement = HtmlEncode(_sNeedle);        
            return Utilities.replace(_sNeedle, replacement, _sHaystack);
        }
        return _sHaystack;
    }//end htmlReplace(String needle, String haystack)

    private String replaceUnicodeEscapes(String sKeyword) {
        int indOfEscapedChar = sKeyword.indexOf("\\u");
        if (indOfEscapedChar > -1) {
            String toParse = "#x" + sKeyword.substring(indOfEscapedChar + 2, indOfEscapedChar + 6);
            String toReplaceWith = com.sun.lwuit.html.HTMLUtils.convertHTMLCharEntity(toParse);
            sKeyword = sKeyword.substring(0, indOfEscapedChar) + toReplaceWith + sKeyword.substring(indOfEscapedChar + 6, sKeyword.length());
        }
        
        if (indOfEscapedChar > -1) {
            sKeyword = replaceUnicodeEscapes(sKeyword);
        }
        return sKeyword;
    }
}
