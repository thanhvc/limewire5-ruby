package org.limewire.core.impl.support;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.limewire.core.api.support.SessionInfo;
import org.limewire.util.BaseTestCase;

public class LocalClientInfoImplTest extends BaseTestCase {

    public LocalClientInfoImplTest(String name) {
        super(name);
    }

    public void testBasics() {

        final boolean canReceiveSolicited = true;
        final boolean acceptedIncomingConnection = true;
        final boolean canDoFWT = false;
        final int allFriendsFileListSize = 1;
        final long byteBufferCacheSize = 2L;
        final long contentResponsesSize = 3L;
        final long creationCacheSize = 4L;
        final long currentUptime = 5L;
        final long diskControllerByteCacheSize = 6L;
        final int diskControllerQueueSize = 7;
        final long diskControllerVerifyingCacheSize = 8L;
        final int managedFileListSize = 9;
        final int numPendingTimeouts = 12;
        final int numActiveDownloads = 10;
        final int numActiveUploads = 11;
        final int numWaitingSockets = 13;
        final int numOldConnections = 17;
        final int port = 22;
        final int numUltrapeertoUltraPeerConnections = 20;
        final int numQueuedUploads = 18;
        final int numWaitingDownloads = 21;
        final int numConnectionCheckerWorkArounds = 14;
        final long[] selectStats = new long[] { 23L, 28L, 29L };
        final int numUltrapeerToLeafeConnections = 19;
        final int simppVersion = 25;
        final int numIndividualDownloaders = 15;
        final int numLeafUltrapeerConnections = 16;
        final int sharedFileListSize = 24;
        final boolean isConnected = true;
        final boolean isGuessCapable = false;
        final boolean isLifecycleLoaded = true;
        final boolean isShieldedLeaf = false;
        final boolean isSuprtNode = true;
        final boolean isUdpPortStable = false;
        final String uploadSlotManagerInfo = "UploadSlotManagerInfo";


        Mockery context = new Mockery();
        final SessionInfo sessionInfo = context.mock(SessionInfo.class);
        context.checking(new Expectations() {
            {
                allowing(sessionInfo).acceptedIncomingConnection();
                will(returnValue(acceptedIncomingConnection));
                allowing(sessionInfo).canDoFWT();
                will(returnValue(canDoFWT));
                allowing(sessionInfo).canReceiveSolicited();
                will(returnValue(canReceiveSolicited));
                allowing(sessionInfo).getAllFriendsFileListSize();
                will(returnValue(allFriendsFileListSize));
                allowing(sessionInfo).getByteBufferCacheSize();
                will(returnValue(byteBufferCacheSize));
                allowing(sessionInfo).getContentResponsesSize();
                will(returnValue(contentResponsesSize));
                allowing(sessionInfo).getCreationCacheSize();
                will(returnValue(creationCacheSize));
                allowing(sessionInfo).getCurrentUptime();
                will(returnValue(currentUptime));
                allowing(sessionInfo).getDiskControllerByteCacheSize();
                will(returnValue(diskControllerByteCacheSize));
                allowing(sessionInfo).getDiskControllerQueueSize();
                will(returnValue(diskControllerQueueSize));
                allowing(sessionInfo).getDiskControllerVerifyingCacheSize();
                will(returnValue(diskControllerVerifyingCacheSize));
                allowing(sessionInfo).getManagedFileListSize();
                will(returnValue(managedFileListSize));
                allowing(sessionInfo).getNumActiveDownloads();
                will(returnValue(numActiveDownloads));
                allowing(sessionInfo).getNumActiveUploads();
                will(returnValue(numActiveUploads));
                allowing(sessionInfo).getNumberOfPendingTimeouts();
                will(returnValue(numPendingTimeouts));
                allowing(sessionInfo).getNumberOfWaitingSockets();
                will(returnValue(numWaitingSockets));
                allowing(sessionInfo).getNumConnectionCheckerWorkarounds();
                will(returnValue(numConnectionCheckerWorkArounds));
                allowing(sessionInfo).getNumIndividualDownloaders();
                will(returnValue(numIndividualDownloaders));
                allowing(sessionInfo).getNumLeafToUltrapeerConnections();
                will(returnValue(numLeafUltrapeerConnections));
                allowing(sessionInfo).getNumOldConnections();
                will(returnValue(numOldConnections));
                allowing(sessionInfo).getNumQueuedUploads();
                will(returnValue(numQueuedUploads));
                allowing(sessionInfo).getNumUltrapeerToLeafConnections();
                will(returnValue(numUltrapeerToLeafeConnections));
                allowing(sessionInfo).getNumUltrapeerToUltrapeerConnections();
                will(returnValue(numUltrapeertoUltraPeerConnections));
                allowing(sessionInfo).getNumWaitingDownloads();
                will(returnValue(numWaitingDownloads));
                allowing(sessionInfo).getPort();
                will(returnValue(port));
                allowing(sessionInfo).getSelectStats();
                will(returnValue(selectStats));
                allowing(sessionInfo).getSharedFileListSize();
                will(returnValue(sharedFileListSize));
                allowing(sessionInfo).getSimppVersion();
                will(returnValue(simppVersion));
                allowing(sessionInfo).getUploadSlotManagerInfo();
                will(returnValue(uploadSlotManagerInfo));
                allowing(sessionInfo).isConnected();
                will(returnValue(isConnected));
                allowing(sessionInfo).isGUESSCapable();
                will(returnValue(isGuessCapable));
                allowing(sessionInfo).isLifecycleLoaded();
                will(returnValue(isLifecycleLoaded));
                allowing(sessionInfo).isShieldedLeaf();
                will(returnValue(isShieldedLeaf));
                allowing(sessionInfo).isSupernode();
                will(returnValue(isSuprtNode));
                allowing(sessionInfo).isUdpPortStable();
                will(returnValue(isUdpPortStable));
                allowing(sessionInfo).lastReportedUdpPort();
                will(returnValue(26));
                allowing(sessionInfo).receivedIpPong();
                will(returnValue(27));

            }
        });

        LocalClientInfoImpl localClientInfoImpl = new LocalClientInfoImpl(new MyException(),
                "My-Thread", "My-Detail", false, sessionInfo);
        localClientInfoImpl.addUserComments("These are my comments");
        String parsedBug = localClientInfoImpl.getParsedBug();
        assertTrue(parsedBug.contains("MyException"));
        assertFalse(localClientInfoImpl.isFatalError());
        context.assertIsSatisfied();
    }

    private class MyException extends Exception {
    }
}
