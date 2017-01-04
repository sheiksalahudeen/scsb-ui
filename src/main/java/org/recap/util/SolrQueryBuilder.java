package org.recap.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.recap.RecapConstants;
import org.recap.model.jpa.MatchingMatchPointsEntity;
import org.recap.model.search.SearchRecordsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by peris on 9/30/16.
 */

@Component
public class SolrQueryBuilder {

    String and = " AND ";

    public SolrQuery buildSolrQueryForAccessionReports(String date, String owningInstitution, boolean isDeleted, String collectionGroupDesignation) {
        StringBuilder query = new StringBuilder();
        query.append("DocType:Item").append(and);
        query.append("ItemCreatedDate:").append("[").append(date).append("]").append(and);
        query.append("IsDeletedItem:").append(isDeleted).append(and);
        query.append("ItemOwningInstitution:").append(owningInstitution).append(and);
        query.append("CollectionGroupDesignation:").append(collectionGroupDesignation);
        return new SolrQuery(query.toString());
    }

    public SolrQuery buildSolrQueryForDeaccessionReports(String date, String owningInstitution, boolean isDeleted, String collectionGroupDesignation) {
        StringBuilder query = new StringBuilder();
        query.append("DocType:Item").append(and);
        query.append("ItemLastUpdatedDate:").append("[").append(date).append("]").append(and);
        query.append("IsDeletedItem:").append(isDeleted).append(and);
        query.append("ItemOwningInstitution:").append(owningInstitution).append(and);
        query.append("CollectionGroupDesignation:").append(collectionGroupDesignation);
        return new SolrQuery(query.toString());
    }


    public SolrQuery buildSolrQueryForCGDReports(String owningInstitution , String collectionGroupDesignation){
        StringBuilder query = new StringBuilder();
        query.append("DocType:Item").append(and);
        query.append("ItemOwningInstitution:").append(owningInstitution).append(and);
        query.append("CollectionGroupDesignation:").append(collectionGroupDesignation).append(and);
        query.append("IsDeletedItem:false");
        return new SolrQuery(query.toString());
    }

    public SolrQuery buildSolrQueryForDeaccesionReportInformation(String date, String owningInstitution, boolean isDeleted) {
        StringBuilder query = new StringBuilder();
        query.append("DocType:Item").append(and);
        query.append("ItemLastUpdatedDate:").append("[").append(date).append("]").append(and);
        query.append("IsDeletedItem:").append(isDeleted).append(and);
        query.append("ItemOwningInstitution:").append(owningInstitution);
        return new SolrQuery(query.toString());
    }


}
