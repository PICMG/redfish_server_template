package org.picmg.redfish_server_template.repository;

import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishCollection;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class RedfishObjectRepository {
    long etagTime = System.currentTimeMillis();
    long etagCount = 0;
    final Object etagObj = new Object();
    @Autowired
    MongoTemplate mongoTemplate;


    // this function creates a new etag string based on the current time and number of
    // etags already created during the current millisecond.
    public String calcEtag() {
        synchronized (etagObj) {
            if (System.currentTimeMillis()!=etagTime) {
                etagCount = 0;
            }
            etagTime = System.currentTimeMillis();
            return String.format("%1$016X%2$016X",etagTime,etagCount++);
        }
    }

    // update the collection etag if a member object because a member object has been altered
    private void updateCollectionEtag(String uri) {
        // get the uri of the containing object
        String collectionUri = uri.substring(0,uri.lastIndexOf('/'));

        // attempt to find the object in the repository
        Query query = new Query();
        query.addCriteria(Criteria.where("_odata_id").is(collectionUri).and("_odata_type").regex(".*Collection$"));
        Update update = new Update().set("_odata_etag",calcEtag());
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(false);
        mongoTemplate.findAndModify(query, update, options, Document.class, "RedfishObject");
    }

    public Document findAndUpdate(Criteria criteria, Update update) {
        // attempt to find the object in the repository
        Query query = new Query();
        query.addCriteria(criteria);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(false);
        return mongoTemplate.findAndModify(query, update, options, Document.class, "RedfishObject");
    }

    public void save(RedfishObject obj ) {
        if (!obj.containsKey("@odata.id")) {
            return;
        }
        if (!obj.containsKey("@odata.type")) {
            return;
        }
        if (!obj.containsKey("_odata_id")) obj.put("_odata_id",obj.getString("@odata.id"));
        if (!obj.containsKey("_odata_type")) {
            String type = obj.getString("@odata.type");
            if (type.contains(".")) {
                type = type.substring(type.lastIndexOf(".")+1);
            }
            obj.put("_odata_type",type);
        }

        // update the collection etag (if any)
        updateCollectionEtag(obj.getString("_odata_id"));

        // update/add the edata annotation field
        obj.put("_odata_etag",calcEtag());
        mongoTemplate.save(obj,"RedfishObject");
    }

    public void update(RedfishObject obj ) {
        Query query = new Query();
        Criteria ctr = Criteria.where("_id").is(obj.get("_id"));
        // for updates, the document to write cannot include an _id field
        obj.remove("_id");
        query.addCriteria(ctr);

        // update the collection etag (if any)
        updateCollectionEtag(obj.getString("_odata_id"));

        // update/add the edata annotation field
        obj.put("_odata_etag",calcEtag());

        mongoTemplate.findAndReplace(query,obj,"RedfishObject");
    }

    public List<RedfishObject> findWithQuery(CriteriaDefinition defs) {
        Query query = new Query();
        query.addCriteria(defs);
        List<Document> listDoc = mongoTemplate.find(query, Document.class, "RedfishObject");
        List<RedfishObject> result = new ArrayList<>();
        for (Document doc: listDoc) {
            if (doc.containsKey("Members@odata.count")) {
                RedfishCollection obj = new RedfishCollection();
                obj.putAll(doc);
                // attempt to add members and update the member id
                if ((obj.containsKey("_odata_id")) && (obj.containsKey("_odata_type"))) {
                    // get the type of object
                    String type = obj.getString("_odata_type");
                    if (type.contains(".")) type = type.substring(type.lastIndexOf(".")+1);
                    type = type.replace("Collection","");

                    // get a list of all objects that are of the same type as this collection
                    List<RedfishObject> members = findWithQuery(Criteria.where("_odata_type").is(type)
                            .and("_odata_id").regex("^"+obj.getString("_odata_id")+"/[^/]+$"));
                    List<Document> membersDoc = new ArrayList<>();
                    for (RedfishObject member: members) {
                        membersDoc.add(Document.parse("{\"@odata.id\":\""+member.getAtOdataId()+"\"}"));
                    }
                    obj.setMembers(membersDoc);
                }
                result.add(obj);
            } else {
                RedfishObject obj = new RedfishObject();
                obj.putAll(doc);
                result.add(obj);
            }
        }
        return result;
    }
    public RedfishObject findFirstWithQuery(CriteriaDefinition defs) {
        Query query = new Query();
        query.addCriteria(defs);
        Document obj = mongoTemplate.findOne(query, Document.class, "RedfishObject");
        if (obj==null) return null;
        if (obj.containsKey("Members@odata.count")) {
            RedfishCollection result = new RedfishCollection();
            result.putAll(obj);
            if ((result.containsKey("_odata_id")) && (result.containsKey("_odata_type"))) {
                // get the type of object
                String type = result.getString("_odata_type");
                if (type.contains(".")) type = type.substring(type.lastIndexOf(".")+1);
                type = type.replace("Collection","");

                // get a list of all objects that are of the same type as this collection
                List<RedfishObject> members = findWithQuery(Criteria.where("_odata_type").is(type)
                        .and("_odata_id").regex("^"+obj.getString("_odata_id")+"/[^/]+$"));
                List<Document> membersDoc = new ArrayList<>();
                for (RedfishObject member: members) {
                    membersDoc.add(Document.parse("{\"@odata.id\":\""+member.getAtOdataId()+"\"}"));
                }
                result.setMembers(membersDoc);
            }

            return result;
        }
        RedfishObject result = new RedfishObject();
        result.putAll(obj);
        return result;
    }

    public void delete(RedfishObject obj) {
        // update the collection etag (if any)
        updateCollectionEtag(obj.getAtOdataId());

        Query query = new Query();
        query.addCriteria(Criteria.where("_odata_id").is(obj.getAtOdataId()));
        mongoTemplate.findAllAndRemove(query,"RedfishObject");
    }

    public void deleteWithQuery(CriteriaDefinition criteria) {
        Query query = new Query();
        query.addCriteria(criteria);
        List<Document> listDoc = mongoTemplate.findAllAndRemove(query,"RedfishObject");
        for (Document doc: listDoc) {
            if (doc.containsKey("@odata.id")) {
                updateCollectionEtag(doc.getString("@odata.id"));
            }
        }
    }

    public void insert(RedfishObject obj) {
        // update the collection etag (if any)
        updateCollectionEtag(obj.getAtOdataId());

        mongoTemplate.insert(obj, "RedfishDB");
    }

    public List<String> getDistinctWithQuery(String collection, CriteriaDefinition criteria, String property) {
        Query query = new Query();
        query.addCriteria(criteria);
        return  mongoTemplate.findDistinct(query, property, collection, String.class);
    }
}
