/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.isl.runnable;

import forth.ics.isl.service.DBService;
import forth.ics.isl.triplestore.RestClient;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.ClientErrorException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author rousakis
 */
public class H2Manager {

    Statement statement;
    static Connection connection;

    public H2Manager() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        connection = DriverManager.getConnection("jdbc:h2:~/evre", "sa", "");
        statement = connection.createStatement();
//        st.executeUpdate(
    }

    public H2Manager(Statement statement, Connection connection) {
        this.statement = statement;
        H2Manager.connection = connection;
    }

    public void init() throws SQLException {
        deleteTable("namedgraph_category");
        deleteTable("namedgraph");
        deleteTable("entity");
        deleteTable("relation");
        deleteTable("relations_material");
        deleteTable("user_favorites");

        createTableCategory();
        createTableNamedgraph();
        createTableEntity();
        createTableRelation();
        createTableRelationsMatUpdates();
        createTableUserFavorites();
        insertEntities();
        insertNamedgraphCategories();
        insertNamedgraphs();
        insertRelationsMatUpdates();

    }

    public int deleteTable(String tableName) throws SQLException {
        return statement.executeUpdate("drop table " + tableName + " if exists");
    }

    public int insertNamedGraph(String uri, String name, String description, int category) throws SQLException {
        return statement.executeUpdate("insert into namedgraph values ('" + uri + "','" + name + "','" + description + "'," + category + ")");
    }

    public int insertNamedGraphCategory(String name) throws SQLException {
        return statement.executeUpdate("insert into namedgraph_category (`name`,`description`) values ('" + name + "', '')");
    }

    public int insertEntity(String name, String uri, String thesaurus, String query, String geoQuery, String textGeoQuery, boolean geospatial, String selectionList, String selectionPattern, String keywordSearch, String geoSearch, String varName) throws SQLException {
        return statement.executeUpdate("insert into entity(`name`, `uri`, `thesaurus`, `query`, `geo_query`, `text_geo_query`, `geospatial`, `selection_list`, `selection_pattern`, `keyword_search`, `geo_search`, `var_name`)"
                + " values ('" + name + "','" + uri + "','" + thesaurus + "','" + query + "','" + geoQuery + "', '" + textGeoQuery + "', " + geospatial + ", '" + selectionList + "', '" + selectionPattern + "', '" + keywordSearch + "', '" + geoSearch + "', '" + varName + "')");
    }

    public int insertRelation(String uri, String name, int sourceEntity, int destinationEntity, String graph) throws SQLException {
        return statement.executeUpdate("insert into relation(`uri`, `name`, `source_entity`, `destination_entity`, `graph`)"
                + " values ('" + uri + "','" + name + "', " + sourceEntity + ", " + destinationEntity + ", '" + graph + "')");
    }

    public int insertRelationMatUpdate(String relatedEntities, String update) throws SQLException {
        return statement.executeUpdate("insert into relations_material(`related_entities`, `update`)"
                + " values ('" + relatedEntities + "', '" + update + "')");
    }

    public int updateEntityGeospatial(String entityName, String columnName, boolean columnValue) throws SQLException {
        return statement.executeUpdate("update entity set geospatial = " + columnValue + " where name = '" + entityName + "'");
    }

    public int createTableNamedgraph() throws SQLException {
        return statement.executeUpdate("CREATE TABLE namedgraph ( \n"
                + "uri varchar(30) not null, \n"
                + "name varchar(20), \n"
                + "description clob, \n"
                + "category int, \n"
                + "PRIMARY KEY (`uri`), \n"
                + "FOREIGN KEY (`category`) REFERENCES `namedgraph_category` (`id`) ON DELETE CASCADE"
                + ");");
    }

    public int createTableCategory() throws SQLException {
        return statement.executeUpdate("CREATE TABLE namedgraph_category ( \n"
                + "id int NOT NULL AUTO_INCREMENT, \n"
                + "name varchar(20), "
                + "description clob,\n"
                + "PRIMARY KEY (`id`)\n"
                + ");");
    }

    public ResultSet executeSelectQuery(String query) throws SQLException {
        return statement.executeQuery(query);
    }

    public int executeUpdateQuery(String update) throws SQLException {
        return statement.executeUpdate(update);
    }

    public int createTableEntity() throws SQLException {
        return statement.executeUpdate("CREATE TABLE entity ( \n"
                + "id int NOT NULL AUTO_INCREMENT, \n"
                + "uri clob, \n"
                + "name varchar(30), \n"
                + "query clob, \n"
                + "geo_query clob, \n"
                + "text_geo_query clob, \n"
                + "thesaurus varchar(50), \n"
                + "geospatial boolean, \n"
                + "selection_list clob, \n"
                + "selection_pattern clob, \n"
                + "keyword_search clob,\n"
                + "geo_search clob,\n"
                + "var_name varchar(10),"
                + "PRIMARY KEY (`id`)\n"
                + ");");
    }

    public int createTableRelation() throws SQLException {
        return statement.executeUpdate("CREATE TABLE relation ( \n"
                + "id int NOT NULL AUTO_INCREMENT, \n"
                + "uri clob, \n"
                + "name varchar(30), \n"
                + "source_entity int, \n"
                + "destination_entity int, \n"
                + "graph varchar(30),"
                + "PRIMARY KEY (`id`),\n"
                + "FOREIGN KEY (`source_entity`) REFERENCES `entity` (`id`) ON DELETE CASCADE,"
                + "FOREIGN KEY (`destination_entity`) REFERENCES `entity` (`id`) ON DELETE CASCADE,"
                + "FOREIGN KEY (`graph`) REFERENCES `namedgraph` (`uri`) ON DELETE CASCADE"
                + ");");
    }

    private int createTableRelationsMatUpdates() throws SQLException {
        return statement.executeUpdate("CREATE TABLE relations_material ( \n"
                + "id int NOT NULL AUTO_INCREMENT, \n"
                + "related_entities varchar(50),\n"
                + "update clob,\n"
                + "PRIMARY KEY (`id`)\n"
                + ");");
    }

    public int createTableUserFavorites() throws SQLException {
        return statement.executeUpdate("CREATE TABLE user_favorites ( \n"
                + "id int NOT NULL AUTO_INCREMENT, \n"
                + "username varchar(250),\n"
                + "title varchar(250),\n"
                + "description clob,\n"
                + "query_model clob,\n"
                + "PRIMARY KEY (`id`)\n"
                + ");");
    }

    public void terminate() throws SQLException {
        if (statement != null && !statement.isClosed()) {
            statement.close();
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    //to do: na elegxoume an tha einai matchAllTerms i oxi
    private void insertEntities() throws SQLException {
        insertEntity("Person",
                "http://eurocris.org/ontology/cerif#Person",
                "thesaurus/persons-firstAndLastNames.json",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "select distinct ?persName ?Service (?pers as ?uri) @#$%FROM%$#@\n"
                + "where {\n"
                + "?pers <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.  \n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.  \n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.  \n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?pers a cerif:Person.  \n"
                + "?pers rdfs:label ?persName. \n"
                + "?persName bds:search \"@#$%TERM%$#@\".  \n"
                + "?persName bds:relevance ?score. \n"
                + "}  ORDER BY desc(?score) ?pers \n",
                "",
                "",
                false,
                "distinct (?@#$%VAR%$#@Name as ?name) ?Service (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@  <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.  \n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                //                + "?@#$%VAR%$#@ a cerif:Person.  \n"
                + "?@#$%VAR%$#@ rdfs:label ?@#$%VAR%$#@Name. \n",
                "?@#$%VAR%$#@ rdfs:label ?@#$%VAR%$#@Name. \n"
                + "?@#$%VAR%$#@Name bds:search \"@#$%TERM%$#@\".",
                "",
                "pers"
        );
        insertEntity("Project",
                "http://eurocris.org/ontology/cerif#Project",
                "thesaurus/project-acronyms.json",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "select distinct (?projectTitle as ?title) ?projectAcronym ?Service (?proj as ?uri) @#$%FROM%$#@ \n"
                + "where {\n"
                + "?proj <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?proj a cerif:Project.\n"
                + "?proj cerif:has_title ?projectTitle.\n"
                + "OPTIONAL {?proj cerif:has_URI ?projURI.}\n"
                + "BIND(if(bound(?projURI),?projURI,?proj) as ?projectURI).\n"
                + "?proj cerif:has_acronym ?projectAcronym.\n"
                + "?proj rdfs:label ?projName.\n"
                + "?projName bds:search \"@#$%TERM%$#@\".\n"
                + "?projName bds:relevance ?score \n"
                + "} ORDER BY desc(?score) \n",
                "",
                "",
                false,
                "distinct (?@#$%VAR%$#@Title as ?title) (?@#$%VAR%$#@Name as ?name) (?@#$%VAR%$#@Acronym as ?acronym) ?Service (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                //                + "?@#$%VAR%$#@ a cerif:Project.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_title> ?@#$%VAR%$#@Title.\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_URI> ?@#$%VAR%$#@URI.}\n"
                + "BIND(if(bound(?@#$%VAR%$#@URI),?@#$%VAR%$#@URI,?@#$%VAR%$#@) as ?@#$%VAR%$#@).\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_acronym> ?@#$%VAR%$#@Acronym.\n"
                + "?@#$%VAR%$#@ rdfs:label ?@#$%VAR%$#@Name.\n",
                "?@#$%VAR%$#@ rdfs:label ?@#$%VAR%$#@Name.\n"
                + "?@#$%VAR%$#@Name bds:search \"@#$%TERM%$#@\".",
                "",
                "proj"
        );
        insertEntity("Publication",
                "http://eurocris.org/ontology/cerif#Publication",
                "thesaurus/publications-titles.json",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "select distinct (?pubTitle as ?title) (?pubDate as ?publication_date) ?Service (?pub as ?uri) @#$%FROM%$#@ \n"
                + "where {\n"
                + "?pub <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                + "?pub a <http://eurocris.org/ontology/cerif#Publication>.\n"
                + "?pub <http://eurocris.org/ontology/cerif#has_title> ?pubTitle.\n"
                + "?pub <http://eurocris.org/ontology/cerif#has_publicationDate> ?pubDate.\n"
                + "?pubTitle bds:search \"@#$%TERM%$#@\".\n"
                + "?pubTitle bds:relevance ?score.\n"
                + "}ORDER BY desc(?score) \n",
                "",
                "",
                false,
                "distinct (?@#$%VAR%$#@Title as ?title) (?@#$%VAR%$#@Date as ?publication_date) ?Service (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                //                + "?@#$%VAR%$#@ a cerif:Publication.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_title> ?@#$%VAR%$#@Title.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_publicationDate> ?@#$%VAR%$#@Date.\n",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_title> ?@#$%VAR%$#@Title.\n"
                + "?@#$%VAR%$#@Title bds:search \"@#$%TERM%$#@\".",
                "",
                "pub"
        );
        insertEntity("OrganisationUnit",
                "http://eurocris.org/ontology/cerif#OrganisationUnit",
                "thesaurus/organizationUnits-acronyms.json",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "select distinct (?orgName as ?name) (?orgAcronym as ?acronym) ?Service (?org as ?uri) @#$%FROM%$#@ \n"
                + "where {\n"
                + "?org <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?org a cerif:OrganisationUnit.\n"
                + "?org cerif:has_name ?orgName.\n"
                + "?org cerif:has_acronym ?orgAcronym.\n"
                + "?orgName bds:search \"@#$%TERM%$#@\".\n"
                + "?orgName bds:relevance ?score.\n"
                + "}ORDER BY desc(?score) \n",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#> \n"
                + "select distinct (?orgName as ?name) (?orgAcronym as ?acronym) ?Service (?org as ?uri) ?east ?west ?north ?south @#$%FROM%$#@ \n"
                + "where {\n"
                + "?org <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?org a cerif:OrganisationUnit.\n"
                + "?org cerif:has_name ?orgName.\n"
                + "?org cerif:has_acronym ?orgAcronym.\n"
                + "?org <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "}",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#> \n"
                + "select distinct (?orgName as ?name) (?orgAcronym as ?acronym) ?Service (?org as ?uri) ?east ?west ?north ?south @#$%FROM%$#@ \n"
                + "where {\n"
                + "?org <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?org a cerif:OrganisationUnit.\n"
                + "?org cerif:has_name ?orgName.\n"
                + "?org cerif:has_acronym ?orgAcronym.\n"
                + "?org <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "?orgName bds:search \"@#$%TERM%$#@\".\n"
                + "?orgName bds:relevance ?score.\n"
                + "}ORDER BY desc(?score) \n",
                false,
                "distinct (?@#$%VAR%$#@Name as ?name) (?@#$%VAR%$#@Acronym as ?acronym) ?Service (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                //                + "?@#$%VAR%$#@ a cerif:OrganisationUnit.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Name.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_acronym> ?@#$%VAR%$#@Acronym.",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Name.\n"
                + "?@#$%VAR%$#@Name bds:search \"@#$%TERM%$#@\".",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "org"
        );
        insertEntity("Product",
                "http://eurocris.org/ontology/cerif#Product",
                "",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Product.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?name bds:search \"@#$%TERM%$#@\".\n"
                + "?name bds:relevance ?score.\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ORDER BY desc(?score) \n",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Product.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "}",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Product.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "?name bds:search \"@#$%TERM%$#@\".\n"
                + "?name bds:relevance ?score.\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ORDER BY desc(?score) \n",
                false,
                "distinct ?@#$%VAR%$#@Νame  ?Responsible ?Service (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#Product>.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Νame.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                + "Optional {\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_destination_of> ?FLE3.\n"
                + "?FLE3 <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 <http://eurocris.org/ontology/cerif#has_source> ?OUorP. ?OUorP <http://eurocris.org/ontology/cerif#has_name> ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU <http://eurocris.org/ontology/cerif#has_name> ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Νame.\n"
                + "?@#$%VAR%$#@Νame bds:search \"@#$%TERM%$#@\".",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "prod"
        );
        insertEntity("Equipment",
                "http://eurocris.org/ontology/cerif#Equipment",
                "",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Equipment.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?name bds:search \"@#$%TERM%$#@\".\n"
                + "?name bds:relevance ?score.\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ORDER BY desc(?score) \n",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Equipment.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "}",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Equipment.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "?name bds:search \"@#$%TERM%$#@\".\n"
                + "?name bds:relevance ?score.\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ORDER BY desc(?score) \n",
                false,
                "distinct ?@#$%VAR%$#@Name  ?Responsible ?Service (?object as ?uri)",
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#Equipment>.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Name.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                + "Optional {\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_destination_of> ?FLE3.\n"
                + "?FLE3 <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 <http://eurocris.org/ontology/cerif#has_source> ?OUorP. ?OUorP <http://eurocris.org/ontology/cerif#has_name> ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU <http://eurocris.org/ontology/cerif#has_name> ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Name.\n"
                + "?@#$%VAR%$#@Name bds:search \"@#$%TERM%$#@\".",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "eq"
        );
        insertEntity("Facility",
                "http://eurocris.org/ontology/cerif#Facility",
                "",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Facility.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?name bds:search \"@#$%TERM%$#@\".\n"
                + "?name bds:relevance ?score.\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ORDER BY desc(?score) \n",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Facility.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "}",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Facility.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "?name bds:search \"@#$%TERM%$#@\".\n"
                + "?name bds:relevance ?score.\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU <http://eurocris.org/ontology/cerif#has_name> ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ORDER BY desc(?score) \n",
                false,
                "distinct ?@#$%VAR%$#@Name  ?Responsible ?Service (?object as ?uri)",
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#Facility>.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Name.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                + "Optional {\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_destination_of> ?FLE3.\n"
                + "?FLE3 <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 <http://eurocris.org/ontology/cerif#has_source> ?OUorP. ?OUorP <http://eurocris.org/ontology/cerif#has_name> ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU <http://eurocris.org/ontology/cerif#has_name> ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Name.\n"
                + "?@#$%VAR%$#@Name bds:search \"@#$%TERM%$#@\".",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "fac"
        );
        insertEntity("Location",
                "http://eurocris.org/ontology/cerif#GeographicBoundingBox",
                "", "", "SELECT DISTINCT ?GBB ?east ?west ?north ?south @#$%FROM%$#@ WHERE {\n"
                + "?GBB a <http://eurocris.org/ontology/cerif#GeographicBoundingBox>.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "}", "", false, "", "", "", "", "");
    }

    private void insertRelationsMatUpdates() throws SQLException {
        insertRelationMatUpdate("OrganisationUnit-Publication",
                "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?org ?project_pub ?pub.\n"
                + "  ?pub ?pub_project ?org.\n"
                + "} WHERE {\n"
                + "  ?pub a <http://eurocris.org/ontology/cerif#Publication>.\n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "\n"
                + "  ?org <http://eurocris.org/ontology/cerif#is_source_of> ?op.\n"
                + "  ?pub <http://eurocris.org/ontology/cerif#is_destination_of> ?op.\n"
                + "     \n"
                + "  ?op <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "  Bind( IRI( concat(\"http://eurocris.org/ontology/cerif#OrganisationUnit-Publication/\",encode_for_uri(?role) )) as ?orgunit_pub ).\n"
                + "  Bind( IRI( concat(\"http://eurocris.org/ontology/cerif#Publication-OrganisationUnit/\",encode_for_uri(?role_opposite) )) as ?pub_orgunit ).\n"
                + "}");
        insertRelationMatUpdate("Person-OrganisationUnit",
                "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?pers ?person_orgunit ?org.\n"
                + "  ?org ?orgunit_person ?pers.\n"
                + "} WHERE {\n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "  ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "  {\n"
                + "    ?org <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "  } UNION {\n"
                + "    ?pou <http://eurocris.org/ontology/cerif#has_destination> ?org.\n"
                + "  }\n"
                + "  ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-OrganisationUnit/\",encode_for_uri(?role) )) as ?person_orgunit ).\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#OrganisationUnit-Person/\",encode_for_uri(?role_opposite) )) as ?orgunit_person )\n"
                + "}");
        insertRelationMatUpdate("Person-Project",
                "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?pers ?person_project ?proj.\n"
                + "  ?proj ?project_person ?pers.\n"
                + "} WHERE {\n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?proj a <http://eurocris.org/ontology/cerif#Project>.\n"
                + "\n"
                + "  ?proj <http://eurocris.org/ontology/cerif#is_source_of> ?pp.\n"
                + "  ?pers <http://eurocris.org/ontology/cerif#is_destination_of> ?pp. \n"
                + "    \n"
                + "  ?pp <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-Project/\",encode_for_uri(?role) )) as ?person_project )\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Project-Person/\",encode_for_uri(?role_opposite) )) as ?project_person )\n"
                + "}");
        insertRelationMatUpdate("Person-Publication",
                "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?pers ?person_publication ?pub.\n"
                + "  ?pub ?publication_person ?pers.\n"
                + "} WHERE {\n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?pub a <http://eurocris.org/ontology/cerif#Publication>.\n"
                + "\n"
                + "  ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pp.\n"
                + "  ?pub <http://eurocris.org/ontology/cerif#is_destination_of> ?pp. \n"
                + "  \n"
                + "  ?pp <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-Publication/\",encode_for_uri(?role) )) as ?person_publication ).\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Publication-Person/\",encode_for_uri(?role_opposite) )) as ?publication_person ).\n"
                + "}");
        insertRelationMatUpdate("Project-Publication",
                "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?proj ?project_pub ?pub.\n"
                + "  ?pub ?pub_project ?proj.\n"
                + "} WHERE {\n"
                + "  ?pub a <http://eurocris.org/ontology/cerif#Publication>.\n"
                + "  ?proj a <http://eurocris.org/ontology/cerif#Project>.\n"
                + "\n"
                + "  ?proj <http://eurocris.org/ontology/cerif#is_source_of> ?pp.\n"
                + "  ?pub <http://eurocris.org/ontology/cerif#is_destination_of> ?pp.\n"
                + "     \n"
                + "  ?pp <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Project-Publication/\",encode_for_uri(?role) )) as ?project_pub ).\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Publication-Project/\",encode_for_uri(?role_opposite) )) as ?pub_project ).\n"
                + "}");
        insertRelationMatUpdate("Project-OrganisationUnit",
                "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?proj ?project_org ?org.\n"
                + "  ?org ?org_project ?proj.\n"
                + "} WHERE {\n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "  ?proj a <http://eurocris.org/ontology/cerif#Project>.\n"
                + "\n"
                + "  ?proj <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "  {\n"
                + "    ?org <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "    ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "    ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role_opposite.\n"
                + "    ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role.\n"
                + "  } UNION {\n"
                + "    ?pou <http://eurocris.org/ontology/cerif#has_destination> ?org.\n"
                + "    ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "    ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "    ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "  }\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Project-OrganisationUnit/\",encode_for_uri(?role) )) as ?project_org ).\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#OrganisationUnit-Project/\",encode_for_uri(?role_opposite) )) as ?org_project ).\n"
                + "}");

    }

    private void insertNamedgraphCategories() throws SQLException {
        insertNamedGraphCategory("VREs");
        insertNamedGraphCategory("RIs");
    }

    private void insertNamedgraphs() throws SQLException {
        insertNamedGraph("http://ekt-data", "EKT", "", 1);
        insertNamedGraph("http://rcuk-data", "RCUK", "", 1);
        insertNamedGraph("http://fris-data", "FRIS", "", 1);
        insertNamedGraph("http://epos-data", "EPOS", "", 2);
        insertNamedGraph("http://envri-data", "ENVRI", "", 2);
    }

    public ResultSet fetchEntities() throws SQLException {
        return statement.executeQuery("select * from entity");
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException, UnsupportedEncodingException, IOException, ParseException {
        H2Manager h2 = new H2Manager();
//        h2.init();
        h2.deleteTable("entity");
        h2.createTableEntity();
        h2.insertEntities();
//        h2.createTableUserFavorites();
//        h2.terminate();

        String authorizationToken = "05ca2485-d8b3-4709-b347-ccc3f5f76e4c";
        String endpoint = "http://139.91.183.97:8080/EVREMetadataServices-1.0-SNAPSHOT";
        String namespace = "vre4eic";

//        executeRelationsMatQueries(endpoint, namespace, authorizationToken);
//        enrichMatRelationsTable(authorizationToken, endpoint, namespace);
    }

    public static void enrichMatRelationsTable(String authorizationToken, String endpoint, String namespace) throws SQLException, UnsupportedEncodingException, ClassNotFoundException, IOException {
        Connection conn = DriverManager.getConnection("jdbc:h2:~/evre", "sa", "");
        DBService.setConnection(conn);
        DBService.setJdbcTemplateUsed(false);
        JSONArray entities = DBService.retrieveAllEntities(false);
        conn = DriverManager.getConnection("jdbc:h2:~/evre", "sa", "");
        DBService.setConnection(conn);
        List<String> uris = DBService.retrieveAllNamedgraphUris();
        ////////
        conn = DriverManager.getConnection("jdbc:h2:~/evre", "sa", "");
        H2Manager h2 = new H2Manager(conn.createStatement(), conn);
        RestClient client = new RestClient(endpoint, namespace, authorizationToken);
        ////////
        for (String graphURI : uris) {
            for (int i = 0; i < entities.size(); i++) {
                JSONObject targetEntity = (JSONObject) entities.get(i);
                String targetEntityURI = (String) targetEntity.get("uri");
                int targetEntityID = (int) targetEntity.get("id");
                int cnt = 0;
                for (int j = 0; j < entities.size(); j++) {
                    if (j == i) {
                        continue;
                    }
                    StringBuilder sparqlQuery = new StringBuilder();
                    JSONObject relatedEntity = (JSONObject) entities.get(j);
                    String relatedEntityURI = (String) relatedEntity.get("uri");
                    int relatedEntityID = (int) relatedEntity.get("id");
                    sparqlQuery.append("select distinct ?relation from <" + graphURI + "> where {\n").
                            append("?target_inst a <" + targetEntityURI + ">.\n").
                            append("?target_inst ?relation [a <" + relatedEntityURI + ">].\n").
                            append("}");
                    String response = client.executeSparqlQuery(sparqlQuery.toString(), "text/csv").readEntity(String.class);
                    String[] data = response.split("\\n");
                    for (int k = 1; k < data.length; k++) {
                        String relationUri = data[k];
                        String relationName = URLDecoder.decode(relationUri, "UTF-8").substring(relationUri.lastIndexOf("/") + 1);
                        h2.insertRelation(relationUri.trim(), relationName.trim(), targetEntityID, relatedEntityID, graphURI);
                    }
                }
            }
        }
        h2.terminate();
    }

    public static void executeRelationsMatQueries(String endpoint, String namespace, String authorizationToken) throws SQLException, ParseException, ClientErrorException, IOException {
        RestClient client = new RestClient(endpoint, namespace, authorizationToken);
        Connection conn = DriverManager.getConnection("jdbc:h2:~/evre", "sa", "");
        DBService.setConnection(conn);
        DBService.setJdbcTemplateUsed(false);
        List<String> graphs = DBService.retrieveAllNamedgraphUris();
        conn = DriverManager.getConnection("jdbc:h2:~/evre", "sa", "");
        DBService.setConnection(conn);
        JSONArray updates = DBService.retrieveAllRelationsMatUpdates();
        StringBuilder sb = new StringBuilder();
        for (String graph : graphs) {
            sb.append(graph + "\n");
            for (int i = 0; i < updates.size(); i++) {
                JSONObject obj = (JSONObject) updates.get(i);
                String update = ((String) obj.get("update")).replace("@#$%FROM%$#@", "<" + graph + ">");
                String relatedEntities = (String) obj.get("related_entities");
                String response = client.executeUpdatePOSTJSON(update, namespace, authorizationToken).readEntity(String.class);
                sb.append(relatedEntities + " -> " + ((JSONObject) new JSONParser().parse(response)).get("status") + "\n");
            }
            sb.append("-------\n");
        }
        System.out.println(sb.toString());
    }

    public Connection getConnection() {
        return this.connection;
    }

}
