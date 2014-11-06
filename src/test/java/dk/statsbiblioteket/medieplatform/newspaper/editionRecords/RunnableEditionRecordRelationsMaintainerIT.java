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
     * Test where one relation should be added.
     *
     * Index query returns three items. DOMS returns two known.
     *
     * The result should be that the missing relation is added.
     *
     * @throws Exception
     */
    @Test(groups="externalTest")
    public void testDoWorkOnItemAddOneRelation() throws Exception {
    }

}