package dk.statsbiblioteket.medieplatform.newspaper.editionRecords;

import org.apache.solr.client.solrj.SolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.sbutil.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.CallResult;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.DomsItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.RunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIDomsAutonomousComponentUtils;
import dk.statsbiblioteket.medieplatform.autonomous.SolrJConnector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class EditionRecordRelationsMaintainerComponent {

    private static Logger log = LoggerFactory.getLogger(EditionRecordRelationsMaintainerComponent.class);


    /**
     * This method reads a properties file either as the first parameter on the command line or as the system variable
     * newspaper.component.properties.file .
     *
     * @param args an array of length 1, where the first entry is a path to the properties file
     */
    public static void main(String[] args) throws Exception {
        log.info("Entered " + EditionRecordRelationsMaintainerComponent.class);
        doMain(args);
    }

    public static int doMain(String[] args) throws Exception {
        log.info("Starting with args {}", args);
        Properties properties = readProperties(args);
        Credentials creds = new Credentials(properties.getProperty(ConfigConstants.DOMS_USERNAME),
                properties.getProperty(ConfigConstants.DOMS_PASSWORD));
        String fedoraLocation = properties.getProperty(ConfigConstants.DOMS_URL);
        int fedoraRetries = Integer.parseInt(properties.getProperty(ConfigConstants.FEDORA_RETRIES, "1"));
        int fedoraDelayBetweenRetries = Integer.parseInt(properties.getProperty(ConfigConstants.FEDORA_DELAY_BETWEEN_RETRIES, "100"));
        EnhancedFedoraImpl eFedora = new EnhancedFedoraImpl(creds,
                fedoraLocation,
                properties.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL),
                null, fedoraRetries, fedoraDelayBetweenRetries);
        SolrServer solrServer = new SolrJConnector(properties.getProperty(ConfigConstants.AUTONOMOUS_SBOI_URL)).getSolrServer();
        DomsItemFactory itemFactory = new DomsItemFactory();
        NewspaperIndex newspaperIndex = new NewspaperIndex(solrServer, itemFactory);

        RunnableComponent<Item> component = new RunnableEditionRecordRelationsMaintainer(properties, eFedora, itemFactory,
                newspaperIndex);
        CallResult<Item> result = SBOIDomsAutonomousComponentUtils.startAutonomousComponent(properties, component,new
                DomsItemFactory());
        log.info("result was: " + result);
        return result.containsFailures();
    }

    /**
     * Reads the properties from the arguments or system properties. Either the first argument must be a path to a
     * properties file, or, if not, the system property "newspaper.component.properties.file" must denote such a path.
     * If neither, then a runtime exception is set
     *
     * @param args the command line arguments
     *
     * @return a properties object parsed from the properties file
     * @throws IOException if the file could not be read
     * @throws RuntimeException    if no path could be determined
     */
    private static Properties readProperties(String[] args) throws IOException, RuntimeException {
        Properties properties = new Properties();
        String propsFileString;
        if (args.length >= 1) {
            propsFileString = args[0];
        } else {
            propsFileString = System.getProperty("newspaper.component.properties.file");
        }
        if (propsFileString == null) {
            throw new RuntimeException("Properties file must be defined either as command-line parameter or as system" + "property newspaper.component.properties .");
        }
        log.info("Reading properties from " + propsFileString);
        File propsFile = new File(propsFileString);
        if (!propsFile.exists()) {
            throw new FileNotFoundException("No such file: " + propsFile.getAbsolutePath());
        }
        properties.load(new FileReader(propsFile));
        return properties;
    }


}
