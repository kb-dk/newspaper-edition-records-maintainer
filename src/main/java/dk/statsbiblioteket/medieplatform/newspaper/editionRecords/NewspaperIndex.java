package dk.statsbiblioteket.medieplatform.newspaper.editionRecords;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;

import java.util.ArrayList;
import java.util.List;

public class NewspaperIndex {

    private static final String ITEM_MODEL_FIELD_NAME = "item_model";
    private static final String ITEM_MODEL_FIELD_VALUE = "\"doms:ContentModel_Newspaper\"";
    private static final String AVIS_ID_FIELD_NAME = "newspapr_title_avisID";
    private static final String START_DATE_FIELD_NAME = "newspapr_title_startDate";
    private static final String END_DATE_FIELD_NAME = "newspapr_title_endDate";
    private final SolrServer solrServer;
    private final ItemFactory itemFactory;

    public NewspaperIndex(SolrServer solrServer, ItemFactory itemFactory) {
        this.solrServer = solrServer;
        this.itemFactory = itemFactory;
    }

    /**
     * Get all newspaper titles matching the given avisID and date
     *
     * @param avisID The avisID the title should match.
     * @param date The date the title should match.
     * @return List of matching newspaper titles.
     */
    public List<Item> getNewspapers(String avisID, String date) {
        try {
            SolrQuery query = new SolrQuery();
            query.setQuery(String.format(ITEM_MODEL_FIELD_NAME + ":" + ITEM_MODEL_FIELD_VALUE
                                                 + " AND "
                                                 + AVIS_ID_FIELD_NAME + ":\"%s\""
                                                 + " AND "
                                                 + START_DATE_FIELD_NAME + ":\"[* TO %s]\""
                                                 + " AND "
                                                 +  END_DATE_FIELD_NAME + ":\"[%s TO *]\"", avisID, date, date));
            query.setRows(Integer.MAX_VALUE); //Fetch size. Do not go over 1000 unless you specify fields to fetch which does not include content_text
            query.setStart(0);
            //IMPORTANT!Only use facets if needed.
            query.set("facet", "false"); //very important. Must overwrite to false. Facets are very slow and expensive.
            query.setFields(SBOIEventIndex.UUID);
            QueryResponse response = solrServer.query(query);
            SolrDocumentList results = response.getResults();
            List<Item> hits = new ArrayList<>();
            for (SolrDocument result : results) {
                String uuid = result.getFirstValue(SBOIEventIndex.UUID).toString();
                Item hit = itemFactory.create(uuid);
                hits.add(hit);
            }
            return hits;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
