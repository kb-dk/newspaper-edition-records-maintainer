package dk.statsbiblioteket.medieplatform.newspaper.editionRecords;

import org.w3c.dom.Document;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RunnableEditionRecordRelationsMaintainer extends AbstractRunnableComponent<Item> {
    private static final String URI_PREFIX = "info:fedora/";
    private final EnhancedFedora eFedora;
    private String editionToNewspaperRelation = "http://doms.statsbiblioteket.dk/relations/default/0/1/#isPartOfNewspaper";
    private ItemFactory<Item> itemFactory;
    private NewspaperIndex newspaperIndex;

    public RunnableEditionRecordRelationsMaintainer(Properties properties, EnhancedFedora eFedora,
                                                    ItemFactory<Item> itemFactory, NewspaperIndex newspaperIndex) {
        super(properties);
        this.eFedora = eFedora;
        this.itemFactory = itemFactory;
        this.newspaperIndex = newspaperIndex;
    }

    @Override
    public String getEventID() {
        return "Editions_relations_generated";
    }

    @Override
    public void doWorkOnItem(Item item, ResultCollector resultCollector) throws Exception {
        // We assume that the received item is an edition object
        // This method is called whenever an edition object has changed

        // Get PID for the edition object
        String domsID = item.getDomsID();

        String newspaperMods = eFedora.getXMLDatastreamContents(domsID, "EDITION");
        Document newspaperDOM = DOM.stringToDOM(newspaperMods, true);
        XPathSelector xpath = DOM.createXPathSelector("v3", "http://www.loc.gov/mods/v3");
        String avisID = xpath.selectString(newspaperDOM, "/v3:mods/v3:titleInfo[@type='uniform']/v3:title/text()");
        String date = xpath.selectString(newspaperDOM, "/v3:mods/v3:originInfo/v3:dateIssued/text()");

        // Get all newspapers that match given edition and date, i.e. titles that SHOULD have the
        // relation
        List<Item> wantedNewspapers =  newspaperIndex.getNewspapers(avisID, date);

        // Get all titles that already HAVE the relation
        List<Item> titlesWithRelation = getTitlesWithRelation(domsID);

        // Now we want wantedTitles to = titlesWithRelation

        // Add relations to titles that are wanted but aren't in titlesWithRelation
        List<Item> titlesToAdd = getTitlesWantedButWithoutRelation(wantedNewspapers, titlesWithRelation);
        for (Item toAdd : titlesToAdd) {
            addRelationFromEditionToNewspaper(toAdd, domsID);
        }

        // Remove relations that are in titlesWithRelation but aren't in wantedTiles
        List<Item> titlesToRemove = getTitlesWithRelationButUnwanted(wantedNewspapers, titlesWithRelation);
        for (Item toRemove : titlesToRemove) {
            removeRelationFromEditionToNewspaper(toRemove, domsID);
        }
    }

    /**
     * Get titles to which relations are wanted but that aren't in titlesWithRelation
     *
     * @param wantedTitles Titles that we want
     * @param titlesWithRelation Titles with a relation from edition that we already have
     * @return titles to which relations are wanted but that aren't in titlesWithRelation
     */
    private List<Item> getTitlesWantedButWithoutRelation(List<Item> wantedTitles, List<Item> titlesWithRelation) {
        List<Item> result = new ArrayList<>();
        for (Item wantedTitle : wantedTitles) {
            if (!titlesWithRelation.contains(wantedTitle)) {
                result.add(wantedTitle);
            }
        }
        return result;
    }

    /**
     * Get titles that are in titlesWithRelation but aren't in wantedTitles
     *
     * @param wantedTitles Titles that we want
     * @param titlesWithRelation Titles with a relation from edition that we already have
     * @return titles that are in titlesWithRelation but aren't in wantedTitles
     */
    private List<Item> getTitlesWithRelationButUnwanted(List<Item> wantedTitles, List<Item> titlesWithRelation) {
        List<Item> result = new ArrayList<>();
        for (Item titleWithRelation : titlesWithRelation) {
            if (!wantedTitles.contains(titleWithRelation)) {
                result.add(titleWithRelation);
            }
        }
        return result;
    }

    /**
     * Get all titles that have the wanted relation from edition object with given DOMS PID
     *
     * @param editionDomsID Edition object from which relation should go
     * @return All titles that have the wanted relation from edition object with given DOMS PID
     */
    private List<Item> getTitlesWithRelation(String editionDomsID) throws
            BackendMethodFailedException, BackendInvalidResourceException, BackendInvalidCredsException {

        List<Item> titles = new ArrayList<>();

        // Get all relations that go to a title from a given edition object
        List<FedoraRelation> relations = eFedora.getNamedRelations(editionDomsID, editionToNewspaperRelation, null);

        // Collect the titles that these relations point to. (Relations point from Edition to Newspaper)
        for (FedoraRelation relation : relations) {
            titles.add(itemFactory.create(uriToDomsID(relation.getObject())));
        }

        return titles;
    }

    /**
     * Add relation to given title from edition with given PID (editionDomsID) in DOMS
     *
     * @param title The title which should be at the "target" end of the wanted relation
     * @param editionDomsID The DOMS PID of the edition object which should be the "source" of wanted relation
     * @throws BackendMethodFailedException
     * @throws BackendInvalidResourceException
     * @throws BackendInvalidCredsException
     */
    private void addRelationFromEditionToNewspaper(Item title, String editionDomsID) throws
            BackendMethodFailedException, BackendInvalidResourceException, BackendInvalidCredsException {

        // Add relation from edition to newspaper object ("titelpost")
        try {
            eFedora.addRelation(editionDomsID, URI_PREFIX + editionDomsID, editionToNewspaperRelation,
                                URI_PREFIX + title.getDomsID(), false, "linking to");
        } catch (BackendInvalidCredsException objectIsPublished) {
            // Edition was already published, so unpublish (set to "I" (inactive)) before adding
            eFedora.modifyObjectState(editionDomsID, "I", "comment");
            try {
                eFedora.addRelation(editionDomsID, URI_PREFIX + editionDomsID, editionToNewspaperRelation,
                                    URI_PREFIX + title.getDomsID(), false, "linking to");
            } finally {
                // Re-publish (set to "A" (active))
                eFedora.modifyObjectState(editionDomsID, "A", "comment");
            }
        }
    }

    /**
     * Remove relation to given title from edition object with given PID (editionDomsID) in DOMS, if it exists
     *
     * @param title The title to which the possible relation from editionDomsID should be removed
     * @param editionDomsID The source of the relations to title that should be removed
     * @throws BackendMethodFailedException
     * @throws BackendInvalidResourceException
     * @throws BackendInvalidCredsException
     */
    private void removeRelationFromEditionToNewspaper(Item title, String editionDomsID) throws
            BackendMethodFailedException, BackendInvalidResourceException, BackendInvalidCredsException {
        // Add relation from edition to newspaper object ("titelpost")
        try {
            eFedora.deleteRelation(editionDomsID, URI_PREFIX + editionDomsID, editionToNewspaperRelation,
                                   URI_PREFIX + title.getDomsID(), false, "linking to");
        } catch (BackendInvalidCredsException objectIsPublished) {
            // Edition was already published, so unpublish (set to "I" (inactive)) before adding
            eFedora.modifyObjectState(editionDomsID, "I", "comment");
            try {
                eFedora.deleteRelation(editionDomsID, URI_PREFIX + editionDomsID, editionToNewspaperRelation,
                                       URI_PREFIX + title.getDomsID(), false, "linking to");
            } finally {
                // Re-publish (set to "A" (active))
                eFedora.modifyObjectState(editionDomsID, "A", "comment");
            }
        }
    }

    /**
     * Remove the "info:fedora/" prefix from uri, making it a proper DOMS PID (starting with "uuid")
     *
     * @param uri The uri from which to remove prefix
     * @return The DOMS PID
     */
    private String uriToDomsID(String uri) {
        return uri.replace(URI_PREFIX, "");
    }
}
