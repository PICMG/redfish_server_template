package org.picmg.redfish_server_template.repository;

import org.bson.Document;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishCollection;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class RedfishObjectRepository {
    @Autowired
    MongoTemplate mongoTemplate;

    public void save(RedfishObject obj ) {
        mongoTemplate.save(obj,"RedfishObject");
    }

    public void update(RedfishObject obj ) {
        Query query = new Query();
        Criteria ctr = Criteria.where("_id").is(obj.get("_id"));
        // for updates, the document to write cannot include an _id field
        obj.remove("_id");
        query.addCriteria(ctr);
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
            return result;
        }
        RedfishObject result = new RedfishObject();
        result.putAll(obj);
        return result;
    }

    public void delete(RedfishObject obj) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_odata_id").is(obj.getAtOdataId()));
        mongoTemplate.findAllAndRemove(query,"RedfishObject");
    }

    public void insert(RedfishObject obj) {
        mongoTemplate.insert(obj, "RedfishDB");
    }
}
