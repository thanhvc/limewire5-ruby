package org.limewire.core.impl;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.limewire.core.api.callback.GuiCallback;
import org.limewire.core.api.callback.GuiCallbackService;
import org.limewire.core.api.download.DownloadAction;
import org.limewire.core.api.download.SaveLocationException;
import org.limewire.core.impl.download.DownloadListener;
import org.limewire.core.impl.download.DownloadListenerList;
import org.limewire.core.impl.magnet.MagnetLinkImpl;
import org.limewire.core.impl.monitor.IncomingSearchListener;
import org.limewire.core.impl.monitor.IncomingSearchListenerList;
import org.limewire.core.impl.search.QueryReplyListener;
import org.limewire.core.impl.search.QueryReplyListenerList;
import org.limewire.core.impl.upload.UploadListener;
import org.limewire.core.impl.upload.UploadListenerList;
import org.limewire.i18n.I18nMarker;
import org.limewire.io.GUID;
import org.limewire.io.IpPort;
import org.limewire.service.ErrorService;
import org.limewire.service.MessageService;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.limegroup.bittorrent.ManagedTorrent;
import com.limegroup.bittorrent.TorrentEvent;
import com.limegroup.bittorrent.TorrentManager;
import com.limegroup.gnutella.ActivityCallback;
import com.limegroup.gnutella.DownloadManager;
import com.limegroup.gnutella.Downloader;
import com.limegroup.gnutella.RemoteFileDesc;
import com.limegroup.gnutella.Uploader;
import com.limegroup.gnutella.browser.MagnetOptions;
import com.limegroup.gnutella.messages.QueryReply;

/**
 * An implementation of the UI callback to handle notifications about 
 * asynchronous backend events.
 */
@Singleton
class GlueActivityCallback implements ActivityCallback, QueryReplyListenerList,
        DownloadListenerList, UploadListenerList, 
        IncomingSearchListenerList, GuiCallbackService {

    private final SortedMap<byte[], List<QueryReplyListener>> queryReplyListeners;

    private final List<DownloadListener> downloadListeners = new CopyOnWriteArrayList<DownloadListener>();
    
    private final List<UploadListener> uploadListeners = new CopyOnWriteArrayList<UploadListener>();

    private final List<IncomingSearchListener> monitorListeners = new CopyOnWriteArrayList<IncomingSearchListener>();

    private final DownloadManager downloadManager;
    
    private final TorrentManager torrentManager;
    
    private GuiCallback guiCallback = null;
    
    
    
    @Inject
    public GlueActivityCallback(DownloadManager downloadManager, TorrentManager torrentManager) {
        this.downloadManager = downloadManager;
        this.torrentManager = torrentManager;
        queryReplyListeners = new ConcurrentSkipListMap<byte[], List<QueryReplyListener>>(
                GUID.GUID_BYTE_COMPARATOR);
    }

    @Override
    public void addQueryReplyListener(byte[] guid, QueryReplyListener listener) {
        synchronized (queryReplyListeners) {
            List<QueryReplyListener> listeners = queryReplyListeners.get(guid);
            if (listeners == null) {
                listeners = new CopyOnWriteArrayList<QueryReplyListener>();
                queryReplyListeners.put(guid, listeners);
            }
            listeners.add(listener);
        }
    }

    @Override
    public void removeQueryReplyListener(byte[] guid, QueryReplyListener listener) {
        synchronized (queryReplyListeners) {
            List<QueryReplyListener> listeners = queryReplyListeners.get(guid);
            if (listeners != null) {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    queryReplyListeners.remove(guid);
                }
            }
        }
    }

    @Override
    public void addDownloadListener(DownloadListener listener) {
        downloadListeners.add(listener);
    }

    @Override
    public void removeDownloadListener(DownloadListener listener) {
        downloadListeners.remove(listener);
    }

    @Override
    public void handleMagnets(MagnetOptions[] magnets) {
        if(guiCallback != null) {
            for(MagnetOptions magnetOption : magnets) {
                guiCallback.handleMagnet(new MagnetLinkImpl(magnetOption));
            }
        }
    }

    @Override
    public void handleQueryResult(RemoteFileDesc rfd, QueryReply queryReply,
            Set<? extends IpPort> locs) {
        List<QueryReplyListener> listeners = queryReplyListeners.get(queryReply.getGUID());
        if (listeners != null) {
            for (QueryReplyListener listener : listeners) {
                listener.handleQueryReply(rfd, queryReply, locs);
            }
        }
    }

    @Override
    public void handleQueryString(String query) {
        for (IncomingSearchListener listener : monitorListeners) {
            listener.handleQueryString(query);
        }
    }

    public void handleSharedFileUpdate(File file) {
        //TODO PJV this looks like it is supposed to update the gui with the meta data changes about how many hits this file received in searches and how many times it has been uploaded.
        //Can we get rid of this now and use a property listener when we need this data again? could fireProperty events when incrementHitcount etc. are called
    }

    @Override
    public void handleTorrent(File torrentFile) {
        if(torrentFile != null && torrentFile.exists() && torrentFile.length() > 0) {
            try {
                downloadManager.downloadTorrent(torrentFile, false);
            } catch (SaveLocationException e) {
                handleSaveLocationException(new DownloadAction() {
                  @Override
                    public void download(File saveFile, boolean overwrite) throws SaveLocationException {
                          downloadManager.downloadTorrent(saveFile, overwrite);
                    }  
                },e,false);
            }
        }
    }

    @Override
    public void installationCorrupted() {
        MessageService.showError(I18nMarker.marktr("<html><b>Your LimeWire may have been corrupted by a virus or trojan!</b><br><br>Please visit <a href=\"http://www.limewire.com/corrupted\">www.limewire.com</a> and download the newest official version of LimeWire.</html>"));
    }

    @Override
    public boolean isQueryAlive(GUID guid) {
        return queryReplyListeners.containsKey(guid.bytes());
    }

    @Override
    public void addUpload(Uploader u) {
        for (UploadListener listener : uploadListeners) {
            listener.uploadAdded(u);
        }
    }
    
    @Override
    public void removeUpload(Uploader u) {
        for (UploadListener listener : uploadListeners) {
            listener.uploadRemoved(u);
        }
    }

    @Override
    public void restoreApplication() {
        if(guiCallback != null) {
            guiCallback.restoreApplication();
        }
    }

    @Override
    public String translate(String s) {
        if(guiCallback != null) {
            return guiCallback.translate(s);
        }
        
        return s;
    }

    @Override
    public void uploadsComplete() {
        for (UploadListener listener : uploadListeners) {
            listener.uploadsCompleted();
        }
    }

    @Override
    public void addDownload(Downloader d) {
        for (DownloadListener listener : downloadListeners) {
            listener.downloadAdded(d);
        }
    }
    
    @Override
    public void downloadsComplete() {
        for (DownloadListener listener : downloadListeners) {
            listener.downloadsCompleted();
        }
    }
    
    @Override
    public void promptAboutCorruptDownload(Downloader dloader) {
        //just kill the download if it is corrupt
        dloader.discardCorruptDownload(true);

    }
    
    @Override
    public void downloadCompleted(Downloader d) {
        for (DownloadListener listener : downloadListeners) {
            listener.downloadRemoved(d);
        }
    }
    
    @Override
    public void showDownloads() {
        if(guiCallback != null) {
            guiCallback.showDownloads();
        }
    }
    
    public void setGuiCallback(GuiCallback guiCallback) {
        this.guiCallback = guiCallback;
    }

    @Override
    public void handleSaveLocationException(DownloadAction downLoadAction,
            SaveLocationException sle, boolean supportsNewSaveDir) {
        if(guiCallback != null) {
            guiCallback.handleSaveLocationException(downLoadAction, sle, supportsNewSaveDir);
        } else {
            ErrorService.error(sle, "Error handling SaveLocationException. GuiCallBack not yet initialized.");
        }
    }

    @Override
    public void addUploadListener(UploadListener listener) {
        uploadListeners.add(listener);
    }

    @Override
    public void removeUploadListener(UploadListener listener) {
        uploadListeners.remove(listener);
    }

    @Override
    public void addIncomingSearchListener(IncomingSearchListener listener) {
        monitorListeners.add(listener);
    }

    @Override
    public void removeIncomingSearchListener(IncomingSearchListener listener) {
        monitorListeners.remove(listener);
    }

    @Override
    public void promptTorrentUploadCancel(ManagedTorrent torrent) {
        boolean approve = true;//default to true
        if(guiCallback != null) {
            if (!torrent.isActive()) {
                return;
            }
            
            if(!torrent.isComplete()) {
                approve = guiCallback.promptUserQuestion(I18nMarker.marktr("If you stop this upload, the torrent download will stop. Are you sure you want to do this?"));
            } else if (torrent.getRatio() < 1.0f) {
                approve = guiCallback.promptUserQuestion(I18nMarker.marktr("This upload is a torrent and it hasn\'t seeded enough. You should let it upload some more. Are you sure you want to stop it?"));
            }
        } 
        if(approve && torrent.isActive()) {
            torrentManager
                    .dispatchEvent(new TorrentEvent(this, TorrentEvent.Type.STOP_APPROVED, torrent));
        }
    }

}
