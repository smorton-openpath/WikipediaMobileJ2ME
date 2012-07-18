/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pages;

import com.sun.lwuit.*;
import com.sun.lwuit.io.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.io.services.ImageDownloadService;

import com.mainMIDlet;
/**
 *
 * @author caxthelm
 */
public class ImageDialog extends BasePage
{
    //Common Command Ids ;
    private final int COMMAND_BACK = COMMAND_RIGHT;
    
    //Lwuit Commands:
    String m_sURL = null;
    
    public ImageDialog(String _sTitle, String _sURL) {
        super("ImageDialog", DIALOG_IMAGE);
        try {
            m_sURL = _sURL;
            //System.out.println("List Size: "+_mainItem.childCount());
            if(!m_bIsLoaded) {
                //TODO: make error dialog.
                System.err.println("We failed to load");
                return;
            }
            m_cDialog.setTitle(_sTitle);
            
            if(_sURL != null && _sURL.length() > 0 && _sURL.indexOf("http://") > -1) {
                Label newLabel = new Label();
                newLabel.setUIID("no_MarginsTransparent");
                ImageDownloadService img = new ImageDownloadService(_sURL, newLabel);
                NetworkManager.getInstance().addToQueue(img);
                m_cDialog.addComponent(BorderLayout.CENTER, newLabel);
            }
            String  str = mainMIDlet.getString("BackSK");
            //mForm.addCommand(new Command(str, Command_Back), Command_Back);
            m_cDialog.addCommand(new Command(str, COMMAND_BACK));
            
            m_cDialog.addCommandListener(this);
            new Thread(new Runnable()  {
                public void run()  {
                    m_cDialog.show(10, 10, 10, 10, false);
                }
            }).start();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }//end ImageDialog(String _sTitle, String _sURL)
    
    public void actionPerformed(ActionEvent ae) {
        System.err.println("Action imageDialog: " + ae.getKeyEvent());
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
                if(test != null && testCont.getLeadComponent() instanceof Button)
                    commandId = test.getCommand().getId();
            }
        }
        switch(commandId) {
            case COMMAND_BACK:
                break;
        }
        mainMIDlet.dialogBack();
    }//end actionPerformed(ActionEvent ae)
    
    
    public void addData(Object _results, int _iResultType) {
    }//end addData()
}
