/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.util.Vector;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordFilter;
/**
 *
 * @author caxthelm
 */
public class DeviceStorage {
    Vector m_vSavedVec = null;
    String m_sStoreName = "";
    
    //Record Filter
    class Filter implements RecordFilter {
        private String search = null;

        public Filter(String search) {
            this.search = search.toLowerCase();
        }//end Filter(String search)

        public boolean matches(byte[] suspect) {
            String string = new String(suspect).toLowerCase();
            if (string != null && string.indexOf(search) != -1) {
                return true;
            }else {
                return false;
            }
        }        
    }
    
    public DeviceStorage(String _sStoreName) {
        m_sStoreName = _sStoreName;        
        //loadRecords(true);
    }
    
    public boolean recordExists(String _sBookmark) {
        if(_sBookmark == null || _sBookmark.length() <= 0) {
            return false;
        }
        if(m_vSavedVec == null) {
            //if we have a null vector then we have to load the bookmarks before 
            //adding the new bookmark to the vector them.
            loadRecords(true);
        }
        if(!m_vSavedVec.contains(_sBookmark)) {
            return false;
        }
        m_vSavedVec.removeElement(_sBookmark);
        RecordStore recordStore = null;
        try {
            recordStore = RecordStore.openRecordStore(m_sStoreName, true);
            Filter filter = new Filter(_sBookmark);
            RecordEnumeration recordEnumeration = recordStore.enumerateRecords(filter, null, false);
            if (recordEnumeration.numRecords() > 0) {
                return true;
            }
        }catch(Exception e) {
            //System.out.println("failed saving watchlist");
            e.printStackTrace();            
        }finally {
            if(recordStore != null) {
                try {
                    recordStore.closeRecordStore();
                }catch(Exception er) {
                }
            }
        }
        return false;
    }//end deleteBookmark(String _sBookmark)
    
    public Vector loadRecords() {
        return loadRecords(false);
    }//end loadRecords()
    
    public Vector loadRecords(boolean _bForceLoad) {
        if(m_vSavedVec == null || _bForceLoad) {
            m_vSavedVec = new Vector();
            RecordStore recordStore = null;
            try {
                //RecordStore.deleteRecordStore(recordStoreWatchList);
                recordStore = RecordStore.openRecordStore(m_sStoreName, true);            
                //System.out.println("loading watch: "+recordStore.getNumRecords());                
                RecordEnumeration recordEnum = recordStore.enumerateRecords(null, null, false);
                while(recordEnum.hasNextElement()) {
                    String temp = new String(recordEnum.nextRecord());
                    m_vSavedVec.addElement(temp); 
                }
            }catch(Exception e) {
                //System.out.println("failed saving watchlist");
                e.printStackTrace();            
            }finally {
                if(recordStore != null) {
                    try {
                        recordStore.closeRecordStore();
                    }catch(Exception er) {
                    }
                }
            }
        
        }//end if(m_vSavedBookmarks == null)
        return m_vSavedVec;
    }//end loadRecords(boolean _bForceLoad)
    
    public void saveRecord(String _sBookmark) {
        if(_sBookmark == null || _sBookmark.length() <= 0) {
            return;
        }
        if(m_vSavedVec == null) {
            //if we have a null vector then we have to load the bookmarks before 
            //adding the new bookmark to the vector them.
            loadRecords(true);
        }
        m_vSavedVec.addElement(_sBookmark);
        RecordStore recordStore = null;
        try {
            recordStore = RecordStore.openRecordStore(m_sStoreName, true);
            recordStore.addRecord(_sBookmark.getBytes(), 0, _sBookmark.getBytes().length);                      
            recordStore.closeRecordStore();
        }catch(Exception e) {
            //System.out.println("failed saving watchlist");
            e.printStackTrace();            
        }finally {
            if(recordStore != null) {
                try {
                    recordStore.closeRecordStore();
                }catch(Exception er) {
                }
            }
        }
    }//end saveBookmarks()
    
    public void deleteRecord(String _sBookmark) {
        if(_sBookmark == null || _sBookmark.length() <= 0) {
            return;
        }
        if(m_vSavedVec == null) {
            //if we have a null vector then we have to load the bookmarks before 
            //adding the new bookmark to the vector them.
            loadRecords(true);
        }
        if(!m_vSavedVec.contains(_sBookmark)) {
            return;
        }
        m_vSavedVec.removeElement(_sBookmark);
        RecordStore recordStore = null;
        try {
            recordStore = RecordStore.openRecordStore(m_sStoreName, true);
            Filter filter = new Filter(_sBookmark);
            RecordEnumeration recordEnumeration = recordStore.enumerateRecords(filter, null, false);
            while (recordEnumeration.hasNextElement()) {
                recordStore.deleteRecord(recordEnumeration.nextRecordId()); 
            }
            recordStore.closeRecordStore();
        }catch(Exception e) {
            //System.out.println("failed saving watchlist");
            e.printStackTrace();            
        }finally {
            if(recordStore != null) {
                try {
                    recordStore.closeRecordStore();
                }catch(Exception er) {
                }
            }
        }//end finally
    }//end deleteBookmark(String _sBookmark)
}
