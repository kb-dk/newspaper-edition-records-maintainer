package dk.statsbiblioteket.medieplatform.newspaper.editionRecords;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.DomsItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.SolrJConnector;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.testng.Assert.*;

public class NewspaperIndexIT {

    private String summaLocation;

    @BeforeClass
    public void setUp() throws Exception {
        Properties props = new Properties();
        try {
            props.load(new FileReader(new File(System.getProperty("integration.test.newspaper.properties"))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        summaLocation = props.getProperty(ConfigConstants.AUTONOMOUS_SBOI_URL);

    }

    @Test(groups = "externalTest")
    public void testGetNewspapers() throws Exception {
        NewspaperIndex newspaperIndex = new NewspaperIndex(new SolrJConnector(summaLocation).getSolrServer(), new DomsItemFactory());
        List<Item> newspapers = newspaperIndex.getNewspapers("berlingsketidende", "1970-01-01");
        assertTrue(newspapers.size() == 1);
    }
}