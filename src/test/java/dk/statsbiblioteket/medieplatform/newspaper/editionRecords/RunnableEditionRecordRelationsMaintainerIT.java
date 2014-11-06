package dk.statsbiblioteket.medieplatform.newspaper.editionRecords;

import org.testng.annotations.Test;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class RunnableEditionRecordRelationsMaintainerIT {
    /**
     * Integration test for edition records
     * @throws Exception
     */
    @Test(groups="externalTest")
    public void testDoWorkOnItem() throws Exception {
        //TODO
        //Create an edition object relating to a known newspaper title
        //Add a relation to another one
        //Flush the triple store index
        //Run the test
        //Test that relations are now to the correct newspaper title
    }

}