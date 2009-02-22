package com.limegroup.bittorrent;

import java.io.File;
import java.net.Socket;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;
import junit.framework.Test;

import org.limewire.core.api.download.DownloadAction;
import org.limewire.core.api.download.SaveLocationException;
import org.limewire.core.settings.ConnectionSettings;
import org.limewire.http.httpclient.LimeHttpClient;
import org.limewire.http.httpclient.SimpleLimeHttpClient;
import org.limewire.io.Address;
import org.limewire.io.GUID;
import org.limewire.io.IpPort;
import org.limewire.listener.EventListener;
import org.limewire.net.ConnectionDispatcher;
import org.limewire.util.TestUtils;

import com.google.inject.Provider;
import com.limegroup.gnutella.ActivityCallback;
import com.limegroup.gnutella.DownloadManager;
import com.limegroup.gnutella.DownloadManagerEvent;
import com.limegroup.gnutella.Downloader;
import com.limegroup.gnutella.NoOpSaveLocationManager;
import com.limegroup.gnutella.RemoteFileDesc;
import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.Uploader;
import com.limegroup.gnutella.Downloader.DownloadState;
import com.limegroup.gnutella.browser.MagnetOptions;
import com.limegroup.gnutella.downloader.CantResumeException;
import com.limegroup.gnutella.downloader.CoreDownloader;
import com.limegroup.gnutella.downloader.DownloadStateEvent;
import com.limegroup.gnutella.downloader.IncompleteFileManager;
import com.limegroup.gnutella.downloader.Visitor;
import com.limegroup.gnutella.http.DefaultHttpExecutor;
import com.limegroup.gnutella.messages.QueryReply;
import com.limegroup.gnutella.messages.QueryRequest;
import com.limegroup.gnutella.util.LimeTestCase;
import com.limegroup.gnutella.version.DownloadInformation;
import com.limegroup.mozilla.MozillaDownload;

public class BTTorrentFileDownloaderImplTest extends LimeTestCase {
    private static final int TEST_PORT = 8080;

    /**
     * A directory containing the torrent data for this unit test.
     */
    public static final File TORRENT_DIR = TestUtils
            .getResourceFile("org/limewire/swarm/bittorrent/public_html/torrents");

    /**
     * A directory containing the download data for this unit test.
     */
    public static final File FILE_DIR = TestUtils
            .getResourceFile("org/limewire/swarm/bittorrent/public_html");

    private boolean localIsPrivateBackup = false;

    private boolean forceIPAddressBackup = false;

    private String forceIPAddressStringBackup = null;

    private FileServer fileServer = null;

    public BTTorrentFileDownloaderImplTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        return buildTestSuite(BTTorrentFileDownloaderImplTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        localIsPrivateBackup = ConnectionSettings.LOCAL_IS_PRIVATE.getValue();
        ConnectionSettings.LOCAL_IS_PRIVATE.setValue(false);
        forceIPAddressBackup = ConnectionSettings.FORCE_IP_ADDRESS.getValue();
        ConnectionSettings.FORCE_IP_ADDRESS.setValue(true);
        forceIPAddressStringBackup = ConnectionSettings.FORCED_IP_ADDRESS_STRING.getValue();
        ConnectionSettings.FORCED_IP_ADDRESS_STRING.setValue("127.0.0.1");
        fileServer = new FileServer(TEST_PORT, FILE_DIR);
        fileServer.start();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        ConnectionSettings.LOCAL_IS_PRIVATE.setValue(localIsPrivateBackup);
        ConnectionSettings.FORCE_IP_ADDRESS.setValue(forceIPAddressBackup);
        ConnectionSettings.FORCED_IP_ADDRESS_STRING.setValue(forceIPAddressStringBackup);
        fileServer.stop();
        fileServer.destroy();
        super.tearDown();
    }
    
    public void testTorrentDownload() throws Exception {
        URI uri = new URI("http://localhost:8080/torrents/test-peer-dl-single-file.torrent");
        BTTorrentFileDownloader btTorrentFileDownloader = createDownloader(uri);
        btTorrentFileDownloader.startDownload();
        finishDownload(btTorrentFileDownloader);
        
        BTMetaInfo btMetaInfo = btTorrentFileDownloader.getBtMetaInfo();
        
        Assert.assertNotNull(btMetaInfo);

    }

    private void finishDownload(BTTorrentFileDownloader btTorrentFileDownloader) throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        btTorrentFileDownloader.addListener(new EventListener<DownloadStateEvent>() {

            @Override
            public void handleEvent(DownloadStateEvent event) {
                if(DownloadState.COMPLETE == event.getType()) {
                    countDownLatch.countDown();
                }        
            }
        });
        countDownLatch.await(5, TimeUnit.SECONDS);
    }

    private BTTorrentFileDownloader createDownloader(URI uri) {
        BTTorrentFileDownloader torrentFileDownloaderImpl = new BTTorrentFileDownloaderImpl(new DownloadManager() {

            @Override
            public boolean acceptPushedSocket(String file, int index, byte[] clientGUID,
                    Socket socket) {
                return false;
            }

            @Override
            public void addNewDownloader(CoreDownloader downloader) {
            }

            @Override
            public void bumpPriority(Downloader downl, boolean up, int amt) {
            }

            @Override
            public boolean conflicts(URN urn, long fileSize, File... fileName) {
                return false;
            }

            @Override
            public Downloader download(RemoteFileDesc[] files, List<? extends RemoteFileDesc> alts,
                    GUID queryGUID, boolean overwrite, File saveDir, String fileName)
                    throws SaveLocationException {
                return null;
            }

            @Override
            public Downloader download(MagnetOptions magnet, boolean overwrite, File saveDir,
                    String fileName) throws IllegalArgumentException, SaveLocationException {
                return null;
            }

            @Override
            public Downloader download(File incompleteFile) throws CantResumeException,
                    SaveLocationException {
                return null;
            }

            @Override
            public Downloader download(DownloadInformation info, long now)
                    throws SaveLocationException {
                return null;
            }

            @Override
            public Downloader downloadFromMozilla(MozillaDownload listener) {
                return null;
            }

            @Override
            public Downloader downloadFromStore(RemoteFileDesc rfd, boolean overwrite,
                    File saveDir, String fileName) throws IllegalArgumentException,
                    SaveLocationException {
                return null;
            }

            @Override
            public Downloader downloadTorrent(URI torrentURI, boolean overwrite)
                    throws SaveLocationException {
                return null;
            }

            @Override
            public Downloader downloadTorrent(File torrentFile, boolean overwrite)
                    throws SaveLocationException {
                return null;
            }

            @Override
            public Downloader downloadTorrent(BTMetaInfo info, boolean overwrite)
                    throws SaveLocationException {
                return null;
            }

            @Override
            public int downloadsInProgress() {
                return 0;
            }

            @Override
            public Iterable<CoreDownloader> getAllDownloaders() {
                return null;
            }

            @Override
            public float getAverageBandwidth() {
                return 0;
            }

            @Override
            public Downloader getDownloaderForIncompleteFile(File file) {
                return null;
            }

            @Override
            public Downloader getDownloaderForURN(URN sha1) {
                return null;
            }

            @Override
            public IncompleteFileManager getIncompleteFileManager() {
                return null;
            }

            @Override
            public float getLastMeasuredBandwidth() {
                return 0;
            }

            @Override
            public float getMeasuredBandwidth() {
                return 0;
            }

            @Override
            public int getNumActiveDownloads() {
                return 0;
            }

            @Override
            public int getNumIndividualDownloaders() {
                return 0;
            }

            @Override
            public int getNumWaitingDownloads() {
                return 0;
            }

            @Override
            public boolean hasInNetworkDownload() {
                return false;
            }

            @Override
            public boolean isActivelyDownloading(URN urn) {
                return false;
            }

            @Override
            public boolean isGuidForQueryDownloading(GUID guid) {
                return false;
            }

            @Override
            public boolean isIncomplete(URN urn) {
                return false;
            }

            @Override
            public boolean isSaveLocationTaken(File candidateFile) {
                return false;
            }

            @Override
            public boolean isSavedDownloadsLoaded() {
                return false;
            }

            @Override
            public void killDownloadersNotListed(Collection<? extends DownloadInformation> updates) {
                
            }

            @Override
            public void loadSavedDownloadsAndScheduleWriting() {
                
            }

            @Override
            public void measureBandwidth() {
                
            }

            @Override
            public void remove(CoreDownloader downloader, boolean completed) {
                
            }

            @Override
            public void sendQuery(QueryRequest query) {
                
            }

            @Override
            public void start() {
                
            }

            @Override
            public void writeSnapshot() {
                
            }

            @Override
            public void visitDownloads(Visitor<CoreDownloader> d) {
                
            }

            @Override
            public void addListener(EventListener<DownloadManagerEvent> listener) {
                
            }

            @Override
            public boolean removeListener(EventListener<DownloadManagerEvent> listener) {
                return false;
            }

            @Override
            public void handleQueryReply(QueryReply qr, Address address) {
            }

            @Override
            public boolean contains(Downloader downloader) {
                return false;
            }

        },new NoOpSaveLocationManager(), new DefaultHttpExecutor(new Provider<LimeHttpClient>() {
           @Override
            public LimeHttpClient get() {
                return new SimpleLimeHttpClient();
            } 
        }), new TorrentManager() {

            @Override
            public boolean allowNewTorrent() {
                return false;
            }

            @Override
            public int getMaxTorrentConnections() {
                return 0;
            }

            @Override
            public int getNumActiveTorrents() {
                return 0;
            }

            @Override
            public File getSharedTorrentMetaDataFile(BTMetaInfo info) {
                return null;
            }

            @Override
            public ManagedTorrent getTorrentForHash(byte[] infoHash) {
                return null;
            }

            @Override
            public ManagedTorrent getTorrentForURN(URN urn) {
                return null;
            }

            @Override
            public boolean hasNonSeeding() {
                return false;
            }

            @Override
            public void initialize(ConnectionDispatcher dispatcher) {
                
            }

            @Override
            public boolean killTorrentForFile(File f) {
                return false;
            }

            @Override
            public void shareTorrentFile(BTMetaInfo m, byte[] body) {
                
            }

            @Override
            public boolean releaseLock(File file) {
                return false;
            }

            @Override
            public void acceptConnection(String word, Socket s) {
                
            }

            @Override
            public boolean isBlocking() {
                return false;
            }

            @Override
            public void handleTorrentEvent(TorrentEvent evt) {
                
            }

            @Override
            public void addEventListener(TorrentEventListener listener) {
                
            }

            @Override
            public void dispatchEvent(TorrentEvent event) {
                
            }

            @Override
            public void removeEventListener(TorrentEventListener listener) {
                
            }
            
        },new BTMetaInfoFactoryImpl(), new ActivityCallback() {

            @Override
            public void addUpload(Uploader u) {
                
            }

            @Override
            public void handleMagnets(MagnetOptions[] magnets) {
            }

            @Override
            public void handleQueryResult(RemoteFileDesc rfd, QueryReply queryReply,
                    Set<? extends IpPort> locs) {
                
            }

            @Override
            public void handleQuery(QueryRequest query, String address, int port) {
                
            }

            public void handleSharedFileUpdate(File file) {
                
            }

            @Override
            public void handleTorrent(File torrentFile) {
                
            }

            @Override
            public void installationCorrupted() {
                
            }

            @Override
            public boolean isQueryAlive(GUID guid) {
                return false;
            }

            @Override
            public void removeUpload(Uploader u) {
            }

            @Override
            public void restoreApplication() {
            }

            @Override
            public String translate(String s) {
                return null;
            }

            @Override
            public void uploadsComplete() {
            }

            @Override
            public void addDownload(Downloader d) {
            }

            @Override
            public void downloadsComplete() {
            }


            @Override
            public void promptAboutCorruptDownload(Downloader dloader) {
            }

            @Override
            public void downloadCompleted(Downloader d) {
            }

            @Override
            public void handleSaveLocationException(DownloadAction downLoadAction,
                    SaveLocationException sle, boolean supportsNewSaveDir) {
                
            }

            @Override
            public void promptTorrentUploadCancel(ManagedTorrent torrent) {
                
            }

        });
        
        torrentFileDownloaderImpl.initDownloadInformation(uri, true);
        return torrentFileDownloaderImpl;
    }
}
