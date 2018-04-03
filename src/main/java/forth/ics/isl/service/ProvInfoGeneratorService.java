/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.isl.service;

import forth.ics.isl.triplestore.RestClient;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.commons.collections.ListUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author rousakis
 */
public class ProvInfoGeneratorService {

    private static final String VREPrefix = "http://139.91.183.70:8090/vre4eic/";
    private static final String CERIFPrefix = "http://eurocris.org/ontology/cerif#";
    private static final String VREClassifications = "http://vre/classifications";
    private static final String VREUsersProvInfo = "http://vre/users";
    //////////
    private String emailString;
    private String personName;
    private String roleString;
    private String orgUnitName;
    private String orgUrl;
    private String eAddressUri;
    private String authorizationToken;
    private String endpoint;
    private String namespace;
    private String importedByUUID;
    private final String personUri;
    private final String orgUnitUri;

    public ProvInfoGeneratorService(String personName, String emailString, String roleString, String orgName, String orgUrl, String endpoint, String namespace, String authorizationToken) throws UnsupportedEncodingException {
        this.personName = personName;
        this.emailString = emailString;
        this.roleString = roleString;
        this.orgUnitName = orgName;
        this.orgUrl = orgUrl;
        this.authorizationToken = authorizationToken;
        this.endpoint = endpoint;
        this.namespace = namespace;
        this.personUri = VREPrefix + "Person." + URLEncoder.encode(emailString, "UTF-8");
        this.orgUnitUri = VREPrefix + "OrgUnit." + URLEncoder.encode(orgUrl, "UTF-8");
        this.eAddressUri = VREPrefix + "EAddress." + URLEncoder.encode(emailString, "UTF-8");
    }

    public List<String> personTriples() {
        List<String> triples = new ArrayList<>();
        triples.add("<" + personUri + "> a <" + CERIFPrefix + "Person>. \n");
        triples.add("<" + personUri + "> <" + CERIFPrefix + "has_name> \"" + personName + "\". \n");
        triples.add("<" + personUri + "> rdfs:label \"" + personName + "\". \n");
        triples.add("<" + eAddressUri + "> a <" + CERIFPrefix + "ElectronicAddress>. \n");
        triples.add("<" + eAddressUri + "> rdfs:label \"" + emailString + "\". \n");
        triples.add("<" + eAddressUri + "> <" + CERIFPrefix + "has_URI> \"" + emailString + "\". \n");
        return triples;
    }

    public List<String> orgTriples() {
        List<String> triples = new ArrayList<>();
        triples.add("<" + orgUnitUri + "> a <" + CERIFPrefix + "OrganisationUnit>. \n");
        triples.add("<" + orgUnitUri + "> <" + CERIFPrefix + "has_name> \"" + orgUnitName + "\". \n");
        triples.add("<" + orgUnitUri + "> rdfs:label \"" + orgUnitName + "\". \n");
        triples.add("<" + orgUnitUri + "> <" + CERIFPrefix + "has_URI> \"" + orgUnitUri + "\". \n");
        return triples;
    }

    public List<String> generateTriples() throws IOException, ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String curDate = format.format(new Date()) + "T00:00:00";
        List<String> triples = new ArrayList<>();
        /////       
        String roleUUID = "urn:uuid:" + UUID.randomUUID().toString();
        triples.add("<" + roleUUID + "> a <" + CERIFPrefix + "SimpleLinkEntity>. \n");
        triples.add("<" + roleUUID + "> rdfs:label \"has role\". \n");
        triples.add("<" + roleUUID + "> <" + CERIFPrefix + "has_source> <" + personUri + ">. \n");
        String roleClassifUri = findClassifFromTerm(roleString);
        triples.add("<" + roleUUID + "> <" + VREPrefix + "has_classification> <" + roleClassifUri + ">. \n");
        /////
        importedByUUID = "urn:uuid:" + UUID.randomUUID().toString();
        triples.add("<" + importedByUUID + "> a <" + CERIFPrefix + "FullLinkEntity>. \n");
        triples.add("<" + importedByUUID + "> rdfs:label \"is imported by\". \n");
        String importClassifUri = findClassifFromRoleExpr("is imported by");
        triples.add("<" + importedByUUID + "> <" + CERIFPrefix + "has_classification> <" + importClassifUri + ">. \n");
        triples.add("<" + importedByUUID + "> <" + CERIFPrefix + "has_destination> <" + personUri + ">. \n");
        triples.add("<" + importedByUUID + "> <" + CERIFPrefix + "has_endDate> \"" + curDate + "\". \n");
        triples.add("<" + importedByUUID + "> <" + CERIFPrefix + "has_startDate> \"" + curDate + "\". \n");
        /////
        String hasElAddressUUID = "urn:uuid:" + UUID.randomUUID().toString();
        triples.add("<" + hasElAddressUUID + "> a <" + CERIFPrefix + "FullLinkEntity>. \n");
        triples.add("<" + hasElAddressUUID + "> rdfs:label \"has electronic address\". \n");
        String emailClassifUri = findClassifFromTerm("Email");
        triples.add("<" + hasElAddressUUID + "> <" + CERIFPrefix + "has_classification> <" + emailClassifUri + ">. \n");
        triples.add("<" + hasElAddressUUID + "> <" + CERIFPrefix + "has_destination> <" + eAddressUri + ">. \n");
        triples.add("<" + hasElAddressUUID + "> <" + CERIFPrefix + "has_source> <" + personUri + ">. \n");
        triples.add("<" + eAddressUri + "> <" + CERIFPrefix + "is_destination_of> <" + hasElAddressUUID + ">. \n");
        //////
        String persOrgUnitUUID = "urn:uuid:" + UUID.randomUUID().toString();
        triples.add("<" + persOrgUnitUUID + "> a <" + CERIFPrefix + "FullLinkEntity>. \n");
        triples.add("<" + persOrgUnitUUID + "> rdfs:label \"is member of\". \n");
        String memberOfClassifUri = findClassifFromRoleExpr("is member of");
        triples.add("<" + persOrgUnitUUID + "> <" + CERIFPrefix + "has_classification> <" + memberOfClassifUri + ">. \n");
        triples.add("<" + persOrgUnitUUID + "> <" + CERIFPrefix + "has_destination> <" + orgUnitUri + ">. \n");
        triples.add("<" + persOrgUnitUUID + "> <" + CERIFPrefix + "has_source> <" + personUri + ">. \n");
        //////
        triples.add("<" + personUri + "> <" + CERIFPrefix + "is_source_of> <" + persOrgUnitUUID + ">. \n");
        triples.add("<" + personUri + "> <" + CERIFPrefix + "is_source_of> <" + roleUUID + ">. \n");
        triples.add("<" + personUri + "> <" + CERIFPrefix + "is_source_of> <" + hasElAddressUUID + ">. \n");
        return triples;
    }

    private String findClassifFromTerm(String term) throws IOException, ParseException {
        String query = "select ?classif from <" + VREClassifications + "> where {\n"
                + "?classif <" + CERIFPrefix + "has_term> ?term. \n"
                + "filter (lcase(?term) = lcase(\"" + term + "\")).\n"
                + "}";
        System.out.println(query);
        RestClient client = new RestClient(endpoint, namespace, authorizationToken);
        Response resp = client.executeSparqlQuery(query, "application/json", 0);
        JSONParser parser = new JSONParser();
        JSONObject result = (JSONObject) parser.parse(resp.readEntity(String.class));
        JSONArray results = (JSONArray) ((JSONObject) result.get("results")).get("bindings");
        String uri = (String) ((JSONObject) ((JSONObject) results.get(0)).get("classif")).get("value");
        return uri;
    }

    private String findClassifFromRoleExpr(String roleExpr) throws IOException, ParseException {
        String query = "select ?classif from <" + VREClassifications + "> where {\n"
                + "  ?classif <" + CERIFPrefix + "has_roleExpression> \"" + roleExpr + "\".\n"
                + "}";
        System.out.println(query);
        RestClient client = new RestClient(endpoint, namespace, authorizationToken);
        Response resp = client.executeSparqlQuery(query, "application/json", 0);
        JSONParser parser = new JSONParser();
        JSONObject result = (JSONObject) parser.parse(resp.readEntity(String.class));
        JSONArray results = (JSONArray) ((JSONObject) result.get("results")).get("bindings");
        String uri = (String) ((JSONObject) ((JSONObject) results.get(0)).get("classif")).get("value");
        return uri;
    }

    public String createProvTriplesInsertQuery(String dstGraph) throws IOException, ParseException {
        List<String> triples = orgTriples();
        triples.addAll(personTriples());
        triples.addAll(generateTriples());
        StringBuilder query = new StringBuilder();
        query.append("insert data {graph <" + dstGraph + "> {\n");
        for (String triple : triples) {
            query.append(triple);
        }
        query.append("} \n}");
        return query.toString();
    }

    public static String CreateInsertQuery(List<String> triples, String dstGraph) {
        StringBuilder query = new StringBuilder();
        query.append("insert data {graph <" + dstGraph + "> {\n");
        for (String triple : triples) {
            query.append(triple);
        }
        query.append("} \n}");
        return query.toString();
    }

    public String createLinkingInsertQuery(String dstGraph) {
        StringBuilder query = new StringBuilder();
        query.append("with <" + dstGraph + "> \n")
                .append("insert {\n")
                .append("?prov_service <" + CERIFPrefix + "is_source_of> <" + importedByUUID + ">. \n")
                .append("} where {\n ")
                .append("?s <" + CERIFPrefix + "is_source_of> ?FLE1.\n")
                .append("?FLE1 <" + CERIFPrefix + "has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n")
                .append("?FLE1 <" + CERIFPrefix + "has_destination> ?ProvService.\n")
                .append("}");
        return query.toString();
    }

    public static void main(String[] args) throws UnsupportedEncodingException, IOException, ParseException {
        String personName = "Vangelis Kritsotakis";
        String emailString = "vkrits@ics.forth.gr";
        String role = "Admin";
        String orgName = "FORTH-ICS-ISL";
        String orgUrl = "https://www.ics.forth.gr/";
        String endpoint = "http://139.91.183.97:8080/EVREMetadataServices-1.0-SNAPSHOT";
        String namespace = "rous";
        String token = "05d7e21c-0fab-453f-857c-d4282fcd4d50";
        ProvInfoGeneratorService info = new ProvInfoGeneratorService(personName, emailString, role, orgName, orgUrl,
                endpoint, namespace, token);

//        System.out.println(info.orgTriples());
//        System.out.println(info.personTriples());
//        System.out.println(info.generateTriples());
//        RestClient client = new RestClient(endpoint, namespace, token);
//        Response resp = client.executeUpdatePOSTJSON(query.toString(), "rous", token);
//        System.out.println(resp.readEntity(String.class));
    }

}