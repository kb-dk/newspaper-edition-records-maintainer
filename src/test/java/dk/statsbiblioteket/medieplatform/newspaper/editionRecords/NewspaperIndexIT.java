package dk.statsbiblioteket.medieplatform.newspaper.editionRecords;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.DomsItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.SolrJConnector;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import static org.testng.AssertJUnit.*;

public class NewspaperIndexIT {

    private String summaLocation;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        Properties props = new Properties();


        props.load(new FileInputStream(pathToProperties));
        summaLocation = props.getProperty(ConfigConstants.AUTONOMOUS_SBOI_URL);

    }

    @Test(groups = "externalTest")
    public void testGetNewspapers() throws Exception {
        NewspaperIndex newspaperIndex = new NewspaperIndex(new SolrJConnector(summaLocation).getSolrServer(), new DomsItemFactory());
        List<Item> newspapers = newspaperIndex.getNewspapers("adresseavisen1759", "1789-01-01");
        assertEquals(1, newspapers.size());
    }
}