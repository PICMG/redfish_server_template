//******************************************************************************************************
// EventService.java
//
// Event service according to redfish specification.
//
//Copyright (C) 2022, PICMG.
//
//        This program is free software: you can redistribute it and/or modify
//        it under the terms of the GNU General Public License as published by
//        the Free Software Foundation, either version 3 of the License, or
//        (at your option) any later version.
//
//        This program is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU General Public License for more details.
//
//        You should have received a copy of the GNU General Public License
//        along with this program.  If not, see <https://www.gnu.org/licenses/>.
//*******************************************************************************************************


package org.picmg.redfish_server_template.services;

import org.bson.Document;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.repository.MessageRegistryRepository;
import org.picmg.redfish_server_template.repository.RedfishObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {

    @Value("${smtp-service.username}")
    String smtpUsername;

    @Value("${smtp-service.password}")
    String smtpPassword;

    @Value("${async.task.wait-time}")
    long taskWaitTime;

    long eventGroupId = 0;

    @Autowired
    RedfishObjectRepository objectRepository;

    @Autowired
    MessageRegistryRepository messageRegistryRepository;

    @Autowired
    APIServices apiServices;

    @Autowired
    TaskService taskService;

    // A list of all SSE emitters
    private TreeMap<String, SseEmitter> emitterMap = new TreeMap<>();

    // A list of all event destination descriptions
    private TreeMap<String, RedfishObject> eventDestinations = new TreeMap();

    // cached service filter capabilitis
    private List<String> eventFormatTypes = new ArrayList<>();
    private List<String> registryPrefixes = new ArrayList<>();
    private List<String> severities = new ArrayList<>();
    private List<String> resourceTypes = new ArrayList<>();

    private List<String> messageIds = new ArrayList<>();

    public boolean isEventFormatTypeSupported(String formatType) {
        return eventFormatTypes.contains(formatType);
    }

    public boolean isRegistryPrefixSupported(String prefix) {
        return registryPrefixes.contains(prefix);
    }

    public boolean isSeveritySupported(String severity) {
        return severities.contains(severity);
    }

    public boolean isResourceTypeSupported(String resourceType) {
        return resourceTypes.contains(resourceType);
    }

    public boolean isMessageIdValid(String id) {return messageIds.contains(id);}

    private List<String> allowedFilters = List.of(
            "MessageId",
            "OriginResource",
            "RegistryPrefix",
            "ResourceType",
            "SubordinateResources"
    );

    private TreeMap<String, ArrayList<String>> processFilterNot(TreeMap<String,ArrayList<String>> valueMap) {
        // first see if the variable name is allowed as a filter
        if (valueMap.size()!=1) return null;  // negation only defined for single filters
        if (!allowedFilters.contains(valueMap.firstKey())) return null;

        // now process the negation based on the filter name
        ArrayList<String> resultList;
        switch (valueMap.firstKey()) {
            case "MessageId":
                resultList = new ArrayList<>(messageIds);
                for (String filterValue: valueMap.get(valueMap.firstKey())) {
                    resultList.remove(filterValue);
                }
                return new TreeMap<String, ArrayList<String>>(Collections.singletonMap(valueMap.firstKey(), resultList));
            case "OriginResource":
                // This is an error.  There is no valid negation for this term
                return null;
            case "RegistryPrefix":
                resultList = new ArrayList<>(registryPrefixes);
                for (String filterValue: valueMap.get(valueMap.firstKey())) {
                    resultList.remove(filterValue);
                }
                return new TreeMap<String, ArrayList<String>>(Collections.singletonMap(valueMap.firstKey(), resultList));
            case "ResourceType":
                resultList = new ArrayList<>(resourceTypes);
                for (String filterValue: valueMap.get(valueMap.firstKey())) {
                    resultList.remove(filterValue);
                }
                return new TreeMap<String, ArrayList<String>>(Collections.singletonMap(valueMap.firstKey(), resultList));
            case "SubordinateResources":
                // this is an error - there is no negation for this term
                return null;
        }
        return null;
    }

    private TreeMap<String, ArrayList<String>> processFilterNe(String variableName, String value) {
        // first see if the variable name is allowed as a filter
        if (!allowedFilters.contains(variableName)) return null;
        if (value.equals("%")) return null;

        // now process the inequality based on the variable name
        ArrayList<String> resultList;
        switch (variableName) {
            case "MessageId":
                String id_no_revision = value.substring(0, value.indexOf('.')) +
                        value.substring(value.lastIndexOf('.'));
                resultList = new ArrayList<>(messageIds);
                resultList.remove(id_no_revision);
                return new TreeMap<String, ArrayList<String>>(Collections.singletonMap(variableName, resultList));
            case "OriginResource":
                // This is an error.  There is no valid negation for this term
                return null;
            case "RegistryPrefix":
                resultList = new ArrayList<>(registryPrefixes);
                resultList.remove(value);
                return new TreeMap<String, ArrayList<String>>(Collections.singletonMap(variableName, resultList));
            case "ResourceType":
                resultList = new ArrayList<>(resourceTypes);
                resultList.remove(value);
                return new TreeMap<String, ArrayList<String>>(Collections.singletonMap(variableName, resultList));
            case "SubordinateResources":
                // this is an error - there is no negation for this term
                return null;
        }
        return null;
    }

    private TreeMap<String, ArrayList<String>> processFilterEq(String variableName, String value) {
        // first see if the variable name is allowed as a filter
        if (!allowedFilters.contains(variableName)) return null;
        if (value.equals("%")) return null;

        // now process the inequality based on the variable name
        ArrayList<String> resultList;
        switch (variableName) {
            case "MessageId":
                String id_no_revision = value.substring(0, value.indexOf('.')) +
                        value.substring(value.lastIndexOf('.'));
                resultList = new ArrayList<>(List.of(id_no_revision));
                return new TreeMap<String, ArrayList<String>>(Collections.singletonMap(variableName, resultList));
            case "OriginResource":
            case "RegistryPrefix":
            case "ResourceType":
            case "SubordinateResources":
                resultList = new ArrayList<>(List.of(value));
                return new TreeMap<String, ArrayList<String>>(Collections.singletonMap(variableName, resultList));
        }
        return null;
    }

    private TreeMap<String, ArrayList<String>> processFilterAnd(TreeMap<String,ArrayList<String>> map1, TreeMap<String,ArrayList<String>> map2) {
        TreeMap<String, ArrayList<String>> result = new TreeMap<>(map2);
        for (String key: map1.keySet()) {
            if (result.containsKey(key)) {
                // perform the intersection of the two maps
                for (String val: map2.get(key)) {
                    if (!map1.get(key).contains(val)) result.remove(val);
                }
            } else {
                result.put(key, map1.get(key));
            }
        }
        return result;
    }

    private TreeMap<String, ArrayList<String>> processFilterOr(TreeMap<String,ArrayList<String>> map1, TreeMap<String,ArrayList<String>> map2) {
        TreeMap<String, ArrayList<String>> result = new TreeMap<>(map2);
        for (String key: map1.keySet()) {
            if (result.containsKey(key)) {
                // perform the union of the two maps
                result.get(key).addAll(map1.get(key));
            } else {
                result.put(key, map1.get(key));
            }
        }
        return result;
    }

    // return the position of the closing parenthesis for the first open parenthesis found
    // if no parentheis is found, return -1;
    private int findClosingParenthesis(String filterString) {
        int depth = 0;
        for (int strpos = filterString.indexOf('('); strpos < filterString.length(); strpos++) {
            switch(filterString.charAt(strpos)) {
                case '(':
                    depth ++;
                    break;
                case ')':
                    depth --;
                    if (depth==0) return strpos;
                    break;
            }
        }
        return -1;
    }
    private TreeMap<String, ArrayList<String>> sseFilterHelper(String filterString) {
        ArrayList<TreeMap<String,ArrayList<String>>> subResults = new ArrayList<>();
        while (filterString.indexOf('(')>=0) {
            // walk through the string until a parenthesis is found
            int firstOpenParenthesis = filterString.indexOf('(');
            int matchingCloseParentheisis = findClosingParenthesis(filterString);
            if (matchingCloseParentheisis<0) return null;

            // evaluate the sub-expression
            TreeMap<String, ArrayList<String>> subExpression = sseFilterHelper(
                    filterString.substring(firstOpenParenthesis+1,matchingCloseParentheisis-1));
            if (subExpression == null) return null;
            subResults.add(subExpression);

            // update the string and keep processing
            filterString = filterString.substring(0,firstOpenParenthesis-1) +
                    " % " + filterString.substring(matchingCloseParentheisis+1);
        }
        // remove duplicate spaces from the filter string
        filterString = filterString.trim().replaceAll(" +"," ");

        // at this point, the string should consist of a sequence of operators (eq, ne, not, and, or) with
        // either parameter names or tokens between them.  There should be no parenthesis
        // example X eq Y or not % and X ne Z
        // parse into a list of tokens/operators
        ArrayList<String> operations = new ArrayList<String>(Arrays.asList(filterString.split(" ")));

        // process any negation operations
        while (operations.contains("not")) {
            int notIndex = operations.indexOf("not");
            if (notIndex>=operations.size()) return null;

            // count the number of tokens in the operations list before this token
            int tokenCount = 0;
            for (int i = 0; i<notIndex-1; i++) {
                if (operations.get(i).equals("%")) tokenCount++;
            }

            TreeMap<String, ArrayList<String>> subExpression =
                    processFilterNot(subResults.get(notIndex+1));
            if (subExpression == null) return null;

            // remove the not operation from the operations list
            operations.remove(notIndex);

            // replace the existing token with the negated one
            subResults.set(tokenCount+1,subExpression);
        }

        // process and ne operations
        while (operations.contains("ne")) {
            int neIndex = operations.indexOf("ne");
            if ((neIndex==0) || (neIndex>=operations.size())) return null;
            TreeMap<String, ArrayList<String>> subExpression =
                    processFilterNe(operations.get(neIndex-1),operations.get(neIndex+1));
            if (subExpression == null) return null;

            // remove the ne operation and its operands from the operations list
            operations.remove(neIndex-1);
            operations.remove(neIndex-1);
            operations.remove(neIndex-1);

            // add a new partial result placeholder in the operations list
            operations.add(neIndex-1,"%");
            // count the number of tokens in the operations list before this token
            int tokenCount = 0;
            for (int i = 0; i<neIndex-1; i++) {
                if (operations.get(i).equals("%")) tokenCount++;
            }
            subResults.add(tokenCount,subExpression);
        }

        // process any eq operations
        while (operations.contains("eq")) {
            int eqIndex = operations.indexOf("eq");
            if ((eqIndex==0) || (eqIndex>=operations.size())) return null;
            TreeMap<String, ArrayList<String>> subExpression =
                    processFilterEq(operations.get(eqIndex-1),operations.get(eqIndex+1));
            if (subExpression == null) return null;

            // remove the eq operation and its operands from the operations list
            operations.remove(eqIndex-1);
            operations.remove(eqIndex-1);
            operations.remove(eqIndex-1);

            // add a new partial result placeholder in the operations list
            operations.add(eqIndex-1,"%");
            // count the number of tokens in the operations list before this token
            int tokenCount = 0;
            for (int i = 0; i < eqIndex-1; i++) {
                if (operations.get(i).equals("%")) tokenCount++;
            }
            subResults.add(tokenCount,subExpression);
        }

        // process any and (intersection operations)
        while (operations.contains("and")) {
            int andIndex = operations.indexOf("and");
            if ((andIndex==0) || (andIndex >= operations.size())) return null;

            // count the number of tokens in the operations list before this token
            int tokenCount = 0;
            for (int i = 0; i < andIndex-1; i++) {
                if (operations.get(i).equals("%")) tokenCount++;
            }

            TreeMap<String, ArrayList<String>> subExpression =
                    processFilterAnd(subResults.get(tokenCount),subResults.get(tokenCount+1));
            if (subExpression == null) return null;

            // remove the eq operation and its operands from the operations list
            operations.remove(andIndex-1);
            operations.remove(andIndex-1);
            operations.remove(andIndex-1);

            // add a new partial result placeholder in the operations list
            operations.add(andIndex-1,"%");
            subResults.remove(tokenCount);
            subResults.set(tokenCount,subExpression);
        }

        // process any or (union operations)
        while (operations.contains("or")) {
            int orIndex = operations.indexOf("or");
            if ((orIndex==0) || (orIndex >= operations.size())) return null;

            // count the number of tokens in the operations list before this token
            int tokenCount = 0;
            for (int i = 0; i < orIndex-1; i++) {
                if (operations.get(i).equals("%")) tokenCount++;
            }

            TreeMap<String, ArrayList<String>> subExpression =
                    processFilterOr(subResults.get(tokenCount),subResults.get(tokenCount+1));
            if (subExpression == null) return null;

            // remove the eq operation and its operands from the operations list
            operations.remove(orIndex-1);
            operations.remove(orIndex-1);
            operations.remove(orIndex-1);

            // add a new partial result placeholder in the operations list
            operations.add(orIndex-1,"%");
            subResults.remove(tokenCount);
            subResults.set(tokenCount,subExpression);
        }
        // at this point there should be only one subResult left.  return that
        if (subResults.size()!=1) return null;
        return subResults.get(0);
    }

    // Given a filter string, evaluate the meaning
    // The result will be a collection of filters where
    // each entry in the map are the allowed values for the particular variable to have.
    public TreeMap<String,ArrayList<String>> evaluateSSEFilter(String filterString) {
        // remove any single quotes from the filter string
        filterString = filterString.replace("'","");

        return sseFilterHelper(filterString);
    }

    @PostConstruct
    private void initializeService() {
        // TODO: this should only remove sse destinations
        objectRepository.deleteWithQuery(Criteria.where("_odata_type").is("EventDestination"));

        // add fields to describe the capabilities of this service.
        RedfishObject obj = objectRepository.findFirstWithQuery(Criteria.where("_odata_type").is("EventService"));

        // add location of sse subscription URI
        obj.put("ServerSentEventUri", "/redfish/v1/EventService/SSE");

        // add SSE filter properties supported
        obj.put("SSEFilterPropertiesSupported", Map.of(
                "EventFormatType", true,
                "EventType", false,
                "MessageId", true,
                "MetricReportDefinition", false,
                "OriginResource", true,
                "RegistryPrefix", true,
                "ResourceType", true,
                "SubordinateResources", true));

        // fix the actions for this resource
        obj.put("Actions", Map.of(
                "#EventService.SubmitTestEvent", Map.of(
                        "target", "/redfish/v1/EventService/Actions/EventService.SubmitTestEvent"),
                "#EventService.TestEventSubscription", Map.of(
                        "target", "/redfish/v1/EventService/Actions/EventService.TestEventSubscription")
                        ));

        eventFormatTypes = List.of("Event");
        obj.put("EventFormatType", eventFormatTypes);
        obj.put("ExcludeMessageId", true);
        obj.put("ExcludeRegistryPrefix", true);
        obj.put("IncludeOriginOfConditionSupported", true);
        obj.put("SubordinateResourcesSupported", true);

        // initialize registry prefixes from all registry prefixes found in the message registry database
        registryPrefixes = objectRepository.getDistinctStringsWithQuery(
                "MessageRegistry",
                Criteria.where("RegistryPrefix").exists(true),
                "RegistryPrefix");
        obj.put("RegistryPrefixes",registryPrefixes);

        // initialize the messageIds that are supported
        for (String registry : registryPrefixes) {
            messageIds.addAll(messageRegistryRepository.getMessagesInRegistry(registry));
        }
        // add the test message if it does not exist in the repository
        if (!messageIds.contains("ResourceEvent.TestMessage")) messageIds.add("ResourceEvent.TestMessage");

        // initialize severities that are supported
        severities = List.of("OK", "Warning", "Critical");
        obj.put("Severities",severities);

        // get a list of resource types that are supported
        List<String> rawResourceTypes = objectRepository.getDistinctStringsWithQuery(
                "json_schema",
                Criteria.where("source").exists(true),
                "source");

        // filter the list to remove versions and duplicates
        resourceTypes = rawResourceTypes
                .stream()
                .filter( name -> name.charAt(0) <= 'Z')
                .map(name -> name.substring(0,name.indexOf('.')))
                .distinct()
                .collect(Collectors.toList());
        obj.put("ResourceTypes", resourceTypes);
        objectRepository.update(obj);
    }

    /*
    @Scheduled(fixedRate=1000)
    public void heartbeat() {
        for (String key : emitterMap.keySet()) {
            // make sure the destination is enabled
            Document doc = objectRepository.findFirstWithQuery(Criteria.where("_odata_id").is(key));
            String state = "Enabled";
            try {
                state = ((HashMap<String,String>)doc.get("State")).get("Status");
            } catch (Exception ignored) {
            }
            if ((state!=null) && (state.equals("Enabled"))) {
                try {
                    String data = "Heartbeat";
                    emitterMap.get(key).send(SseEmitter.event().id(UUID.randomUUID().toString()).data(data));
                } catch (Exception e) {
                    System.out.println("heartbeat failed");
                }
            }
        }
    }
    */

    private boolean filtersMatch(Document destination, HashMap<String,Object> eventRecord) {
        // filter on messageid/registry prefixes
        HashMap<String,String> destExcludeMessageIds=null;
        if (destination.containsKey("ExcludeMessageIds"))
            destExcludeMessageIds = destination.get("ExcludeMessageIds",HashMap.class);
        HashMap<String,String> destExcludeRegistryPrefixes=null;
        if (destination.containsKey("ExcludeRegistryPrefixes"))
            destExcludeRegistryPrefixes = destination.get("ExcludeRegistryPrefixes",HashMap.class);
        ArrayList<String> destMessageIds=null;
        if (destination.containsKey("MessageIds"))
            destMessageIds = destination.get("MessageIds",ArrayList.class);
        ArrayList<String> destRegistryPrefixes=null;
        if (destination.containsKey("RegistryPrefixes"))
            destRegistryPrefixes = destination.get("RegistryPrefixes",ArrayList.class);
        if (eventRecord.containsKey("MessageId")) {
            String messageId_nover = eventRecord.get("MessageId").toString();
            messageId_nover = messageId_nover.substring(0,messageId_nover.indexOf('.'))+
                    messageId_nover.substring(messageId_nover.indexOf('.'));
            String registryPrefix = messageId_nover.substring(0,messageId_nover.indexOf('.'));
            if ((destExcludeMessageIds!=null)&&(destExcludeMessageIds.containsKey(messageId_nover))) return false;
            if ((destMessageIds!=null)&&(!destMessageIds.contains(messageId_nover))) return false;
            if ((destExcludeRegistryPrefixes!=null)&&(destExcludeRegistryPrefixes.containsKey(registryPrefix))) return false;
            if ((destRegistryPrefixes!=null)&&(!destRegistryPrefixes.contains(registryPrefix))) return false;
        }

        // filter on the origin of the condition
        Boolean destIncludeOriginOfCondition = destination.getBoolean("IncludeOriginOfCondition", false);
        Boolean destSubordinateResources = destination.getBoolean("SubordinateResources", false);
        ArrayList<String> destOriginResources=null;
        if (destination.containsKey("OriginResources"))
            destOriginResources = destination.get("OriginResources",ArrayList.class);
        if ((destIncludeOriginOfCondition)&&(destOriginResources!=null)&&(eventRecord.containsKey("OriginOfCondition"))) {
            String eventOrigin = eventRecord.get("OriginOfCondition").toString();
            boolean originMatch = false;
            for (String allowedOrigin: destOriginResources) {
                if (destSubordinateResources) {
                    // match for urls that include subordinate resources
                    if (allowedOrigin.startsWith(eventOrigin)) {
                        originMatch = true;
                        break;
                    }
                } else if (allowedOrigin.equals(eventOrigin)) {
                    // exact match
                    originMatch = true;
                    break;
                }
            }
            if (!originMatch) return false;
        }

        // filter on resource types
        ArrayList<String> destResourceTypes=null;
        if (destination.containsKey("ResourceTypes"))
            destResourceTypes = destination.get("ResourceTypes",ArrayList.class);
        if ((destResourceTypes!=null)&&(eventRecord.containsKey("OriginOfCondition"))) {
            // attempt to get the resource type for the origin of the condition
            RedfishObject source = objectRepository.findFirstWithQuery(
                    Criteria.where("_odata_id").is(eventRecord.get("OriginOfCondition").toString()));
            if ((source!=null)&&(source.containsKey("_odata_type"))) {
                String resType = source.get("_odata_type").toString();
                if (!destResourceTypes.contains(resType)) return false;
            }
        }


        ArrayList<String> destSeverities=null;
        if (destination.containsKey("Severities"))
            destSeverities = destination.get("Severities",ArrayList.class);
        if ((destSeverities!=null)&&(eventRecord.containsKey("MessageSeverity"))) {
            if (!destSeverities.contains(eventRecord.get("MessageSeverity").toString())) {
                return false;
            }
        }
        return true;
    }

    public void sendEvent(RedfishObject event) {
        for (String key : eventDestinations.keySet()) {
            // make sure the destination is enabled
            Document eventDestination = objectRepository.findFirstWithQuery(Criteria.where("_odata_id").is(key));
            String state = "Enabled";
            try {
                state = ((HashMap<String,String>)eventDestination.get("State")).get("Status");
            } catch (Exception ignored) {
            }

            if ((state!=null) && (state.equals("Enabled"))) {
                for (HashMap<String,Object> eventRecord : (ArrayList<HashMap<String,Object>>)event.get("Events")) {
                    if (!filtersMatch(eventDestination, eventRecord)) continue;
                    if (emitterMap.containsKey(key)) {
                        // Send to SSE destination
                        try {
                            String data = event.toJson();
                            emitterMap.get(key).send(SseEmitter.event().id(event.getId()).data(data));
                        } catch (Exception e) {
                        }
                    } else {
                        // send to Redfish destination

                    }
                }
            }
        }
    }

    public void addSubscription(RedfishObject eventDestination, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter
                    .event()
                    .comment("The SSE stream has been opened")
            );
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        emitter.onCompletion(() -> {
            String subscriptionId = null;
            for (String key : emitterMap.keySet()) {
                if (emitterMap.get(key).equals(emitter)) {
                    subscriptionId = key;
                    break;
                }
            }
            if (subscriptionId!=null) {
                removeSubscription(subscriptionId);
                objectRepository.deleteWithQuery(Criteria.where("_odata_id").is(subscriptionId));
            }
        });
        emitter.onError((Throwable ignored) -> {
            String subscriptionId = null;
            for (String key : emitterMap.keySet()) {
                if (emitterMap.get(key).equals(emitter)) {
                    subscriptionId = key;
                    break;
                }
            }
            if (subscriptionId!=null) {
                removeSubscription(subscriptionId);
                objectRepository.deleteWithQuery(Criteria.where("_odata_id").is(subscriptionId));
            }
        });
        emitterMap.put(eventDestination.getAtOdataId(),emitter);
        eventDestinations.put(eventDestination.getAtOdataId(),eventDestination);
    }

    public void removeSubscription(String odataId) {
        // TODO: put a lock on these resources?
        emitterMap.remove(odataId);
        eventDestinations.remove(odataId);
    }

    public void closeSubscription(String odataId) {
        if (emitterMap.containsKey(odataId)) {
            // once the subscription closes, it will also be removed from the maps.
            emitterMap.get(odataId).complete();
        } else {
            // here if the subscription was not SSE - just remove it from the maps
            removeSubscription(odataId);
        }
    }


    public void suspendSubscription(String eventUri) {
        // change the state of the subscription to "Disabled" if the subscription exists
        objectRepository.findAndUpdate(Criteria.where("_odata_id").is(eventUri),new Update().set("Status.State","Disabled"));
    }

    public void resumeSubscription(String eventUri) {
        objectRepository.findAndUpdate(Criteria.where("_odata_id").is(eventUri),new Update().set("Status.State","Enabled"));
    }


    public RedfishObject createEvent(String messageId, List<String> args, String msgSeverity, String severity, String origin) {
        RedfishObject obj = new RedfishObject();

        String msgId_noversion = messageId.substring(0,messageId.indexOf('.')) + messageId.substring(messageId.indexOf('.'));
        if (!messageIds.contains(msgId_noversion)) return null;

        obj.setAtOdataType("#Event.v1_10-0.Event");
        obj.setId(UUID.randomUUID().toString());

        obj.setName("Event "+obj.getId());

        HashMap<String,Object> eventRecord = new HashMap<>();
        eventRecord.put("MessageId", messageId);
        eventRecord.put("MessageArgs", Objects.requireNonNullElseGet(args, List::of));
        if (msgSeverity!=null) eventRecord.put("MessageSeverity", msgSeverity);
        if (severity!=null) eventRecord.put("MessageSeverity", msgSeverity);
        if (origin!=null) eventRecord.put("OriginOfCondition", origin);
        eventRecord.put("EventGroupId",eventGroupId++);
        eventRecord.put("EventId",UUID.randomUUID().toString());
        eventRecord.put("EventType","Other");
        eventRecord.put("MemberId",0);
        ArrayList<Object> eventRecords = new ArrayList<>();
        eventRecords.add(eventRecord);
        obj.put("Events",eventRecords);
        return obj;
    }
}