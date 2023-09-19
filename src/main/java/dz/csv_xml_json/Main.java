package dz.csv_xml_json;

import com.google.gson.*;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import org.w3c.dom.Element;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        String fileName = "data.csv";
        String fileXml = "data.xml";
        // String fileName = "C:/Users/АБ/IdeaProjects/dzCoreXmlCsvInJson/src/main/java/data.csv";

        writeFailCsv("1,John,Smith,USA,25", fileName);
        writeFailCsv("2,Inav,Petrov,RU,23", fileName);

        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        List<Employee> list = parseCSV(columnMapping, fileName);

        String json = listToJson(list);
        String fileNameJson = "data.json";

        writeString(json, fileNameJson);

        //writeFailXml("1", "John", "Smith", "USA", "25", fileXml);
        // writeFailXml("2", "Inav", "Petrov", "RU", "23", fileXml);

        List<Employee> list2 = parseXML(fileXml);
        json = listToJson(list2);
        fileNameJson = "data2.json";
        writeString(json, fileNameJson);

        String json1 = readString("data2.json");
        List<Employee> list1 = jsonToList(json1);
        System.out.println(list1);
    }


    public static void writeFailCsv(String data, String fileName) {
        var employee = data.split(",");
        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(fileName, true))) {
            csvWriter.writeNext(employee);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    public static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> staff = null;
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy =
                    new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping("id", "firstName", "lastName", "country", "age");
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();
            staff = csv.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return staff;
    }


    public static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        String json = gson.toJson(list, listType);

        return json;
    }


    public static void writeString(String json, String fileName) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(json);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    public static void writeFailXml(String iD, String firstname,
                                    String lastname, String couNtry, String aGe, String fileXml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element staff = document.createElement("staff");
            document.appendChild(staff);

            Element employee = document.createElement("employee");
            staff.appendChild(employee);

            Element id = document.createElement("id");
            id.appendChild(document.createTextNode(iD));
            employee.appendChild(id);

            Element firstName = document.createElement("firstName");
            firstName.appendChild(document.createTextNode(firstname));
            employee.appendChild(firstName);

            Element lastName = document.createElement("lastName");
            lastName.appendChild(document.createTextNode(lastname));
            employee.appendChild(lastName);

            Element country = document.createElement("country");
            country.appendChild(document.createTextNode(couNtry));
            employee.appendChild(country);

            Element age = document.createElement("age");
            age.appendChild(document.createTextNode(aGe));
            employee.appendChild(age);

            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new FileOutputStream(fileXml, true));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(domSource, streamResult);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public static List<Employee> parseXML(String fileXml) {
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(new File(fileXml));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        Node root = doc.getDocumentElement();
        NodeList nodeList = root.getChildNodes();
        List<Employee> employeeList = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            if (!nodeList.item(i).getNodeName().equals("employee")) {
                continue;
            }
            long id = 0;
            String firstName = "";
            String lastName = "";
            String country = "";
            int age = 0;

            NodeList employeeChilds = nodeList.item(i).getChildNodes();
            for (int j = 0; j < employeeChilds.getLength(); j++) {
                if (employeeChilds.item(j).getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                switch (employeeChilds.item(j).getNodeName()) {
                    case "id": {
                        id = Long.valueOf(employeeChilds.item(j).getTextContent());
                        break;
                    }
                    case "firstName": {
                        firstName = employeeChilds.item(j).getTextContent();
                        break;
                    }
                    case "lastName": {
                        lastName = employeeChilds.item(j).getTextContent();
                        break;
                    }
                    case "country": {
                        country = employeeChilds.item(j).getTextContent();
                        break;
                    }
                    case "age": {
                        age = Integer.valueOf(employeeChilds.item(j).getTextContent());
                        break;
                    }
                }
            }
            Employee employee = new Employee(id, firstName, lastName, country, age);
            employeeList.add(employee);
        }

        return employeeList;
    }


    public static String readString(String fileJson) {
        String stringJson = null;
        try {
            stringJson = new String(Files.readAllBytes(Paths.get(fileJson)));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return stringJson;
    }


    public static List<Employee> jsonToList(String stringFileJson) {
        List<Employee> employeeList = new ArrayList<>();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Employee[] userArray = gson.fromJson(stringFileJson, Employee[].class);

        for (Employee user : userArray) {
            employeeList.add(user);
        }

        return employeeList;
    }
}

