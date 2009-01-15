package com.limegroup.gnutella.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.limewire.collection.KeyValue;

import com.google.inject.Injector;
import com.limegroup.gnutella.LimeTestUtils;
import com.limegroup.gnutella.util.LimeTestCase;

public class LimeXMLDocumentTest extends LimeTestCase {
            
	private LimeXMLDocumentFactory limeXMLDocumentFactory;

    public LimeXMLDocumentTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(LimeXMLDocumentTest.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
	
	@Override
	protected void setUp() throws Exception {
		Injector injector = LimeTestUtils.createInjector();
		limeXMLDocumentFactory = injector.getInstance(LimeXMLDocumentFactory.class);
	}
	
    public void testHashcode() throws Exception {
        LimeXMLDocumentFactory factory = limeXMLDocumentFactory;
    	List<KeyValue<String, String>> map = new ArrayList<KeyValue<String, String>>();
    	map.add(new KeyValue<String, String>(LimeXMLNames.APPLICATION_NAME, "value"));
    	LimeXMLDocument doc1 = factory.createLimeXMLDocument(map,
                LimeXMLNames.APPLICATION_SCHEMA);
    	LimeXMLDocument doc2 = factory.createLimeXMLDocument(map,
                LimeXMLNames.APPLICATION_SCHEMA);
    	assertEquals(doc1, doc2);
    	assertEquals(doc1.hashCode(), doc2.hashCode());

    	doc1.initIdentifier(new File("file"));
    	assertEquals(doc1, doc2);
    	assertEquals(doc1.hashCode(), doc2.hashCode());
    }

    public void testGetXMLString() throws Exception {
        String xml = "<?xml version=\"1.0\"?><images xsi:noNamespaceSchemaLocation=\"http://www.limewire.com/schemas/image.xsd\"><image title=\"hello world\"/></images>";
        LimeXMLDocument document = limeXMLDocumentFactory.createLimeXMLDocument(xml);
        assertEquals(LimeXMLNames.IMAGE_SCHEMA, document.getSchemaURI());
        assertEquals(xml, document.getXMLString());
        
        xml = "<?xml version=\"1.0\"?><audios xsi:noNamespaceSchemaLocation=\"http://www.limewire.com/schemas/audio.xsd\"><audio title=\"Hello World\" artist=\"Me and you\" album=\"Testing the waters\" genre=\"Rock\" track=\"5/11\" year=\"1999\" seconds=\"956\" bitrate=\"128\" comments=\"woah!\" license=\"me and you\"/></audios>";
        document = limeXMLDocumentFactory.createLimeXMLDocument(xml);
        assertEquals(LimeXMLNames.AUDIO_SCHEMA, document.getSchemaURI());
        assertEquals(xml, document.getXMLString());
    }
}	
