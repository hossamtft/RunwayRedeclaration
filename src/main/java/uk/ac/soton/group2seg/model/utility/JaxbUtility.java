package uk.ac.soton.group2seg.model.utility;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import uk.ac.soton.group2seg.model.Airport;

public class JaxbUtility {

  /**
   * Loads an airport from its XML file
   * @param xmlFileName The XML file name
   * */
  public static Airport loadAirport(String xmlFileName) {
    try{
      Path xmlPath = Path.of("src/main/Resources/" + xmlFileName);
      JAXBContext context = JAXBContext.newInstance(Airport.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();

      try(Reader reader = Files.newBufferedReader(xmlPath)) {
        return (Airport) unmarshaller.unmarshal(reader);
      } catch (IOException e) {
        e.printStackTrace();
        System.err.println(e.getMessage());
      }
    }catch (JAXBException e){
      e.printStackTrace();
      System.err.println(e.getMessage());
    }
    return null;
  }

  /**
   * Parses XML File into list of airport names and ids
   * */
  public static HashMap<String, String> parseAirports() {
    File xmlFile = new File("src/main/Resources/airportList.xml");
    HashMap<String, String> airportMap = new HashMap<>();

    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(AirportListXML.class);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

      AirportListXML airportListXML = (AirportListXML) unmarshaller.unmarshal(xmlFile);

      for(AirportXml airportXml : airportListXML.getAirports()) {
        airportMap.put(airportXml.getName(), airportXml.getId());
      }

      return airportMap;

    }catch (JAXBException e) {
      e.printStackTrace();
      System.err.println(e.getMessage());
    }
    return null;
  }

  /**
   * Adds a new airport to the system
   * 1. Saves the airport as an XML file
   * 2. Updates the airport list XML file
   *
   * @param airport The airport to add
   * @return true if the airport was added successfully, false otherwise
   */
  public boolean addAirport(Airport airport) throws JAXBException, IOException {
    // 1. Save the airport as an XML file
    String airportFileName = airport.getId() + ".xml";
    saveAirportToXml(airport, airportFileName);

    // 2. Update the airport list XML file
    updateAirportListXml(airport);

    return true;
  }

  /**
   * Saves an airport to an XML file
   *
   * @param airport The airport to save
   * @param fileName The file name to save to
   */
  private void saveAirportToXml(Airport airport, String fileName) throws JAXBException, IOException {
    Path xmlPath = Path.of("src/main/Resources/" + fileName);
    JAXBContext context = JAXBContext.newInstance(Airport.class);
    Marshaller marshaller = context.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

    try (Writer writer = new FileWriter(xmlPath.toFile())) {
      marshaller.marshal(airport, writer);
    }
  }

  private void updateAirportListXml(Airport airport) throws JAXBException, IOException {
    File xmlFile = new File("src/main/Resources/airportList.xml");

    JAXBContext jaxbContext = JAXBContext.newInstance(AirportListXML.class);
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    AirportListXML airportListXML;

    if (xmlFile.exists()) {
      airportListXML = (AirportListXML) unmarshaller.unmarshal(xmlFile);
    } else {
      // Create a new airport list if the file doesn't exist
      airportListXML = new AirportListXML();
      airportListXML.setAirports(new ArrayList<>());
    }

    // Check if the airport already exists in the list
    boolean airportExists = false;
    for (AirportXml existingAirport : airportListXML.getAirports()) {
      if (existingAirport.getId().equals(airport.getId())) {
        // Update the existing airport name if needed
        existingAirport.setName(airport.getName());
        airportExists = true;
        break;
      }
    }

    // Add the airport if it doesn't exist
    if (!airportExists) {
      AirportXml newAirportXml = new AirportXml();
      newAirportXml.setId(airport.getId());
      newAirportXml.setName(airport.getName());
      airportListXML.getAirports().add(newAirportXml);
    }

    // Save the updated airport list
    Marshaller marshaller = jaxbContext.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    marshaller.marshal(airportListXML, xmlFile);
  }

  @XmlRootElement(name = "airportList")
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class AirportListXML {

    @XmlElement(name = "airport")
    private List<AirportXml> airports;

    public List<AirportXml> getAirports() {
      return airports;
    }

    public void setAirports(List<AirportXml> airports) {
      this.airports = airports;
    }
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  public static class AirportXml {
    @XmlElement(name = "id")
    private String id;

    @XmlElement(name = "name")
    private String name;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  @XmlRootElement(name = "obstacleList")
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class ObstacleListXML {

    @XmlElement(name = "obstacle")
    private List<ObstacleXml> obstacles;

    public List<ObstacleXml> getObstacles() {
      return obstacles;
    }

    public void setObstacles(List<ObstacleXml> obstacles) {
      this.obstacles = obstacles;
    }
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  public static class ObstacleXml {

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "height")
    private int height;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getHeight() {
      return height;
    }

    public void setHeight(int height) {
      this.height = height;
    }
  }

  public static HashMap<String, String> parseObstacles() {
    File xmlFile = new File("src/main/resources/" + "obstacleList.xml"); // Ensure correct path

    HashMap<String, String> obstacleMap = new HashMap<>();

    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(ObstacleListXML.class);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

      ObstacleListXML obstacleListXML = (ObstacleListXML) unmarshaller.unmarshal(xmlFile);

      for (ObstacleXml obstacleXml : obstacleListXML.getObstacles()) {
        obstacleMap.put(obstacleXml.getName(), String.valueOf(obstacleXml.getHeight()));
      }

      return obstacleMap;

    } catch (JAXBException e) {
      e.printStackTrace();
      System.err.println(e.getMessage());
    }

    return null;
  }
}