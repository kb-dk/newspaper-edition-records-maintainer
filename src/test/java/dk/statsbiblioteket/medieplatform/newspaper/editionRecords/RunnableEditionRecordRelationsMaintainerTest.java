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

public class RunnableEditionRecordRelationsMaintainerTest {

    private static final String URI_PREFIX = "info:fedora/";
    private static final String PREDICATE = "http://doms.statsbiblioteket.dk/relations/default/0/1/#isPartOfNewspaper";
    private static final String DOMS_EDITION_ID = "uuid:0c1969ca-94be-4ebb-abab-0bd8130e59d7";
    private static final String DOMS_TITLE_ID1 = "uuid:38deefa7-381f-4abf-a6c1-a3531b54f997";
    private static final String DOMS_TITLE_ID2 = "uuid:781732b1-ca9a-46d4-94cd-ae1c0b7f1ebf";
    private static final String DOMS_TITLE_ID3 = "uuid:aff7e1f0-4242-4fc1-877d-6382f2bbaaa9";
    private static final String MESSAGE = "linking to";
    private static final String NEWSPAPERID = "avis";
    private static final String DATE = "1970-01-01T01:00:00.000+01:00";
    private static final String EDITION_XML = String.format("<mods xmlns=\"http://www.loc.gov/mods/v3\">\n"
                                                                 + "    <titleInfo type=\"uniform\">\n"
                                                                 + "        <title>%s</title>\n"
                                                                 + "    </titleInfo>\n"
                                                                 + "    <originInfo>\n"
                                                                 + "        <dateIssued>%s</dateIssued>\n"
                                                                 + "    </originInfo>\n" + "</mods>", NEWSPAPERID, DATE);
    private static final String EDITION_DATASTREAM_NAME = "EDITION";

    /**
     * Test where one relation should be added.
     *
     * Index query returns three items. DOMS returns two known.
     *
     * The result should be that the missing relation is added.
     *
     * @throws Exception
     */
    @Test
    public void testDoWorkOnItemAddOneRelation() throws Exception {
        //Set up mocks
        Item itemMock = getItemMock();
        ItemFactory<Item> itemFactoryMock = getItemItemFactoryMock();
        ResultCollector resultCollectorMock = getResultCollectorMock();
        EnhancedFedora enhancedFedoraMock = getEnhancedFedoraMock(DOMS_TITLE_ID1, DOMS_TITLE_ID2);
        NewspaperIndex newspaperIndexMock = getNewspaperIndexMock(DOMS_TITLE_ID1, DOMS_TITLE_ID2, DOMS_TITLE_ID3);

        //Call the component with the mocks
        new RunnableEditionRecordRelationsMaintainer(new Properties(), enhancedFedoraMock, itemFactoryMock, newspaperIndexMock)
            .doWorkOnItem(itemMock, resultCollectorMock);

        //One relation should be added
        verify(enhancedFedoraMock).addRelation(DOMS_EDITION_ID, URI_PREFIX + DOMS_EDITION_ID, PREDICATE,
                                               URI_PREFIX + DOMS_TITLE_ID3, false, MESSAGE);

        //Verify normal calls
        verifyNormalCalls(resultCollectorMock, enhancedFedoraMock, newspaperIndexMock);
    }

    /**
     * Test where one relation should be removed.
     *
     * Index query returns two items. DOMS returns three known.
     *
     * The result should be that the superfluous relation is removed.
     *
     * @throws Exception
     */
    @Test
    public void testDoWorkOnItemRemoveOneRelation() throws Exception {
        //Set up mocks
        Item itemMock = getItemMock();
        ItemFactory<Item> itemFactoryMock = getItemItemFactoryMock();
        ResultCollector resultCollectorMock = getResultCollectorMock();
        EnhancedFedora enhancedFedoraMock = getEnhancedFedoraMock(DOMS_TITLE_ID1, DOMS_TITLE_ID2, DOMS_TITLE_ID3);
        NewspaperIndex newspaperIndexMock = getNewspaperIndexMock(DOMS_TITLE_ID1, DOMS_TITLE_ID2);

        //Call the component with the mocks
        new RunnableEditionRecordRelationsMaintainer(new Properties(), enhancedFedoraMock, itemFactoryMock, newspaperIndexMock)
            .doWorkOnItem(itemMock, resultCollectorMock);

        //One relation should be then deleted
        verify(enhancedFedoraMock)
                .deleteRelation(DOMS_EDITION_ID, URI_PREFIX + DOMS_EDITION_ID, PREDICATE, URI_PREFIX + DOMS_TITLE_ID3,
                                false, MESSAGE);

        //Verify normal calls
        verifyNormalCalls(resultCollectorMock, enhancedFedoraMock, newspaperIndexMock);
    }

    /**
     * Test where no relations should be added or removed.
     *
     * Index query returns three items. DOMS returns the same three known.
     *
     * The result should be that no relations should be added or removed.
     *
     * @throws Exception
     */
    @Test
    public void testDoWorkOnItemNoRelationsChanged() throws Exception {
        //Set up mocks
        Item itemMock = getItemMock();
        ItemFactory<Item> itemFactoryMock = getItemItemFactoryMock();
        ResultCollector resultCollectorMock = getResultCollectorMock();
        EnhancedFedora enhancedFedoraMock = getEnhancedFedoraMock(DOMS_TITLE_ID1, DOMS_TITLE_ID2, DOMS_TITLE_ID3);
        NewspaperIndex newspaperIndexMock = getNewspaperIndexMock(DOMS_TITLE_ID1, DOMS_TITLE_ID2, DOMS_TITLE_ID3);

        //Call the component with the mocks
        new RunnableEditionRecordRelationsMaintainer(new Properties(), enhancedFedoraMock, itemFactoryMock, newspaperIndexMock)
            .doWorkOnItem(itemMock, resultCollectorMock);

        //Verify normal calls
        verifyNormalCalls(resultCollectorMock, enhancedFedoraMock, newspaperIndexMock);
    }

    /**
     * Test where two relations should be added.
     *
     * Index query returns three items. DOMS returns one known.
     *
     * The result should be that the missing two relations are added.
     *
     * @throws Exception
     */
    @Test
    public void testDoWorkOnItemAddMultipleRelations() throws Exception {
        //Set up mocks
        Item itemMock = getItemMock();
        ItemFactory<Item> itemFactoryMock = getItemItemFactoryMock();
        ResultCollector resultCollectorMock = getResultCollectorMock();
        EnhancedFedora enhancedFedoraMock = getEnhancedFedoraMock(DOMS_TITLE_ID1);
        NewspaperIndex newspaperIndexMock = getNewspaperIndexMock(DOMS_TITLE_ID1, DOMS_TITLE_ID2, DOMS_TITLE_ID3);

        //Call the component with the mocks
        new RunnableEditionRecordRelationsMaintainer(new Properties(), enhancedFedoraMock, itemFactoryMock, newspaperIndexMock)
            .doWorkOnItem(itemMock, resultCollectorMock);

        //Two relations should be added
        verify(enhancedFedoraMock).addRelation(DOMS_EDITION_ID, URI_PREFIX + DOMS_EDITION_ID, PREDICATE, URI_PREFIX + DOMS_TITLE_ID2, false,
                                               MESSAGE);
        verify(enhancedFedoraMock).addRelation(DOMS_EDITION_ID, URI_PREFIX + DOMS_EDITION_ID, PREDICATE, URI_PREFIX + DOMS_TITLE_ID3, false,
                                               MESSAGE);

        //Verify normal calls
        verifyNormalCalls(resultCollectorMock, enhancedFedoraMock, newspaperIndexMock);
    }

    /**
     * Test where two relations should be removed.
     *
     * Index query returns one item. DOMS returns three known.
     *
     * The result should be that the superfluous relations are removed.
     *
     * @throws Exception
     */
    @Test
    public void testDoWorkOnItemRemoveMultipleRelations() throws Exception {
        //Set up mocks
        Item itemMock = getItemMock();
        ItemFactory<Item> itemFactoryMock = getItemItemFactoryMock();
        ResultCollector resultCollectorMock = getResultCollectorMock();
        EnhancedFedora enhancedFedoraMock = getEnhancedFedoraMock(DOMS_TITLE_ID1, DOMS_TITLE_ID2, DOMS_TITLE_ID3);
        NewspaperIndex newspaperIndexMock = getNewspaperIndexMock(DOMS_TITLE_ID1);

        //Call the component with the mocks
        new RunnableEditionRecordRelationsMaintainer(new Properties(), enhancedFedoraMock, itemFactoryMock, newspaperIndexMock)
            .doWorkOnItem(itemMock, resultCollectorMock);

        //Two relations should be removed
        verify(enhancedFedoraMock)
                .deleteRelation(DOMS_EDITION_ID, URI_PREFIX + DOMS_EDITION_ID, PREDICATE, URI_PREFIX + DOMS_TITLE_ID2,
                                false, MESSAGE);
        verify(enhancedFedoraMock)
                .deleteRelation(DOMS_EDITION_ID, URI_PREFIX + DOMS_EDITION_ID, PREDICATE, URI_PREFIX + DOMS_TITLE_ID3,
                                false, MESSAGE);

        //Verify normal calls
        verifyNormalCalls(resultCollectorMock, enhancedFedoraMock, newspaperIndexMock);
    }

    /**
     * Test where a mix of relations should be added and removed.
     *
     * Index query returns one item. DOMS returns two different items known.
     *
     * The result should be that one relation is added and the superfluous relations are removed.
     *
     * @throws Exception
     */
    @Test
    public void testDoWorkOnItemAddAndRemoveMultipleRelations() throws Exception {
        //Set up mocks
        Item itemMock = getItemMock();
        ItemFactory<Item> itemFactoryMock = getItemItemFactoryMock();
        ResultCollector resultCollectorMock = getResultCollectorMock();
        EnhancedFedora enhancedFedoraMock = getEnhancedFedoraMock(DOMS_TITLE_ID2, DOMS_TITLE_ID3);
        NewspaperIndex newspaperIndexMock = getNewspaperIndexMock(DOMS_TITLE_ID1);

        //Call the component with the mocks
        new RunnableEditionRecordRelationsMaintainer(new Properties(), enhancedFedoraMock, itemFactoryMock, newspaperIndexMock)
            .doWorkOnItem(itemMock, resultCollectorMock);

        //One relation should be added
        verify(enhancedFedoraMock).addRelation(DOMS_EDITION_ID, URI_PREFIX + DOMS_EDITION_ID, PREDICATE, URI_PREFIX + DOMS_TITLE_ID1, false,
                                               MESSAGE);
        //Two relations should be deleted
        verify(enhancedFedoraMock)
                .deleteRelation(DOMS_EDITION_ID, URI_PREFIX + DOMS_EDITION_ID, PREDICATE, URI_PREFIX + DOMS_TITLE_ID2,
                                false, MESSAGE);
        verify(enhancedFedoraMock)
                .deleteRelation(DOMS_EDITION_ID, URI_PREFIX + DOMS_EDITION_ID, PREDICATE, URI_PREFIX + DOMS_TITLE_ID3,
                                false, MESSAGE);

        //Verify normal calls
        verifyNormalCalls(resultCollectorMock, enhancedFedoraMock, newspaperIndexMock);
    }

    /**
     * Verify that the expected normal calls are made, and none other.
     * @param resultCollectorMock Verify no calls are med to this.
     * @param enhancedFedoraMock Verify reading of MODS and looking up of relations is done, and nothing else.
     * @param newspaperIndexMock Verify search is done, and nothing else.
     *
     * @throws Exception never.
     */
    private void verifyNormalCalls(ResultCollector resultCollectorMock, EnhancedFedora enhancedFedoraMock,
                                   NewspaperIndex newspaperIndexMock) throws Exception {
        //Verify expected DOMS and index calls
        verify(newspaperIndexMock).getNewspapers(NEWSPAPERID, DATE);
        verify(enhancedFedoraMock).getXMLDatastreamContents(DOMS_EDITION_ID, EDITION_DATASTREAM_NAME);
        verify(enhancedFedoraMock).getNamedRelations(DOMS_EDITION_ID, PREDICATE, null);

        //No more calls should be made to DOMS, and none to the result collector
        verifyNoMoreInteractions(newspaperIndexMock, enhancedFedoraMock, resultCollectorMock);
    }

     /**
      * Mock item, return id when asked.
      *
      * @return Mock item.
      */
    private Item getItemMock() {
        Item itemMock = mock(Item.class);
        when(itemMock.getDomsID()).thenReturn(DOMS_EDITION_ID);
        return itemMock;
    }

    /**
     * Mock item factory, knows how to create the test objects.
     *
     * @return Mock item factory.
     */
    private ItemFactory<Item> getItemItemFactoryMock() {
        ItemFactory<Item> itemFactoryMock = mock(ItemFactory.class);
        when(itemFactoryMock.create(DOMS_TITLE_ID1)).thenReturn(new Item(DOMS_TITLE_ID1));
        when(itemFactoryMock.create(DOMS_TITLE_ID2)).thenReturn(new Item(DOMS_TITLE_ID2));
        when(itemFactoryMock.create(DOMS_TITLE_ID3)).thenReturn(new Item(DOMS_TITLE_ID3));
        return itemFactoryMock;
    }

    /**
     * Mock result collector.
     *
     * @return Mock result collector.
     */
    private ResultCollector getResultCollectorMock() {
        return mock(ResultCollector.class);
    }

    /**
     * Mock Fedora. Will return MODS when asked, and list given relations.
     *
     * @param uuids Objects with existing relations.
     * @return Mock Fedora.
     *
     * @throws Exception never.
     */
    private EnhancedFedora getEnhancedFedoraMock(String... uuids) throws Exception {
        EnhancedFedora enhancedFedoraMock = mock(EnhancedFedora.class);
        when(enhancedFedoraMock.getXMLDatastreamContents(DOMS_EDITION_ID, EDITION_DATASTREAM_NAME)).thenReturn(
                EDITION_XML);
        List<FedoraRelation> relations = new ArrayList<>();
        for (String uuid : uuids) {
            FedoraRelation relation = new FedoraRelation(URI_PREFIX + DOMS_EDITION_ID, PREDICATE, URI_PREFIX + uuid);
            relations.add(relation);
        }
        when(enhancedFedoraMock.getNamedRelations(DOMS_EDITION_ID, PREDICATE, null)).thenReturn(
                relations);
        return enhancedFedoraMock;
    }

    /**
     * Mock index. Will return the given ids when asked.
     * @param uuids List of uuids to find.
     * @return Mock index.
     */
    private NewspaperIndex getNewspaperIndexMock(String... uuids) {
        NewspaperIndex newspaperIndexMock = mock(NewspaperIndex.class);
        List<Item> items = new ArrayList<>();
        for (String uuid : uuids) {
            items.add(new Item(uuid));
        }
        when(newspaperIndexMock.getNewspapers(NEWSPAPERID, DATE)).thenReturn(items);
        return newspaperIndexMock;
    }
}