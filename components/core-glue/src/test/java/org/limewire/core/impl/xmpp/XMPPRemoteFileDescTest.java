package org.limewire.core.impl.xmpp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.limewire.core.api.friend.Friend;
import org.limewire.core.api.friend.FriendPresence;
import org.limewire.core.api.friend.Network;
import org.limewire.core.api.friend.feature.features.AuthTokenFeature;
import org.limewire.io.GUID;
import org.limewire.net.address.AddressFactory;
import org.limewire.net.address.AddressSerializer;
import org.limewire.util.BaseTestCase;
import org.limewire.xmpp.api.client.XMPPAddress;
import org.limewire.xmpp.client.impl.XMPPAddressResolver;

import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.downloader.serial.RemoteHostMemento;

public class XMPPRemoteFileDescTest extends BaseTestCase{
    
    public XMPPRemoteFileDescTest(String name) {
        super(name);
    }

    /**
     * Tests the various conditions of XMPPRemoteFileDesc.equals()
     */
    public void testEquals() {

        Mockery context = new Mockery() {
            {   setImposteriser(ClassImposteriser.INSTANCE);
            }
        };
        
        XMPPAddress address = context.mock(XMPPAddress.class);
        XMPPAddress otherAddress = context.mock(XMPPAddress.class);
        
        XMPPRemoteFileDesc rfd = createRFD(address, null, null);
        XMPPRemoteFileDesc otherRfd = context.mock(XMPPRemoteFileDesc.class);
        
        assertEquals(rfd, rfd);
        
        // TODO: must put backwards to get equals invoked on rfd? 
        assertNotEquals(new Integer(-5), rfd);
        
        assertNotEquals(rfd, otherRfd);
        // address not equal
        assertNotEquals(rfd, createRFD(otherAddress, null, null));
        // client guid not equal
        assertNotEquals(rfd, createRFD2(address, null, null));
        // size not equal
        assertNotEquals(rfd, createRFD3(address, null, null));
        // URNs not equal
        assertNotEquals(rfd, createRFD4(address, null, null));
        // filename not equal - should this be true?
        assertEquals(rfd, createRFD5(address, null, null));
    }
    
    /**
     * Tests the hash function for uniqueness and consistency 
     */
    public void testHashCode() {
        
        Mockery context = new Mockery() {
            {   setImposteriser(ClassImposteriser.INSTANCE);
            }
        };
        
        XMPPAddress sharedAddress = context.mock(XMPPAddress.class);
        XMPPAddress otherAddress = context.mock(XMPPAddress.class);
        
        XMPPRemoteFileDesc rfdA = createRFDwithGUID1(sharedAddress, null, null);
        XMPPRemoteFileDesc rfdB = createRFDwithGUID1(sharedAddress, null, null);
        XMPPRemoteFileDesc rfdC = createRFDwithGUID1(otherAddress, null, null);
        XMPPRemoteFileDesc rfdD = createRFDwithGUID2(sharedAddress, null, null);
        
        assertEquals(rfdA.hashCode(), rfdA.hashCode());
        assertEquals(rfdA.hashCode(), rfdB.hashCode());
        assertNotEquals(rfdA.hashCode(), rfdC.hashCode());
        assertNotEquals(rfdA.hashCode(), rfdD.hashCode());
        
    }
    
    /**
     * Tests getCredentials() across multiple successful and unsuccessful invocations. 
     */
    public void testGetCredentials() {
    
        Mockery context = new Mockery() {
            {   setImposteriser(ClassImposteriser.INSTANCE);
            }
        };
                
        final XMPPAddressResolver addressResolver = context.mock(XMPPAddressResolver.class);
        
        final XMPPAddress addressNullResolve = context.mock(XMPPAddress.class);
        final XMPPAddress addressNullFeature = context.mock(XMPPAddress.class);
        final XMPPAddress addressGood = context.mock(XMPPAddress.class);
        
        final FriendPresence presenceNullFeature = context.mock(FriendPresence.class);
        final FriendPresence presenceGood = context.mock(FriendPresence.class);
        
        final AuthTokenFeature featureGood = context.mock(AuthTokenFeature.class);
        
        final byte[] featureFeatureFeatureGood = new byte[] {'a','u','t','h'};
        final Friend friendGood = context.mock(Friend.class);
        final Network networkGood = context.mock(Network.class);
        final String idGood = "fakeNetWorkepID";
        
        final XMPPRemoteFileDesc rfdNullResolve = createRFD(addressNullResolve, null, addressResolver);
        final XMPPRemoteFileDesc rfdNullFeature = createRFD(addressNullFeature, null, addressResolver);
        final XMPPRemoteFileDesc rfdGood = createRFD(addressGood, null, addressResolver);
        
        context.checking(new Expectations() {
            {   allowing(addressResolver).getPresence(addressNullResolve);
                will(returnValue(null));
                
                allowing(addressResolver).getPresence(addressNullFeature);
                will(returnValue(presenceNullFeature));
                allowing(presenceNullFeature).getFeature(AuthTokenFeature.ID);
                will(returnValue(null));

                allowing(addressResolver).getPresence(addressGood);
                will(returnValue(presenceGood));
                allowing(presenceGood).getFeature(AuthTokenFeature.ID);
                will(returnValue(featureGood));
                allowing(featureGood).getFeature();
                will(returnValue(featureFeatureFeatureGood));
                allowing(presenceGood).getFriend();
                will(returnValue(friendGood));
                allowing(friendGood).getNetwork();
                will(returnValue(networkGood));
                allowing(networkGood).getCanonicalizedLocalID();
                will(returnValue(idGood));
            }});
        
        assertNull(rfdNullResolve.getCredentials());
        assertNull(rfdNullFeature.getCredentials());
        
        Credentials credentials = rfdGood.getCredentials();
        assertInstanceof(UsernamePasswordCredentials.class, credentials);
        
        UsernamePasswordCredentials userCredentials = (UsernamePasswordCredentials) credentials;
        assertEquals(idGood, userCredentials.getUserName());
        assertEquals(new String(featureFeatureFeatureGood), userCredentials.getPassword());
        
        context.assertIsSatisfied();
                
    }
    
    public void testGetUrlPath() {
     
        Mockery context = new Mockery() {
            {   setImposteriser(ClassImposteriser.INSTANCE);
            }
        };
                
        final XMPPAddressResolver addressResolver = context.mock(XMPPAddressResolver.class);
        
        final XMPPAddress addressFriendOffline = context.mock(XMPPAddress.class);
        final XMPPAddress addressGood = context.mock(XMPPAddress.class);
        final XMPPAddress addressException = context.mock(XMPPAddress.class);
        
        final FriendPresence presenceGood = context.mock(FriendPresence.class);
        final FriendPresence presenceException = context.mock(FriendPresence.class);
        
        final Friend friendGood = context.mock(Friend.class);
        final Network networkGood = context.mock(Network.class);
        final String idGood = "fakeNetWorkepID";
        
        final String sha1 = "urn:sha1:NETZHKEJKTCM74ZQQALJWSLWQHQJ7N6Q";        
        final XMPPRemoteFileDesc rfdFriendOffline = createRFDwithSHA1Custom(addressFriendOffline, null, addressResolver, sha1);
        final XMPPRemoteFileDesc rfdGood = createRFDwithSHA1Custom(addressGood, null, addressResolver, sha1);
        final XMPPRemoteFileDesc rfdException = createRFDwithSHA1Custom(addressException, null, addressResolver, sha1);
        
        context.checking(new Expectations() {
            {   allowing(addressResolver).getPresence(addressFriendOffline);
                will(returnValue(null));
              
                allowing(addressResolver).getPresence(addressGood);
                will(returnValue(presenceGood));
                allowing(presenceGood).getFriend();
                will(returnValue(friendGood));
                allowing(friendGood).getNetwork();
                will(returnValue(networkGood));
                allowing(networkGood).getCanonicalizedLocalID();
                will(returnValue(idGood));
                
                allowing(addressResolver).getPresence(addressException);
                will(returnValue(presenceException));
                allowing(presenceException).getFriend();
                // TODO: Do not need this at the moment
                // This is a hack to force the exception to be thrown in the same location
                //  to the encoding call.
                will(throwException(new UnsupportedEncodingException()));
            }});
        
        
        String pathFriendOffline = rfdFriendOffline.getUrlPath();
        assertNotNull(pathFriendOffline);
        assertNotSame(pathFriendOffline, "");
        
        String pathGood = rfdGood.getUrlPath();
        assertGreaterThan(-1, pathGood.indexOf(sha1));
        assertGreaterThan(-1, pathGood.indexOf(CoreGlueXMPPService.FRIEND_DOWNLOAD_PREFIX));
                
        try {
            rfdException.getUrlPath();
            fail("A bad \"encoding\" should have thrown an exception");
        } catch (RuntimeException e) {
        }
        
        context.assertIsSatisfied();
        
    }

    public void testIsMe() {
        
        Mockery context = new Mockery() {
            {   setImposteriser(ClassImposteriser.INSTANCE);
            }
        };
        
        
        byte[] guid1 = new byte[0];
        final XMPPAddress address1 = context.mock(XMPPAddress.class);
        XMPPRemoteFileDesc rfd1 = createRFDwithGUIDCustom(address1, null, null, guid1);
        
        byte[] guid2 = new byte[] {1};
        final XMPPAddress address2 = context.mock(XMPPAddress.class);
        XMPPRemoteFileDesc rfd2 = createRFDwithGUIDCustom(address2, null, null, guid2);
        
        byte[] guid3 = new byte[] {1,2,3,4};
        final XMPPAddress address3 = context.mock(XMPPAddress.class);
        XMPPRemoteFileDesc rfd3 = createRFDwithGUIDCustom(address3, null, null, guid3);
        
        byte[] guid4 = new byte[] {5,6,9,'n',10};
        final XMPPAddress address4 = context.mock(XMPPAddress.class);
        XMPPRemoteFileDesc rfd4 = createRFDwithGUIDCustom(address4, null, null, guid4);
        
        byte[] guid5 = new byte[] {1,2,3,4};
        final XMPPAddress address5 = context.mock(XMPPAddress.class);
        XMPPRemoteFileDesc rfd5 = createRFDwithGUIDCustom(address5, null, null, guid3);
        
        assertTrue(rfd1.isMe(guid1));
        assertFalse(rfd1.isMe(guid2));
        assertFalse(rfd1.isMe(guid3));
        assertFalse(rfd1.isMe(guid4));
        assertFalse(rfd1.isMe(guid5));
        
        assertFalse(rfd2.isMe(guid1));
        assertTrue(rfd2.isMe(guid2));
        assertFalse(rfd2.isMe(guid3));
        assertFalse(rfd2.isMe(guid4));
        assertFalse(rfd2.isMe(guid5));
        
        assertFalse(rfd3.isMe(guid1));
        assertFalse(rfd3.isMe(guid2));
        assertTrue(rfd3.isMe(guid3));
        assertFalse(rfd3.isMe(guid4));
        assertTrue(rfd3.isMe(guid5));
        
        assertFalse(rfd4.isMe(guid1));
        assertFalse(rfd4.isMe(guid2));
        assertFalse(rfd4.isMe(guid3));
        assertTrue(rfd4.isMe(guid4));
        assertFalse(rfd4.isMe(guid5));
        
        assertFalse(rfd5.isMe(guid1));
        assertFalse(rfd5.isMe(guid2));
        assertTrue(rfd5.isMe(guid3));
        assertFalse(rfd5.isMe(guid4));
        assertTrue(rfd5.isMe(guid5));
    }
    

    public void testGetSHA1Urn() {
        
        Mockery context = new Mockery() {
            {   setImposteriser(ClassImposteriser.INSTANCE);
            }
        };
        
        final XMPPAddress address = context.mock(XMPPAddress.class);
        
        String sha1A = "urn:sha1:NETZHKEJKTCM74ZQQALJWSLWQHQJ7N6Q";
        String sha1B = "urn:sha1:NETZHKEJKTCM74ZQ6LJWSLWQHQJ7N6Q1";
        String sha1C = "urn:sha1:NETZHKEJK345SDF7N6Q2312321321321";
        String sha1D = "urn:sha1:NETZHKEJKSNEAKYSHA1LWQHQJ7N4Q222";
        
        Set<URN> urnsAllNonSHA1 = new HashSet<URN>();
        urnsAllNonSHA1.add(URN.createGUIDUrn(new GUID(new byte[] {'x', '1', '2', '3', '4', 5, 6, 7, 8, 9,10, 11,12,13,14,15})));
        urnsAllNonSHA1.add(URN.createGUIDUrn(new GUID(new byte[] {'x', '1', '2', '3', '4', 5, 6, 7, 8, 9,10, 11,12,13,14,15})));
        urnsAllNonSHA1.add(URN.createGUIDUrn(new GUID(new byte[] {'x', '1', '2', '3', 'x', 5, 6, 7, 8, 9,10, 11,12,13,14,15})));
                
        Set<URN> urnsB = new HashSet<URN>();
        urnsB.add(URN.createGUIDUrn(new GUID(new byte[] {'x', '1', '2', '3', '4', 5, 6, 7, 8, 9,10, 11,12,13,14,15})));
        urnsB.add(URN.createGUIDUrn(new GUID(new byte[] {'x', '1', '2', '3', '4', 5, 6, 7, 8, 9,10, 11,12,13,14,15})));
        try {
            urnsB.add(URN.createSHA1Urn(sha1D));
        } 
        catch (IOException e) {
            fail("Could not create a sha1 urn for test");
        }
        urnsB.add(URN.createGUIDUrn(new GUID(new byte[] {'x', '1', '2', '3', 'x', 5, 6, 7, 8, 9,10, 11,12,13,14,15})));
        
        XMPPRemoteFileDesc rfdNoSHA1 = createRFD(address, null, null);
        XMPPRemoteFileDesc rfdSingle = createRFDwithSHA1Custom(address, null, null, sha1A);
        XMPPRemoteFileDesc rfdMultipleSHA1 = createRFDwithSHA1Custom(address, null, null, sha1A, sha1C, sha1B);
        XMPPRemoteFileDesc rfdAllNonSHA1 = createRFDwithUrnsCustom(address, null, null, urnsAllNonSHA1);
        XMPPRemoteFileDesc rfdSomeNonSHA1 = createRFDwithUrnsCustom(address, null, null, urnsB);
        
        
        try {
            rfdNoSHA1.getSHA1Urn();
            fail("Should have thrown an exception since there were no SHA1 urns in the RFD");
        }
        catch (IllegalArgumentException e) {
        }
        
        assertEquals(sha1A.toString(), rfdSingle.getSHA1Urn().toString());
        assertContains(rfdSingle.getUrns(), rfdSingle.getSHA1Urn());
        assertContains(rfdMultipleSHA1.getUrns(), rfdMultipleSHA1.getSHA1Urn());
        
        try {
            rfdAllNonSHA1.getSHA1Urn();
            fail("Should have thrown an exception since there were no SHA1 urns in the RFD");
        }
        catch (IllegalArgumentException e) {
        }
        
        assertEquals(sha1D.toString(), rfdSomeNonSHA1.getSHA1Urn().toString());

    }
    
    public void testToMemento() {
        
        Mockery context = new Mockery() {
            {   setImposteriser(ClassImposteriser.INSTANCE);
            }
        };
        
        final String filename = "test";
        
        final XMPPAddress address = context.mock(XMPPAddress.class);
        final AddressFactory addressFactory = context.mock(AddressFactory.class);
        final AddressSerializer serialer = context.mock(AddressSerializer.class);
        
        XMPPRemoteFileDesc rfd = createRFDwithFilenameCustom(address, addressFactory, null, filename);
        
        try {
            context.checking(new Expectations() {
                {  
                    allowing(addressFactory).getSerializer(address.getClass());
                    will(returnValue(serialer));
                    allowing(serialer).getAddressType();
                    will(returnValue("memento"));
                    allowing(serialer).serialize(address);
                    will(returnValue(new byte[] {'m','o','r','i'}));
                }});
        } 
        catch (IOException e) {
        }
        
        RemoteHostMemento memento = rfd.toMemento();
        
        assertEquals(filename, memento.getFileName());
        
        context.assertIsSatisfied();
        
    }
    
    public XMPPRemoteFileDesc createRFD(XMPPAddress address, AddressFactory addressFactory, XMPPAddressResolver addressResolver) {
        return new XMPPRemoteFileDesc(address, Long.MAX_VALUE, null, Long.MAX_VALUE, new byte[] {'x'}, Integer.MIN_VALUE, Integer.MAX_VALUE, null,
                new HashSet<URN>(), null, Long.MIN_VALUE, false, addressFactory, addressResolver);
    }
    
    public XMPPRemoteFileDesc createRFD2(XMPPAddress address, AddressFactory addressFactory, XMPPAddressResolver addressResolver) {
        return new XMPPRemoteFileDesc(address, Long.MAX_VALUE, null, Long.MAX_VALUE, new byte[] {'y'}, Integer.MIN_VALUE, Integer.MAX_VALUE, null,
                new HashSet<URN>(), null, Long.MIN_VALUE, false, addressFactory, addressResolver);
    }
    
    public XMPPRemoteFileDesc createRFD3(XMPPAddress address, AddressFactory addressFactory, XMPPAddressResolver addressResolver) {
        return new XMPPRemoteFileDesc(address, Long.MAX_VALUE, null, -1, new byte[] {'x'}, Integer.MIN_VALUE, Integer.MAX_VALUE, null,
                new HashSet<URN>(), null, Long.MIN_VALUE, false, addressFactory, addressResolver);
    }
    
    public XMPPRemoteFileDesc createRFD4(XMPPAddress address, AddressFactory addressFactory, XMPPAddressResolver addressResolver) {
        
        HashSet<URN> urns = new HashSet<URN>();
        try {
            urns.add(URN.createUrnFromString("urn:sha1:NETZHKEJKTCM74ZQQALJWSLWQHQJ7N6Q"));
        } catch (IOException e) {
        }
        
        return new XMPPRemoteFileDesc(address, Long.MAX_VALUE, null, Long.MAX_VALUE, new byte[] {'x'}, Integer.MIN_VALUE, Integer.MAX_VALUE, null,
                urns, null, Long.MIN_VALUE, false, addressFactory, addressResolver);
    }
    
    public XMPPRemoteFileDesc createRFD5(XMPPAddress address, AddressFactory addressFactory, XMPPAddressResolver addressResolver) {
        return new XMPPRemoteFileDesc(address, Long.MAX_VALUE, "huh", Long.MAX_VALUE, new byte[] {'x'}, Integer.MIN_VALUE, Integer.MAX_VALUE, null,
                new HashSet<URN>(), null, Long.MIN_VALUE, false, addressFactory, addressResolver);
    }
    
    public XMPPRemoteFileDesc createRFDwithGUID1(XMPPAddress address, AddressFactory addressFactory, XMPPAddressResolver addressResolver) {
        return new XMPPRemoteFileDesc(address, Long.MAX_VALUE, null, Long.MAX_VALUE, 
                new byte[] {'0', '1', '2', '3', '4', 5, 6, 7, 8, 9,10, 11,12,13,14,15}, 
                Integer.MIN_VALUE, Integer.MAX_VALUE, null,
                new HashSet<URN>(), null, Long.MIN_VALUE, false, addressFactory, addressResolver);
    }
    
    public XMPPRemoteFileDesc createRFDwithGUID2(XMPPAddress address, AddressFactory addressFactory, XMPPAddressResolver addressResolver) {
        return new XMPPRemoteFileDesc(address, Long.MAX_VALUE, null, Long.MAX_VALUE, 
                new byte[] {'x', '1', '2', '3', '4', 5, 6, 7, 8, 9,10, 11,12,13,14,15}, 
                Integer.MIN_VALUE, Integer.MAX_VALUE, null,
                new HashSet<URN>(), null, Long.MIN_VALUE, false, addressFactory, addressResolver);
    }
    
    public XMPPRemoteFileDesc createRFDwithSHA1Custom(XMPPAddress address, AddressFactory addressFactory, 
            XMPPAddressResolver addressResolver, String... sha1) {
        
        HashSet<URN> urns = new HashSet<URN>();
        try {
            for (String s : sha1 ) {
                urns.add(URN.createUrnFromString(s));
            }
        } catch (IOException e) {
            fail("Could not create a sha1 urn for test");
        }
        
        return new XMPPRemoteFileDesc(address, Long.MAX_VALUE, null, Long.MAX_VALUE, 
                new byte[] {'x', '1', '2', '3', '4', 5, 6, 7, 8, 9,10, 11,12,13,14,15}, 
                Integer.MIN_VALUE, Integer.MAX_VALUE, null,
                urns, null, Long.MIN_VALUE, false, addressFactory, addressResolver);
    }
    
    public XMPPRemoteFileDesc createRFDwithUrnsCustom(XMPPAddress address, AddressFactory addressFactory, 
            XMPPAddressResolver addressResolver, Set<URN> urns) {
     
        return new XMPPRemoteFileDesc(address, Long.MAX_VALUE, null, Long.MAX_VALUE, 
                new byte[] {'x', '1', '2', '3', '4', 5, 6, 7, 8, 9,10, 11,12,13,14,15}, 
                Integer.MIN_VALUE, Integer.MAX_VALUE, null,
                urns, null, Long.MIN_VALUE, false, addressFactory, addressResolver);
    }
    
    public XMPPRemoteFileDesc createRFDwithGUIDCustom(XMPPAddress address, AddressFactory addressFactory, XMPPAddressResolver addressResolver, byte[] guid) {
        return new XMPPRemoteFileDesc(address, Long.MAX_VALUE, null, Long.MAX_VALUE, 
                guid, 
                Integer.MIN_VALUE, Integer.MAX_VALUE, null,
                new HashSet<URN>(), null, Long.MIN_VALUE, false, addressFactory, addressResolver);
    }
    
    public XMPPRemoteFileDesc createRFDwithFilenameCustom(XMPPAddress address, AddressFactory addressFactory, XMPPAddressResolver addressResolver, String filename) {
        return new XMPPRemoteFileDesc(address, Long.MAX_VALUE, filename, Long.MAX_VALUE, 
                new byte[] {'x', '1', '2', '3', '4', 5, 6, 7, 8, 9,10, 11,12,13,14,15},  
                Integer.MIN_VALUE, Integer.MAX_VALUE, null,
                new HashSet<URN>(), null, Long.MIN_VALUE, false, addressFactory, addressResolver);
    }
    
}
